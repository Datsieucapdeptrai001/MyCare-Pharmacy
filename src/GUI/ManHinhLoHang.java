package GUI;

import BUS.BUS_Kho;
import BUS.BUS_DonViDoLuong;
import Entity.DonViDoLuong;
import Entity.LoHang;
import Enumeration.TrangThaiLoHang;
import Utils.MenuIcon;
import Utils.TelexFix;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManHinhLoHang extends JPanel {

    private final List<BatchItem> dsTatCa = new ArrayList<>();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BUS_Kho busKho = new BUS_Kho();
    private final BUS_DonViDoLuong busDonVi = new BUS_DonViDoLuong();

    private static final Color BG_APP = new Color(241, 245, 249);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color PRIMARY_BLUE = new Color(14, 116, 144);
    private static final Color PRIMARY_BLUE_HOVER = new Color(22, 133, 163);

    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color DANGER_HOVER = new Color(220, 38, 38);
    private static final Color DANGER_SOFT = new Color(254, 242, 242);
    private static final Color WARNING = new Color(249, 115, 22);
    private static final Color WARNING_SOFT = new Color(255, 247, 237);
    private static final Color GOLD = new Color(234, 179, 8);
    private static final Color GOLD_SOFT = new Color(254, 252, 232);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color SUCCESS_HOVER = new Color(22, 163, 74);
    private static final Color SUCCESS_SOFT = new Color(240, 253, 244);
    private static final Color HIDDEN = new Color(107, 114, 128);
    private static final Color HIDDEN_SOFT = new Color(243, 244, 246);

    private static final Color ROW_HOVER = new Color(241, 245, 249);

    private JTextField txtSearch;
    private JCheckBox chkNear90;
    private JCheckBox chkShowHidden;
    private boolean hienLoAn = false;
    private int hoveredRow = -1;
    private boolean isStaffRole = false;

    private JLabel lblExpired, lblNear, lblWarning, lblGood;
    private JLabel lblWarningBadge, lblTotal;
    private JButton btnTatCa, btnDuocBan, btnHetHan, btnTamNgung;
    private JTable table;
    private DefaultTableModel tableModel;
    private JPanel pnlChiTietLo;
    private BatchItem selectedDetailItem = null;
    private TrangThaiFilter filter = TrangThaiFilter.TAT_CA;

    public ManHinhLoHang() {
        setLayout(new BorderLayout());
        setBackground(BG_APP);
        setBorder(new EmptyBorder(8, 8, 8, 8));

        add(createMainCard(), BorderLayout.CENTER);
        TelexFix.applyDeep(this);
        loadDataFromDatabase();
    }

    private JPanel createMainCard() {
        JPanel unifiedCard = new JPanel(new BorderLayout(0, 12));
        unifiedCard.setBackground(BG_CARD);
        unifiedCard.setBorder(new CompoundRoundBorder(
                new SmoothShadowBorder(new Color(0, 0, 0, 12), 16),
                new Insets(16, 16, 16, 16)));

        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        topSection.add(createHeaderRow());
        topSection.add(Box.createVerticalStrut(12));
        topSection.add(createStatsRow());
        topSection.add(Box.createVerticalStrut(16));

        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(BORDER_COLOR);
        sep.setBackground(Color.WHITE);
        topSection.add(sep);

        topSection.add(Box.createVerticalStrut(12));
        topSection.add(createControlBarRow());

        unifiedCard.add(topSection, BorderLayout.NORTH);
        unifiedCard.add(createTableContainer(), BorderLayout.CENTER);

        return unifiedCard;
    }

    private JPanel createHeaderRow() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);
        left.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel lblIcon = new JLabel(new MenuIcon("BOX"));
        lblIcon.setForeground(PRIMARY_BLUE);

        JLabel lblTitle = new JLabel("QUẢN LÝ LÔ HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT_PRIMARY);

        lblWarningBadge = new JLabel("0 lô cần chú ý!") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(new Color(254, 202, 202));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblWarningBadge.setIcon(new MenuIcon("WARNING"));
        lblWarningBadge.setOpaque(false);
        lblWarningBadge.setBackground(DANGER_SOFT);
        lblWarningBadge.setForeground(DANGER);
        lblWarningBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblWarningBadge.setIconTextGap(6);
        lblWarningBadge.setBorder(new EmptyBorder(4, 12, 4, 12));

        left.add(lblIcon);
        left.add(lblTitle);
        left.add(Box.createHorizontalStrut(4));
        left.add(lblWarningBadge);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.setOpaque(false);
        right.setAlignmentY(Component.CENTER_ALIGNMENT);

        JComponent searchField = createSearchField();
        searchField.setPreferredSize(new Dimension(220, 38));
        searchField.setMaximumSize(new Dimension(220, 38));
        searchField.setMinimumSize(new Dimension(80, 38));

        JButton btnLamMoi = createHoverButton("Làm mới", new Color(248, 250, 252), new Color(226, 232, 240),
                TEXT_PRIMARY);
        btnLamMoi.setIcon(new MenuIcon("REFRESH"));
        btnLamMoi.setBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8));
        btnLamMoi.setPreferredSize(new Dimension(106, 38));
        btnLamMoi.setMaximumSize(new Dimension(106, 38));
        btnLamMoi.setMinimumSize(new Dimension(40, 38));
        btnLamMoi.addActionListener(e -> {
            txtSearch.setText("");
            if (chkNear90 != null)
                chkNear90.setSelected(false);
            if (chkShowHidden != null)
                chkShowHidden.setSelected(false);
            hienLoAn = false;
            filter = TrangThaiFilter.TAT_CA;
            setActiveFilterButton(btnTatCa);
            loadDataFromDatabase();
            wrapper.requestFocus();
        });

        JButton btnLichSu = createHoverButton("Nhật ký", new Color(248, 250, 252), new Color(226, 232, 240),
                TEXT_PRIMARY);
        btnLichSu.setIcon(new MenuIcon("LIST"));
        btnLichSu.setBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8));
        btnLichSu.setPreferredSize(new Dimension(110, 38));
        btnLichSu.setMaximumSize(new Dimension(110, 38));
        btnLichSu.setMinimumSize(new Dimension(40, 38));
        btnLichSu.addActionListener(e -> moManHinhNhatKyKho());

        JButton btnXuatKho = createHoverButton("Xuất / Hủy Kho", WARNING, new Color(234, 88, 12), Color.WHITE);
        btnXuatKho.setIcon(new MenuIcon("MINUS"));
        btnXuatKho.setPreferredSize(new Dimension(160, 38));
        btnXuatKho.setMaximumSize(new Dimension(160, 38));
        btnXuatKho.setMinimumSize(new Dimension(40, 38));
        btnXuatKho.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnXuatKho.addActionListener(e -> moManHinhXuatKho());

        JButton btnThemLo = createHoverButton("Nhập lô hàng", SUCCESS, SUCCESS_HOVER, Color.WHITE);
        btnThemLo.setIcon(new MenuIcon("ADD"));
        btnThemLo.setPreferredSize(new Dimension(150, 38));
        btnThemLo.setMaximumSize(new Dimension(150, 38));
        btnThemLo.setMinimumSize(new Dimension(40, 38));
        btnThemLo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnThemLo.addActionListener(e -> moManHinhNhapLoMoi());

        right.add(searchField);
        right.add(Box.createHorizontalStrut(10));
        right.add(btnLamMoi);
        right.add(Box.createHorizontalStrut(10));
        right.add(btnLichSu);
        right.add(Box.createHorizontalStrut(10));
        right.add(btnXuatKho);
        right.add(Box.createHorizontalStrut(10));
        right.add(btnThemLo);

        wrapper.add(left);
        wrapper.add(Box.createHorizontalGlue());
        wrapper.add(right);

        return wrapper;
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 96));

        lblExpired = statValueLabel();
        lblNear = statValueLabel();
        lblWarning = statValueLabel();
        lblGood = statValueLabel();

        row.add(createStatCard("Đã hết hạn", new MenuIcon("CANCEL"), lblExpired, DANGER_SOFT, DANGER));
        row.add(createStatCard("Gần hết (≤30 ngày)", new MenuIcon("WARNING"), lblNear, WARNING_SOFT, WARNING));
        row.add(createStatCard("Cảnh báo (31-90 ngày)", new MenuIcon("TIME"), lblWarning, GOLD_SOFT, GOLD));
        row.add(createStatCard("Còn hạn (>90 ngày)", new MenuIcon("CHECK_CIRCLE"), lblGood, SUCCESS_SOFT, SUCCESS));

        return row;
    }

    private JPanel createControlBarRow() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.setOpaque(false);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        left.setOpaque(false);
        left.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel lbl = new JLabel("Lọc theo:  ");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_SECONDARY);

        JPanel filterGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        filterGroup.setOpaque(false);
        filterGroup.setBackground(new Color(241, 245, 249));

        btnTatCa = createFilterButton("Tất cả", "PACKAGE");
        btnDuocBan = createFilterButton("Được bán", "CHECK_CIRCLE");
        btnHetHan = createFilterButton("Hết hạn", "TIME");
        btnTamNgung = createFilterButton("Hết hàng", "BOX");

        btnTatCa.addActionListener(e -> switchFilter(TrangThaiFilter.TAT_CA, btnTatCa));
        btnDuocBan.addActionListener(e -> switchFilter(TrangThaiFilter.DUOC_BAN, btnDuocBan));
        btnHetHan.addActionListener(e -> switchFilter(TrangThaiFilter.HET_HAN, btnHetHan));
        btnTamNgung.addActionListener(e -> switchFilter(TrangThaiFilter.HET_HANG, btnTamNgung));

        filterGroup.add(btnTatCa);
        filterGroup.add(btnDuocBan);
        filterGroup.add(btnHetHan);
        filterGroup.add(btnTamNgung);

        left.add(lbl);
        left.add(filterGroup);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.setOpaque(false);
        right.setAlignmentY(Component.CENTER_ALIGNMENT);

        chkNear90 = new JCheckBox("Chỉ sắp hết hạn");
        setupCheckBox(chkNear90);
        chkNear90.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                filter = TrangThaiFilter.GAN_HET_HAN_90;
                setActiveFilterButton(null);
                if (chkShowHidden != null && chkShowHidden.isSelected())
                    chkShowHidden.setSelected(false);
            } else {
                if (filter == TrangThaiFilter.GAN_HET_HAN_90) {
                    filter = TrangThaiFilter.TAT_CA;
                    setActiveFilterButton(btnTatCa);
                }
            }
            refreshTable();
        });

        chkShowHidden = new JCheckBox("Hiện lô ẩn");
        setupCheckBox(chkShowHidden);
        chkShowHidden.addItemListener(e -> {
            hienLoAn = e.getStateChange() == ItemEvent.SELECTED;
            if (hienLoAn) {
                filter = TrangThaiFilter.TAT_CA;
                setActiveFilterButton(btnTatCa);
                if (chkNear90 != null)
                    chkNear90.setSelected(false);
            }
            loadDataFromDatabase();
        });

        right.add(chkNear90);
        right.add(Box.createHorizontalStrut(16));
        right.add(chkShowHidden);

        wrapper.add(left);
        wrapper.add(Box.createHorizontalGlue());
        wrapper.add(right);

        setActiveFilterButton(btnTatCa);
        return wrapper;
    }

    private void setupCheckBox(JCheckBox chk) {
        chk.setOpaque(false);
        chk.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chk.setForeground(TEXT_PRIMARY);
        chk.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chk.setFocusPainted(false);
        chk.setIcon(new ModernCheckBoxIcon(false));
        chk.setSelectedIcon(new ModernCheckBoxIcon(true));
        chk.setIconTextGap(8);
    }

    private JComponent createSearchField() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 1, 16),
                new Insets(0, 12, 0, 12)));

        JLabel icon = new JLabel(new MenuIcon("SEARCH"));
        icon.setForeground(TEXT_SECONDARY);

        txtSearch = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(148, 163, 184));
                    g2.setFont(getFont().deriveFont(Font.ITALIC));

                    FontMetrics fm = g2.getFontMetrics();
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString("Tìm mã lô, tên SP...", 2, y);
                    g2.dispose();
                }
            }
        };

        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBorder(null);
        txtSearch.setOpaque(false);
        txtSearch.setForeground(TEXT_PRIMARY);

        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtSearch.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtSearch.repaint();
            }
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshTable();
            }
        });

        panel.add(icon, BorderLayout.WEST);
        panel.add(txtSearch, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTableContainer() {
        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setOpaque(false);

        JPanel topInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topInfo.setOpaque(false);
        lblTotal = new JLabel("0 / 0 lô hàng");
        lblTotal.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblTotal.setForeground(TEXT_SECONDARY);
        topInfo.add(lblTotal);
        wrap.add(topInfo, BorderLayout.NORTH);

        String[] cols = {
                "#", "Mã lô", "Sản phẩm", "Đơn vị", "Tồn kho", "Giá vốn (ĐVCB)",
                "Hạn sử dụng", "Còn lại", "Tình trạng", "Thao tác"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(46);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(new Color(241, 245, 249));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setDefaultRenderer(Object.class, new LoHangCellRenderer());

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(230);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.getColumnModel().getColumn(7).setPreferredWidth(130);
        table.getColumnModel().getColumn(8).setPreferredWidth(120);
        table.getColumnModel().getColumn(9).setPreferredWidth(60);

        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (hoveredRow != row) {
                    hoveredRow = row;
                    table.repaint();
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                table.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                if (row < 0) {
                    return;
                }

                String maLo = String.valueOf(table.getValueAt(row, 1));
                BatchItem item = timBatchTheoSoLo(maLo);

                if (item == null) {
                    return;
                }

                // Cột thao tác: Ẩn / Khôi phục
                if (col == 9) {
                    if (isStaffRole) {
                        showCustomNotification(
                                "Từ chối thao tác",
                                "Nhân viên Dược sĩ không có quyền Ẩn hoặc Khôi phục lô hàng!",
                                "WARNING");
                        return;
                    }

                    if ("Đã ẩn".equals(item.trangThai)) {
                        khoiPhucLoHang(item.soLo);
                    } else {
                        anLoHang(item.soLo);
                    }
                    return;
                }

                // Click các cột còn lại: xổ chi tiết lô hàng
                toggleChiTietLo(item);
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8));
        scroll.getViewport().setBackground(Color.WHITE);

        // Áp dụng Modern ScrollBar
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));

        scroll.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 12));

        wrap.add(scroll, BorderLayout.CENTER);

        pnlChiTietLo = createChiTietLoPanel();
        pnlChiTietLo.setVisible(false);
        wrap.add(pnlChiTietLo, BorderLayout.SOUTH);

        return wrap;
    }

    private void loadDataFromDatabase() {
        dsTatCa.clear();
        try {
            busKho.kiemKeKho();
            List<LoHang> dsLo = busKho.layDSLoHang(hienLoAn);

            if (dsLo != null) {
                for (LoHang lh : dsLo) {
                    if (lh == null)
                        continue;

                    String id = safe(lh.getId());
                    String soLo = safe(lh.getSoLoHang());
                    String maVachNoiBo = safe(lh.getMaVachNoiBo());
                    String maSP = "";
                    String tenSP = "";
                    String donVi = "Chưa có";
                    String donViCoBan = ""; // Đã thêm để lưu đơn vị cơ bản
                    String tonKhoHienThi = "";

                    if (lh.getSanPhamId() != null) {
                        if (lh.getSanPhamId().getId() != null)
                            maSP = lh.getSanPhamId().getId();
                        if (lh.getSanPhamId().getTen() != null)
                            tenSP = lh.getSanPhamId().getTen();

                        if (!maSP.isEmpty()) {
                            List<DonViDoLuong> dsDVDL = busDonVi.getDSTheoMaSP(maSP);
                            if (dsDVDL != null && !dsDVDL.isEmpty()) {
                                DonViDoLuong dvCoBan = null;
                                DonViDoLuong dvLon = null;
                                double maxQuyDoi = 1.0;

                                for (DonViDoLuong dv : dsDVDL) {
                                    if (dv.getChuyenDoiSangDonViCoBan() == 1.0) {
                                        dvCoBan = dv;
                                        donVi = dv.getTen();
                                        donViCoBan = dv.getTen(); // Lưu lại đơn vị cơ bản
                                    }
                                    if (dv.getChuyenDoiSangDonViCoBan() > maxQuyDoi) {
                                        maxQuyDoi = dv.getChuyenDoiSangDonViCoBan();
                                        dvLon = dv;
                                    }
                                }

                                int soLuong = lh.getSoLuongLoHang();

                                if (dvCoBan != null && dvLon != null && maxQuyDoi > 1.0) {
                                    int slLon = (int) (soLuong / maxQuyDoi);
                                    int slLe = (int) (soLuong % maxQuyDoi);

                                    if (slLon > 0 && slLe > 0) {
                                        tonKhoHienThi = formatNumber(slLon) + " " + dvLon.getTen() + ", "
                                                + formatNumber(slLe) + " " + dvCoBan.getTen();
                                    } else if (slLon > 0 && slLe == 0) {
                                        tonKhoHienThi = formatNumber(slLon) + " " + dvLon.getTen();
                                    } else {
                                        tonKhoHienThi = formatNumber(slLe) + " " + dvCoBan.getTen();
                                    }
                                    donVi = dvLon.getTen() + "/" + dvCoBan.getTen();
                                } else {
                                    tonKhoHienThi = formatNumber(soLuong);
                                    if (dvCoBan == null && !dsDVDL.isEmpty()) {
                                        donVi = dsDVDL.get(0).getTen();
                                        donViCoBan = dsDVDL.get(0).getTen(); // Lưu đơn vị cơ bản nếu không có quy đổi
                                    }
                                }
                            }
                        }
                    }

                    int soLuong = lh.getSoLuongLoHang();
                    double gia = lh.getGia();

                    if (tonKhoHienThi.isEmpty()) {
                        tonKhoHienThi = formatNumber(soLuong);
                    }

                    String hanSuDung = lh.getNgayHetHan() != null
                            ? lh.getNgayHetHan().toLocalDate().format(DATE_FORMAT)
                            : "";

                    String trangThai = convertTrangThaiToText(lh.getTrangThai());
                    if (soLuong <= 0 && !"Đã ẩn".equals(trangThai)) {
                        trangThai = "Hết hàng";
                    }

                    // Xử lý đơn vị cơ bản nếu trống
                    if (donViCoBan.isEmpty() && !"Chưa có".equals(donVi)) {
                        donViCoBan = donVi;
                    }
                    if ("Chưa có".equals(donViCoBan))
                        donViCoBan = "";

                    dsTatCa.add(new BatchItem(id, soLo, maSP, tenSP, donVi, donViCoBan, soLuong, tonKhoHienThi, gia,
                            hanSuDung,
                            trangThai,
                            maVachNoiBo));
                }
            }
            updateStats();
            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
            showCustomNotification("Lỗi hệ thống", "Lỗi tải dữ liệu lô hàng từ CSDL!", "ERROR");
        }
    }

    private void updateStats() {
        int expired = 0, near = 0, warning = 0, good = 0;
        for (BatchItem item : dsTatCa) {
            if ("Đã ẩn".equals(item.trangThai))
                continue;
            String conLai = item.getConLai();
            if ("Hết hạn".equals(item.trangThai) || conLai.startsWith("Quá")) {
                expired++;
            } else {
                int days = parseDays(conLai);
                if (days <= 30)
                    near++;
                else if (days <= 90)
                    warning++;
                else
                    good++;
            }
        }
        lblExpired.setText(String.valueOf(expired));
        lblNear.setText(String.valueOf(near));
        lblWarning.setText(String.valueOf(warning));
        lblGood.setText(String.valueOf(good));

        int countDanger = near + expired;
        if (countDanger > 0) {
            lblWarningBadge.setText(countDanger + " lô cần chú ý!");
            lblWarningBadge.setVisible(true);
        } else {
            lblWarningBadge.setVisible(false);
        }
    }

    private void moManHinhNhatKyKho() {
        Window owner = SwingUtilities.getWindowAncestor(this);

        JDialog dialog = new JDialog(owner, "Nhật ký lịch sử kho", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setContentPane(new ManHinhNhatKyKho());
        dialog.setSize(1150, 650);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private void refreshTable() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (pnlChiTietLo != null && selectedDetailItem != null) {
            // Nếu đang tìm kiếm/lọc lại thì đóng chi tiết để tránh hiển thị lô không còn
            // trên bảng
            pnlChiTietLo.setVisible(false);
            selectedDetailItem = null;
        }
        List<BatchItem> filtered = new ArrayList<>();

        for (BatchItem item : dsTatCa) {
            String conLai = item.getConLai();
            boolean matchKw = keyword.isEmpty()
                    || safe(item.soLo).toLowerCase().contains(keyword)
                    || safe(item.maSanPham).toLowerCase().contains(keyword)
                    || safe(item.tenSanPham).toLowerCase().contains(keyword);

            boolean matchFilter;
            if (hienLoAn) {
                matchFilter = "Đã ẩn".equals(item.trangThai);
            } else {
                if ("Đã ẩn".equals(item.trangThai)) {
                    matchFilter = false;
                } else {
                    switch (filter) {
                        case DUOC_BAN:
                            matchFilter = "Được bán".equals(item.trangThai);
                            break;
                        case HET_HAN:
                            matchFilter = conLai.startsWith("Quá") || "Hết hạn".equals(item.trangThai);
                            break;
                        case HET_HANG:
                            matchFilter = "Hết hàng".equals(item.trangThai);
                            break;
                        case GAN_HET_HAN_90:
                            matchFilter = !conLai.startsWith("Quá") && parseDays(conLai) <= 90;
                            break;
                        default:
                            matchFilter = true;
                            break;
                    }
                }
            }

            if (matchKw && matchFilter) {
                filtered.add(item);
            }
        }

        filtered.sort((a, b) -> {
            int rankA = getPriority(a);
            int rankB = getPriority(b);
            if (rankA != rankB)
                return Integer.compare(rankA, rankB);
            long daysA = getDaysRemaining(a);
            long daysB = getDaysRemaining(b);
            return Long.compare(daysA, daysB);
        });

        tableModel.setRowCount(0);
        int stt = 1;
        for (BatchItem item : filtered) {
            tableModel.addRow(new Object[] {
                    stt++,
                    item.soLo,
                    item.maSanPham + (safe(item.tenSanPham).isEmpty() ? "" : " - " + item.tenSanPham),
                    item.donVi,
                    item.tonKhoHienThi,
                    // Hiển thị giá vốn kèm đơn vị cơ bản
                    formatCurrency(item.giaNhap) + (item.donViCoBan.isEmpty() ? "" : " / " + item.donViCoBan),
                    item.hanSuDung,
                    item.getConLai(),
                    item.trangThai,
                    ""
            });
        }
        lblTotal.setText("Hiển thị " + filtered.size() + " / " + dsTatCa.size() + " lô");
    }

    public void filterData(String keyword) {
        if (txtSearch != null) {
            txtSearch.setText(keyword);
            txtSearch.requestFocus();
        }
    }

    private int getPriority(BatchItem item) {
        if ("Đã ẩn".equals(item.trangThai))
            return 5;
        String conLai = item.getConLai();
        if ("Hết hạn".equals(item.trangThai) || conLai.startsWith("Quá"))
            return 1;
        int days = parseDays(conLai);
        if (days <= 30)
            return 2;
        if (days <= 90)
            return 3;
        return 4;
    }

    private long getDaysRemaining(BatchItem item) {
        if ("Đã ẩn".equals(item.trangThai))
            return Long.MAX_VALUE;
        try {
            LocalDate hsd = LocalDate.parse(item.hanSuDung, DATE_FORMAT);
            return ChronoUnit.DAYS.between(LocalDate.now(), hsd);
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    public void moManHinhNhapLoMoi(String tenSP) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        ManHinhNhapLoHangMoi dialog = new ManHinhNhapLoHangMoi(owner, this::loadDataFromDatabase);
        if (tenSP != null && !tenSP.isEmpty()) {
            dialog.setSanPhamAutoFill(tenSP);
        }
        dialog.setVisible(true);
    }

    private void moManHinhNhapLoMoi() {
        moManHinhNhapLoMoi(null);
    }

    private void moManHinhXuatKho() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Quy Trình Xuất / Hủy Kho", Dialog.ModalityType.APPLICATION_MODAL);
        ManHinhXuatKho pnlXuatKho = new ManHinhXuatKho();
        dialog.setContentPane(pnlXuatKho);
        dialog.setSize(1100, 650);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        loadDataFromDatabase();
    }

    private void moManHinhLichSuXuat() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Nhật Ký Lịch Sử Xuất Kho", Dialog.ModalityType.APPLICATION_MODAL);
        ManHinhLichSuXuat pnlLichSu = new ManHinhLichSuXuat();
        dialog.setContentPane(pnlLichSu);
        dialog.setSize(1000, 600);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private BatchItem timBatchTheoSoLo(String soLo) {
        for (BatchItem bi : dsTatCa) {
            if (safe(bi.soLo).equalsIgnoreCase(safe(soLo)))
                return bi;
        }
        return null;
    }

    private void anLoHang(String soLo) {
        BatchItem item = timBatchTheoSoLo(soLo);
        if (item == null)
            return;

        if (item.tonKho > 0) {
            String msg = "Lô hàng " + soLo + " hiện vẫn còn tồn " + item.tonKhoHienThi
                    + ".\n\n" +
                    "Theo nguyên tắc quản lý kho, bạn KHÔNG ĐƯỢC PHÉP ẩn lô hàng khi giá trị tài sản vẫn còn trên hệ thống.\n\n"
                    +
                    "Vui lòng sử dụng chức năng 'Xuất / Hủy Kho' để đưa số lượng về 0 trước khi tiến hành ẩn lô này!";
            showCustomNotification("TỪ CHỐI THAO TÁC", msg, "ERROR");
            return;
        }

        boolean confirm = showCustomConfirmDialog("XÁC NHẬN ẨN LÔ HÀNG",
                "Bạn có chắc chắn muốn ẩn lô <b>" + soLo + "</b> khỏi danh sách hiển thị không?");

        if (confirm) {
            try {
                if (busKho.anLoHang(item.id)) {
                    loadDataFromDatabase();
                } else {
                    showCustomNotification("Lỗi hệ thống", "Thao tác ẩn lô thất bại! Vui lòng kiểm tra lại CSDL.",
                            "ERROR");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void khoiPhucLoHang(String soLo) {
        boolean confirm = showCustomConfirmDialog("XÁC NHẬN KHÔI PHỤC",
                "Bạn có chắc muốn khôi phục lô <b>" + soLo + "</b> không?");

        if (confirm) {
            try {
                BatchItem item = timBatchTheoSoLo(soLo);
                if (item != null && busKho.khoiPhucLoHang(item.id)) {
                    loadDataFromDatabase();
                } else {
                    showCustomNotification("Lỗi", "Khôi phục lô thất bại!", "ERROR");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean showCustomConfirmDialog(String titleText, String message) {
        final boolean[] result = { false };
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 2));
        pnlMain.setBackground(Color.WHITE);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(PRIMARY_BLUE);
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
        msg.setVerticalAlignment(SwingConstants.CENTER);
        pnlBody.add(msg, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        pnlFooter.setBackground(Color.WHITE);
        JButton btnYes = createHoverButton("Xác nhận", DANGER, DANGER_HOVER, Color.WHITE);
        btnYes.setPreferredSize(new Dimension(130, 40));
        btnYes.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });
        JButton btnNo = createHoverButton("Hủy bỏ", TEXT_SECONDARY, new Color(71, 85, 105), Color.WHITE);
        btnNo.setPreferredSize(new Dimension(130, 40));
        btnNo.addActionListener(e -> dialog.dispose());
        pnlFooter.add(btnYes);
        pnlFooter.add(btnNo);

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlBody, BorderLayout.CENTER);
        pnlMain.add(pnlFooter, BorderLayout.SOUTH);

        dialog.add(pnlMain);
        dialog.setSize(450, 250);
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
        pnlMain.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 2));
        pnlMain.setBackground(Color.WHITE);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(PRIMARY_BLUE);
        pnlHeader.setPreferredSize(new Dimension(0, 45));
        JLabel lblTitle = new JLabel(titleText.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.CENTER);

        JPanel pnlBody = new JPanel(null);
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setPreferredSize(new Dimension(420, 170));

        JPanel pnlIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = type.equals("ERROR") ? DANGER : (type.equals("SUCCESS") ? SUCCESS : new Color(245, 158, 11));
                g2.setColor(type.equals("ERROR") ? new Color(254, 242, 242)
                        : (type.equals("SUCCESS") ? new Color(209, 250, 229) : new Color(254, 243, 199)));
                g2.fillOval(0, 0, 50, 50);
                g2.setColor(c);
                g2.setStroke(new BasicStroke(3f));
                g2.drawOval(0, 0, 50, 50);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                String s = type.equals("ERROR") ? "X" : (type.equals("SUCCESS") ? "V" : "!");
                g2.drawString(s, (50 - g2.getFontMetrics().stringWidth(s)) / 2, 35);
                g2.dispose();
            }
        };
        pnlIcon.setBounds(25, 30, 50, 50);
        pnlIcon.setOpaque(false);

        JTextArea msgArea = new JTextArea(message);
        msgArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        msgArea.setWrapStyleWord(true);
        msgArea.setLineWrap(true);
        msgArea.setOpaque(false);
        msgArea.setEditable(false);

        JScrollPane scroll = new JScrollPane(msgArea);
        scroll.setBounds(95, 20, 305, 140);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        pnlBody.add(pnlIcon);
        pnlBody.add(scroll);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlFooter.setBackground(Color.WHITE);
        JButton btnClose = createHoverButton("Đóng", PRIMARY_BLUE, PRIMARY_BLUE_HOVER, Color.WHITE);
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

    private void switchFilter(TrangThaiFilter newFilter, JButton source) {
        if (chkNear90 != null)
            chkNear90.setSelected(false);
        if (chkShowHidden != null && chkShowHidden.isSelected())
            chkShowHidden.setSelected(false);
        filter = newFilter;
        setActiveFilterButton(source);
        refreshTable();
    }

    private void setActiveFilterButton(JButton active) {
        JButton[] list = { btnTatCa, btnDuocBan, btnHetHan, btnTamNgung };
        for (JButton b : list) {
            if (b == null)
                continue;
            b.setBackground(new Color(241, 245, 249));
            b.setForeground(TEXT_SECONDARY);
        }
        if (active != null) {
            active.setBackground(Color.WHITE);
            active.setForeground(PRIMARY_BLUE);
        }
        for (JButton b : list) {
            if (b != null)
                b.repaint();
        }
    }

    private JButton createFilterButton(String text, String iconName) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isRollover() && getBackground().equals(new Color(241, 245, 249))) {
                    g2.setColor(new Color(226, 232, 240));
                } else {
                    g2.setColor(getBackground());
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                if (getBackground().equals(Color.WHITE)) {
                    g2.setColor(new Color(0, 0, 0, 15));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setIcon(new MenuIcon(iconName));
        btn.setIconTextGap(6);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        return btn;
    }

    private JPanel createStatCard(String title, Icon icon, JLabel value, Color bg, Color textC) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(bg);
        p.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(textC, 1, 12),
                new Insets(12, 16, 12, 16)));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(TEXT_PRIMARY);
        JLabel ic = new JLabel(icon);
        ic.setForeground(textC);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        top.setOpaque(false);
        top.add(ic);
        top.add(t);
        value.setForeground(textC);
        value.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(top, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private JLabel statValueLabel() {
        JLabel lbl = new JLabel("0");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 30));
        return lbl;
    }

    private JButton createHoverButton(String text, Color bg, Color hoverBg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new RoundedLineBorder(bg, 1, 8));
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

    private int parseDays(String text) {
        try {
            return Integer.parseInt(text.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 999;
        }
    }

    private String formatCurrency(double value) {
        NumberFormat vnNumberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        vnNumberFormat.setMaximumFractionDigits(2);
        return vnNumberFormat.format(value) + "đ";
    }

    private String formatNumber(int value) {
        return String.format("%,d", value);
    }

    private String convertTrangThaiToText(TrangThaiLoHang tt) {
        if (tt == null)
            return "Không xác định";
        if (tt == TrangThaiLoHang.AN)
            return "Đã ẩn";
        if (tt == TrangThaiLoHang.HET_HAN)
            return "Hết hạn";
        if (tt == TrangThaiLoHang.HET_HANG)
            return "Hết hàng";
        return "Được bán";
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private enum TrangThaiFilter {
        TAT_CA, DUOC_BAN, HET_HAN, GAN_HET_HAN_90, HET_HANG
    }

    private JPanel createChiTietLoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 1, 10),
                new Insets(14, 18, 14, 18)));
        return panel;
    }

    private void toggleChiTietLo(BatchItem item) {
        if (item == null) {
            return;
        }

        if (selectedDetailItem != null && safe(selectedDetailItem.soLo).equalsIgnoreCase(safe(item.soLo))) {
            selectedDetailItem = null;
            pnlChiTietLo.setVisible(false);
            pnlChiTietLo.revalidate();
            pnlChiTietLo.repaint();
            return;
        }

        selectedDetailItem = item;
        capNhatChiTietLoPanel(item);
        pnlChiTietLo.setVisible(true);
        pnlChiTietLo.revalidate();
        pnlChiTietLo.repaint();
    }

    private void capNhatChiTietLoPanel(BatchItem item) {
        pnlChiTietLo.removeAll();

        JPanel root = new JPanel(new BorderLayout(16, 12));
        root.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Chi tiết lô " + item.soLo);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Mã hệ thống: " + item.id);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_SECONDARY);

        leftHeader.add(lblTitle);
        leftHeader.add(lblSub);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightHeader.setOpaque(false);

        JButton btnInTem = createHoverButton("Xem / In tem", Color.WHITE, new Color(241, 245, 249), PRIMARY_BLUE);
        btnInTem.setBorder(new RoundedLineBorder(PRIMARY_BLUE, 1, 8));
        btnInTem.setPreferredSize(new Dimension(125, 36));
        btnInTem.addActionListener(e -> moLaiTemMaVach(item));

        JButton btnDong = createHoverButton("Đóng", new Color(248, 250, 252), new Color(226, 232, 240), TEXT_PRIMARY);
        btnDong.setBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8));
        btnDong.setPreferredSize(new Dimension(85, 36));
        btnDong.addActionListener(e -> {
            selectedDetailItem = null;
            pnlChiTietLo.setVisible(false);
        });

        rightHeader.add(btnInTem);

        if (!isStaffRole) {
            JButton btnAnKhoiPhuc;

            if ("Đã ẩn".equals(item.trangThai)) {
                btnAnKhoiPhuc = createHoverButton("Khôi phục", PRIMARY_BLUE, PRIMARY_BLUE_HOVER, Color.WHITE);
                btnAnKhoiPhuc.addActionListener(e -> khoiPhucLoHang(item.soLo));
            } else {
                btnAnKhoiPhuc = createHoverButton("Ẩn lô", DANGER, DANGER_HOVER, Color.WHITE);
                btnAnKhoiPhuc.addActionListener(e -> anLoHang(item.soLo));
            }

            btnAnKhoiPhuc.setPreferredSize(new Dimension(105, 36));
            rightHeader.add(btnAnKhoiPhuc);
        }

        rightHeader.add(btnDong);

        header.add(leftHeader, BorderLayout.WEST);
        header.add(rightHeader, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(18, 0));
        body.setOpaque(false);

        JPanel productBox = new JPanel();
        productBox.setLayout(new BoxLayout(productBox, BoxLayout.Y_AXIS));
        productBox.setOpaque(false);

        JLabel lblSanPhamTitle = new JLabel("Sản phẩm");
        lblSanPhamTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSanPhamTitle.setForeground(TEXT_SECONDARY);

        JLabel lblSanPham = new JLabel(
                "<html><b>" + safe(item.maSanPham) + "</b> - " + safe(item.tenSanPham) + "</html>");
        lblSanPham.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSanPham.setForeground(TEXT_PRIMARY);

        JLabel lblDonVi = new JLabel("Đơn vị: " + safe(item.donVi));
        lblDonVi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDonVi.setForeground(TEXT_SECONDARY);

        productBox.add(lblSanPhamTitle);
        productBox.add(Box.createVerticalStrut(8));
        productBox.add(lblSanPham);
        productBox.add(Box.createVerticalStrut(8));
        productBox.add(lblDonVi);

        JPanel infoGrid = new JPanel(new GridLayout(2, 3, 12, 12));
        infoGrid.setOpaque(false);

        infoGrid.add(createInfoCard("Tồn kho", item.tonKhoHienThi, PRIMARY_BLUE, new Color(240, 249, 255)));
        infoGrid.add(createInfoCard("Giá vốn",
                formatCurrency(item.giaNhap) + (item.donViCoBan.isEmpty() ? "" : " / " + item.donViCoBan), WARNING,
                WARNING_SOFT));
        infoGrid.add(createInfoCard("Hạn sử dụng", item.hanSuDung, TEXT_PRIMARY, new Color(248, 250, 252)));
        infoGrid.add(createInfoCard("Còn lại", item.getConLai(), getMauConLai(item), getNenConLai(item)));
        infoGrid.add(createInfoCard("Trạng thái", item.trangThai, getMauTrangThai(item.trangThai),
                getNenTrangThai(item.trangThai)));
        infoGrid.add(createInfoCard("Mã vạch lô", safe(item.maVachNoiBo).isEmpty() ? "Chưa có" : item.maVachNoiBo,
                PRIMARY_BLUE, new Color(240, 249, 255)));

        body.add(productBox, BorderLayout.WEST);
        body.add(infoGrid, BorderLayout.CENTER);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);

        pnlChiTietLo.add(root, BorderLayout.CENTER);
    }

    private JPanel createInfoCard(String title, String value, Color valueColor, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(bgColor);
        card.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(new Color(0, 0, 0, 0), 0, 10),
                new Insets(10, 12, 10, 12)));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(TEXT_SECONDARY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblValue.setForeground(valueColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);

        return card;
    }

    private Color getMauTrangThai(String trangThai) {
        if ("Được bán".equals(trangThai)) {
            return SUCCESS;
        }
        if ("Hết hạn".equals(trangThai)) {
            return DANGER;
        }
        if ("Hết hàng".equals(trangThai)) {
            return TEXT_SECONDARY;
        }
        if ("Đã ẩn".equals(trangThai)) {
            return HIDDEN;
        }
        return TEXT_SECONDARY;
    }

    private Color getNenTrangThai(String trangThai) {
        if ("Được bán".equals(trangThai)) {
            return SUCCESS_SOFT;
        }
        if ("Hết hạn".equals(trangThai)) {
            return DANGER_SOFT;
        }
        if ("Hết hàng".equals(trangThai)) {
            return HIDDEN_SOFT;
        }
        if ("Đã ẩn".equals(trangThai)) {
            return HIDDEN_SOFT;
        }
        return new Color(248, 250, 252);
    }

    private Color getMauConLai(BatchItem item) {
        String conLai = item.getConLai();

        if (conLai.startsWith("Quá")) {
            return DANGER;
        }

        int days = parseDays(conLai);

        if (days <= 30) {
            return WARNING;
        }

        if (days <= 90) {
            return GOLD;
        }

        return SUCCESS;
    }

    private Color getNenConLai(BatchItem item) {
        String conLai = item.getConLai();

        if (conLai.startsWith("Quá")) {
            return DANGER_SOFT;
        }

        int days = parseDays(conLai);

        if (days <= 30) {
            return WARNING_SOFT;
        }

        if (days <= 90) {
            return GOLD_SOFT;
        }

        return SUCCESS_SOFT;
    }

    private void moLaiTemMaVach(BatchItem item) {
        if (item == null) {
            return;
        }

        if (safe(item.maVachNoiBo).isEmpty()) {
            showCustomNotification(
                    "Không có mã vạch",
                    "Lô hàng này chưa có mã vạch nội bộ để in lại!",
                    "WARNING");
            return;
        }

        Window owner = SwingUtilities.getWindowAncestor(this);

        String maSP = safe(item.maSanPham).isEmpty() ? "N/A" : item.maSanPham;
        String tenSP = safe(item.tenSanPham).isEmpty() ? "Sản phẩm không tên" : item.tenSanPham;

        DialogInMaVach dialogMaVach = new DialogInMaVach(
                owner,
                item.maVachNoiBo,
                maSP,
                tenSP);

        dialogMaVach.setVisible(true);
    }

    private class BatchItem {
        // Cập nhật thêm donViCoBan
        String id, soLo, maSanPham, tenSanPham, donVi, donViCoBan, hanSuDung, trangThai, tonKhoHienThi, maVachNoiBo;
        int tonKho;
        double giaNhap;

        // Cập nhật hàm tạo
        BatchItem(String i, String sl, String msp, String tsp, String dv, String dvcb, int tk, String tkHienThi,
                double gn, String hsd, String tt, String mvnb) {
            id = i;
            soLo = sl;
            maSanPham = msp;
            tenSanPham = tsp;
            donVi = dv;
            donViCoBan = dvcb;
            tonKho = tk;
            tonKhoHienThi = tkHienThi;
            giaNhap = gn;
            hanSuDung = hsd;
            trangThai = tt;
            maVachNoiBo = mvnb;
        }

        String getConLai() {
            if ("Đã ẩn".equals(trangThai))
                return "--";
            try {
                LocalDate hsd = LocalDate.parse(hanSuDung, DATE_FORMAT);
                long days = ChronoUnit.DAYS.between(LocalDate.now(), hsd);
                if (days < 0)
                    return "Quá " + Math.abs(days) + " ngày";
                return days + " ngày";
            } catch (Exception e) {
                return "Lỗi HSD";
            }
        }
    }

    private class LoHangCellRenderer extends DefaultTableCellRenderer {
        private final Color ROW_ALTERNATE = new Color(252, 253, 255);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String trangThaiRow = String.valueOf(table.getValueAt(row, 8));

            lbl.setIcon(null);
            lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lbl.setHorizontalAlignment(SwingConstants.LEFT);
            lbl.setOpaque(true);

            if (isSelected) {
                lbl.setBackground(new Color(224, 242, 254));
                lbl.setForeground(TEXT_PRIMARY);
            } else if (row == hoveredRow) {
                lbl.setBackground(ROW_HOVER);
                lbl.setForeground(TEXT_PRIMARY);
            } else {
                if ("Đã ẩn".equals(trangThaiRow)) {
                    lbl.setBackground(row % 2 == 0 ? HIDDEN_SOFT : new Color(249, 250, 251));
                    lbl.setForeground(HIDDEN);
                } else {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALTERNATE);
                    lbl.setForeground(TEXT_PRIMARY);
                }
            }

            if (column == 0) {
                lbl.setForeground("Đã ẩn".equals(trangThaiRow) ? HIDDEN : TEXT_SECONDARY);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            } else if (column == 1) {
                lbl.setForeground("Đã ẩn".equals(trangThaiRow) ? HIDDEN : PRIMARY_BLUE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else if (column == 3) {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            } else if (column == 4 || column == 5) {
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                if (column == 4) {
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    if (!"Đã ẩn".equals(trangThaiRow)) {
                        lbl.setForeground(PRIMARY_BLUE);
                    }
                }
            } else if (column == 6) {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            } else if (column == 7) {
                String text = String.valueOf(value);
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
                panel.setBackground(lbl.getBackground());

                JLabel pill = new JLabel(text);
                pill.setFont(new Font("Segoe UI", Font.BOLD, 11));
                pill.setOpaque(true);
                pill.setBorder(new CompoundRoundBorder(
                        new RoundedLineBorder(new Color(0, 0, 0, 0), 0, 12),
                        new Insets(3, 10, 3, 10)));

                if ("Đã ẩn".equals(trangThaiRow)) {
                    pill.setText("--");
                    pill.setIcon(new MenuIcon("CLOSE"));
                    pill.setBackground(HIDDEN_SOFT);
                    pill.setForeground(HIDDEN);
                } else if (text.startsWith("Quá")) {
                    pill.setText(text);
                    pill.setIcon(new MenuIcon("WARNING"));
                    pill.setBackground(DANGER_SOFT);
                    pill.setForeground(DANGER);
                } else {
                    int days = parseDays(text);
                    if (days <= 30) {
                        pill.setText(text);
                        pill.setIcon(new MenuIcon("WARNING"));
                        pill.setBackground(WARNING_SOFT);
                        pill.setForeground(WARNING);
                    } else if (days <= 90) {
                        pill.setText(text);
                        pill.setIcon(new MenuIcon("TIME"));
                        pill.setBackground(GOLD_SOFT);
                        pill.setForeground(GOLD);
                    } else {
                        pill.setText(text);
                        pill.setIcon(new MenuIcon("CHECK_CIRCLE"));
                        pill.setBackground(SUCCESS_SOFT);
                        pill.setForeground(SUCCESS);
                    }
                }
                pill.setIconTextGap(4);
                panel.add(pill);
                return panel;
            } else if (column == 8) {
                String text = String.valueOf(value);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setIconTextGap(4);

                if ("Được bán".equals(text)) {
                    lbl.setForeground(SUCCESS);
                    lbl.setIcon(new MenuIcon("CHECK_CIRCLE"));
                } else if ("Hết hạn".equals(text)) {
                    lbl.setForeground(DANGER);
                    lbl.setIcon(new MenuIcon("CANCEL"));
                } else if ("Đã ẩn".equals(text)) {
                    lbl.setForeground(HIDDEN);
                    lbl.setIcon(new MenuIcon("CLOSE"));
                } else {
                    lbl.setForeground(TEXT_SECONDARY);
                    lbl.setIcon(new MenuIcon("BOX"));
                }
            } else if (column == 9) {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setText("");
                if (isStaffRole) {
                    lbl.setIcon(null);
                    lbl.setForeground(TEXT_SECONDARY);
                    lbl.setToolTipText("Chỉ quản lý được ẩn/khôi phục lô hàng");
                } else {
                    if ("Đã ẩn".equals(trangThaiRow)) {
                        lbl.setIcon(new MenuIcon("REFRESH"));
                        lbl.setForeground(PRIMARY_BLUE);
                        lbl.setToolTipText("Khôi phục lô hàng");
                    } else {
                        lbl.setIcon(new MenuIcon("TRASH"));
                        lbl.setForeground(DANGER);
                        lbl.setToolTipText("Ẩn lô hàng");
                    }
                }
            }
            return lbl;
        }
    }

    private static class ModernCheckBoxIcon implements Icon {
        private final boolean selected;

        public ModernCheckBoxIcon(boolean selected) {
            this.selected = selected;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = 16;
            int arc = 4;
            int yOff = y + 2;

            if (selected) {
                g2.setColor(PRIMARY_BLUE);
                g2.fillRoundRect(x, yOff, size, size, arc, arc);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x + 3, yOff + 8, x + 7, yOff + 12);
                g2.drawLine(x + 7, yOff + 12, x + 13, yOff + 4);
            } else {
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(x, yOff, size, size, arc, arc);
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x, yOff, size, size, arc, arc);
            }
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }
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
            for (int i = 0; i < thickness; i++) {
                g2.drawRoundRect(x + i, y + i, w - 1 - i * 2, h - 1 - i * 2, radius, radius);
            }
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets i) {
            i.left = i.right = i.top = i.bottom = radius / 2;
            return i;
        }
    }

    private static class SmoothShadowBorder extends AbstractBorder {
        private final Color shadow;
        private final int radius;

        public SmoothShadowBorder(Color s, int r) {
            shadow = s;
            radius = r;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 4, 8, 4);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < 6; i++) {
                g2.setColor(new Color(shadow.getRed(), shadow.getGreen(), shadow.getBlue(),
                        Math.max(1, shadow.getAlpha() - i * 2)));
                g2.drawRoundRect(x + 1, y + 1 + i, w - 3, h - 3 - i, radius, radius);
            }
            g2.dispose();
        }
    }

    private static class CompoundRoundBorder extends AbstractBorder {
        private final AbstractBorder outer;
        private final Insets inner;

        public CompoundRoundBorder(AbstractBorder o, Insets i) {
            outer = o;
            inner = i;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            Insets o = outer.getBorderInsets(c);
            return new Insets(o.top + inner.top, o.left + inner.left, o.bottom + inner.bottom, o.right + inner.right);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            Insets o = outer.getBorderInsets(c);
            insets.top = o.top + inner.top;
            insets.left = o.left + inner.left;
            insets.bottom = o.bottom + inner.bottom;
            insets.right = o.right + inner.right;
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            outer.paintBorder(c, g, x, y, w, h);
        }
    }

    // --- LỚP MODERNSCROLLBARUI THÊM MỚI ---
    private static class ModernScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(248, 250, 252));
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled())
                return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color thumbColor = isDragging ? new Color(156, 163, 175) : new Color(186, 195, 208);
            g2.setColor(thumbColor);

            int padding = 3;
            int x = thumbBounds.x + padding;
            int y = thumbBounds.y + padding;
            int width = thumbBounds.width - 2 * padding;
            int height = thumbBounds.height - 2 * padding;

            g2.fillRoundRect(x, y, width, height, width, width);
            g2.dispose();
        }
    }
    // --- KẾT THÚC MODERNSCROLLBARUI ---

    public void setReadOnly(boolean readOnly) {
        this.isStaffRole = readOnly;

        /*
         * readOnly = true: Nhân viên / Dược sĩ
         * - Được xem lô hàng
         * - Được tìm kiếm, lọc lô
         * - Được nhập lô hàng
         * - Được xuất / hủy kho
         * - Được xem lịch sử
         * - Không được xem lô ẩn
         * - Không được ẩn / khôi phục lô hàng
         *
         * readOnly = false: Quản lý
         * - Toàn quyền trên màn hình lô hàng
         */

        // Nhân viên không được xem lô ẩn
        if (chkShowHidden != null) {
            chkShowHidden.setSelected(false);
            chkShowHidden.setVisible(!readOnly);
        }

        hienLoAn = false;

        if (readOnly) {
            filter = TrangThaiFilter.TAT_CA;
            setActiveFilterButton(btnTatCa);
        }

        // Không ẩn các nút nghiệp vụ hằng ngày:
        // Nhập lô hàng, Xuất / Hủy Kho, Lịch sử vẫn cho nhân viên dùng.
        // Không remove cột thao tác để tránh lỗi lệch index cột.
        if (table != null) {
            table.repaint();
        }

        loadDataFromDatabase();
    }

    private void disableButtonsByText(Container container, String... texts) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton btn) {
                for (String t : texts) {
                    if (t.equals(btn.getText())) {
                        btn.setVisible(false);
                        break;
                    }
                }
            } else if (c instanceof Container child) {
                disableButtonsByText(child, texts);
            }
        }
    }
}