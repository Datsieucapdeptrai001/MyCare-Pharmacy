package DAO;

import ConnectDB.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DAO_KiemKeKho {

    public static class KiemKeItem {
        private String loHangId;
        private String soLoHang;
        private String sanPhamId;
        private String tenSanPham;
        private String khoHangId;
        private int tonHeThong;
        private int tonThucTe;
        private int chenhLech;
        private String lyDo;

        public KiemKeItem(
                String loHangId,
                String soLoHang,
                String sanPhamId,
                String tenSanPham,
                String khoHangId,
                int tonHeThong
        ) {
            this.loHangId = loHangId;
            this.soLoHang = soLoHang;
            this.sanPhamId = sanPhamId;
            this.tenSanPham = tenSanPham;
            this.khoHangId = khoHangId;
            this.tonHeThong = tonHeThong;
            this.tonThucTe = tonHeThong;
            this.chenhLech = 0;
            this.lyDo = "";
        }

        public String getLoHangId() {
            return loHangId;
        }

        public String getSoLoHang() {
            return soLoHang;
        }

        public String getSanPhamId() {
            return sanPhamId;
        }

        public String getTenSanPham() {
            return tenSanPham;
        }

        public String getKhoHangId() {
            return khoHangId;
        }

        public int getTonHeThong() {
            return tonHeThong;
        }

        public int getTonThucTe() {
            return tonThucTe;
        }

        public void setTonThucTe(int tonThucTe) {
            this.tonThucTe = tonThucTe;
            this.chenhLech = tonThucTe - tonHeThong;
        }

        public int getChenhLech() {
            return chenhLech;
        }

        public String getLyDo() {
            return lyDo;
        }

        public void setLyDo(String lyDo) {
            this.lyDo = lyDo == null ? "" : lyDo.trim();
        }
    }

    public static class KetQuaKiemKe {
        private final boolean thanhCong;
        private final String phieuKiemKeId;
        private final String thongBao;

        public KetQuaKiemKe(
                boolean thanhCong,
                String phieuKiemKeId,
                String thongBao
        ) {
            this.thanhCong = thanhCong;
            this.phieuKiemKeId = phieuKiemKeId;
            this.thongBao = thongBao;
        }

        public boolean isThanhCong() {
            return thanhCong;
        }

        public String getPhieuKiemKeId() {
            return phieuKiemKeId;
        }

        public String getThongBao() {
            return thongBao;
        }
    }

    public List<String> layDanhSachMaKho() {
        List<String> ds = new ArrayList<>();

        String sql =
                "SELECT id " +
                "FROM KhoHang " +
                "ORDER BY id";

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return ds;
        }

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                ds.add(rs.getString("id"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }

    public List<KiemKeItem> layDanhSachLoTheoKho(String khoHangId) {
        List<KiemKeItem> ds = new ArrayList<>();

        String sql =
                "SELECT " +
                "    lh.id AS loHangId, " +
                "    lh.soLoHang, " +
                "    lh.sanPhamId, " +
                "    ISNULL(sp.ten, N'Không rõ sản phẩm') AS tenSanPham, " +
                "    lh.khoHangId, " +
                "    ISNULL(lh.soLuongLoHang, 0) AS soLuongLoHang " +
                "FROM LoHang lh " +
                "LEFT JOIN SanPham sp ON sp.id = lh.sanPhamId " +
                "WHERE lh.khoHangId = ? " +
                "  AND ISNULL(lh.trangThai, N'CON_HANG') <> N'AN' " +
                "ORDER BY sp.ten ASC, lh.soLoHang ASC, lh.id ASC";

        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return ds;
        }

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, khoHangId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ds.add(new KiemKeItem(
                            rs.getString("loHangId"),
                            rs.getString("soLoHang"),
                            rs.getString("sanPhamId"),
                            rs.getString("tenSanPham"),
                            rs.getString("khoHangId"),
                            rs.getInt("soLuongLoHang")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ds;
    }

    public KetQuaKiemKe luuPhieuKiemKe(
            String khoHangId,
            String nhanVienId,
            String ghiChu,
            List<KiemKeItem> dsItem
    ) {
        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return new KetQuaKiemKe(false, null, "Không có kết nối database.");
        }

        boolean oldAutoCommit = true;

        try {
            oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            String phieuId = taoMaPhieuKiemKeMoi(con);

            int tongSoDong = dsItem == null ? 0 : dsItem.size();
            int tongChenhLech = 0;

            if (dsItem != null) {
                for (KiemKeItem item : dsItem) {
                    tongChenhLech += item.getChenhLech();
                }
            }

            themPhieuKiemKe(
                    con,
                    phieuId,
                    khoHangId,
                    nhanVienId,
                    tongSoDong,
                    tongChenhLech,
                    ghiChu
            );

            int index = 1;

            if (dsItem != null) {
                for (KiemKeItem item : dsItem) {
                    String chiTietId = taoMaChiTietTheoIndex(phieuId, index++);

                    themChiTietKiemKe(
                            con,
                            chiTietId,
                            phieuId,
                            item
                    );

                    if (item.getChenhLech() != 0) {
                        capNhatTonLoHang(
                                con,
                                item.getLoHangId(),
                                item.getTonThucTe()
                        );
                    }
                }
            }

            con.commit();
            con.setAutoCommit(oldAutoCommit);

            return new KetQuaKiemKe(
                    true,
                    phieuId,
                    "Đã lưu phiếu kiểm kê và cập nhật tồn kho."
            );

        } catch (Exception e) {
            e.printStackTrace();

            try {
                con.rollback();
                con.setAutoCommit(oldAutoCommit);
            } catch (Exception ignored) {
            }

            return new KetQuaKiemKe(
                    false,
                    null,
                    "Lỗi lưu kiểm kê: " + e.getMessage()
            );
        }
    }

    private void themPhieuKiemKe(
            Connection con,
            String phieuId,
            String khoHangId,
            String nhanVienId,
            int tongSoDong,
            int tongChenhLech,
            String ghiChu
    ) throws SQLException {
        String sql =
                "INSERT INTO PhieuKiemKeKho(" +
                "id, ngayKiemKe, khoHangId, nhanVienId, tongSoDong, tongChenhLech, ghiChu, trangThai" +
                ") VALUES (?, GETDATE(), ?, ?, ?, ?, ?, N'HOAN_THANH')";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, phieuId);
            pst.setString(2, khoHangId);
            pst.setString(3, nhanVienId);
            pst.setInt(4, tongSoDong);
            pst.setInt(5, tongChenhLech);
            pst.setString(6, ghiChu);
            pst.executeUpdate();
        }
    }

    private void themChiTietKiemKe(
            Connection con,
            String chiTietId,
            String phieuId,
            KiemKeItem item
    ) throws SQLException {
        String sql =
                "INSERT INTO ChiTietPhieuKiemKeKho(" +
                "id, phieuKiemKeId, loHangId, sanPhamId, khoHangId, soLoHang, " +
                "tonHeThong, tonThucTe, chenhLech, lyDo" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, chiTietId);
            pst.setString(2, phieuId);
            pst.setString(3, item.getLoHangId());
            pst.setString(4, item.getSanPhamId());
            pst.setString(5, item.getKhoHangId());
            pst.setString(6, item.getSoLoHang());
            pst.setInt(7, item.getTonHeThong());
            pst.setInt(8, item.getTonThucTe());
            pst.setInt(9, item.getChenhLech());
            pst.setString(10, item.getLyDo());
            pst.executeUpdate();
        }
    }

    private void capNhatTonLoHang(
            Connection con,
            String loHangId,
            int tonThucTe
    ) throws SQLException {
        String sql =
                "UPDATE LoHang " +
                "SET soLuongLoHang = ?, " +
                "    trangThai = CASE " +
                "        WHEN ngayHetHan < GETDATE() THEN N'HET_HAN' " +
                "        WHEN ? <= 0 THEN N'HET_HANG' " +
                "        ELSE N'CON_HANG' " +
                "    END " +
                "WHERE id = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, tonThucTe);
            pst.setInt(2, tonThucTe);
            pst.setString(3, loHangId);
            pst.executeUpdate();
        }
    }

    private String taoMaPhieuKiemKeMoi(Connection con) throws SQLException {
        String sql =
                "SELECT TOP 1 id " +
                "FROM PhieuKiemKeKho " +
                "WHERE id LIKE 'PKK-%' " +
                "ORDER BY TRY_CAST(REPLACE(id, 'PKK-', '') AS INT) DESC";

        int next = 1;

        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                next = laySoCuoiMa(rs.getString("id")) + 1;
            }
        }

        return String.format("PKK-%04d", next);
    }

    private String taoMaChiTietTheoIndex(String phieuId, int index) {
        return "CT" + phieuId + "-" + String.format("%03d", index);
    }

    private int laySoCuoiMa(String ma) {
        if (ma == null) {
            return 0;
        }

        try {
            String onlyNumber = ma.replaceAll("[^0-9]", "");
            return Integer.parseInt(onlyNumber);
        } catch (Exception e) {
            return 0;
        }
    }
}