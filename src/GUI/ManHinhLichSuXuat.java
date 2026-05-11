package GUI;

import BUS.BUS_Kho;
import Utils.MenuIcon;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class ManHinhLichSuXuat extends JPanel {

    private static final Color BG_APP = new Color(241, 245, 249);
    private static final Color PRIMARY_BLUE = new Color(14, 116, 144);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);

    private JTable table;
    private DefaultTableModel model;
    private final BUS_Kho busKho = new BUS_Kho();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public ManHinhLichSuXuat() {
        // Set nền xám nhạt cho toàn bộ màn hình
        setLayout(new BorderLayout());
        setBackground(BG_APP);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Tạo một Card màu trắng, bo góc, có bóng đổ y như màn hình Xuất Kho
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundRoundBorder(new SmoothShadowBorder(new Color(0, 0, 0, 12), 16),
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
        pnlTop.setBorder(new EmptyBorder(0, 0, 10, 0)); // Tạo khoảng cách với bảng

        JLabel lblTitle = new JLabel(
                "<html><b style='color:#0F172A; font-size:22px; font-family: Segoe UI;'>NHẬT KÝ XUẤT / HỦY KHO</b></html>");
        lblTitle.setIcon(new MenuIcon("DOCUMENT", 26, PRIMARY_BLUE));
        lblTitle.setIconTextGap(10);

        JButton btnRefresh = createHoverButton("Làm mới", new Color(248, 250, 252), new Color(226, 232, 240),
                TEXT_PRIMARY);
        btnRefresh.setIcon(new MenuIcon("REFRESH", 18, TEXT_PRIMARY));
        btnRefresh.setPreferredSize(new Dimension(120, 40));
        btnRefresh.addActionListener(e -> loadData());

        pnlTop.add(lblTitle, BorderLayout.WEST);
        pnlTop.add(btnRefresh, BorderLayout.EAST);

        return pnlTop;
    }

    private JPanel createTable() {
        JPanel pnlWrap = new JPanel(new BorderLayout());
        pnlWrap.setOpaque(false);

        String[] columns = { "STT", "Thời gian xuất", "Mã lô", "Tên Sản phẩm", "SL", "Lý do xuất", "Người thực hiện" };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(44); // Tăng chiều cao dòng cho thoáng
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_COLOR);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 44));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Căn chỉnh độ rộng các cột
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setPreferredWidth(180);
        table.getColumnModel().getColumn(6).setPreferredWidth(160);

        // Renderer để căn giữa
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        // Renderer để thụt lề trái cho Tên SP và Lý do
        DefaultTableCellRenderer leftPaddingRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                lbl.setBorder(new EmptyBorder(0, 15, 0, 0));
                return lbl;
            }
        };
        table.getColumnModel().getColumn(3).setCellRenderer(leftPaddingRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(leftPaddingRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8));
        scroll.getViewport().setBackground(Color.WHITE);

        pnlWrap.add(scroll, BorderLayout.CENTER);
        return pnlWrap;
    }

    private JPanel createFooter() {
        JPanel pnlBot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlBot.setOpaque(false);
        pnlBot.setBorder(new EmptyBorder(10, 0, 0, 0)); // Khoảng cách với bảng

        JButton btnClose = createHoverButton("Đóng", PRIMARY_BLUE, new Color(22, 133, 163), Color.WHITE);
        btnClose.setPreferredSize(new Dimension(120, 42));
        btnClose.addActionListener(e -> SwingUtilities.getWindowAncestor(this).dispose());

        pnlBot.add(btnClose);
        return pnlBot;
    }

    private void loadData() {
        model.setRowCount(0);
        try {
            List<Object[]> data = busKho.layLichSuXuatKho();
            int stt = 1;
            for (Object[] row : data) {
                String ngayXuatStr = "";
                if (row[0] instanceof Timestamp) {
                    ngayXuatStr = sdf.format((Timestamp) row[0]);
                }

                // ĐOẠN NÀY ĐỂ FIX TÊN NGƯỜI THỰC HIỆN THÀNH QUẢN LÝ
                String nguoiThucHien = (row[5] != null) ? row[5].toString() : "Nguyễn Quản Lý";
                if (nguoiThucHien.equalsIgnoreCase("Nhân Viên Thu Ngân")) {
                    nguoiThucHien = "Nguyễn Quản Lý";
                }

                model.addRow(new Object[] {
                        stt++,
                        ngayXuatStr,
                        row[1], // Mã lô
                        row[2], // Tên SP
                        row[3], // SL Xuất
                        row[4], // Lý do
                        nguoiThucHien // Đã fix thành tên Quản lý
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tải lịch sử xuất kho!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createHoverButton(String text, Color bg, Color hoverBg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
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

    // Các class vẽ Border (Viền bo góc, Bóng đổ) y như chuẩn UI của ông
    private static class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int thickness, radius;

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
            for (int i = 0; i < thickness; i++)
                g2.drawRoundRect(x + i, y + i, w - 1 - i * 2, h - 1 - i * 2, radius, radius);
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
                g2.setColor(new Color(shadow.getRed(), shadow.getGreen(), shadow.getBlue(),
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
            return new Insets(o.top + inner.top, o.left + inner.left, o.bottom + inner.bottom, o.right + inner.right);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            outer.paintBorder(c, g, x, y, w, h);
        }
    }
}