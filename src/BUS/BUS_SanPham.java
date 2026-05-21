package BUS;

import DAO.DAO_SanPham;
import Entity.LoHang;
import Entity.SanPham;

import java.util.ArrayList;
import java.util.List;

public class BUS_SanPham {
    private final DAO_SanPham daoSanPham = new DAO_SanPham();

    public BUS_SanPham() {
    }

    public List<SanPham> traCuuSanPham(String tuKhoa) {
        return daoSanPham.timKiemSanPhamDoiTra(tuKhoa);
    }

    // FIX #9: method riêng cho tìm thuốc phối vào mẫu liều (không cần tồn kho)
    public List<Object[]> timKiemThuocChoMauLieu(String text) {
        if (isBlank(text)) return new ArrayList<>();
        return daoSanPham.timKiemThuocChoMauLieu(text.trim());
    }

    public List<Object[]> timKiemSanPhamBan(String text) {
        if (isBlank(text))
            return new ArrayList<>();
        return daoSanPham.timKiemSanPhamBan(text.trim());
    }

    public double layThueVATTheoTenSP(String tenSP) {
        if (tenSP == null || tenSP.trim().isEmpty())
            return 0;
        return daoSanPham.layThueVATTheoTenSP(tenSP.trim());
    }

    public boolean kiemTraThongTinSP(SanPham sp) {
        if (sp == null)
            return false;
        if (isBlank(sp.getId()))
            return false;
        if (isBlank(sp.getTen()))
            return false;
        if (sp.getTen().trim().length() > 150)
            return false;
        if (sp.getThueVAT() < 0 || sp.getThueVAT() > 100)
            return false;
        if (sp.getGiaBan() < 0)
            return false;
        return true;
    }

    public double tinhGiaBanTheoDonVi(String maSP, String donViMuonBan) {
        if (isBlank(maSP) || isBlank(donViMuonBan))
            return 0.0;

        List<Object[]> list = daoSanPham.layDonViQuyDoiTheoSP(maSP.trim());

        for (Object[] row : list) {
            if (donViMuonBan.trim().equalsIgnoreCase(row[0].toString())) {
                try {
                    return Double.parseDouble(row[2].toString().replace(",", "").trim());
                } catch (Exception ignored) {
                }
            }
        }

        SanPham sp = daoSanPham.getSanPhamDayDu(maSP.trim());
        return sp != null ? sp.getGiaBan() : 0.0;
    }

    public List<LoHang> layTatCaLoTheoSP(String maSP) {
        if (isBlank(maSP))
            return new ArrayList<>();
        return daoSanPham.layTatCaLoTheoSP(maSP.trim());
    }

    public List<LoHang> layLoTheoSP(String maSP) {
        if (isBlank(maSP))
            return new ArrayList<>();
        return daoSanPham.layLoTheoSP(maSP.trim());
    }

    public List<Object[]> layDonViTheoSP(String maSP) {
        if (isBlank(maSP))
            return new ArrayList<>();
        return daoSanPham.layDonViDoLuongTheoSP(maSP.trim());
    }

    public String taoMaMoi() {
        return daoSanPham.layMaSanPhamMoiNhat();
    }

    public boolean themSP(String id, String danhMuc, String dang, String ten, String vietTat,
            String nsx, String hoatChat, double vat, String hamLuong,
            String moTa, String dvt, double giaBan,
            String maVach, String nhomBenhLy) {
        if (isBlank(id) || isBlank(ten) || vat < 0 || giaBan < 0)
            return false;

        return daoSanPham.themSanPhamNhanh(
                id.trim(),
                safe(danhMuc).trim(),
                safe(dang).trim(),
                ten.trim(),
                safe(vietTat).trim(),
                safe(nsx).trim(),
                safe(hoatChat).trim(),
                vat,
                safe(hamLuong).trim(),
                safe(moTa).trim(),
                safe(dvt).trim(),
                giaBan,
                safe(maVach).trim(),
                safe(nhomBenhLy).trim());
    }

    public boolean themSP(String id, String danhMuc, String dang, String ten, String vietTat,
            String nsx, String hoatChat, double vat, String hamLuong,
            String moTa, String dvt, double giaBan,
            String maVach, String nhomBenhLy, List<Object[]> dsDonVi) {
        return themSP(id, danhMuc, dang, ten, vietTat, nsx, hoatChat, vat,
                hamLuong, moTa, dvt, giaBan, maVach, nhomBenhLy, "", dsDonVi);
    }

    /** Overload chính — có viTriId */
    public boolean themSP(String id, String danhMuc, String dang, String ten, String vietTat,
            String nsx, String hoatChat, double vat, String hamLuong,
            String moTa, String dvt, double giaBan,
            String maVach, String nhomBenhLy, String viTriId, List<Object[]> dsDonVi) {
        if (isBlank(id) || isBlank(ten) || vat < 0 || giaBan < 0) return false;
        boolean ok = daoSanPham.themSanPhamNhanh(
                id.trim(), safe(danhMuc).trim(), safe(dang).trim(), ten.trim(),
                safe(vietTat).trim(), safe(nsx).trim(), safe(hoatChat).trim(), vat,
                safe(hamLuong).trim(), safe(moTa).trim(), safe(dvt).trim(), giaBan,
                safe(maVach).trim(), safe(nhomBenhLy).trim(), safe(viTriId).trim());
        if (ok) daoSanPham.luuDonViQuyDoi(id.trim(), dvt, giaBan, dsDonVi);
        return ok;
    }

    public boolean themSP(String id, String danhMuc, String dang, String ten, String vietTat,
            String nsx, String hoatChat, double vat, String hamLuong,
            String moTa, String dvt, double giaBan, List<Object[]> dsDonVi) {
        return themSP(id, danhMuc, dang, ten, vietTat, nsx, hoatChat, vat,
                hamLuong, moTa, dvt, giaBan, "", "", dsDonVi);
    }

    public boolean capNhatSP(String id, String danhMuc, String dang, String ten, String vietTat,
            String nsx, String hoatChat, double vat, String hamLuong,
            String moTa, String dvt, double giaBan,
            String maVach, String nhomBenhLy) {
        if (isBlank(id) || isBlank(ten) || vat < 0 || giaBan < 0)
            return false;
        return daoSanPham.capNhatSanPhamNhanh(
                id.trim(), safe(danhMuc).trim(), safe(dang).trim(), ten.trim(),
                safe(vietTat).trim(), safe(nsx).trim(), safe(hoatChat).trim(), vat,
                safe(hamLuong).trim(), safe(moTa).trim(), safe(dvt).trim(), giaBan,
                safe(maVach).trim(), safe(nhomBenhLy).trim());
    }

    public boolean capNhatSP(String id, String danhMuc, String dang, String ten, String vietTat,
            String nsx, String hoatChat, double vat, String hamLuong,
            String moTa, String dvt, double giaBan,
            String maVach, String nhomBenhLy, List<Object[]> dsDonVi) {
        return capNhatSP(id, danhMuc, dang, ten, vietTat, nsx, hoatChat, vat,
                hamLuong, moTa, dvt, giaBan, maVach, nhomBenhLy, "", dsDonVi);
    }

    /** Overload chính — có viTriId */
    public boolean capNhatSP(String id, String danhMuc, String dang, String ten, String vietTat,
            String nsx, String hoatChat, double vat, String hamLuong,
            String moTa, String dvt, double giaBan,
            String maVach, String nhomBenhLy, String viTriId, List<Object[]> dsDonVi) {
        if (isBlank(id) || isBlank(ten) || vat < 0 || giaBan < 0) return false;
        boolean ok = daoSanPham.capNhatSanPhamNhanh(
                id.trim(), safe(danhMuc).trim(), safe(dang).trim(), ten.trim(),
                safe(vietTat).trim(), safe(nsx).trim(), safe(hoatChat).trim(), vat,
                safe(hamLuong).trim(), safe(moTa).trim(), safe(dvt).trim(), giaBan,
                safe(maVach).trim(), safe(nhomBenhLy).trim(), safe(viTriId).trim());
        if (ok) daoSanPham.luuDonViQuyDoi(id.trim(), dvt, giaBan, dsDonVi);
        return ok;
    }

    public boolean capNhatSP(String id, String danhMuc, String dang, String ten, String vietTat,
            String nsx, String hoatChat, double vat, String hamLuong,
            String moTa, String dvt, double giaBan, List<Object[]> dsDonVi) {
        return capNhatSP(id, danhMuc, dang, ten, vietTat, nsx, hoatChat, vat,
                hamLuong, moTa, dvt, giaBan, "", "", dsDonVi);
    }

    public boolean capNhatMaVachSanPham(String maSP, String maVachMoi) {
        System.out.println("=== DEBUG BUS_SanPham.capNhatMaVachSanPham ===");
        System.out.println("maSP = [" + maSP + "]");
        System.out.println("maVachMoi = [" + maVachMoi + "]");

        if (isBlank(maSP) || isBlank(maVachMoi)) {
            System.out.println("BUS result = false vì mã sản phẩm hoặc mã vạch rỗng");
            return false;
        }

        boolean ok = daoSanPham.capNhatMaVachSanPham(maSP.trim(), maVachMoi.trim());

        System.out.println("BUS result = " + ok);

        return ok;
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
        if (isBlank(maSP))
            return false;

        int ton = daoSanPham.getSoLuongTon(maSP.trim());

        if (ton > 0)
            return false;

        return daoSanPham.anSanPham(maSP.trim());
    }

    public int getSoLuongTon(String maSP) {
        if (isBlank(maSP))
            return 0;
        return daoSanPham.getSoLuongTon(maSP.trim());
    }

    public List<Object[]> layDanhSachSanPhamDaAn() {
        return daoSanPham.layDanhSachSanPhamDaAn();
    }

    public boolean khoiPhucSP(String maSP) {
        if (isBlank(maSP))
            return false;
        return daoSanPham.khoiPhucSanPham(maSP.trim());
    }

    public SanPham getSanPhamDayDu(String maSP) {
        if (isBlank(maSP))
            return null;
        return daoSanPham.getSanPhamDayDu(maSP.trim());
    }

    public List<Object[]> layDonViQuyDoiTheoSP(String maSP) {
        if (isBlank(maSP))
            return new ArrayList<>();
        return daoSanPham.layDonViQuyDoiTheoSP(maSP.trim());
    }

    public List<String> layDanhSachNhaSanXuat() {
        return daoSanPham.layDanhSachNhaSanXuat();
    }

    public List<String> layDanhSachDonViTinh() {
        return daoSanPham.layDanhSachDonViTinh();
    }

    public List<String> layDanhSachTenSanPham() {
        return daoSanPham.layDanhSachTenSanPham();
    }

    public List<String> layDanhSachNhomBenhLy() {
        return daoSanPham.layDanhSachNhomBenhLy();
    }

    public boolean kiemTraMaSPTonTai(String id) {
        if (isBlank(id))
            return false;
        return daoSanPham.kiemTraMaSPTonTai(id.trim());
    }
    public boolean kiemTraMaVachTonTai(String maVach, String maSPBoQua) {
        if (isBlank(maVach)) {
            return false;
        }
        return daoSanPham.kiemTraMaVachTonTai(maVach.trim(), maSPBoQua);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // =========================================================================
    // VỊ TRÍ THUỐC — gộp vào BUS_SanPham, dùng SanPham.ViTriThuoc (inner class)
    // =========================================================================

    public List<SanPham.ViTriThuoc> layTatCaViTri() {
        return daoSanPham.layTatCaViTri();
    }

    public SanPham.ViTriThuoc layViTriTheoId(String id) {
        if (isBlank(id)) return null;
        return daoSanPham.layViTriTheoId(id.trim());
    }

    public List<String> layDanhSachKhuViTri() {
        return daoSanPham.layDanhSachKhuViTri();
    }

    public boolean themViTri(SanPham.ViTriThuoc vt) {
        if (vt == null || isBlank(vt.getId()) || isBlank(vt.getKhu()) || isBlank(vt.getKe()))
            return false;
        return daoSanPham.themViTri(vt);
    }

    public boolean capNhatViTri(SanPham.ViTriThuoc vt) {
        if (vt == null || isBlank(vt.getId())) return false;
        return daoSanPham.capNhatViTri(vt);
    }

    /**
     * Xóa vị trí có kiểm tra nghiệp vụ.
     * Trả về: "OK" | "CON_SP" | "LOI"
     */
    public String xoaViTri(String id) {
        if (isBlank(id)) return "LOI";
        int kq = daoSanPham.xoaViTri(id.trim());
        if (kq == 0)  return "OK";
        if (kq == -1) return "CON_SP";
        return "LOI";
    }

    public boolean ganViTriChoSP(String maSP, String viTriId) {
        if (isBlank(maSP)) return false;
        return daoSanPham.ganViTriChoSP(maSP.trim(), viTriId);
    }

    public String layIdViTriMoi() {
        return daoSanPham.layIdViTriMoi();
    }
 // =========================================================================
    // NGHIỆP VỤ CẮT LIỀU CHUYÊN SÂU (TỪ BUS_MAULIEU)
    // =========================================================================
    public SanPham getSanPhamByBarcode(String maVach) {
        if (maVach == null || maVach.trim().isEmpty()) return null;
        return daoSanPham.getSanPhamByBarcode(maVach.trim());
    }

    public Object[] getDonViNhoNhat(String maSP) {
        if (maSP == null || maSP.trim().isEmpty()) return null;
        return daoSanPham.getDonViNhoNhat(maSP.trim());
    }

    public List<Object[]> layDanhSachComboNangCao() {
        return daoSanPham.layDanhSachComboNangCao();
    }

    public List<String> layDanhSachNhomBenhLieu() {
        return daoSanPham.layDanhSachNhomBenhLieu();
    }

    public SanPham.MauLieu layComboByIdNangCao(String comboId) {
        if (comboId == null || comboId.trim().isEmpty()) return null;
        return daoSanPham.layComboByIdNangCao(comboId.trim());
    }

    public String themMauMoiNangCao(String tenCombo, String nhomBenh, double giaBanCombo, String ghiChu, List<SanPham.ChiTietLieu> dsChiTiet) {
        if (tenCombo == null || tenCombo.trim().isEmpty() || dsChiTiet == null || dsChiTiet.isEmpty()) return null;
        String id = daoSanPham.sinhComboIdMoi();
        SanPham.MauLieu m = new SanPham.MauLieu(id, tenCombo.trim(), nhomBenh, giaBanCombo, ghiChu);
        for (SanPham.ChiTietLieu ct : dsChiTiet) ct.setComboId(id);
        return daoSanPham.taoMauMoiNangCao(m, dsChiTiet) ? id : null;
    }

    public boolean capNhatMauNangCao(String comboId, String tenCombo, String nhomBenh, double giaBanCombo, String ghiChu, List<SanPham.ChiTietLieu> dsChiTiet) {
        if (comboId == null || tenCombo == null || dsChiTiet == null || dsChiTiet.isEmpty()) return false;
        SanPham.MauLieu m = new SanPham.MauLieu(comboId.trim(), tenCombo.trim(), nhomBenh, giaBanCombo, ghiChu);
        for (SanPham.ChiTietLieu ct : dsChiTiet) ct.setComboId(comboId.trim());
        return daoSanPham.capNhatMauNangCao(m, dsChiTiet);
    }

    public boolean xoaMauNangCao(String comboId) {
        if (comboId == null || comboId.trim().isEmpty()) return false;
        return daoSanPham.xoaMauNangCao(comboId.trim());
    }
    public boolean kiemTraTenComboTonTai(String tenCombo) {
        if (tenCombo == null || tenCombo.trim().isEmpty()) return false;
        return daoSanPham.kiemTraTenComboTonTai(tenCombo.trim());
    }
}