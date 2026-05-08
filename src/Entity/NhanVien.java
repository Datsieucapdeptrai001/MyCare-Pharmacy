package Entity;

import java.util.Objects;

import Enumeration.ChucVu;
import Enumeration.TrangThaiLamViec;

public class NhanVien {
	private String nhanVien;
	private String hoVaTen;
	private String soChungChiHanhNghe;
	private String sdt;
	private String email;
	private String diaChi; // Đã thêm thuộc tính địa chỉ
	private ChucVu chucVu;
	private TrangThaiLamViec trangThaiLamViec;
	
	// 1. Hàm khởi tạo rỗng
	public NhanVien() {
		super();
	}
	
	// 2. Hàm khởi tạo 1 tham số (Dùng để gán mã nhân viên khi tạo hóa đơn)
	public NhanVien(String nhanVien) {
	    this.nhanVien = nhanVien;
	}
	
	// 3. Hàm khởi tạo đầy đủ tham số (Cập nhật thêm diaChi)
	public NhanVien(String nhanVien, String hoVaTen, String soChungChiHanhNghe, String sdt, String email, String diaChi, ChucVu chucVu,
			TrangThaiLamViec trangThaiLamViec) {
		super();
		this.nhanVien = nhanVien;
		this.hoVaTen = hoVaTen;
		this.soChungChiHanhNghe = soChungChiHanhNghe;
		this.sdt = sdt;
		this.email = email;
		this.diaChi = diaChi;
		this.chucVu = chucVu;
		this.trangThaiLamViec = trangThaiLamViec;
	}
	
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
	public String getDiaChi() {
		return diaChi;
	}
	public void setDiaChi(String diaChi) {
		this.diaChi = diaChi;
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

	@Override
	public int hashCode() {
		return Objects.hash(chucVu, diaChi, email, hoVaTen, nhanVien, sdt, soChungChiHanhNghe, trangThaiLamViec);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NhanVien other = (NhanVien) obj;
		return chucVu == other.chucVu && Objects.equals(diaChi, other.diaChi) && Objects.equals(email, other.email) 
				&& Objects.equals(hoVaTen, other.hoVaTen) && Objects.equals(nhanVien, other.nhanVien) 
				&& Objects.equals(sdt, other.sdt) && Objects.equals(soChungChiHanhNghe, other.soChungChiHanhNghe)
				&& trangThaiLamViec == other.trangThaiLamViec;
	}

	@Override
	public String toString() {
		return "NhanVien [nhanVien=" + nhanVien + ", hoVaTen=" + hoVaTen + ", soChungChiHanhNghe=" + soChungChiHanhNghe
				+ ", sdt=" + sdt + ", email=" + email + ", diaChi=" + diaChi + ", chucVu=" + chucVu + ", trangThaiLamViec=" + trangThaiLamViec
				+ "]";
	}
}