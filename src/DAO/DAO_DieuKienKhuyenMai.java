package DAO;

import ConnectDB.ConnectDB;
import Entity.DieuKienKhuyenMai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DAO_DieuKienKhuyenMai {

    public boolean themDieuKien(DieuKienKhuyenMai dk) {
        String sql = "INSERT INTO DieuKienKhuyenMai (id, loaiDieuKien, doiTuongApDung, giaTri, khuyenMaiId) VALUES (?, ?, ?, ?, ?)";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, dk.getId());
            pst.setString(2, dk.getLoaiDieuKien());
            pst.setString(3, dk.getDoiTuongApDung());
            pst.setDouble(4, dk.getGiaTri());
            pst.setString(5, dk.getKhuyenMaiId());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean capNhatDieuKienTheoMaKM(DieuKienKhuyenMai dk) {
        String sql = "UPDATE DieuKienKhuyenMai SET loaiDieuKien=?, doiTuongApDung=?, giaTri=? WHERE khuyenMaiId=?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, dk.getLoaiDieuKien());
            pst.setString(2, dk.getDoiTuongApDung());
            pst.setDouble(3, dk.getGiaTri());
            pst.setString(4, dk.getKhuyenMaiId());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean xoaTheoMaKM(String khuyenMaiId) {
        String sql = "DELETE FROM DieuKienKhuyenMai WHERE khuyenMaiId=?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, khuyenMaiId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean kiemTraTonTai(String khuyenMaiId) {
        String sql = "SELECT 1 FROM DieuKienKhuyenMai WHERE khuyenMaiId=?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, khuyenMaiId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<DieuKienKhuyenMai> layTheoKhuyenMaiId(String khuyenMaiId) {
        List<DieuKienKhuyenMai> ds = new ArrayList<>();
        String sql = "SELECT * FROM DieuKienKhuyenMai WHERE khuyenMaiId = ?";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, khuyenMaiId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    DieuKienKhuyenMai dk = new DieuKienKhuyenMai();
                    dk.setId(rs.getString("id"));
                    dk.setLoaiDieuKien(rs.getString("loaiDieuKien"));
                    dk.setDoiTuongApDung(rs.getString("doiTuongApDung"));
                    dk.setGiaTri(rs.getDouble("giaTri"));
                    dk.setKhuyenMaiId(rs.getString("khuyenMaiId"));
                    ds.add(dk);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }
}