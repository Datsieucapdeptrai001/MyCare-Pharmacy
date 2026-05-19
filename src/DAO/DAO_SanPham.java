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

        String sql = "SELECT id, ten, danhMuc, ISNULL(hoatChat,'') AS hoatChat, dang, " +
                "ISNULL(nhaSanXuat,'Khác') AS nhaSanXuat, ISNULL(thueVAT,0) AS thueVAT, " +
                "ISNULL(giaBan,0) AS giaBan, " +
                "ISNULL(maVach,'') AS maVach, " +
                "ISNULL(nhomBenhLy,'') AS nhomBenhLy " +
                "FROM SanPham " +
                "WHERE ISNULL(trangThai,'HOAT_DONG') != 'AN'";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String loai = mapDanhMucToLabel(rs.getString("danhMuc"));
                String dang = mapDangToLabel(rs.getString("dang"));
                String vat = rs.getDouble("thueVAT") + "%";
                String giaBan = String.format("%,.0f", rs.getDouble("giaBan"));

                ds.add(new Object[] {
                        rs.getString("id"),
                        rs.getString("ten"),
                        loai,
                        rs.getString("hoatChat"),
                        dang,
                        rs.getString("nhaSanXuat"),
                        vat,
                        giaBan,
                        rs.getString("maVach"),
                        rs.getString("nhomBenhLy")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }

    public List<Object[]> layDanhSachSanPhamDaAn() {
        List<Object[]> ds = new ArrayList<>();

        String sql = "SELECT id, ten, danhMuc, hoatChat, dang, nhaSanXuat, thueVAT, " +
                "ISNULL(giaBan,0) AS giaBan, " +
                "ISNULL(maVach,'') AS maVach, " +
                "ISNULL(nhomBenhLy,'') AS nhomBenhLy " +
                "FROM SanPham WHERE trangThai='AN'";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                ds.add(new Object[] {
                        rs.getString("id"),
                        rs.getString("ten"),
                        rs.getString("danhMuc"),
                        rs.getString("hoatChat"),
                        rs.getString("dang"),
                        rs.getString("nhaSanXuat"),
                        rs.getDouble("thueVAT") + "%",
                        String.format("%,.0f", rs.getDouble("giaBan")),
                        rs.getString("maVach"),
                        rs.getString("nhomBenhLy")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }

    public boolean kiemTraMaSPTonTai(String id) {
        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement("SELECT COUNT(*) FROM SanPham WHERE id=?")) {

            pst.setString(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean themSanPhamNhanh(String id, String danhMuc, String dang, String ten,
            String vietTat, String nsx, String hoatChat, double vat,
            String hamLuong, String moTa, String dvt, double giaBan,
            String maVach, String nhomBenhLy) {
        return themSanPhamNhanh(id, danhMuc, dang, ten, vietTat, nsx, hoatChat,
                vat, hamLuong, moTa, dvt, giaBan, maVach, nhomBenhLy, null);
    }

    public boolean themSanPhamNhanh(String id, String danhMuc, String dang, String ten,
            String vietTat, String nsx, String hoatChat, double vat,
            String hamLuong, String moTa, String dvt, double giaBan,
            String maVach, String nhomBenhLy, String viTriId) {
        if (kiemTraMaSPTonTai(id))
            return false;

        String sql = "INSERT INTO SanPham (id, danhMuc, dang, ten, tenVietTat, nhaSanXuat, hoatChat, " +
                "thueVAT, hamLuong, moTa, donViDoCoBan, giaBan, ngayTao, maVach, nhomBenhLy, viTriId) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,GETDATE(),?,?,?)";

        try (Connection con = ConnectDB.getInstance().getConnection();
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
            pst.setDouble(12, giaBan);

            if (maVach == null || maVach.isEmpty()) {
                pst.setNull(13, java.sql.Types.NVARCHAR);
            } else {
                pst.setString(13, maVach);
            }

            if (nhomBenhLy == null || nhomBenhLy.isEmpty()) {
                pst.setNull(14, java.sql.Types.NVARCHAR);
            } else {
                pst.setString(14, nhomBenhLy);
            }

            if (viTriId == null || viTriId.isEmpty()) {
                pst.setNull(15, java.sql.Types.NVARCHAR);
            } else {
                pst.setString(15, viTriId);
            }

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean capNhatSanPhamNhanh(String id, String danhMuc, String dang, String ten,
            String vietTat, String nsx, String hoatChat, double vat,
            String hamLuong, String moTa, String dvt, double giaBan,
            String maVach, String nhomBenhLy) {
        return capNhatSanPhamNhanh(id, danhMuc, dang, ten, vietTat, nsx, hoatChat,
                vat, hamLuong, moTa, dvt, giaBan, maVach, nhomBenhLy, null);
    }

    public boolean capNhatSanPhamNhanh(String id, String danhMuc, String dang, String ten,
            String vietTat, String nsx, String hoatChat, double vat,
            String hamLuong, String moTa, String dvt, double giaBan,
            String maVach, String nhomBenhLy, String viTriId) {
        String sql = "UPDATE SanPham SET danhMuc=?,dang=?,ten=?,tenVietTat=?,nhaSanXuat=?," +
                "hoatChat=?,thueVAT=?,hamLuong=?,moTa=?,donViDoCoBan=?,giaBan=?," +
                "maVach=?,nhomBenhLy=?,viTriId=? " +
                "WHERE id=?";

        try (Connection con = ConnectDB.getInstance().getConnection();
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
            pst.setDouble(11, giaBan);

            if (maVach == null || maVach.isEmpty()) {
                pst.setNull(12, java.sql.Types.NVARCHAR);
            } else {
                pst.setString(12, maVach);
            }

            if (nhomBenhLy == null || nhomBenhLy.isEmpty()) {
                pst.setNull(13, java.sql.Types.NVARCHAR);
            } else {
                pst.setString(13, nhomBenhLy);
            }

            if (viTriId == null || viTriId.isEmpty()) {
                pst.setNull(14, java.sql.Types.NVARCHAR);
            } else {
                pst.setString(14, viTriId);
            }

            pst.setString(15, id);

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean capNhatMaVachSanPham(String maSP, String maVachMoi) {
        String sql = "UPDATE SanPham SET maVach = ? WHERE id = ?";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {

            if (maVachMoi == null || maVachMoi.trim().isEmpty()) {
                pst.setNull(1, java.sql.Types.NVARCHAR);
            } else {
                pst.setString(1, maVachMoi.trim());
            }

            pst.setString(2, maSP.trim());

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<SanPham> getDsThuoc() {
        List<SanPham> dsSanPham = new ArrayList<>();

        String sql = "SELECT * FROM SanPham WHERE ISNULL(trangThai,'HOAT_DONG') != 'AN'";

        try (Connection con = ConnectDB.getInstance().getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                dsSanPham.add(mapSanPham(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsSanPham;
    }

    public SanPham getSanPhamTheoMa(String id) {
        SanPham sp = null;

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement("SELECT * FROM SanPham WHERE id = ?")) {

            pst.setString(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    sp = mapSanPham(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sp;
    }

    public SanPham getSanPhamDayDu(String id) {
        return getSanPhamTheoMa(id);
    }

    public List<String> layDanhSachNhomBenhLy() {
        List<String> ds = new ArrayList<>();

        String sql = "SELECT DISTINCT nhomBenhLy FROM SanPham " +
                "WHERE nhomBenhLy IS NOT NULL AND nhomBenhLy != '' " +
                "AND ISNULL(trangThai,'HOAT_DONG') != 'AN' " +
                "ORDER BY nhomBenhLy";

        try (Connection con = ConnectDB.getInstance().getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                ds.add(rs.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ds;
    }

    public List<Object[]> timKiemSanPhamBan(String text) {
        List<Object[]> ds = new ArrayList<>();

        // sp.maVach lưu dạng CSV ("8934...,8934...") — phải dùng exact pattern để tránh match nhầm
        // FIX: đổi "OR sp.maVach LIKE ?" thành "',' + sp.maVach + ',' LIKE ?"
        String sql = "SELECT DISTINCT sp.id, sp.ten, " +
                "ISNULL((SELECT TOP 1 ten FROM DonViDoLuong WHERE sanPhamId = sp.id AND maVach LIKE ?), " +
                "    ISNULL((SELECT TOP 1 ten FROM DonViDoLuong WHERE sanPhamId = sp.id ORDER BY chuyenDoiDonViCoBan DESC), sp.donViDoCoBan)"
                +
                ") AS donViHienThi, " +
                "ISNULL((SELECT TOP 1 gia FROM DonViDoLuong WHERE sanPhamId = sp.id AND maVach LIKE ?), " +
                "    ISNULL((SELECT TOP 1 gia FROM DonViDoLuong WHERE sanPhamId = sp.id ORDER BY chuyenDoiDonViCoBan DESC), sp.giaBan)"
                +
                ") AS giaHienThi, " +
                "lh.soLuongLoHang AS soLuongTon, sp.danhMuc, ISNULL(sp.thueVAT,0) AS thueVAT, " +
                "lh.soLoHang, lh.ngayHetHan " +
                "FROM SanPham sp " +
                "JOIN LoHang lh ON sp.id = lh.sanPhamId " +
                "LEFT JOIN DonViDoLuong dv ON sp.id = dv.sanPhamId " +
                "WHERE (sp.ten LIKE ? OR sp.tenVietTat LIKE ? OR sp.hoatChat LIKE ? " +
                "       OR sp.id LIKE ? OR lh.soLoHang LIKE ? " +
                "       OR dv.maVach LIKE ? OR lh.maVachNoiBo = ? " +
                "       OR ',' + ISNULL(sp.maVach,'') + ',' LIKE ?) " +
                "AND ISNULL(lh.trangThai,'CON_HANG') = 'CON_HANG' " +
                "AND lh.soLuongLoHang > 0 " +
                "AND (lh.ngayHetHan IS NULL OR lh.ngayHetHan >= GETDATE()) " +
                "ORDER BY lh.ngayHetHan ASC";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {

            String p = "%" + text + "%";
            // FIX: sp.maVach dùng exact CSV pattern — "%,<text>,%" — không dùng p thông thường
            String pMaVach = "%," + text + ",%";

            pst.setString(1, p);       // DonViDoLuong.maVach cho đơn vị hiển thị
            pst.setString(2, p);       // DonViDoLuong.maVach cho giá
            pst.setString(3, p);       // sp.ten
            pst.setString(4, p);       // sp.tenVietTat
            pst.setString(5, p);       // sp.hoatChat
            pst.setString(6, p);       // sp.id
            pst.setString(7, p);       // lh.soLoHang
            pst.setString(8, p);       // dv.maVach (đơn vị tính)
            pst.setString(9, text);    // lh.maVachNoiBo — khớp chính xác
            pst.setString(10, pMaVach); // sp.maVach CSV — exact match từng mã

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String loai = mapDanhMucToLabel(rs.getString("danhMuc"));
                    String loHang = rs.getString("soLoHang");

                    java.sql.Date dateHSD = rs.getDate("ngayHetHan");
                    String hsdStr = dateHSD != null
                            ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(dateHSD)
                            : "";

                    ds.add(new Object[] {
                            rs.getString("id"),
                            rs.getString("ten"),
                            rs.getString("donViHienThi"),
                            rs.getDouble("giaHienThi"),
                            rs.getInt("soLuongTon"),
                            loai,
                            rs.getDouble("thueVAT"),
                            loHang,
                            hsdStr
                    });
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }
    
    public double layThueVATTheoTenSP(String tenSP) {
        double vat = 0;

        String sql = "SELECT thueVAT FROM SanPham WHERE ten = ?";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, tenSP);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    vat = rs.getDouble("thueVAT");
            }

        } catch (Exception e) {
            System.err.println("Lỗi lấy VAT: " + e.getMessage());
        }

        return vat;
    }

    public List<LoHang> layLoTheoSP(String maSP) {
        List<LoHang> dsLoHang = new ArrayList<>();

        String sql = "SELECT * FROM LoHang WHERE sanPhamId = ? " +
                "AND soLuongLoHang > 0 AND ISNULL(trangThai, 'CON_HANG') <> 'AN' " +
                "ORDER BY ngayNhap ASC";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, maSP);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    LoHang lh = new LoHang();

                    lh.setId(rs.getString("id"));
                    lh.setSoLoHang(rs.getString("soLoHang"));
                    lh.setSoLuongLoHang(rs.getInt("soLuongLoHang"));
                    lh.setGia(rs.getInt("gia"));
                    try {
                        lh.setMaVachNoiBo(rs.getString("maVachNoiBo"));
                    } catch (Exception ignored) {}

                    // Chỉ để lại ĐÚNG 1 khối if check trạng thái này thôi
                    if (rs.getString("trangThai") != null) {
                        lh.setTrangThai(TrangThaiLoHang.valueOf(rs.getString("trangThai")));
                    }

                    if (rs.getTimestamp("ngayHetHan") != null) {
                        lh.setNgayHetHan(rs.getTimestamp("ngayHetHan").toLocalDateTime());
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

    public List<Object[]> layDonViDoLuongTheoSP(String maSP) {
        return layDonViQuyDoiTheoSP(maSP);
    }

    private List<Object[]> _layDonViDoLuongTheoSP_DEPRECATED(String maSP) {
        List<Object[]> ds = new ArrayList<>();

        String sql = "SELECT ten, chuyenDoiDonViCoBan, gia FROM DonViDoLuong " +
                "WHERE sanPhamId = ? ORDER BY chuyenDoiDonViCoBan ASC";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, maSP);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ds.add(new Object[] {
                            rs.getString("ten"),
                            rs.getBigDecimal("chuyenDoiDonViCoBan").toPlainString()
                    });
                }
            }

        } catch (SQLException e) {
            System.err.println("layDonViDoLuongTheoSP lỗi: " + e.getMessage());
        }

        return ds;
    }

    public List<LoHang> layTatCaLoTheoSP(String maSP) {
        List<LoHang> dsLoHang = new ArrayList<>();

        String sql = "SELECT * FROM LoHang WHERE sanPhamId = ? ORDER BY ngayNhap DESC";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, maSP);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    LoHang lh = new LoHang();

                    lh.setId(rs.getString("id"));
                    lh.setSoLoHang(rs.getString("soLoHang"));
                    lh.setSoLuongLoHang(rs.getInt("soLuongLoHang"));
                    lh.setGia(rs.getInt("gia"));
                    try {
                        lh.setMaVachNoiBo(rs.getString("maVachNoiBo"));
                    } catch (Exception ignored) {}

                    if (rs.getString("trangThai") != null) {
                        lh.setTrangThai(TrangThaiLoHang.valueOf(rs.getString("trangThai")));
                    }

                    if (rs.getTimestamp("ngayHetHan") != null) {
                        lh.setNgayHetHan(rs.getTimestamp("ngayHetHan").toLocalDateTime());
                    }

                    SanPham sp = new SanPham();
                    sp.setId(rs.getString("sanPhamId"));
                    lh.setSanPhamId(sp);

                    dsLoHang.add(lh);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dsLoHang;
    }

    public boolean capNhatSoLuongTon(String maLoHang, int soLuongMoi) {
        if (soLuongMoi < 0)
            return false;

        String sql = "UPDATE LoHang SET soLuongLoHang = ? WHERE id = ? AND ISNULL(trangThai,'CON_HANG') <> 'AN'";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, soLuongMoi);
            pst.setString(2, maLoHang);

            return pst.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String layMaSanPhamMoiNhat() {
        String sql = "SELECT TOP 1 id FROM SanPham WHERE id LIKE 'SP%-%' " +
                "AND ISNULL(TRY_CAST(SUBSTRING(id, CHARINDEX('-', id) + 1, LEN(id)) AS INT), -1) >= 0 " +
                "ORDER BY TRY_CAST(SUBSTRING(id, CHARINDEX('-', id) + 1, LEN(id)) AS INT) DESC";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                String lastId = rs.getString("id");
                String[] parts = lastId.split("-");

                if (parts.length > 1) {
                    String numStr = parts[1].replaceAll("[^0-9]", "");

                    if (!numStr.isEmpty()) {
                        int number = Integer.parseInt(numStr);
                        return String.format("SP2024-%04d", number + 1);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "SP2024-0001";
    }

    public boolean anSanPham(String id) {
        String sql = "UPDATE SanPham SET trangThai = 'AN' WHERE id = ?";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, id);
            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<SanPham> timKiemSanPhamDoiTra(String tuKhoa) {
        List<SanPham> ds = new ArrayList<>();

        String sql = "SELECT * FROM SanPham WHERE (ten LIKE ? OR id LIKE ?) " +
                "AND ISNULL(trangThai,'HOAT_DONG') != 'AN'";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {

            String p = "%" + tuKhoa + "%";

            pst.setString(1, p);
            pst.setString(2, p);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    SanPham sp = new SanPham();

                    sp.setId(rs.getString("id"));
                    sp.setTen(rs.getString("ten"));

                    String danhMucStr = rs.getString("danhMuc");

                    if (danhMucStr != null && !danhMucStr.isEmpty()) {
                        sp.setDanhMuc(DanhMucSanPham.valueOf(danhMucStr));
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
        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement("DELETE FROM SanPham WHERE id=?")) {

            pst.setString(1, id);
            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public int getSoLuongTon(String maSP) {
        String sql = "SELECT ISNULL(SUM(lh.soLuongLoHang),0) AS tonKho FROM LoHang lh " +
                "WHERE lh.sanPhamId=? AND lh.soLuongLoHang>0 " +
                "AND (lh.ngayHetHan IS NULL OR lh.ngayHetHan >= GETDATE()) " +
                "AND ISNULL(lh.trangThai,'CON_HANG') NOT IN ('AN','HET_HAN','HET_HANG')";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, maSP);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    return rs.getInt("tonKho");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean khoiPhucSanPham(String id) {
        String sql = "UPDATE SanPham SET trangThai='HOAT_DONG' WHERE id=?";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, id);
            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean laSanPhamDaAn(String maSP) {
        String sql = "SELECT COUNT(*) FROM SanPham WHERE id = ? AND trangThai = 'AN'";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, maSP);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<String> layDanhSachTenSanPham() {
        List<String> ds = new ArrayList<>();

        String sql = "SELECT ten FROM SanPham WHERE ISNULL(trangThai,'HOAT_DONG') != 'AN'";

        try (Connection con = ConnectDB.getInstance().getConnection();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ds.add(rs.getString("ten"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ds;
    }

    public List<String> layDanhSachNhaSanXuat() {
        List<String> ds = new ArrayList<>();

        String sql = "SELECT DISTINCT nhaSanXuat FROM SanPham WHERE nhaSanXuat IS NOT NULL AND nhaSanXuat != ''";

        try (Connection con = ConnectDB.getInstance().getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                ds.add(rs.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ds;
    }

    public List<String> layDanhSachDonViTinh() {
        List<String> ds = new ArrayList<>();

        String sql = "SELECT DISTINCT ten FROM DonViDoLuong WHERE ten IS NOT NULL AND ten != ''";

        try (Connection con = ConnectDB.getInstance().getConnection();
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                ds.add(rs.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ds;
    }

    public void luuDonViQuyDoi(String maSP, String donViCoBan, double giaBanCoBan, List<Object[]> dsDonVi) {
        if (dsDonVi == null)
            return;

        try (Connection con = ConnectDB.getInstance().getConnection()) {
            if (con == null)
                return;

            java.util.Set<String> tenMoi = new java.util.HashSet<>();

            for (Object[] dv : dsDonVi) {
                if (dv[0] != null && !dv[0].toString().trim().isEmpty()) {
                    tenMoi.add(dv[0].toString().trim().toLowerCase());
                }
            }

            List<String> tenCu = new ArrayList<>();

            try (PreparedStatement pst = con.prepareStatement(
                    "SELECT id, ten FROM DonViDoLuong WHERE sanPhamId = ?")) {

                pst.setString(1, maSP);

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        String tenDb = rs.getString("ten");

                        if (!tenMoi.contains(tenDb.toLowerCase())) {
                            tenCu.add(rs.getString("id"));
                        }
                    }
                }
            }

            for (String idCu : tenCu) {
                try (PreparedStatement pst = con.prepareStatement(
                        "DELETE FROM DonViDoLuong WHERE id = ?")) {

                    pst.setString(1, idCu);
                    pst.executeUpdate();
                }
            }

            for (Object[] dv : dsDonVi) {
                if (dv[0] == null || dv[0].toString().trim().isEmpty())
                    continue;

                String tenDVT = dv[0].toString().trim();
                double tiLe = 1.0;
                double gia = 0.0;

                try {
                    tiLe = Double.parseDouble(dv[1].toString());
                } catch (Exception ignored) {
                }

                try {
                    String giaStr = dv[2].toString().replace(",", "").trim();
                    gia = Double.parseDouble(giaStr);
                } catch (Exception ignored) {
                }

                if (gia > 0) {
                    upsertDonVi(con, maSP, tenDVT, tiLe, gia);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void upsertDonVi(Connection con, String maSP, String tenDVT, double tiLe, double gia) throws SQLException {
        String donViId = null;

        String sqlCheck = "SELECT id FROM DonViDoLuong WHERE sanPhamId = ? AND ten = ?";

        try (PreparedStatement pst = con.prepareStatement(sqlCheck)) {
            pst.setString(1, maSP);
            pst.setString(2, tenDVT);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                donViId = rs.getString("id");
            }
        }

        if (donViId != null) {
            String sqlUpd = "UPDATE DonViDoLuong SET chuyenDoiDonViCoBan = ?, gia = ? WHERE id = ?";

            try (PreparedStatement pst = con.prepareStatement(sqlUpd)) {
                pst.setDouble(1, tiLe);
                pst.setDouble(2, gia);
                pst.setString(3, donViId);
                pst.executeUpdate();
            }
        } else {
            String sqlIns = "INSERT INTO DonViDoLuong (id, sanPhamId, ten, chuyenDoiDonViCoBan, gia) VALUES (?,?,?,?,?)";

            try (PreparedStatement pst = con.prepareStatement(sqlIns)) {
                String uuid = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
                String newId = "DVL-" + uuid;

                pst.setString(1, newId);
                pst.setString(2, maSP);
                pst.setString(3, tenDVT);
                pst.setDouble(4, tiLe);
                pst.setDouble(5, gia);

                pst.executeUpdate();
            }
        }
    }

    public List<Object[]> layDonViQuyDoiTheoSP(String maSP) {
        List<Object[]> ds = new ArrayList<>();

        String sql = "SELECT ten, chuyenDoiDonViCoBan, gia FROM DonViDoLuong " +
                "WHERE sanPhamId = ? ORDER BY chuyenDoiDonViCoBan ASC";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, maSP);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ds.add(new Object[] {
                            rs.getString("ten"),
                            rs.getDouble("chuyenDoiDonViCoBan"),
                            String.format("%,.0f", rs.getDouble("gia"))
                    });
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }

    public List<Object[]> layDanhSachGoiYKhuyenMai() {
        List<Object[]> danhSachGoiY = new ArrayList<>();

        String sql = "SELECT sp.id, sp.ten, lh.soLoHang, lh.ngayHetHan, lh.soLuongLoHang, " +
                "lh.gia AS giaNhap, sp.giaBan " +
                "FROM SanPham sp JOIN LoHang lh ON sp.id = lh.sanPhamId " +
                "WHERE lh.soLuongLoHang > 0 " +
                "AND ISNULL(lh.trangThai,'CON_HANG') = 'CON_HANG' " +
                "AND (lh.ngayHetHan IS NULL OR lh.ngayHetHan >= GETDATE()) " +
                "AND (lh.ngayHetHan IS NULL OR DATEDIFF(day, GETDATE(), lh.ngayHetHan) <= 90 OR lh.soLuongLoHang > 100) "
                +
                "ORDER BY lh.ngayHetHan ASC";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                java.time.LocalDateTime ngayHetHan = null;

                if (rs.getTimestamp("ngayHetHan") != null) {
                    ngayHetHan = rs.getTimestamp("ngayHetHan").toLocalDateTime();
                }

                danhSachGoiY.add(new Object[] {
                        rs.getString("id"),
                        rs.getString("ten"),
                        rs.getString("soLoHang"),
                        ngayHetHan,
                        rs.getInt("soLuongLoHang"),
                        rs.getDouble("giaNhap"),
                        rs.getDouble("giaBan")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return danhSachGoiY;
    }

    public Object[] laySanPhamGoiYTheoHoatChat(String tuKhoaHoatChat) {
        String sql = "SELECT TOP 1 sp.id, sp.ten, sp.giaBan FROM SanPham sp " +
                "JOIN LoHang lh ON sp.id = lh.sanPhamId " +
                "WHERE (sp.hoatChat LIKE ? OR sp.ten LIKE ?) " +
                "AND lh.soLuongLoHang > 0 " +
                "AND (lh.ngayHetHan IS NULL OR lh.ngayHetHan >= GETDATE()) " +
                "AND ISNULL(lh.trangThai,'CON_HANG') = 'CON_HANG' " +
                "AND ISNULL(sp.trangThai,'HOAT_DONG') != 'AN' " +
                "ORDER BY lh.soLuongLoHang DESC";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {

            String tk = "%" + tuKhoaHoatChat + "%";

            pst.setString(1, tk);
            pst.setString(2, tk);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Object[] {
                            rs.getString("id"),
                            rs.getString("ten"),
                            rs.getDouble("giaBan")
                    };
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public double[] layGiaNhapVaGiaBanTheoDonVi(String tenSP, String donVi) {
        double[] gia = new double[] { 0, 0 };

        String maSP = null;
        double giaBanCoBan = 0;
        double tiLe = 1;
        boolean laDonViQuyDoi = false;
        double giaBanDonVi = 0;

        String sqlSanPham = "SELECT sp.id, sp.donViDoCoBan, ISNULL(sp.giaBan,0) AS giaBanCoBan, " +
                "dv.chuyenDoiDonViCoBan, ISNULL(dv.gia,0) AS giaBanDonVi " +
                "FROM SanPham sp " +
                "LEFT JOIN DonViDoLuong dv ON sp.id = dv.sanPhamId AND UPPER(LTRIM(RTRIM(dv.ten))) = UPPER(LTRIM(RTRIM(?))) "
                +
                "WHERE UPPER(LTRIM(RTRIM(sp.ten))) = UPPER(LTRIM(RTRIM(?))) " +
                "AND ISNULL(sp.trangThai,'HOAT_DONG') != 'AN'";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sqlSanPham)) {

            pst.setString(1, donVi != null ? donVi : "");
            pst.setString(2, tenSP != null ? tenSP : "");

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    maSP = rs.getString("id");
                    giaBanCoBan = rs.getDouble("giaBanCoBan");

                    if (rs.getObject("chuyenDoiDonViCoBan") != null) {
                        tiLe = rs.getDouble("chuyenDoiDonViCoBan");
                        giaBanDonVi = rs.getDouble("giaBanDonVi");
                        laDonViQuyDoi = true;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (maSP != null) {
            gia[1] = (laDonViQuyDoi && giaBanDonVi > 0) ? giaBanDonVi : giaBanCoBan * tiLe;

            String sqlLo = "SELECT TOP 1 ISNULL(gia,0) AS giaNhapCoBan FROM LoHang " +
                    "WHERE sanPhamId = ? AND soLuongLoHang > 0 " +
                    "AND ISNULL(trangThai,'CON_HANG') = 'CON_HANG' " +
                    "AND (ngayHetHan IS NULL OR ngayHetHan >= GETDATE()) " +
                    "ORDER BY ngayNhap DESC";

            try (Connection con = ConnectDB.getInstance().getConnection();
                    PreparedStatement pst = con.prepareStatement(sqlLo)) {

                pst.setString(1, maSP);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next())
                        gia[0] = rs.getDouble("giaNhapCoBan") * tiLe;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return gia;
    }

    private String mapDanhMucToLabel(String db) {
        if (db == null)
            return "Khác";

        switch (db) {
            case "THUOC_KE_DON":
                return "Thuốc kê đơn";
            case "THUOC_KHONG_KE_DON":
                return "Thuốc không kê đơn";
            case "MY_PHAM":
                return "Mỹ phẩm";
            default:
                return "Sản phẩm chức năng";
        }
    }

    private String mapDangToLabel(String db) {
        if (db == null)
            return "Viên nén";

        switch (db) {
            case "VIEN_NANG":
                return "Viên nang";
            case "VIEN_SUI":
                return "Viên sủi";
            case "THUOC_BOT":
                return "Thuốc bột";
            case "KEO_NGAM":
                return "Kẹo ngậm";
            case "DUNG_DICH":
                return "Dung dịch";
            case "HON_DICH":
                return "Hỗn dịch";
            case "THUOC_NHO_GIOT":
                return "Thuốc nhỏ giọt";
            case "SUC_MIENG":
                return "Súc miệng";
            default:
                return "Viên nén";
        }
    }

    private SanPham mapSanPham(ResultSet rs) throws SQLException {
        SanPham sp = new SanPham();

        sp.setId(rs.getString("id"));

        if (rs.getString("danhMuc") != null) {
            sp.setDanhMuc(DanhMucSanPham.valueOf(rs.getString("danhMuc")));
        }

        try {
            if (rs.getString("dang") != null) {
                sp.setDang(DangBaoChe.valueOf(rs.getString("dang")));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("DangBaoChe lạ: " + rs.getString("dang"));
        }

        sp.setTen(rs.getString("ten"));
        sp.setTenVietTat(rs.getString("tenVietTat"));
        sp.setNhaSanXuat(rs.getString("nhaSanXuat"));
        sp.setHoatChat(rs.getString("hoatChat"));
        sp.setThueVAT(rs.getDouble("thueVAT"));
        sp.setHamLuong(rs.getString("hamLuong"));
        sp.setMoTa(rs.getString("moTa"));
        sp.setDonViDoCoBan(rs.getString("donViDoCoBan"));

        try {
            sp.setGiaBan(rs.getDouble("giaBan"));
        } catch (Exception ignored) {
        }

        if (rs.getTimestamp("ngayTao") != null) {
            sp.setNgayTao(rs.getTimestamp("ngayTao").toLocalDateTime());
        }

        try {
            sp.setMaVach(rs.getString("maVach"));
        } catch (Exception ignored) {
        }

        try {
            sp.setNhomBenhLy(rs.getString("nhomBenhLy"));
        } catch (Exception ignored) {
        }

        try {
            sp.setViTriId(rs.getString("viTriId"));
        } catch (Exception ignored) {
        }

        // Denormalized vị trí — join sẵn để UI không cần query thêm
        try { sp.setViTriKhu(rs.getString("viTriKhu"));  } catch (Exception ignored) {}
        try { sp.setViTriKe(rs.getString("viTriKe"));    } catch (Exception ignored) {}
        try { sp.setViTriTang(rs.getString("viTriTang")); } catch (Exception ignored) {}

        return sp;
    }
    public boolean kiemTraMaVachTonTai(String maVach, String maSPBoQua) {
        if (maVach == null || maVach.trim().isEmpty()) {
            return false;
        }

        // FIX: bọc chuỗi DB bằng dấu phẩy 2 đầu rồi tìm ",maVach,"
        // → tránh match nhầm "8934" trong "89340,89341"
        String sql = "SELECT COUNT(*) FROM SanPham WHERE "
                   + "(',' + ISNULL(maVach,'') + ',' LIKE ?) ";

        if (maSPBoQua != null && !maSPBoQua.trim().isEmpty()) {
            sql += " AND id != ?";
        }

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            // Pattern: "%,<maVach>,%" — khớp chính xác từng mã trong chuỗi CSV
            String pattern = "%," + maVach.trim() + ",%";
            pst.setString(1, pattern);

            if (maSPBoQua != null && !maSPBoQua.trim().isEmpty()) {
                pst.setString(2, maSPBoQua.trim());
            }

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================================
    // VỊ TRÍ THUỐC — gộp vào DAO_SanPham, dùng SanPham.ViTriThuoc (inner class)
    // =========================================================================

    /** Lấy toàn bộ vị trí, kèm số SP đang dùng */
    public List<SanPham.ViTriThuoc> layTatCaViTri() {
        List<SanPham.ViTriThuoc> ds = new ArrayList<>();
        String sql = "SELECT vt.id, vt.khu, vt.ke, ISNULL(vt.tang,'') tang, ISNULL(vt.moTa,'') moTa, " +
                     "COUNT(sp.id) soSP FROM ViTriThuoc vt " +
                     "LEFT JOIN SanPham sp ON sp.viTriId = vt.id AND ISNULL(sp.trangThai,'HOAT_DONG')='HOAT_DONG' " +
                     "GROUP BY vt.id, vt.khu, vt.ke, vt.tang, vt.moTa ORDER BY vt.khu, vt.ke, vt.tang";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                SanPham.ViTriThuoc vt = new SanPham.ViTriThuoc(
                    rs.getString("id"), rs.getString("khu"),
                    rs.getString("ke"),  rs.getString("tang"), rs.getString("moTa"));
                vt.setSoSanPham(rs.getInt("soSP"));
                ds.add(vt);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    /** Lấy 1 vị trí theo id */
    public SanPham.ViTriThuoc layViTriTheoId(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        String sql = "SELECT id, khu, ke, ISNULL(tang,'') tang, ISNULL(moTa,'') moTa FROM ViTriThuoc WHERE id=?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, id.trim());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return new SanPham.ViTriThuoc(
                    rs.getString("id"), rs.getString("khu"),
                    rs.getString("ke"),  rs.getString("tang"), rs.getString("moTa"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /** Lấy danh sách khu phân biệt — cho filter ComboBox */
    public List<String> layDanhSachKhuViTri() {
        List<String> ds = new ArrayList<>();
        String sql = "SELECT DISTINCT khu FROM ViTriThuoc ORDER BY khu";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) ds.add(rs.getString("khu"));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    /** Thêm vị trí mới */
    public boolean themViTri(SanPham.ViTriThuoc vt) {
        if (vt == null || vt.getId() == null || vt.getKhu() == null || vt.getKe() == null) return false;
        String sql = "INSERT INTO ViTriThuoc (id, khu, ke, tang, moTa) VALUES (?,?,?,?,?)";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, vt.getId().trim());
            pst.setString(2, vt.getKhu().trim());
            pst.setString(3, vt.getKe().trim());
            pst.setString(4, (vt.getTang() == null || vt.getTang().isBlank()) ? null : vt.getTang().trim());
            pst.setString(5, (vt.getMoTa() == null || vt.getMoTa().isBlank()) ? null : vt.getMoTa().trim());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Cập nhật vị trí */
    public boolean capNhatViTri(SanPham.ViTriThuoc vt) {
        if (vt == null || vt.getId() == null) return false;
        String sql = "UPDATE ViTriThuoc SET khu=?, ke=?, tang=?, moTa=? WHERE id=?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, vt.getKhu().trim());
            pst.setString(2, vt.getKe().trim());
            pst.setString(3, (vt.getTang() == null || vt.getTang().isBlank()) ? null : vt.getTang().trim());
            pst.setString(4, (vt.getMoTa() == null || vt.getMoTa().isBlank()) ? null : vt.getMoTa().trim());
            pst.setString(5, vt.getId().trim());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * Xóa vị trí — chỉ xóa được nếu không còn SP nào dùng.
     * Trả về: 0 = xóa thành công, -1 = còn SP đang dùng, -2 = lỗi
     */
    public int xoaViTri(String id) {
        if (id == null || id.trim().isEmpty()) return -2;
        String sqlCheck = "SELECT COUNT(*) FROM SanPham WHERE viTriId=? AND ISNULL(trangThai,'HOAT_DONG')='HOAT_DONG'";
        try (Connection con = ConnectDB.getInstance().getConnection()) {
            try (PreparedStatement chk = con.prepareStatement(sqlCheck)) {
                chk.setString(1, id.trim());
                ResultSet rs = chk.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) return -1; // còn SP đang dùng
            }
            try (PreparedStatement del = con.prepareStatement("DELETE FROM ViTriThuoc WHERE id=?")) {
                del.setString(1, id.trim());
                return del.executeUpdate() > 0 ? 0 : -2;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -2;
    }

    /** Gán viTriId cho sản phẩm (null = bỏ gán) */
    public boolean ganViTriChoSP(String maSP, String viTriId) {
        if (maSP == null || maSP.trim().isEmpty()) return false;
        String sql = "UPDATE SanPham SET viTriId=? WHERE id=?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            if (viTriId == null || viTriId.trim().isEmpty())
                pst.setNull(1, java.sql.Types.NVARCHAR);
            else
                pst.setString(1, viTriId.trim());
            pst.setString(2, maSP.trim());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /** Sinh ID vị trí mới theo format VT-xxx */
    public String layIdViTriMoi() {
        String sql = "SELECT TOP 1 id FROM ViTriThuoc ORDER BY id DESC";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                int num = Integer.parseInt(rs.getString("id").replaceAll("[^0-9]", "")) + 1;
                return String.format("VT-%03d", num);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "VT-001";
    }

    // =========================================================================
    // MẪU LIỀU DÙNG — gộp vào DAO_SanPham
    // Cấu trúc Object[] trả về:
    //   [0] id, [1] tenMau, [2] doiTuong, [3] lieuLuong, [4] donViLieu,
    //   [5] soLanNgay, [6] thoiDiemUong, [7] duongDung,
    //   [8] chongChiDinh, [9] luuY, [10] laMacDinh(Boolean)
    // =========================================================================

    /** Lấy tất cả mẫu liều của 1 SP, mặc định lên trước */
    public List<Object[]> layMauLieuTheoSP(String maSP) {
        List<Object[]> ds = new ArrayList<>();
        if (maSP == null || maSP.trim().isEmpty()) return ds;
        String sql = "SELECT id, ISNULL(tenMau,'') tenMau, doiTuong, lieuLuong, donViLieu, " +
                     "soLanNgay, thoiDiemUong, duongDung, " +
                     "ISNULL(chongChiDinh,'') chongChiDinh, ISNULL(luuY,'') luuY, laMacDinh " +
                     "FROM MauLieuDung WHERE sanPhamId=? ORDER BY laMacDinh DESC, id";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maSP.trim());
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ds.add(new Object[]{
                        rs.getString("id"),
                        rs.getString("tenMau"),
                        rs.getString("doiTuong"),
                        rs.getString("lieuLuong"),
                        rs.getString("donViLieu"),
                        rs.getInt("soLanNgay"),
                        rs.getString("thoiDiemUong"),
                        rs.getString("duongDung"),
                        rs.getString("chongChiDinh"),
                        rs.getString("luuY"),
                        rs.getBoolean("laMacDinh")
                    });
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    /** Lấy mẫu liều mặc định — dùng tại màn hình bán hàng để gợi ý nhanh */
    public Object[] layMauLieuMacDinh(String maSP) {
        List<Object[]> ds = layMauLieuTheoSP(maSP);
        for (Object[] m : ds) if (Boolean.TRUE.equals(m[10])) return m;
        return ds.isEmpty() ? null : ds.get(0); // fallback: mẫu đầu tiên
    }

    /**
     * Lưu toàn bộ mẫu liều — xóa cũ rồi insert batch mới.
     * Gọi sau khi lưu sản phẩm thành công.
     * @param dsMau  mỗi phần tử: [tenMau, doiTuong, lieuLuong, donViLieu, soLanNgay,
     *                             thoiDiemUong, duongDung, chongChiDinh, luuY, laMacDinh]
     */
    public boolean luuMauLieu(String maSP, List<Object[]> dsMau) {
        if (maSP == null || maSP.trim().isEmpty()) return false;
        String del = "DELETE FROM MauLieuDung WHERE sanPhamId=?";
        String ins = "INSERT INTO MauLieuDung " +
                     "(id, sanPhamId, tenMau, doiTuong, lieuLuong, donViLieu, soLanNgay, " +
                     "thoiDiemUong, duongDung, chongChiDinh, luuY, laMacDinh) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = ConnectDB.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement pDel = con.prepareStatement(del)) {
                    pDel.setString(1, maSP.trim()); pDel.executeUpdate();
                }
                if (dsMau != null && !dsMau.isEmpty()) {
                    try (PreparedStatement pIns = con.prepareStatement(ins)) {
                        for (int i = 0; i < dsMau.size(); i++) {
                            Object[] m = dsMau.get(i);
                            pIns.setString(1, "ML-" + maSP.trim() + "-" + String.format("%03d", i + 1));
                            pIns.setString(2, maSP.trim());
                            pIns.setString(3, m[0] != null ? m[0].toString() : "");
                            pIns.setString(4, m[1].toString());
                            pIns.setString(5, m[2] != null ? m[2].toString().trim() : "");
                            pIns.setString(6, m[3].toString());
                            pIns.setInt(7, Integer.parseInt(m[4].toString()));
                            pIns.setString(8, m[5].toString());
                            pIns.setString(9, m[6].toString());
                            pIns.setString(10, m[7] != null ? m[7].toString() : "");
                            pIns.setString(11, m[8] != null ? m[8].toString() : "");
                            pIns.setBoolean(12, Boolean.TRUE.equals(m[9]));
                            pIns.addBatch();
                        }
                        pIns.executeBatch();
                    }
                }
                con.commit();
                return true;
            } catch (SQLException ex) {
                con.rollback(); ex.printStackTrace();
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}