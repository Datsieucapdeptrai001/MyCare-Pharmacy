package Entity;

import java.util.Objects;
import Enumeration.*;

public class HinhThucKhuyenMai {
    private String id;
    private LoaiHinhThuc loaiHinhThuc;
    private DoiTuongApDung doiTuongApDung;
    private String moTa;
    private double giaTri;
    private double giamToiDa;
    private DonViDoLuong donViDoLuongId;
    private SanPham sanPhamId;
    private KhuyenMai khuyenMaiId;
    
    // --- CÁC TRƯỜNG CHO MUA X TẶNG Y VÀ GIẢM TIỀN MẶT ---
    private String spYeuCau;
    private int slYeuCau;
    private String dvdlYeuCau; // Biến mới
    private String spTang;
    private int slTang;
    private String dvdlTang;   // Biến mới

    public HinhThucKhuyenMai() { super(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LoaiHinhThuc getLoaiHinhThuc() { return loaiHinhThuc; }
    public void setLoaiHinhThuc(LoaiHinhThuc loaiHinhThuc) { this.loaiHinhThuc = loaiHinhThuc; }
    public DoiTuongApDung getDoiTuongApDung() { return doiTuongApDung; }
    public void setDoiTuongApDung(DoiTuongApDung doiTuongApDung) { this.doiTuongApDung = doiTuongApDung; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public double getGiaTri() { return giaTri; }
    public void setGiaTri(double giaTri) { this.giaTri = giaTri; }
    public double getGiamToiDa() { return giamToiDa; }
    public void setGiamToiDa(double giamToiDa) { this.giamToiDa = giamToiDa; }
    public DonViDoLuong getDonViDoLuongId() { return donViDoLuongId; }
    public void setDonViDoLuongId(DonViDoLuong donViDoLuongId) { this.donViDoLuongId = donViDoLuongId; }
    public SanPham getSanPhamId() { return sanPhamId; }
    public void setSanPhamId(SanPham sanPhamId) { this.sanPhamId = sanPhamId; }
    public KhuyenMai getKhuyenMaiId() { return khuyenMaiId; }
    public void setKhuyenMaiId(KhuyenMai khuyenMaiId) { this.khuyenMaiId = khuyenMaiId; }

    public String getSpYeuCau() { return spYeuCau; }
    public void setSpYeuCau(String spYeuCau) { this.spYeuCau = spYeuCau; }
    public int getSlYeuCau() { return slYeuCau; }
    public void setSlYeuCau(int slYeuCau) { this.slYeuCau = slYeuCau; }
    public String getDvdlYeuCau() { return dvdlYeuCau; }
    public void setDvdlYeuCau(String dvdlYeuCau) { this.dvdlYeuCau = dvdlYeuCau; }
    public String getSpTang() { return spTang; }
    public void setSpTang(String spTang) { this.spTang = spTang; }
    public int getSlTang() { return slTang; }
    public void setSlTang(int slTang) { this.slTang = slTang; }
    public String getDvdlTang() { return dvdlTang; }
    public void setDvdlTang(String dvdlTang) { this.dvdlTang = dvdlTang; }
}