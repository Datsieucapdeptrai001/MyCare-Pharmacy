package DAO;

import ConnectDB.ConnectDB;
import Entity.NhanVien;
import Enum.ChucVu;
import Enum.TrangThaiLamViec;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO_NhanVien – tương thích DB mới: NhanVien.id (PK), không còn cột nhanVien.
 */
public class DAO_NhanVien {

    public DAO_NhanVien() {}

    public List<NhanVien> layDSNhanVien() {
        List<NhanVien> ds = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return ds;
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM NhanVien")) {
            while (rs.next()) ds.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public boolean themNhanVien(NhanVien nv) {
        String sql = "INSERT INTO NhanVien (id, hoVaTen, soChungChiHanhNghe, sdt, email, chucVu, trangThaiLamViec) VALUES (?,?,?,?,?,?,?)";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nv.getNhanVien()); // Entity field = id
            ps.setString(2, nv.getHoVaTen());
            ps.setString(3, nv.getSoChungChiHanhNghe());
            ps.setString(4, nv.getSdt());
            ps.setString(5, nv.getEmail());
            ps.setString(6, nv.getChucVu().name());
            ps.setString(7, nv.getTrangThaiLamViec().name());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean capNhatNhanVien(NhanVien nv) {
        String sql = "UPDATE NhanVien SET hoVaTen=?,soChungChiHanhNghe=?,sdt=?,email=?,chucVu=?,trangThaiLamViec=? WHERE id=?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nv.getHoVaTen());
            ps.setString(2, nv.getSoChungChiHanhNghe());
            ps.setString(3, nv.getSdt());
            ps.setString(4, nv.getEmail());
            ps.setString(5, nv.getChucVu().name());
            ps.setString(6, nv.getTrangThaiLamViec().name());
            ps.setString(7, nv.getNhanVien()); // id
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public NhanVien layNhanVienTheoMa(String maId) {
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM NhanVien WHERE id=?")) {
            ps.setString(1, maId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private NhanVien mapRow(ResultSet rs) throws SQLException {
        NhanVien nv = new NhanVien();
        nv.setNhanVien(rs.getString("id"));          // DB cột "id" → Entity field "nhanVien"
        nv.setHoVaTen(rs.getString("hoVaTen"));
        nv.setSoChungChiHanhNghe(rs.getString("soChungChiHanhNghe"));
        nv.setSdt(rs.getString("sdt"));
        nv.setEmail(rs.getString("email"));
        try { nv.setChucVu(ChucVu.valueOf(rs.getString("chucVu"))); } catch (Exception ignored) {}
        try { nv.setTrangThaiLamViec(TrangThaiLamViec.valueOf(rs.getString("trangThaiLamViec"))); } catch (Exception ignored) {}
        return nv;
    }
}
