package DAO;

import Entity.ChiTietLieuMau;
import ConnectDB.ConnectDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_ChiTietLieuMau {

    // Lấy danh sách các dòng chi tiết của 1 Liều mẫu
    public List<ChiTietLieuMau> getChiTietTheoLieuMau(String lieuMauId) {
        List<ChiTietLieuMau> dsChiTiet = new ArrayList<>();
        // SỬA: Tên bảng ChiTietMauLieu, cột comboId
        String sql = "SELECT * FROM ChiTietMauLieu WHERE comboId = ?";
        
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, lieuMauId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    dsChiTiet.add(new ChiTietLieuMau(
                        rs.getString("comboId"),       // Sửa từ lieuMauId
                        rs.getString("sanPhamId"),
                        rs.getInt("tongSoLuong")       // Sửa từ soLuong
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsChiTiet;
    }

    // Thêm mới 1 chi tiết thuốc vào liều mẫu
    public boolean themChiTiet(ChiTietLieuMau ct) {
        // SỬA: Tên bảng và tên cột khớp với SQL
        String sql = "INSERT INTO ChiTietMauLieu (comboId, sanPhamId, tongSoLuong) VALUES (?, ?, ?)";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, ct.getLieuMauId());
            pst.setString(2, ct.getSanPhamId());
            pst.setInt(3, ct.getSoLuong()); // Đảm bảo getter này trong Entity trả về int số lượng thuốc
            
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa toàn bộ chi tiết của 1 liều mẫu
    public boolean xoaChiTietCuaLieu(String lieuMauId) {
        // SỬA: Tên bảng và cột
        String sql = "DELETE FROM ChiTietMauLieu WHERE comboId = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, lieuMauId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}