package GUI;

import Utils.MenuIcon;
import ConnectDB.ConnectDB;
import Enum.VaiTro;
import Utils.UserSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainDashboard extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private List<JButton> menuButtons;

    private final String[] ALL_MENU_ITEMS = {
        "Màn hình chính", "Bán hàng & Đổi trả", "Sản phẩm", "Lô hàng",
        "Khuyến mại", "Thống kê", "Nhân viên", "Khách hàng", "Hướng dẫn"
    };
    // STAFF: không có "Thống kê"
    private final String[] STAFF_MENU_ITEMS = {
        "Màn hình chính", "Bán hàng & Đổi trả", "Sản phẩm", "Lô hàng",
        "Khuyến mại", "Nhân viên", "Khách hàng", "Hướng dẫn"
    };
    private String[] menuItems;

    public MainDashboard() {
        boolean isAdmin = UserSession.getInstance().isAdmin();
        menuItems = isAdmin ? ALL_MENU_ITEMS : STAFF_MENU_ITEMS;

        setTitle("MYCARE PHARMACY - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        menuButtons = new ArrayList<>();
        cardLayout  = new CardLayout();
        cardPanel   = new JPanel(cardLayout);

        ManHinhChinh     mhChinh    = new ManHinhChinh();
        ManHinhBanHang   mhBanHang  = new ManHinhBanHang();
        ManHinhSanPham   mhSanPham  = new ManHinhSanPham();
        ManHinhLoHang    mhLoHang   = new ManHinhLoHang();
        ManHinhKhuyenMai mhKhuyenMai= new ManHinhKhuyenMai();
        ManHinhThongKe   mhThongKe  = new ManHinhThongKe();
        ManHinhNhanVien  mhNhanVien = new ManHinhNhanVien();
        ManHinhKhachHang mhKhachHang= new ManHinhKhachHang();
        ManHinhHuongDan  mhHuongDan = new ManHinhHuongDan();

        cardPanel.add(mhChinh,     "Màn hình chính");
        cardPanel.add(mhBanHang,   "Bán hàng & Đổi trả");
        cardPanel.add(mhSanPham,   "Sản phẩm");
        cardPanel.add(mhLoHang,    "Lô hàng");
        cardPanel.add(mhKhuyenMai, "Khuyến mại");
        cardPanel.add(mhThongKe,   "Thống kê");
        cardPanel.add(mhNhanVien,  "Nhân viên");
        cardPanel.add(mhKhachHang, "Khách hàng");
        cardPanel.add(mhHuongDan,  "Hướng dẫn");

        // Phân quyền: STAFF bị readonly
//        if (!isAdmin) {
//            mhSanPham.setReadOnly(true);
//            mhLoHang.setReadOnly(true);
//            mhKhuyenMai.setReadOnly(true);
//            mhNhanVien.setReadOnly(true);
//        }

        JPanel sidebar    = createSidebar();
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(createTopHeader(), BorderLayout.NORTH);
        rightPanel.add(cardPanel,         BorderLayout.CENTER);

        add(sidebar,    BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        cardLayout.show(cardPanel, "Màn hình chính");
    }

    // ── TOP HEADER ──────────────────────────────────────────────
    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));

        String placeholder = "🔍 Tìm kiếm...";
        JTextField txtSearch = new JTextField(placeholder);
        txtSearch.setBackground(Color.decode("#F4F6F8"));
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        txtSearch.setPreferredSize(new Dimension(350, 40));
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals(placeholder)) { txtSearch.setText(""); txtSearch.setForeground(Color.BLACK); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (txtSearch.getText().trim().isEmpty()) { txtSearch.setForeground(Color.GRAY); txtSearch.setText(placeholder); }
            }
        });

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        pnlSearch.setBackground(Color.WHITE);
        pnlSearch.add(txtSearch);
        header.add(pnlSearch, BorderLayout.WEST);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        pnlRight.setBackground(Color.WHITE);

        JLabel lblTime = new JLabel();
        lblTime.setForeground(Color.decode("#637381"));
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        new Timer(1000, ev -> lblTime.setText(
                new SimpleDateFormat("HH:mm:ss — E dd/MM/yyyy").format(new Date()))).start();

        // Tên & vai trò từ DB qua UserSession
        String initials = UserSession.getInstance().getInitials();
        String tenNV    = UserSession.getInstance().getTenHienThi();
        boolean isAdmin = UserSession.getInstance().isAdmin();

        JButton btnUser = new JButton(initials + "  " + tenNV + "  ▼");
        btnUser.setBackground(isAdmin ? Color.decode("#E53935") : Color.decode("#1A73E8"));
        btnUser.setForeground(Color.WHITE);
        btnUser.setFocusPainted(false);
        btnUser.setBorderPainted(false);
        btnUser.setOpaque(true);
        btnUser.setFont(new Font("Segoe UI", Font.BOLD, 13));

        pnlRight.add(lblTime);
        pnlRight.add(btnUser);
        pnlRight.add(new JLabel("🔔"));
        header.add(pnlRight, BorderLayout.EAST);
        return header;
    }

    // ── SIDEBAR ─────────────────────────────────────────────────
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.decode("#152A4B"));
        sidebar.setPreferredSize(new Dimension(230, 0));

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        logoPanel.setBackground(Color.decode("#152A4B"));
        logoPanel.setMaximumSize(new Dimension(230, 70));
        logoPanel.add(new JLabel("<html><b style='color:white;font-size:16px;'>MYCARE</b><br>"
                + "<span style='color:#00BFFF;font-size:11px;'>PHARMACY</span></html>"));
        sidebar.add(logoPanel);

        // User info
        String initials = UserSession.getInstance().getInitials();
        String tenNV    = UserSession.getInstance().getTenHienThi();
        String chucVu   = UserSession.getInstance().getChucVuHienThi();
        boolean isAdmin = UserSession.getInstance().isAdmin();

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        userPanel.setBackground(Color.decode("#152A4B"));
        userPanel.setMaximumSize(new Dimension(230, 70));

        JLabel lblAvatar = new JLabel(initials, SwingConstants.CENTER);
        lblAvatar.setOpaque(true);
        lblAvatar.setBackground(isAdmin ? Color.decode("#C62828") : Color.decode("#1362B1"));
        lblAvatar.setForeground(Color.WHITE);
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAvatar.setPreferredSize(new Dimension(40, 40));

        userPanel.add(lblAvatar);
        userPanel.add(new JLabel("<html><b style='color:white;font-size:13px;'>" + tenNV + "</b><br>"
                + "<span style='color:#00BFFF;font-size:11px;'>" + chucVu + "</span></html>"));
        sidebar.add(userPanel);

        // Badge vai trò
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 2));
        badgePanel.setBackground(Color.decode("#152A4B"));
        badgePanel.setMaximumSize(new Dimension(230, 26));
        JLabel lblBadge = new JLabel(isAdmin ? "● Quản lý · Toàn quyền" : "● Nhân viên · Dược sĩ");
        lblBadge.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBadge.setForeground(isAdmin ? Color.decode("#FFAB00") : Color.decode("#00A76F"));
        badgePanel.add(lblBadge);
        sidebar.add(badgePanel);

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        for (String item : menuItems) {
            JButton btn = createMenuButton(item, false);
            if      (item.equals("Màn hình chính"))      btn.setIcon(new MenuIcon("HOME"));
            else if (item.equals("Bán hàng & Đổi trả"))  btn.setIcon(new MenuIcon("CART"));
            else if (item.equals("Sản phẩm"))             btn.setIcon(new MenuIcon("PILL"));
            else if (item.equals("Lô hàng"))              btn.setIcon(new MenuIcon("BOX"));
            else if (item.equals("Khuyến mại"))           btn.setIcon(new MenuIcon("GIFT"));
            else if (item.equals("Thống kê"))             btn.setIcon(new MenuIcon("CHART"));
            else if (item.equals("Nhân viên"))            btn.setIcon(new MenuIcon("USER"));
            else if (item.equals("Khách hàng"))           btn.setIcon(new MenuIcon("USERS"));
            else if (item.equals("Hướng dẫn"))            btn.setIcon(new MenuIcon("HELP"));
            menuButtons.add(btn);
            sidebar.add(btn);
        }

        setActiveButton(menuButtons.get(0));
        sidebar.add(Box.createVerticalGlue());

        JButton btnLogout = createMenuButton("Đăng xuất", true);
        btnLogout.setIcon(new MenuIcon("LOGOUT"));
        btnLogout.addActionListener(e -> hienThiThongBaoDangXuat());
        sidebar.add(btnLogout);

        return sidebar;
    }

    private void hienThiThongBaoDangXuat() {
        JDialog dialog = new JDialog(this, "Xác nhận", true);
        dialog.setSize(420, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.decode("#152A4B"), 2));
        mainPanel.setBackground(Color.WHITE);

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(Color.decode("#152A4B"));
        hdr.setPreferredSize(new Dimension(0, 40));
        JLabel lblTitle = new JLabel("   XÁC NHẬN ĐĂNG XUẤT");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JButton btnX = new JButton("✕ ");
        btnX.setFocusPainted(false); btnX.setBorderPainted(false);
        btnX.setContentAreaFilled(false); btnX.setForeground(Color.WHITE);
        btnX.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnX.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnX.addActionListener(e -> dialog.dispose());
        hdr.add(lblTitle, BorderLayout.WEST); hdr.add(btnX, BorderLayout.EAST);
        mainPanel.add(hdr, BorderLayout.NORTH);

        JPanel body = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 35));
        body.setBackground(Color.WHITE);
        body.add(new JLabel(new MenuIcon("WARNING")));
        JLabel lMsg = new JLabel("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?");
        lMsg.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        body.add(lMsg);
        mainPanel.add(body, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setBackground(Color.decode("#F4F6F8"));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#DFE3E8")));

        JButton btnHuy = new JButton("Hủy");
        btnHuy.setFont(new Font("Segoe UI", Font.BOLD, 13)); btnHuy.setBackground(Color.WHITE);
        btnHuy.setForeground(Color.decode("#637381")); btnHuy.setFocusPainted(false);
        btnHuy.setPreferredSize(new Dimension(90, 35)); btnHuy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHuy.addActionListener(e -> dialog.dispose());

        JButton btnXN = new JButton("Đăng xuất");
        btnXN.setFont(new Font("Segoe UI", Font.BOLD, 13)); btnXN.setBackground(Color.decode("#FF4D4D"));
        btnXN.setForeground(Color.WHITE); btnXN.setFocusPainted(false);
        btnXN.setPreferredSize(new Dimension(110, 35)); btnXN.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXN.addActionListener(e -> {
            dialog.dispose();
            UserSession.getInstance().logout();
            this.dispose();
            new ManHinhDangNhap().setVisible(true);
        });
        footer.add(btnHuy); footer.add(btnXN);
        mainPanel.add(footer, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private JButton createMenuButton(String text, boolean isLogout) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(230, 50));
        btn.setPreferredSize(new Dimension(230, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false); btn.setOpaque(true); btn.setBorderPainted(true);
        btn.setIconTextGap(15);
        btn.setBackground(Color.decode("#152A4B"));
        btn.setForeground(isLogout ? Color.decode("#FF4D4D") : Color.decode("#E8F0FE"));
        javax.swing.border.Border bottom  = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#1E3F70"));
        javax.swing.border.Border padding = BorderFactory.createEmptyBorder(0, 20, 0, 0);
        btn.setBorder(BorderFactory.createCompoundBorder(bottom, padding));
        if (!isLogout) {
            btn.addActionListener(e -> { setActiveButton(btn); cardLayout.show(cardPanel, text); });
        }
        return btn;
    }

    private void setActiveButton(JButton activeBtn) {
        for (JButton btn : menuButtons) {
            boolean active = (btn == activeBtn);
            btn.setBackground(active ? Color.decode("#1A73E8") : Color.decode("#152A4B"));
            btn.setForeground(active ? Color.WHITE : Color.decode("#E8F0FE"));
        }
    }

    public void chuyenSangTabKhachHang(boolean moFormThem) {
        cardLayout.show(cardPanel, "Khách hàng");
        for (JButton btn : menuButtons) {
            if (btn.getText().contains("Khách hàng")) { setActiveButton(btn); break; }
        }
        if (moFormThem) {
            for (Component c : cardPanel.getComponents()) {
                if (c instanceof ManHinhKhachHang) { ((ManHinhKhachHang) c).moFormThemMoi(); break; }
            }
        }
    }

    public DefaultTableModel getModelKhachHang() {
        for (Component c : cardPanel.getComponents()) {
            if (c instanceof ManHinhKhachHang) return ((ManHinhKhachHang) c).getModel();
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ConnectDB.getInstance().connect();
            new MainDashboard().setVisible(true);
        });
    }
}
