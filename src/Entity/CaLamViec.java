package Entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class CaLamViec {
	private String id;
	private NhanVien nhanVienId;
	private LocalDateTime thoiGianBatDau;
	private LocalDateTime thoiGianKetThuc;
	private double tienHeThongGhiNhan;
	private double tienDauCa;
	private double tienKetCa;
	public CaLamViec() {
		super();
		// TODO Auto-generated constructor stub
	}
	public CaLamViec(String id, NhanVien nhanVienId, LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc,
			double tienHeThongGhiNhan, double tienDauCa, double tienKetCa) {
		super();
		this.id = id;
		this.nhanVienId = nhanVienId;
		this.thoiGianBatDau = thoiGianBatDau;
		this.thoiGianKetThuc = thoiGianKetThuc;
		this.tienHeThongGhiNhan = tienHeThongGhiNhan;
		this.tienDauCa = tienDauCa;
		this.tienKetCa = tienKetCa;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public NhanVien getNhanVienId() {
		return nhanVienId;
	}
	public void setNhanVienId(NhanVien nhanVienId) {
		this.nhanVienId = nhanVienId;
	}
	public LocalDateTime getThoiGianBatDau() {
		return thoiGianBatDau;
	}
	public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) {
		this.thoiGianBatDau = thoiGianBatDau;
	}
	public LocalDateTime getThoiGianKetThuc() {
		return thoiGianKetThuc;
	}
	public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) {
		this.thoiGianKetThuc = thoiGianKetThuc;
	}
	public double getTienHeThongGhiNhan() {
		return tienHeThongGhiNhan;
	}
	public void setTienHeThongGhiNhan(double tienHeThongGhiNhan) {
		this.tienHeThongGhiNhan = tienHeThongGhiNhan;
	}
	public double getTienDauCa() {
		return tienDauCa;
	}
	public void setTienDauCa(double tienDauCa) {
		this.tienDauCa = tienDauCa;
	}
	public double getTienKetCa() {
		return tienKetCa;
	}
	public void setTienKetCa(double tienKetCa) {
		this.tienKetCa = tienKetCa;
	}
	@Override
	public int hashCode() {
		return Objects.hash(id, nhanVienId, thoiGianBatDau, thoiGianKetThuc, tienDauCa, tienHeThongGhiNhan, tienKetCa);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CaLamViec other = (CaLamViec) obj;
		return Objects.equals(id, other.id) && Objects.equals(nhanVienId, other.nhanVienId)
				&& Objects.equals(thoiGianBatDau, other.thoiGianBatDau)
				&& Objects.equals(thoiGianKetThuc, other.thoiGianKetThuc)
				&& Double.doubleToLongBits(tienDauCa) == Double.doubleToLongBits(other.tienDauCa)
				&& Double.doubleToLongBits(tienHeThongGhiNhan) == Double.doubleToLongBits(other.tienHeThongGhiNhan)
				&& Double.doubleToLongBits(tienKetCa) == Double.doubleToLongBits(other.tienKetCa);
	}
	@Override
	public String toString() {
		return "CaLamViec [id=" + id + ", nhanVienId=" + nhanVienId + ", thoiGianBatDau=" + thoiGianBatDau
				+ ", thoiGianKetThuc=" + thoiGianKetThuc + ", tienHeThongGhiNhan=" + tienHeThongGhiNhan + ", tienDauCa="
				+ tienDauCa + ", tienKetCa=" + tienKetCa + "]";
	}
	
}
