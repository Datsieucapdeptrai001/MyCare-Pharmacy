package GUI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainDashboard extends JFrame {

    // Khai báo CardLayout và Panel chứa các màn hình
    private CardLayout cardLayout;
    private JPanel cardPanel;
    
    // Danh sách các nút menu để xử lý đổi màu khi click
    private List<JButton> menuButtons;

    public MainDashboard() {
        setTitle("MYCARE PHARMACY - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 750); 
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        menuButtons = new ArrayList<>();
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // ==================== 1. GẮN MÀN HÌNH THẬT VÀO ĐÂY ====================
        
        // Màn hình chính (Vẫn để giả nếu bạn chưa code xong)
        cardPanel.add(createDummyPanel("Chào mừng đến với MyCare Pharmacy"), "Màn hình chính");
        
        // Gắn ManHinhBanHang thật vào key "Bán hàng & Đổi trả"
        ManHinhBanHang mhBanHang = new ManHinhBanHang(); 
        cardPanel.add(mhBanHang, "Bán hàng & Đổi trả");

        // Các màn hình khác (Để tạm panel giả)
        cardPanel.add(createDummyPanel("Quản lý Sản phẩm"), "Sản phẩm");
        cardPanel.add(createDummyPanel("Quản lý Lô hàng"), "Lô hàng");
        cardPanel.add(createDummyPanel("Chương trình Khuyến mại"), "Khuyến mại");
        cardPanel.add(createDummyPanel("Thống kê doanh thu"), "Thống kê");
        cardPanel.add(createDummyPanel("Quản lý Nhân viên"), "Nhân viên");
        cardPanel.add(createDummyPanel("Quản lý Khách hàng"), "Khách hàng");
        cardPanel.add(createDummyPanel("Hướng dẫn sử dụng"), "Hướng dẫn");
        
        // ======================================================================

        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(createTopHeader(), BorderLayout.NORTH);
        rightPanel.add(cardPanel, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.CENTER);
    }

    // Hàm tạo nhanh một Panel giả để Pột test chuyển tab
    private JPanel createDummyPanel(String text) {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(Color.decode("#F4F6F8"));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lbl.setForeground(Color.GRAY);
        pnl.add(lbl);
        return pnl;
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

        JLabel lblTime = new JLabel("20:27:44 — T5 09/04/2026");
        lblTime.setForeground(Color.decode("#637381"));
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton btnUser = new JButton("MK  Mai Trung Kiên  ▼");
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
        sidebar.setPreferredSize(new Dimension(220, 0)); // Tăng xíu cho giống ảnh

        // Logo panel
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10)); 
        logoPanel.setBackground(Color.decode("#152A4B")); 
        logoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70)); 
        JLabel lblLogo = new JLabel("<html><b style='color:white; font-size:14px;'>MYCARE</b><br><span style='color:#00BFFF; font-size:10px;'>PHARMACY</span></html>");
        logoPanel.add(lblLogo);
        sidebar.add(logoPanel);

        // User profile panel (Đổi thành Mai Trung Kiên - Dược sĩ)
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        userPanel.setBackground(Color.decode("#152A4B"));
        userPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        
        JLabel lblAvatar = new JLabel("MK", SwingConstants.CENTER);
        lblAvatar.setOpaque(true);
        lblAvatar.setBackground(Color.decode("#1362B1"));
        lblAvatar.setForeground(Color.WHITE);
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblAvatar.setPreferredSize(new Dimension(35, 35));

        JLabel lblUserInfo = new JLabel("<html><b style='color:white; font-size:12px;'>Mai Trung Kiên</b><br><span style='color:#00BFFF; font-size:10px;'>Dược sĩ</span></html>");
        
        userPanel.add(lblAvatar);
        userPanel.add(lblUserInfo);
        sidebar.add(userPanel);

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        // Menu buttons
        String[] menuItems = {
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
        
        for (String item : menuItems) {
            JButton btn = createMenuButton(item, false);
            menuButtons.add(btn); // Lưu vào list để quản lý màu sắc
            sidebar.add(btn);
        }
        
        // Mặc định chọn màn hình chính
        setActiveButton(menuButtons.get(0));

        sidebar.add(Box.createVerticalGlue());
        
        // Nút Đăng xuất riêng
        JButton btnLogout = createMenuButton("Đăng xuất", true);
        btnLogout.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Thực hiện chức năng đăng xuất...");
            // System.exit(0); hoặc gọi JFrame đăng nhập
        });
        sidebar.add(btnLogout);
        
        return sidebar;
    }

    private JButton createMenuButton(String text, boolean isLogout) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); 
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT); 
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13)); 
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setBackground(Color.decode("#152A4B")); 
        btn.setForeground(isLogout ? Color.decode("#FF4D4D") : Color.decode("#E8F0FE"));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

        if (!isLogout) {
            // Thêm sự kiện click cho các nút menu (trừ nút Đăng xuất)
            btn.addActionListener(e -> {
                setActiveButton(btn); // Đổi màu nút
                cardLayout.show(cardPanel, text); // Chuyển màn hình tương ứng
            });
        }

        return btn;
    }

    // Hàm xử lý đổi màu nút khi click
    private void setActiveButton(JButton activeBtn) {
        for (JButton btn : menuButtons) {
            if (btn == activeBtn) {
                btn.setBackground(Color.decode("#1A73E8")); // Màu xanh sáng (Active)
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(Color.decode("#152A4B")); // Màu xanh đậm (Inactive)
                btn.setForeground(Color.decode("#E8F0FE"));
            }
        }
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