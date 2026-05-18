package DAO;

import ConnectDB.ConnectDB;
import Entity.ChiTietPhieuNhapHang;
import Entity.KhoHang;
import Entity.LoHang;
import Entity.PhieuNhapHang;
import Entity.SanPham;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class DAO_PhieuNhapHang {

    public DAO_PhieuNhapHang() {
    }

    public String taoMaPhieuNhapMoi() {
        String sql = "SELECT TOP 1 id FROM PhieuNhapHang " +
                "WHERE id LIKE 'PN-%' " +
                "ORDER BY TRY_CAST(SUBSTRING(id, 4, LEN(id)) AS INT) DESC";

        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                String lastId = rs.getString("id");

                try {
                    int so = Integer.parseInt(lastId.substring(3));
                    return String.format("PN-%04d", so + 1);
                } catch (Exception ignored) {
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "PN-0001";
    }

    public String taoMaChiTietMoi() {
        String sql = "SELECT TOP 1 id FROM ChiTietPhieuNhapHang " +
                "WHERE id LIKE 'CTPN-%' " +
                "ORDER BY TRY_CAST(SUBSTRING(id, 6, LEN(id)) AS INT) DESC";

        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                String lastId = rs.getString("id");

                try {
                    int so = Integer.parseInt(lastId.substring(5));
                    return String.format("CTPN-%04d", so + 1);
                } catch (Exception ignored) {
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "CTPN-0001";
    }

    public boolean themPhieuNhap(PhieuNhapHang phieu) {
        String sql = "INSERT INTO PhieuNhapHang " +
                "(id, ngayNhap, nhaCungCapId, nhanVienId, tongTien, ghiChu, trangThai) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, phieu.getId());
            pst.setTimestamp(2, java.sql.Timestamp.valueOf(phieu.getNgayNhap()));
            pst.setString(3, phieu.getNhaCungCapId());
            pst.setString(4, phieu.getNhanVienId());
            pst.setDouble(5, phieu.getTongTien());
            pst.setString(6, phieu.getGhiChu());
            pst.setString(7, phieu.getTrangThai());

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean themChiTietPhieuNhap(ChiTietPhieuNhapHang ct) {
        String sql = "INSERT INTO ChiTietPhieuNhapHang " +
                "(id, phieuNhapId, loHangId, sanPhamId, khoHangId, soLoHang, soLuongNhap, donGiaNhap, thanhTien, hanSuDung) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, ct.getId());
            pst.setString(2, ct.getPhieuNhapId().getId());
            pst.setString(3, ct.getLoHangId().getId());
            pst.setString(4, ct.getSanPhamId().getId());
            pst.setString(5, ct.getKhoHangId().getId());
            pst.setString(6, ct.getSoLoHang());
            pst.setInt(7, ct.getSoLuongNhap());
            pst.setDouble(8, ct.getDonGiaNhap());
            pst.setDouble(9, ct.getThanhTien());

            if (ct.getHanSuDung() == null) {
                pst.setNull(10, Types.TIMESTAMP);
            } else {
                pst.setTimestamp(10, java.sql.Timestamp.valueOf(ct.getHanSuDung()));
            }

            return pst.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean lapPhieuNhapTuLoHang(LoHang loHang, String nhanVienId, String nhaCungCapId, String ghiChu) {
        if (loHang == null) {
            return false;
        }

        Connection con = ConnectDB.getInstance().getConnection();

        try {
            con.setAutoCommit(false);

            String maPN = taoMaPhieuNhapMoi();
            String maCTPN = taoMaChiTietMoi();

            double tongTien = loHang.getSoLuongLoHang() * loHang.getGia();

            String sqlPN = "INSERT INTO PhieuNhapHang " +
                    "(id, ngayNhap, nhaCungCapId, nhanVienId, tongTien, ghiChu, trangThai) " +
                    "VALUES (?, GETDATE(), ?, ?, ?, ?, ?)";

            try (PreparedStatement pst = con.prepareStatement(sqlPN)) {
                pst.setString(1, maPN);
                pst.setString(2, nhaCungCapId);
                pst.setString(3, nhanVienId);
                pst.setDouble(4, tongTien);
                pst.setString(5, ghiChu);
                pst.setString(6, "HOAN_THANH");

                if (pst.executeUpdate() <= 0) {
                    con.rollback();
                    return false;
                }
            }

            String sqlCT = "INSERT INTO ChiTietPhieuNhapHang " +
                    "(id, phieuNhapId, loHangId, sanPhamId, khoHangId, soLoHang, soLuongNhap, donGiaNhap, thanhTien, hanSuDung) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pst = con.prepareStatement(sqlCT)) {
                pst.setString(1, maCTPN);
                pst.setString(2, maPN);
                pst.setString(3, loHang.getId());

                if (loHang.getSanPhamId() == null) {
                    pst.setNull(4, Types.NVARCHAR);
                } else {
                    pst.setString(4, loHang.getSanPhamId().getId());
                }

                if (loHang.getKhoHangId() == null) {
                    pst.setNull(5, Types.NVARCHAR);
                } else {
                    pst.setString(5, loHang.getKhoHangId().getId());
                }

                pst.setString(6, loHang.getSoLoHang());
                pst.setInt(7, loHang.getSoLuongLoHang());
                pst.setDouble(8, loHang.getGia());
                pst.setDouble(9, tongTien);

                if (loHang.getNgayHetHan() == null) {
                    pst.setNull(10, Types.TIMESTAMP);
                } else {
                    pst.setTimestamp(10, java.sql.Timestamp.valueOf(loHang.getNgayHetHan()));
                }

                if (pst.executeUpdate() <= 0) {
                    con.rollback();
                    return false;
                }
            }

            con.commit();
            return true;

        } catch (Exception e) {
            try {
                con.rollback();
            } catch (Exception ignored) {
            }

            e.printStackTrace();
            return false;

        } finally {
            try {
                con.setAutoCommit(true);
            } catch (Exception ignored) {
            }
        }
    }

    public List<PhieuNhapHang> layDanhSachPhieuNhap() {
        List<PhieuNhapHang> ds = new ArrayList<>();

        String sql = "SELECT * FROM PhieuNhapHang ORDER BY ngayNhap DESC";

        Connection con = ConnectDB.getInstance().getConnection();

        try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                PhieuNhapHang pn = new PhieuNhapHang();

                pn.setId(rs.getString("id"));

                if (rs.getTimestamp("ngayNhap") != null) {
                    pn.setNgayNhap(rs.getTimestamp("ngayNhap").toLocalDateTime());
                }

                pn.setNhaCungCapId(rs.getString("nhaCungCapId"));
                pn.setNhanVienId(rs.getString("nhanVienId"));
                pn.setTongTien(rs.getDouble("tongTien"));
                pn.setGhiChu(rs.getString("ghiChu"));
                pn.setTrangThai(rs.getString("trangThai"));

                ds.add(pn);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }

    public List<ChiTietPhieuNhapHang> layChiTietTheoPhieuNhap(String maPN) {
        List<ChiTietPhieuNhapHang> ds = new ArrayList<>();

        String sql = "SELECT * FROM ChiTietPhieuNhapHang " +
                "WHERE phieuNhapId = ? " +
                "ORDER BY id ASC";

        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maPN);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ChiTietPhieuNhapHang ct = new ChiTietPhieuNhapHang();

                    ct.setId(rs.getString("id"));

                    PhieuNhapHang pn = new PhieuNhapHang();
                    pn.setId(rs.getString("phieuNhapId"));
                    ct.setPhieuNhapId(pn);

                    LoHang lh = new LoHang();
                    lh.setId(rs.getString("loHangId"));
                    ct.setLoHangId(lh);

                    SanPham sp = new SanPham();
                    sp.setId(rs.getString("sanPhamId"));
                    ct.setSanPhamId(sp);

                    KhoHang kho = new KhoHang();
                    kho.setId(rs.getString("khoHangId"));
                    ct.setKhoHangId(kho);

                    ct.setSoLoHang(rs.getString("soLoHang"));
                    ct.setSoLuongNhap(rs.getInt("soLuongNhap"));
                    ct.setDonGiaNhap(rs.getDouble("donGiaNhap"));
                    ct.setThanhTien(rs.getDouble("thanhTien"));

                    if (rs.getTimestamp("hanSuDung") != null) {
                        ct.setHanSuDung(rs.getTimestamp("hanSuDung").toLocalDateTime());
                    }

                    ds.add(ct);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }

    public List<Object[]> layNhatKyLoHang() {
        List<Object[]> ds = new ArrayList<>();

        String sql = "SELECT " +
                "   pn.ngayNhap AS thoiGian, " +
                "   pn.id AS maPhieuLo, " +
                "   N'Nhập kho' AS hanhDong, " +
                "   ct.soLoHang AS maLo, " +
                "   ISNULL(sp.ten, ct.sanPhamId) AS tenSanPham, " +
                "   ct.soLuongNhap AS soLuongNhap, " +
                "   ct.donGiaNhap AS donGiaNhap, " +
                "   ct.thanhTien AS thanhTien, " +
                "   ISNULL(nv.hoVaTen, ISNULL(pn.nhanVienId, N'Không xác định')) AS nguoiThucHien " +
                "FROM PhieuNhapHang pn " +
                "JOIN ChiTietPhieuNhapHang ct ON pn.id = ct.phieuNhapId " +
                "LEFT JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                "LEFT JOIN NhanVien nv ON pn.nhanVienId = nv.id " +
                "ORDER BY pn.ngayNhap DESC";

        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                ds.add(new Object[] {
                        rs.getTimestamp("thoiGian"),
                        rs.getString("maPhieuLo"),
                        rs.getString("hanhDong"),
                        rs.getString("maLo"),
                        rs.getString("tenSanPham"),
                        rs.getInt("soLuongNhap"),
                        rs.getDouble("donGiaNhap"),
                        rs.getDouble("thanhTien"),
                        rs.getString("nguoiThucHien")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }
}