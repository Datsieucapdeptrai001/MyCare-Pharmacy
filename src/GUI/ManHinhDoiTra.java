package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import Utils.MenuIcon;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import BUS.*;
import Utils.UserSession;
import javax.swing.Timer;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
public class ManHinhDoiTra extends JPanel {
	protected static String pendingDoiTraIdToOpen;
	private Timer autoCancelTimer;
	private java.util.Map<String, java.time.LocalDateTime> mapThoiGianTao = new java.util.HashMap<>();
    /** Dùng để điều hướng từ Live Notification — set trước khi switchTabAndFilter */
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton[] statusBtns;
    private String filterStatus = "Tất cả";
    private JTextField txtSearch;
    private BUS_HoaDon busHD = new BUS_HoaDon();
    private BUS_ChiTietHoaDon busCTHD = new BUS_ChiTietHoaDon();
    private JPanel pnlDetail, pnlRightWrapper; // Bổ sung pnlRightWrapper
    private JLabel lblDetailTitle, lblDetailDate, lblDetailEmp;
    private JPanel pnlDetailProducts;
    private String expandedMaPhieu = ""; 
    private final int ROW_HEIGHT_NORMAL = 55;
    private int currentDetailHeight = 220;
    private JLabel lblDetailTimer;
    public ManHinhDoiTra() {
        initUI();
        loadDataToTable();
        
        // Kích hoạt đồng hồ đếm ngược trên bảng
        khoiDongBoDemNguoc(); 

       

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                if (txtSearch != null) {
                    txtSearch.setText("Mã phiếu, mã HĐ gốc...");
                    txtSearch.setForeground(Color.GRAY);
                }
                loadDataToTable(); // Gọi lại hàm load dữ liệu
            }
        });
    }

    private void initUI() {
        this.setLayout(new BorderLayout(0, 0));
        this.setBackground(Color.WHITE);
        this.setBorder(new EmptyBorder(0, 30, 20, 30));

        // ==================== 1. THANH TABS ====================
        JPanel pnlTabs = new JPanel(new BorderLayout());
        pnlTabs.setBackground(Color.WHITE);
        pnlTabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));

        JPanel pnlLeftTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlLeftTabs.setOpaque(false);

        JLabel lblBanHang = new JLabel("Bán hàng");
        lblBanHang.setIcon(new MenuIcon("CART"));
        lblBanHang.setIconTextGap(8);
        lblBanHang.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblBanHang.setForeground(Color.decode("#6C757D"));
        lblBanHang.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 40));
        lblBanHang.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblBanHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { chuyenManHinh("Bán hàng & Đổi trả"); }
        });

        JLabel lblDoiTra = new JLabel("Đổi / Trả hàng");
        lblDoiTra.setIcon(new MenuIcon("BOX"));
        lblDoiTra.setIconTextGap(8);
        lblDoiTra.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDoiTra.setForeground(Color.decode("#E11D48")); 
        lblDoiTra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, Color.decode("#E11D48")),
                BorderFactory.createEmptyBorder(20, 0, 20, 40)));

        
        pnlLeftTabs.add(lblBanHang);
        pnlLeftTabs.add(lblDoiTra);
       
        pnlTabs.add(pnlLeftTabs, BorderLayout.WEST);

        // ==================== 2. HEADER (TITLE & SEARCH) ====================
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false); 
        pnlHeader.setBorder(new EmptyBorder(25, 0, 15, 0));

        JLabel lblTitle = new JLabel("ĐỔI / TRẢ HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.decode("#212B36"));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setOpaque(false);

        JPanel pnlSearchWrapper = new JPanel(new BorderLayout(8, 0));
        pnlSearchWrapper.setBackground(Color.WHITE);
        pnlSearchWrapper.setPreferredSize(new Dimension(300, 45));
        pnlSearchWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)));
        pnlSearchWrapper.add(new JLabel(new MenuIcon("SEARCH")), BorderLayout.WEST);

        String placeholder = "Mã phiếu, hóa đơn...";
        txtSearch = new JTextField(placeholder);
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setBorder(null);
        txtSearch.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if (txtSearch.getText().equals(placeholder)) { txtSearch.setText(""); txtSearch.setForeground(Color.BLACK); } }
            public void focusLost(FocusEvent e) { if (txtSearch.getText().isEmpty()) { txtSearch.setText(placeholder); txtSearch.setForeground(Color.GRAY); } }
        });
        txtSearch.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { applyFilter(); } });
        pnlSearchWrapper.add(txtSearch, BorderLayout.CENTER);

        JButton btnReset = createActionBtn("Làm mới", "#6C757D");
        btnReset.setIcon(new MenuIcon("REFRESH"));
        btnReset.addActionListener(e -> {
            txtSearch.setText(placeholder);
            txtSearch.setForeground(Color.GRAY);
            loadDataToTable();
            xuLyStatus(statusBtns[0]);
        });
        
        JButton btnCreate = createActionBtn("Tạo phiếu", "#E11D48"); 
        
        btnCreate.addActionListener(e -> {
            Window p = SwingUtilities.getWindowAncestor(this);
            TaoPhieuDoiTra dialogTaoPhieu = new TaoPhieuDoiTra((Frame) p, model);
            dialogTaoPhieu.setVisible(true);
            
            // ĐOẠN NÀY ĐÃ ĐƯỢC MỞ KHÓA: 
            // Khi form TaoPhieuDoiTra tắt đi (dispose), code sẽ chạy tiếp xuống đây
            if (TaoPhieuDoiTra.isTaoThanhCong) {
                loadDataToTable(); // Tải lại bảng dữ liệu mới nhất
                
                // Hiển thị thông báo màu xanh lá cây cực đẹp ở giữa màn hình lớn
                showCustomNotification("THÀNH CÔNG", "Đã tạo phiếu '" + TaoPhieuDoiTra.maPhieuMoi + "' thành công!", "SUCCESS");
                
                // Reset lại cờ tín hiệu để dùng cho lần sau
                TaoPhieuDoiTra.isTaoThanhCong = false; 
            }
        });
        
        pnlActions.add(pnlSearchWrapper);
        pnlActions.add(btnReset);
        pnlActions.add(btnCreate);
        pnlHeader.add(pnlActions, BorderLayout.EAST);

        // ==================== 3. BỘ LỌC TRẠNG THÁI ====================
        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlFilters.setOpaque(false);
        pnlFilters.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        statusBtns = new JButton[]{
            createFilterBtn("Tất cả", true), 
            createFilterBtn("Chờ xử lý", false),
            createFilterBtn("Đang xử lý", false), 
            createFilterBtn("Hoàn thành", false), 
            createFilterBtn("Từ chối", false)
        };
        for (JButton b : statusBtns) { 
            pnlFilters.add(b); 
            b.addActionListener(e -> xuLyStatus(b)); 
        }

        JPanel pnlNorth = new JPanel();
        pnlNorth.setOpaque(false);
        pnlNorth.setLayout(new BoxLayout(pnlNorth, BoxLayout.Y_AXIS));
        pnlNorth.add(pnlTabs);
        pnlNorth.add(pnlHeader);
        pnlNorth.add(pnlFilters);
        this.add(pnlNorth, BorderLayout.NORTH);

        // ==================== 4. BẢNG DỮ LIỆU ĐỔI TRẢ ====================
        String[] cols = {"Mã phiếu", "Hóa đơn gốc", "Khách hàng", "Loại", "Lỗi", "Tiền hoàn", "Chênh lệch ĐH", "Trạng thái", "Ngày", "Xử lý"};
        model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setRowHeight(ROW_HEIGHT_NORMAL);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Color.decode("#DFE3E8")); 
        table.setSelectionBackground(Color.decode("#F4F6F8")); 

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(100, 45));
        header.setBackground(Color.decode("#D9EAF7")); 
        header.setForeground(Color.decode("#1E293B"));
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        table.setDefaultRenderer(Object.class, new DoiTraTableRenderer());
        
        table.getColumnModel().getColumn(0).setPreferredWidth(80);  
        table.getColumnModel().getColumn(1).setPreferredWidth(120); 
        table.getColumnModel().getColumn(2).setPreferredWidth(120); 
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  
        table.getColumnModel().getColumn(4).setPreferredWidth(230); 
        table.getColumnModel().getColumn(5).setPreferredWidth(100); 
        table.getColumnModel().getColumn(6).setPreferredWidth(110); 
        
        table.getColumnModel().getColumn(9).setPreferredWidth(230);
        table.getColumnModel().getColumn(9).setMinWidth(230);
        table.getColumnModel().getColumn(9).setMaxWidth(230);

        // ========================================================
        // THỦ THUẬT GẮN KHUNG CHI TIẾT TRỰC TIẾP VÀO TRONG BẢNG
        // ========================================================
        pnlDetail = createDetailPanel();
        pnlDetail.setVisible(false);
        
        table.setLayout(null); 
        table.add(pnlDetail);  

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                
                if (row >= 0) {
                    int modelRow = table.convertRowIndexToModel(row);
                    Object objStatus = model.getValueAt(modelRow, 7);
                    String status = objStatus != null ? objStatus.toString() : ""; 
                    
                    // 1. NẾU CLICK VÀO CỘT XỬ LÝ (CỘT 9) VÀ ĐANG Ở TRẠNG THÁI LƯU NHÁP/CHỜ XỬ LÝ -> MỞ CHỈNH SỬA
                 // NẾU CLICK VÀO CỘT NÚT XỬ LÝ (CỘT 9)
                    if (col == 9 && (status.equals("Lưu nháp") || status.equals("Chờ xử lý"))) {
                        Object objMa = model.getValueAt(modelRow, 0);
                        Object objHDGoc = model.getValueAt(modelRow, 1);
                        Object objLoai = model.getValueAt(modelRow, 3); // Bổ sung lấy loại Đổi / Trả
                        
                        String maPhieu = objMa != null ? objMa.toString() : "---";
                        String maHDGoc = objHDGoc != null ? objHDGoc.toString() : "---";
                        String loaiPhieu = objLoai != null ? objLoai.toString() : "Trả hàng";

                        // TRUYỀN THÊM LOẠI PHIẾU VÀO CONSTRUCTOR (Lỗi 3)
                        Window p = SwingUtilities.getWindowAncestor(ManHinhDoiTra.this);
                        TaoPhieuDoiTra dialogTaoPhieu = new TaoPhieuDoiTra((Frame) p, model, maPhieu, maHDGoc, loaiPhieu);
                        dialogTaoPhieu.setVisible(true);

                        if (TaoPhieuDoiTra.isTaoThanhCong) {
                            loadDataToTable();
                            showCustomNotification("THÀNH CÔNG", "Đã cập nhật phiếu thành công!", "SUCCESS");
                            TaoPhieuDoiTra.isTaoThanhCong = false;
                        }
                    }
                    // 2. NẾU CLICK VÀO CÁC CỘT CÒN LẠI -> SỔ DÒNG ACCORDION XEM CHI TIẾT
                    else {
                        Object objMa = model.getValueAt(modelRow, 0);
                        String maPhieu = objMa != null ? objMa.toString() : "---";
                        
                        if (maPhieu.equals(expandedMaPhieu)) {
                            expandedMaPhieu = "";
                            pnlDetail.setVisible(false);
                            updateRowHeights();
                        } else {
                            expandedMaPhieu = maPhieu;
                            Object objNgay = model.getValueAt(modelRow, 8);
                            String ngayTao = objNgay != null ? objNgay.toString() : "---";
                            
                            currentDetailHeight = showDetailPanel(maPhieu, ngayTao);
                            
                            pnlDetail.setVisible(true);
                            updateRowHeights();
                        }
                    }
                }
            }
        });

        table.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                if (pnlDetail.isVisible() && !expandedMaPhieu.isEmpty()) updateRowHeights();
            }
        });
        
        sorter.addRowSorterListener(e -> {
            if (pnlDetail.isVisible() && !expandedMaPhieu.isEmpty()) {
                SwingUtilities.invokeLater(() -> updateRowHeights());
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8")));
        sp.getViewport().setBackground(Color.WHITE); 
        
        // 1. Gắn UI tùy chỉnh cho thanh cuộn dọc
        sp.getVerticalScrollBar().setUI(new Utils.ModernScrollBarUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0)); // Chỉnh độ rộng thanh cuộn (10px)
        
        // 2. Gắn UI tùy chỉnh cho thanh cuộn ngang (nếu bảng có cuộn ngang)
        sp.getHorizontalScrollBar().setUI(new Utils.ModernScrollBarUI());
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10)); // Chỉnh độ cao thanh cuộn (10px)

        this.add(sp, BorderLayout.CENTER);
    }
    
    // ========================================================
    // HÀM XỬ LÝ HIỆU ỨNG ĐẨY DÒNG (ACCORDION)
    // ========================================================
    private void updateRowHeights() {
        for (int i = 0; i < table.getRowCount(); i++) {
            int modelRow = table.convertRowIndexToModel(i);
            Object objMa = model.getValueAt(modelRow, 0);
            String ma = objMa != null ? objMa.toString() : "";
            
            if (ma.equals(expandedMaPhieu)) {
                table.setRowHeight(i, ROW_HEIGHT_NORMAL + currentDetailHeight);
                Rectangle rect = table.getCellRect(i, 0, true);
                pnlDetail.setBounds(0, rect.y + ROW_HEIGHT_NORMAL, table.getWidth(), currentDetailHeight);
            } else {
                table.setRowHeight(i, ROW_HEIGHT_NORMAL);
            }
        }
        table.repaint();
    }
    private long layTienTrucTiepTuDB(String maPhieu, String loaiGhiChu) {
        long tong = 0;
        String sql = "SELECT ISNULL(SUM(ct.soLuong * dvl.gia), 0) FROM ChiTietHoaDon ct " +
                     "JOIN DonViDoLuong dvl ON ct.donViDoLuongId = dvl.id AND ct.sanPhamId = dvl.sanPhamId " +
                     "WHERE ct.hoaDonId = ?";
        if (loaiGhiChu != null) sql += " AND ct.ghiChu = '" + loaiGhiChu + "'";
        try (java.sql.Connection con = ConnectDB.ConnectDB.getInstance().getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maPhieu);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tong = (long) rs.getDouble(1);
            }
        } catch (Exception e) {}
        return tong;
    }
    private void loadDataToTable() {
        model.setRowCount(0);
        mapThoiGianTao.clear();
        DAO.DAO_HoaDon daoHD = new DAO.DAO_HoaDon();
        Utils.UserSession session = Utils.UserSession.getInstance();

        // 1. Lấy toàn bộ danh sách phiếu
        List<Object[]> ds = daoHD.layDanhSachPhieuDoiTra();
        if (ds == null) ds = new java.util.ArrayList<>();

        // 2. Lấy giờ bắt đầu ca (nếu là nhân viên)
        java.time.LocalDateTime shiftStart = null;
        if (!session.isAdmin() && session.getCaHienTai() != null) {
            shiftStart = session.getCaHienTai().getThoiGianBatDau();
        }

        for (Object[] dbRow : ds) {
            String maPhieu = dbRow[0] != null ? dbRow[0].toString() : "";
            String hoaDonGoc = dbRow[1] != null ? dbRow[1].toString() : "";
            String khachHang = dbRow[2] != null ? dbRow[2].toString() : "Khách lẻ";
            String loaiPhieu = dbRow[3] != null ? dbRow[3].toString() : "";
            String ghiChuDB = dbRow[4] != null ? dbRow[4].toString() : "";
            String ngayStr = dbRow[5] != null ? dbRow[5].toString() : "";

            // ========================================================
            // BỘ LỌC THEO CA LÀM VIỆC (ĐÃ BỔ SUNG ĐỌC NGÀY KHÔNG CÓ GIỜ)
            // ========================================================
            if (shiftStart != null && ngayStr != null && !ngayStr.trim().isEmpty()) {
                java.time.LocalDateTime hdTime = null;
                try {
                    java.time.format.DateTimeFormatter formatter1 = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    hdTime = java.time.LocalDateTime.parse(ngayStr.trim(), formatter1);
                } catch (Exception e1) {
                    try {
                        java.time.format.DateTimeFormatter formatter2 = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                        hdTime = java.time.LocalDateTime.parse(ngayStr.trim(), formatter2);
                    } catch (Exception e2) {
                        try {
                            java.time.format.DateTimeFormatter formatter3 = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.S]");
                            hdTime = java.time.LocalDateTime.parse(ngayStr.trim(), formatter3);
                        } catch (Exception e3) {
                            try {
                                // Thử format 4: CHỈ CÓ NGÀY (Khớp với dữ liệu thực tế DB của bạn)
                                java.time.format.DateTimeFormatter formatter4 = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                java.time.LocalDate dateOnly = java.time.LocalDate.parse(ngayStr.trim(), formatter4);
                                // Ép thời gian về cuối ngày (23:59:59) để phiếu trong ngày không bị bộ lọc ca (shiftStart) loại bỏ
                                hdTime = dateOnly.atTime(23, 59, 59);
                            } catch (Exception e4) {
                                System.err.println("Lỗi format ngày tại Đổi Trả! Chuỗi từ DB: [" + ngayStr + "]");
                            }
                        }
                    }
                }

                // Logic lọc:
                if (hdTime != null) {
                    if (hdTime.isBefore(shiftStart)) {
                        continue; // Bỏ qua hóa đơn vì tạo trước khi mở ca
                    }
                } else {
                    continue; // Lỗi format quá nặng, bỏ qua để an toàn
                }
            }
            // ========================================================

            // --- FIX TIỀN: Lấy trực tiếp từ chuỗi Ghi chú của Hóa đơn ---
            String trangThai = "Chờ xử lý";
            String loi = "";
            String tienHoanStr = "0đ";
            String chenhLechStr = "0đ";

            if (ghiChuDB.contains("|")) {
                String[] parts = ghiChuDB.split("\\|");
                if (parts.length >= 1) trangThai = parts[0].trim();
                if (parts.length >= 2) loi = parts[1].trim();
                if (parts.length >= 3) tienHoanStr = parts[2].trim(); 
                if (parts.length >= 4) chenhLechStr = parts[3].trim(); 
            }

            Object[] finalRow = new Object[]{
                maPhieu, hoaDonGoc, khachHang, loaiPhieu, loi, 
                tienHoanStr, 
                chenhLechStr, trangThai, ngayStr, ""
            };
            model.addRow(finalRow);
        }
    }

    private void chuyenManHinh(String tenManHinh) {
        SwingUtilities.invokeLater(() -> {
            Container parent = this.getParent();
            while (parent != null && !(parent.getLayout() instanceof CardLayout)) parent = parent.getParent();
            if (parent != null) {
                CardLayout cl = (CardLayout) parent.getLayout();
                cl.show(parent, tenManHinh);
                Container topLevel = parent.getParent();
                while (topLevel != null && !(topLevel instanceof MainDashboard)) topLevel = topLevel.getParent();
                if (topLevel instanceof MainDashboard) {
                    if (tenManHinh.equals("Bán hàng & Đổi trả") || tenManHinh.equals("DanhSachHD") || tenManHinh.equals("DoiTra")) {
                        ((MainDashboard) topLevel).chuyenSangTabBanHang(); 
                    }
                }
            }
        });
    }
    
    // ========================================================
    // RENDERER MỚI: NEO CHỮ LÊN TRÊN ĐỂ KHÔNG BỊ RỚT KHI DÒNG GIÃN RA
    // ========================================================
    class DoiTraTableRenderer extends DefaultTableCellRenderer {
        JPanel pnlWrapper = new JPanel(new BorderLayout());
        JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 12));
        
        // --- CHỈ DÙNG 1 NÚT CHỈNH SỬA ---
        JButton btnChinhSua = new JButton("Chỉnh sửa");
        
        public DoiTraTableRenderer() {
            pnlAction.setOpaque(false);
            btnChinhSua.setBackground(Color.decode("#F59E0B")); // Màu cam
            btnChinhSua.setForeground(Color.WHITE);
            btnChinhSua.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnChinhSua.setBorderPainted(false); btnChinhSua.setFocusPainted(false);
            btnChinhSua.setPreferredSize(new Dimension(100, 30));
            pnlAction.add(btnChinhSua);
        }

        public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasF, int r, int c) {
            if (c == 9) {
                Object objStatus = t.getValueAt(r, 7);
                String status = objStatus != null ? objStatus.toString() : ""; 
                pnlWrapper.removeAll();
                pnlWrapper.setBackground(isSel ? Color.decode("#F4F6F8") : Color.WHITE);
                
                // HIỆN NÚT CHỈNH SỬA KHI Ở TRẠNG THÁI LƯU NHÁP
                if (status.equals("Lưu nháp") || status.equals("Chờ xử lý")) {
                    pnlWrapper.add(pnlAction, BorderLayout.NORTH); 
                    pnlWrapper.setBorder(new EmptyBorder(0, 0, 0, 0)); 
                    return pnlWrapper; 
                } else {
                    return pnlWrapper; 
                }
            }
            
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasF, r, c);
            lbl.setHorizontalAlignment(CENTER);
            lbl.setVerticalAlignment(SwingConstants.TOP); // Neo chữ lên cao
            lbl.setBorder(new EmptyBorder(18, 5, 0, 5));  // Canh giữa cho 55px đầu
            
            lbl.setOpaque(true);
            lbl.setBackground(isSel ? Color.decode("#F4F6F8") : Color.WHITE);
            lbl.setForeground(Color.decode("#212B36"));

            if (c == 0) lbl.setForeground(Color.decode("#1967D2")); 
            if (c == 3 && v != null && !v.toString().equals("")) { 
                lbl.setBackground(v.toString().equals("Trả hàng") ? Color.decode("#FEE2E2") : Color.decode("#E0F2FE"));
                lbl.setForeground(v.toString().equals("Trả hàng") ? Color.decode("#EF4444") : Color.decode("#0284C7"));
            }
            if (c == 4 && v != null) { 
                String loi = v.toString();
                if (loi.contains("100%") || loi.contains("NSX") || loi.contains("đổi ý")) {
                    lbl.setBackground(Color.decode("#FFEDD5")); lbl.setForeground(Color.decode("#D97706")); 
                } else {
                    lbl.setBackground(Color.decode("#F3F4F6")); lbl.setForeground(Color.decode("#4B5563")); 
                }
            }
            if (c == 5 && v != null && !v.toString().equals("---")) { 
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); lbl.setForeground(Color.decode("#DC2626"));
            }
            if (c == 6 && v != null && !v.toString().equals("---")) { 
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); lbl.setForeground(Color.decode("#DC2626"));
            }
            if (c == 7) { 
                String statusStr = (v != null) ? v.toString() : "";
                lbl.setText(statusStr); // Chỉ hiển thị trạng thái gốc từ Database/Model
                
                if (statusStr.equals("Hoàn thành")) { 
                    lbl.setBackground(Color.decode("#DCFCE7")); 
                    lbl.setForeground(Color.decode("#10B981")); 
                }
                else if (statusStr.equals("Chờ xử lý") || statusStr.equals("Lưu nháp")) { 
                    lbl.setBackground(Color.decode("#FEF3C7")); 
                    lbl.setForeground(Color.decode("#D97706")); 
                }
              
                else { 
                    lbl.setBackground(Color.decode("#F3F4F6")); 
                    lbl.setForeground(Color.decode("#6B7280")); 
                }
            }
            return lbl;
        }
    }

    private JPanel createDetailPanel() {
        JPanel pnl = new JPanel(new BorderLayout(20, 20));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(0, 30, 15, 30), 
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1),
                new EmptyBorder(15, 20, 15, 20)
            )
        ));

        // HEADER
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#F1F3F5")));
        
        JPanel pnlLeftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlLeftTitle.setBackground(Color.WHITE);
        lblDetailTitle = new JLabel("Chi tiết phiếu ---");
        lblDetailTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDetailDate = new JLabel("Ngày tạo: --/--/----");
        lblDetailDate.setForeground(Color.GRAY);
        
        // --- THÊM KHỐI CODE SAU ĐÂY VÀO ---
        lblDetailTimer = new JLabel("");
        lblDetailTimer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDetailTimer.setForeground(Color.decode("#E11D48")); // Màu đỏ nổi bật
        lblDetailTimer.setBorder(new EmptyBorder(0, 15, 0, 0));
        
        pnlLeftTitle.add(lblDetailTitle);
        pnlLeftTitle.add(lblDetailDate);
        pnlLeftTitle.add(lblDetailTimer); // Add thêm label đếm ngược vào giao diện

        lblDetailEmp = new JLabel("Nhân viên: Hệ thống"); 
        lblDetailEmp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDetailEmp.setForeground(Color.GRAY);

        JButton btnInPhieu = new JButton("Xem / In Phiếu");
        btnInPhieu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnInPhieu.setForeground(Color.decode("#1967D2"));
        btnInPhieu.setBackground(Color.WHITE);
        btnInPhieu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#1967D2"), 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btnInPhieu.setFocusPainted(false);
        btnInPhieu.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnInPhieu.addActionListener(e -> {
            if (expandedMaPhieu != null && !expandedMaPhieu.isEmpty()) {
                int targetRow = -1;
                for (int i = 0; i < model.getRowCount(); i++) {
                    if (model.getValueAt(i, 0).toString().equals(expandedMaPhieu)) {
                        targetRow = i; break;
                    }
                }
                
                if (targetRow != -1) {
                    String loaiPhieu = model.getValueAt(targetRow, 3).toString(); 
                    String hdGoc = model.getValueAt(targetRow, 1).toString();
                    String khachHang = model.getValueAt(targetRow, 2).toString();
                    String lyDo = model.getValueAt(targetRow, 4).toString();
                    String tienHoan = model.getValueAt(targetRow, 5).toString();
                    String chenhLech = model.getValueAt(targetRow, 6).toString();
                    String trangThai = model.getValueAt(targetRow, 7).toString();
                    String ngayTao = model.getValueAt(targetRow, 8).toString();
                    String nhanVien = lblDetailEmp.getText().replace("Nhân viên: ", "").trim();

                    Window parentWindow = SwingUtilities.getWindowAncestor(pnl);
                    ChiTietPhieuDoiTra dialog = new ChiTietPhieuDoiTra(
                        (Frame) parentWindow, expandedMaPhieu, loaiPhieu, 
                        trangThai, ngayTao, hdGoc, khachHang, lyDo, nhanVien, 
                        tienHoan, chenhLech
                    );
                    dialog.setVisible(true);
                }
            }
        });

        JPanel pnlRightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlRightHeader.setBackground(Color.WHITE);
        pnlRightHeader.add(lblDetailEmp);
        pnlRightHeader.add(btnInPhieu);

        pnlHeader.add(pnlLeftTitle, BorderLayout.WEST);
        pnlHeader.add(pnlRightHeader, BorderLayout.EAST);

        // BODY
        JPanel pnlBody = new JPanel(new BorderLayout(30, 0));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JPanel pnlSP = new JPanel(new BorderLayout(0, 10));
        pnlSP.setBackground(Color.WHITE);
        JLabel lblDSSP = new JLabel("Danh sách sản phẩm:");
        lblDSSP.setForeground(Color.GRAY);
        pnlSP.add(lblDSSP, BorderLayout.NORTH);
        
        pnlDetailProducts = new JPanel();
        pnlDetailProducts.setLayout(new BoxLayout(pnlDetailProducts, BoxLayout.Y_AXIS));
        pnlDetailProducts.setBackground(Color.WHITE);
        pnlSP.add(pnlDetailProducts, BorderLayout.CENTER);

        // --- CỘT PHẢI ĐỘNG CẬP NHẬT GIAO DIỆN ---
        pnlRightWrapper = new JPanel();
        pnlRightWrapper.setLayout(new BoxLayout(pnlRightWrapper, BoxLayout.Y_AXIS));
        pnlRightWrapper.setBackground(Color.WHITE);
        pnlRightWrapper.setPreferredSize(new Dimension(320, 0));

        pnlBody.add(pnlSP, BorderLayout.CENTER);
        pnlBody.add(pnlRightWrapper, BorderLayout.EAST);

        pnl.add(pnlHeader, BorderLayout.NORTH);
        pnl.add(pnlBody, BorderLayout.CENTER);
        
        return pnl;
    }
 // ========================================================
    // HÀM TIỆN ÍCH TẠO CÁC KHỐI THÔNG TIN BÊN PHẢI (UI MỚI)
    // ========================================================
    private JPanel createRightBox(String title, String content, String colorText, String colorBg) {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setBackground(Color.decode(colorBg));
        pnl.setBorder(new EmptyBorder(12, 15, 12, 15));
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Khóa chiều cao tối đa để Box không bị giãn dài khi màn hình to
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); 
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(Color.decode("#6B7280"));
        
        JLabel lblContent = new JLabel(content);
        lblContent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblContent.setForeground(Color.decode(colorText));
        
        pnl.add(lblTitle, BorderLayout.NORTH);
        pnl.add(lblContent, BorderLayout.CENTER);
        return pnl;
    }
    private int showDetailPanel(String maPhieu, String ngayTao) {
        lblDetailTitle.setText("Chi tiết phiếu " + maPhieu);
        lblDetailDate.setText("Ngày tạo: " + ngayTao);
        lblDetailTimer.setText("");
        String tenNV = "Hệ thống";
        try { if (Utils.UserSession.getInstance() != null) tenNV = Utils.UserSession.getInstance().getTenHienThi(); } catch (Exception e) {}
        lblDetailEmp.setText("Nhân viên: " + tenNV);
        
        pnlDetailProducts.removeAll();
        pnlRightWrapper.removeAll(); 
        int dynamicHeight = 160; 

        int targetRow = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(maPhieu)) { targetRow = i; break; }
        }
        
        String loaiPhieu = targetRow != -1 ? model.getValueAt(targetRow, 3).toString() : "";
        String hoaDonGoc = targetRow != -1 ? model.getValueAt(targetRow, 1).toString() : "";
        String chenhLechStr = targetRow != -1 ? (model.getValueAt(targetRow, 6) != null ? model.getValueAt(targetRow, 6).toString() : "0đ") : "0đ";
        
        boolean isThoiLai = chenhLechStr.contains("Hoàn");
        long chenhLechBang = 0;
        try { chenhLechBang = Long.parseLong(chenhLechStr.replaceAll("[^0-9]", "")); } catch(Exception e){}

        BUS_TraHang busTra = new BUS_TraHang();
        DAO.DAO_HoaDon daoHD = new DAO.DAO_HoaDon();
        long tongTienTra = 0;

        // 1. ĐỔ DỮ LIỆU SẢN PHẨM TRẢ
        List<Object[]> dsTra = busTra.layChiTietPhieu(maPhieu); 
        if (dsTra != null && !dsTra.isEmpty()) {
            for (Object[] sp : dsTra) {
                String tenSP = sp[0] != null ? sp[0].toString() : "Sản phẩm";
                String donVi = sp[2] != null && !sp[2].toString().isEmpty() ? sp[2].toString() : "Hộp"; 
                int sl = 1; try { sl = Integer.parseInt(sp[1].toString().replaceAll("[^0-9]", "")); } catch(Exception e){}
                
                long gia = 0;
                try { gia = Long.parseLong(sp[3].toString().replaceAll(",00$|\\.00$|,0$|\\.0$", "").replaceAll("[^0-9]", "")); } catch(Exception e){}
                if (gia <= 1) {
                    try {
                        Object[] info = daoHD.layThongTinGiaTuHDGoc(hoaDonGoc, tenSP);
                        // ĐÃ FIX: Ép kiểu chuẩn Double
                        if (info != null && info[1] != null) gia = (long) Double.parseDouble(info[1].toString());
                    } catch(Exception e){}
                }
                tongTienTra += gia * sl;
                
                String giaBan = String.format("%,d", gia).replace(',', '.') + "đ";
                JPanel row = new JPanel(new BorderLayout()); 
                row.setBackground(Color.WHITE); row.setBorder(new EmptyBorder(8, 0, 8, 0));
                
                JLabel lblName = new JLabel(loaiPhieu.equalsIgnoreCase("Trả hàng") ? tenSP : "[TRẢ] " + tenSP);
                lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lblName.setForeground(loaiPhieu.equalsIgnoreCase("Trả hàng") ? Color.decode("#212B36") : Color.decode("#DC2626"));
                
                JLabel lblPrice = new JLabel(sl + " " + donVi + " × " + giaBan);
                lblPrice.setFont(new Font("Segoe UI", Font.PLAIN, 13)); lblPrice.setForeground(Color.decode("#4B5563"));

                row.add(lblName, BorderLayout.WEST); row.add(lblPrice, BorderLayout.EAST);
                pnlDetailProducts.add(row);
                dynamicHeight += 40; 
            }
        }

        // 2. ĐỔ DỮ LIỆU SẢN PHẨM ĐỔI
        if (loaiPhieu.equalsIgnoreCase("Đổi hàng")) {
            List<Object[]> dsDoi = busTra.layDanhSachSanPhamDoi(maPhieu);
            if (dsDoi != null && !dsDoi.isEmpty()) {
                for (Object[] sp : dsDoi) {
                    String tenSP = sp[0] != null ? sp[0].toString() : "Sản phẩm";
                    String donVi = sp[2] != null && !sp[2].toString().isEmpty() ? sp[2].toString() : "Hộp"; 
                    int sl = 1; try { sl = Integer.parseInt(sp[1].toString().replaceAll("[^0-9]", "")); } catch(Exception e){}
                    
                    long gia = 0;
                    try { gia = Long.parseLong(sp[3].toString().replaceAll(",00$|\\.00$|,0$|\\.0$", "").replaceAll("[^0-9]", "")); } catch(Exception e){}
                    if (gia <= 1) {
                        try {
                            Object[] info = daoHD.layThongTinGiaTuHDGoc(maPhieu, tenSP);
                            // ĐÃ FIX: Ép kiểu chuẩn Double
                            if (info != null && info[1] != null) gia = (long) Double.parseDouble(info[1].toString());
                        } catch(Exception e){}
                    }
                    
                    if (gia <= 1 && sl > 0) {
                        if (isThoiLai) gia = (tongTienTra - chenhLechBang) / sl; 
                        else gia = (tongTienTra + chenhLechBang) / sl;
                    }

                    String giaBan = String.format("%,d", gia).replace(',', '.') + "đ";
                    JPanel row = new JPanel(new BorderLayout()); 
                    row.setBackground(Color.WHITE); row.setBorder(new EmptyBorder(8, 0, 8, 0));
                    
                    JLabel lblName = new JLabel("[ĐỔI LẤY] " + tenSP);
                    lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    lblName.setForeground(Color.decode("#059669"));
                    
                    JLabel lblPrice = new JLabel(sl + " " + donVi + " × " + giaBan);
                    lblPrice.setFont(new Font("Segoe UI", Font.PLAIN, 13)); lblPrice.setForeground(Color.decode("#4B5563"));

                    row.add(lblName, BorderLayout.WEST); row.add(lblPrice, BorderLayout.EAST);
                    pnlDetailProducts.add(row);
                    dynamicHeight += 40; 
                }
            }
        }

        hienThiThongTinPhuBênPhai(maPhieu);
        this.revalidate();
        this.repaint();
        // ĐÃ FIX: Tăng chiều cao tối thiểu lên 280 để 3 box bên phải không bị cắt chữ
        return Math.max(dynamicHeight, 330); 
    }

    private void hienThiThongTinPhuBênPhai(String maPhieu, BUS_TraHang busTra) {
        int targetRow = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(maPhieu)) { targetRow = i; break; }
        }
        if (targetRow == -1) return;

        String loaiPhieu = model.getValueAt(targetRow, 3).toString(); 
        String ghiChuLoi = model.getValueAt(targetRow, 4).toString(); 
        String phuongThuc = "Tiền mặt";
        try {
            Entity.HoaDon hd = busHD.getHoaDonTheoMa(maPhieu);
            if (hd != null && hd.getPhuongThucThanhToan() != null) {
                phuongThuc = hd.getPhuongThucThanhToan().toString().contains("CHUYEN_KHOAN") ? "Chuyển khoản" : "Tiền mặt";
            }
        } catch (Exception e) {}

        pnlRightWrapper.removeAll();
        DAO.DAO_HoaDon daoHD = new DAO.DAO_HoaDon();

        // TỰ TÍNH LẠI CHÊNH LỆCH ĐỂ CHUẨN XÁC TUYỆT ĐỐI (Vì giá DB giờ đã trả về đúng số)
        long tongTienTra = 0;
        List<Object[]> dsTra = busTra.layChiTietPhieu(maPhieu);
        if (dsTra != null) {
            for (Object[] sp : dsTra) {
                long gia = 0; int sl = 1;
                try { sl = Integer.parseInt(sp[1].toString().replaceAll("[^0-9]", "")); } catch(Exception e){}
                try { gia = Long.parseLong(sp[3].toString().replaceAll(",00$|\\.00$|,0$|\\.0$", "").replaceAll("[^0-9]", "")); } catch(Exception e){}
                if (gia <= 1) {
                    try {
                        Object[] info = daoHD.layThongTinGiaTuHDGoc(model.getValueAt(targetRow, 1).toString(), sp[0].toString());
                        if (info != null && info[1] != null) gia = Long.parseLong(info[1].toString().replaceAll("[^0-9]", ""));
                    } catch(Exception e){}
                }
                tongTienTra += gia * sl;
            }
        }
        
        long tongTienDoi = 0;
        List<Object[]> dsDoi = busTra.layDanhSachSanPhamDoi(maPhieu);
        if (dsDoi != null) {
            for (Object[] sp : dsDoi) {
                long gia = 0; int sl = 1;
                try { sl = Integer.parseInt(sp[1].toString().replaceAll("[^0-9]", "")); } catch(Exception e){}
                try { gia = Long.parseLong(sp[3].toString().replaceAll(",00$|\\.00$|,0$|\\.0$", "").replaceAll("[^0-9]", "")); } catch(Exception e){}
                if (gia <= 1) {
                    try {
                        Object[] info = daoHD.layThongTinGiaTuHDGoc(maPhieu, sp[0].toString());
                        if (info != null && info[1] != null) gia = Long.parseLong(info[1].toString().replaceAll("[^0-9]", ""));
                    } catch(Exception e){}
                }
                tongTienDoi += gia * sl;
            }
        }

        // TÍNH TOÁN THEO CÔNG THỨC CHUẨN CỦA BUS_DoiHang (Mới - Cũ)
        long chenhLechThucTe = tongTienDoi - tongTienTra;

        if (loaiPhieu.equalsIgnoreCase("Trả hàng")) {
            pnlRightWrapper.add(createRightBox("TỔNG TIỀN HOÀN", String.format("%,dđ", tongTienTra).replace(',', '.'), "#DC2626", "#FEF2F2"));
        } else {
            StringBuilder sbDoi = new StringBuilder();
            if (dsDoi != null && !dsDoi.isEmpty()) {
                for (Object[] spDoi : dsDoi) {
                    sbDoi.append("- ").append(spDoi[0].toString()).append("<br>");
                }
            } else {
                sbDoi.append("Không có<br>");
            }
            
            // NẾU CHÊNH LỆCH < 0 TỨC LÀ TIỀN MỚI RẺ HƠN TIỀN CŨ -> THỐI LẠI KHÁCH
            boolean isKhachBu = (chenhLechThucTe > 0);
            String mauChenhLech = isKhachBu ? "#D97706" : "#DC2626"; 
            String textChenhLech = isKhachBu ? "Khách bù: " : "Thối lại khách: ";
            
            sbDoi.append("<span style='color: ").append(mauChenhLech).append("; font-size: 13px; font-weight: normal;'>")
                 .append(textChenhLech).append(String.format("%,dđ", Math.abs(chenhLechThucTe)).replace(',', '.')).append("</span>");
            
            JPanel pnlDoi = createRightBox("SẢN PHẨM ĐỔI LẤY", "<html><div style='line-height: 1.4;'>" + sbDoi.toString() + "</div></html>", "#059669", "#F0FDF4");
            pnlRightWrapper.add(pnlDoi);
        }

        pnlRightWrapper.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlRightWrapper.add(createRightBox("LÝ DO / LỖI", ghiChuLoi, "#4B5563", "#F3F4F6"));
        pnlRightWrapper.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlRightWrapper.add(createRightBox("PHƯƠNG THỨC", phuongThuc, "#1D4ED8", "#EFF6FF"));

        pnlRightWrapper.revalidate();
        pnlRightWrapper.repaint();
    }

    private void hienThiThongTinPhuBênPhai(String maPhieu) {
        int targetRow = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(maPhieu)) {
                targetRow = i;
                break;
            }
        }
        if (targetRow == -1) return;

        String loaiPhieu = model.getValueAt(targetRow, 3).toString(); 
        String ghiChuLoi = model.getValueAt(targetRow, 4).toString(); 
        String tienHoan = model.getValueAt(targetRow, 5) != null ? model.getValueAt(targetRow, 5).toString() : "0đ";
        String chenhLech = model.getValueAt(targetRow, 6) != null ? model.getValueAt(targetRow, 6).toString() : "0đ"; 

        String phuongThuc = "Tiền mặt";
        try {
            Entity.HoaDon hd = busHD.getHoaDonTheoMa(maPhieu);
            if (hd != null && hd.getPhuongThucThanhToan() != null) {
                phuongThuc = hd.getPhuongThucThanhToan().toString().contains("CHUYEN_KHOAN") ? "Chuyển khoản" : "Tiền mặt";
            }
        } catch (Exception e) {}

        pnlRightWrapper.removeAll();

        if (loaiPhieu.equalsIgnoreCase("Trả hàng")) {
            pnlRightWrapper.add(createRightBox("TỔNG TIỀN HOÀN", tienHoan, "#DC2626", "#FEF2F2"));
        } else {
            // Lấy thẳng kết quả cực chuẩn từ bảng chính truyền sang
            boolean isThoiLai = chenhLech.contains("Hoàn");
            String color = isThoiLai ? "#DC2626" : "#D97706";
            String title = isThoiLai ? "TIỀN THỐI LẠI" : "KHÁCH BÙ THÊM";
            
            // Lọc lấy con số
            String giaTri = chenhLech.replace("Hoàn:", "").replace("Bù:", "").trim();
            pnlRightWrapper.add(createRightBox(title, giaTri, color, "#FFFBEB"));
        }

        pnlRightWrapper.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlRightWrapper.add(createRightBox("LÝ DO / LỖI", ghiChuLoi, "#4B5563", "#F3F4F6"));
        pnlRightWrapper.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlRightWrapper.add(createRightBox("PHƯƠNG THỨC HOÀN TIỀN", phuongThuc, "#1D4ED8", "#EFF6FF"));

        pnlRightWrapper.revalidate();
        pnlRightWrapper.repaint();
    }
    private void xuLyStatus(JButton b) {
        for (JButton btn : statusBtns) setBtnNormal(btn);
        setBtnActive(b);
        filterStatus = b.getText().trim();
        applyFilter();
    }

    private void applyFilter() {
    	if (table == null || sorter == null) return;
        expandedMaPhieu = ""; 
        if(pnlDetail != null) pnlDetail.setVisible(false);
        updateRowHeights();
        
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        
        // 1. Lọc theo trạng thái nút bấm
        if (!filterStatus.equals("Tất cả")) {
            filters.add(RowFilter.regexFilter("^" + filterStatus + "$", 7));
        }
        
        String search = txtSearch.getText().trim();
        
        // 2. --- ĐÃ FIX: Chặn triệt để mọi câu chữ mờ (Placeholder) ---
        // Chỉ cần chuỗi có chứa chữ "Mã phiếu" là hệ thống tự hiểu đó là chữ mờ và bỏ qua
        if (!search.isEmpty() && !search.contains("Mã phiếu")) {
            try {
                filters.add(RowFilter.regexFilter("(?i)" + search));
            } catch (java.util.regex.PatternSyntaxException e) {
                // Chống lỗi văng (crash) phần mềm nếu người dùng lỡ gõ ký tự regex đặc biệt
            }
        }
        
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private JButton createActionBtn(String txt, String hex) {
        JButton btn = new JButton(txt);
        btn.setPreferredSize(new Dimension(130, 45));
        btn.setBackground(Color.decode(hex));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        return btn;
    }

    private JButton createFilterBtn(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (isActive) setBtnActive(btn); else setBtnNormal(btn);
        return btn;
    }

    private void setBtnActive(JButton btn) {
        btn.setBackground(Color.decode("#1967D2")); btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#1967D2"), 1), BorderFactory.createEmptyBorder(8, 15, 8, 15)));
    }

    private void setBtnNormal(JButton btn) {
        btn.setBackground(Color.WHITE); btn.setForeground(Color.decode("#374151"));
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1), BorderFactory.createEmptyBorder(8, 15, 8, 15)));
    }

    private void showCustomNotification(String titleText, String message, String type) {
        Window window = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(window != null ? (Frame) window : null, true); 
        dialog.setUndecorated(true); dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createLineBorder(Color.decode("#1E3A8A"), 2));
        pnlMain.setBackground(Color.WHITE);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.decode("#1E3A8A"));
        pnlHeader.setPreferredSize(new Dimension(0, 40));
        JLabel lblTitle = new JLabel(titleText.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15)); lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.CENTER);

        JPanel pnlBody = new JPanel(new BorderLayout(15, 0));
        pnlBody.setBackground(Color.WHITE); pnlBody.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel pnlIcon = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color mainColor = type.equals("ERROR") ? Color.decode("#EF4444") : (type.equals("SUCCESS") ? Color.decode("#10B981") : Color.decode("#F59E0B"));
                Color bgColor = type.equals("ERROR") ? Color.decode("#FEE2E2") : (type.equals("SUCCESS") ? Color.decode("#D1FAE5") : Color.decode("#FEF3C7"));
                g2.setColor(bgColor); g2.fillOval(0, 0, 50, 50);
                g2.setColor(mainColor); g2.setStroke(new java.awt.BasicStroke(3f)); g2.drawOval(0, 0, 50, 50);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26)); FontMetrics fm = g2.getFontMetrics();
                String symbol = type.equals("ERROR") ? "X" : (type.equals("SUCCESS") ? "V" : "!");
                int x = (50 - fm.stringWidth(symbol)) / 2; int y = ((50 - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(symbol, x, y); g2.dispose();
            }
        };
        pnlIcon.setPreferredSize(new Dimension(50, 50));
        JPanel iconWrapper = new JPanel(new BorderLayout()); iconWrapper.setOpaque(false); iconWrapper.add(pnlIcon, BorderLayout.NORTH);

        String htmlContent = "<html><div style='width: 320px; line-height: 1.4; word-wrap: break-word;'>" + message.replace("\n", "<br>") + "</div></html>";
        JLabel msg = new JLabel(htmlContent); msg.setFont(new Font("Segoe UI", Font.PLAIN, 14)); msg.setForeground(Color.decode("#333333")); msg.setVerticalAlignment(SwingConstants.TOP); 

        pnlBody.add(iconWrapper, BorderLayout.WEST); pnlBody.add(msg, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlFooter.setBackground(Color.WHITE);
        JButton btnClose = new JButton("Đóng"); btnClose.setPreferredSize(new Dimension(100, 35)); btnClose.setBackground(Color.decode("#1E3A8A"));
        btnClose.setForeground(Color.WHITE); btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14)); btnClose.setFocusPainted(false); btnClose.setBorderPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnClose.addActionListener(e -> dialog.dispose());
        pnlFooter.add(btnClose);

        pnlMain.add(pnlHeader, BorderLayout.NORTH); pnlMain.add(pnlBody, BorderLayout.CENTER); pnlMain.add(pnlFooter, BorderLayout.SOUTH);

        dialog.add(pnlMain); dialog.pack(); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
    }

    private boolean showCustomConfirmDialog(String titleText, String message) {
        final boolean[] result = {false};
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow != null ? (Frame) parentWindow : null, true);
        dialog.setUndecorated(true); dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createLineBorder(Color.decode("#1E3A8A"), 2)); pnlMain.setBackground(Color.WHITE);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.decode("#1E3A8A")); pnlHeader.setPreferredSize(new Dimension(0, 45));
        JLabel lblTitle = new JLabel(titleText.toUpperCase(), SwingConstants.CENTER); lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15)); lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.CENTER);

        JPanel pnlBody = new JPanel(new BorderLayout());
        pnlBody.setBackground(Color.WHITE); pnlBody.setBorder(new EmptyBorder(20, 20, 20, 20)); 
        JLabel msg = new JLabel("<html><div style='text-align: center;'>" + message.replace("\n", "<br>") + "</div></html>", SwingConstants.CENTER);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 15)); msg.setForeground(Color.decode("#333333")); pnlBody.add(msg, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        pnlFooter.setBackground(Color.WHITE);
        JButton btnYes = new JButton("Đồng ý"); btnYes.setPreferredSize(new Dimension(110, 38)); btnYes.setBackground(Color.decode("#EF4444")); 
        btnYes.setForeground(Color.WHITE); btnYes.setFont(new Font("Segoe UI", Font.BOLD, 14)); btnYes.setFocusPainted(false); btnYes.setBorderPainted(false);
        btnYes.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnYes.addActionListener(e -> { result[0] = true; dialog.dispose(); });

        JButton btnNo = new JButton("Hủy"); btnNo.setPreferredSize(new Dimension(110, 38)); btnNo.setBackground(Color.decode("#1E3A8A"));
        btnNo.setForeground(Color.WHITE); btnNo.setFont(new Font("Segoe UI", Font.BOLD, 14)); btnNo.setFocusPainted(false); btnNo.setBorderPainted(false);
        btnNo.setCursor(new Cursor(Cursor.HAND_CURSOR)); btnNo.addActionListener(e -> dialog.dispose());

        pnlFooter.add(btnYes); pnlFooter.add(btnNo);

        pnlMain.add(pnlHeader, BorderLayout.NORTH); pnlMain.add(pnlBody, BorderLayout.CENTER); pnlMain.add(pnlFooter, BorderLayout.SOUTH);

        dialog.add(pnlMain); dialog.setSize(440, 260); dialog.setLocationRelativeTo(this); dialog.setVisible(true);
        return result[0];
    }
    private void khoiDongBoDemNguoc() {
        if (autoCancelTimer != null) autoCancelTimer.stop();
        
        autoCancelTimer = new Timer(1000, e -> {
            for (int i = 0; i < model.getRowCount(); i++) {
                String trangThai = model.getValueAt(i, 7) != null ? model.getValueAt(i, 7).toString() : "";
                
                // Chỉ xử lý nếu đúng là "Chờ xử lý"
                if (trangThai.trim().equals("Chờ xử lý")) {
                    String maPhieu = model.getValueAt(i, 0).toString();
                    java.time.LocalDateTime thoiGianTao = mapThoiGianTao.get(maPhieu);
                    
                    if (thoiGianTao == null) {
                        thoiGianTao = layThoiGianTaoTuDB(maPhieu);
                        if (thoiGianTao != null) {
                            mapThoiGianTao.put(maPhieu, thoiGianTao);
                        } else {
                            // Nếu DB trả về null (Không tìm thấy giờ), gán giờ mặc định quá hạn để hủy luôn
                            mapThoiGianTao.put(maPhieu, java.time.LocalDateTime.now().minusMinutes(10));
                            continue; // Bỏ qua giây này, chờ giây sau xử lý
                        }
                    }
                    
                    long giayDaQua = java.time.Duration.between(thoiGianTao, java.time.LocalDateTime.now()).getSeconds();
                    long giayConLai = 600 - giayDaQua; 
                    
                    if (giayConLai <= 0) {
                        // 1. GỌI DB HỦY PHIẾU
                        huyPhieuTuDong(maPhieu);
                        
                        // 2. [QUAN TRỌNG] ĐỔI TRẠNG THÁI NGAY TRÊN MODEL ĐỂ DỪNG VÒNG LẶP KẸT LAG
                        model.setValueAt("Từ chối", i, 7); 
                        
                        // 3. THÔNG BÁO HẾT GIỜ (Không đóng sập Detail Panel của người dùng)
                        if (pnlDetail.isVisible() && maPhieu.equals(expandedMaPhieu)) {
                            lblDetailTimer.setText("(Đã hủy tự động do quá hạn)");
                            lblDetailTimer.setForeground(Color.decode("#EF4444")); // Màu đỏ
                        }
                        
                    } else {
                        // 4. NẾU CHƯA HẾT GIỜ VÀ PANEL ĐANG MỞ -> HIỂN THỊ THỜI GIAN
                        if (pnlDetail.isVisible() && maPhieu.equals(expandedMaPhieu)) {
                            long m = giayConLai / 60;
                            long s = giayConLai % 60;
                            lblDetailTimer.setText(String.format("(Tự hủy sau: %02d:%02d)", m, s));
                            lblDetailTimer.setForeground(Color.decode("#E11D48")); 
                        }
                    }
                }
            }
        });
        autoCancelTimer.start();
    }

    // 2. Lấy thời gian gốc tạo hóa đơn
    private java.time.LocalDateTime layThoiGianTaoTuDB(String maPhieu) {
        try (java.sql.Connection con = ConnectDB.ConnectDB.getInstance().getConnection()) {
            String sql = "SELECT ngayLapHD FROM HoaDon WHERE id = ?";
            try (java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, maPhieu);
                try (java.sql.ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        java.sql.Timestamp ts = rs.getTimestamp("ngayLapHD");
                        if (ts != null) return ts.toLocalDateTime();
                    }
                }
            }
        } catch (Exception ex) {}
        return null;
    }

    // 3. ĐÃ FIX LỖI SQL: Hủy phiếu tự động (Không dùng cột trangThai)
    private void huyPhieuTuDong(String maPhieu) {
        try (java.sql.Connection con = ConnectDB.ConnectDB.getInstance().getConnection()) {
            // Cập nhật vào cột ghiChu thay vì cột trangThai không tồn tại
            String sql = "UPDATE HoaDon SET ghiChu = CONCAT(ghiChu, N' | Đã hủy (Tự động quá hạn 10p)') WHERE id = ?";
            try (java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, maPhieu);
                pst.executeUpdate();
                mapThoiGianTao.remove(maPhieu); 
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Tìm và mở phiếu đổi/trả theo mã — được gọi từ Timer sau khi điều hướng
     * từ Live Notification. Tương tự moLaiHoaDonNhap() của ManHinhBanHang.
     *
     * Logic:
     *  1. Set ô tìm kiếm về maPhieu → applyFilter() (reset accordion)
     *  2. Duyệt bảng tìm dòng khớp → mở accordion chi tiết
     *  3. Cuộn bảng đến dòng vừa mở
     */
    public void moLaiPhieuDoiTra(String maPhieu) {
        SwingUtilities.invokeLater(() -> {
            // 1. Đặt ô tìm kiếm và reset bộ lọc trạng thái về "Tất cả"
            if (txtSearch != null) {
                txtSearch.setText(maPhieu);
                txtSearch.setForeground(Color.BLACK);
            }
            if (statusBtns != null) {
                for (JButton b : statusBtns) setBtnNormal(b);
                setBtnActive(statusBtns[0]);
                filterStatus = "Tất cả";
            }
            // applyFilter() sẽ reset expandedMaPhieu = "" và ẩn pnlDetail
            applyFilter();

            // 2. Tìm dòng khớp mã phiếu trong kết quả lọc và mở accordion
            boolean found = false;
            for (int i = 0; i < table.getRowCount(); i++) {
                int modelRow = table.convertRowIndexToModel(i);
                Object objMa = model.getValueAt(modelRow, 0);
                if (objMa != null && maPhieu.equals(objMa.toString())) {
                    expandedMaPhieu = maPhieu;
                    Object objNgay = model.getValueAt(modelRow, 8);
                    String ngayTao = objNgay != null ? objNgay.toString() : "---";
                    currentDetailHeight = showDetailPanel(maPhieu, ngayTao);
                    pnlDetail.setVisible(true);
                    updateRowHeights();
                    // 3. Cuộn bảng đến dòng vừa mở
                    table.scrollRectToVisible(table.getCellRect(i, 0, true));
                    found = true;
                    break;
                }
            }

            if (!found) {
                Utils.ThongBao.show(ManHinhDoiTra.this,
                        "KHÔNG TÌM THẤY",
                        "Phiếu \"" + maPhieu + "\" không nằm trong danh sách hiện tại!\n"
                                + "Có thể phiếu thuộc ca khác hoặc đã bị lọc.",
                        "WARNING");
            }
        });
    }
}