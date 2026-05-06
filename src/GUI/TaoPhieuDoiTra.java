package GUI;

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

public class TaoPhieuDoiTra extends JDialog {
    private DefaultTableModel spMoiModel, mainModel, chiTietModel; 
    private JTable tableSPMoi, table;
    private boolean isUpdatingTable = false, isUpdatingCart = false;
    private JScrollPane spTableTraHang, scrollSPMoi; 
    private JPanel pnlTableTraHang;
    
    private JPanel pnlHeader, pnlNewProduct, pnlFoundData;
    private JLabel lblError;
    private JTextField txtSearch, txtSearchNew, txtGhiChu; 
    private JButton btnTraHang, btnDoiHang, btnTaoPhieu;
    private JComboBox<String> cboLyDo; 
    
    private String phuongThucDoiTra = "Tiền mặt";
    private JPanel pnlPaymentMethods, pnlCashDetails, pnlTransferDetails;
    private JTextField txtTienKhachDua;
    private JPanel pnlPaymentSection;
    private JLabel lblTienThuaTraKhach, lblQRCode, lblQRAmount;
    private long soTienThucTeCanXuLy = 0; 
    private boolean isCKXacNhan = false; 
    private JButton btnXacNhanCK; 
    
    private JLabel lblKhachHang, lblMaHoaDonInfo, lblThoiGianThanhToan;
    private JLabel lblGiaTriSPTra, lblThoiGianMua, lblBadgeHoanTien, lblSoTienHoan, lblTextSoTienHoan;
    private long tongTienMat = 0;
    private java.util.List<int[]> listCounters = new java.util.ArrayList<>();
    private java.util.List<JLabel> listCountLabels = new java.util.ArrayList<>();
    private JLabel lblTotalValue, lblTienThuaValue, lblExactValue;
    
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

        JButton btnClose = new JButton("X");
        btnClose.setForeground(Color.WHITE);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false); 
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(btnClose, BorderLayout.EAST);
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
                "Khách hàng thay đổi ý định (Hỗ trợ 80%)", 
                "Lỗi bảo quản từ phía khách hàng (Từ chối)"
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

        spMoiModel = new DefaultTableModel(new String[]{"Sản phẩm mới", "ĐVT", "SL", "Đơn giá", "Thành tiền"}, 0) {
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
                            double giaBanMoi = 0;
                            
                            try (java.sql.Connection con = ConnectDB.ConnectDB.getInstance().getConnection()) {
                                String sql = "SELECT dv.gia FROM DonViDoLuong dv JOIN SanPham sp ON dv.sanPhamId = sp.id WHERE sp.ten = ? AND dv.ten = ?";
                                try (java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
                                    pst.setString(1, tenSP);
                                    pst.setString(2, donViMoi);
                                    try (java.sql.ResultSet rs = pst.executeQuery()) {
                                        if (rs.next()) giaBanMoi = rs.getDouble("gia");
                                    }
                                }
                            } catch (Exception ex) {}

                            if (giaBanMoi > 0) {
                                spMoiModel.setValueAt(String.format("%,.0fđ", giaBanMoi), row, 3);
                                int sl = Integer.parseInt(spMoiModel.getValueAt(row, 2).toString());
                                spMoiModel.setValueAt(String.format("%,.0fđ", sl * giaBanMoi), row, 4);
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
                        
                        isUpdatingCart = true;
                        spMoiModel.setValueAt(sl, row, 2);
                        spMoiModel.setValueAt(String.format("%,.0fđ", sl * donGia), row, 4);
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

        return pnl;
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
                    
                    try (java.sql.Connection con = ConnectDB.ConnectDB.getInstance().getConnection()) {
                        String sql = "SELECT dv.ten FROM DonViDoLuong dv JOIN SanPham sp ON dv.sanPhamId = sp.id WHERE sp.ten = ?";
                        try (java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
                            pst.setString(1, tenSP);
                            try (java.sql.ResultSet rs = pst.executeQuery()) {
                                boolean hasData = false;
                                while (rs.next()) {
                                    cbDVT.addItem(rs.getString("ten"));
                                    hasData = true;
                                }
                                if (!hasData && !dvtHienTai.isEmpty()) cbDVT.addItem(dvtHienTai);
                            }
                        }
                    } catch (Exception ex) {
                        if (!dvtHienTai.isEmpty()) cbDVT.addItem(dvtHienTai);
                    }
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
        tableSPMoi.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); 
        tableSPMoi.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); 
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        tableSPMoi.getColumnModel().getColumn(3).setCellRenderer(rightRenderer); 
        tableSPMoi.getColumnModel().getColumn(4).setCellRenderer(rightRenderer); 

        tableSPMoi.getColumnModel().getColumn(0).setPreferredWidth(180);
        tableSPMoi.getColumnModel().getColumn(1).setPreferredWidth(60); 
        tableSPMoi.getColumnModel().getColumn(2).setPreferredWidth(40); 
        tableSPMoi.getColumnModel().getColumn(3).setPreferredWidth(80); 
        tableSPMoi.getColumnModel().getColumn(4).setPreferredWidth(80); 
        
        scrollSPMoi = new JScrollPane(tableSPMoi);
        scrollSPMoi.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#E2E8F0")));
        scrollSPMoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        
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

    // THUẬT TOÁN LOẠI BỎ TRÙNG LẶP SẢN PHẨM KHẮC PHỤC LỖI BACKEND
    private void xuLyTimKiemHD() {
        String maHD = txtSearch.getText().trim();
        if (maHD.isEmpty() || maHD.equals("Nhập mã hóa đơn (VD: HD-2024-0001)...")) {
            lblError.setText(" Vui lòng nhập mã hóa đơn!"); lblError.setVisible(true); return;
        }

        boolean isHopLe = busTraHang.kiemTraDieuKien(maHD);
        if (!isHopLe) {
            lblError.setText(" Hóa đơn không tồn tại, quá 7 ngày, hoặc không phải HD Bán Hàng!");
            lblError.setVisible(true);
            if (pnlFoundData != null) pnlFoundData.setVisible(false);
            btnTaoPhieu.setEnabled(false);
            
            setSize(850, 310); 
            setLocationRelativeTo(getOwner()); 
            return;
        }

        HoaDon hd = busHD.getHoaDonTheoMa(maHD); 
        ngayHoaDonGoc = hd.getNgayLapHD(); 
        chiTietModel.setRowCount(0); 
        tongTienGoc = 0; 
        
        BUS_ChiTietHoaDon busCTHD = new BUS_ChiTietHoaDon(); 
        List<Object[]> dsChiTiet = busCTHD.layDuLieuDoiTra(hd.getId()); 

        if (dsChiTiet != null && !dsChiTiet.isEmpty()) {
            java.util.Set<String> addedProducts = new java.util.HashSet<>();
            BUS_SanPham busSP = new BUS_SanPham();
            BUS_DonViDoLuong busDVT = new BUS_DonViDoLuong();

            for (Object[] rowData : dsChiTiet) {
                String tenSP = rowData[0].toString();
                int slMua = Integer.parseInt(rowData[2].toString());
                String donGiaStr = rowData[3].toString();
                long donGiaNum = 0;
                
                try {
                    String clean = donGiaStr.replaceAll("[^0-9]", "");
                    donGiaNum = Long.parseLong(clean);
                } catch(Exception e){}

                // Kỹ thuật gộp chung Tên và Giá để khử trùng lặp 100%
                String uniqueKey = tenSP + "_" + donGiaNum;
                if (!addedProducts.contains(uniqueKey)) {
                    
                    // Tìm ĐVT thực sự khớp với Giá bán trong Kho
                    String correctDvt = "Hộp";
                    try {
                        List<SanPham> listSP = busSP.traCuuSanPham(tenSP);
                        if(listSP != null && !listSP.isEmpty()) {
                            List<DonViDoLuong> listDVT = busDVT.getDSTheoMaSP(listSP.get(0).getId());
                            if(listDVT != null) {
                                for(DonViDoLuong d : listDVT) {
                                    if(d.getGia() == donGiaNum) {
                                        correctDvt = d.getTen();
                                        break;
                                    }
                                }
                            }
                        }
                    } catch(Exception e){}

                    String donGiaFmt = String.format("%,dđ", donGiaNum).replace(',', '.');
                    chiTietModel.addRow(new Object[]{false, tenSP, correctDvt, slMua, donGiaFmt, slMua});
                    addedProducts.add(uniqueKey);
                }
            }
        }

        String khachName = "Khách lẻ";
        if(hd.getKhachHangId() != null) {
            BUS_KhachHang bkh = new BUS_KhachHang();
            KhachHang kh = bkh.getKhachHangTheoSDT(hd.getKhachHangId().getId()); 
            if(kh != null) khachName = kh.getHoVaTen();
        }
        lblKhachHang.setText(khachName);
        lblMaHoaDonInfo.setText(hd.getId());
        lblThoiGianThanhToan.setText(hd.getNgayLapHD().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        long hours = java.time.Duration.between(ngayHoaDonGoc, LocalDateTime.now()).toHours();
        lblThoiGianMua.setText(hours + " giờ");

        lblError.setVisible(false);
        pnlFoundData.setVisible(true);
        btnTaoPhieu.setEnabled(true);
        
        setSize(1050, 720); 
        setLocationRelativeTo(getOwner());
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15)); 
        capNhatDieuKienDoiTra(); 

        SwingUtilities.invokeLater(() -> {
            if (pnlTableTraHang != null && spTableTraHang != null && table != null) {
                int rowCount = chiTietModel.getRowCount();
                int headerHeight = table.getTableHeader() != null ? table.getTableHeader().getPreferredSize().height : 0;
                
                int dynamicHeight = (rowCount * table.getRowHeight()) + headerHeight + 8;
                
                spTableTraHang.setPreferredSize(new Dimension(0, dynamicHeight));
                spTableTraHang.setMaximumSize(new Dimension(Integer.MAX_VALUE, dynamicHeight)); 
                pnlTableTraHang.setMaximumSize(new Dimension(Integer.MAX_VALUE, dynamicHeight + 40)); 
                
                pnlFoundData.revalidate();
                pnlFoundData.repaint();
            }
        });
    }

    private void checkDieuKienThanhToan() {
        if (btnTaoPhieu == null) return;
        boolean isTraHang = btnTraHang.getBackground().equals(Color.WHITE);
        
        if (isTraHang) {
            btnTaoPhieu.setEnabled(true);
            return;
        }

        if (lblTextSoTienHoan.getText().contains("Khách bù")) {
            if ("Tiền mặt".equals(phuongThucDoiTra)) {
                btnTaoPhieu.setEnabled(tongTienMat >= soTienThucTeCanXuLy);
            } else if ("Chuyển khoản".equals(phuongThucDoiTra)) {
                btnTaoPhieu.setEnabled(isCKXacNhan);
            }
        } else {
            btnTaoPhieu.setEnabled(true);
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
            if(btnTaoPhieu != null) btnTaoPhieu.setEnabled(false);
            return;
        }

        double phanTramHoan = 0.0;
        String thongBaoHoanTien = "";

        if (lyDo.contains("Từ chối")) {
            phanTramHoan = 0.0;
            thongBaoHoanTien = "Từ chối (Lỗi KH)";
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
        
        if (phanTramHoan == 0.0) {
            lblBadgeHoanTien.setText(thongBaoHoanTien); lblBadgeHoanTien.setForeground(primaryRed); lblBadgeHoanTien.setBackground(Color.decode("#FEE2E2"));
            lblSoTienHoan.setText("0đ");
            if(btnTaoPhieu != null) btnTaoPhieu.setEnabled(false);
            return;
        }

        double v_qd = busTraHang.xacDinhMucHoanTien(giaTriGoc, phanTramHoan); 

        if (isTraHang) {
            lblTextSoTienHoan.setText("Số tiền hoàn");
            lblBadgeHoanTien.setText(thongBaoHoanTien);
            lblBadgeHoanTien.setForeground(Color.decode("#16A34A")); 
            lblBadgeHoanTien.setBackground(Color.decode("#DCFCE7"));
            
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
                    int totalHeight = (spMoiModel.getRowCount() * rowHeight) + headerHeight + 8; 
                    
                    scrollSPMoi.setPreferredSize(new Dimension(0, totalHeight));
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
        SwingUtilities.invokeLater(() -> {
            String kw = txtSearchNew.getText().trim();
            if (kw.isEmpty() || kw.equals("Gõ tên để tìm sản phẩm...")) { suggestionMenu.setVisible(false); return; }

            BUS_SanPham busSP = new BUS_SanPham();
            List<SanPham> dsGoiY = busSP.traCuuSanPham(kw); 

            if (dsGoiY != null && !dsGoiY.isEmpty()) {
                for (SanPham sp : dsGoiY) { if (sp.getTen().equalsIgnoreCase(kw)) { suggestionMenu.setVisible(false); return; } }
                suggestionMenu.removeAll(); 
                JPanel pnlList = new JPanel(); pnlList.setLayout(new BoxLayout(pnlList, BoxLayout.Y_AXIS)); pnlList.setBackground(Color.WHITE);

                for (SanPham sp : dsGoiY) {
                    JPanel pnlItem = new JPanel(new BorderLayout(15, 5)); pnlItem.setBackground(Color.WHITE);
                    pnlItem.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E5E7EB")), new EmptyBorder(8, 12, 8, 12)));
                    pnlItem.setCursor(new Cursor(Cursor.HAND_CURSOR)); pnlItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55)); 

                    JPanel pnlInfo = new JPanel(new BorderLayout(0, 3)); pnlInfo.setOpaque(false); 
                    JLabel lblTen = new JLabel(sp.getTen()); lblTen.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    
                    JPanel pnlSub = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); pnlSub.setOpaque(false);
                    JLabel lblMa = new JLabel(sp.getId()); lblMa.setFont(new Font("Segoe UI", Font.PLAIN, 13)); lblMa.setForeground(Color.decode("#6B7280")); 
                    
                    String tenDanhMuc = "Khác"; Color bgTag = Color.decode("#F3F4F6"), fgTag = Color.decode("#4B5563"); 
                    if (sp.getDanhMuc() != null) {
                        switch (sp.getDanhMuc()) {
                            case THUOC_KE_DON: tenDanhMuc = "Thuốc kê đơn"; bgTag = Color.decode("#FEE2E2"); fgTag = Color.decode("#DC2626"); break;
                            case THUOC_KHONG_KE_DON: tenDanhMuc = "Không kê đơn"; bgTag = Color.decode("#DCFCE7"); fgTag = Color.decode("#16A34A"); break;
                            case THUC_PHAM_CHUC_NANG: tenDanhMuc = "Thực phẩm chức năng"; bgTag = Color.decode("#DBEAFE"); fgTag = Color.decode("#1D4ED8"); break;
                            case MY_PHAM: tenDanhMuc = "Mỹ phẩm"; bgTag = Color.decode("#FCE7F3"); fgTag = Color.decode("#BE185D"); break;
                        }
                    }

                    final Color finalBgTag = bgTag;
                    JLabel lblTag = new JLabel(tenDanhMuc) {
                        @Override
                        protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(finalBgTag); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); 
                            super.paintComponent(g); g2.dispose();
                        }
                    };
                    lblTag.setFont(new Font("Segoe UI", Font.BOLD, 10)); lblTag.setForeground(fgTag); lblTag.setOpaque(false); lblTag.setBorder(new EmptyBorder(1, 6, 1, 6)); 
                    pnlSub.add(lblMa); pnlSub.add(Box.createRigidArea(new Dimension(8, 0))); pnlSub.add(lblTag);
                    pnlInfo.add(lblTen, BorderLayout.NORTH); pnlInfo.add(pnlSub, BorderLayout.CENTER);

                    BUS_DonViDoLuong busDVDL = new BUS_DonViDoLuong(); List<DonViDoLuong> dsDVT = busDVDL.getDSTheoMaSP(sp.getId());
                    double gia = 0; String dvt = "Hộp"; if (dsDVT != null && !dsDVT.isEmpty()) { gia = dsDVT.get(0).getGia(); dvt = dsDVT.get(0).getTen(); }
                    
                    final double finalGia = gia; 
                    final String finalDvt = dvt; 

                    JLabel lblGia = new JLabel(String.format("%,.0fđ / %s", gia, dvt)); lblGia.setFont(new Font("Segoe UI", Font.BOLD, 13)); lblGia.setForeground(Color.decode("#1967D2")); 
                    pnlItem.add(pnlInfo, BorderLayout.CENTER); pnlItem.add(lblGia, BorderLayout.EAST);

                    pnlItem.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseEntered(java.awt.event.MouseEvent evt) { pnlItem.setBackground(Color.decode("#F8FAFC")); }
                        public void mouseExited(java.awt.event.MouseEvent evt) { pnlItem.setBackground(Color.WHITE); }
                        public void mousePressed(java.awt.event.MouseEvent evt) {
                            suggestionMenu.setVisible(false); 
                            SwingUtilities.invokeLater(() -> {
                                txtSearchNew.setText(""); 
                                boolean exists = false;
                                for(int i=0; i < spMoiModel.getRowCount(); i++) {
                                    if(spMoiModel.getValueAt(i, 0).equals(sp.getTen()) && spMoiModel.getValueAt(i, 1).equals(finalDvt)) {
                                        int oldSL = Integer.parseInt(spMoiModel.getValueAt(i, 2).toString());
                                        spMoiModel.setValueAt(oldSL + 1, i, 2); 
                                        spMoiModel.setValueAt(String.format("%,.0fđ", (oldSL + 1) * finalGia), i, 4);
                                        exists = true; 
                                        break;
                                    }
                                }
                                if(!exists) {
                                    isUpdatingCart = true; 
                                    spMoiModel.addRow(new Object[]{ sp.getTen(), finalDvt, 1, String.format("%,.0fđ", finalGia), String.format("%,.0fđ", finalGia) }); 
                                    isUpdatingCart = false;
                                }
                                tinhTongTienSPMoi(); 
                            });
                        }
                    });
                    pnlList.add(pnlItem);
                }
                
                JScrollPane scrollPane = new JScrollPane(pnlList); scrollPane.setBorder(null); scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); 
                scrollPane.setPreferredSize(new Dimension(txtSearchNew.getWidth(), Math.min(pnlList.getPreferredSize().height, 250)));
                scrollPane.getVerticalScrollBar().setUI(new Utils.ModernScrollBarUI()); scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0)); 
                
                suggestionMenu.add(scrollPane); suggestionMenu.pack(); 
                if (!suggestionMenu.isVisible()) suggestionMenu.show(txtSearchNew, 0, txtSearchNew.getHeight());
            } else suggestionMenu.setVisible(false);
        });
    }

    private void xuLyTaoPhieu() {
        if (ngayHoaDonGoc == null || txtSearch.getText().contains("Nhập mã hóa đơn")) {
            JOptionPane.showMessageDialog(this, "Vui lòng tìm kiếm hóa đơn hợp lệ trước khi tạo phiếu!", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return;
        }

        String loai = btnTraHang.getBackground().equals(Color.WHITE) ? "Trả hàng" : "Đổi hàng";
        if (loai.equals("Đổi hàng") && spMoiModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng tìm và chọn sản phẩm mới muốn đổi sang!", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return;
        }

        String lyDo = cboLyDo.getSelectedItem().toString();
        String colHoanTien = "0đ";
        String colChenhLech = "0đ";

        try {
            if (loai.equals("Trả hàng")) colHoanTien = lblSoTienHoan.getText().trim();
            else colChenhLech = lblSoTienHoan.getText().trim(); 
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tính toán tiền tệ!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
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
                JOptionPane.showMessageDialog(this, "Vui lòng tick chọn ít nhất 1 sản phẩm cần trả (cột Chọn)!", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return;
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
            nv.setNhanVien("DS-0001");
            hdDoiTra.setNhanVienId(nv);
            hdDoiTra.setPhuongThucThanhToan(phuongThucDoiTra.equals("Chuyển khoản") ? 
                Enumeration.PhuongThucThanhToan.CHUYEN_KHOAN_NGAN_HANG : Enumeration.PhuongThucThanhToan.TIEN_MAT);
            
            hdDoiTra.setGhiChu(formatGhiChu);
            
            HoaDon hdGoc = new HoaDon();
            hdGoc.setId(maHDGoc);
            hdDoiTra.setHoaDonGocId(hdGoc);
            
            if (busHD.taoPhieuDoiTra(hdDoiTra)) {
                mainModel.addRow(new Object[]{
                    maPhieu, maHDGoc, khach, loai, lyDoFull, colHoanTien, colChenhLech, "Chờ xử lý", ngayTao, formatGhiChu
                });
                JOptionPane.showMessageDialog(this, "Đã tiếp nhận yêu cầu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi! Không thể ghi nhận phiếu vào Database.", "Lỗi DB", JOptionPane.ERROR_MESSAGE);
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
}