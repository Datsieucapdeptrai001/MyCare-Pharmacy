package BUS;

public class BUS_DoiHang {
    private BUS_Kho busKho;

    public BUS_DoiHang() {
        this.busKho = new BUS_Kho();
    }

    // Gọi lại hàm kiểm tra điều kiện từ BUS_TraHang vì Đổi hay Trả đều phải tuân thủ hạn 7 ngày
    public boolean kiemTraDieuKien(String maHoaDonGoc) {
        BUS_TraHang busTra = new BUS_TraHang();
        return busTra.kiemTraDieuKien(maHoaDonGoc);
    }

    // Kiểm tra xem món hàng khách muốn ĐỔI LẤY có còn trong kho không
    public boolean kiemTraTonKhoDoi(String maSanPhamMoi, int soLuongMuonDoi) {
        return busKho.kiemTraTonKho(maSanPhamMoi, soLuongMuonDoi);
    }

    // Tính tiền chênh lệch.
    // Nếu > 0: Khách phải bù thêm tiền. Nếu < 0: Tiệm thối lại tiền cho khách
    public double tinhTienChenhLech(double tongTienHangCu, double tongTienHangMoi) {
        return tongTienHangMoi - tongTienHangCu; 
    }
}