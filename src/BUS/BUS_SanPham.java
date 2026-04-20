package BUS;

import DAO.DAO_SanPham;
import Entity.LoHang;
import Entity.SanPham;

import java.util.ArrayList;
import java.util.List;

public class BUS_SanPham {
    private final DAO_SanPham daoSanPham = new DAO_SanPham();

    public BUS_SanPham() { }
    
    public List<SanPham> traCuuSanPham(String tuKhoa) {
        // Xóa dsFake và gọi thẳng xuống DAO
        return daoSanPham.timKiemSanPhamDoiTra(tuKhoa);
    }

    public boolean kiemTraThongTinSP(SanPham sp) {
        if (sp == null) return false;
        if (isBlank(sp.getId())) return false;
        if (isBlank(sp.getTen())) return false;
        if (sp.getThueVAT() < 0) return false;
        return true;
    }

    public double tinhGiaBanTheoDonVi(String maSP, String donViMuonBan) {
        if (isBlank(maSP) || isBlank(donViMuonBan)) return 0.0;
        return 5000.0;
    }

    public List<LoHang> layLoTheoSP(String maSP) {
        if (isBlank(maSP)) return new ArrayList<>();
        return daoSanPham.layLoTheoSP(maSP.trim());
    }

    public String taoMaMoi() {
        return daoSanPham.layMaSanPhamMoiNhat();
    }

    public boolean themSP(String id, String danhMuc, String dang, String ten, String vietTat,
                          String nsx, String hoatChat, double vat, String hamLuong,
                          String moTa, String dvt) {
        if (isBlank(id)) return false;
        if (isBlank(ten)) return false;
        if (vat < 0) return false;

        return daoSanPham.themSanPhamNhanh(
                id.trim(),
                safe(danhMuc).trim(),
                safe(dang).trim(),
                ten.trim(),
                safe(vietTat).trim(),
                safe(nsx).trim(),
                safe(hoatChat).trim(),
                vat,
                safe(hamLuong).trim(),
                safe(moTa).trim(),
                safe(dvt).trim()
        );
    }

    public boolean capNhatSP(String id, String danhMuc, String dang, String ten, String vietTat,
                             String nsx, String hoatChat, double vat, String hamLuong,
                             String moTa, String dvt) {
        if (isBlank(id)) return false;
        if (isBlank(ten)) return false;
        if (vat < 0) return false;

        return daoSanPham.capNhatSanPhamNhanh(
                id.trim(),
                safe(danhMuc).trim(),
                safe(dang).trim(),
                ten.trim(),
                safe(vietTat).trim(),
                safe(nsx).trim(),
                safe(hoatChat).trim(),
                vat,
                safe(hamLuong).trim(),
                safe(moTa).trim(),
                safe(dvt).trim()
        );
    }

    public boolean xoaSP(String id) {
        return anSP(id);
    }

    public List<Object[]> layDanhSachChoBang() {
        return daoSanPham.layDanhSachSanPhamChoBang();
    }

    public List<SanPham> getDsThuoc() {
        return daoSanPham.getDsThuoc();
    }

    public boolean anSP(String maSP) {
        if (isBlank(maSP)) return false;

        int ton = daoSanPham.getSoLuongTon(maSP.trim());
        if (ton > 0) {
            return false;
        }

        return daoSanPham.anSanPham(maSP.trim());
    }

    public int getSoLuongTon(String maSP) {
        if (isBlank(maSP)) return 0;
        return daoSanPham.getSoLuongTon(maSP.trim());
    }

    public List<Object[]> layDanhSachSanPhamDaAn() {
        return daoSanPham.layDanhSachSanPhamDaAn();
    }

    public boolean khoiPhucSP(String maSP) {
        if (isBlank(maSP)) return false;
        return daoSanPham.khoiPhucSanPham(maSP.trim());
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}