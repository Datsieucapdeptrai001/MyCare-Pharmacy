package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
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

public class ManHinhKhuyenMai extends JPanel {

    private final Color COLOR_BG = Color.decode("#F4F6F8");
    private final Color COLOR_PRIMARY = Color.decode("#1A73E8");
    private final Color COLOR_ACTIVE_FILTER = Color.decode("#1967D2");
    private final Color COLOR_PURPLE = Color.decode("#9333EA");
    private final Color COLOR_PURPLE_LIGHT = Color.decode("#F5F3FF");
    private final Color COLOR_HEADER_TABLE = Color.decode("#1E3A5F");
    private final Color COLOR_SUCCESS = Color.decode("#10B981");
    private final Color COLOR_DANGER = Color.decode("#DC2626");
    private final Color COLOR_GRAY = Color.decode("#64748B");

    private final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);

    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTable tblKhuyenMai;

    private JTextField txtLoyaltyBuy, txtLoyaltyExchange, txtLoyaltyMin;
    private JTextField txtSearch;
    private List<JButton> filterButtons = new ArrayList<>();

    public ManHinhKhuyenMai() {
        setBackground(COLOR_BG);
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(15, 20, 15, 20));

        add(createTopKPISecton(), BorderLayout.NORTH);

        JPanel pnlCenter = new JPanel();
        pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));
        pnlCenter.setOpaque(false);

        pnlCenter.add(createLoyaltySection());
        pnlCenter.add(Box.createVerticalStrut(20));
        pnlCenter.add(createFilterAndTableSection());

        add(pnlCenter, BorderLayout.CENTER);
        
        loadDataDemo();
    }

    // ================== GIAO DIỆN CHÍNH ==================
    
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
        pnlMain.setBorder(new LineBorder(Color.decode("#E9D5FF"), 1, true));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(10, 15, 5, 15));

        JLabel lblTitle = new JLabel("★ Chương trình tích điểm thưởng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(COLOR_PURPLE);

        JButton btnSaveLoyalty = createButton("Lưu cài đặt", COLOR_PURPLE, Color.WHITE);
        btnSaveLoyalty.addActionListener(e -> {
            if (txtLoyaltyBuy.getText().isEmpty() || txtLoyaltyExchange.getText().isEmpty() || txtLoyaltyMin.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin tích điểm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Đã lưu cấu hình tích điểm thành công!");
        });

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(btnSaveLoyalty, BorderLayout.EAST);

        JPanel pnlContent = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlContent.setOpaque(false);
        pnlContent.setBorder(new EmptyBorder(10, 15, 15, 15));

        txtLoyaltyBuy = new JTextField("10000", 5);
        txtLoyaltyExchange = new JTextField("1000", 5);
        txtLoyaltyMin = new JTextField("1000", 5);

        pnlContent.add(createLoyaltyBox("Tích điểm khi mua", txtLoyaltyBuy, "đ = 1 điểm"));
        pnlContent.add(createLoyaltyBox("Giá trị đổi điểm", txtLoyaltyExchange, "đ / 1 điểm"));
        pnlContent.add(createLoyaltyBox("Điểm tối thiểu đổi", txtLoyaltyMin, "điểm"));

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlContent, BorderLayout.CENTER);
        return pnlMain;
    }

    private JPanel createLoyaltyBox(String title, JTextField txt, String labelAfter) {
        JPanel box = new JPanel(new FlowLayout(FlowLayout.LEFT));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.LIGHT_GRAY), title, TitledBorder.LEFT, TitledBorder.TOP, FONT_BOLD, COLOR_GRAY));
        box.add(txt);
        box.add(new JLabel(labelAfter));
        return box;
    }

    private JPanel createFilterAndTableSection() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setOpaque(false);

        // --- BỘ LỌC & TÌM KIẾM ---
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
            btn.addActionListener(e -> {
                for (JButton b : filterButtons) {
                    b.setBackground(Color.WHITE); b.setForeground(Color.BLACK);
                }
                btn.setBackground(COLOR_ACTIVE_FILTER); btn.setForeground(Color.WHITE);
                rowSorter.setRowFilter(f.equals("Tất cả") ? null : RowFilter.regexFilter("^" + f + "$", 7));
            });
        }

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRight.setOpaque(false);
        txtSearch = new JTextField(15);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText();
                rowSorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text, 1));
            }
        });

        JButton btnRefresh = createButton("Làm mới", COLOR_GRAY, Color.WHITE);
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); loadDataDemo(); });

        JButton btnAddNew = createButton("+ Thêm mới", COLOR_DANGER, Color.WHITE);
        btnAddNew.addActionListener(e -> showKhuyenMaiDialog(-1)); // -1 nghĩa là Thêm mới

        pnlRight.add(txtSearch);
        pnlRight.add(btnRefresh);
        pnlRight.add(btnAddNew);

        pnlFilter.add(pnlBtns, BorderLayout.WEST);
        pnlFilter.add(pnlRight, BorderLayout.EAST);

        // --- BẢNG DỮ LIỆU ---
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
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 9) return Boolean.class; // Cột cuối là Boolean để vẽ Toggle
                return String.class;
            }
        };
        
        tblKhuyenMai = new JTable(tableModel);
        tblKhuyenMai.setRowHeight(45);
        tblKhuyenMai.setShowVerticalLines(false);
        tblKhuyenMai.setGridColor(Color.decode("#F1F5F9"));
        tblKhuyenMai.setFont(FONT_REGULAR);
        
        rowSorter = new TableRowSorter<>(tableModel);
        tblKhuyenMai.setRowSorter(rowSorter);

        // Khóa độ rộng cột
        tblKhuyenMai.getColumnModel().getColumn(0).setPreferredWidth(30);
        tblKhuyenMai.getColumnModel().getColumn(1).setPreferredWidth(180);
        tblKhuyenMai.getColumnModel().getColumn(3).setPreferredWidth(80);
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
        tblKhuyenMai.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setForeground(COLOR_PRIMARY); l.setHorizontalAlignment(SwingConstants.CENTER); l.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return l;
            }
        });
        tblKhuyenMai.getColumnModel().getColumn(9).setCellRenderer(new ToggleSwitchRenderer());

        // CLICK EVENT: Sửa và Bật/Tắt
        tblKhuyenMai.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblKhuyenMai.rowAtPoint(e.getPoint());
                int col = tblKhuyenMai.columnAtPoint(e.getPoint());
                if (row >= 0) {
                    int modelRow = tblKhuyenMai.convertRowIndexToModel(row);
                    if (col == 8) { // Cột Sửa
                        showKhuyenMaiDialog(modelRow);
                    } else if (col == 9) { // Cột Bật/Tắt
                        boolean currentVal = (boolean) tableModel.getValueAt(modelRow, 9);
                        tableModel.setValueAt(!currentVal, modelRow, 9);
                        tblKhuyenMai.repaint();
                    }
                }
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

    // ================== DIALOG THÊM / SỬA KHUYẾN MẠI ==================

    private void showKhuyenMaiDialog(int modelRowToEdit) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog((Frame) parentWindow, modelRowToEdit == -1 ? "Thêm chương trình khuyến mại" : "Cập nhật khuyến mại", true);
        dialog.setSize(550, 450);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5, 10, 10, 10); gbc.weightx = 1.0;

        // Các ô nhập liệu
        JTextField txtTen = new JTextField();
        JComboBox<String> cbLoai = new JComboBox<>(new String[]{"Giảm phần trăm", "Giảm tiền cố định", "Mua X tặng Y"});
        JTextField txtGiamGia = new JTextField();
        JTextField txtNgayBD = new JTextField("dd/MM/yyyy");
        JTextField txtNgayKT = new JTextField("dd/MM/yyyy");
        JTextField txtDonToiThieu = new JTextField("0");
        JComboBox<String> cbApDung = new JComboBox<>(new String[]{"Tất cả", "Thuốc không kê đơn", "Sản phẩm chức năng"});
        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Đang hoạt động", "Sắp diễn ra", "Tạm dừng"});

        // Đổ dữ liệu nếu là Sửa
        if (modelRowToEdit != -1) {
            txtTen.setText(tableModel.getValueAt(modelRowToEdit, 1).toString());
            cbLoai.setSelectedItem(tableModel.getValueAt(modelRowToEdit, 2).toString());
            txtGiamGia.setText(tableModel.getValueAt(modelRowToEdit, 3).toString().replaceAll("[^0-9]", ""));
            txtDonToiThieu.setText(tableModel.getValueAt(modelRowToEdit, 4).toString().replaceAll("[^0-9]", ""));
            cbApDung.setSelectedItem(tableModel.getValueAt(modelRowToEdit, 5).toString());
            String thoiGian = tableModel.getValueAt(modelRowToEdit, 6).toString();
            if(thoiGian.contains(" - ")) {
                txtNgayBD.setText(thoiGian.split(" - ")[0]);
                txtNgayKT.setText(thoiGian.split(" - ")[1]);
            }
            cbTrangThai.setSelectedItem(tableModel.getValueAt(modelRowToEdit, 7).toString());
        }

        // Add vào Form
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        pnlForm.add(new JLabel("Tên chương trình *"), gbc);
        gbc.gridy = 1; pnlForm.add(txtTen, gbc);

        gbc.gridwidth = 1; 
        gbc.gridy = 2; gbc.gridx = 0; pnlForm.add(new JLabel("Loại khuyến mại"), gbc);
        gbc.gridx = 1; pnlForm.add(new JLabel("Mức giảm"), gbc);
        gbc.gridy = 3; gbc.gridx = 0; pnlForm.add(cbLoai, gbc);
        gbc.gridx = 1; pnlForm.add(txtGiamGia, gbc);

        gbc.gridy = 4; gbc.gridx = 0; pnlForm.add(new JLabel("Ngày bắt đầu (dd/MM/yyyy)"), gbc);
        gbc.gridx = 1; pnlForm.add(new JLabel("Ngày kết thúc (dd/MM/yyyy)"), gbc);
        gbc.gridy = 5; gbc.gridx = 0; pnlForm.add(txtNgayBD, gbc);
        gbc.gridx = 1; pnlForm.add(txtNgayKT, gbc);

        gbc.gridy = 6; gbc.gridx = 0; pnlForm.add(new JLabel("Đơn hàng tối thiểu (đ)"), gbc);
        gbc.gridx = 1; pnlForm.add(new JLabel("Áp dụng cho"), gbc);
        gbc.gridy = 7; gbc.gridx = 0; pnlForm.add(txtDonToiThieu, gbc);
        gbc.gridx = 1; pnlForm.add(cbApDung, gbc);

        gbc.gridy = 8; gbc.gridx = 0; pnlForm.add(new JLabel("Trạng thái"), gbc);
        gbc.gridy = 9; gbc.gridx = 0; pnlForm.add(cbTrangThai, gbc);

        dialog.add(pnlForm, BorderLayout.CENTER);

        // Buttons
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlButtons.setBackground(Color.WHITE);
        pnlButtons.setBorder(new EmptyBorder(10, 20, 10, 20));

        JButton btnHuy = createButton("Hủy", Color.WHITE, Color.BLACK);
        btnHuy.setBorder(new LineBorder(Color.LIGHT_GRAY));
        btnHuy.addActionListener(e -> dialog.dispose());

        JButton btnLuu = createButton(modelRowToEdit == -1 ? "+ Thêm" : "Lưu thay đổi", COLOR_PRIMARY, Color.WHITE);
        btnLuu.addActionListener(e -> {
            if (txtTen.getText().isEmpty() || txtGiamGia.getText().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đủ Tên và Mức giảm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (modelRowToEdit == -1) {
                // Thêm mới (Demo)
                tableModel.addRow(new Object[]{
                    tableModel.getRowCount() + 1, txtTen.getText(), cbLoai.getSelectedItem(), 
                    "% " + txtGiamGia.getText(), txtDonToiThieu.getText() + "đ", cbApDung.getSelectedItem(),
                    txtNgayBD.getText() + " - " + txtNgayKT.getText(), cbTrangThai.getSelectedItem(), "Edit", true
                });
                JOptionPane.showMessageDialog(dialog, "Thêm khuyến mại thành công!");
            } else {
                // Cập nhật
                tableModel.setValueAt(txtTen.getText(), modelRowToEdit, 1);
                tableModel.setValueAt(cbLoai.getSelectedItem(), modelRowToEdit, 2);
                tableModel.setValueAt("% " + txtGiamGia.getText(), modelRowToEdit, 3);
                tableModel.setValueAt(txtDonToiThieu.getText() + "đ", modelRowToEdit, 4);
                tableModel.setValueAt(cbApDung.getSelectedItem(), modelRowToEdit, 5);
                tableModel.setValueAt(txtNgayBD.getText() + " - " + txtNgayKT.getText(), modelRowToEdit, 6);
                tableModel.setValueAt(cbTrangThai.getSelectedItem(), modelRowToEdit, 7);
                JOptionPane.showMessageDialog(dialog, "Cập nhật thành công!");
            }
            dialog.dispose();
        });

        pnlButtons.add(btnHuy);
        pnlButtons.add(btnLuu);
        dialog.add(pnlButtons, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }


    // ================== DATA DEMO VÀ COMPONENTS PHỤ ==================

    private void loadDataDemo() {
        tableModel.setRowCount(0);
        Object[][] data = {
            {"1", "Giảm 10% Vitamin C", "Giảm phần trăm", "% 10", "100.000đ", "Sản phẩm chức năng", "01/01/2024 - 31/03/2024", "Tạm dừng", "Edit", false},
            {"2", "Tặng 50k đơn từ 500k", "Giảm tiền cố định", "% 50000 đ", "500.000đ", "Tất cả", "15/02/2024 - 28/02/2024", "Tạm dừng", "Edit", false},
            {"3", "Mua 3 tặng 1 Paracetamol", "Mua X tặng Y", "% 1 sản phẩm", "-", "Thuốc không kê đơn", "01/03/2024 - 31/03/2024", "Đang hoạt động", "Edit", true},
            {"4", "Flash Sale cuối tuần", "Giảm phần trăm", "% 20", "-", "Tất cả", "22/03/2024 - 24/03/2024", "Đang hoạt động", "Edit", true},
            {"5", "Ưu đãi thành viên Vàng", "Giảm tiền cố định", "% 100000 đ", "1.000.000đ", "Tất cả", "01/04/2024 - 30/06/2024", "Sắp diễn ra", "Edit", true}
        };
        for (Object[] row : data) tableModel.addRow(row);
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD); btn.setForeground(fg); btn.setBackground(bg);
        btn.setContentAreaFilled(false); btn.setOpaque(true); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createFilterButton(String text, boolean active) {
        JButton btn = createButton(text, active ? COLOR_ACTIVE_FILTER : Color.WHITE, active ? Color.WHITE : Color.BLACK);
        btn.setBorderPainted(true); btn.setBorder(new LineBorder(active ? COLOR_ACTIVE_FILTER : Color.decode("#CBD5E1")));
        return btn;
    }

    // Các class con vẽ UI
    private class KPICardCustom extends JPanel {
        private String title, value; private Color color; private int type;
        public KPICardCustom(String t, String v, Color c, int type) {
            this.title = t; this.value = v; this.color = c; this.type = type;
            setBackground(Color.WHITE);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE); g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
            g2.setColor(color); g2.fillRect(15, 20, 40, 40);
            g2.setColor(COLOR_GRAY); g2.setFont(FONT_REGULAR); g2.drawString(title, 70, 35);
            g2.setColor(Color.BLACK); g2.setFont(new Font("Segoe UI", Font.BOLD, 22)); g2.drawString(value, 70, 60);
            g2.dispose();
        }
    }

    private class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String status = value.toString();
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            l.setHorizontalAlignment(SwingConstants.CENTER); l.setFont(new Font("Segoe UI", Font.BOLD, 11));
            if (status.equals("Đang hoạt động")) { l.setForeground(COLOR_SUCCESS); l.setText("<html><div style='background:#DCFCE7; padding:2px 8px;'>Đang hoạt động</div></html>"); }
            else if (status.equals("Sắp diễn ra")) { l.setForeground(COLOR_PRIMARY); l.setText("<html><div style='background:#DBEAFE; padding:2px 8px;'>Sắp diễn ra</div></html>"); }
            else { l.setForeground(COLOR_GRAY); l.setText("<html><div style='background:#F1F5F9; padding:2px 8px;'>" + status + "</div></html>"); }
            return l;
        }
    }

    private class ToggleSwitchRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            boolean isOn = (value != null && (boolean) value);
            JPanel pnl = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = 40, h = 20, x = (getWidth() - w) / 2, y = (getHeight() - h) / 2;
                    g2.setColor(isOn ? COLOR_SUCCESS : Color.decode("#CBD5E1")); g2.fillRoundRect(x, y, w, h, h, h);
                    g2.setColor(Color.WHITE); g2.fillOval(isOn ? x + w - 18 : x + 2, y + 2, 16, 16);
                }
            };
            pnl.setBackground(Color.WHITE);
            pnl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return pnl;
        }
    }
}