package DAO;

import ConnectDB.ConnectDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_LichSuKiemKeKho {

    public static class PhieuKiemKeHistory {
        private final String id;
        private final Timestamp ngayKiemKe;
        private final String khoHangId;
        private final String nhanVienId;
        private final String tenNhanVien;
        private final int tongSoDong;
        private final int tongChenhLech;
        private final String ghiChu;
        private final String trangThai;

        public PhieuKiemKeHistory(
                String id,
                Timestamp ngayKiemKe,
                String khoHangId,
                String nhanVienId,
                String tenNhanVien,
                int tongSoDong,
                int tongChenhLech,
                String ghiChu,
                String trangThai
        ) {
            this.id = id;
            this.ngayKiemKe = ngayKiemKe;
            this.khoHangId = khoHangId;
            this.nhanVienId = nhanVienId;
            this.tenNhanVien = tenNhanVien;
            this.tongSoDong = tongSoDong;
            this.tongChenhLech = tongChenhLech;
            this.ghiChu = ghiChu;
            this.trangThai = trangThai;
        }

        public String getId() { return id; }
        public Timestamp getNgayKiemKe() { return ngayKiemKe; }
        public String getKhoHangId() { return khoHangId; }
        public String getNhanVienId() { return nhanVienId; }
        public String getTenNhanVien() { return tenNhanVien; }
        public int getTongSoDong() { return tongSoDong; }
        public int getTongChenhLech() { return tongChenhLech; }
        public String getGhiChu() { return ghiChu; }
        public String getTrangThai() { return trangThai; }
    }

    public static class ChiTietKiemKeHistory {
        private final String id;
        private final String phieuKiemKeId;
        private final String loHangId;
        private final String soLoHang;
        private final String sanPhamId;
        private final String tenSanPham;
        private final String khoHangId;
        private final int tonHeThong;
        private final int tonThucTe;
        private final int chenhLech;
        private final String lyDo;

        public ChiTietKiemKeHistory(
                String id,
                String phieuKiemKeId,
                String loHangId,
                String soLoHang,
                String sanPhamId,
                String tenSanPham,
                String khoHangId,
                int tonHeThong,
                int tonThucTe,
                int chenhLech,
                String lyDo
        ) {
            this.id = id;
            this.phieuKiemKeId = phieuKiemKeId;
            this.loHangId = loHangId;
            this.soLoHang = soLoHang;
            this.sanPhamId = sanPhamId;
            this.tenSanPham = tenSanPham;
            this.khoHangId = khoHangId;
            this.tonHeThong = tonHeThong;
            this.tonThucTe = tonThucTe;
            this.chenhLech = chenhLech;
            this.lyDo = lyDo;
        }

        public String getId() { return id; }
        public String getPhieuKiemKeId() { return phieuKiemKeId; }
        public String getLoHangId() { return loHangId; }
        public String getSoLoHang() { return soLoHang; }
        public String getSanPhamId() { return sanPhamId; }
        public String getTenSanPham() { return tenSanPham; }
        public String getKhoHangId() { return khoHangId; }
        public int getTonHeThong() { return tonHeThong; }
        public int getTonThucTe() { return tonThucTe; }
        public int getChenhLech() { return chenhLech; }
        public String getLyDo() { return lyDo; }
    }

    public List<PhieuKiemKeHistory> layDanhSachPhieuKiemKe() {
        List<PhieuKiemKeHistory> ds = new ArrayList<>();

        String sql =
                "SELECT " +
                "    p.id, " +
                "    p.ngayKiemKe, " +
                "    p.khoHangId, " +
                "    p.nhanVienId, " +
                "    ISNULL(nv.hoVaTen, p.nhanVienId) AS tenNhanVien, " +
                "    p.tongSoDong, " +
                "    p.tongChenhLech, " +
                "    p.ghiChu, " +
                "    p.trangThai " +
                "FROM PhieuKiemKeKho p " +
                "LEFT JOIN NhanVien nv ON nv.id = p.nhanVienId " +
                "ORDER BY p.ngayKiemKe DESC, p.id DESC";

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return ds;
        }

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                ds.add(new PhieuKiemKeHistory(
                        rs.getString("id"),
                        rs.getTimestamp("ngayKiemKe"),
                        rs.getString("khoHangId"),
                        rs.getString("nhanVienId"),
                        rs.getString("tenNhanVien"),
                        rs.getInt("tongSoDong"),
                        rs.getInt("tongChenhLech"),
                        rs.getString("ghiChu"),
                        rs.getString("trangThai")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }

    public List<ChiTietKiemKeHistory> layChiTietTheoPhieu(String phieuKiemKeId) {
        List<ChiTietKiemKeHistory> ds = new ArrayList<>();

        String sql =
                "SELECT " +
                "    ct.id, " +
                "    ct.phieuKiemKeId, " +
                "    ct.loHangId, " +
                "    ct.soLoHang, " +
                "    ct.sanPhamId, " +
                "    ISNULL(sp.ten, N'Không rõ sản phẩm') AS tenSanPham, " +
                "    ct.khoHangId, " +
                "    ct.tonHeThong, " +
                "    ct.tonThucTe, " +
                "    ct.chenhLech, " +
                "    ct.lyDo " +
                "FROM ChiTietPhieuKiemKeKho ct " +
                "LEFT JOIN SanPham sp ON sp.id = ct.sanPhamId " +
                "WHERE ct.phieuKiemKeId = ? " +
                "ORDER BY ct.id ASC";

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return ds;
        }

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, phieuKiemKeId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ds.add(new ChiTietKiemKeHistory(
                            rs.getString("id"),
                            rs.getString("phieuKiemKeId"),
                            rs.getString("loHangId"),
                            rs.getString("soLoHang"),
                            rs.getString("sanPhamId"),
                            rs.getString("tenSanPham"),
                            rs.getString("khoHangId"),
                            rs.getInt("tonHeThong"),
                            rs.getInt("tonThucTe"),
                            rs.getInt("chenhLech"),
                            rs.getString("lyDo")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }
}