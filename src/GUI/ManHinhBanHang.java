package GUI;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Cursor;
import java.awt.Container;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

import Utils.MenuIcon;
import Utils.UserSession;
import BUS.BUS_HoaDon;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ManHinhBanHang extends JPanel {
	private JTextField txtSearch;
    public static String pendingDraftIdToOpen = null; 
    public static String pendingPhoneToLink = null;
    public static String pendingNameToLink = null; // BẠN THÊM ĐÚNG 1 DÒNG NÀY VÀO ĐÂY LÀ XONG
    private BUS_HoaDon busHoaDon;
    
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton[] statusBtns, categoryBtns;
    private String filterStatus = "Tất cả", filterCat = "Tất cả";
    

    public ManHinhBanHang() {
        busHoaDon = new BUS_HoaDon();
        initUI();
        loadData();
        
        // THÊM ĐOẠN NÀY ĐỂ TỰ ĐỘNG RESET BẢNG KHI ĐỔI TÀI KHOẢN HOẶC MỞ LẠI TAB
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                txtSearch.setText("Mã HD, khách hàng, SĐT...");
                txtSearch.setForeground(Color.GRAY);
                loadData();
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

        // --- 1. Tab Bán hàng ---
        JLabel lblBanHang = new JLabel("Bán hàng");
        lblBanHang.setIcon(new MenuIcon("CART"));
        lblBanHang.setIconTextGap(8);
        lblBanHang.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblBanHang.setForeground(Color.decode("#1967D2"));
        lblBanHang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, Color.decode("#1967D2")),
                BorderFactory.createEmptyBorder(20, 0, 20, 40)));

        // --- 2. Tab Đổi / Trả hàng ---
        JLabel lblDoiTra = new JLabel("Đổi / Trả hàng");
        lblDoiTra.setIcon(new MenuIcon("BOX"));
        lblDoiTra.setIconTextGap(8);
        lblDoiTra.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblDoiTra.setForeground(Color.decode("#6C757D"));
        lblDoiTra.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 40));
        lblDoiTra.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblDoiTra.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { 
                chuyenManHinh("DoiTra"); 
            }
        });

        // --- 3. Tab Danh Sách Hóa Đơn ---
        

        pnlLeftTabs.add(lblBanHang);
        pnlLeftTabs.add(lblDoiTra);
        
        pnlTabs.add(pnlLeftTabs, BorderLayout.WEST);

        // ==================== 2. HEADER ====================
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(25, 0, 15, 0));

        JLabel lblTitle = new JLabel("BÁN HÀNG — HÓA ĐƠN");
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

        JLabel lblSearchIcon = new JLabel(new MenuIcon("SEARCH")); 
        lblSearchIcon.setForeground(Color.GRAY);
        pnlSearchWrapper.add(lblSearchIcon, BorderLayout.WEST);

        String placeholder = "Mã HD, khách hàng, SĐT...";
        txtSearch = new JTextField(placeholder);
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBorder(null);
        pnlSearchWrapper.add(txtSearch, BorderLayout.CENTER);

        txtSearch.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals(placeholder)) {
                    txtSearch.setText(""); txtSearch.setForeground(Color.BLACK);
                }
            }
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().trim().isEmpty()) {
                    txtSearch.setForeground(Color.GRAY); txtSearch.setText(placeholder);
                }
            }
        });
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { applyFilter(); }
        });

        JButton btnReset = createActionBtn("Làm mới", "#6C757D");
        btnReset.setIcon(new MenuIcon("REFRESH"));
        btnReset.setIconTextGap(6);
        btnReset.addActionListener(e -> {
            txtSearch.setForeground(Color.GRAY);
            txtSearch.setText(placeholder);
            xuLyStatus(statusBtns[0]);
            xuLyCat(categoryBtns[0]); // Đã hết bị lỗi NullPointerException ở đây
            pnlHeader.requestFocus(); 
        });

        JButton btnCreate = createActionBtn("Tạo hóa đơn", "#E11D48");
        btnCreate.addActionListener(e -> {
            Window p = SwingUtilities.getWindowAncestor(this);
            TaoHoaDon dialogTaoHoaDon = new TaoHoaDon((Frame) p, model); 
            dialogTaoHoaDon.setVisible(true);
            
            // THÊM DÒNG NÀY: Làm mới bảng ngay sau khi đóng form Tạo Hóa Đơn
            loadData(); 
        });

        pnlActions.add(pnlSearchWrapper);
        pnlActions.add(btnReset);
        pnlActions.add(btnCreate);
        pnlHeader.add(pnlActions, BorderLayout.EAST);

        // ==================== 3. BỘ LỌC ====================
        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlFilters.setOpaque(false);
        pnlFilters.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        pnlFilters.add(new JLabel("Trạng thái: "));
        statusBtns = new JButton[]{
            createFilterBtn("Tất cả", true, false), 
            createFilterBtn("Hoàn thành", false, false),
            createFilterBtn("Đang xử lý", false, false), 
            createFilterBtn("Đã hủy", false, false)
        };
        for (JButton b : statusBtns) { pnlFilters.add(b); b.addActionListener(e -> xuLyStatus(b)); }
        javax.swing.Timer autoOpenTimer = new javax.swing.Timer(500, evt -> {
            if (ManHinhBanHang.pendingDraftIdToOpen != null && !ManHinhBanHang.pendingDraftIdToOpen.isEmpty() && this.isShowing()) {
                String maHD = ManHinhBanHang.pendingDraftIdToOpen;
                ManHinhBanHang.pendingDraftIdToOpen = null; 
                
                ((javax.swing.Timer) evt.getSource()).stop(); // Dừng bộ đếm
                
                SwingUtilities.invokeLater(() -> {
                    try {
                        if (maHD.equals("NEW_INVOICE")) {
                            Window p = SwingUtilities.getWindowAncestor(ManHinhBanHang.this);
                            TaoHoaDon dialogTaoHoaDon = new TaoHoaDon((Frame) p, model); 
                            dialogTaoHoaDon.setVisible(true);
                        } else {
                            moLaiHoaDonNhap(maHD);
                        }
                    } finally {
                        ((javax.swing.Timer) evt.getSource()).start(); // Bật lại sau khi xong
                    }
                });
            }
        });
        autoOpenTimer.start();

        JPanel pnlNorth = new JPanel();
        pnlNorth.setLayout(new BoxLayout(pnlNorth, BoxLayout.Y_AXIS));
        pnlNorth.add(pnlTabs);
        pnlNorth.add(pnlHeader);
        pnlNorth.add(pnlFilters);
        this.add(pnlNorth, BorderLayout.NORTH);

        // ==================== 4. BẢNG DỮ LIỆU & SỰ KIỆN CLICK ====================
        String[] cols = {"Mã HD", "Ngày", "Khách hàng", "SĐT", "Thanh toán", "Tổng tiền", "Trạng thái", "Xem HD", "Hidden"};
        model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        sorter.setComparator(1, new java.util.Comparator<String>() {
            java.text.SimpleDateFormat sdfFull = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
            java.text.SimpleDateFormat sdfShort = new java.text.SimpleDateFormat("dd/MM/yyyy");
            @Override
            public int compare(String s1, String s2) {
                try {
                    java.util.Date d1 = s1.contains(":") ? sdfFull.parse(s1) : sdfShort.parse(s1);
                    java.util.Date d2 = s2.contains(":") ? sdfFull.parse(s2) : sdfShort.parse(s2);
                    return d1.compareTo(d2); 
                } catch (Exception e) {
                    return s1.compareTo(s2);
                }
            }
        });
        
        java.util.List<RowSorter.SortKey> sortKeys = new java.util.ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();
        
        table.setRowHeight(55); 
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setShowGrid(false); 
        table.setShowHorizontalLines(true); 
        table.setIntercellSpacing(new Dimension(0, 0)); 
        table.setGridColor(Color.decode("#F1F3F5")); 
        table.setSelectionBackground(Color.decode("#F8F9FA")); 
        table.setSelectionForeground(Color.decode("#212B36"));

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(100, 50));
        header.setBackground(Color.decode("#F9FAFB"));
        header.setForeground(Color.decode("#637381"));
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        table.getColumnModel().getColumn(8).setMinWidth(0);
        table.getColumnModel().getColumn(8).setMaxWidth(0);
        table.setDefaultRenderer(Object.class, new ModernTableRenderer());

     // Tìm đến đoạn xử lý sự kiện click table trong ManHinhBanHang.java
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int viewRow = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                
                if (viewRow >= 0 && col == 7) { 
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    String maHoaDon = table.getValueAt(viewRow, 0).toString();
                    String ngay = table.getValueAt(viewRow, 1).toString();
                    String khach = table.getValueAt(viewRow, 2).toString();
                    String sdt = table.getValueAt(viewRow, 3).toString();
                    String phuongThuc = table.getValueAt(viewRow, 4).toString();
                    String tongTien = table.getValueAt(viewRow, 5).toString();
                    String status = table.getValueAt(viewRow, 6).toString();
                    
                    Window p = SwingUtilities.getWindowAncestor(ManHinhBanHang.this);

                    // tenNhanVien để trống — ChiTietHoaDon sẽ tự lấy đúng từ DB qua JOIN NhanVien
                    String tenNhanVienHienTai = "";

                    if (status.equals("Đang xử lý")) {
                        TaoHoaDon dialogSua = new TaoHoaDon((Frame) p, model, modelRow, maHoaDon, khach, sdt); 
                        dialogSua.setVisible(true);
                        loadData();
                    } else {
                        // 2. THAY ĐỔI: Sử dụng BUS thay vì trực tiếp gọi DAO
                        BUS.BUS_ChiTietHoaDon busCTHD = new BUS.BUS_ChiTietHoaDon();
                        java.util.List<Object[]> listSanPham = busCTHD.layDanhSachSanPhamTheoMaHD(maHoaDon);

                        ChiTietHoaDon dialogChiTiet = new ChiTietHoaDon(
                            (Frame) p, maHoaDon, ngay, khach, sdt, phuongThuc, tongTien, tenNhanVienHienTai, listSanPham
                        );
                        dialogChiTiet.setVisible(true);
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8")));
        sp.getVerticalScrollBar().setUI(new Utils.ModernScrollBarUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        this.add(sp, BorderLayout.CENTER);

        // ==================== 5. PHÂN TRANG ====================
        JPanel pnlSouth = new JPanel(new BorderLayout());
        pnlSouth.setOpaque(false);
        pnlSouth.setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel pnlPage = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlPage.setOpaque(false);
        pnlPage.add(new JButton("< Trước")); pnlPage.add(new JButton("Tiếp >"));

        JLabel lblInfo = new JLabel("Trang 1/1 (3 HĐ)");
        lblInfo.setForeground(Color.GRAY);
        pnlSouth.add(pnlPage, BorderLayout.WEST);
        pnlSouth.add(lblInfo, BorderLayout.EAST);
        this.add(pnlSouth, BorderLayout.SOUTH);
    }

    public void moLaiHoaDonNhap(String maHD) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 0).toString().equals(maHD)) {
                    String khach = model.getValueAt(i, 2).toString();
                    String sdt = model.getValueAt(i, 3).toString();
                    Window p = SwingUtilities.getWindowAncestor(this);
                    
                    TaoHoaDon dialogSua = new TaoHoaDon((Frame) p, model, i, maHD, khach, sdt);
                    dialogSua.setVisible(true);
                    break;
                }
            }
        });
    }

    private void chuyenManHinh(String tenManHinh) {
        SwingUtilities.invokeLater(() -> {
            Container parent = this.getParent();
            while (parent != null && !(parent.getLayout() instanceof CardLayout)) {
                parent = parent.getParent();
            }
            
            if (parent != null) {
                CardLayout cl = (CardLayout) parent.getLayout();
                cl.show(parent, tenManHinh);
                
                Container topLevel = parent.getParent();
                while (topLevel != null && !(topLevel instanceof MainDashboard)) {
                    topLevel = topLevel.getParent();
                }
                if (topLevel instanceof MainDashboard) {
                    if (tenManHinh.equals("Bán hàng & Đổi trả") || 
                        tenManHinh.equals("DanhSachHD") || 
                        tenManHinh.equals("DoiTra")) {
                        ((MainDashboard) topLevel).chuyenSangTabBanHang(); 
                    }
                }
            }
        });
    }

    public void loadData() {
        model.setRowCount(0);
        Utils.UserSession session = Utils.UserSession.getInstance();

        List<Object[]> ds;
        if (session.isAdmin()) {
            ds = busHoaDon.layDanhSachHoaDonChoBang();
        } else {
            String maNV = session.getMaNhanVien();
            List<Object[]> staffDs = busHoaDon.layDanhSachHoaDonTheoNVHomNay(maNV);
            ds = (staffDs != null) ? staffDs : busHoaDon.layDanhSachHoaDonChoBang();
        }

        // --- ĐÃ FIX: LỌC THEO GIỜ MỞ CA BỎ QUA SỐ GIÂY ---
        java.time.LocalDateTime shiftStart = null;
        if (!session.isAdmin() && session.getCaHienTai() != null) {
            // Cắt bỏ phần giây để so sánh công bằng với CSDL
            shiftStart = session.getCaHienTai().getThoiGianBatDau().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
        }

        for (Object[] row : ds) {
            String trangThai = row[6] != null ? row[6].toString() : "";
            if (trangThai.equals("Đổi trả")) continue; // Bỏ qua hóa đơn đổi trả

            boolean hopLeChoCaNay = true;
            if (shiftStart != null && row[1] != null && !row[1].toString().trim().isEmpty()) {
                String ngayStr = row[1].toString().trim();
                java.time.LocalDateTime hdTime = null;
                
                // Cơ chế đọc ngày thông minh chống lỗi crash
                try {
                    hdTime = java.time.LocalDateTime.parse(ngayStr, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                } catch (Exception e1) {
                    try {
                        hdTime = java.time.LocalDateTime.parse(ngayStr, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    } catch (Exception e2) {
                        try {
                            hdTime = java.time.LocalDateTime.parse(ngayStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.S]"));
                        } catch (Exception e3) {
                            try {
                                java.time.LocalDate dateOnly = java.time.LocalDate.parse(ngayStr, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                hdTime = dateOnly.atTime(23, 59, 59);
                            } catch (Exception e4) {
                                System.err.println("Lỗi format ngày tại Hóa Đơn: [" + ngayStr + "]");
                            }
                        }
                    }
                }

                if (hdTime != null) {
                    // Ép giờ hóa đơn về tròn phút để so sánh
                    hdTime = hdTime.truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
                    if (hdTime.isBefore(shiftStart)) {
                        hopLeChoCaNay = false;
                    }
                } else {
                    hopLeChoCaNay = false; // Lỗi format quá nặng, loại bỏ để an toàn
                }
            }
            
            if (hopLeChoCaNay) {
                model.addRow(row);
            }
        }
    }

    private JButton createActionBtn(String txt, String hex) {
        JButton btn = new JButton(txt) {
            public Dimension getPreferredSize() { Dimension size = super.getPreferredSize(); size.height = 45; return size; }
        };
        btn.setBackground(Color.decode(hex)); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14)); btn.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15)); 
        return btn;
    }

    private JButton createFilterBtn(String text, boolean isActive, boolean isPurple) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13)); btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        if (isActive) setBtnActive(btn, isPurple); else setBtnNormal(btn);
        return btn;
    }

    private void setBtnActive(JButton btn, boolean isPurple) {
        Color bgColor = Color.decode(isPurple ? "#9300D9" : "#1967D2");
        btn.setBackground(bgColor); btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bgColor, 1), BorderFactory.createEmptyBorder(8, 18, 8, 18)));
    }

    private void setBtnNormal(JButton btn) {
        btn.setBackground(Color.WHITE); btn.setForeground(Color.decode("#374151"));
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1), BorderFactory.createEmptyBorder(7, 17, 7, 17)));
    }

    private void xuLyStatus(JButton b) {
        String text = b.getText().trim();
        if (filterStatus.equals(text) && !text.equals("Tất cả")) { xuLyStatus(statusBtns[0]); return; }
        for (JButton btn : statusBtns) setBtnNormal(btn);
        setBtnActive(b, false); filterStatus = text; applyFilter();
    }

    private void xuLyCat(JButton b) {
        String text = b.getText().trim();
        if (filterCat.equals(text) && !text.equals("Tất cả")) { xuLyCat(categoryBtns[0]); return; }
        for (JButton btn : categoryBtns) setBtnNormal(btn);
        setBtnActive(b, true); filterCat = text; applyFilter();
    }

    private void applyFilter() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (!filterStatus.equals("Tất cả")) filters.add(RowFilter.regexFilter("^" + filterStatus + "$", 6)); 
        if (!filterCat.equals("Tất cả")) filters.add(RowFilter.regexFilter("^" + filterCat + "$", 8)); 
        String search = txtSearch.getText().trim();
        if (!search.isEmpty() && !search.equals("Mã HD, khách hàng, SĐT...")) filters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(search)));
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    class ModernTableRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasF, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasF, r, c);
            lbl.setHorizontalAlignment(CENTER); lbl.setIcon(null); 
            if (hasF) lbl.setBorder(new EmptyBorder(0, 0, 0, 0));
            lbl.setOpaque(true);

            if (c == 6) { 
                if (v.equals("Hoàn thành")) { lbl.setBackground(Color.decode("#DCFCE7")); lbl.setForeground(Color.decode("#10B981")); }
                else if (v.equals("Đang xử lý")) { lbl.setBackground(Color.decode("#FEF3C7")); lbl.setForeground(Color.decode("#F59E0B")); }
                else { lbl.setBackground(Color.decode("#FEE2E2")); lbl.setForeground(Color.decode("#EF4444")); }
            } else {
                lbl.setBackground(isSel ? Color.decode("#F8F9FA") : Color.WHITE);
                lbl.setForeground(Color.decode("#212B36"));
                if (c == 0) lbl.setForeground(Color.decode("#6A1B9A")); 
                if (c == 7) { 
                    lbl.setForeground(Color.decode("#1967D2")); 
                    if (t.getValueAt(r, 6).toString().equals("Đang xử lý")) { lbl.setText("Sửa"); lbl.setIcon(new MenuIcon("EDIT")); }
                    else { lbl.setText("Xem"); lbl.setIcon(new MenuIcon("EYE")); }
                    lbl.setIconTextGap(6);
                }
            }
            return lbl;
        }
    }

    public void moFormThemMoi() {
        for (Component c : getComponents()) {
            if (c instanceof JPanel) {
                for (Component child : ((JPanel) c).getComponents()) {
                    if (child instanceof JButton && ((JButton) child).getText().contains("Thêm")) {
                        ((JButton) child).doClick();
                        return;
                    }
                }
            }
        }
    }
}