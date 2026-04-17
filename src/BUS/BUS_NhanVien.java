package BUS;

import DAO.DAO_NhanVien;
import Entity.NhanVien;
import Enumeration.ChucVu;

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

    // Đưa code tạo mã từ giao diện xuống BUS
    public String taoMaMoi() {
        return "NV" + String.format("%03d", System.currentTimeMillis() % 1000);
    }

    public boolean themNhanVien(NhanVien nv) {
        if (validateThongTin(nv)) {
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

    public List<NhanVien> traCuuNhanVien(String tuKhoa) {
        List<NhanVien> dsToanBo = daoNhanVien.layDSNhanVien();
        List<NhanVien> dsKetQua = new ArrayList<>();
        
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return dsToanBo;
        }
        
        tuKhoa = tuKhoa.toLowerCase(); 
        for (NhanVien nv : dsToanBo) {
            if (nv.getNhanVien().toLowerCase().contains(tuKhoa) || 
                nv.getHoVaTen().toLowerCase().contains(tuKhoa) ||
                nv.getSdt().contains(tuKhoa)) {
                dsKetQua.add(nv);
            }
        }
        return dsKetQua;
    }

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

    public NhanVien layNhanVienTheoMa(String maNV) {
        return daoNhanVien.layNhanVienTheoMa(maNV);
    }
}