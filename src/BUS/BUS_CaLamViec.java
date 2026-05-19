package BUS;

import DAO.DAO_CaLamViec;
import Entity.CaLamViec;

import java.time.LocalDateTime;
import java.util.List;

public class BUS_CaLamViec {
    private DAO_CaLamViec daoCaLamViec;
    // BUG 2 FIX: inject BUS_ThongKe để tính doanh thu ca chính xác
    private BUS_ThongKe busThongKe;

    public BUS_CaLamViec() {
        this.daoCaLamViec = new DAO_CaLamViec();
        this.busThongKe   = new BUS_ThongKe();
    }
 
    public CaLamViec layCaDangMo(String maNV) {
        return daoCaLamViec.getCaHienTai(maNV); 
    }

    public boolean themCa(CaLamViec ca) {
        if (ca == null || ca.getNhanVienId() == null) return false;
        return moCa(ca);
    }

    public boolean moCa(CaLamViec ca) {
        String maNV = ca.getNhanVienId().getNhanVien();
        CaLamViec caDangMo = daoCaLamViec.getCaHienTai(maNV);
        
        if (caDangMo != null) {
            System.out.println("Lỗi: Nhân viên đang có một ca làm việc chưa kết thúc!");
            return false;
        }

        if (ca.getTienDauCa() < 0) {
            System.out.println("Lỗi: Tiền đầu ca không hợp lệ (không được âm).");
            return false;
        }

        if (ca.getThoiGianBatDau() == null) {
            ca.setThoiGianBatDau(LocalDateTime.now());
        }
        
        ca.setTienHeThongGhiNhan(ca.getTienDauCa());
        ca.setThoiGianKetThuc(null);

        return daoCaLamViec.themCa(ca);
    }

    public boolean ketThucCa(CaLamViec ca) {
        if (ca.getThoiGianKetThuc() == null) {
            ca.setThoiGianKetThuc(LocalDateTime.now());
        }
        
        if (ca.getTienKetCa() < 0) {
            System.out.println("Lỗi: Tiền thực tế kết ca không hợp lệ.");
            return false;
        }
        
        // Tính và cập nhật tienHeThongGhiNhan trước khi lưu
        tinhDoanhThuCa(ca);

        return daoCaLamViec.capNhatCa(ca);
    }

    public double doiSoatTienMat(CaLamViec ca) {
        return ca.getTienKetCa() - ca.getTienHeThongGhiNhan();
    }

    /**
     * BUG 2 FIX: Tính tienHeThongGhiNhan chính xác dựa trên doanh thu thực tế
     * trong ca, bao gồm bán hàng, đổi hàng và trả hàng bằng tiền mặt.
     *
     * Công thức:
     *   tienHeThong = tienDauCa
     *                 + tienBanMat          (thu vào từ bán hàng TM)
     *                 + doiMat[0]           (khách bù thêm khi đổi hàng TM)
     *                 - doiMat[1]           (tiệm hoàn lại khi đổi hàng TM)
     *                 - tienTraMat          (hoàn tiền cho khách trả hàng TM)
     *
     * Sau khi gọi xong, ketThucCa() sẽ tự gọi daoCaLamViec.capNhatCa(ca)
     * nên method này chỉ cần set giá trị vào ca, không tự lưu DB.
     */
    public void tinhDoanhThuCa(CaLamViec ca) {
        String maNV = ca.getNhanVienId().getNhanVien();
        LocalDateTime start = ca.getThoiGianBatDau();

        // Tiền thu được từ bán hàng tiền mặt trong ca
        double tienBanMat = busThongKe.getTienMatBanHangTheoCa(maNV, start);

        // Tiền đổi hàng tiền mặt: [0]=khách bù thêm, [1]=tiệm hoàn lại
        double[] doiMat = busThongKe.getTienDoiHangMatTheoCa(maNV, start);

        // Tiền hoàn trả cho khách (trả hàng tiền mặt) trong ca
        double tienTraMat = busThongKe.getTienHoanTraTheoCa(maNV, start);

        double tienHeThong = ca.getTienDauCa()
                + tienBanMat
                + doiMat[0]   // bù thêm → tiền vào quỹ
                - doiMat[1]   // hoàn lại → tiền ra quỹ
                - tienTraMat; // hoàn trả hàng → tiền ra quỹ

        ca.setTienHeThongGhiNhan(tienHeThong);
    }

    public CaLamViec getCaHienTai(String maNhanVien) {
        return daoCaLamViec.getCaHienTai(maNhanVien);
    }

    public List<CaLamViec> getLichSuCa() {
        return daoCaLamViec.getLichSuCa();
    }
}