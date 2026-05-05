package BUS;

import DAO.DAO_HoaDon;
import Entity.HoaDon;
import Enumeration.LoaiHoaDon;

import java.time.LocalDateTime;
import java.util.ArrayList; // Bổ sung import
import java.util.List;      // Bổ sung import

public class BUS_TraHang {
    private DAO_HoaDon daoHoaDon;

    public BUS_TraHang() {
        this.daoHoaDon = new DAO_HoaDon();
    }

    // Nghiệp vụ: Kiểm tra điều kiện trả hàng (Ví dụ: Chỉ cho trả trong vòng 7 ngày và là HD mua hàng)[cite: 26]
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
        
        // Kiểm tra thời gian (Quy định nhà thuốc: Cho phép trả trong 7 ngày)[cite: 26]
        LocalDateTime ngayHetHanTra = hd.getNgayLapHD().plusDays(7);
        if (LocalDateTime.now().isAfter(ngayHetHanTra)) {
            System.out.println("Hóa đơn đã quá hạn đổi/trả (quá 7 ngày).");
            return false;
        }
        return true;
    }

    // Nghiệp vụ: Xác định mức hoàn tiền[cite: 26]
    public double xacDinhMucHoanTien(double tongTienHD, double phanTramHoan) {
        return tongTienHD * (phanTramHoan / 100.0);
    }

    // Tính chênh lệch nếu có bù trừ[cite: 26]
    public double tinhTienChenhLech(double tienKhachMua, double tienHoanLai) {
        return tienKhachMua - tienHoanLai;
    }

    // ==========================================================
    // CÁC HÀM MỚI BỔ SUNG ĐỂ KẾT NỐI DATABASE VÀ GIAO DIỆN
    // ==========================================================

    // 1. Hàm lấy danh sách và giải mã chuỗi "|" từ Database
    public List<Object[]> layDanhSachPhieu() {
        // Gọi DAO lấy dữ liệu thô (Đảm bảo bạn đã thêm hàm layDanhSachPhieuDoiTra() bên DAO_HoaDon nhé)
        List<Object[]> rawList = daoHoaDon.layDanhSachPhieuDoiTra();
        List<Object[]> result = new ArrayList<>();
        
        for (Object[] row : rawList) {
            // Thứ tự row từ DAO: [maPhieu, hdGoc, khach, loaiHD, ghiChuDB, ngay]
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
        // Đảm bảo bạn đã thêm hàm capNhatTrangThaiPhieuDoiTra bên DAO_HoaDon
        boolean ok = daoHoaDon.capNhatTrangThaiPhieuDoiTra(maPhieu, trangThaiMoi);
        if (ok) {
            System.out.println("Cập nhật phiếu " + maPhieu + " thành " + trangThaiMoi + " thành công! Doanh thu đã được điều chỉnh.");
            return true;
        }
        return false;
    }
}