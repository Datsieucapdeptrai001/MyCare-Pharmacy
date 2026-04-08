package BUS;

import DAO.DAO_LoHang;
import Entity.LoHang;
import Enum.TrangThaiLoHang;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BUS_Kho {
    private DAO_LoHang daoLoHang;

    public BUS_Kho() {
        this.daoLoHang = new DAO_LoHang();
    }

    // Nghiệp vụ: Kiểm tra xem tổng tồn kho của thuốc này có đủ để bán không
    public boolean kiemTraTonKho(String maSP, int soLuongCanBan) {
        List<LoHang> dsLoHienCo = daoLoHang.layLoTheoSP(maSP);
        int tongTonKho = 0;
        
        for (LoHang lh : dsLoHienCo) {
            tongTonKho += lh.getSoLuongLoHang();
        }
        
        return tongTonKho >= soLuongCanBan;
    }

    // Nghiệp vụ: Cảnh báo hàng cận Date (Sắp hết hạn)
    // Tham số soNgayCanhBao: Ví dụ truyền vào 90 (Cảnh báo trước 3 tháng)
    public List<LoHang> canhBaoHangSapHetHan(int soNgayCanhBao) {
        // Tái sử dụng hàm layDSLoHang (bạn đã có trong DAO_LoHang từ trước)
        List<LoHang> dsToanBoLo = new DAO_LoHang().layDSLoHang(); 
        List<LoHang> dsCanhBao = new ArrayList<>();
        
        LocalDateTime thoiDiemHienTai = LocalDateTime.now();
        LocalDateTime thoiDiemGioiHan = thoiDiemHienTai.plusDays(soNgayCanhBao);

        for (LoHang lh : dsToanBoLo) {
            // Lô chưa hết hạn, nhưng ngày hết hạn nằm trong khoảng cảnh báo
            if (lh.getNgayHetHan().isAfter(thoiDiemHienTai) && lh.getNgayHetHan().isBefore(thoiDiemGioiHan)) {
                if (lh.getSoLuongLoHang() > 0) {
                    dsCanhBao.add(lh);
                }
            }
        }
        return dsCanhBao;
    }

    // Nghiệp vụ quan trọng nhất: Xuất kho theo chuẩn FEFO (Hết hạn trước -> Xuất trước)
    // Trả về số lượng thuốc THỰC TẾ không thể đáp ứng (nếu kho không đủ hàng)
    // Nếu kho đủ hàng, nó sẽ trả về 0 và tự động trừ số lượng trong DB.
    public int xuLyXuatKhoFEFO(String maSP, int soLuongCanXuat) {
        // 1. Lấy danh sách lô hàng của Sản phẩm này, đã được DAO order by NgayHetHan ASC
        List<LoHang> dsLo = daoLoHang.layLoTheoSP(maSP);
        
        int soLuongConThieu = soLuongCanXuat;

        // 2. Duyệt qua từng lô hàng, lấy từ lô cận Date nhất
        for (LoHang lh : dsLo) {
            if (soLuongConThieu <= 0) break; // Đã lấy đủ hàng

            int soLuongTrongLo = lh.getSoLuongLoHang();

            if (soLuongTrongLo >= soLuongConThieu) {
                // Lô này đủ hàng để trừ
                daoLoHang.capNhatSoLuongTon(lh.getId(), soLuongTrongLo - soLuongConThieu);
                soLuongConThieu = 0;
            } else {
                // Lô này không đủ, lấy sạch lô này rồi đi qua lô tiếp theo
                daoLoHang.capNhatSoLuongTon(lh.getId(), 0);
                daoLoHang.capNhatTrangThaiLo(lh.getId(), TrangThaiLoHang.HET_HANG); // Cập nhật trạng thái
                soLuongConThieu -= soLuongTrongLo;
            }
        }

        // Nếu chạy xong mà soLuongConThieu > 0 nghĩa là kho không đủ hàng
        if (soLuongConThieu > 0) {
            System.out.println("Cảnh báo: Kho không đủ hàng. Còn thiếu " + soLuongConThieu + " đơn vị.");
        }
        
        return soLuongConThieu; 
    }

    // Nghiệp vụ: Kiểm kê kho định kỳ (Quét và tự động chuyển trạng thái các lô hàng)
    public boolean kiemKeKho() {
        try {
            List<LoHang> dsToanBo = new DAO_LoHang().layDSLoHang();
            LocalDateTime hienTai = LocalDateTime.now();

            for (LoHang lh : dsToanBo) {
                // Nếu số lượng về 0 -> Chuyển thành HẾT HÀNG
                if (lh.getSoLuongLoHang() == 0 && lh.getTrangThai() != TrangThaiLoHang.HET_HANG) {
                    daoLoHang.capNhatTrangThaiLo(lh.getId(), TrangThaiLoHang.HET_HANG);
                }
                // Nếu qua ngày hết hạn -> Chuyển thành HẾT HẠN
                else if (lh.getNgayHetHan().isBefore(hienTai) && lh.getTrangThai() != TrangThaiLoHang.HET_HANG) {
                    daoLoHang.capNhatTrangThaiLo(lh.getId(), TrangThaiLoHang.HET_HANG);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}