package GUI;

import DAO.DAO_SanPham;
import Entity.SanPham;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class ManHinhNhapLoHangMoi extends JDialog {

    public interface BatchSubmitListener {
        void onSubmit(SanPham sanPham, String maLo, int soLuong, int giaNhap, String hanSuDung, String trangThai);
    }

    // --- MÀU SẮC CHUẨN TAILWIND CSS ---
    private static final Color BG_TRANSPARENT = new Color(0, 0, 0, 0);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color HEADER_BG = new Color(15, 23, 42); // Slate 900
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color TEXT_HINT = new Color(148, 163, 184);

    private static final Color PRIMARY = new Color(37, 99, 235); // Blue 600
    private static final Color PRIMARY_HOVER = new Color(29, 78, 216); // Blue 700
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color BORDER_FOCUS = new Color(59, 130, 246);
    private static final Color DANGER = new Color(239, 68, 68);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_TEXT = new Font("Segoe UI", Font.PLAIN, 14);

    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final BatchSubmitListener listener;
    private final NumberFormat vnNumberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
    private final DAO_SanPham daoSanPham = new DAO_SanPham();

    private JComboBox<SanPham> cboSanPham;
    private HintTextField txtMaLo;
    private HintTextField txtSoLuong;
    private HintTextField txtGiaNhap;
    private HintTextField txtHanSuDung;
    private JComboBox<String> cboTrangThai;

    private JLabel errSanPham, errMaLo, errSoLuong, errGiaNhap, errHanSuDung;

    public ManHinhNhapLoHangMoi(Window owner, BatchSubmitListener listener) {
        super(owner, "Nhập lô hàng mới", ModalityType.APPLICATION_MODAL);
        this.listener = listener;

        setUndecorated(true);
        setBackground(BG_TRANSPARENT); // Cho phép bo góc trong suốt
        setSize(560, 560);
        setLocationRelativeTo(owner);
        
        // Bo góc toàn bộ cửa sổ Dialog
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));

        setContentPane(createMainUI());
        registerKeyboardActions();
    }

    private JPanel createMainUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1)); // Viền siêu mảnh

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createBody(), BorderLayout.CENTER);
        root.add(createFooter(), BorderLayout.SOUTH);

        return root;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(16, 24, 16, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel lblIcon = new JLabel("📦");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        lblIcon.setForeground(Color.WHITE);

        JLabel title = new JLabel("Nhập Lô Hàng Mới");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);

        left.add(lblIcon);
        left.add(title);

        JButton btnClose = new JButton("✕");
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setForeground(new Color(148, 163, 184));
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnClose.setForeground(DANGER); }
            @Override public void mouseExited(MouseEvent e) { btnClose.setForeground(new Color(148, 163, 184)); }
        });
        btnClose.addActionListener(e -> dispose());
        
        // Drag window
        MouseAdapter ma = new MouseAdapter() {
            int x, y;
            @Override public void mousePressed(MouseEvent e) { x = e.getX(); y = e.getY(); }
            @Override public void mouseDragged(MouseEvent e) { setLocation(getLocation().x + e.getX() - x, getLocation().y + e.getY() - y); }
        };
        header.addMouseListener(ma);
        header.addMouseMotionListener(ma);

        header.add(left, BorderLayout.WEST);
        header.add(btnClose, BorderLayout.EAST);
        return header;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(CARD_BG);
        body.setBorder(new EmptyBorder(24, 30, 10, 30)); 

        cboSanPham = new JComboBox<>();
        styleComboBox(cboSanPham);
        loadSanPhamToComboBox();

        txtMaLo = createTextField("VD: LOT-2026-001");
        txtSoLuong = createTextField("0");
        txtGiaNhap = createTextField("0");
        
        ((AbstractDocument) txtSoLuong.getDocument()).setDocumentFilter(new DigitsOnlyFilter(9));
        ((AbstractDocument) txtGiaNhap.getDocument()).setDocumentFilter(new CurrencyDigitsFilter(12));
        addCurrencyFormatting(txtGiaNhap);

        cboTrangThai = new JComboBox<>(new String[]{"Được bán", "Hết hàng", "Hết hạn"});
        styleComboBox(cboTrangThai);

        errSanPham = createErrorLabel();
        errMaLo = createErrorLabel();
        errSoLuong = createErrorLabel();
        errGiaNhap = createErrorLabel();
        errHanSuDung = createErrorLabel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.insets = new Insets(0, 0, 14, 0); 

        gbc.gridy = 0;
        body.add(createFullWidthField("Sản phẩm *", cboSanPham, errSanPham), gbc);

        gbc.gridy = 1;
        body.add(createTwoColumnRow("Mã lô *", txtMaLo, errMaLo, "Tình trạng", cboTrangThai, createErrorLabel()), gbc);

        gbc.gridy = 2;
        body.add(createTwoColumnRow("Số lượng *", txtSoLuong, errSoLuong, "Giá nhập (đ/ĐV)", txtGiaNhap, errGiaNhap), gbc);

        // -- Tích hợp DatePicker cho Hạn Sử Dụng --
        gbc.gridy = 3;
        JPanel dateFieldWrapper = createDatePickerField();
        body.add(createFullWidthField("Hạn sử dụng (dd/MM/yyyy) *", dateFieldWrapper, errHanSuDung), gbc);
        
        gbc.gridy = 4; gbc.weighty = 1.0;
        body.add(Box.createVerticalGlue(), gbc);

        return body;
    }

    private JPanel createDatePickerField() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Color.WHITE);
        ModernBorder sharedBorder = new ModernBorder(wrap);
        wrap.setBorder(sharedBorder);

        txtHanSuDung = new HintTextField("dd/MM/yyyy");
        txtHanSuDung.setFont(FONT_TEXT);
        txtHanSuDung.setForeground(TEXT_PRIMARY);
        txtHanSuDung.setBorder(null); // Bỏ viền của text field để xài viền của wrap

        JButton btnCal = new JButton("📅");
        btnCal.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        btnCal.setFocusPainted(false);
        btnCal.setContentAreaFilled(false);
        btnCal.setBorder(new EmptyBorder(0, 8, 0, 8));
        btnCal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Logic mở popup Lịch
        CustomDatePicker datePickerPopup = new CustomDatePicker(txtHanSuDung);
        btnCal.addActionListener(e -> {
            datePickerPopup.show(txtHanSuDung, 0, txtHanSuDung.getHeight() + 4);
        });

        // Đổi màu viền khi focus
        txtHanSuDung.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { sharedBorder.setFocused(true); wrap.repaint(); }
            @Override public void focusLost(FocusEvent e) { sharedBorder.setFocused(false); wrap.repaint(); }
        });

        wrap.add(txtHanSuDung, BorderLayout.CENTER);
        wrap.add(btnCal, BorderLayout.EAST);
        return wrap;
    }

    private JPanel createFullWidthField(String title, JComponent field, JLabel error) {
        JPanel block = new JPanel(new BorderLayout(0, 8)); 
        block.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_LABEL);
        lblTitle.setForeground(TEXT_PRIMARY);

        JPanel fieldWrapper = new JPanel(new BorderLayout(0, 4));
        fieldWrapper.setOpaque(false);
        fieldWrapper.add(field, BorderLayout.CENTER);
        fieldWrapper.add(error, BorderLayout.SOUTH);

        block.add(lblTitle, BorderLayout.NORTH);
        block.add(fieldWrapper, BorderLayout.CENTER);

        return block;
    }

    private JPanel createTwoColumnRow(String title1, JComponent field1, JLabel error1,
                                      String title2, JComponent field2, JLabel error2) {
        JPanel rowPanel = new JPanel(new GridLayout(1, 2, 24, 0)); 
        rowPanel.setOpaque(false);
        rowPanel.add(createFullWidthField(title1, field1, error1));
        rowPanel.add(createFullWidthField(title2, field2, error2));
        return rowPanel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);

        JButton btnCancel = createSecondaryButton("Hủy");
        JButton btnSubmit = createPrimaryButton("Lưu lô hàng");

        btnCancel.addActionListener(e -> dispose());
        btnSubmit.addActionListener(e -> handleSubmit());

        actions.add(btnCancel);
        actions.add(btnSubmit);

        footer.add(actions, BorderLayout.EAST);
        return footer;
    }

    private void loadSanPhamToComboBox() {
        cboSanPham.removeAllItems();
        try {
            for (SanPham sp : daoSanPham.getDsThuoc()) {
                cboSanPham.addItem(sp);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private HintTextField createTextField(String hint) {
        HintTextField field = new HintTextField(hint);
        field.setPreferredSize(new Dimension(100, 40)); 
        field.setFont(FONT_TEXT);
        field.setForeground(TEXT_PRIMARY);
        ModernBorder b = new ModernBorder(field);
        field.setBorder(b);
        
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { b.setFocused(true); field.repaint(); }
            @Override public void focusLost(FocusEvent e) { b.setFocused(false); field.repaint(); }
        });
        return field;
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setPreferredSize(new Dimension(100, 40)); 
        combo.setFont(FONT_TEXT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBackground(Color.WHITE);
        combo.setFocusable(true);
        ModernBorder b = new ModernBorder(combo);
        combo.setBorder(b);
        combo.setUI(new FlatComboBoxUI());
        
        combo.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { b.setFocused(true); combo.repaint(); }
            @Override public void focusLost(FocusEvent e) { b.setFocused(false); combo.repaint(); }
        });

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lb = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lb.setBorder(new EmptyBorder(8, 12, 8, 12)); 
                lb.setFont(FONT_TEXT);
                if (isSelected) {
                    lb.setBackground(new Color(239, 246, 255));
                    lb.setForeground(PRIMARY);
                } else { lb.setBackground(Color.WHITE); }
                if (value instanceof SanPham sp) { lb.setText(sp.getTen() + " (" + sp.getId() + ")"); }
                return lb;
            }
        });
    }

    private JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12)); 
        label.setForeground(DANGER);
        return label;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 24, 10, 24)); 
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(PRIMARY_HOVER); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(PRIMARY); }
        });
        return btn;
    }

    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(9, 23, 9, 23)
        ));
        btn.setOpaque(true);
        
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(241, 245, 249)); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

    private void addCurrencyFormatting(JTextField field) {
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                String digits = getDigitsOnly(field.getText());
                if (!digits.isEmpty()) {
                    try { field.setText(vnNumberFormat.format(Long.parseLong(digits))); } 
                    catch (NumberFormatException ignored) {}
                }
            }
            @Override public void focusGained(FocusEvent e) {
                field.setText(getDigitsOnly(field.getText()));
                SwingUtilities.invokeLater(field::selectAll);
            }
        });
    }

    private void registerKeyboardActions() {
        JRootPane rootPane = getRootPane();
        rootPane.registerKeyboardAction(e -> handleSubmit(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rootPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void handleSubmit() {
        clearErrors();
        boolean valid = true;

        SanPham sanPham = (SanPham) cboSanPham.getSelectedItem();
        String maLo = txtMaLo.getText().trim().toUpperCase();
        String soLuongText = txtSoLuong.getText().trim();
        String giaNhapText = getDigitsOnly(txtGiaNhap.getText().trim());
        String hsd = txtHanSuDung.getText().trim();
        String trangThai = String.valueOf(cboTrangThai.getSelectedItem());

        if (sanPham == null) { errSanPham.setText("Vui lòng chọn sản phẩm"); valid = false; }
        if (maLo.isEmpty()) { errMaLo.setText("Mã lô không được để trống"); valid = false; }

        int soLuong = 0, giaNhap = 0;
        try {
            soLuong = Integer.parseInt(soLuongText);
            if (soLuong <= 0) { errSoLuong.setText("Số lượng > 0"); valid = false; }
        } catch (Exception e) { errSoLuong.setText("Bắt buộc nhập"); valid = false; }

        try {
            if(!giaNhapText.isEmpty()){
                giaNhap = Integer.parseInt(giaNhapText);
                if (giaNhap < 0) { errGiaNhap.setText("Giá không được âm"); valid = false; }
            }
        } catch (Exception e) { errGiaNhap.setText("Không hợp lệ"); valid = false; }

        if(hsd.isEmpty() || hsd.equals("dd/MM/yyyy")){
            errHanSuDung.setText("Vui lòng chọn hoặc nhập HSD"); valid = false;
        } else {
             try { LocalDate.parse(hsd, DATE_FORMAT); } 
             catch (DateTimeParseException e) {
                 errHanSuDung.setText("Định dạng dd/MM/yyyy"); valid = false;
             }
        }

        if (!valid) return;

        if (listener != null) listener.onSubmit(sanPham, maLo, soLuong, giaNhap, hsd, trangThai);
        dispose();
    }

    private void clearErrors() {
        errSanPham.setText(" "); errMaLo.setText(" "); errSoLuong.setText(" ");
        errGiaNhap.setText(" "); errHanSuDung.setText(" ");
    }

    private String getDigitsOnly(String text) { return text == null ? "" : text.replaceAll("\\D+", ""); }

    // =====================================================================================
    // ================= CLASS MINI DATE PICKER (TỰ CODE KHÔNG DÙNG THƯ VIỆN) =================
    // =====================================================================================
    private class CustomDatePicker extends JPopupMenu {
        private int month = LocalDate.now().getMonthValue();
        private int year = LocalDate.now().getYear();
        private final JLabel lblMonthYear;
        private final JPanel pnlDays;
        private final JTextField targetField;

        public CustomDatePicker(JTextField targetField) {
            this.targetField = targetField;
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(BORDER, 1));

            // Header (Tháng Năm + Nút Chuyển)
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(Color.WHITE);
            header.setBorder(new EmptyBorder(8, 8, 8, 8));

            JButton btnPrev = new JButton("<");
            styleNavButton(btnPrev);
            btnPrev.addActionListener(e -> changeMonth(-1));

            JButton btnNext = new JButton(">");
            styleNavButton(btnNext);
            btnNext.addActionListener(e -> changeMonth(1));

            lblMonthYear = new JLabel("", SwingConstants.CENTER);
            lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblMonthYear.setForeground(TEXT_PRIMARY);

            header.add(btnPrev, BorderLayout.WEST);
            header.add(lblMonthYear, BorderLayout.CENTER);
            header.add(btnNext, BorderLayout.EAST);

            // Body (Lưới ngày)
            pnlDays = new JPanel(new GridLayout(0, 7, 2, 2));
            pnlDays.setBackground(Color.WHITE);
            pnlDays.setBorder(new EmptyBorder(0, 8, 8, 8));

            add(header, BorderLayout.NORTH);
            add(pnlDays, BorderLayout.CENTER);

            updateCalendar();
        }

        private void styleNavButton(JButton btn) {
            btn.setFocusPainted(false); btn.setContentAreaFilled(false);
            btn.setBorder(new EmptyBorder(4, 8, 4, 8)); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14)); btn.setForeground(TEXT_SECONDARY);
        }

        private void changeMonth(int offset) {
            month += offset;
            if (month < 1) { month = 12; year--; } 
            else if (month > 12) { month = 1; year++; }
            updateCalendar();
        }

        private void updateCalendar() {
            pnlDays.removeAll();
            lblMonthYear.setText("Tháng " + month + ", " + year);

            String[] daysOfWeek = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
            for (String d : daysOfWeek) {
                JLabel lbl = new JLabel(d, SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(TEXT_SECONDARY);
                pnlDays.add(lbl);
            }

            YearMonth ym = YearMonth.of(year, month);
            int daysInMonth = ym.lengthOfMonth();
            int firstDayOfWeek = ym.atDay(1).getDayOfWeek().getValue() % 7; // Sunday = 0

            for (int i = 0; i < firstDayOfWeek; i++) {
                pnlDays.add(new JLabel("")); // Ô trống đầu tháng
            }

            for (int i = 1; i <= daysInMonth; i++) {
                int day = i;
                JButton btnDay = new JButton(String.valueOf(day));
                btnDay.setFocusPainted(false);
                btnDay.setBackground(Color.WHITE);
                btnDay.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
                btnDay.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                btnDay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                // Đánh dấu ngày hôm nay
                if (day == LocalDate.now().getDayOfMonth() && month == LocalDate.now().getMonthValue() && year == LocalDate.now().getYear()) {
                    btnDay.setForeground(PRIMARY);
                    btnDay.setFont(new Font("Segoe UI", Font.BOLD, 13));
                }

                btnDay.addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { btnDay.setBackground(new Color(239, 246, 255)); }
                    @Override public void mouseExited(MouseEvent e) { btnDay.setBackground(Color.WHITE); }
                });

                btnDay.addActionListener(e -> {
                    targetField.setText(String.format("%02d/%02d/%04d", day, month, year));
                    targetField.setForeground(TEXT_PRIMARY); // Reset màu nếu đang là hint
                    setVisible(false);
                });
                pnlDays.add(btnDay);
            }
            revalidate(); repaint();
        }
    }
    // =====================================================================================

    // --- CÁC CLASS HỖ TRỢ BÊN DƯỚI ---
    private static class DigitsOnlyFilter extends DocumentFilter {
        private final int maxLength;
        public DigitsOnlyFilter(int maxLength) { this.maxLength = maxLength; }
        @Override public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException { replace(fb, offset, 0, string, attr); }
        @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) return;
            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String next = current.substring(0, offset) + text + current.substring(offset + length);
            if (next.matches("\\d*") && next.length() <= maxLength) fb.replace(offset, length, text, attrs);
        }
    }

    private static class CurrencyDigitsFilter extends DocumentFilter {
        private final int maxDigits;
        public CurrencyDigitsFilter(int maxDigits) { this.maxDigits = maxDigits; }
        @Override public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException { replace(fb, offset, 0, string, attr); }
        @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) return;
            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String candidate = current.substring(0, offset) + text + current.substring(offset + length);
            String digits = candidate.replaceAll("\\D+", "");
            if (digits.length() <= maxDigits && candidate.matches("[\\d.,\\s]*")) fb.replace(offset, length, text, attrs);
        }
    }

    private static class HintTextField extends JTextField {
        private final String hint;
        public HintTextField(String hint) { this.hint = hint; setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12)); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(TEXT_HINT); g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = getInsets().left;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(hint, x, y); g2.dispose();
            }
        }
    }

    private static class ModernBorder extends AbstractBorder {
        private boolean focused = false;
        private final int radius = 8;
        public ModernBorder(JComponent comp) {}
        public void setFocused(boolean focused) { this.focused = focused; }
        @Override public Insets getBorderInsets(Component c) { return new Insets(8, 12, 8, 12); }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(focused ? BORDER_FOCUS : BORDER);
            g2.setStroke(new BasicStroke(focused ? 1.5f : 1.0f));
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, radius, radius);
            g2.dispose();
        }
    }

    private static class FlatComboBoxUI extends BasicComboBoxUI {
        @Override protected JButton createArrowButton() {
            JButton button = new JButton() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int size = 10, x = (getWidth() - size) / 2, y = (getHeight() - size / 2) / 2;
                    g2.setColor(TEXT_SECONDARY); g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D path = new Path2D.Double();
                    path.moveTo(x, y); path.lineTo(x + size / 2.0, y + size / 2.0); path.lineTo(x + size, y);
                    g2.draw(path); g2.dispose();
                }
            };
            button.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
            button.setContentAreaFilled(false); button.setFocusPainted(false); button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return button;
        }
        @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            g.setColor(Color.WHITE); g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }
}