package GUI;

import Utils.MenuIcon;
import Utils.TelexFix;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ManHinhNhatKyKho extends JPanel {

    private static final Color HEADER_BG = new Color(30, 64, 96);
    private static final Color BG_APP = new Color(248, 250, 252);
    private static final Color TAB_BG = new Color(248, 250, 252);
    private static final Color TAB_ACTIVE = Color.WHITE;
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color PRIMARY = new Color(30, 64, 96);

    private JPanel pnlContent;
    private CardLayout cardLayout;

    private TabButton tabXuat;
    private TabButton tabNhap;
    private TabButton tabKiemKe;

    private ManHinhLichSuXuat pnlLichSuXuat;
    private ManHinhLichSuNhap pnlLichSuNhap;
    private ManHinhLichSuKiemKeKho pnlLichSuKiemKe;

    public ManHinhNhatKyKho() {
        setLayout(new BorderLayout());
        setBackground(BG_APP);

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        TelexFix.applyDeep(this);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(14, 22, 14, 22));

        JLabel lblTitle = new JLabel("NHẬT KÝ LỊCH SỬ");
        lblTitle.setIcon(new MenuIcon("TIME"));
        lblTitle.setIconTextGap(10);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 21));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JButton btnRefresh = new JButton("Làm mới");
        btnRefresh.setIcon(new MenuIcon("REFRESH"));
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBackground(new Color(71, 99, 132));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorder(new EmptyBorder(9, 16, 9, 16));
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> refreshCurrentTab());

        right.add(btnRefresh);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(BG_APP);

        body.add(createTabBar(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        pnlContent = new JPanel(cardLayout);
        pnlContent.setBackground(BG_APP);

        pnlLichSuXuat = new ManHinhLichSuXuat();
        pnlLichSuNhap = new ManHinhLichSuNhap();
        pnlLichSuKiemKe = new ManHinhLichSuKiemKeKho();

        pnlContent.add(pnlLichSuXuat, "XUAT");
        pnlContent.add(pnlLichSuNhap, "NHAP");
        pnlContent.add(pnlLichSuKiemKe, "KIEM_KE");

        body.add(pnlContent, BorderLayout.CENTER);

        setActiveTab("XUAT");

        return body;
    }

    private JPanel createTabBar() {
        JPanel pnlTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTabs.setBackground(TAB_BG);
        pnlTabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        tabXuat = new TabButton("Lịch sử phiếu xuất kho", new MenuIcon("BOX"));
        tabNhap = new TabButton("Lịch sử phiếu lô hàng", new MenuIcon("TIME"));
        tabKiemKe = new TabButton("Lịch sử kiểm kê kho", new MenuIcon("LIST"));

        tabXuat.addActionListener(e -> setActiveTab("XUAT"));
        tabNhap.addActionListener(e -> setActiveTab("NHAP"));
        tabKiemKe.addActionListener(e -> setActiveTab("KIEM_KE"));

        pnlTabs.add(tabXuat);
        pnlTabs.add(tabNhap);
        pnlTabs.add(tabKiemKe);

        return pnlTabs;
    }

    private void setActiveTab(String tab) {
        boolean isXuat = "XUAT".equals(tab);
        boolean isNhap = "NHAP".equals(tab);
        boolean isKiemKe = "KIEM_KE".equals(tab);

        if (tabXuat != null) {
            tabXuat.setActive(isXuat);
        }

        if (tabNhap != null) {
            tabNhap.setActive(isNhap);
        }

        if (tabKiemKe != null) {
            tabKiemKe.setActive(isKiemKe);
        }

        if (cardLayout != null && pnlContent != null) {
            cardLayout.show(pnlContent, tab);
        }
    }

    private void refreshCurrentTab() {
        if (tabXuat != null && tabXuat.isActive()) {
            if (pnlLichSuXuat != null) {
                pnlLichSuXuat.loadData();
            }
            return;
        }

        if (tabNhap != null && tabNhap.isActive()) {
            if (pnlLichSuNhap != null) {
                pnlLichSuNhap.loadData();
            }
            return;
        }

        if (tabKiemKe != null && tabKiemKe.isActive()) {
            if (pnlLichSuKiemKe != null) {
                pnlLichSuKiemKe.loadData();
            }
        }
    }

    private static class TabButton extends JButton {
        private boolean active = false;

        public TabButton(String text, Icon icon) {
            super(text, icon);

            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(SwingConstants.LEFT);
            setIconTextGap(9);
            setBorder(new EmptyBorder(17, 28, 15, 28));
            setPreferredSize(new Dimension(310, 72));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!active) {
                        setForeground(TEXT_PRIMARY);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!active) {
                        setForeground(TEXT_SECONDARY);
                    }
                }
            });
        }

        public void setActive(boolean active) {
            this.active = active;

            if (active) {
                setForeground(PRIMARY);
                setBackground(TAB_ACTIVE);
            } else {
                setForeground(TEXT_SECONDARY);
                setBackground(TAB_BG);
            }

            repaint();
        }

        public boolean isActive() {
            return active;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (active) {
                g2.setColor(TAB_ACTIVE);
                g2.fillRect(0, 0, getWidth(), getHeight());

                g2.setColor(PRIMARY);
                g2.fillRect(0, getHeight() - 3, getWidth(), 3);
            } else {
                g2.setColor(TAB_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            g2.dispose();

            super.paintComponent(g);
        }
    }
}