package DAO;

import ConnectDB.ConnectDB;
import Entity.KhuyenMai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DAO_KhuyenMai {

    public DAO_KhuyenMai() {
    }

    // Thêm chương trình khuyến mãi mới
    public boolean themKhuyenMai(KhuyenMai km) {
        String sql = "INSERT INTO KhuyenMai (id, tenKhuyenMai, moTa, ngayTao, ngayBatDau, ngayKetThuc) VALUES (?, ?, ?, ?, ?, ?)";
        int n = 0;
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, km.getId());
            pst.setString(2, km.getTenKhuyenMai());
            pst.setString(3, km.getMoTa());
            
            // Ngày tạo thường là thời điểm hiện tại nếu chưa có
            LocalDateTime ngayTao = km.getNgayTao() != null ? km.getNgayTao() : LocalDateTime.now();
            pst.setTimestamp(4, Timestamp.valueOf(ngayTao));
            
            pst.setTimestamp(5, Timestamp.valueOf(km.getNgayBatDau()));
            pst.setTimestamp(6, Timestamp.valueOf(km.getNgayKetThuc()));

            n = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    // Lấy toàn bộ danh sách khuyến mãi (Bao gồm cả cũ và mới)
    public List<KhuyenMai> layDsKhuyenMai() {
        List<KhuyenMai> dsKhuyenMai = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMai";
        Connection con = ConnectDB.getInstance().getConnection();

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                KhuyenMai km = new KhuyenMai();
                km.setId(rs.getString("id"));
                km.setTenKhuyenMai(rs.getString("tenKhuyenMai"));
                km.setMoTa(rs.getString("moTa"));
                
                if (rs.getTimestamp("ngayTao") != null) km.setNgayTao(rs.getTimestamp("ngayTao").toLocalDateTime());
                if (rs.getTimestamp("ngayBatDau") != null) km.setNgayBatDau(rs.getTimestamp("ngayBatDau").toLocalDateTime());
                if (rs.getTimestamp("ngayKetThuc") != null) km.setNgayKetThuc(rs.getTimestamp("ngayKetThuc").toLocalDateTime());

                dsKhuyenMai.add(km);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsKhuyenMai;
    }

    // Chỉ lấy các khuyến mãi ĐANG CÓ HIỆU LỰC (ngayBatDau <= Hiện tại <= ngayKetThuc)
    public List<KhuyenMai> layKMHieuLuc() {
        List<KhuyenMai> dsHieuLuc = new ArrayList<>();
        // Truy vấn trực tiếp bằng SQL để tối ưu hiệu suất
        String sql = "SELECT * FROM KhuyenMai WHERE ngayBatDau <= GETDATE() AND ngayKetThuc >= GETDATE()";
        Connection con = ConnectDB.getInstance().getConnection();

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                KhuyenMai km = new KhuyenMai();
                km.setId(rs.getString("id"));
                km.setTenKhuyenMai(rs.getString("tenKhuyenMai"));
                km.setMoTa(rs.getString("moTa"));
                km.setNgayTao(rs.getTimestamp("ngayTao").toLocalDateTime());
                km.setNgayBatDau(rs.getTimestamp("ngayBatDau").toLocalDateTime());
                km.setNgayKetThuc(rs.getTimestamp("ngayKetThuc").toLocalDateTime());

                dsHieuLuc.add(km);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsHieuLuc;
    }

    // Tìm kiếm một chương trình khuyến mãi cụ thể theo Mã (ID)
    public KhuyenMai layMaKM(String maKM) {
        KhuyenMai km = null;
        String sql = "SELECT * FROM KhuyenMai WHERE id = ?";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maKM);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    km = new KhuyenMai();
                    km.setId(rs.getString("id"));
                    km.setTenKhuyenMai(rs.getString("tenKhuyenMai"));
                    km.setMoTa(rs.getString("moTa"));
                    if (rs.getTimestamp("ngayTao") != null) km.setNgayTao(rs.getTimestamp("ngayTao").toLocalDateTime());
                    if (rs.getTimestamp("ngayBatDau") != null) km.setNgayBatDau(rs.getTimestamp("ngayBatDau").toLocalDateTime());
                    if (rs.getTimestamp("ngayKetThuc") != null) km.setNgayKetThuc(rs.getTimestamp("ngayKetThuc").toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return km;
    }
}