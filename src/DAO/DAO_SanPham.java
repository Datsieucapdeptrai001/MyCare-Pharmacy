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

        // FIX #5: thêm tenVietTat để doSearch() lọc được theo tên viết tắt (index [10])
        String sql = "SELECT id, ten, danhMuc, ISNULL(hoatChat,'') AS hoatChat, dang, " +
                "ISNULL(nhaSanXuat,'Khác') AS nhaSanXuat, ISNULL(thueVAT,0) AS thueVAT, " +
                "ISNULL(giaBan,0) AS giaBan, " +
                "ISNULL(maVach,'') AS maVach, " +
                "ISNULL(nhomBenhLy,'') AS nhomBenhLy, " +
                "ISNULL(tenVietTat,'') AS tenVietTat " +
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
                        rs.getString("nhomBenhLy"),
                        rs.getString("tenVietTat")  // [10] — dùng cho doSearch() lọc tên viết tắt
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

            System.out.println("=== DEBUG DAO_SanPham.capNhatMaVachSanPham ===");
            System.out.println("maSP = [" + maSP + "]");
            System.out.println("maVachMoi = [" + maVachMoi + "]");

            pst.setString(1, maVachMoi.trim());
            pst.setString(2, maSP.trim());

            int row = pst.executeUpdate();

            System.out.println("row update SanPham.maVach = " + row);

            return row > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
                    } catch (Exception ignored) {
                    }

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
                    } catch (Exception ignored) {
                    }

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

                    try {
                        sp.setMaVach(rs.getString("maVach"));
                    } catch (Exception ignored) {
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
                try (PreparedStatement chk = con.prepareStatement(
                        "SELECT COUNT(*) FROM ChiTietHoaDon WHERE donViDoLuongId = ?")) {
                    chk.setString(1, idCu);
                    try (ResultSet rs = chk.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            continue;
                        }
                    }
                }
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
        if (db == null) return "Khác";
        switch (db) {
            case "THUOC_KE_DON":        return "Thuốc kê đơn";
            case "THUOC_KHONG_KE_DON":  return "Thuốc không kê đơn";
            case "MY_PHAM":             return "Mỹ phẩm";
            case "THUC_PHAM_CHUC_NANG": return "Sản phẩm chức năng"; // ← thêm dòng này
            default:                    return "Khác";
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
        String sqlSP = "SELECT COUNT(*) FROM SanPham WHERE "
                     + "(',' + ISNULL(maVach,'') + ',' LIKE ?) ";

        if (maSPBoQua != null && !maSPBoQua.trim().isEmpty()) {
            sqlSP += " AND id != ?";
        }

        // Bug3-fix: kiểm tra thêm trong LoHang.maVachNoiBo
        // Nếu maSPBoQua không rỗng thì bỏ qua các lô thuộc SP đó
        String sqlLo = "SELECT COUNT(*) FROM LoHang WHERE maVachNoiBo = ?";
        if (maSPBoQua != null && !maSPBoQua.trim().isEmpty()) {
            sqlLo += " AND sanPhamId != ?";
        }

        try (Connection con = ConnectDB.getInstance().getConnection()) {
            // --- Check SanPham.maVach ---
            try (PreparedStatement pst = con.prepareStatement(sqlSP)) {
                String pattern = "%," + maVach.trim() + ",%";
                pst.setString(1, pattern);
                if (maSPBoQua != null && !maSPBoQua.trim().isEmpty()) {
                    pst.setString(2, maSPBoQua.trim());
                }
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return true;
                }
            }
            // --- Bug3-fix: Check LoHang.maVachNoiBo ---
            try (PreparedStatement pst2 = con.prepareStatement(sqlLo)) {
                pst2.setString(1, maVach.trim());
                if (maSPBoQua != null && !maSPBoQua.trim().isEmpty()) {
                    pst2.setString(2, maSPBoQua.trim());
                }
                try (ResultSet rs2 = pst2.executeQuery()) {
                    if (rs2.next() && rs2.getInt(1) > 0) return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // FIX #1/#9: method tìm kiếm riêng cho mẫu liều — KHÔNG lọc theo tồn kho
    // Lý do: timKiemSanPhamBan dùng JOIN LoHang → thuốc chưa có lô hoặc hết kho
    // sẽ không bao giờ xuất hiện trong ô tìm thuốc cắt liều.
    // Mẫu liều là template, không cần tồn kho thực tế.
    public List<Object[]> timKiemThuocChoMauLieu(String text) {
        List<Object[]> ds = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) return ds;
        String sql =
            "SELECT DISTINCT sp.id, sp.ten, " +
            "  ISNULL(sp.donViDoCoBan, 'Viên') AS donViHienThi, " +
            "  ISNULL(sp.giaBan, 0)             AS giaHienThi, " +
            "  ISNULL(sp.thueVAT, 0)            AS thueVAT, " +
            "  ISNULL(sp.danhMuc, '')            AS danhMuc " +
            "FROM SanPham sp " +
            "WHERE ISNULL(sp.trangThai,'HOAT_DONG') != 'AN' " +
            "  AND (sp.ten LIKE ? OR sp.tenVietTat LIKE ? OR sp.hoatChat LIKE ? " +
            "       OR sp.id LIKE ? " +
            "       OR ',' + ISNULL(sp.maVach,'') + ',' LIKE ?) " +
            "ORDER BY sp.ten ASC";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            String p    = "%" + text.trim() + "%";
            String pMv  = "%," + text.trim() + ",%";
            pst.setString(1, p); pst.setString(2, p); pst.setString(3, p);
            pst.setString(4, p); pst.setString(5, pMv);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ds.add(new Object[]{
                        rs.getString("id"),
                        rs.getString("ten"),
                        rs.getString("donViHienThi"),
                        rs.getDouble("giaHienThi"),
                        0,   // tồn kho — không cần cho mẫu liều
                        mapDanhMucToLabel(rs.getString("danhMuc")),
                        rs.getDouble("thueVAT")
                    });
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
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
    // THAY THẾ TOÀN BỘ CHỨC NĂNG CẮT LIỀU NÂNG CAO (TỪ DAO_MAULIEU)
    // =========================================================================
    public Object[] getDonViNhoNhat(String maSP) {
        if (maSP == null || maSP.trim().isEmpty()) return null;
        String sql = "SELECT TOP 1 ten, ISNULL(gia, 0) AS gia FROM DonViDoLuong WHERE sanPhamId = ? AND chuyenDoiDonViCoBan = 1";
        try (Connection con = ConnectDB.getInstance().getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maSP.trim());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return new Object[]{rs.getString("ten"), rs.getDouble("gia")};
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public SanPham getSanPhamByBarcode(String maVach) {
        if (maVach == null || maVach.trim().isEmpty()) return null;
        String sql = "SELECT DISTINCT sp.id, sp.ten, sp.donViDoCoBan, ISNULL(sp.giaBan,0) AS giaBan, ISNULL(sp.thueVAT,0) AS thueVAT, ISNULL(sp.nhomBenhLy,'') AS nhomBenhLy FROM SanPham sp LEFT JOIN DonViDoLuong dv ON sp.id = dv.sanPhamId WHERE ISNULL(sp.trangThai,'HOAT_DONG') != 'AN' AND (dv.maVach = ? OR sp.id = ? OR ',' + ISNULL(sp.maVach,'') + ',' LIKE ?)";
        try (Connection con = ConnectDB.getInstance().getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            // FIX #4: phải có dấu phẩy cả 2 đầu — tránh "123" khớp nhầm "123456"
            pst.setString(1, maVach.trim()); pst.setString(2, maVach.trim()); pst.setString(3, "%," + maVach.trim() + ",%");
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    SanPham sp = new SanPham(); sp.setId(rs.getString("id")); sp.setTen(rs.getString("ten")); sp.setDonViDoCoBan(rs.getString("donViDoCoBan")); sp.setGiaBan(rs.getDouble("giaBan")); sp.setThueVAT(rs.getDouble("thueVAT")); sp.setNhomBenhLy(rs.getString("nhomBenhLy")); return sp;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Object[]> layDanhSachComboNangCao() {
        List<Object[]> ds = new ArrayList<>();
        String sql = "SELECT m.comboId, m.tenCombo, ISNULL(m.nhomBenh,'') AS nhomBenh, ISNULL(m.giaBanCombo,0) AS giaBanCombo, ISNULL(m.ghiChu,'') AS ghiChu, (SELECT COUNT(*) FROM ChiTietMauLieu c WHERE c.comboId = m.comboId) AS soChiTiet FROM MauLieu m ORDER BY m.tenCombo ASC";
        try (Connection con = ConnectDB.getInstance().getConnection(); PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                ds.add(new Object[]{ rs.getString("comboId"), rs.getString("tenCombo"), rs.getString("nhomBenh"), rs.getDouble("giaBanCombo"), rs.getString("ghiChu"), rs.getInt("soChiTiet") });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public List<String> layDanhSachNhomBenhLieu() {
        List<String> ds = new ArrayList<>(List.of("Hô hấp", "Tiêu hóa", "Cơ xương khớp", "Tim mạch", "Da liễu", "Khác"));
        String sql = "SELECT DISTINCT nhomBenh FROM MauLieu WHERE nhomBenh IS NOT NULL AND nhomBenh != ''";
        try (Connection con = ConnectDB.getInstance().getConnection(); PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                String nb = rs.getString("nhomBenh"); if (!ds.contains(nb)) ds.add(nb);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public SanPham.MauLieu layComboByIdNangCao(String comboId) {
        // FIX #2: tách thành 2 bước riêng — đóng RS trước, rồi mới mở statement thứ hai
        // Lý do: SQL Server JDBC tắt MARS theo mặc định → 2 ResultSet mở đồng thời trên
        // cùng 1 connection → SQLServerException "The result set is closed"
        String sql = "SELECT comboId, tenCombo, ISNULL(nhomBenh,'') AS nhomBenh, ISNULL(giaBanCombo,0) AS giaBanCombo, ISNULL(m.ghiChu,'') AS ghiChu FROM MauLieu m WHERE comboId = ?";
        SanPham.MauLieu result = null;
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, comboId);
            // Bước 1: lấy header — đóng RS bằng try-with-resources
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    result = new SanPham.MauLieu(
                        rs.getString("comboId"),
                        rs.getString("tenCombo"),
                        rs.getString("nhomBenh"),
                        rs.getDouble("giaBanCombo"),
                        rs.getString("ghiChu")
                    );
                }
            } // RS đóng hoàn toàn tại đây
            // Bước 2: bây giờ mới an toàn mở statement thứ hai trên cùng connection
            if (result != null) {
                result.setDsChiTiet(layChiTietTheoComboNangCao(comboId, con));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    private List<SanPham.ChiTietLieu> layChiTietTheoComboNangCao(String comboId, Connection con) throws SQLException {
        List<SanPham.ChiTietLieu> ds = new ArrayList<>();
        String sql = "SELECT c.id, c.comboId, c.sanPhamId, sp.ten AS tenSanPham, c.dvt, c.sang, c.trua, c.chieu, c.toi, ISNULL(c.cachDung,'') AS cachDung, c.soNgay, c.tongSoLuong, c.giaDonVi FROM ChiTietMauLieu c JOIN SanPham sp ON c.sanPhamId = sp.id WHERE c.comboId = ? ORDER BY c.id ASC";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, comboId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    SanPham.ChiTietLieu ct = new SanPham.ChiTietLieu(rs.getString("comboId"), rs.getString("sanPhamId"), rs.getString("tenSanPham"), rs.getString("dvt"), rs.getDouble("sang"), rs.getDouble("trua"), rs.getDouble("chieu"), rs.getDouble("toi"), rs.getString("cachDung"), rs.getInt("soNgay"), rs.getDouble("giaDonVi"));
                    ct.setId(rs.getInt("id")); ds.add(ct);
                }
            }
        }
        return ds;
    }

    public boolean taoMauMoiNangCao(SanPham.MauLieu combo, List<SanPham.ChiTietLieu> dsChiTiet) {
        try (Connection con = ConnectDB.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try {
                String sqlIns = "INSERT INTO MauLieu(comboId, tenCombo, nhomBenh, giaBanCombo, ghiChu, ngayTao) VALUES(?,?,?,?,?,GETDATE())";
                try (PreparedStatement pst = con.prepareStatement(sqlIns)) {
                    pst.setString(1, combo.getComboId()); pst.setString(2, combo.getTenCombo()); pst.setString(3, combo.getNhomBenh()); pst.setDouble(4, combo.getGiaBanCombo()); pst.setString(5, combo.getGhiChu()); pst.executeUpdate();
                }
                insertChiTietNangCao(con, combo.getComboId(), dsChiTiet); con.commit(); return true;
            } catch (SQLException ex) { con.rollback(); ex.printStackTrace(); }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean capNhatMauNangCao(SanPham.MauLieu combo, List<SanPham.ChiTietLieu> dsChiTiet) {
        try (Connection con = ConnectDB.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try {
                String sqlUpd = "UPDATE MauLieu SET tenCombo=?, nhomBenh=?, giaBanCombo=?, ghiChu=?, ngaySua=GETDATE() WHERE comboId=?";
                try (PreparedStatement pst = con.prepareStatement(sqlUpd)) {
                    pst.setString(1, combo.getTenCombo()); pst.setString(2, combo.getNhomBenh()); pst.setDouble(3, combo.getGiaBanCombo()); pst.setString(4, combo.getGhiChu()); pst.setString(5, combo.getComboId()); pst.executeUpdate();
                }
                try (PreparedStatement del = con.prepareStatement("DELETE FROM ChiTietMauLieu WHERE comboId=?")) { del.setString(1, combo.getComboId()); del.executeUpdate(); }
                insertChiTietNangCao(con, combo.getComboId(), dsChiTiet); con.commit(); return true;
            } catch (SQLException ex) { con.rollback(); ex.printStackTrace(); }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    private void insertChiTietNangCao(Connection con, String comboId, List<SanPham.ChiTietLieu> ds) throws SQLException {
        String insSql = "INSERT INTO ChiTietMauLieu (comboId, sanPhamId, dvt, sang, trua, chieu, toi, cachDung, soNgay, tongSoLuong, giaDonVi) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ins = con.prepareStatement(insSql)) {
            for (SanPham.ChiTietLieu ct : ds) {
                ins.setString(1, comboId); ins.setString(2, ct.getSanPhamId()); ins.setString(3, ct.getDvt());
                ins.setDouble(4, ct.getSang()); ins.setDouble(5, ct.getTrua()); ins.setDouble(6, ct.getChieu()); ins.setDouble(7, ct.getToi());
                ins.setString(8, ct.getCachDung()); ins.setInt(9, ct.getSoNgay()); ins.setInt(10, ct.getTongSoLuong()); ins.setDouble(11, ct.getGiaDonVi()); ins.addBatch();
            }
            ins.executeBatch();
        }
    }

    public boolean xoaMauNangCao(String comboId) {
        // FIX #3: dùng transaction + xóa ChiTietMauLieu trước, sau đó mới xóa MauLieu
        // Lý do: nếu có FK constraint (hoặc không bật CASCADE DELETE) thì DELETE MauLieu
        // sẽ ném lỗi FK violation — dữ liệu con bị để lại thành orphan records.
        try (Connection con = ConnectDB.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try {
                // Bước 1: xóa chi tiết trước
                try (PreparedStatement del1 = con.prepareStatement(
                        "DELETE FROM ChiTietMauLieu WHERE comboId = ?")) {
                    del1.setString(1, comboId);
                    del1.executeUpdate();
                }
                // Bước 2: xóa header
                int rows;
                try (PreparedStatement del2 = con.prepareStatement(
                        "DELETE FROM MauLieu WHERE comboId = ?")) {
                    del2.setString(1, comboId);
                    rows = del2.executeUpdate();
                }
                con.commit();
                return rows > 0;
            } catch (SQLException ex) {
                con.rollback();
                ex.printStackTrace();
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public String sinhComboIdMoi() {
        String today = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        // FIX #10: dùng PreparedStatement thay vì string concat trực tiếp vào SQL
        String sql = "SELECT MAX(comboId) FROM MauLieu WHERE comboId LIKE ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, "CB-" + today + "-%");
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next() && rs.getString(1) != null) {
                    String[] parts = rs.getString(1).split("-");
                    if (parts.length >= 3) {
                        int seq = Integer.parseInt(parts[2]) + 1;
                        return String.format("CB-%s-%04d", today, seq);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return String.format("CB-%s-0001", today);
    }
    public boolean kiemTraTenComboTonTai(String tenCombo) {
        String sql = "SELECT COUNT(*) FROM MauLieu WHERE LOWER(TenCombo) = LOWER(?)";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tenCombo.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
}