package BUS;

import DAO.DAO_HoaDon;
import DAO.DAO_ChiTietHoaDon; // ĐÃ BỔ SUNG
import Entity.HoaDon;
import Enumeration.LoaiHoaDon;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BUS_TraHang {
    private DAO_HoaDon daoHoaDon;
    private DAO_ChiTietHoaDon daoCTHD; // ĐÃ BỔ SUNG

    public BUS_TraHang() {
        this.daoHoaDon = new DAO_HoaDon();
        this.daoCTHD = new DAO_ChiTietHoaDon(); // ĐÃ KHỞI TẠO
    }

    // Nghiệp vụ: Kiểm tra điều kiện trả hàng (Ví dụ: Chỉ cho trả trong vòng 7 ngày và là HD mua hàng)
    public boolean kiemTraDieuKien(String maHoaDonGoc) {
        HoaDon hd = daoHoaDon.layHoaDonTheoMa(maHoaDonGoc);
        if (hd == null) {
            System.out.println("Không tìm thấy hóa đơn này.");
            return false;
        }
        if (hd.getLoaiHD() != LoaiHoaDon.BAN_HANG) {
            System.out.println("Lỗi: Chỉ được trả hàng cho hóa đơn Bán Hàng.");
            return false;
        }
        
        // Kiểm tra thời gian (Quy định nhà thuốc: Cho phép trả trong 7 ngày)
        LocalDateTime ngayHetHanTra = hd.getNgayLapHD().plusDays(7);
        if (LocalDateTime.now().isAfter(ngayHetHanTra)) {
            System.out.println("Hóa đơn đã quá hạn đổi/trả (quá 7 ngày).");
            return false;
        }
        return true;
    }

    // Nghiệp vụ: Xác định mức hoàn tiền
    public double xacDinhMucHoanTien(double tongTienHD, double phanTramHoan) {
        return tongTienHD * (phanTramHoan / 100.0);
    }

    // Tính chênh lệch nếu có bù trừ
    public double tinhTienChenhLech(double tienKhachMua, double tienHoanLai) {
        return tienKhachMua - tienHoanLai;
    }

    // ==========================================================
    // CÁC HÀM MỚI BỔ SUNG ĐỂ KẾT NỐI DATABASE VÀ GIAO DIỆN
    // ==========================================================

    // 1. Hàm lấy danh sách và giải mã chuỗi "|" từ Database
    public List<Object[]> layDanhSachPhieu() {
        // Gọi DAO lấy dữ liệu thô
        List<Object[]> rawList = daoHoaDon.layDanhSachPhieuDoiTra();
        List<Object[]> result = new ArrayList<>();
        
        for (Object[] row : rawList) {
            String ghiChuDB = (String) row[4];
            
            String trangThai = "Chờ xử lý";
            String loi = "Chưa xác định";
            String tienHoan = "0đ";
            String chenhLech = "0đ";
            
            // Cắt chuỗi ghi chú bằng dấu "|" để tách ra các cột UI tương ứng
            if (ghiChuDB != null && ghiChuDB.contains("|")) {
                String[] parts = ghiChuDB.split("\\|");
                if(parts.length > 0) trangThai = parts[0].trim();
                if(parts.length > 1) loi = parts[1].trim();
                if(parts.length > 2) tienHoan = parts[2].trim();
                if(parts.length > 3) chenhLech = parts[3].trim();
            }
            
            // Gắn vào mảng 10 cột khớp 100% với cấu trúc bảng của ManHinhDoiTra
            result.add(new Object[]{
                row[0], row[1], row[2], row[3], loi, tienHoan, chenhLech, trangThai, row[5], ""
            });
        }
        return result;
    }

    // 2. Thay thế hàm xác nhận giao dịch để nhận đúng 2 tham số từ sự kiện Nút bấm UI
    public boolean xacNhanGiaoDichDoiTra(String maPhieu, String trangThaiMoi) {
        boolean ok = daoHoaDon.capNhatTrangThaiPhieuDoiTra(maPhieu, trangThaiMoi);
        if (ok) {
            System.out.println("Cập nhật phiếu " + maPhieu + " thành " + trangThaiMoi + " thành công! Doanh thu đã được điều chỉnh.");
            return true;
        }
        return false;
    }

    // ==========================================================
    // ĐÂY LÀ HÀM BẠN ĐANG THIẾU ĐỂ ĐỔ SẢN PHẨM VÀO KHUNG CHI TIẾT
    // ==========================================================
    public List<Object[]> layChiTietPhieu(String maPhieu) {
        // Tận dụng hàm đã có bên DAO_ChiTietHoaDon để lấy danh sách món hàng
        List<Object[]> rawData = daoCTHD.layDuLieuChoTaoHoaDon(maPhieu); 
        List<Object[]> result = new ArrayList<>();

        if (rawData != null) {
            for (Object[] row : rawData) {
                // Mảng gốc từ DAO đang là: [TenSP, DVT, Số Lượng, Đơn Giá, ...]
                // Giao diện showDetailPanel đang cần: [TenSP, Số Lượng, DVT, Đơn Giá]
                // Ta cần tráo đổi vị trí index 1 (DVT) và index 2 (Số Lượng) cho khớp UI
                
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