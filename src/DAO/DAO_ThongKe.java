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
            
        // 1. Lọc theo nhân viên
        if (filter.maNV != null && !filter.maNV.isEmpty()) {
            sql.append(" AND ").append(nvCol).append(" = ?");
            params.add(filter.maNV);
        }

        // 2. Lọc theo thời gian bắt đầu hoặc Ca làm việc
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

        // ----------------------------------------------------
        // ĐÂY LÀ ĐOẠN MÌNH THÊM LỌC NĂM VÀO NÈ CẬU:
        // ----------------------------------------------------
        if (filter.year != null) {
            sql.append(" AND YEAR(").append(dateCol).append(") = ?");
            params.add(filter.year);
        }
        // ----------------------------------------------------

        // 3. Lọc theo Tháng / Quý / Tùy chỉnh
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

    public double tinhDoanhThu(LocalDateTime tuNgay, LocalDateTime denNgay) {
        double doanhThu = 0;
        // BUG FIX: Tính đúng cả 3 loại hóa đơn:
        //   BAN_HANG + DOI_HANG (xuất mới) cộng vào doanh thu
        //   TRA_HANG + DOI_HANG (nhận lại) trừ khỏi doanh thu
        String sql = "SELECT hd.id, hd.ghiChu, hd.loaiHD, "
                + "  SUM(CASE "
                + "    WHEN hd.loaiHD = 'BAN_HANG' OR (hd.loaiHD = 'DOI_HANG' AND ct.soLuong > 0) "
                + "    THEN ROUND(ABS(ct.thanhTien) / (1.0 + ISNULL(sp.thueVAT, 0)/100.0), 0) "
                + "    WHEN hd.loaiHD = 'TRA_HANG' OR (hd.loaiHD = 'DOI_HANG' AND ct.soLuong < 0) "
                + "    THEN -ROUND(ABS(ct.thanhTien) / (1.0 + ISNULL(sp.thueVAT, 0)/100.0), 0) "
                + "    ELSE 0 "
                + "  END) AS dtThuanNet "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
                + "WHERE hd.ngayLapHD BETWEEN ? AND ? "
                + "  AND hd.loaiHD IN ('BAN_HANG', 'TRA_HANG', 'DOI_HANG') "
                + "GROUP BY hd.id, hd.ghiChu, hd.loaiHD";
        try (PreparedStatement pst = getConn().prepareStatement(sql)) {
            pst.setTimestamp(1, Timestamp.valueOf(tuNgay));
            pst.setTimestamp(2, Timestamp.valueOf(denNgay));
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    double dtNet = rs.getDouble("dtThuanNet");
                    // Chỉ trừ điểm thưởng cho hóa đơn BAN_HANG (KM% đã baked vào ct.thanhTien)
                    double tienDiemTru = 0;
                    if ("BAN_HANG".equals(rs.getString("loaiHD"))) {
                        tienDiemTru = tinhTienDiemTruInline(rs.getString("ghiChu"));
                    }
                    doanhThu += dtNet - tienDiemTru;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doanhThu;
    }

    private double tinhTienDiemTruInline(String ghiChu) {
        double tienDiemTru = 0;
        if (ghiChu == null || ghiChu.isEmpty()) return 0;
        String[] parts = ghiChu.split("\\|");
        for (String p : parts) {
            p = p.trim();
            if (p.startsWith("Dùng điểm: -") || p.contains("KM_GIAM:")) {
                try {
                    tienDiemTru += Long.parseLong(p.replaceAll("[^0-9]", ""));
                } catch (Exception ignored) {}
            }
        }
        return tienDiemTru;
    }

    public double tinhLoiNhuan(LocalDateTime tuNgay, LocalDateTime denNgay) {
        // BUG FIX: Giá vốn (COGS) phải tính từ PhanBoLoHang (giá vốn hàng thực tế xuất bán),
        // không phải từ LoHang.ngayNhap (có thể nhập trước/sau kỳ báo cáo).
        double dt = tinhDoanhThu(tuNgay, denNgay);
        double cp = 0;
        // COGS = giá vốn xuất kho (BAN_HANG + DOI_HANG xuất mới)
        //      - giá vốn hàng nhận lại kho (TRA_HANG + DOI_HANG nhận lại)
        String sql =
            "SELECT " +
            "  ISNULL(SUM(CASE " +
            "    WHEN hd.loaiHD = 'BAN_HANG' OR (hd.loaiHD = 'DOI_HANG' AND ct.soLuong > 0) " +
            "    THEN ISNULL(pbl_cost.giaVon, 0) ELSE 0 " +
            "  END), 0) AS giaVonBan, " +
            "  ISNULL(SUM(CASE " +
            "    WHEN hd.loaiHD = 'TRA_HANG' OR (hd.loaiHD = 'DOI_HANG' AND ct.soLuong < 0) " +
            "    THEN ISNULL(pbl_cost.giaVon, 0) ELSE 0 " +
            "  END), 0) AS giaVonHoan " +
            "FROM HoaDon hd " +
            "JOIN ChiTietHoaDon ct ON ct.hoaDonId = hd.id " +
            "LEFT JOIN ( " +
            "    SELECT pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId, " +
            "           SUM(pbl.soLuong * lh.gia) AS giaVon " +
            "    FROM PhanBoLoHang pbl JOIN LoHang lh ON lh.id = pbl.loHangId " +
            "    GROUP BY pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId " +
            ") pbl_cost ON pbl_cost.hoaDonId = ct.hoaDonId " +
            "          AND pbl_cost.sanPhamId = ct.sanPhamId " +
            "          AND pbl_cost.donViDoLuongId = ct.donViDoLuongId " +
            "WHERE hd.ngayLapHD BETWEEN ? AND ? " +
            "  AND hd.loaiHD IN ('BAN_HANG', 'TRA_HANG', 'DOI_HANG')";
        try (PreparedStatement pst = getConn().prepareStatement(sql)) {
            pst.setTimestamp(1, Timestamp.valueOf(tuNgay));
            pst.setTimestamp(2, Timestamp.valueOf(denNgay));
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    double giaVonBan  = rs.getDouble("giaVonBan");
                    double giaVonHoan = rs.getDouble("giaVonHoan");
                    cp = giaVonBan - giaVonHoan;
                }
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

    public String getPTTT(String hdId) {
        String sql = "SELECT phuongThucThanhToan FROM HoaDon WHERE id=?";
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
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
                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
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

    public List<Object[]> getRawHDGioTrongNgay(String dateYMD, BUS.BUS_ThongKe.ThongKeFilter filter) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DATEPART(HOUR, hd.ngayLapHD) AS h, "
                // SUM(ABS(ct.thanhTien)) = tiền thực thu có VAT, sau KM baked-in
                + "SUM(ABS(ct.thanhTien)) AS tienThucThu "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
                + "WHERE hd.loaiHD = 'BAN_HANG' AND CAST(hd.ngayLapHD AS DATE) = ? ");
        List<Object> params = new ArrayList<>();
        params.add(dateYMD);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY DATEPART(HOUR, hd.ngayLapHD)");

        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.add(new Object[]{rs.getInt("h"), rs.getDouble("tienThucThu")});
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
                + "ISNULL(SUM(ABS(ct.thanhTien)), 0) AS tongGocCoVAT, "
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
                + "SUM(CASE "
                + "         WHEN hd.loaiHD = 'TRA_HANG' THEN -ABS(ct.thanhTien) "
                + "         WHEN hd.loaiHD = 'DOI_HANG' AND ct.soLuong > 0  THEN  ABS(ct.thanhTien) "
                + "         WHEN hd.loaiHD = 'DOI_HANG' AND ct.soLuong < 0  THEN -ABS(ct.thanhTien) "
                + "         ELSE 0 END) AS netRefund_coVAT "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
                + "WHERE " + dateCondition
                + " AND hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') ");
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY hd.id, hd.loaiHD, hd.ghiChu, DATEPART(HOUR, hd.ngayLapHD)");

        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            	result.add(new Object[] { rs.getString("loaiHD"), rs.getString("ghiChu"), rs.getInt("h"), rs.getDouble("netRefund_coVAT") });
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
        double[] data = new double[12];
        
        // A. Tính tổng giá vốn bán hàng
        StringBuilder sqlBan = new StringBuilder(
            "SELECT MONTH(hd.ngayLapHD) as m, SUM(ISNULL(pbl_cost.giaVon, 0)) as cp " +
            "FROM HoaDon hd " +
            "JOIN ChiTietHoaDon ct ON ct.hoaDonId = hd.id " +
            "JOIN (SELECT pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId, SUM(pbl.soLuong * lh.gia) as giaVon " +
            "      FROM PhanBoLoHang pbl JOIN LoHang lh ON lh.id = pbl.loHangId GROUP BY pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId) pbl_cost " +
            "ON pbl_cost.hoaDonId = ct.hoaDonId AND pbl_cost.sanPhamId = ct.sanPhamId AND pbl_cost.donViDoLuongId = ct.donViDoLuongId " +
            "WHERE YEAR(hd.ngayLapHD) = ? AND hd.loaiHD = 'BAN_HANG' "
        );
        List<Object> paramsBan = new ArrayList<>();
        paramsBan.add(year);
        applyFilter(filter, sqlBan, paramsBan, "hd.ngayLapHD", "hd.nhanVienId");
        sqlBan.append(" GROUP BY MONTH(hd.ngayLapHD)");
        
        try (PreparedStatement ps = getConn().prepareStatement(sqlBan.toString())) {
            for (int i=0; i<paramsBan.size(); i++) ps.setObject(i+1, paramsBan.get(i));
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                int m = rs.getInt("m");
                if (m >= 1 && m <= 12) data[m-1] += rs.getDouble("cp") / 1_000_000.0;
            }
        } catch (Exception e) { e.printStackTrace(); }

        // B. Trừ đi giá vốn của hàng Đổi/Trả
        StringBuilder sqlTra = new StringBuilder(
            "SELECT MONTH(hd.ngayLapHD) as m, SUM(ISNULL(pbl_cost.giaVon, 0)) as cp " +
            "FROM HoaDon hd " +
            "JOIN ChiTietHoaDon ct ON ct.hoaDonId = hd.id " +
            "JOIN (SELECT pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId, SUM(pbl.soLuong * lh.gia) as giaVon " +
            "      FROM PhanBoLoHang pbl JOIN LoHang lh ON lh.id = pbl.loHangId GROUP BY pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId) pbl_cost " +
            "ON pbl_cost.hoaDonId = ct.hoaDonId AND pbl_cost.sanPhamId = ct.sanPhamId AND pbl_cost.donViDoLuongId = ct.donViDoLuongId " +
            "WHERE YEAR(hd.ngayLapHD) = ? AND hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') AND hd.ghiChu LIKE N'%Hoàn thành%' " +
            "AND (hd.loaiHD = 'TRA_HANG' OR ct.soLuong < 0) "
        );
        List<Object> paramsTra = new ArrayList<>();
        paramsTra.add(year);
        applyFilter(filter, sqlTra, paramsTra, "hd.ngayLapHD", "hd.nhanVienId");
        sqlTra.append(" GROUP BY MONTH(hd.ngayLapHD)");

        try (PreparedStatement ps = getConn().prepareStatement(sqlTra.toString())) {
            for (int i=0; i<paramsTra.size(); i++) ps.setObject(i+1, paramsTra.get(i));
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                int m = rs.getInt("m");
                if (m >= 1 && m <= 12) data[m-1] -= rs.getDouble("cp") / 1_000_000.0;
            }
        } catch (Exception e) { e.printStackTrace(); }

        for (int i = 0; i < 12; i++) data[i] = Math.max(0, data[i]);
        return data;
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
        StringBuilder sql = new StringBuilder("SELECT sp.ten, sp.danhMuc, "
                + "SUM(CASE WHEN hd.loaiHD = 'BAN_HANG' THEN ct.soLuong ELSE -ABS(ct.soLuong) END) AS sl, "
                + "SUM(CASE WHEN hd.loaiHD = 'BAN_HANG' "
                + "THEN ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0) "
                + "ELSE -ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0) END) AS dtThuan "
                + "FROM ChiTietHoaDon ct "
                + "JOIN HoaDon hd ON hd.id = ct.hoaDonId "
                + "JOIN SanPham sp ON sp.id = ct.sanPhamId "
                + "JOIN DonViDoLuong dvl ON dvl.sanPhamId = ct.sanPhamId AND dvl.id = ct.donViDoLuongId "
                + "WHERE YEAR(hd.ngayLapHD) = ? "
                + "AND (hd.loaiHD = 'BAN_HANG' OR (hd.loaiHD IN ('TRA_HANG','DOI_HANG') AND hd.ghiChu LIKE N'%Hoàn thành%')) ");
        List<Object> params = new ArrayList<>();
        params.add(year);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY sp.ten, sp.danhMuc"
                + " HAVING SUM(CASE WHEN hd.loaiHD = 'BAN_HANG'"
                + " THEN ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)"
                + " ELSE -ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0) END) > 0"
                + " ORDER BY dtThuan DESC");

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
        // Pủn Fix: Dùng CASE WHEN tương tự Top Sản Phẩm để bóc tách VAT bị trừ
        StringBuilder sql = new StringBuilder("SELECT sp.id AS spId, sp.ten, sp.danhMuc, ISNULL(sp.thueVAT, 0) AS vatPct, "
                + "SUM(CASE WHEN hd.loaiHD = 'BAN_HANG' "
                + "THEN ROUND(ABS(ct.thanhTien) * (ISNULL(sp.thueVAT,0)/100.0) / (1 + ISNULL(sp.thueVAT,0)/100.0), 0) "
                + "ELSE -ROUND(ABS(ct.thanhTien) * (ISNULL(sp.thueVAT,0)/100.0) / (1 + ISNULL(sp.thueVAT,0)/100.0), 0) END) AS tienThue "
                + "FROM ChiTietHoaDon ct "
                + "JOIN HoaDon hd ON hd.id = ct.hoaDonId "
                + "JOIN SanPham sp ON sp.id = ct.sanPhamId "
                + "JOIN DonViDoLuong dvl ON dvl.sanPhamId = ct.sanPhamId AND dvl.id = ct.donViDoLuongId "
                + "WHERE YEAR(hd.ngayLapHD) = ? "
                + "AND (hd.loaiHD = 'BAN_HANG' OR (hd.loaiHD IN ('TRA_HANG','DOI_HANG') AND hd.ghiChu LIKE N'%Hoàn thành%')) ");
        List<Object> params = new ArrayList<>();
        params.add(year);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY sp.id, sp.ten, sp.danhMuc, ISNULL(sp.thueVAT, 0)"
                + " HAVING SUM(CASE WHEN hd.loaiHD = 'BAN_HANG'"
                + " THEN ROUND(ABS(ct.thanhTien) * (ISNULL(sp.thueVAT,0)/100.0) / (1 + ISNULL(sp.thueVAT,0)/100.0), 0)"
                + " ELSE -ROUND(ABS(ct.thanhTien) * (ISNULL(sp.thueVAT,0)/100.0) / (1 + ISNULL(sp.thueVAT,0)/100.0), 0) END) > 0"
                + " ORDER BY tienThue DESC");

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
        // Dùng ct.thanhTien (đã baked KM) và bóc VAT đúng từng sản phẩm
        String sql = "SELECT CONVERT(NVARCHAR,CAST(hd.ngayLapHD AS DATE),103) AS d, "
                + "hd.id AS hdId, hd.ghiChu, "
                + "ISNULL(SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)),0) AS doanhThuThuan "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON ct.hoaDonId=hd.id "
                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
                + "WHERE hd.loaiHD='BAN_HANG' "
                + "AND CAST(hd.ngayLapHD AS DATE) BETWEEN ? AND DATEADD(DAY,6,CAST(? AS DATE)) "
                + "GROUP BY CAST(hd.ngayLapHD AS DATE), CONVERT(NVARCHAR,CAST(hd.ngayLapHD AS DATE),103), hd.id, hd.ghiChu "
                + "ORDER BY CAST(hd.ngayLapHD AS DATE), hd.id";

        Map<String, double[]> dayMap = new java.util.LinkedHashMap<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, weekStartYMD);
            ps.setString(2, weekStartYMD);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String d = rs.getString("d");
                double dtThuan = rs.getDouble("doanhThuThuan");
                // Chỉ trừ điểm thưởng — KHÔNG gọi tinhTongTienGiamInline (double KM)
                double tienDiemTru = tinhTienDiemTruInline(rs.getString("ghiChu"));
                double dt = Math.max(0, dtThuan - tienDiemTru);
                double[] cur = dayMap.computeIfAbsent(d, k -> new double[]{0, 0});
                cur[0]++;       // soHD
                cur[1] += dt;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dayMap.forEach((d, v) -> result.add(new Object[]{d, (int) v[0], v[1] / 1_000_000.0}));
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
        	// Bước 1: tính per-HĐ doanh thu thuần, gom theo khachHangId
        	Map<String, double[]> purchaseMap = new HashMap<>();
        	String sqlP = "SELECT kh.id AS khId, hd.id AS hdId, hd.ghiChu, "
        	        + "ISNULL(SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)),0) AS doanhThuThuan "
        	        + "FROM HoaDon hd "
        	        + "JOIN KhachHang kh ON kh.id=hd.khachHangId "
        	        + "LEFT JOIN ChiTietHoaDon ct ON ct.hoaDonId=hd.id "
        	        + "LEFT JOIN SanPham sp ON sp.id=ct.sanPhamId "
        	        + "WHERE hd.loaiHD='BAN_HANG' "
        	        + "GROUP BY kh.id, hd.id, hd.ghiChu";
        	try (PreparedStatement ps = con.prepareStatement(sqlP); ResultSet rs = ps.executeQuery()) {
        	    while (rs.next()) {
        	        String khId = rs.getString("khId");
        	        double dtThuan = rs.getDouble("doanhThuThuan");
        	        // Chỉ trừ điểm thưởng — KM% đã baked vào ct.thanhTien
        	        double tienDiemTru = tinhTienDiemTruInline(rs.getString("ghiChu"));
        	        double dt = Math.max(0, dtThuan - tienDiemTru);
        	        double[] cur = purchaseMap.computeIfAbsent(khId, k -> new double[]{0, 0});
        	        cur[0]++; // soHD
        	        cur[1] += dt;
        	    }
        	}
        	// Bước 2: giữ nguyên — query top KH theo điểm rồi merge
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

    /**
     * Tổng số lượng sản phẩm thực tế trong ngày, đã tính đổi/trả:
     *   + BAN_HANG                    : SUM(ct.soLuong)        — hàng bán ra
     *   + DOI_HANG hoàn thành, soLuong > 0 : SUM(ct.soLuong)  — SP xuất cho khách đổi
     *   - TRA_HANG hoàn thành         : SUM(ABS(ct.soLuong))   — hàng khách trả lại
     *   - DOI_HANG hoàn thành, soLuong < 0 : SUM(ABS(ct.soLuong)) — hàng bị lấy lại khi đổi
     */
    public int getTongSoLuongSPHomNay(BUS.BUS_ThongKe.ThongKeFilter filter) {
        StringBuilder sql = new StringBuilder(
            "SELECT ISNULL(SUM(" +
            "  CASE " +
            "    WHEN hd.loaiHD = 'BAN_HANG' THEN ct.soLuong " +
            "    WHEN hd.loaiHD = 'DOI_HANG' AND ct.soLuong > 0 " +
            "         AND hd.ghiChu LIKE N'%Hoàn thành%' THEN ct.soLuong " +
            "    WHEN (hd.loaiHD = 'TRA_HANG' " +
            "          OR (hd.loaiHD = 'DOI_HANG' AND ct.soLuong < 0)) " +
            "         AND hd.ghiChu LIKE N'%Hoàn thành%' THEN ct.soLuong " + // soLuong < 0 nên trừ tự nhiên
            "    ELSE 0 " +
            "  END" +
            "), 0) AS tongSL " +
            "FROM HoaDon hd " +
            "JOIN ChiTietHoaDon ct ON ct.hoaDonId = hd.id " +
            "WHERE CAST(hd.ngayLapHD AS DATE) = CAST(GETDATE() AS DATE) " +
            "AND (" +
            "  hd.loaiHD = 'BAN_HANG' " +
            "  OR (hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') AND hd.ghiChu LIKE N'%Hoàn thành%')" +
            ")");
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Math.max(0, rs.getInt("tongSL"));
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
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM HoaDon hd WHERE hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') AND hd.ghiChu LIKE N'%Hoàn thành%'");
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
                + "ISNULL("
                + "    SUM(pc.tongGiaVon) / NULLIF(SUM(pc.tongSLCoBan), 0) * MAX(dvl.chuyenDoiDonViCoBan), "
                + "    MAX(lh2.gia) * MAX(dvl.chuyenDoiDonViCoBan) "
                + ") AS giaVon "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
                + "JOIN DonViDoLuong dvl ON ct.donViDoLuongId = dvl.id AND ct.sanPhamId = dvl.sanPhamId "
                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
                + "LEFT JOIN ("
                + "    SELECT pbl.sanPhamId, pbl.donViDoLuongId, pbl.hoaDonId, "
                + "           SUM(pbl.soLuong * lh.gia) AS tongGiaVon, "
                + "           SUM(pbl.soLuong)          AS tongSLCoBan "
                + "    FROM PhanBoLoHang pbl "
                + "    JOIN LoHang lh ON lh.id = pbl.loHangId "
                + "    GROUP BY pbl.sanPhamId, pbl.donViDoLuongId, pbl.hoaDonId"
                + ") pc ON pc.sanPhamId = ct.sanPhamId AND pc.donViDoLuongId = ct.donViDoLuongId AND pc.hoaDonId = ct.hoaDonId "
                + "LEFT JOIN LoHang lh2 ON lh2.sanPhamId = sp.id AND lh2.trangThai = 'CON_HANG' "
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
	                + "SUM(CASE WHEN ct.soLuong > 0 "
	                + "         THEN  ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0) / 100.0) "
	                + "         WHEN ct.soLuong < 0 "
	                + "         THEN -ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0) / 100.0) "
	                + "         ELSE 0 END) AS netRefund_chuaVAT "
	                + "FROM HoaDon hd "
	                + "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
	                + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
	                + "WHERE YEAR(hd.ngayLapHD) = ? "
	                + " AND hd.loaiHD = 'DOI_HANG' AND hd.ghiChu LIKE N'%Hoàn thành%'");
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
    
    public List<Object[]> getRawHDDoiTra30Ngay(BUS.BUS_ThongKe.ThongKeFilter filter) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT CONVERT(NVARCHAR,CAST(hd.ngayLapHD AS DATE),103) d, "
            + "SUM(CASE WHEN ct.soLuong > 0 "
            + "         THEN  ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0) / 100.0) "
            + "         WHEN ct.soLuong < 0 "
            + "         THEN -ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0) / 100.0) "
            + "         ELSE 0 END) AS netRefund_chuaVAT "
            + "FROM HoaDon hd "
            + "JOIN ChiTietHoaDon ct ON ct.hoaDonId=hd.id "
            + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
            + "WHERE hd.loaiHD = 'DOI_HANG' AND hd.ghiChu LIKE N'%Hoàn thành%' "
            + "AND CAST(hd.ngayLapHD AS DATE) >= DATEADD(DAY,-29,CAST(GETDATE() AS DATE))");
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY CAST(hd.ngayLapHD AS DATE), CONVERT(NVARCHAR,CAST(hd.ngayLapHD AS DATE),103)");
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(new Object[] { rs.getString("d"), rs.getDouble("netRefund_chuaVAT") });
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }
 
    public double[] getTienVaGiaVonHangTra(BUS.BUS_ThongKe.ThongKeFilter filter) {
        double[] kq = new double[]{0, 0, 0};
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                "SUM(ABS(ct.thanhTien)) as tienHoanCoVAT, " +
                "SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)) as tienHoanChuaVAT, " +
                "SUM(ISNULL(pbl_cost.giaVon, ISNULL(pbl_goc.donGiaVonGoc * ABS(ct.soLuong), 0))) as giaVonHoan " +
                "FROM HoaDon hd " +
                "JOIN ChiTietHoaDon ct ON ct.hoaDonId = hd.id " +
                "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                "LEFT JOIN (" +
                "    SELECT pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId, SUM(pbl.soLuong * lh.gia) as giaVon " +
                "    FROM PhanBoLoHang pbl " +
                "    JOIN LoHang lh ON lh.id = pbl.loHangId " +
                "    GROUP BY pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId" +
                ") pbl_cost ON pbl_cost.hoaDonId = ct.hoaDonId AND pbl_cost.sanPhamId = ct.sanPhamId AND pbl_cost.donViDoLuongId = ct.donViDoLuongId " +
                "LEFT JOIN (" +
                "    SELECT pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId, " +
                "           (SUM(pbl.soLuong * lh.gia) / NULLIF(SUM(pbl.soLuong), 0)) AS donGiaVonGoc " +
                "    FROM PhanBoLoHang pbl " +
                "    JOIN LoHang lh ON lh.id = pbl.loHangId " +
                "    GROUP BY pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId " +
                ") pbl_goc ON pbl_goc.hoaDonId = hd.hoaDonGocId AND pbl_goc.sanPhamId = ct.sanPhamId AND pbl_goc.donViDoLuongId = ct.donViDoLuongId " +
                "WHERE hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') AND hd.ghiChu LIKE N'%Hoàn thành%' " +
                "AND (hd.loaiHD = 'TRA_HANG' OR ct.soLuong < 0)"
            );
        
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");

        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                kq[0] = rs.getDouble("tienHoanCoVAT");
                kq[1] = rs.getDouble("tienHoanChuaVAT");
                kq[2] = rs.getDouble("giaVonHoan");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return kq;
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
                        if (!s.isEmpty()) tongHoanTra += Double.parseDouble(s);
                    } else if ("DOI_HANG".equals(loai) && parts.length >= 4) {
                        String chenhLech = parts[3].trim();
                        if (chenhLech.contains("Hoàn")) {
                            String s = chenhLech.replaceAll("[^0-9]", "");
                            if (!s.isEmpty()) tongHoanTra += Double.parseDouble(s);
                        }
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return tongHoanTra;
    }
    
    public double[] getThongKeTraHangNV(String nvId, int year, BUS.BUS_ThongKe.ThongKeFilter filter) {
        double[] res = {0, 0};
        // soHD: đếm riêng qua subquery (không lọc theo ct.soLuong để tránh bỏ sót / đếm nhầm)
        // tienHoan: chỉ SUM dòng TRA_HANG (toàn bộ) + dòng DOI_HANG có soLuong < 0 (hàng bị lấy lại)
        StringBuilder sql = new StringBuilder(
            "SELECT " +
            "  (SELECT COUNT(DISTINCT hd2.id) FROM HoaDon hd2 " +
            "   WHERE hd2.nhanVienId = ? AND YEAR(hd2.ngayLapHD) = ? " +
            "   AND hd2.loaiHD IN ('TRA_HANG', 'DOI_HANG') " +
            "   AND hd2.ghiChu LIKE N'%Hoàn thành%') AS soHD, " +
            "SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)) as tienHoan " +
            "FROM HoaDon hd " +
            "JOIN ChiTietHoaDon ct ON ct.hoaDonId = hd.id " +
            "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
            "WHERE hd.nhanVienId = ? AND YEAR(hd.ngayLapHD) = ? " +
            "AND hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') AND hd.ghiChu LIKE N'%Hoàn thành%' " +
            "AND (hd.loaiHD = 'TRA_HANG' OR (hd.loaiHD = 'DOI_HANG' AND ct.soLuong < 0))"
        );
        // Thứ tự params: [nvId(subquery), year(subquery), nvId(main), year(main), ...filterParams]
        List<Object> params = new ArrayList<>();
        params.add(nvId); params.add(year); // cho subquery soHD
        params.add(nvId); params.add(year); // cho main query tienHoan
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        
        try (PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
            for (int i=0; i<params.size(); i++) ps.setObject(i+1, params.get(i));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                res[0] = rs.getInt("soHD");
                res[1] = rs.getDouble("tienHoan");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return res;
    }

    // =====================================================================
    // KẾT CA — TIỀN ĐỔI HÀNG
    // =====================================================================

    /**
     * Trả về double[2] tổng tiền đổi hàng trong ca (mọi phương thức thanh toán):
     *   [0] = tổng tiền khách bù thêm  (ct.soLuong > 0, chưa VAT)
     *   [1] = tổng tiền tiệm hoàn lại  (ct.soLuong < 0, chưa VAT)
     *
     * @param maNV  ID nhân viên phụ trách ca
     * @param start Thời điểm bắt đầu ca (ngayLapHD >= start)
     * @return double[2]; nếu không có dữ liệu trả về {0.0, 0.0}
     */
    public double[] getTienDoiHangTheoCa(String maNV, LocalDateTime start) {
        double[] kq = new double[]{0.0, 0.0};
        String sql =
            "SELECT " +
            "  SUM(CASE WHEN ct.soLuong > 0 " +
            "           THEN ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0) / 100.0) " +
            "           ELSE 0 END) AS tienBuThem, " +
            "  SUM(CASE WHEN ct.soLuong < 0 " +
            "           THEN ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0) / 100.0) " +
            "           ELSE 0 END) AS tienHoanLai " +
            "FROM HoaDon hd " +
            "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId " +
            "JOIN SanPham sp       ON ct.sanPhamId = sp.id " +
            "WHERE hd.nhanVienId = ? " +
            "  AND hd.ngayLapHD >= ? " +
            "  AND hd.loaiHD = 'DOI_HANG' " +
            "  AND hd.ghiChu LIKE N'Hoàn thành%'";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    kq[0] = rs.getDouble("tienBuThem");
                    kq[1] = rs.getDouble("tienHoanLai");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kq;
    }

    /**
     * Giống {@link #getTienDoiHangTheoCa} nhưng chỉ tính hóa đơn thanh toán TIEN_MAT.
     * Dùng để đối soát tiền mặt thực tế trong quỹ khi kết ca.
     *
     * @param maNV  ID nhân viên phụ trách ca
     * @param start Thời điểm bắt đầu ca (ngayLapHD >= start)
     * @return double[2]; nếu không có dữ liệu trả về {0.0, 0.0}
     */
    public double[] getTienDoiHangTheoCaMat(String maNV, LocalDateTime start) {
        double[] kq = new double[]{0.0, 0.0};
        String sql =
            "SELECT " +
            "  SUM(CASE WHEN ct.soLuong > 0 " +
            "           THEN ABS(ct.thanhTien) " +
            "           ELSE 0 END) AS tienBuThem, " +
            "  SUM(CASE WHEN ct.soLuong < 0 " +
            "           THEN ABS(ct.thanhTien) " +
            "           ELSE 0 END) AS tienHoanLai " +
            "FROM HoaDon hd " +
            "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId " +
            "JOIN SanPham sp       ON ct.sanPhamId = sp.id " +
            "WHERE hd.nhanVienId = ? " +
            "  AND hd.ngayLapHD >= ? " +
            "  AND hd.loaiHD = 'DOI_HANG' " +
            "  AND hd.ghiChu LIKE N'Hoàn thành%' " +
            "  AND hd.phuongThucThanhToan = 'TIEN_MAT'";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    kq[0] = rs.getDouble("tienBuThem");
                    kq[1] = rs.getDouble("tienHoanLai");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kq;
    }
    
     public List<Object[]> getBaoCaoTaiChinh(
             BUS.BUS_ThongKe.ThongKeFilter filter, String groupBy) {
         final String groupExpr;
         if ("THANG".equalsIgnoreCase(groupBy)) {
             groupExpr = "CAST(YEAR(hd.ngayLapHD) AS NVARCHAR(4)) + N'-' "
                       + "+ RIGHT(N'0' + CAST(MONTH(hd.ngayLapHD) AS NVARCHAR(2)), 2)";
         } else {
             groupExpr = "CONVERT(NVARCHAR(10), CAST(hd.ngayLapHD AS DATE), 103)";
         }
         StringBuilder filterForRevenue = new StringBuilder();
         List<Object>  paramsRevenue    = new ArrayList<>();
         applyFilter(filter, filterForRevenue, paramsRevenue, "hd.ngayLapHD", "hd.nhanVienId");

         StringBuilder filterForCogs = new StringBuilder();
         List<Object>  paramsCogs    = new ArrayList<>();
         applyFilter(filter, filterForCogs, paramsCogs, "hd.ngayLapHD", "hd.nhanVienId");

         String sql =
                 "WITH CTE_Revenue AS (\n"
               + "  SELECT\n"
               + "    " + groupExpr + " AS tg,\n"
               + "    SUM(CASE\n"
               + "          WHEN (hd.loaiHD = 'BAN_HANG' AND ISNULL(ct.ghiChu, '') != 'TRA_LAI')\n"
               + "            OR (hd.loaiHD = 'DOI_HANG' AND ct.soLuong > 0)\n"
               + "          THEN ABS(ct.thanhTien) ELSE 0\n"
               + "        END) AS doanhThuGop,\n"
               + "    SUM(CASE\n"
               + "          WHEN (hd.loaiHD = 'BAN_HANG' AND ISNULL(ct.ghiChu, '') != 'TRA_LAI')\n"
               + "            OR (hd.loaiHD = 'DOI_HANG' AND ct.soLuong > 0)\n"
               + "          THEN ROUND(ABS(ct.thanhTien) * (ISNULL(sp.thueVAT, 0) / 100.0) / (1.0 + ISNULL(sp.thueVAT, 0) / 100.0), 0)\n"
               + "          WHEN (hd.loaiHD = 'TRA_HANG' OR ct.ghiChu = 'TRA_LAI' OR (hd.loaiHD = 'DOI_HANG' AND ct.soLuong < 0))\n"
               + "            AND hd.ghiChu LIKE N'%Hoàn thành%'\n"
               + "          THEN -ROUND(ABS(ct.thanhTien) * (ISNULL(sp.thueVAT, 0) / 100.0) / (1.0 + ISNULL(sp.thueVAT, 0) / 100.0), 0)\n"
               + "          ELSE 0\n"
               + "        END) AS thueVAT,\n"
               + "    SUM(CASE\n"
               + "          WHEN (   hd.loaiHD = 'TRA_HANG'\n"
               + "                OR ct.ghiChu = 'TRA_LAI'\n"
               + "                OR (hd.loaiHD = 'DOI_HANG' AND ct.soLuong < 0))\n"
               + "            AND hd.ghiChu LIKE N'%Hoàn thành%'\n"
               + "          THEN ABS(ct.thanhTien) ELSE 0\n"
               + "        END) AS hangBanBiTraLai\n"
               + "  FROM HoaDon hd\n"
               + "  JOIN ChiTietHoaDon ct ON ct.hoaDonId = hd.id\n"
               + "  JOIN SanPham sp        ON sp.id = ct.sanPhamId\n"
               + "  WHERE hd.loaiHD IN ('BAN_HANG', 'TRA_HANG', 'DOI_HANG')\n"
               + filterForRevenue.toString() + "\n"
               + "  GROUP BY " + groupExpr + "\n"
               + "),\n"
               + "CTE_COGS AS (\n"
               + "  SELECT\n"
               + "    " + groupExpr + " AS tg,\n"
               + "    SUM(CASE\n"
               + "          WHEN hd.loaiHD = 'BAN_HANG'\n"
               + "            OR (hd.loaiHD = 'DOI_HANG' AND ct.soLuong > 0)\n"
               + "          THEN ISNULL(pbl_cost.giaVon, 0) ELSE 0\n"
               + "        END) AS giaVonBan,\n"
               + "    SUM(CASE\n"
               + "          WHEN (   hd.loaiHD = 'TRA_HANG'\n"
               + "                OR ct.ghiChu = 'TRA_LAI'\n"
               + "                OR (hd.loaiHD = 'DOI_HANG' AND ct.soLuong < 0))\n"
               + "            AND hd.ghiChu LIKE N'%Hoàn thành%'\n"
               + "          THEN ISNULL(pbl_cost.giaVon, ISNULL(pbl_goc.donGiaVonGoc * ABS(ct.soLuong), 0)) ELSE 0\n"
               + "        END) AS giaVonHoan\n"
               + "  FROM HoaDon hd\n"
               + "  JOIN ChiTietHoaDon ct ON ct.hoaDonId = hd.id\n"
               + "  LEFT JOIN (\n"
               + "      SELECT pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId,\n"
               + "             SUM(pbl.soLuong * lh.gia) AS giaVon\n"
               + "      FROM PhanBoLoHang pbl\n"
               + "      JOIN LoHang lh ON lh.id = pbl.loHangId\n"
               + "      GROUP BY pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId\n"
               + "  ) pbl_cost ON pbl_cost.hoaDonId       = ct.hoaDonId\n"
               + "            AND pbl_cost.sanPhamId       = ct.sanPhamId\n"
               + "            AND pbl_cost.donViDoLuongId  = ct.donViDoLuongId\n"
               + "  LEFT JOIN (\n"
               + "      SELECT pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId,\n"
               + "             (SUM(pbl.soLuong * lh.gia) / NULLIF(SUM(pbl.soLuong), 0)) AS donGiaVonGoc\n"
               + "      FROM PhanBoLoHang pbl\n"
               + "      JOIN LoHang lh ON lh.id = pbl.loHangId\n"
               + "      GROUP BY pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId\n"
               + "  ) pbl_goc ON pbl_goc.hoaDonId       = hd.hoaDonGocId\n"
               + "           AND pbl_goc.sanPhamId      = ct.sanPhamId\n"
               + "           AND pbl_goc.donViDoLuongId = ct.donViDoLuongId\n"
               + "  WHERE hd.loaiHD IN ('BAN_HANG', 'TRA_HANG', 'DOI_HANG')\n"
               + filterForCogs.toString() + "\n"
               + "  GROUP BY " + groupExpr + "\n"
               + ")\n"
               + "SELECT\n"
               + "  ISNULL(r.tg, c.tg)              AS thoiGian,\n"
               + "  ISNULL(r.doanhThuGop,     0)    AS doanhThuGop,\n"
               + "  ISNULL(r.thueVAT,         0)    AS thueVAT,\n"
               + "  ISNULL(r.hangBanBiTraLai, 0)    AS hangBanBiTraLai,\n"
               + "  ISNULL(c.giaVonBan,       0)    AS giaVonBan,\n"
               + "  ISNULL(c.giaVonHoan,      0)    AS giaVonHoan\n"
               + "FROM CTE_Revenue r\n"
               + "FULL OUTER JOIN CTE_COGS c ON c.tg = r.tg\n"
               + "ORDER BY ISNULL(r.tg, c.tg) ASC";
         List<Object> allParams = new ArrayList<>(paramsRevenue);
         allParams.addAll(paramsCogs);
         List<Object[]> result = new ArrayList<>();
         try (PreparedStatement ps = getConn().prepareStatement(sql)) {
             for (int i = 0; i < allParams.size(); i++) {
                 ps.setObject(i + 1, allParams.get(i));
             }
             try (ResultSet rs = ps.executeQuery()) {
            	 while (rs.next()) {
                     result.add(new Object[]{
                         rs.getString("thoiGian"),
                         rs.getDouble("doanhThuGop"),
                         rs.getDouble("thueVAT"),
                         rs.getDouble("hangBanBiTraLai"),
                         rs.getDouble("giaVonBan"),
                         rs.getDouble("giaVonHoan")
                     });
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return result;
     }

     public double[] getCogsTheoFilter(BUS.BUS_ThongKe.ThongKeFilter filter) {
         double[] kq = {0.0, 0.0};

         StringBuilder filterSb = new StringBuilder();
         List<Object>  params   = new ArrayList<>();
         applyFilter(filter, filterSb, params, "hd.ngayLapHD", "hd.nhanVienId");
         if (filter != null && "THANG".equals(filter.modeLocThoiGian) && filter.month != null) {
         }

         String sql =
                 "SELECT\n"
               + "  SUM(CASE\n"
               + "        WHEN hd.loaiHD = 'BAN_HANG'\n"
               + "          OR (hd.loaiHD = 'DOI_HANG' AND ct.soLuong > 0)\n"
               + "        THEN ISNULL(pbl_cost.giaVon, 0) ELSE 0\n"
               + "      END) AS giaVonBan,\n"
               + "  SUM(CASE\n"
               + "        WHEN (   hd.loaiHD = 'TRA_HANG'\n"
               + "              OR (hd.loaiHD = 'DOI_HANG' AND ct.soLuong < 0))\n"
               + "          AND hd.ghiChu LIKE N'%Hoàn thành%'\n"
               + "        THEN ISNULL(pbl_cost.giaVon, ISNULL(pbl_goc.donGiaVonGoc * ABS(ct.soLuong), 0)) ELSE 0\n"
               + "      END) AS giaVonHoan\n"
               + "FROM HoaDon hd\n"
               + "JOIN ChiTietHoaDon ct ON ct.hoaDonId = hd.id\n"
               + "LEFT JOIN (\n"
               + "    SELECT pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId,\n"
               + "           SUM(pbl.soLuong * lh.gia) AS giaVon\n"
               + "    FROM PhanBoLoHang pbl\n"
               + "    JOIN LoHang lh ON lh.id = pbl.loHangId\n"
               + "    GROUP BY pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId\n"
               + ") pbl_cost ON pbl_cost.hoaDonId      = ct.hoaDonId\n"
               + "          AND pbl_cost.sanPhamId      = ct.sanPhamId\n"
               + "          AND pbl_cost.donViDoLuongId = ct.donViDoLuongId\n"
               + "LEFT JOIN (\n"
               + "    SELECT pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId,\n"
               + "           (SUM(pbl.soLuong * lh.gia) / NULLIF(SUM(pbl.soLuong), 0)) AS donGiaVonGoc\n"
               + "    FROM PhanBoLoHang pbl\n"
               + "    JOIN LoHang lh ON lh.id = pbl.loHangId\n"
               + "    GROUP BY pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId\n"
               + ") pbl_goc ON pbl_goc.hoaDonId       = hd.hoaDonGocId\n"
               + "         AND pbl_goc.sanPhamId      = ct.sanPhamId\n"
               + "         AND pbl_goc.donViDoLuongId = ct.donViDoLuongId\n"
               + "WHERE hd.loaiHD IN ('BAN_HANG', 'TRA_HANG', 'DOI_HANG')\n"
               + filterSb.toString();

         try (PreparedStatement ps = getConn().prepareStatement(sql)) {
             for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
             try (ResultSet rs = ps.executeQuery()) {
                 if (rs.next()) {
                     kq[0] = rs.getDouble("giaVonBan");
                     kq[1] = rs.getDouble("giaVonHoan");
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return kq;
     }
     
     public BUS.BUS_KetQuaDoiChieuCa layDoiChieuDoanhThuTheoCa(BUS.BUS_ThongKe.ThongKeFilter filter) {
    	    BUS.BUS_KetQuaDoiChieuCa kq = new BUS.BUS_KetQuaDoiChieuCa();
    	    StringBuilder sql = new StringBuilder(
    	        "SELECT "
    	        // --- ĐẾM SỐ HÓA ĐƠN ---
    	        + "COUNT(DISTINCT CASE WHEN hd.loaiHD = 'BAN_HANG' THEN hd.id END) AS soHdBan, "
    	        + "COUNT(DISTINCT CASE WHEN hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') AND hd.ghiChu LIKE N'%Hoàn thành%' THEN hd.id END) AS soHdTra, "
    	        
    	        // --- NHÓM A: BÁN HÀNG ---
    	        + "ISNULL(SUM(CASE WHEN hd.loaiHD = 'BAN_HANG' THEN dvl.gia * ABS(ct.soLuong) ELSE 0 END), 0) AS a_giaGoc, "
    	        + "ISNULL(SUM(CASE WHEN hd.loaiHD = 'BAN_HANG' THEN (dvl.gia * ABS(ct.soLuong)) - ROUND(ABS(ct.thanhTien) / (1.0 + ISNULL(sp.thueVAT, 0) / 100.0), 0) ELSE 0 END), 0) AS a_khuyenMai, "
    	        + "ISNULL(SUM(CASE WHEN hd.loaiHD = 'BAN_HANG' THEN ABS(ct.thanhTien) - ROUND(ABS(ct.thanhTien) / (1.0 + ISNULL(sp.thueVAT, 0) / 100.0), 0) ELSE 0 END), 0) AS a_vat, "
    	        
    	        // --- NHÓM B: ĐỔI/TRẢ HÀNG ---
    	        + "ISNULL(SUM(CASE WHEN hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') AND ct.soLuong < 0 AND hd.ghiChu LIKE N'%Hoàn thành%' THEN dvl.gia * ABS(ct.soLuong) ELSE 0 END), 0) AS b_giaGoc, "
    	        + "ISNULL(SUM(CASE WHEN hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') AND ct.soLuong < 0 AND hd.ghiChu LIKE N'%Hoàn thành%' THEN (dvl.gia * ABS(ct.soLuong)) - ROUND(ABS(ct.thanhTien) / (1.0 + ISNULL(sp.thueVAT, 0) / 100.0), 0) ELSE 0 END), 0) AS b_khuyenMai, "
    	        + "ISNULL(SUM(CASE WHEN hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') AND ct.soLuong < 0 AND hd.ghiChu LIKE N'%Hoàn thành%' THEN ABS(ct.thanhTien) - ROUND(ABS(ct.thanhTien) / (1.0 + ISNULL(sp.thueVAT, 0) / 100.0), 0) ELSE 0 END), 0) AS b_vat "
    	        
    	        + "FROM HoaDon hd "
    	        + "LEFT JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
    	        + "LEFT JOIN SanPham sp ON ct.sanPhamId = sp.id "
    	        + "LEFT JOIN DonViDoLuong dvl ON ct.donViDoLuongId = dvl.id AND ct.sanPhamId = dvl.sanPhamId "
    	        + "WHERE 1=1 "
    	    );

    	    // Xây dựng câu lệnh WHERE dựa vào Filter
    	    if (filter != null) {
    	        if (filter.maNV != null && !filter.maNV.isEmpty()) {
    	            sql.append(" AND hd.nhanVienId = '").append(filter.maNV).append("' ");
    	        }
    	        if (filter.fromDate != null) {
    	            sql.append(" AND CAST(hd.ngayLapHD AS DATE) >= '").append(filter.fromDate.toString()).append("' ");
    	        }
    	        if (filter.toDate != null) {
    	            sql.append(" AND CAST(hd.ngayLapHD AS DATE) <= '").append(filter.toDate.toString()).append("' ");
    	        }
    	        if (filter.startTime != null) {
    	            sql.append(" AND hd.ngayLapHD >= ? ");
    	        } else if (filter.ca != null && filter.fromDate == null && filter.toDate == null) {
    	            int ca = filter.ca;
    	            if (ca == 1) sql.append(" AND DATEPART(HOUR, hd.ngayLapHD) BETWEEN 6 AND 13 ");
    	            else if (ca == 2) sql.append(" AND DATEPART(HOUR, hd.ngayLapHD) BETWEEN 14 AND 21 ");
    	            else if (ca == 3) sql.append(" AND (DATEPART(HOUR, hd.ngayLapHD) >= 22 OR DATEPART(HOUR, hd.ngayLapHD) < 6) ");
    	        }
    	    }

    	    try (java.sql.PreparedStatement ps = getConn().prepareStatement(sql.toString())) {
    	        if (filter != null && filter.startTime != null) {
    	            ps.setTimestamp(1, java.sql.Timestamp.valueOf(filter.startTime));
    	        }
    	        
    	        try (java.sql.ResultSet rs = ps.executeQuery()) {
    	            if (rs.next()) {
    	                kq.soHdBan = rs.getInt("soHdBan");
    	                kq.a_giaGocChuaThue = rs.getDouble("a_giaGoc");
    	                kq.a_khuyenMai = rs.getDouble("a_khuyenMai");
    	                kq.a_vat = rs.getDouble("a_vat");

    	                kq.soHdTra = rs.getInt("soHdTra");
    	                kq.b_giaGocMonTra = rs.getDouble("b_giaGoc");
    	                kq.b_khuyenMaiHoanTra = rs.getDouble("b_khuyenMai");
    	                kq.b_vatHoanTra = rs.getDouble("b_vat");
    	            }
    	        }
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	    }
    	    return kq;
    	}
}