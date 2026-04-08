package BUS;

import DAO.DAO_SanPham;
import Entity.LoHang;
import Entity.SanPham;

import java.util.ArrayList;
import java.util.List;

public class BUS_SanPham {
    private DAO_SanPham daoSanPham;

    public BUS_SanPham() {
        this.daoSanPham = new DAO_SanPham();
    }

    // 1. Nghiệp vụ: Tìm kiếm thuốc theo Tên hoặc Hoạt chất (giúp Dược sĩ dễ kê đơn)
    public List<SanPham> traCuuSanPham(String tuKhoa) {
        List<SanPham> dsToanBo = daoSanPham.getDsThuoc();
        List<SanPham> dsKetQua = new ArrayList<>();

        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return dsToanBo;
        }

        tuKhoa = tuKhoa.toLowerCase();
        for (SanPham sp : dsToanBo) {
            // Cho phép tìm kiếm theo cả Tên Thuốc và Tên Hoạt Chất
            boolean matchTen = sp.getTen() != null && sp.getTen().toLowerCase().contains(tuKhoa);
            boolean matchHoatChat = sp.getHoatChat() != null && sp.getHoatChat().toLowerCase().contains(tuKhoa);
            
            if (matchTen || matchHoatChat) {
                dsKetQua.add(sp);
            }
        }
        return dsKetQua;
    }

    // 2. Nghiệp vụ: Xác thực dữ liệu đầu vào khi thêm/sửa Sản phẩm
    public boolean kiemTraThongTinSP(SanPham sp) {
        if (sp.getId() == null || sp.getId().trim().isEmpty()) {
            System.out.println("Lỗi: Mã sản phẩm không được trống.");
            return false;
        }
        if (sp.getTen() == null || sp.getTen().trim().isEmpty()) {
            System.out.println("Lỗi: Tên sản phẩm không được trống.");
            return false;
        }
        if (sp.getDonViDoCoBan() == null || sp.getDonViDoCoBan().trim().isEmpty()) {
            System.out.println("Lỗi: Đơn vị đo cơ bản (Hộp, Chai, Lọ...) không được trống.");
            return false;
        }
        if (sp.getThueVAT() < 0) {
            System.out.println("Lỗi: Thuế VAT không thể là số âm.");
            return false;
        }
        return true;
    }

    // 3. Nghiệp vụ tính giá bán quy đổi (Hàm "ăn điểm" của đồ án)
    // Giả sử Đơn vị cơ bản trong DB là "Hộp" (giá 100.000đ).
    // Nếu khách mua "Vỉ" (1 Hộp = 10 Vỉ) -> Giá Vỉ = 10.000đ
    // Nếu khách mua "Viên" (1 Vỉ = 10 Viên) -> Giá Viên = 1.000đ
    public double tinhGiaBanTheoDonVi(String maSP, String donViMuonBan) {
        // Lấy danh sách lô hàng của sản phẩm này để biết giá gốc
        List<LoHang> dsLo = daoSanPham.layLoTheoSP(maSP);
        
        if (dsLo.isEmpty()) {
            System.out.println("Sản phẩm này hiện đã hết hàng hoặc không có giá.");
            return 0.0;
        }

        // Lấy giá của lô hàng ưu tiên xuất trước nhất (Lô đầu tiên) làm cơ sở
        double giaGoc = dsLo.get(0).getGia(); 

        // Thực hiện logic quy đổi (Bạn có thể tùy chỉnh tỷ lệ theo thực tế DB của bạn)
        donViMuonBan = donViMuonBan.toLowerCase().trim();
        
        switch (donViMuonBan) {
            case "hộp":
            case "chai":
            case "lọ":
                return giaGoc; // Giữ nguyên giá
                
            case "vỉ":
                // Giả định 1 hộp có 10 vỉ
                return giaGoc / 10.0; 
                
            case "viên":
                // Giả định 1 hộp có 100 viên (10 vỉ x 10 viên)
                return giaGoc / 100.0; 
                
            case "gói":
                // Giả định 1 hộp có 30 gói
                return giaGoc / 30.0; 
                
            default:
                return giaGoc; // Mặc định trả về giá gốc nếu không khớp đơn vị quy đổi
        }
    }
}