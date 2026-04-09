package GUI;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ManHinhBanHang extends JPanel {

    private JTable tableHoaDon;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JButton[] categoryButtons;
    private JButton[] statusButtons;
    private String currentStatusFilter = "Tất cả";
    private String currentCategoryFilter = "Tất cả";

    public ManHinhBanHang() {
        initUI();
    }

    private void initUI() {
        this.setLayout(new BorderLayout(15, 15));
        this.setBackground(Color.WHITE);
        this.setBorder(BorderFactory.createEmptyBorder(0, 25, 20, 25));

        // ==================== THANH TAB SUB-NAVIGATION ====================
        JPanel panelTabs = new JPanel(new BorderLayout());
        panelTabs.setBackground(Color.WHITE);
        panelTabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));

        JPanel pnlLeftTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        pnlLeftTabs.setBackground(Color.WHITE);

        JLabel lblBanHang = new JLabel("Bán hàng");
        lblBanHang.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBanHang.setForeground(Color.decode("#2179E0"));
        lblBanHang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, Color.decode("#2179E0")),
                BorderFactory.createEmptyBorder(15, 5, 15, 5)));

        JLabel lblDoiTra = new JLabel("Đổi / Trả hàng");
        lblDoiTra.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDoiTra.setForeground(Color.decode("#6C757D"));
        lblDoiTra.setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));

        pnlLeftTabs.add(lblBanHang);
        pnlLeftTabs.add(lblDoiTra);

        JLabel lblQuanLy = new JLabel("Quản lý");
        lblQuanLy.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblQuanLy.setForeground(Color.decode("#10B981"));
        lblQuanLy.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 0));

        panelTabs.add(pnlLeftTabs, BorderLayout.WEST);
        panelTabs.add(lblQuanLy, BorderLayout.EAST);

        // ==================== HEADER (SEARCH & ACTIONS) ====================
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(Color.WHITE);
        panelHeader.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JLabel lblTitle = new JLabel("BÁN HÀNG — HÓA ĐƠN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.decode("#212B36"));

        JPanel panelActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelActions.setBackground(Color.WHITE);

        // Ô TÌM KIẾM
        JTextField txtTimKiem = new JTextField(" Mã HD, khách hàng, SĐT...");
        txtTimKiem.setPreferredSize(new Dimension(250, 38));
        txtTimKiem.setForeground(Color.LIGHT_GRAY);
        txtTimKiem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8")),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));

        // Xử lý placeholder và tìm kiếm
        txtTimKiem.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtTimKiem.getText().equals(" Mã HD, khách hàng, SĐT...")) {
                    txtTimKiem.setText("");
                    txtTimKiem.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (txtTimKiem.getText().isEmpty()) {
                    txtTimKiem.setForeground(Color.LIGHT_GRAY);
                    txtTimKiem.setText(" Mã HD, khách hàng, SĐT...");
                }
            }
        });

        txtTimKiem.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = txtTimKiem.getText();
                if (text.trim().isEmpty() || text.equals(" Mã HD, khách hàng, SĐT...")) {
                    applyCombinedFilter(); 
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        JButton btnLamMoi = new JButton("↻ Làm mới");
        btnLamMoi.setPreferredSize(new Dimension(110, 38));
        btnLamMoi.setBackground(Color.decode("#6C757D"));
        btnLamMoi.setForeground(Color.WHITE);
        btnLamMoi.setFocusPainted(false);
        btnLamMoi.addActionListener(e -> {
            txtTimKiem.setText(" Mã HD, khách hàng, SĐT...");
            txtTimKiem.setForeground(Color.LIGHT_GRAY);
            xuLyClickNutTrangThai(statusButtons[0]);
            xuLyClickNutDanhMuc(categoryButtons[0]);
        });

        JButton btnTaoHoaDon = new JButton("+ Tạo hóa đơn");
        btnTaoHoaDon.setPreferredSize(new Dimension(140, 38));
        btnTaoHoaDon.setBackground(Color.decode("#E11D48"));
        btnTaoHoaDon.setForeground(Color.WHITE);
        btnTaoHoaDon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnTaoHoaDon.setFocusPainted(false);
        btnTaoHoaDon.addActionListener(e -> {
            Window parent = SwingUtilities.getWindowAncestor(this);
            TaoHoaDon dialog = new TaoHoaDon((Frame) parent);
            dialog.setVisible(true);
        });

        panelActions.add(txtTimKiem);
        panelActions.add(btnLamMoi);
        panelActions.add(btnTaoHoaDon);

        panelHeader.add(lblTitle, BorderLayout.WEST);
        panelHeader.add(panelActions, BorderLayout.EAST);

        // ==================== BỘ LỌC ====================
        JPanel panelFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 15));
        panelFilters.setBackground(Color.WHITE);

        panelFilters.add(new JLabel("Trạng thái:"));
        JButton btnStTatCa = createFilterButton("Tất cả", true, false);
        JButton btnStHoanThanh = createFilterButton("Hoàn thành", false, false);
        JButton btnStDangXuLy = createFilterButton("Đang xử lý", false, false);
        JButton btnStDaHuy = createFilterButton("Đã hủy", false, false);
        statusButtons = new JButton[]{btnStTatCa, btnStHoanThanh, btnStDangXuLy, btnStDaHuy};
        for (JButton btn : statusButtons) {
            panelFilters.add(btn);
            btn.addActionListener(e -> xuLyClickNutTrangThai(btn));
        }

        panelFilters.add(new JLabel("   |   Danh mục:"));
        JButton btnTatCa = createFilterButton("Tất cả", true, true);
        JButton btnKeDon = createFilterButton("Thuốc kê đơn", false, true);
        JButton btnKhongKeDon = createFilterButton("Thuốc không kê đơn", false, true);
        JButton btnTPCN = createFilterButton("TPCN", false, true);
        JButton btnMyPham = createFilterButton("Mỹ phẩm", false, true);
        categoryButtons = new JButton[]{btnTatCa, btnKeDon, btnKhongKeDon, btnTPCN, btnMyPham};
        for (JButton btn : categoryButtons) {
            panelFilters.add(btn);
            btn.addActionListener(e -> xuLyClickNutDanhMuc(btn));
        }

        JPanel panelNorth = new JPanel(new BorderLayout());
        panelNorth.setBackground(Color.WHITE);
        panelNorth.add(panelTabs, BorderLayout.NORTH);
        JPanel pnlCenterOfNorth = new JPanel(new BorderLayout());
        pnlCenterOfNorth.setBackground(Color.WHITE);
        pnlCenterOfNorth.add(panelHeader, BorderLayout.NORTH);
        pnlCenterOfNorth.add(panelFilters, BorderLayout.CENTER);
        panelNorth.add(pnlCenterOfNorth, BorderLayout.CENTER);
        this.add(panelNorth, BorderLayout.NORTH);

        // ==================== BẢNG DỮ LIỆU ====================
        String[] columnNames = {"Mã HD", "Ngày", "Khách hàng", "SĐT", "Thanh toán", "Tổng tiền", "Trạng thái", "Xem HD", "DanhMucHidden"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableHoaDon = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        tableHoaDon.setRowSorter(rowSorter);
        tableHoaDon.setRowHeight(45);
        tableHoaDon.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableHoaDon.setShowVerticalLines(false);
        tableHoaDon.setGridColor(Color.decode("#DFE3E8"));

        tableHoaDon.getColumnModel().getColumn(8).setMinWidth(0);
        tableHoaDon.getColumnModel().getColumn(8).setMaxWidth(0);
        tableHoaDon.getColumnModel().getColumn(8).setWidth(0);

        CustomRenderer customRenderer = new CustomRenderer();
        for (int i = 0; i < tableHoaDon.getColumnCount() - 1; i++) {
            tableHoaDon.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }

        JTableHeader header = tableHoaDon.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(Color.decode("#F4F6F8"));
        header.setForeground(Color.decode("#637381"));
        header.setPreferredSize(new Dimension(100, 45));

        JScrollPane scrollPane = new JScrollPane(tableHoaDon);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8")));
        scrollPane.getViewport().setBackground(Color.WHITE);
        this.add(scrollPane, BorderLayout.CENTER);

        loadDuLieuMau();
    }

    private void xuLyClickNutTrangThai(JButton clickedBtn) {
        for (JButton btn : statusButtons) resetButton(btn);
        setActiveButton(clickedBtn, false);
        currentStatusFilter = clickedBtn.getText().trim();
        applyCombinedFilter();
    }

    private void xuLyClickNutDanhMuc(JButton clickedBtn) {
        for (JButton btn : categoryButtons) resetButton(btn);
        setActiveButton(clickedBtn, true);
        currentCategoryFilter = clickedBtn.getText().trim();
        applyCombinedFilter();
    }

    private void applyCombinedFilter() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (!currentStatusFilter.equals("Tất cả")) filters.add(RowFilter.regexFilter("(?i)^" + currentStatusFilter + "$", 6));
        if (!currentCategoryFilter.equals("Tất cả")) filters.add(RowFilter.regexFilter("(?i)^" + currentCategoryFilter + "$", 8));
        
        if (filters.isEmpty()) rowSorter.setRowFilter(null);
        else rowSorter.setRowFilter(RowFilter.andFilter(filters));
    }

    private void resetButton(JButton btn) {
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.decode("#374151"));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#D1D5DB")),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
    }

    private void setActiveButton(JButton btn, boolean isPurple) {
        String hexColor = isPurple ? "#9300D9" : "#1967D2";
        btn.setBackground(Color.decode(hexColor));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 11, 6, 11));
    }

    private JButton createFilterButton(String text, boolean isActive, boolean isPurple) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        if (isActive) setActiveButton(btn, isPurple);
        else resetButton(btn);
        return btn;
    }

    class CustomRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setOpaque(true);

            if (column == 6) { // Cột Trạng thái
                String stt = (value != null) ? value.toString() : "";
                if (stt.equals("Hoàn thành")) {
                    label.setForeground(Color.decode("#10B981"));
                    label.setBackground(Color.decode("#DCFCE7"));
                } else if (stt.equals("Đang xử lý")) {
                    label.setForeground(Color.decode("#F59E0B"));
                    label.setBackground(Color.decode("#FEF3C7"));
                } else if (stt.equals("Đã hủy")) {
                    label.setForeground(Color.decode("#EF4444"));
                    label.setBackground(Color.decode("#FEE2E2"));
                }
            } else {
                label.setBackground(isSelected ? Color.decode("#F4F6F8") : Color.WHITE);
                label.setForeground(Color.decode("#212B36"));
            }
            
            if (column == 0 || column == 7) label.setForeground(Color.decode("#6A1B9A"));

            return label;
        }
    }

    public void loadDuLieuMau() {
        tableModel.addRow(new Object[]{"HD-2024-0001", "05/04/2026", "Nguyễn Văn An", "0912345678", "Tiền mặt", "104.000đ", "Hoàn thành", "Chi tiết", "Thuốc kê đơn"});
        tableModel.addRow(new Object[]{"HD-2024-0002", "06/04/2026", "Trần Thị Bình", "0987654321", "Chuyển khoản", "105.000đ", "Đang xử lý", "Chi tiết", "Thuốc không kê đơn"});
        tableModel.addRow(new Object[]{"HD-2024-0003", "09/04/2026", "Lê Văn Cường", "0901234567", "Tiền mặt", "206.250đ", "Đã hủy", "Chi tiết", "TPCN"});
        tableModel.addRow(new Object[]{"HD-2024-0004", "09/04/2026", "Phạm Thị Dung", "0978901234", "Chuyển khoản", "253.000đ", "Hoàn thành", "Chi tiết", "Mỹ phẩm"});
        tableModel.addRow(new Object[]{"HD-2024-0005", "10/04/2026", "Lê Thị Thu", "0911223344", "Tiền mặt", "500.000đ", "Đang xử lý", "Chi tiết", "Thuốc kê đơn"});
        tableModel.addRow(new Object[]{"HD-2024-0006", "11/04/2026", "Trịnh Văn Phát", "0999888777", "Tiền mặt", "120.000đ", "Đã hủy", "Chi tiết", "Mỹ phẩm"});
    }
}