package Entity;

import java.util.Objects;

import Enumeration.VaiTro;

public class TaiKhoan {
	private String id;
	private NhanVien nhanVienId;
	private VaiTro vaiTro;
	private String tenDangNhap;
	private String matKhau;
	public TaiKhoan(String id, NhanVien nhanVienId, VaiTro vaiTro, String tenDangNhap, String matKhau) {
		super();
		this.id = id;
		this.nhanVienId = nhanVienId;
		this.vaiTro = vaiTro;
		this.tenDangNhap = tenDangNhap;
		this.matKhau = matKhau;
	}
	
	public TaiKhoan() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public NhanVien getNhanVienId() {
		return nhanVienId;
	}
	public void setNhanVienId(NhanVien nhanVienId) {
		this.nhanVienId = nhanVienId;
	}
	public VaiTro getVaiTro() {
		return vaiTro;
	}
	public void setVaiTro(VaiTro vaiTro) {
		this.vaiTro = vaiTro;
	}
	public String getTenDangNhap() {
		return tenDangNhap;
	}
	public void setTenDangNhap(String tenDangNhap) {
		this.tenDangNhap = tenDangNhap;
	}
	public String getMatKhau() {
		return matKhau;
	}
	public void setMatKhau(String matKhau) {
		this.matKhau = matKhau;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, matKhau, nhanVienId, tenDangNhap, vaiTro);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaiKhoan other = (TaiKhoan) obj;
		return Objects.equals(id, other.id) && Objects.equals(matKhau, other.matKhau)
				&& Objects.equals(nhanVienId, other.nhanVienId) && Objects.equals(tenDangNhap, other.tenDangNhap)
				&& vaiTro == other.vaiTro;
	}

	@Override
	public String toString() {
		return "TaiKhoan [id=" + id + ", nhanVienId=" + nhanVienId + ", vaiTro=" + vaiTro + ", tenDangNhap="
				+ tenDangNhap + ", matKhau=" + matKhau + "]";
	}
	
	
}