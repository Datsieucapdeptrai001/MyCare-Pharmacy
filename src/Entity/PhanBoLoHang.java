package Entity;

import java.util.Objects;

public class PhanBoLoHang {
	private HoaDon hoaDonId;
	private DonViDoLuong donViDoLuong;
	private SanPham sanPhamId;
	private LoHang loHangId;
	private int soLuong;
	
	public PhanBoLoHang() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public PhanBoLoHang(HoaDon hoaDonId, DonViDoLuong donViDoLuong, SanPham sanPhamId, LoHang loHangId, int soLuong) {
		super();
		this.hoaDonId = hoaDonId;
		this.donViDoLuong = donViDoLuong;
		this.sanPhamId = sanPhamId;
		this.loHangId = loHangId;
		this.soLuong = soLuong;
	}

	public HoaDon getHoaDonId() {
		return hoaDonId;
	}
	public void setHoaDonId(HoaDon hoaDonId) {
		this.hoaDonId = hoaDonId;
	}
	public DonViDoLuong getDonViDoLuong() {
		return donViDoLuong;
	}
	public void setDonViDoLuong(DonViDoLuong donViDoLuong) {
		this.donViDoLuong = donViDoLuong;
	}
	public SanPham getSanPhamId() {
		return sanPhamId;
	}
	public void setSanPhamId(SanPham sanPhamId) {
		this.sanPhamId = sanPhamId;
	}
	public LoHang getLoHangId() {
		return loHangId;
	}
	public void setLoHangId(LoHang loHangId) {
		this.loHangId = loHangId;
	}
	public int getSoLuong() {
		return soLuong;
	}
	public void setSoLuong(int soLuong) {
		this.soLuong = soLuong;
	}

	@Override
	public int hashCode() {
		return Objects.hash(donViDoLuong, hoaDonId, loHangId, sanPhamId, soLuong);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PhanBoLoHang other = (PhanBoLoHang) obj;
		return Objects.equals(donViDoLuong, other.donViDoLuong) && Objects.equals(hoaDonId, other.hoaDonId)
				&& Objects.equals(loHangId, other.loHangId) && Objects.equals(sanPhamId, other.sanPhamId)
				&& soLuong == other.soLuong;
	}

	@Override
	public String toString() {
		return "PhanBoLoHang [hoaDonId=" + hoaDonId + ", donViDoLuong=" + donViDoLuong + ", sanPhamId=" + sanPhamId
				+ ", loHangId=" + loHangId + ", soLuong=" + soLuong + "]";
	}
	
}
