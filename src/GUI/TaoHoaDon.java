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
        setSize(900, 800);
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
                int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn HỦY hóa đơn này không?", "Xác nhận hủy", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    // FIX: Thêm try-with-resources để đóng PreparedStatement và Connection
                    if (this.maHDDangSua != null && !this.maHDDangSua.isEmpty()) {
                        String sql = "UPDATE HoaDon SET ghiChu = N'Đã hủy' WHERE id = ?"; 
                        try (java.sql.Connection con = ConnectDB.getInstance().getConnection();
                             java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
                            
                            pst.setString(1, this.maHDDangSua);
                            pst.executeUpdate();
                            
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(this, "Lỗi khi hủy: " + ex.getMessage());
                            return;
                        }
                    }

                    if (this.editingModelRow != -1) {
                        mainTableModel.setValueAt("Đã hủy", this.editingModelRow, 6); 
                    }

                    JOptionPane.showMessageDialog(this, "Đã hủy hóa đơn thành công!");
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
                JOptionPane.showMessageDialog(this, "Vui lòng thêm ít nhất 1 sản phẩm vào hóa đơn!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (pnlDonThuoc.isVisible()) {
                String bacSi = txtBacSi.getText().trim();
                String coSo = txtCoSo.getText().trim();
                
                if (bacSi.isEmpty() || bacSi.contains("BS. Nguyễn") || 
                    coSo.isEmpty() || coSo.contains("BV Bạch Mai")) {
                    JOptionPane.showMessageDialog(this, "Đơn hàng này có THUỐC KÊ ĐƠN.\nVui lòng nhập đầy đủ Tên Bác Sĩ và Cơ Sở Khám Bệnh!", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                    return; 
                }
            }
            String khach = isCustomerLinked ? linkedTenKH : txtName.getText();
            if(khach.equals("Tên khách (bỏ trống = Khách lẻ)") || khach.trim().isEmpty()) khach = "Khách lẻ";
            String sdt = isCustomerLinked ? linkedSdtKH : txtPhone.getText().replace("Số điện thoại (tuỳ chọn)", "");
            String tongTien = lblTotalPriceValue.getText();

            // FIX: Sử dụng try-with-resources cho Connection bao bọc toàn bộ giao dịch thanh toán
            try (java.sql.Connection con = ConnectDB.getInstance().getConnection()) {

                String maHDMoi = (editingModelRow != -1) ? maHDDangSua.replace("-TEMP", "") : phatSinhMaHoaDon();
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
                            if(diemDaDung > 0) msgThongBao += "➖ Đã sử dụng: " + diemDaDung + " điểm\n";
                            if(diemCongMoi > 0) msgThongBao += "➕ Tích lũy thêm: " + diemCongMoi + " điểm\n";
                            
                            JOptionPane.showMessageDialog(this, msgThongBao, "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(this, "Thanh toán thành công nhưng có lỗi khi cập nhật điểm!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Thanh toán thành công!\nHóa đơn và Tồn kho đã được cập nhật.", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                    }

                    String ngayStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    if (editingModelRow != -1) {
                        mainTableModel.setValueAt(maHDMoi, editingModelRow, 0); 
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
                    JOptionPane.showMessageDialog(this, "Lỗi Database: Kho không đủ hàng hoặc dữ liệu sai!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } catch(Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi Nghiêm Trọng", JOptionPane.ERROR_MESSAGE);
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

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlLeft.setOpaque(false);
        
        JLabel lblIcon = new JLabel("P", SwingConstants.CENTER); 
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblIcon.setForeground(Color.decode("#EE4D2D"));
        lblIcon.setBorder(BorderFactory.createLineBorder(Color.decode("#EE4D2D"), 2, true));
        lblIcon.setPreferredSize(new Dimension(26, 26));

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
        
        long tongTien = 0;
        int soLuongSanPham = 0;

        for (int i = 0; i < productModel.getRowCount(); i++) {
            try {
                // 1. Lấy Số lượng (Thường ở cột 2)
                int sl = 0;
                Object slObj = getSafeValue(productModel, i, 2);
                if (slObj != null) {
                    sl = Integer.parseInt(slObj.toString().trim());
                }

                // 2. Lấy Đơn giá (Thường ở cột 3) - Cắt bỏ ký tự 'đ' và dấu chấm
                long donGia = 0;
                Object donGiaObj = getSafeValue(productModel, i, 3);
                if (donGiaObj != null) {
                    String donGiaStr = donGiaObj.toString().replaceAll("[^0-9]", "");
                    if (!donGiaStr.isEmpty()) donGia = Long.parseLong(donGiaStr);
                }

                // 3. Lấy Tiền giảm giá mỗi SP (Thường ở cột 4)
                long tienGiamSP = 0;
                Object giamObj = getSafeValue(productModel, i, 4);
                if (giamObj != null) {
                     String giamStr = giamObj.toString().replaceAll("[^0-9]", "");
                     if (!giamStr.isEmpty()) tienGiamSP = Long.parseLong(giamStr);
                }
                
                // 4. Lấy Hệ số quy đổi (Thường ở cột 10)
                int heSo = 1;
                Object heSoObj = getSafeValue(productModel, i, 10);
                if (heSoObj != null) {
                    String heSoStr = heSoObj.toString().trim();
                    if (!heSoStr.isEmpty()) heSo = Integer.parseInt(heSoStr);
                }

                // 5. Cộng dồn tiền ( (Số lượng * Đơn giá / Hệ số) - Giảm giá SP )
                long thanhTien = (sl * donGia / (heSo > 0 ? heSo : 1)) - tienGiamSP;
                tongTien += (thanhTien > 0 ? thanhTien : 0); // Không cho âm tiền
                soLuongSanPham += sl;

            } catch (Exception ex) {
                System.out.println("Lỗi định dạng số ở dòng " + i + ": " + ex.getMessage());
            }
        }

        // Cập nhật lên UI
        if (lblTotalItems != null) {
            lblTotalItems.setText(String.format("Tổng sản phẩm: %d", soLuongSanPham));
        }

        // Tính các chỉ số phụ
        this.tongHoaDon = tongTien;
        this.tamTinh = tongTien;
        this.vat = (long) (this.tongHoaDon * 0.08); // Giả sử VAT 8%
        
        long totalToPay = this.tongHoaDon + this.vat;
        
        // Trừ điểm tích lũy khách hàng (nếu có)
        if (isDungDiem) {
            long tienGiamDiem = diemHienTaiKH * 100L;
            totalToPay -= tienGiamDiem;
        }
        
        // Trừ tiền giảm giá từ Khuyến mãi (nếu có)
        totalToPay -= tienGiamGia; 
        
        if (totalToPay < 0) totalToPay = 0;

        // Đổ số liệu ra giao diện (Đã format tiền tệ)
        if (lblSubTotal != null) lblSubTotal.setText(String.format("%,d", this.tamTinh).replace(',', '.') + "đ");
        if (lblVAT != null) lblVAT.setText(String.format("%,d", this.vat).replace(',', '.') + "đ");
        if (lblTotal != null) lblTotal.setText(String.format("%,d", totalToPay).replace(',', '.') + "đ");
        if (lblCustomerPay != null) lblCustomerPay.setText(String.format("%,d", totalToPay).replace(',', '.') + "đ");
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
            
            // FIX: Đóng DB an toàn cho hành động check áp mã
            try (java.sql.Connection con = ConnectDB.getInstance().getConnection();
                 java.sql.PreparedStatement pst = con.prepareStatement(sqlCheck)) {
                 
                pst.setString(1, code);
                try (java.sql.ResultSet rsCheck = pst.executeQuery()) {
                    if(rsCheck.next()) {
                        String loaiKM = rsCheck.getString("loaiHinhThuc");
                        double giaTri = rsCheck.getDouble("mucGiam");
                        long donToiThieu = (long) rsCheck.getDouble("donToiThieu");

                        if (tongTienDK < donToiThieu) {
                            JOptionPane.showMessageDialog(this, 
                                "Chưa đạt giá trị đơn tối thiểu (" + String.format("%,d", donToiThieu).replace(',', '.') + "đ) để áp dụng mã này!", 
                                "Cảnh báo", JOptionPane.WARNING_MESSAGE);
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
                        JOptionPane.showMessageDialog(this, "Mã khuyến mãi không tồn tại hoặc đã hết hạn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
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
        JPanel pnl = new JPanel(new BorderLayout(0, 10)); 
        pnl.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel(title);
        if (iconType != null) {
            lblTitle.setIcon(new MenuIcon(iconType)); 
            lblTitle.setIconTextGap(8); 
        }
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.decode("#212B36"));

        pnl.add(lblTitle, BorderLayout.NORTH);
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

        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    customerSuggestionPopup.setVisible(false);
                    return;
                }
                String text = txtSearch.getText().trim();
                if (text.isEmpty()) {
                    customerSuggestionPopup.setVisible(false);
                    return;
                }

                customerSuggestionPopup.removeAll();
                boolean hasResult = false;

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
                                customerSuggestionPopup.add(createCustomerSuggestionItem(ma, ten, sdt, diem));
                                hasResult = true;
                                count++;
                            }
                            if (count >= 5) break; 
                        }
                    }
                }

                if (hasResult) {
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
        
        JPanel pnlPoints = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlPoints.setOpaque(false);
        lblLinkedPoints = new JLabel("200 điểm", SwingConstants.RIGHT);
        lblLinkedPoints.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLinkedPoints.setForeground(Color.decode("#9333EA")); 
        lblLinkedMoney = new JLabel("≈ 200.000đ", SwingConstants.RIGHT);
        lblLinkedMoney.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLinkedMoney.setForeground(Color.GRAY);
        pnlPoints.add(lblLinkedPoints);
        pnlPoints.add(lblLinkedMoney);
        
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

            pnlLinkedCustomer.setVisible(false);
            pnlInputFields.setVisible(true);
            CardLayout cl = (CardLayout)(pnlBadgeWrap.getLayout());
            cl.show(pnlBadgeWrap, "LE");
            pnlBody.revalidate(); pnlBody.repaint();
        });
        
        JPanel pnlRightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlRightInfo.setOpaque(false);
        pnlRightInfo.add(pnlPoints);
        pnlRightInfo.add(btnUnlink);
        
        pnlLinkedCustomer.add(lblLinkedAvatar, BorderLayout.WEST);
        pnlLinkedCustomer.add(pnlNameInfo, BorderLayout.CENTER);
        pnlLinkedCustomer.add(pnlRightInfo, BorderLayout.EAST);
        pnlLinkedCustomer.setVisible(false); 

        pnlBody.add(pnlSearch);
        pnlBody.add(pnlWarn);
        pnlBody.add(pnlLinkedCustomer);

        btnSearch.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();
            if (keyword.isEmpty() || keyword.equals("Nhập SĐT hoặc mã KH để liên kết điểm thưởng...")) return;

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
                pnlWarn.setVisible(false);
                pnlInputFields.setVisible(false);     
                pnlLinkedCustomer.setVisible(true);   
                CardLayout cl = (CardLayout)(pnlBadgeWrap.getLayout());
                cl.show(pnlBadgeWrap, "LINKED");      
            } else {
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
                // 1. Ghi nhớ số điện thoại đang gõ dở vào biến toàn cục
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

                
                // 2. Cố gắng lưu nháp (Nếu giỏ hàng có đồ)
                luuNhapHoaDon(true); 
                
                // 3. GHI NHỚ ĐỂ MỞ LẠI (ĐÃ FIX LỖI GIỎ HÀNG TRỐNG)
                if (this.maHDDangSua != null && !this.maHDDangSua.isEmpty()) {
                    ManHinhBanHang.pendingDraftIdToOpen = this.maHDDangSua; // Mở lại đơn cũ
                } else {
                    ManHinhBanHang.pendingDraftIdToOpen = "NEW_INVOICE"; // Báo hiệu mở đơn mới toanh
                }
                
                // 4. Đóng popup hiện tại 
                this.dispose();
                
                // 5. Nhảy sang tab Khách hàng
                ((MainDashboard) owner).chuyenSangTabKhachHang(true); 
                
                JOptionPane.showMessageDialog(owner, 
                    "Đã chuyển sang màn hình Khách Hàng.\n" +
                    "Tạo xong khách hàng, hãy bấm lại vào tab Bán Hàng, hệ thống sẽ tự động mở lại hóa đơn này!", 
                    "Hướng dẫn", JOptionPane.INFORMATION_MESSAGE);
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
            public boolean isCellEditable(int row, int column) {
                return column == 2 || column == 3; 
            }
        };
        productModel.addTableModelListener(e -> {
            if (isTableUpdating) return; 

            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                
                if (row >= 0 && (col == 2 || col == 3)) {
                    SwingUtilities.invokeLater(() -> {
                        isTableUpdating = true; 
                        try {
                            String slStr = productModel.getValueAt(row, 2).toString().trim();
                            int sl = 1;
                            try {
                                sl = Integer.parseInt(slStr);
                                if (sl <= 0) sl = 1; 
                            } catch (Exception ex) {
                                sl = 1; 
                            }

                            String giaStr = productModel.getValueAt(row, 3).toString().replaceAll("[^0-9]", "");
                            long donGia = 0;
                            try { donGia = Long.parseLong(giaStr); } catch (Exception ex) {}

                            long thanhTien = sl * donGia;
                            productModel.setValueAt(String.valueOf(sl), row, 2); 
                            productModel.setValueAt(String.format("%,d", thanhTien).replace(',', '.') + "đ", row, 5);
                            
                            recalculateTotals();
                            
                        } finally {
                            isTableUpdating = false; 
                        }
                    });
                }
            }
        });

        JTable tbl = new JTable(productModel) {
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                int tableHeight = getRowCount() * getRowHeight();
                return new Dimension(getPreferredSize().width, tableHeight);
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
        for(int i=1; i<=4; i++) tbl.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        
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
                    if (col == 6 && SwingUtilities.isLeftMouseButton(e)) { 
                        productModel.removeRow(row);
                        recalculateTotals();
                        luuNhapHoaDon(true);
                        sp.revalidate(); sp.repaint();
                        return; // FIX: Ngăn crash OutOfBounds sau khi xóa dòng
                    } 
                    
                    if (tbl.isEditing()) tbl.getCellEditor().stopCellEditing(); 

                    if (col != 2 && col != 3 && col != 6) {
                        int slHienTai = Integer.parseInt(productModel.getValueAt(row, 2).toString());
                        
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            productModel.setValueAt(String.valueOf(slHienTai + 1), row, 2);
                        } 
                        else if (SwingUtilities.isRightMouseButton(e)) {
                            if (slHienTai > 1) {
                                productModel.setValueAt(String.valueOf(slHienTai - 1), row, 2);
                            } else {
                                productModel.removeRow(row);
                                recalculateTotals();
                                return; // FIX: Ngăn crash OutOfBounds
                            }
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
                            return; // FIX: Ngăn crash OutOfBounds
                        }
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
                            for (Object[] row : ketQua) {
                                String id = row[0].toString();
                                String ten = row[1].toString();
                                String donVi = row[2] != null ? row[2].toString() : "";
                                long giaBan = Math.round(Double.parseDouble(row[3].toString()));
                                String gia = String.valueOf(giaBan); 
                                String tonKho = row[4] != null ? row[4].toString() : "0";
                                
                                String danhMuc = (row.length > 5 && row[5] != null) ? row[5].toString() : "Khác";

                                String iconType = "PACKAGE"; 
                                String dmCheck = danhMuc.toLowerCase();
                                if (dmCheck.contains("thuốc kê đơn")) iconType = "PILL";         
                                else if (dmCheck.contains("không kê đơn")) iconType = "BOX";          
                                else if (dmCheck.contains("mỹ phẩm")) iconType = "COSMETIC";     
                                else if (dmCheck.contains("chức năng") || dmCheck.contains("tpcn")) iconType = "LEAF";         
                                else if (dmCheck.contains("vật tư") || dmCheck.contains("y tế")) iconType = "MEDICAL_TOOL"; 

                                pnlList.add(createSuggestionItem(suggestionPopup, txtSearchProduct, iconType, ten, donVi, gia, tonKho, danhMuc));
                            }
                            hasResult = true;
                        }

                        if (hasResult) {
                            JScrollPane scrollPane = new JScrollPane(pnlList);
                            scrollPane.setBorder(null);
                            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                            scrollPane.getVerticalScrollBar().setUnitIncrement(16); 
                            
                            int listHeight = pnlList.getPreferredSize().height;
                            scrollPane.setPreferredSize(new Dimension(pnlSearchWrapper.getWidth(), Math.min(listHeight, 300)));
                            
                            suggestionPopup.add(scrollPane);
                            suggestionPopup.show(pnlSearchWrapper, 0, pnlSearchWrapper.getHeight());
                        } else {
                            JMenuItem emptyItem = new JMenuItem("Không tìm thấy sản phẩm nào phù hợp...");
                            emptyItem.setEnabled(false);
                            suggestionPopup.add(emptyItem);
                            suggestionPopup.setPreferredSize(new Dimension(pnlSearchWrapper.getWidth(), suggestionPopup.getPreferredSize().height));
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
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP || e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
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

        JPanel pnlNote = new JPanel(new BorderLayout(0, 5));
        pnlNote.setBackground(Color.WHITE);
        JLabel lblNote = new JLabel("Ghi chú");
        lblNote.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
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

        JPanel pnlCenter = new JPanel();
        pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));
        pnlCenter.setBackground(Color.WHITE);
        
        pnlMethod.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlTienMatWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlFinal.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        pnlCenter.add(pnlMethod);
        pnlCenter.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlCenter.add(pnlTienMatWrapper); 
        pnlCenter.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlCenter.add(pnlNote);
        pnlCenter.add(Box.createRigidArea(new Dimension(0, 20)));
        pnlCenter.add(pnlFinal);

        pnlWrapper.add(pnlCenter, BorderLayout.CENTER);
        
        btnTienMat.addActionListener(e -> {
            phuongThuc = "Tiền mặt";
            setPaymentBtnActive(btnTienMat);
            setPaymentBtnInactive(btnChuyenKhoan);
            pnlTienMatWrapper.setVisible(true); 
            pnlWrapper.revalidate(); pnlWrapper.repaint();
        });

        btnChuyenKhoan.addActionListener(e -> {
            phuongThuc = "Chuyển khoản";
            setPaymentBtnActive(btnChuyenKhoan);
            setPaymentBtnInactive(btnTienMat);
            pnlTienMatWrapper.setVisible(false); 
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
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(Color.decode("#FFFBEB"));
        pnl.setBorder(BorderFactory.createCompoundBorder(
        	    BorderFactory.createLineBorder(Color.decode("#E5E7EB"), 1, true),
        	    new EmptyBorder(2, 6, 2, 6) // <--- CHỈNH THÔNG SỐ Ở ĐÂY
        	));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("Tiền khách đưa");
        lblTitle.setIconTextGap(8);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.decode("#B45309"));

        JButton btnChon = new JButton("Chọn mệnh giá");
        btnChon.setBackground(Color.decode("#F59E0B"));
        btnChon.setForeground(Color.WHITE);
        btnChon.setFocusPainted(false);
        btnChon.setBorderPainted(false);
        btnChon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnChon.setPreferredSize(new Dimension(130, 30));
        
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(btnChon, BorderLayout.EAST);

        JPanel pnlGrid = new JPanel(new GridLayout(3, 3, 10, 10));
        pnlGrid.setOpaque(false);
        
        pnlGrid.add(createMoneyCell("500.000đ", 500000, "#3B82F6")); 
        pnlGrid.add(createMoneyCell("200.000đ", 200000, "#D946EF")); 
        pnlGrid.add(createMoneyCell("100.000đ", 100000, "#10B981")); 
        pnlGrid.add(createMoneyCell("50.000đ", 50000, "#EAB308"));  
        pnlGrid.add(createMoneyCell("20.000đ", 20000, "#EF4444"));  
        pnlGrid.add(createMoneyCell("10.000đ", 10000, "#F43F5E"));  
        pnlGrid.add(createMoneyCell("5.000đ", 5000, "#EC4899"));   
        pnlGrid.add(createMoneyCell("2.000đ", 2000, "#6B7280"));   
        pnlGrid.add(createMoneyCell("1.000đ", 1000, "#6B7280"));   

        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setOpaque(false);
        pnlFooter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#FDE047")),
            new EmptyBorder(10, 0, 0, 0)
        ));
        
        JLabel lblTotalText = new JLabel("Tổng tiền mặt");
        lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalText.setForeground(Color.decode("#D97706"));
        
        lblTotalValue = new JLabel("0đ");
        lblTotalValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalValue.setForeground(Color.decode("#D97706"));
        
        JPanel pnlInfoRight = new JPanel(new GridLayout(2, 1));
        pnlInfoRight.setOpaque(false);
        pnlInfoRight.add(lblTotalValue);
        
        lblTienThuaValue = new JLabel("Tiền thừa: 0đ", SwingConstants.RIGHT);
        lblTienThuaValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlInfoRight.add(lblTienThuaValue);

        pnlFooter.add(lblTotalText, BorderLayout.WEST);
        pnlFooter.add(pnlInfoRight, BorderLayout.EAST);

        pnl.add(pnlHeader, BorderLayout.NORTH);
        pnl.add(pnlGrid, BorderLayout.CENTER);
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

    private JPanel createCustomerSuggestionItem(String id, String name, String phone, String points) {
        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(8, 12, 8, 12));
        pnl.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblInfo = new JLabel("<html><b>" + name + "</b> - " + phone + "</html>");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JLabel lblPts = new JLabel(points + "đ");
        lblPts.setForeground(Color.decode("#9333EA")); 
        lblPts.setFont(new Font("Segoe UI", Font.BOLD, 12));

        pnl.add(lblInfo, BorderLayout.CENTER);
        pnl.add(lblPts, BorderLayout.EAST);

        pnl.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                txtSearch.setText(phone);
                customerSuggestionPopup.setVisible(false);
                for (java.awt.event.ActionListener al : ((JButton)((JPanel)txtSearch.getParent()).getComponent(1)).getActionListeners()) {
                    al.actionPerformed(new java.awt.event.ActionEvent(this, java.awt.event.ActionEvent.ACTION_PERFORMED, ""));
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) { pnl.setBackground(Color.decode("#F3F4F6")); }
            @Override
            public void mouseExited(MouseEvent e) { pnl.setBackground(Color.WHITE); }
        });

        return pnl;
    }

    private void luuNhapHoaDon(boolean isAutoSave) {
        try {
            java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();

            if (productModel.getRowCount() == 0) {
                if (!isAutoSave) JOptionPane.showMessageDialog(this, "Hóa đơn chưa có sản phẩm nào để lưu nháp!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // FIX: Bao bọc bằng try-with-resources
            try (java.sql.Connection con = ConnectDB.getInstance().getConnection()) {
                
                String rawName = txtName.getText().trim();
                String rawPhone = txtPhone.getText().trim();

                if (rawName.toLowerCase().contains("tên khách") || rawName.toLowerCase().contains("bỏ trống")) {
                    rawName = "";
                }
                if (rawPhone.toLowerCase().contains("số điện") || rawPhone.toLowerCase().contains("tùy chọn") || rawPhone.toLowerCase().contains("tuỳ chọn")) {
                    rawPhone = "";
                }

                // --- BẮT ĐẦU ĐOẠN FIX BỔ SUNG ---
                // Nếu người dùng nhập SĐT ở ô tìm kiếm (txtSearch) nhưng chưa liên kết, 
                // ta vẫn phải "vét" cái SĐT đó để lưu vào hoá đơn nháp
                String txtSearchValue = txtSearch.getText().trim();
                if (rawPhone.isEmpty() && !txtSearchValue.isEmpty() && !txtSearchValue.contains("Nhập SĐT")) {
                    // Nếu gõ toàn số (khoảng 9-11 số), gán luôn nó làm số điện thoại nháp
                    if (txtSearchValue.matches("^[0-9]{9,11}$")) {
                        rawPhone = txtSearchValue;
                    }
                }

                String khach = rawName.isEmpty() ? "Khách lẻ" : rawName;
                String sdt = rawPhone;

                DAO.DAO_HoaDon daoHD = new DAO.DAO_HoaDon();
                DAO.DAO_ChiTietHoaDon daoCT = new DAO.DAO_ChiTietHoaDon();
                
                Entity.HoaDon hd = new Entity.HoaDon();
                String maHD = (this.maHDDangSua != null && !this.maHDDangSua.isEmpty()) ? this.maHDDangSua : "HD-" + System.currentTimeMillis(); 
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
                    String ngayStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                    if (editingModelRow != -1) {
                        mainTableModel.setValueAt(maHD, editingModelRow, 0); 
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
                        JOptionPane.showMessageDialog(this, "Đã lưu nháp hóa đơn thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        this.dispose(); 
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private JPanel createSuggestionItem(JPopupMenu popup, JTextField txtSearch, String iconType, String name, String unit, String price, String stock, String danhMuc) {
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
            public void mouseEntered(java.awt.event.MouseEvent e) {
                pnl.setBackground(Color.decode("#F3F4F6")); 
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                pnl.setBackground(Color.WHITE);
            }
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
                        // FIX: Truyền thêm giá trị % VAT mặc định, bạn có thể thay đổi sau nếu có DB
                        productModel.addRow(new Object[]{
                            name, unit, "1", price, "5%", String.format("%,d", Integer.parseInt(price)).replace(',', '.') + "đ", 
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
        
        String sql = "SELECT MAX(id) FROM HoaDon WHERE id LIKE 'HD-" + year + "-%'";
        // FIX: Đóng Connection an toàn
        try (java.sql.Connection con = ConnectDB.getInstance().getConnection();
             java.sql.Statement st = con.createStatement();
             java.sql.ResultSet rs = st.executeQuery(sql)) {
             
            if (rs.next()) {
                String maxId = rs.getString(1);
                if (maxId != null) {
                    String[] parts = maxId.split("-");
                    if (parts.length == 3) {
                        int stt = Integer.parseInt(parts[2]) + 1;
                        maMoi = String.format("HD-%d-%04d", year, stt);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maMoi;
    }
    
    private void updateVoucherTagsUI() {
        if (pnlVoucherTags == null) return;
        
        String maApDung = (maKhuyenMaiApDung == null) ? "" : maKhuyenMaiApDung.trim();
        
        for (Component comp : pnlVoucherTags.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel pnl = (JPanel) comp;
                String maKM = pnl.getName(); 
                
                if (maKM != null && pnl.getComponentCount() > 0) {
                    maKM = maKM.trim(); 
                    JLabel lblText = (JLabel) pnl.getComponent(0);
                    
                    if (!maApDung.isEmpty() && maKM.equalsIgnoreCase(maApDung)) {
                        pnl.setBackground(Color.decode("#D1FAE5")); 
                        pnl.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.decode("#10B981"), 1, true),
                            new EmptyBorder(4, 8, 4, 8)
                        ));
                        lblText.setForeground(Color.decode("#047857"));
                        lblText.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else {
                        pnl.setBackground(Color.WHITE);
                        pnl.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.decode("#E5E7EB"), 1, true),
                            new EmptyBorder(4, 8, 4, 8)
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
    
    public void loadDuLieuHoaDonNhap(String maHoaDon) {
        this.maHDDangSua = maHoaDon;
        
        // --- FIX TRIỆT ĐỂ: Trì hoãn việc load dữ liệu cho đến khi Giao diện vẽ xong toàn bộ 11 cột ---
        SwingUtilities.invokeLater(() -> {
            productModel.setRowCount(0); 
            
            try {
                DAO_ChiTietHoaDon daoCT = new DAO_ChiTietHoaDon();
                java.util.List<Object[]> dsMonHang = daoCT.layDuLieuChoTaoHoaDon(maHoaDon);
                
                // Lúc này giao diện đã load xong, getColumnCount() chắc chắn sẽ lấy đúng 11 cột
                int colCount = Math.max(productModel.getColumnCount(), 11); 
                
                for (Object[] row : dsMonHang) {
                    Object[] fullRow = new Object[colCount];
                    for (int j = 0; j < colCount; j++) fullRow[j] = ""; 
                    
                    // Chống Null cho các cột tính toán ẩn
                    if (colCount > 4) fullRow[4] = "0";     
                    if (colCount > 9) fullRow[9] = "999";   
                    if (colCount > 10) fullRow[10] = "1";    
                    
                    for (int j = 0; j < Math.min(row.length, colCount); j++) {
                        if (row[j] != null) fullRow[j] = row[j];
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
                JOptionPane.showMessageDialog(this, "Lỗi khi tải hóa đơn nháp: " + e.getMessage());
            } 
        }); // --- Kết thúc khối trì hoãn ---
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