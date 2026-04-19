package Entity;

import Enumeration.DangBaoChe;
import Enumeration.DanhMucSanPham;

import java.time.LocalDateTime;

public class SanPham {
    private String id;
    private DanhMucSanPham danhMuc;
    private DangBaoChe dang;
    private String ten;
    private String tenVietTat;
    private String nhaSanXuat;
    private String hoatChat;
    private double thueVAT;
    private String hamLuong;
    private String moTa;
    private String donViDoCoBan;
    private LocalDateTime ngayTao;

    public SanPham() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DanhMucSanPham getDanhMuc() {
        return danhMuc;
    }

    public void setDanhMuc(DanhMucSanPham danhMuc) {
        this.danhMuc = danhMuc;
    }

    public DangBaoChe getDang() {
        return dang;
    }

    public void setDang(DangBaoChe dang) {
        this.dang = dang;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getTenVietTat() {
        return tenVietTat;
    }

    public void setTenVietTat(String tenVietTat) {
        this.tenVietTat = tenVietTat;
    }

    public String getNhaSanXuat() {
        return nhaSanXuat;
    }

    public void setNhaSanXuat(String nhaSanXuat) {
        this.nhaSanXuat = nhaSanXuat;
    }

    public String getHoatChat() {
        return hoatChat;
    }

    public void setHoatChat(String hoatChat) {
        this.hoatChat = hoatChat;
    }

    public double getThueVAT() {
        return thueVAT;
    }

    public void setThueVAT(double thueVAT) {
        this.thueVAT = thueVAT;
    }

    public String getHamLuong() {
        return hamLuong;
    }

    public void setHamLuong(String hamLuong) {
        this.hamLuong = hamLuong;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getDonViDoCoBan() {
        return donViDoCoBan;
    }

    public void setDonViDoCoBan(String donViDoCoBan) {
        this.donViDoCoBan = donViDoCoBan;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    @Override
    public String toString() {
        return ten + " (" + id + ")";
    }
}