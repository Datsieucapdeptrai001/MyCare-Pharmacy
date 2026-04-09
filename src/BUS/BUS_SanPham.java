package BUS;

import Entity.SanPham;
import Entity.LoHang;
import java.util.ArrayList;
import java.util.List;

public class BUS_SanPham {
    // Không khởi tạo DAO để tránh lỗi kết nối Database
    // private DAO_SanPham daoSanPham; 

    public BUS_SanPham() {
        // Constructor để trống vì chạy ảo
    }

    // 1. Nghiệp vụ: Trả về danh sách dữ liệu giả (Mock Data)
    // Giúp hiển thị lên bảng mà không cần Database
    public List<SanPham> traCuuSanPham(String tuKhoa) {
        List<SanPham> dsFake = new ArrayList<>();

        // Tạo SP 1: Vitamin C
        SanPham sp1 = new SanPham();
        sp1.setId("PRO2023-0001");
        sp1.setTen("Vitamin C 1000mg");
        sp1.setHoatChat("Ascorbic Acid");
        sp1.setDonViDoCoBan("Viên");
        sp1.setThueVAT(10);
        
        // Tạo SP 2: Siro tăng sức đề kháng
        SanPham sp2 = new SanPham();
        sp2.setId("PRO2023-0005");
        sp2.setTen("Siro tăng sức đề kháng");
        sp2.setHoatChat("Various");
        sp2.setDonViDoCoBan("Chai");
        sp2.setThueVAT(10);

        // Tạo SP 3: Paracetamol
        SanPham sp3 = new SanPham();
        sp3.setId("PRO2023-0006");
        sp3.setTen("Paracetamol 500mg");
        sp3.setHoatChat("Paracetamol");
        sp3.setDonViDoCoBan("Vỉ");
        sp3.setThueVAT(5);

        // Thêm vào danh sách
        dsFake.add(sp1);
        dsFake.add(sp2);
        dsFake.add(sp3);

        // Logic tìm kiếm ảo
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return dsFake;
        }

        List<SanPham> ketQuaTimKiem = new ArrayList<>();
        String tuKhoaLower = tuKhoa.toLowerCase();
        for (SanPham sp : dsFake) {
            if (sp.getTen().toLowerCase().contains(tuKhoaLower) || 
                sp.getHoatChat().toLowerCase().contains(tuKhoaLower)) {
                ketQuaTimKiem.add(sp);
            }
        }
        return ketQuaTimKiem;
    }

    // 2. Nghiệp vụ: Kiểm tra thông tin (Luôn trả về true để bạn thao tác mượt)
    public boolean kiemTraThongTinSP(SanPham sp) {
        return true; 
    }

    // 3. Nghiệp vụ: Tính giá bán (Trả về giá giả định)
    public double tinhGiaBanTheoDonVi(String maSP, String donViMuonBan) {
        // Trả về một con số bất kỳ để giao diện hiển thị được giá
        return 5000.0; 
    }
    
    // Bổ sung hàm này nếu ManHinhSanPham có gọi để lấy lô hàng ảo
    public List<LoHang> layLoTheoSP(String maSP) {
        List<LoHang> dsLoFake = new ArrayList<>();
        // Bạn có thể thêm dữ liệu LoHang giả ở đây nếu cần
        return dsLoFake;
    }
}