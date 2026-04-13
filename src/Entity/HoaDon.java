package Entity;

import java.time.LocalDateTime;
import java.util.Objects;

import Enumeration.LoaiHoaDon;
import Enumeration.PhuongThucThanhToan;

public class HoaDon {
	private String id;
	private LoaiHoaDon loaiHD;
	private String ghiChu;
	private LocalDateTime ngayLapHD;
	private NhanVien nhanVienId;
	private KhachHang KhachHangId;
	private KhuyenMai khuyenMaiId;
	private PhuongThucThanhToan phuongThucThanhToan;
	private HoaDon hoaDonGocId;
	public HoaDon() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public HoaDon(String id, LoaiHoaDon loaiHD, String ghiChu, LocalDateTime ngayLapHD, NhanVien nhanVienId,
			KhachHang khachHangId, KhuyenMai khuyenMaiId, PhuongThucThanhToan phuongThucThanhToan, HoaDon hoaDonGocId) {
		super();
		this.id = id;
		this.loaiHD = loaiHD;
		this.ghiChu = ghiChu;
		this.ngayLapHD = ngayLapHD;
		this.nhanVienId = nhanVienId;
		KhachHangId = khachHangId;
		this.khuyenMaiId = khuyenMaiId;
		this.phuongThucThanhToan = phuongThucThanhToan;
		this.hoaDonGocId = hoaDonGocId;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public LoaiHoaDon getLoaiHD() {
		return loaiHD;
	}
	public void setLoaiHD(LoaiHoaDon loaiHD) {
		this.loaiHD = loaiHD;
	}
	public String getGhiChu() {
		return ghiChu;
	}
	public void setGhiChu(String ghiChu) {
		this.ghiChu = ghiChu;
	}
	public LocalDateTime getNgayLapHD() {
		return ngayLapHD;
	}
	public void setNgayLapHD(LocalDateTime ngayLapHD) {
		this.ngayLapHD = ngayLapHD;
	}
	public NhanVien getNhanVienId() {
		return nhanVienId;
	}
	public void setNhanVienId(NhanVien nhanVienId) {
		this.nhanVienId = nhanVienId;
	}
	public KhachHang getKhachHangId() {
		return KhachHangId;
	}
	public void setKhachHangId(KhachHang khachHangId) {
		KhachHangId = khachHangId;
	}
	public KhuyenMai getKhuyenMaiId() {
		return khuyenMaiId;
	}
	public void setKhuyenMaiId(KhuyenMai khuyenMaiId) {
		this.khuyenMaiId = khuyenMaiId;
	}
	public PhuongThucThanhToan getPhuongThucThanhToan() {
		return phuongThucThanhToan;
	}
	public void setPhuongThucThanhToan(PhuongThucThanhToan phuongThucThanhToan) {
		this.phuongThucThanhToan = phuongThucThanhToan;
	}
	public HoaDon getHoaDonGocId() {
		return hoaDonGocId;
	}
	public void setHoaDonGocId(HoaDon hoaDonGocId) {
		this.hoaDonGocId = hoaDonGocId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(KhachHangId, ghiChu, hoaDonGocId, id, khuyenMaiId, loaiHD, ngayLapHD, nhanVienId,
				phuongThucThanhToan);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HoaDon other = (HoaDon) obj;
		return Objects.equals(KhachHangId, other.KhachHangId) && Objects.equals(ghiChu, other.ghiChu)
				&& Objects.equals(hoaDonGocId, other.hoaDonGocId) && Objects.equals(id, other.id)
				&& Objects.equals(khuyenMaiId, other.khuyenMaiId) && loaiHD == other.loaiHD
				&& Objects.equals(ngayLapHD, other.ngayLapHD) && Objects.equals(nhanVienId, other.nhanVienId)
				&& phuongThucThanhToan == other.phuongThucThanhToan;
	}

	@Override
	public String toString() {
		return "HoaDon [id=" + id + ", loaiHD=" + loaiHD + ", ghiChu=" + ghiChu + ", ngayLapHD=" + ngayLapHD
				+ ", nhanVienId=" + nhanVienId + ", KhachHangId=" + KhachHangId + ", khuyenMaiId=" + khuyenMaiId
				+ ", phuongThucThanhToan=" + phuongThucThanhToan + ", hoaDonGocId=" + hoaDonGocId + "]";
	}
	
	
	
}
