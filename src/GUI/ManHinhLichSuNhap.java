package GUI;

import BUS.BUS_PhieuNhapHang;
import Utils.MenuIcon;
import Utils.TelexFix;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class ManHinhLichSuNhap extends JPanel {

    private static final Color BG_APP = new Color(248, 250, 252);
    private static final Color PRIMARY = new Color(14, 116, 144);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color HEADER_BG = new Color(204, 226, 241);

    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);

    private static final Color SUCCESS = new Color(22, 163, 74);
    private static final Color SUCCESS_SOFT = new Color(220, 252, 231);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color BLUE_SOFT = new Color(239, 246, 255);
    private static final Color ROW_ALT = new Color(252, 253, 255);

    private JTable table;
    private DefaultTableModel model;
    private SuggestTextField txtTimKiem;
    private TableRowSorter<DefaultTableModel> sorter;
    private JPanel pnlChiTiet;
    private JScrollPane tableScroll;

    private String selectedMaPhieu = null;

    private final BUS_PhieuNhapHang busPhieuNhap = new BUS_PhieuNhapHang();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final NumberFormat moneyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    public ManHinhLichSuNhap() {
        moneyFormat.setMaximumFractionDigits(0);

        setLayout(new BorderLayout(0, 12));
        setBackground(BG_APP);
        setBorder(new EmptyBorder(22, 28, 22, 28));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
        TelexFix.applyDeep(this);
        loadData();
    }

    private JPanel createHeader() {
        JPanel pnlTop = new JPanel(new BorderLayout(12, 10));
        pnlTop.setOpaque(false);

        JLabel lblTitle = new JLabel("LỊCH SỬ PHIẾU LÔ HÀNG");
        lblTitle.setIcon(new MenuIcon("TIME"));
        lblTitle.setIconTextGap(10);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_PRIMARY);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRight.setOpaque(false);

        txtTimKiem = new SuggestTextField("Tìm phiếu nhập, mã lô, sản phẩm...");
        txtTimKiem.setPreferredSize(new Dimension(350, 40));
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTimKiem.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(BORDER, 1, 8),
                new Insets(0, 12, 0, 12)));

        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                locDuLieu();
                SwingUtilities.invokeLater(() -> txtTimKiem.showSuggestions());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                locDuLieu();
                SwingUtilities.invokeLater(() -> txtTimKiem.showSuggestions());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                locDuLieu();
                SwingUtilities.invokeLater(() -> txtTimKiem.showSuggestions());
            }
        });

        txtTimKiem.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(() -> txtTimKiem.showSuggestions());
            }
        });

        JButton btnLamMoi = createHoverButton(
                "Làm mới",
                new Color(248, 250, 252),
                new Color(226, 232, 240),
                TEXT_PRIMARY);
        btnLamMoi.setIcon(new MenuIcon("REFRESH"));
        btnLamMoi.setPreferredSize(new Dimension(125, 40));
        btnLamMoi.addActionListener(e -> {
            txtTimKiem.setText("");
            loadData();
            txtTimKiem.requestFocusInWindow();
        });

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSearch.setForeground(TEXT_SECONDARY);

        pnlRight.add(lblSearch);
        pnlRight.add(txtTimKiem);
        pnlRight.add(btnLamMoi);

        pnlTop.add(lblTitle, BorderLayout.WEST);
        pnlTop.add(pnlRight, BorderLayout.EAST);

        return pnlTop;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);

        content.add(createTable(), BorderLayout.CENTER);

        pnlChiTiet = createChiTietPanel();
        pnlChiTiet.setVisible(false);
        content.add(pnlChiTiet, BorderLayout.SOUTH);

        return content;
    }

    private JPanel createTable() {
        JPanel pnlWrap = new JPanel(new BorderLayout());
        pnlWrap.setOpaque(false);

        String[] columns = {
                "STT",
                "Thời gian",
                "Mã phiếu lô",
                "Hành động",
                "Mã lô",
                "Sản phẩm",
                "SL nhập",
                "Giá vốn",
                "Thành tiền",
                "Người thực hiện"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        table.setRowHeight(46);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(224, 242, 254));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setDefaultRenderer(Object.class, new NhatKyLoRenderer());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(HEADER_BG);
        header.setForeground(new Color(51, 65, 85));
        header.setPreferredSize(new Dimension(header.getWidth(), 48));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setReorderingAllowed(false);

        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(135);
        table.getColumnModel().getColumn(3).setPreferredWidth(105);
        table.getColumnModel().getColumn(4).setPreferredWidth(135);
        table.getColumnModel().getColumn(5).setPreferredWidth(235);
        table.getColumnModel().getColumn(6).setPreferredWidth(85);
        table.getColumnModel().getColumn(7).setPreferredWidth(120);
        table.getColumnModel().getColumn(8).setPreferredWidth(125);
        table.getColumnModel().getColumn(9).setPreferredWidth(180);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());

                if (viewRow < 0) {
                    return;
                }

                int modelRow = table.convertRowIndexToModel(viewRow);
                hienChiTietDong(modelRow);
            }
        });

        tableScroll = new JScrollPane(table);
        tableScroll.setBorder(new RoundedLineBorder(BORDER, 1, 10));
        tableScroll.getViewport().setBackground(Color.WHITE);
        tableScroll.setPreferredSize(new Dimension(100, 180));
        styleScrollPane(tableScroll);

        pnlWrap.add(tableScroll, BorderLayout.CENTER);
        return pnlWrap;
    }

    public void loadData() {
        model.setRowCount(0);

        if (pnlChiTiet != null) {
            pnlChiTiet.setVisible(false);
        }

        selectedMaPhieu = null;

        try {
            List<Object[]> data = busPhieuNhap.layNhatKyLoHang();

            int stt = 1;

            for (Object[] row : data) {
                String thoiGian = "";

                if (row[0] instanceof Timestamp) {
                    thoiGian = ((Timestamp) row[0]).toLocalDateTime().format(dtf);
                } else if (row[0] != null) {
                    thoiGian = row[0].toString();
                }

                String maPhieuLo = safe(row[1]);
                String hanhDong = safe(row[2]);
                String maLo = safe(row[3]);
                String sanPham = safe(row[4]);

                int soLuongNhap = parseInt(row[5]);
                double donGiaNhap = parseDouble(row[6]);
                double thanhTien = parseDouble(row[7]);

                String nguoiThucHien = safe(row[8]);

                if (nguoiThucHien.isEmpty()
                        || nguoiThucHien.equalsIgnoreCase("NV-DEFAULT")
                        || nguoiThucHien.equalsIgnoreCase("Người dùng hiện tại")) {
                    nguoiThucHien = "Không xác định";
                }

                model.addRow(new Object[] {
                        stt++,
                        thoiGian,
                        maPhieuLo,
                        hanhDong,
                        maLo,
                        sanPham,
                        "+" + moneyFormat.format(soLuongNhap),
                        formatMoneySmart(donGiaNhap) + "đ",
                        moneyFormat.format(thanhTien) + "đ",
                        nguoiThucHien
                });
            }

            capNhatGoiY();
            locDuLieu();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi khi tải lịch sử lô hàng!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void capNhatGoiY() {
        Set<String> set = new LinkedHashSet<>();

        for (int r = 0; r < model.getRowCount(); r++) {
            addSuggest(set, model.getValueAt(r, 1));
            addSuggest(set, model.getValueAt(r, 2));
            addSuggest(set, model.getValueAt(r, 4));
            addSuggest(set, model.getValueAt(r, 5));
            addSuggest(set, model.getValueAt(r, 9));
        }

        txtTimKiem.setSuggestions(new ArrayList<>(set));
    }

    private void addSuggest(Set<String> set, Object value) {
        if (value == null) {
            return;
        }

        String s = value.toString().trim();

        if (!s.isEmpty()) {
            set.add(s);
        }
    }

    private void locDuLieu() {
        if (sorter == null || txtTimKiem == null) {
            return;
        }

        String text = txtTimKiem.getText().trim();

        if (text.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }

        sorter.setRowFilter(RowFilter.regexFilter(
                "(?i)" + Pattern.quote(text),
                1, 2, 4, 5, 9));
    }

    private void anChiTiet() {
        pnlChiTiet.setVisible(false);
        selectedMaPhieu = null;
        revalidate();
        repaint();
    }

    private void hienChiTietDong(int modelRow) {
        String maPhieu = String.valueOf(model.getValueAt(modelRow, 2));

        if (selectedMaPhieu != null && selectedMaPhieu.equals(maPhieu) && pnlChiTiet.isVisible()) {
            anChiTiet();
            return;
        }

        selectedMaPhieu = maPhieu;

        String thoiGian = String.valueOf(model.getValueAt(modelRow, 1));
        String hanhDong = String.valueOf(model.getValueAt(modelRow, 3));
        String maLo = String.valueOf(model.getValueAt(modelRow, 4));
        String sanPham = String.valueOf(model.getValueAt(modelRow, 5));
        String soLuong = String.valueOf(model.getValueAt(modelRow, 6));
        String giaVon = String.valueOf(model.getValueAt(modelRow, 7));
        String thanhTien = String.valueOf(model.getValueAt(modelRow, 8));
        String nguoiThucHien = String.valueOf(model.getValueAt(modelRow, 9));

        pnlChiTiet.removeAll();

        JPanel root = new JPanel(new BorderLayout(12, 10));
        root.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Chi tiết phiếu lô " + maPhieu);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Thời gian: " + thoiGian + "   |   Người thực hiện: " + nguoiThucHien);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_SECONDARY);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblSub);

        JButton btnXemPhieu = createHoverButton(
                "Xem phiếu nhập",
                Color.WHITE,
                new Color(239, 246, 255),
                PRIMARY);
        btnXemPhieu.setBorder(new RoundedLineBorder(PRIMARY, 1, 8));
        btnXemPhieu.setPreferredSize(new Dimension(145, 36));
        btnXemPhieu.addActionListener(e -> moChiTietPhieuNhap(maPhieu));

        JButton btnClose = createCloseDetailButton();

        JPanel actionBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBox.setOpaque(false);
        actionBox.add(btnXemPhieu);
        actionBox.add(btnClose);

        header.add(titleBox, BorderLayout.WEST);
        header.add(actionBox, BorderLayout.EAST);

        JPanel grid = new JPanel(new GridLayout(2, 3, 10, 10));
        grid.setOpaque(false);

        grid.add(createInfoCard("Hành động", hanhDong, SUCCESS, SUCCESS_SOFT));
        grid.add(createInfoCard("Mã lô", maLo, PRIMARY, new Color(240, 249, 255)));
        grid.add(createInfoCard("Sản phẩm", sanPham, TEXT_PRIMARY, Color.WHITE));
        grid.add(createInfoCard("Số lượng nhập", soLuong + " đơn vị", SUCCESS, SUCCESS_SOFT));
        grid.add(createInfoCard("Giá vốn / đơn vị", giaVon, BLUE, BLUE_SOFT));
        grid.add(createInfoCard("Thành tiền", thanhTien, PRIMARY, new Color(240, 249, 255)));

        root.add(header, BorderLayout.NORTH);
        root.add(grid, BorderLayout.CENTER);

        pnlChiTiet.add(root, BorderLayout.CENTER);
        pnlChiTiet.setVisible(true);

        revalidate();
        repaint();

        int viewRow = table.convertRowIndexToView(modelRow);
        if (viewRow >= 0) {
            Rectangle rect = table.getCellRect(viewRow, 0, true);
            table.scrollRectToVisible(rect);
        }
    }

    private JButton createCloseDetailButton() {
        JButton btn = new JButton("×");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btn.setForeground(TEXT_SECONDARY);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedLineBorder(BORDER, 1, 8));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(42, 36));
        btn.setToolTipText("Đóng chi tiết");

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setForeground(Color.WHITE);
                btn.setBackground(new Color(239, 68, 68));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setForeground(TEXT_SECONDARY);
                btn.setBackground(Color.WHITE);
            }
        });

        btn.addActionListener(e -> anChiTiet());

        return btn;
    }

    private JPanel createChiTietPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(100, 230));
        panel.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(BORDER, 1, 12),
                new Insets(14, 16, 14, 16)));
        return panel;
    }

    private JPanel createInfoCard(String title, String value, Color valueColor, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(bgColor);
        card.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(BORDER, 1, 9),
                new Insets(9, 12, 9, 12)));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(TEXT_SECONDARY);

        JLabel lblValue = new JLabel("<html>" + value + "</html>");
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblValue.setForeground(valueColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);

        return card;
    }

    private void moChiTietPhieuNhap(String maPN) {
        if (maPN == null || maPN.trim().isEmpty()) {
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(this);

        JDialog dialog = new JDialog(owner, "Chi tiết phiếu nhập " + maPN, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(new ManHinhChiTietPhieuNhap(maPN));
        dialog.setSize(980, 560);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private JButton createHoverButton(String text, Color bg, Color hoverBg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedLineBorder(bg, 1, 8));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });

        return btn;
    }

    private String formatMoneySmart(double value) {
        if (value == Math.floor(value)) {
            return moneyFormat.format(value);
        }

        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(0);

        return nf.format(value);
    }

    private String safe(Object obj) {
        return obj == null ? "" : obj.toString().trim();
    }

    private int parseInt(Object obj) {
        try {
            return Integer.parseInt(String.valueOf(obj).replace("+", "").replace(".", "").trim());
        } catch (Exception e) {
            try {
                return Integer.parseInt(String.valueOf(obj));
            } catch (Exception ignored) {
                return 0;
            }
        }
    }

    private double parseDouble(Object obj) {
        try {
            return Double.parseDouble(String.valueOf(obj));
        } catch (Exception e) {
            return 0;
        }
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));

        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(18);

        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
    }

    private class NhatKyLoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    table,
                    value,
                    isSelected,
                    hasFocus,
                    row,
                    column);

            lbl.setIcon(null);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
            lbl.setOpaque(true);
            lbl.setToolTipText(value == null ? "" : value.toString());

            if (isSelected) {
                lbl.setBackground(new Color(224, 242, 254));
                lbl.setForeground(TEXT_PRIMARY);
            } else {
                lbl.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                lbl.setForeground(TEXT_PRIMARY);
            }

            if (column == 0 || column == 1 || column == 2 || column == 3 || column == 4
                    || column == 6 || column == 7 || column == 8 || column == 9) {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
            }

            if (column == 0) {
                lbl.setForeground(TEXT_SECONDARY);
            }

            if (column == 2) {
                lbl.setForeground(new Color(0, 75, 255));
                lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                lbl.setToolTipText("Bấm để xem chi tiết");
            }

            if (column == 3) {
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(SUCCESS);
                lbl.setBackground(isSelected ? new Color(224, 242, 254) : SUCCESS_SOFT);
            }

            if (column == 5) {
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }

            if (column == 6) {
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lbl.setForeground(SUCCESS);
            }

            if (column == 7) {
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lbl.setForeground(BLUE);
            }

            if (column == 8) {
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lbl.setForeground(PRIMARY);
            }

            if (column == 9) {
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }

            return lbl;
        }
    }

    private static class SuggestTextField extends JTextField {
        private final String placeholder;
        private final JPopupMenu popup = new JPopupMenu();
        private final DefaultListModel<String> listModel = new DefaultListModel<>();
        private final JList<String> list = new JList<>(listModel);
        private final JScrollPane scrollPane = new JScrollPane(list);
        private final List<String> suggestions = new ArrayList<>();
        private boolean selecting = false;

        public SuggestTextField(String placeholder) {
            this.placeholder = placeholder;

            setOpaque(false);

            list.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            list.setFixedCellHeight(32);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setBackground(Color.WHITE);
            list.setForeground(new Color(15, 23, 42));
            list.setSelectionBackground(new Color(224, 242, 254));
            list.setSelectionForeground(new Color(15, 23, 42));
            list.setBorder(new EmptyBorder(4, 0, 4, 0));

            scrollPane.setBorder(null);
            scrollPane.getViewport().setBackground(Color.WHITE);
            scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(7, 0));
            scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());

            popup.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
            popup.setFocusable(false);
            popup.add(scrollPane);

            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    chonGoiY();
                }
            });

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (!popup.isVisible()) {
                        return;
                    }

                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        int index = list.getSelectedIndex();
                        if (index < listModel.size() - 1) {
                            list.setSelectedIndex(index + 1);
                            list.ensureIndexIsVisible(index + 1);
                        }
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        int index = list.getSelectedIndex();
                        if (index > 0) {
                            list.setSelectedIndex(index - 1);
                            list.ensureIndexIsVisible(index - 1);
                        }
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        chonGoiY();
                        e.consume();
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        popup.setVisible(false);
                        e.consume();
                    }
                }
            });
        }

        public void setSuggestions(List<String> data) {
            suggestions.clear();

            if (data != null) {
                suggestions.addAll(data);
            }
        }

        public void showSuggestions() {
            if (selecting) {
                return;
            }

            String keyword = getText().trim().toLowerCase();
            listModel.clear();

            for (String s : suggestions) {
                if (keyword.isEmpty() || s.toLowerCase().contains(keyword)) {
                    listModel.addElement(s);
                }
            }

            if (listModel.isEmpty()) {
                popup.setVisible(false);
                return;
            }

            int visibleRows = Math.min(5, listModel.size());
            int rowHeight = 32;
            int popupHeight = visibleRows * rowHeight + 8;
            int popupWidth = Math.max(getWidth(), 280);

            list.setVisibleRowCount(visibleRows);
            scrollPane.setPreferredSize(new Dimension(popupWidth, popupHeight));

            if (listModel.size() > 0) {
                list.setSelectedIndex(0);
            }

            popup.show(this, 0, getHeight() + 2);
        }

        private void chonGoiY() {
            String value = list.getSelectedValue();

            if (value == null) {
                return;
            }

            selecting = true;
            setText(value);
            selecting = false;

            popup.setVisible(false);
            requestFocusInWindow();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(148, 163, 184));
                g2.setFont(new Font("Segoe UI", Font.ITALIC, 14));

                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

                g2.drawString(placeholder, getInsets().left + 2, y);
                g2.dispose();
            }
        }
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {

        private final Color customThumbColor = new Color(148, 163, 184, 160);
        private final Color customThumbHover = new Color(100, 116, 139, 190);
        private final Color customTrackColor = new Color(241, 245, 249);

        @Override
        protected void configureScrollBarColors() {
            super.configureScrollBarColors();
            thumbColor = customThumbColor;
            trackColor = customTrackColor;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            btn.setBorder(null);
            btn.setFocusable(false);
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            return btn;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(customTrackColor);
            g2.fillRoundRect(
                    trackBounds.x,
                    trackBounds.y,
                    trackBounds.width,
                    trackBounds.height,
                    8,
                    8);

            g2.dispose();
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isDragging || isThumbRollover() ? customThumbHover : customThumbColor);

            int arc = 8;
            int x = thumbBounds.x + 1;
            int y = thumbBounds.y + 1;
            int w = thumbBounds.width - 2;
            int h = thumbBounds.height - 2;

            g2.fillRoundRect(x, y, w, h, arc, arc);

            g2.dispose();
        }
    }

    private static class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int radius;

        public RoundedLineBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);

            for (int i = 0; i < thickness; i++) {
                g2.drawRoundRect(
                        x + i,
                        y + i,
                        w - 1 - i * 2,
                        h - 1 - i * 2,
                        radius,
                        radius);
            }

            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
    }

    private static class CompoundRoundBorder extends AbstractBorder {
        private final AbstractBorder outer;
        private final Insets inner;

        public CompoundRoundBorder(AbstractBorder outer, Insets inner) {
            this.outer = outer;
            this.inner = inner;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            Insets o = outer.getBorderInsets(c);
            return new Insets(
                    o.top + inner.top,
                    o.left + inner.left,
                    o.bottom + inner.bottom,
                    o.right + inner.right);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            outer.paintBorder(c, g, x, y, w, h);
        }
    }
}