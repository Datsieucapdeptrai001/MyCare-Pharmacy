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
 * ManHinhKhuyenMai.java - Đã Refactor & Polish
 * Tập trung vào quản lý danh sách và điều hướng.
 */
public class ManHinhKhuyenMai extends JPanel {

    // --- BẢNG MÀU CHUẨN ---
    private final Color COLOR_BG = Color.decode("#F4F6F8");
    private final Color COLOR_PRIMARY = Color.decode("#1A73E8");
    private final Color COLOR_ACTIVE_FILTER = Color.decode("#1967D2");
    private final Color COLOR_PURPLE = Color.decode("#9333EA");
    private final Color COLOR_PURPLE_LIGHT = Color.decode("#F5F3FF");
    private final Color COLOR_HEADER_TABLE = Color.decode("#1E3A5F");
    private final Color COLOR_SUCCESS = Color.decode("#10B981");
    private final Color COLOR_DANGER = Color.decode("#DC2626");
    private final Color COLOR_GRAY = Color.decode("#64748B");
    private final Color COLOR_LABEL = Color.decode("#475569");

    private final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);

    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTable tblKhuyenMai;
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

        JButton btnSave = createStyledButton("Lưu cài đặt", COLOR_PURPLE, Color.WHITE);
        btnSave.addActionListener(e -> JOptionPane.showMessageDialog(this, "Đã lưu cấu hình tích điểm!"));

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(btnSave, BorderLayout.EAST);

        JPanel pnlContent = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlContent.setOpaque(false);
        pnlContent.setBorder(new EmptyBorder(10, 15, 15, 15));

        pnlContent.add(createLoyaltyBox("Tích điểm khi mua", "10000", "đ = 1 điểm"));
        pnlContent.add(createLoyaltyBox("Giá trị đổi điểm", "1000", "đ / 1 điểm"));
        pnlContent.add(createLoyaltyBox("Điểm tối thiểu đổi", "1000", "điểm"));

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlContent, BorderLayout.CENTER);
        return pnlMain;
    }

    private JPanel createLoyaltyBox(String title, String val, String unit) {
        JPanel box = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.decode("#E2E8F0")), title, 0, 0, FONT_BOLD, COLOR_LABEL));
        
        JTextField txt = new JTextField(val, 6);
        styleTextField(txt);
        box.add(txt);
        box.add(new JLabel(unit));
        return box;
    }

    private JPanel createFilterAndTableSection() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setOpaque(false);

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
                    b.setBorder(new LineBorder(Color.decode("#CBD5E1")));
                }
                btn.setBackground(COLOR_ACTIVE_FILTER); btn.setForeground(Color.WHITE);
                btn.setBorder(new LineBorder(COLOR_ACTIVE_FILTER));
                rowSorter.setRowFilter(f.equals("Tất cả") ? null : RowFilter.regexFilter("^" + f + "$", 7));
            });
        }

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRight.setOpaque(false);
        txtSearch = new JTextField(15);
        styleTextField(txtSearch);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText();
                rowSorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text, 1));
            }
        });

        JButton btnRefresh = createStyledButton("Làm mới", COLOR_GRAY, Color.WHITE);
        btnRefresh.addActionListener(e -> { txtSearch.setText(""); loadDataDemo(); });

        JButton btnAddNew = createStyledButton("+ Thêm mới", COLOR_DANGER, Color.WHITE);
        btnAddNew.addActionListener(e -> {
            new DialogKhuyenMai(SwingUtilities.getWindowAncestor(this), tableModel, -1).setVisible(true);
        });

        pnlRight.add(new JLabel("Tìm kiếm: "));
        pnlRight.add(txtSearch);
        pnlRight.add(btnRefresh);
        pnlRight.add(btnAddNew);

        pnlFilter.add(pnlBtns, BorderLayout.WEST);
        pnlFilter.add(pnlRight, BorderLayout.EAST);

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
                if (columnIndex == 9) return Boolean.class;
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

        tblKhuyenMai.getColumnModel().getColumn(0).setPreferredWidth(30);
        tblKhuyenMai.getColumnModel().getColumn(1).setPreferredWidth(180);
        tblKhuyenMai.getColumnModel().getColumn(8).setPreferredWidth(40);
        tblKhuyenMai.getColumnModel().getColumn(9).setPreferredWidth(60);

        tblKhuyenMai.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());
        tblKhuyenMai.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setForeground(COLOR_PRIMARY); l.setHorizontalAlignment(SwingConstants.CENTER); l.setText("✎"); l.setFont(new Font("Arial", Font.BOLD, 18));
                return l;
            }
        });
        tblKhuyenMai.getColumnModel().getColumn(9).setCellRenderer(new ToggleSwitchRenderer());

        tblKhuyenMai.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblKhuyenMai.rowAtPoint(e.getPoint());
                int col = tblKhuyenMai.columnAtPoint(e.getPoint());
                if (row >= 0) {
                    int modelRow = tblKhuyenMai.convertRowIndexToModel(row);
                    if (col == 8) {
                        new DialogKhuyenMai(SwingUtilities.getWindowAncestor(ManHinhKhuyenMai.this), tableModel, modelRow).setVisible(true);
                    } else if (col == 9) {
                        boolean currentVal = (boolean) tableModel.getValueAt(modelRow, 9);
                        boolean newVal = !currentVal;
                        tableModel.setValueAt(newVal, modelRow, 9);
                        tableModel.setValueAt(newVal ? "Đang hoạt động" : "Tạm dừng", modelRow, 7);
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

    private void loadDataDemo() {
        tableModel.setRowCount(0);
        Object[][] data = {
            {"1", "Giảm 10% Vitamin C", "Giảm phần trăm", "% 10", "100.000đ", "Sản phẩm chức năng", "01/01/2024 - 31/03/2024", "Tạm dừng", "Edit", false},
            {"2", "Mua 3 tặng 1 Paracetamol", "Mua X tặng Y", "% 1 sản phẩm", "-", "Thuốc không kê đơn", "01/03/2024 - 31/03/2024", "Đang hoạt động", "Edit", true},
            {"3", "Flash Sale cuối tuần", "Giảm phần trăm", "% 20", "-", "Tất cả", "22/03/2024 - 24/03/2024", "Đang hoạt động", "Edit", true}
        };
        for (Object[] row : data) tableModel.addRow(row);
    }

    // --- HELPER UI METHODS ---

    private void styleTextField(JTextField txt) {
        txt.setFont(FONT_REGULAR);
        txt.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.decode("#CBD5E1"), 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setFont(FONT_BOLD); btn.setForeground(fg); btn.setBackground(bg);
        btn.setContentAreaFilled(false); btn.setOpaque(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(8, 15, 8, 15));
        return btn;
    }

    private JButton createFilterButton(String text, boolean active) {
        JButton btn = createStyledButton(text, active ? COLOR_ACTIVE_FILTER : Color.WHITE, active ? Color.WHITE : Color.BLACK);
        btn.setBorderPainted(true); btn.setBorder(new LineBorder(active ? COLOR_ACTIVE_FILTER : Color.decode("#CBD5E1")));
        return btn;
    }

    private class KPICardCustom extends JPanel {
        private String title, value; private Color color; private int type;
        public KPICardCustom(String t, String v, Color c, int type) {
            this.title = t; this.value = v; this.color = c; this.type = type;
            setBackground(Color.WHITE);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE); g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
            g2.setColor(color); g2.fillRoundRect(15, 20, 40, 40, 8, 8);
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
            if (status.equals("Đang hoạt động")) { l.setForeground(COLOR_SUCCESS); l.setText("<html><div style='background:#DCFCE7; padding:2px 8px; border-radius:10px;'>Đang hoạt động</div></html>"); }
            else if (status.equals("Sắp diễn ra")) { l.setForeground(COLOR_PRIMARY); l.setText("<html><div style='background:#DBEAFE; padding:2px 8px; border-radius:10px;'>Sắp diễn ra</div></html>"); }
            else { l.setForeground(COLOR_GRAY); l.setText("<html><div style='background:#F1F5F9; padding:2px 8px; border-radius:10px;'>" + status + "</div></html>"); }
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
            return pnl;
        }
    }
}