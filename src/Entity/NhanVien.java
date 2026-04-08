package Entity;

import java.util.Objects;

import Enum.ChucVu;
import Enum.TrangThaiLamViec;

public class NhanVien {
	private String nhanVien;
	private String hoVaTen;
	private String soChungChiHanhNghe;
	private String sdt;
	private String email;
	private ChucVu chucVu;
	private TrangThaiLamViec trangThaiLamViec;
	public String getNhanVien() {
		return nhanVien;
	}
	public void setNhanVien(String nhanVien) {
		this.nhanVien = nhanVien;
	}
	public String getHoVaTen() {
		return hoVaTen;
	}
	public void setHoVaTen(String hoVaTen) {
		this.hoVaTen = hoVaTen;
	}
	public String getSoChungChiHanhNghe() {
		return soChungChiHanhNghe;
	}
	public void setSoChungChiHanhNghe(String soChungChiHanhNghe) {
		this.soChungChiHanhNghe = soChungChiHanhNghe;
	}
	public String getSdt() {
		return sdt;
	}
	public void setSdt(String sdt) {
		this.sdt = sdt;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public ChucVu getChucVu() {
		return chucVu;
	}
	public void setChucVu(ChucVu chucVu) {
		this.chucVu = chucVu;
	}
	public TrangThaiLamViec getTrangThaiLamViec() {
		return trangThaiLamViec;
	}
	public void setTrangThaiLamViec(TrangThaiLamViec trangThaiLamViec) {
		this.trangThaiLamViec = trangThaiLamViec;
	}
	
	
	
}
