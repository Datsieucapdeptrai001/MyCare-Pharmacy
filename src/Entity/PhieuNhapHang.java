package Entity;

import java.time.LocalDateTime;

public class PhieuNhapHang {
    private String id;
    private LocalDateTime ngayNhap;
    private String nhaCungCapId;
    private String nhanVienId;
    private double tongTien;
    private String ghiChu;
    private String trangThai;

    public PhieuNhapHang() {
    }

    public PhieuNhapHang(String id, LocalDateTime ngayNhap, String nhaCungCapId,
            String nhanVienId, double tongTien, String ghiChu, String trangThai) {
        this.id = id;
        this.ngayNhap = ngayNhap;
        this.nhaCungCapId = nhaCungCapId;
        this.nhanVienId = nhanVienId;
        this.tongTien = tongTien;
        this.ghiChu = ghiChu;
        this.trangThai = trangThai;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getNgayNhap() {
        return ngayNhap;
    }

    public void setNgayNhap(LocalDateTime ngayNhap) {
        this.ngayNhap = ngayNhap;
    }

    public String getNhaCungCapId() {
        return nhaCungCapId;
    }

    public void setNhaCungCapId(String nhaCungCapId) {
        this.nhaCungCapId = nhaCungCapId;
    }

    public String getNhanVienId() {
        return nhanVienId;
    }

    public void setNhanVienId(String nhanVienId) {
        this.nhanVienId = nhanVienId;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return id;
    }
}