package GUI;

import Components.MenuIcon;
import Components.UIHelper;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class ManHinhDangKy extends JFrame {

    private final Color COLOR_PRIMARY_BLUE = Color.decode("#1976D2"); 
    private final Color COLOR_PRIMARY_LIGHT_BLUE = Color.decode("#64B5F6"); 
    private final Color COLOR_BODY_BG = Color.decode("#FFFFFF"); 
    private final Color COLOR_INPUT_FIELD_BG = Color.decode("#FFFFFF"); 
    private final Color COLOR_INPUT_FIELD_BORDER = Color.decode("#E0E0E0"); 
    private final Color COLOR_TEXT_PRIMARY = Color.decode("#212121"); 

    private final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_HEADER_SUB = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_FORM_FIELD = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_LOGIN_BUTTON = new Font("Segoe UI", Font.BOLD, 16);

    private JTextField txtHoTen, txtTaiKhoan;
    private JPasswordField txtMatKhau, txtNhapLaiMatKhau;

    public ManHinhDangKy() {
        setTitle("Đăng ký Tài khoản mới");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 600); 
        setLocationRelativeTo(null); 
        setResizable(false); 

        JPanel pnlMain = new JPanel(new GridBagLayout());
        pnlMain.setBackground(COLOR_BODY_BG);
        add(pnlMain);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0; g.gridx = 0; g.gridy = 0;

        JPanel pnlHeader = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setPaint(new GradientPaint(0, 0, COLOR_PRIMARY_BLUE, 0, getHeight(), COLOR_PRIMARY_LIGHT_BLUE));
                g2d.fillRect(0, 0, getWidth(), getHeight()); g2d.dispose();
            }
        };
        pnlHeader.setPreferredSize(new Dimension(0, 150));
        pnlHeader.setLayout(new GridBagLayout());
        GridBagConstraints hg = new GridBagConstraints();
        hg.fill = GridBagConstraints.HORIZONTAL; hg.anchor = GridBagConstraints.CENTER; hg.gridx = 0; hg.gridy = 0; hg.weightx = 1.0;

        JLabel lblSystemName = new JLabel("MYCARE", SwingConstants.CENTER);
        lblSystemName.setFont(new Font("Segoe UI", Font.BOLD, 20)); lblSystemName.setForeground(Color.WHITE);
        pnlHeader.add(lblSystemName, hg); hg.gridy++;
        
        JLabel lblHeaderSub = new JLabel("Đăng ký tài khoản hệ thống", SwingConstants.CENTER);
        lblHeaderSub.setFont(FONT_HEADER_SUB); lblHeaderSub.setForeground(Color.WHITE);
        pnlHeader.add(lblHeaderSub, hg);

        pnlMain.add(pnlHeader, g);
        g.gridy++; g.insets = new Insets(-15, 20, 0, 20); 

        JPanel pnlBody = new JPanel(new GridBagLayout());
        pnlBody.setBackground(COLOR_BODY_BG); pnlBody.setBorder(new EmptyBorder(20, 0, 20, 0));
        UIHelper.setRoundedCorners(pnlBody, 15); 

        GridBagConstraints bg = new GridBagConstraints();
        bg.fill = GridBagConstraints.HORIZONTAL; bg.weightx = 1.0; bg.gridx = 0; bg.gridy = 0; bg.insets = new Insets(0, 0, 15, 0);

        JLabel lblBodyTitle = new JLabel("TẠO TÀI KHOẢN MỚI", SwingConstants.CENTER);
        lblBodyTitle.setFont(FONT_SUBTITLE); lblBodyTitle.setForeground(COLOR_TEXT_PRIMARY);
        pnlBody.add(lblBodyTitle, bg); bg.gridy++;

        pnlBody.add(createInputPanel(new MenuIcon("USER"), txtHoTen = new ManHinhDangNhap.WatermarkJTextField("Họ và tên")), bg); bg.gridy++;
        pnlBody.add(createInputPanel(new MenuIcon("USER"), txtTaiKhoan = new ManHinhDangNhap.WatermarkJTextField("Tên đăng nhập")), bg); bg.gridy++;
        pnlBody.add(createInputPanel(new MenuIcon("LOCK"), txtMatKhau = new ManHinhDangNhap.WatermarkJPasswordField("Mật khẩu")), bg); bg.gridy++;
        pnlBody.add(createInputPanel(new MenuIcon("LOCK"), txtNhapLaiMatKhau = new ManHinhDangNhap.WatermarkJPasswordField("Nhập lại mật khẩu")), bg); bg.gridy++;

        JButton btnDangKy = new JButton("Xác nhận Đăng ký");
        btnDangKy.setFont(FONT_LOGIN_BUTTON); btnDangKy.setForeground(Color.WHITE); btnDangKy.setBackground(Color.decode("#E11D48"));
        btnDangKy.setPreferredSize(new Dimension(0, 45)); btnDangKy.setOpaque(true); btnDangKy.setBorderPainted(false);
        UIHelper.setRoundedCorners(btnDangKy, 10); 
        pnlBody.add(btnDangKy, bg); bg.gridy++;

        JButton btnHuy = new JButton("Trở về Đăng nhập");
        btnHuy.setFont(FONT_LOGIN_BUTTON); btnHuy.setForeground(Color.WHITE); btnHuy.setBackground(Color.decode("#64748B"));
        btnHuy.setPreferredSize(new Dimension(0, 45)); btnHuy.setOpaque(true); btnHuy.setBorderPainted(false);
        UIHelper.setRoundedCorners(btnHuy, 10); 
        pnlBody.add(btnHuy, bg); bg.gridy++;

        pnlMain.add(pnlBody, g);

        btnDangKy.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Đăng ký thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            new ManHinhDangNhap().setVisible(true); dispose();
        });
        
        btnHuy.addActionListener(e -> { new ManHinhDangNhap().setVisible(true); dispose(); });
    }

    private JPanel createInputPanel(MenuIcon icon, JTextField field) {
        JPanel inputPanel = new JPanel(new BorderLayout()); inputPanel.setBackground(COLOR_INPUT_FIELD_BG);
        inputPanel.setBorder(new CompoundBorder(new LineBorder(COLOR_INPUT_FIELD_BORDER, 1), new EmptyBorder(10, 15, 10, 15)));
        UIHelper.setRoundedCorners(inputPanel, 10); 
        JLabel iconLabel = new JLabel(icon); iconLabel.setBorder(new EmptyBorder(0, 0, 0, 10)); 
        inputPanel.add(iconLabel, BorderLayout.WEST);
        field.setBorder(null); field.setFont(FONT_FORM_FIELD); inputPanel.add(field, BorderLayout.CENTER);
        return inputPanel;
    }
}