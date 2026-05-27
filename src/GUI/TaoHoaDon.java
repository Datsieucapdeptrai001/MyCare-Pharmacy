package GUI;import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.Icon;
import BUS.BUS_KhuyenMai;import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.*;
import java.util.List;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import Enumeration.*;
import Utils.*;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import Entity.HoaDon;
import Entity.ChiTietHoaDon;
import Entity.SanPham;
import Entity.DonViDoLuong;
import Entity.NhanVien;
import Entity.KhachHang;
import BUS.BUS_SanPham;
import BUS.BUS_ChiTietHoaDon;
import BUS.BUS_HoaDon;
import BUS.BUS_KhachHang;
public class TaoHoaDon extends JDialog {
	private DefaultTableModel quickAddProductModel;
    private JTable quickAddProductTable;
    private JPanel pnlQuickAddContent;
    private JPanel pnlInputFields, pnlLinkedCustomer;
    private JLabel lblLinkedAvatar, lblLinkedName, lblLinkedSub, lblLinkedPoints, lblLinkedMoney;
    private JLabel lblLinkStatus, lblBadgeLe;
    private boolean isCustomerLinked = false;
    private String linkedTenKH = "", linkedSdtKH = "";
    private JTextField txtSearch;
    private JPopupMenu customerSuggestionPopup;
    private JButton btnApplyVoucher;
    private long tongHoaDon = 0;
    private long tamTinh = 0;
    private long vat = 0;
    private DefaultTableModel productModel;
    private JLabel lblSubtotalValue, lblVatValue, lblTotalPriceValue;
    private JLabel lblTotalItems;    
    private JLabel lblSubTotal;      
    private JLabel lblVAT;           
    private JLabel lblTotal;         
    private JLabel lblCustomerPay;   
    private int editingModelRow = -1; 
    private String maHDDangSua = "";
    private java.util.List<Object[]> dsKhuyenMaiCache = new java.util.ArrayList<>();
    private JTextField txtVoucherInput;
    private JLabel lblTienThuaValue;
    private DefaultTableModel mainTableModel;
    private JTextField txtName, txtPhone;
    private String phuongThuc = "Tiền mặt";
    private boolean isTableUpdating = false;
    private Color darkBlue = Color.decode("#152A4B");
    private Color accentBlue = Color.decode("#1A73E8");
    private Color successGreen = Color.decode("#10B981");
    private Color orangeLogo = Color.decode("#F59E0B");
    private Color lightGray = Color.decode("#F8F9FA");
    private Color borderColor = Color.decode("#DFE3E8");
    private long tongTienMat = 0;
    private BUS.BUS_LieuMau busLieuMau = new BUS.BUS_LieuMau();
    private final BUS.BUS_Kho busKho = new BUS.BUS_Kho();
    private JLabel lblTotalValue;
    private int editingRow = -1;
    private SwingWorker<java.util.List<Object[]>, Void> currentSearchWorker; // FIX: Quản lý luồng tìm kiếm
    private BUS.BUS_KhuyenMai busKhuyenMai = new BUS.BUS_KhuyenMai();
    // --- BIẾN QUẢN LÝ KHUYẾN MÃI ---
    private long tienGiamGia = 0;
    private String maKhuyenMaiApDung = "";
    private JLabel lblDiscountValue;
    private JPanel pnlDonThuoc;
    private JTextField txtBacSi, txtCoSo, txtChuanDoan;
    private JButton btnToggleKeDon; // Thêm dòng này
    public static String pendingDraftIdToOpen = null; 
    // --- TÍCH HỢP TÍNH NĂNG DÙNG ĐIỂM (SHOPEE STYLE) ---
    private int diemHienTaiKH = 0;
    private boolean isDungDiem = false;
    private long tienGiamTuDiem = 0;
    private JLabel lblDungDiemValue; 
    private JPanel pnlDungDiem; 
    private JLabel lblDungDiemText;
    private CustomToggleSwitch toggleDungDiem;
    private JPanel pnlVoucherTags;
    private JPanel pnlChuyenKhoanWrapper;
    private JLabel lblQRCode;
    private JLabel lblQRAmount;
    private JLabel lblExactValue;
    private JScrollPane mainScrollPane;
    private int thoiGianConLai = 1200; // 600 giây = 10 phút
    private javax.swing.Timer boDemNguoc;
    private JLabel lblDongHoDemNguoc;
    private JPopupMenu suggestionInvoiceMenu;
    private JTextField txtNote; // Biến này để lưu ô Ghi chú, đưa ra toàn cục để dễ lấy dữ liệu
    private JCheckBox chkInHoaDon;
    private java.util.List<int[]> listCounters = new java.util.ArrayList<>();
    private java.util.List<JLabel> listCountLabels = new java.util.ArrayList<>();
 // Thêm dòng này ngay cạnh pendingPhoneToLink của bạn
    public static String pendingPhoneToLink = null;
    private javax.swing.Timer boKiemTraTienToi;
    private String maGiaoDichHienTai = "";
    public static String pendingNameToLink = null; // Thêm biến nhớ Tên
    public TaoHoaDon(Frame parent, DefaultTableModel mainModel) {
        super(parent, "Tạo hóa đơn bán hàng mới", true);
        this.mainTableModel = mainModel;
        this.editingRow = -1; 
        initUI(parent);
        
        // --- THÊM LỆNH LÀM MỚI KHUYẾN MÃI VÀO ĐÂY ---
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                lamMoiKhuyenMai();
                
                // --- ĐOẠN KHÔI PHỤC TRÍ NHỚ VÀ TỰ ĐỘNG LIÊN KẾT KHÁCH HÀNG ---
                if (ManHinhBanHang.pendingPhoneToLink != null) {
                    String phoneToLink = ManHinhBanHang.pendingPhoneToLink;
                    ManHinhBanHang.pendingPhoneToLink = null; // Xóa trí nhớ

                    SwingUtilities.invokeLater(() -> {
                        // TÌM THẲNG TRONG DATABASE VÀ GẮN LUÔN VÀO GIAO DIỆN (KHÔNG CẦN BẤM NÚT)
                    	try {
                            // Khởi tạo BUS Khách Hàng
                            BUS.BUS_KhachHang busKH = new BUS.BUS_KhachHang();
                            
                            // Gọi hàm tìm kiếm qua BUS thay vì thao tác Database trực tiếp
                            Entity.KhachHang kh = busKH.getKhachHangTheoSDT(phoneToLink);
                            
                            // Nếu tìm thấy khách hàng (Tương đương rs.next() == true)
                            if (kh != null) {
                                String maKH = kh.getId();
                                String tenKH = kh.getHoVaTen();
                                String sdtKH = kh.getSdt();
                                int diemKH = kh.getDiemTichLuy();

                                // 1. Cập nhật biến Logic
                                isCustomerLinked = true;
                                linkedTenKH = tenKH;
                                linkedSdtKH = sdtKH;
                                diemHienTaiKH = diemKH;
                                
                                // 2. Đổ dữ liệu lên UI Giao diện liên kết
                                lblLinkedAvatar.setText(tenKH.substring(0, 1).toUpperCase());
                                lblLinkedName.setText(tenKH);
                                lblLinkedSub.setText(maKH + " • " + sdtKH);
                                lblLinkedPoints.setText(diemKH + " điểm");
                                
                                long tienQuyDoi = diemHienTaiKH * 100L;
                                lblLinkedMoney.setText("≈ " + String.format("%,d", tienQuyDoi).replace(',', '.') + "đ");

                                if (diemHienTaiKH > 0) {
                                    lblDungDiemText.setText(String.format("Dùng %,d điểm", diemHienTaiKH) + " (-" + String.format("%,d", tienQuyDoi).replace(',', '.') + "đ)");
                                    pnlDungDiem.setVisible(true);
                                } else {
                                    pnlDungDiem.setVisible(false);
                                }
                                
                                toggleDungDiem.setOn(false);
                                isDungDiem = false;
                                
                                // 3. Chuyển đổi hiển thị Panel (Ẩn form nhập tay -> Hiện form đã liên kết)
                                pnlInputFields.setVisible(false);
                                pnlLinkedCustomer.setVisible(true);
                                
                                // 4. Chuyển Badge trạng thái sang xanh lá
                                if(lblBadgeLe != null && lblBadgeLe.getParent() != null) {
                                    java.awt.CardLayout cl = (java.awt.CardLayout)(lblBadgeLe.getParent().getLayout());
                                    cl.show(lblBadgeLe.getParent(), "LINKED");
                                }
                                
                                recalculateTotals();
                                System.out.println("Đã liên kết khách hàng tự động thành công!");
                            } else {
                                // Nếu lỗi không tìm thấy, đổ số điện thoại vào ô Tìm kiếm
                                txtSearch.setText(phoneToLink);
                                txtSearch.setForeground(java.awt.Color.BLACK);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                }
            }
        });
    }

    public TaoHoaDon(Frame parent, DefaultTableModel mainModel, int modelRow, String maHD, String tenKH, String sdt) {
        super(parent, "Chỉnh sửa hóa đơn nháp", true);
        this.mainTableModel = mainModel;
        this.editingModelRow = modelRow; 
        this.maHDDangSua = maHD;
        
        initUI(parent);

        if (tenKH != null && !tenKH.equals("Khách lẻ")) {
            txtName.setText(tenKH);
            txtName.setForeground(Color.BLACK);
        }
        if (sdt != null) {
            txtPhone.setText(sdt);
        }
        loadDuLieuHoaDonNhap(this.maHDDangSua);
        
        // --- VÀ THÊM VÀO ĐÂY NỮA ---
     // --- VÀ THÊM VÀO ĐÂY NỮA ---
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                lamMoiKhuyenMai();

                // --- BỔ SUNG ĐOẠN KHÔI PHỤC TRÍ NHỚ VÀO ĐÂY ---
                if (ManHinhBanHang.pendingPhoneToLink != null) {
                    String phoneToLink = ManHinhBanHang.pendingPhoneToLink;
                    ManHinhBanHang.pendingPhoneToLink = null; // Xóa trí nhớ

                    SwingUtilities.invokeLater(() -> {
                        // TÌM THẲNG TRONG DATABASE VÀ GẮN LUÔN VÀO GIAO DIỆN
                    	try {
                            BUS.BUS_KhachHang busKH = new BUS.BUS_KhachHang();
                            // Gọi qua BUS thay vì dùng SQL
                            Entity.KhachHang kh = busKH.getKhachHangTheoSDT(phoneToLink);
                            
                            if (kh != null) {
                                String maKH = kh.getId();
                                String tenKH = kh.getHoVaTen();
                                String sdtKH = kh.getSdt();
                                int diemKH = kh.getDiemTichLuy();

                                // 1. Cập nhật biến Logic
                                isCustomerLinked = true;
                                linkedTenKH = tenKH;
                                linkedSdtKH = sdtKH;
                                diemHienTaiKH = diemKH;
                                
                                // 2. Đổ dữ liệu lên UI
                                lblLinkedAvatar.setText(tenKH.substring(0, 1).toUpperCase());
                                lblLinkedName.setText(tenKH);
                                lblLinkedSub.setText(maKH + " • " + sdtKH);
                                lblLinkedPoints.setText(diemKH + " điểm");
                                
                                long tienQuyDoi = diemHienTaiKH * 100L;
                                lblLinkedMoney.setText("≈ " + String.format("%,d", tienQuyDoi).replace(',', '.') + "đ");

                                if (diemHienTaiKH > 0) {
                                    lblDungDiemText.setText(String.format("Dùng %,d điểm", diemHienTaiKH) + " (-" + String.format("%,d", tienQuyDoi).replace(',', '.') + "đ)");
                                    pnlDungDiem.setVisible(true);
                                } else {
                                    pnlDungDiem.setVisible(false);
                                }
                                
                                toggleDungDiem.setOn(false);
                                isDungDiem = false;
                                
                                // 3. Đổi giao diện sang Đã liên kết
                                pnlInputFields.setVisible(false);
                                pnlLinkedCustomer.setVisible(true);
                                
                                if(lblBadgeLe != null && lblBadgeLe.getParent() != null) {
                                    java.awt.CardLayout cl = (java.awt.CardLayout)(lblBadgeLe.getParent().getLayout());
                                    cl.show(lblBadgeLe.getParent(), "LINKED");
                                }
                                
                                recalculateTotals();
                                System.out.println("Đã liên kết khách hàng tự động thành công!");
                            } else {
                                txtSearch.setText(phoneToLink);
                                txtSearch.setForeground(java.awt.Color.BLACK);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                }
                // --- KẾT THÚC BỔ SUNG ---
            }
        });
    }
    private void lamMoiKhuyenMai() {
        pnlVoucherTags.removeAll();
        BUS_KhuyenMai busKM = new BUS_KhuyenMai();
        List<Object[]> ds = busKM.layDanhSachKhuyenMaiFull(); 
        
        if (ds != null && !ds.isEmpty()) {
            int count = 0;
            JPanel currentRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            currentRow.setOpaque(false);
            pnlVoucherTags.add(currentRow);

            for (Object[] km : ds) {
                String maKM = km[0] != null ? km[0].toString() : "UNKNOWN";
                String tenKM = km[1] != null ? km[1].toString() : "Khuyến mãi";
                String moTa = km[2] != null ? km[2].toString() : "";
                String tenSPY = km[5] != null ? km[5].toString() : "";
            
                String hienThi = (!moTa.trim().isEmpty()) ? moTa : tenKM;
                String label = maKM + " (" + hienThi + ")";
                if (!tenSPY.trim().isEmpty()) {
                    label += " - Áp dụng: " + tenSPY;
                }

                // Cứ đủ 5 mã thì rớt xuống hàng ngang mới
                if (count == 4) {
                    currentRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
                    currentRow.setOpaque(false);
                    pnlVoucherTags.add(currentRow);
                    count = 0;
                }

                currentRow.add(createVoucherTag(label, maKM, txtVoucherInput));
                count++;
            }
        }
        pnlVoucherTags.revalidate(); 
        pnlVoucherTags.repaint();
    }
    private void xuLyHoanThanhHoaDon() {
        BUS.BUS_DonViDoLuong busDonVi = new BUS.BUS_DonViDoLuong();
        BUS.BUS_Kho busKho = new BUS.BUS_Kho();
        BUS.BUS_HoaDon busHD = new BUS.BUS_HoaDon();

        String khach = isCustomerLinked ? linkedTenKH : txtName.getText();
        if(khach.equals("Tên khách (bỏ trống = Khách lẻ)") || khach.trim().isEmpty()) khach = "Khách lẻ";
        String sdt = isCustomerLinked ? linkedSdtKH : txtPhone.getText().replace("Số điện thoại (tuỳ chọn)", "");
        String tongTien = lblTotalPriceValue.getText();

        String maHDMoi = (editingModelRow != -1) ? maHDDangSua.replace("-LuuNhap", "") : phatSinhMaHoaDon();
        Entity.HoaDon hd = new Entity.HoaDon();
        
        hd.setId(maHDMoi);
        hd.setLoaiHD(Enumeration.LoaiHoaDon.BAN_HANG);
        hd.setNgayLapHD(java.time.LocalDateTime.now());

        String strDiem = isDungDiem ? " | Dùng điểm: -" + tienGiamTuDiem : "";
        String strKM = (maKhuyenMaiApDung == null || maKhuyenMaiApDung.isEmpty()) ? "" : " | KM: " + maKhuyenMaiApDung;
        StringBuilder strGifts = new StringBuilder();
        
        java.util.Map<String, Entity.ChiTietHoaDon> mapFinal = new java.util.HashMap<>();

        // Lấy kmRatioGlobal từ BUS — không đọc từ cột giao diện
        java.util.List<long[]> dsSPTmp = new java.util.ArrayList<>();
        for (int i = 0; i < productModel.getRowCount(); i++) {
            try {
            	String ten = productModel.getValueAt(i, 0).toString();
            	if (ten.startsWith("[QUÀ TẶNG]") || ten.startsWith("[LIỀU]")) continue;
                int sl = Integer.parseInt(productModel.getValueAt(i, 3).toString().trim());
                long dg = Long.parseLong(productModel.getValueAt(i, 4).toString().replaceAll("[^0-9]", ""));
                long vat = Long.parseLong(productModel.getValueAt(i, 8).toString().replace("%","").trim());
                dsSPTmp.add(new long[]{sl, dg, vat});
            } catch (Exception ignored) {}
        }
        BUS.BUS_HoaDon.KetQuaHoaDon kqSave = busHD.tinhToanTienHoaDon(dsSPTmp, false, 0, this.tienGiamGia);
        double kmRatioGlobal = kqSave.kmRatioGlobal;

        for (int i = 0; i < productModel.getRowCount(); i++) {
            String tenRow = productModel.getValueAt(i, 0).toString().trim();
            
            // Bỏ qua dòng tiêu đề ảo của Combo (ví dụ: [LIỀU] Cảm Cúm)
            if (tenRow.startsWith("[LIỀU]")) continue; 

            boolean isGift = tenRow.startsWith("[QUÀ TẶNG]");
            String tenSP = tenRow.replace("CHILD_ITEM ", "").replace("[QUÀ TẶNG]", "").trim();
            
            // [ĐIỂM FIX LỖI CỐT LÕI]: Dọn dẹp sạch sẽ mọi loại mũi tên đang dùng trên UI
            tenSP = tenSP.replace("➔", "")
                         .replace("↳", "")
                         .replace("=>", "")
                         .trim();
                         
            String tenDVT = productModel.getValueAt(i, 1).toString().trim();
            int soLuongMua = Integer.parseInt(productModel.getValueAt(i, 3).toString());
            
            String[] ids = busHD.layMaSPVaMaDVT(tenSP, tenDVT);
            if (ids == null) {
                showCustomNotification("LỖI THANH TOÁN", "Mặt hàng bị sai tên: " + tenSP, "ERROR");
                return; // Dừng thanh toán ngay nếu lỗi
            }
            String maSP = ids[0];
            String maDVT = ids[1];
            String key = maSP + "_" + maDVT; 

            if (isGift) {
                strGifts.append(" | TANG:").append(tenSP).append(";").append(soLuongMua).append(";").append(tenDVT);
                continue; // Quà tặng chỉ ghi vào ghiChu
            }

            if (mapFinal.containsKey(key)) {
                Entity.ChiTietHoaDon existing = mapFinal.get(key);
                existing.setSoLuong(existing.getSoLuong() + soLuongMua);
                long donGiaEx = 0;
                double vatRateEx = 0;
                try {
                    donGiaEx = Long.parseLong(productModel.getValueAt(i, 4).toString().replaceAll("[^0-9]", ""));
                    Object vatValEx = productModel.getColumnCount() >= 9 ? productModel.getValueAt(i, 8) : null;
                    if (vatValEx != null && !vatValEx.toString().trim().isEmpty())
                        vatRateEx = Double.parseDouble(vatValEx.toString().replace("%", "").trim());
                } catch (Exception ex2) {}
                double donGiaSauKMEx = donGiaEx * (1.0 - kmRatioGlobal);
                long thanhTienEx = Math.round(existing.getSoLuong() * donGiaSauKMEx * (1.0 + vatRateEx / 100.0));
                existing.setDonGiaThucTe(donGiaSauKMEx);
                existing.setThanhTien(thanhTienEx);
            } else {
                Entity.ChiTietHoaDon ct = new Entity.ChiTietHoaDon();
                ct.setHoaDonId(hd);
                Entity.SanPham sp = new Entity.SanPham(); sp.setId(maSP); ct.setSanPhamId(sp);
                Entity.DonViDoLuong dv = new Entity.DonViDoLuong(); dv.setId(maDVT); ct.setDonViDoLuongId(dv);
                ct.setSoLuong(soLuongMua);

                long donGia = 0;
                double vatRate = 0;
                try {
                    donGia   = Long.parseLong(productModel.getValueAt(i, 4).toString().replaceAll("[^0-9]", ""));
                    Object vatVal = productModel.getColumnCount() >= 9 ? productModel.getValueAt(i, 8) : null;
                    if (vatVal != null && !vatVal.toString().trim().isEmpty()) {
                        vatRate = Double.parseDouble(vatVal.toString().replace("%", "").trim());
                    }
                } catch (Exception exCalc) {}
                double donGiaSauKM = donGia * (1.0 - kmRatioGlobal);
                long thanhTienRow  = Math.round(soLuongMua * donGiaSauKM * (1.0 + vatRate / 100.0));
                ct.setDonGiaThucTe(donGiaSauKM);
                ct.setThanhTien(thanhTienRow);

                mapFinal.put(key, ct);
            }
        }

        StringBuilder strLieuMau = new StringBuilder();
        String tenLieuHienTai = "Thuốc liều mẫu"; 

        for (int i = 0; i < productModel.getRowCount(); i++) {
            Object valObj = productModel.getValueAt(i, 0);
            if (valObj == null) continue;
            String tenRow = valObj.toString().trim();
            
            if (tenRow.contains("[LIỀU]")) {
                tenLieuHienTai = tenRow.replace("[LIỀU]", "").trim(); 
            } else if (tenRow.contains("CHILD_ITEM")) {
                String realName = tenRow.replace("CHILD_ITEM", "").trim();
                if (strLieuMau.length() > 0) strLieuMau.append("~");
                strLieuMau.append(realName).append("=").append(tenLieuHienTai); 
            }
        }
        String lieuMauPart = strLieuMau.length() > 0 ? " | LIEU_MAU:" + strLieuMau.toString() : "";

        String userNote = txtNote.getText().trim();
        if (userNote.equals("Ghi chú thêm...")) userNote = "";
        String prefixUserNote = userNote.isEmpty() ? "" : (userNote + " | ");
        String strKeDon = "";
        if (pnlDonThuoc != null && pnlDonThuoc.isVisible()) {
            String bs = txtBacSi.getText().trim();
            String cs = txtCoSo.getText().trim();
            String cd = txtChuanDoan.getText().trim();
            if (cd.contains("Chẩn đoán bệnh")) cd = ""; 
            strKeDon = " | BS:" + bs + " | CS:" + cs + (cd.isEmpty() ? "" : " | CD:" + cd);
        }
        
        hd.setGhiChu(prefixUserNote + (phuongThuc.equals("Tiền mặt") ? "CASH:" + tongTienMat : "BANK") + strKeDon + strKM + strDiem + strGifts.toString() + lieuMauPart + " | VAT_AMT:" + this.vat);

        Entity.NhanVien nv = new Entity.NhanVien();
        String maNV = Utils.UserSession.getInstance().getMaNhanVien();
        
        nv.setNhanVien(maNV != null && !maNV.trim().isEmpty() ? maNV : "NV001");
        hd.setNhanVienId(nv);
        if (!sdt.isEmpty()) {
            BUS.BUS_KhachHang busKH = new BUS.BUS_KhachHang();
            Entity.KhachHang khObj = busKH.timKhachHangTheoSdt(sdt);
            if (khObj == null) {
                // Nếu là khách mới hoàn toàn nhập qua Textbox -> Tự động tạo mới
                khObj = new Entity.KhachHang();
                khObj.setId(busKH.phatSinhMaKHTiepTheo());
                khObj.setHoVaTen(khach);
                khObj.setSdt(sdt);
                khObj.setNgayTao(java.time.LocalDateTime.now());
                khObj.setDiemTichLuy(0);
                busKH.themKhachHang(khObj);
            }
            hd.setKhachHangId(khObj);
        }
        
        hd.setPhuongThucThanhToan(phuongThuc.equals("Tiền mặt") ? Enumeration.PhuongThucThanhToan.TIEN_MAT : Enumeration.PhuongThucThanhToan.CHUYEN_KHOAN_NGAN_HANG);
        if (maKhuyenMaiApDung != null && !maKhuyenMaiApDung.isEmpty()) {
            Entity.KhuyenMai km = new Entity.KhuyenMai(); km.setId(maKhuyenMaiApDung.split(",")[0].trim());
            hd.setKhuyenMaiId(km);
        }

        if (editingModelRow != -1 && maHDDangSua != null) {
            busHD.xoaHoaDonNhap(maHDDangSua);
        }

        java.util.List<Entity.ChiTietHoaDon> dsKetQua = new java.util.ArrayList<>(mapFinal.values());
        boolean success = busHD.thanhToan(hd, dsKetQua, new java.util.ArrayList<>()); 

        if (success) {
            if (boDemNguoc != null) boDemNguoc.stop();
            
            // --- FIX 2: BỎ ĐIỀU KIỆN isCustomerLinked, LUÔN GHI ĐIỂM NẾU CÓ SĐT ---
            if (!sdt.isEmpty()) {
                BUS.BUS_KhachHang busKH = new BUS.BUS_KhachHang();
                
                int diemDung = isDungDiem ? (int)(tienGiamTuDiem / 100) : 0;
                int diemMoi = (int) (tongHoaDon / 10000); 
                
                // 1. Cập nhật tổng điểm
                busKH.capNhatDiemTichLuy(sdt, diemMoi - diemDung);
                
                // 2. Ghi lịch sử
                Entity.KhachHang kh = busKH.timKhachHangTheoSdt(sdt);
                if (kh != null) {
                    DAO.DAO_KhachHang daoKH = new DAO.DAO_KhachHang();
                    
                    if (diemDung > 0) {
                        String ghiChuTru = "Sử dụng điểm cho hóa đơn " + maHDMoi; 
                        daoKH.ghiLichSuDiem(kh.getId(), maHDMoi, "TRU", diemDung, ghiChuTru);
                    }
                    
                    // Chỉ ghi lịch sử nếu thực sự có điểm cộng vào (tránh rác DB)
                    if (diemMoi > 0) {
                        String ghiChuTich = "Tích điểm từ hóa đơn " + maHDMoi;
                        daoKH.ghiLichSuDiem(kh.getId(), maHDMoi, "TICH", diemMoi, ghiChuTich);
                    }
                }
            }

            if (chkInHoaDon.isSelected()) {
                showCustomNotification("HOÀN TẤT", "Thanh toán thành công!\nHệ thống đang xuất lệnh in hóa đơn...", "SUCCESS");
            } else {
                showCustomNotification("HOÀN TẤT", "Thanh toán thành công!", "SUCCESS");
            }

            dispose();
        } else {
            showCustomNotification("LỖI", "Không thể lưu hóa đơn vào CSDL.", "ERROR");
        }
    }
    private void initUI(Frame parent) {
    	setSize(1350, 750);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(darkBlue);
        pnlHeader.setPreferredSize(new Dimension(0, 50));
        pnlHeader.setBorder(new EmptyBorder(0, 15, 0, 15));
        JLabel lblTitle = new JLabel(editingModelRow == -1 ? "Tạo hóa đơn bán hàng mới" : "Sửa hóa đơn: " + maHDDangSua);
        lblTitle.setIcon(new MenuIcon("CART"));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        pnlBody.add(createCustomerPanel());
        pnlBody.add(Box.createRigidArea(new Dimension(0, 8)));
        JButton btnChonLieuMau = new JButton(" Chọn liều phối sẵn");
        btnChonLieuMau.setIcon(new MenuIcon("PLUS")); // Bạn có thể thay icon PLUS thành icon khác nếu muốn
        btnChonLieuMau.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnChonLieuMau.setBackground(Color.decode("#E0F2FE")); // Nền xanh nhạt
        btnChonLieuMau.setForeground(Color.decode("#0284C7")); // Chữ xanh biển đậm
        btnChonLieuMau.setFocusPainted(false);
        btnChonLieuMau.setBorderPainted(false);
        btnChonLieuMau.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChonLieuMau.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btnChonLieuMau.addActionListener(e -> hienThiDialogChonLieuMau());
        pnlBody.add(createSectionPanel("Thêm sản phẩm", "PACKAGE", createProductPanel(), btnChonLieuMau));
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));
        
        pnlDonThuoc = createDonThuocPanel();
        pnlDonThuoc.setVisible(false); // Mặc định ẩn

        // 1. Tạo nút bấm Toggle
        btnToggleKeDon = new JButton(" Thêm thông tin kê đơn");
        btnToggleKeDon.setIcon(new MenuIcon("FILE")); 
        btnToggleKeDon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnToggleKeDon.setBackground(Color.decode("#F3F4F6")); 
        btnToggleKeDon.setForeground(Color.decode("#4B5563"));
        btnToggleKeDon.setFocusPainted(false);
        btnToggleKeDon.setBorderPainted(false);
        btnToggleKeDon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Thêm padding cho nút mập mạp và đẹp hơn
        btnToggleKeDon.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); 

        // 2. Sự kiện đóng/mở panel
        btnToggleKeDon.addActionListener(e -> {
            boolean isCurrentlyVisible = pnlDonThuoc.isVisible();
            pnlDonThuoc.setVisible(!isCurrentlyVisible);
            
            if (!isCurrentlyVisible) {
                btnToggleKeDon.setText(" Đã thêm thông tin kê đơn");
                btnToggleKeDon.setBackground(Color.decode("#FEE2E2"));
                btnToggleKeDon.setForeground(Color.decode("#DC2626"));
            } else {
                btnToggleKeDon.setText(" Thêm thông tin kê đơn");
                btnToggleKeDon.setBackground(Color.decode("#F3F4F6"));
                btnToggleKeDon.setForeground(Color.decode("#4B5563"));
                txtBacSi.setText(""); txtCoSo.setText(""); txtChuanDoan.setText("");
            }
            pnlBody.revalidate();
            pnlBody.repaint();
        });

        JPanel pnlKeDonMaster = new JPanel(new BorderLayout(0, 10));
        pnlKeDonMaster.setBackground(Color.WHITE);

        // 4. Bọc nút bấm vào khung nhỏ canh trái tuyệt đối
        JPanel pnlBtnLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlBtnLeft.setBackground(Color.WHITE);
        pnlBtnLeft.add(btnToggleKeDon);

        // 5. Ráp nối vào Khung Tổng
        pnlKeDonMaster.add(pnlBtnLeft, BorderLayout.NORTH); 
        pnlKeDonMaster.add(pnlDonThuoc, BorderLayout.CENTER); 

        // 6. Nhét Khung Tổng vào giao diện chính
        pnlBody.add(pnlKeDonMaster);
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));
        
        
        pnlBody.add(createSectionPanel("Mã khuyến mãi", "GIFT", createVoucherPanel()));
        pnlBody.add(Box.createRigidArea(new Dimension(0, 5)));
        
        pnlDungDiem = createDungDiemPanel();
        pnlDungDiem.setVisible(false);
        pnlBody.add(pnlDungDiem);
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));
        
        pnlBody.add(createSummaryPanel());

        // --- 1. THÊM LỚP "ÁO KHOÁC" ĐỂ ÉP KÍCH THƯỚC ---
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(Color.WHITE);
        wrapperPanel.add(pnlBody, BorderLayout.NORTH); 

        // --- 2. BỎ WRAPPER VÀO SCROLLPANE THAY VÌ PNLBODY ---
        mainScrollPane = new JScrollPane(wrapperPanel); 
        mainScrollPane.setBorder(null);
        mainScrollPane.getViewport().setBackground(Color.WHITE);
        
        // --- 3. KHÓA VĨNH VIỄN THANH CUỘN NGANG ---
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollBar customScrollBar = new JScrollBar() {
            @Override
            public void updateUI() {
                setUI(new ModernScrollBarUI()); 
            }
        };
        customScrollBar.setPreferredSize(new Dimension(10, 0));
        customScrollBar.setUnitIncrement(16);
        mainScrollPane.setVerticalScrollBar(customScrollBar);
        
        add(mainScrollPane, BorderLayout.CENTER);
        
        // =========================================================
        // GIAO DIỆN CHÂN TRANG (FOOTER) - LÀM TO NÚT & THÊM ĐỒNG HỒ
        // =========================================================
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor),
            new EmptyBorder(15, 25, 15, 25) // Tăng lề để form rộng ra
        ));

        if (editingModelRow != -1) {
            JButton btnXoaDon = new JButton("Xóa đơn");
            btnXoaDon.setIcon(new MenuIcon("TRASH")); 
            btnXoaDon.setIconTextGap(8);
            styleButton(btnXoaDon, Color.decode("#EF4444")); 
            btnXoaDon.setPreferredSize(new Dimension(140, 45)); // Nút to ra
            
            btnXoaDon.addActionListener(e -> {
                boolean confirm = showCustomConfirmDialog("Xác nhận hủy", "Bạn có chắc chắn muốn HỦY hóa đơn này không?<br>Thao tác này không thể hoàn tác.");
                
                if (confirm) {
                    if (boDemNguoc != null) boDemNguoc.stop(); // Dừng đồng hồ
                    
                    if (this.maHDDangSua != null && !this.maHDDangSua.isEmpty()) {
                        // GỌI QUA TẦNG BUS THAY VÌ CHẠY SQL TRỰC TIẾP
                        BUS.BUS_HoaDon busHD = new BUS.BUS_HoaDon();
                        boolean isSuccess = busHD.huyHoaDon(this.maHDDangSua);
                        
                        if (!isSuccess) {
                            showCustomNotification("LỖI", "Lỗi khi cập nhật hệ thống. Vui lòng thử lại!", "ERROR");
                            return; // Dừng lại nếu lỗi
                        }
                    }

                    // Cập nhật lại giao diện bảng bên ngoài màn hình chính
                    if (this.editingModelRow != -1) {
                        mainTableModel.setValueAt("Đã hủy", this.editingModelRow, 6); 
                    }

                    showCustomNotification("THÀNH CÔNG", "Đã hủy hóa đơn thành công!", "SUCCESS");
                    this.dispose(); // Đóng cửa sổ hiện tại
                }
            });
            pnlFooter.add(btnXoaDon, BorderLayout.WEST);
        }

        JPanel pnlRightFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlRightFooter.setOpaque(false);

        // Khởi tạo label Đồng hồ
        if (lblDongHoDemNguoc == null) {
            lblDongHoDemNguoc = new JLabel("");
        }
        lblDongHoDemNguoc.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDongHoDemNguoc.setForeground(Color.decode("#EF4444")); 
        lblDongHoDemNguoc.setBorder(new EmptyBorder(0, 0, 0, 15)); 
        
        // --- CHỈ HIỆN ĐỒNG HỒ NẾU ĐANG MỞ HÓA ĐƠN ĐÃ LƯU NHÁP ---
        if (editingModelRow == -1) {
            lblDongHoDemNguoc.setVisible(false);
        }

        JButton btnLuuNhap = new JButton("Lưu nháp");
        styleButton(btnLuuNhap, orangeLogo);
        btnLuuNhap.setPreferredSize(new Dimension(140, 45)); 

        JButton btnThanhToan = new JButton("Thanh toán");
        styleButton(btnThanhToan, successGreen);
        btnThanhToan.setPreferredSize(new Dimension(140, 45));

        btnThanhToan.addActionListener(e -> {
            if (productModel.getRowCount() == 0) {
                showCustomNotification("CẢNH BÁO", "Vui lòng thêm ít nhất 1 sản phẩm vào hóa đơn!", "WARNING");
                return;
            }
            if ("Chuyển khoản".equals(phuongThuc)) {
                boolean xacNhan = showCustomConfirmDialog(
                    "Xác nhận nhận tiền", 
                    "Khách hàng đang thanh toán bằng hình thức <b>CHUYỂN KHOẢN</b>.<br><br>" +
                    "Nhân viên vui lòng kiểm tra App Ngân hàng hoặc SMS.<br>" +
                    "Bạn xác nhận <b>ĐÃ NHẬN ĐỦ TIỀN</b> chưa?"
                );
                
                if (!xacNhan) {
                    return; 
                }
            }
            if ("Tiền mặt".equals(phuongThuc)) {
                if (tongTienMat < tongHoaDon) {
                    String strCanTra = String.format("%,d", tongHoaDon).replace(',', '.') + "đ";
                    String strKhachDua = String.format("%,d", tongTienMat).replace(',', '.') + "đ";
                    
                    showCustomNotification(
                        "THIẾU TIỀN MẶT", 
                        "Khách đưa chưa đủ tiền hoặc bạn quên nhập mệnh giá!\n\n" +
                        "• Cần thanh toán: " + strCanTra + "\n" +
                        "• Khách đưa mới: " + strKhachDua + "\n\n" +
                        "Vui lòng chọn đủ mệnh giá tiền khách đưa trước khi thanh toán.", 
                        "WARNING"
                    );
                    return; 
                } else {
                    // --- THÊM CHỐT CHẶN TIỀN THỐI KHÁCH Ở ĐÂY ---
                    long tienThua = tongTienMat - tongHoaDon;
                    if (tienThua > 0) {
                        long tienDangCo = layTienMatTrongKetHienTai();
                        
                        // Nếu số tiền phải thối vượt quá số tiền có sẵn trong két
                        if (tienThua > tienDangCo) {
                            showCustomNotification(
                                "KÉT KHÔNG ĐỦ TIỀN MẶT", 
                                "Tiền trong két không đủ để thối lại cho khách!\n\n" +
                                "• Két hiện có: " + String.format("%,d", tienDangCo).replace(',', '.') + "đ\n" +
                                "• Cần thối lại: " + String.format("%,d", tienThua).replace(',', '.') + "đ\n\n" +
                                "Gợi ý: Yêu cầu khách đổi sang thẻ/chuyển khoản hoặc đưa mệnh giá nhỏ hơn.", 
                                "ERROR"
                            );
                            return; // Chặn quá trình thanh toán
                        }
                    }
                    // ----------------------------------------------
                }
            }
            if (pnlDonThuoc.isVisible()) {
                String bacSi = txtBacSi.getText().trim();
                String coSo = txtCoSo.getText().trim();
                
                if (bacSi.isEmpty() || bacSi.contains("BS. Nguyễn") || coSo.isEmpty() || coSo.contains("BV Bạch Mai")) {
                    showCustomNotification("THIẾU THÔNG TIN", "Đơn hàng này có THUỐC KÊ ĐƠN.\nVui lòng nhập đầy đủ Tên Bác Sĩ và Cơ Sở Khám Bệnh!", "WARNING");
                    return; 
                }
            }
            
            // GỌI HÀM LƯU DATABASE VỪA TẠO
            xuLyHoanThanhHoaDon(); 
        });

        btnLuuNhap.addActionListener(e -> {
            if (boDemNguoc != null) boDemNguoc.stop(); // Dừng đồng hồ
            luuNhapHoaDon(false);
        });
        chkInHoaDon = new JCheckBox("In hóa đơn khi hoàn tất");
        chkInHoaDon.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chkInHoaDon.setForeground(Color.decode("#152A4B"));
        chkInHoaDon.setSelected(true); // Mặc định luôn tích sẵn
        chkInHoaDon.setFocusPainted(false);
        chkInHoaDon.setBackground(Color.WHITE);
        chkInHoaDon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pnlRightFooter.add(lblDongHoDemNguoc); // Gắn đồng hồ vào màn hình
        pnlRightFooter.add(btnLuuNhap);
        pnlRightFooter.add(btnThanhToan);
        pnlFooter.add(pnlRightFooter, BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);

        // BẮT ĐẦU ĐẾM NGƯỢC NGAY KHI MỞ FORM
        
    }

    
  
 // HÀM MỚI CHỈ CÒN 3 DÒNG: Gọi Class bên ngoài
    private void hienThiDialogChonLieuMau() {
        DialogChonLieuMau dialog = new DialogChonLieuMau(this);
        dialog.setVisible(true);
    }
    

 

    private JPanel createLieuMauCard(JDialog parentDialog, String tenLieu, String moTa, int soNgay, long giaDuKien) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#CBD5E1"), 1, true),
            new EmptyBorder(12, 15, 12, 15)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel pnlHead = new JPanel(new BorderLayout());
        pnlHead.setOpaque(false);
        
        JLabel lblTen = new JLabel(tenLieu);
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTen.setForeground(Color.decode("#0F172A"));
        
        JLabel lblNgay = new JLabel(soNgay + " Ngày");
        lblNgay.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNgay.setForeground(Color.decode("#059669"));
        lblNgay.setBackground(Color.decode("#D1FAE5"));
        lblNgay.setOpaque(true);
        lblNgay.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        pnlHead.add(lblTen, BorderLayout.WEST);
        pnlHead.add(lblNgay, BorderLayout.EAST);

        JLabel lblMoTa = new JLabel("<html><p style='color: #64748B; font-size: 13px;'>" + moTa + "</p></html>");
        
        JPanel pnlFoot = new JPanel(new BorderLayout());
        pnlFoot.setOpaque(false);
        
        JLabel lblGia = new JLabel("~ " + String.format("%,d", giaDuKien).replace(',', '.') + "đ");
        lblGia.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblGia.setForeground(Color.decode("#DC2626"));

        JButton btnChon = new JButton("Bán liều này");
        btnChon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnChon.setBackground(Color.decode("#2563EB"));
        btnChon.setForeground(Color.WHITE);
        btnChon.setFocusPainted(false);
        btnChon.setBorderPainted(false);

        pnlFoot.add(lblGia, BorderLayout.WEST);
        pnlFoot.add(btnChon, BorderLayout.EAST);

        card.add(pnlHead, BorderLayout.NORTH);
        card.add(lblMoTa, BorderLayout.CENTER);
        card.add(pnlFoot, BorderLayout.SOUTH);

        java.awt.event.MouseAdapter clickEvent = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#3B82F6"), 2, true),
                    new EmptyBorder(11, 14, 11, 14) 
                ));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#CBD5E1"), 1, true),
                    new EmptyBorder(12, 15, 12, 15)
                ));
            }
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    thucThiDoThuocTuLieuVaoGio(tenLieu, soNgay);
                    parentDialog.dispose(); 
                }
            }
        };
        card.addMouseListener(clickEvent);
        btnChon.addActionListener(e -> {
            thucThiDoThuocTuLieuVaoGio(tenLieu, soNgay);
            parentDialog.dispose();
        });

        return card;
    }

 // =================================================================
    // 1. HÀM NẠP CHỒNG (OVERLOAD) - Sửa triệt để lỗi gọi 2 tham số từ Card mẫu
    // =================================================================
    public void thucThiDoThuocTuLieuVaoGio(String tenLieu, int soNgay) {
        thucThiDoThuocTuLieuVaoGio(tenLieu, soNgay, null, ""); 
    }

    public void thucThiDoThuocTuLieuVaoGio(String tenLieu, int soNgay, java.util.Map<String, Integer> mapThuocChon, String huongDanSuDung) {
        try {
            String idLieuMau = null;
            java.util.List<Entity.LieuMau> dsLieuMau = busLieuMau.getTatCaLieuMau();
            for (Entity.LieuMau lm : dsLieuMau) {
                if (lm.getTenLieu().equalsIgnoreCase(tenLieu.trim())) { idLieuMau = lm.getId(); break; }
            }
            if (idLieuMau == null) return;

            java.util.List<Object[]> dsChiTiet = busLieuMau.getChiTietThuocCuaLieu(idLieuMau);
            if (dsChiTiet == null || dsChiTiet.isEmpty()) return;

            String headerName = "[LIỀU] " + tenLieu.toUpperCase();
            
            // 1. Kiểm tra xem Liều này đã có trong bảng chưa (Gộp số lượng nếu đã có)
            int existingHeaderRow = -1;
            for(int i = 0; i < productModel.getRowCount(); i++) {
                if(productModel.getValueAt(i, 0).toString().equals(headerName)) {
                    existingHeaderRow = i; break;
                }
            }
            
            if (existingHeaderRow != -1) {
                int currentDays = Integer.parseInt(productModel.getValueAt(existingHeaderRow, 3).toString());
                productModel.setValueAt(String.valueOf(currentDays + soNgay), existingHeaderRow, 3);
                
                // [FIX] - TỰ ĐỘNG ĐIỀN GHI CHÚ NẾU LIỀU ĐÃ TỒN TẠI VÀ BỊ CỘNG DỒN
                if (huongDanSuDung != null && !huongDanSuDung.trim().isEmpty()) {
                    String currentNote = txtNote.getText().trim();
                    if (!currentNote.contains(huongDanSuDung)) { // Chống điền lặp chữ nếu thao tác 2 lần
                        txtNote.setText(currentNote.equals("Ghi chú thêm...") ? huongDanSuDung : currentNote + ". " + huongDanSuDung);
                        txtNote.setForeground(Color.BLACK);
                    }
                }
                return; 
            }

            // 1. THÊM DÒNG NÀY: Tạo dòng Tiêu đề của Liều vào bảng trước
            productModel.addRow(new Object[]{
                    headerName,             // Cột 0: Tên tiêu đề (vd: [LIỀU] HO ĐỜM)
                    "Liều",                 // Cột 1: ĐVT
                    "",                     // Cột 2: Lô/HSD
                    String.valueOf(soNgay), // Cột 3: Số lượng (Số ngày)
                    "0đ",                   // Cột 4: Đơn giá (SỬA "" THÀNH "0đ")
                    "0%",                   // Cột 5: KM%
                    "0đ",                   // Cột 6: Thành tiền
                    "",                     // Cột 7: Nút xóa
                    "0%",                   // Cột 8: Cột VAT ẩn
                    "1"                     // Cột 9: Base Qty ẩn
                });
            
            // Lưu lại vị trí của dòng Tiêu đề vừa thêm để lát nữa gắn tổng tiền
            int headerRowIndex = productModel.getRowCount() - 1; 

            BUS.BUS_SanPham busSP = new BUS.BUS_SanPham();
            long tongTienTatCaThuocTrongLieu = 0; // Biến cộng dồn tiền
            
            // 2. Tính tổng tiền 1 liều (Có áp dụng bộ lọc mapThuocChon)
            for (Object[] row : dsChiTiet) {
                String idSP = row[0] != null ? row[0].toString() : "";
                
                // Nếu có truyền Map bộ lọc và thuốc này KHÔNG được tích chọn -> Bỏ qua không đẩy vào bảng
                if (mapThuocChon != null && !mapThuocChon.containsKey(idSP)) continue;
                
                String tenSP = row[1] != null ? row[1].toString() : "";
                int soLuongMotLieu = (mapThuocChon != null) ? mapThuocChon.get(idSP) : (row[3] != null ? Integer.parseInt(row[3].toString()) : 0);
                String dvt = row[4] != null ? row[4].toString() : "Viên";
                long giaBan = row[5] != null ? (long) Double.parseDouble(row[5].toString()) : 0;
                
                String thueVatStr = "0%"; 
                String loHsdText = "Chưa có lô";
                
                try {
                    java.util.List<Object[]> ketQua = busSP.timKiemSanPhamBan(tenSP);
                    if (ketQua != null && !ketQua.isEmpty()) {
                        Object[] firstItem = ketQua.get(0);
                        if (firstItem.length > 6 && firstItem[6] != null) {
                            double vatVal = Double.parseDouble(firstItem[6].toString());
                            if (vatVal > 0) {
                                if (vatVal < 1) vatVal = vatVal * 100;
                                thueVatStr = (int) vatVal + "%";
                            }
                        }
                        String loHang = (firstItem.length > 7 && firstItem[7] != null) ? firstItem[7].toString() : "";
                        String hsd = (firstItem.length > 8 && firstItem[8] != null) ? firstItem[8].toString() : "";
                        if (!loHang.isEmpty()) loHsdText = loHang + (!hsd.isEmpty() ? " — " + hsd : "");
                    }
                } catch (Exception e) {}

                int tongSoLuongMua = soLuongMotLieu * soNgay;
                
                // 2. THÊM DÒNG NÀY: Cộng dồn tiền của loại thuốc này vào tổng tiền Liều
                tongTienTatCaThuocTrongLieu += (giaBan * tongSoLuongMua);

                String giaBanFormatted = String.format("%,d", giaBan).replace(',', '.') + "đ";
                String thanhTienFormatted = String.format("%,d", giaBan * tongSoLuongMua).replace(',', '.') + "đ";

                productModel.addRow(new Object[]{
                    "CHILD_ITEM " + tenSP, 
                    dvt, 
                    loHsdText, 
                    String.valueOf(tongSoLuongMua), 
                    giaBanFormatted, 
                    "0%", 
                    thanhTienFormatted, 
                    "", 
                    thueVatStr, 
                    String.valueOf(soLuongMotLieu)
                });
            }
            
            // 3. THÊM DÒNG NÀY: Gắn lại tổng tiền vào dòng Tiêu đề Liều ở trên cùng
            productModel.setValueAt(String.format("%,d", tongTienTatCaThuocTrongLieu).replace(',', '.') + "đ", headerRowIndex, 6);

            isTableUpdating = false; 
            recalculateTotals();

            // --- [FIX] - TỰ ĐỘNG ĐIỀN GHI CHÚ CHO TRƯỜNG HỢP THÊM MỚI ---
            if (huongDanSuDung != null && !huongDanSuDung.trim().isEmpty()) {
                String currentNote = txtNote.getText().trim();
                if (!currentNote.contains(huongDanSuDung)) { // Chống lặp chữ
                    if (currentNote.equals("Ghi chú thêm...") || currentNote.isEmpty()) {
                        txtNote.setText(huongDanSuDung);
                        txtNote.setForeground(Color.BLACK);
                    } else {
                        txtNote.setText(currentNote + ". " + huongDanSuDung);
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void thucThiDoThuocTuLieuVaoGio_TuComboId(String comboId) {
        if (comboId == null || comboId.trim().isEmpty()) return;
 
        try {
            BUS.BUS_SanPham busSP = new BUS.BUS_SanPham();
 
            // ── Bước 1: lấy đầy đủ combo từ DB ──────────────────────────────
            Entity.SanPham.MauLieu mau = busSP.layComboByIdNangCao(comboId.trim());
            if (mau == null) {
                showCustomNotification("CẢNH BÁO",
                    "Không tìm thấy combo này trong hệ thống.", "WARNING");
                return;
            }
            if (mau.getDsChiTiet() == null || mau.getDsChiTiet().isEmpty()) {
                showCustomNotification("CẢNH BÁO",
                    "Combo «" + mau.getTenCombo() + "» chưa có thuốc nào.", "WARNING");
                return;
            }
 
            // ── Bước 2: kiểm tra combo đã ở trong hóa đơn chưa ──────────────
            String headerName = "[LIỀU] " + mau.getTenCombo().toUpperCase();
            for (int i = 0; i < productModel.getRowCount(); i++) {
                if (productModel.getValueAt(i, 0).toString().equals(headerName)) {
                    showCustomNotification("THÔNG BÁO",
                        "Combo «" + mau.getTenCombo() + "» đã có trong hóa đơn rồi!", "WARNING");
                    return;
                }
            }
 
            // ── Bước 3: tính số ngày lớn nhất trong combo để hiện ở dòng header ─
            int soNgayMax = mau.getDsChiTiet().stream()
                .mapToInt(Entity.SanPham.ChiTietLieu::getSoNgay)
                .max().orElse(0);
 
            // ── Bước 4: thêm dòng tiêu đề [LIỀU] vào bảng ───────────────────
            productModel.addRow(new Object[]{
                headerName,                 // [0] Tên tiêu đề — được nhận dạng bởi recalculate
                "Liều",                     // [1] ĐVT
                "",                         // [2] Lô/HSD
                String.valueOf(soNgayMax),  // [3] Số lượng hiển thị = số ngày dùng
                "0đ",                       // [4] Đơn giá (sẽ cập nhật thành tổng ở cột 6)
                "0%",                       // [5] KM%
                "0đ",                       // [6] Thành tiền (sẽ cập nhật cuối)
                "",                         // [7] nút xóa
                "0%",                       // [8] VAT ẩn
                "1"                         // [9] Base qty ẩn
            });
            int headerRowIndex = productModel.getRowCount() - 1;
 
            // ── Bước 5: duyệt từng thuốc con trong combo ─────────────────────
            long tongTienCombo = 0;
 
            for (Entity.SanPham.ChiTietLieu ct : mau.getDsChiTiet()) {
                String tenSP   = ct.getTenSanPham();
                String dvt     = ct.getDvt();
                int    tongSL  = ct.getTongSoLuong();   // = (S+T+Ch+To) × soNgay
 
                // Giá ban đầu từ combo (lúc tạo mẫu)
                long   giaBan  = (long) ct.getGiaDonVi();
                String vatStr  = "0%";
                String loHsd   = "Chưa có lô";
 
                // Cập nhật giá thực tế + lô FEFO từ DB (ưu tiên hơn giá lưu trong mẫu)
                try {
                    java.util.List<Object[]> ketQua = busSP.timKiemSanPhamBan(tenSP);
                    // timKiemSanPhamBan trả về:
                    // [0]=maSP [1]=ten [2]=donViDoCoBan [3]=soLuongTon
                    // [4]=dvt  [5]=thueVAT [6]=giaBan [7]=soLoHang [8]=hsd
                    if (ketQua != null && !ketQua.isEmpty()) {
                        Object[] first = ketQua.get(0);
 
                        // Giá thực từ DB
                        if (first.length > 6 && first[6] != null) {
                            try { giaBan = (long) Double.parseDouble(first[6].toString()); }
                            catch (Exception ignored) {}
                        }
 
                        // VAT
                        if (first.length > 5 && first[5] != null) {
                            try {
                                double vatVal = Double.parseDouble(first[5].toString());
                                if (vatVal > 0) {
                                    if (vatVal < 1) vatVal *= 100; // 0.05 → 5
                                    vatStr = (int) vatVal + "%";
                                }
                            } catch (Exception ignored) {}
                        }
 
                        // Lô / HSD (FEFO — cận hạn lên đầu đã được sort trong query)
                        String lo  = (first.length > 7 && first[7] != null) ? first[7].toString() : "";
                        String hsd = (first.length > 8 && first[8] != null) ? first[8].toString() : "";
                        if (!lo.isEmpty()) loHsd = lo + (!hsd.isEmpty() ? " — " + hsd : "");
                    }
                } catch (Exception ignored) {}
 
                long thanhTien = giaBan * tongSL;
                tongTienCombo += thanhTien;
 
                // Thêm dòng thuốc con (prefix CHILD_ITEM để recalculate nhận dạng)
                productModel.addRow(new Object[]{
                    "CHILD_ITEM " + tenSP,
                    dvt,
                    loHsd,
                    String.valueOf(tongSL),
                    String.format("%,d", giaBan).replace(',', '.') + "đ",
                    "0%",
                    String.format("%,d", thanhTien).replace(',', '.') + "đ",
                    "",
                    vatStr,
                    // Base qty = liều/lần (S+T+Ch+To), dùng để tính lại khi đổi số ngày
                    String.valueOf((int)(ct.getSang() + ct.getTrua() + ct.getChieu() + ct.getToi()))
                });
            }
 
            // ── Bước 6: cập nhật tổng tiền vào dòng tiêu đề ─────────────────
            productModel.setValueAt(
                String.format("%,d", tongTienCombo).replace(',', '.') + "đ",
                headerRowIndex, 6
            );
 
            isTableUpdating = false;
            recalculateTotals();
 
            showCustomNotification(
                "ĐÃ THÊM COMBO",
                "Đã thêm «" + mau.getTenCombo() + "» ("
                    + mau.getDsChiTiet().size() + " loại thuốc) vào hóa đơn!",
                "SUCCESS"
            );
 
        } catch (Exception ex) {
            ex.printStackTrace();
            showCustomNotification("LỖI",
                "Không thể load combo: " + ex.getMessage(), "ERROR");
        }
    }

    private void themMotThuocTuLieuVaoBang(String name, String unit, int slThem, long giaBan, String vat, String chosenLoHsd) {
        boolean daTonTai = false;
        int rowIndex = -1;
        int currentQty = 0;

        for (int i = 0; i < productModel.getRowCount(); i++) {
            if (productModel.getValueAt(i, 0).toString().equals(name) &&
                productModel.getValueAt(i, 1).toString().equals(unit)) {
                daTonTai = true;
                rowIndex = i;
                currentQty = Integer.parseInt(productModel.getValueAt(i, 3).toString());
                break;
            }
        }

        if (daTonTai) {
            currentQty += slThem;
            productModel.setValueAt(String.valueOf(currentQty), rowIndex, 3);
            
            double kmValue = 0;
            try { kmValue = Double.parseDouble(productModel.getValueAt(rowIndex, 5).toString().replace("%", "").trim()) / 100.0; } catch (Exception ex) {}
            long thanhTien = Math.round(giaBan * currentQty * (1.0 - kmValue));
            productModel.setValueAt(String.format("%,d", thanhTien).replace(',', '.') + "đ", rowIndex, 6);
        } else {
            String loHsdText = "Chưa có lô";
            
            // Ưu tiên lấy Lô từ tham số truyền vào
            if (chosenLoHsd != null && !chosenLoHsd.isEmpty() && !chosenLoHsd.equals("Chưa có lô")) {
                loHsdText = chosenLoHsd;
            } else {
                // TỰ ĐỘNG TÌM LÔ/HSD TỪ DATABASE NẾU TRUYỀN NULL (Fallback FEFO)
                try {
                    BUS.BUS_SanPham busSP = new BUS.BUS_SanPham();
                    java.util.List<Object[]> ketQua = busSP.timKiemSanPhamBan(name);
                    
                    if (ketQua != null && !ketQua.isEmpty()) {
                        Object[] firstItem = ketQua.get(0);
                        String loHang = (firstItem.length > 7 && firstItem[7] != null) ? firstItem[7].toString() : "";
                        String hsd = (firstItem.length > 8 && firstItem[8] != null) ? firstItem[8].toString() : "";
                        
                        if (!loHang.isEmpty() && !hsd.isEmpty()) loHsdText = loHang + " — " + hsd;
                        else if (!loHang.isEmpty()) loHsdText = loHang;
                        else if (!hsd.isEmpty()) loHsdText = hsd;
                    }
                } catch (Exception ex) {}
            }

            String giaFormatted = String.format("%,d", giaBan).replace(',', '.') + "đ";
            long thanhTien = giaBan * slThem; 
            String thanhTienFormatted = String.format("%,d", thanhTien).replace(',', '.') + "đ";

            productModel.addRow(new Object[]{
            	    name, unit, loHsdText, String.valueOf(slThem), giaFormatted, "0%", thanhTienFormatted, "", vat, "1"
            	});
        }
    }
    
    private void batDauQuetGiaoDichNganHang(String maGiaoDich, long soTienCanNhan) {
        if (boKiemTraTienToi != null && boKiemTraTienToi.isRunning()) {
            boKiemTraTienToi.stop();
        }

        boKiemTraTienToi = new javax.swing.Timer(3000, e -> {
            boolean daNhanTien = kiemTraLichSuGiaoDichTuAPI(maGiaoDich, soTienCanNhan);
            if (daNhanTien) {
                boKiemTraTienToi.stop(); 
                showCustomNotification("TING TING", "Đã nhận " + String.format("%,d", soTienCanNhan) + "đ\nHệ thống đang tự động chốt đơn...", "SUCCESS");
                
                // Gọi hàm chốt đơn, bên trong hàm này ĐÃ CÓ lệnh dispose() để tự đóng cửa sổ
                xuLyHoanThanhHoaDon(); 
            }
        });
        boKiemTraTienToi.start();
    }

    private boolean kiemTraLichSuGiaoDichTuAPI(String maGiaoDich, long soTien) {
        try {
            // =============== ĐIỀN LẠI MÃ CỦA BẠN VÀO ĐÂY ===============
        	String clientId = "";
            String apiKey = "";
            // =========================================================

            String apiUrl = "https://api-merchant.payos.vn/v2/payment-requests/" + maGiaoDich;

            java.net.URL url = new java.net.URL(apiUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("x-client-id", clientId);
            conn.setRequestProperty("x-api-key", apiKey);
            conn.setRequestProperty("Content-Type", "application/json");

            if (conn.getResponseCode() == 200) {
                java.util.Scanner s = new java.util.Scanner(conn.getInputStream(), "UTF-8").useDelimiter("\\A");
                String response = s.hasNext() ? s.next() : "";
                
                // Trạng thái PAID nghĩa là tiền đã vào tài khoản ACB của bạn
                if (response.contains("\"status\":\"PAID\"")) {
                    return true;
                }
            }
        } catch (Exception ex) {
            System.err.println("Lỗi gọi API PayOS: " + ex.getMessage());
        }
        return false;
    }

    private void loadQRCodeVCB(JLabel lblQRCode) {
        lblQRCode.setIcon(null);
        lblQRCode.setText("Đang tạo mã QR PayOS...");

        long finalTotalAmount = this.tongHoaDon;
        if (finalTotalAmount <= 0) {
            lblQRCode.setText("Vui lòng thêm sản phẩm!");
            return;
        }

        // 1. PayOS bắt buộc orderCode phải là SỐ. Dùng Unix timestamp là chuẩn nhất.
        long orderCode = System.currentTimeMillis() / 1000;
        this.maGiaoDichHienTai = String.valueOf(orderCode); 
        String description = "Thanh toan " + orderCode;

        SwingWorker<ImageIcon, Void> worker = new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                // =============== ĐIỀN MÃ CỦA BẠN VÀO ĐÂY ===============
                String clientId = "";
                String apiKey = "";
                String checksumKey = "";
                // =========================================================

                String cancelUrl = "https://localhost";
                String returnUrl = "https://localhost";

                // 2. TẠO CHỮ KÝ BẢO MẬT (SIGNATURE)
                String dataForSignature = "amount=" + finalTotalAmount + "&cancelUrl=" + cancelUrl + "&description=" + description + "&orderCode=" + orderCode + "&returnUrl=" + returnUrl;
                
                javax.crypto.Mac sha256_HMAC = javax.crypto.Mac.getInstance("HmacSHA256");
                javax.crypto.spec.SecretKeySpec secret_key = new javax.crypto.spec.SecretKeySpec(checksumKey.getBytes("UTF-8"), "HmacSHA256");
                sha256_HMAC.init(secret_key);
                byte[] hash = sha256_HMAC.doFinal(dataForSignature.getBytes("UTF-8"));
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                String signature = hexString.toString();

                // 3. GỌI API TẠO PAYMENT LINK
                java.net.URL url = new java.net.URL("https://api-merchant.payos.vn/v2/payment-requests");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000); // Thêm timeout để không treo app khi mất mạng
                conn.setReadTimeout(5000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("x-client-id", clientId);
                conn.setRequestProperty("x-api-key", apiKey);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String jsonBody = "{"
                        + "\"orderCode\": " + orderCode + ","
                        + "\"amount\": " + finalTotalAmount + ","
                        + "\"description\": \"" + description + "\","
                        + "\"cancelUrl\": \"" + cancelUrl + "\","
                        + "\"returnUrl\": \"" + returnUrl + "\","
                        + "\"signature\": \"" + signature + "\""
                        + "}";

                try (java.io.OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonBody.getBytes("UTF-8");
                    os.write(input, 0, input.length);
                }

                if (conn.getResponseCode() == 200) {
                    java.util.Scanner s = new java.util.Scanner(conn.getInputStream(), "UTF-8").useDelimiter("\\A");
                    String response = s.hasNext() ? s.next() : "";

                    String qrData = "";
                    if (response.contains("\"qrCode\":\"")) {
                        int start = response.indexOf("\"qrCode\":\"") + 10;
                        int end = response.indexOf("\"", start);
                        qrData = response.substring(start, end);
                    }

                    if (!qrData.isEmpty()) {
                    	String qrImageUrl = "https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=" + java.net.URLEncoder.encode(qrData, "UTF-8");
                    	java.net.URL imgUrl = new java.net.URL(qrImageUrl);
                    	java.net.HttpURLConnection imgConn = (java.net.HttpURLConnection) imgUrl.openConnection();
                    	imgConn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    	java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(imgConn.getInputStream());
                    	return new ImageIcon(image.getScaledInstance(200, 200, java.awt.Image.SCALE_SMOOTH));
                    }
                }
                
                // --- BỔ SUNG: NẾU GỌI API LỖI HOẶC KHÔNG CÓ MẠNG, CHUYỂN SANG QR OFFLINE ---
                return sinhQRCodeOffline(finalTotalAmount, maHDDangSua);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        lblQRCode.setText("");
                        lblQRCode.setIcon(icon);
                        
                        // Nếu là mã online thì mới cần quét, mã offline chỉ hiện để khách quét tay
                        batDauQuetGiaoDichNganHang(maGiaoDichHienTai, finalTotalAmount);

                        for (java.awt.event.MouseListener ml : lblQRCode.getMouseListeners()) {
                            lblQRCode.removeMouseListener(ml);
                        }
                        lblQRCode.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mouseClicked(java.awt.event.MouseEvent e) {
                                if (e.getClickCount() == 2) {
                                    if (boKiemTraTienToi != null) boKiemTraTienToi.stop();
                                    showCustomNotification("TING TING (DEMO)", "Đã chốt đơn thủ công!", "SUCCESS");
                                    xuLyHoanThanhHoaDon();
                                }
                            }
                        });
                    } else {
                        lblQRCode.setText("Lỗi tạo QR!");
                    }
                } catch (Exception ex) {
                    lblQRCode.setText("Lỗi kết nối!");
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    // Hàm dự phòng Offline
    private ImageIcon sinhQRCodeOffline(long soTien, String maHD) {
        String data = "Ngan hang: MBBank | STK: 123456789 | So tien: " + soTien + " | ND: HD " + maHD;
        return generateQR(data, 200); // Dùng hàm generateQR offline bạn đã có
    }
    private void khoiDongDongHoHuyDon(int thoiGianGiay) {
        if (boDemNguoc != null && boDemNguoc.isRunning()) {
            boDemNguoc.stop();
        }
        thoiGianConLai = thoiGianGiay; 
        
        // Nếu mở lên mà đã quá hạn 10 phút -> Hủy luôn
        if (thoiGianConLai <= 0) {
            thucHienHuyDonTuDong();
            return;
        }

        // Khởi chạy dòng chữ hiển thị ngay lập tức để không bị delay 1s
        int p = thoiGianConLai / 60;
        int g = thoiGianConLai % 60;
        if (lblDongHoDemNguoc != null) {
            lblDongHoDemNguoc.setText(String.format("Hủy đơn sau: %02d:%02d", p, g));
        }
        
        boDemNguoc = new javax.swing.Timer(1000, e -> {
            thoiGianConLai--;
            int phut = thoiGianConLai / 60;
            int giay = thoiGianConLai % 60;
            if (lblDongHoDemNguoc != null) {
                lblDongHoDemNguoc.setText(String.format("Hủy đơn sau: %02d:%02d", phut, giay));
            }
            
            if (thoiGianConLai <= 0) {
                boDemNguoc.stop();
                thucHienHuyDonTuDong();
            }
        });
        boDemNguoc.start();
    }

    private void thucHienHuyDonTuDong() {
        if (this.maHDDangSua != null && !this.maHDDangSua.isEmpty()) {
            BUS.BUS_HoaDon busHD = new BUS.BUS_HoaDon();
            busHD.huyHoaDon(this.maHDDangSua);
        }
        if (this.editingModelRow != -1 && mainTableModel != null) {
            mainTableModel.setValueAt("Đã hủy", this.editingModelRow, 6); 
        }
        this.dispose(); 
    }
    class CustomToggleSwitch extends JPanel {
        private boolean isOn = false;
        private Runnable onToggle;
        
        public CustomToggleSwitch(Runnable onToggle) {
            this.onToggle = onToggle;
            setPreferredSize(new Dimension(50, 26));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setOpaque(false);
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    isOn = !isOn;
                    repaint();
                    if (onToggle != null) onToggle.run();
                }
            });
        }
        public boolean isOn() { return isOn; }
        public void setOn(boolean on) { this.isOn = on; repaint(); }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(isOn ? Color.decode("#EE4D2D") : Color.decode("#E5E7EB")); 
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            
            g2.setColor(Color.WHITE);
            int padding = 2;
            int r = getHeight() - (padding * 2); 
            int x = isOn ? getWidth() - r - padding : padding;
            g2.fillOval(x, padding, r, r);
            
            g2.dispose();
        }
    }

    private JPanel createDungDiemPanel() {
        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(12, 15, 12, 15)
        ));

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0)); // Tăng khoảng cách 15px cho thoáng
        pnlLeft.setOpaque(false);
        
        // --- FIX: Biến ô chữ P thành một ĐỒNG XU TRÒN to đẹp, không bao giờ bị dính vách ---
        JLabel lblIcon = new JLabel("MyCare", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#FEF2F2")); // Nền đỏ nhạt
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.decode("#EE4D2D")); // Viền cam đỏ
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.drawOval(1, 1, getWidth()-3, getHeight()-3); // Bo viền tròn khít
                super.paintComponent(g);
                g2.dispose();
            }
        };
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 10)); // Chữ P to rõ ràng
        lblIcon.setForeground(Color.decode("#EE4D2D"));
        lblIcon.setPreferredSize(new Dimension(45, 38)); // Phóng to đồng xu lên 38x38
        lblIcon.setBorder(null); // Xóa bỏ cái viền hình vuông bị lỗi cũ

        lblDungDiemText = new JLabel("Dùng 0 điểm (-0đ)");
        lblDungDiemText.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblDungDiemText.setForeground(Color.decode("#212B36"));

        pnlLeft.add(lblIcon);
        pnlLeft.add(lblDungDiemText);

        toggleDungDiem = new CustomToggleSwitch(() -> {
            isDungDiem = toggleDungDiem.isOn();
            recalculateTotals();
        });

        pnl.add(pnlLeft, BorderLayout.WEST);
        pnl.add(toggleDungDiem, BorderLayout.EAST);
        return pnl;
    }

    private void recalculateTotals() {
        if (productModel == null) return;
        
        int currentScrollPos = 0;
        if (mainScrollPane != null) currentScrollPos = mainScrollPane.getVerticalScrollBar().getValue();
        
        // 1. GUI chỉ làm nhiệm vụ dọn dẹp chuỗi (bỏ chữ "đ", "%") và thu thập dữ liệu thô
        java.util.List<long[]> danhSachSP = new java.util.ArrayList<>();
        
        for (int i = 0; i < productModel.getRowCount(); i++) {
            try {
                String tenSp = productModel.getValueAt(i, 0).toString();
                
                // CHỈ BỎ QUA dòng Tiêu đề Liều, giữ lại Quà Tặng để đếm đủ Tổng Sản Phẩm
                if (tenSp.startsWith("[LIỀU]")) continue;

                int sl = Integer.parseInt(getSafeValue(productModel, i, 3).toString().trim());
                long donGia = Long.parseLong(getSafeValue(productModel, i, 4).toString().replaceAll("\\D+", ""));
                
                // Đọc VAT% từ cột ẩn (cột 8)
                long vatPercent = 0;
                try {
                    Object vatVal = getSafeValue(productModel, i, 8);
                    if (vatVal != null && !vatVal.toString().trim().isEmpty()) {
                        vatPercent = Long.parseLong(vatVal.toString().replace("%", "").replaceAll("\\s+", ""));
                    }
                } catch (Exception ex) {}
                
                danhSachSP.add(new long[]{sl, donGia, vatPercent});
            } catch (Exception ex) {}
        }

        // --- GỌI BUS TÍNH TOÁN (ĐÁP ỨNG ĐÚNG CHUẨN 3 LỚP) ---
        BUS.BUS_HoaDon busHD = new BUS.BUS_HoaDon();
        BUS.BUS_HoaDon.KetQuaHoaDon kq = busHD.tinhToanTienHoaDon(danhSachSP, isDungDiem, diemHienTaiKH, tienGiamGia);

        // Cập nhật lại các biến toàn cục của Form
        this.tamTinh = kq.tamTinh;
        this.vat = kq.tongVat;
        this.tienGiamTuDiem = kq.tienGiamTuDiem;
        this.tongHoaDon = kq.tongThanhToan;
        
        // Khuyến mãi vẫn tạm để đây do liên quan giao diện
        tuDongApDungKhuyenMai(); 

        // [FIX LỖI 3]: Tính lại kq sau khi tuDongApDungKhuyenMai() đã cập nhật tienGiamGia,
        // tránh hiển thị kết quả cũ (trước khuyến mãi) lên UI
        kq = busHD.tinhToanTienHoaDon(danhSachSP, isDungDiem, diemHienTaiKH, tienGiamGia);
        this.tamTinh        = kq.tamTinh;
        this.vat            = kq.tongVat;
        this.tienGiamTuDiem = kq.tienGiamTuDiem;
        this.tongHoaDon     = kq.tongThanhToan;

        // 2. GUI nhận kết quả từ BUS và Đổ lên màn hình (Chỉ setText)
        if (lblTotalItems != null) lblTotalItems.setText(String.format("Tổng sản phẩm: %d", kq.tongSoLuongSP));
        if (lblSubtotalValue != null) lblSubtotalValue.setText(String.format("%,d", kq.tamTinh).replace(',', '.') + "đ");
        if (lblVatValue != null) lblVatValue.setText("+" + String.format("%,d", kq.tongVat).replace(',', '.') + "đ");
        if (lblDiscountValue != null) lblDiscountValue.setText("-" + String.format("%,d", tienGiamGia).replace(',', '.') + "đ");
        if (lblDungDiemValue != null) lblDungDiemValue.setText("-" + String.format("%,d", kq.tienGiamTuDiem).replace(',', '.') + "đ");
        if (lblTotalPriceValue != null) lblTotalPriceValue.setText(String.format("%,d", kq.tongThanhToan).replace(',', '.') + "đ");
        if (lblQRAmount != null) lblQRAmount.setText("Cần thanh toán: " + String.format("%,d", kq.tongThanhToan).replace(',', '.') + "đ");
        if (lblExactValue != null) lblExactValue.setText(String.format("%,d", kq.tongThanhToan).replace(',', '.') + "đ");

        if (lblDungDiemText != null) {
            int diemThucTeDung = (int)(kq.tienGiamTuDiem / 100L);
            if (isDungDiem) {
                lblDungDiemText.setText(String.format("Dùng %,d điểm", diemThucTeDung) + " (-" + String.format("%,d", kq.tienGiamTuDiem).replace(',', '.') + "đ)");
            } else {
                lblDungDiemText.setText(String.format("Dùng %,d điểm", diemHienTaiKH) + " (-" + String.format("%,d", diemHienTaiKH * 100L).replace(',', '.') + "đ)");
            }
        }

        if ("Chuyển khoản".equals(phuongThuc) && lblQRCode != null) loadQRCodeVCB(lblQRCode); 
        capNhatTongTien();

        if (mainScrollPane != null) {
            final int savedScrollPos = currentScrollPos;
            SwingUtilities.invokeLater(() -> {
                mainScrollPane.revalidate(); mainScrollPane.repaint();
                SwingUtilities.invokeLater(() -> mainScrollPane.getVerticalScrollBar().setValue(savedScrollPos));
            });
        }
    }
    private Object getSafeValue(javax.swing.table.TableModel model, int row, int col) {
        try {
            return model.getValueAt(row, col);
        } catch (ArrayIndexOutOfBoundsException e) {
            return null; // Dòng này bị thiếu cột, trả về null an toàn
        }
    }
    private JPanel createVoucherPanel() {
        JPanel pnlWrapper = new JPanel(new BorderLayout(10, 10));
        pnlWrapper.setBackground(Color.WHITE);
        pnlWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel pnlInput = new JPanel(new BorderLayout(10, 0));
        pnlInput.setOpaque(false);
        
        txtVoucherInput = createStyledTextField("NHẬP MÃ HOẶC CHỌN BÊN DƯỚI...");
        JButton btnApply = new JButton("Áp dụng");
        styleButton(btnApply, Color.decode("#1967D2")); 
        btnApply.setPreferredSize(new Dimension(100, 36));

        pnlInput.add(txtVoucherInput, BorderLayout.CENTER);
        pnlInput.add(btnApply, BorderLayout.EAST);

     // TẠO LAYOUT CHÍNH LÀ DỌC (TỪ TRÊN XUỐNG DƯỚI)
        pnlVoucherTags = new JPanel();
        pnlVoucherTags.setLayout(new BoxLayout(pnlVoucherTags, BoxLayout.Y_AXIS));
        pnlVoucherTags.setOpaque(false);
        
        BUS.BUS_KhuyenMai busKM = new BUS.BUS_KhuyenMai();
        java.util.List<Object[]> dsKM = busKM.layDanhSachKhuyenMaiHienThiTag();
        
        boolean hasVoucher = false;
        if(dsKhuyenMaiCache != null) dsKhuyenMaiCache.clear();

        int count = 0;
        // Tạo hàng ngang đầu tiên
        JPanel currentRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        currentRow.setOpaque(false);
        pnlVoucherTags.add(currentRow);

        for (Object[] row : dsKM) {
            hasVoucher = true;
            String maKM = row[0] != null ? row[0].toString() : "";
            String tenKM = row[1] != null ? row[1].toString() : "";
            String moTa = row[2] != null ? row[2].toString() : "";
            String tenSanPhamYeuCau = row[3] != null ? row[3].toString() : "";
            
            dsKhuyenMaiCache.add(new Object[]{maKM});

            String hienThi = (moTa != null && !moTa.trim().isEmpty()) ? moTa : tenKM;
            if (hienThi == null || hienThi.trim().isEmpty()) {
                hienThi = "Chương trình ưu đãi";
            }

            if (hienThi.equals(tenKM) && !tenSanPhamYeuCau.isEmpty()) {
                hienThi += " (Áp dụng cho " + tenSanPhamYeuCau + ")";
            }

            String labelStr = maKM + " (" + hienThi + ")";

            // LOGIC QUAN TRỌNG: Cứ đủ 5 thẻ thì tạo thêm hàng ngang mới
            if (count == 5) {
                currentRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
                currentRow.setOpaque(false);
                pnlVoucherTags.add(currentRow);
                count = 0;
            }
            currentRow.add(createVoucherTag(labelStr, maKM, txtVoucherInput));
            count++;
        }
        
        if(!hasVoucher) {
            JLabel lblEmpty = new JLabel("<html><i>(Hiện chưa có chương trình khuyến mãi nào)</i></html>");
            lblEmpty.setForeground(Color.GRAY);
            pnlVoucherTags.add(lblEmpty);
        }

        pnlWrapper.add(pnlInput, BorderLayout.NORTH);

        JPanel pnlTagsContainer = new JPanel(new BorderLayout());
        pnlTagsContainer.setOpaque(false);
        pnlTagsContainer.add(pnlVoucherTags, BorderLayout.NORTH); 

        pnlWrapper.add(pnlTagsContainer, BorderLayout.CENTER);

        // NÚT ÁP DỤNG: Đã được làm gọn gàng, đẩy logic sang hàm kiemTraHopLeKhuyenMai
        btnApply.addActionListener(e -> {
            String code = txtVoucherInput.getText().trim();
            if(code.isEmpty() || code.equals("NHẬP MÃ HOẶC CHỌN BÊN DƯỚI...")) return;

            if (kiemTraHopLeKhuyenMai(code)) {
                maKhuyenMaiApDung = code;
                recalculateTotals(); 
                showCustomNotification("THÀNH CÔNG", "Đã áp dụng mã khuyến mãi: " + code, "SUCCESS");
            } else {
                // Nếu sai, tự reset mã và tiền giảm
                maKhuyenMaiApDung = "";
                tienGiamGia = 0;
                recalculateTotals();
            }
        });

        return pnlWrapper;
    }

    private JPanel createSectionPanel(String title, String iconType, JPanel content) {
        return createSectionPanel(title, iconType, content, null); // Gọi hàm bên dưới, mặc định không có nút phụ
    }

    private JPanel createSectionPanel(String title, String iconType, JPanel content, Component extraComponent) {
        JPanel pnl = new JPanel(new BorderLayout(0, 15)); 
        pnl.setBackground(Color.WHITE);
        
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new javax.swing.border.EmptyBorder(5, 10, 5, 10)
        ));
        
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#F1F3F5"))); 
        
        JLabel lblTitle = new JLabel(title);
        if (iconType != null) {
            lblTitle.setIcon(new MenuIcon(iconType)); 
            lblTitle.setIconTextGap(10); 
        }
        
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16)); 
        lblTitle.setForeground(Color.decode("#152A4B")); 
        lblTitle.setBorder(new javax.swing.border.EmptyBorder(0, 0, 10, 0)); 

        pnlHeader.add(lblTitle, BorderLayout.WEST);

        // HIỂN THỊ NÚT BẤM KẾ BÊN TIÊU ĐỀ (GÓC PHẢI)
        if (extraComponent != null) {
            JPanel pnlExtra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            pnlExtra.setOpaque(false);
            pnlExtra.setBorder(new javax.swing.border.EmptyBorder(0, 0, 8, 0)); // Căn lề cho nút không đè gạch dưới
            pnlExtra.add(extraComponent);
            pnlHeader.add(pnlExtra, BorderLayout.EAST);
        }

        pnl.add(pnlHeader, BorderLayout.NORTH);
        pnl.add(content, BorderLayout.CENTER);
        
        return pnl;
    }
    
    private JPanel createDonThuocPanel() {
        JPanel pnlWrapper = new JPanel(new BorderLayout(0, 15));
        pnlWrapper.setBackground(Color.decode("#FEF2F2")); 
        pnlWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#FECACA"), 1, true), 
            new EmptyBorder(15, 20, 15, 20)
        ));

     
        JLabel lblTitle = new JLabel("Thông tin kê đơn (bắt buộc)");
        lblTitle.setIcon(new MenuIcon("WARNING")); 
        lblTitle.setIconTextGap(8);
        lblTitle.setForeground(Color.decode("#DC2626"));
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        pnlWrapper.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlFields = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlFields.setOpaque(false);

        txtBacSi = createStyledTextField("BS. Nguyễn Văn A...");
        txtCoSo = createStyledTextField("BV Bạch Mai...");
        txtChuanDoan = createStyledTextField("Chẩn đoán bệnh...");

        pnlFields.add(wrapFieldWithLabel("Bác sĩ kê đơn *", txtBacSi));
        pnlFields.add(wrapFieldWithLabel("Cơ sở khám *", txtCoSo));
        pnlFields.add(wrapFieldWithLabel("Chẩn đoán *", txtChuanDoan));

        pnlWrapper.add(pnlFields, BorderLayout.CENTER);
        
        pnlWrapper.setVisible(false); 
        return pnlWrapper;
    }

    private JPanel wrapFieldWithLabel(String labelText, JTextField txtField) {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(Color.decode("#4B5563"));
        pnl.add(lbl, BorderLayout.NORTH);
        pnl.add(txtField, BorderLayout.CENTER);
        return pnl;
    }
    
    private JPanel createCustomerPanel() {
        JPanel pnlWrapper = new JPanel(new BorderLayout(0, 4)); 
        pnlWrapper.setBackground(Color.WHITE);
        pnlWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(2, 5, 2, 5)
        ));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlTitle.setBackground(Color.WHITE);
        
        JLabel lblIcon = new JLabel(new MenuIcon("USER")); 
        lblIcon.setForeground(Color.decode("#6C757D"));
        
        JLabel lblTitleText = new JLabel("Tra cứu khách hàng");
        lblTitleText.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitleText.setForeground(Color.decode("#212B36"));
        
        JLabel lblSubTitle = new JLabel("(tuỳ chọn)");
        lblSubTitle.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblSubTitle.setForeground(Color.decode("#9CA3AF"));
        
        pnlTitle.add(lblIcon);
        pnlTitle.add(lblTitleText);
        pnlTitle.add(lblSubTitle);

        JPanel pnlBadgeWrap = new JPanel(new CardLayout());
        pnlBadgeWrap.setOpaque(false);
        
        lblBadgeLe = new JLabel("Khách lẻ", SwingConstants.CENTER);
        lblBadgeLe.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblBadgeLe.setForeground(Color.decode("#4B5563"));
        lblBadgeLe.setBackground(Color.decode("#F3F4F6"));
        lblBadgeLe.setOpaque(true);
        lblBadgeLe.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        lblLinkStatus = new JLabel("Đã liên kết", SwingConstants.CENTER);
        lblLinkStatus.setIcon(new MenuIcon("CORRECT")); 
        lblLinkStatus.setIconTextGap(4); 
        lblLinkStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLinkStatus.setForeground(Color.WHITE); 
        lblLinkStatus.setBackground(Color.decode("#10B981"));
        lblLinkStatus.setOpaque(true);
        lblLinkStatus.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#059669"), 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 10)
        ));
        
        pnlBadgeWrap.add(lblBadgeLe, "LE");
        pnlBadgeWrap.add(lblLinkStatus, "LINKED");
        
        pnlHeader.add(pnlTitle, BorderLayout.WEST);
        pnlHeader.add(pnlBadgeWrap, BorderLayout.EAST);

        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);

        JPanel pnlSearch = new JPanel(new BorderLayout(10, 0));
        pnlSearch.setBackground(Color.WHITE);
        
        txtSearch = createStyledTextField("Nhập SĐT hoặc mã KH để liên kết điểm thưởng...");
        
        // FIX: Di dời khối khởi tạo Popup Gợi ý Khách Hàng lên đúng chỗ
        customerSuggestionPopup = new JPopupMenu();
        customerSuggestionPopup.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8")));

     // --- FIX CHUỘT: BỌC DANH SÁCH VÀO PANEL TRUNG GIAN ĐỂ CHỐNG "NUỐT CLICK" ---
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (customerSuggestionPopup.isVisible() && customerSuggestionPopup.getComponentCount() > 0) {
                        // Do ta đã bọc trong 1 cái rổ (pnlList), nên Component(0) của popup chính là cái rổ
                        Component wrapperPnl = customerSuggestionPopup.getComponent(0);
                        if (wrapperPnl instanceof JPanel && ((JPanel) wrapperPnl).getComponentCount() > 0) {
                            // Lấy tiếp Component(0) bên trong cái rổ (Chính là khách hàng đầu tiên)
                            Component firstItem = ((JPanel) wrapperPnl).getComponent(0);
                            
                            if (firstItem instanceof JPanel) {
                                JPanel targetPnl = (JPanel) firstItem;
                                String id = targetPnl.getName();

                                customerSuggestionPopup.setVisible(false);
                                txtSearch.setText(id);
                                txtSearch.setForeground(Color.BLACK);

                                SwingUtilities.invokeLater(() -> {
                                    for (Component c : txtSearch.getParent().getComponents()) {
                                        if (c instanceof JButton) {
                                            ((JButton) c).doClick();
                                            break;
                                        }
                                    }
                                });
                            }
                        }
                    } else {
                        for (Component c : txtSearch.getParent().getComponents()) {
                            if (c instanceof JButton) { ((JButton) c).doClick(); break; }
                        }
                    }
                    e.consume();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    return;
                }
                
                String text = txtSearch.getText().trim();
                if (text.isEmpty() || text.equals("Nhập SĐT hoặc mã KH để liên kết điểm thưởng...")) {
                    customerSuggestionPopup.setVisible(false);
                    return;
                }

                customerSuggestionPopup.removeAll();
                boolean hasResult = false;

                // TẠO RỔ CHỨA: Bọc các gợi ý vào Panel trung gian để JPopupMenu không ăn mất chuột
                JPanel pnlList = new JPanel();
                pnlList.setLayout(new BoxLayout(pnlList, BoxLayout.Y_AXIS));
                pnlList.setBackground(Color.WHITE);

                Window parentWindow = SwingUtilities.getWindowAncestor(TaoHoaDon.this);
                if (parentWindow instanceof MainDashboard) {
                    DefaultTableModel khModel = ((MainDashboard) parentWindow).getModelKhachHang();
                    if (khModel != null) {
                        int count = 0;
                        for (int i = 0; i < khModel.getRowCount(); i++) {
                            String ma = khModel.getValueAt(i, 0).toString();
                            String ten = khModel.getValueAt(i, 1).toString();
                            String sdt = khModel.getValueAt(i, 2).toString();
                            String diem = khModel.getValueAt(i, 5).toString();

                            if (sdt.startsWith(text) || ten.toLowerCase().contains(text.toLowerCase())) {
                                pnlList.add(createCustomerSuggestionItem(ma, ten, sdt, diem));
                                hasResult = true;
                                count++;
                            }
                            if (count >= 5) break; 
                        }
                    }
                }

                if (hasResult) {
                    customerSuggestionPopup.add(pnlList); // Ném nguyên cái rổ chứa vào Popup
                    customerSuggestionPopup.pack(); // Ép popup tự động co giãn ôm sát cái rổ
                    customerSuggestionPopup.show(txtSearch, 0, txtSearch.getHeight());
                    txtSearch.requestFocus(); 
                } else {
                    customerSuggestionPopup.setVisible(false);
                }
            }
        });

        JButton btnSearch = new JButton("Tra cứu");
        btnSearch.setIcon(new MenuIcon("SEARCH")); 
        btnSearch.setIconTextGap(6);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.setBackground(Color.decode("#1967D2"));
        btnSearch.setForeground(Color.WHITE); 
        btnSearch.setFocusPainted(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        txtSearch.addActionListener(e -> btnSearch.doClick());
        
        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        pnlSearch.add(btnSearch, BorderLayout.EAST);

        JPanel pnlWarn = new JPanel(new BorderLayout(10, 0));
        pnlWarn.setBackground(Color.WHITE);
        pnlWarn.setBorder(new EmptyBorder(8, 0, 0, 0));
        
        JLabel lblWarning = new JLabel("Không tìm thấy khách hàng. Bạn có muốn tạo mới?");
        lblWarning.setIcon(new MenuIcon("WARNING"));
        lblWarning.setIconTextGap(8);
        lblWarning.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblWarning.setForeground(Color.decode("#DC2626")); 
        lblWarning.setBackground(Color.decode("#FFFBEB")); 
        lblWarning.setOpaque(true);
        lblWarning.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#FCA5A5"), 1, true), 
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        
        JButton btnAddCustomer = new JButton("Thêm KH mới");
        btnAddCustomer.setIcon(new MenuIcon("USER_ADD"));
        btnAddCustomer.setIconTextGap(6);
        btnAddCustomer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddCustomer.setBackground(Color.decode("#E11D48")); 
        btnAddCustomer.setForeground(Color.WHITE);
        btnAddCustomer.setFocusPainted(false);
        btnAddCustomer.setBorderPainted(false);
        btnAddCustomer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        pnlWarn.add(lblWarning, BorderLayout.CENTER);
        pnlWarn.add(btnAddCustomer, BorderLayout.EAST);
        pnlWarn.setVisible(false);

        pnlInputFields = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlInputFields.setBackground(Color.WHITE);
        pnlInputFields.setBorder(new EmptyBorder(8, 0, 0, 0));
        txtName = createStyledTextField("Tên khách (bỏ trống = Khách lẻ)");
        txtPhone = createStyledTextField("Số điện thoại (tuỳ chọn)");
        pnlInputFields.add(txtName);
        pnlInputFields.add(txtPhone);

        pnlLinkedCustomer = new JPanel(new BorderLayout(15, 0));
        pnlLinkedCustomer.setBackground(Color.decode("#F0F9FF")); 
        pnlLinkedCustomer.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(8, 0, 0, 0),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#BAE6FD"), 1, true), 
                new EmptyBorder(10, 15, 10, 15)
            )
        ));
        
        lblLinkedAvatar = new JLabel("N", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#152A4B"));
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        lblLinkedAvatar.setForeground(Color.WHITE);
        lblLinkedAvatar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLinkedAvatar.setPreferredSize(new Dimension(45, 45));
        
        JPanel pnlNameInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlNameInfo.setOpaque(false);
        lblLinkedName = new JLabel("Nguyễn Văn An");
        lblLinkedName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblLinkedName.setForeground(Color.BLACK);
        lblLinkedSub = new JLabel("KH2024-0001 • 0910000000");
        lblLinkedSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLinkedSub.setForeground(Color.GRAY);
        pnlNameInfo.add(lblLinkedName);
        pnlNameInfo.add(lblLinkedSub);
        
     // =========================================================
        // --- FIX: GIAO DIỆN ĐIỂM SỐ RỘNG RÃI & CÓ ICON CORRECT ---
        // =========================================================
        JPanel pnlPointsLayout = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlPointsLayout.setOpaque(false);

        lblLinkedPoints = new JLabel("0 điểm");
        lblLinkedPoints.setIcon(new MenuIcon("CORRECT")); 
        lblLinkedPoints.setIconTextGap(6); 
        lblLinkedPoints.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        lblLinkedPoints.setForeground(Color.decode("#9333EA")); 
        lblLinkedPoints.setBackground(Color.decode("#F3E8FF")); 
        lblLinkedPoints.setOpaque(true);
        lblLinkedPoints.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E9D5FF"), 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12) 
        ));
        
        lblLinkedMoney = new JLabel("≈ 0đ");
        lblLinkedMoney.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
        lblLinkedMoney.setForeground(Color.decode("#4B5563")); 
        lblLinkedMoney.setBackground(Color.decode("#F3F4F6")); 
        lblLinkedMoney.setOpaque(true);
        lblLinkedMoney.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E5E7EB"), 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12) 
        ));

        pnlPointsLayout.add(lblLinkedPoints);
        pnlPointsLayout.add(lblLinkedMoney);
        
        JButton btnUnlink = new JButton(); 
        btnUnlink.setIcon(new MenuIcon("CLOSE")); 
        btnUnlink.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnUnlink.setForeground(Color.GRAY); 
        btnUnlink.setContentAreaFilled(false);
        btnUnlink.setBorderPainted(false);
        btnUnlink.setFocusPainted(false);
        btnUnlink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnUnlink.addActionListener(e -> {
            isCustomerLinked = false;
            linkedTenKH = ""; linkedSdtKH = "";
            diemHienTaiKH = 0;
            isDungDiem = false;
            toggleDungDiem.setOn(false);
            pnlDungDiem.setVisible(false);
            recalculateTotals();

            if (txtSearch != null) {
                txtSearch.setText("Nhập SĐT hoặc mã KH để liên kết điểm thưởng...");
                txtSearch.setForeground(Color.GRAY);
            }
            if (txtName != null) {
                txtName.setText("Tên khách (bỏ trống = Khách lẻ)");
                txtName.setForeground(Color.GRAY);
            }
            if (txtPhone != null) {
                txtPhone.setText("Số điện thoại (tuỳ chọn)");
                txtPhone.setForeground(Color.GRAY);
            }

            pnlLinkedCustomer.setVisible(false);
            pnlInputFields.setVisible(true);
            CardLayout cl = (CardLayout)(pnlBadgeWrap.getLayout());
            cl.show(pnlBadgeWrap, "LE");
            
            if (customerSuggestionPopup != null) {
                customerSuggestionPopup.setVisible(false);
            }
            
            pnlBody.revalidate(); pnlBody.repaint();
        });
        
        JPanel pnlRightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlRightInfo.setOpaque(false);
        pnlRightInfo.add(pnlPointsLayout);
        pnlRightInfo.add(btnUnlink);
        // =========================================================
        
        
        
        pnlLinkedCustomer.add(lblLinkedAvatar, BorderLayout.WEST);
        pnlLinkedCustomer.add(pnlNameInfo, BorderLayout.CENTER);
        pnlLinkedCustomer.add(pnlRightInfo, BorderLayout.EAST);
        pnlLinkedCustomer.setVisible(false); 

        pnlBody.add(pnlSearch);
        pnlBody.add(pnlWarn);
        pnlBody.add(pnlLinkedCustomer);

     // --- XỬ LÝ KHI BẤM NÚT TRA CỨU HOẶC ẤN ENTER ---
        btnSearch.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();
            
            // 1. NẾU Ô TRỐNG HOẶC CHƯA NHẬP GÌ -> Hiện thẳng bảng cảnh báo đỏ
            if (keyword.isEmpty() || keyword.equals("Nhập SĐT hoặc mã KH để liên kết điểm thưởng...")) {
                if (customerSuggestionPopup != null) customerSuggestionPopup.setVisible(false);
                pnlWarn.setVisible(true);
                pnlLinkedCustomer.setVisible(false);
                pnlInputFields.setVisible(true);
                CardLayout cl = (CardLayout)(pnlBadgeWrap.getLayout());
                cl.show(pnlBadgeWrap, "LE");
                pnlBody.revalidate(); pnlBody.repaint();
                return;
            }

            boolean found = false;
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            if (parentWindow instanceof MainDashboard) {
                DefaultTableModel khModel = ((MainDashboard) parentWindow).getModelKhachHang();
                if (khModel != null) {
                    for (int i = 0; i < khModel.getRowCount(); i++) {
                        String maKH = khModel.getValueAt(i, 0).toString();  
                        String tenKH = khModel.getValueAt(i, 1).toString(); 
                        String sdtKH = khModel.getValueAt(i, 2).toString(); 
                        String diemKH = khModel.getValueAt(i, 5).toString(); 
                        
                        if (keyword.equalsIgnoreCase(maKH) || keyword.equals(sdtKH)) {
                            isCustomerLinked = true;
                            linkedTenKH = tenKH;
                            linkedSdtKH = sdtKH;
                            
                            lblLinkedAvatar.setText(tenKH.substring(0, 1).toUpperCase());
                            lblLinkedName.setText(tenKH);
                            lblLinkedSub.setText(maKH + " • " + sdtKH);
                            lblLinkedPoints.setText(diemKH + " điểm");
                            
                            try {
                                diemHienTaiKH = Integer.parseInt(diemKH.replace(".", "").replace(",", ""));
                                long tienQuyDoi = diemHienTaiKH * 100L;
                                lblLinkedMoney.setText("≈ " + String.format("%,d", tienQuyDoi).replace(',', '.') + "đ");

                                if (diemHienTaiKH > 0) {
                                    lblDungDiemText.setText(String.format("Dùng %,d điểm", diemHienTaiKH) + " (-" + String.format("%,d", tienQuyDoi).replace(',', '.') + "đ)");
                                    pnlDungDiem.setVisible(true);
                                } else {
                                    pnlDungDiem.setVisible(false);
                                }
                            } catch (Exception ex) {
                                diemHienTaiKH = 0;
                                lblLinkedMoney.setText("≈ 0đ");
                                pnlDungDiem.setVisible(false);
                            }
                            
                            toggleDungDiem.setOn(false);
                            isDungDiem = false;
                            recalculateTotals();

                            found = true;
                            break; 
                        }
                    }
                }
            }

            if (found) {
                // TÌM THẤY -> Ẩn cảnh báo đỏ, hiện thông tin khách
                pnlWarn.setVisible(false);
                pnlInputFields.setVisible(false);     
                pnlLinkedCustomer.setVisible(true);   
                CardLayout cl = (CardLayout)(pnlBadgeWrap.getLayout());
                cl.show(pnlBadgeWrap, "LINKED");      
            } else {
                // KHÔNG TÌM THẤY -> Ẩn Popup xổ xuống, bung bảng cảnh báo đỏ bên dưới
                if (customerSuggestionPopup != null) customerSuggestionPopup.setVisible(false);
                pnlWarn.setVisible(true);
                pnlLinkedCustomer.setVisible(false);
                pnlInputFields.setVisible(true);
                CardLayout cl = (CardLayout)(pnlBadgeWrap.getLayout());
                cl.show(pnlBadgeWrap, "LE");
            }
            pnlBody.revalidate(); pnlBody.repaint();
        });

        btnAddCustomer.addActionListener(e -> {
            Window owner = this.getOwner(); 
            if (owner instanceof MainDashboard) {
                String searchedText = txtSearch.getText().trim();
                if (!searchedText.isEmpty() && !searchedText.equals("Nhập SĐT hoặc mã KH để liên kết điểm thưởng...")) {
                    ManHinhBanHang.pendingPhoneToLink = searchedText; 
                }
                String phoneInput = txtSearch.getText().trim();
                if (phoneInput.isEmpty() || phoneInput.contains("Nhập SĐT")) {
                    phoneInput = txtPhone.getText().trim();
                }
                if (!phoneInput.contains("Số điện thoại") && !phoneInput.isEmpty()) {
                    ManHinhBanHang.pendingPhoneToLink = phoneInput; 
                }

                luuNhapHoaDon(true); 
                
                if (this.maHDDangSua != null && !this.maHDDangSua.isEmpty()) {
                    ManHinhBanHang.pendingDraftIdToOpen = this.maHDDangSua; 
                } else {
                    ManHinhBanHang.pendingDraftIdToOpen = "NEW_INVOICE"; 
                }
                
                this.dispose();
                
                ((MainDashboard) owner).chuyenSangTabKhachHang(true); 
                
                showCustomNotification("HƯỚNG DẪN", 
                    "Đã chuyển sang màn hình Khách Hàng.\n" +
                    "Tạo xong khách hàng, hãy bấm lại vào tab Bán Hàng, hệ thống sẽ tự động mở lại hóa đơn này!", 
                    "SUCCESS");
            }
        });

        pnlWrapper.add(pnlHeader, BorderLayout.NORTH);
        pnlWrapper.add(pnlBody, BorderLayout.CENTER);

        return pnlWrapper;
    }
    @Override
    public void setVisible(boolean b) {
        if (b) {
            SwingUtilities.invokeLater(() -> {
                lamMoiKhuyenMai();
                kiemTraVaKhoiPhucTrangThaiKH(); // Gọi hàm kiểm tra bộ nhớ
            });
        }
        super.setVisible(b);
    }
    private void kiemTraVaKhoiPhucTrangThaiKH() {
        String phoneToLink = ManHinhBanHang.pendingPhoneToLink;
        String nameToLink = ManHinhBanHang.pendingNameToLink;

        // Chạy nếu 1 trong 2 biến (SĐT hoặc Tên) có dữ liệu
        if ((phoneToLink != null && !phoneToLink.isEmpty()) || (nameToLink != null && !nameToLink.isEmpty())) {
            
            // Xóa trí nhớ ngay sau khi lấy ra để tránh bị lặp lại ở hóa đơn sau
            ManHinhBanHang.pendingPhoneToLink = null; 
            ManHinhBanHang.pendingNameToLink = null;  

            // Ưu tiên dùng số điện thoại để tra cứu, nếu SĐT trống thì lấy Tên
            String searchKeyword = (phoneToLink != null && !phoneToLink.isEmpty()) ? phoneToLink : nameToLink;

            // Khởi tạo BUS (Bạn có thể đưa dòng này lên khai báo ở cấp class nếu muốn dùng chung)
            BUS.BUS_KhachHang busKhachHang = new BUS.BUS_KhachHang();
            
            // Tìm kiếm khách hàng thông qua BUS thay vì gọi SQL trực tiếp
            java.util.List<Entity.KhachHang> dsKhachHang = busKhachHang.traCuuKhachHang(searchKeyword);

            try {
                // Nếu tìm thấy ít nhất 1 khách hàng khớp với từ khóa
                if (dsKhachHang != null && !dsKhachHang.isEmpty()) {
                    
                    // Lấy khách hàng đầu tiên trong danh sách kết quả
                    Entity.KhachHang kh = dsKhachHang.get(0);
                    
                    String maKH = kh.getId();
                    String tenKH = kh.getHoVaTen();
                    String sdtKH = kh.getSdt();
                    int diemKH = kh.getDiemTichLuy();

                    // 1. Cập nhật biến Logic
                    isCustomerLinked = true;
                    linkedTenKH = tenKH;
                    linkedSdtKH = sdtKH;
                    diemHienTaiKH = diemKH;
                    
                    // 2. Đổ dữ liệu lên giao diện
                    lblLinkedAvatar.setText(tenKH.substring(0, 1).toUpperCase());
                    lblLinkedName.setText(tenKH);
                    lblLinkedSub.setText(maKH + " • " + sdtKH);
                    lblLinkedPoints.setText(diemKH + " điểm");
                    
                    long tienQuyDoi = diemHienTaiKH * 100L;
                    lblLinkedMoney.setText("≈ " + String.format("%,d", tienQuyDoi).replace(',', '.') + "đ");

                    if (diemHienTaiKH > 0) {
                        lblDungDiemText.setText(String.format("Dùng %,d điểm", diemHienTaiKH) + " (-" + String.format("%,d", tienQuyDoi).replace(',', '.') + "đ)");
                        pnlDungDiem.setVisible(true);
                    } else {
                        pnlDungDiem.setVisible(false);
                    }
                    
                    toggleDungDiem.setOn(false);
                    isDungDiem = false;
                    
                    // 3. Đổi giao diện sang "Đã liên kết"
                    pnlInputFields.setVisible(false);
                    pnlLinkedCustomer.setVisible(true);
                    
                    if(lblBadgeLe != null && lblBadgeLe.getParent() != null) {
                        java.awt.CardLayout cl = (java.awt.CardLayout)(lblBadgeLe.getParent().getLayout());
                        cl.show(lblBadgeLe.getParent(), "LINKED");
                    }
                    
                    recalculateTotals();
                } else {
                    // Nếu user đổi ý không tạo nữa và quay lại, vẫn đổ thông tin cũ vào ô
                    if (nameToLink != null && !nameToLink.isEmpty()) {
                        txtName.setText(nameToLink);
                        txtName.setForeground(java.awt.Color.BLACK);
                    }
                    
                    if (phoneToLink != null && !phoneToLink.isEmpty()) {
                        txtPhone.setText(phoneToLink);
                        txtPhone.setForeground(java.awt.Color.BLACK);
                        txtSearch.setText(phoneToLink);
                        txtSearch.setForeground(java.awt.Color.BLACK);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    private JTextField createStyledTextField(String placeholder) {
        JTextField txt = new JTextField(placeholder);
        txt.setPreferredSize(new Dimension(0, 28)); 
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setForeground(Color.GRAY);
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        
        txt.addFocusListener(new java.awt.event.FocusAdapter() {
        	public void focusGained(java.awt.event.FocusEvent e) {
                // ĐÃ SỬA: Bỏ vế điều kiện ngược để không bị nuốt chữ cái của khách hàng
                if (txt.getText().contains(placeholder)) {
                    txt.setText(""); 
                    txt.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txt.getText().trim().isEmpty()) {
                    txt.setForeground(Color.GRAY); 
                    txt.setText(placeholder);
                }
            }
        });
        return txt;
    }

    private JPanel createProductPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 15));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel pnlSearchWrapper = new JPanel(new BorderLayout(10, 0));
        pnlSearchWrapper.setBackground(Color.WHITE);
        pnlSearchWrapper.setPreferredSize(new Dimension(0, 42));
        pnlSearchWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#94A3B8"), 1, true),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));

        JLabel lblSearchIcon = new JLabel(new MenuIcon("SEARCH"));
        lblSearchIcon.setForeground(Color.GRAY);
        pnlSearchWrapper.add(lblSearchIcon, BorderLayout.WEST);

        String placeholderText = "Tìm tên sản phẩm hoặc mã để thêm vào đơn hàng (Ấn Enter để thêm)...";
        JTextField txtSearchProduct = new JTextField(placeholderText);
        txtSearchProduct.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearchProduct.setForeground(Color.GRAY);
        txtSearchProduct.setBorder(null); 
        pnlSearchWrapper.add(txtSearchProduct, BorderLayout.CENTER);

        txtSearchProduct.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                // Đổi .equals thành .contains để bắt lỗi an toàn hơn
                if (txtSearchProduct.getText().contains("Tìm tên sản phẩm")) {
                    txtSearchProduct.setText("");
                    txtSearchProduct.setForeground(Color.BLACK);
                    lblSearchIcon.setForeground(Color.decode("#1967D2")); 
                    pnlSearchWrapper.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode("#1967D2"), 1, true),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)
                    ));
                }
            }
            // ... (giữ nguyên focusLost)
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtSearchProduct.getText().trim().isEmpty()) {
                    txtSearchProduct.setForeground(Color.GRAY);
                    txtSearchProduct.setText(placeholderText);
                    lblSearchIcon.setForeground(Color.GRAY); 
                    pnlSearchWrapper.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode("#94A3B8"), 1, true),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)
                    ));
                }
            }
        });

        pnl.add(pnlSearchWrapper, BorderLayout.NORTH);

     // Bổ sung cột thứ 10 (_BaseQty) làm cột ẩn để lưu số lượng của 1 liều
        String[] cols = {"Sản phẩm", "ĐVT", "Lô / HSD", "SL", "Đơn giá", "KM%", "Thành tiền", "", "_VAT", "_BaseQty"};
        productModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                String tenSP = getValueAt(r, 0).toString();
                if (tenSP.startsWith("[QUÀ TẶNG]")) return c == 7; 
                if (tenSP.startsWith("↳ ")) return false; // KHÓA: Thuốc con trong liều không được sửa hay xóa lẻ
                if (tenSP.startsWith("[LIỀU]")) return c == 3 || c == 7; // Tiêu đề Liều chỉ cho sửa Số lượng và Xóa
                return c == 3 || c == 7 || c == 1; // Hàng thuốc lẻ bình thường
            }
        };
        
        productModel.addTableModelListener(e -> {
            if (isTableUpdating) return; 

            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                
                // --- SỬA: Nếu sửa Số Lượng (Cột 3) hoặc đổi ĐVT (Cột 1) ---
                if (row >= 0 && (col == 3 || col == 1)) {
                    SwingUtilities.invokeLater(() -> {
                        isTableUpdating = true; 
                        try {
                            // --- BƯỚC 1: XỬ LÝ KHI ĐỔI ĐƠN VỊ TÍNH (Cột 1) ---
                            if (col == 1) {
                                String tenSP = productModel.getValueAt(row, 0).toString().trim();
                                String donViMoi = productModel.getValueAt(row, 1).toString().trim();
                                
                                // KIỂM TRA CHỐNG TRÙNG SAU KHI ĐỔI ĐVT
                                boolean biTrung = false;
                                for (int i = 0; i < productModel.getRowCount(); i++) {
                                    if (i != row && 
                                        productModel.getValueAt(i, 0).toString().equals(tenSP) && 
                                        productModel.getValueAt(i, 1).toString().equals(donViMoi)) {
                                        biTrung = true;
                                        break;
                                    }
                                }
                                
                                if (biTrung) {
                                    showCustomNotification("TRÙNG SẢN PHẨM", "Đơn vị tính này đã có trong giỏ hàng. Vui lòng cộng dồn số lượng ở dòng bên dưới!", "WARNING");
                                    productModel.removeRow(row);
                                    recalculateTotals();
                                    return;
                                }

                                double giaBanMoi = 0;
                                try {
                                    // [FIX]: SỬ DỤNG BUS THAY CHO CÂU LỆNH SQL
                                    BUS.BUS_DonViDoLuong busDV = new BUS.BUS_DonViDoLuong();
                                    BUS.BUS_SanPham busSP = new BUS.BUS_SanPham();

                                    // 1. Kiểm tra xem có phải đơn vị quy đổi không
                                    java.util.List<Entity.DonViDoLuong> listDV = busDV.getDSTheoTenSP(tenSP);
                                    if (listDV != null) {
                                        for (Entity.DonViDoLuong dv : listDV) {
                                            if (dv.getTen().equalsIgnoreCase(donViMoi)) {
                                                giaBanMoi = dv.getGia();
                                                break;
                                            }
                                        }
                                    }

                                    // 2. Nếu không tìm thấy trong đơn vị quy đổi -> là đơn vị cơ bản -> Lấy giá gốc
                                    if (giaBanMoi <= 0) {
                                        java.util.List<Entity.SanPham> listSP = busSP.traCuuSanPham(tenSP);
                                        if (listSP != null) {
                                            for (Entity.SanPham sp : listSP) {
                                                if (sp.getTen().equalsIgnoreCase(tenSP)) {
                                                    giaBanMoi = sp.getGiaBan();
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception ex) {
                                    System.out.println("Lỗi truy vấn giá qua BUS: " + ex.getMessage());
                                }
                                
                                if (giaBanMoi > 0) {
                                    // --- SỬA: Ghi Đơn giá mới vào Cột 4 ---
                                    productModel.setValueAt(String.format("%,d", (long)giaBanMoi).replace(',', '.') + "đ", row, 4);
                                }
                            }
                            String tenSPSua = productModel.getValueAt(row, 0).toString();
                            if (col == 3 && tenSPSua.startsWith("[LIỀU]")) {
                                int newDays = 1;
                                try { newDays = Integer.parseInt(productModel.getValueAt(row, 3).toString().trim()); } catch (Exception ex){}
                                
                                long totalComboPrice = 0;
                                int childIndex = row + 1;
                                
                                // Chạy vòng lặp quét các dòng con bên dưới nó
                                while (childIndex < productModel.getRowCount() && productModel.getValueAt(childIndex, 0).toString().startsWith("↳ ")) {
                                    int baseQty = Integer.parseInt(productModel.getValueAt(childIndex, 9).toString()); // Cột ẩn 9 lưu gốc
                                    int newQty = baseQty * newDays;
                                    productModel.setValueAt(String.valueOf(newQty), childIndex, 3);
                                    
                                    long dgCon = Long.parseLong(productModel.getValueAt(childIndex, 4).toString().replaceAll("[^0-9]", ""));
                                    long ttCon = dgCon * newQty;
                                    productModel.setValueAt(String.format("%,d", ttCon).replace(',', '.') + "đ", childIndex, 6);
                                    
                                    totalComboPrice += ttCon;
                                    childIndex++;
                                }
                                // Gán lại tổng tiền cho dòng tiêu đề Liều
                                productModel.setValueAt(String.format("%,d", totalComboPrice).replace(',', '.') + "đ", row, 6);
                                recalculateTotals();
                                isTableUpdating = false;
                                return; // Dừng lại, không chạy đoạn xử lý thuốc lẻ bên dưới nữa
                            }
                            // --- BƯỚC 2: XỬ LÝ SỐ LƯỢNG VÀ THÀNH TIỀN ---
                            // --- SỬA: Lấy Số lượng từ Cột 3 ---
                            String slStr = productModel.getValueAt(row, 3).toString().trim();
                            int sl = 1;
                            try {
                                sl = Integer.parseInt(slStr);
                                if (sl <= 0) sl = 1; 
                            } catch (Exception ex) { sl = 1; }

                            // --- SỬA: Cập nhật lại Số lượng vào Cột 3 nếu nhập sai ---
                            if (col == 3 && !slStr.equals(String.valueOf(sl))) {
                                productModel.setValueAt(String.valueOf(sl), row, 3); 
                            }

                            // --- SỬA: Lấy Đơn giá từ Cột 4 ---
                            String giaStr = productModel.getValueAt(row, 4).toString().replaceAll("[^0-9]", "");
                            long donGia = 0;
                            try { donGia = Long.parseLong(giaStr); } catch (Exception ex) {}

                            // --- SỬA: LẤY KM% từ Cột 5 (cột này giờ là KM%, không còn là VAT%) ---
                            double kmPercent = 0;
                            try {
                                String kmStr = productModel.getValueAt(row, 5).toString().replace("%", "").trim();
                                kmPercent = Double.parseDouble(kmStr) / 100.0;
                            } catch (Exception ex) {}

                            // --- SỬA: Thành tiền = sau KM, chưa VAT (VAT tính ở tổng hóa đơn) ---
                            long thanhTien = Math.round(sl * donGia * (1.0 - kmPercent));
                            productModel.setValueAt(String.format("%,d", thanhTien).replace(',', '.') + "đ", row, 6);
                            
                            recalculateTotals(); 
                            
                        } catch (Exception ex) {
                             System.out.println("Lỗi tính toán bảng Hóa đơn: " + ex.getMessage());
                        } finally {
                            isTableUpdating = false; 
                        }
                    });
                }
            }
        });

        // ====================================================================
        // 2. KHỞI TẠO BẢNG VỚI COMBOBOX ĐỘNG THEO DẠNG THUỐC (CỘT 1)
        // ====================================================================
        JTable tbl = new JTable(productModel) {
            
            @Override
            public void scrollRectToVisible(Rectangle aRect) {
                if (isTableUpdating) return; 
                super.scrollRectToVisible(aRect);
            }

            @Override
            public Dimension getPreferredScrollableViewportSize() {
                int tableHeight = getRowCount() * getRowHeight();
                return new Dimension(getPreferredSize().width, tableHeight);
            }

            @Override
            public javax.swing.table.TableCellEditor getCellEditor(int row, int column) {
                if (column == 1) { // Rơi vào đúng Cột Đơn Vị Tính
                    String dvtHienTai = "";
                    Object val = getValueAt(row, 1);
                    if (val != null) dvtHienTai = val.toString().trim();
                    
                    String tenSP = "";
                    Object valTenSP = getValueAt(row, 0);
                    if (valTenSP != null) tenSP = valTenSP.toString().trim();
                    
                    JComboBox<String> cbDVT = new JComboBox<>();
                    // FIX: Đổi font từ 14 xuống 12, bỏ BOLD nếu có để chữ thanh mảnh hơn
                    cbDVT.setFont(new Font("Segoe UI", Font.PLAIN, 12)); 
                    cbDVT.setBackground(Color.WHITE);
                    
                    // --- [FIX]: LOAD ĐVT CHUẨN TỪ BUS ---
                    try {
                        BUS.BUS_DonViDoLuong busDV = new BUS.BUS_DonViDoLuong();
                        java.util.List<Entity.DonViDoLuong> dsDV = busDV.getDSTheoTenSP(tenSP);
                        
                        if (dsDV != null) {
                            for (Entity.DonViDoLuong dv : dsDV) {
                                cbDVT.addItem(dv.getTen());
                            }
                        }
                        
                        // Đảm bảo đơn vị hiện hành (thường là đơn vị cơ bản) luôn tồn tại trong Combobox
                        boolean hasHienTai = false;
                        for (int i = 0; i < cbDVT.getItemCount(); i++) {
                            if (cbDVT.getItemAt(i).equalsIgnoreCase(dvtHienTai)) {
                                hasHienTai = true; 
                                break;
                            }
                        }
                        if (!hasHienTai && !dvtHienTai.isEmpty()) {
                            cbDVT.addItem(dvtHienTai);
                        }
                    } catch (Exception ex) {
                        System.out.println("Lỗi tải ĐVT động qua BUS: " + ex.getMessage());
                        if (!dvtHienTai.isEmpty()) cbDVT.addItem(dvtHienTai);
                    }
                    
                    // Đặt đơn vị hiện tại làm mặc định được chọn
                    cbDVT.setSelectedItem(dvtHienTai);

                    return new DefaultCellEditor(cbDVT);
                }
                return super.getCellEditor(row, column);
            }
        };
        
        tbl.setRowHeight(45);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.setShowGrid(false); 
        tbl.setShowHorizontalLines(true); 
        tbl.setGridColor(Color.decode("#F1F3F5"));

        tbl.getTableHeader().setBackground(Color.decode("#D9EAF7")); 
        tbl.getTableHeader().setForeground(Color.decode("#1E293B"));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tbl.getTableHeader().setPreferredSize(new Dimension(0, 30));
        tbl.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        tbl.getColumnModel().getColumn(0).setPreferredWidth(170); // Sản phẩm (Giảm bớt để nhường chỗ)
        tbl.getColumnModel().getColumn(1).setPreferredWidth(50);  // ĐVT
        tbl.getColumnModel().getColumn(2).setPreferredWidth(195); // Lô / HSD (Tăng từ 120 lên 195 để hiện đủ ngày)
        tbl.getColumnModel().getColumn(3).setPreferredWidth(50);  // SL
        tbl.getColumnModel().getColumn(4).setPreferredWidth(90);  // Đơn giá
        tbl.getColumnModel().getColumn(5).setPreferredWidth(45);  // KM%
        tbl.getColumnModel().getColumn(6).setPreferredWidth(95);  // Thành tiền
        tbl.getColumnModel().getColumn(7).setPreferredWidth(40);  // Thùng rác
        // Cột 8 (_VAT) là cột ẩn – lưu VAT% gốc để dùng khi lưu DB
        tbl.getColumnModel().getColumn(8).setMinWidth(0);
        tbl.getColumnModel().getColumn(8).setMaxWidth(0);
        tbl.getColumnModel().getColumn(8).setWidth(0);
        tbl.getColumnModel().getColumn(9).setMinWidth(0);
        tbl.getColumnModel().getColumn(9).setMaxWidth(0);
        tbl.getColumnModel().getColumn(9).setWidth(0);
        tbl.getColumnModel().getColumn(8).setResizable(false);
        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbl.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String text = (value != null) ? value.toString() : "";
                
                if (text.startsWith("CHILD_ITEM ")) {
                    lbl.setIcon(new MenuIcon("ARROW_SUB", 16, Color.GRAY)); // Dùng icon mũi tên đã vẽ
                    String tenThuoc = text.replace("CHILD_ITEM ", ""); 
                    String htmlText = "<html><div style='padding-top: 2px;'>"
                                    + "<span style='font-family: Segoe UI; font-size: 12px; color: #111827;'>" + tenThuoc + "</span><br>"
                                    + "<span style='font-family: Segoe UI; font-size: 10px; font-style: italic; color: #6B7280;'>(Thuốc liều mẫu)</span>"
                                    + "</div></html>";
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    lbl.setText(htmlText);
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0)); // Thụt lề vào trong
                    
                } else if (text.startsWith("[LIỀU]")) {
                    lbl.setIcon(null); 
                    lbl.setText(text); 
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 13)); 
                    lbl.setForeground(Color.decode("#3B82F6")); 
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                    
                } else if (text.startsWith("[QUÀ TẶNG]")) {
                    lbl.setIcon(null);
                    String tenQua = text.replace("[QUÀ TẶNG]", "").trim();
                    
                    // [FIX] Bọc thêm thẻ <nobr> ở hai đầu để ép chữ luôn nằm trên 1 dòng duy nhất
                    String htmlText = "<html><div style='padding-top: 2px;'><nobr>"
                                    + "<span style='font-family: Segoe UI; font-size: 11px; font-weight: bold; color: #E11D48;'>[QUÀ TẶNG] </span>"
                                    + "<span style='font-family: Segoe UI; font-size: 12px; color: #4B5563;'>" + tenQua + "</span>"
                                    + "</nobr></div></html>";
                    lbl.setText(htmlText);
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); // Thụt lề nhẹ vào 10px cho dễ nhìn

                } else {
                    lbl.setIcon(null);
                    lbl.setText("<html><span style='font-family: Segoe UI; font-size: 14px; color: #000000;'>" + text + "</span></html>");
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    lbl.setForeground(Color.BLACK);
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                }
                return lbl;
            }
        });
        // --- BỔ SUNG RENDERER TẠO THẺ BO GÓC XANH CHO CỘT LÔ/HSD ---
        tbl.getColumnModel().getColumn(2).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel pnl = new JPanel(new GridBagLayout()); 
                pnl.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                String text = value != null ? value.toString().trim() : "";
                
                if (!text.isEmpty()) {
                    JLabel lbl = new JLabel(text);
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    lbl.setForeground(Color.decode("#059669")); // Xanh ngọc lục bảo
                    lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode("#34D399"), 1, true),
                        BorderFactory.createEmptyBorder(2, 8, 2, 8) // Bo lề cho viên thuốc phình ra
                    ));
                    pnl.add(lbl);
                }
                return pnl;
            }
        });
       
     // --- COPY ĐOẠN NÀY ĐÈ LÊN CÁC RENDERER CỘT 4, 5, 6, 7 CŨ ---
        javax.swing.table.DefaultTableCellRenderer hideChildRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int r, int c) {
                String tenSP = table.getValueAt(r, 0).toString();
                // NẾU LÀ THUỐC CON -> ẨN HOÀN TOÀN NỘI DUNG (GIỐNG FIGMA)
                if (tenSP.startsWith("↳ ")) {
                    JLabel lblEmpty = new JLabel("");
                    lblEmpty.setOpaque(true);
                    lblEmpty.setBackground(isSel ? table.getSelectionBackground() : Color.WHITE);
                    return lblEmpty;
                }
                
                // NẾU LÀ DÒNG BÌNH THƯỜNG HOẶC TIÊU ĐỀ LIỀU -> HIỂN THỊ CHỮ
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSel, hasFocus, r, c);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setHorizontalAlignment(c == 7 ? JLabel.CENTER : JLabel.RIGHT);
                
                if (c == 6) lbl.setForeground(Color.decode("#DC2626")); // Thành tiền màu Đỏ
                else if (c == 7) { // Thùng rác
                    lbl.setText(""); lbl.setIcon(new MenuIcon("TRASH")); lbl.setForeground(Color.decode("#EF4444"));
                }
                else lbl.setForeground(Color.decode("#111827")); // Giá, KM% màu Đen
                
                return lbl;
            }
        };

        tbl.getColumnModel().getColumn(4).setCellRenderer(hideChildRenderer);
        tbl.getColumnModel().getColumn(5).setCellRenderer(hideChildRenderer);
        tbl.getColumnModel().getColumn(6).setCellRenderer(hideChildRenderer);
        tbl.getColumnModel().getColumn(7).setCellRenderer(hideChildRenderer);
        tbl.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        
        tbl.setSelectionBackground(Color.decode("#E0F2FE"));
        
        
        
        class SpinnerCellEditor extends javax.swing.AbstractCellEditor implements javax.swing.table.TableCellEditor {
            private JSpinner spinner;
            private boolean isSettingValue = false; 

            public SpinnerCellEditor() {
                spinner = new JSpinner(new SpinnerNumberModel(1, 1, 999999, 1));
                JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
                editor.getTextField().setHorizontalAlignment(JTextField.CENTER);
                editor.getTextField().setFont(new Font("Segoe UI", Font.BOLD, 12));
                
                spinner.setBorder(BorderFactory.createLineBorder(Color.decode("#1967D2"), 2));
                editor.setBorder(BorderFactory.createEmptyBorder());
                editor.getTextField().setBorder(BorderFactory.createEmptyBorder());
                
                editor.getTextField().addKeyListener(new java.awt.event.KeyAdapter() {
                    public void keyTyped(java.awt.event.KeyEvent e) {
                        if (!Character.isDigit(e.getKeyChar())) e.consume(); 
                    }
                });

                editor.getTextField().addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusGained(java.awt.event.FocusEvent e) {
                        SwingUtilities.invokeLater(editor.getTextField()::selectAll); 
                    }
                });
                
                spinner.addChangeListener(e -> {
                    if (isSettingValue) return; 
                    int row = tbl.getEditingRow();
                    if (row >= 0) {
                        // FIX: Đổi từ row, 2 sang row, 3
                        productModel.setValueAt(spinner.getValue().toString(), row, 3);
                    }
                });
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                isSettingValue = true; 
                try { 
                    spinner.setValue(Integer.parseInt(value.toString())); 
                } catch (Exception e) { 
                    spinner.setValue(1); 
                }
                isSettingValue = false; 
                
                ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setBackground(Color.WHITE);
                return spinner;
            }

            @Override
            public Object getCellEditorValue() { return spinner.getValue().toString(); }

            @Override
            public boolean stopCellEditing() {
                try { spinner.commitEdit(); } catch (java.text.ParseException e) {}
                return super.stopCellEditing();
            }

            @Override
            public boolean isCellEditable(java.util.EventObject e) {
                if (e instanceof java.awt.event.MouseEvent) {
                    return ((java.awt.event.MouseEvent) e).getClickCount() >= 1; 
                }
                return true;
            }
        }
        
        // FIX: Đổi từ getColumn(2) sang getColumn(3)
        tbl.getColumnModel().getColumn(3).setCellEditor(new SpinnerCellEditor());

        tbl.getColumnModel().getColumn(5).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSel, hasFocus, r, c);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(Color.decode("#111827"));
                lbl.setHorizontalAlignment(JLabel.RIGHT); 
                return lbl;
            }
        });
        
        tbl.getColumnModel().getColumn(6).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSel, hasFocus, r, c);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(Color.decode("#DC2626")); // Tô màu đỏ cho tổng tiền dễ nhìn
                lbl.setHorizontalAlignment(JLabel.RIGHT); 
                return lbl;
            }
        });
        tbl.getColumnModel().getColumn(7).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSel, hasFocus, r, c);
                lbl.setText(""); // Xóa sạch mọi chữ rác bị lọt vào đây
                lbl.setIcon(new MenuIcon("TRASH")); // Vẽ lại icon Thùng rác
                lbl.setForeground(Color.decode("#EF4444")); 
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return lbl;
            }
        });
        JScrollPane sp = new JScrollPane(tbl) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                int tableHeight = tbl.getRowCount() * tbl.getRowHeight();
                int headerHeight = tbl.getTableHeader() != null ? tbl.getTableHeader().getPreferredSize().height : 0;
                
                // Giữ lại tối thiểu chiều cao 1 dòng khi bảng rỗng để giao diện không bị móp
                if (tableHeight == 0) {
                    tableHeight = tbl.getRowHeight();
                }
                
                // [FIX] Tăng từ 5 lên 15px để bù trừ độ dày của các đường kẻ lưới và viền JScrollPane
                d.height = tableHeight + headerHeight + 15; 
                return d;
            }

            @Override
            public Dimension getMaximumSize() {
                // [FIX] Cho phép JScrollPane giãn ngang tối đa (Integer.MAX_VALUE), khóa cứng chiều cao
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
            
            @Override
            public Dimension getMinimumSize() {
                // [FIX] Chặn BoxLayout bên ngoài bóp méo (thu nhỏ) chiều cao của bảng
                return getPreferredSize();
            }
        };
        
        sp.getViewport().setBackground(Color.WHITE); 
        sp.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"))); 
        
        // TẮT HOÀN TOÀN THANH CUỘN DỌC VÀ NGANG CỦA BẢNG ĐỂ DÙNG THANH CUỘN TỔNG
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER); 

        pnl.add(sp, BorderLayout.CENTER);

        tbl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) { 
                int row = tbl.rowAtPoint(e.getPoint());
                int col = tbl.columnAtPoint(e.getPoint());
                
                if (row >= 0) {
                    // Lấy tên sản phẩm để nhận diện đang click vào Liều, Thuốc con, hay Thuốc lẻ
                    String tenSPClick = productModel.getValueAt(row, 0).toString();

                    // ==========================================
                    // 1. XỬ LÝ NÚT XÓA (CLICK CỘT 7)
                    // ==========================================
                    if (col == 7 && SwingUtilities.isLeftMouseButton(e)) { 
                        if (tbl.isEditing()) tbl.getCellEditor().stopCellEditing(); 
                        
                        // A. Nếu xóa dòng [LIỀU] -> Xóa sạch nguyên cụm các thuốc con bên dưới
                        if (tenSPClick.startsWith("[LIỀU]")) {
                            int countToDelete = 1; 
                            // SỬA CHỮ ↳ THÀNH CHILD_ITEM Ở DÒNG DƯỚI NÀY
                            while (row + countToDelete < productModel.getRowCount() && 
                                   productModel.getValueAt(row + countToDelete, 0).toString().startsWith("CHILD_ITEM ")) {
                                countToDelete++;
                            }
                            for (int i = 0; i < countToDelete; i++) {
                                productModel.removeRow(row);
                            }
                        } 
                        // B. Chặn không cho xóa dòng thuốc con (Dù đã ẩn icon nhưng đề phòng click trúng)
                        else if (tenSPClick.startsWith("CHILD_ITEM ")) {
                            return; // Thoát luôn, không làm gì cả
                        } 
                        
                        // C. Xóa thuốc lẻ bình thường
                        else {
                            productModel.removeRow(row);
                        }
                        
                        recalculateTotals();
                        luuNhapHoaDon(true);
                        sp.revalidate(); sp.repaint();
                        return; 
                    }
                    
                    if (tbl.isEditing() && tbl.getEditingColumn() != col) {
                        tbl.getCellEditor().stopCellEditing(); 
                    }

                    // ==========================================
                    // 2. TĂNG GIẢM NHANH BẰNG CHUỘT TRÁI/PHẢI (CLICK CỘT 0)
                    // ==========================================
                    if (col == 0) {
                        // KHÓA: Tuyệt đối không cho click tăng/giảm ở dòng thuốc con
                        if (tenSPClick.startsWith("↳ ")) {
                            return; 
                        }

                        try {
                            // Lấy số lượng hiện tại từ cột 3
                            int slHienTai = Integer.parseInt(productModel.getValueAt(row, 3).toString().trim());
                            
                            if (SwingUtilities.isLeftMouseButton(e)) {
                                // Click Trái -> Tăng 1
                                productModel.setValueAt(String.valueOf(slHienTai + 1), row, 3);
                                // Hàm setValueAt sẽ tự động gọi TableModelListener để nhân số lượng thuốc con!
                            } 
                            else if (SwingUtilities.isRightMouseButton(e)) {
                                // Click Phải -> Giảm 1
                                if (slHienTai > 1) {
                                    productModel.setValueAt(String.valueOf(slHienTai - 1), row, 3);
                                } else {
                                    // Nếu số lượng = 1 mà bấm giảm nữa -> Xóa luôn
                                    if (tbl.isEditing()) tbl.getCellEditor().stopCellEditing();
                                    
                                    // Lặp lại logic xóa cụm liều hoặc xóa lẻ giống hệt nút Thùng rác
                                    if (tenSPClick.startsWith("[LIỀU]")) {
                                        int countToDelete = 1; 
                                        while (row + countToDelete < productModel.getRowCount() && 
                                               productModel.getValueAt(row + countToDelete, 0).toString().startsWith("↳ ")) {
                                            countToDelete++;
                                        }
                                        for (int i = 0; i < countToDelete; i++) {
                                            productModel.removeRow(row);
                                        }
                                    } else {
                                        productModel.removeRow(row);
                                    }
                                    
                                    recalculateTotals();
                                    luuNhapHoaDon(true);
                                    sp.revalidate(); sp.repaint();
                                    return; 
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace(); // Bắt lỗi an toàn nếu dữ liệu ô đang rỗng
                        }
                    }
                }
            }
        });

        tbl.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int row = tbl.getSelectedRow();
                if (row >= 0 && !tbl.isEditing()) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ADD || e.getKeyCode() == java.awt.event.KeyEvent.VK_EQUALS) {
                        int slHienTai = Integer.parseInt(productModel.getValueAt(row, 3).toString());
                        productModel.setValueAt(String.valueOf(slHienTai + 1), row, 3); 
                    } 
                    else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_SUBTRACT || e.getKeyCode() == java.awt.event.KeyEvent.VK_MINUS) {
                        int slHienTai = Integer.parseInt(productModel.getValueAt(row, 3).toString());
                        if (slHienTai > 1) {
                            productModel.setValueAt(String.valueOf(slHienTai - 1), row, 3); 
                        } else {
                            productModel.removeRow(row);
                            recalculateTotals();
                        }
                    }
                }
            }

            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                int row = tbl.getSelectedRow();
                if (row >= 0 && !tbl.isEditing()) {
                    char c = e.getKeyChar();
                    if (Character.isDigit(c)) {
                        tbl.editCellAt(row, 3); // FIX: Buộc mở ô số lượng ở cột 3
                        Component editor = tbl.getEditorComponent();
                        if (editor instanceof JSpinner) {
                            JSpinner spinner = (JSpinner) editor;
                            JSpinner.DefaultEditor spinEditor = (JSpinner.DefaultEditor) spinner.getEditor();
                            spinEditor.getTextField().setText(String.valueOf(c)); 
                            spinEditor.getTextField().requestFocus(); 
                        }
                        e.consume(); 
                    }
                }
            }
        });

        JPopupMenu suggestionPopup = new JPopupMenu() {
            @Override
            public void setVisible(boolean b) {
                // Nếu hệ thống đòi ẩn khung tìm kiếm (b = false) nhưng đang có cờ dont_close bảo vệ thì BỎ QUA
                if (!b && Boolean.TRUE.equals(getClientProperty("dont_close"))) {
                    return; 
                }
                super.setVisible(b);
            }
        };
        suggestionPopup.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8")));
        suggestionPopup.setBackground(Color.WHITE);

        javax.swing.Timer searchTimer = new javax.swing.Timer(300, e -> {
            String rawText = txtSearchProduct.getText().trim();

            // Thêm .contains để chặn hệ thống mang chữ nổi đi tìm kiếm
            if (rawText.isEmpty() || rawText.contains("Tìm tên sản phẩm")) {
                suggestionPopup.setVisible(false);
                return;
            }

            // Xử lý quét mã vạch GS1: (91)LOT-2026-0001(92)100...
            final String text; // BẮT BUỘC PHẢI KHAI BÁO FINAL TẠI ĐÂY
            if (rawText.startsWith("(91)")) {
                int start = rawText.indexOf("(91)") + 4;
                int end = rawText.indexOf("(92)");
                if (end != -1) {
                    text = rawText.substring(start, end);
                } else {
                    text = rawText.substring(start);
                }
            } else {
                text = rawText; // Gán lại thành rawText nếu không phải mã GS1
            }

            // FIX: Hủy Worker cũ nếu người dùng gõ quá nhanh
            if (currentSearchWorker != null && !currentSearchWorker.isDone()) {
                currentSearchWorker.cancel(true);
            }

            currentSearchWorker = new SwingWorker<java.util.List<Object[]>, Void>() {
                @Override
                protected java.util.List<Object[]> doInBackground() throws Exception {
                    BUS.BUS_SanPham busSP = new BUS.BUS_SanPham(); // Gọi qua BUS
                    return busSP.timKiemSanPhamBan(text);
                }

                @Override
                protected void done() {
                    if (isCancelled()) return;
                    try {
                        java.util.List<Object[]> ketQua = get();
                        suggestionPopup.removeAll();
                        boolean hasResult = false;

                        JPanel pnlList = new JPanel();
                        pnlList.setLayout(new BoxLayout(pnlList, BoxLayout.Y_AXIS));
                        pnlList.setBackground(Color.WHITE);

                        if (ketQua != null && !ketQua.isEmpty()) {
                            hasResult = true; 
                            
                            // --- FIX LAG: ĐẶT GIỚI HẠN SỐ LƯỢNG UI RENDER ---
                            int count = 0;
                            int MAX_ITEMS = 12; 

                            // 1. Gom nhóm các lô hàng có cùng Tên và ĐVT
                            java.util.Map<String, java.util.List<Object[]>> groupedSP = new java.util.LinkedHashMap<>();
                            for (Object[] row : ketQua) {
                                String key = row[1].toString() + "_" + (row[2] != null ? row[2].toString() : "");
                                groupedSP.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(row);
                            }

                            // 2. Duyệt qua từng nhóm sản phẩm
                            for (java.util.List<Object[]> batches : groupedSP.values()) {
                                if (count >= MAX_ITEMS) break; 

                                // 3. Sắp xếp lô hàng: Ưu tiên HSD gần nhất lên đầu
                                batches.sort((a, b) -> {
                                    String hsdA = (a.length > 8 && a[8] != null) ? a[8].toString() : "";
                                    String hsdB = (b.length > 8 && b[8] != null) ? b[8].toString() : "";
                                    if (hsdA.isEmpty()) return 1;
                                    if (hsdB.isEmpty()) return -1;
                                    try {
                                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                                        return sdf.parse(hsdA).compareTo(sdf.parse(hsdB));
                                    } catch (Exception e) { return 0; }
                                });

                                // Lấy thông tin chung từ Lô tốt nhất (trên cùng)
                                Object[] firstItem = batches.get(0);
                                String ten = firstItem[1].toString();
                                String donVi = firstItem[2] != null ? firstItem[2].toString() : "";
                                long giaBan = Math.round(Double.parseDouble(firstItem[3].toString()));
                                String gia = String.valueOf(giaBan); 
                                
                                // Cộng tổng tồn kho của tất cả các Lô
                                int tongTon = 0;
                                for(Object[] b : batches) {
                                    try { tongTon += Integer.parseInt(b[4].toString()); } catch(Exception ex){}
                                }
                                String tonKho = String.valueOf(tongTon);
                                
                                String danhMuc = (firstItem.length > 5 && firstItem[5] != null) ? firstItem[5].toString() : "Khác";

                                String thueVat = "5%";
                                String danhMucCheck = (firstItem.length > 5 && firstItem[5] != null) ? firstItem[5].toString() : "";
                                if (danhMucCheck.toLowerCase().contains("kê đơn") && !danhMucCheck.toLowerCase().contains("không kê đơn")) {
                                    
                                }
                                if (firstItem.length > 6 && firstItem[6] != null) {
                                    String rawVat = firstItem[6].toString().trim();
                                    try {
                                        double v = Double.parseDouble(rawVat);
                                        if (v > 0 && v < 1) v = v * 100; 
                                        thueVat = (int)v + "%";
                                    } catch (Exception ex) {
                                        thueVat = rawVat + (rawVat.contains("%") ? "" : "%");
                                    }
                                }

                                // TRUYỀN DANH SÁCH LÔ (batches) VÀO HÀM GIAO DIỆN
                                pnlList.add(createSuggestionItem(suggestionPopup, txtSearchProduct, "", ten, donVi, gia, tonKho, danhMuc, thueVat, batches));
                                count++; 
                            }
                            
                            // THÊM: Hiển thị dòng thông báo nếu còn nhiều sản phẩm bị ẩn đi
                            if (ketQua.size() > MAX_ITEMS) {
                                JLabel lblMore = new JLabel("Còn " + (ketQua.size() - MAX_ITEMS) + " sản phẩm khác. Vui lòng gõ thêm chi tiết...");
                                lblMore.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                                lblMore.setForeground(Color.GRAY);
                                lblMore.setBorder(new EmptyBorder(8, 15, 8, 15));
                                pnlList.add(lblMore);
                            }
                        }

                        if (hasResult) {
                            JScrollPane scrollPane = new JScrollPane(pnlList);
                            scrollPane.setBorder(null);
                            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                            scrollPane.getVerticalScrollBar().setUnitIncrement(16); 

                            scrollPane.getVerticalScrollBar().setUI(new Utils.ModernScrollBarUI());
                            scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
                            
                            int listHeight = pnlList.getPreferredSize().height;
                            scrollPane.setPreferredSize(new Dimension(pnlSearchWrapper.getWidth(), Math.min(listHeight, 300)));
                            
                            suggestionPopup.add(scrollPane);
                            
                            // --- FIX LỖI: ÉP POPUP PHẢI GIÃN RA VỪA KHÍT NỘI DUNG ---
                            suggestionPopup.pack(); 
                            // -------------------------------------------------------
                            
                            suggestionPopup.show(pnlSearchWrapper, 0, pnlSearchWrapper.getHeight());
                        } else {
                            JMenuItem emptyItem = new JMenuItem("Không tìm thấy sản phẩm nào phù hợp...");
                            emptyItem.setEnabled(false);
                            suggestionPopup.add(emptyItem);
                            
                            // --- FIX LỖI: ÉP POPUP PHẢI GIÃN RA ---
                            suggestionPopup.pack(); 
                            // --------------------------------------
                            
                            suggestionPopup.show(pnlSearchWrapper, 0, pnlSearchWrapper.getHeight());
                        }
                        txtSearchProduct.requestFocus(); 
                        
                    } catch (Exception ex) {
                        System.err.println("Lỗi khi hiển thị UI tìm kiếm: " + ex.getMessage());
                    }
                }
            };
            currentSearchWorker.execute();
        });
        
        searchTimer.setRepeats(false); 

        txtSearchProduct.addKeyListener(new java.awt.event.KeyAdapter() {
            
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    
                    // 1. CHỐT CHẶN QUAN TRỌNG: Dừng ngay bộ đếm giờ (tránh hiện popup gợi ý sau khi đã ấn Enter)
                    searchTimer.stop(); 
                    
                    String rawSearchText = txtSearchProduct.getText().trim();
                    if (rawSearchText.isEmpty() || rawSearchText.contains("Tìm tên sản phẩm")) return;

                    // 2. ÉP ĐÓNG POPUP GỢI Ý NGAY LẬP TỨC
                    if (suggestionPopup != null && suggestionPopup.isVisible()) {
                        suggestionPopup.setVisible(false);
                    }
                    
                    if (currentSearchWorker != null && !currentSearchWorker.isDone()) {
                        currentSearchWorker.cancel(true);
                    }

                    // === KHAI BÁO FINAL CHUẨN ===
                    final String searchText;
                    if (rawSearchText.startsWith("(91)")) {
                        int start = rawSearchText.indexOf("(91)") + 4;
                        int end = rawSearchText.indexOf("(92)");
                        if (end != -1) {
                            searchText = rawSearchText.substring(start, end);
                        } else {
                            searchText = rawSearchText.substring(start);
                        }
                    } else {
                        searchText = rawSearchText; 
                    }

                    // === DÙNG LUỒNG ẨN (SWINGWORKER) ĐỂ KHÔNG LÀM TREO MÀN HÌNH ===
                    SwingWorker<java.util.List<Object[]>, Void> enterWorker = new SwingWorker<java.util.List<Object[]>, Void>() {
                        @Override
                        protected java.util.List<Object[]> doInBackground() throws Exception {
                            return new BUS.BUS_SanPham().timKiemSanPhamBan(searchText); 
                        }

                        @Override
                        protected void done() {
                            if (isCancelled()) return;
                            try {
                                java.util.List<Object[]> ketQua = get();
                                if (ketQua != null && !ketQua.isEmpty()) {
                                    
                                    // ÉP SẮP XẾP CÁC LÔ CẬN HẠN NHẤT LÊN ĐẦU
                                    ketQua.sort((a, b) -> {
                                        String hsdA = (a.length > 8 && a[8] != null) ? a[8].toString().trim() : "";
                                        String hsdB = (b.length > 8 && b[8] != null) ? b[8].toString().trim() : "";
                                        if (hsdA.isEmpty() || hsdA.equalsIgnoreCase("N/A")) return 1;
                                        if (hsdB.isEmpty() || hsdB.equalsIgnoreCase("N/A")) return -1;
                                        try {
                                            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                            return java.time.LocalDate.parse(hsdA, fmt).compareTo(java.time.LocalDate.parse(hsdB, fmt));
                                        } catch (Exception ex) { return 0; }
                                    });

                                    if (ketQua.size() == 1) {
                                        if (suggestionPopup != null) suggestionPopup.setVisible(false); // Diệt popup
                                        xyLyThemSanPhamNhanh(ketQua.get(0), suggestionPopup, txtSearchProduct);
                                    } else {
                                        boolean timThayDichDanh = false;
                                        if (searchText.length() < 8) {
                                            for (Object[] row : ketQua) {
                                                String maSP = row[0] != null ? row[0].toString() : "";
                                                String soLo = row.length > 7 && row[7] != null ? row[7].toString() : "";
                                                if (maSP.equalsIgnoreCase(searchText) || soLo.equalsIgnoreCase(searchText)) {
                                                    if (suggestionPopup != null) suggestionPopup.setVisible(false); // Diệt popup
                                                    xyLyThemSanPhamNhanh(row, suggestionPopup, txtSearchProduct);
                                                    timThayDichDanh = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (!timThayDichDanh) {
                                            // 3. DIỆT POPUP TẬN GỐC LẦN NỮA TRƯỚC KHI MỞ BẢNG CHỌN LÔ
                                            if (suggestionPopup != null) suggestionPopup.setVisible(false);
                                            hienThiPopupChonLoKhiQuet(ketQua, suggestionPopup, txtSearchProduct);
                                        }
                                    }
                                } else {
                                    showCustomNotification("KHÔNG TÌM THẤY", "Không tìm thấy sản phẩm với mã: " + searchText, "WARNING");
                                    txtSearchProduct.setText("");
                                    txtSearchProduct.requestFocus();
                                }
                            } catch (Exception ex) {}
                        }
                    };
                    enterWorker.execute();
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP || 
                    e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN || 
                    e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    return; 
                }
                searchTimer.restart(); 
            }
        });
        
        return pnl;
    }

    private void xyLyThemSanPhamNhanh(Object[] firstItem, JPopupMenu suggestionPopup, JTextField txtSearchProduct) {
        String name = firstItem[1].toString();
        String unit = firstItem[2] != null ? firstItem[2].toString() : "";
        long giaBan = Math.round(Double.parseDouble(firstItem[3].toString()));
        String price = String.valueOf(giaBan);
        
        int tonKhoThucTe = 0;
        try {
            if (firstItem.length > 4 && firstItem[4] != null) {
                tonKhoThucTe = Integer.parseInt(firstItem[4].toString().trim());
            }
        } catch (NumberFormatException ignored) {}

        if (tonKhoThucTe <= 0) {
            JOptionPane.showMessageDialog(
                txtSearchProduct,
                "Mã vạch lạ! Thuốc chưa được nhập vào hệ thống\n"
                    + "hoặc lô hàng hiện tại đã hết tồn kho.\n\n"
                    + "Sản phẩm: " + name,
                "Không thể thêm vào hóa đơn",
                JOptionPane.ERROR_MESSAGE
            );
            txtSearchProduct.setText(""); 
            txtSearchProduct.requestFocus();
            return; 
        }
        
        String thueVat = "5%";
        if (firstItem.length > 6 && firstItem[6] != null) {
            String rawVat = firstItem[6].toString().trim();
            try {
                double v = Double.parseDouble(rawVat);
                if (v > 0 && v < 1) v = v * 100; 
                thueVat = (int)v + "%";
            } catch (Exception ex) { thueVat = rawVat + (rawVat.contains("%") ? "" : "%"); }
        }

        String loHang = (firstItem.length > 7 && firstItem[7] != null) ? firstItem[7].toString() : "";
        String hsd = (firstItem.length > 8 && firstItem[8] != null) ? firstItem[8].toString() : "";
        String loHsdText = loHang;
        if (!loHsdText.isEmpty() && !hsd.isEmpty()) loHsdText += " — " + hsd;
        else if (loHsdText.isEmpty() && !hsd.isEmpty()) loHsdText = hsd;

        if (hsd != null && !hsd.trim().isEmpty() && !hsd.equalsIgnoreCase("N/A")) {
            try {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                java.time.LocalDate expiryDate = java.time.LocalDate.parse(hsd.trim(), formatter);
                java.time.LocalDate today = java.time.LocalDate.now();
                
                if (expiryDate.isBefore(today.plusMonths(6))) {
                    String msgWarning = "Sản phẩm \"" + name + "\" thuộc Lô này gần hết hạn (HSD: " + hsd + "). Tiếp tục bán?";
                    if (expiryDate.isBefore(today)) {
                        msgWarning = "Sản phẩm \"" + name + "\" ĐÃ HẾT HẠN (HSD: " + hsd + ")! Chắc chắn bán?";
                    }
                    boolean tiepTuc = showCustomConfirmDialog("CẢNH BÁO", msgWarning);
                    if (!tiepTuc) return; 
                }
            } catch (Exception ex) {}
        }
        String danhMucCheck = (firstItem.length > 5 && firstItem[5] != null) ? firstItem[5].toString() : "";
        if (danhMucCheck.toLowerCase().contains("kê đơn") && !danhMucCheck.toLowerCase().contains("không kê đơn")) {
            moBangKeDonTuDong();
        }
        boolean daTonTai = false;
        int rowIndex = -1;
        int currentQty = 0;
        for (int i = 0; i < productModel.getRowCount(); i++) {
            if (productModel.getValueAt(i, 0).toString().equals(name) &&
                productModel.getValueAt(i, 1).toString().equals(unit) &&
                productModel.getValueAt(i, 2).toString().equals(loHsdText)) { 
                daTonTai = true;
                rowIndex = i;
                currentQty = Integer.parseInt(productModel.getValueAt(i, 3).toString());
                break;
            }
        }

        // =========================================================
        // KHÓA MẮT LISTENER ĐỂ CHỐNG LOOP & LAG CPU
        // =========================================================
        isTableUpdating = true; 
        try {
            if (daTonTai) {
                currentQty++; 
                productModel.setValueAt(String.valueOf(currentQty), rowIndex, 3);
                double kmV = 0;
                try {
                    String kmStr = productModel.getValueAt(rowIndex, 5).toString().replace("%", "").trim();
                    kmV = Double.parseDouble(kmStr) / 100.0;
                } catch (Exception ex) {}
                
                long thanhTien = Math.round(giaBan * currentQty * (1.0 - kmV));
                productModel.setValueAt(String.format("%,d", thanhTien).replace(',', '.') + "đ", rowIndex, 6);
            } else {
                long thanhTien = giaBan; 
                String thanhTienFormatted = String.format("%,d", thanhTien).replace(',', '.') + "đ"; 
                String giaFormatted = String.format("%,d", giaBan).replace(',', '.') + "đ";

                productModel.addRow(new Object[]{
                    name, unit, loHsdText, "1", giaFormatted, "0%", thanhTienFormatted, "", thueVat, "1" 
                });
            }
        } finally {
            isTableUpdating = false; // XONG VIỆC MỚI MỞ KHÓA LẠI
        }
            
        recalculateTotals();
        if (suggestionPopup != null) suggestionPopup.setVisible(false); 
        if (txtSearchProduct != null) txtSearchProduct.setText(""); 
    }

    private JPanel createSummaryPanel() {
        JPanel pnlWrapper = new JPanel(new BorderLayout(0, 15));
        pnlWrapper.setBackground(Color.WHITE);

        JPanel pnlMethod = new JPanel(new BorderLayout(0, 5));
        pnlMethod.setBackground(Color.WHITE);
        
        JLabel lblMethodTitle = new JLabel("Phương thức thanh toán");
        lblMethodTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlMethod.add(lblMethodTitle, BorderLayout.NORTH);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlButtons.setBackground(Color.WHITE);
        
        JButton btnTienMat = new JButton("Tiền mặt");
        JButton btnChuyenKhoan = new JButton("Chuyển khoản");
        
        Dimension btnSize = new Dimension(150, 40);
        btnTienMat.setPreferredSize(btnSize);
        btnChuyenKhoan.setPreferredSize(btnSize);
        
        setPaymentBtnActive(btnTienMat);
        setPaymentBtnInactive(btnChuyenKhoan);
        
        pnlButtons.add(btnTienMat);
        pnlButtons.add(btnChuyenKhoan);
        pnlMethod.add(pnlButtons, BorderLayout.CENTER);

        JPanel pnlTienMatWrapper = createCashGridPanel();
        pnlChuyenKhoanWrapper = new JPanel(new BorderLayout());
        pnlChuyenKhoanWrapper.setBackground(Color.WHITE);
        pnlChuyenKhoanWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E5E7EB"), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        pnlChuyenKhoanWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblQRTriGia = new JLabel("Quét mã qua ứng dụng Ngân hàng", SwingConstants.CENTER);
        lblQRTriGia.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblQRTriGia.setForeground(Color.decode("#009A33")); 

        lblQRCode = new JLabel("Đang tạo mã QR...", SwingConstants.CENTER);
        lblQRCode.setPreferredSize(new Dimension(200, 200));
        lblQRCode.setBorder(BorderFactory.createLineBorder(Color.decode("#F3F4F6"), 2));
        lblQRAmount = new JLabel("Cần thanh toán: 0đ", SwingConstants.CENTER);
        lblQRAmount.setFont(new Font("Segoe UI", Font.BOLD, 18)); 
        lblQRAmount.setForeground(Color.decode("#DC2626")); 

        pnlChuyenKhoanWrapper.add(lblQRTriGia, BorderLayout.NORTH);
        pnlChuyenKhoanWrapper.add(lblQRCode, BorderLayout.CENTER);
        JButton btnInQR = new JButton("🖨");
        btnInQR.setPreferredSize(new Dimension(40, 32));
        btnInQR.setBackground(Color.WHITE);
        btnInQR.setBorder(BorderFactory.createLineBorder(borderColor));
        btnInQR.setFocusPainted(false);
        btnInQR.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnInQR.setToolTipText("In mã QR thanh toán");

        btnInQR.addActionListener(ev -> {
            if (lblQRCode.getIcon() != null) {
                thucHienInMaQRThanhToan();
            } else {
                showCustomNotification("Thông báo", "Mã QR chưa sẵn sàng để in!", "WARNING");
            }
        });

        // Bọc số tiền và nút in chung 1 dòng ở SOUTH
        JPanel pnlQRSouth = new JPanel(new BorderLayout(5, 0));
        pnlQRSouth.setBackground(Color.WHITE);
        pnlQRSouth.add(lblQRAmount, BorderLayout.CENTER);
        pnlQRSouth.add(btnInQR, BorderLayout.EAST);

        pnlChuyenKhoanWrapper.add(pnlQRSouth, BorderLayout.SOUTH);
        pnlChuyenKhoanWrapper.setVisible(false);
         
        
        JPanel pnlNote = new JPanel(new BorderLayout(0, 5));
        pnlNote.setBackground(Color.WHITE);
        JLabel lblNote = new JLabel("Ghi chú");
        lblNote.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlNote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        txtNote = new JTextField("Ghi chú thêm..."); // <--- Đã sửa: Xóa chữ JTextField
        txtNote.setForeground(Color.GRAY);
        txtNote.setPreferredSize(new Dimension(0, 40));
        txtNote.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        
        txtNote.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txtNote.getText().equals("Ghi chú thêm...")) { txtNote.setText(""); txtNote.setForeground(Color.BLACK); }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtNote.getText().isEmpty()) { txtNote.setForeground(Color.GRAY); txtNote.setText("Ghi chú thêm..."); }
            }
        });
        
        pnlNote.add(lblNote, BorderLayout.NORTH);
        pnlNote.add(txtNote, BorderLayout.CENTER);

        JPanel pnlFinal = new JPanel(new GridLayout(5, 2, 10, 5));
        pnlFinal.setBackground(darkBlue);
        pnlFinal.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        pnlFinal.add(createWhiteLabel("Tạm tính:")); 
        lblSubtotalValue = createWhiteLabel("0đ", SwingConstants.RIGHT);
        pnlFinal.add(lblSubtotalValue);
        
        // LỖI 3 FIX: Đặt Giảm khuyến mãi TRƯỚC VAT
        pnlFinal.add(createWhiteLabel("Giảm khuyến mãi:")); 
        lblDiscountValue = createWhiteLabel("-0đ", SwingConstants.RIGHT);
        lblDiscountValue.setForeground(Color.decode("#FCA5A5")); 
        pnlFinal.add(lblDiscountValue);
        
        pnlFinal.add(createWhiteLabel("VAT:")); 
        lblVatValue = createWhiteLabel("+0đ", SwingConstants.RIGHT);
        pnlFinal.add(lblVatValue);
        
        pnlFinal.add(createWhiteLabel("Dùng điểm:")); 
        lblDungDiemValue = createWhiteLabel("-0đ", SwingConstants.RIGHT);
        lblDungDiemValue.setForeground(Color.decode("#FCD34D")); 
        pnlFinal.add(lblDungDiemValue);
        
        JLabel lblTotal = new JLabel("TỔNG THANH TOÁN:");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18)); lblTotal.setForeground(Color.WHITE);
        
        lblTotalPriceValue = new JLabel("0đ");
        lblTotalPriceValue.setFont(new Font("Segoe UI", Font.BOLD, 24)); lblTotalPriceValue.setForeground(Color.YELLOW);
        lblTotalPriceValue.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlFinal.add(lblTotal); pnlFinal.add(lblTotalPriceValue);

        // --- ĐÃ FIX: Gói pnlFinal vào một Wrapper để chống lỗi giãn theo chiều dọc ---
        JPanel pnlFinalWrapper = new JPanel(new BorderLayout());
        pnlFinalWrapper.setBackground(Color.WHITE);
        pnlFinalWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlFinalWrapper.add(pnlFinal, BorderLayout.NORTH); 

        JPanel pnlCenter = new JPanel();
        pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));
        pnlCenter.setBackground(Color.WHITE);
        
        pnlMethod.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlTienMatWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        pnlCenter.add(pnlMethod);
        pnlCenter.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlCenter.add(pnlTienMatWrapper); 
        pnlCenter.add(pnlChuyenKhoanWrapper); 
        pnlCenter.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlCenter.add(pnlNote);
        pnlCenter.add(Box.createRigidArea(new Dimension(0, 20)));
        pnlCenter.add(pnlFinalWrapper); // Add cái bọc ngoài thay vì add trực tiếp
        pnlCenter.add(Box.createVerticalGlue()); // Khóa keo dán lại
        
        pnlWrapper.add(pnlCenter, BorderLayout.CENTER);
        
        btnTienMat.addActionListener(e -> {
            phuongThuc = "Tiền mặt";
            setPaymentBtnActive(btnTienMat);
            setPaymentBtnInactive(btnChuyenKhoan);
            pnlTienMatWrapper.setVisible(true); 
            pnlChuyenKhoanWrapper.setVisible(false); 
            pnlWrapper.revalidate(); pnlWrapper.repaint();
        });

        btnChuyenKhoan.addActionListener(e -> {
            phuongThuc = "Chuyển khoản";
            setPaymentBtnActive(btnChuyenKhoan);
            setPaymentBtnInactive(btnTienMat);
            pnlTienMatWrapper.setVisible(false); 
            pnlChuyenKhoanWrapper.setVisible(true);  
            
            loadQRCodeVCB(lblQRCode);
            pnlWrapper.revalidate(); pnlWrapper.repaint();
        });

        return pnlWrapper;
    }

    private JLabel createWhiteLabel(String text) { return createWhiteLabel(text, SwingConstants.LEFT); }
    private JLabel createWhiteLabel(String text, int align) {
        JLabel l = new JLabel(text, align); l.setForeground(Color.WHITE); return l;
    }
    
    private void setPaymentBtnActive(JButton btn) {
        btn.setBackground(Color.decode("#1967D2"));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createLineBorder(Color.decode("#1967D2"), 1, true));
        btn.setFocusPainted(false);
    }

    private void setPaymentBtnInactive(JButton btn) {
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.decode("#4B5563"));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true));
        btn.setFocusPainted(false);
    }

    private JPanel createCashGridPanel() {
        listCounters.clear();
        listCountLabels.clear();
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        // ĐỔI: Nền từ vàng nhạt sang Xanh dương siêu nhạt (#F0F9FF)
        pnl.setBackground(Color.decode("#F0F9FF")); 
        pnl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DBEAFE"), 1, true),
                new EmptyBorder(5, 10, 5, 10) 
            ));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("Tiền khách đưa");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.decode("#1E40AF")); // Đổi tiêu đề sang Xanh đậm

        JButton btnChon = new JButton("Chọn mệnh giá");
        btnChon.setBackground(Color.decode("#3B82F6")); // Nút bấm màu Xanh Blue rực
        btnChon.setForeground(Color.WHITE);
        btnChon.setFocusPainted(false);
        btnChon.setBorderPainted(false);
        btnChon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnChon.setPreferredSize(new Dimension(130, 30));
        
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(btnChon, BorderLayout.EAST);

        JPanel pnlGrid = new JPanel(new GridLayout(3, 3, 10, 10));
        pnlGrid.setOpaque(false);
        
        // Giữ nguyên các ô tiền mệnh giá (vì mỗi ô đã có màu đặc trưng rồi)
        pnlGrid.add(createMoneyCell("500.000đ", 500000, "#3B82F6")); 
        pnlGrid.add(createMoneyCell("200.000đ", 200000, "#D946EF")); 
        pnlGrid.add(createMoneyCell("100.000đ", 100000, "#10B981")); 
        pnlGrid.add(createMoneyCell("50.000đ", 50000, "#EAB308"));  
        pnlGrid.add(createMoneyCell("20.000đ", 20000, "#EF4444"));  
        pnlGrid.add(createMoneyCell("10.000đ", 10000, "#F43F5E"));  
        pnlGrid.add(createMoneyCell("5.000đ", 5000, "#EC4899"));   
        pnlGrid.add(createMoneyCell("2.000đ", 2000, "#6B7280"));   
        pnlGrid.add(createMoneyCell("1.000đ", 1000, "#6B7280"));   

        JPanel pnlTopRow = new JPanel(new GridLayout(1, 3, 10, 10));
        pnlTopRow.setOpaque(false);
        pnlTopRow.add(new JLabel()); 
        pnlTopRow.add(createExactMoneyCell()); // Ô Vừa đủ tông xanh nằm đây
        pnlTopRow.add(new JLabel()); 

        JPanel pnlCenterWrapper = new JPanel(new BorderLayout(0, 10));
        pnlCenterWrapper.setOpaque(false);
        pnlCenterWrapper.add(pnlTopRow, BorderLayout.NORTH);
        pnlCenterWrapper.add(pnlGrid, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setOpaque(false);
        // ĐỔI: Đường gạch chân sang màu Xanh nhạt
        pnlFooter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#93C5FD")),
            new EmptyBorder(10, 0, 5, 0)
        ));
        
        JLabel lblTotalText = new JLabel("Tổng tiền mặt");
        lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalText.setForeground(Color.decode("#1E3A8A")); // Chữ xanh đen
        
        lblTotalValue = new JLabel("0đ");
        lblTotalValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalValue.setForeground(Color.decode("#1D4ED8")); // Tiền nhảy màu xanh đậm
        
        JPanel pnlInfoRight = new JPanel(new GridLayout(2, 1));
        pnlInfoRight.setOpaque(false);
        pnlInfoRight.add(lblTotalValue);
        
        lblTienThuaValue = new JLabel("Tiền thừa: 0đ", SwingConstants.RIGHT);
        lblTienThuaValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlInfoRight.add(lblTienThuaValue);

        pnlFooter.add(lblTotalText, BorderLayout.WEST);
        pnlFooter.add(pnlInfoRight, BorderLayout.EAST);

        pnl.add(pnlHeader, BorderLayout.NORTH);
        pnl.add(pnlCenterWrapper, BorderLayout.CENTER); 
        pnl.add(pnlFooter, BorderLayout.SOUTH);

        return pnl;
    }

    private JPanel createMoneyCell(String labelText, long faceValue, String hexColor) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode(hexColor), 1, true),
            new EmptyBorder(8, 10, 8, 10) 
        ));
        
        pnl.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblValue = new JLabel(labelText);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblValue.setForeground(Color.decode(hexColor));

        JLabel lblCount = new JLabel("0"); 
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCount.setForeground(Color.decode("#111827"));
        
        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlRight.setOpaque(false);
        pnlRight.add(lblCount);

        final int[] count = {0}; 

        java.awt.event.MouseAdapter clickAdapter = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    count[0]++;
                    lblCount.setText(String.valueOf(count[0]));
                    tongTienMat += faceValue;
                    capNhatTongTien();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    if (count[0] > 0) { 
                        count[0]--;
                        lblCount.setText(String.valueOf(count[0]));
                        tongTienMat -= faceValue;
                        capNhatTongTien(); 
                    }
                }
            }
        };

        pnl.addMouseListener(clickAdapter);
        lblValue.addMouseListener(clickAdapter);
        lblCount.addMouseListener(clickAdapter);
        pnlRight.addMouseListener(clickAdapter);

        pnl.add(lblValue, BorderLayout.WEST);
        pnl.add(pnlRight, BorderLayout.EAST);
        
        return pnl;
    }
    
    private void capNhatTongTien() {
        if (lblTotalValue != null) {
            String formattedString = String.format("%,d", tongTienMat).replace(',', '.');
            lblTotalValue.setText(formattedString + "đ");
            
            long tienThua = tongTienMat - tongHoaDon;
            if (tienThua >= 0) {
                lblTienThuaValue.setText("Tiền thừa: " + String.format("%,d", tienThua).replace(',', '.') + "đ");
                lblTienThuaValue.setForeground(Color.decode("#10B981")); 
            } else {
                lblTienThuaValue.setText("Còn thiếu...");
                lblTienThuaValue.setForeground(Color.decode("#EF4444")); 
            }
        }
    }

 // --- FIX: GẮN SỰ KIỆN CHUỘT CHO CẢ KHUNG LẪN CHỮ ĐỂ NHẤP CHUỘT NHẠY 100% ---
    private JPanel createCustomerSuggestionItem(String id, String name, String phone, String points) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pnl.setBorder(new EmptyBorder(5, 10, 5, 10));
        pnl.setName(id); // Cất giấu Mã KH (ID) vào thuộc tính Name để lấy cho chuẩn

        String htmlText = "<html><div style='width: 220px;'>"
                        + "<span style='font-size: 13px; font-weight: bold; color: #111827;'>" + name + "</span>"
                        + "<span style='font-size: 13px; color: #6B7280;'> - " + phone + "</span><br>"
                        + "<span style='font-size: 11px; color: #9333EA;'>Tích lũy: " + points + "đ</span>"
                        + "</div></html>";

        JLabel lblInfo = new JLabel(htmlText);
        lblInfo.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hiển thị hình bàn tay khi rê vào chữ
        pnl.add(lblInfo, BorderLayout.CENTER);

        // Đóng gói sự kiện chuột vào một biến chung
        java.awt.event.MouseAdapter clickHandler = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                pnl.setBackground(Color.decode("#F3F4F6")); // Hover đổi màu
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                pnl.setBackground(Color.WHITE);
            }
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    customerSuggestionPopup.setVisible(false);
                    txtSearch.setText(id); 
                    txtSearch.setForeground(Color.BLACK);
                    
                    // Kích hoạt nút Tra Cứu
                    SwingUtilities.invokeLater(() -> {
                        for (Component c : txtSearch.getParent().getComponents()) {
                            if (c instanceof JButton) {
                                ((JButton) c).doClick();
                                break;
                            }
                        }
                    });
                }
            }
        };

        // GẮN SỰ KIỆN CHO CẢ KHUNG PANEL LẪN DÒNG CHỮ (Tránh bị chữ nuốt click)
        pnl.addMouseListener(clickHandler);
        lblInfo.addMouseListener(clickHandler);

        return pnl;
    }
    private void showCustomNotification(String titleText, String message, String type) {
        JDialog dialog = new JDialog(this, true); 
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createLineBorder(Color.decode("#1E3A8A"), 2));
        pnlMain.setBackground(Color.WHITE);

        // 1. HEADER 
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.decode("#1E3A8A"));
        pnlHeader.setPreferredSize(new Dimension(0, 40));
        JLabel lblTitle = new JLabel(titleText.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.CENTER);

        // 2. BODY (ĐÃ SỬA: BỎ ÉP KÍCH THƯỚC CỨNG, CHO TỰ ĐỘNG CO GIÃN THEO CHỮ)
        JPanel pnlBody = new JPanel(new BorderLayout(15, 0));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel pnlIcon = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color mainColor = type.equals("ERROR") ? Color.decode("#EF4444") : 
                                  (type.equals("SUCCESS") ? Color.decode("#10B981") : Color.decode("#F59E0B"));
                Color bgColor = type.equals("ERROR") ? Color.decode("#FEE2E2") : 
                                (type.equals("SUCCESS") ? Color.decode("#D1FAE5") : Color.decode("#FEF3C7"));
                
                g2.setColor(bgColor);
                g2.fillOval(0, 0, 50, 50);
                g2.setColor(mainColor);
                g2.setStroke(new java.awt.BasicStroke(3f));
                g2.drawOval(0, 0, 50, 50);
                
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                FontMetrics fm = g2.getFontMetrics();
                String symbol = type.equals("ERROR") ? "X" : (type.equals("SUCCESS") ? "V" : "!");
                int x = (50 - fm.stringWidth(symbol)) / 2;
                int y = ((50 - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(symbol, x, y);
                g2.dispose();
            }
        };
        pnlIcon.setPreferredSize(new Dimension(50, 50));
        
        JPanel iconWrapper = new JPanel(new BorderLayout());
        iconWrapper.setOpaque(false);
        iconWrapper.add(pnlIcon, BorderLayout.NORTH);

        String htmlContent = "<html><div style='width: 320px; line-height: 1.4; word-wrap: break-word;'>" 
                + message.replace("\n", "<br>") 
                + "</div></html>";

        JLabel msg = new JLabel(htmlContent);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(Color.decode("#333333"));
        msg.setVerticalAlignment(SwingConstants.TOP); 

        pnlBody.add(iconWrapper, BorderLayout.WEST);
        pnlBody.add(msg, BorderLayout.CENTER);

        // 3. FOOTER
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlFooter.setBackground(Color.WHITE);
        JButton btnClose = new JButton("Đóng");
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.setBackground(Color.decode("#1E3A8A"));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dialog.dispose());
        pnlFooter.add(btnClose);

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlBody, BorderLayout.CENTER);
        pnlMain.add(pnlFooter, BorderLayout.SOUTH);

        dialog.add(pnlMain);
        dialog.pack(); // Lệnh này giúp hộp thoại tự ôm khít văn bản, ko bị dư khoảng trống
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private boolean showCustomConfirmDialog(String titleText, String message) {
        final boolean[] result = {false};
        
        // Dùng WindowAncestor để popup luôn nằm chính giữa màn hình cha
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow != null ? (Frame) parentWindow : null, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createLineBorder(Color.decode("#1E3A8A"), 2));
        pnlMain.setBackground(Color.WHITE);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.decode("#1E3A8A"));
        pnlHeader.setPreferredSize(new Dimension(0, 45));
        JLabel lblTitle = new JLabel(titleText.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.CENTER);

        JPanel pnlBody = new JPanel(new BorderLayout());
        pnlBody.setBackground(Color.WHITE);
        // FIX: Tăng khoảng trống 4 góc lên 20px để chữ không bị sát mép
        pnlBody.setBorder(new EmptyBorder(20, 20, 20, 20)); 
        
        // Bỏ CSS font-size, chỉ dùng HTML để xuống dòng và in đậm
        JLabel msg = new JLabel("<html><div style='text-align: center;'>" + message.replace("\n", "<br>") + "</div></html>", SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Set font trực tiếp bằng Java
        msg.setForeground(Color.decode("#333333"));
        pnlBody.add(msg, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        pnlFooter.setBackground(Color.WHITE);
        
        JButton btnYes = new JButton("Đồng ý");
        btnYes.setPreferredSize(new Dimension(110, 38));
        btnYes.setFocusable(false);
        btnYes.setBackground(Color.decode("#EF4444")); 
        btnYes.setForeground(Color.WHITE);
        btnYes.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnYes.setFocusPainted(false);
        btnYes.setBorderPainted(false);
        btnYes.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnYes.addActionListener(e -> { result[0] = true; dialog.dispose(); });

        JButton btnNo = new JButton("Hủy");
        btnNo.setPreferredSize(new Dimension(110, 38));
        btnNo.setFocusable(false);
        btnNo.setBackground(Color.decode("#1E3A8A"));
        btnNo.setForeground(Color.WHITE);
        btnNo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNo.setFocusPainted(false);
        btnNo.setBorderPainted(false);
        btnNo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNo.addActionListener(e -> dialog.dispose());

        pnlFooter.add(btnYes); pnlFooter.add(btnNo);

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlBody, BorderLayout.CENTER);
        pnlMain.add(pnlFooter, BorderLayout.SOUTH);

        dialog.add(pnlMain);
        
        // FIX QUAN TRỌNG: Tăng kích thước khung lên 440x260 để chứa đủ 5 dòng chữ
        dialog.setSize(440, 260); 
        dialog.setLocationRelativeTo(this);
        dialog.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ENTER"), "none");
        dialog.setVisible(true);

        return result[0];
    }
    private void luuNhapHoaDon(boolean isAutoSave) {
        try {
            java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();

            if (productModel.getRowCount() == 0) {
                if (!isAutoSave) showCustomNotification("CẢNH BÁO", "Hóa đơn chưa có sản phẩm nào để lưu nháp!", "WARNING");
                return;
            }

            String rawName = txtName.getText().trim();
            String rawPhone = txtPhone.getText().trim();

            if (rawName.toLowerCase().contains("tên khách") || rawName.toLowerCase().contains("bỏ trống")) rawName = "";
            if (rawPhone.toLowerCase().contains("số điện") || rawPhone.toLowerCase().contains("tùy chọn") || rawPhone.toLowerCase().contains("tuỳ chọn")) rawPhone = "";

            String txtSearchValue = txtSearch.getText().trim();
            if (rawPhone.isEmpty() && !txtSearchValue.isEmpty() && !txtSearchValue.contains("Nhập SĐT")) {
                if (txtSearchValue.matches("^[0-9]{9,11}$")) {
                    rawPhone = txtSearchValue;
                }
            }

            String khach = rawName.isEmpty() ? "Khách lẻ" : rawName;
            String sdt = rawPhone;

            BUS.BUS_HoaDon busHD = new BUS.BUS_HoaDon(); 
            BUS.BUS_ChiTietHoaDon busCT = new BUS.BUS_ChiTietHoaDon();
            BUS.BUS_KhachHang busKH = new BUS.BUS_KhachHang();
            BUS.BUS_SanPham busSP = new BUS.BUS_SanPham();
            BUS.BUS_DonViDoLuong busDV = new BUS.BUS_DonViDoLuong();

            Entity.HoaDon hd = new Entity.HoaDon();
            String maHD = (this.maHDDangSua != null && !this.maHDDangSua.isEmpty()) ? this.maHDDangSua : phatSinhMaHoaDon() + "-LuuNhap";
            hd.setId(maHD);
            hd.setNgayLapHD(java.time.LocalDateTime.now());
            
            String maNVHienTai = Utils.UserSession.getInstance().getMaNhanVien();
         // FIX: Đổi "DS-0001" thành mã nhân viên CÓ THẬT tương tự như trên
         Entity.NhanVien nv = new Entity.NhanVien(maNVHienTai != null && !maNVHienTai.trim().isEmpty() ? maNVHienTai : "NV001");
         hd.setNhanVienId(nv);

            Entity.KhachHang khObj = null;
            if (!sdt.isEmpty() || !khach.equals("Khách lẻ")) {
                if (!sdt.isEmpty()) khObj = busKH.getKhachHangTheoSDT(sdt);
                if (khObj == null) {
                    khObj = new Entity.KhachHang();
                    khObj.setId(busKH.phatSinhMaKHTiepTheo());
                    khObj.setHoVaTen(khach);
                    khObj.setSdt(sdt.isEmpty() ? null : sdt);
                    khObj.setNgayTao(java.time.LocalDateTime.now());
                    khObj.setDiemTichLuy(0);
                    busKH.themKhachHang(khObj);
                }
            }
            hd.setKhachHangId(khObj);

            String strKeDon = "";
            if (pnlDonThuoc != null && pnlDonThuoc.isVisible()) {
                String bs = txtBacSi.getText().trim();
                String cs = txtCoSo.getText().trim();
                String cd = txtChuanDoan.getText().trim();
                if (cd.contains("Chẩn đoán bệnh")) cd = ""; 
                strKeDon = " | BS:" + bs + " | CS:" + cs + (cd.isEmpty() ? "" : " | CD:" + cd);
            }
            
            StringBuilder strGifts = new StringBuilder();
            for (int i = 0; i < productModel.getRowCount(); i++) {
                String tenSP = productModel.getValueAt(i, 0).toString();
                if (tenSP.startsWith("[QUÀ TẶNG]")) {
                    String realName = tenSP.replace("[QUÀ TẶNG]", "").trim();
                    String unit = productModel.getValueAt(i, 1).toString();
                    // [FIX]: Lấy số lượng từ cột 3
                    String qty = productModel.getValueAt(i, 3).toString();
                    strGifts.append(" | TANG:").append(realName).append(";").append(qty).append(";").append(unit);
                }
            }
            
            hd.setLoaiHD(Enumeration.LoaiHoaDon.BAN_HANG); 
            hd.setPhuongThucThanhToan(Enumeration.PhuongThucThanhToan.TIEN_MAT);
            StringBuilder strLieuMauNhap = new StringBuilder();
            StringBuilder strBatches = new StringBuilder(); // Dùng để ghi nhớ Lô Hàng

            for (int i = 0; i < productModel.getRowCount(); i++) {
                String tenRow = productModel.getValueAt(i, 0).toString().trim();
                
                // Lấy Lô hàng để lưu vào Ghi chú
                String loHSD = productModel.getValueAt(i, 2).toString().trim();
                if (!loHSD.isEmpty() && !loHSD.equals("Chưa có lô") && !tenRow.startsWith("[LIỀU]")) {
                    String cleanName = tenRow.replace("CHILD_ITEM ", "").replace("[QUÀ TẶNG] ", "").trim();
                    strBatches.append(" | BATCH:").append(cleanName).append("=").append(loHSD);
                }

                if (tenRow.startsWith("CHILD_ITEM ")) {
                    String realName = tenRow.replace("CHILD_ITEM ", "").trim();
                    if (strLieuMauNhap.length() > 0) strLieuMauNhap.append(",");
                    strLieuMauNhap.append(realName);
                }
            }
            String lieuMauPartNhap = strLieuMauNhap.length() > 0 ? " | LIEU_MAU:" + strLieuMauNhap.toString() : "";

            // Gắn Lô hàng vào Ghi chú để sau này khôi phục
            hd.setGhiChu("Lưu nháp" + strKeDon + strGifts.toString() + lieuMauPartNhap + strBatches.toString() + " | VAT_AMT:" + this.vat);
            
            if (this.maKhuyenMaiApDung != null && !this.maKhuyenMaiApDung.isEmpty()) {
                Entity.KhuyenMai km = new Entity.KhuyenMai();
                km.setId(this.maKhuyenMaiApDung.split(",")[0].trim()); 
                hd.setKhuyenMaiId(km);
            } else {
                hd.setKhuyenMaiId(null);
            }

            boolean isHDSaved = false;
            if (this.editingModelRow != -1) {
                isHDSaved = busHD.capNhatHoaDon(hd);
                if (isHDSaved) busCT.xoaChiTietTheoMaHD(maHD); 
            } else {
                isHDSaved = busHD.themHoaDon(hd);
            }
            
            if (isHDSaved) {
                java.util.Map<String, Entity.ChiTietHoaDon> mapGopChiTiet = new java.util.HashMap<>();

                for (int i = 0; i < productModel.getRowCount(); i++) {
                    String tenSP = productModel.getValueAt(i, 0).toString().trim();
                    if (tenSP.startsWith("[QUÀ TẶNG]")) continue;
                    if (tenSP.startsWith("[LIỀU]")) continue; 
                    
                    tenSP = tenSP.replace("CHILD_ITEM ", "").replace("➔", "").replace("↳", "").replace("=>", "").trim();

                    String tenDVT = productModel.getValueAt(i, 1).toString().trim();
                    int soLuong = Integer.parseInt(productModel.getValueAt(i, 3).toString());
                    
                    long donGiaGoc = 0;
                    try { donGiaGoc = Long.parseLong(productModel.getValueAt(i, 4).toString().replaceAll("\\D+", "")); } catch(Exception e) {}
                    
                    String maSP = ""; String maDVT = "";
                    java.util.List<Entity.SanPham> listSP = busSP.traCuuSanPham(tenSP);
                    if (listSP != null) {
                        for(Entity.SanPham sp : listSP) {
                            if(sp.getTen().equalsIgnoreCase(tenSP)) { maSP = sp.getId(); break; }
                        }
                    }
                    
                    java.util.List<Entity.DonViDoLuong> listDV = busDV.getDSTheoTenSP(tenSP);
                    if (listDV != null) {
                        for(Entity.DonViDoLuong dv : listDV) {
                            if(dv.getTen().equalsIgnoreCase(tenDVT)) { maDVT = dv.getId(); break; }
                        }
                    }
                    
                    if (maSP.isEmpty()) continue; 

                    String key = maSP + "_" + maDVT;
                    
                    if (mapGopChiTiet.containsKey(key)) {
                        Entity.ChiTietHoaDon existing = mapGopChiTiet.get(key);
                        existing.setSoLuong(existing.getSoLuong() + soLuong); 
                        existing.setThanhTien((long)(existing.getDonGiaThucTe() * existing.getSoLuong()));
                    } else {
                        Entity.ChiTietHoaDon cthd = new Entity.ChiTietHoaDon();
                        cthd.setHoaDonId(hd);
                        Entity.SanPham sp = new Entity.SanPham(); sp.setId(maSP); cthd.setSanPhamId(sp);
                        if (!maDVT.isEmpty()) {
                            Entity.DonViDoLuong dv = new Entity.DonViDoLuong(); dv.setId(maDVT); cthd.setDonViDoLuongId(dv);
                        }
                        cthd.setSoLuong(soLuong);
                        
                        // FIX: LƯU ĐƠN GIÁ VÀ THÀNH TIỀN ĐỂ KHÔNG BỊ MẤT KHI MỞ LẠI
                        cthd.setDonGiaThucTe((double)donGiaGoc); 
                        cthd.setThanhTien(donGiaGoc * soLuong); 
                        
                        mapGopChiTiet.put(key, cthd); 
                    }
                }
                
                for (Entity.ChiTietHoaDon ct : mapGopChiTiet.values()) {
                    busCT.themCTHD(ct); 
                }
                
                String tongTien = lblTotalPriceValue.getText();
                String ngayStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

                if (editingModelRow != -1) {
                    mainTableModel.setValueAt(maHD, editingModelRow, 0);
                    mainTableModel.setValueAt(ngayStr, editingModelRow, 1);
                    mainTableModel.setValueAt(khach, editingModelRow, 2);  
                    mainTableModel.setValueAt(sdt, editingModelRow, 3);    
                    mainTableModel.setValueAt("Tiền mặt", editingModelRow, 4);   
                    mainTableModel.setValueAt(tongTien, editingModelRow, 5);     
                    mainTableModel.setValueAt("Đang xử lý", editingModelRow, 6); 
                } else {
                    mainTableModel.insertRow(0, new Object[]{maHD, ngayStr, khach, sdt, "Tiền mặt", tongTien, "Đang xử lý", "", "Tất cả"});
                    this.editingModelRow = 0;
                    this.maHDDangSua = maHD;
                }
                
                final String maHoaDonHuy = maHD;
                final javax.swing.table.DefaultTableModel modelBangChinh = mainTableModel;
                
                javax.swing.Timer timerHuyDon = new javax.swing.Timer(1200000, e -> {
                    new Thread(() -> {
                        try {
                            BUS.BUS_HoaDon busHuy = new BUS.BUS_HoaDon();
                            // Dùng layHoaDonTheoMa cho đồng nhất
                            Entity.HoaDon checkHD = busHuy.layHoaDonTheoMa(maHoaDonHuy); 
                            
                            if (checkHD != null && checkHD.getGhiChu() != null && checkHD.getGhiChu().contains("Lưu nháp")) {
                                
                                // GỌI ĐÚNG HÀM HUYHOADON() ĐỂ HỆ THỐNG XỬ LÝ DATABASE VÀ HOÀN KHO
                                boolean updateSuccess = busHuy.huyHoaDon(maHoaDonHuy);
                                
                                if(updateSuccess) {
                                    javax.swing.SwingUtilities.invokeLater(() -> {
                                        if (modelBangChinh != null) {
                                            for (int i = 0; i < modelBangChinh.getRowCount(); i++) {
                                                if (maHoaDonHuy.equals(modelBangChinh.getValueAt(i, 0))) {
                                                    modelBangChinh.setValueAt("Đã hủy", i, 6);
                                                    break;
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                });
                timerHuyDon.setRepeats(false); 
                timerHuyDon.start(); 

                if (!isAutoSave) {
                    showCustomNotification("THÀNH CÔNG", "Đã lưu nháp hóa đơn thành công!\n(Hóa đơn sẽ tự động hủy nếu không thanh toán trong 20 phút tới)", "SUCCESS");
                    
                    // Bổ sung xóa sạch bảng dữ liệu sau khi lưu
                    productModel.setRowCount(0);
                    recalculateTotals();
                    
                    this.dispose(); 
                }
            } else {
                // Bổ sung thông báo nếu lưu DB bị lỗi để form không bị đứng im
                showCustomNotification("LỖI", "Không thể lưu hóa đơn nháp vào hệ thống!", "ERROR");
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
 // Đã đổi tham số cuối thành java.util.List<Object[]> batches
    private JPanel createSuggestionItem(JPopupMenu popup, JTextField txtSearch, String iconType, String name, String unit, String price, String stock, String danhMuc, String vat, java.util.List<Object[]> batches) {
        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(10, 15, 10, 15));
        pnl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        int slKhoiTao = 0;
        for (int i = 0; i < productModel.getRowCount(); i++) {
            if (productModel.getValueAt(i, 0).toString().equals(name) &&
                productModel.getValueAt(i, 1).toString().equals(unit)) {
                slKhoiTao = Integer.parseInt(productModel.getValueAt(i, 3).toString());
                break;
            }
        }

        JPanel pnlLeft = new JPanel(new BorderLayout()); 
        pnlLeft.setOpaque(false);

        String badgeText = "Khác";
        Color bgColor = Color.decode("#F3F4F6"); 
        Color fgColor = Color.decode("#4B5563"); 

        if (danhMuc != null) {
            String dm = danhMuc.toLowerCase();
            if (dm.contains("không kê đơn")) {
                badgeText = "Thuốc thường"; 
                bgColor = Color.decode("#D1FAE5"); fgColor = Color.decode("#059669"); // Xanh lá
            } else if (dm.contains("kê đơn")) {
                badgeText = "Thuốc KĐ"; 
                bgColor = Color.decode("#DBEAFE"); fgColor = Color.decode("#2563EB"); // Xanh dương
            } else if (dm.contains("mỹ phẩm")) {
                badgeText = "Mỹ phẩm"; bgColor = Color.decode("#F3E8FF"); fgColor = Color.decode("#9333EA");
            } else if (dm.contains("chức năng") || dm.contains("tpcn")) {
                badgeText = "TPCN"; bgColor = Color.decode("#FEF3C7"); fgColor = Color.decode("#D97706");
            } else if (dm.contains("vật tư") || dm.contains("y tế")) {
                badgeText = "Vật tư"; bgColor = Color.decode("#E5E7EB"); fgColor = Color.decode("#374151");
            }
        }

        JLabel lblBadge = new JLabel(badgeText, SwingConstants.CENTER);
        lblBadge.setOpaque(true);
        lblBadge.setBackground(bgColor);
        lblBadge.setForeground(fgColor);
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor, 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));

        String htmlName = "<html><div style='max-width: 180px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;'>" 
                        + "<span style='font-weight: bold; font-size: 14px; color: #111827;'>" + name + "</span>"
                        + "&nbsp;<span style='color: #2563EB; font-size: 12px; font-weight: bold;'>(" + unit + ")</span>"
                        + "</div></html>";
        JLabel lblNameInfo = new JLabel(htmlName);
        
        JPanel pnlNameBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlNameBadge.setOpaque(false);
        pnlNameBadge.add(lblBadge);
        pnlNameBadge.add(lblNameInfo);
        
        // --- HIỂN THỊ COMBOBOX LÔ HÀNG NẰM NGANG NHAU ---
        JComboBox<String> cboBatches = new JComboBox<>();
     
        cboBatches.setFont(new Font("Segoe UI", Font.PLAIN, 12)); 
        cboBatches.setPreferredSize(new Dimension(140, 26)); // Ép chiều rộng 140px, cao 26px
        cboBatches.setForeground(Color.decode("#059669"));
        cboBatches.setBackground(Color.WHITE);
        
        if (batches != null && !batches.isEmpty()) {
            for (Object[] b : batches) {
                String lo = (b.length > 7 && b[7] != null && !b[7].toString().trim().isEmpty()) ? b[7].toString() : "N/A";
                String hd = (b.length > 8 && b[8] != null && !b[8].toString().trim().isEmpty()) ? b[8].toString() : "N/A";
                cboBatches.addItem("Lô: " + lo + " - HSD: " + hd);
            }
        }
        pnlNameBadge.add(cboBatches);

        // --- MẸO ĐẶC BIỆT: BẬT TÍNH NĂNG CHỐN SẬP KHI ĐANG CHỌN ITEM TRONG COMBOBOX ---
        cboBatches.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                popup.putClientProperty("dont_close", Boolean.TRUE); // Khóa không cho sụp khung tìm kiếm
            }
            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                // Delay 150ms giải phóng cờ để tránh xung đột Focus hệ thống
                SwingUtilities.invokeLater(() -> {
                    try { Thread.sleep(150); } catch(Exception ex){}
                    popup.putClientProperty("dont_close", Boolean.FALSE);
                });
            }
            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
                popup.putClientProperty("dont_close", Boolean.FALSE);
            }
        });

        // Chặn sự kiện chuột lan truyền từ ComboBox lên Panel nền
        java.awt.event.MouseAdapter stopBubble = new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) { e.consume(); }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { e.consume(); }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { e.consume(); }
        };
        cboBatches.addMouseListener(stopBubble);
        for (Component child : cboBatches.getComponents()) {
            child.addMouseListener(stopBubble);
        }

        pnlLeft.add(pnlNameBadge, BorderLayout.CENTER);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlRight.setOpaque(false);

        JLabel lblCount = new JLabel(slKhoiTao > 0 ? "[" + slKhoiTao + "]" : "");
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCount.setForeground(Color.decode("#E11D48")); 
        lblCount.setPreferredSize(new Dimension(35, 20));
        lblCount.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblPrice = new JLabel(String.format("%,d", Integer.parseInt(price)).replace(',', '.') + "đ");
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPrice.setForeground(Color.decode("#111827"));

        JLabel lblStock = new JLabel("Tổng tồn: " + stock);
        lblStock.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStock.setForeground(Color.decode("#059669")); 

        pnlRight.add(lblCount);
        pnlRight.add(lblPrice);
        pnlRight.add(lblStock);

        pnl.add(pnlLeft, BorderLayout.WEST);
        pnl.add(pnlRight, BorderLayout.EAST);

        pnl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) { pnl.setBackground(Color.decode("#F3F4F6")); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) { pnl.setBackground(Color.WHITE); }
            
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                // --- FIX TOẠ ĐỘ CHUỘT TUYỆT ĐỐI ---
                Point mousePt = SwingUtilities.convertPoint(pnl, e.getPoint(), cboBatches);
                if (cboBatches.contains(mousePt) || cboBatches.isPopupVisible()) {
                    return; // Nếu bấm trúng ComboBox thì dừng lại, nhường quyền cho ComboBox xổ danh sách lô
                }

                boolean isLeftClick = SwingUtilities.isLeftMouseButton(e);
                boolean isRightClick = SwingUtilities.isRightMouseButton(e);

                // LẤY ĐÚNG LÔ HÀNG ĐANG ĐƯỢC CHỌN TRÊN COMBOBOX
                int selectedIndex = cboBatches.getSelectedIndex();
                if (selectedIndex < 0) selectedIndex = 0;
                Object[] selectedBatch = batches.get(selectedIndex);
                
                String loHang = (selectedBatch.length > 7 && selectedBatch[7] != null) ? selectedBatch[7].toString() : "";
                String hsd = (selectedBatch.length > 8 && selectedBatch[8] != null) ? selectedBatch[8].toString() : "";
                
                // --- CẢNH BÁO THUỐC CẬN HẠN (DƯỚI 6 THÁNG HOẶC QUÁ HẠN) ---
                if (isLeftClick && hsd != null && !hsd.trim().isEmpty() && !hsd.equalsIgnoreCase("N/A")) {
                    try {
                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        java.time.LocalDate expiryDate = java.time.LocalDate.parse(hsd.trim(), formatter);
                        java.time.LocalDate today = java.time.LocalDate.now();
                        
                        if (expiryDate.isBefore(today.plusMonths(6))) {
                            String msgWarning = "Sản phẩm \"" + name + "\" thuộc lô này gần hết hạn sử dụng (HSD: " + hsd + "). Bạn vẫn muốn bán?";
                            if (expiryDate.isBefore(today)) {
                                msgWarning = "Sản phẩm \"" + name + "\" thuộc lô này ĐÃ HẾT HẠN SỬ DỤNG (HSD: " + hsd + ")! Bạn có chắc chắn muốn bán?";
                            }
                            boolean confirm = showCustomConfirmDialog("CẢNH BÁO HẠN SỬ DỤNG", msgWarning);
                            if (!confirm) return; 
                        }
                    } catch (Exception ex) {}
                }

                popup.setVisible(false); // Chỉ đóng khung tìm kiếm sau khi add sản phẩm thành công

                String loHsdText = "";
                if (loHang != null && !loHang.isEmpty()) loHsdText = loHang;
                if (!loHsdText.isEmpty() && hsd != null && !hsd.isEmpty()) loHsdText += " — " + hsd;
                else if (loHsdText.isEmpty() && hsd != null && !hsd.isEmpty()) loHsdText = hsd;
                final String finalLoHsdText = loHsdText;

                boolean daTonTai = false;
                int rowIndex = -1;
                int currentQty = 0;
                
                for (int i = 0; i < productModel.getRowCount(); i++) {
                    // Phải trùng Tên, ĐVT và TRÙNG CẢ LÔ HÀNG thì mới cộng dồn
                    if (productModel.getValueAt(i, 0).toString().equals(name) &&
                        productModel.getValueAt(i, 1).toString().equals(unit) &&
                        productModel.getValueAt(i, 2).toString().equals(loHsdText)) { // [THÊM ĐIỀU KIỆN NÀY]
                        daTonTai = true;
                        rowIndex = i;
                        currentQty = Integer.parseInt(productModel.getValueAt(i, 3).toString());
                        break;
                    }
                }
                
                if (isLeftClick) {
                	if (danhMuc != null && danhMuc.toLowerCase().contains("kê đơn") && !danhMuc.toLowerCase().contains("không kê đơn")) {
                        moBangKeDonTuDong();
                    }
                    currentQty++;
                    if (daTonTai) {
                        productModel.setValueAt(String.valueOf(currentQty), rowIndex, 3);
                        long donGia = 0;
                        try { donGia = Long.parseLong(productModel.getValueAt(rowIndex, 4).toString().replaceAll("[^0-9]", "")); } catch(Exception ex){}
                        
                        // SỬA: Đọc KM% từ cột 5, không còn là VAT%
                        double kmValue = 0;
                        try { kmValue = Double.parseDouble(productModel.getValueAt(rowIndex, 5).toString().replace("%", "").trim()) / 100.0; } catch (Exception ex) {}
                        
                        long thanhTien = Math.round(donGia * currentQty * (1.0 - kmValue));
                        productModel.setValueAt(String.format("%,d", thanhTien).replace(',', '.') + "đ", rowIndex, 6);
                    } else {
                        long donGiaChuan = Long.parseLong(price);
                        try {
                            BUS.BUS_DonViDoLuong busDV = new BUS.BUS_DonViDoLuong();
                            java.util.List<Entity.DonViDoLuong> listDV = busDV.getDSTheoTenSP(name); 
                            if (listDV != null) {
                                for (Entity.DonViDoLuong dv : listDV) {
                                    if (dv.getTen().equalsIgnoreCase(unit)) {
                                        donGiaChuan = Math.round(dv.getGia());
                                        break;
                                    }
                                }
                            }
                        } catch (Exception ex) {}

                        String giaFormatted = String.format("%,d", donGiaChuan).replace(',', '.') + "đ";

                        // SỬA: Thành tiền mới = đơn giá * 1 * (1 - 0%) = đơn giá (KM=0% mặc định)
                        // VAT% (tham số vat) được lưu vào cột ẩn (cột 8), không hiển thị
                        String thanhTienFormatted = giaFormatted;

                        productModel.addRow(new Object[]{
                                name, unit, finalLoHsdText, "1", giaFormatted, "0%", thanhTienFormatted, "", vat 
                            });
                    }
                    lblCount.setText("[" + currentQty + "]"); 
                    txtSearch.setText(""); 
                    
                } else if (isRightClick) {
                    if (daTonTai) {
                        currentQty--;
                        if (currentQty > 0) {
                            productModel.setValueAt(String.valueOf(currentQty), rowIndex, 3);
                            long donGia = 0;
                            try { donGia = Long.parseLong(productModel.getValueAt(rowIndex, 4).toString().replaceAll("[^0-9]", "")); } catch(Exception ex){}
                            // SỬA: Đọc KM% từ cột 5
                            double kmValue = 0;
                            try { kmValue = Double.parseDouble(productModel.getValueAt(rowIndex, 5).toString().replace("%", "").trim()) / 100.0; } catch (Exception ex) {}
                            long thanhTien = Math.round(donGia * currentQty * (1.0 - kmValue));
                            productModel.setValueAt(String.format("%,d", thanhTien).replace(',', '.') + "đ", rowIndex, 6);
                            lblCount.setText("[" + currentQty + "]"); 
                        } else {
                            productModel.removeRow(rowIndex); 
                            lblCount.setText(""); 
                        }
                    }
                }
                recalculateTotals(); 
            }
        });

        return pnl;
    }
    
    private JPanel createVoucherTag(String tenTag, String maKM, JTextField txtInput) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        pnl.setName(maKM.trim()); 
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E5E7EB"), 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
        pnl.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblText = new JLabel(tenTag);
        lblText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblText.setForeground(Color.decode("#4B5563"));

        pnl.add(lblText);

        pnl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Kiểm tra trước khi cho phép gắn màu xanh
                
                if (!kiemTraHopLeKhuyenMai(maKM.trim())) {
                    maKhuyenMaiApDung = "";
                    tienGiamGia = 0;
                    recalculateTotals();
                    return; 
                }
                if (txtInput != null) {
                    txtInput.setText(maKM.trim());
                    txtInput.setForeground(Color.decode("#10B981")); 
                }
                
                maKhuyenMaiApDung = maKM.trim();
                recalculateTotals(); 
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                String currentKM = maKhuyenMaiApDung == null ? "" : maKhuyenMaiApDung.trim();
                if (!maKM.trim().equalsIgnoreCase(currentKM)) {
                    pnl.setBackground(Color.decode("#F3F4F6"));
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                String currentKM = maKhuyenMaiApDung == null ? "" : maKhuyenMaiApDung.trim();
                if (!maKM.trim().equalsIgnoreCase(currentKM)) {
                    pnl.setBackground(Color.WHITE);
                }
            }
        });

        return pnl;
    }
    
    private String phatSinhMaHoaDon() {
        BUS.BUS_HoaDon busHD = new BUS.BUS_HoaDon();
        return busHD.phatSinhMaHoaDonTuDong();
    }
    private void updateVoucherTagsUI() {
        if (pnlVoucherTags == null) return;
        
        // 1. Tách danh sách các mã đang được áp dụng (VD: "KM01, KM02" -> ["KM01", "KM02"])
        java.util.List<String> listApplied = new java.util.ArrayList<>();
        if (this.maKhuyenMaiApDung != null && !this.maKhuyenMaiApDung.isEmpty()) {
            for (String s : this.maKhuyenMaiApDung.split(",")) {
                listApplied.add(s.trim().toUpperCase()); // Thêm toUpperCase để check cho chuẩn
            }
        }
        
        for (Component rowComp : pnlVoucherTags.getComponents()) {
            if (rowComp instanceof JPanel) {
                JPanel currentRow = (JPanel) rowComp;
                
                // Quét từng thẻ trong 1 hàng
                for (Component comp : currentRow.getComponents()) {
                    if (comp instanceof JPanel) {
                        JPanel pnl = (JPanel) comp;
                        String maKMTag = pnl.getName(); 
                        
                        if (maKMTag != null && pnl.getComponentCount() > 0) {
                            maKMTag = maKMTag.trim().toUpperCase();
                            JLabel lblText = (JLabel) pnl.getComponent(0);
                            
                            // Nếu mã đang chọn -> Tô màu xanh
                            if (listApplied.contains(maKMTag)) {
                                pnl.setBackground(Color.decode("#D1FAE5")); 
                                pnl.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(Color.decode("#10B981"), 1, true), 
                                    new javax.swing.border.EmptyBorder(4, 8, 4, 8)
                                ));
                                lblText.setForeground(Color.decode("#047857")); 
                                lblText.setFont(new Font("Segoe UI", Font.BOLD, 12));
                            } 
                            // Nếu không chọn -> Về màu xám
                            else {
                                pnl.setBackground(Color.WHITE);
                                pnl.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(Color.decode("#E5E7EB"), 1, true),
                                    new javax.swing.border.EmptyBorder(4, 8, 4, 8)
                                ));
                                lblText.setForeground(Color.decode("#4B5563"));
                                lblText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                            }
                        }
                    }
                }
            }
        }
        pnlVoucherTags.revalidate();
        pnlVoucherTags.repaint();
    }
    private JPanel createExactMoneyCell() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        // Đổi viền sang màu Xanh biển đậm (Indigo/Blue)
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#2563EB"), 1, true),
            new EmptyBorder(8, 10, 8, 10) 
        ));
        pnl.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblExactValue = new JLabel("0đ");
        lblExactValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblExactValue.setForeground(Color.decode("#2563EB")); // Chữ màu xanh

        JLabel lblTextRight = new JLabel("");
        lblTextRight.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTextRight.setForeground(Color.decode("#1E3A8A")); // Chữ xanh đen đậm
        
        pnl.add(lblExactValue, BorderLayout.WEST);
        pnl.add(lblTextRight, BorderLayout.EAST);

        java.awt.event.MouseAdapter clickAdapter = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) { pnl.setBackground(Color.decode("#EFF6FF")); } // Hover xanh nhạt
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) { pnl.setBackground(Color.WHITE); }
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    pnl.setBackground(Color.decode("#DBEAFE")); 
                    for (int[] c : listCounters) c[0] = 0;
                    for (JLabel l : listCountLabels) l.setText("0");
                    tongTienMat = tongHoaDon;
                    capNhatTongTien();
                }
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) { pnl.setBackground(Color.decode("#EFF6FF")); }
        };

        pnl.addMouseListener(clickAdapter);
        lblExactValue.addMouseListener(clickAdapter);
        lblTextRight.addMouseListener(clickAdapter);

        return pnl;
    }
    public void loadDuLieuHoaDonNhap(String maHoaDon) {
        this.maHDDangSua = maHoaDon;
        
        SwingUtilities.invokeLater(() -> {
            productModel.setRowCount(0); 
            try {
                BUS.BUS_ChiTietHoaDon busCT = new BUS.BUS_ChiTietHoaDon();
                java.util.List<Object[]> dsMonHang = busCT.layDuLieuDoiTra(maHoaDon);
                
                for (Object[] row : dsMonHang) {
                    Object[] newRow = new Object[10]; 
                    newRow[0] = row.length > 0 && row[0] != null ? row[0] : ""; 
                    newRow[1] = row.length > 1 && row[1] != null ? row[1] : ""; 
                    newRow[3] = row.length > 2 && row[2] != null ? row[2] : "1"; 
                    
                    String giaTien = "0đ";
                    if (row.length > 3 && row[3] != null) {
                        String valStr = row[3].toString();
                        if (!valStr.contains("đ")) {
                            try { giaTien = String.format("%,d", (long)Double.parseDouble(valStr)).replace(',', '.') + "đ"; } catch(Exception e) {}
                        } else giaTien = valStr;
                    }
                    newRow[4] = giaTien; 
                    newRow[5] = "0%"; 
                    
                    // FIX: TẢI ĐÚNG VAT VÀ LÔ FEFO MẶC ĐỊNH
                    String vatStr = "0%";
                    String loHsdText = "Chưa có lô";
                    try {
                        BUS.BUS_SanPham busSP = new BUS.BUS_SanPham();
                        java.util.List<Object[]> ketQua = busSP.timKiemSanPhamBan(newRow[0].toString());
                        if (ketQua != null && !ketQua.isEmpty()) {
                            Object[] firstItem = ketQua.get(0);
                            if (firstItem.length > 6 && firstItem[6] != null) {
                                String rawVat = firstItem[6].toString().trim();
                                double v = Double.parseDouble(rawVat);
                                if (v > 0 && v < 1) v = v * 100;
                                vatStr = (int)v + "%";
                            }
                            String loHang = (firstItem.length > 7 && firstItem[7] != null) ? firstItem[7].toString() : "";
                            String hsd = (firstItem.length > 8 && firstItem[8] != null) ? firstItem[8].toString() : "";
                            if (!loHang.isEmpty()) loHsdText = loHang + (!hsd.isEmpty() ? " — " + hsd : "");
                            else if (!hsd.isEmpty()) loHsdText = hsd;
                        }
                    } catch(Exception e) {}
                    
                    newRow[2] = loHsdText; 
                    newRow[8] = vatStr; 
                    
                    long thanhTien = 0;
                    try {
                        int sl = Integer.parseInt(newRow[3].toString());
                        long dg = Long.parseLong(newRow[4].toString().replaceAll("\\D+", ""));
                        thanhTien = dg * sl; 
                    } catch(Exception e) {}
                    
                    newRow[6] = String.format("%,d", thanhTien).replace(',', '.') + "đ"; 
                    newRow[7] = ""; 
                    newRow[9] = "1";    
                    
                    productModel.addRow(newRow);
                }
                
                BUS.BUS_HoaDon busHD = new BUS.BUS_HoaDon();
                Entity.HoaDon hdGoc = busHD.layHoaDonTheoMa(maHoaDon);
                
                if (hdGoc != null) {
                    String ghiChu = hdGoc.getGhiChu();
                    // FIX: Bỏ điều kiện ghiChu.contains("| BATCH:") để nó đọc được cả thông tin Kê đơn
                    if (ghiChu != null) {
                        String[] parts = ghiChu.split("\\|");
                        for(String p : parts) {
                            String pTrim = p.trim();
                            if(pTrim.startsWith("BATCH:")) {
                                String bData = pTrim.substring(6); 
                                int eqIndex = bData.indexOf("=");
                                if(eqIndex != -1) {
                                    String bName = bData.substring(0, eqIndex);
                                    String bLot = bData.substring(eqIndex + 1);
                                    for(int i = 0; i < productModel.getRowCount(); i++) {
                                        String rName = productModel.getValueAt(i,0).toString().replace("CHILD_ITEM ", "").replace("[QUÀ TẶNG] ", "").trim();
                                        if(rName.equals(bName)) {
                                            productModel.setValueAt(bLot, i, 2);
                                            break;
                                        }
                                    }
                                }
                            }
                            // --- PHẦN KHÔI PHỤC KÊ ĐƠN ---
                            else if (pTrim.startsWith("BS:")) {
                                txtBacSi.setText(pTrim.substring(3).trim());
                                txtBacSi.setForeground(Color.BLACK);
                                if (pnlDonThuoc != null) pnlDonThuoc.setVisible(true);
                                if (btnToggleKeDon != null) {
                                    btnToggleKeDon.setText(" Đã thêm thông tin kê đơn");
                                    btnToggleKeDon.setBackground(Color.decode("#FEE2E2"));
                                    btnToggleKeDon.setForeground(Color.decode("#DC2626"));
                                }
                            } else if (pTrim.startsWith("CS:")) {
                                txtCoSo.setText(pTrim.substring(3).trim());
                                txtCoSo.setForeground(Color.BLACK);
                            } else if (pTrim.startsWith("CD:")) {
                                txtChuanDoan.setText(pTrim.substring(3).trim());
                                txtChuanDoan.setForeground(Color.BLACK);
                            }
                        }
                    }
                    
                    if (hdGoc.getKhachHangId() != null && hdGoc.getKhachHangId().getId() != null) {
                        BUS.BUS_KhachHang busKH = new BUS.BUS_KhachHang();
                        Entity.KhachHang kh = busKH.timKhachHangTheoMa(hdGoc.getKhachHangId().getId());
                        
                        if (kh != null) {
                            this.isCustomerLinked = true;
                            this.linkedTenKH = kh.getHoVaTen();
                            this.linkedSdtKH = kh.getSdt();
                            
                            this.diemHienTaiKH = kh.getDiemTichLuy();

                            lblLinkedAvatar.setText(kh.getHoVaTen().substring(0, 1).toUpperCase());
                            lblLinkedName.setText(kh.getHoVaTen());
                            lblLinkedSub.setText(kh.getId() + " • " + kh.getSdt());
                            lblLinkedPoints.setText(kh.getDiemTichLuy() + " điểm"); 
                            
                            long tienQuyDoi = this.diemHienTaiKH * 100L;
                            lblLinkedMoney.setText("≈ " + String.format("%,d", tienQuyDoi).replace(',', '.') + "đ");

                            if (this.diemHienTaiKH > 0) {
                                lblDungDiemText.setText(String.format("Dùng %,d điểm", this.diemHienTaiKH) + " (-" + String.format("%,d", tienQuyDoi).replace(',', '.') + "đ)");
                                pnlDungDiem.setVisible(true);
                            } else pnlDungDiem.setVisible(false);
                            
                            toggleDungDiem.setOn(false);
                            isDungDiem = false;
                            
                            if (pnlInputFields != null) pnlInputFields.setVisible(false); 
                            if (pnlLinkedCustomer != null) pnlLinkedCustomer.setVisible(true); 
                            
                            if(lblBadgeLe != null && lblBadgeLe.getParent() != null) {
                                CardLayout cl = (CardLayout)(lblBadgeLe.getParent().getLayout());
                                cl.show(lblBadgeLe.getParent(), "LINKED");
                            }
                        }
                    } else {
                        this.isCustomerLinked = false;
                        if (pnlInputFields != null) pnlInputFields.setVisible(true);
                        if (pnlLinkedCustomer != null) pnlLinkedCustomer.setVisible(false);
                        
                        if(lblBadgeLe != null && lblBadgeLe.getParent() != null) {
                            CardLayout cl = (CardLayout)(lblBadgeLe.getParent().getLayout());
                            cl.show(lblBadgeLe.getParent(), "LE");
                        }
                        
                        String savedPhoneOrName = txtPhone.getText().trim();
                        if (!savedPhoneOrName.isEmpty() && !savedPhoneOrName.equals("Số điện thoại (tuỳ chọn)")) {
                            txtSearch.setText(savedPhoneOrName);
                            txtSearch.setForeground(Color.BLACK);
                        }
                    }
                    
                    if (hdGoc.getKhuyenMaiId() != null) {
                        txtVoucherInput.setText(hdGoc.getKhuyenMaiId().getId());
                        this.maKhuyenMaiApDung = hdGoc.getKhuyenMaiId().getId(); 
                    }
                    if (hdGoc.getNgayLapHD() != null) {
                        long giayDaQua = java.time.temporal.ChronoUnit.SECONDS.between(hdGoc.getNgayLapHD(), java.time.LocalDateTime.now());
                        int giayConLai = 1200 - (int) giayDaQua; 
                        khoiDongDongHoHuyDon(giayConLai); 
                    } else khoiDongDongHoHuyDon(1200);
                }
                
                this.revalidate();
                this.repaint();
                recalculateTotals(); 
                
            } catch (Exception e) {
                e.printStackTrace();
                showCustomNotification("LỖI", "Lỗi khi tải hóa đơn nháp: " + e.getMessage(), "ERROR");
            } 
        }); 
    }
    
    private void tuDongApDungKhuyenMai() {
        isTableUpdating = true; 
        for (int i = productModel.getRowCount() - 1; i >= 0; i--) {
            String ten = productModel.getValueAt(i, 0).toString();
            if (ten.startsWith("[QUÀ TẶNG]")) productModel.removeRow(i);
        }
        isTableUpdating = false;

        long tongTienBill = this.tamTinh + this.vat;
        int tongSoLuongSP_ThucTe = 0;
        
        for (int i = 0; i < productModel.getRowCount(); i++) {
            try {
                String tenSp = productModel.getValueAt(i, 0).toString();
                // [FIX]: Lấy số lượng từ cột 3
                Object slObj = productModel.getValueAt(i, 3);
                if (slObj != null && !tenSp.startsWith("[QUÀ TẶNG]")) {
                    tongSoLuongSP_ThucTe += Integer.parseInt(slObj.toString().trim());
                }
            } catch (Exception e) {}
        }

        if (tongTienBill == 0 && tongSoLuongSP_ThucTe == 0) {
            this.tienGiamGia = 0;
            this.maKhuyenMaiApDung = "";
            resetVoucherUI();
            updateVoucherTagsUI(); 
            if (lblTotalItems != null) lblTotalItems.setText("Tổng sản phẩm: 0");
            return; 
        }

        long tongTienGiamDoc = 0;
        java.util.List<Object[]> danhSachQuaTang = new java.util.ArrayList<>();
        java.util.List<String> danhSachMaDaDuyet = new java.util.ArrayList<>();
        java.util.Set<String> processedPromoIds = new java.util.HashSet<>();

        BUS.BUS_KhuyenMai busKM = new BUS.BUS_KhuyenMai();
        java.util.List<Object[]> dsKhuyenMai = busKM.layDanhSachKhuyenMaiHopLe();
        
        long maxTienGiam = 0;
        String bestMaKM_GiamGia = "";

        for (Object[] row : dsKhuyenMai) {
            String maKM = row[0] != null ? row[0].toString().trim() : "";
            if (processedPromoIds.contains(maKM)) continue;

            String loaiKM = row[1] != null ? row[1].toString().toUpperCase() : "";
            double giaTriGiam = (double) row[2];
            long donToiThieu = 0;
            int slYeuCau = (int) row[5];
            
            String loaiDK = row[4] != null ? row[4].toString() : "";
            if ("SO_LUONG".equals(loaiDK)) {
                if (slYeuCau <= 0) slYeuCau = (int) ((double) row[3]);
            } else {
                donToiThieu = (long) ((double) row[3]);
            }
            
            String spYeuCau = row[6] != null ? row[6].toString().trim() : "";
            String dvdlYeuCau = row[7] != null ? row[7].toString().trim() : ""; 
            
            int slTang = (int) row[8];
            String spTang = row[9] != null ? row[9].toString().trim() : "";
            String dvdlTang = row[10] != null ? row[10].toString().trim() : "";

            if (spYeuCau.isEmpty() && !spTang.isEmpty() && loaiKM.contains("SAN_PHAM_KEM_THEO")) spYeuCau = spTang;

            boolean duDieuKien = false;
            long soLuongTangThucTe = 0;
            String unitToGive = dvdlTang;

         // --- FIX ĐƠN VỊ TÍNH QUÀ TẶNG (CHUẨN 3 LỚP) ---
            if (!spYeuCau.isEmpty()) {
                int slSanPhamYeuCauThucTeCoBan = 0; // Tính theo Đơn vị cơ bản
                String tenDonViCoBan = "Viên"; // Mặc định
                
                // Khởi tạo BUS để gọi
                BUS.BUS_DonViDoLuong busDVDL = new BUS.BUS_DonViDoLuong();

                for (int i = 0; i < productModel.getRowCount(); i++) {
                    String tenSpTrongBang = productModel.getValueAt(i, 0).toString();
                    String dvtTrongBang = productModel.getValueAt(i, 1).toString();
                    
                    if (!tenSpTrongBang.startsWith("[QUÀ TẶNG]") && tenSpTrongBang.toLowerCase().contains(spYeuCau.toLowerCase())) {
                        int slTrenBang = Integer.parseInt(productModel.getValueAt(i, 3).toString());
                        
                        // 1. Lấy hệ số quy đổi qua BUS (Ví dụ: Hộp = 10, Viên = 1)
                        int heSo = busDVDL.layHeSoQuyDoi(tenSpTrongBang, dvtTrongBang);
                        slSanPhamYeuCauThucTeCoBan += (slTrenBang * heSo); // Cộng dồn số lượng theo đơn vị nhỏ nhất
                        
                        // 2. Tra cứu tên Đơn vị cơ bản qua BUS
                        tenDonViCoBan = busDVDL.layTenDonViCoBan(tenSpTrongBang);
                    }
                }
                
                // 3. Quy đổi số lượng YÊU CẦU ra Đơn vị cơ bản
                int heSoYeuCau = 1;
                if (!dvdlYeuCau.isEmpty()) {
                    String spChuanDeTra = spYeuCau;
                    for (int i = 0; i < productModel.getRowCount(); i++) {
                        String ten = productModel.getValueAt(i, 0).toString();
                        if (!ten.startsWith("[QUÀ TẶNG]") && ten.toLowerCase().contains(spYeuCau.toLowerCase())) {
                            spChuanDeTra = ten; break;
                        }
                    }
                    heSoYeuCau = busDVDL.layHeSoQuyDoi(spChuanDeTra, dvdlYeuCau);
                }
                int slYeuCauCoBan = slYeuCau * heSoYeuCau;

                // 4. Xét duyệt điều kiện
                if (slYeuCauCoBan > 0 && slSanPhamYeuCauThucTeCoBan >= slYeuCauCoBan) {
                    duDieuKien = true;
                    
                    int soLanDat = slSanPhamYeuCauThucTeCoBan / slYeuCauCoBan;

                    if (!dvdlTang.isEmpty()) {
                        unitToGive = dvdlTang;
                    } else {
                        unitToGive = tenDonViCoBan; // TẶNG THEO ĐƠN VỊ NHỎ NHẤT (Viên)
                    }
                    soLuongTangThucTe = soLanDat * (slTang > 0 ? slTang : 1);
                }
            } else {
                // Tương tự cho phần if (slYeuCau > 0)
                BUS.BUS_DonViDoLuong busDVDL = new BUS.BUS_DonViDoLuong();
                if (slYeuCau > 0 && tongSoLuongSP_ThucTe >= slYeuCau) {
                    duDieuKien = true;
                    soLuongTangThucTe = (tongSoLuongSP_ThucTe / slYeuCau) * (slTang > 0 ? slTang : 1);
                    if (unitToGive.isEmpty() && !spTang.isEmpty()) unitToGive = busDVDL.layTenDonViCoBan(spTang);
                    else if (unitToGive.isEmpty()) unitToGive = "Cái";
                } else if (donToiThieu > 0 && tongTienBill >= donToiThieu) {
                    duDieuKien = true;
                    soLuongTangThucTe = (slTang > 0 ? slTang : 1);
                    if (unitToGive.isEmpty() && !spTang.isEmpty()) unitToGive = busDVDL.layTenDonViCoBan(spTang);
                    else if (unitToGive.isEmpty()) unitToGive = "Cái";
                } else if (slYeuCau == 0 && donToiThieu == 0) {
                    duDieuKien = true; 
                    soLuongTangThucTe = (slTang > 0 ? slTang : 1);
                    if (unitToGive.isEmpty() && !spTang.isEmpty()) unitToGive = busDVDL.layTenDonViCoBan(spTang);
                    else if (unitToGive.isEmpty()) unitToGive = "Cái";
                }
            }

            if (duDieuKien) {
                if (loaiKM.contains("SAN_PHAM_KEM_THEO") || loaiKM.contains("TANG")) {
                    processedPromoIds.add(maKM);
                    danhSachMaDaDuyet.add(maKM);
                    if (soLuongTangThucTe > 0 && !spTang.isEmpty()) {
                        boolean daGop = false;
                        for (Object[] q : danhSachQuaTang) {
                            if (q[0].toString().equals("[QUÀ TẶNG] " + spTang) && q[1].toString().equals(unitToGive)) {
                                // [FIX]: Cập nhật quà tặng ở cột 3
                                long oldSL = Long.parseLong(q[3].toString());
                                q[3] = String.valueOf(oldSL + soLuongTangThucTe);
                                daGop = true; break;
                            }
                        }
                        if (!daGop) {
                            // [FIX]: Chèn thêm cột Lô/HSD trống để bảng không bị xô lệch
                        	danhSachQuaTang.add(new Object[]{
                        		    "[QUÀ TẶNG] " + spTang, 
                        		    unitToGive, 
                        		    "", // Cột Lô/HSD
                        		    String.valueOf(soLuongTangThucTe), 
                        		    "0đ", "0%", "0đ", "", "0%", "1"
                        		});
                        }
                    }
                } else {
                    long tienGiamTamTinh = 0;
                    if (loaiKM.contains("PHAN_TRAM") || loaiKM.contains("%")) {
                        tienGiamTamTinh = (long) (this.tamTinh * (giaTriGiam / 100.0)); // [FIX]: Giảm giá áp lên giá CHƯA VAT (theo luật thuế VN)
                    } else tienGiamTamTinh = (long) giaTriGiam;

                    if (tienGiamTamTinh > maxTienGiam) {
                        maxTienGiam = tienGiamTamTinh;
                        bestMaKM_GiamGia = maKM;
                    }
                }
            }
        }
        
        if (maxTienGiam > 0 && !bestMaKM_GiamGia.isEmpty()) {
            tongTienGiamDoc = maxTienGiam; 
            danhSachMaDaDuyet.add(bestMaKM_GiamGia); 
        }

        isTableUpdating = true; 
        for (Object[] rowQuaTang : danhSachQuaTang) productModel.addRow(rowQuaTang);
        isTableUpdating = false;

        this.tienGiamGia = tongTienGiamDoc;
        this.maKhuyenMaiApDung = String.join(", ", danhSachMaDaDuyet);

        if (!danhSachMaDaDuyet.isEmpty()) {
            txtVoucherInput.setText(this.maKhuyenMaiApDung + " (Đã áp dụng " + danhSachMaDaDuyet.size() + " mã)");
            txtVoucherInput.setBackground(java.awt.Color.decode("#DCFCE7"));
            txtVoucherInput.setForeground(java.awt.Color.decode("#059669"));
            txtVoucherInput.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(java.awt.Color.decode("#10B981"), 2),
                new javax.swing.border.EmptyBorder(0, 10, 0, 10)
            ));
        } else {
            this.tienGiamGia = 0;
            resetVoucherUI();
        }
        
        int totalSp = tongSoLuongSP_ThucTe;
        // [FIX]: Lấy số lượng từ cột 3
        for (Object[] q : danhSachQuaTang) totalSp += Integer.parseInt(q[3].toString());
        if (lblTotalItems != null) lblTotalItems.setText(String.format("Tổng sản phẩm: %d", totalSp));

        // Lấy kmRatio từ BUS (không tự tính trong GUI)
        BUS.BUS_HoaDon busHDTmp = new BUS.BUS_HoaDon();
        java.util.List<long[]> dsTmp = new java.util.ArrayList<>();
        for (int i = 0; i < productModel.getRowCount(); i++) {
            try {
                String ten = productModel.getValueAt(i, 0).toString();
                if (ten.startsWith("[QUÀ TẶNG]")) continue;
                int sl = Integer.parseInt(productModel.getValueAt(i, 3).toString().trim());
                long dg = Long.parseLong(productModel.getValueAt(i, 4).toString().replaceAll("[^0-9]", ""));
                long vat = Long.parseLong(productModel.getValueAt(i, 8).toString().replace("%","").trim());
                dsTmp.add(new long[]{sl, dg, vat});
            } catch (Exception ignored) {}
        }
        BUS.BUS_HoaDon.KetQuaHoaDon kqTmp = busHDTmp.tinhToanTienHoaDon(dsTmp, false, 0, this.tienGiamGia);
        double kmRatioDisplay = kqTmp.kmRatioGlobal;
        String kmPercentStr = (kmRatioDisplay > 0)
            ? String.format("%.0f%%", kmRatioDisplay * 100.0)
            : "0%";

        isTableUpdating = true;
        isTableUpdating = true;
        try {
            for (int i = 0; i < productModel.getRowCount(); i++) {
                String ten = productModel.getValueAt(i, 0).toString();
                
                if (ten.startsWith("[QUÀ TẶNG]")) continue;
                
                // THÊM DÒNG NÀY ĐỂ TRÁNH LỖI ĐƠ BẢNG: Bỏ qua dòng Tiêu đề Liều
                if (ten.startsWith("[LIỀU]")) continue; 

                productModel.setValueAt(kmPercentStr, i, 5); // Cập nhật KM%
                
                // Thành tiền hiển thị = SL × donGia × (1 - KM%) — chưa VAT
                int sl = Integer.parseInt(productModel.getValueAt(i, 3).toString().trim());
                long dg = Long.parseLong(productModel.getValueAt(i, 4).toString().replaceAll("[^0-9]", ""));
                long thanhTienDisplay = Math.round(sl * dg * (1.0 - kmRatioDisplay));
                productModel.setValueAt(String.format("%,d", thanhTienDisplay).replace(',', '.') + "đ", i, 6);
            }
        } finally {
            isTableUpdating = false;
        }

        updateVoucherTagsUI(); 
    }

    // Hàm phụ trợ Reset UI ô nhập mã
    private void resetVoucherUI() {
        if (txtVoucherInput.getText().contains("(Đã áp dụng")) {
            txtVoucherInput.setText("NHẬP MÃ HOẶC CHỌN BÊN DƯỚI...");
        }
        txtVoucherInput.setBackground(Color.WHITE);
        txtVoucherInput.setForeground(Color.GRAY);
        txtVoucherInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1),
            new javax.swing.border.EmptyBorder(0, 10, 0, 10)
        ));
        isTableUpdating = true;
        try {
            for (int i = 0; i < productModel.getRowCount(); i++) {
                String ten = productModel.getValueAt(i, 0).toString();
                if (ten.startsWith("[QUÀ TẶNG]")) continue;
                productModel.setValueAt("0%", i, 5);
                int sl = Integer.parseInt(productModel.getValueAt(i, 3).toString().trim());
                long dg = Long.parseLong(productModel.getValueAt(i, 4).toString().replaceAll("[^0-9]", ""));
                productModel.setValueAt(String.format("%,d", sl * dg).replace(',', '.') + "đ", i, 6);
            }
        } finally {
            isTableUpdating = false;
        }
    }
    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(130, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
    }
    private long layTienMatTrongKetHienTai() {
        Utils.UserSession session = Utils.UserSession.getInstance();
        if (session.getCaHienTai() == null) return Long.MAX_VALUE; // Cho phép đi qua nếu Quản lý không có ca
        
        long tienDauCa = (long) session.getCaHienTai().getTienDauCa();
        long doanhThuTienMat = 0;
        
        try {
            BUS.BUS_HoaDon busHD = new BUS.BUS_HoaDon();
            String maNV = session.getMaNhanVien();
            java.time.LocalDateTime thoiGianBatDau = session.getCaHienTai().getThoiGianBatDau();
            
            // Gọi qua BUS thay vì thao tác DB trực tiếp
            doanhThuTienMat = busHD.tinhDoanhThuTienMatCaHienTai(maNV, thoiGianBatDau);
            
        } catch (Exception ex) {
            System.err.println("Lỗi lấy tiền trong két: " + ex.getMessage());
        }
        
        return tienDauCa + doanhThuTienMat;
    }
    private boolean kiemTraHopLeKhuyenMai(String maKM) {
        long tongTienDK = tamTinh + vat;
        java.util.List<Object[]> dsSP = new java.util.ArrayList<>();
        
        for (int i = 0; i < productModel.getRowCount(); i++) {
            dsSP.add(new Object[] {
                productModel.getValueAt(i, 0).toString(),
                productModel.getValueAt(i, 1).toString(),
                Integer.parseInt(productModel.getValueAt(i, 3).toString()) 
            });
        }

        BUS.BUS_KhuyenMai busKM = new BUS.BUS_KhuyenMai();
        BUS.BUS_KhuyenMai.PromoValidationResult result = busKM.kiemTraHopLePromotion(maKM, tongTienDK, dsSP);

        if (!result.isValid) {
            showCustomNotification("CHƯA ĐỦ ĐIỀU KIỆN", result.message, "WARNING");
            return false;
        }

        this.tienGiamGia = result.discountAmount;
        return true;
    }
    private void initQuickAddProductTable() {
        String[] columns = {"  ", "Tên sản phẩm", "ĐVT", "Giá", "Mã vạch", "Thêm"};
        quickAddProductModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 5; }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Icon.class;
                return super.getColumnClass(columnIndex);
            }
        };

        quickAddProductTable = new JTable(quickAddProductModel);
        
        quickAddProductTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setIcon(new MenuIcon("PACKAGE")); // Thay bằng Pill_Icon nếu bạn có
                setText(""); 
                return this;
            }
        });
        
        quickAddProductTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        quickAddProductTable.setRowHeight(45);
        quickAddProductTable.setShowGrid(false);
        quickAddProductTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        quickAddProductTable.getTableHeader().setBackground(Color.decode("#F1F5F9"));
        quickAddProductTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        quickAddProductTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); 
        quickAddProductTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); 
        quickAddProductTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); 

        quickAddProductTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JButton btn = new JButton("+");
                btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
                btn.setForeground(Color.decode("#152A4B"));
                btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
                return btn;
            }
        });

        quickAddProductTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = quickAddProductTable.rowAtPoint(e.getPoint());
                int col = quickAddProductTable.columnAtPoint(e.getPoint());
                if (col == 5 && row >= 0) { 
                    String tenSP = quickAddProductTable.getValueAt(row, 1).toString();
                    String dvt = quickAddProductTable.getValueAt(row, 2).toString();
                    long giaBan = Long.parseLong(quickAddProductTable.getValueAt(row, 3).toString().replace("đ", "").replace(".", ""));
                    addOneProductToMainTable(tenSP, dvt, giaBan);
                }
            }
        });

        quickAddProductTable.getColumnModel().getColumn(0).setPreferredWidth(40); 
        quickAddProductTable.getColumnModel().getColumn(1).setPreferredWidth(200); 
        quickAddProductTable.getColumnModel().getColumn(2).setPreferredWidth(60); 
        quickAddProductTable.getColumnModel().getColumn(3).setPreferredWidth(90); 
        quickAddProductTable.getColumnModel().getColumn(4).setPreferredWidth(100); 
        quickAddProductTable.getColumnModel().getColumn(5).setPreferredWidth(60); 
    }

    private void loadAllProductsToQuickAdd() {
        if (quickAddProductModel == null || pnlQuickAddContent == null) return;
        quickAddProductModel.setRowCount(0); 
        try {
            BUS.BUS_SanPham busSP = new BUS.BUS_SanPham();
            java.util.List<Object[]> ketQua = busSP.timKiemSanPhamBan(""); 
            if (ketQua != null && !ketQua.isEmpty()) {
                for (Object[] rowData : ketQua) {
                    String tenSP = rowData[1] != null ? rowData[1].toString() : "";
                    String dvt = rowData[4] != null ? rowData[4].toString() : "Viên";
                    long giaBan = rowData[6] != null ? (long) Double.parseDouble(rowData[6].toString()) : 0;
                    String maVach = rowData[8] != null ? rowData[8].toString() : "";
                    quickAddProductModel.addRow(new Object[]{ "", tenSP, dvt, String.format("%,d", giaBan).replace(',', '.') + "đ", maVach, "+" });
                }
            }
        } catch(Exception e) { e.printStackTrace(); }
    }

    private void addOneProductToMainTable(String tenSP, String dvt, long giaBan) {
        try {
            BUS.BUS_SanPham busSP = new BUS.BUS_SanPham();
            
            // Khởi tạo gốc là 0% thay vì 5%
            String thueVatStr = "0%"; 
            try {
                // Lấy chính xác VAT từ hàm đã viết trong DAO_SanPham
                double vatVal = busSP.layThueVATTheoTenSP(tenSP); 
                if (vatVal > 0) {
                    if (vatVal < 1) vatVal = vatVal * 100; // Quy đổi 0.05 thành 5
                    thueVatStr = (int) vatVal + "%";
                }
            } catch(Exception ex) {}

            themMotThuocTuLieuVaoBang(tenSP, dvt, 1, giaBan, thueVatStr, null);
            recalculateTotals();
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    private void hienThiPopupChonLoKhiQuet(java.util.List<Object[]> danhSachLo, JPopupMenu suggestionPopup, JTextField txtSearchProduct) {
        JDialog dialog = new JDialog(this, "Hệ thống phát hiện sản phẩm có nhiều lô hàng", true);
        dialog.setSize(750, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 10));
        dialog.getContentPane().setBackground(Color.WHITE);

        String tenSP = danhSachLo.get(0)[1] != null ? danhSachLo.get(0)[1].toString() : "Sản phẩm"; 

     // 1. Tạo Label với chữ được căn trái (để đứng cạnh Icon cho gọn gàng)
     JLabel lblTitle = new JLabel("<html><div style='text-align: left; padding: 2px 0px;'>"
             + "<span style='font-size:16px;'>Sản phẩm: <b style='color:#1A73E8;'>" + tenSP + "</b> đang có nhiều lô.</span><br>"
             + "<span style='font-size:13px; font-weight:normal; color:#DC2626;'>"
             + "Chú ý: Ưu tiên bốc hộp thuốc có màu đỏ để tránh tồn kho!</span>"
             + "</div></html>");

     // 2. Gắn Icon WARNING có sẵn từ file MenuIcon của bạn
     lblTitle.setIcon(Utils.MenuIcon.IC_WARNING); 

     // 3. Căn chỉnh tổng thể: Đẩy cả cụm (Icon + Chữ) ra giữa màn hình
     lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
     lblTitle.setIconTextGap(15); // Tạo khoảng cách 15 pixel giữa Icon và Chữ cho thoáng
     lblTitle.setBorder(new javax.swing.border.EmptyBorder(15, 10, 5, 10)); // Căn lề trên/dưới/trái/phải

     dialog.add(lblTitle, BorderLayout.NORTH);

        // Tạo bảng chứa danh sách lô
        String[] cols = {"Số Lô", "Hạn Sử Dụng", "Tồn", "Đơn Giá", "Chỉ dẫn đi lấy hàng", "Thao Tác"};
        DefaultTableModel modelLo = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // Danh sách này đã được thuật toán FEFO của bạn sort (cận hạn lên đầu) ở logic quét
        for (int i = 0; i < danhSachLo.size(); i++) {
            Object[] lo = danhSachLo.get(i);
            String soLo = (lo.length > 7 && lo[7] != null) ? lo[7].toString() : "N/A";
            String hsd = (lo.length > 8 && lo[8] != null) ? lo[8].toString() : "N/A";
            String tonKho = (lo.length > 4 && lo[4] != null) ? lo[4].toString() : "0";
            
            String gia = "0đ";
            try {
                long giaBan = Math.round(Double.parseDouble(lo[3].toString()));
                gia = String.format("%,d", giaBan).replace(',', '.') + "đ";
            } catch(Exception e) {}
            
            String chiDanViTri = "";
            if (i == 0) {
                chiDanViTri = "[!] TẠI QUẦY (Bốc hộp ngoài cùng)";
            } else {
                chiDanViTri = "[Kho] TRONG KHO (Hàng dự phòng)";
            }
            
            modelLo.addRow(new Object[]{soLo, hsd, tonKho, gia, chiDanViTri, "CHỌN BÁN"});
        }

        JTable tblLo = new JTable(modelLo);
        tblLo.setRowHeight(45);
        tblLo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblLo.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblLo.getTableHeader().setBackground(Color.decode("#F8F9FA"));
        tblLo.setShowGrid(true);
        tblLo.setGridColor(Color.decode("#F1F3F5"));

        // Tô màu cột Chỉ dẫn vị trí
        tblLo.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                if (row == 0) setForeground(Color.decode("#DC2626")); // Đỏ cho dòng cận hạn
                else setForeground(Color.decode("#2563EB")); // Xanh cho hàng lưu kho
                return c;
            }
        });

        // Nút chọn
        tblLo.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JButton btn = new JButton(value.toString());
                btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btn.setForeground(Color.WHITE);
                btn.setBackground(row == 0 ? Color.decode("#10B981") : Color.decode("#9CA3AF")); // Xanh lá ưu tiên, Xám cho kho
                return btn;
            }
        });

        // Bắt sự kiện click chọn
        tblLo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tblLo.rowAtPoint(e.getPoint());
                int col = tblLo.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 5) {
                    Object[] loDuocChon = danhSachLo.get(row);
                    // Gọi lại hàm CÓ SẴN của bạn để đẩy sản phẩm xuống giỏ hàng
                    xyLyThemSanPhamNhanh(loDuocChon, suggestionPopup, txtSearchProduct);
                    dialog.dispose();
                }
            }
        });

        // Chỉnh độ rộng cột
        tblLo.getColumnModel().getColumn(0).setPreferredWidth(90);
        tblLo.getColumnModel().getColumn(1).setPreferredWidth(100);
        tblLo.getColumnModel().getColumn(2).setPreferredWidth(50);
        tblLo.getColumnModel().getColumn(3).setPreferredWidth(90);
        tblLo.getColumnModel().getColumn(4).setPreferredWidth(230);
        tblLo.getColumnModel().getColumn(5).setPreferredWidth(100);

        dialog.add(new JScrollPane(tblLo), BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    private ImageIcon generateQR(String data, int size) {
        try {
            com.google.zxing.qrcode.QRCodeWriter barcodeWriter = new com.google.zxing.qrcode.QRCodeWriter();
            com.google.zxing.common.BitMatrix bitMatrix = barcodeWriter.encode(data, com.google.zxing.BarcodeFormat.QR_CODE, size, size);
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_RGB);
            img.createGraphics();
            java.awt.Graphics2D g = (java.awt.Graphics2D) img.getGraphics();
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, size, size);
            g.setColor(java.awt.Color.BLACK);
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (bitMatrix.get(i, j)) {
                        g.fillRect(i, j, 1, 1);
                    }
                }
            }
            return new ImageIcon(img);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private void thucHienInMaQRThanhToan() {
        Icon icon = lblQRCode.getIcon();
        if (icon == null || !(icon instanceof ImageIcon)) {
            showCustomNotification("LỖI", "Không tìm thấy ảnh mã QR để in!", "ERROR");
            return;
        }

        // Trích xuất ảnh thật từ Icon
        java.awt.Image img = ((ImageIcon) icon).getImage();
        java.awt.image.BufferedImage finalImg = new java.awt.image.BufferedImage(
                img.getWidth(null), img.getHeight(null), java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D bGr = finalImg.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        String soTien = lblTotalPriceValue.getText();
        String maGD = this.maGiaoDichHienTai != null && !this.maGiaoDichHienTai.isEmpty() ? this.maGiaoDichHienTai : "OFFLINE";
        String tenCuaHang = "NHÀ THUỐC"; 

        JDialog dlg = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), "In / Xuất mã QR Thanh toán", true);
        dlg.setUndecorated(false);

        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(24, 32, 20, 32));

        JLabel lblTitle = new JLabel("MÃ THANH TOÁN QR", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.decode("#1E293B"));

        // Panel vẽ mã QR sắc nét
        JPanel pnlImg = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                int margin = 8;
                int size = Math.min(getWidth(), getHeight()) - margin * 2;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                g2.drawImage(finalImg, x, y, size, size, null);
            }
        };
        pnlImg.setPreferredSize(new Dimension(250, 250));
        pnlImg.setBackground(Color.WHITE);
        pnlImg.setBorder(BorderFactory.createLineBorder(Color.decode("#E2E8F0"), 1));

        JLabel lblSo = new JLabel("Mã GD: " + maGD, SwingConstants.CENTER);
        lblSo.setFont(new Font("Courier New", Font.BOLD, 14));
        lblSo.setForeground(Color.decode("#334155"));

        JLabel lblGia = new JLabel("Thanh toán: " + soTien, SwingConstants.CENTER);
        lblGia.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblGia.setForeground(Color.decode("#E11D48"));

        // --- NÚT XUẤT ẢNH ---
        JButton btnXuatAnh = new JButton("Xuất ảnh");
        btnXuatAnh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnXuatAnh.setBackground(Color.decode("#22C55E"));
        btnXuatAnh.setForeground(Color.WHITE);
        btnXuatAnh.setBorderPainted(false);
        btnXuatAnh.setFocusPainted(false);
        btnXuatAnh.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnXuatAnh.addActionListener(ev -> {
            java.awt.Frame parentFrame = (java.awt.Frame) SwingUtilities.getWindowAncestor(this);
            java.awt.FileDialog fd = new java.awt.FileDialog(parentFrame, "Chọn nơi lưu ảnh QR", java.awt.FileDialog.SAVE);
            fd.setFile("QR_" + maGD + ".png");
            fd.setVisible(true);
            String dir = fd.getDirectory();
            String file = fd.getFile();
            if (dir != null && file != null) {
                String filePath = dir + file;
                if (!filePath.toLowerCase().endsWith(".png")) filePath += ".png";
                try {
                    java.awt.image.BufferedImage labelImg = new java.awt.image.BufferedImage(300, 380, java.awt.image.BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = labelImg.createGraphics();
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(0, 0, 300, 380);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString(tenCuaHang, (300 - fm.stringWidth(tenCuaHang)) / 2, 30);

                    g2d.drawImage(finalImg, 25, 45, 250, 250, null);

                    g2d.setFont(new Font("Courier New", Font.BOLD, 14));
                    fm = g2d.getFontMetrics();
                    g2d.drawString("Mã GD: " + maGD, (300 - fm.stringWidth("Mã GD: " + maGD)) / 2, 320);

                    g2d.setColor(Color.decode("#E11D48"));
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
                    fm = g2d.getFontMetrics();
                    g2d.drawString("Thanh toán: " + soTien, (300 - fm.stringWidth("Thanh toán: " + soTien)) / 2, 350);

                    g2d.dispose();
                    javax.imageio.ImageIO.write(labelImg, "png", new java.io.File(filePath));
                    showCustomNotification("XUẤT ẢNH THÀNH CÔNG", "Đã lưu ảnh mã QR tại:\n" + filePath, "SUCCESS");
                } catch (Exception ex) {
                    showCustomNotification("LỖI", "Không lưu được file ảnh: " + ex.getMessage(), "ERROR");
                }
            }
        });

        // --- NÚT IN MÁY IN ---
        JButton btnInTrucTiep = new JButton("In máy in");
        btnInTrucTiep.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnInTrucTiep.setBackground(Color.decode("#0EA5E9"));
        btnInTrucTiep.setForeground(Color.WHITE);
        btnInTrucTiep.setBorderPainted(false);
        btnInTrucTiep.setFocusPainted(false);
        btnInTrucTiep.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnInTrucTiep.addActionListener(ev -> {
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) return java.awt.print.Printable.NO_SUCH_PAGE;
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                double width = 150;
                double height = width; 

                g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2d.drawString(tenCuaHang, 10, 15);
                g2d.drawImage(finalImg, 10, 20, (int)width, (int)height, null);
                g2d.setFont(new Font("Courier New", Font.PLAIN, 10));
                g2d.drawString("GD: " + maGD, 10, (int)height + 35);
                g2d.drawString("Tien: " + soTien, 10, (int)height + 50);
                return java.awt.print.Printable.PAGE_EXISTS;
            });

            if (job.printDialog()) {
                try {
                    job.print();
                    showCustomNotification("IN THÀNH CÔNG", "Đã gửi lệnh in đến máy in.", "SUCCESS");
                } catch (java.awt.print.PrinterException ex) {
                    showCustomNotification("LỖI IN", "Không thể in: " + ex.getMessage(), "ERROR");
                }
            }
        });

        JButton btnDong = new JButton("Đóng");
        btnDong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDong.setBackground(Color.decode("#1E3A8A")); // darkBlue
        btnDong.setForeground(Color.WHITE);
        btnDong.setBorderPainted(false);
        btnDong.setFocusPainted(false);
        btnDong.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDong.addActionListener(ev -> dlg.dispose());

        JPanel pnlBtn = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 0));
        pnlBtn.setBackground(Color.WHITE);
        pnlBtn.add(btnXuatAnh);
        pnlBtn.add(btnInTrucTiep);
        pnlBtn.add(btnDong);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);

        for (JComponent comp : new JComponent[]{lblTitle, pnlImg, lblSo, lblGia}) {
            comp.setAlignmentX(0.5f);
            center.add(comp);
            center.add(Box.createVerticalStrut(8));
        }

        main.add(center, BorderLayout.CENTER);
        main.add(pnlBtn, BorderLayout.SOUTH);

        dlg.setContentPane(main);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(360, 440));
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
    private void moBangKeDonTuDong() {
        // Nếu bảng đang bị ẩn thì tự động giả lập một cú click chuột để mở nó ra
        if (pnlDonThuoc != null && !pnlDonThuoc.isVisible() && btnToggleKeDon != null) {
            btnToggleKeDon.doClick(); 
            showCustomNotification("LƯU Ý KÊ ĐƠN", "Bạn vừa thêm Thuốc Kê Đơn vào giỏ hàng.\nHệ thống đã tự động mở bảng nhập thông tin Bác sĩ!", "WARNING");
        }
    }
}