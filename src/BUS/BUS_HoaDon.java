package BUS;

import DAO.*;
import Entity.*;
import Enumeration.TrangThaiLoHang;
import java.util.List;

public class BUS_HoaDon {
    private DAO_HoaDon daoHD = new DAO_HoaDon();
    private DAO_ChiTietHoaDon daoCTHD = new DAO_ChiTietHoaDon();
    private DAO_PhanBoLoHang daoPB = new DAO_PhanBoLoHang();
    private DAO_LoHang daoLo = new DAO_LoHang();
    
    public List<Object[]> layDanhSachHoaDonChoBang() {
        return daoHD.layDanhSachHoaDonChoBang(); 
    }
    
    public HoaDon getHoaDonTheoMa(String maHD) {
        return daoHD.timHoaDonTheoMa(maHD); 
    }
    
    public HoaDon layHoaDonTheoMa(String maHD) {
        return daoHD.layHoaDonTheoMa(maHD); 
    }
    
    public List<HoaDon> layTatCaHoaDon() {
        return daoHD.layTatCaHoaDon(); 
    }
    
    public boolean thanhToan(HoaDon hd, List<ChiTietHoaDon> dsCTHD) {
        return daoHD.luuGiaoDichThanhToan(hd, dsCTHD, daoCTHD, daoLo, daoPB);
    }
}