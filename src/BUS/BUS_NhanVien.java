package BUS;

import DAO.DAO_NhanVien;
import DAO.DAO_TaiKhoan;
import Entity.NhanVien;
import Entity.TaiKhoan;
import Enumeration.ChucVu;
import Enumeration.TrangThaiLamViec;
import Enumeration.VaiTro;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BUS_NhanVien {
    private DAO_NhanVien daoNhanVien;
    private DAO_TaiKhoan daoTaiKhoan;

    public BUS_NhanVien() {
        this.daoNhanVien = new DAO_NhanVien();
        this.daoTaiKhoan = new DAO_TaiKhoan();
    }

    public List<NhanVien> layDSNhanVien() {
        return daoNhanVien.layDSNhanVien();
    }

    // Kiểm tra tính hợp lệ của dữ liệu nhân viên
    public boolean validateThongTin(NhanVien nv) {
        if (nv.getNhanVien() == null || nv.getNhanVien().trim().isEmpty()) {
            return false;
        }
        if (nv.getHoVaTen() == null || nv.getHoVaTen().trim().isEmpty()) {
            return false;
        }
        
        String sdtRegex = "^0\\d{9}$";
        if (nv.getSdt() == null || !Pattern.matches(sdtRegex, nv.getSdt())) {
            return false;
        }
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (nv.getEmail() != null && !nv.getEmail().trim().isEmpty() && !Pattern.matches(emailRegex, nv.getEmail())) {
            return false;
        }
        return true;
    }

    // =========================================================================
    // PHẦN 1: CÁC HÀM XỬ LÝ ĐỘC LẬP CHO NHÂN VIÊN (ĐÃ KHÔI PHỤC)
    // =========================================================================

    public boolean themNhanVien(NhanVien nv) {
        if (validateThongTin(nv)) {
            // Kiểm tra xem nhân viên này đã tồn tại trong DB chưa
            if (daoNhanVien.layNhanVienTheoMa(nv.getNhanVien()) != null) {
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

    // Lọc danh sách nhân viên theo chức vụ
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

    // =========================================================================
    // PHẦN 2: CÁC HÀM XỬ LÝ ĐỒNG BỘ NHÂN VIÊN VÀ TÀI KHOẢN (GIỮ NGUYÊN)
    // =========================================================================

    /**
     * Hàm gọi từ GUI để thêm Nhân Viên kèm theo Tạo Tài Khoản
     */
    public String themNhanVienVaTaiKhoan(NhanVien nv, String tenDangNhap, String matKhau) {
        if (!validateThongTin(nv)) return "Thông tin nhân viên không hợp lệ (Kiểm tra lại SĐT hoặc Email).";
        
        if (daoNhanVien.layNhanVienTheoMa(nv.getNhanVien()) != null) {
            return "Mã nhân viên đã tồn tại.";
        }

        if (daoTaiKhoan.checkTrungTenDangNhap(tenDangNhap)) {
            return "Tên đăng nhập đã tồn tại. Vui lòng chọn tên khác.";
        }

        // Bước 1: Lưu nhân viên
        if (daoNhanVien.themNhanVien(nv)) {
            // Bước 2: Tạo đối tượng tài khoản và lưu
            VaiTro vaiTro = (nv.getChucVu() == ChucVu.NGUOI_QUAN_LY) ? VaiTro.ADMIN : VaiTro.STAFF;
            String idTK = "TK" + System.currentTimeMillis() % 10000; // Khởi tạo mã TK ngẫu nhiên
            TaiKhoan tk = new TaiKhoan(idTK, nv, vaiTro, tenDangNhap, matKhau);
            
            if (daoTaiKhoan.themTaiKhoan(tk)) {
                return "SUCCESS";
            } else {
                return "Lưu nhân viên thành công nhưng tạo tài khoản thất bại.";
            }
        }
        return "Lỗi khi lưu thông tin nhân viên vào cơ sở dữ liệu.";
    }

    /**
     * Hàm gọi từ GUI để cập nhật Nhân Viên kèm Cập nhật Tài Khoản
     */
    public String capNhatNhanVienVaTaiKhoan(NhanVien nv, String tenDangNhap, String matKhau) {
        if (!validateThongTin(nv)) return "Thông tin nhân viên không hợp lệ.";

        TaiKhoan tkHienTai = daoTaiKhoan.layTaiKhoanTheoMaNV(nv.getNhanVien());
        
        // Nếu tên đăng nhập bị đổi thành tên khác, kiểm tra xem có trùng với người khác không
        if (tkHienTai != null && !tkHienTai.getTenDangNhap().equals(tenDangNhap)) {
            if (daoTaiKhoan.checkTrungTenDangNhap(tenDangNhap)) {
                return "Tên đăng nhập mới đã có người sử dụng.";
            }
        }

        // Cập nhật thông tin nhân viên
        if (daoNhanVien.capNhatNhanVien(nv)) {
            if (tkHienTai != null) {
                // Cập nhật tài khoản hiện có
                daoTaiKhoan.capNhatTaiKhoanTheoMaNV(nv.getNhanVien(), tenDangNhap, matKhau);
            } else {
                // Nếu chưa có tài khoản thì tạo mới bổ sung
                VaiTro vaiTro = (nv.getChucVu() == ChucVu.NGUOI_QUAN_LY) ? VaiTro.ADMIN : VaiTro.STAFF;
                String idTK = "TK" + System.currentTimeMillis() % 10000;
                TaiKhoan tk = new TaiKhoan(idTK, nv, vaiTro, tenDangNhap, matKhau);
                daoTaiKhoan.themTaiKhoan(tk);
            }
            return "SUCCESS";
        }
        return "Cập nhật thông tin nhân viên thất bại.";
    }

    /**
     * Lấy tài khoản để hiển thị lên form sửa
     */
    public TaiKhoan layTaiKhoanTheoMaNV(String maNV) {
        return daoTaiKhoan.layTaiKhoanTheoMaNV(maNV);
    }
}