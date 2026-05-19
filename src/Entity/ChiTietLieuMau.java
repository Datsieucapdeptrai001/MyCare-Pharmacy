package Entity;

public class ChiTietLieuMau {
    private String lieuMauId;
    private String sanPhamId;
    private int soLuong;

    public ChiTietLieuMau() {}

    public ChiTietLieuMau(String lieuMauId, String sanPhamId, int soLuong) {
        this.lieuMauId = lieuMauId;
        this.sanPhamId = sanPhamId;
        this.soLuong = soLuong;
    }

    public String getLieuMauId() { return lieuMauId; }
    public void setLieuMauId(String lieuMauId) { this.lieuMauId = lieuMauId; }
    public String getSanPhamId() { return sanPhamId; }
    public void setSanPhamId(String sanPhamId) { this.sanPhamId = sanPhamId; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
}