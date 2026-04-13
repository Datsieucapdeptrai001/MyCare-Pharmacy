package DAO;

import ConnectDB.ConnectDB;
import Entity.NhanVien;
import Entity.TaiKhoan;
import Enum.ChucVu;
import Enum.TrangThaiLamViec;
import Enum.VaiTro;

import java.sql.*;

public class DAO_TaiKhoan {

    public DAO_TaiKhoan() {}

    /**
     * Lấy TaiKhoan + NhanVien đầy đủ theo tên đăng nhập.
     * DB mới: NhanVien.id (PK), không phải nhanVien
     */
    public TaiKhoan getTaiKhoan(String tenDangNhap) {
        TaiKhoan tk = null;
        // JOIN dùng nv.id (PK mới của NhanVien)
        String sql =
            "SELECT tk.id, tk.nhanVienId, tk.vaiTro, tk.tenDangNhap, tk.matKhau, " +
            "nv.hoVaTen, nv.chucVu, nv.sdt, nv.email, nv.soChungChiHanhNghe, nv.trangThaiLamViec " +
            "FROM TaiKhoan tk " +
            "LEFT JOIN NhanVien nv ON tk.nhanVienId = nv.id " +
            "WHERE tk.tenDangNhap = ?";

        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, tenDangNhap);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    tk = new TaiKhoan();
                    tk.setId(rs.getString("id"));
                    tk.setVaiTro(VaiTro.valueOf(rs.getString("vaiTro")));
                    tk.setTenDangNhap(rs.getString("tenDangNhap"));
                    tk.setMatKhau(rs.getString("matKhau"));

                    NhanVien nv = new NhanVien();
                    // Lưu ID nhân viên vào trường nhanVien của Entity
                    nv.setNhanVien(rs.getString("nhanVienId"));
                    String hoVaTen = rs.getString("hoVaTen");
                    nv.setHoVaTen(hoVaTen != null ? hoVaTen : tenDangNhap);
                    nv.setSdt(rs.getString("sdt"));
                    nv.setEmail(rs.getString("email"));
                    nv.setSoChungChiHanhNghe(rs.getString("soChungChiHanhNghe"));
                    try {
                        String cv = rs.getString("chucVu");
                        if (cv != null) nv.setChucVu(ChucVu.valueOf(cv));
                    } catch (Exception ignored) {}
                    try {
                        String tt = rs.getString("trangThaiLamViec");
                        if (tt != null) nv.setTrangThaiLamViec(TrangThaiLamViec.valueOf(tt));
                    } catch (Exception ignored) {}
                    tk.setNhanVienId(nv);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tk;
    }

    public boolean capNhatMatKhau(String tenDangNhap, String matKhauMoi) {
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE tenDangNhap = ?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, matKhauMoi);
            pst.setString(2, tenDangNhap);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean themTaiKhoan(TaiKhoan tk) {
        String sql = "INSERT INTO TaiKhoan (id, nhanVienId, vaiTro, tenDangNhap, matKhau) VALUES (?, ?, ?, ?, ?)";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, tk.getId());
            pst.setString(2, tk.getNhanVienId().getNhanVien());
            pst.setString(3, tk.getVaiTro().name());
            pst.setString(4, tk.getTenDangNhap());
            pst.setString(5, tk.getMatKhau());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean checkTrungTenDangNhap(String tenDangNhap) {
        String sql = "SELECT COUNT(*) FROM TaiKhoan WHERE tenDangNhap = ?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, tenDangNhap);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
