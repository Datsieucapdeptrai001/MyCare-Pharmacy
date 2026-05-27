package BUS;

import DAO.DAO_LoHang;
import DAO.DAO_SanPham;
import Entity.KhoHang;
import Entity.LoHang;
import Enumeration.TrangThaiLoHang;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
    public static class KetQuaXuatKho {
        private final boolean thanhCong;
        private final String maPhieuXuat;
        private final String ngayXuat;
        private final String qrData;
        private final String thongBao;

        public KetQuaXuatKho(boolean thanhCong, String maPhieuXuat, String ngayXuat, String qrData, String thongBao) {
            this.thanhCong = thanhCong;
            this.maPhieuXuat = maPhieuXuat;
            this.ngayXuat = ngayXuat;
            this.qrData = qrData;
            this.thongBao = thongBao;
        }

        public boolean isThanhCong() {
            return thanhCong;
        }

        public String getMaPhieuXuat() {
            return maPhieuXuat;
        }

        public String getNgayXuat() {
            return ngayXuat;
        }

        public String getQrData() {
            return qrData;
        }

        public String getThongBao() {
            return thongBao;
        }
    }

    public KetQuaXuatKho xuatHuyKhoVaTaoPhieu(List<Object[]> danhSachXuat, String nguoiThucHien) {
        if (danhSachXuat == null || danhSachXuat.isEmpty()) {
            return new KetQuaXuatKho(false, "", "", "", "Danh sách xuất kho đang trống.");
        }

        if (isBlank(nguoiThucHien)) {
            return new KetQuaXuatKho(false, "", "", "", "Không xác định được người thực hiện.");
        }

        /*
         * GUI có thể truyền thêm dữ liệu hiển thị sau index 3.
         * DAO hiện chỉ cần 3 cột:
         * 0: loHangId
         * 1: soLuongQuyDoi
         * 2: lyDo
         */
        List<Object[]> dsGhiDB = new ArrayList<>();

        for (Object[] item : danhSachXuat) {
            if (item == null || item.length < 3) {
                return new KetQuaXuatKho(false, "", "", "", "Dữ liệu xuất kho không hợp lệ.");
            }

            String loHangId = item[0] == null ? "" : item[0].toString().trim();
            String soLuongText = item[1] == null ? "" : item[1].toString().trim();
            String lyDo = item[2] == null ? "" : item[2].toString().trim();

            if (isBlank(loHangId) || isBlank(soLuongText) || isBlank(lyDo)) {
                return new KetQuaXuatKho(false, "", "", "", "Thiếu mã lô, số lượng hoặc lý do xuất.");
            }

            int soLuong;

            try {
                soLuong = Integer.parseInt(soLuongText);
            } catch (Exception e) {
                return new KetQuaXuatKho(false, "", "", "", "Số lượng xuất không hợp lệ.");
            }

            if (soLuong <= 0) {
                return new KetQuaXuatKho(false, "", "", "", "Số lượng xuất phải lớn hơn 0.");
            }

            LoHang lo = daoLoHang.getLoHangTheoId(loHangId);

            if (lo == null) {
                return new KetQuaXuatKho(false, "", "", "", "Không tìm thấy lô hàng: " + loHangId);
            }

            if (lo.getTrangThai() == TrangThaiLoHang.AN) {
                return new KetQuaXuatKho(false, "", "", "", "Lô hàng đã ẩn, không thể xuất: " + loHangId);
            }

            if (lo.getSoLuongLoHang() < soLuong) {
                return new KetQuaXuatKho(false, "", "", "", "Tồn kho không đủ cho lô: " + loHangId);
            }

            dsGhiDB.add(new Object[]{loHangId, soLuong, lyDo});
        }

        String maPhieuXuat = taoMaPhieuXuatTam();
        String ngayXuat = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        boolean ok = daoLoHang.thucThiXuatHuyKhoBangTransaction(dsGhiDB, nguoiThucHien.trim());

        if (!ok) {
            return new KetQuaXuatKho(false, "", "", "", "Gặp sự cố khi lưu phiếu xuất kho vào database.");
        }

        String qrData = taoQrDataPhieuXuat(maPhieuXuat, ngayXuat, danhSachXuat);

        return new KetQuaXuatKho(
                true,
                maPhieuXuat,
                ngayXuat,
                qrData,
                "Đã xuất kho thành công."
        );
    }

    private String taoMaPhieuXuatTam() {
        return "PX" + System.currentTimeMillis();
    }

    private String taoQrDataPhieuXuat(String maPhieuXuat, String ngayXuat, List<Object[]> danhSachXuat) {
        StringBuilder qrData = new StringBuilder();

        qrData.append("PHIẾU XUẤT: ").append(maPhieuXuat).append("\n");
        qrData.append("NGÀY XUẤT: ").append(ngayXuat).append("\n");
        qrData.append("--- CHI TIẾT HÀNG ---\n");

        for (Object[] item : danhSachXuat) {
            String loHangId = layGiaTriItem(item, 0);
            String soLuongQuyDoi = layGiaTriItem(item, 1);
            String lyDo = layGiaTriItem(item, 2);

            String maLoHienThi = layGiaTriItem(item, 3);
            String khoHienThi = layGiaTriItem(item, 4);
            String tenSanPham = layGiaTriItem(item, 5);
            String soLuongNhap = layGiaTriItem(item, 6);
            String donVi = layGiaTriItem(item, 7);

            if (isBlank(maLoHienThi)) {
                maLoHienThi = loHangId;
            }

            qrData.append("• ")
                    .append(maLoHienThi)
                    .append(" | Kho: ")
                    .append(khoHienThi)
                    .append(" | ")
                    .append(tenSanPham)
                    .append(" | ")
                    .append(isBlank(soLuongNhap) ? soLuongQuyDoi : soLuongNhap)
                    .append(" ")
                    .append(donVi)
                    .append(" | ")
                    .append(lyDo)
                    .append("\n");
        }

        return qrData.toString();
    }

    private String layGiaTriItem(Object[] item, int index) {
        if (item == null || index < 0 || index >= item.length || item[index] == null) {
            return "";
        }

        return item[index].toString().trim();
    }
}