package DAO;

import ConnectDB.ConnectDB;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import BUS.BUS_ThongKe.ThongKeFilter;

public class DAO_ThongKe {

    public DAO_ThongKe() {
    }

    private Connection getConn() {
        return ConnectDB.getInstance().getConnection();
    }

    public void applyFilter(BUS.BUS_ThongKe.ThongKeFilter filter, StringBuilder sql, List<Object> params,
            String dateCol, String nvCol) {
        if (filter == null)
            return;
        if (filter.maNV != null && !filter.maNV.isEmpty()) {
            sql.append(" AND ").append(nvCol).append(" = ?");
            params.add(filter.maNV);
        }

        if (filter.startTime != null) {
            sql.append(" AND ").append(dateCol).append(" >= ?");
            params.add(Timestamp.valueOf(filter.startTime));
        } else if (filter.ca != null && filter.ca > 0) {
            if (filter.ca == 1)
                sql.append(" AND DATEPART(HOUR, ").append(dateCol).append(") BETWEEN 6 AND 13");
            else if (filter.ca == 2)
                sql.append(" AND DATEPART(HOUR, ").append(dateCol).append(") BETWEEN 14 AND 21");
            else if (filter.ca == 3)
                sql.append(" AND (DATEPART(HOUR, ").append(dateCol).append(") >= 22 OR DATEPART(HOUR, ").append(dateCol)
                        .append(") < 6)");
        }
        if ("THANG".equals(filter.modeLocThoiGian) && filter.month != null) {
            sql.append(" AND MONTH(").append(dateCol).append(") = ?");
            params.add(filter.month);
        } else if ("QUY".equals(filter.modeLocThoiGian) && filter.quarter != null) {
            if (filter.quarter == 1)
                sql.append(" AND MONTH(").append(dateCol).append(") BETWEEN 1 AND 3");
            else if (filter.quarter == 2)
                sql.append(" AND MONTH(").append(dateCol).append(") BETWEEN 4 AND 6");
            else if (filter.quarter == 3)
                sql.append(" AND MONTH(").append(dateCol).append(") BETWEEN 7 AND 9");
            else if (filter.quarter == 4)
                sql.append(" AND MONTH(").append(dateCol).append(") BETWEEN 10 AND 12");
        } else if ("TUYCHINH".equals(filter.modeLocThoiGian) && filter.fromDate != null && filter.toDate != null) {
            sql.append(" AND CAST(").append(dateCol).append(" AS DATE) BETWEEN ? AND ?");
            params.add(filter.fromDate);
            params.add(filter.toDate);
        }
    }

    // =====================================================================
    // LEGACY METHODS — Vẫn giữ vì BUS_ThongKe vẫn dùng trực tiếp
    // =====================================================================

    public int demSoLuongHoaDon(LocalDateTime tuNgay, LocalDateTime denNgay) {
        int soLuong = 0;
        String sql = "SELECT COUNT(*) as TongSo FROM HoaDon WHERE ngayLapHD BETWEEN ? AND ? AND loaiHD = 'BAN_HANG'";
        try (PreparedStatement pst = getConn().prepareStatement(sql)) {
            pst.setTimestamp(1, Timestamp.valueOf(tuNgay));
            pst.setTimestamp(2, Timestamp.valueOf(denNgay));
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    soLuong = rs.getInt("TongSo");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return soLuong;
    }

    /**
     * Doanh thu thuần cho khoảng thời gian — dùng bởi legacy BUS methods.
     *
     * Công thức đúng: Doanh thu = (thực thu sau KM) / 1.1
     * - Bước 1: Query từng HĐ lấy tongGocCoVAT (giá gốc có VAT) và ghiChu.
     * - Bước 2: Với mỗi HĐ, parse ghiChu để tính tiền giảm KM (giống
     * BUS.tinhTienThucTe).
     * - Bước 3: thucTe = max(0, tongGocCoVAT - tongTienGiam) → doanhThu += thucTe /
     * 1.1
     *
     * Trước đây sai vì: SUM(thanhTien) / 1.1 — bỏ qua KM ghi trong ghiChu.
     */
    public double tinhDoanhThu(LocalDateTime tuNgay, LocalDateTime denNgay) {
        double doanhThu = 0;
        // Lấy từng HĐ: tongGocCoVAT và ghiChu để BUS-level logic có thể tính KM
        String sql = "SELECT SUM(ct.soLuong * dvl.gia * (1 + ISNULL(sp.thueVAT, 0)/100.0)) AS tongGocCoVAT, hd.ghiChu "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
                + "JOIN DonViDoLuong dvl ON ct.donViDoLuongId = dvl.id AND ct.sanPhamId = dvl.sanPhamId "
                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
                + "WHERE hd.ngayLapHD BETWEEN ? AND ? AND hd.loaiHD = 'BAN_HANG' "
                + "GROUP BY hd.id, hd.ghiChu";
        try (Connection con = getConn();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setTimestamp(1, Timestamp.valueOf(tuNgay));
            pst.setTimestamp(2, Timestamp.valueOf(denNgay));
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    double tongGocCoVAT = rs.getDouble("tongGocCoVAT");
                    String ghiChu = rs.getString("ghiChu");
                    // Tính tiền giảm KM từ ghiChu (mirror BUS.tinhTienThucTe)
                    double tongTienGiam = tinhTongTienGiamInline(con, tongGocCoVAT, ghiChu);
                    double thucTe = Math.max(0, tongGocCoVAT - tongTienGiam);
                    doanhThu += thucTe / 1.1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doanhThu;
    }

    /**
     * Tính tổng tiền giảm từ chuỗi ghiChu của 1 hóa đơn.
     *
     * Đây là bản sao logic của BUS_ThongKe.tinhTienThucTe() dùng riêng cho
     * tinhDoanhThu() (legacy method trong DAO, không thể gọi sang BUS để tránh
     * circular dependency). Hai phương thức PHẢI được giữ đồng bộ với nhau.
     *
     * @param con          Connection đang dùng (tránh mở connection mới)
     * @param tongGocCoVAT Tổng giá gốc có VAT (để tính % KM)
     * @param ghiChu       Chuỗi ghi chú hóa đơn (phân cách bởi |)
     * @return Tổng tiền giảm (>= 0)
     */
    private double tinhTongTienGiamInline(Connection con, double tongGocCoVAT, String ghiChu) {
        double tongTienGiam = 0;
        if (ghiChu == null || ghiChu.isEmpty())
            return 0;
        String[] parts = ghiChu.split("\\|");
        for (String p : parts) {
            p = p.trim();
            if (p.startsWith("Dùng điểm: -") || p.contains("KM_GIAM:")) {
                try {
                    tongTienGiam += Long.parseLong(p.replaceAll("[^0-9]", ""));
                } catch (Exception ignored) {
                }
            } else if (p.startsWith("KM:")) {
                String[] mks = p.substring(3).trim().split(",");
                for (String mk : mks) {
                    String sqlKM = "SELECT loaiHinhThuc, giaTri FROM HinhThucKhuyenMai WHERE khuyenMaiId = ?";
                    try (PreparedStatement ps = con.prepareStatement(sqlKM)) {
                        ps.setString(1, mk.trim());
                        try (ResultSet rsKM = ps.executeQuery()) {
                            if (rsKM.next()) {
                                String loai = rsKM.getString("loaiHinhThuc");
                                double val = rsKM.getDouble("giaTri");
                                if (loai.contains("PHAN_TRAM") || loai.contains("%"))
                                    tongTienGiam += tongGocCoVAT * (val / 100.0);
                                else if (loai.contains("TIEN_MAT"))
                                    tongTienGiam += val;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return tongTienGiam;
    }

    public double tinhLoiNhuan(LocalDateTime tuNgay, LocalDateTime denNgay) {
        double dt = tinhDoanhThu(tuNgay, denNgay);
        double cp = 0;
        String sql = "SELECT ISNULL(SUM(lh.soLuongLoHang * lh.gia), 0) AS ChiPhi FROM LoHang lh WHERE lh.ngayNhap BETWEEN ? AND ?";
        try (PreparedStatement pst = getConn().prepareStatement(sql)) {
            pst.setTimestamp(1, Timestamp.valueOf(tuNgay));
            pst.setTimestamp(2, Timestamp.valueOf(denNgay));
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    cp = rs.getDouble("ChiPhi");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dt - cp;
    }

    // =====================================================================
    // NHÂN VIÊN DƯỢC SĨ
    // =====================================================================

    /**
     * Trả về List<String[3]>: {id, hoVaTen, chucVu} của tất cả dược sĩ đang làm
     * việc
     */
    public List<String[]> getDuocSiList() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT id, hoVaTen, chucVu FROM NhanVien "
                + "WHERE trangThaiLamViec='DANG_LAM_VIEC' ORDER BY hoVaTen";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(new String[] { rs.getString("id"), rs.getString("hoVaTen"), rs.getString("chucVu") });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public String getPTTT(Connection con, String hdId) {
        String sql = "SELECT phuongThucThanhToan FROM HoaDon WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hdId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getString(1);
            }
        } catch (SQLException ignored) {
        }
        return "TIEN_MAT";
    }

    public List<Object[]> getRawHDHomNay(BUS.BUS_ThongKe.ThongKeFilter filter, String pttt) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT hd.id, hd.ghiChu, "
                + "SUM(ABS(ct.thanhTien)) as tongGocCoVAT, "
                + "SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)) as tongGocChuaVAT "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId "
                + "JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId "
                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
                + "WHERE CONVERT(DATE, hd.ngayLapHD) = CONVERT(DATE, GETDATE()) AND hd.loaiHD = 'BAN_HANG'");
        List<Object> params = new ArrayList<>();
        if (pttt != null && !pttt.isEmpty()) {
            sql.append(" AND hd.phuongThucThanhToan=?");
            params.add(pttt);
        }
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY hd.id, hd.ghiChu");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                // TRẢ VỀ 4 PHẦN TỬ CHUẨN FORM
                result.add(new Object[] { "", rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"), rs.getDouble("tongGocChuaVAT") });
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }
    
    // RAW DATA — NHÓM HĐ BÁN HÀNG 7 NGÀY QUA
    // Trả về: List<Object[]> = { tongGocCoVAT(double), ghiChu(String) }
    /**
     * Raw HĐ bán hàng 7 ngày qua.
     *
     * @param maNV ID nhân viên (rỗng/null = tất cả)
     * @param pttt phương thức thanh toán (null = tất cả)
     */
    public List<Object[]> getRawHD7NgayQua(BUS.BUS_ThongKe.ThongKeFilter filter, String pttt) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT hd.id, hd.ghiChu, "
                + "SUM(ABS(ct.thanhTien)) as tongGocCoVAT, "
                + "SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)) as tongGocChuaVAT "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId "
                + "JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId "
                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
                + "WHERE hd.ngayLapHD >= DATEADD(DAY,-7,GETDATE()) AND hd.loaiHD = 'BAN_HANG'");
        List<Object> params = new ArrayList<>();
        if (pttt != null && !pttt.isEmpty()) {
            sql.append(" AND hd.phuongThucThanhToan=?");
            params.add(pttt);
        }
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY hd.id, hd.ghiChu");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                // TRẢ VỀ MẢNG 4 PHẦN TỬ (Đồng bộ cấu trúc)
                result.add(new Object[] { rs.getString("id"), rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"), rs.getDouble("tongGocChuaVAT") });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Object[]> getRawHDByDateRange(LocalDateTime tuNgay, LocalDateTime denNgay) {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT hd.ghiChu, "
                + "SUM(ABS(ct.thanhTien)) as tongGocCoVAT, "
                + "SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)) as tongGocChuaVAT "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId "
                + "JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId "
                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
                + "WHERE hd.ngayLapHD BETWEEN ? AND ? AND hd.loaiHD = 'BAN_HANG' "
                + "GROUP BY hd.id, hd.ghiChu";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(tuNgay));
            ps.setTimestamp(2, Timestamp.valueOf(denNgay));
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.add(new Object[] { "", rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"), rs.getDouble("tongGocChuaVAT") });
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public List<Object[]> getRawHDTheoCa(String maNV, LocalDateTime start, String pttt) {
        List<Object[]> result = new ArrayList<>();
        String ptttCond = (pttt != null && !pttt.isEmpty()) ? " AND hd.phuongThucThanhToan='" + pttt + "'" : "";
        String sql = "SELECT hd.id, hd.ghiChu, "
                + "SUM(ABS(ct.thanhTien)) as tongGocCoVAT, "
                + "SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)) as tongGocChuaVAT "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId "
                + "JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId "
                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
                + "WHERE hd.nhanVienId=? AND hd.ngayLapHD>=? AND hd.loaiHD='BAN_HANG'" + ptttCond
                + " GROUP BY hd.id, hd.ghiChu";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.add(new Object[] { rs.getString("id"), rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"), rs.getDouble("tongGocChuaVAT") });
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    public List<Object[]> getRawHD12Thang(int year, BUS.BUS_ThongKe.ThongKeFilter filter) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT MONTH(hd.ngayLapHD) m, "
                + "SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)) AS doanhThuThuan "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
                + "JOIN DonViDoLuong dvl ON ct.donViDoLuongId = dvl.id AND ct.sanPhamId = dvl.sanPhamId "
                + "WHERE YEAR(hd.ngayLapHD) = ? AND hd.loaiHD = 'BAN_HANG'");
        List<Object> params = new ArrayList<>();
        params.add(year);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY MONTH(hd.ngayLapHD)");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(new Object[] { rs.getInt("m"), rs.getDouble("doanhThuThuan") });
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    public List<Object[]> getRawHD30Ngay(BUS.BUS_ThongKe.ThongKeFilter filter) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT CONVERT(NVARCHAR,CAST(hd.ngayLapHD AS DATE),103) d, hd.id, hd.ghiChu, "
                        + "SUM(ABS(ct.thanhTien)) as tongGocCoVAT, "
                + "SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)) as tongGocChuaVAT " 
                        + "FROM HoaDon hd "
                        + "JOIN ChiTietHoaDon ct ON ct.hoaDonId=hd.id "
                        + "JOIN DonViDoLuong dvl ON dvl.id=ct.donViDoLuongId AND dvl.sanPhamId=ct.sanPhamId "
                        + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
                        + "WHERE hd.loaiHD='BAN_HANG' "
                        + "AND CAST(hd.ngayLapHD AS DATE)>=DATEADD(DAY,-29,CAST(GETDATE() AS DATE))");
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY CAST(hd.ngayLapHD AS DATE), CONVERT(NVARCHAR,CAST(hd.ngayLapHD AS DATE),103), hd.id, hd.ghiChu ORDER BY CAST(hd.ngayLapHD AS DATE) ASC");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.add(new Object[] { rs.getString("d"), rs.getString("id"),
                        rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"), rs.getDouble("tongGocChuaVAT") });
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }
    // =====================================================================
    // RAW DATA — NHÓM ĐỔI / TRẢ HÀNG (cho BUS.tinhDieuChinhDoiTra)
    // Trả về: List<Object[]> = { loaiHD(String), ghiChu(String) }
    // =====================================================================

    /**
     * Raw HĐ đổi/trả đã hoàn thành (BUS xử lý split chuỗi ghiChu).
     *
     * @param dateCondition điều kiện lọc ngày, VD
     *                      "CONVERT(DATE,hd.ngayLapHD)=CONVERT(DATE,GETDATE())"
     * @param extraCond     điều kiện bổ sung (NV, ca...)
     */
    public List<Object[]> getRawHDDoiTra(String dateCondition, BUS.BUS_ThongKe.ThongKeFilter filter) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT loaiHD, ghiChu FROM HoaDon hd WHERE " + dateCondition
                + " AND hd.loaiHD IN ('TRA_HANG','DOI_HANG') AND hd.ghiChu LIKE N'%Hoàn thành%'");
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.add(new Object[] { rs.getString("loaiHD"), rs.getString("ghiChu") });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // =====================================================================
    // RAW DATA — NHÓM DOANH THU THEO GIỜ TRONG NGÀY
    // Trả về: List<Object[]> = { hour(int), tongGocCoVAT(double), ghiChu(String) }
    // =====================================================================

    /**
     * Raw HĐ bán hàng theo giờ trong 1 ngày cụ thể.
     *
     * @param dateYMD định dạng yyyy-MM-dd
     * @param condNV  điều kiện NV + ca (có thể rỗng)
     */
    public List<Object[]> getRawHDGioTrongNgay(String dateYMD, BUS.BUS_ThongKe.ThongKeFilter filter) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DATEPART(HOUR, hd.ngayLapHD) AS h, "
                + "SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)) AS doanhThuThuan "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
                + "JOIN DonViDoLuong dvl ON ct.donViDoLuongId = dvl.id AND ct.sanPhamId = dvl.sanPhamId "
                + "WHERE hd.loaiHD = 'BAN_HANG' AND CAST(hd.ngayLapHD AS DATE) = ? ");
        List<Object> params = new ArrayList<>();
        params.add(dateYMD);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY DATEPART(HOUR, hd.ngayLapHD)");
        
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(new Object[] { rs.getInt("h"), rs.getDouble("doanhThuThuan") });
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    public List<Object[]> getRawHDForTopSP(String dateYMD, BUS.BUS_ThongKe.ThongKeFilter filter) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT hd.id, hd.ghiChu, "
                + "SUM(ABS(ct.thanhTien)) as tongGocCoVAT, "
                + "SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)) as tongGocChuaVAT "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId "
                + "JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId "
                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
                + "WHERE hd.loaiHD='BAN_HANG' AND CAST(hd.ngayLapHD AS DATE)=?");
        List<Object> params = new ArrayList<>();
        params.add(dateYMD);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY hd.id, hd.ghiChu");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.add(new Object[] { rs.getString("id"), rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"), rs.getDouble("tongGocChuaVAT") });
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }	

    public List<Object[]> getRawCTHD(String hdId) {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT sp.ten, ct.soLuong, "
                + "ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0) AS doanhThuThuan "
                + "FROM ChiTietHoaDon ct "
                + "JOIN HoaDon hd ON ct.hoaDonId = hd.id "
                + "JOIN SanPham sp ON sp.id = ct.sanPhamId "
                + "JOIN DonViDoLuong dvl ON dvl.sanPhamId = ct.sanPhamId AND dvl.id = ct.donViDoLuongId "
                + "WHERE ct.hoaDonId = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, hdId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.add(new Object[] { rs.getString("ten"), rs.getInt("soLuong"), rs.getDouble("doanhThuThuan") });
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    public List<Object[]> getRawHDCaByTime(String nvId, int year, BUS.BUS_ThongKe.ThongKeFilter filter, int startHour, int endHour) {
        List<Object[]> result = new ArrayList<>();
        String timeCond = (startHour > endHour)
                ? "AND (DATEPART(HOUR,hd.ngayLapHD) >= 22 OR DATEPART(HOUR,hd.ngayLapHD) < 6)"
                : "AND DATEPART(HOUR,hd.ngayLapHD) BETWEEN " + startHour + " AND " + endHour;

        StringBuilder sql = new StringBuilder("SELECT hd.id, hd.ghiChu, "
                + "SUM(ABS(ct.thanhTien)) as tongGocCoVAT, "
                + "SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)) as tongGocChuaVAT "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON ct.hoaDonId=hd.id "
                + "JOIN DonViDoLuong dvl ON dvl.id=ct.donViDoLuongId AND dvl.sanPhamId=ct.sanPhamId "
                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
                + "WHERE hd.nhanVienId=? AND YEAR(hd.ngayLapHD)=? AND hd.loaiHD='BAN_HANG' " + timeCond);
        List<Object> params = new ArrayList<>();
        params.add(nvId); params.add(year);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY hd.id, hd.ghiChu");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.add(new Object[] { rs.getString("id"), rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"), rs.getDouble("tongGocChuaVAT") });
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    public List<Object[]> getRawKHHD(int year, BUS.BUS_ThongKe.ThongKeFilter filter) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT kh.hoVaTen, kh.diemTichLuy, hd.ghiChu, "
                + "SUM(ABS(ct.thanhTien)) as tongGocCoVAT, "
                + "SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)) as tongGocChuaVAT "
                + "FROM KhachHang kh "
                + "JOIN HoaDon hd ON hd.khachHangId=kh.id "
                + "JOIN ChiTietHoaDon ct ON ct.hoaDonId=hd.id "
                + "JOIN DonViDoLuong dvl ON dvl.id=ct.donViDoLuongId AND dvl.sanPhamId=ct.sanPhamId "
                + "JOIN SanPham sp ON sp.id=ct.sanPhamId "
                + "WHERE hd.loaiHD='BAN_HANG' AND YEAR(hd.ngayLapHD)=?");
        List<Object> params = new ArrayList<>();
        params.add(year);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY kh.hoVaTen, kh.diemTichLuy, hd.id, hd.ghiChu");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.add(new Object[] { rs.getString("hoVaTen"), rs.getInt("diemTichLuy"),
                        rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"), rs.getDouble("tongGocChuaVAT") });
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // =====================================================================
    // RAW DATA — NHÓM HĐ GẦN NHẤT / GIÁ TRỊ CAO
    // Trả về: List<Object[]> = { hdId, tenKH, tongGocCoVAT, ghiChu, pttt, gio }
    // gio = null nếu không cần
    // =====================================================================

    /**
     * Raw data các hóa đơn bán hàng hôm nay, sắp xếp theo thời gian mới nhất.
     */
    public List<Object[]> getRawHDDashboard(BUS.BUS_ThongKe.ThongKeFilter filter, int limit, boolean includeGio) {
        List<Object[]> result = new ArrayList<>();
        String topStr = limit > 0 ? "TOP " + limit + " " : "";
        String gioCol = includeGio ? ", FORMAT(hd.ngayLapHD, 'HH:mm') AS gio" : "";
        
        StringBuilder sql = new StringBuilder("SELECT " + topStr + "hd.id, ISNULL(kh.hoVaTen, N'Khách lẻ') AS kh, "
                + "ISNULL(SUM(ct.soLuong * dvl.gia * (1 + ISNULL(sp.thueVAT, 0)/100.0)), 0) AS tongGocCoVAT, "
                + "hd.ghiChu, hd.phuongThucThanhToan AS pttt " + gioCol + ", hd.loaiHD " 
                + "FROM HoaDon hd "
                + "LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id "
                + "LEFT JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
                + "LEFT JOIN DonViDoLuong dvl ON ct.donViDoLuongId = dvl.id AND ct.sanPhamId = dvl.sanPhamId "
                + "LEFT JOIN SanPham sp ON ct.sanPhamId = sp.id "
                + "WHERE hd.loaiHD IN ('BAN_HANG', 'TRA_HANG', 'DOI_HANG') AND CAST(hd.ngayLapHD AS DATE) = CAST(GETDATE() AS DATE) ");
        
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY hd.id, kh.hoVaTen, hd.phuongThucThanhToan, hd.ngayLapHD, hd.ghiChu, hd.loaiHD ");
        sql.append(" ORDER BY hd.ngayLapHD DESC");

        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String gio = includeGio ? rs.getString("gio") : null;
                // Add thêm loaiHD vào mảng trả về
                result.add(new Object[] { rs.getString("id"), rs.getString("kh"),
                        rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"),
                        rs.getString("pttt"), gio, rs.getString("loaiHD") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    public List<Object[]> getRawHDDoiTraGio(String dateCondition, BUS.BUS_ThongKe.ThongKeFilter filter) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT hd.loaiHD, hd.ghiChu, DATEPART(HOUR, hd.ngayLapHD) AS h, "
                + "SUM(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0) / 100.0)) AS netRefund_chuaVAT "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
                + "WHERE " + dateCondition
                + " AND hd.loaiHD IN ('TRA_HANG','DOI_HANG') AND hd.ghiChu LIKE N'%Hoàn thành%' "
                + " AND (hd.loaiHD = 'TRA_HANG' OR ct.ghiChu = 'TRA_LAI' OR ct.soLuong < 0) ");
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY hd.id, hd.loaiHD, hd.ghiChu, DATEPART(HOUR, hd.ngayLapHD)");

        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.add(new Object[] { rs.getString("loaiHD"), rs.getString("ghiChu"), rs.getInt("h"), rs.getDouble("netRefund_chuaVAT") });
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // =====================================================================
    // DONUT - PHÂN LOẠI SẢN PHẨM (không cần tinhTienThucTe → giữ nguyên)
    // =====================================================================

    public int[] getSoLuongTheoLoaiSP(int year, BUS.BUS_ThongKe.ThongKeFilter filter) {
        String[] catDB = { "THUOC_KE_DON", "THUOC_KHONG_KE_DON", "THUC_PHAM_CHUC_NANG", "MY_PHAM" };
        int[] catVals = new int[4];
        int total = 0;

        for (int i = 0; i < 4; i++) {
            StringBuilder sql = new StringBuilder("SELECT ISNULL(SUM(ct.soLuong),0) FROM ChiTietHoaDon ct "
                    + "JOIN HoaDon hd ON hd.id=ct.hoaDonId "
                    + "JOIN SanPham sp ON sp.id=ct.sanPhamId "
                    + "WHERE sp.danhMuc=? AND YEAR(hd.ngayLapHD)=? AND hd.loaiHD='BAN_HANG'");
            List<Object> params = new ArrayList<>();
            params.add(catDB[i]);
            params.add(year);
            applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");

            try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
                for (int p = 0; p < params.size(); p++)
                    ps.setObject(p + 1, params.get(p));
                ResultSet rs = ps.executeQuery();
                if (rs.next())
                    catVals[i] = Math.max(1, rs.getInt(1));
            } catch (Exception e) {
                catVals[i] = 1;
            }
            total += catVals[i];
        }
        int[] result = new int[4];
        for (int i = 0; i < 4; i++)
            result[i] = Math.max(1, (int) Math.round(catVals[i] * 100.0 / total));
        return result;
    }

    // =====================================================================
    // CHI PHÍ 12 THÁNG (không cần tinhTienThucTe → giữ nguyên)
    // =====================================================================

    public double[] getChiPhi12Thang(int year, BUS.BUS_ThongKe.ThongKeFilter filter) {
        double[] result = new double[12];
        StringBuilder sql = new StringBuilder("SELECT MONTH(hd.ngayLapHD) AS thang, "
                + "       SUM(pblh.soLuong * lh.gia) AS chiPhi "
                + "FROM HoaDon hd "
                + "JOIN PhanBoLoHang pblh ON pblh.hoaDonId = hd.id "
                + "JOIN LoHang lh         ON pblh.loHangId = lh.id "
                + "WHERE YEAR(hd.ngayLapHD) = ? "
                + "  AND hd.loaiHD = 'BAN_HANG' ");
        List<Object> params = new ArrayList<>();
        params.add(year);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY MONTH(hd.ngayLapHD)");

        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int thang = rs.getInt("thang");
                if (thang >= 1 && thang <= 12)
                    result[thang - 1] = rs.getDouble("chiPhi") / 1_000_000.0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // =====================================================================
    // THỐNG KÊ THEO NGÀY (10 ngày, đếm HĐ, v.v.)
    // =====================================================================

    /** 10 ngày gần nhất có HĐ, sắp xếp tăng dần, định dạng dd/MM/yyyy */
    public List<String> get10NgayGanNhat(int year, BUS.BUS_ThongKe.ThongKeFilter filter) {
        List<String> dates = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT TOP 10 CONVERT(NVARCHAR, CAST(hd.ngayLapHD AS DATE), 103) AS d "
                + "FROM HoaDon hd WHERE hd.loaiHD='BAN_HANG' AND YEAR(hd.ngayLapHD)=?");
        List<Object> params = new ArrayList<>();
        params.add(year);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY CAST(hd.ngayLapHD AS DATE) ORDER BY CAST(hd.ngayLapHD AS DATE) DESC");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                dates.add(0, rs.getString("d")); // đảo để tăng dần
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dates;
    }

    /** Đếm số HĐ của 1 NV trong 1 ngày cụ thể (định dạng dd/MM/yyyy) */
    public int getDailyHDCuaNV(String nvId, String date, BUS.BUS_ThongKe.ThongKeFilter filter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM HoaDon hd WHERE hd.nhanVienId=? "
                + "AND CONVERT(NVARCHAR,CONVERT(DATE,hd.ngayLapHD),103)=? AND hd.loaiHD='BAN_HANG'");
        List<Object> params = new ArrayList<>();
        params.add(nvId);
        params.add(date);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int p = 0; p < params.size(); p++)
                ps.setObject(p + 1, params.get(p));
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (Exception e) {
            /* ignored */ }
        return 0;
    }

    // =====================================================================
    // TỔNG SỐ HÓA ĐƠN
    // =====================================================================

    public long getTongHoaDon(int year, BUS.BUS_ThongKe.ThongKeFilter filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM HoaDon hd WHERE YEAR(hd.ngayLapHD)=? AND hd.loaiHD='BAN_HANG'");
        List<Object> params = new ArrayList<>();
        params.add(year);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int p = 0; p < params.size(); p++)
                ps.setObject(p + 1, params.get(p));
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getLong(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Object[]> getTopSanPham(int year, BUS.BUS_ThongKe.ThongKeFilter filter) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT sp.ten, sp.danhMuc, SUM(ct.soLuong) AS sl, "
                + "SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)) AS dtThuan "
                + "FROM ChiTietHoaDon ct "
                + "JOIN HoaDon hd ON hd.id = ct.hoaDonId "
                + "JOIN SanPham sp ON sp.id = ct.sanPhamId "
                + "JOIN DonViDoLuong dvl ON dvl.sanPhamId = ct.sanPhamId AND dvl.id = ct.donViDoLuongId "
                + "WHERE YEAR(hd.ngayLapHD) = ? AND hd.loaiHD = 'BAN_HANG'");
        List<Object> params = new ArrayList<>();
        params.add(year);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY sp.ten, sp.danhMuc ORDER BY dtThuan DESC");

        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int p = 0; p < params.size(); p++) ps.setObject(p + 1, params.get(p));
            ResultSet rs = ps.executeQuery();
            int rank = 0;
            while (rs.next() && rank < 10) {
                result.add(new Object[] { rs.getString("ten"), rs.getString("danhMuc"), 
                                          rs.getInt("sl"), rs.getDouble("dtThuan") / 1_000_000.0 });
                rank++;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    public List<Object[]> getVATReport(int year, BUS.BUS_ThongKe.ThongKeFilter filter) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT sp.id AS spId, sp.ten, sp.danhMuc, ISNULL(sp.thueVAT, 0) AS vatPct, "
                + "SUM(ROUND(ABS(ct.thanhTien) * (ISNULL(sp.thueVAT,0)/100.0) / (1 + ISNULL(sp.thueVAT,0)/100.0), 0)) AS tienThue "
                + "FROM ChiTietHoaDon ct "
                + "JOIN HoaDon hd ON hd.id = ct.hoaDonId "
                + "JOIN SanPham sp ON sp.id = ct.sanPhamId "
                + "JOIN DonViDoLuong dvl ON dvl.sanPhamId = ct.sanPhamId AND dvl.id = ct.donViDoLuongId "
                + "WHERE YEAR(hd.ngayLapHD) = ? AND hd.loaiHD = 'BAN_HANG'");
        List<Object> params = new ArrayList<>();
        params.add(year);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY sp.id, sp.ten, sp.danhMuc, ISNULL(sp.thueVAT, 0) ORDER BY tienThue DESC");

        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int p = 0; p < params.size(); p++) ps.setObject(p + 1, params.get(p));
            ResultSet rs = ps.executeQuery();
            int count = 0;
            while (rs.next() && count < 20) {
                result.add(new Object[] { rs.getString("spId"), rs.getString("ten"), rs.getString("danhMuc"), 
                                          rs.getInt("vatPct"), rs.getDouble("tienThue") / 1_000_000.0 });
                count++;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // =====================================================================
    // SẢN PHẨM SẮP HẾT HẠN
    // =====================================================================

    /**
     * Lô hàng còn hàng, hết hạn trong 6 tháng tới.
     * Mỗi Object[5]: {soLoHang, tenSP, maKho, soLuong(int), ngayHetHan(String
     * dd/MM/yyyy)}
     */
    public List<Object[]> getSpSapHetHan() {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT lh.soLoHang, sp.ten, kh.id AS Kho, lh.soLuongLoHang, lh.ngayHetHan "
                + "FROM LoHang lh "
                + "JOIN SanPham sp ON lh.sanPhamId = sp.id "
                + "JOIN KhoHang kh ON lh.khoHangId = kh.id "
                + "WHERE lh.trangThai = 'CON_HANG' "
                + "AND lh.ngayHetHan <= DATEADD(MONTH, 6, GETDATE()) "
                + "ORDER BY lh.ngayHetHan ASC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            while (rs.next())
                result.add(new Object[] { rs.getString("soLoHang"), rs.getString("ten"),
                        rs.getString("Kho"), rs.getInt("soLuongLoHang"),
                        sdf.format(rs.getDate("ngayHetHan")) });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object[] getThongKeNgayCuThe(String dateYMD) {
        String sql = "SELECT COUNT(DISTINCT hd.id) AS tongHD, "
                + "COUNT(DISTINCT hd.khachHangId) AS tongKH, "
                + "ISNULL(SUM(ct.soLuong),0) AS tongSP, "
                + "ISNULL(SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)),0) AS tongDT "
                + "FROM HoaDon hd "
                + "LEFT JOIN ChiTietHoaDon ct ON ct.hoaDonId = hd.id "
                + "LEFT JOIN SanPham sp ON sp.id = ct.sanPhamId "
                + "LEFT JOIN DonViDoLuong dvl ON dvl.id = ct.donViDoLuongId AND dvl.sanPhamId = ct.sanPhamId "
                + "WHERE hd.loaiHD = 'BAN_HANG' AND CAST(hd.ngayLapHD AS DATE) = ?";
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dateYMD);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Object[] { rs.getInt("tongHD"), rs.getDouble("tongDT") / 1_000_000.0, rs.getInt("tongKH"), rs.getInt("tongSP") };
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new Object[] { 0, 0.0, 0, 0 };
    }

    public List<Object[]> getNVTrongNgay(String dateYMD) {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT nv.hoVaTen, COUNT(DISTINCT hd.id) AS soHD, "
                + "ISNULL(SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)),0) AS dtThuan "
                + "FROM HoaDon hd "
                + "JOIN NhanVien nv ON nv.id = hd.nhanVienId "
                + "LEFT JOIN ChiTietHoaDon ct ON ct.hoaDonId = hd.id "
                + "LEFT JOIN DonViDoLuong dvl ON dvl.id = ct.donViDoLuongId AND dvl.sanPhamId = ct.sanPhamId "
                + "WHERE hd.loaiHD = 'BAN_HANG' AND CAST(hd.ngayLapHD AS DATE) = ? "
                + "GROUP BY nv.hoVaTen ORDER BY dtThuan DESC";
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dateYMD);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(new Object[] { rs.getString("hoVaTen"), rs.getInt("soHD"), rs.getDouble("dtThuan") / 1_000_000.0 });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    public List<Object[]> getThongKeTuan(String weekStartYMD) {
        List<Object[]> result = new ArrayList<>();
        // Query per-(ngày, HĐ) để tính KM chính xác từng hóa đơn
        String sql = "SELECT CONVERT(NVARCHAR,CAST(hd.ngayLapHD AS DATE),103) AS d, "
                + "CAST(hd.ngayLapHD AS DATE) AS rawDate, "
                + "hd.id AS hdId, hd.ghiChu, "
                + "ISNULL(SUM(ct.soLuong * dvl.gia * (1 + ISNULL(sp.thueVAT, 0)/100.0)),0) AS tongGocCoVAT "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON ct.hoaDonId=hd.id "
                + "JOIN DonViDoLuong dvl ON dvl.id=ct.donViDoLuongId AND dvl.sanPhamId=ct.sanPhamId "
                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
                + "WHERE hd.loaiHD='BAN_HANG' "
                + "AND CAST(hd.ngayLapHD AS DATE) BETWEEN ? AND DATEADD(DAY,6,CAST(? AS DATE)) "
                + "GROUP BY CAST(hd.ngayLapHD AS DATE), CONVERT(NVARCHAR,CAST(hd.ngayLapHD AS DATE),103), hd.id, hd.ghiChu "
                + "ORDER BY CAST(hd.ngayLapHD AS DATE), hd.id";
        // Gom per-HĐ theo ngày (dd/MM/yyyy) — dùng LinkedHashMap giữ thứ tự ngày tăng
        // dần
        Map<String, double[]> dayMap = new java.util.LinkedHashMap<>(); // d → [soHD, dt]
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, weekStartYMD);
            ps.setString(2, weekStartYMD);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String d = rs.getString("d");
                String ghiChu = rs.getString("ghiChu");
                double goc = rs.getDouble("tongGocCoVAT");
                double giam = tinhTongTienGiamInline(con, goc, ghiChu);
                double dt = Math.max(0, goc - giam) / 1.1;
                double[] cur = dayMap.computeIfAbsent(d, k -> new double[] { 0, 0 });
                cur[0]++; // soHD
                cur[1] += dt;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dayMap.forEach((d, v) -> result.add(new Object[] { d, (int) v[0], v[1] / 1_000_000.0 }));
        return result;
    }

    // =====================================================================
    // THỐNG KÊ KHÁCH HÀNG
    // =====================================================================

    /** KH mới từng tháng trong năm. Trả về int[12] */
    public int[] getKHMoiTheoThang(int year) {
        int[] data = new int[12];
        String sql = "SELECT MONTH(ngayTao) m, COUNT(*) cnt FROM KhachHang WHERE YEAR(ngayTao)=? GROUP BY MONTH(ngayTao)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int m = rs.getInt("m");
                if (m >= 1 && m <= 12)
                    data[m - 1] = rs.getInt("cnt");
            }
        } catch (Exception e) {
            /* ngayTao có thể null */ }
        return data;
    }

    /** KPI tổng hợp KH. Object[3]: {tongKH(int), khCoTK(int), tongDiem(int)} */
    public Object[] getKpiKhachHang() {
        Object[] result = { 0, 0, 0 };
        try (Statement st = getConn().createStatement()) {
            ResultSet rs = st.executeQuery(
                    "SELECT COUNT(*) tongKH, COUNT(sdt) khCoTK, ISNULL(SUM(diemTichLuy),0) tongDiem FROM KhachHang");
            if (rs.next())
                result = new Object[] { rs.getInt("tongKH"), rs.getInt("khCoTK"), rs.getInt("tongDiem") };
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Top KH sắp xếp theo điểm tích lũy giảm dần.
     * Mỗi Object[5]: {hoVaTen, sdt, soHD, tongDT_trieu, diemTichLuy}
     *
     * Công thức đúng: per-HĐ → trừ KM từ ghiChu → chia 1.1 → gom theo KH.
     * Bước 1: tính dt chính xác per KH từ các HĐ (INNER JOIN HoaDon).
     * Bước 2: query top KH theo diemTichLuy rồi merge (KH chưa mua → soHD=0, dt=0).
     */
    public List<Object[]> getTopKhachHangTheoDiem(int limit) {
        List<Object[]> result = new ArrayList<>();
        try (Connection con = getConn()) {
            // Bước 1: tính per-HĐ, gom theo khachHangId
            Map<String, double[]> purchaseMap = new HashMap<>(); // khId → [soHD, dt]
            String sqlP = "SELECT kh.id AS khId, hd.id AS hdId, hd.ghiChu, "
                    + "ISNULL(SUM(ct.soLuong * dvl.gia * (1 + ISNULL(sp.thueVAT, 0)/100.0)),0) AS tongGocCoVAT "
                    + "FROM HoaDon hd "
                    + "JOIN KhachHang kh ON kh.id=hd.khachHangId "
                    + "LEFT JOIN ChiTietHoaDon ct ON ct.hoaDonId=hd.id "
                    + "LEFT JOIN DonViDoLuong dvl ON dvl.id=ct.donViDoLuongId AND dvl.sanPhamId=ct.sanPhamId "
                    + "LEFT JOIN SanPham sp ON sp.id=ct.sanPhamId "
                    + "WHERE hd.loaiHD='BAN_HANG' "
                    + "GROUP BY kh.id, hd.id, hd.ghiChu";
            try (PreparedStatement ps = con.prepareStatement(sqlP); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String khId = rs.getString("khId");
                    double goc = rs.getDouble("tongGocCoVAT");
                    String ghiChu = rs.getString("ghiChu");
                    double giam = tinhTongTienGiamInline(con, goc, ghiChu);
                    double dt = Math.max(0, goc - giam) / 1.1;
                    double[] cur = purchaseMap.computeIfAbsent(khId, k -> new double[] { 0, 0 });
                    cur[0]++; // soHD
                    cur[1] += dt;
                }
            }
            // Bước 2: query top KH theo điểm, merge với dữ liệu mua hàng
            String sqlKH = "SELECT TOP " + limit + " id, hoVaTen, ISNULL(sdt,'') AS sdt, diemTichLuy "
                    + "FROM KhachHang ORDER BY diemTichLuy DESC";
            try (java.sql.Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlKH)) {
                while (rs.next()) {
                    String khId = rs.getString("id");
                    double[] p = purchaseMap.getOrDefault(khId, new double[] { 0, 0 });
                    result.add(new Object[] { rs.getString("hoVaTen"), rs.getString("sdt"),
                            (int) p[0], p[1] / 1_000_000.0, rs.getInt("diemTichLuy") });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // =====================================================================
    // THỐNG KÊ KHO HÀNG
    // =====================================================================

    public double getTongGiaTriTonKho() {
        String sql = "SELECT ISNULL(SUM(lh.soLuongLoHang*lh.gia),0) FROM LoHang lh WHERE lh.trangThai='CON_HANG'";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return rs.getDouble(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Object[] getSoLoTheoTrangThai() {
        Object[] result = { 0, 0, 0 };
        try (Statement st = getConn().createStatement()) {
            ResultSet rs = st.executeQuery(
                    "SELECT SUM(CASE WHEN trangThai='CON_HANG' THEN 1 ELSE 0 END) conHang, "
                            + "SUM(CASE WHEN trangThai='HET_HANG' THEN 1 ELSE 0 END) hetHang, "
                            + "SUM(CASE WHEN trangThai='HET_HAN' THEN 1 ELSE 0 END) hetHan "
                            + "FROM LoHang");
            if (rs.next())
                result = new Object[] { rs.getInt("conHang"), rs.getInt("hetHang"), rs.getInt("hetHan") };
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Phân bổ tồn kho theo kho. Mỗi Object[3]: {maKho, soLuong(int),
     * giaTriTrieu(double)}
     */
    public List<Object[]> getTonKhoTheoKho() {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT kh.id, ISNULL(SUM(lh.soLuongLoHang),0) sl, ISNULL(SUM(lh.soLuongLoHang*lh.gia),0)/1000000.0 gt "
                + "FROM KhoHang kh LEFT JOIN LoHang lh ON lh.khoHangId=kh.id AND lh.trangThai='CON_HANG' "
                + "GROUP BY kh.id ORDER BY gt DESC";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                result.add(new Object[] { rs.getString("id"), rs.getInt("sl"), rs.getDouble("gt") });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Top 10 SP tồn nhiều nhất. Mỗi Object[3]: {tenSP, soLuongTon(int),
     * giaTriTrieu(double)}
     */
    public List<Object[]> getTopSPTonNhieu() {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT TOP 10 sp.ten, ISNULL(SUM(lh.soLuongLoHang),0) sl, "
                + "ISNULL(SUM(lh.soLuongLoHang*lh.gia),0)/1000000.0 gt "
                + "FROM SanPham sp LEFT JOIN LoHang lh ON sp.id=lh.sanPhamId AND lh.trangThai='CON_HANG' "
                + "GROUP BY sp.ten ORDER BY sl DESC";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                result.add(new Object[] { rs.getString("ten"), rs.getInt("sl"), rs.getDouble("gt") });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public double[] getNhapHang12Thang(int year) {
        double[] data = new double[12];
        String sql = "SELECT MONTH(ngayNhap) m, ISNULL(SUM(soLuongLoHang*gia),0)/1000000.0 gt "
                + "FROM LoHang WHERE YEAR(ngayNhap)=? GROUP BY MONTH(ngayNhap)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int m = rs.getInt("m");
                if (m >= 1 && m <= 12)
                    data[m - 1] = rs.getDouble("gt");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    // =====================================================================
    // DASHBOARD — CÁC HÀM ĐẾM ĐƠN GIẢN (không cần tinhTienThucTe)
    // =====================================================================

    public int getTongSanPham() {
        try (Statement st = getConn().createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM SanPham")) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTongKhachHang() {
        try (Statement st = getConn().createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM KhachHang")) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getSoHoaDonTheoCa(String maNV, LocalDateTime start) {
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE nhanVienId=? AND ngayLapHD>=? AND loaiHD='BAN_HANG'";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Đếm số HĐ bán hàng hôm nay theo filter (NV, ca...) */
    public int getSoHoaDonHomNay(BUS.BUS_ThongKe.ThongKeFilter filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM HoaDon hd WHERE CAST(hd.ngayLapHD AS DATE) = CAST(GETDATE() AS DATE) AND hd.loaiHD = 'BAN_HANG'");
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getSoHoaDon7NgayQua(BUS.BUS_ThongKe.ThongKeFilter filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM HoaDon hd WHERE hd.ngayLapHD >= DATEADD(DAY, -7, GETDATE()) AND hd.loaiHD = 'BAN_HANG'");
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTongPhieuDoiTra(BUS.BUS_ThongKe.ThongKeFilter filter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM HoaDon hd WHERE hd.loaiHD IN ('TRA_HANG', 'DOI_HANG')");
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int p = 0; p < params.size(); p++) ps.setObject(p + 1, params.get(p));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getPhieuDoiTraChoXuLy(BUS.BUS_ThongKe.ThongKeFilter filter) {
        String sqlStr = "SELECT COUNT(*) FROM HoaDon hd WHERE hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') "
                + "AND (hd.ghiChu IS NULL OR (hd.ghiChu NOT LIKE N'%Hoàn thành%' AND hd.ghiChu NOT LIKE N'%Từ chối%'))";
        StringBuilder sql = new StringBuilder(sqlStr);
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int p = 0; p < params.size(); p++) ps.setObject(p + 1, params.get(p));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<Object[]> getTop4SanPhamSapHetHang() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT TOP 4 sp.ten, ISNULL(SUM(lh.soLuongLoHang),0) ton FROM SanPham sp "
                + "LEFT JOIN LoHang lh ON sp.id=lh.sanPhamId GROUP BY sp.ten ORDER BY ton ASC";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(new Object[] { rs.getString("ten"), rs.getInt("ton"), 50 });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getSoSanPhamDuTon() {
        try (Statement st = getConn().createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT COUNT(DISTINCT sp.id) FROM SanPham sp JOIN LoHang lh ON sp.id=lh.sanPhamId")) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getSoLoHangSapHetHanKhoang(int days) {
        try (Statement st = getConn().createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT COUNT(*) FROM LoHang WHERE ngayHetHan IS NOT NULL AND DATEDIFF(DAY,GETDATE(),ngayHetHan)<="
                                + days)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Object[]> getLoHangSapHetHanNhanh(int days) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT TOP 8 sp.ten, lh.soLoHang, lh.soLuongLoHang, lh.ngayHetHan, DATEDIFF(DAY,GETDATE(),lh.ngayHetHan) cl "
                + "FROM LoHang lh JOIN SanPham sp ON lh.sanPhamId=sp.id "
                + "WHERE lh.ngayHetHan IS NOT NULL AND DATEDIFF(DAY,GETDATE(),lh.ngayHetHan)<=? "
                + "ORDER BY lh.ngayHetHan ASC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, days);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(new Object[] { rs.getString("ten"), rs.getString("soLoHang"),
                        rs.getInt("soLuongLoHang"), rs.getTimestamp("ngayHetHan"), rs.getInt("cl") });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Raw data HĐ bán hàng của 1 ngày cụ thể. Trả về: List<Object[]> = {
     * id, tongGocCoVAT, ghiChu }
     */
    public List<Object[]> getRawHDByDay(String dateYMD, BUS.BUS_ThongKe.ThongKeFilter filter) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT hd.id, hd.ghiChu, "
                + "SUM(ABS(ct.thanhTien)) as tongGocCoVAT, "
                + "SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)) as tongGocChuaVAT "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
                + "JOIN DonViDoLuong dvl ON ct.donViDoLuongId = dvl.id AND ct.sanPhamId = dvl.sanPhamId "
                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
                + "WHERE hd.loaiHD = 'BAN_HANG' AND CAST(hd.ngayLapHD AS DATE) = ? ");
        List<Object> params = new ArrayList<>();
        params.add(dateYMD);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY hd.id, hd.ghiChu");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                // TRẢ VỀ MẢNG 4 PHẦN TỬ (Thêm tongGocChuaVAT ở cuối)
                result.add(new Object[] { rs.getString("id"), rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"), rs.getDouble("tongGocChuaVAT") });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // NHÂN VIÊN
    public List<String[]> getDanhSachNhanVien() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT id, hoVaTen FROM NhanVien WHERE trangThaiLamViec='DANG_LAM_VIEC'";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(new String[] { rs.getString("id"), rs.getString("hoVaTen") });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // =====================================================================
    // GỢI Ý KHUYẾN MÃI (SQL-level analytics — không cần tinhTienThucTe)
    // =====================================================================

    /**
     * Phân tích top SP để gợi ý KM.
     * Mỗi Object[10]: {tenSP, danhMuc, slBan, dtTrieu, giaVon, bienLN_pct, coKM,
     * goiYLoai, lyDo, mucGiam}
     */
    public List<Object[]> getGoiYKhuyenMai(int year) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT TOP 10 sp.ten, sp.danhMuc, "
                + "SUM(ct.soLuong) AS soLuongBan, "
                + "SUM(ct.soLuong * dvl.gia) AS doanhThu, "
                + "ISNULL(MAX(lh.gia), 0) AS giaVon "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
                + "JOIN DonViDoLuong dvl ON ct.donViDoLuongId = dvl.id AND ct.sanPhamId = dvl.sanPhamId "
                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
                + "LEFT JOIN LoHang lh ON lh.sanPhamId = sp.id AND lh.trangThai = 'CON_HANG' "
                + "WHERE YEAR(hd.ngayLapHD) = ? AND hd.loaiHD = 'BAN_HANG' "
                + "GROUP BY sp.ten, sp.danhMuc "
                + "ORDER BY soLuongBan DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String tenSP = rs.getString("ten");
                String danhMuc = rs.getString("danhMuc");
                if (danhMuc == null)
                    danhMuc = "Khác";
                int slBan = rs.getInt("soLuongBan");
                double doanhThu = rs.getDouble("doanhThu");
                double dtTrieu = doanhThu / 1000000.0;
                double giaVon = rs.getDouble("giaVon");
                double giaBanTB = (slBan > 0) ? (doanhThu / slBan) : 0;
                double bienLNPct = (giaBanTB > 0) ? ((giaBanTB - giaVon) / giaBanTB) * 100 : 0;
                String goiYLoai = "Khuyến mãi giảm giá";
                String lyDo = "Sản phẩm bán chạy";
                String mucGiam = "5%";
                if (bienLNPct > 40) {
                    goiYLoai = "Mua 2 tặng 1";
                    lyDo = "Biên lợi nhuận cao";
                    mucGiam = "10%";
                } else if (slBan > 100) {
                    goiYLoai = "Tích điểm nhân đôi";
                    lyDo = "Tăng tần suất mua lại";
                    mucGiam = "Quà tặng";
                }
                list.add(new Object[] { tenSP, danhMuc, slBan, dtTrieu, giaVon, bienLNPct, "Không", goiYLoai, lyDo,
                        mucGiam });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Object[]> getKhachHangVIPMuaHomNay(int limit) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT TOP " + limit + " kh.hoVaTen, kh.diemTichLuy, SUM(ct.soLuong) AS soSP "
                + "FROM HoaDon hd "
                + "JOIN KhachHang kh ON hd.khachHangId = kh.id "
                + "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
                + "WHERE CONVERT(DATE, hd.ngayLapHD) = CONVERT(DATE, GETDATE()) "
                + "AND hd.loaiHD = 'BAN_HANG' AND kh.diemTichLuy >= 500 "
                + "GROUP BY kh.hoVaTen, kh.diemTichLuy "
                + "ORDER BY kh.diemTichLuy DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(new Object[] { rs.getString("hoVaTen"), rs.getInt("diemTichLuy"), rs.getInt("soSP") });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Object[]> getRawHDGiaTriCao(int limit, BUS.BUS_ThongKe.ThongKeFilter filter) {
        return getRawHDDashboard(filter, limit, true);
    }
    
    public double[] getTienTraHangChinhXac(String dateCondition, BUS.BUS_ThongKe.ThongKeFilter filter) {
        double[] res = new double[]{0, 0};
        StringBuilder sql = new StringBuilder(
            "SELECT " +
            "ISNULL(SUM(ABS(ct.thanhTien)), 0) AS tienCoVAT, " +
            "ISNULL(SUM(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0) / 100.0)), 0) AS tienChuaVAT " +
            "FROM ChiTietHoaDon ct " +
            "JOIN HoaDon hd ON ct.hoaDonId = hd.id " +
            "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
            "WHERE (hd.loaiHD = 'TRA_HANG' OR ct.ghiChu = 'TRA_LAI' OR ct.soLuong < 0) " +
            "AND hd.ghiChu LIKE N'%Hoàn thành%' AND " + dateCondition);
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");

        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                res[0] = rs.getDouble("tienCoVAT");
                res[1] = rs.getDouble("tienChuaVAT");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return res;
    }
 
    public double getPercentKhuyenMai(String hdId) {
    	String sql = "SELECT ISNULL(MAX(ht.giaTri), 0) FROM HoaDon hd " +
                  "JOIN HinhThucKhuyenMai ht ON hd.khuyenMaiId = ht.khuyenMaiId " +
                  "WHERE hd.id = ? AND ht.loaiHinhThuc = 'GIAM_THEO_PHAN_TRAM'";
    	try (PreparedStatement ps = getConn().prepareStatement(sql)) {
    		ps.setString(1, hdId);
    		ResultSet rs = ps.executeQuery();
    		if (rs.next()) return rs.getDouble(1);
    	}catch (Exception e) { e.printStackTrace(); }
    	return 0;
    }
    
	 public List<Object[]> getRawLineItems(String hdId) {
	     List<Object[]> list = new ArrayList<>();
	     String sql = "SELECT ct.soLuong, dvl.gia, ISNULL(sp.thueVAT,0) AS thueVAT, ABS(ct.thanhTien) AS thanhTien " +
	                  "FROM ChiTietHoaDon ct " +
	                  "JOIN DonViDoLuong dvl ON ct.donViDoLuongId = dvl.id AND ct.sanPhamId = dvl.sanPhamId " +
	                  "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
	                  "WHERE ct.hoaDonId = ?";
	     try (PreparedStatement ps = getConn().prepareStatement(sql)) {
	         ps.setString(1, hdId);
	         ResultSet rs = ps.executeQuery();
	         while (rs.next()) {
	             list.add(new Object[] { 
	                 rs.getInt("soLuong"), 
	                 rs.getDouble("gia"), 
	                 rs.getDouble("thueVAT"),
	                 rs.getDouble("thanhTien")
	             });
	         }
	     } catch (Exception e) { e.printStackTrace(); }
	     return list;
	 }
	 
	 public List<Object[]> getTraHangChiTiet(String dateYMD, BUS.BUS_ThongKe.ThongKeFilter filter) {
	        List<Object[]> result = new ArrayList<>();
	        StringBuilder sql = new StringBuilder("SELECT sp.ten, ABS(ct.soLuong) AS sl, "
	                + "ABS(ct.thanhTien / (1 + ISNULL(sp.thueVAT, 0) / 100.0)) AS chuaVAT "
	                + "FROM ChiTietHoaDon ct "
	                + "JOIN HoaDon hd ON ct.hoaDonId = hd.id "
	                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
	                + "WHERE (hd.loaiHD = 'TRA_HANG' OR ct.ghiChu = 'TRA_LAI' OR ct.soLuong < 0) "
	                + "AND hd.ghiChu LIKE N'%Hoàn thành%' "
	                + "AND CAST(hd.ngayLapHD AS DATE) = ? ");
	        List<Object> params = new ArrayList<>();
	        params.add(dateYMD);
	        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");

	        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
	            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
	            ResultSet rs = ps.executeQuery();
	            while (rs.next()) {
	                result.add(new Object[] { rs.getString("ten"), rs.getInt("sl"), rs.getDouble("chuaVAT") });
	            }
	        } catch (Exception e) { e.printStackTrace(); }
	        return result;
	    }
	 
	 public List<Object[]> getRawHDDoiTra12Thang(int year, BUS.BUS_ThongKe.ThongKeFilter filter) {
	        List<Object[]> result = new ArrayList<>();
	        StringBuilder sql = new StringBuilder("SELECT MONTH(hd.ngayLapHD) AS m, "
	                + "SUM(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0) / 100.0)) AS netRefund_chuaVAT "
	                + "FROM HoaDon hd "
	                + "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
	                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
	                + "WHERE YEAR(hd.ngayLapHD) = ? "
	                + " AND hd.loaiHD IN ('TRA_HANG','DOI_HANG') AND hd.ghiChu LIKE N'%Hoàn thành%' "
	                + " AND (hd.loaiHD = 'TRA_HANG' OR ct.ghiChu = 'TRA_LAI' OR ct.soLuong < 0) ");
	        List<Object> params = new ArrayList<>();
	        params.add(year);
	        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
	        sql.append(" GROUP BY MONTH(hd.ngayLapHD)");

	        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
	            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
	            ResultSet rs = ps.executeQuery();
	            while (rs.next()) result.add(new Object[] { rs.getInt("m"), rs.getDouble("netRefund_chuaVAT") });
	        } catch (Exception e) { e.printStackTrace(); }
	        return result;
	    }

	public String buildConditionFromFilter(BUS.BUS_ThongKe.ThongKeFilter f) {
	    if (f == null) return " 1=1 ";
	    StringBuilder sb = new StringBuilder(" 1=1 ");
	    if (f.modeLocThoiGian != null) {
	        if (f.modeLocThoiGian.equals("THANG")) 
	            sb.append(" AND MONTH(hd.ngayLapHD) = ").append(f.month).append(" AND YEAR(hd.ngayLapHD) = YEAR(GETDATE())");
	        else if (f.modeLocThoiGian.equals("QUY"))
	            sb.append(" AND DATEPART(QUARTER, hd.ngayLapHD) = ").append(f.quarter).append(" AND YEAR(hd.ngayLapHD) = YEAR(GETDATE())");
	        else if (f.modeLocThoiGian.equals("TUYCHINH") && f.fromDate != null && f.toDate != null)
	            sb.append(" AND CAST(hd.ngayLapHD AS DATE) BETWEEN '").append(f.fromDate).append("' AND '").append(f.toDate).append("'");
	    }
	    return sb.toString();
	}

	public List<Object[]> getRawHDByFilter(BUS.BUS_ThongKe.ThongKeFilter filter) {
	    List<Object[]> result = new ArrayList<>();
	    StringBuilder sql = new StringBuilder("SELECT hd.id, hd.ghiChu FROM HoaDon hd WHERE hd.loaiHD = 'BAN_HANG' ");
	    List<Object> params = new ArrayList<>();
	    
	    // Áp dụng bộ lọc thời gian (Tháng/Quý/Tùy chỉnh)
	    if (filter.modeLocThoiGian != null) {
	        if (filter.modeLocThoiGian.equals("THANG")) {
	            sql.append(" AND MONTH(hd.ngayLapHD) = ? AND YEAR(hd.ngayLapHD) = YEAR(GETDATE())");
	            params.add(filter.month);
	        } else if (filter.modeLocThoiGian.equals("QUY")) {
	            sql.append(" AND DATEPART(QUARTER, hd.ngayLapHD) = ? AND YEAR(hd.ngayLapHD) = YEAR(GETDATE())");
	            params.add(filter.quarter);
	        } else if (filter.modeLocThoiGian.equals("TUYCHINH")) {
	            sql.append(" AND CAST(hd.ngayLapHD AS DATE) BETWEEN ? AND ?");
	            params.add(filter.fromDate); params.add(filter.toDate);
	        }
	    }
	    // Áp dụng bộ lọc Nhân viên/Ca nếu có
	    applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
	
	    try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
	        for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
	        ResultSet rs = ps.executeQuery();
	        while (rs.next()) result.add(new Object[] { rs.getString("id"), "", rs.getString("ghiChu") });
	    } catch (Exception e) { e.printStackTrace(); }
	    return result;
	}
	
    public List<Object[]> getRawHD12ThangChuan(int year, BUS.BUS_ThongKe.ThongKeFilter filter) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT MONTH(hd.ngayLapHD) AS thang, hd.id, hd.ghiChu "
                + "FROM HoaDon hd "
                + "WHERE YEAR(hd.ngayLapHD) = ? AND hd.loaiHD = 'BAN_HANG'");
        List<Object> params = new ArrayList<>();
        params.add(year);
        
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");

        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(new Object[] { rs.getInt("thang"), rs.getString("id"), rs.getString("ghiChu") });
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return result;
    }
}