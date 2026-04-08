package Entity;

import java.util.Objects;

import Enum.DoiTuongApDung;
import Enum.LoaiDieuKien;

public class DieuKienKhuyenMai {
	private String id;
	private LoaiDieuKien loaiDieuKien;
	private DoiTuongApDung doiTuongApDung;
	private double giaTri;
	private KhuyenMai khuyenMaiID;
	
	public DieuKienKhuyenMai(String id, LoaiDieuKien loaiDieuKien, DoiTuongApDung doiTuongApDung, double giaTri,
			KhuyenMai khuyenMaiID) {
		super();
		this.id = id;
		this.loaiDieuKien = loaiDieuKien;
		this.doiTuongApDung = doiTuongApDung;
		this.giaTri = giaTri;
		this.khuyenMaiID = khuyenMaiID;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public LoaiDieuKien getLoaiDieuKien() {
		return loaiDieuKien;
	}
	public void setLoaiDieuKien(LoaiDieuKien loaiDieuKien) {
		this.loaiDieuKien = loaiDieuKien;
	}
	public DoiTuongApDung getDoiTuongApDung() {
		return doiTuongApDung;
	}
	public void setDoiTuongApDung(DoiTuongApDung doiTuongApDung) {
		this.doiTuongApDung = doiTuongApDung;
	}
	public double getGiaTri() {
		return giaTri;
	}
	public void setGiaTri(double giaTri) {
		this.giaTri = giaTri;
	}
	public KhuyenMai getKhuyenMaiID() {
		return khuyenMaiID;
	}
	public void setKhuyenMaiID(KhuyenMai khuyenMaiID) {
		this.khuyenMaiID = khuyenMaiID;
	}
	@Override
	public int hashCode() {
		return Objects.hash(doiTuongApDung, giaTri, id, khuyenMaiID, loaiDieuKien);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DieuKienKhuyenMai other = (DieuKienKhuyenMai) obj;
		return doiTuongApDung == other.doiTuongApDung
				&& Double.doubleToLongBits(giaTri) == Double.doubleToLongBits(other.giaTri)
				&& Objects.equals(id, other.id) && Objects.equals(khuyenMaiID, other.khuyenMaiID)
				&& loaiDieuKien == other.loaiDieuKien;
	}
	@Override
	public String toString() {
		return "DieuKienKhuyenMai [id=" + id + ", loaiDieuKien=" + loaiDieuKien + ", doiTuongApDung=" + doiTuongApDung
				+ ", giaTri=" + giaTri + ", khuyenMaiID=" + khuyenMaiID + "]";
	}
	
}
