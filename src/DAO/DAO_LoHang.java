package DAO;

import ConnectDB.ConnectDB;
import Entity.KhoHang;
import Entity.LoHang;
import Entity.SanPham;
import Enumeration.TrangThaiLoHang;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
                    "ORDER BY " +
                    "CASE WHEN ISNULL(lh.trangThai, 'CON_HANG') = 'AN' THEN 1 ELSE 0 END, " +
                    "lh.ngayHetHan ASC, lh.ngayNhap ASC";
        } else {
            sql = "SELECT lh.*, sp.ten AS tenSanPham " +
                    "FROM LoHang lh " +
                    "LEFT JOIN SanPham sp ON lh.sanPhamId = sp.id " +
                    "WHERE ISNULL(lh.trangThai, 'CON_HANG') <> 'AN' " +
                    "ORDER BY lh.ngayHetHan ASC, lh.ngayNhap ASC";
        }

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return dsLoHang;
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
                "WHERE ISNULL(lh.trangThai, 'CON_HANG') = 'AN' " +
                "ORDER BY lh.ngayNhap DESC, lh.ngayHetHan ASC";

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return dsLoHang;
        }

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

    public List<KhoHang> layDanhSachKhoHang() {
        List<KhoHang> dsKho = new ArrayList<>();

        String sql = "SELECT id FROM KhoHang ORDER BY id";

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return dsKho;
        }

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                KhoHang kho = new KhoHang();
                kho.setId(rs.getString("id"));
                dsKho.add(kho);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dsKho;
    }

    public boolean tonTaiKho(String maKho) {
        String sql = "SELECT COUNT(*) FROM KhoHang WHERE id = ?";

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return false;
        }

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maKho);

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

    public boolean tonTaiMaVachNoiBo(String maVachNoiBo) {
        String sql = "SELECT COUNT(*) FROM LoHang WHERE maVachNoiBo = ?";

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return false;
        }

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maVachNoiBo);

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

    public LoHang getLoHangTheoId(String id) {
        String sql = "SELECT lh.*, sp.ten AS tenSanPham " +
                "FROM LoHang lh " +
                "LEFT JOIN SanPham sp ON lh.sanPhamId = sp.id " +
                "WHERE lh.id = ?";

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return null;
        }

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

    public LoHang getLoHangTheoMaVachNoiBo(String maVachNoiBo) {
        String sql = "SELECT lh.*, sp.ten AS tenSanPham " +
                "FROM LoHang lh " +
                "LEFT JOIN SanPham sp ON lh.sanPhamId = sp.id " +
                "WHERE UPPER(LTRIM(RTRIM(lh.maVachNoiBo))) = UPPER(LTRIM(RTRIM(?))) " +
                "AND ISNULL(lh.trangThai, 'CON_HANG') <> 'AN' " +
                "AND lh.soLuongLoHang > 0";

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return null;
        }

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maVachNoiBo);

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

    public List<LoHang> timLoHangChoXuatKho(String keyword) {
        List<LoHang> ds = new ArrayList<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            return ds;
        }

        String sql = "SELECT lh.*, sp.ten AS tenSanPham " +
                "FROM LoHang lh " +
                "LEFT JOIN SanPham sp ON lh.sanPhamId = sp.id " +
                "WHERE ISNULL(lh.trangThai, 'CON_HANG') <> 'AN' " +
                "AND lh.soLuongLoHang > 0 " +
                "AND ( " +
                "    UPPER(LTRIM(RTRIM(lh.id))) = UPPER(LTRIM(RTRIM(?))) " +
                " OR UPPER(LTRIM(RTRIM(lh.soLoHang))) = UPPER(LTRIM(RTRIM(?))) " +
                " OR UPPER(LTRIM(RTRIM(lh.maVachNoiBo))) = UPPER(LTRIM(RTRIM(?))) " +
                " OR UPPER(LTRIM(RTRIM(lh.sanPhamId))) = UPPER(LTRIM(RTRIM(?))) " +
                ") " +
                "ORDER BY lh.ngayHetHan ASC, lh.ngayNhap ASC";

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return ds;
        }

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            String key = keyword.trim();

            pst.setString(1, key);
            pst.setString(2, key);
            pst.setString(3, key);
            pst.setString(4, key);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapLoHang(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ds;
    }

    public LoHang getLoHangTheoSoLo(String soLoHang) {
        String sql = "SELECT lh.*, sp.ten AS tenSanPham " +
                "FROM LoHang lh " +
                "LEFT JOIN SanPham sp ON lh.sanPhamId = sp.id " +
                "WHERE UPPER(LTRIM(RTRIM(lh.soLoHang))) = UPPER(LTRIM(RTRIM(?)))";

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return null;
        }

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
                "AND lh.soLuongLoHang > 0 " +
                "AND ISNULL(lh.trangThai, 'CON_HANG') NOT IN (?, ?) " +
                "ORDER BY lh.ngayHetHan ASC, lh.ngayNhap ASC";

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
        String sql = "INSERT INTO LoHang(id, soLoHang, soLuongLoHang, gia, ngayNhap, ngayHetHan, trangThai, sanPhamId, khoHangId, maVachNoiBo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return false;
        }

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, lo.getId());
            pst.setString(2, lo.getSoLoHang());
            pst.setInt(3, lo.getSoLuongLoHang());
            pst.setDouble(4, lo.getGia());
            pst.setTimestamp(5, java.sql.Timestamp.valueOf(lo.getNgayNhap()));
            pst.setTimestamp(6, java.sql.Timestamp.valueOf(lo.getNgayHetHan()));
            pst.setString(7, lo.getTrangThai().name());
            pst.setString(8, lo.getSanPhamId() == null ? null : lo.getSanPhamId().getId());
            pst.setString(9, lo.getKhoHangId() == null ? null : lo.getKhoHangId().getId());
            pst.setString(10, lo.getMaVachNoiBo());

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean khoiPhucVaCapNhatLoHang(LoHang lo) {
        String sql = "UPDATE LoHang SET " +
                "sanPhamId = ?, " +
                "soLuongLoHang = ?, " +
                "gia = ?, " +
                "ngayNhap = ?, " +
                "ngayHetHan = ?, " +
                "khoHangId = ?, " +
                "maVachNoiBo = ?, " +
                "trangThai = CASE WHEN ? < GETDATE() THEN 'HET_HAN' WHEN ? <= 0 THEN 'HET_HANG' ELSE 'CON_HANG' END " +
                "WHERE id = ? AND trangThai = 'AN'";

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return false;
        }

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, lo.getSanPhamId() == null ? null : lo.getSanPhamId().getId());
            pst.setInt(2, lo.getSoLuongLoHang());
            pst.setDouble(3, lo.getGia());
            pst.setTimestamp(4, java.sql.Timestamp.valueOf(lo.getNgayNhap()));
            pst.setTimestamp(5, java.sql.Timestamp.valueOf(lo.getNgayHetHan()));
            pst.setString(6, lo.getKhoHangId() == null ? null : lo.getKhoHangId().getId());
            pst.setString(7, lo.getMaVachNoiBo());
            pst.setTimestamp(8, java.sql.Timestamp.valueOf(lo.getNgayHetHan()));
            pst.setInt(9, lo.getSoLuongLoHang());
            pst.setString(10, lo.getId());

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean anLoHang(String maLoHang) {
        String sql = "UPDATE LoHang SET trangThai = 'AN' " +
                "WHERE id = ? AND ISNULL(trangThai, 'CON_HANG') <> 'AN'";

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return false;
        }

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maLoHang);
            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean khoiPhucLoHang(String maLoHang) {
        String sql = "UPDATE LoHang SET trangThai = CASE " +
                "WHEN ngayHetHan < GETDATE() THEN 'HET_HAN' " +
                "WHEN soLuongLoHang <= 0 THEN 'HET_HANG' " +
                "ELSE 'CON_HANG' END " +
                "WHERE id = ? AND trangThai = 'AN'";

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return false;
        }

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

    public boolean capNhatTrangThaiLo(Connection con, String maLoHang, TrangThaiLoHang trangThaiMoi)
            throws SQLException {
        String sql = "UPDATE LoHang SET trangThai = ? " +
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
        if (soLuongMoi < 0) {
            return false;
        }

        String sql = "UPDATE LoHang SET soLuongLoHang = ? " +
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

    public boolean capNhatSoLuongVaTrangThaiLo(Connection con, String maLoHang, int soLuongMoi)
            throws SQLException {
        if (soLuongMoi < 0) {
            return false;
        }

        String sql = "UPDATE LoHang SET soLuongLoHang = ?, " +
                "trangThai = CASE " +
                "WHEN ISNULL(trangThai, 'CON_HANG') = 'AN' THEN 'AN' " +
                "WHEN ngayHetHan < GETDATE() THEN 'HET_HAN' " +
                "WHEN ? <= 0 THEN 'HET_HANG' " +
                "ELSE 'CON_HANG' END " +
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

        if (con == null) {
            return false;
        }

        try {
            con.setAutoCommit(false);

            try (PreparedStatement pst1 = con.prepareStatement(
                    "UPDATE LoHang SET trangThai = 'HET_HAN' " +
                            "WHERE ISNULL(trangThai, 'CON_HANG') <> 'AN' AND ngayHetHan < GETDATE()")) {
                pst1.executeUpdate();
            }

            try (PreparedStatement pst2 = con.prepareStatement(
                    "UPDATE LoHang SET trangThai = 'HET_HANG' " +
                            "WHERE ISNULL(trangThai, 'CON_HANG') <> 'AN' AND ngayHetHan >= GETDATE() AND soLuongLoHang <= 0")) {
                pst2.executeUpdate();
            }

            try (PreparedStatement pst3 = con.prepareStatement(
                    "UPDATE LoHang SET trangThai = 'CON_HANG' " +
                            "WHERE ISNULL(trangThai, 'CON_HANG') <> 'AN' AND ngayHetHan >= GETDATE() AND soLuongLoHang > 0")) {
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
        lh.setGia(rs.getDouble("gia"));

        try {
            lh.setMaVachNoiBo(rs.getString("maVachNoiBo"));
        } catch (Exception ignored) {
        }

        String trangThai = rs.getString("trangThai");

        if (trangThai != null && !trangThai.trim().isEmpty()) {
            try {
                lh.setTrangThai(TrangThaiLoHang.valueOf(trangThai));
            } catch (Exception ignored) {
                lh.setTrangThai(TrangThaiLoHang.CON_HANG);
            }
        } else {
            lh.setTrangThai(TrangThaiLoHang.CON_HANG);
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

    public int xuatKhoFEFO(String maSP, int soLuongCanXuat) {
        Connection con = null;
        int soLuongBanDau = soLuongCanXuat;

        try {
            con = ConnectDB.getInstance().getConnection();
            con.setAutoCommit(false);

            List<LoHang> dsLo = layLoTheoSP(con, maSP);
            int soLuongConThieu = soLuongCanXuat;

            for (LoHang lh : dsLo) {
                if (soLuongConThieu == 0) {
                    break;
                }

                if (lh == null || lh.getSoLuongLoHang() <= 0) {
                    continue;
                }

                if (lh.getTrangThai() == TrangThaiLoHang.AN) {
                    continue;
                }

                int soLuongXuat = Math.min(lh.getSoLuongLoHang(), soLuongConThieu);
                int soLuongMoi = lh.getSoLuongLoHang() - soLuongXuat;

                boolean ok = capNhatSoLuongVaTrangThaiLo(con, lh.getId(), soLuongMoi);

                if (!ok) {
                    throw new SQLException("Không cập nhật được lô: " + lh.getId());
                }

                soLuongConThieu -= soLuongXuat;
            }

            if (soLuongConThieu > 0) {
                throw new SQLException("Kho không đủ hàng. Còn thiếu " + soLuongConThieu + " đơn vị.");
            }

            con.commit();
            return 0;

        } catch (Exception e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            e.printStackTrace();
            return soLuongBanDau;

        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean thucThiXuatHuyKhoBangTransaction(List<Object[]> danhSachXuat, String nguoiThucHien) {
        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return false;
        }

        try {
            con.setAutoCommit(false);

            for (Object[] item : danhSachXuat) {
                String loHangId = item[0].toString();
                int slXuat = Integer.parseInt(item[1].toString());
                String lyDo = item[2].toString();

                LoHang loHienTai = getLoHangTheoIdTrongTransaction(con, loHangId);

                if (loHienTai == null) {
                    con.rollback();
                    return false;
                }

                if (loHienTai.getSoLuongLoHang() < slXuat) {
                    con.rollback();
                    return false;
                }

                if (!truTonKhoInternal(loHangId, slXuat, con)) {
                    con.rollback();
                    return false;
                }

                if (!ghiNhanLichSuXuatHuyInternal(loHienTai, slXuat, lyDo, nguoiThucHien, con)) {
                    con.rollback();
                    return false;
                }
            }

            con.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();

            try {
                con.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return false;

        } finally {
            try {
                con.setAutoCommit(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean truTonKhoInternal(String loHangId, int soLuongXuat, Connection con) throws SQLException {
        String sql = "UPDATE LoHang SET " +
                "soLuongLoHang = soLuongLoHang - ?, " +
                "trangThai = CASE " +
                "    WHEN ngayHetHan < GETDATE() THEN 'HET_HAN' " +
                "    WHEN soLuongLoHang - ? <= 0 THEN 'HET_HANG' " +
                "    ELSE 'CON_HANG' " +
                "END " +
                "WHERE id = ? " +
                "AND ISNULL(trangThai, 'CON_HANG') <> 'AN' " +
                "AND soLuongLoHang >= ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, soLuongXuat);
            stmt.setInt(2, soLuongXuat);
            stmt.setString(3, loHangId);
            stmt.setInt(4, soLuongXuat);

            return stmt.executeUpdate() > 0;
        }
    }

    private boolean ghiNhanLichSuXuatHuyInternal(
            LoHang loHang,
            int soLuongXuat,
            String lyDo,
            String nguoiThucHien,
            Connection con
    ) throws SQLException {
        String sql = "INSERT INTO PhieuXuatKho " +
                "(SoLoHang, SoLuongXuat, LyDoXuat, NguoiThucHien, NgayXuat) " +
                "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, loHang.getSoLoHang());
            stmt.setInt(2, soLuongXuat);
            stmt.setString(3, lyDo);
            stmt.setString(4, nguoiThucHien);

            return stmt.executeUpdate() > 0;
        }
    }

    private LoHang getLoHangTheoIdTrongTransaction(Connection con, String id) throws SQLException {
        String sql = "SELECT lh.*, sp.ten AS tenSanPham " +
                "FROM LoHang lh " +
                "LEFT JOIN SanPham sp ON lh.sanPhamId = sp.id " +
                "WHERE lh.id = ? " +
                "AND ISNULL(lh.trangThai, 'CON_HANG') <> 'AN'";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapLoHang(rs);
                }
            }
        }

        return null;
    }

    public List<Object[]> layLichSuXuatKho() {
        List<Object[]> list = new ArrayList<>();

        String sql =
                "SELECT " +
                "    px.NgayXuat, " +
                "    px.SoLoHang, " +
                "    ISNULL(sp.ten, N'Sản phẩm không xác định') AS TenSanPham, " +
                "    px.SoLuongXuat, " +
                "    px.LyDoXuat, " +
                "    px.NguoiThucHien, " +
                "    lh.sanPhamId AS SanPhamId " +
                "FROM PhieuXuatKho px " +
                "OUTER APPLY ( " +
                "    SELECT TOP 1 lh2.sanPhamId " +
                "    FROM LoHang lh2 " +
                "    WHERE UPPER(LTRIM(RTRIM(px.SoLoHang))) = UPPER(LTRIM(RTRIM(lh2.soLoHang))) " +
                "    ORDER BY lh2.ngayNhap DESC " +
                ") lh " +
                "LEFT JOIN SanPham sp ON lh.sanPhamId = sp.id " +
                "ORDER BY px.NgayXuat DESC";

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return list;
        }

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String rawNguoiThucHien = rs.getString("NguoiThucHien");
                String tenNguoiThucHien = layTenNguoiThucHien(con, rawNguoiThucHien);

                String sanPhamId = rs.getString("SanPhamId");
                String tenSanPham = rs.getString("TenSanPham");
                String donViCoBan = layDonViCoBan(con, sanPhamId, tenSanPham);

                list.add(new Object[]{
                        rs.getTimestamp("NgayXuat"),
                        rs.getString("SoLoHang"),
                        tenSanPham != null ? tenSanPham : "Sản phẩm không xác định",
                        rs.getInt("SoLuongXuat"),
                        rs.getString("LyDoXuat"),
                        tenNguoiThucHien,
                        donViCoBan
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    private String layTenNguoiThucHien(Connection con, String rawNguoiThucHien) {
        String raw = safe(rawNguoiThucHien);

        if (raw.isEmpty()
                || raw.equalsIgnoreCase("NV-DEFAULT")
                || raw.equalsIgnoreCase("Người dùng hiện tại")) {
            return "Không xác định";
        }

        String tenTuNhanVien = timTenNhanVienTheoId(con, raw);

        if (!tenTuNhanVien.isEmpty()) {
            return tenTuNhanVien;
        }

        String nhanVienIdTuTaiKhoan = timNhanVienIdTuTaiKhoan(con, raw);

        if (!nhanVienIdTuTaiKhoan.isEmpty()) {
            tenTuNhanVien = timTenNhanVienTheoId(con, nhanVienIdTuTaiKhoan);

            if (!tenTuNhanVien.isEmpty()) {
                return tenTuNhanVien;
            }
        }

        return raw;
    }

    private String timTenNhanVienTheoId(Connection con, String nhanVienId) {
        if (con == null || nhanVienId == null || nhanVienId.trim().isEmpty()) {
            return "";
        }

        try {
            if (!tableExists(con, "NhanVien")) {
                return "";
            }

            String idCol = firstExistingColumn(con, "NhanVien",
                    "id", "maNhanVien", "nhanVienId", "maNV");

            String nameCol = firstExistingColumn(con, "NhanVien",
                    "hoTen", "hoVaTen", "tenNhanVien", "tenNV", "ten", "name");

            if (idCol.isEmpty() || nameCol.isEmpty()) {
                return "";
            }

            String sql = "SELECT TOP 1 " + quoteName(nameCol) + " AS TenNhanVien " +
                    "FROM NhanVien " +
                    "WHERE UPPER(LTRIM(RTRIM(" + quoteName(idCol) + "))) = UPPER(LTRIM(RTRIM(?)))";

            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, nhanVienId.trim());

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return safe(rs.getString("TenNhanVien"));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private String timNhanVienIdTuTaiKhoan(Connection con, String tenDangNhapOrId) {
        if (con == null || tenDangNhapOrId == null || tenDangNhapOrId.trim().isEmpty()) {
            return "";
        }

        try {
            if (!tableExists(con, "TaiKhoan")) {
                return "";
            }

            String nhanVienIdCol = firstExistingColumn(con, "TaiKhoan",
                    "nhanVienId", "maNhanVien", "maNV", "nhanVien");

            if (nhanVienIdCol.isEmpty()) {
                return "";
            }

            List<String> cotCoTheTim = new ArrayList<>();

            addColumnIfExists(con, "TaiKhoan", cotCoTheTim, "tenDangNhap");
            addColumnIfExists(con, "TaiKhoan", cotCoTheTim, "username");
            addColumnIfExists(con, "TaiKhoan", cotCoTheTim, "taiKhoan");
            addColumnIfExists(con, "TaiKhoan", cotCoTheTim, "id");

            if (cotCoTheTim.isEmpty()) {
                return "";
            }

            List<String> dieuKien = new ArrayList<>();

            for (String col : cotCoTheTim) {
                dieuKien.add("UPPER(LTRIM(RTRIM(" + quoteName(col) + "))) = UPPER(LTRIM(RTRIM(?)))");
            }

            String sql = "SELECT TOP 1 " + quoteName(nhanVienIdCol) + " AS NhanVienId " +
                    "FROM TaiKhoan " +
                    "WHERE " + String.join(" OR ", dieuKien);

            try (PreparedStatement pst = con.prepareStatement(sql)) {
                String key = tenDangNhapOrId.trim();

                for (int i = 1; i <= cotCoTheTim.size(); i++) {
                    pst.setString(i, key);
                }

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return safe(rs.getString("NhanVienId"));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private String layDonViCoBan(Connection con, String sanPhamId, String tenSanPham) {
        String fromSanPham = layDonViTuBangSanPham(con, sanPhamId);

        if (!fromSanPham.isEmpty()) {
            return fromSanPham;
        }

        String fromDonVi = layDonViTuBangDonViDoLuong(con, sanPhamId);

        if (!fromDonVi.isEmpty()) {
            return fromDonVi;
        }

        return doanDonViTheoTenSanPham(tenSanPham);
    }

    private String layDonViTuBangSanPham(Connection con, String sanPhamId) {
        if (con == null || sanPhamId == null || sanPhamId.trim().isEmpty()) {
            return "";
        }

        try {
            if (!tableExists(con, "SanPham")) {
                return "";
            }

            String idCol = firstExistingColumn(con, "SanPham", "id", "sanPhamId", "maSanPham", "maSP");
            String unitCol = firstExistingColumn(con, "SanPham",
                    "donViDoCoBan", "donViCoBan", "donViTinh", "donVi", "dvt");

            if (idCol.isEmpty() || unitCol.isEmpty()) {
                return "";
            }

            String sql = "SELECT TOP 1 " + quoteName(unitCol) + " AS DonViCoBan " +
                    "FROM SanPham " +
                    "WHERE UPPER(LTRIM(RTRIM(" + quoteName(idCol) + "))) = UPPER(LTRIM(RTRIM(?)))";

            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, sanPhamId.trim());

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return safe(rs.getString("DonViCoBan"));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private String layDonViTuBangDonViDoLuong(Connection con, String sanPhamId) {
        if (con == null || sanPhamId == null || sanPhamId.trim().isEmpty()) {
            return "";
        }

        try {
            if (!tableExists(con, "DonViDoLuong")) {
                return "";
            }

            String spCol = firstExistingColumn(con, "DonViDoLuong", "sanPhamId", "maSanPham", "maSP");
            String unitCol = firstExistingColumn(con, "DonViDoLuong",
                    "tenDonVi", "donVi", "donViTinh", "dvt", "ten");

            if (spCol.isEmpty() || unitCol.isEmpty()) {
                return "";
            }

            boolean hasDvcb = columnExists(con, "DonViDoLuong", "chuyenDoiDonViCoBan");

            String sql;

            if (hasDvcb) {
                sql = "SELECT TOP 1 " + quoteName(unitCol) + " AS DonViCoBan " +
                        "FROM DonViDoLuong " +
                        "WHERE UPPER(LTRIM(RTRIM(" + quoteName(spCol) + "))) = UPPER(LTRIM(RTRIM(?))) " +
                        "ORDER BY CASE WHEN chuyenDoiDonViCoBan = 1 THEN 0 ELSE 1 END";
            } else {
                sql = "SELECT TOP 1 " + quoteName(unitCol) + " AS DonViCoBan " +
                        "FROM DonViDoLuong " +
                        "WHERE UPPER(LTRIM(RTRIM(" + quoteName(spCol) + "))) = UPPER(LTRIM(RTRIM(?)))";
            }

            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, sanPhamId.trim());

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return safe(rs.getString("DonViCoBan"));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private String doanDonViTheoTenSanPham(String tenSanPham) {
        if (tenSanPham == null || tenSanPham.trim().isEmpty()) {
            return "đơn vị";
        }

        String ten = tenSanPham.trim().toLowerCase();

        if (ten.contains("siro")
                || ten.contains("xịt")
                || ten.contains("chai")
                || ten.contains("rohto")
                || ten.contains("osla")
                || ten.contains("listerine")
                || ten.contains("betadine")
                || ten.contains("cerave")
                || ten.contains("bioderma")
                || ten.contains("toner")
                || ten.contains("nước")) {
            return "Chai";
        }

        if (ten.contains("tuýp")
                || ten.contains("tuyp")
                || ten.contains("la roche")
                || ten.contains("kem chống nắng")) {
            return "Tuýp";
        }

        if (ten.contains("gói")
                || ten.contains("goi")
                || ten.contains("oresol")
                || ten.contains("smecta")
                || ten.contains("hapacol")
                || ten.contains("phosphalugel")
                || ten.contains("collagen")) {
            return "Gói";
        }

        if (ten.contains("hộp")
                || ten.contains("hop")
                || ten.contains("kem")
                || ten.contains("eucerin")) {
            return "Hộp";
        }

        return "Viên";
    }

    private boolean tableExists(Connection con, String tableName) {
        try {
            DatabaseMetaData meta = con.getMetaData();

            try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }

            try (ResultSet rs = meta.getTables(null, "dbo", tableName, new String[]{"TABLE"})) {
                return rs.next();
            }

        } catch (Exception e) {
            return false;
        }
    }

    private boolean columnExists(Connection con, String tableName, String columnName) {
        try {
            DatabaseMetaData meta = con.getMetaData();

            try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
                if (rs.next()) {
                    return true;
                }
            }

            try (ResultSet rs = meta.getColumns(null, "dbo", tableName, columnName)) {
                return rs.next();
            }

        } catch (Exception e) {
            return false;
        }
    }

    private String firstExistingColumn(Connection con, String tableName, String... columns) {
        for (String col : columns) {
            if (columnExists(con, tableName, col)) {
                return col;
            }
        }

        return "";
    }

    private void addColumnIfExists(Connection con, String tableName, List<String> list, String columnName) {
        if (columnExists(con, tableName, columnName)) {
            list.add(columnName);
        }
    }

    private String quoteName(String name) {
        if (name == null) {
            return "";
        }

        return "[" + name.replace("]", "]]") + "]";
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    public List<Object[]> layDuLieuLoHangCanDateTho() {
        List<Object[]> listData = new ArrayList<>();

        String sql = "SELECT l.id as maLo, l.soLoHang, s.ten as tenSP, d.gia as giaBan, l.gia as giaNhap, " +
                "DATEDIFF(day, GETDATE(), l.ngayHetHan) as soNgayConLai " +
                "FROM LoHang l " +
                "JOIN SanPham s ON l.sanPhamId = s.id " +
                "JOIN DonViDoLuong d ON s.id = d.sanPhamId " +
                "WHERE DATEDIFF(day, GETDATE(), l.ngayHetHan) BETWEEN 0 AND 90 " +
                "AND ISNULL(l.trangThai, 'CON_HANG') <> 'AN' " +
                "AND d.chuyenDoiDonViCoBan = 1";

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return listData;
        }

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                listData.add(new Object[]{
                        rs.getString("soLoHang"),
                        rs.getString("tenSP"),
                        rs.getDouble("giaBan"),
                        rs.getDouble("giaNhap"),
                        rs.getInt("soNgayConLai")
                });
            }

        } catch (Exception e) {
            System.err.println("Lỗi query Lô hàng cận date (DAO_LoHang): " + e.getMessage());
        }

        return listData;
    }
}