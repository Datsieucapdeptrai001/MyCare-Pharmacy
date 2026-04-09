package DAO;

import ConnectDB.ConnectDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class DAO_ThongKe {

    public DAO_ThongKe() {
    }

    // 1. Đếm tổng số lượng hóa đơn bán ra trong khoảng thời gian
    public int demSoLuongHoaDon(LocalDateTime tuNgay, LocalDateTime denNgay) {
        int soLuong = 0;
        String sql = "SELECT COUNT(*) as TongSo FROM HoaDon WHERE ngayLapHD BETWEEN ? AND ? AND loaiHD = 'BAN_HANG'";
        
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setTimestamp(1, Timestamp.valueOf(tuNgay));
            pst.setTimestamp(2, Timestamp.valueOf(denNgay));
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    soLuong = rs.getInt("TongSo");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return soLuong;
    }

    // 2. Tính tổng doanh thu bằng cách nhân (Số lượng * Giá bán) từ Chi Tiết Hóa Đơn
    public double tinhDoanhThu(LocalDateTime tuNgay, LocalDateTime denNgay) {
        double doanhThu = 0;
        // Gom dữ liệu từ HoaDon -> ChiTietHoaDon -> LoHang để lấy giá trị chính xác
        String sql = "SELECT SUM(ct.soLuong * lh.gia) AS DoanhThu " +
                     "FROM HoaDon hd " +
                     "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId " +
                     "JOIN PhanBoLoHang pblh ON ct.hoaDonId = pblh.hoaDonId AND ct.sanPhamId = pblh.sanPhamId " +
                     "JOIN LoHang lh ON pblh.loHangId = lh.id " +
                     "WHERE hd.ngayLapHD BETWEEN ? AND ? AND hd.loaiHD = 'BAN_HANG'";
                     
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setTimestamp(1, Timestamp.valueOf(tuNgay));
            pst.setTimestamp(2, Timestamp.valueOf(denNgay));
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    doanhThu = rs.getDouble("DoanhThu");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doanhThu;
    }
    public double tinhLoiNhuan(LocalDateTime tuNgay, LocalDateTime denNgay) {
        double tongLoiNhuan = 0.0;
        
        // Câu SQL tính lợi nhuận: (Giá bán - Giá nhập) * Số lượng
        // LƯU Ý: Bạn CẦN đổi tên bảng và tên cột cho khớp với Database thực tế của bạn
        String sql = "SELECT SUM(ct.SoLuong * (ct.DonGia - sp.GiaNhap)) AS LoiNhuan "
                   + "FROM HoaDon hd "
                   + "JOIN ChiTietHoaDon ct ON hd.MaHoaDon = ct.MaHoaDon "
                   + "JOIN SanPham sp ON ct.MaSanPham = sp.MaSanPham "
                   + "WHERE hd.NgayLap BETWEEN ? AND ?";

        // Thay ConnectDB.getConnection() bằng class kết nối DB của bạn
        try (Connection con = ConnectDB.getConnection(); 
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            // Chuyển đổi LocalDateTime sang Timestamp để truyền vào SQL
            pst.setTimestamp(1, Timestamp.valueOf(tuNgay));
            pst.setTimestamp(2, Timestamp.valueOf(denNgay));
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    tongLoiNhuan = rs.getDouble("LoiNhuan");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return tongLoiNhuan;
    }
}