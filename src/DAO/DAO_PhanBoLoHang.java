package DAO;

import ConnectDB.ConnectDB;
import Entity.PhanBoLoHang;
import java.sql.*;

public class DAO_PhanBoLoHang {
    public boolean themPhanBo(PhanBoLoHang pb) {
        // ĐÃ SỬA: Thêm donViDoLuongId vào danh sách cột và thêm 1 dấu ? vào VALUES
        String sql = "INSERT INTO PhanBoLoHang (hoaDonId, sanPhamId, donViDoLuongId, loHangId, soLuong) VALUES (?, ?, ?, ?, ?)";
        Connection con = ConnectDB.getInstance().getConnection();
        
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, pb.getHoaDonId().getId());
            pst.setString(2, pb.getSanPhamId().getId());
            
            // ĐÃ SỬA: Thêm dòng truyền giá trị Mã Đơn Vị Đo Lường xuống CSDL
            pst.setString(3, pb.getDonViDoLuongId().getId()); 
            
            pst.setString(4, pb.getLoHangId().getId());
            pst.setInt(5, pb.getSoLuong());
            
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}