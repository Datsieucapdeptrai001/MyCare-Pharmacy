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
     * Lấy danh sách hóa đơn tiền mặt trong một ca làm việc (dữ liệu thô).
     * Mỗi phần tử: Object[]{ loaiHD (String), tongTienHienTai (double), tongTienGoc (double) }
     * Tính toán nghiệp vụ (BAN_HANG / TRA_HANG / DOI_HANG) do BUS đảm nhiệm.
     *
     * @param maCa  id của CaLamViec
     * @return      list row thô
     */
    public List<Object[]> getHoaDonTienMatTrongCa(String maCa) {
        List<Object[]> result = new ArrayList<>();
        String sql =
            "SELECT hd.loaiHD, " +
            "    ISNULL((SELECT SUM(thanhTien) FROM ChiTietHoaDon WHERE hoaDonId = hd.id), 0)          AS tongTienHienTai, " +
            "    ISNULL((SELECT SUM(thanhTien) FROM ChiTietHoaDon WHERE hoaDonId = hd.hoaDonGocId), 0) AS tongTienGoc " +
            "FROM CaLamViec clv " +
            "JOIN HoaDon hd " +
            "    ON  hd.nhanVienId = clv.nhanVienId " +
            "    AND hd.ngayLapHD >= clv.thoiGianBatDau " +
            "    AND (clv.thoiGianKetThuc IS NULL OR hd.ngayLapHD <= clv.thoiGianKetThuc) " +
            "    AND hd.phuongThucThanhToan = 'TIEN_MAT' " +
            "WHERE clv.id = ?";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maCa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[]{
                        rs.getString("loaiHD"),
                        rs.getDouble("tongTienHienTai"),
                        rs.getDouble("tongTienGoc")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
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
}