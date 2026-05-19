package DAO;

import Entity.ChiTietLieuMau;
import ConnectDB.ConnectDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_ChiTietLieuMau {

    // Lấy danh sách các dòng chi tiết (chỉ mã thuốc và số lượng) của 1 Liều mẫu
    public List<ChiTietLieuMau> getChiTietTheoLieuMau(String lieuMauId) {
        List<ChiTietLieuMau> dsChiTiet = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietLieuMau WHERE lieuMauId = ?";
        
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, lieuMauId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    dsChiTiet.add(new ChiTietLieuMau(
                        rs.getString("lieuMauId"),
                        rs.getString("sanPhamId"),
                        rs.getInt("soLuong")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsChiTiet;
    }

    // Thêm mới 1 chi tiết thuốc vào liều mẫu (Dùng khi bạn tạo Liều Mẫu mới)
    public boolean themChiTiet(ChiTietLieuMau ct) {
        String sql = "INSERT INTO ChiTietLieuMau (lieuMauId, sanPhamId, soLuong) VALUES (?, ?, ?)";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, ct.getLieuMauId());
            pst.setString(2, ct.getSanPhamId());
            pst.setInt(3, ct.getSoLuong());
            
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Xóa toàn bộ chi tiết của 1 liều mẫu (Dùng khi muốn cập nhật/xóa hẳn liều mẫu đó)
    public boolean xoaChiTietCuaLieu(String lieuMauId) {
        String sql = "DELETE FROM ChiTietLieuMau WHERE lieuMauId = ?";
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