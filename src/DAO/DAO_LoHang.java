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

    public DAO_LoHang() {}

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
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
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
        try (PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            while (rs.next()) dsLoHang.add(mapLoHang(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return dsLoHang;
    }

    public LoHang getLoHangTheoId(String id) {
        String sql = "SELECT lh.*, sp.ten AS tenSanPham FROM LoHang lh LEFT JOIN SanPham sp ON lh.sanPhamId = sp.id WHERE lh.id = ?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return mapLoHang(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public LoHang getLoHangTheoSoLo(String soLoHang) {
        String sql = "SELECT lh.*, sp.ten AS tenSanPham FROM LoHang lh LEFT JOIN SanPham sp ON lh.sanPhamId = sp.id WHERE UPPER(LTRIM(RTRIM(lh.soLoHang))) = UPPER(LTRIM(RTRIM(?)))";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, soLoHang);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return mapLoHang(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<LoHang> layLoTheoSP(String maSP) {
        Connection con = ConnectDB.getInstance().getConnection();
        try { return layLoTheoSP(con, maSP); } catch (SQLException e) { e.printStackTrace(); return new ArrayList<>(); }
    }

    public List<LoHang> layLoTheoSP(Connection con, String maSP) throws SQLException {
        List<LoHang> dsLoHang = new ArrayList<>();
        String sql = "SELECT lh.*, sp.ten AS tenSanPham FROM LoHang lh LEFT JOIN SanPham sp ON lh.sanPhamId = sp.id WHERE lh.sanPhamId = ? AND lh.soLuongLoHang > 0 AND ISNULL(lh.trangThai, 'CON_HANG') NOT IN (?, ?) ORDER BY lh.ngayHetHan ASC, lh.ngayNhap ASC";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maSP); pst.setString(2, TrangThaiLoHang.HET_HAN.name()); pst.setString(3, TrangThaiLoHang.AN.name());
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) dsLoHang.add(mapLoHang(rs));
            }
        }
        return dsLoHang;
    }

    public boolean themLoHang(LoHang lo) {
        String sql = "INSERT INTO LoHang(id, soLoHang, soLuongLoHang, gia, ngayNhap, ngayHetHan, trangThai, sanPhamId, khoHangId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, lo.getId()); pst.setString(2, lo.getSoLoHang()); pst.setInt(3, lo.getSoLuongLoHang());
            pst.setInt(4, lo.getGia()); pst.setTimestamp(5, java.sql.Timestamp.valueOf(lo.getNgayNhap()));
            pst.setTimestamp(6, java.sql.Timestamp.valueOf(lo.getNgayHetHan())); pst.setString(7, lo.getTrangThai().name());
            pst.setString(8, lo.getSanPhamId() == null ? null : lo.getSanPhamId().getId());
            pst.setString(9, lo.getKhoHangId() == null ? null : lo.getKhoHangId().getId());
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean khoiPhucVaCapNhatLoHang(LoHang lo) {
        String sql = "UPDATE LoHang SET sanPhamId = ?, soLuongLoHang = ?, gia = ?, ngayNhap = ?, ngayHetHan = ?, khoHangId = ?, trangThai = CASE WHEN ? < GETDATE() THEN 'HET_HAN' WHEN ? <= 0 THEN 'HET_HANG' ELSE 'CON_HANG' END WHERE id = ? AND trangThai = 'AN'";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, lo.getSanPhamId() == null ? null : lo.getSanPhamId().getId()); pst.setInt(2, lo.getSoLuongLoHang());
            pst.setInt(3, lo.getGia()); pst.setTimestamp(4, java.sql.Timestamp.valueOf(lo.getNgayNhap()));
            pst.setTimestamp(5, java.sql.Timestamp.valueOf(lo.getNgayHetHan())); pst.setString(6, lo.getKhoHangId() == null ? null : lo.getKhoHangId().getId());
            pst.setTimestamp(7, java.sql.Timestamp.valueOf(lo.getNgayHetHan())); pst.setInt(8, lo.getSoLuongLoHang()); pst.setString(9, lo.getId());
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean anLoHang(String maLoHang) {
        String sql = "UPDATE LoHang SET trangThai = 'AN' WHERE id = ? AND ISNULL(trangThai, 'CON_HANG') <> 'AN'";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maLoHang); return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean khoiPhucLoHang(String maLoHang) {
        String sql = "UPDATE LoHang SET trangThai = CASE WHEN ngayHetHan < GETDATE() THEN 'HET_HAN' WHEN soLuongLoHang <= 0 THEN 'HET_HANG' ELSE 'CON_HANG' END WHERE id = ? AND trangThai = 'AN'";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maLoHang); return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean capNhatTrangThaiLo(String maLoHang, TrangThaiLoHang trangThaiMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        try { return capNhatTrangThaiLo(con, maLoHang, trangThaiMoi); } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean capNhatTrangThaiLo(Connection con, String maLoHang, TrangThaiLoHang trangThaiMoi) throws SQLException {
        String sql = "UPDATE LoHang SET trangThai = ? WHERE id = ? AND ISNULL(trangThai, 'CON_HANG') <> 'AN'";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, trangThaiMoi.name()); pst.setString(2, maLoHang); return pst.executeUpdate() > 0;
        }
    }

    public boolean capNhatSoLuongTon(String maLoHang, int soLuongMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        try { return capNhatSoLuongTon(con, maLoHang, soLuongMoi); } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean capNhatSoLuongTon(Connection con, String maLoHang, int soLuongMoi) throws SQLException {
        if (soLuongMoi < 0) return false;
        String sql = "UPDATE LoHang SET soLuongLoHang = ? WHERE id = ? AND ISNULL(trangThai, 'CON_HANG') <> 'AN'";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, soLuongMoi); pst.setString(2, maLoHang); return pst.executeUpdate() > 0;
        }
    }

    public boolean capNhatSoLuongVaTrangThaiLo(String maLoHang, int soLuongMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        try { return capNhatSoLuongVaTrangThaiLo(con, maLoHang, soLuongMoi); } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean capNhatSoLuongVaTrangThaiLo(Connection con, String maLoHang, int soLuongMoi) throws SQLException {
        if (soLuongMoi < 0) return false;
        String sql = "UPDATE LoHang SET soLuongLoHang = ?, trangThai = CASE WHEN ISNULL(trangThai, 'CON_HANG') = 'AN' THEN 'AN' WHEN ngayHetHan < GETDATE() THEN 'HET_HAN' WHEN ? <= 0 THEN 'HET_HANG' ELSE 'CON_HANG' END WHERE id = ? AND ISNULL(trangThai, 'CON_HANG') <> 'AN'";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, soLuongMoi); pst.setInt(2, soLuongMoi); pst.setString(3, maLoHang); return pst.executeUpdate() > 0;
        }
    }

    public boolean dongBoTrangThaiLoHang() {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            con.setAutoCommit(false);
            try (PreparedStatement pst1 = con.prepareStatement("UPDATE LoHang SET trangThai = 'HET_HAN' WHERE ISNULL(trangThai, 'CON_HANG') <> 'AN' AND ngayHetHan < GETDATE()")) { pst1.executeUpdate(); }
            try (PreparedStatement pst2 = con.prepareStatement("UPDATE LoHang SET trangThai = 'HET_HANG' WHERE ISNULL(trangThai, 'CON_HANG') <> 'AN' AND ngayHetHan >= GETDATE() AND soLuongLoHang <= 0")) { pst2.executeUpdate(); }
            try (PreparedStatement pst3 = con.prepareStatement("UPDATE LoHang SET trangThai = 'CON_HANG' WHERE ISNULL(trangThai, 'CON_HANG') <> 'AN' AND ngayHetHan >= GETDATE() AND soLuongLoHang > 0")) { pst3.executeUpdate(); }
            con.commit(); con.setAutoCommit(true); return true;
        } catch (Exception e) {
            try { con.rollback(); con.setAutoCommit(true); } catch (Exception ignored) {}
            e.printStackTrace(); return false;
        }
    }

    private LoHang mapLoHang(ResultSet rs) throws SQLException {
        LoHang lh = new LoHang();
        lh.setId(rs.getString("id")); lh.setSoLoHang(rs.getString("soLoHang")); lh.setSoLuongLoHang(rs.getInt("soLuongLoHang")); lh.setGia((int) Math.round(rs.getDouble("gia")));
        if (rs.getString("trangThai") != null) lh.setTrangThai(TrangThaiLoHang.valueOf(rs.getString("trangThai")));
        if (rs.getTimestamp("ngayHetHan") != null) lh.setNgayHetHan(rs.getTimestamp("ngayHetHan").toLocalDateTime());
        if (rs.getTimestamp("ngayNhap") != null) lh.setNgayNhap(rs.getTimestamp("ngayNhap").toLocalDateTime());
        SanPham sp = new SanPham(); sp.setId(rs.getString("sanPhamId"));
        try { sp.setTen(rs.getString("tenSanPham")); } catch (Exception ignored) {}
        lh.setSanPhamId(sp);
        KhoHang kho = new KhoHang(); kho.setId(rs.getString("khoHangId")); lh.setKhoHangId(kho);
        return lh;
    }

    public int xuatKhoFEFO(String maSP, int soLuongCanXuat) {
        Connection con = null; int soLuongBanDau = soLuongCanXuat;
        try {
            con = ConnectDB.getInstance().getConnection(); con.setAutoCommit(false);
            List<LoHang> dsLo = layLoTheoSP(con, maSP);   
            int soLuongConThieu = soLuongCanXuat;
            for (LoHang lh : dsLo) {
                if (soLuongConThieu == 0) break;
                if (lh == null || lh.getSoLuongLoHang() <= 0) continue;
                if (lh.getTrangThai() == TrangThaiLoHang.AN) continue;
                int soLuongXuat = Math.min(lh.getSoLuongLoHang(), soLuongConThieu);
                int soLuongMoi = lh.getSoLuongLoHang() - soLuongXuat;
                boolean ok = capNhatSoLuongVaTrangThaiLo(con, lh.getId(), soLuongMoi);  
                if (!ok) throw new SQLException("Không cập nhật được lô: " + lh.getId());
                soLuongConThieu -= soLuongXuat;
            }
            if (soLuongConThieu > 0) throw new SQLException("Kho không đủ hàng. Còn thiếu " + soLuongConThieu + " đơn vị.");
            con.commit(); return 0;
        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace(); return soLuongBanDau;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // =========================================================
    // HÀM XỬ LÝ XUẤT HỦY KHO TỪ MANHINHXUATKHO
    // =========================================================
    public boolean thucThiXuatHuyKhoBangTransaction(List<Object[]> danhSachXuat, String nguoiThucHien) {
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return false;
        try {
            con.setAutoCommit(false);
            for (Object[] item : danhSachXuat) {
                String maLo = item[0].toString();
                int slXuat = Integer.parseInt(item[1].toString());
                String lyDo = item[2].toString();

                if (!truTonKhoInternal(maLo, slXuat, con)) { con.rollback(); return false; }
                if (!ghiNhanLichSuXuatHuyInternal(maLo, slXuat, lyDo, nguoiThucHien, con)) { con.rollback(); return false; }
            }
            con.commit(); return true;
        } catch (Exception e) {
            e.printStackTrace();
            try { con.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private boolean truTonKhoInternal(String soLo, int soLuongXuat, Connection con) throws SQLException {
        String sql = "UPDATE LoHang SET soLuongLoHang = soLuongLoHang - ? WHERE UPPER(LTRIM(RTRIM(soLoHang))) = UPPER(LTRIM(RTRIM(?))) AND soLuongLoHang >= ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, soLuongXuat); stmt.setString(2, soLo); stmt.setInt(3, soLuongXuat); 
            return stmt.executeUpdate() > 0;
        }
    }

    private boolean ghiNhanLichSuXuatHuyInternal(String soLo, int soLuongXuat, String lyDo, String nguoiThucHien, Connection con) throws SQLException {
        String sql = "INSERT INTO PhieuXuatKho (SoLoHang, SoLuongXuat, LyDoXuat, NguoiThucHien, NgayXuat) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, soLo.trim()); stmt.setInt(2, soLuongXuat); stmt.setString(3, lyDo); stmt.setString(4, nguoiThucHien);
            return stmt.executeUpdate() > 0;
        }
    }

    // =========================================================
    // LẤY LỊCH SỬ XUẤT KHO LÊN GIAO DIỆN
    // =========================================================
    public List<Object[]> layLichSuXuatKho() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT px.NgayXuat, px.SoLoHang, sp.ten AS TenSanPham, px.SoLuongXuat, px.LyDoXuat, px.NguoiThucHien " +
                     "FROM PhieuXuatKho px " +
                     "LEFT JOIN LoHang lh ON UPPER(LTRIM(RTRIM(px.SoLoHang))) = UPPER(LTRIM(RTRIM(lh.soLoHang))) " +
                     "LEFT JOIN SanPham sp ON lh.sanPhamId = sp.id " +
                     "ORDER BY px.NgayXuat DESC";
        
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return list;
        try (PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getTimestamp("NgayXuat"), rs.getString("SoLoHang"),
                    rs.getString("TenSanPham") != null ? rs.getString("TenSanPham") : "Sản phẩm không xác định",
                    rs.getInt("SoLuongXuat"), rs.getString("LyDoXuat"),
                    rs.getString("NguoiThucHien") != null ? rs.getString("NguoiThucHien") : "Hệ thống"
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
 // =========================================================
    // HÀM LẤY LÔ HÀNG CẬN DATE (PHỤC VỤ GỢI Ý KHUYẾN MÃI Ở TẦNG BUS)
    // =========================================================
    public List<Object[]> layDuLieuLoHangCanDateTho() {
        List<Object[]> listData = new ArrayList<>();
        
        // Tối ưu Query: Không dùng SubQuery. 
        // Lấy l.gia làm Giá Nhập và d.gia làm Giá Bán.
        String sql = "SELECT l.id as maLo, l.soLoHang, s.ten as tenSP, d.gia as giaBan, l.gia as giaNhap, " +
                     "DATEDIFF(day, GETDATE(), l.ngayHetHan) as soNgayConLai " +
                     "FROM LoHang l " +
                     "JOIN SanPham s ON l.sanPhamId = s.id " +
                     "JOIN DonViDoLuong d ON s.id = d.sanPhamId " +
                     "WHERE DATEDIFF(day, GETDATE(), l.ngayHetHan) BETWEEN 0 AND 90 " +
                     "AND ISNULL(l.trangThai, 'CON_HANG') <> 'AN' " +
                     "AND d.chuyenDoiDonViCoBan = 1";
                     
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return listData;
        
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
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