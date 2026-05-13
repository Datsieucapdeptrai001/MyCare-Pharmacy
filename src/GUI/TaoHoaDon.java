package GUI;
import BUS.BUS_KhuyenMai;
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
    private int thoiGianConLai = 600; // 600 giây = 10 phút
    private javax.swing.Timer boDemNguoc;
    private JLabel lblDongHoDemNguoc;
    private JPopupMenu suggestionInvoiceMenu;
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

        // 1. CHUẨN BỊ DỮ LIỆU CƠ BẢN
        String khach = isCustomerLinked ? linkedTenKH : txtName.getText();
        if(khach.equals("Tên khách (bỏ trống = Khách lẻ)") || khach.trim().isEmpty()) khach = "Khách lẻ";
        String sdt = isCustomerLinked ? linkedSdtKH : txtPhone.getText().replace("Số điện thoại (tuỳ chọn)", "");
        String tongTien = lblTotalPriceValue.getText();

        String maHDMoi = (editingModelRow != -1) ? maHDDangSua.replace("-LuuNhap", "") : phatSinhMaHoaDon();
        Entity.HoaDon hd = new Entity.HoaDon();
        hd.setId(maHDMoi);
        hd.setLoaiHD(Enumeration.LoaiHoaDon.BAN_HANG);
        hd.setNgayLapHD(java.time.LocalDateTime.now());

        // Xử lý Ghi chú & Quà tặng (Để in hóa đơn)
        String strDiem = isDungDiem ? " | Dùng điểm: -" + tienGiamTuDiem : "";
        String strKM = (maKhuyenMaiApDung == null || maKhuyenMaiApDung.isEmpty()) ? "" : " | KM: " + maKhuyenMaiApDung;
        StringBuilder strGifts = new StringBuilder();
        
        // === FIX 1: DÙNG HASHMAP ĐỂ GỘP TRÙNG NGAY TỪ ĐẦU ===
        // Key = maSP + "_" + maDVT
        java.util.Map<String, Entity.ChiTietHoaDon> mapFinal = new java.util.HashMap<>();

        for (int i = 0; i < productModel.getRowCount(); i++) {
            String tenRow = productModel.getValueAt(i, 0).toString().trim();
            boolean isGift = tenRow.startsWith("[QUÀ TẶNG]");
            String tenSP = isGift ? tenRow.replace("[QUÀ TẶNG]", "").trim() : tenRow;
            String tenDVT = productModel.getValueAt(i, 1).toString().trim();
            int soLuongMua = Integer.parseInt(productModel.getValueAt(i, 2).toString());

            // Lấy mã SP và mã DVT từ BUS
            String[] ids = busHD.layMaSPVaMaDVT(tenSP, tenDVT);
            if (ids == null) {
                showCustomNotification("LỖI HỆ THỐNG", "Không tìm thấy mã cho: " + tenSP, "ERROR"); return;
            }
            String maSP = ids[0]; String maDVT = ids[1];
            String key = maSP + "_" + maDVT; // Khóa chính gây lỗi trùng

            // Gộp quà tặng vào chuỗi ghi chú
            if (isGift) {
                strGifts.append(" | TANG:").append(tenSP).append(";").append(soLuongMua).append(";").append(tenDVT);
            }

            // Thực hiện gộp số lượng nếu trùng (Sản phẩm + Đơn vị)
            if (mapFinal.containsKey(key)) {
                Entity.ChiTietHoaDon existing = mapFinal.get(key);
                existing.setSoLuong(existing.getSoLuong() + soLuongMua);
            } else {
                Entity.ChiTietHoaDon ct = new Entity.ChiTietHoaDon();
                ct.setHoaDonId(hd);
                Entity.SanPham sp = new Entity.SanPham(); sp.setId(maSP); ct.setSanPhamId(sp);
                Entity.DonViDoLuong dv = new Entity.DonViDoLuong(); dv.setId(maDVT); ct.setDonViDoLuongId(dv);
                ct.setSoLuong(soLuongMua);
                mapFinal.put(key, ct);
            }
        }

        hd.setGhiChu((phuongThuc.equals("Tiền mặt") ? "CASH:" + tongTienMat : "BANK") + strKM + strDiem + strGifts.toString());

        // Thiết lập Nhân viên, Khách hàng, Khuyến mãi
        Entity.NhanVien nv = new Entity.NhanVien();
        String maNV = Utils.UserSession.getInstance().getMaNhanVien();
        nv.setNhanVien(maNV != null ? maNV : "DS-0001");
        hd.setNhanVienId(nv);
        
        if (isCustomerLinked && !sdt.isEmpty()) {
            hd.setKhachHangId(new BUS.BUS_KhachHang().timKhachHangTheoSdt(sdt));
        }
        hd.setPhuongThucThanhToan(phuongThuc.equals("Tiền mặt") ? Enumeration.PhuongThucThanhToan.TIEN_MAT : Enumeration.PhuongThucThanhToan.CHUYEN_KHOAN_NGAN_HANG);
        if (maKhuyenMaiApDung != null && !maKhuyenMaiApDung.isEmpty()) {
            Entity.KhuyenMai km = new Entity.KhuyenMai(); km.setId(maKhuyenMaiApDung.split(",")[0].trim());
            hd.setKhuyenMaiId(km);
        }

        // 2. XÓA NHÁP VÀ LƯU DATABASE
        if (editingModelRow != -1 && maHDDangSua != null) {
            busHD.xoaHoaDonNhap(maHDDangSua);
        }

        // Chuyển Map thành List sạch
        java.util.List<Entity.ChiTietHoaDon> dsKetQua = new java.util.ArrayList<>(mapFinal.values());
        boolean success = busHD.thanhToan(hd, dsKetQua, new java.util.ArrayList<>()); // Quà tặng đã được gộp vào dsKetQua

        if (success) {
            if (boDemNguoc != null) boDemNguoc.stop();
            
            // Cập nhật tồn kho (Dùng danh sách đã gộp để chính xác)
            for (Entity.ChiTietHoaDon ct : dsKetQua) {
                double tiLe = 1.0;
                List<Entity.DonViDoLuong> dsDonVi = busDonVi.getDSTheoMaSP(ct.getSanPhamId().getId());
                for (Entity.DonViDoLuong dv : dsDonVi) {
                    if (dv.getId().equals(ct.getDonViDoLuongId().getId())) { 
                        tiLe = dv.getChuyenDoiSangDonViCoBan(); break; 
                    }
                }
                int tongTru = (int)(ct.getSoLuong() * tiLe);
                busKho.xuLyXuatKhoFEFO(ct.getSanPhamId().getId(), tongTru);
            }

            // Xử lý điểm tích lũy
            if (isCustomerLinked && !sdt.isEmpty()) {
                int diemDung = isDungDiem ? (int)(tienGiamTuDiem / 100) : 0;
                int diemMoi = (int) (tongHoaDon / 10000);
                new BUS.BUS_KhachHang().capNhatDiemTichLuy(sdt, diemMoi - diemDung);
                showCustomNotification("HOÀN TẤT", "Thanh toán thành công!", "SUCCESS");
            }

            // Cập nhật UI và đóng bảng
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
        pnlBody.add(createSectionPanel("Thêm sản phẩm", "PACKAGE", createProductPanel()));
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));
        
        pnlDonThuoc = createDonThuocPanel();
        pnlDonThuoc.setVisible(false); // Mặc định ẩn

        // 1. Tạo nút bấm Toggle
        JButton btnToggleKeDon = new JButton(" Thêm thông tin kê đơn");
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

        pnlRightFooter.add(lblDongHoDemNguoc); // Gắn đồng hồ vào màn hình
        pnlRightFooter.add(btnLuuNhap);
        pnlRightFooter.add(btnThanhToan);
        pnlFooter.add(pnlRightFooter, BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);

        // BẮT ĐẦU ĐẾM NGƯỢC NGAY KHI MỞ FORM
        
    }
 // =================================================================
    // HÀM QUÉT MÃ QR & LẮNG NGHE TỰ ĐỘNG
    // =================================================================
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

                    // Lấy chuỗi dữ liệu qrCode từ JSON trả về
                    String qrData = "";
                    if (response.contains("\"qrCode\":\"")) {
                        int start = response.indexOf("\"qrCode\":\"") + 10;
                        int end = response.indexOf("\"", start);
                        qrData = response.substring(start, end);
                    }

                    if (!qrData.isEmpty()) {
                        // 4. CHUYỂN CHUỖI QR THÀNH HÌNH ẢNH ĐỂ HIỂN THỊ
                    	String qrImageUrl = "https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=" + java.net.URLEncoder.encode(qrData, "UTF-8");
                    	java.net.URL imgUrl = new java.net.URL(qrImageUrl);
                    	java.net.HttpURLConnection imgConn = (java.net.HttpURLConnection) imgUrl.openConnection();
                    	imgConn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    	java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(imgConn.getInputStream());

                    	// 2. Thu nhỏ lại vừa đúng khung Label 200x200 (Giữ nguyên tỉ lệ để không vỡ)
                    	return new ImageIcon(image.getScaledInstance(200, 200, java.awt.Image.SCALE_SMOOTH));
                    }
                } else {
                    java.io.InputStream err = conn.getErrorStream();
                    if(err != null) {
                        java.util.Scanner s = new java.util.Scanner(err, "UTF-8").useDelimiter("\\A");
                        System.err.println("Lỗi PayOS: " + (s.hasNext() ? s.next() : ""));
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        lblQRCode.setText("");
                        lblQRCode.setIcon(icon);
                        
                        // Kích hoạt đồng hồ quét ngân hàng ACB liên tục
                        batDauQuetGiaoDichNganHang(maGiaoDichHienTai, finalTotalAmount);

                        // Giữ lại mẹo Double-click chuột để phòng hờ lúc báo cáo đồ án mạng yếu
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
                        lblQRCode.setText("Lỗi tạo QR. Xem log console!");
                    }
                } catch (Exception ex) {
                    lblQRCode.setText("Lỗi kết nối PayOS!");
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
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
                Object danhMucObj = getSafeValue(productModel, i, 6);
                String tenSp = productModel.getValueAt(i, 0).toString();
                
                // Bỏ qua hàng Quà tặng khi tính tiền
                if (tenSp.startsWith("[QUÀ TẶNG]")) continue;

                int sl = Integer.parseInt(getSafeValue(productModel, i, 2).toString().trim());
                long donGia = Long.parseLong(getSafeValue(productModel, i, 3).toString().replaceAll("\\D+", ""));
                long vatPercent = Long.parseLong(getSafeValue(productModel, i, 4).toString().replace("%", "").replaceAll("\\s+", ""));
                
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
        // Tăng khoảng cách dọc giữa các thành phần lên 15px cho thoáng
        JPanel pnl = new JPanel(new BorderLayout(0, 15)); 
        pnl.setBackground(Color.WHITE);
        
        // 1. Thêm viền bo góc mỏng và lề (padding) bên trong Card
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new javax.swing.border.EmptyBorder(5, 10, 5, 10)
        ));
        
        // 2. Tạo một Header Panel để chứa tiêu đề và đường gạch chân mờ
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        // Đường line phân cách mỏng màu xám siêu nhạt
        pnlHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#F1F3F5"))); 
        
        JLabel lblTitle = new JLabel(title);
        if (iconType != null) {
            lblTitle.setIcon(new MenuIcon(iconType)); 
            lblTitle.setIconTextGap(10); // Tăng khoảng cách giữa Icon và Text
        }
        
        // 3. Tinh chỉnh Font chữ và màu sắc cho hiện đại hơn
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16)); 
        lblTitle.setForeground(Color.decode("#152A4B")); // Sử dụng màu xanh đen chủ đạo
        lblTitle.setBorder(new javax.swing.border.EmptyBorder(0, 0, 10, 0)); // Cách đường gạch chân 10px

        pnlHeader.add(lblTitle, BorderLayout.WEST);

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

        JLabel lblTitle = new JLabel("Thông tin đơn kê đơn (bắt buộc)");
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

        String[] cols = {"Sản phẩm", "ĐVT", "SL", "Đơn giá", "VAT%", "Thành tiền", ""};
        productModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                // Khóa sạch cột SL và ĐVT nếu là Quà tặng
                if (getValueAt(r, 0).toString().startsWith("[QUÀ TẶNG]")) return c == 6; 
                return c == 2 || c == 6 || c == 1; 
            }
        };
        
        productModel.addTableModelListener(e -> {
            if (isTableUpdating) return; 

            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                
                // Nếu sửa Số Lượng (Cột 2) hoặc đổi ĐVT (Cột 1)
                if (row >= 0 && (col == 2 || col == 1)) {
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
                                    productModel.setValueAt(String.format("%,d", (long)giaBanMoi).replace(',', '.') + "đ", row, 3);
                                }
                            }

                            // --- BƯỚC 2: XỬ LÝ SỐ LƯỢNG VÀ THÀNH TIỀN ---
                            String slStr = productModel.getValueAt(row, 2).toString().trim();
                            int sl = 1;
                            try {
                                sl = Integer.parseInt(slStr);
                                if (sl <= 0) sl = 1; 
                            } catch (Exception ex) { sl = 1; }

                            if (col == 2 && !slStr.equals(String.valueOf(sl))) {
                                productModel.setValueAt(String.valueOf(sl), row, 2); 
                            }

                            // Lấy Đơn giá
                            String giaStr = productModel.getValueAt(row, 3).toString().replaceAll("[^0-9]", "");
                            long donGia = 0;
                            try { donGia = Long.parseLong(giaStr); } catch (Exception ex) {}

                            // LẤY VÀ TÍNH VAT
                            double vatPercent = 0;
                            try {
                                String vatStr = productModel.getValueAt(row, 4).toString().replace("%", "").trim();
                                vatPercent = Double.parseDouble(vatStr) / 100.0;
                            } catch (Exception ex) {}

                            // Tính lại Thành tiền 
                            long tienHang = sl * donGia;
                            long thanhTien = tienHang + (long)(tienHang * vatPercent);
                            productModel.setValueAt(String.format("%,d", thanhTien).replace(',', '.') + "đ", row, 5);
                            
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
                    cbDVT.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
        
        tbl.setRowHeight(30); 
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tbl.setShowGrid(false); 
        tbl.setShowHorizontalLines(true); 
        tbl.setGridColor(Color.decode("#F1F3F5"));

        tbl.getTableHeader().setBackground(Color.decode("#D9EAF7")); 
        tbl.getTableHeader().setForeground(Color.decode("#1E293B"));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tbl.getTableHeader().setPreferredSize(new Dimension(0, 30));
        tbl.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        tbl.getColumnModel().getColumn(0).setPreferredWidth(200); 
        tbl.getColumnModel().getColumn(1).setPreferredWidth(60);  
        tbl.getColumnModel().getColumn(2).setPreferredWidth(50);  
        tbl.getColumnModel().getColumn(3).setPreferredWidth(90);  
        tbl.getColumnModel().getColumn(4).setPreferredWidth(45);  
        tbl.getColumnModel().getColumn(5).setPreferredWidth(90);  
        tbl.getColumnModel().getColumn(6).setPreferredWidth(40);  
        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
       
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tbl.getColumnModel().getColumn(0).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                String text = value != null ? value.toString() : "";
                
                if (text.contains("QUÀ TẶNG") || text.contains("🎁") || text.contains("[]")) {
                    String cleanText = text.replace("[QUÀ TẶNG]", "").replace("🎁", "").replace("[]", "").trim();
                    lbl.setText(cleanText); 
                    lbl.setIcon(new MenuIcon("QUA_TANG"));         
                    lbl.setForeground(Color.decode("#E11D48"));  
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                } else {
                    lbl.setIcon(null);                             
                    lbl.setForeground(isSelected ? Color.BLACK : Color.decode("#111827"));  
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                }
                
                lbl.setIconTextGap(8); 
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
                
                return lbl;
            }
        });
        
        tbl.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tbl.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tbl.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tbl.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        
        tbl.setSelectionBackground(Color.decode("#E0F2FE"));
        
        // CÁC THÀNH PHẦN KHÁC (Renderer/Editor của số lượng, Button Thùng rác, v.v) ĐƯỢC GIỮ NGUYÊN
        class SpinnerCellRenderer extends JSpinner implements javax.swing.table.TableCellRenderer {
            public SpinnerCellRenderer() {
                super(new SpinnerNumberModel(1, 1, 9999, 1));
                JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) getEditor();
                editor.getTextField().setHorizontalAlignment(JTextField.CENTER);
                editor.getTextField().setFont(new Font("Segoe UI", Font.BOLD, 14));
                
                setBorder(BorderFactory.createEmptyBorder());
                editor.setBorder(BorderFactory.createEmptyBorder());
                editor.getTextField().setBorder(BorderFactory.createEmptyBorder());
            }
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                try { setValue(Integer.parseInt(value.toString())); } catch (Exception e) {}
                
                Color bg = isSelected ? table.getSelectionBackground() : Color.WHITE;
                setBackground(bg);
                ((JSpinner.DefaultEditor) getEditor()).getTextField().setBackground(bg);
                ((JSpinner.DefaultEditor) getEditor()).getTextField().setForeground(Color.decode("#111827"));
                return this;
            }
        }
        tbl.getColumnModel().getColumn(2).setCellRenderer(new SpinnerCellRenderer());
        
        class SpinnerCellEditor extends javax.swing.AbstractCellEditor implements javax.swing.table.TableCellEditor {
            private JSpinner spinner;
            private boolean isSettingValue = false; 

            public SpinnerCellEditor() {
                spinner = new JSpinner(new SpinnerNumberModel(1, 1, 999999, 1));
                JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
                editor.getTextField().setHorizontalAlignment(JTextField.CENTER);
                editor.getTextField().setFont(new Font("Segoe UI", Font.BOLD, 14));
                
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
                        productModel.setValueAt(spinner.getValue().toString(), row, 2);
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
                try {
                    spinner.commitEdit();
                } catch (java.text.ParseException e) {
                }
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
        
        tbl.getColumnModel().getColumn(2).setCellEditor(new SpinnerCellEditor());

        tbl.getColumnModel().getColumn(5).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSel, hasFocus, r, c);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lbl.setForeground(Color.decode("#111827"));
                lbl.setHorizontalAlignment(JLabel.RIGHT); 
                return lbl;
            }
        });
        
        tbl.getColumnModel().getColumn(6).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSel, hasFocus, r, c);
                lbl.setText(""); 
                lbl.setIcon(new MenuIcon("TRASH")); 
                lbl.setForeground(Color.decode("#EF4444")); 
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return lbl;
            }
        });

        JScrollPane sp = new JScrollPane(tbl);
        sp.getViewport().setBackground(Color.WHITE); 
        sp.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"))); 
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        sp.getVerticalScrollBar().setUnitIncrement(16);

        pnl.add(sp, BorderLayout.CENTER);

        tbl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) { 
                int row = tbl.rowAtPoint(e.getPoint());
                int col = tbl.columnAtPoint(e.getPoint());
                
                if (row >= 0) {
                    // 1. Xử lý nút thùng rác (Cột 6)
                    if (col == 6 && SwingUtilities.isLeftMouseButton(e)) { 
                        if (tbl.isEditing()) tbl.getCellEditor().stopCellEditing(); 
                        productModel.removeRow(row);
                        recalculateTotals();
                        luuNhapHoaDon(true);
                        sp.revalidate(); sp.repaint();
                        return; 
                    } 
                    
                    // --- FIX LỖI TẮT KHUNG NHẬP LIỆU ---
                    if (tbl.isEditing() && tbl.getEditingColumn() != col) {
                        tbl.getCellEditor().stopCellEditing(); 
                    }

                    // 2. FIX LỖI NHẢY SỐ LƯỢNG KHI BẤM ĐVT: 
                    // CHỈ cho phép click chuột trái/phải để tăng giảm SL khi bấm vào CỘT 0 (Tên Sản Phẩm)
                    if (col == 0) {
                        int slHienTai = Integer.parseInt(productModel.getValueAt(row, 2).toString());
                        
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            productModel.setValueAt(String.valueOf(slHienTai + 1), row, 2);
                        } 
                        else if (SwingUtilities.isRightMouseButton(e)) {
                            if (slHienTai > 1) {
                                productModel.setValueAt(String.valueOf(slHienTai - 1), row, 2);
                            } else {
                                if (tbl.isEditing()) tbl.getCellEditor().stopCellEditing();
                                productModel.removeRow(row);
                                recalculateTotals();
                                return; 
                            }
                        }
                    }
                }
            }
        });

        tbl.addKeyListener(new java.awt.event.KeyAdapter() {
            // keyPressed: Chỉ dùng để bắt các phím chức năng (+, -, Enter)
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int row = tbl.getSelectedRow();
                if (row >= 0 && !tbl.isEditing()) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ADD || e.getKeyCode() == java.awt.event.KeyEvent.VK_EQUALS) {
                        int slHienTai = Integer.parseInt(productModel.getValueAt(row, 2).toString());
                        productModel.setValueAt(String.valueOf(slHienTai + 1), row, 2); 
                    } 
                    else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_SUBTRACT || e.getKeyCode() == java.awt.event.KeyEvent.VK_MINUS) {
                        int slHienTai = Integer.parseInt(productModel.getValueAt(row, 2).toString());
                        if (slHienTai > 1) {
                            productModel.setValueAt(String.valueOf(slHienTai - 1), row, 2); 
                        } else {
                            productModel.removeRow(row);
                            recalculateTotals();
                        }
                    }
                }
            }

            // keyTyped: Dùng để bắt ký tự chữ/số mà khách gõ vào
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                int row = tbl.getSelectedRow();
                if (row >= 0 && !tbl.isEditing()) {
                    char c = e.getKeyChar();
                    // Nếu là phím số (0-9)
                    if (Character.isDigit(c)) {
                        tbl.editCellAt(row, 2); // Buộc mở ô số lượng
                        Component editor = tbl.getEditorComponent();
                        if (editor instanceof JSpinner) {
                            JSpinner spinner = (JSpinner) editor;
                            JSpinner.DefaultEditor spinEditor = (JSpinner.DefaultEditor) spinner.getEditor();
                            spinEditor.getTextField().setText(String.valueOf(c)); // Đè phím số vừa gõ vào
                            spinEditor.getTextField().requestFocus(); 
                        }
                        e.consume(); // Chặn Java Swing tự động in đúp ký tự
                    }
                }
            }
        });

        JPopupMenu suggestionPopup = new JPopupMenu();
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
                            int MAX_ITEMS = 12; // Chỉ vẽ tối đa 12 kết quả để đảm bảo độ mượt tuyệt đối

                            for (Object[] row : ketQua) {
                                if (count >= MAX_ITEMS) break; 

                                String id = row[0].toString();
                                String ten = row[1].toString();
                                String donVi = row[2] != null ? row[2].toString() : "";
                                long giaBan = Math.round(Double.parseDouble(row[3].toString()));
                                String gia = String.valueOf(giaBan); 
                                String tonKho = row[4] != null ? row[4].toString() : "0";
                                String danhMuc = (row.length > 5 && row[5] != null) ? row[5].toString() : "Khác";

                                String thueVat = "5%";
                                if (row.length > 6 && row[6] != null) {
                                    String rawVat = row[6].toString().trim();
                                    try {
                                        double v = Double.parseDouble(rawVat);
                                        if (v > 0 && v < 1) v = v * 100; 
                                        thueVat = (int)v + "%";
                                    } catch (Exception ex) {
                                        thueVat = rawVat + (rawVat.contains("%") ? "" : "%");
                                    }
                                }
                                
                                // ĐOẠN FIX: MÓC LẤY LÔ VÀ HSD TỪ ARRAY
                                String loHang = (row.length > 7 && row[7] != null) ? row[7].toString() : "";
                                String hsd = (row.length > 8 && row[8] != null) ? row[8].toString() : "";

                                String iconType = "PACKAGE"; 
                                String dmCheck = danhMuc.toLowerCase();
                                if (dmCheck.contains("thuốc") || dmCheck.contains("kê đơn")) iconType = "PILL"; // Gộp chung thành icon thuốc          
                                else if (dmCheck.contains("mỹ phẩm")) iconType = "COSMETIC";     
                                else if (dmCheck.contains("chức năng") || dmCheck.contains("tpcn")) iconType = "LEAF";         
                                else if (dmCheck.contains("vật tư") || dmCheck.contains("y tế")) iconType = "MEDICAL_TOOL"; 

                                // Truyền thêm biến loHang và hsd vào cuối hàm
                                pnlList.add(createSuggestionItem(suggestionPopup, txtSearchProduct, iconType, ten, donVi, gia, tonKho, danhMuc, thueVat, loHang, hsd));
                                
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
                    String rawSearchText = txtSearchProduct.getText().trim();
                    if (rawSearchText.isEmpty() || rawSearchText.contains("Tìm tên sản phẩm")) return;

                    // Xử lý quét mã vạch GS1: (91)LOT-2026-0001(92)100...
                    String searchText = rawSearchText;
                    if (rawSearchText.startsWith("(91)")) {
                        int start = rawSearchText.indexOf("(91)") + 4;
                        int end = rawSearchText.indexOf("(92)");
                        if (end != -1) {
                            searchText = rawSearchText.substring(start, end);
                        } else {
                            searchText = rawSearchText.substring(start);
                        }
                    }

                    // Nếu có gợi ý đang mở và luồng tìm kiếm đã lấy được dữ liệu
                    if (suggestionPopup.isVisible() && currentSearchWorker != null && currentSearchWorker.isDone()) {
                        try {
                            java.util.List<Object[]> ketQua = currentSearchWorker.get();
                            if (ketQua != null && !ketQua.isEmpty()) {
                                // Tự động lấy món ĐẦU TIÊN trong danh sách gợi ý
                                Object[] firstItem = ketQua.get(0);
                                xyLyThemSanPhamNhanh(firstItem, suggestionPopup, txtSearchProduct);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        // TRƯỜNG HỢP QUÉT MÃ VẠCH (Tốc độ rất nhanh, Popup chưa kịp hiện)
                    	BUS.BUS_SanPham busSP = new BUS.BUS_SanPham(); // Gọi qua BUS
                        java.util.List<Object[]> ketQua = busSP.timKiemSanPhamBan(searchText);
                        if (ketQua != null && !ketQua.isEmpty()) {
                            // Ưu tiên tìm đúng mã sản phẩm hoặc lô hàng
                            Object[] spCanThem = ketQua.get(0);
                            for (Object[] row : ketQua) {
                                // Kiểm tra mã SP ở cột 0 hoặc số lô ở cột 7
                                String maSP = row[0] != null ? row[0].toString() : "";
                                String soLo = row.length > 7 && row[7] != null ? row[7].toString() : "";
                                if (maSP.equalsIgnoreCase(searchText) || soLo.equalsIgnoreCase(searchText)) {
                                    spCanThem = row;
                                    break;
                                }
                            }
                            xyLyThemSanPhamNhanh(spCanThem, suggestionPopup, txtSearchProduct);
                        } else {
                            showCustomNotification("KHÔNG TÌM THẤY", "Không tìm thấy sản phẩm với mã: " + searchText, "WARNING");
                            txtSearchProduct.setText("");
                        }
                    }
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                // Bổ sung chặn VK_ENTER để không gọi lặp lại tìm kiếm
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
        String danhMuc = (firstItem.length > 5 && firstItem[5] != null) ? firstItem[5].toString() : "Khác";
        
        String thueVat = "5%";
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

        // ---------------------------------------------------------
        // BẢN VÁ LỖI TRÙNG LẶP: Check cả TÊN SẢN PHẨM và ĐƠN VỊ TÍNH
        // ---------------------------------------------------------
        boolean daTonTai = false;
        int rowIndex = -1;
        int currentQty = 0;
        for (int i = 0; i < productModel.getRowCount(); i++) {
            // Phải trùng CẢ tên VÀ đơn vị tính thì mới được cộng dồn
            if (productModel.getValueAt(i, 0).toString().equals(name) &&
                productModel.getValueAt(i, 1).toString().equals(unit)) {
                daTonTai = true;
                rowIndex = i;
                currentQty = Integer.parseInt(productModel.getValueAt(i, 2).toString());
                break;
            }
        }

        if (daTonTai) {
            currentQty++; // Tăng 1
            productModel.setValueAt(String.valueOf(currentQty), rowIndex, 2);
            
            // TÍNH LẠI THÀNH TIỀN CÓ VAT
            double vat = 0;
            try {
                String vatStr = productModel.getValueAt(rowIndex, 4).toString().replace("%", "").trim();
                vat = Double.parseDouble(vatStr) / 100.0;
            } catch (Exception ex) {}
            
            long tienHang = giaBan * currentQty;
            long thanhTien = tienHang + (long)(tienHang * vat);
            
            productModel.setValueAt(String.format("%,d", thanhTien).replace(',', '.') + "đ", rowIndex, 5);
        } else {
            // TÍNH THÀNH TIỀN CÓ VAT CHO SẢN PHẨM MỚI
            double vat = 0;
            try {
                String vatStr = thueVat.replace("%", "").trim();
                vat = Double.parseDouble(vatStr) / 100.0;
            } catch (Exception ex) {}
            
            long thanhTien = giaBan + (long)(giaBan * vat);
            String giaFormatted = String.format("%,d", giaBan).replace(',', '.') + "đ"; // FIX FORMAT ĐƠN GIÁ

            productModel.addRow(new Object[]{
                name, unit, "1", giaFormatted, thueVat, String.format("%,d", thanhTien).replace(',', '.') + "đ", danhMuc 
            });
        }
        
        recalculateTotals();
        if (suggestionPopup != null) suggestionPopup.setVisible(false); // Đóng popup
        if (txtSearchProduct != null) txtSearchProduct.setText(""); // Xóa trắng ô để gõ/bắn mã tiếp theo
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
        pnlChuyenKhoanWrapper.add(lblQRAmount, BorderLayout.SOUTH);
        pnlChuyenKhoanWrapper.setVisible(false); 
        
        JPanel pnlNote = new JPanel(new BorderLayout(0, 5));
        pnlNote.setBackground(Color.WHITE);
        JLabel lblNote = new JLabel("Ghi chú");
        lblNote.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlNote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        JTextField txtNote = new JTextField("Ghi chú thêm...");
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
        
        pnlFinal.add(createWhiteLabel("VAT:")); 
        lblVatValue = createWhiteLabel("+0đ", SwingConstants.RIGHT);
        pnlFinal.add(lblVatValue);
        
        pnlFinal.add(createWhiteLabel("Giảm khuyến mãi:")); 
        lblDiscountValue = createWhiteLabel("-0đ", SwingConstants.RIGHT);
        lblDiscountValue.setForeground(Color.decode("#FCA5A5")); 
        pnlFinal.add(lblDiscountValue);
        
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
        btnYes.setBackground(Color.decode("#EF4444")); 
        btnYes.setForeground(Color.WHITE);
        btnYes.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnYes.setFocusPainted(false);
        btnYes.setBorderPainted(false);
        btnYes.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnYes.addActionListener(e -> { result[0] = true; dialog.dispose(); });

        JButton btnNo = new JButton("Hủy");
        btnNo.setPreferredSize(new Dimension(110, 38));
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

            // Xử lý thông tin form
            String rawName = txtName.getText().trim();
            String rawPhone = txtPhone.getText().trim();

            if (rawName.toLowerCase().contains("tên khách") || rawName.toLowerCase().contains("bỏ trống")) {
                rawName = "";
            }
            if (rawPhone.toLowerCase().contains("số điện") || rawPhone.toLowerCase().contains("tùy chọn") || rawPhone.toLowerCase().contains("tuỳ chọn")) {
                rawPhone = "";
            }

            String txtSearchValue = txtSearch.getText().trim();
            if (rawPhone.isEmpty() && !txtSearchValue.isEmpty() && !txtSearchValue.contains("Nhập SĐT")) {
                if (txtSearchValue.matches("^[0-9]{9,11}$")) {
                    rawPhone = txtSearchValue;
                }
            }

            String khach = rawName.isEmpty() ? "Khách lẻ" : rawName;
            String sdt = rawPhone;

            // ==========================================
            // KHỞI TẠO CÁC LỚP BUS ĐỂ THAO TÁC DATABASE
            // ==========================================
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
            Entity.NhanVien nv = new Entity.NhanVien(maNVHienTai != null && !maNVHienTai.isEmpty() ? maNVHienTai : "DS-0001");
            hd.setNhanVienId(nv);

            // ==========================================
            // 1. XỬ LÝ KHÁCH HÀNG (Gọi qua BUS_KhachHang)
            // ==========================================
            Entity.KhachHang khObj = null;
            if (!sdt.isEmpty() || !khach.equals("Khách lẻ")) {
                if (!sdt.isEmpty()) {
                    khObj = busKH.getKhachHangTheoSDT(sdt);
                }
                
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

            // Xử lý Ghi Chú & Quà tặng
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
                    String qty = productModel.getValueAt(i, 2).toString();
                    strGifts.append(" | TANG:").append(realName).append(";").append(qty).append(";").append(unit);
                }
            }
            
            hd.setLoaiHD(Enumeration.LoaiHoaDon.BAN_HANG); 
            hd.setPhuongThucThanhToan(Enumeration.PhuongThucThanhToan.TIEN_MAT);
            hd.setGhiChu("Lưu nháp" + strKeDon + strGifts.toString());
            
            if (this.maKhuyenMaiApDung != null && !this.maKhuyenMaiApDung.isEmpty()) {
                Entity.KhuyenMai km = new Entity.KhuyenMai();
                km.setId(this.maKhuyenMaiApDung.split(",")[0].trim()); 
                hd.setKhuyenMaiId(km);
            } else {
                hd.setKhuyenMaiId(null);
            }

            boolean isHDSaved = false;

            // ==========================================
            // 2. LƯU / CẬP NHẬT HÓA ĐƠN (Gọi qua BUS_HoaDon)
            // ==========================================
            if (this.editingModelRow != -1) {
                isHDSaved = busHD.capNhatHoaDon(hd);
                if (isHDSaved) {
                    busCT.xoaChiTietTheoMaHD(maHD); // Xóa chi tiết cũ để nạp chi tiết mới
                }
            } else {
                isHDSaved = busHD.themHoaDon(hd);
            }
            
            // ==========================================
            // 3. LƯU CHI TIẾT HÓA ĐƠN
            // ==========================================
            if (isHDSaved) {
                for (int i = 0; i < productModel.getRowCount(); i++) {
                    String tenSP = productModel.getValueAt(i, 0).toString().trim();
                    if (tenSP.startsWith("[QUÀ TẶNG]")) continue;
                    
                    String tenDVT = productModel.getValueAt(i, 1).toString().trim();
                    int soLuong = Integer.parseInt(productModel.getValueAt(i, 2).toString());
                    
                    String maSP = "";
                    String maDVT = "";
                    
                    // Lấy mã SP qua BUS
                    java.util.List<Entity.SanPham> listSP = busSP.traCuuSanPham(tenSP);
                    if (listSP != null) {
                        for(Entity.SanPham sp : listSP) {
                            if(sp.getTen().equalsIgnoreCase(tenSP)) {
                                maSP = sp.getId(); break;
                            }
                        }
                    }
                    
                    // Lấy mã ĐVT qua BUS
                    java.util.List<Entity.DonViDoLuong> listDV = busDV.getDSTheoTenSP(tenSP);
                    if (listDV != null) {
                        for(Entity.DonViDoLuong dv : listDV) {
                            if(dv.getTen().equalsIgnoreCase(tenDVT)) {
                                maDVT = dv.getId(); break;
                            }
                        }
                    }
                    
                    if (maSP.isEmpty()) continue; 

                    Entity.ChiTietHoaDon cthd = new Entity.ChiTietHoaDon();
                    cthd.setHoaDonId(hd);
                    
                    Entity.SanPham sp = new Entity.SanPham();
                    sp.setId(maSP);
                    cthd.setSanPhamId(sp);
                    
                    if (!maDVT.isEmpty()) {
                        Entity.DonViDoLuong dv = new Entity.DonViDoLuong();
                        dv.setId(maDVT);
                        cthd.setDonViDoLuongId(dv);
                    }
                    
                    cthd.setSoLuong(soLuong);
                    busCT.themCTHD(cthd); 
                }
                
                // Cập nhật UI Bảng Chính
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
                
                // =========================================================
                // 4. FIX: LUỒNG TỰ HỦY ĐƠN (Đã gộp Thread và Timer làm 1)
                // =========================================================
                final String maHoaDonHuy = maHD;
                final javax.swing.table.DefaultTableModel modelBangChinh = mainTableModel;
                
                javax.swing.Timer timerHuyDon = new javax.swing.Timer(600000, e -> {
                    // Mở một Thread ngầm để thao tác DB không làm đơ UI của người dùng
                    new Thread(() -> {
                        try {
                            BUS.BUS_HoaDon busHuy = new BUS.BUS_HoaDon();
                            Entity.HoaDon checkHD = busHuy.getHoaDonTheoMa(maHoaDonHuy); 
                            
                            if (checkHD != null && checkHD.getGhiChu() != null && checkHD.getGhiChu().contains("Lưu nháp")) {
                                checkHD.setGhiChu("Đã hủy (Hết hạn)");
                                boolean updateSuccess = busHuy.capNhatHoaDon(checkHD);
                                
                                if(updateSuccess) {
                                    // Bắn kết quả lại về luồng UI chính
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
                                    System.out.println("Hệ thống tự động hủy đơn nháp quá hạn: " + maHoaDonHuy);
                                }
                            }
                        } catch (Exception ex) {
                            System.out.println("Lỗi hủy đơn tự động: " + ex.getMessage());
                        }
                    }).start();
                });
                
                timerHuyDon.setRepeats(false); 
                timerHuyDon.start(); 

                if (!isAutoSave) {
                    showCustomNotification("THÀNH CÔNG", "Đã lưu nháp hóa đơn thành công!\n(Hóa đơn sẽ tự động hủy nếu không thanh toán trong 10 phút tới)", "SUCCESS");
                    this.dispose(); 
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
 // THÊM 2 BIẾN: String loHang, String hsd VÀO CUỐI THAM SỐ
    private JPanel createSuggestionItem(JPopupMenu popup, JTextField txtSearch, String iconType, String name, String unit, String price, String stock, String danhMuc, String vat, String loHang, String hsd) {
        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(10, 15, 10, 15));
        pnl.setCursor(new Cursor(Cursor.HAND_CURSOR));

        int slKhoiTao = 0;
        for (int i = 0; i < productModel.getRowCount(); i++) {
            if (productModel.getValueAt(i, 0).toString().equals(name) &&
                productModel.getValueAt(i, 1).toString().equals(unit)) {
                slKhoiTao = Integer.parseInt(productModel.getValueAt(i, 2).toString());
                break;
            }
        }

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlLeft.setOpaque(false);

        String badgeText = "Khác";
        Color bgColor = Color.decode("#F3F4F6"); 
        Color fgColor = Color.decode("#4B5563"); 

        if (danhMuc != null) {
            String dm = danhMuc.toLowerCase();
            if (dm.contains("thuốc") || dm.contains("kê đơn")) {
                badgeText = "Thuốc"; 
                bgColor = Color.decode("#DBEAFE"); 
                fgColor = Color.decode("#2563EB"); 
            } else if (dm.contains("mỹ phẩm")) {
                badgeText = "Mỹ phẩm"; bgColor = Color.decode("#F3E8FF"); fgColor = Color.decode("#9333EA");
            } else if (dm.contains("chức năng") || dm.contains("tpcn")) {
                badgeText = "TPCN"; bgColor = Color.decode("#D1FAE5"); fgColor = Color.decode("#059669");
            } else if (dm.contains("vật tư") || dm.contains("y tế")) {
                badgeText = "Vật tư"; bgColor = Color.decode("#E5E7EB"); fgColor = Color.decode("#374151");
            }
        }

        JLabel lblBadge = new JLabel(badgeText, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); 
                super.paintComponent(g);
                g2.dispose();
            }
        };
        lblBadge.setOpaque(false); 
        lblBadge.setBackground(bgColor);
        lblBadge.setForeground(fgColor);
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8)); 

        String strBatchInfo = "";
        if ((loHang != null && !loHang.isEmpty()) || (hsd != null && !hsd.isEmpty())) {
            strBatchInfo += "&nbsp;&nbsp;<span style='color: #D97706; font-size: 11px; font-weight: normal; margin-left: 5px; border-left: 1px solid #E5E7EB; padding-left: 5px;'>";
            if (loHang != null && !loHang.isEmpty()) strBatchInfo += "Lô: " + loHang;
            if (hsd != null && !hsd.isEmpty()) strBatchInfo += (strBatchInfo.contains("Lô:") ? " - " : "") + "HSD: " + hsd;
            strBatchInfo += "</span>";
        }

        String htmlName = "<html><div style='max-width: 320px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;'>" 
                        + "<span style='font-weight: bold; font-size: 14px; color: #111827;'>" + name + "</span>"
                        + "&nbsp;&nbsp;<span style='color: #6B7280; font-size: 12px;'>" + unit + "</span>"
                        + strBatchInfo
                        + "</div></html>";

        JLabel lblNameInfo = new JLabel(htmlName);
        
        pnlLeft.add(lblBadge); 
        pnlLeft.add(lblNameInfo);

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

        JLabel lblStock = new JLabel("Tồn: " + stock);
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
                boolean isLeftClick = SwingUtilities.isLeftMouseButton(e);
                boolean isRightClick = SwingUtilities.isRightMouseButton(e);

                popup.setVisible(false); 

                boolean daTonTai = false;
                int rowIndex = -1;
                int currentQty = 0;
                
                for (int i = 0; i < productModel.getRowCount(); i++) {
                    if (productModel.getValueAt(i, 0).toString().equals(name) &&
                        productModel.getValueAt(i, 1).toString().equals(unit)) {
                        daTonTai = true;
                        rowIndex = i;
                        currentQty = Integer.parseInt(productModel.getValueAt(i, 2).toString());
                        break;
                    }
                }
                
                if (isLeftClick) {
                    currentQty++;
                    if (daTonTai) {
                    	productModel.setValueAt(String.valueOf(currentQty), rowIndex, 2);
                        long donGia = 0;
                        try { donGia = Long.parseLong(productModel.getValueAt(rowIndex, 3).toString().replaceAll("[^0-9]", "")); } catch(Exception ex){}
                        
                        double vatValue = 0;
                        try { vatValue = Double.parseDouble(productModel.getValueAt(rowIndex, 4).toString().replace("%", "").trim()) / 100.0; } catch (Exception ex) {}
                        
                        long tienHang = donGia * currentQty;
                        long thanhTien = tienHang + (long)(tienHang * vatValue);
                        productModel.setValueAt(String.format("%,d", thanhTien).replace(',', '.') + "đ", rowIndex, 5);
                    } else {
                        // =========================================================
                        // FIX: Gọi BUS để lấy giá chuẩn của ĐVT thay vì query SQL
                        // =========================================================
                        long donGiaChuan = Long.parseLong(price);
                        try {
                            BUS.BUS_DonViDoLuong busDV = new BUS.BUS_DonViDoLuong();
                            // Lấy danh sách ĐVT theo tên sản phẩm thông qua BUS
                            java.util.List<Entity.DonViDoLuong> listDV = busDV.getDSTheoTenSP(name); 
                            
                            if (listDV != null) {
                                for (Entity.DonViDoLuong dv : listDV) {
                                    if (dv.getTen().equalsIgnoreCase(unit)) {
                                        donGiaChuan = Math.round(dv.getGia());
                                        break;
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            System.err.println("Lỗi quy đổi giá qua BUS: " + ex.getMessage());
                        }

                        String giaFormatted = String.format("%,d", donGiaChuan).replace(',', '.') + "đ";

                        double vatValue = 0;
                        try {
                            vatValue = Double.parseDouble(vat.replace("%", "").trim()) / 100.0;
                        } catch (Exception ex) {}
                        
                        long thanhTienCoVat = donGiaChuan + (long)(donGiaChuan * vatValue);
                        String thanhTienFormatted = String.format("%,d", thanhTienCoVat).replace(',', '.') + "đ";

                        productModel.addRow(new Object[]{
                            name, unit, "1", giaFormatted, vat, thanhTienFormatted, danhMuc 
                        });
                    }
                    lblCount.setText("[" + currentQty + "]"); 
                    txtSearch.setText(""); 
                    
                } else if (isRightClick) {
                    if (daTonTai) {
                        currentQty--;
                        if (currentQty > 0) {
                            productModel.setValueAt(String.valueOf(currentQty), rowIndex, 2);
                            
                            // (Tùy chọn bổ sung): Đã cập nhật lại cách tính Thành Tiền khi Right-click để ăn khớp với VAT như Left-click
                            long donGia = 0;
                            try { donGia = Long.parseLong(productModel.getValueAt(rowIndex, 3).toString().replaceAll("[^0-9]", "")); } catch(Exception ex){}
                            
                            double vatValue = 0;
                            try { vatValue = Double.parseDouble(productModel.getValueAt(rowIndex, 4).toString().replace("%", "").trim()) / 100.0; } catch (Exception ex) {}
                            
                            long tienHang = donGia * currentQty;
                            long thanhTien = tienHang + (long)(tienHang * vatValue);
                            
                            productModel.setValueAt(String.format("%,d", thanhTien).replace(',', '.') + "đ", rowIndex, 5);
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
    
 // Bỏ tham số btnApply ở cuối đi
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
                
                int colCount = Math.max(productModel.getColumnCount(), 11); 
                
                for (Object[] row : dsMonHang) {
                    Object[] fullRow = new Object[colCount];
                    for (int j = 0; j < colCount; j++) fullRow[j] = ""; 
                    
                    if (colCount > 4) fullRow[4] = "0%";     
                    if (colCount > 9) fullRow[9] = "999";   
                    if (colCount > 10) fullRow[10] = "1";    
                    
                    for (int j = 0; j < Math.min(row.length, colCount); j++) {
                        if (row[j] != null) fullRow[j] = row[j];
                    }
                    
                    // =========================================================
                    // FIX LỖI TỔNG TIỀN: Format lại tiền từ Database ra chuỗi (VD: 50.000đ)
                    // =========================================================
                    try {
                        // Cột 3: Đơn giá
                        if (fullRow[3] != null && !fullRow[3].toString().isEmpty()) {
                            String valStr = fullRow[3].toString();
                            if (!valStr.contains("đ")) { // Chỉ ép kiểu nếu CSDL trả về số thô
                                double rawGia = Double.parseDouble(valStr);
                                fullRow[3] = String.format("%,d", (long)rawGia).replace(',', '.') + "đ";
                            }
                        }
                        // Cột 5: Thành tiền
                        if (fullRow[5] != null && !fullRow[5].toString().isEmpty()) {
                            String valStr = fullRow[5].toString();
                            if (!valStr.contains("đ")) {
                                double rawThanhTien = Double.parseDouble(valStr);
                                fullRow[5] = String.format("%,d", (long)rawThanhTien).replace(',', '.') + "đ";
                            }
                        }
                    } catch (Exception ex) {
                        System.out.println("Lỗi format tiền load nháp: " + ex.getMessage());
                    }
                    // =========================================================

                    String tenSP = fullRow[0].toString();
                    try {
                        BUS.BUS_SanPham busSP = new BUS.BUS_SanPham();
                        double v = busSP.layThueVATTheoTenSP(tenSP);
                        
                        if (v > 0 && v < 1) {
                            v = v * 100; 
                        }
                        fullRow[4] = (int)v + "%";
                        
                    } catch(Exception ex) {
                        System.out.println("Lỗi lấy VAT cho hóa đơn nháp qua BUS: " + ex.getMessage());
                    }

                    // =========================================================
                    // FIX LỖI THÀNH TIỀN: TÍNH LẠI THÀNH TIỀN (CÓ VAT) TRƯỚC KHI THÊM VÀO BẢNG
                    // =========================================================
                    try {
                        int sl = 1;
                        if (fullRow[2] != null && !fullRow[2].toString().isEmpty()) {
                            sl = Integer.parseInt(fullRow[2].toString().trim());
                        }
                        
                        long donGia = 0;
                        if (fullRow[3] != null) {
                            donGia = Long.parseLong(fullRow[3].toString().replaceAll("\\D+", ""));
                        }
                        
                        double vatPercent = 0.0;
                        if (fullRow[4] != null) {
                            String vatStr = fullRow[4].toString().replace("%", "").trim();
                            if (!vatStr.isEmpty()) vatPercent = Double.parseDouble(vatStr) / 100.0;
                        }
                        
                        long tienHang = donGia * sl;
                        long thanhTienCoVat = tienHang + (long)(tienHang * vatPercent);
                        
                        // Ghi đè lại cột Thành Tiền (Cột 5)
                        fullRow[5] = String.format("%,d", thanhTienCoVat).replace(',', '.') + "đ";
                    } catch (Exception e) {
                        System.out.println("Lỗi tính lại thành tiền load nháp: " + e.getMessage());
                    }
                    // =========================================================
                    
                    productModel.addRow(fullRow);
                }
                
                BUS.BUS_HoaDon busHD = new BUS.BUS_HoaDon();
                HoaDon hdGoc = busHD.layHoaDonTheoMa(maHoaDon);
                
                if (hdGoc != null) {
                    if (hdGoc.getKhachHangId() != null && hdGoc.getKhachHangId().getId() != null) {
                        BUS.BUS_KhachHang busKH = new BUS.BUS_KhachHang();
                        KhachHang kh = busKH.timKhachHangTheoMa(hdGoc.getKhachHangId().getId());
                        
                        if (kh != null) {
                            this.isCustomerLinked = true;
                            this.linkedTenKH = kh.getHoVaTen();
                            this.linkedSdtKH = kh.getSdt();
                            
                            // =================================================================
                            // FIX: KHÔI PHỤC ĐẦY ĐỦ GIAO DIỆN AVATAR VÀ TÍNH NĂNG DÙNG ĐIỂM
                            // =================================================================
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
                            } else {
                                pnlDungDiem.setVisible(false);
                            }
                            
                            toggleDungDiem.setOn(false);
                            isDungDiem = false;
                            
                            if (pnlInputFields != null) pnlInputFields.setVisible(false); 
                            if (pnlLinkedCustomer != null) pnlLinkedCustomer.setVisible(true); 
                            
                            // Chuyển Badge trạng thái sang màu xanh lá "Đã liên kết"
                            if(lblBadgeLe != null && lblBadgeLe.getParent() != null) {
                                CardLayout cl = (CardLayout)(lblBadgeLe.getParent().getLayout());
                                cl.show(lblBadgeLe.getParent(), "LINKED");
                            }
                        }
                    } else {
                        this.isCustomerLinked = false;
                        if (pnlInputFields != null) pnlInputFields.setVisible(true);
                        if (pnlLinkedCustomer != null) pnlLinkedCustomer.setVisible(false);
                        
                        // Chuyển Badge về "Khách lẻ"
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
                        // FIX: Phải gán mã vào biến này thì hệ thống mới tự trừ tiền khuyến mãi
                        this.maKhuyenMaiApDung = hdGoc.getKhuyenMaiId().getId(); 
                    }
                    if (hdGoc.getNgayLapHD() != null) {
                        // Tính số giây từ lúc lưu nháp đến hiện tại
                        long giayDaQua = java.time.temporal.ChronoUnit.SECONDS.between(hdGoc.getNgayLapHD(), java.time.LocalDateTime.now());
                        int giayConLai = 600 - (int) giayDaQua;
                        khoiDongDongHoHuyDon(giayConLai); // Tiếp tục chạy phần thời gian còn lại
                    } else {
                        khoiDongDongHoHuyDon(600);
                    }
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
        // [1] DỌN DẸP QUÀ TẶNG CŨ TRÊN GIAO DIỆN
        isTableUpdating = true; 
        for (int i = productModel.getRowCount() - 1; i >= 0; i--) {
            String ten = productModel.getValueAt(i, 0).toString();
            if (ten.startsWith("[QUÀ TẶNG]")) {
                productModel.removeRow(i);
            }
        }
        isTableUpdating = false;

        // [2] TÍNH TỔNG TIỀN & SỐ LƯỢNG THỰC TẾ (BỎ QUA QUÀ TẶNG)
        long tongTienBill = this.tamTinh + this.vat;
        int tongSoLuongSP_ThucTe = 0;
        
        for (int i = 0; i < productModel.getRowCount(); i++) {
            try {
                String tenSp = productModel.getValueAt(i, 0).toString();
                Object slObj = productModel.getValueAt(i, 2);
                if (slObj != null && !tenSp.startsWith("[QUÀ TẶNG]")) {
                    tongSoLuongSP_ThucTe += Integer.parseInt(slObj.toString().trim());
                }
            } catch (Exception e) {}
        }

        // Nếu giỏ hàng trống VÀ không có mã nào đang được áp dụng
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

        // ========================================================
        // [3] GỌI DỮ LIỆU QUA BUS (KHÔNG CÒN SQL Ở GIAO DIỆN NỮA)
        // ========================================================
        BUS.BUS_KhuyenMai busKM = new BUS.BUS_KhuyenMai();
        java.util.List<Object[]> dsKhuyenMai = busKM.layDanhSachKhuyenMaiHopLe();
        
        long maxTienGiam = 0;
        String bestMaKM_GiamGia = "";

        // Quét danh sách Khuyến Mãi lấy từ Database lên
        for (Object[] row : dsKhuyenMai) {
            String maKM = row[0] != null ? row[0].toString().trim() : "";
            if (processedPromoIds.contains(maKM)) continue;

            String loaiKM = row[1] != null ? row[1].toString().toUpperCase() : "";
            double giaTriGiam = (double) row[2];
            
            long donToiThieu = 0;
            int slYeuCau = (int) row[5];
            
            // Nhận dạng Điều kiện
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

            if (spYeuCau.isEmpty() && !spTang.isEmpty() && loaiKM.contains("SAN_PHAM_KEM_THEO")) {
                spYeuCau = spTang;
            }

            boolean duDieuKien = false;
            long soLuongTangThucTe = 0;
            String unitToGive = dvdlTang;

            // TRƯỜNG HỢP 1: Yêu cầu SẢN PHẨM CỤ THỂ
            if (!spYeuCau.isEmpty()) {
                int slSanPhamYeuCauThucTe = 0;
                String matchedUnit = "Hộp";
                
                for (int i = 0; i < productModel.getRowCount(); i++) {
                    String tenSpTrongBang = productModel.getValueAt(i, 0).toString();
                    String dvtTrongBang = productModel.getValueAt(i, 1).toString();
                    
                    if (!tenSpTrongBang.startsWith("[QUÀ TẶNG]") && tenSpTrongBang.toLowerCase().contains(spYeuCau.toLowerCase())) {
                        if (dvdlYeuCau.isEmpty() || dvtTrongBang.equalsIgnoreCase(dvdlYeuCau)) {
                            slSanPhamYeuCauThucTe += Integer.parseInt(productModel.getValueAt(i, 2).toString());
                            matchedUnit = dvtTrongBang; 
                        }
                    }
                }
                
                if (slYeuCau > 0 && slSanPhamYeuCauThucTe >= slYeuCau) {
                    duDieuKien = true;
                    soLuongTangThucTe = (slSanPhamYeuCauThucTe / slYeuCau) * (slTang > 0 ? slTang : 1); 
                    if (unitToGive.isEmpty()) unitToGive = matchedUnit; 
                }
            } 
            // TRƯỜNG HỢP 2: KHÔNG yêu cầu sản phẩm cụ thể (Tính trên tổng giỏ hàng)
            else {
                if (slYeuCau > 0 && tongSoLuongSP_ThucTe >= slYeuCau) {
                    duDieuKien = true;
                    soLuongTangThucTe = (tongSoLuongSP_ThucTe / slYeuCau) * (slTang > 0 ? slTang : 1);
                    if (unitToGive.isEmpty()) unitToGive = "Hộp";
                } 
                else if (donToiThieu > 0 && tongTienBill >= donToiThieu) {
                    duDieuKien = true;
                    soLuongTangThucTe = (slTang > 0 ? slTang : 1);
                    if (unitToGive.isEmpty()) unitToGive = "Hộp";
                } 
                else if (slYeuCau == 0 && donToiThieu == 0) {
                    duDieuKien = true; 
                    soLuongTangThucTe = (slTang > 0 ? slTang : 1);
                    if (unitToGive.isEmpty()) unitToGive = "Hộp";
                }
            }

            // XỬ LÝ KHUYẾN MÃI: CHỌN MÃ TỐT NHẤT & KHÔNG CỘNG DỒN
            if (duDieuKien) {
                if (loaiKM.contains("SAN_PHAM_KEM_THEO") || loaiKM.contains("TANG")) {
                    processedPromoIds.add(maKM);
                    danhSachMaDaDuyet.add(maKM);
                    if (soLuongTangThucTe > 0 && !spTang.isEmpty()) {
                        boolean daGop = false;
                        for (Object[] q : danhSachQuaTang) {
                            if (q[0].toString().equals("[QUÀ TẶNG] " + spTang) && q[1].toString().equals(unitToGive)) {
                                long oldSL = Long.parseLong(q[2].toString());
                                q[2] = String.valueOf(oldSL + soLuongTangThucTe);
                                daGop = true; break;
                            }
                        }
                        if (!daGop) {
                            danhSachQuaTang.add(new Object[]{
                                "[QUÀ TẶNG] " + spTang, 
                                unitToGive, 
                                String.valueOf(soLuongTangThucTe), 
                                "0đ", "0%", "0đ", "Hàng tặng"
                            });
                        }
                    }
                } else {
                    long tienGiamTamTinh = 0;
                    if (loaiKM.contains("PHAN_TRAM") || loaiKM.contains("%")) {
                        tienGiamTamTinh = (long) (tongTienBill * (giaTriGiam / 100.0));
                    } else {
                        tienGiamTamTinh = (long) giaTriGiam;
                    }

                    if (tienGiamTamTinh > maxTienGiam) {
                        maxTienGiam = tienGiamTamTinh;
                        bestMaKM_GiamGia = maKM;
                    }
                }
            }
        }
        
        // CHỐT MÃ KHUYẾN MÃI
        if (maxTienGiam > 0 && !bestMaKM_GiamGia.isEmpty()) {
            tongTienGiamDoc = maxTienGiam; 
            danhSachMaDaDuyet.add(bestMaKM_GiamGia); 
        }

        // [4] BẮN MÓN QUÀ LÊN BẢNG
        isTableUpdating = true; 
        for (Object[] rowQuaTang : danhSachQuaTang) {
            productModel.addRow(rowQuaTang);
        }
        isTableUpdating = false;

        // [5] CẬP NHẬT UI
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
        for (Object[] q : danhSachQuaTang) totalSp += Integer.parseInt(q[2].toString());
        if (lblTotalItems != null) lblTotalItems.setText(String.format("Tổng sản phẩm: %d", totalSp));
        
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
 // Sửa hàm kiemTraHopLeKhuyenMai trong TaoHoaDon.java
    private boolean kiemTraHopLeKhuyenMai(String maKM) {
        long tongTienDK = tamTinh + vat;
        java.util.List<Object[]> dsSP = new java.util.ArrayList<>();
        
        // GUI nhặt dữ liệu thô từ table
        for (int i = 0; i < productModel.getRowCount(); i++) {
            dsSP.add(new Object[] {
                productModel.getValueAt(i, 0).toString(),
                productModel.getValueAt(i, 1).toString(),
                Integer.parseInt(productModel.getValueAt(i, 2).toString())
            });
        }

        // Gọi BUS xử lý logic
        BUS_KhuyenMai busKM = new BUS_KhuyenMai();
        BUS_KhuyenMai.PromoValidationResult result = busKM.kiemTraHopLePromotion(maKM, tongTienDK, dsSP);

        if (!result.isValid) {
            showCustomNotification("CHƯA ĐỦ ĐIỀU KIỆN", result.message, "WARNING");
            return false;
        }

        this.tienGiamGia = result.discountAmount;
        return true;
    }
    
}