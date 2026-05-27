package GUI;import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import java.awt.print.PrinterJob;
import java.awt.print.Printable;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import javax.swing.*;
import Entity.*;
import javax.swing.border.*;
import javax.swing.table.*;
import Utils.MenuIcon;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import BUS.*;
import javax.swing.Timer;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
public class TaoPhieuDoiTra extends JDialog {
	private boolean isSelectingInvoice = false;
	private boolean isSelectingProduct = false;
    private DefaultTableModel spMoiModel, mainModel, chiTietModel; 
    private JTable tableSPMoi, table;
    private boolean isUpdatingTable = false, isUpdatingCart = false;
    private JScrollPane spTableTraHang, scrollSPMoi; 
    private JPanel pnlTableTraHang;
    public static boolean isTaoThanhCong = false;
    public static String maPhieuMoi = "";
    private JPanel pnlHeader, pnlNewProduct, pnlFoundData;
    private JLabel lblError;
    private JTextField txtSearch, txtSearchNew, txtGhiChu; 
    private JButton btnTraHang, btnDoiHang, btnTaoPhieu, btnLuuNhap;
    private JComboBox<String> cboLyDo; 
    private JPopupMenu suggestionInvoiceMenu;
    private String phuongThucDoiTra = "Tiền mặt";
    private JPanel pnlPaymentMethods, pnlCashDetails, pnlTransferDetails;
    private JTextField txtTienKhachDua;
    private JPanel pnlPaymentSection;
    private JLabel lblTienThuaTraKhach, lblQRCode, lblQRAmount;
    private long soTienThucTeCanXuLy = 0; 
    private boolean isCKXacNhan = false; 
    private JButton btnXacNhanCK; 
    private Timer autoCancelTimer;
    private java.util.Map<String, java.time.LocalDateTime> mapThoiGianTao = new java.util.HashMap<>();
    private JLabel lblKhachHang, lblMaHoaDonInfo, lblThoiGianThanhToan;
    private JLabel lblGiaTriSPTra, lblThoiGianMua, lblBadgeHoanTien, lblSoTienHoan, lblTextSoTienHoan;
    private long tongTienMat = 0;
    private java.util.List<int[]> listCounters = new java.util.ArrayList<>();
    private java.util.List<JLabel> listCountLabels = new java.util.ArrayList<>();
    private JLabel lblTotalValue, lblTienThuaValue, lblExactValue;
    private JLabel lblTimer;
    private Timer dialogTimer;
    private int remainingSeconds = 600; // Đếm ngược 10 phút cho Form
    private BUS_HoaDon busHD = new BUS_HoaDon();
    private BUS_TraHang busTraHang = new BUS_TraHang();
    private BUS_DoiHang busDoiHang = new BUS_DoiHang();
    private JPanel pnlTienMatWrapper;
    private LocalDateTime ngayHoaDonGoc; 
    private JPopupMenu suggestionMenu;
    private double tongTienGoc = 0, giaMoi = 0; 
    private String tenMoi = "";
    private final Color primaryRed = Color.decode("#E11D48"); 
    private final Color primaryBlue = Color.decode("#2563EB");
    private final Color borderGray = Color.decode("#E2E8F0");
    private final Color textDark = Color.decode("#212B36");
    private Timer scannerTimer;
    private String maHDDangXuLy = "";
    private StringBuilder scanBuffer = new StringBuilder();
    private long lastKeyTime = 0;
    public boolean isEditMode = false;
    public String maPhieuDangSua = "";
    private javax.swing.Timer boKiemTraTienToi;
    private String maGiaoDichHienTai = "";
    private KeyEventDispatcher scannerDispatcher;// Lưu mã để hủy nếu hết giờ
    public TaoPhieuDoiTra(Frame parent, DefaultTableModel model) {
        super(parent, "Tạo phiếu đổi / trả hàng", true);
        this.mainModel = model;
        setUndecorated(true);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.decode("#F8FAFC"));

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
            }
        });

        initUI();
        setSize(850, 330); 
        setLocationRelativeTo(parent);
        
        // GỌI HÀM CÀI ĐẶT MÁY QUÉT SAU KHI KHỞI TẠO XONG UI
        setupGlobalBarcodeScanner(); 
    }
 // Constructor dành riêng cho chế độ Sửa Phiếu Nháp
    public TaoPhieuDoiTra(Frame parent, DefaultTableModel model, String maPhieuEdit, String maHDGoc, String loaiPhieu) {
        this(parent, model); 
        this.isEditMode = true;
        this.maPhieuDangSua = maPhieuEdit;
        
        // FIX LỖI 3: Mở đúng tab Đổi hàng hay Trả hàng
        if (loaiPhieu != null && loaiPhieu.equalsIgnoreCase("Đổi hàng")) {
            setToggleState(false);
        } else {
            setToggleState(true);
        }

        isSelectingInvoice = true; // Khóa mỏ popup lại
        txtSearch.setText(maHDGoc);
        txtSearch.setForeground(Color.BLACK);
        txtSearch.setEnabled(false); 
        isSelectingInvoice = false; // Khóa luôn ô tìm kiếm để tránh người dùng đổi mã HĐ khác gây lỗi
        
        SwingUtilities.invokeLater(() -> {
            xuLyTimKiemHD();
            taiDuLieuBanNhap(); // GỌI HÀM FIX LỖI 4
        });
    }
    private void initUI() {
    	pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(primaryRed);
        pnlHeader.setBorder(new EmptyBorder(12, 20, 12, 20)); 
        
        JLabel lblTitle = new JLabel("TẠO PHIẾU TRẢ HÀNG");
        lblTitle.setIcon(new MenuIcon("SYNC", 20)); 
        lblTitle.setIconTextGap(10);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        // --- ĐÃ FIX: ĐƯA ĐỒNG HỒ VÀ NÚT TẮT VÀO GÓC PHẢI ---
        JPanel pnlRightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlRightHeader.setOpaque(false);
        
        
        JButton btnClose = new JButton("X");
        btnClose.setForeground(Color.WHITE);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false); 
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

       
        pnlRightHeader.add(btnClose);

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(pnlRightHeader, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlBody = new JPanel(new BorderLayout()); 
        pnlBody.setBackground(Color.decode("#F8FAFC"));
        pnlBody.setBorder(new EmptyBorder(15, 15, 15, 15)); 

        JPanel pnlContainer = new JPanel(new BorderLayout()); 
        pnlContainer.setBackground(Color.WHITE);
        pnlContainer.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borderGray, 1, true),
            new EmptyBorder(15, 20, 15, 20) 
        ));

        initModels();

        JPanel pnlTopArea = new JPanel();
        pnlTopArea.setLayout(new BoxLayout(pnlTopArea, BoxLayout.Y_AXIS));
        pnlTopArea.setBackground(Color.WHITE);
        pnlTopArea.add(createTogglePanel());
        pnlTopArea.add(Box.createRigidArea(new Dimension(0, 15))); 
        pnlTopArea.add(createSearchPanel());
        pnlTopArea.add(Box.createRigidArea(new Dimension(0, 15)));

        pnlContainer.add(pnlTopArea, BorderLayout.NORTH);

        pnlFoundData = createFoundDataPanel();
        pnlFoundData.setVisible(false);
        pnlContainer.add(pnlFoundData, BorderLayout.CENTER);

        pnlBody.add(pnlContainer, BorderLayout.CENTER);
        add(pnlBody, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, borderGray),
            new EmptyBorder(10, 20, 10, 20) 
        ));

        JButton btnHuy = new JButton("Hủy bỏ");
        btnHuy.setPreferredSize(new Dimension(120, 40));
        btnHuy.setBackground(Color.WHITE);
        btnHuy.setForeground(Color.decode("#475569"));
        btnHuy.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnHuy.setBorder(BorderFactory.createLineBorder(borderGray, 1, true));
        btnHuy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHuy.addActionListener(e -> dispose());

        // --- THÊM NÚT LƯU NHÁP ---
        btnLuuNhap = new JButton("Lưu nháp");
        btnLuuNhap.setVisible(false);
        btnLuuNhap.setEnabled(false);
        btnLuuNhap.setPreferredSize(new Dimension(130, 40));
        btnLuuNhap.setBackground(Color.decode("#F59E0B")); // Màu cam
        btnLuuNhap.setForeground(Color.WHITE);
        btnLuuNhap.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLuuNhap.setBorderPainted(false);
        btnLuuNhap.setFocusPainted(false);
        btnLuuNhap.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLuuNhap.addActionListener(e -> xuLyTaoPhieu(true)); // true = Lưu nháp

        btnTaoPhieu = new JButton("Xác nhận trả hàng");
        btnTaoPhieu.setIcon(new MenuIcon("CHECK_CIRCLE", 20));
        btnTaoPhieu.setPreferredSize(new Dimension(200, 40));
        btnTaoPhieu.setBackground(primaryRed);
        btnTaoPhieu.setForeground(Color.WHITE);
        btnTaoPhieu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTaoPhieu.setBorderPainted(false);
        btnTaoPhieu.setFocusPainted(false);
        btnTaoPhieu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTaoPhieu.setVisible(false); // FIX: Ẩn luôn nút khi chưa tìm kiếm
        btnTaoPhieu.addActionListener(e -> xuLyTaoPhieu(false)); // false = Hoàn thành// false = Hoàn thành

        // --- GOM 2 NÚT VÀO PANEL BÊN PHẢI ---
        JPanel pnlActionRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActionRight.setBackground(Color.WHITE);
        pnlActionRight.add(btnLuuNhap);
        pnlActionRight.add(btnTaoPhieu);

        pnlFooter.add(btnHuy, BorderLayout.WEST);
        pnlFooter.add(pnlActionRight, BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);
        
        setToggleState(true); 
    }

    private void initModels() {
        btnTraHang = new JButton("Trả hàng");
        btnDoiHang = new JButton("Đổi hàng");
        cboLyDo = new JComboBox<>(new String[]{
        	    "Lỗi do NSX / Giao sai thuốc (Hỗ trợ 100%)", 
        	    "Bác sĩ đổi phác đồ / Dị ứng (Hỗ trợ 100%)", 
        	    "Sản phẩm cận date / Hết hạn (Hỗ trợ 100%)", 
        	    "Lỗi bảo quản từ phía khách hàng (Từ chối)",
        	    "Khách hàng đổi ý (Hỗ trợ theo thời gian)" // <-- Dòng mới thêm
        	});
        
        // THÊM CỘT ĐVT VÀO BẢNG SẢN PHẨM TRẢ
        String[] cols = {"Chọn", "Sản phẩm", "ĐVT", "Số lượng", "Đơn giá", "MaxSL"};
        chiTietModel = new DefaultTableModel(cols, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class; 
                if (columnIndex == 3 || columnIndex == 5) return Integer.class; 
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) { 
                return column == 0 || column == 3; 
            }
        };

        chiTietModel.addTableModelListener(e -> {
            if (isUpdatingTable) return;
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                
                if (row >= 0 && col == 3) { 
                    try {
                        int slMax = (int) chiTietModel.getValueAt(row, 5);
                        int slThaoTac = (int) chiTietModel.getValueAt(row, 3);
                        if (slThaoTac < 1) slThaoTac = 1;
                        if (slThaoTac > slMax) slThaoTac = slMax; 
                        
                        isUpdatingTable = true;
                        chiTietModel.setValueAt(slThaoTac, row, 3);
                        chiTietModel.setValueAt(true, row, 0); 
                    } catch (Exception ex) { 
                        ex.printStackTrace();
                    } finally {
                        isUpdatingTable = false;
                        SwingUtilities.invokeLater(() -> capNhatDieuKienDoiTra());
                    }
                }
                else if (row >= 0 && col == 0) {
                    SwingUtilities.invokeLater(() -> capNhatDieuKienDoiTra());
                }
            }
        });

     // Cột: 0=Tên, 1=ĐVT, 2=Lô/HSD, 3=SL, 4=Đơn giá, 5=VAT%, 6=Thành tiền, 7=Xóa
        spMoiModel = new DefaultTableModel(new String[]{"Sản phẩm mới", "ĐVT", "Lô / HSD", "SL", "Đơn giá", "VAT", "Thành tiền", ""}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 1 || column == 3; } // Sửa SL thành cột 3
        };
        
        spMoiModel.addTableModelListener(e -> {
            if (isUpdatingCart) return;
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();

                if (col == 1) { // Đổi ĐVT
                    SwingUtilities.invokeLater(() -> {
                        isUpdatingCart = true;
                        try {
                            String tenSP = spMoiModel.getValueAt(row, 0).toString();
                            String donViMoi = spMoiModel.getValueAt(row, 1).toString();
                            double giaChuaVAT = 0;
                            double thueVAT = 0;

                            BUS_DonViDoLuong busDVDL_inner = new BUS_DonViDoLuong();
                            List<DonViDoLuong> dsDV = busDVDL_inner.getDSTheoTenSP(tenSP);
                            for (DonViDoLuong dv : dsDV) {
                                if (dv.getTen().equalsIgnoreCase(donViMoi)) {
                                    giaChuaVAT = dv.getGia();
                                    break;
                                }
                            }
                            BUS_SanPham busSP_inner = new BUS_SanPham();
                            thueVAT = busSP_inner.layThueVATTheoTenSP(tenSP);
                            if (thueVAT > 0 && thueVAT < 1) thueVAT = thueVAT * 100;

                            if (giaChuaVAT > 0) {
                                int sl = Integer.parseInt(spMoiModel.getValueAt(row, 3).toString()); // Đọc SL cột 3
                                double thanhTien = sl * giaChuaVAT * (1.0 + thueVAT / 100.0);
                                spMoiModel.setValueAt(String.format("%,.0fđ", giaChuaVAT), row, 4); // Đơn giá cột 4
                                spMoiModel.setValueAt(String.format("%.0f%%", thueVAT), row, 5);    // VAT cột 5
                                spMoiModel.setValueAt(String.format("%,.0fđ", thanhTien), row, 6);   // Thành tiền cột 6
                            }
                        } catch (Exception ex) {}
                        finally {
                            isUpdatingCart = false;
                            tinhTongTienSPMoi();
                        }
                    });
                } 
                else if (col == 3) { // Sửa Số lượng
                    try {
                        int sl = Integer.parseInt(spMoiModel.getValueAt(row, 3).toString());
                        if (sl < 1) sl = 1;
                        double donGia = Double.parseDouble(spMoiModel.getValueAt(row, 4).toString().replaceAll("[^0-9]", ""));
                        double vatPct = 0;
                        try {
                            String vatStr = spMoiModel.getValueAt(row, 5).toString().replace("%", "").trim();
                            vatPct = Double.parseDouble(vatStr);
                        } catch (Exception ignored) {}
                        double thanhTien = sl * donGia * (1.0 + vatPct / 100.0);
                        
                        isUpdatingCart = true;
                        spMoiModel.setValueAt(sl, row, 3);
                        spMoiModel.setValueAt(String.format("%,.0fđ", thanhTien), row, 6); // Thành tiền cột 6
                    } catch(Exception ex) { 
                    } finally {
                        isUpdatingCart = false;
                        SwingUtilities.invokeLater(() -> tinhTongTienSPMoi());
                    }
                }
            }
        });
    }

    private JPanel createTogglePanel() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnl.setBackground(Color.WHITE);
        
        JPanel pnlWrap = new JPanel(new GridLayout(1, 2));
        pnlWrap.setBackground(Color.decode("#F1F5F9"));
        pnlWrap.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.decode("#F1F5F9"), 1, true),
            new EmptyBorder(4, 4, 4, 4)
        ));

        Dimension btnSize = new Dimension(160, 38); 
        btnDoiHang.setPreferredSize(btnSize);
        btnTraHang.setPreferredSize(btnSize);
        
        btnDoiHang.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTraHang.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        btnDoiHang.setFocusPainted(false);
        btnTraHang.setFocusPainted(false);
        btnDoiHang.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTraHang.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnTraHang.addActionListener(e -> setToggleState(true));
        btnDoiHang.addActionListener(e -> setToggleState(false));

        pnlWrap.add(btnDoiHang);
        pnlWrap.add(btnTraHang);
        pnl.add(pnlWrap);
        return pnl;
    }

    private JPanel createSearchPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 5)); 
        pnl.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Tra cứu hóa đơn bán hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.decode("#0F172A"));
        pnl.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlInputWrap = new JPanel(new BorderLayout(10, 0));
        pnlInputWrap.setBackground(Color.WHITE);

        JPanel pnlTxt = new JPanel(new BorderLayout(10, 0));
        pnlTxt.setBackground(Color.WHITE);
        pnlTxt.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.decode("#CBD5E1"), 1, true),
            new EmptyBorder(0, 12, 0, 12)
        ));
        
        JLabel lblIconSearch = new JLabel(new MenuIcon("SEARCH", 18));
        lblIconSearch.setForeground(Color.decode("#94A3B8"));
        
        txtSearch = new JTextField("Nhập mã hóa đơn (VD: HD-2024-0001)...");
        txtSearch.setPreferredSize(new Dimension(0, 40)); 
        txtSearch.setBorder(null);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.setForeground(Color.GRAY);
        txtSearch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (txtSearch.getText().equals("Nhập mã hóa đơn (VD: HD-2024-0001)...")) {
                    txtSearch.setText(""); 
                    txtSearch.setForeground(Color.BLACK);
                }
            }
        });
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().equals("Nhập mã hóa đơn (VD: HD-2024-0001)...")) {
                    txtSearch.setText(""); txtSearch.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setForeground(Color.GRAY); txtSearch.setText("Nhập mã hóa đơn (VD: HD-2024-0001)...");
                }
            }
        });

        pnlTxt.add(lblIconSearch, BorderLayout.WEST);
        pnlTxt.add(txtSearch, BorderLayout.CENTER);

        JButton btnTim = new JButton("Tìm kiếm");
        btnTim.setPreferredSize(new Dimension(150, 50)); 
        btnTim.setBackground(Color.decode("#1E293B"));
        btnTim.setForeground(Color.WHITE);
        btnTim.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTim.setFocusPainted(false);
        btnTim.setBorderPainted(false);
        btnTim.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnlInputWrap.add(pnlTxt, BorderLayout.CENTER);
        pnlInputWrap.add(btnTim, BorderLayout.EAST);
        pnl.add(pnlInputWrap, BorderLayout.CENTER);
        JPanel pnlLockHeight = new JPanel(new BorderLayout());
        pnlLockHeight.setBackground(Color.WHITE);
        pnlLockHeight.add(pnlInputWrap, BorderLayout.NORTH); 
        pnl.add(pnlLockHeight, BorderLayout.CENTER);
        lblError = new JLabel(" Không tìm thấy hóa đơn");
        lblError.setIcon(new MenuIcon("WARNING", 14)); 
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblError.setForeground(primaryRed);
        lblError.setBorder(new EmptyBorder(3, 3, 0, 0));
        lblError.setVisible(false);
        pnl.add(lblError, BorderLayout.SOUTH);

        btnTim.addActionListener(e -> xuLyTimKiemHD());
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) xuLyTimKiemHD();
            }
        });
        suggestionInvoiceMenu = new JPopupMenu();
        suggestionInvoiceMenu.setFocusable(false);
        suggestionInvoiceMenu.setBackground(Color.WHITE);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { timKiemHoaDonLive(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { timKiemHoaDonLive(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { timKiemHoaDonLive(); }
        });
        return pnl;
    }
 // --- KHAI BÁO THÊM BIẾN NÀY NGAY TRÊN HÀM ---
    private SwingWorker<java.util.List<String>, Void> currentInvoiceSearchWorker;

    private void timKiemHoaDonLive() {
        if (isSelectingInvoice) return; 

        String kw = txtSearch.getText().trim();
        if (kw.isEmpty() || kw.equals("Nhập mã hóa đơn (VD: HD-2024-0001)...")) {
            SwingUtilities.invokeLater(() -> suggestionInvoiceMenu.setVisible(false));
            return;
        }

        // --- ĐÃ FIX: Hủy tiến trình gợi ý cũ nếu người dùng gõ phím mới ---
        if (currentInvoiceSearchWorker != null && !currentInvoiceSearchWorker.isDone()) {
            currentInvoiceSearchWorker.cancel(true);
        }

        currentInvoiceSearchWorker = new SwingWorker<java.util.List<String>, Void>() {
            @Override
            protected java.util.List<String> doInBackground() throws Exception {
                return busHD.timKiemMaHoaDonGoiY(kw);
            }

            @Override
            protected void done() {
                if (isCancelled()) return; // Bị ép dừng thì ngắt luôn, không show popup nữa
                try {
                    java.util.List<String> dsGoiY = get();
                    SwingUtilities.invokeLater(() -> {
                        if (!txtSearch.getText().trim().equals(kw)) return; 

                        if (dsGoiY != null && !dsGoiY.isEmpty()) {
                            for (String id : dsGoiY) {
                                if (id.equalsIgnoreCase(kw)) {
                                    suggestionInvoiceMenu.setVisible(false);
                                    return; 
                                }
                            }
                            
                            suggestionInvoiceMenu.removeAll();
                            JPanel pnlList = new JPanel();
                            pnlList.setLayout(new BoxLayout(pnlList, BoxLayout.Y_AXIS));
                            pnlList.setBackground(Color.WHITE);

                            for (String idHD : dsGoiY) {
                                JPanel pnlItem = new JPanel(new BorderLayout());
                                pnlItem.setBackground(Color.WHITE);
                                pnlItem.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E5E7EB")), 
                                    new javax.swing.border.EmptyBorder(10, 15, 10, 15)
                                ));
                                pnlItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
                                
                                JLabel lblId = new JLabel("Hóa đơn: " + idHD);
                                lblId.setFont(new Font("Segoe UI", Font.BOLD, 14));
                                lblId.setForeground(Color.decode("#1967D2"));
                                pnlItem.add(lblId, BorderLayout.CENTER);

                                final String finalIdHD = idHD;

                                pnlItem.addMouseListener(new java.awt.event.MouseAdapter() {
                                    public void mouseEntered(java.awt.event.MouseEvent evt) { pnlItem.setBackground(Color.decode("#F8FAFC")); }
                                    public void mouseExited(java.awt.event.MouseEvent evt) { pnlItem.setBackground(Color.WHITE); }
                                    
                                    public void mousePressed(java.awt.event.MouseEvent evt) {
                                        suggestionInvoiceMenu.setVisible(false);
                                        TaoPhieuDoiTra.this.requestFocusInWindow();
                                        
                                        javax.swing.Timer timerUnikey = new javax.swing.Timer(50, e -> {
                                            isSelectingInvoice = true; 
                                            txtSearch.setText(finalIdHD); 
                                            txtSearch.setForeground(Color.BLACK);
                                            isSelectingInvoice = false;
                                            
                                            SwingUtilities.invokeLater(() -> xuLyTimKiemHD()); 
                                            txtSearch.requestFocusInWindow();
                                        });
                                        timerUnikey.setRepeats(false);
                                        timerUnikey.start();
                                    }
                                });
                                pnlList.add(pnlItem);
                            }
                            suggestionInvoiceMenu.add(pnlList);
                            suggestionInvoiceMenu.pack();
                            if (!suggestionInvoiceMenu.isVisible() && txtSearch.isShowing()) {
                                suggestionInvoiceMenu.show(txtSearch, 0, txtSearch.getHeight());
                            }
                        } else {
                            suggestionInvoiceMenu.setVisible(false);
                        }
                    });
                } catch (Exception ex) {}
            }
        };
        currentInvoiceSearchWorker.execute();
    }
    private JPanel createFoundDataPanel() {
        JPanel pnl = new JPanel(new BorderLayout(20, 0)); 
        pnl.setBackground(Color.WHITE);

        JPanel pnlLeft = new JPanel();
        pnlLeft.setLayout(new BoxLayout(pnlLeft, BoxLayout.Y_AXIS));
        pnlLeft.setBackground(Color.WHITE);

        JPanel pnlInvoice = new JPanel(new BorderLayout());
        pnlInvoice.setBackground(Color.WHITE);
        pnlInvoice.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borderGray, 1, true), new EmptyBorder(10, 15, 10, 15) 
        ));
        pnlInvoice.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlInvoice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JPanel pnlInvTop = new JPanel(new BorderLayout());
        pnlInvTop.setBackground(Color.WHITE);
        JLabel lblT1 = new JLabel("Thông tin hóa đơn gốc");
        lblT1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel lblBadge = new JLabel("Đã thanh toán");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setForeground(Color.WHITE);
        lblBadge.setBackground(Color.decode("#22C55E"));
        lblBadge.setOpaque(true);
        lblBadge.setBorder(new EmptyBorder(3, 8, 3, 8));
        pnlInvTop.add(lblT1, BorderLayout.WEST);
        pnlInvTop.add(lblBadge, BorderLayout.EAST);

        JPanel pnlInvMid = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlInvMid.setBackground(Color.WHITE);
        pnlInvMid.setBorder(new EmptyBorder(10, 0, 10, 0)); 
        
        lblKhachHang = new JLabel("---"); lblKhachHang.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMaHoaDonInfo = new JLabel("---"); lblMaHoaDonInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JPanel pnlKH = new JPanel(new GridLayout(2,1)); pnlKH.setBackground(Color.WHITE);
        JLabel l1 = new JLabel("Khách hàng"); l1.setForeground(Color.GRAY); l1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlKH.add(l1); pnlKH.add(lblKhachHang);
        JPanel pnlMa = new JPanel(new GridLayout(2,1)); pnlMa.setBackground(Color.WHITE);
        JLabel l2 = new JLabel("Mã hóa đơn"); l2.setForeground(Color.GRAY); l2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlMa.add(l2); pnlMa.add(lblMaHoaDonInfo);
        
        pnlInvMid.add(pnlKH); pnlInvMid.add(pnlMa);

        JPanel pnlInvBot = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlInvBot.setBackground(Color.decode("#F8FAFC"));
        pnlInvBot.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.decode("#E2E8F0"), 1, true), new EmptyBorder(8, 10, 8, 10)
        ));
        JLabel iconCal = new JLabel(new MenuIcon("CALENDAR", 16));
        iconCal.setForeground(Color.decode("#3B82F6"));
        JPanel pnlTime = new JPanel(new GridLayout(2,1)); pnlTime.setOpaque(false);
        JLabel l3 = new JLabel("Thời gian thanh toán"); l3.setForeground(Color.decode("#3B82F6")); l3.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblThoiGianThanhToan = new JLabel("---"); lblThoiGianThanhToan.setFont(new Font("Segoe UI", Font.BOLD, 13)); lblThoiGianThanhToan.setForeground(Color.decode("#1E3A8A"));
        pnlTime.add(l3); pnlTime.add(lblThoiGianThanhToan);
        pnlInvBot.add(iconCal); pnlInvBot.add(pnlTime);

        pnlInvoice.add(pnlInvTop, BorderLayout.NORTH); pnlInvoice.add(pnlInvMid, BorderLayout.CENTER); pnlInvoice.add(pnlInvBot, BorderLayout.SOUTH);

        pnlTableTraHang = new JPanel(new BorderLayout(0, 5));
        pnlTableTraHang.setBackground(Color.WHITE);
        pnlTableTraHang.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblTb = new JLabel("Sản phẩm trả hàng");
        lblTb.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlTableTraHang.add(lblTb, BorderLayout.NORTH);

        table = new JTable(chiTietModel);
        table.setRowHeight(38); 
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderGray));
        
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col == 0 && row >= 0) {
                    if (table.isEditing()) {
                        table.getCellEditor().stopCellEditing(); 
                    }
                    SwingUtilities.invokeLater(() -> capNhatDieuKienDoiTra());
                }
            }
        });

        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(5));
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(60); 
        table.getColumnModel().getColumn(3).setPreferredWidth(80); 
        table.getColumnModel().getColumn(3).setCellRenderer(new SpinnerRenderer());
        table.getColumnModel().getColumn(3).setCellEditor(new SpinnerEditor());
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        rightRenderer.setForeground(Color.GRAY);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); 

        spTableTraHang = new JScrollPane(table);
        spTableTraHang.getViewport().setBackground(Color.WHITE);
        spTableTraHang.setBorder(BorderFactory.createLineBorder(borderGray));
        pnlTableTraHang.add(spTableTraHang, BorderLayout.CENTER);
        
        pnlNewProduct = createNewProductPanel();
        pnlNewProduct.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pnlReasonNote = new JPanel(new GridLayout(1, 2, 15, 0)); 
        pnlReasonNote.setBackground(Color.WHITE);
        pnlReasonNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlReasonNote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel pnlLyDo = new JPanel(new BorderLayout(0, 5));
        pnlLyDo.setBackground(Color.WHITE);
        JLabel lblLd = new JLabel("<html><b>Lý do trả hàng</b></html>");
        lblLd.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboLyDo.setBackground(Color.WHITE);
        cboLyDo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboLyDo.setPreferredSize(new Dimension(0, 36));
        cboLyDo.addActionListener(e -> capNhatDieuKienDoiTra());
        pnlLyDo.add(lblLd, BorderLayout.NORTH);
        pnlLyDo.add(cboLyDo, BorderLayout.CENTER);
        
        JPanel pnlGhiChu = new JPanel(new BorderLayout(0, 5));
        pnlGhiChu.setBackground(Color.WHITE);
        JLabel lblGc = new JLabel("<html><b>Ghi chú thêm</b></html>");
        lblGc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtGhiChu = new JTextField();
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtGhiChu.setPreferredSize(new Dimension(0, 36));
        txtGhiChu.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borderGray), new EmptyBorder(0, 10, 0, 10)
        ));
        pnlGhiChu.add(lblGc, BorderLayout.NORTH);
        pnlGhiChu.add(txtGhiChu, BorderLayout.CENTER);
        
        pnlReasonNote.add(pnlLyDo);
        pnlReasonNote.add(pnlGhiChu);
        
        pnlPaymentSection = createPaymentSection();
        pnlPaymentSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlLeft.add(pnlInvoice);
        pnlLeft.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlLeft.add(pnlTableTraHang); 
        pnlLeft.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlLeft.add(pnlNewProduct);     
        pnlLeft.add(pnlReasonNote);     
        pnlLeft.add(pnlPaymentSection); 

        JPanel pnlLeftWrapper = new JPanel(new BorderLayout());
        pnlLeftWrapper.setBackground(Color.WHITE);
        pnlLeftWrapper.add(pnlLeft, BorderLayout.NORTH);

        JScrollPane scrollLeft = new JScrollPane(pnlLeftWrapper);
        scrollLeft.setBorder(null);
        scrollLeft.getViewport().setBackground(Color.WHITE);
        scrollLeft.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollLeft.getVerticalScrollBar().setUI(new Utils.ModernScrollBarUI());
        scrollLeft.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollLeft.getVerticalScrollBar().setUnitIncrement(16);

        JPanel pnlRightWrap = new JPanel(new BorderLayout());
        pnlRightWrap.setBackground(Color.WHITE);
        pnlRightWrap.setPreferredSize(new Dimension(280, 0)); 

        JPanel pnlSummary = new JPanel();
        pnlSummary.setLayout(new BoxLayout(pnlSummary, BoxLayout.Y_AXIS));
        pnlSummary.setBackground(Color.decode("#F8FAFC"));
        pnlSummary.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borderGray, 1, true), new EmptyBorder(20, 15, 20, 15) 
        ));

        JLabel lblSumTitle = new JLabel("Tổng kết giao dịch"); lblSumTitle.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblSumTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pnlGiaTri = new JPanel(new BorderLayout()); pnlGiaTri.setOpaque(false);
        JLabel lgt = new JLabel("Giá trị SP trả:"); lgt.setForeground(Color.GRAY); lgt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblGiaTriSPTra = new JLabel("0đ"); lblGiaTriSPTra.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlGiaTri.add(lgt, BorderLayout.WEST); pnlGiaTri.add(lblGiaTriSPTra, BorderLayout.EAST);

        JPanel pnlTGM = new JPanel(new BorderLayout()); pnlTGM.setOpaque(false);
        JLabel ltg = new JLabel("T/g đã mua:"); ltg.setForeground(Color.GRAY); ltg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblThoiGianMua = new JLabel("0 giờ"); lblThoiGianMua.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlTGM.add(ltg, BorderLayout.WEST); pnlTGM.add(lblThoiGianMua, BorderLayout.EAST);

        lblBadgeHoanTien = new JLabel("Vui lòng chọn SP", SwingConstants.CENTER);
        lblBadgeHoanTien.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadgeHoanTien.setForeground(Color.decode("#16A34A")); lblBadgeHoanTien.setBackground(Color.decode("#DCFCE7"));
        lblBadgeHoanTien.setOpaque(true); lblBadgeHoanTien.setBorder(new EmptyBorder(5, 0, 5, 0));
        lblBadgeHoanTien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26)); lblBadgeHoanTien.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator(); sep.setForeground(borderGray); sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JPanel pnlTienHoan = new JPanel(new BorderLayout()); pnlTienHoan.setOpaque(false);
        lblTextSoTienHoan = new JLabel("Số tiền hoàn"); lblTextSoTienHoan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSoTienHoan = new JLabel("0đ"); lblSoTienHoan.setFont(new Font("Segoe UI", Font.BOLD, 22)); lblSoTienHoan.setForeground(primaryRed);
        pnlTienHoan.add(lblTextSoTienHoan, BorderLayout.WEST); pnlTienHoan.add(lblSoTienHoan, BorderLayout.EAST);
        
        JLabel lblSub = new JLabel("Sẽ hoàn trả cho khách hàng", SwingConstants.RIGHT);
        lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 11)); lblSub.setForeground(Color.GRAY);
        lblSub.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JPanel pnlSubWrap = new JPanel(new BorderLayout()); pnlSubWrap.setOpaque(false);
        pnlSubWrap.add(lblSub, BorderLayout.EAST);

        pnlSummary.add(lblSumTitle); pnlSummary.add(Box.createRigidArea(new Dimension(0, 15))); pnlSummary.add(pnlGiaTri);
        pnlSummary.add(Box.createRigidArea(new Dimension(0, 10))); pnlSummary.add(pnlTGM); pnlSummary.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlSummary.add(lblBadgeHoanTien); pnlSummary.add(Box.createRigidArea(new Dimension(0, 15))); pnlSummary.add(sep);
        pnlSummary.add(Box.createRigidArea(new Dimension(0, 15))); pnlSummary.add(pnlTienHoan); pnlSummary.add(pnlSubWrap);
        
        pnlRightWrap.add(pnlSummary, BorderLayout.NORTH); 

        pnl.add(scrollLeft, BorderLayout.CENTER); 
        pnl.add(pnlRightWrap, BorderLayout.EAST);

        return pnl;
    }
    private void startDialogCountdown() {
        if (dialogTimer != null) dialogTimer.stop();
        remainingSeconds = 600; // Reset về 10 phút mỗi khi tìm mã mới
        lblTimer.setVisible(true);
        lblTimer.setForeground(Color.YELLOW);
        lblTimer.setText("THỜI GIAN: 10:00");

        dialogTimer = new Timer(1000, e -> {
            remainingSeconds--;
            if (remainingSeconds <= 0) {
                dialogTimer.stop();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(TaoPhieuDoiTra.this, 
                        "Đã hết 10 phút thao tác! Giao dịch tự động đóng để bảo vệ dữ liệu.", 
                        "HẾT GIỜ", JOptionPane.WARNING_MESSAGE);
                    dispose(); // Chỉ đóng Form, KHÔNG hủy DB
                });
            } else {
                int m = remainingSeconds / 60;
                int s = remainingSeconds % 60;
                lblTimer.setText(String.format("THỜI GIAN: %02d:%02d", m, s));
                if (remainingSeconds < 60) lblTimer.setForeground(Color.RED);
            }
        });
        dialogTimer.start();
    }
    
    // Ghi đè hàm dispose để tắt ngầm Timer khi đóng cửa sổ
    
    private JPanel createNewProductPanel() {
        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setBackground(Color.WHITE);
        pnlWrapper.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlMain.setBackground(Color.decode("#F8FAFC")); 
        pnlMain.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.decode("#E2E8F0"), 1, true), 
            new EmptyBorder(15, 15, 15, 15) 
        ));

        JLabel lblTitle = new JLabel("Sản phẩm mới");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.decode("#212B36"));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Đổi cùng loại miễn phí. Đổi khác loại sẽ tính chênh lệch.");
        lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblSub.setForeground(Color.decode("#2563EB")); 
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pnlSearchInput = new JPanel(new BorderLayout(8, 0));
        pnlSearchInput.setBackground(Color.WHITE);
        pnlSearchInput.setPreferredSize(new Dimension(0, 40));
        pnlSearchInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        pnlSearchInput.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlSearchInput.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.decode("#CBD5E1"), 1, true),
            new EmptyBorder(0, 10, 0, 10)
        ));

        JLabel lblIconSearch = new JLabel(new MenuIcon("SEARCH", 18));
        lblIconSearch.setForeground(Color.decode("#94A3B8"));

        txtSearchNew = new JTextField("Tìm sản phẩm thay thế...");
        txtSearchNew.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearchNew.setForeground(Color.GRAY);
        txtSearchNew.setBorder(null);

        txtSearchNew.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    String kw = txtSearchNew.getText().trim();
                    if (kw.isEmpty() || kw.equals("Tìm sản phẩm thay thế...")) return;
                    if (suggestionMenu != null) suggestionMenu.setVisible(false);
                    
                    new SwingWorker<java.util.List<Object[]>, Void>() {
                        @Override protected java.util.List<Object[]> doInBackground() throws Exception {
                            return new BUS.BUS_SanPham().timKiemSanPhamBan(kw);
                        }
                        @Override protected void done() {
                            try {
                                java.util.List<Object[]> ketQua = get();
                                if (ketQua != null && !ketQua.isEmpty()) {
                                    // Sắp xếp Lô cận hạn lên đầu
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

                                    // FIX: Đảm bảo tắt popup trước khi làm thao tác khác
                                    if (suggestionMenu != null) suggestionMenu.setVisible(false);

                                    if (ketQua.size() == 1) {
                                        xyLyThemSanPhamNhanh(ketQua.get(0), null, txtSearchNew);
                                    } else {
                                        boolean timThayDichDanh = false;
                                        if (kw.length() < 8) {
                                            for (Object[] row : ketQua) {
                                                String maSP = row[0] != null ? row[0].toString() : "";
                                                String soLo = row.length > 7 && row[7] != null ? row[7].toString() : "";
                                                if (maSP.equalsIgnoreCase(kw) || soLo.equalsIgnoreCase(kw)) {
                                                    xyLyThemSanPhamNhanh(row, null, txtSearchNew);
                                                    timThayDichDanh = true; break;
                                                }
                                            }
                                        }
                                        if (!timThayDichDanh) {
                                            // FIX: Dùng invokeLater đẩy việc bật Modal Dialog ra hàng đợi
                                            // để JPopupMenu kịp biến mất hoàn toàn, tránh giật/nhảy UI
                                            SwingUtilities.invokeLater(() -> {
                                                hienThiPopupChonLoKhiQuet(ketQua, null, txtSearchNew);
                                            });
                                        }
                                    }
                                } else {
                                    showCustomNotification("KHÔNG TÌM THẤY", "Không tìm thấy sản phẩm với mã: " + kw, "WARNING");
                                }
                            } catch (Exception ex) {}
                        }
                    }.execute();
                }
            }
        });
        
        // ĐÃ FIX 1: Bổ sung sự kiện bắt click chuột để quét sạch chữ mờ 100%
        txtSearchNew.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (txtSearchNew.getText().equals("Tìm sản phẩm thay thế...")) {
                    txtSearchNew.setText(""); txtSearchNew.setForeground(Color.BLACK);
                }
            }
        });

        pnlSearchInput.add(lblIconSearch, BorderLayout.WEST);
        pnlSearchInput.add(txtSearchNew, BorderLayout.CENTER);

        suggestionMenu = new JPopupMenu();
        suggestionMenu.setFocusable(false);
        suggestionMenu.setBackground(Color.WHITE);
        txtSearchNew.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { timKiemLive(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { timKiemLive(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { timKiemLive(); }
        });

        tableSPMoi = new JTable(spMoiModel) {
            @Override
            public javax.swing.table.TableCellEditor getCellEditor(int row, int column) {
                if (column == 1) { 
                    String dvtHienTai = getValueAt(row, 1) != null ? getValueAt(row, 1).toString().trim() : "";
                    String tenSP = getValueAt(row, 0) != null ? getValueAt(row, 0).toString().trim() : "";
                    
                    JComboBox<String> cbDVT = new JComboBox<>();
                    cbDVT.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    cbDVT.setBackground(Color.WHITE);

                    BUS_DonViDoLuong busDVDL_editor = new BUS_DonViDoLuong();
                    List<DonViDoLuong> dsDV_editor = busDVDL_editor.getDSTheoTenSP(tenSP);
                    boolean hasData = false;
                    for (DonViDoLuong dv : dsDV_editor) {
                        cbDVT.addItem(dv.getTen());
                        hasData = true;
                    }
                    if (!hasData && !dvtHienTai.isEmpty()) cbDVT.addItem(dvtHienTai);
                    cbDVT.setSelectedItem(dvtHienTai);
                    return new DefaultCellEditor(cbDVT);
                }
                return super.getCellEditor(row, column);
            }
        };
        
        tableSPMoi.setRowHeight(35);
        tableSPMoi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableSPMoi.setShowGrid(false);
        tableSPMoi.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        tableSPMoi.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // ĐVT
        
        // --- GẮN SPINNER VÀO CỘT SỐ LƯỢNG (CỘT 3) ---
        tableSPMoi.getColumnModel().getColumn(3).setCellRenderer(new SpinnerRenderer()); 
        tableSPMoi.getColumnModel().getColumn(3).setCellEditor(new SpinnerEditorSPMoi()); 
        
        tableSPMoi.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tableSPMoi.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); // Đơn giá
        tableSPMoi.getColumnModel().getColumn(6).setCellRenderer(rightRenderer); // Thành tiền
        
        // Render Nhãn xanh cho Lô/HSD (Cột 2)
        tableSPMoi.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel pnl = new JPanel(new GridBagLayout()); 
                pnl.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                String text = value != null ? value.toString().trim() : "";
                if (!text.isEmpty()) {
                    JLabel lbl = new JLabel(text);
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    lbl.setForeground(Color.decode("#059669"));
                    lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode("#34D399"), 1, true),
                        BorderFactory.createEmptyBorder(2, 6, 2, 6)
                    ));
                    pnl.add(lbl);
                }
                return pnl;
            }
        });

        // Nút Thùng rác (Cột 7)
        tableSPMoi.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(new MenuIcon("TRASH"));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setForeground(Color.decode("#EF4444"));
                lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return lbl;
            }
        });

        tableSPMoi.getColumnModel().getColumn(0).setPreferredWidth(160); // Tên
        tableSPMoi.getColumnModel().getColumn(1).setPreferredWidth(50);  // ĐVT
        tableSPMoi.getColumnModel().getColumn(2).setPreferredWidth(130); // Lô/HSD
        tableSPMoi.getColumnModel().getColumn(3).setPreferredWidth(45);  // SL
        tableSPMoi.getColumnModel().getColumn(4).setPreferredWidth(85);  // Đơn giá
        tableSPMoi.getColumnModel().getColumn(5).setPreferredWidth(45);  // VAT
        tableSPMoi.getColumnModel().getColumn(6).setPreferredWidth(85);  // Thành tiền
        tableSPMoi.getColumnModel().getColumn(7).setPreferredWidth(40);  // Xóa

        // Sự kiện xóa dòng
        tableSPMoi.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int row = tableSPMoi.rowAtPoint(e.getPoint());
                int col = tableSPMoi.columnAtPoint(e.getPoint());
                if (col == 7 && row >= 0 && SwingUtilities.isLeftMouseButton(e)) {
                    if (tableSPMoi.isEditing()) tableSPMoi.getCellEditor().stopCellEditing();
                    spMoiModel.removeRow(row);
                    tinhTongTienSPMoi();
                }
            }
        });

        tableSPMoi.getColumnModel().getColumn(0).setPreferredWidth(160); 
        tableSPMoi.getColumnModel().getColumn(1).setPreferredWidth(60);  
        tableSPMoi.getColumnModel().getColumn(2).setPreferredWidth(35);  
        tableSPMoi.getColumnModel().getColumn(3).setPreferredWidth(85);  
        tableSPMoi.getColumnModel().getColumn(4).setPreferredWidth(45);  
        tableSPMoi.getColumnModel().getColumn(5).setPreferredWidth(85);  
        tableSPMoi.getColumnModel().getColumn(6).setPreferredWidth(40); // Cột Thùng rác

        // Bắt sự kiện Click để Xóa dòng
        tableSPMoi.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int row = tableSPMoi.rowAtPoint(e.getPoint());
                int col = tableSPMoi.columnAtPoint(e.getPoint());
                if (col == 6 && row >= 0 && SwingUtilities.isLeftMouseButton(e)) {
                    if (tableSPMoi.isEditing()) tableSPMoi.getCellEditor().stopCellEditing();
                    spMoiModel.removeRow(row);
                    tinhTongTienSPMoi();
                }
            }
        });
        
        scrollSPMoi = new JScrollPane(tableSPMoi);
        scrollSPMoi.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#E2E8F0")));
        scrollSPMoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // ĐÃ FIX 2: Khóa vĩnh viễn thanh cuộn dọc và ngang của bảng này
        scrollSPMoi.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollSPMoi.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        scrollSPMoi.setPreferredSize(new Dimension(0, 0));
        scrollSPMoi.setVisible(false);

        pnlMain.add(lblTitle);
        pnlMain.add(Box.createVerticalStrut(5));
        pnlMain.add(lblSub);
        pnlMain.add(Box.createVerticalStrut(10));
        pnlMain.add(pnlSearchInput);
        pnlMain.add(Box.createVerticalStrut(10));
        pnlMain.add(scrollSPMoi);

        pnlWrapper.add(pnlMain, BorderLayout.CENTER);
        return pnlWrapper;
    }
    
    private void xuLyTimKiemHD() {
    	if (currentInvoiceSearchWorker != null && !currentInvoiceSearchWorker.isDone()) {
            currentInvoiceSearchWorker.cancel(true); // Giết tiến trình chạy ngầm
        }
        if (suggestionInvoiceMenu != null) {
            suggestionInvoiceMenu.setVisible(false); // Đóng ngay lập tức menu nếu nó đang mở
        }
        // --- KẾT THÚC FIX ---

        String maHD = txtSearch.getText().trim();
        if (maHD.isEmpty() || maHD.equals("Nhập mã hóa đơn (VD: HD-2024-0001)...")) {
            lblError.setText(" Vui lòng nhập mã hóa đơn!"); lblError.setVisible(true); 
            return;
        }

        BUS_HoaDon busHD = new BUS_HoaDon();
        HoaDon hd = busHD.getHoaDonTheoMa(maHD); 

        if (hd == null) {
            lblError.setText(" Không tìm thấy hóa đơn này trong hệ thống!"); lblError.setVisible(true);
            return;
        }

        String ghiChuThucTe = busHD.layGhiChuHoaDon(maHD);

        if (ghiChuThucTe == null) ghiChuThucTe = "";

        // Kiểm tra điều kiện: Chặn các hóa đơn không thành công
        boolean laHoaDonLoi = ghiChuThucTe.contains("Đã hủy") || 
                              ghiChuThucTe.contains("Đang xử lý") || 
                              ghiChuThucTe.contains("Lưu nháp") ||
                              ghiChuThucTe.contains("Từ chối");

        if (laHoaDonLoi) {
            lblError.setText(" Chỉ hỗ trợ đổi trả cho hóa đơn ĐÃ THANH TOÁN THÀNH CÔNG!"); 
            lblError.setVisible(true);
            if (pnlFoundData != null && pnlFoundData.isVisible()) { 
                pnlFoundData.setVisible(false); 
                setSize(1000, 400);
                setLocationRelativeTo(getOwner()); 
            }
            if (btnTaoPhieu != null) btnTaoPhieu.setEnabled(true); if (btnLuuNhap != null) btnLuuNhap.setEnabled(true); if (btnLuuNhap != null) btnLuuNhap.setEnabled(true); 
            return;
        }

        // =========================================================
        // RÀNG BUỘC: KIỂM TRA ĐÃ TỪNG ĐỔI / TRẢ CHƯA 
        // =========================================================
        boolean daCoPhieu = false;
        String maPhieuCu = "";
        String loaiPhieuCu = "Đổi / Trả";

        String[] phieuCu = busHD.layPhieuDoiTraTheoHDGoc(hd.getId());
        if (phieuCu != null) {
            daCoPhieu = true;
            maPhieuCu = phieuCu[0];
            String loai = phieuCu[1];
            if (loai != null) {
                if (loai.contains("TRA_HANG")) loaiPhieuCu = "Trả hàng";
                else if (loai.contains("DOI_HANG")) loaiPhieuCu = "Đổi hàng";
            }
        }

        if (daCoPhieu) {
            if (isEditMode) {
                // Đang sửa nháp thì cho phép bypass lỗi khóa hóa đơn
            } else {
                lblError.setText(" Hóa đơn này đã được " + loaiPhieuCu + " trước đó (Mã: " + maPhieuCu + ")!"); 
                lblError.setVisible(true);
                if (pnlFoundData != null && pnlFoundData.isVisible()) { pnlFoundData.setVisible(false); setSize(1000, 400); setLocationRelativeTo(getOwner()); }
                if (btnTaoPhieu != null) btnTaoPhieu.setEnabled(false); if (btnLuuNhap != null) btnLuuNhap.setEnabled(false); if (btnLuuNhap != null) btnLuuNhap.setEnabled(false); if (btnLuuNhap != null) btnLuuNhap.setEnabled(false); 
                return;
            }
        }

        boolean isHopLe = busTraHang.kiemTraDieuKien(maHD);
        if (!isHopLe) {
            lblError.setText(" Hóa đơn đã quá hạn đổi trả hoặc không hợp lệ!"); lblError.setVisible(true);
            if (pnlFoundData != null && pnlFoundData.isVisible()) { pnlFoundData.setVisible(false); setSize(850, 330); setLocationRelativeTo(getOwner()); }
            if (btnTaoPhieu != null) btnTaoPhieu.setEnabled(false); if (btnLuuNhap != null) btnLuuNhap.setEnabled(false); if (btnLuuNhap != null) btnLuuNhap.setEnabled(false); if (btnLuuNhap != null) btnLuuNhap.setEnabled(false); 
            return;
        }

     // --- 1. RÀO CHẮN LỖI NGÀY HÓA ĐƠN RỖNG ---
        ngayHoaDonGoc = hd.getNgayLapHD(); 
        if (ngayHoaDonGoc == null) {
            ngayHoaDonGoc = LocalDateTime.now(); // Gán tạm ngày hiện tại nếu DB bị mất ngày
        }
        
        chiTietModel.setRowCount(0); 
        tongTienGoc = 0; 
        
        BUS_ChiTietHoaDon busCTHD = new BUS_ChiTietHoaDon(); 
        List<Object[]> dsChiTiet = busCTHD.layDuLieuDoiTra(hd.getId()); 

        if (dsChiTiet != null && !dsChiTiet.isEmpty()) {
            java.util.Set<String> addedProducts = new java.util.HashSet<>();
            DAO.DAO_HoaDon daoHD = new DAO.DAO_HoaDon(); 

            for (Object[] rowData : dsChiTiet) {
                // --- 2. RÀO CHẮN LỖI TÊN SẢN PHẨM RỖNG ---
                String tenSP = (rowData[0] != null) ? rowData[0].toString() : "Sản phẩm không xác định";
                
                // --- 3. RÀO CHẮN LỖI ÉP KIỂU SỐ LƯỢNG ---
                int slMua = 1;
                try {
                    if (rowData[2] != null) {
                        slMua = Integer.parseInt(rowData[2].toString());
                    }
                } catch (Exception e) {
                    // Nếu dữ liệu bị lỗi chữ, mặc định số lượng là 1
                }
                
                long donGiaNum = 0;
                String correctDvt = "Hộp";

                try {
                    Object[] info = daoHD.layThongTinGiaTuHDGoc(hd.getId(), tenSP);
                    if (info != null) {
                        if (info[0] != null) correctDvt = info[0].toString();
                        if (info[1] != null) donGiaNum = Math.round((Double) info[1]); 
                    }
                } catch(Exception e) {}

                if (donGiaNum <= 0) {
                    try { 
                       
                        if (rowData[3] != null) {
                            donGiaNum = Long.parseLong(rowData[3].toString().replaceAll("[^0-9]", "")); 
                        }
                    } catch(Exception e){}
                }
                
                String uniqueKey = tenSP + "_" + donGiaNum;
                if (!addedProducts.contains(uniqueKey)) {
                    String donGiaFmt = String.format("%,dđ", donGiaNum).replace(',', '.');
                    chiTietModel.addRow(new Object[]{false, tenSP, correctDvt, slMua, donGiaFmt, slMua});
                    addedProducts.add(uniqueKey);
                }
            }
        }

        String khachName = busHD.layTenKhachHangTheoHD(hd.getId());
        
        lblKhachHang.setText(khachName); lblMaHoaDonInfo.setText(hd.getId());
        lblThoiGianThanhToan.setText(hd.getNgayLapHD().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lblThoiGianMua.setText(java.time.Duration.between(ngayHoaDonGoc, LocalDateTime.now()).toHours() + " giờ");

        lblError.setVisible(false); pnlFoundData.setVisible(true); 
        if (btnTaoPhieu != null) {
            btnTaoPhieu.setVisible(true);
            btnTaoPhieu.setEnabled(true); if (btnLuuNhap != null) btnLuuNhap.setEnabled(true); if (btnLuuNhap != null) btnLuuNhap.setEnabled(true);
        }
        if (btnLuuNhap != null) {
            btnLuuNhap.setVisible(true);
            btnLuuNhap.setEnabled(true);
        }
        setSize(1200, 800); setLocationRelativeTo(getOwner());
        setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15)); 
        capNhatDieuKienDoiTra(); 

        SwingUtilities.invokeLater(() -> {
            if (pnlTableTraHang != null && spTableTraHang != null && table != null) {
                int rHeight = (chiTietModel.getRowCount() * table.getRowHeight()) + (table.getTableHeader() != null ? table.getTableHeader().getPreferredSize().height : 0) + 8;
                spTableTraHang.setPreferredSize(new Dimension(0, rHeight)); spTableTraHang.setMaximumSize(new Dimension(Integer.MAX_VALUE, rHeight)); 
                pnlTableTraHang.setMaximumSize(new Dimension(Integer.MAX_VALUE, rHeight + 40)); 
                pnlFoundData.revalidate(); pnlFoundData.repaint();
            }
        });
    }

    private void checkDieuKienThanhToan() {
        // ĐÃ FIX: Không khóa nút cứng ngắc nữa để người dùng có thể bấm vào xem thông báo lỗi!
        if (btnTaoPhieu != null) {
            boolean isTraHang = btnTraHang.getBackground().equals(Color.WHITE);
            // Chỉ mở nút khi đã có sản phẩm gốc (Trả) HOẶC có cả 2 (Đổi)
            if (isTraHang || spMoiModel.getRowCount() > 0) {
            	btnTaoPhieu.setEnabled(true); if (btnLuuNhap != null) btnLuuNhap.setEnabled(true); if (btnLuuNhap != null) btnLuuNhap.setEnabled(true); if (btnLuuNhap != null) btnLuuNhap.setEnabled(true);
            } else {
                btnTaoPhieu.setEnabled(false); if (btnLuuNhap != null) btnLuuNhap.setEnabled(false); if (btnLuuNhap != null) btnLuuNhap.setEnabled(false); if (btnLuuNhap != null) btnLuuNhap.setEnabled(false);
            }
        }
    }
    
    private void capNhatDieuKienDoiTra() {
        if (lblSoTienHoan == null || cboLyDo == null || ngayHoaDonGoc == null) return;

        isCKXacNhan = false;
        if (btnXacNhanCK != null) {
            btnXacNhanCK.setText("Đã nhận tiền chuyển khoản");
            btnXacNhanCK.setBackground(Color.decode("#22C55E"));
        }

        long soGioDaMua = java.time.Duration.between(ngayHoaDonGoc, LocalDateTime.now()).toHours();
        String lyDo = cboLyDo.getSelectedItem().toString();
        boolean isTraHang = btnTraHang.getBackground().equals(Color.WHITE); 
        
        double giaTriGoc = 0;
        int countSelected = 0;
        
        for (int i = 0; i < chiTietModel.getRowCount(); i++) {
            boolean isSelected = false;
            Object val = chiTietModel.getValueAt(i, 0);
            if (val != null) isSelected = (boolean) val;
            
            if (isSelected) {
                int sl = (int) chiTietModel.getValueAt(i, 3); 
                String donGiaStr = chiTietModel.getValueAt(i, 4).toString();
                double donGia = Double.parseDouble(donGiaStr.replaceAll("[^0-9]", ""));
                giaTriGoc += sl * donGia;
                countSelected++;
            }
        }
        
        lblGiaTriSPTra.setText(String.format("%,.0fđ", giaTriGoc));

        if (countSelected == 0) {
            lblBadgeHoanTien.setText("Vui lòng chọn SP"); lblBadgeHoanTien.setForeground(Color.decode("#CA8A04")); lblBadgeHoanTien.setBackground(Color.decode("#FEF9C3"));
            lblSoTienHoan.setText("0đ");
            if(btnTaoPhieu != null) btnTaoPhieu.setEnabled(true); if (btnLuuNhap != null) btnLuuNhap.setEnabled(true); if (btnLuuNhap != null) btnLuuNhap.setEnabled(true); 
            return;
        }

        double phanTramHoan = 0.0;
        String thongBaoHoanTien = "";

        if (lyDo.contains("Từ chối")) {
            phanTramHoan = 0.0;
            thongBaoHoanTien = "Từ chối (Lỗi KH)";
        } else if (lyDo.contains("Khách hàng đổi ý")) { 
            if (soGioDaMua <= 24) {
                phanTramHoan = 100.0;
                thongBaoHoanTien = "Hoàn 100% (≤ 24 giờ)";
            } else if (soGioDaMua > 24 && soGioDaMua <= 72) {
                phanTramHoan = 80.0;
                thongBaoHoanTien = "Hoàn 80% (> 24h & ≤ 3 ngày)";
            } else {
                phanTramHoan = 0.0;
                thongBaoHoanTien = "Từ chối (> 3 ngày)";
            }
        } else if (lyDo.contains("Hỗ trợ 100%")) {
            if (soGioDaMua <= 24) {
                phanTramHoan = 100.0;
                thongBaoHoanTien = "Hoàn 100% (≤ 24 giờ)";
            } else if (soGioDaMua > 24 && soGioDaMua <= 72) {
                phanTramHoan = 80.0;
                thongBaoHoanTien = "Hoàn 80% (> 24h & ≤ 3 ngày)";
            } else {
                phanTramHoan = 0.0;
                thongBaoHoanTien = "Từ chối (> 3 ngày)";
            }
        } else if (lyDo.contains("Hỗ trợ 80%")) {
            if (soGioDaMua <= 72) {
                phanTramHoan = 80.0;
                thongBaoHoanTien = "Hoàn 80% (Khách đổi ý)";
            } else {
                phanTramHoan = 0.0;
                thongBaoHoanTien = "Từ chối (> 3 ngày)";
            }
        }

        double v_qd = busTraHang.xacDinhMucHoanTien(giaTriGoc, phanTramHoan); 

        if (isTraHang) {
            lblTextSoTienHoan.setText("Số tiền hoàn");
            lblBadgeHoanTien.setText(thongBaoHoanTien);
            
            if (phanTramHoan == 0.0) {
                lblBadgeHoanTien.setForeground(primaryRed); 
                lblBadgeHoanTien.setBackground(Color.decode("#FEE2E2"));
            } else {
                lblBadgeHoanTien.setForeground(Color.decode("#16A34A")); 
                lblBadgeHoanTien.setBackground(Color.decode("#DCFCE7"));
            }
            
            this.soTienThucTeCanXuLy = (long) v_qd;
            lblSoTienHoan.setText(String.format("%,.0fđ", v_qd)); 
            lblSoTienHoan.setForeground(primaryRed);
            
            btnTaoPhieu.setEnabled(true); if (btnLuuNhap != null) btnLuuNhap.setEnabled(true); if (btnLuuNhap != null) btnLuuNhap.setEnabled(true);
        } else {
            lblBadgeHoanTien.setText("Đang tính chênh lệch..."); 
            lblBadgeHoanTien.setForeground(primaryBlue); 
            lblBadgeHoanTien.setBackground(Color.decode("#DBEAFE"));
            
            if (spMoiModel.getRowCount() == 0) {
                lblSoTienHoan.setText("0đ"); 
                lblTextSoTienHoan.setText("Vui lòng thêm SP mới");
                this.soTienThucTeCanXuLy = 0; 
                
                if (btnTaoPhieu != null) btnTaoPhieu.setEnabled(false); if (btnLuuNhap != null) btnLuuNhap.setEnabled(false); if (btnLuuNhap != null) btnLuuNhap.setEnabled(false); if (btnLuuNhap != null) btnLuuNhap.setEnabled(false);
            } else {
                double chenhLech = giaMoi - v_qd; 
                this.soTienThucTeCanXuLy = (long) Math.abs(chenhLech);
                
                if (chenhLech > 0) {
                    lblTextSoTienHoan.setText("Khách bù thêm"); 
                    lblSoTienHoan.setForeground(primaryBlue);
                    if ("Chuyển khoản".equals(phuongThucDoiTra)) updateQRCode();
                } else {
                    lblTextSoTienHoan.setText("Thối lại khách"); 
                    lblSoTienHoan.setForeground(primaryRed);
                    if (lblQRCode != null) {
                        lblQRCode.setIcon(null);
                        lblQRCode.setText("Hoàn tiền qua STK khách");
                    }
                }
                
                lblSoTienHoan.setText(String.format("%,.0fđ", Math.abs(chenhLech)));
                lblBadgeHoanTien.setText(thongBaoHoanTien); 
                if (phanTramHoan == 0.0) {
                    lblBadgeHoanTien.setForeground(primaryRed); 
                    lblBadgeHoanTien.setBackground(Color.decode("#FEE2E2"));
                }
                
                if (btnTaoPhieu != null) {
                    btnTaoPhieu.setEnabled(true); if (btnLuuNhap != null) btnLuuNhap.setEnabled(true); if (btnLuuNhap != null) btnLuuNhap.setEnabled(true);
                }
            }
        }
        
        if (lblExactValue != null) {
            lblExactValue.setText(String.format("%,dđ", soTienThucTeCanXuLy));
        }
        capNhatTongTienMat(); 
    }

    private void setToggleState(boolean isTraHang) {
        if (isTraHang) {
            btnTraHang.setBackground(Color.WHITE); btnTraHang.setForeground(primaryRed); btnTraHang.setBorder(new LineBorder(borderGray, 1, true));
            btnDoiHang.setBackground(Color.decode("#F1F5F9")); btnDoiHang.setForeground(Color.decode("#64748B")); btnDoiHang.setBorder(BorderFactory.createEmptyBorder());
            if (pnlHeader != null) pnlHeader.setBackground(primaryRed);
            JLabel lblTitle = (JLabel) pnlHeader.getComponent(0); lblTitle.setText("TẠO PHIẾU TRẢ HÀNG");
            btnTaoPhieu.setText("Xác nhận trả hàng"); btnTaoPhieu.setBackground(primaryRed);
        } else {
            btnDoiHang.setBackground(Color.WHITE); btnDoiHang.setForeground(primaryBlue); btnDoiHang.setBorder(new LineBorder(borderGray, 1, true));
            btnTraHang.setBackground(Color.decode("#F1F5F9")); btnTraHang.setForeground(Color.decode("#64748B")); btnTraHang.setBorder(BorderFactory.createEmptyBorder());
            if (pnlHeader != null) pnlHeader.setBackground(primaryBlue);
            JLabel lblTitle = (JLabel) pnlHeader.getComponent(0); lblTitle.setText("TẠO PHIẾU ĐỔI HÀNG");
            btnTaoPhieu.setText("Xác nhận đổi hàng"); btnTaoPhieu.setBackground(primaryBlue);
        }
        
        if (pnlNewProduct != null) pnlNewProduct.setVisible(!isTraHang); 
        
        if (pnlPaymentSection != null) pnlPaymentSection.setVisible(!isTraHang);
        if (pnlFoundData != null) {
            pnlFoundData.revalidate();
            pnlFoundData.repaint();
        }
        
        capNhatDieuKienDoiTra();
    }

    private void tinhTongTienSPMoi() {
        giaMoi = 0; tenMoi = "";
        
        int colThanhTien = 6; // Đã đổi thành cột 6
        
        for (int i = 0; i < spMoiModel.getRowCount(); i++) {
            Object valTien = spMoiModel.getValueAt(i, colThanhTien);
            if (valTien != null) {
                giaMoi += Double.parseDouble(valTien.toString().replaceAll("[^0-9]", ""));
            }
            
            Object valTen = spMoiModel.getValueAt(i, 0);
            if (valTen != null) {
                tenMoi += valTen.toString() + ", ";
            }
        }
        if(tenMoi.endsWith(", ")) tenMoi = tenMoi.substring(0, tenMoi.length() - 2);
        
        SwingUtilities.invokeLater(() -> {
            if (scrollSPMoi != null && tableSPMoi != null) {
                if (spMoiModel.getRowCount() > 0) {
                    scrollSPMoi.setVisible(true);
                    int headerHeight = tableSPMoi.getTableHeader() != null ? tableSPMoi.getTableHeader().getPreferredSize().height : 0;
                    int rowHeight = tableSPMoi.getRowHeight();
                    int totalHeight = (spMoiModel.getRowCount() * rowHeight) + headerHeight + 5; 
                    
                    scrollSPMoi.setPreferredSize(new Dimension(0, totalHeight));
                    scrollSPMoi.setMinimumSize(new Dimension(0, totalHeight)); 
                    scrollSPMoi.setMaximumSize(new Dimension(Integer.MAX_VALUE, totalHeight));
                } else {
                    scrollSPMoi.setVisible(false);
                }
                
                if (pnlNewProduct != null) {
                    pnlNewProduct.revalidate();
                    pnlNewProduct.repaint();
                }
            }
        });
        
        capNhatDieuKienDoiTra();
    }
    
    private SwingWorker<java.util.List<Object[]>, Void> currentSearchWorker;

    private void timKiemLive() {
        if (isSelectingProduct) return;

        String kw = txtSearchNew.getText().trim();
        if (kw.isEmpty() || kw.equals("Tìm sản phẩm thay thế...")) { 
            SwingUtilities.invokeLater(() -> suggestionMenu.setVisible(false)); 
            return; 
        }

        if (currentSearchWorker != null && !currentSearchWorker.isDone()) {
            currentSearchWorker.cancel(true);
        }

        currentSearchWorker = new SwingWorker<java.util.List<Object[]>, Void>() {
            @Override
            protected java.util.List<Object[]> doInBackground() throws Exception {
                return new BUS.BUS_SanPham().timKiemSanPhamBan(kw);
            }

            @Override
            protected void done() {
                if (isCancelled()) return;
                try {
                    java.util.List<Object[]> ketQua = get();
                    SwingUtilities.invokeLater(() -> {
                        if (!txtSearchNew.getText().trim().equals(kw)) return;

                        if (ketQua != null && !ketQua.isEmpty()) {
                            suggestionMenu.removeAll(); 
                            JPanel pnlList = new JPanel(); 
                            pnlList.setLayout(new BoxLayout(pnlList, BoxLayout.Y_AXIS)); 
                            pnlList.setBackground(Color.WHITE);

                            int count = 0;
                            int MAX_ITEMS = 12; 

                            java.util.Map<String, java.util.List<Object[]>> groupedSP = new java.util.LinkedHashMap<>();
                            for (Object[] row : ketQua) {
                                String key = row[1].toString() + "_" + (row[2] != null ? row[2].toString() : "");
                                groupedSP.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(row);
                            }

                            for (java.util.List<Object[]> batches : groupedSP.values()) {
                                if (count >= MAX_ITEMS) break; 

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

                                Object[] firstItem = batches.get(0);
                                String ten = firstItem[1].toString();
                                String donVi = firstItem[2] != null ? firstItem[2].toString() : "";
                                long giaBan = Math.round(Double.parseDouble(firstItem[3].toString()));
                                String gia = String.valueOf(giaBan); 
                                
                                int tongTon = 0;
                                for(Object[] b : batches) {
                                    try { tongTon += Integer.parseInt(b[4].toString()); } catch(Exception ex){}
                                }
                                String tonKho = String.valueOf(tongTon);
                                String danhMuc = (firstItem.length > 5 && firstItem[5] != null) ? firstItem[5].toString() : "Khác";

                                String thueVat = "5%";
                                if (firstItem.length > 6 && firstItem[6] != null) {
                                    String rawVat = firstItem[6].toString().trim();
                                    try {
                                        double v = Double.parseDouble(rawVat);
                                        if (v > 0 && v < 1) v = v * 100; 
                                        thueVat = (int)v + "%";
                                    } catch (Exception ex) {}
                                }

                                pnlList.add(createSuggestionItem(suggestionMenu, txtSearchNew, "", ten, donVi, gia, tonKho, danhMuc, thueVat, batches));
                                count++; 
                            }
                            
                            if (ketQua.size() > MAX_ITEMS) {
                                JLabel lblMore = new JLabel("Còn " + (ketQua.size() - MAX_ITEMS) + " sản phẩm khác. Vui lòng gõ thêm chi tiết...");
                                lblMore.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                                lblMore.setForeground(Color.GRAY);
                                lblMore.setBorder(new EmptyBorder(8, 15, 8, 15));
                                pnlList.add(lblMore);
                            }

                            JScrollPane scrollPane = new JScrollPane(pnlList); 
                            scrollPane.setBorder(null); 
                            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); 
                            scrollPane.setPreferredSize(new Dimension(txtSearchNew.getWidth(), Math.min(pnlList.getPreferredSize().height, 250)));
                            scrollPane.getVerticalScrollBar().setUI(new Utils.ModernScrollBarUI()); 
                            scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0)); 
                            
                            suggestionMenu.add(scrollPane); 
                            suggestionMenu.pack(); 
                            if (!suggestionMenu.isVisible() && txtSearchNew.isShowing()) {
                                suggestionMenu.show(txtSearchNew, 0, txtSearchNew.getHeight());
                            }
                        } else {
                            suggestionMenu.setVisible(false);
                        }
                    });
                } catch (Exception ex) {}
            }
        };
        currentSearchWorker.execute();
    }

    private void xuLyTaoPhieu(boolean isLuuNhap) {
        if (ngayHoaDonGoc == null || txtSearch.getText().contains("Nhập mã hóa đơn")) {
            showCustomNotification("CẢNH BÁO", "Vui lòng tìm kiếm hóa đơn hợp lệ trước khi tạo phiếu!", "WARNING"); return;
        }

        String loai = btnTraHang.getBackground().equals(Color.WHITE) ? "Trả hàng" : "Đổi hàng";
        if (loai.equals("Đổi hàng") && spMoiModel.getRowCount() == 0) {
            showCustomNotification("CẢNH BÁO", "Vui lòng tìm và chọn sản phẩm mới muốn đổi sang!", "WARNING"); return;
        }

        // ==========================================================
        // FIX LỖI: CHỈ BẮT BUỘC KIỂM TRA THANH TOÁN NẾU KHÔNG PHẢI LÀ LƯU NHÁP
        // ==========================================================
        if (!isLuuNhap) {
            if (lblTextSoTienHoan != null && lblTextSoTienHoan.getText().contains("Khách bù")) {
                if ("Chuyển khoản".equals(phuongThucDoiTra)) {
                    if (!isCKXacNhan) {
                        boolean xacNhan = showCustomConfirmDialog(
                            "Xác nhận nhận tiền", 
                            "Khách hàng cần BÙ THÊM TIỀN bằng hình thức <b>CHUYỂN KHOẢN</b>.<br><br>" +
                            "Nhân viên vui lòng kiểm tra App Ngân hàng hoặc SMS.<br>" +
                            "Bạn xác nhận <b>ĐÃ NHẬN ĐỦ TIỀN</b> chưa?"
                        );
                        if (!xacNhan) return; 
                    }
                } else if ("Tiền mặt".equals(phuongThucDoiTra)) {
                    if (tongTienMat < soTienThucTeCanXuLy) {
                        String strCanTra = String.format("%,d", soTienThucTeCanXuLy).replace(',', '.') + "đ";
                        String strKhachDua = String.format("%,d", tongTienMat).replace(',', '.') + "đ";
                        showCustomNotification(
                            "THIẾU TIỀN MẶT", 
                            "Khách đưa chưa đủ tiền bù hoặc bạn quên nhập mệnh giá!\n\n" +
                            "• Khách cần bù: " + strCanTra + "\n" +
                            "• Đã nhập: " + strKhachDua + "\n\n" +
                            "Vui lòng chọn đủ mệnh giá tiền khách đưa trước khi xác nhận.", 
                            "WARNING"
                        );
                        return; 
                    }
                }
            }
        }
        // ==========================================================

        String lyDo = cboLyDo.getSelectedItem().toString();
        String colHoanTien = "0đ";
        String colChenhLech = "0đ";

        try {
            if (loai.equals("Trả hàng")) colHoanTien = lblSoTienHoan.getText().trim();
            else colChenhLech = lblSoTienHoan.getText().trim(); 
        } catch (Exception ex) {
            showCustomNotification("LỖI HỆ THỐNG", "Lỗi tính toán tiền tệ!", "ERROR"); return;
        }

        if(mainModel != null) {
            String maHDGoc = txtSearch.getText().trim();
            String ngayTao = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String khach = lblKhachHang.getText().trim(); 
            String ghiChu = txtGhiChu.getText().trim();

            StringBuilder spTraSb = new StringBuilder();
            boolean hasSelected = false;
            if (table.isEditing()) table.getCellEditor().stopCellEditing();
            
            for (int i = 0; i < chiTietModel.getRowCount(); i++) {
                Object val = chiTietModel.getValueAt(i, 0);
                boolean isSelected = (val != null && (boolean) val);
                
                if (isSelected) { 
                    String tenSp = chiTietModel.getValueAt(i, 1).toString();
                    String dvt = chiTietModel.getValueAt(i, 2).toString();
                    String soLuong = chiTietModel.getValueAt(i, 3).toString();
                    String giaChuan = chiTietModel.getValueAt(i, 4).toString().replaceAll("[^0-9]", ""); 
                    
                    spTraSb.append(tenSp).append("_").append(dvt).append("_").append(soLuong).append("_").append(giaChuan).append("; ");
                    hasSelected = true;
                }
            }
            
            if (!hasSelected) {
                showCustomNotification("CẢNH BÁO", "Vui lòng tick chọn ít nhất 1 sản phẩm cần trả (cột Chọn)!", "WARNING"); return;
            }
            String strSPTra = "TRA: " + spTraSb.toString().replaceAll("; $", "");

            String strSPDoi = "DOI: Không có";
            if (loai.equals("Đổi hàng") && spMoiModel.getRowCount() > 0) {
                StringBuilder spDoiSb = new StringBuilder();
                if (tableSPMoi.isEditing()) tableSPMoi.getCellEditor().stopCellEditing();
                
                for(int i = 0; i < spMoiModel.getRowCount(); i++) {
                    String tenSp = spMoiModel.getValueAt(i, 0).toString();
                    String dvt = spMoiModel.getValueAt(i, 1).toString();
                    String sl = spMoiModel.getValueAt(i, 3).toString(); 
                    String giaChuan = spMoiModel.getValueAt(i, 4).toString().replaceAll("[^0-9]", ""); 
                    
                    spDoiSb.append(tenSp).append("_").append(dvt).append("_").append(sl).append("_").append(giaChuan).append("; ");
                }
                strSPDoi = "DOI: " + spDoiSb.toString().replaceAll("; $", "");
            }

            String lyDoFull = lyDo + (ghiChu.isEmpty() ? "" : " - " + ghiChu);
            
            // XÁC ĐỊNH TRẠNG THÁI: Nếu lưu nháp là "Lưu nháp", nếu xác nhận đổi/trả thì "Hoàn thành"
            String trangThaiPhieu = isLuuNhap ? "Lưu nháp" : "Hoàn thành";
            String formatGhiChu = trangThaiPhieu + " | " + lyDoFull + " | " + colHoanTien + " | " + colChenhLech + " | " + strSPTra + " | " + strSPDoi;
            
            // NẾU ĐANG SỬA THÌ DÙNG LẠI MÃ CŨ, NẾU TẠO MỚI THÌ DÙNG MÃ MỚI
            String maPhieu = isEditMode ? maPhieuDangSua : "DTH-" + (System.currentTimeMillis() % 10000);

            HoaDon hdDoiTra = new HoaDon();
            hdDoiTra.setId(maPhieu);
            hdDoiTra.setLoaiHD(loai.equals("Trả hàng") ? Enumeration.LoaiHoaDon.TRA_HANG : Enumeration.LoaiHoaDon.DOI_HANG);
            hdDoiTra.setNgayLapHD(LocalDateTime.now());
            
            Entity.NhanVien nv = new Entity.NhanVien();
            String maNVHienTai = Utils.UserSession.getInstance().getMaNhanVien();
            if (maNVHienTai == null || maNVHienTai.trim().isEmpty()) {
                maNVHienTai = "DS-0001";
            }
            nv.setNhanVien(maNVHienTai);
            hdDoiTra.setNhanVienId(nv);
            hdDoiTra.setPhuongThucThanhToan(phuongThucDoiTra.equals("Chuyển khoản") ? 
                Enumeration.PhuongThucThanhToan.CHUYEN_KHOAN_NGAN_HANG : Enumeration.PhuongThucThanhToan.TIEN_MAT);
            
            hdDoiTra.setGhiChu(formatGhiChu);
            
            HoaDon hdGoc = new HoaDon();
            hdGoc.setId(maHDGoc);
            hdDoiTra.setHoaDonGocId(hdGoc);
            
            // =========================================================
            // FIX LỖI PRIMARY KEY: XÓA BẢN NHÁP CŨ TRƯỚC KHI LƯU ĐÈ
            // =========================================================
            if (isEditMode) {
                try {
                    java.sql.Connection con = ConnectDB.ConnectDB.getInstance().getConnection();
                    // 1. Xóa chi tiết hóa đơn cũ
                    java.sql.PreparedStatement ps1 = con.prepareStatement("DELETE FROM ChiTietHoaDon WHERE hoaDonId = ?");
                    ps1.setString(1, maPhieuDangSua);
                    ps1.executeUpdate();
                    // 2. Xóa hóa đơn nháp cũ
                    java.sql.PreparedStatement ps2 = con.prepareStatement("DELETE FROM HoaDon WHERE id = ?");
                    ps2.setString(1, maPhieuDangSua);
                    ps2.executeUpdate();
                } catch (Exception ex) {
                    System.out.println("Lỗi dọn dẹp phiếu nháp cũ: " + ex.getMessage());
                }
            }
            // =========================================================

            if (busHD.taoPhieuDoiTra(hdDoiTra)) {
                
                BUS.BUS_ChiTietHoaDon busCT = new BUS.BUS_ChiTietHoaDon();

                for (int i = 0; i < chiTietModel.getRowCount(); i++) {
                    Object val = chiTietModel.getValueAt(i, 0);
                    if (val != null && (boolean) val) {
                        String ten = chiTietModel.getValueAt(i, 1).toString().trim();
                        String dvt = chiTietModel.getValueAt(i, 2).toString().trim();
                        int sl = Integer.parseInt(chiTietModel.getValueAt(i, 3).toString());
                        String strGia = chiTietModel.getValueAt(i, 4).toString().replaceAll("[^0-9]", "");
                        double gia = Double.parseDouble(strGia);
                        busCT.themChiTietDoiTra(maPhieu, ten, dvt, sl, gia, "Hàng khách trả", true);
                    }
                }

                if (loai.equals("Đổi hàng") && spMoiModel.getRowCount() > 0) {
                    for (int i = 0; i < spMoiModel.getRowCount(); i++) {
                        String ten = spMoiModel.getValueAt(i, 0).toString().trim();
                        String dvt = spMoiModel.getValueAt(i, 1).toString().trim();
                        int sl = Integer.parseInt(spMoiModel.getValueAt(i, 3).toString()); 
                        String strGia = spMoiModel.getValueAt(i, 4).toString().replaceAll("[^0-9]", ""); 
                        
                        double gia = Double.parseDouble(strGia);
                        busCT.themChiTietDoiTra(maPhieu, ten, dvt, sl, gia, "Hàng khách đổi mới", false);
                    }
                }

                if (isEditMode) {
                    for (int i = 0; i < mainModel.getRowCount(); i++) {
                        if (mainModel.getValueAt(i, 0).toString().equals(maPhieuDangSua)) {
                            mainModel.setValueAt(trangThaiPhieu, i, 7); 
                            mainModel.setValueAt(formatGhiChu, i, 9);   
                            mainModel.setValueAt(colHoanTien, i, 5);    
                            mainModel.setValueAt(colChenhLech, i, 6);   
                            mainModel.setValueAt(lyDoFull, i, 4);       
                            break;
                        }
                    }
                } else {
                    mainModel.addRow(new Object[]{
                        maPhieu, maHDGoc, khach, loai, lyDoFull, colHoanTien, colChenhLech, trangThaiPhieu, ngayTao, formatGhiChu
                    });
                }
                
                isTaoThanhCong = true;
                maPhieuMoi = maPhieu;

                SwingUtilities.invokeLater(() -> {
                    Window owner = getOwner();
                    if (owner instanceof MainDashboard) {
                        MainDashboard md = (MainDashboard) owner;
                        md.lamMoiManHinhChinh();
                        md.lamMoiManHinhThongKe();   
                    }
                });

                // =========================================================
                // HIỆN THÔNG BÁO IN MÁY IN (NẾU HOÀN THÀNH) HOẶC ĐÓNG (NẾU LƯU NHÁP)
                // =========================================================
                if (!isLuuNhap) {
                    showCustomNotification("THÀNH CÔNG", "Đổi/Trả hàng thành công! Mã phiếu: " + maPhieu, "SUCCESS");
                    dispose();
                } else {
                    showCustomNotification("THÀNH CÔNG", "Đã lưu nháp phiếu thành công!", "SUCCESS");
                    dispose(); 
                }
                
            } else {
                showCustomNotification("LỖI CƠ SỞ DỮ LIỆU", "Lỗi! Không thể ghi nhận phiếu vào Database.", "ERROR");
            }
        }
    }

    class SpinnerRenderer extends JPanel implements TableCellRenderer {
        private JSpinner spinner;
        public SpinnerRenderer() {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(2, 5, 2, 5)); 
            setOpaque(true);
            spinner = new JSpinner();
            spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            add(spinner, BorderLayout.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            if (value != null) spinner.setValue(value);
            return this;
        }
    }

    class SpinnerEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel pnl;
        private JSpinner spinner;

        public SpinnerEditor() {
            pnl = new JPanel(new BorderLayout());
            pnl.setBorder(new EmptyBorder(2, 5, 2, 5));
            spinner = new JSpinner();
            spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            
            spinner.addChangeListener(e -> fireEditingStopped());
            pnl.add(spinner, BorderLayout.CENTER);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            pnl.setBackground(table.getSelectionBackground());
            
            int max = (int) table.getModel().getValueAt(row, 5);
            int currentVal = value != null ? (int) value : 1;
            
            spinner.setModel(new SpinnerNumberModel(currentVal, 1, max, 1));
            return pnl;
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }
    }
    
    private JPanel createPaymentSection() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10)); 
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(15, 0, 0, 0));
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 450)); 

        JPanel pnlHeader = new JPanel(new BorderLayout(0, 8));
        pnlHeader.setBackground(Color.WHITE);
        
        JLabel lblTitle = new JLabel("Phương thức thanh toán");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlBtns.setBackground(Color.WHITE);
        
        JButton btnTienMat = new JButton("Tiền mặt");
        JButton btnChuyenKhoan = new JButton("Chuyển khoản");
        btnTienMat.setPreferredSize(new Dimension(150, 40));
        btnChuyenKhoan.setPreferredSize(new Dimension(150, 40));
        
        stylePaymentBtn(btnTienMat, true);
        stylePaymentBtn(btnChuyenKhoan, false);
        
        pnlBtns.add(btnTienMat);
        pnlBtns.add(btnChuyenKhoan);
        
        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        pnlHeader.add(pnlBtns, BorderLayout.CENTER);

        JPanel pnlCards = new JPanel(new CardLayout());
        pnlCards.setBackground(Color.WHITE);
        
        pnlTienMatWrapper = createCashGridPanel();
        
        pnlTransferDetails = new JPanel(new BorderLayout(0, 10));
        pnlTransferDetails.setBackground(Color.WHITE);
        pnlTransferDetails.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.decode("#E2E8F0"), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel lblQRTriGia = new JLabel("Quét mã qua ứng dụng Ngân hàng", SwingConstants.CENTER);
        lblQRTriGia.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblQRTriGia.setForeground(Color.decode("#009A33"));

        lblQRCode = new JLabel("Vui lòng thêm sản phẩm!", SwingConstants.CENTER);
        lblQRCode.setPreferredSize(new Dimension(200, 200));
        lblQRCode.setBorder(BorderFactory.createLineBorder(Color.decode("#F3F4F6"), 2));

        lblQRAmount = new JLabel("Cần thanh toán: 0đ", SwingConstants.CENTER);
        lblQRAmount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblQRAmount.setForeground(Color.decode("#DC2626"));

        pnlTransferDetails.add(lblQRTriGia, BorderLayout.NORTH);
        pnlTransferDetails.add(lblQRCode, BorderLayout.CENTER);
        
        JPanel pnlTransferBot = new JPanel(new BorderLayout());
        pnlTransferBot.setOpaque(false);
        
        // --- BỔ SUNG NÚT IN QR MỚI ---
        JPanel pnlQRSouth = new JPanel(new BorderLayout(5, 0));
        pnlQRSouth.setOpaque(false);
        pnlQRSouth.add(lblQRAmount, BorderLayout.CENTER);

        JButton btnInQR = new JButton();
        btnInQR.setIcon(new MenuIcon("PRINT", 18)); // Sử dụng thư viện icon sẵn có
        btnInQR.setToolTipText("In mã QR thanh toán");
        btnInQR.setPreferredSize(new Dimension(40, 32));
        btnInQR.setBackground(Color.WHITE);
        btnInQR.setBorder(BorderFactory.createLineBorder(borderGray));
        btnInQR.setFocusPainted(false);
        btnInQR.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnInQR.setToolTipText("In mã QR thanh toán");
        btnInQR.addActionListener(ev -> {
            if (lblQRCode.getIcon() != null) thucHienInMaQRThanhToan();
            else showCustomNotification("Thông báo", "Mã QR chưa sẵn sàng để in!", "WARNING");
        });
        
        pnlQRSouth.add(btnInQR, BorderLayout.EAST);
        pnlTransferBot.add(pnlQRSouth, BorderLayout.NORTH);
        
        btnXacNhanCK = new JButton("Đã nhận tiền chuyển khoản");
        btnXacNhanCK.setBackground(Color.decode("#22C55E"));
        btnXacNhanCK.setForeground(Color.WHITE);
        btnXacNhanCK.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnXacNhanCK.setFocusPainted(false);
        btnXacNhanCK.setBorder(new EmptyBorder(8,0,8,0));
        btnXacNhanCK.addActionListener(e -> {
            if (lblTextSoTienHoan.getText().contains("Khách bù")) {
                isCKXacNhan = true;
                btnXacNhanCK.setText("✓ Đã xác nhận tiền");
                btnXacNhanCK.setBackground(Color.decode("#16A34A"));
                checkDieuKienThanhToan();
            }
        });
        
        pnlTransferBot.add(btnXacNhanCK, BorderLayout.SOUTH);
        pnlTransferDetails.add(pnlTransferBot, BorderLayout.SOUTH);
        
        pnlCards.add(pnlTienMatWrapper, "CASH");
        pnlCards.add(pnlTransferDetails, "BANK");
        
        CardLayout cl = (CardLayout) pnlCards.getLayout();

        btnTienMat.addActionListener(e -> {
            phuongThucDoiTra = "Tiền mặt";
            stylePaymentBtn(btnTienMat, true);
            stylePaymentBtn(btnChuyenKhoan, false);
            cl.show(pnlCards, "CASH"); 
            checkDieuKienThanhToan();
        });

        btnChuyenKhoan.addActionListener(e -> {
            phuongThucDoiTra = "Chuyển khoản";
            stylePaymentBtn(btnChuyenKhoan, true);
            stylePaymentBtn(btnTienMat, false);
            cl.show(pnlCards, "BANK"); 
            
            if (lblTextSoTienHoan != null && lblTextSoTienHoan.getText().contains("Khách bù")) {
                updateQRCode();
            } else { 
                lblQRCode.setIcon(null); 
                lblQRCode.setText("Hoàn tiền qua STK khách"); 
            }
            checkDieuKienThanhToan();
        });

        pnl.add(pnlHeader, BorderLayout.NORTH);
        pnl.add(pnlCards, BorderLayout.CENTER);
        
        return pnl;
    }

    private void stylePaymentBtn(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(Color.decode("#1967D2"));
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setBorder(new LineBorder(Color.decode("#1967D2"), 1, true));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.decode("#4B5563"));
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            btn.setBorder(new LineBorder(borderGray, 1, true));
        }
        btn.setFocusPainted(false);
    }

    private void updateQRCode() {
        if (lblQRCode == null || soTienThucTeCanXuLy <= 0) return;
        
        lblQRAmount.setText("Cần bù: " + String.format("%,d", soTienThucTeCanXuLy).replace(',', '.') + "đ");
        lblQRCode.setIcon(null);
        lblQRCode.setText("Đang tạo mã QR PayOS...");

        long finalTotalAmount = this.soTienThucTeCanXuLy;
        long orderCode = System.currentTimeMillis() / 1000;
        this.maGiaoDichHienTai = String.valueOf(orderCode); 
        String description = "Doi tra " + orderCode;

        SwingWorker<ImageIcon, Void> worker = new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                // =============== ĐIỀN MÃ CỦA BẠN VÀO ĐÂY ===============
                String clientId = "1afb5cca-f110-470d-a073-9f10bbfcd24b";
                String apiKey = "85b2b6fa-b442-4ea8-9800-9947e218417d";
                String checksumKey = "7a28013866c8bed8e8fd10557599cebf5ef61232a2e1f7b50637c24b7fd934de";
                // =========================================================

                String cancelUrl = "https://localhost";
                String returnUrl = "https://localhost";

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

                java.net.URL url = new java.net.URL("https://api-merchant.payos.vn/v2/payment-requests");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000); 
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
                
                // Backup mã offline nếu rớt mạng
                return generateQRCode("ThanhToan:" + finalTotalAmount + "|" + maGiaoDichHienTai, 200);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        lblQRCode.setText("");
                        lblQRCode.setIcon(icon);
                        
                        // Bắt đầu quét chờ tiền về
                        batDauQuetGiaoDichNganHang(maGiaoDichHienTai, finalTotalAmount);

                        
                        lblQRCode.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mouseClicked(java.awt.event.MouseEvent e) {
                                // Nếu click 2 lần (double click)
                                if (e.getClickCount() == 2) {
                                    // 1. Dừng việc quét API ngân hàng lại
                                    if (boKiemTraTienToi != null) boKiemTraTienToi.stop();
                                    
                                    // 2. Hiển thị thông báo thành công (Bạn có thể đổi lại chữ ở đây)
                                    showCustomNotification("TING TING", "Đã chốt phiếu đổi trả thủ công!", "SUCCESS");
                                    
                                    // 3. Ép cờ xác nhận tiền = true để vượt qua lớp bảo vệ
                                    isCKXacNhan = true;
                                    if (btnXacNhanCK != null) {
                                        btnXacNhanCK.setText("✓ Đã xác nhận tiền");
                                        btnXacNhanCK.setBackground(Color.decode("#16A34A"));
                                    }
                                    
                                    // 4. Gọi hàm chốt phiếu đổi trả (hàm này sẽ tự đóng cửa sổ)
                                    xuLyTaoPhieu(false);
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
    private void batDauQuetGiaoDichNganHang(String maGiaoDich, long soTienCanNhan) {
        if (boKiemTraTienToi != null && boKiemTraTienToi.isRunning()) {
            boKiemTraTienToi.stop();
        }

        boKiemTraTienToi = new javax.swing.Timer(3000, e -> {
            boolean daNhanTien = kiemTraLichSuGiaoDichTuAPI(maGiaoDich, soTienCanNhan);
            if (daNhanTien) {
                boKiemTraTienToi.stop(); 
                showCustomNotification("TING TING", "Đã nhận " + String.format("%,d", soTienCanNhan) + "đ\nHệ thống đang tự động chốt phiếu đổi trả...", "SUCCESS");
                
                // Ép xác nhận để bỏ qua bước nhân viên tự bấm nút xác nhận
                isCKXacNhan = true;
                if (btnXacNhanCK != null) {
                    btnXacNhanCK.setText("✓ Đã xác nhận tiền");
                    btnXacNhanCK.setBackground(Color.decode("#16A34A"));
                }
                
                // Chốt đơn Đổi/Trả
                xuLyTaoPhieu(false); 
            }
        });
        boKiemTraTienToi.start();
    }

    private boolean kiemTraLichSuGiaoDichTuAPI(String maGiaoDich, long soTien) {
        try {
            // =============== ĐIỀN LẠI MÃ CỦA BẠN VÀO ĐÂY ===============
            String clientId = "1afb5cca-f110-470d-a073-9f10bbfcd24b";
            String apiKey = "85b2b6fa-b442-4ea8-9800-9947e218417d";
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
                
                if (response.contains("\"status\":\"PAID\"")) {
                    return true;
                }
            }
        } catch (Exception ex) {
            System.err.println("Lỗi gọi API PayOS: " + ex.getMessage());
        }
        return false;
    }
    private JPanel createCashGridPanel() {
        listCounters.clear();
        listCountLabels.clear();
        
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(Color.decode("#FEFCE8")); 
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#FEF08A"), 1, true),
            new EmptyBorder(12, 12, 12, 12)
        ));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("💵 Tiền khách đưa thêm (tùy chọn)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(Color.decode("#9A3412"));

        JButton btnChon = new JButton("✓ Chọn mệnh giá");
        btnChon.setBackground(Color.decode("#F59E0B")); 
        btnChon.setForeground(Color.WHITE);
        btnChon.setFocusPainted(false);
        btnChon.setBorderPainted(false);
        btnChon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(btnChon, BorderLayout.EAST);

        JPanel pnlGrid = new JPanel(new GridLayout(3, 3, 10, 10)); 
        pnlGrid.setOpaque(false);
        pnlGrid.setBorder(new EmptyBorder(5, 0, 5, 0));
        
        pnlGrid.add(createMoneyCell("500.000đ", 500000, "#3B82F6")); 
        pnlGrid.add(createMoneyCell("200.000đ", 200000, "#A855F7")); 
        pnlGrid.add(createMoneyCell("100.000đ", 100000, "#22C55E")); 
        pnlGrid.add(createMoneyCell("50.000đ", 50000, "#EAB308"));  
        pnlGrid.add(createMoneyCell("20.000đ", 20000, "#F97316"));  
        pnlGrid.add(createMoneyCell("10.000đ", 10000, "#EF4444"));  
        pnlGrid.add(createMoneyCell("5.000đ", 5000, "#EC4899"));   
        pnlGrid.add(createMoneyCell("2.000đ", 2000, "#6B7280"));   
        pnlGrid.add(createMoneyCell("1.000đ", 1000, "#6B7280"));   

        JPanel pnlTotalBox = new JPanel(new BorderLayout());
        pnlTotalBox.setOpaque(false);
        pnlTotalBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#FDE047"), 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));
        JLabel lblTotalText = new JLabel("Tổng tiền mặt:");
        lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalText.setForeground(Color.decode("#9A3412"));
        lblTotalValue = new JLabel("0đ");
        lblTotalValue.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalValue.setForeground(Color.decode("#9A3412"));
        pnlTotalBox.add(lblTotalText, BorderLayout.WEST);
        pnlTotalBox.add(lblTotalValue, BorderLayout.EAST);

        JPanel pnlThuaBox = new JPanel(new BorderLayout());
        pnlThuaBox.setBackground(Color.decode("#DCFCE7"));
        pnlThuaBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#86EFAC"), 1, true),
            new EmptyBorder(10, 10, 10, 10)
        ));
        JLabel lblThuaText = new JLabel("Tiền thừa trả khách:");
        lblThuaText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblThuaText.setForeground(Color.decode("#065F46"));
        lblTienThuaValue = new JLabel("0đ");
        lblTienThuaValue.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTienThuaValue.setForeground(Color.decode("#047857"));
        pnlThuaBox.add(lblThuaText, BorderLayout.WEST);
        pnlThuaBox.add(lblTienThuaValue, BorderLayout.EAST);

        JPanel pnlFooters = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlFooters.setOpaque(false);
        pnlFooters.add(pnlTotalBox);
        pnlFooters.add(pnlThuaBox);

        pnl.add(pnlHeader, BorderLayout.NORTH);
        pnl.add(pnlGrid, BorderLayout.CENTER);
        pnl.add(pnlFooters, BorderLayout.SOUTH);

        return pnl;
    }

    private JPanel createMoneyCell(String labelText, long faceValue, String hexColor) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode(hexColor), 1, true),
            new EmptyBorder(6, 8, 6, 8) 
        ));

        JLabel lblValue = new JLabel(labelText);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblValue.setForeground(Color.decode(hexColor));

        JLabel lblCount = new JLabel("0", SwingConstants.CENTER); 
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCount.setForeground(Color.decode(hexColor));
        lblCount.setPreferredSize(new Dimension(20, 20));
        listCountLabels.add(lblCount);
        
        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        pnlControls.setOpaque(false);
        
        JLabel lblMinus = new JLabel(" - ");
        lblMinus.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMinus.setForeground(Color.decode(hexColor));
        lblMinus.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel lblPlus = new JLabel(" + ");
        lblPlus.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPlus.setForeground(Color.decode(hexColor));
        lblPlus.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnlControls.add(lblMinus);
        pnlControls.add(lblCount);
        pnlControls.add(lblPlus);

        final int[] count = {0}; 
        listCounters.add(count);

        lblPlus.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                count[0]++;
                lblCount.setText(String.valueOf(count[0]));
                tongTienMat += faceValue;
                capNhatTongTienMat();
            }
        });
        
        lblMinus.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (count[0] > 0) {
                    count[0]--;
                    lblCount.setText(String.valueOf(count[0]));
                    tongTienMat -= faceValue;
                    capNhatTongTienMat();
                }
            }
        });

        pnl.add(lblValue, BorderLayout.WEST);
        pnl.add(pnlControls, BorderLayout.EAST);
        
        return pnl;
    }

    private void capNhatTongTienMat() {
        if (lblTotalValue != null) {
            lblTotalValue.setText(String.format("%,d", tongTienMat).replace(',', '.') + "đ");
            
            if (lblTextSoTienHoan != null && lblTextSoTienHoan.getText().contains("Khách bù")) {
                // Ép dương soTienThucTeCanXuLy để tính toán an toàn
                long tienThua = tongTienMat - Math.abs(soTienThucTeCanXuLy);
                if (tienThua >= 0) {
                    lblTienThuaValue.setText(String.format("%,d", tienThua).replace(',', '.') + "đ");
                } else {
                    lblTienThuaValue.setText("Còn thiếu...");
                }
            } else {
                // Nếu là "Thối lại khách" -> Khách không cần trả tiền -> Không tính tiền thừa
                if (lblTienThuaValue != null) {
                    lblTienThuaValue.setText("---");
                }
            }
        }
    }
    private void showCustomNotification(String titleText, String message, String type) {
        JDialog dialog = new JDialog(this, true); 
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createLineBorder(Color.decode("#1E3A8A"), 2));
        pnlMain.setBackground(Color.WHITE);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.decode("#1E3A8A"));
        pnlHeader.setPreferredSize(new Dimension(0, 40));
        JLabel lblTitle = new JLabel(titleText.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.CENTER);

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
 // HÀM TẠO HỘP THOẠI XÁC NHẬN YES/NO (COPY TỪ TAOHOADON)
    private boolean showCustomConfirmDialog(String titleText, String message) {
        final boolean[] result = {false};
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
        pnlBody.setBorder(new EmptyBorder(20, 20, 20, 20)); 
        
        JLabel msg = new JLabel("<html><div style='text-align: center;'>" + message.replace("\n", "<br>") + "</div></html>", SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        msg.setForeground(Color.decode("#333333"));
        pnlBody.add(msg, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        pnlFooter.setBackground(Color.WHITE);
        
        JButton btnYes = new JButton("Đồng ý");
        btnYes.setPreferredSize(new Dimension(110, 38));
        btnYes.setBackground(Color.decode("#EF4444")); 
        btnYes.setForeground(Color.WHITE);
        btnYes.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnYes.setFocusPainted(false); btnYes.setBorderPainted(false);
        btnYes.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnYes.addActionListener(e -> { result[0] = true; dialog.dispose(); });

        JButton btnNo = new JButton("Hủy");
        btnNo.setPreferredSize(new Dimension(110, 38));
        btnNo.setBackground(Color.decode("#1E3A8A"));
        btnNo.setForeground(Color.WHITE);
        btnNo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNo.setFocusPainted(false); btnNo.setBorderPainted(false);
        btnNo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNo.addActionListener(e -> dialog.dispose());

        pnlFooter.add(btnYes); pnlFooter.add(btnNo);

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlBody, BorderLayout.CENTER);
        pnlMain.add(pnlFooter, BorderLayout.SOUTH);

        dialog.add(pnlMain);
        dialog.setSize(440, 260); 
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return result[0];
    }
    private long layTienMatTrongKetHienTai() {
        return busHD.layTienMatTrongKet();
    }
 // Nâng cấp Máy quét (hỗ trợ quét đổi hàng)
    private void setupGlobalBarcodeScanner() {
        scannerTimer = new Timer(200, e -> {
            if (scanBuffer.length() > 0) {
                String ketQuaQuet = scanBuffer.toString().trim();
                
                // Nếu đang dùng tab Đổi Hàng và không phải mã Hóa Đơn -> Đẩy sang quét sản phẩm mới
                if (!ketQuaQuet.startsWith("HD-") && btnDoiHang.getBackground().equals(Color.WHITE)) {
                    isSelectingProduct = true;
                    txtSearchNew.setText(ketQuaQuet);
                    txtSearchNew.setForeground(Color.BLACK);
                    isSelectingProduct = false;
                    scanBuffer.setLength(0);
                    
                    // Thực thi tìm kiếm bằng Barcode
                    new SwingWorker<java.util.List<Object[]>, Void>() {
                        @Override protected java.util.List<Object[]> doInBackground() throws Exception {
                            return new BUS.BUS_SanPham().timKiemSanPhamBan(ketQuaQuet); 
                        }
                        @Override protected void done() {
                            try {
                                java.util.List<Object[]> ketQua = get();
                                if (ketQua != null && !ketQua.isEmpty()) {
                                    // FIX: Tắt popup menu ngay lập tức
                                    if (suggestionMenu != null) suggestionMenu.setVisible(false);
                                    
                                    if (ketQua.size() == 1) {
                                        xyLyThemSanPhamNhanh(ketQua.get(0), null, txtSearchNew);
                                    } else {
                                        // FIX: Đẩy Dialog chọn lô ra hàng đợi để chống lỗi Z-Order Focus
                                        SwingUtilities.invokeLater(() -> {
                                            hienThiPopupChonLoKhiQuet(ketQua, null, txtSearchNew);
                                        });
                                    }
                                } else {
                                    showCustomNotification("KHÔNG TÌM THẤY", "Mã vạch không tồn tại!", "WARNING");
                                    txtSearchNew.setText("");
                                }
                            } catch (Exception ex) {}
                        }
                    }.execute();
                } else if (ketQuaQuet.startsWith("HD-")) {
                    // Xử lý quét hóa đơn gốc
                    isSelectingInvoice = true; 
                    txtSearch.setText(ketQuaQuet);
                    txtSearch.setForeground(Color.BLACK);
                    isSelectingInvoice = false;
                    scanBuffer.setLength(0);
                    SwingUtilities.invokeLater(() -> xuLyTimKiemHD());
                } else {
                    scanBuffer.setLength(0);
                }
            }
        });
        scannerTimer.setRepeats(false);

        scannerDispatcher = new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (!TaoPhieuDoiTra.this.isShowing() || !TaoPhieuDoiTra.this.isActive()) return false;

                // ==========================================
                // THÊM ĐOẠN CODE NÀY ĐỂ FIX LỖI NHẢY POPUP
                // Bỏ qua bộ quét ngầm nếu người dùng đang chủ động gõ chữ vào TextField
                Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (focusOwner instanceof JTextField) {
                    return false; 
                }
                // ==========================================

                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastKeyTime > 100) scanBuffer.setLength(0);
                    lastKeyTime = currentTime;

                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        // ... (Giữ nguyên phần code xử lý VK_ENTER) ...
                    } else {
                        char c = e.getKeyChar();
                        if (Character.isLetterOrDigit(c) || c == '-') {
                            scanBuffer.append(c);
                            scannerTimer.restart(); 
                        }
                    }
                }
                return false;
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(scannerDispatcher);
    }

    private void xyLyThemSanPhamNhanh(Object[] firstItem, JPopupMenu suggestionPopup, JTextField txtSearchProduct) {
        String name = firstItem[1].toString();
        String unit = firstItem[2] != null ? firstItem[2].toString() : "";
        long giaBan = Math.round(Double.parseDouble(firstItem[3].toString()));
        
        String thueVat = "5%";
        if (firstItem.length > 6 && firstItem[6] != null) {
            String rawVat = firstItem[6].toString().trim();
            try {
                double v = Double.parseDouble(rawVat);
                if (v > 0 && v < 1) v = v * 100; 
                thueVat = (int)v + "%";
            } catch (Exception ex) {}
        }

        // Lấy Lô và HSD
        String loHang = (firstItem.length > 7 && firstItem[7] != null) ? firstItem[7].toString() : "";
        String hsd = (firstItem.length > 8 && firstItem[8] != null) ? firstItem[8].toString() : "";
        String loHsdText = loHang;
        if (!loHsdText.isEmpty() && !hsd.isEmpty()) loHsdText += " — " + hsd;
        else if (loHsdText.isEmpty() && !hsd.isEmpty()) loHsdText = hsd;
        if (loHsdText.isEmpty()) loHsdText = "Chưa có lô";

        // CẢNH BÁO HẠN SỬ DỤNG
        if (hsd != null && !hsd.trim().isEmpty() && !hsd.equalsIgnoreCase("N/A")) {
            try {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                java.time.LocalDate expiryDate = java.time.LocalDate.parse(hsd.trim(), formatter);
                java.time.LocalDate today = java.time.LocalDate.now();
                
                if (expiryDate.isBefore(today.plusMonths(6))) {
                    String msgWarning = "Sản phẩm \"" + name + "\" gần hết hạn (HSD: " + hsd + "). Đổi cho khách?";
                    if (expiryDate.isBefore(today)) {
                        msgWarning = "Sản phẩm \"" + name + "\" ĐÃ HẾT HẠN (HSD: " + hsd + ")! Chắc chắn đổi?";
                    }
                    boolean tiepTuc = showCustomConfirmDialog("CẢNH BÁO", msgWarning);
                    if (!tiepTuc) return; 
                }
            } catch (Exception ex) {}
        }

        boolean daTonTai = false;
        int rowIndex = -1;
        int currentQty = 0;
        for (int i = 0; i < spMoiModel.getRowCount(); i++) {
            // Kiểm tra trùng Tên, ĐVT và CẢ LÔ HSD
            if (spMoiModel.getValueAt(i, 0).toString().equals(name) &&
                spMoiModel.getValueAt(i, 1).toString().equals(unit) &&
                spMoiModel.getValueAt(i, 2).toString().equals(loHsdText)) { 
                daTonTai = true;
                rowIndex = i;
                currentQty = Integer.parseInt(spMoiModel.getValueAt(i, 3).toString()); // SL ở cột 3
                break;
            }
        }

        isUpdatingCart = true; 
        try {
            if (daTonTai) {
                currentQty++; 
                spMoiModel.setValueAt(currentQty, rowIndex, 3); // Cột 3
                double vatPct = Double.parseDouble(thueVat.replace("%", ""));
                double thanhTien = currentQty * giaBan * (1.0 + vatPct / 100.0);
                spMoiModel.setValueAt(String.format("%,.0fđ", thanhTien), rowIndex, 6); // Cột 6
            } else {
                double vatPct = Double.parseDouble(thueVat.replace("%", ""));
                double thanhTien = giaBan * (1.0 + vatPct / 100.0);
                spMoiModel.addRow(new Object[]{
                    name, unit, loHsdText, 1, String.format("%,dđ", giaBan).replace(',', '.'), thueVat, String.format("%,.0fđ", thanhTien), ""
                }); // 8 Cột
            }
        } finally {
            isUpdatingCart = false; 
        }
            
        tinhTongTienSPMoi();
        if (suggestionPopup != null) suggestionPopup.setVisible(false); 
        if (txtSearchProduct != null) txtSearchProduct.setText(""); 
    }

    private void hienThiPopupChonLoKhiQuet(java.util.List<Object[]> danhSachLo, JPopupMenu suggestionPopup, JTextField txtSearchProduct) {
        // 1. Tắt bảng gợi ý ngay lập tức nếu nó đang mở
        if (suggestionPopup != null) {
            suggestionPopup.setVisible(false);
        }
        
        // 2. Tạo độ trễ 150 mili-giây để JPopupMenu biến mất hoàn toàn, trả lại Focus cho Form chính
        // Điều này ngăn chặn 100% hiện tượng chớp màn hình (Z-Order Conflict)
        javax.swing.Timer delayOpenDialog = new javax.swing.Timer(150, evt -> {
            JDialog dialog = new JDialog(this, "Hệ thống phát hiện sản phẩm có nhiều lô hàng", true);
            dialog.setSize(750, 400);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout(0, 10));
            dialog.getContentPane().setBackground(Color.WHITE);

            String tenSP = danhSachLo.get(0)[1] != null ? danhSachLo.get(0)[1].toString() : "Sản phẩm"; 

            JLabel lblTitle = new JLabel("<html><div style='text-align: left; padding: 2px 0px;'>"
                 + "<span style='font-size:16px;'>Sản phẩm: <b style='color:#1A73E8;'>" + tenSP + "</b> đang có nhiều lô.</span><br>"
                 + "<span style='font-size:13px; font-weight:normal; color:#DC2626;'>"
                 + "Chú ý: Ưu tiên bốc hộp thuốc có màu đỏ để tránh tồn kho!</span>"
                 + "</div></html>");

            lblTitle.setIcon(Utils.MenuIcon.IC_WARNING); 
            lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
            lblTitle.setIconTextGap(15);
            lblTitle.setBorder(new javax.swing.border.EmptyBorder(15, 10, 5, 10)); 

            dialog.add(lblTitle, BorderLayout.NORTH);

            String[] cols = {"Số Lô", "Hạn Sử Dụng", "Tồn", "Đơn Giá", "Chỉ dẫn đi lấy hàng", "Thao Tác"};
            DefaultTableModel modelLo = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };

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
                
                String chiDanViTri = (i == 0) ? "[!] TẠI QUẦY (Bốc hộp ngoài cùng)" : "[Kho] TRONG KHO (Hàng dự phòng)";
                modelLo.addRow(new Object[]{soLo, hsd, tonKho, gia, chiDanViTri, "CHỌN BÁN"});
            }

            JTable tblLo = new JTable(modelLo);
            tblLo.setRowHeight(45);
            tblLo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tblLo.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            tblLo.getTableHeader().setBackground(Color.decode("#F8F9FA"));
            tblLo.setShowGrid(true);
            tblLo.setGridColor(Color.decode("#F1F3F5"));

            tblLo.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                    if (row == 0) setForeground(Color.decode("#DC2626")); 
                    else setForeground(Color.decode("#2563EB")); 
                    return c;
                }
            });

            tblLo.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JButton btn = new JButton(value.toString());
                    btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    btn.setForeground(Color.WHITE);
                    btn.setBackground(row == 0 ? Color.decode("#10B981") : Color.decode("#9CA3AF"));
                    return btn;
                }
            });

            tblLo.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    int row = tblLo.rowAtPoint(e.getPoint());
                    int col = tblLo.columnAtPoint(e.getPoint());
                    if (row >= 0 && col == 5) {
                        Object[] loDuocChon = danhSachLo.get(row);
                        xyLyThemSanPhamNhanh(loDuocChon, suggestionPopup, txtSearchProduct);
                        dialog.dispose();
                    }
                }
            });

            tblLo.getColumnModel().getColumn(0).setPreferredWidth(90);
            tblLo.getColumnModel().getColumn(1).setPreferredWidth(100);
            tblLo.getColumnModel().getColumn(2).setPreferredWidth(50);
            tblLo.getColumnModel().getColumn(3).setPreferredWidth(90);
            tblLo.getColumnModel().getColumn(4).setPreferredWidth(230);
            tblLo.getColumnModel().getColumn(5).setPreferredWidth(100);

            dialog.add(new JScrollPane(tblLo), BorderLayout.CENTER);
            dialog.setVisible(true);
        });
        
        delayOpenDialog.setRepeats(false);
        delayOpenDialog.start();
    }

    // Helper (copy từ TaoHoaDon sang)
    private JPanel createSuggestionItem(JPopupMenu popup, JTextField txtSearch, String iconType, String name, String unit, String price, String stock, String danhMuc, String vat, java.util.List<Object[]> batches) {
        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(10, 15, 10, 15));
        pnl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel pnlLeft = new JPanel(new BorderLayout()); 
        pnlLeft.setOpaque(false);

        String badgeText = "Khác";
        Color bgColor = Color.decode("#F3F4F6"), fgColor = Color.decode("#4B5563"); 

        if (danhMuc != null) {
            String dm = danhMuc.toLowerCase();
            if (dm.contains("thuốc") || dm.contains("kê đơn")) {
                badgeText = "Thuốc KĐ"; bgColor = Color.decode("#DBEAFE"); fgColor = Color.decode("#2563EB"); 
            } else if (dm.contains("mỹ phẩm")) {
                badgeText = "Mỹ phẩm"; bgColor = Color.decode("#F3E8FF"); fgColor = Color.decode("#9333EA");
            } else if (dm.contains("chức năng") || dm.contains("tpcn")) {
                badgeText = "TPCN"; bgColor = Color.decode("#D1FAE5"); fgColor = Color.decode("#059669");
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
            BorderFactory.createLineBorder(bgColor, 1), BorderFactory.createEmptyBorder(2, 6, 2, 6)
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
        
        JComboBox<String> cboBatches = new JComboBox<>();
        cboBatches.setFont(new Font("Segoe UI", Font.PLAIN, 12)); 
        cboBatches.setPreferredSize(new Dimension(140, 26)); 
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

        cboBatches.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) { popup.putClientProperty("dont_close", Boolean.TRUE); }
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) { SwingUtilities.invokeLater(() -> popup.putClientProperty("dont_close", Boolean.FALSE)); }
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) { popup.putClientProperty("dont_close", Boolean.FALSE); }
        });

        java.awt.event.MouseAdapter stopBubble = new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) { e.consume(); }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { e.consume(); }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { e.consume(); }
        };
        cboBatches.addMouseListener(stopBubble);
        for (Component child : cboBatches.getComponents()) child.addMouseListener(stopBubble);

        pnlLeft.add(pnlNameBadge, BorderLayout.CENTER);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlRight.setOpaque(false);

        JLabel lblPrice = new JLabel(String.format("%,d", Integer.parseInt(price)).replace(',', '.') + "đ");
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPrice.setForeground(Color.decode("#111827"));

        JLabel lblStock = new JLabel("Tổng tồn: " + stock);
        lblStock.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStock.setForeground(Color.decode("#059669")); 

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
                Point mousePt = SwingUtilities.convertPoint(pnl, e.getPoint(), cboBatches);
                if (cboBatches.contains(mousePt) || cboBatches.isPopupVisible()) return; 

                if (SwingUtilities.isLeftMouseButton(e)) {
                    int selectedIndex = Math.max(0, cboBatches.getSelectedIndex());
                    Object[] selectedBatch = batches.get(selectedIndex);
                    
                    // 1. Tắt popup ngay lập tức
                    if (popup != null) popup.setVisible(false);
                    
                    // 2. Chuyển focus ra ngoài để Unikey xả bộ đệm
                    TaoPhieuDoiTra.this.requestFocusInWindow();
                    
                    // 3. Đợi 100ms rồi mới xử lý
                    javax.swing.Timer timerUnikey = new javax.swing.Timer(100, evt -> {
                        isSelectingProduct = true; // Khóa DocumentListener
                        
                        if (txtSearch != null) {
                            txtSearch.setText(""); 
                        }
                        
                        // FIX: Truyền đúng biến popup vào để đóng, và ép đóng luôn suggestionMenu tổng
                        xyLyThemSanPhamNhanh(selectedBatch, popup, txtSearch); 
                        if (suggestionMenu != null) suggestionMenu.setVisible(false);
                        
                        if (txtSearch != null) {
                            txtSearch.requestFocusInWindow(); 
                        }
                        
                        isSelectingProduct = false; // Mở lại DocumentListener
                    });
                    timerUnikey.setRepeats(false);
                    timerUnikey.start();
                }
            }
        });

        return pnl;
    }
    private void thucHienInMaQRThanhToan() {
        Icon icon = lblQRCode.getIcon();
        if (icon == null || !(icon instanceof ImageIcon)) {
            showCustomNotification("LỖI", "Không tìm thấy ảnh mã QR để in!", "ERROR");
            return;
        }

        java.awt.Image img = ((ImageIcon) icon).getImage();
        java.awt.image.BufferedImage finalImg = new java.awt.image.BufferedImage(
                img.getWidth(null), img.getHeight(null), java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D bGr = finalImg.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        String soTien = lblSoTienHoan.getText();
        String maGD = lblMaHoaDonInfo.getText();
        String tenCuaHang = "NHÀ THUỐC"; 

        JDialog dlg = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), "In / Xuất mã QR Thanh toán", true);
        dlg.setUndecorated(false);

        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(24, 32, 20, 32));

        JLabel lblTitle = new JLabel("MÃ THANH TOÁN QR", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.decode("#1E293B"));

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

        JLabel lblSo = new JLabel("Bù tiền Đổi/Trả cho Hóa Đơn: " + maGD, SwingConstants.CENTER);
        lblSo.setFont(new Font("Courier New", Font.BOLD, 14));
        lblSo.setForeground(Color.decode("#334155"));

        JLabel lblGia = new JLabel("Cần bù: " + soTien, SwingConstants.CENTER);
        lblGia.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblGia.setForeground(Color.decode("#E11D48"));

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
            fd.setFile("QR_BU_TIEN_" + maGD + ".png");
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
                    g2d.drawString("Cần bù: " + soTien, (300 - fm.stringWidth("Cần bù: " + soTien)) / 2, 350);

                    g2d.dispose();
                    javax.imageio.ImageIO.write(labelImg, "png", new java.io.File(filePath));
                    showCustomNotification("XUẤT ẢNH THÀNH CÔNG", "Đã lưu ảnh mã QR tại:\n" + filePath, "SUCCESS");
                } catch (Exception ex) {
                    showCustomNotification("LỖI", "Không lưu được file ảnh: " + ex.getMessage(), "ERROR");
                }
            }
        });
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
        btnDong.setBackground(Color.decode("#1E3A8A")); 
        btnDong.setForeground(Color.WHITE);
        btnDong.setBorderPainted(false);
        btnDong.setFocusPainted(false);
        btnDong.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDong.addActionListener(ev -> dlg.dispose());

        JPanel pnlBtn = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 0));
        pnlBtn.setBackground(Color.WHITE);
        pnlBtn.add(btnXuatAnh);
        pnlBtn.add(btnInTrucTiep); // <-- Thêm dòng này
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
    private String taoChuoiMaChuyenKhoan(long soTien, String maGiaoDich) {
        String nganHang = "MB"; // Đổi thành mã ngân hàng của bạn (VD: VCB, TCB, MB...)
        String soTaiKhoan = "0123456789"; // Đổi thành STK của bạn
        String tenTaiKhoan = "NGUYEN VAN A"; // Tên chủ thẻ
        
        // Cú pháp VietQR (gọi API trả về ảnh QR ngay lập tức)
        String url = String.format("https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s", 
                                   nganHang, soTaiKhoan, soTien, maGiaoDich, tenTaiKhoan.replace(" ", "%20"));
        return url;
    }

    // 2. Hàm hiển thị Dialog chứa Mã QR và Nút In
    private void hienThiDialogMaQRChuyenKhoan(long soTien) {
        JDialog dialog = new JDialog(this, "Mã QR Chuyển Khoản", true);
        dialog.setSize(420, 580);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        String maPhieu = "DTH-" + (System.currentTimeMillis() % 10000); 
        String urlQR = taoChuoiMaChuyenKhoan(soTien, maPhieu);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 10));
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setBorder(new javax.swing.border.EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("QUÉT MÃ ĐỂ THANH TOÁN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.decode("#1967D2"));
        pnlContent.add(lblTitle, BorderLayout.NORTH);

        JLabel lblQR = new JLabel();
        lblQR.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Tải ảnh từ API VietQR, nếu mất mạng thì sinh mã offline bằng ZXing
        try {
            java.net.URL url = new java.net.URL(urlQR);
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(url);
            if (img != null) {
                Image scaledImg = img.getScaledInstance(280, 280, Image.SCALE_SMOOTH);
                lblQR.setIcon(new ImageIcon(scaledImg));
            }
        } catch (Exception ex) {
            lblQR.setIcon(generateQRCode("ThanhToan:" + soTien + "|" + maPhieu, 280));
        }
        pnlContent.add(lblQR, BorderLayout.CENTER);

        JLabel lblInfo = new JLabel("<html><div style='text-align: center; line-height: 1.5;'>"
                + "Khách hàng cần thanh toán bù:<br>"
                + "<b style='color:#DC2626; font-size:20px;'>" + String.format("%,d", soTien).replace(',', '.') + "đ</b><br>"
                + "Nội dung CK: <b>" + maPhieu + "</b></div></html>", SwingConstants.CENTER);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        pnlContent.add(lblInfo, BorderLayout.SOUTH);

        dialog.add(pnlContent, BorderLayout.CENTER);

        // Nút In và Đóng
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        pnlBottom.setBackground(Color.WHITE);

        JButton btnPrint = new JButton("In Mã QR");
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPrint.setBackground(Color.decode("#10B981")); 
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFocusPainted(false);
        btnPrint.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPrint.setPreferredSize(new Dimension(130, 40));
        btnPrint.addActionListener(e -> inPanelMaQR(pnlContent));

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setBackground(Color.decode("#6B7280")); 
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setPreferredSize(new Dimension(100, 40));
        btnClose.addActionListener(e -> dialog.dispose());

        pnlBottom.add(btnPrint);
        pnlBottom.add(btnClose);

        dialog.add(pnlBottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // 3. Hàm tạo QR offline phòng hờ rớt mạng (Dùng ZXing)
    private ImageIcon generateQRCode(String data, int size) {
        try {
            QRCodeWriter barcodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = barcodeWriter.encode(data, BarcodeFormat.QR_CODE, size, size);
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_RGB);
            img.createGraphics();
            Graphics2D g = (Graphics2D) img.getGraphics();
            g.setColor(Color.WHITE); g.fillRect(0, 0, size, size);
            g.setColor(Color.BLACK);
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (bitMatrix.get(i, j)) g.fillRect(i, j, 1, 1);
                }
            }
            return new ImageIcon(img);
        } catch (Exception e) { return null; }
    }

    // 4. Hàm In Panel mã QR ra giấy máy in
    private void inPanelMaQR(JPanel panelToPrint) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                double widthScale = pageFormat.getImageableWidth() / panelToPrint.getWidth();
                g2d.scale(widthScale, widthScale);
                panelToPrint.printAll(g2d);
                return Printable.PAGE_EXISTS;
            }
        });
        if (job.printDialog()) {
            try { job.print(); } catch (PrinterException ex) { ex.printStackTrace(); }
        }
    }
    @Override
    public void dispose() {
        if (boKiemTraTienToi != null && boKiemTraTienToi.isRunning()) {
            boKiemTraTienToi.stop();
        }
        
        // --- THÊM ĐOẠN NÀY VÀO ĐỂ DIỆT TẬN GỐC LISTENER NGẦM ---
        if (scannerDispatcher != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(scannerDispatcher);
        }
        if (scannerTimer != null) scannerTimer.stop();
        if (dialogTimer != null) dialogTimer.stop();
        // -------------------------------------------------------

        super.dispose();
    }
 // FIX LỖI 4: HÀM TỰ ĐỘNG TICK CHỌN LẠI SẢN PHẨM ĐÃ LƯU NHÁP
 // FIX LỖI 4: HÀM TỰ ĐỘNG TICK CHỌN LẠI SẢN PHẨM ĐÃ LƯU NHÁP (ĐÃ CẬP NHẬT LẤY THUẾ THỰC TẾ)
    private void taiDuLieuBanNhap() {
        if (!isEditMode) return;
        BUS_TraHang busTra = new BUS_TraHang();
        
        // 1. Phục hồi tick chọn Sản phẩm Trả
        List<Object[]> dsTra = busTra.layChiTietPhieu(maPhieuDangSua);
        if (dsTra != null) {
            for (Object[] sp : dsTra) {
                String tenSP = sp[0] != null ? sp[0].toString().trim() : "";
                int sl = 1; 
                try { sl = Integer.parseInt(sp[1].toString().replaceAll("[^0-9]", "")); } catch(Exception e){}
                
                for (int i = 0; i < chiTietModel.getRowCount(); i++) {
                    if (chiTietModel.getValueAt(i, 1).toString().trim().equalsIgnoreCase(tenSP)) {
                        chiTietModel.setValueAt(true, i, 0); // Tự động tick chọn lại
                        chiTietModel.setValueAt(sl, i, 3);   // Set lại đúng số lượng
                        break;
                    }
                }
            }
        }

        // 2. Phục hồi Sản phẩm mới (nếu đang ở tab Đổi hàng)
        if (btnDoiHang.getBackground().equals(Color.WHITE)) {
            List<Object[]> dsDoi = busTra.layDanhSachSanPhamDoi(maPhieuDangSua);
            if (dsDoi != null) {
                BUS.BUS_SanPham busSP = new BUS.BUS_SanPham(); // Gọi BUS để lấy thuế thật
                
                for (Object[] sp : dsDoi) {
                    String tenSP = sp[0] != null ? sp[0].toString().trim() : "";
                    String dvt = sp[2] != null ? sp[2].toString().trim() : "Hộp";
                    int sl = 1; 
                    try { sl = Integer.parseInt(sp[1].toString().replaceAll("[^0-9]", "")); } catch(Exception e){}
                    long gia = 0;
                    try { gia = Long.parseLong(sp[3].toString().replaceAll("[,.]", "").replaceAll("[^0-9]", "")); } catch(Exception e){}

                    // --- BẮT ĐẦU ĐOẠN SỬA THUẾ VAT ---
                    double thueVatThucTe = busSP.layThueVATTheoTenSP(tenSP);
                    if (thueVatThucTe > 0 && thueVatThucTe < 1) {
                        thueVatThucTe = thueVatThucTe * 100; // Đề phòng DB lưu số thập phân (VD: 0.05 thay vì 5)
                    }
                    
                    String thueVatStr = (int)thueVatThucTe + "%";
                    double thanhTien = sl * gia * (1.0 + (thueVatThucTe / 100.0));
                    // --- KẾT THÚC ĐOẠN SỬA ---

                    spMoiModel.addRow(new Object[]{
                        tenSP, dvt, "Chưa có lô", sl, String.format("%,dđ", gia).replace(',', '.'), thueVatStr, String.format("%,.0fđ", thanhTien), ""
                    });
                }
                tinhTongTienSPMoi(); // Gọi hàm tính lại bảng dưới
            }
        }
        
        // Gọi hàm kích hoạt tính toán tổng tiền
        capNhatDieuKienDoiTra();
    }
 
 // ==========================================
    // CLASS HỖ TRỢ CHỈNH SỐ LƯỢNG CHO BẢNG SP MỚI
    // ==========================================
    class SpinnerEditorSPMoi extends AbstractCellEditor implements TableCellEditor {
        private JPanel pnl;
        private JSpinner spinner;

        public SpinnerEditorSPMoi() {
            pnl = new JPanel(new BorderLayout());
            pnl.setBorder(new EmptyBorder(2, 5, 2, 5));
            spinner = new JSpinner();
            spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            spinner.addChangeListener(e -> fireEditingStopped());
            pnl.add(spinner, BorderLayout.CENTER);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            pnl.setBackground(table.getSelectionBackground());
            int currentVal = 1;
            try { currentVal = Integer.parseInt(value.toString()); } catch (Exception e) {}
            
            // Cho phép nhập số lượng từ 1 đến 9999
            spinner.setModel(new SpinnerNumberModel(currentVal, 1, 9999, 1));
            return pnl;
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }
    }
}