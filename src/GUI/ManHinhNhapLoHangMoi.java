package GUI;

import BUS.BUS_Kho;
import BUS.BUS_SanPham;
import BUS.BUS_DonViDoLuong;
import Entity.DonViDoLuong;
import Entity.KhoHang;
import Entity.LoHang;
import Entity.SanPham;
import Utils.MenuIcon;

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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color BORDER_FOCUS = new Color(14, 116, 144);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color WARNING = new Color(249, 115, 22);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color SUCCESS_HOVER = new Color(22, 163, 74);

    private static final int SO_NGAY_CAN_HAN = 30;

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_TEXT = new Font("Segoe UI", Font.PLAIN, 15);

    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat vnNumberFormat;

    private final BUS_SanPham busSanPham = new BUS_SanPham();
    private final BUS_Kho busKho = new BUS_Kho();
    private final BUS_DonViDoLuong busDonVi = new BUS_DonViDoLuong();

    private HintTextField txtTimSanPham;
    private SanPham selectedSanPham = null;
    private List<SanPham> dsTatCaSanPham = new ArrayList<>();
    private JPopupMenu popupSanPham;
    private JList<SanPham> listSanPham;
    private DefaultListModel<SanPham> modelSanPham;
    private boolean isFiltering = false;

    private double currentQuyCach = 1.0;
    private String currentDonViNho = "ĐV cơ bản";
    private String currentDonViLon = "Đơn vị";

    private HintTextField txtMaLo;
    private HintTextField txtSoLuong;
    private HintTextField txtGiaNhap;
    private HintTextField txtHanSuDung;

    private JLabel errSanPham, errMaLo, errSoLuong, errGiaNhap, errHanSuDung;
    private JLabel lblTongQuyDoi, lblGiaVonVien;
    private JLabel lblTitleSoLuong, lblTitleGiaNhap;

    private final ReloadListener reloadListener;

    // Bộ nhớ tạm liên kết mã vạch lạ trong phiên làm việc hiện tại
    private Map<String, SanPham> mapLienKetTam = new HashMap<>();

    public ManHinhNhapLoHangMoi(Window owner, ReloadListener reloadListener) {
        super(owner, "Thêm lô hàng", ModalityType.APPLICATION_MODAL);
        this.reloadListener = reloadListener;

        vnNumberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        vnNumberFormat.setMaximumFractionDigits(2);

        setUndecorated(true);
        setBackground(BG_TRANSPARENT);
        setSize(600, 680);
        setLocationRelativeTo(owner);

        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
        setContentPane(createMainUI());
        registerKeyboardActions();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                Timer timer = new Timer(150, evt -> showQRScannerDialog());
                timer.setRepeats(false);
                timer.start();
            }
        });
    }

    public void setSanPhamAutoFill(String tenSP) {
        if (txtTimSanPham != null) {
            txtTimSanPham.setText(tenSP);
            txtTimSanPham.requestFocus();
            txtTimSanPham.setCaretPosition(txtTimSanPham.getText().length());
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
                new EmptyBorder(16, 24, 16, 20)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel lblIcon = new JLabel(new MenuIcon("ADD"));
        lblIcon.setForeground(Color.WHITE);

        JLabel title = new JLabel("Thêm Lô Hàng Mới");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);

        left.add(lblIcon);
        left.add(title);

        JButton btnClose = new JButton(new MenuIcon("CLOSE"));
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setForeground(new Color(255, 255, 255, 180));
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnClose.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnClose.setForeground(new Color(255, 255, 255, 180));
            }
        });

        btnClose.addActionListener(e -> dispose());

        MouseAdapter dragWindow = new MouseAdapter() {
            int x, y;

            @Override
            public void mousePressed(MouseEvent e) {
                x = e.getX();
                y = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(getLocation().x + e.getX() - x, getLocation().y + e.getY() - y);
            }
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
        body.setBorder(new EmptyBorder(20, 32, 20, 32));

        try {
            dsTatCaSanPham = busSanPham.getDsThuoc();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel pnlQR = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlQR.setOpaque(false);

        JButton btnScanQR = createSecondaryButton("Quét mã tem");
        btnScanQR.setForeground(PRIMARY);
        btnScanQR.addActionListener(e -> showQRScannerDialog());
        pnlQR.add(btnScanQR);

        JPanel comboSanPhamWrapper = createProductSelectorField();

        txtMaLo = createTextField("VD: LOT-2026-0001");
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

        lblTongQuyDoi = new JLabel("Tổng số lượng: 0 (ĐV cơ bản)");
        lblTongQuyDoi.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTongQuyDoi.setForeground(PRIMARY);

        lblGiaVonVien = new JLabel("Giá vốn: 0 đ/ĐV cơ bản");
        lblGiaVonVien.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 14));
        lblGiaVonVien.setForeground(WARNING);

        DocumentListener calcListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                calculateTotal();
            }

            public void removeUpdate(DocumentEvent e) {
                calculateTotal();
            }

            public void changedUpdate(DocumentEvent e) {
                calculateTotal();
            }
        };

        txtSoLuong.getDocument().addDocumentListener(calcListener);
        txtGiaNhap.getDocument().addDocumentListener(calcListener);

        lblTitleSoLuong = new JLabel("Số lượng nhập kho *");
        lblTitleSoLuong.setFont(FONT_LABEL);
        lblTitleSoLuong.setForeground(TEXT_PRIMARY);

        lblTitleGiaNhap = new JLabel("Giá nhập (của 1 đơn vị) *");
        lblTitleGiaNhap.setFont(FONT_LABEL);
        lblTitleGiaNhap.setForeground(TEXT_PRIMARY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 12, 0);

        gbc.gridy = 0;
        body.add(pnlQR, gbc);

        gbc.gridy = 1;
        body.add(createFullWidthField("Sản phẩm (Chọn sản phẩm có sẵn) *", comboSanPhamWrapper, errSanPham, "SEARCH"),
                gbc);

        gbc.gridy = 2;
        body.add(createFullWidthField("Mã lô *", txtMaLo, errMaLo, "DOCUMENT"), gbc);

        gbc.gridy = 3;
        JPanel pnlCol1 = createFullWidthFieldWithLabel(lblTitleSoLuong, txtSoLuong, errSoLuong, "BOX");
        JPanel pnlCol2 = createFullWidthFieldWithLabel(lblTitleGiaNhap, txtGiaNhap, errGiaNhap, "TAB_DOLLAR");

        JPanel rowPanel = new JPanel(new GridLayout(1, 2, 24, 0));
        rowPanel.setOpaque(false);
        rowPanel.add(pnlCol1);
        rowPanel.add(pnlCol2);
        body.add(rowPanel, gbc);

        gbc.gridy = 4;
        JPanel dateFieldWrapper = createDatePickerField();
        body.add(createFullWidthField("Hạn sử dụng (dd/MM/yyyy) *", dateFieldWrapper, errHanSuDung, "TIME"), gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(10, 0, 10, 0);

        JPanel pnlTong = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlTong.setBackground(CARD_BG);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        row1.setOpaque(false);
        row1.add(lblTongQuyDoi);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        row2.setOpaque(false);
        row2.add(lblGiaVonVien);

        pnlTong.add(row1);
        pnlTong.add(row2);
        body.add(pnlTong, gbc);

        gbc.gridy = 6;
        gbc.weighty = 1.0;
        body.add(Box.createVerticalGlue(), gbc);

        return body;
    }

    private void updateQuyCachTuSanPham(String maSP) {
        currentQuyCach = 1.0;
        currentDonViNho = "ĐV cơ bản";
        currentDonViLon = "Đơn vị";

        try {
            List<DonViDoLuong> dsDVDL = busDonVi.getDSTheoMaSP(maSP);
            if (dsDVDL != null && !dsDVDL.isEmpty()) {
                for (DonViDoLuong dv : dsDVDL) {
                    if (dv.getChuyenDoiSangDonViCoBan() == 1.0) {
                        currentDonViNho = dv.getTen();
                    }

                    if (dv.getChuyenDoiSangDonViCoBan() > currentQuyCach) {
                        currentQuyCach = dv.getChuyenDoiSangDonViCoBan();
                        currentDonViLon = dv.getTen();
                    }
                }

                if (currentQuyCach == 1.0) {
                    currentDonViLon = dsDVDL.get(0).getTen();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        lblTitleSoLuong.setText("Số lượng nhập kho (" + currentDonViLon + ") *");
        lblTitleGiaNhap.setText("Giá nhập (của 1 " + currentDonViLon + ") *");
        calculateTotal();
    }

    private void calculateTotal() {
        try {
            long sl = txtSoLuong.getText().trim().isEmpty() ? 0 : Long.parseLong(txtSoLuong.getText().trim());
            long tong = (long) (sl * currentQuyCach);

            String textTong = "Tổng nhập kho: " + vnNumberFormat.format(tong) + " " + currentDonViNho;
            if (currentQuyCach > 1.0) {
                textTong += " (Quy cách 1 " + currentDonViLon + " = " + (int) currentQuyCach + " " + currentDonViNho
                        + ")";
            }

            lblTongQuyDoi.setText(textTong);

            long giaNhap = txtGiaNhap.getText().trim().isEmpty() ? 0
                    : Long.parseLong(getDigitsOnly(txtGiaNhap.getText()));
            double giaVien = (currentQuyCach > 0) ? ((double) giaNhap / currentQuyCach) : 0;

            lblGiaVonVien.setText("Giá vốn quy đổi: ~ " + vnNumberFormat.format(giaVien) + " đ/" + currentDonViNho);
        } catch (Exception e) {
            lblTongQuyDoi.setText("Tổng nhập kho: 0 (ĐV cơ bản)");
            lblGiaVonVien.setText("Giá vốn quy đổi: 0 đ/ĐV cơ bản");
        }
    }

    private JPanel createProductSelectorField() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Color.WHITE);

        ModernBorder sharedBorder = new ModernBorder();
        wrap.setBorder(sharedBorder);

        txtTimSanPham = new HintTextField("Nhập tên, mã SP hoặc bấm ▼ để chọn...");
        txtTimSanPham.setFont(FONT_TEXT);
        txtTimSanPham.setForeground(TEXT_PRIMARY);
        txtTimSanPham.setBorder(null);

        JButton btnDrop = new JButton("▼");
        btnDrop.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDrop.setFocusPainted(false);
        btnDrop.setContentAreaFilled(false);
        btnDrop.setBorder(new EmptyBorder(0, 12, 0, 12));
        btnDrop.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDrop.setForeground(PRIMARY);

        setupAutocompletePopup(wrap);

        btnDrop.addActionListener(e -> {
            if (popupSanPham != null && popupSanPham.isVisible()) {
                popupSanPham.setVisible(false);
            } else {
                txtTimSanPham.requestFocus();
                isFiltering = true;
                modelSanPham.clear();

                for (SanPham sp : dsTatCaSanPham) {
                    modelSanPham.addElement(sp);
                }

                isFiltering = false;
                showProductPopup(wrap);
            }
        });

        txtTimSanPham.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                SwingUtilities.invokeLater(() -> filterSanPham(wrap));
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }
        });

        txtTimSanPham.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!popupSanPham.isVisible() || modelSanPham.getSize() == 0) {
                    return;
                }

                int index = listSanPham.getSelectedIndex();

                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    index++;
                    if (index >= modelSanPham.getSize()) {
                        index = 0;
                    }
                    listSanPham.setSelectedIndex(index);
                    listSanPham.ensureIndexIsVisible(index);
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    index--;
                    if (index < 0) {
                        index = modelSanPham.getSize() - 1;
                    }
                    listSanPham.setSelectedIndex(index);
                    listSanPham.ensureIndexIsVisible(index);
                }
            }
        });

        txtTimSanPham.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                sharedBorder.setFocused(true);
                wrap.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                sharedBorder.setFocused(false);
                wrap.repaint();
            }
        });

        wrap.add(txtTimSanPham, BorderLayout.CENTER);
        wrap.add(btnDrop, BorderLayout.EAST);

        return wrap;
    }

    private void setupAutocompletePopup(JPanel anchorPanel) {
        modelSanPham = new DefaultListModel<>();

        listSanPham = new JList<>(modelSanPham);
        listSanPham.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSanPham.setVisibleRowCount(6);

        listSanPham.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel lb = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lb.setBorder(new EmptyBorder(10, 14, 10, 14));
                lb.setFont(FONT_TEXT);

                if (isSelected) {
                    lb.setBackground(new Color(241, 245, 249));
                    lb.setForeground(PRIMARY);
                } else {
                    lb.setBackground(Color.WHITE);
                    lb.setForeground(TEXT_PRIMARY);
                }

                if (value instanceof SanPham sp) {
                    lb.setText(sp.getTen() + " (" + sp.getId() + ")");
                }

                return lb;
            }
        });

        listSanPham.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    selectSanPhamFromList();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(listSanPham);
        scroll.setBorder(BorderFactory.createLineBorder(PRIMARY, 2));
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

        popupSanPham = new JPopupMenu();
        popupSanPham.setBorder(BorderFactory.createEmptyBorder());
        popupSanPham.add(scroll);
        popupSanPham.setFocusable(false);
    }

    private void filterSanPham(JPanel anchorPanel) {
        if (isFiltering) {
            return;
        }

        String kw = txtTimSanPham.getText().trim().toLowerCase();
        modelSanPham.clear();

        if (kw.isEmpty()) {
            for (SanPham sp : dsTatCaSanPham) {
                modelSanPham.addElement(sp);
            }
        } else {
            for (SanPham sp : dsTatCaSanPham) {
                if ((sp.getTen() != null && sp.getTen().toLowerCase().contains(kw))
                        || (sp.getId() != null && sp.getId().toLowerCase().contains(kw))) {
                    modelSanPham.addElement(sp);
                }
            }
        }

        if (modelSanPham.getSize() > 0) {
            showProductPopup(anchorPanel);
        } else {
            popupSanPham.setVisible(false);
        }
    }

    private void showProductPopup(JPanel anchorPanel) {
        int popupHeight = listSanPham.getPreferredScrollableViewportSize().height;
        int yPos = anchorPanel.getHeight() + 2;

        try {
            Point screenLoc = anchorPanel.getLocationOnScreen();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            if (screenLoc.y + yPos + popupHeight > screenSize.height - 40) {
                yPos = -popupHeight - 2;
            }
        } catch (Exception e) {
        }

        popupSanPham.setPopupSize(anchorPanel.getWidth(), popupHeight + 4);
        popupSanPham.show(anchorPanel, 0, yPos);
        txtTimSanPham.requestFocus();
    }

    private void selectSanPhamFromList() {
        SanPham sp = listSanPham.getSelectedValue();

        if (sp != null) {
            isFiltering = true;

            selectedSanPham = sp;
            txtTimSanPham.setText(sp.getTen() + " (" + sp.getId() + ")");
            popupSanPham.setVisible(false);
            txtTimSanPham.requestFocus();
            txtTimSanPham.setCaretPosition(txtTimSanPham.getText().length());

            isFiltering = false;

            updateQuyCachTuSanPham(sp.getId());
        }
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

        JButton btnCal = new JButton(new MenuIcon("CALENDAR"));
        btnCal.setFocusPainted(false);
        btnCal.setContentAreaFilled(false);
        btnCal.setBorder(new EmptyBorder(0, 12, 0, 12));
        btnCal.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCal.setForeground(PRIMARY);

        CustomDatePicker datePickerPopup = new CustomDatePicker(this, txtHanSuDung);

        btnCal.addActionListener(e -> datePickerPopup.showPopup(wrap));

        txtHanSuDung.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                sharedBorder.setFocused(true);
                wrap.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                sharedBorder.setFocused(false);
                wrap.repaint();
            }
        });

        wrap.add(txtHanSuDung, BorderLayout.CENTER);
        wrap.add(btnCal, BorderLayout.EAST);

        return wrap;
    }

    private JPanel createFullWidthField(String title, JComponent field, JLabel error, String iconName) {
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_LABEL);
        lblTitle.setForeground(TEXT_PRIMARY);
        return createFullWidthFieldWithLabel(lblTitle, field, error, iconName);
    }

    private JPanel createFullWidthFieldWithLabel(JLabel lblTitle, JComponent field, JLabel error, String iconName) {
        JPanel block = new JPanel(new BorderLayout(0, 8));
        block.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        titlePanel.setOpaque(false);

        if (iconName != null && !iconName.isEmpty()) {
            JLabel lblIcon = new JLabel(new MenuIcon(iconName));
            lblIcon.setForeground(PRIMARY);
            titlePanel.add(lblIcon);
        }

        titlePanel.add(lblTitle);

        JPanel fieldWrapper = new JPanel(new BorderLayout(0, 4));
        fieldWrapper.setOpaque(false);
        fieldWrapper.add(field, BorderLayout.CENTER);
        fieldWrapper.add(error, BorderLayout.SOUTH);

        block.add(titlePanel, BorderLayout.NORTH);
        block.add(fieldWrapper, BorderLayout.CENTER);

        return block;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(FOOTER_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(16, 24, 16, 24)));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);

        JButton btnCancel = createSecondaryButton("Hủy");
        btnCancel.setIcon(new MenuIcon("CANCEL"));
        btnCancel.setIconTextGap(8);

        JButton btnSubmit = createPrimaryButton("Lưu & In Tem");
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
            @Override
            public void focusGained(FocusEvent e) {
                border.setFocused(true);
                field.repaint();
                SwingUtilities.invokeLater(field::selectAll);
            }

            @Override
            public void focusLost(FocusEvent e) {
                border.setFocused(false);
                field.repaint();
            }
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
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(SUCCESS_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(SUCCESS);
            }
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
                new EmptyBorder(9, 23, 9, 23)));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(241, 245, 249));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
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
                        field.setText(vnNumberFormat.format(Long.parseLong(digits)));
                    } catch (NumberFormatException ignored) {
                    }
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

        rootPane.registerKeyboardAction(e -> {
            if (popupSanPham != null && popupSanPham.isVisible() && listSanPham.getSelectedIndex() >= 0) {
                selectSanPhamFromList();
            } else {
                handleSubmit();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        rootPane.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private String getDigitsOnly(String text) {
        return text == null ? "" : text.replaceAll("\\D+", "");
    }

    private void handleSubmit() {
        clearErrors();

        String spText = txtTimSanPham.getText().trim();
        String maLo = txtMaLo.getText().trim().toUpperCase();
        String giaNhapText = getDigitsOnly(txtGiaNhap.getText().trim());
        String hanSuDung = txtHanSuDung.getText().trim();

        boolean valid = true;

        // 1. Kiểm tra sản phẩm: bắt buộc chọn sản phẩm có sẵn
        if (spText.isEmpty() || spText.equals("Nhập tên, mã SP hoặc bấm ▼ để chọn...")) {
            errSanPham.setText("Vui lòng chọn sản phẩm");
            valid = false;
        } else {
            selectedSanPham = null;

            for (SanPham sp : dsTatCaSanPham) {
                String displayText = sp.getTen() + " (" + sp.getId() + ")";
                if (spText.equalsIgnoreCase(displayText)
                        || spText.equalsIgnoreCase(sp.getTen())
                        || spText.equalsIgnoreCase(sp.getId())) {
                    selectedSanPham = sp;
                    break;
                }
            }

            if (selectedSanPham == null) {
                errSanPham.setText("Sản phẩm chưa tồn tại, vui lòng chọn sản phẩm có sẵn");
                valid = false;
            } else {
                updateQuyCachTuSanPham(selectedSanPham.getId());
            }
        }

        // 2. Kiểm tra mã lô
        if (maLo.isEmpty()) {
            errMaLo.setText("Số lô không được bỏ trống");
            valid = false;
        } else if (!maLo.matches("[A-Z0-9\\-_/\\.]{2,50}")) {
            errMaLo.setText("Mã lô chỉ gồm chữ, số, -, _, /, . và từ 2-50 ký tự");
            valid = false;
        }

        // 3. Kiểm tra số lượng
        long soLuongLon = 0;
        try {
            String soLuongText = txtSoLuong.getText().trim();

            if (soLuongText.isEmpty()) {
                errSoLuong.setText("Vui lòng nhập số lượng");
                valid = false;
            } else {
                soLuongLon = Long.parseLong(soLuongText);
            }
        } catch (Exception e) {
            errSoLuong.setText("Số lượng không hợp lệ");
            valid = false;
        }

        long tongSoLuongQuyDoiLong = (long) (soLuongLon * currentQuyCach);

        if (tongSoLuongQuyDoiLong <= 0) {
            errSoLuong.setText("Số lượng phải > 0");
            valid = false;
        }

        if (tongSoLuongQuyDoiLong > Integer.MAX_VALUE) {
            errSoLuong.setText("Số lượng quá lớn");
            valid = false;
        }

        int tongSoLuongQuyDoi = (int) tongSoLuongQuyDoiLong;

        // 4. Kiểm tra giá nhập
        long giaNhapDonViLon = 0;
        double giaVonDonViNho = 0.0;

        try {
            if (giaNhapText.isEmpty()) {
                errGiaNhap.setText("Vui lòng nhập giá");
                valid = false;
            } else {
                giaNhapDonViLon = Long.parseLong(giaNhapText);

                if (giaNhapDonViLon <= 0) {
                    errGiaNhap.setText("Giá nhập phải > 0");
                    valid = false;
                } else {
                    giaVonDonViNho = (currentQuyCach > 0) ? ((double) giaNhapDonViLon / currentQuyCach) : 0;
                }
            }
        } catch (Exception e) {
            errGiaNhap.setText("Giá nhập không hợp lệ");
            valid = false;
        }

        // 5. Kiểm tra hạn sử dụng
        LocalDate ngayHSD = null;
        boolean canHan = false;

        if (hanSuDung.isEmpty() || hanSuDung.equals("dd/MM/yyyy")) {
            errHanSuDung.setText("Vui lòng nhập HSD");
            valid = false;
        } else {
            try {
                ngayHSD = LocalDate.parse(hanSuDung, DATE_FORMAT);

                if (ngayHSD.isBefore(LocalDate.now())) {
                    errHanSuDung.setText("Lô hàng đã hết hạn");
                    valid = false;
                } else if (!ngayHSD.isAfter(LocalDate.now().plusDays(SO_NGAY_CAN_HAN))) {
                    canHan = true;
                }
            } catch (DateTimeParseException e) {
                errHanSuDung.setText("Sai định dạng ngày");
                valid = false;
            }
        }

        if (!valid) {
            return;
        }

        // 6. Không cho trùng mã lô đang hoạt động
        if (busKho.tonTaiMaLoDangHoatDong(maLo)) {
            errMaLo.setText("Mã lô đã tồn tại trong kho");
            return;
        }

        // 7. Cảnh báo cận hạn
        if (canHan) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Lô hàng còn dưới hoặc bằng " + SO_NGAY_CAN_HAN + " ngày đến hạn.\nBạn vẫn muốn nhập kho?",
                    "Cảnh báo cận hạn",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                errHanSuDung.setText("Lô hàng cận hạn, cần xác nhận để nhập");
                return;
            }
        }

        // 8. Cảnh báo giá nhập > giá bán
        if (selectedSanPham != null && selectedSanPham.getGiaBan() > 0) {
            double giaBanCoBan = selectedSanPham.getGiaBan();

            if (giaVonDonViNho > giaBanCoBan) {
                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "Giá nhập quy đổi đang lớn hơn giá bán hiện tại.\n"
                                + "Giá nhập quy đổi: " + vnNumberFormat.format(giaVonDonViNho) + " đ/" + currentDonViNho
                                + "\n"
                                + "Giá bán hiện tại: " + vnNumberFormat.format(giaBanCoBan) + " đ/" + currentDonViNho
                                + "\n"
                                + "Bạn vẫn muốn nhập lô này?",
                        "Cảnh báo lợi nhuận",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (confirm != JOptionPane.YES_OPTION) {
                    errGiaNhap.setText("Giá nhập > giá bán, cần kiểm tra");
                    return;
                }
            }
        }

        String idTuDong = taoMaLoHangTuDong();
        String maVachNoiBo = taoMaVachNoiBoKhongTrung(idTuDong);

        LoHang loHang = new LoHang();
        loHang.setId(idTuDong);
        loHang.setSoLoHang(maLo);
        loHang.setSoLuongLoHang(tongSoLuongQuyDoi);
        loHang.setGia(giaVonDonViNho);
        loHang.setNgayNhap(LocalDateTime.now());
        loHang.setNgayHetHan(ngayHSD.atStartOfDay());
        loHang.setSanPhamId(selectedSanPham);
        loHang.setMaVachNoiBo(maVachNoiBo);

        String maKhoMacDinh = "KHO-0001";

        if (!busKho.tonTaiKho(maKhoMacDinh)) {
            showModernAlert("Kho mặc định KHO-0001 chưa tồn tại trong CSDL!", false);
            return;
        }

        KhoHang kho = new KhoHang();
        kho.setId(maKhoMacDinh);
        loHang.setKhoHangId(kho);

        boolean laTaiSuDung = busKho.tonTaiMaLoDaAn(maLo);
        boolean success = busKho.themLoHang(loHang);

        if (success) {
            String tenSPHienThi = selectedSanPham.getTen() != null ? selectedSanPham.getTen() : "Sản phẩm không tên";
            String maSPHienThi = selectedSanPham.getId() != null ? selectedSanPham.getId() : "N/A";

            showModernAlert(laTaiSuDung ? "Đã tái sử dụng lô hàng đã ẩn!" : "Thêm lô hàng thành công!", true);

            if (reloadListener != null) {
                reloadListener.onReload();
            }

            dispose();

            Window owner = SwingUtilities.getWindowAncestor(this);
            DialogInMaVach dialogMaVach = new DialogInMaVach(owner, maVachNoiBo, maSPHienThi, tenSPHienThi);
            dialogMaVach.setVisible(true);
        } else {
            showModernAlert("Lưu lô hàng thất bại!", false);
        }
    }

    private String taoMaLoHangTuDong() {
        try {
            List<LoHang> dsLo = busKho.layDSLoHang(true);

            int max = 0;

            for (LoHang lh : dsLo) {
                if (lh == null || lh.getId() == null) {
                    continue;
                }

                String id = lh.getId().trim().toUpperCase().replace("-", "");

                if (id.startsWith("LH")) {
                    try {
                        int so = Integer.parseInt(id.substring(2));
                        if (so > max) {
                            max = so;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            return String.format("LH-%04d", max + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "LH-0001";
    }

    private String taoMaVachNoiBoKhongTrung(String idLoHang) {
        String base = "MV" + idLoHang.replace("-", "");
        String ma = base;
        int i = 1;

        while (busKho.tonTaiMaVachNoiBo(ma)) {
            ma = base + "-" + i;
            i++;
        }

        return ma;
    }

    private void clearErrors() {
        errSanPham.setText(" ");
        errMaLo.setText(" ");
        errSoLuong.setText(" ");
        errGiaNhap.setText(" ");
        errHanSuDung.setText(" ");
    }

    // ==============================================================================
    // BẢNG QUÉT MÃ QR/TEM 2D
    // ==============================================================================
    private void showQRScannerDialog() {
        JDialog dialog = new JDialog(this, "YÊU CẦU QUÉT MÃ", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(450, 180);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setShape(new RoundRectangle2D.Double(0, 0, dialog.getWidth(), dialog.getHeight(), 16, 16));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(PRIMARY, 2));

        JLabel lblIconScan = new JLabel(new MenuIcon("ADD", 40, PRIMARY));
        lblIconScan.setHorizontalAlignment(SwingConstants.CENTER);
        lblIconScan.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel lblInfo = new JLabel("Vui lòng đưa súng quét đọc mã in trên hộp...", SwingConstants.CENTER);
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblInfo.setForeground(TEXT_PRIMARY);

        JButton btnClose = createSecondaryButton("Nhập tay");
        btnClose.setPreferredSize(new Dimension(120, 38));
        btnClose.addActionListener(e -> dialog.dispose());

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.add(btnClose);

        root.add(lblIconScan, BorderLayout.NORTH);
        root.add(lblInfo, BorderLayout.CENTER);
        root.add(pnlBottom, BorderLayout.SOUTH);

        JTextField txtHiddenQR = new JTextField();
        txtHiddenQR.setOpaque(false);
        txtHiddenQR.setBorder(null);
        txtHiddenQR.setForeground(new Color(0, 0, 0, 0));
        root.add(txtHiddenQR, BorderLayout.WEST);

        txtHiddenQR.addActionListener(e -> {
            String qrData = txtHiddenQR.getText().trim();

            if (!qrData.isEmpty()) {
                dialog.dispose();
                processQRCodeData(qrData);
            }
        });

        dialog.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                txtHiddenQR.requestFocusInWindow();
            }
        });

        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private static class Parsed2DDataMatrix {
        String gtin;
        String soLo;
        LocalDate hanDung;

        Parsed2DDataMatrix(String gtin, String soLo, LocalDate hanDung) {
            this.gtin = gtin;
            this.soLo = soLo;
            this.hanDung = hanDung;
        }
    }

    private Parsed2DDataMatrix parse2DDataMatrix(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        String data = raw.trim();

        // Format test dễ dùng:
        // GTIN|SOLO|HSD
        // Ví dụ: 8931234567890|LOT001|31/12/2026
        if (data.contains("|")) {
            String[] parts = data.split("\\|", -1);

            if (parts.length >= 3) {
                String gtin = parts[0].trim();
                String soLo = parts[1].trim();
                LocalDate hsd = parseNgayHanDung(parts[2].trim());

                if (!isBlankLocal(gtin) && !isBlankLocal(soLo) && hsd != null) {
                    return new Parsed2DDataMatrix(gtin, soLo, hsd);
                }
            }

            return null;
        }

        // Format GS1 có ngoặc:
        // (01)GTIN(10)SOLO(17)YYMMDD
        if (data.contains("(01)") || data.contains("(10)") || data.contains("(17)")) {
            String gtin = getAIWithParentheses(data, "01");
            String soLo = getAIWithParentheses(data, "10");
            String hsdRaw = getAIWithParentheses(data, "17");

            LocalDate hsd = parseNgayHanDung(hsdRaw);

            if (!isBlankLocal(gtin) && !isBlankLocal(soLo) && hsd != null) {
                return new Parsed2DDataMatrix(gtin, soLo, hsd);
            }

            return null;
        }

        // Format GS1 raw:
        // 01 + GTIN 14 số + 17 + YYMMDD + 10 + số lô
        // hoặc 01 + GTIN + 10 + số lô + 17 + YYMMDD
        String normalized = data.replace('\u001D', '|');

        if (normalized.startsWith("01") && normalized.length() >= 16) {
            try {
                String gtin = normalized.substring(2, 16);
                String remain = normalized.substring(16);

                String soLo = null;
                String hsdRaw = null;

                int idx17 = remain.indexOf("17");
                int idx10 = remain.indexOf("10");

                if (idx17 >= 0 && idx17 + 8 <= remain.length()) {
                    hsdRaw = remain.substring(idx17 + 2, idx17 + 8);
                }

                if (idx10 >= 0) {
                    int startLot = idx10 + 2;
                    int endLot = remain.length();

                    int sep = remain.indexOf("|", startLot);
                    if (sep >= 0) {
                        endLot = sep;
                    } else {
                        int next17 = remain.indexOf("17", startLot);
                        if (next17 > startLot) {
                            endLot = next17;
                        }
                    }

                    soLo = remain.substring(startLot, endLot);
                }

                LocalDate hsd = parseNgayHanDung(hsdRaw);

                if (!isBlankLocal(gtin) && !isBlankLocal(soLo) && hsd != null) {
                    return new Parsed2DDataMatrix(gtin, soLo, hsd);
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private String getAIWithParentheses(String data, String ai) {
        try {
            Pattern pattern = Pattern.compile("\\(" + ai + "\\)(.*?)(?=\\(\\d{2}\\)|$)");
            Matcher matcher = pattern.matcher(data);

            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private LocalDate parseNgayHanDung(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        String s = text.trim();

        // dd/MM/yyyy
        try {
            return LocalDate.parse(s, DATE_FORMAT);
        } catch (Exception ignored) {
        }

        // yyyy-MM-dd
        try {
            return LocalDate.parse(s);
        } catch (Exception ignored) {
        }

        // GS1 AI 17: YYMMDD
        if (s.matches("\\d{6}")) {
            try {
                int yy = Integer.parseInt(s.substring(0, 2));
                int mm = Integer.parseInt(s.substring(2, 4));
                int dd = Integer.parseInt(s.substring(4, 6));

                return LocalDate.of(2000 + yy, mm, dd);
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private boolean isBlankLocal(String s) {
        return s == null || s.trim().isEmpty();
    }

    private SanPham timSanPhamTheoMaVachHoacId(String maQuet) {
        if (maQuet == null || maQuet.trim().isEmpty()) {
            return null;
        }

        String ma = maQuet.trim();

        if (mapLienKetTam.containsKey(ma)) {
            return mapLienKetTam.get(ma);
        }

        // 1. Tìm theo mã vạch chính trong SanPham.maVach
        for (SanPham sp : dsTatCaSanPham) {
            if (sp.getMaVach() != null && sp.getMaVach().trim().equalsIgnoreCase(ma)) {
                return sp;
            }
        }

        // 2. Tìm theo mã sản phẩm
        for (SanPham sp : dsTatCaSanPham) {
            if (sp.getId() != null && sp.getId().trim().equalsIgnoreCase(ma)) {
                return sp;
            }
        }

        // 3. Tìm theo mã vạch đơn vị đo lường nếu có
        DonViDoLuong dv = busDonVi.layDonViTheoMaVach(ma);
        if (dv != null && dv.getSanPhamId() != null && dv.getSanPhamId().getId() != null) {
            String maSP = dv.getSanPhamId().getId();

            for (SanPham sp : dsTatCaSanPham) {
                if (sp.getId() != null && sp.getId().equalsIgnoreCase(maSP)) {
                    return sp;
                }
            }
        }

        return null;
    }

    private boolean chonSanPhamTuMaQuet(String maQuet) {
        selectedSanPham = timSanPhamTheoMaVachHoacId(maQuet);

        if (selectedSanPham == null) {
            boolean daLienKet = moHopThoaiLienKetMaVach(maQuet);
            if (!daLienKet) {
                return false;
            }
        }

        if (selectedSanPham != null && selectedSanPham.getId() != null) {
            setSanPhamAutoFill(selectedSanPham.getTen() + " (" + selectedSanPham.getId() + ")");
            updateQuyCachTuSanPham(selectedSanPham.getId());
            return true;
        }

        return false;
    }

    private void canhBaoCanHanNeuCo(LocalDate hsd) {
        if (hsd == null) {
            return;
        }

        if (!hsd.isBefore(LocalDate.now()) && !hsd.isAfter(LocalDate.now().plusDays(SO_NGAY_CAN_HAN))) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lô hàng còn dưới hoặc bằng " + SO_NGAY_CAN_HAN
                            + " ngày đến hạn.\nVui lòng kiểm tra kỹ trước khi lưu.",
                    "Cảnh báo cận hạn",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void processQRCodeData(String qrData) {
        try {
            if (qrData == null || qrData.trim().isEmpty()) {
                showModernAlert("Mã quét rỗng!", false);
                return;
            }

            qrData = qrData.trim();

            // 1. Quét 2D DataMatrix: lấy GTIN, số lô, hạn dùng
            Parsed2DDataMatrix parsed = parse2DDataMatrix(qrData);

            if (parsed != null) {
                String gtin = parsed.gtin.trim();
                String soLo = parsed.soLo.trim().toUpperCase();
                LocalDate hsd = parsed.hanDung;

                if (hsd.isBefore(LocalDate.now())) {
                    showModernAlert("Lô hàng đã hết hạn, không được nhập kho!", false);
                    return;
                }

                if (!chonSanPhamTuMaQuet(gtin)) {
                    showModernAlert("Đã hủy liên kết mã GTIN!", false);
                    return;
                }

                txtMaLo.setText(soLo);
                txtHanSuDung.setText(hsd.format(DATE_FORMAT));

                // Mã 2D không chứa số lượng và giá nhập
                // Nhân viên nhập thủ công 2 ô này
                txtSoLuong.requestFocus();

                calculateTotal();
                clearErrors();
                canhBaoCanHanNeuCo(hsd);

                showModernAlert("Đã quét GTIN, số lô và hạn dùng. Vui lòng nhập số lượng và giá nhập.", true);
                return;
            }

            // 2. Quét mã vạch sản phẩm 1D
            boolean laMaVachSanPham = qrData.matches("\\d{8,14}");

            if (laMaVachSanPham) {
                if (!chonSanPhamTuMaQuet(qrData)) {
                    showModernAlert("Đã hủy liên kết mã sản phẩm!", false);
                    return;
                }

                txtMaLo.requestFocus();
                showModernAlert("Đã nhận diện sản phẩm. Vui lòng nhập/quét tiếp số lô.", true);
                return;
            }

            // 3. Còn lại xem là số lô
            txtMaLo.setText(qrData.toUpperCase());
            txtSoLuong.requestFocus();
            clearErrors();

            showModernAlert("Đã quét số lô: " + qrData, true);
        } catch (Exception ex) {
            ex.printStackTrace();
            showModernAlert("Lỗi xử lý dữ liệu quét!", false);
        }
    }

    // ==============================================================================
    // DIALOG LIÊN KẾT MÃ LẠ
    // ==============================================================================
    private boolean moHopThoaiLienKetMaVach(String maVachLa) {
        final boolean[] result = { false };

        JDialog dlgLink = new JDialog(this, "Liên Kết Mã Vạch", ModalityType.APPLICATION_MODAL);
        dlgLink.setSize(480, 260);
        dlgLink.setLocationRelativeTo(this);
        dlgLink.setUndecorated(true);

        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBorder(BorderFactory.createLineBorder(WARNING, 2));
        pnl.setBackground(Color.WHITE);

        JLabel lblHeader = new JLabel(" PHÁT HIỆN MÃ VẠCH LẠ", SwingConstants.CENTER);
        lblHeader.setOpaque(true);
        lblHeader.setBackground(WARNING);
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblHeader.setPreferredSize(new Dimension(0, 45));
        lblHeader.setIcon(new MenuIcon("WARNING", 20, Color.WHITE));
        lblHeader.setIconTextGap(10);

        pnl.add(lblHeader, BorderLayout.NORTH);

        JPanel pnlBody = new JPanel(new GridBagLayout());
        pnlBody.setBackground(Color.WHITE);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(10, 20, 10, 20);
        g.weightx = 1.0;

        String msg = "<html>Mã quét được <b>[" + maVachLa
                + "]</b> chưa có trong CSDL.<br>Hãy chọn Sản phẩm tương ứng để liên kết GTIN/mã vạch này:</html>";

        JLabel lblMsg = new JLabel(msg);
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMsg.setForeground(TEXT_PRIMARY);

        JComboBox<String> cbChonSP = new JComboBox<>();
        for (SanPham sp : dsTatCaSanPham) {
            cbChonSP.addItem(sp.getTen() + " (" + sp.getId() + ")");
        }

        cbChonSP.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cbChonSP.setPreferredSize(new Dimension(0, 40));
        cbChonSP.setBackground(Color.WHITE);

        g.gridy = 0;
        pnlBody.add(lblMsg, g);

        g.gridy = 1;
        pnlBody.add(cbChonSP, g);

        pnl.add(pnlBody, BorderLayout.CENTER);

        JPanel pnlFoot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlFoot.setBackground(Color.WHITE);

        JButton btnHuy = createSecondaryButton("Hủy bỏ");
        btnHuy.addActionListener(e -> dlgLink.dispose());

        JButton btnXacNhan = createPrimaryButton("Xác nhận liên kết");
        btnXacNhan.setBackground(PRIMARY);

        btnXacNhan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnXacNhan.setBackground(PRIMARY.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnXacNhan.setBackground(PRIMARY);
            }
        });

        btnXacNhan.addActionListener(e -> {
            int idx = cbChonSP.getSelectedIndex();

            if (idx >= 0) {
                SanPham spChon = dsTatCaSanPham.get(idx);

                boolean ok = busSanPham.capNhatMaVachSanPham(
                        spChon.getId(),
                        maVachLa);

                if (!ok) {
                    showModernAlert("Không thể lưu mã vạch vào sản phẩm!", false);
                    return;
                }

                spChon.setMaVach(maVachLa);
                mapLienKetTam.put(maVachLa, spChon);
                selectedSanPham = spChon;
                result[0] = true;
                dlgLink.dispose();
            }
        });

        pnlFoot.add(btnHuy);
        pnlFoot.add(btnXacNhan);
        pnl.add(pnlFoot, BorderLayout.SOUTH);

        dlgLink.setContentPane(pnl);
        dlgLink.setVisible(true);

        return result[0];
    }

    private void showModernAlert(String message, boolean isSuccess) {
        JDialog dialog = new JDialog(this, "Thông báo", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        Color themeColor = isSuccess ? SUCCESS : DANGER;

        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createLineBorder(themeColor, 2));
        pnlMain.setBackground(Color.WHITE);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(themeColor);
        pnlHeader.setPreferredSize(new Dimension(0, 45));

        JLabel lblTitle = new JLabel(isSuccess ? "THÀNH CÔNG" : "THÔNG BÁO LỖI", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        pnlHeader.add(lblTitle, BorderLayout.CENTER);

        JPanel pnlBody = new JPanel(null);
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setPreferredSize(new Dimension(420, 130));

        JPanel pnlIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (!isSuccess) {
                    g2.setColor(new Color(254, 226, 226));
                    g2.fillRoundRect(0, 0, 50, 50, 50, 50);
                    g2.setColor(DANGER);
                    g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(17, 17, 33, 33);
                    g2.drawLine(33, 17, 17, 33);
                } else {
                    g2.setColor(new Color(209, 250, 229));
                    g2.fillRoundRect(0, 0, 50, 50, 50, 50);
                    g2.setColor(SUCCESS);
                    g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(15, 26, 22, 33);
                    g2.drawLine(22, 33, 35, 16);
                }

                g2.dispose();
            }
        };

        pnlIcon.setBounds(25, 25, 50, 50);
        pnlIcon.setOpaque(false);

        JTextArea msgArea = new JTextArea(message);
        msgArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        msgArea.setWrapStyleWord(true);
        msgArea.setLineWrap(true);
        msgArea.setOpaque(false);
        msgArea.setEditable(false);
        msgArea.setFocusable(false);

        JScrollPane scroll = new JScrollPane(msgArea);
        scroll.setBounds(95, 20, 305, 95);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        pnlBody.add(pnlIcon);
        pnlBody.add(scroll);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlFooter.setBackground(Color.WHITE);

        JButton btnClose = new JButton("Đóng");
        btnClose.setFocusPainted(false);
        btnClose.setForeground(Color.WHITE);
        btnClose.setBackground(themeColor);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.setBorder(new EmptyBorder(8, 30, 8, 30));
        btnClose.setOpaque(true);

        btnClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnClose.setBackground(isSuccess ? SUCCESS_HOVER : DANGER.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnClose.setBackground(themeColor);
            }
        });

        btnClose.addActionListener(e -> dialog.dispose());

        pnlFooter.add(btnClose);

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlBody, BorderLayout.CENTER);
        pnlMain.add(pnlFooter, BorderLayout.SOUTH);

        dialog.add(pnlMain);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        if (isSuccess) {
            Timer timer = new Timer(1500, e -> dialog.dispose());
            timer.setRepeats(false);
            timer.start();
        }

        dialog.setVisible(true);
    }

    private static class HintTextField extends JTextField {
        private final String hint;

        public HintTextField(String hint) {
            this.hint = hint;
        }

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

        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 14, 8, 14);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = 14;
            insets.right = 14;
            insets.top = 8;
            insets.bottom = 8;
            return insets;
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

    private static class DigitsOnlyFilter extends DocumentFilter {
        private final int maxLength;

        public DigitsOnlyFilter(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) {
                return;
            }

            replace(fb, offset, 0, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) {
                return;
            }

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
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string == null) {
                return;
            }

            replace(fb, offset, 0, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) {
                return;
            }

            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String next = current.substring(0, offset) + text + current.substring(offset + length);
            String digits = next.replaceAll("\\D+", "");

            if (digits.length() <= maxDigits && next.matches("[\\d,.]*")) {
                fb.replace(offset, length, text, attrs);
            }
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
            for (int i = 0; i < 12; i++) {
                monthNames[i] = "Tháng " + (i + 1);
            }

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

            for (int i = 0; i < 16; i++) {
                years[i] = currentYear + i;
            }

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

            String[] days = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };
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
            } catch (Exception ex) {
            }

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

                if (year < (Integer) cbYear.getItemAt(0)) {
                    year = (Integer) cbYear.getItemAt(0);
                }
            } else if (month > 12) {
                month = 1;
                year++;

                if (year > (Integer) cbYear.getItemAt(cbYear.getItemCount() - 1)) {
                    year = (Integer) cbYear.getItemAt(cbYear.getItemCount() - 1);
                }
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

            for (int i = 1; i < startDayOfWeek; i++) {
                pnlDays.add(new JLabel(""));
            }

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

                btnDay.addActionListener(e -> {
                    targetField.setText(date.format(DATE_FORMAT));
                    setVisible(false);
                });

                pnlDays.add(btnDay);
            }

            pnlDays.revalidate();
            pnlDays.repaint();
            pack();
        }
    }
}