package BUS;

import DAO.DAO_TaiKhoan;
import Entity.NhanVien;
import Entity.TaiKhoan;

public class BUS_TaiKhoan {
    private DAO_TaiKhoan daoTaiKhoan;

    public BUS_TaiKhoan() {
        this.daoTaiKhoan = new DAO_TaiKhoan();
    }

    // Nghiệp vụ: Xác thực đăng nhập
    public boolean authenticate(String tenDangNhap, String matKhau) {
        TaiKhoan tk = daoTaiKhoan.getTaiKhoan(tenDangNhap);
        
        if (tk == null) {
            System.out.println("Tên đăng nhập không tồn tại.");
            return false;
        }
        
        // So sánh mật khẩu (Lưu ý: Thực tế nên dùng mã hóa HASH như BCrypt, ở đây so sánh chuỗi cơ bản)
        if (tk.getMatKhau().equals(matKhau)) {
            return true; // Đăng nhập thành công
        } else {
            System.out.println("Mật khẩu không chính xác.");
            return false;
        }
    }

    // Nghiệp vụ: Kiểm tra độ mạnh của mật khẩu mới
    public boolean validateMatKhauMoi(String matKhauMoi) {
        if (matKhauMoi == null || matKhauMoi.length() < 6) {
            System.out.println("Lỗi: Mật khẩu phải có ít nhất 6 ký tự.");
            return false;
        }
        
        // Kiểm tra xem có chứa ít nhất 1 chữ cái và 1 chữ số hay không
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : matKhauMoi.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        
        if (!hasLetter || !hasDigit) {
            System.out.println("Lỗi: Mật khẩu phải bao gồm cả chữ cái và số.");
            return false;
        }
        
        return true;
    }

    // Nghiệp vụ: Xử lý khi người dùng chọn "Quên mật khẩu"
    public boolean xuLyQuenMatKhau(String tenDangNhap, String email) {
    	TaiKhoan tk = daoTaiKhoan.getTaiKhoan(tenDangNhap);

        if (tk == null) {
            System.out.println("Không tìm thấy tài khoản.");
            return false;
        }

        NhanVien nv = tk.getNhanVienId();
        if (nv == null || nv.getEmail() == null || nv.getEmail().trim().isEmpty()) {
            System.out.println("Tài khoản chưa có email để khôi phục mật khẩu.");
            return false;
        }

        if (email == null || !nv.getEmail().equalsIgnoreCase(email.trim())) {
            System.out.println("Email khôi phục không khớp.");
            return false;
        }

        System.out.println("Chức năng quên mật khẩu chưa được triển khai gửi OTP/email.");
        return false;
    }

    // Nghiệp vụ: Phân quyền sau khi đăng nhập thành công
    public String kiemTraQuyenTruyCap(String tenDangNhap) {
        TaiKhoan tk = daoTaiKhoan.getTaiKhoan(tenDangNhap);
        
        if (tk != null && tk.getVaiTro() != null) {
            return tk.getVaiTro().name(); // Ví dụ trả về: "QUAN_LY" hoặc "NHAN_VIEN_BAN_HANG"
        }
        
        return "GUEST"; 
    }
    
  
    public boolean doiMatKhau(String tenDangNhap, String matKhauHienTai, String matKhauMoi) {
        if (authenticate(tenDangNhap, matKhauHienTai)) { 
            if (validateMatKhauMoi(matKhauMoi)) { 
                return daoTaiKhoan.capNhatMatKhau(tenDangNhap, matKhauMoi);
            }
        }
        return false;
    }
    public boolean themTaiKhoanMoi(TaiKhoan tk) {
        if (daoTaiKhoan.checkTrungTenDangNhap(tk.getTenDangNhap())) {
            System.out.println("Lỗi: Tên đăng nhập đã tồn tại.");
            return false;
        }
        if (validateMatKhauMoi(tk.getMatKhau())) {
            return daoTaiKhoan.themTaiKhoan(tk);
        }
        return false;
    }
    public TaiKhoan getTaiKhoanDayDu(String tenDangNhap) {
        TaiKhoan tk = daoTaiKhoan.getTaiKhoan(tenDangNhap);
        if (tk == null || tk.getNhanVienId() == null) return tk;

        // Load thông tin đầy đủ của NhanVien
        try {
            DAO.DAO_NhanVien daoNV = new DAO.DAO_NhanVien();
            String maNV = tk.getNhanVienId().getNhanVien();
            java.util.List<Entity.NhanVien> dsNV = daoNV.layDSNhanVien();
            for (Entity.NhanVien nv : dsNV) {
                if (nv.getNhanVien().equals(maNV)) {
                    tk.setNhanVienId(nv);
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return tk;
    }
}