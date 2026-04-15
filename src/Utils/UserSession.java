package Utils;

import Entity.CaLamViec;
import Entity.TaiKhoan;
import Enumeration.VaiTro;

/**
 * Singleton lưu toàn bộ trạng thái phiên đăng nhập.
 */
public class UserSession {

    private static UserSession instance;
    private TaiKhoan taiKhoan;
    private CaLamViec caHienTai;   // Ca đang mở (null nếu ADMIN)
    private long tienDauCa = 0;     // Tiền đầu ca do nhân viên khai
    private int  loaiCa    = 0;     // 0=Sáng, 1=Chiều, 2=Tối

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) instance = new UserSession();
        return instance;
    }

    // ── Tài khoản ──────────────────────────────────────────
    public void setTaiKhoan(TaiKhoan tk) { this.taiKhoan = tk; }
    public TaiKhoan getTaiKhoan()        { return taiKhoan; }

    public boolean isAdmin() {
        return taiKhoan != null && taiKhoan.getVaiTro() == VaiTro.ADMIN;
    }

    public String getTenHienThi() {
        if (taiKhoan != null && taiKhoan.getNhanVienId() != null
                && taiKhoan.getNhanVienId().getHoVaTen() != null) {
            return taiKhoan.getNhanVienId().getHoVaTen();
        }
        return taiKhoan != null ? taiKhoan.getTenDangNhap() : "Người dùng";
    }

    public String getInitials() {
        String ten = getTenHienThi();
        String[] parts = ten.trim().split("\\s+");
        if (parts.length >= 2)
            return (parts[0].substring(0,1) + parts[parts.length-1].substring(0,1)).toUpperCase();
        return ten.isEmpty() ? "ND" : ten.substring(0, Math.min(2, ten.length())).toUpperCase();
    }

    public String getChucVuHienThi() {
        if (taiKhoan != null && taiKhoan.getNhanVienId() != null
                && taiKhoan.getNhanVienId().getChucVu() != null)
            return taiKhoan.getNhanVienId().getChucVu().name().replace("_", " ");
        return isAdmin() ? "Quản lý" : "Dược sĩ";
    }

    public String getMaNhanVien() {
        if (taiKhoan != null && taiKhoan.getNhanVienId() != null)
            return taiKhoan.getNhanVienId().getNhanVien();
        return "";
    }

    // ── Ca làm việc ────────────────────────────────────────
    public void setCaHienTai(CaLamViec ca) { this.caHienTai = ca; }
    public CaLamViec getCaHienTai()        { return caHienTai; }

    public void setTienDauCa(long tien)    { this.tienDauCa = tien; }
    public long getTienDauCa()             { return tienDauCa; }

    public void setLoaiCa(int loai)        { this.loaiCa = loai; }
    public int  getLoaiCa()                { return loaiCa; }

    public boolean daMoCa()                { return caHienTai != null; }

    // ── Đăng xuất ──────────────────────────────────────────
    public void logout() {
        taiKhoan   = null;
        caHienTai  = null;
        tienDauCa  = 0;
        loaiCa     = 0;
    }
}
