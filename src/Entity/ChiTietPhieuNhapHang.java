package Entity;

import java.time.LocalDateTime;

public class ChiTietPhieuNhapHang {
    private String id;
    private PhieuNhapHang phieuNhapId;
    private LoHang loHangId;
    private SanPham sanPhamId;
    private KhoHang khoHangId;
    private String soLoHang;
    private int soLuongNhap;
    private double donGiaNhap;
    private double thanhTien;
    private LocalDateTime hanSuDung;

    public ChiTietPhieuNhapHang() {
    }

    public ChiTietPhieuNhapHang(String id, PhieuNhapHang phieuNhapId, LoHang loHangId,
            SanPham sanPhamId, KhoHang khoHangId, String soLoHang,
            int soLuongNhap, double donGiaNhap, double thanhTien,
            LocalDateTime hanSuDung) {
        this.id = id;
        this.phieuNhapId = phieuNhapId;
        this.loHangId = loHangId;
        this.sanPhamId = sanPhamId;
        this.khoHangId = khoHangId;
        this.soLoHang = soLoHang;
        this.soLuongNhap = soLuongNhap;
        this.donGiaNhap = donGiaNhap;
        this.thanhTien = thanhTien;
        this.hanSuDung = hanSuDung;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PhieuNhapHang getPhieuNhapId() {
        return phieuNhapId;
    }

    public void setPhieuNhapId(PhieuNhapHang phieuNhapId) {
        this.phieuNhapId = phieuNhapId;
    }

    public LoHang getLoHangId() {
        return loHangId;
    }

    public void setLoHangId(LoHang loHangId) {
        this.loHangId = loHangId;
    }

    public SanPham getSanPhamId() {
        return sanPhamId;
    }

    public void setSanPhamId(SanPham sanPhamId) {
        this.sanPhamId = sanPhamId;
    }

    public KhoHang getKhoHangId() {
        return khoHangId;
    }

    public void setKhoHangId(KhoHang khoHangId) {
        this.khoHangId = khoHangId;
    }

    public String getSoLoHang() {
        return soLoHang;
    }

    public void setSoLoHang(String soLoHang) {
        this.soLoHang = soLoHang;
    }

    public int getSoLuongNhap() {
        return soLuongNhap;
    }

    public void setSoLuongNhap(int soLuongNhap) {
        this.soLuongNhap = soLuongNhap;
    }

    public double getDonGiaNhap() {
        return donGiaNhap;
    }

    public void setDonGiaNhap(double donGiaNhap) {
        this.donGiaNhap = donGiaNhap;
    }

    public double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(double thanhTien) {
        this.thanhTien = thanhTien;
    }

    public LocalDateTime getHanSuDung() {
        return hanSuDung;
    }

    public void setHanSuDung(LocalDateTime hanSuDung) {
        this.hanSuDung = hanSuDung;
    }
}