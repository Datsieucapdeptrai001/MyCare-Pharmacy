package Entity;

import java.util.Objects;

public class DieuKienKhuyenMai {
    private String id;
    private String loaiDieuKien;
    private String doiTuongApDung;
    private double giaTri;
    private String khuyenMaiId;

    public DieuKienKhuyenMai() {
    }

    public DieuKienKhuyenMai(String id, String loaiDieuKien, String doiTuongApDung, double giaTri, String khuyenMaiId) {
        this.id = id;
        this.loaiDieuKien = loaiDieuKien;
        this.doiTuongApDung = doiTuongApDung;
        this.giaTri = giaTri;
        this.khuyenMaiId = khuyenMaiId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoaiDieuKien() {
        return loaiDieuKien;
    }

    public void setLoaiDieuKien(String loaiDieuKien) {
        this.loaiDieuKien = loaiDieuKien;
    }

    public String getDoiTuongApDung() {
        return doiTuongApDung;
    }

    public void setDoiTuongApDung(String doiTuongApDung) {
        this.doiTuongApDung = doiTuongApDung;
    }

    public double getGiaTri() {
        return giaTri;
    }

    public void setGiaTri(double giaTri) {
        this.giaTri = giaTri;
    }

    public String getKhuyenMaiId() {
        return khuyenMaiId;
    }

    public void setKhuyenMaiId(String khuyenMaiId) {
        this.khuyenMaiId = khuyenMaiId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, loaiDieuKien, doiTuongApDung, giaTri, khuyenMaiId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DieuKienKhuyenMai other = (DieuKienKhuyenMai) obj;
        return Double.compare(other.giaTri, giaTri) == 0
                && Objects.equals(id, other.id)
                && Objects.equals(loaiDieuKien, other.loaiDieuKien)
                && Objects.equals(doiTuongApDung, other.doiTuongApDung)
                && Objects.equals(khuyenMaiId, other.khuyenMaiId);
    }

    @Override
    public String toString() {
        return "DieuKienKhuyenMai [id=" + id
                + ", loaiDieuKien=" + loaiDieuKien
                + ", doiTuongApDung=" + doiTuongApDung
                + ", giaTri=" + giaTri
                + ", khuyenMaiId=" + khuyenMaiId + "]";
    }
}