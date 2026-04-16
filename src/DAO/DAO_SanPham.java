package DAO;

import ConnectDB.ConnectDB;
import Entity.LoHang;
import Entity.SanPham;
import Enumeration.DangBaoChe;
import Enumeration.DanhMucSanPham;
import Enumeration.TrangThaiLoHang;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_SanPham {

    public DAO_SanPham() {
    }

    private Connection getConnection() {
        return ConnectDB.getInstance().getConnection();
    }

    public List<Object[]> layDanhSachSanPhamChoBang() {
        List<Object[]> ds = new ArrayList<>();

        String sql = "SELECT id, ten, danhMuc, ISNULL(hoatChat, '') AS hoatChat, dang, " +
                "ISNULL(nhaSanXuat, 'Khác') AS nhaSanXuat, ISNULL(thueVAT, 0) AS thueVAT " +
                "FROM SanPham";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String ma = rs.getString("id");
                String ten = rs.getString("ten");

                String danhMucDB = rs.getString("danhMuc");
                String loai = "Sản phẩm chức năng";
                if ("THUOC_KE_DON".equals(danhMucDB)) loai = "Thuốc kê đơn";
                else if ("THUOC_KHONG_KE_DON".equals(danhMucDB)) loai = "Thuốc không kê đơn";

                String hoatChat = rs.getString("hoatChat");

                String dangDB = rs.getString("dang");
                String dang = "Viên nén";
                if ("DANG_LONG".equals(dangDB)) dang = "Dung dịch";

                String nsx = rs.getString("nhaSanXuat");
                String vat = rs.getDouble("thueVAT") + "%";

                ds.add(new Object[]{ma, ten, loai, hoatChat, dang, nsx, vat});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }

    public List<SanPham> getDsThuoc() {
        List<SanPham> dsSanPham = new ArrayList<>();
        String sql = "SELECT * FROM SanPham";

        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                SanPham sp = mapSanPham(rs);
                dsSanPham.add(sp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsSanPham;
    }

    public SanPham getSanPhamTheoMa(String id) {
        String sql = "SELECT * FROM SanPham WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapSanPham(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<LoHang> layLoTheoSP(String maSP) {
        List<LoHang> dsLoHang = new ArrayList<>();

        String sql = "SELECT * FROM LoHang " +
                "WHERE sanPhamId = ? AND soLuongLoHang > 0 AND trangThai != 'HET_HAN' " +
                "ORDER BY ngayHetHan ASC";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, maSP);

            try (ResultSet rs = pst.executeQuery()) {
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

                    dsLoHang.add(lh);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsLoHang;
    }

    public List<Object[]> timKiemSanPhamBan(String keyword) {
        List<Object[]> list = new ArrayList<>();

        String sql = "SELECT " +
                "sp.id, " +
                "sp.ten, " +
                "dv.ten AS donVi, " +
                "dv.gia AS giaBan, " +
                "ISNULL(SUM(lh.soLuongLoHang), 0) AS tonKho " +
                "FROM SanPham sp " +
                "JOIN DonViDoLuong dv ON sp.id = dv.sanPhamId " +
                "LEFT JOIN LoHang lh ON sp.id = lh.sanPhamId " +
                "    AND lh.trangThai = 'CON_HANG' " +
                "    AND lh.ngayHetHan > GETDATE() " +
                "WHERE sp.ten LIKE ? OR sp.tenVietTat LIKE ? OR sp.hoatChat LIKE ? OR sp.id LIKE ? " +
                "GROUP BY sp.id, sp.ten, dv.ten, dv.gia";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pst.setString(1, searchPattern);
            pst.setString(2, searchPattern);
            pst.setString(3, searchPattern);
            pst.setString(4, searchPattern);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[5];
                    row[0] = rs.getString("id");
                    row[1] = rs.getString("ten");
                    row[2] = rs.getString("donVi");
                    row[3] = rs.getDouble("giaBan");
                    row[4] = rs.getInt("tonKho");
                    list.add(row);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi SQL tại timKiemSanPhamBan: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }

    public boolean capNhatSoLuongTon(String maLoHang, int soLuongMoi) {
        String sql = "UPDATE LoHang SET soLuongLoHang = ? WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, soLuongMoi);
            pst.setString(2, maLoHang);

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // ============================================================
    // CRUD SẢN PHẨM
    // ============================================================

    public String layMaSanPhamMoiNhat() {
        String sql = "SELECT TOP 1 id FROM SanPham ORDER BY id DESC";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                String lastId = rs.getString("id");

                if (lastId != null && lastId.contains("-")) {
                    String[] parts = lastId.split("-");
                    int number = Integer.parseInt(parts[1]);
                    return String.format("SP2024-%04d", number + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "SP2024-0001";
    }

    public boolean themSanPhamNhanh(String id, String danhMuc, String dang, String ten,
                                    String vietTat, String nsx, String hoatChat,
                                    double vat, String hamLuong, String moTa, String dvt) {

        String sql = "INSERT INTO SanPham " +
                "(id, danhMuc, dang, ten, tenVietTat, nhaSanXuat, hoatChat, thueVAT, hamLuong, moTa, donViDoCoBan, ngayTao) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, id);
            pst.setString(2, danhMuc);
            pst.setString(3, dang);
            pst.setString(4, ten);
            pst.setString(5, vietTat);
            pst.setString(6, nsx);
            pst.setString(7, hoatChat);
            pst.setDouble(8, vat);
            pst.setString(9, hamLuong);
            pst.setString(10, moTa);
            pst.setString(11, dvt);

            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean capNhatSanPhamNhanh(String id, String danhMuc, String dang, String ten,
                                       String vietTat, String nsx, String hoatChat,
                                       double vat, String hamLuong, String moTa, String dvt) {

        String sql = "UPDATE SanPham SET danhMuc=?, dang=?, ten=?, tenVietTat=?, nhaSanXuat=?, " +
                "hoatChat=?, thueVAT=?, hamLuong=?, moTa=?, donViDoCoBan=? WHERE id=?";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, danhMuc);
            pst.setString(2, dang);
            pst.setString(3, ten);
            pst.setString(4, vietTat);
            pst.setString(5, nsx);
            pst.setString(6, hoatChat);
            pst.setDouble(7, vat);
            pst.setString(8, hamLuong);
            pst.setString(9, moTa);
            pst.setString(10, dvt);
            pst.setString(11, id);

            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean xoaSanPham(String id) {
        String sql = "DELETE FROM SanPham WHERE id=?";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, id);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ============================================================
    // HELPER
    // ============================================================

    private SanPham mapSanPham(ResultSet rs) throws SQLException {
        SanPham sp = new SanPham();

        sp.setId(rs.getString("id"));

        if (rs.getString("danhMuc") != null) {
            sp.setDanhMuc(DanhMucSanPham.valueOf(rs.getString("danhMuc")));
        }

        if (rs.getString("dang") != null) {
            sp.setDang(DangBaoChe.valueOf(rs.getString("dang")));
        }

        sp.setTen(rs.getString("ten"));
        sp.setTenVietTat(rs.getString("tenVietTat"));
        sp.setNhaSanXuat(rs.getString("nhaSanXuat"));
        sp.setHoatChat(rs.getString("hoatChat"));
        sp.setThueVAT(rs.getDouble("thueVAT"));
        sp.setHamLuong(rs.getString("hamLuong"));
        sp.setMoTa(rs.getString("moTa"));
        sp.setDonViDoCoBan(rs.getString("donViDoCoBan"));

        if (rs.getTimestamp("ngayTao") != null) {
            sp.setNgayTao(rs.getTimestamp("ngayTao").toLocalDateTime());
        }

        return sp;
    }
}