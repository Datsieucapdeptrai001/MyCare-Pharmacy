package GUI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import Entity.LoHang;
import Utils.MenuIcon;

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
	private Component btnThem;
	private Component btnXoa;
	private Component btnSua;
	private Component btnLuu;

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

        pnlRightDetail = createRightDetail();

        add(pnlBody, BorderLayout.CENTER);

        loadDataFromDatabase();
        currentFilteredData.addAll(allDataMock);
        setDetailVisible(false);
    }

    // ========================================================================
    // BỘ CHUYỂN ĐỔI ENUM <-> TEXT
    // ========================================================================
    private String mapToDbDanhMuc(String uiText) {
        if (uiText.contains("kê đơn") && !uiText.contains("không")) return "THUOC_KE_DON";
        if (uiText.contains("chức năng")) return "THUC_PHAM_CHUC_NANG";
        if (uiText.contains("Mỹ phẩm")) return "MY_PHAM";
        return "THUOC_KHONG_KE_DON";
    }

    private String mapToDbDang(String uiText) {
        if (uiText.contains("Dung dịch") || uiText.contains("Si rô") || uiText.contains("nhỏ giọt") || uiText.contains("Súc miệng")) return "DANG_LONG";
        return "DANG_RAN";
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

    private static class ToggleEditableTableModel extends DefaultTableModel {
        private boolean editable = true;
        private final BitSet readOnlyCols = new BitSet();
        ToggleEditableTableModel(String[] cols, int rows) { super(cols, rows); }
        void setEditable(boolean e) { if (this.editable != e) { this.editable = e; fireTableDataChanged(); } }
        void setReadOnlyColumns(int... cols) { readOnlyCols.clear(); if (cols != null) for (int c : cols) if (c >= 0) readOnlyCols.set(c); fireTableDataChanged(); }
        @Override public boolean isCellEditable(int r, int c) { return editable && !readOnlyCols.get(c); }
    }

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

    private JPanel createTopbar() {
        JPanel pnl = new JPanel(); pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS)); pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER), new EmptyBorder(15, 20, 15, 20)));

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); pnlLeft.setOpaque(false);
        JLabel lblBoxIcon = new JLabel(new MenuIcon("PACKAGE")); lblBoxIcon.setForeground(COLOR_PRIMARY);
        pnlLeft.add(lblBoxIcon);
        pnlLeft.add(new JLabel("<html><b style='color:#1E3A8A; font-size:16px;'>QUẢN LÝ SẢN PHẨM</b></html>"));
        pnlLeft.add(new JLabel("(42 sản phẩm)"));

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); pnlRight.setOpaque(false);
        pnlRight.add(new JLabel("Tìm kiếm: "));
        txtTimKiem = new WatermarkTextField("Tên, mã, hoạt chất..."); txtTimKiem.setPreferredSize(new Dimension(180, 36)); pnlRight.add(txtTimKiem);
        
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { doSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { doSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { doSearch(); }
        });

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

    private JPanel createCenterList() {
        JPanel pnl = new JPanel(new BorderLayout()); pnl.setBackground(Color.WHITE); pnl.setBorder(null);
        JLabel lblHeader = new JLabel("  Danh sách sản phẩm"); lblHeader.setOpaque(true); lblHeader.setBackground(COLOR_HEADER_TABLE); lblHeader.setForeground(Color.WHITE); lblHeader.setFont(FONT_BOLD); lblHeader.setPreferredSize(new Dimension(0, 38)); pnl.add(lblHeader, BorderLayout.NORTH);

        modelSanPham = new DefaultTableModel(COLS_EXPANDED, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tblSanPham = new JTable(modelSanPham); setupTableStyle(tblSanPham);
        tblSanPham.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
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
                String nsx = row.length > 5 ? row[5].toString() : "DHG Pharma";
                String vat = row.length > 6 ? row[6].toString() : "10%";
                modelSanPham.addRow(new Object[]{row[0], row[1], row[2], row[3], dang, nsx, vat});
            }
        }
    }

    private JPanel createRightDetail() {
        JPanel pnl = new JPanel(new BorderLayout()); pnl.setPreferredSize(new Dimension(450, 0)); pnl.setBackground(Color.WHITE); pnl.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, COLOR_BORDER));
        
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

        JPanel pnlScrollContent = new JPanel(); pnlScrollContent.setLayout(new BoxLayout(pnlScrollContent, BoxLayout.Y_AXIS)); pnlScrollContent.setBackground(Color.WHITE);
        JPanel pnlForm = new JPanel(new GridBagLayout()); pnlForm.setBackground(Color.WHITE); pnlForm.setBorder(new EmptyBorder(10, 15, 0, 15));
        GridBagConstraints g = new GridBagConstraints(); g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(3, 5, 3, 5); g.weightx = 1.0;

        addFormField(pnlForm, "Tên sản phẩm *", txtTen     = new JTextField(), g, 0, 0, 2);
        addFormField(pnlForm, "Mã sản phẩm",    txtId      = new JTextField(), g, 0, 2, 1);
        addFormField(pnlForm, "Tên viết tắt",   txtVietTat = new JTextField(), g, 1, 2, 1);

        cbLoaiCT = new JComboBox<>(new String[]{"Thuốc không kê đơn", "Thuốc kê đơn", "Sản phẩm chức năng", "Mỹ phẩm"}); applyFlatComboBoxStyle(cbLoaiCT);
        cbDang   = new JComboBox<>(new String[]{"Viên nén", "Viên nang", "Dung dịch", "Si rô", "Viên sủi", "Thuốc bột", "Kẹo ngậm", "Thuốc nhỏ giọt", "Súc miệng"}); applyFlatComboBoxStyle(cbDang);
        cbNhaSX  = new JComboBox<>(new String[]{"DHG Pharma", "Traphaco", "Sanofi", "Domesco"}); applyFlatComboBoxStyle(cbNhaSX);
        cbDVT    = new JComboBox<>(new String[]{"Viên", "Chai", "Hộp", "Vỉ", "Ống"}); applyFlatComboBoxStyle(cbDVT);

        pnlLoai = new JPanel(clLoai = new CardLayout()); txtLoaiView = createViewField(); pnlLoai.add(txtLoaiView, "VIEW"); pnlLoai.add(cbLoaiCT, "EDIT");
        pnlDang = new JPanel(clDang = new CardLayout()); txtDangView = createViewField(); pnlDang.add(txtDangView, "VIEW"); pnlDang.add(cbDang,   "EDIT");
        pnlNSX  = new JPanel(clNSX  = new CardLayout()); txtNSXView  = createViewField(); pnlNSX.add(txtNSXView,  "VIEW"); pnlNSX.add(cbNhaSX, "EDIT");
        pnlDVT  = new JPanel(clDVT  = new CardLayout()); txtDVTView  = createViewField(); pnlDVT.add(txtDVTView,  "VIEW"); pnlDVT.add(cbDVT,   "EDIT");

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

    private void batDauThemMoi() {
        isAdding = true;
        txtTen.setText(""); txtVietTat.setText(""); txtHoatChat.setText(""); txtHamLuong.setText(""); txtVAT.setText("10"); txtMoTa.setText("");
        txtLoaiView.setText(""); txtDangView.setText(""); txtNSXView.setText(""); txtDVTView.setText("");
        cbLoaiCT.setSelectedIndex(0); cbDang.setSelectedIndex(0); cbNhaSX.setSelectedIndex(0); cbDVT.setSelectedIndex(0);
        
        BUS.BUS_SanPham bus = new BUS.BUS_SanPham();
        txtId.setText(bus.taoMaMoi());
        
        uomAutoChanging = true; modelDonVi.setRowCount(0); modelDonVi.addRow(new Object[2]); uomAutoChanging = false;
        modelLoHang.setRowCount(0);
        tblSanPham.clearSelection();
        setDetailVisible(true); 
        setEditMode(true);
    }

    private void huyThaoTac() {
        isAdding = false;
        setDetailVisible(false); 
        clearDetailForm();
        tblSanPham.clearSelection();
    }

    private void thucHienLuu() {
        String ma = txtId.getText(); 
        String ten = txtTen.getText().trim();
        if (ten.isEmpty()) { 
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên sản phẩm!", "Thông báo", JOptionPane.WARNING_MESSAGE); 
            return; 
        }
        
        String loai = cbLoaiCT.getSelectedItem().toString();
        String dangBaoChe = cbDang.getSelectedItem().toString();
        String hoatChat = txtHoatChat.getText().trim();
        String vietTat = txtVietTat.getText().trim();
        String nsx = cbNhaSX.getSelectedItem().toString();
        String dvt = cbDVT.getSelectedItem().toString();
        double vat = Double.parseDouble(txtVAT.getText().trim().isEmpty() ? "0" : txtVAT.getText().trim());
        String hamLuong = txtHamLuong.getText().trim();
        String moTa = txtMoTa.getText().trim();

        txtLoaiView.setText(loai); txtDangView.setText(dangBaoChe); txtNSXView.setText(nsx); txtDVTView.setText(dvt);
        if (loai.equals("Thuốc kê đơn")) txtLoaiView.setDisabledTextColor(Color.decode("#EF4444")); else if (loai.equals("Sản phẩm chức năng")) txtLoaiView.setDisabledTextColor(Color.decode("#10B981")); else txtLoaiView.setDisabledTextColor(Color.decode("#2179E0"));

        String dbDanhMuc = mapToDbDanhMuc(loai);
        String dbDang = mapToDbDang(dangBaoChe);

        BUS.BUS_SanPham bus = new BUS.BUS_SanPham();
        boolean success = false;

        if (isAdding) {
            success = bus.themSP(ma, dbDanhMuc, dbDang, ten, vietTat, nsx, hoatChat, vat, hamLuong, moTa, dvt);
            if(success) JOptionPane.showMessageDialog(this, "Đã thêm sản phẩm vào Database thành công!");
        } else {
            success = bus.capNhatSP(ma, dbDanhMuc, dbDang, ten, vietTat, nsx, hoatChat, vat, hamLuong, moTa, dvt);
            if(success) JOptionPane.showMessageDialog(this, "Cập nhật Database thành công!");
        }

        if(success) {
            loadDataFromDatabase(); 
            for (int i = 0; i < tblSanPham.getRowCount(); i++) { 
                if (tblSanPham.getValueAt(i, 0).toString().equals(ma)) { tblSanPham.setRowSelectionInterval(i, i); break; } 
            }
            setEditMode(false);
            isAdding = false;
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
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
        if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa khỏi CSDL?", "Xóa", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            BUS.BUS_SanPham bus = new BUS.BUS_SanPham();
            if(bus.xoaSP(txtId.getText())) {
                JOptionPane.showMessageDialog(this, "Đã xóa vĩnh viễn!");
                loadDataFromDatabase();
                clearDetailForm();
                setDetailVisible(false);
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại do lỗi CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void hienThiChiTietSanPham(int row) {
        Object[] data = currentFilteredData.get((currentPageMock - 1) * itemsPerPageMock + row);
        String maSP = data[0].toString();
        txtId.setText(maSP); txtTen.setText(data[1].toString());
        String loai = data[2].toString();
        txtLoaiView.setText(loai);
        if (loai.equals("Thuốc kê đơn")) txtLoaiView.setDisabledTextColor(Color.decode("#EF4444")); else if (loai.equals("Sản phẩm chức năng")) txtLoaiView.setDisabledTextColor(Color.decode("#10B981")); else txtLoaiView.setDisabledTextColor(Color.decode("#2179E0"));
        txtHoatChat.setText(data[3].toString());
        String dangBaoChe = (data.length > 4) ? data[4].toString() : "Viên nén";
        txtDangView.setText(dangBaoChe);
        txtVietTat.setText(txtTen.getText().split(" ")[0]);
        txtHamLuong.setText(""); txtVAT.setText(data.length > 6 ? data[6].toString().replace("%", "") : "10"); txtMoTa.setText(""); txtNSXView.setText(data.length > 5 ? data[5].toString() : "DHG Pharma");
        String dvtGoc = dangBaoChe.contains("Dung dịch") || dangBaoChe.contains("Si rô") ? "Chai" : "Viên";
        txtDVTView.setText(dvtGoc);
        uomAutoChanging = true; modelDonVi.setRowCount(0);
        if (dvtGoc.equals("Viên")) { modelDonVi.addRow(new Object[]{"Vỉ", "10"}); modelDonVi.addRow(new Object[]{"Hộp", "100"}); } else if (dvtGoc.equals("Chai")) { modelDonVi.addRow(new Object[]{"Thùng", "24"}); }
        modelDonVi.addRow(new Object[2]); uomAutoChanging = false;
        
        BUS.BUS_SanPham bus = new BUS.BUS_SanPham();
        List<LoHang> listLo = bus.layLoTheoSP(maSP);
        modelLoHang.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if(listLo != null) {
            for(LoHang lh : listLo) {
                String tTrang = "Được bán";
                if(lh.getNgayHetHan() != null && lh.getNgayHetHan().toLocalDate().isBefore(LocalDate.now())) tTrang = "Hết hạn sử dụng";
                String hsdStr = lh.getNgayHetHan() != null ? lh.getNgayHetHan().toLocalDate().format(fmt) : "N/A";
                modelLoHang.addRow(new Object[]{lh.getSoLoHang(), lh.getSoLuongLoHang(), hsdStr, tTrang});
            }
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
    // DIALOG XUẤT EXCEL (FIX ĐỊNH DẠNG ĐÚNG NHƯ ẢNH)
    // ========================================================================
 // ========================================================================
    // DIALOG XUẤT EXCEL CÓ HEADER CHUẨN ĐẸP 12 CỘT DATABASE NHƯ ẢNH
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
                
                // LẤY DỮ LIỆU TRỰC TIẾP TỪ DATABASE (Đầy đủ 12 cột)
                DAO.DAO_SanPham dao = new DAO.DAO_SanPham();
                List<Entity.SanPham> fullData = dao.getDsThuoc();
                List<Entity.SanPham> dataToExport = new ArrayList<>();
                
                if (rbAll.isSelected()) {
                    dataToExport.addAll(fullData);
                } else {
                    // Lọc ra các ID đang hiển thị trên bảng hiện tại
                    Set<String> visibleIds = new HashSet<>();
                    for (int i = 0; i < tblSanPham.getRowCount(); i++) {
                        visibleIds.add(tblSanPham.getValueAt(i, 0).toString());
                    }
                    for (Entity.SanPham sp : fullData) {
                        if (visibleIds.contains(sp.getId())) {
                            dataToExport.add(sp);
                        }
                    }
                }
                
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

    private boolean exportExcelAction(String path, List<Entity.SanPham> data) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh Sach San Pham");
            
            // 1. TẠO DÒNG TIÊU ĐỀ (HEADER) KHỚP 100% VỚI TÊN CỘT DATABASE NHƯ ẢNH
            Row headerRow = sheet.createRow(0);
            String[] headers = {"id", "danhMuc", "dang", "ten", "tenVietTat", "nhaSanXuat", "hoatChat", "thueVAT", "hamLuong", "moTa", "donViDoCoBan", "ngayTao"};
            
            // Style Header (Nền xám nhạt, viền dưới mỏng giống hệt SQL Server Export)
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            
            for(int i=0; i<headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // 2. GHI DỮ LIỆU ĐÚNG CHUẨN THỰC TẾ
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 1); // Bắt đầu từ dòng số 1 do vướng Header
                Entity.SanPham sp = data.get(i);
                
                row.createCell(0).setCellValue(sp.getId() != null ? sp.getId() : "");
                row.createCell(1).setCellValue(sp.getDanhMuc() != null ? sp.getDanhMuc().name() : "");
                row.createCell(2).setCellValue(sp.getDang() != null ? sp.getDang().name() : "");
                row.createCell(3).setCellValue(sp.getTen() != null ? sp.getTen() : "");
                row.createCell(4).setCellValue(sp.getTenVietTat() != null ? sp.getTenVietTat() : "");
                row.createCell(5).setCellValue(sp.getNhaSanXuat() != null ? sp.getNhaSanXuat() : "");
                row.createCell(6).setCellValue(sp.getHoatChat() != null ? sp.getHoatChat() : "");
                
                // Thuế VAT lấy định dạng 2 số thập phân (VD: 8.00, 5.00, 10.00) giống trong ảnh
                row.createCell(7).setCellValue(Double.parseDouble(String.format(java.util.Locale.US, "%.2f", sp.getThueVAT())));
                
                // Xử lý hiện chữ "NULL" nếu rỗng giống hệt ảnh
                row.createCell(8).setCellValue((sp.getHamLuong() != null && !sp.getHamLuong().isEmpty()) ? sp.getHamLuong() : "NULL");
                row.createCell(9).setCellValue((sp.getMoTa() != null && !sp.getMoTa().isEmpty()) ? sp.getMoTa() : "NULL");
                
                row.createCell(10).setCellValue(sp.getDonViDoCoBan() != null ? sp.getDonViDoCoBan() : "");
                row.createCell(11).setCellValue(sp.getNgayTao() != null ? sp.getNgayTao().format(dtf) : "");
            }
            
            // Căn chỉnh chiều rộng tự động cho 12 cột
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (java.io.FileOutputStream out = new java.io.FileOutputStream(path)) {
                workbook.write(out);
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ========================================================================
    // DIALOG NHẬP EXCEL (KIỂM TRA TRÙNG LẶP CHỐNG LỖI)
    // ========================================================================
    private void showImportDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nhập Excel", true);
        dlg.setSize(420, 300); dlg.setLocationRelativeTo(this); dlg.setResizable(false);
        JPanel pnl = new JPanel(new BorderLayout()); pnl.setBackground(Color.WHITE);
        JPanel header = new JPanel(new BorderLayout()); header.setBackground(COLOR_HEADER_TABLE); header.setPreferredSize(new Dimension(0, 48));
        JLabel lbl = new JLabel("  ⬆  Nhập danh sách sản phẩm"); lbl.setForeground(Color.WHITE); lbl.setFont(FONT_BOLD); header.add(lbl); pnl.add(header, BorderLayout.NORTH);
        JPanel body = new JPanel(); body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS)); body.setBackground(Color.WHITE); body.setBorder(new EmptyBorder(16, 24, 10, 24));
        JPanel infoBox = new JPanel(new BorderLayout()); infoBox.setBackground(Color.decode("#EFF6FF")); infoBox.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#BFDBFE")), new EmptyBorder(10,12,10,12)));
        JLabel lblInfo = new JLabel("<html><b>Lưu ý trước khi nhập:</b><br>• Hệ thống sẽ tự cập nhật nếu trùng TÊN sản phẩm.<br>• Cột bắt buộc: Tên, Loại, Hoạt chất, Dạng bào chế<br>• Nên tải <b>file mẫu</b> để tránh lỗi định dạng</html>");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12)); infoBox.add(lblInfo); infoBox.setAlignmentX(Component.LEFT_ALIGNMENT); infoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        body.add(infoBox); body.add(Box.createVerticalStrut(14));
        JPanel rowFile = new JPanel(new BorderLayout(6,0)); rowFile.setOpaque(false); rowFile.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34)); rowFile.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField txtFile = new JTextField("Chưa chọn file..."); txtFile.setEditable(false); txtFile.setFont(FONT_NORMAL); txtFile.setBackground(COLOR_DISABLED_BG); txtFile.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(0,8,0,8)));
        JButton btnChon = createBtnWithIcon("Chọn file", "#1D68B2", null); btnChon.setPreferredSize(new Dimension(105,34));
        
        File[] selectedFileArr = new File[1]; 
        
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
            
            // Hiện con trỏ chuột loading
            dlg.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            try (java.io.FileInputStream fis = new java.io.FileInputStream(selectedFileArr[0]);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                
                Sheet sheet = workbook.getSheetAt(0);
                DataFormatter formatter = new DataFormatter(); 
                BUS.BUS_SanPham bus = new BUS.BUS_SanPham();
                DAO.DAO_SanPham dao = new DAO.DAO_SanPham(); // Gọi DAO lấy DB
                
                int successCount = 0;
                int updateCount = 0;
                List<Integer> errorRows = new ArrayList<>();
                
                // Lấy toàn bộ danh sách để check trùng lặp (Upsert)
                List<Entity.SanPham> dsHienTai = dao.getDsThuoc();
                
                for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Bỏ qua dòng header (0)
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        String ten = formatter.formatCellValue(row.getCell(1)).trim();
                        if(ten.isEmpty()) continue; // Bỏ qua dòng trống

                        try {
                            String loai = formatter.formatCellValue(row.getCell(2));
                            String hoatChat = formatter.formatCellValue(row.getCell(3));
                            String dang = formatter.formatCellValue(row.getCell(4));
                            String nsx = formatter.formatCellValue(row.getCell(5));
                            
                            // Đọc VAT, mặc định 10% nếu bỏ trống
                            double vat = 10.0;
                            try {
                                String vatStr = formatter.formatCellValue(row.getCell(6)).replace("%", "").trim();
                                if(!vatStr.isEmpty()) vat = Double.parseDouble(vatStr);
                            } catch(Exception ignored){}
                            
                            // Check trùng Tên sản phẩm
                            Entity.SanPham spCu = null;
                            for (Entity.SanPham sp : dsHienTai) {
                                if (sp.getTen().equalsIgnoreCase(ten)) {
                                    spCu = sp; break;
                                }
                            }
                            
                            if (spCu != null) {
                                // 1. Tồn tại -> Update lại thông tin
                                bus.capNhatSP(spCu.getId(), mapToDbDanhMuc(loai), mapToDbDang(dang), ten, ten, nsx, hoatChat, vat, spCu.getHamLuong(), spCu.getMoTa(), spCu.getDonViDoCoBan());
                                updateCount++;
                            } else {
                                // 2. Chưa có -> Insert mới
                                String newId = bus.taoMaMoi();
                                boolean isSaved = bus.themSP(newId, mapToDbDanhMuc(loai), mapToDbDang(dang), ten, ten, nsx, hoatChat, vat, "", "", "Hộp");
                                if(isSaved) {
                                    successCount++;
                                    Entity.SanPham newSp = new Entity.SanPham();
                                    newSp.setId(newId); newSp.setTen(ten);
                                    dsHienTai.add(newSp); // Update bộ đệm để tránh trùng dòng ngay trong file
                                }
                            }
                        } catch (Exception rowEx) {
                            errorRows.add(i + 1); // Lưu lại thứ tự dòng bị lỗi
                        }
                    }
                }
                
                // Trả lại chuột bình thường
                dlg.setCursor(Cursor.getDefaultCursor());
                
                // Hiển thị kết quả chuyên nghiệp
                StringBuilder msg = new StringBuilder();
                msg.append("Đã xử lý xong file Excel:\n");
                msg.append("- Thêm mới: ").append(successCount).append(" sản phẩm.\n");
                msg.append("- Cập nhật: ").append(updateCount).append(" sản phẩm.\n");
                
                if (!errorRows.isEmpty()) {
                    msg.append("- LỖI dữ liệu ở các dòng: ").append(errorRows.toString());
                    JOptionPane.showMessageDialog(dlg, msg.toString(), "Hoàn tất (Có lỗi dữ liệu)", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dlg, msg.toString(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                }
                
                loadDataFromDatabase(); 
            } catch (Exception ex) {
                dlg.setCursor(Cursor.getDefaultCursor());
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dlg, "Lỗi đọc file: " + ex.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
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

    private void loadDataFromDatabase() {
        allDataMock.clear();
        DAO.DAO_SanPham daoSP = new DAO.DAO_SanPham();
        List<Object[]> dsSP = daoSP.layDanhSachSanPhamChoBang();
        
        if (dsSP != null) {
            allDataMock.addAll(dsSP);
        }
        
        currentFilteredData.clear();
        currentFilteredData.addAll(allDataMock);
        updatePagination();
    }

    public void setReadOnly(boolean isReadOnly) {
        // Nếu isReadOnly = true (là Staff), ta sẽ khóa các nút thay đổi dữ liệu (false)
        // Nếu isReadOnly = false (là Admin), ta mở lại các nút (true)
        
        boolean canEdit = !isReadOnly; 
       
        btnThem.setEnabled(canEdit); 
        btnXoa.setEnabled(canEdit);
        btnSua.setEnabled(canEdit);
        btnLuu.setEnabled(canEdit);
        
    }
}