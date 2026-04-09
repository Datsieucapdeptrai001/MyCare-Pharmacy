package BUS;

import DAO.DAO_DieuKienKhuyenMai;
import DAO.DAO_KhuyenMai;
import Entity.KhuyenMai;
import Entity.DieuKienKhuyenMai;
import java.time.LocalDateTime;
import java.util.List;

public class BUS_KhuyenMai {
    private DAO_KhuyenMai daoKhuyenMai;
    private DAO_DieuKienKhuyenMai daoDieuKienKhuyenMai;
    public BUS_KhuyenMai() {
        this.daoKhuyenMai = new DAO_KhuyenMai();
        this.daoDieuKienKhuyenMai = new DAO_DieuKienKhuyenMai();
    }

    // Lấy danh sách (dùng cho giao diện quản lý)
    public List<KhuyenMai> layDsKhuyenMai() {
        return daoKhuyenMai.layDsKhuyenMai();
    }
    
    // Thêm khuyến mãi (có validate)
    public boolean themKhuyenMai(KhuyenMai km) {
        if (!kiemTraThoiGian(km.getNgayBatDau(), km.getNgayKetThuc())) {
            System.out.println("Lỗi: Ngày kết thúc phải sau ngày bắt đầu.");
            return false;
        }
        if (kiemTraMaKM(km.getId())) {
            System.out.println("Lỗi: Mã khuyến mãi này đã tồn tại.");
            return false;
        }
        return daoKhuyenMai.themKhuyenMai(km);
    }

    // Nghiệp vụ: Kiểm tra tính hợp lệ của thời gian (Ngày bắt đầu phải trước ngày kết thúc)
    public boolean kiemTraThoiGian(LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc) {
        if (ngayBatDau == null || ngayKetThuc == null) {
            return false;
        }
        return ngayBatDau.isBefore(ngayKetThuc);
    }

    // Nghiệp vụ: Kiểm tra mã Khuyến Mãi có tồn tại không (True = Đã tồn tại)
    public boolean kiemTraMaKM(String maKM) {
        KhuyenMai km = daoKhuyenMai.layMaKM(maKM);
        return km != null;
    }

    // Nghiệp vụ: Kiểm tra xem hóa đơn hiện tại có đủ điều kiện áp dụng KM này không
    // (Bổ sung tham số tongTienHoaDon để làm cơ sở xét điều kiện)
    public boolean kiemTraDieuKienKhuyenMai(String maKM, double tongTienHoaDon) {
        KhuyenMai km = daoKhuyenMai.layMaKM(maKM);
        if (km == null) {
            return false;
        }

        // 1. Kiểm tra thời hạn
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(km.getNgayBatDau()) || now.isAfter(km.getNgayKetThuc())) {
            System.out.println("Khuyến mãi đã hết hạn hoặc chưa tới thời gian áp dụng.");
            return false;
        }

        // 2. Kiểm tra điều kiện áp dụng
        List<DieuKienKhuyenMai> dsDieuKien = daoDieuKienKhuyenMai.layTheoKhuyenMaiId(maKM);

        if (dsDieuKien == null || dsDieuKien.isEmpty()) {
            return true;
        }

        for (DieuKienKhuyenMai dk : dsDieuKien) {
            if (dk == null) {
                continue;
            }

            if ("HOA_DON".equalsIgnoreCase(dk.getDoiTuongApDung())
                    && "GIA_TRI".equalsIgnoreCase(dk.getLoaiDieuKien())) {

                if (tongTienHoaDon < dk.getGiaTri()) {
                    System.out.println("Hóa đơn chưa đủ điều kiện áp dụng khuyến mãi.");
                    return false;
                }
            }
        }

        return true;
    }


    public double apDungKM(String maKM, double tongTienHoaDon, double phanTramGiamGia, double giamToiDa) {
        if (kiemTraDieuKienKhuyenMai(maKM, tongTienHoaDon)) {
            double soTienDuocGiam = tongTienHoaDon * (phanTramGiamGia / 100.0);
            
            // Nếu có mức giảm tối đa (ví dụ: Giảm 10% nhưng tối đa không quá 50.000đ)
            if (giamToiDa > 0 && soTienDuocGiam > giamToiDa) {
                return giamToiDa;
            }
            return soTienDuocGiam;
        }
        return 0.0; // Không được giảm đồng nào nếu không đủ điều kiện
    }
}