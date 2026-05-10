package DAO;

import ConnectDB.ConnectDB;
import Entity.PhanBoLoHang;
import java.sql.*;

public class DAO_PhanBoLoHang {
    public boolean themPhanBo(PhanBoLoHang pb) {
        Connection con = ConnectDB.getInstance().getConnection();
        String updateSql = "UPDATE PhanBoLoHang SET soLuong = soLuong + ? WHERE hoaDonId = ? AND sanPhamId = ? AND donViDoLuongId = ? AND loHangId = ?";
        try (PreparedStatement pstUpd = con.prepareStatement(updateSql)) {
            pstUpd.setInt(1, pb.getSoLuong());
            pstUpd.setString(2, pb.getHoaDonId().getId());
            pstUpd.setString(3, pb.getSanPhamId().getId());
            pstUpd.setString(4, pb.getDonViDoLuong().getId());
            pstUpd.setString(5, pb.getLoHangId().getId());
            if (pstUpd.executeUpdate() > 0) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql = "INSERT INTO PhanBoLoHang (hoaDonId, sanPhamId, donViDoLuongId, loHangId, soLuong) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, pb.getHoaDonId().getId());
            pst.setString(2, pb.getSanPhamId().getId());
            pst.setString(3, pb.getDonViDoLuong().getId());
            pst.setString(4, pb.getLoHangId().getId());
            pst.setInt(5, pb.getSoLuong());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean themPhanBo(Connection con, PhanBoLoHang pb) throws SQLException {
        String updateSql = "UPDATE PhanBoLoHang SET soLuong = soLuong + ? WHERE hoaDonId = ? AND sanPhamId = ? AND donViDoLuongId = ? AND loHangId = ?";
        try (PreparedStatement pstUpd = con.prepareStatement(updateSql)) {
            pstUpd.setInt(1, pb.getSoLuong());
            pstUpd.setString(2, pb.getHoaDonId().getId());
            pstUpd.setString(3, pb.getSanPhamId().getId());
            pstUpd.setString(4, pb.getDonViDoLuong().getId());
            pstUpd.setString(5, pb.getLoHangId().getId());
            if (pstUpd.executeUpdate() > 0) return true;
        }

        String sql = "INSERT INTO PhanBoLoHang (hoaDonId, sanPhamId, donViDoLuongId, loHangId, soLuong) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, pb.getHoaDonId().getId());
            pst.setString(2, pb.getSanPhamId().getId());
            pst.setString(3, pb.getDonViDoLuong().getId());
            pst.setString(4, pb.getLoHangId().getId());
            pst.setInt(5, pb.getSoLuong());
            return pst.executeUpdate() > 0;
        }
    }
}