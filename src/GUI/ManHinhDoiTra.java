package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import Utils.MenuIcon;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import BUS.*;

public class ManHinhDoiTra extends JPanel {
    
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton[] statusBtns;
    private String filterStatus = "Tất cả";
    private JTextField txtSearch;
    private BUS_HoaDon busHD = new BUS_HoaDon();
    private JPanel pnlDetail;
    private JLabel lblDetailTitle, lblDetailDate, lblDetailEmp, lblDetailTotal;
    private JPanel pnlDetailProducts;
    public ManHinhDoiTra() {
        initUI();
        loadDataToTable();
    }

    private void initUI() {
        this.setLayout(new BorderLayout(0, 0));
        this.setBackground(Color.WHITE);
        this.setBorder(new EmptyBorder(0, 30, 20, 30));

        // ==================== 1. THANH TABS ====================
        JPanel pnlTabs = new JPanel(new BorderLayout());
        pnlTabs.setBackground(Color.WHITE);
        pnlTabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));

        JPanel pnlLeftTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlLeftTabs.setOpaque(false);

        JLabel lblBanHang = new JLabel("Bán hàng");
        lblBanHang.setIcon(new MenuIcon("CART"));
        lblBanHang.setIconTextGap(8);
        lblBanHang.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblBanHang.setForeground(Color.decode("#6C757D"));
        lblBanHang.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 40));
        lblBanHang.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        lblBanHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { 
                chuyenManHinh("Bán hàng & Đổi trả");
            }
        });

        JLabel lblDoiTra = new JLabel("Đổi / Trả hàng");
        lblDoiTra.setIcon(new MenuIcon("BOX"));
        lblDoiTra.setIconTextGap(8);
        lblDoiTra.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDoiTra.setForeground(Color.decode("#E11D48")); 
        lblDoiTra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, Color.decode("#E11D48")),
                BorderFactory.createEmptyBorder(20, 0, 20, 40)));

        JLabel lblDanhSachHD = new JLabel("Danh Sách Hóa Đơn");
        lblDanhSachHD.setIcon(new MenuIcon("LIST")); 
        lblDanhSachHD.setIconTextGap(8);
        lblDanhSachHD.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblDanhSachHD.setForeground(Color.decode("#6C757D"));
        lblDanhSachHD.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 40));
        lblDanhSachHD.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        lblDanhSachHD.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { 
                chuyenManHinh("DanhSachHD");
            }
        });

        pnlLeftTabs.add(lblBanHang);
        pnlLeftTabs.add(lblDoiTra);
        pnlLeftTabs.add(lblDanhSachHD); 
        pnlTabs.add(pnlLeftTabs, BorderLayout.WEST);

        // ==================== 2. HEADER (TITLE & SEARCH) ====================
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false); // Nền sẽ trong suốt để nhìn thấy màu xám của cha
        pnlHeader.setBorder(new EmptyBorder(25, 0, 15, 0));

        JLabel lblTitle = new JLabel("ĐỔI / TRẢ HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.decode("#212B36"));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setOpaque(false);

        JPanel pnlSearchWrapper = new JPanel(new BorderLayout(8, 0));
        pnlSearchWrapper.setBackground(Color.WHITE);
        pnlSearchWrapper.setPreferredSize(new Dimension(300, 45));
        pnlSearchWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)));

        pnlSearchWrapper.add(new JLabel(new MenuIcon("SEARCH")), BorderLayout.WEST);

        String placeholder = "Mã phiếu, hóa đơn...";
        txtSearch = new JTextField(placeholder);
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setBorder(null);
        txtSearch.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if (txtSearch.getText().equals(placeholder)) { txtSearch.setText(""); txtSearch.setForeground(Color.BLACK); } }
            public void focusLost(FocusEvent e) { if (txtSearch.getText().isEmpty()) { txtSearch.setText(placeholder); txtSearch.setForeground(Color.GRAY); } }
        });
        txtSearch.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { applyFilter(); } });
        pnlSearchWrapper.add(txtSearch, BorderLayout.CENTER);

        // FIX: Đổi màu nút Làm mới cho giống Ảnh 2
        JButton btnReset = createActionBtn("Làm mới", "#6C757D");
        btnReset.setIcon(new MenuIcon("REFRESH"));
        btnReset.addActionListener(e -> {
            txtSearch.setText(placeholder);
            txtSearch.setForeground(Color.GRAY);
            loadDataToTable();
            xuLyStatus(statusBtns[0]);
        });
        
        JButton btnCreate = createActionBtn("Tạo phiếu", "#E11D48"); 
        btnCreate.addActionListener(e -> {
            Window p = SwingUtilities.getWindowAncestor(this);
            TaoPhieuDoiTra dialogTaoPhieu = new TaoPhieuDoiTra((Frame) p, model);
            dialogTaoPhieu.setVisible(true);
        });
        
        pnlActions.add(pnlSearchWrapper);
        pnlActions.add(btnReset);
        pnlActions.add(btnCreate);
        pnlHeader.add(pnlActions, BorderLayout.EAST);

        // ==================== 3. BỘ LỌC TRẠNG THÁI ====================
        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlFilters.setOpaque(false);
        pnlFilters.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        statusBtns = new JButton[]{
            createFilterBtn("Tất cả", true), 
            createFilterBtn("Chờ xử lý", false),
            createFilterBtn("Đang xử lý", false), 
            createFilterBtn("Hoàn thành", false), 
            createFilterBtn("Từ chối", false)
        };
        for (JButton b : statusBtns) { 
            pnlFilters.add(b); 
            b.addActionListener(e -> xuLyStatus(b)); 
        }

        JPanel pnlNorth = new JPanel();
        pnlNorth.setOpaque(false);
        pnlNorth.setLayout(new BoxLayout(pnlNorth, BoxLayout.Y_AXIS));
        pnlNorth.add(pnlTabs);
        pnlNorth.add(pnlHeader);
        pnlNorth.add(pnlFilters);
        this.add(pnlNorth, BorderLayout.NORTH);

        // ==================== 4. BẢNG DỮ LIỆU ĐỔI TRẢ ====================
        String[] cols = {"Mã phiếu", "Hóa đơn gốc", "Khách hàng", "Loại", "Lỗi", "Tiền hoàn", "Chênh lệch ĐH", "Trạng thái", "Ngày", "Xử lý"};
        model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setRowHeight(55);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Color.decode("#DFE3E8")); // Đường kẻ xám nhạt như Ảnh 2
        table.setSelectionBackground(Color.decode("#F4F6F8")); 

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(100, 45));
        header.setBackground(Color.decode("#D9EAF7")); // MÀU XANH NHẠT ĐẸP
        header.setForeground(Color.decode("#1E293B"));
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        table.setDefaultRenderer(Object.class, new DoiTraTableRenderer());
        
        table.getColumnModel().getColumn(0).setPreferredWidth(80);  
        table.getColumnModel().getColumn(1).setPreferredWidth(120); 
        table.getColumnModel().getColumn(2).setPreferredWidth(120); 
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  
        table.getColumnModel().getColumn(4).setPreferredWidth(230); 
        table.getColumnModel().getColumn(5).setPreferredWidth(100); 
        table.getColumnModel().getColumn(6).setPreferredWidth(110); 
        table.getColumnModel().getColumn(9).setPreferredWidth(190);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                
                if (row >= 0 && col == 9) {
                    int modelRow = table.convertRowIndexToModel(row);
                    String status = model.getValueAt(modelRow, 7).toString(); 
                    
                    if (status.equals("Chờ xử lý")) {
                        String maPhieu = model.getValueAt(modelRow, 0).toString();
                        String loai = model.getValueAt(modelRow, 3).toString();

                        Rectangle cellRect = table.getCellRect(row, col, false);
                        int clickX = e.getX() - cellRect.x;
                        
                        if (clickX < cellRect.width / 2) {
                            boolean isConfirm = showCustomConfirmDialog("TIẾP NHẬN", "Xác nhận TIẾP NHẬN phiếu " + loai + " này và cập nhật doanh thu?");
                            if (isConfirm) {
                                BUS_TraHang busTra = new BUS_TraHang();
                                if (busTra.xacNhanGiaoDichDoiTra(maPhieu, "Hoàn thành")) {
                                    model.setValueAt("Hoàn thành", modelRow, 7);
                                    table.repaint();
                                    showCustomNotification("THÀNH CÔNG", "Xử lý thành công! Doanh thu đã được cập nhật.", "SUCCESS");
                                } else {
                                    showCustomNotification("LỖI DB", "Có lỗi xảy ra khi cập nhật Database!", "ERROR");
                                }
                            }
                        } else {
                            boolean isConfirm = showCustomConfirmDialog("TỪ CHỐI", "Xác nhận TỪ CHỐI phiếu này?");
                            if (isConfirm) {
                                BUS_TraHang busTra = new BUS_TraHang();
                                if (busTra.xacNhanGiaoDichDoiTra(maPhieu, "Từ chối")) {
                                    model.setValueAt("Từ chối", modelRow, 7);
                                    table.repaint();
                                    showCustomNotification("ĐÃ TỪ CHỐI", "Phiếu giao dịch này đã bị hủy.", "WARNING");
                                }
                            }
                        }
                    }
                }
                // ĐÃ XÓA PHẦN ELSE IF TẠI ĐÂY
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8")));
        sp.getViewport().setBackground(Color.WHITE); // Tẩy trắng nền
        
        // THAY VÌ BỌC VÀO PNLTABLEAREA, ADD TRỰC TIẾP SP VÀO PANEL CHÍNH
        this.add(sp, BorderLayout.CENTER);
    }
    
    private void loadDataToTable() {
        model.setRowCount(0); 
        BUS_TraHang busTraHang = new BUS_TraHang();
        List<Object[]> dsPhieu = busTraHang.layDanhSachPhieu();
        for (Object[] row : dsPhieu) {
            model.addRow(row); 
        }
    }

    private void chuyenManHinh(String tenManHinh) {
        SwingUtilities.invokeLater(() -> {
            Container parent = this.getParent();
            while (parent != null && !(parent.getLayout() instanceof CardLayout)) {
                parent = parent.getParent();
            }
            
            if (parent != null) {
                CardLayout cl = (CardLayout) parent.getLayout();
                cl.show(parent, tenManHinh);
                
                Container topLevel = parent.getParent();
                while (topLevel != null && !(topLevel instanceof MainDashboard)) {
                    topLevel = topLevel.getParent();
                }
                if (topLevel instanceof MainDashboard) {
                    if (tenManHinh.equals("Bán hàng & Đổi trả") || 
                        tenManHinh.equals("DanhSachHD") || 
                        tenManHinh.equals("DoiTra")) {
                        ((MainDashboard) topLevel).chuyenSangTabBanHang(); 
                    }
                }
            }
        });
    }
    
    // --- BỔ SUNG 3: RENDERER MỚI CÓ CHỨA NÚT BẤM VÀ MÀU SẮC MỚI ---
    class DoiTraTableRenderer extends DefaultTableCellRenderer {
        
        JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 12));
        JButton btnTiepNhan = new JButton("Tiếp nhận");
        JButton btnTuChoi = new JButton("Từ chối");
        
        public DoiTraTableRenderer() {
            pnlAction.setOpaque(true);
            
            btnTiepNhan.setBackground(Color.decode("#3B82F6"));
            btnTiepNhan.setForeground(Color.WHITE);
            btnTiepNhan.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnTiepNhan.setBorderPainted(false);
            btnTiepNhan.setFocusPainted(false);
            btnTiepNhan.setPreferredSize(new Dimension(85, 30));
            btnTiepNhan.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            btnTuChoi.setBackground(Color.decode("#EF4444"));
            btnTuChoi.setForeground(Color.WHITE);
            btnTuChoi.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnTuChoi.setBorderPainted(false);
            btnTuChoi.setFocusPainted(false);
            btnTuChoi.setPreferredSize(new Dimension(75, 30));
            btnTuChoi.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            pnlAction.add(btnTiepNhan);
            pnlAction.add(btnTuChoi);
        }

        public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasF, int r, int c) {
            
            if (c == 9) {
                String status = t.getValueAt(r, 7).toString(); 
                pnlAction.setBackground(isSel ? Color.decode("#F4F6F8") : Color.WHITE);
                
                if (status.equals("Chờ xử lý")) {
                    return pnlAction; 
                } else {
                    JLabel empty = new JLabel();
                    empty.setOpaque(true);
                    empty.setBackground(isSel ? Color.decode("#F4F6F8") : Color.WHITE);
                    return empty;
                }
            }
            
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasF, r, c);
            lbl.setHorizontalAlignment(CENTER);
            lbl.setOpaque(true);
            lbl.setBackground(isSel ? Color.decode("#F4F6F8") : Color.WHITE);
            lbl.setForeground(Color.decode("#212B36"));

            if (c == 0) lbl.setForeground(Color.decode("#1967D2")); 
            
            if (c == 3 && v != null && !v.toString().equals("")) { 
                lbl.setBackground(v.toString().equals("Trả hàng") ? Color.decode("#FEE2E2") : Color.decode("#E0F2FE"));
                lbl.setForeground(v.toString().equals("Trả hàng") ? Color.decode("#EF4444") : Color.decode("#0284C7"));
            }
            
            // FIX MÀU: Đồng bộ màu vàng cam như trong ảnh
            if (c == 4 && v != null) { 
                String loi = v.toString();
                if (loi.contains("100%") || loi.contains("NSX") || loi.contains("phác đồ") || loi.contains("cận date") || loi.contains("80%") || loi.contains("đổi ý")) {
                    lbl.setBackground(Color.decode("#FFEDD5")); // Nền vàng cam
                    lbl.setForeground(Color.decode("#D97706")); // Chữ vàng cam đậm
                } 
                else {
                    lbl.setBackground(Color.decode("#F3F4F6")); 
                    lbl.setForeground(Color.decode("#4B5563")); 
                }
            }

            if (c == 5 && v != null && !v.toString().equals("---")) { 
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lbl.setForeground(Color.decode("#DC2626"));
            }
            
            if (c == 6 && v != null && !v.toString().equals("---")) { 
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lbl.setForeground(Color.decode("#DC2626"));
            }

            if (c == 7) { 
                if (v.equals("Hoàn thành")) { lbl.setBackground(Color.decode("#DCFCE7")); lbl.setForeground(Color.decode("#10B981")); }
                else if (v.equals("Chờ xử lý")) { lbl.setBackground(Color.decode("#FEF3C7")); lbl.setForeground(Color.decode("#D97706")); } 
                else if (v.equals("Từ chối")) { lbl.setBackground(Color.decode("#FEE2E2")); lbl.setForeground(Color.decode("#EF4444")); } 
                else { lbl.setBackground(Color.decode("#F3F4F6")); lbl.setForeground(Color.decode("#6B7280")); }
            }
            return lbl;
        }
    }

    private void xuLyStatus(JButton b) {
        for (JButton btn : statusBtns) setBtnNormal(btn);
        setBtnActive(b);
        filterStatus = b.getText().trim();
        applyFilter();
    }

    private void applyFilter() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (!filterStatus.equals("Tất cả")) filters.add(RowFilter.regexFilter("^" + filterStatus + "$", 7));
        String search = txtSearch.getText().trim();
        if (!search.isEmpty() && !search.equals("Mã phiếu, hóa đơn...")) filters.add(RowFilter.regexFilter("(?i)" + search));
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private JButton createActionBtn(String txt, String hex) {
        JButton btn = new JButton(txt);
        btn.setPreferredSize(new Dimension(130, 45));
        btn.setBackground(Color.decode(hex));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        return btn;
    }

    private JButton createFilterBtn(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (isActive) setBtnActive(btn); 
        else setBtnNormal(btn);
        
        return btn;
    }

    private void setBtnActive(JButton btn) {
        btn.setBackground(Color.decode("#1967D2"));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#1967D2"), 1), BorderFactory.createEmptyBorder(8, 15, 8, 15)));
    }

    private void setBtnNormal(JButton btn) {
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.decode("#374151"));
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1), BorderFactory.createEmptyBorder(8, 15, 8, 15)));
    }

    // =====================================================================================
    // CÁC HÀM UI CUSTOM NOTIFICATION & CONFIRM 
    // =====================================================================================
    private void showCustomNotification(String titleText, String message, String type) {
        Window window = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(window != null ? (Frame) window : null, true); 
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
                + message.replace("\n", "<br>") + "</div></html>";

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
        dialog.setSize(440, 260); 
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return result[0];
    }
    
}