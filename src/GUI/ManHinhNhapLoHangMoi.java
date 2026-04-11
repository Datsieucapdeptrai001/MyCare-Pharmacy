package GUI;

import DAO.DAO_SanPham;
import Entity.SanPham;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class ManHinhNhapLoHangMoi extends JDialog {

    public interface BatchSubmitListener {
        void onSubmit(SanPham sanPham, String maLo, int soLuong, int giaNhap, String hanSuDung, String trangThai);
    }

    private static final Color BG_APP = new Color(238, 243, 249);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PANEL_SOFT = new Color(245, 248, 252);

    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(71, 85, 105);
    private static final Color TEXT_HINT = new Color(148, 163, 184);

    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color PRIMARY_HOVER = new Color(29, 78, 216);
    private static final Color PRIMARY_SOFT = new Color(219, 234, 254);

    private static final Color SUCCESS = new Color(22, 163, 74);
    private static final Color WARNING = new Color(245, 158, 11);
    private static final Color DANGER = new Color(220, 38, 38);

    private static final Color BORDER = new Color(203, 213, 225);
    private static final Color BORDER_FOCUS = new Color(59, 130, 246);
    private static final Color INPUT_BG = new Color(248, 250, 252);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 21);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_TEXT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_SUB = new Font("Segoe UI", Font.PLAIN, 12);

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

    private JLabel errSanPham;
    private JLabel errMaLo;
    private JLabel errSoLuong;
    private JLabel errGiaNhap;
    private JLabel errHanSuDung;

    public ManHinhNhapLoHangMoi(Window owner, BatchSubmitListener listener) {
        super(owner, "Nhập lô hàng mới", ModalityType.APPLICATION_MODAL);
        this.listener = listener;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(660, 520);
        setMinimumSize(new Dimension(660, 520));
        setResizable(false);
        setLocationRelativeTo(owner);
        setContentPane(createMainUI());

        registerKeyboardActions();
    }

    private JPanel createMainUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_APP);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new ShadowBorder());

        card.add(createHeader(), BorderLayout.NORTH);
        card.add(createScrollArea(), BorderLayout.CENTER);
        card.add(createFooter(), BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        return root;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(CARD_BG);
        header.setBorder(new EmptyBorder(14, 16, 10, 16));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel badge = new JLabel("QUẢN LÝ KHO");
        badge.setOpaque(true);
        badge.setBackground(PRIMARY_SOFT);
        badge.setForeground(new Color(30, 64, 175));
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setBorder(new EmptyBorder(4, 8, 4, 8));
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Nhập lô hàng mới");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Form gọn, footer cố định, phần nhập liệu cuộn riêng.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tagPanel.setOpaque(false);
        tagPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagPanel.add(createMiniTag("Bắt buộc", DANGER));
        tagPanel.add(createMiniTag("dd/MM/yyyy", WARNING));
        tagPanel.add(createMiniTag("Nhập kho", SUCCESS));

        header.add(badge);
        header.add(Box.createVerticalStrut(6));
        header.add(title);
        header.add(Box.createVerticalStrut(3));
        header.add(sub);
        header.add(Box.createVerticalStrut(7));
        header.add(tagPanel);
        header.add(Box.createVerticalStrut(8));
        header.add(createDivider());

        return header;
    }

    private JScrollPane createScrollArea() {
        JPanel form = createFormPanel();

        JScrollPane scrollPane = new JScrollPane(
                form,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());

        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
        return scrollPane;
    }

    private JPanel createFormPanel() {
        JPanel form = new JPanel();
        form.setBackground(CARD_BG);
        form.setBorder(new EmptyBorder(12, 16, 10, 16));
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        cboSanPham = new JComboBox<>();
        styleSanPhamComboBox(cboSanPham);
        loadSanPhamToComboBox();

        txtMaLo = createTextField("Ví dụ: LO2026001");
        txtSoLuong = createTextField("Ví dụ: 100");
        txtGiaNhap = createTextField("Ví dụ: 250000");
        txtHanSuDung = createTextField("dd/MM/yyyy");
        txtHanSuDung.setText(LocalDate.now().plusMonths(6).format(DATE_FORMAT));

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

        form.add(createFieldBlock("Sản phẩm *", "Chọn sản phẩm từ danh sách", cboSanPham, errSanPham));
        form.add(createFieldBlock("Mã lô *", "Mã nhận diện của lô hàng", txtMaLo, errMaLo));
        form.add(createFieldBlock("Số lượng *", "Chỉ nhập số nguyên", txtSoLuong, errSoLuong));
        form.add(createFieldBlock("Giá nhập *", "Tự định dạng tiền Việt", txtGiaNhap, errGiaNhap));
        form.add(createFieldBlock("Hạn sử dụng *", "Định dạng dd/MM/yyyy", txtHanSuDung, errHanSuDung));
        form.add(createFieldBlock("Tình trạng", "Trạng thái kinh doanh", cboTrangThai, new JLabel(" ")));
        form.add(createInfoPanel());

        return form;
    }

    private void loadSanPhamToComboBox() {
        cboSanPham.removeAllItems();
        try {
            for (SanPham sp : daoSanPham.getDsThuoc()) {
                cboSanPham.addItem(sp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không tải được danh sách sản phẩm!");
        }
    }

    private void styleSanPhamComboBox(JComboBox<SanPham> combo) {
        combo.setPreferredSize(new Dimension(560, 38));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        combo.setFont(FONT_TEXT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBackground(INPUT_BG);
        combo.setFocusable(true);
        combo.setBorder(new ModernBorder(combo));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lb = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lb.setBorder(new EmptyBorder(6, 10, 6, 10));
                if (value instanceof SanPham sp) {
                    lb.setText(sp.getTen() + " (" + sp.getId() + ")");
                }
                return lb;
            }
        });
    }

    private JPanel createFieldBlock(String title, String helper, JComponent field, JLabel error) {
        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setBorder(new EmptyBorder(0, 0, 8, 0));
        block.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_LABEL);
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblHelper = new JLabel(helper);
        lblHelper.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHelper.setForeground(TEXT_SECONDARY);
        lblHelper.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        error.setAlignmentX(Component.LEFT_ALIGNMENT);

        block.add(lblTitle);
        block.add(Box.createVerticalStrut(2));
        block.add(lblHelper);
        block.add(Box.createVerticalStrut(4));
        block.add(field);
        block.add(Box.createVerticalStrut(2));
        block.add(error);

        return block;
    }

    private JPanel createInfoPanel() {
        JPanel info = new JPanel(new BorderLayout(8, 0));
        info.setBackground(PANEL_SOFT);
        info.setBorder(new EmptyBorder(10, 12, 10, 12));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel icon = new JLabel("ℹ");
        icon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14));
        icon.setForeground(PRIMARY);

        JLabel text = new JLabel("Kéo thanh dọc để xem thêm nếu nội dung dài.");
        text.setFont(FONT_SUB);
        text.setForeground(TEXT_SECONDARY);

        info.add(icon, BorderLayout.WEST);
        info.add(text, BorderLayout.CENTER);
        return info;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(CARD_BG);
        footer.setBorder(new EmptyBorder(6, 16, 12, 16));

        footer.add(createDivider(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(8, 0, 0, 0));

        JLabel note = new JLabel("Các trường có dấu * là bắt buộc");
        note.setFont(FONT_SUB);
        note.setForeground(TEXT_SECONDARY);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton btnCancel = createSecondaryButton("Hủy");
        JButton btnReset = createWarningButton("Làm mới");
        JButton btnSubmit = createPrimaryButton("Nhập lô hàng");

        btnCancel.addActionListener(e -> dispose());
        btnReset.addActionListener(e -> resetForm());
        btnSubmit.addActionListener(e -> handleSubmit());

        actions.add(btnCancel);
        actions.add(btnReset);
        actions.add(btnSubmit);

        content.add(note, BorderLayout.WEST);
        content.add(actions, BorderLayout.EAST);

        footer.add(content, BorderLayout.CENTER);
        return footer;
    }

    private JPanel createMiniTag(String text, Color accent) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        p.setOpaque(true);
        p.setBackground(mix(accent, Color.WHITE, 0.87f));
        p.setBorder(new EmptyBorder(4, 7, 4, 7));

        JLabel dot = new JLabel("●");
        dot.setForeground(accent);
        dot.setFont(new Font("Segoe UI Symbol", Font.BOLD, 8));

        JLabel lb = new JLabel(text);
        lb.setForeground(accent.darker());
        lb.setFont(new Font("Segoe UI", Font.BOLD, 10));

        p.add(dot);
        p.add(lb);
        return p;
    }

    private JPanel createDivider() {
        JPanel divider = new JPanel();
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setPreferredSize(new Dimension(10, 1));
        divider.setBackground(new Color(226, 232, 240));
        return divider;
    }

    private HintTextField createTextField(String hint) {
        HintTextField field = new HintTextField(hint);
        field.setPreferredSize(new Dimension(560, 38));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setFont(FONT_TEXT);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(INPUT_BG);
        field.setBorder(new ModernBorder(field));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setPreferredSize(new Dimension(560, 38));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        combo.setFont(FONT_TEXT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBackground(INPUT_BG);
        combo.setFocusable(true);
        combo.setBorder(new ModernBorder(combo));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lb = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lb.setBorder(new EmptyBorder(6, 10, 6, 10));
                return lb;
            }
        });
    }

    private JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        label.setForeground(DANGER);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = createButtonBase(text, Color.WHITE, PRIMARY);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(PRIMARY_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(PRIMARY);
            }
        });
        return btn;
    }

    private JButton createSecondaryButton(String text) {
        JButton btn = createButtonBase(text, TEXT_PRIMARY, Color.WHITE);
        btn.setBorder(new RoundedLineBorder(BORDER, 1, 12));
        return btn;
    }

    private JButton createWarningButton(String text) {
        JButton btn = createButtonBase(text, new Color(146, 64, 14), new Color(254, 243, 199));
        btn.setBorder(new RoundedLineBorder(new Color(251, 191, 36), 1, 12));
        return btn;
    }

    private JButton createButtonBase(String text, Color fg, Color bg) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 16, 9, 16));
        btn.setOpaque(true);
        return btn;
    }

    private void addCurrencyFormatting(JTextField field) {
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String digits = getDigitsOnly(field.getText());
                if (!digits.isEmpty()) {
                    try {
                        long value = Long.parseLong(digits);
                        field.setText(vnNumberFormat.format(value));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                String digits = getDigitsOnly(field.getText());
                field.setText(digits);
                SwingUtilities.invokeLater(field::selectAll);
            }
        });
    }

    private void registerKeyboardActions() {
        JRootPane rootPane = getRootPane();

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

        rootPane.registerKeyboardAction(e -> handleSubmit(), enter, JComponent.WHEN_IN_FOCUSED_WINDOW);
        rootPane.registerKeyboardAction(e -> dispose(), esc, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void resetForm() {
        if (cboSanPham.getItemCount() > 0) {
            cboSanPham.setSelectedIndex(0);
        }
        txtMaLo.setText("");
        txtSoLuong.setText("");
        txtGiaNhap.setText("");
        txtHanSuDung.setText(LocalDate.now().plusMonths(6).format(DATE_FORMAT));
        cboTrangThai.setSelectedIndex(0);
        clearErrors();
        cboSanPham.requestFocusInWindow();
    }

    private void handleSubmit() {
        clearErrors();

        SanPham sanPham = (SanPham) cboSanPham.getSelectedItem();
        String maLo = txtMaLo.getText().trim().toUpperCase();
        String soLuongText = txtSoLuong.getText().trim();
        String giaNhapText = getDigitsOnly(txtGiaNhap.getText().trim());
        String hsd = txtHanSuDung.getText().trim();
        String trangThai = String.valueOf(cboTrangThai.getSelectedItem());

        boolean valid = true;

        if (sanPham == null) {
            errSanPham.setText("Vui lòng chọn sản phẩm");
            valid = false;
        }

        if (maLo.isEmpty()) {
            errMaLo.setText("Mã lô không được để trống");
            valid = false;
        }

        int soLuong = 0;
        try {
            soLuong = Integer.parseInt(soLuongText);
            if (soLuong <= 0) {
                errSoLuong.setText("Số lượng phải lớn hơn 0");
                valid = false;
            }
        } catch (Exception e) {
            errSoLuong.setText("Số lượng phải là số nguyên");
            valid = false;
        }

        int giaNhap = 0;
        try {
            giaNhap = Integer.parseInt(giaNhapText);
            if (giaNhap < 0) {
                errGiaNhap.setText("Giá nhập không được âm");
                valid = false;
            }
        } catch (Exception e) {
            errGiaNhap.setText("Giá nhập phải là số hợp lệ");
            valid = false;
        }

        try {
            LocalDate.parse(hsd, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            errHanSuDung.setText("Ngày phải đúng định dạng dd/MM/yyyy");
            valid = false;
        }

        if (!valid) return;

        if (listener != null) {
            listener.onSubmit(sanPham, maLo, soLuong, giaNhap, hsd, trangThai);
        }

        JOptionPane.showMessageDialog(
                this,
                "Nhập lô hàng thành công!",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE
        );
        dispose();
    }

    private void clearErrors() {
        errSanPham.setText(" ");
        errMaLo.setText(" ");
        errSoLuong.setText(" ");
        errGiaNhap.setText(" ");
        errHanSuDung.setText(" ");
    }

    private String getDigitsOnly(String text) {
        return text == null ? "" : text.replaceAll("\\D+", "");
    }

    private static Color mix(Color c1, Color c2, float ratio) {
        float r = Math.max(0f, Math.min(1f, ratio));
        int red = (int) (c1.getRed() * (1 - r) + c2.getRed() * r);
        int green = (int) (c1.getGreen() * (1 - r) + c2.getGreen() * r);
        int blue = (int) (c1.getBlue() * (1 - r) + c2.getBlue() * r);
        return new Color(red, green, blue);
    }

    private static class DigitsOnlyFilter extends DocumentFilter {
        private final int maxLength;

        public DigitsOnlyFilter(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            replace(fb, offset, 0, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) return;

            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String next = current.substring(0, offset) + text + current.substring(offset + length);

            if (next.matches("\\d*") && next.length() <= maxLength) {
                fb.replace(offset, length, text, attrs);
            }
        }
    }

    private static class CurrencyDigitsFilter extends DocumentFilter {
        private final int maxDigits;

        public CurrencyDigitsFilter(int maxDigits) {
            this.maxDigits = maxDigits;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            replace(fb, offset, 0, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) return;

            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String candidate = current.substring(0, offset) + text + current.substring(offset + length);
            String digits = candidate.replaceAll("\\D+", "");

            if (digits.length() <= maxDigits && candidate.matches("[\\d.,\\s]*")) {
                fb.replace(offset, length, text, attrs);
            }
        }
    }

    private static class HintTextField extends JTextField {
        private final String hint;

        public HintTextField(String hint) {
            this.hint = hint;
            setMargin(new Insets(0, 10, 0, 10));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(TEXT_HINT);
                g2.setFont(getFont());

                FontMetrics fm = g2.getFontMetrics();
                int x = getInsets().left + 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

                g2.drawString(hint, x, y);
                g2.dispose();
            }
        }
    }

    private static class ModernBorder extends AbstractBorder {
        private final JComponent comp;
        private final int radius = 14;

        public ModernBorder(JComponent comp) {
            this.comp = comp;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 10, 8, 10);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = 10;
            insets.right = 10;
            insets.top = 8;
            insets.bottom = 8;
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean focused = comp.isFocusOwner();

            if (focused) {
                g2.setColor(new Color(59, 130, 246, 24));
                g2.fillRoundRect(x, y, width - 1, height - 1, radius, radius);
            }

            g2.setColor(focused ? BORDER_FOCUS : BORDER);
            g2.setStroke(new BasicStroke(focused ? 1.6f : 1f));
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, radius, radius);

            g2.dispose();
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
            return new Insets(7, 11, 7, 11);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = 11;
            insets.right = 11;
            insets.top = 7;
            insets.bottom = 7;
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);

            for (int i = 0; i < thickness; i++) {
                g2.drawRoundRect(x + i, y + i, width - 1 - i * 2, height - 1 - i * 2, radius, radius);
            }
            g2.dispose();
        }
    }

    private static class ShadowBorder extends AbstractBorder {
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 8, 10, 8);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.top = 8;
            insets.left = 8;
            insets.bottom = 10;
            insets.right = 8;
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (int i = 0; i < 7; i++) {
                g2.setColor(new Color(15, 23, 42, Math.max(3, 16 - i * 2)));
                g2.drawRoundRect(x + 2, y + 2 + i, width - 5, height - 5 - i, 22, 22);
            }

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x, y, width - 4, height - 4, 22, 22);

            g2.dispose();
        }
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(100, 116, 139);
            trackColor = new Color(241, 245, 249);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return zeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return zeroButton();
        }

        private JButton zeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(thumbColor);
            g2.fillRoundRect(
                    thumbBounds.x + 2,
                    thumbBounds.y + 2,
                    thumbBounds.width - 4,
                    thumbBounds.height - 4,
                    8, 8
            );
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(trackColor);
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }
    }
}