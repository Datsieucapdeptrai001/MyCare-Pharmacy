package DAO;

import ConnectDB.ConnectDB;
import Entity.CaLamViec;
import Entity.NhanVien;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_CaLamViec {

    public DAO_CaLamViec() {}

    /**
     * Tính tổng tiền mặt hệ thống ghi nhận trong két sắt cho một ca làm việc.
     *
     * Công thức:
     *   tienHeThong = tienDauCa
     *               + Σ thanhTien (BAN_HANG / TIEN_MAT)        ← tiền vào
     *               + Σ (thanhTien mới − thanhTien cũ) (DOI_HANG / TIEN_MAT)
     *                     > 0 → khách bù thêm (tiền vào)
     *                     < 0 → tiệm hoàn lại (tiền ra)
     *               − Σ thanhTien (TRA_HANG / TIEN_MAT)        ← tiền ra
     *
     * Lưu ý thiết kế SQL:
     * - Dùng LEFT JOIN để ca không có hóa đơn nào vẫn trả về tienDauCa.
     * - Điều kiện phuongThucThanhToan đặt trong ON để không loại dòng NULL.
     * - ISNULL(..., 0) ở mọi tầng SUM chống NullPointerException.
     * - Correlated sub-query cho ChiTietHoaDon gốc dùng hoaDonGocId của DOI_HANG.
     *
     * @param maCa  id của CaLamViec cần tính
     * @return      số tiền hệ thống ghi nhận (tienHeThongGhiNhan)
     */
    public double tinhTienMatThucTeTrongCa(String maCa) {
        String sql =
            "SELECT " +
            "    clv.tienDauCa + ISNULL( " +
            "        SUM( " +
            "            CASE hd.loaiHD " +
            "                WHEN 'BAN_HANG' THEN ( " +
            "                    SELECT ISNULL(SUM(ct.thanhTien), 0) " +
            "                    FROM   ChiTietHoaDon ct " +
            "                    WHERE  ct.hoaDonId = hd.id " +
            "                ) " +
            "                WHEN 'DOI_HANG' THEN ( " +
            "                    SELECT ISNULL(SUM(ct_moi.thanhTien), 0) " +
            "                    FROM   ChiTietHoaDon ct_moi " +
            "                    WHERE  ct_moi.hoaDonId = hd.id " +
            "                ) - ( " +
            "                    SELECT ISNULL(SUM(ct_cu.thanhTien), 0) " +
            "                    FROM   ChiTietHoaDon ct_cu " +
            "                    WHERE  ct_cu.hoaDonId = hd.hoaDonGocId " +
            "                ) " +
            "                WHEN 'TRA_HANG' THEN -( " +
            "                    SELECT ISNULL(SUM(ct.thanhTien), 0) " +
            "                    FROM   ChiTietHoaDon ct " +
            "                    WHERE  ct.hoaDonId = hd.id " +
            "                ) " +
            "                ELSE 0 " +
            "            END " +
            "        ), 0 " +
            "    ) AS tienHeThongGhiNhan " +
            "FROM  CaLamViec clv " +
            // LEFT JOIN: nếu ca chưa có hóa đơn nào vẫn trả về tienDauCa
            "LEFT  JOIN HoaDon hd " +
            "    ON  hd.nhanVienId          = clv.nhanVienId " +
            "    AND hd.ngayLapHD           >= clv.thoiGianBatDau " +
            // Ca đang mở (thoiGianKetThuc IS NULL) → lấy hết đến hiện tại
            "    AND (clv.thoiGianKetThuc IS NULL OR hd.ngayLapHD <= clv.thoiGianKetThuc) " +
            // Lọc TIEN_MAT trong ON để không làm mất dòng LEFT JOIN
            "    AND hd.phuongThucThanhToan = 'TIEN_MAT' " +
            "WHERE clv.id = ? " +
            "GROUP BY clv.tienDauCa";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maCa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("tienHeThongGhiNhan");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Trường hợp không tìm thấy ca → trả 0
        return 0.0;
    }

    public Entity.CaLamViec layCaChuaDongCuaNhanVien(String maNV) {
        Entity.CaLamViec ca = null;
        String sql = "SELECT * FROM CaLamViec WHERE nhanVienId = ? AND thoiGianKetThuc IS NULL";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maNV);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                ca = new Entity.CaLamViec();
                ca.setId(rs.getString("id"));
                ca.setThoiGianBatDau(rs.getTimestamp("thoiGianBatDau").toLocalDateTime());
                ca.setTienDauCa(rs.getDouble("tienDauCa"));
                ca.setLoaiCa(rs.getInt("loaiCa"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ca;
    }

    public boolean themCa(CaLamViec ca) {
        String sql = "INSERT INTO CaLamViec (id, nhanVienId, thoiGianBatDau, thoiGianKetThuc, " +
                     "tienHeThongGhiNhan, tienDauCa, tienKetCa, loaiCa, ghiChuKetCa) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ca.getId());
            ps.setString(2, ca.getNhanVienId().getNhanVien());
            ps.setTimestamp(3, Timestamp.valueOf(ca.getThoiGianBatDau()));
            if (ca.getThoiGianKetThuc() != null)
                ps.setTimestamp(4, Timestamp.valueOf(ca.getThoiGianKetThuc()));
            else
                ps.setNull(4, Types.TIMESTAMP);
            ps.setDouble(5, ca.getTienHeThongGhiNhan());
            ps.setDouble(6, ca.getTienDauCa());
            ps.setDouble(7, ca.getTienKetCa());
            ps.setInt(8, ca.getLoaiCa());
            ps.setString(9, ca.getGhiChuKetCa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatCa(CaLamViec ca) {
        String sql = "UPDATE CaLamViec SET thoiGianKetThuc=?, tienHeThongGhiNhan=?, " +
                     "tienKetCa=?, tienDauCa=?, ghiChuKetCa=? WHERE id=?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (ca.getThoiGianKetThuc() != null)
                ps.setTimestamp(1, Timestamp.valueOf(ca.getThoiGianKetThuc()));
            else
                ps.setNull(1, Types.TIMESTAMP);
            ps.setDouble(2, ca.getTienHeThongGhiNhan());
            ps.setDouble(3, ca.getTienKetCa());
            ps.setDouble(4, ca.getTienDauCa());
            ps.setString(5, ca.getGhiChuKetCa());
            ps.setString(6, ca.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public CaLamViec getCaHienTai(String maNhanVien) {
        String sql = "SELECT * FROM CaLamViec WHERE nhanVienId=? AND thoiGianKetThuc IS NULL " +
                     "ORDER BY thoiGianBatDau DESC";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<CaLamViec> getLichSuCa() {
        List<CaLamViec> ds = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT * FROM CaLamViec ORDER BY thoiGianBatDau DESC")) {
            while (rs.next()) ds.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    private CaLamViec mapRow(ResultSet rs) throws SQLException {
        CaLamViec ca = new CaLamViec();
        ca.setId(rs.getString("id"));

        NhanVien nv = new NhanVien();
        nv.setNhanVien(rs.getString("nhanVienId"));
        ca.setNhanVienId(nv);

        ca.setThoiGianBatDau(rs.getTimestamp("thoiGianBatDau").toLocalDateTime());
        Timestamp ket = rs.getTimestamp("thoiGianKetThuc");
        if (ket != null) ca.setThoiGianKetThuc(ket.toLocalDateTime());

        ca.setTienHeThongGhiNhan(rs.getDouble("tienHeThongGhiNhan"));
        ca.setTienDauCa(rs.getDouble("tienDauCa"));
        ca.setTienKetCa(rs.getDouble("tienKetCa"));
        ca.setLoaiCa(rs.getInt("loaiCa"));
        ca.setGhiChuKetCa(rs.getString("ghiChuKetCa"));

        return ca;
    }

    public boolean capNhatTienDauCa(String idCa, double tienDauCaMoi) {
        String sql = "UPDATE CaLamViec SET tienDauCa = ? WHERE id = ?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, tienDauCaMoi);
            ps.setString(2, idCa);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy ca làm việc đã đóng gần nhất của một nhân viên.
     * Dùng cho tính năng xem lại Bill Kết Ca ở ManHinhThongKe.
     *
     * @param maNV  mã nhân viên
     * @return      CaLamViec đã đóng gần nhất, hoặc null nếu không có
     */
    public CaLamViec getCaDaKetThucGanNhat(String maNV) {
        String sql = "SELECT TOP 1 * FROM CaLamViec " +
                     "WHERE nhanVienId = ? AND thoiGianKetThuc IS NOT NULL " +
                     "ORDER BY thoiGianKetThuc DESC";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}