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
                    ds.add(dk);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ds;
    }
}