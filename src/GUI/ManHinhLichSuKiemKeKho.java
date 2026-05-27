package GUI;

import BUS.BUS_LichSuKiemKeKho;
import BUS.BUS_LichSuKiemKeKho.ChiTietKiemKeHistory;
import BUS.BUS_LichSuKiemKeKho.PhieuKiemKeHistory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ManHinhLichSuKiemKeKho extends JPanel {

    private static final Color PRIMARY = new Color(14, 116, 144);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color WARNING = new Color(245, 158, 11);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color HEADER = new Color(219, 234, 254);
    private static final Color ROW_SELECTED = new Color(224, 242, 254);

    private final BUS_LichSuKiemKeKho bus = new BUS_LichSuKiemKeKho();

    private JTextField txtSearch;
    private JTable tablePhieu;
    private JTable tableChiTiet;
    private DefaultTableModel modelPhieu;
    private DefaultTableModel modelChiTiet;
    private JLabel lblTitleChiTiet;

    private List<PhieuKiemKeHistory> dsPhieuGoc = new ArrayList<>();
    private List<PhieuKiemKeHistory> dsPhieuDangHienThi = new ArrayList<>();

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public ManHinhLichSuKiemKeKho() {
        setLayout(new BorderLayout(0, 12));
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(20, 28, 20, 28));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        loadData();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);

        JLabel title = new JLabel("LỊCH SỬ KIỂM KÊ KHO");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSearch.setForeground(TEXT_SECONDARY);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(360, 38));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 12, 0, 12)
        ));
        txtSearch.getDocument().addDocumentListener(new SimpleDocumentListener(this::filterData));

        JButton btnRefresh = createModernButton("Làm mới", new Color(71, 85, 105));
        btnRefresh.setPreferredSize(new Dimension(130, 38));
        btnRefresh.setToolTipText("Tải lại dữ liệu lịch sử kiểm kê");
        btnRefresh.addActionListener(e -> lamMoiKhongThongBao());

        right.add(lblSearch);
        right.add(txtSearch);
        right.add(btnRefresh);

        panel.add(title, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);

        return panel;
    }
    private void lamMoiKhongThongBao() {
        if (txtSearch != null) {
            txtSearch.setText("");
        }

        loadData();

        if (tablePhieu != null) {
            tablePhieu.clearSelection();
        }

        if (tableChiTiet != null) {
            modelChiTiet.setRowCount(0);
        }

        if (lblTitleChiTiet != null) {
            lblTitleChiTiet.setText("Chi tiết phiếu kiểm kê");
        }
    }
    private JSplitPane createBody() {
        JPanel pnlPhieu = createPhieuPanel();
        JPanel pnlChiTiet = createChiTietPanel();

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pnlPhieu, pnlChiTiet);
        split.setResizeWeight(0.55);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setOpaque(false);

        return split;
    }

    private JPanel createPhieuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(BORDER));

        String[] cols = {
                "STT",
                "Mã phiếu",
                "Thời gian",
                "Kho",
                "Số dòng",
                "Tổng chênh lệch",
                "Người thực hiện",
                "Ghi chú",
                "ID_ẨN"
        };

        modelPhieu = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablePhieu = new JTable(modelPhieu);
        styleTable(tablePhieu);
        tablePhieu.setDefaultRenderer(Object.class, new HistoryCellRenderer());

        tablePhieu.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                hienThiChiTietDangChon();
            }
        });

        tablePhieu.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablePhieu.getColumnModel().getColumn(1).setPreferredWidth(120);
        tablePhieu.getColumnModel().getColumn(2).setPreferredWidth(160);
        tablePhieu.getColumnModel().getColumn(3).setPreferredWidth(100);
        tablePhieu.getColumnModel().getColumn(4).setPreferredWidth(80);
        tablePhieu.getColumnModel().getColumn(5).setPreferredWidth(130);
        tablePhieu.getColumnModel().getColumn(6).setPreferredWidth(180);
        tablePhieu.getColumnModel().getColumn(7).setPreferredWidth(280);

        int hidden = tablePhieu.getColumnModel().getColumnCount() - 1;
        tablePhieu.getColumnModel().getColumn(hidden).setMinWidth(0);
        tablePhieu.getColumnModel().getColumn(hidden).setMaxWidth(0);
        tablePhieu.getColumnModel().getColumn(hidden).setWidth(0);
        tablePhieu.getColumnModel().getColumn(hidden).setPreferredWidth(0);

        JScrollPane scroll = createSmoothScrollPane(tablePhieu);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createChiTietPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10, 10, 10, 10)
        ));

        lblTitleChiTiet = new JLabel("Chi tiết phiếu kiểm kê");
        lblTitleChiTiet.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitleChiTiet.setForeground(TEXT_PRIMARY);

        String[] cols = {
                "STT",
                "Mã lô",
                "Sản phẩm",
                "Kho",
                "Tồn hệ thống",
                "Tồn thực tế",
                "Chênh lệch",
                "Lý do"
        };

        modelChiTiet = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableChiTiet = new JTable(modelChiTiet);
        styleTable(tableChiTiet);
        tableChiTiet.setDefaultRenderer(Object.class, new HistoryCellRenderer());

        tableChiTiet.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableChiTiet.getColumnModel().getColumn(1).setPreferredWidth(130);
        tableChiTiet.getColumnModel().getColumn(2).setPreferredWidth(320);
        tableChiTiet.getColumnModel().getColumn(3).setPreferredWidth(100);
        tableChiTiet.getColumnModel().getColumn(4).setPreferredWidth(110);
        tableChiTiet.getColumnModel().getColumn(5).setPreferredWidth(110);
        tableChiTiet.getColumnModel().getColumn(6).setPreferredWidth(110);
        tableChiTiet.getColumnModel().getColumn(7).setPreferredWidth(260);

        JScrollPane scroll = createSmoothScrollPane(tableChiTiet);

        panel.add(lblTitleChiTiet, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    public void loadData() {
        dsPhieuGoc = bus.layDanhSachPhieuKiemKe();

        if (dsPhieuGoc == null) {
            dsPhieuGoc = new ArrayList<>();
        }

        dsPhieuDangHienThi = new ArrayList<>(dsPhieuGoc);
        renderPhieuTable();

        if (tablePhieu.getRowCount() > 0) {
            tablePhieu.setRowSelectionInterval(0, 0);
        } else {
            modelChiTiet.setRowCount(0);
            lblTitleChiTiet.setText("Chi tiết phiếu kiểm kê");
        }
    }

    private void filterData() {
        String keyword = txtSearch.getText() == null
                ? ""
                : txtSearch.getText().trim().toLowerCase();

        dsPhieuDangHienThi.clear();

        for (PhieuKiemKeHistory p : dsPhieuGoc) {
            boolean match = keyword.isEmpty()
                    || safe(p.getId()).toLowerCase().contains(keyword)
                    || safe(p.getKhoHangId()).toLowerCase().contains(keyword)
                    || safe(p.getNhanVienId()).toLowerCase().contains(keyword)
                    || safe(p.getTenNhanVien()).toLowerCase().contains(keyword)
                    || safe(p.getGhiChu()).toLowerCase().contains(keyword);

            if (match) {
                dsPhieuDangHienThi.add(p);
            }
        }

        renderPhieuTable();

        if (tablePhieu.getRowCount() > 0) {
            tablePhieu.setRowSelectionInterval(0, 0);
        } else {
            modelChiTiet.setRowCount(0);
            lblTitleChiTiet.setText("Chi tiết phiếu kiểm kê");
        }
    }

    private void renderPhieuTable() {
        modelPhieu.setRowCount(0);

        int stt = 1;

        for (PhieuKiemKeHistory p : dsPhieuDangHienThi) {
            modelPhieu.addRow(new Object[]{
                    stt++,
                    p.getId(),
                    p.getNgayKiemKe() == null ? "" : sdf.format(p.getNgayKiemKe()),
                    p.getKhoHangId(),
                    p.getTongSoDong(),
                    p.getTongChenhLech(),
                    safe(p.getTenNhanVien()),
                    safe(p.getGhiChu()),
                    p.getId()
            });
        }
    }

    private void hienThiChiTietDangChon() {
        int viewRow = tablePhieu.getSelectedRow();

        if (viewRow < 0) {
            return;
        }

        int modelRow = tablePhieu.convertRowIndexToModel(viewRow);
        String phieuId = safe(modelPhieu.getValueAt(modelRow, 8));

        lblTitleChiTiet.setText("Chi tiết phiếu kiểm kê " + phieuId);

        List<ChiTietKiemKeHistory> ds = bus.layChiTietTheoPhieu(phieuId);

        modelChiTiet.setRowCount(0);

        int stt = 1;

        for (ChiTietKiemKeHistory ct : ds) {
            modelChiTiet.addRow(new Object[]{
                    stt++,
                    ct.getSoLoHang(),
                    ct.getSanPhamId() + " - " + ct.getTenSanPham(),
                    ct.getKhoHangId(),
                    ct.getTonHeThong(),
                    ct.getTonThucTe(),
                    ct.getChenhLech(),
                    safe(ct.getLyDo())
            });
        }
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    private void styleTable(JTable table) {
        table.setRowHeight(42);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(ROW_SELECTED);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(new Color(241, 245, 249));
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.enableInputMethods(false);

        JTableHeader header = table.getTableHeader();

        if (header != null) {
            header.setFont(new Font("Segoe UI", Font.BOLD, 14));
            header.setBackground(HEADER);
            header.setForeground(TEXT_PRIMARY);
            header.setPreferredSize(new Dimension(header.getWidth(), 38));
            header.setReorderingAllowed(false);
            header.enableInputMethods(false);
            header.setFocusable(false);
        }
    }

    private JScrollPane createSmoothScrollPane(Component view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(Color.WHITE);

        scroll.getVerticalScrollBar().setUI(new SmoothScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setBlockIncrement(80);

        scroll.getHorizontalScrollBar().setUI(new SmoothScrollBarUI());
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        scroll.getHorizontalScrollBar().setUnitIncrement(18);
        scroll.getHorizontalScrollBar().setBlockIncrement(80);

        return scroll;
    }

    private JButton createModernButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.enableInputMethods(false);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(color.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(color);
            }
        });

        return btn;
    }

    private void showModernAlert(String title, String message, Color color) {
        Window owner = SwingUtilities.getWindowAncestor(this);

        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(color, 2));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(color);
        header.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);

        JButton btnX = new JButton("×");
        btnX.setFont(new Font("Segoe UI", Font.BOLD, 22));
        btnX.setForeground(Color.WHITE);
        btnX.setFocusPainted(false);
        btnX.setBorderPainted(false);
        btnX.setContentAreaFilled(false);
        btnX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnX.addActionListener(e -> dialog.dispose());

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnX, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(22, 26, 18, 26));

        JLabel icon = new JLabel(color == SUCCESS ? "✓" : "!");
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setVerticalAlignment(SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 30));
        icon.setForeground(color);
        icon.setPreferredSize(new Dimension(52, 52));

        JLabel msg = new JLabel("<html><div style='width:380px; font-family:Segoe UI; font-size:13px; color:#0f172a;'>"
                + escapeHtml(message).replace("\n", "<br>")
                + "</div></html>");

        body.add(icon, BorderLayout.WEST);
        body.add(msg, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footer.setBackground(Color.WHITE);

        JButton btnClose = createModernButton("Đóng", color);
        btnClose.addActionListener(e -> dialog.dispose());

        footer.add(btnClose);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String escapeHtml(String s) {
        if (s == null) {
            return "";
        }

        return s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private class HistoryCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table,
                    value,
                    isSelected,
                    hasFocus,
                    row,
                    column
            );

            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setBorder(new EmptyBorder(0, 8, 0, 8));
            label.setOpaque(true);

            if (isSelected) {
                label.setBackground(ROW_SELECTED);
                label.setForeground(TEXT_PRIMARY);
            } else {
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
                label.setForeground(TEXT_PRIMARY);
            }

            if (column == 0 || column == 3 || column == 4 || column == 5 || column == 6) {
                label.setHorizontalAlignment(SwingConstants.RIGHT);
            } else {
                label.setHorizontalAlignment(SwingConstants.LEFT);
            }

            if (column == 5 || column == 6) {
                int val = 0;

                try {
                    val = Integer.parseInt(safe(value));
                } catch (Exception ignored) {
                }

                label.setFont(new Font("Segoe UI", Font.BOLD, 14));

                if (val > 0) {
                    label.setForeground(SUCCESS);
                } else if (val < 0) {
                    label.setForeground(DANGER);
                } else {
                    label.setForeground(TEXT_SECONDARY);
                }
            }

            return label;
        }
    }

    private static class SmoothScrollBarUI extends BasicScrollBarUI {
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
            return btn;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(248, 250, 252));
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds == null || thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color thumb = isDragging
                    ? new Color(100, 116, 139)
                    : new Color(148, 163, 184);

            g2.setColor(thumb);

            int pad = 3;

            if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
                int x = thumbBounds.x + pad;
                int y = thumbBounds.y + pad;
                int width = thumbBounds.width - pad * 2;
                int height = thumbBounds.height - pad * 2;
                g2.fillRoundRect(x, y, Math.max(width, 4), Math.max(height, 18), 8, 8);
            } else {
                int x = thumbBounds.x + pad;
                int y = thumbBounds.y + pad;
                int width = thumbBounds.width - pad * 2;
                int height = thumbBounds.height - pad * 2;
                g2.fillRoundRect(x, y, Math.max(width, 18), Math.max(height, 4), 8, 8);
            }

            g2.dispose();
        }
    }

    private interface ChangeCallback {
        void run();
    }

    private static class SimpleDocumentListener implements DocumentListener {
        private final ChangeCallback callback;

        public SimpleDocumentListener(ChangeCallback callback) {
            this.callback = callback;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            callback.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            callback.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            callback.run();
        }
    }
}