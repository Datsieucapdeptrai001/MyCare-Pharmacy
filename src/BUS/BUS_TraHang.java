package BUS;

import DAO.DAO_HoaDon;
import Entity.HoaDon;
import Enum.LoaiHoaDon;
import java.time.LocalDateTime;

public class BUS_TraHang {
    private DAO_HoaDon daoHoaDon;

    public BUS_TraHang() {
        this.daoHoaDon = new DAO_HoaDon();
    }

    // Nghiệp vụ: Kiểm tra điều kiện trả hàng (Ví dụ: Chỉ cho trả trong vòng 7 ngày và là HD mua hàng)
    public boolean kiemTraDieuKien(String maHoaDonGoc) {
        HoaDon hd = daoHoaDon.layHoaDonTheoMa(maHoaDonGoc);
        if (hd == null) {
            System.out.println("Không tìm thấy hóa đơn này.");
            return false;
        }
        if (hd.getLoaiHD() != LoaiHoaDon.BAN_HANG) {
            System.out.println("Lỗi: Chỉ được trả hàng cho hóa đơn Bán Hàng.");
            return false;
        }
        
        // Kiểm tra thời gian (Quy định nhà thuốc: Cho phép trả trong 7 ngày)
        LocalDateTime ngayHetHanTra = hd.getNgayLapHD().plusDays(7);
        if (LocalDateTime.now().isAfter(ngayHetHanTra)) {
            System.out.println("Hóa đơn đã quá hạn đổi/trả (quá 7 ngày).");
            return false;
        }
        return true;
    }

    // Nghiệp vụ: Xác định mức hoàn tiền (Ví dụ: Thuốc bị lỗi thì hoàn 100%, khách tự ý trả hoàn 80%)
    public double xacDinhMucHoanTien(double tongTienHD, double phanTramHoan) {
        return tongTienHD * (phanTramHoan / 100.0);
    }

    // Tính chênh lệch nếu có bù trừ
    public double tinhTienChenhLech(double tienKhachMua, double tienHoanLai) {
        return tienKhachMua - tienHoanLai;
    }
}