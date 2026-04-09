package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * ManHinhKhuyenMai.java - Phiên bản hoàn thiện 100% Giao diện & Sự kiện
 * Tích hợp TableRowSorter, Validation, DocumentListener và MouseListener.
 */
public class ManHinhKhuyenMai extends JPanel {

    // Bảng màu chuẩn
    private final Color COLOR_BG = Color.decode("#F4F6F8");
    private final Color COLOR_PRIMARY = Color.decode("#1A73E8");
    private final Color COLOR_ACTIVE_FILTER = Color.decode("#1967D2");
    private final Color COLOR_PURPLE = Color.decode("#9333EA");
    private final Color COLOR_PURPLE_LIGHT = Color.decode("#F5F3FF");
    private final Color COLOR_PURPLE_BORDER = Color.decode("#E9D5FF");
    private final Color COLOR_HEADER_TABLE = Color.decode("#1E3A5F");
    private final Color COLOR_SUCCESS = Color.decode("#10B981");
    private final Color COLOR_DANGER = Color.decode("#DC2626");
    private final Color COLOR_GRAY = Color.decode("#64748B");

    private final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);

    // Thành phần điều khiển Layout
    private CardLayout cardLayout;
    private JPanel pnlMain;

    // Thành phần dữ liệu & Bảng
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTable tblKhuyenMai;

    // Các ô nhập liệu Tích điểm
    private JTextField txtLoyaltyBuy, txtLoyaltyExchange, txtLoyaltyMin;
    
    // Tìm kiếm & Lọc
    private JTextField txtSearch;
    private List<JButton> filterButtons = new ArrayList<>();

    public ManHinhKhuyenMai() {
        setBackground(COLOR_BG);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        pnlMain = new JPanel(cardLayout);
        pnlMain.setOpaque(false);

        // Khởi tạo 2 màn hình chính
        pnlMain.add(createListScreen(), "CARD_LIST");
        pnlMain.add(createFormScreen(), "CARD_FORM");

        add(pnlMain, BorderLayout.CENTER);
        
        // Load dữ liệu ban đầu
        loadDataDemo();
    }

    /**
     * MÀN HÌNH DANH SÁCH & THIẾT LẬP
     */
    private JPanel createListScreen() {
        JPanel pnl = new JPanel(new BorderLayout(0, 15));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(15, 20, 15, 20));

        // 1. TOP: KPI
        pnl.add(createTopKPISecton(), BorderLayout.NORTH);

        // 2. CENTER: Loyalty & Table
        JPanel pnlCenter = new JPanel();
        pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));
        pnlCenter.setOpaque(false);

        pnlCenter.add(createLoyaltySection());
        pnlCenter.add(Box.createVerticalStrut(20));
        pnlCenter.add(createFilterAndTableSection());

        pnl.add(pnlCenter, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createTopKPISecton() {
        JPanel pnl = new JPanel(new GridLayout(1, 3, 20, 0));
        pnl.setOpaque(false);
        pnl.setPreferredSize(new Dimension(0, 80));
        pnl.add(new KPICardCustom("Đang hoạt động", "2", COLOR_SUCCESS, 1));
        pnl.add(new KPICardCustom("Sắp diễn ra", "3", COLOR_PRIMARY, 2));
        pnl.add(new KPICardCustom("Đã kết thúc / Tạm dừng", "2", COLOR_GRAY, 3));
        return pnl;
    }

    private JPanel createLoyaltySection() {
        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBackground(COLOR_PURPLE_LIGHT);
        pnlMain.setBorder(new LineBorder(COLOR_PURPLE_BORDER, 1, true));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(10, 15, 5, 15));

        JLabel lblTitle = new JLabel("Chương trình tích điểm thưởng");
        lblTitle.setFont(FONT_BOLD);
        lblTitle.setForeground(COLOR_PURPLE);
        lblTitle.setIcon(new IconDrawer(12, COLOR_PURPLE, "STAR")); 

        JButton btnSaveLoyalty = createWindowsButton("Lưu cài đặt", COLOR_PURPLE, Color.WHITE);
        btnSaveLoyalty.setPreferredSize(new Dimension(100, 30));
        
        // SỰ KIỆN: Lưu cài đặt tích điểm
        btnSaveLoyalty.addActionListener(e -> handleSaveLoyalty());

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(btnSaveLoyalty, BorderLayout.EAST);

        JPanel pnlContent = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlContent.setOpaque(false);
        pnlContent.setBorder(new EmptyBorder(10, 15, 15, 15));

        txtLoyaltyBuy = new JTextField("10000", 5);
        txtLoyaltyExchange = new JTextField("1000", 5);
        txtLoyaltyMin = new JTextField("1000", 5);

        pnlContent.add(createLoyaltyBox("Tích điểm khi mua", txtLoyaltyBuy, "đ = 1 điểm", "Ví dụ: mua 100.000đ -> được 10 điểm", "CHART"));
        pnlContent.add(createLoyaltyBox("Giá trị đổi điểm", txtLoyaltyExchange, "1 điểm =", "đ", "GIFT"));
        pnlContent.add(createLoyaltyBox("Điểm tối thiểu đổi", txtLoyaltyMin, "điểm", "Phải có ít nhất 1000 điểm mới được đổi", "PERCENT"));

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlContent, BorderLayout.CENTER);
        return pnlMain;
    }

    private JPanel createLoyaltyBox(String title, JTextField txt, String labelAfter, String note, String iconType) {
        JPanel box = new JPanel(new BorderLayout(5, 5));
        box.setBackground(Color.WHITE);
        box.setBorder(new LineBorder(Color.decode("#E2E8F0"), 1, true));
        box.setBorder(BorderFactory.createCompoundBorder(box.getBorder(), new EmptyBorder(10, 10, 10, 10)));

        JLabel lblT = new JLabel(title);
        lblT.setFont(FONT_BOLD);
        lblT.setIcon(new IconDrawer(14, COLOR_PURPLE, iconType));

        JPanel pnlInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlInput.setOpaque(false);
        
        if (title.equals("Giá trị đổi điểm")) {
            pnlInput.add(new JLabel("1 điểm = "));
            pnlInput.add(txt);
            pnlInput.add(new JLabel("đ"));
        } else {
            pnlInput.add(txt);
            pnlInput.add(new JLabel(labelAfter));
        }

        JLabel lblNote = new JLabel(note);
        lblNote.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblNote.setForeground(COLOR_GRAY);

        box.add(lblT, BorderLayout.NORTH);
        box.add(pnlInput, BorderLayout.CENTER);
        box.add(lblNote, BorderLayout.SOUTH);
        return box;
    }

    private JPanel createFilterAndTableSection() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setOpaque(false);

        // Thanh lọc & Tìm kiếm
        JPanel pnlFilter = new JPanel(new BorderLayout());
        pnlFilter.setOpaque(false);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlBtns.setOpaque(false);
        pnlBtns.add(new JLabel("Lọc trạng thái:  "));
        
        String[] filters = {"Tất cả", "Đang hoạt động", "Sắp diễn ra", "Đã kết thúc", "Tạm dừng"};
        for (String f : filters) {
            JButton btn = createFilterButton(f, f.equals("Tất cả"));
            filterButtons.add(btn);
            pnlBtns.add(btn);
            
            // SỰ KIỆN: Lọc theo trạng thái
            btn.addActionListener(e -> handleFilterStatus(btn, f));
        }

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRight.setOpaque(false);
        txtSearch = new JTextField(15);
        
        // SỰ KIỆN: Tìm kiếm theo tên (DocumentListener)
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { handleSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { handleSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { handleSearch(); }
        });

        JButton btnRefresh = createWindowsButton("Làm mới", Color.decode("#64748B"), Color.WHITE);
        btnRefresh.addActionListener(e -> handleRefresh());

        JButton btnAddNew = createWindowsButton("+ Thêm mới", COLOR_DANGER, Color.WHITE);
        btnAddNew.addActionListener(e -> cardLayout.show(pnlMain, "CARD_FORM"));

        pnlRight.add(new JLabel("Tìm kiếm: "));
        pnlRight.add(txtSearch);
        pnlRight.add(btnRefresh);
        pnlRight.add(btnAddNew);

        pnlFilter.add(pnlBtns, BorderLayout.WEST);
        pnlFilter.add(pnlRight, BorderLayout.EAST);

        // Bảng dữ liệu
        JPanel pnlTableContainer = new JPanel(new BorderLayout());
        pnlTableContainer.setBackground(Color.WHITE);

        JPanel pnlTableHeader = new JPanel(new BorderLayout());
        pnlTableHeader.setBackground(COLOR_HEADER_TABLE);
        pnlTableHeader.setPreferredSize(new Dimension(0, 35));
        JLabel lblTableTitle = new JLabel("  Danh sách chương trình khuyến mại");
        lblTableTitle.setForeground(Color.WHITE);
        lblTableTitle.setFont(FONT_BOLD);
        pnlTableHeader.add(lblTableTitle, BorderLayout.WEST);
        
        String[] cols = {"#", "Tên chương trình", "Loại", "Giảm giá", "Đơn tối thiểu", "Áp dụng", "Thời gian", "Trạng thái", "Sửa", "Bật/Tắt"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        
        tblKhuyenMai = new JTable(tableModel);
        tblKhuyenMai.setRowHeight(45);
        tblKhuyenMai.setShowVerticalLines(false);
        tblKhuyenMai.setGridColor(Color.decode("#F1F5F9"));
        tblKhuyenMai.setFont(FONT_REGULAR);
        
        // Khởi tạo Sorter
        rowSorter = new TableRowSorter<>(tableModel);
        tblKhuyenMai.setRowSorter(rowSorter);

        // Khóa cột
        tblKhuyenMai.getColumnModel().getColumn(0).setPreferredWidth(30);
        tblKhuyenMai.getColumnModel().getColumn(1).setPreferredWidth(180);
        tblKhuyenMai.getColumnModel().getColumn(7).setPreferredWidth(120);
        tblKhuyenMai.getColumnModel().getColumn(8).setPreferredWidth(40);
        tblKhuyenMai.getColumnModel().getColumn(9).setPreferredWidth(60);

        // Renderers
        tblKhuyenMai.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setForeground(COLOR_DANGER); l.setFont(FONT_BOLD); l.setHorizontalAlignment(SwingConstants.CENTER);
                return l;
            }
        });
        tblKhuyenMai.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());
        tblKhuyenMai.getColumnModel().getColumn(9).setCellRenderer(new ToggleSwitchRenderer());

        // SỰ KIỆN: Click trên bảng (Sửa & Bật/Tắt)
        tblKhuyenMai.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleTableClick(e);
            }
        });

        JScrollPane scroll = new JScrollPane(tblKhuyenMai);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        pnlTableContainer.add(pnlTableHeader, BorderLayout.NORTH);
        pnlTableContainer.add(scroll, BorderLayout.CENTER);

        pnl.add(pnlFilter, BorderLayout.NORTH);
        pnl.add(pnlTableContainer, BorderLayout.CENTER);
        return pnl;
    }

    /**
     * MÀN HÌNH FORM THÊM MỚI
     */
    private JPanel createFormScreen() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(COLOR_BG);
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(30, 50, 30, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel lblTitle = new JLabel("THIẾT LẬP CHƯƠNG TRÌNH MỚI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        pnlForm.add(lblTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; pnlForm.add(new JLabel("Tên chương trình:"), gbc);
        gbc.gridx = 1; pnlForm.add(new JTextField(20), gbc);

        gbc.gridx = 0; gbc.gridy = 2; pnlForm.add(new JLabel("Hình thức:"), gbc);
        gbc.gridx = 1; pnlForm.add(new JComboBox<>(new String[]{"Giảm % hóa đơn", "Giảm tiền mặt", "Tặng quà"}), gbc);

        JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlAction.setOpaque(false);
        JButton btnBack = new JButton("Quay lại");
        btnBack.addActionListener(e -> cardLayout.show(pnlMain, "CARD_LIST"));
        
        JButton btnSave = createWindowsButton("Lưu chương trình", COLOR_PRIMARY, Color.WHITE);
        btnSave.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Đã lưu chương trình mới!");
            cardLayout.show(pnlMain, "CARD_LIST");
        });

        pnlAction.add(btnBack);
        pnlAction.add(btnSave);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        pnlForm.add(pnlAction, gbc);

        pnl.add(pnlForm, BorderLayout.NORTH);
        return pnl;
    }

    // --- XỬ LÝ SỰ KIỆN (EVENT HANDLING) ---

    private void handleSaveLoyalty() {
        String b = txtLoyaltyBuy.getText().trim();
        String e = txtLoyaltyExchange.getText().trim();
        String m = txtLoyaltyMin.getText().trim();

        if (b.isEmpty() || e.isEmpty() || m.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng không để trống các ô thiết lập!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Integer.parseInt(b);
            Integer.parseInt(e);
            Integer.parseInt(m);
            JOptionPane.showMessageDialog(this, "Lưu cấu hình tích điểm thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá trị nhập vào phải là số nguyên dương!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleFilterStatus(JButton btnClicked, String status) {
        // Đổi màu nút
        for (JButton b : filterButtons) {
            b.setBackground(Color.WHITE);
            b.setForeground(Color.BLACK);
            b.setBorder(new LineBorder(Color.decode("#CBD5E1")));
        }
        btnClicked.setBackground(COLOR_ACTIVE_FILTER);
        btnClicked.setForeground(Color.WHITE);
        btnClicked.setBorder(new LineBorder(COLOR_ACTIVE_FILTER));

        // Lọc bảng
        if (status.equals("Tất cả")) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("^" + status + "$", 7));
        }
    }

    private void handleSearch() {
        String text = txtSearch.getText().trim();
        if (text.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            // Lọc theo cột Tên chương trình (cột 1)
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1));
        }
    }

    private void handleRefresh() {
        txtSearch.setText("");
        handleFilterStatus(filterButtons.get(0), "Tất cả");
        loadDataDemo();
        JOptionPane.showMessageDialog(this, "Đã làm mới dữ liệu từ hệ thống!");
    }

    private void handleTableClick(MouseEvent e) {
        int row = tblKhuyenMai.rowAtPoint(e.getPoint());
        int col = tblKhuyenMai.columnAtPoint(e.getPoint());

        if (row < 0) return;
        
        // Chuyển đổi index từ view sang model (quan trọng khi đang lọc)
        int modelRow = tblKhuyenMai.convertRowIndexToModel(row);
        String tenKM = tableModel.getValueAt(modelRow, 1).toString();

        if (col == 8) { // Cột Sửa
            JOptionPane.showMessageDialog(this, "Đang mở form chỉnh sửa cho: " + tenKM);
        } else if (col == 9) { // Cột Bật/Tắt
            boolean currentStatus = (boolean) tableModel.getValueAt(modelRow, 9);
            tableModel.setValueAt(!currentStatus, modelRow, 9);
            tblKhuyenMai.repaint();
        }
    }

    private void loadDataDemo() {
        tableModel.setRowCount(0);
        Object[][] data = {
            {"1", "Giảm 10% Vitamin C", "Giảm phần trăm", "% 10", "100.000đ", "Sản phẩm chức năng", "01/01/2024 - 31/03/2024", "Tạm dừng", "Edit", false},
            {"2", "Tặng 50k đơn từ 500k", "Giảm tiền cố định", "% 50000 đ", "500.000đ", "Tất cả", "15/02/2024 - 28/02/2024", "Tạm dừng", "Edit", false},
            {"3", "Mua 3 tặng 1 Paracetamol", "Mua X tặng Y", "% 1 sản phẩm", "-", "Thuốc không kê đơn", "01/03/2024 - 31/03/2024", "Đang hoạt động", "Edit", true},
            {"4", "Flash Sale cuối tuần", "Giảm phần trăm", "% 20", "-", "Tất cả", "22/03/2024 - 24/03/2024", "Đang hoạt động", "Edit", true},
            {"5", "Ưu đãi thành viên Vàng", "Giảm tiền cố định", "% 100000 đ", "1.000.000đ", "Tất cả", "01/04/2024 - 30/06/2024", "Sắp diễn ra", "Edit", true},
            {"6", "Giảm 15% Omega 3", "Giảm phần trăm", "% 15", "-", "Sản phẩm chức năng", "01/04/2024 - 15/04/2024", "Sắp diễn ra", "Edit", true},
            {"7", "Sinh nhật nhà thuốc", "Giảm phần trăm", "% 25", "200.000đ", "Tất cả", "10/05/2024 - 12/05/2024", "Sắp diễn ra", "Edit", true}
        };
        for (Object[] row : data) tableModel.addRow(row);
    }

    // --- CÁC COMPONENT CUSTOM (GIỮ NGUYÊN TỪ PHẦN TRƯỚC) ---

    private JButton createWindowsButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD); btn.setForeground(fg); btn.setBackground(bg);
        btn.setContentAreaFilled(false); btn.setOpaque(true); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createFilterButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_REGULAR); btn.setFocusPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (active) {
            btn.setBackground(COLOR_ACTIVE_FILTER); btn.setForeground(Color.WHITE);
            btn.setBorder(new LineBorder(COLOR_ACTIVE_FILTER));
        } else {
            btn.setBackground(Color.WHITE); btn.setForeground(Color.BLACK);
            btn.setBorder(new LineBorder(Color.decode("#CBD5E1")));
        }
        return btn;
    }

    private class KPICardCustom extends JPanel {
        private String title, value; private Color color; private int type;
        public KPICardCustom(String title, String value, Color color, int type) {
            this.title = title; this.value = value; this.color = color; this.type = type;
            setBackground(Color.WHITE); setPreferredSize(new Dimension(250, 80));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE); g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
            g2.setColor(color); int iconSize = 40; int iy = (getHeight() - iconSize) / 2;
            g2.fillRoundRect(15, iy, iconSize, iconSize, 10, 10);
            g2.setColor(Color.WHITE);
            if (type == 1) { g2.drawOval(22, iy + 12, 16, 16); g2.fillOval(27, iy + 17, 6, 6); }
            else if (type == 2) { g2.fillRect(22, iy + 12, 26, 20); g2.setColor(color); g2.fillRect(24, iy + 14, 22, 4); }
            else { g2.drawOval(22, iy + 12, 26, 26); g2.drawLine(35, iy + 25, 35, iy + 18); g2.drawLine(35, iy + 25, 42, iy + 25); }
            g2.setColor(COLOR_GRAY); g2.setFont(FONT_REGULAR); g2.drawString(title, 70, 35);
            g2.setColor(Color.BLACK); g2.setFont(new Font("Segoe UI", Font.BOLD, 22)); g2.drawString(value, 70, 60);
            g2.dispose();
        }
    }

    private class IconDrawer implements Icon {
        private int size; private Color color; private String type;
        public IconDrawer(int size, Color color, String type) { this.size = size; this.color = color; this.type = type; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            if (type.equals("STAR")) { int[] px = {x+size/2, x+size, x, x+size, x}; int[] py = {y, y+size, y+size/3, y+size/3, y+size}; g2.fillPolygon(px, py, 5); }
            else if (type.equals("CHART")) { g2.fillRect(x, y+size-4, 4, 4); g2.fillRect(x+6, y+size-8, 4, 8); g2.fillRect(x+12, y+size-12, 4, 12); }
            else if (type.equals("GIFT")) { g2.drawRect(x, y+4, size, size-4); g2.drawLine(x, y+size/2+2, x+size, y+size/2+2); g2.drawLine(x+size/2, y+4, x+size/2, y+size); }
            else { g2.setFont(new Font("Arial", Font.BOLD, size)); g2.drawString("%", x, y + size); }
            g2.dispose();
        }
        @Override public int getIconWidth() { return size + 5; }
        @Override public int getIconHeight() { return size; }
    }

    private class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String status = value.toString();
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            l.setHorizontalAlignment(SwingConstants.CENTER); l.setFont(new Font("Segoe UI", Font.BOLD, 11));
            if (status.equals("Đang hoạt động")) { l.setForeground(COLOR_SUCCESS); l.setText("<html><div style='background:#DCFCE7; padding:2px 8px; border-radius:10px;'>Đang hoạt động</div></html>"); }
            else if (status.equals("Sắp diễn ra")) { l.setForeground(COLOR_PRIMARY); l.setText("<html><div style='background:#DBEAFE; padding:2px 8px; border-radius:10px;'>Sắp diễn ra</div></html>"); }
            else { l.setForeground(COLOR_GRAY); l.setText("<html><div style='background:#F1F5F9; padding:2px 8px; border-radius:10px;'>" + status + "</div></html>"); }
            return l;
        }
    }

    private class ToggleSwitchRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            boolean isOn = (boolean) value;
            JPanel pnl = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = 36, h = 18; int x = (getWidth() - w) / 2; int y = (getHeight() - h) / 2;
                    g2.setColor(isOn ? COLOR_SUCCESS : Color.decode("#CBD5E1")); g2.fillRoundRect(x, y, w, h, h, h);
                    g2.setColor(Color.WHITE); int dotX = isOn ? x + w - 16 : x + 2; g2.fillOval(dotX, y + 2, 14, 14);
                    g2.dispose();
                }
            };
            pnl.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return pnl;
        }
    }
}