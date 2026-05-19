package Entity;

import Enumeration.DangBaoChe;
import Enumeration.DanhMucSanPham;

import java.time.LocalDateTime;

public class SanPham {
    private String id;
    private DanhMucSanPham danhMuc;
    private DangBaoChe dang;
    private String ten;
    private String tenVietTat;
    private String nhaSanXuat;
    private String hoatChat;
    private double thueVAT;
    private String hamLuong;
    private String moTa;
    private String donViDoCoBan;
    private LocalDateTime ngayTao;
    private double giaBan;
    private String maVach;
    private String nhomBenhLy;
    private String viTriId;     // FK → ViTriThuoc.id
    private String viTriKhu;    // denormalized để hiển thị không cần join thêm
    private String viTriKe;
    private String viTriTang;

    // =========================================================================
    // INNER CLASS: ViTriThuoc — gộp vào SanPham, không tạo file riêng
    // Dùng SanPham.ViTriThuoc ở mọi nơi thay cho class độc lập
    // =========================================================================
    public static class ViTriThuoc {
        private String id;
        private String khu;   // OTC, Kê đơn, Tủ lạnh, Thảo dược, Mỹ phẩm
        private String ke;    // Kệ Cảm - Sốt, Tủ Kháng sinh...
        private String tang;  // Tầng 1, Tầng 2 (nullable)
        private String moTa;
        private int soSanPham; // số SP đang dùng vị trí này (dùng trong quản lý vị trí)

        public ViTriThuoc() {}
        public ViTriThuoc(String id, String khu, String ke, String tang, String moTa) {
            this.id = id; this.khu = khu; this.ke = ke; this.tang = tang; this.moTa = moTa;
        }

        public String getId()    { return id; }
        public void setId(String id) { this.id = id; }
        public String getKhu()   { return khu; }
        public void setKhu(String khu) { this.khu = khu; }
        public String getKe()    { return ke; }
        public void setKe(String ke) { this.ke = ke; }
        public String getTang()  { return tang; }
        public void setTang(String tang) { this.tang = tang; }
        public String getMoTa()  { return moTa; }
        public void setMoTa(String moTa) { this.moTa = moTa; }
        public int getSoSanPham() { return soSanPham; }
        public void setSoSanPham(int soSanPham) { this.soSanPham = soSanPham; }

        /** Hiển thị đầy đủ dùng cho ComboBox / label: "OTC / Kệ Cảm - Sốt / Tầng 1" */
        public String toDisplayString() {
            StringBuilder sb = new StringBuilder(khu).append(" / ").append(ke);
            if (tang != null && !tang.trim().isEmpty()) sb.append(" / ").append(tang);
            return sb.toString();
        }

        @Override
        public String toString() { return toDisplayString(); }
    }

    public SanPham() {
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public DanhMucSanPham getDanhMuc() { return danhMuc; }
    public void setDanhMuc(DanhMucSanPham danhMuc) { this.danhMuc = danhMuc; }

    public DangBaoChe getDang() { return dang; }
    public void setDang(DangBaoChe dang) { this.dang = dang; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getTenVietTat() { return tenVietTat; }
    public void setTenVietTat(String tenVietTat) { this.tenVietTat = tenVietTat; }

    public String getNhaSanXuat() { return nhaSanXuat; }
    public void setNhaSanXuat(String nhaSanXuat) { this.nhaSanXuat = nhaSanXuat; }

    public String getHoatChat() { return hoatChat; }
    public void setHoatChat(String hoatChat) { this.hoatChat = hoatChat; }

    public double getThueVAT() { return thueVAT; }
    public void setThueVAT(double thueVAT) { this.thueVAT = thueVAT; }

    public String getHamLuong() { return hamLuong; }
    public void setHamLuong(String hamLuong) { this.hamLuong = hamLuong; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getDonViDoCoBan() { return donViDoCoBan; }
    public void setDonViDoCoBan(String donViDoCoBan) { this.donViDoCoBan = donViDoCoBan; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    public String getMaVach() { return maVach; }
    public void setMaVach(String maVach) { this.maVach = maVach; }

    public String getNhomBenhLy() { return nhomBenhLy; }
    public void setNhomBenhLy(String nhomBenhLy) { this.nhomBenhLy = nhomBenhLy; }

    public String getViTriId() { return viTriId; }
    public void setViTriId(String viTriId) { this.viTriId = viTriId; }

    public String getViTriKhu()  { return viTriKhu; }
    public void setViTriKhu(String viTriKhu) { this.viTriKhu = viTriKhu; }

    public String getViTriKe()   { return viTriKe; }
    public void setViTriKe(String viTriKe) { this.viTriKe = viTriKe; }

    public String getViTriTang() { return viTriTang; }
    public void setViTriTang(String viTriTang) { this.viTriTang = viTriTang; }

    /** Trả về chuỗi vị trí hiển thị đầy đủ — dùng trực tiếp trong UI */
    public String getViTriDisplay() {
        if (viTriKhu == null && viTriKe == null) return "";
        StringBuilder sb = new StringBuilder();
        if (viTriKhu != null) sb.append(viTriKhu);
        if (viTriKe  != null) sb.append(" / ").append(viTriKe);
        if (viTriTang != null && !viTriTang.trim().isEmpty()) sb.append(" / ").append(viTriTang);
        return sb.toString();
    }

    @Override
    public String toString() {
        return ten + " (" + id + ")";
    }
}