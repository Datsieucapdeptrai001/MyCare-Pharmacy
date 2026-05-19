package DAO;

import ConnectDB.ConnectDB;
import Entity.LoHang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DAO_NhapLoHangDongBo {

    public static class KetQuaDAO {
        private final boolean thanhCong;
        private final boolean congDonVaoLoCu;
        private final String loHangId;
        private final String phieuNhapId;
        private final String thongBao;

        public KetQuaDAO(
                boolean thanhCong,
                boolean congDonVaoLoCu,
                String loHangId,
                String phieuNhapId,
                String thongBao) {
            this.thanhCong = thanhCong;
            this.congDonVaoLoCu = congDonVaoLoCu;
            this.loHangId = loHangId;
            this.phieuNhapId = phieuNhapId;
            this.thongBao = thongBao;
        }

        public boolean isThanhCong() {
            return thanhCong;
        }

        public boolean isCongDonVaoLoCu() {
            return congDonVaoLoCu;
        }

        public String getLoHangId() {
            return loHangId;
        }

        public String getPhieuNhapId() {
            return phieuNhapId;
        }

        public String getThongBao() {
            return thongBao;
        }
    }

    private static class LoHangTonTai {
        private final String id;
        private final String sanPhamId;
        private final String khoHangId;
        private final String soLoHang;
        private final java.sql.Date ngayHetHanDate;
        private final int soLuong;
        private final double gia;

        public LoHangTonTai(
                String id,
                String sanPhamId,
                String khoHangId,
                String soLoHang,
                java.sql.Date ngayHetHanDate,
                int soLuong,
                double gia) {
            this.id = id;
            this.sanPhamId = sanPhamId;
            this.khoHangId = khoHangId;
            this.soLoHang = soLoHang;
            this.ngayHetHanDate = ngayHetHanDate;
            this.soLuong = soLuong;
            this.gia = gia;
        }
    }

    public KetQuaDAO luuNhapLoVaTaoPhieuNhap(
            LoHang loNhapMoi,
            String maNhanVien,
            String ghiChu) {
        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            return new KetQuaDAO(
                    false,
                    false,
                    null,
                    null,
                    "Không có kết nối database.");
        }

        boolean oldAutoCommit = true;

        try {
            oldAutoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            String sanPhamId = loNhapMoi.getSanPhamId() == null
                    ? null
                    : loNhapMoi.getSanPhamId().getId();

            String khoHangId = loNhapMoi.getKhoHangId() == null
                    ? null
                    : loNhapMoi.getKhoHangId().getId();

            if (isBlank(sanPhamId)) {
                rollback(con, oldAutoCommit);
                return new KetQuaDAO(false, false, null, null, "Thiếu sản phẩm.");
            }

            if (isBlank(khoHangId)) {
                rollback(con, oldAutoCommit);
                return new KetQuaDAO(false, false, null, null, "Thiếu kho hàng.");
            }

            if (isBlank(loNhapMoi.getSoLoHang())) {
                rollback(con, oldAutoCommit);
                return new KetQuaDAO(false, false, null, null, "Thiếu mã lô.");
            }

            if (loNhapMoi.getNgayHetHan() == null) {
                rollback(con, oldAutoCommit);
                return new KetQuaDAO(false, false, null, null, "Thiếu hạn sử dụng.");
            }

            if (loNhapMoi.getSoLuongLoHang() <= 0) {
                rollback(con, oldAutoCommit);
                return new KetQuaDAO(false, false, null, null, "Số lượng nhập phải lớn hơn 0.");
            }

            if (loNhapMoi.getGia() <= 0) {
                rollback(con, oldAutoCommit);
                return new KetQuaDAO(false, false, null, null, "Giá nhập phải lớn hơn 0.");
            }

            if (isBlank(maNhanVien)) {
                maNhanVien = "QL-0001";
            }

            /*
             * LOGIC:
             *
             * 1. Nếu cùng sản phẩm + cùng mã lô nhưng khác kho:
             * -> được tạo lô mới ở kho khác.
             *
             * 2. Nếu cùng sản phẩm + cùng kho + cùng mã lô:
             * -> chỉ được cộng dồn khi cùng hạn sử dụng và cùng giá nhập.
             *
             * 3. Nếu cùng sản phẩm + cùng kho + cùng mã lô
             * nhưng khác hạn sử dụng hoặc khác giá nhập:
             * -> báo lỗi, không insert LoHang, không tạo phiếu nhập.
             */
            LoHangTonTai loCuCungSPKhoMaLo = timLoCuCungSanPhamKhoVaMaLo(
                    con,
                    sanPhamId,
                    khoHangId,
                    loNhapMoi.getSoLoHang());

            boolean congDon = false;
            String loHangIdDungDeNhap;

            if (loCuCungSPKhoMaLo == null) {
                loHangIdDungDeNhap = loNhapMoi.getId();
                themLoHangMoi(con, loNhapMoi);
            } else {
                boolean dungDieuKienCongDon = kiemTraCungHanSuDungVaGiaNhap(
                        loCuCungSPKhoMaLo,
                        loNhapMoi);

                if (!dungDieuKienCongDon) {
                    rollback(con, oldAutoCommit);

                    String thongBao = "Mã lô '" + loNhapMoi.getSoLoHang() + "' đã tồn tại trong kho này.\n\n"
                            + "Chỉ được cộng dồn khi cùng:\n"
                            + "- Sản phẩm\n"
                            + "- Kho nhập\n"
                            + "- Mã lô\n"
                            + "- Hạn sử dụng\n"
                            + "- Giá nhập\n\n"
                            + "Nếu muốn nhập cùng mã lô ở kho khác, hãy chọn kho khác chưa có mã lô này.";

                    return new KetQuaDAO(false, false, loCuCungSPKhoMaLo.id, null, thongBao);
                }

                congDon = true;
                loHangIdDungDeNhap = loCuCungSPKhoMaLo.id;
                capNhatCongDonLoCu(con, loCuCungSPKhoMaLo, loNhapMoi);
            }

            String maPhieuNhap = taoMaPhieuNhapMoi(con);
            String maChiTiet = taoMaChiTietPhieuNhapMoi(con);

            double thanhTienLanNhap = loNhapMoi.getSoLuongLoHang() * loNhapMoi.getGia();

            themPhieuNhap(
                    con,
                    maPhieuNhap,
                    maNhanVien,
                    thanhTienLanNhap,
                    ghiChu);

            themChiTietPhieuNhap(
                    con,
                    maChiTiet,
                    maPhieuNhap,
                    loHangIdDungDeNhap,
                    sanPhamId,
                    khoHangId,
                    loNhapMoi.getSoLoHang(),
                    loNhapMoi.getSoLuongLoHang(),
                    loNhapMoi.getGia(),
                    thanhTienLanNhap,
                    loNhapMoi);

            con.commit();
            con.setAutoCommit(oldAutoCommit);

            if (congDon) {
                return new KetQuaDAO(
                        true,
                        true,
                        loHangIdDungDeNhap,
                        maPhieuNhap,
                        "Đã cộng dồn vào lô cũ trong cùng kho và tạo phiếu nhập mới.");
            }

            return new KetQuaDAO(
                    true,
                    false,
                    loHangIdDungDeNhap,
                    maPhieuNhap,
                    "Đã tạo lô hàng mới và phiếu nhập mới.");

        } catch (Exception e) {
            e.printStackTrace();

            try {
                con.rollback();
                con.setAutoCommit(oldAutoCommit);
            } catch (Exception ignored) {
            }

            return new KetQuaDAO(
                    false,
                    false,
                    null,
                    null,
                    "Lỗi nhập lô hàng: " + e.getMessage());
        }
    }

    private LoHangTonTai timLoCuCungSanPhamKhoVaMaLo(
            Connection con,
            String sanPhamId,
            String khoHangId,
            String soLoHang) throws SQLException {
        String sql = "SELECT TOP 1 " +
                "id, sanPhamId, khoHangId, soLoHang, " +
                "CAST(ngayHetHan AS DATE) AS ngayHetHanDate, " +
                "soLuongLoHang, gia " +
                "FROM LoHang " +
                "WHERE sanPhamId = ? " +
                "  AND khoHangId = ? " +
                "  AND UPPER(LTRIM(RTRIM(soLoHang))) = UPPER(LTRIM(RTRIM(?))) " +
                "  AND ISNULL(trangThai, 'CON_HANG') <> 'AN' " +
                "ORDER BY ngayNhap ASC, id ASC";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, sanPhamId);
            pst.setString(2, khoHangId);
            pst.setString(3, soLoHang);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new LoHangTonTai(
                            rs.getString("id"),
                            rs.getString("sanPhamId"),
                            rs.getString("khoHangId"),
                            rs.getString("soLoHang"),
                            rs.getDate("ngayHetHanDate"),
                            rs.getInt("soLuongLoHang"),
                            rs.getDouble("gia"));
                }
            }
        }

        return null;
    }

    private boolean kiemTraCungHanSuDungVaGiaNhap(
            LoHangTonTai loCu,
            LoHang loNhapMoi) {
        if (loCu == null || loNhapMoi == null || loNhapMoi.getNgayHetHan() == null) {
            return false;
        }

        java.sql.Date ngayMoi = java.sql.Date.valueOf(loNhapMoi.getNgayHetHan().toLocalDate());
        boolean cungHanSuDung = loCu.ngayHetHanDate != null && loCu.ngayHetHanDate.equals(ngayMoi);

        double giaCu = lamTron2So(loCu.gia);
        double giaMoi = lamTron2So(loNhapMoi.getGia());
        boolean cungGiaNhap = Double.compare(giaCu, giaMoi) == 0;

        return cungHanSuDung && cungGiaNhap;
    }

    private void capNhatCongDonLoCu(
            Connection con,
            LoHangTonTai loCu,
            LoHang loNhapMoi) throws SQLException {
        int tongSoLuongMoi = loCu.soLuong + loNhapMoi.getSoLuongLoHang();

        String sql = "UPDATE LoHang " +
                "SET soLuongLoHang = ?, " +
                "    trangThai = CASE " +
                "        WHEN ngayHetHan < GETDATE() THEN N'HET_HAN' " +
                "        WHEN ? <= 0 THEN N'HET_HANG' " +
                "        ELSE N'CON_HANG' " +
                "    END, " +
                "    maVachNoiBo = CASE " +
                "        WHEN maVachNoiBo IS NULL OR LTRIM(RTRIM(maVachNoiBo)) = '' THEN ? " +
                "        ELSE maVachNoiBo " +
                "    END " +
                "WHERE id = ?";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, tongSoLuongMoi);
            pst.setInt(2, tongSoLuongMoi);
            pst.setString(3, loNhapMoi.getMaVachNoiBo());
            pst.setString(4, loCu.id);
            pst.executeUpdate();
        }
    }

    private void themLoHangMoi(
            Connection con,
            LoHang loNhapMoi) throws SQLException {
        String sql = "INSERT INTO LoHang(" +
                "id, soLoHang, soLuongLoHang, gia, ngayNhap, ngayHetHan, " +
                "trangThai, sanPhamId, khoHangId, maVachNoiBo" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, loNhapMoi.getId());
            pst.setString(2, loNhapMoi.getSoLoHang());
            pst.setInt(3, loNhapMoi.getSoLuongLoHang());
            pst.setDouble(4, loNhapMoi.getGia());
            pst.setTimestamp(5, java.sql.Timestamp.valueOf(loNhapMoi.getNgayNhap()));
            pst.setTimestamp(6, java.sql.Timestamp.valueOf(loNhapMoi.getNgayHetHan()));
            pst.setString(7, tinhTrangThaiLo(loNhapMoi));
            pst.setString(8, loNhapMoi.getSanPhamId().getId());
            pst.setString(9, loNhapMoi.getKhoHangId().getId());
            pst.setString(10, loNhapMoi.getMaVachNoiBo());
            pst.executeUpdate();
        }
    }

    private void themPhieuNhap(
            Connection con,
            String maPhieuNhap,
            String maNhanVien,
            double tongTien,
            String ghiChu) throws SQLException {
        String sql = "INSERT INTO PhieuNhapHang(" +
                "id, ngayNhap, nhaCungCapId, nhanVienId, tongTien, ghiChu, trangThai" +
                ") VALUES (?, GETDATE(), NULL, ?, ?, ?, N'HOAN_THANH')";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maPhieuNhap);
            pst.setString(2, maNhanVien);
            pst.setDouble(3, tongTien);
            pst.setString(4, isBlank(ghiChu)
                    ? "Phiếu nhập từ màn hình nhập lô hàng"
                    : ghiChu);
            pst.executeUpdate();
        }
    }

    private void themChiTietPhieuNhap(
            Connection con,
            String maChiTiet,
            String maPhieuNhap,
            String loHangId,
            String sanPhamId,
            String khoHangId,
            String soLoHang,
            int soLuongNhap,
            double donGiaNhap,
            double thanhTien,
            LoHang loNhapMoi) throws SQLException {
        String sql = "INSERT INTO ChiTietPhieuNhapHang(" +
                "id, phieuNhapId, loHangId, sanPhamId, khoHangId, soLoHang, " +
                "soLuongNhap, donGiaNhap, thanhTien, hanSuDung" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maChiTiet);
            pst.setString(2, maPhieuNhap);
            pst.setString(3, loHangId);
            pst.setString(4, sanPhamId);
            pst.setString(5, khoHangId);
            pst.setString(6, soLoHang);
            pst.setInt(7, soLuongNhap);
            pst.setDouble(8, donGiaNhap);
            pst.setDouble(9, thanhTien);
            pst.setTimestamp(10, java.sql.Timestamp.valueOf(loNhapMoi.getNgayHetHan()));
            pst.executeUpdate();
        }
    }

    private String taoMaPhieuNhapMoi(Connection con) throws SQLException {
        String sql = "SELECT TOP 1 id " +
                "FROM PhieuNhapHang " +
                "WHERE id LIKE 'PN-%' " +
                "ORDER BY TRY_CAST(REPLACE(id, 'PN-', '') AS INT) DESC";

        int next = 1;

        try (PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                next = laySoCuoiMa(rs.getString("id")) + 1;
            }
        }

        return String.format("PN-%04d", next);
    }

    private String taoMaChiTietPhieuNhapMoi(Connection con) throws SQLException {
        String sql = "SELECT TOP 1 id " +
                "FROM ChiTietPhieuNhapHang " +
                "WHERE id LIKE 'CTPN-%' " +
                "ORDER BY TRY_CAST(REPLACE(id, 'CTPN-', '') AS INT) DESC";

        int next = 1;

        try (PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                next = laySoCuoiMa(rs.getString("id")) + 1;
            }
        }

        return String.format("CTPN-%04d", next);
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

    private String tinhTrangThaiLo(LoHang loHang) {
        if (loHang.getNgayHetHan() != null
                && loHang.getNgayHetHan().isBefore(java.time.LocalDateTime.now())) {
            return "HET_HAN";
        }

        if (loHang.getSoLuongLoHang() <= 0) {
            return "HET_HANG";
        }

        return "CON_HANG";
    }

    private void rollback(Connection con, boolean oldAutoCommit) {
        try {
            con.rollback();
            con.setAutoCommit(oldAutoCommit);
        } catch (Exception ignored) {
        }
    }

    private double lamTron2So(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}