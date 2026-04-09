package DAO;

import ConnectDB.ConnectDB;
import Entity.KhoHang;
import Entity.LoHang;
import Entity.SanPham;
import Enum.TrangThaiLoHang;

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

    // 1. Lấy toàn bộ danh sách lô hàng
    public List<LoHang> layDSLoHang() {
        List<LoHang> dsLoHang = new ArrayList<>();
        String sql = "SELECT * FROM LoHang";
        Connection con = ConnectDB.getInstance().getConnection();

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
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

                dsLoHang.add(lh);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsLoHang;
    }

    // 2. Lấy danh sách các lô hàng của một sản phẩm cụ thể (Phục vụ FEFO)
    // Hàm cũ
    public List<LoHang> layLoTheoSP(String maSP) {
        Connection con = ConnectDB.getInstance().getConnection();
        return layLoTheoSP(con, maSP);
    }

    // Hàm mới dùng cho transaction
    public List<LoHang> layLoTheoSP(Connection con, String maSP) {
        List<LoHang> dsLoHang = new ArrayList<>();
        String sql = "SELECT * FROM LoHang "
                   + "WHERE sanPhamId = ? AND soLuongLoHang > 0 AND trangThai <> ? "
                   + "ORDER BY ngayHetHan ASC";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maSP);
            pst.setString(2, TrangThaiLoHang.HET_HAN.name());

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    LoHang lh = new LoHang();
                    lh.setId(rs.getString("id"));
                    lh.setSoLoHang(rs.getString("soLoHang"));
                    lh.setSoLuongLoHang(rs.getInt("soLuongLoHang"));
                    lh.setGia(rs.getInt("gia"));

                    if (rs.getTimestamp("ngayHetHan") != null) {
                        lh.setNgayHetHan(rs.getTimestamp("ngayHetHan").toLocalDateTime());
                    }
                    if (rs.getTimestamp("ngayNhap") != null) {
                        lh.setNgayNhap(rs.getTimestamp("ngayNhap").toLocalDateTime());
                    }
                    if (rs.getString("trangThai") != null) {
                        lh.setTrangThai(TrangThaiLoHang.valueOf(rs.getString("trangThai")));
                    }

                    SanPham sp = new SanPham();
                    sp.setId(rs.getString("sanPhamId"));
                    lh.setSanPhamId(sp);

                    KhoHang kho = new KhoHang();
                    kho.setId(rs.getString("khoHangId"));
                    lh.setKhoHangId(kho);

                    dsLoHang.add(lh);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsLoHang;
    }

    // 3. Cập nhật số lượng tồn của một lô cụ thể
    // Hàm cũ
    public boolean capNhatSoLuongTon(String maLoHang, int soLuongMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        return capNhatSoLuongTon(con, maLoHang, soLuongMoi);
    }

    // Hàm mới dùng cho transaction
    public boolean capNhatSoLuongTon(Connection con, String maLoHang, int soLuongMoi) {
        String sql = "UPDATE LoHang SET soLuongLoHang = ? WHERE id = ?";
        int n = 0;

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, soLuongMoi);
            pst.setString(2, maLoHang);
            n = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    // 4. Cập nhật trạng thái lô hàng
    // Hàm cũ
    public boolean capNhatTrangThaiLo(String maLoHang, TrangThaiLoHang trangThaiMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        return capNhatTrangThaiLo(con, maLoHang, trangThaiMoi);
    }

    // Hàm mới dùng cho transaction
    public boolean capNhatTrangThaiLo(Connection con, String maLoHang, TrangThaiLoHang trangThaiMoi) {
        String sql = "UPDATE LoHang SET trangThai = ? WHERE id = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, trangThaiMoi.name());
            pst.setString(2, maLoHang);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}