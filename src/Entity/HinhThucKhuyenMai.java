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
	private DonViDoLuong donViDoLuongId;
	private SanPham sanPhamId;
	private KhuyenMai khuyenMaiId;
	public HinhThucKhuyenMai() {
		super();
		// TODO Auto-generated constructor stub
	}
	public HinhThucKhuyenMai(String id, LoaiHinhThuc loaiHinhThuc, DoiTuongApDung doiTuongApDung, String moTa,
			double giaTri, DonViDoLuong donViDoLuongId, SanPham sanPhamId, KhuyenMai khuyenMaiId) {
		super();
		this.id = id;
		this.loaiHinhThuc = loaiHinhThuc;
		this.doiTuongApDung = doiTuongApDung;
		this.moTa = moTa;
		this.giaTri = giaTri;
		this.donViDoLuongId = donViDoLuongId;
		this.sanPhamId = sanPhamId;
		this.khuyenMaiId = khuyenMaiId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public LoaiHinhThuc getLoaiHinhThuc() {
		return loaiHinhThuc;
	}
	public void setLoaiHinhThuc(LoaiHinhThuc loaiHinhThuc) {
		this.loaiHinhThuc = loaiHinhThuc;
	}
	public DoiTuongApDung getDoiTuongApDung() {
		return doiTuongApDung;
	}
	public void setDoiTuongApDung(DoiTuongApDung doiTuongApDung) {
		this.doiTuongApDung = doiTuongApDung;
	}
	public String getMoTa() {
		return moTa;
	}
	public void setMoTa(String moTa) {
		this.moTa = moTa;
	}
	public double getGiaTri() {
		return giaTri;
	}
	public void setGiaTri(double giaTri) {
		this.giaTri = giaTri;
	}
	public DonViDoLuong getDonViDoLuongId() {
		return donViDoLuongId;
	}
	public void setDonViDoLuongId(DonViDoLuong donViDoLuongId) {
		this.donViDoLuongId = donViDoLuongId;
	}
	public SanPham getSanPhamId() {
		return sanPhamId;
	}
	public void setSanPhamId(SanPham sanPhamId) {
		this.sanPhamId = sanPhamId;
	}
	public KhuyenMai getKhuyenMaiId() {
		return khuyenMaiId;
	}
	public void setKhuyenMaiId(KhuyenMai khuyenMaiId) {
		this.khuyenMaiId = khuyenMaiId;
	}
	@Override
	public int hashCode() {
		return Objects.hash(doiTuongApDung, donViDoLuongId, giaTri, id, khuyenMaiId, loaiHinhThuc, moTa, sanPhamId);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HinhThucKhuyenMai other = (HinhThucKhuyenMai) obj;
		return doiTuongApDung == other.doiTuongApDung && Objects.equals(donViDoLuongId, other.donViDoLuongId)
				&& Double.doubleToLongBits(giaTri) == Double.doubleToLongBits(other.giaTri)
				&& Objects.equals(id, other.id) && Objects.equals(khuyenMaiId, other.khuyenMaiId)
				&& loaiHinhThuc == other.loaiHinhThuc && Objects.equals(moTa, other.moTa)
				&& Objects.equals(sanPhamId, other.sanPhamId);
	}
	@Override
	public String toString() {
		return "HinhThucKhuyenMai [id=" + id + ", loaiHinhThuc=" + loaiHinhThuc + ", doiTuongApDung=" + doiTuongApDung
				+ ", moTa=" + moTa + ", giaTri=" + giaTri + ", donViDoLuongId=" + donViDoLuongId + ", sanPhamId="
				+ sanPhamId + ", khuyenMaiId=" + khuyenMaiId + "]";
	}
	
}
