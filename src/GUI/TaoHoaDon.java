package GUI;

import javax.swing.*;
import java.util.List;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import Enumeration.*;
import Utils.*;
import ConnectDB.ConnectDB;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import DAO.DAO_SanPham;
import DAO.DAO_ChiTietHoaDon;
import DAO.DAO_HoaDon;
import Entity.HoaDon;
import Entity.ChiTietHoaDon;
import Entity.SanPham;
import Entity.DonViDoLuong;
import Entity.NhanVien;
import Entity.KhachHang;
import DAO.DAO_KhachHang;

public class TaoHoaDon extends JDialog {
    // --- BIẾN QUẢN LÝ UI KHÁCH HÀNG ---
    private JPanel pnlInputFields, pnlLinkedCustomer;
    private JLabel lblLinkedAvatar, lblLinkedName, lblLinkedSub, lblLinkedPoints, lblLinkedMoney;
    private JLabel lblLinkStatus, lblBadgeLe;
    private boolean isCustomerLinked = false;
    private String linkedTenKH = "", linkedSdtKH = "";
    private JTextField txtSearch;
    private JPopupMenu customerSuggestionPopup;
    private JButton btnApplyVoucher;
    // --- CÁC BIẾN LOGIC KHÁC ---
    private long tongHoaDon = 0;
    private long tamTinh = 0;
    private long vat = 0;
    private DefaultTableModel productModel;
    private JLabel lblSubtotalValue, lblVatValue, lblTotalPriceValue;
    private JLabel lblTotalItems;    // Hiển thị: Tổng sản phẩm: 0
    private JLabel lblSubTotal;      // Hiển thị: Tạm tính
    private JLabel lblVAT;           // Hiển thị: VAT (8%)
    private JLabel lblTotal;         // Hiển thị: Tổng tiền thanh toán
    private JLabel lblCustomerPay;   // Hiển thị: Khách cần trả
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
    private java.util.List<int[]> listCounters = new java.util.ArrayList<>();
    private java.util.List<JLabel> listCountLabels = new java.util.ArrayList<>();
 // Thêm dòng này ngay cạnh pendingPhoneToLink của bạn
    public static String pendingPhoneToLink = null;
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
                        try (java.sql.Connection con = ConnectDB.getInstance().getConnection();
                             java.sql.PreparedStatement pst = con.prepareStatement("SELECT * FROM KhachHang WHERE sdt = ?")) {
                            
                            pst.setString(1, phoneToLink);
                            try (java.sql.ResultSet rs = pst.executeQuery()) {
                                if (rs.next()) {
                                    String maKH = rs.getString("id");
                                    String tenKH = rs.getString("hoVaTen");
                                    String sdtKH = rs.getString("sdt");
                                    int diemKH = rs.getInt("diemTichLuy");

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
                                        CardLayout cl = (CardLayout)(lblBadgeLe.getParent().getLayout());
                                        cl.show(lblBadgeLe.getParent(), "LINKED");
                                    }
                                    
                                    recalculateTotals();
                                    System.out.println("Đã liên kết khách hàng tự động thành công!");
                                } else {
                                    // Nếu lỗi không tìm thấy, đổ số điện thoại vào ô Tìm kiếm
                                    txtSearch.setText(phoneToLink);
                                    txtSearch.setForeground(Color.BLACK);
                                }
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
                        try (java.sql.Connection con = ConnectDB.getInstance().getConnection();
                             java.sql.PreparedStatement pst = con.prepareStatement("SELECT * FROM KhachHang WHERE sdt = ?")) {
                            
                            pst.setString(1, phoneToLink);
                            try (java.sql.ResultSet rs = pst.executeQuery()) {
                                if (rs.next()) {
                                    String maKH = rs.getString("id");
                                    String tenKH = rs.getString("hoVaTen");
                                    String sdtKH = rs.getString("sdt");
                                    int diemKH = rs.getInt("diemTichLuy");

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
                                        CardLayout cl = (CardLayout)(lblBadgeLe.getParent().getLayout());
                                        cl.show(lblBadgeLe.getParent(), "LINKED");
                                    }
                                    
                                    recalculateTotals();
                                    System.out.println("Đã liên kết khách hàng tự động thành công!");
                                } else {
                                    txtSearch.setText(phoneToLink);
                                    txtSearch.setForeground(Color.BLACK);
                                }
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
        try {
            List<Object[]> dsMoi = busKhuyenMai.layDanhSachKhuyenMaiChoTable(); 
            
            if (dsMoi != null) {
                // Xóa cache cũ đi
                this.dsKhuyenMaiCache.clear();
                
                // Lọc ra các mã đang ở trạng thái "Đang hoạt động" để đưa vào hóa đơn
                for(Object[] km : dsMoi) {
                    String trangThai = km[7].toString(); // Cột số 7 chứa Trạng Thái
                    if("Đang hoạt động".equals(trangThai)) {
                        this.dsKhuyenMaiCache.add(km);
                    }
                }
                
                System.out.println("Đã cập nhật " + this.dsKhuyenMaiCache.size() + " mã khuyến mãi hợp lệ.");
                
                // Vẽ lại các nhãn (tags) mã KM lên giao diện
                renderVoucherTagsUI();
                
                // Tính toán lại tổng tiền
                recalculateTotals(); 
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // THÊM HÀM NÀY ĐỂ VẼ GIAO DIỆN CÁC MÃ KHUYẾN MÃI
 // THÊM HÀM NÀY ĐỂ VẼ GIAO DIỆN CÁC MÃ KHUYẾN MÃI
    private void renderVoucherTagsUI() {
        if (pnlVoucherTags == null) return;
        pnlVoucherTags.removeAll();

        if (dsKhuyenMaiCache == null || dsKhuyenMaiCache.isEmpty()) {
            // Khi không có mã, dùng FlowLayout để chữ thông báo không bị giãn ngang
            pnlVoucherTags.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
            JLabel lblEmpty = new JLabel("<html><i>(Hiện chưa có chương trình khuyến mãi nào)</i></html>");
            lblEmpty.setForeground(Color.GRAY);
            pnlVoucherTags.add(lblEmpty);
        } else {
            // KHI CÓ MÃ: Sử dụng GridLayout(0, 3) để ép chia đúng 3 cột, tự động xuống dòng
            pnlVoucherTags.setLayout(new GridLayout(0, 3, 10, 10));
            
            for (Object[] row : dsKhuyenMaiCache) {
                String maKM = row[0].toString();      
                String mucGiamUI = row[3].toString(); 
                String donToiThieuUI = row[4].toString(); 

                String labelStr = maKM + ": " + mucGiamUI;
                if (!donToiThieuUI.equals("Không yêu cầu") && !donToiThieuUI.equals("Mọi đơn hàng")) {
                    labelStr += " (Đơn ≥ " + donToiThieuUI + ")";
                }

                pnlVoucherTags.add(createVoucherTag(labelStr, maKM, txtVoucherInput));
            }
        }
        pnlVoucherTags.revalidate();
        pnlVoucherTags.repaint();
        updateVoucherTagsUI(); 
    }
    private void initUI(Frame parent) {
        setSize(1000, 800);
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
        pnlBody.setBorder(new EmptyBorder(10, 25, 10, 25));
        
        pnlBody.add(createCustomerPanel());
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));
        pnlBody.add(createSectionPanel("Thêm sản phẩm", "PACKAGE", createProductPanel()));
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));
        
        pnlDonThuoc = createDonThuocPanel();
        pnlBody.add(pnlDonThuoc);
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));
        
        pnlBody.add(createSectionPanel("Mã khuyến mãi", "GIFT", createVoucherPanel()));
        pnlBody.add(Box.createRigidArea(new Dimension(0, 5)));
        
        pnlDungDiem = createDungDiemPanel();
        pnlDungDiem.setVisible(false);
        pnlBody.add(pnlDungDiem);
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));
        
        pnlBody.add(createSummaryPanel());

        JScrollPane scrollPane = new JScrollPane(pnlBody);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        JScrollBar customScrollBar = new JScrollBar() {
            @Override
            public void updateUI() {
                setUI(new ModernScrollBarUI()); 
            }
        };
        customScrollBar.setPreferredSize(new Dimension(10, 0));
        customScrollBar.setUnitIncrement(16);
        scrollPane.setVerticalScrollBar(customScrollBar);
        
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor),
            new EmptyBorder(10, 25, 10, 25)
        ));

        if (editingModelRow != -1) {
            JButton btnXoaDon = new JButton("Xóa đơn");
            btnXoaDon.setIcon(new MenuIcon("TRASH")); 
            btnXoaDon.setIconTextGap(8);
            styleButton(btnXoaDon, Color.decode("#EF4444")); 
            
            btnXoaDon.addActionListener(e -> {
                boolean confirm = showCustomConfirmDialog("Xác nhận hủy", "Bạn có chắc chắn muốn HỦY hóa đơn này không?<br>Thao tác này không thể hoàn tác.");
                
                if (confirm) {
                    if (this.maHDDangSua != null && !this.maHDDangSua.isEmpty()) {
                        String sql = "UPDATE HoaDon SET ghiChu = N'Đã hủy' WHERE id = ?"; 
                        try (java.sql.Connection con = ConnectDB.getInstance().getConnection();
                             java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
                            
                            pst.setString(1, this.maHDDangSua);
                            pst.executeUpdate();
                            
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            showCustomNotification("LỖI", "Lỗi khi hủy: " + ex.getMessage(), "ERROR");
                            return;
                        }
                    }

                    if (this.editingModelRow != -1) {
                        mainTableModel.setValueAt("Đã hủy", this.editingModelRow, 6); 
                    }

                    showCustomNotification("THÀNH CÔNG", "Đã hủy hóa đơn thành công!", "SUCCESS");
                    this.dispose(); 
                }
            });
            pnlFooter.add(btnXoaDon, BorderLayout.WEST);
        }

        JPanel pnlRightFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlRightFooter.setOpaque(false);

        JButton btnLuuNhap = new JButton("Lưu nháp");
        styleButton(btnLuuNhap, orangeLogo);

        JButton btnThanhToan = new JButton("Thanh toán");
        styleButton(btnThanhToan, successGreen);

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
            if (pnlDonThuoc.isVisible()) {
                String bacSi = txtBacSi.getText().trim();
                String coSo = txtCoSo.getText().trim();
                
                if (bacSi.isEmpty() || bacSi.contains("BS. Nguyễn") || 
                    coSo.isEmpty() || coSo.contains("BV Bạch Mai")) {
                    showCustomNotification("THIẾU THÔNG TIN", "Đơn hàng này có THUỐC KÊ ĐƠN.\nVui lòng nhập đầy đủ Tên Bác Sĩ và Cơ Sở Khám Bệnh!", "WARNING");
                    return; 
                }
            }
            String khach = isCustomerLinked ? linkedTenKH : txtName.getText();
            if(khach.equals("Tên khách (bỏ trống = Khách lẻ)") || khach.trim().isEmpty()) khach = "Khách lẻ";
            String sdt = isCustomerLinked ? linkedSdtKH : txtPhone.getText().replace("Số điện thoại (tuỳ chọn)", "");
            String tongTien = lblTotalPriceValue.getText();

            try (java.sql.Connection con = ConnectDB.getInstance().getConnection()) {

            	String maHDMoi = (editingModelRow != -1) ? maHDDangSua.replace("-LuuNhap", "") : phatSinhMaHoaDon();
                Entity.HoaDon hd = new Entity.HoaDon();
                hd.setId(maHDMoi);
                hd.setLoaiHD(Enumeration.LoaiHoaDon.BAN_HANG);
                
                String strDiem = isDungDiem ? " | Dùng điểm: -" + tienGiamTuDiem : "";
                String strKM = maKhuyenMaiApDung.isEmpty() ? "" : " | KM: " + maKhuyenMaiApDung;
                
                String strKeDon = "";
                if (pnlDonThuoc != null && pnlDonThuoc.isVisible()) {
                    String bs = txtBacSi.getText().trim();
                    String cs = txtCoSo.getText().trim();
                    String cd = txtChuanDoan.getText().trim();
                    if (cd.contains("Chẩn đoán bệnh")) cd = ""; 
                    strKeDon = " | BS:" + bs + " | CS:" + cs + (cd.isEmpty() ? "" : " | CD:" + cd);
                }
                
                if (phuongThuc.equals("Tiền mặt")) {
                    hd.setGhiChu("CASH:" + tongTienMat + strKM + strDiem + strKeDon);
                } else {
                    hd.setGhiChu("BANK" + strKM + strDiem + strKeDon); 
                }
                hd.setNgayLapHD(java.time.LocalDateTime.now());

                Entity.NhanVien nv = new Entity.NhanVien();
                try { nv.setNhanVien("DS-0001"); } catch(Exception ex) { }
                hd.setNhanVienId(nv);

                if (isCustomerLinked && !sdt.isEmpty()) {
                    try (java.sql.PreparedStatement pstKH = con.prepareStatement("SELECT id FROM KhachHang WHERE sdt = ?")) {
                        pstKH.setString(1, sdt);
                        try (java.sql.ResultSet rsKH = pstKH.executeQuery()) {
                            if (rsKH.next()) {
                                Entity.KhachHang khObj = new Entity.KhachHang();
                                khObj.setId(rsKH.getString("id"));
                                hd.setKhachHangId(khObj);
                            }
                        }
                    }
                }

                Enumeration.PhuongThucThanhToan pt = phuongThuc.equals("Tiền mặt") 
                        ? Enumeration.PhuongThucThanhToan.TIEN_MAT 
                        : Enumeration.PhuongThucThanhToan.CHUYEN_KHOAN_NGAN_HANG;
                hd.setPhuongThucThanhToan(pt);

                java.util.List<Entity.ChiTietHoaDon> dsCTHD = new java.util.ArrayList<>();
                for (int i = 0; i < productModel.getRowCount(); i++) {
                    String tenSP = productModel.getValueAt(i, 0).toString();
                    if (tenSP.startsWith("🎁")) continue;
                    String tenDVT = productModel.getValueAt(i, 1).toString();
                    int soLuong = Integer.parseInt(productModel.getValueAt(i, 2).toString());

                    String maSP = "";
                    String maDVT = "";
                    String sql = "SELECT sp.id AS MaSP, dv.id AS MaDVT FROM SanPham sp JOIN DonViDoLuong dv ON sp.id = dv.sanPhamId WHERE sp.ten = ? AND dv.ten = ?";
                    
                    try (java.sql.PreparedStatement pstSP = con.prepareStatement(sql)) {
                        pstSP.setString(1, tenSP);
                        pstSP.setString(2, tenDVT);
                        try (java.sql.ResultSet rsSP = pstSP.executeQuery()) {
                            if(rsSP.next()) {
                                maSP = rsSP.getString("MaSP");
                                maDVT = rsSP.getString("MaDVT");
                            }
                        }
                    }

                    if (maSP.isEmpty()) throw new Exception("Không tìm thấy mã sản phẩm trong CSDL cho: " + tenSP);

                    Entity.ChiTietHoaDon ct = new Entity.ChiTietHoaDon();
                    ct.setHoaDonId(hd);
                    
                    Entity.SanPham sp = new Entity.SanPham();
                    sp.setId(maSP);
                    ct.setSanPhamId(sp);

                    Entity.DonViDoLuong dv = new Entity.DonViDoLuong();
                    dv.setId(maDVT);
                    ct.setDonViDoLuongId(dv);

                    ct.setSoLuong(soLuong);
                    dsCTHD.add(ct);
                }

                BUS.BUS_HoaDon busHD = new BUS.BUS_HoaDon();
                boolean success = busHD.thanhToan(hd, dsCTHD);

                if (success) {
                    if (isCustomerLinked && !sdt.isEmpty()) {
                        try {
                            int diemDaDung = isDungDiem ? (int)(tienGiamTuDiem / 100) : 0;
                            int diemCongMoi = (int) (tongHoaDon / 10000); 
                            int diemChenhLech = diemCongMoi - diemDaDung; 
                        
                            String sqlUpdateDiem = "UPDATE KhachHang SET diemTichLuy = ISNULL(diemTichLuy, 0) + ? WHERE sdt = ?";
                            try (java.sql.PreparedStatement pstUpdate = con.prepareStatement(sqlUpdateDiem)) {
                                pstUpdate.setInt(1, diemChenhLech);
                                pstUpdate.setString(2, sdt);
                                pstUpdate.executeUpdate();
                            }

                            Window parentWindow = SwingUtilities.getWindowAncestor(this);
                            if (parentWindow instanceof MainDashboard) {
                                DefaultTableModel khModel = ((MainDashboard) parentWindow).getModelKhachHang();
                                if (khModel != null) {
                                    for (int j = 0; j < khModel.getRowCount(); j++) {
                                        String phoneInTable = khModel.getValueAt(j, 2).toString();
                                        if (sdt.equals(phoneInTable)) {
                                            int dHienTai = 0;
                                            try {
                                                String currentDiemStr = khModel.getValueAt(j, 5).toString().replace(".", "").replace(",", "");
                                                dHienTai = Integer.parseInt(currentDiemStr);
                                            } catch (Exception ex) {}
                                            khModel.setValueAt(String.valueOf(dHienTai + diemChenhLech), j, 5);

                                            int donHangHienTai = 0;
                                            try {
                                                donHangHienTai = Integer.parseInt(khModel.getValueAt(j, 3).toString());
                                            } catch(Exception ex) {}
                                            khModel.setValueAt(String.valueOf(donHangHienTai + 1), j, 3);

                                            long chiTieuHienTai = 0;
                                            try {
                                                String currentChiTieu = khModel.getValueAt(j, 4).toString().replaceAll("[^0-9]", "");
                                                chiTieuHienTai = Long.parseLong(currentChiTieu);
                                            } catch(Exception ex) {}
                                            java.text.DecimalFormat df = new java.text.DecimalFormat("#,###đ");
                                            khModel.setValueAt(df.format(chiTieuHienTai + tongHoaDon), j, 4);

                                            break; 
                                        }
                                    }
                                }
                            }
                            
                            String msgThongBao = "Thanh toán thành công!\n";
                            if(diemDaDung > 0) msgThongBao += "- Đã sử dụng: " + String.format("%,d", diemDaDung) + " điểm\n";
                            if(diemCongMoi > 0) msgThongBao += "+ Tích lũy thêm: " + String.format("%,d", diemCongMoi) + " điểm\n";
                            
                            showCustomNotification("HOÀN TẤT", msgThongBao, "SUCCESS");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            showCustomNotification("CẢNH BÁO", "Thanh toán thành công nhưng có lỗi khi cập nhật điểm!", "WARNING");
                        }
                    } else {
                        showCustomNotification("HOÀN TẤT", "Thanh toán thành công!\nHóa đơn và Tồn kho đã được cập nhật.", "SUCCESS");
                    }

                    String ngayStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    if (editingModelRow != -1) {
                        mainTableModel.setValueAt(maHDMoi, editingModelRow, 0);
                        mainTableModel.setValueAt(ngayStr, editingModelRow, 1);
                        mainTableModel.setValueAt(khach, editingModelRow, 2);         
                        mainTableModel.setValueAt(sdt, editingModelRow, 3);           
                        mainTableModel.setValueAt(phuongThuc, editingModelRow, 4);   
                        mainTableModel.setValueAt(tongTien, editingModelRow, 5);     
                        mainTableModel.setValueAt("Hoàn thành", editingModelRow, 6); 
                    } else {
                    	mainTableModel.insertRow(0, new Object[]{maHDMoi, ngayStr, khach, sdt, phuongThuc, tongTien, "Hoàn thành", "", "TPCN"});
                    }
                    dispose(); 
                } else {
                	showCustomNotification("TỪ CHỐI THANH TOÁN", "Hệ thống từ chối giao dịch!\nVui lòng kiểm tra lại số lượng tồn kho.", "WARNING");
                }

            } catch(Exception ex) {
                ex.printStackTrace();
                String errorMsg = ex.getMessage();
                if (errorMsg != null && errorMsg.toLowerCase().contains("kho không đủ")) {
                    showCustomNotification("TỒN KHO KHÔNG ĐỦ", 
                        errorMsg + "\n\nVui lòng giảm số lượng trong đơn hoặc nhập thêm hàng vào kho!", 
                        "WARNING");
                } else {
                    showCustomNotification("LỖI HỆ THỐNG", "Đã xảy ra lỗi: " + errorMsg, "ERROR");
                }
            }
        });

        btnLuuNhap.addActionListener(e -> luuNhapHoaDon(false));

        pnlRightFooter.add(btnLuuNhap);
        pnlRightFooter.add(btnThanhToan);
        pnlFooter.add(pnlRightFooter, BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);
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
        JLabel lblIcon = new JLabel("P", SwingConstants.CENTER) {
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
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 18)); // Chữ P to rõ ràng
        lblIcon.setForeground(Color.decode("#EE4D2D"));
        lblIcon.setPreferredSize(new Dimension(38, 38)); // Phóng to đồng xu lên 38x38
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
        
        long tongTien = 0;     // Tiền hàng (Tạm tính)
        long tongVat = 0;      // Tổng tiền thuế VAT
        int soLuongSanPham = 0;
        
        // --- THÊM MỚI: BIẾN KIỂM TRA THUỐC KÊ ĐƠN ---
        boolean hasThuocKeDon = false; 

        for (int i = 0; i < productModel.getRowCount(); i++) {
            try {
                // --- THÊM MỚI: Quét cột số 6 xem có chứa chữ "kê đơn" không ---
                Object danhMucObj = getSafeValue(productModel, i, 6);
                if (danhMucObj != null) {
                    String dm = danhMucObj.toString().toLowerCase();
                    if (dm.contains("kê đơn") && !dm.contains("không")) {
                        hasThuocKeDon = true;
                    }
                }

                // 1. Lấy Số lượng (Cột 2)
                int sl = 0;
                Object slObj = getSafeValue(productModel, i, 2);
                if (slObj != null) {
                    sl = Integer.parseInt(slObj.toString().trim());
                }

                // 2. Lấy Đơn giá (Cột 3)
                long donGia = 0;
                Object donGiaObj = getSafeValue(productModel, i, 3);
                if (donGiaObj != null) {
                    String donGiaStr = donGiaObj.toString().replaceAll("[^0-9]", "");
                    if (!donGiaStr.isEmpty()) donGia = Long.parseLong(donGiaStr);
                }

                // 3. Tính tiền trước VAT (Tạm tính từng món)
                long thanhTien = sl * donGia;
                tongTien += thanhTien;
                soLuongSanPham += sl;

                // 4. Lấy % VAT (Cột 4) và tính VAT thực tế
                double thueSuat = 0.0;
                Object vatObj = getSafeValue(productModel, i, 4);
                if (vatObj != null) {
                    String vatStr = vatObj.toString().replace("%", "").trim();
                    if (!vatStr.isEmpty()) {
                        thueSuat = Double.parseDouble(vatStr) / 100.0;
                    }
                }
                tongVat += (long) (thanhTien * thueSuat);

            } catch (Exception ex) {
                System.out.println("Lỗi định dạng số ở dòng " + i + ": " + ex.getMessage());
            }
        }

        // --- THÊM MỚI: TỰ ĐỘNG BẬT/TẮT BẢNG KÊ ĐƠN & RENDER LẠI GIAO DIỆN ---
        if (pnlDonThuoc != null && pnlDonThuoc.isVisible() != hasThuocKeDon) {
            pnlDonThuoc.setVisible(hasThuocKeDon);
            this.revalidate(); // Yêu cầu vẽ lại giao diện cho khít
            this.repaint();
        }

        // --- TỔNG HỢP VÀ CẬP NHẬT BIẾN ---
        if (lblTotalItems != null) {
            lblTotalItems.setText(String.format("Tổng sản phẩm: %d", soLuongSanPham));
        }

        this.tamTinh = tongTien;
        this.vat = tongVat;
        tuDongApDungKhuyenMai();
        long totalToPay = this.tamTinh + this.vat;
        
        // 1. Trừ tiền giảm giá từ Khuyến mãi (Ưu tiên trừ trước)
        totalToPay -= tienGiamGia; 
        if (totalToPay < 0) totalToPay = 0;
        
        // 2. FIX LỖI NUỐT ĐIỂM: Trừ điểm tích lũy khách hàng (Không trừ lố)
        if (isDungDiem) {
            long maxTienGiam = diemHienTaiKH * 100L;
            if (maxTienGiam > totalToPay) {
                // Nếu điểm khách lớn hơn tiền hóa đơn -> Chỉ trừ đúng bằng số tiền hóa đơn (làm tròn theo 100đ)
                this.tienGiamTuDiem = (totalToPay / 100L) * 100L; 
            } else {
                this.tienGiamTuDiem = maxTienGiam; // Nếu không đủ thì dùng hết điểm
            }
            totalToPay -= this.tienGiamTuDiem;
        } else {
            this.tienGiamTuDiem = 0; 
        }
        
        // Đảm bảo tổng tiền không bị âm
        if (totalToPay < 0) totalToPay = 0;
        
        // Gán lại để hàm khác (như thanh toán, tiền khách đưa) xài đúng
        this.tongHoaDon = totalToPay; 

        // --- ĐỔ SỐ LIỆU RA GIAO DIỆN ---
        if (lblSubtotalValue != null) {
            lblSubtotalValue.setText(String.format("%,d", this.tamTinh).replace(',', '.') + "đ");
        }
        if (lblVatValue != null) {
            lblVatValue.setText("+" + String.format("%,d", this.vat).replace(',', '.') + "đ");
        }
        if (lblDiscountValue != null) {
            lblDiscountValue.setText("-" + String.format("%,d", tienGiamGia).replace(',', '.') + "đ");
        }
        if (lblDungDiemValue != null) {
            lblDungDiemValue.setText("-" + String.format("%,d", this.tienGiamTuDiem).replace(',', '.') + "đ");
        }
        if (lblTotalPriceValue != null) {
            lblTotalPriceValue.setText(String.format("%,d", totalToPay).replace(',', '.') + "đ");
        }
        if (lblQRAmount != null) {
            lblQRAmount.setText("Cần thanh toán: " + String.format("%,d", totalToPay).replace(',', '.') + "đ");
        }
        if (lblExactValue != null) {
            lblExactValue.setText(String.format("%,d", totalToPay).replace(',', '.') + "đ");
        }

        // --- FIX UI/UX: CẬP NHẬT TRỰC TIẾP ĐIỂM CÒN LẠI LÊN GIAO DIỆN NGAY LẬP TỨC ---
        if (lblDungDiemText != null) {
            int diemThucTeDung = (int)(this.tienGiamTuDiem / 100L);
            
            // Đổi chữ hiển thị trên công tắc bật/tắt
            if (isDungDiem) {
                lblDungDiemText.setText(String.format("Dùng %,d điểm", diemThucTeDung) + " (-" + String.format("%,d", this.tienGiamTuDiem).replace(',', '.') + "đ)");
            } else {
                lblDungDiemText.setText(String.format("Dùng %,d điểm", diemHienTaiKH) + " (-" + String.format("%,d", diemHienTaiKH * 100L).replace(',', '.') + "đ)");
            }
            
            // Ép điểm của khách hàng trên bảng thông tin xanh nhạt thụt giảm ngay lập tức
            if (lblLinkedPoints != null && isCustomerLinked) {
                int diemConLai = diemHienTaiKH - diemThucTeDung;
                lblLinkedPoints.setText(String.format("%,d", diemConLai).replace(',', '.') + " điểm");
                lblLinkedMoney.setText("≈ " + String.format("%,d", diemConLai * 100L).replace(',', '.') + "đ");
            }
        }

        // Nếu đang ở tab mã QR, update lại QR với số tiền mới
        if ("Chuyển khoản".equals(phuongThuc) && lblQRCode != null) {
            loadQRCodeVCB(lblQRCode); 
        }
        
        capNhatTongTien();
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

        pnlVoucherTags = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlVoucherTags.setOpaque(false);
        
        // FIX: Đóng Connection và Statement đúng chuẩn
        String sqlLoad = "SELECT k.id, h.loaiHinhThuc, h.giaTri AS mucGiam, ISNULL(d.giaTri, 0) AS donToiThieu " +
                         "FROM KhuyenMai k " +
                         "JOIN HinhThucKhuyenMai h ON k.id = h.khuyenMaiId " +
                         "LEFT JOIN DieuKienKhuyenMai d ON k.id = d.khuyenMaiId " +
                         "WHERE k.ngayBatDau <= GETDATE() AND k.ngayKetThuc >= GETDATE()";
                         
        try (java.sql.Connection con = ConnectDB.getInstance().getConnection();
             java.sql.Statement st = con.createStatement();
             java.sql.ResultSet rs = st.executeQuery(sqlLoad)) {
            
            boolean hasVoucher = false;
            dsKhuyenMaiCache.clear();

            while(rs.next()) {
                hasVoucher = true;
                String maKM = rs.getString("id");
                String loaiKM = rs.getString("loaiHinhThuc"); 
                double giaTri = rs.getDouble("mucGiam");
                double donToiThieu = rs.getDouble("donToiThieu");
                
                dsKhuyenMaiCache.add(new Object[]{maKM, loaiKM, giaTri, donToiThieu});

                String labelStr = "Mã " + maKM + ": ";
                if(loaiKM != null && loaiKM.trim().toUpperCase().contains("PHAN_TRAM")) {
                    labelStr += "Giảm " + (int)giaTri + "%";
                } else {
                    labelStr += "Giảm " + String.format("%,d", (long)giaTri).replace(',', '.') + "đ";
                }
                
                if(donToiThieu > 0) {
                    labelStr += " (Đơn ≥ " + String.format("%,d", (long)donToiThieu).replace(',', '.') + "đ)";
                }

                pnlVoucherTags.add(createVoucherTag(labelStr, maKM, txtVoucherInput));
            }
            
            if(!hasVoucher) {
                JLabel lblEmpty = new JLabel("<html><i>(Hiện chưa có chương trình khuyến mãi nào)</i></html>");
                lblEmpty.setForeground(Color.GRAY);
                pnlVoucherTags.add(lblEmpty);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        pnlWrapper.add(pnlInput, BorderLayout.NORTH);

     // Tạo một Panel bọc ngoài để ngăn GridLayout tự động giãn chiều cao
     JPanel pnlTagsContainer = new JPanel(new BorderLayout());
     pnlTagsContainer.setOpaque(false);
     pnlTagsContainer.add(pnlVoucherTags, BorderLayout.NORTH); // Ép các thẻ lên sát phía trên

     pnlWrapper.add(pnlTagsContainer, BorderLayout.CENTER);

     btnApply.addActionListener(e -> {
         String code = txtVoucherInput.getText().trim();
         if(code.isEmpty() || code.equals("NHẬP MÃ HOẶC CHỌN BÊN DƯỚI...")) return;

         long tongTienDK = tamTinh + vat; 
         String sqlCheck = "SELECT h.loaiHinhThuc, h.giaTri AS mucGiam, ISNULL(d.giaTri, 0) AS donToiThieu " +
                           "FROM KhuyenMai k " +
                           "JOIN HinhThucKhuyenMai h ON k.id = h.khuyenMaiId " +
                           "LEFT JOIN DieuKienKhuyenMai d ON k.id = d.khuyenMaiId " +
                           "WHERE k.id = ? AND k.ngayBatDau <= GETDATE() AND k.ngayKetThuc >= GETDATE()";
         
         try (java.sql.Connection con = ConnectDB.getInstance().getConnection();
              java.sql.PreparedStatement pst = con.prepareStatement(sqlCheck)) {
              
             pst.setString(1, code);
             try (java.sql.ResultSet rsCheck = pst.executeQuery()) {
                 if(rsCheck.next()) {
                     String loaiKM = rsCheck.getString("loaiHinhThuc");
                     double giaTri = rsCheck.getDouble("mucGiam");
                     long donToiThieu = (long) rsCheck.getDouble("donToiThieu");

                     if (tongTienDK < donToiThieu) {
                         showCustomNotification("CẢNH BÁO", "Chưa đạt giá trị đơn tối thiểu (" + String.format("%,d", donToiThieu).replace(',', '.') + "đ) để áp dụng mã này!", "WARNING");
                         return;
                     }

                     if(loaiKM != null && loaiKM.trim().toUpperCase().contains("PHAN_TRAM")) {
                         tienGiamGia = (long) (tongTienDK * (giaTri / 100.0));
                     } else {
                         tienGiamGia = (long) giaTri; 
                     }

                     maKhuyenMaiApDung = code;
                 } else {
                     tienGiamGia = 0;
                     maKhuyenMaiApDung = "";
                     showCustomNotification("LỖI", "Mã khuyến mãi không tồn tại hoặc đã hết hạn!", "ERROR");
                 }
             }
             recalculateTotals(); 

         } catch(Exception ex) {
             ex.printStackTrace();
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
            new javax.swing.border.EmptyBorder(15, 20, 15, 20)
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
                new EmptyBorder(5, 15, 8, 15)
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

            // [FIX SQL]: Tìm cả SĐT, tên chính xác, hoặc tên gần đúng
            String sql = "SELECT * FROM KhachHang WHERE sdt = ? OR hoVaTen = ? OR hoVaTen LIKE N'%' + ? + '%'";

            try (java.sql.Connection con = ConnectDB.getInstance().getConnection();
                 java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
                
                // Truyền từ khóa tìm kiếm cho 3 dấu chấm hỏi
                pst.setString(1, searchKeyword);
                pst.setString(2, searchKeyword);
                pst.setString(3, searchKeyword);
                
                try (java.sql.ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        String maKH = rs.getString("id");
                        String tenKH = rs.getString("hoVaTen");
                        String sdtKH = rs.getString("sdt");
                        int diemKH = rs.getInt("diemTichLuy");

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
                            CardLayout cl = (CardLayout)(lblBadgeLe.getParent().getLayout());
                            cl.show(lblBadgeLe.getParent(), "LINKED");
                        }
                        
                        recalculateTotals();
                    } else {
                        // Nếu user đổi ý không tạo nữa và quay lại, vẫn đổ thông tin cũ vào ô
                        if (nameToLink != null && !nameToLink.isEmpty()) {
                            txtName.setText(nameToLink);
                            txtName.setForeground(Color.BLACK);
                        }
                        
                        if (phoneToLink != null && !phoneToLink.isEmpty()) {
                            txtPhone.setText(phoneToLink);
                            txtPhone.setForeground(Color.BLACK);
                            txtSearch.setText(phoneToLink);
                            txtSearch.setForeground(Color.BLACK);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    private JTextField createStyledTextField(String placeholder) {
        JTextField txt = new JTextField(placeholder);
        txt.setPreferredSize(new Dimension(0, 36)); 
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setForeground(Color.GRAY);
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        
        txt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txt.getText().equals(placeholder)) {
                    txt.setText(""); txt.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txt.getText().isEmpty()) {
                    txt.setForeground(Color.GRAY); txt.setText(placeholder);
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
                if (txtSearchProduct.getText().equals(placeholderText)) {
                    txtSearchProduct.setText("");
                    txtSearchProduct.setForeground(Color.BLACK);
                    lblSearchIcon.setForeground(Color.decode("#1967D2")); 
                    pnlSearchWrapper.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode("#1967D2"), 1, true),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)
                    ));
                }
            }
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
                // FIX: Chỉ cho phép sửa Cột SL (2), ĐVT (1), và Nút xóa (6)
            	return c == 2 || c == 6 || c == 1; 
            }
        };
        
     // ====================================================================
        // 1. LẮNG NGHE SỰ THAY ĐỔI ĐỂ TỰ ĐỘNG NHÂN THÀNH TIỀN & ĐỔI ĐƠN GIÁ
        // ====================================================================
     // ====================================================================
        // 1. LẮNG NGHE SỰ THAY ĐỔI ĐỂ TỰ ĐỘNG NHÂN THÀNH TIỀN & ĐỔI ĐƠN GIÁ
        // ====================================================================
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
                                // Lấy Tên SP trực tiếp từ cột 0
                                String tenSP = productModel.getValueAt(row, 0).toString().trim();
                                String donViMoi = productModel.getValueAt(row, 1).toString().trim();
                                double giaBanMoi = 0;
                                
                                // Truy vấn Database để tìm giá mới dựa vào TÊN SẢN PHẨM thay vì Mã SP
                                try (java.sql.Connection con = ConnectDB.getInstance().getConnection()) {
                                    // 1. Thử tìm trong bảng Đơn vị quy đổi trước
                                    String sqlQuyDoi = "SELECT dv.gia FROM DonViDoLuong dv JOIN SanPham sp ON dv.sanPhamId = sp.id WHERE sp.ten = ? AND dv.ten = ?";
                                    try (java.sql.PreparedStatement pst = con.prepareStatement(sqlQuyDoi)) {
                                        pst.setString(1, tenSP);
                                        pst.setString(2, donViMoi);
                                        try (java.sql.ResultSet rs = pst.executeQuery()) {
                                            if (rs.next()) giaBanMoi = rs.getDouble("gia");
                                        }
                                    }
                                    
                                    // 2. Nếu không có trong bảng quy đổi, lấy giá gốc từ bảng SanPham
                                    if (giaBanMoi <= 0) {
                                        String sqlGoc = "SELECT giaBan FROM SanPham WHERE ten = ?";
                                        try (java.sql.PreparedStatement pst = con.prepareStatement(sqlGoc)) {
                                            pst.setString(1, tenSP);
                                            try (java.sql.ResultSet rs = pst.executeQuery()) {
                                                if (rs.next()) giaBanMoi = rs.getDouble("giaBan");
                                            }
                                        }
                                    }
                                } catch (Exception ex) {
                                    System.out.println("Lỗi truy vấn giá: " + ex.getMessage());
                                }
                                
                                // Ép Đơn giá mới lên Bảng (Cột 3)
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

                            // [QUAN TRỌNG - CHỐNG GIẬT UI] 
                            // CHỈ ghi đè lại ô Số Lượng nếu người dùng vừa thao tác trên Cột 2 (Số lượng) 
                            // VÀ nhập sai định dạng (vd: gõ chữ thay vì số). Tuyệt đối không can thiệp khi đang ở Cột 1.
                            if (col == 2 && !slStr.equals(String.valueOf(sl))) {
                                productModel.setValueAt(String.valueOf(sl), row, 2); 
                            }

                            // Lấy Đơn giá (đã được làm mới tự động nếu vừa đổi ĐVT ở Bước 1)
                            String giaStr = productModel.getValueAt(row, 3).toString().replaceAll("[^0-9]", "");
                            long donGia = 0;
                            try { donGia = Long.parseLong(giaStr); } catch (Exception ex) {}

                            // Tính lại Thành tiền và đẩy lên cột 5
                            long thanhTien = sl * donGia;
                            productModel.setValueAt(String.format("%,d", thanhTien).replace(',', '.') + "đ", row, 5);
                            
                            // Cập nhật lại Tổng Hóa Đơn bên dưới
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
                    
                    // --- ĐOẠN MỚI: LẤY TÊN SẢN PHẨM Ở CỘT 0 ---
                    String tenSP = "";
                    Object valTenSP = getValueAt(row, 0);
                    if (valTenSP != null) tenSP = valTenSP.toString().trim();
                    
                    JComboBox<String> cbDVT = new JComboBox<>();
                    cbDVT.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    cbDVT.setBackground(Color.WHITE);
                    
                    // --- ĐOẠN MỚI: LOAD ĐVT CHUẨN TỪ DATABASE DỰA THEO TÊN SẢN PHẨM ---
                    try (java.sql.Connection con = ConnectDB.getInstance().getConnection()) {
                        String sql = "SELECT dv.ten FROM DonViDoLuong dv JOIN SanPham sp ON dv.sanPhamId = sp.id WHERE sp.ten = ?";
                        try (java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
                            pst.setString(1, tenSP);
                            try (java.sql.ResultSet rs = pst.executeQuery()) {
                                boolean hasData = false;
                                while (rs.next()) {
                                    cbDVT.addItem(rs.getString("ten"));
                                    hasData = true;
                                }
                                // Đề phòng lỗi DB, nếu không có data thì nạp tạm đơn vị hiện tại
                                if (!hasData && !dvtHienTai.isEmpty()) {
                                    cbDVT.addItem(dvtHienTai);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        System.out.println("Lỗi tải ĐVT động: " + ex.getMessage());
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
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tbl.setShowGrid(false); 
        tbl.setShowHorizontalLines(true); 
        tbl.setGridColor(Color.decode("#F1F3F5"));

        tbl.getTableHeader().setBackground(Color.decode("#D9EAF7")); 
        tbl.getTableHeader().setForeground(Color.decode("#1E293B"));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tbl.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tbl.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        tbl.getColumnModel().getColumn(0).setPreferredWidth(250); 
        tbl.getColumnModel().getColumn(1).setPreferredWidth(60);  
        tbl.getColumnModel().getColumn(2).setPreferredWidth(50);  
        tbl.getColumnModel().getColumn(3).setPreferredWidth(100); 
        tbl.getColumnModel().getColumn(4).setPreferredWidth(50);  
        tbl.getColumnModel().getColumn(5).setPreferredWidth(100); 
        tbl.getColumnModel().getColumn(6).setPreferredWidth(40);  

       

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        // FIX 1: Bỏ cột Số Lượng (Cột 2) ra khỏi danh sách hiển thị Text thường
        tbl.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tbl.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tbl.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tbl.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        
        tbl.setSelectionBackground(Color.decode("#E0F2FE"));
        // 2. EDITOR (Bộ xử lý khi nhấp chuột vào)
        class SpinnerCellRenderer extends JSpinner implements javax.swing.table.TableCellRenderer {
            public SpinnerCellRenderer() {
                super(new SpinnerNumberModel(1, 1, 9999, 1));
                JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) getEditor();
                editor.getTextField().setHorizontalAlignment(JTextField.CENTER);
                editor.getTextField().setFont(new Font("Segoe UI", Font.BOLD, 14));
                
                // LÀM GỌN: Xóa sạch viền cứng nhiều lớp của Java bằng EmptyBorder
                setBorder(BorderFactory.createEmptyBorder());
                editor.setBorder(BorderFactory.createEmptyBorder());
                editor.getTextField().setBorder(BorderFactory.createEmptyBorder());
            }
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                try { setValue(Integer.parseInt(value.toString())); } catch (Exception e) {}
                
                // Đồng bộ nền khít 100% với dòng của bảng
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

            public SpinnerCellEditor() {
                // FIX 1: Tăng giới hạn tối đa lên 999.999 để bạn gõ số lượng lớn (như 10010) không bị lỗi
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
                    // FIX 2: Đã xóa sự kiện keyReleased ở đây để chống lỗi nhảy con trỏ chuột khi gõ
                });

                editor.getTextField().addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusGained(java.awt.event.FocusEvent e) {
                        SwingUtilities.invokeLater(editor.getTextField()::selectAll); 
                    }
                });
                
                // Khi bấm mũi tên Tăng/Giảm thì vẫn cho phép tiền nhảy ngay lập tức
                spinner.addChangeListener(e -> {
                    int row = tbl.getEditingRow();
                    if (row >= 0) {
                        productModel.setValueAt(spinner.getValue().toString(), row, 2);
                    }
                });
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                try { spinner.setValue(Integer.parseInt(value.toString())); } catch (Exception e) { spinner.setValue(1); }
                ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setBackground(Color.WHITE);
                return spinner;
            }

            @Override
            public Object getCellEditorValue() { return spinner.getValue().toString(); }

            // --- FIX 3: BỔ SUNG HÀM NÀY ---
            // Đảm bảo khi bạn gõ phím xong (Bấm Enter hoặc Click ra chỗ khác), số đang gõ dở sẽ được lưu lại
            @Override
            public boolean stopCellEditing() {
                try {
                    spinner.commitEdit(); // Ép JSpinner ghi nhận số vừa gõ
                } catch (java.text.ParseException e) {
                    // Bỏ qua lỗi parse
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
            String text = txtSearchProduct.getText().trim();

            if (text.isEmpty() || text.equals(placeholderText.toLowerCase()) || text.equals(placeholderText)) {
                suggestionPopup.setVisible(false);
                return;
            }

            // FIX: Hủy Worker cũ nếu người dùng gõ quá nhanh
            if (currentSearchWorker != null && !currentSearchWorker.isDone()) {
                currentSearchWorker.cancel(true);
            }

            currentSearchWorker = new SwingWorker<java.util.List<Object[]>, Void>() {
                @Override
                protected java.util.List<Object[]> doInBackground() throws Exception {
                    DAO.DAO_SanPham daoSP = new DAO.DAO_SanPham();
                    return daoSP.timKiemSanPhamBan(text);
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
                                if (count >= MAX_ITEMS) break; // Ngắt vòng lặp ngay khi đủ 12 món

                                String id = row[0].toString();
                                String ten = row[1].toString();
                                String donVi = row[2] != null ? row[2].toString() : "";
                                long giaBan = Math.round(Double.parseDouble(row[3].toString()));
                                String gia = String.valueOf(giaBan); 
                                String tonKho = row[4] != null ? row[4].toString() : "0";
                                String danhMuc = (row.length > 5 && row[5] != null) ? row[5].toString() : "Khác";

                                // Lấy VAT
                                String thueVat = "0%";
                                if (row.length > 6 && row[6] != null) {
                                    String rawVat = row[6].toString().trim();
                                    try {
                                        double v = Double.parseDouble(rawVat);
                                        if (v > 0 && v < 1) v = v * 100; 
                                        thueVat = (int)v + "%";
                                    } catch (Exception ex) {
                                        thueVat = rawVat + (rawVat.contains("%") ? "" : "%");
                                    }
                                } else {
                                    thueVat = "5%"; 
                                }

                                String iconType = "PACKAGE"; 
                                String dmCheck = danhMuc.toLowerCase();
                                if (dmCheck.contains("thuốc kê đơn")) iconType = "PILL";         
                                else if (dmCheck.contains("không kê đơn")) iconType = "BOX";          
                                else if (dmCheck.contains("mỹ phẩm")) iconType = "COSMETIC";     
                                else if (dmCheck.contains("chức năng") || dmCheck.contains("tpcn")) iconType = "LEAF";         
                                else if (dmCheck.contains("vật tư") || dmCheck.contains("y tế")) iconType = "MEDICAL_TOOL"; 

                                pnlList.add(createSuggestionItem(suggestionPopup, txtSearchProduct, iconType, ten, donVi, gia, tonKho, danhMuc, thueVat));
                                
                                count++; // Tăng biến đếm
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
            
            // --- THÊM MỚI: BẮT PHÍM ENTER ĐỂ THÊM NHANH ---
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    // Nếu có gợi ý đang mở và luồng tìm kiếm đã lấy được dữ liệu
                    if (suggestionPopup.isVisible() && currentSearchWorker != null && currentSearchWorker.isDone()) {
                        try {
                            java.util.List<Object[]> ketQua = currentSearchWorker.get();
                            if (ketQua != null && !ketQua.isEmpty()) {
                                // Tự động lấy món ĐẦU TIÊN trong danh sách gợi ý
                                Object[] firstItem = ketQua.get(0);
                                
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

                                // Logic: Kiểm tra xem đã có trong giỏ hàng chưa
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

                                if (daTonTai) {
                                    currentQty++; // Tăng 1
                                    productModel.setValueAt(String.valueOf(currentQty), rowIndex, 2);
                                    productModel.setValueAt(String.format("%,d", giaBan * currentQty).replace(',', '.') + "đ", rowIndex, 5);
                                } else {
                                    productModel.addRow(new Object[]{
                                        name, unit, "1", price, thueVat, String.format("%,d", giaBan).replace(',', '.') + "đ", danhMuc 
                                    });
                                }
                                
                                recalculateTotals();
                                suggestionPopup.setVisible(false); // Đóng popup
                                txtSearchProduct.setText(""); // Xóa trắng ô để gõ/bắn mã tiếp theo
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
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
 // ========================================================================
    // BỘ THÔNG BÁO UI/UX GIAO DIỆN CHUẨN MỚI
    // ========================================================================
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
        pnlHeader.setPreferredSize(new Dimension(0, 45));
        JLabel lblTitle = new JLabel(titleText.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.CENTER);

        // 2. BODY 
        JPanel pnlBody = new JPanel(null);
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setPreferredSize(new Dimension(420, 110));

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
        pnlIcon.setBounds(20, 25, 50, 50);
        pnlIcon.setOpaque(false);

        JTextArea msg = new JTextArea(message);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(Color.decode("#333333"));
        msg.setWrapStyleWord(true);
        msg.setLineWrap(true);
        msg.setOpaque(false);
        msg.setEditable(false);
        msg.setFocusable(false);
        
        JScrollPane scroll = new JScrollPane(msg);
        scroll.setBounds(85, 20, 315, 80);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        pnlBody.add(pnlIcon);
        pnlBody.add(scroll);

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
        dialog.pack();
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

            try (java.sql.Connection con = ConnectDB.getInstance().getConnection()) {
                
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

                DAO.DAO_HoaDon daoHD = new DAO.DAO_HoaDon();
                DAO.DAO_ChiTietHoaDon daoCT = new DAO.DAO_ChiTietHoaDon();
                
                Entity.HoaDon hd = new Entity.HoaDon();
                String maHD = (this.maHDDangSua != null && !this.maHDDangSua.isEmpty()) ? this.maHDDangSua : phatSinhMaHoaDon() + "-LuuNhap";
                hd.setId(maHD);
                hd.setNgayLapHD(java.time.LocalDateTime.now());
                
                Entity.NhanVien nv = new Entity.NhanVien("DS-0001"); 
                hd.setNhanVienId(nv);

                String idKH = null;
                
                if (!sdt.isEmpty() || !khach.equals("Khách lẻ")) {
                    if (!sdt.isEmpty()) {
                        try (java.sql.PreparedStatement pstCheck = con.prepareStatement("SELECT id FROM KhachHang WHERE sdt = ?")) {
                            pstCheck.setString(1, sdt);
                            try (java.sql.ResultSet rsCheck = pstCheck.executeQuery()) {
                                if (rsCheck.next()) {
                                    idKH = rsCheck.getString("id"); 
                                }
                            }
                        }
                    }

                    if (idKH == null) {
                        idKH = "KH" + (System.currentTimeMillis() % 10000000); 
                        String sqlInsert = "INSERT INTO KhachHang (id, hoVaTen, sdt, ngayTao, diemTichLuy) VALUES (?, ?, ?, GETDATE(), 0)";
                        try (java.sql.PreparedStatement pstInsert = con.prepareStatement(sqlInsert)) {
                            pstInsert.setString(1, idKH);
                            pstInsert.setNString(2, khach); 
                            
                            if (!sdt.isEmpty()) pstInsert.setString(3, sdt); 
                            else pstInsert.setNull(3, java.sql.Types.VARCHAR); 
                            
                            pstInsert.executeUpdate();
                        }
                    }
                }

                if (idKH != null) {
                    Entity.KhachHang khObj = new Entity.KhachHang();
                    khObj.setId(idKH);
                    hd.setKhachHangId(khObj);
                } else {
                    hd.setKhachHangId(null); 
                }

                String strKeDon = "";
                if (pnlDonThuoc != null && pnlDonThuoc.isVisible()) {
                    String bs = txtBacSi.getText().trim();
                    String cs = txtCoSo.getText().trim();
                    String cd = txtChuanDoan.getText().trim();
                    if (cd.contains("Chẩn đoán bệnh")) cd = ""; 
                    strKeDon = " | BS:" + bs + " | CS:" + cs + (cd.isEmpty() ? "" : " | CD:" + cd);
                }

                hd.setLoaiHD(Enumeration.LoaiHoaDon.BAN_HANG); 
                hd.setPhuongThucThanhToan(Enumeration.PhuongThucThanhToan.TIEN_MAT);
                hd.setGhiChu("Lưu nháp" + strKeDon); 

                boolean isHDSaved = false;

                if (this.editingModelRow != -1) {
                    String sqlUpdate = "UPDATE HoaDon SET khachHangId = ?, ghiChu = ?, ngayLapHD = ? WHERE id = ?";
                    try (java.sql.PreparedStatement pstUpd = con.prepareStatement(sqlUpdate)) {
                        if (idKH != null) pstUpd.setString(1, idKH); else pstUpd.setNull(1, java.sql.Types.NVARCHAR);
                        pstUpd.setString(2, hd.getGhiChu()); 
                        pstUpd.setTimestamp(3, java.sql.Timestamp.valueOf(hd.getNgayLapHD()));
                        pstUpd.setString(4, maHD);
                        pstUpd.executeUpdate();
                    }

                    String sqlDel = "DELETE FROM ChiTietHoaDon WHERE hoaDonId = ?";
                    try (java.sql.PreparedStatement pstDel = con.prepareStatement(sqlDel)) {
                        pstDel.setString(1, maHD);
                        pstDel.executeUpdate();
                    }
                    isHDSaved = true;
                } else {
                    isHDSaved = daoHD.themHoaDon(hd);
                }
                
                if (isHDSaved) {
                    for (int i = 0; i < productModel.getRowCount(); i++) {
                        String tenSP = productModel.getValueAt(i, 0).toString().trim();
                        String tenDVT = productModel.getValueAt(i, 1).toString().trim();
                        int soLuong = Integer.parseInt(productModel.getValueAt(i, 2).toString());
                        
                        String maSP = "", maDVT = "";
                        String sql = "SELECT sp.id AS MaSP, dv.id AS MaDVT FROM SanPham sp JOIN DonViDoLuong dv ON sp.id = dv.sanPhamId WHERE sp.ten = ? AND dv.ten = ?";
                        
                        try (java.sql.PreparedStatement pstSP = con.prepareStatement(sql)) {
                            pstSP.setNString(1, tenSP);
                            pstSP.setNString(2, tenDVT);
                            try (java.sql.ResultSet rsSP = pstSP.executeQuery()) {
                                if(rsSP.next()) {
                                    maSP = rsSP.getString("MaSP");
                                    maDVT = rsSP.getString("MaDVT");
                                }
                            }
                        }
                        if (maSP.isEmpty()) continue; 

                        Entity.ChiTietHoaDon cthd = new Entity.ChiTietHoaDon();
                        cthd.setHoaDonId(hd);
                        
                        Entity.SanPham sp = new Entity.SanPham();
                        sp.setId(maSP);
                        cthd.setSanPhamId(sp);
                        
                        Entity.DonViDoLuong dv = new Entity.DonViDoLuong();
                        dv.setId(maDVT);
                        cthd.setDonViDoLuongId(dv);
                        cthd.setSoLuong(soLuong);
                        
                        daoCT.themCTHD(cthd); 
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
                    
                    if (!isAutoSave) {
                        showCustomNotification("THÀNH CÔNG", "Đã lưu nháp hóa đơn thành công!", "SUCCESS");
                        this.dispose(); 
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
 // THÊM: Biến "String vat" ở cuối cùng
    private JPanel createSuggestionItem(JPopupMenu popup, JTextField txtSearch, String iconType, String name, String unit, String price, String stock, String danhMuc, String vat) {
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
            if (dm.contains("thuốc kê đơn")) {
                badgeText = "Kê đơn"; bgColor = Color.decode("#FEE2E2"); fgColor = Color.decode("#DC2626");
            } else if (dm.contains("không kê đơn")) {
                badgeText = "Không kê đơn"; bgColor = Color.decode("#DBEAFE"); fgColor = Color.decode("#2563EB");
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

        JLabel lblName = new JLabel(name);
        JLabel lblIcon = new JLabel(new MenuIcon(iconType));
        lblIcon.setForeground(Color.decode("#1967D2")); 
        
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(Color.decode("#111827"));

        JLabel lblUnit = new JLabel(unit);
        lblUnit.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUnit.setForeground(Color.decode("#6B7280"));

        pnlLeft.add(lblBadge); 
        pnlLeft.add(lblName);
        pnlLeft.add(lblUnit);

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
                        long donGia = Long.parseLong(price);
                        productModel.setValueAt(String.format("%,d", donGia * currentQty).replace(',', '.') + "đ", rowIndex, 5);
                    } else {
                        // FIX: Truyền biến 'vat' lấy từ DB vào đây thay vì gán cứng "5%"
                        productModel.addRow(new Object[]{
                            name, unit, "1", price, vat, String.format("%,d", Integer.parseInt(price)).replace(',', '.') + "đ", 
                            danhMuc 
                        });
                    }
                    lblCount.setText("[" + currentQty + "]"); 
                    txtSearch.setText(""); 
                    
                } else if (isRightClick) {
                    if (daTonTai) {
                        currentQty--;
                        if (currentQty > 0) {
                            productModel.setValueAt(String.valueOf(currentQty), rowIndex, 2);
                            long donGia = Long.parseLong(price);
                            productModel.setValueAt(String.format("%,d", donGia * currentQty).replace(',', '.') + "đ", rowIndex, 5);
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
                // Khi click vào tag, điền mã vào ô text
                if (txtInput != null) {
                    txtInput.setText(maKM.trim());
                    txtInput.setForeground(Color.decode("#10B981")); // Đổi màu xanh lá
                }
                
                // Ép buộc hệ thống nhận diện mã này
                maKhuyenMaiApDung = maKM.trim();
                
                // Gọi tính lại tiền thay vì bấm nút ảo (tránh lỗi NullPointerException)
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
        int year = java.time.Year.now().getValue();
        String maMoi = "HD-" + year + "-0001"; 
        
        // Tìm tất cả các mã trong năm hiện tại
        String sql = "SELECT id FROM HoaDon WHERE id LIKE 'HD-" + year + "-%'";
        
        try (java.sql.Connection con = ConnectDB.getInstance().getConnection();
             java.sql.Statement st = con.createStatement();
             java.sql.ResultSet rs = st.executeQuery(sql)) {
             
            int maxStt = 0;
            while (rs.next()) {
                String id = rs.getString("id"); // VD: HD-2026-0001 hoặc HD-2026-0001-LuuNhap
                String[] parts = id.split("-");
                // Lấy phần tử số 3 (index 2) chính là số thứ tự 0001
                if (parts.length >= 3) {
                    try {
                        int stt = Integer.parseInt(parts[2]);
                        if (stt > maxStt) {
                            maxStt = stt;
                        }
                    } catch (Exception ignored) {}
                }
            }
            
            // Nếu đã có hóa đơn trong năm, cộng thêm 1
            if (maxStt > 0) {
                maMoi = String.format("HD-%d-%04d", year, maxStt + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maMoi;
    }
    
    private void updateVoucherTagsUI() {
        if (pnlVoucherTags == null) return;
        
        java.util.List<String> listApplied = new java.util.ArrayList<>();
        if (this.maKhuyenMaiApDung != null && !this.maKhuyenMaiApDung.isEmpty()) {
            for (String s : this.maKhuyenMaiApDung.split(",")) {
                listApplied.add(s.trim());
            }
        }
        
        for (Component comp : pnlVoucherTags.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel pnl = (JPanel) comp;
                String maKMTag = pnl.getName(); 
                
                if (maKMTag != null && pnl.getComponentCount() > 0) {
                    maKMTag = maKMTag.trim();
                    JLabel lblText = (JLabel) pnl.getComponent(0);
                    
                    if (listApplied.contains(maKMTag)) {
                        pnl.setBackground(Color.decode("#D1FAE5")); 
                        pnl.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.decode("#10B981"), 1, true),
                            new javax.swing.border.EmptyBorder(4, 8, 4, 8)
                        ));
                        lblText.setForeground(Color.decode("#047857"));
                        lblText.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else {
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
                DAO_ChiTietHoaDon daoCT = new DAO_ChiTietHoaDon();
                java.util.List<Object[]> dsMonHang = daoCT.layDuLieuChoTaoHoaDon(maHoaDon);
                
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
                    
                    String tenSP = fullRow[0].toString();
                    try (java.sql.Connection con = ConnectDB.getInstance().getConnection();
                         java.sql.PreparedStatement pst = con.prepareStatement("SELECT thueVAT FROM SanPham WHERE ten = ?")) {
                        pst.setString(1, tenSP);
                        try (java.sql.ResultSet rs = pst.executeQuery()) {
                            if (rs.next()) {
                                double v = rs.getDouble("thueVAT");
                                if (v > 0 && v < 1) v = v * 100; 
                                fullRow[4] = (int)v + "%";
                            }
                        }
                    } catch(Exception ex) {
                        System.out.println("Lỗi lấy VAT cho hóa đơn nháp: " + ex.getMessage());
                    }
                    
                    productModel.addRow(fullRow);
                }
                
                DAO_HoaDon daoHD = new DAO_HoaDon();
                HoaDon hdGoc = daoHD.layHoaDonTheoMa(maHoaDon);
                
                if (hdGoc != null) {
                    if (hdGoc.getKhachHangId() != null && hdGoc.getKhachHangId().getId() != null) {
                        DAO_KhachHang daoKH = new DAO_KhachHang();
                        KhachHang kh = daoKH.timKhachHangTheoMa(hdGoc.getKhachHangId().getId());
                        
                        if (kh != null) {
                            this.isCustomerLinked = true;
                            this.linkedTenKH = kh.getHoVaTen();
                            this.linkedSdtKH = kh.getSdt();

                            lblLinkedName.setText(kh.getHoVaTen());
                            lblLinkedSub.setText(kh.getSdt());
                            lblLinkedPoints.setText("Điểm tích lũy: " + kh.getDiemTichLuy()); 
                            
                            if (pnlInputFields != null) pnlInputFields.setVisible(false); 
                            if (pnlLinkedCustomer != null) pnlLinkedCustomer.setVisible(true); 
                        }
                    } else {
                        this.isCustomerLinked = false;
                        if (pnlInputFields != null) pnlInputFields.setVisible(true);
                        if (pnlLinkedCustomer != null) pnlLinkedCustomer.setVisible(false);
                        
                        String savedPhoneOrName = txtPhone.getText().trim();
                        if (!savedPhoneOrName.isEmpty() && !savedPhoneOrName.equals("Số điện thoại (tuỳ chọn)")) {
                            txtSearch.setText(savedPhoneOrName);
                            txtSearch.setForeground(Color.BLACK);
                            
                            if (txtSearch.getParent() != null && txtSearch.getParent().getComponentCount() > 1) {
                                Component btn = txtSearch.getParent().getComponent(1);
                                if (btn instanceof JButton) {
                                    ((JButton) btn).doClick();
                                }
                            }
                        }
                    }
                    
                    if (hdGoc.getKhuyenMaiId() != null) {
                        txtVoucherInput.setText(hdGoc.getKhuyenMaiId().getId());
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
    private void loadQRCodeVCB(JLabel lblQRCode) {
        lblQRCode.setIcon(null);
        lblQRCode.setText("Đang tạo mã QR Vietcombank...");

        // 1. Lấy thẳng tổng tiền cuối cùng đã được hệ thống tính toán
        long finalTotalAmount = this.tongHoaDon;
        
        // Nếu chưa có món hàng nào (0đ) thì không cần gọi API tạo mã làm gì
        if (finalTotalAmount <= 0) {
            lblQRCode.setText("Vui lòng thêm sản phẩm!");
            return;
        }

        // 2. CẤU HÌNH THÔNG TIN TÀI KHOẢN VCB
        String bankBin = "vcb"; 
        String stk = "1038858525"; 
        String tenTK = "MAI TRUNG KIEN"; 
        
        String maHD = (this.maHDDangSua != null && !this.maHDDangSua.isEmpty()) ? this.maHDDangSua : "HD_MOI";
        String loiNhan = "Thanh toan " + maHD;

        // 3. Tiến hành gọi API chạy ngầm
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                String encodedLoiNhan = java.net.URLEncoder.encode(loiNhan, "UTF-8").replace("+", "%20");
                String encodedTenTK = java.net.URLEncoder.encode(tenTK, "UTF-8").replace("+", "%20");
                
                String apiUrl = String.format(
                    "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                    bankBin, stk, finalTotalAmount, encodedLoiNhan, encodedTenTK
                );

                java.net.URL url = new java.net.URL(apiUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                connection.setConnectTimeout(5000); 
                connection.setReadTimeout(5000);
                
                java.io.InputStream in = connection.getInputStream();
                java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(in);
                
                if (image == null) {
                    throw new Exception("Không nhận được dữ liệu ảnh từ API VietQR!");
                }
                
                Image scaledImg = image.getScaledInstance(230, 260, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImg);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    lblQRCode.setText("");
                    lblQRCode.setIcon(icon);
                } catch (Exception ex) {
                    System.err.println("Lỗi load QR: " + ex.getMessage());
                    lblQRCode.setText("Lỗi tạo QR. Vui lòng thử lại!");
                }
            }
        };
        worker.execute();
    }
 // ========================================================================
    // MODULE: TỰ ĐỘNG ÁP DỤNG ĐA KHUYẾN MÃI & THÊM QUÀ TẶNG (V12 - FIX TRÀN MẢNG)
    // ========================================================================
    private void tuDongApDungKhuyenMai() {
        // [1] DỌN DẸP QUÀ TẶNG CŨ TRÊN GIAO DIỆN CHỐNG LẶP
        isTableUpdating = true; 
        for (int i = productModel.getRowCount() - 1; i >= 0; i--) {
            String ten = productModel.getValueAt(i, 0).toString();
            if (ten.startsWith("🎁")) {
                productModel.removeRow(i);
            }
        }
        isTableUpdating = false;

        // [2] TÍNH TỔNG TIỀN & SỐ LƯỢNG THỰC TẾ (BỎ QUA QUÀ TẶNG)
        long tongTienBill = this.tamTinh + this.vat;
        int tongSoLuongSP_ThucTe = 0;
        String tenSpTangMau = ""; 
        String dvtTangMau = "";
        
        for (int i = 0; i < productModel.getRowCount(); i++) {
            try {
                String tenSp = productModel.getValueAt(i, 0).toString();
                Object slObj = productModel.getValueAt(i, 2);
                if (slObj != null) {
                    tongSoLuongSP_ThucTe += Integer.parseInt(slObj.toString().trim());
                    // Lấy tên SP đầu tiên để làm mẫu in ra chữ 🎁 Quà tặng
                    if (!tenSp.startsWith("🎁") && tenSpTangMau.isEmpty()) {
                        tenSpTangMau = tenSp;
                        dvtTangMau = productModel.getValueAt(i, 1).toString();
                    }
                }
            } catch (Exception e) {}
        }

        // CHỐT CHẶN 1: Giỏ hàng trống không thì cấm mọi hoạt động
        if (tongTienBill == 0 && tongSoLuongSP_ThucTe == 0) {
            this.tienGiamGia = 0;
            this.maKhuyenMaiApDung = "";
            resetVoucherUI();
            updateVoucherTagsUI(); 
            if (lblTotalItems != null) lblTotalItems.setText("Tổng sản phẩm: 0");
            return; 
        }

        long tongTienGiamDoc = 0;
        long tongSoLuongTang = 0; 
        java.util.List<String> danhSachMa = new java.util.ArrayList<>();

        // [3] KẾT NỐI DATABASE KIỂM TRA ĐIỀU KIỆN (CHUẨN XÁC 100%)
        String sql = "SELECT k.id, h.loaiHinhThuc, h.giaTri AS mucGiam, ISNULL(d.giaTri, 0) AS donToiThieu " +
                     "FROM KhuyenMai k " +
                     "JOIN HinhThucKhuyenMai h ON k.id = h.khuyenMaiId " +
                     "LEFT JOIN DieuKienKhuyenMai d ON k.id = d.khuyenMaiId " +
                     "WHERE CAST(k.ngayBatDau AS DATE) <= CAST(GETDATE() AS DATE) " +
                     "AND CAST(k.ngayKetThuc AS DATE) >= CAST(GETDATE() AS DATE)";

        java.sql.Connection con = null;
        java.sql.PreparedStatement pst = null;
        java.sql.ResultSet rs = null;

        try {
            con = ConnectDB.getInstance().getConnection();
            pst = con.prepareStatement(sql);
            rs = pst.executeQuery();
             
            while (rs.next()) {
                String maKM = rs.getString("id").trim();
                String loaiKM = rs.getString("loaiHinhThuc") != null ? rs.getString("loaiHinhThuc").toUpperCase() : "";
                double giaTriGiam = rs.getDouble("mucGiam");
                double dieuKien = rs.getDouble("donToiThieu");

                boolean duDieuKien = false;

                // CHỐT CHẶN 2: CHỈ ÁP DỤNG KHI ĐỦ ĐIỀU KIỆN THEO LOGIC MÁY TÍNH
                if (dieuKien <= 0) {
                    // Nếu điều kiện là 0đ -> Chỉ cho phép mã giảm tiền áp dụng. 
                    // NGHIÊM CẤM mã "Tặng Sản Phẩm" áp dụng bừa khi điều kiện = 0.
                    if (!loaiKM.contains("TANG")) {
                        duDieuKien = true; 
                    }
                } else if (dieuKien < 1000) { 
                    // Nếu điều kiện nhỏ hơn 1000 (VD: 3 viên) -> Xét theo TỔNG SỐ LƯỢNG
                    if (tongSoLuongSP_ThucTe >= dieuKien) {
                        duDieuKien = true;
                    }
                } else {
                    // Nếu điều kiện lớn hơn 1000 (VD: 50.000đ) -> Xét theo TỔNG TIỀN HÓA ĐƠN
                    if (tongTienBill >= dieuKien) {
                        duDieuKien = true;
                    }
                }

                // CỘNG DỒN KHUYẾN MÃI NẾU PASS CHỐT CHẶN
                if (duDieuKien) {
                    danhSachMa.add(maKM);
                    
                    if (loaiKM.contains("TANG")) {
                        // Tính số lượng tặng (Hỗ trợ cấp số nhân: Mua 6 tặng 2)
                        long slTang = (long) (giaTriGiam > 0 ? giaTriGiam : 1);
                        if (dieuKien > 0 && dieuKien < 1000) {
                            long heSo = tongSoLuongSP_ThucTe / (long) dieuKien; 
                            slTang = slTang * heSo;
                        }
                        tongSoLuongTang += slTang;
                    } else if (loaiKM.contains("PHAN_TRAM")) {
                        tongTienGiamDoc += (long) (tongTienBill * (giaTriGiam / 100.0));
                    } else {
                        tongTienGiamDoc += (long) giaTriGiam;
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Lỗi tự động quét khuyến mãi: " + ex.getMessage());
        } finally {
            // LƯU Ý SỐNG CÒN: Tuyệt đối KHÔNG đóng biến "con" để bảo vệ luồng kết nối chính
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pst != null) pst.close(); } catch (Exception e) {}
        }

        // [4] BẮN MÓN QUÀ LÊN BẢNG HIỂN THỊ
        if (tongSoLuongTang > 0 && !tenSpTangMau.isEmpty()) {
            isTableUpdating = true; 
            productModel.addRow(new Object[]{
                "🎁 " + tenSpTangMau, dvtTangMau, String.valueOf(tongSoLuongTang), "0đ", "0%", "0đ", "Hàng tặng" 
            });
            isTableUpdating = false;
        }

        // [5] CẬP NHẬT KẾT QUẢ VÀO HỆ THỐNG
        this.tienGiamGia = tongTienGiamDoc;
        this.maKhuyenMaiApDung = String.join(", ", danhSachMa);

        if (!danhSachMa.isEmpty()) {
            txtVoucherInput.setText(this.maKhuyenMaiApDung + " (Đã áp dụng " + danhSachMa.size() + " mã)");
            txtVoucherInput.setBackground(Color.decode("#DCFCE7"));
            txtVoucherInput.setForeground(Color.decode("#059669"));
            txtVoucherInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#10B981"), 2),
                new javax.swing.border.EmptyBorder(0, 10, 0, 10)
            ));
        } else {
            resetVoucherUI();
        }
        
        if (lblTotalItems != null) {
            lblTotalItems.setText(String.format("Tổng sản phẩm: %d", tongSoLuongSP_ThucTe + tongSoLuongTang));
        }
        
        // Gọi hàm để tô màu các Tag bên dưới
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
}