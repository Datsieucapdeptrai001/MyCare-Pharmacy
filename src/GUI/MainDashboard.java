package GUI;

import Components.MenuIcon; 
import javax.swing.*;
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

    // Mảng tên Menu sạch sẽ (Key để chuyển màn hình)
    private final String[] menuItems = {
        "Màn hình chính", 
        "Bán hàng & Đổi trả", 
        "Sản phẩm", 
        "Lô hàng", 
        "Khuyến mại", 
        "Thống kê", 
        "Nhân viên", 
        "Khách hàng", 
        "Hướng dẫn"
    };

    public MainDashboard() {
        setTitle("MYCARE PHARMACY - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 750); 
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        menuButtons = new ArrayList<>();
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // KHU VỰC ĐÃ DỌN DẸP SẠCH SẼ - 1 MÀN HÌNH CHỈ CÓ 1 KEY
        
        // 1. Màn hình chính
        ManHinhChinh mhChinh = new ManHinhChinh();
        cardPanel.add(mhChinh, "Màn hình chính");
        
        // 2. Bán hàng & Đổi trả
        ManHinhBanHang mhBanHang = new ManHinhBanHang(); 
        cardPanel.add(mhBanHang, "Bán hàng & Đổi trả");

        // 3. Sản phẩm
        ManHinhSanPham mhSanPham = new ManHinhSanPham();
        cardPanel.add(mhSanPham, "Sản phẩm");

        // 4. Lô hàng
        ManHinhLoHang mhLoHang = new ManHinhLoHang();
        cardPanel.add(mhLoHang, "Lô hàng");

        // 5. Khuyến mại
        ManHinhKhuyenMai mhKhuyenMai = new ManHinhKhuyenMai();
        cardPanel.add(mhKhuyenMai, "Khuyến mại");

        // 6. Thống kê 
        ManHinhThongKe mhThongKe = new ManHinhThongKe();
        cardPanel.add(mhThongKe, "Thống kê");

        // 7. Nhân viên (Chưa có -> Dùng Dummy)
        cardPanel.add(createDummyPanel("Quản lý Nhân viên"), "Nhân viên");
        
        // 8. Khách hàng (Chưa có -> Dùng Dummy)
        ManHinhKhachHang mhKhachHang = new ManHinhKhachHang();
        cardPanel.add(mhKhachHang, "Khách hàng"); // Thay DummyPanel bằng mhKhachHang

        // 9. Hướng dẫn
        ManHinhHuongDan mhHuongDan = new ManHinhHuongDan();
        cardPanel.add(mhHuongDan, "Hướng dẫn");

        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(createTopHeader(), BorderLayout.NORTH);
        rightPanel.add(cardPanel, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.CENTER);

        // Hiển thị Màn hình chính đầu tiên khi chạy code
        cardLayout.show(cardPanel, "Màn hình chính");
    }

    private JPanel createDummyPanel(String text) {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(Color.decode("#F4F6F8"));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lbl.setForeground(Color.GRAY);
        pnl.add(lbl);
        return pnl;
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));

        String placeholderText = "🔍 Tìm kiếm...";
        JTextField txtSearch = new JTextField(placeholderText);
        txtSearch.setBackground(Color.decode("#F4F6F8"));
        txtSearch.setForeground(Color.GRAY); 
        txtSearch.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15)); 
        txtSearch.setPreferredSize(new Dimension(350, 40));
        
        txtSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals(placeholderText)) {
                    txtSearch.setText(""); 
                    txtSearch.setForeground(Color.BLACK); 
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().trim().isEmpty()) {
                    txtSearch.setForeground(Color.GRAY); 
                    txtSearch.setText(placeholderText); 
                }
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

        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss — E dd/MM/yyyy");
            lblTime.setText(sdf.format(new Date()));
        });
        timer.start();

        // Tên Đạt, chức Dược sĩ
        JButton btnUser = new JButton("Đ  Đạt  ▼"); 
        btnUser.setBackground(Color.decode("#1A73E8")); 
        btnUser.setForeground(Color.WHITE);
        btnUser.setFocusPainted(false);
        btnUser.setBorderPainted(false);
        btnUser.setOpaque(true);
        btnUser.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel lblBell = new JLabel("🔔");
        lblBell.setForeground(Color.BLACK);
        lblBell.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        pnlRight.add(lblTime);
        pnlRight.add(btnUser);
        pnlRight.add(lblBell);

        header.add(pnlRight, BorderLayout.EAST);
        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.decode("#152A4B")); 
        sidebar.setPreferredSize(new Dimension(230, 0)); 

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10)); 
        logoPanel.setBackground(Color.decode("#152A4B")); 
        logoPanel.setMaximumSize(new Dimension(230, 70)); 
        JLabel lblLogo = new JLabel("<html><b style='color:white; font-size:16px;'>MYCARE</b><br><span style='color:#00BFFF; font-size:11px;'>PHARMACY</span></html>");
        logoPanel.add(lblLogo);
        sidebar.add(logoPanel);

        // User info
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        userPanel.setBackground(Color.decode("#152A4B"));
        userPanel.setMaximumSize(new Dimension(230, 70));
        
        JLabel lblAvatar = new JLabel("Đ", SwingConstants.CENTER);
        lblAvatar.setOpaque(true);
        lblAvatar.setBackground(Color.decode("#1362B1"));
        lblAvatar.setForeground(Color.WHITE);
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAvatar.setPreferredSize(new Dimension(40, 40)); 

        JLabel lblUserInfo = new JLabel("<html><b style='color:white; font-size:13px;'>Đạt</b><br><span style='color:#00BFFF; font-size:11px;'>Dược sĩ</span></html>");
        
        userPanel.add(lblAvatar);
        userPanel.add(lblUserInfo);
        sidebar.add(userPanel);

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        // GẮN NÚT VÀ GỌI CLASS MENU_ICON ĐỂ LẤY FULL ICON
        for (String item : menuItems) {
            JButton btn = createMenuButton(item, false);
            
            if (item.equals("Màn hình chính")) btn.setIcon(new MenuIcon("HOME"));
            else if (item.equals("Bán hàng & Đổi trả")) btn.setIcon(new MenuIcon("CART"));
            else if (item.equals("Sản phẩm")) btn.setIcon(new MenuIcon("PILL"));
            else if (item.equals("Lô hàng")) btn.setIcon(new MenuIcon("BOX"));
            else if (item.equals("Khuyến mại")) btn.setIcon(new MenuIcon("GIFT"));
            else if (item.equals("Thống kê")) btn.setIcon(new MenuIcon("CHART"));
            else if (item.equals("Nhân viên")) btn.setIcon(new MenuIcon("USER"));
            else if (item.equals("Khách hàng")) btn.setIcon(new MenuIcon("USERS"));
            else if (item.equals("Hướng dẫn")) btn.setIcon(new MenuIcon("HELP"));
            
            menuButtons.add(btn); 
            sidebar.add(btn);
        }
        
        setActiveButton(menuButtons.get(0));

        sidebar.add(Box.createVerticalGlue());
        
        JButton btnLogout = createMenuButton("Đăng xuất", true);
        btnLogout.setIcon(new MenuIcon("LOGOUT"));
        btnLogout.addActionListener(e -> JOptionPane.showMessageDialog(this, "Thực hiện chức năng đăng xuất..."));
        sidebar.add(btnLogout);
        
        return sidebar;
    }

    private JButton createMenuButton(String text, boolean isLogout) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(230, 50)); 
        btn.setPreferredSize(new Dimension(230, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT); 
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(true); 
        
        // Khoảng cách giữa Icon và Text
        btn.setIconTextGap(15);
        
        btn.setBackground(Color.decode("#152A4B")); 
        btn.setForeground(isLogout ? Color.decode("#FF4D4D") : Color.decode("#E8F0FE"));
        
        javax.swing.border.Border bottomLine = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#1E3F70"));
        javax.swing.border.Border padding = BorderFactory.createEmptyBorder(0, 20, 0, 0); 
        btn.setBorder(BorderFactory.createCompoundBorder(bottomLine, padding));

        if (!isLogout) {
            btn.addActionListener(e -> {
                setActiveButton(btn); 
                cardLayout.show(cardPanel, text); 
            });
        }
        return btn;
    }

    private void setActiveButton(JButton activeBtn) {
        for (JButton btn : menuButtons) {
            if (btn == activeBtn) {
                btn.setBackground(Color.decode("#1A73E8")); 
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(Color.decode("#152A4B")); 
                btn.setForeground(Color.decode("#E8F0FE"));
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainDashboard().setVisible(true);
        });
    }
}