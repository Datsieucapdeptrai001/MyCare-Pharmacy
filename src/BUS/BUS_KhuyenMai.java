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
import ConnectDB.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public double[] layThongKeHieuSuatKM(String maKM) {
        try {
            return daoKhuyenMai.layThongKeHieuSuatKM(maKM);
        } catch (Exception e) {
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
                
                String danhMuc = "OTC"; 
                if (dongDuLieu.length > 5 && dongDuLieu[5] != null) {
                    danhMuc = dongDuLieu[5].toString().toUpperCase();
                } else {
                    Entity.SanPham sp = daoSanPham.getSanPhamTheoMa(tenSP); 
                    if (sp != null && sp.getDanhMuc() != null) {
                        danhMuc = sp.getDanhMuc().toString().toUpperCase();
                    }
                }
                
                Map<String, Object> item = new HashMap<>();
                Map<String, Object> autoData = new HashMap<>();
                LocalDate now = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                
                if (danhMuc.contains("RX") || danhMuc.contains("KE_DON") || danhMuc.contains("KÊ ĐƠN")) {
                    if (soNgay <= 180) { 
                        item.put("title", "⚠️ Cảnh báo Rx: " + soLoHang);
                        item.put("desc", "Thuốc <b>" + tenSP + "</b> còn " + soNgay + " ngày. <br><font color='#E1304C'><b>CẤM KHUYẾN MÃI.</b></font> Trả NPP hoặc tiêu hủy.");
                        item.put("icon", "WARNING");
                        item.put("color", "#E1304C"); 
                        item.put("autoData", null); 
                        goiyList.add(item);
                    }
                } 
                else if (danhMuc.contains("TPCN") || danhMuc.contains("THUC_PHAM") || danhMuc.contains("THỰC PHẨM")) {
                    if (soNgay >= 90 && soNgay <= 180) { 
                        double tySuatLoiNhuan = ((giaBan - giaNhap) / giaBan) * 100.0;
                        int mucGiam = tySuatLoiNhuan > 35 ? 30 : 20; 

                        item.put("title", "Xả kho TPCN cận date: " + soLoHang);
                        item.put("desc", "Còn <b>" + soNgay + " ngày</b>. Đủ thời gian 1 liệu trình. Khuyên dùng: Giảm giá sâu <b>" + mucGiam + "%</b> cho " + tenSP + ".");
                        item.put("icon", "LIGHTBULB");
                        item.put("color", "#FFAB00"); 

                        autoData.put("isAutoFill", true);
                        autoData.put("1", "Xả kho TPCN " + tenSP); 
                        autoData.put("2", "Giảm theo phần trăm (%)"); 
                        autoData.put("5", "Sản phẩm"); 
                        autoData.put("10", String.valueOf(mucGiam)); 
                        autoData.put("12", tenSP); 
                        autoData.put("6", now.format(formatter) + " - " + now.plusDays(30).format(formatter));
                        
                        item.put("autoData", autoData);
                        goiyList.add(item);
                    }
                } 
                else {
                    if (soNgay >= 180 && soNgay <= 270) { 
                        item.put("title", "Đẩy hàng OTC: " + soLoHang);
                        item.put("desc", "Còn <b>" + soNgay + " ngày</b>. Khuyên dùng: Tạo <b>Combo mua 2 giảm 10k</b> trực tiếp cho " + tenSP + ".");
                        item.put("icon", "PACKAGE");
                        item.put("color", "#00A76F"); 

                        autoData.put("isAutoFill", true);
                        autoData.put("1", "Ưu đãi tại quầy " + tenSP); 
                        autoData.put("2", "Giảm tiền mặt theo số lượng"); 
                        autoData.put("10", "10000"); 
                        autoData.put("12", tenSP); 
                        autoData.put("13", "2"); 
                        autoData.put("6", now.format(formatter) + " - " + now.plusDays(30).format(formatter));
                        
                        item.put("autoData", autoData);
                        goiyList.add(item);
                    }
                }
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
            
            data2.put("title", "Marketing Tăng Doanh Thu");
            data2.put("desc", "Hàng hóa trong kho đang ở trạng thái an toàn. Gợi ý: Tạo ưu đãi giảm 10% cho toàn bộ hóa đơn từ 500,000đ.");
            data2.put("icon", "USERS");
            data2.put("color", "#1A73E8");
            
            autoData2.put("1", "Chương trình Tri ân Khách hàng");
            autoData2.put("2", "Giảm theo phần trăm (%)");
            autoData2.put("5", "Hóa đơn");
            autoData2.put("10", "10"); 
            autoData2.put("11", "500000"); 
            
            data2.put("autoData", autoData2);
            goiyList.add(data2);
        }
        
        return goiyList;
    }

    // ==============================================================================
    // HÀM MỚI: QUÉT KHO, THÔNG BÁO HSD VÀ CHẶN LỖI (BLOCK/WARNING)
    // ==============================================================================
    public Map<String, Object> kiemTraHopLeKhiThemThuCong(String tenHoacMaSP) {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "OK"); // OK, WARNING, BLOCK
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

        String input = tenHoacMaSP.trim();
        // Lấy danh mục, tổng tồn và thông tin lô hàng cận date nhất
        String sql = "SELECT TOP 1 sp.id, sp.danhMuc, sp.ten, lh.soLoHang, lh.ngayHetHan, " +
                     "DATEDIFF(day, GETDATE(), lh.ngayHetHan) as soNgay, " +
                     "(SELECT ISNULL(SUM(soLuongLoHang), 0) FROM LoHang WHERE sanPhamId = sp.id AND trangThai <> 'AN') as tongTon " +
                     "FROM SanPham sp " +
                     "LEFT JOIN LoHang lh ON sp.id = lh.sanPhamId AND lh.trangThai <> 'AN' AND lh.soLuongLoHang > 0 " +
                     "WHERE sp.id = ? OR sp.ten = ? " +
                     "ORDER BY lh.ngayHetHan ASC";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, input);
            pst.setString(2, input);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String danhMuc = rs.getString("danhMuc");
                    String tenThuc = rs.getString("ten");
                    String soLo = rs.getString("soLoHang");
                    java.sql.Timestamp ngayHetHan = rs.getTimestamp("ngayHetHan");
                    int soNgay = rs.getInt("soNgay");
                    int tonKho = rs.getInt("tongTon");

                    result.put("danhMuc", danhMuc != null ? danhMuc : "KHÁC");
                    result.put("tenThuc", tenThuc);
                    result.put("tonKho", tonKho);

                    if (soLo != null) {
                        result.put("soLo", soLo);
                        result.put("soNgay", soNgay);
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                        result.put("hsd", sdf.format(ngayHetHan));
                    }

                    // KIỂM TRA HẾT HÀNG
                    if (tonKho <= 0) {
                        result.put("status", "BLOCK");
                        result.put("message", "Sản phẩm đã hết hàng trong kho. Không thể tạo khuyến mãi!");
                        return result;
                    }

                    // KIỂM TRA HẾT HẠN
                    if (soLo != null && soNgay < 0) {
                        result.put("status", "BLOCK");
                        result.put("message", "Lô hàng cận nhất đã HẾT HẠN SỬ DỤNG. Cấm khuyến mãi, yêu cầu hủy!");
                        return result;
                    }

                    // LUẬT 1: THUỐC KÊ ĐƠN (Rx) BỊ CẤM HOÀN TOÀN
                    if ("THUOC_KE_DON".equalsIgnoreCase(danhMuc)) {
                        result.put("status", "BLOCK");
                        String msg = "CẤM KHUYẾN MÃI THUỐC KÊ ĐƠN (Rx) THEO LUẬT DƯỢC 2016!";
                        if (soLo != null && soNgay <= 180) {
                            msg += " Thuốc cận date (Còn " + soNgay + " ngày), yêu cầu làm thủ tục trả NPP hoặc tiêu hủy!";
                        }
                        result.put("message", msg);
                        return result;
                    }

                    // LUẬT 2: TPCN VÀ OTC KIỂM TRA THỜI GIAN
                    if (soLo != null) {
                        if ("THUC_PHAM_CHUC_NANG".equalsIgnoreCase(danhMuc) && soNgay < 90) {
                            result.put("status", "BLOCK");
                            result.put("message", "TPCN cận date dưới 3 tháng. Không kịp 1 liệu trình an toàn, CẤM bán khuyến mãi!");
                        } else if ("THUOC_KHONG_KE_DON".equalsIgnoreCase(danhMuc) && soNgay < 180) {
                            result.put("status", "BLOCK");
                            result.put("message", "Thuốc OTC cận date dưới 6 tháng. Nguy hiểm khi tích trữ tại nhà, CẤM khuyến mãi!");
                        } else if ("THUC_PHAM_CHUC_NANG".equalsIgnoreCase(danhMuc) && soNgay <= 180) {
                            result.put("status", "WARNING");
                            result.put("message", "Thời điểm vàng xả kho TPCN (3-6 tháng). Hãy ưu tiên giảm sâu.");
                        } else if ("THUOC_KHONG_KE_DON".equalsIgnoreCase(danhMuc) && soNgay <= 270) {
                            result.put("status", "WARNING");
                            result.put("message", "Thời điểm vàng đẩy hàng OTC (6-9 tháng). Khuyên dùng Combo.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // silent fail
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
        
        List<Object[]> dsKM = layDanhSachKhuyenMaiChoTable(); 
        
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