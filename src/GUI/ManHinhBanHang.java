package GUI;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ManHinhBanHang extends JPanel {

    private JTable tableHoaDon;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter; 
    
    // Mảng chứa các nút để dễ đổi màu khi click
    private JButton[] categoryButtons; 
    private JButton[] statusButtons;
    
    // Lưu trữ điều kiện lọc hiện tại
    private String currentStatusFilter = "Tất cả";
    private String currentCategoryFilter = "Tất cả";

    // Khai báo BUS
    // private BUS.BUS_HoaDon busHoaDon; 

    public ManHinhBanHang() {
        // busHoaDon = new BUS.BUS_HoaDon(); 
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
                BorderFactory.createEmptyBorder(15, 5, 15, 5) 
        ));

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

        // ==================== HEADER ====================
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(Color.WHITE);
        panelHeader.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JLabel lblTitle = new JLabel("BÁN HÀNG — HÓA ĐƠN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.decode("#212B36"));

        JPanel panelActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelActions.setBackground(Color.WHITE);

        JTextField txtTimKiem = new JTextField(20);
        txtTimKiem.setPreferredSize(new Dimension(220, 38));
        txtTimKiem.setText(" Mã HD, khách hàng, SĐT...");
        txtTimKiem.setForeground(Color.LIGHT_GRAY);

        JButton btnLamMoi = new JButton("↻ Làm mới");
        btnLamMoi.setPreferredSize(new Dimension(110, 38));
        btnLamMoi.setBackground(Color.decode("#6C757D"));
        btnLamMoi.setForeground(Color.WHITE);
        btnLamMoi.setFocusPainted(false);
        btnLamMoi.setOpaque(true);
        btnLamMoi.setBorderPainted(false);

        JButton btnTaoHoaDon = new JButton("+ Tạo hóa đơn");
        btnTaoHoaDon.setPreferredSize(new Dimension(140, 38));
        btnTaoHoaDon.setBackground(Color.decode("#E11D48")); 
        btnTaoHoaDon.setForeground(Color.WHITE);
        btnTaoHoaDon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnTaoHoaDon.setFocusPainted(false);
        btnTaoHoaDon.setOpaque(true);
        btnTaoHoaDon.setBorderPainted(false);

        panelActions.add(txtTimKiem);
        panelActions.add(btnLamMoi);
        panelActions.add(btnTaoHoaDon);

        panelHeader.add(lblTitle, BorderLayout.WEST);
        panelHeader.add(panelActions, BorderLayout.EAST);

        // ==================== BỘ LỌC ====================
        JPanel panelFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 15));
        panelFilters.setBackground(Color.WHITE);

        panelFilters.add(new JLabel("Trạng thái:"));
        
        // 1. TẠO VÀ GẮN SỰ KIỆN CHO NÚT TRẠNG THÁI (Màu Xanh)
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
        
        // 2. TẠO VÀ GẮN SỰ KIỆN CHO NÚT DANH MỤC (Màu Tím)
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

        // ==================== BẢNG ====================
        String[] columnNames = {"Mã HD", "Ngày", "Khách hàng", "SĐT", "Thanh toán", "Tổng tiền", "Trạng thái", "Xem HD", "DanhMucHidden"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableHoaDon = new JTable(tableModel);
        
        rowSorter = new TableRowSorter<>(tableModel);
        tableHoaDon.setRowSorter(rowSorter);
        
        tableHoaDon.setRowHeight(45);
        tableHoaDon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHoaDon.setShowVerticalLines(false);
        tableHoaDon.setGridColor(Color.decode("#DFE3E8")); 

        // Ẩn cột Danh mục (cột thứ 9)
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

    // ==================== XỬ LÝ SỰ KIỆN CLICK VÀ ĐỔI MÀU ====================

    // Xử lý Click nút TRẠNG THÁI
    private void xuLyClickNutTrangThai(JButton clickedBtn) {
        for (JButton btn : statusButtons) {
            resetButton(btn); // Trả tất cả về viền xám nền trắng
        }
        setActiveButton(clickedBtn, false); // false = Đổi nút được chọn sang Xanh dương
        
        currentStatusFilter = clickedBtn.getText();
        applyCombinedFilter();
    }

    // Xử lý Click nút DANH MỤC
    private void xuLyClickNutDanhMuc(JButton clickedBtn) {
        for (JButton btn : categoryButtons) {
            resetButton(btn); // Trả tất cả về viền xám nền trắng
        }
        setActiveButton(clickedBtn, true); // true = Đổi nút được chọn sang Tím
        
        currentCategoryFilter = clickedBtn.getText();
        applyCombinedFilter();
    }

    // Hàm áp dụng LỌC KẾP
    private void applyCombinedFilter() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        if (!currentStatusFilter.equals("Tất cả")) {
            filters.add(RowFilter.regexFilter("(?i)^" + currentStatusFilter + "$", 6));
        }

        if (!currentCategoryFilter.equals("Tất cả")) {
            filters.add(RowFilter.regexFilter("(?i)^" + currentCategoryFilter + "$", 8));
        }

        if (filters.isEmpty()) {
            rowSorter.setRowFilter(null); 
        } else {
            rowSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    // ==================== HÀM CẤU HÌNH MÀU SẮC NÚT BẤM ====================

    // Hàm reset nút về trạng thái CHƯA CHỌN (Nền trắng, chữ đen, viền xám)
    private void resetButton(JButton btn) {
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.decode("#374151")); // Chữ xám đậm
        btn.setBorder(BorderFactory.createLineBorder(Color.decode("#D1D5DB"))); // Viền xám nhạt
    }

    // Hàm set nút về trạng thái ĐANG CHỌN (Nền xanh/tím, chữ trắng)
    private void setActiveButton(JButton btn, boolean isPurple) {
        // Màu Xanh chuẩn từ ảnh của bạn: #1967D2
        String hexColor = isPurple ? "#9300D9" : "#1967D2"; 
        
        btn.setBackground(Color.decode(hexColor));
        btn.setForeground(Color.WHITE);
        // Set viền cùng màu nền để kích thước nút không bị co giật khi click
        btn.setBorder(BorderFactory.createLineBorder(Color.decode(hexColor))); 
    }

    // Khởi tạo nút ban đầu
    private JButton createFilterButton(String text, boolean isActive, boolean isPurple) {
        JButton btn = new JButton("  " + text + "  "); // Thêm khoảng trắng cho nút rộng ra chút
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        if (isActive) {
            setActiveButton(btn, isPurple);
        } else {
            resetButton(btn);
        }
        return btn;
    }

    // ==================== RENDER BẢNG ====================
    class CustomRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(JLabel.CENTER);

            if (isSelected) {
                c.setBackground(Color.decode("#F4F6F8")); 
            } else {
                c.setBackground(Color.WHITE);
            }
            c.setForeground(Color.decode("#212B36"));

            if (column == 0) { c.setForeground(Color.decode("#6A1B9A")); } 
            
            if (column == 6) { 
                String stt = (value != null) ? value.toString() : "";
                if(stt.equals("Hoàn thành")) c.setForeground(Color.decode("#10B981")); 
                else if(stt.equals("Đang xử lý")) c.setForeground(Color.decode("#F59E0B")); 
                else if(stt.equals("Đã hủy")) c.setForeground(Color.decode("#EF4444")); 
            }
            
            if (column == 7) { c.setForeground(Color.decode("#6A1B9A")); } 
            return c;
        }
    }

    // Load data mẫu (Bạn có thể bỏ khi nối database)
    public void loadDuLieuMau() {
        tableModel.addRow(new Object[]{"HD-2024-0001", "05/04/2026", "Nguyễn Văn An", "0912345678", "Tiền mặt", "104.000đ", "Hoàn thành", "Chi tiết", "Thuốc kê đơn"});
        tableModel.addRow(new Object[]{"HD-2024-0002", "06/04/2026", "Trần Thị Bình", "0987654321", "Chuyển khoản", "105.000đ", "Đang xử lý", "Chi tiết", "Thuốc không kê đơn"});
        tableModel.addRow(new Object[]{"HD-2024-0003", "09/04/2026", "Lê Văn Cường", "0901234567", "Tiền mặt", "206.250đ", "Đã hủy", "Chi tiết", "TPCN"});
        tableModel.addRow(new Object[]{"HD-2024-0004", "09/04/2026", "Phạm Thị Dung", "0978901234", "Chuyển khoản", "253.000đ", "Hoàn thành", "Chi tiết", "Mỹ phẩm"});
        tableModel.addRow(new Object[]{"HD-2024-0005", "10/04/2026", "Lê Thị Thu", "0911223344", "Tiền mặt", "500.000đ", "Đang xử lý", "Chi tiết", "Thuốc kê đơn"});
        tableModel.addRow(new Object[]{"HD-2024-0006", "11/04/2026", "Trịnh Văn Phát", "0999888777", "Tiền mặt", "120.000đ", "Đã hủy", "Chi tiết", "Mỹ phẩm"});
    }
}