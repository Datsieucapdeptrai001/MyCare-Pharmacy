package GUI;

import BUS.BUS_Kho;
import BUS.BUS_DonViDoLuong;
import Entity.DonViDoLuong;
import Entity.LoHang;
import Entity.SanPham;
import Utils.MenuIcon;
import Utils.SessionDangNhap;
import Utils.TelexFix;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ManHinhXuatKho extends JPanel {

    private static final Color BG_APP = Color.WHITE;
    private static final Color BG_SOFT = new Color(248, 250, 252);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color PRIMARY_BLUE = new Color(37, 99, 235);
    private static final Color PRIMARY_HOVER = new Color(29, 78, 216);
    private static final Color PRIMARY_DARK = new Color(30, 58, 95);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color ORANGE_BTN = new Color(249, 115, 22);
    private static final Color ORANGE_HOVER = new Color(194, 65, 12);

    private JTextField txtMaLo, txtSoLuong, txtGhiChu;
    private JComboBox<String> cbLyDo, cbDonVi;
    private JLabel lblTenSP, lblTonKhoHienTai, lblKhoHang, lblTableTitle;

    private JTable table;
    private DefaultTableModel tableModel;

    private final BUS_Kho busKho = new BUS_Kho();
    private final BUS_DonViDoLuong busDonVi = new BUS_DonViDoLuong();

    private int currentTonKho = 0;
    private SanPham currentSanPham = null;
    private String currentKhoHienThi = "Không xác định";
    private List<DonViDoLuong> currentDsDonVi = new ArrayList<>();
    private List<LoHang> cacheDanhSachLo = new ArrayList<>();

    private LoHang currentLoHang = null;
    private String currentLoHangId = "";

    private JPopupMenu popupSuggest;
    private JList<String> listSuggest;
    private DefaultListModel<String> modelSuggest;
    private JScrollPane scrollSuggest;
    private final List<LoHang> dsSuggestLo = new ArrayList<>();
    private boolean isProgrammaticUpdate = false;

    public ManHinhXuatKho() {
        setLayout(new BorderLayout());
        setBackground(BG_APP);

        new Thread(() -> {
            List<LoHang> tempData = busKho.layDSLoHang(false);

            List<LoHang> dsCoTheXuat = new ArrayList<>();

            if (tempData != null) {
                for (LoHang lh : tempData) {
                    if (laLoCoTheXuat(lh)) {
                        dsCoTheXuat.add(lh);
                    }
                }
            }

            SwingUtilities.invokeLater(() -> cacheDanhSachLo = dsCoTheXuat);
        }).start();

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        setupPhimTatChucNang();

        TelexFix.applyDeep(this);
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                SwingUtilities.invokeLater(() -> showQRScannerDialog());
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }
    private void setupPhimTatChucNang() {
        dangKyPhimTat("XK_SCAN_QR_F2", KeyEvent.VK_F2, 0, this::showQRScannerDialog);

        dangKyPhimTat("XK_KIEM_TRA_F3", KeyEvent.VK_F3, 0, this::kiemTraMaLo);

        dangKyPhimTat("XK_MAX_F4", KeyEvent.VK_F4, 0, this::chucNangXuatMax);

        dangKyPhimTat("XK_THEM_VAO_PHIEU_F5", KeyEvent.VK_F5, 0, this::themVaoDanhSach);

        dangKyPhimTat("XK_LAM_MOI_F6", KeyEvent.VK_F6, 0, this::resetFormToanBo);

        dangKyPhimTat("XK_XAC_NHAN_F10", KeyEvent.VK_F10, 0, this::hoanTatXuatKho);

        dangKyPhimTat("XK_ESC", KeyEvent.VK_ESCAPE, 0, this::dongManHinhXuatKho);
    }
    private void dangKyPhimTat(String actionKey, int keyCode, int modifiers, Runnable action) {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(keyCode, modifiers), actionKey);

        actionMap.put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (action != null) {
                    action.run();
                }
            }
        });
    }

    private void dongManHinhXuatKho() {
        if (popupSuggest != null && popupSuggest.isVisible()) {
            popupSuggest.setVisible(false);
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(this);

        if (owner instanceof JDialog) {
            owner.dispose();
        } else {
            resetFormToanBo();
        }
    }
    
    private JPanel createHeader() {
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        pnlHeader.setBackground(PRIMARY_DARK);
        pnlHeader.setPreferredSize(new Dimension(0, 58));

        JLabel lblIcon = new JLabel(new MenuIcon("DOCUMENT", 22, Color.WHITE));
        lblIcon.setBorder(new EmptyBorder(0, 4, 0, 6));
        pnlHeader.add(lblIcon);

        JLabel lblTitle = new JLabel("PHIẾU XUẤT KHO / XUẤT HỦY");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle);

        return pnlHeader;
    }

    private JPanel createMainContent() {
        JPanel pnlContent = new JPanel(new BorderLayout());
        pnlContent.setBackground(BG_APP);

        JPanel pnlLeft = createInputForm();
        pnlLeft.setPreferredSize(new Dimension(400, 0));

        JPanel pnlRight = createCartTable();

        pnlContent.add(pnlLeft, BorderLayout.WEST);
        pnlContent.add(pnlRight, BorderLayout.CENTER);

        return pnlContent;
    }

    private JPanel createInputForm() {
        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setOpaque(false);
        pnlWrapper.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        JPanel pnlInput = new JPanel(new GridBagLayout());
        pnlInput.setOpaque(false);
        pnlInput.setBorder(new EmptyBorder(14, 18, 14, 18));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 12, 0);

        txtMaLo = createTextField();
        txtMaLo.setPreferredSize(new Dimension(0, 36));

        setupAutoSuggest();

        JButton btnKiemTra = createOutlineButton("Kiểm tra [F3]", "CHECK_CIRCLE", TEXT_PRIMARY);
        btnKiemTra.setPreferredSize(new Dimension(120, 36));
        btnKiemTra.setToolTipText("Phím tắt: F3");
        btnKiemTra.addActionListener(e -> kiemTraMaLo());

        JButton btnScanQR = createHoverButton("Quét QR [F2]", "SEARCH", PRIMARY_BLUE, PRIMARY_HOVER, Color.WHITE);
        btnScanQR.setPreferredSize(new Dimension(125, 36));
        btnScanQR.setToolTipText("Phím tắt: F2");
        btnScanQR.addActionListener(e -> showQRScannerDialog());

        JPanel pnlMaLoBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlMaLoBtns.setOpaque(false);
        pnlMaLoBtns.add(btnKiemTra);
        pnlMaLoBtns.add(btnScanQR);

        JPanel pnlMaLo = new JPanel(new BorderLayout(8, 0));
        pnlMaLo.setOpaque(false);
        pnlMaLo.add(txtMaLo, BorderLayout.CENTER);
        pnlMaLo.add(pnlMaLoBtns, BorderLayout.EAST);

        gbc.gridy = 0;
        pnlInput.add(createFormGroup("Nhập / Chọn Mã Lô hàng", "BARCODE", pnlMaLo), gbc);

        gbc.gridy = 1;
        pnlInput.add(createInfoBox(), gbc);

        txtSoLuong = createTextField();
        txtSoLuong.setPreferredSize(new Dimension(0, 36));
        txtSoLuong.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    themVaoDanhSach();
                }
            }
        });

        JButton btnMax = createOutlineButton("MAX [F4]", null, PRIMARY_BLUE);
        btnMax.setPreferredSize(new Dimension(90, 36));
        btnMax.setToolTipText("Phím tắt: F4");
        btnMax.addActionListener(e -> chucNangXuatMax());

        JPanel pnlSLInput = new JPanel(new BorderLayout(5, 0));
        pnlSLInput.setOpaque(false);
        pnlSLInput.add(txtSoLuong, BorderLayout.CENTER);
        pnlSLInput.add(btnMax, BorderLayout.EAST);

        cbDonVi = new JComboBox<>();
        cbDonVi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbDonVi.setBackground(Color.WHITE);
        cbDonVi.setPreferredSize(new Dimension(105, 36));

        JPanel pnlSL_DV = new JPanel(new BorderLayout(12, 0));
        pnlSL_DV.setOpaque(false);
        pnlSL_DV.add(createFormGroup("Số lượng xuất", "PACKAGE", pnlSLInput), BorderLayout.CENTER);
        pnlSL_DV.add(createFormGroup("Đơn vị", "PILL", cbDonVi), BorderLayout.EAST);

        gbc.gridy = 2;
        pnlInput.add(pnlSL_DV, gbc);

        cbLyDo = new JComboBox<>(new String[] {
                "Bán hàng",
                "Xuất hủy hàng hỏng/lỗi",
                "Xuất trả nhà cung cấp",
                "Xuất tiêu hao nội bộ",
                "Khác"
        });
        cbLyDo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbLyDo.setBackground(Color.WHITE);
        cbLyDo.setPreferredSize(new Dimension(0, 36));

        gbc.gridy = 3;
        pnlInput.add(createFormGroup("Lý do xuất", "LIST", cbLyDo), gbc);

        txtGhiChu = createTextField();
        txtGhiChu.setPreferredSize(new Dimension(0, 36));

        gbc.gridy = 4;
        pnlInput.add(createFormGroup("Ghi chú", "EDIT", txtGhiChu), gbc);

        JButton btnAdd = createHoverButton("Thêm vào phiếu [F5]", "ADD", PRIMARY_BLUE, PRIMARY_HOVER, Color.WHITE);
        btnAdd.setPreferredSize(new Dimension(0, 38));
        btnAdd.setToolTipText("Phím tắt: F5");
        btnAdd.addActionListener(e -> themVaoDanhSach());

        gbc.gridy = 5;
        gbc.insets = new Insets(2, 0, 0, 0);
        pnlInput.add(btnAdd, gbc);

        JScrollPane scroll = new JScrollPane(pnlInput);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        pnlWrapper.add(scroll, BorderLayout.CENTER);
        return pnlWrapper;
    }

    private JPanel createInfoBox() {
        JPanel pnlInfoBox = new JPanel(new GridBagLayout());
        pnlInfoBox.setBackground(BG_SOFT);
        pnlInfoBox.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 1, 8),
                new EmptyBorder(10, 12, 10, 12)));

        lblTenSP = createInfoValue();
        lblKhoHang = createInfoValue();
        lblTonKhoHienTai = createInfoValue();

        addInfoRow(pnlInfoBox, 0, "Sản phẩm:", "MEDICAL_TOOL", lblTenSP);
        addInfoRow(pnlInfoBox, 1, "Kho hàng:", "BOX", lblKhoHang);
        addInfoRow(pnlInfoBox, 2, "Tồn kho:", "PACKAGE", lblTonKhoHienTai);

        return pnlInfoBox;
    }

    private JLabel createInfoValue() {
        JLabel lbl = new JLabel("-- Chưa chọn --");
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }

    private void addInfoRow(JPanel panel, int row, String title, String icon, JLabel valueLabel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(TEXT_SECONDARY);
        lblTitle.setIcon(new MenuIcon(icon, 15, TEXT_SECONDARY));
        lblTitle.setIconTextGap(7);

        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.ipadx = 8;
        panel.add(lblTitle, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(valueLabel, gbc);
    }

    private boolean laLoCoTheXuat(LoHang loHang) {
        if (loHang == null) {
            return false;
        }

        if (loHang.getSoLuongLoHang() <= 0) {
            return false;
        }

        if (loHang.getTrangThai() == null) {
            return true;
        }

        String trangThai = loHang.getTrangThai().toString().trim().toUpperCase();

        /*
         * Tuyệt đối không dùng contains("AN")
         * vì CON_HANG cũng chứa chữ "AN".
         */
        if ("AN".equals(trangThai)
                || "ẨN".equals(trangThai)
                || "HET_HANG".equals(trangThai)
                || "HẾT_HÀNG".equals(trangThai)
                || "HET HANG".equals(trangThai)
                || "HẾT HÀNG".equals(trangThai)) {
            return false;
        }

        return true;
    }
    
    private void setupAutoSuggest() {
        popupSuggest = new JPopupMenu();
        modelSuggest = new DefaultListModel<>();
        listSuggest = new JList<>(modelSuggest);
        listSuggest.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listSuggest.setSelectionBackground(new Color(239, 246, 255));
        listSuggest.setSelectionForeground(TEXT_PRIMARY);
        listSuggest.setFixedCellHeight(28);

        scrollSuggest = new JScrollPane(listSuggest);
        scrollSuggest.setBorder(null);
        scrollSuggest.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        popupSuggest.add(scrollSuggest);
        popupSuggest.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE));

        txtMaLo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSuggest();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSuggest();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSuggest();
            }
        });

        listSuggest.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 || e.getClickCount() == 2) {
                    chonTuSuggest();
                }
            }
        });

        txtMaLo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN && popupSuggest.isVisible()) {
                    listSuggest.setSelectedIndex(0);
                    listSuggest.requestFocus();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (popupSuggest.isVisible() && listSuggest.getSelectedIndex() != -1) {
                        chonTuSuggest();
                    } else {
                        popupSuggest.setVisible(false);
                        kiemTraMaLo();
                    }
                }
            }
        });

        listSuggest.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    chonTuSuggest();
                }
            }
        });
    }

    private void chonTuSuggest() {
        int index = listSuggest.getSelectedIndex();

        if (index < 0 || index >= dsSuggestLo.size()) {
            return;
        }

        LoHang loDuocChon = dsSuggestLo.get(index);

        if (loDuocChon == null) {
            return;
        }

        isProgrammaticUpdate = true;
        txtMaLo.setText(loDuocChon.getSoLoHang());
        popupSuggest.setVisible(false);
        isProgrammaticUpdate = false;

        hienThiThongTinLoHang(loDuocChon, null);
    }

    private void updateSuggest() {
        if (isProgrammaticUpdate) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            String text = txtMaLo.getText().trim().toLowerCase();

            modelSuggest.clear();
            dsSuggestLo.clear();

            if (text.isEmpty()) {
                popupSuggest.setVisible(false);
                return;
            }

            for (LoHang lh : cacheDanhSachLo) {
            	if (!laLoCoTheXuat(lh)) {
            	    continue;
            	}

                String maLo = lh.getSoLoHang() != null ? lh.getSoLoHang().toLowerCase() : "";
                String tenSP = lh.getSanPhamId() != null && lh.getSanPhamId().getTen() != null
                        ? lh.getSanPhamId().getTen().toLowerCase()
                        : "";
                String kho = layKhoTuLoHang(lh).toLowerCase();
                String maVachNoiBo = lh.getMaVachNoiBo() != null ? lh.getMaVachNoiBo().toLowerCase() : "";

                if (maLo.contains(text) || tenSP.contains(text) || kho.contains(text) || maVachNoiBo.contains(text)) {
                    String tenSanPham = lh.getSanPhamId() != null && lh.getSanPhamId().getTen() != null
                            ? lh.getSanPhamId().getTen()
                            : "Không rõ sản phẩm";

                    String hienThi = lh.getSoLoHang()
                            + " - "
                            + tenSanPham
                            + " - "
                            + layKhoTuLoHang(lh);

                    modelSuggest.addElement(hienThi);
                    dsSuggestLo.add(lh);
                }
            }

            int count = modelSuggest.getSize();

            if (count > 0) {
                int displayCount = Math.min(count, 5);
                listSuggest.setVisibleRowCount(displayCount);

                int exactHeight = displayCount * 28;
                scrollSuggest.setPreferredSize(new Dimension(390, exactHeight));

                popupSuggest.pack();
                popupSuggest.show(txtMaLo, 0, txtMaLo.getHeight());
                txtMaLo.requestFocus();
            } else {
                popupSuggest.setVisible(false);
            }
        });
    }

    private void chucNangXuatMax() {
        if (currentSanPham == null || currentTonKho <= 0) {
            return;
        }

        String tenDVChon = cbDonVi.getSelectedItem() != null ? cbDonVi.getSelectedItem().toString() : "";
        DonViDoLuong dvChon = null;

        for (DonViDoLuong dv : currentDsDonVi) {
            if (dv.getTen().equals(tenDVChon)) {
                dvChon = dv;
                break;
            }
        }

        if (dvChon == null) {
            return;
        }

        int tongDaCoTrongGio = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String loHangIdTrongGio = tableModel.getValueAt(i, 9).toString();

            if (loHangIdTrongGio.equalsIgnoreCase(currentLoHangId)) {
                tongDaCoTrongGio += Integer.parseInt(tableModel.getValueAt(i, 8).toString());
            }
        }

        int conLai = currentTonKho - tongDaCoTrongGio;

        if (conLai > 0) {
            int maxSL = (int) (conLai / dvChon.getChuyenDoiSangDonViCoBan());
            txtSoLuong.setText(String.valueOf(maxSL));
        } else {
            txtSoLuong.setText("0");
        }
    }

    private JPanel createFormGroup(String title, String iconName, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_PRIMARY);

        if (iconName != null) {
            lbl.setIcon(new MenuIcon(iconName, 17, PRIMARY_BLUE));
            lbl.setIconTextGap(8);
        }

        p.add(lbl, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JTextField createTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setPreferredSize(new Dimension(0, 36));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(4, 9, 4, 9)));
        return txt;
    }

    private JButton createOutlineButton(String text, String iconName, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(color);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedLineBorder(color, 1, 6));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (iconName != null) {
            btn.setIcon(new MenuIcon(iconName, 15, color));
            btn.setIconTextGap(5);
        }

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(color);
                btn.setForeground(Color.WHITE);

                if (iconName != null) {
                    btn.setIcon(new MenuIcon(iconName, 15, Color.WHITE));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
                btn.setForeground(color);

                if (iconName != null) {
                    btn.setIcon(new MenuIcon(iconName, 15, color));
                }
            }
        });

        return btn;
    }

    private JButton createHoverButton(String text, String iconName, Color bg, Color hoverBg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedLineBorder(bg, 1, 6));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (iconName != null) {
            btn.setIcon(new MenuIcon(iconName, 17, fg));
            btn.setIconTextGap(5);
        }

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });

        return btn;
    }

    private JPanel createCartTable() {
        JPanel pnlWrapper = new JPanel(new BorderLayout(0, 12));
        pnlWrapper.setOpaque(false);
        pnlWrapper.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel pnlTableContainer = new JPanel(new BorderLayout());
        pnlTableContainer.setBackground(Color.WHITE);

        JPanel pnlTableHeader = new JPanel(new BorderLayout());
        pnlTableHeader.setBackground(new Color(241, 245, 249));
        pnlTableHeader.setBorder(new EmptyBorder(10, 15, 10, 15));

        lblTableTitle = new JLabel("DANH SÁCH CHỜ XUẤT (0)");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTableTitle.setForeground(TEXT_PRIMARY);
        lblTableTitle.setIcon(new MenuIcon("CART", 18, PRIMARY_BLUE));
        lblTableTitle.setIconTextGap(8);
        pnlTableHeader.add(lblTableTitle, BorderLayout.WEST);

        String[] cols = {
                "Mã lô",
                "Kho",
                "Tên Sản phẩm",
                "SL",
                "ĐVT",
                "Lý do",
                "Ghi chú",
                "Xóa",
                "SL_QuyDoi_Hidden",
                "LO_HANG_ID_HIDDEN"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_COLOR);

        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(105);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(48);
        table.getColumnModel().getColumn(4).setPreferredWidth(58);
        table.getColumnModel().getColumn(5).setPreferredWidth(132);
        table.getColumnModel().getColumn(6).setPreferredWidth(130);
        table.getColumnModel().getColumn(7).setPreferredWidth(42);

        table.getColumnModel().getColumn(8).setMinWidth(0);
        table.getColumnModel().getColumn(8).setMaxWidth(0);
        table.getColumnModel().getColumn(8).setWidth(0);
        table.getColumnModel().getColumn(9).setMinWidth(0);
        table.getColumnModel().getColumn(9).setMaxWidth(0);
        table.getColumnModel().getColumn(9).setWidth(0);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(Color.WHITE);
        header.setForeground(TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, BORDER_COLOR));

        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {
                JLabel lbl = new JLabel(new MenuIcon("TRASH", 18, DANGER));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                return lbl;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                if (row >= 0 && col == 7) {
                    tableModel.removeRow(row);
                    updateTableTitle();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);

        pnlTableContainer.add(pnlTableHeader, BorderLayout.NORTH);
        pnlTableContainer.add(scroll, BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        pnlBottom.setOpaque(false);

        JButton btnLamMoi = createOutlineButton("Hủy / Làm mới [F6]", "REFRESH", TEXT_SECONDARY);
        btnLamMoi.setPreferredSize(new Dimension(190, 40));
        btnLamMoi.setToolTipText("Phím tắt: F6");
        btnLamMoi.addActionListener(e -> resetFormToanBo());

        JButton btnXacNhan = new JButton("Xác nhận xuất kho [F10]");
        btnXacNhan.setToolTipText("Phím tắt: F10");
        btnXacNhan.setPreferredSize(new Dimension(235, 40));
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setBackground(new Color(203, 213, 225));
        btnXacNhan.setFocusPainted(false);
        btnXacNhan.setBorder(new RoundedLineBorder(new Color(203, 213, 225), 1, 6));
        btnXacNhan.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnXacNhan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (tableModel.getRowCount() > 0) {
                    btnXacNhan.setBackground(ORANGE_HOVER);
                } else {
                    btnXacNhan.setBackground(new Color(148, 163, 184));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (tableModel.getRowCount() > 0) {
                    btnXacNhan.setBackground(ORANGE_BTN);
                } else {
                    btnXacNhan.setBackground(new Color(203, 213, 225));
                }
            }
        });

        btnXacNhan.addActionListener(e -> hoanTatXuatKho());

        tableModel.addTableModelListener(e -> {
            if (tableModel.getRowCount() > 0) {
                btnXacNhan.setBackground(ORANGE_BTN);
                btnXacNhan.setBorder(new RoundedLineBorder(ORANGE_BTN, 1, 6));
            } else {
                btnXacNhan.setBackground(new Color(203, 213, 225));
                btnXacNhan.setBorder(new RoundedLineBorder(new Color(203, 213, 225), 1, 6));
            }
        });

        pnlBottom.add(btnLamMoi);
        pnlBottom.add(btnXacNhan);

        pnlWrapper.add(pnlTableContainer, BorderLayout.CENTER);
        pnlWrapper.add(pnlBottom, BorderLayout.SOUTH);

        return pnlWrapper;
    }

    private void updateTableTitle() {
        lblTableTitle.setText("DANH SÁCH CHỜ XUẤT (" + tableModel.getRowCount() + ")");
    }

    public void showQRScannerDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);

        if (owner == null) {
            return;
        }

        JDialog dialog = new JDialog(owner, "YÊU CẦU QUÉT MÃ", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(450, 180);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setShape(new RoundRectangle2D.Double(0, 0, dialog.getWidth(), dialog.getHeight(), 16, 16));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 2));

        JLabel lblIconScan = new JLabel(new MenuIcon("XUAT_KHO", 40, PRIMARY_BLUE));
        lblIconScan.setHorizontalAlignment(SwingConstants.CENTER);
        lblIconScan.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel lblInfo = new JLabel("Vui lòng đưa súng quét đọc mã trên tem...", SwingConstants.CENTER);
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblInfo.setForeground(TEXT_PRIMARY);

        JButton btnClose = createOutlineButton("Nhập tay", "EDIT", TEXT_SECONDARY);
        btnClose.setPreferredSize(new Dimension(130, 38));
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
                txtMaLo.setText(qrData);
                kiemTraMaLo();
            }
        });

        dialog.addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
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
    private void hienThiThongTinLoHang(LoHang loTimThay, DonViDoLuong dvQuetDuoc) {
        if (loTimThay == null) {
            resetThongTinSP();
            return;
        }

        currentLoHang = loTimThay;
        currentLoHangId = loTimThay.getId() == null ? "" : loTimThay.getId();

        isProgrammaticUpdate = true;
        txtMaLo.setText(loTimThay.getSoLoHang());
        isProgrammaticUpdate = false;

        lblTenSP.setText(loTimThay.getSanPhamId().getTen());
        lblTenSP.setForeground(PRIMARY_BLUE);
        lblTenSP.setFont(new Font("Segoe UI", Font.BOLD, 13));

        currentKhoHienThi = layKhoTuLoHang(loTimThay);
        lblKhoHang.setText(currentKhoHienThi);
        lblKhoHang.setForeground(PRIMARY_BLUE);
        lblKhoHang.setFont(new Font("Segoe UI", Font.BOLD, 13));

        currentTonKho = loTimThay.getSoLuongLoHang();
        currentSanPham = loTimThay.getSanPhamId();

        lblTonKhoHienTai.setText(formatTonKhoHienThi(currentSanPham.getId(), currentTonKho));

        if (currentTonKho <= 0) {
            lblTonKhoHienTai.setForeground(DANGER);
        } else {
            lblTonKhoHienTai.setForeground(PRIMARY_BLUE);
        }

        lblTonKhoHienTai.setFont(new Font("Segoe UI", Font.BOLD, 13));

        cbDonVi.removeAllItems();
        currentDsDonVi = busDonVi.getDSTheoMaSP(currentSanPham.getId());

        if (currentDsDonVi != null) {
            for (DonViDoLuong dv : currentDsDonVi) {
                cbDonVi.addItem(dv.getTen());
            }
        }

        if (dvQuetDuoc != null) {
            cbDonVi.setSelectedItem(dvQuetDuoc.getTen());
        } else if (currentDsDonVi != null && !currentDsDonVi.isEmpty()) {
            DonViDoLuong dvCoBan = currentDsDonVi.get(0);

            for (DonViDoLuong d : currentDsDonVi) {
                if (d.getChuyenDoiSangDonViCoBan() == 1.0) {
                    dvCoBan = d;
                    break;
                }
            }

            cbDonVi.setSelectedItem(dvCoBan.getTen());
        }

        txtSoLuong.setText("1");
        txtSoLuong.requestFocus();
        txtSoLuong.selectAll();
    }
    
    private void kiemTraMaLo() {
        isProgrammaticUpdate = true;

        if (popupSuggest != null) {
            popupSuggest.setVisible(false);
        }

        isProgrammaticUpdate = false;

        String maQuet = txtMaLo.getText() == null ? "" : txtMaLo.getText().trim();

        if (maQuet.isEmpty()) {
            return;
        }

        List<LoHang> dsLoKhop = timLoHangTheoMaNhap(maQuet);

        if (dsLoKhop == null || dsLoKhop.isEmpty()) {
            showCustomNotification(
                    "Từ chối thao tác",
                    "Mã vạch lạ! Thuốc chưa được nhập kho hoặc không tồn tại mã: " + maQuet,
                    "ERROR"
            );
            resetThongTinSP();
            isProgrammaticUpdate = true;
            txtMaLo.setText("");
            isProgrammaticUpdate = false;
            txtMaLo.requestFocus();
            
            return;
        }

        if (dsLoKhop.size() > 1) {
            modelSuggest.clear();
            dsSuggestLo.clear();

            for (LoHang lh : dsLoKhop) {
                if (lh == null) {
                    continue;
                }

                String tenSanPham = "Không rõ sản phẩm";

                if (lh.getSanPhamId() != null && lh.getSanPhamId().getTen() != null) {
                    tenSanPham = lh.getSanPhamId().getTen();
                }

                modelSuggest.addElement(
                        (lh.getSoLoHang() == null ? "" : lh.getSoLoHang())
                                + " - "
                                + tenSanPham
                                + " - "
                                + layKhoTuLoHang(lh)
                );
                dsSuggestLo.add(lh);
            }

            int displayCount = Math.min(dsSuggestLo.size(), 5);
            listSuggest.setVisibleRowCount(displayCount);
            scrollSuggest.setPreferredSize(new Dimension(390, displayCount * 28));

            popupSuggest.pack();
            popupSuggest.show(txtMaLo, 0, txtMaLo.getHeight());

            if (listSuggest.getModel().getSize() > 0) {
                listSuggest.setSelectedIndex(0);
            }

            txtMaLo.requestFocus();

            showCustomNotification(
                    "Trùng mã lô",
                    "Mã này đang khớp nhiều lô / nhiều kho.\nVui lòng chọn đúng dòng kho cần xuất trong danh sách gợi ý.",
                    "WARNING"
            );
            return;
        }

        hienThiThongTinLoHang(dsLoKhop.get(0), null);
    }
    private List<LoHang> timLoHangTheoMaNhap(String maNhap) {
        List<LoHang> dsLoKhop = new ArrayList<>();

        if (maNhap == null || maNhap.trim().isEmpty()) {
            return dsLoKhop;
        }

        String key = maNhap.trim();

        /*
         * ƯU TIÊN 1:
         * Query trực tiếp DB qua BUS.
         * Cách này bắt được mã vạch nội bộ MVLH0001 chắc hơn cache.
         */
        try {
            List<LoHang> dsTuDB = busKho.timLoHangChoXuatKho(key);

            if (dsTuDB != null) {
                for (LoHang lh : dsTuDB) {
                    if (laLoCoTheXuat(lh)) {
                        dsLoKhop.add(lh);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!dsLoKhop.isEmpty()) {
            return dsLoKhop;
        }

        /*
         * ƯU TIÊN 2:
         * Nếu DB direct chưa ra thì reload cache đồng bộ rồi tìm lại.
         */
        try {
            reloadCacheLoHangCoTheXuatSync();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DonViDoLuong dvQuetDuoc = null;

        try {
            dvQuetDuoc = busDonVi.layDonViTheoMaVach(key);
        } catch (Exception ignored) {
        }

        for (LoHang lh : cacheDanhSachLo) {
            if (!laLoCoTheXuat(lh)) {
                continue;
            }

            boolean match = false;

            if (dvQuetDuoc != null) {
                if (lh.getSanPhamId() != null
                        && dvQuetDuoc.getSanPhamId() != null
                        && lh.getSanPhamId().getId() != null
                        && dvQuetDuoc.getSanPhamId().getId() != null
                        && lh.getSanPhamId().getId().equalsIgnoreCase(dvQuetDuoc.getSanPhamId().getId())) {
                    match = true;
                }
            } else {
                boolean khopIdLo = lh.getId() != null
                        && lh.getId().trim().equalsIgnoreCase(key);

                boolean khopSoLo = lh.getSoLoHang() != null
                        && lh.getSoLoHang().trim().equalsIgnoreCase(key);

                boolean khopMaVachNoiBo = lh.getMaVachNoiBo() != null
                        && lh.getMaVachNoiBo().trim().equalsIgnoreCase(key);

                boolean khopMaSP = lh.getSanPhamId() != null
                        && lh.getSanPhamId().getId() != null
                        && lh.getSanPhamId().getId().trim().equalsIgnoreCase(key);

                if (khopIdLo || khopSoLo || khopMaVachNoiBo || khopMaSP) {
                    match = true;
                }
            }

            if (match) {
                dsLoKhop.add(lh);
            }
        }

        return dsLoKhop;
    }
    private String layKhoTuLoHang(LoHang loHang) {
        if (loHang == null) {
            return "Không xác định";
        }

        try {
            Object kho = loHang.getKhoHangId();

            if (kho == null) {
                return "Không xác định";
            }

            String tenKho = goiGetterNeuCo(kho, "getTenKho");
            if (!tenKho.isEmpty()) {
                return tenKho;
            }

            tenKho = goiGetterNeuCo(kho, "getTenKhoHang");
            if (!tenKho.isEmpty()) {
                return tenKho;
            }

            tenKho = goiGetterNeuCo(kho, "getTen");
            if (!tenKho.isEmpty()) {
                return tenKho;
            }

            tenKho = goiGetterNeuCo(kho, "getId");
            if (!tenKho.isEmpty()) {
                return tenKho;
            }

            return kho.toString();

        } catch (Exception e) {
            return "Không xác định";
        }
    }

    private String goiGetterNeuCo(Object obj, String methodName) {
        try {
            Method method = obj.getClass().getMethod(methodName);
            Object value = method.invoke(obj);

            if (value == null) {
                return "";
            }

            return value.toString().trim();

        } catch (Exception e) {
            return "";
        }
    }

    private String formatTonKhoHienThi(String maSP, int soLuongCoBan) {
        List<DonViDoLuong> ds = busDonVi.getDSTheoMaSP(maSP);

        if (ds == null || ds.isEmpty()) {
            return soLuongCoBan + " Đơn vị";
        }

        DonViDoLuong dvLonNhat = ds.get(0);
        DonViDoLuong dvCoBan = ds.get(0);

        for (DonViDoLuong dv : ds) {
            if (dv.getChuyenDoiSangDonViCoBan() == 1.0) {
                dvCoBan = dv;
            }

            if (dv.getChuyenDoiSangDonViCoBan() > dvLonNhat.getChuyenDoiSangDonViCoBan()) {
                dvLonNhat = dv;
            }
        }

        int heSoQuyDoi = (int) dvLonNhat.getChuyenDoiSangDonViCoBan();

        if (heSoQuyDoi <= 1 || soLuongCoBan == 0) {
            return soLuongCoBan + " " + dvCoBan.getTen();
        }

        int soLuongLon = soLuongCoBan / heSoQuyDoi;
        int soLuongLe = soLuongCoBan % heSoQuyDoi;

        String ketQua = "";

        if (soLuongLon > 0) {
            ketQua += soLuongLon + " " + dvLonNhat.getTen();
        }

        if (soLuongLe > 0) {
            if (!ketQua.isEmpty()) {
                ketQua += ", ";
            }

            ketQua += soLuongLe + " " + dvCoBan.getTen();
        }

        return ketQua;
    }

    private void themVaoDanhSach() {
        if (lblTenSP.getText().equals("-- Chưa chọn --") || cbDonVi.getSelectedItem() == null) {
            showCustomNotification("Chưa có dữ liệu", "Vui lòng Quét đúng Mã lô trước.", "WARNING");
            return;
        }

        try {
            int slXuatNhapVao = Integer.parseInt(txtSoLuong.getText().trim());

            if (slXuatNhapVao <= 0) {
                showCustomNotification("Lỗi nhập liệu", "Số lượng xuất kho phải lớn hơn 0.", "ERROR");
                return;
            }

            String tenDVChon = cbDonVi.getSelectedItem().toString();
            DonViDoLuong dvChon = null;

            for (DonViDoLuong dv : currentDsDonVi) {
                if (dv.getTen().equals(tenDVChon)) {
                    dvChon = dv;
                    break;
                }
            }

            if (dvChon == null) {
                return;
            }

            int slXuatQuyDoi = (int) (slXuatNhapVao * dvChon.getChuyenDoiSangDonViCoBan());
            int tongDaCoTrongGio = 0;
            String maLoHienTai = txtMaLo.getText().trim();

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String loHangIdTrongGio = tableModel.getValueAt(i, 9).toString();

                if (loHangIdTrongGio.equalsIgnoreCase(currentLoHangId)) {
                    tongDaCoTrongGio += Integer.parseInt(tableModel.getValueAt(i, 8).toString());
                }
            }

            if ((slXuatQuyDoi + tongDaCoTrongGio) > currentTonKho) {
                int conLaiCoTheXuat = currentTonKho - tongDaCoTrongGio;

                String tenDVCoBan = "Đơn vị";

                if (currentDsDonVi != null) {
                    for (DonViDoLuong dv : currentDsDonVi) {
                        if (dv.getChuyenDoiSangDonViCoBan() == 1.0) {
                            tenDVCoBan = dv.getTen();
                            break;
                        }
                    }
                }

                String msg = "Số lượng xuất lố tồn kho gốc!\n\n";
                msg += "• Kho còn: " + currentTonKho + " " + tenDVCoBan + "\n";
                msg += "• Đang chờ xuất: " + tongDaCoTrongGio + " " + tenDVCoBan + "\n";

                if (conLaiCoTheXuat > 0) {
                    msg += "• Chỉ có thể xuất thêm tối đa: " + conLaiCoTheXuat + " " + tenDVCoBan + ".";
                } else {
                    msg += "• Lô này đã được vét sạch vào giỏ.";
                }

                showCustomNotification("Tồn kho không đủ", msg, "ERROR");
                return;
            }

            tableModel.addRow(new Object[] {
                    maLoHienTai,
                    currentKhoHienThi,
                    lblTenSP.getText(),
                    slXuatNhapVao,
                    dvChon.getTen(),
                    cbLyDo.getSelectedItem().toString(),
                    txtGhiChu.getText().trim(),
                    "",
                    slXuatQuyDoi,
                    currentLoHangId
            });

            updateTableTitle();
            resetFormNhap();
            txtMaLo.requestFocus();

        } catch (NumberFormatException e) {
            showCustomNotification("Sai định dạng", "Vui lòng nhập số nguyên hợp lệ.", "ERROR");
        }
    }

    private void hoanTatXuatKho() {
        if (tableModel.getRowCount() == 0) {
            showCustomNotification("Danh sách trống", "Chưa có lô hàng nào trong danh sách.", "WARNING");
            return;
        }

        boolean confirmed = showCustomConfirmDialog(
                "XÁC NHẬN XUẤT KHO",
                "Xuất <b>" + tableModel.getRowCount()
                        + "</b> lô hàng. Hệ thống sẽ trừ trực tiếp vào kho hiện tại.<br>Bạn chắc chắn thực hiện?"
        );

        if (!confirmed) {
            return;
        }

        List<Object[]> ds = new ArrayList<>();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            ds.add(new Object[]{
                    tableModel.getValueAt(i, 9), // 0 - loHangId để BUS/DAO xử lý
                    tableModel.getValueAt(i, 8), // 1 - số lượng quy đổi
                    tableModel.getValueAt(i, 5), // 2 - lý do xuất

                    tableModel.getValueAt(i, 0), // 3 - mã lô hiển thị
                    tableModel.getValueAt(i, 1), // 4 - kho hiển thị
                    tableModel.getValueAt(i, 2), // 5 - tên sản phẩm
                    tableModel.getValueAt(i, 3), // 6 - số lượng nhập
                    tableModel.getValueAt(i, 4), // 7 - đơn vị
                    tableModel.getValueAt(i, 6)  // 8 - ghi chú
            });
        }

        String maNhanVienDangNhap = SessionDangNhap.getMaNhanVienOrDefault();

        BUS_Kho.KetQuaXuatKho ketQua = busKho.xuatHuyKhoVaTaoPhieu(ds, maNhanVienDangNhap);

        if (!ketQua.isThanhCong()) {
            showCustomNotification("THẤT BẠI", ketQua.getThongBao(), "ERROR");
            return;
        }

        showCustomNotification("THÀNH CÔNG", ketQua.getThongBao(), "SUCCESS");

        String tenHienThi = "KIỆN HÀNG XUẤT (" + tableModel.getRowCount() + " Món)";

        xemTruocPhieuXuatKho(
                ketQua.getMaPhieuXuat(),
                ketQua.getNgayXuat(),
                ketQua.getQrData(),
                tenHienThi
        );

        reloadCacheLoHangCoTheXuat();
        resetFormToanBo();
    }
    private void xemTruocPhieuXuatKho(
            String maPhieuXuat,
            String ngayXuat,
            String qrData,
            String tenHienThi
    ) {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Xem trước phiếu xuất",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(new Color(30, 64, 105), 2));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 64, 105));
        header.setBorder(new EmptyBorder(14, 20, 14, 18));

        JLabel lblTitle = new JLabel("XEM TRƯỚC PHIẾU XUẤT / HỦY KHO");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JButton btnX = new JButton("×");
        btnX.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btnX.setForeground(Color.WHITE);
        btnX.setFocusPainted(false);
        btnX.setBorderPainted(false);
        btnX.setContentAreaFilled(false);
        btnX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnX.addActionListener(e -> dialog.dispose());

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnX, BorderLayout.EAST);

        JTextArea txtPreview = new JTextArea();
        txtPreview.setEditable(false);
        txtPreview.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtPreview.setForeground(TEXT_PRIMARY);
        txtPreview.setBackground(Color.WHITE);
        txtPreview.setMargin(new Insets(18, 22, 18, 22));
        txtPreview.setText(buildNoiDungPhieuXuatPreview(maPhieuXuat, ngayXuat));
        txtPreview.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(txtPreview);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnDong = new JButton("Đóng");
        btnDong.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnDong.setForeground(new Color(100, 116, 139));
        btnDong.setBackground(Color.WHITE);
        btnDong.setFocusPainted(false);
        btnDong.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(9, 20, 9, 20)
        ));
        btnDong.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDong.setPreferredSize(new Dimension(110, 40));
        btnDong.addActionListener(e -> dialog.dispose());

        JButton btnIn = new JButton("In phiếu");
        btnIn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnIn.setForeground(Color.WHITE);
        btnIn.setBackground(new Color(30, 64, 105));
        btnIn.setFocusPainted(false);
        btnIn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btnIn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnIn.setPreferredSize(new Dimension(130, 40));
        btnIn.addActionListener(e -> {
            dialog.dispose();

            Window owner = SwingUtilities.getWindowAncestor(this);

            DialogInQRCode dialogQR = new DialogInQRCode(
                    owner,
                    qrData,
                    maPhieuXuat,
                    "Ngày: " + ngayXuat,
                    tenHienThi
            );

            dialogQR.setVisible(true);
        });

        footer.add(btnDong);
        footer.add(btnIn);

        root.add(header, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setSize(700, 620);
        dialog.setShape(new RoundRectangle2D.Double(0, 0, dialog.getWidth(), dialog.getHeight(), 16, 16));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    private String buildNoiDungPhieuXuatPreview(String maPhieuXuat, String ngayXuat) {
        StringBuilder sb = new StringBuilder();

        sb.append("MYCARE PHARMACY\n");
        sb.append("============================================================\n");
        sb.append("                 PHIẾU XUẤT / HỦY KHO\n");
        sb.append("============================================================\n\n");

        sb.append("Mã phiếu xuất : ").append(maPhieuXuat).append("\n");
        sb.append("Ngày xuất     : ").append(ngayXuat).append("\n");
        sb.append("Số dòng xuất  : ").append(tableModel.getRowCount()).append("\n\n");

        sb.append(String.format("%-4s %-16s %-10s %-28s %-8s %-12s\n",
                "STT", "Mã lô", "Kho", "Sản phẩm", "SL", "Lý do"));

        sb.append("------------------------------------------------------------\n");

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String maLo = safeCell(i, 0);
            String kho = safeCell(i, 1);
            String tenSP = safeCell(i, 2);
            String soLuong = safeCell(i, 3);
            String lyDo = safeCell(i, 5);
            String ghiChu = safeCell(i, 6);

            sb.append(String.format("%-4s %-16s %-10s %-28s %-8s %-12s\n",
                    i + 1,
                    catChuoiPreview(maLo, 15),
                    catChuoiPreview(kho, 9),
                    catChuoiPreview(tenSP, 27),
                    soLuong,
                    catChuoiPreview(lyDo, 11)
            ));
        }

        sb.append("\n============================================================\n");
        sb.append("Người lập phiếu       Dược sũ phụ trách        Quản lý\n\n\n");
        sb.append("(Ký, ghi rõ họ tên)       (Ký tên)              (Ký tên)\n");

        return sb.toString();
    }

    private String safeCell(int row, int col) {
        try {
            Object value = tableModel.getValueAt(row, col);
            return value == null ? "" : value.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String catChuoiPreview(String text, int max) {
        if (text == null) {
            return "";
        }

        String value = text.trim();

        if (value.length() <= max) {
            return value;
        }

        return value.substring(0, Math.max(0, max - 3)) + "...";
    }
    private void reloadCacheLoHangCoTheXuatSync() {
        List<LoHang> tempData = busKho.layDSLoHang(false);

        List<LoHang> dsCoTheXuat = new ArrayList<>();

        if (tempData != null) {
            for (LoHang lh : tempData) {
                if (laLoCoTheXuat(lh)) {
                    dsCoTheXuat.add(lh);
                }
            }
        }

        cacheDanhSachLo = dsCoTheXuat;
    }
    
    private void reloadCacheLoHangCoTheXuat() {
        new Thread(() -> {
            List<LoHang> tempData = busKho.layDSLoHang(false);

            List<LoHang> dsCoTheXuat = new ArrayList<>();

            if (tempData != null) {
                for (LoHang lh : tempData) {
                    if (laLoCoTheXuat(lh)) {
                        dsCoTheXuat.add(lh);
                    }
                }
            }

            SwingUtilities.invokeLater(() -> cacheDanhSachLo = dsCoTheXuat);
        }).start();
    }
    
    private void resetFormNhap() {
        isProgrammaticUpdate = true;
        txtMaLo.setText("");
        isProgrammaticUpdate = false;

        txtSoLuong.setText("");
        txtGhiChu.setText("");
        cbLyDo.setSelectedIndex(0);
        resetThongTinSP();
    }

    private void resetThongTinSP() {
        lblTenSP.setText("-- Chưa chọn --");
        lblTenSP.setForeground(TEXT_SECONDARY);
        lblTenSP.setFont(new Font("Segoe UI", Font.ITALIC, 13));

        lblKhoHang.setText("-- Chưa chọn --");
        lblKhoHang.setForeground(TEXT_SECONDARY);
        lblKhoHang.setFont(new Font("Segoe UI", Font.ITALIC, 13));

        lblTonKhoHienTai.setText("-- Chưa chọn --");
        lblTonKhoHienTai.setForeground(TEXT_SECONDARY);
        lblTonKhoHienTai.setFont(new Font("Segoe UI", Font.ITALIC, 13));

        cbDonVi.removeAllItems();
        currentTonKho = 0;
        currentSanPham = null;
        currentKhoHienThi = "Không xác định";
        currentDsDonVi.clear();

        currentLoHang = null;
        currentLoHangId = "";
    }

    private void resetFormToanBo() {
        resetFormNhap();
        tableModel.setRowCount(0);
        updateTableTitle();
        txtMaLo.requestFocus();
    }

    private boolean showCustomConfirmDialog(String titleText, String message) {
        final boolean[] result = { false };

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createLineBorder(PRIMARY_DARK, 2));
        pnlMain.setBackground(Color.WHITE);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(PRIMARY_DARK);
        pnlHeader.setPreferredSize(new Dimension(0, 45));

        JLabel lblTitle = new JLabel(titleText.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.CENTER);

        JPanel pnlBody = new JPanel(new BorderLayout());
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(20, 25, 15, 25));

        JLabel msg = new JLabel(
                "<html><center style='color:#333333; font-family:Segoe UI; font-size:14px;'>"
                        + message
                        + "</center></html>",
                SwingConstants.CENTER);
        pnlBody.add(msg, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        pnlFooter.setBackground(Color.WHITE);

        JButton btnYes = createHoverButton("Xác nhận", "CHECK_CIRCLE", ORANGE_BTN, ORANGE_HOVER, Color.WHITE);
        btnYes.setPreferredSize(new Dimension(130, 40));
        btnYes.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });

        JButton btnNo = createOutlineButton("Hủy bỏ", "CANCEL", TEXT_SECONDARY);
        btnNo.setPreferredSize(new Dimension(130, 40));
        btnNo.addActionListener(e -> dialog.dispose());

        pnlFooter.add(btnYes);
        pnlFooter.add(btnNo);

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlBody, BorderLayout.CENTER);
        pnlMain.add(pnlFooter, BorderLayout.SOUTH);

        dialog.add(pnlMain);
        dialog.setSize(450, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return result[0];
    }

    private void showCustomNotification(String titleText, String message, String type) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createLineBorder(PRIMARY_DARK, 2));
        pnlMain.setBackground(Color.WHITE);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(PRIMARY_DARK);
        pnlHeader.setPreferredSize(new Dimension(0, 45));

        JLabel lblTitle = new JLabel(titleText.toUpperCase(), SwingConstants.CENTER);
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

                if (type.equals("ERROR")) {
                    g2.setColor(new Color(254, 226, 226));
                    g2.fillRoundRect(0, 0, 50, 50, 50, 50);
                    g2.setColor(DANGER);
                    g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(17, 17, 33, 33);
                    g2.drawLine(33, 17, 17, 33);
                } else if (type.equals("SUCCESS")) {
                    g2.setColor(new Color(209, 250, 229));
                    g2.fillRoundRect(0, 0, 50, 50, 50, 50);
                    g2.setColor(SUCCESS);
                    g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(15, 26, 22, 33);
                    g2.drawLine(22, 33, 35, 16);
                } else {
                    g2.setColor(new Color(254, 243, 199));
                    g2.fillRoundRect(0, 0, 50, 50, 50, 50);
                    g2.setColor(new Color(249, 115, 22));
                    g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(25, 14, 25, 28);
                    g2.fillOval(22, 33, 6, 6);
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

        JButton btnClose = createOutlineButton("Đóng", "CLOSE", TEXT_SECONDARY);
        btnClose.setPreferredSize(new Dimension(110, 38));
        btnClose.addActionListener(e -> dialog.dispose());

        pnlFooter.add(btnClose);

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlBody, BorderLayout.CENTER);
        pnlMain.add(pnlFooter, BorderLayout.SOUTH);

        dialog.add(pnlMain);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        if (type.equals("SUCCESS")) {
            new Timer(1500, e -> dialog.dispose()).start();
        }

        dialog.setVisible(true);
    }

    private static class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int radius;

        public RoundedLineBorder(Color c, int t, int r) {
            color = c;
            thickness = t;
            radius = r;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);

            for (int i = 0; i < thickness; i++) {
                g2.drawRoundRect(
                        x + i,
                        y + i,
                        w - 1 - i * 2,
                        h - 1 - i * 2,
                        radius,
                        radius);
            }

            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
    }
}