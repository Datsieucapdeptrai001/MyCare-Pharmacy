package BUS;

import DAO.DAO_PhieuNhapHang;
import Entity.ChiTietPhieuNhapHang;
import Entity.LoHang;
import Entity.PhieuNhapHang;

import java.util.ArrayList;
import java.util.List;

public class BUS_PhieuNhapHang {

    private final DAO_PhieuNhapHang daoPhieuNhapHang;

    public BUS_PhieuNhapHang() {
        daoPhieuNhapHang = new DAO_PhieuNhapHang();
    }

    public String taoMaPhieuNhapMoi() {
        return daoPhieuNhapHang.taoMaPhieuNhapMoi();
    }

    public String taoMaChiTietMoi() {
        return daoPhieuNhapHang.taoMaChiTietMoi();
    }

    public boolean themPhieuNhap(PhieuNhapHang phieu) {
        if (phieu == null) {
            return false;
        }

        if (isBlank(phieu.getId())) {
            return false;
        }

        if (phieu.getNgayNhap() == null) {
            return false;
        }

        if (phieu.getTongTien() < 0) {
            return false;
        }

        if (isBlank(phieu.getTrangThai())) {
            phieu.setTrangThai("HOAN_THANH");
        }

        return daoPhieuNhapHang.themPhieuNhap(phieu);
    }

    public boolean themChiTietPhieuNhap(ChiTietPhieuNhapHang ct) {
        if (ct == null) {
            return false;
        }

        if (isBlank(ct.getId())) {
            return false;
        }

        if (ct.getPhieuNhapId() == null || isBlank(ct.getPhieuNhapId().getId())) {
            return false;
        }

        if (ct.getLoHangId() == null || isBlank(ct.getLoHangId().getId())) {
            return false;
        }

        if (ct.getSanPhamId() == null || isBlank(ct.getSanPhamId().getId())) {
            return false;
        }

        if (ct.getKhoHangId() == null || isBlank(ct.getKhoHangId().getId())) {
            return false;
        }

        if (isBlank(ct.getSoLoHang())) {
            return false;
        }

        if (ct.getSoLuongNhap() <= 0) {
            return false;
        }

        if (ct.getDonGiaNhap() <= 0) {
            return false;
        }

        if (ct.getThanhTien() <= 0) {
            return false;
        }

        return daoPhieuNhapHang.themChiTietPhieuNhap(ct);
    }

    public boolean lapPhieuNhapTuLoHang(LoHang loHang, String nhanVienId, String nhaCungCapId, String ghiChu) {
        if (loHang == null) {
            return false;
        }

        if (isBlank(loHang.getId())) {
            return false;
        }

        if (loHang.getSanPhamId() == null || isBlank(loHang.getSanPhamId().getId())) {
            return false;
        }

        if (loHang.getKhoHangId() == null || isBlank(loHang.getKhoHangId().getId())) {
            return false;
        }

        if (isBlank(loHang.getSoLoHang())) {
            return false;
        }

        if (loHang.getSoLuongLoHang() <= 0) {
            return false;
        }

        if (loHang.getGia() <= 0) {
            return false;
        }

        if (isBlank(nhanVienId)) {
            nhanVienId = "QL-0001";
        }

        return daoPhieuNhapHang.lapPhieuNhapTuLoHang(
                loHang,
                nhanVienId.trim(),
                safeNullable(nhaCungCapId),
                safe(ghiChu)
        );
    }

    public List<PhieuNhapHang> layDanhSachPhieuNhap() {
        List<PhieuNhapHang> ds = daoPhieuNhapHang.layDanhSachPhieuNhap();

        if (ds == null) {
            return new ArrayList<>();
        }

        return ds;
    }

    public List<ChiTietPhieuNhapHang> layChiTietTheoPhieuNhap(String maPN) {
        if (isBlank(maPN)) {
            return new ArrayList<>();
        }

        List<ChiTietPhieuNhapHang> ds = daoPhieuNhapHang.layChiTietTheoPhieuNhap(maPN.trim());

        if (ds == null) {
            return new ArrayList<>();
        }

        return ds;
    }

    /*
     * Dùng cho màn hình lịch sử lô hàng.
     *
     * Chuẩn dữ liệu trả về cho GUI:
     * row[0] = thời gian nhập
     * row[1] = mã phiếu nhập
     * row[2] = hành động
     * row[3] = mã lô
     * row[4] = tên sản phẩm
     * row[5] = số lượng nhập
     * row[6] = đơn giá nhập / đơn vị cơ bản
     * row[7] = thành tiền
     * row[8] = người thực hiện
     * row[9] = đơn vị cơ bản: Viên / Hộp / Chai / Gói / Tuýp...
     */
    public List<Object[]> layNhatKyLoHang() {
        List<Object[]> rawData = daoPhieuNhapHang.layNhatKyLoHang();
        List<Object[]> result = new ArrayList<>();

        if (rawData == null) {
            return result;
        }

        for (Object[] row : rawData) {
            if (row == null) {
                continue;
            }

            Object thoiGian = getValue(row, 0);
            Object maPhieuNhap = getValue(row, 1);
            Object hanhDong = getValue(row, 2);
            Object maLo = getValue(row, 3);
            Object tenSanPham = getValue(row, 4);
            Object soLuongNhap = getValue(row, 5);
            Object donGiaNhap = getValue(row, 6);
            Object thanhTien = getValue(row, 7);
            Object nguoiThucHien = getValue(row, 8);

            String donViCoBan = "";

            /*
             * Nếu DAO đã trả thêm đơn vị cơ bản ở row[9] thì dùng luôn.
             */
            if (row.length > 9) {
                donViCoBan = safeObject(row[9]);
            }

            /*
             * Nếu DAO chưa trả đơn vị thì đoán tạm từ tên sản phẩm.
             * Nên sửa DAO để SELECT sp.donViDoCoBan cho chuẩn 100%.
             */
            if (isBlank(donViCoBan)) {
                donViCoBan = layDonViCoBanTuTenSanPham(safeObject(tenSanPham));
            }

            result.add(new Object[]{
                    thoiGian,
                    maPhieuNhap,
                    isBlank(safeObject(hanhDong)) ? "Nhập kho" : hanhDong,
                    maLo,
                    tenSanPham,
                    soLuongNhap,
                    donGiaNhap,
                    thanhTien,
                    nguoiThucHien,
                    donViCoBan
            });
        }

        return result;
    }

    /*
     * Dùng nếu màn hình chi tiết phiếu nhập cần lấy đơn vị cơ bản rõ ràng hơn.
     */
    public String layDonViCoBanTuTenSanPham(String tenSanPham) {
        if (tenSanPham == null) {
            return "đơn vị";
        }

        String ten = tenSanPham.trim().toLowerCase();

        if (ten.isEmpty()) {
            return "đơn vị";
        }

        if (ten.contains("siro")
                || ten.contains("xịt")
                || ten.contains("rohto")
                || ten.contains("osla")
                || ten.contains("listerine")
                || ten.contains("betadine")
                || ten.contains("cerave")
                || ten.contains("bioderma")
                || ten.contains("toner")
                || ten.contains("nước")
                || ten.contains("chai")) {
            return "Chai";
        }

        if (ten.contains("la roche")
                || ten.contains("tuýp")
                || ten.contains("tuyp")
                || ten.contains("kem chống nắng")) {
            return "Tuýp";
        }

        if (ten.contains("oresol")
                || ten.contains("smecta")
                || ten.contains("hapacol")
                || ten.contains("phosphalugel")
                || ten.contains("collagen")
                || ten.contains("gói")) {
            return "Gói";
        }

        if (ten.contains("kem")
                || ten.contains("eucerin")
                || ten.contains("hộp")) {
            return "Hộp";
        }

        return "Viên";
    }

    private Object getValue(Object[] row, int index) {
        if (row == null || index < 0 || index >= row.length) {
            return null;
        }

        return row[index];
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private String safeNullable(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }

    private String safeObject(Object obj) {
        return obj == null ? "" : obj.toString().trim();
    }
}