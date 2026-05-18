package DAO;

import ConnectDB.ConnectDB;
import Entity.NhanVien;
import Entity.TaiKhoan;
import Enumeration.ChucVu;
import Enumeration.TrangThaiLamViec;
import Enumeration.VaiTro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DAO_TaiKhoan {

    public DAO_TaiKhoan() {
    }

    /**
     * Lấy tài khoản + thông tin nhân viên theo tên đăng nhập
     */
    public TaiKhoan getTaiKhoan(String input) {
        TaiKhoan tk = null;

        // FIX: Thêm "OR tk.nhanVienId = ?" để chấp nhận cả Mã nhân viên lẫn Tên đăng nhập
        String sql =
                "SELECT tk.id, tk.nhanVienId, tk.vaiTro, tk.tenDangNhap, tk.matKhau, " +
                "nv.hoVaTen, nv.chucVu, nv.sdt, nv.email, nv.soChungChiHanhNghe, nv.trangThaiLamViec " +
                "FROM TaiKhoan tk " +
                "LEFT JOIN NhanVien nv ON tk.nhanVienId = nv.id " +
                "WHERE tk.tenDangNhap = ? OR tk.nhanVienId = ?"; // <-- SỬA Ở ĐÂY

        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, input);
            pst.setString(2, input); // <-- THÊM DÒNG NÀY để truyền tham số thứ 2

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    tk = mapTaiKhoan(rs, input);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tk;
    }

    /**
     * Lấy tài khoản + thông tin nhân viên theo email
     */
    public TaiKhoan getTaiKhoanTheoEmail(String email) {
        TaiKhoan tk = null;

        String sql =
                "SELECT tk.id, tk.nhanVienId, tk.vaiTro, tk.tenDangNhap, tk.matKhau, " +
                "nv.hoVaTen, nv.chucVu, nv.sdt, nv.email, nv.soChungChiHanhNghe, nv.trangThaiLamViec " +
                "FROM TaiKhoan tk " +
                "LEFT JOIN NhanVien nv ON tk.nhanVienId = nv.id " +
                "WHERE nv.email = ?";

        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, email);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    tk = mapTaiKhoan(rs, rs.getString("tenDangNhap"));
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
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra email có tồn tại trong hệ thống không
     */
    public boolean kiemTraEmailTonTai(String email) {
        String sql =
                "SELECT COUNT(*) " +
                "FROM TaiKhoan tk " +
                "INNER JOIN NhanVien nv ON tk.nhanVienId = nv.id " +
                "WHERE nv.email = ?";

        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, email);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Cập nhật mật khẩu theo email
     */
    public boolean capNhatMatKhauTheoEmail(String email, String matKhauMoi) {
        String sql =
                "UPDATE TaiKhoan " +
                "SET matKhau = ? " +
                "WHERE nhanVienId IN (SELECT id FROM NhanVien WHERE email = ?)";

        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, matKhauMoi);
            pst.setString(2, email);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkTrungTenDangNhap(String tenDangNhap) {
        String sql = "SELECT COUNT(*) FROM TaiKhoan WHERE tenDangNhap = ?";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, tenDangNhap);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private TaiKhoan mapTaiKhoan(ResultSet rs, String tenDangNhapMacDinh) throws SQLException {
        TaiKhoan tk = new TaiKhoan();
        tk.setId(rs.getString("id"));

        String vaiTro = rs.getString("vaiTro");
        if (vaiTro != null) {
            tk.setVaiTro(VaiTro.valueOf(vaiTro));
        }

        tk.setTenDangNhap(rs.getString("tenDangNhap"));
        tk.setMatKhau(rs.getString("matKhau"));

        NhanVien nv = new NhanVien();
        nv.setNhanVien(rs.getString("nhanVienId"));

        String hoVaTen = rs.getString("hoVaTen");
        nv.setHoVaTen(hoVaTen != null ? hoVaTen : tenDangNhapMacDinh);
        nv.setSdt(rs.getString("sdt"));
        nv.setEmail(rs.getString("email"));
        nv.setSoChungChiHanhNghe(rs.getString("soChungChiHanhNghe"));

        try {
            String chucVu = rs.getString("chucVu");
            if (chucVu != null) {
                nv.setChucVu(ChucVu.valueOf(chucVu));
            }
        } catch (Exception ignored) {
        }

        try {
            String trangThaiLamViec = rs.getString("trangThaiLamViec");
            if (trangThaiLamViec != null) {
                nv.setTrangThaiLamViec(TrangThaiLamViec.valueOf(trangThaiLamViec));
            }
        } catch (Exception ignored) {
        }

        tk.setNhanVienId(nv);
        return tk;
    }

    // ================== CÁC HÀM BỔ SUNG MỚI CHO FORM NHÂN VIÊN ==================

    /**
     * Lấy tài khoản dựa trên mã Nhân Viên (Dùng khi click xem chi tiết/sửa nhân viên)
     */
    public TaiKhoan layTaiKhoanTheoMaNV(String maNV) {
        TaiKhoan tk = null;
        String sql = "SELECT * FROM TaiKhoan WHERE nhanVienId = ?";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maNV);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    tk = new TaiKhoan();
                    tk.setId(rs.getString("id"));
                    tk.setTenDangNhap(rs.getString("tenDangNhap"));
                    tk.setMatKhau(rs.getString("matKhau"));
                    
                    String vaiTroStr = rs.getString("vaiTro");
                    if (vaiTroStr != null) tk.setVaiTro(VaiTro.valueOf(vaiTroStr));
                    
                    NhanVien nv = new NhanVien();
                    nv.setNhanVien(rs.getString("nhanVienId")); 
                    tk.setNhanVienId(nv);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tk;
    }

    /**
     * Cập nhật tên đăng nhập và mật khẩu dựa theo mã nhân viên
     */
    public boolean capNhatTaiKhoanTheoMaNV(String maNV, String tenDangNhap, String matKhau) {
        String sql = "UPDATE TaiKhoan SET tenDangNhap = ?, matKhau = ? WHERE nhanVienId = ?";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, tenDangNhap);
            pst.setString(2, matKhau);
            pst.setString(3, maNV);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}