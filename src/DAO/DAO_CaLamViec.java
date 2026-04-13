package DAO;

import ConnectDB.ConnectDB;
import Entity.CaLamViec;
import Entity.NhanVien;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO_CaLamViec – tương thích DB mới có cột loaiCa, ghiChuKetCa.
 */
public class DAO_CaLamViec {

    public DAO_CaLamViec() {}

    public boolean themCa(CaLamViec ca) {
        String sql = "INSERT INTO CaLamViec (id, nhanVienId, thoiGianBatDau, thoiGianKetThuc, " +
                     "tienHeThongGhiNhan, tienDauCa, tienKetCa, loaiCa, ghiChuKetCa) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ca.getId());
            ps.setString(2, ca.getNhanVienId().getNhanVien()); // nhanVienId = NhanVien.id
            ps.setTimestamp(3, Timestamp.valueOf(ca.getThoiGianBatDau()));
            if (ca.getThoiGianKetThuc() != null)
                ps.setTimestamp(4, Timestamp.valueOf(ca.getThoiGianKetThuc()));
            else ps.setNull(4, Types.TIMESTAMP);
            ps.setDouble(5, ca.getTienHeThongGhiNhan());
            ps.setDouble(6, ca.getTienDauCa());
            ps.setDouble(7, ca.getTienKetCa());
            ps.setInt(8, 0); // loaiCa default
            ps.setNull(9, Types.NVARCHAR); // ghiChuKetCa
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean capNhatCa(CaLamViec ca) {
        String sql = "UPDATE CaLamViec SET thoiGianKetThuc=?, tienHeThongGhiNhan=?, tienKetCa=?, ghiChuKetCa=? WHERE id=?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (ca.getThoiGianKetThuc() != null)
                ps.setTimestamp(1, Timestamp.valueOf(ca.getThoiGianKetThuc()));
            else ps.setNull(1, Types.TIMESTAMP);
            ps.setDouble(2, ca.getTienHeThongGhiNhan());
            ps.setDouble(3, ca.getTienKetCa());
            ps.setNull(4, Types.NVARCHAR);
            ps.setString(5, ca.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public CaLamViec getCaHienTai(String maNhanVien) {
        String sql = "SELECT * FROM CaLamViec WHERE nhanVienId=? AND thoiGianKetThuc IS NULL ORDER BY thoiGianBatDau DESC";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNhanVien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<CaLamViec> getLichSuCa() {
        List<CaLamViec> ds = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM CaLamViec ORDER BY thoiGianBatDau DESC")) {
            while (rs.next()) ds.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
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
        return ca;
    }
}
