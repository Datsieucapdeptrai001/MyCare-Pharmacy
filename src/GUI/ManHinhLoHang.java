package GUI;

import BUS.BUS_Kho;
import Entity.LoHang;
import Enumeration.TrangThaiLoHang;
import Utils.MenuIcon;

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ManHinhLoHang extends JPanel {

    private final List<BatchItem> dsTatCa = new ArrayList<>();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BUS_Kho busKho = new BUS_Kho();

    private static final Color BG_APP = new Color(241, 245, 249);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color PRIMARY_BLUE = new Color(14, 116, 144);
    private static final Color PRIMARY_BLUE_HOVER = new Color(22, 133, 163);

    private static final Color DANGER = new Color(239, 68, 68);
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

    private JLabel lblExpired, lblNear, lblWarning, lblGood;
    private JLabel lblWarningBadge, lblTotal;
    private JButton btnTatCa, btnDuocBan, btnHetHan, btnTamNgung;
    private JTable table;
    private DefaultTableModel tableModel;

    private TrangThaiFilter filter = TrangThaiFilter.TAT_CA;

    public ManHinhLoHang() {
        setLayout(new BorderLayout());
        setBackground(BG_APP);
        // THU GỌN LỀ NGOÀI APP ĐỂ TĂNG DIỆN TÍCH
        setBorder(new EmptyBorder(8, 8, 8, 8));

        add(createMainCard(), BorderLayout.CENTER);
        loadDataFromDatabase();
    }

    private JPanel createMainCard() {
        JPanel unifiedCard = new JPanel(new BorderLayout(0, 12));
        unifiedCard.setBackground(BG_CARD);
        // THU GỌN LỀ CỦA CARD CHÍNH ĐỂ BẢNG RỘNG HƠN
        unifiedCard.setBorder(new CompoundRoundBorder(
                new SmoothShadowBorder(new Color(0, 0, 0, 12), 16),
                new Insets(16, 16, 16, 16)
        ));

        JPanel topSection = new JPanel();
        topSection.setOpaque(false);
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));

        topSection.add(createHeaderRow());
        topSection.add(Box.createVerticalStrut(12)); // Giảm khoảng cách thừa
        topSection.add(createStatsRow());
        topSection.add(Box.createVerticalStrut(16)); // Giảm khoảng cách thừa
        
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(BORDER_COLOR);
        sep.setBackground(Color.WHITE);
        topSection.add(sep);
        
        topSection.add(Box.createVerticalStrut(12)); // Giảm khoảng cách thừa
        topSection.add(createControlBarRow());

        unifiedCard.add(topSection, BorderLayout.NORTH);
        unifiedCard.add(createTableContainer(), BorderLayout.CENTER);

        return unifiedCard;
    }

    // --- FIX LỖI ĐÈ NHAU BẰNG BOXLAYOUT CHO HEADER ---
    private JPanel createHeaderRow() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.setOpaque(false);

        // BÊN TRÁI: Tiêu đề + Cảnh báo
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

        // BÊN PHẢI: Tìm kiếm -> Làm mới -> Nhập lô hàng
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.setAlignmentY(Component.CENTER_ALIGNMENT);

        JComponent searchField = createSearchField();
        searchField.setPreferredSize(new Dimension(240, 38));

        JButton btnLamMoi = createHoverButton("Làm mới", new Color(248, 250, 252), new Color(226, 232, 240), TEXT_PRIMARY);
        btnLamMoi.setIcon(new MenuIcon("REFRESH"));
        btnLamMoi.setBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8));
        btnLamMoi.setPreferredSize(new Dimension(106, 38));
        btnLamMoi.addActionListener(e -> {
            txtSearch.setText("");
            if (chkNear90 != null) chkNear90.setSelected(false);
            if (chkShowHidden != null) chkShowHidden.setSelected(false);
            hienLoAn = false;
            filter = TrangThaiFilter.TAT_CA;
            setActiveFilterButton(btnTatCa);
            loadDataFromDatabase();
            wrapper.requestFocus(); 
        });

        JButton btnThemLo = createHoverButton("Nhập lô hàng", SUCCESS, SUCCESS_HOVER, Color.WHITE);
        btnThemLo.setIcon(new MenuIcon("ADD"));
        btnThemLo.setPreferredSize(new Dimension(150, 38)); 
        btnThemLo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnThemLo.addActionListener(e -> moManHinhNhapLoMoi());

        right.add(searchField);
        right.add(btnLamMoi);
        right.add(btnThemLo);

        // Ghép trái phải bằng lò xo (Glue) để chống đè nhau
        wrapper.add(left);
        wrapper.add(Box.createHorizontalGlue());
        wrapper.add(right);

        return wrapper;
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 96)); // Thu gọn chiều cao thẻ thống kê 1 chút

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

    // --- FIX LỆCH DÒNG CONTROL BAR: Căn giữa theo trục Y ---
    private JPanel createControlBarRow() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.setOpaque(false);

        // BÊN TRÁI: Nhóm Tabs Lọc
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
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

        // BÊN PHẢI: Nhóm Checkbox
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 4));
        right.setOpaque(false);
        right.setAlignmentY(Component.CENTER_ALIGNMENT);
        
        chkNear90 = new JCheckBox("Chỉ sắp hết hạn");
        setupCheckBox(chkNear90);
        chkNear90.addItemListener(e -> {
            filter = e.getStateChange() == ItemEvent.SELECTED ? TrangThaiFilter.GAN_HET_HAN_90 : TrangThaiFilter.TAT_CA;
            setActiveFilterButton(filter == TrangThaiFilter.TAT_CA ? btnTatCa : null);
            refreshTable();
        });

        chkShowHidden = new JCheckBox("Hiện lô ẩn");
        setupCheckBox(chkShowHidden);
        chkShowHidden.addItemListener(e -> {
            hienLoAn = e.getStateChange() == ItemEvent.SELECTED;
            loadDataFromDatabase();
        });

        right.add(chkNear90);
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
                new Insets(0, 12, 0, 12)
        ));

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
            @Override public void focusGained(java.awt.event.FocusEvent evt) { txtSearch.repaint(); }
            @Override public void focusLost(java.awt.event.FocusEvent evt) { txtSearch.repaint(); }
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { refreshTable(); }
            @Override public void removeUpdate(DocumentEvent e) { refreshTable(); }
            @Override public void changedUpdate(DocumentEvent e) { refreshTable(); }
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
                "#", "Mã lô", "Sản phẩm", "Tồn kho", "Giá nhập",
                "Hạn sử dụng", "Còn lại", "Tình trạng", "Thao tác"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        // TỐI ƯU ROW HEIGHT ĐỂ HIỂN THỊ ĐƯỢC NHIỀU DÒNG HƠN
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
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(320);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(150);
        table.getColumnModel().getColumn(7).setPreferredWidth(120);
        table.getColumnModel().getColumn(8).setPreferredWidth(70);
        
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
                if (row >= 0 && col == 8) {
                    String maLo = String.valueOf(table.getValueAt(row, 1));
                    BatchItem item = timBatchTheoSoLo(maLo);
                    if (item == null) return;

                    if ("Đã ẩn".equals(item.trangThai)) {
                        khoiPhucLoHang(item.soLo);
                    } else {
                        anLoHang(item.soLo);
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8));
        scroll.getViewport().setBackground(Color.WHITE);

        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    private void loadDataFromDatabase() {
        dsTatCa.clear();
        try {
            busKho.kiemKeKho();
            List<LoHang> dsLo = busKho.layDSLoHang(hienLoAn);
            if (dsLo != null) {
                for (LoHang lh : dsLo) {
                    if (lh == null) continue;
                    String id = safe(lh.getId());
                    String soLo = safe(lh.getSoLoHang());
                    String maSP = "";
                    String tenSP = "";
                    if (lh.getSanPhamId() != null) {
                        if (lh.getSanPhamId().getId() != null) maSP = lh.getSanPhamId().getId();
                        if (lh.getSanPhamId().getTen() != null) tenSP = lh.getSanPhamId().getTen();
                    }
                    int soLuong = lh.getSoLuongLoHang();
                    int gia = lh.getGia();
                    String hanSuDung = lh.getNgayHetHan() != null
                            ? lh.getNgayHetHan().toLocalDate().format(DATE_FORMAT)
                            : "";
                    String trangThai = convertTrangThaiToText(lh.getTrangThai());
                    dsTatCa.add(new BatchItem(id, soLo, maSP, tenSP, soLuong, gia, hanSuDung, trangThai));
                }
            }
            updateStats();
            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu lô hàng!");
        }
    }

    private void updateStats() {
        int expired = 0, near = 0, warning = 0, good = 0;
        for (BatchItem item : dsTatCa) {
            if ("Đã ẩn".equals(item.trangThai)) continue;
            String conLai = item.getConLai();
            if ("Hết hạn".equals(item.trangThai) || conLai.startsWith("Quá")) {
                expired++;
            } else {
                int days = parseDays(conLai);
                if (days <= 30) near++;
                else if (days <= 90) warning++;
                else good++;
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

    private void refreshTable() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        List<BatchItem> filtered = new ArrayList<>();

        for (BatchItem item : dsTatCa) {
            String conLai = item.getConLai();
            boolean matchKw = keyword.isEmpty()
                    || safe(item.soLo).toLowerCase().contains(keyword)
                    || safe(item.maSanPham).toLowerCase().contains(keyword)
                    || safe(item.tenSanPham).toLowerCase().contains(keyword);

            boolean matchFilter = switch (filter) {
                case DUOC_BAN -> "Được bán".equals(item.trangThai);
                case HET_HAN -> conLai.startsWith("Quá") || "Hết hạn".equals(item.trangThai);
                case HET_HANG -> "Hết hàng".equals(item.trangThai);
                case GAN_HET_HAN_90 -> !"Đã ẩn".equals(item.trangThai) && !conLai.startsWith("Quá") && parseDays(conLai) <= 90;
                default -> true;
            };
            if (matchKw && matchFilter) {
                filtered.add(item);
            }
        }

        filtered.sort((a, b) -> {
            int rankA = getPriority(a);
            int rankB = getPriority(b);
            if (rankA != rankB) return Integer.compare(rankA, rankB);
            long daysA = getDaysRemaining(a);
            long daysB = getDaysRemaining(b);
            return Long.compare(daysA, daysB);
        });

        tableModel.setRowCount(0);
        int stt = 1;
        for (BatchItem item : filtered) {
            tableModel.addRow(new Object[]{
                    stt++,
                    item.soLo,
                    item.maSanPham + (safe(item.tenSanPham).isEmpty() ? "" : " - " + item.tenSanPham),
                    formatNumber(item.tonKho),
                    formatCurrency(item.giaNhap),
                    item.hanSuDung,
                    item.getConLai(),
                    item.trangThai,
                    ""
            });
        }
        lblTotal.setText("Hiển thị " + filtered.size() + " / " + dsTatCa.size() + " lô");
    }

    private int getPriority(BatchItem item) {
        if ("Đã ẩn".equals(item.trangThai)) return 5;
        String conLai = item.getConLai();
        if ("Hết hạn".equals(item.trangThai) || conLai.startsWith("Quá")) return 1;
        int days = parseDays(conLai);
        if (days <= 30) return 2;
        if (days <= 90) return 3;
        return 4;
    }

    private long getDaysRemaining(BatchItem item) {
        if ("Đã ẩn".equals(item.trangThai)) return Long.MAX_VALUE;
        try {
            LocalDate hsd = LocalDate.parse(item.hanSuDung, DATE_FORMAT);
            return ChronoUnit.DAYS.between(LocalDate.now(), hsd);
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    private void moManHinhNhapLoMoi() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        ManHinhNhapLoHangMoi dialog = new ManHinhNhapLoHangMoi(owner, this::loadDataFromDatabase);
        dialog.setVisible(true);
    }

    private BatchItem timBatchTheoSoLo(String soLo) {
        for (BatchItem bi : dsTatCa) {
            if (safe(bi.soLo).equalsIgnoreCase(safe(soLo))) return bi;
        }
        return null;
    }

    private void anLoHang(String soLo) {
        int confirm = JOptionPane.showConfirmDialog(
                this, "Bạn có chắc muốn ẩn lô " + soLo + " không?",
                "Xác nhận ẩn lô", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                BatchItem item = timBatchTheoSoLo(soLo);
                if (item != null && busKho.anLoHang(item.id)) loadDataFromDatabase();
                else JOptionPane.showMessageDialog(this, "Ẩn lô thất bại!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void khoiPhucLoHang(String soLo) {
        int confirm = JOptionPane.showConfirmDialog(
                this, "Bạn có chắc muốn khôi phục lô " + soLo + " không?",
                "Xác nhận khôi phục", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                BatchItem item = timBatchTheoSoLo(soLo);
                if (item != null && busKho.khoiPhucLoHang(item.id)) loadDataFromDatabase();
                else JOptionPane.showMessageDialog(this, "Khôi phục lô thất bại!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void switchFilter(TrangThaiFilter newFilter, JButton source) {
        if (chkNear90 != null) chkNear90.setSelected(false);
        filter = newFilter;
        setActiveFilterButton(source);
        refreshTable();
    }

    private void setActiveFilterButton(JButton active) {
        JButton[] list = {btnTatCa, btnDuocBan, btnHetHan, btnTamNgung};
        for (JButton b : list) {
            if (b == null) continue;
            b.setBackground(new Color(241, 245, 249)); 
            b.setForeground(TEXT_SECONDARY);
        }
        if (active != null) {
            active.setBackground(Color.WHITE); 
            active.setForeground(PRIMARY_BLUE);
        }
        for (JButton b : list) {
            if (b != null) b.repaint();
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
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
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
                new Insets(12, 16, 12, 16)
        ));
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
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hoverBg); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private int parseDays(String text) {
        try { return Integer.parseInt(text.replaceAll("[^0-9]", "")); } 
        catch (Exception e) { return 999; }
    }

    private String formatCurrency(int value) { return String.format("%,dđ", value); }
    private String formatNumber(int value) { return String.format("%,d", value); }

    private String convertTrangThaiToText(TrangThaiLoHang tt) {
        if (tt == null) return "Không xác định";
        if (tt == TrangThaiLoHang.AN) return "Đã ẩn";
        if (tt == TrangThaiLoHang.HET_HAN) return "Hết hạn";
        if (tt == TrangThaiLoHang.HET_HANG) return "Hết hàng";
        return "Được bán";
    }

    private String safe(String s) { return s == null ? "" : s; }

    private enum TrangThaiFilter { TAT_CA, DUOC_BAN, HET_HAN, GAN_HET_HAN_90, HET_HANG }

    private class BatchItem {
        String id, soLo, maSanPham, tenSanPham, hanSuDung, trangThai;
        int tonKho, giaNhap;
        BatchItem(String i, String sl, String msp, String tsp, int tk, int gn, String hsd, String tt) {
            id = i; soLo = sl; maSanPham = msp; tenSanPham = tsp;
            tonKho = tk; giaNhap = gn; hanSuDung = hsd; trangThai = tt;
        }
        String getConLai() {
            if ("Đã ẩn".equals(trangThai)) return "--";
            try {
                LocalDate hsd = LocalDate.parse(hanSuDung, DATE_FORMAT);
                long days = ChronoUnit.DAYS.between(LocalDate.now(), hsd);
                if (days < 0) return "Quá " + Math.abs(days) + " ngày";
                return days + " ngày";
            } catch (Exception e) { return "Lỗi HSD"; }
        }
    }

    private class LoHangCellRenderer extends DefaultTableCellRenderer {
        private final Color ROW_ALTERNATE = new Color(252, 253, 255);
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String trangThaiRow = String.valueOf(table.getValueAt(row, 7));

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
            } else if (column == 3 || column == 4) {
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                if (column == 3) lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else if (column == 5) {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            } else if (column == 6) {
                String text = String.valueOf(value);
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
                panel.setBackground(lbl.getBackground());

                JLabel pill = new JLabel(text);
                pill.setFont(new Font("Segoe UI", Font.BOLD, 11));
                pill.setOpaque(true);
                pill.setBorder(new CompoundRoundBorder(
                        new RoundedLineBorder(new Color(0, 0, 0, 0), 0, 12),
                        new Insets(3, 10, 3, 10)
                ));

                if ("Đã ẩn".equals(trangThaiRow)) {
                    pill.setText("--"); pill.setIcon(new MenuIcon("CLOSE"));
                    pill.setBackground(HIDDEN_SOFT); pill.setForeground(HIDDEN);
                } else if (text.startsWith("Quá")) {
                    pill.setText(text); pill.setIcon(new MenuIcon("WARNING"));
                    pill.setBackground(DANGER_SOFT); pill.setForeground(DANGER);
                } else {
                    int days = parseDays(text);
                    if (days <= 30) {
                        pill.setText(text); pill.setIcon(new MenuIcon("WARNING"));
                        pill.setBackground(WARNING_SOFT); pill.setForeground(WARNING);
                    } else if (days <= 90) {
                        pill.setText(text); pill.setIcon(new MenuIcon("TIME"));
                        pill.setBackground(GOLD_SOFT); pill.setForeground(GOLD);
                    } else {
                        pill.setText(text); pill.setIcon(new MenuIcon("CHECK_CIRCLE"));
                        pill.setBackground(SUCCESS_SOFT); pill.setForeground(SUCCESS);
                    }
                }
                pill.setIconTextGap(4);
                panel.add(pill);
                return panel;
            } else if (column == 7) {
                String text = String.valueOf(value);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setIconTextGap(4);

                if ("Được bán".equals(text)) { lbl.setForeground(SUCCESS); lbl.setIcon(new MenuIcon("CHECK_CIRCLE")); } 
                else if ("Hết hạn".equals(text)) { lbl.setForeground(DANGER); lbl.setIcon(new MenuIcon("CANCEL")); } 
                else if ("Đã ẩn".equals(text)) { lbl.setForeground(HIDDEN); lbl.setIcon(new MenuIcon("CLOSE")); } 
                else { lbl.setForeground(TEXT_SECONDARY); lbl.setIcon(new MenuIcon("BOX")); }
            } else if (column == 8) {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setText("");

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
            return lbl;
        }
    }

    private static class ModernCheckBoxIcon implements Icon {
        private final boolean selected;
        public ModernCheckBoxIcon(boolean selected) { this.selected = selected; }
        
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
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 20; }
    }

    private static class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int thickness, radius;
        public RoundedLineBorder(Color c, int t, int r) { color = c; thickness = t; radius = r; }
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
        @Override public Insets getBorderInsets(Component c) { return new Insets(radius / 2, radius / 2, radius / 2, radius / 2); }
        @Override public Insets getBorderInsets(Component c, Insets i) { i.left = i.right = i.top = i.bottom = radius / 2; return i; }
    }

    private static class SmoothShadowBorder extends AbstractBorder {
        private final Color shadow;
        private final int radius;
        public SmoothShadowBorder(Color s, int r) { shadow = s; radius = r; }
        @Override public Insets getBorderInsets(Component c) { return new Insets(4, 4, 8, 4); }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < 6; i++) {
                g2.setColor(new Color(shadow.getRed(), shadow.getGreen(), shadow.getBlue(), Math.max(1, shadow.getAlpha() - i * 2)));
                g2.drawRoundRect(x + 1, y + 1 + i, w - 3, h - 3 - i, radius, radius);
            }
            g2.dispose();
        }
    }

    private static class CompoundRoundBorder extends AbstractBorder {
        private final AbstractBorder outer;
        private final Insets inner;
        public CompoundRoundBorder(AbstractBorder o, Insets i) { outer = o; inner = i; }
        @Override public Insets getBorderInsets(Component c) {
            Insets o = outer.getBorderInsets(c);
            return new Insets(o.top + inner.top, o.left + inner.left, o.bottom + inner.bottom, o.right + inner.right);
        }
        @Override public Insets getBorderInsets(Component c, Insets insets) {
            Insets o = outer.getBorderInsets(c);
            insets.top = o.top + inner.top; insets.left = o.left + inner.left;
            insets.bottom = o.bottom + inner.bottom; insets.right = o.right + inner.right;
            return insets;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) { outer.paintBorder(c, g, x, y, w, h); }
    }

    public void setReadOnly(boolean readOnly) {
        if (!readOnly) return;
        disableButtonsByText(this, "Thêm mới", "Nhập lô hàng", "Nhập Excel", "Thêm", "Xóa", "Sửa", "Lưu");
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