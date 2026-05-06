package BUS;

import DAO.DAO_HoaDon;
import DAO.DAO_ChiTietHoaDon;
import Entity.HoaDon;
import Enumeration.LoaiHoaDon;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BUS_TraHang {
    private DAO_HoaDon daoHoaDon;
    private DAO_ChiTietHoaDon daoCTHD; 

    public BUS_TraHang() {
        this.daoHoaDon = new DAO_HoaDon();
        this.daoCTHD = new DAO_ChiTietHoaDon(); 
    }

    public boolean kiemTraDieuKien(String maHoaDonGoc) {
        HoaDon hd = daoHoaDon.layHoaDonTheoMa(maHoaDonGoc);
        if (hd == null) return false;
        if (hd.getLoaiHD() != LoaiHoaDon.BAN_HANG) return false;
        
        LocalDateTime ngayHetHanTra = hd.getNgayLapHD().plusDays(7);
        return !LocalDateTime.now().isAfter(ngayHetHanTra);
    }

    public double xacDinhMucHoanTien(double tongTienHD, double phanTramHoan) {
        return tongTienHD * (phanTramHoan / 100.0);
    }

    public double tinhTienChenhLech(double tienKhachMua, double tienHoanLai) {
        return tienKhachMua - tienHoanLai;
    }

    public List<Object[]> layDanhSachPhieu() {
        List<Object[]> rawList = daoHoaDon.layDanhSachPhieuDoiTra();
        List<Object[]> result = new ArrayList<>();
        
        for (Object[] row : rawList) {
            String ghiChuDB = (String) row[4];
            String trangThai = "Chờ xử lý";
            String loi = "Chưa xác định";
            String tienHoan = "0đ";
            String chenhLech = "0đ";
            
            if (ghiChuDB != null && ghiChuDB.contains("|")) {
                String[] parts = ghiChuDB.split("\\|");
                if(parts.length > 0) trangThai = parts[0].trim();
                if(parts.length > 1) loi = parts[1].trim();
                if(parts.length > 2) tienHoan = parts[2].trim();
                if(parts.length > 3) chenhLech = parts[3].trim();
            }
            
            result.add(new Object[]{
                row[0], row[1], row[2], row[3], loi, tienHoan, chenhLech, trangThai, row[5], ""
            });
        }
        return result;
    }

    public boolean xacNhanGiaoDichDoiTra(String maPhieu, String trangThaiMoi) {
        return daoHoaDon.capNhatTrangThaiPhieuDoiTra(maPhieu, trangThaiMoi);
    }

    public List<Object[]> layChiTietPhieu(String maPhieu) {
        List<Object[]> rawData = daoCTHD.layDuLieuChoTaoHoaDon(maPhieu); 
        List<Object[]> result = new ArrayList<>();

        if (rawData != null) {
            for (Object[] row : rawData) {
                Object tenSP = row[0];
                Object soLuong = row[2]; // Số lượng nằm ở cột 2
                Object donVi = row[1];   // ĐVT nằm ở cột 1
                Object donGia = row[3];
                result.add(new Object[]{tenSP, soLuong, donVi, donGia});
            }
        }
        return result;
    }
}