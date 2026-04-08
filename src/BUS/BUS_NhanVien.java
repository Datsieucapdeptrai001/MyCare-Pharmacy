package BUS;

import DAO.DAO_NhanVien;
import Entity.NhanVien;
import Enum.ChucVu;
import Enum.TrangThaiLamViec;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BUS_NhanVien {
    private DAO_NhanVien daoNhanVien;

    public BUS_NhanVien() {
        this.daoNhanVien = new DAO_NhanVien();
    }

    public List<NhanVien> layDSNhanVien() {
        return daoNhanVien.layDSNhanVien();
    }

    // Validate dữ liệu trước khi gửi xuống DAO
    public boolean validateThongTin(NhanVien nv) {
        if (nv.getNhanVien() == null || nv.getNhanVien().trim().isEmpty()) {
            System.out.println("Lỗi: Mã nhân viên không được để trống.");
            return false;
        }
        if (nv.getHoVaTen() == null || nv.getHoVaTen().trim().isEmpty()) {
            System.out.println("Lỗi: Họ và tên không được để trống.");
            return false;
        }
        
        // SĐT phải có 10 số và bắt đầu bằng số 0
        String sdtRegex = "^0\\d{9}$";
        if (nv.getSdt() == null || !Pattern.matches(sdtRegex, nv.getSdt())) {
            System.out.println("Lỗi: Số điện thoại không hợp lệ (phải có 10 số, bắt đầu bằng 0).");
            return false;
        }
        
        // Email có thể rỗng, nhưng nếu nhập thì phải đúng định dạng
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (nv.getEmail() != null && !nv.getEmail().trim().isEmpty() && !Pattern.matches(emailRegex, nv.getEmail())) {
            System.out.println("Lỗi: Email sai định dạng.");
            return false;
        }
        
        return true;
    }

    public boolean themNhanVien(NhanVien nv) {
        if (validateThongTin(nv)) {
            // Kiểm tra xem nhân viên này đã tồn tại trong DB chưa
            if (daoNhanVien.layNhanVienTheoMa(nv.getNhanVien()) != null) {
                System.out.println("Lỗi: Mã nhân viên '" + nv.getNhanVien() + "' đã tồn tại trong hệ thống.");
                return false;
            }
            return daoNhanVien.themNhanVien(nv);
        }
        return false;
    }

    public boolean capNhatNhanVien(NhanVien nv) {
        if (validateThongTin(nv)) {
            return daoNhanVien.capNhatNhanVien(nv);
        }
        return false;
    }

    // Chức năng thanh tìm kiếm (Tìm theo mã, theo tên hoặc theo SĐT)
    public List<NhanVien> traCuuNhanVien(String tuKhoa) {
        List<NhanVien> dsToanBo = daoNhanVien.layDSNhanVien();
        List<NhanVien> dsKetQua = new ArrayList<>();
        
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return dsToanBo;
        }
        
        tuKhoa = tuKhoa.toLowerCase(); // Đưa về chữ thường để so sánh không phân biệt hoa/thường
        for (NhanVien nv : dsToanBo) {
            if (nv.getNhanVien().toLowerCase().contains(tuKhoa) || 
                nv.getHoVaTen().toLowerCase().contains(tuKhoa) ||
                nv.getSdt().contains(tuKhoa)) {
                dsKetQua.add(nv);
            }
        }
        return dsKetQua;
    }

    // Lọc danh sách nhân viên theo chức vụ (vd: Lọc ra tất cả Dược Sĩ)
    public List<NhanVien> phanLoaiNV(ChucVu loaiChucVu) {
        List<NhanVien> dsToanBo = daoNhanVien.layDSNhanVien();
        List<NhanVien> dsKetQua = new ArrayList<>();
        
        for (NhanVien nv : dsToanBo) {
            if (nv.getChucVu() == loaiChucVu) {
                dsKetQua.add(nv);
            }
        }
        return dsKetQua;
    }

    // Hàm gọi lấy 1 nhân viên cụ thể theo mã
    public NhanVien layNhanVienTheoMa(String maNV) {
        return daoNhanVien.layNhanVienTheoMa(maNV);
    }
}