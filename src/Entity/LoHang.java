package Entity;

import java.time.LocalDateTime;
import java.util.Objects;

import Enum.TrangThaiLoHang;

public class LoHang {
	private String id;
	private String soLoHang;
	private SanPham sanPhamId;
	private int soLuongLoHang;
	private int gia;
	private LocalDateTime ngayHetHan;
	private TrangThaiLoHang trangThai;
	private LocalDateTime ngayNhap;
	private KhoHang khoHangId;
	public LoHang() {
		super();
		// TODO Auto-generated constructor stub
	}
	public LoHang(String id, String soLoHang, SanPham sanPhamId, int soLuongLoHang, int gia, LocalDateTime ngayHetHan,
			TrangThaiLoHang trangThai, LocalDateTime ngayNhap, KhoHang khoHangId) {
		super();
		this.id = id;
		this.soLoHang = soLoHang;
		this.sanPhamId = sanPhamId;
		this.soLuongLoHang = soLuongLoHang;
		this.gia = gia;
		this.ngayHetHan = ngayHetHan;
		this.trangThai = trangThai;
		this.ngayNhap = ngayNhap;
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
	public SanPham getSanPhamId() {
		return sanPhamId;
	}
	public void setSanPhamId(SanPham sanPhamId) {
		this.sanPhamId = sanPhamId;
	}
	public int getSoLuongLoHang() {
		return soLuongLoHang;
	}
	public void setSoLuongLoHang(int soLuongLoHang) {
		this.soLuongLoHang = soLuongLoHang;
	}
	public int getGia() {
		return gia;
	}
	public void setGia(int gia) {
		this.gia = gia;
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
	public LocalDateTime getNgayNhap() {
		return ngayNhap;
	}
	public void setNgayNhap(LocalDateTime ngayNhap) {
		this.ngayNhap = ngayNhap;
	}
	public KhoHang getKhoHangId() {
		return khoHangId;
	}
	public void setKhoHangId(KhoHang khoHangId) {
		this.khoHangId = khoHangId;
	}
	@Override
	public int hashCode() {
		return Objects.hash(gia, id, khoHangId, ngayHetHan, ngayNhap, sanPhamId, soLoHang, soLuongLoHang, trangThai);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LoHang other = (LoHang) obj;
		return gia == other.gia && Objects.equals(id, other.id) && Objects.equals(khoHangId, other.khoHangId)
				&& Objects.equals(ngayHetHan, other.ngayHetHan) && Objects.equals(ngayNhap, other.ngayNhap)
				&& Objects.equals(sanPhamId, other.sanPhamId) && Objects.equals(soLoHang, other.soLoHang)
				&& soLuongLoHang == other.soLuongLoHang && trangThai == other.trangThai;
	}
	@Override
	public String toString() {
		return "LoHang [id=" + id + ", soLoHang=" + soLoHang + ", sanPhamId=" + sanPhamId + ", soLuongLoHang="
				+ soLuongLoHang + ", gia=" + gia + ", ngayHetHan=" + ngayHetHan + ", trangThai=" + trangThai
				+ ", ngayNhap=" + ngayNhap + ", khoHangId=" + khoHangId + "]";
	}
	
}
