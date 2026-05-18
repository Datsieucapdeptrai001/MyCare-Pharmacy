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
        if (kiemTraMaSPTonTai(id))
            return false;

        String sql = "INSERT INTO SanPham (id, danhMuc, dang, ten, tenVietTat, nhaSanXuat, hoatChat, " +
                "thueVAT, hamLuong, moTa, donViDoCoBan, giaBan, ngayTao, maVach, nhomBenhLy) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,GETDATE(),?,?)";

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
        String sql = "UPDATE SanPham SET danhMuc=?,dang=?,ten=?,tenVietTat=?,nhaSanXuat=?," +
                "hoatChat=?,thueVAT=?,hamLuong=?,moTa=?,donViDoCoBan=?,giaBan=?," +
                "maVach=?,nhomBenhLy=? " +
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

            pst.setString(14, id);

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

        // ĐÃ FIX: Sửa lại maVach = ? thành maVach LIKE ? ở cả SELECT phụ và WHERE
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
                "       OR sp.maVach LIKE ?) " +
                "AND ISNULL(lh.trangThai,'CON_HANG') = 'CON_HANG' " +
                "AND lh.soLuongLoHang > 0 " +
                "AND (lh.ngayHetHan IS NULL OR lh.ngayHetHan >= GETDATE()) " +
                "ORDER BY lh.ngayHetHan ASC";

        try (Connection con = ConnectDB.getInstance().getConnection();
                PreparedStatement pst = con.prepareStatement(sql)) {

            // ĐÃ FIX: Dùng biến p (chứa %...%) cho tất cả các cột cần quét (trừ mã QR nội bộ)
            String p = "%" + text + "%";

            pst.setString(1, p); // Tìm mã vạch quy đổi đơn vị hiển thị
            pst.setString(2, p); // Tìm mã vạch quy đổi giá tiền
            pst.setString(3, p); // sp.ten
            pst.setString(4, p); // sp.tenVietTat
            pst.setString(5, p); // sp.hoatChat
            pst.setString(6, p); // sp.id
            pst.setString(7, p); // lh.soLoHang
            pst.setString(8, p); // dv.maVach (Mã vạch Đơn vị tính)
            
            // Riêng mã QR nội bộ thường cần khớp chính xác tuyệt đối nên giữ nguyên text
            pst.setString(9, text); 
            
            pst.setString(10, p); // sp.maVach (Mã vạch sản phẩm chung)

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

        return sp;
    }
}