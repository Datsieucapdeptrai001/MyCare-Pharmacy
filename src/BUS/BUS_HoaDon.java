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
    public String phatSinhMaHoaDonTuDong() {
        return daoHD.phatSinhMaHoaDonTuDong();
    }
    public List<Object[]> layDanhSachPhieuDoiTra() {
        return daoHD.layDanhSachPhieuDoiTra();
    }
    
    public List<Object[]> layDanhSachPhieuDoiTraTheoNVHomNay(String maNV) {
        return daoHD.layDanhSachPhieuDoiTraTheoNVHomNay(maNV);
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
    
    public boolean thanhToan(HoaDon hd, List<ChiTietHoaDon> dsCTHD, List<ChiTietHoaDon> dsQuaTang) {
        return daoHD.luuGiaoDichThanhToan(hd, dsCTHD, dsQuaTang, daoCTHD, daoLo, daoPB);
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
    public List<String> timGoiYHoaDonHoanThanh(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return daoHD.timGoiYHoaDonHoanThanh(tuKhoa.trim());
    }
 // Thêm class này vào bên trong BUS_HoaDon (hoặc tạo file riêng tùy ý)
    public static class KetQuaHoaDon {
        public long tamTinh = 0;
        public long tongVat = 0;
        public int tongSoLuongSP = 0;
        public long tienGiamTuDiem = 0;
        public long tongThanhToan = 0;
    }

    // THÊM HÀM NÀY VÀO BUS_HoaDon: Đưa mọi phép toán cộng trừ nhân chia về đây
    public KetQuaHoaDon tinhToanTienHoaDon(List<long[]> danhSachSanPham, boolean isDungDiem, int diemHienTaiKH, long tienGiamGiaKhuyenMai) {
        KetQuaHoaDon kq = new KetQuaHoaDon();
        
        // 1. Tính toán Tạm tính, VAT và Tổng số lượng
        for (long[] sp : danhSachSanPham) {
            long soLuong = sp[0];
            long donGia = sp[1];
            double thueSuat = sp[2] / 100.0; // VAT (%)
            
            long thanhTien = soLuong * donGia;
            kq.tamTinh += thanhTien;
            kq.tongVat += (long) (thanhTien * thueSuat);
            kq.tongSoLuongSP += soLuong;
        }
        
        // 2. Tính Tổng thanh toán (chưa trừ điểm)
        long totalToPay = kq.tamTinh + kq.tongVat - tienGiamGiaKhuyenMai;
        if (totalToPay < 0) totalToPay = 0;
        
        // 3. Xử lý logic dùng điểm thưởng
        if (isDungDiem) {
            long maxTienGiam = diemHienTaiKH * 100L; // 1 điểm = 100đ
            if (maxTienGiam > totalToPay) {
                kq.tienGiamTuDiem = (totalToPay / 100L) * 100L; // Làm tròn điểm
            } else {
                kq.tienGiamTuDiem = maxTienGiam; 
            }
            totalToPay -= kq.tienGiamTuDiem;
        } else {
            kq.tienGiamTuDiem = 0;
        }
        
        if (totalToPay < 0) totalToPay = 0;
        kq.tongThanhToan = totalToPay;
        
        return kq;
    }
}