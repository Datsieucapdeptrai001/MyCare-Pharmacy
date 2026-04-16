package BUS;

import ConnectDB.ConnectDB; 
import DAO.*;
import Entity.*;
import Enumeration.TrangThaiLoHang;

import java.sql.Connection; 
import java.sql.SQLException; 
import java.util.List;

public class BUS_HoaDon {
    private DAO_HoaDon daoHD = new DAO_HoaDon();
    private DAO_ChiTietHoaDon daoCTHD = new DAO_ChiTietHoaDon();
    private DAO_PhanBoLoHang daoPB = new DAO_PhanBoLoHang();
    private DAO_LoHang daoLo = new DAO_LoHang();
    public List<Object[]> layDanhSachHoaDonChoBang() {
        return daoHD.layDanhSachHoaDonChoBang(); // daoHD là biến DAO_HoaDon bạn đã khai báo ở đầu file BUS
    }
    public HoaDon getHoaDonTheoMa(String maHD) {
        return daoHD.timHoaDonTheoMa(maHD); // Gọi xuống DAO để lấy dữ liệu thực
    }
    public HoaDon layHoaDonTheoMa(String maHD) {
        return daoHD.layHoaDonTheoMa(maHD); // Gọi hàm đã có trong DAO_HoaDon
    }
    public List<HoaDon> layTatCaHoaDon() {
        return daoHD.layTatCaHoaDon(); 
    }
    public boolean thanhToan(HoaDon hd, List<ChiTietHoaDon> dsCTHD) {
        Connection con = ConnectDB.getInstance().getConnection();
        try {
            // Tắt Auto Commit để đảm bảo tính giao dịch (Transaction)
            con.setAutoCommit(false);

            // 1. Lưu hóa đơn tổng
            if (!daoHD.themHoaDon(hd)) throw new Exception("Lỗi lưu hóa đơn");

            for (ChiTietHoaDon ct : dsCTHD) {
                // 2. Lưu chi tiết từng mặt hàng
                // (Lưu ý: Hàm của bạn tên là themCTHD, nếu bên DAO tên là themChiTietHoaDon thì bạn nhớ đổi lại cho khớp nhé)
                if (!daoCTHD.themCTHD(ct)) throw new Exception("Lỗi lưu chi tiết");

                // 3. Xử lý trừ kho theo lô (FEFO)
                List<LoHang> dsLoHieuLuc = daoLo.layLoTheoSP(ct.getSanPhamId().getId());
                int soLuongCanLay = ct.getSoLuong();

                for (LoHang lh : dsLoHieuLuc) {
                    if (soLuongCanLay <= 0) break;

                    int layDuoc = Math.min(lh.getSoLuongLoHang(), soLuongCanLay);
                    
                    // --- ĐÃ FIX LỖI NULL ĐƠN VỊ ĐO LƯỜNG Ở ĐÂY ---
                    // Đổi chữ "null" thành ct.getDonViDoLuongId()
                    PhanBoLoHang pb = new PhanBoLoHang(hd, ct.getDonViDoLuongId(), ct.getSanPhamId(), lh, layDuoc);
                    daoPB.themPhanBo(pb);

                    // Cập nhật số lượng còn lại trong lô
                    int conLai = lh.getSoLuongLoHang() - layDuoc;
                    daoLo.capNhatSoLuongTon(lh.getId(), conLai);
                    
                    if (conLai == 0) daoLo.capNhatTrangThaiLo(lh.getId(), TrangThaiLoHang.HET_HANG);

                    soLuongCanLay -= layDuoc;
                }
                
                if (soLuongCanLay > 0) throw new Exception("Kho không đủ hàng cho SP: " + ct.getSanPhamId().getId());
            }

            con.commit(); // Hoàn tất giao dịch
            return true;
        } catch (Exception e) {
            try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}