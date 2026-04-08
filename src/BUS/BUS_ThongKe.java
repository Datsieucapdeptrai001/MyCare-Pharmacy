package BUS;

import DAO.DAO_ThongKe;
import java.time.LocalDateTime;

public class BUS_ThongKe {
    private DAO_ThongKe daoThongKe;

    public BUS_ThongKe() {
        this.daoThongKe = new DAO_ThongKe();
    }

    // Validate chung cho thời gian
    private boolean kiemTraThoiGianHople(LocalDateTime tuNgay, LocalDateTime denNgay) {
        if (tuNgay == null || denNgay == null) {
            System.out.println("Vui lòng chọn đầy đủ thời gian bắt đầu và kết thúc.");
            return false;
        }
        if (tuNgay.isAfter(denNgay)) {
            System.out.println("Thời gian bắt đầu không được lớn hơn thời gian kết thúc.");
            return false;
        }
        return true;
    }

    // Lấy tổng số đơn hàng
    public int getTongSoDonHang(LocalDateTime tuNgay, LocalDateTime denNgay) {
        if (kiemTraThoiGianHople(tuNgay, denNgay)) {
            return daoThongKe.demSoLuongHoaDon(tuNgay, denNgay);
        }
        return 0;
    }

    // Lấy tổng doanh thu
    public double getTongDoanhThu(LocalDateTime tuNgay, LocalDateTime denNgay) {
        if (kiemTraThoiGianHople(tuNgay, denNgay)) {
            return daoThongKe.tinhDoanhThu(tuNgay, denNgay);
        }
        return 0.0;
    }
}