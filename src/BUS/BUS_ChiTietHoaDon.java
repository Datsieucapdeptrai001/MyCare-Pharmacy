package BUS;

import java.util.List;
import DAO.DAO_ChiTietHoaDon;

public class BUS_ChiTietHoaDon {
    
    private DAO_ChiTietHoaDon daoCTHD;

    public BUS_ChiTietHoaDon() {
        daoCTHD = new DAO_ChiTietHoaDon();
    }

    // Hàm lấy danh sách sản phẩm theo mã hóa đơn để hiển thị lên bảng chi tiết
    public List<Object[]> layDanhSachSanPhamTheoMaHD(String maHD) {
        if (maHD == null || maHD.trim().isEmpty()) {
            return null;
        }
        return daoCTHD.layDanhSachSanPhamTheoMaHD(maHD); 
    }

    // Hàm phục vụ đổi trả (bạn đã có)
    public List<Object[]> layDuLieuDoiTra(String maHD) {
        if (maHD == null || maHD.trim().isEmpty()) {
            return null;
        }
        return daoCTHD.layDuLieuChoTaoHoaDon(maHD); 
    }
}