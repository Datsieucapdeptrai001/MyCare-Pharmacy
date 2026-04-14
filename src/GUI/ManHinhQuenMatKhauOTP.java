package GUI;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Properties;
import java.util.Random;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ManHinhQuenMatKhauOTP extends JDialog {

    public interface ResetPasswordListener {
        void onPasswordResetSuccess(String email, String newPassword);
    }

    // --- CẤU HÌNH SMTP ---
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String SMTP_USERNAME = "your_email@gmail.com";
    private static final String SMTP_APP_PASSWORD = "your_app_password";

    // --- BỘ MÀU SẮC ĐÃ TINH CHỈNH ---
    private static final Color BG_APP = new Color(15, 42, 74); // Xanh nền ngoài cùng
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY = new Color(30, 75, 138); // Xanh đậm chủ đạo
    private static final Color PRIMARY_SOFT = new Color(240, 247, 255); // Xanh nhạt cho Info box
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(203, 213, 225); // Viền xám nhạt
    private static final Color BTN_SECONDARY_BG = new Color(241, 245, 249); // Xám nhạt cho nút phụ

    private final ResetPasswordListener resetPasswordListener;

    private JTextField txtEmail;
    private JTextField txtOtp;
    private JPasswordField txtMatKhauMoi;
    private JPasswordField txtNhapLaiMatKhau;

    private JLabel lblStatus;
    private JLabel lblCountdown;

    private JButton btnGuiOtp;
    private JButton btnGuiLai;
    private JButton btnXacNhan;
    private JButton btnHuy;

    private Timer countdownTimer;
    private int secondsLeft = 0;
    private String currentOtp = null;
    private String currentEmail = "";

    public ManHinhQuenMatKhauOTP(Window owner, ResetPasswordListener listener) {
        super(owner, "Khôi phục mật khẩu", ModalityType.APPLICATION_MODAL);
        this.resetPasswordListener = listener;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(540, 780);
        setLocationRelativeTo(owner);
        setResizable(false);
        setContentPane(buildContent());
        installEscToClose();
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG_APP);

        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundRoundBorder(
                new ShadowBorder(new Color(0, 0, 0, 20), 16),
                new Insets(25, 35, 25, 35)
        ));
        card.setPreferredSize(new Dimension(480, 710));

        card.add(createHeader(), BorderLayout.NORTH);
        card.add(createBody(), BorderLayout.CENTER);
        card.add(createFooter(), BorderLayout.SOUTH);

        root.add(card);
        return root;
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // FIX LOGO: Bọc trong FlowLayout để không bị giãn ngang
        JPanel logoWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoWrapper.setOpaque(false);
        
        JPanel logoContainer = new JPanel(new BorderLayout());
        logoContainer.setBackground(CARD_BG);
        logoContainer.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 1, 16),
                new Insets(12, 16, 12, 16)
        ));
        JLabel logoIcon = new JLabel("\uD83D\uDC8A", SwingConstants.CENTER); // Icon viên thuốc
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        logoIcon.setForeground(PRIMARY);
        logoContainer.add(logoIcon, BorderLayout.CENTER);
        logoWrapper.add(logoContainer);

        JLabel mainTitle = new JLabel("MYCARE PHARMACY", SwingConstants.CENTER);
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        mainTitle.setForeground(PRIMARY);
        mainTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subTitle = new JLabel("KHÔI PHỤC MẬT KHẨU", SwingConstants.CENTER);
        subTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        subTitle.setForeground(TEXT_PRIMARY);
        subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel genericSub = new JLabel("Phần mềm quản lý thuốc", SwingConstants.CENTER);
        genericSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        genericSub.setForeground(TEXT_SECONDARY);
        genericSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(logoWrapper);
        panel.add(Box.createVerticalStrut(10));
        panel.add(mainTitle);
        panel.add(subTitle);
        panel.add(Box.createVerticalStrut(4));
        panel.add(genericSub);
        panel.add(Box.createVerticalStrut(10));

        return panel;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // 1. Info Box
        JPanel infoBox = new JPanel(new BorderLayout(12, 0));
        infoBox.setBackground(PRIMARY_SOFT);
        infoBox.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(PRIMARY, 1, 8),
                new Insets(12, 16, 12, 16)
        ));
        JLabel infoIcon = new JLabel("✉", SwingConstants.CENTER);
        infoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        infoIcon.setForeground(PRIMARY);
        JLabel infoText = new JLabel("Mã OTP gồm 6 số sẽ được gửi tới email của bạn.");
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        infoText.setForeground(PRIMARY);
        infoBox.add(infoIcon, BorderLayout.WEST);
        infoBox.add(infoText, BorderLayout.CENTER);
        
        gbc.insets = new Insets(0, 0, 16, 0);
        body.add(infoBox, gbc);

        // 2. Email Field
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 6, 0);
        body.add(createFieldLabel("Địa chỉ Email *"), gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);
        txtEmail = createTextField();
        body.add(txtEmail, gbc);

        // 3. Header OTP
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 6, 0);
        JPanel otpHeaderRow = new JPanel(new BorderLayout());
        otpHeaderRow.setOpaque(false);
        otpHeaderRow.add(createFieldLabel("Mã xác thực OTP *"), BorderLayout.WEST);
        
        lblCountdown = new JLabel(" ", SwingConstants.RIGHT);
        lblCountdown.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCountdown.setForeground(Color.RED);
        otpHeaderRow.add(lblCountdown, BorderLayout.EAST);
        body.add(otpHeaderRow, gbc);

        // 4. Hàng Nhập OTP & Nút bấm
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 4, 0);
        JPanel otpActionRow = new JPanel(new GridBagLayout());
        otpActionRow.setOpaque(false);
        
        GridBagConstraints otpGbc = new GridBagConstraints();
        otpGbc.fill = GridBagConstraints.BOTH;
        otpGbc.weighty = 1.0;
        
        // Ô nhập OTP
        txtOtp = createTextField();
        txtOtp.setHorizontalAlignment(SwingConstants.CENTER);
        txtOtp.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtOtp.setDocument(new JTextFieldLimit(6));
        txtOtp.setEnabled(false);
        otpGbc.weightx = 1.0;
        otpGbc.gridx = 0;
        otpGbc.insets = new Insets(0, 0, 0, 8);
        otpActionRow.add(txtOtp, otpGbc);

        // Nút Gửi mã
        btnGuiOtp = createCustomButton("Gửi mã", PRIMARY, Color.WHITE);
        btnGuiOtp.setPreferredSize(new Dimension(90, 42));
        btnGuiOtp.addActionListener(e -> guiOtp());
        otpGbc.weightx = 0.0;
        otpGbc.gridx = 1;
        otpActionRow.add(btnGuiOtp, otpGbc);

        // Nút Gửi lại
        btnGuiLai = createCustomButton("Gửi lại", BTN_SECONDARY_BG, TEXT_PRIMARY);
        btnGuiLai.setPreferredSize(new Dimension(80, 42));
        btnGuiLai.setEnabled(false);
        btnGuiLai.addActionListener(e -> guiOtp());
        otpGbc.gridx = 2;
        otpGbc.insets = new Insets(0, 0, 0, 0); // Xóa margin phải
        otpActionRow.add(btnGuiLai, otpGbc);

        body.add(otpActionRow, gbc);

        // 5. Status
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);
        lblStatus = new JLabel("Nhập email rồi nhấn 'Gửi mã'.");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(TEXT_SECONDARY);
        body.add(lblStatus, gbc);

        // 6. Mật khẩu mới
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 6, 0);
        body.add(createFieldLabel("Mật khẩu mới *"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);
        txtMatKhauMoi = createPasswordField();
        body.add(txtMatKhauMoi, gbc);

        // 7. Xác nhận mật khẩu
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 6, 0);
        body.add(createFieldLabel("Xác nhận mật khẩu *"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        txtNhapLaiMatKhau = createPasswordField();
        body.add(txtNhapLaiMatKhau, gbc);

        return body;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new GridLayout(1, 2, 12, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 0, 0, 0));

        btnHuy = createCustomButton("Hủy bỏ", BTN_SECONDARY_BG, TEXT_PRIMARY);
        btnHuy.setPreferredSize(new Dimension(0, 45));
        btnHuy.addActionListener(e -> dispose());

        // Đổi màu nút xác nhận thành màu xanh primary cho đồng bộ, hoặc đỏ tùy bạn
        btnXacNhan = createCustomButton("Xác nhận đổi mật khẩu", PRIMARY, Color.WHITE);
        btnXacNhan.setPreferredSize(new Dimension(0, 45));
        btnXacNhan.addActionListener(e -> verifyOtpAndResetPassword());

        footer.add(btnHuy);
        footer.add(btnXacNhan);
        return footer;
    }

    // --- TIỆN ÍCH TẠO UI ---

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    private JTextField createTextField() {
        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(0, 42));
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 1, 8),
                new Insets(5, 12, 5, 12)
        ));
        return txt;
    }

    private JPasswordField createPasswordField() {
        JPasswordField txt = new JPasswordField();
        txt.setPreferredSize(new Dimension(0, 42));
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 1, 8),
                new Insets(5, 12, 5, 12)
        ));
        return txt;
    }

    // FIX NÚT BẤM: Tự vẽ background để xóa bỏ hoàn toàn viền bóng kính mặc định của Windows
    private JButton createCustomButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (!isEnabled()) {
                    g2.setColor(new Color(226, 232, 240)); // Màu xám khi disable
                } else if (getModel().isPressed()) {
                    g2.setColor(bg.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.brighter());
                } else {
                    g2.setColor(bg);
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(fg);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // CÁC DÒNG NÀY RẤT QUAN TRỌNG ĐỂ XÓA VIỀN NATIVE
        btn.setContentAreaFilled(false); 
        btn.setFocusPainted(false);
        btn.setBorderPainted(false); 
        
        return btn;
    }

    // --- LOGIC XỬ LÝ (Giữ nguyên) ---
    private void guiOtp() {
        String email = txtEmail.getText() == null ? "" : txtEmail.getText().trim();
        if (email.isEmpty()) {
            showError("Vui lòng nhập địa chỉ email.");
            txtEmail.requestFocus();
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showError("Định dạng email không hợp lệ.");
            txtEmail.requestFocus();
            return;
        }
        try {
            currentEmail = email;
            currentOtp = String.format("%06d", new Random().nextInt(999999));
            txtOtp.setEnabled(true);
            txtOtp.setText("");
            txtOtp.requestFocus();

            btnGuiOtp.setEnabled(false);
            btnGuiLai.setEnabled(false);
            showInfo("Đang gửi mã OTP...");
            setCursor(new Cursor(Cursor.WAIT_CURSOR));

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    sendOtpMail(currentEmail, currentOtp);
                    return null;
                }
                @Override
                protected void done() {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    try {
                        get(); 
                        showSuccess("Mã OTP đã được gửi tới email.");
                        startCountdown(60);
                    } catch (Exception ex) {
                        showError("Lỗi gửi mail: Kiểm tra kết nối mạng hoặc SMTP.");
                        btnGuiOtp.setEnabled(true);
                    }
                }
            };
            worker.execute();
        } catch (Exception ex) {
            showError("Đã xảy ra lỗi.");
        }
    }

    private void verifyOtpAndResetPassword() {
        if (currentOtp == null || currentOtp.isEmpty()) {
            showError("Bạn chưa yêu cầu hoặc mã OTP đã hết hạn.");
            return;
        }
        String inputOtp = txtOtp.getText() == null ? "" : txtOtp.getText().trim();
        if (inputOtp.length() != 6 || !currentOtp.equals(inputOtp)) {
            showError("Mã OTP không chính xác.");
            return;
        }
        String matKhauMoi = new String(txtMatKhauMoi.getPassword()).trim();
        String nhapLai = new String(txtNhapLaiMatKhau.getPassword()).trim();

        if (matKhauMoi.isEmpty() || matKhauMoi.length() < 6) {
            showError("Mật khẩu mới phải có ít nhất 6 ký tự.");
            return;
        }
        if (!matKhauMoi.equals(nhapLai)) {
            showError("Xác nhận mật khẩu không khớp.");
            return;
        }
        stopCountdown();
        showSuccess("Xác thực thành công. Đang xử lý...");

        Timer t = new Timer(600, e -> {
            ((Timer) e.getSource()).stop();
            dispose();
            if (resetPasswordListener != null) {
                resetPasswordListener.onPasswordResetSuccess(currentEmail, matKhauMoi);
            }
        });
        t.setRepeats(false);
        t.start();
    }

    private void startCountdown(int seconds) {
        stopCountdown();
        secondsLeft = seconds;
        updateCountdownLabel();
        countdownTimer = new Timer(1000, e -> {
            secondsLeft--;
            updateCountdownLabel();
            if (secondsLeft <= 0) {
                stopCountdown();
                currentOtp = null;
                btnGuiLai.setEnabled(true);
                btnGuiOtp.setEnabled(false);
                txtOtp.setEnabled(false);
                showError("Mã OTP đã hết hạn. Vui lòng gửi lại.");
            }
        });
        countdownTimer.start();
    }

    private void stopCountdown() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        lblCountdown.setText(" ");
    }

    private void updateCountdownLabel() {
        if (secondsLeft > 0) {
            lblCountdown.setText(String.format("00:%02d", secondsLeft));
        }
    }

    private void showInfo(String text) {
        lblStatus.setForeground(TEXT_PRIMARY);
        lblStatus.setText(text);
    }

    private void showSuccess(String text) {
        lblStatus.setForeground(new Color(22, 163, 74));
        lblStatus.setText("✓ " + text);
    }

    private void showError(String text) {
        lblStatus.setForeground(Color.RED);
        lblStatus.setText("⚠ " + text);
    }

    private void installEscToClose() {
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private static void sendOtpMail(String toEmail, String otp) throws Exception {
        // Code mail giữ nguyên
    }

    // --- CUSTOM CLASSES ---
    private static class JTextFieldLimit extends javax.swing.text.PlainDocument {
        private int limit;
        JTextFieldLimit(int limit) { this.limit = limit; }
        public void insertString(int offset, String str, javax.swing.text.AttributeSet attr) throws javax.swing.text.BadLocationException {
            if (str == null || !str.matches("\\d+")) return;
            if ((getLength() + str.length()) <= limit) super.insertString(offset, str, attr);
        }
    }

    private static class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int radius;
        public RoundedLineBorder(Color color, int thickness, int radius) {
            this.color = color; this.thickness = thickness; this.radius = radius;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            for (int i = 0; i < thickness; i++) {
                g2.drawRoundRect(x + i, y + i, width - 1 - (i * 2), height - 1 - (i * 2), radius, radius);
            }
            g2.dispose();
        }
    }

    private static class ShadowBorder extends AbstractBorder {
        private final Color shadow;
        private final int radius;
        public ShadowBorder(Color shadow, int radius) {
            this.shadow = shadow; this.radius = radius;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < 6; i++) {
                g2.setColor(new Color(shadow.getRed(), shadow.getGreen(), shadow.getBlue(), Math.max(0, shadow.getAlpha() - (i * 3))));
                g2.drawRoundRect(x + i, y + i, width - 1 - (i * 2), height - 1 - (i * 2), radius, radius);
            }
            g2.dispose();
        }
    }

    private static class CompoundRoundBorder extends AbstractBorder {
        private final AbstractBorder outer;
        private final Insets innerPadding;
        public CompoundRoundBorder(AbstractBorder outer, Insets innerPadding) {
            this.outer = outer; this.innerPadding = innerPadding;
        }
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(innerPadding.top, innerPadding.left, innerPadding.bottom, innerPadding.right);
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            outer.paintBorder(c, g, x, y, width, height);
        }
    }
}