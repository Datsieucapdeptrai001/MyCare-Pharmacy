package GUI;

import BUS.BUS_TaiKhoan;
import java.util.prefs.Preferences;
import Utils.MenuIcon;
import DAO.DAO_TaiKhoan;
import Entity.TaiKhoan;
import Utils.UserSession;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class ManHinhDangNhap extends JFrame {

    private final Color COLOR_BG_DARK = Color.decode("#122A4A");
    private final Color COLOR_BG_LIGHT = Color.decode("#1A3C64");
    private final Color COLOR_HEADER_DARK = Color.decode("#103358");
    private final Color COLOR_HEADER_LIGHT = Color.decode("#1870A9");
    private final Color COLOR_BTN_LOGIN = Color.decode("#1A4B7C");
    private final Color COLOR_LINK = Color.decode("#1976D2");
    private final Color COLOR_TEXT_GRAY = Color.decode("#555555");
    private final Color COLOR_BORDER = Color.decode("#D4D4D4");

    private JTextField txtTaiKhoan;
    private JPasswordField txtMatKhau;
    private JButton btnEye;

    public ManHinhDangNhap() {
        setTitle("MYCARE PHARMACY - Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 850); 
        setLocationRelativeTo(null);
        
        JPanel pnlBackground = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setPaint(new GradientPaint(0, 0, COLOR_BG_DARK, getWidth(), getHeight(), COLOR_BG_LIGHT));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(new Color(255, 255, 255, 8)); 
                g2d.fillOval(-100, 50, 400, 400);
                g2d.fillOval(getWidth() - 350, 100, 500, 500);
                g2d.fillOval(getWidth() - 600, getHeight() - 250, 450, 450);
                g2d.dispose();
            }
        };
        setContentPane(pnlBackground);

        // WRAPPER: Rộng 500px (Nới thêm ngang)
        JPanel pnlWrapper = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.fillRoundRect(15, 15, 500, 540, 20, 20); 
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(10, 10, 500, 540, 20, 20);
                g2d.setPaint(new GradientPaint(10, 10, COLOR_HEADER_DARK, 10, 580, COLOR_HEADER_LIGHT));
                g2d.fillRoundRect(10, 10, 500, 210, 20, 20); 
                g2d.fillRect(10, 110, 500, 100); 
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.drawRoundRect(10, 10, 500, 670, 20, 20);
                g2d.dispose();
            }
        };
        pnlWrapper.setPreferredSize(new Dimension(530, 560));
        pnlWrapper.setOpaque(false);
        
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.gridx = 0; gbcForm.gridy = 0;
        pnlBackground.add(pnlWrapper, gbcForm);

        JPanel pnlContent = new JPanel(null);
        pnlContent.setBounds(10, 10, 500, 540);
        pnlContent.setOpaque(false);
        pnlWrapper.add(pnlContent);

        // --- HEADER ---
        JPanel pnlLogo = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE); 
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22); 
                g2d.setColor(COLOR_HEADER_LIGHT);
                g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.translate(getWidth()/2, getHeight()/2); g2d.rotate(Math.toRadians(-45));
                g2d.drawRoundRect(-13, -6, 26, 12, 10, 10);
                g2d.drawLine(0, -6, 0, 6); 
                g2d.dispose();
            }
        };
        pnlLogo.setBounds(215, 20, 70, 70); 
        pnlLogo.setOpaque(false);
        pnlContent.add(pnlLogo);

        JLabel lblTitle = new JLabel("MYCARE", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBounds(0, 95, 500, 35);
        pnlContent.add(lblTitle);

        JLabel lblSub = new JLabel("PHARMACY", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSub.setForeground(Color.decode("#90CAF9"));
        lblSub.setBounds(0, 125, 500, 20);
        pnlContent.add(lblSub);

        JLabel lblDesc = new JLabel("Phần mềm quản lý thuốc", SwingConstants.CENTER);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblDesc.setForeground(Color.WHITE);
        lblDesc.setBounds(0, 160, 500, 20);
        pnlContent.add(lblDesc);

        // --- BODY ---
        JLabel lblHeaderCard = new JLabel("ĐĂNG NHẬP VÀO HỆ THỐNG", SwingConstants.CENTER);
        lblHeaderCard.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeaderCard.setForeground(Color.decode("#1F2937"));
        lblHeaderCard.setBounds(0, 220, 500, 30);
        pnlContent.add(lblHeaderCard);

        JLabel lblUser = new JLabel("Tên đăng nhập");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUser.setForeground(COLOR_TEXT_GRAY);
        lblUser.setBounds(40, 265, 300, 20);
        pnlContent.add(lblUser);

        JPanel pnlInputUser = createInputBorder();
        pnlInputUser.setBounds(40, 288, 420, 48);
        JLabel iconUser = new JLabel(new MenuIcon("USER"));
        iconUser.setBounds(15, 13, 22, 22);
        pnlInputUser.add(iconUser);
        txtTaiKhoan = new WatermarkJTextField("Nhập tên đăng nhập");
        txtTaiKhoan.setBounds(50, 5, 360, 38);
        txtTaiKhoan.setBorder(null); txtTaiKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        pnlInputUser.add(txtTaiKhoan);
        pnlContent.add(pnlInputUser);

        JLabel lblPass = new JLabel("Mật khẩu");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPass.setForeground(COLOR_TEXT_GRAY);
        lblPass.setBounds(40, 348, 300, 20);
        pnlContent.add(lblPass);

        JPanel pnlInputPass = createInputBorder();
        pnlInputPass.setBounds(40, 371, 420, 48);
        JLabel iconLock = new JLabel(new MenuIcon("LOCK"));
        iconLock.setBounds(15, 13, 22, 22);
        pnlInputPass.add(iconLock);
        txtMatKhau = new WatermarkJPasswordField("Nhập mật khẩu");
        txtMatKhau.setBounds(50, 5, 330, 38);
        txtMatKhau.setBorder(null); txtMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        pnlInputPass.add(txtMatKhau);
        btnEye = new JButton(new MenuIcon("EYE_HIDE"));
        btnEye.setBounds(385, 11, 25, 25);
        btnEye.setBorder(null); btnEye.setContentAreaFilled(false); btnEye.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pnlInputPass.add(btnEye);
        pnlContent.add(pnlInputPass);

        JCheckBox cbGhiNho = new JCheckBox("Ghi nhớ đăng nhập");
        cbGhiNho.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbGhiNho.setForeground(Color.decode("#374151"));
        cbGhiNho.setBackground(Color.WHITE);
        cbGhiNho.setBounds(36, 428, 160, 20); 
        pnlContent.add(cbGhiNho);

        JLabel lblForgot = new JLabel("Quên mật khẩu?", SwingConstants.RIGHT);
        lblForgot.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblForgot.setForeground(COLOR_LINK);
        lblForgot.setBounds(310, 428, 150, 20);
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pnlContent.add(lblForgot);

        JButton btnLogin = new JButton("Đăng nhập") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_BTN_LOGIN);
                g2.fillRoundRect(0,0,getWidth(),getHeight(), 10, 10); 
                super.paintComponent(g2);
            }
        };
        btnLogin.setBounds(40, 465, 420, 50);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnLogin.setContentAreaFilled(false); btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pnlContent.add(btnLogin);

        // FOOTER
        JLabel lblFooter = new JLabel("© 2024 MYCARE Pharmacy Management System", SwingConstants.CENTER);
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooter.setForeground(new Color(255, 255, 255, 150)); 
        GridBagConstraints gbcFooter = new GridBagConstraints();
        gbcFooter.gridx = 0; gbcFooter.gridy = 1; 
        gbcFooter.insets = new Insets(15, 0, 0, 0); 
        pnlBackground.add(lblFooter, gbcFooter);

        // LOGIC
        btnEye.addActionListener(e -> {
            boolean visible = txtMatKhau.getEchoChar() == 0;
            txtMatKhau.setEchoChar(visible ? '•' : 0);
            btnEye.setIcon(visible ? new MenuIcon("EYE_HIDE") : new MenuIcon("EYE"));
        });

        // Tự điền nếu đã ghi nhớ
        Preferences prefs = Preferences.userNodeForPackage(ManHinhDangNhap.class);
        String savedUser = prefs.get("saved_username", "");
        if (!savedUser.isEmpty()) {
            txtTaiKhoan.setText(savedUser);
            cbGhiNho.setSelected(true);
        }

        btnLogin.addActionListener(e -> {
            String u = txtTaiKhoan.getText().trim();
            String p = new String(txtMatKhau.getPassword()).trim();
            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập tên đăng nhập và mật khẩu!",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }
            BUS_TaiKhoan bus = new BUS_TaiKhoan();
            if (bus.authenticate(u, p)) {
                // Ghi nhớ đăng nhập
                if (cbGhiNho.isSelected()) prefs.put("saved_username", u);
                else                        prefs.remove("saved_username");

                // Lưu session đầy đủ
                DAO_TaiKhoan dao = new DAO_TaiKhoan();
                TaiKhoan tk = dao.getTaiKhoan(u);
                UserSession.getInstance().setTaiKhoan(tk);

                // STAFF → mở Dialog Mở Ca
                if (!UserSession.getInstance().isAdmin()) {
                    DialogMoCa dlg = new DialogMoCa(this);
                    dlg.setVisible(true);
                    if (!dlg.isConfirmed()) {
                        UserSession.getInstance().logout();
                        return;
                    }
                    // Lưu thông tin ca vào session
                    UserSession.getInstance().setLoaiCa(dlg.getSelectedCa());
                    UserSession.getInstance().setTienDauCa(dlg.getTongTienDauCa());

                    // Ghi ca vào DB
                    Entity.CaLamViec ca = new Entity.CaLamViec();
                    ca.setId("Ca-" + System.currentTimeMillis());
                    ca.setNhanVienId(tk.getNhanVienId());
                    ca.setThoiGianBatDau(java.time.LocalDateTime.now());
                    ca.setTienDauCa(dlg.getTongTienDauCa());
                    ca.setTienHeThongGhiNhan(0);
                    ca.setTienKetCa(0);
                    new DAO.DAO_CaLamViec().themCa(ca);
                    UserSession.getInstance().setCaHienTai(ca);
                }

                new MainDashboard().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Sai tài khoản hoặc mật khẩu!\nVui lòng kiểm tra lại.",
                        "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
            }
        });
        getRootPane().setDefaultButton(btnLogin); 
    }

    private JPanel createInputBorder() {
        JPanel p = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.setColor(COLOR_BORDER); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }


    static class WatermarkJTextField extends JTextField {
        private String watermark; public WatermarkJTextField(String watermark) { this.watermark = watermark; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!hasFocus() && getText().isEmpty()) {
                Graphics2D g2d = (Graphics2D) g.create(); g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.decode("#9CA3AF")); g2d.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                g2d.drawString(watermark, getInsets().left + 15, (getHeight() - g2d.getFontMetrics().getHeight()) / 2 + g2d.getFontMetrics().getAscent()); g2d.dispose();
            }
        }
    }
    static class WatermarkJPasswordField extends JPasswordField {
        private String watermark; public WatermarkJPasswordField(String watermark) { this.watermark = watermark; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!hasFocus() && getPassword().length == 0) {
                Graphics2D g2d = (Graphics2D) g.create(); g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.decode("#9CA3AF")); g2d.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                g2d.drawString(watermark, getInsets().left + 15, (getHeight() - g2d.getFontMetrics().getHeight()) / 2 + g2d.getFontMetrics().getAscent()); g2d.dispose();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainDashboard().setVisible(true));
    }
}