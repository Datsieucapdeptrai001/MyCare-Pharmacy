package BUS;

public class KetQuaDoiChieuCa {
    // === NHÓM A: BÁN HÀNG ===
    public double a_giaGocChuaThue = 0;
    public double a_khuyenMai = 0;
    public double a_vat = 0;
    public int soHdBan = 0;

    // === NHÓM B: TRẢ HÀNG / ĐỔI HÀNG ===
    public double b_giaGocMonTra = 0;
    public double b_khuyenMaiHoanTra = 0;
    public double b_vatHoanTra = 0;
    public int soHdTra = 0;
    
    // 1. Tính toán Nhóm A
    public double get_a_DoanhThuThuan() { 
    	return a_giaGocChuaThue - a_khuyenMai; 
    }
    public double get_a_TongDoanhThu() { 
    	return get_a_DoanhThuThuan() + a_vat; 
    }

    // 2. Tính toán Nhóm B
    public double get_b_DoanhThuThuan() { 
    	return b_giaGocMonTra - b_khuyenMaiHoanTra; 
    }
    public double get_b_TongChiTra() { 
    	return get_b_DoanhThuThuan() + b_vatHoanTra; 
    }

    // 3. Tính toán Nhóm C (Thực tế = A - B)
    public double get_c_DoanhThuThuanThucTe() { 
    	return get_a_DoanhThuThuan() - get_b_DoanhThuThuan(); 
    }
    public double get_c_VatThucTe() { 
    	return a_vat - b_vatHoanTra; 
    }
    public double get_c_TongThucThu() { 
    	return get_a_TongDoanhThu() - get_b_TongChiTra(); 
    }
}