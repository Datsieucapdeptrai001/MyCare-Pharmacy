package BUS;

import DAO.DAO_DieuKienKhuyenMai;
import DAO.DAO_HinhThucKhuyenMai;
import DAO.DAO_KhuyenMai;
import DAO.DAO_SanPham; 
import Entity.KhuyenMai;
import Entity.DieuKienKhuyenMai;
import Entity.HinhThucKhuyenMai;
import Enumeration.*;

import java.util.ArrayList; 
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BUS_KhuyenMai {
    private DAO_KhuyenMai daoKhuyenMai;
    private DAO_DieuKienKhuyenMai daoDieuKienKhuyenMai;
    private DAO_HinhThucKhuyenMai daoHinhThucKhuyenMai;
    private DAO_SanPham daoSanPham; 

    public BUS_KhuyenMai() {
        this.daoKhuyenMai = new DAO_KhuyenMai();
        this.daoDieuKienKhuyenMai = new DAO_DieuKienKhuyenMai();
        this.daoHinhThucKhuyenMai = new DAO_HinhThucKhuyenMai();
        this.daoSanPham = new DAO_SanPham(); 
    }

    public Object[] timKhuyenMaiTotNhat(double tongTienHoaDon) {
        List<String> dsMaKM = daoKhuyenMai.layDanhSachMaKMCoHieuLuc();
        String maTotNhat = null;
        double tienGiamMax = 0;

        for (String maKM : dsMaKM) {
            if (kiemTraDieuKienKhuyenMai(maKM, tongTienHoaDon)) {
                double giaSauGiam = apDungKM(maKM, tongTienHoaDon);
                double tienGiamThucTe = tongTienHoaDon - giaSauGiam;

                if (tienGiamThucTe > tienGiamMax) {
                    tienGiamMax = tienGiamThucTe;
                    maTotNhat = maKM;
                }
            }
        }

        if (maTotNhat != null) {
            return new Object[]{maTotNhat, tienGiamMax};
        }
        return null;
    }

    public boolean capNhatTrangThai(String maKM, boolean trangThai) {
        return daoKhuyenMai.capNhatTrangThai(maKM, trangThai);
    }

    public int[] layCauHinhTichDiem() {
        return daoKhuyenMai.layCauHinhTichDiem();
    }
    
    public boolean luuCauHinhTichDiem(int tienMua, int diemThuong, int tienDoi, int diemToiThieu) {
        return daoKhuyenMai.luuCauHinhTichDiem(tienMua, diemThuong, tienDoi, diemToiThieu);
    }

    public List<Object[]> layDanhSachKhuyenMaiChoTable() {
        return daoKhuyenMai.layDanhSachKhuyenMaiChoTable();
    }

    public List<KhuyenMai> layDsKhuyenMai() {
        return daoKhuyenMai.layDsKhuyenMai();
    }

    public boolean themKhuyenMai(KhuyenMai km) {
        if (!kiemTraThoiGian(km.getNgayBatDau(), km.getNgayKetThuc())) return false;
        if (kiemTraMaKM(km.getId())) return false;
        return daoKhuyenMai.themKhuyenMai(km);
    }
    
    public boolean themKhuyenMaiToanDien(KhuyenMai km, HinhThucKhuyenMai ht, DieuKienKhuyenMai dk) {
        if (!themKhuyenMai(km)) return false;
        daoHinhThucKhuyenMai.themHinhThuc(ht);
        if (dk != null) {
            daoDieuKienKhuyenMai.themDieuKien(dk);
        }
        return true;
    }

    public boolean capNhatKhuyenMaiToanDien(KhuyenMai km, HinhThucKhuyenMai ht, DieuKienKhuyenMai dk) {
        if (!kiemTraThoiGian(km.getNgayBatDau(), km.getNgayKetThuc())) return false;
        
        boolean isKmUpdated = daoKhuyenMai.capNhatKhuyenMai(km);
        if (isKmUpdated) {
            daoHinhThucKhuyenMai.capNhatHinhThuc(ht);
            if (dk != null) {
                if (daoDieuKienKhuyenMai.kiemTraTonTai(km.getId())) {
                    daoDieuKienKhuyenMai.capNhatDieuKienTheoMaKM(dk);
                } else {
                    daoDieuKienKhuyenMai.themDieuKien(dk);
                }
            } else {
                daoDieuKienKhuyenMai.xoaTheoMaKM(km.getId());
            }
            return true;
        }
        return false;
    }

    public boolean xoaKhuyenMaiToanDien(String maKM) {
        daoHinhThucKhuyenMai.xoaTheoMaKM(maKM);
        daoDieuKienKhuyenMai.xoaTheoMaKM(maKM);
        return daoKhuyenMai.xoaKhuyenMai(maKM);
    }

    public boolean kiemTraThoiGian(LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc) {
        if (ngayBatDau == null || ngayKetThuc == null) return false;
        return ngayBatDau.isBefore(ngayKetThuc);
    }

    public boolean kiemTraMaKM(String maKM) {
        return daoKhuyenMai.layMaKM(maKM) != null;
    }

    public boolean kiemTraDieuKienKhuyenMai(String maKM, double tongTienHoaDon) {
        KhuyenMai km = daoKhuyenMai.layMaKM(maKM);
        if (km == null) return false;

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(km.getNgayBatDau()) || now.isAfter(km.getNgayKetThuc())) return false;

        List<DieuKienKhuyenMai> dsDieuKien = daoDieuKienKhuyenMai.layTheoKhuyenMaiId(maKM);
        if (dsDieuKien == null || dsDieuKien.isEmpty()) return true;

        for (DieuKienKhuyenMai dk : dsDieuKien) {
            if (dk == null) continue;
            if ("HOA_DON".equalsIgnoreCase(dk.getDoiTuongApDung()) && "GIA_TRI".equalsIgnoreCase(dk.getLoaiDieuKien())) {
                if (tongTienHoaDon < dk.getGiaTri()) {
                    return false;
                }
            }
        }
        return true;
    }

    public double apDungKM(String maKM, double tongTienHoaDon) {
        if (!kiemTraDieuKienKhuyenMai(maKM, tongTienHoaDon)) return tongTienHoaDon;

        HinhThucKhuyenMai htkm = daoHinhThucKhuyenMai.layTheoMaKM(maKM);
        if (htkm == null) return tongTienHoaDon;

        if (htkm.getLoaiHinhThuc() == LoaiHinhThuc.GIAM_THEO_PHAN_TRAM && 
            htkm.getDoiTuongApDung() == DoiTuongApDung.HOA_DON) {
            
            double tienGiam = tongTienHoaDon * htkm.getGiaTri() / 100.0;
            if (htkm.getGiamToiDa() > 0 && tienGiam > htkm.getGiamToiDa()) {
                tienGiam = htkm.getGiamToiDa();
            }
            return tongTienHoaDon - tienGiam;
        } else if (htkm.getLoaiHinhThuc() == LoaiHinhThuc.GIAM_TIEN_MAT && htkm.getDoiTuongApDung() == DoiTuongApDung.HOA_DON) {
            return Math.max(0, tongTienHoaDon - htkm.getGiaTri());
        }
        return tongTienHoaDon;
    }

    public boolean kiemTraKhuyenMaiHetHan(String maKM) {
        KhuyenMai km = daoKhuyenMai.layMaKM(maKM);
        if (km == null || km.getNgayKetThuc() == null) return true;
        return LocalDateTime.now().isAfter(km.getNgayKetThuc());
    }

    public List<Map<String, Object>> layDanhSachGoiYKhuyenMaiVoiLogic() {
        List<Map<String, Object>> danhSachGoiY = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String ngayBatDau = today.format(formatter);
        String ngayKetThuc = today.plusDays(30).format(formatter);
        
        if (month == 2) {
            Object[] spValentine = daoSanPham.laySanPhamGoiYTheoHoatChat("Bao cao su");
            if (spValentine == null) spValentine = daoSanPham.laySanPhamGoiYTheoHoatChat("Tránh thai");
            
            if (spValentine != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("title", "Lễ Tình nhân - Valentine");
                data.put("desc", "Giảm 20% cho các sản phẩm kế hoạch hóa gia đình.");
                data.put("icon", "HEART"); 
                data.put("color", "#E1304C"); 
                
                Map<String, Object> autoData = new HashMap<>();
                autoData.put("isAutoFill", true);
                autoData.put("1", "Khuyến mãi Valentine: " + spValentine[1].toString());
                autoData.put("10", "20"); 
                autoData.put("6", ngayBatDau + " - " + ngayKetThuc);
                data.put("autoData", autoData);
                
                danhSachGoiY.add(data);
            }
        }
        
        if (month >= 5 && month <= 10) {
            Object[] spMuaMua = daoSanPham.laySanPhamGoiYTheoHoatChat("Paracetamol");
            if (spMuaMua == null) spMuaMua = daoSanPham.laySanPhamGoiYTheoHoatChat("Cảm cúm");
            
            if (spMuaMua != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("title", "Đón Mùa Mưa - Đánh bay cảm cúm");
                data.put("desc", "Tặng kèm khẩu trang hoặc giảm giá khi mua thuốc cảm.");
                data.put("icon", "HELP"); 
                data.put("color", "#1A73E8"); 
                
                Map<String, Object> autoData = new HashMap<>();
                autoData.put("isAutoFill", true);
                autoData.put("1", "Combo Mùa Mưa: " + spMuaMua[1].toString());
                autoData.put("10", "15"); 
                autoData.put("6", ngayBatDau + " - " + ngayKetThuc);
                data.put("autoData", autoData);
                
                danhSachGoiY.add(data);
            }
        }
        
        List<Object[]> duLieuGoiYTho = daoSanPham.layDanhSachGoiYKhuyenMai();
        if (duLieuGoiYTho != null && !duLieuGoiYTho.isEmpty()) {
            int soGoiYDaThem = 0; 
            
            for (Object[] dongDuLieu : duLieuGoiYTho) {
                if (soGoiYDaThem >= 3) break;
                
                String tenSanPham = (String) dongDuLieu[1]; 
                LocalDateTime hanSuDung = (LocalDateTime) dongDuLieu[3];
                int soLuongTonKho = (int) dongDuLieu[4];
                
                long soNgayConHan = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), hanSuDung);
                
                if (soNgayConHan <= 0) {
                    continue; 
                }
                
                Map<String, Object> data = new HashMap<>();
                Map<String, Object> autoData = new HashMap<>();
                autoData.put("isAutoFill", true);
                autoData.put("6", ngayBatDau + " - " + ngayKetThuc);
                
                if (soNgayConHan <= 90) { 
                    data.put("title", "Xả kho hàng cận Date");
                    data.put("desc", "Áp dụng Mua 3 tặng 1 cho " + tenSanPham + " (còn " + soNgayConHan + " ngày)");
                    data.put("icon", "CLOCK");
                    data.put("color", "#FFAB00");
                    
                    autoData.put("1", "Xả hàng cận date: " + tenSanPham);
                    autoData.put("2", "Sản phẩm kèm theo");
                    
                    autoData.put("12", tenSanPham); 
                    autoData.put("13", "3");        
                    autoData.put("14", "");         
                    autoData.put("15", tenSanPham); 
                    autoData.put("16", "1");        
                    autoData.put("17", "");         
                } else { 
                    data.put("title", "Giảm tải tồn kho lớn");
                    data.put("desc", "Giảm 30% cho " + tenSanPham + " (Tồn: " + soLuongTonKho + ")");
                    data.put("icon", "BOX");
                    data.put("color", "#9C27B0");
                    
                    autoData.put("1", "Khuyến mãi tồn kho: " + tenSanPham);
                    autoData.put("10", "30"); 
                }
                
                data.put("autoData", autoData);
                danhSachGoiY.add(data);
                soGoiYDaThem++;
            }
        }
        
        return danhSachGoiY;
    }

    public double tinhLoiNhuanDuKien(int loaiHinhThuc, double giaNhapMua, double giaBanMua, 
                                     double giaTriGiam, double giamToiDa, 
                                     int slMua, double giaNhapTang, int slTang) {
        double loiNhuan = 0;
        
        if (loaiHinhThuc == 0) {
            double tienGiam = (giaBanMua * giaTriGiam) / 100.0;
            if (giamToiDa > 0 && tienGiam > giamToiDa) {
                tienGiam = giamToiDa;
            }
            loiNhuan = (giaBanMua - tienGiam) - giaNhapMua;
            
        } else if (loaiHinhThuc == 1) {
            double tongThu = (giaBanMua * slMua) - giaTriGiam;
            double tongVon = (giaNhapMua * slMua);
            loiNhuan = tongThu - tongVon;
            
        } else if (loaiHinhThuc == 2) {
            double tongThu = (giaBanMua * slMua);
            double tongVon = (giaNhapMua * slMua) + (giaNhapTang * slTang);
            loiNhuan = tongThu - tongVon;
        }
        
        return loiNhuan;
    }

    /**
     * BỔ SUNG: Hàm giao tiếp lấy chính xác Giá Nhập và Giá Bán từ Tầng Dữ Liệu
     * Phục vụ cho tính năng Dự toán hiệu quả khuyến mãi tự động nạp.
     */
    public double[] layGiaTheoDonVi(String tenSP, String donVi) {
        try {
            return daoSanPham.layGiaNhapVaGiaBanTheoDonVi(tenSP, donVi);
        } catch (Exception e) {
            return new double[]{0, 0};
        }
    }
}