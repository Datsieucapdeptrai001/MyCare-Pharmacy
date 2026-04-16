package GUI;

import BUS.BUS_Kho;
import Entity.KhoHang;
import Entity.LoHang;
import Entity.SanPham;
import Enumeration.TrangThaiLoHang;
import Utils.MenuIcon;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color DANGER_SOFT = new Color(254, 242, 242);
    private static final Color WARNING = new Color(249, 115, 22);
    private static final Color WARNING_SOFT = new Color(255, 247, 237);
    private static final Color GOLD = new Color(234, 179, 8);
    private static final Color GOLD_SOFT = new Color(254, 252, 232);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color SUCCESS_SOFT = new Color(240, 253, 244);

    private JTextField txtSearch;
    private JCheckBox chkNear90;
    private JLabel lblExpired, lblNear, lblWarning, lblGood;
    private JLabel lblWarningBadge, lblTotal;
    private JButton btnTatCa, btnDuocBan, btnHetHan, btnTamNgung;
    private JTable table;
    private DefaultTableModel tableModel;

    private TrangThaiFilter filter = TrangThaiFilter.TAT_CA;

    public ManHinhLoHang() {
        setLayout(new BorderLayout());
        setBackground(BG_APP);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(createMainCard(), BorderLayout.CENTER);
        loadDataFromDatabase();
    }

    private JPanel createMainCard() {
        JPanel unifiedCard = new JPanel(new BorderLayout(0, 16));
        unifiedCard.setBackground(BG_CARD);
        unifiedCard.setBorder(new CompoundRoundBorder(
                new ShadowBorder(new Color(0, 0, 0, 10), 16),
                new Insets(16, 20, 20, 20)
        ));

        JPanel topSection = new JPanel();
        topSection.setOpaque(false);
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));

        topSection.add(createToolbarHeader());
        topSection.add(Box.createVerticalStrut(16));
        topSection.add(createStatsRow());
        topSection.add(Box.createVerticalStrut(12));
        topSection.add(new JSeparator(SwingConstants.HORIZONTAL));
        topSection.add(Box.createVerticalStrut(12));
        topSection.add(createFilterRow());

        unifiedCard.add(topSection, BorderLayout.NORTH);
        unifiedCard.add(createTableContainer(), BorderLayout.CENTER);

        return unifiedCard;
    }

    private JPanel createToolbarHeader() {
        // SỬ DỤNG BOXLAYOUT THAY CHO BORDERLAYOUT ĐỂ TRÁNH BỊ ĐÈ COMPONENT
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.setOpaque(false);

        // --- BÊN TRÁI (Tiêu đề & Huy hiệu) ---
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel lblIcon = new JLabel(new MenuIcon("BOX"));
        lblIcon.setForeground(PRIMARY_BLUE);

        JLabel lblTitle = new JLabel("QUẢN LÝ LÔ HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(PRIMARY_BLUE);

        lblWarningBadge = new JLabel("0 lô gần hết hạn!") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Vẽ nền
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                // Vẽ viền
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
        left.add(lblWarningBadge);

        // --- BÊN PHẢI (Tìm kiếm & Nút bấm) ---
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JComponent searchField = createSearchField();

        JButton btnLamMoi = createButton("Làm mới", new Color(248, 250, 252), TEXT_PRIMARY);
        btnLamMoi.setIcon(new MenuIcon("REFRESH"));
        btnLamMoi.setBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8));
        btnLamMoi.addActionListener(e -> {
            txtSearch.setText("");
            chkNear90.setSelected(false);
            filter = TrangThaiFilter.TAT_CA;
            setActiveFilterButton(btnTatCa);
            loadDataFromDatabase();
        });

        JPanel chkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        chkPanel.setOpaque(false);
        chkPanel.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 1, 8),
                new Insets(4, 6, 4, 6)
        ));

        chkNear90 = new JCheckBox("Chỉ sắp hết hạn (≤90 ngày)");
        chkNear90.setOpaque(false);
        chkNear90.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkNear90.setForeground(TEXT_PRIMARY);
        chkNear90.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chkNear90.addItemListener(e -> {
            filter = e.getStateChange() == ItemEvent.SELECTED
                    ? TrangThaiFilter.GAN_HET_HAN_90
                    : TrangThaiFilter.TAT_CA;
            setActiveFilterButton(filter == TrangThaiFilter.TAT_CA ? btnTatCa : null);
            refreshTable();
        });
        chkPanel.add(chkNear90);

        JButton btnThemLo = createButton("Nhập lô hàng", DANGER, Color.WHITE);
        btnThemLo.setIcon(new MenuIcon("ADD"));
        btnThemLo.addActionListener(e -> moManHinhNhapLoMoi());

        right.add(searchField);
        right.add(btnLamMoi);
        right.add(chkPanel);
        right.add(btnThemLo);

        // --- RÁP NỐI VÀO WRAPPER ---
        wrapper.add(left);
        wrapper.add(Box.createHorizontalGlue()); // Cục lò xo tự động dãn ra đẩy 2 bên về 2 mép
        wrapper.add(right);

        return wrapper;
    }

    private JComponent createSearchField() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(260, 38));
        panel.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 1, 18),
                new Insets(0, 12, 0, 12)
        ));

        JLabel icon = new JLabel(new MenuIcon("SEARCH"));
        icon.setForeground(TEXT_SECONDARY);

        txtSearch = new JTextField();
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(null);
        txtSearch.setOpaque(false);
        txtSearch.setForeground(TEXT_PRIMARY);
        txtSearch.setToolTipText("Nhập mã lô, mã sản phẩm để tìm kiếm...");
        txtSearch.addActionListener(e -> refreshTable());

        panel.add(icon, BorderLayout.WEST);
        panel.add(txtSearch, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 90));

        lblExpired = statValueLabel();
        lblNear = statValueLabel();
        lblWarning = statValueLabel();
        lblGood = statValueLabel();

        // Đổi icon của "Đã hết hạn" từ WARNING thành CANCEL cho hợp lý hơn
        row.add(createStatCard("Đã hết hạn", new MenuIcon("CANCEL"), lblExpired, DANGER_SOFT, DANGER));
        row.add(createStatCard("Gần hết hạn (≤30 ngày)", new MenuIcon("WARNING"), lblNear, WARNING_SOFT, WARNING));
        row.add(createStatCard("Cảnh báo (31-90 ngày)", new MenuIcon("TIME"), lblWarning, GOLD_SOFT, GOLD));
        row.add(createStatCard("Còn hạn tốt (>90 ngày)", new MenuIcon("CHECK_CIRCLE"), lblGood, SUCCESS_SOFT, SUCCESS));

        return row;
    }

    private JPanel createFilterRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel lbl = new JLabel("Tình trạng:");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(TEXT_SECONDARY);

        // Đã thêm Icon vào các nút filter
        btnTatCa = createFilterButton("Tất cả", "PACKAGE");
        btnDuocBan = createFilterButton("Được bán", "CHECK_CIRCLE");
        btnHetHan = createFilterButton("Hết hạn", "TIME");
        btnTamNgung = createFilterButton("Hết hàng", "BOX");

        btnTatCa.addActionListener(e -> switchFilter(TrangThaiFilter.TAT_CA, btnTatCa));
        btnDuocBan.addActionListener(e -> switchFilter(TrangThaiFilter.DUOC_BAN, btnDuocBan));
        btnHetHan.addActionListener(e -> switchFilter(TrangThaiFilter.HET_HAN, btnHetHan));
        btnTamNgung.addActionListener(e -> switchFilter(TrangThaiFilter.HET_HANG, btnTamNgung));

        left.add(lbl);
        left.add(btnTatCa);
        left.add(btnDuocBan);
        left.add(btnHetHan);
        left.add(btnTamNgung);

        lblTotal = new JLabel("0 / 0 lô hàng");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotal.setForeground(TEXT_SECONDARY);

        row.add(left, BorderLayout.WEST);
        row.add(lblTotal, BorderLayout.EAST);

        setActiveFilterButton(btnTatCa);
        return row;
    }

    private JPanel createTableContainer() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);

        String[] cols = {
                "#", "Mã lô", "Mã sản phẩm", "Tồn kho", "Giá nhập",
                "Hạn sử dụng", "Còn lại", "Tình trạng", "Thao tác"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(52);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(new Color(241, 245, 249));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setDefaultRenderer(Object.class, new LoHangCellRenderer());

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 44));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(130);
        table.getColumnModel().getColumn(6).setPreferredWidth(160);
        table.getColumnModel().getColumn(7).setPreferredWidth(130);
        table.getColumnModel().getColumn(8).setPreferredWidth(80);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 8) {
                    String maLo = String.valueOf(table.getValueAt(row, 1));
                    capNhatLoHetHang(maLo);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    private void loadDataFromDatabase() {
        dsTatCa.clear();
        try {
            busKho.kiemKeKho();

            List<LoHang> dsLo = busKho.layDSLoHang();
            if (dsLo != null) {
                for (LoHang lh : dsLo) {
                    if (lh == null) continue;

                    String id = safe(lh.getId());
                    String soLo = safe(lh.getSoLoHang());

                    String maSP = "";
                    if (lh.getSanPhamId() != null && lh.getSanPhamId().getId() != null) {
                        maSP = lh.getSanPhamId().getId();
                    }

                    int soLuong = lh.getSoLuongLoHang();
                    int gia = lh.getGia();
                    String hanSuDung = lh.getNgayHetHan() != null
                            ? lh.getNgayHetHan().toLocalDate().format(DATE_FORMAT)
                            : "";

                    String trangThai = convertTrangThaiToText(lh.getTrangThai());

                    dsTatCa.add(new BatchItem(id, soLo, maSP, soLuong, gia, hanSuDung, trangThai));
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
                    || safe(item.maSanPham).toLowerCase().contains(keyword);

            boolean matchFilter = switch (filter) {
                case DUOC_BAN -> "Được bán".equals(item.trangThai);
                case HET_HAN -> conLai.startsWith("Quá") || "Hết hạn".equals(item.trangThai);
                case HET_HANG -> "Hết hàng".equals(item.trangThai);
                case GAN_HET_HAN_90 -> !conLai.startsWith("Quá") && parseDays(conLai) <= 90;
                default -> true;
            };

            if (matchKw && matchFilter) {
                filtered.add(item);
            }
        }

        tableModel.setRowCount(0);
        int stt = 1;
        for (BatchItem item : filtered) {
            tableModel.addRow(new Object[]{
                    stt++, item.soLo, item.maSanPham,
                    formatNumber(item.tonKho), formatCurrency(item.giaNhap),
                    item.hanSuDung, item.getConLai(), item.trangThai, ""
            });
        }

        lblTotal.setText("Hiển thị " + filtered.size() + " / " + dsTatCa.size() + " lô");
    }

    private void moManHinhNhapLoMoi() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        ManHinhNhapLoHangMoi dialog = new ManHinhNhapLoHangMoi(owner, this::loadDataFromDatabase);
        dialog.setVisible(true);
    }

    private void capNhatLoHetHang(String soLo) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn cập nhật lô " + soLo + " về hết hàng không?",
                "Cảnh báo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                BatchItem item = null;
                for (BatchItem bi : dsTatCa) {
                    if (safe(bi.soLo).equalsIgnoreCase(safe(soLo))) {
                        item = bi;
                        break;
                    }
                }

                if (item == null) {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy lô cần cập nhật!");
                    return;
                }

                boolean ok = busKho.capNhatLoHetHang(item.id);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Đã cập nhật lô về trạng thái hết hàng!");
                    loadDataFromDatabase();
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật lô hàng!");
            }
        }
    }

    private void switchFilter(TrangThaiFilter newFilter, JButton source) {
        chkNear90.setSelected(false);
        filter = newFilter;
        setActiveFilterButton(source);
        refreshTable();
    }

    private void setActiveFilterButton(JButton active) {
        JButton[] list = {btnTatCa, btnDuocBan, btnHetHan, btnTamNgung};
        for (JButton b : list) {
            b.setBackground(Color.WHITE);
            b.setForeground(TEXT_SECONDARY);
            b.setBorder(new RoundedLineBorder(BORDER_COLOR, 1, 6));
        }
        if (active != null) {
            active.setBackground(PRIMARY_BLUE);
            active.setForeground(Color.WHITE);
            active.setBorder(new RoundedLineBorder(PRIMARY_BLUE, 1, 6));
        }
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
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 32));
        return lbl;
    }

    // Cập nhật hàm này để nhận thêm tham số tên Icon
    private JButton createFilterButton(String text, String iconName) {
        JButton btn = new JButton(text);
        btn.setIcon(new MenuIcon(iconName)); // Thêm Icon
        btn.setIconTextGap(8); // Tạo khoảng cách giữa Icon và Text
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(6, 14, 6, 14));
        return btn;
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new RoundedLineBorder(bg, 1, 8));
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setIconTextGap(8);
        return btn;
    }

    private int parseDays(String text) {
        try {
            return Integer.parseInt(text.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 999;
        }
    }

    private String formatCurrency(int value) {
        return String.format("%,dđ", value);
    }

    private String formatNumber(int value) {
        return String.format("%,d", value);
    }

    private String convertTrangThaiToText(TrangThaiLoHang tt) {
        if (tt == null) return "Không xác định";
        if (tt == TrangThaiLoHang.HET_HAN) return "Hết hạn";
        if (tt == TrangThaiLoHang.HET_HANG) return "Hết hàng";
        return "Được bán";
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private enum TrangThaiFilter {
        TAT_CA, DUOC_BAN, HET_HAN, GAN_HET_HAN_90, HET_HANG
    }

    private class BatchItem {
        String id, soLo, maSanPham, hanSuDung, trangThai;
        int tonKho, giaNhap;

        BatchItem(String i, String sl, String msp, int tk, int gn, String hsd, String tt) {
            id = i;
            soLo = sl;
            maSanPham = msp;
            tonKho = tk;
            giaNhap = gn;
            hanSuDung = hsd;
            trangThai = tt;
        }

        String getConLai() {
            try {
                LocalDate hsd = LocalDate.parse(hanSuDung, DATE_FORMAT);
                long days = ChronoUnit.DAYS.between(LocalDate.now(), hsd);
                if (days < 0) return "Quá " + Math.abs(days) + " ngày";
                return days + " ngày";
            } catch (Exception e) {
                return "Lỗi HSD";
            }
        }
    }

    private class LoHangCellRenderer extends DefaultTableCellRenderer {
        private final Color ROW_COLOR = new Color(250, 252, 255);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // Xóa icon cũ để tránh bị lặp icon sang cột khác khi Table Render cuộn
            lbl.setIcon(null); 
            
            lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lbl.setHorizontalAlignment(SwingConstants.LEFT);
            lbl.setOpaque(true);

            if (isSelected) {
                lbl.setBackground(new Color(224, 242, 254));
                lbl.setForeground(TEXT_PRIMARY);
            } else {
                lbl.setBackground(row % 2 == 0 ? Color.WHITE : ROW_COLOR);
                lbl.setForeground(TEXT_PRIMARY);
            }

            if (column == 0) {
                lbl.setForeground(TEXT_SECONDARY);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            } else if (column == 1) {
                lbl.setForeground(PRIMARY_BLUE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            } else if (column == 3 || column == 4) {
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                if (column == 3) lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            } else if (column == 5) {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            } else if (column == 6) {
                String text = String.valueOf(value);
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
                panel.setBackground(lbl.getBackground());

                JLabel pill = new JLabel(text);
                pill.setFont(new Font("Segoe UI", Font.BOLD, 12));
                pill.setOpaque(true);
                pill.setBorder(new CompoundRoundBorder(
                        new RoundedLineBorder(new Color(0, 0, 0, 0), 0, 12),
                        new Insets(4, 12, 4, 12)
                ));

                if (text.startsWith("Quá")) {
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
                        pill.setIcon(new MenuIcon("WARNING"));
                        pill.setBackground(GOLD_SOFT);
                        pill.setForeground(GOLD);
                    } else {
                        pill.setText(text);
                        pill.setIcon(new MenuIcon("CHECK_CIRCLE"));
                        pill.setBackground(SUCCESS_SOFT);
                        pill.setForeground(SUCCESS);
                    }
                    pill.setIconTextGap(6);
                }
                panel.add(pill);
                return panel;
            } else if (column == 7) { // CỘT TRẠNG THÁI: Thêm Icon trực quan
                String text = String.valueOf(value);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setIconTextGap(6); // Khoảng cách Icon và chữ
                
                if ("Được bán".equals(text)) {
                    lbl.setForeground(SUCCESS);
                    lbl.setIcon(new MenuIcon("CHECK_CIRCLE"));
                }
                else if ("Hết hạn".equals(text)) {
                    lbl.setForeground(DANGER);
                    lbl.setIcon(new MenuIcon("CANCEL"));
                }
                else {
                    lbl.setForeground(TEXT_SECONDARY);
                    lbl.setIcon(new MenuIcon("BOX"));
                }
            } else if (column == 8) {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setIcon(new MenuIcon("TRASH"));
                lbl.setText("");
                lbl.setForeground(DANGER);
                lbl.setToolTipText("Cập nhật hết hàng");
            }

            return lbl;
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

    private static class ShadowBorder extends AbstractBorder {
        private final Color shadow;
        private final int radius;

        public ShadowBorder(Color s, int r) {
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
                g2.setColor(new Color(
                        shadow.getRed(), shadow.getGreen(), shadow.getBlue(),
                        Math.max(1, shadow.getAlpha() - i * 2)
                ));
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

    public void setReadOnly(boolean readOnly) {
        if (!readOnly) return;
        disableButtonsByText(this, "Thêm mới", "Nhập Excel", "Thêm", "Xóa", "Sửa", "Lưu", "+ Nhập lô hàng", "Nhập lô hàng");
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