package BUS;

import DAO.DAO_NhapLoHangDongBo;
import Entity.LoHang;

import java.time.LocalDateTime;

public class BUS_NhapLoHangDongBo {

    private final DAO_NhapLoHangDongBo daoNhapLoHangDongBo;

    public BUS_NhapLoHangDongBo() {
        this.daoNhapLoHangDongBo = new DAO_NhapLoHangDongBo();
    }

    public KetQuaNhapLo luuNhapLoVaTaoPhieu(
            LoHang loHang,
            String maNhanVien,
            String ghiChu) {
        if (loHang == null) {
            return KetQuaNhapLo.loi("Dữ liệu lô hàng không hợp lệ.");
        }

        if (loHang.getSanPhamId() == null || isBlank(loHang.getSanPhamId().getId())) {
            return KetQuaNhapLo.loi("Chưa chọn sản phẩm.");
        }

        if (loHang.getKhoHangId() == null || isBlank(loHang.getKhoHangId().getId())) {
            return KetQuaNhapLo.loi("Chưa chọn kho hàng.");
        }

        if (isBlank(loHang.getSoLoHang())) {
            return KetQuaNhapLo.loi("Chưa nhập mã lô.");
        }

        if (loHang.getSoLuongLoHang() <= 0) {
            return KetQuaNhapLo.loi("Số lượng nhập phải lớn hơn 0.");
        }

        if (loHang.getGia() <= 0) {
            return KetQuaNhapLo.loi("Giá nhập phải lớn hơn 0.");
        }

        if (loHang.getNgayHetHan() == null) {
            return KetQuaNhapLo.loi("Chưa nhập hạn sử dụng.");
        }

        if (loHang.getNgayHetHan().isBefore(LocalDateTime.now())) {
            return KetQuaNhapLo.loi("Hạn sử dụng phải lớn hơn hoặc bằng ngày hiện tại.");
        }

        if (loHang.getNgayNhap() == null) {
            loHang.setNgayNhap(LocalDateTime.now());
        }

        if (isBlank(maNhanVien)) {
            maNhanVien = "QL-0001";
        }

        DAO_NhapLoHangDongBo.KetQuaDAO ketQuaDAO = daoNhapLoHangDongBo.luuNhapLoVaTaoPhieuNhap(
                loHang,
                maNhanVien,
                ghiChu);

        return new KetQuaNhapLo(
                ketQuaDAO.isThanhCong(),
                ketQuaDAO.isCongDonVaoLoCu(),
                ketQuaDAO.getLoHangId(),
                ketQuaDAO.getPhieuNhapId(),
                ketQuaDAO.getThongBao());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class KetQuaNhapLo {
        private final boolean thanhCong;
        private final boolean congDonVaoLoCu;
        private final String loHangId;
        private final String phieuNhapId;
        private final String thongBao;

        public KetQuaNhapLo(
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

        public static KetQuaNhapLo loi(String thongBao) {
            return new KetQuaNhapLo(
                    false,
                    false,
                    null,
                    null,
                    thongBao);
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
}