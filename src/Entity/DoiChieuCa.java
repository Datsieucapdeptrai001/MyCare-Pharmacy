package Entity;

import java.util.Objects;

public class DoiChieuCa {
    // Nhóm A: BAN_HANG + DOI_HANG xuất mới
    private int soHdBan;
    private double a_giaGocChuaThue;
    private double a_khuyenMai;
    private double a_vat;

    // Nhóm B: TRA_HANG / DOI_HANG nhận lại (hoàn thành)
    private int soHdTra;
    private double b_giaGocMonTra;
    private double b_khuyenMaiHoanTra;
    private double b_vatHoanTra;

    public DoiChieuCa() {}

    public DoiChieuCa(int soHdBan, double a_giaGocChuaThue, double a_khuyenMai, double a_vat,
                      int soHdTra, double b_giaGocMonTra, double b_khuyenMaiHoanTra, double b_vatHoanTra) {
        this.soHdBan = soHdBan;
        this.a_giaGocChuaThue = a_giaGocChuaThue;
        this.a_khuyenMai = a_khuyenMai;
        this.a_vat = a_vat;
        this.soHdTra = soHdTra;
        this.b_giaGocMonTra = b_giaGocMonTra;
        this.b_khuyenMaiHoanTra = b_khuyenMaiHoanTra;
        this.b_vatHoanTra = b_vatHoanTra;
    }

    public int getSoHdBan() { return soHdBan; }
    public void setSoHdBan(int soHdBan) { this.soHdBan = soHdBan; }

    public double getA_giaGocChuaThue() { return a_giaGocChuaThue; }
    public void setA_giaGocChuaThue(double a_giaGocChuaThue) { this.a_giaGocChuaThue = a_giaGocChuaThue; }

    public double getA_khuyenMai() { return a_khuyenMai; }
    public void setA_khuyenMai(double a_khuyenMai) { this.a_khuyenMai = a_khuyenMai; }

    public double getA_vat() { return a_vat; }
    public void setA_vat(double a_vat) { this.a_vat = a_vat; }

    public int getSoHdTra() { return soHdTra; }
    public void setSoHdTra(int soHdTra) { this.soHdTra = soHdTra; }

    public double getB_giaGocMonTra() { return b_giaGocMonTra; }
    public void setB_giaGocMonTra(double b_giaGocMonTra) { this.b_giaGocMonTra = b_giaGocMonTra; }

    public double getB_khuyenMaiHoanTra() { return b_khuyenMaiHoanTra; }
    public void setB_khuyenMaiHoanTra(double b_khuyenMaiHoanTra) { this.b_khuyenMaiHoanTra = b_khuyenMaiHoanTra; }

    public double getB_vatHoanTra() { return b_vatHoanTra; }
    public void setB_vatHoanTra(double b_vatHoanTra) { this.b_vatHoanTra = b_vatHoanTra; }

    @Override
    public int hashCode() {
        return Objects.hash(soHdBan, a_giaGocChuaThue, a_khuyenMai, a_vat,
                soHdTra, b_giaGocMonTra, b_khuyenMaiHoanTra, b_vatHoanTra);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DoiChieuCa other = (DoiChieuCa) obj;
        return soHdBan == other.soHdBan
                && soHdTra == other.soHdTra
                && Double.doubleToLongBits(a_giaGocChuaThue) == Double.doubleToLongBits(other.a_giaGocChuaThue)
                && Double.doubleToLongBits(a_khuyenMai)       == Double.doubleToLongBits(other.a_khuyenMai)
                && Double.doubleToLongBits(a_vat)             == Double.doubleToLongBits(other.a_vat)
                && Double.doubleToLongBits(b_giaGocMonTra)    == Double.doubleToLongBits(other.b_giaGocMonTra)
                && Double.doubleToLongBits(b_khuyenMaiHoanTra)== Double.doubleToLongBits(other.b_khuyenMaiHoanTra)
                && Double.doubleToLongBits(b_vatHoanTra)      == Double.doubleToLongBits(other.b_vatHoanTra);
    }

    @Override
    public String toString() {
        return "DoiChieuCa ["
                + "soHdBan=" + soHdBan
                + ", a_giaGocChuaThue=" + a_giaGocChuaThue
                + ", a_khuyenMai=" + a_khuyenMai
                + ", a_vat=" + a_vat
                + ", soHdTra=" + soHdTra
                + ", b_giaGocMonTra=" + b_giaGocMonTra
                + ", b_khuyenMaiHoanTra=" + b_khuyenMaiHoanTra
                + ", b_vatHoanTra=" + b_vatHoanTra
                + "]";
    }
}