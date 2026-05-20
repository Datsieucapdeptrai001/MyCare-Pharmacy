package BUS;

import DAO.DAO_KiemKeKho;

import java.util.ArrayList;
import java.util.List;

public class BUS_KiemKeKho {

    private final DAO_KiemKeKho daoKiemKeKho;

    public BUS_KiemKeKho() {
        this.daoKiemKeKho = new DAO_KiemKeKho();
    }

    public List<String> layDanhSachMaKho() {
        return daoKiemKeKho.layDanhSachMaKho();
    }

    public List<KiemKeItem> layDanhSachLoTheoKho(String khoHangId) {
        if (isBlank(khoHangId)) {
            return new ArrayList<>();
        }

        List<DAO_KiemKeKho.KiemKeItem> dsDAO =
                daoKiemKeKho.layDanhSachLoTheoKho(khoHangId);

        List<KiemKeItem> dsBUS = new ArrayList<>();

        if (dsDAO != null) {
            for (DAO_KiemKeKho.KiemKeItem itemDAO : dsDAO) {
                KiemKeItem itemBUS = new KiemKeItem(
                        itemDAO.getLoHangId(),
                        itemDAO.getSoLoHang(),
                        itemDAO.getSanPhamId(),
                        itemDAO.getTenSanPham(),
                        itemDAO.getKhoHangId(),
                        itemDAO.getTonHeThong()
                );

                itemBUS.setTonThucTe(itemDAO.getTonThucTe());
                itemBUS.setLyDo(itemDAO.getLyDo());

                dsBUS.add(itemBUS);
            }
        }

        return dsBUS;
    }

    public KetQuaKiemKe luuPhieuKiemKe(
            String khoHangId,
            String nhanVienId,
            String ghiChu,
            List<KiemKeItem> dsItem
    ) {
        if (isBlank(khoHangId)) {
            return new KetQuaKiemKe(false, null, "Chưa chọn kho kiểm kê.");
        }

        if (isBlank(nhanVienId)) {
            nhanVienId = "QL-0001";
        }

        if (dsItem == null || dsItem.isEmpty()) {
            return new KetQuaKiemKe(false, null, "Không có dữ liệu kiểm kê.");
        }

        for (KiemKeItem item : dsItem) {
            if (item.getTonThucTe() < 0) {
                return new KetQuaKiemKe(
                        false,
                        null,
                        "Tồn thực tế không được âm tại lô " + item.getSoLoHang()
                );
            }

            if (item.getChenhLech() != 0 && isBlank(item.getLyDo())) {
                return new KetQuaKiemKe(
                        false,
                        null,
                        "Lô " + item.getSoLoHang() + " có chênh lệch, vui lòng nhập lý do."
                );
            }
        }

        List<DAO_KiemKeKho.KiemKeItem> dsDAO = new ArrayList<>();

        for (KiemKeItem itemBUS : dsItem) {
            DAO_KiemKeKho.KiemKeItem itemDAO =
                    new DAO_KiemKeKho.KiemKeItem(
                            itemBUS.getLoHangId(),
                            itemBUS.getSoLoHang(),
                            itemBUS.getSanPhamId(),
                            itemBUS.getTenSanPham(),
                            itemBUS.getKhoHangId(),
                            itemBUS.getTonHeThong()
                    );

            itemDAO.setTonThucTe(itemBUS.getTonThucTe());
            itemDAO.setLyDo(itemBUS.getLyDo());

            dsDAO.add(itemDAO);
        }

        DAO_KiemKeKho.KetQuaKiemKe ketQuaDAO =
                daoKiemKeKho.luuPhieuKiemKe(
                        khoHangId,
                        nhanVienId,
                        ghiChu,
                        dsDAO
                );

        return new KetQuaKiemKe(
                ketQuaDAO.isThanhCong(),
                ketQuaDAO.getPhieuKiemKeId(),
                ketQuaDAO.getThongBao()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class KiemKeItem {
        private String loHangId;
        private String soLoHang;
        private String sanPhamId;
        private String tenSanPham;
        private String khoHangId;
        private int tonHeThong;
        private int tonThucTe;
        private int chenhLech;
        private String lyDo;

        public KiemKeItem(
                String loHangId,
                String soLoHang,
                String sanPhamId,
                String tenSanPham,
                String khoHangId,
                int tonHeThong
        ) {
            this.loHangId = loHangId;
            this.soLoHang = soLoHang;
            this.sanPhamId = sanPhamId;
            this.tenSanPham = tenSanPham;
            this.khoHangId = khoHangId;
            this.tonHeThong = tonHeThong;
            this.tonThucTe = tonHeThong;
            this.chenhLech = 0;
            this.lyDo = "";
        }

        public String getLoHangId() {
            return loHangId;
        }

        public String getSoLoHang() {
            return soLoHang;
        }

        public String getSanPhamId() {
            return sanPhamId;
        }

        public String getTenSanPham() {
            return tenSanPham;
        }

        public String getKhoHangId() {
            return khoHangId;
        }

        public int getTonHeThong() {
            return tonHeThong;
        }

        public int getTonThucTe() {
            return tonThucTe;
        }

        public void setTonThucTe(int tonThucTe) {
            this.tonThucTe = tonThucTe;
            this.chenhLech = tonThucTe - tonHeThong;
        }

        public int getChenhLech() {
            return chenhLech;
        }

        public String getLyDo() {
            return lyDo;
        }

        public void setLyDo(String lyDo) {
            this.lyDo = lyDo == null ? "" : lyDo.trim();
        }
    }

    public static class KetQuaKiemKe {
        private final boolean thanhCong;
        private final String phieuKiemKeId;
        private final String thongBao;

        public KetQuaKiemKe(
                boolean thanhCong,
                String phieuKiemKeId,
                String thongBao
        ) {
            this.thanhCong = thanhCong;
            this.phieuKiemKeId = phieuKiemKeId;
            this.thongBao = thongBao;
        }

        public boolean isThanhCong() {
            return thanhCong;
        }

        public String getPhieuKiemKeId() {
            return phieuKiemKeId;
        }

        public String getThongBao() {
            return thongBao;
        }
    }
}