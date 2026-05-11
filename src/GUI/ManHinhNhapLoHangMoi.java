package GUI;

import BUS.BUS_Kho;
import BUS.BUS_SanPham;
import Entity.KhoHang;
import Entity.LoHang;
import Entity.SanPham;
import Enumeration.TrangThaiLoHang;
import Utils.MenuIcon;
import Utils.ThongBao;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManHinhNhapLoHangMoi extends JDialog {

    public interface ReloadListener {
        void onReload();
    }

    private static final Color BG_TRANSPARENT = new Color(0, 0, 0, 0);
    private static final Color CARD_BG = Color.WHITE;
    
    private static final Color HEADER_BG = new Color(14, 116, 144); 
    private static final Color FOOTER_BG = new Color(248, 250, 252); 
    
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42); 
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color TEXT_HINT = new Color(148, 163, 184);

    private static final Color PRIMARY = new Color(14, 116, 144); 
    private static final Color PRIMARY_HOVER = new Color(22, 133, 163);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color BORDER_FOCUS = new Color(14, 116, 144); 
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color SUCCESS = new Color(34, 197, 94); 
    private static final Color SUCCESS_HOVER = new Color(22, 163, 74);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_TEXT = new Font("Segoe UI", Font.PLAIN, 15);

    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat vnNumberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    private final BUS_SanPham busSanPham = new BUS_SanPham();
    private final BUS_Kho busKho = new BUS_Kho();

    private SuggestionField txtTimSanPham;
    private SanPham selectedSanPham = null;
    private List<SanPham> dsTatCaSanPham = new ArrayList<>();
    
    private HintTextField txtMaLo;
    private HintTextField txtSoLuong;
    private HintTextField txtGiaNhap;
    private HintTextField txtHanSuDung;

    private JLabel errSanPham;
    private JLabel errMaLo;
    private JLabel errSoLuong;
    private JLabel errGiaNhap;
    private JLabel errHanSuDung;

    private final ReloadListener reloadListener;

    public ManHinhNhapLoHangMoi(Window owner, ReloadListener reloadListener) {
        super(owner, "Thêm lô hàng", ModalityType.APPLICATION_MODAL);
        this.reloadListener = reloadListener;

        setUndecorated(true);
        setBackground(BG_TRANSPARENT);
        setSize(580, 620); 
        setLocationRelativeTo(owner);

        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16)); 
        setContentPane(createMainUI());
        registerKeyboardActions();
    }

    public void setSanPhamAutoFill(String tenSP) {
        if (txtTimSanPham != null) {
            txtTimSanPham.getTextField().setText(tenSP);
            txtTimSanPham.getTextField().requestFocus();
            txtTimSanPham.getTextField().setCaretPosition(txtTimSanPham.getTextField().getText().length());
        }
    }

    private JPanel createMainUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createLineBorder(PRIMARY, 2));

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createBody(), BorderLayout.CENTER);
        root.add(createFooter(), BorderLayout.SOUTH);

        return root;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(8, 80, 100)),
                new EmptyBorder(16, 24, 16, 20)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel lblIcon = new JLabel(new MenuIcon("ADD"));
        lblIcon.setForeground(Color.WHITE);

        JLabel title = new JLabel("Thêm Lô Hàng");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);

        left.add(lblIcon);
        left.add(title);

        JButton btnClose = new JButton();
        btnClose.setIcon(new MenuIcon("CLOSE"));
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setForeground(new Color(255, 255, 255, 180));
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnClose.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e) { btnClose.setForeground(new Color(255, 255, 255, 180)); }
        });
        btnClose.addActionListener(e -> dispose());

        MouseAdapter dragWindow = new MouseAdapter() {
            int x, y;
            @Override public void mousePressed(MouseEvent e) { x = e.getX(); y = e.getY(); }
            @Override public void mouseDragged(MouseEvent e) { setLocation(getLocation().x + e.getX() - x, getLocation().y + e.getY() - y); }
        };

        header.addMouseListener(dragWindow);
        header.addMouseMotionListener(dragWindow);

        header.add(left, BorderLayout.WEST);
        header.add(btnClose, BorderLayout.EAST);

        return header;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(CARD_BG);
        body.setBorder(new EmptyBorder(24, 32, 10, 32));

        // ĐÃ KHẮC PHỤC: Lấy danh sách sản phẩm đổ vào String List cho SuggestionField giống ManHinhKhuyenMai
        List<String> khoSanPham = new ArrayList<>();
        try {
            dsTatCaSanPham = busSanPham.getDsThuoc();
            for (SanPham sp : dsTatCaSanPham) {
                khoSanPham.add(sp.getTen() + " (" + sp.getId() + ")");
            }
        } catch (Exception e) {
            // Ignored
        }

        txtTimSanPham = new SuggestionField("Sản phẩm (Bắt buộc chọn từ danh sách) *", "Nhập tên, mã SP...", khoSanPham, "SEARCH");

        txtMaLo = createTextField("VD: LOT-2026-001");
        txtSoLuong = createTextField("0");
        txtGiaNhap = createTextField("0");

        ((AbstractDocument) txtSoLuong.getDocument()).setDocumentFilter(new DigitsOnlyFilter(9));
        ((AbstractDocument) txtGiaNhap.getDocument()).setDocumentFilter(new CurrencyDigitsFilter(12));
        addCurrencyFormatting(txtGiaNhap);

        errSanPham = createErrorLabel();
        errMaLo = createErrorLabel();
        errSoLuong = createErrorLabel();
        errGiaNhap = createErrorLabel();
        errHanSuDung = createErrorLabel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 16, 0); 

        gbc.gridy = 0;
        // Bọc lỗi cho SuggestionField
        JPanel spWrapper = new JPanel(new BorderLayout());
        spWrapper.setOpaque(false);
        spWrapper.add(txtTimSanPham, BorderLayout.CENTER);
        spWrapper.add(errSanPham, BorderLayout.SOUTH);
        body.add(spWrapper, gbc);

        gbc.gridy = 1;
        body.add(createFullWidthField("Mã lô *", txtMaLo, errMaLo, "DOCUMENT"), gbc);

        gbc.gridy = 2;
        body.add(createTwoColumnRow("Số lượng *", txtSoLuong, errSoLuong, "BOX", 
                                    "Giá nhập (đ) *", txtGiaNhap, errGiaNhap, "TAB_DOLLAR"), gbc);

        gbc.gridy = 3;
        JPanel dateFieldWrapper = createDatePickerField();
        body.add(createFullWidthField("Hạn sử dụng (dd/MM/yyyy) *", dateFieldWrapper, errHanSuDung, "TIME"), gbc);

        gbc.gridy = 4;
        gbc.weighty = 1.0;
        body.add(Box.createVerticalGlue(), gbc);

        return body;
    }

    private JPanel createDatePickerField() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Color.WHITE);

        ModernBorder sharedBorder = new ModernBorder();
        wrap.setBorder(sharedBorder);

        txtHanSuDung = new HintTextField("dd/MM/yyyy");
        txtHanSuDung.setFont(FONT_TEXT);
        txtHanSuDung.setForeground(TEXT_PRIMARY);
        txtHanSuDung.setBorder(null);

        JButton btnCal = new JButton();
        btnCal.setIcon(new MenuIcon("CALENDAR"));
        btnCal.setFocusPainted(false);
        btnCal.setContentAreaFilled(false);
        btnCal.setBorder(new EmptyBorder(0, 12, 0, 12));
        btnCal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCal.setForeground(PRIMARY);

        CustomDatePicker datePickerPopup = new CustomDatePicker(this, txtHanSuDung);
        btnCal.addActionListener(e -> datePickerPopup.showPopup(wrap));

        txtHanSuDung.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { sharedBorder.setFocused(true); wrap.repaint(); }
            @Override public void focusLost(FocusEvent e) { sharedBorder.setFocused(false); wrap.repaint(); }
        });

        wrap.add(txtHanSuDung, BorderLayout.CENTER);
        wrap.add(btnCal, BorderLayout.EAST);

        return wrap;
    }

    private JPanel createFullWidthField(String title, JComponent field, JLabel error, String iconName) {
        JPanel block = new JPanel(new BorderLayout(0, 8));
        block.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        titlePanel.setOpaque(false);

        if (iconName != null && !iconName.isEmpty()) {
            JLabel lblIcon = new JLabel(new MenuIcon(iconName));
            lblIcon.setForeground(PRIMARY); 
            titlePanel.add(lblIcon);
        }

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_LABEL);
        lblTitle.setForeground(TEXT_PRIMARY);
        titlePanel.add(lblTitle);

        JPanel fieldWrapper = new JPanel(new BorderLayout(0, 4));
        fieldWrapper.setOpaque(false);
        fieldWrapper.add(field, BorderLayout.CENTER);
        fieldWrapper.add(error, BorderLayout.SOUTH);

        block.add(titlePanel, BorderLayout.NORTH);
        block.add(fieldWrapper, BorderLayout.CENTER);

        return block;
    }

    private JPanel createTwoColumnRow(String title1, JComponent field1, JLabel error1, String icon1,
                                      String title2, JComponent field2, JLabel error2, String icon2) {
        JPanel rowPanel = new JPanel(new GridLayout(1, 2, 24, 0));
        rowPanel.setOpaque(false);

        rowPanel.add(createFullWidthField(title1, field1, error1, icon1));
        rowPanel.add(createFullWidthField(title2, field2, error2, icon2));

        return rowPanel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(FOOTER_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);

        JButton btnCancel = createSecondaryButton("Hủy");
        btnCancel.setIcon(new MenuIcon("CANCEL"));
        btnCancel.setIconTextGap(8); 

        JButton btnSubmit = createPrimaryButton("Lưu lô hàng");
        btnSubmit.setIcon(new MenuIcon("SAVE"));
        btnSubmit.setIconTextGap(8); 

        btnCancel.addActionListener(e -> dispose());
        btnSubmit.addActionListener(e -> handleSubmit());

        actions.add(btnCancel);
        actions.add(btnSubmit);

        footer.add(actions, BorderLayout.EAST);
        return footer;
    }

    private HintTextField createTextField(String hint) {
        HintTextField field = new HintTextField(hint);
        field.setPreferredSize(new Dimension(100, 44)); 
        field.setFont(FONT_TEXT);
        field.setForeground(TEXT_PRIMARY);

        ModernBorder border = new ModernBorder();
        field.setBorder(border);

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { border.setFocused(true); field.repaint(); }
            @Override public void focusLost(FocusEvent e) { border.setFocused(false); field.repaint(); }
        });

        return field;
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
        btn.setBackground(SUCCESS); 
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 24, 10, 24));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(SUCCESS_HOVER); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(SUCCESS); }
        });

        return btn;
    }

    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setForeground(TEXT_SECONDARY); 
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
            @Override
            public void focusLost(FocusEvent e) {
                String digits = getDigitsOnly(field.getText());
                if (!digits.isEmpty()) {
                    try { field.setText(vnNumberFormat.format(Long.parseLong(digits))); } 
                    catch (NumberFormatException ignored) {}
                }
            }
            @Override
            public void focusGained(FocusEvent e) {
                field.setText(getDigitsOnly(field.getText()));
                SwingUtilities.invokeLater(field::selectAll);
            }
        });
    }

    private void registerKeyboardActions() {
        JRootPane rootPane = getRootPane();
        rootPane.registerKeyboardAction(
                e -> handleSubmit(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        rootPane.registerKeyboardAction(
                e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void handleSubmit() {
        clearErrors();

        String spText = txtTimSanPham.getTextField().getText().trim();
        String maLo = txtMaLo.getText().trim().toUpperCase();
        String soLuongText = txtSoLuong.getText().trim();
        String giaNhapText = getDigitsOnly(txtGiaNhap.getText().trim());
        String hanSuDung = txtHanSuDung.getText().trim();

        boolean valid = true;

        if (spText.isEmpty() || spText.equals("Nhập tên, mã SP...")) { 
            errSanPham.setText("Vui lòng chọn sản phẩm từ danh sách!"); 
            valid = false; 
        } else {
            selectedSanPham = null;
            for (SanPham sp : dsTatCaSanPham) {
                String displayText = sp.getTen() + " (" + sp.getId() + ")";
                if (spText.equalsIgnoreCase(displayText) || spText.equalsIgnoreCase(sp.getTen()) || spText.equalsIgnoreCase(sp.getId())) {
                    selectedSanPham = sp;
                    break;
                }
            }
            if (selectedSanPham == null || selectedSanPham.getId() == null || selectedSanPham.getId().isEmpty()) {
                errSanPham.setText("Vui lòng chọn 1 sản phẩm hợp lệ từ danh sách gợi ý!");
                valid = false;
            }
        }

        if (maLo.isEmpty()) { errMaLo.setText("Mã lô không được để trống"); valid = false; }

        int soLuong = 0;
        int giaNhap = 0;

        try {
            soLuong = Integer.parseInt(soLuongText);
            if (soLuong <= 0) { errSoLuong.setText("Số lượng phải > 0"); valid = false; }
        } catch (Exception e) { errSoLuong.setText("Số lượng không hợp lệ"); valid = false; }

        try {
            giaNhap = Integer.parseInt(giaNhapText);
            if (giaNhap < 0) { errGiaNhap.setText("Giá nhập không được âm"); valid = false; }
        } catch (Exception e) { errGiaNhap.setText("Giá nhập không hợp lệ"); valid = false; }

        LocalDate ngayHSD = null;
        if (hanSuDung.isEmpty() || hanSuDung.equals("dd/MM/yyyy")) {
            errHanSuDung.setText("Vui lòng nhập hạn sử dụng");
            valid = false;
        } else {
            try {
                ngayHSD = LocalDate.parse(hanSuDung, DATE_FORMAT);
                if (ngayHSD.isBefore(LocalDate.now())) {
                    errHanSuDung.setText("Hạn sử dụng phải >= hôm nay");
                    valid = false;
                }
            } catch (DateTimeParseException e) {
                errHanSuDung.setText("Sai định dạng dd/MM/yyyy");
                valid = false;
            }
        }

        if (!valid) return;

        if (busKho.tonTaiMaLoDangHoatDong(maLo)) {
            errMaLo.setText("Mã lô đã tồn tại");
            return;
        }

        LoHang loHang = new LoHang();
        loHang.setId(taoMaLoHangTuDong());
        loHang.setSoLoHang(maLo);
        loHang.setSoLuongLoHang(soLuong);
        loHang.setGia(giaNhap);
        loHang.setNgayNhap(LocalDateTime.now());
        loHang.setNgayHetHan(ngayHSD.atStartOfDay());
        loHang.setSanPhamId(selectedSanPham);
        loHang.setTrangThai(TrangThaiLoHang.CON_HANG); 

        KhoHang kho = new KhoHang();
        kho.setId("KHO-0001");
        loHang.setKhoHangId(kho);

        boolean laTaiSuDung = busKho.tonTaiMaLoDaAn(maLo);
        boolean success = busKho.themLoHang(loHang);

        if (success) {
            String msg = laTaiSuDung ? "Đã tái sử dụng lô hàng đã ẩn thành công!" : "Thêm lô hàng mới vào hệ thống thành công!";
            ThongBao.show(this, "Thành Công", msg, "SUCCESS");
            if (reloadListener != null) reloadListener.onReload();
            dispose();
        } else {
            ThongBao.show(this, "Thất Bại", "Lưu lô hàng thất bại! Vui lòng kiểm tra lại CSDL.", "ERROR");
        }
    }

    private String taoMaLoHangTuDong() {
        try {
            List<LoHang> dsLo = busKho.layDSLoHang();
            int max = 0;
            for (LoHang lh : dsLo) {
                if (lh == null || lh.getId() == null) continue;
                String id = lh.getId().trim().toUpperCase().replace("-", "");
                if (id.startsWith("LH")) {
                    try {
                        int so = Integer.parseInt(id.substring(2));
                        if (so > max) max = so;
                    } catch (Exception ignored) {}
                }
            }
            return String.format("LH-%04d", max + 1);
        } catch (Exception e) { 
        }
        return "LH-0001";
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

    private static class HintTextField extends JTextField {
        private final String hint;
        public HintTextField(String hint) { this.hint = hint; }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocusOwner()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(TEXT_HINT);
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                Insets ins = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(hint, ins.left + 2, y);
                g2.dispose();
            }
        }
    }

    private static class ModernBorder extends AbstractBorder {
        private boolean focused = false;
        public void setFocused(boolean focused) { this.focused = focused; }
        @Override public Insets getBorderInsets(Component c) { return new Insets(8, 14, 8, 14); }
        @Override public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = 14; insets.right = 14; insets.top = 8; insets.bottom = 8; return insets;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(focused ? BORDER_FOCUS : BORDER);
            g2.setStroke(new BasicStroke(focused ? 1.5f : 1f));
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 10, 10);
            g2.dispose();
        }
    }

    // ĐÃ KHẮC PHỤC: Chép toàn bộ JMenuItem JPopupMenu an toàn từ màn hình khuyến mãi qua
    class SuggestionField extends JPanel {
        private TextFieldWithPlaceholder textField;
        private JPopupMenu popupMenu;
        private List<String> dictionary;
        private boolean isSelecting = false;

        public SuggestionField(String label, String placeholder, List<String> dictionary, String iconName) {
            this.dictionary = dictionary;
            setLayout(new BorderLayout(0, 8));
            setOpaque(false);

            JLabel lbl = new JLabel(label);
            lbl.setFont(FONT_LABEL);
            lbl.setForeground(TEXT_PRIMARY);

            textField = new TextFieldWithPlaceholder(placeholder);
            textField.setBorder(null);
            textField.setOpaque(false);
            textField.setPreferredSize(new Dimension(0, 36));

            JPanel pnlInput = new JPanel(new BorderLayout(10, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pnlInput.setOpaque(false);
            pnlInput.setBackground(Color.WHITE);

            pnlInput.setBorder(BorderFactory.createCompoundBorder(
                    new ModernBorder(),
                    new EmptyBorder(0, 0, 0, 0)));

            if (iconName != null && !iconName.isEmpty()) {
                JLabel lblIcon = new JLabel(new MenuIcon(iconName));
                lblIcon.setForeground(PRIMARY);
                pnlInput.add(lblIcon, BorderLayout.WEST);
            }

            textField.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    pnlInput.setBackground(Color.decode("#F0F8FF"));
                    pnlInput.repaint();
                }

                public void focusLost(FocusEvent e) {
                    pnlInput.setBackground(Color.WHITE);
                    pnlInput.repaint();
                }
            });

            pnlInput.add(textField, BorderLayout.CENTER);

            add(lbl, BorderLayout.NORTH);
            add(pnlInput, BorderLayout.CENTER);

            popupMenu = new JPopupMenu();
            popupMenu.setFocusable(false);

            textField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { if (!isSelecting) showSuggestions(); }
                public void removeUpdate(DocumentEvent e) { if (!isSelecting) showSuggestions(); }
                public void changedUpdate(DocumentEvent e) { if (!isSelecting) showSuggestions(); }
            });
        }

        private void showSuggestions() {
            if (!textField.hasFocus()) return; 

            SwingUtilities.invokeLater(() -> {
                if (isSelecting) return;
                popupMenu.setVisible(false);
                popupMenu.removeAll();

                String rawText = textField.getText();
                if (rawText == null || rawText.trim().isEmpty()) return;

                String text = rawText.toLowerCase();
                boolean hasItems = false;
                int count = 0;

                for (String word : dictionary) {
                    if (word.toLowerCase().contains(text)) {
                        JMenuItem item = new JMenuItem(word);
                        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        item.setBackground(Color.WHITE);
                        item.setFont(FONT_TEXT);

                        item.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mousePressed(MouseEvent e) {
                                if (SwingUtilities.isLeftMouseButton(e)) {
                                    isSelecting = true;
                                    if (textField.getInputContext() != null) {
                                        textField.getInputContext().endComposition();
                                    }
                                    SwingUtilities.invokeLater(() -> {
                                        textField.setText(word);
                                        textField.setCaretPosition(word.length());
                                        textField.setForeground(TEXT_PRIMARY);
                                        popupMenu.setVisible(false);
                                        Timer timer = new Timer(50, evt -> isSelecting = false);
                                        timer.setRepeats(false);
                                        timer.start();
                                    });
                                }
                            }
                        });
                        popupMenu.add(item);
                        hasItems = true;
                        count++;
                        if (count >= 10) break;
                    }
                }

                if (hasItems) {
                    popupMenu.pack();
                    popupMenu.show(textField, 0, textField.getHeight());
                }
            });
        }

        public JTextField getTextField() {
            return textField;
        }

        class TextFieldWithPlaceholder extends JTextField {
            private String placeholder;
            public TextFieldWithPlaceholder(String placeholder) {
                this.placeholder = placeholder;
                this.setFont(FONT_TEXT);
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && placeholder != null && !placeholder.isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(TEXT_HINT);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(placeholder, getInsets().left, y);
                    g2.dispose();
                }
            }
        }
    }

    private static class DigitsOnlyFilter extends DocumentFilter {
        private final int maxLength;
        public DigitsOnlyFilter(int maxLength) { this.maxLength = maxLength; }
        @Override public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) return; replace(fb, offset, 0, string, attr);
        }
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
        @Override public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) return; replace(fb, offset, 0, string, attr);
        }
        @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) return;
            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String next = current.substring(0, offset) + text + current.substring(offset + length);
            String digits = next.replaceAll("\\D+", "");
            if (digits.length() <= maxDigits && next.matches("[\\d,.]*")) fb.replace(offset, length, text, attrs);
        }
    }

    private class CustomDatePicker extends JDialog {
        private int month = LocalDate.now().getMonthValue();
        private int year = LocalDate.now().getYear();
        private final JComboBox<String> cbMonth;
        private final JComboBox<Integer> cbYear;
        private final JPanel pnlDays;
        private final JTextField targetField;
        private boolean isUpdating = false;

        public CustomDatePicker(JDialog owner, JTextField targetField) {
            super(owner, false); 
            this.targetField = targetField;
            setUndecorated(true);

            JPanel rootPanel = new JPanel(new BorderLayout());
            rootPanel.setBackground(Color.WHITE);
            rootPanel.setBorder(BorderFactory.createLineBorder(PRIMARY, 2));
            setContentPane(rootPanel);

            JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
            header.setBackground(Color.WHITE);

            JButton btnPrev = new JButton(new MenuIcon("CHEVRON_LEFT"));
            styleNavButton(btnPrev);
            btnPrev.addActionListener(e -> changeMonth(-1));

            JButton btnNext = new JButton(new MenuIcon("CHEVRON_RIGHT"));
            styleNavButton(btnNext);
            btnNext.addActionListener(e -> changeMonth(1));

            String[] monthNames = new String[12];
            for (int i = 0; i < 12; i++) monthNames[i] = "Tháng " + (i + 1);
            cbMonth = new JComboBox<>(monthNames);
            styleComboBox(cbMonth);
            cbMonth.setSelectedIndex(month - 1);
            cbMonth.addActionListener(e -> {
                if (!isUpdating) {
                    month = cbMonth.getSelectedIndex() + 1;
                    refreshCalendar();
                }
            });

            Integer[] years = new Integer[16];
            int currentYear = LocalDate.now().getYear();
            for (int i = 0; i < 16; i++) years[i] = currentYear + i;
            cbYear = new JComboBox<>(years);
            styleComboBox(cbYear);
            cbYear.setSelectedItem(year);
            cbYear.addActionListener(e -> {
                if (!isUpdating) {
                    year = (Integer) cbYear.getSelectedItem();
                    refreshCalendar();
                }
            });

            header.add(btnPrev);
            header.add(cbMonth);
            header.add(cbYear);
            header.add(btnNext);

            JPanel weekHeader = new JPanel(new GridLayout(1, 7));
            weekHeader.setBackground(Color.WHITE);
            String[] days = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
            for (String d : days) {
                JLabel lb = new JLabel(d, SwingConstants.CENTER);
                lb.setFont(new Font("Segoe UI", Font.BOLD, 11)); 
                lb.setForeground(TEXT_SECONDARY);
                lb.setBorder(new EmptyBorder(2, 0, 2, 0));
                weekHeader.add(lb);
            }

            pnlDays = new JPanel(new GridLayout(0, 7, 2, 2));
            pnlDays.setBackground(Color.WHITE);
            pnlDays.setBorder(new EmptyBorder(4, 4, 4, 4));

            JPanel center = new JPanel(new BorderLayout());
            center.setBackground(Color.WHITE);
            center.add(weekHeader, BorderLayout.NORTH);
            center.add(pnlDays, BorderLayout.CENTER);

            rootPanel.add(header, BorderLayout.NORTH);
            rootPanel.add(center, BorderLayout.CENTER);

            refreshCalendar();

            addWindowFocusListener(new WindowAdapter() {
                @Override
                public void windowLostFocus(WindowEvent e) {
                    Window opp = e.getOppositeWindow();
                    if (opp != null && opp instanceof JWindow) {
                        return; 
                    }
                    setVisible(false); 
                }
            });
        }

        public void showPopup(Component invoker) {
            pack();
            Point screenLoc = invoker.getLocationOnScreen();
            int popupHeight = getHeight();
            int yPos = screenLoc.y + invoker.getHeight() + 2;

            try {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                if (yPos + popupHeight > screenSize.height - 40) { 
                    yPos = screenLoc.y - popupHeight - 2; 
                }
            } catch (Exception ex) {}

            setLocation(screenLoc.x, yPos);
            setVisible(true);
        }

        private void styleNavButton(JButton btn) {
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setForeground(PRIMARY);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        private void styleComboBox(JComboBox<?> cb) {
            cb.setFont(new Font("Segoe UI", Font.BOLD, 12));
            cb.setForeground(PRIMARY);
            cb.setBackground(Color.WHITE);
            cb.setFocusable(false);
            cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        private void changeMonth(int delta) {
            month += delta;
            if (month < 1) { 
                month = 12; 
                year--; 
                if (year < (Integer)cbYear.getItemAt(0)) year = (Integer)cbYear.getItemAt(0);
            } 
            else if (month > 12) { 
                month = 1; 
                year++; 
                if (year > (Integer)cbYear.getItemAt(cbYear.getItemCount() - 1)) year = (Integer)cbYear.getItemAt(cbYear.getItemCount() - 1);
            }
            refreshCalendar();
        }

        private void refreshCalendar() {
            isUpdating = true;
            cbMonth.setSelectedIndex(month - 1);
            cbYear.setSelectedItem(year);
            isUpdating = false;

            pnlDays.removeAll();
            YearMonth ym = YearMonth.of(year, month);
            LocalDate firstDay = ym.atDay(1);
            int daysInMonth = ym.lengthOfMonth();
            int startDayOfWeek = firstDay.getDayOfWeek().getValue();

            for (int i = 1; i < startDayOfWeek; i++) pnlDays.add(new JLabel(""));

            LocalDate today = LocalDate.now();
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = LocalDate.of(year, month, day);
                JButton btnDay = new JButton(String.valueOf(day));
                btnDay.setFocusPainted(false);
                btnDay.setFont(new Font("Segoe UI", Font.BOLD, 11));
                btnDay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btnDay.setMargin(new Insets(2, 2, 2, 2));

                if (date.isBefore(today)) {
                    btnDay.setEnabled(false);
                    btnDay.setForeground(new Color(180, 180, 180));
                    btnDay.setBackground(new Color(245, 245, 245));
                } else if (date.equals(today)) {
                    btnDay.setForeground(Color.WHITE);
                    btnDay.setBackground(PRIMARY);
                    btnDay.setBorder(BorderFactory.createLineBorder(PRIMARY));
                } else {
                    btnDay.setForeground(TEXT_PRIMARY);
                    btnDay.setBackground(Color.WHITE);
                    btnDay.setBorder(BorderFactory.createLineBorder(BORDER));
                }

                btnDay.addActionListener(e -> { targetField.setText(date.format(DATE_FORMAT)); setVisible(false); });
                pnlDays.add(btnDay);
            }
            pnlDays.revalidate(); pnlDays.repaint(); pack();
        }
    }
}