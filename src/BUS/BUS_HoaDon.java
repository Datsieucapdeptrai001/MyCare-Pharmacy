package BUS;

import DAO.*;
import Entity.*;
import Enumeration.TrangThaiLoHang;
import java.time.LocalDateTime;
import java.util.List;

public class BUS_HoaDon {
    private DAO_HoaDon daoHD = new DAO_HoaDon();
    private DAO_ChiTietHoaDon daoCTHD = new DAO_ChiTietHoaDon();
    private DAO_PhanBoLoHang daoPB = new DAO_PhanBoLoHang();
    private DAO_LoHang daoLo = new DAO_LoHang();
    
    public List<Object[]> layDanhSachHoaDonCuaNhanVien(String maNV) {
        return daoHD.layDanhSachHoaDonCuaNhanVien(maNV); 
    }
    
    /** STAFF: Chỉ hóa đơn của nhân viên đó, hôm nay. Trả null nếu maNV rỗng. */
    public List<Object[]> layDanhSachHoaDonTheoNVHomNay(String maNV) {
        return daoHD.layDanhSachHoaDonTheoNVHomNay(maNV);
    }
    
    public List<Object[]> layDanhSachHoaDonChoBang() {
        return daoHD.layDanhSachHoaDonChoBang(); 
    }
    
    public HoaDon getHoaDonTheoMa(String maHD) {
        return daoHD.timHoaDonTheoMa(maHD); 
    }
    
    public HoaDon layHoaDonTheoMa(String maHD) {
        return daoHD.layHoaDonTheoMa(maHD); 
    }
    
    public List<HoaDon> layTatCaHoaDon() {
        return daoHD.layTatCaHoaDon(); 
    }
    
    public boolean thanhToan(HoaDon hd, List<ChiTietHoaDon> dsCTHD) {
        return daoHD.luuGiaoDichThanhToan(hd, dsCTHD, daoCTHD, daoLo, daoPB);
    }
    
    // =========================================================
    // XỬ LÝ LƯU PHIẾU ĐỔI TRẢ VÀ CHI TIẾT SẢN PHẨM (CHUẨN 3 TẦNG)
    // =========================================================
    public boolean taoPhieuDoiTra(HoaDon hdDoiTra, List<Object[]> dsTra, List<Object[]> dsDoi) {
        // 1. Lưu thông tin Phiếu (Hóa đơn) vào Database
        boolean isSuccess = daoHD.themHoaDon(hdDoiTra);
        if (!isSuccess) return false;

        // 2. Lưu danh sách sản phẩm KHÁCH TRẢ LẠI
        if (dsTra != null) {
            for (Object[] spTra : dsTra) {
                String tenSP = spTra[0].toString();
                int soLuong = (int) spTra[1];
                // Gọi DAO chi tiết (Gắn nhãn 'TRA_LAI')
                daoCTHD.themChiTietDoiTra(hdDoiTra.getId(), tenSP, soLuong, "TRA_LAI");
            }
        }

        // 3. Lưu danh sách sản phẩm KHÁCH LẤY MỚI (Dành cho Đổi hàng)
        if (dsDoi != null && !dsDoi.isEmpty()) {
            for (Object[] spDoi : dsDoi) {
                String tenSP = spDoi[0].toString();
                int soLuong = (int) spDoi[1];
                // Gọi DAO chi tiết (Gắn nhãn 'DOI_LAY')
                daoCTHD.themChiTietDoiTra(hdDoiTra.getId(), tenSP, soLuong, "DOI_LAY");
            }
        }
        
        return true; // Thành công toàn bộ
    }
    public boolean taoPhieuDoiTra(HoaDon hdDoiTra) {
        
        return daoHD.themHoaDon(hdDoiTra);
    }
}