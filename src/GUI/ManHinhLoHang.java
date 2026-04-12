package GUI;

import DAO.DAO_LoHang;
import DAO.DAO_SanPham;
import Entity.KhoHang;
import Entity.LoHang;
import Entity.SanPham;
import Enum.TrangThaiLoHang;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ManHinhLoHang extends JPanel {

    private final List<BatchItem> dsTatCa = new ArrayList<>();
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DAO_LoHang daoLoHang = new DAO_LoHang();
    private final DAO_SanPham daoSanPham = new DAO_SanPham();

    private static final Color BG_APP = new Color(245, 247, 251);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color PRIMARY_SOFT = new Color(239, 246, 255);
    private static final Color SUCCESS = new Color(22, 163, 74);
    private static final Color SUCCESS_SOFT = new Color(240, 253, 244);
    private static final Color WARNING = new Color(234, 88, 12);
    private static final Color WARNING_SOFT = new Color(255, 247, 237);
    private static final Color GOLD = new Color(202, 138, 4);
    private static final Color GOLD_SOFT = new Color(254, 252, 232);
    private static final Color DANGER = new Color(220, 38, 38);
    private static final Color DANGER_SOFT = new Color(254, 242, 242);

    private JTextField txtSearch;
    private JCheckBox chkNear90;
    private JComboBox<String> cboSort;

    private JLabel lblExpired;
    private JLabel lblNear;
    private JLabel lblWarning;
    private JLabel lblGood;
    private JLabel lblTabBadge;
    private JLabel lblTotal;

    private JButton btnTatCa;
    private JButton btnDuocBan;
    private JButton btnHetHan;
    private JButton btnHetHang;

    private JTable table;
    private DefaultTableModel tableModel;

    private TrangThaiFilter filter = TrangThaiFilter.TAT_CA;

    public ManHinhLoHang() {
        setLayout(new BorderLayout());
        setBackground(BG_APP);
        setBorder(new EmptyBorder(22, 22, 22, 22));

        add(createMainCard(), BorderLayout.CENTER);

        loadDataFromDatabase();
    }

    private JPanel createMainCard() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);

        JPanel unifiedCard = new JPanel(new BorderLayout(0, 12));
        unifiedCard.setBackground(BG_CARD);
        unifiedCard.setBorder(new CompoundRoundBorder(
                new ShadowBorder(new Color(15, 23, 42, 12), 22),
                new Insets(16, 16, 16, 16)
        ));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(createToolbarHeader());
        content.add(Box.createVerticalStrut(12));
        content.add(createStatsRow());
        content.add(Box.createVerticalStrut(12));
        content.add(createFilterRow());
        content.add(Box.createVerticalStrut(12));
        content.add(createTableContainer());

        unifiedCard.add(content, BorderLayout.CENTER);
        outer.add(unifiedCard, BorderLayout.CENTER);
        return outer;
    }

    private JPanel createToolbarHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(12, 0));
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Lô hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(TEXT_PRIMARY);

        lblTabBadge = createCapsuleCounter("0", DANGER, Color.WHITE);

        titleRow.add(lblTitle);
        titleRow.add(lblTabBadge);

        JLabel lblSub = new JLabel("Quản lý tồn kho theo lô, theo dõi hạn sử dụng và trạng thái bán.");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(TEXT_SECONDARY);

        left.add(titleRow);
        left.add(Box.createVerticalStrut(4));
        left.add(lblSub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(250, 40));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(new RoundedLineBorder(new Color(220, 226, 236), 1, 14));
        txtSearch.setBackground(new Color(248, 250, 252));
        txtSearch.setToolTipText("Tìm mã lô, tên sản phẩm...");
        txtSearch.addActionListener(e -> refreshTable());

        cboSort = new JComboBox<>(new String[]{"Mới cập nhật", "Hạn gần nhất", "Tồn kho cao", "Tên A-Z"});
        cboSort.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboSort.setPreferredSize(new Dimension(145, 40));
        cboSort.setBackground(Color.WHITE);
        cboSort.addActionListener(e -> refreshTable());

        JButton btnThemLo = createButton("+ Thêm lô hàng", PRIMARY, Color.WHITE, new Insets(11, 16, 11, 16));
        btnThemLo.addActionListener(e -> moManHinhNhapLoMoi());

        JButton btnLamMoi = createButton("Làm mới", new Color(15, 23, 42), Color.WHITE, new Insets(11, 15, 11, 15));
        btnLamMoi.addActionListener(e -> {
            txtSearch.setText("");
            chkNear90.setSelected(false);
            cboSort.setSelectedIndex(0);
            filter = TrangThaiFilter.TAT_CA;
            setActiveFilterButton(btnTatCa);
            loadDataFromDatabase();
        });

        right.add(txtSearch);
        right.add(cboSort);
        right.add(btnThemLo);
        right.add(btnLamMoi);

        wrapper.add(left, BorderLayout.WEST);
        wrapper.add(right, BorderLayout.EAST);
        return wrapper;
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));

        lblExpired = statValueLabel();
        lblNear = statValueLabel();
        lblWarning = statValueLabel();
        lblGood = statValueLabel();

        row.add(createStatCard("Đã hết hạn", "⚠", lblExpired, DANGER_SOFT, DANGER));
        row.add(createStatCard("Gần hết hạn", "⏳", lblNear, WARNING_SOFT, WARNING));
        row.add(createStatCard("Cảnh báo", "◔", lblWarning, GOLD_SOFT, GOLD));
        row.add(createStatCard("Còn hạn tốt", "✓", lblGood, SUCCESS_SOFT, SUCCESS));

        return row;
    }

    private JPanel createFilterRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel lbl = new JLabel("Bộ lọc:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_SECONDARY);

        btnTatCa = createFilterButton("Tất cả");
        btnDuocBan = createFilterButton("Được bán");
        btnHetHan = createFilterButton("Hết hạn");
        btnHetHang = createFilterButton("Hết hàng");

        btnTatCa.addActionListener(e -> switchFilter(TrangThaiFilter.TAT_CA, btnTatCa));
        btnDuocBan.addActionListener(e -> switchFilter(TrangThaiFilter.DUOC_BAN, btnDuocBan));
        btnHetHan.addActionListener(e -> switchFilter(TrangThaiFilter.HET_HAN, btnHetHan));
        btnHetHang.addActionListener(e -> switchFilter(TrangThaiFilter.HET_HANG, btnHetHang));

        chkNear90 = new JCheckBox("≤ 90 ngày");
        chkNear90.setOpaque(false);
        chkNear90.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkNear90.setForeground(TEXT_SECONDARY);
        chkNear90.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                filter = TrangThaiFilter.GAN_HET_HAN_90;
                setActiveFilterButton(null);
            } else {
                filter = TrangThaiFilter.TAT_CA;
                setActiveFilterButton(btnTatCa);
            }
            refreshTable();
        });

        left.add(lbl);
        left.add(btnTatCa);
        left.add(btnDuocBan);
        left.add(btnHetHan);
        left.add(btnHetHang);
        left.add(Box.createHorizontalStrut(8));
        left.add(chkNear90);

        lblTotal = new JLabel("0 / 0 lô hàng");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotal.setForeground(TEXT_SECONDARY);

        row.add(left, BorderLayout.WEST);
        row.add(lblTotal, BorderLayout.EAST);

        setActiveFilterButton(btnTatCa);
        return row;
    }

    private JPanel createTableContainer() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setPreferredSize(new Dimension(0, 520));
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 520));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("Danh sách chi tiết lô hàng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        top.add(title, BorderLayout.WEST);

        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(new RoundedLineBorder(BORDER, 1, 16));
        tableCard.add(createTablePanel(), BorderLayout.CENTER);

        wrap.add(top, BorderLayout.NORTH);
        wrap.add(tableCard, BorderLayout.CENTER);
        return wrap;
    }

    private JScrollPane createTablePanel() {
        String[] cols = {
                "Mã lô", "Tên sản phẩm", "Tồn kho", "Giá nhập",
                "Hạn sử dụng", "Còn lại", "Tình trạng", "Thao tác"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(56);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(new Color(239, 242, 247));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setDefaultRenderer(Object.class, new LoHangCellRenderer());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setOpaque(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 44));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(238, 241, 246)));

        setColumnWidth(0, 130);
        setColumnWidth(1, 300);
        setColumnWidth(2, 100);
        setColumnWidth(3, 120);
        setColumnWidth(4, 130);
        setColumnWidth(5, 130);
        setColumnWidth(6, 140);
        setColumnWidth(7, 120);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0) return;

                String soLo = String.valueOf(table.getValueAt(row, 0));
                if (col == 7) {
                    xoaLo(soLo);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private void moManHinhNhapLoMoi() {
        Window owner = SwingUtilities.getWindowAncestor(this);

        ManHinhNhapLoHangMoi dialog = new ManHinhNhapLoHangMoi(owner,
                (sanPham, maLo, soLuong, giaNhap, hanSuDung, trangThai) -> {
                    try {
                        LoHang lo = new LoHang();
                        lo.setId("LH" + System.currentTimeMillis());
                        lo.setSoLoHang(maLo);
                        lo.setSanPhamId(sanPham);
                        lo.setSoLuongLoHang(soLuong);
                        lo.setGia(giaNhap);
                        lo.setNgayNhap(LocalDateTime.now());
                        lo.setNgayHetHan(LocalDate.parse(hanSuDung, DATE_FORMAT).atStartOfDay());
                        lo.setTrangThai(convertTextToTrangThai(trangThai));

                        KhoHang kho = new KhoHang();
                        kho.setId("KHO001");
                        lo.setKhoHangId(kho);

                        boolean ok = daoLoHang.themLoHang(lo);

                        if (ok) {
                            JOptionPane.showMessageDialog(this, "Thêm lô hàng thành công!");
                            loadDataFromDatabase();
                        } else {
                            JOptionPane.showMessageDialog(this, "Thêm lô hàng thất bại!");
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Lỗi khi lưu lô hàng vào database!");
                    }
                });

        dialog.setVisible(true);
    }
    private void loadDataFromDatabase() {
        dsTatCa.clear();

        try {
            List<LoHang> dsLo = daoLoHang.layDSLoHang();

            for (LoHang lh : dsLo) {
                if (lh == null) continue;

                String id = safe(lh.getId());
                String soLo = safe(lh.getSoLoHang());
                String tenSanPham = "";
                String sanPhamId = "";

                if (lh.getSanPhamId() != null) {
                    sanPhamId = safe(lh.getSanPhamId().getId());
                    tenSanPham = sanPhamId;

                    if (!sanPhamId.isEmpty()) {
                        SanPham sp = daoSanPham.getSanPhamTheoMa(sanPhamId);
                        if (sp != null && sp.getTen() != null && !sp.getTen().isBlank()) {
                            tenSanPham = sp.getTen();
                        }
                    }
                }

                int soLuong = lh.getSoLuongLoHang();
                int gia = lh.getGia();

                String hanSuDung = "";
                if (lh.getNgayHetHan() != null) {
                    hanSuDung = lh.getNgayHetHan().toLocalDate().format(DATE_FORMAT);
                }

                String trangThai = convertTrangThaiToText(lh.getTrangThai());

                dsTatCa.add(new BatchItem(
                        id,
                        soLo,
                        tenSanPham,
                        soLuong,
                        gia,
                        hanSuDung,
                        trangThai
                ));
            }

            updateStats();
            refreshTable();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không tải được dữ liệu lô hàng từ database!");
        }
    }

    private SanPham timSanPhamTheoTen(String tenSanPham) {
        if (tenSanPham == null || tenSanPham.trim().isEmpty()) return null;

        try {
            List<SanPham> ds = daoSanPham.getDsThuoc();
            for (SanPham sp : ds) {
                if (sp.getTen() != null && sp.getTen().trim().equalsIgnoreCase(tenSanPham.trim())) {
                    return sp;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void xoaLo(String soLo) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa lô " + soLo + " không?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            BatchItem item = findBatchBySoLo(soLo);
            if (item == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy lô cần xóa!");
                return;
            }

            boolean ok1 = daoLoHang.capNhatSoLuongTon(item.id, 0);
            boolean ok2 = daoLoHang.capNhatTrangThaiLo(item.id, TrangThaiLoHang.HET_HANG);

            if (ok1 && ok2) {
                JOptionPane.showMessageDialog(this, "Đã cập nhật lô về trạng thái hết hàng!");
                loadDataFromDatabase();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi xóa lô hàng!");
        }
    }

    private BatchItem findBatchBySoLo(String soLo) {
        for (BatchItem item : dsTatCa) {
            if (item.soLo.equalsIgnoreCase(soLo)) {
                return item;
            }
        }
        return null;
    }

    private void setColumnWidth(int col, int width) {
        TableColumn column = table.getColumnModel().getColumn(col);
        column.setPreferredWidth(width);
    }

    private void refreshTable() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();

        List<BatchItem> filtered = new ArrayList<>();
        for (BatchItem item : dsTatCa) {
            String conLai = item.getConLai();

            boolean matchKw = keyword.isEmpty()
                    || item.soLo.toLowerCase().contains(keyword)
                    || item.tenSanPham.toLowerCase().contains(keyword);

            boolean matchFilter = switch (filter) {
                case DUOC_BAN -> "Được bán".equals(item.trangThai);
                case HET_HAN -> conLai.startsWith("Quá") || "Hết hạn".equals(item.trangThai);
                case HET_HANG -> "Hết hàng".equals(item.trangThai);
                case GAN_HET_HAN_90 -> !conLai.startsWith("Quá") && parseDays(conLai) <= 90;
                default -> true;
            };

            if (matchKw && matchFilter) filtered.add(item);
        }

        applySort(filtered);

        tableModel.setRowCount(0);
        for (BatchItem item : filtered) {
            tableModel.addRow(new Object[]{
                    item.soLo,
                    item.tenSanPham,
                    formatNumber(item.tonKho),
                    formatCurrency(item.giaNhap),
                    item.hanSuDung,
                    item.getConLai(),
                    item.trangThai,
                    "Xóa"
            });
        }

        lblTotal.setText(filtered.size() + " / " + dsTatCa.size() + " lô hàng");
    }

    private void applySort(List<BatchItem> list) {
        String sortValue = cboSort == null ? "Mới cập nhật" : String.valueOf(cboSort.getSelectedItem());

        switch (sortValue) {
            case "Hạn gần nhất":
                list.sort(Comparator.comparing(this::getSortDateSafe));
                break;
            case "Tồn kho cao":
                list.sort((a, b) -> Integer.compare(b.tonKho, a.tonKho));
                break;
            case "Tên A-Z":
                list.sort(Comparator.comparing(a -> a.tenSanPham.toLowerCase()));
                break;
            default:
                list.sort((a, b) -> b.id.compareToIgnoreCase(a.id));
                break;
        }
    }

    private LocalDate getSortDateSafe(BatchItem item) {
        try {
            return LocalDate.parse(item.hanSuDung, DATE_FORMAT);
        } catch (Exception e) {
            return LocalDate.MAX;
        }
    }

    private void updateStats() {
        int expired = 0;
        int near = 0;
        int warning = 0;
        int good = 0;

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
        lblTabBadge.setText(String.valueOf(near + warning));
    }

    private void switchFilter(TrangThaiFilter newFilter, JButton source) {
        chkNear90.setSelected(false);
        filter = newFilter;
        setActiveFilterButton(source);
        refreshTable();
    }

    private void setActiveFilterButton(JButton active) {
        JButton[] list = {btnTatCa, btnDuocBan, btnHetHan, btnHetHang};
        for (JButton b : list) {
            if (b == null) continue;
            b.setBackground(Color.WHITE);
            b.setForeground(TEXT_SECONDARY);
            b.setBorder(new RoundedLineBorder(new Color(226, 232, 240), 1, 16));
        }

        if (active != null) {
            active.setBackground(PRIMARY_SOFT);
            active.setForeground(PRIMARY);
            active.setBorder(new RoundedLineBorder(new Color(147, 197, 253), 1, 16));
        }
    }

    private JPanel createStatCard(String title, String icon, JLabel value, Color bg, Color accent) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(bg);
        p.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(new Color(233, 237, 244), 1, 16),
                new Insets(12, 14, 12, 14)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(TEXT_SECONDARY);

        JLabel ic = new JLabel(icon, SwingConstants.CENTER);
        ic.setForeground(accent);
        ic.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

        value.setForeground(accent);

        top.add(t, BorderLayout.WEST);
        top.add(ic, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private JLabel statValueLabel() {
        JLabel lbl = new JLabel("0");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        return lbl;
    }

    private JButton createFilterButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_SECONDARY);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new RoundedLineBorder(new Color(226, 232, 240), 1, 15));
        btn.setMargin(new Insets(8, 12, 8, 12));
        return btn;
    }

    private JButton createButton(String text, Color bg, Color fg, Insets padding) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(padding.top, padding.left, padding.bottom, padding.right));
        return btn;
    }

    private JLabel createCapsuleCounter(String text, Color bg, Color fg) {
        JLabel lbl = new JLabel(text);
        lbl.setOpaque(true);
        lbl.setBackground(bg);
        lbl.setForeground(fg);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setBorder(new EmptyBorder(4, 9, 4, 9));
        return lbl;
    }

    private int parseDays(String text) {
        try {
            return Integer.parseInt(text.replace("ngày", "").trim());
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

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String convertTrangThaiToText(TrangThaiLoHang trangThai) {
        if (trangThai == null) return "Không xác định";

        switch (trangThai) {
            case HET_HAN:
                return "Hết hạn";
            case HET_HANG:
                return "Hết hàng";
            case CON_HANG:
            default:
                return "Được bán";
        }
    }

    private TrangThaiLoHang convertTextToTrangThai(String text) {
        if (text == null) return TrangThaiLoHang.CON_HANG;

        switch (text.trim()) {
            case "Hết hạn":
                return TrangThaiLoHang.HET_HAN;
            case "Hết hàng":
                return TrangThaiLoHang.HET_HANG;
            case "Được bán":
            default:
                return TrangThaiLoHang.CON_HANG;
        }
    }

    private enum TrangThaiFilter {
        TAT_CA, DUOC_BAN, HET_HAN, GAN_HET_HAN_90, HET_HANG
    }

    private class BatchItem {
        String id;
        String soLo;
        String tenSanPham;
        int tonKho;
        int giaNhap;
        String hanSuDung;
        String trangThai;

        BatchItem(String id, String soLo, String tenSanPham, int tonKho, int giaNhap, String hanSuDung, String trangThai) {
            this.id = id;
            this.soLo = soLo;
            this.tenSanPham = tenSanPham;
            this.tonKho = tonKho;
            this.giaNhap = giaNhap;
            this.hanSuDung = hanSuDung;
            this.trangThai = trangThai;
        }

        String getConLai() {
            try {
                LocalDate hsd = LocalDate.parse(hanSuDung, DATE_FORMAT);
                long days = ChronoUnit.DAYS.between(LocalDate.now(), hsd);
                if (days < 0) return "Quá " + Math.abs(days) + " ngày";
                return days + " ngày";
            } catch (Exception e) {
                return "Không hợp lệ";
            }
        }
    }

    private class LoHangCellRenderer extends DefaultTableCellRenderer {
        private final Color rowAlt = new Color(250, 252, 255);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setBorder(new EmptyBorder(0, 14, 0, 14));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lbl.setOpaque(true);

            if (isSelected) {
                lbl.setBackground(new Color(239, 246, 255));
                lbl.setForeground(TEXT_PRIMARY);
            } else {
                lbl.setBackground(row % 2 == 0 ? Color.WHITE : rowAlt);
                lbl.setForeground(TEXT_PRIMARY);
            }

            if (column == 0 || column == 1) {
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
            }

            if (column == 0) {
                lbl.setForeground(PRIMARY);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                return lbl;
            }

            if (column == 1) {
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                return lbl;
            }

            if (column == 5) {
                String text = String.valueOf(value);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                if (text.startsWith("Quá")) {
                    lbl.setForeground(DANGER);
                } else {
                    try {
                        int days = Integer.parseInt(text.replace("ngày", "").trim());
                        if (days <= 30) lbl.setForeground(WARNING);
                        else if (days <= 90) lbl.setForeground(GOLD);
                        else lbl.setForeground(SUCCESS);
                    } catch (Exception e) {
                        lbl.setForeground(TEXT_SECONDARY);
                    }
                }
                return lbl;
            }

            if (column == 6) {
                String text = String.valueOf(value);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setBorder(new EmptyBorder(10, 14, 10, 14));

                if ("Được bán".equals(text)) {
                    lbl.setBackground(SUCCESS_SOFT);
                    lbl.setForeground(SUCCESS);
                } else if ("Hết hạn".equals(text)) {
                    lbl.setBackground(DANGER_SOFT);
                    lbl.setForeground(DANGER);
                } else {
                    lbl.setBackground(WARNING_SOFT);
                    lbl.setForeground(WARNING);
                }
                return lbl;
            }

            if (column == 7) {
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setForeground(DANGER);
                lbl.setText("🗑 Xóa");
                return lbl;
            }

            return lbl;
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
            return new Insets(10, 12, 10, 12);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = 12;
            insets.right = 12;
            insets.top = 10;
            insets.bottom = 10;
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            for (int i = 0; i < thickness; i++) {
                g2.drawRoundRect(x + i, y + i, width - 1 - (i * 2), height - 1 - (i * 2), radius, radius);
            }
            g2.dispose();
        }
    }

    private static class ShadowBorder extends AbstractBorder {
        private final Color shadow;
        private final int radius;

        public ShadowBorder(Color shadow, int radius) {
            this.shadow = shadow;
            this.radius = radius;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(6, 6, 10, 6);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.top = 6;
            insets.left = 6;
            insets.bottom = 10;
            insets.right = 6;
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (int i = 0; i < 7; i++) {
                g2.setColor(new Color(
                        shadow.getRed(),
                        shadow.getGreen(),
                        shadow.getBlue(),
                        Math.max(4, shadow.getAlpha() - i * 3)
                ));
                g2.drawRoundRect(x + 1, y + 1 + i, width - 3, height - 3 - i, radius, radius);
            }
            g2.dispose();
        }
    }

    private static class CompoundRoundBorder extends AbstractBorder {
        private final AbstractBorder outer;
        private final Insets innerPadding;

        public CompoundRoundBorder(AbstractBorder outer, Insets innerPadding) {
            this.outer = outer;
            this.innerPadding = innerPadding;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            Insets o = outer.getBorderInsets(c);
            return new Insets(
                    o.top + innerPadding.top,
                    o.left + innerPadding.left,
                    o.bottom + innerPadding.bottom,
                    o.right + innerPadding.right
            );
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            Insets o = outer.getBorderInsets(c);
            insets.top = o.top + innerPadding.top;
            insets.left = o.left + innerPadding.left;
            insets.bottom = o.bottom + innerPadding.bottom;
            insets.right = o.right + innerPadding.right;
            return insets;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            outer.paintBorder(c, g, x, y, width, height);
        }
    }
}