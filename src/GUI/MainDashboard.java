package GUI;

import Components.MenuIcon;
import ConnectDB.ConnectDB;

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
        
        // ĐÃ SỬA: CHỈ CẦN 1 DÒNG NÀY ĐỂ GỌI FORM XỊN XÒ CỦA BẠN
        btnLogout.addActionListener(e -> hienThiThongBaoDangXuat());
        
        sidebar.add(btnLogout);
        
        return sidebar;
    }

    private void hienThiThongBaoDangXuat() {
        // Tạo một Dialog tùy chỉnh, làm mờ màn hình phía sau
        JDialog dialog = new JDialog(this, "Xác nhận", true);
        dialog.setSize(420, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true); // Câu lệnh này giúp tắt cái viền "lỏ" của Windows

        // Panel chính bọc ngoài cùng (Tạo viền màu xanh đậm đồng bộ với Sidebar)
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.decode("#152A4B"), 2));
        mainPanel.setBackground(Color.WHITE);

        // Header (Thanh tiêu đề màu xanh đậm)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.decode("#152A4B"));
        header.setPreferredSize(new Dimension(0, 40));
        JLabel lblTitle = new JLabel("   XÁC NHẬN ĐĂNG XUẤT");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Nút X để tắt nhanh trên góc
        JButton btnClose = new JButton("✕ ");
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dialog.dispose());
        
        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnClose, BorderLayout.EAST);
        mainPanel.add(header, BorderLayout.NORTH);

        // Body (Chứa Icon cảnh báo và Lời nhắn)
        JPanel body = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 35));
        body.setBackground(Color.WHITE);
        
        // Tận dụng icon WARNING trong class MenuIcon của bạn
        JLabel lblIcon = new JLabel(new MenuIcon("WARNING")); 
        lblIcon.setForeground(Color.decode("#FF4D4D")); // Tô màu đỏ cho icon cảnh báo
        
        JLabel lblMessage = new JLabel("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?");
        lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblMessage.setForeground(Color.decode("#333333"));
        
        body.add(lblIcon);
        body.add(lblMessage);
        mainPanel.add(body, BorderLayout.CENTER);

        // Footer (Chứa 2 nút Hủy và Đăng xuất)
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        footer.setBackground(Color.decode("#F4F6F8")); // Màu xám nhạt đồng bộ với Header search
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#DFE3E8")));

        JButton btnHuy = new JButton("Hủy");
        btnHuy.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnHuy.setBackground(Color.WHITE);
        btnHuy.setForeground(Color.decode("#637381"));
        btnHuy.setFocusPainted(false);
        btnHuy.setPreferredSize(new Dimension(90, 35));
        btnHuy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHuy.addActionListener(e -> dialog.dispose()); // Tắt hộp thoại

        JButton btnXacNhan = new JButton("Đăng xuất");
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnXacNhan.setBackground(Color.decode("#FF4D4D")); // Nút đỏ báo hiệu hành động nguy hiểm
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setFocusPainted(false);
        btnXacNhan.setPreferredSize(new Dimension(110, 35));
        btnXacNhan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXacNhan.addActionListener(e -> {
            dialog.dispose(); // Đóng thông báo
            this.dispose();   // Đóng Dashboard
            new ManHinhDangNhap().setVisible(true); // Về trang Login
        });

        footer.add(btnHuy);
        footer.add(btnXacNhan);
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
 // Sửa hàm cũ: Thêm tham số boolean vao
    public void chuyenSangTabKhachHang(boolean moFormThem) {
        cardLayout.show(cardPanel, "Khách hàng"); 

        // Bôi xanh menu bên trái
        for (JButton btn : menuButtons) {
            if (btn.getText().contains("Khách hàng")) {
                setActiveButton(btn); 
                break;
            }
        }

        // Kích hoạt form thêm mới
        if (moFormThem) {
            for (Component comp : cardPanel.getComponents()) {
                // Phải kiểm tra đúng class ManHinhKhachHang
                if (comp instanceof ManHinhKhachHang) {
                    ((ManHinhKhachHang) comp).moFormThemMoi();
                    break;
                }
            }
        }
    }
 // Thêm hàm này vào MainDashboard
    public DefaultTableModel getModelKhachHang() {
        for (Component comp : cardPanel.getComponents()) {
            if (comp instanceof ManHinhKhachHang) {
                return ((ManHinhKhachHang) comp).getModel();
            }
        }
        return null;
    }
    // --- HÀM CHẠY CHƯƠNG TRÌNH ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Đảm bảo bạn đã có class ConnectDB và hàm connect()
            ConnectDB.getInstance().connect();
            new ManHinhDangNhap().setVisible(true);
        });
    }
}