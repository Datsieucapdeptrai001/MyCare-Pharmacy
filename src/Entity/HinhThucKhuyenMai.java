package Entity;

import java.util.Objects;
import Enum.DoiTuongApDung;
import Enum.LoaiHinhThuc;

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
    
    // --- 4 TRƯỜNG BỔ SUNG CHO KHUYẾN MÃI MUA X TẶNG Y ---
    private String spYeuCau;
    private int slYeuCau;
    private String spTang;
    private int slTang;

    public HinhThucKhuyenMai() {
        super();
    }

    public HinhThucKhuyenMai(String id, LoaiHinhThuc loaiHinhThuc, DoiTuongApDung doiTuongApDung, String moTa,
            double giaTri, double giamToiDa, DonViDoLuong donViDoLuongId, SanPham sanPhamId, KhuyenMai khuyenMaiId) {
        super();
        this.id = id;
        this.loaiHinhThuc = loaiHinhThuc;
        this.doiTuongApDung = doiTuongApDung;
        this.moTa = moTa;
        this.giaTri = giaTri;
        this.giamToiDa = giamToiDa;
        this.donViDoLuongId = donViDoLuongId;
        this.sanPhamId = sanPhamId;
        this.khuyenMaiId = khuyenMaiId;
    }

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
    public String getSpTang() { return spTang; }
    public void setSpTang(String spTang) { this.spTang = spTang; }
    public int getSlTang() { return slTang; }
    public void setSlTang(int slTang) { this.slTang = slTang; }

    @Override
    public int hashCode() {
        return Objects.hash(doiTuongApDung, donViDoLuongId, giaTri, giamToiDa, id, khuyenMaiId, loaiHinhThuc, moTa, sanPhamId, spYeuCau, slYeuCau, spTang, slTang);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        HinhThucKhuyenMai other = (HinhThucKhuyenMai) obj;
        return doiTuongApDung == other.doiTuongApDung
                && Objects.equals(donViDoLuongId, other.donViDoLuongId)
                && Double.compare(other.giaTri, giaTri) == 0
                && Double.compare(other.giamToiDa, giamToiDa) == 0
                && Objects.equals(id, other.id)
                && Objects.equals(khuyenMaiId, other.khuyenMaiId)
                && loaiHinhThuc == other.loaiHinhThuc
                && Objects.equals(moTa, other.moTa)
                && Objects.equals(sanPhamId, other.sanPhamId)
                && Objects.equals(spYeuCau, other.spYeuCau)
                && slYeuCau == other.slYeuCau
                && Objects.equals(spTang, other.spTang)
                && slTang == other.slTang;
    }

    @Override
    public String toString() {
        return "HinhThucKhuyenMai [id=" + id + ", loaiHinhThuc=" + loaiHinhThuc + ", doiTuongApDung=" + doiTuongApDung
                + ", moTa=" + moTa + ", giaTri=" + giaTri + ", giamToiDa=" + giamToiDa + ", spYeuCau=" + spYeuCau 
                + ", slYeuCau=" + slYeuCau + ", spTang=" + spTang + ", slTang=" + slTang + "]";
    }
}