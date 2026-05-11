package BUS;

import DAO.DAO_DieuKienKhuyenMai;
import DAO.DAO_HinhThucKhuyenMai;
import DAO.DAO_KhuyenMai;
import DAO.DAO_SanPham; 
import DAO.DAO_LoHang; // IMPORT DAO_LoHang CHO CHUẨN KIẾN TRÚC
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
    private DAO_LoHang daoLoHang; // KHAI BÁO DAO_LOHANG

    public BUS_KhuyenMai() {
        this.daoKhuyenMai = new DAO_KhuyenMai();
        this.daoDieuKienKhuyenMai = new DAO_DieuKienKhuyenMai();
        this.daoHinhThucKhuyenMai = new DAO_HinhThucKhuyenMai();
        this.daoSanPham = new DAO_SanPham(); 
        this.daoLoHang = new DAO_LoHang(); // KHỞI TẠO
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

    public double[] layGiaTheoDonVi(String tenSP, String donVi) {
        try {
            return daoSanPham.layGiaNhapVaGiaBanTheoDonVi(tenSP, donVi);
        } catch (Exception e) {
            return new double[]{0, 0};
        }
    }

    public double[] layThongKeHieuSuatKM(String maKM) {
        try {
            return daoKhuyenMai.layThongKeHieuSuatKM(maKM);
        } catch (Exception e) {
            e.printStackTrace();
            return new double[]{0, 0, 0};
        }
    }

    // ===========================================
    // GỌI DAO_LOHANG ĐỂ LẤY DỮ LIỆU ĐÚNG CHUẨN 3 LỚP
    // ===========================================
    public List<Map<String, Object>> layDanhSachGoiYKhuyenMaiVoiLogic() {
        List<Map<String, Object>> goiyList = new ArrayList<>();
        
        // Gọi DAO_LoHang để lấy dữ liệu, tuân thủ đúng nguyên tắc Single Responsibility
        List<Object[]> dsLoCanDate = daoLoHang.layDuLieuLoHangCanDateTho();
        
        // Chạy thuật toán tính toán Business Logic
        if (dsLoCanDate != null && !dsLoCanDate.isEmpty()) {
            for (Object[] dongDuLieu : dsLoCanDate) {
                String soLoHang = (String) dongDuLieu[0];
                String tenSP = (String) dongDuLieu[1];
                double giaBan = (Double) dongDuLieu[2];
                double giaNhap = (Double) dongDuLieu[3];
                int soNgay = (Integer) dongDuLieu[4];
                
                // Thuật toán: Tính % tối đa có thể giảm trước khi chạm mốc lỗ
                double phanTramMax = ((giaBan - giaNhap) / giaBan) * 100.0;
                
                if (phanTramMax <= 5) continue; // Nếu biên lợi nhuận quá mỏng (< 5%), không gợi ý
                
                // Gợi ý mức giảm an toàn: Ưu tiên xả hàng cận date nên giảm mạnh nhưng chừa lại 5-10% lời
                int mucGiamGoiY = 5;
                if (phanTramMax >= 40) mucGiamGoiY = 30;
                else if (phanTramMax >= 30) mucGiamGoiY = 20;
                else if (phanTramMax >= 20) mucGiamGoiY = 15;
                else mucGiamGoiY = 10;
                
                double loiNhuanDuKien = giaBan - (giaBan * mucGiamGoiY / 100.0) - giaNhap;
                
                // Khởi tạo Map dữ liệu trả về cho GUI
                Map<String, Object> item = new HashMap<>();
                item.put("title", "Xả hàng lô: " + soLoHang);
                item.put("desc", "Còn <b>" + soNgay + " ngày</b> hết hạn. Khuyên dùng: Giảm <b>" + mucGiamGoiY + "%</b> cho <b>" + tenSP + "</b> (Lãi: " + String.format("%,.0fđ", loiNhuanDuKien) + "/SP).");
                item.put("icon", "ALERT");
                item.put("color", "#E1304C"); 
                
                // Dữ liệu dùng để AutoFill vào Form (Hoàn toàn dùng giá trị Enum/Text)
                Map<String, Object> autoData = new HashMap<>();
                autoData.put("isAutoFill", true);
                autoData.put("1", "Xả hàng lô " + soLoHang + " (" + tenSP + ")"); 
                autoData.put("2", "Giảm theo phần trăm (%)"); 
                autoData.put("5", "Sản phẩm"); 
                autoData.put("10", String.valueOf(mucGiamGoiY)); 
                autoData.put("12", tenSP); 
                
                LocalDate now = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                autoData.put("6", now.format(formatter) + " - " + now.plusDays(soNgay).format(formatter));
                
                item.put("autoData", autoData);
                goiyList.add(item);
            }
        }

        // Fallback mặc định nếu không có lô hàng nào sắp hết hạn
        if (goiyList.isEmpty()) {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String ngayBatDau = today.format(formatter);
            String ngayKetThuc = today.plusDays(30).format(formatter);

            Map<String, Object> data2 = new HashMap<>();
            Map<String, Object> autoData2 = new HashMap<>();
            autoData2.put("isAutoFill", true);
            autoData2.put("6", ngayBatDau + " - " + ngayKetThuc);
            
            data2.put("title", "Ngày hội Khách hàng thân thiết");
            data2.put("desc", "Giảm 10% cho toàn bộ hóa đơn từ 500,000đ trở lên.");
            data2.put("icon", "USERS");
            data2.put("color", "#00A76F");
            
            autoData2.put("1", "Ngày hội Thành viên");
            autoData2.put("2", "Giảm theo phần trăm (%)");
            autoData2.put("5", "Hóa đơn");
            autoData2.put("10", "10"); 
            autoData2.put("11", "500000"); 
            
            data2.put("autoData", autoData2);
            goiyList.add(data2);
        }
        
        return goiyList;
    }
}