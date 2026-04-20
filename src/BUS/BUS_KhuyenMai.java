package BUS;

import DAO.DAO_DieuKienKhuyenMai;
import DAO.DAO_HinhThucKhuyenMai;
import DAO.DAO_KhuyenMai;
import DAO.DAO_SanPham; // Thêm kết nối tới DAO_SanPham
import Entity.KhuyenMai;
import Entity.DieuKienKhuyenMai;
import Entity.HinhThucKhuyenMai;
import Enumeration.*;

import java.util.List;
import java.time.LocalDateTime;

public class BUS_KhuyenMai {
    private DAO_KhuyenMai daoKhuyenMai;
    private DAO_DieuKienKhuyenMai daoDieuKienKhuyenMai;
    private DAO_HinhThucKhuyenMai daoHinhThucKhuyenMai;
    private DAO_SanPham daoSanPham; // Khai báo đối tượng DAO Sản Phẩm

    public BUS_KhuyenMai() {
        this.daoKhuyenMai = new DAO_KhuyenMai();
        this.daoDieuKienKhuyenMai = new DAO_DieuKienKhuyenMai();
        this.daoHinhThucKhuyenMai = new DAO_HinhThucKhuyenMai();
        this.daoSanPham = new DAO_SanPham(); // Khởi tạo
    }
    public boolean capNhatTrangThai(String maKM, boolean trangThai) {
        return daoKhuyenMai.capNhatTrangThai(maKM, trangThai);
    }
    public int[] layCauHinhTichDiem() {
        return daoKhuyenMai.layCauHinhTichDiem();
    }
    
    public boolean luuCauHinhTichDiem(int tienMua, int diemThuong, int tienDoi, int diemToiThieu) {
        return daoKhuyenMai.luuCauHinhTichDiem(tienMua, diemThuong, tienDoi, diemToiThieu);
    }

    public List<Object[]> layDanhSachKhuyenMaiChoTable() {
        return daoKhuyenMai.layDanhSachKhuyenMaiChoTable();
    }

    public List<KhuyenMai> layDsKhuyenMai() {
        return daoKhuyenMai.layDsKhuyenMai();
    }

    public boolean themKhuyenMai(KhuyenMai km) {
        if (!kiemTraThoiGian(km.getNgayBatDau(), km.getNgayKetThuc())) return false;
        if (kiemTraMaKM(km.getId())) return false;
        return daoKhuyenMai.themKhuyenMai(km);
    }
    
    public boolean themKhuyenMaiToanDien(KhuyenMai km, HinhThucKhuyenMai ht, DieuKienKhuyenMai dk) {
        if (!themKhuyenMai(km)) return false;
        daoHinhThucKhuyenMai.themHinhThuc(ht);
        if (dk != null) {
            daoDieuKienKhuyenMai.themDieuKien(dk);
        }
        return true;
    }

    public boolean capNhatKhuyenMaiToanDien(KhuyenMai km, HinhThucKhuyenMai ht, DieuKienKhuyenMai dk) {
        if (!kiemTraThoiGian(km.getNgayBatDau(), km.getNgayKetThuc())) return false;
        
        boolean isKmUpdated = daoKhuyenMai.capNhatKhuyenMai(km);
        if (isKmUpdated) {
            daoHinhThucKhuyenMai.capNhatHinhThuc(ht);
            if (dk != null) {
                if (daoDieuKienKhuyenMai.kiemTraTonTai(km.getId())) {
                    daoDieuKienKhuyenMai.capNhatDieuKienTheoMaKM(dk);
                } else {
                    daoDieuKienKhuyenMai.themDieuKien(dk);
                }
            } else {
                daoDieuKienKhuyenMai.xoaTheoMaKM(km.getId());
            }
            return true;
        }
        return false;
    }

    public boolean xoaKhuyenMaiToanDien(String maKM) {
        daoHinhThucKhuyenMai.xoaTheoMaKM(maKM);
        daoDieuKienKhuyenMai.xoaTheoMaKM(maKM);
        return daoKhuyenMai.xoaKhuyenMai(maKM);
    }

    public boolean kiemTraThoiGian(LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc) {
        if (ngayBatDau == null || ngayKetThuc == null) return false;
        return ngayBatDau.isBefore(ngayKetThuc);
    }

    public boolean kiemTraMaKM(String maKM) {
        return daoKhuyenMai.layMaKM(maKM) != null;
    }

    public boolean kiemTraDieuKienKhuyenMai(String maKM, double tongTienHoaDon) {
        KhuyenMai km = daoKhuyenMai.layMaKM(maKM);
        if (km == null) return false;

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(km.getNgayBatDau()) || now.isAfter(km.getNgayKetThuc())) return false;

        List<DieuKienKhuyenMai> dsDieuKien = daoDieuKienKhuyenMai.layTheoKhuyenMaiId(maKM);
        if (dsDieuKien == null || dsDieuKien.isEmpty()) return true;

        for (DieuKienKhuyenMai dk : dsDieuKien) {
            if (dk == null) continue;
            if ("HOA_DON".equalsIgnoreCase(dk.getDoiTuongApDung()) && "GIA_TRI".equalsIgnoreCase(dk.getLoaiDieuKien())) {
                if (tongTienHoaDon < dk.getGiaTri()) {
                    return false;
                }
            }
        }
        return true;
    }

    public double apDungKM(String maKM, double tongTienHoaDon) {
        if (!kiemTraDieuKienKhuyenMai(maKM, tongTienHoaDon)) return tongTienHoaDon;

        HinhThucKhuyenMai htkm = daoHinhThucKhuyenMai.layTheoMaKM(maKM);
        if (htkm == null) return tongTienHoaDon;

        if (htkm.getLoaiHinhThuc() == LoaiHinhThuc.GIAM_THEO_PHAN_TRAM && 
            htkm.getDoiTuongApDung() == DoiTuongApDung.HOA_DON) {
            
            double tienGiam = tongTienHoaDon * htkm.getGiaTri() / 100.0;
            if (htkm.getGiamToiDa() > 0 && tienGiam > htkm.getGiamToiDa()) {
                tienGiam = htkm.getGiamToiDa();
            }
            return tongTienHoaDon - tienGiam;
        }
        return tongTienHoaDon;
    }

    // ===========================================
    // HÀM MỚI: GỌI XUYÊN LỚP LẤY DANH SÁCH SẢN PHẨM TRẢ VỀ CHO GUI
    // ===========================================
    public List<String> layDanhSachTenSanPham() {
        return daoSanPham.layDanhSachTenSanPham();
    }
}