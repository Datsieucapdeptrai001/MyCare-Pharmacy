package BUS;

import DAO.DAO_LoHang;
import DAO.DAO_SanPham;
import Entity.KhoHang;
import Entity.LoHang;
import Enumeration.TrangThaiLoHang;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BUS_Kho {
    private final DAO_LoHang daoLoHang;
    private final DAO_SanPham daoSanPham;

    public BUS_Kho() {
        this.daoLoHang = new DAO_LoHang();
        this.daoSanPham = new DAO_SanPham();
    }

    public boolean kiemTraTonKho(String maSP, int soLuongCanBan) {
        if (maSP == null || maSP.trim().isEmpty() || soLuongCanBan <= 0) {
            return false;
        }

        List<LoHang> dsLoHienCo = daoLoHang.layLoTheoSP(maSP);
        int tongTonKho = 0;

        for (LoHang lh : dsLoHienCo) {
            if (lh == null) {
                continue;
            }

            tongTonKho += Math.max(0, lh.getSoLuongLoHang());
        }

        return tongTonKho >= soLuongCanBan;
    }

    public List<LoHang> canhBaoHangSapHetHan(int soNgayCanhBao) {
        List<LoHang> dsToanBoLo = daoLoHang.layDSLoHang(true);
        List<LoHang> dsCanhBao = new ArrayList<>();

        LocalDateTime hienTai = LocalDateTime.now();
        LocalDateTime mocCanhBao = hienTai.plusDays(soNgayCanhBao);

        for (LoHang lh : dsToanBoLo) {
            if (lh == null || lh.getNgayHetHan() == null) {
                continue;
            }

            if (lh.getTrangThai() == TrangThaiLoHang.AN) {
                continue;
            }

            if (lh.getNgayHetHan().isAfter(hienTai)
                    && lh.getNgayHetHan().isBefore(mocCanhBao)
                    && lh.getSoLuongLoHang() > 0
                    && lh.getTrangThai() != TrangThaiLoHang.HET_HAN) {
                dsCanhBao.add(lh);
            }
        }

        return dsCanhBao;
    }

    public int xuLyXuatKhoFEFO(String maSP, int soLuongCanXuat, double heSoQuyDoi) {
        if (maSP == null || maSP.trim().isEmpty() || soLuongCanXuat <= 0) {
            throw new IllegalArgumentException("Mã sản phẩm hoặc số lượng cần xuất không hợp lệ.");
        }

        int soLuongCoSo = (int) Math.round(soLuongCanXuat * heSoQuyDoi);
        return daoLoHang.xuatKhoFEFO(maSP, soLuongCoSo);
    }

    public boolean kiemKeKho() {
        try {
            return daoLoHang.dongBoTrangThaiLoHang();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<LoHang> layDSLoHang() {
        return daoLoHang.layDSLoHang(false);
    }

    public List<LoHang> layDSLoHang(boolean hienLoAn) {
        return daoLoHang.layDSLoHang(hienLoAn);
    }

    public List<LoHang> layDSLoHangDaAn() {
        return daoLoHang.layDSLoHangDaAn();
    }

    public List<KhoHang> layDanhSachKhoHang() {
        return daoLoHang.layDanhSachKhoHang();
    }

    public List<LoHang> timLoHangChoXuatKho(String keyword) {
        if (isBlank(keyword)) {
            return new ArrayList<>();
        }

        return daoLoHang.timLoHangChoXuatKho(keyword.trim());
    }

    public boolean themLoHang(LoHang loHang) {
        if (loHang == null) {
            return false;
        }

        if (isBlank(loHang.getSoLoHang())) {
            return false;
        }

        if (loHang.getSanPhamId() == null || isBlank(loHang.getSanPhamId().getId())) {
            return false;
        }

        if (loHang.getKhoHangId() == null || isBlank(loHang.getKhoHangId().getId())) {
            return false;
        }

        if (loHang.getSoLuongLoHang() <= 0) {
            return false;
        }

        if (loHang.getGia() <= 0) {
            return false;
        }

        if (loHang.getNgayHetHan() == null) {
            return false;
        }

        if (loHang.getNgayNhap() == null) {
            loHang.setNgayNhap(LocalDateTime.now());
        }

        String maSP = loHang.getSanPhamId().getId().trim();

        if (daoSanPham.laSanPhamDaAn(maSP)) {
            return false;
        }

        if (loHang.getNgayNhap().isAfter(loHang.getNgayHetHan())) {
            return false;
        }

        loHang.setTrangThai(suyRaTrangThai(loHang.getSoLuongLoHang(), loHang.getNgayHetHan()));

        return daoLoHang.themLoHang(loHang);
    }

    public boolean anLoHang(String maLoHang) {
        if (isBlank(maLoHang)) {
            return false;
        }

        LoHang lo = daoLoHang.getLoHangTheoId(maLoHang);

        if (lo == null) {
            return false;
        }

        if (lo.getTrangThai() == TrangThaiLoHang.AN) {
            return true;
        }

        return daoLoHang.anLoHang(maLoHang);
    }

    public boolean khoiPhucLoHang(String maLoHang) {
        if (isBlank(maLoHang)) {
            return false;
        }

        LoHang lo = daoLoHang.getLoHangTheoId(maLoHang);

        if (lo == null) {
            return false;
        }

        if (lo.getTrangThai() != TrangThaiLoHang.AN) {
            return false;
        }

        if (lo.getSanPhamId() != null && !isBlank(lo.getSanPhamId().getId())) {
            if (daoSanPham.laSanPhamDaAn(lo.getSanPhamId().getId().trim())) {
                return false;
            }
        }

        return daoLoHang.khoiPhucLoHang(maLoHang);
    }

    public boolean capNhatSoLuongTon(String maLoHang, int soLuongMoi) {
        if (isBlank(maLoHang) || soLuongMoi < 0) {
            return false;
        }

        LoHang lo = daoLoHang.getLoHangTheoId(maLoHang);

        if (lo == null) {
            return false;
        }

        return daoLoHang.capNhatSoLuongVaTrangThaiLo(maLoHang, soLuongMoi);
    }

    public boolean capNhatTrangThaiLo(String maLoHang, TrangThaiLoHang trangThaiMoi) {
        if (isBlank(maLoHang) || trangThaiMoi == null) {
            return false;
        }

        return daoLoHang.capNhatTrangThaiLo(maLoHang, trangThaiMoi);
    }

    public boolean capNhatLoHetHang(String maLoHang) {
        if (isBlank(maLoHang)) {
            return false;
        }

        LoHang lo = daoLoHang.getLoHangTheoId(maLoHang);

        if (lo == null || lo.getTrangThai() == TrangThaiLoHang.AN) {
            return false;
        }

        return daoLoHang.capNhatSoLuongVaTrangThaiLo(maLoHang, 0);
    }

    public boolean tonTaiMaLoDangHoatDong(String soLoHang) {
        if (isBlank(soLoHang)) {
            return false;
        }

        LoHang lo = daoLoHang.getLoHangTheoSoLo(soLoHang.trim());

        return lo != null && lo.getTrangThai() != TrangThaiLoHang.AN;
    }

    public boolean tonTaiMaLoDaAn(String soLoHang) {
        if (isBlank(soLoHang)) {
            return false;
        }

        LoHang lo = daoLoHang.getLoHangTheoSoLo(soLoHang.trim());

        return lo != null && lo.getTrangThai() == TrangThaiLoHang.AN;
    }

    public boolean tonTaiKho(String maKho) {
        if (isBlank(maKho)) {
            return false;
        }

        return daoLoHang.tonTaiKho(maKho.trim());
    }

    public boolean tonTaiMaVachNoiBo(String maVachNoiBo) {
        if (isBlank(maVachNoiBo)) {
            return false;
        }

        return daoLoHang.tonTaiMaVachNoiBo(maVachNoiBo.trim());
    }

    public boolean xuatHuyKho(List<Object[]> danhSachXuat, String nguoiThucHien) {
        if (danhSachXuat == null || danhSachXuat.isEmpty()
                || nguoiThucHien == null || nguoiThucHien.trim().isEmpty()) {
            return false;
        }

        for (Object[] item : danhSachXuat) {
            if (item == null || item.length < 3) {
                return false;
            }

            String loHangId = item[0] == null ? "" : item[0].toString().trim();
            String soLuongText = item[1] == null ? "" : item[1].toString().trim();
            String lyDo = item[2] == null ? "" : item[2].toString().trim();

            if (isBlank(loHangId) || isBlank(soLuongText) || isBlank(lyDo)) {
                return false;
            }

            int soLuong;

            try {
                soLuong = Integer.parseInt(soLuongText);
            } catch (Exception e) {
                return false;
            }

            if (soLuong <= 0) {
                return false;
            }

            LoHang lo = daoLoHang.getLoHangTheoId(loHangId);

            if (lo == null) {
                return false;
            }

            if (lo.getTrangThai() == TrangThaiLoHang.AN) {
                return false;
            }

            if (lo.getSoLuongLoHang() < soLuong) {
                return false;
            }
        }

        return daoLoHang.thucThiXuatHuyKhoBangTransaction(danhSachXuat, nguoiThucHien.trim());
    }

    public List<Object[]> layLichSuXuatKho() {
        return daoLoHang.layLichSuXuatKho();
    }

    private TrangThaiLoHang suyRaTrangThai(int soLuong, LocalDateTime ngayHetHan) {
        LocalDateTime now = LocalDateTime.now();

        if (ngayHetHan != null && ngayHetHan.isBefore(now)) {
            return TrangThaiLoHang.HET_HAN;
        }

        if (soLuong <= 0) {
            return TrangThaiLoHang.HET_HANG;
        }

        return TrangThaiLoHang.CON_HANG;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
    public LoHang getLoHangTheoMaVachNoiBo(String maVachNoiBo) {
        if (maVachNoiBo == null || maVachNoiBo.trim().isEmpty()) {
            return null;
        }
        return daoLoHang.getLoHangTheoMaVachNoiBo(maVachNoiBo.trim());
    }
}