package Entity;

import java.util.Objects;

public class ApDungKhuyenMai {
	private String id;
	private KhuyenMai khuyenMaiId;
	private SanPham sanPhamId;
	public ApDungKhuyenMai() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ApDungKhuyenMai(String id, KhuyenMai khuyenMaiId, SanPham sanPhamId) {
		super();
		this.id = id;
		this.khuyenMaiId = khuyenMaiId;
		this.sanPhamId = sanPhamId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public KhuyenMai getKhuyenMaiId() {
		return khuyenMaiId;
	}
	public void setKhuyenMaiId(KhuyenMai khuyenMaiId) {
		this.khuyenMaiId = khuyenMaiId;
	}
	public SanPham getSanPhamId() {
		return sanPhamId;
	}
	public void setSanPhamId(SanPham sanPhamId) {
		this.sanPhamId = sanPhamId;
	}
	@Override
	public int hashCode() {
		return Objects.hash(id, khuyenMaiId, sanPhamId);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApDungKhuyenMai other = (ApDungKhuyenMai) obj;
		return Objects.equals(id, other.id) && Objects.equals(khuyenMaiId, other.khuyenMaiId)
				&& Objects.equals(sanPhamId, other.sanPhamId);
	}
	@Override
	public String toString() {
		return "ApDungKhuyenMai [id=" + id + ", khuyenMaiId=" + khuyenMaiId + ", sanPhamId=" + sanPhamId + "]";
	}
	
}
