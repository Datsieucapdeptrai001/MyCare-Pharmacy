package Entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class KhachHang {
	private String id;
	private String sdt;
	private String hoVaTen;
	private LocalDateTime ngayTao;
	public KhachHang(String id, String sdt, String hoVaTen, LocalDateTime ngayTao) {
		super();
		this.id = id;
		this.sdt = sdt;
		this.hoVaTen = hoVaTen;
		this.ngayTao = ngayTao;
	}
	public KhachHang() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSdt() {
		return sdt;
	}
	public void setSdt(String sdt) {
		this.sdt = sdt;
	}
	public String getHoVaTen() {
		return hoVaTen;
	}
	public void setHoVaTen(String hoVaTen) {
		this.hoVaTen = hoVaTen;
	}
	public LocalDateTime getNgayTao() {
		return ngayTao;
	}
	public void setNgayTao(LocalDateTime ngayTao) {
		this.ngayTao = ngayTao;
	}
	@Override
	public int hashCode() {
		return Objects.hash(hoVaTen, id, ngayTao, sdt);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KhachHang other = (KhachHang) obj;
		return Objects.equals(hoVaTen, other.hoVaTen) && Objects.equals(id, other.id)
				&& Objects.equals(ngayTao, other.ngayTao) && Objects.equals(sdt, other.sdt);
	}
	@Override
	public String toString() {
		return "KhachHang [id=" + id + ", sdt=" + sdt + ", hoVaTen=" + hoVaTen + ", ngayTao=" + ngayTao + "]";
	}
}
