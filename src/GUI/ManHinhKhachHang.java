package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

import Utils.MenuIcon;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ManHinhKhachHang extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;
    
    private JLabel lblTotalKhachHang; 
    private JLabel lblTotalPoints; // Biến quản lý số điểm hiển thị

    private JPanel pnlDetail;
    private JLabel lblDetAvatar, lblDetName, lblDetId;
    private JLabel lblDetGenderDOB, lblDetPhone, lblDetEmail, lblDetAddress;
    private JLabel lblDetOrders, lblDetPoints, lblDetTotalSpend, lblDetLastVisit;
    private JButton btnEdit;
    private JButton btnAdd;

    public ManHinhKhachHang() {
        initUI();
    }

    private void initUI() {
        this.setLayout(new BorderLayout(0, 20));
        this.setBackground(Color.decode("#F3F4F6")); 
        this.setBorder(new EmptyBorder(20, 25, 20, 25));
        btnAdd = new JButton("Thêm khách hàng");
        
        // ==================== 1. PHẦN ĐẦU ====================
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.decode("#1E293B"));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setOpaque(false);

        JPanel pnlSearchWrapper = new JPanel(new BorderLayout(8, 0));
        pnlSearchWrapper.setBackground(Color.WHITE);
        pnlSearchWrapper.setPreferredSize(new Dimension(280, 40));
        pnlSearchWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)));

        pnlSearchWrapper.add(new JLabel(new MenuIcon("SEARCH")), BorderLayout.WEST);

        String placeholder = "Tên, mã, SĐT...";
        txtSearch = new JTextField(placeholder);
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setBorder(null);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals(placeholder)) { txtSearch.setText(""); txtSearch.setForeground(Color.BLACK); }
            }
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) { txtSearch.setText(placeholder); txtSearch.setForeground(Color.GRAY); }
            }
        });
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { applyFilter(); }
        });
        pnlSearchWrapper.add(txtSearch, BorderLayout.CENTER);

        JButton btnReset = createActionBtn("Làm mới", "#6C757D", "REFRESH");
        JButton btnAdd = createActionBtn("+ Thêm KH", "#E11D48", null);
        btnAdd.addActionListener(e -> {
            Window p = SwingUtilities.getWindowAncestor(this);
            ThemKhachHang dialog = new ThemKhachHang((Frame) p, model);
            dialog.setVisible(true);
        });
        
        pnlActions.add(pnlSearchWrapper);
        pnlActions.add(btnReset);
        pnlActions.add(btnAdd);
        pnlHeader.add(pnlActions, BorderLayout.EAST);

        // ==================== 2. THẺ THỐNG KÊ ====================
        JPanel pnlCards = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlCards.setOpaque(false);
        pnlCards.setPreferredSize(new Dimension(0, 90));
        
        // ĐÃ THÊM LẠI 2 DÒNG NÀY ĐỂ KHỞI TẠO LABEL THÀNH CÔNG
        pnlCards.add(createSummaryCard("Tổng khách hàng", "0", 1)); 
        pnlCards.add(createSummaryCard("Tổng điểm tích lũy", "0", 2));

        JPanel pnlTop = new JPanel(new BorderLayout(0, 20));
        pnlTop.setOpaque(false);
        pnlTop.add(pnlHeader, BorderLayout.NORTH);
        pnlTop.add(pnlCards, BorderLayout.CENTER);

        this.add(pnlTop, BorderLayout.NORTH);

        // ==================== 3. KHU VỰC BẢNG & SIDEBAR ====================
        JPanel pnlCenter = new JPanel(new BorderLayout(20, 0)); 
        pnlCenter.setOpaque(false);

        // -- BẢNG DỮ LIỆU --
        String[] cols = {"Mã KH", "Họ tên", "Điện thoại", "Đơn hàng", "Chi tiêu", "Điểm", "Lần cuối", "Thao tác"};
        model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        
        table.setRowHeight(50);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Color.decode("#F1F3F5"));
        table.setSelectionBackground(Color.decode("#E0F2FE")); 

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 45));
        header.setBackground(Color.decode("#E0F2FE")); 
        header.setForeground(Color.decode("#1E293B"));
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));

        table.setDefaultRenderer(Object.class, new CustomerTableRenderer());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8")));
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected JButton createDecreaseButton(int o) { return createZeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return createZeroButton(); }
            private JButton createZeroButton() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle t) {
                Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#F8F9FA")); g2.fillRect(t.x, t.y, t.width, t.height);
            }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle t) {
                if (t.isEmpty() || !scrollbar.isEnabled()) return;
                Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isDragging) g2.setColor(Color.decode("#94A3B8")); else if (isThumbRollover()) g2.setColor(Color.decode("#CBD5E1")); else g2.setColor(Color.decode("#E2E8F0"));
                g2.fillRoundRect(t.x + 2, t.y + 2, t.width - 4, t.height - 4, 8, 8);
            }
        });
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));

        // -- SIDEBAR CHI TIẾT --
        pnlDetail = createDetailSidebar();
        pnlDetail.setVisible(false); 

        pnlCenter.add(sp, BorderLayout.CENTER);
        pnlCenter.add(pnlDetail, BorderLayout.EAST);
        this.add(pnlCenter, BorderLayout.CENTER);

        // --- SỰ KIỆN CLICK BẢNG ---
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int modelRow = table.convertRowIndexToModel(row);
                    updateDetailSidebar(modelRow); 
                    pnlDetail.setVisible(true);    
                    revalidate();
                    repaint();
                }
            }
        });

        // --- SỰ KIỆN CLICK NÚT SỬA ---
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                String hoten = model.getValueAt(modelRow, 1).toString();
                String sdt = model.getValueAt(modelRow, 2).toString();
                
                Window p = SwingUtilities.getWindowAncestor(this);
                ThemKhachHang dialog = new ThemKhachHang((Frame) p, model, modelRow, hoten, sdt);
                dialog.setVisible(true);
                
                updateDetailSidebar(modelRow);
            }
        });

        model.addTableModelListener(e -> {
            if (lblTotalKhachHang != null) lblTotalKhachHang.setText(String.valueOf(model.getRowCount()));
            tinhTongDiemTichLuy(); // Đảm bảo tổng điểm trên cùng cũng được cập nhật
            
            // --- THÊM LOGIC MỚI: Tự động cập nhật điểm lên thanh Sidebar nếu nó đang mở ---
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                SwingUtilities.invokeLater(() -> {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        int modelRow = table.convertRowIndexToModel(selectedRow);
                        // Kiểm tra nếu dòng bị thay đổi dữ liệu trùng với dòng khách hàng đang chọn xem
                        if (e.getFirstRow() == modelRow && pnlDetail != null && pnlDetail.isVisible()) {
                            updateDetailSidebar(modelRow); // Ép thanh bên phải load lại điểm ngay lập tức
                        }
                    }
                });
            }
            
            if (e.getType() == javax.swing.event.TableModelEvent.INSERT) {
                SwingUtilities.invokeLater(() -> {
                    int lastRow = table.getRowCount() - 1;
                    table.setRowSelectionInterval(lastRow, lastRow);
                    table.scrollRectToVisible(table.getCellRect(lastRow, 0, true));
                });
            }
        });

        loadData();
    }

    private void tinhTongDiemTichLuy() {
        long tongDiem = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                String diemStr = model.getValueAt(i, 5).toString().replace(".", "").replace(",", "");
                tongDiem += Long.parseLong(diemStr);
            } catch (Exception ex) {
                // Bỏ qua lỗi parse số
            }
        }
        
        if (lblTotalPoints != null) {
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
            lblTotalPoints.setText(df.format(tongDiem));
        }
    }

    private JPanel createDetailSidebar() {
        JPanel pnl = new JPanel(new BorderLayout());
        
        pnl.setPreferredSize(new Dimension(270, 0)); 
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1));

        // --- 1.1 HEADER (Avatar, Tên, ID) ---
        JPanel pnlDetHeader = new JPanel(new BorderLayout(10, 0));
        pnlDetHeader.setBackground(Color.WHITE);
        pnlDetHeader.setBorder(new EmptyBorder(15, 15, 10, 10));

        lblDetAvatar = new JLabel("N", SwingConstants.CENTER);
        lblDetAvatar.setOpaque(true);
        lblDetAvatar.setBackground(Color.decode("#152A4B"));
        lblDetAvatar.setForeground(Color.WHITE);
        lblDetAvatar.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblDetAvatar.setPreferredSize(new Dimension(45, 45));

        JPanel pnlName = new JPanel(new GridLayout(2, 1));
        pnlName.setBackground(Color.WHITE);
        lblDetName = new JLabel("Nguyễn Văn An");
        lblDetName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDetId = new JLabel("KH2024-0001");
        lblDetId.setForeground(Color.GRAY);
        pnlName.add(lblDetName);
        pnlName.add(lblDetId);

        JButton btnCloseDet = new JButton("×"); 
        btnCloseDet.setFont(new Font("Arial", Font.BOLD, 20));
        btnCloseDet.setForeground(Color.GRAY);
        btnCloseDet.setBorderPainted(false);
        btnCloseDet.setContentAreaFilled(false);
        btnCloseDet.setFocusPainted(false);
        btnCloseDet.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCloseDet.addActionListener(e -> pnlDetail.setVisible(false));

        pnlDetHeader.add(lblDetAvatar, BorderLayout.WEST);
        pnlDetHeader.add(pnlName, BorderLayout.CENTER);
        pnlDetHeader.add(btnCloseDet, BorderLayout.EAST);

        // --- 1.2 TABS ---
        JPanel pnlTabs = new JPanel(new GridLayout(1, 2));
        pnlTabs.setBackground(Color.WHITE);
        pnlTabs.setPreferredSize(new Dimension(0, 35));
        
        JLabel lblTab1 = new JLabel("Thông tin", SwingConstants.CENTER);
        lblTab1.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTab1.setForeground(Color.decode("#1967D2"));
        lblTab1.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#1967D2")));
        
        JLabel lblTab2 = new JLabel("Lịch sử điểm", SwingConstants.CENTER);
        lblTab2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTab2.setForeground(Color.GRAY);
        lblTab2.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));
        
        pnlTabs.add(lblTab1);
        pnlTabs.add(lblTab2);

        // --- 1.3 BODY ---
        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);

        JPanel pnlInfoList = new JPanel(new GridLayout(4, 1, 0, 10));
        pnlInfoList.setBackground(Color.WHITE);
        pnlInfoList.setBorder(new EmptyBorder(15, 15, 15, 15));
        pnlInfoList.setAlignmentX(Component.LEFT_ALIGNMENT); 

        lblDetGenderDOB = new JLabel(" Nam • 01/01/1990");
        lblDetGenderDOB.setIcon(new MenuIcon("USER"));
        lblDetPhone = new JLabel(" 0910000000");
        lblDetPhone.setIcon(new MenuIcon("PHONE"));
        lblDetEmail = new JLabel(" khachhang@email.com");
        lblDetEmail.setIcon(new MenuIcon("MAIL"));
        lblDetAddress = new JLabel(" TP. Hồ Chí Minh");
        lblDetAddress.setIcon(new MenuIcon("LOCATION"));

        for(JLabel l : new JLabel[]{lblDetGenderDOB, lblDetPhone, lblDetEmail, lblDetAddress}) {
            l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            l.setForeground(Color.decode("#4B5563"));
            l.setIconTextGap(10);
            pnlInfoList.add(l);
        }

        JPanel pnlStats = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlStats.setBackground(Color.WHITE);
        pnlStats.setBorder(new EmptyBorder(0, 15, 15, 15));
        pnlStats.setMaximumSize(new Dimension(1000, 70));
        pnlStats.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel box1 = new JPanel(new GridLayout(2, 1));
        box1.setBackground(Color.decode("#F0F9FF")); 
        box1.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel l1 = new JLabel("Đơn hàng", SwingConstants.CENTER);
        l1.setForeground(Color.GRAY);
        lblDetOrders = new JLabel("6", SwingConstants.CENTER);
        lblDetOrders.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblDetOrders.setForeground(Color.decode("#1967D2"));
        box1.add(l1); box1.add(lblDetOrders);

        JPanel box2 = new JPanel(new GridLayout(2, 1));
        box2.setBackground(Color.decode("#FAF5FF")); 
        box2.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel l2 = new JLabel("Điểm hiện có", SwingConstants.CENTER);
        l2.setForeground(Color.GRAY);
        lblDetPoints = new JLabel("200", SwingConstants.CENTER);
        lblDetPoints.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblDetPoints.setForeground(Color.decode("#9333EA"));
        box2.add(l2); box2.add(lblDetPoints);

        pnlStats.add(box1); pnlStats.add(box2);

        JPanel pnlSpend = new JPanel();
        pnlSpend.setLayout(new BoxLayout(pnlSpend, BoxLayout.Y_AXIS));
        pnlSpend.setBackground(Color.WHITE);
        pnlSpend.setBorder(new EmptyBorder(0, 15, 10, 15)); 
        pnlSpend.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSpendText = new JLabel("Tổng chi tiêu:");
        lblSpendText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSpendText.setForeground(Color.GRAY);
        lblSpendText.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblDetTotalSpend = new JLabel("1.600.000đ");
        lblDetTotalSpend.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDetTotalSpend.setForeground(Color.decode("#16A34A")); 
        lblDetTotalSpend.setBorder(new EmptyBorder(2, 0, 4, 0));
        lblDetTotalSpend.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblDetLastVisit = new JLabel("Lần cuối: 01/01/2024");
        lblDetLastVisit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDetLastVisit.setForeground(Color.GRAY);
        lblDetLastVisit.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlSpend.add(lblSpendText);
        pnlSpend.add(lblDetTotalSpend);
        pnlSpend.add(lblDetLastVisit);

        pnlBody.add(pnlInfoList);
        pnlBody.add(pnlStats);
        pnlBody.add(pnlSpend);
        
        JScrollPane spBody = new JScrollPane(pnlBody);
        spBody.setBorder(null);
        spBody.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0)); 

        // --- 1.4 FOOTER ---
        JPanel pnlFooterActions = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlFooterActions.setBackground(Color.WHITE);
        pnlFooterActions.setBorder(new EmptyBorder(10, 15, 15, 15));

        btnEdit = new JButton("Chỉnh sửa");
        btnEdit.setIcon(new MenuIcon("EDIT"));
        btnEdit.setIconTextGap(8);
        btnEdit.setBackground(Color.decode("#1967D2"));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setFocusPainted(false);
        btnEdit.setBorderPainted(false);
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEdit.setPreferredSize(new Dimension(0, 36));
        btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnDelete = new JButton("Xóa khách hàng");
        btnDelete.setIcon(new MenuIcon("TRASH"));
        btnDelete.setIconTextGap(8);
        btnDelete.setBackground(Color.decode("#FEF2F2")); 
        btnDelete.setForeground(Color.decode("#DC2626"));
        btnDelete.setFocusPainted(false);
        btnDelete.setBorder(BorderFactory.createLineBorder(Color.decode("#FECACA")));
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDelete.setPreferredSize(new Dimension(0, 36));
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnlFooterActions.add(btnEdit);
        pnlFooterActions.add(btnDelete);

        JPanel pnlTopWrap = new JPanel(new BorderLayout());
        pnlTopWrap.add(pnlDetHeader, BorderLayout.NORTH);
        pnlTopWrap.add(pnlTabs, BorderLayout.SOUTH);

        pnl.add(pnlTopWrap, BorderLayout.NORTH);
        pnl.add(spBody, BorderLayout.CENTER);
        pnl.add(pnlFooterActions, BorderLayout.SOUTH);

        return pnl;
    }

    private void updateDetailSidebar(int modelRow) {
        String id = model.getValueAt(modelRow, 0).toString();
        String name = model.getValueAt(modelRow, 1).toString();
        String phone = model.getValueAt(modelRow, 2).toString();
        String orders = model.getValueAt(modelRow, 3).toString();
        String spend = model.getValueAt(modelRow, 4).toString();
        String points = model.getValueAt(modelRow, 5).toString();
        String lastVisit = model.getValueAt(modelRow, 6).toString();

        lblDetAvatar.setText(name.substring(0, 1).toUpperCase()); 
        lblDetName.setText(name);
        lblDetId.setText(id);

        String gender = name.contains("Thị") ? "Nữ" : "Nam"; 
        lblDetGenderDOB.setText(" " + gender + " • 01/01/1990");
        lblDetPhone.setText(" " + phone);
        
        String dummyEmail = "khachhang." + id.split("-")[1] + "@email.com";
        lblDetEmail.setText(" " + dummyEmail.toLowerCase());

        lblDetOrders.setText(orders);
        lblDetPoints.setText(points);
        lblDetTotalSpend.setText(spend);
        lblDetLastVisit.setText("Lần cuối: " + lastVisit);
    }

    private JPanel createSummaryCard(String title, String value, int type) {
        JPanel pnl = new JPanel(new GridLayout(2, 1, 0, 5));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1),
            new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(Color.decode("#6B7280"));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(Color.decode("#111827"));

        if (type == 1) lblTotalKhachHang = lblValue; 
        if (type == 2) lblTotalPoints = lblValue; 

        pnl.add(lblTitle);
        pnl.add(lblValue);
        return pnl;
    }

    class CustomerTableRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasF, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasF, r, c);
            if (c == 3 || c == 5 || c == 6 || c == 7) lbl.setHorizontalAlignment(CENTER);
            else lbl.setHorizontalAlignment(LEFT);

            lbl.setOpaque(true);
            lbl.setBackground(isSel ? Color.decode("#E0F2FE") : Color.WHITE);
            lbl.setForeground(Color.decode("#374151")); 
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lbl.setBorder(new EmptyBorder(0, 10, 0, 10)); 
            lbl.setIcon(null);

            if (c == 0) lbl.setForeground(Color.decode("#1967D2")); 
            if (c == 1) lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
            if (c == 4) { lbl.setForeground(Color.decode("#1D4ED8")); lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); }
            if (c == 5) { lbl.setForeground(Color.decode("#9333EA")); lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); }
            if (c == 7) { 
                lbl.setText(""); 
                lbl.setIcon(new MenuIcon("EDIT")); 
                lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            return lbl;
        }
    }

    private JButton createActionBtn(String txt, String hexColor, String iconName) {
        JButton btn = new JButton(txt);
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setBackground(Color.decode(hexColor));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        if (iconName != null) { btn.setIcon(new MenuIcon(iconName)); btn.setIconTextGap(6); }
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void applyFilter() {
        String search = txtSearch.getText().trim();
        if (search.isEmpty() || search.equals("Tên, mã, SĐT...")) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + search));
    }

    public void moFormThemMoi() {
        Window p = SwingUtilities.getWindowAncestor(this);
        ThemKhachHang dialog = new ThemKhachHang((Frame) p, model);
        dialog.setVisible(true);
    }

    public DefaultTableModel getModel() {
        return model;
    }

    private void loadData() {
        model.setRowCount(0);
        
        DAO.DAO_KhachHang daoKH = new DAO.DAO_KhachHang();
        List<Object[]> dsKhachHang = daoKH.layDanhSachKhachHangChoBang();
        
        for (Object[] row : dsKhachHang) {
            model.addRow(row);
        }
        
        if (lblTotalKhachHang != null) {
            lblTotalKhachHang.setText(String.valueOf(model.getRowCount()));
        }
        tinhTongDiemTichLuy();
    }
}