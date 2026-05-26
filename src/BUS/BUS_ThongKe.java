package BUS;

import Entity.BoLocThongKe;
import Entity.DoiChieuCa;

import DAO.DAO_ThongKe;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class BUS_ThongKe {


    private final DAO_ThongKe dao;
    private double tGiaVon;

    public BUS_ThongKe() {
        this.dao = new DAO_ThongKe();
    }

    // Tính tiền thực tế khách trả = tongGocCoVAT - tiền giảm từ điểm thưởng và KM_GIAM trong ghiChu
    // ghiChu dạng "... | Dùng điểm: -50000 | ..."
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

    // Alias của tinhTienThucTe, dùng khi muốn gọi theo tên rõ nghĩa hơn
    public double tinhTienThucTeCuaHoaDon(double tongGocCoVAT, String ghiChu) {
        return tinhTienThucTe(tongGocCoVAT, ghiChu);
    }

    // Tính tổng tiền điều chỉnh từ HĐ đổi/trả hoàn thành theo điều kiện ngày và filter
    // TRA_HANG: trừ tiền (đọc từ parts[2] trong ghiChu)
    // DOI_HANG: "Hoàn" → trừ, "Bù" → cộng (đọc từ parts[3])
    // Trả về tổng net (âm = tiệm phải trả ra, dương = khách bù thêm)
    public double tinhDieuChinhDoiTra(String dateCondition, BoLocThongKe filter) {
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

    // Lấy tổng tiền hoàn trả trong ca của 1 NV (parse từ ghiChu)
    public double getTienHoanTraTheoCa(String maNV, LocalDateTime start) {
        return dao.getTienHoanTraTheoCa(maNV, start);
    }

    // Cộng tổng doanh thu thuần (cột [3] = tongGocChuaVAT) từ danh sách raw HĐ
    private double sumDoanhThuThuan(List<Object[]> rawList) {
        double total = 0;
        for (Object[] row : rawList) {
            if (row.length >= 4 && row[3] instanceof Double) total += (Double) row[3];
        }
        return total;
    }

    // Cộng tổng tiền thực thu (có VAT, sau trừ điểm/KM) từ danh sách raw HĐ
    // Công thức: tinhTienThucTe(tongGocCoVAT, ghiChu) cho từng HĐ
    private double sumThucThu(List<Object[]> rawList) {
        double total = 0;
        for (Object[] row : rawList) {
            double coVAT = (Double) row[1];
            String ghiChu = (String) row[2];
            total += tinhTienThucTe(coVAT, ghiChu);
        }
        return total;
    }

    // Doanh thu BAN_HANG tiền mặt trong ca của NV (từ giờ start trở đi)
    public double getTienMatBanHangTheoCa(String maNV, LocalDateTime start) {
        List<Object[]> raw = dao.getRawHDTheoCa(maNV, start, "TIEN_MAT");
        return sumThucThu(raw);
    }

    // Doanh thu BAN_HANG bằng chuyển khoản trong ca = tổng tất cả - tiền mặt
    // Cách này tránh lỗi do enum CHUYEN_KHOAN vs CHUYEN_KHOAN_NGAN_HANG
    public double getTienCKBanHangTheoCa(String maNV, LocalDateTime start) {
        List<Object[]> rawAll = dao.getRawHDTheoCa(maNV, start, null);
        List<Object[]> rawMat = dao.getRawHDTheoCa(maNV, start, "TIEN_MAT");
        return Math.max(0, sumThucThu(rawAll) - sumThucThu(rawMat));
    }

    // Bọc dao.getTienDoiHangTheoCaMat(), dùng cho BUS_CaLamViec tính tienHeThongGhiNhan
    // Trả về double[2]: [0]=tienBuThem (tiền mặt), [1]=tienHoanLai (tiền mặt)
    public double[] getTienDoiHangMatTheoCa(String maNV, LocalDateTime start) {
        return dao.getTienDoiHangTheoCaMat(maNV, start);
    }

    // Tổng tiền đổi hàng trong ca theo TẤT CẢ phương thức (dùng cho tab Đối chiếu doanh thu)
    // Trả về double[2]: [0]=tienBuThem, [1]=tienHoanLai
    public double[] getDoiHangSummaryTheoCa(String maNV, LocalDateTime start) {
        return dao.getTienDoiHangTheoCa(maNV, start);
    }

    // Tổng doanh thu thực tế trong ca = doanh thu BAN_HANG + tiền khách bù đổi - tiền tiệm hoàn đổi
    public double getDoanhThuTheoCa(String maNV, LocalDateTime start) {
        List<Object[]> raw = dao.getRawHDTheoCa(maNV, start, null);
        double doanhThuBanHang = sumThucThu(raw);
        double[] doiHang = dao.getTienDoiHangTheoCa(maNV, start);
        return doanhThuBanHang + doiHang[0] - doiHang[1];
    }

    // Doanh thu tiền mặt thực tế trong ca = tiền mặt BAN_HANG + bù đổi TM - hoàn đổi TM
    public double getDoanhThuTienMatTheoCa(String maNV, LocalDateTime start) {
        List<Object[]> raw = dao.getRawHDTheoCa(maNV, start, "TIEN_MAT");
        double doanhThuBanHang = sumThucThu(raw);
        double[] doiMat = dao.getTienDoiHangTheoCaMat(maNV, start);
        return doanhThuBanHang + doiMat[0] - doiMat[1];
    }

    // Kiểm tra khoảng thời gian hợp lệ: cả 2 không null và tu <= den
    private boolean kiemTraThoiGianHople(LocalDateTime tu, LocalDateTime den) {
        return tu != null && den != null && !tu.isAfter(den);
    }

    // Tổng số HĐ BAN_HANG trong khoảng tuNgay - denNgay (kiểm tra hợp lệ trước)
    public int getTongSoDonHang(LocalDateTime tuNgay, LocalDateTime denNgay) {
        return kiemTraThoiGianHople(tuNgay, denNgay) ? dao.demSoLuongHoaDon(tuNgay, denNgay) : 0;
    }

    // Tổng doanh thu thuần trong khoảng tuNgay - denNgay
    public double getTongDoanhThu(LocalDateTime tuNgay, LocalDateTime denNgay) {
        if (!kiemTraThoiGianHople(tuNgay, denNgay)) return 0.0;
        return sumDoanhThuThuan(dao.getRawHDByDateRange(tuNgay, denNgay));
    }

    // Giá trị trung bình mỗi HĐ = tổng doanh thu / số HĐ
    public double getGiaTriTBTrenDon(LocalDateTime tuNgay, LocalDateTime denNgay) {
        if (!kiemTraThoiGianHople(tuNgay, denNgay)) return 0.0;
        int so = getTongSoDonHang(tuNgay, denNgay);
        return so == 0 ? 0.0 : getTongDoanhThu(tuNgay, denNgay) / so;
    }

    // Tổng lợi nhuận trong khoảng tuNgay - denNgay (doanh thu thuần - COGS)
    public double getTongLoiNhuan(LocalDateTime tuNgay, LocalDateTime denNgay) {
        if (!kiemTraThoiGianHople(tuNgay, denNgay)) return 0.0;
        double doanhThu = sumDoanhThuThuan(dao.getRawHDByDateRange(tuNgay, denNgay));
        double[] cogs   = dao.getRawCogsData(tuNgay, denNgay); // [0]=giaVonBan, [1]=giaVonHoan
        return doanhThu - (cogs[0] - cogs[1]);
    }

    // Danh sách dược sĩ đang làm việc: {id, hoVaTen, chucVu}
    public List<String[]> getDuocSiList() {
        return dao.getDuocSiList();
    }

    // Danh sách nhân viên đang làm việc: {id, hoVaTen}
    public List<String[]> getDanhSachNhanVien() {
        return dao.getDanhSachNhanVien();
    }

    // Tổng số HĐ hôm nay (BAN_HANG + TRA_HANG + DOI_HANG) theo filter
    public int getHoaDonHomNay(BoLocThongKe filter) {
        return dao.getSoTatCaHDHomNay(filter);
    }

    // Doanh thu thực thu (có VAT, trừ điểm/KM) hôm nay theo filter
    public double getDoanhThuHomNay(BoLocThongKe filter) {
        String today = java.time.LocalDate.now().toString();
        List<Object[]> raw = dao.getRawHDByDay(today, filter);
        return sumThucThu(raw);
    }

    // Doanh thu thuần (không VAT, không điểm/KM) hôm nay theo filter
    public double getDoanhThuThuanHomNay(BoLocThongKe filter) {
        String today = java.time.LocalDate.now().toString();
        List<Object[]> raw = dao.getRawHDByDay(today, filter);
        return sumDoanhThuThuan(raw);
    }

    // Doanh thu thuần 7 ngày qua theo filter
    public double getDoanhThuThuan7NgayQua(BoLocThongKe filter) {
        List<Object[]> raw = dao.getRawHD7NgayQua(filter, null);
        return sumDoanhThuThuan(raw);
    }

    // Doanh thu thực thu (có VAT) 7 ngày qua theo filter
    public double getDoanhThu7NgayQua(BoLocThongKe filter) {
        List<Object[]> raw = dao.getRawHD7NgayQua(filter, null);
        return sumThucThu(raw);
    }

    // Doanh thu thực thu hôm nay của 1 NV cụ thể
    public double getDoanhThuHomNay(String maNV) {
        BoLocThongKe f = new BoLocThongKe();
        f.setMaNV(maNV);
        return getDoanhThuHomNay(f);
    }

    // Doanh thu tiền mặt hôm nay của 1 NV cụ thể
    public double getDoanhThuTienMatHomNay(String maNV) {
        BoLocThongKe f = new BoLocThongKe();
        f.setMaNV(maNV);
        List<Object[]> raw = dao.getRawHDHomNay(f, "TIEN_MAT");
        return sumThucThu(raw);
    }

    // Doanh thu thực thu 7 ngày qua của 1 NV cụ thể
    public double getDoanhThu7NgayQua(String maNV) {
        BoLocThongKe f = new BoLocThongKe();
        f.setMaNV(maNV);
        return getDoanhThu7NgayQua(f);
    }

    // Doanh thu thuần 7 ngày qua của 1 NV cụ thể
    public double getDoanhThuThuan7NgayQua(String maNV) {
        BoLocThongKe f = new BoLocThongKe();
        f.setMaNV(maNV);
        return getDoanhThuThuan7NgayQua(f);
    }

    // Doanh thu thực thu hôm nay của 1 NV, lọc theo ca (1=sáng, 2=chiều, 3=tối)
    public double getDoanhThuHomNay(String maNV, int ca) {
        BoLocThongKe f = new BoLocThongKe();
        f.setMaNV(maNV);
        f.setCa(ca);
        return getDoanhThuHomNay(f);
    }

    // Doanh thu tiền mặt hôm nay của 1 NV, lọc theo ca
    public double getDoanhThuTienMatHomNay(String maNV, int ca) {
        BoLocThongKe f = new BoLocThongKe();
        f.setMaNV(maNV);
        f.setCa(ca);
        List<Object[]> raw = dao.getRawHDHomNay(f, "TIEN_MAT");
        return sumThucThu(raw);
    }

    // Doanh thu thuần hôm nay của 1 NV, lọc theo ca
    public double getDoanhThuThuanHomNay(String maNV, int ca) {
        BoLocThongKe f = new BoLocThongKe();
        f.setMaNV(maNV);
        f.setCa(ca);
        return getDoanhThuThuanHomNay(f);
    }

    // Doanh thu thuần từng tháng trong năm year, đơn vị triệu đồng
    // Tính per-HĐ: bóc VAT từng dòng + trừ điểm thưởng → gom theo tháng
    // Cộng thêm netDelta từ HĐ DOI_HANG (tháng đó)
    // Trả về double[12]
    public double[] getDoanhThu12Thang(int year, BoLocThongKe filter) {
        if (year <= 0) return new double[12];
        double[] data = new double[12];
        List<Object[]> listHDCuaNam = dao.getRawHD12ThangChuan(year, filter);

        for (Object[] row : listHDCuaNam) {
            int thang = (Integer) row[0];
            String hdId = (String) row[1];
            String ghiChu = (String) row[2];

            // Bóc VAT từng dòng SP trong HĐ
            List<Object[]> lineItems = dao.getRawLineItems(hdId);
            double doanhThuThuanHD = 0;
            for (Object[] item : lineItems) {
                double thanhTien = (double) item[3];
                double vatRate   = (double) item[2];
                doanhThuThuanHD += Math.round(thanhTien / (1.0 + vatRate / 100.0));
            }
            // Trừ tiền điểm thưởng (đọc từ ghiChu "m: -xxx")
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
        // Cộng/trừ netDelta từ HĐ DOI_HANG từng tháng
        List<Object[]> doiTraList = dao.getRawHDDoiTra12Thang(year, filter);
        for (Object[] row : doiTraList) {
            int m = (Integer) row[0];
            double netDelta = (Double) row[1];
            if (m >= 1 && m <= 12) {
                data[m - 1] += netDelta / 1_000_000.0;
            }
        }
        for (int i = 0; i < 12; i++) data[i] = Math.max(0, data[i]);
        return data;
    }

    // Giá vốn hàng bán (COGS) từng tháng trong năm year, đơn vị triệu
    // Trả về double[12]
    public double[] getChiPhi12Thang(int year, BoLocThongKe filter) {
        if (year <= 0) return new double[12];
        double[] raw = dao.getChiPhi12Thang(year, filter);
        for (int i = 0; i < 12; i++) raw[i] = Math.max(0, raw[i] / 1_000_000.0);
        return raw;
    }

    // Lợi nhuận gộp từng tháng = doanh thu - chi phí (COGS), đơn vị triệu
    // Trả về double[12]
    public double[] getLoiNhuan12Thang(int year, BoLocThongKe filter) {
        if (year <= 0) return new double[12];
        double[] dt = getDoanhThu12Thang(year, filter);
        double[] cp = getChiPhi12Thang(year, filter);
        double[] ln = new double[12];
        for (int i = 0; i < 12; i++) ln[i] = Math.max(0, dt[i] - cp[i]);
        return ln;
    }

    // Thống kê doanh thu thuần 30 ngày gần nhất, nhóm theo ngày
    // Cộng thêm netDelta từ DOI_HANG từng ngày
    // Trả về List<{ngay(dd/MM/yyyy), soHD(int), dtTrieu(double)}>
    public List<Object[]> getThongKe30NgayGanNhat(BoLocThongKe filter) {
        List<Object[]> rawList = dao.getRawHD30Ngay(filter);
        Map<String, double[]> dailyData = new LinkedHashMap<>();

        for (Object[] row : rawList) {
            String date = (String) row[0];
            double dt = (row.length >= 5 && row[4] instanceof Double) ? (Double) row[4] : 0.0;
            dailyData.putIfAbsent(date, new double[] { 0, 0 });
            dailyData.get(date)[0] += 1;
            dailyData.get(date)[1] += dt;
        }

        // Cộng/trừ netDelta từ DOI_HANG 30 ngày
        List<Object[]> doiTraList = dao.getRawHDDoiTra30Ngay(filter);
        for (Object[] row : doiTraList) {
            String date = (String) row[0];
            double netDelta = (Double) row[1];
            if (dailyData.containsKey(date)) {
                dailyData.get(date)[1] = Math.max(0, dailyData.get(date)[1] + netDelta);
            }
        }

        List<Object[]> result = new ArrayList<>();
        for (Map.Entry<String, double[]> entry : dailyData.entrySet())
            result.add(new Object[] { entry.getKey(), (int) entry.getValue()[0], entry.getValue()[1] / 1_000_000.0 });
        return result;
    }

    // Helper nội bộ: lấy kết quả ca theo khoảng giờ (startHour - endHour) của 1 NV trong năm year
    // Trả về double[2]: [0]=soHD (unique), [1]=dtTrieu
    private double[] getKetQuaCaByTime(String nvId, int year, BoLocThongKe filter, int startHour, int endHour) {
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

    // Kết quả ca sáng (6h-13h): double[2] = {soHD, dtTrieu}
    public double[] getKetQuaCaSang(String nvId, int year, BoLocThongKe filter) {
        return getKetQuaCaByTime(nvId, year, filter, 6, 13);
    }

    // Kết quả ca chiều (14h-21h): double[2] = {soHD, dtTrieu}
    public double[] getKetQuaCaChieu(String nvId, int year, BoLocThongKe filter) {
        return getKetQuaCaByTime(nvId, year, filter, 14, 21);
    }

    // Kết quả ca tối (22h-5h): double[2] = {soHD, dtTrieu}
    public double[] getKetQuaCaToi(String nvId, int year, BoLocThongKe filter) {
        return getKetQuaCaByTime(nvId, year, filter, 22, 5);
    }

    // Doanh thu theo từng giờ trong ngày (không lọc NV) - overload không filter
    public double[] getDTTheoGioTrongNgay(String dateYMD) {
        return getDTTheoGioTrongNgay(dateYMD, null);
    }

    // Doanh thu (có VAT) theo từng giờ (0-23) trong ngày dateYMD, đơn vị triệu
    // Cộng thêm netDelta từ DOI_HANG/TRA_HANG cùng ngày vào đúng giờ tương ứng
    // Trả về double[24]
    public double[] getDTTheoGioTrongNgay(String dateYMD, BoLocThongKe filter) {
        if (dateYMD == null || dateYMD.isEmpty()) return new double[24];
        double[] data = new double[24];
        List<Object[]> rawList = dao.getRawHDGioTrongNgay(dateYMD, filter);
        for (Object[] row : rawList) {
            int h = (Integer) row[0];
            if (h >= 0 && h < 24) data[h] += (Double) row[1] / 1_000_000.0;
        }
        // Cộng/trừ netDelta từ TRA/DOI theo từng giờ
        List<Object[]> doiTraList = dao.getRawHDDoiTraGio("CAST(hd.ngayLapHD AS DATE)='" + dateYMD + "'", filter);
        for (Object[] row : doiTraList) {
            int h = (Integer) row[2];
            double netDelta_coVAT = (row.length >= 4 && row[3] instanceof Double) ? (Double) row[3] : 0;
            if (h >= 0 && h < 24) data[h] = Math.max(0, data[h] + netDelta_coVAT / 1_000_000.0);
        }
        return data;
    }

    // Top SP bán chạy nhất hôm nay - overload không filter
    public List<Object[]> getTopSPTrongNgay(String dateYMD) {
        return getTopSPTrongNgay(dateYMD, null);
    }

    // Top 10 SP bán chạy nhất trong ngày dateYMD theo doanh thu thuần, đơn vị triệu
    // Gom từ: HĐ BAN_HANG + SP từ DOI_HANG xuất ra, trừ đi SP khách đã trả
    // Trả về List<{tenSP, soLuong(int), dtTrieu(double)}>
    public List<Object[]> getTopSPTrongNgay(String dateYMD, BoLocThongKe filter) {
        if (dateYMD == null || dateYMD.isEmpty()) return new ArrayList<>();
        Map<String, double[]> spMap = new LinkedHashMap<>();

        // Cộng từ HĐ BAN_HANG
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
        // Trừ đi SP khách đã trả
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
        // Cộng SP từ DOI_HANG xuất ra (hàng đổi mới cho khách)
        List<Object[]> doiList = dao.getSPDoiHangXuatRaTrongNgay(dateYMD, filter);
        for (Object[] doi : doiList) {
            String ten = (String) doi[0];
            int sl = (Integer) doi[1];
            double dtSP = (Double) doi[2];
            double[] cur = spMap.getOrDefault(ten, new double[]{0, 0});
            cur[0] += sl; cur[1] += dtSP; spMap.put(ten, cur);
        }
        List<Object[]> result = new ArrayList<>();
        spMap.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue()[1], a.getValue()[1]))
            .limit(10)
            .forEach(e -> result.add(new Object[] { e.getKey(), (int) e.getValue()[0], e.getValue()[1] / 1_000_000.0 }));
        return result;
    }

    // Top 10 KH theo doanh thu thuần trong năm year, đơn vị triệu
    // Trả về List<{hoVaTen, soHD(int), dtTrieu(double), diemTichLuy(int)}>
    public List<Object[]> getTopKhachHang(int year, BoLocThongKe filter) {
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
        khMap.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue()[1], a.getValue()[1]))
            .limit(10)
            .forEach(e -> result.add(new Object[] { e.getKey(), (int) e.getValue()[0], e.getValue()[1], (int) e.getValue()[2] }));
        return result;
    }

    // Danh sách HĐ gần đây trong ca của NV (theo số ca 1/2/3), tính thucThu từng HĐ
    // Trả về List<{id, tenKH, thucThu, pttt}>
    public List<Object[]> getHoaDonGanDayTrongCa(String maNV, int ca) {
        BoLocThongKe f = new BoLocThongKe(); f.setMaNV(maNV); f.setCa(ca);
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

    // Danh sách HĐ gần đây theo filter, tính thucThu:
    // BAN_HANG → tinhTienThucTe, TRA/DOI → -parseTienHoanTra
    // Trả về List<{id, tenKH, thucThu, pttt, loaiHD}>
    public List<Object[]> getHoaDonGanDayTrongCa(BoLocThongKe f) {
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

    // Overload: lấy HĐ gần đây của NV (không lọc ca)
    public List<Object[]> getHoaDonGanDayTrongCa(String maNV) {
        return getHoaDonGanDayTrongCa(maNV, 0);
    }

    // Helper nội bộ: parse tiền hoàn trả từ ghiChu của HĐ TRA/DOI
    // TRA_HANG: lấy parts[2]; DOI_HANG: "Hoàn" → dương (tiệm trả ra), "Bù" → âm (khách trả thêm)
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
                if (chenhLech.contains("Hoàn")) tien = amount;
                else if (chenhLech.contains("Bù")) tien = -amount;
            }
        }
        return tien;
    }

    // Lấy tối đa limit HĐ gần nhất hôm nay theo filter, kèm giờ HH:mm
    // Tính thucThu cho BAN_HANG, âm cho TRA/DOI
    // Trả về List<{id, tenKH, thucThu, gio, loaiHD}>
    public List<Object[]> getHoaDonGanNhat(BoLocThongKe filter, int limit) {
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

    // Overload: lấy HĐ gần nhất theo maNV và ca
    public List<Object[]> getHoaDonGanNhat(String maNV, int ca, int limit) {
        BoLocThongKe f = new BoLocThongKe(); f.setMaNV(maNV); f.setCa(ca);
        return getHoaDonGanNhat(f, limit);
    }

    // Lấy HĐ giá trị cao nhất hôm nay (tất cả NV) - không lọc NV
    public List<Object[]> getHoaDonGiaTriCaoHomNay(int limit) {
        return getHoaDonGiaTriCaoHomNay(limit, null);
    }

    // Lấy top limit HĐ giá trị cao nhất hôm nay, có thể lọc theo NV
    // Trả về List<{id, tenKH, thucThu, gio}>
    public List<Object[]> getHoaDonGiaTriCaoHomNay(int limit, String maNV) {
        BoLocThongKe f = new BoLocThongKe(); f.setMaNV(maNV);
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

    // KPI đối chiếu doanh thu hôm nay (hoặc theo khoảng ngày từ filter)
    // Tính per-HĐ: bóc giá gốc, KM, VAT, điểm thưởng, phân biệt TM/CK
    // Trả về double[9]:
    //   [0]=soHDBan, [1]=tGocChuaVAT, [2]=tKhuyenMai, [3]=tVAT,
    //   [4]=tThucThuBanHang, [5]=tMat, [6]=tCK,
    //   [7]=tienTraCoVAT, [8]=doanhThuThuanNet (sau trừ hàng trả)
    public double[] getKpiDoiChieu(BoLocThongKe f) {
        int soHD = dao.getSoHoaDonHomNay(f);
        List<Object[]> rows;
        String dateCondForTra;
        String today = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        if (f != null && f.getStartTime() != null) {
            // NV trong ca: hôm nay + từ giờ startTime
            rows = dao.getRawHDByDay(today, f);
            dateCondForTra = "CAST(hd.ngayLapHD AS DATE) = CAST(GETDATE() AS DATE)"
                    + " AND hd.ngayLapHD >= '" + f.getStartTime().toString().replace("T", " ") + "'";
        } else if (f != null && f.getFromDate() != null && f.getToDate() != null) {
            // Admin theo khoảng ngày
            rows = dao.getRawHDByFilter(f);
            dateCondForTra = "CAST(hd.ngayLapHD AS DATE) BETWEEN '" + f.getFromDate() + "' AND '" + f.getToDate() + "'";
        } else {
            // Admin xem hôm nay
            rows = dao.getRawHDByDay(today, f);
            dateCondForTra = "CAST(hd.ngayLapHD AS DATE) = CAST(GETDATE() AS DATE)";
        }

        double tGocChuaVAT = 0, tKhuyenMai = 0, tDoanhThuThuan = 0, tVAT = 0, tThucThuBanHang = 0, tMat = 0, tCK = 0;

        for (Object[] row : rows) {
            String hdId = (String) row[0];
            String pttt = dao.getPTTT(hdId);
            List<Object[]> lineItems = dao.getRawLineItems(hdId);
            double tienGocHD = 0, doanhThuThuanHD = 0, vatHD = 0;

            for (Object[] item : lineItems) {
                // item: [soLuong, giaGoc, thueVAT%, thanhTien]
                double giaGoc = (double) item[1]; double thueSuat = (double) item[2]; double thanhTien = (double) item[3];
                int sl = (int) item[0];
                double thuanDong = Math.round(thanhTien / (1.0 + thueSuat / 100.0));
                double vatDong = Math.round(thanhTien - thuanDong); double gocDong = sl * giaGoc;
                tienGocHD += gocDong; doanhThuThuanHD += thuanDong; vatHD += vatDong;
            }
            // Trừ tiền điểm thưởng từ ghiChu
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

    // KPI đối chiếu 7 ngày qua của 1 NV cụ thể, tương tự getKpiDoiChieu nhưng không chia TM/CK
    // Trả về double[9] (tương tự, [5] và [6] = 0)
    public double[] getKpiDoiChieu7NgayQua(String maNV) {
        BoLocThongKe f = new BoLocThongKe(); f.setMaNV(maNV);
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

    // Tiền chuyển khoản hôm nay của NV trong ca = tổng - tiền mặt
    public double getTienChuyenKhoan(String maNV, int ca) {
        return Math.max(0, getDoanhThuHomNay(maNV, ca) - getDoanhThuTienMatHomNay(maNV, ca));
    }

    // Tổng điều chỉnh đổi/trả hôm nay của NV trong ca (parse ghiChu)
    public double getDieuChinhDoiTraHomNay(String maNV, int ca) {
        BoLocThongKe f = new BoLocThongKe(); f.setMaNV(maNV); f.setCa(ca);
        return tinhDieuChinhDoiTra("CONVERT(DATE,hd.ngayLapHD)=CONVERT(DATE,GETDATE())", f);
    }

    // Tổng điều chỉnh đổi/trả 7 ngày qua của NV (parse ghiChu)
    public double getDieuChinhDoiTra7NgayQua(String maNV) {
        BoLocThongKe f = new BoLocThongKe(); f.setMaNV(maNV);
        return tinhDieuChinhDoiTra("hd.ngayLapHD>=DATEADD(DAY,-7,GETDATE())", f);
    }

    // Tổng số lượng SP thực tế hôm nay (đã tính đổi/trả theo filter)
    public int getTongSoLuongSPHomNay(BoLocThongKe filter) {
        return Math.max(0, dao.getTongSoLuongSPHomNay(filter));
    }

    // Tổng số sản phẩm trong DB
    public int getTongSanPham() { return dao.getTongSanPham(); }

    // Tổng số khách hàng trong DB
    public int getTongKhachHang() { return dao.getTongKhachHang(); }

    // Số HĐ BAN_HANG hôm nay của 1 NV
    public int getHoaDonHomNay(String maNV) {
        BoLocThongKe f = new BoLocThongKe(); f.setMaNV(maNV);
        return dao.getSoHoaDonHomNay(f);
    }

    // Số HĐ BAN_HANG hôm nay của 1 NV, lọc theo ca
    public int getHoaDonHomNay(String maNV, int ca) {
        BoLocThongKe f = new BoLocThongKe(); f.setMaNV(maNV); f.setCa(ca);
        return dao.getSoHoaDonHomNay(f);
    }

    // Top 4 SP sắp hết hàng (tồn ít nhất)
    public List<Object[]> getTop4SanPhamSapHetHang() { return dao.getTop4SanPhamSapHetHang(); }

    // Số SP còn hàng tồn kho
    public int getSoSanPhamDuTon() { return dao.getSoSanPhamDuTon(); }

    // Số lô hàng sắp hết hạn trong vòng days ngày
    public int getSoLoHangSapHetHanKhoang(int days) { return dao.getSoLoHangSapHetHanKhoang(days); }

    // Danh sách lô hàng sắp hết hạn trong vòng days ngày
    public List<Object[]> getLoHangSapHetHanNhanh(int days) { return dao.getLoHangSapHetHanNhanh(days); }

    // Số HĐ BAN_HANG 7 ngày qua của 1 NV
    public int getSoHoaDon7NgayQua(String maNV) {
        BoLocThongKe f = new BoLocThongKe(); f.setMaNV(maNV);
        return dao.getSoHoaDon7NgayQua(f);
    }

    // Tổng phiếu TRA/DOI đã hoàn thành theo filter
    public int getTongPhieuDoiTra(BoLocThongKe filter) { return dao.getTongPhieuDoiTra(filter); }

    // Số phiếu TRA/DOI chưa xử lý theo filter
    public int getPhieuDoiTraChoXuLy(BoLocThongKe filter) { return dao.getPhieuDoiTraChoXuLy(filter); }

    // Số HĐ BAN_HANG của NV từ thời điểm start (dùng cho kết ca)
    public int getSoHoaDonTheoCa(String maNV, LocalDateTime s) { return dao.getSoHoaDonTheoCa(maNV, s); }

    // Danh sách dược sĩ đang làm việc (đầy đủ {id, hoVaTen, chucVu})
    public List<String[]> getDuocSiListFull() { return dao.getDuocSiList(); }

    // Tổng số HĐ BAN_HANG trong năm year theo filter
    public long getTongHoaDon(int year, BoLocThongKe filter) { return dao.getTongHoaDon(year, filter); }

    // Tỉ lệ % số lượng bán theo loại SP: int[4] = {THUOC_KE_DON, THUOC_KHONG_KE_DON, THUC_PHAM, MY_PHAM}
    public int[] getSoLuongTheoLoaiSP(int year, BoLocThongKe filter) {
        int[] rawCounts = dao.getSoLuongTheoLoaiSP(year, filter);
        int total = 0;
        for (int v : rawCounts) total += Math.max(1, v);
        int[] result = new int[4];
        for (int i = 0; i < 4; i++)
            result[i] = Math.max(1, (int) Math.round(Math.max(1, rawCounts[i]) * 100.0 / total));
        return result;
    }

    // 10 ngày gần nhất có HĐ BAN_HANG trong năm year (dd/MM/yyyy, tăng dần)
    public List<String> get10NgayGanNhat(int year, BoLocThongKe c) { return dao.get10NgayGanNhat(year, c); }

    // Số HĐ BAN_HANG của NV trong ngày d theo filter
    public int getDailyHDCuaNV(String id, String d, BoLocThongKe c) { return dao.getDailyHDCuaNV(id, d, c); }

    // Top 10 SP bán chạy nhất theo doanh thu thuần trong năm year
    public List<Object[]> getTopSanPham(int year, BoLocThongKe c) {
        List<Object[]> raw = dao.getTopSanPham(year, c);
        for (Object[] row : raw) row[3] = (double) row[3] / 1_000_000.0;
        return raw;
    }

    // Top 20 SP có tiền VAT cao nhất trong năm year
    public List<Object[]> getVATReport(int year, BoLocThongKe c) {
        List<Object[]> raw = dao.getVATReport(year, c);
        for (Object[] row : raw) row[4] = (double) row[4] / 1_000_000.0;
        return raw;
    }

    // Danh sách SP sắp hết hạn trong 6 tháng tới
    public List<Object[]> getSpSapHetHan() { return dao.getSpSapHetHan(); }

    // Thống kê tổng hợp 1 ngày: {soHD, dtTrieu, soKH, soSP}
    public Object[] getThongKeNgayCuThe(String d) {
        Object[] raw = dao.getThongKeNgayCuThe(d);
        raw[1] = (double) raw[1] / 1_000_000.0;
        return raw;
    }

    // Danh sách NV bán hàng trong ngày d: {hoVaTen, soHD, dtTrieu}
    public List<Object[]> getNVTrongNgay(String d) {
        List<Object[]> raw = dao.getNVTrongNgay(d);
        for (Object[] row : raw) row[2] = (double) row[2] / 1_000_000.0;
        return raw;
    }

    // KH mới từng tháng trong năm year: int[12]
    public int[] getKHMoiTheoThang(int year) { return dao.getKHMoiTheoThang(year); }

    // KPI tổng hợp KH: {tongKH, khCoTK, tongDiem}
    public Object[] getKpiKhachHang() { return dao.getKpiKhachHang(); }

    // Top limit KH theo điểm tích lũy: {hoVaTen, sdt, soHD, dtTrieu, diem}
    public List<Object[]> getTopKhachHangTheoDiem(int limit) {
        List<Object[]> raw = dao.getTopKhachHangTheoDiem(limit);
        for (Object[] row : raw) row[3] = (double) row[3] / 1_000_000.0;
        return raw;
    }

    // Tổng giá trị tồn kho (lô CON_HANG)
    public double getTongGiaTriTonKho() { return dao.getTongGiaTriTonKho(); }

    // Số lô theo trạng thái: {conHang, hetHang, hetHan}
    public Object[] getSoLoTheoTrangThai() { return dao.getSoLoTheoTrangThai(); }

    // Tồn kho theo từng kho: {maKho, soLuong, giaTriTrieu}
    public List<Object[]> getTonKhoTheoKho() { return dao.getTonKhoTheoKho(); }

    // Top 10 SP tồn nhiều nhất: {tenSP, soLuong, giaTriTrieu}
    public List<Object[]> getTopSPTonNhieu() { return dao.getTopSPTonNhieu(); }

    // Giá trị nhập hàng từng tháng trong năm year: double[12]
    public double[] getNhapHang12Thang(int year) { return dao.getNhapHang12Thang(year); }

    // Gợi ý KM thông minh dựa trên biên LN + tốc độ bán + hàng sắp hết hạn
    // NGUỒN 1: Phân tích biên LN + tốc độ bán:
    //   - Biên >40% + chậm → "Mua 2 tặng 1"
    //   - Biên >40% + nhanh → "Giảm 10%"
    //   - Biên 20-40% + chậm → "Mua 2 tặng 1"
    //   - Biên 20-40% + nhanh → "Giảm 15%"
    //   - Biên <20% → "Giảm 5%"
    //   Enrich thêm: đang có KM, xu hướng quý, mùa vụ cùng kỳ năm ngoái
    // NGUỒN 2: Hàng sắp hết hạn ≤90 ngày → "Mua [SP bán chạy] tặng [SP sắp HH]"
    // Trả về List<Object[11]>: {tenSP, danhMuc, sl, dtTrieu, giaVon, bienLN, coKM, loaiKM, lyDo, mucGiam, isHetHan}
    public List<Object[]> getGoiYKhuyenMai(int year) {
        List<Object[]> raw = dao.getRawTopSanPhamBanChay(year); // {tenSP, danhMuc, slBan, doanhThu, giaVon}
        List<Object[]> result = new ArrayList<>();

        // Tính slTrungBinh để phân biệt bán chạy / chậm
        double tongSl = 0;
        int countCoGiaVon = 0;
        for (Object[] r : raw) {
            int    slBan_   = (int)    r[2];
            double doanhThu_= (double) r[3];
            double giaVon_  = (double) r[4];
            double giaBanTB_= slBan_ > 0 ? doanhThu_ / slBan_ : 0;
            double bienLN_  = giaBanTB_ > 0 ? (giaBanTB_ - giaVon_) / giaBanTB_ * 100 : 0;
            if (bienLN_ > 0) { tongSl += slBan_; countCoGiaVon++; }
        }
        double slTrungBinh = countCoGiaVon > 0 ? tongSl / countCoGiaVon : 1;

        // Tìm SP bán chạy nhất để dùng trong gợi ý KM hàng sắp HH
        String spBanChayNhat = "";
        int maxSl = 0;
        for (Object[] r : raw) {
            int    sl_     = (int)    r[2];
            double doanhThu_ = (double) r[3];
            double giaVon_   = (double) r[4];
            double giaBanTB_ = sl_ > 0 ? doanhThu_ / sl_ : 0;
            double bienLN_   = giaBanTB_ > 0 ? (giaBanTB_ - giaVon_) / giaBanTB_ * 100 : 0;
            if (bienLN_ > 0 && sl_ > maxSl) { maxSl = sl_; spBanChayNhat = String.valueOf(r[0]); }
        }

        int thangHienTai = java.time.LocalDate.now().getMonthValue();

        for (Object[] r : raw) {
            String tenSP   = String.valueOf(r[0]);
            int    slBan   = (int)    r[2];
            double doanhThu = (double) r[3];
            double giaVon  = (double) r[4];
            double giaBanTB = slBan > 0 ? doanhThu / slBan : 0;
            double bienLN   = giaBanTB > 0 ? (giaBanTB - giaVon) / giaBanTB * 100 : 0;
            if (bienLN <= 0) continue; // Bỏ qua SP không có giá vốn
            double dtTrieu  = doanhThu / 1_000_000.0;
            boolean banChay = slBan >= slTrungBinh;

            String loaiKMGoiY, lyDoGoiY, mucGiamGoiY;

            // Xác định loại KM dựa trên biên LN và tốc độ bán
            if (bienLN > 40) {
                if (!banChay) { loaiKMGoiY = "Mua 2 tặng 1"; lyDoGoiY = "Biên LN cao, cần kích cầu"; mucGiamGoiY = "0"; }
                else { loaiKMGoiY = "Giảm 10% giá bán"; lyDoGoiY = "Biên LN cao, sản phẩm bán chạy"; mucGiamGoiY = "10"; }
            } else if (bienLN >= 20) {
                if (!banChay) { loaiKMGoiY = "Mua 2 tặng 1"; lyDoGoiY = "Bán chậm, cần kích cầu"; mucGiamGoiY = "0"; }
                else { loaiKMGoiY = "Giảm 15% giá bán"; lyDoGoiY = "Biên LN trung bình, bán chạy"; mucGiamGoiY = "15"; }
            } else {
                loaiKMGoiY = "Giảm 5% giá bán"; lyDoGoiY = "Biên LN thấp, KM nhẹ để giữ giá"; mucGiamGoiY = "5";
            }

            // Enrich 1: SP đang có KM chạy thì ghi chú
            boolean coKM = dao.coKMDangChay(tenSP);
            if (coKM) { loaiKMGoiY = "Đang có KM"; mucGiamGoiY = "-"; lyDoGoiY += " | ⚠ SP đang trong chương trình KM"; }

            // Enrich 2: Xu hướng quý (so quý này vs quý trước)
            int[] xuHuong = dao.getXuHuongBanSP(tenSP, year);
            int slQuyNay = xuHuong[0], slQuyCu = xuHuong[1];
            if (slQuyCu > 0 && slQuyNay < slQuyCu) {
                int pctGiam = (int) Math.round((slQuyCu - slQuyNay) * 100.0 / slQuyCu);
                lyDoGoiY += " | ↓ Q này giảm " + pctGiam + "% so Q trước → cần KM kích cầu";
            } else if (slQuyCu > 0 && slQuyNay > slQuyCu && !banChay) {
                lyDoGoiY += " | ↑ Đang tăng trưởng Q này";
            }

            // Enrich 3: Mùa vụ - so cùng tháng năm ngoái
            int slCungKy = dao.getSlBanCungKy(tenSP, thangHienTai, year);
            if (!banChay && slCungKy == 0) lyDoGoiY += " | Cùng kỳ năm trước cũng chậm — xem xét mùa vụ";
            else if (!banChay && slCungKy > 0 && slBan < slCungKy / 2)
                lyDoGoiY += " | Thấp hơn cùng kỳ năm trước (" + slCungKy + " sp)";

            result.add(new Object[]{ tenSP, r[1], slBan, dtTrieu, giaVon, bienLN, coKM, loaiKMGoiY, lyDoGoiY, mucGiamGoiY, false });
        }

        // NGUỒN 2: Hàng sắp hết hạn ≤90 ngày → gợi ý KM tặng kèm
        try {
            List<Object[]> dsHetHan = dao.getSpSapHetHan();
            java.time.LocalDate homNay = java.time.LocalDate.now();
            java.util.Set<String> tenDaCoGoiY = new java.util.HashSet<>();
            for (Object[] r : result) tenDaCoGoiY.add(String.valueOf(r[0]).toLowerCase());

            for (Object[] hh : dsHetHan) {
                String tenSP     = String.valueOf(hh[1]);
                String ngayHHStr = String.valueOf(hh[4]);
                int slTon = 0;
                try { slTon = Integer.parseInt(String.valueOf(hh[3]).replaceAll("[^0-9]", "")); } catch (Exception ignored) {}

                if (slTon <= 0 || tenDaCoGoiY.contains(tenSP.toLowerCase())) continue;

                // Parse ngày hết hạn
                java.time.LocalDate ngayHH = null;
                for (String fmt : new String[]{"yyyy-MM-dd", "dd/MM/yyyy", "yyyy-MM-dd HH:mm:ss"}) {
                    try {
                        ngayHH = java.time.LocalDate.parse(
                            ngayHHStr.length() > 10 ? ngayHHStr.substring(0, 10) : ngayHHStr,
                            java.time.format.DateTimeFormatter.ofPattern(fmt.length() > 10 ? "yyyy-MM-dd" : fmt));
                        break;
                    } catch (Exception ignored) {}
                }
                if (ngayHH == null) continue;

                long soNgayConLai = java.time.temporal.ChronoUnit.DAYS.between(homNay, ngayHH);
                if (soNgayConLai < 0 || soNgayConLai > 90) continue;

                String spMua = spBanChayNhat.isEmpty() ? "bất kỳ sản phẩm" : spBanChayNhat;
                result.add(new Object[]{
                    tenSP, String.valueOf(hh[2]), slTon, 0.0, 0.0, 0.0, false,
                    "Mua " + spMua + " tặng " + tenSP,
                    "[Sắp HH] Còn " + soNgayConLai + " ngày - Tồn " + slTon + " SP",
                    "0", true // isHetHan = true → GUI highlight đỏ
                });
                tenDaCoGoiY.add(tenSP.toLowerCase());
            }
        } catch (Exception e) { e.printStackTrace(); }

        return result;
    }

    // Top limit KH VIP (diem >= 500) mua hàng hôm nay: {hoVaTen, diemTichLuy, soSP}
    public List<Object[]> getKhachHangVIPMuaHomNay(int limit) { return dao.getKhachHangVIPMuaHomNay(limit); }

    // Mảng mệnh giá tiền mặt chuẩn (dùng để đếm quỹ tiền khi bắt đầu ca)
    public static final long[] MENH_GIA = { 500000, 200000, 100000, 50000, 20000, 10000, 5000, 2000, 1000 };

    // Tính tổng tiền đầu ca từ mảng số lượng tờ tiền (tương ứng với MENH_GIA)
    public long tinhTongTienDauCa(int[] soLuong) {
        long t = 0;
        for (int i = 0; i < Math.min(soLuong.length, MENH_GIA.length); i++) {
            t += soLuong[i] * MENH_GIA[i];
        }
        return t;
    }

    // Tính doanh thu thuần và VAT của 1 HĐ từ các dòng line item
    // Trả về double[3]: [0]=tongDoanhThuThuan, [1]=tongVAT, [2]=tongCoVAT
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

    // KPI tổng quát theo filter (Tháng/Quý/Tùy chỉnh): tính doanh thu, VAT, lợi nhuận, hàng trả
    // Dùng COGS từ PhanBoLoHang (chính xác), không dùng biến tGiaVon cũ (luôn = 0)
    // Trả về double[8]:
    //   [0]=doanhThuRong, [1]=tongHD, [2]=reserved,
    //   [3]=loiNhuan, [4]=vatRong, [5]=thucThuRong,
    //   [6]=soHDBan, [7]=soHDTra
    public double[] getKpiTongQuat(BoLocThongKe f) {
        List<Object[]> rows = dao.getRawHDByFilter(f);
        double tGocChuaVAT = 0, tKhuyenMai = 0, tDoanhThuThuan = 0, tVAT = 0, tThucThuBanHang = 0;
        int soHD = rows.size();

        for (Object[] row : rows) {
            String hdId   = (String) row[0];
            String ghiChu = (String) row[2];

            List<Object[]> items = dao.getRawLineItems(hdId);
            double thuanHD = 0, vatHD = 0;

            for (Object[] it : items) {
                double vatRate   = (double) it[2];
                double thanhTien = (double) it[3];
                double thuanDong = Math.round(thanhTien / (1.0 + vatRate / 100.0));
                double vatDong   = Math.round(thanhTien - thuanDong);
                tDoanhThuThuan += thuanDong; tVAT += vatDong;
                thuanHD += thuanDong; vatHD += vatDong;
            }

            // Trừ điểm thưởng từ ghiChu "m: -xxx"
            if (ghiChu != null && ghiChu.contains("m: -")) {
                try {
                    String ds = ghiChu.substring(ghiChu.lastIndexOf("-") + 1).replaceAll("[^0-9]", "");
                    if (!ds.isEmpty()) tDoanhThuThuan = Math.max(0, tDoanhThuThuan - Double.parseDouble(ds));
                } catch (Exception ignored) {}
            }
            tThucThuBanHang += tinhTienThucTe(thuanHD + vatHD, ghiChu);
        }

        double[] dataTraHang    = dao.getTienVaGiaVonHangTra(f);
        double tienHoanCoVAT    = dataTraHang[0];
        double tienHoanChuaVAT  = dataTraHang[1];
        double giaVonHoanLaiKho = dataTraHang[2];

        double[] cogsData = dao.getCogsTheoFilter(f);
        double giaVonBan  = cogsData[0];

        double doanhThuRong = Math.max(0, tDoanhThuThuan - tienHoanChuaVAT);
        double chiPhiRong   = Math.max(0, giaVonBan - giaVonHoanLaiKho);
        double loiNhuan     = doanhThuRong - chiPhiRong;

        int traCount = dao.getTongPhieuDoiTra(f);
        return new double[] {
            doanhThuRong,                                            // [0]
            soHD + traCount,                                         // [1] tổng HĐ
            0,                                                        // [2] reserved
            loiNhuan,                                                // [3]
            Math.max(0, tVAT - (tienHoanCoVAT - tienHoanChuaVAT)), // [4]
            Math.max(0, tThucThuBanHang - tienHoanCoVAT),           // [5]
            soHD,                                                    // [6] đơn BAN_HANG
            traCount                                                 // [7] đơn TRA_HANG
        };
    }

    // Số HĐ TRA/DOI hoàn thành và tổng tiền hoàn của NV trong năm year
    // Trả về double[2]: [0]=soHD, [1]=tienHoan
    public double[] getThongKeTraHangNV(String nvId, int year, BoLocThongKe filter) {
        return dao.getThongKeTraHangNV(nvId, year, filter);
    }

    // Báo cáo tài chính nhóm theo ngày hoặc tháng (groupBy = "THANG" hoặc "NGAY")
    // BUS tính thêm: doanhThuThuan = dtGop - hangTra - VAT, giaVonThucTe, loiNhuanGop
    // Trả về List<Object[7]>:
    //   [0]=thoiGian, [1]=dtGop, [2]=VAT, [3]=hangTra,
    //   [4]=dtThuan, [5]=giaVonThucTe, [6]=loiNhuanGop
    public List<Object[]> getBaoCaoTaiChinh(BoLocThongKe filter, String groupBy) {
        List<Object[]> rawData = dao.getBaoCaoTaiChinh(filter, groupBy);
        List<Object[]> processedData = new ArrayList<>();

        for (Object[] raw : rawData) {
            String thoiGian      = (String) raw[0];
            double doanhThuGop   = (double) raw[1];
            double thueVAT       = (double) raw[2];
            double hangBanBiTraLai = (double) raw[3];
            double giaVonBan     = (double) raw[4];
            double giaVonHoan    = (double) raw[5];

            double doanhThuThuan = doanhThuGop - hangBanBiTraLai - thueVAT;
            double giaVonThucTe  = giaVonBan - giaVonHoan;
            double loiNhuanGop   = doanhThuThuan - giaVonThucTe;
            processedData.add(new Object[]{ thoiGian, doanhThuGop, thueVAT, hangBanBiTraLai, doanhThuThuan, giaVonThucTe, loiNhuanGop });
        }
        return processedData;
    }

    // KPI tài chính tổng hợp từ báo cáo tài chính (nhóm theo ngày)
    // Trả về double[7]:
    //   [0]=sumDTGop, [1]=sumVAT, [2]=sumHangTra,
    //   [3]=sumDTThuan, [4]=sumCOGS, [5]=sumLoiNhuan, [6]=tyLeLoiNhuan%
    public double[] getKpiTaiChinh(BoLocThongKe filter) {
        List<Object[]> rows = this.getBaoCaoTaiChinh(filter, "NGAY");

        double sumDTG = 0, sumVAT = 0, sumTra = 0;
        double sumDTT = 0, sumCOGS = 0, sumLN = 0;

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
            sumDTG,  // [0] Doanh Thu Gộp
            sumVAT,  // [1] Thuế VAT
            sumTra,  // [2] Hàng Bán Bị Trả Lại
            sumDTT,  // [3] Doanh Thu Thuần
            sumCOGS, // [4] Giá Vốn Hàng Bán
            sumLN,   // [5] Lợi Nhuận Gộp
            tyLe     // [6] Tỷ Lệ Lợi Nhuận %
        };
    }

    // Lấy dữ liệu đối chiếu doanh thu theo ca, map sang BUS result object có getter tính toán
    public BUS_KetQuaDoiChieuCa layDoiChieuDoanhThuTheoCa(BoLocThongKe filter) {
        DoiChieuCa raw = dao.layDoiChieuDoanhThuTheoCa(filter);
        if (raw == null) raw = new DoiChieuCa();
        return new BUS_KetQuaDoiChieuCa(raw);
    }

    // Thống kê BAN_HANG từng ngày trong tuần bắt đầu weekStartYMD: {ngay, soHD, dtTrieu}
    public List<Object[]> getThongKeTuan(String weekStartYMD) {
        List<Object[]> raw = dao.getThongKeTuan(weekStartYMD);
        for (Object[] row : raw) row[2] = (double) row[2] / 1_000_000.0;
        return raw;
    }

    // Format số tiền thành dạng "#,### đ"
    public String formatTien(double tien) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,### đ");
        return df.format(tien);
    }
}