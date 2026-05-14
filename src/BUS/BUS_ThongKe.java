package BUS;

import ConnectDB.ConnectDB;
import DAO.DAO_ThongKe;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;

public class BUS_ThongKe {

    public static class ThongKeFilter {
        public String maNV;
        public Integer ca; // 1 = Sáng, 2 = Chiều, 3 = Tối
        public LocalDateTime startTime;
        public String modeLocThoiGian; // "THANG", "QUY", "TUYCHINH"
        public Integer month;
        public Integer quarter;
        public java.sql.Date fromDate;
        public java.sql.Date toDate;
    }

    private final DAO_ThongKe dao;

    public BUS_ThongKe() {
        this.dao = new DAO_ThongKe();
    }

    private Connection getConn() {
        return ConnectDB.getInstance().getConnection();
    }

    public double tinhTienThucTe(Connection con, double tongGocCoVAT, String ghiChu) {
        double tongTienGiam = 0;
        if (ghiChu != null && !ghiChu.isEmpty()) {
            String[] parts = ghiChu.split("\\|");
            for (String p : parts) {
                p = p.trim();
                // 1. Xử lý giảm giá tiền mặt hoặc điểm thưởng (Trừ trực tiếp trên tổng thanh toán)
                if (p.startsWith("Dùng điểm: -") || p.contains("KM_GIAM:")) {
                    try {
                        tongTienGiam += Long.parseLong(p.replaceAll("[^0-9]", ""));
                    } catch (Exception ignored) {
                    }
                } 
                // 2. Xử lý giảm giá theo % Khuyến mãi
                else if (p.startsWith("KM:")) {
                    String[] mks = p.substring(3).trim().split(",");
                    for (String mk : mks) {
                        String sqlKM = "SELECT loaiHinhThuc, giaTri FROM HinhThucKhuyenMai WHERE khuyenMaiId = ?";
                        try (PreparedStatement pstKM = con.prepareStatement(sqlKM)) {
                            pstKM.setString(1, mk.trim());
                            try (ResultSet rsKM = pstKM.executeQuery()) {
                                if (rsKM.next()) {
                                    String loaiKM = rsKM.getString("loaiHinhThuc");
                                    double val = rsKM.getDouble("giaTri");
                                    
                                    if (loaiKM.contains("PHAN_TRAM") || loaiKM.contains("%")) {
                                        double uocTinhChuaVAT = tongGocCoVAT / 1.08; // Giả định mức thuế bình quân 8% hoặc dùng ratio phù hợp
                                        double tienGiamChuaVAT = uocTinhChuaVAT * (val / 100.0);
                                      
                                        tongTienGiam += Math.round(tienGiamChuaVAT * 1.08); 
                                    } else if (loaiKM.contains("TIEN_MAT")) {
                                        tongTienGiam += val;
                                    }
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
        return Math.max(0, tongGocCoVAT - tongTienGiam);
    }


    public double tinhTienThucTeCuaHoaDon(Connection con, double tongGocCoVAT, String ghiChu) {
        return tinhTienThucTe(con, tongGocCoVAT, ghiChu);
    }

    public double tinhDieuChinhDoiTra(String dateCondition, ThongKeFilter filter) {
        double dieuChinh = 0;
        List<Object[]> rawList = dao.getRawHDDoiTra(dateCondition, filter);
        for (Object[] row : rawList) {
            String loai = (String) row[0];
            String ghiChu = (String) row[1];
            if (ghiChu == null || !ghiChu.contains("|"))
                continue;
            String[] parts = ghiChu.split("\\|");

            if ("TRA_HANG".equals(loai)) {
                // parts[2] = tiền hoàn trả cho khách → trừ doanh thu
                if (parts.length >= 3) {
                    String s = parts[2].trim().replaceAll("[^0-9]", "");
                    if (!s.isEmpty())
                        dieuChinh -= Double.parseDouble(s);
                }
            } else if ("DOI_HANG".equals(loai)) {
                // parts[3]: "Hoàn X" (cửa hàng trả) hoặc "Bù X" (khách trả thêm)
                if (parts.length >= 4) {
                    String chenhLech = parts[3].trim();
                    String s = chenhLech.replaceAll("[^0-9]", "");
                    if (!s.isEmpty()) {
                        double tien = Double.parseDouble(s);
                        if (chenhLech.contains("Hoàn"))
                            dieuChinh -= tien;
                        else if (chenhLech.contains("Bù"))
                            dieuChinh += tien;
                    }
                }
            }
        }
        return dieuChinh;
    }

    public double getTienHoanTraTheoCa(String maNV, LocalDateTime start) {
        double tongHoanTra = 0;
        String sql = "SELECT id, loaiHD, ghiChu FROM HoaDon "
                + "WHERE nhanVienId = ? AND ngayLapHD >= ? AND loaiHD IN ('TRA_HANG','DOI_HANG') "
                + "AND ghiChu LIKE N'%Hoàn thành%'";
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String loai = rs.getString("loaiHD");
                String ghiChu = rs.getString("ghiChu");
                if (ghiChu != null && ghiChu.contains("|")) {
                    String[] parts = ghiChu.split("\\|");
                    if ("TRA_HANG".equals(loai) && parts.length >= 3) {
                        String s = parts[2].trim().replaceAll("[^0-9]", "");
                        if (!s.isEmpty())
                            tongHoanTra += Double.parseDouble(s);
                    } else if ("DOI_HANG".equals(loai) && parts.length >= 4) {
                        String chenhLech = parts[3].trim();
                        if (chenhLech.contains("Hoàn")) {
                            String s = chenhLech.replaceAll("[^0-9]", "");
                            if (!s.isEmpty())
                                tongHoanTra += Double.parseDouble(s);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tongHoanTra;
    }

    /**
     * Cộng dồn doanh thu thuần (trước VAT, sau KM) từ danh sách raw DAO.
     *
     * <b>Hợp đồng DAO → BUS</b>: mỗi Object[] row có cấu trúc chuẩn:
     * <pre>
     *   row[0] = id           (String)  — mã hóa đơn
     *   row[1] = tongGocCoVAT (Double)  — tổng giá niêm yết GỒM VAT
     *   row[2] = ghiChu       (String)  — chuỗi ghi chú KM
     *   row[3] = chuaVAT_ThucTe (Double) — SUM(ct.thanhTien / (1 + sp.thueVAT/100.0))
     *                                       tính đúng theo TỪNG mức thuế, KHÔNG chia đều /1.1
     * </pre>
     * DAO BẮT BUỘC cung cấp row[3] = {@code SUM(ct.thanhTien / (1 + sp.thueVAT/100.0))}.
     * Hàm KHÔNG thực hiện bất kỳ phép chia /1.1 hay nhân tỷ lệ (ratio) nào.
     */
    private double sumDoanhThuThuan(List<Object[]> rawList) {
        double total = 0;
        for (Object[] row : rawList) {
            // Tin tưởng hoàn toàn vào chuaVAT_ThucTe mà DAO đã tính đúng từng mức thuế.
            // Không dùng /1.1, không dùng ratio — tránh sai lệch hóa đơn đa thuế suất.
            if (row.length >= 4 && row[3] instanceof Double) {
                total += (Double) row[3];
            }
        }
        return total;
    }

    /**
     * Cộng dồn tiền thực thu của khách (GỒM VAT, sau KM) từ danh sách raw DAO.
     *
     * <b>Hợp đồng DAO → BUS</b>:
     * <pre>
     *   row[1] = tongGocCoVAT (Double) — giá gốc gồm VAT (trước KM)
     *   row[2] = ghiChu       (String) — chuỗi ghi chú KM để bóc tách tiền giảm
     * </pre>
     */
    private double sumThucThu(List<Object[]> rawList) {
        double total = 0;
        try (Connection con = getConn()) {
            for (Object[] row : rawList) {
                double coVAT = (Double) row[1];
                String ghiChu = (String) row[2];
                total += tinhTienThucTe(con, coVAT, ghiChu);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return total;
    }
    
    public double getTienMatBanHangTheoCa(String maNV, LocalDateTime start) {
        List<Object[]> raw = dao.getRawHDTheoCa(maNV, start, "TIEN_MAT");
        return sumThucThu(raw);
    }

    private boolean kiemTraThoiGianHople(LocalDateTime tu, LocalDateTime den) {
        return tu != null && den != null && !tu.isAfter(den);
    }
    
    public int getTongSoDonHang(LocalDateTime tuNgay, LocalDateTime denNgay) {
        return kiemTraThoiGianHople(tuNgay, denNgay) ? dao.demSoLuongHoaDon(tuNgay, denNgay) : 0;
    }

    public double getTongDoanhThu(LocalDateTime tuNgay, LocalDateTime denNgay) {
        if (!kiemTraThoiGianHople(tuNgay, denNgay))
            return 0.0;
        return sumDoanhThuThuan(dao.getRawHDByDateRange(tuNgay, denNgay));
    }

    public double getGiaTriTBTrenDon(LocalDateTime tuNgay, LocalDateTime denNgay) {
        if (!kiemTraThoiGianHople(tuNgay, denNgay))
            return 0.0;
        int so = getTongSoDonHang(tuNgay, denNgay);
        return so == 0 ? 0.0 : getTongDoanhThu(tuNgay, denNgay) / so;
    }

    public double getTongLoiNhuan(LocalDateTime tuNgay, LocalDateTime denNgay) {
        return kiemTraThoiGianHople(tuNgay, denNgay) ? dao.tinhLoiNhuan(tuNgay, denNgay) : 0.0;
    }

    public List<String[]> getDuocSiList() {
        return dao.getDuocSiList();
    }

    public List<String[]> getDanhSachNhanVien() {
        return dao.getDanhSachNhanVien();
    }

    public int getHoaDonHomNay(ThongKeFilter filter) {
        String today = java.time.LocalDate.now().toString();
        return dao.getRawHDByDay(today, filter).size();
    }

    public double getDoanhThuHomNay(ThongKeFilter filter) {
        String today = java.time.LocalDate.now().toString();
        List<Object[]> raw = dao.getRawHDByDay(today, filter);
        return sumThucThu(raw);
    }

    public double getDoanhThuThuanHomNay(ThongKeFilter filter) {
        String today = java.time.LocalDate.now().toString();
        List<Object[]> raw = dao.getRawHDByDay(today, filter);
        return sumDoanhThuThuan(raw);
    }

    public double getDoanhThuThuan7NgayQua(ThongKeFilter filter) {
        List<Object[]> raw = dao.getRawHD7NgayQua(filter, null);
        return sumDoanhThuThuan(raw);
    }

    public double getDoanhThu7NgayQua(ThongKeFilter filter) {
        List<Object[]> raw = dao.getRawHD7NgayQua(filter, null);
        return sumThucThu(raw);
    }

    public double getDoanhThuHomNay(String maNV) {
        ThongKeFilter f = new ThongKeFilter();
        f.maNV = maNV;
        return getDoanhThuHomNay(f);
    }

    public double getDoanhThuTienMatHomNay(String maNV) {
        ThongKeFilter f = new ThongKeFilter();
        f.maNV = maNV;
        List<Object[]> raw = dao.getRawHDHomNay(f, "TIEN_MAT");
        return sumThucThu(raw);
    }

    public double getDoanhThu7NgayQua(String maNV) {
        ThongKeFilter f = new ThongKeFilter();
        f.maNV = maNV;
        return getDoanhThu7NgayQua(f);
    }

    public double getDoanhThuThuan7NgayQua(String maNV) {
        ThongKeFilter f = new ThongKeFilter();
        f.maNV = maNV;
        return getDoanhThuThuan7NgayQua(f);
    }

    public double getDoanhThuHomNay(String maNV, int ca) {
        ThongKeFilter f = new ThongKeFilter();
        f.maNV = maNV;
        f.ca = ca;
        return getDoanhThuHomNay(f);
    }

    public double getDoanhThuTienMatHomNay(String maNV, int ca) {
        ThongKeFilter f = new ThongKeFilter();
        f.maNV = maNV;
        f.ca = ca;
        List<Object[]> raw = dao.getRawHDHomNay(f, "TIEN_MAT");
        return sumThucThu(raw);
    }

    public double getDoanhThuThuanHomNay(String maNV, int ca) {
        ThongKeFilter f = new ThongKeFilter();
        f.maNV = maNV;
        f.ca = ca;
        return getDoanhThuThuanHomNay(f);
    }

    public double getDoanhThuTheoCa(String maNV, LocalDateTime start) {
        List<Object[]> raw = dao.getRawHDTheoCa(maNV, start, null);
        double dt = sumThucThu(raw);
        ThongKeFilter f = new ThongKeFilter();
        f.maNV = maNV;
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        dt += tinhDieuChinhDoiTra("hd.ngayLapHD>='" + start.format(fmt) + "'", f);
        return dt;
    }

    public double getDoanhThuTienMatTheoCa(String maNV, LocalDateTime start) {
        List<Object[]> raw = dao.getRawHDTheoCa(maNV, start, "TIEN_MAT");
        double dt = sumThucThu(raw);
        ThongKeFilter f = new ThongKeFilter();
        f.maNV = maNV;
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        dt += tinhDieuChinhDoiTra("hd.ngayLapHD>='" + start.format(fmt) + "'", f);
        return dt;
    }

    public double[] getDoanhThu12Thang(int year, ThongKeFilter filter) {
        if (year <= 0) return new double[12];
        double[] data = new double[12];
        
        // 1. Gọi thẳng xuống DAO lấy list hóa đơn thực tế (Không dùng giả lập)
        List<Object[]> listHDCuaNam = dao.getRawHD12ThangChuan(year, filter); 
        
        try (Connection con = getConn()) {
            for (Object[] row : listHDCuaNam) {
                int thang = (Integer) row[0];
                String hdId = (String) row[1];
                String ghiChu = (String) row[2];
                
                // --- BẮT ĐẦU ÁP DỤNG LUẬT BÓC TÁCH LINE-ITEM TỪNG DÒNG ---
                double phanTramKM = dao.getPercentKhuyenMai(hdId);
                List<Object[]> lineItems = dao.getRawLineItems(hdId);
                
                double doanhThuThuanHD = 0;
                
                for (Object[] item : lineItems) {
                    int soLuong = (int) item[0];
                    double giaNiemyet = (double) item[1];
                    
                    // Toán học chuẩn: Tính tiền gốc -> Tiền giảm -> Làm tròn thành tiền chưa thuế
                    double tienGocDong = soLuong * giaNiemyet;
                    double tienGiamDong = tienGocDong * (phanTramKM / 100.0);
                    double thanhTienChuaThue = Math.round(tienGocDong - tienGiamDong);
                    
                    doanhThuThuanHD += thanhTienChuaThue;
                }
                
                // Trừ thêm điểm tích lũy khách dùng (Trừ trực tiếp vào doanh thu thuần vì là khoản giảm trừ)
                double tienDiemTru = 0;
                if (ghiChu != null && ghiChu.contains("Dùng điểm: -")) {
                    try {
                        String diemStr = ghiChu.substring(ghiChu.indexOf("-") + 1).replaceAll("[^0-9]", "");
                        tienDiemTru = Double.parseDouble(diemStr);
                    } catch (Exception ignored) {}
                }
                
                // Cộng dồn doanh thu thuần của hóa đơn vào đúng tháng
                if (thang >= 1 && thang <= 12) {
                    data[thang - 1] += Math.max(0, doanhThuThuanHD - tienDiemTru) / 1_000_000.0;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        // 2. Khấu trừ tiền Đổi/Trả hàng (Logic cũ của ông viết đã rất chuẩn, giữ nguyên)
        List<Object[]> doiTraList = dao.getRawHDDoiTra12Thang(year, filter);
        for (Object[] row : doiTraList) {
            int m = (Integer) row[0];
            double refundChuaVAT = (Double) row[1]; // Hàm getRawHDDoiTra12Thang đã tính bóc VAT rồi
            if (m >= 1 && m <= 12 && refundChuaVAT > 0) {
                data[m - 1] -= refundChuaVAT / 1_000_000.0;
            }
        }
        
        // Chống hiển thị số âm trên biểu đồ
        for(int i = 0; i < 12; i++) data[i] = Math.max(0, data[i]); 
        return data;
    }

    public double[] getChiPhi12Thang(int year, ThongKeFilter filter) {
        if (year <= 0)
            return new double[12];
        return dao.getChiPhi12Thang(year, filter);
    }

    public double[] getLoiNhuan12Thang(int year, ThongKeFilter filter) {
        if (year <= 0)
            return new double[12];
        double[] dt = getDoanhThu12Thang(year, filter);
        double[] cp = getChiPhi12Thang(year, filter);
        double[] ln = new double[12];
        for (int i = 0; i < 12; i++)
            ln[i] = dt[i] - cp[i];
        return ln;
    }

    /**
     * DAO ({@code getRawHD30Ngay}) PHẢI trả về: {date[0], ?(1), coVAT[2], ghiChu[3], chuaVAT_ThucTe[4]}.
     */
    public List<Object[]> getThongKe30NgayGanNhat(ThongKeFilter filter) {
        List<Object[]> rawList = dao.getRawHD30Ngay(filter);
        Map<String, double[]> dailyData = new LinkedHashMap<>();
        for (Object[] row : rawList) {
            String date = (String) row[0];
            // chuaVAT_ThucTe: DAO đã tính đúng từng mức VAT — không ratio, không /1.1
            double dt = (row.length >= 5 && row[4] instanceof Double) ? (Double) row[4] : 0.0;
            dailyData.putIfAbsent(date, new double[] { 0, 0 });
            dailyData.get(date)[0] += 1;
            dailyData.get(date)[1] += dt;
        }
        List<Object[]> result = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : dailyData.entrySet())
            result.add(new Object[] { entry.getKey(), (int) entry.getValue()[0], entry.getValue()[1] / 1_000_000.0 });
        return result;
    }

    /**
     * DAO ({@code getRawHDCaByTime}) PHẢI trả về: {id[0], coVAT[1], ghiChu[2], chuaVAT_ThucTe[3]}.
     */
    private double[] getKetQuaCaByTime(String nvId, int year, ThongKeFilter filter, int startHour, int endHour) {
        if (nvId == null || nvId.isEmpty()) return new double[] { 0, 0 };
        List<Object[]> rawList = dao.getRawHDCaByTime(nvId, year, filter, startHour, endHour);
        Set<String> hdIds = new HashSet<>();
        double totalDT = 0;
        for (Object[] row : rawList) {
            String hdId = (String) row[0];
            hdIds.add(hdId);
            // chuaVAT_ThucTe từ DAO — không ratio, không /1.1
            if (row.length >= 4 && row[3] instanceof Double) {
                totalDT += (Double) row[3];
            }
        }
        return new double[] { hdIds.size(), totalDT / 1_000_000.0 };
    }
    public double[] getKetQuaCaSang(String nvId, int year, ThongKeFilter filter) {
        return getKetQuaCaByTime(nvId, year, filter, 6, 13);
    }

    public double[] getKetQuaCaChieu(String nvId, int year, ThongKeFilter filter) {
        return getKetQuaCaByTime(nvId, year, filter, 14, 21);
    }

    public double[] getKetQuaCaToi(String nvId, int year, ThongKeFilter filter) {
        return getKetQuaCaByTime(nvId, year, filter, 22, 5);
    }

    public double[] getDTTheoGioTrongNgay(String dateYMD) {
        return getDTTheoGioTrongNgay(dateYMD, null);
    }

    public double[] getDTTheoGioTrongNgay(String dateYMD, ThongKeFilter filter) {
        if (dateYMD == null || dateYMD.isEmpty()) return new double[24];
        double[] data = new double[24];
        
        List<Object[]> rawList = dao.getRawHDGioTrongNgay(dateYMD, filter);
        for (Object[] row : rawList) {
            int h = (Integer) row[0];
            if (h >= 0 && h < 24) {
                data[h] += (Double) row[1] / 1_000_000.0;
            }
        }

        // Trừ đi tiền khách trả hàng
        List<Object[]> doiTraList = dao.getRawHDDoiTraGio("CAST(hd.ngayLapHD AS DATE)='" + dateYMD + "'", filter);
        for (Object[] row : doiTraList) {
            int h = (Integer) row[2];
            double chuaVATRefund = (row.length >= 4 && row[3] instanceof Double) ? (Double) row[3] : 0;
            if (h >= 0 && h < 24 && chuaVATRefund > 0) {
                data[h] -= chuaVATRefund / 1_000_000.0;
            }
        }
        return data;
    }

    public List<Object[]> getTopSPTrongNgay(String dateYMD) {
        return getTopSPTrongNgay(dateYMD, null);
    }

    public List<Object[]> getTopSPTrongNgay(String dateYMD, ThongKeFilter filter) {
        if (dateYMD == null || dateYMD.isEmpty()) return new ArrayList<>();
        Map<String, double[]> spMap = new LinkedHashMap<>();
        
        // 1. Cộng hàng bán
        List<Object[]> hdList = dao.getRawHDForTopSP(dateYMD, filter);
        for (Object[] hd : hdList) {
            String hdId = (String) hd[0];
            List<Object[]> ctList = dao.getRawCTHD(hdId);
            for (Object[] ct : ctList) {
                String ten = (String) ct[0];
                int sl = (Integer) ct[1];
                double dtSP = (ct.length >= 3 && ct[2] instanceof Double) ? (Double) ct[2] : 0.0;
                double[] cur = spMap.getOrDefault(ten, new double[] { 0, 0 });
                cur[0] += sl;
                cur[1] += dtSP;
                spMap.put(ten, cur);
            }
        }
        
        // 2. Trừ hàng bị trả lại
        List<Object[]> traList = dao.getTraHangChiTiet(dateYMD, filter);
        for (Object[] tra : traList) {
            String ten = (String) tra[0];
            int sl = (Integer) tra[1];
            double dtSP = (Double) tra[2];
            if (spMap.containsKey(ten)) {
                double[] cur = spMap.get(ten);
                cur[0] -= sl;
                cur[1] -= dtSP;
                if (cur[0] <= 0) spMap.remove(ten); 
                else spMap.put(ten, cur);
            }
        }

        List<Object[]> result = new ArrayList<>();
        spMap.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue()[1], a.getValue()[1]))
                .limit(10)
                .forEach(e -> result.add(new Object[] {
                        e.getKey(), (int) e.getValue()[0], e.getValue()[1] / 1_000_000.0 }));
        return result;
    }
    /**
     * DAO ({@code getRawKHHD}) PHẢI trả về: {ten[0], diem[1], coVAT[2], ghiChu[3], chuaVAT_ThucTe[4]}.
     */
    public List<Object[]> getTopKhachHang(int year, ThongKeFilter filter) {
        Map<String, double[]> khMap = new LinkedHashMap<>();
        List<Object[]> rawList = dao.getRawKHHD(year, filter);
        for (Object[] row : rawList) {
            String ten = (String) row[0];
            int diem = (Integer) row[1];
            // chuaVAT_ThucTe từ DAO — không ratio, không /1.1
            double dtThuan = (row.length >= 5 && row[4] instanceof Double)
                    ? (Double) row[4] / 1_000_000.0
                    : 0.0;
            double[] cur = khMap.getOrDefault(ten, new double[] { 0, 0, diem });
            cur[0] += 1;
            cur[1] += dtThuan;
            cur[2] = diem;
            khMap.put(ten, cur);
        }
        List<Object[]> result = new ArrayList<>();
        khMap.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue()[1], a.getValue()[1]))
                .limit(10)
                .forEach(e -> result.add(new Object[] {
                        e.getKey(), (int) e.getValue()[0], e.getValue()[1], (int) e.getValue()[2] }));
        return result;
    }

    public List<Object[]> getHoaDonGanDayTrongCa(String maNV, int ca) {
        ThongKeFilter f = new ThongKeFilter();
        f.maNV = maNV;
        f.ca = ca;
        List<Object[]> rawList = dao.getRawHDDashboard(f, 100, false);
        List<Object[]> result = new ArrayList<>();
        try (Connection con = getConn()) {
            for (Object[] row : rawList) {
                String id = (String) row[0];
                String kh = (String) row[1];
                double coVAT = (Double) row[2];
                String gc = (String) row[3];
                String pttt = (String) row[4];
                double thucThu = tinhTienThucTe(con, coVAT, gc);
                result.add(new Object[] { id, kh, thucThu, pttt });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Object[]> getHoaDonGanDayTrongCa(ThongKeFilter f) {
        List<Object[]> rawList = dao.getRawHDDashboard(f, 100, false);
        List<Object[]> result = new ArrayList<>();
        try (Connection con = getConn()) {
            for (Object[] row : rawList) {
                double thucThu = 0;
                String loai = (String) row[6];
                if ("BAN_HANG".equals(loai)) {
                    thucThu = tinhTienThucTe(con, (Double) row[2], (String) row[3]);
                } else {
                    thucThu = -parseTienHoanTra(loai, (String) row[3]);
                }
                result.add(new Object[] { row[0], row[1], thucThu, row[4], loai });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }
    
    public List<Object[]> getHoaDonGanDayTrongCa(String maNV) {
        return getHoaDonGanDayTrongCa(maNV, 0);
    }

    private double parseTienHoanTra(String loai, String ghiChu) {
        double tien = 0;
        if (ghiChu == null || !ghiChu.contains("|")) return 0;
        String[] parts = ghiChu.split("\\|");
        if ("TRA_HANG".equals(loai) && parts.length >= 3) {
            String s = parts[2].trim().replaceAll("[^0-9]", "");
            if (!s.isEmpty()) tien = Double.parseDouble(s);
        } else if ("DOI_HANG".equals(loai) && parts.length >= 4) {
            String chenhLech = parts[3].trim();
            if (chenhLech.contains("Hoàn")) {
                String s = chenhLech.replaceAll("[^0-9]", "");
                if (!s.isEmpty()) tien = Double.parseDouble(s);
            }
        }
        return tien;
    }
  
    public List<Object[]> getHoaDonGanNhat(ThongKeFilter filter, int limit) {
        List<Object[]> rawList = dao.getRawHDDashboard(filter, limit, true);
        List<Object[]> result = new ArrayList<>();
        try (Connection con = getConn()) {
            for (Object[] row : rawList) {
                String id = (String) row[0];
                String kh = (String) row[1];
                double coVAT = (Double) row[2];
                String gc = (String) row[3];
                String gio = (String) row[5];
                String loai = (String) row[6]; 

                double thucThu = 0;
                if ("BAN_HANG".equals(loai)) {
                    // FIX: Hiển thị đúng số tiền KHÁCH THỰC TRẢ (Đã có VAT), KHÔNG CHIA 1.1 NỮA
                    thucThu = tinhTienThucTe(con, coVAT, gc); 
                } else {
                    thucThu = -parseTienHoanTra(loai, gc);
                }
                result.add(new Object[] { id, kh, thucThu, gio, loai });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    public List<Object[]> getHoaDonGanNhat(String maNV, int ca, int limit) {
        ThongKeFilter f = new ThongKeFilter();
        f.maNV = maNV;
        f.ca = ca;
        return getHoaDonGanNhat(f, limit);
    }

    public List<Object[]> getHoaDonGiaTriCaoHomNay(int limit) {
        return getHoaDonGiaTriCaoHomNay(limit, null);
    }

    public List<Object[]> getHoaDonGiaTriCaoHomNay(int limit, String maNV) {
        ThongKeFilter f = new ThongKeFilter();
        f.maNV = maNV;
        List<Object[]> rawList = dao.getRawHDGiaTriCao(limit, f);
        List<Object[]> result = new ArrayList<>();
        try (Connection con = getConn()) {
            for (Object[] row : rawList) {
                String id = (String) row[0];
                String kh = (String) row[1];
                double coVAT = (Double) row[2];
                String gc = (String) row[3];
                double thucThu = tinhTienThucTe(con, coVAT, gc);
                String gio = (row.length > 5) ? (String) row[5] : null;
                result.add(new Object[] { id, kh, thucThu, gio });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public double[] getKpiDoiChieu(ThongKeFilter f) {
        int soHD = dao.getSoHoaDonHomNay(f);
        List<Object[]> rows = dao.getRawHDByDay(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")), f);

        double tGocChuaVAT = 0;       
        double tKhuyenMai = 0;        
        double tDoanhThuThuan = 0;    
        double tVAT = 0;              
        double tThucThuBanHang = 0;   
        double tMat = 0;              
        double tCK = 0;               

        try (Connection con = getConn()) {
            for (Object[] row : rows) {
                String hdId = (String) row[0];
                String pttt = dao.getPTTT(con, hdId);
                
                double phanTramKM = dao.getPercentKhuyenMai(hdId);
                List<Object[]> lineItems = dao.getRawLineItems(hdId);
                
                double tienGocHD = 0;
                double doanhThuThuanHD = 0;
                double vatHD = 0;

                for (Object[] item : lineItems) {
                    int soLuong = (int) item[0];
                    double giaNiemyet = (double) item[1]; 
                    double thueSuat = (double) item[2];   

                    double tienGocDong = soLuong * giaNiemyet;
                    double tienGiamDong = tienGocDong * (phanTramKM / 100.0);
                    double thanhTienChuaThue = Math.round(tienGocDong - tienGiamDong);
                    double tienVATCuaDong = Math.round(thanhTienChuaThue * (thueSuat / 100.0));

                    tienGocHD += tienGocDong;
                    doanhThuThuanHD += thanhTienChuaThue;
                    vatHD += tienVATCuaDong;
                }
                
                double tienDiemTru = 0;
                String ghiChu = (String) row[2];
                if (ghiChu != null && ghiChu.contains("Dùng điểm: -")) {
                    try {
                        String diemStr = ghiChu.substring(ghiChu.indexOf("-") + 1).replaceAll("[^0-9]", "");
                        tienDiemTru = Double.parseDouble(diemStr);
                    } catch (Exception ignored) {}
                }
                
                double thucThuHD = Math.max(0, doanhThuThuanHD + vatHD - tienDiemTru);
                
                tGocChuaVAT += tienGocHD;
                tKhuyenMai += (tienGocHD - doanhThuThuanHD); 
                tDoanhThuThuan += doanhThuThuanHD;
                tVAT += vatHD;
                tThucThuBanHang += thucThuHD;
                
                if ("TIEN_MAT".equalsIgnoreCase(pttt)) tMat += thucThuHD;
                else tCK += thucThuHD;
            }
        } catch (Exception e) { e.printStackTrace(); }

        double[] traHang = dao.getTienTraHangChinhXac("CAST(hd.ngayLapHD AS DATE) = CAST(GETDATE() AS DATE)", f);
        double tHoanTraCoVAT = traHang[0];
        double tHoanTraChuaVAT = traHang[1];
        
        // PỘT NHỚ DÒNG NÀY ĐỂ TRỪ ĐI SỐ PHIẾU BỊ TRẢ NHÉ
        int soPhieuTra = dao.getTongPhieuDoiTra(f);

        return new double[] { 
            Math.max(0, soHD - soPhieuTra), // Số HĐ ròng
            tGocChuaVAT,       
            tKhuyenMai,          
            tVAT,        
            tThucThuBanHang,   
            tMat,                
            tCK,                 
            tHoanTraCoVAT,       
            Math.max(0, tDoanhThuThuan - tHoanTraChuaVAT) // Doanh thu ròng
        };
    }
    
    public double[] getKpiDoiChieu7NgayQua(String maNV) {
        ThongKeFilter f = new ThongKeFilter(); f.maNV = maNV;
        int hd7 = dao.getSoHoaDon7NgayQua(f);
        List<Object[]> rows = dao.getRawHD7NgayQua(f, null);

        double tGocChuaVAT = 0;
        double tKhuyenMai = 0;
        double tDoanhThuThuan = 0;
        double tVAT = 0;
        double tThucThuBanHang = 0;

        try (Connection con = getConn()) {
            for (Object[] row : rows) {
                String hdId = (String) row[0];
                
                double phanTramKM = dao.getPercentKhuyenMai(hdId);
                List<Object[]> lineItems = dao.getRawLineItems(hdId);
                
                double gocHD = 0;
                double doanhThuThuanHD = 0;
                double vatHD = 0;

                for (Object[] item : lineItems) {
                    int soLuong = (int) item[0];
                    double giaNiemyet = (double) item[1];
                    double thueSuat = (double) item[2];

                    double tienGocDong = soLuong * giaNiemyet;
                    double tienGiamDong = tienGocDong * (phanTramKM / 100.0);
                    double thanhTienChuaThue = Math.round(tienGocDong - tienGiamDong);
                    double tienVATCuaDong = Math.round(thanhTienChuaThue * (thueSuat / 100.0));

                    gocHD += tienGocDong;
                    doanhThuThuanHD += thanhTienChuaThue;
                    vatHD += tienVATCuaDong;
                }
                
                double tienDiemTru = 0;
                String ghiChu = (String) row[2];
                if (ghiChu != null && ghiChu.contains("Dùng điểm: -")) {
                    try {
                        String diemStr = ghiChu.substring(ghiChu.indexOf("-") + 1).replaceAll("[^0-9]", "");
                        tienDiemTru = Double.parseDouble(diemStr);
                    } catch (Exception ignored) {}
                }
                
                tGocChuaVAT += gocHD;
                tKhuyenMai += (gocHD - doanhThuThuanHD);
                tDoanhThuThuan += doanhThuThuanHD;
                tVAT += vatHD;
                tThucThuBanHang += Math.max(0, doanhThuThuanHD + vatHD - tienDiemTru);
            }
        } catch (Exception e) { e.printStackTrace(); }

        double[] traHang = dao.getTienTraHangChinhXac("hd.ngayLapHD >= DATEADD(DAY, -7, GETDATE())", f);
        double tHoanTraCoVAT   = traHang[0];
        double tHoanTraChuaVAT = traHang[1];
        
        int soPhieuTra = dao.getTongPhieuDoiTra(f);

        return new double[] { 
            Math.max(0, hd7 - soPhieuTra), 
            tGocChuaVAT, 
            tKhuyenMai, 
            tVAT, 
            tThucThuBanHang, 
            0, 
            0, 
            tHoanTraCoVAT, 
            Math.max(0, tDoanhThuThuan - tHoanTraChuaVAT) 
        };
    }
    public double getTienChuyenKhoan(String maNV, int ca) {
        return Math.max(0, getDoanhThuHomNay(maNV, ca) - getDoanhThuTienMatHomNay(maNV, ca));
    }

    public double getDieuChinhDoiTraHomNay(String maNV, int ca) {
        ThongKeFilter f = new ThongKeFilter();
        f.maNV = maNV;
        f.ca = ca;
        return tinhDieuChinhDoiTra("CONVERT(DATE,hd.ngayLapHD)=CONVERT(DATE,GETDATE())", f);
    }

    public double getDieuChinhDoiTra7NgayQua(String maNV) {
        ThongKeFilter f = new ThongKeFilter();
        f.maNV = maNV;
        return tinhDieuChinhDoiTra("hd.ngayLapHD>=DATEADD(DAY,-7,GETDATE())", f);
    }

    public int getTongSanPham() {
        return dao.getTongSanPham();
    }

    public int getTongKhachHang() {
        return dao.getTongKhachHang();
    }

    public int getHoaDonHomNay(String maNV) {
        ThongKeFilter f = new ThongKeFilter();
        f.maNV = maNV;
        return dao.getSoHoaDonHomNay(f);
    }

    public int getHoaDonHomNay(String maNV, int ca) {
        ThongKeFilter f = new ThongKeFilter();
        f.maNV = maNV;
        f.ca = ca;
        return dao.getSoHoaDonHomNay(f);
    }

    public List<Object[]> getTop4SanPhamSapHetHang() {
        return dao.getTop4SanPhamSapHetHang();
    }

    public int getSoSanPhamDuTon() {
        return dao.getSoSanPhamDuTon();
    }

    public int getSoLoHangSapHetHanKhoang(int days) {
        return dao.getSoLoHangSapHetHanKhoang(days);
    }

    public List<Object[]> getLoHangSapHetHanNhanh(int days) {
        return dao.getLoHangSapHetHanNhanh(days);
    }

    public int getSoHoaDon7NgayQua(String maNV) {
        ThongKeFilter f = new ThongKeFilter();
        f.maNV = maNV;
        return dao.getSoHoaDon7NgayQua(f);
    }

    public int getTongPhieuDoiTra(ThongKeFilter filter) {
        return dao.getTongPhieuDoiTra(filter);
    }

    public int getPhieuDoiTraChoXuLy(ThongKeFilter filter) {
        return dao.getPhieuDoiTraChoXuLy(filter);
    }

    public int getSoHoaDonTheoCa(String maNV, LocalDateTime s) {
        return dao.getSoHoaDonTheoCa(maNV, s);
    }

    public List<String[]> getDuocSiListFull() {
        return dao.getDuocSiList();
    }

    public long getTongHoaDon(int year, ThongKeFilter filter) {
        return dao.getTongHoaDon(year, filter);
    }

    public int[] getSoLuongTheoLoaiSP(int year, ThongKeFilter c) {
        return dao.getSoLuongTheoLoaiSP(year, c);
    }

    public List<String> get10NgayGanNhat(int year, ThongKeFilter c) {
        return dao.get10NgayGanNhat(year, c);
    }

    public int getDailyHDCuaNV(String id, String d, ThongKeFilter c) {
        return dao.getDailyHDCuaNV(id, d, c);
    }

    public List<Object[]> getTopSanPham(int year, ThongKeFilter c) {
        return dao.getTopSanPham(year, c);
    }

    public List<Object[]> getVATReport(int year, ThongKeFilter c) {
        return dao.getVATReport(year, c);
    }

    public List<Object[]> getSpSapHetHan() {
        return dao.getSpSapHetHan();
    }

    public Object[] getThongKeNgayCuThe(String d) {
        return dao.getThongKeNgayCuThe(d);
    }

    public List<Object[]> getNVTrongNgay(String d) {
        return dao.getNVTrongNgay(d);
    }

    public int[] getKHMoiTheoThang(int year) {
        return dao.getKHMoiTheoThang(year);
    }

    public Object[] getKpiKhachHang() {
        return dao.getKpiKhachHang();
    }

    public List<Object[]> getTopKhachHangTheoDiem(int limit) {
        return dao.getTopKhachHangTheoDiem(limit);
    }

    public double getTongGiaTriTonKho() {
        return dao.getTongGiaTriTonKho();
    }

    public Object[] getSoLoTheoTrangThai() {
        return dao.getSoLoTheoTrangThai();
    }

    public List<Object[]> getTonKhoTheoKho() {
        return dao.getTonKhoTheoKho();
    }

    public List<Object[]> getTopSPTonNhieu() {
        return dao.getTopSPTonNhieu();
    }

    public double[] getNhapHang12Thang(int year) {
        return dao.getNhapHang12Thang(year);
    }

    public List<Object[]> getGoiYKhuyenMai(int year) {
        return dao.getGoiYKhuyenMai(year);
    }

    public List<Object[]> getKhachHangVIPMuaHomNay(int limit) {
        return dao.getKhachHangVIPMuaHomNay(limit);
    }

    public static final long[] MENH_GIA = { 500000, 200000, 100000, 50000, 20000, 10000, 5000, 2000, 1000 };

    public long tinhTongTienDauCa(int[] soLuong) {
        long t = 0;
        for (int i = 0; i < Math.min(soLuong.length, MENH_GIA.length); i++) {
            t += soLuong[i] * MENH_GIA[i];
        }
        return t;
    }

    public double[] tinhToanTaiChinhHoaDon(String hdId) {
        // 1. Gọi DAO lấy dữ liệu thô
        double phanTramKM = dao.getPercentKhuyenMai(hdId);
        List<Object[]> rawItems = dao.getRawLineItems(hdId);

        double tongDoanhThuThuan = 0; // Tổng chưa thuế
        double tongTienThueVAT = 0;   // Tổng tiền thuế

        // 2. Thực hiện Business Logic trên từng dòng (Line-Item)
        for (Object[] item : rawItems) {
            int soLuong = (int) item[0];
            double giaNiemyet = (double) item[1];
            double thueSuat = (double) item[2];

            // Công thức tính trên từng dòng theo đúng luật
            double thanhTienChuaThue = Math.round((soLuong * giaNiemyet) * (1 - phanTramKM / 100.0));
            double tienVATCuaDong = Math.round(thanhTienChuaThue * (thueSuat / 100.0));

            tongDoanhThuThuan += thanhTienChuaThue;
            tongTienThueVAT += tienVATCuaDong;
        }

        // Trả về mảng: [Doanh thu thuần, Tổng VAT, Tổng thanh toán]
        return new double[] { 
            tongDoanhThuThuan, 
            tongTienThueVAT, 
            tongDoanhThuThuan + tongTienThueVAT 
        };
    }
    public double[] getKpiTongQuat(ThongKeFilter f) {
        // 1. Lấy danh sách ID hóa đơn thô dựa theo bộ lọc (Ngày/Tháng/NV/Ca)
        List<Object[]> rows = dao.getRawHDByFilter(f); 

        double tGoc = 0, tKM = 0, tThuannChuaVAT = 0, tVAT = 0, tThucThu = 0;
        int soHD = rows.size();

        try (Connection con = getConn()) {
            for (Object[] row : rows) {
                String hdId = (String) row[0];
                double phanTramKM = dao.getPercentKhuyenMai(hdId);
                List<Object[]> items = dao.getRawLineItems(hdId);

                for (Object[] it : items) {
                    int sl = (int) it[0];
                    double gia = (double) it[1];
                    double vatRate = (double) it[2];

                    double gocDong = sl * gia;
                    double giamDong = gocDong * (phanTramKM / 100.0);
                    double thuanDong = Math.round(gocDong - giamDong);
                    double vatDong = Math.round(thuanDong * (vatRate / 100.0));

                    tGoc += gocDong;
                    tKM += giamDong;
                    tThuannChuaVAT += thuanDong;
                    tVAT += vatDong;
                }
                // Tiền khách đưa thực tế (sau điểm thưởng)
                tThucThu += tinhTienThucTe(con, (tThuannChuaVAT + tVAT), (String)row[2]);
            }
        } catch (Exception e) { e.printStackTrace(); }

        // 2. Khấu trừ tiền Đổi/Trả hàng
        double[] tra = dao.getTienTraHangChinhXac(dao.buildConditionFromFilter(f), f);
        
        return new double[] {
            Math.max(0, tThuannChuaVAT - tra[1]), // Index 0: Doanh thu thuần ròng
            Math.max(0, soHD - dao.getTongPhieuDoiTra(f)), // Index 1: Số HĐ ròng
            0, // Index 2: Sản phẩm (tính ở hàm riêng)
            0, // Index 3: Khách hàng (tính ở hàm riêng)
            tVAT - (tra[0] - tra[1]), // Index 4: Thuế VAT ròng
            tKM // Index 5: Tổng khuyến mãi
        };
    }
}