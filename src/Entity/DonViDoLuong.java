package Entity;

import java.util.Objects;

public class DonViDoLuong {
	private String id;
	private SanPham sanPhamId;
	private String ten;
	private double chuyenDoiSangDonViCoBan;
	private double gia;
	public DonViDoLuong() {
		super();
		// TODO Auto-generated constructor stub
	}
	public DonViDoLuong(String id, SanPham sanPhamId, String ten, double chuyenDoiSangDonViCoBan, double gia) {
		super();
		this.id = id;
		this.sanPhamId = sanPhamId;
		this.ten = ten;
		this.chuyenDoiSangDonViCoBan = chuyenDoiSangDonViCoBan;
		this.gia = gia;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public SanPham getSanPhamId() {
		return sanPhamId;
	}
	public void setSanPhamId(SanPham sanPhamId) {
		this.sanPhamId = sanPhamId;
	}
	public String getTen() {
		return ten;
	}
	public void setTen(String ten) {
		this.ten = ten;
	}
	public double getChuyenDoiSangDonViCoBan() {
		return chuyenDoiSangDonViCoBan;
	}
	public void setChuyenDoiSangDonViCoBan(double chuyenDoiSangDonViCoBan) {
		this.chuyenDoiSangDonViCoBan = chuyenDoiSangDonViCoBan;
	}
	public double getGia() {
		return gia;
	}
	public void setGia(double gia) {
		this.gia = gia;
	}
	@Override
	public int hashCode() {
		return Objects.hash(chuyenDoiSangDonViCoBan, gia, id, sanPhamId, ten);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DonViDoLuong other = (DonViDoLuong) obj;
		return Double.doubleToLongBits(chuyenDoiSangDonViCoBan) == Double
				.doubleToLongBits(other.chuyenDoiSangDonViCoBan)
				&& Double.doubleToLongBits(gia) == Double.doubleToLongBits(other.gia) && Objects.equals(id, other.id)
				&& Objects.equals(sanPhamId, other.sanPhamId) && Objects.equals(ten, other.ten);
	}
	@Override
	public String toString() {
		return "DonViDoLuong [id=" + id + ", sanPhamId=" + sanPhamId + ", ten=" + ten + ", chuyenDoiSangDonViCoBan="
				+ chuyenDoiSangDonViCoBan + ", gia=" + gia + "]";
	}
	
}
