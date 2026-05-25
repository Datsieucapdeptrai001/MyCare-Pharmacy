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
import javax.swing.JOptionPane;
import java.awt.BasicStroke;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JFormattedTextField;
import javax.swing.JSplitPane;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.print.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JLabel;

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
        public UnitOfMeasure(MeasurementName measurement, BigDecimal rate) {
            this.measurement = measurement; this.baseUnitConversionRate = rate;
        }
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
            this.id = id; this.batchNumber = batch; this.quantity = qty;
            this.rawPrice = price; this.expiryDate = exp; this.status = st;
        }
        public String getId() { return id; }
        public String getBatchNumber() { return batchNumber; }
        public int getQuantity() { return quantity; }
        public BigDecimal getRawPrice() { return rawPrice; }
        public LocalDate getExpiryDate() { return expiryDate; }
        public LotStatus getStatus() { return status; }
    }

    public static class Product {
        private String id, name, shortName, manufacturer, activeIngredient, strength, description, baseUnitOfMeasure;
        private ProductCategory category;
        private DosageForm form;
        private double vat;
        private Set<UnitOfMeasure> unitOfMeasureSet = new HashSet<>();
        private Set<Lot> lotSet = new HashSet<>();
        public Product(String id, String bc, ProductCategory cat, DosageForm frm, String nm, String sn,
                       String mfg, String act, double v, String str, String desc, String baseUom) {
            this.id = id; this.category = cat; this.form = frm; this.name = nm;
            this.shortName = sn; this.manufacturer = mfg; this.activeIngredient = act;
            this.vat = v; this.strength = str; this.description = desc; this.baseUnitOfMeasure = baseUom;
        }
        public String getId() { return id; } public void setId(String id) { this.id = id; }
        public String getName() { return name; } public String getShortName() { return shortName; }
        public String getManufacturer() { return manufacturer; }
        public String getActiveIngredient() { return activeIngredient; }
        public String getStrength() { return strength; } public String getDescription() { return description; }
        public ProductCategory getCategory() { return category; } public DosageForm getForm() { return form; }
        public double getVatAsDouble() { return vat; }
        public String getBaseUnitOfMeasure() { return baseUnitOfMeasure; }
        public Set<UnitOfMeasure> getUnitOfMeasureSet() { return unitOfMeasureSet; }
        public void setUnitOfMeasureSet(Set<UnitOfMeasure> uoms) { this.unitOfMeasureSet = uoms; }
        public Set<Lot> getLotSet() { return lotSet; }
        public void setLotSet(Set<Lot> lots) { this.lotSet = lots; }
    }

    // =========================================================================
    // HẰNG SỐ MÀU / FONT
    // =========================================================================
    private final Color COLOR_PRIMARY      = Color.decode("#1E3A8A");
    private final Color COLOR_HEADER_TABLE = Color.decode("#2D435E");
    private final Color COLOR_BORDER       = Color.decode("#DFE3E8");
    private final Color COLOR_LIGHT_BLUE   = Color.decode("#E0F2FE");
    private final Color COLOR_DISABLED_BG  = Color.decode("#F3F4F6");

    private final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);

    // =========================================================================
    // BẢNG / MODEL
    // =========================================================================
    private JTable tblSanPham, tblDonVi, tblLoHang;
    private DefaultTableModel modelSanPham;
    private ToggleEditableTableModel modelDonVi;
    private DefaultTableModel modelLoHang;

    private final List<Object[]> allDataMock        = new ArrayList<>();
    private final List<Object[]> currentFilteredData = new ArrayList<>();

    private int currentPageMock  = 1;
    private int itemsPerPageMock = 20;

    private JLabel  lblPage;
    private JLabel  lblTotalTopBar;
    private JButton btnPrev, btnNext;
    private JComboBox<String> cbLimit;

    // =========================================================================
    // SIDEBAR FILTER — CÁC NHÓM CÓ THỂ THU GỌN
    // =========================================================================
    private WatermarkTextField txtTimKiem;
    private ButtonGroup bgLoai, bgDang, bgNhom;
    private javax.swing.JCheckBox chkThungRac;

    // Panel chứa radio + nút toggle cho từng nhóm
    private JPanel pnlLoaiBody, pnlDangBody, pnlNhomBody;
    private JButton btnToggleLoai, btnToggleDang, btnToggleNhom;
    private boolean loaiExpanded = true, dangExpanded = false, nhomExpanded = false;

    // =========================================================================
    // FORM CHI TIẾT BÊN PHẢI
    // =========================================================================
    private JTextField txtId, txtTen, txtVietTat, txtHoatChat, txtHamLuong, txtVAT;
    private JTextField txtGiaBan;
    // 2 FIELD MỚI
    private JTextField txtMaVach;
    private JComboBox<String> cbNhomBenhLy;
    private JTextField txtNhomBenhLyView;
    // Vị trí thuốc — ComboBox dùng String[] thay vì class riêng
    private JComboBox<String> cbViTri;           // hiển thị "Khu / Kệ / Tầng"
    private JTextField txtViTriView;
    private CardLayout clViTri;
    private JPanel pnlViTriCard;

    private JTextArea  txtMoTa;
    private CardLayout clLoai, clDang, clNSX, clDVT, clNhomBenhLy;
    private JPanel     pnlLoai, pnlDang, pnlNSX, pnlDVT, pnlNhomBenhLyCard;
    private JTextField txtLoaiView, txtDangView, txtNSXView, txtDVTView;
    private JComboBox<String> cbLoaiCT, cbDang, cbNhaSX, cbDVT;

    // ── Vị trí thuốc (lưu nội tuyến — không class riêng) ────────────────────
    // Mỗi phần tử: [id, khu, ke, tang, moTa]
    private List<Object[]> dsViTriData = new ArrayList<>();   // load từ DB
    private String currentViTriId = "";                        // viTriId của SP đang xem

    private JPanel  pnlActionBottom;
    private JButton btnCapNhatBottom, btnXoaBottom, btnLuuBottom, btnHuyBottom, btnXacNhanThem;
    private JScrollPane scrollMoTa;
    private JScrollPane scrollTblSanPham;

    private JPanel  pnlBody;
    private JPanel  pnlRightDetail;
    private boolean detailVisible  = false;
    private boolean isAdding       = false;
    private boolean uomAutoChanging = false;
    private JPanel pnlMaVachList;
    private List<String[]> listMaVachData = new ArrayList<>();
    private JTextField txtScanBarcode;
    private JButton btnThemMV;
    private JButton btnXoaMV;
    private JButton btnInMaVach;
    private String currentMaSP = "";
    private String selectedMaSP = ""; // Ma SP dang duoc highlight trong bang
    private double giaBanGoc = 0;
    private JButton topBtnThem, topBtnNhap, topBtnXuat;
    private boolean isStaffRole = false;
    private static final String[] COLS_COLLAPSED = {"Mã", "Mã vạch", "Nhóm bệnh lý", "Tên", "Loại", "Hoạt chất", "Giá bán"};
    private static final String[] COLS_EXPANDED  = {"Mã", "Mã vạch", "Nhóm bệnh lý", "Tên", "Loại", "Hoạt chất", "Dạng bào chế", "NSX", "VAT", "Giá bán"};
    private static final String[] DANH_SACH_NHOM_BENH_LY = {
            "Ho – Đờm – Viêm họng",
            "Sốt – Cảm cúm – Sổ mũi",
            "Đau đầu – Giảm đau – Hạ sốt",
            "Dạ dày – Tiêu hóa – Đại tràng",
            "Dị ứng – Mẩn ngứa – Mề đay",
            "Mắt – Tai – Mũi",
            "Thuốc bổ – Vitamin – Khoáng chất",
            "Dược mỹ phẩm – Da liễu",
            "Xương khớp – Gút",
            "Tim mạch – Huyết áp",
            "Đái tháo đường",
        };
 // --- Các biến trạng thái tích hợp từ PanelCatLieu ---
    private static final int COL_L_MA_SP      = 0;
    private static final int COL_L_TEN_THUOC  = 1;
    private static final int COL_L_DVT        = 2;
    private static final int COL_L_SANG       = 3;
    private static final int COL_L_TRUA       = 4;
    private static final int COL_L_CHIEU      = 5;
    private static final int COL_L_TOI        = 6;
    private static final int COL_L_CACH_DUNG  = 7;
    private static final int COL_L_NGAY       = 8;
    private static final int COL_L_TONG_SL    = 9;
    private static final int COL_L_THANH_TIEN = 10;
    private static final int COL_L_XOA        = 11;

    private String comboIdDangSua = null;
    private final List<SanPham.ChiTietLieu> dsChiTietLieuLuuTru = new ArrayList<>();
    private String maSPDangChonLieu = null, tenSPDangChonLieu = null, dvtDangChonLieu = null;
    private double giaDangChonLieu = 0;
    private boolean dangCapNhatBangLieu = false;

    private JTable tblDanhSachLieuNangCao;
    private DefaultTableModel mdlDanhSachLieuNangCao;
    private JTable tblChiTietLieu;
    private DefaultTableModel modChiTietLieu;
    private JTextField txtTenComboNangCao, txtGhiChuLieuNangCao;
    private JComboBox<String> cbNhomBenhLieuNangCao, cbTimKiemLieuNangCao, cbCachDungLieuNangCao;
    private JFormattedTextField txtGiaBanComboNangCao;
    private JTextField txtSearchLieuNangCao, txtSangLieu, txtTruaLieu, txtChieuLieu, txtToiLieu, txtSoNgayLieu;
    private JLabel lblDVTLieu, lblGiaLieu, lblComboIdNangCao, lblTongTienLieuNangCao;
    private javax.swing.Timer autocompleteTimerLieu;
    private List<Object[]> cacheKetQuaTimKiemLieu = new ArrayList<>();
    private javax.swing.JPopupMenu popupTimThuoc;
    private javax.swing.JList<String> listPopupThuoc;
    private javax.swing.DefaultListModel<String> modelPopupThuoc;
    private boolean dangChonTuPopup = false;
    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
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
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);

        // Khởi tạo tab 1 (Danh sách sản phẩm đơn lẻ)
        pnlBody = new JPanel(new BorderLayout());
        pnlBody.setBackground(Color.WHITE);
        pnlBody.add(createSidebarFilter(), BorderLayout.WEST);
        pnlBody.add(createCenterList(), BorderLayout.CENTER);
        pnlRightDetail = createRightDetail();
        
        tabbedPane.addTab("Sản phẩm đơn lẻ", pnlBody);
        
        // --- TAB 2: QUẢN LÝ CẮT LIỀU ---
        JPanel pnlTabLieu = createTabQuanLyLieu();
        tabbedPane.addTab("Mẫu thuốc cắt liều (Combo)", pnlTabLieu);

        // CHỈ THÊM DUY NHẤT TABBED PANE VÀO CENTER CỦA MÀN HÌNH CHÍNH
        add(tabbedPane, BorderLayout.CENTER); 
        // Đã xóa dòng: add(pnlBody, BorderLayout.CENTER); bị lỗi ở đây

        loadDataFromDatabase();
        capNhatDuLieuComboBoxTuDB();
        loadDanhSachComboNangCaoRaGiaoDien(true);
        setDetailVisible(false);
    }
 // =========================================================================
    // GIAO DIỆN TAB THUỐC CẮT LIỀU (CHUẨN MASTER DATA)
    // =========================================================================
    private JPanel createTabQuanLyLieu() {
        JPanel pnlMain = new JPanel(new BorderLayout(8, 8));
        pnlMain.setBackground(Color.WHITE);
        pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 
        // ── PANEL TRÁI: DANH SÁCH MẪU ────────────────────────────────────────
        JPanel pnlLeft = new JPanel(new BorderLayout(4, 4));
        pnlLeft.setBorder(BorderFactory.createTitledBorder(" Danh sách mẫu liều "));
        pnlLeft.setBackground(Color.WHITE);
        pnlLeft.setPreferredSize(new Dimension(340, 0));
 
        String[] colsDS = {"Mã", "Tên Mẫu Liều", "Nhóm Bệnh", "Giá"};
        mdlDanhSachLieuNangCao = new DefaultTableModel(colsDS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDanhSachLieuNangCao = new JTable(mdlDanhSachLieuNangCao);
        tblDanhSachLieuNangCao.setRowHeight(26);
        tblDanhSachLieuNangCao.getSelectionModel().addListSelectionListener(
            e -> { if (!e.getValueIsAdjusting()) onChonMauLieuTuDanhSach(); });
        pnlLeft.add(new JScrollPane(tblDanhSachLieuNangCao), BorderLayout.CENTER);
 
        JButton btnXoaMau = createBtnWithIcon("Xóa mẫu chọn", "#EF4444", new MenuIcon("TRASH", 16));
        btnXoaMau.addActionListener(e -> xoaMauLieuHienTai());
        JPanel pnlBotLeft = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBotLeft.setOpaque(false);
        pnlBotLeft.add(btnXoaMau);
        pnlLeft.add(pnlBotLeft, BorderLayout.SOUTH);
 
        // ── PANEL PHẢI: SOẠN THẢO CHI TIẾT ──────────────────────────────────
        JPanel pnlRight = new JPanel(new BorderLayout(0, 8));
        pnlRight.setBackground(Color.WHITE);
 
        // Form thông tin chung
        JPanel pnlInfo = new JPanel(new GridBagLayout());
        pnlInfo.setBorder(BorderFactory.createTitledBorder(" Thông tin mẫu "));
        pnlInfo.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 5, 3, 5);
        g.fill = GridBagConstraints.HORIZONTAL;
 
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        pnlInfo.add(new JLabel("Tên mẫu liều:"), g);
        g.gridx = 1; g.weightx = 1;
        txtTenComboNangCao = new JTextField();
        pnlInfo.add(txtTenComboNangCao, g);
 
        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        pnlInfo.add(new JLabel("Nhóm bệnh:"), g);
        g.gridx = 1; g.weightx = 1;
        // [FIX NHÓM BỆNH] Dùng danh sách chuẩn giống tab Sản phẩm
        cbNhomBenhLieuNangCao = new JComboBox<>();
        for (String nb : DANH_SACH_NHOM_BENH_LY) cbNhomBenhLieuNangCao.addItem(nb);
        cbNhomBenhLieuNangCao.setEditable(true);
        ((JTextField) cbNhomBenhLieuNangCao.getEditor().getEditorComponent())
            .addActionListener(ev -> {
                String newItem = cbNhomBenhLieuNangCao.getEditor().getItem().toString().trim();
                if (!newItem.isEmpty()) {
                    boolean exists = false;
                    for (int i = 0; i < cbNhomBenhLieuNangCao.getItemCount(); i++)
                        if (cbNhomBenhLieuNangCao.getItemAt(i).equalsIgnoreCase(newItem)) { exists = true; break; }
                    if (!exists) {
                        cbNhomBenhLieuNangCao.addItem(newItem);
                        cbNhomBenhLieuNangCao.setSelectedItem(newItem);
                    }
                }
            });
        pnlInfo.add(cbNhomBenhLieuNangCao, g);
 
        // Lọc danh sách khi chọn nhóm bệnh
        cbNhomBenhLieuNangCao.addActionListener(ev -> {
            if (dangCapNhatBangLieu) return;
            Object selected = cbNhomBenhLieuNangCao.getSelectedItem();
            if (selected == null) return;
            String nhomLoc = selected.toString().trim();
            dangCapNhatBangLieu = true;
            mdlDanhSachLieuNangCao.setRowCount(0);
            for (Object[] r : new BUS_SanPham().layDanhSachComboNangCao()) {
            	if (nhomLoc.isEmpty() || r[2].toString().equalsIgnoreCase(nhomLoc)) {
                    mdlDanhSachLieuNangCao.addRow(new Object[]{
                        r[0], r[1], r[2], String.format("%,.0f đ", (Double) r[3])});
                }
            }
            dangCapNhatBangLieu = false;
        });
 
        g.gridx = 0; g.gridy = 2; g.weightx = 0;
        pnlInfo.add(new JLabel("Giá combo:"), g);
        g.gridx = 1; g.weightx = 1;
        txtGiaBanComboNangCao = new JFormattedTextField(java.text.NumberFormat.getIntegerInstance());
        txtGiaBanComboNangCao.setValue(0L);
        txtGiaBanComboNangCao.setEditable(false);
        txtGiaBanComboNangCao.setBackground(Color.decode("#F3F4F6"));
        pnlInfo.add(txtGiaBanComboNangCao, g);
 
        g.gridx = 0; g.gridy = 3; g.weightx = 0;
        pnlInfo.add(new JLabel("Ghi chú:"), g);
        g.gridx = 1; g.weightx = 1;
        txtGhiChuLieuNangCao = new JTextField();
        pnlInfo.add(txtGhiChuLieuNangCao, g);
 
        g.gridx = 0; g.gridy = 4; g.weightx = 0;
        pnlInfo.add(new JLabel("Mã Hệ thống:"), g);
        g.gridx = 1;
        lblComboIdNangCao = new JLabel("(Tự động sinh)");
        lblComboIdNangCao.setForeground(Color.GRAY);
        pnlInfo.add(lblComboIdNangCao, g);
 
        pnlRight.add(pnlInfo, BorderLayout.NORTH);
 
        // ── FORM NHẬP THUỐC NHANH + BẢNG CHI TIẾT LIỀU ──────────────────────
        JPanel pnlDetailWrap = new JPanel(new BorderLayout(0, 6));
        pnlDetailWrap.setBorder(BorderFactory.createTitledBorder(" Chi tiết phối liều lượng "));
        pnlDetailWrap.setBackground(Color.WHITE);
 
        JPanel pnlAddBar = new JPanel(new GridBagLayout());
        pnlAddBar.setBackground(Color.decode("#F8FAFC"));
        GridBagConstraints g2 = new GridBagConstraints();
        g2.insets = new Insets(3, 3, 3, 3);
        g2.fill = GridBagConstraints.HORIZONTAL;
 
        // [FIX TÌM KIẾM] Dùng WatermarkTextField thuần + popup riêng (giống sidebar SP)
        g2.gridx = 0; g2.gridy = 0; g2.weightx = 0;
        pnlAddBar.add(new JLabel("Tìm thuốc:"), g2);
 
        txtSearchLieuNangCao = new WatermarkTextField("Nhập tên thuốc");
        g2.gridx = 1; g2.weightx = 1;
        pnlAddBar.add(txtSearchLieuNangCao, g2);
 
        g2.gridx = 2; g2.weightx = 0;
        pnlAddBar.add(new JLabel("ĐVT:"), g2);
        g2.gridx = 3;
        lblDVTLieu = new JLabel("---");
        lblDVTLieu.setForeground(Color.BLUE);
        pnlAddBar.add(lblDVTLieu, g2);
 
        g2.gridx = 4;
        pnlAddBar.add(new JLabel("Giá lẻ:"), g2);
        g2.gridx = 5;
        lblGiaLieu = new JLabel("---");
        lblGiaLieu.setForeground(Color.decode("#15803D"));
        pnlAddBar.add(lblGiaLieu, g2);
 
        // Dòng liều dùng từng buổi
        JPanel pnlDoses = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlDoses.setOpaque(false);
        pnlDoses.add(new JLabel("Sáng:"));  pnlDoses.add(txtSangLieu  = new JTextField("1", 3));
        pnlDoses.add(new JLabel("Trưa:"));  pnlDoses.add(txtTruaLieu  = new JTextField("0", 3));
        pnlDoses.add(new JLabel("Chiều:")); pnlDoses.add(txtChieuLieu = new JTextField("0", 3));
        pnlDoses.add(new JLabel("Tối:"));   pnlDoses.add(txtToiLieu   = new JTextField("1", 3));
        pnlDoses.add(new JLabel("Số ngày:")); pnlDoses.add(txtSoNgayLieu = new JTextField("5", 3));
 
        g2.gridx = 0; g2.gridy = 1; g2.gridwidth = 6; g2.weightx = 1;
        pnlAddBar.add(pnlDoses, g2);
        g2.gridwidth = 1;
 
        g2.gridx = 0; g2.gridy = 2; g2.weightx = 0;
        pnlAddBar.add(new JLabel("Cách dùng:"), g2);
        cbCachDungLieuNangCao = new JComboBox<>(new String[]{
            "Sau ăn 30 phút", "Trước ăn 30 phút", "Trong bữa ăn", "Lúc đói"});
        cbCachDungLieuNangCao.setEditable(true);
        g2.gridx = 1; g2.weightx = 1;
        pnlAddBar.add(cbCachDungLieuNangCao, g2);
 
        JButton btnThemThuoc = createBtnWithIcon("+ Đưa vào toa", "#3B82F6", null);
        btnThemThuoc.addActionListener(e -> themThuocVaoDocLieu());
        g2.gridx = 2; g2.gridwidth = 4; g2.weightx = 0;
        pnlAddBar.add(btnThemThuoc, g2);
        g2.gridwidth = 1;
 
        pnlDetailWrap.add(pnlAddBar, BorderLayout.NORTH);
 
        // Bảng chi tiết liều 12 cột
        String[] colsChiTiet = {"Mã SP", "Tên Thuốc", "ĐVT",
            "Sáng", "Trưa", "Chiều", "Tối", "Cách dùng", "Ngày", "Tổng SL", "Thành tiền", "Xóa"};
        modChiTietLieu = new DefaultTableModel(colsChiTiet, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return c >= COL_L_SANG && c <= COL_L_NGAY;
            }
        };
        tblChiTietLieu = new JTable(modChiTietLieu);
        tblChiTietLieu.setRowHeight(26);
        modChiTietLieu.addTableModelListener(this::onTableCellLieuChanged);
 
        tblChiTietLieu.getColumnModel().getColumn(COL_L_XOA)
            .setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                	JLabel lbl = new JLabel(new MenuIcon("TRASH", 18, Color.decode("#EF4444")));
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                    wrap.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                    wrap.add(lbl);
                    return wrap;
                }
            });
 
        tblChiTietLieu.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int viewRow = tblChiTietLieu.rowAtPoint(e.getPoint());
                int col     = tblChiTietLieu.columnAtPoint(e.getPoint());
                if (viewRow >= 0 && col == COL_L_XOA) {
                    // [FIX 4] Chuyển index giao diện → index model để tránh IndexOutOfBounds khi sort
                    int row = tblChiTietLieu.convertRowIndexToModel(viewRow);
                    if (row < 0 || row >= dsChiTietLieuLuuTru.size()) return;
                    dsChiTietLieuLuuTru.remove(row);
                    dangCapNhatBangLieu = true;
                    modChiTietLieu.removeRow(row);
                    dangCapNhatBangLieu = false;
                    capNhatTongTienLieuHienTai();
                }
            }
        });
 
        pnlDetailWrap.add(new JScrollPane(tblChiTietLieu), BorderLayout.CENTER);
        pnlRight.add(pnlDetailWrap, BorderLayout.CENTER);
 
        // Footer
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(Color.WHITE);
 
        JPanel pnlFLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlFLeft.setOpaque(false);
        pnlFLeft.add(new JLabel("Tổng thành tiền: "));
        lblTongTienLieuNangCao = new JLabel("0 đ");
        lblTongTienLieuNangCao.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTongTienLieuNangCao.setForeground(Color.RED);
        pnlFLeft.add(lblTongTienLieuNangCao);
 
        // [FIX GIÁ ĐỀ XUẤT] Thêm tooltip giải thích rõ tác dụng
        JButton btnDeXuat = createBtnWithIcon("Lấy giá đề xuất", "#F59E0B", new MenuIcon("CHART", 16));
        btnDeXuat.setToolTipText(
            "<html><b>Lấy giá đề xuất</b><br>"
            + "Tính tổng thành tiền của tất cả thuốc trong toa,<br>"
            + "rồi điền tự động vào ô <i>Giá combo</i> ở trên.<br>"
            + "Bạn có thể chỉnh lại giá bán thực tế sau đó.</html>");
        pnlFooter.add(pnlFLeft, BorderLayout.WEST);
 
        JPanel pnlFRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlFRight.setOpaque(false);
        JButton btnTaoMoi = createBtnWithIcon("Tạo mẫu mới", "#10B981", new MenuIcon("ADD", 16));
        btnTaoMoi.addActionListener(e -> resetFormSoanThaoLieu());
        JButton btnLuuLieu = createBtnWithIcon("Lưu mẫu liều", "#2563EB", new MenuIcon("SAVE", 16));
        btnLuuLieu.addActionListener(e -> luuMauLieuChuyenSau());
        pnlFRight.add(btnTaoMoi);
        pnlFRight.add(btnLuuLieu);
        pnlFooter.add(pnlFRight, BorderLayout.EAST);
 
        pnlRight.add(pnlFooter, BorderLayout.SOUTH);
 
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlLeft, pnlRight);
        split.setDividerLocation(340);
        pnlMain.add(split, BorderLayout.CENTER);
 
        // Khởi tạo popup tìm kiếm thuốc (method mới bên dưới)
        khoiTaoPopupTimThuoc();
        hookAutocompleteLieuNangCao();
        loadDanhSachComboNangCaoRaGiaoDien();
 
        return pnlMain;
    }
    private void khoiTaoPopupTimThuoc() {
        modelPopupThuoc = new javax.swing.DefaultListModel<>();
        listPopupThuoc  = new javax.swing.JList<>(modelPopupThuoc);
        listPopupThuoc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listPopupThuoc.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listPopupThuoc.setFixedCellHeight(28);
        listPopupThuoc.setBackground(Color.WHITE);
 
        listPopupThuoc.setCellRenderer(new javax.swing.ListCellRenderer<String>() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<? extends String> list,
                    String value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = new JLabel(value == null ? "" : value);
                lbl.setBorder(new EmptyBorder(3, 10, 3, 10));
                lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                lbl.setOpaque(true);
                lbl.setBackground(isSelected ? Color.decode("#DBEAFE") : Color.WHITE);
                lbl.setForeground(isSelected ? Color.decode("#1E3A8A") : Color.DARK_GRAY);
                return lbl;
            }
        });
 
        JScrollPane spPopup = new JScrollPane(listPopupThuoc,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        spPopup.setBorder(BorderFactory.createLineBorder(Color.decode("#CBD5E1")));
 
        popupTimThuoc = new javax.swing.JPopupMenu();
        popupTimThuoc.setLayout(new BorderLayout());
        popupTimThuoc.add(spPopup, BorderLayout.CENTER);
        popupTimThuoc.setBorder(BorderFactory.createLineBorder(Color.decode("#93C5FD"), 1));
 
        // Chọn item bằng chuột
        listPopupThuoc.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int idx = listPopupThuoc.locationToIndex(e.getPoint());
                if (idx >= 0 && idx < cacheKetQuaTimKiemLieu.size()) {
                    chonThuocTuPopup(idx);
                }
            }
        });
 
        // Điều hướng bàn phím trong txtSearch → popup
        txtSearchLieuNangCao.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (!popupTimThuoc.isVisible()) return;
                int cur = listPopupThuoc.getSelectedIndex();
                int size = modelPopupThuoc.getSize();
                switch (e.getKeyCode()) {
                    case java.awt.event.KeyEvent.VK_DOWN:
                        listPopupThuoc.setSelectedIndex(Math.min(cur + 1, size - 1));
                        listPopupThuoc.ensureIndexIsVisible(listPopupThuoc.getSelectedIndex());
                        e.consume();
                        break;
                    case java.awt.event.KeyEvent.VK_UP:
                        listPopupThuoc.setSelectedIndex(Math.max(cur - 1, 0));
                        listPopupThuoc.ensureIndexIsVisible(listPopupThuoc.getSelectedIndex());
                        e.consume();
                        break;
                    case java.awt.event.KeyEvent.VK_ENTER:
                        if (cur >= 0 && cur < cacheKetQuaTimKiemLieu.size()) {
                            chonThuocTuPopup(cur);
                        }
                        e.consume();
                        break;
                    case java.awt.event.KeyEvent.VK_ESCAPE:
                        popupTimThuoc.setVisible(false);
                        e.consume();
                        break;
                }
            }
        });
 
        // Ẩn popup khi mất focus — dùng isAncestorOf thay vì hasFocus
        // vì JPopupMenu lấy focus của window nên listPopupThuoc.hasFocus() luôn false
        txtSearchLieuNangCao.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                java.awt.Component opposite = e.getOppositeComponent();
                // Chỉ ẩn nếu focus KHÔNG chuyển sang list trong popup
                if (opposite == null || !SwingUtilities.isDescendingFrom(opposite, popupTimThuoc)) {
                    SwingUtilities.invokeLater(() -> popupTimThuoc.setVisible(false));
                }
            }
        });

        // Ngăn popup cướp focus khỏi txtSearch khi hiển thị
        popupTimThuoc.setFocusable(false);
        listPopupThuoc.setFocusable(false);
    }
 
    /** Người dùng chọn thuốc ở popup index idx — cập nhật state và đóng popup */
    private void chonThuocTuPopup(int idx) {
        if (idx < 0 || idx >= cacheKetQuaTimKiemLieu.size()) return;
        Object[] r = cacheKetQuaTimKiemLieu.get(idx);
        maSPDangChonLieu  = r[0].toString();
        tenSPDangChonLieu = r[1].toString();
        dvtDangChonLieu   = r[2].toString();
        giaDangChonLieu   = (Double) r[3];

        lblDVTLieu.setText(dvtDangChonLieu);
        lblGiaLieu.setText(String.format("%,.0f đ", giaDangChonLieu));

        dangChonTuPopup = true;
        txtSearchLieuNangCao.setText(tenSPDangChonLieu + " | " + dvtDangChonLieu);
        dangChonTuPopup = false;
        SwingUtilities.invokeLater(() -> themThuocVaoDocLieu());

        popupTimThuoc.setVisible(false);
        txtSearchLieuNangCao.requestFocus();
    }

    // =========================================================================
    // TOPBAR
    // =========================================================================
    private JPanel createTopbar() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
            new EmptyBorder(15, 20, 15, 20)));

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlLeft.setOpaque(false);
        JLabel lblBoxIcon = new JLabel(new MenuIcon("PACKAGE"));
        lblBoxIcon.setForeground(COLOR_PRIMARY);
        pnlLeft.add(lblBoxIcon);
        pnlLeft.add(new JLabel("<html><b style='color:#1E3A8A; font-size:16px;'>QUẢN LÝ SẢN PHẨM</b></html>"));
        lblTotalTopBar = new JLabel("(0 sản phẩm)");
        pnlLeft.add(lblTotalTopBar);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlRight.setOpaque(false);
        pnlRight.add(new JLabel("Tìm kiếm: "));
        txtTimKiem = new WatermarkTextField("Tên, mã, mã vạch, hoạt chất...");
        txtTimKiem.setPreferredSize(new Dimension(200, 36));
        pnlRight.add(txtTimKiem);

        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { doSearchAndAutoOpen(); }
            @Override public void removeUpdate(DocumentEvent e)  { doSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { doSearchAndAutoOpen(); }
        });

        JButton btnTim     = createBtnWithIcon("Tìm",        "#1D68B2", new MenuIcon("SEARCH"));  btnTim.setPreferredSize(new Dimension(95, 36));
        JButton btnLamMoi  = createBtnWithIcon("Làm mới",    "#64748B", new MenuIcon("REFRESH")); btnLamMoi.setPreferredSize(new Dimension(115, 36));
        JButton btnXuatTop = createBtnWithIcon("Xuất Excel", "#22C55E", new MenuIcon("EXPORT"));  btnXuatTop.setPreferredSize(new Dimension(130, 36));
        JButton btnNhapTop = createBtnWithIcon("Nhập Excel", "#0EA5E9", new MenuIcon("IMPORT"));  btnNhapTop.setPreferredSize(new Dimension(130, 36));
        JButton btnThem    = createBtnWithIcon("Thêm mới",   "#E11D48", new MenuIcon("ADD"));     btnThem.setPreferredSize(new Dimension(125, 36));

        this.topBtnXuat = btnXuatTop;
        this.topBtnNhap = btnNhapTop;
        this.topBtnThem = btnThem;

        btnTim.addActionListener(e -> doSearch());
        btnLamMoi.addActionListener(e -> {
            txtTimKiem.setText("");
            bgLoai.getElements().nextElement().setSelected(true);
            bgDang.getElements().nextElement().setSelected(true);
            bgNhom.getElements().nextElement().setSelected(true);
            doSearch();
            setDetailVisible(false);
        });
        btnXuatTop.addActionListener(e -> thucHienXuatExcelThang());
        btnNhapTop.addActionListener(e -> thucHienNhapExcelThang());
        btnThem.addActionListener(e -> batDauThemMoi());

        pnlRight.add(btnTim); pnlRight.add(btnLamMoi);
        pnlRight.add(btnXuatTop); pnlRight.add(btnNhapTop);
        pnlRight.add(btnThem);
        pnl.add(pnlLeft); pnl.add(Box.createHorizontalGlue()); pnl.add(pnlRight);
        return pnl;
    }

    // =========================================================================
    // SEARCH
    // =========================================================================
    private void doSearch() {
        String keyword    = txtTimKiem.getText().toLowerCase().trim();
        String loaiSelect = getSelectedRadioText(bgLoai);
        String dangSelect = getSelectedRadioText(bgDang);
        String nhomSelect = getSelectedRadioText(bgNhom);
        currentFilteredData.clear();
        for (Object[] row : allDataMock) {
            // row: [0]=mã, [1]=tên, [2]=loại, [3]=hoạtChất, [4]=dạng, [5]=nsx, [6]=vat, [7]=giaBan, [8]=maVach, [9]=nhomBenhLy, [10]=tenVietTat
            boolean matchKey = keyword.isEmpty()
                || row[0].toString().toLowerCase().contains(keyword)
                || row[1].toString().toLowerCase().contains(keyword)
                || row[3].toString().toLowerCase().contains(keyword)
                || (row[8] != null && row[8].toString().toLowerCase().contains(keyword))
                // FIX #5: tìm theo tên viết tắt (tenVietTat ở index [10])
                || (row.length > 10 && row[10] != null && row[10].toString().toLowerCase().contains(keyword));
            boolean matchLoai = loaiSelect.equals("Tất cả") || row[2].toString().equalsIgnoreCase(loaiSelect);
            boolean matchDang = dangSelect.equals("Tất cả") || (row.length > 4 && row[4].toString().equalsIgnoreCase(dangSelect));
            boolean matchNhom = nhomSelect.equals("Tất cả") || (row.length > 9 && row[9].toString().equalsIgnoreCase(nhomSelect));
            if (matchKey && matchLoai && matchDang && matchNhom) currentFilteredData.add(row);
        }
        currentPageMock = 1;
        updatePagination();
    }

    private void doSearchAndAutoOpen() {
        doSearch();
        String kw = txtTimKiem.getText().trim();
        if (kw.length() >= 6 && currentFilteredData.size() == 1) {
            Object[] row = currentFilteredData.get(0);
            String maVachSP = row.length > 8 ? row[8].toString() : "";
            boolean isExactMatch = false;
            for (String mv : maVachSP.split(",")) {
                if (mv.trim().equalsIgnoreCase(kw)) { isExactMatch = true; break; }
            }
            if (isExactMatch) {
                // Tim index trong currentFilteredData
                final int rowIdx = currentFilteredData.indexOf(row);
                SwingUtilities.invokeLater(() -> {
                    if (rowIdx >= 0) {
                        hienThiChiTietSanPham(rowIdx);
                        txtTimKiem.setText("");
                    }
                });
            }
        }
    }

    private String getSelectedRadioText(ButtonGroup bg) {
        for (Enumeration<AbstractButton> buttons = bg.getElements(); buttons.hasMoreElements();) {
            AbstractButton b = buttons.nextElement();
            if (b.isSelected()) return b.getText();
        }
        return "Tất cả";
    }

    // =========================================================================
    // SIDEBAR FILTER — CÓ NHÓM BỆNH LÝ VÀ CÁC ACCORDION THU GỌN
    // =========================================================================
    private JPanel createSidebarFilter() {
        // Outer: BorderLayout — accordion cuộn ở CENTER, checkbox pin ở SOUTH
        JPanel outer = new JPanel(new BorderLayout());
        outer.setPreferredSize(new Dimension(205, 0));
        outer.setMaximumSize(new Dimension(205, Integer.MAX_VALUE));
        outer.setMinimumSize(new Dimension(205, 0));
        outer.setBackground(Color.WHITE);
        outer.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, COLOR_BORDER));

        // scrollContent: chứa 3 accordion — có thể cuộn khi mở hết
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(Color.WHITE);

        // THỨ TỰ: Nhóm bệnh lý → Loại → Dạng bào chế (tất cả đóng mặc định)
        scrollContent.add(createAccordionHeader("Nhóm bệnh lý", false, () -> toggleAccordion("nhom")));
        bgNhom = new ButtonGroup();
        pnlNhomBody = createRadioGroupPanel(buildNhomOptions(), bgNhom);
        pnlNhomBody.setVisible(false);
        scrollContent.add(pnlNhomBody);

        scrollContent.add(createAccordionHeader("Loại", false, () -> toggleAccordion("loai")));
        bgLoai = new ButtonGroup();
        pnlLoaiBody = createRadioGroupPanel(
            new String[]{"Tất cả","Thuốc kê đơn","Thuốc không kê đơn","Sản phẩm chức năng","Mỹ phẩm"}, bgLoai);
        pnlLoaiBody.setVisible(false);
        scrollContent.add(pnlLoaiBody);

        scrollContent.add(createAccordionHeader("Dạng bào chế", false, () -> toggleAccordion("dang")));
        bgDang = new ButtonGroup();
        pnlDangBody = createRadioGroupPanel(
            new String[]{"Tất cả","Viên nén","Viên nang","Viên sủi","Thuốc bột","Kẹo ngậm","Dung dịch","Hỗn dịch","Thuốc nhỏ giọt","Súc miệng"}, bgDang);
        pnlDangBody.setVisible(false);
        scrollContent.add(pnlDangBody);
        scrollContent.add(Box.createVerticalGlue());

        // JScrollPane bọc scrollContent — cuộn dọc khi cần, không cuộn ngang
        JScrollPane sp = new JScrollPane(scrollContent,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        // SOUTH PIN: "Sản phẩm đã ẩn" — luôn hiển thị dù cuộn
        JPanel pnlBottom = new JPanel();
        pnlBottom.setLayout(new BoxLayout(pnlBottom, BoxLayout.Y_AXIS));
        pnlBottom.setBackground(Color.WHITE);
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(185, 1));
        sep.setForeground(COLOR_BORDER);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlBottom.add(sep);
        chkThungRac = new javax.swing.JCheckBox("Sản phẩm đã ẩn");
        chkThungRac.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        chkThungRac.setBackground(Color.WHITE);
        chkThungRac.setBorder(new EmptyBorder(6, 15, 6, 0));
        chkThungRac.setFocusPainted(false);
        chkThungRac.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkThungRac.addActionListener(e -> {
            if (chkThungRac.isSelected()) {
                loadDataThungRac();
                btnXoaBottom.setText("Khôi phục");
                btnXoaBottom.setBackground(Color.decode("#10B981"));
            } else {
                loadDataFromDatabase();
                btnXoaBottom.setText("Ẩn sản phẩm");
                btnXoaBottom.setBackground(Color.decode("#F59E0B"));
            }
        });
        pnlBottom.add(chkThungRac);

        outer.add(sp, BorderLayout.CENTER);
        outer.add(pnlBottom, BorderLayout.SOUTH);
        return outer;
    }

    // =========================================================================
    // ACCORDION HEADER — gradient hover + gạch chân đẹp
    // =========================================================================
    private JPanel createAccordionHeader(String title, boolean expanded, Runnable onToggle) {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? Color.decode("#F0F6FF") : Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                if (hovered) {
                    g2.setColor(Color.decode("#1E3A8A"));
                    g2.fillRect(0, 0, 3, getHeight());
                }
                g2.setColor(Color.decode("#E2E8F0"));
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBackground(Color.WHITE);
        wrapper.setCursor(new Cursor(Cursor.HAND_CURSOR));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        wrapper.setMinimumSize(new Dimension(0, 46));
        wrapper.setPreferredSize(new Dimension(215, 46));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setBorder(new EmptyBorder(0, 14, 0, 12));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(Color.decode("#1E293B"));
        lblTitle.setPreferredSize(new Dimension(150, 46));

        JLabel lblArrow = new JLabel(expanded ? "▲" : "▼") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#64748B"));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        lblArrow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblArrow.setHorizontalAlignment(SwingConstants.CENTER);
        lblArrow.setPreferredSize(new Dimension(28, 46));
        lblArrow.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if ("Loại".equals(title))              btnToggleLoai = makeProxyButton(lblArrow);
        else if ("Dạng bào chế".equals(title)) btnToggleDang = makeProxyButton(lblArrow);
        else if ("Nhóm bệnh lý".equals(title)) btnToggleNhom = makeProxyButton(lblArrow);

        wrapper.add(lblTitle, BorderLayout.CENTER);
        wrapper.add(lblArrow, BorderLayout.EAST);

        MouseAdapter clickHandler = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onToggle.run(); }
        };
        wrapper.addMouseListener(clickHandler);
        lblTitle.addMouseListener(clickHandler);
        lblArrow.addMouseListener(clickHandler);
        return wrapper;
    }

    /** Proxy JButton chỉ để forward setText() sang JLabel mũi tên */
    private JButton makeProxyButton(JLabel arrowLabel) {
        return new JButton() {
            @Override public void setText(String text) {
                super.setText(text);
                arrowLabel.setText(text);
                arrowLabel.repaint();
            }
        };
    }

    /**
     * Toggle mở/đóng một accordion section
     */
    private void toggleAccordion(String which) {
        switch (which) {
            case "loai":
                loaiExpanded = !loaiExpanded;
                pnlLoaiBody.setVisible(loaiExpanded);
                if (btnToggleLoai != null) btnToggleLoai.setText(loaiExpanded ? "▲" : "▼");
                break;
            case "dang":
                dangExpanded = !dangExpanded;
                pnlDangBody.setVisible(dangExpanded);
                if (btnToggleDang != null) btnToggleDang.setText(dangExpanded ? "▲" : "▼");
                break;
            case "nhom":
                nhomExpanded = !nhomExpanded;
                pnlNhomBody.setVisible(nhomExpanded);
                if (btnToggleNhom != null) btnToggleNhom.setText(nhomExpanded ? "▲" : "▼");
                break;
        }
        // Buộc JScrollPane tính lại preferred height của scrollContent
        if (pnlLoaiBody.getParent() != null) {
            pnlLoaiBody.getParent().revalidate();
            pnlLoaiBody.getParent().repaint();
        }
        revalidate(); repaint();
    }

    /**
     * Tạo panel radio buttons (dùng chung cho cả 3 nhóm)
     */
    private JPanel createRadioGroupPanel(String[] options, ButtonGroup group) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(2, 0, 6, 0));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int i = 0; i < options.length; i++) {
            JRadioButton rb = new JRadioButton(options[i]);
            rb.setBackground(Color.WHITE);
            rb.setFont(FONT_NORMAL);
            rb.setBorder(new EmptyBorder(4, 20, 4, 0));
            rb.setFocusPainted(false);
            if (i == 0) { rb.setSelected(true); rb.setForeground(Color.decode("#1D68B2")); rb.setFont(FONT_BOLD); }
            rb.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    rb.setForeground(Color.decode("#1D68B2")); rb.setFont(FONT_BOLD); doSearch();
                } else {
                    rb.setForeground(Color.BLACK); rb.setFont(FONT_NORMAL);
                }
            });
            group.add(rb); p.add(rb);
        }
        return p;
    }

    private String[] buildNhomOptions() {
        // Lấy từ DB để luôn đồng bộ với dữ liệu thực tế
        List<String> fromDB = new BUS_SanPham().layDanhSachNhomBenhLy();
        List<String> opts = new ArrayList<>();
        opts.add("Tất cả");
        for (String nb : fromDB) {
            if (!opts.contains(nb)) opts.add(nb);
        }
        if (opts.size() == 1) {
            for (String nb : DANH_SACH_NHOM_BENH_LY) opts.add(nb);
        }
        return opts.toArray(new String[0]);
    }

    // =========================================================================
    // BẢNG DANH SÁCH — CÓ CỘT MÃ VẠCH VÀ NHÓM BỆNH LÝ
    // =========================================================================
    private JPanel createCenterList() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);

        JLabel lblHeader = new JLabel("  Danh sách sản phẩm");
        lblHeader.setOpaque(true);
        lblHeader.setBackground(COLOR_HEADER_TABLE);
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(FONT_BOLD);
        lblHeader.setPreferredSize(new Dimension(0, 38));
        pnl.add(lblHeader, BorderLayout.NORTH);

        modelSanPham = new DefaultTableModel(COLS_EXPANDED, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblSanPham = new JTable(modelSanPham);
        setupTableStyle(tblSanPham);
        tblSanPham.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        applyTableCellRenderers();

        // Độ rộng cột: Mã, Mã vạch, Nhóm bệnh lý, Tên, Loại, Hoạt chất, Dạng, NSX, VAT, Giá bán
        int[] widths = {90, 100, 140, 200, 120, 150, 100, 120, 50, 90};
        for (int i = 0; i < widths.length && i < tblSanPham.getColumnCount(); i++)
            tblSanPham.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

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

        // Pagination
        JPanel pnlPage = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        pnlPage.setBackground(Color.WHITE);
        btnPrev = new JButton("< Trang trước"); stylePageBtn(btnPrev);
        btnNext = new JButton("Trang tiếp >");  stylePageBtn(btnNext);
        cbLimit = new JComboBox<>(new String[]{"20", "30", "50"});
        cbLimit.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbLimit.setBackground(Color.WHITE);
        lblPage = new JLabel("Trang 1/1 (Tổng: 0)");
        lblPage.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        btnPrev.addActionListener(e -> { if (currentPageMock > 1) { currentPageMock--; updatePagination(); } });
        btnNext.addActionListener(e -> {
            int max = (int) Math.ceil((double) currentFilteredData.size() / itemsPerPageMock);
            if (currentPageMock < max) { currentPageMock++; updatePagination(); }
        });
        cbLimit.addActionListener(e -> {
            itemsPerPageMock = Integer.parseInt(cbLimit.getSelectedItem().toString());
            currentPageMock = 1; updatePagination();
        });
        pnlPage.add(btnPrev); pnlPage.add(btnNext); pnlPage.add(cbLimit); pnlPage.add(lblPage);
        pnl.add(pnlPage, BorderLayout.SOUTH);
        return pnl;
    }

    private void stylePageBtn(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER),
            new EmptyBorder(5, 10, 5, 10)));
    }

    private void updatePagination() {
        if (lblTotalTopBar != null)
            lblTotalTopBar.setText("(" + allDataMock.size() + " sản phẩm)");

        int total      = currentFilteredData.size();
        int totalPages = (int) Math.ceil((double) total / itemsPerPageMock);
        if (totalPages == 0) totalPages = 1;
        if (currentPageMock > totalPages) currentPageMock = totalPages;

        lblPage.setText("Trang " + currentPageMock + "/" + totalPages + " (Tổng: " + total + ")");
        btnPrev.setEnabled(currentPageMock > 1);
        btnNext.setEnabled(currentPageMock < totalPages);

        int start = (currentPageMock - 1) * itemsPerPageMock;
        int end   = Math.min(start + itemsPerPageMock, total);
        modelSanPham.setRowCount(0);

        for (int i = start; i < end; i++) {
            Object[] row = currentFilteredData.get(i);
            // row: [0]=mã, [1]=tên, [2]=loại, [3]=hoạtChất, [4]=dạng, [5]=nsx, [6]=vat, [7]=giaBan, [8]=maVach, [9]=nhomBenhLy
            String maVach    = row.length > 8 ? row[8].toString() : "";
            String nhomBL    = row.length > 9 ? row[9].toString() : "";
            if (detailVisible) {
                // Collapsed: Mã, Mã vạch, Nhóm bệnh lý, Tên, Loại, Hoạt chất, Giá bán
                modelSanPham.addRow(new Object[]{row[0], maVach, nhomBL, row[1], row[2], row[3], row[7]});
            } else {
                // Expanded: Mã, Mã vạch, Nhóm bệnh lý, Tên, Loại, Hoạt chất, Dạng, NSX, VAT, Giá bán
                String dang   = row.length > 4 ? row[4].toString() : "";
                String nsx    = row.length > 5 ? row[5].toString() : "";
                String vat    = row.length > 6 ? row[6].toString() : "";
                String giaBan = row.length > 7 ? row[7].toString() : "0";
                modelSanPham.addRow(new Object[]{row[0], maVach, nhomBL, row[1], row[2], row[3], dang, nsx, vat, giaBan});
            }
        }
    }

    // =========================================================================
    // PANEL DETAIL BÊN PHẢI — FORM CÓ MÃ VẠCH + NHÓM BỆNH LÝ
    // =========================================================================
    private JPanel createRightDetail() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setPreferredSize(new Dimension(460, 0));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, COLOR_BORDER));

        // Header
        JPanel pnlH = new JPanel(new BorderLayout());
        pnlH.setBackground(COLOR_HEADER_TABLE);
        pnlH.setPreferredSize(new Dimension(0, 38));
        JLabel lblCT = new JLabel("  Thông tin sản phẩm");
        lblCT.setForeground(Color.WHITE); lblCT.setFont(FONT_BOLD);
        pnlH.add(lblCT, BorderLayout.WEST);
        JButton btnDong = new JButton("X ");
        btnDong.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnDong.setForeground(Color.WHITE);
        btnDong.setBackground(COLOR_HEADER_TABLE);
        btnDong.setBorder(null); btnDong.setFocusPainted(false);
        btnDong.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDong.addActionListener(e -> setDetailVisible(false));
        pnlH.add(btnDong, BorderLayout.EAST);
        pnl.add(pnlH, BorderLayout.NORTH);

        // Form scroll
        JPanel pnlScrollContent = new JPanel();
        pnlScrollContent.setLayout(new BoxLayout(pnlScrollContent, BoxLayout.Y_AXIS));
        pnlScrollContent.setBackground(Color.WHITE);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(10, 15, 0, 15));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(3, 5, 3, 5);
        g.weightx = 1.0;

        // Row 0: Tên SP (full width)
        addFormField(pnlForm, "Tên sản phẩm *", txtTen = new JTextField(), g, 0, 0, 2);

        // Row 2: Mã SP | Mã vạch 1D | Nhóm bệnh lý
        g.gridx = 0; g.gridy = 2; g.gridwidth = 1;
        pnlForm.add(new JLabel("Mã sản phẩm") {{ setFont(FONT_NORMAL); }}, g);
        g.gridx = 1;
        pnlForm.add(new JLabel("Mã vạch 1D (Barcode)") {{ setFont(FONT_NORMAL); }}, g);

        g.gridx = 0; g.gridy = 3; g.gridwidth = 1;
        txtId = new JTextField(); styleField(txtId); pnlForm.add(txtId, g);
        
        // --- GIAO DIỆN MÃ VẠCH + NÚT IN ---
        g.gridx = 1;
        JPanel pnlMaVachBox = new JPanel(new BorderLayout(5, 0));
        pnlMaVachBox.setBackground(Color.WHITE);
        
        txtMaVach = new JTextField(); 
        styleField(txtMaVach);
        txtMaVach.setToolTipText("Quét mã vạch hoặc để trống để hệ thống tự sinh mã");
        // Validate EAN-13 check digit khi nguoi dung nhap tay ma vach
        txtMaVach.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                String mv = txtMaVach.getText().trim();
                if (mv.isEmpty()) return;
                String digits = mv.replaceAll("[^0-9]", "");
                if (digits.length() == 13) {
                    String fixed = chuanHoaEAN13(mv);
                    if (fixed != null && !mv.equals(fixed)) {
                        showCustomNotification("CHECK DIGIT SAI",
                            "Ma vach " + mv + " co check digit sai! Da tu dong sua: " + fixed,
                            "WARNING");
                        txtMaVach.setText(fixed);
                    }
                } else if (digits.length() == 12) {
                    String fixed = chuanHoaEAN13(mv);
                    if (fixed != null) txtMaVach.setText(fixed);
                }
            }
        });
        
        btnInMaVach = new JButton("🖨"); 
        btnInMaVach.setPreferredSize(new Dimension(40, 32));
        btnInMaVach.setBackground(Color.WHITE);
        btnInMaVach.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        btnInMaVach.setFocusPainted(false);
        btnInMaVach.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Sự kiện: Bấm in sẽ tự sinh mã nếu trống và gọi lệnh in
        btnInMaVach.addActionListener(e -> {
            String maVach = txtMaVach.getText().trim();
            String maSP = txtId.getText().trim();
            String tenSP = txtTen.getText().trim();

            if (tenSP.isEmpty()) {
                showCustomNotification("Thông báo", "Vui lòng nhập tên sản phẩm trước khi in tem!", "WARNING");
                txtTen.requestFocus();
                return;
            }

            if (maVach.isEmpty()) {
                // Sinh ma noi bo EAN-13 hop le: prefix 200 (GS1 internal)
                String numOnly = "0";
                if (maSP.contains("-")) {
                    numOnly = maSP.split("-")[1].replaceAll("[^0-9]", "");
                } else {
                    numOnly = maSP.replaceAll("[^0-9]", "");
                }
                if (numOnly.isEmpty()) numOnly = "0";
                try {
                    String digits12 = String.format("200%09d", Long.parseLong(numOnly) % 1000000000L);
                    int chk = tinhCheckDigitEAN13(digits12);
                    maVach = digits12 + chk; // 13 digits EAN-13 hop le
                } catch (Exception ex) {
                    String digits12 = String.format("200%09d", System.currentTimeMillis() % 1000000000L);
                    int chk = tinhCheckDigitEAN13(digits12);
                    maVach = digits12 + chk;
                }
                txtMaVach.setText(maVach);
            }

            thucHienInMaVach1D(maVach, tenSP, txtGiaBan != null ? txtGiaBan.getText() : "0");
        });

        pnlMaVachBox.add(txtMaVach, BorderLayout.CENTER);
        pnlMaVachBox.add(btnInMaVach, BorderLayout.EAST);
        pnlForm.add(pnlMaVachBox, g);
        g.gridx = 0; g.gridy = 4; g.gridwidth = 1;
        pnlForm.add(new JLabel("Tên viết tắt") {{ setFont(FONT_NORMAL); }}, g);
        g.gridx = 1;
        pnlForm.add(new JLabel("Nhóm bệnh lý") {{ setFont(FONT_NORMAL); }}, g);

        g.gridx = 0; g.gridy = 5; g.gridwidth = 1;
        txtVietTat = new JTextField(); 
        styleField(txtVietTat); 
        pnlForm.add(txtVietTat, g);

        // Nhóm bệnh lý — dùng CardLayout (view/edit)
        cbNhomBenhLy = new JComboBox<>();
        cbNhomBenhLy.addItem("-- Chọn nhóm --");
        for (String nb : DANH_SACH_NHOM_BENH_LY) cbNhomBenhLy.addItem(nb);
        cbNhomBenhLy.addItem("+ Thêm nhóm mới...");
        applyFlatComboBoxStyle(cbNhomBenhLy);
        cbNhomBenhLy.addActionListener(e -> {
            Object sel = cbNhomBenhLy.getSelectedItem();
            if (sel != null && sel.toString().startsWith("+ Thêm nhóm")) {
                String tenMoi = showNhomBenhLyInputDialog(cbNhomBenhLy);
                if (tenMoi != null) {
                    // Chèn trước item "+ Thêm..."
                    cbNhomBenhLy.insertItemAt(tenMoi, cbNhomBenhLy.getItemCount() - 1);
                    cbNhomBenhLy.setSelectedItem(tenMoi);
                    // Đồng bộ sang combo bên tab mẫu liều
                    boolean existsLieu = false;
                    for (int i = 0; i < cbNhomBenhLieuNangCao.getItemCount(); i++) {
                        if (cbNhomBenhLieuNangCao.getItemAt(i).equalsIgnoreCase(tenMoi)) {
                            existsLieu = true; break;
                        }
                    }
                    if (!existsLieu) cbNhomBenhLieuNangCao.addItem(tenMoi);
                } else {
                    cbNhomBenhLy.setSelectedIndex(0);
                }
            }
        });

        txtNhomBenhLyView = createViewField();
        pnlNhomBenhLyCard = new JPanel(clNhomBenhLy = new CardLayout());
        pnlNhomBenhLyCard.add(txtNhomBenhLyView, "VIEW");
        pnlNhomBenhLyCard.add(cbNhomBenhLy, "EDIT");
        pnlNhomBenhLyCard.setPreferredSize(new Dimension(0, 32));
        g.gridx = 1; g.gridy = 5;
        pnlForm.add(pnlNhomBenhLyCard, g);

        // ── Vị trí thuốc ─────────────────────────────────────────────────────
        // Load từ DB dưới dạng Object[] [id, khu, ke, tang] — không cần class riêng
        loadDsViTri();
        cbViTri = new JComboBox<>();
        cbViTri.addItem("-- Chọn vị trí --");
        for (Object[] vt : dsViTriData) {
            String disp = vt[1] + " / " + vt[2] + (vt[3] != null && !vt[3].toString().isEmpty() ? " / " + vt[3] : "");
            cbViTri.addItem(disp);
        }
        applyFlatComboBoxStyle(cbViTri);
        txtViTriView = createViewField();
        pnlViTriCard = new JPanel(clViTri = new CardLayout());
        pnlViTriCard.add(txtViTriView, "VIEW");
        pnlViTriCard.add(cbViTri, "EDIT");
        pnlViTriCard.setPreferredSize(new Dimension(0, 32));

        // Vị trí thuốc — full-width, 2 rows riêng: label row 6, panel row 7
        g.gridx = 0; g.gridy = 6; g.gridwidth = 2; g.anchor = GridBagConstraints.WEST;
        JLabel lblViTri = new JLabel("Vị trí:");
        lblViTri.setFont(FONT_NORMAL); lblViTri.setForeground(Color.decode("#64748B"));
        pnlForm.add(lblViTri, g);
        g.gridy = 7;
        pnlViTriCard.setPreferredSize(new Dimension(0, 32));
        pnlForm.add(pnlViTriCard, g);
        g.gridwidth = 1; g.anchor = GridBagConstraints.CENTER; // reset

        // Row 6-7: Loại | Dạng bào chế
        cbLoaiCT = new JComboBox<>(new String[]{"Thuốc không kê đơn", "Thuốc kê đơn", "Sản phẩm chức năng", "Mỹ phẩm"});
        applyFlatComboBoxStyle(cbLoaiCT);
        cbDang = new JComboBox<>(new String[]{"Viên nén", "Viên nang", "Viên sủi", "Thuốc bột", "Kẹo ngậm", "Dung dịch", "Hỗn dịch", "Thuốc nhỏ giọt", "Súc miệng"});
        applyFlatComboBoxStyle(cbDang);
        cbNhaSX = new JComboBox<>(new String[]{"DHG Pharma", "Traphaco", "Sanofi", "Domesco"});
        applyFlatComboBoxStyle(cbNhaSX);
        cbDVT = new JComboBox<>(new String[]{"Viên", "Chai", "Hộp", "Vỉ", "Ống"});
        applyFlatComboBoxStyle(cbDVT);
        cbDVT.addActionListener(e -> {
            if (uomAutoChanging) return;
            String newDVT = cbDVT.getSelectedItem() != null ? cbDVT.getSelectedItem().toString() : "";
            txtDVTView.setText(newDVT); 

            if (modelDonVi.getRowCount() > 0) {
                uomAutoChanging = true;
                boolean updated = false;
                for (int i = 0; i < modelDonVi.getRowCount(); i++) {
                    Object tiLeObj = modelDonVi.getValueAt(i, 1);
                    if (tiLeObj != null && tiLeObj.toString().equals("1.0")) {
                        modelDonVi.setValueAt(newDVT, i, 0);
                        updated = true;
                        break;
                    }
                }
                if (!updated && !newDVT.isEmpty()) {
                    String giaTien = txtGiaBan != null && !txtGiaBan.getText().isEmpty() ? txtGiaBan.getText() : "0";
                    modelDonVi.insertRow(0, new Object[]{newDVT, "1.0", giaTien});
                }
                uomAutoChanging = false;
            }
        });

        cbLoaiCT.addActionListener(e -> {
            if (cbLoaiCT.getSelectedItem() != null && txtVAT != null) {
                String loai = cbLoaiCT.getSelectedItem().toString();
                txtVAT.setText(loai.contains("Thuốc") ? "5.0" : "8.0");
            }
        });

        pnlLoai = new JPanel(clLoai = new CardLayout()); txtLoaiView = createViewField(); pnlLoai.add(txtLoaiView, "VIEW"); pnlLoai.add(cbLoaiCT, "EDIT");
        pnlDang = new JPanel(clDang = new CardLayout()); txtDangView = createViewField(); pnlDang.add(txtDangView, "VIEW"); pnlDang.add(cbDang, "EDIT");
        pnlNSX  = new JPanel(clNSX  = new CardLayout()); txtNSXView  = createViewField(); pnlNSX.add(txtNSXView,  "VIEW"); pnlNSX.add(cbNhaSX, "EDIT");
        pnlDVT  = new JPanel(clDVT  = new CardLayout()); txtDVTView  = createViewField(); pnlDVT.add(txtDVTView,  "VIEW"); pnlDVT.add(cbDVT,   "EDIT");

        addCustomField(pnlForm, "Loại *",       pnlLoai, g, 0, 8);
        addCustomField(pnlForm, "Dạng bào chế", pnlDang, g, 1, 8);
        addFormField(pnlForm,   "Hoạt chất",    txtHoatChat = new JTextField(), g, 0, 10, 1);
        addFormField(pnlForm,   "Hàm lượng",    txtHamLuong = new JTextField(), g, 1, 10, 1);
        addCustomField(pnlForm, "Nhà sản xuất", pnlNSX,  g, 0, 12);
        addCustomField(pnlForm, "ĐVT gốc",      pnlDVT,  g, 1, 12);
        addFormField(pnlForm,   "VAT (%)",       txtVAT    = new JTextField(), g, 0, 14, 1);
        addFormField(pnlForm, "Giá bán (VNĐ) *",txtGiaBan = new JTextField(), g, 1, 14, 1);
        // Listener: khi giá thay đổi → cập nhật giaBanGoc và tính lại bảng đơn vị
        txtGiaBan.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusLost(java.awt.event.FocusEvent ev) { syncGiaBanVaoBangDonVi(); }
        });
        txtGiaBan.addActionListener(ev -> syncGiaBanVaoBangDonVi());
        txtGiaBan.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showMenhGiaDialog();
            }
        });
        // Tooltip hint
        txtGiaBan.setToolTipText("Double-click de chon menh gia");

        g.gridx = 0; g.gridy = 16; g.gridwidth = 2;
        JLabel lblMoTa = new JLabel("Mô tả"); lblMoTa.setFont(FONT_NORMAL);
        pnlForm.add(lblMoTa, g);
        g.gridy = 17;
        txtMoTa = new JTextArea(3, 0);
        txtMoTa.setFont(FONT_NORMAL); txtMoTa.setLineWrap(true); txtMoTa.setWrapStyleWord(true);
        txtMoTa.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(4, 8, 4, 8)));
        scrollMoTa = new JScrollPane(txtMoTa);
        scrollMoTa.setPreferredSize(new Dimension(0, 70));
        scrollMoTa.setBorder(null);
        applyThinScrollBar(scrollMoTa);
        pnlForm.add(scrollMoTa, g);

        pnlScrollContent.add(pnlForm);

        // Sub-table đơn vị quy đổi
        String[] colsDV = {"Đơn vị", "Tỷ lệ quy đổi", "Giá bán"};
        modelDonVi = new ToggleEditableTableModel(colsDV, 0);
        tblDonVi = new JTable(modelDonVi);
        setupTableStyle(tblDonVi);
        tblDonVi.setFillsViewportHeight(true);
        DefaultTableCellRenderer centerDV = new DefaultTableCellRenderer();
        centerDV.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tblDonVi.getColumnCount(); i++)
            tblDonVi.getColumnModel().getColumn(i).setCellRenderer(centerDV);

        modelDonVi.addTableModelListener(e -> {
            if (uomAutoChanging) return;
            
            // THÊM DÒNG NÀY: Bỏ qua nếu là sự kiện xóa dòng (setRowCount = 0)
            if (e.getType() == javax.swing.event.TableModelEvent.DELETE) return; 

            int row = e.getFirstRow();
            int col = e.getColumn();
            
            // THÊM ĐIỀU KIỆN NÀY: Đảm bảo dòng hợp lệ
            if (row < 0 || row >= modelDonVi.getRowCount()) return; 

            // Auto-thêm dòng trống khi dòng cuối có tên
            Object tenVal = modelDonVi.getValueAt(row, 0);
            if (tenVal != null && !tenVal.toString().trim().isEmpty() && row == modelDonVi.getRowCount() - 1) {
                uomAutoChanging = true;
                modelDonVi.addRow(new Object[3]);
                uomAutoChanging = false;
            }

            // Khi tỷ lệ (col 1) thay đổi → tự tính giá = giaBanGoc × tỷ lệ
            // Col 1 (ty le) thay doi -> tinh lai gia = giaBanGoc x ty le
            if (col == 1 && giaBanGoc > 0) {
                Object tiLeObj = modelDonVi.getValueAt(row, 1);
                if (tiLeObj != null && !tiLeObj.toString().trim().isEmpty()) {
                    try {
                        double tiLe = Double.parseDouble(tiLeObj.toString().trim());
                        if (tiLe > 0) {
                            uomAutoChanging = true;
                            long giaAuto = Math.round(giaBanGoc * tiLe);
                            modelDonVi.setValueAt(String.format("%,.0f", (double)giaAuto), row, 2);
                            uomAutoChanging = false;
                        }
                    } catch (Exception ignored) {}
                }
            }
            // FIX: Col 2 (gia) cua don vi co ban (tiLe=1.0) thay doi -> sync len txtGiaBan
            if (col == 2) {
                Object tiLeObj = modelDonVi.getValueAt(row, 1);
                Object giaObj  = modelDonVi.getValueAt(row, 2);
                if (tiLeObj != null && giaObj != null) {
                    try {
                        double tiLe = Double.parseDouble(tiLeObj.toString().trim());
                        if (tiLe == 1.0 && txtGiaBan != null) {
                            String giaStr = giaObj.toString().replace(",", "").trim();
                            double gia = Double.parseDouble(giaStr);
                            if (gia > 0) {
                                uomAutoChanging = true;
                                giaBanGoc = gia;
                                txtGiaBan.setText(String.valueOf((long)gia));
                                uomAutoChanging = false;
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        });

        JPanel pnlDVWrap = new JPanel(new BorderLayout());
        pnlDVWrap.setBorder(new EmptyBorder(10, 15, 5, 15));
        pnlDVWrap.setOpaque(false);
        JLabel lblDV = new JLabel("  Đơn vị quy đổi");
        lblDV.setOpaque(true); lblDV.setBackground(COLOR_HEADER_TABLE);
        lblDV.setForeground(Color.WHITE); lblDV.setFont(FONT_BOLD);
        lblDV.setPreferredSize(new Dimension(0, 30));
        JScrollPane scrollDV = new JScrollPane(tblDonVi);
        scrollDV.setPreferredSize(new Dimension(0, 110));
        scrollDV.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        applyThinScrollBar(scrollDV);
        pnlDVWrap.add(lblDV, BorderLayout.NORTH);
        // Panel mã vạch liên kết (thêm sau scrollDV bên dưới)
        pnlDVWrap.add(scrollDV, BorderLayout.CENTER);
        pnlScrollContent.add(pnlDVWrap);

     // ─── DANH SÁCH MÃ VẠCH LIÊN KẾT (UI CARD) ───────────────────────────────────────
        JPanel pnlMaVachWrap = new JPanel(new BorderLayout());
        pnlMaVachWrap.setBorder(new EmptyBorder(5, 15, 5, 15));
        pnlMaVachWrap.setOpaque(false);

        JLabel lblMV = new JLabel("  Danh sách mã vạch liên kết");
        lblMV.setOpaque(true); lblMV.setBackground(COLOR_HEADER_TABLE);
        lblMV.setForeground(Color.WHITE); lblMV.setFont(FONT_BOLD);
        lblMV.setPreferredSize(new Dimension(0, 30));

        // Ô quét mã + nút thêm
        JPanel pnlScanBar = new JPanel(new BorderLayout(6, 0));
        pnlScanBar.setOpaque(false);
        pnlScanBar.setBorder(new EmptyBorder(6, 0, 4, 0));
        txtScanBarcode = new JTextField();
        txtScanBarcode.setFont(FONT_NORMAL);
        txtScanBarcode.setPreferredSize(new Dimension(0, 32));
        txtScanBarcode.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(0, 8, 0, 8)));
        txtScanBarcode.setToolTipText("Quét mã vạch hoặc nhập mã rồi nhấn Enter để thêm");

        btnThemMV = createSmallBtn("+ Thêm", "#378ADD");
        pnlScanBar.add(txtScanBarcode, BorderLayout.CENTER);
        pnlScanBar.add(btnThemMV, BorderLayout.EAST);

        // Container chứa các thẻ Card
        pnlMaVachList = new JPanel();
        pnlMaVachList.setLayout(new BoxLayout(pnlMaVachList, BoxLayout.Y_AXIS));
        pnlMaVachList.setBackground(Color.WHITE);
        
        JScrollPane scrollMV = new JScrollPane(pnlMaVachList);
        scrollMV.setPreferredSize(new Dimension(0, 150));
        scrollMV.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        scrollMV.getVerticalScrollBar().setUnitIncrement(16);
        applyThinScrollBar(scrollMV);

        pnlMaVachWrap.add(lblMV, BorderLayout.NORTH);
        pnlMaVachWrap.add(pnlScanBar, BorderLayout.CENTER);
        pnlMaVachWrap.add(scrollMV, BorderLayout.SOUTH);

        pnlScrollContent.add(pnlMaVachWrap);

        // Su kien them the Card - debounce chong scanner ban doi
        final long[] lastScanTime = {0};
        final int DEBOUNCE_MS = 500;

        Runnable themMaVachAction = () -> {
            String mv = txtScanBarcode.getText().trim();
            if (mv.isEmpty()) return;

            // DEBOUNCE: bo qua neu quet lai trong 500ms
            long now = System.currentTimeMillis();
            if (now - lastScanTime[0] < DEBOUNCE_MS) {
                txtScanBarcode.setText("");
                return;
            }
            lastScanTime[0] = now;

            // Tu phat hien ma bi doc doi: "8934...8934..." => cat con nua
            if (mv.length() % 2 == 0) {
                String half = mv.substring(0, mv.length() / 2);
                if (mv.equals(half + half)) mv = half;
            }

            final String mvFinal = mv;
            // Kiem tra trung voi danh sach da co (ke ca ma chinh dang trong list)
            for (String[] item : listMaVachData) {
                if (item[0].equalsIgnoreCase(mvFinal)) {
                    showCustomNotification("TRÙNG MÃ VẠCH", "Mã vạch này đã có trong danh sách!", "WARNING");
                    txtScanBarcode.setText("");
                    return;
                }
            }

            // FIX: dùng phanLoaiMaVach() thay regex inline
            String loaiMV = phanLoaiMaVach(mvFinal);
            listMaVachData.add(new String[]{mvFinal, loaiMV});
            renderMaVachList();
            txtScanBarcode.setText("");
        };
        txtScanBarcode.addActionListener(ev -> themMaVachAction.run());
        btnThemMV.addActionListener(ev -> themMaVachAction.run());

        // Sub-table lô hàng
        String[] colsLH = {"Số lô", "Số lượng", "HSD", "Trạng thái"};
        modelLoHang = new DefaultTableModel(colsLH, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblLoHang = new JTable(modelLoHang);
        setupTableStyle(tblLoHang);
        tblLoHang.setFillsViewportHeight(true);
        DefaultTableCellRenderer centerTbl = new DefaultTableCellRenderer();
        centerTbl.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tblLoHang.getColumnCount(); i++) {
        	if (i == 3) {
                tblLoHang.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override public Component getTableCellRendererComponent(JTable table, Object value,
                            boolean isSelected, boolean hasFocus, int row, int column) {
                        String val = value != null ? value.toString() : "";
                        JLabel lbl = new JLabel(val, SwingConstants.CENTER);
                        lbl.setOpaque(true); lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                        lbl.setBorder(new EmptyBorder(4, 10, 4, 10));
                        
                        if ("Còn hàng".equals(val)) {
                            lbl.setBackground(Color.decode("#DCFCE7")); lbl.setForeground(Color.decode("#15803D"));
                        } else if ("Hết hạn".equals(val)) {
                            lbl.setBackground(Color.decode("#FEE2E2")); lbl.setForeground(Color.decode("#B91C1C"));
                        } else if ("Hết hàng".equals(val)) {
                            lbl.setBackground(Color.decode("#FEF3C7")); lbl.setForeground(Color.decode("#D97706"));
                        } else {
                            lbl.setBackground(Color.decode("#F1F5F9")); lbl.setForeground(Color.decode("#64748B"));
                        }
                        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
                        wrap.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                        wrap.add(lbl); return wrap;
                    }
                });
            } else { tblLoHang.getColumnModel().getColumn(i).setCellRenderer(centerTbl); }
        }

        JPanel pnlLotWrap = new JPanel(new BorderLayout());
        pnlLotWrap.setBorder(new EmptyBorder(5, 15, 10, 15));
        pnlLotWrap.setOpaque(false);
        JLabel lblLot = new JLabel("  Lô hàng tồn kho");
        lblLot.setOpaque(true); lblLot.setBackground(COLOR_HEADER_TABLE);
        lblLot.setForeground(Color.WHITE); lblLot.setFont(FONT_BOLD);
        lblLot.setPreferredSize(new Dimension(0, 30));
        JScrollPane scrollLH = new JScrollPane(tblLoHang);
        scrollLH.setPreferredSize(new Dimension(0, 120));
        scrollLH.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        applyThinScrollBar(scrollLH);
        pnlLotWrap.add(lblLot, BorderLayout.NORTH);
        pnlLotWrap.add(scrollLH, BorderLayout.CENTER);
        pnlScrollContent.add(pnlLotWrap);


        JScrollPane mainScroll = new JScrollPane(pnlScrollContent);
        mainScroll.setBorder(null);
        applyThinScrollBar(mainScroll);
        pnl.add(mainScroll, BorderLayout.CENTER);

        // Action buttons
        pnlActionBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlActionBottom.setBackground(Color.WHITE);
        pnlActionBottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));
        btnCapNhatBottom = createBtnWithIcon("Cập nhật",      "#1D68B2", new MenuIcon("EDIT"));   btnCapNhatBottom.setPreferredSize(new Dimension(130, 36));
        btnXoaBottom     = createBtnWithIcon("Ẩn sản phẩm",  "#F59E0B", new MenuIcon("TRASH"));  btnXoaBottom.setPreferredSize(new Dimension(140, 36));
        btnLuuBottom     = createBtnWithIcon("Lưu",           "#10B981", new MenuIcon("SAVE"));   btnLuuBottom.setPreferredSize(new Dimension(100, 36));
        btnXacNhanThem   = createBtnWithIcon("Xác nhận Thêm","#E11D48", new MenuIcon("ADD"));    btnXacNhanThem.setPreferredSize(new Dimension(170, 36));
        btnHuyBottom     = createBtnWithIcon("Hủy",           "#64748B", new MenuIcon("CANCEL")); btnHuyBottom.setPreferredSize(new Dimension(100, 36));

        btnCapNhatBottom.addActionListener(e -> {
            if (txtId.getText().isEmpty()) {
                showCustomNotification("Thông báo", "Vui lòng chọn sản phẩm cần cập nhật!", "WARNING"); return;
            }
            setEditMode(true);
        });
        btnXoaBottom.addActionListener(e -> {
            if (txtId.getText().isEmpty()) return;
            if (btnXoaBottom.getText().equals("Khôi phục")) {
                BUS_SanPham bus = new BUS_SanPham();
                if (bus.khoiPhucSP(txtId.getText())) {
                    showCustomNotification("KHÔI PHỤC THÀNH CÔNG", "Đã đưa sản phẩm trở lại danh sách!", "SUCCESS");
                    loadDataThungRac(); setDetailVisible(false);
                } else {
                    showCustomNotification("LỖI", "Khôi phục thất bại!", "ERROR");
                }
            } else { actionAnSanPham(); }
        });
        btnLuuBottom.addActionListener(e   -> thucHienLuu());
        btnXacNhanThem.addActionListener(e -> thucHienLuu());
        btnHuyBottom.addActionListener(e   -> huyThaoTac());

        pnl.add(pnlActionBottom, BorderLayout.SOUTH);
        return pnl;
    }

    // =========================================================================
    // HELPER: style field
    // =========================================================================
    private void styleField(JTextField t) {
        t.setPreferredSize(new Dimension(0, 32));
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(0, 8, 0, 8)));
        t.setDisabledTextColor(Color.BLACK);
        t.setFont(FONT_NORMAL);
    }

    // =========================================================================
    // THÊM MỚI
    // =========================================================================
    private void batDauThemMoi() {
        isAdding = true;
        resetAllBorders();
        txtTen.setText(""); txtVietTat.setText(""); txtHoatChat.setText("");
        txtHamLuong.setText(""); txtVAT.setText("10"); txtMoTa.setText("");
        txtMaVach.setText("");
        txtLoaiView.setText(""); txtDangView.setText(""); txtNSXView.setText(""); txtDVTView.setText("");
        txtNhomBenhLyView.setText("");
        if (txtGiaBan != null) txtGiaBan.setText("");

        cbLoaiCT.setSelectedIndex(0); cbDang.setSelectedIndex(0);
        cbNhaSX.setSelectedIndex(0);  cbDVT.setSelectedIndex(0);
        cbNhomBenhLy.setSelectedIndex(0);
        cbViTri.setSelectedIndex(0);
        txtViTriView.setText("");
        currentViTriId = "";
        if (txtVAT != null && cbLoaiCT.getSelectedItem() != null) {
            String loaiInit = cbLoaiCT.getSelectedItem().toString();
            txtVAT.setText(loaiInit.contains("Thuốc") ? "5.0" : "8.0");
        }

        BUS_SanPham bus = new BUS_SanPham();
        txtId.setText(bus.taoMaMoi());

        giaBanGoc = 0;
        uomAutoChanging = true;
        modelDonVi.setRowCount(0);
        // Dòng ĐVT gốc mặc định
        String dvtMoi = cbDVT != null && cbDVT.getSelectedItem() != null ? cbDVT.getSelectedItem().toString() : "";
        modelDonVi.addRow(new Object[]{dvtMoi, "1.0", ""});
        modelDonVi.addRow(new Object[3]);
        uomAutoChanging = false;
        modelLoHang.setRowCount(0);
        listMaVachData.clear();
        if (pnlMaVachList != null) { pnlMaVachList.removeAll(); pnlMaVachList.repaint(); }
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
        resetAllBorders();

        String ma        = txtId.getText();
        String ten       = txtTen.getText().trim();
        String hoatChat  = txtHoatChat.getText().trim();
        String vietTat   = txtVietTat.getText().trim();
        String hamLuong  = txtHamLuong.getText().trim();
        String vatStr    = txtVAT.getText().trim();
        String giaBanStr = txtGiaBan != null ? txtGiaBan.getText().trim().replace(",", "") : "";
        String maVach    = txtMaVach.getText().trim();
        List<String> codes = new ArrayList<>();
        if (!maVach.isEmpty()) codes.add(maVach);
        for (String[] mv : listMaVachData) {
            // Bug1-fix: bỏ qua mã lô (flag "LO") — chỉ lưu mã thuộc SanPham
            if (mv.length > 2 && "LO".equals(mv[2])) continue;
            if (!codes.contains(mv[0])) codes.add(mv[0]);
        }
        maVach = String.join(",", codes);
        // Giu toi da 200 ky tu - cat an toan theo ranh gioi dau phay
        if (maVach.length() > 200) {
            StringBuilder sb = new StringBuilder();
            for (String c : codes) {
                if (sb.length() + c.length() + 1 <= 200) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(c);
                }
            }
            maVach = sb.toString();
        }
        // Lấy nhóm bệnh lý từ combobox
        String nhomBenhLy = "";
        if (cbNhomBenhLy.getSelectedIndex() > 0) {
            String selNhom = cbNhomBenhLy.getSelectedItem().toString();
            if (!selNhom.startsWith("+ ")) nhomBenhLy = selNhom;
        }

        // Lấy vị trí thuốc — cbViTri dùng String, index 0 = "-- Chọn vị trí --"
        String viTriId = "";
        int vtIdx = cbViTri.getSelectedIndex();
        if (vtIdx > 0 && vtIdx - 1 < dsViTriData.size()) {
            viTriId = dsViTriData.get(vtIdx - 1)[0].toString();
        }

        // Validate bắt buộc
        List<String> errors = new ArrayList<>();
        JTextField firstError = null;
        if (ten.isEmpty())       { errors.add("Tên sản phẩm"); setErrorBorder(txtTen);    if (firstError == null) firstError = txtTen; }
        if (vietTat.isEmpty())   { errors.add("Tên viết tắt"); setErrorBorder(txtVietTat); if (firstError == null) firstError = txtVietTat; }
        if (hoatChat.isEmpty())  { errors.add("Hoạt chất");    setErrorBorder(txtHoatChat); if (firstError == null) firstError = txtHoatChat; }
        if (hamLuong.isEmpty())  { errors.add("Hàm lượng");    setErrorBorder(txtHamLuong); if (firstError == null) firstError = txtHamLuong; }
        if (vatStr.isEmpty())    { errors.add("Thuế VAT (%)"); setErrorBorder(txtVAT);     if (firstError == null) firstError = txtVAT; }
        if (giaBanStr.isEmpty()) { errors.add("Giá bán (VNĐ)"); if (txtGiaBan != null) setErrorBorder(txtGiaBan); if (firstError == null) firstError = txtGiaBan; }
        if (!errors.isEmpty()) {
            showCustomNotification("THIẾU THÔNG TIN", "Vui lòng nhập đầy đủ:\n- " + String.join("\n- ", errors), "WARNING");
            if (firstError != null) firstError.requestFocus();
            return;
        }

        List<String> formatErrors = new ArrayList<>();
        if (hamLuong.trim().startsWith("-")) {
            formatErrors.add("- Hàm lượng không được mang giá trị âm!");
            setErrorBorder(txtHamLuong); if (firstError == null) firstError = txtHamLuong;
        }
        double vat = 0, giaBan = 0;
        try {
            vat = Double.parseDouble(vatStr);
            if (vat < 0) { formatErrors.add("- Thuế VAT không được là số âm!"); setErrorBorder(txtVAT); }
        } catch (NumberFormatException e) {
            formatErrors.add("- Thuế VAT phải là số hợp lệ!"); setErrorBorder(txtVAT);
        }
        try {
            giaBan = Double.parseDouble(giaBanStr);
            if (giaBan <= 0) { formatErrors.add("- Giá bán phải là số dương lớn hơn 0!"); if (txtGiaBan != null) setErrorBorder(txtGiaBan); }
        } catch (NumberFormatException e) {
            formatErrors.add("- Giá bán phải là số hợp lệ!"); if (txtGiaBan != null) setErrorBorder(txtGiaBan);
        }
        if (!formatErrors.isEmpty()) {
            showCustomNotification("SAI ĐỊNH DẠNG", String.join("\n", formatErrors), "ERROR");
            if (firstError != null) firstError.requestFocus();
            return;
        }

     // --- LOGIC TỰ SINH MÃ (NẾU TRỐNG) ---
        if (maVach.isEmpty()) {
            String numOnly = "0";
            if (ma.contains("-")) {
                numOnly = ma.split("-")[1].replaceAll("[^0-9]", "");
            } else {
                numOnly = ma.replaceAll("[^0-9]", "");
            }
            if (numOnly.isEmpty()) numOnly = "0";
            try {
                // 12 digits = "200" + 9 digits tu ma SP
                String digits12 = String.format("200%09d", Long.parseLong(numOnly) % 1000000000L);
                int check = tinhCheckDigitEAN13(digits12);
                maVach = digits12 + check; // -> 13 digits EAN-13 hop le
            } catch (Exception ex) {
                // Fallback: dung timestamp
                String digits12 = String.format("200%09d", System.currentTimeMillis() % 1000000000L);
                int check = tinhCheckDigitEAN13(digits12);
                maVach = digits12 + check;
            }
            txtMaVach.setText(maVach);
            
            // QUAN TRỌNG: Nạp mã vừa sinh vào danh sách để mang đi check trùng
            if (!codes.contains(maVach)) {
                codes.add(maVach);
            }
        }

        BUS_SanPham bus = new BUS_SanPham();
        String maSPBoQua = isAdding ? null : ma;
        
        for (String code : codes) {
            if (bus.kiemTraMaVachTonTai(code, maSPBoQua)) {
                showCustomNotification("TRÙNG MÃ VẠCH", 
                    "Mã vạch '" + code + "' đã bị trùng với một sản phẩm khác đang có trong hệ thống!", "WARNING");
                txtMaVach.requestFocus();
                return; // Dừng ngay lập tức, không cho lưu
            }
        }

        // --- THU THẬP ĐƠN VỊ QUY ĐỔI CHUẨN (3 CỘT) ---
        List<Object[]> dsDonVi = new ArrayList<>();
        String dvtGoc = cbDVT.getSelectedItem().toString().trim();
        boolean daCoDvtGoc = false;
        
        for (int i = 0; i < modelDonVi.getRowCount(); i++) {
            Object tenDVObj = modelDonVi.getValueAt(i, 0);
            Object quyDoiDV = modelDonVi.getValueAt(i, 1);
            Object giaDVObj = modelDonVi.getValueAt(i, 2);
            
            if (tenDVObj != null && !tenDVObj.toString().trim().isEmpty() && quyDoiDV != null && giaDVObj != null) {
                try {
                    String tenDV = tenDVObj.toString().trim();
                    double tiLe  = Double.parseDouble(quyDoiDV.toString().trim());
                    double gia   = Double.parseDouble(giaDVObj.toString().replace(",", "").trim());
                    
                    if (tenDV.equalsIgnoreCase(dvtGoc)) {
                        dsDonVi.add(new Object[]{tenDV, 1.0, gia}); daCoDvtGoc = true;
                    } else if (!tenDV.equalsIgnoreCase(dvtGoc)) {
                        // Fix: cho phep ti le < 1 (vd: nua vien = 0.5)
                        dsDonVi.add(new Object[]{tenDV, tiLe, gia});
                    }
                } catch (Exception ignored) {}
            }
        }
        if (!daCoDvtGoc) {
            dsDonVi.add(new Object[]{dvtGoc, 1.0, giaBan});
        }

        String loai = cbLoaiCT.getSelectedItem().toString();
        String dangBaoChe = cbDang.getSelectedItem().toString();
        String nsx  = cbNhaSX.getSelectedItem().toString();
        String dvt  = cbDVT.getSelectedItem().toString();
        String moTa = txtMoTa.getText().trim();

        // Cập nhật view fields
        txtLoaiView.setText(loai); txtDangView.setText(dangBaoChe);
        txtNSXView.setText(nsx);  txtDVTView.setText(dvt);
        txtNhomBenhLyView.setText(nhomBenhLy);
        txtViTriView.setText(cbViTri.getSelectedIndex() > 0 ? cbViTri.getSelectedItem().toString() : "");
        if (loai.equals("Thuốc kê đơn"))          txtLoaiView.setDisabledTextColor(Color.decode("#EF4444"));
        else if (loai.equals("Sản phẩm chức năng")) txtLoaiView.setDisabledTextColor(Color.decode("#10B981"));
        else txtLoaiView.setDisabledTextColor(Color.decode("#2179E0"));

        String dbDanhMuc = mapToDbDanhMuc(loai);
        String dbDang    = mapToDbDang(dangBaoChe);

        if (isAdding && bus.kiemTraMaSPTonTai(ma)) {
            showCustomNotification("TRÙNG MÃ HỆ THỐNG",
                "Mã sản phẩm " + ma + " đã có người khác vừa dùng! Hệ thống đã tự lấy mã mới, vui lòng bấm Lưu lại.", "WARNING");
            txtId.setText(bus.taoMaMoi());
            return;
        }

        boolean success;
        if (isAdding) {
            success = bus.themSP(ma, dbDanhMuc, dbDang, ten, vietTat, nsx, hoatChat, vat,
                                 hamLuong, moTa, dvt, giaBan, maVach, nhomBenhLy, viTriId, dsDonVi);
            if (success) showCustomNotification("THÊM MỚI THÀNH CÔNG", "Đã thêm sản phẩm thành công!", "SUCCESS");
        } else {
            success = bus.capNhatSP(ma, dbDanhMuc, dbDang, ten, vietTat, nsx, hoatChat, vat,
                                    hamLuong, moTa, dvt, giaBan, maVach, nhomBenhLy, viTriId, dsDonVi);
            if (success) showCustomNotification("CẬP NHẬT THÀNH CÔNG", "Cập nhật sản phẩm thành công!", "SUCCESS");
        }

        if (success) {
            loadDataFromDatabase();
            for (int i = 0; i < tblSanPham.getRowCount(); i++) {
                try {
                    int modelIdx = tblSanPham.convertRowIndexToModel(i);
                    if (tblSanPham.getModel().getValueAt(modelIdx, 0).toString().equals(ma)) {
                        tblSanPham.setRowSelectionInterval(i, i);
                        tblSanPham.scrollRectToVisible(tblSanPham.getCellRect(i, 0, true));
                        selectedMaSP = ma; tblSanPham.repaint();
                        break;
                    }
                } catch (Exception ignored) {}
            }
            setEditMode(false); isAdding = false;
        } else {
            showCustomNotification("LỖI HỆ THỐNG", "Lưu thất bại! Vui lòng kiểm tra lại kết nối CSDL.", "ERROR");
        }
    }

    // =========================================================================
    // HIỂN THỊ CHI TIẾT SẢN PHẨM — ĐỌC maVach VÀ nhomBenhLy
    // =========================================================================
    private void hienThiChiTietSanPham(int row) {
        // Nếu đang ở edit mode (user click SP khác mà không lưu/hủy), reset về VIEW
        isAdding = false;
        setDetailVisible(true);
        setEditMode(false);
        resetAllBorders();
        Object[] data = currentFilteredData.get((currentPageMock - 1) * itemsPerPageMock + row);
        String maSP = data[0].toString();

        txtId.setText(maSP);
        txtTen.setText(data[1].toString());
        String loai = data[2].toString();
        txtLoaiView.setText(loai);
        if (loai.equals("Thuốc kê đơn"))          txtLoaiView.setDisabledTextColor(Color.decode("#EF4444"));
        else if (loai.equals("Sản phẩm chức năng")) txtLoaiView.setDisabledTextColor(Color.decode("#10B981"));
        else txtLoaiView.setDisabledTextColor(Color.decode("#2179E0"));

        txtHoatChat.setText(data[3].toString());
        String dangBaoChe = (data.length > 4) ? data[4].toString() : "Viên nén";
        txtDangView.setText(dangBaoChe);
        txtVietTat.setText(""); // Se duoc load chinh xac tu spDayDu ben duoi
        txtHamLuong.setText("");
        txtNSXView.setText(data.length > 5 ? data[5].toString() : "DHG Pharma");
        // Fix: data[6] la "5.0%" -> hien thi "5" thay vi "5.0"
        if (data.length > 6) {
            String vatStr2 = data[6].toString().replace("%", "").trim();
            try {
                double vatVal = Double.parseDouble(vatStr2);
                txtVAT.setText(vatVal == (long)vatVal ? String.valueOf((long)vatVal) : vatStr2);
            } catch (Exception e) { txtVAT.setText(vatStr2); }
        } else { txtVAT.setText("10"); }
        txtMoTa.setText("");
        if (txtGiaBan != null) txtGiaBan.setText(data.length > 7 ? data[7].toString().replace(",", "") : "0");

        // Đọc mã vạch và nhóm bệnh lý từ data[]
        String maVach   = data.length > 8 ? data[8].toString() : "";
        String nhomBL   = data.length > 9 ? data[9].toString() : "";
        txtMaVach.setText(maVach);
        txtNhomBenhLyView.setText(nhomBL);

        BUS_SanPham bus = new BUS_SanPham();
        SanPham spDayDu = bus.getSanPhamDayDu(maSP);
        String dvtGoc = dangBaoChe.contains("Dung dịch") || dangBaoChe.contains("Si rô") ? "Chai" : "Viên";

        if (spDayDu != null) {
            if (spDayDu.getHamLuong() != null)   txtHamLuong.setText(spDayDu.getHamLuong());
            if (spDayDu.getMoTa() != null)        txtMoTa.setText(spDayDu.getMoTa());
            if (spDayDu.getTenVietTat() != null)  txtVietTat.setText(spDayDu.getTenVietTat());
            if (txtGiaBan != null) {
            long gb = (long) spDayDu.getGiaBan();
            txtGiaBan.setText(String.valueOf(gb));
            giaBanGoc = spDayDu.getGiaBan();
        }
            if (spDayDu.getDonViDoCoBan() != null && !spDayDu.getDonViDoCoBan().isEmpty())
                dvtGoc = spDayDu.getDonViDoCoBan();
            if (spDayDu.getMaVach() != null && !spDayDu.getMaVach().isEmpty()) {
                String[] mvs = spDayDu.getMaVach().split(",");
                txtMaVach.setText(mvs[0].trim()); // CHỈ HIỂN THỊ MÃ ĐẦU TIÊN VÀO Ô CHÍNH
            }
            if (spDayDu.getNhomBenhLy() != null && !spDayDu.getNhomBenhLy().isEmpty())
                txtNhomBenhLyView.setText(spDayDu.getNhomBenhLy());

            // Hiển thị vị trí thuốc — tìm theo viTriId trong dsViTriData
            currentViTriId = spDayDu.getViTriId() != null ? spDayDu.getViTriId() : "";
            cbViTri.setSelectedIndex(0); txtViTriView.setText("");
            if (!currentViTriId.isEmpty()) {
                for (int i = 0; i < dsViTriData.size(); i++) {
                    if (dsViTriData.get(i)[0].toString().equals(currentViTriId)) {
                        cbViTri.setSelectedIndex(i + 1); // +1 vì index 0 là "-- Chọn vị trí --"
                        txtViTriView.setText(cbViTri.getItemAt(i + 1));
                        break;
                    }
                }
            }

        txtDVTView.setText(dvtGoc);

     // Load đơn vị quy đổi
        uomAutoChanging = true;
        modelDonVi.setRowCount(0);
        List<Object[]> dsQuyDoi = bus.layDonViQuyDoiTheoSP(maSP);
        if (dsQuyDoi != null) {
            for (Object[] dv : dsQuyDoi) {
                double tiLeDb = Double.parseDouble(dv[1].toString());
                Object tiLeHienThi = (tiLeDb == (long) tiLeDb) ? (long) tiLeDb : tiLeDb;
                String giaHienThi = dv[2].toString();
                // Chi format lai gia neu la don vi co ban (tiLe = 1.0 chinh xac)
                // Khong format don vi khac (chai, hop...) du tiLe < 1
                if (tiLeDb == 1.0 && giaBanGoc > 0) { giaHienThi = String.format("%,.0f", giaBanGoc); }
                
                // Đẩy 3 cột lên bảng
                modelDonVi.addRow(new Object[]{dv[0].toString(), tiLeHienThi, giaHienThi});
            }
        }
        modelDonVi.addRow(new Object[3]); // Dòng trống tự động
        uomAutoChanging = false;

        // Load lô hàng
        modelLoHang.setRowCount(0);
        List<LoHang> listLo = bus.layTatCaLoTheoSP(maSP);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (listLo != null) {
            for (LoHang lh : listLo) {
            	String tTrang;
                if (lh.getTrangThai() != null && "AN".equals(lh.getTrangThai().name())) {
                    tTrang = "Đã ẩn";
                } else if (lh.getSoLuongLoHang() <= 0) {
                    tTrang = "Hết hàng";
                } else if (lh.getNgayHetHan() != null
                        && lh.getNgayHetHan().toLocalDate().isBefore(LocalDate.now())) {
                    tTrang = "Hết hạn";
                } else {
                    tTrang = "Còn hàng";
                }
                String hsdStr = lh.getNgayHetHan() != null ? lh.getNgayHetHan().toLocalDate().format(fmt) : "N/A";
                modelLoHang.addRow(new Object[]{lh.getSoLoHang(), lh.getSoLuongLoHang(), hsdStr, tTrang});
            }
        }

     // Load danh sách mã vạch lên UI Card (Gộp mã Sản phẩm + Mã QR Lô hàng)
        listMaVachData.clear();
        // LinkedHashSet: lọc trùng, giữ thứ tự — mã chính luôn đứng đầu
        java.util.Set<String> uniqueSpBarcodes  = new java.util.LinkedHashSet<>();
        java.util.Set<String> uniqueLoBarcodes  = new java.util.LinkedHashSet<>();

        // 1. Lấy TẤT CẢ mã vạch của Sản Phẩm vào tập riêng
        if (spDayDu != null && spDayDu.getMaVach() != null && !spDayDu.getMaVach().isEmpty()) {
            for (String mv : spDayDu.getMaVach().split(",")) {
                String cleanMv = mv.trim();
                if (!cleanMv.isEmpty()) uniqueSpBarcodes.add(cleanMv);
            }
        }

        // 2. Bug1-fix: maVachNoiBo của lô chỉ hiển thị tham khảo, KHÔNG đưa vào mã SP
        //    Dùng tập riêng uniqueLoBarcodes, không gộp vào uniqueSpBarcodes
        if (listLo != null) {
            for (LoHang lh : listLo) {
                if (lh.getMaVachNoiBo() != null && !lh.getMaVachNoiBo().trim().isEmpty()) {
                    String mvLo = lh.getMaVachNoiBo().trim();
                    // Chỉ thêm vào tập lô nếu không phải mã SP (tránh hiện trùng)
                    if (!uniqueSpBarcodes.contains(mvLo)) {
                        uniqueLoBarcodes.add(mvLo);
                    }
                }
            }
        }

        // 3. Đưa mã SP vào listMaVachData (không có flag → editable, được lưu)
        String maChinh = txtMaVach.getText().trim();
        for (String mv : uniqueSpBarcodes) {
            String loaiMV = mv.equalsIgnoreCase(maChinh)
                ? phanLoaiMaVach(mv) + " ★"
                : phanLoaiMaVach(mv);
            listMaVachData.add(new String[]{mv, loaiMV}); // String[2] → không có flag → SP barcode
        }

        // 4. Bug1-fix: mã lô đưa vào listMaVachData với flag "LO" → readOnly, không lưu vào SP
        for (String mv : uniqueLoBarcodes) {
            listMaVachData.add(new String[]{mv, "Mã QR lô hàng", "LO"});
        }

        renderMaVachList();
        setDetailVisible(true);
        setEditMode(false);
    }
    }

    // =========================================================================
    // DIALOG CHON MENH GIA
    // =========================================================================
    private static final int[] MENH_GIA = {500000, 200000, 100000, 50000, 20000, 10000, 5000, 2000, 1000};
    private static final String[] MENH_GIA_COLOR = {
        "#1565C0", "#AD1457", "#2E7D32",
        "#F9A825", "#C62828", "#6A1B9A",
        "#C62828", "#37474F", "#37474F"
    };

    private void showMenhGiaDialog() {
        if (txtGiaBan == null || !txtGiaBan.isEditable()) return;

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this) instanceof java.awt.Frame
            ? (java.awt.Frame) SwingUtilities.getWindowAncestor(this) : null, true);
        dlg.setUndecorated(true);

        // 1. Panel chính: Viền vuông vức chuẩn màu hệ thống (#1E3A8A)
        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createLineBorder(Color.decode("#1E3A8A"), 2));
        pnlMain.setBackground(Color.WHITE);

        // 2. Header: Nền xanh đậm, chữ trắng đồng bộ với form Notification
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.decode("#1E3A8A"));
        pnlHeader.setPreferredSize(new Dimension(0, 45));
        JLabel lblTitle = new JLabel("NHẬP NHANH MỆNH GIÁ", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.CENTER);

        // 3. Khu vực hiển thị số tiền tổng
        JPanel pnlDisplayWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        pnlDisplayWrap.setBackground(Color.WHITE);
        JLabel lblDisplay = new JLabel("0 đ", SwingConstants.CENTER);
        lblDisplay.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblDisplay.setForeground(Color.decode("#E11D48")); // Màu đỏ nổi bật
        lblDisplay.setPreferredSize(new Dimension(350, 45));
        lblDisplay.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E2E8F0"), 1),
            new EmptyBorder(5, 15, 5, 15)));
        pnlDisplayWrap.add(lblDisplay);

        // 4. Lưới các mệnh giá (Giữ nguyên logic đếm của bạn)
        int[] counts = new int[MENH_GIA.length];
        long[] tongTien = {0};

        JPanel pnlGrid = new JPanel(new GridLayout(3, 3, 10, 10));
        pnlGrid.setBackground(Color.WHITE);
        pnlGrid.setBorder(new EmptyBorder(0, 15, 15, 15));

        for (int i = 0; i < MENH_GIA.length; i++) {
            final int idx = i;
            String colorHex = MENH_GIA_COLOR[i];
            String label = String.format("%,.0f đ", (double) MENH_GIA[i]).replace(",", ".");

            JPanel pnlCard = new JPanel(new BorderLayout(5, 0));
            pnlCard.setBackground(Color.WHITE);
            // Bỏ paintComponent bo tròn cũ, thay bằng Border vuông vức phẳng
            pnlCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode(colorHex), 1),
                new EmptyBorder(8, 10, 8, 10)));
            pnlCard.setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel lblMenh = new JLabel(label);
            lblMenh.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblMenh.setForeground(Color.decode(colorHex));

            JLabel lblCount = new JLabel("0", SwingConstants.RIGHT);
            lblCount.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblCount.setForeground(Color.decode("#64748B"));
            lblCount.setPreferredSize(new Dimension(25, 20));

            pnlCard.add(lblMenh, BorderLayout.WEST);
            pnlCard.add(lblCount, BorderLayout.EAST);

            // Logic click: Trái tăng, Phải giảm
            pnlCard.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
                        if (counts[idx] > 0) { counts[idx]--; tongTien[0] -= MENH_GIA[idx]; }
                    } else {
                        counts[idx]++; tongTien[0] += MENH_GIA[idx];
                    }
                    lblCount.setText(String.valueOf(counts[idx]));
                    lblDisplay.setText(String.format("%,.0f đ", (double) tongTien[0]).replace(",", "."));
                }
            });
            pnlGrid.add(pnlCard);
        }

        // 5. Footer chứa các nút bấm hành động
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#E2E8F0")));

        // Nút xóa trắng (Thay thế cho nút "Chọn mệnh giá" nằm sai chỗ trong code cũ)
        JButton btnXoaTrang = new JButton("Xóa trắng");
        btnXoaTrang.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnXoaTrang.setForeground(Color.decode("#64748B"));
        btnXoaTrang.setContentAreaFilled(false); btnXoaTrang.setBorderPainted(false);
        btnXoaTrang.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXoaTrang.addActionListener(e -> {
            for (int i = 0; i < counts.length; i++) counts[i] = 0;
            tongTien[0] = 0;
            lblDisplay.setText("0 đ");
            for (Component c : pnlGrid.getComponents()) {
                if (c instanceof JPanel) ((JLabel)((JPanel)c).getComponent(1)).setText("0");
            }
        });

        JButton btnHuy = new JButton("Hủy");
        btnHuy.setPreferredSize(new Dimension(80, 35));
        btnHuy.setBackground(Color.decode("#64748B")); btnHuy.setForeground(Color.WHITE);
        btnHuy.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnHuy.setFocusPainted(false); btnHuy.setBorderPainted(false);
        btnHuy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHuy.addActionListener(e -> dlg.dispose());

        JButton btnOK = new JButton("Áp dụng");
        btnOK.setPreferredSize(new Dimension(100, 35));
        btnOK.setBackground(Color.decode("#1E3A8A")); btnOK.setForeground(Color.WHITE);
        btnOK.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnOK.setFocusPainted(false); btnOK.setBorderPainted(false);
        btnOK.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOK.addActionListener(e -> {
            if (tongTien[0] > 0) {
                txtGiaBan.setText(String.valueOf(tongTien[0]));
                syncGiaBanVaoBangDonVi();
            }
            dlg.dispose();
        });

        pnlBottom.add(btnXoaTrang);
        pnlBottom.add(btnHuy);
        pnlBottom.add(btnOK);

        // 6. Tự động load giá hiện tại trên TextBox vào biến tổng tiền khi vừa mở Dialog
        String cur = txtGiaBan.getText().trim().replace(",", "").replace(".", "");
        try { tongTien[0] = Long.parseLong(cur); } catch (Exception ignored) { tongTien[0] = 0; }
        lblDisplay.setText(String.format("%,.0f đ", (double) tongTien[0]).replace(",", "."));

        // Ráp các thành phần lại
        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setBackground(Color.WHITE);
        pnlCenter.add(pnlDisplayWrap, BorderLayout.NORTH);
        pnlCenter.add(pnlGrid, BorderLayout.CENTER);

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlCenter, BorderLayout.CENTER);
        pnlMain.add(pnlBottom, BorderLayout.SOUTH);

        dlg.add(pnlMain);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(500, 0));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // =========================================================================
    // SET EDIT MODE — BỔ SUNG txtMaVach VÀ cbNhomBenhLy
    // =========================================================================
    private void setEditMode(boolean edit) {
        Color bg = edit ? Color.WHITE : COLOR_DISABLED_BG;
        txtTen.setEditable(edit);      txtTen.setBackground(bg);
        txtId.setEditable(false);      txtId.setBackground(COLOR_DISABLED_BG);
        txtVietTat.setEditable(edit);  txtVietTat.setBackground(bg);
        txtHoatChat.setEditable(edit); txtHoatChat.setBackground(bg);
        txtHamLuong.setEditable(edit); txtHamLuong.setBackground(bg);
        txtVAT.setEditable(edit);      txtVAT.setBackground(bg);
        txtMaVach.setEditable(edit);   txtMaVach.setBackground(bg);
        if (txtScanBarcode != null) {
            txtScanBarcode.setEditable(edit);
            txtScanBarcode.setBackground(bg);
        }
        if (btnThemMV != null) {
            btnThemMV.setEnabled(edit);
            btnThemMV.setBackground(edit ? Color.decode("#378ADD") : Color.LIGHT_GRAY);
        }
        if (btnXoaMV != null) {
            btnXoaMV.setEnabled(edit);
            btnXoaMV.setBackground(edit ? Color.decode("#E11D48") : Color.LIGHT_GRAY);
        }
        if (btnInMaVach != null) {
            boolean coMa = txtMaVach != null && !txtMaVach.getText().trim().isEmpty();
            boolean enabled = edit || coMa;
            btnInMaVach.setEnabled(enabled);
            btnInMaVach.setBackground(enabled ? Color.WHITE : Color.decode("#F1F5F9"));
            btnInMaVach.setForeground(enabled ? Color.BLACK : Color.decode("#94A3B8"));
        }
        if (txtGiaBan != null) { txtGiaBan.setEditable(edit); txtGiaBan.setBackground(bg); }
        txtMoTa.setEditable(edit);
        txtMoTa.setBackground(edit ? Color.WHITE : COLOR_DISABLED_BG);
        scrollMoTa.getViewport().setBackground(edit ? Color.WHITE : COLOR_DISABLED_BG);

        clLoai.show(pnlLoai, edit ? "EDIT" : "VIEW");
        clDang.show(pnlDang, edit ? "EDIT" : "VIEW");
        clNSX.show(pnlNSX,   edit ? "EDIT" : "VIEW");
        clDVT.show(pnlDVT,   edit ? "EDIT" : "VIEW");
        clNhomBenhLy.show(pnlNhomBenhLyCard, edit ? "EDIT" : "VIEW");

        if (edit && !isAdding) {
            cbLoaiCT.setSelectedItem(txtLoaiView.getText());
            cbDang.setSelectedItem(txtDangView.getText());
            cbNhaSX.setSelectedItem(txtNSXView.getText());
            cbDVT.setSelectedItem(txtDVTView.getText());
            // Set nhóm bệnh lý
            String nhomHienTai = txtNhomBenhLyView.getText();
            cbNhomBenhLy.setSelectedItem(nhomHienTai.isEmpty() ? "-- Chọn nhóm --" : nhomHienTai);
        }
        modelDonVi.setEditable(edit);

        pnlActionBottom.removeAll();
        if (edit) {
            if (isAdding) pnlActionBottom.add(btnXacNhanThem);
            else          pnlActionBottom.add(btnLuuBottom);
            pnlActionBottom.add(btnHuyBottom);
        } else {
            if (!isStaffRole) {
                pnlActionBottom.add(btnCapNhatBottom);
                pnlActionBottom.add(btnXoaBottom);
            }
        }
        pnlActionBottom.revalidate(); pnlActionBottom.repaint();
    }

    // =========================================================================
    // ẨN SẢN PHẨM
    // =========================================================================
    private void actionAnSanPham() {
        if (txtId.getText().isEmpty()) return;
        String maSP = txtId.getText();
        String tenSP = txtTen.getText().trim();
        boolean confirmed = showCustomConfirmDialog("XÁC NHẬN ẨN SẢN PHẨM",
            "Sản phẩm <b>" + tenSP + "</b> sẽ bị ẩn khỏi hệ thống.\nDữ liệu lịch sử vẫn được giữ nguyên.\n\nBạn có chắc chắn?");
        if (!confirmed) return;

        BUS_SanPham bus = new BUS_SanPham();
        int soLuongTon = bus.getSoLuongTon(maSP);
        if (soLuongTon > 0) {
            showCustomNotification("KHÔNG THỂ ẨN", "Sản phẩm còn " + soLuongTon + " đơn vị trong kho.\nXuất hết tồn kho trước khi ẩn.", "WARNING");
            return;
        }
        if (bus.anSP(maSP)) {
            showCustomNotification("ẨN THÀNH CÔNG", "Sản phẩm \"" + tenSP + "\" đã được ẩn.", "SUCCESS");
            loadDataFromDatabase(); clearDetailForm(); setDetailVisible(false);
        } else {
            showCustomNotification("THAO TÁC THẤT BẠI", "Không thể ẩn sản phẩm!", "ERROR");
        }
    }

    // =========================================================================
    // CLEAR FORM
    // =========================================================================
    private void clearDetailForm() {
        txtId.setText(""); txtTen.setText(""); txtVietTat.setText("");
        txtHoatChat.setText(""); txtHamLuong.setText(""); txtVAT.setText(""); txtMoTa.setText("");
        txtMaVach.setText("");
        txtLoaiView.setText(""); txtDangView.setText(""); txtNSXView.setText(""); txtDVTView.setText("");
        txtNhomBenhLyView.setText("");
        if (txtGiaBan != null) txtGiaBan.setText("");
        modelDonVi.setRowCount(0); modelLoHang.setRowCount(0);
        listMaVachData.clear();
        if (pnlMaVachList != null) { pnlMaVachList.removeAll(); pnlMaVachList.repaint(); }
        resetAllBorders();
    }

    private void resetAllBorders() {
        resetBorder(txtTen); resetBorder(txtVietTat); resetBorder(txtHoatChat);
        resetBorder(txtHamLuong); resetBorder(txtVAT);
        if (txtGiaBan != null) resetBorder(txtGiaBan);
    }

    private void resetBorder(JTextField t) {
        if (t != null) t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(0, 8, 0, 8)));
    }
    private void setErrorBorder(JTextField t) {
        if (t != null) t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.RED, 2), new EmptyBorder(0, 8, 0, 8)));
    }

    // =========================================================================
    // DETAIL VISIBLE
    // =========================================================================
    private void setDetailVisible(boolean visible) {
        detailVisible = visible;
        if (visible) {
            if (pnlRightDetail.getParent() == null) pnlBody.add(pnlRightDetail, BorderLayout.EAST);
            modelSanPham.setColumnIdentifiers(COLS_COLLAPSED);
        } else {
            pnlBody.remove(pnlRightDetail);
            modelSanPham.setColumnIdentifiers(COLS_EXPANDED);
        }
        // Đặt lại độ rộng cột sau khi đổi column
        if (visible) {
            int[] w = {90, 100, 140, 180, 110, 150, 90};
            for (int i = 0; i < w.length && i < tblSanPham.getColumnCount(); i++)
                tblSanPham.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        } else {
            int[] w = {90, 100, 140, 200, 120, 150, 100, 120, 50, 90};
            for (int i = 0; i < w.length && i < tblSanPham.getColumnCount(); i++)
                tblSanPham.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
        pnlBody.revalidate(); pnlBody.repaint();
        updatePagination();
    }

    // =========================================================================
    // LOAD DATA
    // =========================================================================
    private void loadDataFromDatabase() {
        allDataMock.clear();
        BUS_SanPham bus = new BUS_SanPham();
        List<Object[]> dsSP = bus.layDanhSachChoBang();
        if (dsSP != null) allDataMock.addAll(dsSP);
        SwingUtilities.invokeLater(() -> tblSanPham.repaint()); // Giu highlight sau reload
        currentFilteredData.clear();
        currentFilteredData.addAll(allDataMock);
        updatePagination();
    }

    private void loadDataThungRac() {
        allDataMock.clear();
        BUS_SanPham bus = new BUS_SanPham();
        List<Object[]> dsSP = bus.layDanhSachSanPhamDaAn();
        if (dsSP != null) allDataMock.addAll(dsSP);
        currentFilteredData.clear();
        currentFilteredData.addAll(allDataMock);
        updatePagination();
    }

    private void capNhatDuLieuComboBoxTuDB() {
        BUS_SanPham bus = new BUS_SanPham();
        List<String> dsNSX = bus.layDanhSachNhaSanXuat();
        cbNhaSX.removeAllItems();
        cbNhaSX.addItem("DHG Pharma"); cbNhaSX.addItem("Traphaco");
        for (String nsx : dsNSX) {
            boolean exists = false;
            for (int i = 0; i < cbNhaSX.getItemCount(); i++) if (cbNhaSX.getItemAt(i).equals(nsx)) exists = true;
            if (!exists) cbNhaSX.addItem(nsx);
        }
        List<String> dsDVT = bus.layDanhSachDonViTinh();
        cbDVT.removeAllItems();
        cbDVT.addItem("Viên"); cbDVT.addItem("Chai");
        for (String dvt : dsDVT) {
            boolean exists = false;
            for (int i = 0; i < cbDVT.getItemCount(); i++) if (cbDVT.getItemAt(i).equals(dvt)) exists = true;
            if (!exists) cbDVT.addItem(dvt);
        }
    }

    public void setReadOnly(boolean isReadOnly) {
        this.isStaffRole = isReadOnly;
        if (isReadOnly) {
            if (topBtnThem != null) topBtnThem.setVisible(false);
            if (topBtnNhap != null) topBtnNhap.setVisible(false);
            if (topBtnXuat != null) topBtnXuat.setVisible(false);
        }
    }

    // =========================================================================
    // MAP DB <-> LABEL
    // =========================================================================
    private String mapToDbDanhMuc(String label) {
        switch (label) {
            case "Thuốc kê đơn":        return "THUOC_KE_DON";
            case "Thuốc không kê đơn":  return "THUOC_KHONG_KE_DON";
            case "Mỹ phẩm":             return "MY_PHAM";
            default:                    return "THUC_PHAM_CHUC_NANG";
        }
    }

    private String mapToDbDang(String label) {
        switch (label) {
            case "Viên nang":       return "VIEN_NANG";
            case "Viên sủi":        return "VIEN_SUI";
            case "Thuốc bột":       return "THUOC_BOT";
            case "Kẹo ngậm":        return "KEO_NGAM";
            case "Dung dịch":       return "DUNG_DICH";
            case "Hỗn dịch":        return "HON_DICH";
            case "Thuốc nhỏ giọt":  return "THUOC_NHO_GIOT";
            case "Súc miệng":       return "SUC_MIENG";
            default:                return "VIEN_NEN";
        }
    }

    // =========================================================================
    // UI HELPERS
    // =========================================================================
    private void applyTableCellRenderers() {
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);
        for (int i = 0; i < tblSanPham.getColumnCount(); i++) {
            // Tên, Hoạt chất, Nhóm bệnh lý căn trái; còn lại căn giữa
            tblSanPham.getColumnModel().getColumn(i).setCellRenderer(
                (i == 3 || i == 5 || i == 2) ? left : center);
        }
    }

    class WatermarkTextField extends JTextField {
        private String watermark;
        public WatermarkTextField(String watermark) { this.watermark = watermark; }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !hasFocus()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Color.GRAY); g2.setFont(getFont().deriveFont(Font.ITALIC));
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(watermark, getInsets().left + 5, y); g2.dispose();
            }
        }
    }

    private void applyFlatComboBoxStyle(JComboBox<String> cb) {
        cb.setBackground(Color.WHITE); cb.setFocusable(false);
        cb.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton btn = new JButton("\u25BC");
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                btn.setForeground(Color.GRAY); btn.setBackground(Color.WHITE);
                btn.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                btn.setFocusPainted(false); btn.setContentAreaFilled(false);
                btn.setOpaque(true); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return btn;
            }
        });
        cb.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(0, 5, 0, 5)));
    }

    private JTextField createViewField() {
        JTextField t = new JTextField();
        t.setEditable(false); t.setFont(FONT_NORMAL);
        t.setDisabledTextColor(Color.BLACK);
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(0, 8, 0, 8)));
        return t;
    }

    private void addFormField(JPanel p, String l, JTextField t, GridBagConstraints g, int x, int y, int w) {
        g.gridx = x; g.gridy = y; g.gridwidth = w;
        JLabel lbl = new JLabel(l); lbl.setFont(FONT_NORMAL); lbl.setForeground(Color.BLACK);
        p.add(lbl, g);
        g.gridy++;
        t.setPreferredSize(new Dimension(0, 32));
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(0, 8, 0, 8)));
        t.setDisabledTextColor(Color.BLACK); t.setFont(FONT_NORMAL);
        p.add(t, g);
    }

    private void addCustomField(JPanel p, String l, JPanel c, GridBagConstraints g, int x, int y) {
        g.gridx = x; g.gridy = y; g.gridwidth = 1;
        JLabel lbl = new JLabel(l); lbl.setFont(FONT_NORMAL); lbl.setForeground(Color.BLACK);
        p.add(lbl, g); g.gridy++;
        c.setPreferredSize(new Dimension(0, 32));
        p.add(c, g);
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

    private void setupTableStyle(JTable t) {
        t.setRowHeight(30); t.setFont(FONT_NORMAL);
        t.getTableHeader().setFont(FONT_BOLD);
        t.getTableHeader().setBackground(COLOR_LIGHT_BLUE);
        t.setGridColor(COLOR_BORDER);
        t.setSelectionBackground(COLOR_LIGHT_BLUE);
        t.setSelectionForeground(Color.BLACK);
    }

    private void applyThinScrollBar(JScrollPane sp) {
        sp.getVerticalScrollBar().setUnitIncrement(20);
        sp.getHorizontalScrollBar().setUnitIncrement(20);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = Color.decode("#CBD5E1"); trackColor = Color.decode("#F1F5F9");
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0));
                b.setMinimumSize(new Dimension(0,0)); b.setMaximumSize(new Dimension(0,0)); return b;
            }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty() || r.width <= 0 || r.height <= 0) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(r.x + 1, r.y + 1, Math.max(0, r.width - 2), Math.max(0, r.height - 2), 6, 6);
                g2.dispose();
            }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor); g2.fillRect(r.x, r.y, r.width, r.height); g2.dispose();
            }
        });
        sp.getHorizontalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = Color.decode("#CBD5E1"); trackColor = Color.decode("#F1F5F9");
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0));
                b.setMinimumSize(new Dimension(0,0)); b.setMaximumSize(new Dimension(0,0)); return b;
            }
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(r.x + 1, r.y + 1, Math.max(0, r.width - 2), Math.max(0, r.height - 2), 6, 6);
                g2.dispose();
            }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor); g2.fillRect(r.x, r.y, r.width, r.height); g2.dispose();
            }
        });
    }

    // =========================================================================
    // THÔNG BÁO / CONFIRM
    // =========================================================================
    private void showCustomNotification(String titleText, String message, String type) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setUndecorated(true);
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

        JPanel pnlBody2 = new JPanel(null);
        pnlBody2.setBackground(Color.WHITE);
        pnlBody2.setPreferredSize(new Dimension(420, 110));

        JPanel pnlIcon = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color mainColor = type.equals("ERROR") ? Color.decode("#EF4444") :
                                  (type.equals("SUCCESS") ? Color.decode("#10B981") : Color.decode("#F59E0B"));
                Color bgColor   = type.equals("ERROR") ? Color.decode("#FEE2E2") :
                                  (type.equals("SUCCESS") ? Color.decode("#D1FAE5") : Color.decode("#FEF3C7"));
                g2.setColor(bgColor); g2.fillOval(0, 0, 50, 50);
                g2.setColor(mainColor); g2.setStroke(new java.awt.BasicStroke(3f)); g2.drawOval(0, 0, 50, 50);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                FontMetrics fm = g2.getFontMetrics();
                String symbol = type.equals("ERROR") ? "X" : (type.equals("SUCCESS") ? "V" : "!");
                g2.drawString(symbol, (50 - fm.stringWidth(symbol)) / 2, ((50 - fm.getHeight()) / 2) + fm.getAscent());
                g2.dispose();
            }
        };
        pnlIcon.setBounds(20, 25, 50, 50); pnlIcon.setOpaque(false);

        JTextArea msg = new JTextArea(message);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14)); msg.setForeground(Color.decode("#333333"));
        msg.setWrapStyleWord(true); msg.setLineWrap(true); msg.setOpaque(false);
        msg.setEditable(false); msg.setFocusable(false);
        JScrollPane scroll = new JScrollPane(msg);
        scroll.setBounds(85, 20, 315, 80); scroll.setBorder(null);
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        applyThinScrollBar(scroll);

        pnlBody2.add(pnlIcon); pnlBody2.add(scroll);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlFooter.setBackground(Color.WHITE);
        JButton btnClose = new JButton("Đóng");
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.setBackground(Color.decode("#1E3A8A")); btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setFocusPainted(false); btnClose.setBorderPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dialog.dispose());
        pnlFooter.add(btnClose);

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlBody2,  BorderLayout.CENTER);
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
        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createLineBorder(Color.decode("#1E3A8A"), 2));
        pnlMain.setBackground(Color.WHITE);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.decode("#1E3A8A"));
        pnlHeader.setPreferredSize(new Dimension(0, 45));
        JLabel lblTitle = new JLabel(titleText, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15)); lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.CENTER);

        JTextArea msgArea = new JTextArea(message);
        msgArea.setFont(new Font("Segoe UI", Font.PLAIN, 14)); msgArea.setEditable(false);
        msgArea.setOpaque(false); msgArea.setLineWrap(true); msgArea.setWrapStyleWord(true);
        msgArea.setBorder(new EmptyBorder(20, 20, 10, 20));

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlFooter.setBackground(Color.WHITE);
        JButton btnYes = new JButton("Xác nhận");
        btnYes.setPreferredSize(new Dimension(110, 35));
        btnYes.setBackground(Color.decode("#1E3A8A")); btnYes.setForeground(Color.WHITE);
        btnYes.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnYes.setFocusPainted(false); btnYes.setBorderPainted(false);
        btnYes.addActionListener(e -> { result[0] = true; dialog.dispose(); });

        JButton btnNo = new JButton("Hủy");
        btnNo.setPreferredSize(new Dimension(80, 35));
        btnNo.setBackground(Color.decode("#64748B")); btnNo.setForeground(Color.WHITE);
        btnNo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNo.setFocusPainted(false); btnNo.setBorderPainted(false);
        btnNo.addActionListener(e -> dialog.dispose());

        pnlFooter.add(btnYes); pnlFooter.add(btnNo);
        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(msgArea,   BorderLayout.CENTER);
        pnlMain.add(pnlFooter, BorderLayout.SOUTH);
        dialog.setPreferredSize(new Dimension(400, 200));
        dialog.add(pnlMain);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        return result[0];
    }

    // =========================================================================
    // INNER CLASS — ToggleEditableTableModel
    // =========================================================================
    private static class ToggleEditableTableModel extends DefaultTableModel {
        private boolean editable = true;
        public ToggleEditableTableModel(Object[] cols, int rows) { super(cols, rows); }
        @Override public boolean isCellEditable(int r, int c) { return editable; }
        public void setEditable(boolean e) { this.editable = e; fireTableDataChanged(); }
    }

    // =========================================================================
    // XUẤT / NHẬP EXCEL
    // =========================================================================
    private void thucHienXuatExcelThang() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        java.awt.FileDialog fd = new java.awt.FileDialog(parentFrame, "Chọn nơi lưu file Excel", java.awt.FileDialog.SAVE);
        fd.setFile("DanhSachSanPham.xlsx"); fd.setVisible(true);
        String dir = fd.getDirectory(); String file = fd.getFile();
        if (dir != null && file != null) {
            String filePath = dir + file;
            if (!filePath.endsWith(".xlsx")) filePath += ".xlsx";
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            BUS_SanPham bus = new BUS_SanPham();
            List<SanPham> fullData = bus.getDsThuoc();
            if (fullData == null) { setCursor(Cursor.getDefaultCursor()); showCustomNotification("Lỗi", "Không thể lấy dữ liệu!", "ERROR"); return; }
            String result = exportExcelAction(filePath, fullData);
            setCursor(Cursor.getDefaultCursor());
            if (result.equals("SUCCESS")) showCustomNotification("XUẤT THÀNH CÔNG", "Đã xuất " + fullData.size() + " sản phẩm:\n" + filePath, "SUCCESS");
            else if (result.equals("FILE_OPEN")) showCustomNotification("LỖI GHI ĐÈ", "File đang mở! Đóng file Excel trước.", "ERROR");
            else showCustomNotification("LỖI", "Xuất thất bại: " + result, "ERROR");
        }
    }

    private String exportExcelAction(String path, List<SanPham> data) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh Sach San Pham");
            String[] headers = {"id","danhMuc","dang","ten","tenVietTat","nhaSanXuat","hoatChat","thueVAT","hamLuong","moTa","donViDoCoBan","ngayTao","trangThai","giaBan","maVach","nhomBenhLy"};
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]); cell.setCellStyle(headerStyle);
            }
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 1); SanPham sp = data.get(i);
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
                row.createCell(12).setCellValue("HOAT_DONG");
                row.createCell(13).setCellValue(sp.getGiaBan());
                row.createCell(14).setCellValue(sp.getMaVach() != null ? sp.getMaVach() : "");
                row.createCell(15).setCellValue(sp.getNhomBenhLy() != null ? sp.getNhomBenhLy() : "");
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(path)) { workbook.write(out); }
            return "SUCCESS";
        } catch (java.io.FileNotFoundException ex) { return "FILE_OPEN"; }
        catch (Exception ex) { ex.printStackTrace(); return ex.getMessage(); }
    }

    private void thucHienNhapExcelThang() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        java.awt.FileDialog fd = new java.awt.FileDialog(parentFrame, "Chọn file Excel để nhập", java.awt.FileDialog.LOAD);
        fd.setFile("*.xlsx;*.xls"); fd.setVisible(true);
        String dir = fd.getDirectory(); String file = fd.getFile();
        if (dir != null && file != null) {
            File fileToImport = new File(dir, file);
            if (!fileToImport.exists()) { showCustomNotification("LỖI FILE", "Không tìm thấy file!", "ERROR"); return; }
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try (java.io.FileInputStream fis = new java.io.FileInputStream(fileToImport);
                 Workbook workbook = new XSSFWorkbook(fis)) {
                Sheet sheet = workbook.getSheetAt(0);
                DataFormatter formatter = new DataFormatter();
                BUS_SanPham bus = new BUS_SanPham();
                int successCount = 0, updateCount = 0;
                List<Integer> errorRows = new ArrayList<>(), missingDataRows = new ArrayList<>();
                List<SanPham> dsHienTai = bus.getDsThuoc();

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    String ten = formatter.formatCellValue(row.getCell(3)).trim();
                    if (ten.isEmpty()) continue;
                    String loai = formatter.formatCellValue(row.getCell(1)).trim();
                    String dang = formatter.formatCellValue(row.getCell(2)).trim();
                    if (loai.isEmpty() || dang.isEmpty() || loai.equals("NULL") || dang.equals("NULL")) {
                        missingDataRows.add(i + 1); continue;
                    }
                    try {
                        String vietTat  = formatter.formatCellValue(row.getCell(4)).trim();
                        String nsx      = formatter.formatCellValue(row.getCell(5)).trim();
                        String hoatChat = formatter.formatCellValue(row.getCell(6)).trim();
                        double vat = 10.0;
                        try { String vs = formatter.formatCellValue(row.getCell(7)).replace("%","").trim(); if (!vs.isEmpty() && !vs.equals("NULL")) vat = Double.parseDouble(vs); } catch (Exception ignored) {}
                        String hamLuong = formatter.formatCellValue(row.getCell(8)).trim(); if (hamLuong.equals("NULL")) hamLuong = "";
                        String moTa     = formatter.formatCellValue(row.getCell(9)).trim();  if (moTa.equals("NULL")) moTa = "";
                        String donVi    = formatter.formatCellValue(row.getCell(10)).trim(); if (donVi.equals("NULL") || donVi.isEmpty()) donVi = "Hộp";
                        // Cột 14: maVach, 15: nhomBenhLy (nếu có trong file)
                        String maVach   = row.getCell(14) != null ? formatter.formatCellValue(row.getCell(14)).trim() : "";
                        String nhomBL   = row.getCell(15) != null ? formatter.formatCellValue(row.getCell(15)).trim() : "";

                        SanPham spCu = null;
                        for (SanPham sp : dsHienTai) { if (sp.getTen().equalsIgnoreCase(ten)) { spCu = sp; break; } }

                        if (spCu != null) {
                            bus.capNhatSP(spCu.getId(), mapToDbDanhMuc(loai), mapToDbDang(dang), ten, vietTat, nsx, hoatChat, vat, hamLuong, moTa, donVi, spCu.getGiaBan(), maVach, nhomBL, new ArrayList<>());
                            updateCount++;
                        } else {
                            double giaBanMoi = 0;
                            try { String gs = formatter.formatCellValue(row.getCell(13)).replace(",","").replace(".","").trim(); if (!gs.isEmpty() && !gs.equals("NULL")) giaBanMoi = Double.parseDouble(gs); } catch (Exception ignored) {}
                            if (giaBanMoi < 0) giaBanMoi = 0;
                            String newId = bus.taoMaMoi();
                            boolean isSaved = bus.themSP(newId, mapToDbDanhMuc(loai), mapToDbDang(dang), ten, vietTat, nsx, hoatChat, vat, hamLuong, moTa, donVi, giaBanMoi, maVach, nhomBL, new ArrayList<>());
                            if (isSaved) { successCount++; SanPham ns = new SanPham(); ns.setId(newId); ns.setTen(ten); dsHienTai.add(ns); }
                        }
                    } catch (Exception rowEx) { errorRows.add(i + 1); }
                }

                setCursor(Cursor.getDefaultCursor());
                StringBuilder msg = new StringBuilder("Quá trình xử lý hoàn tất:\n");
                msg.append("   • Thêm mới: ").append(successCount).append("\n");
                msg.append("   • Cập nhật: ").append(updateCount);
                boolean hasError = false;
                if (!missingDataRows.isEmpty()) { msg.append("\n⚠ Bỏ qua dòng thiếu dữ liệu: ").append(missingDataRows); hasError = true; }
                if (!errorRows.isEmpty())       { msg.append("\n⚠ Lỗi format dòng: ").append(errorRows); hasError = true; }
                showCustomNotification(hasError ? "NHẬP CÓ CẢNH BÁO" : "NHẬP THÀNH CÔNG", msg.toString(), hasError ? "WARNING" : "SUCCESS");
                loadDataFromDatabase();
            } catch (Exception ex) {
                setCursor(Cursor.getDefaultCursor());
                ex.printStackTrace();
                showCustomNotification("LỖI HỆ THỐNG", "Không thể đọc file:\n" + ex.getMessage(), "ERROR");
            }
        }
    }

    /**
     * Đồng bộ giá bán vào bảng đơn vị:
     * - Cập nhật dòng ĐVT gốc (tỷ lệ = 1) theo txtGiaBan
     * - Tự động tính giá các dòng khác = giaBanGoc × tỷ lệ
     */
    private JButton createSmallBtn(String text, String hex) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(Color.decode(hex));
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(120, 28));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void syncGiaBanVaoBangDonVi() {
        if (txtGiaBan == null) return;
        String gs = txtGiaBan.getText().trim().replace(",", "");
        if (gs.isEmpty()) return;
        try { giaBanGoc = Double.parseDouble(gs); } catch (Exception e) { return; }
        if (giaBanGoc <= 0) return;

        String dvtGoc = cbDVT != null && cbDVT.getSelectedItem() != null ? cbDVT.getSelectedItem().toString().trim() : "";

        uomAutoChanging = true;
        for (int i = 0; i < modelDonVi.getRowCount(); i++) {
            Object tenObj = modelDonVi.getValueAt(i, 0);
            Object tiLeObj = modelDonVi.getValueAt(i, 1);
            if (tenObj == null || tenObj.toString().trim().isEmpty()) continue;
            String ten = tenObj.toString().trim();
            double tiLe = 1.0;
            try { tiLe = Double.parseDouble(tiLeObj.toString()); } catch (Exception ex) {}

            // Dòng ĐVT gốc: tỷ lệ = 1, giá = giaBanGoc
            if (ten.equalsIgnoreCase(dvtGoc) || tiLe == 1.0) {
                modelDonVi.setValueAt("1.0", i, 1);
                modelDonVi.setValueAt(String.format("%,.0f", giaBanGoc), i, 2);
                // Đã xóa dòng đẩy mã vạch xuống bảng vì bảng chỉ còn 3 cột
            } else {
                long giaAuto = Math.round(giaBanGoc * tiLe);
                modelDonVi.setValueAt(String.format("%,.0f", (double)giaAuto), i, 2);
            }
        }
        uomAutoChanging = false;
    }
    /**
     * Phân loại mã vạch đúng chuẩn GS1.
     * - Prefix 200-299: mã nội bộ nhà thuốc (tự sinh, không đăng ký GS1)
     * - Prefix khác + 13 chữ số: EAN-13 thật của nhà sản xuất
     * - 8 chữ số: EAN-8 (GS1)
     * - Có chữ hoặc gạch: Code 128 nội bộ (mã lô, mã SP)
     */
    private String phanLoaiMaVach(String mv) {
        if (mv == null || mv.isEmpty()) return "Không xác định";
        String digits = mv.replaceAll("[^0-9]", "");
        if (mv.equals(digits)) {
            if (mv.length() == 13) {
                int prefix3 = Integer.parseInt(mv.substring(0, 3));
                if (prefix3 >= 200 && prefix3 <= 299) return "Nội bộ EAN-13";
                return "EAN-13 (GS1)";
            }
            if (mv.length() == 8)  return "EAN-8 (GS1)";
            if (mv.length() == 12) return "UPC-A (GS1)";
            if (mv.length() == 14) return "GTIN-14 (GS1)";
        }
        return "Nội bộ (Code 128)";
    }

    /**
     * Tinh check digit EAN-13 theo chuan GS1.
     * Input: 12 chu so dau. Output: check digit (0-9).
     */
    private int tinhCheckDigitEAN13(String digits12) {
        if (digits12 == null || digits12.length() < 12) return 0;
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int d = Character.getNumericValue(digits12.charAt(i));
            sum += (i % 2 == 0) ? d : d * 3;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * Chuan hoa ma vach EAN-13: giu 12 digits dau, tinh lai check digit.
     * Tra ve null neu khong phai dinh dang EAN (12 hoac 13 chu so).
     */
    private String chuanHoaEAN13(String ma) {
        if (ma == null) return null;
        String digits = ma.replaceAll("[^0-9]", "");
        if (digits.length() == 13) digits = digits.substring(0, 12);
        if (digits.length() != 12) return null;
        return digits + tinhCheckDigitEAN13(digits);
    }

    private void thucHienInMaVach1D(String barcodeData, String productName, String price) {
        // Tạo ảnh Code128 to để hiển thị trên màn hình quét
        java.awt.image.BufferedImage barcodeImg;
        try {
            Code128Writer writer = new Code128Writer();
            barcodeImg = MatrixToImageWriter.toBufferedImage(
                writer.encode(barcodeData, BarcodeFormat.CODE_128, 520, 150));
        } catch (Exception ex) {
            showCustomNotification("LỖI", "Không tạo được mã vạch: " + ex.getMessage(), "ERROR");
            return;
        }
        final java.awt.image.BufferedImage finalImg = barcodeImg;

        // Dialog hiển thị mã vạch to trên màn hình
        JDialog dlg = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this),
            "In / Xuất mã vạch sản phẩm", true);
        dlg.setUndecorated(false);

        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(24, 32, 20, 32));

        // Tên sản phẩm
        JLabel lblTen = new JLabel(productName, SwingConstants.CENTER);
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTen.setForeground(Color.decode("#1E293B"));

        // Mã vạch — vẽ bằng paintComponent để sắc nét hơn
        JPanel pnlImg = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                int margin = 8;
                g2.drawImage(finalImg, margin, margin,
                    getWidth() - margin * 2, getHeight() - margin * 2, null);
            }
        };
        pnlImg.setPreferredSize(new Dimension(520, 166));
        pnlImg.setBackground(Color.WHITE);
        pnlImg.setBorder(BorderFactory.createLineBorder(Color.decode("#E2E8F0"), 1));

        // Số mã
        JLabel lblSo = new JLabel(barcodeData, SwingConstants.CENTER);
        lblSo.setFont(new Font("Courier New", Font.BOLD, 17));
        lblSo.setForeground(Color.decode("#334155"));

        // Giá tiền
        String priceFmt = price.isEmpty() || price.equals("0") ? "" : price + " đ";
        JLabel lblGia = new JLabel(priceFmt, SwingConstants.CENTER);
        lblGia.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblGia.setForeground(Color.decode("#E11D48"));

        // --- NÚT MỚI: XUẤT ẢNH ---
        JButton btnXuatAnh = new JButton("Xuất ảnh");
        btnXuatAnh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnXuatAnh.setBackground(Color.decode("#22C55E")); // Màu xanh lá
        btnXuatAnh.setForeground(Color.WHITE);
        btnXuatAnh.setBorderPainted(false);
        btnXuatAnh.setFocusPainted(false);
        btnXuatAnh.setOpaque(true);
        btnXuatAnh.setPreferredSize(new Dimension(110, 36));
        btnXuatAnh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnXuatAnh.addActionListener(ev -> {
            java.awt.Frame parentFrame = (java.awt.Frame) SwingUtilities.getWindowAncestor(this);
            java.awt.FileDialog fd = new java.awt.FileDialog(parentFrame, "Chọn nơi lưu ảnh mã tem dán", java.awt.FileDialog.SAVE);
            fd.setFile(barcodeData + ".png");
            fd.setVisible(true);
            String dir = fd.getDirectory();
            String file = fd.getFile();
            if (dir != null && file != null) {
                String filePath = dir + file;
                if (!filePath.toLowerCase().endsWith(".png")) filePath += ".png";
                try {
                    // Tự động tạo ảnh phôi tem
                    java.awt.image.BufferedImage labelImg = new java.awt.image.BufferedImage(540, 240, java.awt.image.BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = labelImg.createGraphics();
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(0, 0, 540, 240);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString(productName, (540 - fm.stringWidth(productName)) / 2, 35);
                    
                    g2d.drawImage(finalImg, 10, 55, 520, 110, null);
                    
                    g2d.setFont(new Font("Courier New", Font.BOLD, 16));
                    fm = g2d.getFontMetrics();
                    g2d.drawString(barcodeData, (540 - fm.stringWidth(barcodeData)) / 2, 185);
                    
                    if (!priceFmt.isEmpty()) {
                        g2d.setColor(Color.decode("#E11D48"));
                        g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                        fm = g2d.getFontMetrics();
                        g2d.drawString(priceFmt, (540 - fm.stringWidth(priceFmt)) / 2, 215);
                    }
                    
                    g2d.dispose();
                    javax.imageio.ImageIO.write(labelImg, "png", new java.io.File(filePath));
                    showCustomNotification("XUẤT ẢNH THÀNH CÔNG", "Đã lưu ảnh mã tem dán thành công tại:\n" + filePath, "SUCCESS");
                } catch (Exception ex) {
                    showCustomNotification("LỖI", "Không lưu được file ảnh: " + ex.getMessage(), "ERROR");
                }
            }
        });

     // --- NÚT MỚI: IN TRỰC TIẾP RA MÁY IN ---
        JButton btnInTrucTiep = new JButton("In máy in");
        btnInTrucTiep.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnInTrucTiep.setBackground(Color.decode("#0EA5E9")); // Màu xanh dương nhạt
        btnInTrucTiep.setForeground(Color.WHITE);
        btnInTrucTiep.setBorderPainted(false);
        btnInTrucTiep.setFocusPainted(false);
        btnInTrucTiep.setOpaque(true);
        btnInTrucTiep.setPreferredSize(new Dimension(110, 36));
        btnInTrucTiep.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnInTrucTiep.addActionListener(ev -> {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
                
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                
                // Thu nhỏ hình mã vạch để vừa với khổ giấy in tem (vd: máy in nhiệt Xprinter)
                double width = 150; // Độ rộng tem in thực tế (tùy chỉnh theo máy)
                double height = width * finalImg.getHeight() / finalImg.getWidth();
                
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2d.drawString(productName, 10, 15); // Vẽ tên SP
                g2d.drawImage(finalImg, 10, 20, (int)width, (int)height, null); // Vẽ mã vạch
                g2d.setFont(new Font("Courier New", Font.PLAIN, 10));
                g2d.drawString(barcodeData, 10, (int)height + 35); // Vẽ mã số
                if (!priceFmt.isEmpty()) {
                    g2d.drawString("Gia: " + priceFmt, 10, (int)height + 50); // Vẽ giá
                }
                return Printable.PAGE_EXISTS;
            });

            if (job.printDialog()) {
                try {
                    job.print();
                    showCustomNotification("IN THÀNH CÔNG", "Đã gửi lệnh in đến máy in.", "SUCCESS");
                } catch (PrinterException ex) {
                    showCustomNotification("LỖI IN", "Không thể in: " + ex.getMessage(), "ERROR");
                }
            }
        });

        // Nút Đóng
        JButton btnDong = new JButton("Đóng");
        btnDong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDong.setBackground(COLOR_PRIMARY);
        btnDong.setForeground(Color.WHITE);
        btnDong.setBorderPainted(false);
        btnDong.setFocusPainted(false);
        btnDong.setOpaque(true);
        btnDong.setPreferredSize(new Dimension(110, 36));
        btnDong.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDong.addActionListener(ev -> dlg.dispose());

        // Gom các nút vào panel
        JPanel pnlBtn = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 0));
        pnlBtn.setBackground(Color.WHITE);
        pnlBtn.add(btnXuatAnh);     // Giữ lại nút xuất ảnh của bạn
        pnlBtn.add(btnInTrucTiep);  // Thêm nút in máy in
        pnlBtn.add(btnDong);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);
        
        for (JComponent comp : new JComponent[]{lblTen, pnlImg, lblSo, lblGia}) {
            comp.setAlignmentX(0.5f);
            center.add(comp);
            center.add(Box.createVerticalStrut(8));
        }

        main.add(center, BorderLayout.CENTER);
        main.add(pnlBtn, BorderLayout.SOUTH);

        dlg.setContentPane(main);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(580, 360)); 
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
 // --- VẼ DANH SÁCH MÃ VẠCH CARD ---
    private void renderMaVachList() {
        if (pnlMaVachList == null) return;
        pnlMaVachList.removeAll();
        pnlMaVachList.add(Box.createRigidArea(new Dimension(0, 5)));
        for (int i = 0; i < listMaVachData.size(); i++) {
            String[] mv = listMaVachData.get(i);
            // Bug1-fix: mv[2]="LO" → mã QR nội bộ lô, chỉ hiển thị, không lưu vào SP
            boolean readOnly = mv.length > 2 && "LO".equals(mv[2]);
            pnlMaVachList.add(createMaVachCard(mv[0], mv[1], i, readOnly));
            pnlMaVachList.add(Box.createRigidArea(new Dimension(0, 6)));
        }
        pnlMaVachList.revalidate();
        pnlMaVachList.repaint();
    }

    private JPanel createMaVachCard(String barcode, String loai, int index, boolean readOnly) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                readOnly ? Color.decode("#FDE68A") : Color.decode("#E2E8F0"), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlLeft.setBackground(Color.WHITE);
        
        JLabel lblIcon = new JLabel("||||");
        lblIcon.setForeground(Color.decode("#94A3B8"));
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 15));
        
        JLabel lblCode = new JLabel(barcode);
        lblCode.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCode.setForeground(Color.decode("#1E293B"));
        
        JLabel lblBadge = new JLabel(loai) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        lblBadge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblBadge.setOpaque(false);
        lblBadge.setBorder(new EmptyBorder(4, 10, 4, 10));
        
        if (readOnly) {
            // Bug1-fix: mã QR nội bộ lô — nền vàng nhạt, nhãn rõ ràng
            lblBadge.setBackground(Color.decode("#FEF9C3")); lblBadge.setForeground(Color.decode("#854D0E"));
        } else if (loai.contains("★")) {
            lblBadge.setBackground(Color.decode("#DBEAFE")); lblBadge.setForeground(Color.decode("#1D4ED8"));
        } else if (loai.contains("GS1") || loai.contains("UPC") || loai.contains("GTIN")) {
            lblBadge.setBackground(Color.decode("#DCFCE7")); lblBadge.setForeground(Color.decode("#15803D"));
        } else {
            lblBadge.setBackground(Color.decode("#F0F9FF")); lblBadge.setForeground(Color.decode("#0369A1"));
        }
        
        pnlLeft.add(lblIcon); pnlLeft.add(lblCode); pnlLeft.add(lblBadge);

        card.add(pnlLeft, BorderLayout.CENTER);

        if (readOnly) {
            // Bug1-fix: không cho xóa mã QR lô — chỉ xem tham khảo
            JLabel lblRO = new JLabel("Mã lô");
            lblRO.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            lblRO.setForeground(Color.decode("#92400E"));
            card.add(lblRO, BorderLayout.EAST);
        } else {
            JButton btnDel = new JButton(new MenuIcon("TRASH"));
            btnDel.setBackground(Color.WHITE);
            btnDel.setBorder(null);
            btnDel.setContentAreaFilled(false);
            btnDel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDel.setToolTipText("Xóa mã vạch này");
            btnDel.addActionListener(e -> {
                listMaVachData.remove(index);
                renderMaVachList();
            });
            card.add(btnDel, BorderLayout.EAST);
        }
        return card;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // VỊ TRÍ THUỐC — dùng BUS_SanPham + SanPham.ViTriThuoc (inner class)
    // ══════════════════════════════════════════════════════════════════════════
    /** Load vị trí thuốc qua BUS — dsViTriData: List<SanPham.ViTriThuoc> */
    private void loadDsViTri() {
        dsViTriData.clear();
        BUS_SanPham busVT = new BUS_SanPham();
        List<SanPham.ViTriThuoc> ds = busVT.layTatCaViTri();
        for (SanPham.ViTriThuoc vt : ds) {
            dsViTriData.add(new Object[]{vt.getId(), vt.getKhu(), vt.getKe(), vt.getTang()});
        }
    }
 // =========================================================================
    // TOÀN BỘ LOGIC EVENT TÍCH HỢP TỪ PANELCATLIEU VÀO MANHINHSANPHAM
    // =========================================================================
    private void loadDanhSachComboNangCaoRaGiaoDien() {
        loadDanhSachComboNangCaoRaGiaoDien(false);
    }

    /**
     * @param boQuaLoc true = load tất cả không lọc (dùng sau khi lưu/xóa)
     *                 false = lọc theo nhóm bệnh đang chọn
     */
    private void loadDanhSachComboNangCaoRaGiaoDien(boolean boQuaLoc) {
        dangCapNhatBangLieu = true;
        mdlDanhSachLieuNangCao.setRowCount(0);
        BUS_SanPham bus = new BUS_SanPham();

        String nhomLoc = "";
        if (!boQuaLoc && cbNhomBenhLieuNangCao != null
                && cbNhomBenhLieuNangCao.getSelectedItem() != null) {
            nhomLoc = cbNhomBenhLieuNangCao.getSelectedItem().toString().trim();
        }

        for (Object[] r : bus.layDanhSachComboNangCao()) {
            if (nhomLoc.isEmpty() || nhomLoc.startsWith("--")
                    || r[2].toString().equalsIgnoreCase(nhomLoc)) {
                mdlDanhSachLieuNangCao.addRow(new Object[]{
                    r[0], r[1], r[2], String.format("%,.0f đ", (Double) r[3])});
            }
        }
        dangCapNhatBangLieu = false;
    }

    private void hookAutocompleteLieuNangCao() {
        autocompleteTimerLieu = new javax.swing.Timer(280, e -> thucHienAutocompleteLieu());
        autocompleteTimerLieu.setRepeats(false);
 
        txtSearchLieuNangCao.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                if (!dangCapNhatBangLieu && !dangChonTuPopup) scheduleLieuAuto();
            }
            @Override public void removeUpdate(DocumentEvent e) {
                if (!dangCapNhatBangLieu && !dangChonTuPopup) {
                    if (txtSearchLieuNangCao.getText().trim().isEmpty()) {
                        maSPDangChonLieu = null;
                        lblDVTLieu.setText("---");
                        lblGiaLieu.setText("---");
                        popupTimThuoc.setVisible(false);
                    } else {
                        scheduleLieuAuto();
                    }
                }
            }
            @Override public void changedUpdate(DocumentEvent e) {}
        });
 
        // Enter → quét barcode nhanh
        txtSearchLieuNangCao.addActionListener(e -> xuLyQuetBarcodeNhanhLieu());
    }

    private void xuLyQuetBarcodeNhanhLieu() {
        String code = txtSearchLieuNangCao.getText().trim();
        if (code.isEmpty()) return;

        // Chỉ xử lý barcode nếu chuỗi có vẻ là mã vạch thực sự:
        // >= 8 ký tự, chỉ gồm số/chữ không dấu, không có khoảng trắng ở giữa
        boolean coTheLaBarcode = code.length() >= 8
            && code.matches("[A-Za-z0-9\\-\\.]+");

        if (!coTheLaBarcode) {
            // Người dùng chỉ nhấn Enter sau khi gõ tên thuốc
            // → nếu popup đang hiện và có item được chọn thì chọn item đó
            if (popupTimThuoc.isVisible()) {
                int cur = listPopupThuoc.getSelectedIndex();
                if (cur >= 0 && cur < cacheKetQuaTimKiemLieu.size()) {
                    chonThuocTuPopup(cur);
                }
            }
            return;
        }

        autocompleteTimerLieu.stop();
        BUS_SanPham bus = new BUS_SanPham();
        SanPham sp = bus.getSanPhamByBarcode(code);

        if (sp != null) {
            maSPDangChonLieu  = sp.getId();
            tenSPDangChonLieu = sp.getTen();
            dvtDangChonLieu   = sp.getDonViDoCoBan();

            Object[] uomMin = bus.getDonViNhoNhat(sp.getId());
            giaDangChonLieu = (uomMin != null) ? (Double) uomMin[1] : sp.getGiaBan();

            lblDVTLieu.setText(dvtDangChonLieu);
            lblGiaLieu.setText(String.format("%,.0f đ", giaDangChonLieu));
            popupTimThuoc.setVisible(false);

            // Barcode match → hỏi xác nhận trước khi đưa vào toa
            // (không tự động thêm luôn để tránh nhầm)
            dangChonTuPopup = true;
            txtSearchLieuNangCao.setText(tenSPDangChonLieu + " | " + dvtDangChonLieu);
            dangChonTuPopup = false;
        } else {
            // Không phải barcode hợp lệ → tìm kiếm thường
            thucHienAutocompleteLieu();
        }
    }

    private void scheduleLieuAuto() {
        autocompleteTimerLieu.stop(); String text = txtSearchLieuNangCao.getText();
        if (text != null && text.trim().length() >= 2) autocompleteTimerLieu.start();
    }

    private void thucHienAutocompleteLieu() {
        String kw = txtSearchLieuNangCao.getText();
        if (kw == null || kw.trim().length() < 2) {
            popupTimThuoc.setVisible(false);
            return;
        }

        List<Object[]> res = new BUS_SanPham().timKiemThuocChoMauLieu(kw.trim());
        cacheKetQuaTimKiemLieu = res;

        modelPopupThuoc.clear();

        if (res.isEmpty()) {
            modelPopupThuoc.addElement("  (Không tìm thấy thuốc phù hợp)");
            // Khi click vào item "không tìm thấy" thì không làm gì
            // → mouseClicked check idx < cacheKetQuaTimKiemLieu.size() sẽ chặn
            int popupHeight = 32;
            popupTimThuoc.setPreferredSize(
                new Dimension(txtSearchLieuNangCao.getWidth() + 200, popupHeight));
            popupTimThuoc.show(txtSearchLieuNangCao, 0, txtSearchLieuNangCao.getHeight());
            listPopupThuoc.clearSelection(); // không cho chọn
        } else {
            for (Object[] r : res) {
                modelPopupThuoc.addElement(
                    String.format("%s | %s | Giá: %,.0f đ", r[1], r[2], (Double) r[3]));
            }
            int popupHeight = Math.min(res.size(), 8) * 28 + 4;
            popupTimThuoc.setPreferredSize(
                new Dimension(txtSearchLieuNangCao.getWidth() + 200, popupHeight));
            popupTimThuoc.show(txtSearchLieuNangCao, 0, txtSearchLieuNangCao.getHeight());
            listPopupThuoc.setSelectedIndex(0);
        }

        // Trả focus về ô tìm kiếm để tiếp tục gõ
        txtSearchLieuNangCao.requestFocusInWindow();
    }

    private void themThuocVaoDocLieu() {
        // Kiểm tra đã chọn thuốc từ danh sách chưa
        if (maSPDangChonLieu == null || maSPDangChonLieu.trim().isEmpty()) {
            showCustomNotification("CHƯA CHỌN THUỐC",
                "Hãy gõ tên thuốc → chọn từ danh sách gợi ý → rồi nhấn \"Đưa vào toa\".", "WARNING");
            txtSearchLieuNangCao.requestFocus();
            return;
        }

        // Kiểm tra trùng lặp
        for (SanPham.ChiTietLieu ct : dsChiTietLieuLuuTru) {
            if (ct.getSanPhamId().equals(maSPDangChonLieu)) {
                showCustomNotification("TRÙNG THUỐC",
                    tenSPDangChonLieu + " đã có trong toa rồi!", "WARNING");
                return;
            }
        }
 
        try {
            double s = parseDoubleSafe(txtSangLieu.getText());
            double t = parseDoubleSafe(txtTruaLieu.getText());
            double c = parseDoubleSafe(txtChieuLieu.getText());
            double o = parseDoubleSafe(txtToiLieu.getText());
            int    n = parseIntSafe(txtSoNgayLieu.getText());
 
            if (s < 0 || t < 0 || c < 0 || o < 0) {
                showCustomNotification("LIỀU LƯỢNG ÂM", "Liều dùng các buổi không được âm!", "ERROR");
                return;
            }
            if (n <= 0) {
                showCustomNotification("SỐ NGÀY KHÔNG HỢP LỆ", "Số ngày dùng thuốc phải lớn hơn 0!", "ERROR");
                return;
            }
            if ((s + t + c + o) <= 0) {
                showCustomNotification("THIẾU LIỀU LƯỢNG",
                    "Ít nhất một buổi (Sáng/Trưa/Chiều/Tối) phải có liều lượng lớn hơn 0!", "WARNING");
                return;
            }
 
            String cd = cbCachDungLieuNangCao.getSelectedItem() != null
                ? cbCachDungLieuNangCao.getSelectedItem().toString()
                : "Sau ăn 30 phút";
 
            SanPham.ChiTietLieu ct = new SanPham.ChiTietLieu(
                comboIdDangSua, maSPDangChonLieu, tenSPDangChonLieu,
                dvtDangChonLieu, s, t, c, o, cd, n, giaDangChonLieu);
 
            // Cảnh báo vượt tồn kho
            int tonKho = new BUS_SanPham().getSoLuongTon(maSPDangChonLieu);
            if (ct.getTongSoLuong() > tonKho) {
            	if (!showCustomConfirm("VƯỢT TỒN KHO",
            	        "<b>" + tenSPDangChonLieu + "</b> chỉ còn <b>" + tonKho
            	        + "</b> " + dvtDangChonLieu + " trong kho.<br>"
            	        + "Toa yêu cầu <b>" + ct.getTongSoLuong() + "</b> " + dvtDangChonLieu + ".<br><br>"
            	        + "Vẫn tiếp tục thêm vào toa?")) return;
            }

            dsChiTietLieuLuuTru.add(ct);
            HienThiDongChiTietMoiLenBang(ct);
            capNhatTongTienLieuHienTai();

            // Reset ô tìm kiếm và state
            maSPDangChonLieu = null;
            tenSPDangChonLieu = null;
            dvtDangChonLieu = null;
            giaDangChonLieu = 0;
            dangChonTuPopup = true;
            txtSearchLieuNangCao.setText("");
            dangChonTuPopup = false;
            lblDVTLieu.setText("---");
            lblGiaLieu.setText("---");
            // [FIX 3B] Reset liều dùng về mặc định để tránh kế thừa liều thuốc trước
            txtSangLieu.setText("1");
            txtTruaLieu.setText("0");
            txtChieuLieu.setText("0");
            txtToiLieu.setText("1");
            txtSoNgayLieu.setText("5");
            txtSearchLieuNangCao.requestFocus();

        } catch (Exception ex) {
            showCustomNotification("SAI ĐỊNH DẠNG",
                "Vui lòng nhập số hợp lệ cho liều lượng và số ngày!", "ERROR");
        }
    }
 
    /** Parse double an toàn — trả 0 nếu rỗng, ném exception nếu không phải số */
    private double parseDoubleSafe(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        return Double.parseDouble(s.trim().replace(",", "."));
    }
 
    /** Parse int an toàn — trả 0 nếu rỗng, ném exception nếu không phải số */
    private int parseIntSafe(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        return Integer.parseInt(s.trim());
    }

    private void HienThiDongChiTietMoiLenBang(SanPham.ChiTietLieu ct) {
        // FIX #6: nhớ trạng thái của caller — không cứng false cuối hàm
        // Lý do: onChonMauLieuTuDanhSach() giữ flag=true trong vòng lặp, nhưng hàm con
        // cứng đặt false sau mỗi lần → các addRow tiếp theo không còn được bảo vệ
        boolean wasLocked = dangCapNhatBangLieu;
        dangCapNhatBangLieu = true;
        modChiTietLieu.addRow(new Object[]{ ct.getSanPhamId(), ct.getTenSanPham(), ct.getDvt(), ct.getSang(), ct.getTrua(), ct.getChieu(), ct.getToi(), ct.getCachDung(), ct.getSoNgay(), ct.getTongSoLuong(), String.format("%,.0f", ct.tinhThanhTien()), "Xóa" });
        dangCapNhatBangLieu = wasLocked; // khôi phục đúng trạng thái của caller
    }

    private void onTableCellLieuChanged(javax.swing.event.TableModelEvent e) {
        if (dangCapNhatBangLieu) return; int row = e.getFirstRow(), col = e.getColumn();
        if (row < 0 || row >= dsChiTietLieuLuuTru.size()) return;
        SanPham.ChiTietLieu ct = dsChiTietLieuLuuTru.get(row);
        try {
            dangCapNhatBangLieu = true;
            // FIX #7: validate giá trị âm — chặn ngay tại đây, không để tính toán sai
            switch(col) {
                case COL_L_SANG: {
                    double v = Double.parseDouble(modChiTietLieu.getValueAt(row, col).toString());
                    if (v < 0) throw new NumberFormatException("Liều Sáng < 0");
                    ct.setSang(v); break;
                }
                case COL_L_TRUA: {
                    double v = Double.parseDouble(modChiTietLieu.getValueAt(row, col).toString());
                    if (v < 0) throw new NumberFormatException("Liều Trưa < 0");
                    ct.setTrua(v); break;
                }
                case COL_L_CHIEU: {
                    double v = Double.parseDouble(modChiTietLieu.getValueAt(row, col).toString());
                    if (v < 0) throw new NumberFormatException("Liều Chiều < 0");
                    ct.setChieu(v); break;
                }
                case COL_L_TOI: {
                    double v = Double.parseDouble(modChiTietLieu.getValueAt(row, col).toString());
                    if (v < 0) throw new NumberFormatException("Liều Tối < 0");
                    ct.setToi(v); break;
                }
                case COL_L_NGAY: {
                    int n = Integer.parseInt(modChiTietLieu.getValueAt(row, col).toString());
                    if (n <= 0) throw new NumberFormatException("Số ngày phải >= 1");
                    ct.setSoNgay(n); break;
                }
            }
            modChiTietLieu.setValueAt(ct.getTongSoLuong(), row, COL_L_TONG_SL);
            modChiTietLieu.setValueAt(String.format("%,.0f", ct.tinhThanhTien()), row, COL_L_THANH_TIEN);
            // [FIX 1A] Kiểm tra lại tồn kho sau khi người dùng sửa liều trực tiếp trên bảng
            int tonKho = new BUS_SanPham().getSoLuongTon(ct.getSanPhamId());
            if (ct.getTongSoLuong() > tonKho) {
                SwingUtilities.invokeLater(() ->
                    showCustomNotification("VƯỢT TỒN KHO",
                        ct.getTenSanPham() + " chỉ còn " + tonKho
                        + " " + ct.getDvt() + " trong kho, nhưng toa đang yêu cầu "
                        + ct.getTongSoLuong() + " " + ct.getDvt() + ".",
                        "WARNING")
                );
            }
        } catch(Exception ignored) {
            // FIX #7: rollback — khôi phục giá trị hợp lệ trước đó lên ô vừa nhập sai
            switch(col) {
                case COL_L_SANG:  modChiTietLieu.setValueAt(ct.getSang(),   row, col); break;
                case COL_L_TRUA:  modChiTietLieu.setValueAt(ct.getTrua(),   row, col); break;
                case COL_L_CHIEU: modChiTietLieu.setValueAt(ct.getChieu(),  row, col); break;
                case COL_L_TOI:   modChiTietLieu.setValueAt(ct.getToi(),    row, col); break;
                case COL_L_NGAY:  modChiTietLieu.setValueAt(ct.getSoNgay(), row, col); break;
            }
        } finally { dangCapNhatBangLieu = false; }
        capNhatTongTienLieuHienTai();
    }

    private void capNhatTongTienLieuHienTai() {
        double tong = dsChiTietLieuLuuTru.stream()
            .mapToDouble(SanPham.ChiTietLieu::tinhThanhTien).sum();
        lblTongTienLieuNangCao.setText(String.format("%,.0f đ", tong));
        txtGiaBanComboNangCao.setValue((long) tong);
    }

    private void resetFormSoanThaoLieu() {
        comboIdDangSua = null; dsChiTietLieuLuuTru.clear(); modChiTietLieu.setRowCount(0);
        txtTenComboNangCao.setText(""); txtGhiChuLieuNangCao.setText("");
        txtGiaBanComboNangCao.setValue(0L);
        lblComboIdNangCao.setText("(Tự động sinh)"); lblTongTienLieuNangCao.setText("0 đ");
        // [FIX 5] Bỏ highlight hàng cũ để ListSelectionListener kích hoạt được lần sau
        tblDanhSachLieuNangCao.clearSelection();
    }

    private void onChonMauLieuTuDanhSach() {
        int r = tblDanhSachLieuNangCao.getSelectedRow(); if (r < 0) return;
        String id = mdlDanhSachLieuNangCao.getValueAt(r, 0).toString();
        BUS_SanPham bus = new BUS_SanPham(); SanPham.MauLieu m = bus.layComboByIdNangCao(id);
        if (m == null) return;
        
        dangCapNhatBangLieu = true; // Khóa giữ trạng thái, không cho phép gãy dòng dữ liệu
        comboIdDangSua = m.getComboId(); txtTenComboNangCao.setText(m.getTenCombo());
        cbNhomBenhLieuNangCao.setSelectedItem(m.getNhomBenh()); 
        txtGiaBanComboNangCao.setValue((long)m.getGiaBanCombo());
        txtGhiChuLieuNangCao.setText(m.getGhiChu()); lblComboIdNangCao.setText(m.getComboId());
        dsChiTietLieuLuuTru.clear(); modChiTietLieu.setRowCount(0);
        BUS_SanPham busGia = new BUS_SanPham(); // [FIX 2B] dùng chung 1 instance để tránh N kết nối DB
        for (SanPham.ChiTietLieu ct : m.getDsChiTiet()) {
            // [FIX 2B] Làm mới giá theo giá hiện tại thay vì dùng giá snapshot trong DB
            Object[] uomMin = busGia.getDonViNhoNhat(ct.getSanPhamId());
            if (uomMin != null) {
                ct.setGiaDonVi((Double) uomMin[1]);
            } else {
            	SanPham spMoi = busGia.getSanPhamDayDu(ct.getSanPhamId());
                if (spMoi != null) ct.setGiaDonVi(spMoi.getGiaBan());
            }
            dsChiTietLieuLuuTru.add(ct); HienThiDongChiTietMoiLenBang(ct);
        }
        capNhatTongTienLieuHienTai();
        dangCapNhatBangLieu = false; // Mở khóa
    }

    private void luuMauLieuChuyenSau() {
        String ten = txtTenComboNangCao.getText().trim(); 
        String nbRaw = cbNhomBenhLieuNangCao.getSelectedItem() != null
                ? cbNhomBenhLieuNangCao.getSelectedItem().toString()
                : "";
        String nb = (nbRaw.startsWith("--")) ? "" : nbRaw;
        String gc = txtGhiChuLieuNangCao.getText().trim();
        
        if (ten.isEmpty()) {
            showCustomNotification("THIẾU TÊN MẪU", "Vui lòng nhập tên cho mẫu thuốc phối liều trước khi lưu.", "WARNING");
            txtTenComboNangCao.requestFocus();
            return;
        }
        if (dsChiTietLieuLuuTru.isEmpty()) {
            showCustomNotification("TOA TRỐNG", "Vui lòng tìm và thêm ít nhất 1 loại thuốc vào toa trước khi lưu.", "WARNING");
            txtSearchLieuNangCao.requestFocus();
            return;
        }
        
        double gia = 0;
        Object giaTriOText = txtGiaBanComboNangCao.getValue();
        if (giaTriOText instanceof Number) {
            gia = ((Number) giaTriOText).doubleValue();
        } else if (giaTriOText != null) {
            try {
                gia = Double.parseDouble(giaTriOText.toString().replace(",", "").replace(".", "").trim());
            } catch (Exception ignored) {}
        }
        
        if (gia < 0) {
            showCustomNotification("GIÁ KHÔNG HỢP LỆ", "Giá bán combo không được nhỏ hơn 0!", "ERROR");
            txtGiaBanComboNangCao.requestFocus();
            return;
        }

        BUS_SanPham bus = new BUS_SanPham();
        boolean ok;
        String newId = null;
        if (comboIdDangSua == null) {
            // [FIX 3A] Kiểm tra trùng tên trước khi tạo mới
            if (bus.kiemTraTenComboTonTai(ten)) {
                showCustomNotification("TRÙNG TÊN MẪU",
                    "Đã tồn tại mẫu liều có tên \"" + ten + "\".\n"
                    + "Vui lòng đặt tên khác hoặc mở mẫu đó để chỉnh sửa.",
                    "WARNING");
                txtTenComboNangCao.requestFocus();
                return;
            }
            newId = bus.themMauMoiNangCao(ten, nb, gia, gc, dsChiTietLieuLuuTru);
            ok = (newId != null);
            if (ok) comboIdDangSua = newId;
        } else {
            ok = bus.capNhatMauNangCao(comboIdDangSua, ten, nb, gia, gc, dsChiTietLieuLuuTru);
        }
        
        if (ok) {
            showCustomNotification("LƯU THÀNH CÔNG",
                (comboIdDangSua == null ? "Đã tạo mẫu liều mới thành công!" : "Đã cập nhật mẫu liều thành công!"),
                "SUCCESS");
            loadDanhSachComboNangCaoRaGiaoDien(true);
            SwingUtilities.invokeLater(() -> {
                int lastRow = mdlDanhSachLieuNangCao.getRowCount() - 1;
                if (lastRow >= 0) {
                    tblDanhSachLieuNangCao.scrollRectToVisible(
                        tblDanhSachLieuNangCao.getCellRect(lastRow, 0, true));
                    tblDanhSachLieuNangCao.setRowSelectionInterval(lastRow, lastRow);
                }
            });
        }
    }

    private void xoaMauLieuHienTai() {
        int r = tblDanhSachLieuNangCao.getSelectedRow();
        if (r < 0) {
            showCustomNotification("CHƯA CHỌN MẪU", "Vui lòng chọn một mẫu liều trong danh sách để xóa.", "WARNING");
            return;
        }
        String id = mdlDanhSachLieuNangCao.getValueAt(r, 0).toString();
        String tenMau = mdlDanhSachLieuNangCao.getValueAt(r, 1).toString();
        if (showCustomConfirm("XÁC NHẬN XÓA",
                "Bạn chắc chắn muốn xóa mẫu liều:<br><b>" + tenMau + "</b>?<br>"
                + "<font color='red'>Thao tác này không thể hoàn tác!</font>")) {
            if (new BUS_SanPham().xoaMauNangCao(id)) {
                resetFormSoanThaoLieu();
                loadDanhSachComboNangCaoRaGiaoDien(true);
                showCustomNotification("XÓA THÀNH CÔNG", "Đã xóa mẫu liều \"" + tenMau + "\".", "SUCCESS");
            } else {
                showCustomNotification("LỖI XÓA", "Không thể xóa mẫu liều. Vui lòng thử lại.", "ERROR");
            }
        }
    }
    private boolean showCustomConfirm(String title, String message) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dlg.setUndecorated(true);
        JPanel pnl = new JPanel(new BorderLayout(0, 12));
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#F59E0B"), 2),
            new EmptyBorder(20, 24, 16, 24)));
        pnl.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.decode("#92400E"));
        pnl.add(lblTitle, BorderLayout.NORTH);

        JLabel lblMsg = new JLabel("<html><body style='width:300px'>" + message + "</body></html>");
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pnl.add(lblMsg, BorderLayout.CENTER);

        boolean[] result = {false};
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlBtn.setOpaque(false);
        JButton btnYes = new JButton("Xác nhận");
        btnYes.setBackground(Color.decode("#EF4444")); btnYes.setForeground(Color.WHITE);
        btnYes.setFont(new Font("Segoe UI", Font.BOLD, 13)); btnYes.setFocusPainted(false);
        btnYes.addActionListener(e -> { result[0] = true; dlg.dispose(); });
        JButton btnNo = new JButton("Hủy");
        btnNo.setFont(new Font("Segoe UI", Font.PLAIN, 13)); btnNo.setFocusPainted(false);
        btnNo.addActionListener(e -> dlg.dispose());
        pnlBtn.add(btnNo); pnlBtn.add(btnYes);
        pnl.add(pnlBtn, BorderLayout.SOUTH);

        dlg.setContentPane(pnl);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        return result[0];
    }
    private String showNhomBenhLyInputDialog(JComboBox<String> existingCombo) {
        final String[] result = {null};
 
        JDialog dialog = new JDialog(
            (java.awt.Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setUndecorated(true);
 
        // ── Wrapper chính ──────────────────────────────────────────────────────
        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createLineBorder(Color.decode("#1E3A8A"), 2));
        pnlMain.setBackground(Color.WHITE);
 
        // ── Header ─────────────────────────────────────────────────────────────
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.decode("#1E3A8A"));
        pnlHeader.setPreferredSize(new Dimension(0, 48));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
 
        // Icon dấu + bên trái header
        JLabel lblIconH = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#93C5FD")); // xanh nhạt
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.drawLine(cx, cy - 8, cx, cy + 8); // dọc
                g2.drawLine(cx - 8, cy, cx + 8, cy); // ngang
                g2.dispose();
            }
        };
        lblIconH.setPreferredSize(new Dimension(28, 48));
 
        JLabel lblTitle = new JLabel("THÊM NHÓM BỆNH LÝ MỚI", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);
 
        pnlHeader.add(lblIconH, BorderLayout.WEST);
        pnlHeader.add(lblTitle, BorderLayout.CENTER);
 
        // ── Body ───────────────────────────────────────────────────────────────
        JPanel pnlBody = new JPanel(new BorderLayout(0, 12));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
 
        // Mô tả nhỏ
        JLabel lblDesc = new JLabel(
            "<html><span style='color:#64748B;font-size:12px'>"
            + "Nhóm mới sẽ được thêm vào danh sách và chọn ngay lập tức.</span></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlBody.add(lblDesc, BorderLayout.NORTH);
 
        // Panel nhập liệu
        JPanel pnlInput = new JPanel(new BorderLayout(8, 0));
        pnlInput.setBackground(Color.WHITE);
        pnlInput.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
 
        JLabel lblFieldIcon = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Vẽ icon "tag" đơn giản
                g2.setColor(Color.decode("#3B82F6"));
                int[] xp = {2, 14, 14, 8, 2};
                int[] yp = {4, 4, 14, 19, 14};
                g2.fillPolygon(xp, yp, 5);
                g2.setColor(Color.WHITE);
                g2.fillOval(9, 7, 4, 4);
                g2.dispose();
            }
        };
        lblFieldIcon.setPreferredSize(new Dimension(24, 36));
        lblFieldIcon.setOpaque(false);
 
        JTextField txtNhomMoi = new JTextField();
        txtNhomMoi.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNhomMoi.setPreferredSize(new Dimension(0, 36));
        txtNhomMoi.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#CBD5E1"), 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        txtNhomMoi.putClientProperty("JTextField.placeholderText", "Ví dụ: Xương khớp – Gút");
 
        // Label cảnh báo trùng (ẩn mặc định)
        JLabel lblWarning = new JLabel(" ");
        lblWarning.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblWarning.setForeground(Color.decode("#EF4444"));
        lblWarning.setBorder(BorderFactory.createEmptyBorder(2, 2, 0, 0));
 
        pnlInput.add(lblFieldIcon, BorderLayout.WEST);
        pnlInput.add(txtNhomMoi, BorderLayout.CENTER);
 
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 4));
        pnlCenter.setBackground(Color.WHITE);
 
        // Label nhãn field
        JLabel lblFieldLabel = new JLabel("Tên nhóm bệnh lý");
        lblFieldLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFieldLabel.setForeground(Color.decode("#1E3A8A"));
 
        pnlCenter.add(lblFieldLabel, BorderLayout.NORTH);
        pnlCenter.add(pnlInput, BorderLayout.CENTER);
        pnlCenter.add(lblWarning, BorderLayout.SOUTH);
 
        pnlBody.add(pnlCenter, BorderLayout.CENTER);
 
        // ── Gợi ý danh sách hiện có (nhỏ gọn) ─────────────────────────────────
        JPanel pnlHint = new JPanel(new BorderLayout());
        pnlHint.setBackground(Color.decode("#F8FAFC"));
        pnlHint.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E2E8F0"), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
 
        JLabel lblHintTitle = new JLabel("Nhóm hiện có:");
        lblHintTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHintTitle.setForeground(Color.decode("#64748B"));
        pnlHint.add(lblHintTitle, BorderLayout.NORTH);
 
        // Gom tên các nhóm đã có (bỏ qua "--Chọn--" và "+ Thêm")
        StringBuilder sb = new StringBuilder("<html><span style='color:#94A3B8;font-size:11px'>");
        int count = 0;
        for (int i = 0; i < existingCombo.getItemCount(); i++) {
            String it = existingCombo.getItemAt(i);
            if (it.startsWith("--") || it.startsWith("+")) continue;
            if (count > 0) sb.append("  ·  ");
            sb.append(it);
            count++;
        }
        sb.append("</span></html>");
        JLabel lblHintList = new JLabel(sb.toString());
        lblHintList.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pnlHint.add(lblHintList, BorderLayout.CENTER);
 
        pnlBody.add(pnlHint, BorderLayout.SOUTH);
 
        // ── Footer buttons ──────────────────────────────────────────────────────
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#E2E8F0")));
 
        JButton btnHuy = new JButton("Hủy");
        btnHuy.setPreferredSize(new Dimension(90, 36));
        btnHuy.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnHuy.setBackground(Color.decode("#64748B"));
        btnHuy.setForeground(Color.WHITE);
        btnHuy.setFocusPainted(false);
        btnHuy.setBorderPainted(false);
        btnHuy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHuy.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnHuy.setBackground(Color.decode("#475569")); }
            @Override public void mouseExited(MouseEvent e)  { btnHuy.setBackground(Color.decode("#64748B")); }
        });
        btnHuy.addActionListener(e -> dialog.dispose());
 
        JButton btnThem = new JButton("✚  Thêm nhóm");
        btnThem.setPreferredSize(new Dimension(130, 36));
        btnThem.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnThem.setBackground(Color.decode("#1E3A8A"));
        btnThem.setForeground(Color.WHITE);
        btnThem.setFocusPainted(false);
        btnThem.setBorderPainted(false);
        btnThem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnThem.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnThem.setBackground(Color.decode("#1e40af")); }
            @Override public void mouseExited(MouseEvent e)  { btnThem.setBackground(Color.decode("#1E3A8A")); }
        });
 
        // Đổi màu border khi focus txtNhomMoi
        txtNhomMoi.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                txtNhomMoi.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#3B82F6"), 2, true),
                    BorderFactory.createEmptyBorder(3, 9, 3, 9)));
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                txtNhomMoi.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#CBD5E1"), 1, true),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)));
            }
        });
 
        // Logic xác nhận thêm
        Runnable doAdd = () -> {
            String ten = txtNhomMoi.getText().trim();
            if (ten.isEmpty()) {
                lblWarning.setText("⚠  Vui lòng nhập tên nhóm bệnh lý!");
                txtNhomMoi.requestFocus();
                return;
            }
            // Kiểm tra trùng
            for (int i = 0; i < existingCombo.getItemCount(); i++) {
                if (existingCombo.getItemAt(i).equalsIgnoreCase(ten)) {
                    lblWarning.setText("⚠  Nhóm \"" + ten + "\" đã tồn tại trong danh sách!");
                    txtNhomMoi.selectAll();
                    txtNhomMoi.requestFocus();
                    return;
                }
            }
            result[0] = ten;
            dialog.dispose();
        };
 
        btnThem.addActionListener(e -> doAdd.run());
        // Nhấn Enter trong ô text cũng xác nhận
        txtNhomMoi.addActionListener(e -> doAdd.run());
        // Xóa cảnh báo khi gõ lại
        txtNhomMoi.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { lblWarning.setText(" "); }
            @Override public void removeUpdate(DocumentEvent e)  { lblWarning.setText(" "); }
            @Override public void changedUpdate(DocumentEvent e) { lblWarning.setText(" "); }
        });
 
        pnlFooter.add(btnHuy);
        pnlFooter.add(btnThem);
 
        // ── Lắp ráp ────────────────────────────────────────────────────────────
        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlBody,   BorderLayout.CENTER);
        pnlMain.add(pnlFooter, BorderLayout.SOUTH);
 
        dialog.add(pnlMain);
        dialog.setPreferredSize(new Dimension(480, 320));
        dialog.pack();
        dialog.setLocationRelativeTo(this);
 
        // Focus vào ô nhập khi dialog mở
        SwingUtilities.invokeLater(txtNhomMoi::requestFocusInWindow);
        dialog.setVisible(true);
 
        return result[0];
    }
}