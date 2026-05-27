package BUS;

import DAO.DAO_CaLamViec;
import Entity.CaLamViec;

import java.time.LocalDateTime;
import java.util.List;

public class BUS_CaLamViec {

    private final DAO_CaLamViec daoCaLamViec;

    public BUS_CaLamViec() {
        this.daoCaLamViec = new DAO_CaLamViec();
    }

    // Mở ca
    public CaLamViec layCaDangMo(String maNV) {
        return daoCaLamViec.getCaHienTai(maNV);
    }

    public boolean themCa(CaLamViec ca) {
        if (ca == null || ca.getNhanVienId() == null) return false;
        return moCa(ca);
    }

    public boolean moCa(CaLamViec ca) {
        String maNV = ca.getNhanVienId().getNhanVien();

        // Kiểm tra ca chưa đóng
        CaLamViec caDangMo = daoCaLamViec.getCaHienTai(maNV);
        if (caDangMo != null) {
            System.out.println("Lỗi: Nhân viên đang có một ca làm việc chưa kết thúc!");
            return false;
        }

        // Validate tiền đầu ca
        if (ca.getTienDauCa() < 0) {
            System.out.println("Lỗi: Tiền đầu ca không hợp lệ (không được âm).");
            return false;
        }

        if (ca.getThoiGianBatDau() == null) {
            ca.setThoiGianBatDau(LocalDateTime.now());
        }

        // Khi mở ca: tienHeThongGhiNhan = tienDauCa (chưa có giao dịch nào)
        ca.setTienHeThongGhiNhan(ca.getTienDauCa());
        ca.setThoiGianKetThuc(null);

        return daoCaLamViec.themCa(ca);
    }

    // Kết ca
    public boolean ketThucCa(CaLamViec ca) {
        if (ca.getThoiGianKetThuc() == null) {
            ca.setThoiGianKetThuc(LocalDateTime.now());
        }

        if (ca.getTienKetCa() < 0) {
            System.out.println("Lỗi: Tiền thực tế kết ca không hợp lệ.");
            return false;
        }

        // Tính tienHeThongGhiNhan theo nghiệp vụ → set vào ca trước khi lưu
        tinhDoanhThuCa(ca);

        return daoCaLamViec.capNhatCa(ca);
    }

    /**
     * Tính tienHeThongGhiNhan theo nghiệp vụ:
     *   tienHeThong = tienDauCa
     *               + Σ tongTienHienTai  (BAN_HANG)   ← tiền vào
     *               + Σ (tongTienHienTai − tongTienGoc) (DOI_HANG) ← chênh lệch
     *               − Σ tongTienHienTai  (TRA_HANG)   ← tiền ra
     *
     * DAO chỉ cung cấp dữ liệu thô (loaiHD, tongTienHienTai, tongTienGoc).
     * Toàn bộ logic tính toán nằm ở đây (BUS).
     */
    public void tinhDoanhThuCa(CaLamViec ca) {
        if (ca == null || ca.getId() == null || ca.getId().isBlank()) {
            System.out.println("Cảnh báo: tinhDoanhThuCa() nhận ca null hoặc thiếu id.");
            return;
        }

        List<Object[]> dsHoaDon = daoCaLamViec.getHoaDonTienMatTrongCa(ca.getId());

        double netFlow = 0;
        for (Object[] row : dsHoaDon) {
            String loaiHD          = (String) row[0];
            double tongTienHienTai = (double) row[1];
            double tongTienGoc     = (double) row[2];

            switch (loaiHD) {
                case "BAN_HANG" -> netFlow += tongTienHienTai;
                case "TRA_HANG" -> netFlow -= tongTienHienTai;
                case "DOI_HANG" -> netFlow += (tongTienHienTai - tongTienGoc);
            }
        }

        ca.setTienHeThongGhiNhan(ca.getTienDauCa() + netFlow);
    }

    // Đối soát
    public double doiSoatTienMat(CaLamViec ca) {
        return ca.getTienKetCa() - ca.getTienHeThongGhiNhan();
    }

    public CaLamViec getCaHienTai(String maNhanVien) {
        return daoCaLamViec.getCaHienTai(maNhanVien);
    }

    public List<CaLamViec> getLichSuCa() {
        return daoCaLamViec.getLichSuCa();
    }
}