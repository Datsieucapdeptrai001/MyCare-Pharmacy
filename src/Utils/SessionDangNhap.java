package Utils;

public class SessionDangNhap {

    private static String maNhanVien;
    private static String hoTenNhanVien;
    private static String vaiTro;

    private SessionDangNhap() {
    }

    public static void dangNhap(String maNV, String hoTen, String role) {
        maNhanVien = maNV;
        hoTenNhanVien = hoTen;
        vaiTro = role;
    }

    public static void dangXuat() {
        maNhanVien = null;
        hoTenNhanVien = null;
        vaiTro = null;
    }

    public static String getMaNhanVien() {
        return maNhanVien;
    }

    public static String getHoTenNhanVien() {
        return hoTenNhanVien;
    }

    public static String getVaiTro() {
        return vaiTro;
    }

    public static boolean daDangNhap() {
        return maNhanVien != null && !maNhanVien.trim().isEmpty();
    }

    public static String getMaNhanVienOrDefault() {
        if (daDangNhap()) {
            return maNhanVien.trim();
        }

        // Fallback để không lỗi nếu chưa set session
        return "QL-0001";
    }

    public static String getHoTenOrDefault() {
        if (hoTenNhanVien != null && !hoTenNhanVien.trim().isEmpty()) {
            return hoTenNhanVien.trim();
        }

        return "Người dùng hiện tại";
    }

    public static boolean laQuanLy() {
        if (vaiTro == null) {
            return false;
        }

        String role = vaiTro.trim().toUpperCase();

        return role.contains("QUAN")
                || role.contains("QL")
                || role.contains("MANAGER");
    }

    public static boolean laNhanVien() {
        if (vaiTro == null) {
            return false;
        }

        String role = vaiTro.trim().toUpperCase();

        return role.contains("NHAN")
                || role.contains("NV")
                || role.contains("DUOC");
    }
}