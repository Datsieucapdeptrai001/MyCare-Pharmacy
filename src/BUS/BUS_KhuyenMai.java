package BUS;

import DAO.DAO_DieuKienKhuyenMai;
import DAO.DAO_HinhThucKhuyenMai;
import DAO.DAO_KhuyenMai;
import DAO.DAO_SanPham; 
import DAO.DAO_LoHang; 
import Entity.KhuyenMai;
import Entity.DieuKienKhuyenMai;
import Entity.HinhThucKhuyenMai;
import Enumeration.*;

import java.util.ArrayList; 
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

public class BUS_KhuyenMai {
    private DAO_KhuyenMai daoKhuyenMai;
    private DAO_DieuKienKhuyenMai daoDieuKienKhuyenMai;
    private DAO_HinhThucKhuyenMai daoHinhThucKhuyenMai;
    private DAO_SanPham daoSanPham; 
    private DAO_LoHang daoLoHang; 

    public BUS_KhuyenMai() {
        this.daoKhuyenMai = new DAO_KhuyenMai();
        this.daoDieuKienKhuyenMai = new DAO_DieuKienKhuyenMai();
        this.daoHinhThucKhuyenMai = new DAO_HinhThucKhuyenMai();
        this.daoSanPham = new DAO_SanPham(); 
        this.daoLoHang = new DAO_LoHang(); 
    }

    public List<Object[]> layDanhSachKhuyenMaiHopLe() {
        return daoKhuyenMai.layDanhSachKhuyenMaiHopLe();
    }

    public List<Object[]> layDanhSachKhuyenMaiHienThiTag() {
        return daoKhuyenMai.layDanhSachKhuyenMaiHienThiTag();
    }

    public List<Object[]> layDanhSachKhuyenMaiFull() {
        return daoKhuyenMai.layDanhSachKhuyenMaiFull(); 
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
    
    public boolean anKhuyenMai(String maKM) {
        return daoKhuyenMai.anKhuyenMai(maKM);
    }

    public int[] layCauHinhTichDiem() {
        return daoKhuyenMai.layCauHinhTichDiem();
    }
    
    public boolean luuCauHinhTichDiem(int tienMua, int diemThuong, int tienDoi, int diemToiThieu) {
        return daoKhuyenMai.luuCauHinhTichDiem(tienMua, diemThuong, tienDoi, diemToiThieu);
    }

    public List<Object[]> layDanhSachKhuyenMaiChoTable(boolean hienDaAn) {
        return daoKhuyenMai.layDanhSachKhuyenMaiChoTable(hienDaAn);
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

    public String kiemTraChiTietKhuyenMaiVoiGioHang(String maKM, double tongTienHienTai, Map<String, Integer> gioHang) {
        List<DieuKienKhuyenMai> dsDieuKien = daoDieuKienKhuyenMai.layTheoKhuyenMaiId(maKM);
        if (dsDieuKien != null && !dsDieuKien.isEmpty()) {
            for (DieuKienKhuyenMai dk : dsDieuKien) {
                if ("HOA_DON".equalsIgnoreCase(dk.getDoiTuongApDung()) && "GIA_TRI".equalsIgnoreCase(dk.getLoaiDieuKien())) {
                    if (tongTienHienTai < dk.getGiaTri()) {
                        double tienThieu = dk.getGiaTri() - tongTienHienTai;
                        return "Chưa đủ điều kiện! Cần mua thêm " + String.format("%,.0f VNĐ", tienThieu) + " để áp dụng.";
                    }
                }
            }
        }

        HinhThucKhuyenMai htkm = daoHinhThucKhuyenMai.layTheoMaKM(maKM);
        if (htkm != null && htkm.getDoiTuongApDung() == DoiTuongApDung.SAN_PHAM) {
            String spYeuCau = htkm.getSpYeuCau();
            int slYeuCau = htkm.getSlYeuCau();

            if (spYeuCau != null && !spYeuCau.trim().isEmpty()) {
                String tenSpCheck = spYeuCau.trim();
                
                Entity.SanPham sp = daoSanPham.getSanPhamTheoMa(tenSpCheck);
                if (sp != null && sp.getTen() != null) {
                    tenSpCheck = sp.getTen().trim(); 
                }

                int slTrongGio = 0;
                for (Map.Entry<String, Integer> entry : gioHang.entrySet()) {
                    if (entry.getKey().trim().equalsIgnoreCase(tenSpCheck)) {
                        slTrongGio += entry.getValue();
                    }
                }

                if (slTrongGio < slYeuCau) {
                    int slThieu = slYeuCau - slTrongGio;
                    String donVi = (htkm.getDvdlYeuCau() == null || htkm.getDvdlYeuCau().trim().isEmpty()) ? "SP" : htkm.getDvdlYeuCau().trim();
                    return "Chưa đủ điều kiện! Cần mua thêm " + slThieu + " " + donVi + " [" + tenSpCheck + "].";
                }
            }
        }
        return "OK"; 
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

    public Map<String, Object> kiemTraHopLeKhiThemThuCong(String tenHoacMaSP) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "OK"); 
        result.put("message", "");
        result.put("soLo", "");
        result.put("hsd", "");
        result.put("soNgay", 9999);
        result.put("tonKho", 0);
        result.put("danhMuc", "");
        result.put("tenThuc", tenHoacMaSP);

        if (tenHoacMaSP == null || tenHoacMaSP.trim().isEmpty() || tenHoacMaSP.startsWith("VD:")) {
            return result;
        }

        Map<String, Object> dbData = daoKhuyenMai.layThongTinSanPhamVaLoHangGoiY(tenHoacMaSP.trim());

        if (!dbData.isEmpty()) {
            String danhMuc = (String) dbData.get("danhMuc");
            String tenThuc = (String) dbData.get("tenThuc");
            String soLo = (String) dbData.get("soLo");
            java.util.Date ngayHetHan = (java.util.Date) dbData.get("ngayHetHan");
            int soNgay = (int) dbData.get("soNgay");
            int tonKho = (int) dbData.get("tonKho");

            result.put("danhMuc", danhMuc != null ? danhMuc : "KHÁC");
            result.put("tenThuc", tenThuc);
            result.put("tonKho", tonKho);

            if (soLo != null && ngayHetHan != null) {
                result.put("soLo", soLo);
                result.put("soNgay", soNgay);
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                result.put("hsd", sdf.format(ngayHetHan));
            }

            if (tonKho <= 0) {
                result.put("status", "WARNING");
                result.put("message", "Sản phẩm đã hết hàng trong kho.");
                return result;
            }

            if (soLo != null && soNgay < 0) {
                result.put("status", "WARNING");
                result.put("message", "Lô hàng cận nhất đã HẾT HẠN SỬ DỤNG. Cần hủy.");
                return result;
            }

            if ("THUOC_KE_DON".equalsIgnoreCase(danhMuc)) {
                result.put("status", "WARNING");
                String msg = "VI PHẠM: Thuốc kê đơn (Rx) không được phép khuyến mãi theo Luật Dược 2016!";
                if (soLo != null && soNgay <= 180) {
                    msg += " Thuốc cận date (Còn " + soNgay + " ngày), yêu cầu làm thủ tục trả NPP hoặc tiêu hủy!";
                }
                result.put("message", msg);
                return result;
            }

            if (soLo != null) {
                if ("THUC_PHAM_CHUC_NANG".equalsIgnoreCase(danhMuc) && soNgay < 90) {
                    result.put("status", "WARNING");
                    result.put("message", "TPCN cận date dưới 3 tháng. Không kịp 1 liệu trình an toàn.");
                } else if ("THUOC_KHONG_KE_DON".equalsIgnoreCase(danhMuc) && soNgay < 180) {
                    result.put("status", "WARNING");
                    result.put("message", "Thuốc OTC cận date dưới 6 tháng. Nguy hiểm khi tích trữ tại nhà.");
                } else if ("THUC_PHAM_CHUC_NANG".equalsIgnoreCase(danhMuc) && soNgay <= 180) {
                    result.put("status", "WARNING");
                    result.put("message", "Thời điểm vàng xả kho TPCN (3-6 tháng). Hãy ưu tiên giảm sâu.");
                } else if ("THUOC_KHONG_KE_DON".equalsIgnoreCase(danhMuc) && soNgay <= 270) {
                    result.put("status", "WARNING");
                    result.put("message", "Thời điểm vàng đẩy hàng OTC (6-9 tháng). Khuyên dùng Combo.");
                }
            }
        }
        return result;
    }

    public boolean kiemTraTrungKhuyenMai(String tenSP, int loaiHinhThucStr, double mucGiam, String currentKMId) {
        return daoKhuyenMai.kiemTraTrungKhuyenMai(tenSP, loaiHinhThucStr, mucGiam, currentKMId);
    }
    
    public static class KetQuaApDungKhuyenMai {
        private long tongTienGiam;
        private List<Object[]> danhSachQuaTang;
        private List<String> danhSachMaDaDuyet;

        public KetQuaApDungKhuyenMai(long tongTienGiam, List<Object[]> danhSachQuaTang, List<String> danhSachMaDaDuyet) {
            this.tongTienGiam = tongTienGiam;
            this.danhSachQuaTang = danhSachQuaTang;
            this.danhSachMaDaDuyet = danhSachMaDaDuyet;
        }

        public long getTongTienGiam() { return tongTienGiam; }
        public List<Object[]> getDanhSachQuaTang() { return danhSachQuaTang; }
        public List<String> getDanhSachMaDaDuyet() { return danhSachMaDaDuyet; }
    }

    public KetQuaApDungKhuyenMai tinhToanKhuyenMaiTuDong(long tongTienBill, int tongSoLuongSPThucTe, Map<String, Integer> gioHangHienTai) {
        long tongTienGiamDoc = 0;
        List<Object[]> danhSachQuaTang = new ArrayList<>();
        List<String> danhSachMaDaDuyet = new ArrayList<>();
        
        List<Object[]> dsKM = layDanhSachKhuyenMaiChoTable(false); 
        
        if (dsKM != null) {
            for (Object[] kmRow : dsKM) {
                String maKM = (String) kmRow[0];
                boolean dangHoatDong = (Boolean) kmRow[9]; 
                
                if (!dangHoatDong) continue;

                String loaiKM = (String) kmRow[2]; 
                double donToiThieu = (Double) kmRow[11];
                
                if (donToiThieu > 0 && tongTienBill < donToiThieu) {
                    continue; 
                }

                if (loaiKM.contains("phần trăm")) {
                    String apDungCho = (String) kmRow[5]; 
                    double mucGiamTreo = (Double) kmRow[10];
                    double giamToiDa = (Double) kmRow[18];
                    
                    if (apDungCho.equalsIgnoreCase("Hóa đơn")) {
                        double tienGiamPhanTram = tongTienBill * (mucGiamTreo / 100.0);
                        if (giamToiDa > 0 && tienGiamPhanTram > giamToiDa) {
                            tienGiamPhanTram = giamToiDa;
                        }
                        tongTienGiamDoc += tienGiamPhanTram;
                        danhSachMaDaDuyet.add(maKM);
                    } 
                    else {
                        String tenSPYeuCau = (String) kmRow[12]; 
                        Entity.SanPham sp = daoSanPham.getSanPhamTheoMa(tenSPYeuCau);
                        String tenSpCheck = sp != null ? sp.getTen() : tenSPYeuCau;

                        if (gioHangHienTai.containsKey(tenSpCheck)) {
                            danhSachMaDaDuyet.add(maKM);
                        }
                    }
                } 
                else if (loaiKM.contains("tiền mặt")) {
                    String tenSPYeuCau = (String) kmRow[12]; 
                    Entity.SanPham sp = daoSanPham.getSanPhamTheoMa(tenSPYeuCau);
                    String tenSpCheck = sp != null ? sp.getTen() : tenSPYeuCau;
                    int slYeuCau = (Integer) kmRow[13];
                    double tienGiam = (Double) kmRow[10];
                    
                    int slTrongGio = gioHangHienTai.getOrDefault(tenSpCheck, 0);
                    if (slTrongGio >= slYeuCau) {
                        int soLanApDung = slTrongGio / slYeuCau; 
                        tongTienGiamDoc += (tienGiam * soLanApDung);
                        danhSachMaDaDuyet.add(maKM);
                    }
                } 
                else if (loaiKM.contains("kèm theo")) {
                    String tenSPYeuCau = (String) kmRow[12]; 
                    Entity.SanPham spMua = daoSanPham.getSanPhamTheoMa(tenSPYeuCau);
                    String tenSpCheck = spMua != null ? spMua.getTen() : tenSPYeuCau;
                    
                    int slYeuCau = (Integer) kmRow[13];
                    
                    int slTrongGio = gioHangHienTai.getOrDefault(tenSpCheck, 0);
                    if (slTrongGio >= slYeuCau) {
                        int soLanApDung = slTrongGio / slYeuCau; 
                        
                        String tenSPTang = (String) kmRow[15]; 
                        Entity.SanPham spTang = daoSanPham.getSanPhamTheoMa(tenSPTang);
                        String tenThuong = spTang != null ? spTang.getTen() : tenSPTang;
                        
                        int slTang = (Integer) kmRow[16];
                        String dvTang = (String) kmRow[17];
                        
                        int tongSlTang = slTang * soLanApDung;
                        danhSachQuaTang.add(new Object[]{
                            "[QUÀ TẶNG] " + tenThuong, dvTang, tongSlTang, "0đ", "0%", "0đ", "Khác"
                        });
                        
                        danhSachMaDaDuyet.add(maKM);
                    }
                }
            }
        }
        return new KetQuaApDungKhuyenMai(tongTienGiamDoc, danhSachQuaTang, danhSachMaDaDuyet);
    }
    
    public static class PromoValidationResult {
        public boolean isValid;
        public String message;
        public long discountAmount;

        public PromoValidationResult(boolean isValid, String message, long discountAmount) {
            this.isValid = isValid;
            this.message = message;
            this.discountAmount = discountAmount;
        }
    }

    public PromoValidationResult kiemTraHopLePromotion(String maKM, double tongTienHoaDon, List<Object[]> dsSP) {
        Map<String, Integer> gioHang = new HashMap<>();
        for (Object[] item : dsSP) {
            String tenSP = item[0].toString();
            if (tenSP.startsWith("[QUÀ TẶNG]")) continue; 
            
            int sl = Integer.parseInt(item[2].toString());
            gioHang.put(tenSP, gioHang.getOrDefault(tenSP, 0) + sl);
        }

        String checkMsg = kiemTraChiTietKhuyenMaiVoiGioHang(maKM, tongTienHoaDon, gioHang);
        if (!"OK".equals(checkMsg)) {
            return new PromoValidationResult(false, checkMsg, 0);
        }

        double giaSauGiam = apDungKM(maKM, tongTienHoaDon);
        long tienGiam = (long) (tongTienHoaDon - giaSauGiam);

        return new PromoValidationResult(true, "Áp dụng khuyến mãi thành công!", tienGiam);
    }
}