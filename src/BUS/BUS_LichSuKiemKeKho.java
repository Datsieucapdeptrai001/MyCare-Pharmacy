package BUS;

import DAO.DAO_LichSuKiemKeKho;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class BUS_LichSuKiemKeKho {

    private final DAO_LichSuKiemKeKho dao = new DAO_LichSuKiemKeKho();

    public List<PhieuKiemKeHistory> layDanhSachPhieuKiemKe() {
        List<DAO_LichSuKiemKeKho.PhieuKiemKeHistory> dsDAO = dao.layDanhSachPhieuKiemKe();
        List<PhieuKiemKeHistory> dsBUS = new ArrayList<>();

        for (DAO_LichSuKiemKeKho.PhieuKiemKeHistory x : dsDAO) {
            dsBUS.add(new PhieuKiemKeHistory(
                    x.getId(),
                    x.getNgayKiemKe(),
                    x.getKhoHangId(),
                    x.getNhanVienId(),
                    x.getTenNhanVien(),
                    x.getTongSoDong(),
                    x.getTongChenhLech(),
                    x.getGhiChu(),
                    x.getTrangThai()
            ));
        }

        return dsBUS;
    }

    public List<ChiTietKiemKeHistory> layChiTietTheoPhieu(String phieuKiemKeId) {
        if (phieuKiemKeId == null || phieuKiemKeId.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<DAO_LichSuKiemKeKho.ChiTietKiemKeHistory> dsDAO = dao.layChiTietTheoPhieu(phieuKiemKeId);
        List<ChiTietKiemKeHistory> dsBUS = new ArrayList<>();

        for (DAO_LichSuKiemKeKho.ChiTietKiemKeHistory x : dsDAO) {
            dsBUS.add(new ChiTietKiemKeHistory(
                    x.getId(),
                    x.getPhieuKiemKeId(),
                    x.getLoHangId(),
                    x.getSoLoHang(),
                    x.getSanPhamId(),
                    x.getTenSanPham(),
                    x.getKhoHangId(),
                    x.getTonHeThong(),
                    x.getTonThucTe(),
                    x.getChenhLech(),
                    x.getLyDo()
            ));
        }

        return dsBUS;
    }

    public static class PhieuKiemKeHistory {
        private final String id;
        private final Timestamp ngayKiemKe;
        private final String khoHangId;
        private final String nhanVienId;
        private final String tenNhanVien;
        private final int tongSoDong;
        private final int tongChenhLech;
        private final String ghiChu;
        private final String trangThai;

        public PhieuKiemKeHistory(
                String id,
                Timestamp ngayKiemKe,
                String khoHangId,
                String nhanVienId,
                String tenNhanVien,
                int tongSoDong,
                int tongChenhLech,
                String ghiChu,
                String trangThai
        ) {
            this.id = id;
            this.ngayKiemKe = ngayKiemKe;
            this.khoHangId = khoHangId;
            this.nhanVienId = nhanVienId;
            this.tenNhanVien = tenNhanVien;
            this.tongSoDong = tongSoDong;
            this.tongChenhLech = tongChenhLech;
            this.ghiChu = ghiChu;
            this.trangThai = trangThai;
        }

        public String getId() { return id; }
        public Timestamp getNgayKiemKe() { return ngayKiemKe; }
        public String getKhoHangId() { return khoHangId; }
        public String getNhanVienId() { return nhanVienId; }
        public String getTenNhanVien() { return tenNhanVien; }
        public int getTongSoDong() { return tongSoDong; }
        public int getTongChenhLech() { return tongChenhLech; }
        public String getGhiChu() { return ghiChu; }
        public String getTrangThai() { return trangThai; }
    }

    public static class ChiTietKiemKeHistory {
        private final String id;
        private final String phieuKiemKeId;
        private final String loHangId;
        private final String soLoHang;
        private final String sanPhamId;
        private final String tenSanPham;
        private final String khoHangId;
        private final int tonHeThong;
        private final int tonThucTe;
        private final int chenhLech;
        private final String lyDo;

        public ChiTietKiemKeHistory(
                String id,
                String phieuKiemKeId,
                String loHangId,
                String soLoHang,
                String sanPhamId,
                String tenSanPham,
                String khoHangId,
                int tonHeThong,
                int tonThucTe,
                int chenhLech,
                String lyDo
        ) {
            this.id = id;
            this.phieuKiemKeId = phieuKiemKeId;
            this.loHangId = loHangId;
            this.soLoHang = soLoHang;
            this.sanPhamId = sanPhamId;
            this.tenSanPham = tenSanPham;
            this.khoHangId = khoHangId;
            this.tonHeThong = tonHeThong;
            this.tonThucTe = tonThucTe;
            this.chenhLech = chenhLech;
            this.lyDo = lyDo;
        }

        public String getId() { return id; }
        public String getPhieuKiemKeId() { return phieuKiemKeId; }
        public String getLoHangId() { return loHangId; }
        public String getSoLoHang() { return soLoHang; }
        public String getSanPhamId() { return sanPhamId; }
        public String getTenSanPham() { return tenSanPham; }
        public String getKhoHangId() { return khoHangId; }
        public int getTonHeThong() { return tonHeThong; }
        public int getTonThucTe() { return tonThucTe; }
        public int getChenhLech() { return chenhLech; }
        public String getLyDo() { return lyDo; }
    }
}