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
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class ManHinhNhapLoHangMoi extends JDialog {

    public interface BatchSubmitListener {
        void onSubmit(SanPham sanPham, String maLo, int soLuong, int giaNhap, String hanSuDung, String trangThai);
    }

    // --- MÀU SẮC CHUẨN THEO THIẾT KẾ ---
    private static final Color BG_APP = new Color(243, 244, 246);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color HEADER_BG = new Color(29, 58, 95); // Xanh navy đậm

    private static final Color TEXT_PRIMARY = new Color(55, 65, 81);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color TEXT_HINT = new Color(156, 163, 175);

    private static final Color PRIMARY = new Color(24, 104, 180); // Xanh dương nút bấm
    private static final Color PRIMARY_HOVER = new Color(20, 85, 150);
    private static final Color BORDER = new Color(209, 213, 219); // Xám nhạt
    private static final Color BORDER_FOCUS = new Color(59, 130, 246);
    private static final Color INPUT_BG = Color.WHITE;
    private static final Color DANGER = new Color(220, 38, 38);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
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

    private JLabel errSanPham;
    private JLabel errMaLo;
    private JLabel errSoLuong;
    private JLabel errGiaNhap;
    private JLabel errHanSuDung;

    public ManHinhNhapLoHangMoi(Window owner, BatchSubmitListener listener) {
        super(owner, "Nhập lô hàng mới", ModalityType.APPLICATION_MODAL);
        this.listener = listener;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(580, 520);
        setMinimumSize(new Dimension(580, 520));
        setResizable(false);
        setLocationRelativeTo(owner);
        setContentPane(createMainUI());
        
        // Loại bỏ viền xanh mặc định của Windows
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        setUndecorated(true); // Bỏ thanh title mặc định để custom hoàn toàn

        registerKeyboardActions();
    }

    private JPanel createMainUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_APP);
        // Shadow giả lập
        root.setBorder(BorderFactory.createCompoundBorder(
                new ShadowBorder(),
                BorderFactory.createLineBorder(new Color(220, 220, 220))
        ));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);

        card.add(createHeader(), BorderLayout.NORTH);
        card.add(createBody(), BorderLayout.CENTER);
        card.add(createFooter(), BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        return root;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(new EmptyBorder(14, 20, 14, 16));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        // Icon vẽ tay (Xếp chồng)
        JComponent icon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                // Vẽ 3 lớp hình thoi xếp chồng
                int w = 16, h = 14;
                int y = 2;
                Path2D p1 = new Path2D.Double();
                p1.moveTo(0, y+4); p1.lineTo(w/2, y); p1.lineTo(w, y+4); p1.lineTo(w/2, y+8); p1.closePath();
                g2.draw(p1);
                
                Path2D p2 = new Path2D.Double();
                p2.moveTo(0, y+8); p2.lineTo(w/2, y+12); p2.lineTo(w, y+8);
                g2.draw(p2);
                
                Path2D p3 = new Path2D.Double();
                p3.moveTo(0, y+12); p3.lineTo(w/2, y+16); p3.lineTo(w, y+12);
                g2.draw(p3);
                
                g2.dispose();
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(16, 20);
            }
        };

        JLabel title = new JLabel("Nhập lô hàng mới");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);

        left.add(icon);
        left.add(title);

        // Nút close
        JButton btnClose = new JButton("✕");
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setForeground(new Color(200, 210, 220));
        btnClose.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 16));
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnClose.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e) { btnClose.setForeground(new Color(200, 210, 220)); }
        });
        btnClose.addActionListener(e -> dispose());
        
        // Cho phép kéo thả form (vì đã setUndecorated)
        MouseAdapter ma = new MouseAdapter() {
            int x, y;
            @Override public void mousePressed(MouseEvent e) { x = e.getX(); y = e.getY(); }
            @Override public void mouseDragged(MouseEvent e) {
                setLocation(getLocation().x + e.getX() - x, getLocation().y + e.getY() - y);
            }
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
        body.setBorder(new EmptyBorder(20, 24, 10, 24)); // Padding rộng chuẩn form

        cboSanPham = new JComboBox<>();
        styleComboBox(cboSanPham);
        loadSanPhamToComboBox();

        txtMaLo = createTextField("VD: LOT-2024-001");
        txtSoLuong = createTextField("0");
        txtGiaNhap = createTextField("0");
        txtHanSuDung = createTextField("mm/dd/yyyy");

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
        gbc.insets = new Insets(0, 0, 16, 0); // Khoảng cách giữa các hàng

        // Layout từng dòng
        gbc.gridy = 0;
        body.add(createFullWidthField("Sản phẩm *", cboSanPham, errSanPham), gbc);

        gbc.gridy = 1;
        body.add(createTwoColumnRow("Mã lô *", txtMaLo, errMaLo, "Tình trạng", cboTrangThai, createErrorLabel()), gbc);

        gbc.gridy = 2;
        body.add(createTwoColumnRow("Số lượng *", txtSoLuong, errSoLuong, "Giá nhập (đ/ĐV)", txtGiaNhap, errGiaNhap), gbc);

        gbc.gridy = 3;
        body.add(createFullWidthField("Hạn sử dụng *", txtHanSuDung, errHanSuDung), gbc);
        
        gbc.gridy = 4; gbc.weighty = 1.0;
        body.add(Box.createVerticalGlue(), gbc);

        return body;
    }

    private JPanel createFullWidthField(String title, JComponent field, JLabel error) {
        JPanel block = new JPanel(new BorderLayout(0, 6)); 
        block.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_LABEL);
        lblTitle.setForeground(TEXT_SECONDARY);

        JPanel fieldWrapper = new JPanel(new BorderLayout(0, 2));
        fieldWrapper.setOpaque(false);
        fieldWrapper.add(field, BorderLayout.CENTER);
        fieldWrapper.add(error, BorderLayout.SOUTH);

        block.add(lblTitle, BorderLayout.NORTH);
        block.add(fieldWrapper, BorderLayout.CENTER);

        return block;
    }

    private JPanel createTwoColumnRow(String title1, JComponent field1, JLabel error1,
                                      String title2, JComponent field2, JLabel error2) {
        JPanel rowPanel = new JPanel(new GridLayout(1, 2, 20, 0)); // Khoảng cách 2 cột 20px
        rowPanel.setOpaque(false);
        rowPanel.add(createFullWidthField(title1, field1, error1));
        rowPanel.add(createFullWidthField(title2, field2, error2));
        return rowPanel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 235, 240)), // Đường kẻ mờ
                new EmptyBorder(16, 24, 16, 24)
        ));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);

        JButton btnCancel = createSecondaryButton("Hủy");
        JButton btnSubmit = createPrimaryButton("+ Thêm lô hàng");

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HintTextField createTextField(String hint) {
        HintTextField field = new HintTextField(hint);
        field.setPreferredSize(new Dimension(100, 36)); // Chiều cao chuẩn web 36px
        field.setFont(FONT_TEXT);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(INPUT_BG);
        field.setBorder(new ModernBorder(field));
        
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { field.repaint(); }
            @Override public void focusLost(FocusEvent e) { field.repaint(); }
        });
        return field;
    }

    // --- CUSTOM COMBOBOX XỊN ---
    private void styleComboBox(JComboBox<?> combo) {
        combo.setPreferredSize(new Dimension(100, 36)); 
        combo.setFont(FONT_TEXT);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBackground(INPUT_BG);
        combo.setFocusable(true);
        combo.setBorder(new ModernBorder(combo));
        
        // Gán giao diện phẳng cho combobox
        combo.setUI(new FlatComboBoxUI());
        
        combo.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { combo.repaint(); }
            @Override public void focusLost(FocusEvent e) { combo.repaint(); }
        });

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lb = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lb.setBorder(new EmptyBorder(6, 12, 6, 12)); // Khoảng cách text bên trong list
                if (isSelected) {
                    lb.setBackground(new Color(243, 244, 246));
                    lb.setForeground(TEXT_PRIMARY);
                } else {
                    lb.setBackground(Color.WHITE);
                }
                if (value instanceof SanPham sp) {
                    lb.setText(sp.getTen() + " (" + sp.getId() + ")");
                }
                return lb;
            }
        });
    }

    private JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11)); 
        label.setForeground(DANGER);
        return label;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 20, 9, 20)); // Nút dầy dặn
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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new RoundedLineBorder(BORDER, 1, 6)); // Viền bo góc xám
        btn.setOpaque(true);
        btn.setMargin(new Insets(9, 20, 9, 20));
        
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(249, 250, 251)); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(Color.WHITE); }
        });
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
                    } catch (NumberFormatException ignored) {}
                }
                field.repaint();
            }

            @Override
            public void focusGained(FocusEvent e) {
                String digits = getDigitsOnly(field.getText());
                field.setText(digits);
                SwingUtilities.invokeLater(field::selectAll);
                field.repaint();
            }
        });
    }

    private void registerKeyboardActions() {
        JRootPane rootPane = getRootPane();
        rootPane.registerKeyboardAction(e -> handleSubmit(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        rootPane.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
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
            errSoLuong.setText("Số lượng hợp lệ bắt buộc nhập");
            valid = false;
        }

        int giaNhap = 0;
        try {
            if(!giaNhapText.isEmpty()){
                giaNhap = Integer.parseInt(giaNhapText);
                if (giaNhap < 0) {
                    errGiaNhap.setText("Giá nhập không được âm");
                    valid = false;
                }
            }
        } catch (Exception e) {
            errGiaNhap.setText("Giá nhập không hợp lệ");
            valid = false;
        }

        if(hsd.isEmpty()){
            errHanSuDung.setText("Hạn sử dụng không được trống");
            valid = false;
        } else {
             try {
                // Xử lý MM/dd/yyyy hoặc dd/MM/yyyy linh hoạt tùy chuẩn của bạn
                LocalDate.parse(hsd, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            } catch (DateTimeParseException e) {
                try{
                     LocalDate.parse(hsd, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }catch(Exception ex){
                     errHanSuDung.setText("Vui lòng nhập đúng định dạng ngày");
                     valid = false;
                }
            }
        }

        if (!valid) return;

        if (listener != null) {
            listener.onSubmit(sanPham, maLo, soLuong, giaNhap, hsd, trangThai);
        }

        JOptionPane.showMessageDialog(this, "Nhập lô hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
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

    // --- CÁC CLASS HỖ TRỢ BÊN DƯỚI ---

    private static class DigitsOnlyFilter extends DocumentFilter {
        private final int maxLength;
        public DigitsOnlyFilter(int maxLength) { this.maxLength = maxLength; }
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
        public CurrencyDigitsFilter(int maxDigits) { this.maxDigits = maxDigits; }
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
            setMargin(new Insets(0, 12, 0, 12));
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
                int x = getInsets().left;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(hint, x, y);
                g2.dispose();
            }
        }
    }

    // Border nét mảnh, bo tròn nhẹ
    private static class ModernBorder extends AbstractBorder {
        private final JComponent comp;
        private final int radius = 6;

        public ModernBorder(JComponent comp) { this.comp = comp; }

        @Override
        public Insets getBorderInsets(Component c) { return new Insets(6, 12, 6, 12); }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = 12; insets.right = 12; insets.top = 6; insets.bottom = 6;
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean focused = comp.isFocusOwner();
            g2.setColor(focused ? BORDER_FOCUS : BORDER);
            g2.setStroke(new BasicStroke(focused ? 1.5f : 1.0f));
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, radius, radius);
            g2.dispose();
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
        public Insets getBorderInsets(Component c) { return new Insets(6, 12, 6, 12); }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = 12; insets.right = 12; insets.top = 6; insets.bottom = 6;
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }

    private static class ShadowBorder extends AbstractBorder {
        @Override
        public Insets getBorderInsets(Component c) { return new Insets(2, 2, 4, 2); }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 0, 0, 10)); // Bóng siêu mờ
            g2.fillRect(x, y + height - 2, width, 2);
            g2.dispose();
        }
    }

    // --- FLAT COMBOBOX UI (Thay thế nút Dropdown cũ bằng icon Chevron) ---
    private static class FlatComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton button = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int size = 10;
                    int x = (getWidth() - size) / 2;
                    int y = (getHeight() - size / 2) / 2;

                    g2.setColor(TEXT_PRIMARY);
                    g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    
                    // Vẽ hình chữ V (Chevron Down)
                    Path2D path = new Path2D.Double();
                    path.moveTo(x, y);
                    path.lineTo(x + size / 2.0, y + size / 2.0);
                    path.lineTo(x + size, y);
                    g2.draw(path);
                    
                    g2.dispose();
                }
            };
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return button;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            // Ngăn Swing tự vẽ màu background khi chọn
            g.setColor(INPUT_BG);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }
}