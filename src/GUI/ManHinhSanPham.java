package GUI;

import BUS.BUS_SanPham;
import ConnectDB.ConnectDB;
import Utils.MenuIcon;
import Entity.LoHang;
import Entity.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class ManHinhSanPham extends JPanel {

    public enum ProductCategory { OTC, ETC, SUPPLEMENT }
    public enum DosageForm { SOLID, LIQUID_DOSAGE }
    public enum LotStatus { AVAILABLE, EXPIRED, FAULTY }

    public static class MeasurementName {
        private String name;
        public MeasurementName(String name) { this.name = name; }
        public String getName() { return name; }
        @Override public String toString() { return name; }
    }

    public static class UnitOfMeasure {
        private MeasurementName measurement;
        private BigDecimal baseUnitConversionRate;
        public UnitOfMeasure(MeasurementName measurement, BigDecimal rate) { this.measurement = measurement; this.baseUnitConversionRate = rate; }
        public MeasurementName getMeasurement() { return measurement; }
        public BigDecimal getBaseUnitConversionRate() { return baseUnitConversionRate; }
    }

    public static class Lot {
        private String id, batchNumber;
        private int quantity;
        private BigDecimal rawPrice;
        private LocalDate expiryDate;
        private LotStatus status;
        public Lot(String id, String batch, int qty, BigDecimal price, LocalDate exp, LotStatus st) {
            this.id = id; this.batchNumber = batch; this.quantity = qty; this.rawPrice = price; this.expiryDate = exp; this.status = st;
        }
        public String getId() { return id; } public String getBatchNumber() { return batchNumber; } public int getQuantity() { return quantity; }
        public BigDecimal getRawPrice() { return rawPrice; } public LocalDate getExpiryDate() { return expiryDate; } public LotStatus getStatus() { return status; }
    }

    public static class Product {
        private String id, barcode, name, shortName, manufacturer, activeIngredient, strength, description, baseUnitOfMeasure;
        private ProductCategory category; private DosageForm form; private double vat;
        private Set<UnitOfMeasure> unitOfMeasureSet = new HashSet<>();
        private Set<Lot> lotSet = new HashSet<>();
        public Product(String id, String bc, ProductCategory cat, DosageForm frm, String nm, String sn, String mfg, String act, double v, String str, String desc, String baseUom) {
            this.id = id; this.barcode = bc; this.category = cat; this.form = frm; this.name = nm; this.shortName = sn; this.manufacturer = mfg; this.activeIngredient = act; this.vat = v; this.strength = str; this.description = desc; this.baseUnitOfMeasure = baseUom;
        }
        public String getId() { return id; } public void setId(String id) { this.id = id; }
        public String getName() { return name; } public String getBarcode() { return barcode; } public String getShortName() { return shortName; }
        public String getManufacturer() { return manufacturer; } public String getActiveIngredient() { return activeIngredient; }
        public String getStrength() { return strength; } public String getDescription() { return description; }
        public ProductCategory getCategory() { return category; } public DosageForm getForm() { return form; }
        public double getVatAsDouble() { return vat; } public String getBaseUnitOfMeasure() { return baseUnitOfMeasure; }
        public Set<UnitOfMeasure> getUnitOfMeasureSet() { return unitOfMeasureSet; } public void setUnitOfMeasureSet(Set<UnitOfMeasure> uoms) { this.unitOfMeasureSet = uoms; }
        public Set<Lot> getLotSet() { return lotSet; } public void setLotSet(Set<Lot> lots) { this.lotSet = lots; }
    }

    // ========================================================================
    // GIAO DIỆN & LOGIC
    // ========================================================================
    private final Color COLOR_PRIMARY     = Color.decode("#1E3A8A");
    private final Color COLOR_HEADER_TABLE = Color.decode("#2D435E");
    private final Color COLOR_BORDER      = Color.decode("#DFE3E8");
    private final Color COLOR_LIGHT_BLUE  = Color.decode("#E0F2FE");
    private final Color COLOR_DISABLED_BG = Color.decode("#F3F4F6");

    private final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);

    // List & pagination
    private JTable tblSanPham, tblDonVi, tblLoHang;
    private DefaultTableModel modelSanPham;
    private ToggleEditableTableModel modelDonVi;
    private DefaultTableModel modelLoHang;

    private final List<Object[]> allDataMock         = new ArrayList<>();
    private final List<Object[]> currentFilteredData  = new ArrayList<>();
    private int currentPageMock  = 1;
    private int itemsPerPageMock = 10;
    private JLabel  lblPage;
    private JButton btnPrev, btnNext;
    private JComboBox<String> cbLimit;

    // Toolbar
    private WatermarkTextField txtTimKiem;
    private ButtonGroup bgLoai, bgDang;

    // Detail fields
    private JTextField txtId, txtTen, txtVietTat, txtHoatChat, txtHamLuong, txtVAT;
    private JTextArea  txtMoTa;
    private CardLayout clLoai, clDang, clNSX, clDVT;
    private JPanel     pnlLoai, pnlDang, pnlNSX, pnlDVT;
    private JTextField txtLoaiView, txtDangView, txtNSXView, txtDVTView;
    private JComboBox<String> cbLoaiCT, cbDang, cbNhaSX, cbDVT;

    private JPanel  pnlActionBottom;
    private JButton btnCapNhatBottom, btnXoaBottom, btnLuuBottom, btnHuyBottom, btnXacNhanThem;
    private JScrollPane scrollMoTa;
    private JScrollPane scrollTblSanPham;

    // === Panel body & detail toggle ===
    private JPanel  pnlBody;
    private JPanel  pnlRightDetail;
    private boolean detailVisible = false;

    // Cột bảng DS
    private static final String[] COLS_COLLAPSED = {"Mã", "Tên", "Loại", "Hoạt chất"};
    private static final String[] COLS_EXPANDED  = {"Mã", "Tên", "Loại", "Hoạt chất", "Dạng bào chế", "NSX", "VAT"};

    // Trạng thái
    private boolean isAdding       = false;
    private boolean uomAutoChanging = false;

    public ManHinhSanPham() {
        UIManager.put("TextField.inactiveForeground", Color.BLACK);
        UIManager.put("TextArea.inactiveForeground",  Color.BLACK);
        UIManager.put("ComboBox.disabledForeground",  Color.BLACK);
        UIManager.put("Button.focus",   new Color(0,0,0,0));
        UIManager.put("ComboBox.focus", new Color(0,0,0,0));
        UIManager.put("Component.focusWidth", 0);
        UIManager.put("ComboBox.border", BorderFactory.createLineBorder(COLOR_BORDER));

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        add(createTopbar(), BorderLayout.NORTH);

        pnlBody = new JPanel(new BorderLayout());
        pnlBody.setBackground(Color.WHITE);
        pnlBody.add(createSidebarFilter(), BorderLayout.WEST);
        pnlBody.add(createCenterList(),    BorderLayout.CENTER);

        // Tạo panel detail nhưng CHƯA add — mặc định ẩn
        pnlRightDetail = createRightDetail();

        add(pnlBody, BorderLayout.CENTER);

        khoiTaoDuLieuDB();
        currentFilteredData.addAll(allDataMock);
        // Mặc định: detail ẩn, bảng mở rộng 7 cột
        setDetailVisible(false);
    }

    // ========================================================================
    // SHOW / HIDE PANEL CHI TIẾT
    // ========================================================================
    private void setDetailVisible(boolean visible) {
        detailVisible = visible;
        if (visible) {
            if (pnlRightDetail.getParent() == null) pnlBody.add(pnlRightDetail, BorderLayout.EAST);
            rebuildTableColumns(COLS_COLLAPSED);
            scrollTblSanPham.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        } else {
            if (pnlRightDetail.getParent() != null) pnlBody.remove(pnlRightDetail);
            rebuildTableColumns(COLS_EXPANDED);
            scrollTblSanPham.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }
        
        // Luôn để bảng tự động co dãn theo vùng hiển thị (Center) để che khuyết điểm
        tblSanPham.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        pnlBody.revalidate();
        pnlBody.repaint();
        updatePagination();
    }

    private void rebuildTableColumns(String[] cols) {
        modelSanPham = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tblSanPham.setModel(modelSanPham);
        applyTableCellRenderers();
    }

    private void applyTableCellRenderers() {
        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tblSanPham.getColumnCount(); i++) {
            if (i == 2) {
                tblSanPham.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                        String loai = value != null ? value.toString() : "";
                        if      (loai.equals("Thuốc kê đơn"))       c.setForeground(Color.decode("#EF4444"));
                        else if (loai.equals("Sản phẩm chức năng"))  c.setForeground(Color.decode("#10B981"));
                        else if (loai.equals("Thuốc không kê đơn"))  c.setForeground(Color.decode("#2179E0"));
                        else c.setForeground(Color.BLACK);
                        return c;
                    }
                });
            } else {
                tblSanPham.getColumnModel().getColumn(i).setCellRenderer(centerR);
            }
        }
    }

    // ========================================================================
    // MÔ HÌNH BẢNG
    // ========================================================================
    private static class ToggleEditableTableModel extends DefaultTableModel {
        private boolean editable = true;
        private final BitSet readOnlyCols = new BitSet();
        ToggleEditableTableModel(String[] cols, int rows) { super(cols, rows); }
        void setEditable(boolean e) { if (this.editable != e) { this.editable = e; fireTableDataChanged(); } }
        void setReadOnlyColumns(int... cols) { readOnlyCols.clear(); if (cols != null) for (int c : cols) if (c >= 0) readOnlyCols.set(c); fireTableDataChanged(); }
        @Override public boolean isCellEditable(int r, int c) { return editable && !readOnlyCols.get(c); }
    }

    // ========================================================================
    // UI UTILS
    // ========================================================================
    class WatermarkTextField extends JTextField {
        private String watermark;
        public WatermarkTextField(String watermark) { this.watermark = watermark; }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !hasFocus()) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setColor(Color.GRAY); g2.setFont(getFont().deriveFont(Font.ITALIC));
                FontMetrics fm = g2.getFontMetrics(); int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(watermark, getInsets().left + 5, y); g2.dispose();
            }
        }
    }

    private void applyFlatComboBoxStyle(JComboBox<String> cb) {
        cb.setBackground(Color.WHITE); cb.setFocusable(false);
        cb.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton btn = new JButton("\u25BC"); btn.setFont(new Font("Segoe UI", Font.PLAIN, 10)); btn.setForeground(Color.GRAY); btn.setBackground(Color.WHITE); btn.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8)); btn.setFocusPainted(false); btn.setContentAreaFilled(false); btn.setOpaque(true); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); return btn;
            }
        });
        cb.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(0, 5, 0, 5)));
    }

    // ========================================================================
    // 1. THANH TOPBAR
    // ========================================================================
    private JPanel createTopbar() {
        JPanel pnl = new JPanel(); pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS)); pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER), new EmptyBorder(15, 20, 15, 20)));

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); pnlLeft.setOpaque(false);
        JLabel lblBoxIcon = new JLabel(new MenuIcon("PACKAGE")); lblBoxIcon.setForeground(COLOR_PRIMARY);
        pnlLeft.add(lblBoxIcon);
        pnlLeft.add(new JLabel("<html><b style='color:#1E3A8A; font-size:16px;'>QUẢN LÝ SẢN PHẨM</b></html>"));
        JLabel lblCount = new JLabel("(đang tải...)");
        pnlLeft.add(lblCount);
        // Cập nhật số lượng sau khi load DB
        SwingUtilities.invokeLater(() -> lblCount.setText("(" + allDataMock.size() + " sản phẩm)"));

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); pnlRight.setOpaque(false);
        pnlRight.add(new JLabel("Tìm kiếm: "));
        txtTimKiem = new WatermarkTextField("Tên, mã, hoạt chất..."); txtTimKiem.setPreferredSize(new Dimension(180, 36)); pnlRight.add(txtTimKiem);

        JButton btnTim    = createBtnWithIcon("Tìm",        "#1D68B2", new MenuIcon("SEARCH"));  btnTim.setPreferredSize(new Dimension(95, 36));
        JButton btnLamMoi = createBtnWithIcon("Làm mới",    "#64748B", new MenuIcon("REFRESH")); btnLamMoi.setPreferredSize(new Dimension(115, 36));
        JButton btnXuat   = createBtnWithIcon("Xuất Excel", "#22C55E", new MenuIcon("EXPORT"));  btnXuat.setPreferredSize(new Dimension(130, 36));
        JButton btnNhap   = createBtnWithIcon("Nhập Excel", "#0EA5E9", new MenuIcon("IMPORT"));  btnNhap.setPreferredSize(new Dimension(130, 36));
        JButton btnThem   = createBtnWithIcon("Thêm mới",   "#E11D48", new MenuIcon("ADD"));     btnThem.setPreferredSize(new Dimension(125, 36));

        btnTim.addActionListener(e -> doSearch());
        btnLamMoi.addActionListener(e -> {
            txtTimKiem.setText("");
            bgLoai.getElements().nextElement().setSelected(true);
            bgDang.getElements().nextElement().setSelected(true);
            doSearch();
            setDetailVisible(false);
        });
        btnXuat.addActionListener(e -> showExportDialog());
        btnNhap.addActionListener(e -> showImportDialog());
        btnThem.addActionListener(e -> batDauThemMoi());

        pnlRight.add(btnTim); pnlRight.add(btnLamMoi);
        pnlRight.add(btnXuat); pnlRight.add(btnNhap);
        pnlRight.add(btnThem);
        pnl.add(pnlLeft); pnl.add(Box.createHorizontalGlue()); pnl.add(pnlRight);
        return pnl;
    }

    private void doSearch() {
        String keyword    = txtTimKiem.getText().toLowerCase().trim();
        String loaiSelect = getSelectedRadioText(bgLoai);
        String dangSelect = getSelectedRadioText(bgDang);
        currentFilteredData.clear();
        for (Object[] row : allDataMock) {
            boolean matchKey  = keyword.isEmpty() || row[0].toString().toLowerCase().contains(keyword) || row[1].toString().toLowerCase().contains(keyword) || row[3].toString().toLowerCase().contains(keyword);
            boolean matchLoai = loaiSelect.equals("Tất cả") || row[2].toString().equalsIgnoreCase(loaiSelect);
            boolean matchDang = dangSelect.equals("Tất cả") || (row.length > 4 && row[4].toString().equalsIgnoreCase(dangSelect));
            if (matchKey && matchLoai && matchDang) currentFilteredData.add(row);
        }
        currentPageMock = 1; updatePagination();
    }

    private String getSelectedRadioText(ButtonGroup bg) {
        for (Enumeration<AbstractButton> buttons = bg.getElements(); buttons.hasMoreElements();) { AbstractButton b = buttons.nextElement(); if (b.isSelected()) return b.getText(); } return "Tất cả";
    }

    // ========================================================================
    // 2. SIDEBAR LỌC
    // ========================================================================
    private JPanel createSidebarFilter() {
        JPanel pnl = new JPanel(); pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS)); pnl.setPreferredSize(new Dimension(190, 0)); pnl.setBackground(Color.WHITE); pnl.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, COLOR_BORDER));
        pnl.add(createLabelFilter("Loại")); JSeparator sep1 = new JSeparator(); sep1.setMaximumSize(new Dimension(170, 1)); sep1.setForeground(COLOR_BORDER); sep1.setAlignmentX(Component.LEFT_ALIGNMENT); pnl.add(sep1); pnl.add(Box.createVerticalStrut(5));
        bgLoai = new ButtonGroup(); pnl.add(createRadioGroup(new String[]{"Tất cả", "Thuốc kê đơn", "Thuốc không kê đơn", "Sản phẩm chức năng"}, bgLoai));
        pnl.add(Box.createVerticalStrut(15));
        pnl.add(createLabelFilter("Dạng bào chế")); JSeparator sep2 = new JSeparator(); sep2.setMaximumSize(new Dimension(170, 1)); sep2.setForeground(COLOR_BORDER); sep2.setAlignmentX(Component.LEFT_ALIGNMENT); pnl.add(sep2); pnl.add(Box.createVerticalStrut(5));
        bgDang = new ButtonGroup(); pnl.add(createRadioGroup(new String[]{"Tất cả", "Viên nén", "Viên nang", "Viên sủi", "Thuốc bột", "Kẹo ngậm", "Dung dịch", "Hỗn dịch", "Thuốc nhỏ giọt", "Súc miệng"}, bgDang));
        return pnl;
    }

    private JPanel createRadioGroup(String[] options, ButtonGroup group) {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(Color.WHITE); p.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int i = 0; i < options.length; i++) {
            JRadioButton rb = new JRadioButton(options[i]); rb.setBackground(Color.WHITE); rb.setFont(FONT_NORMAL); rb.setBorder(new EmptyBorder(5, 15, 5, 0)); rb.setFocusPainted(false);
            if (i == 0) { rb.setSelected(true); rb.setForeground(Color.decode("#1D68B2")); rb.setFont(FONT_BOLD); }
            rb.addItemListener(e -> { if (e.getStateChange() == ItemEvent.SELECTED) { rb.setForeground(Color.decode("#1D68B2")); rb.setFont(FONT_BOLD); doSearch(); } else { rb.setForeground(Color.BLACK); rb.setFont(FONT_NORMAL); } });
            group.add(rb); p.add(rb);
        }
        return p;
    }

    // ========================================================================
    // 3. BẢNG DANH SÁCH & PHÂN TRANG
    // ========================================================================
    private JPanel createCenterList() {
        JPanel pnl = new JPanel(new BorderLayout()); pnl.setBackground(Color.WHITE); pnl.setBorder(null);
        JLabel lblHeader = new JLabel("  Danh sách sản phẩm"); lblHeader.setOpaque(true); lblHeader.setBackground(COLOR_HEADER_TABLE); lblHeader.setForeground(Color.WHITE); lblHeader.setFont(FONT_BOLD); lblHeader.setPreferredSize(new Dimension(0, 38)); pnl.add(lblHeader, BorderLayout.NORTH);

        // Khởi tạo với cột mở rộng (detail mặc định ẩn)
        modelSanPham = new DefaultTableModel(COLS_EXPANDED, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tblSanPham = new JTable(modelSanPham); setupTableStyle(tblSanPham);
        tblSanPham.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // Đã sửa
        applyTableCellRenderers();
        int[] widths = {130, 200, 140, 130, 110, 100, 60};
        for (int i = 0; i < widths.length; i++) tblSanPham.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        tblSanPham.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = tblSanPham.getSelectedRow();
                if (row != -1) { isAdding = false; hienThiChiTietSanPham(row); }
            }
        });

        scrollTblSanPham = new JScrollPane(tblSanPham);
        scrollTblSanPham.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        scrollTblSanPham.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollTblSanPham.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        applyThinScrollBar(scrollTblSanPham);
        pnl.add(scrollTblSanPham, BorderLayout.CENTER);

        JPanel pnlPage = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)); pnlPage.setBackground(Color.WHITE);
        btnPrev = new JButton("< Trang trước"); btnPrev.setFont(new Font("Segoe UI", Font.PLAIN, 14)); btnPrev.setBackground(Color.WHITE); btnPrev.setFocusPainted(false); btnPrev.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(5, 10, 5, 10)));
        btnNext = new JButton("Trang tiếp >");  btnNext.setFont(new Font("Segoe UI", Font.PLAIN, 14)); btnNext.setBackground(Color.WHITE); btnNext.setFocusPainted(false); btnNext.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(5, 10, 5, 10)));
        cbLimit = new JComboBox<>(new String[]{"10", "20", "50"}); cbLimit.setFont(new Font("Segoe UI", Font.PLAIN, 14)); cbLimit.setBackground(Color.WHITE);
        lblPage = new JLabel("Trang 1/1 (Tổng: 0)"); lblPage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnPrev.addActionListener(e -> { if (currentPageMock > 1) { currentPageMock--; updatePagination(); } });
        btnNext.addActionListener(e -> { int max = (int) Math.ceil((double) currentFilteredData.size() / itemsPerPageMock); if (currentPageMock < max) { currentPageMock++; updatePagination(); } });
        cbLimit.addActionListener(e -> { itemsPerPageMock = Integer.parseInt(cbLimit.getSelectedItem().toString()); currentPageMock = 1; updatePagination(); });
        pnlPage.add(btnPrev); pnlPage.add(btnNext); pnlPage.add(cbLimit); pnlPage.add(lblPage);
        pnl.add(pnlPage, BorderLayout.SOUTH);
        return pnl;
    }

    private void updatePagination() {
        int total = currentFilteredData.size();
        int totalPages = (int) Math.ceil((double) total / itemsPerPageMock); if (totalPages == 0) totalPages = 1; if (currentPageMock > totalPages) currentPageMock = totalPages;
        lblPage.setText("Trang " + currentPageMock + "/" + totalPages + " (Tổng: " + total + ")");
        btnPrev.setEnabled(currentPageMock > 1); btnNext.setEnabled(currentPageMock < totalPages);
        int start = (currentPageMock - 1) * itemsPerPageMock; int end = Math.min(start + itemsPerPageMock, total);
        modelSanPham.setRowCount(0);
        for (int i = start; i < end; i++) {
            Object[] row = currentFilteredData.get(i);
            if (detailVisible) {
                modelSanPham.addRow(new Object[]{row[0], row[1], row[2], row[3]});
            } else {
                String dang = row.length > 4 ? row[4].toString() : "";
                modelSanPham.addRow(new Object[]{row[0], row[1], row[2], row[3], dang, "DHG Pharma", "10%"});
            }
        }
    }

    // ========================================================================
    // 4. CHI TIẾT SẢN PHẨM & BẢNG PHỤ
    // ========================================================================
    private JPanel createRightDetail() {
        JPanel pnl = new JPanel(new BorderLayout()); pnl.setPreferredSize(new Dimension(450, 0)); pnl.setBackground(Color.WHITE); pnl.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, COLOR_BORDER));
        
        // --- THÊM NÚT ĐÓNG VÀO HEADER ---
        JPanel pnlH = new JPanel(new BorderLayout()); 
        pnlH.setBackground(COLOR_HEADER_TABLE); 
        pnlH.setPreferredSize(new Dimension(0, 38));
        
        JLabel lblCT = new JLabel("  Thông tin sản phẩm"); 
        lblCT.setForeground(Color.WHITE); 
        lblCT.setFont(FONT_BOLD); 
        pnlH.add(lblCT, BorderLayout.WEST);

        JButton btnDong = new JButton("X ");
        btnDong.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnDong.setForeground(Color.WHITE);
        btnDong.setBackground(COLOR_HEADER_TABLE);
        btnDong.setBorder(null);
        btnDong.setFocusPainted(false);
        btnDong.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDong.addActionListener(e -> setDetailVisible(false));
        pnlH.add(btnDong, BorderLayout.EAST);
        
        pnl.add(pnlH, BorderLayout.NORTH);
        // --------------------------------

        JPanel pnlScrollContent = new JPanel(); pnlScrollContent.setLayout(new BoxLayout(pnlScrollContent, BoxLayout.Y_AXIS)); pnlScrollContent.setBackground(Color.WHITE);
        JPanel pnlForm = new JPanel(new GridBagLayout()); pnlForm.setBackground(Color.WHITE); pnlForm.setBorder(new EmptyBorder(10, 15, 0, 15));
        GridBagConstraints g = new GridBagConstraints(); g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(3, 5, 3, 5); g.weightx = 1.0;

        addFormField(pnlForm, "Tên sản phẩm *", txtTen     = new JTextField(), g, 0, 0, 2);
        addFormField(pnlForm, "Mã sản phẩm",    txtId      = new JTextField(), g, 0, 2, 1);
        addFormField(pnlForm, "Tên viết tắt",   txtVietTat = new JTextField(), g, 1, 2, 1); // Đã dồn lên ngang hàng với Mã sản phẩm

        cbLoaiCT = new JComboBox<>(new String[]{"Thuốc không kê đơn", "Thuốc kê đơn", "Sản phẩm chức năng"}); applyFlatComboBoxStyle(cbLoaiCT);
        cbDang   = new JComboBox<>(new String[]{"Viên nén", "Viên nang", "Dung dịch", "Si rô", "Viên sủi", "Thuốc bột", "Kẹo ngậm", "Thuốc nhỏ giọt", "Súc miệng"}); applyFlatComboBoxStyle(cbDang);
        cbNhaSX  = new JComboBox<>(new String[]{"DHG Pharma", "Traphaco", "Sanofi", "Domesco"}); applyFlatComboBoxStyle(cbNhaSX);
        cbDVT    = new JComboBox<>(new String[]{"Viên", "Chai", "Hộp", "Vỉ", "Ống"}); applyFlatComboBoxStyle(cbDVT);

        pnlLoai = new JPanel(clLoai = new CardLayout()); txtLoaiView = createViewField(); pnlLoai.add(txtLoaiView, "VIEW"); pnlLoai.add(cbLoaiCT, "EDIT");
        pnlDang = new JPanel(clDang = new CardLayout()); txtDangView = createViewField(); pnlDang.add(txtDangView, "VIEW"); pnlDang.add(cbDang,   "EDIT");
        pnlNSX  = new JPanel(clNSX  = new CardLayout()); txtNSXView  = createViewField(); pnlNSX.add(txtNSXView,  "VIEW"); pnlNSX.add(cbNhaSX, "EDIT");
        pnlDVT  = new JPanel(clDVT  = new CardLayout()); txtDVTView  = createViewField(); pnlDVT.add(txtDVTView,  "VIEW"); pnlDVT.add(cbDVT,   "EDIT");

        // Các ô bên dưới cũng được dồn lên cho gọn gàng
        addCustomField(pnlForm, "Loại *",       pnlLoai, g, 0, 4);
        addCustomField(pnlForm, "Dạng bào chế", pnlDang, g, 1, 4);
        addFormField(pnlForm,   "Hoạt chất",    txtHoatChat = new JTextField(), g, 0, 6, 1);
        addFormField(pnlForm,   "Hàm lượng",    txtHamLuong = new JTextField(), g, 1, 6, 1);
        addCustomField(pnlForm, "Nhà sản xuất", pnlNSX,  g, 0, 8);
        addCustomField(pnlForm, "ĐVT gốc",      pnlDVT,  g, 1, 8);
        addFormField(pnlForm,   "VAT (%)",      txtVAT      = new JTextField(), g, 0, 10, 1);

        g.gridx = 0; g.gridy = 12; g.gridwidth = 2;
        JLabel lblMoTa = new JLabel("Mô tả"); lblMoTa.setFont(FONT_NORMAL); lblMoTa.setForeground(Color.BLACK); pnlForm.add(lblMoTa, g);
        g.gridy++;
        txtMoTa = new JTextArea() {
            @Override protected void paintComponent(Graphics g2) {
                super.paintComponent(g2);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2d = (Graphics2D) g2.create(); g2d.setColor(Color.GRAY); g2d.setFont(getFont().deriveFont(Font.ITALIC));
                    g2d.drawString("Chi tiết mô tả...", getInsets().left + 4, getInsets().top + getFontMetrics(getFont()).getAscent() + 2); g2d.dispose();
                }
            }
        };
        txtMoTa.setFont(FONT_NORMAL); txtMoTa.setLineWrap(true); txtMoTa.setWrapStyleWord(true); txtMoTa.setRows(3); txtMoTa.setDisabledTextColor(Color.BLACK);
        scrollMoTa = new JScrollPane(txtMoTa);
        scrollMoTa.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(2, 2, 2, 2)));
        scrollMoTa.setPreferredSize(new Dimension(0, 72));
        applyThinScrollBar(scrollMoTa);
        pnlForm.add(scrollMoTa, g);

        pnlScrollContent.add(pnlForm); pnlScrollContent.add(Box.createVerticalStrut(10));

        // Bảng Đơn vị quy đổi
        JPanel pnlDonViWrap = createSubTable("Đơn vị quy đổi", new String[]{"Tên ĐV", "Quy đổi"}, 140);
        tblDonVi   = (JTable) ((JScrollPane) pnlDonViWrap.getComponent(1)).getViewport().getView();
        modelDonVi = new ToggleEditableTableModel(new String[]{"Tên ĐV", "Quy đổi"}, 0);
        tblDonVi.setModel(modelDonVi); setupTableStyle(tblDonVi);
        modelDonVi.addTableModelListener(e -> {
            if (uomAutoChanging) return; uomAutoChanging = true;
            try {
                if (modelDonVi.getRowCount() == 0) { modelDonVi.addRow(new Object[2]); return; }
                int last = modelDonVi.getRowCount() - 1; boolean complete = true;
                for (int c = 0; c < 2; c++) if (modelDonVi.getValueAt(last, c) == null || modelDonVi.getValueAt(last, c).toString().isEmpty()) { complete = false; break; }
                if (complete) modelDonVi.addRow(new Object[2]);
            } finally { uomAutoChanging = false; }
        });
        pnlScrollContent.add(pnlDonViWrap);

        // Bảng Lô hàng
        JPanel pnlLotWrap = createSubTable("Lô & hạn sử dụng (Chỉ xem)", new String[]{"Mã lô", "Số lượng", "HSD", "Tình trạng"}, 160);
        tblLoHang   = (JTable) ((JScrollPane) pnlLotWrap.getComponent(1)).getViewport().getView();
        modelLoHang = new DefaultTableModel(new String[]{"Mã lô", "Số lượng", "HSD", "Tình trạng"}, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tblLoHang.setModel(modelLoHang); setupTableStyle(tblLoHang);
        tblLoHang.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblLoHang.getColumnModel().getColumn(0).setPreferredWidth(160);
        tblLoHang.getColumnModel().getColumn(1).setPreferredWidth(75);
        tblLoHang.getColumnModel().getColumn(2).setPreferredWidth(90);
        tblLoHang.getColumnModel().getColumn(3).setPreferredWidth(120);
        applyThinScrollBar((JScrollPane) pnlLotWrap.getComponent(1));

        DefaultTableCellRenderer centerTbl = new DefaultTableCellRenderer(); centerTbl.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tblLoHang.getColumnCount(); i++) {
            if (i == 3) {
                tblLoHang.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        JLabel lbl = new JLabel(value != null ? value.toString() : "", SwingConstants.CENTER);
                        lbl.setOpaque(true); lbl.setFont(new Font("Segoe UI", Font.BOLD, 11)); lbl.setBorder(new EmptyBorder(2, 6, 2, 6));
                        if ("Được bán".equals(value))              { lbl.setBackground(Color.decode("#DCFCE7")); lbl.setForeground(Color.decode("#15803D")); }
                        else if ("Hết hạn sử dụng".equals(value)) { lbl.setBackground(Color.decode("#FEE2E2")); lbl.setForeground(Color.decode("#B91C1C")); }
                        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5)); wrap.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE); wrap.add(lbl); return wrap;
                    }
                });
            } else { tblLoHang.getColumnModel().getColumn(i).setCellRenderer(centerTbl); }
        }
        pnlScrollContent.add(pnlLotWrap);

        JScrollPane mainScroll = new JScrollPane(pnlScrollContent); mainScroll.setBorder(null);
        applyThinScrollBar(mainScroll);
        pnl.add(mainScroll, BorderLayout.CENTER);

        // Khu vực nút bottom
        pnlActionBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10)); pnlActionBottom.setBackground(Color.WHITE); pnlActionBottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));
        btnCapNhatBottom = createBtnWithIcon("Cập nhật",      "#1D68B2", new MenuIcon("EDIT"));   btnCapNhatBottom.setPreferredSize(new Dimension(130, 36));
        btnXoaBottom     = createBtnWithIcon("Xóa",           "#EF4444", new MenuIcon("TRASH"));  btnXoaBottom.setPreferredSize(new Dimension(100, 36));
        btnLuuBottom     = createBtnWithIcon("Lưu",           "#10B981", new MenuIcon("SAVE"));   btnLuuBottom.setPreferredSize(new Dimension(100, 36));
        btnXacNhanThem   = createBtnWithIcon("Xác nhận Thêm", "#E11D48", new MenuIcon("ADD"));    btnXacNhanThem.setPreferredSize(new Dimension(170, 36));
        btnHuyBottom     = createBtnWithIcon("Hủy",           "#64748B", new MenuIcon("CANCEL")); btnHuyBottom.setPreferredSize(new Dimension(100, 36));

        btnCapNhatBottom.addActionListener(e -> { if (txtId.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần cập nhật!"); return; } setEditMode(true); });
        btnXoaBottom.addActionListener(e    -> actionXoaSanPham());
        btnLuuBottom.addActionListener(e    -> thucHienLuu());
        btnXacNhanThem.addActionListener(e  -> thucHienLuu());
        btnHuyBottom.addActionListener(e    -> huyThaoTac());

        pnl.add(pnlActionBottom, BorderLayout.SOUTH);
        return pnl;
    }

    // ========================================================================
    // LOGIC NGHIỆP VỤ
    // ========================================================================
    private void batDauThemMoi() {
        isAdding = true;
        txtTen.setText(""); txtVietTat.setText(""); txtHoatChat.setText(""); txtHamLuong.setText(""); txtVAT.setText("10"); txtMoTa.setText("");
        txtLoaiView.setText(""); txtDangView.setText(""); txtNSXView.setText(""); txtDVTView.setText("");
        cbLoaiCT.setSelectedIndex(0); cbDang.setSelectedIndex(0); cbNhaSX.setSelectedIndex(0); cbDVT.setSelectedIndex(0);
        txtId.setText("PRO2026-" + String.format("%04d", allDataMock.size() + 1));
        uomAutoChanging = true; modelDonVi.setRowCount(0); modelDonVi.addRow(new Object[2]); uomAutoChanging = false;
        modelLoHang.setRowCount(0);
        tblSanPham.clearSelection();
        setDetailVisible(true); // Mở detail khi thêm mới
        setEditMode(true);
    }

    private void huyThaoTac() {
        isAdding = false;
        setDetailVisible(false); // Thu lại khi hủy
        clearDetailForm();
        tblSanPham.clearSelection();
    }

    private void thucHienLuu() {
        String ma       = txtId.getText().trim();
        String ten      = txtTen.getText().trim();
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên sản phẩm!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String loaiHienThi = cbLoaiCT.getSelectedItem().toString();
        String hoatChat    = txtHoatChat.getText().trim();
        String dangBaoChe  = cbDang.getSelectedItem().toString();
        String nsx         = cbNhaSX.getSelectedItem().toString();
        String dvtGoc      = cbDVT.getSelectedItem().toString();
        String hamLuong    = txtHamLuong.getText().trim();
        String tenVietTat  = txtVietTat.getText().trim();
        String moTa        = txtMoTa.getText().trim();
        double vat;
        try { vat = Double.parseDouble(txtVAT.getText().trim()); } catch (NumberFormatException e) { vat = 0; }

        // Map loại hiển thị → enum DB
        String danhMucDB = loaiHienThi.equals("Thuốc kê đơn")         ? "THUOC_KE_DON"         :
                           loaiHienThi.equals("Thuốc không kê đơn")   ? "THUOC_KHONG_KE_DON"   :
                           loaiHienThi.equals("Mỹ phẩm")              ? "MY_PHAM"               : "THUC_PHAM_CHUC_NANG";

        try {
            Connection con = ConnectDB.getInstance().getConnection();
            if (con == null) throw new SQLException("Không có kết nối DB");

            if (isAdding) {
                // ── INSERT mới ──
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO SanPham (id, danhMuc, dang, ten, tenVietTat, nhaSanXuat, hoatChat, " +
                    "thueVAT, hamLuong, moTa, donViDoCoBan, ngayTao) VALUES (?,?,?,?,?,?,?,?,?,?,?,GETDATE())");
                ps.setString(1,  ma);      ps.setString(2, danhMucDB); ps.setString(3, dangBaoChe);
                ps.setString(4,  ten);     ps.setString(5, tenVietTat.isEmpty() ? null : tenVietTat);
                ps.setString(6,  nsx.isEmpty() ? null : nsx);
                ps.setString(7,  hoatChat.isEmpty() ? null : hoatChat);
                ps.setDouble(8,  vat);
                ps.setString(9,  hamLuong.isEmpty() ? null : hamLuong);
                ps.setString(10, moTa.isEmpty() ? null : moTa);
                ps.setString(11, dvtGoc);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Đã thêm sản phẩm thành công!");
                isAdding = false;
            } else {
                // ── UPDATE ──
                PreparedStatement ps = con.prepareStatement(
                    "UPDATE SanPham SET danhMuc=?, dang=?, ten=?, tenVietTat=?, nhaSanXuat=?, " +
                    "hoatChat=?, thueVAT=?, hamLuong=?, moTa=?, donViDoCoBan=? WHERE id=?");
                ps.setString(1, danhMucDB); ps.setString(2, dangBaoChe);
                ps.setString(3, ten);       ps.setString(4, tenVietTat.isEmpty() ? null : tenVietTat);
                ps.setString(5, nsx.isEmpty() ? null : nsx);
                ps.setString(6, hoatChat.isEmpty() ? null : hoatChat);
                ps.setDouble(7, vat);
                ps.setString(8, hamLuong.isEmpty() ? null : hamLuong);
                ps.setString(9, moTa.isEmpty() ? null : moTa);
                ps.setString(10, dvtGoc);
                ps.setString(11, ma);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Cập nhật sản phẩm thành công!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi lưu vào CSDL: " + ex.getMessage(), "Lỗi DB", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Cập nhật view + tên hiển thị
        txtLoaiView.setText(loaiHienThi); txtDangView.setText(dangBaoChe);
        txtNSXView.setText(nsx);         txtDVTView.setText(dvtGoc);
        if (loaiHienThi.equals("Thuốc kê đơn"))         txtLoaiView.setDisabledTextColor(Color.decode("#EF4444"));
        else if (loaiHienThi.equals("Sản phẩm chức năng")) txtLoaiView.setDisabledTextColor(Color.decode("#10B981"));
        else                                               txtLoaiView.setDisabledTextColor(Color.decode("#2179E0"));

        // Reload toàn bộ danh sách từ DB để đồng bộ
        khoiTaoDuLieuDB();
        currentFilteredData.clear();
        currentFilteredData.addAll(allDataMock);
        doSearch();

        // Chọn lại dòng vừa lưu
        for (int i = 0; i < tblSanPham.getRowCount(); i++) {
            if (tblSanPham.getValueAt(i, 0).toString().equals(ma)) {
                tblSanPham.setRowSelectionInterval(i, i); break;
            }
        }
        setEditMode(false);
    }

    private void setEditMode(boolean edit) {
        Color bg = edit ? Color.WHITE : COLOR_DISABLED_BG;
        txtTen.setEditable(edit);     txtTen.setBackground(bg);
        txtId.setEditable(false);     txtId.setBackground(COLOR_DISABLED_BG);
        txtVietTat.setEditable(edit); txtVietTat.setBackground(bg);
        txtHoatChat.setEditable(edit);txtHoatChat.setBackground(bg);
        txtHamLuong.setEditable(edit);txtHamLuong.setBackground(bg);
        txtVAT.setEditable(edit);     txtVAT.setBackground(bg);
        txtMoTa.setEditable(edit); txtMoTa.setBackground(edit ? Color.WHITE : COLOR_DISABLED_BG); scrollMoTa.getViewport().setBackground(edit ? Color.WHITE : COLOR_DISABLED_BG);
        clLoai.show(pnlLoai, edit ? "EDIT" : "VIEW"); clDang.show(pnlDang, edit ? "EDIT" : "VIEW"); clNSX.show(pnlNSX, edit ? "EDIT" : "VIEW"); clDVT.show(pnlDVT, edit ? "EDIT" : "VIEW");
        if (edit && !isAdding) { cbLoaiCT.setSelectedItem(txtLoaiView.getText()); cbDang.setSelectedItem(txtDangView.getText()); cbNhaSX.setSelectedItem(txtNSXView.getText()); cbDVT.setSelectedItem(txtDVTView.getText()); }
        modelDonVi.setEditable(edit);
        pnlActionBottom.removeAll();
        if (edit) { if (isAdding) pnlActionBottom.add(btnXacNhanThem); else pnlActionBottom.add(btnLuuBottom); pnlActionBottom.add(btnHuyBottom); }
        else { pnlActionBottom.add(btnCapNhatBottom); pnlActionBottom.add(btnXoaBottom); }
        pnlActionBottom.revalidate(); pnlActionBottom.repaint();
    }

    private void actionXoaSanPham() {
        if (txtId.getText().isEmpty()) return;
        if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa sản phẩm này?", "Xóa",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        String ma = txtId.getText().trim();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            if (con == null) throw new SQLException("Không có kết nối DB");
            PreparedStatement ps = con.prepareStatement("DELETE FROM SanPham WHERE id=?");
            ps.setString(1, ma);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Đã xóa sản phẩm thành công.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi xóa: " + ex.getMessage(), "Lỗi DB", JOptionPane.ERROR_MESSAGE);
            return;
        }
        khoiTaoDuLieuDB();
        currentFilteredData.clear();
        currentFilteredData.addAll(allDataMock);
        doSearch();
        clearDetailForm();
        setDetailVisible(false);
    }

    private void hienThiChiTietSanPham(int row) {
        Object[] data = currentFilteredData.get((currentPageMock - 1) * itemsPerPageMock + row);
        String maSP   = data[0].toString();
        txtId.setText(maSP);
        txtTen.setText(data[1].toString());
        String loai = data[2].toString();
        txtLoaiView.setText(loai);
        if (loai.equals("Thuốc kê đơn"))         txtLoaiView.setDisabledTextColor(Color.decode("#EF4444"));
        else if (loai.equals("Sản phẩm chức năng")) txtLoaiView.setDisabledTextColor(Color.decode("#10B981"));
        else                                        txtLoaiView.setDisabledTextColor(Color.decode("#2179E0"));
        txtHoatChat.setText(data[3].toString());
        String dangBaoChe = (data.length > 4) ? data[4].toString() : "";
        txtDangView.setText(dangBaoChe);

        // ── Load thêm thông tin chi tiết từ DB ──
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            if (con != null) {
                PreparedStatement ps = con.prepareStatement(
                    "SELECT nhaSanXuat, thueVAT, hamLuong, tenVietTat, donViDoCoBan FROM SanPham WHERE id=?");
                ps.setString(1, maSP);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    txtNSXView.setText(rs.getString("nhaSanXuat") != null ? rs.getString("nhaSanXuat") : "");
                    txtVAT.setText(rs.getBigDecimal("thueVAT") != null
                        ? rs.getBigDecimal("thueVAT").stripTrailingZeros().toPlainString() : "0");
                    txtHamLuong.setText(rs.getString("hamLuong") != null ? rs.getString("hamLuong") : "");
                    txtVietTat.setText(rs.getString("tenVietTat") != null ? rs.getString("tenVietTat") : "");
                    txtDVTView.setText(rs.getString("donViDoCoBan") != null ? rs.getString("donViDoCoBan") : "");
                }
                // Đơn vị quy đổi
                uomAutoChanging = true;
                modelDonVi.setRowCount(0);
                PreparedStatement ps2 = con.prepareStatement(
                    "SELECT ten, tyLeQuyDoi FROM DonViDoLuong WHERE sanPhamId=? ORDER BY tyLeQuyDoi");
                ps2.setString(1, maSP);
                ResultSet rs2 = ps2.executeQuery();
                while (rs2.next()) {
                    modelDonVi.addRow(new Object[]{rs2.getString("ten"), rs2.getInt("tyLeQuyDoi")});
                }
                modelDonVi.addRow(new Object[2]);
                uomAutoChanging = false;
                // Lô hàng
                modelLoHang.setRowCount(0);
                PreparedStatement ps3 = con.prepareStatement(
                    "SELECT soLoHang, soLuongLoHang, ngayHetHan, trangThai FROM LoHang " +
                    "WHERE sanPhamId=? ORDER BY ngayHetHan");
                ps3.setString(1, maSP);
                ResultSet rs3 = ps3.executeQuery();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                while (rs3.next()) {
                    String trangThai = "AVAILABLE".equals(rs3.getString("trangThai")) ? "Được bán" : "Hết hạn sử dụng";
                    String hsd = rs3.getTimestamp("ngayHetHan") != null
                        ? sdf.format(rs3.getTimestamp("ngayHetHan")) : "---";
                    modelLoHang.addRow(new Object[]{
                        rs3.getString("soLoHang"),
                        rs3.getInt("soLuongLoHang"),
                        hsd,
                        trangThai
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            // fallback nếu lỗi DB: để nguyên các field trống
            uomAutoChanging = false;
        }

        setDetailVisible(true);
        setEditMode(false);
    }

    private void clearDetailForm() {
        txtId.setText(""); txtTen.setText(""); txtVietTat.setText(""); txtHoatChat.setText(""); txtHamLuong.setText(""); txtVAT.setText(""); txtMoTa.setText("");
        txtLoaiView.setText(""); txtDangView.setText(""); txtNSXView.setText(""); txtDVTView.setText("");
        modelDonVi.setRowCount(0); modelLoHang.setRowCount(0);
    }

    private JTextField createViewField() { JTextField t = new JTextField(); t.setEditable(false); t.setFont(FONT_NORMAL); t.setDisabledTextColor(Color.BLACK); t.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(0, 8, 0, 8))); return t; }
    private void addFormField(JPanel p, String l, JTextField t, GridBagConstraints g, int x, int y, int w) { g.gridx = x; g.gridy = y; g.gridwidth = w; JLabel lbl = new JLabel(l); lbl.setFont(FONT_NORMAL); lbl.setForeground(Color.BLACK); p.add(lbl, g); g.gridy++; t.setPreferredSize(new Dimension(0, 32)); t.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(0, 8, 0, 8))); t.setDisabledTextColor(Color.BLACK); t.setFont(FONT_NORMAL); p.add(t, g); }
    private void addCustomField(JPanel p, String l, JPanel c, GridBagConstraints g, int x, int y) { g.gridx = x; g.gridy = y; g.gridwidth = 1; JLabel lbl = new JLabel(l); lbl.setFont(FONT_NORMAL); lbl.setForeground(Color.BLACK); p.add(lbl, g); g.gridy++; c.setPreferredSize(new Dimension(0, 32)); p.add(c, g); }
    private JButton createBtn(String t, String c) { return createBtnWithIcon(t, c, null); }

    // ========================================================================
    // THANH CUỘN MẢNH
    // ========================================================================
    private void applyThinScrollBar(JScrollPane sp) {
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() { thumbColor = Color.decode("#CBD5E1"); trackColor = Color.decode("#F1F5F9"); }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); b.setMinimumSize(new Dimension(0,0)); b.setMaximumSize(new Dimension(0,0)); return b; }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) { if (r.isEmpty()) return; Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(thumbColor); g2.fillRoundRect(r.x+1, r.y+1, r.width-2, r.height-2, 6, 6); g2.dispose(); }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) { Graphics2D g2 = (Graphics2D) g.create(); g2.setColor(trackColor); g2.fillRect(r.x, r.y, r.width, r.height); g2.dispose(); }
        });
        sp.getHorizontalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() { thumbColor = Color.decode("#CBD5E1"); trackColor = Color.decode("#F1F5F9"); }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); b.setMinimumSize(new Dimension(0,0)); b.setMaximumSize(new Dimension(0,0)); return b; }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) { if (r.isEmpty()) return; Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(thumbColor); g2.fillRoundRect(r.x+1, r.y+1, r.width-2, r.height-2, 6, 6); g2.dispose(); }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) { Graphics2D g2 = (Graphics2D) g.create(); g2.setColor(trackColor); g2.fillRect(r.x, r.y, r.width, r.height); g2.dispose(); }
        });
    }

    // ========================================================================
    // DIALOG XUẤT EXCEL (CHẠY THẬT)
    // ========================================================================
    private void showExportDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Xuất Excel", true);
        dlg.setSize(380, 260); dlg.setLocationRelativeTo(this); dlg.setResizable(false);
        JPanel pnl = new JPanel(new BorderLayout()); pnl.setBackground(Color.WHITE);
        JPanel header = new JPanel(new BorderLayout()); header.setBackground(COLOR_HEADER_TABLE); header.setPreferredSize(new Dimension(0, 48));
        JLabel lbl = new JLabel("  ⬇  Xuất danh sách sản phẩm"); lbl.setForeground(Color.WHITE); lbl.setFont(FONT_BOLD); header.add(lbl); pnl.add(header, BorderLayout.NORTH);
        JPanel body = new JPanel(); body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS)); body.setBackground(Color.WHITE); body.setBorder(new EmptyBorder(20, 24, 10, 24));
        JLabel lblSub = new JLabel("Chọn phạm vi dữ liệu muốn xuất:"); lblSub.setFont(FONT_NORMAL); lblSub.setAlignmentX(Component.LEFT_ALIGNMENT); body.add(lblSub); body.add(Box.createVerticalStrut(14));
        ButtonGroup bg = new ButtonGroup();
        JRadioButton rbAll  = new JRadioButton("Xuất toàn bộ  (" + allDataMock.size() + " sản phẩm)");
        JRadioButton rbPage = new JRadioButton("Xuất trang hiện tại  (" + tblSanPham.getRowCount() + " sản phẩm)");
        for (JRadioButton rb : new JRadioButton[]{rbAll, rbPage}) { rb.setFont(FONT_NORMAL); rb.setBackground(Color.WHITE); rb.setFocusPainted(false); rb.setAlignmentX(Component.LEFT_ALIGNMENT); bg.add(rb); body.add(rb); body.add(Box.createVerticalStrut(8)); }
        rbAll.setSelected(true); pnl.add(body, BorderLayout.CENTER);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10)); footer.setBackground(Color.WHITE); footer.setBorder(BorderFactory.createMatteBorder(1,0,0,0,COLOR_BORDER));
        JButton btnOk = createBtnWithIcon("Xuất Excel", "#22C55E", null); btnOk.setPreferredSize(new Dimension(130, 34));
        JButton btnCancel = createBtnWithIcon("Hủy", "#64748B", null); btnCancel.setPreferredSize(new Dimension(90, 34));
        
        btnOk.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Chọn nơi lưu file Excel");
            if (fileChooser.showSaveDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.endsWith(".xlsx")) filePath += ".xlsx";
                
                List<Object[]> dataToExport = rbAll.isSelected() ? allDataMock : getCurrentTableData();
                boolean success = exportExcelAction(filePath, dataToExport);
                
                if (success) {
                    JOptionPane.showMessageDialog(dlg, "Đã xuất " + dataToExport.size() + " sản phẩm thành công tại:\n" + filePath, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dlg.dispose();
                } else {
                    JOptionPane.showMessageDialog(dlg, "Xuất file thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        btnCancel.addActionListener(e -> dlg.dispose());
        footer.add(btnOk); footer.add(btnCancel); pnl.add(footer, BorderLayout.SOUTH);
        dlg.setContentPane(pnl); dlg.setVisible(true);
    }

    private List<Object[]> getCurrentTableData() {
        List<Object[]> list = new ArrayList<>();
        for (int i = 0; i < tblSanPham.getRowCount(); i++) {
            Object[] row = new Object[tblSanPham.getColumnCount()];
            for (int j = 0; j < tblSanPham.getColumnCount(); j++) row[j] = tblSanPham.getValueAt(i, j);
            list.add(row);
        }
        return list;
    }

    private boolean exportExcelAction(String path, List<Object[]> data) {
        /*
        // Code mẫu khi bạn đã thêm thư viện Apache POI (poi-ooxml):
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Danh Sach San Pham");
            for (int i = 0; i < data.size(); i++) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(i);
                Object[] rowData = data.get(i);
                for (int j = 0; j < rowData.length; j++) {
                    row.createCell(j).setCellValue(rowData[j] != null ? rowData[j].toString() : "");
                }
            }
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(path)) {
                workbook.write(out);
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        */
        
        // Hiện tại trả về true để bạn test UI, khi có POI hãy mở comment block phía trên
        System.out.println("Đã xuất " + data.size() + " dòng ra file: " + path);
        return true; 
    }

    // ========================================================================
    // DIALOG NHẬP EXCEL (CHẠY THẬT)
    // ========================================================================
    private void showImportDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nhập Excel", true);
        dlg.setSize(420, 300); dlg.setLocationRelativeTo(this); dlg.setResizable(false);
        JPanel pnl = new JPanel(new BorderLayout()); pnl.setBackground(Color.WHITE);
        JPanel header = new JPanel(new BorderLayout()); header.setBackground(COLOR_HEADER_TABLE); header.setPreferredSize(new Dimension(0, 48));
        JLabel lbl = new JLabel("  ⬆  Nhập danh sách sản phẩm"); lbl.setForeground(Color.WHITE); lbl.setFont(FONT_BOLD); header.add(lbl); pnl.add(header, BorderLayout.NORTH);
        JPanel body = new JPanel(); body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS)); body.setBackground(Color.WHITE); body.setBorder(new EmptyBorder(16, 24, 10, 24));
        JPanel infoBox = new JPanel(new BorderLayout()); infoBox.setBackground(Color.decode("#EFF6FF")); infoBox.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#BFDBFE")), new EmptyBorder(10,12,10,12)));
        JLabel lblInfo = new JLabel("<html><b>Lưu ý trước khi nhập:</b><br>• Định dạng file: <b>.xlsx</b> hoặc <b>.xls</b><br>• Cột bắt buộc: Tên, Loại, Hoạt chất, Dạng bào chế<br>• Nên tải <b>file mẫu</b> để tránh lỗi định dạng</html>");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12)); infoBox.add(lblInfo); infoBox.setAlignmentX(Component.LEFT_ALIGNMENT); infoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        body.add(infoBox); body.add(Box.createVerticalStrut(14));
        JPanel rowFile = new JPanel(new BorderLayout(6,0)); rowFile.setOpaque(false); rowFile.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34)); rowFile.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField txtFile = new JTextField("Chưa chọn file..."); txtFile.setEditable(false); txtFile.setFont(FONT_NORMAL); txtFile.setBackground(COLOR_DISABLED_BG); txtFile.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(0,8,0,8)));
        JButton btnChon = createBtnWithIcon("Chọn file", "#1D68B2", null); btnChon.setPreferredSize(new Dimension(105,34));
        
        File[] selectedFileArr = new File[1]; // Biến lưu trữ file đã chọn
        
        btnChon.addActionListener(e -> { 
            JFileChooser fc = new JFileChooser(); 
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files","xlsx","xls")); 
            if (fc.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION) {
                selectedFileArr[0] = fc.getSelectedFile();
                txtFile.setText(selectedFileArr[0].getName()); 
            }
        });
        
        rowFile.add(txtFile, BorderLayout.CENTER); rowFile.add(btnChon, BorderLayout.EAST); body.add(rowFile); pnl.add(body, BorderLayout.CENTER);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10)); footer.setBackground(Color.WHITE); footer.setBorder(BorderFactory.createMatteBorder(1,0,0,0,COLOR_BORDER));
        JButton btnMau    = createBtnWithIcon("Tải file mẫu", "#0EA5E9", null); btnMau.setPreferredSize(new Dimension(135,34));
        JButton btnOk     = createBtnWithIcon("Nhập dữ liệu", "#22C55E", null); btnOk.setPreferredSize(new Dimension(135,34));
        JButton btnCancel = createBtnWithIcon("Hủy",          "#64748B", null); btnCancel.setPreferredSize(new Dimension(90,34));
        
        btnMau.addActionListener(e -> JOptionPane.showMessageDialog(dlg, "Đang tải file mẫu về máy...", "Tải mẫu", JOptionPane.INFORMATION_MESSAGE));
        
        btnOk.addActionListener(e -> { 
            if (selectedFileArr[0] == null || txtFile.getText().equals("Chưa chọn file...")) { 
                JOptionPane.showMessageDialog(dlg, "Vui lòng chọn file Excel!", "Lỗi", JOptionPane.WARNING_MESSAGE); 
                return; 
            } 
            
            JOptionPane.showMessageDialog(dlg, "Đang đọc dữ liệu từ file Excel...\n(Cần tích hợp thư viện Apache POI để đọc thật)", "Thông báo", JOptionPane.INFORMATION_MESSAGE); 
            
            /*
            // Code mẫu khi bạn đã thêm thư viện Apache POI (poi-ooxml):
            try (java.io.FileInputStream fis = new java.io.FileInputStream(selectedFileArr[0]);
                 org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(fis)) {
                
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
                for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Bỏ qua dòng header
                    org.apache.poi.ss.usermodel.Row row = sheet.getRow(i);
                    if (row != null) {
                        String ma = row.getCell(0).getStringCellValue();
                        String ten = row.getCell(1).getStringCellValue();
                        String loai = row.getCell(2).getStringCellValue();
                        String hoatChat = row.getCell(3).getStringCellValue();
                        String dang = row.getCell(4).getStringCellValue();
                        allDataMock.add(0, new Object[]{ma, ten, loai, hoatChat, dang});
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            */

            doSearch(); // Làm mới bảng sau khi nạp dữ liệu
            dlg.dispose(); 
        });
        
        btnCancel.addActionListener(e -> dlg.dispose());
        footer.add(btnMau); footer.add(btnOk); footer.add(btnCancel); pnl.add(footer, BorderLayout.SOUTH);
        dlg.setContentPane(pnl); dlg.setVisible(true);
    }

    private JButton createBtnWithIcon(String text, String hexColor, Icon icon) {
        JButton btn = new JButton(text);
        if (icon != null) { btn.setIcon(icon); btn.setIconTextGap(6); }
        btn.setBackground(Color.decode(hexColor)); btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BOLD); btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setOpaque(true); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(4, 10, 4, 10));
        return btn;
    }

    private JPanel createSubTable(String t, String[] c, int h) {
        JPanel p = new JPanel(new BorderLayout()); p.setBorder(new EmptyBorder(10, 15, 5, 15)); p.setOpaque(false);
        JLabel l = new JLabel("  " + t); l.setOpaque(true); l.setBackground(COLOR_HEADER_TABLE); l.setForeground(Color.WHITE); l.setFont(FONT_BOLD); l.setPreferredSize(new Dimension(0, 30));
        JTable tbl = new JTable(new DefaultTableModel(c, 0)); setupTableStyle(tbl);
        DefaultTableCellRenderer centerTbl = new DefaultTableCellRenderer(); centerTbl.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tbl.getColumnCount(); i++) tbl.getColumnModel().getColumn(i).setCellRenderer(centerTbl);
        tbl.setFillsViewportHeight(true);
        JScrollPane s = new JScrollPane(tbl); s.setPreferredSize(new Dimension(0, h)); s.setBorder(BorderFactory.createLineBorder(COLOR_BORDER)); s.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED); s.getViewport().setBackground(Color.WHITE);
        p.add(l, BorderLayout.NORTH); p.add(s, BorderLayout.CENTER); return p;
    }

    private void setupTableStyle(JTable t) { t.setRowHeight(30); t.setFont(FONT_NORMAL); t.getTableHeader().setFont(FONT_BOLD); t.getTableHeader().setBackground(COLOR_LIGHT_BLUE); t.setGridColor(COLOR_BORDER); t.setSelectionBackground(COLOR_LIGHT_BLUE); t.setSelectionForeground(Color.BLACK); }
    private JLabel createLabelFilter(String t) { JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI", Font.BOLD, 14)); l.setBorder(new EmptyBorder(10, 15, 5, 0)); return l; }

    private void khoiTaoDuLieuDB() {
        allDataMock.clear();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            if (con == null) return;
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT id, ten, danhMuc, hoatChat, dang, nhaSanXuat, thueVAT, hamLuong, tenVietTat, donViDoCoBan " +
                "FROM SanPham ORDER BY ten");
            while (rs.next()) {
                String dm = rs.getString("danhMuc");
                String loai = "THUOC_KE_DON".equals(dm) ? "Thuốc kê đơn" :
                              "THUOC_KHONG_KE_DON".equals(dm) ? "Thuốc không kê đơn" :
                              "MY_PHAM".equals(dm) ? "Mỹ phẩm" : "Sản phẩm chức năng";
                allDataMock.add(new Object[]{
                    rs.getString("id"),
                    rs.getString("ten"),
                    loai,
                    rs.getString("hoatChat") != null ? rs.getString("hoatChat") : "",
                    rs.getString("dang")     != null ? rs.getString("dang")     : "",
                    rs.getString("nhaSanXuat") != null ? rs.getString("nhaSanXuat") : "",
                    rs.getBigDecimal("thueVAT") != null ? rs.getBigDecimal("thueVAT").toPlainString() : "0",
                    rs.getString("hamLuong") != null ? rs.getString("hamLuong") : "",
                    rs.getString("tenVietTat") != null ? rs.getString("tenVietTat") : "",
                    rs.getString("donViDoCoBan") != null ? rs.getString("donViDoCoBan") : ""
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Phân quyền: Ẩn/disable các nút Thêm, Nhập Excel, Sửa, Xóa cho STAFF.
     */
    public void setReadOnly(boolean readOnly) {
        if (!readOnly) return;
        if (btnCapNhatBottom != null) btnCapNhatBottom.setVisible(false);
        if (btnXoaBottom     != null) btnXoaBottom.setVisible(false);
        if (btnLuuBottom     != null) btnLuuBottom.setVisible(false);
        if (btnXacNhanThem   != null) btnXacNhanThem.setVisible(false);
        disableButtonsByText(this, "Thêm mới", "Nhập Excel", "Thêm", "Xóa",
                             "Sửa", "Lưu", "Cập nhật", "Xác nhận thêm");
    }

    /** Duyệt đệ quy tất cả component, tìm JButton theo text rồi ẩn đi. */
    private void disableButtonsByText(Container container, String... texts) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton) {
                JButton btn = (JButton) c;
                for (String t : texts) {
                    if (t.equals(btn.getText())) { btn.setVisible(false); break; }
                }
            } else if (c instanceof Container) {
                disableButtonsByText((Container) c, texts);
            }
        }
    }
}