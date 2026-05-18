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
                safe(ghiChu));
    }

    public List<PhieuNhapHang> layDanhSachPhieuNhap() {
        return daoPhieuNhapHang.layDanhSachPhieuNhap();
    }

    public List<ChiTietPhieuNhapHang> layChiTietTheoPhieuNhap(String maPN) {
        if (isBlank(maPN)) {
            return new ArrayList<>();
        }

        return daoPhieuNhapHang.layChiTietTheoPhieuNhap(maPN.trim());
    }

    public List<Object[]> layNhatKyLoHang() {
        return daoPhieuNhapHang.layNhatKyLoHang();
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
}