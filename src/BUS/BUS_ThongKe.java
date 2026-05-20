package BUS;

import DAO.DAO_ThongKe;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

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
    private double tGiaVon; 

    public BUS_ThongKe() {
        this.dao = new DAO_ThongKe();
    }

    public double tinhTienThucTe(double tongGocCoVAT, String ghiChu) {
        double tongTienGiam = 0;
        if (ghiChu != null && !ghiChu.isEmpty()) {
            String[] parts = ghiChu.split("\\|");
            for (String p : parts) {
                p = p.trim();
                if (p.startsWith("Dùng điểm: -") || p.contains("KM_GIAM:")) {
                    try {
                        tongTienGiam += Long.parseLong(p.replaceAll("[^0-9]", ""));
                    } catch (Exception ignored) {}
                } 
            }
        }
        return Math.max(0, tongGocCoVAT - tongTienGiam);
    }

    public double tinhTienThucTeCuaHoaDon(double tongGocCoVAT, String ghiChu) {
        return tinhTienThucTe(tongGocCoVAT, ghiChu);
    }

    public double tinhDieuChinhDoiTra(String dateCondition, ThongKeFilter filter) {
        double dieuChinh = 0;
        List<Object[]> rawList = dao.getRawHDDoiTra(dateCondition, filter);
        for (Object[] row : rawList) {
            String loai = (String) row[0];
            String ghiChu = (String) row[1];
            if (ghiChu == null || !ghiChu.contains("|")) continue;
            String[] parts = ghiChu.split("\\|");

            if ("TRA_HANG".equals(loai)) {
                if (parts.length >= 3) {
                    String s = parts[2].trim().replaceAll("[^0-9]", "");
                    if (!s.isEmpty()) dieuChinh -= Double.parseDouble(s);
                }
            } else if ("DOI_HANG".equals(loai)) {
                if (parts.length >= 4) {
                    String chenhLech = parts[3].trim();
                    String s = chenhLech.replaceAll("[^0-9]", "");
                    if (!s.isEmpty()) {
                        double tien = Double.parseDouble(s);
                        if (chenhLech.contains("Hoàn")) dieuChinh -= tien;
                        else if (chenhLech.contains("Bù")) dieuChinh += tien;
                    }
                }
            }
        }
        return dieuChinh;
    }

    public double getTienHoanTraTheoCa(String maNV, LocalDateTime start) {
        return dao.getTienHoanTraTheoCa(maNV, start);
    }

    private double sumDoanhThuThuan(List<Object[]> rawList) {
        double total = 0;
        for (Object[] row : rawList) {
            if (row.length >= 4 && row[3] instanceof Double) total += (Double) row[3];
        }
        return total;
    }

    private double sumThucThu(List<Object[]> rawList) {
        double total = 0;
        for (Object[] row : rawList) {
            double coVAT = (Double) row[1];
            String ghiChu = (String) row[2];
            total += tinhTienThucTe(coVAT, ghiChu);
        }
        return total;
    }
    
    public double getTienMatBanHangTheoCa(String maNV, LocalDateTime start) {
        List<Object[]> raw = dao.getRawHDTheoCa(maNV, start, "TIEN_MAT");
        return sumThucThu(raw);
    }

    /**
     * Doanh thu bán hàng bằng CHUYỂN KHOẢN trong ca.
     * Tính bằng: Tổng bán hàng - Tiền mặt bán hàng
     * (Tránh vấn đề enum CHUYEN_KHOAN vs CHUYEN_KHOAN_NGAN_HANG)
     * Chỉ dùng để HIỂN THỊ — không ảnh hưởng công thức quỹ tiền mặt.
     */
    public double getTienCKBanHangTheoCa(String maNV, LocalDateTime start) {
        List<Object[]> rawAll = dao.getRawHDTheoCa(maNV, start, null);
        List<Object[]> rawMat = dao.getRawHDTheoCa(maNV, start, "TIEN_MAT");
        return Math.max(0, sumThucThu(rawAll) - sumThucThu(rawMat));
    }

    // === BUG 1 + BUG 3 FIX ===
    // Thay thế tinhDieuChinhDoiTra() (parse ghiChu fragile) bằng
    // dao.getTienDoiHangTheoCa() / dao.getTienDoiHangTheoCaMat() (query SQL chính xác)

    /**
     * Bọc dao.getTienDoiHangTheoCaMat() để BUS_CaLamViec dùng khi tính tienHeThongGhiNhan.
     * Trả về double[2]: [0]=tienBuThem, [1]=tienHoanLai (chỉ giao dịch tiền mặt)
     */
    public double[] getTienDoiHangMatTheoCa(String maNV, LocalDateTime start) {
        return dao.getTienDoiHangTheoCaMat(maNV, start);
    }

    /**
     * Tổng tiền đổi hàng theo ca — TẤT CẢ phương thức thanh toán (dùng cho tab Đối chiếu doanh thu).
     * Trả về double[2]: [0]=tienBuThem (khách trả thêm), [1]=tienHoanLai (tiệm hoàn lại)
     */
    public double[] getDoiHangSummaryTheoCa(String maNV, LocalDateTime start) {
        return dao.getTienDoiHangTheoCa(maNV, start);
    }

    // BUG 1 FIX: Không còn cộng tinhDieuChinhDoiTra() (parse ghiChu, bỏ sót DOI_HANG)
    // Thay bằng dao.getTienDoiHangTheoCa() trả về số tiền chính xác từ DB
    public double getDoanhThuTheoCa(String maNV, LocalDateTime start) {
        List<Object[]> raw = dao.getRawHDTheoCa(maNV, start, null);
        double doanhThuBanHang = sumThucThu(raw);
        double[] doiHang = dao.getTienDoiHangTheoCa(maNV, start);
        // doiHang[0] = tienBuThem (khách trả thêm → tăng doanh thu)
        // doiHang[1] = tienHoanLai (tiệm hoàn lại  → giảm doanh thu)
        return doanhThuBanHang + doiHang[0] - doiHang[1];
    }

    // BUG 3 FIX: Tương tự getDoanhThuTheoCa() nhưng chỉ tính tiền mặt
    public double getDoanhThuTienMatTheoCa(String maNV, LocalDateTime start) {
        List<Object[]> raw = dao.getRawHDTheoCa(maNV, start, "TIEN_MAT");
        double doanhThuBanHang = sumThucThu(raw);
        double[] doiMat = dao.getTienDoiHangTheoCaMat(maNV, start);
        return doanhThuBanHang + doiMat[0] - doiMat[1];
    }

    private boolean kiemTraThoiGianHople(LocalDateTime tu, LocalDateTime den) {
        return tu != null && den != null && !tu.isAfter(den);
    }
    
    public int getTongSoDonHang(LocalDateTime tuNgay, LocalDateTime denNgay) {
        return kiemTraThoiGianHople(tuNgay, denNgay) ? dao.demSoLuongHoaDon(tuNgay, denNgay) : 0;
    }

    public double getTongDoanhThu(LocalDateTime tuNgay, LocalDateTime denNgay) {
        if (!kiemTraThoiGianHople(tuNgay, denNgay)) return 0.0;
        return sumDoanhThuThuan(dao.getRawHDByDateRange(tuNgay, denNgay));
    }

    public double getGiaTriTBTrenDon(LocalDateTime tuNgay, LocalDateTime denNgay) {
        if (!kiemTraThoiGianHople(tuNgay, denNgay)) return 0.0;
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

    public double[] getDoanhThu12Thang(int year, ThongKeFilter filter) {
        if (year <= 0) return new double[12];
        double[] data = new double[12];
        List<Object[]> listHDCuaNam = dao.getRawHD12ThangChuan(year, filter); 
        
        for (Object[] row : listHDCuaNam) {
            int thang = (Integer) row[0];
            String hdId = (String) row[1];
            String ghiChu = (String) row[2];
            
            List<Object[]> lineItems = dao.getRawLineItems(hdId);
            double doanhThuThuanHD = 0;
            for (Object[] item : lineItems) {
                double thanhTien = (double) item[3]; 
                double vatRate   = (double) item[2]; 
                doanhThuThuanHD += Math.round(thanhTien / (1.0 + vatRate / 100.0));
            }
            double tienDiemTru = 0;
            if (ghiChu != null && ghiChu.contains("m: -")) {
                try {
                    String diemStr = ghiChu.substring(ghiChu.lastIndexOf("-") + 1).replaceAll("[^0-9]", "");
                    if (!diemStr.isEmpty()) tienDiemTru = Double.parseDouble(diemStr);
                } catch (Exception ignored) {}
            }
            if (thang >= 1 && thang <= 12) {
                data[thang - 1] += Math.max(0, doanhThuThuanHD - tienDiemTru) / 1_000_000.0;
            }
        }
        List<Object[]> doiTraList = dao.getRawHDDoiTra12Thang(year, filter);
        for (Object[] row : doiTraList) {
            int m = (Integer) row[0];
            double netDelta = (Double) row[1]; // âm = hoàn trả nhiều hơn đổi, dương = đổi mới đắt hơn cũ
            if (m >= 1 && m <= 12) {
                // netDelta < 0: doanh thu giảm (khách trả nhiều hơn lấy) → trừ ABS
                // netDelta > 0: doanh thu tăng (hàng mới đắt hơn cũ) → cộng
                data[m - 1] += netDelta / 1_000_000.0;
            }
        }
        for(int i = 0; i < 12; i++) data[i] = Math.max(0, data[i]); 
        return data;
    }

    public double[] getChiPhi12Thang(int year, ThongKeFilter filter) {
        if (year <= 0) return new double[12];
        return dao.getChiPhi12Thang(year, filter);
    }

    public double[] getLoiNhuan12Thang(int year, ThongKeFilter filter) {
        if (year <= 0) return new double[12];
        double[] dt = getDoanhThu12Thang(year, filter);
        double[] cp = getChiPhi12Thang(year, filter);
        double[] ln = new double[12];
        for (int i = 0; i < 12; i++) ln[i] = Math.max(0, dt[i] - cp[i]);
        return ln;
    }

    public List<Object[]> getThongKe30NgayGanNhat(ThongKeFilter filter) {
        List<Object[]> rawList = dao.getRawHD30Ngay(filter);
        Map<String, double[]> dailyData = new LinkedHashMap<>();
        
        for (Object[] row : rawList) {
            String date = (String) row[0];
            double dt = (row.length >= 5 && row[4] instanceof Double) ? (Double) row[4] : 0.0;
            dailyData.putIfAbsent(date, new double[] { 0, 0 });
            dailyData.get(date)[0] += 1;
            dailyData.get(date)[1] += dt;
        }

        List<Object[]> doiTraList = dao.getRawHDDoiTra30Ngay(filter);
        for (Object[] row : doiTraList) {
            String date = (String) row[0];
            double netDelta = (Double) row[1]; // âm = trả nhiều, dương = đổi lấy đắt hơn
            if (dailyData.containsKey(date)) {
                // cộng netDelta trực tiếp (netDelta âm → trừ DT, dương → tăng DT)
                dailyData.get(date)[1] = Math.max(0, dailyData.get(date)[1] + netDelta);
            }
        }

        List<Object[]> result = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : dailyData.entrySet())
            result.add(new Object[] { entry.getKey(), (int) entry.getValue()[0], entry.getValue()[1] / 1_000_000.0 });
        return result;
    }
    
    private double[] getKetQuaCaByTime(String nvId, int year, ThongKeFilter filter, int startHour, int endHour) {
        if (nvId == null || nvId.isEmpty()) return new double[] { 0, 0 };
        List<Object[]> rawList = dao.getRawHDCaByTime(nvId, year, filter, startHour, endHour);
        Set<String> hdIds = new HashSet<>();
        double totalDT = 0;
        for (Object[] row : rawList) {
            String hdId = (String) row[0];
            hdIds.add(hdId);
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
            if (h >= 0 && h < 24) data[h] += (Double) row[1] / 1_000_000.0;
        }
        List<Object[]> doiTraList = dao.getRawHDDoiTraGio("CAST(hd.ngayLapHD AS DATE)='" + dateYMD + "'", filter);
        for (Object[] row : doiTraList) {
            int h = (Integer) row[2];
            double netDelta_coVAT = (row.length >= 4 && row[3] instanceof Double) ? (Double) row[3] : 0;
            // netDelta_coVAT < 0: doanh thu giờ đó giảm; > 0: tăng
            if (h >= 0 && h < 24) data[h] = Math.max(0, data[h] + netDelta_coVAT / 1_000_000.0);
        }
        return data;
    }

    public List<Object[]> getTopSPTrongNgay(String dateYMD) {
        return getTopSPTrongNgay(dateYMD, null);
    }

    public List<Object[]> getTopSPTrongNgay(String dateYMD, ThongKeFilter filter) {
        if (dateYMD == null || dateYMD.isEmpty()) return new ArrayList<>();
        Map<String, double[]> spMap = new LinkedHashMap<>();
        List<Object[]> hdList = dao.getRawHDForTopSP(dateYMD, filter);
        for (Object[] hd : hdList) {
            String hdId = (String) hd[0];
            List<Object[]> ctList = dao.getRawCTHD(hdId);
            for (Object[] ct : ctList) {
                String ten = (String) ct[0];
                int sl = (Integer) ct[1];
                double dtSP = (ct.length >= 3 && ct[2] instanceof Double) ? (Double) ct[2] : 0.0;
                double[] cur = spMap.getOrDefault(ten, new double[] { 0, 0 });
                cur[0] += sl; cur[1] += dtSP; spMap.put(ten, cur);
            }
        }
        List<Object[]> traList = dao.getTraHangChiTiet(dateYMD, filter);
        for (Object[] tra : traList) {
            String ten = (String) tra[0];
            int sl = (Integer) tra[1];
            double dtSP = (Double) tra[2];
            if (spMap.containsKey(ten)) {
                double[] cur = spMap.get(ten);
                cur[0] -= sl; cur[1] -= dtSP;
                if (cur[0] <= 0) spMap.remove(ten); else spMap.put(ten, cur);
            }
        }
        List<Object[]> result = new ArrayList<>();
        spMap.entrySet().stream().sorted((a, b) -> Double.compare(b.getValue()[1], a.getValue()[1])).limit(10)
                .forEach(e -> result.add(new Object[] { e.getKey(), (int) e.getValue()[0], e.getValue()[1] / 1_000_000.0 }));
        return result;
    }

    public List<Object[]> getTopKhachHang(int year, ThongKeFilter filter) {
        Map<String, double[]> khMap = new LinkedHashMap<>();
        List<Object[]> rawList = dao.getRawKHHD(year, filter);
        for (Object[] row : rawList) {
            String ten = (String) row[0];
            int diem = (Integer) row[1];
            double dtThuan = (row.length >= 5 && row[4] instanceof Double) ? (Double) row[4] / 1_000_000.0 : 0.0;
            double[] cur = khMap.getOrDefault(ten, new double[] { 0, 0, diem });
            cur[0] += 1; cur[1] += dtThuan; cur[2] = diem; khMap.put(ten, cur);
        }
        List<Object[]> result = new ArrayList<>();
        khMap.entrySet().stream().sorted((a, b) -> Double.compare(b.getValue()[1], a.getValue()[1])).limit(10)
                .forEach(e -> result.add(new Object[] { e.getKey(), (int) e.getValue()[0], e.getValue()[1], (int) e.getValue()[2] }));
        return result;
    }

    public List<Object[]> getHoaDonGanDayTrongCa(String maNV, int ca) {
        ThongKeFilter f = new ThongKeFilter(); f.maNV = maNV; f.ca = ca;
        List<Object[]> rawList = dao.getRawHDDashboard(f, 100, false);
        List<Object[]> result = new ArrayList<>();
        for (Object[] row : rawList) {
            String id = (String) row[0]; String kh = (String) row[1]; double coVAT = (Double) row[2];
            String gc = (String) row[3]; String pttt = (String) row[4];
            double thucThu = tinhTienThucTe(coVAT, gc);
            result.add(new Object[] { id, kh, thucThu, pttt });
        }
        return result;
    }

    public List<Object[]> getHoaDonGanDayTrongCa(ThongKeFilter f) {
        List<Object[]> rawList = dao.getRawHDDashboard(f, 100, false);
        List<Object[]> result = new ArrayList<>();
        for (Object[] row : rawList) {
            double thucThu = 0;
            String loai = (String) row[6];
            if ("BAN_HANG".equals(loai)) thucThu = tinhTienThucTe((Double) row[2], (String) row[3]);
            else thucThu = -parseTienHoanTra(loai, (String) row[3]);
            result.add(new Object[] { row[0], row[1], thucThu, row[4], loai });
        }
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
            String s = chenhLech.replaceAll("[^0-9]", "");
            if (!s.isEmpty()) {
                double amount = Double.parseDouble(s);
                if (chenhLech.contains("Hoàn")) {
                    tien = amount;        // Cửa hàng hoàn tiền → dương → caller -tien → âm (tiền ra)
                } else if (chenhLech.contains("Bù")) {
                    tien = -amount;       // Khách bù thêm → âm → caller -tien → dương (tiền vào)
                }
            }
        }
        return tien;
    }
  
    public List<Object[]> getHoaDonGanNhat(ThongKeFilter filter, int limit) {
        List<Object[]> rawList = dao.getRawHDDashboard(filter, limit, true);
        List<Object[]> result = new ArrayList<>();
        for (Object[] row : rawList) {
            String id = (String) row[0]; String kh = (String) row[1]; double coVAT = (Double) row[2];
            String gc = (String) row[3]; String gio = (String) row[5]; String loai = (String) row[6]; 
            double thucThu = 0;
            if ("BAN_HANG".equals(loai)) thucThu = tinhTienThucTe(coVAT, gc); 
            else thucThu = -parseTienHoanTra(loai, gc);
            result.add(new Object[] { id, kh, thucThu, gio, loai });
        }
        return result;
    }

    public List<Object[]> getHoaDonGanNhat(String maNV, int ca, int limit) {
        ThongKeFilter f = new ThongKeFilter(); f.maNV = maNV; f.ca = ca;
        return getHoaDonGanNhat(f, limit);
    }

    public List<Object[]> getHoaDonGiaTriCaoHomNay(int limit) {
        return getHoaDonGiaTriCaoHomNay(limit, null);
    }

    public List<Object[]> getHoaDonGiaTriCaoHomNay(int limit, String maNV) {
        ThongKeFilter f = new ThongKeFilter(); f.maNV = maNV;
        List<Object[]> rawList = dao.getRawHDGiaTriCao(limit, f);
        List<Object[]> result = new ArrayList<>();
        for (Object[] row : rawList) {
            String id = (String) row[0]; String kh = (String) row[1]; double coVAT = (Double) row[2];
            String gc = (String) row[3]; double thucThu = tinhTienThucTe(coVAT, gc);
            String gio = (row.length > 5) ? (String) row[5] : null;
            result.add(new Object[] { id, kh, thucThu, gio });
        }
        return result;
    }

    public double[] getKpiDoiChieu(ThongKeFilter f) {
        int soHD = dao.getSoHoaDonHomNay(f);
        // Lấy đúng danh sách HĐ theo filter: nếu có startTime thì lọc từ giờ bắt đầu ca
        List<Object[]> rows;
        String dateCondForTra;
        if (f != null && f.startTime != null) {
            rows = dao.getRawHDByFilter(f);
            dateCondForTra = "hd.ngayLapHD >= '" + f.startTime.toString().replace("T", " ") + "'";
        } else {
            rows = dao.getRawHDByDay(LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")), f);
            dateCondForTra = "CAST(hd.ngayLapHD AS DATE) = CAST(GETDATE() AS DATE)";
        }

        double tGocChuaVAT = 0, tKhuyenMai = 0, tDoanhThuThuan = 0, tVAT = 0, tThucThuBanHang = 0, tMat = 0, tCK = 0;               

        for (Object[] row : rows) {
            String hdId = (String) row[0];
            String pttt = dao.getPTTT(hdId);
            List<Object[]> lineItems = dao.getRawLineItems(hdId);
            double tienGocHD = 0, doanhThuThuanHD = 0, vatHD = 0;

            for (Object[] item : lineItems) {
                double giaGoc = (double) item[1]; double thueSuat = (double) item[2]; double thanhTien = (double) item[3]; 
                int sl = (int) item[0];
                double thuanDong = Math.round(thanhTien / (1.0 + thueSuat / 100.0));
                double vatDong = Math.round(thanhTien - thuanDong); double gocDong = sl * giaGoc;
                tienGocHD += gocDong; doanhThuThuanHD += thuanDong; vatHD += vatDong;
            }
            double tienDiemTru = 0; String ghiChu = (String) row[2];
            if (ghiChu != null && ghiChu.contains("Dùng điểm: -")) {
                try {
                    String diemStr = ghiChu.substring(ghiChu.indexOf("-") + 1).replaceAll("[^0-9]", "");
                    tienDiemTru = Double.parseDouble(diemStr);
                } catch (Exception ignored) {}
            }
            double thucThuHD = Math.max(0, doanhThuThuanHD + vatHD - tienDiemTru);
            tGocChuaVAT += tienGocHD; tKhuyenMai += (tienGocHD - doanhThuThuanHD); 
            tDoanhThuThuan += doanhThuThuanHD; tVAT += vatHD; tThucThuBanHang += thucThuHD;
            if ("TIEN_MAT".equalsIgnoreCase(pttt)) tMat += thucThuHD; else tCK += thucThuHD;
        }

        double[] traHang = dao.getTienTraHangChinhXac(dateCondForTra, f);
        int soPhieuTra = dao.getTongPhieuDoiTra(f);
        return new double[] { Math.max(0, soHD - soPhieuTra), tGocChuaVAT, tKhuyenMai, tVAT, tThucThuBanHang, tMat, tCK, traHang[0], Math.max(0, tDoanhThuThuan - traHang[1]) };
    }
    
    public double[] getKpiDoiChieu7NgayQua(String maNV) {
        ThongKeFilter f = new ThongKeFilter(); f.maNV = maNV;
        int hd7 = dao.getSoHoaDon7NgayQua(f);
        List<Object[]> rows = dao.getRawHD7NgayQua(f, null);

        double tGocChuaVAT = 0, tKhuyenMai = 0, tDoanhThuThuan = 0, tVAT = 0, tThucThuBanHang = 0;

        for (Object[] row : rows) {
            String hdId = (String) row[0];
            List<Object[]> lineItems = dao.getRawLineItems(hdId);
            double gocHD = 0, doanhThuThuanHD = 0, vatHD = 0;

            for (Object[] item : lineItems) {
                double giaGoc = (double) item[1]; double thueSuat = (double) item[2]; double thanhTien = (double) item[3]; int sl = (int) item[0];
                double thuanDong = Math.round(thanhTien / (1.0 + thueSuat / 100.0));
                double vatDong = Math.round(thanhTien - thuanDong);
                gocHD += sl * giaGoc; doanhThuThuanHD += thuanDong; vatHD += vatDong;
            }
            double tienDiemTru = 0; String ghiChu = (String) row[2];
            if (ghiChu != null && ghiChu.contains("Dùng điểm: -")) {
                try {
                    String diemStr = ghiChu.substring(ghiChu.indexOf("-") + 1).replaceAll("[^0-9]", "");
                    tienDiemTru = Double.parseDouble(diemStr);
                } catch (Exception ignored) {}
            }
            tGocChuaVAT += gocHD; tKhuyenMai += (gocHD - doanhThuThuanHD);
            tDoanhThuThuan += doanhThuThuanHD; tVAT += vatHD;
            tThucThuBanHang += Math.max(0, doanhThuThuanHD + vatHD - tienDiemTru);
        }

        double[] traHang = dao.getTienTraHangChinhXac("hd.ngayLapHD >= DATEADD(DAY, -7, GETDATE())", f);
        int soPhieuTra = dao.getTongPhieuDoiTra(f);
        return new double[] { Math.max(0, hd7 - soPhieuTra), tGocChuaVAT, tKhuyenMai, tVAT, tThucThuBanHang, 0, 0, traHang[0], Math.max(0, tDoanhThuThuan - traHang[1]) };
    }

    public double getTienChuyenKhoan(String maNV, int ca) { 
    	return Math.max(0, getDoanhThuHomNay(maNV, ca) - getDoanhThuTienMatHomNay(maNV, ca)); 
    }
    public double getDieuChinhDoiTraHomNay(String maNV, int ca) {
    		ThongKeFilter f = new ThongKeFilter(); f.maNV = maNV; f.ca = ca; 
    	return tinhDieuChinhDoiTra("CONVERT(DATE,hd.ngayLapHD)=CONVERT(DATE,GETDATE())", f); 
    }
    public double getDieuChinhDoiTra7NgayQua(String maNV) { 
    		ThongKeFilter f = new ThongKeFilter(); f.maNV = maNV; 
    	return tinhDieuChinhDoiTra("hd.ngayLapHD>=DATEADD(DAY,-7,GETDATE())", f); 
    }
    /**
     * Tổng số lượng sản phẩm thực tế hôm nay (đã tính đổi/trả).
     * Công thức: SP bán (BAN_HANG) + SP xuất ra (DOI_HANG soLuong>0)
     *           - SP khách trả (TRA_HANG) - SP lấy lại khi đổi (DOI_HANG soLuong<0)
     */
    public int getTongSoLuongSPHomNay(ThongKeFilter filter) {
        return dao.getTongSoLuongSPHomNay(filter);
    }

    public int getTongSanPham() { 
    	return dao.getTongSanPham(); 
    }
    public int getTongKhachHang() { 
    	return dao.getTongKhachHang(); 
    }
    public int getHoaDonHomNay(String maNV) { 
    		ThongKeFilter f = new ThongKeFilter(); f.maNV = maNV; 
    	return dao.getSoHoaDonHomNay(f); 
    }
    public int getHoaDonHomNay(String maNV, int ca) { 
    		ThongKeFilter f = new ThongKeFilter(); f.maNV = maNV; f.ca = ca; 
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
    		ThongKeFilter f = new ThongKeFilter(); f.maNV = maNV; 
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
    public int[] getSoLuongTheoLoaiSP(int year, ThongKeFilter filter) { 
    	return dao.getSoLuongTheoLoaiSP(year, filter); 
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
        List<Object[]> rawItems = dao.getRawLineItems(hdId);
        double tongDoanhThuThuan = 0, tongTienThueVAT = 0;
        for (Object[] item : rawItems) {
            double thanhTien = (double) item[3]; 
            double thueSuat  = (double) item[2]; 
            double thuanDong = Math.round(thanhTien / (1.0 + thueSuat / 100.0));
            tongDoanhThuThuan += thuanDong;
            tongTienThueVAT   += Math.round(thanhTien - thuanDong);
        }
        return new double[] { tongDoanhThuThuan, tongTienThueVAT, tongDoanhThuThuan + tongTienThueVAT };
    }

    public double[] getKpiTongQuat(ThongKeFilter f) {
        // ---- Phần doanh thu: giữ nguyên logic cũ (bóc VAT + KM + điểm) ----
        List<Object[]> rows = dao.getRawHDByFilter(f);
        double tGocChuaVAT = 0, tKhuyenMai = 0, tDoanhThuThuan = 0,
               tVAT = 0, tThucThuBanHang = 0;
        int soHD = rows.size();
 
        for (Object[] row : rows) {
            String hdId   = (String) row[0];
            String ghiChu = (String) row[2];
 
            List<Object[]> items = dao.getRawLineItems(hdId);
            double thuanHD = 0, vatHD = 0;
 
            for (Object[] it : items) {
                double giaGoc    = (double) it[1];
                double vatRate   = (double) it[2];
                double thanhTien = (double) it[3];
 
                double thuanDong = Math.round(thanhTien / (1.0 + vatRate / 100.0));
                double vatDong   = Math.round(thanhTien - thuanDong);
 
                tDoanhThuThuan += thuanDong;
                tVAT           += vatDong;
                thuanHD        += thuanDong;
                vatHD          += vatDong;
            }
 
            if (ghiChu != null && ghiChu.contains("m: -")) {
                try {
                    String ds = ghiChu.substring(ghiChu.lastIndexOf("-") + 1)
                                      .replaceAll("[^0-9]", "");
                    if (!ds.isEmpty()) {
                        double diem = Double.parseDouble(ds);
                        tDoanhThuThuan = Math.max(0, tDoanhThuThuan - diem);
                    }
                } catch (Exception ignored) {}
            }
            tThucThuBanHang += tinhTienThucTe(thuanHD + vatHD, ghiChu);
        }
 
        // ---- Hàng trả: giữ nguyên logic cũ ----
        double[] dataTraHang       = dao.getTienVaGiaVonHangTra(f);
        double tienHoanCoVAT       = dataTraHang[0];
        double tienHoanChuaVAT     = dataTraHang[1];
        double giaVonHoanLaiKho    = dataTraHang[2];
 
        // ---- [FIX] COGS bán hàng — lấy từ PhanBoLoHang ----
        // Bản cũ dùng `tGiaVon` (= 0 mãi mãi) → sai
        double[] cogsData  = dao.getCogsTheoFilter(f);
        double giaVonBan   = cogsData[0]; // giaVon xuất bán
        // giaVonHoanLaiKho đã được tính bởi getTienVaGiaVonHangTra(), dùng lại.
 
        double doanhThuRong = Math.max(0, tDoanhThuThuan - tienHoanChuaVAT);
        double chiPhiRong   = Math.max(0, giaVonBan - giaVonHoanLaiKho); // NET COGS
        double loiNhuan     = doanhThuRong - chiPhiRong;
 
        return new double[] {
            doanhThuRong,                                            // [0]
            Math.max(0, soHD - dao.getTongPhieuDoiTra(f)),          // [1]
            0,                                                        // [2] reserved
            loiNhuan,                                                // [3] ← đúng bây giờ
            Math.max(0, tVAT - (tienHoanCoVAT - tienHoanChuaVAT)), // [4]
            Math.max(0, tThucThuBanHang - tienHoanCoVAT)           // [5]
        };
    }
    
    public double[] getThongKeTraHangNV(String nvId, int year, ThongKeFilter filter) {
        return dao.getThongKeTraHangNV(nvId, year, filter);
    }
    
    public List<Object[]> getBaoCaoTaiChinh(ThongKeFilter filter, String groupBy) {
        return dao.getBaoCaoTaiChinh(filter, groupBy);
    }
 
    public double[] getKpiTaiChinh(ThongKeFilter filter) {
        List<Object[]> rows = dao.getBaoCaoTaiChinh(filter, "NGAY"); // NGAY cho granularity cao
 
        double sumDTG  = 0, sumVAT = 0, sumTra  = 0;
        double sumDTT  = 0, sumCOGS = 0, sumLN  = 0;
 
        for (Object[] row : rows) {
            sumDTG  += (double) row[1];
            sumVAT  += (double) row[2];
            sumTra  += (double) row[3];
            sumDTT  += (double) row[4];
            sumCOGS += (double) row[5];
            sumLN   += (double) row[6];
        }
 
        double tyLe = (sumDTT > 0) ? sumLN / sumDTT * 100.0 : 0.0;
 
        return new double[]{
            sumDTG,   // [0] Doanh Thu Gộp
            sumVAT,   // [1] Thuế VAT
            sumTra,   // [2] Hàng Bán Bị Trả Lại
            sumDTT,   // [3] Doanh Thu Thuần
            sumCOGS,  // [4] Giá Vốn Hàng Bán
            sumLN,    // [5] Lợi Nhuận Gộp
            tyLe      // [6] Tỷ lệ LN/DTT (%)
        };
    }
    
    public BUS.KetQuaDoiChieuCa layDoiChieuDoanhThuTheoCa(ThongKeFilter filter) {
        return dao.layDoiChieuDoanhThuTheoCa(filter);
    }
    
    public String formatTien(double tien) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,### đ");
        return df.format(tien);
    }
}