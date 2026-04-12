package DAO;

import ConnectDB.ConnectDB;
import Entity.NhanVien;
import Entity.TaiKhoan;
import Enum.VaiTro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DAO_TaiKhoan {

    public DAO_TaiKhoan() {
    }

    // Lấy thông tin tài khoản dựa trên Tên đăng nhập
    public TaiKhoan getTaiKhoan(String tenDangNhap) {
        TaiKhoan tk = null;
        String sql = "SELECT * FROM TaiKhoan WHERE tenDangNhap = ?";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, tenDangNhap);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    tk = new TaiKhoan();
                    tk.setId(rs.getString("id"));
                    
                    // Gán ID nhân viên
                    NhanVien nv = new NhanVien();
                    nv.setNhanVien(rs.getString("nhanVienId"));
                    tk.setNhanVienId(nv);
                    
                    tk.setVaiTro(VaiTro.valueOf(rs.getString("vaiTro")));
                    tk.setTenDangNhap(rs.getString("tenDangNhap"));
                    tk.setMatKhau(rs.getString("matKhau"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tk;
    }

    // Cập nhật mật khẩu mới cho tài khoản
    public boolean capNhatMatKhau(String tenDangNhap, String matKhauMoi) {
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE tenDangNhap = ?";
        int n = 0;
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, matKhauMoi);
            pst.setString(2, tenDangNhap);
            
            n = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    // Thêm một tài khoản mới vào hệ thống (Khi tiếp nhận nhân viên mới)
    public boolean themTaiKhoan(TaiKhoan tk) {
        String sql = "INSERT INTO TaiKhoan (id, nhanVienId, vaiTro, tenDangNhap, matKhau) VALUES (?, ?, ?, ?, ?)";
        int n = 0;
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, tk.getId());
            pst.setString(2, tk.getNhanVienId().getNhanVien());
            pst.setString(3, tk.getVaiTro().name());
            pst.setString(4, tk.getTenDangNhap());
            pst.setString(5, tk.getMatKhau());
            
            n = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    // Kiểm tra xem tên đăng nhập đã có người sử dụng chưa
    public boolean checkTrungTenDangNhap(String tenDangNhap) {
        String sql = "SELECT COUNT(*) FROM TaiKhoan WHERE tenDangNhap = ?";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, tenDangNhap);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0; // Trả về true nếu đã tồn tại
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}