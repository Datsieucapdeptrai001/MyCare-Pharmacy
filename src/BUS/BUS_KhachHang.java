package BUS;

import DAO.DAO_KhachHang;
import Entity.KhachHang;
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
    
    public KhachHang getKhachHangTheoSDT(String sdt) {
        if (sdt == null || sdt.trim().isEmpty()) {
            return null;
        }
        return daoKhachHang.getKhachHangTheoSDT(sdt);
    }
    
    // ========================================================
    // FIX 1: Bổ sung Alias function để khớp với lệnh gọi trong TaoHoaDon.java
    // ========================================================
    public KhachHang timKhachHangTheoSdt(String sdt) {
        return getKhachHangTheoSDT(sdt);
    }
    
    public boolean validateThongTin(KhachHang kh) {
        if (kh.getId() == null || kh.getId().trim().isEmpty()) return false;
        if (kh.getHoVaTen() == null || kh.getHoVaTen().trim().isEmpty()) return false;
        String sdtRegex = "^0\\d{9}$";
        if (kh.getSdt() == null || !Pattern.matches(sdtRegex, kh.getSdt())) return false;
        return true;
    }

    public boolean themKhachHang(KhachHang kh) {
        if (validateThongTin(kh)) {
            if (daoKhachHang.getKhachHangTheoSDT(kh.getSdt()) != null) return false;
            if (kh.getNgayTao() == null) kh.setNgayTao(LocalDateTime.now());
            return daoKhachHang.themKhachHang(kh);
        }
        return false;
    }
    
    public KhachHang timKhachHangTheoMa(String id) {
        return daoKhachHang.timKhachHangTheoMa(id);
    }
    
    public List<KhachHang> traCuuKhachHang(String tuKhoa) {
        List<KhachHang> dsToanBo = daoKhachHang.getDSKhachHang();
        List<KhachHang> dsKetQua = new ArrayList<>();
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) return dsToanBo;
        tuKhoa = tuKhoa.trim().toLowerCase();
        for (KhachHang kh : dsToanBo) {
            if (kh == null) continue;
            String sdt = kh.getSdt();
            String hoTen = kh.getHoVaTen();
            boolean trungSdt = sdt != null && sdt.contains(tuKhoa);
            boolean trungHoTen = hoTen != null && hoTen.toLowerCase().contains(tuKhoa);
            if (trungSdt || trungHoTen) dsKetQua.add(kh);
        }
        return dsKetQua;
    }
    
    public boolean tichDiem(String sdtKhachHang, double soTienThanhToan) {
        KhachHang kh = daoKhachHang.getKhachHangTheoSDT(sdtKhachHang);
        if (kh == null) return false;
        int diemHienTai = kh.getDiemTichLuy();
        int diemCongThem = (int) (soTienThanhToan / 1000);
        int diemMoi = diemHienTai + diemCongThem;
        kh.setDiemTichLuy(diemMoi);
        return daoKhachHang.capNhatDiemTichLuy(kh.getId(), diemMoi);
    }

    // ========================================================
    // FIX 2: Bổ sung hàm cập nhật điểm nhận (SĐT, Điểm chênh lệch) từ TaoHoaDon
    // BUS sẽ tự lấy Mã KH và tính toán tổng điểm mới trước khi đẩy xuống DAO
    // ========================================================
    public boolean capNhatDiemTichLuy(String sdtKhachHang, int diemChenhLech) {
        if (sdtKhachHang == null || sdtKhachHang.trim().isEmpty()) return false;
        
        KhachHang kh = daoKhachHang.getKhachHangTheoSDT(sdtKhachHang);
        if (kh == null) return false;

        // Tính điểm mới dựa trên điểm hiện tại và phần chênh lệch (Cộng thêm hoặc Trừ đi)
        int diemMoi = kh.getDiemTichLuy() + diemChenhLech;
        
        // Đảm bảo điểm không bị âm do lỗi logic hoặc khách hàng dùng quá số điểm
        if (diemMoi < 0) diemMoi = 0; 

        // Đẩy xuống DAO với định dạng chuẩn (Mã KH, Điểm Mới)
        return daoKhachHang.capNhatDiemTichLuy(kh.getId(), diemMoi);
    }

    // ========================================================
    // FIX: 2 HÀM BỔ SUNG ĐỂ CHO PHÉP THÊM / SỬA KHÁCH HÀNG 
    // ========================================================
    public boolean capNhatKhachHang(KhachHang kh) {
        if (validateThongTin(kh)) {
            return daoKhachHang.capNhatKhachHang(kh);
        }
        return false;
    }

    public String phatSinhMaKHTiepTheo() {
        return daoKhachHang.phatSinhMaKHTiepTheo();
    }
}