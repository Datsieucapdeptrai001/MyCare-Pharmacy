package BUS;

import DAO.DAO_HoaDon;
import Entity.HoaDon;
import Enumeration.LoaiHoaDon;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BUS_TraHang {
    private DAO_HoaDon daoHoaDon;

    public BUS_TraHang() {
        this.daoHoaDon = new DAO_HoaDon();
    }

    public boolean kiemTraDieuKien(String maHoaDonGoc) {
        HoaDon hd = daoHoaDon.layHoaDonTheoMa(maHoaDonGoc);
        if (hd == null) return false;
        if (hd.getLoaiHD() != LoaiHoaDon.BAN_HANG) return false;
        
        LocalDateTime ngayHetHanTra = hd.getNgayLapHD().plusDays(7);
        return !LocalDateTime.now().isAfter(ngayHetHanTra);
    }

    public double xacDinhMucHoanTien(double tongTienHD, double phanTramHoan) {
        return tongTienHD * (phanTramHoan / 100.0);
    }

    public double tinhTienChenhLech(double tienKhachMua, double tienHoanLai) {
        return tienKhachMua - tienHoanLai;
    }

    public List<Object[]> layDanhSachPhieu() {
        List<Object[]> rawList = daoHoaDon.layDanhSachPhieuDoiTra();
        return parseRawPhieu(rawList);
    }

    /** STAFF: Chỉ phiếu của nhân viên đó, hôm nay. Trả null nếu maNV rỗng. */
    public List<Object[]> layDanhSachPhieuCuaNhanVien(String maNV) {
        List<Object[]> rawList = daoHoaDon.layDanhSachPhieuDoiTraTheoNVHomNay(maNV);
        if (rawList == null) return null;
        return parseRawPhieu(rawList);
    }

    private List<Object[]> parseRawPhieu(List<Object[]> rawList) {
        List<Object[]> result = new ArrayList<>();
        for (Object[] row : rawList) {
            String ghiChuDB = (String) row[4];
            String trangThai = "Chờ xử lý";
            String loi = "Chưa xác định";
            String tienHoan = "0đ";
            String chenhLech = "0đ";
            if (ghiChuDB != null && ghiChuDB.contains("|")) {
                String[] parts = ghiChuDB.split("\\|");
                if (parts.length > 0) trangThai = parts[0].trim();
                if (parts.length > 1) loi = parts[1].trim();
                if (parts.length > 2) tienHoan = parts[2].trim();
                if (parts.length > 3) chenhLech = parts[3].trim();
            }
            result.add(new Object[]{ row[0], row[1], row[2], row[3], loi, tienHoan, chenhLech, trangThai, row[5], "" });
        }
        return result;
    }

    public boolean xacNhanGiaoDichDoiTra(String maPhieu, String trangThaiMoi) {
        return daoHoaDon.capNhatTrangThaiPhieuDoiTra(maPhieu, trangThaiMoi);
    }

    // LOGIC BUS: BÓC TÁCH SẢN PHẨM TRỰC TIẾP TỪ GHI CHÚ HÓA ĐƠN
    private List<Object[]> parseGhiChuLaySanPham(String maPhieu, boolean isTraLai) {
        List<Object[]> result = new ArrayList<>();
        HoaDon hd = daoHoaDon.layHoaDonTheoMa(maPhieu);
        if (hd == null || hd.getGhiChu() == null) return result;

        String[] parts = hd.getGhiChu().split("\\|");
        String targetPart = (isTraLai && parts.length > 4) ? parts[4].trim() : 
                            (!isTraLai && parts.length > 5) ? parts[5].trim() : "";

        targetPart = targetPart.replace("TRA: ", "").replace("DOI: ", "");
        if (targetPart.isEmpty() || targetPart.equals("Không có")) return result;

        String[] items = targetPart.split("; ");
        for (String item : items) {
            if (item.contains("_")) {
                String[] vals = item.split("_");
                // Format ghiChu: tenSP_DVT_soLuong_giaChuan
                // vals[0]=ten, vals[1]=dvt, vals[2]=soLuong, vals[3]=gia
                String ten = vals[0];
                String dvt = vals.length > 1 ? vals[1] : "Hộp";
                int sl = 1;
                if (vals.length > 2) {
                    try { sl = Integer.parseInt(vals[2].trim()); } catch (Exception e) {}
                }
                long donGia = 0;
                if (vals.length > 3) {
                    try {
                        String strGia = vals[3].replaceAll(",00$|\\.00$|,0$|\\.0$", "")
                                                .replaceAll("[^0-9]", "");
                        if (!strGia.isEmpty()) donGia = Long.parseLong(strGia);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // [0]=ten, [1]=sl(int), [2]=dvt(String), [3]=donGia(long)
                result.add(new Object[]{ ten, sl, dvt, donGia });
            }
        }
        return result;
    }

    public List<Object[]> layDanhSachSanPhamDoi(String maPhieu) {
        return parseGhiChuLaySanPham(maPhieu, false); // false = Lấy SP Đổi
    }

    // Đã thêm hàm này vào BUS_TraHang.java
    public List<Object[]> layChiTietPhieu(String maPhieu) {
        // true = Lấy danh sách Sản Phẩm Trả lại
        return parseGhiChuLaySanPham(maPhieu, true); 
    }

    public String laySoDienThoaiKhachHang(String maHDGoc) {
        HoaDon hd = daoHoaDon.layHoaDonTheoMa(maHDGoc);
        if (hd != null && hd.getKhachHangId() != null) {
            String sdt = hd.getKhachHangId().getId();
            if (sdt != null && !sdt.trim().isEmpty()) {
                return sdt;
            }
        }
        return "Không cung cấp";
    }
}