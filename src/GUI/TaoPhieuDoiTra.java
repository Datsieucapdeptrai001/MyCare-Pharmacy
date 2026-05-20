package GUI;
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
    private JButton btnTraHang, btnDoiHang, btnTaoPhieu;
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

        btnTaoPhieu = new JButton("Xác nhận trả hàng");
        btnTaoPhieu.setIcon(new MenuIcon("CHECK_CIRCLE", 20));
        btnTaoPhieu.setPreferredSize(new Dimension(200, 40));
        btnTaoPhieu.setBackground(primaryRed);
        btnTaoPhieu.setForeground(Color.WHITE);
        btnTaoPhieu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTaoPhieu.setBorderPainted(false);
        btnTaoPhieu.setFocusPainted(false);
        btnTaoPhieu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTaoPhieu.setEnabled(false); 
        btnTaoPhieu.addActionListener(e -> xuLyTaoPhieu());

        pnlFooter.add(btnHuy, BorderLayout.WEST);
        pnlFooter.add(btnTaoPhieu, BorderLayout.EAST);
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

        // Cột: 0=Tên, 1=ĐVT, 2=SL, 3=Đơn giá (chưa VAT), 4=VAT%, 5=Thành tiền (đã gộp VAT)
        spMoiModel = new DefaultTableModel(new String[]{"Sản phẩm mới", "ĐVT", "SL", "Đơn giá", "VAT", "Thành tiền"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 1 || column == 2; } 
        };
        
        spMoiModel.addTableModelListener(e -> {
            if (isUpdatingCart) return;
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();

                if (col == 1) {
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
                                int sl = Integer.parseInt(spMoiModel.getValueAt(row, 2).toString());
                                double thanhTien = sl * giaChuaVAT * (1.0 + thueVAT / 100.0);
                                // col 3 = Đơn giá (chưa VAT), col 4 = VAT%, col 5 = Thành tiền (đã gộp VAT)
                                spMoiModel.setValueAt(String.format("%,.0fđ", giaChuaVAT), row, 3);
                                spMoiModel.setValueAt(String.format("%.0f%%", thueVAT), row, 4);
                                spMoiModel.setValueAt(String.format("%,.0fđ", thanhTien), row, 5);
                            }
                        } catch (Exception ex) {}
                        finally {
                            isUpdatingCart = false;
                            tinhTongTienSPMoi();
                        }
                    });
                } 
                else if (col == 2) {
                    try {
                        int sl = Integer.parseInt(spMoiModel.getValueAt(row, 2).toString());
                        if (sl < 1) sl = 1;
                        double donGia = Double.parseDouble(spMoiModel.getValueAt(row, 3).toString().replaceAll("[^0-9]", ""));
                        double vatPct = 0;
                        try {
                            String vatStr = spMoiModel.getValueAt(row, 4).toString().replace("%", "").trim();
                            vatPct = Double.parseDouble(vatStr);
                        } catch (Exception ignored) {}
                        double thanhTien = sl * donGia * (1.0 + vatPct / 100.0);
                        
                        isUpdatingCart = true;
                        spMoiModel.setValueAt(sl, row, 2);
                        spMoiModel.setValueAt(String.format("%,.0fđ", thanhTien), row, 5);
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
        btnTim.setPreferredSize(new Dimension(120, 40)); 
        btnTim.setBackground(Color.decode("#1E293B"));
        btnTim.setForeground(Color.WHITE);
        btnTim.setFont(new Font("Segoe UI", Font.BOLD, 14));
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
    private void timKiemHoaDonLive() {
        // FIX: Nếu đang dùng code để gán text thì bỏ qua, không tìm kiếm nữa
        if (isSelectingInvoice) return; 

        String kw = txtSearch.getText().trim();
        if (kw.isEmpty() || kw.equals("Nhập mã hóa đơn (VD: HD-2024-0001)...")) {
            SwingUtilities.invokeLater(() -> suggestionInvoiceMenu.setVisible(false));
            return;
        }

        new Thread(() -> {
            java.util.List<String> dsGoiY = busHD.timKiemMaHoaDonGoiY(kw);

            SwingUtilities.invokeLater(() -> {
                if (!txtSearch.getText().trim().equals(kw)) return; 

                if (!dsGoiY.isEmpty()) {
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

                        // FIX LỖI "effectively final": Tạo một biến final để truyền an toàn vào Timer
                        final String finalIdHD = idHD;

                        pnlItem.addMouseListener(new java.awt.event.MouseAdapter() {
                            public void mouseEntered(java.awt.event.MouseEvent evt) { pnlItem.setBackground(Color.decode("#F8FAFC")); }
                            public void mouseExited(java.awt.event.MouseEvent evt) { pnlItem.setBackground(Color.WHITE); }
                            
                            public void mousePressed(java.awt.event.MouseEvent evt) {
                                suggestionInvoiceMenu.setVisible(false);
                                
                                // FIX NHẢY CHỮ: Chuyển focus ra khỏi ô tìm kiếm để Unikey nhả bộ đệm
                                TaoPhieuDoiTra.this.requestFocusInWindow();
                                
                                // Chờ 50ms để Unikey xả sạch chữ thừa giống hệt bên tìm sản phẩm
                                javax.swing.Timer timerUnikey = new javax.swing.Timer(50, e -> {
                                    isSelectingInvoice = true; 
                                    
                                    // SỬ DỤNG BIẾN finalIdHD TẠI ĐÂY
                                    txtSearch.setText(finalIdHD); 
                                    txtSearch.setForeground(Color.BLACK);
                                    isSelectingInvoice = false;
                                    
                                    SwingUtilities.invokeLater(() -> xuLyTimKiemHD()); 
                                    
                                    // Trả lại focus
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
                    if (!suggestionInvoiceMenu.isVisible()) suggestionInvoiceMenu.show(txtSearch, 0, txtSearch.getHeight());
                } else {
                    suggestionInvoiceMenu.setVisible(false);
                }
            });
        }).start();
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

        txtSearchNew.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtSearchNew.getText().equals("Tìm sản phẩm thay thế...")) {
                    txtSearchNew.setText(""); txtSearchNew.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtSearchNew.getText().isEmpty()) {
                    txtSearchNew.setForeground(Color.GRAY); txtSearchNew.setText("Tìm sản phẩm thay thế...");
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
        tableSPMoi.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // SL
        tableSPMoi.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // VAT%
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tableSPMoi.getColumnModel().getColumn(3).setCellRenderer(rightRenderer); // Đơn giá
        tableSPMoi.getColumnModel().getColumn(5).setCellRenderer(rightRenderer); // Thành tiền

        tableSPMoi.getColumnModel().getColumn(0).setPreferredWidth(160); // Tên SP
        tableSPMoi.getColumnModel().getColumn(1).setPreferredWidth(60);  // ĐVT
        tableSPMoi.getColumnModel().getColumn(2).setPreferredWidth(35);  // SL
        tableSPMoi.getColumnModel().getColumn(3).setPreferredWidth(85);  // Đơn giá
        tableSPMoi.getColumnModel().getColumn(4).setPreferredWidth(45);  // VAT%
        tableSPMoi.getColumnModel().getColumn(5).setPreferredWidth(85);  // Thành tiền
        
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
                setSize(850, 330); 
                setLocationRelativeTo(getOwner()); 
            }
            if (btnTaoPhieu != null) btnTaoPhieu.setEnabled(false); 
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
            lblError.setText(" Hóa đơn này đã được " + loaiPhieuCu + " trước đó (Mã: " + maPhieuCu + ")!"); 
            lblError.setVisible(true);
            if (pnlFoundData != null && pnlFoundData.isVisible()) { pnlFoundData.setVisible(false); setSize(850, 330); setLocationRelativeTo(getOwner()); }
            if (btnTaoPhieu != null) btnTaoPhieu.setEnabled(false); 
            return;
        }

        boolean isHopLe = busTraHang.kiemTraDieuKien(maHD);
        if (!isHopLe) {
            lblError.setText(" Hóa đơn đã quá hạn đổi trả hoặc không hợp lệ!"); lblError.setVisible(true);
            if (pnlFoundData != null && pnlFoundData.isVisible()) { pnlFoundData.setVisible(false); setSize(850, 330); setLocationRelativeTo(getOwner()); }
            if (btnTaoPhieu != null) btnTaoPhieu.setEnabled(false); 
            return;
        }

        ngayHoaDonGoc = hd.getNgayLapHD(); 
        chiTietModel.setRowCount(0); 
        tongTienGoc = 0; 
        
        BUS_ChiTietHoaDon busCTHD = new BUS_ChiTietHoaDon(); 
        List<Object[]> dsChiTiet = busCTHD.layDuLieuDoiTra(hd.getId()); 

        if (dsChiTiet != null && !dsChiTiet.isEmpty()) {
            java.util.Set<String> addedProducts = new java.util.HashSet<>();
            DAO.DAO_HoaDon daoHD = new DAO.DAO_HoaDon(); 

            for (Object[] rowData : dsChiTiet) {
                String tenSP = rowData[0].toString();
                int slMua = Integer.parseInt(rowData[2].toString());
                
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
                    try { donGiaNum = Long.parseLong(rowData[3].toString().replaceAll("[^0-9]", "")); } catch(Exception e){}
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
        if (btnTaoPhieu != null) btnTaoPhieu.setEnabled(true);
        setSize(1050, 720); setLocationRelativeTo(getOwner());
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
                btnTaoPhieu.setEnabled(true);
            } else {
                btnTaoPhieu.setEnabled(false);
            }
        }
    }
    private void setupGlobalBarcodeScanner() {
        // Khởi tạo Timer: Tự động "nhấn Enter" sau khi máy quét dừng truyền chữ 200ms
        scannerTimer = new Timer(200, e -> {
            if (scanBuffer.length() > 0) {
                String ketQuaQuet = scanBuffer.toString().trim();
                if (ketQuaQuet.startsWith("HD-")) {
                    isSelectingInvoice = true; // Khóa popup gợi ý để không bị che mất giao diện
                    txtSearch.setText(ketQuaQuet);
                    txtSearch.setForeground(Color.BLACK);
                    isSelectingInvoice = false;
                    
                    scanBuffer.setLength(0);
                    SwingUtilities.invokeLater(() -> xuLyTimKiemHD()); // Tự động chạy lệnh tìm kiếm
                } else {
                    scanBuffer.setLength(0);
                }
            }
        });
        scannerTimer.setRepeats(false); // Chỉ đếm 1 lần rồi dừng

        scannerDispatcher = new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (!TaoPhieuDoiTra.this.isShowing() || !TaoPhieuDoiTra.this.isActive()) {
                    return false;
                }

                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    long currentTime = System.currentTimeMillis();
                    
                    // Nếu thời gian gõ tay > 100ms -> Là người đang gõ bàn phím -> Xóa bộ đệm
                    if (currentTime - lastKeyTime > 100) {
                        scanBuffer.setLength(0);
                    }
                    lastKeyTime = currentTime;

                    // Nếu app điện thoại có gửi phím Enter
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        scannerTimer.stop(); // Hủy đồng hồ chờ tự động
                        if (scanBuffer.length() > 0) {
                            String ketQuaQuet = scanBuffer.toString().trim();
                            if (ketQuaQuet.startsWith("HD-")) {
                                isSelectingInvoice = true;
                                txtSearch.setText(ketQuaQuet);
                                txtSearch.setForeground(Color.BLACK);
                                isSelectingInvoice = false;
                                
                                scanBuffer.setLength(0);
                                SwingUtilities.invokeLater(() -> xuLyTimKiemHD()); // Tự động tìm kiếm
                                return true; // Chặn phím Enter để không bấm nhầm nút khác trên Form
                            }
                            scanBuffer.setLength(0);
                        }
                    } else {
                        char c = e.getKeyChar();
                        // Chỉ lưu các ký tự là chữ, số hoặc dấu gạch ngang
                        if (Character.isLetterOrDigit(c) || c == '-') {
                            scanBuffer.append(c);
                            scannerTimer.restart(); // Mỗi lần quét 1 chữ, gia hạn thêm 200ms. Hết 200ms tự search!
                        }
                    }
                }
                return false;
            }
        };

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(scannerDispatcher);

        // Hủy bộ lắng nghe và Timer khi đóng Form để giải phóng RAM
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(scannerDispatcher);
                if (scannerTimer != null) {
                    scannerTimer.stop();
                }
            }
        });
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
            if(btnTaoPhieu != null) btnTaoPhieu.setEnabled(true); 
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
            
            btnTaoPhieu.setEnabled(true);
        } else {
            lblBadgeHoanTien.setText("Đang tính chênh lệch..."); 
            lblBadgeHoanTien.setForeground(primaryBlue); 
            lblBadgeHoanTien.setBackground(Color.decode("#DBEAFE"));
            
            if (spMoiModel.getRowCount() == 0) {
                lblSoTienHoan.setText("0đ"); 
                lblTextSoTienHoan.setText("Vui lòng thêm SP mới");
                this.soTienThucTeCanXuLy = 0; 
                
                if (btnTaoPhieu != null) btnTaoPhieu.setEnabled(false);
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
                    btnTaoPhieu.setEnabled(true);
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
        
        int colThanhTien = spMoiModel.getColumnCount() - 1;
        
        for (int i = 0; i < spMoiModel.getRowCount(); i++) {
            giaMoi += Double.parseDouble(spMoiModel.getValueAt(i, colThanhTien).toString().replaceAll("[^0-9]", ""));
            tenMoi += spMoiModel.getValueAt(i, 0).toString() + ", ";
        }
        if(tenMoi.endsWith(", ")) tenMoi = tenMoi.substring(0, tenMoi.length() - 2);
        
        SwingUtilities.invokeLater(() -> {
            if (scrollSPMoi != null && tableSPMoi != null) {
                if (spMoiModel.getRowCount() > 0) {
                    scrollSPMoi.setVisible(true);
                    
                    int headerHeight = tableSPMoi.getTableHeader() != null ? tableSPMoi.getTableHeader().getPreferredSize().height : 0;
                    int rowHeight = tableSPMoi.getRowHeight();
                    // ĐÃ FIX 2: Set chặt chiều cao tối thiểu và tối đa bằng nhau để bảng luôn giãn hết cỡ
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
    
    private void timKiemLive() {
        // 1. Chặn tìm kiếm khi đang dùng lệnh setText("") để tránh vòng lặp vô hạn
        if (isSelectingProduct) return;

        String kw = txtSearchNew.getText().trim();
        
        // 2. Nếu trống hoặc là placeholder thì ẩn menu gợi ý ngay
        if (kw.isEmpty() || kw.equals("Tìm sản phẩm thay thế...")) { 
            SwingUtilities.invokeLater(() -> suggestionMenu.setVisible(false)); 
            return; 
        }

        // 3. Đưa thao tác truy vấn Database/BUS vào luồng ngầm (Thread) để không làm đơ UI khi gõ
        new Thread(() -> {
            BUS_SanPham busSP = new BUS_SanPham();
            List<SanPham> dsGoiY = busSP.traCuuSanPham(kw); 

            // 4. Cập nhật giao diện phải quay lại luồng EDT (SwingUtilities.invokeLater)
            SwingUtilities.invokeLater(() -> {
                // Kiểm tra xem chữ trong ô search còn khớp với từ khóa tìm kiếm không 
                // (Phòng trường hợp kết quả trả về chậm khi người dùng đã gõ sang chữ khác)
                if (!txtSearchNew.getText().trim().equals(kw)) return;

                if (dsGoiY != null && !dsGoiY.isEmpty()) {
                    // Nếu người dùng gõ đúng y hệt tên sản phẩm thì không cần hiện gợi ý nữa
                    for (SanPham sp : dsGoiY) { 
                        if (sp.getTen().equalsIgnoreCase(kw)) { 
                            suggestionMenu.setVisible(false); 
                            return; 
                        } 
                    }

                    suggestionMenu.removeAll(); 
                    JPanel pnlList = new JPanel(); 
                    pnlList.setLayout(new BoxLayout(pnlList, BoxLayout.Y_AXIS)); 
                    pnlList.setBackground(Color.WHITE);

                    for (SanPham sp : dsGoiY) {
                        // Tạo Panel cho từng dòng sản phẩm gợi ý
                        JPanel pnlItem = new JPanel(new BorderLayout(15, 5)); 
                        pnlItem.setBackground(Color.WHITE);
                        pnlItem.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E5E7EB")), 
                            new EmptyBorder(8, 12, 8, 12)
                        ));
                        pnlItem.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
                        pnlItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55)); 

                        // Phần thông tin Tên và Mã
                        JPanel pnlInfo = new JPanel(new BorderLayout(0, 3)); 
                        pnlInfo.setOpaque(false); 
                        JLabel lblTen = new JLabel(sp.getTen()); 
                        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        
                        JPanel pnlSub = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); 
                        pnlSub.setOpaque(false);
                        JLabel lblMa = new JLabel(sp.getId()); 
                        lblMa.setFont(new Font("Segoe UI", Font.PLAIN, 13)); 
                        lblMa.setForeground(Color.decode("#6B7280")); 
                        
                        // Xử lý màu sắc cho Tag danh mục
                        String tenDanhMuc = "Khác"; 
                        Color bgTag = Color.decode("#F3F4F6"), fgTag = Color.decode("#4B5563"); 
                        if (sp.getDanhMuc() != null) {
                            switch (sp.getDanhMuc()) {
                                case THUOC_KE_DON: 
                                    tenDanhMuc = "Thuốc kê đơn"; bgTag = Color.decode("#FEE2E2"); fgTag = Color.decode("#DC2626"); break;
                                case THUOC_KHONG_KE_DON: 
                                    tenDanhMuc = "Không kê đơn"; bgTag = Color.decode("#DCFCE7"); fgTag = Color.decode("#16A34A"); break;
                                case THUC_PHAM_CHUC_NANG: 
                                    tenDanhMuc = "Thực phẩm chức năng"; bgTag = Color.decode("#DBEAFE"); fgTag = Color.decode("#1D4ED8"); break;
                                case MY_PHAM: 
                                    tenDanhMuc = "Mỹ phẩm"; bgTag = Color.decode("#FCE7F3"); fgTag = Color.decode("#BE185D"); break;
                            }
                        }

                        final Color finalBgTag = bgTag;
                        JLabel lblTag = new JLabel(tenDanhMuc) {
                            @Override
                            protected void paintComponent(Graphics g) {
                                Graphics2D g2 = (Graphics2D) g.create(); 
                                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                g2.setColor(finalBgTag); 
                                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); 
                                super.paintComponent(g); 
                                g2.dispose();
                            }
                        };
                        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 10)); 
                        lblTag.setForeground(fgTag); 
                        lblTag.setOpaque(false); 
                        lblTag.setBorder(new EmptyBorder(1, 6, 1, 6)); 
                        
                        pnlSub.add(lblMa); 
                        pnlSub.add(Box.createRigidArea(new Dimension(8, 0))); 
                        pnlSub.add(lblTag);
                        pnlInfo.add(lblTen, BorderLayout.NORTH); 
                        pnlInfo.add(pnlSub, BorderLayout.CENTER);

                        // Lấy giá bán và VAT
                        BUS_DonViDoLuong busDVDL = new BUS_DonViDoLuong(); 
                        List<DonViDoLuong> dsDVT = busDVDL.getDSTheoMaSP(sp.getId());
                        double gia = 0; String dvt = "Hộp"; 
                        if (dsDVT != null && !dsDVT.isEmpty()) { 
                            gia = dsDVT.get(0).getGia(); 
                            dvt = dsDVT.get(0).getTen(); 
                        }
                        // Lấy VAT của sản phẩm qua BUS
                        double vatSP = 0;
                        BUS_SanPham busSPVAT = new BUS_SanPham();
                        SanPham spFull = busSPVAT.getSanPhamDayDu(sp.getId());
                        if (spFull != null) {
                            vatSP = spFull.getThueVAT();
                            if (vatSP > 0 && vatSP < 1) vatSP = vatSP * 100;
                        }
                        
                        final double finalGia = gia; 
                        final String finalDvt = dvt;
                        final double finalVat = vatSP;

                        JLabel lblGia = new JLabel(String.format("%,.0fđ / %s", gia, dvt)); 
                        lblGia.setFont(new Font("Segoe UI", Font.BOLD, 13)); 
                        lblGia.setForeground(Color.decode("#1967D2")); 
                        
                        pnlItem.add(pnlInfo, BorderLayout.CENTER); 
                        pnlItem.add(lblGia, BorderLayout.EAST);

                     // SỰ KIỆN CLICK CHỌN SẢN PHẨM MỚI (ĐÃ FIX LỖI COPY NHẦM)
                        final String tenSP = sp.getTen();
                        final String dvtChuan = finalDvt;
                        final double giaChuan = finalGia;
                        final double vatChuan = finalVat;

                        pnlItem.addMouseListener(new java.awt.event.MouseAdapter() {
                            public void mouseEntered(java.awt.event.MouseEvent evt) { pnlItem.setBackground(Color.decode("#F8FAFC")); }
                            public void mouseExited(java.awt.event.MouseEvent evt) { pnlItem.setBackground(Color.WHITE); }
                            
                            public void mousePressed(java.awt.event.MouseEvent evt) {
                                suggestionMenu.setVisible(false);
                                
                                // FIX NHẢY CHỮ: Chuyển focus ra khỏi ô tìm kiếm để Unikey nhả bộ đệm
                                TaoPhieuDoiTra.this.requestFocusInWindow();
                                
                                javax.swing.Timer timerUnikey = new javax.swing.Timer(50, e -> {
                                    isSelectingProduct = true; 
                                    
                                    // 1. Kiểm tra xem sản phẩm đã có trong bảng giỏ hàng chưa
                                    boolean tonTai = false;
                                    for (int i = 0; i < spMoiModel.getRowCount(); i++) {
                                        if (spMoiModel.getValueAt(i, 0).toString().equals(tenSP) && 
                                            spMoiModel.getValueAt(i, 1).toString().equals(dvtChuan)) {
                                            int slCu = Integer.parseInt(spMoiModel.getValueAt(i, 2).toString());
                                            spMoiModel.setValueAt(slCu + 1, i, 2); // Tăng số lượng lên 1
                                            tonTai = true;
                                            break;
                                        }
                                    }
                                    
                                    // 2. Nếu chưa có thì thêm dòng sản phẩm mới vào bảng (6 cột)
                                    if (!tonTai) {
                                        double thanhTienMoi = giaChuan * (1.0 + vatChuan / 100.0);
                                        spMoiModel.addRow(new Object[]{
                                            tenSP, 
                                            dvtChuan, 
                                            1, 
                                            String.format("%,.0fđ", giaChuan),   // Đơn giá (chưa VAT)
                                            String.format("%.0f%%", vatChuan),   // VAT%
                                            String.format("%,.0fđ", thanhTienMoi) // Thành tiền (đã gộp VAT)
                                        });
                                    }
                                    
                                    // 3. Xóa text đã nhập, đưa ô tìm kiếm về trạng thái ban đầu
                                    txtSearchNew.setText("Tìm sản phẩm thay thế..."); 
                                    txtSearchNew.setForeground(Color.GRAY);
                                    isSelectingProduct = false;
                                    
                                    // 4. Tính toán lại tổng tiền chênh lệch của hóa đơn
                                    SwingUtilities.invokeLater(() -> tinhTongTienSPMoi()); 
                                    
                                    txtSearchNew.requestFocusInWindow();
                                });
                                timerUnikey.setRepeats(false);
                                timerUnikey.start();
                            }
                        });
                        pnlList.add(pnlItem);
                    }
                    
                    // Cấu hình JScrollPane cho Menu gợi ý
                    JScrollPane scrollPane = new JScrollPane(pnlList); 
                    scrollPane.setBorder(null); 
                    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); 
                    scrollPane.setPreferredSize(new Dimension(txtSearchNew.getWidth(), Math.min(pnlList.getPreferredSize().height, 250)));
                    scrollPane.getVerticalScrollBar().setUI(new Utils.ModernScrollBarUI()); 
                    scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0)); 
                    
                    suggestionMenu.removeAll();
                    suggestionMenu.add(scrollPane); 
                    suggestionMenu.pack(); 
                    if (!suggestionMenu.isVisible()) {
                        suggestionMenu.show(txtSearchNew, 0, txtSearchNew.getHeight());
                    }
                } else {
                    suggestionMenu.setVisible(false);
                }
            });
        }).start();
    }

    private void xuLyTaoPhieu() {
    	if (ngayHoaDonGoc == null || txtSearch.getText().contains("Nhập mã hóa đơn")) {
            showCustomNotification("CẢNH BÁO", "Vui lòng tìm kiếm hóa đơn hợp lệ trước khi tạo phiếu!", "WARNING"); return;
        }

        String loai = btnTraHang.getBackground().equals(Color.WHITE) ? "Trả hàng" : "Đổi hàng";
        if (loai.equals("Đổi hàng") && spMoiModel.getRowCount() == 0) {
            showCustomNotification("CẢNH BÁO", "Vui lòng tìm và chọn sản phẩm mới muốn đổi sang!", "WARNING"); return;
        }

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
            String maPhieu = "DTH-" + (System.currentTimeMillis() % 10000);
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
                    String sl = spMoiModel.getValueAt(i, 2).toString();
                    String giaChuan = spMoiModel.getValueAt(i, 3).toString().replaceAll("[^0-9]", "");
                    
                    spDoiSb.append(tenSp).append("_").append(dvt).append("_").append(sl).append("_").append(giaChuan).append("; ");
                }
                strSPDoi = "DOI: " + spDoiSb.toString().replaceAll("; $", "");
            }

            String lyDoFull = lyDo + (ghiChu.isEmpty() ? "" : " - " + ghiChu);
            String formatGhiChu = "Chờ xử lý | " + lyDoFull + " | " + colHoanTien + " | " + colChenhLech + " | " + strSPTra + " | " + strSPDoi;

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
            	        int sl = Integer.parseInt(spMoiModel.getValueAt(i, 2).toString());
            	        String strGia = spMoiModel.getValueAt(i, 3).toString().replaceAll("[^0-9]", "");
            	        double gia = Double.parseDouble(strGia);
            	        busCT.themChiTietDoiTra(maPhieu, ten, dvt, sl, gia, "Hàng khách đổi mới", false);
            	    }
            	}

                // Cập nhật giao diện sau khi lưu xong
                mainModel.addRow(new Object[]{
                    maPhieu, maHDGoc, khach, loai, lyDoFull, colHoanTien, colChenhLech, "Chờ xử lý", ngayTao, formatGhiChu
                });
                
                isTaoThanhCong = true;
                maPhieuMoi = maPhieu;

                SwingUtilities.invokeLater(() -> {
                    Window owner = getOwner();
                    if (owner instanceof MainDashboard) {
                        MainDashboard md = (MainDashboard) owner;
                        md.lamMoiManHinhChinh();
                        md.lamMoiManHinhThongKe();   // Thêm dòng này
                    }
                });

                dispose(); 
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
        pnlTransferBot.add(lblQRAmount, BorderLayout.NORTH);
        
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
        
        lblQRAmount.setText("Cần bù: " + String.format("%,dđ", soTienThucTeCanXuLy));
        
        try {
            String info = "Bu tien doi hang " + lblMaHoaDonInfo.getText();
            info = info.replace(" ", "%20"); 
            String url = "https://img.vietqr.io/image/vcb-1038858525-compact2.png?amount=" 
                         + soTienThucTeCanXuLy + "&addInfo=" + info;
            
            new SwingWorker<ImageIcon, Void>() {
                @Override protected ImageIcon doInBackground() throws Exception {
                    java.net.URL imgUrl = new java.net.URL(url);
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) imgUrl.openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0"); 
                    return new ImageIcon(new javax.swing.ImageIcon(javax.imageio.ImageIO.read(conn.getInputStream()))
                        .getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH));
                }
                @Override protected void done() {
                    try { lblQRCode.setIcon(get()); lblQRCode.setText(""); } catch (Exception e) {}
                }
            }.execute();
        } catch (Exception e) {}
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
}