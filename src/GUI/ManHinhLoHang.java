package GUI;

import DAO.DAO_LoHang;
import DAO.DAO_SanPham;
import Entity.KhoHang;
import Entity.LoHang;
import Entity.SanPham;
import Enumeration.TrangThaiLoHang;

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
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Thay thế bằng DAO thực tế của bạn
    private final DAO_LoHang daoLoHang = new DAO_LoHang();
    private final DAO_SanPham daoSanPham = new DAO_SanPham();

    // --- BẢNG MÀU CHUẨN UI/UX HIỆN ĐẠI ---
    private static final Color BG_APP = new Color(241, 245, 249); 
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color PRIMARY_BLUE = new Color(14, 116, 144);
    
    // Màu Semantic (Trạng thái)
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color DANGER_SOFT = new Color(254, 242, 242);
    private static final Color WARNING = new Color(249, 115, 22);
    private static final Color WARNING_SOFT = new Color(255, 247, 237);
    private static final Color GOLD = new Color(234, 179, 8);
    private static final Color GOLD_SOFT = new Color(254, 252, 232);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color SUCCESS_SOFT = new Color(240, 253, 244);

    // Các Component UI
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
        setBorder(new EmptyBorder(16, 16, 16, 16)); // Padding tổng

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
        JPanel wrapper = new JPanel(new BorderLayout(12, 0));
        wrapper.setOpaque(false);

        // --- TRÁI: Tiêu đề ---
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel lblIcon = new JLabel("📚"); 
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JLabel lblTitle = new JLabel("QUẢN LÝ LÔ HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(PRIMARY_BLUE);

        lblWarningBadge = new JLabel("⚠ 0 lô gần hết hạn!");
        lblWarningBadge.setOpaque(true);
        lblWarningBadge.setBackground(DANGER_SOFT);
        lblWarningBadge.setForeground(DANGER);
        lblWarningBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblWarningBadge.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(DANGER_SOFT, 1, 10),
                new Insets(4, 8, 4, 8) 
        ));

        left.add(lblIcon);
        left.add(lblTitle);
        left.add(lblWarningBadge);

        // --- PHẢI: Thanh tìm kiếm & Các nút chức năng ---
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        // Tìm kiếm
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(260, 38));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(BORDER_COLOR, 1, 18),
                new Insets(0, 14, 0, 14) 
        ));
        txtSearch.setToolTipText("Nhập mã lô, tên sản phẩm để tìm kiếm...");
        txtSearch.addActionListener(e -> refreshTable());

        // Nút Làm mới
        JButton btnLamMoi = createButton("🔄 Làm mới", new Color(248, 250, 252), TEXT_PRIMARY, new Insets(8, 14, 8, 14));
        btnLamMoi.setBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8));
        btnLamMoi.addActionListener(e -> {
            txtSearch.setText("");
            chkNear90.setSelected(false);
            filter = TrangThaiFilter.TAT_CA;
            setActiveFilterButton(btnTatCa);
            loadDataFromDatabase();
        });

        // Checkbox Chỉ sắp hết hạn
        JPanel chkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        chkPanel.setOpaque(false);
        chkPanel.setBorder(new CompoundRoundBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8), new Insets(4, 6, 4, 6)));
        chkPanel.setBackground(Color.WHITE);
        
        chkNear90 = new JCheckBox("Chỉ sắp hết hạn (≤90 ngày)");
        chkNear90.setOpaque(false);
        chkNear90.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkNear90.setForeground(TEXT_PRIMARY);
        chkNear90.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chkNear90.addItemListener(e -> {
            filter = e.getStateChange() == ItemEvent.SELECTED ? TrangThaiFilter.GAN_HET_HAN_90 : TrangThaiFilter.TAT_CA;
            setActiveFilterButton(filter == TrangThaiFilter.TAT_CA ? btnTatCa : null);
            refreshTable();
        });
        chkPanel.add(chkNear90);

        // Nút Nhập lô hàng
        JButton btnThemLo = createButton("+ Nhập lô hàng", DANGER, Color.WHITE, new Insets(8, 16, 8, 16));
        btnThemLo.addActionListener(e -> moManHinhNhapLoMoi());

        right.add(txtSearch);
        right.add(btnLamMoi);
        right.add(chkPanel);
        right.add(btnThemLo);

        wrapper.add(left, BorderLayout.WEST);
        wrapper.add(right, BorderLayout.EAST);
        return wrapper;
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 90));

        lblExpired = statValueLabel();
        lblNear = statValueLabel();
        lblWarning = statValueLabel();
        lblGood = statValueLabel();

        row.add(createStatCard("Đã hết hạn", "⛔", lblExpired, DANGER_SOFT, DANGER));
        row.add(createStatCard("Gần hết hạn (≤30 ngày)", "⚠", lblNear, WARNING_SOFT, WARNING));
        row.add(createStatCard("Cảnh báo (31-90 ngày)", "⏱", lblWarning, GOLD_SOFT, GOLD));
        row.add(createStatCard("Còn hạn tốt (>90 ngày)", "✅", lblGood, SUCCESS_SOFT, SUCCESS));

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

        btnTatCa = createFilterButton("Tất cả");
        btnDuocBan = createFilterButton("Được bán");
        btnHetHan = createFilterButton("Hết hạn");
        btnTamNgung = createFilterButton("Tạm ngưng");

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
                "#", "Mã lô", "Tên sản phẩm", "Tồn kho", "Giá nhập",
                "Hạn sử dụng", "Còn lại", "Tình trạng", "Thao tác"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
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
        table.getColumnModel().getColumn(2).setPreferredWidth(280); 
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
                    String soLo = String.valueOf(table.getValueAt(row, 1));
                    xoaLo(soLo);
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
            List<LoHang> dsLo = daoLoHang.layDSLoHang();
            if(dsLo != null) {
                for (LoHang lh : dsLo) {
                    if (lh == null) continue;

                    String id = lh.getId() == null ? "" : lh.getId();
                    String soLo = lh.getSoLoHang() == null ? "" : lh.getSoLoHang();
                    String tenSanPham = "";
                    if (lh.getSanPhamId() != null) {
                        SanPham sp = daoSanPham.getSanPhamTheoMa(lh.getSanPhamId().getId());
                        if (sp != null && sp.getTen() != null) tenSanPham = sp.getTen();
                    }

                    int soLuong = lh.getSoLuongLoHang();
                    int gia = lh.getGia();
                    String hanSuDung = lh.getNgayHetHan() != null ? lh.getNgayHetHan().toLocalDate().format(DATE_FORMAT) : "";
                    String trangThai = convertTrangThaiToText(lh.getTrangThai());

                    dsTatCa.add(new BatchItem(id, soLo, tenSanPham, soLuong, gia, hanSuDung, trangThai));
                }
            }
            updateStats();
            refreshTable();
        } catch (Exception e) {
            e.printStackTrace();
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
        if(countDanger > 0) {
            lblWarningBadge.setText("⚠ " + countDanger + " lô cần chú ý!");
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
            boolean matchKw = keyword.isEmpty() || item.soLo.toLowerCase().contains(keyword) || item.tenSanPham.toLowerCase().contains(keyword);
            
            boolean matchFilter = switch (filter) {
                case DUOC_BAN -> "Được bán".equals(item.trangThai);
                case HET_HAN -> conLai.startsWith("Quá") || "Hết hạn".equals(item.trangThai);
                case HET_HANG -> "Hết hàng".equals(item.trangThai);
                case GAN_HET_HAN_90 -> !conLai.startsWith("Quá") && parseDays(conLai) <= 90;
                default -> true;
            };

            if (matchKw && matchFilter) filtered.add(item);
        }

        tableModel.setRowCount(0);
        int stt = 1;
        for (BatchItem item : filtered) {
            tableModel.addRow(new Object[]{
                    stt++, item.soLo, item.tenSanPham,
                    formatNumber(item.tonKho), formatCurrency(item.giaNhap),
                    item.hanSuDung, item.getConLai(), item.trangThai, ""
            });
        }
        lblTotal.setText("Hiển thị " + filtered.size() + " / " + dsTatCa.size() + " lô");
    }

    // --- LOGIC GỌI MÀN HÌNH NHẬP LÔ MỚI ---
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

    // --- LOGIC XÓA LÔ HÀNG ---
    private void xoaLo(String soLo) {
        int confirm = JOptionPane.showConfirmDialog(
                this, 
                "Bạn có chắc muốn xóa/tạm ngưng lô " + soLo + " không?", 
                "Cảnh báo", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
             try {
                BatchItem item = null;
                for (BatchItem bi : dsTatCa) {
                    if (bi.soLo.equalsIgnoreCase(soLo)) {
                        item = bi;
                        break;
                    }
                }
                
                if (item == null) {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy lô cần xóa!");
                    return;
                }

                // Cập nhật số lượng về 0 và trạng thái về HẾT HÀNG theo logic cũ của bạn
                boolean ok1 = daoLoHang.capNhatSoLuongTon(item.id, 0);
                boolean ok2 = daoLoHang.capNhatTrangThaiLo(item.id, TrangThaiLoHang.HET_HANG);

                if (ok1 && ok2) {
                    JOptionPane.showMessageDialog(this, "Đã cập nhật lô về trạng thái hết hàng!");
                    loadDataFromDatabase();
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
                }

             } catch (Exception e) {
                 e.printStackTrace();
                 JOptionPane.showMessageDialog(this, "Lỗi khi xóa lô hàng!");
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

    private JPanel createStatCard(String title, String icon, JLabel value, Color bg, Color textC) {
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
        ic.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
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

    private JButton createFilterButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(6, 14, 6, 14));
        return btn;
    }

    private JButton createButton(String text, Color bg, Color fg, Insets padding) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new RoundedLineBorder(bg, 1, 8));
        return btn;
    }

    private int parseDays(String text) {
        try { return Integer.parseInt(text.replaceAll("[^0-9]", "")); } catch (Exception e) { return 999; }
    }
    private String formatCurrency(int value) { return String.format("%,dđ", value); }
    private String formatNumber(int value) { return String.format("%,d", value); }
    
    private String convertTrangThaiToText(TrangThaiLoHang tt) {
        if (tt == null) return "Không xác định";
        if(tt == TrangThaiLoHang.HET_HAN) return "Hết hạn";
        if(tt == TrangThaiLoHang.HET_HANG) return "Hết hàng";
        return "Được bán";
    }

    private TrangThaiLoHang convertTextToTrangThai(String text) {
        if (text == null) return TrangThaiLoHang.CON_HANG;
        switch (text.trim()) {
            case "Hết hạn": return TrangThaiLoHang.HET_HAN;
            case "Hết hàng": return TrangThaiLoHang.HET_HANG;
            case "Được bán":
            default: return TrangThaiLoHang.CON_HANG;
        }
    }

    // --- ENUM & LỚP DỮ LIỆU ---
    private enum TrangThaiFilter { TAT_CA, DUOC_BAN, HET_HAN, GAN_HET_HAN_90, HET_HANG }

    private class BatchItem {
        String id, soLo, tenSanPham, hanSuDung, trangThai;
        int tonKho, giaNhap;

        BatchItem(String i, String sl, String tsp, int tk, int gn, String hsd, String tt) {
            id = i; soLo = sl; tenSanPham = tsp; tonKho = tk; giaNhap = gn; hanSuDung = hsd; trangThai = tt;
        }

        String getConLai() {
            try {
                LocalDate hsd = LocalDate.parse(hanSuDung, DATE_FORMAT);
                long days = ChronoUnit.DAYS.between(LocalDate.now(), hsd);
                if (days < 0) return "Quá " + Math.abs(days) + " ngày";
                return days + " ngày";
            } catch (Exception e) { return "Lỗi HSD"; }
        }
    }

    // --- RENDERER CHO BẢNG ---
    private class LoHangCellRenderer extends DefaultTableCellRenderer {
        private final Color ROW_COLOR = new Color(250, 252, 255); 

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
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

            // STT
            if (column == 0) {
                lbl.setForeground(TEXT_SECONDARY);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            }
            // Mã lô
            else if (column == 1) {
                lbl.setForeground(PRIMARY_BLUE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }
            // Tồn kho & Giá nhập
            else if (column == 3 || column == 4) {
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                if(column == 3) lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }
            // HSD
            else if(column == 5) {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            }
            // Cột "Còn lại"
            else if (column == 6) {
                String text = String.valueOf(value);
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
                panel.setBackground(lbl.getBackground());
                
                JLabel pill = new JLabel(text);
                pill.setFont(new Font("Segoe UI", Font.BOLD, 12));
                pill.setOpaque(true);
                pill.setBorder(new CompoundRoundBorder(new RoundedLineBorder(new Color(0,0,0,0), 0, 12), new Insets(4, 12, 4, 12)));

                if (text.startsWith("Quá")) {
                    pill.setText("⛔ " + text);
                    pill.setBackground(DANGER_SOFT); pill.setForeground(DANGER);
                } else {
                    int days = parseDays(text);
                    if (days <= 30) {
                        pill.setText("⚠ " + text);
                        pill.setBackground(WARNING_SOFT); pill.setForeground(WARNING);
                    } else if (days <= 90) {
                        pill.setText("⏱ " + text);
                        pill.setBackground(GOLD_SOFT); pill.setForeground(GOLD);
                    } else {
                        pill.setText("✅ " + text);
                        pill.setBackground(SUCCESS_SOFT); pill.setForeground(SUCCESS);
                    }
                }
                panel.add(pill);
                return panel;
            }
            // Trạng thái
            else if (column == 7) {
                String text = String.valueOf(value);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                if ("Được bán".equals(text)) lbl.setForeground(SUCCESS);
                else if ("Hết hạn".equals(text)) lbl.setForeground(DANGER);
                else lbl.setForeground(TEXT_SECONDARY);
            }
            // Nút Xóa
            else if (column == 8) {
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
                lbl.setForeground(DANGER);
                lbl.setText("🗑");
                lbl.setToolTipText("Xóa lô hàng này");
            }

            return lbl;
        }
    }

    // --- CÁC CLASS ĐỊNH DẠNG UI CUSTOM ---
    private static class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int thickness, radius;
        public RoundedLineBorder(Color c, int t, int r) { color = c; thickness = t; radius = r; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            for (int i = 0; i < thickness; i++) g2.drawRoundRect(x + i, y + i, w - 1 - i * 2, h - 1 - i * 2, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
        @Override public Insets getBorderInsets(Component c, Insets i) { i.left = i.right = i.top = i.bottom = radius/2; return i; }
    }

    private static class ShadowBorder extends AbstractBorder {
        private final Color shadow;
        private final int radius;
        public ShadowBorder(Color s, int r) { shadow = s; radius = r; }
        @Override public Insets getBorderInsets(Component c) { return new Insets(4, 4, 8, 4); }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
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
        
        public CompoundRoundBorder(AbstractBorder o, Insets i) { 
            outer = o; 
            inner = i; 
        }
        
        @Override public Insets getBorderInsets(Component c) {
            Insets o = outer.getBorderInsets(c);
            return new Insets(o.top + inner.top, o.left + inner.left, o.bottom + inner.bottom, o.right + inner.right);
        }
        
        @Override public Insets getBorderInsets(Component c, Insets insets) {
            Insets o = outer.getBorderInsets(c);
            insets.top = o.top + inner.top;
            insets.left = o.left + inner.left;
            insets.bottom = o.bottom + inner.bottom;
            insets.right = o.right + inner.right;
            return insets;
        }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) { 
            outer.paintBorder(c, g, x, y, w, h); 
        }
    }
    /**
     * Phân quyền: Ẩn nút Thêm/Sửa/Xóa cho STAFF.
     */
    public void setReadOnly(boolean readOnly) {
        if (!readOnly) return;

        disableButtonsByText(this, "Thêm mới", "Nhập Excel", "Thêm", "Xóa", "Sửa", "Lưu", "+ Nhập lô hàng");
    }

    private void disableButtonsByText(java.awt.Container container, String... texts) {
        for (java.awt.Component c : container.getComponents()) {
            if (c instanceof javax.swing.JButton) {
                javax.swing.JButton btn = (javax.swing.JButton) c;
                for (String t : texts) {
                    if (t.equals(btn.getText())) { btn.setVisible(false); break; }
                }
            } else if (c instanceof java.awt.Container) {
                disableButtonsByText((java.awt.Container) c, texts);
            }
        }
    }

}