package BUS;

import java.util.List;
import DAO.DAO_DonViDoLuong;
import Entity.DonViDoLuong;

public class BUS_DonViDoLuong {
    
    private DAO_DonViDoLuong daoDVDL;

    public BUS_DonViDoLuong() {
        // Khởi tạo đối tượng DAO
        daoDVDL = new DAO_DonViDoLuong();
    }

    /**
     * Lấy danh sách các đơn vị tính và giá tiền của một sản phẩm
     * @param maSP ID của sản phẩm cần lấy giá
     * @return Danh sách DonViDoLuong (Hộp, Vỉ, Viên...) kèm giá tương ứng
     */
    public List<DonViDoLuong> getDSTheoMaSP(String maSP) {
        // Xử lý nghiệp vụ cơ bản: Kiểm tra mã SP có hợp lệ không
        if (maSP == null || maSP.trim().isEmpty()) {
            return null;
        }
        
        // Gọi xuống tầng DAO để lấy dữ liệu từ DB
        return daoDVDL.getDSTheoMaSP(maSP);
    }
}