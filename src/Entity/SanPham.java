package Entity;

import java.time.LocalDateTime;
import java.util.Objects;

import Enum.DangBaoChe;
import Enum.DanhMucSanPham;

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
		super();
		// TODO Auto-generated constructor stub
	}
	public SanPham(String id, DanhMucSanPham danhMuc, DangBaoChe dang, String ten, String tenVietTat, String nhaSanXuat,
			String hoatChat, double thueVAT, String hamLuong, String moTa, String donViDoCoBan, LocalDateTime ngayTao) {
		super();
		this.id = id;
		this.danhMuc = danhMuc;
		this.dang = dang;
		this.ten = ten;
		this.tenVietTat = tenVietTat;
		this.nhaSanXuat = nhaSanXuat;
		this.hoatChat = hoatChat;
		this.thueVAT = thueVAT;
		this.hamLuong = hamLuong;
		this.moTa = moTa;
		this.donViDoCoBan = donViDoCoBan;
		this.ngayTao = ngayTao;
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
	public int hashCode() {
		return Objects.hash(dang, danhMuc, donViDoCoBan, hamLuong, hoatChat, id, moTa, ngayTao, nhaSanXuat, ten,
				tenVietTat, thueVAT);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SanPham other = (SanPham) obj;
		return dang == other.dang && danhMuc == other.danhMuc && Objects.equals(donViDoCoBan, other.donViDoCoBan)
				&& Objects.equals(hamLuong, other.hamLuong) && Objects.equals(hoatChat, other.hoatChat)
				&& Objects.equals(id, other.id) && Objects.equals(moTa, other.moTa)
				&& Objects.equals(ngayTao, other.ngayTao) && Objects.equals(nhaSanXuat, other.nhaSanXuat)
				&& Objects.equals(ten, other.ten) && Objects.equals(tenVietTat, other.tenVietTat)
				&& Double.doubleToLongBits(thueVAT) == Double.doubleToLongBits(other.thueVAT);
	}
	@Override
	public String toString() {
		return "SanPham [id=" + id + ", danhMuc=" + danhMuc + ", dang=" + dang + ", ten=" + ten + ", tenVietTat="
				+ tenVietTat + ", nhaSanXuat=" + nhaSanXuat + ", hoatChat=" + hoatChat + ", thueVAT=" + thueVAT
				+ ", hamLuong=" + hamLuong + ", moTa=" + moTa + ", donViDoCoBan=" + donViDoCoBan + ", ngayTao="
				+ ngayTao + "]";
	}
	
	
}
