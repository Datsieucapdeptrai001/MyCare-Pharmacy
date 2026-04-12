package Entity;

import java.util.Objects;

public class KhoHang {
	private String id;
	private int sucChua;
	
	public KhoHang() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public KhoHang(String id, int sucChua) {
		super();
		this.id = id;
		this.sucChua = sucChua;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getSucChua() {
		return sucChua;
	}

	public void setSucChua(int sucChua) {
		this.sucChua = sucChua;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, sucChua);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KhoHang other = (KhoHang) obj;
		return Objects.equals(id, other.id) && sucChua == other.sucChua;
	}

	@Override
	public String toString() {
		return "KhoHang [id=" + id + ", sucChua=" + sucChua + "]";
	}
	
}
