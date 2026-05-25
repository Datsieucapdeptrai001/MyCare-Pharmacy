package GUI;

import BUS.BUS_PhieuNhapHang;
import Entity.ChiTietPhieuNhapHang;
import Utils.MenuIcon;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ManHinhChiTietPhieuNhap extends JPanel {

    private static final Color BG_APP = new Color(241, 245, 249);
    private static final Color PRIMARY_BLUE = new Color(14, 116, 144);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);

    private final String maPN;
    private final BUS_PhieuNhapHang busPhieuNhap = new BUS_PhieuNhapHang();
    private final NumberFormat moneyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JTable table;
    private DefaultTableModel model;

    public ManHinhChiTietPhieuNhap(String maPN) {
        this.maPN = maPN;
        moneyFormat.setMaximumFractionDigits(0);

        setLayout(new BorderLayout());
        setBackground(BG_APP);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundRoundBorder(
                new SmoothShadowBorder(new Color(0, 0, 0, 12), 16),
                new Insets(20, 25, 20, 25)));

        card.add(createHeader(), BorderLayout.NORTH);
        card.add(createTable(), BorderLayout.CENTER);
        card.add(createFooter(), BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);

        loadData();
    }

    private JPanel createHeader() {
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblTitle = new JLabel("CHI TIẾT PHIẾU NHẬP " + maPN);
        lblTitle.setIcon(new MenuIcon("DOCUMENT", 24, PRIMARY_BLUE));
        lblTitle.setIconTextGap(8);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 21));
        lblTitle.setForeground(TEXT_PRIMARY);

        pnlTop.add(lblTitle, BorderLayout.WEST);
        return pnlTop;
    }

    private JPanel createTable() {
        JPanel pnlWrap = new JPanel(new BorderLayout());
        pnlWrap.setOpaque(false);

        String[] columns = {
                "STT",
                "Mã lô",
                "Mã SP",
                "Kho",
                "Số lô",
                "SL nhập",
                "Đơn giá",
                "Thành tiền",
                "HSD"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(44);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_COLOR);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setDefaultRenderer(Object.class, new ChiTietRenderer());

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(204, 226, 241));
        header.setForeground(new Color(51, 65, 85));
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(120);
        table.getColumnModel().getColumn(7).setPreferredWidth(130);
        table.getColumnModel().getColumn(8).setPreferredWidth(100);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8));
        scroll.getViewport().setBackground(Color.WHITE);

        pnlWrap.add(scroll, BorderLayout.CENTER);
        return pnlWrap;
    }

    private JPanel createFooter() {
        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlBot.setOpaque(false);

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setForeground(Color.WHITE);
        btnClose.setBackground(PRIMARY_BLUE);
        btnClose.setFocusPainted(false);
        btnClose.setPreferredSize(new Dimension(120, 42));
        btnClose.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w != null) {
                w.dispose();
            }
        });

        pnlBot.add(btnClose);
        return pnlBot;
    }

    private void loadData() {
        model.setRowCount(0);

        try {
            List<ChiTietPhieuNhapHang> ds = busPhieuNhap.layChiTietTheoPhieuNhap(maPN);

            int stt = 1;

            for (ChiTietPhieuNhapHang ct : ds) {
                String loHangId = ct.getLoHangId() != null ? ct.getLoHangId().getId() : "";
                String sanPhamId = ct.getSanPhamId() != null ? ct.getSanPhamId().getId() : "";
                String khoId = ct.getKhoHangId() != null ? ct.getKhoHangId().getId() : "";
                String hsd = ct.getHanSuDung() != null ? ct.getHanSuDung().format(df) : "";

                model.addRow(new Object[] {
                        stt++,
                        loHangId,
                        sanPhamId,
                        khoId,
                        ct.getSoLoHang(),
                        ct.getSoLuongNhap(),
                        moneyFormat.format(ct.getDonGiaNhap()) + "đ",
                        moneyFormat.format(ct.getThanhTien()) + "đ",
                        hsd
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi khi tải chi tiết phiếu nhập!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private class ChiTietRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
            lbl.setOpaque(true);

            if (isSelected) {
                lbl.setBackground(new Color(224, 242, 254));
                lbl.setForeground(TEXT_PRIMARY);
            } else {
                lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
                lbl.setForeground(TEXT_PRIMARY);
            }

            if (column == 6 || column == 7) {
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            }

            return lbl;
        }
    }

    private static class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int radius;

        public RoundedLineBorder(Color c, int t, int r) {
            color = c;
            thickness = t;
            radius = r;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);

            for (int i = 0; i < thickness; i++) {
                g2.drawRoundRect(x + i, y + i, w - 1 - i * 2, h - 1 - i * 2, radius, radius);
            }

            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
    }

    private static class SmoothShadowBorder extends AbstractBorder {
        private final Color shadow;
        private final int radius;

        public SmoothShadowBorder(Color s, int r) {
            shadow = s;
            radius = r;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 4, 8, 4);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (int i = 0; i < 6; i++) {
                g2.setColor(new Color(
                        shadow.getRed(),
                        shadow.getGreen(),
                        shadow.getBlue(),
                        Math.max(1, shadow.getAlpha() - i * 2)));
                g2.drawRoundRect(x + 1, y + 1 + i, w - 3, h - 3 - i, radius, radius);
            }

            g2.dispose();
        }
    }

    private static class CompoundRoundBorder extends AbstractBorder {
        private final AbstractBorder outer;
        private final Insets inner;

        public CompoundRoundBorder(AbstractBorder o, Insets i) {
            outer = o;
            inner = i;
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