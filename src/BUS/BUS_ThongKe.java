package BUS;

import DAO.DAO_ThongKe;
import java.time.LocalDateTime;
import java.util.List;

public class BUS_ThongKe {

    private final DAO_ThongKe dao;

    public BUS_ThongKe() {
        this.dao = new DAO_ThongKe();
    }

    // LEGACY METHODS
    public double getTongTienHangHomNay(String maNV, int ca) { 
        return dao.getTongTienHangHomNay(maNV, ca); 
    }
    public double getTienHoanTraTheoCa(String maNV, LocalDateTime start) { 
        return dao.getTienHoanTraTheoCa(maNV, start); 
    }
    public double getTongKhuyenMaiHomNay(String maNV, int ca) { 
        return dao.getTongKhuyenMaiHomNay(maNV, ca); 
    }
    private boolean kiemTraThoiGianHople(LocalDateTime tuNgay, LocalDateTime denNgay) {
        if (tuNgay == null || denNgay == null) return false;
        if (tuNgay.isAfter(denNgay)) return false;
        return true;
    }

    public int getTongSoDonHang(LocalDateTime tuNgay, LocalDateTime denNgay) {
        return kiemTraThoiGianHople(tuNgay, denNgay) ? dao.demSoLuongHoaDon(tuNgay, denNgay) : 0;
    }

    public double getTongDoanhThu(LocalDateTime tuNgay, LocalDateTime denNgay) {
        return kiemTraThoiGianHople(tuNgay, denNgay) ? dao.tinhDoanhThu(tuNgay, denNgay) : 0.0;
    }

    public double getGiaTriTBTrenDon(LocalDateTime tuNgay, LocalDateTime denNgay) {
        if (!kiemTraThoiGianHople(tuNgay, denNgay)) return 0.0;
        int so = getTongSoDonHang(tuNgay, denNgay);
        return so == 0 ? 0.0 : getTongDoanhThu(tuNgay, denNgay) / so;
    }

    public double getTongLoiNhuan(LocalDateTime tuNgay, LocalDateTime denNgay) {
        return kiemTraThoiGianHople(tuNgay, denNgay) ? dao.tinhLoiNhuan(tuNgay, denNgay) : 0.0;
    }

    // NHÂN VIÊN DƯỢC SĨ

    /** Danh sách dược sĩ đang hoạt động: List<String[3]> = {id, hoVaTen, chucVu} */
    public List<String[]> getDuocSiList() {
        return dao.getDuocSiList();
    }

    // DOANH THU & CHI PHÍ 12 THÁNG

    /**
     * Doanh thu 12 tháng theo năm và điều kiện lọc (triệu đồng).
     * condHD: chuỗi điều kiện SQL bổ sung (rỗng = không lọc thêm)
     */
    public double[] getDoanhThu12Thang(int year, String condHD) {
        if (year <= 0) return new double[12];
        return dao.getDoanhThu12Thang(year, condHD);
    }

    /**
     * Chi phí nhập hàng 12 tháng (triệu đồng).
     * condPN: điều kiện SQL bổ sung cho bảng LoHang
     */
    public double[] getChiPhi12Thang(int year, String condPN) {
        if (year <= 0) return new double[12];
        return dao.getChiPhi12Thang(year, condPN);
    }

    // DONUT - PHÂN LOẠI SẢN PHẨM

    /**
     * int[4]: % số lượng bán theo loại SP
     * Thứ tự: [THUOC_KE_DON, THUOC_KHONG_KE_DON, THUC_PHAM_CHUC_NANG, MY_PHAM]
     */
    public int[] getSoLuongTheoLoaiSP(int year, String condHD) {
        return dao.getSoLuongTheoLoaiSP(year, condHD);
    }

    // THỐNG KÊ THEO NGÀY

    /** 10 ngày gần nhất có HĐ, sắp xếp tăng dần (dd/MM/yyyy) */
    public List<String> get10NgayGanNhat(int year, String condHD) {
        return dao.get10NgayGanNhat(year, condHD);
    }

    /** Số HĐ của 1 NV trong 1 ngày cụ thể */
    public int getDailyHDCuaNV(String nvId, String date, String condHD) {
        if (nvId == null || nvId.isEmpty() || date == null) return 0;
        return dao.getDailyHDCuaNV(nvId, date, condHD);
    }

    /**
     * Dữ liệu 30 ngày gần nhất.
     * Mỗi Object[3]: {date(String), hdCount(int), dt_triệu(double)}
     */
    public List<Object[]> getThongKe30NgayGanNhat(String condHD) {
        return dao.getThongKe30NgayGanNhat(condHD);
    }

    // NHÂN VIÊN - CA SÁNG / CHIỀU

    /** double[2] = {hdCount, dt_triệu} cho ca SÁNG (trước 14h) của 1 NV */
    public double[] getKetQuaCaSang(String nvId, int year, String condHD) {
        if (nvId == null || nvId.isEmpty()) return new double[]{0, 0};
        return dao.getKetQuaCaSang(nvId, year, condHD);
    }

    /** double[2] = {hdCount, dt_triệu} cho ca CHIỀU (14h–22h) của 1 NV */
    public double[] getKetQuaCaChieu(String nvId, int year, String condHD) {
        if (nvId == null || nvId.isEmpty()) return new double[]{0, 0};
        return dao.getKetQuaCaChieu(nvId, year, condHD);
    }

    /** double[2] = {hdCount, dt_triệu} cho ca TỐI (22h–6h) của 1 NV */
    public double[] getKetQuaCaToi(String nvId, int year, String condHD) {
        if (nvId == null || nvId.isEmpty()) return new double[]{0, 0};
        return dao.getKetQuaCaToi(nvId, year, condHD);
    }

    // TỔNG HÓA ĐƠN

    /** Tổng số HĐ bán hàng theo năm + điều kiện lọc */
    public long getTongHoaDon(int year, String condHD) {
        return dao.getTongHoaDon(year, condHD);
    }

    // TOP SẢN PHẨM

    /**
     * Top 10 SP bán chạy.
     * Mỗi Object[4]: {tenSP, danhMuc, soLuong(int), doanhThu_triệu(double)}
     */
    public List<Object[]> getTopSanPham(int year, String condHD) {
        return dao.getTopSanPham(year, condHD);
    }

    // BÁO CÁO VAT

    /**
     * Dữ liệu VAT top 20 SP.
     * Mỗi Object[5]: {maSP, tenSP, danhMuc, vatPct(int), tienThue_triệu(double)}
     */
    public List<Object[]> getVATReport(int year, String condHD) {
        return dao.getVATReport(year, condHD);
    }
    
    // SẢN PHẨM SẮP HẾT HẠN
    
    /**
     * Lô hàng còn hàng, hết hạn trong 6 tháng.
     * Mỗi Object[5]: {soLoHang, tenSP, maKho, soLuong(int), ngayHetHan(String)}
     */
    public List<Object[]> getSpSapHetHan() {
        return dao.getSpSapHetHan();
    }
    
    // CÁC HÀM BỔ SUNG CHO MÀN HÌNH CHÍNH (DASHBOARD)
    
    public int getTongSanPham() { return dao.getTongSanPham(); }
    public int getTongKhachHang() { return dao.getTongKhachHang(); }
    public int getHoaDonHomNay(String maNV) { return dao.getHoaDonHomNay(maNV); }
    public double getDoanhThuHomNay(String maNV) { return dao.getDoanhThuHomNay(maNV); }
    public double getDoanhThuTienMatHomNay(String maNV) { return dao.getDoanhThuTienMatHomNay(maNV); }
    
    public List<Object[]> getHoaDonGanDayTrongCa(String maNV) { return dao.getHoaDonGanDayTrongCa(maNV); }
    public List<Object[]> getTop4SanPhamSapHetHang() { return dao.getTop4SanPhamSapHetHang(); }
    public int getSoSanPhamDuTon() { return dao.getSoSanPhamDuTon(); }
    public int getSoLoHangSapHetHanKhoang(int days) { return dao.getSoLoHangSapHetHanKhoang(days); }
    public List<Object[]> getLoHangSapHetHanNhanh(int days) { return dao.getLoHangSapHetHanNhanh(days); }
    
    public double getDoanhThu7NgayQua(String maNV) { return dao.getDoanhThu7NgayQua(maNV); }
    public int getSoHoaDon7NgayQua(String maNV) { return dao.getSoHoaDon7NgayQua(maNV); }
    public int getTongPhieuDoiTra() { return dao.getTongPhieuDoiTra(); }
    public int getPhieuDoiTraChoXuLy() { return dao.getPhieuDoiTraChoXuLy(); }
    
    public int getSoHoaDonTheoCa(String maNV, LocalDateTime start) { return dao.getSoHoaDonTheoCa(maNV, start); }
    public double getDoanhThuTheoCa(String maNV, LocalDateTime start) { return dao.getDoanhThuTheoCa(maNV, start); }
    public double getDoanhThuTienMatTheoCa(String maNV, LocalDateTime start) { return dao.getDoanhThuTienMatTheoCa(maNV, start); }
    public List<String[]> getDanhSachNhanVien() { return dao.getDanhSachNhanVien(); }
    public int getHoaDonHomNay(String maNV, int ca) { return dao.getHoaDonHomNay(maNV, ca); }
    public double getDoanhThuHomNay(String maNV, int ca) { return dao.getDoanhThuHomNay(maNV, ca); }
    public double getDoanhThuTienMatHomNay(String maNV, int ca) { return dao.getDoanhThuTienMatHomNay(maNV, ca); }
    public List<Object[]> getHoaDonGanDayTrongCa(String maNV, int ca) { return dao.getHoaDonGanDayTrongCa(maNV, ca); }

    // ==================== THỐNG KÊ NGÀY CỤ THỂ ====================

    /**
     * KPI tổng hợp của 1 ngày cụ thể (định dạng yyyy-MM-dd).
     * Object[4]: {tongHD(int), tongDT_trieu(double), tongKH(int), tongSPBan(int)}
     */
    public Object[] getThongKeNgayCuThe(String dateYMD) {
        if (dateYMD == null || dateYMD.isEmpty()) return new Object[]{0, 0.0, 0, 0};
        return dao.getThongKeNgayCuThe(dateYMD);
    }

    /**
     * Top 10 SP bán nhiều trong 1 ngày cụ thể.
     * Mỗi Object[3]: {tenSP(String), soLuong(int), doanhThu_trieu(double)}
     */
    public List<Object[]> getTopSPTrongNgay(String dateYMD) {
        if (dateYMD == null || dateYMD.isEmpty()) return new java.util.ArrayList<>();
        return dao.getTopSPTrongNgay(dateYMD);
    }

    /**
     * Doanh thu theo từng giờ trong ngày (0–23).
     * Trả về double[24] (đơn vị: triệu đồng)
     */
    public double[] getDTTheoGioTrongNgay(String dateYMD) {
        if (dateYMD == null || dateYMD.isEmpty()) return new double[24];
        return dao.getDTTheoGioTrongNgay(dateYMD);
    }

    /**
     * Danh sách NV làm việc và doanh số trong ngày.
     * Mỗi Object[3]: {hoVaTen(String), soHD(int), doanhThu_trieu(double)}
     */
    public List<Object[]> getNVTrongNgay(String dateYMD) {
        if (dateYMD == null || dateYMD.isEmpty()) return new java.util.ArrayList<>();
        return dao.getNVTrongNgay(dateYMD);
    }

    // ==================== THỐNG KÊ KHÁCH HÀNG ====================

    /**
     * KH mới từng tháng trong năm.
     * Trả về int[12]
     */
    public int[] getKHMoiTheoThang(int year) {
        if (year <= 0) return new int[12];
        return dao.getKHMoiTheoThang(year);
    }

    /**
     * Top 10 KH mua nhiều nhất trong năm.
     * Mỗi Object[4]: {hoVaTen, soHD(int), tongDT_trieu(double), diemTichLuy(int)}
     */
    public List<Object[]> getTopKhachHang(int year, String condHD) {
        return dao.getTopKhachHang(year, condHD != null ? condHD : "");
    }

    /**
     * KPI tổng hợp khách hàng.
     * Object[3]: {tongKH(int), khCoTK(int), tongDiem(int)}
     */
    public Object[] getKpiKhachHang() {
        return dao.getKpiKhachHang();
    }

    // ==================== THỐNG KÊ KHO HÀNG ====================

    /** Tổng giá trị tồn kho (đơn vị: VND) */
    public double getTongGiaTriTonKho() {
        return dao.getTongGiaTriTonKho();
    }

    /**
     * Số lô theo trạng thái.
     * Object[3]: {soLoConHang(int), soLoHetHang(int), soLoHetHan(int)}
     */
    public Object[] getSoLoTheoTrangThai() {
        return dao.getSoLoTheoTrangThai();
    }

    /**
     * Phân bổ tồn kho theo từng kho.
     * Mỗi Object[3]: {maKho(String), soLuong(int), giaTriTrieu(double)}
     */
    public List<Object[]> getTonKhoTheoKho() {
        return dao.getTonKhoTheoKho();
    }

    /**
     * Top 10 SP tồn kho nhiều nhất.
     * Mỗi Object[3]: {tenSP(String), soLuongTon(int), giaTriTrieu(double)}
     */
    public List<Object[]> getTopSPTonNhieu() {
        return dao.getTopSPTonNhieu();
    }

    /**
     * Giá trị nhập hàng 12 tháng (triệu đồng).
     * Trả về double[12]
     */
    public double[] getNhapHang12Thang(int year) {
        if (year <= 0) return new double[12];
        return dao.getNhapHang12Thang(year);
    }
    public double getDoanhThuThuanHomNay(String maNV, int ca) { 
        return dao.getDoanhThuThuanHomNay(maNV, ca); 
    }
}