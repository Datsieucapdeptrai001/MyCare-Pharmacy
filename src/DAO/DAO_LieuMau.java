package DAO;

import Entity.LieuMau;
import ConnectDB.ConnectDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_LieuMau {
    
    // Lấy danh sách tất cả các Liều Mẫu
    public List<LieuMau> getTatCaLieuMau() {
        List<LieuMau> dsLieu = new ArrayList<>();
        String sql = "SELECT * FROM LieuMau ORDER BY nhomBenh, tenLieu";
        
        // Đã sửa thành ConnectDB.getInstance().getConnection()
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                dsLieu.add(new LieuMau(
                    rs.getString("id"),
                    rs.getString("tenLieu"),
                    rs.getString("nhomBenh"),
                    rs.getString("moTa")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsLieu;
    }

    // Lấy chi tiết thuốc của 1 liều mẫu
    public List<Object[]> getChiTietThuocCuaLieu(String idLieuMau) {
        List<Object[]> dsChiTiet = new ArrayList<>();
        String sql = "SELECT sp.id, sp.ten, sp.nhomBenhLy, ct.soLuong, dv.ten AS donViTinh, dv.gia AS donGia " +
                     "FROM ChiTietLieuMau ct " +
                     "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                     "JOIN DonViDoLuong dv ON sp.id = dv.sanPhamId AND sp.donViDoCoBan = dv.ten " +
                     "WHERE ct.lieuMauId = ?";
                     
        // Đã sửa thành ConnectDB.getInstance().getConnection()
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, idLieuMau);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                dsChiTiet.add(new Object[]{
                    rs.getString("id"),           
                    rs.getString("ten"),          
                    rs.getString("nhomBenhLy"),   
                    rs.getInt("soLuong"),         
                    rs.getString("donViTinh"),    
                    rs.getDouble("donGia")        
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsChiTiet;
    }
}