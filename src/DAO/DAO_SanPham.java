package DAO;

import ConnectDB.ConnectDB;
import Entity.LoHang;
import Entity.SanPham;
import Enumeration.DangBaoChe;
import Enumeration.DanhMucSanPham;
import Enumeration.TrangThaiLoHang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;  
import java.util.ArrayList;
import java.util.List;

public class DAO_SanPham {

    public DAO_SanPham() {
    }

    public List<Object[]> layDanhSachSanPhamChoBang() {
        List<Object[]> ds = new ArrayList<>();
        String sql = "SELECT id, ten, danhMuc, ISNULL(hoatChat, '') AS hoatChat, dang, " +
                "ISNULL(nhaSanXuat, 'Khác') AS nhaSanXuat, ISNULL(thueVAT, 0) AS thueVAT " +
                "FROM SanPham " +
                "WHERE ISNULL(trangThai, 'HOAT_DONG') <> 'AN'";

        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql);
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
        String sql = "SELECT * FROM SanPham WHERE ISNULL(trangThai, 'HOAT_DONG') <> 'AN'";
        Connection con = ConnectDB.getInstance().getConnection();

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                SanPham sp = new SanPham();
                sp.setId(rs.getString("id"));

                if (rs.getString("danhMuc") != null) sp.setDanhMuc(DanhMucSanPham.valueOf(rs.getString("danhMuc")));
                if (rs.getString("dang") != null) sp.setDang(DangBaoChe.valueOf(rs.getString("dang")));

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

                dsSanPham.add(sp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsSanPham;
    }

    public SanPham getSanPhamTheoMa(String id) {
        SanPham sp = null;
        String sql = "SELECT * FROM SanPham WHERE id = ?";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    sp = new SanPham();
                    sp.setId(rs.getString("id"));

                    if (rs.getString("danhMuc") != null) sp.setDanhMuc(DanhMucSanPham.valueOf(rs.getString("danhMuc")));
                    if (rs.getString("dang") != null) sp.setDang(DangBaoChe.valueOf(rs.getString("dang")));

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
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sp;
    }

    public List<LoHang> layLoTheoSP(String maSP) {
        List<LoHang> dsLoHang = new ArrayList<>();
        String sql = "SELECT * FROM LoHang " +
                     "WHERE sanPhamId = ? " +
                     "AND soLuongLoHang > 0 " +
                     "AND ISNULL(trangThai, 'CON_HANG') NOT IN ('HET_HAN', 'AN') " +
                     "ORDER BY ngayHetHan ASC, ngayNhap ASC";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
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

    public List<Object[]> timKiemSanPhamBan(String text) {
        List<Object[]> ds = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();

        try {
            String sql = "SELECT sp.id AS id, sp.ten AS ten, sp.donViDoCoBan AS donVi, " +
                         "       ISNULL(MAX(lh.gia), 0) AS giaBan, " +
                         "       ISNULL(SUM(CASE " +
                         "           WHEN lh.soLuongLoHang > 0 " +
                         "            AND lh.ngayHetHan >= GETDATE() " +
                         "            AND ISNULL(lh.trangThai, 'CON_HANG') NOT IN ('AN', 'HET_HAN') " +
                         "           THEN lh.soLuongLoHang ELSE 0 END), 0) AS soLuongTon, " +
                         "       sp.danhMuc AS danhMuc " +
                         "FROM SanPham sp " +
                         "LEFT JOIN LoHang lh ON sp.id = lh.sanPhamId " +
                         "WHERE ISNULL(sp.trangThai, 'HOAT_DONG') <> 'AN' " +
                         "  AND (sp.ten LIKE ? OR sp.id LIKE ?) " +
                         "GROUP BY sp.id, sp.ten, sp.donViDoCoBan, sp.danhMuc " +
                         "HAVING ISNULL(SUM(CASE " +
                         "           WHEN lh.soLuongLoHang > 0 " +
                         "            AND lh.ngayHetHan >= GETDATE() " +
                         "            AND ISNULL(lh.trangThai, 'CON_HANG') NOT IN ('AN', 'HET_HAN') " +
                         "           THEN lh.soLuongLoHang ELSE 0 END), 0) > 0";

            try (PreparedStatement pst = con.prepareStatement(sql)) {
                String searchPattern = "%" + text + "%";
                pst.setString(1, searchPattern);
                pst.setString(2, searchPattern);

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        String danhMucDB = rs.getString("danhMuc");
                        String loai = "Khác";
                        if (danhMucDB != null) {
                            if (danhMucDB.equals("THUOC_KE_DON")) loai = "Thuốc kê đơn";
                            else if (danhMucDB.equals("THUOC_KHONG_KE_DON")) loai = "Thuốc không kê đơn";
                            else if (danhMucDB.equals("THUC_PHAM_CHUC_NANG")) loai = "Thực phẩm chức năng";
                            else if (danhMucDB.equals("MY_PHAM")) loai = "Mỹ phẩm";
                            else if (danhMucDB.equals("VAT_TU_Y_TE")) loai = "Vật tư y tế";
                        }

                        ds.add(new Object[]{
                                rs.getString("id"),
                                rs.getString("ten"),
                                rs.getString("donVi"),
                                rs.getDouble("giaBan"),
                                rs.getInt("soLuongTon"),
                                loai
                        });
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi tại DAO_SanPham.timKiemSanPhamBan: " + e.getMessage());
            e.printStackTrace();
        }
        return ds;
    }

    public boolean capNhatSoLuongTon(String maLoHang, int soLuongMoi) {
        if (soLuongMoi < 0) return false;

        String sql = "UPDATE LoHang " +
                     "SET soLuongLoHang = ? " +
                     "WHERE id = ? AND ISNULL(trangThai, 'CON_HANG') <> 'AN'";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, soLuongMoi);
            pst.setString(2, maLoHang);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String layMaSanPhamMoiNhat() {
        String sql = "SELECT TOP 1 id FROM SanPham ORDER BY id DESC";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                String lastId = rs.getString("id");
                int number = Integer.parseInt(lastId.split("-")[1]);
                return String.format("SP2024-%04d", number + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "SP2024-0001";
    }

    public boolean anSanPham(String id) {
        String sql = "UPDATE SanPham SET trangThai = 'AN' WHERE id = ?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, id);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean themSanPhamNhanh(String id, String danhMuc, String dang, String ten, String vietTat, String nsx, String hoatChat, double vat, String hamLuong, String moTa, String dvt) {
        String sql = "INSERT INTO SanPham (id, danhMuc, dang, ten, tenVietTat, nhaSanXuat, hoatChat, thueVAT, hamLuong, moTa, donViDoCoBan, ngayTao, trangThai) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), 'HOAT_DONG')";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
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

    public boolean capNhatSanPhamNhanh(String id, String danhMuc, String dang, String ten, String vietTat, String nsx, String hoatChat, double vat, String hamLuong, String moTa, String dvt) {
        String sql = "UPDATE SanPham SET danhMuc=?, dang=?, ten=?, tenVietTat=?, nhaSanXuat=?, hoatChat=?, thueVAT=?, hamLuong=?, moTa=?, donViDoCoBan=? WHERE id=?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
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

    public List<SanPham> timKiemSanPhamDoiTra(String tuKhoa) {
        List<SanPham> ds = new ArrayList<>();
        String sql = "SELECT * FROM SanPham WHERE ten LIKE ? OR id LIKE ?";

        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, "%" + tuKhoa + "%");
            pst.setString(2, "%" + tuKhoa + "%");

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    SanPham sp = new SanPham();
                    sp.setId(rs.getString("id"));
                    sp.setTen(rs.getString("ten"));

                    String danhMucStr = rs.getString("danhMuc");
                    if (danhMucStr != null && !danhMucStr.isEmpty()) {
                        sp.setDanhMuc(Enumeration.DanhMucSanPham.valueOf(danhMucStr));
                    }

                    ds.add(sp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean xoaSanPham(String id) {
        String sql = "DELETE FROM SanPham WHERE id=?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, id);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getSoLuongTon(String maSP) {
        String sql = "SELECT ISNULL(SUM(lh.soLuongLoHang), 0) AS tonKho " +
                     "FROM LoHang lh " +
                     "WHERE lh.sanPhamId = ? " +
                     "AND lh.soLuongLoHang > 0 " +
                     "AND lh.ngayHetHan >= GETDATE() " +
                     "AND ISNULL(lh.trangThai, 'CON_HANG') NOT IN ('AN', 'HET_HAN')";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maSP);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("tonKho");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Object[]> layDanhSachSanPhamDaAn() {
        List<Object[]> ds = new ArrayList<>();
        String sql = "SELECT id, ten, danhMuc, hoatChat, dang, nhaSanXuat, thueVAT " +
                     "FROM SanPham WHERE trangThai = 'AN'";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                ds.add(new Object[]{
                        rs.getString("id"),
                        rs.getString("ten"),
                        rs.getString("danhMuc"),
                        rs.getString("hoatChat"),
                        rs.getString("dang"),
                        rs.getString("nhaSanXuat"),
                        rs.getDouble("thueVAT") + "%"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean khoiPhucSanPham(String id) {
        String sql = "UPDATE SanPham SET trangThai = 'HOAT_DONG' WHERE id = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, id);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> layDanhSachTenSanPham() {
        List<String> dsTenSP = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return dsTenSP;

        String sql = "SELECT ten FROM SanPham";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                dsTenSP.add(rs.getString("ten"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsTenSP;
    }

    public boolean laSanPhamDaAn(String maSP) {
        String sql = "SELECT COUNT(*) FROM SanPham WHERE id = ? AND trangThai = 'AN'";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maSP);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}