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
 // Tính giá trị trung bình trên mỗi đơn hàng
    public double getGiaTriTBTrenDon(LocalDateTime tuNgay, LocalDateTime denNgay) {
        if (kiemTraThoiGianHople(tuNgay, denNgay)) {
            int tongSoDon = getTongSoDonHang(tuNgay, denNgay);
            
            // Tránh lỗi chia cho 0 nếu không có đơn hàng nào
            if (tongSoDon == 0) {
                return 0.0;
            }
            
            double tongDoanhThu = getTongDoanhThu(tuNgay, denNgay);
            return tongDoanhThu / tongSoDon;
        }
        return 0.0;
    }
 // Lấy tổng lợi nhuận
    public double getTongLoiNhuan(LocalDateTime tuNgay, LocalDateTime denNgay) {
        if (kiemTraThoiGianHople(tuNgay, denNgay)) {
            // Gọi hàm tính lợi nhuận từ DAO
            return daoThongKe.tinhLoiNhuan(tuNgay, denNgay); 
        }
        return 0.0;
    }
}