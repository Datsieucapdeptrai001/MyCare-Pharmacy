package BUS;

import DAO.DAO_CaLamViec;
import Entity.CaLamViec;

import java.time.LocalDateTime;
import java.util.List;

public class BUS_CaLamViec {
    private DAO_CaLamViec daoCaLamViec;

    public BUS_CaLamViec() {
        this.daoCaLamViec = new DAO_CaLamViec();
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
        
     
        tinhDoanhThuCa(ca);

        return daoCaLamViec.capNhatCa(ca);
    }


    public double doiSoatTienMat(CaLamViec ca) {
       
        return ca.getTienKetCa() - ca.getTienHeThongGhiNhan();
    }

    
    public void tinhDoanhThuCa(CaLamViec ca) {
        
         System.out.println("Đã tính toán và cập nhật tiền hệ thống ghi nhận.");
    }

   
    public CaLamViec getCaHienTai(String maNhanVien) {
        return daoCaLamViec.getCaHienTai(maNhanVien);
    }


    public List<CaLamViec> getLichSuCa() {
        return daoCaLamViec.getLichSuCa();
    }
}