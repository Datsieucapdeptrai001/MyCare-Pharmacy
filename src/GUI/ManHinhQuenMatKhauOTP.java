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
    private static final String SMTP_USERNAME = "namgpt1310@gmail.com";
    private static final String SMTP_APP_PASSWORD = "drpf zlsf edjg wdje";

    // --- MÀU ---
    private static final Color BG_APP = new Color(15, 42, 74);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY = new Color(30, 75, 138);
    private static final Color PRIMARY_SOFT = new Color(240, 247, 255);
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);
    private static final Color BTN_SECONDARY_BG = new Color(241, 245, 249);
    private static final Color SUCCESS = new Color(22, 163, 74);
    private static final Color ERROR = new Color(220, 38, 38);
    private static final Color WARNING_BG = new Color(255, 247, 237);
    private static final Color WARNING_BORDER = new Color(253, 230, 138);
    private static final Color WARNING_TEXT = new Color(194, 65, 12);

    private final ResetPasswordListener resetPasswordListener;

    private JTextField txtEmail;
    private JTextField txtOtp;
    private JPasswordField txtMatKhauMoi;
    private JPasswordField txtNhapLaiMatKhau;

    private JLabel lblStep;
    private JLabel lblStatus;
    private JLabel lblCountdown;
    private JLabel lblEmailPreview;

    private JButton btnNextEmail;
    private JButton btnVerifyOtp;
    private JButton btnBackOtp;
    private JButton btnBackPassword;
    private JButton btnConfirmPassword;
    private JButton btnGuiLai;
    private JButton btnHuy;

    private JPanel contentCards;
    private CardLayout cardLayout;

    private Timer countdownTimer;
    private int secondsLeft = 0;
    private String currentOtp = null;
    private String currentEmail = "";

    public ManHinhQuenMatKhauOTP(Window owner, ResetPasswordListener listener) {
        super(owner, "Khôi phục mật khẩu", ModalityType.APPLICATION_MODAL);
        this.resetPasswordListener = listener;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(540, 760);
        setLocationRelativeTo(owner);
        setResizable(false);
        setContentPane(buildContent());
        installEscToClose();
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG_APP);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundRoundBorder(
                new ShadowBorder(new Color(0, 0, 0, 20), 16),
                new Insets(25, 35, 25, 35)
        ));
        card.setPreferredSize(new Dimension(480, 690));

        card.add(createHeader(), BorderLayout.NORTH);
        card.add(createCenter(), BorderLayout.CENTER);
        card.add(createBottomStatus(), BorderLayout.SOUTH);

        root.add(card);
        return root;
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JPanel logoWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoWrapper.setOpaque(false);

        JPanel logoContainer = new JPanel(new BorderLayout());
        logoContainer.setBackground(CARD_BG);
        logoContainer.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 1, 16),
                new Insets(12, 16, 12, 16)
        ));

        JLabel logoIcon = new JLabel("\uD83D\uDD11", SwingConstants.CENTER);
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        logoIcon.setForeground(PRIMARY);
        logoContainer.add(logoIcon, BorderLayout.CENTER);
        logoWrapper.add(logoContainer);

        JLabel mainTitle = new JLabel("KHÔI PHỤC MẬT KHẨU", SwingConstants.CENTER);
        mainTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        mainTitle.setForeground(TEXT_PRIMARY);
        mainTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subTitle = new JLabel("Nhập email, xác thực OTP và đặt lại mật khẩu mới.", SwingConstants.CENTER);
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subTitle.setForeground(TEXT_SECONDARY);
        subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblStep = new JLabel("Bước 1/3: Nhập email", SwingConstants.CENTER);
        lblStep.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStep.setForeground(PRIMARY);
        lblStep.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(logoWrapper);
        panel.add(Box.createVerticalStrut(10));
        panel.add(mainTitle);
        panel.add(Box.createVerticalStrut(6));
        panel.add(subTitle);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblStep);

        return panel;
    }

    private JPanel createCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);

        JPanel infoBox = new JPanel(new BorderLayout(12, 0));
        infoBox.setBackground(WARNING_BG);
        infoBox.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(WARNING_BORDER, 1, 8),
                new Insets(12, 16, 12, 16)
        ));

        JLabel infoIcon = new JLabel("✉", SwingConstants.CENTER);
        infoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        infoIcon.setForeground(WARNING_TEXT);

        JLabel infoText = new JLabel("Mã OTP gồm 6 số sẽ được gửi tới email của bạn.");
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        infoText.setForeground(WARNING_TEXT);

        infoBox.add(infoIcon, BorderLayout.WEST);
        infoBox.add(infoText, BorderLayout.CENTER);

        center.add(infoBox, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setOpaque(false);

        contentCards.add(createEmailStep(), "STEP_EMAIL");
        contentCards.add(createOtpStep(), "STEP_OTP");
        contentCards.add(createPasswordStep(), "STEP_PASSWORD");

        center.add(contentCards, BorderLayout.CENTER);
        return center;
    }

    private JPanel createEmailStep() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = baseGbc();

        panel.add(createFieldLabel("Địa chỉ Email *"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);
        txtEmail = createTextField();
        panel.add(txtEmail, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);
        btnNextEmail = createCustomButton("Gửi mã OTP", PRIMARY, Color.WHITE);
        btnNextEmail.setPreferredSize(new Dimension(0, 44));
        btnNextEmail.addActionListener(e -> guiOtpVaChuyenBuoc());
        panel.add(btnNextEmail, gbc);

        return panel;
    }

    private JPanel createOtpStep() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = baseGbc();

        panel.add(createFieldLabel("Email xác thực"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 14, 0);
        lblEmailPreview = new JLabel(" ");
        lblEmailPreview.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblEmailPreview.setForeground(PRIMARY);
        panel.add(lblEmailPreview, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 6, 0);

        JPanel otpHeader = new JPanel(new BorderLayout());
        otpHeader.setOpaque(false);
        otpHeader.add(createFieldLabel("Mã xác thực OTP *"), BorderLayout.WEST);

        lblCountdown = new JLabel(" ", SwingConstants.RIGHT);
        lblCountdown.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCountdown.setForeground(ERROR);
        otpHeader.add(lblCountdown, BorderLayout.EAST);

        panel.add(otpHeader, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 14, 0);
        txtOtp = createTextField();
        txtOtp.setHorizontalAlignment(SwingConstants.CENTER);
        txtOtp.setFont(new Font("Segoe UI", Font.BOLD, 20));
        txtOtp.setDocument(new JTextFieldLimit(6));
        panel.add(txtOtp, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);

        JPanel actionRow = new JPanel(new GridLayout(1, 3, 10, 0));
        actionRow.setOpaque(false);

        btnBackOtp = createCustomButton("Quay lại", BTN_SECONDARY_BG, TEXT_PRIMARY);
        btnBackOtp.addActionListener(e -> {
            stopCountdown();
            cardLayout.show(contentCards, "STEP_EMAIL");
            lblStep.setText("Bước 1/3: Nhập email");
            showInfo("Nhập email rồi nhấn 'Gửi mã OTP'.");
        });

        btnGuiLai = createCustomButton("Gửi lại", BTN_SECONDARY_BG, TEXT_PRIMARY);
        btnGuiLai.setEnabled(false);
        btnGuiLai.addActionListener(e -> guiOtpVaChuyenBuoc());

        btnVerifyOtp = createCustomButton("Xác thực OTP", PRIMARY, Color.WHITE);
        btnVerifyOtp.addActionListener(e -> xacThucOtpVaChuyenBuoc());

        actionRow.add(btnBackOtp);
        actionRow.add(btnGuiLai);
        actionRow.add(btnVerifyOtp);

        panel.add(actionRow, gbc);

        return panel;
    }

    private JPanel createPasswordStep() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        GridBagConstraints gbc = baseGbc();

        panel.add(createFieldLabel("Mật khẩu mới *"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);
        txtMatKhauMoi = createPasswordField();
        panel.add(txtMatKhauMoi, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 6, 0);
        panel.add(createFieldLabel("Xác nhận mật khẩu *"), gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 16, 0);
        txtNhapLaiMatKhau = createPasswordField();
        panel.add(txtNhapLaiMatKhau, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0);

        JPanel actionRow = new JPanel(new GridLayout(1, 2, 10, 0));
        actionRow.setOpaque(false);

        btnBackPassword = createCustomButton("Quay lại", BTN_SECONDARY_BG, TEXT_PRIMARY);
        btnBackPassword.addActionListener(e -> {
            cardLayout.show(contentCards, "STEP_OTP");
            lblStep.setText("Bước 2/3: Nhập OTP");
            showInfo("Nhập mã OTP để xác thực.");
        });

        btnConfirmPassword = createCustomButton("Đổi mật khẩu", PRIMARY, Color.WHITE);
        btnConfirmPassword.addActionListener(e -> verifyOtpAndResetPassword());

        actionRow.add(btnBackPassword);
        actionRow.add(btnConfirmPassword);

        panel.add(actionRow, gbc);

        return panel;
    }

    private JPanel createBottomStatus() {
        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(12, 0, 0, 0));

        lblStatus = new JLabel("Nhập email rồi nhấn 'Gửi mã OTP'.");
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatus.setForeground(TEXT_SECONDARY);

        btnHuy = createCustomButton("Đóng", BTN_SECONDARY_BG, TEXT_PRIMARY);
        btnHuy.setPreferredSize(new Dimension(90, 38));
        btnHuy.addActionListener(e -> dispose());

        bottom.add(lblStatus, BorderLayout.CENTER);
        bottom.add(btnHuy, BorderLayout.EAST);
        return bottom;
    }

    private GridBagConstraints baseGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 6, 0);
        return gbc;
    }

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

    private JButton createCustomButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color drawColor = bg;
                if (!isEnabled()) {
                    drawColor = new Color(226, 232, 240);
                } else if (getModel().isPressed()) {
                    drawColor = bg.darker();
                } else if (getModel().isRollover()) {
                    drawColor = brighter(bg, 12);
                }

                g2.setColor(drawColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();

                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(fg);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        return btn;
    }

    private Color brighter(Color color, int amount) {
        int r = Math.min(255, color.getRed() + amount);
        int g = Math.min(255, color.getGreen() + amount);
        int b = Math.min(255, color.getBlue() + amount);
        return new Color(r, g, b);
    }

    private void guiOtpVaChuyenBuoc() {
        String email = txtEmail.getText() == null ? "" : txtEmail.getText().trim();

        if (email.isEmpty()) {
            showError("Vui lòng nhập email.");
            txtEmail.requestFocus();
            return;
        }

        if (!isValidEmail(email)) {
            showError("Email không hợp lệ.");
            txtEmail.requestFocus();
            return;
        }

        try {
            currentEmail = email;
            currentOtp = generateOtp();

            sendOtpMail(currentEmail, currentOtp);

            lblEmailPreview.setText(currentEmail);
            txtOtp.setText("");
            txtOtp.setEnabled(true);

            btnGuiLai.setEnabled(false);
            startCountdown(60);

            cardLayout.show(contentCards, "STEP_OTP");
            lblStep.setText("Bước 2/3: Nhập OTP");
            showSuccess("Mã OTP đã được gửi tới email.");

            SwingUtilities.invokeLater(() -> txtOtp.requestFocus());
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Không thể gửi OTP. Kiểm tra cấu hình mail hoặc App Password.");
        }
    }

    private void xacThucOtpVaChuyenBuoc() {
        if (currentOtp == null || currentOtp.isEmpty()) {
            showError("Bạn chưa yêu cầu hoặc mã OTP đã hết hạn.");
            return;
        }

        String inputOtp = txtOtp.getText() == null ? "" : txtOtp.getText().trim();
        if (inputOtp.length() != 6 || !currentOtp.equals(inputOtp)) {
            showError("Mã OTP không chính xác.");
            return;
        }

        cardLayout.show(contentCards, "STEP_PASSWORD");
        lblStep.setText("Bước 3/3: Đặt mật khẩu mới");
        showSuccess("OTP hợp lệ. Hãy nhập mật khẩu mới.");
        SwingUtilities.invokeLater(() -> txtMatKhauMoi.requestFocus());
    }

    private void verifyOtpAndResetPassword() {
        String matKhauMoi = new String(txtMatKhauMoi.getPassword()).trim();
        String nhapLai = new String(txtNhapLaiMatKhau.getPassword()).trim();

        if (!isValidNewPassword(matKhauMoi)) {
            showError("Mật khẩu mới phải có ít nhất 8 ký tự, gồm chữ và số.");
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
    private boolean isValidNewPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            }

            if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }

        return hasLetter && hasDigit;
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
                txtOtp.setEnabled(false);
                btnGuiLai.setEnabled(true);
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
        } else {
            lblCountdown.setText(" ");
        }
    }

    private void showInfo(String text) {
        lblStatus.setForeground(TEXT_PRIMARY);
        lblStatus.setText(text);
    }

    private void showSuccess(String text) {
        lblStatus.setForeground(SUCCESS);
        lblStatus.setText("✓ " + text);
    }

    private void showError(String text) {
        lblStatus.setForeground(ERROR);
        lblStatus.setText("⚠ " + text);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private String generateOtp() {
        int otp = new Random().nextInt(900000) + 100000;
        return String.valueOf(otp);
    }

    private void installEscToClose() {
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private static void sendOtpMail(String toEmail, String otp) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.debug", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_APP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_USERNAME, "MYCARE Pharmacy"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("[MYCARE] Mã OTP đặt lại mật khẩu");
        message.setText("Mã OTP của bạn là: " + otp);

        System.out.println("===== BAT DAU GUI MAIL =====");
        System.out.println("Gui tu: " + SMTP_USERNAME);
        System.out.println("Gui den: " + toEmail);
        System.out.println("OTP: " + otp);

        Transport.send(message);

        System.out.println("===== GUI MAIL THANH CONG =====");
    }

    private static class JTextFieldLimit extends javax.swing.text.PlainDocument {
        private final int limit;

        JTextFieldLimit(int limit) {
            this.limit = limit;
        }

        @Override
        public void insertString(int offset, String str, javax.swing.text.AttributeSet attr)
                throws javax.swing.text.BadLocationException {
            if (str == null || !str.matches("\\d+")) return;
            if ((getLength() + str.length()) <= limit) {
                super.insertString(offset, str, attr);
            }
        }
    }

    private static class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int radius;

        public RoundedLineBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 8, 8, 8);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.top = 8;
            insets.left = 8;
            insets.bottom = 8;
            insets.right = 8;
            return insets;
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
            this.shadow = shadow;
            this.radius = radius;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(6, 6, 10, 6);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.top = 6;
            insets.left = 6;
            insets.bottom = 10;
            insets.right = 6;
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < 6; i++) {
                int alpha = Math.max(0, shadow.getAlpha() - (i * 3));
                g2.setColor(new Color(shadow.getRed(), shadow.getGreen(), shadow.getBlue(), alpha));
                g2.drawRoundRect(x + i, y + i, width - 1 - (i * 2), height - 1 - (i * 2), radius, radius);
            }
            g2.dispose();
        }
    }

    private static class CompoundRoundBorder extends AbstractBorder {
        private final AbstractBorder outer;
        private final Insets innerPadding;

        public CompoundRoundBorder(AbstractBorder outer, Insets innerPadding) {
            this.outer = outer;
            this.innerPadding = innerPadding;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            Insets outerInsets = outer.getBorderInsets(c);
            return new Insets(
                    outerInsets.top + innerPadding.top,
                    outerInsets.left + innerPadding.left,
                    outerInsets.bottom + innerPadding.bottom,
                    outerInsets.right + innerPadding.right
            );
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            Insets outerInsets = outer.getBorderInsets(c);
            insets.top = outerInsets.top + innerPadding.top;
            insets.left = outerInsets.left + innerPadding.left;
            insets.bottom = outerInsets.bottom + innerPadding.bottom;
            insets.right = outerInsets.right + innerPadding.right;
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            outer.paintBorder(c, g, x, y, width, height);
        }
    }
}