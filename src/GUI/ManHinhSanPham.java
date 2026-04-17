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
import javax.swing.JLabel;
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
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import BUS.BUS_SanPham;
import Entity.LoHang;
import Entity.SanPham;
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

    private final Color COLOR_PRIMARY      = Color.decode("#1E3A8A");
    private final Color COLOR_HEADER_TABLE = Color.decode("#2D435E");
    private final Color COLOR_BORDER       = Color.decode("#DFE3E8");
    private final Color COLOR_LIGHT_BLUE   = Color.decode("#E0F2FE");
    private final Color COLOR_DISABLED_BG  = Color.decode("#F3F4F6");

    private final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);

    private JTable tblSanPham, tblDonVi, tblLoHang;
    private DefaultTableModel modelSanPham;
    private ToggleEditableTableModel modelDonVi;
    private DefaultTableModel modelLoHang;

    private final List<Object[]> allDataMock         = new ArrayList<>();
    private final List<Object[]> currentFilteredData  = new ArrayList<>();
    
    // MẶC ĐỊNH LÀ 20 DÒNG THEO YÊU CẦU
    private int currentPageMock  = 1;
    private int itemsPerPageMock = 20; 
    
    private JLabel  lblPage;
    private JLabel  lblTotalTopBar;
    private JButton btnPrev, btnNext;
    private JComboBox<String> cbLimit;

    private WatermarkTextField txtTimKiem;
    private ButtonGroup bgLoai, bgDang;
    private javax.swing.JCheckBox chkThungRac;

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

    private JPanel  pnlBody;
    private JPanel  pnlRightDetail;
    private boolean detailVisible = false;
    private boolean isAdding       = false;
    private boolean uomAutoChanging = false;

    private static final String[] COLS_COLLAPSED = {"Mã", "Tên", "Loại", "Hoạt chất"};
    private static final String[] COLS_EXPANDED  = {"Mã", "Tên", "Loại", "Hoạt chất", "Dạng bào chế", "NSX", "VAT"};

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
        setDetailVisible(false);
    }

    // ========================================================================
    // BỘ THÔNG BÁO UI/UX GIAO DIỆN CHUẨN MỚI (CHẮC CHẮN HIỆN CHỮ)
    // ========================================================================
    private void showCustomNotification(String titleText, String message, String type) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createLineBorder(Color.decode("#1E3A8A"), 2));
        pnlMain.setBackground(Color.WHITE);

        // 1. HEADER (TIÊU ĐỀ XANH ĐẬM)
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.decode("#1E3A8A"));
        pnlHeader.setPreferredSize(new Dimension(0, 45));
        JLabel lblTitle = new JLabel(titleText.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.CENTER);

        // 2. BODY (ICON VÀ CHỮ)
        JPanel pnlBody = new JPanel(null);
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setPreferredSize(new Dimension(420, 110));

        JPanel pnlIcon = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color mainColor = type.equals("ERROR") ? Color.decode("#EF4444") : 
                                  (type.equals("SUCCESS") ? Color.decode("#10B981") : Color.decode("#F59E0B"));
                Color bgColor = type.equals("ERROR") ? Color.decode("#FEE2E2") : 
                                (type.equals("SUCCESS") ? Color.decode("#D1FAE5") : Color.decode("#FEF3C7"));
                
                g2.setColor(bgColor);
                g2.fillOval(0, 0, 50, 50);
                g2.setColor(mainColor);
                g2.setStroke(new java.awt.BasicStroke(3f));
                g2.drawOval(0, 0, 50, 50);
                
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                FontMetrics fm = g2.getFontMetrics();
                String symbol = type.equals("ERROR") ? "X" : (type.equals("SUCCESS") ? "V" : "!");
                int x = (50 - fm.stringWidth(symbol)) / 2;
                int y = ((50 - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(symbol, x, y);
                g2.dispose();
            }
        };
        pnlIcon.setBounds(20, 25, 50, 50);
        pnlIcon.setOpaque(false);

        JTextArea msg = new JTextArea(message);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(Color.decode("#333333"));
        msg.setWrapStyleWord(true);
        msg.setLineWrap(true);
        msg.setOpaque(false);
        msg.setEditable(false);
        msg.setFocusable(false);
        
        JScrollPane scroll = new JScrollPane(msg);
        scroll.setBounds(85, 20, 315, 80);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        applyThinScrollBar(scroll);

        pnlBody.add(pnlIcon);
        pnlBody.add(scroll);

        // 3. FOOTER (NÚT ĐÓNG CANH PHẢI)
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlFooter.setBackground(Color.WHITE);
        JButton btnClose = new JButton("Đóng");
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.setBackground(Color.decode("#1E3A8A"));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dialog.dispose());
        pnlFooter.add(btnClose);

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlBody, BorderLayout.CENTER);
        pnlMain.add(pnlFooter, BorderLayout.SOUTH);

        dialog.add(pnlMain);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private boolean showCustomConfirmDialog(String titleText, String message) {
        final boolean[] result = {false};
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createLineBorder(Color.decode("#1E3A8A"), 2));
        pnlMain.setBackground(Color.WHITE);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.decode("#1E3A8A"));
        pnlHeader.setPreferredSize(new Dimension(0, 45));
        JLabel lblTitle = new JLabel(titleText.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.CENTER);

        JPanel pnlBody = new JPanel(new BorderLayout());
        pnlBody.setBackground(Color.WHITE);
        // ĐÃ SỬA: Giảm padding trên xuống 15, dưới xuống 10 để nhường chỗ cho chữ
        pnlBody.setBorder(new EmptyBorder(15, 20, 10, 20));
        JLabel msg = new JLabel("<html><div style='text-align: center; color:#333333; font-family:Segoe UI; font-size:14px; line-height: 1.5;'>" + message.replace("\n", "<br>") + "</div></html>", SwingConstants.CENTER);
        pnlBody.add(msg, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        pnlFooter.setBackground(Color.WHITE);
        
        JButton btnYes = new JButton("Đồng ý");
        btnYes.setPreferredSize(new Dimension(100, 35));
        btnYes.setBackground(Color.decode("#EF4444")); 
        btnYes.setForeground(Color.WHITE);
        btnYes.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnYes.setFocusPainted(false);
        btnYes.setBorderPainted(false);
        btnYes.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnYes.addActionListener(e -> { result[0] = true; dialog.dispose(); });

        JButton btnNo = new JButton("Hủy");
        btnNo.setPreferredSize(new Dimension(100, 35));
        btnNo.setBackground(Color.decode("#1E3A8A"));
        btnNo.setForeground(Color.WHITE);
        btnNo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNo.setFocusPainted(false);
        btnNo.setBorderPainted(false);
        btnNo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNo.addActionListener(e -> dialog.dispose());

        pnlFooter.add(btnYes); pnlFooter.add(btnNo);

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlBody, BorderLayout.CENTER);
        pnlMain.add(pnlFooter, BorderLayout.SOUTH);

        dialog.add(pnlMain);
        // ĐÃ SỬA: Tăng chiều cao lên 210 để khung rộng rãi, không bị cụt chữ
        dialog.setSize(400, 210);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return result[0];
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
        
        lblTotalTopBar = new JLabel("(0 sản phẩm)");
        pnlLeft.add(lblTotalTopBar);

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
        
        btnXuat.addActionListener(e -> thucHienXuatExcelThang());
        btnNhap.addActionListener(e -> thucHienNhapExcelThang());
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
        
        // --- ĐOẠN MỚI THÊM: THÙNG RÁC ---
        pnl.add(Box.createVerticalStrut(20));
        chkThungRac = new javax.swing.JCheckBox("Sản phẩm đã ẩn");
        chkThungRac.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        chkThungRac.setBackground(Color.WHITE);
        chkThungRac.setBorder(new EmptyBorder(5, 15, 5, 0));
        chkThungRac.setFocusPainted(false);
        chkThungRac.addActionListener(e -> {
            if (chkThungRac.isSelected()) {
                loadDataThungRac(); 
                btnXoaBottom.setText("Khôi phục");
                btnXoaBottom.setBackground(Color.decode("#10B981")); // Xanh lá
            } else {
                loadDataFromDatabase(); 
                btnXoaBottom.setText("Ẩn sản phẩm");
                btnXoaBottom.setBackground(Color.decode("#F59E0B")); // Cam
            }
        });
        pnl.add(chkThungRac);
        // ---------------------------------
        
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
        cbLimit = new JComboBox<>(new String[]{"20", "30", "50"}); cbLimit.setFont(new Font("Segoe UI", Font.PLAIN, 14)); cbLimit.setBackground(Color.WHITE);
        lblPage = new JLabel("Trang 1/1 (Tổng: 0)"); lblPage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnPrev.addActionListener(e -> { if (currentPageMock > 1) { currentPageMock--; updatePagination(); } });
        btnNext.addActionListener(e -> { int max = (int) Math.ceil((double) currentFilteredData.size() / itemsPerPageMock); if (currentPageMock < max) { currentPageMock++; updatePagination(); } });
        cbLimit.addActionListener(e -> { itemsPerPageMock = Integer.parseInt(cbLimit.getSelectedItem().toString()); currentPageMock = 1; updatePagination(); });
        pnlPage.add(btnPrev); pnlPage.add(btnNext); pnlPage.add(cbLimit); pnlPage.add(lblPage);
        pnl.add(pnlPage, BorderLayout.SOUTH);
        return pnl;
    }

    private void updatePagination() {
        if (lblTotalTopBar != null) {
            lblTotalTopBar.setText("(" + allDataMock.size() + " sản phẩm)");
        }

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
        btnXoaBottom = createBtnWithIcon("Ẩn sản phẩm", "#F59E0B", new MenuIcon("TRASH"));  btnXoaBottom.setPreferredSize(new Dimension(140, 36));
        btnLuuBottom     = createBtnWithIcon("Lưu",           "#10B981", new MenuIcon("SAVE"));   btnLuuBottom.setPreferredSize(new Dimension(100, 36));
        btnXacNhanThem   = createBtnWithIcon("Xác nhận Thêm", "#E11D48", new MenuIcon("ADD"));    btnXacNhanThem.setPreferredSize(new Dimension(170, 36));
        btnHuyBottom     = createBtnWithIcon("Hủy",           "#64748B", new MenuIcon("CANCEL")); btnHuyBottom.setPreferredSize(new Dimension(100, 36));

        btnCapNhatBottom.addActionListener(e -> { 
            if (txtId.getText().isEmpty()) { 
                showCustomNotification("Thông báo", "Vui lòng chọn sản phẩm cần cập nhật!", "WARNING"); 
                return; 
            } 
            setEditMode(true); 
        });
        btnXoaBottom.addActionListener(e -> {
            if (txtId.getText().isEmpty()) return;
            
            // Nếu nút đang là Khôi phục
            if (btnXoaBottom.getText().equals("Khôi phục")) {
                BUS_SanPham bus = new BUS_SanPham();
                if (bus.khoiPhucSP(txtId.getText())) {
                    showCustomNotification("KHÔI PHỤC THÀNH CÔNG", "Đã đưa sản phẩm trở lại danh sách bán hàng!", "SUCCESS");
                    loadDataThungRac(); // Tải lại danh sách thùng rác
                    setDetailVisible(false);
                } else {
                    showCustomNotification("LỖI", "Khôi phục thất bại!", "ERROR");
                }
            } else {
                // Nếu nút là Ẩn
                actionAnSanPham(); 
            }
        });
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
        
        BUS_SanPham bus = new BUS_SanPham();
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
        String hoatChat = txtHoatChat.getText().trim();
        String vietTat = txtVietTat.getText().trim();
        String hamLuong = txtHamLuong.getText().trim();
        String vatStr = txtVAT.getText().trim();

        // 1. KIỂM TRA RỖNG: Ép buộc người dùng phải nhập đủ các trường quan trọng
        if (ten.isEmpty() || hoatChat.isEmpty() || vietTat.isEmpty() || hamLuong.isEmpty() || vatStr.isEmpty()) { 
            showCustomNotification("THIẾU THÔNG TIN", "Vui lòng điền đầy đủ các ô: Tên, Viết tắt, Hoạt chất, Hàm lượng và VAT!", "WARNING"); 
            return; 
        }

        // 2. KIỂM TRA ĐỊNH DẠNG: Thuế VAT phải là số và không được âm
        double vat = 0;
        try {
            vat = Double.parseDouble(vatStr);
            if (vat < 0) {
                showCustomNotification("SAI ĐỊNH DẠNG", "Thuế VAT không được là số âm!", "ERROR"); 
                return;
            }
        } catch (NumberFormatException e) {
            showCustomNotification("SAI ĐỊNH DẠNG", "Thuế VAT phải là một con số hợp lệ!", "ERROR"); 
            return;
        }
        
        String loai = cbLoaiCT.getSelectedItem().toString();
        String dangBaoChe = cbDang.getSelectedItem().toString();
        String nsx = cbNhaSX.getSelectedItem().toString();
        String dvt = cbDVT.getSelectedItem().toString();
        String moTa = txtMoTa.getText().trim();

        txtLoaiView.setText(loai); txtDangView.setText(dangBaoChe); txtNSXView.setText(nsx); txtDVTView.setText(dvt);
        if (loai.equals("Thuốc kê đơn")) txtLoaiView.setDisabledTextColor(Color.decode("#EF4444")); else if (loai.equals("Sản phẩm chức năng")) txtLoaiView.setDisabledTextColor(Color.decode("#10B981")); else txtLoaiView.setDisabledTextColor(Color.decode("#2179E0"));

        String dbDanhMuc = mapToDbDanhMuc(loai);
        String dbDang = mapToDbDang(dangBaoChe);

        BUS_SanPham bus = new BUS_SanPham();
        boolean success = false;

        if (isAdding) {
            success = bus.themSP(ma, dbDanhMuc, dbDang, ten, vietTat, nsx, hoatChat, vat, hamLuong, moTa, dvt);
            if(success) showCustomNotification("THÊM MỚI THÀNH CÔNG", "Đã thêm sản phẩm vào Database thành công!", "SUCCESS");
        } else {
            success = bus.capNhatSP(ma, dbDanhMuc, dbDang, ten, vietTat, nsx, hoatChat, vat, hamLuong, moTa, dvt);
            if(success) showCustomNotification("CẬP NHẬT THÀNH CÔNG", "Cập nhật Database thành công!", "SUCCESS");
        }

        if(success) {
            loadDataFromDatabase(); 
            for (int i = 0; i < tblSanPham.getRowCount(); i++) { 
                if (tblSanPham.getValueAt(i, 0).toString().equals(ma)) { tblSanPham.setRowSelectionInterval(i, i); break; } 
            }
            setEditMode(false);
            isAdding = false;
        } else {
            showCustomNotification("LỖI HỆ THỐNG", "Lưu thất bại! Vui lòng kiểm tra lại kết nối CSDL.", "ERROR");
        }
    }
    private void loadDataThungRac() {
        allDataMock.clear();
        BUS_SanPham bus = new BUS_SanPham();
        List<Object[]> dsSP = bus.layDanhSachSanPhamDaAn();
        
        if (dsSP != null) {
            allDataMock.addAll(dsSP);
        }
        
        currentFilteredData.clear();
        currentFilteredData.addAll(allDataMock);
        updatePagination();
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

    private void actionAnSanPham() {
        if (txtId.getText().isEmpty()) return;

        String maSP = txtId.getText();
        String tenSP = txtTen.getText().trim();

        boolean confirmed = showCustomConfirmDialog(
            "XÁC NHẬN ẨN SẢN PHẨM",
            "Sản phẩm <b>" + tenSP + "</b> sẽ bị ẩn khỏi hệ thống.\n"
            + "Dữ liệu lịch sử đơn hàng vẫn được giữ nguyên.\n\n"
            + "Bạn có chắc chắn muốn ẩn sản phẩm này?"
        );

        if (!confirmed) return;

        BUS_SanPham bus = new BUS_SanPham();

        // Kiểm tra xem SP còn tồn kho không — không nên ẩn khi còn hàng
        int soLuongTon = bus.getSoLuongTon(maSP);
        if (soLuongTon > 0) {
            showCustomNotification(
                "KHÔNG THỂ ẨN SẢN PHẨM",
                "Sản phẩm \"" + tenSP + "\" hiện còn " + soLuongTon 
                + " đơn vị trong kho.\n"
                + "Vui lòng xuất hết tồn kho trước khi ẩn sản phẩm.",
                "WARNING"
            );
            return;
        }

        boolean success = bus.anSP(maSP);

        if (success) {
            showCustomNotification(
                "ẨN SẢN PHẨM THÀNH CÔNG",
                "Sản phẩm \"" + tenSP + "\" đã được ẩn.\n"
                + "Lịch sử giao dịch liên quan vẫn được lưu trữ đầy đủ.",
                "SUCCESS"
            );
            loadDataFromDatabase();
            clearDetailForm();
            setDetailVisible(false);
        } else {
            showCustomNotification(
                "THAO TÁC THẤT BẠI",
                "Không thể ẩn sản phẩm \"" + tenSP + "\"!\n"
                + "Vui lòng kiểm tra lại kết nối CSDL.",
                "ERROR"
            );
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
        
        BUS_SanPham bus = new BUS_SanPham();
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

    private void thucHienXuatExcelThang() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        java.awt.FileDialog fd = new java.awt.FileDialog(parentFrame, "Chọn nơi lưu file Excel", java.awt.FileDialog.SAVE);
        fd.setFile("DanhSachSanPham.xlsx");
        fd.setVisible(true); 
        
        String dir = fd.getDirectory();
        String file = fd.getFile();
        
        if (dir != null && file != null) {
            String filePath = dir + file;
            if (!filePath.endsWith(".xlsx")) filePath += ".xlsx";
            
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            BUS_SanPham bus = new BUS_SanPham();
            List<SanPham> fullData = bus.getDsThuoc();
            
            if (fullData == null) {
                setCursor(Cursor.getDefaultCursor());
                showCustomNotification("Lỗi dữ liệu", "Không thể lấy dữ liệu từ CSDL!", "ERROR");
                return;
            }
            
            String result = exportExcelAction(filePath, fullData);
            
            setCursor(Cursor.getDefaultCursor());

            if (result.equals("SUCCESS")) {
                showCustomNotification("XUẤT EXCEL THÀNH CÔNG", "Đã xuất " + fullData.size() + " sản phẩm thành công tại:\n" + filePath, "SUCCESS");
            } else if (result.equals("FILE_OPEN")) {
                showCustomNotification("LỖI GHI ĐÈ", "File Excel này ĐANG ĐƯỢC MỞ!\nVui lòng đóng file Excel trước khi xuất.", "ERROR");
            } else {
                showCustomNotification("LỖI", "Xuất file thất bại!\nChi tiết: " + result, "ERROR");
            }
        }
    }

    private String exportExcelAction(String path, List<SanPham> data) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh Sach San Pham");
            
            Row headerRow = sheet.createRow(0);
            String[] headers = {"id", "danhMuc", "dang", "ten", "tenVietTat", "nhaSanXuat", "hoatChat", "thueVAT", "hamLuong", "moTa", "donViDoCoBan", "ngayTao"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            
            for(int i=0; i<headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 1); 
                SanPham sp = data.get(i);
                
                row.createCell(0).setCellValue(sp.getId() != null ? sp.getId() : "");
                row.createCell(1).setCellValue(sp.getDanhMuc() != null ? sp.getDanhMuc().name() : "");
                row.createCell(2).setCellValue(sp.getDang() != null ? sp.getDang().name() : "");
                row.createCell(3).setCellValue(sp.getTen() != null ? sp.getTen() : "");
                row.createCell(4).setCellValue(sp.getTenVietTat() != null ? sp.getTenVietTat() : "");
                row.createCell(5).setCellValue(sp.getNhaSanXuat() != null ? sp.getNhaSanXuat() : "");
                row.createCell(6).setCellValue(sp.getHoatChat() != null ? sp.getHoatChat() : "");
                row.createCell(7).setCellValue(Double.parseDouble(String.format(java.util.Locale.US, "%.2f", sp.getThueVAT())));
                row.createCell(8).setCellValue((sp.getHamLuong() != null && !sp.getHamLuong().isEmpty()) ? sp.getHamLuong() : "NULL");
                row.createCell(9).setCellValue((sp.getMoTa() != null && !sp.getMoTa().isEmpty()) ? sp.getMoTa() : "NULL");
                row.createCell(10).setCellValue(sp.getDonViDoCoBan() != null ? sp.getDonViDoCoBan() : "");
                row.createCell(11).setCellValue(sp.getNgayTao() != null ? sp.getNgayTao().format(dtf) : "");
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (java.io.FileOutputStream out = new java.io.FileOutputStream(path)) {
                workbook.write(out);
            }
            
            return "SUCCESS";
        } catch (java.io.FileNotFoundException ex) {
            return "FILE_OPEN";
        } catch (Exception ex) {
            ex.printStackTrace();
            return ex.getMessage();
        }
    }

    private void thucHienNhapExcelThang() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        java.awt.FileDialog fd = new java.awt.FileDialog(parentFrame, "Chọn file Excel để nhập", java.awt.FileDialog.LOAD);
        fd.setFile("*.xlsx;*.xls"); 
        fd.setVisible(true); 
        
        String dir = fd.getDirectory();
        String file = fd.getFile();
        
        if (dir != null && file != null) {
            File fileToImport = new File(dir, file);
            
            if (!fileToImport.exists()) {
                showCustomNotification("LỖI FILE", "Không tìm thấy file:\n" + fileToImport.getAbsolutePath(), "ERROR");
                return;
            }

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            try (java.io.FileInputStream fis = new java.io.FileInputStream(fileToImport);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                
                Sheet sheet = workbook.getSheetAt(0);
                DataFormatter formatter = new DataFormatter(); 
                BUS_SanPham bus = new BUS_SanPham();
                
                int successCount = 0;
                int updateCount = 0;
                List<Integer> errorRows = new ArrayList<>();
                List<Integer> missingDataRows = new ArrayList<>();
                
                List<SanPham> dsHienTai = bus.getDsThuoc();
                
                for (int i = 1; i <= sheet.getLastRowNum(); i++) { 
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        String ten = formatter.formatCellValue(row.getCell(3)).trim(); 
                        if(ten.isEmpty()) continue; 

                        String loai = formatter.formatCellValue(row.getCell(1)).trim(); 
                        String dang = formatter.formatCellValue(row.getCell(2)).trim(); 
                        
                        if (loai.isEmpty() || dang.isEmpty() || loai.equals("NULL") || dang.equals("NULL")) {
                            missingDataRows.add(i + 1); 
                            continue;
                        }

                        try {
                            String vietTat = formatter.formatCellValue(row.getCell(4)).trim();
                            String nsx = formatter.formatCellValue(row.getCell(5)).trim();
                            String hoatChat = formatter.formatCellValue(row.getCell(6)).trim();
                            
                            double vat = 10.0;
                            try {
                                String vatStr = formatter.formatCellValue(row.getCell(7)).replace("%", "").trim();
                                if(!vatStr.isEmpty() && !vatStr.equals("NULL")) vat = Double.parseDouble(vatStr);
                            } catch(Exception ignored){}
                            
                            String hamLuong = formatter.formatCellValue(row.getCell(8)).trim();
                            if(hamLuong.equals("NULL")) hamLuong = "";
                            
                            String moTa = formatter.formatCellValue(row.getCell(9)).trim();
                            if(moTa.equals("NULL")) moTa = "";
                            
                            String donVi = formatter.formatCellValue(row.getCell(10)).trim();
                            if(donVi.equals("NULL") || donVi.isEmpty()) donVi = "Hộp"; 
                            
                            SanPham spCu = null;
                            for (SanPham sp : dsHienTai) {
                                if (sp.getTen().equalsIgnoreCase(ten)) {
                                    spCu = sp; break;
                                }
                            }
                            
                            if (spCu != null) {
                                bus.capNhatSP(spCu.getId(), mapToDbDanhMuc(loai), mapToDbDang(dang), ten, vietTat, nsx, hoatChat, vat, hamLuong, moTa, donVi);
                                updateCount++;
                            } else {
                                String newId = bus.taoMaMoi();
                                boolean isSaved = bus.themSP(newId, mapToDbDanhMuc(loai), mapToDbDang(dang), ten, vietTat, nsx, hoatChat, vat, hamLuong, moTa, donVi);
                                if(isSaved) {
                                    successCount++;
                                    SanPham newSp = new SanPham();
                                    newSp.setId(newId); newSp.setTen(ten);
                                    dsHienTai.add(newSp); 
                                }
                            }
                        } catch (Exception rowEx) {
                            errorRows.add(i + 1);
                        }
                    }
                }
                
                setCursor(Cursor.getDefaultCursor());
                
                StringBuilder msg = new StringBuilder();
                msg.append("Quá trình xử lý file Excel hoàn tất:\n");
                msg.append("   • Thêm mới thành công: ").append(successCount).append("\n");
                msg.append("   • Cập nhật thông tin: ").append(updateCount).append("\n");
                
                boolean hasError = false;
                if (!missingDataRows.isEmpty()) {
                    msg.append("\n⚠ Cảnh báo: Các dòng sau bị bỏ qua do thiếu Tên/Loại/Dạng: ").append(missingDataRows.toString());
                    hasError = true;
                }
                if (!errorRows.isEmpty()) {
                    msg.append("\n⚠ Lỗi format dữ liệu ở các dòng: ").append(errorRows.toString());
                    hasError = true;
                }
                
                if (hasError) {
                    showCustomNotification("NHẬP CÓ CẢNH BÁO", msg.toString(), "WARNING");
                } else {
                    showCustomNotification("NHẬP DỮ LIỆU THÀNH CÔNG", msg.toString(), "SUCCESS");
                }
                
                loadDataFromDatabase(); 
            } catch (Exception ex) {
                setCursor(Cursor.getDefaultCursor());
                ex.printStackTrace();
                showCustomNotification("LỖI HỆ THỐNG", "Không thể đọc file Excel:\n" + ex.getMessage(), "ERROR");
            }
        }
    }

    private void loadDataFromDatabase() {
        allDataMock.clear();
        BUS_SanPham bus = new BUS_SanPham();
        List<Object[]> dsSP = bus.layDanhSachChoBang();
        
        if (dsSP != null) {
            allDataMock.addAll(dsSP);
        }
        
        currentFilteredData.clear();
        currentFilteredData.addAll(allDataMock);
        updatePagination();
    }
}