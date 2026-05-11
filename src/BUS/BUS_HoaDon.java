package BUS;

import DAO.*;
import Entity.*;
import Enumeration.TrangThaiLoHang;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
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
        // Gọi DAO lấy dữ liệu thô
        List<Object[]> dsRaw = daoHD.layDanhSachHoaDonRaw();
        List<Object[]> dsFormatted = new ArrayList<>();
        
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###đ");
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (Object[] row : dsRaw) {
            String id = (String) row[0];
            String loaiHD = (String) row[1];
            java.sql.Timestamp tsNgay = (java.sql.Timestamp) row[2];
            String kh = (String) row[3];
            String sdt = (String) row[4];
            String phuongThuc = (String) row[5];
            String ghiChu = (String) row[6];
            double tongTienGoc = row[7] != null ? (double) row[7] : 0;
            double tongVAT = row[8] != null ? (double) row[8] : 0;
            
            // ----------------------------------------------------
            // BƯỚC 1: XỬ LÝ TOÁN HỌC (TÍNH VAT VÀ TỔNG TIỀN)
            // ----------------------------------------------------
            double totalAmount = tongTienGoc + tongVAT; 
            double originalAmount = totalAmount; 
            double tongTienGiam = 0;
            
            // Xử lý đọc ghi chú để khấu trừ tiền
            if (ghiChu != null && !ghiChu.isEmpty()) {
                String[] parts = ghiChu.split("\\|");
                for (String p : parts) {
                    p = p.trim();
                    if (p.startsWith("Dùng điểm: -") || p.contains("KM_GIAM:")) {
                        try {
                            long tienGiam = Long.parseLong(p.replaceAll("[^0-9]", ""));
                            tongTienGiam += tienGiam;
                        } catch (Exception ignored) {}
                    } else if (p.startsWith("KM:")) {
                        String[] mks = p.substring(3).trim().split(",");
                        for (String mk : mks) {
                            // Gọi DAO lấy thông tin KM, hoàn toàn không dính dáng câu lệnh SQL ở BUS
                            double[] kmData = daoHD.layThongTinKhuyenMai(mk);
                            if (kmData[0] == 1.0) { // Giảm theo %
                                tongTienGiam += originalAmount * (kmData[1] / 100.0);
                            } else if (kmData[0] == 2.0) { // Giảm tiền mặt
                                tongTienGiam += kmData[1];
                            }
                        }
                    }
                }
            }
            
            totalAmount -= tongTienGiam; // Trừ đi tổng tiền khuyến mãi/điểm thưởng
            if (totalAmount < 0) totalAmount = 0;

            // ----------------------------------------------------
            // BƯỚC 2: FORMAT DỮ LIỆU CHUẨN BỊ CHO GUI HIỂN THỊ
            // ----------------------------------------------------
            String ngayStr = tsNgay != null ? tsNgay.toLocalDateTime().format(dtf) : "";
            String tenKhach = kh != null ? kh : "Khách lẻ";
            String sdtKhach = sdt != null ? sdt : "";
            String hienThiPT = "CHUYEN_KHOAN_NGAN_HANG".equals(phuongThuc) ? "Chuyển khoản" : "Tiền mặt";
            
            String trangThai = "Hoàn thành";
            if (loaiHD != null && (loaiHD.equals("TRA_HANG") || loaiHD.equals("DOI_HANG"))) {
                trangThai = "Đổi trả";
            } else if (ghiChu != null) {
                if (ghiChu.contains("Lưu nháp") || ghiChu.contains("Đang xử lý")) trangThai = "Đang xử lý";
                else if (ghiChu.contains("Đã hủy")) trangThai = "Đã hủy";
            }

            // Gói vào mảng Object chuẩn bị đưa lên Table trên GUI
            dsFormatted.add(new Object[]{
                id, ngayStr, tenKhach, sdtKhach, hienThiPT, df.format(totalAmount).replace(',', '.'), trangThai, ghiChu
            });
        }
        
        return dsFormatted;
    }
 // File: BUS_HoaDon.java
    public List<Object[]> layDanhSachHoaDonHienThi() {
        List<Object[]> dsRaw = daoHD.layDanhSachHoaDonRaw();
        List<Object[]> dsFormatted = new ArrayList<>();
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###đ");
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Object[] row : dsRaw) {
            double tongChuaThue = (double) row[7];
            double tongThue = (double) row[8];
            double totalAmount = tongChuaThue + tongThue; // TÍNH TOÁN VAT TẠI BUS
            
            double tongTienGiam = 0;
            String ghiChu = (String) row[6];
            
            // Xử lý logic bóc tách ghi chú để trừ tiền (Logic Business)
            if (ghiChu != null && !ghiChu.isEmpty()) {
                String[] parts = ghiChu.split("\\|");
                for (String p : parts) {
                    if (p.contains("Dùng điểm") || p.contains("KM_GIAM:")) {
                        tongTienGiam += Long.parseLong(p.replaceAll("[^0-9]", ""));
                    }
                }
            }
            
            totalAmount -= tongTienGiam;
            if (totalAmount < 0) totalAmount = 0;

            String hienThiPT = "CHUYEN_KHOAN_NGAN_HANG".equals(row[5]) ? "Chuyển khoản" : "Tiền mặt";
            String ngayStr = row[2] != null ? ((java.sql.Timestamp)row[2]).toLocalDateTime().format(dtf) : "";

            dsFormatted.add(new Object[]{
                row[0], ngayStr, row[3] != null ? row[3] : "Khách lẻ", 
                row[4] != null ? row[4] : "", hienThiPT, df.format(totalAmount).replace(',', '.'), 
                "Hoàn thành", ghiChu
            });
        }
        return dsFormatted;
    }
    public HoaDon getHoaDonTheoMa(String maHD) {
        return daoHD.timHoaDonTheoMa(maHD); 
    }
    public void xoaHoaDonNhap(String maHD) {
        new DAO.DAO_HoaDon().xoaHoaDonNhap(maHD);
    }
    public boolean thanhToanToanDien(HoaDon hd, List<ChiTietHoaDon> dsCTHD, List<ChiTietHoaDon> dsQuaTang, 
            String maHDDangSua, KhachHang kh, int diemChenhLech) {
    	// Có thể thêm logic kiểm tra tồn kho bằng busKho ở đây trước khi gọi DAO
    	return daoHD.thanhToanToanDien(hd, dsCTHD, dsQuaTang, maHDDangSua, kh, diemChenhLech);
    }
    public String[] layMaSPVaMaDVT(String tenSP, String tenDVT) {
    	return daoHD.layMaSPVaMaDVT(tenSP, tenDVT);
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
    
    public boolean huyHoaDon(String maHD) {
        if (maHD == null || maHD.trim().isEmpty()) {
            return false;
        }
        return daoHD.huyHoaDon(maHD);
    }
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

    
    public KetQuaHoaDon tinhToanTienHoaDon(List<long[]> danhSachSanPham, boolean isDungDiem, int diemHienTaiKH, long tienGiamGiaKhuyenMai) {
        KetQuaHoaDon kq = new KetQuaHoaDon();
        for (long[] sp : danhSachSanPham) {
            long soLuong = sp[0];
            long donGia = sp[1];
            double thueSuat = sp[2] / 100.0; // VAT (%)
            
            long thanhTien = soLuong * donGia;
            kq.tamTinh += thanhTien;
            kq.tongVat += (long) (thanhTien * thueSuat);
            kq.tongSoLuongSP += soLuong;
        }
        long totalToPay = kq.tamTinh + kq.tongVat - tienGiamGiaKhuyenMai;
        if (totalToPay < 0) totalToPay = 0;
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