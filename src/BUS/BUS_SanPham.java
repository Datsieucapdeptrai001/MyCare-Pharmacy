package BUS;

import DAO.DAO_SanPham;
import Entity.LoHang;
import Entity.SanPham;

import java.util.ArrayList;
import java.util.List;

public class BUS_SanPham {
    private final DAO_SanPham daoSanPham = new DAO_SanPham();

    public BUS_SanPham() { }

    public List<SanPham> traCuuSanPham(String tuKhoa) {
        return daoSanPham.timKiemSanPhamDoiTra(tuKhoa);
    }
    public List<Object[]> timKiemSanPhamBan(String text) {
        if (isBlank(text)) return new ArrayList<>();
        return daoSanPham.timKiemSanPhamBan(text.trim());
    }
    public double layThueVATTheoTenSP(String tenSP) {
        if (tenSP == null || tenSP.trim().isEmpty()) {
            return 0;
        }
        return daoSanPham.layThueVATTheoTenSP(tenSP.trim());
    }
    public boolean kiemTraThongTinSP(SanPham sp) {
        if (sp == null) return false;
        if (isBlank(sp.getId())) return false;
        if (isBlank(sp.getTen())) return false;
        if (sp.getThueVAT() < 0) return false;
        return true;
    }

    public double tinhGiaBanTheoDonVi(String maSP, String donViMuonBan) {
        if (isBlank(maSP) || isBlank(donViMuonBan)) return 0.0;
        List<Object[]> list = daoSanPham.layDonViQuyDoiTheoSP(maSP.trim());
        for (Object[] row : list) {
            if (donViMuonBan.trim().equalsIgnoreCase(row[0].toString())) {
                try {
                    return Double.parseDouble(row[2].toString().replace(",", "").trim());
                } catch (Exception ignored) {}
            }
        }
        SanPham sp = daoSanPham.getSanPhamDayDu(maSP.trim());
        return sp != null ? sp.getGiaBan() : 0.0;
    }

    public List<LoHang> layLoTheoSP(String maSP) {
        if (isBlank(maSP)) return new ArrayList<>();
        return daoSanPham.layLoTheoSP(maSP.trim());
    }

    public List<Object[]> layDonViTheoSP(String maSP) {
        if (isBlank(maSP)) return new ArrayList<>();
        return daoSanPham.layDonViDoLuongTheoSP(maSP.trim());
    }

    public String taoMaMoi() {
        return daoSanPham.layMaSanPhamMoiNhat();
    }


    public boolean themSP(String id, String danhMuc, String dang, String ten, String vietTat,
                        String nsx, String hoatChat, double vat, String hamLuong,
                        String moTa, String dvt, double giaBan) {
        if (isBlank(id) || isBlank(ten) || vat < 0 || giaBan < 0) return false;
        return daoSanPham.themSanPhamNhanh(
                id.trim(), safe(danhMuc).trim(), safe(dang).trim(),
                ten.trim(), safe(vietTat).trim(), safe(nsx).trim(),
                safe(hoatChat).trim(), vat, safe(hamLuong).trim(),
                safe(moTa).trim(), safe(dvt).trim(), giaBan
        );
    }

    public boolean capNhatSP(String id, String danhMuc, String dang, String ten, String vietTat,
                             String nsx, String hoatChat, double vat, String hamLuong,
                             String moTa, String dvt, double giaBan) {
        if (isBlank(id) || isBlank(ten) || vat < 0 || giaBan < 0) return false;
        return daoSanPham.capNhatSanPhamNhanh(
                id.trim(), safe(danhMuc).trim(), safe(dang).trim(),
                ten.trim(), safe(vietTat).trim(), safe(nsx).trim(),
                safe(hoatChat).trim(), vat, safe(hamLuong).trim(),
                safe(moTa).trim(), safe(dvt).trim(), giaBan
        );
    }

    public boolean xoaSP(String id) {
        return anSP(id);
    }

    public List<Object[]> layDanhSachChoBang() {
        return daoSanPham.layDanhSachSanPhamChoBang();
    }

    public List<SanPham> getDsThuoc() {
        return daoSanPham.getDsThuoc();
    }

    public boolean anSP(String maSP) {
        if (isBlank(maSP)) return false;
        int ton = daoSanPham.getSoLuongTon(maSP.trim());
        if (ton > 0) return false;
        return daoSanPham.anSanPham(maSP.trim());
    }

    public int getSoLuongTon(String maSP) {
        if (isBlank(maSP)) return 0;
        return daoSanPham.getSoLuongTon(maSP.trim());
    }

    public List<Object[]> layDanhSachSanPhamDaAn() {
        return daoSanPham.layDanhSachSanPhamDaAn();
    }

    public boolean khoiPhucSP(String maSP) {
        if (isBlank(maSP)) return false;
        return daoSanPham.khoiPhucSanPham(maSP.trim());
    }

    public SanPham getSanPhamDayDu(String maSP) {
        if (isBlank(maSP)) return null;
        return daoSanPham.getSanPhamDayDu(maSP.trim());
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    public boolean themSP(String id, String danhMuc, String dang, String ten, String vietTat,
                          String nsx, String hoatChat, double vat, String hamLuong,
                          String moTa, String dvt, double giaBan, List<Object[]> dsDonVi) {
        boolean ok = themSP(id, danhMuc, dang, ten, vietTat, nsx, hoatChat, vat, hamLuong, moTa, dvt, giaBan);
        if (ok) {
            daoSanPham.luuDonViQuyDoi(id.trim(), dvt, giaBan, dsDonVi);
        }
        return ok;
    }

    public boolean capNhatSP(String id, String danhMuc, String dang, String ten, String vietTat,
                             String nsx, String hoatChat, double vat, String hamLuong,
                             String moTa, String dvt, double giaBan, List<Object[]> dsDonVi) {
        boolean ok = capNhatSP(id, danhMuc, dang, ten, vietTat, nsx, hoatChat, vat, hamLuong, moTa, dvt, giaBan);
        if (ok) {
            daoSanPham.luuDonViQuyDoi(id.trim(), dvt, giaBan, dsDonVi);
        }
        return ok;
    }

    public List<Object[]> layDonViQuyDoiTheoSP(String maSP) {
        if (isBlank(maSP)) return new ArrayList<>();
        return daoSanPham.layDonViQuyDoiTheoSP(maSP.trim());
    }
    public List<String> layDanhSachNhaSanXuat() {
        return daoSanPham.layDanhSachNhaSanXuat();
    }

    public List<String> layDanhSachDonViTinh() {
        return daoSanPham.layDanhSachDonViTinh();
    }

    // ===========================================
    // HÀM MỚI BỔ SUNG: Gọi sang DAO lấy danh sách tên SP (Dùng cho Giao diện)
    // ===========================================
    public List<String> layDanhSachTenSanPham() {
        return daoSanPham.layDanhSachTenSanPham();
    }
}