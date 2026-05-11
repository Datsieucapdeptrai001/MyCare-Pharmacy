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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    // ==============================================================
    // HÀM MỚI: KIỂM TRA CHI TIẾT GIỎ HÀNG CHUẨN 3 LỚP (TRẢ VỀ LỜI NHẮC NHỞ)
    // ==============================================================
    public String kiemTraChiTietKhuyenMaiVoiGioHang(String maKM, double tongTienHienTai, Map<String, Integer> gioHang) {
        // 1. Kiểm tra Tổng tiền hóa đơn (Nếu khuyến mãi có yêu cầu Đơn tối thiểu)
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

        // 2. Kiểm tra Số lượng Sản phẩm yêu cầu trong Giỏ hàng
        HinhThucKhuyenMai htkm = daoHinhThucKhuyenMai.layTheoMaKM(maKM);
        if (htkm != null && htkm.getDoiTuongApDung() == DoiTuongApDung.SAN_PHAM) {
            String spYeuCau = htkm.getSpYeuCau(); // spYeuCau lúc này đang chứa Mã SP (Ví dụ: SP001)
            int slYeuCau = htkm.getSlYeuCau();

            // Nếu chương trình có chỉ định 1 sản phẩm bắt buộc phải mua
            if (spYeuCau != null && !spYeuCau.trim().isEmpty()) {
                String tenSpCheck = spYeuCau.trim();
                
                // [CHUẨN 3 LỚP]: Dùng DAO_SanPham để lấy thông tin Tên Sản Phẩm thực tế
                Entity.SanPham sp = daoSanPham.getSanPhamTheoMa(tenSpCheck);
                if (sp != null && sp.getTen() != null) {
                    tenSpCheck = sp.getTen().trim(); // Đã dịch từ Mã SP (SP001) sang Tên SP (Panadol)
                }

                int slTrongGio = 0;
                
                // Quét giỏ hàng xem có sản phẩm này không (Quét không phân biệt hoa thường để an toàn)
                for (Map.Entry<String, Integer> entry : gioHang.entrySet()) {
                    if (entry.getKey().trim().equalsIgnoreCase(tenSpCheck)) {
                        slTrongGio += entry.getValue();
                    }
                }

                // Nếu số lượng trong giỏ chưa đạt đủ số lượng yêu cầu của Khuyến mãi
                if (slTrongGio < slYeuCau) {
                    int slThieu = slYeuCau - slTrongGio;
                    String donVi = (htkm.getDvdlYeuCau() == null || htkm.getDvdlYeuCau().trim().isEmpty()) ? "SP" : htkm.getDvdlYeuCau().trim();
                    return "Chưa đủ điều kiện! Cần mua thêm " + slThieu + " " + donVi + " [" + tenSpCheck + "].";
                }
            }
        }

        return "OK"; // Thỏa mãn tất cả mọi điều kiện
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

    public List<Map<String, Object>> layDanhSachGoiYKhuyenMaiVoiLogic() {
        List<Map<String, Object>> goiyList = new ArrayList<>();
        
        List<Object[]> dsLoCanDate = daoLoHang.layDuLieuLoHangCanDateTho();
        
        if (dsLoCanDate != null && !dsLoCanDate.isEmpty()) {
            for (Object[] dongDuLieu : dsLoCanDate) {
                String soLoHang = (String) dongDuLieu[0];
                String tenSP = (String) dongDuLieu[1];
                double giaBan = (Double) dongDuLieu[2];
                double giaNhap = (Double) dongDuLieu[3];
                int soNgay = (Integer) dongDuLieu[4];
                
                double phanTramMax = ((giaBan - giaNhap) / giaBan) * 100.0;
                
                if (phanTramMax <= 5) continue; 
                
                int mucGiamGoiY = 5;
                if (phanTramMax >= 40) mucGiamGoiY = 30;
                else if (phanTramMax >= 30) mucGiamGoiY = 20;
                else if (phanTramMax >= 20) mucGiamGoiY = 15;
                else mucGiamGoiY = 10;
                
                double loiNhuanDuKien = giaBan - (giaBan * mucGiamGoiY / 100.0) - giaNhap;
                
                Map<String, Object> item = new HashMap<>();
                item.put("title", "Xả hàng lô: " + soLoHang);
                item.put("desc", "Còn <b>" + soNgay + " ngày</b> hết hạn. Khuyên dùng: Giảm <b>" + mucGiamGoiY + "%</b> cho <b>" + tenSP + "</b> (Lãi: " + String.format("%,.0fđ", loiNhuanDuKien) + "/SP).");
                item.put("icon", "ALERT");
                item.put("color", "#E1304C"); 
                
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
    
    // ==============================================================
    // LỚP HỖ TRỢ TRẢ VỀ KẾT QUẢ KHUYẾN MÃI
    // ==============================================================
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

    // ==============================================================
    // HÀM TỰ ĐỘNG TÍNH TOÁN QUÀ TẶNG & TIỀN GIẢM CHO TẦNG GUI
    // ==============================================================
    public KetQuaApDungKhuyenMai tinhToanKhuyenMaiTuDong(long tongTienBill, int tongSoLuongSPThucTe, Map<String, Integer> gioHangHienTai) {
        long tongTienGiamDoc = 0;
        List<Object[]> danhSachQuaTang = new ArrayList<>();
        List<String> danhSachMaDaDuyet = new ArrayList<>();
        
        // Gọi DAO lấy tất cả Khuyến mãi đang có hiệu lực
        List<Object[]> dsKM = layDanhSachKhuyenMaiChoTable(); 
        
        if (dsKM != null) {
            for (Object[] kmRow : dsKM) {
                String maKM = (String) kmRow[0];
                boolean dangHoatDong = (Boolean) kmRow[9]; 
                
                if (!dangHoatDong) continue;

                String loaiKM = (String) kmRow[2]; // Hình thức khuyến mãi
                double donToiThieu = (Double) kmRow[11];
                
                // 1. Nếu khuyến mãi yêu cầu đơn tối thiểu (Ví dụ hóa đơn > 500k)
                if (donToiThieu > 0 && tongTienBill < donToiThieu) {
                    continue; 
                }

                // 2. Phân loại và xử lý từng loại Khuyến mãi
                if (loaiKM.contains("phần trăm")) {
                    String apDungCho = (String) kmRow[5]; // "Hóa đơn" hoặc Tên sản phẩm
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
                        // Áp dụng cho 1 sản phẩm cụ thể
                        String tenSPYeuCau = (String) kmRow[12]; // db lưu ID sản phẩm
                        Entity.SanPham sp = daoSanPham.getSanPhamTheoMa(tenSPYeuCau);
                        String tenSpCheck = sp != null ? sp.getTen() : tenSPYeuCau;

                        if (gioHangHienTai.containsKey(tenSpCheck)) {
                            // Ghi chú: Chỗ này thường phải biết Đơn giá của SP đó để tính tiền giảm. 
                            // Tạm thời nếu có trong giỏ thì add mã vào để GUI biết là đã kích hoạt.
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
                        int soLanApDung = slTrongGio / slYeuCau; // Khách mua gấp đôi yêu cầu thì giảm gấp đôi
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
}