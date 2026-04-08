package BUS;

import DAO.DAO_SanPham;
import Entity.SanPham;
import ConnectDB.ConnectDB;
import java.util.ArrayList;
import java.util.List;

public class BUS_TraCuu {
    private DAO_SanPham daoSanPham;

    public BUS_TraCuu() {
        this.daoSanPham = new DAO_SanPham();
    }

    // 1. Kiểm tra từ khóa tìm kiếm có bị rỗng hoặc chỉ toàn khoảng trắng không
    public boolean kiemTraRong(String tuKhoa) {
        return tuKhoa == null || tuKhoa.trim().isEmpty();
    }

    // 2. Lấy danh sách thuốc dựa trên từ khóa (Tìm theo Tên hoặc Hoạt chất)
    public List<SanPham> getDsThuoc(String tuKhoa) {
        // Nếu người dùng không nhập gì mà bấm tìm kiếm -> Trả về toàn bộ danh sách
        if (kiemTraRong(tuKhoa)) {
            return daoSanPham.getDsThuoc(); 
        }
        
        List<SanPham> dsToanBo = daoSanPham.getDsThuoc();
        List<SanPham> dsKetQua = new ArrayList<>();
        
        // Chuyển từ khóa về chữ thường để tìm kiếm không phân biệt Hoa/Thường
        String tk = tuKhoa.toLowerCase().trim();
        
        for (SanPham sp : dsToanBo) {
            boolean matchTen = sp.getTen() != null && sp.getTen().toLowerCase().contains(tk);
            boolean matchHoatChat = sp.getHoatChat() != null && sp.getHoatChat().toLowerCase().contains(tk);
            
            if (matchTen || matchHoatChat) {
                dsKetQua.add(sp);
            }
        }
        return dsKetQua;
    }

    // 3. Kiểm tra xem danh sách kết quả trả về có dữ liệu không 
    // (Dùng để hiển thị thông báo "Không tìm thấy sản phẩm" trên giao diện)
    public boolean kiemTraKetQua(List<SanPham> ds) {
        if (ds == null || ds.isEmpty()) {
            // Có thể quăng thông báo lỗi ra UI tại đây
            System.out.println("Không tìm thấy sản phẩm nào khớp với từ khóa.");
            return false;
        }
        return true;
    }

    // 4. Lấy thông tin chi tiết của 1 loại thuốc khi click đúp vào dòng trên bảng (JTable)
    public SanPham getChiTietThuoc(String id) {
        if (kiemTraRong(id)) {
            return null;
        }
        return daoSanPham.getSanPhamTheoMa(id);
    }
}