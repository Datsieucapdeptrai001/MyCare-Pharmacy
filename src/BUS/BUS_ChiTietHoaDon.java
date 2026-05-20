package BUS;

import java.util.List;
import DAO.DAO_ChiTietHoaDon;
import Entity.ChiTietHoaDon;

public class BUS_ChiTietHoaDon {
    
    private DAO_ChiTietHoaDon daoCTHD;

    public BUS_ChiTietHoaDon() {
        daoCTHD = new DAO_ChiTietHoaDon();
    }


    
    // Hàm lấy danh sách sản phẩm theo mã hóa đơn để hiển thị lên bảng chi tiết UI
    public List<Object[]> layDanhSachSanPhamTheoMaHD(String maHD) {
        if (maHD == null || maHD.trim().isEmpty()) {
            return null;
        }
        return daoCTHD.layDanhSachSanPhamTheoMaHD(maHD); 
    }

    // Hàm phục vụ đổi trả trên UI TaoHoaDon
    public List<Object[]> layDuLieuDoiTra(String maHD) {
        if (maHD == null || maHD.trim().isEmpty()) {
            return null;
        }
        return daoCTHD.layDuLieuChoTaoHoaDon(maHD); 
    }
    
    // Lấy nguyên list object ChiTietHoaDon phục vụ tính toán logic nội bộ
    public List<ChiTietHoaDon> layDSChiTietHD(String maHoaDon) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            return null;
        }
        return daoCTHD.layDSChiTietHD(maHoaDon);
    }

    // =======================================================
    // 2. NHÓM HÀM THÊM MỚI (INSERT)
    // =======================================================
    
    // Thêm một Chi Tiết Hóa Đơn mới vào Database (Cần thiết cho tính năng Thanh Toán / Lưu Nháp)
    public boolean themCTHD(ChiTietHoaDon cthd) {
        if (cthd == null || cthd.getHoaDonId() == null || cthd.getSanPhamId() == null) {
            return false;
        }
        // Có thể bổ sung thêm các bước kiểm tra (Validate) logic kinh doanh tại đây nếu cần
        if (cthd.getSoLuong() <= 0) {
            return false;
        }
        
        return daoCTHD.themCTHD(cthd);
    }
    
    // Hàm thêm chi tiết riêng cho tính năng Đổi / Trả Hàng
    // isTraLai=true  → hàng khách trả lại  → DAO sẽ lưu soLuong ÂM
    // isTraLai=false → hàng khách đổi mới  → DAO sẽ lưu soLuong DƯƠNG
    public boolean themChiTietDoiTra(String maHD, String tenSP, String dvt, int soLuong, double donGia, String ghiChu, boolean isTraLai) {
        if (maHD == null || maHD.trim().isEmpty() || tenSP == null || tenSP.trim().isEmpty() || soLuong <= 0) {
            return false;
        }
        return daoCTHD.themChiTietDoiTra(maHD, tenSP, dvt, soLuong, donGia, ghiChu, isTraLai);
    }

    // =======================================================
    // 3. NHÓM HÀM XÓA (DELETE) - BỔ SUNG MỚI
    // =======================================================
    
    // Hàm này bắt buộc phải có để tính năng "Cập Nhật Hóa Đơn" / "Sửa Lại Đơn Lưu Nháp" hoạt động
    // Logic: Xóa toàn bộ chi tiết cũ của mã Hóa Đơn đó, sau đó insert chi tiết mới vào.
    public boolean xoaChiTietTheoMaHD(String maHD) {
        if (maHD == null || maHD.trim().isEmpty()) {
            return false;
        }
        return daoCTHD.xoaChiTietTheoMaHD(maHD);
    }
}