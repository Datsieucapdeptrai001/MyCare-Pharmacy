package DAO;

import ConnectDB.ConnectDB;
import Entity.HoaDon;
import Entity.KhachHang;
import Entity.KhuyenMai;
import Entity.NhanVien;
import Enum.LoaiHoaDon;
import Enum.PhuongThucThanhToan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class DAO_HoaDon {

    public DAO_HoaDon() {}

    public boolean themHoaDon(HoaDon hd) {
        // hoaDonGocId dùng cho trường hợp hóa đơn này là hóa đơn Đổi/Trả tham chiếu đến hóa đơn mua ban đầu
        String sql = "INSERT INTO HoaDon (id, loaiHD, ghiChu, ngayLapHD, nhanVienId, KhachHangId, khuyenMaiId, phuongThucThanhToan, hoaDonGocId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int n = 0;
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, hd.getId());
            pst.setString(2, hd.getLoaiHD().name());
            pst.setString(3, hd.getGhiChu());
            
            LocalDateTime ngayLap = hd.getNgayLapHD() != null ? hd.getNgayLapHD() : LocalDateTime.now();
            pst.setTimestamp(4, Timestamp.valueOf(ngayLap));
            
            pst.setString(5, hd.getNhanVienId() != null ? hd.getNhanVienId().getNhanVien() : null);
            pst.setString(6, hd.getKhachHangId() != null ? hd.getKhachHangId().getId() : null);
            pst.setString(7, hd.getKhuyenMaiId() != null ? hd.getKhuyenMaiId().getId() : null);
            pst.setString(8, hd.getPhuongThucThanhToan() != null ? hd.getPhuongThucThanhToan().name() : null);
            pst.setString(9, hd.getHoaDonGocId() != null ? hd.getHoaDonGocId().getId() : null);

            n = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public HoaDon layHoaDonTheoMa(String maHoaDon) {
        HoaDon hd = null;
        String sql = "SELECT * FROM HoaDon WHERE id = ?";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maHoaDon);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    hd = new HoaDon();
                    hd.setId(rs.getString("id"));
                    if (rs.getString("loaiHD") != null) hd.setLoaiHD(LoaiHoaDon.valueOf(rs.getString("loaiHD")));
                    hd.setGhiChu(rs.getString("ghiChu"));
                    if (rs.getTimestamp("ngayLapHD") != null) hd.setNgayLapHD(rs.getTimestamp("ngayLapHD").toLocalDateTime());
                    if (rs.getString("phuongThucThanhToan") != null) hd.setPhuongThucThanhToan(PhuongThucThanhToan.valueOf(rs.getString("phuongThucThanhToan")));
                    
                    // Bạn có thể set tạm ID cho các object liên kết (Lazy loading)
                    // ... (set NhanVien, KhachHang)
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hd;
    }

    // Hàm cập nhật trạng thái (Trong Entity của bạn đang quản lý bằng Enum LoaiHoaDon: BAN_HANG, TRA_HANG)
    public boolean capNhatTrangThai(String idHD, LoaiHoaDon loaiMoi) {
        String sql = "UPDATE HoaDon SET loaiHD = ? WHERE id = ?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, loaiMoi.name());
            pst.setString(2, idHD);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}