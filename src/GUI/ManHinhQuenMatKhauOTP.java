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

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String SMTP_USERNAME = "your_email@gmail.com";
    private static final String SMTP_APP_PASSWORD = "your_app_password";

    private static final Color BG_APP = new Color(15, 42, 74);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY = new Color(18, 87, 153);
    private static final Color PRIMARY_SOFT = new Color(239, 246, 255);
    private static final Color TEXT_PRIMARY = new Color(20, 28, 45);
    private static final Color TEXT_SECONDARY = new Color(99, 115, 140);
    private static final Color BORDER = new Color(223, 228, 235);
    private static final Color SUCCESS = new Color(22, 163, 74);
    private static final Color DANGER = new Color(220, 38, 38);
    private static final Color WARNING_BG = new Color(255, 247, 237);
    private static final Color WARNING_TEXT = new Color(194, 65, 12);

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
        super(owner, "Quên mật khẩu", ModalityType.APPLICATION_MODAL);
        this.resetPasswordListener = listener;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(620, 650);
        setLocationRelativeTo(owner);
        setResizable(false);
        setContentPane(buildContent());
        installEscToClose();
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG_APP);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel card = new JPanel(new BorderLayout(0, 18));
        card.setBackground(CARD_BG);
        card.setPreferredSize(new Dimension(560, 590));
        card.setBorder(new CompoundRoundBorder(
                new ShadowBorder(new Color(0, 0, 0, 20), 24),
                new Insets(22, 22, 22, 22)
        ));

        card.add(createHeader(), BorderLayout.NORTH);
        card.add(createBody(), BorderLayout.CENTER);
        card.add(createFooter(), BorderLayout.SOUTH);

        root.add(card);
        return root;
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("🔑", SwingConstants.CENTER);
        icon.setOpaque(true);
        icon.setBackground(PRIMARY_SOFT);
        icon.setForeground(PRIMARY);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 34));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        icon.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("QUÊN MẬT KHẨU");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Nhập email, nhận OTP và đặt lại mật khẩu mới.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(TEXT_SECONDARY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(icon);
        panel.add(Box.createVerticalStrut(14));
        panel.add(title);
        panel.add(Box.createVerticalStrut(8));
        panel.add(sub);

        return panel;
    }

    private JPanel createBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel infoBox = new JPanel(new BorderLayout(8, 0));
        infoBox.setBackground(WARNING_BG);
        infoBox.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(new Color(253, 230, 138), 1, 16),
                new Insets(12, 14, 12, 14)
        ));
        infoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel infoIcon = new JLabel("✉", SwingConstants.CENTER);
        infoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        infoIcon.setForeground(WARNING_TEXT);

        JLabel infoText = new JLabel("OTP sẽ được gửi tới email bạn nhập bên dưới");
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        infoText.setForeground(TEXT_PRIMARY);

        infoBox.add(infoIcon, BorderLayout.WEST);
        infoBox.add(infoText, BorderLayout.CENTER);

        JLabel lblEmail = createFieldLabel("Email");
        txtEmail = createTextField();
        txtEmail.setToolTipText("Nhập email dùng để khôi phục mật khẩu");

        JLabel lblOtp = createFieldLabel("Mã OTP");
        txtOtp = createTextField();
        txtOtp.setHorizontalAlignment(SwingConstants.CENTER);
        txtOtp.setFont(new Font("Segoe UI", Font.BOLD, 20));
        txtOtp.setEnabled(false);
        txtOtp.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) || txtOtp.getText().length() >= 6) {
                    e.consume();
                }
            }
        });

        JPanel actionRow = new JPanel(new GridLayout(1, 2, 10, 0));
        actionRow.setOpaque(false);
        actionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        btnGuiOtp = createButton("Gửi mã OTP", PRIMARY, Color.WHITE);
        btnGuiOtp.addActionListener(e -> guiOtp());

        btnGuiLai = createButton("Gửi lại", new Color(248, 250, 252), TEXT_PRIMARY);
        btnGuiLai.setBorder(new RoundedLineBorder(BORDER, 1, 14));
        btnGuiLai.setEnabled(false);
        btnGuiLai.addActionListener(e -> guiOtp());

        actionRow.add(btnGuiOtp);
        actionRow.add(btnGuiLai);

        JPanel statusRow = new JPanel(new BorderLayout(10, 0));
        statusRow.setOpaque(false);
        statusRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        lblStatus = new JLabel("Nhập email rồi nhấn 'Gửi mã OTP'.");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatus.setForeground(TEXT_SECONDARY);

        lblCountdown = new JLabel(" ", SwingConstants.RIGHT);
        lblCountdown.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCountdown.setForeground(PRIMARY);

        statusRow.add(lblStatus, BorderLayout.WEST);
        statusRow.add(lblCountdown, BorderLayout.EAST);

        JLabel lblMatKhauMoi = createFieldLabel("Mật khẩu mới");
        txtMatKhauMoi = createPasswordField();

        JLabel lblNhapLai = createFieldLabel("Nhập lại mật khẩu");
        txtNhapLaiMatKhau = createPasswordField();

        body.add(infoBox);
        body.add(Box.createVerticalStrut(14));
        body.add(lblEmail);
        body.add(Box.createVerticalStrut(6));
        body.add(txtEmail);
        body.add(Box.createVerticalStrut(14));
        body.add(lblOtp);
        body.add(Box.createVerticalStrut(6));
        body.add(txtOtp);
        body.add(Box.createVerticalStrut(12));
        body.add(actionRow);
        body.add(Box.createVerticalStrut(12));
        body.add(statusRow);
        body.add(Box.createVerticalStrut(14));
        body.add(lblMatKhauMoi);
        body.add(Box.createVerticalStrut(6));
        body.add(txtMatKhauMoi);
        body.add(Box.createVerticalStrut(14));
        body.add(lblNhapLai);
        body.add(Box.createVerticalStrut(6));
        body.add(txtNhapLaiMatKhau);

        return body;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new GridLayout(1, 2, 12, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(8, 0, 0, 0));

        btnHuy = createButton("Hủy", new Color(248, 250, 252), TEXT_PRIMARY);
        btnHuy.setBorder(new RoundedLineBorder(BORDER, 1, 14));
        btnHuy.addActionListener(e -> dispose());

        btnXacNhan = createButton("Xác nhận đổi mật khẩu", DANGER, Color.WHITE);
        btnXacNhan.addActionListener(e -> verifyOtpAndResetPassword());

        footer.add(btnHuy);
        footer.add(btnXacNhan);
        return footer;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField createTextField() {
        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(0, 46));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txt.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(BORDER, 1, 16),
                new Insets(8, 16, 8, 16)
        ));
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);
        return txt;
    }

    private JPasswordField createPasswordField() {
        JPasswordField txt = new JPasswordField();
        txt.setPreferredSize(new Dimension(0, 46));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txt.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(BORDER, 1, 16),
                new Insets(8, 16, 8, 16)
        ));
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);
        return txt;
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new RoundedLineBorder(bg, 1, 14));
        btn.setPreferredSize(new Dimension(0, 46));
        return btn;
    }

    private void guiOtp() {
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

            txtOtp.setEnabled(true);
            txtOtp.setText("");
            txtOtp.requestFocus();

            sendOtpMail(currentEmail, currentOtp);

            btnGuiOtp.setEnabled(false);
            btnGuiLai.setEnabled(false);

            showInfo("Mã OTP đã được gửi tới: " + currentEmail);
            startCountdown(60);
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Không thể gửi OTP. Kiểm tra cấu hình thư viện mail và SMTP.");
            btnGuiOtp.setEnabled(true);
            btnGuiLai.setEnabled(true);
        }
    }

    private void verifyOtpAndResetPassword() {
        if (currentOtp == null || currentOtp.isEmpty()) {
            showError("Bạn chưa gửi mã OTP.");
            return;
        }

        String inputOtp = txtOtp.getText() == null ? "" : txtOtp.getText().trim();
        if (inputOtp.length() != 6) {
            showError("Mã OTP phải gồm 6 số.");
            return;
        }

        if (!currentOtp.equals(inputOtp)) {
            showError("Mã OTP không đúng.");
            return;
        }

        String matKhauMoi = new String(txtMatKhauMoi.getPassword()).trim();
        String nhapLai = new String(txtNhapLaiMatKhau.getPassword()).trim();

        if (matKhauMoi.isEmpty() || nhapLai.isEmpty()) {
            showError("Vui lòng nhập mật khẩu mới.");
            return;
        }

        if (matKhauMoi.length() < 6) {
            showError("Mật khẩu mới phải từ 6 ký tự.");
            return;
        }

        if (!matKhauMoi.equals(nhapLai)) {
            showError("Nhập lại mật khẩu không khớp.");
            return;
        }

        stopCountdown();
        showSuccess("Xác thực thành công. Đang cập nhật mật khẩu...");

        Timer t = new Timer(500, e -> {
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
        btnGuiOtp.setEnabled(true);
    }

    private void updateCountdownLabel() {
        if (secondsLeft > 0) {
            lblCountdown.setText("Hết hạn sau: 00:" + String.format("%02d", secondsLeft));
        } else {
            lblCountdown.setText(" ");
        }
    }

    private String generateOtp() {
        int otp = new Random().nextInt(900000) + 100000;
        return String.valueOf(otp);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private void showInfo(String text) {
        lblStatus.setForeground(TEXT_SECONDARY);
        lblStatus.setText(text);
    }

    private void showSuccess(String text) {
        lblStatus.setForeground(SUCCESS);
        lblStatus.setText(text);
    }

    private void showError(String text) {
        lblStatus.setForeground(DANGER);
        lblStatus.setText(text);
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
        message.setContent(buildHtmlContent(otp), "text/html; charset=UTF-8");

        Transport.send(message);
    }

    private static String buildHtmlContent(String otp) {
        return "<div style='font-family:Segoe UI,Arial,sans-serif;max-width:600px;margin:auto;padding:24px;background:#f8fafc;'>"
                + "<div style='background:#ffffff;border-radius:18px;padding:28px;border:1px solid #e2e8f0;'>"
                + "<h2 style='margin:0 0 12px;color:#0f172a;'>Đặt lại mật khẩu MYCARE</h2>"
                + "<p style='font-size:14px;color:#475569;line-height:1.6;'>"
                + "Bạn vừa yêu cầu đặt lại mật khẩu. Vui lòng sử dụng mã OTP bên dưới để xác nhận."
                + "</p>"
                + "<div style='margin:22px 0;padding:16px;border-radius:14px;background:#eff6ff;text-align:center;'>"
                + "<div style='font-size:30px;font-weight:700;letter-spacing:8px;color:#125799;'>" + otp + "</div>"
                + "</div>"
                + "<p style='font-size:13px;color:#64748b;'>Mã có hiệu lực trong 60 giây.</p>"
                + "</div></div>";
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
            return new Insets(10, 12, 10, 12);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = 12;
            insets.right = 12;
            insets.top = 10;
            insets.bottom = 10;
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
            return new Insets(5, 5, 10, 5);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.top = 5;
            insets.left = 5;
            insets.bottom = 10;
            insets.right = 5;
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < 6; i++) {
                g2.setColor(new Color(
                        shadow.getRed(),
                        shadow.getGreen(),
                        shadow.getBlue(),
                        Math.max(4, shadow.getAlpha() - i * 3)
                ));
                g2.drawRoundRect(x + 1, y + 1 + i, width - 3, height - 3 - i, radius, radius);
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
            Insets o = outer.getBorderInsets(c);
            return new Insets(
                    o.top + innerPadding.top,
                    o.left + innerPadding.left,
                    o.bottom + innerPadding.bottom,
                    o.right + innerPadding.right
            );
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            Insets o = outer.getBorderInsets(c);
            insets.top = o.top + innerPadding.top;
            insets.left = o.left + innerPadding.left;
            insets.bottom = o.bottom + innerPadding.bottom;
            insets.right = o.right + innerPadding.right;
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            outer.paintBorder(c, g, x, y, width, height);
        }
    }
}