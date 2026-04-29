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
        return layDSLoHang(false);
    }

    public List<LoHang> layDSLoHang(boolean hienLoAn) {
        List<LoHang> dsLoHang = new ArrayList<>();

        String sql;
        if (hienLoAn) {
            sql = "SELECT lh.*, sp.ten AS tenSanPham " +
                  "FROM LoHang lh " +
                  "LEFT JOIN SanPham sp ON lh.sanPhamId = sp.id " +
                  "ORDER BY CASE WHEN lh.trangThai = 'AN' THEN 1 ELSE 0 END, lh.ngayHetHan ASC, lh.ngayNhap ASC";
        } else {
            sql = "SELECT lh.*, sp.ten AS tenSanPham " +
                  "FROM LoHang lh " +
                  "LEFT JOIN SanPham sp ON lh.sanPhamId = sp.id " +
                  "WHERE ISNULL(lh.trangThai, 'CON_HANG') <> 'AN' " +
                  "ORDER BY lh.ngayHetHan ASC, lh.ngayNhap ASC";
        }

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

    public List<LoHang> layDSLoHangDaAn() {
        List<LoHang> dsLoHang = new ArrayList<>();

        String sql = "SELECT lh.*, sp.ten AS tenSanPham " +
                     "FROM LoHang lh " +
                     "LEFT JOIN SanPham sp ON lh.sanPhamId = sp.id " +
                     "WHERE lh.trangThai = 'AN' " +
                     "ORDER BY lh.ngayNhap DESC";

        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                dsLoHang.add(mapLoHang(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsLoHang;
    }

    public LoHang getLoHangTheoId(String id) {
        String sql = "SELECT lh.*, sp.ten AS tenSanPham " +
                     "FROM LoHang lh " +
                     "LEFT JOIN SanPham sp ON lh.sanPhamId = sp.id " +
                     "WHERE lh.id = ?";

        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapLoHang(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public LoHang getLoHangTheoSoLo(String soLoHang) {
        String sql = "SELECT lh.*, sp.ten AS tenSanPham " +
                     "FROM LoHang lh " +
                     "LEFT JOIN SanPham sp ON lh.sanPhamId = sp.id " +
                     "WHERE UPPER(LTRIM(RTRIM(lh.soLoHang))) = UPPER(LTRIM(RTRIM(?)))";

        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, soLoHang);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapLoHang(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
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

        String sql = "SELECT lh.*, sp.ten AS tenSanPham " +
                     "FROM LoHang lh " +
                     "LEFT JOIN SanPham sp ON lh.sanPhamId = sp.id " +
                     "WHERE lh.sanPhamId = ? " +
                     "  AND lh.soLuongLoHang > 0 " +
                     "  AND ISNULL(lh.trangThai, 'CON_HANG') NOT IN (?, ?) " +
                     "ORDER BY lh.ngayHetHan ASC";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maSP);
            pst.setString(2, TrangThaiLoHang.HET_HAN.name());
            pst.setString(3, TrangThaiLoHang.AN.name());

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    dsLoHang.add(mapLoHang(rs));
                }
            }
        }

        return dsLoHang;
    }

    public boolean themLoHang(LoHang lo) {
        String sql = "INSERT INTO LoHang(id, soLoHang, soLuongLoHang, gia, ngayNhap, ngayHetHan, trangThai, sanPhamId, khoHangId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection  con = ConnectDB.getInstance().getConnection();
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

    public boolean khoiPhucVaCapNhatLoHang(LoHang lo) {
        String sql = "UPDATE LoHang " +
                     "SET sanPhamId = ?, " +
                     "    soLuongLoHang = ?, " +
                     "    gia = ?, " +
                     "    ngayNhap = ?, " +
                     "    ngayHetHan = ?, " +
                     "    khoHangId = ?, " +
                     "    trangThai = CASE " +
                     "        WHEN ? < GETDATE() THEN 'HET_HAN' " +
                     "        WHEN ? <= 0 THEN 'HET_HANG' " +
                     "        ELSE 'CON_HANG' " +
                     "    END " +
                     "WHERE id = ? AND trangThai = 'AN'";

        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, lo.getSanPhamId() == null ? null : lo.getSanPhamId().getId());
            pst.setInt(2, lo.getSoLuongLoHang());
            pst.setInt(3, lo.getGia());
            pst.setTimestamp(4, java.sql.Timestamp.valueOf(lo.getNgayNhap()));
            pst.setTimestamp(5, java.sql.Timestamp.valueOf(lo.getNgayHetHan()));
            pst.setString(6, lo.getKhoHangId() == null ? null : lo.getKhoHangId().getId());
            pst.setTimestamp(7, java.sql.Timestamp.valueOf(lo.getNgayHetHan()));
            pst.setInt(8, lo.getSoLuongLoHang());
            pst.setString(9, lo.getId());

            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean anLoHang(String maLoHang) {
        String sql = "UPDATE LoHang " +
                     "SET trangThai = 'AN' " +
                     "WHERE id = ? AND ISNULL(trangThai, 'CON_HANG') <> 'AN'";

        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maLoHang);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean khoiPhucLoHang(String maLoHang) {
        String sql = "UPDATE LoHang " +
                     "SET trangThai = CASE " +
                     "    WHEN ngayHetHan < GETDATE() THEN 'HET_HAN' " +
                     "    WHEN soLuongLoHang <= 0 THEN 'HET_HANG' " +
                     "    ELSE 'CON_HANG' " +
                     "END " +
                     "WHERE id = ? AND trangThai = 'AN'";

        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maLoHang);
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
        String sql = "UPDATE LoHang " +
                     "SET trangThai = ? " +
                     "WHERE id = ? AND ISNULL(trangThai, 'CON_HANG') <> 'AN'";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, trangThaiMoi.name());
            pst.setString(2, maLoHang);
            return pst.executeUpdate() > 0;
        }
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
        if (soLuongMoi < 0) return false;

        String sql = "UPDATE LoHang " +
                     "SET soLuongLoHang = ? " +
                     "WHERE id = ? AND ISNULL(trangThai, 'CON_HANG') <> 'AN'";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, soLuongMoi);
            pst.setString(2, maLoHang);
            return pst.executeUpdate() > 0;
        }
    }

    public boolean capNhatSoLuongVaTrangThaiLo(String maLoHang, int soLuongMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            return capNhatSoLuongVaTrangThaiLo(con, maLoHang, soLuongMoi);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean capNhatSoLuongVaTrangThaiLo(Connection con, String maLoHang, int soLuongMoi) throws SQLException {
        if (soLuongMoi < 0) return false;

        String sql =
                "UPDATE LoHang " +
                "SET soLuongLoHang = ?, " +
                "    trangThai = CASE " +
                "        WHEN ISNULL(trangThai, 'CON_HANG') = 'AN' THEN 'AN' " +
                "        WHEN ngayHetHan < GETDATE() THEN 'HET_HAN' " +
                "        WHEN ? <= 0 THEN 'HET_HANG' " +
                "        ELSE 'CON_HANG' " +
                "    END " +
                "WHERE id = ? AND ISNULL(trangThai, 'CON_HANG') <> 'AN'";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, soLuongMoi);
            pst.setInt(2, soLuongMoi);
            pst.setString(3, maLoHang);
            return pst.executeUpdate() > 0;
        }
    }

    public boolean dongBoTrangThaiLoHang() {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;

        try {
            con.setAutoCommit(false);

            try (PreparedStatement pst1 = con.prepareStatement(
                    "UPDATE LoHang " +
                    "SET trangThai = 'HET_HAN' " +
                    "WHERE ISNULL(trangThai, 'CON_HANG') <> 'AN' " +
                    "  AND ngayHetHan < GETDATE()"
            )) {
                pst1.executeUpdate();
            }

            try (PreparedStatement pst2 = con.prepareStatement(
                    "UPDATE LoHang " +
                    "SET trangThai = 'HET_HANG' " +
                    "WHERE ISNULL(trangThai, 'CON_HANG') <> 'AN' " +
                    "  AND ngayHetHan >= GETDATE() " +
                    "  AND soLuongLoHang <= 0"
            )) {
                pst2.executeUpdate();
            }

            try (PreparedStatement pst3 = con.prepareStatement(
                    "UPDATE LoHang " +
                    "SET trangThai = 'CON_HANG' " +
                    "WHERE ISNULL(trangThai, 'CON_HANG') <> 'AN' " +
                    "  AND ngayHetHan >= GETDATE() " +
                    "  AND soLuongLoHang > 0"
            )) {
                pst3.executeUpdate();
            }

            con.commit();
            con.setAutoCommit(true);
            return true;
        } catch (Exception e) {
            try {
                con.rollback();
                con.setAutoCommit(true);
            } catch (Exception ignored) {
            }
            e.printStackTrace();
            return false;
        }
    }

    private LoHang mapLoHang(ResultSet rs) throws SQLException {
        LoHang lh = new LoHang();

        lh.setId(rs.getString("id"));
        lh.setSoLoHang(rs.getString("soLoHang"));
        lh.setSoLuongLoHang(rs.getInt("soLuongLoHang"));
        lh.setGia((int) Math.round(rs.getDouble("gia")));

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
        try {
            sp.setTen(rs.getString("tenSanPham"));
        } catch (Exception ignored) {
        }
        lh.setSanPhamId(sp);

        KhoHang kho = new KhoHang();
        kho.setId(rs.getString("khoHangId"));
        lh.setKhoHangId(kho);

        return lh;
    }
 // DAO_LoHang.java — thêm method này
    public int xuatKhoFEFO(String maSP, int soLuongCanXuat) {
        Connection con = null;
        int soLuongBanDau = soLuongCanXuat;

        try {
            con = ConnectDB.getInstance().getConnection();
            con.setAutoCommit(false);

            List<LoHang> dsLo = layLoTheoSP(con, maSP);   // gọi overload nội bộ
            int soLuongConThieu = soLuongCanXuat;

            for (LoHang lh : dsLo) {
                if (soLuongConThieu == 0) break;
                if (lh == null || lh.getSoLuongLoHang() <= 0) continue;
                if (lh.getTrangThai() == TrangThaiLoHang.AN) continue;

                int soLuongXuat = Math.min(lh.getSoLuongLoHang(), soLuongConThieu);
                int soLuongMoi = lh.getSoLuongLoHang() - soLuongXuat;

                boolean ok = capNhatSoLuongVaTrangThaiLo(con, lh.getId(), soLuongMoi);  // overload nội bộ
                if (!ok) throw new SQLException("Không cập nhật được lô: " + lh.getId());

                soLuongConThieu -= soLuongXuat;
            }

            if (soLuongConThieu > 0)
                throw new SQLException("Kho không đủ hàng. Còn thiếu " + soLuongConThieu + " đơn vị.");

            con.commit();
            return 0;

        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return soLuongBanDau;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}