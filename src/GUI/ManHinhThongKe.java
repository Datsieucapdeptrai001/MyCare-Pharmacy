package GUI;

import Utils.UserSession;
import Utils.MenuIcon;
import Utils.ModernDatePicker;
import Utils.ModernScrollBarUI;
import BUS.BUS_ThongKe;
import BUS.BUS_CaLamViec;
import Entity.CaLamViec;
import java.io.*;
import java.util.Calendar;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.print.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

public class ManHinhThongKe extends JPanel {

    // BUS LAYER
    private final BUS_ThongKe busThongKe = new BUS_ThongKe();
    private final BUS_CaLamViec busCaLamViec = new BUS_CaLamViec();

    // DỮ LIỆU LOAD TỪ DB (Dùng chung)
    static String[] NV_NAMES = {};
    static String[] NV_IDS = {};
    static String[] NV_ROLES = {};
    static String[] NV_SHORT = {};
    static Color[] NV_COLORS = {};
    static int[] NV_HD_S = {};
    static int[] NV_HD_C = {};
    static int[] NV_HD_T = {};
    static double[] NV_DT_S = {};
    static double[] NV_DT_C = {};
    static double[] NV_DT_T = {};
    static int[] NV_HD_TRA = {};
    static double[] NV_DT_TRA = {};
    static String[] DATES_10 = {};
    static int[][] NV_DAILY = {};

    static double[] DT_DATA = new double[12];
    static double[] CP_DATA = new double[12];
    static double[] LN_DATA = new double[12];
    // Nhãn trục X biểu đồ – thay đổi theo kỳ lọc
    static String[] CHART_LABELS = new String[]{"T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"};

    static double[] DAILY_30_DT = new double[30];
    static int[] DAILY_30_HD = new int[30];
    static String[] DAILY_30_DATES = new String[30];
    static final String[] THANG = { "T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12" };
    static int[] DONUT_VALS = { 1, 1, 1, 1 };
    static final String[] DONUT_LABELS = { "Thuốc KĐ","Thuốc KKĐ","TPCN","Mỹ phẩm" };
    static final Color[] DONUT_COLORS = {
            Color.decode("#FF5630"), Color.decode("#1A73E8"),
            Color.decode("#00A76F"), Color.decode("#FFAB00")
    };
    static final Color[] PALETTE = {
            Color.decode("#1A73E8"), Color.decode("#00A76F"),
            Color.decode("#FFAB00"), Color.decode("#9C27B0"),
            Color.decode("#FF5630"), Color.decode("#00BCD4"),
            Color.decode("#E91E63"), Color.decode("#FF7043"),
            Color.decode("#607D8B")
    };

    // ───── FIELDS CHUNG ─────
    private CardLayout cardBody;
    private JPanel pnlBody;
    // Tab buttons – NGAY bỏ, thêm DONHANG
    private JButton btnDT, btnDonHang, btnNV, btnKH, btnKho;
    private JComboBox<String> cboKyLoc, cboNhanVien;
    private JButton btnNamPicker;
    private JLabel lblYearBadge;
    private int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    // modeLocThoiGian: HOM_NAY | TUAN | THANG | QUY | NAM | TUYCHINH
    private String modeLocThoiGian = "THANG";

    // KPI label refs (Tab DT)
    private JLabel kpiDTVal, kpiDTSub;
    private JLabel kpiHDVal, kpiHDSub;
    private JLabel kpiLNVal, kpiLNSub;
    private JLabel kpiTBVal, kpiTBSub;

    // Chart & UI references
    private BarChartMain chartBarMain;
    private LineChartDaily chartLineDaily;
    private DonutChart chartDonut;
    private NVDailyChart chartNVDaily;
    private NVShiftChart chartNVShift;
    private NVDetailTable tblNVDetail;
    private DefaultTableModel modelTopSP, modelVAT;
    private JLabel lblVATTotal;
    private JLabel miniPeakDate, miniAvgDT, miniAvgOrder;
    private JLabel lblChartSub; // subtitle của biểu đồ DT-CP-LN (cập nhật theo filter)

    // ── KPI Tài Chính (Tab DT – 3 chỉ số chính) ──
    private JLabel kpiGVVal,  kpiGVSub;   // Giá vốn hàng bán (COGS)
    private JLabel kpiLNGVal, kpiLNGSub;  // Lợi nhuận gộp
    // Bảng báo cáo tài chính chi tiết (thay thế bảng VAT cũ)
    private DefaultTableModel modelTaiChinh;

    private int selectedNVIdx = -1;

    private JTextField txtTuNgay, txtDenNgay;
    private JComboBox<String> cboThang, cboQuy;

    private JLabel lblTongNV, lblDTTB_NV, lblTongDT_NV;
    private JLabel lblHDSang, lblHDChieu, lblHDToi;
    private JPanel pnlFilterDuocSi;
    private JPanel pnlFilter;

    // Tab NHÂN VIÊN – shift filter + đang trực
    private JComboBox<String> cboCaFilter;        // Tất cả / Sáng / Chiều / Tối
    private JPanel pnlDangTruc;                   // panel hiện NV đang trực ca
    private DefaultTableModel modelDangTruc;

    // Tab ĐƠN HÀNG (mới – thay Theo ngày)
    private JLabel lblDonKpiTong, lblDonKpiTB, lblDonKpiGioC, lblDonKpiSP;
    private JLabel lblDonKpiTongSub, lblDonKpiTBSub, lblDonKpiGioCsub, lblDonKpiSPSub;
    private int donBanCount = 0, donTraCount = 0; // breakdown cho popup tổng đơn
    private HourBarChart chartDonHour;
    private DefaultTableModel modelDonHang;
    private JLabel lblDonPeriod;

    // Tab KHÁCH HÀNG
    private DefaultTableModel modelTopKH;
    private JLabel lblKHTotal, lblKHCoTK, lblKHTongDiem;
    private BarChartKHMoi chartKHMoi;

    // Tab KHO HÀNG
    private JLabel lblKhoGiaTriTon, lblKhoSoLo, lblKhoHetHan;
    private DefaultTableModel modelTonKho;
    private BarChartNhapHang chartNhapHang;
    private BarChartTonKho chartTonKhoKho;

    // CONSTRUCTOR
    public ManHinhThongKe() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.decode("#F4F6F8"));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        if (!UserSession.getInstance().isAdmin()) {
            showAccessDenied();
            return;
        }

        add(buildHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);

        JPanel topInfo = new JPanel(new BorderLayout(0, 8));
        topInfo.setOpaque(false);
        topInfo.add(buildFilterBar(), BorderLayout.NORTH);
        center.add(topInfo, BorderLayout.NORTH);

        cardBody = new CardLayout();
        pnlBody = new JPanel(cardBody);
        pnlBody.setOpaque(false);

        pnlBody.add(buildViewDT(),      "DT");
        pnlBody.add(buildViewDonHang(), "DONHANG");
        pnlBody.add(buildViewNV(),      "NV");
        pnlBody.add(buildViewKH(),      "KH");
        pnlBody.add(buildViewKho(),     "KHO");

        center.add(pnlBody, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        setupEvents();
        loadDataFromDB(currentYear);
        loadKhachHang(currentYear, buildCondHD());
        loadKho(currentYear);
        loadDonHangTab();
        loadNVDangTruc();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API – dùng cho MainDashboard gọi vào để refresh toàn bộ dữ liệu
    // ─────────────────────────────────────────────────────────────────────────
    public void refreshAll() {
        loadDataFromDB(currentYear);
        loadDonHangTab();
        loadNVDangTruc();
    }

    // Tự load lại khi panel được hiển thị lần đầu hoặc add lại vào container
    @Override
    public void addNotify() {
        super.addNotify();
        // Chỉ refresh nếu data chưa được load (tránh double-load khi init)
        SwingUtilities.invokeLater(() -> refreshAll());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HEADER
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        lblYearBadge = new JLabel("Cả năm " + currentYear);
        lblYearBadge.setForeground(Color.decode("#1A73E8"));
        lblYearBadge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblYearBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#1A73E8"), 1, true),
                new EmptyBorder(2, 10, 2, 10)));

        JLabel title = new JLabel("  THỐNG KÊ & BÁO CÁO  ");
        title.setIcon(MenuIcon.IC_CHART);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.decode("#152A4B"));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        left.add(title);
        left.add(lblYearBadge);

        // ── 3 action buttons: Excel / PDF / In ────────────────────────────────
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlActions.setOpaque(false);

        JButton btnXuatExcel = makeActionBtn("Excel (.xlsx)", makeSimpleIcon("EXCEL", 16, Color.WHITE),
                Color.decode("#217346"));
        btnXuatExcel.addActionListener(e -> xuatExcel());

        JButton btnXuatPDF = makeActionBtn("Xuất PDF", makeSimpleIcon("PDF", 16, Color.WHITE),
                Color.decode("#C0392B"));
        btnXuatPDF.addActionListener(e -> xuatPDF());

        JButton btnIn = makeActionBtn("In báo cáo", makeSimpleIcon("PRINT", 16, Color.WHITE),
                Color.decode("#152A4B"));
        btnIn.addActionListener(e -> inBaoCao());

        pnlActions.add(btnXuatExcel);
        pnlActions.add(btnXuatPDF);
        pnlActions.add(btnIn);

        p.add(left, BorderLayout.WEST);
        p.add(pnlActions, BorderLayout.EAST);
        return p;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FILTER BAR (6 kỳ lọc + NV)
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildFilterBar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);

        // --- Tab buttons ---
        JPanel pnlTab = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        pnlTab.setBackground(Color.WHITE);

        btnDT      = makeTabBtn(MenuIcon.of("TAB_CHART", 16, Color.decode("#1A73E8")), "Doanh thu", true);
        btnDonHang = makeTabBtn(MenuIcon.of("CART", 16, Color.decode("#444444")), "Đơn hàng", false);
        btnNV      = makeTabBtn(makeSimpleIcon("PEOPLE", 16, Color.decode("#444444")), "Nhân viên", false);
        btnKH      = makeTabBtn(makeSimpleIcon("HEART",  16, Color.decode("#444444")), "Khách hàng", false);
        btnKho     = makeTabBtn(makeSimpleIcon("STORE",  16, Color.decode("#444444")), "Kho hàng", false);

        pnlTab.add(btnDT);
        pnlTab.add(btnDonHang);
        pnlTab.add(btnNV);
        pnlTab.add(btnKH);
        pnlTab.add(btnKho);

        // --- Kỳ lọc 6 nút ---
        pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        pnlFilter.setBackground(Color.WHITE);
        pnlFilter.setBorder(new EmptyBorder(0, 0, 6, 0));

        Color activeBg = Color.decode("#1A73E8"), activeFg = Color.WHITE;
        Color inactiveBg = Color.decode("#F8F9FA"), inactiveFg = Color.decode("#333333");

        JButton[] periodBtns = new JButton[6];
        String[] periodLabels = { "Hôm nay","Tuần này","Tháng","Quý","Cả năm","Tùy chỉnh" };
        for (int i = 0; i < 6; i++) {
            periodBtns[i] = new JButton(periodLabels[i]);
            periodBtns[i].setFocusPainted(false);
            periodBtns[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            periodBtns[i].setFont(new Font("Segoe UI", Font.PLAIN, 11));
            periodBtns[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                    new EmptyBorder(4, 10, 4, 10)));
            periodBtns[i].setBackground(i == 2 ? activeBg : inactiveBg);
            periodBtns[i].setForeground(i == 2 ? activeFg : inactiveFg);
        }

        // Month combo (dùng cho mode THANG)
        cboThang = new JComboBox<>(new String[]{"T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"});
        cboQuy   = new JComboBox<>(new String[]{"Quý 1 (T1-T3)","Quý 2 (T4-T6)","Quý 3 (T7-T9)","Quý 4 (T10-T12)"});
        styleCombo(cboThang);
        styleCombo(cboQuy);

        JPanel pnlCustomDate = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlCustomDate.setOpaque(false);
        txtTuNgay = new JTextField("dd/mm/yyyy", 8);
        txtTuNgay.setEditable(false);
        txtTuNgay.setBackground(Color.WHITE);
        txtDenNgay = new JTextField("dd/mm/yyyy", 8);
        txtDenNgay.setEditable(false);
        txtDenNgay.setBackground(Color.WHITE);
        MouseAdapter openCal = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JTextField txt = (JTextField) e.getSource();
                Window win = SwingUtilities.getWindowAncestor(ManHinhThongKe.this);
                new ModernDatePicker(win, txt).setVisible(true);
                onFilterChanged();
            }
        };
        txtTuNgay.addMouseListener(openCal);
        txtDenNgay.addMouseListener(openCal);
        pnlCustomDate.add(new JLabel("Từ: "));
        pnlCustomDate.add(txtTuNgay);
        pnlCustomDate.add(new JLabel(" → "));
        pnlCustomDate.add(txtDenNgay);

        // CardLayout cho phần bên phải của bộ lọc
        JPanel pnlTimeOptions = new JPanel(new CardLayout());
        pnlTimeOptions.setOpaque(false);
        pnlTimeOptions.add(new JPanel(), "NONE");         // Hôm nay / Tuần / Năm
        pnlTimeOptions.add(cboThang,      "THANG");
        pnlTimeOptions.add(cboQuy,        "QUY");
        pnlTimeOptions.add(pnlCustomDate, "TUYCHINH");
        CardLayout timeCL = (CardLayout) pnlTimeOptions.getLayout();

        // Hành vi từng nút kỳ lọc
        Runnable[] activators = {
            () -> { modeLocThoiGian = "HOM_NAY"; timeCL.show(pnlTimeOptions, "NONE"); },
            () -> { modeLocThoiGian = "TUAN";    timeCL.show(pnlTimeOptions, "NONE"); },
            () -> { modeLocThoiGian = "THANG";   timeCL.show(pnlTimeOptions, "THANG"); },
            () -> { modeLocThoiGian = "QUY";     timeCL.show(pnlTimeOptions, "QUY"); },
            () -> { modeLocThoiGian = "NAM";     timeCL.show(pnlTimeOptions, "NONE"); },
            () -> { modeLocThoiGian = "TUYCHINH"; timeCL.show(pnlTimeOptions, "TUYCHINH"); }
        };
        for (int i = 0; i < 6; i++) {
            final int idx = i;
            periodBtns[i].addActionListener(e -> {
                activators[idx].run();
                for (JButton b : periodBtns) { b.setBackground(inactiveBg); b.setForeground(inactiveFg); }
                periodBtns[idx].setBackground(activeBg);
                periodBtns[idx].setForeground(activeFg);
                if (!"TUYCHINH".equals(modeLocThoiGian)) onFilterChanged();
            });
        }
        // Mặc định chọn "Tháng" (index=2)
        timeCL.show(pnlTimeOptions, "THANG");

        cboThang.addActionListener(e -> onFilterChanged());
        cboQuy.addActionListener(e -> onFilterChanged());
        cboKyLoc = cboThang;

        btnNamPicker = new JButton(" " + currentYear + " ▼");
        btnNamPicker.setIcon(MenuIcon.IC_CALENDAR);
        btnNamPicker.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnNamPicker.setFocusPainted(false);
        btnNamPicker.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(4, 10, 4, 10)));
        btnNamPicker.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnNamPicker.addActionListener(e -> showYearCalendarPopup(btnNamPicker));

        cboNhanVien = new JComboBox<>(new String[]{"Tất cả"});
        styleCombo(cboNhanVien);
        cboNhanVien.addActionListener(e -> applyNVFilter());

        pnlFilterDuocSi = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlFilterDuocSi.setOpaque(false);
        pnlFilterDuocSi.add(new JLabel("| Nhân viên:"));
        pnlFilterDuocSi.add(cboNhanVien);
        pnlFilterDuocSi.setVisible(false);

        // Thêm vào filter bar
        for (JButton b : periodBtns) pnlFilter.add(b);
        pnlFilter.add(pnlTimeOptions);
        pnlFilter.add(btnNamPicker);
        pnlFilter.add(pnlFilterDuocSi);

        p.add(pnlTab);
        p.add(pnlFilter);
        return p;
    }

    private void showYearCalendarPopup(JButton source) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(Color.WHITE);
        popup.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(0, 0, 0, 0)));

        JLabel header = new JLabel("  Chọn năm thống kê", MenuIcon.of("CALENDAR", 14, Color.decode("#152A4B")), SwingConstants.LEFT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setBackground(Color.decode("#1A73E8"));
        header.setBorder(new EmptyBorder(10, 12, 10, 12));

        JPanel grid = new JPanel(new GridLayout(0, 3, 6, 6));
        grid.setBackground(Color.WHITE);
        grid.setBorder(new EmptyBorder(12, 12, 12, 12));
        int now = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = now - 5; y <= now + 1; y++) {
            final int yr = y;
            JButton btn = new JButton(String.valueOf(y));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setFont(new Font("Segoe UI", yr == currentYear ? Font.BOLD : Font.PLAIN, 12));
            if (yr == currentYear) { btn.setBackground(Color.decode("#1A73E8")); btn.setForeground(Color.WHITE); }
            else if (yr == now)    { btn.setBackground(Color.decode("#EEF2FF")); btn.setForeground(Color.decode("#1A73E8")); }
            else                   { btn.setBackground(Color.decode("#F8F9FA")); btn.setForeground(Color.decode("#333333")); }
            btn.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true));
            btn.addActionListener(e -> {
                currentYear = yr;
                btnNamPicker.setText(" " + currentYear + " ▼");
                popup.setVisible(false);
                onFilterChanged();
            });
            grid.add(btn);
        }

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.add(header, BorderLayout.NORTH);
        content.add(grid, BorderLayout.CENTER);
        popup.add(content);
        popup.setPreferredSize(new Dimension(240, 200));
        popup.show(source, 0, source.getHeight() + 4);
    }

    private void styleCombo(JComboBox<?> cbo) {
        cbo.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true));
        cbo.setBackground(Color.WHITE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EVENTS
    // ─────────────────────────────────────────────────────────────────────────
    private void setupEvents() {
        btnDT.addActionListener(e -> {
            switchTab("DT"); cardBody.show(pnlBody, "DT");
            pnlFilterDuocSi.setVisible(false); pnlFilter.setVisible(true);
        });
        btnDonHang.addActionListener(e -> {
            switchTab("DONHANG"); cardBody.show(pnlBody, "DONHANG");
            pnlFilterDuocSi.setVisible(false); pnlFilter.setVisible(true);
            loadDonHangTab();
        });
        btnNV.addActionListener(e -> {
            switchTab("NV"); cardBody.show(pnlBody, "NV");
            pnlFilterDuocSi.setVisible(true); pnlFilter.setVisible(true);
            loadNVDangTruc();
        });
        btnKH.addActionListener(e -> {
            switchTab("KH"); cardBody.show(pnlBody, "KH");
            pnlFilterDuocSi.setVisible(false); pnlFilter.setVisible(false);
            loadKhachHang(currentYear, buildCondHD());
        });
        btnKho.addActionListener(e -> {
            switchTab("KHO"); cardBody.show(pnlBody, "KHO");
            pnlFilterDuocSi.setVisible(false); pnlFilter.setVisible(false);
            loadKho(currentYear);
        });
    }

    private void switchTab(String id) {
        // Background & Text color
        btnDT.setBackground("DT".equals(id)         ? Color.decode("#E8F0FE") : Color.WHITE);
        btnDT.setForeground("DT".equals(id)         ? Color.decode("#1A73E8") : Color.decode("#444444"));
        btnDonHang.setBackground("DONHANG".equals(id) ? Color.decode("#E8F5E9") : Color.WHITE);
        btnDonHang.setForeground("DONHANG".equals(id) ? Color.decode("#00A76F") : Color.decode("#444444"));
        btnNV.setBackground("NV".equals(id)         ? Color.decode("#F3E5F5") : Color.WHITE);
        btnNV.setForeground("NV".equals(id)         ? Color.decode("#9C27B0") : Color.decode("#444444"));
        btnKH.setBackground("KH".equals(id)         ? Color.decode("#FFF3E0") : Color.WHITE);
        btnKH.setForeground("KH".equals(id)         ? Color.decode("#FF9800") : Color.decode("#444444"));
        btnKho.setBackground("KHO".equals(id)       ? Color.decode("#FFEBEE") : Color.WHITE);
        btnKho.setForeground("KHO".equals(id)       ? Color.decode("#FF5630") : Color.decode("#444444"));

        // MỚI: Update Icon Color
        btnDT.setIcon(MenuIcon.of("TAB_CHART", 16, "DT".equals(id) ? Color.decode("#1A73E8") : Color.decode("#444444")));
        btnDonHang.setIcon(MenuIcon.of("CART", 16, "DONHANG".equals(id) ? Color.decode("#00A76F") : Color.decode("#444444")));
        btnNV.setIcon(makeSimpleIcon("PEOPLE", 16, "NV".equals(id)  ? Color.decode("#9C27B0") : Color.decode("#444444")));
        btnKH.setIcon(makeSimpleIcon("HEART",  16, "KH".equals(id)  ? Color.decode("#FF9800") : Color.decode("#444444")));
        btnKho.setIcon(makeSimpleIcon("STORE",  16, "KHO".equals(id) ? Color.decode("#FF5630") : Color.decode("#444444")));
    }
    private void onFilterChanged() {
        lblYearBadge.setText(getKyString() + " " + currentYear);
        loadDataFromDB(currentYear);
        loadKhachHang(currentYear, buildCondHD());
        loadDonHangTab();
    }

    private void applyNVFilter() {
        int idx = cboNhanVien.getSelectedIndex() - 1;
        selectedNVIdx = idx;
        if (chartNVDaily != null) chartNVDaily.setFilter(idx);
        if (chartNVShift != null) chartNVShift.setFilter(idx);
        if (tblNVDetail  != null) tblNVDetail.refreshData();
        repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BUILD CONDITION
    // ─────────────────────────────────────────────────────────────────────────
    private BUS.BUS_ThongKe.ThongKeFilter buildCond() {
        BUS.BUS_ThongKe.ThongKeFilter filter = new BUS.BUS_ThongKe.ThongKeFilter();

        // -----------------------------------------------------------------
        // ĐOẠN FIX: Lấy thẳng biến currentYear cậu đã lưu sẵn
        // -----------------------------------------------------------------
        filter.year = currentYear;
        // -----------------------------------------------------------------

        // NV filter (index 0 = "Tất cả")
        int nvIdx = cboNhanVien.getSelectedIndex() - 1;
        if (nvIdx >= 0 && nvIdx < NV_IDS.length) filter.maNV = NV_IDS[nvIdx];

        LocalDate today = LocalDate.now();
        switch (modeLocThoiGian) {
            case "HOM_NAY":
                filter.modeLocThoiGian = "TUYCHINH";
                filter.fromDate = java.sql.Date.valueOf(today);
                filter.toDate   = java.sql.Date.valueOf(today);
                break;
            case "TUAN":
                LocalDate weekStart = today.with(DayOfWeek.MONDAY);
                filter.modeLocThoiGian = "TUYCHINH";
                filter.fromDate = java.sql.Date.valueOf(weekStart);
                filter.toDate   = java.sql.Date.valueOf(today);
                break;
            case "THANG":
                filter.modeLocThoiGian = "THANG";
                String val = (String) cboThang.getSelectedItem();
                if (val != null && !val.equals("Cả năm"))
                    filter.month = Integer.parseInt(val.replace("T", ""));
                break;
            case "QUY":
                filter.modeLocThoiGian = "QUY";
                filter.quarter = cboQuy.getSelectedIndex() + 1;
                break;
            case "NAM":
                filter.modeLocThoiGian = "THANG"; // month=null → cả năm
                break;
            case "TUYCHINH":
                filter.modeLocThoiGian = "TUYCHINH";
                String tu = txtTuNgay.getText(), den = txtDenNgay.getText();
                if (!tu.contains("d") && !den.contains("d")) {
                    try {
                        String from = tu.split("/")[2]+"-"+tu.split("/")[1]+"-"+tu.split("/")[0];
                        String to   = den.split("/")[2]+"-"+den.split("/")[1]+"-"+den.split("/")[0];
                        filter.fromDate = java.sql.Date.valueOf(from);
                        filter.toDate   = java.sql.Date.valueOf(to);
                    } catch (Exception ignored) {}
                }
                break;
            default:
                filter.modeLocThoiGian = "THANG";
        }
        return filter;
    }

    private BUS.BUS_ThongKe.ThongKeFilter buildCondHD() { return buildCond(); }

    private String buildCondString(String dateColumn) {
        switch (modeLocThoiGian) {
            case "HOM_NAY":
                return " AND CAST("+dateColumn+" AS DATE) = CAST(GETDATE() AS DATE)";
            case "TUAN":
                return " AND "+dateColumn+" >= DATEADD(DAY, 1-DATEPART(WEEKDAY,GETDATE()), CAST(GETDATE() AS DATE))";
            case "THANG": {
                String v = (String) cboThang.getSelectedItem();
                if (v != null && !v.equals("Cả năm"))
                    return " AND MONTH("+dateColumn+") = "+Integer.parseInt(v.replace("T",""));
                return "";
            }
            case "QUY": {
                int s = cboQuy.getSelectedIndex();
                if (s==0) return " AND MONTH("+dateColumn+") BETWEEN 1 AND 3";
                if (s==1) return " AND MONTH("+dateColumn+") BETWEEN 4 AND 6";
                if (s==2) return " AND MONTH("+dateColumn+") BETWEEN 7 AND 9";
                if (s==3) return " AND MONTH("+dateColumn+") BETWEEN 10 AND 12";
                return "";
            }
            case "NAM": return "";
            case "TUYCHINH": {
                String tu = txtTuNgay.getText(), den = txtDenNgay.getText();
                if (!tu.contains("d") && !den.contains("d")) {
                    try {
                        String f = tu.split("/")[2]+"-"+tu.split("/")[1]+"-"+tu.split("/")[0];
                        String t = den.split("/")[2]+"-"+den.split("/")[1]+"-"+den.split("/")[0];
                        return " AND CAST("+dateColumn+" AS DATE) BETWEEN '"+f+"' AND '"+t+"'";
                    } catch (Exception ignored) {}
                }
                return "";
            }
        }
        return "";
    }

    private String buildCondPN() { return buildCondString("ngayNhap"); }

    private void loadDataFromDB(int year) {
        final BUS.BUS_ThongKe.ThongKeFilter fCondHD = buildCondHD();
        new Thread(() -> {
            try {
                java.util.List<String[]> nvList = busThongKe.getDuocSiList();
                int n = nvList.size();
                String[] tmp_NV_NAMES  = new String[n]; String[] tmp_NV_IDS   = new String[n];
                String[] tmp_NV_ROLES  = new String[n]; String[] tmp_NV_SHORT  = new String[n];
                Color[]  tmp_NV_COLORS = new Color[n];
                int[]    tmp_NV_HD_S   = new int[n];    int[]    tmp_NV_HD_C   = new int[n];
                int[]    tmp_NV_HD_T   = new int[n];
                double[] tmp_NV_DT_S   = new double[n]; double[] tmp_NV_DT_C   = new double[n];
                double[] tmp_NV_DT_T   = new double[n];
                int[]    tmp_NV_HD_TRA = new int[n];
                double[] tmp_NV_DT_TRA = new double[n];
                
                for (int i = 0; i < n; i++) {
                    tmp_NV_IDS[i]   = nvList.get(i)[0];
                    tmp_NV_NAMES[i] = nvList.get(i)[1];
                    tmp_NV_ROLES[i] = nvList.get(i)[2] != null ? nvList.get(i)[2] : "Dược sĩ";
                    String[] parts  = tmp_NV_NAMES[i].trim().split("\\s+");
                    tmp_NV_SHORT[i] = parts[parts.length - 1];
                    tmp_NV_COLORS[i]= PALETTE[i % PALETTE.length];
                }

                // ──────────────────────────────────────────────────────────────
                // BIỂU ĐỒ DT-CP-LN: Load động theo kỳ lọc hiện tại
                // ──────────────────────────────────────────────────────────────
                BUS.BUS_ThongKe.ThongKeFilter filterBieuDo;
                String chartGroupBy;
                String chartSubtitleStr;

                if ("NAM".equals(modeLocThoiGian)) {
                    // Cả năm → 12 tháng
                    filterBieuDo = new BUS.BUS_ThongKe.ThongKeFilter();
                    filterBieuDo.maNV = fCondHD.maNV;
                    filterBieuDo.year = year;
                    filterBieuDo.modeLocThoiGian = "NAM";
                    chartGroupBy = "THANG";
                    chartSubtitleStr = "12 tháng · " + year;
                } else if ("QUY".equals(modeLocThoiGian)) {
                    // Theo quý → hiển thị từng tháng trong quý
                    filterBieuDo = fCondHD;
                    chartGroupBy = "THANG";
                    int qIdx = cboQuy.getSelectedIndex() + 1;
                    chartSubtitleStr = "Theo tháng · Q" + qIdx + " · " + year;
                } else if ("THANG".equals(modeLocThoiGian)) {
                    // Theo tháng → hiển thị từng ngày trong tháng đó
                    filterBieuDo = fCondHD;
                    chartGroupBy = "NGAY";
                    String thangSel = (String) cboThang.getSelectedItem();
                    chartSubtitleStr = "Theo ngày · " + thangSel + "/" + year;
                } else if ("TUAN".equals(modeLocThoiGian)) {
                    filterBieuDo = fCondHD;
                    chartGroupBy = "NGAY";
                    chartSubtitleStr = "Theo ngày · Tuần này · " + year;
                } else if ("HOM_NAY".equals(modeLocThoiGian)) {
                    filterBieuDo = fCondHD;
                    chartGroupBy = "NGAY";
                    chartSubtitleStr = "Hôm nay · " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } else {
                    // TUYCHINH
                    filterBieuDo = fCondHD;
                    chartGroupBy = "NGAY";
                    chartSubtitleStr = "Tùy chỉnh · " + year;
                }

                java.util.List<Object[]> dataBieuDo = busThongKe.getBaoCaoTaiChinh(filterBieuDo, chartGroupBy);
                int chartN = Math.max(1, dataBieuDo.size());
                double[] tmp_DT_DATA = new double[chartN];
                double[] tmp_CP_DATA = new double[chartN];
                double[] tmp_LN_DATA = new double[chartN];
                String[] tmp_CHART_LABELS = new String[chartN];

                for (int ci = 0; ci < dataBieuDo.size(); ci++) {
                    Object[] r = dataBieuDo.get(ci);
                    tmp_DT_DATA[ci] = (double) r[4] / 1_000_000.0;
                    tmp_CP_DATA[ci] = (double) r[5] / 1_000_000.0;
                    tmp_LN_DATA[ci] = (double) r[6] / 1_000_000.0;
                    // Build nhãn trục X từ thoiGian trả về
                    String tg = String.valueOf(r[0]);
                    if ("THANG".equals(chartGroupBy) && tg.contains("-")) {
                        // "YYYY-MM" → "T5"
                        tmp_CHART_LABELS[ci] = "T" + Integer.parseInt(tg.substring(tg.lastIndexOf("-") + 1));
                    } else if (tg.length() >= 5) {
                        // "DD/MM/YYYY" → "DD/MM"
                        tmp_CHART_LABELS[ci] = tg.substring(0, 5);
                    } else {
                        tmp_CHART_LABELS[ci] = tg;
                    }
                }
                final String fChartSubtitle = chartSubtitleStr;

                int[] tmp_DONUT = busThongKe.getSoLuongTheoLoaiSP(year, fCondHD);

                java.util.List<String> dateList = busThongKe.get10NgayGanNhat(year, fCondHD);
                String[] tmp_DATES_10 = dateList.toArray(new String[0]);
                int days = tmp_DATES_10.length;
                int[][] tmp_NV_DAILY = new int[n][days];
                for (int i = 0; i < n; i++)
                    for (int j = 0; j < days; j++)
                        tmp_NV_DAILY[i][j] = busThongKe.getDailyHDCuaNV(tmp_NV_IDS[i], tmp_DATES_10[j], fCondHD);

                double[] tmp_DAILY_30_DT = new double[30];
                int[]    tmp_DAILY_30_HD = new int[30];
                String[] tmp_DAILY_30_DATES = new String[30];
                LocalDate today = LocalDate.now();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (int i = 0; i < 30; i++)
                    tmp_DAILY_30_DATES[29-i] = today.minusDays(i).format(dtf);
                
                BUS.BUS_ThongKe.ThongKeFilter filterDaily30 = new BUS.BUS_ThongKe.ThongKeFilter();
                filterDaily30.maNV = fCondHD.maNV; 
                filterDaily30.fromDate = java.sql.Date.valueOf(today.minusDays(29));
                filterDaily30.toDate   = java.sql.Date.valueOf(today);
                filterDaily30.modeLocThoiGian = "TUYCHINH";

                java.util.List<Object[]> daily30BCTC = busThongKe.getBaoCaoTaiChinh(filterDaily30, "NGAY");
                java.util.List<Object[]> daily30HD = busThongKe.getThongKe30NgayGanNhat(filterDaily30);

                for (Object[] row : daily30HD) {
                    String d = (String) row[0]; int hd = (int) row[1];
                    for (int i = 0; i < 30; i++) {
                        if (tmp_DAILY_30_DATES[i].equals(d)) {
                            tmp_DAILY_30_HD[i] = hd; break;
                        }
                    }
                }
                for (Object[] row : daily30BCTC) {
                    String d = (String) row[0]; 
                    double dtThuan = (double) row[4] / 1_000_000.0; 
                    for (int i = 0; i < 30; i++) {
                        if (tmp_DAILY_30_DATES[i].equals(d)) {
                            tmp_DAILY_30_DT[i] = dtThuan; break;
                        }
                    }
                }

                for (int i = 0; i < n; i++) {
                    double[] sang  = busThongKe.getKetQuaCaSang(tmp_NV_IDS[i], year, fCondHD);
                    double[] chieu = busThongKe.getKetQuaCaChieu(tmp_NV_IDS[i], year, fCondHD);
                    double[] toi   = busThongKe.getKetQuaCaToi(tmp_NV_IDS[i], year, fCondHD);
                    tmp_NV_HD_S[i] = (int) sang[0];  tmp_NV_DT_S[i] = sang[1];
                    tmp_NV_HD_C[i] = (int) chieu[0]; tmp_NV_DT_C[i] = chieu[1];
                    tmp_NV_HD_T[i] = (int) toi[0];   tmp_NV_DT_T[i] = toi[1];
                    
                    double[] tra = busThongKe.getThongKeTraHangNV(tmp_NV_IDS[i], year, fCondHD);
                    tmp_NV_HD_TRA[i] = (int) tra[0];
                    tmp_NV_DT_TRA[i] = tra[1] / 1_000_000.0; 
                }

                double[] kpiData  = busThongKe.getKpiTongQuat(fCondHD);
                long   fHD        = (long) kpiData[1]; 

                double[] tcData  = busThongKe.getKpiTaiChinh(fCondHD);
                final double fDTT   = tcData[3]; 
                final double fGV    = tcData[4]; 
                final double fLNG   = tcData[5]; 
                final double fVATtc = tcData[1]; 
                final double fTyLe  = tcData[6]; 

                int peakMonth = 0;
                for (int i = 1; i < tmp_DT_DATA.length; i++)
                    if (tmp_DT_DATA[i] > tmp_DT_DATA[peakMonth]) peakMonth = i;

                final int    fPeak  = peakMonth;
                final String kyStr  = getKyString();
                final int    fYear  = year;
                final int[]  fDonut = tmp_DONUT;

                final String[] fn  = tmp_NV_NAMES;  final String[] fids = tmp_NV_IDS;
                final String[] fro = tmp_NV_ROLES;  final String[] fsh  = tmp_NV_SHORT;
                final Color[]  fc  = tmp_NV_COLORS;
                final int[] fHDS=tmp_NV_HD_S, fHDC=tmp_NV_HD_C, fHDT=tmp_NV_HD_T;
                final double[] fDTS=tmp_NV_DT_S, fDTC=tmp_NV_DT_C, fDT_T=tmp_NV_DT_T;
                final int[] fHDTra = tmp_NV_HD_TRA;
                final double[] fDTTra = tmp_NV_DT_TRA;
                final String[] fD10=tmp_DATES_10; final int[][] fDaily=tmp_NV_DAILY;
                final double[] fD30=tmp_DAILY_30_DT; final int[] fH30=tmp_DAILY_30_HD;
                final String[] fDates30=tmp_DAILY_30_DATES;
                final double[] fDT12=tmp_DT_DATA, fCP12=tmp_CP_DATA, fLN12=tmp_LN_DATA;
                final String[] fChartLbls = tmp_CHART_LABELS;

                SwingUtilities.invokeLater(() -> {
                    NV_NAMES=fn; NV_IDS=fids; NV_ROLES=fro; NV_SHORT=fsh; NV_COLORS=fc;
                    NV_HD_S=fHDS; NV_HD_C=fHDC; NV_HD_T=fHDT;
                    NV_DT_S=fDTS; NV_DT_C=fDTC; NV_DT_T=fDT_T;
                    NV_HD_TRA=fHDTra; NV_DT_TRA=fDTTra;
                    DATES_10=fD10; NV_DAILY=fDaily;
                    DT_DATA=fDT12; CP_DATA=fCP12; LN_DATA=fLN12;
                    CHART_LABELS=fChartLbls;
                    DONUT_VALS=fDonut;
                    DAILY_30_DT=fD30; DAILY_30_HD=fH30; DAILY_30_DATES=fDates30;
                    // Cập nhật subtitle biểu đồ
                    if (lblChartSub != null) lblChartSub.setText(fChartSubtitle);

                    if (kpiDTVal  != null) {
                        kpiDTVal.setText(formatK(fDTT));
                        kpiDTSub.setText("doanh thu thuần (sau trả hàng)");
                    }
                    if (kpiGVVal  != null) {
                        kpiGVVal.setText(formatK(fGV));
                        kpiGVSub.setText("giá vốn hàng bán (COGS)");
                    }
                    if (kpiLNGVal != null) {
                        kpiLNGVal.setText(formatK(fLNG));
                        kpiLNGSub.setText(String.format("biên LN gộp %.1f%%", fTyLe));
                    }
                    if (kpiTBVal  != null) {
                        kpiTBVal.setText(formatK(fVATtc));
                        kpiTBSub.setText("thuế VAT 5% & 10% thu hộ");
                    }
                    if (lblYearBadge!=null) lblYearBadge.setText(kyStr+" "+fYear);
                    if (btnNamPicker!=null) btnNamPicker.setText(" "+fYear+" ▼");

                    if (cboNhanVien != null) {
                        String sel = (String) cboNhanVien.getSelectedItem();
                        ActionListener[] als = cboNhanVien.getActionListeners();
                        for (ActionListener l : als) cboNhanVien.removeActionListener(l);
                        cboNhanVien.removeAllItems(); cboNhanVien.addItem("Tất cả");
                        for (String nm : NV_NAMES) cboNhanVien.addItem(nm);
                        if (sel != null) cboNhanVien.setSelectedItem(sel);
                        for (ActionListener l : als) cboNhanVien.addActionListener(l);
                    }

                    if (chartBarMain  != null) chartBarMain.repaint();
                    if (chartLineDaily!= null) chartLineDaily.setData(DAILY_30_DT, DAILY_30_DATES);
                    if (chartDonut    != null) chartDonut.repaint();
                    if (chartNVDaily  != null) chartNVDaily.repaint();
                    if (chartNVShift  != null) chartNVShift.repaint();
                    if (tblNVDetail   != null) tblNVDetail.refreshData();
                    if (pnlBody       != null) { pnlBody.revalidate(); pnlBody.repaint(); }

                    int soNV = NV_NAMES.length;
                    if (lblTongNV != null) lblTongNV.setText(String.valueOf(soNV));
                    
                    double sumDT_NV = 0;
                    for (double s : NV_DT_S) sumDT_NV += s;
                    for (double c : NV_DT_C) sumDT_NV += c;
                    for (double t : NV_DT_T) sumDT_NV += t;
                    for (double tra : NV_DT_TRA) sumDT_NV -= tra; 
                    
                    if (lblTongDT_NV != null) lblTongDT_NV.setText(formatM(sumDT_NV));
                    if (lblDTTB_NV   != null) lblDTTB_NV.setText(soNV>0 ? formatM(sumDT_NV/soNV) : "0đ");

                    int sHS=0, sHC=0, sHT=0;
                    for (int s:NV_HD_S) sHS+=s; for (int c:NV_HD_C) sHC+=c; for (int t:NV_HD_T) sHT+=t;
                    if (lblHDSang  != null) lblHDSang.setText(sHS+" HĐ");
                    if (lblHDChieu != null) lblHDChieu.setText(sHC+" HĐ");
                    if (lblHDToi   != null) lblHDToi.setText(sHT+" HĐ");

                    double sumDT30=0, maxDT30=0; int sumHD30=0, actDays=0; String pk="--";
                    for (int i=0;i<30;i++) {
                        if (DAILY_30_DT[i]>0||DAILY_30_HD[i]>0) actDays++;
                        sumDT30+=DAILY_30_DT[i]; sumHD30+=DAILY_30_HD[i];
                        if (DAILY_30_DT[i]>maxDT30) { maxDT30=DAILY_30_DT[i]; pk=DAILY_30_DATES[i].length()>=5?DAILY_30_DATES[i].substring(0,5):"--"; }
                    }
                    double avgDT=actDays>0?sumDT30/actDays:0, avgHD=actDays>0?(double)sumHD30/actDays:0;
                    if (miniPeakDate  != null) miniPeakDate.setText(pk);
                    if (miniAvgDT     != null) miniAvgDT.setText(formatM(avgDT));
                    if (miniAvgOrder  != null) miniAvgOrder.setText(String.format("%.0f", avgHD));

                    reloadTopSP(fCondHD, fYear);
                    reloadTaiChinh(fCondHD);
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    /** Load dữ liệu tab Đơn hàng */
    private void loadDonHangTab() {
        final BUS.BUS_ThongKe.ThongKeFilter f = buildCondHD();
        if (lblDonPeriod != null) lblDonPeriod.setText(getKyString() + " " + currentYear);
        new Thread(() -> {
        	double[] kpi = busThongKe.getKpiTongQuat(f);
        	double[] kpiTC = busThongKe.getKpiTaiChinh(f); // Gọi thêm hàm này

        	long tongDon   = (long) kpi[1];  // tổng = bán + trả
        	int  fBanC     = (int) kpi[6];   // đơn BAN_HANG
        	int  fTraC     = (int) kpi[7];   // đơn TRA_HANG
        	double tongDTT = kpiTC[3]; // Lấy đúng Doanh Thu Thuần
        	double dtTB    = fBanC > 0 ? tongDTT / fBanC : 0; // TB chỉ tính trên đơn bán

            // Phân bổ theo giờ dùng 30 ngày gần nhất làm xấp xỉ
            double[] hourData = busThongKe.getDTTheoGioTrongNgay(LocalDate.now().toString(), f);

            // Top giờ cao điểm
            int gioCaoDiem = 0;
            for (int i = 1; i < 24; i++)
                if (hourData[i] > hourData[gioCaoDiem]) gioCaoDiem = i;

            // Danh sách hóa đơn gần nhất
            java.util.List<Object[]> hdList = busThongKe.getHoaDonGanNhat(f, 50);

            final long fTong = tongDon; final double fDTTB = dtTB;
            final double fVAT = kpiTC[1]; final int fGioCao = gioCaoDiem;
            final double[] fHour = hourData;
            final java.util.List<Object[]> fHDList = hdList;
            final int fBanCount = fBanC, fTraCount = fTraC;

            SwingUtilities.invokeLater(() -> {
                donBanCount = fBanCount;
                donTraCount = fTraCount;
                if (lblDonKpiTong != null) { lblDonKpiTong.setText(String.format("%,d", fTong)); lblDonKpiTongSub.setText("hóa đơn"); }
                if (lblDonKpiTB   != null) { lblDonKpiTB.setText(formatK(fDTTB));  lblDonKpiTBSub.setText("TB / đơn"); }
                if (lblDonKpiGioC != null) { lblDonKpiGioC.setText(fGioCao+"h–"+(fGioCao+1)+"h"); lblDonKpiGioCsub.setText("giờ cao điểm"); }
                if (lblDonKpiSP   != null) { lblDonKpiSP.setText(formatK(fVAT)); lblDonKpiSPSub.setText("tổng VAT"); }
                if (chartDonHour  != null) { chartDonHour.setData(fHour); chartDonHour.repaint(); }
                if (modelDonHang  != null) {
                    modelDonHang.setRowCount(0);
                    for (Object[] r : fHDList) {
                        // r: {id, ngayLap, nhanVien, soSP, tongTien}
                        // row: {id[0], khachHang[1], thucThu[2], gio[3], loaiHD[4]}
                        String id    = String.valueOf(r[0]);
                        String kh    = String.valueOf(r[1]);
                        double tt    = r[2] instanceof Number ? ((Number)r[2]).doubleValue() : 0;
                        String gio   = String.valueOf(r[3]);
                        String loai  = String.valueOf(r[4]);
                        String icon  = "BAN_HANG".equals(loai) ? "" : "[Trả] ";
                        modelDonHang.addRow(new Object[]{ id, gio, icon+kh, "", formatK(tt) });
                    }
                }
            });
        }).start();
    }

    /** Load NV đang trực ca hiện tại */
    private void loadNVDangTruc() {
        if (modelDangTruc == null) return;
        new Thread(() -> {
            java.util.List<CaLamViec> dsAll = busCaLamViec.getLichSuCa();
            LocalDate today = LocalDate.now();
            java.util.List<Object[]> rows = new ArrayList<>();
            DateTimeFormatter hhmm = DateTimeFormatter.ofPattern("HH:mm");
            for (CaLamViec ca : dsAll) {
                if (ca.getThoiGianKetThuc() != null) continue; // đã kết ca
                if (!ca.getThoiGianBatDau().toLocalDate().equals(today)) continue;
                String maNV   = ca.getNhanVienId() != null ? ca.getNhanVienId().getNhanVien() : "?";
                String tenNV  = ""; // Tìm tên NV từ NV_IDS/NV_NAMES
                for (int i = 0; i < NV_IDS.length; i++)
                    if (NV_IDS[i].equals(maNV)) { tenNV = NV_NAMES[i]; break; }
                if (tenNV.isEmpty()) tenNV = maNV;
                String gioBD  = ca.getThoiGianBatDau().format(hhmm);
                String[] caLabels = {"Ca Sáng","Ca Chiều","Ca Tối"};
                String loaiCa = (ca.getLoaiCa() >= 0 && ca.getLoaiCa() < 3) ? caLabels[ca.getLoaiCa()] : "Ca "+ca.getLoaiCa();
                rows.add(new Object[]{ tenNV, loaiCa, gioBD });
            }
            SwingUtilities.invokeLater(() -> {
                modelDangTruc.setRowCount(0);
                for (Object[] r : rows) modelDangTruc.addRow(r);
                if (pnlDangTruc != null) pnlDangTruc.setVisible(!rows.isEmpty());
            });
        }).start();
    }

    private void loadKhachHang(int year, BUS.BUS_ThongKe.ThongKeFilter condHD) {
        new Thread(() -> {
            Object[] kpi = busThongKe.getKpiKhachHang();
            int tk=(int)kpi[0], kc=(int)kpi[1], td=(int)kpi[2];
            int[] khMoi = busThongKe.getKHMoiTheoThang(year);
            java.util.List<Object[]> topKH = busThongKe.getTopKhachHangTheoDiem(10);
            SwingUtilities.invokeLater(() -> {
                if (lblKHTotal   != null) lblKHTotal.setText(String.valueOf(tk));
                if (lblKHCoTK    != null) lblKHCoTK.setText(String.valueOf(kc));
                if (lblKHTongDiem!= null) lblKHTongDiem.setText(String.format("%,d", td));
                if (chartKHMoi   != null) { chartKHMoi.setData(khMoi); chartKHMoi.repaint(); }
                if (modelTopKH   != null) {
                    modelTopKH.setRowCount(0);
                    int rk=1;
                    for (Object[] r : topKH) {
                        String m=rk==1?"Top 1":rk==2?"Top 2":rk==3?"Top 3":String.valueOf(rk);
                        modelTopKH.addRow(new Object[]{
                            m,               // hạng (Top 1, Top 2...)
                            r[0],            // tên KH  [hoVaTen]
                            (int) r[2],      // số đơn  [soHD]
                            formatM((double) r[3]),               // doanh thu (r[3] đã là triệu → formatM nhân 1_000_000)
                            String.format("%,d", (int) r[4]) + " đ."  // điểm tích lũy
                        });
                        rk++;
                    }
                }
            });
        }).start();
    }

    private void loadKho(int year) {
        new Thread(() -> {
            double gt = busThongKe.getTongGiaTriTonKho();
            Object[] sl = busThongKe.getSoLoTheoTrangThai();
            java.util.List<Object[]> tkKho  = busThongKe.getTonKhoTheoKho();
            java.util.List<Object[]> topTon = busThongKe.getTopSPTonNhieu();
            double[] nhap = busThongKe.getNhapHang12Thang(year);
            SwingUtilities.invokeLater(() -> {
                if (lblKhoGiaTriTon!= null) lblKhoGiaTriTon.setText(formatK(gt));
                if (lblKhoSoLo     != null) lblKhoSoLo.setText(String.valueOf((int)sl[0]));
                if (lblKhoHetHan   != null) lblKhoHetHan.setText(String.valueOf((int)sl[2]));
                if (chartNhapHang  != null) { chartNhapHang.setData(nhap); chartNhapHang.repaint(); }
                if (chartTonKhoKho != null) {
                    String[] lb=new String[tkKho.size()]; double[] vl=new double[tkKho.size()];
                    for (int i=0;i<tkKho.size();i++) { lb[i]=(String)tkKho.get(i)[0]; vl[i]=(double)tkKho.get(i)[2]; }
                    chartTonKhoKho.setData(lb, vl); chartTonKhoKho.repaint();
                }
                if (modelTonKho != null) {
                    modelTonKho.setRowCount(0); int rk=1;
                    for (Object[] r : topTon)
                        modelTonKho.addRow(new Object[]{rk++,r[0],String.format("%,d",(int)r[1]),formatM((double)r[2])});
                }
            });
        }).start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FORMATTERS & HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private String formatM(double valInMillions) { return formatK(valInMillions * 1_000_000); }
    private String formatK(double rawVal) { return String.format("%,.0f đ", rawVal).replace(",", "."); }

    private String getKyString() {
        if ("QUY".equals(modeLocThoiGian)) {
            int i = cboQuy.getSelectedIndex();
            if (i==0) return "Q1"; if (i==1) return "Q2"; if (i==2) return "Q3"; if (i==3) return "Q4";
            return "Quý";
        }
        if ("TUYCHINH".equals(modeLocThoiGian)) return "Tùy chỉnh";
        if ("HOM_NAY".equals(modeLocThoiGian))  return "Hôm nay";
        if ("TUAN".equals(modeLocThoiGian))      return "Tuần này";
        if ("NAM".equals(modeLocThoiGian))       return "Cả năm";
        String s = (String) cboThang.getSelectedItem();
        return s != null ? s : "Cả năm";
    }

    private String getDayOfWeek(DayOfWeek d) {
        switch (d) {
            case MONDAY:    return "Thứ Hai";
            case TUESDAY:   return "Thứ Ba";
            case WEDNESDAY: return "Thứ Tư";
            case THURSDAY:  return "Thứ Năm";
            case FRIDAY:    return "Thứ Sáu";
            case SATURDAY:  return "Thứ Bảy";
            default:        return "Chủ Nhật";
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER COMPONENTS
    // ─────────────────────────────────────────────────────────────────────────
    private JScrollPane makeScrollPane(JComponent content) {
        JScrollPane sp = new JScrollPane(content);
        sp.setOpaque(false); sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setWheelScrollingEnabled(true);
        sp.getVerticalScrollBar().setUnitIncrement(30);
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        sp.getViewport().addChangeListener(e -> { content.revalidate(); content.repaint(); });
        return sp;
    }

    private void attachDynamicRows(JTable tbl, JScrollPane sp) {
        Runnable adj = () -> {
            int rows = tbl.getRowCount(); if (rows<=0) return;
            int viewH = sp.getViewport().getHeight();
            int hdrH  = tbl.getTableHeader()!=null ? tbl.getTableHeader().getHeight() : 0;
            int avail = viewH - hdrH; if (avail<=0) return;
            
            int newRH = Math.max(30, avail/rows);
            newRH = Math.min(newRH, 40);
            
            if (tbl.getRowHeight()!=newRH) tbl.setRowHeight(newRH);
        };
        sp.getViewport().addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { adj.run(); }
        });
        tbl.getModel().addTableModelListener(e -> SwingUtilities.invokeLater(adj));
    }

    private JPanel card14() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(14, 14, 10, 14)));
        return p;
    }

    private JTable mkTable(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(30);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.getTableHeader().setBackground(Color.decode("#F0F4FF"));
        t.setShowHorizontalLines(true);
        t.setGridColor(Color.decode("#F0F0F0"));
        t.setSelectionBackground(Color.decode("#F0F4FF"));
        t.setFillsViewportHeight(true);
        return t;
    }

    private JScrollPane mkSP(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        return sp;
    }

    private JButton navBtn(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(4, 10, 4, 10)));
        b.setBackground(Color.decode("#F8F9FA"));
        b.setForeground(Color.decode("#333333"));
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Tạo KPI card có thể click để xem chi tiết */
    private JPanel makeClickableKpiCard(String label, JLabel valLabel, JLabel subLabel,
                                        String iconType, String iconBg, String[] detailHeaders,
                                        java.util.function.Supplier<java.util.List<Object[]>> dataLoader) {
        JPanel p = statCardDynamic(label, valLabel, subLabel, iconType, iconBg);
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(14, 16, 14, 16)));

        // Hover effect
        p.addMouseListener(new MouseAdapter() {
            Color orig = p.getBackground();
            @Override public void mouseEntered(MouseEvent e) {
                p.setBackground(Color.decode("#F8F9FA")); p.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                p.setBackground(orig); p.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                showKpiDetail(label, detailHeaders, dataLoader);
            }
        });

        // "Click xem chi tiết" hint
        JLabel hint = new JLabel("Click để xem chi tiết");
        hint.setIcon(MenuIcon.of("SEARCH", 10, Color.decode("#BBBBBB")));
        hint.setIconTextGap(4);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        hint.setForeground(Color.decode("#BBBBBB"));
        p.add(hint, BorderLayout.SOUTH);
        return p;
    }

    private void showKpiDetail(String title, String[] headers, java.util.function.Supplier<java.util.List<Object[]>> loader) {
				Window owner = SwingUtilities.getWindowAncestor(this);
				JDialog dlg = new JDialog(owner instanceof Frame ? (Frame) owner : null, true);
				dlg.setUndecorated(true);
				dlg.setSize(680, 420);
				dlg.setResizable(true);
				dlg.setLocationRelativeTo(this);
				
				// Nền bo góc + Drop shadow
				JPanel root = new JPanel(new BorderLayout()) {
				@Override
				protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				for (int i = 0; i < 5; i++) {
				 g2.setColor(new Color(0, 0, 0, 10 - i * 2));
				 g2.fillRoundRect(i, i, getWidth() - i * 2, getHeight() - i * 2, 16, 16);
				}
				g2.setColor(Color.WHITE);
				g2.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 12, 12);
				g2.dispose();
				}
				};
				root.setOpaque(false);
				root.setBorder(new EmptyBorder(4, 4, 4, 4));
				
				// Header Gradient
				JPanel hdr = new JPanel(new BorderLayout()) {
				@Override
				protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setPaint(new GradientPaint(0, 0, Color.decode("#152A4B"), getWidth(), 0, Color.decode("#1A73E8")));
				g2.fillRoundRect(0, 0, getWidth(), getHeight() + 8, 12, 12);
				g2.dispose();
				}
				};
				hdr.setOpaque(false);
				hdr.setBorder(new EmptyBorder(14, 20, 14, 15));
				
				JLabel lblTitle = new JLabel(title.toUpperCase(), SwingConstants.LEFT);
				lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
				lblTitle.setForeground(Color.WHITE);
				lblTitle.setIcon(MenuIcon.of("CHART", 18, Color.WHITE));
				lblTitle.setIconTextGap(10);
				
				JButton btnClose = new JButton(MenuIcon.of("CLOSE", 14, new Color(255, 255, 255, 180)));
				btnClose.setContentAreaFilled(false);
				btnClose.setBorderPainted(false);
				btnClose.setFocusPainted(false);
				btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
				btnClose.addActionListener(e -> dlg.dispose());
				btnClose.addMouseListener(new MouseAdapter() {
				public void mouseEntered(MouseEvent e) { btnClose.setIcon(MenuIcon.of("CLOSE", 14, Color.WHITE)); }
				public void mouseExited(MouseEvent e) { btnClose.setIcon(MenuIcon.of("CLOSE", 14, new Color(255, 255, 255, 180))); }
				});
				hdr.add(lblTitle, BorderLayout.CENTER);
				hdr.add(btnClose, BorderLayout.EAST);
				
				// Table Model
				DefaultTableModel m = new DefaultTableModel(headers, 0) {
				@Override public boolean isCellEditable(int r, int c) { return false; }
				};
				JTable tbl = new JTable(m);
				tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
				tbl.setRowHeight(36);
				tbl.setShowGrid(false);
				tbl.setShowHorizontalLines(true);
				tbl.setGridColor(Color.decode("#F1F3F5"));
				tbl.setSelectionBackground(Color.decode("#EBF5FF"));
				tbl.setIntercellSpacing(new Dimension(0, 0));
				tbl.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
				tbl.setFillsViewportHeight(true);
				
				tbl.getTableHeader().setPreferredSize(new Dimension(0, 38));
				tbl.getTableHeader().setBackground(Color.decode("#F4F6F8"));
				tbl.getTableHeader().setForeground(Color.decode("#637381"));
				tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
				tbl.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#DFE3E8")));
				
				// Custom Renderer (Align Right, Bold, Colors)
				tbl.setDefaultRenderer(Object.class, (t, val, sel, foc, r, c) -> {
				JLabel l = new JLabel(val != null ? val.toString() : "", SwingConstants.LEFT);
				l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
				l.setBorder(new EmptyBorder(4, 12, 4, 12));
				l.setOpaque(true);
				l.setBackground(sel ? Color.decode("#EBF5FF") : (r % 2 == 0 ? Color.WHITE : Color.decode("#FAFBFC")));
				l.setForeground(Color.decode("#212B36"));
				
				String colName = t.getColumnName(c).toLowerCase();
				if (colName.contains("doanh thu") || colName.contains("thực thu") || colName.contains("điểm") || colName.contains("vat")) {
				l.setHorizontalAlignment(SwingConstants.RIGHT);
				l.setForeground(Color.decode("#1A73E8"));
				l.setFont(new Font("Segoe UI", Font.BOLD, 13));
				} else if (colName.contains("số")) {
				l.setHorizontalAlignment(SwingConstants.CENTER);
				l.setFont(new Font("Segoe UI", Font.BOLD, 13));
				l.setForeground(Color.decode("#152A4B"));
				}
				return l;
				});
				
				JScrollPane scroll = new JScrollPane(tbl);
				scroll.setBorder(new EmptyBorder(0, 8, 8, 8));
				scroll.getViewport().setBackground(Color.WHITE);
				scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI());
				
				// Footer đếm dòng
				JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
				footer.setBackground(Color.decode("#F9FAFB"));
				footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#EEF2F6")));
				JLabel lblCount = new JLabel("Dang tai...");
				lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 11));
				lblCount.setForeground(Color.GRAY);
				footer.add(lblCount);
				
				root.add(hdr, BorderLayout.NORTH);
				root.add(scroll, BorderLayout.CENTER);
				root.add(footer, BorderLayout.SOUTH);
				dlg.setContentPane(root);
				
				// Fetch data
				new Thread(() -> {
				java.util.List<Object[]> data = loader.get();
				SwingUtilities.invokeLater(() -> {
				m.setRowCount(0);
				for (Object[] r : data) m.addRow(r);
				lblCount.setText(data.size() + " ban ghi");
				});
				}).start();
				
				dlg.setVisible(true);
				}

    // ─────────────────────────────────────────────────────────────────────────
    // TAB 1: DOANH THU
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildViewDT() {
        JPanel root = new ScrollablePanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);

        // ── 4 KPI card tài chính ──────────────────────────────────────────────
        // [0] Doanh thu thuần  [1] Giá vốn hàng bán  [2] Lợi nhuận gộp  [3] VAT thu hộ
        kpiDTVal  = makeKpiVal("...", "#1A73E8"); kpiDTSub  = makeKpiSub("Đang tải...");
        kpiGVVal  = makeKpiVal("...", "#9C27B0"); kpiGVSub  = makeKpiSub("...");
        kpiLNGVal = makeKpiVal("...", "#00A76F"); kpiLNGSub = makeKpiSub("...");
        kpiTBVal  = makeKpiVal("...", "#FF9800"); kpiTBSub  = makeKpiSub("...");

        // Header detail popup columns
        String[] hdrsDTT  = {"Kỳ", "Doanh thu gộp", "(−) Thuế VAT", "(−) Hoàn trả", "Doanh thu thuần"};
        String[] hdrsGV   = {"Kỳ", "Doanh thu thuần", "Giá vốn", "Lợi nhuận gộp", "Tỷ lệ LN%"};
        // VAT popup: thêm cột Hoàn trả để làm rõ tại sao DT gộp lớn hơn VAT tương ứng
        String[] hdrsVAT  = {"Kỳ", "DT gộp (bán ra)", "(−) Hoàn trả", "DT sau trả", "(−) Thuế VAT (net)", "Doanh thu thuần"};

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // Card 1 – Doanh thu thuần
        cards.add(makeClickableKpiCard("Doanh thu thuần", kpiDTVal, kpiDTSub, "TAB_CHART", "#EEF2FF",
                hdrsDTT, () -> {
                    java.util.List<Object[]> rows = new ArrayList<>();
                    BUS.BUS_ThongKe.ThongKeFilter f = buildCond();
                    String groupBy = ("HOM_NAY".equals(modeLocThoiGian) || "TUAN".equals(modeLocThoiGian) || "TUYCHINH".equals(modeLocThoiGian)) ? "NGAY" : "THANG";
                    for (Object[] row : busThongKe.getBaoCaoTaiChinh(f, groupBy))
                        rows.add(new Object[]{ row[0],
                            formatK((double) row[1]), formatK((double) row[2]),
                            formatK((double) row[3]), formatK((double) row[4]) });
                    return rows;
                }));

        // Card 2 – Giá vốn hàng bán (COGS)
        cards.add(makeClickableKpiCard("Giá vốn hàng bán", kpiGVVal, kpiGVSub, "STORE", "#F3E5F5",
                hdrsGV, () -> {
                    java.util.List<Object[]> rows = new ArrayList<>();
                    BUS.BUS_ThongKe.ThongKeFilter f = buildCond();
                    String groupBy = ("HOM_NAY".equals(modeLocThoiGian) || "TUAN".equals(modeLocThoiGian) || "TUYCHINH".equals(modeLocThoiGian)) ? "NGAY" : "THANG";
                    for (Object[] row : busThongKe.getBaoCaoTaiChinh(f, groupBy)) {
                        double dtt  = (double) row[4];
                        double cogs = (double) row[5];
                        double ln   = (double) row[6];
                        double tyLe = dtt > 0 ? ln / dtt * 100 : 0;
                        rows.add(new Object[]{ row[0],
                            formatK(dtt), formatK(cogs), formatK(ln),
                            String.format("%.1f%%", tyLe) });
                    }
                    return rows;
                }));

        // Card 3 – Lợi nhuận gộp
        cards.add(makeClickableKpiCard("Lợi nhuận gộp", kpiLNGVal, kpiLNGSub, "TAB_CHART", "#E8F5E9",
                hdrsGV, () -> {
                    java.util.List<Object[]> rows = new ArrayList<>();
                    BUS.BUS_ThongKe.ThongKeFilter f = buildCond();
                    String groupBy = ("HOM_NAY".equals(modeLocThoiGian) || "TUAN".equals(modeLocThoiGian) || "TUYCHINH".equals(modeLocThoiGian)) ? "NGAY" : "THANG";
                    for (Object[] row : busThongKe.getBaoCaoTaiChinh(f, groupBy)) {
                        double dtt  = (double) row[4];
                        double cogs = (double) row[5];
                        double ln   = (double) row[6];
                        double tyLe = dtt > 0 ? ln / dtt * 100 : 0;
                        rows.add(new Object[]{ row[0],
                            formatK(dtt), formatK(cogs), formatK(ln),
                            String.format("%.1f%%", tyLe) });
                    }
                    return rows;
                }));

        // Card 4 – Thuế VAT thu hộ
        cards.add(makeClickableKpiCard("Thuế VAT thu hộ", kpiTBVal, kpiTBSub, "DOCUMENT", "#FFF3E0",
                hdrsVAT, () -> {
                    java.util.List<Object[]> rows = new ArrayList<>();
                    BUS.BUS_ThongKe.ThongKeFilter f = buildCond();
                    String groupBy = ("HOM_NAY".equals(modeLocThoiGian) || "TUAN".equals(modeLocThoiGian) || "TUYCHINH".equals(modeLocThoiGian)) ? "NGAY" : "THANG";
                    for (Object[] row : busThongKe.getBaoCaoTaiChinh(f, groupBy)) {
                        double dtGop    = (double) row[1]; // Doanh thu gộp (bán ra)
                        double hoanTra  = (double) row[3]; // Hàng bán bị trả lại
                        double dtSauTra = dtGop - hoanTra; // DT gộp sau khi trừ hoàn trả
                        double thueVAT  = (double) row[2]; // VAT thuần (bán - hoàn trả)
                        double dtThuan  = (double) row[4]; // Doanh thu thuần
                        rows.add(new Object[]{ row[0],
                            formatK(dtGop), formatK(hoanTra), formatK(dtSauTra),
                            formatK(thueVAT), formatK(dtThuan) });
                    }
                    return rows;
                }));
        root.add(cards);
        root.add(Box.createVerticalStrut(12));

        chartBarMain = new BarChartMain();
        // Tạo chart card thủ công để giữ ref lblChartSub (cập nhật theo filter)
        JPanel cBarCard = new JPanel(new BorderLayout(0, 4));
        cBarCard.setBackground(Color.WHITE);
        cBarCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(14, 14, 10, 14)));
        JLabel cBarTitle = new JLabel(" Doanh thu – Chi phí – Lợi nhuận", MenuIcon.of("TAB_CHART", 16, Color.decode("#152A4B")), SwingConstants.LEFT);
        cBarTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cBarTitle.setForeground(Color.decode("#152A4B"));
        lblChartSub = new JLabel("12 tháng · " + currentYear);
        lblChartSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblChartSub.setForeground(Color.decode("#888888"));
        JPanel cBarTitles = new JPanel(new GridLayout(2, 1, 0, 2));
        cBarTitles.setOpaque(false);
        cBarTitles.add(cBarTitle);
        cBarTitles.add(lblChartSub);
        chartBarMain.setPreferredSize(new Dimension(0, 280));
        cBarCard.add(cBarTitles, BorderLayout.NORTH);
        cBarCard.add(chartBarMain, BorderLayout.CENTER);
        cBarCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        root.add(cBarCard);
        root.add(Box.createVerticalStrut(12));

        JPanel row2 = new JPanel(new GridLayout(1, 2, 12, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));

        chartLineDaily = new LineChartDaily();
        JPanel lineCard = new JPanel(new BorderLayout(0, 8));
        lineCard.setBackground(Color.WHITE);
        lineCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(14, 14, 14, 14)));
        JLabel ltitle = new JLabel(" Thống kê theo ngày (30 ngày gần nhất)", new MenuIcon("CALENDAR"), SwingConstants.LEFT);
        ltitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ltitle.setForeground(Color.decode("#152A4B"));
        miniPeakDate = makeKpiVal("--","#1A73E8");
        miniAvgDT    = makeKpiVal("--","#00A76F");
        miniAvgOrder = makeKpiVal("--","#FF9800");
        JPanel miniStats = new JPanel(new GridLayout(1, 3, 8, 0));
        miniStats.setOpaque(false);
        miniStats.add(miniStatInlineDynamic("Ngày cao nhất",   miniPeakDate,"--",  "#1A73E8","#E3F2FD"));
        miniStats.add(miniStatInlineDynamic("DT TB / ngày",    miniAvgDT,   "bình quân","#00A76F","#E8F5E9"));
        miniStats.add(miniStatInlineDynamic("Đơn TB / ngày",   miniAvgOrder,"đơn / ngày","#FF9800","#FFF3E0"));
        lineCard.add(ltitle, BorderLayout.NORTH);
        lineCard.add(miniStats, BorderLayout.CENTER);
        lineCard.add(chartLineDaily, BorderLayout.SOUTH);
        chartLineDaily.setPreferredSize(new Dimension(0, 160));
        row2.add(lineCard);

        chartDonut = new DonutChart();
        row2.add(wrapChart("Doanh thu theo loại SP", "Cơ cấu sản phẩm", chartDonut, 200));
        root.add(row2);
        root.add(Box.createVerticalStrut(12));
        
        // Bảng Top SP – full width, chiều cao cố định
        JPanel pnlTopSP = buildTopSPTable();
        pnlTopSP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        pnlTopSP.setPreferredSize(new Dimension(0, 280));
        root.add(pnlTopSP);
        root.add(Box.createVerticalStrut(12));

        // Bảng Báo cáo tài chính – full width bên dưới, đủ chỗ xem các cột
        JPanel pnlTaiChinh = buildTaiChinhTable();
        pnlTaiChinh.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
        pnlTaiChinh.setPreferredSize(new Dimension(0, 300));
        root.add(pnlTaiChinh);
        root.add(Box.createVerticalStrut(12));

        JScrollPane sp = makeScrollPane(root);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(sp, BorderLayout.CENTER);
        return wrapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TAB 2: ĐƠN HÀNG (mới thay Theo ngày)
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildViewDonHang() {
        JPanel root = new ScrollablePanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);

        // Kỳ lọc hiển thị
        lblDonPeriod = new JLabel(getKyString() + " " + currentYear);
        lblDonPeriod.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDonPeriod.setForeground(Color.decode("#1A73E8"));
        JPanel periodBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        periodBar.setBackground(Color.WHITE);
        periodBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(4, 8, 4, 8)));
        periodBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JLabel lblDH = new JLabel(" Thống kê đơn hàng – Kỳ:");
        lblDH.setIcon(MenuIcon.of("DOCUMENT", 16, Color.decode("#152A4B")));
        periodBar.add(lblDH);
        periodBar.add(lblDonPeriod);
        root.add(periodBar);
        root.add(Box.createVerticalStrut(10));

        // KPI cards
        lblDonKpiTong = makeKpiVal("...", "#1A73E8"); lblDonKpiTongSub = makeKpiSub("hóa đơn");
        lblDonKpiTB   = makeKpiVal("...", "#00A76F"); lblDonKpiTBSub   = makeKpiSub("TB / đơn");
        lblDonKpiGioC = makeKpiVal("...", "#9C27B0"); lblDonKpiGioCsub = makeKpiSub("giờ cao điểm");
        lblDonKpiSP   = makeKpiVal("...", "#FF9800"); lblDonKpiSPSub   = makeKpiSub("tổng VAT");

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

        // Card Tổng đơn hàng – clickable popup phân loại bán / trả
        String[] hdrsDon = { "Loại đơn", "Số lượng", "Mô tả" };
        cards.add(makeClickableKpiCard("Tổng đơn hàng", lblDonKpiTong, lblDonKpiTongSub, "CART", "#EEF2FF",
                hdrsDon, () -> {
                    java.util.List<Object[]> rows = new ArrayList<>();
                    rows.add(new Object[]{ "Don ban hang",  donBanCount, "Hoa don ban san pham cho khach" });
                    rows.add(new Object[]{ "Don tra hang",  donTraCount, "Hoa don khach hoan tra / doi hang" });
                    rows.add(new Object[]{ "Tong cong",     donBanCount + donTraCount, "Tong tat ca loai hoa don" });
                    return rows;
                }));
        cards.add(statCardDynamic("Giá trị TB / đơn", lblDonKpiTB,   lblDonKpiTBSub,   "TAB_CHART", "#E8F5E9"));
        cards.add(statCardDynamic("Giờ cao điểm",     lblDonKpiGioC, lblDonKpiGioCsub, "CLOCK",     "#F3E5F5"));
        cards.add(statCardDynamic("Tổng VAT",          lblDonKpiSP,   lblDonKpiSPSub,   "DOCUMENT",  "#FFF3E0"));
        root.add(cards);
        root.add(Box.createVerticalStrut(12));

        // Biểu đồ phân bổ đơn hàng theo giờ
        chartDonHour = new HourBarChart();
        JPanel hCard = wrapChart("Phân bổ đơn hàng theo giờ trong ngày",
                "Màu: xanh=ca sáng, tím=ca chiều, xanh lá=ca tối", chartDonHour, 220);
        hCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        root.add(hCard);
        root.add(Box.createVerticalStrut(12));

        // Bảng đơn hàng gần nhất
        JPanel tblCard = card14();
        JPanel tblHeader = new JPanel(new BorderLayout());
        tblHeader.setOpaque(false);
        JLabel tblTitle = new JLabel(" Danh sách hóa đơn gần nhất", new MenuIcon("CART"), SwingConstants.LEFT);
        tblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblTitle.setForeground(Color.decode("#152A4B"));
        tblHeader.add(tblTitle, BorderLayout.WEST);
        tblCard.add(tblHeader, BorderLayout.NORTH);

        modelDonHang = new DefaultTableModel(
                new String[]{"Mã HĐ","Ngày giờ","Nhân viên","Số SP","Tổng tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tDon = mkTable(modelDonHang);
        tDon.getColumnModel().getColumn(3).setCellRenderer(new ShiftRenderer(Color.decode("#9C27B0")));
        tDon.getColumnModel().getColumn(4).setCellRenderer(new DTRenderer());
        tDon.getColumnModel().getColumn(0).setPreferredWidth(90);
        tDon.getColumnModel().getColumn(1).setPreferredWidth(100);
        tDon.getColumnModel().getColumn(2).setPreferredWidth(130);
        JScrollPane ssDon = mkSP(tDon);
        ssDon.setPreferredSize(new Dimension(0, 320));
        tblCard.add(ssDon, BorderLayout.CENTER);
        root.add(tblCard);
        root.add(Box.createVerticalStrut(12));

        JScrollPane sp = makeScrollPane(root);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(sp, BorderLayout.CENTER);
        return wrapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TAB 3: NHÂN VIÊN (có lọc ca + hiện NV đang trực)
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildViewNV() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);

        // KPI tổng quan
        lblTongNV   = makeKpiVal("...", "#1A73E8");
        lblDTTB_NV  = makeKpiVal("...", "#9C27B0");
        lblTongDT_NV= makeKpiVal("...", "#00A76F");

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        cards.add(statCardDynamic("Tổng nhân viên",   lblTongNV,    makeKpiSub("đang hoạt động"), "USERS",     "#EEF2FF"));
        cards.add(statCardDynamic("DT trung bình / NV",lblDTTB_NV,  makeKpiSub("mỗi nhân viên"),  "TAB_CHART", "#F3E5F5"));
        cards.add(statCardDynamic("Tổng doanh thu NV", lblTongDT_NV,makeKpiSub("tất cả nhân viên"),"TAB_CHART","#E8F5E9"));
        root.add(cards);
        root.add(Box.createVerticalStrut(12));

        // ─ PANEL NV ĐANG TRỰC CA ─
        pnlDangTruc = new JPanel(new BorderLayout(0, 8));
        pnlDangTruc.setBackground(Color.decode("#E8F5E9"));
        pnlDangTruc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#00A76F"), 1, true),
                new EmptyBorder(10, 14, 10, 14)));
        pnlDangTruc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JPanel dtHeader = new JPanel(new BorderLayout());
        dtHeader.setOpaque(false);
        JLabel dtTitle = new JLabel(" Nhân viên đang trực ca hôm nay");
        dtTitle.setIcon(MenuIcon.of("PEOPLE", 16, Color.decode("#00875A")));        dtTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dtTitle.setForeground(Color.decode("#00875A"));
        JButton btnRefreshTruc = navBtn("Làm mới");
        btnRefreshTruc.setIcon(MenuIcon.of("REFRESH", 14, Color.decode("#333333")));
        btnRefreshTruc.addActionListener(e -> loadNVDangTruc());
        dtHeader.add(dtTitle, BorderLayout.WEST);
        dtHeader.add(btnRefreshTruc, BorderLayout.EAST);
        pnlDangTruc.add(dtHeader, BorderLayout.NORTH);

        modelDangTruc = new DefaultTableModel(new String[]{"Nhân viên","Loại ca","Giờ vào ca"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tDT = mkTable(modelDangTruc);
        tDT.setBackground(Color.decode("#E8F5E9"));
        tDT.getTableHeader().setBackground(Color.decode("#C8E6C9"));
        tDT.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                String s = val != null ? val.toString() : "";
                setForeground(s.contains("Sáng") ? Color.decode("#E65100") :
                              s.contains("Chiều")? Color.decode("#3949AB") : Color.decode("#6A1B9A"));
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                return this;
            }
        });
        tDT.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                setFont(new Font("Segoe UI Semibold", Font.BOLD, 13));
                setForeground(Color.decode("#00875A"));
                return this;
            }
        });
        JScrollPane spDT = mkSP(tDT);
        spDT.setPreferredSize(new Dimension(0, 90));
        spDT.getViewport().setBackground(Color.decode("#E8F5E9"));
        pnlDangTruc.add(spDT, BorderLayout.CENTER);
        root.add(pnlDangTruc);
        root.add(Box.createVerticalStrut(10));

        // ─ LỌC THEO CA ─
        JPanel caFilterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        caFilterBar.setBackground(Color.WHITE);
        caFilterBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(4, 8, 4, 8)));
        caFilterBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        JLabel lblLocCa = new JLabel(" Lọc ca làm việc:");
        lblLocCa.setIcon(MenuIcon.of("FILTER", 14, Color.decode("#444444")));
        caFilterBar.add(lblLocCa);
        cboCaFilter = new JComboBox<>(new String[]{"Tất cả ca", "Ca Sáng (6h–13h)", "Ca Chiều (14h–21h)", "Ca Tối (22h–6h)"});
        styleCombo(cboCaFilter);
        cboCaFilter.addActionListener(e -> {
            int selCa = cboCaFilter.getSelectedIndex(); // 0=all,1=sang,2=chieu,3=toi
            if (chartNVShift != null) chartNVShift.setCaFilter(selCa);
            if (tblNVDetail  != null) tblNVDetail.setCaFilter(selCa);
            repaint();
        });
        caFilterBar.add(cboCaFilter);
        root.add(caFilterBar);
        root.add(Box.createVerticalStrut(10));

        // Biểu đồ NV theo ngày
        chartNVDaily = new NVDailyChart();
        JPanel nvDailyCard = new JPanel(new BorderLayout(0, 8));
        nvDailyCard.setBackground(Color.WHITE);
        nvDailyCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(14, 14, 10, 14)));
        nvDailyCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        JLabel ndTitle = new JLabel(" Số HĐ nhân viên theo ngày (10 ngày gần nhất)", new MenuIcon("CALENDAR"), SwingConstants.LEFT);
        ndTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ndTitle.setForeground(Color.decode("#152A4B"));
        chartNVDaily.setPreferredSize(new Dimension(0, 200));
        nvDailyCard.add(ndTitle, BorderLayout.NORTH);
        nvDailyCard.add(chartNVDaily, BorderLayout.CENTER);
        nvDailyCard.add(buildNVDailyLegend(), BorderLayout.SOUTH);
        root.add(nvDailyCard);
        root.add(Box.createVerticalStrut(12));

        // Phân bổ theo ca
        JPanel shiftCard = new JPanel(new BorderLayout(0, 8));
        shiftCard.setBackground(Color.WHITE);
        shiftCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(14, 14, 10, 14)));
        shiftCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        JLabel shTitle = new JLabel(" HĐ theo ca làm việc", new MenuIcon("CLOCK"), SwingConstants.LEFT);
        shTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        shTitle.setForeground(Color.decode("#152A4B"));

        lblHDSang  = makeKpiVal("...", "#F59E0B");
        lblHDChieu = makeKpiVal("...", "#6366F1");
        lblHDToi   = makeKpiVal("...", "#8B5CF6");

        JPanel shMini = new JPanel(new GridLayout(1, 3, 8, 0));
        shMini.setOpaque(false);
        shMini.add(statCardDynamic("Ca sáng (6h–13h)",  lblHDSang,  makeKpiSub("Hóa đơn"), "SHIFT_MORNING",   "#FFF8E1"));
        shMini.add(statCardDynamic("Ca chiều (14h–21h)",lblHDChieu, makeKpiSub("Hóa đơn"), "SHIFT_AFTERNOON", "#EEF2FF"));
        shMini.add(statCardDynamic("Ca tối (22h–6h)",   lblHDToi,   makeKpiSub("Hóa đơn"), "SHIFT_NIGHT",     "#F3E8FF"));

        chartNVShift = new NVShiftChart();
        chartNVShift.setPreferredSize(new Dimension(0, 140));

        JPanel shLegend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        shLegend.setOpaque(false);
        shLegend.add(legendDot(Color.decode("#FFAB00"), "Ca sáng"));
        shLegend.add(legendDot(Color.decode("#4F46E5"), "Ca chiều"));
        shLegend.add(legendDot(Color.decode("#8B5CF6"), "Ca tối"));

        JPanel shCenter = new JPanel(new BorderLayout(0, 6));
        shCenter.setOpaque(false);
        shCenter.add(shMini,        BorderLayout.NORTH);
        shCenter.add(chartNVShift,  BorderLayout.CENTER);
        shCenter.add(shLegend,      BorderLayout.SOUTH);
        shiftCard.add(shTitle,  BorderLayout.NORTH);
        shiftCard.add(shCenter, BorderLayout.CENTER);
        root.add(shiftCard);
        root.add(Box.createVerticalStrut(12));

        // Bảng chi tiết NV
        tblNVDetail = new NVDetailTable();
        tblNVDetail.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        root.add(tblNVDetail);
        root.add(Box.createVerticalStrut(12));

        JScrollPane sp = makeScrollPane(root);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(sp, BorderLayout.CENTER);
        return wrapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TAB 4: KHÁCH HÀNG
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildViewKH() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);

        lblKHTotal   = makeKpiVal("...", "#1A73E8");
        lblKHCoTK    = makeKpiVal("...", "#00A76F");
        lblKHTongDiem= makeKpiVal("...", "#FF9800");

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        cards.add(makeClickableKpiCard("Tổng khách hàng",   lblKHTotal,    makeKpiSub("đã đăng ký"), "USERS", "#EEF2FF",
                new String[]{"Tháng","KH mới"}, () -> {
                    int[] khm = busThongKe.getKHMoiTheoThang(currentYear);
                    java.util.List<Object[]> r = new ArrayList<>();
                    for (int i=0;i<12;i++) r.add(new Object[]{THANG[i], khm[i]});
                    return r;
                }));
        cards.add(statCardDynamic("KH có tài khoản",    lblKHCoTK,     makeKpiSub("thành viên"),   "USERS", "#E8F5E9"));
        cards.add(statCardDynamic("Tổng điểm tích lũy", lblKHTongDiem, makeKpiSub("điểm"),         "GIFT",  "#FFF3E0"));
        root.add(cards);
        root.add(Box.createVerticalStrut(12));

        chartKHMoi = new BarChartKHMoi();
        JPanel kc = wrapChart("Khách hàng mới theo tháng","Tăng trưởng KH · "+currentYear, chartKHMoi, 240);
        kc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        root.add(kc);
        root.add(Box.createVerticalStrut(12));

        JPanel topC = card14();
        JLabel topT = new JLabel(" Top khách hàng mua nhiều nhất", new MenuIcon("TROPHY"), SwingConstants.LEFT);
        topT.setFont(new Font("Segoe UI", Font.BOLD, 13));
        topT.setForeground(Color.decode("#152A4B"));
        topC.add(topT, BorderLayout.NORTH);
        modelTopKH = new DefaultTableModel(new String[]{"#","Khách hàng","Số đơn","Doanh thu","Điểm tích lũy"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tKH = mkTable(modelTopKH);
        tKH.getColumnModel().getColumn(2).setCellRenderer(new ShiftRenderer(Color.decode("#9C27B0")));
        tKH.getColumnModel().getColumn(3).setCellRenderer(new DTRenderer());
        tKH.getColumnModel().getColumn(4).setCellRenderer(new ShiftRenderer(Color.decode("#FF9800")));
        JScrollPane ssKH = mkSP(tKH);
        attachDynamicRows(tKH, ssKH);
        topC.add(ssKH, BorderLayout.CENTER);
        root.add(topC);
        root.add(Box.createVerticalStrut(12));

        JScrollPane sp = makeScrollPane(root);
        JPanel w = new JPanel(new BorderLayout());
        w.setOpaque(false);
        w.add(sp, BorderLayout.CENTER);
        return w;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TAB 5: KHO HÀNG (có gợi ý KM)
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildViewKho() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);

        lblKhoGiaTriTon = makeKpiVal("...", "#1A73E8");
        lblKhoSoLo      = makeKpiVal("...", "#00A76F");
        lblKhoHetHan    = makeKpiVal("...", "#FF5630");

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        cards.add(makeClickableKpiCard("Tổng giá trị tồn kho", lblKhoGiaTriTon, makeKpiSub("giá nhập · lô còn hàng"),
                "TAB_DOLLAR","#EEF2FF", new String[]{"Kho","Số lô","Giá trị"}, () -> {
                    java.util.List<Object[]> tk = busThongKe.getTonKhoTheoKho();
                    java.util.List<Object[]> r = new ArrayList<>();
                    for (Object[] row : tk) r.add(new Object[]{row[0], row[1], formatM((double)row[2])});
                    return r;
                }));
        cards.add(statCardDynamic("Lô hàng còn hàng",  lblKhoSoLo,  makeKpiSub("đang có hàng"), "GIFT",   "#E8F5E9"));
        cards.add(makeClickableKpiCard("Lô sắp/đã hết hạn", lblKhoHetHan, makeKpiSub("cần xử lý"),
                "CANCEL","#FFEBEE", new String[]{"Mã Lô","Sản phẩm","Kho","SL Tồn","Ngày hết hạn"}, () -> {
                    java.util.List<Object[]> list = busThongKe.getSpSapHetHan();
                    return list;
                }));
        root.add(cards);
        root.add(Box.createVerticalStrut(12));

        // Biểu đồ nhập hàng & tồn kho theo kho
        JPanel row2 = new JPanel(new GridLayout(1, 2, 12, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        chartNhapHang  = new BarChartNhapHang();
        chartTonKhoKho = new BarChartTonKho();
        row2.add(wrapChart("Xu hướng nhập hàng 12 tháng","Triệu đồng · "+currentYear, chartNhapHang, 240));
        row2.add(wrapChart("Phân bổ tồn kho theo kho","Giá trị (triệu đồng)", chartTonKhoKho, 240));
        root.add(row2);
        root.add(Box.createVerticalStrut(12));

        // Top tồn kho
        JPanel topC = card14();
        JLabel topT = new JLabel(" Top sản phẩm tồn kho nhiều nhất", new MenuIcon("TROPHY"), SwingConstants.LEFT);
        topT.setFont(new Font("Segoe UI", Font.BOLD, 13));
        topT.setForeground(Color.decode("#152A4B"));
        topC.add(topT, BorderLayout.NORTH);
        modelTonKho = new DefaultTableModel(new String[]{"#","Sản phẩm","Số lượng tồn","Giá trị (tr.đ)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tTon = mkTable(modelTonKho);
        tTon.getColumnModel().getColumn(2).setCellRenderer(new ShiftRenderer(Color.decode("#00A76F")));
        tTon.getColumnModel().getColumn(3).setCellRenderer(new DTRenderer());
        JScrollPane ssTon = mkSP(tTon);
        attachDynamicRows(tTon, ssTon);
        topC.add(ssTon, BorderLayout.CENTER);
        root.add(topC);
        root.add(Box.createVerticalStrut(12));

        // ─ CẢNH BÁO + GỢI Ý KM (điểm mới) ─
        JPanel row3 = new JPanel(new GridLayout(1, 2, 12, 0));
        row3.setOpaque(false);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        row3.add(buildSpSapHetHanTable());  // bảng cảnh báo hết hạn
        row3.add(buildGoiYKMPanel());       // gợi ý KM thông minh
        root.add(row3);
        root.add(Box.createVerticalStrut(12));

        JScrollPane sp = makeScrollPane(root);
        JPanel w = new JPanel(new BorderLayout());
        w.setOpaque(false);
        w.add(sp, BorderLayout.CENTER);
        return w;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PHÂN QUYỀN
    // ─────────────────────────────────────────────────────────────────────────
    private void showAccessDenied() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(Color.decode("#F4F6F8"));
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#FFCDD2"), 2),
                new EmptyBorder(40, 60, 40, 60)));
        JLabel lblIcon  = new JLabel(); lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        lblIcon.setIcon(MenuIcon.of("LOCK", 48)); lblIcon.setAlignmentX(CENTER_ALIGNMENT);
        JLabel lblTitle = new JLabel("Không có quyền truy cập", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.decode("#C62828")); lblTitle.setAlignmentX(CENTER_ALIGNMENT);
        JLabel lblDesc  = new JLabel("<html><center>Chức năng Thống kê chỉ dành cho <b>Quản lý</b>.<br>Vui lòng liên hệ quản lý để được hỗ trợ.</center></html>", SwingConstants.CENTER);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDesc.setForeground(Color.decode("#637381")); lblDesc.setAlignmentX(CENTER_ALIGNMENT);
        card.add(lblIcon); card.add(Box.createRigidArea(new Dimension(0,16)));
        card.add(lblTitle); card.add(Box.createRigidArea(new Dimension(0,10))); card.add(lblDesc);
        pnl.add(card);
        add(pnl, BorderLayout.CENTER);
    }

    // ===== LỚP BIỂU ĐỒ BÊN TRONG =====
    class BarChartMain extends JPanel {
        // hoverGroupIdx = group (tháng/ngày) đang hover; hoverBarSeries = series (0=DT,1=CP,2=LN)
        private int hoverGroupIdx = -1;
        private int hoverBarSeries = -1;
        private Point tooltipPt = null;

        BarChartMain() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(0, 260));
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    hoverGroupIdx = -1;
                    hoverBarSeries = -1;
                    tooltipPt = null;
                    int dataLen = DT_DATA.length;
                    if (dataLen == 0) { repaint(); return; }

                    int startX = 50, maxH = getHeight() - 60;
                    int availW = getWidth() - startX - 10;
                    int idealGroupW = Math.max(10, availW / Math.max(dataLen, 12));
                    int groupW = Math.min(80, idealGroupW);
                    int gap = 2;
                    int barW = Math.max(4, Math.min(22, (groupW - gap * 2 - 4) / 3));
                    int baseY = getHeight() - 35;
                    int totalBarsW = dataLen * groupW;
                    int chartOffsetX = startX + Math.max(0, (availW - totalBarsW) / 2);

                    double maxVal = calcMaxVal();
                    outer:
                    for (int i = 0; i < dataLen; i++) {
                        int offset = (groupW - (barW * 3 + gap * 2)) / 2;
                        int gx = chartOffsetX + i * groupW + offset;
                        double[] vals = { DT_DATA[i], CP_DATA[i], Math.max(0, LN_DATA[i]) };
                        for (int b = 0; b < 3; b++) {
                            int bx = gx + b * (barW + gap);
                            int bh = maxVal > 0 ? (int) (vals[b] / maxVal * maxH) : 0;
                            bh = Math.max(2, bh);
                            int by = baseY - bh;
                            if (e.getX() >= bx && e.getX() <= bx + barW && e.getY() >= by && e.getY() <= by + bh) {
                                hoverGroupIdx = i;
                                hoverBarSeries = b;
                                tooltipPt = e.getPoint();
                                break outer;
                            }
                        }
                    }
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hoverGroupIdx = -1;
                    hoverBarSeries = -1;
                    tooltipPt = null;
                    repaint();
                }
            });
        }

        private double calcMaxVal() {
            double max = 0.0;
            for (double v : DT_DATA) if (v > max) max = v;
            for (double v : CP_DATA) if (v > max) max = v;
            for (double v : LN_DATA) if (v > max) max = v;
            if (max <= 0) max = 1.0; // tránh chia 0 khi chưa có dữ liệu
            return max * 1.25;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int startX = 50, baseY = h - 35, maxH = h - 60;

            int dataLen = DT_DATA.length;
            if (dataLen == 0) return;

            int availW = w - startX - 10;
            int idealGroupW = Math.max(10, availW / Math.max(dataLen, 12));
            int groupW = Math.min(80, idealGroupW);
            int gap = 2;
            int barW = Math.max(4, Math.min(22, (groupW - gap * 2 - 4) / 3));
            int totalBarsW = dataLen * groupW;
            int chartOffsetX = startX + Math.max(0, (availW - totalBarsW) / 2);

            double maxVal = calcMaxVal();
            Color[] barColors = { Color.decode("#1A73E8"), Color.decode("#FFAB00"), Color.decode("#00A76F") };

            // Gridlines + Y-axis labels
            g2.setStroke(new BasicStroke(0.5f));
            for (double v = 0; v <= maxVal; v += Math.max(1.0, maxVal / 5.0)) {
                int y = baseY - (int) (v / maxVal * maxH);
                g2.setColor(Color.decode("#F0F0F0"));
                g2.drawLine(startX, y, w - 10, y);
                g2.setColor(Color.decode("#AAAAAA"));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String axisLabel = formatM(v).replace(" đ", "").replace("đ", "");
                g2.drawString(axisLabel, 4, y + 4);
            }

            // Bars
            for (int i = 0; i < dataLen; i++) {
                int offset = (groupW - (barW * 3 + gap * 2)) / 2;
                int gx = chartOffsetX + i * groupW + offset;
                double[] vals = { DT_DATA[i], CP_DATA[i], Math.max(0, LN_DATA[i]) };

                for (int b = 0; b < 3; b++) {
                    int rawBh = maxVal > 0 ? (int) (vals[b] / maxVal * maxH) : 0;
                    int bh = (vals[b] > 0) ? Math.max(1, rawBh) : 0;
                    int bx = gx + b * (barW + gap);
                    int by = baseY - bh;
                    Color c = barColors[b];

                    // Highlight: làm sáng đúng cột (group + series) đang hover
                    boolean isHovered = (i == hoverGroupIdx && b == hoverBarSeries);
                    if (isHovered) {
                        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
                        c = Color.getHSBColor(hsb[0], hsb[1], Math.min(1f, hsb[2] * 1.25f));
                    }

                    if (bh == 0 && !isHovered) {
                        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 60));
                    } else {
                        g2.setColor(c);
                    }

                    int radius = Math.min(8, barW);
                    g2.fillRoundRect(bx, by, barW, bh, radius, radius);
                    if (bh > 4) g2.fillRect(bx, by + 4, barW, bh - 4);
                }

                // X-axis labels (dùng CHART_LABELS thay vì THANG cố định)
                if (groupW > 16 && i < CHART_LABELS.length) {
                    g2.setColor(Color.decode("#888888"));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    String lbl = CHART_LABELS[i];
                    int lblW = g2.getFontMetrics().stringWidth(lbl);
                    g2.drawString(lbl, chartOffsetX + i * groupW + (groupW - lblW) / 2, h - 16);
                }
            }

            // Legend
            String[] legends = { "Doanh thu", "Chi phí", "Lợi nhuận" };
            int lx = startX;
            for (int b = 0; b < 3; b++) {
                g2.setColor(barColors[b]);
                g2.fillRoundRect(lx, 8, 12, 12, 3, 3);
                g2.setColor(Color.decode("#555555"));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g2.drawString(legends[b], lx + 18, 18);
                lx += 90;
            }

            // Tooltip: chỉ hiện cho đúng cột (series) đang hover
            if (hoverGroupIdx >= 0 && hoverBarSeries >= 0 && tooltipPt != null
                    && hoverGroupIdx < DT_DATA.length) {
                int i = hoverGroupIdx;
                String xLabel = i < CHART_LABELS.length ? CHART_LABELS[i] : String.valueOf(i + 1);
                String[] seriesNames = { "Doanh thu", "Chi phí", "Lợi nhuận" };
                double[] seriesVals = { DT_DATA[i], CP_DATA[i], LN_DATA[i] };
                String seriesName  = seriesNames[hoverBarSeries];
                String seriesValue = formatM(seriesVals[hoverBarSeries]);
                // Hiển thị thêm 2 series còn lại nhỏ hơn ở dòng thứ 2 để tiện so sánh
                String otherInfo = String.format("  DT %s  |  CP %s  |  LN %s",
                        formatM(DT_DATA[i]), formatM(CP_DATA[i]), formatM(LN_DATA[i]));
                String mainLine = String.format("  %s  ·  %s:  %s  ", xLabel, seriesName, seriesValue);
                drawTooltip(g2, Math.min(tooltipPt.x + 10, w - 260), tooltipPt.y - 30, mainLine);
            }
        }
    }

    class LineChartDaily extends JPanel {
        private final double[] vals;
        private final String[] dates;
        private int hoverIdx = -1;
        private Point tooltipPt = null;

        LineChartDaily() {
            setBackground(Color.WHITE);
            vals = new double[30];
            dates = new String[30];
            java.util.Arrays.fill(dates, "");

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int n = vals.length;
                    float step = (float) (getWidth() - 40) / (n - 1);
                    hoverIdx = -1;
                    tooltipPt = null;

                    double maxVal = 1.0;
                    for (double v : vals)
                        if (v > maxVal)
                            maxVal = v;
                    maxVal *= 1.2;

                    for (int i = 0; i < n; i++) {
                        int px = 20 + (int) (i * step);
                        int py = getHeight() - 20 - (int) ((vals[i] / maxVal) * (getHeight() - 30));
                        if (Math.abs(e.getX() - px) < 10 && Math.abs(e.getY() - py) < 10) {
                            hoverIdx = i;
                            tooltipPt = e.getPoint();
                            break;
                        }
                    }
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hoverIdx = -1;
                    repaint();
                }
            });
        }

        void setData(double[] newVals, String[] newDates) {
            java.util.Arrays.fill(vals, 0);
            java.util.Arrays.fill(dates, "");
            for (int i = 0; i < Math.min(newVals.length, vals.length); i++)
                vals[i] = newVals[i];
            for (int i = 0; i < Math.min(newDates.length, dates.length); i++)
                dates[i] = newDates[i];
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int n = vals.length, w = getWidth(), h = getHeight();
            float step = (float) (w - 40) / (n - 1);

            double maxVal = 1.0;
            for (double v : vals)
                if (v > maxVal)
                    maxVal = v;
            maxVal *= 1.2;

            GeneralPath area = new GeneralPath();
            area.moveTo(20, h - 20);
            for (int i = 0; i < n; i++) {
                int px = 20 + (int) (i * step);
                int py = h - 20 - (int) ((vals[i] / maxVal) * (h - 30));
                area.lineTo(px, py);
            }
            area.lineTo(20 + (int) ((n - 1) * step), h - 20);
            area.closePath();
            g2.setColor(new Color(26, 115, 232, 25));
            g2.fill(area);

            g2.setColor(Color.decode("#1A73E8"));
            g2.setStroke(new BasicStroke(2f));
            int[] xs = new int[n], ys = new int[n];
            for (int i = 0; i < n; i++) {
                xs[i] = 20 + (int) (i * step);
                ys[i] = h - 20 - (int) ((vals[i] / maxVal) * (h - 30));
            }
            for (int i = 0; i < n - 1; i++)
                g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
            for (int i = 0; i < n; i++) {
                if (i == hoverIdx) {
                    g2.setColor(Color.decode("#1A73E8"));
                    g2.fillOval(xs[i] - 5, ys[i] - 5, 10, 10);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(xs[i] - 3, ys[i] - 3, 6, 6);
                } else {
                    g2.setColor(Color.decode("#1A73E8"));
                    g2.fillOval(xs[i] - 2, ys[i] - 2, 5, 5);
                }
            }

            g2.setColor(Color.decode("#AAAAAA"));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int li : new int[] { 0, 5, 10, 15, 20, 25, 29 }) {
                String dLabel = (dates[li] != null && dates[li].length() >= 5) ? dates[li].substring(0, 5) : "";
                g2.drawString(dLabel, xs[li] - 12, h - 4);
            }

            if (hoverIdx >= 0 && tooltipPt != null) {
                String dateStr = (dates[hoverIdx] != null && dates[hoverIdx].length() >= 5)
                        ? dates[hoverIdx].substring(0, 5)
                        : "";
                drawTooltip(g2, Math.min(tooltipPt.x + 8, w - 160), tooltipPt.y - 28,
                        String.format("Ngày %s: %s", dateStr, formatM(vals[hoverIdx])));
            }
        }
    }

    class DonutChart extends JPanel {
        private int hoverIdx = -1;
        private Point tooltipPt = null;

        DonutChart() {
            setBackground(Color.WHITE);
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    hoverIdx = -1;
                    tooltipPt = null;
                    int w = getWidth(), h = getHeight();
                    int legendSpace = 40;
                    int size = Math.min(w, h - legendSpace) - 20;
                    if (size < 10)
                        return;

                    double cx = w / 2.0, cy = (h - legendSpace) / 2.0;
                    double dist = Math.hypot(e.getX() - cx, e.getY() - cy);
                    double r = size / 2.0;
                    double inner = r * 0.55;

                    if (dist >= inner && dist <= r) {
                        double angle = Math.toDegrees(Math.atan2(-(e.getY() - cy), e.getX() - cx));
                        if (angle < 0)
                            angle += 360;
                        double cur = 0, total = 0;
                        for (int v : DONUT_VALS)
                            total += v;
                        for (int i = 0; i < DONUT_VALS.length; i++) {
                            double sweep = DONUT_VALS[i] / total * 360;
                            if (angle >= cur && angle < cur + sweep) {
                                hoverIdx = i;
                                tooltipPt = e.getPoint();
                                break;
                            }
                            cur += sweep;
                        }
                    }
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hoverIdx = -1;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int legendSpace = 40;
            int size = Math.min(w, h - legendSpace) - 20;
            if (size < 10)
                return;

            int x = (w - size) / 2;
            int y = (h - legendSpace - size) / 2;

            double total = 0;
            for (int v : DONUT_VALS)
                total += v;
            int startAngle = 0;
            double radius = size / 2.0;
            double labelRadius = radius * 0.65;

            for (int i = 0; i < DONUT_VALS.length; i++) {
                int sweep = (int) Math.round(DONUT_VALS[i] / total * 360);
                Color c = DONUT_COLORS[i];
                if (i == hoverIdx) {
                    float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
                    c = Color.getHSBColor(hsb[0], hsb[1], Math.min(1f, hsb[2] * 1.15f));
                    g2.setColor(c);
                    g2.fillArc(x - 5, y - 5, size + 10, size + 10, startAngle, sweep);
                } else {
                    g2.setColor(c);
                    g2.fillArc(x, y, size, size, startAngle, sweep);
                }
                double mid = Math.toRadians(startAngle + sweep / 2.0);
                int lx = (int) (x + radius + labelRadius * Math.cos(mid));
                int ly = (int) (y + radius - labelRadius * Math.sin(mid));

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                int pct = (int) Math.round(DONUT_VALS[i] / total * 100);
                g2.drawString(pct + "%", lx - 10, ly + 4);
                startAngle += sweep;
            }

            g2.setColor(Color.WHITE);
            int holeSize = (int) (size * 0.55);
            int holeX = x + (size - holeSize) / 2;
            int holeY = y + (size - holeSize) / 2;
            g2.fillOval(holeX, holeY, holeSize, holeSize);

            int ly2 = h - legendSpace + 10;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            int lx2 = Math.max(10, (w - (4 * 70)) / 2);
            for (int i = 0; i < 4; i++) {
                if (lx2 + 70 > w) {
                    lx2 = 10;
                    ly2 += 16;
                }
                g2.setColor(DONUT_COLORS[i]);
                g2.fillRoundRect(lx2, ly2, 10, 10, 3, 3);
                g2.setColor(Color.decode("#555555"));
                g2.drawString(DONUT_LABELS[i], lx2 + 14, ly2 + 10);
                lx2 += 80;
            }

            if (hoverIdx >= 0 && tooltipPt != null) {
                drawTooltip(g2, Math.min(tooltipPt.x + 8, w - 100), tooltipPt.y - 28,
                        DONUT_LABELS[hoverIdx] + ": " + (int) Math.round(DONUT_VALS[hoverIdx] / total * 100) + "%");
            }
        }
    }

    class NVDailyChart extends JPanel {
        private int filterIdx = -1;
        private int hoverDay = -1;
        private Point tooltipPt = null;

        NVDailyChart() {
            setBackground(Color.WHITE);
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    hoverDay = -1;
                    tooltipPt = null;
                    int n = DATES_10.length;
                    if (n == 0)
                        return;
                    int startX = 45, groupW = (getWidth() - startX - 10) / n;
                    for (int d = 0; d < n; d++) {
                        if (e.getX() >= startX + d * groupW && e.getX() < startX + (d + 1) * groupW) {
                            hoverDay = d;
                            tooltipPt = e.getPoint();
                            break;
                        }
                    }
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hoverDay = -1;
                    repaint();
                }
            });
        }

        void setFilter(int idx) {
            this.filterIdx = idx;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int n = DATES_10.length;
            if (n == 0) {
                drawEmpty(g2, w, h);
                return;
            }
            int startX = 45, baseY = h - 30, maxH = h - 45;
            int groupW = (w - startX - 10) / n;
            int maxVal = 16;
            for (int i = 0; i < n; i++)
                for (int j = 0; j < NV_DAILY.length; j++)
                    if (j < NV_DAILY.length && i < NV_DAILY[j].length && NV_DAILY[j][i] > maxVal)
                        maxVal = NV_DAILY[j][i];
            maxVal = (int) (maxVal * 1.2) + 1;

            g2.setStroke(new BasicStroke(0.5f));
            for (int v = 0; v <= maxVal; v += Math.max(1, maxVal / 4)) {
                int y = baseY - (int) ((double) v / maxVal * maxH);
                g2.setColor(Color.decode("#F0F0F0"));
                g2.drawLine(startX, y, w - 10, y);
                g2.setColor(Color.decode("#AAAAAA"));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(String.valueOf(v), 4, y + 3);
            }
            g2.setColor(Color.decode("#AAAAAA"));
            g2.drawString("HĐ", 4, 15);

            int nvCount = (filterIdx >= 0) ? 1 : NV_NAMES.length;
            int barW = Math.max(5, (groupW - 8) / Math.max(1, nvCount));

            for (int d = 0; d < n; d++) {
                int gx = startX + d * groupW + 3;
                boolean isHover = (d == hoverDay);
                int bIdx = 0;
                for (int nv = 0; nv < NV_NAMES.length; nv++) {
                    if (filterIdx >= 0 && nv != filterIdx)
                        continue;
                    if (nv >= NV_DAILY.length || d >= NV_DAILY[nv].length) {
                        bIdx++;
                        continue;
                    }
                    int val = NV_DAILY[nv][d];
                    int bh = (int) ((double) val / maxVal * maxH);
                    if (bh <= 0) {
                        bIdx++;
                        continue;
                    }
                    int bx = gx + bIdx * barW;
                    Color c = NV_COLORS[nv];
                    if (isHover) {
                        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
                        c = Color.getHSBColor(hsb[0], hsb[1], Math.min(1f, hsb[2] * 1.2f));
                    }
                    g2.setColor(c);
                    g2.fillRoundRect(bx, baseY - bh, barW - 1, bh, 3, 3);
                    bIdx++;
                }
                g2.setColor(Color.decode("#888888"));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                String dateLabel = DATES_10[d].length() > 5 ? DATES_10[d].substring(0, 5) : DATES_10[d];
                g2.drawString(dateLabel, gx, h - 12);
            }

            if (hoverDay >= 0 && hoverDay < n && tooltipPt != null) {
                int d = hoverDay;
                StringBuilder sb = new StringBuilder("Ngày " + DATES_10[d] + ": ");
                if (filterIdx >= 0 && filterIdx < NV_SHORT.length) {
                    sb.append(NV_SHORT[filterIdx]).append("=")
                            .append(d < NV_DAILY[filterIdx].length ? NV_DAILY[filterIdx][d] : 0).append(" HĐ");
                } else {
                    for (int nv = 0; nv < NV_NAMES.length; nv++) {
                        if (nv > 0)
                            sb.append("  ");
                        sb.append(nv < NV_SHORT.length ? NV_SHORT[nv] : "NV" + nv).append("=")
                                .append(nv < NV_DAILY.length && d < NV_DAILY[nv].length ? NV_DAILY[nv][d] : 0);
                    }
                }
                drawTooltip(g2, Math.min(tooltipPt.x + 8, w - 180), tooltipPt.y - 28, sb.toString());
            }
        }

        private void drawEmpty(Graphics2D g2, int w, int h) {
            g2.setColor(Color.decode("#CCCCCC"));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            String s = "Đang tải dữ liệu...";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(s, (w - fm.stringWidth(s)) / 2, h / 2);
        }
    }

    class NVShiftChart extends JPanel {
        private int filterIdx = -1;
        private int caFilter = 0; // 0=all,1=sáng,2=chiều,3=tối
        private int hoverIdx = -1;
        private Point tooltipPt = null;

        NVShiftChart() {
            setBackground(Color.WHITE);
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    hoverIdx = -1;
                    tooltipPt = null;
                    int n = (filterIdx >= 0) ? 1 : NV_NAMES.length;
                    if (n == 0)
                        return;
                    int startX = 30, groupW = (getWidth() - startX - 10) / n;
                    for (int i = 0; i < n; i++) {
                        if (e.getX() >= startX + i * groupW && e.getX() < startX + (i + 1) * groupW) {
                            hoverIdx = i;
                            tooltipPt = e.getPoint();
                            break;
                        }
                    }
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hoverIdx = -1;
                    repaint();
                }
            });
        }

        void setFilter(int idx) {
            this.filterIdx = idx;
            this.hoverIdx = -1;
            this.tooltipPt = null;
            repaint();
        }

        void setCaFilter(int ca) {
            this.caFilter = ca;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int[] nvIdxs = buildNVIdxs();
            int n = nvIdxs.length;
            if (n == 0)
                return;
            int maxVal = 10;
            for (int nv : nvIdxs) {
                if (nv < NV_HD_S.length && NV_HD_S[nv] > maxVal) maxVal = NV_HD_S[nv];
                if (nv < NV_HD_C.length && NV_HD_C[nv] > maxVal) maxVal = NV_HD_C[nv];
                if (nv < NV_HD_T.length && NV_HD_T[nv] > maxVal) maxVal = NV_HD_T[nv];
            }
            maxVal = (int) (maxVal * 1.2) + 1;
            int startX = 30, baseY = h - 28, maxH = h - 40;
            int groupW = (w - startX - 10) / n, barW = Math.max(6, (groupW - 8) / 3);

            for (int v = 0; v <= maxVal; v += Math.max(1, maxVal / 4)) {
                int y = baseY - (int) ((double) v / maxVal * maxH);
                g2.setColor(Color.decode("#F0F0F0"));
                g2.setStroke(new BasicStroke(0.5f));
                g2.drawLine(startX, y, w - 5, y);
                g2.setColor(Color.decode("#AAAAAA"));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(String.valueOf(v), 2, y + 3);
            }

            for (int i = 0; i < n; i++) {
                int nv = nvIdxs[i];
                int gx = startX + i * groupW + 2;
                boolean hov = (i == hoverIdx);
                int shS = nv < NV_HD_S.length ? NV_HD_S[nv] : 0;
                int shC = nv < NV_HD_C.length ? NV_HD_C[nv] : 0;
                int shT = nv < NV_HD_T.length ? NV_HD_T[nv] : 0;
                int hS = (int) ((double) shS / maxVal * maxH), hC = (int) ((double) shC / maxVal * maxH),
                        hT = (int) ((double) shT / maxVal * maxH);
                Color cS = hov ? Color.decode("#FFD54F") : Color.decode("#FFAB00");
                Color cC = hov ? Color.decode("#7986CB") : Color.decode("#4F46E5");
                Color cT = hov ? Color.decode("#C4B5FD") : Color.decode("#8B5CF6");
                
                // ✅ PỘT FIX: Hiển thị đúng cột theo caFilter
                if (caFilter == 0 || caFilter == 1) { 
                    if (hS > 0) {
                        g2.setColor(cS);
                        g2.fillRoundRect(gx, baseY - hS, barW, hS, 3, 3);
                    }
                }
                if (caFilter == 0 || caFilter == 2) { 
                    int targetX = (caFilter == 0) ? gx + barW + 2 : gx;
                    if (hC > 0) {
                        g2.setColor(cC);
                        g2.fillRoundRect(targetX, baseY - hC, barW, hC, 3, 3);
                    }
                }
                if (caFilter == 0 || caFilter == 3) { 
                    int targetX = (caFilter == 0) ? gx + (barW + 2) * 2 : gx;
                    if (hT > 0) {
                        g2.setColor(cT);
                        g2.fillRoundRect(targetX, baseY - hT, barW, hT, 3, 3);
                    }
                }
                
                g2.setColor(Color.decode("#888888"));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(nv < NV_SHORT.length ? NV_SHORT[nv] : "NV" + (nv + 1), gx + 2, h - 12);
            }

            if (hoverIdx >= 0 && hoverIdx < nvIdxs.length && tooltipPt != null) {
                int nv = nvIdxs[hoverIdx];
                String txt = String.format("%s  Sáng:%d | Chiều:%d | Tối:%d HĐ",
                        nv < NV_NAMES.length ? NV_NAMES[nv] : "NV" + (nv + 1),
                        nv < NV_HD_S.length ? NV_HD_S[nv] : 0,
                        nv < NV_HD_C.length ? NV_HD_C[nv] : 0,
                        nv < NV_HD_T.length ? NV_HD_T[nv] : 0);
                drawTooltip(g2, Math.min(tooltipPt.x + 8, w - 260), tooltipPt.y - 28, txt);
            }
        }

        private int[] buildNVIdxs() {
            if (filterIdx >= 0)
                return new int[] { filterIdx };
            int[] arr = new int[NV_NAMES.length];
            for (int i = 0; i < arr.length; i++)
                arr[i] = i;
            return arr;
        }
    }
    class NVDetailTable extends JPanel {
        private int filterIdx = -1;
        private int caFilterIdx = 0; 
        private String filterText = "";
        private JTable tbl;
        private DefaultTableModel model;
        private JScrollPane detailSp;
        /** Ánh xạ index dòng → maNV (không tính dòng TỔNG cuối). Dùng cho double-click. */
        private final java.util.List<String> visibleMaNVList = new java.util.ArrayList<>();

        NVDetailTable() {
            setLayout(new BorderLayout(0, 6));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                    new EmptyBorder(14, 14, 10, 14)));

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            JLabel t1 = new JLabel(" Chi tiết dược sĩ theo ca", new MenuIcon("CLOCK"), SwingConstants.LEFT);
            t1.setFont(new Font("Segoe UI", Font.BOLD, 13));
            t1.setForeground(Color.decode("#152A4B"));

            JTextField search = new JTextField();
            search.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            search.setPreferredSize(new Dimension(160, 28));
            search.setForeground(Color.GRAY);
            search.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                    BorderFactory.createEmptyBorder(0, 8, 0, 8)));

            final String PLACEHOLDER = "Tìm nhân viên...";
            search.setText(PLACEHOLDER);
            search.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) {
                    if (search.getText().equals(PLACEHOLDER)) {
                        search.setText(""); search.setForeground(Color.decode("#333333"));
                    }
                }
                @Override public void focusLost(FocusEvent e) {
                    if (search.getText().trim().isEmpty()) {
                        search.setText(PLACEHOLDER); search.setForeground(Color.GRAY);
                        filterText = ""; refreshData();
                    }
                }
            });
            search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { applySearch(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { applySearch(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { applySearch(); }
                private void applySearch() {
                    String t = search.getText();
                    filterText = t.equals(PLACEHOLDER) ? "" : t.trim().toLowerCase();
                    refreshData();
                }
            });

            header.add(t1, BorderLayout.WEST);
            header.add(search, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);

            String[] cols = { "Nhân viên", "Ca sáng", "Ca chiều", "Ca tối", "Hoàn trả", "T.Đơn (Thực)", "T.DT (Thực)" };
            model = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            tbl = new JTable(model) {
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                    Component c = super.prepareRenderer(renderer, row, col);
                    if (row == getRowCount() - 1) {
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                        c.setBackground(Color.decode("#F0F4FF"));
                        c.setForeground(Color.decode("#152A4B"));
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : Color.decode("#FAFBFF"));
                        c.setForeground(Color.decode("#333333"));
                    }
                    return c;
                }
            };
            tbl.setRowHeight(34);
            tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
            tbl.getTableHeader().setBackground(Color.decode("#EEF2FF"));
            tbl.getTableHeader().setForeground(Color.decode("#555555"));
            tbl.setShowHorizontalLines(true);
            tbl.setGridColor(Color.decode("#F0F0F0"));
            tbl.setSelectionBackground(Color.decode("#F0F4FF"));
            tbl.setFillsViewportHeight(true);

            DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
            centerR.setHorizontalAlignment(SwingConstants.CENTER);
            for (int i = 1; i <= 5; i++) tbl.getColumnModel().getColumn(i).setCellRenderer(centerR);
            tbl.getColumnModel().getColumn(6).setCellRenderer(new DTRenderer());

            refreshData();
            detailSp = new JScrollPane(tbl);
            detailSp.setBorder(BorderFactory.createEmptyBorder());
            detailSp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
            attachDynamicRows(tbl, detailSp);

            // ── Double-click: xem lại Bill Kết Ca gần nhất của nhân viên ──────
            tbl.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() < 2) return;
                    int row = tbl.rowAtPoint(e.getPoint());
                    // Bỏ qua dòng TỔNG (dòng cuối) và dòng ngoài phạm vi
                    if (row < 0 || row >= visibleMaNVList.size()) return;

                    String maNV = visibleMaNVList.get(row);
                    int nvIdx = findNVIdx(maNV);
                    String tenNV = (nvIdx >= 0 && nvIdx < NV_NAMES.length) ? NV_NAMES[nvIdx] : maNV;

                    // Lấy ca đã đóng gần nhất từ DB
                    CaLamViec ca = busCaLamViec.getCaDaKetThucGanNhat(maNV);
                    if (ca == null) {
                        JOptionPane.showMessageDialog(
                            SwingUtilities.getWindowAncestor(ManHinhThongKe.this),
                            "Nhân viên này chưa có ca làm việc nào đã kết thúc.",
                            "Không tìm thấy ca", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(
                            ManHinhThongKe.this);
                    ManHinhChinh.hienThiBillKetCaLichSu(parentFrame, ca, tenNV);
                }
            });

            add(detailSp, BorderLayout.CENTER);
        }

        /** Tìm index trong NV_IDS theo maNV. */
        private int findNVIdx(String maNV) {
            for (int i = 0; i < NV_IDS.length; i++)
                if (NV_IDS[i].equals(maNV)) return i;
            return 0;
        }

        void setFilter(int idx) {
            this.filterIdx = idx;
            refreshData();
        }

        void setCaFilter(int ca) {
            this.caFilterIdx = ca;
            refreshData();
        }

        void refreshData() {
            model.setRowCount(0);
            visibleMaNVList.clear();   // ← reset mapping trước mỗi lần refresh
            double totalDTS = 0, totalDTC = 0, totalDTT = 0, totalDTTra = 0;
            int totalHDS = 0, totalHDC = 0, totalHDT = 0, totalHDTra = 0;

            for (int i = 0; i < NV_NAMES.length; i++) {
                if (filterIdx >= 0 && i != filterIdx) continue;
                if (!filterText.isEmpty() && !NV_NAMES[i].toLowerCase().contains(filterText)) continue;

                int hdS   = i < NV_HD_S.length   ? NV_HD_S[i]   : 0;
                double dtS = i < NV_DT_S.length  ? NV_DT_S[i]   : 0;
                int hdC   = i < NV_HD_C.length   ? NV_HD_C[i]   : 0;
                double dtC = i < NV_DT_C.length  ? NV_DT_C[i]   : 0;
                int hdT   = i < NV_HD_T.length   ? NV_HD_T[i]   : 0;
                double dtT = i < NV_DT_T.length  ? NV_DT_T[i]   : 0;
                int hdTra  = (NV_HD_TRA != null && i < NV_HD_TRA.length) ? NV_HD_TRA[i] : 0;
                double dtTra = (NV_DT_TRA != null && i < NV_DT_TRA.length) ? NV_DT_TRA[i] : 0;

                // Xác định nội dung từng cột Ca và giá trị net theo caFilterIdx
                String colS, colC, colT;
                int netHD;
                double netDT;

                if (caFilterIdx == 1) {         // Ca Sáng
                    colS  = hdS + " đơn · " + formatM(dtS);
                    colC  = "—";
                    colT  = "—";
                    netHD = hdS - hdTra;
                    netDT = dtS - dtTra;
                    totalHDS += hdS; totalDTS += dtS;
                } else if (caFilterIdx == 2) {  // Ca Chiều
                    colS  = "—";
                    colC  = hdC + " đơn · " + formatM(dtC);
                    colT  = "—";
                    netHD = hdC - hdTra;
                    netDT = dtC - dtTra;
                    totalHDC += hdC; totalDTC += dtC;
                } else if (caFilterIdx == 3) {  // Ca Tối
                    colS  = "—";
                    colC  = "—";
                    colT  = hdT + " đơn · " + formatM(dtT);
                    netHD = hdT - hdTra;
                    netDT = dtT - dtTra;
                    totalHDT += hdT; totalDTT += dtT;
                } else {                        // Tất cả (caFilterIdx == 0)
                    colS  = hdS + " đơn · " + formatM(dtS);
                    colC  = hdC + " đơn · " + formatM(dtC);
                    colT  = hdT + " đơn · " + formatM(dtT);
                    netHD = hdS + hdC + hdT - hdTra;
                    netDT = dtS + dtC + dtT - dtTra;
                    totalHDS += hdS; totalHDC += hdC; totalHDT += hdT;
                    totalDTS += dtS; totalDTC += dtC; totalDTT += dtT;
                }
                totalHDTra += hdTra;
                totalDTTra += dtTra; // dtTra đã ở triệu

                visibleMaNVList.add(NV_IDS[i]); // ← ghi nhận maNV cho double-click
                model.addRow(new Object[] {
                        NV_NAMES[i],
                        colS,
                        colC,
                        colT,
                        "<html><font color='#D32F2F'>" + hdTra + " đơn · " + formatM(dtTra) + "</font></html>",
                        netHD,
                        formatM(netDT)
                });
            }

            // Dòng TỔNG tính đúng theo ca đang lọc
            String totColS, totColC, totColT;
            int netHDTotal;
            double netDTTotal;

            if (caFilterIdx == 1) {         // Ca Sáng
                totColS    = totalHDS + " đơn · " + formatM(totalDTS);
                totColC    = "—";
                totColT    = "—";
                netHDTotal = totalHDS - totalHDTra;
                netDTTotal = totalDTS - totalDTTra;
            } else if (caFilterIdx == 2) {  // Ca Chiều
                totColS    = "—";
                totColC    = totalHDC + " đơn · " + formatM(totalDTC);
                totColT    = "—";
                netHDTotal = totalHDC - totalHDTra;
                netDTTotal = totalDTC - totalDTTra;
            } else if (caFilterIdx == 3) {  // Ca Tối
                totColS    = "—";
                totColC    = "—";
                totColT    = totalHDT + " đơn · " + formatM(totalDTT);
                netHDTotal = totalHDT - totalHDTra;
                netDTTotal = totalDTT - totalDTTra;
            } else {                        // Tất cả (caFilterIdx == 0)
                totColS    = totalHDS + " đơn · " + formatM(totalDTS);
                totColC    = totalHDC + " đơn · " + formatM(totalDTC);
                totColT    = totalHDT + " đơn · " + formatM(totalDTT);
                netHDTotal = totalHDS + totalHDC + totalHDT - totalHDTra;
                netDTTotal = totalDTS + totalDTC + totalDTT - totalDTTra;
            }

            model.addRow(new Object[] {
                    "TỔNG:",
                    totColS,
                    totColC,
                    totColT,
                    "<html><font color='#D32F2F'>" + totalHDTra + " đơn · " + formatM(totalDTTra) + "</font></html>",
                    netHDTotal,
                    formatM(netDTTotal)
            });
        }
    }

    class HourBarChart extends JPanel {
        private double[] data = new double[24];
        private int hov = -1;
        private Point tp = null;

        HourBarChart() {
            setBackground(Color.WHITE);
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    hov = -1;
                    tp = null;
                    int sX = 40, bW = (getWidth() - sX - 10) / 24;
                    for (int i = 0; i < 24; i++)
                        if (e.getX() >= sX + i * bW && e.getX() < sX + (i + 1) * bW) {
                            hov = i;
                            tp = e.getPoint();
                            break;
                        }
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hov = -1;
                    repaint();
                }
            });
        }

        void setData(double[] d) {
            java.util.Arrays.fill(data, 0);
            for (int i = 0; i < Math.min(d.length, 24); i++)
                data[i] = d[i];
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), sX = 40, bY = h - 30, mH = h - 50;
            
            // Tìm Max cẩn thận hơn để tránh chia cho 0
            double mx = 1;
            for (double v : data) {
                if (v > mx) mx = v;
            }
            mx *= 1.2;
            
            int bW = (w - sX - 10) / 24;
            g2.setStroke(new BasicStroke(0.5f));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            
            // Vẽ đường gióng ngang (Grid lines)
            for (int s = 0; s <= 4; s++) {
                double v = mx * s / 4;
                int y = bY - (int) (v / mx * mH);
                g2.setColor(Color.decode("#F0F0F0"));
                g2.drawLine(sX, y, w - 10, y);
                g2.setColor(Color.decode("#AAAAAA"));
                g2.drawString(formatM(v).replace(" đ", ""), 4, y + 3);
            }
            
            // Mảng màu 24 phần tử cho 24 giờ
            Color[] cc = new Color[24];
            for (int i = 0; i < 24; i++) {
                if (i >= 6 && i <= 13) cc[i] = Color.decode("#FFAB00"); // Sáng: Cam
                else if (i >= 14 && i <= 21) cc[i] = Color.decode("#1A73E8"); // Chiều: Xanh
                else cc[i] = Color.decode("#8B5CF6"); // Tối: Tím
            }
            
            // Vẽ 24 cột
            for (int i = 0; i < 24; i++) {
                int bx = sX + i * bW;
                int bh = (int) (data[i] / mx * mH);
                int by = bY - bh;
                Color c = cc[i];
                
                if (i == hov) {
                    c = c.brighter(); // Sáng lên khi di chuột
                }
                
                if (bh > 0) {
                    g2.setColor(c);
                    g2.fillRoundRect(bx + 1, by, bW - 2, bh, 4, 4);
                }
                
                g2.setColor(Color.decode("#AAAAAA"));
                if (i % 3 == 0) g2.drawString(i + "h", bx + 1, h - 12);
            }
            
            // Vẽ phần chú thích (Legend) ở dưới cùng
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(Color.decode("#FFAB00"));
            g2.fillRect(sX, 8, 10, 10);
            g2.setColor(Color.decode("#555555"));
            g2.drawString("Ca sáng (6-13h)", sX + 14, 16);
            
            g2.setColor(Color.decode("#1A73E8"));
            g2.fillRect(sX + 120, 8, 10, 10);
            g2.setColor(Color.decode("#555555"));
            g2.drawString("Ca chiều (14-21h)", sX + 134, 16);
            
            g2.setColor(Color.decode("#8B5CF6"));
            g2.fillRect(sX + 260, 8, 10, 10);
            g2.setColor(Color.decode("#555555"));
            g2.drawString("Ca tối (22-5h)", sX + 274, 16);
            
            // Tooltip khi di chuột
            if (hov >= 0 && tp != null) {
                drawTooltip(g2, Math.min(tp.x + 8, w - 160), tp.y - 28,
                        String.format("%dh: %s", hov, formatM(data[hov])));
            }
        }
    }
    
    class BarChartKHMoi extends JPanel {
        private int[] data = new int[12];
        private int hov = -1;
        private Point tp = null;

        BarChartKHMoi() {
            setBackground(Color.WHITE);
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    hov = -1;
                    tp = null;
                    int sX = 45, gW = (getWidth() - sX - 10) / 12;
                    for (int i = 0; i < 12; i++)
                        if (e.getX() >= sX + i * gW && e.getX() < sX + (i + 1) * gW) {
                            hov = i;
                            tp = e.getPoint();
                            break;
                        }
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hov = -1;
                    repaint();
                }
            });
        }

        void setData(int[] d) {
            java.util.Arrays.fill(data, 0);
            for (int i = 0; i < Math.min(d.length, 12); i++)
                data[i] = d[i];
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), sX = 45, bY = h - 30, mH = h - 45;
            int mx = 1;
            for (int v : data)
                if (v > mx)
                    mx = v;
            mx = (int) (mx * 1.2) + 1;
            int bW = (w - sX - 10) / 12;
            g2.setStroke(new BasicStroke(0.5f));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            for (int s = 0; s <= 4; s++) {
                int v = mx * s / 4;
                int y = bY - (int) ((double) v / mx * mH);
                g2.setColor(Color.decode("#F0F0F0"));
                g2.drawLine(sX, y, w - 10, y);
                g2.setColor(Color.decode("#AAAAAA"));
                g2.drawString(String.valueOf(v), 4, y + 3);
            }
            for (int i = 0; i < 12; i++) {
                int bx = sX + i * bW + 2, bh = (int) ((double) data[i] / mx * mH), by = bY - bh;
                Color c = i == hov ? Color.decode("#7B1FA2") : Color.decode("#9C27B0");
                if (bh > 0) {
                    g2.setColor(c);
                    g2.fillRoundRect(bx, by, bW - 4, bh, 4, 4);
                    if (bh > 4)
                        g2.fillRect(bx, by + 4, bW - 4, bh - 4);
                }
                if (data[i] > 0 && bh > 14) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    g2.drawString(data[i] + "", bx + (bW - 4) / 2 - 4, by + bh / 2 + 3);
                }
                g2.setColor(Color.decode("#888888"));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(THANG[i], bx + (bW - 4) / 2 - 7, h - 14);
            }
            if (hov >= 0 && tp != null)
                drawTooltip(g2, Math.min(tp.x + 8, w - 150), tp.y - 28,
                        "Tháng " + (hov + 1) + ": " + data[hov] + " KH mới");
        }
    }

    class BarChartNhapHang extends JPanel {
        private double[] data = new double[12];
        private int hov = -1;
        private Point tp = null;

        BarChartNhapHang() {
            setBackground(Color.WHITE);
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    hov = -1;
                    tp = null;
                    int sX = 45, gW = (getWidth() - sX - 10) / 12;
                    for (int i = 0; i < 12; i++)
                        if (e.getX() >= sX + i * gW && e.getX() < sX + (i + 1) * gW) {
                            hov = i;
                            tp = e.getPoint();
                            break;
                        }
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hov = -1;
                    repaint();
                }
            });
        }

        void setData(double[] d) {
            java.util.Arrays.fill(data, 0);
            for (int i = 0; i < Math.min(d.length, 12); i++)
                data[i] = d[i];
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), sX = 45, bY = h - 30, mH = h - 45;
            double mx = 1;
            for (double v : data)
                if (v > mx)
                    mx = v;
            mx *= 1.2;
            int bW = (w - sX - 10) / 12;
            g2.setStroke(new BasicStroke(0.5f));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            for (int s = 0; s <= 4; s++) {
                double v = mx * s / 4;
                int y = bY - (int) (v / mx * mH);
                g2.setColor(Color.decode("#F0F0F0"));
                g2.drawLine(sX, y, w - 10, y);
                g2.setColor(Color.decode("#AAAAAA"));
                g2.drawString(formatM(v).replace(" đ", ""), 4, y + 3);
            }
            for (int i = 0; i < 12; i++) {
                int bx = sX + i * bW + 2, bh = (int) (data[i] / mx * mH), by = bY - bh;
                Color c = i == hov ? Color.decode("#0277BD") : Color.decode("#1A73E8");
                if (bh > 0) {
                    g2.setColor(c);
                    g2.fillRoundRect(bx, by, bW - 4, bh, 4, 4);
                    if (bh > 4)
                        g2.fillRect(bx, by + 4, bW - 4, bh - 4);
                }
                g2.setColor(Color.decode("#888888"));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(THANG[i], bx + (bW - 4) / 2 - 7, h - 14);
            }
            if (hov >= 0 && tp != null)
                drawTooltip(g2, Math.min(tp.x + 8, w - 150), tp.y - 28,
                        "Tháng " + (hov + 1) + ": " + formatM(data[hov]));
        }
    }

    class BarChartTonKho extends JPanel {
        private String[] labels = new String[0];
        private double[] values = new double[0];
        private int hov = -1;
        private Point tp = null;

        BarChartTonKho() {
            setBackground(Color.WHITE);
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    hov = -1;
                    tp = null;
                    if (labels.length == 0)
                        return;
                    int sX = 60, bW = (getWidth() - sX - 20) / Math.max(1, labels.length);
                    for (int i = 0; i < labels.length; i++)
                        if (e.getX() >= sX + i * bW && e.getX() < sX + (i + 1) * bW) {
                            hov = i;
                            tp = e.getPoint();
                            break;
                        }
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hov = -1;
                    repaint();
                }
            });
        }

        void setData(String[] lb, double[] vl) {
            labels = lb;
            values = vl;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), n = labels.length;
            if (n == 0) {
                g2.setColor(Color.decode("#CCCCCC"));
                g2.drawString("Đang tải...", w / 2 - 30, h / 2);
                return;
            }
            int sX = 60, bY = h - 30, mH = h - 45, bW = (w - sX - 20) / n;
            double mx = 1;
            for (double v : values)
                if (v > mx)
                    mx = v;
            mx *= 1.2;
            g2.setStroke(new BasicStroke(0.5f));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            for (int s = 0; s <= 4; s++) {
                double v = mx * s / 4;
                int y = bY - (int) (v / mx * mH);
                g2.setColor(Color.decode("#F0F0F0"));
                g2.drawLine(sX, y, w - 10, y);
                g2.setColor(Color.decode("#AAAAAA"));
                g2.drawString(formatM(v).replace(" đ", ""), 4, y + 3);
            }
            Color[] cols = { Color.decode("#00A76F"), Color.decode("#1A73E8"), Color.decode("#FF9800"),
                    Color.decode("#9C27B0") };
            for (int i = 0; i < n; i++) {
                int bx = sX + i * bW + 4, bh = (int) (values[i] / mx * mH), by = bY - bh;
                Color c = cols[i % cols.length];
                if (i == hov)
                    c = c.brighter();
                if (bh > 0) {
                    g2.setColor(c);
                    g2.fillRoundRect(bx, by, bW - 8, bh, 6, 6);
                    if (bh > 6)
                        g2.fillRect(bx, by + 6, bW - 8, bh - 6);
                }
                g2.setColor(Color.decode("#555555"));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String lb = labels[i];
                g2.drawString(lb, bx + (bW - 8) / 2 - g2.getFontMetrics().stringWidth(lb) / 2, h - 14);
            }
            if (hov >= 0 && tp != null)
                drawTooltip(g2, Math.min(tp.x + 8, w - 160), tp.y - 28, labels[hov] + ": " + formatM(values[hov]));
        }
    }

    // ===== RENDERERS =====
    static class ShiftRenderer extends DefaultTableCellRenderer {
        Color color;

        ShiftRenderer(Color c) {
            this.color = c;
            setHorizontalAlignment(CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            setForeground(color);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            return this;
        }
    }

    static class DTRenderer extends DefaultTableCellRenderer {
        DTRenderer() {
            setHorizontalAlignment(RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            setForeground(Color.decode("#00A76F"));
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            return this;
        }
    }

    // ===== RENDER TABLE VÀ LOAD DỮ LIỆU =====
    private JPanel buildTopSPTable() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(14, 14, 10, 14)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel t = new JLabel(" Top sản phẩm bán chạy", MenuIcon.of("TROPHY", 16, Color.decode("#FFAB00")), SwingConstants.LEFT);
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setForeground(Color.decode("#152A4B"));
        header.add(t, BorderLayout.WEST);
        p.add(header, BorderLayout.NORTH);

        String[] cols = { "#", "Sản phẩm", "Loại", "SL", "Doanh thu" };
        modelTopSP = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        DefaultTableModel m = modelTopSP;

        JTable tbl = new JTable(m);
        tbl.setRowHeight(34);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tbl.getTableHeader().setBackground(Color.decode("#F4F6F8"));
        tbl.getTableHeader().setPreferredSize(new Dimension(0, 36));
        tbl.setShowHorizontalLines(true);
        tbl.setGridColor(Color.decode("#F1F3F5"));
        tbl.setSelectionBackground(Color.decode("#EBF5FF"));
        tbl.setFillsViewportHeight(true);
        
        tbl.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t2, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = new JLabel(v != null ? v.toString() : "", SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String sv = v != null ? v.toString() : "";
                switch (sv) {
                    case "TPCN":
                        lbl.setBackground(Color.decode("#E8F5E9"));
                        lbl.setForeground(Color.decode("#2E7D32"));
                        break;
                    case "Thuốc KĐ":
                        lbl.setBackground(Color.decode("#FCE4EC"));
                        lbl.setForeground(Color.decode("#C62828"));
                        break;
                    case "Thuốc KKĐ":
                        lbl.setBackground(Color.decode("#E3F2FD"));
                        lbl.setForeground(Color.decode("#1565C0"));
                        break;
                    default:
                        lbl.setBackground(Color.decode("#FFF3E0"));
                        lbl.setForeground(Color.decode("#E65100"));
                        break;
                }
                return lbl;
            }
        });
        tbl.getColumnModel().getColumn(4).setCellRenderer(new DTRenderer());

        // Set column widths: # nhỏ, Sản phẩm rộng, Loại vừa, SL nhỏ, Doanh thu vừa
        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(55);
        tbl.getColumnModel().getColumn(0).setMaxWidth(70);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(260);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(90);
        tbl.getColumnModel().getColumn(2).setMaxWidth(100);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(55);
        tbl.getColumnModel().getColumn(3).setMaxWidth(70);
        tbl.getColumnModel().getColumn(4).setPreferredWidth(130);

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private void reloadTopSP(BUS.BUS_ThongKe.ThongKeFilter condHD, int year) {
        if (modelTopSP == null)
            return;
        modelTopSP.setRowCount(0);
        new Thread(() -> {
            java.util.List<Object[]> list = busThongKe.getTopSanPham(year, condHD);
            int rank = 1;
            for (Object[] item : list) {
                String ten = (String) item[0];
                String danhMuc = (String) item[1];
                int sl = (int) item[2];
                double dt = (double) item[3];
                String catLabel = danhMuc == null ? "Khác"
                        : danhMuc.equals("THUOC_KE_DON") ? "Thuốc KĐ"
                                : danhMuc.equals("THUOC_KHONG_KE_DON") ? "Thuốc KKĐ"
                                        : danhMuc.equals("THUC_PHAM_CHUC_NANG") ? "TPCN"
                                                : danhMuc.equals("MY_PHAM") ? "Mỹ phẩm" : "Khác";
                final int r2 = rank++;
                final Object[] row = {
                		r2 == 1 ? "Top 1" : r2 == 2 ? "Top 2" : r2 == 3 ? "Top 3" : String.valueOf(r2),
                        ten, catLabel, String.format("%,d", sl), formatM(dt)
                };
                SwingUtilities.invokeLater(() -> modelTopSP.addRow(row));
            }
        }).start();
    }

    private JPanel buildTaiChinhTable() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(14, 14, 10, 14)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel t = new JLabel(" Báo cáo tài chính chi tiết", MenuIcon.of("DOCUMENT", 16, Color.decode("#1A73E8")), SwingConstants.LEFT);
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setForeground(Color.decode("#152A4B"));
        lblVATTotal = new JLabel("Đang tải...");
        lblVATTotal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblVATTotal.setForeground(Color.decode("#3730A3"));
        lblVATTotal.setBackground(Color.decode("#EEF2FF"));
        lblVATTotal.setOpaque(true);
        lblVATTotal.setBorder(new EmptyBorder(3, 10, 3, 10));
        header.add(t, BorderLayout.WEST);
        header.add(lblVATTotal, BorderLayout.EAST);

        JPanel formula = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 3));
        formula.setBackground(Color.decode("#F8F9FA"));
        formula.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.decode("#E5E7EB")));
        Object[][] fParts = {
            { "DT gộp",        "#1A73E8", "#DBEAFE" },
            { " −  Hoàn trả",  "#B00020", "#FFE4E6" },
            { " =  DT sau trả","#0891B2", "#E0F2FE" },
            { " −  VAT",       "#B45309", "#FEF3C7" },
            { " =  DT thuần",  "#00875A", "#D1FAE5" },
            { " −  Giá vốn",   "#7B1FA2", "#F3E8FF" },
            { " =  LN gộp",    "#D97706", "#FFFBEB" },
        };
        for (Object[] fp : fParts) {
            JLabel fl = new JLabel((String) fp[0]);
            fl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            fl.setForeground(Color.decode((String) fp[1]));
            fl.setBackground(Color.decode((String) fp[2]));
            fl.setOpaque(true);
            fl.setBorder(new EmptyBorder(3, 7, 3, 7));
            formula.add(fl);
        }

        JPanel northWrapper = new JPanel(new BorderLayout(0, 4));
        northWrapper.setOpaque(false);
        northWrapper.add(header,  BorderLayout.NORTH);
        northWrapper.add(formula, BorderLayout.SOUTH);
        p.add(northWrapper, BorderLayout.NORTH);

        final String[] COLS    = { "Kỳ", "DT gộp", "(−) Hoàn trả", "DT sau trả",
                                   "(−) VAT", "DT thuần", "Giá vốn", "LN gộp" };
        final String[] FG_COLS = { null, "#1A73E8", "#B00020", "#0891B2",
                                   "#B45309", "#00875A", "#7B1FA2", "#D97706" };
        final String[] BG_ODD  = { null, "#EFF6FF", "#FFF1F2", "#E0F7FF",
                                   "#FFFBEB", "#ECFDF5", "#F5F3FF", "#FFFBEB" };
        final String[] BG_EVEN = { null, "#F8FAFF", "#FFF8F8", "#F0FBFF",
                                   "#FFFEF5", "#F0FDF9", "#FAF8FF", "#FFFDF5" };

        modelTaiChinh = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(modelTaiChinh);
        tbl.setRowHeight(34);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tbl.getTableHeader().setBackground(Color.decode("#F5F7FA"));
        tbl.getTableHeader().setForeground(Color.decode("#444444"));
        tbl.getTableHeader().setPreferredSize(new Dimension(0, 36));
        tbl.setShowHorizontalLines(true);
        tbl.setGridColor(Color.decode("#F0F0F0"));
        tbl.setSelectionBackground(Color.decode("#EBF4FF"));
        tbl.setFillsViewportHeight(true);

        TableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t2, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t2, v, sel, foc, row, col);
                String txt = v != null ? v.toString() : "";
                boolean isTotals = "TỔNG".equals(t2.getValueAt(row, 0));
                setBorder(new EmptyBorder(0, col == 0 ? 8 : 4, 0, col == 0 ? 4 : 8));
                setFont(new Font("Segoe UI", isTotals ? Font.BOLD : Font.PLAIN, 12));
                setHorizontalAlignment(col == 0 ? LEFT : RIGHT);
                if (!sel) {
                    if (col == 0) {
                        setBackground(row % 2 == 0 ? Color.WHITE : Color.decode("#FAFBFC"));
                        setForeground(isTotals ? Color.decode("#152A4B") : Color.decode("#555555"));
                    } else {
                        setBackground(Color.decode(row % 2 == 0 ? BG_EVEN[col] : BG_ODD[col]));
                        setForeground(Color.decode(FG_COLS[col]));
                    }
                }
                if (isTotals) {
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(2, 0, 0, 0, Color.decode("#CBD5E1")),
                        new EmptyBorder(0, col == 0 ? 8 : 4, 0, col == 0 ? 4 : 8)));
                }
                return this;
            }
        };
        for (int c = 0; c < COLS.length; c++)
            tbl.getColumnModel().getColumn(c).setCellRenderer(renderer);

        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(80);
        tbl.getColumnModel().getColumn(0).setMaxWidth(120); 
        
        for (int c = 1; c < COLS.length; c++) {
            tbl.getColumnModel().getColumn(c).setPreferredWidth(115);
        }
        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        
        p.add(sp, BorderLayout.CENTER);
        
        return p; 
    }
    // ─────────────────────────────────────────────────────────────────────────
    // RELOAD BÁO CÁO TÀI CHÍNH CHI TIẾT
    // Gọi BUS_ThongKe.getBaoCaoTaiChinh() → đổ vào modelTaiChinh
    // Mỗi dòng: Kỳ | DT gộp | (−)VAT | (−)Hoàn trả | DT thuần | Giá vốn | LN gộp
    // Dòng cuối: tổng cộng toàn kỳ
    // ─────────────────────────────────────────────────────────────────────────
    private void reloadTaiChinh(BUS.BUS_ThongKe.ThongKeFilter condHD) {
        if (modelTaiChinh == null) return;
        modelTaiChinh.setRowCount(0);
        new Thread(() -> {
            // Chọn granularity phù hợp theo kỳ lọc đang chọn
            String groupBy;
            switch (modeLocThoiGian) {
                case "HOM_NAY":
                case "TUAN":
                    groupBy = "NGAY";  break;
                case "NAM":
                    groupBy = "THANG"; break;
                case "QUY":
                    groupBy = "THANG"; break;
                default:               // THANG, TUYCHINH
                    groupBy = "NGAY";  break;
            }

            java.util.List<Object[]> list = busThongKe.getBaoCaoTaiChinh(condHD, groupBy);

            // Tích lũy tổng để dòng footer
            double sumDTG=0, sumVAT=0, sumTra=0, sumDTT=0, sumCOGS=0, sumLN=0;
            java.util.List<Object[]> rowsBuf = new ArrayList<>();

            for (Object[] item : list) {
                String ky   = String.valueOf(item[0]);
                double dtg  = toD(item[1]);   // DT gộp (bán ra)
                double vat  = toD(item[2]);   // Thuế VAT
                double tra  = toD(item[3]);   // Hoàn trả
                double dtt  = toD(item[4]);   // DT thuần (sau hoàn trả & VAT)
                double cogs = toD(item[5]);   // Giá vốn COGS
                double ln   = toD(item[6]);   // LN gộp
                double dtSauTra = dtg - tra;  // DT sau hoàn trả (trước VAT)
                sumDTG+=dtg; sumVAT+=vat; sumTra+=tra;
                sumDTT+=dtt; sumCOGS+=cogs; sumLN+=ln;
                rowsBuf.add(new Object[]{
                    ky,
                    formatK(dtg),       // DT gộp
                    formatK(tra),       // (−) Hoàn trả
                    formatK(dtSauTra),  // DT sau trả
                    formatK(vat),       // (−) VAT
                    formatK(dtt),       // DT thuần
                    formatK(cogs),      // Giá vốn
                    formatK(ln)         // LN gộp
                });
            }

            // Dòng TỔNG
            double sumDtSauTra = sumDTG - sumTra;
            final Object[] totalsRow = {
                "TỔNG",
                formatK(sumDTG), formatK(sumTra), formatK(sumDtSauTra),
                formatK(sumVAT), formatK(sumDTT), formatK(sumCOGS), formatK(sumLN)
            };

            // Badge summary cho header
            final double fVAT = sumVAT, fTra = sumTra, fDTT = sumDTT, fLN = sumLN;
            final java.util.List<Object[]> fRows = rowsBuf;

            SwingUtilities.invokeLater(() -> {
                modelTaiChinh.setRowCount(0);
                for (Object[] r : fRows) modelTaiChinh.addRow(r);
                if (!fRows.isEmpty()) modelTaiChinh.addRow(totalsRow);

                if (lblVATTotal != null)
                    lblVATTotal.setText(String.format(
                        "VAT: %s  |  Hoàn trả: %s  |  LN gộp: %s",
                        formatK(fVAT), formatK(fTra), formatK(fLN)));
            });
        }).start();
    }

    /** Helper: safely convert Object to double */
    private static double toD(Object o) {
        return (o instanceof Number) ? ((Number) o).doubleValue() : 0.0;
    }

    private JPanel buildNVDailyLegend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 2));
        p.setOpaque(false);
        for (int i = 0; i < NV_NAMES.length; i++)
            p.add(legendDot(NV_COLORS[i], NV_SHORT[i]));
        return p;
    }

    // ===== UI UTILS =====
    private JLabel makeKpiVal(String init, String color) {
        JLabel l = new JLabel(init);
        l.setFont(new Font("Segoe UI", Font.BOLD, 20));
        l.setForeground(Color.decode(color));
        return l;
    }

    private JLabel makeKpiSub(String init) {
        JLabel l = new JLabel(init);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(Color.decode("#999999"));
        return l;
    }

    private JPanel statCardDynamic(String label, JLabel valLabel, JLabel subLabel, String iconType, String iconBg) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(14, 16, 14, 16)));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Color.decode("#888888"));
        JLabel ico = new JLabel();
        ico.setHorizontalAlignment(SwingConstants.CENTER);
        ico.setIcon(MenuIcon.of(iconType, 22));
        ico.setForeground(valLabel.getForeground());
        ico.setOpaque(true);
        ico.setBackground(Color.decode(iconBg));
        ico.setPreferredSize(new Dimension(36, 36));
        top.add(lbl, BorderLayout.WEST);
        top.add(ico, BorderLayout.EAST);
        JPanel bottom = new JPanel(new GridLayout(2, 1, 0, 2));
        bottom.setOpaque(false);
        bottom.add(valLabel);
        bottom.add(subLabel);
        p.add(top, BorderLayout.NORTH);
        p.add(bottom, BorderLayout.CENTER);
        return p;
    }

    private void drawTooltip(Graphics2D g2, int x, int y, String text) {
        FontMetrics fm = g2.getFontMetrics(new Font("Segoe UI", Font.PLAIN, 11));
        int tw = fm.stringWidth(text) + 16, th = fm.getHeight() + 10;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.setColor(new Color(21, 42, 75, 220));
        g2.fillRoundRect(x, y, tw, th, 6, 6);
        g2.setColor(Color.WHITE);
        g2.drawString(text, x + 8, y + th - 7);
    }

    private JPanel wrapChart(String title, String sub, JComponent chart, int chartH) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(14, 14, 10, 14)));
        JLabel lt = new JLabel(title);
        lt.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lt.setForeground(Color.decode("#152A4B"));
        JLabel ls = new JLabel(sub);
        ls.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ls.setForeground(Color.decode("#888888"));
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(lt);
        titles.add(ls);
        chart.setPreferredSize(new Dimension(0, chartH));
        p.add(titles, BorderLayout.NORTH);
        p.add(chart, BorderLayout.CENTER);
        return p;
    }

    private JPanel miniStatInlineDynamic(String label, JLabel valLabel, String sub, String color, String bgColor) {
        JPanel p = new JPanel(new GridLayout(3, 1, 0, 1));
        p.setBackground(Color.decode(bgColor));
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#DDDDDD"), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        JLabel l1 = new JLabel(label);
        l1.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        l1.setForeground(Color.decode("#888888"));
        JLabel l3 = new JLabel(sub);
        l3.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        l3.setForeground(Color.decode("#999999"));
        p.add(l1);
        p.add(valLabel);
        p.add(l3);
        return p;
    }

    private JLabel legendDot(Color c, String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(Color.decode("#555555"));
        l.setIcon(new Icon() {
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                g.setColor(c);
                g.fillRoundRect(x, y, 10, 10, 3, 3);
            }

            public int getIconWidth() {
                return 12;
            }

            public int getIconHeight() {
                return 12;
            }
        });
        return l;
    }

    // ===== INLINE ICON HELPERS (cho những key không có trong MenuIcon) =====
    private static Icon makeSimpleIcon(String type, int size, Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                switch (type) {
                    case "PEOPLE": {
                        // Head
                        int r = size / 4;
                        g2.fillOval(x + size / 2 - r, y, r * 2, r * 2);
                        // Body (arc bán nguyệt)
                        g2.fillArc(x + 1, y + size / 2, size - 2, size - 2, 0, 180);
                        break;
                    }
                    case "HEART": {
                        // 2 vòng tròn + tam giác → hình tim
                        int hw = size / 2, hh = size * 2 / 3;
                        int r2 = hw / 2;
                        g2.fillOval(x, y + r2 / 2, hw, hw);
                        g2.fillOval(x + hw - 1, y + r2 / 2, hw, hw);
                        int[] xp = { x, x + size / 2, x + size };
                        int[] yp = { y + r2 + hw / 2, y + size, y + r2 + hw / 2 };
                        g2.fillPolygon(xp, yp, 3);
                        break;
                    }
                    case "STORE": {
                        // Mái + thân hộp
                        int mid = size / 3;
                        int[] rx = { x, x + size / 2, x + size };
                        int[] ry = { y + mid, y, y + mid };
                        g2.fillPolygon(rx, ry, 3);
                        g2.fillRect(x + 1, y + mid, size - 2, size - mid - 1);
                        // Cửa (màu nền - để tạo khoảng trắng)
                        g2.setColor(Color.WHITE);
                        g2.fillRect(x + size * 3 / 8, y + mid + (size - mid) / 2,
                                size / 4, (size - mid) / 2);
                        break;
                    }
                    case "EXCEL": {
                        // Chữ X đơn giản trong hình chữ nhật
                        g2.setColor(color);
                        g2.setStroke(new BasicStroke(2f));
                        g2.drawRoundRect(x + 1, y + 1, size - 3, size - 3, 3, 3);
                        g2.drawLine(x + 4, y + 4, x + size - 5, y + size - 5);
                        g2.drawLine(x + size - 5, y + 4, x + 4, y + size - 5);
                        break;
                    }
                    case "PDF": {
                        // Tờ giấy + chữ P
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.drawRoundRect(x + 2, y + 1, size - 5, size - 2, 2, 2);
                        g2.setFont(new Font("Arial", Font.BOLD, size * 7 / 12));
                        FontMetrics fm = g2.getFontMetrics();
                        String s = "P";
                        g2.drawString(s, x + (size - fm.stringWidth(s)) / 2,
                                y + (size + fm.getAscent() - fm.getDescent()) / 2);
                        break;
                    }
                    case "PRINT": {
                        // Hình máy in đơn giản
                        int pad = size / 5;
                        // Thân máy in
                        g2.fillRoundRect(x, y + pad, size, size - pad * 2, 3, 3);
                        // Tờ giấy ra
                        g2.setColor(Color.WHITE);
                        g2.fillRect(x + pad, y + pad + 2, size - pad * 2, size / 4);
                        // Tờ giấy vào
                        g2.setColor(color);
                        g2.fillRect(x + pad, y, size - pad * 2, pad + 2);
                        g2.setColor(Color.WHITE);
                        g2.fillRect(x + pad, y + size - pad - 2, size - pad * 2, pad);
                        break;
                    }
                    default:
                        g2.fillOval(x + 2, y + 2, size - 4, size - 4);
                }
                g2.dispose();
            }
            @Override public int getIconWidth()  { return size; }
            @Override public int getIconHeight() { return size; }
        };
    }

    private JButton makeActionBtn(String text, Icon icon, Color bg) {
        JButton b = new JButton(" " + text);
        if (icon != null) { b.setIcon(icon); b.setIconTextGap(6); }
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(7, 14, 7, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        // hover darken
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                b.setBackground(bg.darker()); }
            @Override public void mouseExited(MouseEvent e)  {
                b.setBackground(bg); }
        });
        return b;
    }

    // ===== PDF EXPORT (dùng Java PrinterJob → Print to PDF / máy in) =====
    private void xuatPDF() {
        // Chọn tên file lưu
        Window owner = SwingUtilities.getWindowAncestor(this);
        Frame parentFrame = (owner instanceof Frame) ? (Frame) owner : null;
        java.awt.FileDialog fd = new java.awt.FileDialog(parentFrame, "Lưu báo cáo PDF", java.awt.FileDialog.SAVE);
        String tsNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmm"));
        fd.setFile("BaoCaoThongKe_" + currentYear + "_" + tsNow + ".pdf");
        fd.setFilenameFilter((dir, name) -> name.toLowerCase().endsWith(".pdf") || !name.contains("."));
        fd.setVisible(true);
        String dir = fd.getDirectory(), file = fd.getFile();
        if (dir == null || file == null) return;
        String filePath = dir + (file.toLowerCase().endsWith(".pdf") ? file : file + ".pdf");

        PrinterJob pj = PrinterJob.getPrinterJob();
        // Tìm dịch vụ PDF (Microsoft Print to PDF / cups-pdf / ...)
        PrintService pdfService = null;
        for (PrintService ps : PrinterJob.lookupPrintServices()) {
            String n = ps.getName().toLowerCase();
            if (n.contains("pdf") || n.contains("microsoft print")) { pdfService = ps; break; }
        }

        PageFormat pf = pj.defaultPage();
        Paper paper = new Paper();
        double w = 595, h = 842; // A4 @ 72dpi
        paper.setSize(w, h);
        paper.setImageableArea(36, 36, w - 72, h - 72);
        pf.setPaper(paper);
        pf.setOrientation(PageFormat.PORTRAIT);

        final String finalPath = filePath;
        pj.setPrintable(buildReportPrintable(), pf);

        PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
        attrs.add(new javax.print.attribute.standard.Copies(1));

        if (pdfService != null) {
            // Dùng PDF printer tự động – chạy trong background thread
            try { pj.setPrintService(pdfService); }
            catch (PrinterException ex) { showCustomDialog("Lỗi PDF printer: " + ex.getMessage(), "ERROR"); return; }
            try {
                attrs.add(new javax.print.attribute.standard.Destination(
                        new java.net.URI("file:///" + finalPath.replace("\\", "/"))));
            } catch (Exception ex) { ex.printStackTrace(); return; }
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new Thread(() -> {
                try {
                    pj.print(attrs);
                    SwingUtilities.invokeLater(() -> {
                        setCursor(Cursor.getDefaultCursor());
                        showCustomDialog("Xuất PDF thành công!\n" + finalPath, "SUCCESS");
                        try { Desktop.getDesktop().open(new File(finalPath)); } catch (Exception ignored) {}
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        setCursor(Cursor.getDefaultCursor());
                        showCustomDialog("Lỗi xuất PDF: " + ex.getMessage(), "ERROR");
                    });
                }
            }).start();
        } else {
            // Fallback: hiện dialog in, user tự chọn "Save as PDF"
            JOptionPane.showMessageDialog(this,
                    "<html>Không tìm thấy máy in PDF tự động.<br>" +
                    "Trong hộp thoại in, chọn <b>\"Microsoft Print to PDF\"</b> hoặc <b>\"Save as PDF\"</b>.<br>" +
                    "Đường dẫn gợi ý: <b>" + finalPath + "</b></html>",
                    "Hướng dẫn", JOptionPane.INFORMATION_MESSAGE);
            if (pj.printDialog(attrs)) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                new Thread(() -> {
                    try {
                        pj.print(attrs);
                        SwingUtilities.invokeLater(() -> {
                            setCursor(Cursor.getDefaultCursor());
                            showCustomDialog("Đã gửi lệnh in thành công!", "SUCCESS");
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            setCursor(Cursor.getDefaultCursor());
                            showCustomDialog("Lỗi xuất PDF: " + ex.getMessage(), "ERROR");
                        });
                    }
                }).start();
            }
        }
    }

    // ===== IN BÁO CÁO =====
    private void inBaoCao() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new Thread(() -> {
            try {
                PrinterJob pj = PrinterJob.getPrinterJob();
                PageFormat pf = pj.defaultPage();
                pf.setOrientation(PageFormat.PORTRAIT);
                Paper paper = new Paper();
                double w = 595, h = 842; // Khổ A4
                paper.setSize(w, h);
                paper.setImageableArea(36, 36, w - 72, h - 72);
                pf.setPaper(paper);
                
                pj.setPrintable(buildReportPrintable(), pf);
                SwingUtilities.invokeLater(() -> setCursor(Cursor.getDefaultCursor()));
                if (pj.printDialog()) {
                    pj.print(); // Tiến hành in
                    SwingUtilities.invokeLater(() ->
                        showCustomDialog("Đã gửi lệnh in thành công!", "SUCCESS"));
                }
            } catch (PrinterException ex) {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    showCustomDialog("Lỗi máy in: " + ex.getMessage(), "ERROR");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    showCustomDialog("Lỗi hệ thống: " + e.getMessage(), "ERROR");
                });
            }
        }).start();
    }
    /** Tạo Printable vẽ báo cáo tóm tắt lên trang in / PDF */
    private Printable buildReportPrintable() {
        // Thu thập snapshot data hiện tại
        final String kyStr = getKyString() + " " + currentYear;
        final double[] dtSnap = DT_DATA.clone();
        final double[] cpSnap = CP_DATA.clone();
        final double[] lnSnap = LN_DATA.clone();
        final String[] labSnap = CHART_LABELS.clone();
        final String[] nvNames = NV_NAMES.clone();
        final int[] nvHDS = NV_HD_S.clone(), nvHDC = NV_HD_C.clone(), nvHDT = NV_HD_T.clone();
        final double[] nvDTS = NV_DT_S.clone(), nvDTC = NV_DT_C.clone(), nvDT_T = NV_DT_T.clone();
        final String exportTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
        final String userInfo = "Quản Lý";

        // Cố load logo
        java.awt.image.BufferedImage logoImg = null;
        try {
            java.net.URL logoUrl = getClass().getResource("/anhlogo.png");
            if (logoUrl == null) logoUrl = getClass().getResource("/logo.png");
            if (logoUrl != null) logoImg = javax.imageio.ImageIO.read(logoUrl);
            if (logoImg == null) {
                java.io.File f1 = new java.io.File("anhlogo.png");
                if (!f1.exists()) f1 = new java.io.File("logo.png");
                if (f1.exists()) logoImg = javax.imageio.ImageIO.read(f1);
            }
        } catch (Exception ignored) {}
        final java.awt.image.BufferedImage finalLogo = logoImg;

        return (graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 1) return Printable.NO_SUCH_PAGE;

            Graphics2D g = (Graphics2D) graphics;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

            float ox = (float) pageFormat.getImageableX();
            float oy = (float) pageFormat.getImageableY();
            float pw = (float) pageFormat.getImageableWidth();
            float ph = (float) pageFormat.getImageableHeight();
            g.translate(ox, oy);

            float y = 0;
            float lineH = 19;

            // ─────────────────────────────────────────────────────────────────
            // HEADER GRADIENT BAND
            // ─────────────────────────────────────────────────────────────────
            int hdrH = 62;
            GradientPaint hdrGrad = new GradientPaint(0, 0, Color.decode("#152A4B"), pw, 0, Color.decode("#1A73E8"));
            g.setPaint(hdrGrad);
            g.fillRect(0, 0, (int) pw, hdrH);

            // Logo (trái)
            if (finalLogo != null) {
                int lh = 46, lw = (int)(lh * (double)finalLogo.getWidth() / finalLogo.getHeight());
                g.drawImage(finalLogo, 8, 8, lw, lh, null);
            }

            // Tên công ty + báo cáo (phải logo)
            int logoW = finalLogo != null ? (int)(46.0 * finalLogo.getWidth() / finalLogo.getHeight()) + 14 : 0;
            g.setColor(Color.WHITE);
            g.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g.drawString("MYCARE PHARMACY", logoW, 26);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g.setColor(new Color(220, 235, 255));
            g.drawString("BÁO CÁO THỐNG KÊ NHÀ THUỐC", logoW, 43);
            g.drawString("Kỳ: " + kyStr + "   |   Xuất: " + exportTime + "   |   " + userInfo, logoW, 57);

            y = hdrH + 10;

            // ─────────────────────────────────────────────────────────────────
            // TRANG 1: DOANH THU - CHI PHÍ - LỢI NHUẬN
            // ─────────────────────────────────────────────────────────────────
            if (pageIndex == 0) {
                // Section title bar
                g.setPaint(Color.decode("#EEF2FF"));
                g.fillRoundRect(0, (int)y, (int)pw, 22, 6, 6);
                g.setColor(Color.decode("#1A73E8"));
                g.fillRoundRect(0, (int)y, 4, 22, 3, 3);
                g.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g.setColor(Color.decode("#152A4B"));
                g.drawString("I.  DOANH THU – CHI PHÍ – LỢI NHUẬN", 12, y + 15);
                y += 28;

                String[] hdr = { "Kỳ", "Doanh thu", "Chi phí", "Lợi nhuận" };
                float[] colW = { pw * 0.16f, pw * 0.28f, pw * 0.28f, pw * 0.28f };

                // Table header row
                g.setPaint(new GradientPaint(0, y, Color.decode("#1A73E8"), pw, y, Color.decode("#0D5DBF")));
                g.fillRect(0, (int) y, (int) pw, (int) lineH);
                g.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g.setColor(Color.WHITE);
                float cx = 0;
                for (int i = 0; i < hdr.length; i++) {
                    g.drawString(hdr[i], cx + 6, y + lineH - 5);
                    cx += colW[i];
                }
                y += lineH;

                // Vertical dividers on header
                g.setColor(new Color(255,255,255,60));
                cx = colW[0];
                for (int i = 1; i < 3; i++) { g.fillRect((int)cx, (int)(y-lineH), 1, (int)lineH); cx += colW[i]; }

                double totDT = 0, totCP = 0, totLN = 0;
                int rows = Math.min(labSnap.length, dtSnap.length);
                for (int i = 0; i < rows; i++) {
                    Color rowBg = (i % 2 == 0) ? Color.WHITE : Color.decode("#F8FAFF");
                    g.setColor(rowBg);
                    g.fillRect(0, (int) y, (int) pw, (int) lineH);

                    // Left accent
                    boolean isPositive = lnSnap[i] >= 0;
                    g.setColor(isPositive ? Color.decode("#00A76F") : Color.decode("#FF5630"));
                    g.fillRect(0, (int)y, 3, (int)lineH);

                    g.setColor(Color.decode("#DFE3E8"));
                    cx = colW[0];
                    for (int k = 1; k < 3; k++) { g.fillRect((int)cx-1, (int)y, 1, (int)lineH); cx += colW[k]; }

                    g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                    g.setColor(Color.decode("#212B36"));
                    cx = 4;
                    g.drawString(labSnap[i], cx + 3, y + lineH - 5); cx += colW[0];
                    g.drawString(formatKRaw(dtSnap[i] * 1_000_000), cx + 3, y + lineH - 5); cx += colW[1];
                    g.drawString(formatKRaw(cpSnap[i] * 1_000_000), cx + 3, y + lineH - 5); cx += colW[2];
                    g.setColor(isPositive ? Color.decode("#00875A") : Color.decode("#FF5630"));
                    g.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g.drawString(formatKRaw(lnSnap[i] * 1_000_000), cx + 3, y + lineH - 5);

                    totDT += dtSnap[i]; totCP += cpSnap[i]; totLN += lnSnap[i];
                    y += lineH;

                    // Row bottom border
                    g.setColor(Color.decode("#EEEEEE"));
                    g.fillRect(0, (int)y - 1, (int)pw, 1);
                }

                // Total row
                g.setPaint(Color.decode("#EEF2FF"));
                g.fillRect(0, (int) y, (int) pw, (int) lineH + 2);
                g.setColor(Color.decode("#1A73E8"));
                g.fillRect(0, (int)y, 3, (int)lineH+2);
                g.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g.setColor(Color.decode("#152A4B"));
                cx = 4;
                g.drawString("TỔNG CỘNG", cx + 3, y + lineH - 3); cx += colW[0];
                g.drawString(formatKRaw(totDT * 1_000_000), cx + 3, y + lineH - 3); cx += colW[1];
                g.drawString(formatKRaw(totCP * 1_000_000), cx + 3, y + lineH - 3); cx += colW[2];
                g.setColor(totLN >= 0 ? Color.decode("#00875A") : Color.decode("#FF5630"));
                g.drawString(formatKRaw(totLN * 1_000_000), cx + 3, y + lineH - 3);
                y += lineH + 12;

                // ── Summary info box ────────────────────────────────────────
                String[] sumKeys = { "Tổng doanh thu:", "Tổng chi phí:", "Lợi nhuận ròng:" };
                String[] sumVals = {
                    formatKRaw(totDT * 1_000_000),
                    formatKRaw(totCP * 1_000_000),
                    formatKRaw(totLN * 1_000_000)
                };
                Color[] sumColors = { Color.decode("#1A73E8"), Color.decode("#FF5630"), totLN >= 0 ? Color.decode("#00A76F") : Color.decode("#FF5630") };
                float boxW = pw / 3 - 6;
                for (int i = 0; i < 3; i++) {
                    float bx = i * (boxW + 9);
                    g.setColor(Color.decode("#F8FAFF"));
                    g.fillRoundRect((int)bx, (int)y, (int)boxW, 38, 8, 8);
                    g.setColor(Color.decode("#DFE3E8"));
                    g.drawRoundRect((int)bx, (int)y, (int)boxW, 38, 8, 8);
                    g.setColor(sumColors[i]);
                    g.fillRoundRect((int)bx, (int)y, (int)boxW, 4, 3, 3);
                    g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                    g.setColor(Color.decode("#637381"));
                    g.drawString(sumKeys[i], bx + 6, y + 18);
                    g.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    g.setColor(sumColors[i]);
                    g.drawString(sumVals[i], bx + 6, y + 34);
                }
                y += 50;
            }

            // ─────────────────────────────────────────────────────────────────
            // TRANG 2: THỐNG KÊ DƯỢC SĨ
            // ─────────────────────────────────────────────────────────────────
            if (pageIndex == 1) {
                g.setPaint(Color.decode("#FFF8E1"));
                g.fillRoundRect(0, (int)y, (int)pw, 22, 6, 6);
                g.setColor(Color.decode("#FFAB00"));
                g.fillRoundRect(0, (int)y, 4, 22, 3, 3);
                g.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g.setColor(Color.decode("#152A4B"));
                g.drawString("II.  THỐNG KÊ DƯỢC SĨ", 12, y + 15);
                y += 28;

                String[] hNV = { "Dược sĩ", "Ca Sáng", "DT Sáng", "Ca Chiều", "DT Chiều", "Ca Tối", "DT Tối", "Tổng HĐ" };
                float[] cW  = { pw*0.22f, pw*0.06f, pw*0.12f, pw*0.07f, pw*0.12f, pw*0.06f, pw*0.12f, pw*0.11f };

                // Table header
                g.setPaint(new GradientPaint(0, y, Color.decode("#F59E0B"), pw, y, Color.decode("#D97706")));
                g.fillRect(0, (int) y, (int) pw, (int) lineH);
                g.setFont(new Font("Segoe UI", Font.BOLD, 9));
                g.setColor(Color.WHITE);
                float cx = 0;
                for (int i = 0; i < hNV.length; i++) { g.drawString(hNV[i], cx + 3, y + lineH - 5); cx += cW[i]; }
                y += lineH;

                for (int i = 0; i < nvNames.length; i++) {
                    Color rowBg = (i % 2 == 0) ? Color.WHITE : Color.decode("#FFFBF0");
                    g.setColor(rowBg);
                    g.fillRect(0, (int) y, (int) pw, (int) lineH);
                    g.setColor(Color.decode("#FFEECC"));
                    cx = cW[0];
                    for (int k = 1; k < cW.length - 1; k++) { g.fillRect((int)cx-1, (int)y, 1, (int)lineH); cx += cW[k]; }
                    g.setColor(Color.decode("#EEEEEE"));
                    g.fillRect(0, (int)y+(int)lineH-1, (int)pw, 1);

                    int hdS = i < nvHDS.length ? nvHDS[i] : 0;
                    int hdC = i < nvHDC.length ? nvHDC[i] : 0;
                    int hdT = i < nvHDT.length ? nvHDT[i] : 0;
                    double dS = i < nvDTS.length ? nvDTS[i] : 0;
                    double dC = i < nvDTC.length ? nvDTC[i] : 0;
                    double dT = i < nvDT_T.length ? nvDT_T[i] : 0;
                    Object[] vals = { nvNames[i], hdS, formatM(dS), hdC, formatM(dC), hdT, formatM(dT), hdS+hdC+hdT };

                    cx = 0;
                    for (int j = 0; j < vals.length; j++) {
                        boolean isTotal = (j == vals.length - 1);
                        g.setFont(new Font("Segoe UI", isTotal ? Font.BOLD : Font.PLAIN, 9));
                        g.setColor(isTotal ? Color.decode("#152A4B") : Color.decode("#212B36"));
                        g.drawString(vals[j].toString(), cx + 3, y + lineH - 5);
                        cx += cW[j];
                    }
                    y += lineH;
                }
            }

            // ─────────────────────────────────────────────────────────────────
            // FOOTER
            // ─────────────────────────────────────────────────────────────────
            g.setColor(Color.decode("#DFE3E8"));
            g.fillRect(0, (int)(ph - 22), (int)pw, 1);
            g.setFont(new Font("Segoe UI", Font.ITALIC, 8));
            g.setColor(Color.decode("#999999"));
            g.drawString("© MyCare Pharmacy – Báo cáo tự động | Trang " + (pageIndex + 1) + " / 2", 0, (int)(ph - 8));
            String rTxt = "Kỳ: " + kyStr;
            g.drawString(rTxt, pw - g.getFontMetrics().stringWidth(rTxt), (int)(ph - 8));

            g.translate(-ox, -oy);
            return Printable.PAGE_EXISTS;
        };
    }

    /** formatK không dấu ngoặc (dùng cho in) */
    private static String formatKRaw(double val) {
        if (Math.abs(val) >= 1_000_000_000) return String.format("%,.1f tỷ", val / 1_000_000_000.0);
        if (Math.abs(val) >= 1_000_000)     return String.format("%,.0f tr", val / 1_000_000.0);
        return String.format("%,.0f đ", val);
    }

    private JButton makeTabBtn(Icon icon, String text, boolean active) {
        JButton b = new JButton(text);
        if (icon != null) {
            b.setIcon(icon);
            b.setIconTextGap(8);
        }
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(6, 14, 6, 14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(active ? Color.decode("#E8F0FE") : Color.WHITE);
        b.setForeground(active ? Color.decode("#1A73E8") : Color.decode("#444444"));
        if (active) b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return b;
    }

    // ===== EXCEL EXPORT =====
    private void xuatExcel() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        Frame parentFrame = (owner instanceof Frame) ? (Frame) owner : null;

        java.awt.FileDialog fd = new java.awt.FileDialog(parentFrame, "Lưu báo cáo Excel", java.awt.FileDialog.SAVE);
        String tsNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmm"));
        fd.setFile("BaoCaoThongKe_" + currentYear + "_" + tsNow + ".xlsx");
        fd.setVisible(true);

        String dir = fd.getDirectory();
        String file = fd.getFile();

        if (dir != null && file != null) {
            String filePath = dir + file;
            if (!filePath.toLowerCase().endsWith(".xlsx"))
                filePath += ".xlsx";

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            try (Workbook workbook = new XSSFWorkbook()) {
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setBorderBottom(BorderStyle.THIN);
                org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                CellStyle currencyStyle = workbook.createCellStyle();
                short format = workbook.createDataFormat().getFormat("#,##0");
                currencyStyle.setDataFormat(format);

                // Tên sheet tab + nhãn cột A theo kỳ lọc hiện tại
                String kyLabel, colALabel;
                switch (modeLocThoiGian) {
                    case "HOM_NAY":  kyLabel = "Doanh Thu Hôm Nay";    colALabel = "Giờ";   break;
                    case "TUAN":     kyLabel = "Doanh Thu Tuần Này";    colALabel = "Ngày";  break;
                    case "THANG":    kyLabel = "Doanh Thu Theo Ngày";   colALabel = "Ngày";  break;
                    case "QUY":      kyLabel = "Doanh Thu Theo Tháng";  colALabel = "Tháng"; break;
                    case "TUYCHINH": kyLabel = "Doanh Thu Tùy Chỉnh";  colALabel = "Ngày";  break;
                    default:         kyLabel = "Doanh Thu 12 Tháng";   colALabel = "Tháng"; break; // NAM
                }

                Sheet sheetDT = workbook.createSheet(kyLabel);
                Row rowHeaderDT = sheetDT.createRow(0);
                String[] headersDT = { colALabel, "Doanh thu (VNĐ)", "Chi phí (VNĐ)", "Lợi nhuận (VNĐ)" };
                for (int i = 0; i < headersDT.length; i++) {
                    org.apache.poi.ss.usermodel.Cell c = rowHeaderDT.createCell(i);
                    c.setCellValue(headersDT[i]);
                    c.setCellStyle(headerStyle);
                }
                double tDT = 0, tCP = 0, tLN = 0;
                int dtLen = Math.min(CHART_LABELS.length,
                            Math.min(DT_DATA.length, Math.min(CP_DATA.length, LN_DATA.length)));
                for (int i = 0; i < dtLen; i++) {
                    Row r = sheetDT.createRow(i + 1);
                    r.createCell(0).setCellValue(CHART_LABELS[i]);  // nhãn động (T1..T12 / ngày...)

                    org.apache.poi.ss.usermodel.Cell c1 = r.createCell(1);
                    c1.setCellValue(DT_DATA[i] * 1_000_000);
                    c1.setCellStyle(currencyStyle);

                    org.apache.poi.ss.usermodel.Cell c2 = r.createCell(2);
                    c2.setCellValue(CP_DATA[i] * 1_000_000);
                    c2.setCellStyle(currencyStyle);

                    org.apache.poi.ss.usermodel.Cell c3 = r.createCell(3);
                    c3.setCellValue(LN_DATA[i] * 1_000_000);
                    c3.setCellStyle(currencyStyle);

                    tDT += DT_DATA[i];
                    tCP += CP_DATA[i];
                    tLN += LN_DATA[i];
                }
                Row rTotal = sheetDT.createRow(dtLen + 1);
                rTotal.createCell(0).setCellValue("TỔNG");
                org.apache.poi.ss.usermodel.Cell ct1 = rTotal.createCell(1);
                ct1.setCellValue(tDT * 1_000_000);
                ct1.setCellStyle(currencyStyle);
                org.apache.poi.ss.usermodel.Cell ct2 = rTotal.createCell(2);
                ct2.setCellValue(tCP * 1_000_000);
                ct2.setCellStyle(currencyStyle);
                org.apache.poi.ss.usermodel.Cell ct3 = rTotal.createCell(3);
                ct3.setCellValue(tLN * 1_000_000);
                ct3.setCellStyle(currencyStyle);
                for (int i = 0; i < 4; i++)
                    sheetDT.autoSizeColumn(i);

                Sheet sheetNV = workbook.createSheet("Thống Kê Dược Sĩ");
                Row rowHeaderNV = sheetNV.createRow(0);
                String[] headersNV = { "Tên Dược sĩ", "Chức vụ", "HĐ Sáng", "DT Sáng", "HĐ Chiều", "DT Chiều", "HĐ Tối",
                        "DT Tối", "Tổng HĐ", "Tổng DT" };
                for (int i = 0; i < headersNV.length; i++) {
                    org.apache.poi.ss.usermodel.Cell c = rowHeaderNV.createCell(i);
                    c.setCellValue(headersNV[i]);
                    c.setCellStyle(headerStyle);
                }
                for (int i = 0; i < NV_NAMES.length; i++) {
                    Row r = sheetNV.createRow(i + 1);
                    r.createCell(0).setCellValue(NV_NAMES[i]);
                    r.createCell(1).setCellValue(NV_ROLES[i]);
                    r.createCell(2).setCellValue(NV_HD_S[i]);
                    org.apache.poi.ss.usermodel.Cell cs = r.createCell(3);
                    cs.setCellValue(NV_DT_S[i] * 1_000_000);
                    cs.setCellStyle(currencyStyle);
                    r.createCell(4).setCellValue(NV_HD_C[i]);
                    org.apache.poi.ss.usermodel.Cell cc = r.createCell(5);
                    cc.setCellValue(NV_DT_C[i] * 1_000_000);
                    cc.setCellStyle(currencyStyle);

                    int hdT = i < NV_HD_T.length ? NV_HD_T[i] : 0;
                    double dtT = i < NV_DT_T.length ? NV_DT_T[i] : 0;
                    r.createCell(6).setCellValue(hdT);
                    org.apache.poi.ss.usermodel.Cell ct = r.createCell(7);
                    ct.setCellValue(dtT * 1_000_000);
                    ct.setCellStyle(currencyStyle);

                    r.createCell(8).setCellValue(NV_HD_S[i] + NV_HD_C[i] + hdT);
                    org.apache.poi.ss.usermodel.Cell cTot = r.createCell(9);
                    cTot.setCellValue((NV_DT_S[i] + NV_DT_C[i] + dtT) * 1_000_000);
                    cTot.setCellStyle(currencyStyle);
                }
                for (int i = 0; i < 10; i++)
                    sheetNV.autoSizeColumn(i);

                Sheet sheetTop = workbook.createSheet("Top Sản Phẩm");
                Row rowHeaderTop = sheetTop.createRow(0);
                String[] headersTop = { "Hạng", "Sản phẩm", "Loại", "Số lượng", "Doanh thu (VNĐ)" };
                for (int i = 0; i < headersTop.length; i++) {
                    org.apache.poi.ss.usermodel.Cell c = rowHeaderTop.createCell(i);
                    c.setCellValue(headersTop[i]);
                    c.setCellStyle(headerStyle);
                }
                if (modelTopSP != null) {
                    for (int i = 0; i < modelTopSP.getRowCount(); i++) {
                        Row r = sheetTop.createRow(i + 1);
                        r.createCell(0).setCellValue(modelTopSP.getValueAt(i, 0).toString());
                        r.createCell(1).setCellValue(modelTopSP.getValueAt(i, 1).toString());
                        r.createCell(2).setCellValue(modelTopSP.getValueAt(i, 2).toString());
                        try {
                            r.createCell(3).setCellValue(
                                    Double.parseDouble(modelTopSP.getValueAt(i, 3).toString().replace(",", "")));
                        } catch (Exception e) {
                        }

                        double dt = 0;
                        try {
                            dt = Double.parseDouble(modelTopSP.getValueAt(i, 4).toString().replaceAll("[^\\d]", ""));
                        } catch (Exception e) {
                        }
                        org.apache.poi.ss.usermodel.Cell cDt = r.createCell(4);
                        cDt.setCellValue(dt);
                        cDt.setCellStyle(currencyStyle);
                    }
                }
                for (int i = 0; i < 5; i++)
                    sheetTop.autoSizeColumn(i);

                // ── Sheet: Báo cáo tài chính chi tiết ───────────────────────────────
                Sheet sheetVAT = workbook.createSheet("Báo Cáo Tài Chính");
                Row rowHeaderVAT = sheetVAT.createRow(0);
                String[] headersVAT = {
                    "Kỳ",
                    "DT gộp (VNĐ)", "(−) Hoàn trả (VNĐ)", "DT sau trả (VNĐ)",
                    "(−) Thuế VAT (VNĐ)", "DT thuần (VNĐ)",
                    "Giá vốn COGS (VNĐ)", "Lợi nhuận gộp (VNĐ)"
                };
                for (int i = 0; i < headersVAT.length; i++) {
                    org.apache.poi.ss.usermodel.Cell c = rowHeaderVAT.createCell(i);
                    c.setCellValue(headersVAT[i]);
                    c.setCellStyle(headerStyle);
                }
                if (modelTaiChinh != null) {
                    for (int i = 0; i < modelTaiChinh.getRowCount(); i++) {
                        Row r = sheetVAT.createRow(i + 1);
                        r.createCell(0).setCellValue(modelTaiChinh.getValueAt(i, 0).toString());
                        // Cột 1–7: parse số từ chuỗi định dạng VNĐ
                        for (int c = 1; c < headersVAT.length; c++) {
                            double val = 0;
                            try {
                                val = Double.parseDouble(
                                    modelTaiChinh.getValueAt(i, c).toString()
                                               .replaceAll("[^\\d]", ""));
                            } catch (Exception ignored) {}
                            org.apache.poi.ss.usermodel.Cell cell = r.createCell(c);
                            cell.setCellValue(val);
                            cell.setCellStyle(currencyStyle);
                        }
                    }
                }
                for (int i = 0; i < headersVAT.length; i++)
                    sheetVAT.autoSizeColumn(i);

                // ── Sheet: Khách hàng ────────────────────────────────────────────────
                Sheet sheetKH = workbook.createSheet("Khách Hàng");
                Row rowHeaderKH = sheetKH.createRow(0);
                // modelTopKH có đúng 5 cột: Hạng | Tên KH | Số đơn | Doanh thu | Điểm tích lũy
                String[] headersKH = { "Hạng", "Tên khách hàng", "Số đơn", "Doanh thu (VNĐ)", "Điểm tích lũy" };
                for (int i = 0; i < headersKH.length; i++) {
                    org.apache.poi.ss.usermodel.Cell c = rowHeaderKH.createCell(i);
                    c.setCellValue(headersKH[i]);
                    c.setCellStyle(headerStyle);
                }
                if (modelTopKH != null) {
                    for (int i = 0; i < modelTopKH.getRowCount(); i++) {
                        Row r = sheetKH.createRow(i + 1);
                        // Cột 0: Hạng (text)
                        r.createCell(0).setCellValue(modelTopKH.getValueAt(i, 0).toString());
                        // Cột 1: Tên KH (text)
                        r.createCell(1).setCellValue(modelTopKH.getValueAt(i, 1).toString());
                        // Cột 2: Số đơn (số nguyên)
                        try {
                            r.createCell(2).setCellValue(
                                Double.parseDouble(modelTopKH.getValueAt(i, 2).toString().replaceAll("[^\\d]", "")));
                        } catch (Exception ignored) { r.createCell(2).setCellValue(0); }
                        // Cột 3: Doanh thu — strip tất cả ký tự không phải chữ số
                        try {
                            org.apache.poi.ss.usermodel.Cell cDT = r.createCell(3);
                            cDT.setCellValue(Double.parseDouble(
                                modelTopKH.getValueAt(i, 3).toString().replaceAll("[^\\d]", "")));
                            cDT.setCellStyle(currencyStyle);
                        } catch (Exception ignored) { r.createCell(3).setCellValue(0); }
                        // Cột 4: Điểm tích lũy — strip tất cả ký tự không phải chữ số
                        try {
                            r.createCell(4).setCellValue(Double.parseDouble(
                                modelTopKH.getValueAt(i, 4).toString().replaceAll("[^\\d]", "")));
                        } catch (Exception ignored) { r.createCell(4).setCellValue(0); }
                    }
                }
                for (int i = 0; i < headersKH.length; i++)
                    sheetKH.autoSizeColumn(i);

                Sheet sheetExp = workbook.createSheet("Sắp Hết Hạn");
                Row rowHeaderExp = sheetExp.createRow(0);
                String[] headersExp = { "Mã Lô", "Tên SP", "Kho", "SL Tồn", "Ngày hết hạn" };
                for (int i = 0; i < headersExp.length; i++) {
                    org.apache.poi.ss.usermodel.Cell c = rowHeaderExp.createCell(i);
                    c.setCellValue(headersExp[i]);
                    c.setCellStyle(headerStyle);
                }
                java.util.List<Object[]> listHH = busThongKe.getSpSapHetHan();
                if (listHH != null) {
                    for (int i = 0; i < listHH.size(); i++) {
                        Row r = sheetExp.createRow(i + 1);
                        r.createCell(0).setCellValue(listHH.get(i)[0].toString());
                        r.createCell(1).setCellValue(listHH.get(i)[1].toString());
                        r.createCell(2).setCellValue(listHH.get(i)[2].toString());
                        try {
                            r.createCell(3).setCellValue(Double.parseDouble(listHH.get(i)[3].toString()));
                        } catch (Exception e) {
                        }
                        r.createCell(4).setCellValue(listHH.get(i)[4].toString());
                    }
                }
                for (int i = 0; i < 5; i++)
                    sheetExp.autoSizeColumn(i);

                try (java.io.FileOutputStream out = new java.io.FileOutputStream(filePath)) {
                    workbook.write(out);
                }

                try {
                    java.awt.Desktop.getDesktop().open(new File(filePath));
                } catch (Exception ignored) {
                }
                showCustomDialog("Xuất báo cáo Excel thành công!\n" + filePath, "SUCCESS");

            } catch (java.io.FileNotFoundException ex) {
                showCustomDialog("File Excel này ĐANG ĐƯỢC MỞ!\nVui lòng đóng file trước khi xuất.", "ERROR");
            } catch (Exception ex) {
                ex.printStackTrace();
                showCustomDialog("Lỗi xuất file: " + ex.getMessage(), "ERROR");
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    private void showCustomDialog(String message, String type) {
        boolean isSuccess  = "SUCCESS".equals(type);
        boolean isWarning  = "WARNING".equals(type);
        Color headerColor  = isSuccess ? Color.decode("#00A76F")
                           : isWarning ? Color.decode("#FFAB00")
                           : Color.decode("#D32F2F");
        String headerTitle = isSuccess ? "Thành công"
                           : isWarning ? "Nhắc nhở"
                           : "Lỗi";
        String iconType    = isSuccess ? "CORRECT"
                           : isWarning ? "WARNING"
                           : "CANCEL";

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = (owner instanceof Frame) ? new JDialog((Frame) owner, headerTitle, true)
                : new JDialog((Dialog) owner, headerTitle, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(420, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setResizable(false);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        header.setBackground(headerColor);
        JLabel icoLbl = new JLabel();
        icoLbl.setIcon(new MenuIcon(iconType));
        icoLbl.setForeground(Color.WHITE);
        JLabel titleLbl = new JLabel(headerTitle);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(Color.WHITE);
        header.add(icoLbl);
        header.add(titleLbl);

        String html = "<html><body style='font-family:Segoe UI;font-size:12pt;padding:4px'>"
                + message.replace("\n", "<br>") + "</body></html>";
        JLabel msgLbl = new JLabel(html);
        msgLbl.setBorder(new EmptyBorder(14, 18, 6, 18));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 8));
        footer.setBackground(Color.decode("#F8F9FA"));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#DFE3E8")));
        JButton btnClose = new JButton("  Đóng");
        btnClose.setIcon(new MenuIcon("CANCEL"));
        btnClose.setBackground(headerColor);
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dialog.dispose());
        footer.add(btnClose);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(msgLbl, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel buildSpSapHetHanTable() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(14, 14, 10, 14)));

        JLabel t = new JLabel(" Sản phẩm sắp hết hạn (Dưới 6 tháng)");
        t.setIcon(MenuIcon.of("WARNING", 16, Color.decode("#D32F2F")));
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(Color.decode("#D32F2F"));
        p.add(t, BorderLayout.NORTH);

        String[] cols = { "Mã Lô", "Tên SP", "Kho", "SL Tồn", "Ngày hết hạn" };
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        new Thread(() -> {
            java.util.List<Object[]> list = busThongKe.getSpSapHetHan();
            for (Object[] row : list)
                SwingUtilities.invokeLater(() -> m.addRow(row));
        }).start();

        JTable tbl = new JTable(m);
        tbl.setRowHeight(30);
        tbl.getTableHeader().setBackground(Color.decode("#FFEBEE"));
        JScrollPane spHetHan = new JScrollPane(tbl);
        spHetHan.setBorder(BorderFactory.createEmptyBorder());
        spHetHan.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        spHetHan.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        p.add(spHetHan, BorderLayout.CENTER);
        return p;
    }

    // ===== GỢI Ý KHUYẾN MÃI THÔNG MINH =====
    private DefaultTableModel modelGoiYKM;

    private JPanel buildGoiYKMPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new javax.swing.border.EmptyBorder(14, 14, 10, 14)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel(" Gợi ý khuyến mãi thông minh", MenuIcon.of("GIFT", 16, Color.decode("#E65100")), SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Color.decode("#E65100"));

        JButton btnTaoKM = new JButton("Tạo KM nhanh");
        btnTaoKM.setIcon(MenuIcon.of("ADD", 14, Color.WHITE));
        btnTaoKM.setIconTextGap(6);
        btnTaoKM.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnTaoKM.setForeground(Color.WHITE);
        btnTaoKM.setBackground(Color.decode("#00A76F"));
        btnTaoKM.setBorderPainted(false);
        btnTaoKM.setFocusPainted(false);
        btnTaoKM.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JButton btnRefresh = new JButton("Phân tích lại");
        btnRefresh.setIcon(MenuIcon.of("REFRESH", 14, Color.WHITE));
        btnRefresh.setIconTextGap(6);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBackground(Color.decode("#E65100"));
        btnRefresh.setBorderPainted(false);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel pnlNutAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlNutAction.setOpaque(false);
        pnlNutAction.add(btnTaoKM);
        pnlNutAction.add(btnRefresh);

        header.add(title, BorderLayout.WEST);
        header.add(pnlNutAction, BorderLayout.EAST);
        p.add(header, BorderLayout.NORTH);

        String[] cols = { "Sản phẩm", "Bán/năm", "Biên LN", "Gợi ý KM", "Lý do" };
        modelGoiYKM = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tbl = new JTable(modelGoiYKM);
        tbl.setRowHeight(32);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tbl.getTableHeader().setBackground(Color.decode("#FFF3E0"));
        tbl.getTableHeader().setForeground(Color.decode("#E65100"));

        tbl.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    String lyDo = modelGoiYKM.getRowCount() > row
                            ? String.valueOf(modelGoiYKM.getValueAt(row, 4))
                            : "";
                    String goiY = modelGoiYKM.getRowCount() > row
                            ? String.valueOf(modelGoiYKM.getValueAt(row, 3))
                            : "";
                    if (lyDo.contains("[Sắp HH]")) {
                        setBackground(new Color(255, 235, 238)); // đỏ nhạt – hàng sắp HH
                        setForeground(Color.decode("#B71C1C"));
                    } else if (goiY.contains("[Giảm]")) {
                        setBackground(new Color(255, 243, 224));
                        setForeground(Color.decode("#152A4B"));
                    } else if (goiY.contains("[Quà]")) {
                        setBackground(new Color(232, 245, 233));
                        setForeground(Color.decode("#152A4B"));
                    } else {
                        setBackground(Color.WHITE);
                        setForeground(Color.decode("#152A4B"));
                    }
                }
                setBorder(new javax.swing.border.EmptyBorder(0, 6, 0, 6));
                return this;
            }
        });

        tbl.getColumnModel().getColumn(2).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                String s = val != null ? val.toString() : "";
                setText(s);
                setHorizontalAlignment(CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (!sel) {
                    // Hàng sắp HH: cột biên LN hiển thị "Sắp HH" thay vì %
                    String lyDo = modelGoiYKM.getRowCount() > row
                            ? String.valueOf(modelGoiYKM.getValueAt(row, 4)) : "";
                    if (lyDo.contains("[Sắp HH]")) {
                        setForeground(Color.decode("#B71C1C"));
                        setBackground(new Color(255, 235, 238));
                    } else {
                        double pct = 0;
                        try { pct = Double.parseDouble(s.replace("%", "").trim()); } catch (Exception ignored) {}
                        if (pct >= 40) setForeground(Color.decode("#00875A"));
                        else if (pct >= 20) setForeground(Color.decode("#B95000"));
                        else setForeground(Color.decode("#BF2600"));
                        setBackground(row % 2 == 0 ? new Color(255, 250, 245) : Color.WHITE);
                    }
                }
                setBorder(new javax.swing.border.EmptyBorder(0, 4, 0, 4));
                return this;
            }
        });

        tbl.setFillsViewportHeight(false);
        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(150);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(60);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(65);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(130);
        tbl.getColumnModel().getColumn(4).setMinWidth(200);

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        p.add(sp, BorderLayout.CENTER);
        btnTaoKM.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row == -1) {
                showCustomDialog("Vui lòng chọn 1 sản phẩm trong danh sách gợi ý\ntrước khi tạo chương trình khuyến mãi.", "WARNING");
                return;
            }

            String tenSP    = modelGoiYKM.getValueAt(row, 0).toString();
            String goiYText = modelGoiYKM.getValueAt(row, 3).toString();
            String lyDo     = modelGoiYKM.getValueAt(row, 4).toString();
            boolean isHetHan = lyDo.contains("[Sắp HH]");

            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("isAutoFill", true);

            if (isHetHan) {
                // Hàng sắp HH: tên KM gợi ý kèm cảnh báo
                data.put("1", "[Giải phóng tồn] Mua kèm " + tenSP);
                data.put("2", "Sản phẩm kèm theo");
                // SP tặng = tenSP (hàng sắp HH), slTang = 1
                data.put("15", tenSP);  // fldMaSPTang
                data.put("16", "1");    // fldSoLuongTang
            } else if (goiYText.startsWith("[Quà]")) {
                data.put("1", "KM - " + tenSP);
                data.put("2", "Sản phẩm kèm theo");
            } else {
                data.put("1", "KM - " + tenSP);
                data.put("2", "Giảm theo phần trăm (%)");
                try {
                    String mucGiam = goiYText.replaceAll(".*\\[Giảm\\]\\s*(\\d+)%.*", "$1");
                    data.put("10", mucGiam);
                } catch (Exception ex) {
                    data.put("10", "5");
                }
            }
            data.put("5", "HoaDon");

            Window win = SwingUtilities.getWindowAncestor(p);
            if (win instanceof MainDashboard) {
                ((MainDashboard) win).chuyenSangTabKhuyenMaiVaTaoMoi(data);
            }
        });

        btnRefresh.addActionListener(e -> reloadGoiYKM());
        reloadGoiYKM();
        return p;
    }

    private JPanel buildForecastPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(14, 14, 10, 14)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        // FIX-5: Ẩn nội dung dự báo khi xem năm cũ
        if (currentYear != java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)) {
            JLabel warn = new JLabel("Dự báo chỉ khả dụng cho năm hiện tại", SwingConstants.CENTER);
            warn.setIcon(MenuIcon.of("WARNING", 16, Color.decode("#637381")));
            warn.setIconTextGap(6);
            warn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            warn.setForeground(Color.decode("#637381"));
            p.add(warn, BorderLayout.CENTER);
            return p;
        }

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel titleLbl = new JLabel(" Dự báo doanh thu cuối tháng " +
                java.time.LocalDate.now().getMonthValue() + "/" + currentYear,
                MenuIcon.of("TAB_CHART", 14, Color.decode("#152A4B")), SwingConstants.LEFT);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(Color.decode("#152A4B"));
        header.add(titleLbl, BorderLayout.WEST);
        p.add(header, BorderLayout.NORTH);

        // Tính tốc độ TB 7 ngày gần nhất từ DAILY_30_DT (index 23..29)
        double sum7 = 0;
        int cnt7 = 0;
        for (int i = 23; i < 30; i++) {
            if (DAILY_30_DT.length > i && DAILY_30_DT[i] > 0) {
                sum7 += DAILY_30_DT[i];
                cnt7++;
            }
        }
        double dtTBNgay = cnt7 > 0 ? sum7 / cnt7 : 0;

        // DT tháng hiện tại từ DT_DATA (triệu đồng)
        int thangHT = java.time.LocalDate.now().getMonthValue();
        double dtThangHT = (DT_DATA.length >= thangHT && DT_DATA[thangHT - 1] > 0)
                ? DT_DATA[thangHT - 1]
                : 0;

        // Số ngày còn lại trong tháng
        int ngayConLai = java.time.LocalDate.now().lengthOfMonth()
                - java.time.LocalDate.now().getDayOfMonth();
        double duBao = dtThangHT + dtTBNgay * ngayConLai;

        // Hiển thị dạng lưới 4 dòng
        String[][] rows = {
                { "DT thực tế tháng " + thangHT, formatM(dtThangHT) },
                { "Tốc độ TB 7 ngày qua", formatM(dtTBNgay) + " / ngày" },
                { "Dự báo đến cuối tháng", "~ " + formatM(duBao) },
                { "Còn " + ngayConLai + " ngày", "cần " + formatM(dtTBNgay) + " / ngày" }
        };
        Color[] rowColors = {
                Color.decode("#152A4B"), Color.decode("#637381"),
                Color.decode("#00A76F"), Color.decode("#FFAB00")
        };

        JPanel grid = new JPanel(new GridLayout(4, 1, 0, 0));
        grid.setBackground(Color.WHITE);
        grid.setBorder(BorderFactory.createLineBorder(Color.decode("#EEF2F6")));
        for (int i = 0; i < rows.length; i++) {
            JPanel row = new JPanel(new BorderLayout());
            row.setBackground(i % 2 == 0 ? Color.WHITE : Color.decode("#F8F9FA"));
            row.setBorder(BorderFactory.createMatteBorder(
                    0, 0, i < 3 ? 1 : 0, 0, Color.decode("#EEF2F6")));
            JLabel lbl = new JLabel("  " + rows[i][0]);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(Color.decode("#637381"));
            JLabel val = new JLabel(rows[i][1] + "  ");
            val.setFont(new Font("Segoe UI", Font.BOLD, 12));
            val.setForeground(rowColors[i]);
            row.add(lbl, BorderLayout.WEST);
            row.add(val, BorderLayout.EAST);
            grid.add(row);
        }
        p.add(grid, BorderLayout.CENTER);
        return p;
    }

    private void reloadGoiYKM() {

        if (modelGoiYKM == null)
            return;
        modelGoiYKM.setRowCount(0);
        modelGoiYKM.addRow(new Object[] { "Đang phân tích...", "", "", "", "" });
        int year = currentYear;
        new Thread(() -> {
            java.util.List<Object[]> list = busThongKe.getGoiYKhuyenMai(year);
            SwingUtilities.invokeLater(() -> {
                modelGoiYKM.setRowCount(0);
                if (list.isEmpty()) {
                    modelGoiYKM.addRow(new Object[] { "Không có gợi ý phù hợp", "", "", "", "" });
                    return;
                }
                for (Object[] r : list) {
                    // BUS đã phân tích: r[10] = isHetHan (Boolean)
                    String ten     = String.valueOf(r[0]);
                    int slBan      = 0;
                    try { slBan = r[2] instanceof Integer ? (int) r[2] : Integer.parseInt(String.valueOf(r[2])); } catch (Exception ignored) {}
                    double bienLN  = (double) r[5];
                    String loaiKM  = String.valueOf(r[7]);
                    String lyDo    = String.valueOf(r[8]);
                    String mucGiam = String.valueOf(r[9]);
                    boolean isHetHan = r.length > 10 && Boolean.TRUE.equals(r[10]);

                    // Hàng HH: loaiKM dạng "Mua [SP bán chạy] tặng [SP HH]"
                    String goiYText;
                    if (isHetHan) {
                        goiYText = "[Quà] " + loaiKM;
                    } else if ("Đang có KM".equals(loaiKM)) {
                        goiYText = "⚠ Đang có KM";
                    } else if (loaiKM.contains("Mua")) {
                        goiYText = "[Quà] " + loaiKM;
                    } else {
                        goiYText = "[Giảm] " + mucGiam + "% giá bán";
                    }

                    // Cột Biên LN: hàng thường hiện %, hàng HH hiện "Sắp HH"
                    String bienStr = isHetHan ? "Sắp HH" : String.format("%.0f%%", bienLN);

                    modelGoiYKM.addRow(new Object[]{
                            ten,
                            String.format("%,d", slBan),
                            bienStr,
                            goiYText,
                            lyDo
                    });
                }
            });
        }).start();
    }
    class ScrollablePanel extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return super.getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle v, int o, int d) { return 14; }
        @Override public int getScrollableBlockIncrement(Rectangle v, int o, int d) { return 50; }
        @Override public boolean getScrollableTracksViewportWidth() {
            Container vp = SwingUtilities.getUnwrappedParent(this);
            return (vp instanceof JViewport) && (vp.getWidth() > 900); // Chặn ép chữ
        }
        @Override public boolean getScrollableTracksViewportHeight() {
            Container vp = SwingUtilities.getUnwrappedParent(this);
            return (vp instanceof JViewport) && (vp.getHeight() > super.getPreferredSize().height); // Lấp đầy đáy
        }
    }
    
}