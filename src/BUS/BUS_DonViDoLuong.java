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
}