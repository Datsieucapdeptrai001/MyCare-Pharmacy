package BUS;

import DAO.DAO_DieuKienKhuyenMai;
import DAO.DAO_HinhThucKhuyenMai;
import DAO.DAO_KhuyenMai;
import Entity.KhuyenMai;
import Entity.DieuKienKhuyenMai;
import java.time.LocalDateTime;
import java.util.List;
import Entity.HinhThucKhuyenMai;
import Enum.DoiTuongApDung;
import Enum.LoaiHinhThuc;
public class BUS_KhuyenMai {
    private DAO_KhuyenMai daoKhuyenMai;
    private DAO_DieuKienKhuyenMai daoDieuKienKhuyenMai;
    private DAO_HinhThucKhuyenMai daoHinhThucKhuyenMai;
    public BUS_KhuyenMai() {
        this.daoKhuyenMai = new DAO_KhuyenMai();
        this.daoDieuKienKhuyenMai = new DAO_DieuKienKhuyenMai();
        this.daoHinhThucKhuyenMai = new DAO_HinhThucKhuyenMai();
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


    public double apDungKM(String maKM, double tongTienHoaDon) {
        KhuyenMai km = daoKhuyenMai.layMaKM(maKM);
        if (km == null) {
            return tongTienHoaDon;
        }

        if (!kiemTraDieuKienKhuyenMai(maKM, tongTienHoaDon)) {
            return tongTienHoaDon;
        }

        HinhThucKhuyenMai htkm = daoHinhThucKhuyenMai.layTheoMaKM(maKM);
        if (htkm == null) {
            return tongTienHoaDon;
        }

        if (htkm.getLoaiHinhThuc() == LoaiHinhThuc.GIAM_THEO_PHAN_TRAM
                && htkm.getDoiTuongApDung() == DoiTuongApDung.HOA_DON) {

            double tienGiam = tongTienHoaDon * htkm.getGiaTri() / 100.0;

            if (htkm.getGiamToiDa() > 0 && tienGiam > htkm.getGiamToiDa()) {
                tienGiam = htkm.getGiamToiDa();
            }

            return tongTienHoaDon - tienGiam;
        }

        return tongTienHoaDon;
    }
}