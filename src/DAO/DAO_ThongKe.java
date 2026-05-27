package DAO;
import Entity.BoLocThongKe;
import Entity.DoiChieuCa;

import ConnectDB.ConnectDB;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class DAO_ThongKe {

    public DAO_ThongKe() {
    }

    // Lấy kết nối DB từ singleton ConnectDB
    private Connection getConn() {
        return ConnectDB.getInstance().getConnection();
    }

    // Gắn thêm điều kiện WHERE vào câu SQL dựa trên filter (NV, ca, năm, tháng, quý, tùy chỉnh)
    // dateCol = tên cột ngày (vd "hd.ngayLapHD"), nvCol = tên cột nhân viên
    // Tự động loại trừ HĐ Lưu nháp và Đã hủy
    public void applyFilter(Entity.BoLocThongKe filter, StringBuilder sql, List<Object> params,
            String dateCol, String nvCol) {
        if (filter == null)
            return;
        sql.append(" AND (hd.ghiChu IS NULL OR (hd.ghiChu NOT LIKE N'%Lưu nháp%' AND hd.ghiChu NOT LIKE N'%Đã hủy%'))");

        // Lọc theo mã nhân viên nếu có
        if (filter.getMaNV() != null && !filter.getMaNV().isEmpty()) {
            sql.append(" AND ").append(nvCol).append(" = ?");
            params.add(filter.getMaNV());
        }

        // Lọc theo giờ bắt đầu ca (startTime) hoặc theo số ca (1=sáng 6-13, 2=chiều 14-21, 3=tối 22-6)
        if (filter.getStartTime() != null) {
            sql.append(" AND ").append(dateCol).append(" >= ?");
            params.add(Timestamp.valueOf(filter.getStartTime()));
        } else if (filter.getCa() != null && filter.getCa() > 0) {
            if (filter.getCa() == 1)
                sql.append(" AND DATEPART(HOUR, ").append(dateCol).append(") BETWEEN 6 AND 13");
            else if (filter.getCa() == 2)
                sql.append(" AND DATEPART(HOUR, ").append(dateCol).append(") BETWEEN 14 AND 21");
            else if (filter.getCa() == 3)
                sql.append(" AND (DATEPART(HOUR, ").append(dateCol).append(") >= 22 OR DATEPART(HOUR, ").append(dateCol)
                        .append(") < 6)");
        }

        // Lọc theo năm nếu có
        if (filter.getYear() != null) {
            sql.append(" AND YEAR(").append(dateCol).append(") = ?");
            params.add(filter.getYear());
        }

        // Lọc theo tháng / quý / khoảng ngày tùy chỉnh (fromDate - toDate)
        if ("THANG".equals(filter.getModeLocThoiGian()) && filter.getMonth() != null) {
            sql.append(" AND MONTH(").append(dateCol).append(") = ?");
            params.add(filter.getMonth());
        } else if ("QUY".equals(filter.getModeLocThoiGian()) && filter.getQuarter() != null) {
            if (filter.getQuarter() == 1)
                sql.append(" AND MONTH(").append(dateCol).append(") BETWEEN 1 AND 3");
            else if (filter.getQuarter() == 2)
                sql.append(" AND MONTH(").append(dateCol).append(") BETWEEN 4 AND 6");
            else if (filter.getQuarter() == 3)
                sql.append(" AND MONTH(").append(dateCol).append(") BETWEEN 7 AND 9");
            else if (filter.getQuarter() == 4)
                sql.append(" AND MONTH(").append(dateCol).append(") BETWEEN 10 AND 12");
        } else if ("TUYCHINH".equals(filter.getModeLocThoiGian()) && filter.getFromDate() != null && filter.getToDate() != null) {
            sql.append(" AND CAST(").append(dateCol).append(" AS DATE) BETWEEN ? AND ?");
            params.add(filter.getFromDate());
            params.add(filter.getToDate());
        }
    }

    // =====================================================================
    // LEGACY METHODS — Vẫn giữ vì BUS_ThongKe vẫn dùng trực tiếp
    // =====================================================================

    // Đếm số HĐ loại BAN_HANG trong khoảng tuNgay - denNgay (bỏ HĐ nháp/hủy)
    public int demSoLuongHoaDon(LocalDateTime tuNgay, LocalDateTime denNgay) {
        int soLuong = 0;
        String sql = "SELECT COUNT(*) as TongSo FROM HoaDon WHERE ngayLapHD BETWEEN ? AND ? AND loaiHD = 'BAN_HANG'" +
                     " AND (ghiChu IS NULL OR (ghiChu NOT LIKE N'%Lưu nháp%' AND ghiChu NOT LIKE N'%Đã hủy%'))";
        try (Connection con = getConn();
             PreparedStatement pst = con.prepareStatement(sql)) {
             
            pst.setTimestamp(1, Timestamp.valueOf(tuNgay));
            pst.setTimestamp(2, Timestamp.valueOf(denNgay));
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    soLuong = rs.getInt("TongSo");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return soLuong;
    }

    // Tính doanh thu thuần trong khoảng tuNgay - denNgay
    // Công thức: SUM(thanhTien / (1 + VAT%)) cho BAN_HANG và DOI_HANG xuất
    //            trừ đi phần TRA_HANG và DOI_HANG nhận lại
    //            rồi trừ thêm tiền giảm từ điểm thưởng (đọc từ ghiChu)
    // Dùng nội bộ bởi getRawCogsData — không phải public API
    private double tinhDoanhThu(LocalDateTime tuNgay, LocalDateTime denNgay) {
        double doanhThu = 0;
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
                + "  AND (hd.ghiChu IS NULL OR (hd.ghiChu NOT LIKE N'%Lưu nháp%' AND hd.ghiChu NOT LIKE N'%Đã hủy%')) "
                + "GROUP BY hd.id, hd.ghiChu, hd.loaiHD";
        try (Connection con = getConn(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setTimestamp(1, Timestamp.valueOf(tuNgay));
            pst.setTimestamp(2, Timestamp.valueOf(denNgay));
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    doanhThu += rs.getDouble("dtThuanNet");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doanhThu;
    }

    // Tính lợi nhuận = doanh thu thuần - giá vốn hàng bán (COGS) trong khoảng thời gian
    // COGS lấy từ PhanBoLoHang (giá vốn thực tế xuất bán), trừ đi giá vốn hàng nhận lại (TRA/DOI)
    // Trả về raw COGS từ DB: double[]{giaVonBan, giaVonHoan}
    // BUS_ThongKe.getTongLoiNhuan() nhận mảng này rồi tính: loiNhuan = doanhThu - (giaVonBan - giaVonHoan)
    public double[] getRawCogsData(LocalDateTime tuNgay, LocalDateTime denNgay) {
        double[] result = {0, 0};
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
            "           SUM(pbl.soLuong * dvl.chuyenDoiDonViCoBan * lh.gia) AS giaVon " +
            "    FROM PhanBoLoHang pbl JOIN LoHang lh ON lh.id = pbl.loHangId " +
            "    JOIN DonViDoLuong dvl ON dvl.id = pbl.donViDoLuongId AND dvl.sanPhamId = pbl.sanPhamId " +
            "    GROUP BY pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId " +
            ") pbl_cost ON pbl_cost.hoaDonId = ct.hoaDonId " +
            "          AND pbl_cost.sanPhamId = ct.sanPhamId " +
            "          AND pbl_cost.donViDoLuongId = ct.donViDoLuongId " +
            "WHERE hd.ngayLapHD BETWEEN ? AND ? " +
            "  AND hd.loaiHD IN ('BAN_HANG', 'TRA_HANG', 'DOI_HANG')" +
            "  AND (hd.ghiChu IS NULL OR (hd.ghiChu NOT LIKE N'%Lưu nháp%' AND hd.ghiChu NOT LIKE N'%Đã hủy%'))";
        try (Connection con = getConn(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setTimestamp(1, Timestamp.valueOf(tuNgay));
            pst.setTimestamp(2, Timestamp.valueOf(denNgay));
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    result[0] = rs.getDouble("giaVonBan");
                    result[1] = rs.getDouble("giaVonHoan");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // =====================================================================
    // NHÂN VIÊN DƯỢC SĨ
    // =====================================================================

    // Lấy danh sách dược sĩ đang làm việc: trả về List<{id, hoVaTen, chucVu}>
    public List<String[]> getDuocSiList() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT id, hoVaTen, chucVu FROM NhanVien "
                + "WHERE trangThaiLamViec='DANG_LAM_VIEC' ORDER BY hoVaTen";
        try (Connection con = getConn(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(new String[] { rs.getString("id"), rs.getString("hoVaTen"), rs.getString("chucVu") });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy phương thức thanh toán (phuongThucThanhToan) của 1 HĐ theo id
    // Mặc định trả về "TIEN_MAT" nếu không tìm thấy
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

    // Lấy raw HĐ BAN_HANG hôm nay, có thể lọc thêm theo pttt và filter
    // Trả về List<{rỗng, tongGocCoVAT, ghiChu, tongGocChuaVAT}>
    public List<Object[]> getRawHDHomNay(Entity.BoLocThongKe filter, String pttt) {
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
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new Object[] { "", rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"), rs.getDouble("tongGocChuaVAT") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // Lấy raw HĐ BAN_HANG 7 ngày qua, có thể lọc theo pttt và filter
    // Trả về List<{id, tongGocCoVAT, ghiChu, tongGocChuaVAT}>
    public List<Object[]> getRawHD7NgayQua(Entity.BoLocThongKe filter, String pttt) {
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
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new Object[] { rs.getString("id"), rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"), rs.getDouble("tongGocChuaVAT") });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // Lấy raw HĐ BAN_HANG trong khoảng tuNgay - denNgay
    // Trả về List<{rỗng, tongGocCoVAT, ghiChu, tongGocChuaVAT}>
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
                + "AND (hd.ghiChu IS NULL OR (hd.ghiChu NOT LIKE N'%Lưu nháp%' AND hd.ghiChu NOT LIKE N'%Đã hủy%')) "
                + "GROUP BY hd.id, hd.ghiChu";
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(tuNgay));
            ps.setTimestamp(2, Timestamp.valueOf(denNgay));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new Object[] { "", rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"), rs.getDouble("tongGocChuaVAT") });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // Lấy raw HĐ BAN_HANG của 1 NV từ thời điểm start, lọc thêm theo pttt nếu có
    // Trả về List<{id, tongGocCoVAT, ghiChu, tongGocChuaVAT}>
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
                + " AND (hd.ghiChu IS NULL OR (hd.ghiChu NOT LIKE N'%Lưu nháp%' AND hd.ghiChu NOT LIKE N'%Đã hủy%'))"
                + " GROUP BY hd.id, hd.ghiChu";
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new Object[] { rs.getString("id"), rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"), rs.getDouble("tongGocChuaVAT") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // Lấy doanh thu thuần từng tháng (1-12) của năm year
    // Trả về List<{thang(int), doanhThuThuan(double)}>
    public List<Object[]> getRawHD12Thang(int year, Entity.BoLocThongKe filter) {
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
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(new Object[] { rs.getInt("m"), rs.getDouble("doanhThuThuan") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // Lấy raw HĐ BAN_HANG 30 ngày gần nhất, nhóm theo ngày
    // Trả về List<{ngay(dd/MM/yyyy), id, tongGocCoVAT, ghiChu, tongGocChuaVAT}>
    public List<Object[]> getRawHD30Ngay(Entity.BoLocThongKe filter) {
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
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new Object[] { rs.getString("d"), rs.getString("id"),
                            rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"), rs.getDouble("tongGocChuaVAT") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // Lấy raw HĐ đổi/trả đã hoàn thành theo điều kiện ngày và filter
    // Trả về List<{loaiHD, ghiChu}> để BUS parse tiền hoàn/bù thêm
    public List<Object[]> getRawHDDoiTra(String dateCondition, Entity.BoLocThongKe filter) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT loaiHD, ghiChu FROM HoaDon hd WHERE " + dateCondition
                + " AND hd.loaiHD IN ('TRA_HANG','DOI_HANG') AND hd.ghiChu LIKE N'%Hoàn thành%'");
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new Object[] { rs.getString("loaiHD"), rs.getString("ghiChu") });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // Lấy doanh thu thực thu (có VAT) từng giờ trong ngày dateYMD
    // Trả về List<{gio(int 0-23), tienThucThu(double)}>
    public List<Object[]> getRawHDGioTrongNgay(String dateYMD, Entity.BoLocThongKe filter) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DATEPART(HOUR, hd.ngayLapHD) AS h, "
                + "SUM(ABS(ct.thanhTien)) AS tienThucThu "
                + "FROM HoaDon hd "
                + "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
                + "WHERE hd.loaiHD = 'BAN_HANG' AND CAST(hd.ngayLapHD AS DATE) = ? ");
        List<Object> params = new ArrayList<>();
        params.add(dateYMD);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY DATEPART(HOUR, hd.ngayLapHD)");

        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new Object[]{rs.getInt("h"), rs.getDouble("tienThucThu")});
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // Lấy raw HĐ BAN_HANG trong ngày dateYMD để tính Top SP
    // Trả về List<{id, tongGocCoVAT, ghiChu, tongGocChuaVAT}>
    public List<Object[]> getRawHDForTopSP(String dateYMD, Entity.BoLocThongKe filter) {
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
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new Object[] { rs.getString("id"), rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"), rs.getDouble("tongGocChuaVAT") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // Lấy SP xuất ra từ HĐ DOI_HANG hôm nay (chỉ dòng soLuong > 0 = hàng đổi mới cho khách)
    // Trả về List<{tenSP, soLuong, dtThuan}>
    public List<Object[]> getSPDoiHangXuatRaTrongNgay(String dateYMD, Entity.BoLocThongKe filter) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT sp.ten, SUM(ct.soLuong) AS sl, " +
            "SUM(ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0)) AS dtThuan " +
            "FROM HoaDon hd " +
            "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId " +
            "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
            "WHERE hd.loaiHD = 'DOI_HANG' AND ct.soLuong > 0 " +
            "AND CAST(hd.ngayLapHD AS DATE) = ? ");
        List<Object> params = new ArrayList<>();
        params.add(dateYMD);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY sp.ten");
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new Object[]{rs.getString("ten"), rs.getInt("sl"), rs.getDouble("dtThuan")});
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // Lấy chi tiết các dòng sản phẩm trong 1 HĐ theo hdId
    // Trả về List<{tenSP, soLuong, doanhThuThuan}>
    public List<Object[]> getRawCTHD(String hdId) {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT sp.ten, ct.soLuong, "
                + "ROUND(ABS(ct.thanhTien) / (1 + ISNULL(sp.thueVAT, 0)/100.0), 0) AS doanhThuThuan "
                + "FROM ChiTietHoaDon ct "
                + "JOIN HoaDon hd ON ct.hoaDonId = hd.id "
                + "JOIN SanPham sp ON sp.id = ct.sanPhamId "
                + "JOIN DonViDoLuong dvl ON dvl.sanPhamId = ct.sanPhamId AND dvl.id = ct.donViDoLuongId "
                + "WHERE ct.hoaDonId = ?";
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hdId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new Object[] { rs.getString("ten"), rs.getInt("soLuong"), rs.getDouble("doanhThuThuan") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // Lấy raw HĐ BAN_HANG của 1 NV trong năm year, lọc theo khoảng giờ ca (startHour-endHour)
    // Trả về List<{id, tongGocCoVAT, ghiChu, tongGocChuaVAT}>
    public List<Object[]> getRawHDCaByTime(String nvId, int year, Entity.BoLocThongKe filter, int startHour, int endHour) {
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
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new Object[] { rs.getString("id"), rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"), rs.getDouble("tongGocChuaVAT") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // Lấy raw HĐ BAN_HANG của KH có tài khoản (join KhachHang) trong năm year
    // Trả về List<{hoVaTen, diemTichLuy, tongGocCoVAT, ghiChu, tongGocChuaVAT}>
    public List<Object[]> getRawKHHD(int year, Entity.BoLocThongKe filter) {
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
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new Object[] { rs.getString("hoVaTen"), rs.getInt("diemTichLuy"),
                            rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"), rs.getDouble("tongGocChuaVAT") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // Lấy danh sách HĐ hôm nay (BAN_HANG + TRA_HANG + DOI_HANG) để hiển thị dashboard
    // limit > 0 sẽ lấy TOP n, includeGio=true sẽ thêm cột giờ HH:mm
    // Trả về List<{id, tenKH, tongGocCoVAT, ghiChu, pttt, gio, loaiHD}>
    public List<Object[]> getRawHDDashboard(Entity.BoLocThongKe filter, int limit, boolean includeGio) {
        List<Object[]> result = new ArrayList<>();
        String topStr = limit > 0 ? "TOP " + limit + " " : "";
        String gioCol = includeGio ? ", FORMAT(hd.ngayLapHD, 'HH:mm') AS gio" : "";
        boolean hasDateFilter = filter != null && (
            filter.getFromDate() != null ||
            "THANG".equals(filter.getModeLocThoiGian()) || 
            ("QUY".equals(filter.getModeLocThoiGian())   && filter.getQuarter() != null) ||
            "CANAM".equals(filter.getModeLocThoiGian()));

        StringBuilder sql = new StringBuilder("SELECT " + topStr + "hd.id, ISNULL(kh.hoVaTen, N'Khách lẻ') AS kh, "
                + "ISNULL(SUM(ABS(ct.thanhTien)), 0) AS tongGocCoVAT, "
                + "hd.ghiChu, hd.phuongThucThanhToan AS pttt " + gioCol + ", hd.loaiHD "
                + "FROM HoaDon hd "
                + "LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id "
                + "LEFT JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
                + "LEFT JOIN DonViDoLuong dvl ON ct.donViDoLuongId = dvl.id AND ct.sanPhamId = dvl.sanPhamId "
                + "LEFT JOIN SanPham sp ON ct.sanPhamId = sp.id "
                + "WHERE hd.loaiHD IN ('BAN_HANG', 'TRA_HANG', 'DOI_HANG') ");
        if (!hasDateFilter) {
            sql.append("AND CAST(hd.ngayLapHD AS DATE) = CAST(GETDATE() AS DATE) ");
        }
        
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY hd.id, kh.hoVaTen, hd.phuongThucThanhToan, hd.ngayLapHD, hd.ghiChu, hd.loaiHD ");
        sql.append(" ORDER BY hd.ngayLapHD DESC");

        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String gio = includeGio ? rs.getString("gio") : null;
                    result.add(new Object[] { rs.getString("id"), rs.getString("kh"),
                            rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"),
                            rs.getString("pttt"), gio, rs.getString("loaiHD") });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // Lấy raw HĐ TRA_HANG/DOI_HANG theo điều kiện ngày, tính netRefund (có VAT) theo từng giờ
    // Trả về List<{loaiHD, ghiChu, gio(int), netRefund_coVAT(double)}>
    public List<Object[]> getRawHDDoiTraGio(String dateCondition, Entity.BoLocThongKe filter) {
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

        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new Object[] { rs.getString("loaiHD"), rs.getString("ghiChu"), rs.getInt("h"), rs.getDouble("netRefund_coVAT") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // =====================================================================
    // DONUT - PHÂN LOẠI SẢN PHẨM
    // =====================================================================

    // Tính % số lượng bán theo từng loại SP (THUOC_KE_DON, THUOC_KHONG_KE_DON, THUC_PHAM_CHUC_NANG, MY_PHAM)
    // Trả về int[4] = phần trăm (tổng = ~100%)
    public int[] getSoLuongTheoLoaiSP(int year, Entity.BoLocThongKe filter) {
        String[] catDB = { "THUOC_KE_DON", "THUOC_KHONG_KE_DON", "THUC_PHAM_CHUC_NANG", "MY_PHAM" };
        int[] catVals = new int[4];

        for (int i = 0; i < 4; i++) {
            StringBuilder sql = new StringBuilder("SELECT ISNULL(SUM(ct.soLuong),0) FROM ChiTietHoaDon ct "
                    + "JOIN HoaDon hd ON hd.id=ct.hoaDonId "
                    + "JOIN SanPham sp ON sp.id=ct.sanPhamId "
                    + "WHERE sp.danhMuc=? AND YEAR(hd.ngayLapHD)=? AND hd.loaiHD='BAN_HANG'");
            List<Object> params = new ArrayList<>();
            params.add(catDB[i]);
            params.add(year);
            applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");

            try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
                for (int p = 0; p < params.size(); p++)
                    ps.setObject(p + 1, params.get(p));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next())
                        catVals[i] = rs.getInt(1);
                }
            } catch (Exception e) {
                catVals[i] = 0;
            }
        }
        return catVals;
    }

    // =====================================================================
    // CHI PHÍ 12 THÁNG
    // =====================================================================

    // Tính giá vốn hàng bán (COGS) từng tháng trong năm year, đơn vị triệu đồng
    // = Tổng giá vốn BAN_HANG - giá vốn hàng Đổi/Trả nhận lại
    // Trả về double[12]
    public double[] getChiPhi12Thang(int year, Entity.BoLocThongKe filter) {
        double[] data = new double[12];
        
        // A. Tổng giá vốn hàng bán ra
        StringBuilder sqlBan = new StringBuilder(
            "SELECT MONTH(hd.ngayLapHD) as m, SUM(ISNULL(pbl_cost.giaVon, 0)) as cp " +
            "FROM HoaDon hd " +
            "JOIN ChiTietHoaDon ct ON ct.hoaDonId = hd.id " +
            "JOIN (SELECT pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId, SUM(pbl.soLuong * dvl.chuyenDoiDonViCoBan * lh.gia) as giaVon " +
            "      FROM PhanBoLoHang pbl JOIN LoHang lh ON lh.id = pbl.loHangId " +
            "      JOIN DonViDoLuong dvl ON dvl.id = pbl.donViDoLuongId AND dvl.sanPhamId = pbl.sanPhamId " +
            "      GROUP BY pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId) pbl_cost " +
            "ON pbl_cost.hoaDonId = ct.hoaDonId AND pbl_cost.sanPhamId = ct.sanPhamId AND pbl_cost.donViDoLuongId = ct.donViDoLuongId " +
            "WHERE YEAR(hd.ngayLapHD) = ? AND hd.loaiHD = 'BAN_HANG' "
        );
        List<Object> paramsBan = new ArrayList<>();
        paramsBan.add(year);
        applyFilter(filter, sqlBan, paramsBan, "hd.ngayLapHD", "hd.nhanVienId");
        sqlBan.append(" GROUP BY MONTH(hd.ngayLapHD)");
        
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sqlBan.toString())) {
            for (int i=0; i<paramsBan.size(); i++) ps.setObject(i+1, paramsBan.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    int m = rs.getInt("m");
                    if (m >= 1 && m <= 12) data[m-1] += rs.getDouble("cp");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        // B. Trừ giá vốn hàng Đổi/Trả nhận lại (soLuong < 0, đã hoàn thành)
        StringBuilder sqlTra = new StringBuilder(
            "SELECT MONTH(hd.ngayLapHD) as m, SUM(ISNULL(pbl_cost.giaVon, 0)) as cp " +
            "FROM HoaDon hd " +
            "JOIN ChiTietHoaDon ct ON ct.hoaDonId = hd.id " +
            "JOIN (SELECT pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId, SUM(pbl.soLuong * dvl.chuyenDoiDonViCoBan * lh.gia) as giaVon " +
            "      FROM PhanBoLoHang pbl JOIN LoHang lh ON lh.id = pbl.loHangId " +
            "      JOIN DonViDoLuong dvl ON dvl.id = pbl.donViDoLuongId AND dvl.sanPhamId = pbl.sanPhamId " +
            "      GROUP BY pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId) pbl_cost " +
            "ON pbl_cost.hoaDonId = ct.hoaDonId AND pbl_cost.sanPhamId = ct.sanPhamId AND pbl_cost.donViDoLuongId = ct.donViDoLuongId " +
            "WHERE YEAR(hd.ngayLapHD) = ? AND hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') AND hd.ghiChu LIKE N'%Hoàn thành%' " +
            "AND (hd.loaiHD = 'TRA_HANG' OR ct.soLuong < 0) "
        );
        List<Object> paramsTra = new ArrayList<>();
        paramsTra.add(year);
        applyFilter(filter, sqlTra, paramsTra, "hd.ngayLapHD", "hd.nhanVienId");
        sqlTra.append(" GROUP BY MONTH(hd.ngayLapHD)");

        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sqlTra.toString())) {
            for (int i=0; i<paramsTra.size(); i++) ps.setObject(i+1, paramsTra.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    int m = rs.getInt("m");
                    if (m >= 1 && m <= 12) data[m-1] -= rs.getDouble("cp");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        return data;
    }

    // =====================================================================
    // THỐNG KÊ THEO NGÀY
    // =====================================================================

    // Lấy 10 ngày gần nhất có HĐ BAN_HANG trong năm year, định dạng dd/MM/yyyy, sắp xếp tăng dần
    public List<String> get10NgayGanNhat(int year, Entity.BoLocThongKe filter) {
        List<String> dates = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT TOP 10 CONVERT(NVARCHAR, CAST(hd.ngayLapHD AS DATE), 103) AS d "
                + "FROM HoaDon hd WHERE hd.loaiHD='BAN_HANG' AND YEAR(hd.ngayLapHD)=?");
        List<Object> params = new ArrayList<>();
        params.add(year);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        sql.append(" GROUP BY CAST(hd.ngayLapHD AS DATE) ORDER BY CAST(hd.ngayLapHD AS DATE) DESC");
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    dates.add(0, rs.getString("d")); // đảo để tăng dần
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dates;
    }

    // Đếm số HĐ BAN_HANG của 1 NV trong 1 ngày cụ thể (định dạng dd/MM/yyyy)
    public int getDailyHDCuaNV(String nvId, String date, Entity.BoLocThongKe filter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM HoaDon hd WHERE hd.nhanVienId=? "
                + "AND CONVERT(NVARCHAR,CONVERT(DATE,hd.ngayLapHD),103)=? AND hd.loaiHD='BAN_HANG'");
        List<Object> params = new ArrayList<>();
        params.add(nvId);
        params.add(date);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int p = 0; p < params.size(); p++)
                ps.setObject(p + 1, params.get(p));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (Exception e) {
            /* ignored */ }
        return 0;
    }

    // =====================================================================
    // TỔNG SỐ HÓA ĐƠN
    // =====================================================================

    // Đếm tổng HĐ BAN_HANG trong năm year có áp dụng filter
    public long getTongHoaDon(int year, Entity.BoLocThongKe filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM HoaDon hd WHERE YEAR(hd.ngayLapHD)=? AND hd.loaiHD='BAN_HANG'");
        List<Object> params = new ArrayList<>();
        params.add(year);
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int p = 0; p < params.size(); p++)
                ps.setObject(p + 1, params.get(p));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getLong(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Top 10 SP bán chạy nhất theo doanh thu thuần (có trừ TRA/DOI), đơn vị triệu đồng
    // Trả về List<{tenSP, danhMuc, soLuong, dtTrieu}>
    public List<Object[]> getTopSanPham(int year, Entity.BoLocThongKe filter) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT sp.ten, sp.danhMuc, "
                + "SUM(CASE WHEN hd.loaiHD = 'BAN_HANG' THEN ct.soLuong * dvl.chuyenDoiDonViCoBan ELSE -ABS(ct.soLuong * dvl.chuyenDoiDonViCoBan) END) AS sl, "
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
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 0;
                while (rs.next() && rank < 10) {
                    result.add(new Object[] { rs.getString("ten"), rs.getString("danhMuc"), 
                                              rs.getInt("sl"), rs.getDouble("dtThuan") });
                    rank++;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // Top 20 SP có tiền thuế VAT cao nhất trong năm year (đã trừ TRA/DOI), đơn vị triệu
    // Trả về List<{spId, tenSP, danhMuc, vatPct, tienThueTrieu}>
    public List<Object[]> getVATReport(int year, Entity.BoLocThongKe filter) {
        List<Object[]> result = new ArrayList<>();
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
            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next() && count < 20) {
                    result.add(new Object[] { rs.getString("spId"), rs.getString("ten"), rs.getString("danhMuc"), 
                                              rs.getInt("vatPct"), rs.getDouble("tienThue") });
                    count++;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // Lấy danh sách SP sắp hết hạn trong 6 tháng tới (trangThai = CON_HANG)
    // Trả về List<{soLoHang, tenSP, kho, soLuongLoHang, ngayHetHan(dd/MM/yyyy)}>
    public List<Object[]> getSpSapHetHan() {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT lh.soLoHang, sp.ten, kh.id AS Kho, lh.soLuongLoHang, lh.ngayHetHan "
                + "FROM LoHang lh "
                + "JOIN SanPham sp ON lh.sanPhamId = sp.id "
                + "JOIN KhoHang kh ON lh.khoHangId = kh.id "
                + "WHERE lh.trangThai = 'CON_HANG' "
                + "AND lh.ngayHetHan <= DATEADD(MONTH, 6, GETDATE()) "
                + "ORDER BY lh.ngayHetHan ASC";
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
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

    // Lấy thống kê tổng hợp của 1 ngày cụ thể (BAN_HANG): số HĐ, số KH, số SP, doanh thu thuần
    // Trả về Object[4]: {tongHD, dtTrieu, tongKH, tongSP}
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
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[] { rs.getInt("tongHD"), rs.getDouble("tongDT"), rs.getInt("tongKH"), rs.getInt("tongSP") };
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new Object[] { 0, 0.0, 0, 0 };
    }

    // Lấy danh sách NV bán hàng trong ngày dateYMD: tên NV, số HĐ, doanh thu thuần (triệu)
    // Sắp xếp theo doanh thu giảm dần
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
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[] { rs.getString("hoVaTen"), rs.getInt("soHD"), rs.getDouble("dtThuan") });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // Lấy thống kê BAN_HANG theo từng ngày trong tuần bắt đầu từ weekStartYMD (7 ngày)
    // Tính doanh thu sau khi trừ điểm thưởng, đơn vị triệu
    // Trả về List<{ngay(dd/MM/yyyy), soHD(int), dtTrieu(double)}>
    public List<Object[]> getThongKeTuan(String weekStartYMD) {
        List<Object[]> result = new ArrayList<>();
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
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, weekStartYMD);
            ps.setString(2, weekStartYMD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String d = rs.getString("d");
                    double dtThuan = rs.getDouble("doanhThuThuan");
                    double dt = dtThuan;
                    double[] cur = dayMap.computeIfAbsent(d, k -> new double[]{0, 0});
                    cur[0]++;       // soHD
                    cur[1] += dt;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dayMap.forEach((d, v) -> result.add(new Object[]{d, (int) v[0], v[1]}));
        return result;
    }

    // =====================================================================
    // THỐNG KÊ KHÁCH HÀNG
    // =====================================================================

    // Đếm KH mới đăng ký từng tháng trong năm year, trả về int[12]
    public int[] getKHMoiTheoThang(int year) {
        int[] data = new int[12];
        String sql = "SELECT MONTH(ngayTao) m, COUNT(*) cnt FROM KhachHang WHERE YEAR(ngayTao)=? GROUP BY MONTH(ngayTao)";
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int m = rs.getInt("m");
                    if (m >= 1 && m <= 12)
                        data[m - 1] = rs.getInt("cnt");
                }
            }
        } catch (Exception e) {
            /* ngayTao có thể null */ }
        return data;
    }

    // KPI tổng hợp khách hàng: tổng KH, số KH có tài khoản (sdt), tổng điểm tích lũy
    // Trả về Object[3]: {tongKH, khCoTK, tongDiem}
    public Object[] getKpiKhachHang() {
        Object[] result = { 0, 0, 0 };
        try (Connection con = getConn(); Statement st = con.createStatement(); 
             ResultSet rs = st.executeQuery("SELECT COUNT(*) tongKH, COUNT(sdt) khCoTK, ISNULL(SUM(diemTichLuy),0) tongDiem FROM KhachHang")) {
            if (rs.next())
                result = new Object[] { rs.getInt("tongKH"), rs.getInt("khCoTK"), rs.getInt("tongDiem") };
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // Top KH sắp xếp theo điểm tích lũy giảm dần, tính doanh thu thực mua của từng KH
    // Trả về List<{hoVaTen, sdt, soHD, dtTrieu, diemTichLuy}>
    public List<Object[]> getTopKhachHangTheoDiem(int limit) {
        List<Object[]> result = new ArrayList<>();
        try (Connection con = getConn()) {
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
        	        double dt = dtThuan;
        	        double[] cur = purchaseMap.computeIfAbsent(khId, k -> new double[]{0, 0});
        	        cur[0]++; // soHD
        	        cur[1] += dt;
        	    }
        	}
            String sqlKH = "SELECT TOP " + limit + " id, hoVaTen, ISNULL(sdt,'') AS sdt, diemTichLuy "
                    + "FROM KhachHang ORDER BY diemTichLuy DESC";
            try (java.sql.Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlKH)) {
                while (rs.next()) {
                    String khId = rs.getString("id");
                    double[] p = purchaseMap.getOrDefault(khId, new double[] { 0, 0 });
                    result.add(new Object[] { rs.getString("hoVaTen"), rs.getString("sdt"),
                            (int) p[0], p[1], rs.getInt("diemTichLuy") });
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

    // Tổng giá trị tồn kho (lô CON_HANG): SUM(soLuongLoHang * gia)
    public double getTongGiaTriTonKho() {
        String sql = "SELECT ISNULL(SUM(lh.soLuongLoHang*lh.gia),0) FROM LoHang lh WHERE lh.trangThai='CON_HANG'";
        try (Connection con = getConn(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next())
                return rs.getDouble(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Đếm số lô hàng theo trạng thái: CON_HANG, HET_HANG, HET_HAN
    // Trả về Object[3]: {conHang, hetHang, hetHan}
    public Object[] getSoLoTheoTrangThai() {
        Object[] result = { 0, 0, 0 };
        try (Connection con = getConn(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT SUM(CASE WHEN trangThai='CON_HANG' THEN 1 ELSE 0 END) conHang, "
                            + "SUM(CASE WHEN trangThai='HET_HANG' THEN 1 ELSE 0 END) hetHang, "
                            + "SUM(CASE WHEN trangThai='HET_HAN' THEN 1 ELSE 0 END) hetHan "
                            + "FROM LoHang")) {
            if (rs.next())
                result = new Object[] { rs.getInt("conHang"), rs.getInt("hetHang"), rs.getInt("hetHan") };
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // Phân bổ tồn kho theo từng kho, đơn vị triệu, sắp xếp theo giá trị giảm dần
    // Trả về List<{maKho, soLuong, giaTriTrieu}>
    public List<Object[]> getTonKhoTheoKho() {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT kh.id, ISNULL(SUM(lh.soLuongLoHang),0) sl, ISNULL(SUM(lh.soLuongLoHang*lh.gia),0)/1000000.0 gt "
                + "FROM KhoHang kh LEFT JOIN LoHang lh ON lh.khoHangId=kh.id AND lh.trangThai='CON_HANG' "
                + "GROUP BY kh.id ORDER BY gt DESC";
        try (Connection con = getConn(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                result.add(new Object[] { rs.getString("id"), rs.getInt("sl"), rs.getDouble("gt") });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // Top 10 SP tồn kho nhiều nhất, đơn vị triệu
    // Trả về List<{tenSP, soLuongTon, giaTriTrieu}>
    public List<Object[]> getTopSPTonNhieu() {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT TOP 10 sp.ten, ISNULL(SUM(lh.soLuongLoHang),0) sl, "
                + "ISNULL(SUM(lh.soLuongLoHang*lh.gia),0)/1000000.0 gt "
                + "FROM SanPham sp LEFT JOIN LoHang lh ON sp.id=lh.sanPhamId AND lh.trangThai='CON_HANG' "
                + "GROUP BY sp.ten ORDER BY sl DESC";
        try (Connection con = getConn(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                result.add(new Object[] { rs.getString("ten"), rs.getInt("sl"), rs.getDouble("gt") });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // Giá trị nhập hàng (theo ngayNhap LoHang) từng tháng trong năm year, đơn vị triệu
    // Trả về double[12]
    public double[] getNhapHang12Thang(int year) {
        double[] data = new double[12];
        String sql = "SELECT MONTH(ngayNhap) m, ISNULL(SUM(soLuongLoHang*gia),0)/1000000.0 gt "
                + "FROM LoHang WHERE YEAR(ngayNhap)=? GROUP BY MONTH(ngayNhap)";
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int m = rs.getInt("m");
                    if (m >= 1 && m <= 12)
                        data[m - 1] = rs.getDouble("gt");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    // =====================================================================
    // DASHBOARD — CÁC HÀM ĐẾM ĐƠN GIẢN
    // =====================================================================

    // Tổng số sản phẩm (tất cả) trong bảng SanPham
    public int getTongSanPham() {
        try (Connection con = getConn(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM SanPham")) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Tổng số khách hàng trong bảng KhachHang
    public int getTongKhachHang() {
        try (Connection con = getConn(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM KhachHang")) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Đếm số HĐ BAN_HANG của 1 NV từ thời điểm start (dùng cho kết ca)
    public int getSoHoaDonTheoCa(String maNV, LocalDateTime start) {
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE nhanVienId=? AND ngayLapHD>=? AND loaiHD='BAN_HANG'";
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Tổng số lượng SP thực tế hôm nay (BAN_HANG cộng + DOI_HANG xuất + TRA/DOI trừ)
    public int getTongSoLuongSPHomNay(Entity.BoLocThongKe filter) {
        StringBuilder sql = new StringBuilder(
            "SELECT ISNULL(SUM(" +
            "  CASE " +
            "    WHEN hd.loaiHD = 'BAN_HANG' THEN ct.soLuong " +
            "    WHEN hd.loaiHD = 'DOI_HANG' AND ct.soLuong > 0 " +
            "         AND hd.ghiChu LIKE N'%Hoàn thành%' THEN ct.soLuong " +
            "    WHEN (hd.loaiHD = 'TRA_HANG' " +
            "          OR (hd.loaiHD = 'DOI_HANG' AND ct.soLuong < 0)) " +
            "         AND hd.ghiChu LIKE N'%Hoàn thành%' THEN ct.soLuong " + 
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
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("tongSL");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Đếm số HĐ BAN_HANG hôm nay theo filter (NV, ca...)
    public int getSoHoaDonHomNay(Entity.BoLocThongKe filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM HoaDon hd WHERE CAST(hd.ngayLapHD AS DATE) = CAST(GETDATE() AS DATE) AND hd.loaiHD = 'BAN_HANG'");
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Đếm tất cả loại HĐ hôm nay (BAN_HANG + TRA_HANG + DOI_HANG) theo filter
    public int getSoTatCaHDHomNay(Entity.BoLocThongKe filter) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM HoaDon hd WHERE CAST(hd.ngayLapHD AS DATE) = CAST(GETDATE() AS DATE) " +
            "AND hd.loaiHD IN ('BAN_HANG', 'TRA_HANG', 'DOI_HANG')");
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // Đếm số HĐ BAN_HANG trong 7 ngày qua theo filter
    public int getSoHoaDon7NgayQua(Entity.BoLocThongKe filter) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM HoaDon hd WHERE hd.ngayLapHD >= DATEADD(DAY, -7, GETDATE()) AND hd.loaiHD = 'BAN_HANG'");
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Đếm tổng phiếu TRA_HANG/DOI_HANG đã hoàn thành theo filter
    public int getTongPhieuDoiTra(Entity.BoLocThongKe filter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM HoaDon hd WHERE hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') AND hd.ghiChu LIKE N'%Hoàn thành%'");
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int p = 0; p < params.size(); p++) ps.setObject(p + 1, params.get(p));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // Đếm phiếu TRA/DOI chưa xử lý (chưa Hoàn thành và chưa Từ chối)
    public int getPhieuDoiTraChoXuLy(Entity.BoLocThongKe filter) {
        String sqlStr = "SELECT COUNT(*) FROM HoaDon hd WHERE hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') "
                + "AND (hd.ghiChu IS NULL OR (hd.ghiChu NOT LIKE N'%Hoàn thành%' AND hd.ghiChu NOT LIKE N'%Từ chối%'))";
        StringBuilder sql = new StringBuilder(sqlStr);
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int p = 0; p < params.size(); p++) ps.setObject(p + 1, params.get(p));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // Top 4 SP sắp hết hàng nhất (tồn ít nhất), hiển thị widget cảnh báo
    // Trả về List<{tenSP, soLuongTon, 50}> (50 là ngưỡng giả định)
    public List<Object[]> getTop4SanPhamSapHetHang() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT TOP 4 sp.ten, ISNULL(SUM(lh.soLuongLoHang),0) ton FROM SanPham sp "
                + "LEFT JOIN LoHang lh ON sp.id=lh.sanPhamId GROUP BY sp.ten ORDER BY ton ASC";
        try (Connection con = getConn(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(new Object[] { rs.getString("ten"), rs.getInt("ton"), 50 });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Đếm số SP còn tồn kho (có ít nhất 1 lô)
    public int getSoSanPhamDuTon() {
        try (Connection con = getConn(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(DISTINCT sp.id) FROM SanPham sp JOIN LoHang lh ON sp.id=lh.sanPhamId")) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Đếm số lô hàng sắp hết hạn trong vòng days ngày
    public int getSoLoHangSapHetHanKhoang(int days) {
        try (Connection con = getConn(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM LoHang WHERE ngayHetHan IS NOT NULL AND DATEDIFF(DAY,GETDATE(),ngayHetHan)<=" + days)) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Lấy TOP 100 lô hàng còn tồn và sắp hết hạn trong vòng days ngày, sắp xếp theo ngày HH tăng dần
    // Trả về List<{tenSP, soLoHang, soLuong, ngayHetHan(Timestamp), conLai(ngày)}>
    public List<Object[]> getLoHangSapHetHanNhanh(int days) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT TOP 100 sp.ten, lh.soLoHang, lh.soLuongLoHang, lh.ngayHetHan, DATEDIFF(DAY,GETDATE(),lh.ngayHetHan) cl "
                + "FROM LoHang lh JOIN SanPham sp ON lh.sanPhamId=sp.id "
                + "WHERE lh.ngayHetHan IS NOT NULL AND DATEDIFF(DAY,GETDATE(),lh.ngayHetHan)<=? "
                + "AND lh.soLuongLoHang > 0 "
                + "ORDER BY lh.ngayHetHan ASC";
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(new Object[] { rs.getString("ten"), rs.getString("soLoHang"),
                            rs.getInt("soLuongLoHang"), rs.getTimestamp("ngayHetHan"), rs.getInt("cl") });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy raw HĐ BAN_HANG của 1 ngày cụ thể dateYMD
    // Trả về List<{id, tongGocCoVAT, ghiChu, tongGocChuaVAT}>
    public List<Object[]> getRawHDByDay(String dateYMD, Entity.BoLocThongKe filter) {
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
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++)
                ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    result.add(new Object[] { rs.getString("id"), rs.getDouble("tongGocCoVAT"), rs.getString("ghiChu"), rs.getDouble("tongGocChuaVAT") });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // Lấy danh sách NV đang làm việc (id, hoVaTen)
    public List<String[]> getDanhSachNhanVien() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT id, hoVaTen FROM NhanVien WHERE trangThaiLamViec='DANG_LAM_VIEC'";
        try (Connection con = getConn(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(new String[] { rs.getString("id"), rs.getString("hoVaTen") });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // =====================================================================
    // GỢI Ý KHUYẾN MÃI
    // =====================================================================

    // Lấy top 10 SP bán chạy nhất để phân tích gợi ý KM (biên LN, số lượng, giá vốn)
    // Trả về List<{tenSP, danhMuc, slBan, doanhThu, giaVon}>
    public List<Object[]> getRawTopSanPhamBanChay(int year) {
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
                + "           SUM(pbl.soLuong * dvl2.chuyenDoiDonViCoBan * lh.gia) AS tongGiaVon, "
                + "           SUM(pbl.soLuong * dvl2.chuyenDoiDonViCoBan)          AS tongSLCoBan "
                + "    FROM PhanBoLoHang pbl "
                + "    JOIN LoHang lh ON lh.id = pbl.loHangId "
                + "    JOIN DonViDoLuong dvl2 ON dvl2.id = pbl.donViDoLuongId AND dvl2.sanPhamId = pbl.sanPhamId "
                + "    GROUP BY pbl.sanPhamId, pbl.donViDoLuongId, pbl.hoaDonId"
                + ") pc ON pc.sanPhamId = ct.sanPhamId AND pc.donViDoLuongId = ct.donViDoLuongId AND pc.hoaDonId = ct.hoaDonId "
                + "LEFT JOIN LoHang lh2 ON lh2.sanPhamId = sp.id AND lh2.trangThai = 'CON_HANG' "
                + "WHERE YEAR(hd.ngayLapHD) = ? AND hd.loaiHD = 'BAN_HANG' "
                + "GROUP BY sp.ten, sp.danhMuc "
                + "ORDER BY soLuongBan DESC";
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[] {
                        rs.getString("ten"),
                        rs.getString("danhMuc") != null ? rs.getString("danhMuc") : "Khác",
                        rs.getInt("soLuongBan"),
                        rs.getDouble("doanhThu"),
                        rs.getDouble("giaVon")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Lấy top n KH VIP (diemTichLuy >= 500) mua hàng hôm nay, sắp xếp theo điểm giảm dần
    // Trả về List<{hoVaTen, diemTichLuy, soSP}>
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
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(new Object[] { rs.getString("hoVaTen"), rs.getInt("diemTichLuy"), rs.getInt("soSP") });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Alias cho getRawHDDashboard với limit và includeGio=true (dùng cho tab HĐ giá trị cao)
    public List<Object[]> getRawHDGiaTriCao(int limit, Entity.BoLocThongKe filter) {
        return getRawHDDashboard(filter, limit, true);
    }
    
    // Tính tổng tiền hoàn trả (có VAT và không VAT) của TRA_HANG/DOI_HANG hoàn thành theo điều kiện ngày
    // Trả về double[2]: [0]=tienCoVAT, [1]=tienChuaVAT
    public double[] getTienTraHangChinhXac(String dateCondition, Entity.BoLocThongKe filter) {
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

        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    res[0] = rs.getDouble("tienCoVAT");
                    res[1] = rs.getDouble("tienChuaVAT");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return res;
    }
 
    // Lấy % giảm giá theo phần trăm (loại GIAM_THEO_PHAN_TRAM) của 1 HĐ từ bảng HinhThucKhuyenMai
    public double getPercentKhuyenMai(String hdId) {
    	String sql = "SELECT ISNULL(MAX(ht.giaTri), 0) FROM HoaDon hd " +
                  "JOIN HinhThucKhuyenMai ht ON hd.khuyenMaiId = ht.khuyenMaiId " +
                  "WHERE hd.id = ? AND ht.loaiHinhThuc = 'GIAM_THEO_PHAN_TRAM'";
    	try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
    		ps.setString(1, hdId);
    		try (ResultSet rs = ps.executeQuery()) {
    		    if (rs.next()) return rs.getDouble(1);
            }
    	}catch (Exception e) { e.printStackTrace(); }
    	return 0;
    }
    
    // Lấy từng dòng sản phẩm của 1 HĐ: soLuong, giaNiemYet, thueVAT%, thanhTien (sau KM)
    // Trả về List<{soLuong(int), gia(double), thueVAT(double), thanhTien(double)}>
    public List<Object[]> getRawLineItems(String hdId) {
	     List<Object[]> list = new ArrayList<>();
	     String sql = "SELECT ct.soLuong, dvl.gia, ISNULL(sp.thueVAT,0) AS thueVAT, ABS(ct.thanhTien) AS thanhTien " +
	                  "FROM ChiTietHoaDon ct " +
	                  "JOIN DonViDoLuong dvl ON ct.donViDoLuongId = dvl.id AND ct.sanPhamId = dvl.sanPhamId " +
	                  "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
	                  "WHERE ct.hoaDonId = ?";
	     try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
	         ps.setString(1, hdId);
	         try (ResultSet rs = ps.executeQuery()) {
	             while (rs.next()) {
	                 list.add(new Object[] { 
	                     rs.getInt("soLuong"), 
	                     rs.getDouble("gia"), 
	                     rs.getDouble("thueVAT"),
	                     rs.getDouble("thanhTien")
	                 });
	             }
             }
	     } catch (Exception e) { e.printStackTrace(); }
	     return list;
    }
    
    // Lấy danh sách SP trong HĐ TRA_HANG trong ngày dateYMD: tên, số lượng, tiền chưa VAT
    // Trả về List<{tenSP, sl(int), chuaVAT(double)}>
    public List<Object[]> getTraHangChiTiet(String dateYMD, Entity.BoLocThongKe filter) {
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

        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[] { rs.getString("ten"), rs.getInt("sl"), rs.getDouble("chuaVAT") });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }
 
    // Lấy netRefund từng tháng của HĐ DOI_HANG hoàn thành trong năm year (chưa VAT)
    // netRefund: dương = hàng mới đắt hơn (tăng DT), âm = hoàn nhiều hơn (giảm DT)
    // Trả về List<{thang(int), netRefund_chuaVAT(double)}>
    public List<Object[]> getRawHDDoiTra12Thang(int year, Entity.BoLocThongKe filter) {
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

        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(new Object[] { rs.getInt("m"), rs.getDouble("netRefund_chuaVAT") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // Tạo chuỗi điều kiện WHERE từ filter (dùng cho các query xây chuỗi thủ công)
    // Trả về chuỗi SQL bắt đầu bằng " 1=1 AND ..."
    public String buildConditionFromFilter(Entity.BoLocThongKe f) {
	    if (f == null) return " 1=1 ";
	    StringBuilder sb = new StringBuilder(" 1=1 ");
	    if (f.getModeLocThoiGian() != null) {
	        if (f.getModeLocThoiGian().equals("THANG")) 
	            sb.append(" AND MONTH(hd.ngayLapHD) = ").append(f.getMonth()).append(" AND YEAR(hd.ngayLapHD) = YEAR(GETDATE())");
	        else if (f.getModeLocThoiGian().equals("QUY"))
	            sb.append(" AND DATEPART(QUARTER, hd.ngayLapHD) = ").append(f.getQuarter()).append(" AND YEAR(hd.ngayLapHD) = YEAR(GETDATE())");
	        else if (f.getModeLocThoiGian().equals("TUYCHINH") && f.getFromDate() != null && f.getToDate() != null)
	            sb.append(" AND CAST(hd.ngayLapHD AS DATE) BETWEEN '").append(f.getFromDate()).append("' AND '").append(f.getToDate()).append("'");
	    }
	    return sb.toString();
	}

    // Lấy raw HĐ BAN_HANG theo filter (Tháng/Quý/Tùy chỉnh + NV/Ca)
    // Trả về List<{id, rỗng, ghiChu}>
    public List<Object[]> getRawHDByFilter(Entity.BoLocThongKe filter) {
	    List<Object[]> result = new ArrayList<>();
	    StringBuilder sql = new StringBuilder("SELECT hd.id, hd.ghiChu FROM HoaDon hd WHERE hd.loaiHD = 'BAN_HANG' ");
	    List<Object> params = new ArrayList<>();
	    
	    if (filter.getModeLocThoiGian() != null) {
	        if (filter.getModeLocThoiGian().equals("THANG")) {
	        	int yr = (filter.getYear() != null) ? filter.getYear() : java.time.LocalDate.now().getYear();
	        	if (filter.getMonth() != null) {
	        	    sql.append(" AND YEAR(hd.ngayLapHD) = ? AND MONTH(hd.ngayLapHD) = ?");
	        	    params.add(yr);
	        	    params.add(filter.getMonth());
	        	} else {
	        	    sql.append(" AND YEAR(hd.ngayLapHD) = ?");
	        	    params.add(yr);
	        	}
	        } else if (filter.getModeLocThoiGian().equals("QUY")) {
	            sql.append(" AND DATEPART(QUARTER, hd.ngayLapHD) = ? AND YEAR(hd.ngayLapHD) = YEAR(GETDATE())");
	            params.add(filter.getQuarter());
	        } else if (filter.getModeLocThoiGian().equals("TUYCHINH")) {
	            sql.append(" AND CAST(hd.ngayLapHD AS DATE) BETWEEN ? AND ?");
	            params.add(filter.getFromDate()); params.add(filter.getToDate());
	        }
	    }
	    applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
	
	    try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
	        for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) result.add(new Object[] { rs.getString("id"), "", rs.getString("ghiChu") });
            }
	    } catch (Exception e) { e.printStackTrace(); }
	    return result;
	}
	
    // Lấy raw HĐ BAN_HANG của năm year theo filter: trả về từng HĐ kèm thang, id, ghiChu
    // Dùng để BUS tính doanh thu 12 tháng (per-HĐ rồi bóc VAT + trừ điểm)
    // Trả về List<{thang(int), id, ghiChu}>
    public List<Object[]> getRawHD12ThangChuan(int year, Entity.BoLocThongKe filter) {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT MONTH(hd.ngayLapHD) AS thang, hd.id, hd.ghiChu "
                + "FROM HoaDon hd "
                + "WHERE YEAR(hd.ngayLapHD) = ? AND hd.loaiHD = 'BAN_HANG'");
        List<Object> params = new ArrayList<>();
        params.add(year);
        
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");

        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[] { rs.getInt("thang"), rs.getString("id"), rs.getString("ghiChu") });
                }
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return result;
    }
    
    // Lấy netRefund từng ngày của HĐ DOI_HANG hoàn thành trong 30 ngày gần nhất (chưa VAT)
    // netRefund: âm = hoàn trả nhiều (giảm DT), dương = hàng đổi mới đắt hơn (tăng DT)
    // Trả về List<{ngay(dd/MM/yyyy), netRefund_chuaVAT(double)}>
    public List<Object[]> getRawHDDoiTra30Ngay(Entity.BoLocThongKe filter) {
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
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(new Object[] { rs.getString("d"), rs.getDouble("netRefund_chuaVAT") });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }
 
    // Tổng tiền hoàn trả (có VAT, không VAT) và giá vốn hàng nhận lại của TRA_HANG/DOI_HANG đã hoàn thành
    // Trả về double[3]: [0]=tienHoanCoVAT, [1]=tienHoanChuaVAT, [2]=giaVonHoan
    public double[] getTienVaGiaVonHangTra(Entity.BoLocThongKe filter) {
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
                "    SELECT pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId, SUM(pbl.soLuong * dvl2.chuyenDoiDonViCoBan * lh.gia) as giaVon " +
                "    FROM PhanBoLoHang pbl " +
                "    JOIN LoHang lh ON lh.id = pbl.loHangId " +
                "    JOIN DonViDoLuong dvl2 ON dvl2.id = pbl.donViDoLuongId AND dvl2.sanPhamId = pbl.sanPhamId " +
                "    GROUP BY pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId" +
                ") pbl_cost ON pbl_cost.hoaDonId = ct.hoaDonId AND pbl_cost.sanPhamId = ct.sanPhamId AND pbl_cost.donViDoLuongId = ct.donViDoLuongId " +
                "LEFT JOIN (" +
                "    SELECT pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId, " +
                "           (SUM(pbl.soLuong * dvl2.chuyenDoiDonViCoBan * lh.gia) / NULLIF(SUM(pbl.soLuong * dvl2.chuyenDoiDonViCoBan), 0)) AS donGiaVonGoc " +
                "    FROM PhanBoLoHang pbl " +
                "    JOIN LoHang lh ON lh.id = pbl.loHangId " +
                "    JOIN DonViDoLuong dvl2 ON dvl2.id = pbl.donViDoLuongId AND dvl2.sanPhamId = pbl.sanPhamId " +
                "    GROUP BY pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId " +
                ") pbl_goc ON pbl_goc.hoaDonId = hd.hoaDonGocId AND pbl_goc.sanPhamId = ct.sanPhamId AND pbl_goc.donViDoLuongId = ct.donViDoLuongId " +
                "WHERE hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') AND hd.ghiChu LIKE N'%Hoàn thành%' " +
                "AND (hd.loaiHD = 'TRA_HANG' OR ct.soLuong < 0)"
            );
        
        List<Object> params = new ArrayList<>();
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");

        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    kq[0] = rs.getDouble("tienHoanCoVAT");
                    kq[1] = rs.getDouble("tienHoanChuaVAT");
                    kq[2] = rs.getDouble("giaVonHoan");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return kq;
    }
    
    // Tính tổng tiền hoàn trả trong ca của 1 NV bằng cách parse ghiChu TRA_HANG/DOI_HANG
    // TRA_HANG: lấy phần [2] trong ghiChu split "|"
    // DOI_HANG: lấy phần [3], kiểm tra từ khóa "Hoàn" để biết tiệm trả tiền lại cho khách
    public double getTienHoanTraTheoCa(String maNV, LocalDateTime start) {
        double tongHoanTra = 0;
        String sql = "SELECT id, loaiHD, ghiChu FROM HoaDon "
                + "WHERE nhanVienId = ? AND ngayLapHD >= ? AND loaiHD IN ('TRA_HANG','DOI_HANG') "
                + "AND ghiChu LIKE N'%Hoàn thành%'";
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
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
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return tongHoanTra;
    }
    
    // Lấy số HĐ TRA/DOI hoàn thành và tổng tiền hoàn (chưa VAT) của 1 NV trong năm year
    // Trả về double[2]: [0]=soHD, [1]=tienHoan
    public double[] getThongKeTraHangNV(String nvId, int year, Entity.BoLocThongKe filter) {
        double[] res = {0, 0};
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
        List<Object> params = new ArrayList<>();
        params.add(nvId); params.add(year); // cho subquery soHD
        params.add(nvId); params.add(year); // cho main query tienHoan
        applyFilter(filter, sql, params, "hd.ngayLapHD", "hd.nhanVienId");
        
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i=0; i<params.size(); i++) ps.setObject(i+1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    res[0] = rs.getInt("soHD");
                    res[1] = rs.getDouble("tienHoan");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return res;
    }

    // =====================================================================
    // KẾT CA — TIỀN ĐỔI HÀNG
    // =====================================================================

    // Tính tiền đổi hàng trong ca của 1 NV (TẤT CẢ phương thức), chưa VAT
    // Trả về double[2]: [0]=tienBuThem (khách trả thêm), [1]=tienHoanLai (tiệm hoàn lại)
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
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
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

    // Giống getTienDoiHangTheoCa nhưng CHỈ tính HĐ thanh toán TIEN_MAT
    // Dùng để đối soát quỹ tiền mặt khi kết ca
    // Trả về double[2]: [0]=tienBuThem, [1]=tienHoanLai
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
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
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
    
    // Báo cáo tài chính nhóm theo ngày hoặc tháng (groupBy = "THANG" hoặc "NGAY")
    // Dùng 2 CTE: CTE_Revenue (doanh thu gộp, VAT, hàng trả) và CTE_COGS (giá vốn bán, giá vốn hoàn)
    // Trả về List<{thoiGian, dtGop, thueVAT, hangTra, giaVonBan, giaVonHoan}>
    public List<Object[]> getBaoCaoTaiChinh(
             Entity.BoLocThongKe filter, String groupBy) {
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
               + "             SUM(pbl.soLuong * dvl2.chuyenDoiDonViCoBan * lh.gia) AS giaVon\n"
               + "      FROM PhanBoLoHang pbl\n"
               + "      JOIN LoHang lh ON lh.id = pbl.loHangId\n"
               + "      JOIN DonViDoLuong dvl2 ON dvl2.id = pbl.donViDoLuongId AND dvl2.sanPhamId = pbl.sanPhamId\n"
               + "      GROUP BY pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId\n"
               + "  ) pbl_cost ON pbl_cost.hoaDonId       = ct.hoaDonId\n"
               + "            AND pbl_cost.sanPhamId       = ct.sanPhamId\n"
               + "            AND pbl_cost.donViDoLuongId  = ct.donViDoLuongId\n"
               + "  LEFT JOIN (\n"
               + "      SELECT pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId,\n"
               + "             (SUM(pbl.soLuong * dvl2.chuyenDoiDonViCoBan * lh.gia) / NULLIF(SUM(pbl.soLuong * dvl2.chuyenDoiDonViCoBan), 0)) AS donGiaVonGoc\n"
               + "      FROM PhanBoLoHang pbl\n"
               + "      JOIN LoHang lh ON lh.id = pbl.loHangId\n"
               + "      JOIN DonViDoLuong dvl2 ON dvl2.id = pbl.donViDoLuongId AND dvl2.sanPhamId = pbl.sanPhamId\n"
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
         try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
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

    // Tính tổng COGS (giá vốn bán - giá vốn hoàn) theo filter
    // Trả về double[2]: [0]=giaVonBan, [1]=giaVonHoan
    public double[] getCogsTheoFilter(Entity.BoLocThongKe filter) {
         double[] kq = {0.0, 0.0};

         StringBuilder filterSb = new StringBuilder();
         List<Object>  params   = new ArrayList<>();
         applyFilter(filter, filterSb, params, "hd.ngayLapHD", "hd.nhanVienId");

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
               + "           SUM(pbl.soLuong * dvl2.chuyenDoiDonViCoBan * lh.gia) AS giaVon\n"
               + "    FROM PhanBoLoHang pbl\n"
               + "    JOIN LoHang lh ON lh.id = pbl.loHangId\n"
               + "    JOIN DonViDoLuong dvl2 ON dvl2.id = pbl.donViDoLuongId AND dvl2.sanPhamId = pbl.sanPhamId\n"
               + "    GROUP BY pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId\n"
               + ") pbl_cost ON pbl_cost.hoaDonId      = ct.hoaDonId\n"
               + "          AND pbl_cost.sanPhamId      = ct.sanPhamId\n"
               + "          AND pbl_cost.donViDoLuongId = ct.donViDoLuongId\n"
               + "LEFT JOIN (\n"
               + "    SELECT pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId,\n"
               + "           (SUM(pbl.soLuong * dvl2.chuyenDoiDonViCoBan * lh.gia) / NULLIF(SUM(pbl.soLuong * dvl2.chuyenDoiDonViCoBan), 0)) AS donGiaVonGoc\n"
               + "    FROM PhanBoLoHang pbl\n"
               + "    JOIN LoHang lh ON lh.id = pbl.loHangId\n"
               + "    JOIN DonViDoLuong dvl2 ON dvl2.id = pbl.donViDoLuongId AND dvl2.sanPhamId = pbl.sanPhamId\n"
               + "    GROUP BY pbl.hoaDonId, pbl.sanPhamId, pbl.donViDoLuongId\n"
               + ") pbl_goc ON pbl_goc.hoaDonId       = hd.hoaDonGocId\n"
               + "         AND pbl_goc.sanPhamId      = ct.sanPhamId\n"
               + "         AND pbl_goc.donViDoLuongId = ct.donViDoLuongId\n"
               + "WHERE hd.loaiHD IN ('BAN_HANG', 'TRA_HANG', 'DOI_HANG')\n"
               + filterSb.toString();

         try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
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
     
     // =====================================================================
     // PHÂN TÍCH KM / XU HƯỚNG / MÙA VỤ
     // =====================================================================

     // Kiểm tra SP (theo tên) có đang chạy KM hoạt động hôm nay không
     // Link: SanPham → ApDungKhuyenMai → KhuyenMai.trangThai = 'HOAT_DONG'
     public boolean coKMDangChay(String tenSP) {
         String sql = "SELECT COUNT(*) FROM ApDungKhuyenMai akm "
                 + "JOIN KhuyenMai km ON akm.khuyenMaiId = km.id "
                 + "JOIN SanPham sp   ON akm.sanPhamId   = sp.id "
                 + "WHERE sp.ten = ? "
                 + "  AND km.trangThai = 'HOAT_DONG' "
                 + "  AND km.ngayBatDau <= GETDATE() "
                 + "  AND (km.ngayKetThuc IS NULL OR km.ngayKetThuc >= GETDATE())";
         try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
             ps.setString(1, tenSP);
             try (ResultSet rs = ps.executeQuery()) {
                 if (rs.next()) return rs.getInt(1) > 0;
             }
         } catch (Exception e) { e.printStackTrace(); }
         return false;
     }

     // So sánh số lượng bán quý này vs quý trước của năm year (nếu là năm hiện tại dùng quý thực tế)
     // Trả về int[2]: [0]=slQuyHienTai, [1]=slQuyCu
     public int[] getXuHuongBanSP(String tenSP, int year) {
         int nowYear = java.time.LocalDate.now().getYear();
         int currentQ = (year == nowYear)
                 ? (java.time.LocalDate.now().getMonthValue() - 1) / 3 + 1
                 : 4;
         int prevQ    = currentQ > 1 ? currentQ - 1 : 4;
         int prevYear = currentQ > 1 ? year : year - 1;

         String sql = "SELECT "
                 + "ISNULL(SUM(CASE WHEN DATEPART(QUARTER, hd.ngayLapHD) = ? AND YEAR(hd.ngayLapHD) = ? "
                 + "               THEN ct.soLuong ELSE 0 END), 0) AS slQuyNay, "
                 + "ISNULL(SUM(CASE WHEN DATEPART(QUARTER, hd.ngayLapHD) = ? AND YEAR(hd.ngayLapHD) = ? "
                 + "               THEN ct.soLuong ELSE 0 END), 0) AS slQuyCu "
                 + "FROM ChiTietHoaDon ct "
                 + "JOIN HoaDon hd ON ct.hoaDonId = hd.id "
                 + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
                 + "WHERE sp.ten = ? AND hd.loaiHD = 'BAN_HANG'" +
                 " AND (hd.ghiChu IS NULL OR (hd.ghiChu NOT LIKE N'%Lưu nháp%' AND hd.ghiChu NOT LIKE N'%Đã hủy%'))";
         try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
             ps.setInt(1, currentQ);
             ps.setInt(2, year);
             ps.setInt(3, prevQ);
             ps.setInt(4, prevYear);
             ps.setString(5, tenSP);
             try (ResultSet rs = ps.executeQuery()) {
                 if (rs.next()) return new int[]{ rs.getInt("slQuyNay"), rs.getInt("slQuyCu") };
             }
         } catch (Exception e) { e.printStackTrace(); }
         return new int[]{0, 0};
     }

     // Lấy số lượng bán của SP trong tháng thang, cùng kỳ năm ngoái (year - 1)
     // Dùng để phát hiện mùa vụ: nếu cùng kỳ năm trước cũng thấp → bình thường theo mùa
     public int getSlBanCungKy(String tenSP, int thang, int year) {
         String sql = "SELECT ISNULL(SUM(ct.soLuong), 0) "
                 + "FROM ChiTietHoaDon ct "
                 + "JOIN HoaDon hd ON ct.hoaDonId = hd.id "
                 + "JOIN SanPham sp ON ct.sanPhamId = sp.id "
                 + "WHERE sp.ten = ? "
                 + "  AND MONTH(hd.ngayLapHD) = ? "
                 + "  AND YEAR(hd.ngayLapHD)  = ? "
                 + "  AND hd.loaiHD = 'BAN_HANG'"
                 + "  AND (hd.ghiChu IS NULL OR (hd.ghiChu NOT LIKE N'%Lưu nháp%' AND hd.ghiChu NOT LIKE N'%Đã hủy%'))";
         try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
             ps.setString(1, tenSP);
             ps.setInt(2, thang);
             ps.setInt(3, year - 1);
             try (ResultSet rs = ps.executeQuery()) {
                 if (rs.next()) return rs.getInt(1);
             }
         } catch (Exception e) { e.printStackTrace(); }
         return 0;
     }

     // Lấy dữ liệu đối chiếu doanh thu theo ca (theo filter): đếm HĐ bán/trả, tính giá gốc, KM, VAT
     // Trả về BUS_DoiChieuCa chứa các thông số tài chính chi tiết
     public Entity.DoiChieuCa layDoiChieuDoanhThuTheoCa(Entity.BoLocThongKe filter) {
    	    Entity.DoiChieuCa kq = new Entity.DoiChieuCa();
    	    StringBuilder sql = new StringBuilder(
    	        "SELECT "
    	        + "COUNT(DISTINCT CASE WHEN hd.loaiHD = 'BAN_HANG' THEN hd.id END) AS soHdBan, "
    	        + "COUNT(DISTINCT CASE WHEN hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') AND hd.ghiChu LIKE N'%Hoàn thành%' THEN hd.id END) AS soHdTra, "
    	        // Nhóm A: BAN_HANG + DOI_HANG xuất mới (soLuong > 0)
    	        + "ISNULL(SUM(CASE WHEN hd.loaiHD = 'BAN_HANG' OR (hd.loaiHD = 'DOI_HANG' AND ct.soLuong > 0) THEN dvl.gia * ABS(ct.soLuong) ELSE 0 END), 0) AS a_giaGoc, "
    	        + "ISNULL(SUM(CASE WHEN hd.loaiHD = 'BAN_HANG' OR (hd.loaiHD = 'DOI_HANG' AND ct.soLuong > 0) THEN (dvl.gia * ABS(ct.soLuong)) - ROUND(ABS(ct.thanhTien) / (1.0 + ISNULL(sp.thueVAT, 0) / 100.0), 0) ELSE 0 END), 0) AS a_khuyenMai, "
    	        + "ISNULL(SUM(CASE WHEN hd.loaiHD = 'BAN_HANG' OR (hd.loaiHD = 'DOI_HANG' AND ct.soLuong > 0) THEN ABS(ct.thanhTien) - ROUND(ABS(ct.thanhTien) / (1.0 + ISNULL(sp.thueVAT, 0) / 100.0), 0) ELSE 0 END), 0) AS a_vat, "
    	        // Nhóm B: ĐỔI/TRẢ HÀNG (soLuong < 0, hoàn thành)
    	        + "ISNULL(SUM(CASE WHEN hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') AND ct.soLuong < 0 AND hd.ghiChu LIKE N'%Hoàn thành%' THEN dvl.gia * ABS(ct.soLuong) ELSE 0 END), 0) AS b_giaGoc, "
    	        + "ISNULL(SUM(CASE WHEN hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') AND ct.soLuong < 0 AND hd.ghiChu LIKE N'%Hoàn thành%' THEN (dvl.gia * ABS(ct.soLuong)) - ROUND(ABS(ct.thanhTien) / (1.0 + ISNULL(sp.thueVAT, 0) / 100.0), 0) ELSE 0 END), 0) AS b_khuyenMai, "
    	        + "ISNULL(SUM(CASE WHEN hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') AND ct.soLuong < 0 AND hd.ghiChu LIKE N'%Hoàn thành%' THEN ABS(ct.thanhTien) - ROUND(ABS(ct.thanhTien) / (1.0 + ISNULL(sp.thueVAT, 0) / 100.0), 0) ELSE 0 END), 0) AS b_vat "
    	        + "FROM HoaDon hd "
    	        + "LEFT JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
    	        + "LEFT JOIN SanPham sp ON ct.sanPhamId = sp.id "
    	        + "LEFT JOIN DonViDoLuong dvl ON ct.donViDoLuongId = dvl.id AND ct.sanPhamId = dvl.sanPhamId "
    	        + "WHERE 1=1 "
    	        + "AND (hd.ghiChu IS NULL OR (hd.ghiChu NOT LIKE N'%Lưu nháp%' AND hd.ghiChu NOT LIKE N'%Đã hủy%')) "
    	    );

    	    // Gắn điều kiện filter
    	    if (filter != null) {
    	        if (filter.getMaNV() != null && !filter.getMaNV().isEmpty()) {
    	            sql.append(" AND hd.nhanVienId = '").append(filter.getMaNV()).append("' ");
    	        }
    	        if (filter.getFromDate() != null) {
    	            sql.append(" AND CAST(hd.ngayLapHD AS DATE) >= '").append(filter.getFromDate().toString()).append("' ");
    	        }
    	        if (filter.getToDate() != null) {
    	            sql.append(" AND CAST(hd.ngayLapHD AS DATE) <= '").append(filter.getToDate().toString()).append("' ");
    	        }
    	        if (filter.getStartTime() != null) {
    	            sql.append(" AND hd.ngayLapHD >= ? ");
    	        } else if (filter.getCa() != null && filter.getFromDate() == null && filter.getToDate() == null) {
    	            int ca = filter.getCa();
    	            if (ca == 1) sql.append(" AND DATEPART(HOUR, hd.ngayLapHD) BETWEEN 6 AND 13 ");
    	            else if (ca == 2) sql.append(" AND DATEPART(HOUR, hd.ngayLapHD) BETWEEN 14 AND 21 ");
    	            else if (ca == 3) sql.append(" AND (DATEPART(HOUR, hd.ngayLapHD) >= 22 OR DATEPART(HOUR, hd.ngayLapHD) < 6) ");
    	        }
    	    }

    	    try (Connection con = getConn(); java.sql.PreparedStatement ps = con.prepareStatement(sql.toString())) {
    	        if (filter != null && filter.getStartTime() != null) {
    	            ps.setTimestamp(1, java.sql.Timestamp.valueOf(filter.getStartTime()));
    	        }
    	        
    	        try (java.sql.ResultSet rs = ps.executeQuery()) {
    	            if (rs.next()) {
    	                kq.setSoHdBan(rs.getInt("soHdBan"));
    	                kq.setA_giaGocChuaThue(rs.getDouble("a_giaGoc"));
    	                kq.setA_khuyenMai(rs.getDouble("a_khuyenMai"));
    	                kq.setA_vat(rs.getDouble("a_vat"));

    	                kq.setSoHdTra(rs.getInt("soHdTra"));
    	                kq.setB_giaGocMonTra(rs.getDouble("b_giaGoc"));
    	                kq.setB_khuyenMaiHoanTra(rs.getDouble("b_khuyenMai"));
    	                kq.setB_vatHoanTra(rs.getDouble("b_vat"));
    	            }
    	        }
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	    }
    	    return kq;
    	}
     
     /** Phân bổ doanh thu theo giờ (0-23) theo bất kỳ filter kỳ nào (THANG/QUY/TUYCHINH).
      * Trả về List<{h(int), tienThucThu(double)}> */
     public List<Object[]> getRawHDGioTheoFilter(Entity.BoLocThongKe filter) {
         List<Object[]> result = new ArrayList<>();
         StringBuilder sql = new StringBuilder(
             "SELECT DATEPART(HOUR, hd.ngayLapHD) AS h, SUM(ABS(ct.thanhTien)) AS tienThucThu "
             + "FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId "
             + "WHERE hd.loaiHD = 'BAN_HANG' "
             + "AND (hd.ghiChu IS NULL OR (hd.ghiChu NOT LIKE N'%Lưu nháp%' AND hd.ghiChu NOT LIKE N'%Đã hủy%')) ");
         List<Object> params = new ArrayList<>();

         int year = (filter != null && filter.getYear() != null)
                    ? filter.getYear() : java.time.LocalDate.now().getYear();

         if (filter != null && "THANG".equals(filter.getModeLocThoiGian()) && filter.getMonth() != null) {
             sql.append(" AND YEAR(hd.ngayLapHD)=? AND MONTH(hd.ngayLapHD)=?");
             params.add(year); params.add(filter.getMonth());
         } else if (filter != null && "THANG".equals(filter.getModeLocThoiGian()) && filter.getMonth() == null) {
             // Cả năm
             sql.append(" AND YEAR(hd.ngayLapHD)=?");
             params.add(year);
         } else if (filter != null && "QUY".equals(filter.getModeLocThoiGian()) && filter.getQuarter() != null) {
             sql.append(" AND YEAR(hd.ngayLapHD)=? AND DATEPART(QUARTER,hd.ngayLapHD)=?");
             params.add(year); params.add(filter.getQuarter());
         } else if (filter != null && "TUYCHINH".equals(filter.getModeLocThoiGian()) && filter.getFromDate() != null) {
             sql.append(" AND CAST(hd.ngayLapHD AS DATE) BETWEEN ? AND ?");
             params.add(filter.getFromDate()); params.add(filter.getToDate());
         } else if (filter != null && "CANAM".equals(filter.getModeLocThoiGian())) {
             sql.append(" AND YEAR(hd.ngayLapHD)=?");
             params.add(year);
         } else {
             sql.append(" AND CAST(hd.ngayLapHD AS DATE) = CAST(GETDATE() AS DATE)");
         }

         if (filter != null && filter.getMaNV() != null && !filter.getMaNV().isEmpty()) {
             sql.append(" AND hd.nhanVienId=?");
             params.add(filter.getMaNV());
         }
         sql.append(" GROUP BY DATEPART(HOUR, hd.ngayLapHD)");

         try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
             for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
             try (ResultSet rs = ps.executeQuery()) {
                 while (rs.next()) result.add(new Object[]{ rs.getInt("h"), rs.getDouble("tienThucThu") });
             }
         } catch (Exception e) { e.printStackTrace(); }
         return result;
     }
}