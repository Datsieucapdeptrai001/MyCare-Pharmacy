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
        // SỬA: Đổi tên bảng thành MauLieu và các cột tương ứng
        String sql = "SELECT * FROM MauLieu ORDER BY nhomBenh, tenCombo";
        
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                // SỬA: Map đúng tên cột trong bảng MauLieu của SQL
                dsLieu.add(new LieuMau(
                    rs.getString("comboId"),   // DB dùng comboId thay vì id
                    rs.getString("tenCombo"),  // DB dùng tenCombo thay vì tenLieu
                    rs.getString("nhomBenh"),
                    rs.getString("ghiChu")     // DB dùng ghiChu thay vì moTa
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
        // Đã bổ sung LEFT JOIN ViTriThuoc và cột viTriThuocHienThi
        String sql = "SELECT sp.id, sp.ten, sp.nhomBenhLy, ct.tongSoLuong AS soLuong, dv.ten AS donViTinh, dv.gia AS donGia, " +
                     "ISNULL(vt.khu, '') + ' - ' + ISNULL(vt.ke, '') AS viTriThuocHienThi " +
                     "FROM ChiTietMauLieu ct " +
                     "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                     "JOIN DonViDoLuong dv ON sp.id = dv.sanPhamId AND sp.donViDoCoBan = dv.ten " +
                     "LEFT JOIN ViTriThuoc vt ON sp.viTriId = vt.id " +
                     "WHERE ct.comboId = ?";
                     
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, idLieuMau); // Dấu ? sẽ được thay bằng mã liều mẫu ở đây
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                dsChiTiet.add(new Object[]{
                    rs.getString("id"),           
                    rs.getString("ten"),          
                    rs.getString("nhomBenhLy"),   
                    rs.getInt("soLuong"),         
                    rs.getString("donViTinh"),    
                    rs.getDouble("donGia"),
                    rs.getString("viTriThuocHienThi") // <--- Lấy thêm vị trí từ Database
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsChiTiet;
    }
}