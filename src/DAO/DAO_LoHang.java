package DAO;

import ConnectDB.ConnectDB;
import Entity.KhoHang;
import Entity.LoHang;
import Entity.SanPham;
import Enumeration.TrangThaiLoHang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DAO_LoHang {

    public DAO_LoHang() {
    }

    public List<LoHang> layDSLoHang() {
        List<LoHang> dsLoHang = new ArrayList<>();
        String sql = "SELECT * FROM LoHang";
        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            throw new RuntimeException("Không kết nối được database. Connection đang null.");
        }

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                dsLoHang.add(mapLoHang(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsLoHang;
    }

    public List<LoHang> layLoTheoSP(String maSP) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            return layLoTheoSP(con, maSP);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<LoHang> layLoTheoSP(Connection con, String maSP) throws SQLException {
        List<LoHang> dsLoHang = new ArrayList<>();

        String sql = "SELECT * FROM LoHang " +
                     "WHERE sanPhamId = ? " +
                     "AND soLuongLoHang > 0 " +
                     "AND trangThai <> ? " +
                     "ORDER BY ngayHetHan ASC";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maSP);
            pst.setString(2, TrangThaiLoHang.HET_HAN.name());

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    dsLoHang.add(mapLoHang(rs));
                }
            }
        }

        return dsLoHang;
    }

    public boolean capNhatSoLuongTon(String maLoHang, int soLuongMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            return capNhatSoLuongTon(con, maLoHang, soLuongMoi);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatSoLuongTon(Connection con, String maLoHang, int soLuongMoi) throws SQLException {
        String sql = "UPDATE LoHang SET soLuongLoHang = ? WHERE id = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, soLuongMoi);
            pst.setString(2, maLoHang);
            return pst.executeUpdate() > 0;
        }
    }

    public boolean capNhatTrangThaiLo(String maLoHang, TrangThaiLoHang trangThaiMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            return capNhatTrangThaiLo(con, maLoHang, trangThaiMoi);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatTrangThaiLo(Connection con, String maLoHang, TrangThaiLoHang trangThaiMoi) throws SQLException {
        String sql = "UPDATE LoHang SET trangThai = ? WHERE id = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, trangThaiMoi.name());
            pst.setString(2, maLoHang);
            return pst.executeUpdate() > 0;
        }
    }

    public boolean themLoHang(LoHang lo) {
        String sql = "INSERT INTO LoHang(id, soLoHang, soLuongLoHang, gia, ngayNhap, ngayHetHan, trangThai, sanPhamId, khoHangId) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, lo.getId());
            pst.setString(2, lo.getSoLoHang());
            pst.setInt(3, lo.getSoLuongLoHang());
            pst.setInt(4, lo.getGia());
            pst.setTimestamp(5, java.sql.Timestamp.valueOf(lo.getNgayNhap()));
            pst.setTimestamp(6, java.sql.Timestamp.valueOf(lo.getNgayHetHan()));
            pst.setString(7, lo.getTrangThai().name());
            pst.setString(8, lo.getSanPhamId() == null ? null : lo.getSanPhamId().getId());
            pst.setString(9, lo.getKhoHangId() == null ? null : lo.getKhoHangId().getId());

            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private LoHang mapLoHang(ResultSet rs) throws SQLException {
        LoHang lh = new LoHang();

        lh.setId(rs.getString("id"));
        lh.setSoLoHang(rs.getString("soLoHang"));
        lh.setSoLuongLoHang(rs.getInt("soLuongLoHang"));
        lh.setGia(rs.getInt("gia"));

        if (rs.getString("trangThai") != null) {
            lh.setTrangThai(TrangThaiLoHang.valueOf(rs.getString("trangThai")));
        }

        if (rs.getTimestamp("ngayHetHan") != null) {
            lh.setNgayHetHan(rs.getTimestamp("ngayHetHan").toLocalDateTime());
        }

        if (rs.getTimestamp("ngayNhap") != null) {
            lh.setNgayNhap(rs.getTimestamp("ngayNhap").toLocalDateTime());
        }

        SanPham sp = new SanPham();
        sp.setId(rs.getString("sanPhamId"));
        lh.setSanPhamId(sp);

        KhoHang kho = new KhoHang();
        kho.setId(rs.getString("khoHangId"));
        lh.setKhoHangId(kho);

        return lh;
    }
}