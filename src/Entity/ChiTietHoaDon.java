package Entity;

import java.util.Objects;

public class ChiTietHoaDon {
	private HoaDon hoaDonId;
	private DonViDoLuong donViDoLuongId;
	private SanPham sanPhamId;
	private int soLuong;
	private double donGiaThucTe;
	private double thanhTien;
	public ChiTietHoaDon() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ChiTietHoaDon(HoaDon hoaDonId, DonViDoLuong donViDoLuongId, SanPham sanPhamId, int soLuong) {
		super();
		this.hoaDonId = hoaDonId;
		this.donViDoLuongId = donViDoLuongId;
		this.sanPhamId = sanPhamId;
		this.soLuong = soLuong;
	}
	public HoaDon getHoaDonId() {
		return hoaDonId;
	}
	public void setHoaDonId(HoaDon hoaDonId) {
		this.hoaDonId = hoaDonId;
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
	public int getSoLuong() {
		return soLuong;
	}
	public void setSoLuong(int soLuong) {
		this.soLuong = soLuong;
	}
	public double getDonGiaThucTe() { return donGiaThucTe; }
	public void setDonGiaThucTe(double donGiaThucTe) { this.donGiaThucTe = donGiaThucTe; }

	public double getThanhTien() { return thanhTien; }
	public void setThanhTien(double thanhTien) { this.thanhTien = thanhTien; }
	@Override
	public int hashCode() {
		return Objects.hash(donViDoLuongId, hoaDonId, sanPhamId, soLuong);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChiTietHoaDon other = (ChiTietHoaDon) obj;
		return Objects.equals(donViDoLuongId, other.donViDoLuongId) && Objects.equals(hoaDonId, other.hoaDonId)
				&& Objects.equals(sanPhamId, other.sanPhamId) && soLuong == other.soLuong;
	}
	@Override
	public String toString() {
		return "ChiTietHoaDon [hoaDonId=" + hoaDonId + ", donViDoLuongId=" + donViDoLuongId + ", sanPhamId=" + sanPhamId
				+ ", soLuong=" + soLuong + "]";
	}
	
}
