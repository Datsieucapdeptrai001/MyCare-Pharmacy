package BUS;

import java.util.ArrayList;
import java.util.List;
import DAO.DAO_DonViDoLuong;
import Entity.DonViDoLuong;

public class BUS_DonViDoLuong {

    private DAO_DonViDoLuong daoDVDL;

    public BUS_DonViDoLuong() {
        daoDVDL = new DAO_DonViDoLuong();
    }
    public int layHeSoQuyDoi(String tenSP, String tenDVT) {
        if (tenSP == null || tenDVT == null || tenSP.trim().isEmpty() || tenDVT.trim().isEmpty()) {
            return 1;
        }
        return daoDVDL.layHeSoQuyDoi(tenSP.trim(), tenDVT.trim());
    }

    public String layTenDonViCoBan(String tenSP) {
        if (tenSP == null || tenSP.trim().isEmpty()) {
            return "Viên";
        }
        return daoDVDL.layTenDonViCoBan(tenSP.trim());
    }
    /**
     * Lấy danh sách các đơn vị tính và giá tiền của một sản phẩm theo MÃ SẢN PHẨM
     */
    public List<DonViDoLuong> getDSTheoMaSP(String maSP) {
        if (maSP == null || maSP.trim().isEmpty()) {
            return null;
        }
        return daoDVDL.getDSTheoMaSP(maSP);
    }

    /**
     * Lấy danh sách các đơn vị tính của một sản phẩm theo TÊN SẢN PHẨM
     * (Phục vụ cho chức năng tự động load Combobox Đơn vị đo lường)
     */
    public List<DonViDoLuong> getDSTheoTenSP(String tenSP) {
        if (tenSP == null || tenSP.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return daoDVDL.getDSTheoTenSP(tenSP.trim());
    }

    /**
     * Lấy đơn vị đo lường theo Mã Vạch (phục vụ tính năng quét mã bằng súng)
     */
    public DonViDoLuong layDonViTheoMaVach(String maVach) {
        if (maVach == null || maVach.trim().isEmpty()) {
            return null;
        }
        return daoDVDL.layDonViTheoMaVach(maVach.trim());
    }

    /**
     * Cập nhật mã vạch cho đơn vị tính (thường gọi lúc mới nhập hàng)
     */
    public boolean capNhatMaVach(String maSP, String tenDonVi, String maVachMoi) {
        if (maSP == null || tenDonVi == null || maVachMoi == null) {
            return false;
        }
        return daoDVDL.capNhatMaVach(maSP, tenDonVi, maVachMoi);
    }
}