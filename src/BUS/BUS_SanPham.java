package BUS;

import Entity.SanPham;
import Entity.LoHang;
import DAO.DAO_SanPham;
import java.util.ArrayList;
import java.util.List;

public class BUS_SanPham {
    private DAO_SanPham daoSanPham = new DAO_SanPham(); 

    public BUS_SanPham() { }

    public List<SanPham> traCuuSanPham(String tuKhoa) {
        List<SanPham> dsFake = new ArrayList<>();
        SanPham sp1 = new SanPham();
        sp1.setId("PRO2023-0001"); sp1.setTen("Vitamin C 1000mg"); sp1.setHoatChat("Ascorbic Acid"); sp1.setDonViDoCoBan("Viên"); sp1.setThueVAT(10);
        
        SanPham sp2 = new SanPham();
        sp2.setId("PRO2023-0005"); sp2.setTen("Siro tăng sức đề kháng"); sp2.setHoatChat("Various"); sp2.setDonViDoCoBan("Chai"); sp2.setThueVAT(10);

        SanPham sp3 = new SanPham();
        sp3.setId("PRO2023-0006"); sp3.setTen("Paracetamol 500mg"); sp3.setHoatChat("Paracetamol"); sp3.setDonViDoCoBan("Vỉ"); sp3.setThueVAT(5);

        dsFake.add(sp1); dsFake.add(sp2); dsFake.add(sp3);

        if (tuKhoa == null || tuKhoa.trim().isEmpty()) return dsFake;

        List<SanPham> ketQuaTimKiem = new ArrayList<>();
        String tuKhoaLower = tuKhoa.toLowerCase();
        for (SanPham sp : dsFake) {
            if (sp.getTen().toLowerCase().contains(tuKhoaLower) || sp.getHoatChat().toLowerCase().contains(tuKhoaLower)) {
                ketQuaTimKiem.add(sp);
            }
        }
        return ketQuaTimKiem;
    }

    public boolean kiemTraThongTinSP(SanPham sp) {
        return true; 
    }

    public double tinhGiaBanTheoDonVi(String maSP, String donViMuonBan) {
        return 5000.0; 
    }
    
    public List<LoHang> layLoTheoSP(String maSP) {
        return daoSanPham.layLoTheoSP(maSP); 
    }

    public String taoMaMoi() {
        return daoSanPham.layMaSanPhamMoiNhat();
    }

    public boolean themSP(String id, String danhMuc, String dang, String ten, String vietTat, String nsx, String hoatChat, double vat, String hamLuong, String moTa, String dvt) {
        if (ten == null || ten.trim().isEmpty()) return false;
        return daoSanPham.themSanPhamNhanh(id, danhMuc, dang, ten, vietTat, nsx, hoatChat, vat, hamLuong, moTa, dvt);
    }

    public boolean capNhatSP(String id, String danhMuc, String dang, String ten, String vietTat, String nsx, String hoatChat, double vat, String hamLuong, String moTa, String dvt) {
        return daoSanPham.capNhatSanPhamNhanh(id, danhMuc, dang, ten, vietTat, nsx, hoatChat, vat, hamLuong, moTa, dvt);
    }

    public boolean xoaSP(String id) {
        if (id == null || id.isEmpty()) return false;
        return daoSanPham.anSanPham(id);
    }
    
    public List<Object[]> layDanhSachChoBang() {
        return daoSanPham.layDanhSachSanPhamChoBang();
    }
    
    // Đã thêm hàm getDsThuoc() để GUI gọi sang
    public List<SanPham> getDsThuoc() {
        return daoSanPham.getDsThuoc();
    }
 // Ẩn sản phẩm — chỉ update cột, không xóa dòng
    public boolean anSP(String maSP) {
        return new DAO_SanPham().anSanPham(maSP);
    }

    // Kiểm tra tồn kho trước khi cho phép ẩn
    public int getSoLuongTon(String maSP) {
        return new DAO_SanPham().getSoLuongTon(maSP);
    }
 // BỔ SUNG: Gọi DAO lấy thùng rác
    public List<Object[]> layDanhSachSanPhamDaAn() {
        return daoSanPham.layDanhSachSanPhamDaAn();
    }

    // BỔ SUNG: Gọi DAO khôi phục
    public boolean khoiPhucSP(String maSP) {
        return daoSanPham.khoiPhucSanPham(maSP);
    }
}