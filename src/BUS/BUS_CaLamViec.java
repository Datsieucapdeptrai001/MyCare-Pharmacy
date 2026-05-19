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

        // Tính tienHeThongGhiNhan bằng SQL tổng hợp → set vào ca trước khi lưu
        tinhDoanhThuCa(ca);

        return daoCaLamViec.capNhatCa(ca);
    }

    // Tính doanh thu ca 
    public void tinhDoanhThuCa(CaLamViec ca) {
        if (ca == null || ca.getId() == null || ca.getId().isBlank()) {
            System.out.println("Cảnh báo: tinhDoanhThuCa() nhận ca null hoặc thiếu id.");
            return;
        }

        // Một lần gọi DB duy nhất — trả về tienDauCa + net cash flow trong ca
        double tienHeThong = daoCaLamViec.tinhTienMatThucTeTrongCa(ca.getId());
        ca.setTienHeThongGhiNhan(tienHeThong);
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

    /**
     * Lấy ca làm việc đã đóng gần nhất của một nhân viên.
     * Dùng cho tính năng xem lại Bill Kết Ca ở ManHinhThongKe.
     *
     * @param maNV  mã nhân viên
     * @return      CaLamViec gần nhất đã đóng, hoặc null
     */
    public CaLamViec getCaDaKetThucGanNhat(String maNV) {
        if (maNV == null || maNV.isBlank()) return null;
        return daoCaLamViec.getCaDaKetThucGanNhat(maNV);
    }
}