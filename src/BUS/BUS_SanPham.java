package BUS;

import DAO.DAO_SanPham;
import Entity.LoHang;
import Entity.SanPham;

import java.util.ArrayList;
import java.util.List;

public class BUS_SanPham {

    private final DAO_SanPham daoSanPham;

    public BUS_SanPham() {
        daoSanPham = new DAO_SanPham();
    }

    // ============================================================
    // PHẦN 1: NGHIỆP VỤ CHO GUI
    // ============================================================

    public List<SanPham> getDsThuoc() {
        return daoSanPham.getDsThuoc();
    }

    public SanPham getSanPhamTheoMa(String id) {
        if (id == null || id.trim().isEmpty()) return null;
        return daoSanPham.getSanPhamTheoMa(id.trim());
    }

    public List<SanPham> traCuuSanPham(String tuKhoa) {
        List<SanPham> dsSanPham = daoSanPham.getDsThuoc();

        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return dsSanPham;
        }

        List<SanPham> ketQua = new ArrayList<>();
        String key = tuKhoa.trim().toLowerCase();

        for (SanPham sp : dsSanPham) {
            String id = sp.getId() != null ? sp.getId().toLowerCase() : "";
            String ten = sp.getTen() != null ? sp.getTen().toLowerCase() : "";
            String vietTat = sp.getTenVietTat() != null ? sp.getTenVietTat().toLowerCase() : "";
            String hoatChat = sp.getHoatChat() != null ? sp.getHoatChat().toLowerCase() : "";

            if (id.contains(key) || ten.contains(key) || vietTat.contains(key) || hoatChat.contains(key)) {
                ketQua.add(sp);
            }
        }
        return ketQua;
    }

    public List<LoHang> layLoTheoSP(String maSP) {
        if (maSP == null || maSP.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return daoSanPham.layLoTheoSP(maSP.trim());
    }

    public List<Object[]> timKiemSanPhamBan(String keyword) {
        if (keyword == null) keyword = "";
        return daoSanPham.timKiemSanPhamBan(keyword.trim());
    }

    public List<Object[]> layDanhSachChoBang() {
        return daoSanPham.layDanhSachSanPhamChoBang();
    }

    // ============================================================
    // PHẦN 2: VALIDATE NGHIỆP VỤ
    // ============================================================

    public boolean kiemTraThongTinSP(SanPham sp) {
        if (sp == null) return false;
        if (sp.getId() == null || sp.getId().trim().isEmpty()) return false;
        if (sp.getTen() == null || sp.getTen().trim().isEmpty()) return false;
        if (sp.getDonViDoCoBan() == null || sp.getDonViDoCoBan().trim().isEmpty()) return false;
        if (sp.getThueVAT() < 0) return false;
        return true;
    }

    public double tinhGiaBanTheoDonVi(String maSP, String donViMuonBan) {
        if (maSP == null || maSP.trim().isEmpty()) return 0;
        if (donViMuonBan == null || donViMuonBan.trim().isEmpty()) return 0;

        List<Object[]> ds = daoSanPham.timKiemSanPhamBan(maSP.trim());
        for (Object[] row : ds) {
            String ma = String.valueOf(row[0]);
            String donVi = String.valueOf(row[2]);

            if (ma.equalsIgnoreCase(maSP.trim()) && donVi.equalsIgnoreCase(donViMuonBan.trim())) {
                return ((Number) row[3]).doubleValue();
            }
        }
        return 0;
    }

    // ============================================================
    // PHẦN 3: CRUD SẢN PHẨM
    // ============================================================

    public String taoMaMoi() {
        return daoSanPham.layMaSanPhamMoiNhat();
    }

    public boolean themSP(String id, String danhMuc, String dang, String ten,
                          String vietTat, String nsx, String hoatChat,
                          double vat, String hamLuong, String moTa, String dvt) {

        if (id == null || id.trim().isEmpty()) return false;
        if (ten == null || ten.trim().isEmpty()) return false;
        if (dvt == null || dvt.trim().isEmpty()) return false;
        if (vat < 0) return false;

        return daoSanPham.themSanPhamNhanh(
                id.trim(),
                danhMuc,
                dang,
                ten.trim(),
                vietTat != null ? vietTat.trim() : "",
                nsx != null ? nsx.trim() : "",
                hoatChat != null ? hoatChat.trim() : "",
                vat,
                hamLuong != null ? hamLuong.trim() : "",
                moTa != null ? moTa.trim() : "",
                dvt.trim()
        );
    }

    public boolean capNhatSP(String id, String danhMuc, String dang, String ten,
                             String vietTat, String nsx, String hoatChat,
                             double vat, String hamLuong, String moTa, String dvt) {

        if (id == null || id.trim().isEmpty()) return false;
        if (ten == null || ten.trim().isEmpty()) return false;
        if (dvt == null || dvt.trim().isEmpty()) return false;
        if (vat < 0) return false;

        return daoSanPham.capNhatSanPhamNhanh(
                id.trim(),
                danhMuc,
                dang,
                ten.trim(),
                vietTat != null ? vietTat.trim() : "",
                nsx != null ? nsx.trim() : "",
                hoatChat != null ? hoatChat.trim() : "",
                vat,
                hamLuong != null ? hamLuong.trim() : "",
                moTa != null ? moTa.trim() : "",
                dvt.trim()
        );
    }

    public boolean xoaSP(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        return daoSanPham.xoaSanPham(id.trim());
    }

    // ============================================================
    // PHẦN 4: LÔ HÀNG / TỒN KHO
    // ============================================================

    public boolean capNhatSoLuongTon(String maLoHang, int soLuongMoi) {
        if (maLoHang == null || maLoHang.trim().isEmpty()) return false;
        if (soLuongMoi < 0) return false;
        return daoSanPham.capNhatSoLuongTon(maLoHang.trim(), soLuongMoi);
    }
}