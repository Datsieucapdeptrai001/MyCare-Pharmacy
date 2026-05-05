package BUS;

import DAO.DAO_TaiKhoan;
import Entity.NhanVien;
import Entity.TaiKhoan;
import Utils.PasswordUtils;
import java.util.List;

public class BUS_TaiKhoan {
    private DAO_TaiKhoan daoTaiKhoan;

    public BUS_TaiKhoan() {
        this.daoTaiKhoan = new DAO_TaiKhoan();
    }

    // 1. TỰ ĐỘNG XÁC THỰC (Băm pass nhập vào và so sánh với DB)
    public boolean authenticate(String tenDangNhap, String matKhauNhapVao) {
        TaiKhoan tk = daoTaiKhoan.getTaiKhoan(tenDangNhap);
        if (tk == null) return false;
        
        // Gọi hàm kiểm tra băm bảo mật
        return PasswordUtils.verifyPassword(matKhauNhapVao, tk.getMatKhau());
    }

    // 2. TỰ ĐỘNG BĂM KHI THÊM MỚI TỪ QUẢN LÝ
    public boolean themTaiKhoanMoi(TaiKhoan tk) {
        if (daoTaiKhoan.checkTrungTenDangNhap(tk.getTenDangNhap())) {
            return false;
        }
        String matKhauTho = tk.getMatKhau();
        if (validateMatKhauMoi(matKhauTho)) {
            // TỰ ĐỘNG BĂM trước khi đưa xuống DAO
            tk.setMatKhau(PasswordUtils.hashPassword(matKhauTho));
            return daoTaiKhoan.themTaiKhoan(tk);
        }
        return false;
    }
    
 // 3. TỰ ĐỘNG BĂM KHI SỬA TÀI KHOẢN TỪ FORM QUẢN LÝ
    public boolean capNhatTaiKhoanTheoMaNV(String maNV, String tenDangNhap, String matKhauNhapVao) {
        String matKhauLuuDB = matKhauNhapVao;
        
        // Quản lý để trống ô mật khẩu -> Giữ nguyên pass cũ
        if (matKhauNhapVao == null || matKhauNhapVao.isEmpty()) {
            TaiKhoan tkCu = daoTaiKhoan.layTaiKhoanTheoMaNV(maNV);
            if (tkCu != null) {
                matKhauLuuDB = tkCu.getMatKhau();
            }
        } 
        // Quản lý gõ pass mới (Độ dài < 60 ký tự) -> Mang đi băm!
        else if (matKhauNhapVao.length() < 60) {
            matKhauLuuDB = PasswordUtils.hashPassword(matKhauNhapVao);
        }
        
        return daoTaiKhoan.capNhatTaiKhoanTheoMaNV(maNV, tenDangNhap, matKhauLuuDB);
    }

    // 4. ĐỔI MẬT KHẨU CÁ NHÂN
    public boolean doiMatKhau(String tenDangNhap, String matKhauHienTai, String matKhauMoiTho) {
        if (authenticate(tenDangNhap, matKhauHienTai)) { 
            if (validateMatKhauMoi(matKhauMoiTho)) { 
                return daoTaiKhoan.capNhatMatKhau(tenDangNhap, PasswordUtils.hashPassword(matKhauMoiTho));
            }
        }
        return false;
    }

    // Khôi phục mật khẩu qua Email
    public boolean capNhatMatKhauTheoEmail(String email, String matKhauMoiTho) {
        if(validateMatKhauMoi(matKhauMoiTho)) {
            return daoTaiKhoan.capNhatMatKhauTheoEmail(email, PasswordUtils.hashPassword(matKhauMoiTho));
        }
        return false;
    }

    public boolean validateMatKhauMoi(String matKhauMoi) {
        if (matKhauMoi == null || matKhauMoi.length() < 6) return false;
        boolean hasLetter = false, hasDigit = false;
        for (char c : matKhauMoi.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        return hasLetter && hasDigit;
    }

    public TaiKhoan getTaiKhoanDayDu(String tenDangNhap) {
        TaiKhoan tk = daoTaiKhoan.getTaiKhoan(tenDangNhap);
        if (tk == null || tk.getNhanVienId() == null) return tk;
        try {
            DAO.DAO_NhanVien daoNV = new DAO.DAO_NhanVien();
            String maNV = tk.getNhanVienId().getNhanVien();
            List<Entity.NhanVien> dsNV = daoNV.layDSNhanVien();
            for (Entity.NhanVien nv : dsNV) {
                if (nv.getNhanVien().equals(maNV)) {
                    tk.setNhanVienId(nv);
                    break;
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return tk;
    }
}