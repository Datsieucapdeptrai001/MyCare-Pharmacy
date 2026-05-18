package Entity;

public class LieuMau {
    private String id;
    private String tenLieu;
    private String nhomBenh;
    private String moTa;

    public LieuMau() {}

    public LieuMau(String id, String tenLieu, String nhomBenh, String moTa) {
        this.id = id;
        this.tenLieu = tenLieu;
        this.nhomBenh = nhomBenh;
        this.moTa = moTa;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTenLieu() { return tenLieu; }
    public void setTenLieu(String tenLieu) { this.tenLieu = tenLieu; }
    public String getNhomBenh() { return nhomBenh; }
    public void setNhomBenh(String nhomBenh) { this.nhomBenh = nhomBenh; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
}