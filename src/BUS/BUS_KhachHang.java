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

    public boolean validateThongTin(KhachHang kh) {
        if (kh.getId() == null || kh.getId().trim().isEmpty()) {
            System.out.println("Lỗi: Mã khách hàng không được để trống.");
            return false;
        }

        if (kh.getHoVaTen() == null || kh.getHoVaTen().trim().isEmpty()) {
            System.out.println("Lỗi: Tên khách hàng không được để trống.");
            return false;
        }

        String sdtRegex = "^0\\d{9}$";
        if (kh.getSdt() == null || !Pattern.matches(sdtRegex, kh.getSdt())) {
            System.out.println("Lỗi: Số điện thoại không hợp lệ.");
            return false;
        }

        return true;
    }

    public boolean themKhachHang(KhachHang kh) {
        if (validateThongTin(kh)) {
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

    public List<KhachHang> traCuuKhachHang(String tuKhoa) {
        List<KhachHang> dsToanBo = daoKhachHang.getDSKhachHang();
        List<KhachHang> dsKetQua = new ArrayList<>();

        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return dsToanBo;
        }

        tuKhoa = tuKhoa.trim().toLowerCase();

        for (KhachHang kh : dsToanBo) {
            if (kh == null) {
                continue;
            }

            String sdt = kh.getSdt();
            String hoTen = kh.getHoVaTen();

            boolean trungSdt = sdt != null && sdt.contains(tuKhoa);
            boolean trungHoTen = hoTen != null && hoTen.toLowerCase().contains(tuKhoa);

            if (trungSdt || trungHoTen) {
                dsKetQua.add(kh);
            }
        }

        return dsKetQua;
    }
    // Nghiệp vụ: Tích điểm khi khách mua hàng 
    // Tôi bổ sung tham số 'soTienThanhToan' vì phải biết
    public boolean tichDiem(String sdtKhachHang, double soTienThanhToan) {
        KhachHang kh = daoKhachHang.getKhachHangTheoSDT(sdtKhachHang);

        if (kh == null) {
            return false;
        }

        int diemHienTai = kh.getDiemTichLuy();
        int diemCongThem = (int) (soTienThanhToan / 1000);
        int diemMoi = diemHienTai + diemCongThem;

        kh.setDiemTichLuy(diemMoi);

        boolean ketQua = daoKhachHang.capNhatDiemTichLuy(kh.getId(), diemMoi);

        if (ketQua) {
            System.out.println("Đã tích thêm " + diemCongThem + " điểm cho khách hàng " + kh.getHoVaTen());
        }

        return ketQua;
    }
}