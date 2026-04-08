package Entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class KhuyenMai {
	private String id;
	private String tenKhuyenMai;
	private String moTa;
	private LocalDateTime ngayTao;
	private LocalDateTime ngayBatDau;
	private LocalDateTime ngayKetThuc;
	public KhuyenMai() {
		super();
		// TODO Auto-generated constructor stub
	}
	public KhuyenMai(String id, String tenKhuyenMai, String moTa, LocalDateTime ngayTao, LocalDateTime ngayBatDau,
			LocalDateTime ngayKetThuc) {
		super();
		this.id = id;
		this.tenKhuyenMai = tenKhuyenMai;
		this.moTa = moTa;
		this.ngayTao = ngayTao;
		this.ngayBatDau = ngayBatDau;
		this.ngayKetThuc = ngayKetThuc;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTenKhuyenMai() {
		return tenKhuyenMai;
	}
	public void setTenKhuyenMai(String tenKhuyenMai) {
		this.tenKhuyenMai = tenKhuyenMai;
	}
	public String getMoTa() {
		return moTa;
	}
	public void setMoTa(String moTa) {
		this.moTa = moTa;
	}
	public LocalDateTime getNgayTao() {
		return ngayTao;
	}
	public void setNgayTao(LocalDateTime ngayTao) {
		this.ngayTao = ngayTao;
	}
	public LocalDateTime getNgayBatDau() {
		return ngayBatDau;
	}
	public void setNgayBatDau(LocalDateTime ngayBatDau) {
		this.ngayBatDau = ngayBatDau;
	}
	public LocalDateTime getNgayKetThuc() {
		return ngayKetThuc;
	}
	public void setNgayKetThuc(LocalDateTime ngayKetThuc) {
		this.ngayKetThuc = ngayKetThuc;
	}
	@Override
	public int hashCode() {
		return Objects.hash(id, moTa, ngayBatDau, ngayKetThuc, ngayTao, tenKhuyenMai);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KhuyenMai other = (KhuyenMai) obj;
		return Objects.equals(id, other.id) && Objects.equals(moTa, other.moTa)
				&& Objects.equals(ngayBatDau, other.ngayBatDau) && Objects.equals(ngayKetThuc, other.ngayKetThuc)
				&& Objects.equals(ngayTao, other.ngayTao) && Objects.equals(tenKhuyenMai, other.tenKhuyenMai);
	}
	@Override
	public String toString() {
		return "KhuyenMai [id=" + id + ", tenKhuyenMai=" + tenKhuyenMai + ", moTa=" + moTa + ", ngayTao=" + ngayTao
				+ ", ngayBatDau=" + ngayBatDau + ", ngayKetThuc=" + ngayKetThuc + "]";
	}
	
}
