package BUS;

import Entity.DoiChieuCa;

/**
 * Lớp BUS bọc Entity.DoiChieuCa và cung cấp các tính toán nghiệp vụ.
 * DAO trả về DoiChieuCa (dữ liệu thô), BUS_ThongKe wrap vào đây rồi
 * trả cho GUI — GUI chỉ cần gọi computed getter, không tự tính.
 */
public class BUS_KetQuaDoiChieuCa {

    private final DoiChieuCa data;

    public BUS_KetQuaDoiChieuCa(DoiChieuCa data) {
        this.data = data;
    }

    // =====================================================================
    // DELEGATE — truy cập dữ liệu thô từ Entity
    // =====================================================================

    public int    getSoHdBan()              { return data.getSoHdBan(); }
    public double getA_giaGocChuaThue()     { return data.getA_giaGocChuaThue(); }
    public double getA_khuyenMai()          { return data.getA_khuyenMai(); }
    public double getA_vat()                { return data.getA_vat(); }

    public int    getSoHdTra()              { return data.getSoHdTra(); }
    public double getB_giaGocMonTra()       { return data.getB_giaGocMonTra(); }
    public double getB_khuyenMaiHoanTra()   { return data.getB_khuyenMaiHoanTra(); }
    public double getB_vatHoanTra()         { return data.getB_vatHoanTra(); }

    // =====================================================================
    // COMPUTED — nghiệp vụ tính toán, đây là lý do tồn tại của lớp này
    // =====================================================================

    /** Doanh thu thuần nhóm A = giá gốc - khuyến mãi */
    public double get_a_DoanhThuThuan() {
        return data.getA_giaGocChuaThue() - data.getA_khuyenMai();
    }

    /** Tổng doanh thu nhóm A = doanh thu thuần + VAT */
    public double get_a_TongDoanhThu() {
        return get_a_DoanhThuThuan() + data.getA_vat();
    }

    /** Doanh thu thuần nhóm B = giá gốc món trả - khuyến mãi hoàn trả */
    public double get_b_DoanhThuThuan() {
        return data.getB_giaGocMonTra() - data.getB_khuyenMaiHoanTra();
    }

    /** Tổng chi trả nhóm B = doanh thu thuần trả + VAT hoàn trả */
    public double get_b_TongChiTra() {
        return get_b_DoanhThuThuan() + data.getB_vatHoanTra();
    }

    /** Doanh thu thuần thực tế (C) = A thuần - B thuần */
    public double get_c_DoanhThuThuanThucTe() {
        return get_a_DoanhThuThuan() - get_b_DoanhThuThuan();
    }

    /** VAT thực tế (C) = VAT bán ra - VAT hoàn trả */
    public double get_c_VatThucTe() {
        return data.getA_vat() - data.getB_vatHoanTra();
    }

    /** Tổng thực thu (C) = Tổng A - Tổng B */
    public double get_c_TongThucThu() {
        return get_a_TongDoanhThu() - get_b_TongChiTra();
    }
}