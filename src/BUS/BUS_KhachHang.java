package BUS;

import DAO.DAO_KhachHang;
import Entity.KhachHang;
// Import DAO_HoaDon nếu bạn muốn truy vấn tổng tiền khách đã mua
// import DAO.DAO_HoaDon; 

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BUS_KhachHang {
    private DAO_KhachHang daoKhachHang;

    public BUS_KhachHang() {
        this.daoKhachHang = new DAO_KhachHang();
    }

    public List<KhachHang> getDSKhachHang() {
        return daoKhachHang.getDSKhachHang();
    }

    // Nghiệp vụ: Kiểm tra tính hợp lệ của dữ liệu trước khi lưu
    public boolean validateThongTin(KhachHang kh) {
        if (kh.getId() == null || kh.getId().trim().isEmpty()) {
            System.out.println("Lỗi: Mã khách hàng không được để trống.");
            return false;
        }
        if (kh.getHoVaTen() == null || kh.getHoVaTen().trim().isEmpty()) {
            System.out.println("Lỗi: Tên khách hàng không được để trống.");
            return false;
        }
        
        // Kiểm tra số điện thoại (10 số, bắt đầu bằng 0)
        String sdtRegex = "^0\\d{9}$";
        if (kh.getSdt() == null || !Pattern.matches(sdtRegex, kh.getSdt())) {
            System.out.println("Lỗi: Số điện thoại không hợp lệ.");
            return false;
        }
        return true;
    }

    public boolean themKhachHang(KhachHang kh) {
        if (validateThongTin(kh)) {
            // Kiểm tra trùng SĐT trước khi thêm
            if (daoKhachHang.getKhachHangTheoSDT(kh.getSdt()) != null) {
                System.out.println("Lỗi: Số điện thoại này đã được đăng ký cho khách hàng khác.");
                return false;
            }
            if (kh.getNgayTao() == null) {
                kh.setNgayTao(LocalDateTime.now());
            }
            return daoKhachHang.themKhachHang(kh);
        }
        return false;
    }

    // Nghiệp vụ: Tìm kiếm khách hàng theo Tên hoặc Số điện thoại
    public List<KhachHang> traCuuKhachHang(String tuKhoa) {
        List<KhachHang> dsToanBo = daoKhachHang.getDSKhachHang();
        List<KhachHang> dsKetQua = new ArrayList<>();
        
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return dsToanBo;
        }
        
        tuKhoa = tuKhoa.toLowerCase();
        for (KhachHang kh : dsToanBo) {
            if (kh.getSdt().contains(tuKhoa) || kh.getHoVaTen().toLowerCase().contains(tuKhoa)) {
                dsKetQua.add(kh);
            }
        }
        return dsKetQua;
    }

    // Nghiệp vụ: Tích điểm khi khách mua hàng
    // Tôi bổ sung tham số 'soTienThanhToan' vì phải biết hóa đơn bao nhiêu tiền mới tính được điểm
    public void tichDiem(String sdtKhachHang, double soTienThanhToan) {
        KhachHang kh = daoKhachHang.getKhachHangTheoSDT(sdtKhachHang);
        if (kh != null) {
    
            int diemCongThem = (int) (soTienThanhToan / 1000);
            
            System.out.println("Đã tích thêm " + diemCongThem + " điểm cho khách hàng " + kh.getHoVaTen());
        }
    }

    
}