package GUI;

import Utils.*;
import BUS.BUS_TaiKhoan;
import BUS.BUS_CaLamViec;
import Entity.TaiKhoan;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

public class ManHinhDangNhap extends JFrame {

    private final Color COLOR_BG_DARK      = Color.decode("#122A4A");
    private final Color COLOR_BG_LIGHT     = Color.decode("#1A3C64");
    private final Color COLOR_HEADER_DARK  = Color.decode("#103358");
    private final Color COLOR_HEADER_LIGHT = Color.decode("#1870A9");
    private final Color COLOR_BTN_LOGIN    = Color.decode("#1A4B7C");
    private final Color COLOR_LINK         = Color.decode("#1976D2");
    private final Color COLOR_TEXT_GRAY    = Color.decode("#555555");
    private final Color COLOR_BORDER       = Color.decode("#D4D4D4");

    private AutoSuggestJTextField txtTaiKhoan;
    private JPasswordField        txtMatKhau;
    private JButton               btnEye;
    private JCheckBox             cbGhiNho;

    private Preferences  prefs          = Preferences.userRoot().node(this.getClass().getName());
    private List<String> savedUsersList;

    public ManHinhDangNhap() {
        setTitle("MYCARE PHARMACY - Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setResizable(true);

        String savedStr = prefs.get("saved_users", "");
        savedUsersList = savedStr.isEmpty()
                ? new ArrayList<>()
                : new ArrayList<>(Arrays.asList(savedStr.split(",")));

        JPanel pnlBackground = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setPaint(new GradientPaint(0, 0, COLOR_BG_DARK, getWidth(), getHeight(), COLOR_BG_LIGHT));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(new Color(255, 255, 255, 8));
                g2d.fillOval(-100, 50, 400, 400);
                g2d.fillOval(150, -150, 300, 300);
                g2d.fillOval(getWidth() - 350, 100, 500, 500);
                g2d.fillOval(getWidth() - 600, getHeight() - 250, 450, 450);
                g2d.fillOval(50, getHeight() - 200, 350, 350);
                g2d.dispose();
            }
        };
        setContentPane(pnlBackground);

        JPanel pnlWrapper = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.fillRoundRect(15, 15, 500, 560, 20, 20);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(10, 10, 500, 560, 20, 20);
                g2d.setPaint(new GradientPaint(10, 10, COLOR_HEADER_DARK, 10, 550, COLOR_HEADER_LIGHT));
                g2d.fillRoundRect(10, 10, 500, 210, 20, 20);
                g2d.fillRect(10, 110, 500, 100);
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.drawRoundRect(10, 10, 500, 560, 20, 20);
                g2d.dispose();
            }
        };
        pnlWrapper.setPreferredSize(new Dimension(530, 600));
        pnlWrapper.setOpaque(false);

        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.gridx = 0; gbcForm.gridy = 0;
        pnlBackground.add(pnlWrapper, gbcForm);

        JPanel pnlContent = new JPanel(null);
        pnlContent.setBounds(10, 10, 500, 560);
        pnlContent.setOpaque(false);
        pnlWrapper.add(pnlContent);

        JPanel pnlLogo = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2d.setColor(COLOR_HEADER_LIGHT);
                g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.translate(getWidth() / 2, getHeight() / 2);
                g2d.rotate(Math.toRadians(-45));
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

        JLabel lblHeaderCard = new JLabel("ĐĂNG NHẬP VÀO HỆ THỐNG", SwingConstants.CENTER);
        lblHeaderCard.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeaderCard.setForeground(Color.decode("#1F2937"));
        lblHeaderCard.setBounds(0, 225, 500, 30);
        pnlContent.add(lblHeaderCard);

        JLabel lblUser = new JLabel("Tên đăng nhập");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUser.setForeground(COLOR_TEXT_GRAY);
        lblUser.setBounds(40, 270, 300, 20);
        pnlContent.add(lblUser);

        JPanel pnlInputUser = createInputBorder();
        pnlInputUser.setBounds(40, 293, 420, 48);
        JLabel iconUser = new JLabel(new MenuIcon("USER"));
        iconUser.setBounds(15, 13, 22, 22);
        pnlInputUser.add(iconUser);

        txtTaiKhoan = new AutoSuggestJTextField("Nhập tên đăng nhập", savedUsersList);
        txtTaiKhoan.setBounds(50, 5, 360, 38);
        txtTaiKhoan.setBorder(new EmptyBorder(0, 10, 0, 0));
        txtTaiKhoan.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        txtTaiKhoan.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (txtTaiKhoan.getText().isEmpty() && !savedUsersList.isEmpty()) {
                    JPopupMenu popup = new JPopupMenu();
                    popup.setFocusable(false);
                    popup.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
                    popup.setBackground(Color.WHITE);

                    JLabel lblMenuTitle = new JLabel("   Tài khoản đã lưu:");
                    lblMenuTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    lblMenuTitle.setForeground(Color.decode("#9CA3AF"));
                    lblMenuTitle.setBorder(new EmptyBorder(5, 0, 5, 0));
                    popup.add(lblMenuTitle);
                    popup.addSeparator();

                    for (String user : savedUsersList) {
                        JMenuItem item = new JMenuItem("   " + user);
                        item.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        item.setForeground(COLOR_HEADER_DARK);
                        item.setBackground(Color.WHITE);
                        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        item.addActionListener(ae -> {
                            txtTaiKhoan.setText(user);
                            txtMatKhau.setText("");
                            txtMatKhau.requestFocus();
                        });
                        popup.add(item);
                    }

                    popup.addSeparator();
                    JMenuItem clearItem = new JMenuItem("   [X] Xóa danh sách gợi ý");
                    clearItem.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                    clearItem.setForeground(Color.RED);
                    clearItem.setBackground(Color.WHITE);
                    clearItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    clearItem.addActionListener(ae -> {
                        savedUsersList.clear();
                        prefs.remove("saved_users");
                        prefs.remove("saved_username");
                        txtTaiKhoan.setText("");
                        cbGhiNho.setSelected(false);
                    });
                    popup.add(clearItem);

                    popup.setPopupSize(txtTaiKhoan.getWidth() + 10, popup.getPreferredSize().height);
                    popup.show(txtTaiKhoan, 0, txtTaiKhoan.getHeight() + 2);
                    
                    txtTaiKhoan.requestFocusInWindow(); 
                }
            }
        });
        pnlInputUser.add(txtTaiKhoan);
        pnlContent.add(pnlInputUser);

        JLabel lblPass = new JLabel("Mật khẩu");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPass.setForeground(COLOR_TEXT_GRAY);
        lblPass.setBounds(40, 353, 300, 20);
        pnlContent.add(lblPass);

        JPanel pnlInputPass = createInputBorder();
        pnlInputPass.setBounds(40, 376, 420, 48);

        JLabel iconLock = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_TEXT_GRAY);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(4, 10, 14, 10, 3, 3);
                g2.drawArc(6, 4, 10, 12, 0, 180);
                g2.dispose();
            }
        };
        iconLock.setBounds(15, 10, 22, 22);
        pnlInputPass.add(iconLock);

        txtMatKhau = new WatermarkJPasswordField("Nhập mật khẩu");
        txtMatKhau.setBounds(50, 5, 330, 38);
        txtMatKhau.setBorder(new EmptyBorder(0, 10, 0, 0));
        txtMatKhau.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        pnlInputPass.add(txtMatKhau);

        btnEye = new JButton(new MenuIcon("EYE_HIDE"));
        btnEye.setBounds(385, 11, 25, 25);
        btnEye.setBorder(null); btnEye.setContentAreaFilled(false);
        btnEye.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pnlInputPass.add(btnEye);
        pnlContent.add(pnlInputPass);

        cbGhiNho = new JCheckBox("Ghi nhớ tên đăng nhập");
        cbGhiNho.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbGhiNho.setForeground(Color.decode("#374151"));
        cbGhiNho.setBackground(Color.WHITE);
        cbGhiNho.setBounds(36, 433, 180, 20);
        pnlContent.add(cbGhiNho);

        JLabel lblForgot = new JLabel("Quên mật khẩu?", SwingConstants.RIGHT);
        lblForgot.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblForgot.setForeground(COLOR_LINK);
        lblForgot.setBounds(310, 433, 150, 20);
        lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblForgot.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                ManHinhQuenMatKhauOTP otpDialog = new ManHinhQuenMatKhauOTP(ManHinhDangNhap.this, (email, newPass) -> {
                    new BUS_TaiKhoan().capNhatMatKhauTheoEmail(email, newPass);
                    JOptionPane.showMessageDialog(ManHinhDangNhap.this, "Đổi mật khẩu thành công! Hãy đăng nhập lại.");
                });
                otpDialog.setVisible(true);
            }
        });
        pnlContent.add(lblForgot);

        JButton btnLogin = new JButton("Đăng nhập") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? COLOR_BTN_LOGIN : Color.GRAY); 
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g2);
            }
        };
        btnLogin.setBounds(40, 475, 420, 50);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnLogin.setContentAreaFilled(false); btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pnlContent.add(btnLogin);

        JLabel lblFooter = new JLabel("© 2026 MYCARE Pharmacy Management System", SwingConstants.CENTER);
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooter.setForeground(new Color(255, 255, 255, 150));
        GridBagConstraints gbcFooter = new GridBagConstraints();
        gbcFooter.gridx = 0; gbcFooter.gridy = 1;
        gbcFooter.insets = new Insets(20, 0, 0, 0);
        pnlBackground.add(lblFooter, gbcFooter);

        String savedUser = prefs.get("saved_username", "");
        if (!savedUser.isEmpty()) {
            txtTaiKhoan.setText(savedUser);
            cbGhiNho.setSelected(true);
            SwingUtilities.invokeLater(() -> txtMatKhau.requestFocus());
        }

        btnEye.addActionListener(e -> {
            boolean visible = txtMatKhau.getEchoChar() == 0;
            txtMatKhau.setEchoChar(visible ? '•' : 0);
            btnEye.setIcon(visible ? new MenuIcon("EYE_HIDE") : new MenuIcon("EYE"));
        });

        btnLogin.addActionListener(e -> {
            String u = txtTaiKhoan.getText().trim();
            String p = new String(txtMatKhau.getPassword()).trim();

            if (u.isEmpty() || p.isEmpty()) {
                showCustomErrorDialog("<html>Vui lòng nhập đầy đủ tên đăng nhập<br/>và mật khẩu!</html>");
                return;
            }

            btnLogin.setEnabled(false);
            btnLogin.setText("Đang xác thực...");
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            SwingWorker<TaiKhoan, Void> worker = new SwingWorker<TaiKhoan, Void>() {
                @Override
                protected TaiKhoan doInBackground() throws Exception {
                    BUS_TaiKhoan bus = new BUS_TaiKhoan();
                    if (bus.authenticate(u, p)) {
                        return bus.getTaiKhoanDayDu(u);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Đăng nhập");
                    setCursor(Cursor.getDefaultCursor());

                    try {
                        TaiKhoan tk = get();
                        if (tk != null) {
                            if (cbGhiNho.isSelected()) {
                                prefs.put("saved_username", u);
                                if (!savedUsersList.contains(u)) {
                                    savedUsersList.add(u);
                                    prefs.put("saved_users", String.join(",", savedUsersList));
                                }
                            } else {
                                prefs.remove("saved_username");
                                if (savedUsersList.contains(u)) {
                                    savedUsersList.remove(u);
                                    prefs.put("saved_users", String.join(",", savedUsersList));
                                }
                            }

                            UserSession.getInstance().setTaiKhoan(tk);

                            if (!UserSession.getInstance().isAdmin()) {
                                DialogMoCa dlg = new DialogMoCa(ManHinhDangNhap.this);
                                dlg.setVisible(true);
                                if (!dlg.isConfirmed()) {
                                    UserSession.getInstance().logout();
                                    return;
                                }
                                UserSession.getInstance().setLoaiCa(dlg.getSelectedCa());
                                UserSession.getInstance().setTienDauCa(dlg.getTongTienDauCa());

                                Entity.CaLamViec ca = new Entity.CaLamViec();
                                ca.setId("Ca-" + System.currentTimeMillis());
                                ca.setNhanVienId(tk.getNhanVienId());
                                ca.setThoiGianBatDau(java.time.LocalDateTime.now());
                                ca.setTienDauCa(dlg.getTongTienDauCa());
                                ca.setTienHeThongGhiNhan(0); 
                                ca.setTienKetCa(0);
                                
                                // GỌI BUS THAY VÌ DAO
                                new BUS_CaLamViec().themCa(ca); 
                                UserSession.getInstance().setCaHienTai(ca);
                            }

                            new MainDashboard().setVisible(true);
                            dispose();

                        } else {
                            showCustomErrorDialog("<html>Tài khoản hoặc mật khẩu không chính xác.<br/>Vui lòng kiểm tra lại!</html>");
                            txtMatKhau.setText("");
                            txtMatKhau.requestFocus();
                        }
                    } catch (Exception ex) {
                        showCustomErrorDialog("<html>Lỗi kết nối cơ sở dữ liệu.<br/>Vui lòng thử lại sau!</html>");
                        ex.printStackTrace();
                    }
                }
            };
            worker.execute();
        });

        getRootPane().setDefaultButton(btnLogin);
    }

    private void showCustomErrorDialog(String message) {
        JDialog dialog = new JDialog(this, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel pnl = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(COLOR_HEADER_DARK);
                g2.fillRoundRect(0, 0, getWidth(), 45, 12, 12);
                g2.fillRect(0, 20, getWidth(), 25);
                g2.setColor(COLOR_HEADER_DARK);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                int iconX = 25, iconY = 75;
                g2.setColor(Color.decode("#FEE2E2"));
                g2.fillOval(iconX, iconY, 45, 45);
                g2.setColor(Color.decode("#EF4444"));
                g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(iconX, iconY, 45, 45);
                g2.drawLine(iconX + 15, iconY + 15, iconX + 30, iconY + 30);
                g2.drawLine(iconX + 30, iconY + 15, iconX + 15, iconY + 30);
                g2.dispose();
            }
        };
        pnl.setPreferredSize(new Dimension(450, 190));

        JLabel title = new JLabel("ĐĂNG NHẬP THẤT BẠI", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setBounds(0, 0, 450, 45);

        JLabel msg = new JLabel(message);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        msg.setForeground(Color.decode("#374151"));
        msg.setBounds(90, 60, 340, 55);

        JButton btnOk = new JButton("Đóng") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_BTN_LOGIN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btnOk.setBounds(330, 135, 100, 38);
        btnOk.setForeground(Color.WHITE);
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnOk.setContentAreaFilled(false); btnOk.setBorderPainted(false);
        btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOk.addActionListener(e -> dialog.dispose());

        pnl.add(title); pnl.add(msg); pnl.add(btnOk);
        dialog.add(pnl); dialog.pack(); dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createInputBorder() {
        JPanel p = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(COLOR_BORDER); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    static class AutoSuggestJTextField extends JTextField {
        private String       watermark;
        private List<String> dictionary;
        private String       suggestion = "";

        public AutoSuggestJTextField(String watermark, List<String> dictionary) {
            this.watermark  = watermark;
            this.dictionary = dictionary;
            setOpaque(false);

            getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e)  { updateSuggestion(); }
                public void removeUpdate(DocumentEvent e)  { updateSuggestion(); }
                public void changedUpdate(DocumentEvent e) { updateSuggestion(); }
            });

            addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if ((e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_TAB)
                            && !suggestion.isEmpty()) {
                        setText(getText() + suggestion);
                        e.consume();
                    }
                }
            });
        }

        private void updateSuggestion() {
            String text = getText();
            suggestion = "";
            if (!text.isEmpty()) {
                for (String word : dictionary) {
                    if (word.toLowerCase().startsWith(text.toLowerCase())) {
                        suggestion = word.substring(text.length());
                        break;
                    }
                }
            }
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            int textY  = (getHeight() - g2d.getFontMetrics().getHeight()) / 2 + g2d.getFontMetrics().getAscent();
            int startX = getInsets().left;

            if (getText().isEmpty() && !hasFocus()) {
                g2d.setColor(Color.decode("#9CA3AF"));
                g2d.drawString(watermark, startX, textY);
            } else if (!suggestion.isEmpty()) {
                g2d.setColor(Color.decode("#6B7280"));
                int textWidth = g2d.getFontMetrics(getFont()).stringWidth(getText());
                g2d.drawString(suggestion, startX + textWidth, textY);
            }
            g2d.dispose();
        }
    }

    static class WatermarkJPasswordField extends JPasswordField {
        private String watermark;
        public WatermarkJPasswordField(String watermark) { this.watermark = watermark; setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!hasFocus() && getPassword().length == 0) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.decode("#9CA3AF"));
                g2d.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                g2d.drawString(watermark, getInsets().left + 15,
                        (getHeight() - g2d.getFontMetrics().getHeight()) / 2 + g2d.getFontMetrics().getAscent());
                g2d.dispose();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManHinhDangNhap().setVisible(true));
    }
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> new ManHinhDangNhap().setVisible(true));
//    }
}