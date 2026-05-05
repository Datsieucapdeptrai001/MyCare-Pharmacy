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
    
    // UI Elements 
    private JPanel pnlHeader, pnlNewProduct, pnlFoundData;
    private JLabel lblError;
    private JTextField txtSearch, txtSearchNew, txtGhiChu; // Bổ sung txtGhiChu
    private JButton btnTraHang, btnDoiHang, btnTaoPhieu;
    private JComboBox<String> cboLyDo; 
    
    // UI Elements - Cột Data
    private JLabel lblKhachHang, lblMaHoaDonInfo, lblThoiGianThanhToan;
    private JLabel lblGiaTriSPTra, lblThoiGianMua, lblBadgeHoanTien, lblSoTienHoan, lblTextSoTienHoan;
    
    // BUS Logic
    private BUS_HoaDon busHD = new BUS_HoaDon();
    private BUS_TraHang busTraHang = new BUS_TraHang();
    private BUS_DoiHang busDoiHang = new BUS_DoiHang();
    
    private LocalDateTime ngayHoaDonGoc; 
    private JPopupMenu suggestionMenu;

    private double tongTienGoc = 0, giaMoi = 0; 
    private String tenMoi = "";
    
    private final Color primaryRed = Color.decode("#E11D48"); 
    private final Color primaryBlue = Color.decode("#2563EB");
    private final Color borderGray = Color.decode("#E2E8F0");
    
    public TaoPhieuDoiTra(Frame parent, DefaultTableModel model) {
        super(parent, "Tạo phiếu đổi / trả hàng", true);
        setSize(850, 420); // Tăng size ban đầu để các ô nhập liệu nhìn thoáng hơn
        this.mainModel = model;
        setLocationRelativeTo(parent);
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
    }

    private void initUI() {
        pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(primaryRed);
        pnlHeader.setBorder(new EmptyBorder(12, 20, 12, 20)); // Ép Header nhỏ lại
        
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

        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.decode("#F8FAFC"));
        pnlBody.setBorder(new EmptyBorder(15, 15, 15, 15)); // Ép lề ngoài cùng gọn lại

        JPanel pnlContainer = new JPanel();
        pnlContainer.setLayout(new BoxLayout(pnlContainer, BoxLayout.Y_AXIS));
        pnlContainer.setBackground(Color.WHITE);
        pnlContainer.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borderGray, 1, true),
            new EmptyBorder(15, 20, 15, 20) // Ép lề khối trắng bên trong
        ));

        initModels();

        pnlContainer.add(createTogglePanel());
        pnlContainer.add(Box.createRigidArea(new Dimension(0, 15))); // Giảm khoảng cách
        pnlContainer.add(createSearchPanel());
        pnlContainer.add(Box.createRigidArea(new Dimension(0, 15))); // Giảm khoảng cách

        pnlFoundData = createFoundDataPanel();
        pnlFoundData.setVisible(false);
        pnlContainer.add(pnlFoundData);

        pnlBody.add(pnlContainer);

        JScrollPane scrollPane = new JScrollPane(pnlBody);
        scrollPane.setBorder(null);
        // Tắt hẳn thanh cuộn ngang, thanh cuộn dọc chỉ hiện khi thật sự cần
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, borderGray),
            new EmptyBorder(10, 20, 10, 20) // Ép lề Footer
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
        
        String[] cols = {"Chọn", "Sản phẩm", "Số lượng", "Đơn giá", "MaxSL"};
        chiTietModel = new DefaultTableModel(cols, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class; 
                if (columnIndex == 2 || columnIndex == 4) return Integer.class; 
                return String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) { 
                return column == 0 || column == 2; 
            }
        };

        chiTietModel.addTableModelListener(e -> {
            if (isUpdatingTable) return;
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                
                if (row >= 0 && col == 2) { 
                    try {
                        int slMax = (int) chiTietModel.getValueAt(row, 4);
                        int slThaoTac = (int) chiTietModel.getValueAt(row, 2);
                        if (slThaoTac < 1) slThaoTac = 1;
                        if (slThaoTac > slMax) slThaoTac = slMax; 
                        
                        isUpdatingTable = true;
                        chiTietModel.setValueAt(slThaoTac, row, 2);
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

        spMoiModel = new DefaultTableModel(new String[]{"Sản phẩm mới", "SL", "Đơn giá", "Thành tiền"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 1; } 
        };
        
        spMoiModel.addTableModelListener(e -> {
            if (isUpdatingCart) return;
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 1) {
                int row = e.getFirstRow();
                try {
                    int sl = Integer.parseInt(spMoiModel.getValueAt(row, 1).toString());
                    if (sl < 1) sl = 1;
                    double donGia = Double.parseDouble(spMoiModel.getValueAt(row, 2).toString().replaceAll("[^0-9]", ""));
                    
                    isUpdatingCart = true;
                    spMoiModel.setValueAt(sl, row, 1);
                    spMoiModel.setValueAt(String.format("%,.0fđ", sl * donGia), row, 3);
                } catch(Exception ex) { 
                    ex.printStackTrace();
                } finally {
                    isUpdatingCart = false;
                    SwingUtilities.invokeLater(() -> tinhTongTienSPMoi());
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

        Dimension btnSize = new Dimension(160, 38); // Hạ chiều cao nút xuống 38px
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
        JPanel pnl = new JPanel(new BorderLayout(0, 5)); // Đẩy sát title và input
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
        txtSearch.setPreferredSize(new Dimension(0, 40)); // Hạ chiều cao input xuống 40px
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
        btnTim.setPreferredSize(new Dimension(120, 40)); // Hạ chiều cao nút tìm kiếm
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

        // 1. Box Hóa đơn (Ép gọn)
        JPanel pnlInvoice = new JPanel(new BorderLayout());
        pnlInvoice.setBackground(Color.WHITE);
        pnlInvoice.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borderGray, 1, true), new EmptyBorder(10, 15, 10, 15) 
        ));

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

        // 2. Bảng sản phẩm (Giảm chiều cao dòng)
        JPanel pnlTable = new JPanel(new BorderLayout(0, 5));
        pnlTable.setBackground(Color.WHITE);
        JLabel lblTb = new JLabel("Sản phẩm trả hàng");
        lblTb.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlTable.add(lblTb, BorderLayout.NORTH);

        table = new JTable(chiTietModel);
        table.setRowHeight(38); // Hạ chiều cao dòng xuống để bảng gọn lại
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderGray));

        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(4));
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(230);
        
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setCellRenderer(new SpinnerRenderer());
        table.getColumnModel().getColumn(2).setCellEditor(new SpinnerEditor());
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        rightRenderer.setForeground(Color.GRAY);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer); 

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(0, 125)); // Giảm chiều cao thanh cuộn của bảng
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(BorderFactory.createLineBorder(borderGray));
        pnlTable.add(sp, BorderLayout.CENTER);
        
        pnlNewProduct = createNewProductPanel();

        // 3. ĐƯA LÝ DO VÀ GHI CHÚ NẰM NGANG NHAU ĐỂ TIẾT KIỆM DIỆN TÍCH
        JPanel pnlReasonNote = new JPanel(new GridLayout(1, 2, 15, 0)); // 1 dòng, 2 cột
        pnlReasonNote.setBackground(Color.WHITE);

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
        
        pnlLeft.add(pnlInvoice);
        pnlLeft.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlLeft.add(pnlTable);
        pnlLeft.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlLeft.add(pnlNewProduct);
        pnlLeft.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlLeft.add(pnlReasonNote);

        // --- CỘT PHẢI ---
        JPanel pnlRightWrap = new JPanel(new BorderLayout());
        pnlRightWrap.setBackground(Color.WHITE);
        pnlRightWrap.setPreferredSize(new Dimension(260, 0)); 

        JPanel pnlSummary = new JPanel();
        pnlSummary.setLayout(new BoxLayout(pnlSummary, BoxLayout.Y_AXIS));
        pnlSummary.setBackground(Color.decode("#F8FAFC"));
        pnlSummary.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borderGray, 1, true), new EmptyBorder(20, 15, 20, 15) // Rút gọn viền hộp tổng kết
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
        pnl.add(pnlLeft, BorderLayout.CENTER); pnl.add(pnlRightWrap, BorderLayout.EAST);

        return pnl;
    }

    private JPanel createNewProductPanel() {
        JPanel pnl = new JPanel(new BorderLayout(15, 10));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createTitledBorder(null, "Giỏ hàng sản phẩm mới (Có thể chọn nhiều)", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13)));

        txtSearchNew = new JTextField();
        txtSearchNew.setPreferredSize(new Dimension(0, 42));
        txtSearchNew.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearchNew.setText("Gõ tên để tìm sản phẩm...");
        txtSearchNew.setForeground(Color.GRAY);
        txtSearchNew.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtSearchNew.getText().equals("Gõ tên để tìm sản phẩm...")) {
                    txtSearchNew.setText(""); txtSearchNew.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtSearchNew.getText().isEmpty()) {
                    txtSearchNew.setForeground(Color.GRAY); txtSearchNew.setText("Gõ tên để tìm sản phẩm...");
                }
            }
        });

        suggestionMenu = new JPopupMenu();
        suggestionMenu.setFocusable(false);
        suggestionMenu.setBackground(Color.WHITE);
        txtSearchNew.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { timKiemLive(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { timKiemLive(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { timKiemLive(); }
        });

        tableSPMoi = new JTable(spMoiModel);
        tableSPMoi.setRowHeight(35);
        tableSPMoi.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableSPMoi.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        JScrollPane scrollSPMoi = new JScrollPane(tableSPMoi);
        scrollSPMoi.setPreferredSize(new Dimension(0, 120));

        JPanel pnlTop = new JPanel(new BorderLayout(10, 0));
        pnlTop.setOpaque(false);
        pnlTop.add(txtSearchNew, BorderLayout.CENTER);
        
        JButton btnXoa = new JButton("Xóa món chọn");
        btnXoa.setBackground(Color.decode("#FEE2E2"));
        btnXoa.setForeground(Color.decode("#DC2626"));
        btnXoa.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnXoa.setFocusPainted(false);
        btnXoa.addActionListener(e -> {
            int row = tableSPMoi.getSelectedRow();
            if(row >= 0) {
                spMoiModel.removeRow(row);
                tinhTongTienSPMoi();
            }
        });
        pnlTop.add(btnXoa, BorderLayout.EAST);

        pnl.add(pnlTop, BorderLayout.NORTH);
        pnl.add(scrollSPMoi, BorderLayout.CENTER);
        return pnl;
    }

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
            setSize(850, 420); 
            return;
        }

        HoaDon hd = busHD.getHoaDonTheoMa(maHD); 
        ngayHoaDonGoc = hd.getNgayLapHD(); 
        chiTietModel.setRowCount(0); 
        tongTienGoc = 0; 
        
        BUS_ChiTietHoaDon busCTHD = new BUS_ChiTietHoaDon(); 
        List<Object[]> dsChiTiet = busCTHD.layDuLieuDoiTra(hd.getId()); 

        if (dsChiTiet != null && !dsChiTiet.isEmpty()) {
            for (Object[] rowData : dsChiTiet) {
                int slMua = Integer.parseInt(rowData[2].toString()); 
                String donGia = rowData[3].toString(); 
                if(!donGia.contains("đ")) donGia = String.format("%,.0fđ", Double.parseDouble(donGia));

                chiTietModel.addRow(new Object[]{false, rowData[0].toString(), slMua, donGia, slMua});
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
        
        setSize(900, 680); // Nới rộng chiều cao để chứa Form Ghi chú mới thêm
        setLocationRelativeTo(getOwner());
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15)); 
        capNhatDieuKienDoiTra(); 
    }

    private void capNhatDieuKienDoiTra() {
        if (lblSoTienHoan == null || cboLyDo == null || ngayHoaDonGoc == null) return;

        long soGioDaMua = java.time.Duration.between(ngayHoaDonGoc, LocalDateTime.now()).toHours();
        String lyDo = cboLyDo.getSelectedItem().toString();
        boolean isTraHang = btnTraHang.getBackground().equals(Color.WHITE); 
        
        double giaTriGoc = 0;
        int countSelected = 0;
        
        for (int i = 0; i < chiTietModel.getRowCount(); i++) {
            boolean isSelected = (boolean) chiTietModel.getValueAt(i, 0);
            if (isSelected) {
                int sl = (int) chiTietModel.getValueAt(i, 2); 
                String donGiaStr = chiTietModel.getValueAt(i, 3).toString();
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

        // LOGIC KẾT HỢP LÝ DO VÀ THỜI GIAN THEO YÊU CẦU CỦA CEO
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

        if(btnTaoPhieu != null) btnTaoPhieu.setEnabled(true);
        double v_qd = busTraHang.xacDinhMucHoanTien(giaTriGoc, phanTramHoan); 

        if (isTraHang) {
            lblTextSoTienHoan.setText("Số tiền hoàn");
            lblBadgeHoanTien.setText(thongBaoHoanTien);
            lblBadgeHoanTien.setForeground(Color.decode("#16A34A")); lblBadgeHoanTien.setBackground(Color.decode("#DCFCE7"));
            lblSoTienHoan.setText(String.format("%,.0fđ", v_qd)); lblSoTienHoan.setForeground(primaryRed);
        } else {
            lblBadgeHoanTien.setText("Đang tính chênh lệch..."); lblBadgeHoanTien.setForeground(primaryBlue); lblBadgeHoanTien.setBackground(Color.decode("#DBEAFE"));
            if (spMoiModel.getRowCount() == 0) {
                lblSoTienHoan.setText("0đ"); lblTextSoTienHoan.setText("Vui lòng thêm SP mới");
                if(btnTaoPhieu != null) btnTaoPhieu.setEnabled(false);
            } else {
                double chenhLech = busDoiHang.tinhTienChenhLech(v_qd, giaMoi);
                if (chenhLech > 0) {
                    lblTextSoTienHoan.setText("Khách bù thêm"); lblSoTienHoan.setForeground(primaryBlue);
                } else {
                    lblTextSoTienHoan.setText("Thối lại khách"); lblSoTienHoan.setForeground(primaryRed);
                }
                lblSoTienHoan.setText(String.format("%,.0fđ", Math.abs(chenhLech)));
                lblBadgeHoanTien.setText(thongBaoHoanTien); 
            }
        }
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
        capNhatDieuKienDoiTra();
    }

    private void tinhTongTienSPMoi() {
        giaMoi = 0; tenMoi = "";
        for (int i = 0; i < spMoiModel.getRowCount(); i++) {
            giaMoi += Double.parseDouble(spMoiModel.getValueAt(i, 3).toString().replaceAll("[^0-9]", ""));
            tenMoi += spMoiModel.getValueAt(i, 0).toString() + ", ";
        }
        if(tenMoi.endsWith(", ")) tenMoi = tenMoi.substring(0, tenMoi.length() - 2);
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
                                    if(spMoiModel.getValueAt(i, 0).equals(sp.getTen())) {
                                        int oldSL = Integer.parseInt(spMoiModel.getValueAt(i, 1).toString());
                                        spMoiModel.setValueAt(oldSL + 1, i, 1); exists = true; break;
                                    }
                                }
                                if(!exists) {
                                    isUpdatingCart = true; spMoiModel.addRow(new Object[]{ sp.getTen(), 1, String.format("%,.0fđ", finalGia), String.format("%,.0fđ", finalGia) }); isUpdatingCart = false;
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

        int rowSelected = table.getSelectedRow(); 
        if (rowSelected == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng click chọn 1 sản phẩm trong bảng cần đổi/trả!", "Cảnh báo", JOptionPane.WARNING_MESSAGE); return;
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
            JOptionPane.showMessageDialog(this, "Lỗi tính toán tiền tệ. Vui lòng kiểm tra lại điều kiện đổi trả!", "Lỗi", JOptionPane.ERROR_MESSAGE); return;
        }

        if(mainModel != null) {
            String maPhieu = "DTH-" + (System.currentTimeMillis() % 10000);
            String maHDGoc = txtSearch.getText().trim();
            String ngayTao = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String khach = lblKhachHang.getText().trim(); 
            
            String ghiChu = txtGhiChu.getText().trim();
            if (loai.equals("Đổi hàng") && spMoiModel.getRowCount() > 0) {
                StringBuilder tenMoiSb = new StringBuilder();
                for(int i=0; i<spMoiModel.getRowCount(); i++) {
                    tenMoiSb.append(spMoiModel.getValueAt(i, 0)).append(", ");
                }
                String tenMoiStr = tenMoiSb.toString().replaceAll(", $", "");
                ghiChu = "Đổi sang: " + tenMoiStr + (ghiChu.isEmpty() ? "" : " | " + ghiChu);
            }

            // =========================================================
            // FIX LỖI DB: TẠO OBJECT VÀ LƯU PHIẾU ĐỔI TRẢ VÀO SQL
            // =========================================================
            HoaDon hdDoiTra = new HoaDon();
            hdDoiTra.setId(maPhieu);
            hdDoiTra.setLoaiHD(loai.equals("Trả hàng") ? Enumeration.LoaiHoaDon.TRA_HANG : Enumeration.LoaiHoaDon.DOI_HANG);
            hdDoiTra.setNgayLapHD(LocalDateTime.now());
            
            Entity.NhanVien nv = new Entity.NhanVien();
            nv.setNhanVien("DS-0001"); // Tài khoản thao tác (Nên lấy từ tài khoản đang đăng nhập)
            hdDoiTra.setNhanVienId(nv);
            
            hdDoiTra.setPhuongThucThanhToan(Enumeration.PhuongThucThanhToan.TIEN_MAT);
            // Lưu trạng thái "Chờ xử lý" cùng lý do vào DB để màn hình Quản lý đọc được
            hdDoiTra.setGhiChu("Chờ xử lý | " + lyDo + " - " + ghiChu); 
            
            HoaDon hdGoc = new HoaDon();
            hdGoc.setId(maHDGoc);
            hdDoiTra.setHoaDonGocId(hdGoc);
            
            DAO.DAO_HoaDon daoHD = new DAO.DAO_HoaDon();
            if (daoHD.themHoaDon(hdDoiTra)) {
                // LƯU SQL THÀNH CÔNG THÌ MỚI ADD LÊN BẢNG GIAO DIỆN
                mainModel.addRow(new Object[]{
                    maPhieu, maHDGoc, khach, loai, lyDo, colHoanTien, colChenhLech, "Chờ xử lý", ngayTao, ghiChu
                });
                JOptionPane.showMessageDialog(this, "Đã tiếp nhận yêu cầu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi! Không thể ghi nhận phiếu vào Database.", "Lỗi DB", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

 // =========================================================================
    // CLASS CUSTOM JSPINNER RENDERER VÀ EDITOR ĐỂ LÀM GIAO DIỆN SỐ LƯỢNG MỚI
    // =========================================================================

    class SpinnerRenderer extends JPanel implements TableCellRenderer {
        private JSpinner spinner;
        public SpinnerRenderer() {
            setLayout(new BorderLayout());
            // Ép viền nhỏ lại để nhét vừa row 38px, không bị phình to làm vỡ bảng
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
            // Ép viền nhỏ lại tương tự như Renderer
            pnl.setBorder(new EmptyBorder(2, 5, 2, 5));
            spinner = new JSpinner();
            spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            
            spinner.addChangeListener(e -> fireEditingStopped());
            pnl.add(spinner, BorderLayout.CENTER);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            pnl.setBackground(table.getSelectionBackground());
            
            // Lấy giới hạn MaxSL từ cột 4 (cột ẩn) để giới hạn Spinner không cho tăng lố số lượng khách mua
            int max = (int) table.getModel().getValueAt(row, 4);
            int currentVal = value != null ? (int) value : 1;
            
            spinner.setModel(new SpinnerNumberModel(currentVal, 1, max, 1));
            return pnl;
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }
    }
}