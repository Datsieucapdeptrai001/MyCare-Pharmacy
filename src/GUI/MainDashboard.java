package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainDashboard extends JFrame {

    public MainDashboard() {
        setTitle("MYCARE PHARMACY - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 750); 
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(createTopHeader(), BorderLayout.NORTH);
        
        ManHinhBanHang formHoaDon = new ManHinhBanHang();
        rightPanel.add(formHoaDon, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.CENTER);
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));

        JTextField txtSearch = new JTextField("   🔍 Tìm kiếm...");
        txtSearch.setBackground(Color.decode("#F4F6F8"));
        txtSearch.setForeground(Color.decode("#637381"));
        txtSearch.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10)); 
        txtSearch.setPreferredSize(new Dimension(300, 40));
        
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        pnlSearch.setBackground(Color.WHITE);
        pnlSearch.add(txtSearch);
        header.add(pnlSearch, BorderLayout.WEST);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        pnlRight.setBackground(Color.WHITE);

        JLabel lblTime = new JLabel("19:09:04 — T5 09/04/2026");
        lblTime.setForeground(Color.decode("#637381"));
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton btnUser = new JButton("VK  Võ Anh Kiệt  ▼");
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
        sidebar.setPreferredSize(new Dimension(200, 0));

        // Logo panel
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10)); 
        logoPanel.setBackground(Color.decode("#152A4B")); 
        logoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70)); 
        JLabel lblLogo = new JLabel("<html><b style='color:white; font-size:13px;'>MYCARE</b><br><span style='color:#00BFFF; font-size:9px;'>PHARMACY</span></html>");
        logoPanel.add(lblLogo);
        sidebar.add(logoPanel);

        // User profile panel
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        userPanel.setBackground(Color.decode("#152A4B"));
        userPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        JLabel lblAvatar = new JLabel("VK", SwingConstants.CENTER);
        lblAvatar.setOpaque(true);
        lblAvatar.setBackground(Color.decode("#1362B1"));
        lblAvatar.setForeground(Color.WHITE);
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAvatar.setPreferredSize(new Dimension(35, 35));

        JLabel lblUserInfo = new JLabel("<html><b style='color:white; font-size:11px;'>Võ Anh Kiệt</b><br><span style='color:#FFD700; font-size:9px;'>Quản lý</span></html>");
        
        userPanel.add(lblAvatar);
        userPanel.add(lblUserInfo);
        sidebar.add(userPanel);

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        // Menu buttons
        String[] menuItems = {"Màn hình chính", 
                "Bán hàng & Đổi trả", 
                "Sản phẩm", 
                "Lô hàng", 
                "Khuyến mại", 
                "Thống kê", 
                "Nhân viên", 
                "Khách hàng", 
                "Hướng dẫn"};
        for (int i = 0; i < menuItems.length; i++) {
            sidebar.add(createMenuButton(menuItems[i], i == 1, false)); 
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createMenuButton("Đăng xuất", false, true));
        
        return sidebar;
    }


    private JButton createMenuButton(String text, boolean isActive, boolean isLogout) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); 
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT); 
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13)); 
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);

        if (isActive) {
            btn.setBackground(Color.decode("#1A73E8")); 
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.decode("#152A4B")); 
            btn.setForeground(isLogout ? Color.decode("#FF4D4D") : Color.decode("#E8F0FE"));
        }
        
        btn.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0)); // Left margin for alignment
        return btn;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {}
            new MainDashboard().setVisible(true);
        });
    }
}