package BUS;

import java.util.List;
import DAO.DAO_ChiTietHoaDon;

public class BUS_ChiTietHoaDon {
    
    private DAO_ChiTietHoaDon daoCTHD;

    public BUS_ChiTietHoaDon() {
        daoCTHD = new DAO_ChiTietHoaDon();
    }

    // Tận dụng hàm trả về List<Object[]> có sẵn trong DAO của bạn
    public List<Object[]> layDuLieuDoiTra(String maHD) {
        if (maHD == null || maHD.trim().isEmpty()) {
            return null;
        }
        return daoCTHD.layDuLieuChoTaoHoaDon(maHD); 
    }
}