package GUI;

import BUS.BUS_Kho;
import BUS.BUS_DonViDoLuong;
import Entity.DonViDoLuong;
import Entity.LoHang;
import Entity.SanPham;
import Utils.MenuIcon;

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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class ManHinhXuatKho extends JPanel {

    private static final Color BG_APP = Color.WHITE;
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
    private JLabel lblTenSP, lblTonKhoHienTai, lblTableTitle;

    private JTable table;
    private DefaultTableModel tableModel;

    private final BUS_Kho busKho = new BUS_Kho();
    private final BUS_DonViDoLuong busDonVi = new BUS_DonViDoLuong();

    private int currentTonKho = 0;
    private SanPham currentSanPham = null;
    private List<DonViDoLuong> currentDsDonVi = new ArrayList<>();
    private List<LoHang> cacheDanhSachLo = new ArrayList<>();

    // Biến cho tính năng Auto-Suggest Mã Lô
    private JPopupMenu popupSuggest;
    private JList<String> listSuggest;
    private DefaultListModel<String> modelSuggest;
    private JScrollPane scrollSuggest; // Thêm biến này để ép kích thước động
    private boolean isProgrammaticUpdate = false;

    public ManHinhXuatKho() {
        setLayout(new BorderLayout());
        setBackground(BG_APP);

        new Thread(() -> {
            List<LoHang> tempData = busKho.layDSLoHang(true);
            SwingUtilities.invokeLater(() -> {
                cacheDanhSachLo = tempData;
            });
        }).start();

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        this.addAncestorListener(new AncestorListener() {
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

    private JPanel createHeader() {
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        pnlHeader.setBackground(PRIMARY_DARK);

        JLabel lblIcon = new JLabel(new MenuIcon("DOCUMENT", 24, Color.WHITE));
        lblIcon.setBorder(new EmptyBorder(0, 0, 0, 5));
        pnlHeader.add(lblIcon);

        JLabel lblTitle = new JLabel("PHIẾU XUẤT KHO / XUẤT HỦY");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle);

        return pnlHeader;
    }

    private JPanel createMainContent() {
        JPanel pnlContent = new JPanel(new BorderLayout());
        pnlContent.setBackground(BG_APP);

        JPanel pnlLeft = createInputForm();
        pnlLeft.setPreferredSize(new Dimension(420, 0));

        JPanel pnlRight = createCartTable();

        pnlContent.add(pnlLeft, BorderLayout.WEST);
        pnlContent.add(pnlRight, BorderLayout.CENTER);

        return pnlContent;
    }

    private JPanel createInputForm() {
        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setOpaque(false);
        pnlWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR),
                new EmptyBorder(20, 20, 20, 20)));

        JPanel pnlInput = new JPanel(new GridBagLayout());
        pnlInput.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 18, 0);

        // =======================================================
        // 1. Mã Lô & Nút chức năng
        // =======================================================
        txtMaLo = new JTextField();
        txtMaLo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMaLo.setPreferredSize(new Dimension(150, 40));
        txtMaLo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(5, 10, 5, 10)));

        // Khởi tạo tính năng gợi ý thông minh
        setupAutoSuggest();

        JButton btnKiemTra = createOutlineButton("Kiểm tra", "CHECK_CIRCLE", TEXT_PRIMARY);
        btnKiemTra.setPreferredSize(new Dimension(100, 40));
        btnKiemTra.addActionListener(e -> kiemTraMaLo());

        JButton btnScanQR = createHoverButton("Quét mã QR", "SEARCH", PRIMARY_BLUE, PRIMARY_HOVER, Color.WHITE);
        btnScanQR.setPreferredSize(new Dimension(135, 40));
        btnScanQR.addActionListener(e -> showQRScannerDialog());

        JPanel pnlMaLoBtns = new JPanel(new GridBagLayout());
        pnlMaLoBtns.setOpaque(false);
        GridBagConstraints gbcBtns = new GridBagConstraints();
        gbcBtns.insets = new Insets(0, 8, 0, 0);
        gbcBtns.gridx = 0;
        gbcBtns.gridy = 0;
        pnlMaLoBtns.add(btnKiemTra, gbcBtns);
        gbcBtns.gridx = 1;
        pnlMaLoBtns.add(btnScanQR, gbcBtns);

        JPanel pnlMaLo = new JPanel(new BorderLayout());
        pnlMaLo.setOpaque(false);
        pnlMaLo.add(txtMaLo, BorderLayout.CENTER);
        pnlMaLo.add(pnlMaLoBtns, BorderLayout.EAST);

        gbc.gridy = 0;
        pnlInput.add(createFormGroup("Nhập / Chọn Mã Lô hàng", "BARCODE", pnlMaLo), gbc);

        // =======================================================
        // 2. Khối Box xám hiển thị thông tin Sản phẩm
        // =======================================================
        JPanel pnlInfoBox = new JPanel(new GridLayout(4, 1, 2, 5));
        pnlInfoBox.setBackground(new Color(248, 250, 252));
        pnlInfoBox.setBorder(BorderFactory.createCompoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 1, 8),
                new EmptyBorder(12, 15, 12, 15)));

        JLabel lblT1 = new JLabel("Sản phẩm:");
        lblT1.setForeground(TEXT_SECONDARY);
        lblT1.setIcon(new MenuIcon("MEDICAL_TOOL", 16, TEXT_SECONDARY));
        lblT1.setIconTextGap(8);

        lblTenSP = new JLabel("-- Chưa chọn --");
        lblTenSP.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblTenSP.setForeground(TEXT_SECONDARY);
        lblTenSP.setBorder(new EmptyBorder(0, 24, 0, 0));

        JLabel lblT2 = new JLabel("Tồn kho hiện tại:");
        lblT2.setForeground(TEXT_SECONDARY);
        lblT2.setIcon(new MenuIcon("BOX", 16, TEXT_SECONDARY));
        lblT2.setIconTextGap(8);

        lblTonKhoHienTai = new JLabel("-- Chưa chọn --");
        lblTonKhoHienTai.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblTonKhoHienTai.setForeground(TEXT_SECONDARY);
        lblTonKhoHienTai.setBorder(new EmptyBorder(0, 24, 0, 0));

        pnlInfoBox.add(lblT1);
        pnlInfoBox.add(lblTenSP);
        pnlInfoBox.add(lblT2);
        pnlInfoBox.add(lblTonKhoHienTai);

        gbc.gridy = 1;
        pnlInput.add(pnlInfoBox, gbc);

        // =======================================================
        // 3. Số lượng, Nút MAX & Đơn vị
        // =======================================================
        txtSoLuong = createTextField();
        txtSoLuong.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    themVaoDanhSach();
            }
        });

        // Nút Xuất MAX
        JButton btnMax = createOutlineButton("MAX", null, PRIMARY_BLUE);
        btnMax.setPreferredSize(new Dimension(65, 40));
        btnMax.setToolTipText("Điền tự động tất cả số lượng còn lại trong kho");
        btnMax.addActionListener(e -> chucNangXuatMax());

        JPanel pnlSLInput = new JPanel(new BorderLayout(5, 0));
        pnlSLInput.setOpaque(false);
        pnlSLInput.add(txtSoLuong, BorderLayout.CENTER);
        pnlSLInput.add(btnMax, BorderLayout.EAST);

        cbDonVi = new JComboBox<>();
        cbDonVi.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbDonVi.setBackground(Color.WHITE);
        cbDonVi.setPreferredSize(new Dimension(110, 40));

        JPanel pnlSL_DV = new JPanel(new BorderLayout(15, 0));
        pnlSL_DV.setOpaque(false);
        pnlSL_DV.add(createFormGroup("Số lượng xuất", "PACKAGE", pnlSLInput), BorderLayout.CENTER);
        pnlSL_DV.add(createFormGroup("Đơn vị", "PILL", cbDonVi), BorderLayout.EAST);

        gbc.gridy = 2;
        pnlInput.add(pnlSL_DV, gbc);

        // =======================================================
        // 4. Lý do xuất
        // =======================================================
        cbLyDo = new JComboBox<>(new String[] { "Bán hàng", "Xuất hủy hàng hỏng/lỗi", "Xuất trả nhà cung cấp",
                "Xuất tiêu hao nội bộ", "Khác" });
        cbLyDo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbLyDo.setBackground(Color.WHITE);
        cbLyDo.setPreferredSize(new Dimension(0, 40));

        gbc.gridy = 3;
        pnlInput.add(createFormGroup("Lý do xuất", "LIST", cbLyDo), gbc);

        // =======================================================
        // 5. Ghi chú
        // =======================================================
        txtGhiChu = createTextField();

        gbc.gridy = 4;
        pnlInput.add(createFormGroup("Ghi chú", "EDIT", txtGhiChu), gbc);

        // =======================================================
        // 6. Nút Thêm vào danh sách
        // =======================================================
        JButton btnAdd = createHoverButton("Thêm vào phiếu", "ADD", PRIMARY_BLUE, PRIMARY_HOVER, Color.WHITE);
        btnAdd.setPreferredSize(new Dimension(0, 42));
        btnAdd.addActionListener(e -> themVaoDanhSach());

        gbc.gridy = 5;
        gbc.insets = new Insets(5, 0, 0, 0);
        pnlInput.add(btnAdd, gbc);

        pnlWrapper.add(pnlInput, BorderLayout.NORTH);
        return pnlWrapper;
    }

    // =======================================================================
    // FIX TÍNH NĂNG GỢI Ý MÃ LÔ THÔNG MINH - ĐẢM BẢO KHÔNG BỊ DƯ KHOẢNG TRẮNG
    // =======================================================================
    private void setupAutoSuggest() {
        popupSuggest = new JPopupMenu();
        modelSuggest = new DefaultListModel<>();
        listSuggest = new JList<>(modelSuggest);
        listSuggest.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listSuggest.setSelectionBackground(new Color(239, 246, 255));
        listSuggest.setSelectionForeground(TEXT_PRIMARY);

        // Chốt cứng mỗi dòng cao đúng 30px để nhân lên cho dễ
        listSuggest.setFixedCellHeight(30);

        scrollSuggest = new JScrollPane(listSuggest);
        scrollSuggest.setBorder(null);
        scrollSuggest.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        popupSuggest.add(scrollSuggest);
        popupSuggest.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE));

        // Lắng nghe sự kiện gõ phím
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

        // Click chuột vào list để chọn
        listSuggest.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 || e.getClickCount() == 2)
                    chonTuSuggest();
            }
        });

        // Hỗ trợ phím mũi tên Lên/Xuống và Enter
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
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    chonTuSuggest();
            }
        });
    }

    private void chonTuSuggest() {
        String selected = listSuggest.getSelectedValue();
        if (selected != null) {
            isProgrammaticUpdate = true;
            txtMaLo.setText(selected.split(" - ")[0]);
            popupSuggest.setVisible(false);
            isProgrammaticUpdate = false;
            kiemTraMaLo();
        }
    }

    private void updateSuggest() {
        if (isProgrammaticUpdate)
            return;
        SwingUtilities.invokeLater(() -> {
            String text = txtMaLo.getText().trim().toLowerCase();
            modelSuggest.clear();
            if (text.isEmpty()) {
                popupSuggest.setVisible(false);
                return;
            }
            int count = 0;
            for (LoHang lh : cacheDanhSachLo) {
                String maLo = lh.getSoLoHang() != null ? lh.getSoLoHang().toLowerCase() : "";
                String tenSP = lh.getSanPhamId() != null ? lh.getSanPhamId().getTen().toLowerCase() : "";
                if (maLo.contains(text) || tenSP.contains(text)) {
                    modelSuggest.addElement(lh.getSoLoHang() + " - " + lh.getSanPhamId().getTen());
                    count++;
                }
            }

            if (count > 0) {
                int displayCount = Math.min(count, 5); // Hiển thị tối đa 5 dòng
                listSuggest.setVisibleRowCount(displayCount);

                // CÔNG THỨC VÀNG: Ép cứng khung chứa list bằng pixel (số dòng * 30px)
                int exactHeight = displayCount * 30;
                scrollSuggest.setPreferredSize(new Dimension(340, exactHeight)); // Đảm bảo rộng rãi 340px

                popupSuggest.pack(); // Bung theo đúng size ép cứng
                popupSuggest.show(txtMaLo, 0, txtMaLo.getHeight());
                txtMaLo.requestFocus();
            } else {
                popupSuggest.setVisible(false);
            }
        });
    }

    private void chucNangXuatMax() {
        if (currentSanPham == null || currentTonKho <= 0)
            return;

        String tenDVChon = cbDonVi.getSelectedItem() != null ? cbDonVi.getSelectedItem().toString() : "";
        DonViDoLuong dvChon = null;
        for (DonViDoLuong dv : currentDsDonVi) {
            if (dv.getTen().equals(tenDVChon)) {
                dvChon = dv;
                break;
            }
        }
        if (dvChon == null)
            return;

        int tongDaCoTrongGio = 0;
        String maLoHienTai = txtMaLo.getText().trim();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).toString().equalsIgnoreCase(maLoHienTai)) {
                tongDaCoTrongGio += Integer.parseInt(tableModel.getValueAt(i, 7).toString());
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
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(TEXT_PRIMARY);
        if (iconName != null) {
            lbl.setIcon(new MenuIcon(iconName, 18, PRIMARY_BLUE));
            lbl.setIconTextGap(8);
        }

        p.add(lbl, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JTextField createTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setPreferredSize(new Dimension(0, 40));
        txt.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(5, 10, 5, 10)));
        return txt;
    }

    private JButton createOutlineButton(String text, String iconName, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(color);
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedLineBorder(color, 1, 6));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (iconName != null) {
            btn.setIcon(new MenuIcon(iconName, 16, color));
            btn.setIconTextGap(6);
        }
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(color);
                btn.setForeground(Color.WHITE);
                if (iconName != null)
                    btn.setIcon(new MenuIcon(iconName, 16, Color.WHITE));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
                btn.setForeground(color);
                if (iconName != null)
                    btn.setIcon(new MenuIcon(iconName, 16, color));
            }
        });
        return btn;
    }

    private JButton createHoverButton(String text, String iconName, Color bg, Color hoverBg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedLineBorder(bg, 1, 6));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (iconName != null) {
            btn.setIcon(new MenuIcon(iconName, 18, fg));
            btn.setIconTextGap(6);
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
        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setOpaque(false);
        pnlWrapper.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel pnlTableHeader = new JPanel(new BorderLayout());
        pnlTableHeader.setBackground(new Color(241, 245, 249));
        pnlTableHeader.setBorder(new EmptyBorder(10, 15, 10, 15));

        lblTableTitle = new JLabel("DANH SÁCH CHỜ XUẤT (0)");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTableTitle.setForeground(TEXT_PRIMARY);
        lblTableTitle.setIcon(new MenuIcon("CART", 18, PRIMARY_BLUE));
        lblTableTitle.setIconTextGap(8);
        pnlTableHeader.add(lblTableTitle, BorderLayout.WEST);

        String[] cols = { "Mã lô", "Tên Sản phẩm", "SL", "ĐVT", "Lý do", "Ghi chú", "Xóa", "SL_QuyDoi_Hidden" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(42);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_COLOR);

        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        table.getColumnModel().getColumn(3).setPreferredWidth(60);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        table.getColumnModel().getColumn(6).setPreferredWidth(50);

        table.getColumnModel().getColumn(7).setMinWidth(0);
        table.getColumnModel().getColumn(7).setMaxWidth(0);
        table.getColumnModel().getColumn(7).setWidth(0);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.WHITE);
        header.setForeground(TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, BORDER_COLOR));

        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(new MenuIcon("TRASH", 20, DANGER));
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
                if (row >= 0 && col == 6) {
                    tableModel.removeRow(row);
                    updateTableTitle();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel pnlTableContainer = new JPanel(new BorderLayout());
        pnlTableContainer.add(pnlTableHeader, BorderLayout.NORTH);
        pnlTableContainer.add(scroll, BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        pnlBottom.setOpaque(false);
        pnlBottom.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton btnLamMoi = createOutlineButton("Hủy / Làm mới", "REFRESH", TEXT_SECONDARY);
        btnLamMoi.setPreferredSize(new Dimension(150, 42));
        btnLamMoi.addActionListener(e -> resetFormToanBo());

        JButton btnXacNhan = new JButton("Xác nhận xuất kho");
        btnXacNhan.setIcon(new MenuIcon("EXPORT", 18, Color.WHITE));
        btnXacNhan.setIconTextGap(8);
        btnXacNhan.setPreferredSize(new Dimension(200, 42));
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setBackground(new Color(203, 213, 225));
        btnXacNhan.setFocusPainted(false);
        btnXacNhan.setBorder(new RoundedLineBorder(new Color(203, 213, 225), 1, 6));
        btnXacNhan.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnXacNhan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (tableModel.getRowCount() > 0)
                    btnXacNhan.setBackground(ORANGE_HOVER);
                else
                    btnXacNhan.setBackground(new Color(148, 163, 184));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (tableModel.getRowCount() > 0)
                    btnXacNhan.setBackground(ORANGE_BTN);
                else
                    btnXacNhan.setBackground(new Color(203, 213, 225));
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
        if (owner == null)
            return;

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
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private void kiemTraMaLo() {
        isProgrammaticUpdate = true;
        popupSuggest.setVisible(false);
        isProgrammaticUpdate = false;

        String maQuet = txtMaLo.getText().trim();
        if (maQuet.isEmpty())
            return;

        LoHang loTimThay = null;
        List<LoHang> dsLoKhop = new ArrayList<>();
        DonViDoLuong dvQuetDuoc = null;

        try {
            dvQuetDuoc = busDonVi.layDonViTheoMaVach(maQuet);
        } catch (Exception e) {
        }

        for (LoHang lh : cacheDanhSachLo) {
            boolean match = false;
            if (dvQuetDuoc != null) {
                if (lh.getSanPhamId() != null && lh.getSanPhamId().getId().equals(dvQuetDuoc.getSanPhamId().getId()))
                    match = true;
            } else {
                boolean khopMaVachNoiBo = lh.getMaVachNoiBo() != null && lh.getMaVachNoiBo().equalsIgnoreCase(maQuet);
                boolean khopSoLo = lh.getSoLoHang() != null && lh.getSoLoHang().equalsIgnoreCase(maQuet);
                boolean khopMaSP = lh.getSanPhamId() != null && lh.getSanPhamId().getId().equalsIgnoreCase(maQuet);
                if (khopMaVachNoiBo || khopSoLo || khopMaSP)
                    match = true;
            }
            if (match)
                dsLoKhop.add(lh);
        }

        if (dsLoKhop.isEmpty()) {
            showCustomNotification("Không tìm thấy", "Mã '" + maQuet + "' không tồn tại trong kho.", "ERROR");
            resetThongTinSP();
            return;
        }

        dsLoKhop.sort((a, b) -> a.getNgayHetHan().compareTo(b.getNgayHetHan()));
        loTimThay = dsLoKhop.get(0);

        isProgrammaticUpdate = true;
        txtMaLo.setText(loTimThay.getSoLoHang());
        isProgrammaticUpdate = false;

        lblTenSP.setText(loTimThay.getSanPhamId().getTen());
        lblTenSP.setForeground(PRIMARY_BLUE);
        lblTenSP.setFont(new Font("Segoe UI", Font.BOLD, 14));

        currentTonKho = loTimThay.getSoLuongLoHang();
        currentSanPham = loTimThay.getSanPhamId();

        lblTonKhoHienTai.setText(formatTonKhoHienThi(currentSanPham.getId(), currentTonKho));
        lblTonKhoHienTai.setForeground(DANGER);
        lblTonKhoHienTai.setFont(new Font("Segoe UI", Font.BOLD, 14));

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
                if (d.getChuyenDoiSangDonViCoBan() == 1.0)
                    dvCoBan = d;
            }
            cbDonVi.setSelectedItem(dvCoBan.getTen());
        }

        txtSoLuong.setText("1");
        txtSoLuong.requestFocus();
        txtSoLuong.selectAll();
    }

    private String formatTonKhoHienThi(String maSP, int soLuongCoBan) {
        List<DonViDoLuong> ds = busDonVi.getDSTheoMaSP(maSP);
        if (ds == null || ds.isEmpty())
            return soLuongCoBan + " Đơn vị";
        DonViDoLuong dvLonNhat = ds.get(0);
        DonViDoLuong dvCoBan = ds.get(0);
        for (DonViDoLuong dv : ds) {
            if (dv.getChuyenDoiSangDonViCoBan() == 1.0)
                dvCoBan = dv;
            if (dv.getChuyenDoiSangDonViCoBan() > dvLonNhat.getChuyenDoiSangDonViCoBan())
                dvLonNhat = dv;
        }
        int heSoQuyDoi = (int) dvLonNhat.getChuyenDoiSangDonViCoBan();
        if (heSoQuyDoi <= 1 || soLuongCoBan == 0)
            return soLuongCoBan + " " + dvCoBan.getTen();
        int soLuongLon = soLuongCoBan / heSoQuyDoi;
        int soLuongLe = soLuongCoBan % heSoQuyDoi;
        String ketQua = "";
        if (soLuongLon > 0)
            ketQua += soLuongLon + " " + dvLonNhat.getTen();
        if (soLuongLe > 0) {
            if (!ketQua.isEmpty())
                ketQua += ", ";
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
            if (dvChon == null)
                return;

            int slXuatQuyDoi = (int) (slXuatNhapVao * dvChon.getChuyenDoiSangDonViCoBan());
            int tongDaCoTrongGio = 0;
            String maLoHienTai = txtMaLo.getText().trim();

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (tableModel.getValueAt(i, 0).toString().equalsIgnoreCase(maLoHienTai)) {
                    tongDaCoTrongGio += Integer.parseInt(tableModel.getValueAt(i, 7).toString());
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
                    maLoHienTai, lblTenSP.getText(), slXuatNhapVao, dvChon.getTen(),
                    cbLyDo.getSelectedItem().toString(), txtGhiChu.getText().trim(), "", slXuatQuyDoi
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
        boolean confirmed = showCustomConfirmDialog("XÁC NHẬN XUẤT KHO", "Xuất <b>" + tableModel.getRowCount()
                + "</b> lô hàng. Hệ thống sẽ trừ trực tiếp vào kho hiện tại.<br>Bạn chắc chắn thực hiện?");
        if (confirmed) {
            List<Object[]> ds = new ArrayList<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                ds.add(new Object[] { tableModel.getValueAt(i, 0), tableModel.getValueAt(i, 7),
                        tableModel.getValueAt(i, 4) });
            }

            if (busKho.xuatHuyKho(ds, "Nguyễn Quản Lý")) {
                showCustomNotification("THÀNH CÔNG", "Đã trừ kho thành công!", "SUCCESS");

                String maPhieuXuat = "PX" + (System.currentTimeMillis() % 100000);
                String ngayXuat = java.time.LocalDate.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                StringBuilder qrData = new StringBuilder();
                qrData.append("PHIẾU XUẤT: ").append(maPhieuXuat).append("\n");
                qrData.append("NGÀY XUẤT: ").append(ngayXuat).append("\n");
                qrData.append("--- CHI TIẾT HÀNG ---\n");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    qrData.append("• ").append(tableModel.getValueAt(i, 0)).append(" | ");
                    qrData.append(tableModel.getValueAt(i, 1)).append(" | ");
                    qrData.append(tableModel.getValueAt(i, 2)).append(" ");
                    qrData.append(tableModel.getValueAt(i, 3)).append("\n");
                }

                String tenHienThi = "KIỆN HÀNG XUẤT (" + tableModel.getRowCount() + " Món)";
                Window owner = SwingUtilities.getWindowAncestor(this);
                DialogInQRCode dialogQR = new DialogInQRCode(owner, qrData.toString(), maPhieuXuat, "Ngày: " + ngayXuat,
                        tenHienThi);
                dialogQR.setVisible(true);

                resetFormToanBo();
            } else {
                showCustomNotification("THẤT BẠI", "Gặp sự cố khi lưu vào Database.", "ERROR");
            }
        }
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
        lblTenSP.setFont(new Font("Segoe UI", Font.ITALIC, 14));

        lblTonKhoHienTai.setText("-- Chưa chọn --");
        lblTonKhoHienTai.setForeground(TEXT_SECONDARY);
        lblTonKhoHienTai.setFont(new Font("Segoe UI", Font.ITALIC, 14));

        cbDonVi.removeAllItems();
        currentTonKho = 0;
        currentSanPham = null;
        currentDsDonVi.clear();
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
        JLabel msg = new JLabel("<html><center style='color:#333333; font-family:Segoe UI; font-size:14px;'>" + message
                + "</center></html>", SwingConstants.CENTER);
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
        if (type.equals("SUCCESS"))
            new Timer(1500, e -> dialog.dispose()).start();
        dialog.setVisible(true);
    }

    private static class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int thickness, radius;

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
            for (int i = 0; i < thickness; i++)
                g2.drawRoundRect(x + i, y + i, w - 1 - i * 2, h - 1 - i * 2, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
    }
}