package DAO;

import ConnectDB.ConnectDB;
import Entity.PhanBoLoHang;
import java.sql.*;

public class DAO_PhanBoLoHang {
    public boolean themPhanBo(PhanBoLoHang pb) {
        String sql = "INSERT INTO PhanBoLoHang (hoaDonId, sanPhamId, loHangId, soLuong) VALUES (?, ?, ?, ?)";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, pb.getHoaDonId().getId());
            pst.setString(2, pb.getSanPhamId().getId());
            pst.setString(3, pb.getLoHangId().getId());
            pst.setInt(4, pb.getSoLuong());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}