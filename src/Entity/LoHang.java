package Entity;

import Enumeration.TrangThaiLoHang;

import java.time.LocalDateTime;

public class LoHang {
    private String id;
    private String soLoHang;
    private int soLuongLoHang;
    private double gia;
    private LocalDateTime ngayNhap;
    private LocalDateTime ngayHetHan;
    private TrangThaiLoHang trangThai;
    private SanPham sanPhamId;
    private KhoHang khoHangId;

    public LoHang() {
    }

    public LoHang(String id, String soLoHang, int soLuongLoHang, double gia,
            LocalDateTime ngayNhap, LocalDateTime ngayHetHan,
            TrangThaiLoHang trangThai, SanPham sanPhamId, KhoHang khoHangId) {
        this.id = id;
        this.soLoHang = soLoHang;
        this.soLuongLoHang = soLuongLoHang;
        this.gia = gia;
        this.ngayNhap = ngayNhap;
        this.ngayHetHan = ngayHetHan;
        this.trangThai = trangThai;
        this.sanPhamId = sanPhamId;
        this.khoHangId = khoHangId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSoLoHang() {
        return soLoHang;
    }

    public void setSoLoHang(String soLoHang) {
        this.soLoHang = soLoHang;
    }

    public int getSoLuongLoHang() {
        return soLuongLoHang;
    }

    public void setSoLuongLoHang(int soLuongLoHang) {
        this.soLuongLoHang = soLuongLoHang;
    }

    public double getGia() {
        return gia;
    }

    public void setGia(double gia) {
        this.gia = gia;
    }

    public LocalDateTime getNgayNhap() {
        return ngayNhap;
    }

    public void setNgayNhap(LocalDateTime ngayNhap) {
        this.ngayNhap = ngayNhap;
    }

    public LocalDateTime getNgayHetHan() {
        return ngayHetHan;
    }

    public void setNgayHetHan(LocalDateTime ngayHetHan) {
        this.ngayHetHan = ngayHetHan;
    }

    public TrangThaiLoHang getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiLoHang trangThai) {
        this.trangThai = trangThai;
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
}