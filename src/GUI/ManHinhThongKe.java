package GUI;

import Utils.UserSession;
import Utils.ModernDatePicker;
import Utils.ModernScrollBarUI;
import BUS.BUS_ThongKe;
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

public class ManHinhThongKe extends JPanel {

    // BUS LAYER
    private final BUS_ThongKe busThongKe = new BUS_ThongKe();

    // DỮ LIỆU LOAD TỪ DB
    static String[] NV_NAMES  = {};
    static String[] NV_IDS    = {};
    static String[] NV_ROLES  = {};
    static String[] NV_SHORT  = {};
    static Color[]  NV_COLORS = {};
    static int[]    NV_HD_S   = {};
    static int[]    NV_HD_C   = {};
    static double[] NV_DT_S   = {};
    static double[] NV_DT_C   = {};
    static String[] DATES_10  = {};
    static int[][]  NV_DAILY  = {};
    
    static double[] DT_DATA   = new double[12];
    static double[] CP_DATA   = new double[12];
    
    static double[] DAILY_30_DT    = new double[30];
    static int[]    DAILY_30_HD    = new int[30];
    static String[] DAILY_30_DATES = new String[30];
    static final String[] THANG = {"T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"};
    static int[]    DONUT_VALS   = {1,1,1,1};
    static final String[] DONUT_LABELS = {"Thuốc KĐ","Thuốc KKĐ","TPCN","Mỹ phẩm"};
    static final Color[]  DONUT_COLORS = {
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

    // FIELDS
    private CardLayout cardBody;
    private JPanel pnlBody;
    private JButton btnDT, btnNV;
    private JComboBox<String> cboKyLoc, cboNhanVien;
    private JButton  btnNamPicker;
    private JLabel   lblYearBadge;
    private int      currentYear = Calendar.getInstance().get(Calendar.YEAR);

    // KPI label refs (Tab DT)
    private JLabel kpiDTVal, kpiDTSub;
    private JLabel kpiHDVal, kpiHDSub;
    private JLabel kpiLNVal, kpiLNSub;
    private JLabel kpiTBVal, kpiTBSub;

    // Chart references
    private BarChartMain   chartBarMain;
    private LineChartDaily chartLineDaily;
    private DonutChart     chartDonut;
    private NVDailyChart   chartNVDaily;
    private NVShiftChart   chartNVShift;
    private NVDetailTable  tblNVDetail;
    private DefaultTableModel modelTopSP, modelVAT;
    private JLabel lblVATTotal;
    private JLabel miniPeakDate, miniAvgDT, miniAvgOrder;

    private int selectedNVIdx = -1;
    
    private JTextField txtTuNgay, txtDenNgay;
    private JComboBox<String> cboThang, cboQuy;
    private String modeLocThoiGian = "THANG"; // Lưu trạng thái đang lọc theo gì

    private JLabel lblTongNV, lblDTTB_NV, lblTongDT_NV;
    private JLabel lblHDSang, lblHDChieu;

    // Panel dược sĩ chỉ hiện ở tab NV
    private JPanel pnlFilterDuocSi;

    // CONSTRUCTOR
    public ManHinhThongKe() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.decode("#F4F6F8"));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // Phân quyền: Chỉ Admin mới được xem
        if (!UserSession.getInstance().isAdmin()) {
            showAccessDenied();
            return;
        }

        add(buildHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        
        // Khu vực top: Chỉ thêm thanh Lọc, KHÔNG thêm Banner Tiền Đầu Ca nữa
        JPanel topInfo = new JPanel(new BorderLayout(0, 8));
        topInfo.setOpaque(false);
        topInfo.add(buildFilterBar(), BorderLayout.NORTH); 
        
        center.add(topInfo, BorderLayout.NORTH);

        cardBody = new CardLayout();
        pnlBody  = new JPanel(cardBody);
        pnlBody.setOpaque(false);
        pnlBody.add(buildViewDT(), "DT");
        pnlBody.add(buildViewNV(), "NV");
        center.add(pnlBody, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        setupEvents();
        loadDataFromDB(currentYear);
    }

    // Helper: JScrollPane với ModernScrollBarUI + fix repaint
    private JScrollPane makeScrollPane(JComponent content) {
        JScrollPane sp = new JScrollPane(content);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setWheelScrollingEnabled(true);
        sp.getVerticalScrollBar().setUnitIncrement(30);
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

        sp.getViewport().addChangeListener(e -> {
            content.revalidate();
            content.repaint();
        });
        return sp;
    }

    // LOAD DỮ LIỆU TỪ DATABASE (tham số year)
    private void loadDataFromDB(int year) {
        // BƯỚC 1: XÂY DỰNG ĐIỀU KIỆN LỌC
        String condHD = "";
        String condPN = "";

        if ("THANG".equals(modeLocThoiGian)) {
            String val = (String) cboThang.getSelectedItem();
            if (val != null && !val.equals("Cả năm")) {
                int m = Integer.parseInt(val.replace("T", ""));
                condHD = " AND MONTH(hd.ngayLapHD) = " + m;
                condPN = " AND MONTH(ngayNhap) = " + m;
            }
        } else if ("QUY".equals(modeLocThoiGian)) {
            int sel = cboQuy.getSelectedIndex();
            if (sel == 1) { condHD = " AND MONTH(hd.ngayLapHD) BETWEEN 1 AND 3";   condPN = " AND MONTH(ngayNhap) BETWEEN 1 AND 3"; }
            else if (sel == 2) { condHD = " AND MONTH(hd.ngayLapHD) BETWEEN 4 AND 6";  condPN = " AND MONTH(ngayNhap) BETWEEN 4 AND 6"; }
            else if (sel == 3) { condHD = " AND MONTH(hd.ngayLapHD) BETWEEN 7 AND 9";  condPN = " AND MONTH(ngayNhap) BETWEEN 7 AND 9"; }
            else if (sel == 4) { condHD = " AND MONTH(hd.ngayLapHD) BETWEEN 10 AND 12"; condPN = " AND MONTH(ngayNhap) BETWEEN 10 AND 12"; }
        } else if ("TUYCHINH".equals(modeLocThoiGian)) {
            String tu = txtTuNgay.getText(), den = txtDenNgay.getText();
            if (!tu.contains("dd") && !den.contains("dd")) {
                try {
                    String from = tu.split("/")[2] + "-" + tu.split("/")[1] + "-" + tu.split("/")[0];
                    String to   = den.split("/")[2] + "-" + den.split("/")[1] + "-" + den.split("/")[0];
                    condHD = " AND CAST(hd.ngayLapHD AS DATE) BETWEEN '" + from + "' AND '" + to + "'";
                    condPN = " AND CAST(ngayNhap AS DATE) BETWEEN '" + from + "' AND '" + to + "'";
                } catch (Exception e) { /* định dạng chưa đủ, bỏ qua */ }
            }
        }

        final String fCondHD = condHD;
        final String fCondPN = condPN;

        // BƯỚC 2: LOAD DATA QUA BUS TRÊN THREAD RIÊNG
        new Thread(() -> {
            try {
                // 1. Danh sách dược sĩ
                java.util.List<String[]> nvList = busThongKe.getDuocSiList();
                int n = nvList.size();

                NV_NAMES  = new String[n]; NV_IDS   = new String[n];
                NV_ROLES  = new String[n]; NV_SHORT = new String[n];
                NV_COLORS = new Color[n];
                NV_HD_S   = new int[n];    NV_HD_C  = new int[n];
                NV_DT_S   = new double[n]; NV_DT_C  = new double[n];

                for (int i = 0; i < n; i++) {
                    NV_IDS[i]   = nvList.get(i)[0];
                    NV_NAMES[i] = nvList.get(i)[1];
                    NV_ROLES[i] = nvList.get(i)[2] != null ? nvList.get(i)[2] : "Dược sĩ";
                    String[] parts = NV_NAMES[i].trim().split("\\s+");
                    NV_SHORT[i] = parts[parts.length - 1];
                    NV_COLORS[i] = PALETTE[i % PALETTE.length];
                }

                // 2. Doanh thu & chi phí 12 tháng
                DT_DATA = busThongKe.getDoanhThu12Thang(year, fCondHD);
                CP_DATA = busThongKe.getChiPhi12Thang(year, fCondPN);

                // 3. Donut phân loại SP
                DONUT_VALS = busThongKe.getSoLuongTheoLoaiSP(year, fCondHD);

                // 4. 10 ngày gần nhất & dữ liệu daily của từng NV
                java.util.List<String> dateList = busThongKe.get10NgayGanNhat(year, fCondHD);
                DATES_10 = dateList.toArray(new String[0]);
                int days = DATES_10.length;
                NV_DAILY = new int[n][days];
                for (int i = 0; i < n; i++)
                    for (int j = 0; j < days; j++)
                        NV_DAILY[i][j] = busThongKe.getDailyHDCuaNV(NV_IDS[i], DATES_10[j], fCondHD);

                // 4b. 30 ngày gần nhất
                java.util.Arrays.fill(DAILY_30_DT, 0);
                java.util.Arrays.fill(DAILY_30_HD, 0);
                java.util.Arrays.fill(DAILY_30_DATES, "");
                java.util.List<Object[]> daily30 = busThongKe.getThongKe30NgayGanNhat(fCondHD);
                for (int i = 0; i < Math.min(daily30.size(), 30); i++) {
                    Object[] row = daily30.get(i);
                    DAILY_30_DATES[i] = (String) row[0];
                    DAILY_30_HD[i]    = (int)    row[1];
                    DAILY_30_DT[i]    = (double) row[2];
                }

                // 4c. Kết quả ca sáng/chiều từng NV
                for (int i = 0; i < n; i++) {
                    double[] sang  = busThongKe.getKetQuaCaSang(NV_IDS[i], year, fCondHD);
                    double[] chieu = busThongKe.getKetQuaCaChieu(NV_IDS[i], year, fCondHD);
                    NV_HD_S[i] = (int) sang[0];  NV_DT_S[i] = sang[1];
                    NV_HD_C[i] = (int) chieu[0]; NV_DT_C[i] = chieu[1];
                }

                // 5. KPI tổng hợp
                double totalDT = 0, totalCP = 0;
                for (double v : DT_DATA) totalDT += v;
                for (double v : CP_DATA) totalCP += v;
                long totalHD     = busThongKe.getTongHoaDon(year, fCondHD);
                double loiNhuan  = totalDT - totalCP;
                double lnPct     = totalDT > 0 ? loiNhuan / totalDT * 100 : 0;
                double giaTriTBDon = totalHD > 0 ? (totalDT * 1_000_000.0 / totalHD) : 0;
                int peakMonth = 0;
                for (int i = 1; i < 12; i++) if (DT_DATA[i] > DT_DATA[peakMonth]) peakMonth = i;

                final long   fHD   = totalHD;
                final double fDT   = totalDT, fLN = loiNhuan, fLNPct = lnPct, fTB = giaTriTBDon;
                final int    fPeak = peakMonth;
                final String kyStr = getKyString();
                final int    fYear = year;

                // BƯỚC 3: CẬP NHẬT GIAO DIỆN TRÊN EDT
                SwingUtilities.invokeLater(() -> {
                    if (kpiDTVal != null) { kpiDTVal.setText(formatM(fDT)); kpiDTSub.setText(kyStr + " " + fYear); }
                    if (kpiHDVal != null) { kpiHDVal.setText(String.format("%,d", fHD)); kpiHDSub.setText("TB mỗi kỳ: " + (fHD/12) + " đơn"); }
                    if (kpiLNVal != null) { kpiLNVal.setText(formatM(fLN)); kpiLNSub.setText(String.format("Tỷ lệ: %.1f%%", fLNPct)); }
                    if (kpiTBVal != null) { kpiTBVal.setText(formatK(fTB)); kpiTBSub.setText("Tháng cao nhất: T" + (fPeak+1)); }
                    if (lblYearBadge != null) lblYearBadge.setText(kyStr + " " + fYear);
                    if (btnNamPicker != null) btnNamPicker.setText("📅 " + fYear + " ▼");

                    if (cboNhanVien != null) {
                        String sel = (String) cboNhanVien.getSelectedItem();
                        ActionListener[] als = cboNhanVien.getActionListeners();
                        for (ActionListener l : als) cboNhanVien.removeActionListener(l);
                        cboNhanVien.removeAllItems();
                        cboNhanVien.addItem("Tất cả");
                        for (String nm : NV_NAMES) cboNhanVien.addItem(nm);
                        if (sel != null) cboNhanVien.setSelectedItem(sel);
                        for (ActionListener l : als) cboNhanVien.addActionListener(l);
                    }

                    if (chartBarMain   != null) chartBarMain.repaint();
                    if (chartLineDaily != null) chartLineDaily.repaint();
                    if (chartDonut     != null) chartDonut.repaint();
                    if (chartNVDaily   != null) chartNVDaily.repaint();
                    if (chartNVShift   != null) chartNVShift.repaint();
                    if (tblNVDetail    != null) tblNVDetail.refreshData();
                    if (pnlBody        != null) { pnlBody.revalidate(); pnlBody.repaint(); }

                    int soLuongNV = NV_NAMES.length;
                    if (lblTongNV != null) lblTongNV.setText(String.valueOf(soLuongNV));

                    double sumDT_NV = 0;
                    for (double s : NV_DT_S) sumDT_NV += s;
                    for (double c : NV_DT_C) sumDT_NV += c;
                    if (lblTongDT_NV != null) lblTongDT_NV.setText(formatM(sumDT_NV));
                    if (lblDTTB_NV   != null) lblDTTB_NV.setText(soLuongNV > 0 ? formatM(sumDT_NV / soLuongNV) : "0đ");

                    int sumHDSang = 0, sumHDChieu = 0;
                    for (int s : NV_HD_S) sumHDSang  += s;
                    for (int c : NV_HD_C) sumHDChieu += c;
                    if (lblHDSang  != null) lblHDSang.setText(sumHDSang  + " HĐ");
                    if (lblHDChieu != null) lblHDChieu.setText(sumHDChieu + " HĐ");

                    if (chartLineDaily != null) chartLineDaily.setData(DAILY_30_DT, DAILY_30_DATES);

                    double sumDT30=0, maxDT30=0; int sumHD30=0, activeDays30=0; String peakDate30="--";
                    for (int i = 0; i < 30; i++) {
                        if (DAILY_30_DT[i]>0||DAILY_30_HD[i]>0) activeDays30++;
                        sumDT30 += DAILY_30_DT[i]; sumHD30 += DAILY_30_HD[i];
                        if (DAILY_30_DT[i]>maxDT30) { maxDT30=DAILY_30_DT[i]; peakDate30=DAILY_30_DATES[i].length()>=5?DAILY_30_DATES[i].substring(0,5):"--"; }
                    }
                    double avgDT30 = activeDays30>0?sumDT30/activeDays30:0;
                    double avgHD30 = activeDays30>0?(double)sumHD30/activeDays30:0;
                    if (miniPeakDate != null) miniPeakDate.setText(peakDate30);
                    if (miniAvgDT    != null) miniAvgDT.setText(formatM(avgDT30));
                    if (miniAvgOrder != null) miniAvgOrder.setText(String.format("%.0f", avgHD30));

                    reloadTopSP(fCondHD, fYear);
                    reloadVAT(fCondHD, fYear);
                });

            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private String formatM(double val) {
        if (val >= 1000) return String.format("%.1fB đ", val / 1000);
        return String.format("%.1fM đ", val);
    }
    private String formatK(double val) {
        if (val >= 1_000_000) return String.format("%.1fM đ", val / 1_000_000);
        if (val >= 1000)      return String.format("%.0fK đ", val / 1000);
        return String.format("%.0fđ", val);
    }
    private String getKyString() {
        if (cboKyLoc == null) return "Cả năm";
        String s = (String) cboKyLoc.getSelectedItem();
        return s != null ? s : "Cả năm";
    }

    // HEADER
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        lblYearBadge = new JLabel("Cả năm " + currentYear);
        lblYearBadge.setForeground(Color.decode("#1A73E8"));
        lblYearBadge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblYearBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#1A73E8"), 1, true),
            new EmptyBorder(2, 10, 2, 10)));

        JLabel title = new JLabel("📊 THỐNG KÊ & BÁO CÁO  ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.decode("#152A4B"));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        left.add(title); left.add(lblYearBadge);

        JButton btnXuat = new JButton("📥 Xuất báo cáo ▼");
        btnXuat.setBackground(Color.decode("#152A4B"));
        btnXuat.setForeground(Color.WHITE);
        btnXuat.setFocusPainted(false);
        btnXuat.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnXuat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPopupMenu menu = new JPopupMenu();
        JMenuItem miExcel = new JMenuItem("Xuất Excel (.csv)");
        miExcel.addActionListener(ev -> xuatExcel());
        JMenuItem miPDF = new JMenuItem("Xuất PDF (HTML)");
        miPDF.addActionListener(ev -> xuatPDF());
        menu.add(miExcel); menu.add(miPDF);
        btnXuat.addActionListener(e -> menu.show(btnXuat, 0, btnXuat.getHeight()));

        p.add(left, BorderLayout.WEST);
        p.add(btnXuat, BorderLayout.EAST);
        return p;
    }

    // FILTER BAR
    private JPanel buildFilterBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        p.setBackground(Color.WHITE);

        btnDT = makeTabBtn("📈 Doanh thu & SP", true);
        btnNV = makeTabBtn("👥 Nhân viên", false);

        // Group Toggle
        JPanel pnlToggle = new JPanel(new GridLayout(1, 3));
        JButton btnThang = new JButton("Tháng");
        JButton btnQuy = new JButton("Quý");
        JButton btnTuyChinh = new JButton("Tùy chỉnh");
        
        Color activeBg = Color.decode("#1A73E8"); Color activeFg = Color.WHITE;
        Color inactiveBg = Color.WHITE; Color inactiveFg = Color.decode("#333333");
        
        btnThang.setBackground(activeBg); btnThang.setForeground(activeFg);
        btnQuy.setBackground(inactiveBg); btnQuy.setForeground(inactiveFg);
        btnTuyChinh.setBackground(inactiveBg); btnTuyChinh.setForeground(inactiveFg);
        pnlToggle.add(btnThang); pnlToggle.add(btnQuy); pnlToggle.add(btnTuyChinh);

        // CÁC Ô TÙY CHỌN THỜI GIAN
        JPanel pnlTimeOptions = new JPanel(new CardLayout());
        pnlTimeOptions.setOpaque(false);

        cboThang = new JComboBox<>(new String[]{"Cả năm", "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"});
        cboQuy = new JComboBox<>(new String[]{"Tất cả", "Quý 1 (T1-T3)", "Quý 2 (T4-T6)", "Quý 3 (T7-T9)", "Quý 4 (T10-T12)"});
        styleCombo(cboThang); styleCombo(cboQuy);

        JPanel pnlCustomDate = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlCustomDate.setOpaque(false);
        txtTuNgay = new JTextField("dd/mm/yyyy", 8); txtTuNgay.setEditable(false); txtTuNgay.setBackground(Color.WHITE);
        txtDenNgay = new JTextField("dd/mm/yyyy", 8); txtDenNgay.setEditable(false); txtDenNgay.setBackground(Color.WHITE);
        
        // GẮN SỰ KIỆN MỞ LỊCH CHO 2 Ô TEXTFIELD
        MouseAdapter openCal = new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JTextField txt = (JTextField) e.getSource();
                Window win = SwingUtilities.getWindowAncestor(ManHinhThongKe.this);
                new ModernDatePicker(win, txt).setVisible(true);
                onFilterChanged();
            }
        };
        txtTuNgay.addMouseListener(openCal); txtDenNgay.addMouseListener(openCal);

        pnlCustomDate.add(txtTuNgay); pnlCustomDate.add(new JLabel(" → ")); pnlCustomDate.add(txtDenNgay);

        pnlTimeOptions.add(cboThang, "THANG");
        pnlTimeOptions.add(cboQuy, "QUY");
        pnlTimeOptions.add(pnlCustomDate, "TUYCHINH");

        // SỰ KIỆN ĐỔI TAB (THÁNG / QUÝ / TÙY CHỈNH)
        CardLayout cl = (CardLayout) pnlTimeOptions.getLayout();
        btnThang.addActionListener(e -> { modeLocThoiGian="THANG"; cl.show(pnlTimeOptions, "THANG"); btnThang.setBackground(activeBg); btnThang.setForeground(activeFg); btnQuy.setBackground(inactiveBg); btnQuy.setForeground(inactiveFg); btnTuyChinh.setBackground(inactiveBg); btnTuyChinh.setForeground(inactiveFg); onFilterChanged(); });
        btnQuy.addActionListener(e -> { modeLocThoiGian="QUY"; cl.show(pnlTimeOptions, "QUY"); btnQuy.setBackground(activeBg); btnQuy.setForeground(activeFg); btnThang.setBackground(inactiveBg); btnThang.setForeground(inactiveFg); btnTuyChinh.setBackground(inactiveBg); btnTuyChinh.setForeground(inactiveFg); onFilterChanged(); });
        btnTuyChinh.addActionListener(e -> { modeLocThoiGian="TUYCHINH"; cl.show(pnlTimeOptions, "TUYCHINH"); btnTuyChinh.setBackground(activeBg); btnTuyChinh.setForeground(activeFg); btnThang.setBackground(inactiveBg); btnThang.setForeground(inactiveFg); btnQuy.setBackground(inactiveBg); btnQuy.setForeground(inactiveFg); });

        // SỰ KIỆN CHỌN COMBOBOX
        cboThang.addActionListener(e -> onFilterChanged());
        cboQuy.addActionListener(e -> onFilterChanged());
        cboKyLoc = cboThang; 

        // GẮN SỰ KIỆN CHO NÚT NĂM
        btnNamPicker = new JButton("📅 " + currentYear + " ▼");
        btnNamPicker.addActionListener(e -> showYearCalendarPopup(btnNamPicker)); 
        
        // GẮN SỰ KIỆN DƯỢC SĨ
        cboNhanVien = new JComboBox<>(new String[]{"Tất cả"});
        styleCombo(cboNhanVien);
        cboNhanVien.addActionListener(e -> applyNVFilter());

        pnlFilterDuocSi = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pnlFilterDuocSi.setOpaque(false);
        pnlFilterDuocSi.add(new JLabel("| Dược sĩ:")); pnlFilterDuocSi.add(cboNhanVien);
        pnlFilterDuocSi.setVisible(false); 

        p.add(btnDT); p.add(btnNV); p.add(new JLabel(" | "));
        p.add(pnlToggle); p.add(pnlTimeOptions); p.add(btnNamPicker); p.add(pnlFilterDuocSi);

        return p;
    }

    // Calendar/Year picker popup
    private void showYearCalendarPopup(JButton source) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBackground(Color.WHITE);
        popup.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(0, 0, 0, 0)));

        JLabel header = new JLabel("  Chọn năm thống kê", SwingConstants.LEFT);
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
            if (yr == currentYear) {
                btn.setBackground(Color.decode("#1A73E8"));
                btn.setForeground(Color.WHITE);
            } else if (yr == now) {
                btn.setBackground(Color.decode("#EEF2FF"));
                btn.setForeground(Color.decode("#1A73E8"));
            } else {
                btn.setBackground(Color.decode("#F8F9FA"));
                btn.setForeground(Color.decode("#333333"));
            }
            btn.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true));
            btn.addActionListener(e -> {
                currentYear = yr;
                btnNamPicker.setText("📅 " + currentYear + " ▼");
                popup.setVisible(false);
                onFilterChanged();
            });
            grid.add(btn);
        }

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.add(header, BorderLayout.NORTH);
        content.add(grid,   BorderLayout.CENTER);
        popup.add(content);
        popup.setPreferredSize(new Dimension(240, 200));
        popup.show(source, 0, source.getHeight() + 4);
    }

    private void styleCombo(JComboBox<?> cbo) {
        cbo.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true));
        cbo.setBackground(Color.WHITE);
    }

    // TAB 1: DOANH THU & SẢN PHẨM
    private JPanel buildViewDT() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);

        kpiDTVal = makeKpiVal("...", "#1A73E8"); kpiDTSub = makeKpiSub("Đang tải...");
        kpiHDVal = makeKpiVal("...", "#9C27B0"); kpiHDSub = makeKpiSub("...");
        kpiLNVal = makeKpiVal("...", "#00A76F"); kpiLNSub = makeKpiSub("...");
        kpiTBVal = makeKpiVal("...", "#FF9800"); kpiTBSub = makeKpiSub("...");

        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        cards.add(statCardDynamic("Tổng doanh thu",    kpiDTVal, kpiDTSub, "📈", "#EEF2FF"));
        cards.add(statCardDynamic("Số lượng đơn hàng", kpiHDVal, kpiHDSub, "🛒", "#F3E5F5"));
        cards.add(statCardDynamic("Lợi nhuận",         kpiLNVal, kpiLNSub, "💰", "#E8F5E9"));
        cards.add(statCardDynamic("Giá trị TB / đơn",  kpiTBVal, kpiTBSub, "🎁", "#FFF3E0"));
        root.add(cards);
        root.add(Box.createVerticalStrut(12));

        chartBarMain = new BarChartMain();
        JPanel cBarCard = wrapChart("Doanh thu – Chi phí – Lợi nhuận",
            "12 tháng · " + currentYear, chartBarMain, 280);
        cBarCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
        root.add(cBarCard);
        root.add(Box.createVerticalStrut(12));

        JPanel row2 = new JPanel(new GridLayout(1, 2, 12, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 310));

        chartLineDaily = new LineChartDaily();
        
        JPanel lineCard = new JPanel(new BorderLayout(0, 8));
        lineCard.setBackground(Color.WHITE);
        lineCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(14, 14, 14, 14)));
        JLabel ltitle = new JLabel("📅 Thống kê theo ngày (30 ngày gần nhất)");
        ltitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ltitle.setForeground(Color.decode("#152A4B"));
        miniPeakDate = new JLabel("--"); miniPeakDate.setFont(new Font("Segoe UI",Font.BOLD,16)); miniPeakDate.setForeground(Color.decode("#1A73E8"));
        miniAvgDT    = new JLabel("--"); miniAvgDT.setFont(new Font("Segoe UI",Font.BOLD,16));    miniAvgDT.setForeground(Color.decode("#00A76F"));
        miniAvgOrder = new JLabel("--"); miniAvgOrder.setFont(new Font("Segoe UI",Font.BOLD,16)); miniAvgOrder.setForeground(Color.decode("#FF9800"));
        JPanel miniStats = new JPanel(new GridLayout(1, 3, 8, 0));
        miniStats.setOpaque(false);
        miniStats.add(miniStatInlineDynamic("Ngày cao nhất", miniPeakDate, "--",         "#1A73E8", "#E3F2FD"));
        miniStats.add(miniStatInlineDynamic("DT TB / ngày",  miniAvgDT,   "bình quân",  "#00A76F", "#E8F5E9"));
        miniStats.add(miniStatInlineDynamic("Đơn TB / ngày", miniAvgOrder,"đơn / ngày", "#FF9800", "#FFF3E0"));
        lineCard.add(ltitle,         BorderLayout.NORTH);
        lineCard.add(miniStats,      BorderLayout.CENTER);
        lineCard.add(chartLineDaily, BorderLayout.SOUTH);
        chartLineDaily.setPreferredSize(new Dimension(0, 160));
        row2.add(lineCard);

        chartDonut = new DonutChart();
        row2.add(wrapChart("Doanh thu theo loại SP", "Cơ cấu sản phẩm", chartDonut, 200));
        root.add(row2);
        root.add(Box.createVerticalStrut(12));

        JPanel row3 = new JPanel(new GridLayout(1, 2, 12, 0));
        row3.setOpaque(false);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        row3.add(buildTopSPTable());
        row3.add(buildVATTable());
        root.add(row3);
        root.add(Box.createVerticalStrut(12));
        
        JPanel row4 = new JPanel(new BorderLayout());
        row4.setOpaque(false);
        row4.add(buildSpSapHetHanTable(), BorderLayout.CENTER);
        root.add(row4);
        root.add(Box.createVerticalStrut(12));

        JScrollPane sp = makeScrollPane(root);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(sp, BorderLayout.CENTER);
        return wrapper;
    }

    // TAB 2: NHÂN VIÊN
    private JPanel buildViewNV() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);

        lblTongNV = makeKpiVal("...", "#1A73E8");
        lblDTTB_NV = makeKpiVal("...", "#9C27B0");
        lblTongDT_NV = makeKpiVal("...", "#00A76F");

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        cards.add(statCardDynamic("Tổng nhân viên", lblTongNV, makeKpiSub("đang hoạt động · " + currentYear), "👥", "#EEF2FF"));
        cards.add(statCardDynamic("DT trung bình / NV", lblDTTB_NV, makeKpiSub("mỗi nhân viên"), "📈", "#F3E5F5"));
        cards.add(statCardDynamic("Tổng doanh thu NV", lblTongDT_NV, makeKpiSub("tất cả nhân viên"), "📈", "#E8F5E9"));
        root.add(cards);
        root.add(Box.createVerticalStrut(12));

        chartNVDaily = new NVDailyChart();
        JPanel nvDailyCard = new JPanel(new BorderLayout(0, 8));
        nvDailyCard.setBackground(Color.WHITE);
        nvDailyCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(14, 14, 10, 14)));
        nvDailyCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JPanel nvDailyHeader = new JPanel(new BorderLayout());
        nvDailyHeader.setOpaque(false);
        JPanel nvDailyTitles = new JPanel(new GridLayout(2, 1, 0, 2));
        nvDailyTitles.setOpaque(false);
        JLabel ndTitle = new JLabel("📅 Thống kê dược sĩ theo ngày (10 ngày gần nhất)");
        ndTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ndTitle.setForeground(Color.decode("#152A4B"));
        JLabel ndSub = new JLabel("Số hóa đơn xử lý mỗi ngày theo từng dược sĩ");
        ndSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ndSub.setForeground(Color.decode("#888888"));
        nvDailyTitles.add(ndTitle); nvDailyTitles.add(ndSub);
        nvDailyHeader.add(nvDailyTitles, BorderLayout.WEST);

        chartNVDaily.setPreferredSize(new Dimension(0, 200));
        nvDailyCard.add(nvDailyHeader, BorderLayout.NORTH);
        nvDailyCard.add(chartNVDaily,  BorderLayout.CENTER);
        nvDailyCard.add(buildNVDailyLegend(), BorderLayout.SOUTH);
        root.add(nvDailyCard);
        root.add(Box.createVerticalStrut(12));

        JPanel row2 = new JPanel(new GridLayout(1, 2, 12, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));

        JPanel shiftCard = new JPanel(new BorderLayout(0, 8));
        shiftCard.setBackground(Color.WHITE);
        shiftCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(14, 14, 10, 14)));
        JLabel shTitle = new JLabel("⏱ HĐ theo ca làm việc");
        shTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        shTitle.setForeground(Color.decode("#152A4B"));

        lblHDSang = makeKpiVal("...", "#F59E0B");
        lblHDChieu = makeKpiVal("...", "#6366F1");

        JPanel shMini = new JPanel(new GridLayout(1, 2, 8, 0));
        shMini.setOpaque(false);
        shMini.add(statCardDynamic("Ca sáng (6h–13h)", lblHDSang, makeKpiSub("Hóa đơn"), "☀️", "#FFF8E1"));
        shMini.add(statCardDynamic("Ca chiều (14h–21h)", lblHDChieu, makeKpiSub("Hóa đơn"), "🌙", "#EEF2FF"));

        chartNVShift = new NVShiftChart();
        chartNVShift.setPreferredSize(new Dimension(0, 160));

        JPanel shLegend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        shLegend.setOpaque(false);
        shLegend.add(legendDot(Color.decode("#FFAB00"), "Ca sáng"));
        shLegend.add(legendDot(Color.decode("#4F46E5"), "Ca chiều"));

        shiftCard.add(shTitle, BorderLayout.NORTH);
        JPanel shCenter = new JPanel(new BorderLayout(0, 6));
        shCenter.setOpaque(false);
        shCenter.add(shMini,       BorderLayout.NORTH);
        shCenter.add(chartNVShift, BorderLayout.CENTER);
        shCenter.add(shLegend,     BorderLayout.SOUTH);
        shiftCard.add(shCenter, BorderLayout.CENTER);
        row2.add(shiftCard);

        tblNVDetail = new NVDetailTable();
        row2.add(tblNVDetail);
        root.add(row2);
        root.add(Box.createVerticalStrut(12));

        root.add(Box.createVerticalStrut(8));

        JScrollPane sp = makeScrollPane(root);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(sp, BorderLayout.CENTER);
        return wrapper;
    }

    // PHÂN QUYỀN
    private void showAccessDenied() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(Color.decode("#F4F6F8"));
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#FFCDD2"), 2),
            new EmptyBorder(40, 60, 40, 60)));
        JLabel lblIcon  = new JLabel("🔒", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        lblIcon.setAlignmentX(CENTER_ALIGNMENT);
        JLabel lblTitle = new JLabel("Không có quyền truy cập", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.decode("#C62828"));
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);
        JLabel lblDesc  = new JLabel(
            "<html><center>Chức năng Thống kê chỉ dành cho <b>Quản lý</b>.<br>" +
            "Vui lòng liên hệ quản lý để được hỗ trợ.</center></html>",
            SwingConstants.CENTER);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDesc.setForeground(Color.decode("#637381"));
        lblDesc.setAlignmentX(CENTER_ALIGNMENT);
        card.add(lblIcon);
        card.add(Box.createRigidArea(new Dimension(0, 16)));
        card.add(lblTitle);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(lblDesc);
        pnl.add(card);
        add(pnl, BorderLayout.CENTER);
    }

    // SỰ KIỆN
    private void setupEvents() {
        btnDT.addActionListener(e -> {
            toggleTab(true);
            cardBody.show(pnlBody, "DT");
            pnlFilterDuocSi.setVisible(false);
        });
        btnNV.addActionListener(e -> {
            toggleTab(false);
            cardBody.show(pnlBody, "NV");
            pnlFilterDuocSi.setVisible(true);
        });
    }

    private void onFilterChanged() {
        lblYearBadge.setText(getKyString() + " " + currentYear);
        loadDataFromDB(currentYear);
    }

    private void applyNVFilter() {
        int idx = cboNhanVien.getSelectedIndex() - 1;
        selectedNVIdx = idx;
        if (chartNVDaily != null) chartNVDaily.setFilter(idx);
        if (chartNVShift != null) chartNVShift.setFilter(idx);
        if (tblNVDetail  != null) tblNVDetail.setFilter(idx);
        repaint();
    }

    private void toggleTab(boolean isDT) {
        btnDT.setBackground(isDT  ? Color.decode("#E8F0FE") : Color.WHITE);
        btnDT.setForeground(isDT  ? Color.decode("#1A73E8") : Color.decode("#444444"));
        btnNV.setBackground(!isDT ? Color.decode("#F3E5F5") : Color.WHITE);
        btnNV.setForeground(!isDT ? Color.decode("#9C27B0") : Color.decode("#444444"));
    }

    // BIỂU ĐỒ CỘT CHÍNH (DT-CP-LN)
    class BarChartMain extends JPanel {
        private int   hoverIdx  = -1;
        private Point tooltipPt = null;

        BarChartMain() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(0, 260));
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    hoverIdx = -1; tooltipPt = null;
                    int barW = 16, gap = 4;
                    int groupW = barW * 3 + gap * 2 + 14;
                    int startX = 50, maxH = getHeight() - 60;
                    double maxVal = calcMaxVal();
                    for (int i = 0; i < 12; i++) {
                        int gx = startX + i * groupW;
                        for (int b = 0; b < 3; b++) {
                            int bx  = gx + b * (barW + gap);
                            double val = (b==0)?DT_DATA[i]:(b==1)?CP_DATA[i]:Math.max(0, DT_DATA[i]-CP_DATA[i]);
                            int bh  = maxVal>0 ? (int)(val/maxVal*maxH) : 0;
                            int by  = getHeight() - 35 - bh;
                            if (e.getX()>=bx && e.getX()<=bx+barW && e.getY()>=by && e.getY()<=by+bh) {
                                hoverIdx = i; tooltipPt = e.getPoint(); break;
                            }
                        }
                        if (hoverIdx >= 0) break;
                    }
                    repaint();
                }
                @Override public void mouseExited(MouseEvent e) { hoverIdx=-1; tooltipPt=null; repaint(); }
            });
        }

        private double calcMaxVal() {
            double max = 10.0;
            for (double v : DT_DATA) if (v > max) max = v;
            return max * 1.2;
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int barW = 16, gap = 4, groupW = barW*3 + gap*2 + 14;
            int startX = 50, baseY = h-35, maxH = h-60;
            double maxVal = calcMaxVal();
            Color[] barColors = {Color.decode("#1A73E8"), Color.decode("#FFAB00"), Color.decode("#00A76F")};

            // Grid lines (Dùng double để vẽ mượt hơn)
            g2.setStroke(new BasicStroke(0.5f));
            for (double v = 0; v <= maxVal; v += Math.max(1.0, maxVal/4.0)) {
                int y = baseY - (int)(v/maxVal*maxH);
                g2.setColor(Color.decode("#F0F0F0")); g2.drawLine(startX, y, w-10, y);
                g2.setColor(Color.decode("#AAAAAA")); g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
                g2.drawString(String.format("%.1fM", v), 4, y+4);
            }

            for (int i = 0; i < 12; i++) {
                int gx = startX + i*groupW;
                double[] vals = {DT_DATA[i], CP_DATA[i], Math.max(0, DT_DATA[i]-CP_DATA[i])};
                for (int b = 0; b < 3; b++) {
                    int bh = maxVal>0 ? (int)(vals[b]/maxVal*maxH) : 0;
                    if (bh <= 0) continue;
                    int bx = gx + b*(barW+gap);
                    int by = baseY - bh;
                    Color c = barColors[b];
                    if (i == hoverIdx) {
                        float[] hsb = Color.RGBtoHSB(c.getRed(),c.getGreen(),c.getBlue(),null);
                        c = Color.getHSBColor(hsb[0], hsb[1], Math.min(1f, hsb[2]*1.2f));
                    }
                    g2.setColor(c);
                    g2.fillRoundRect(bx, by, barW, bh, 3, 3);
                }
                g2.setColor(Color.decode("#888888")); g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
                g2.drawString(THANG[i], gx+4, h-16);
            }

            // Legend
            String[] legends = {"Doanh thu","Chi phí","Lợi nhuận"};
            int lx = startX;
            for (int b = 0; b < 3; b++) {
                g2.setColor(barColors[b]); g2.fillRoundRect(lx, 8, 10, 10, 3, 3);
                g2.setColor(Color.decode("#555555")); g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
                g2.drawString(legends[b], lx+14, 18);
                lx += 80;
            }

            // Tooltip (Hiển thị số thập phân)
            if (hoverIdx >= 0 && tooltipPt != null) {
                int i = hoverIdx;
                String txt = String.format("T%d  DT:%.1fM | CP:%.1fM | LN:%.1fM", i+1, DT_DATA[i], CP_DATA[i], DT_DATA[i]-CP_DATA[i]);
                drawTooltip(g2, Math.min(tooltipPt.x+10, w-200), tooltipPt.y-30, txt);
            }
        }
    }

    // BIỂU ĐỒ ĐƯỜNG (DAILY)
    class LineChartDaily extends JPanel {
        private final double[] vals;
        private int   hoverIdx  = -1;
        private Point tooltipPt = null;

        LineChartDaily() {
            setBackground(Color.WHITE);
            vals = new double[30];
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    int n = vals.length; float step = (float)(getWidth()-40)/(n-1);
                    hoverIdx = -1; tooltipPt = null;
                    for (int i = 0; i < n; i++) {
                        int px = 20+(int)(i*step);
                        int py = getHeight()-20-(int)((vals[i]/4.5)*(getHeight()-30));
                        if (Math.abs(e.getX()-px)<10 && Math.abs(e.getY()-py)<10) {
                            hoverIdx=i; tooltipPt=e.getPoint(); break;
                        }
                    }
                    repaint();
                }
                @Override public void mouseExited(MouseEvent e) { hoverIdx=-1; repaint(); }
            });
        }
        void setData(double[] newVals, String[] dates) {
            java.util.Arrays.fill(vals, 0);
            for (int i=0; i<Math.min(newVals.length, vals.length); i++) vals[i]=newVals[i];
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int n = vals.length, w = getWidth(), h = getHeight();
            float step = (float)(w-40)/(n-1);

            GeneralPath area = new GeneralPath();
            area.moveTo(20, h-20);
            for (int i = 0; i < n; i++) {
                int px = 20+(int)(i*step);
                int py = h-20-(int)((vals[i]/4.5)*(h-30));
                area.lineTo(px, py);
            }
            area.lineTo(20+(int)((n-1)*step), h-20);
            area.closePath();
            g2.setColor(new Color(26,115,232,25)); g2.fill(area);

            g2.setColor(Color.decode("#1A73E8")); g2.setStroke(new BasicStroke(2f));
            int[] xs = new int[n], ys = new int[n];
            for (int i = 0; i < n; i++) {
                xs[i] = 20+(int)(i*step);
                ys[i] = h-20-(int)((vals[i]/4.5)*(h-30));
            }
            for (int i = 0; i < n-1; i++) g2.drawLine(xs[i], ys[i], xs[i+1], ys[i+1]);
            for (int i = 0; i < n; i++) {
                if (i == hoverIdx) {
                    g2.setColor(Color.decode("#1A73E8")); g2.fillOval(xs[i]-5,ys[i]-5,10,10);
                    g2.setColor(Color.WHITE);             g2.fillOval(xs[i]-3,ys[i]-3, 6, 6);
                } else {
                    g2.setColor(Color.decode("#1A73E8")); g2.fillOval(xs[i]-2,ys[i]-2, 5, 5);
                }
            }
            g2.setColor(Color.decode("#AAAAAA")); g2.setFont(new Font("Segoe UI",Font.PLAIN,9));
            for (int li : new int[]{0,4,9,14,19,24,29}) {
                g2.drawString((li+1<10?"0":"")+(li+1)+"/", xs[li]-8, h-4);
            }
            if (hoverIdx >= 0 && tooltipPt != null) {
                drawTooltip(g2, Math.min(tooltipPt.x+8, w-160), tooltipPt.y-28,
                    String.format("Ngày %02d: %.1fM đ", hoverIdx+1, vals[hoverIdx]));
            }
        }
    }

    // BIỂU ĐỒ DONUT
    class DonutChart extends JPanel {
        private int   hoverIdx  = -1;
        private Point tooltipPt = null;

        DonutChart() {
            setBackground(Color.WHITE);
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    double cx = getWidth()/2.0, cy = getHeight()/2.0;
                    double dist = Math.hypot(e.getX()-cx, e.getY()-cy);
                    int r = Math.min(getWidth(),getHeight())/2-10, inner = r-50;
                    hoverIdx = -1; tooltipPt = null;
                    if (dist >= inner && dist <= r) {
                        double angle = Math.toDegrees(Math.atan2(-(e.getY()-cy), e.getX()-cx));
                        if (angle < 0) angle += 360;
                        double cur = 0, total = 0;
                        for (int v : DONUT_VALS) total += v;
                        for (int i = 0; i < DONUT_VALS.length; i++) {
                            double sweep = DONUT_VALS[i]/total*360;
                            if (angle >= cur && angle < cur+sweep) { hoverIdx=i; tooltipPt=e.getPoint(); break; }
                            cur += sweep;
                        }
                    }
                    repaint();
                }
                @Override public void mouseExited(MouseEvent e) { hoverIdx=-1; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int size = Math.min(w,h)-40, x=(w-size)/2, y=(h-size)/2;
            double total = 0; for (int v : DONUT_VALS) total += v;
            int startAngle = 0;
            for (int i = 0; i < DONUT_VALS.length; i++) {
                int sweep = (int)Math.round(DONUT_VALS[i]/total*360);
                Color c = DONUT_COLORS[i];
                if (i == hoverIdx) {
                    float[] hsb = Color.RGBtoHSB(c.getRed(),c.getGreen(),c.getBlue(),null);
                    c = Color.getHSBColor(hsb[0], hsb[1], Math.min(1f, hsb[2]*1.15f));
                    g2.setColor(c); g2.fillArc(x-5,y-5,size+10,size+10,startAngle,sweep);
                } else {
                    g2.setColor(c); g2.fillArc(x,y,size,size,startAngle,sweep);
                }
                double mid = Math.toRadians(startAngle + sweep/2.0);
                int lx = (int)(x+size/2.0+(size/2.0-35)*Math.cos(mid));
                int ly = (int)(y+size/2.0-(size/2.0-35)*Math.sin(mid));
                g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI",Font.BOLD,11));
                g2.drawString((int)Math.round(DONUT_VALS[i]/total*100)+"%", lx-10, ly+4);
                startAngle += sweep;
            }
            g2.setColor(Color.WHITE);
            int hole = size-100;
            g2.fillOval(x+50, y+50, hole, hole);
            
            int ly2 = y + size + 8;
            g2.setFont(new Font("Segoe UI",Font.PLAIN,10));
            int lx2 = x;
            for (int i = 0; i < 4; i++) {
                if (lx2 + 80 > w) { lx2 = x; ly2 += 16; }
                g2.setColor(DONUT_COLORS[i]); g2.fillRoundRect(lx2,ly2,10,10,3,3);
                g2.setColor(Color.decode("#555555")); g2.drawString(DONUT_LABELS[i], lx2+14, ly2+10);
                lx2 += 90;
            }
            if (hoverIdx >= 0 && tooltipPt != null) {
                drawTooltip(g2, tooltipPt.x+8, tooltipPt.y-28,
                    DONUT_LABELS[hoverIdx] + ": " + (int)Math.round(DONUT_VALS[hoverIdx]/total*100) + "%");
            }
        }
    }

    // BIỂU ĐỒ CỘT DƯỢC SĨ THEO NGÀY
    class NVDailyChart extends JPanel {
        private int   filterIdx = -1;
        private int   hoverDay  = -1;
        private Point tooltipPt = null;

        NVDailyChart() {
            setBackground(Color.WHITE);
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    hoverDay = -1; tooltipPt = null;
                    int n = DATES_10.length; if (n == 0) return;
                    int startX = 45, groupW = (getWidth()-startX-10)/n;
                    for (int d = 0; d < n; d++) {
                        if (e.getX() >= startX+d*groupW && e.getX() < startX+(d+1)*groupW) {
                            hoverDay=d; tooltipPt=e.getPoint(); break;
                        }
                    }
                    repaint();
                }
                @Override public void mouseExited(MouseEvent e) { hoverDay=-1; repaint(); }
            });
        }

        void setFilter(int idx) { this.filterIdx=idx; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int n = DATES_10.length; if (n == 0) { drawEmpty(g2, w, h); return; }
            int startX = 45, baseY = h-30, maxH = h-45;
            int groupW = (w-startX-10)/n;
            int maxVal = 16;
            for (int i = 0; i < n; i++) for (int j = 0; j < NV_DAILY.length; j++)
                if (j < NV_DAILY.length && i < NV_DAILY[j].length && NV_DAILY[j][i] > maxVal) maxVal = NV_DAILY[j][i];
            maxVal = (int)(maxVal * 1.2) + 1;

            g2.setStroke(new BasicStroke(0.5f));
            for (int v = 0; v <= maxVal; v += Math.max(1, maxVal/4)) {
                int y = baseY-(int)((double)v/maxVal*maxH);
                g2.setColor(Color.decode("#F0F0F0")); g2.drawLine(startX, y, w-10, y);
                g2.setColor(Color.decode("#AAAAAA")); g2.setFont(new Font("Segoe UI",Font.PLAIN,9));
                g2.drawString(String.valueOf(v), 4, y+3);
            }
            g2.setColor(Color.decode("#AAAAAA")); g2.drawString("HĐ", 4, 15);

            int nvCount = (filterIdx>=0)?1:NV_NAMES.length;
            int barW = Math.max(5, (groupW-8)/Math.max(1, nvCount));

            for (int d = 0; d < n; d++) {
                int gx = startX + d*groupW + 3;
                boolean isHover = (d == hoverDay);
                int bIdx = 0;
                for (int nv = 0; nv < NV_NAMES.length; nv++) {
                    if (filterIdx >= 0 && nv != filterIdx) continue;
                    if (nv >= NV_DAILY.length || d >= NV_DAILY[nv].length) { bIdx++; continue; }
                    int val = NV_DAILY[nv][d];
                    int bh  = (int)((double)val/maxVal*maxH);
                    if (bh <= 0) { bIdx++; continue; }
                    int bx = gx + bIdx*barW;
                    Color c = NV_COLORS[nv];
                    if (isHover) { float[] hsb=Color.RGBtoHSB(c.getRed(),c.getGreen(),c.getBlue(),null); c=Color.getHSBColor(hsb[0],hsb[1],Math.min(1f,hsb[2]*1.2f)); }
                    g2.setColor(c); g2.fillRoundRect(bx, baseY-bh, barW-1, bh, 3, 3);
                    bIdx++;
                }
                g2.setColor(Color.decode("#888888")); g2.setFont(new Font("Segoe UI",Font.PLAIN,9));
                String dateLabel = DATES_10[d].length() > 5 ? DATES_10[d].substring(0,5) : DATES_10[d];
                g2.drawString(dateLabel, gx, h-12);
            }

            if (hoverDay >= 0 && tooltipPt != null) {
                int d = hoverDay;
                StringBuilder sb = new StringBuilder("Ngày " + DATES_10[d] + ": ");
                if (filterIdx >= 0 && filterIdx < NV_SHORT.length) {
                    sb.append(NV_SHORT[filterIdx]).append("=").append(d<NV_DAILY[filterIdx].length?NV_DAILY[filterIdx][d]:0).append(" HĐ");
                } else {
                    for (int nv = 0; nv < NV_NAMES.length; nv++) {
                        if (nv > 0) sb.append("  ");
                        sb.append(nv<NV_SHORT.length?NV_SHORT[nv]:"NV"+nv).append("=")
                          .append(nv<NV_DAILY.length&&d<NV_DAILY[nv].length?NV_DAILY[nv][d]:0);
                    }
                }
                drawTooltip(g2, Math.min(tooltipPt.x+8, w-180), tooltipPt.y-28, sb.toString());
            }
        }

        private void drawEmpty(Graphics2D g2, int w, int h) {
            g2.setColor(Color.decode("#CCCCCC"));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            String s = "Đang tải dữ liệu...";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(s, (w-fm.stringWidth(s))/2, h/2);
        }
    }

    // BIỂU ĐỒ CỘT CA LÀM VIỆC
    class NVShiftChart extends JPanel {
        private int   filterIdx = -1;
        private int   hoverIdx  = -1;
        private Point tooltipPt = null;

        NVShiftChart() {
            setBackground(Color.WHITE);
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    hoverIdx = -1; tooltipPt = null;
                    int n = (filterIdx>=0)?1:NV_NAMES.length; if (n==0) return;
                    int startX=30, groupW=(getWidth()-startX-10)/n;
                    for (int i = 0; i < n; i++) {
                        if (e.getX()>=startX+i*groupW && e.getX()<startX+(i+1)*groupW) { hoverIdx=i; tooltipPt=e.getPoint(); break; }
                    }
                    repaint();
                }
                @Override public void mouseExited(MouseEvent e) { hoverIdx=-1; repaint(); }
            });
        }

        void setFilter(int idx) { this.filterIdx=idx; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w=getWidth(), h=getHeight();
            int[] nvIdxs = buildNVIdxs();
            int n = nvIdxs.length; if (n==0) return;
            int maxVal = 10;
            for (int nv : nvIdxs) if (nv < NV_HD_S.length) { if(NV_HD_S[nv]>maxVal)maxVal=NV_HD_S[nv]; if(NV_HD_C[nv]>maxVal)maxVal=NV_HD_C[nv]; }
            maxVal = (int)(maxVal*1.2)+1;
            int startX=30, baseY=h-28, maxH=h-40;
            int groupW=(w-startX-10)/n, barW=Math.max(8,(groupW-6)/2);

            for (int v=0; v<=maxVal; v+=Math.max(1,maxVal/4)) {
                int y=baseY-(int)((double)v/maxVal*maxH);
                g2.setColor(Color.decode("#F0F0F0")); g2.setStroke(new BasicStroke(0.5f)); g2.drawLine(startX, y, w-5, y);
                g2.setColor(Color.decode("#AAAAAA")); g2.setFont(new Font("Segoe UI",Font.PLAIN,9)); g2.drawString(String.valueOf(v), 2, y+3);
            }

            for (int i = 0; i < n; i++) {
                int nv  = nvIdxs[i];
                int gx  = startX + i*groupW + 2;
                boolean hov = (i==hoverIdx);
                int shS = nv<NV_HD_S.length?NV_HD_S[nv]:0, shC = nv<NV_HD_C.length?NV_HD_C[nv]:0;
                int hS  = (int)((double)shS/maxVal*maxH), hC = (int)((double)shC/maxVal*maxH);
                Color cS = hov?Color.decode("#FFD54F"):Color.decode("#FFAB00");
                Color cC = hov?Color.decode("#7986CB"):Color.decode("#4F46E5");
                if (hS>0) { g2.setColor(cS); g2.fillRoundRect(gx, baseY-hS, barW, hS, 3, 3); }
                if (hC>0) { g2.setColor(cC); g2.fillRoundRect(gx+barW+2, baseY-hC, barW, hC, 3, 3); }
                g2.setColor(Color.decode("#888888")); g2.setFont(new Font("Segoe UI",Font.PLAIN,9));
                g2.drawString(nv<NV_SHORT.length?NV_SHORT[nv]:"NV"+(nv+1), gx+2, h-12);
            }

            if (hoverIdx>=0 && tooltipPt!=null) {
                int nv = nvIdxs[hoverIdx];
                String txt = String.format("%s  Sáng:%d HĐ | Chiều:%d HĐ",
                    nv<NV_NAMES.length?NV_NAMES[nv]:"NV"+(nv+1),
                    nv<NV_HD_S.length?NV_HD_S[nv]:0, nv<NV_HD_C.length?NV_HD_C[nv]:0);
                drawTooltip(g2, Math.min(tooltipPt.x+8, w-220), tooltipPt.y-28, txt);
            }
        }

        private int[] buildNVIdxs() {
            if (filterIdx >= 0) return new int[]{filterIdx};
            int[] arr = new int[NV_NAMES.length];
            for (int i=0;i<arr.length;i++) arr[i]=i;
            return arr;
        }
    }

    // BẢNG CHI TIẾT NHÂN VIÊN THEO CA
    class NVDetailTable extends JPanel {
        private int filterIdx = -1;
        private JTable tbl;
        private DefaultTableModel model;

        NVDetailTable() {
            setLayout(new BorderLayout(0, 6));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(14, 14, 10, 14)));

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            JLabel t1 = new JLabel("⏰ Chi tiết dược sĩ theo ca");
            t1.setFont(new Font("Segoe UI", Font.BOLD, 13));
            t1.setForeground(Color.decode("#152A4B"));
            JTextField search = new JTextField("🔍 Tìm nhân viên...");
            search.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            search.setPreferredSize(new Dimension(150, 26));
            search.setForeground(Color.GRAY);
            search.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true));
            header.add(t1,     BorderLayout.WEST);
            header.add(search, BorderLayout.EAST);
            add(header, BorderLayout.NORTH);

            String[] cols = {"Nhân viên","HĐ Sáng","DT Sáng","HĐ Chiều","DT Chiều","T.HĐ","T.DT"};
            model = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            tbl = new JTable(model);
            tbl.setRowHeight(34);
            tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
            tbl.getTableHeader().setBackground(Color.decode("#EEF2FF"));
            tbl.getTableHeader().setForeground(Color.decode("#555555"));
            tbl.setShowHorizontalLines(true);
            tbl.setGridColor(Color.decode("#F0F0F0"));
            tbl.setSelectionBackground(Color.decode("#F0F4FF"));
            tbl.getColumnModel().getColumn(1).setCellRenderer(new ShiftRenderer(Color.decode("#D97706")));
            tbl.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer(){{ setHorizontalAlignment(RIGHT); }});
            tbl.getColumnModel().getColumn(3).setCellRenderer(new ShiftRenderer(Color.decode("#4F46E5")));
            tbl.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer(){{ setHorizontalAlignment(RIGHT); }});
            tbl.getColumnModel().getColumn(5).setCellRenderer(new ShiftRenderer(Color.decode("#152A4B")));
            tbl.getColumnModel().getColumn(6).setCellRenderer(new DTRenderer());

            refreshData();
            JScrollPane sp = new JScrollPane(tbl);
            sp.setBorder(BorderFactory.createEmptyBorder());
            sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
            add(sp, BorderLayout.CENTER);
        }

        void setFilter(int idx) { this.filterIdx=idx; refreshData(); }

        void refreshData() {
            model.setRowCount(0);
            double totalDTS=0, totalDTC=0; int totalHDS=0, totalHDC=0;
            for (int i = 0; i < NV_NAMES.length; i++) {
                if (filterIdx>=0 && i!=filterIdx) continue;
                totalHDS+=NV_HD_S[i]; totalHDC+=NV_HD_C[i];
                totalDTS+=NV_DT_S[i]; totalDTC+=NV_DT_C[i];
                model.addRow(new Object[]{
                    NV_NAMES[i], NV_HD_S[i], String.format("%.1fM đ",NV_DT_S[i]),
                    NV_HD_C[i],  String.format("%.1fM đ",NV_DT_C[i]),
                    NV_HD_S[i]+NV_HD_C[i], String.format("%.1fM đ",NV_DT_S[i]+NV_DT_C[i])
                });
            }
            model.addRow(new Object[]{
                "TỔNG:", totalHDS, String.format("%.1fM đ",totalDTS),
                totalHDC, String.format("%.1fM đ",totalDTC),
                totalHDS+totalHDC, String.format("%.1fM đ",totalDTS+totalDTC)
            });
        }
    }

    static class ShiftRenderer extends DefaultTableCellRenderer {
        Color color;
        ShiftRenderer(Color c) { this.color=c; setHorizontalAlignment(CENTER); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t,v,s,f,r,c);
            setForeground(color); setFont(new Font("Segoe UI",Font.BOLD,12)); return this;
        }
    }
    static class DTRenderer extends DefaultTableCellRenderer {
        DTRenderer() { setHorizontalAlignment(RIGHT); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t,v,s,f,r,c);
            setForeground(Color.decode("#00A76F")); setFont(new Font("Segoe UI",Font.BOLD,12)); return this;
        }
    }

    // BẢNG TOP SẢN PHẨM (load từ DB)
    private JPanel buildTopSPTable() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(14, 14, 10, 14)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel t = new JLabel("🏆 Top sản phẩm bán chạy");
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(Color.decode("#152A4B"));
        header.add(t, BorderLayout.WEST);
        p.add(header, BorderLayout.NORTH);

        String[] cols = {"#","Sản phẩm","Loại","SL","Doanh thu"};
        modelTopSP = new DefaultTableModel(cols, 0)  {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        DefaultTableModel m = modelTopSP;

        JTable tbl = new JTable(m);
        tbl.setRowHeight(30);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tbl.getTableHeader().setBackground(Color.decode("#F0F4FF"));
        tbl.setShowHorizontalLines(true);
        tbl.setGridColor(Color.decode("#F0F0F0"));
        tbl.setSelectionBackground(Color.decode("#F0F4FF"));
        tbl.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t2, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = new JLabel(v!=null?v.toString():"", SwingConstants.CENTER);
                lbl.setOpaque(true); lbl.setFont(new Font("Segoe UI",Font.BOLD,10));
                String sv = v!=null?v.toString():"";
                switch(sv) {
                    case "TPCN":       lbl.setBackground(Color.decode("#E8F5E9")); lbl.setForeground(Color.decode("#2E7D32")); break;
                    case "Thuốc KĐ":  lbl.setBackground(Color.decode("#FCE4EC")); lbl.setForeground(Color.decode("#C62828")); break;
                    case "Thuốc KKĐ": lbl.setBackground(Color.decode("#E3F2FD")); lbl.setForeground(Color.decode("#1565C0")); break;
                    default:           lbl.setBackground(Color.decode("#FFF3E0")); lbl.setForeground(Color.decode("#E65100")); break;
                }
                return lbl;
            }
        });
        tbl.getColumnModel().getColumn(4).setCellRenderer(new DTRenderer());
        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        p.add(sp, BorderLayout.CENTER);
        return p;
    }
    
    private void reloadTopSP(String condHD, int year) {
        if (modelTopSP == null) return;
        modelTopSP.setRowCount(0);
        new Thread(() -> {
            java.util.List<Object[]> list = busThongKe.getTopSanPham(year, condHD);
            int rank = 1;
            for (Object[] item : list) {
                String ten     = (String) item[0];
                String danhMuc = (String) item[1];
                int    sl      = (int)    item[2];
                double dt      = (double) item[3];
                String catLabel = danhMuc == null ? "Khác" :
                    danhMuc.equals("THUOC_KE_DON")          ? "Thuốc KĐ"  :
                    danhMuc.equals("THUOC_KHONG_KE_DON")    ? "Thuốc KKĐ" :
                    danhMuc.equals("THUC_PHAM_CHUC_NANG")   ? "TPCN"       :
                    danhMuc.equals("MY_PHAM")                ? "Mỹ phẩm"   : "Khác";
                final int r2 = rank++;
                final Object[] row = {
                    r2==1?"🥇":r2==2?"🥈":r2==3?"🥉":String.valueOf(r2),
                    ten, catLabel, String.format("%,d", sl), formatM(dt)
                };
                SwingUtilities.invokeLater(() -> modelTopSP.addRow(row));
            }
        }).start();
    }

    // BẢNG VAT
    private JPanel buildVATTable() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(14, 14, 10, 14)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel t = new JLabel("📋 Báo cáo thuế VAT chi tiết");
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(Color.decode("#152A4B"));
        lblVATTotal = new JLabel("Tổng VAT: --đ");
        lblVATTotal.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblVATTotal.setForeground(Color.decode("#3730A3"));
        lblVATTotal.setBackground(Color.decode("#EEF2FF")); lblVATTotal.setOpaque(true);
        lblVATTotal.setBorder(new EmptyBorder(3, 10, 3, 10));
        header.add(t, BorderLayout.WEST); header.add(lblVATTotal, BorderLayout.EAST);
        p.add(header, BorderLayout.NORTH);

        String[] cols = {"Mã SP","Tên sản phẩm","Kê đơn","VAT (%)","Tiền thuế"};
        modelVAT = new DefaultTableModel(cols, 0)  {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        DefaultTableModel m = modelVAT;

        JTable tbl = new JTable(m);
        tbl.setRowHeight(30); tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tbl.getTableHeader().setBackground(Color.decode("#F5F7FA"));
        tbl.setShowHorizontalLines(true); tbl.setGridColor(Color.decode("#F0F0F0"));
        tbl.setSelectionBackground(Color.decode("#F0F4FF"));
        tbl.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t2, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t2,v,s,f,r,c);
                setForeground(Color.decode("#1A73E8")); setFont(new Font("Segoe UI",Font.BOLD,11)); return this;
            }
        });
        tbl.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t2, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = new JLabel(v!=null?v.toString():"", SwingConstants.CENTER); l.setOpaque(true);
                l.setFont(new Font("Segoe UI",Font.BOLD,10));
                if ("Có".equals(v!=null?v.toString():"")) { l.setBackground(Color.decode("#E8F5E9")); l.setForeground(Color.decode("#2E7D32")); }
                else { l.setBackground(Color.decode("#FAFAFA")); l.setForeground(Color.GRAY); }
                return l;
            }
        });
        tbl.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t2, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = new JLabel(v!=null?v.toString():"", SwingConstants.CENTER); l.setOpaque(true);
                l.setFont(new Font("Segoe UI",Font.BOLD,10));
                if ("10%".equals(v!=null?v.toString():"")) { l.setBackground(Color.decode("#FFF3E0")); l.setForeground(Color.decode("#B45309")); }
                else { l.setBackground(Color.decode("#EEF2FF")); l.setForeground(Color.decode("#3730A3")); }
                return l;
            }
        });
        tbl.getColumnModel().getColumn(4).setCellRenderer(new DTRenderer());
        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        p.add(sp, BorderLayout.CENTER);
        return p;
    }
    
    private void reloadVAT(String condHD, int year) {
        if (modelVAT == null) return;
        modelVAT.setRowCount(0);
        new Thread(() -> {
            java.util.List<Object[]> list = busThongKe.getVATReport(year, condHD);
            double[] totalVAT = {0};
            for (Object[] item : list) {
                String maSP    = (String) item[0];
                String ten     = (String) item[1];
                String danhMuc = (String) item[2];
                int    vat     = (int)    item[3];
                double thue    = (double) item[4];
                totalVAT[0] += thue;
                boolean keDon = "THUOC_KE_DON".equals(danhMuc);
                final Object[] row = {maSP, ten, keDon ? "Có" : "Không", vat + "%", formatM(thue)};
                SwingUtilities.invokeLater(() -> modelVAT.addRow(row));
            }
            final double tv = totalVAT[0];
            SwingUtilities.invokeLater(() -> { if (lblVATTotal != null) lblVATTotal.setText("Tổng VAT: " + formatM(tv)); });
        }).start();
    }

    // LEGEND CHO DƯỢC SĨ
    private JPanel buildNVDailyLegend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 2));
        p.setOpaque(false);
        for (int i = 0; i < NV_NAMES.length; i++) p.add(legendDot(NV_COLORS[i], NV_SHORT[i]));
        return p;
    }

    // HELPERS
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

    private JPanel statCardDynamic(String label, JLabel valLabel, JLabel subLabel, String icon, String iconBg) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(14, 16, 14, 16)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Color.decode("#888888"));
        JLabel ico = new JLabel(icon, SwingConstants.CENTER);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        ico.setOpaque(true);
        ico.setBackground(Color.decode(iconBg));
        ico.setPreferredSize(new Dimension(36, 36));
        top.add(lbl, BorderLayout.WEST); top.add(ico, BorderLayout.EAST);

        JPanel bottom = new JPanel(new GridLayout(2,1,0,2));
        bottom.setOpaque(false);
        bottom.add(valLabel); bottom.add(subLabel);

        p.add(top,    BorderLayout.NORTH);
        p.add(bottom, BorderLayout.CENTER);
        return p;
    }

    private void drawTooltip(Graphics2D g2, int x, int y, String text) {
        FontMetrics fm = g2.getFontMetrics(new Font("Segoe UI",Font.PLAIN,11));
        int tw = fm.stringWidth(text)+16, th = fm.getHeight()+10;
        g2.setFont(new Font("Segoe UI",Font.PLAIN,11));
        g2.setColor(new Color(21,42,75,220));
        g2.fillRoundRect(x, y, tw, th, 6, 6);
        g2.setColor(Color.WHITE);
        g2.drawString(text, x+8, y+th-7);
    }

    private JPanel wrapChart(String title, String sub, JComponent chart, int chartH) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(14, 14, 10, 14)));
        JLabel lt = new JLabel(title); lt.setFont(new Font("Segoe UI",Font.BOLD,13)); lt.setForeground(Color.decode("#152A4B"));
        JLabel ls = new JLabel(sub);   ls.setFont(new Font("Segoe UI",Font.PLAIN,11)); ls.setForeground(Color.decode("#888888"));
        JPanel titles = new JPanel(new GridLayout(2,1,0,2)); titles.setOpaque(false);
        titles.add(lt); titles.add(ls);
        chart.setPreferredSize(new Dimension(0, chartH));
        p.add(titles, BorderLayout.NORTH);
        p.add(chart,  BorderLayout.CENTER);
        return p;
    }

    private JPanel miniStatInlineDynamic(String label, JLabel valLabel, String sub, String color, String bgColor) {
        JPanel p = new JPanel(new GridLayout(3,1,0,1));
        p.setBackground(Color.decode(bgColor));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DDDDDD"),1,true), new EmptyBorder(6,10,6,10)));
        JLabel l1 = new JLabel(label); l1.setFont(new Font("Segoe UI",Font.PLAIN,10)); l1.setForeground(Color.decode("#888888"));
        JLabel l3 = new JLabel(sub);   l3.setFont(new Font("Segoe UI",Font.PLAIN,10)); l3.setForeground(Color.decode("#999999"));
        p.add(l1); p.add(valLabel); p.add(l3);
        return p;
    }

    private JLabel legendDot(Color c, String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(Color.decode("#555555"));
        l.setIcon(new Icon() {
            public void paintIcon(Component comp, Graphics g, int x, int y) { g.setColor(c); g.fillRoundRect(x,y,10,10,3,3); }
            public int getIconWidth()  { return 12; }
            public int getIconHeight() { return 12; }
        });
        return l;
    }

    private JButton makeTabBtn(String text, boolean active) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(6, 14, 6, 14)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(active ? Color.decode("#E8F0FE") : Color.WHITE);
        b.setForeground(active ? Color.decode("#1A73E8") : Color.decode("#444444"));
        if (active) b.setFont(new Font("Segoe UI",Font.BOLD,12));
        return b;
    }

    // XUẤT BÁO CÁO (Đã fix double formatter)
    private void xuatExcel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu báo cáo Excel (CSV)");
        fc.setSelectedFile(new File("BaoCaoThongKe_" + currentYear + "_" +
            new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date()) + ".csv"));
        fc.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv"))
            file = new File(file.getAbsolutePath()+".csv");
        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), "UTF-8"))) {
            pw.print('\uFEFF');
            pw.println("BÁO CÁO THỐNG KÊ - MYCARE PHARMACY");
            pw.println("Ngày xuất:," + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()));
            pw.println("Năm:," + currentYear);
            pw.println();
            pw.println("DOANH THU THEO THÁNG");
            pw.println("Tháng,Doanh thu (triệu đ),Chi phí (triệu đ),Lợi nhuận (triệu đ)");
            for (int i = 0; i < 12; i++) {
                double dt=DT_DATA[i], cp=CP_DATA[i];
                pw.println(THANG[i]+","+String.format("%.1f",dt)+","+String.format("%.1f",cp)+","+String.format("%.1f",dt-cp));
            }
            pw.println();
            pw.println("THỐNG KÊ NHÂN VIÊN");
            pw.println("Tên,Chức vụ,HĐ Ca Sáng,HĐ Ca Chiều,DT Sáng (M đ),DT Chiều (M đ),Tổng DT (M đ)");
            for (int i = 0; i < NV_NAMES.length; i++) {
                pw.println(NV_NAMES[i]+","+NV_ROLES[i]+","+NV_HD_S[i]+","+NV_HD_C[i]+","+
                    String.format("%.1f",NV_DT_S[i])+","+String.format("%.1f",NV_DT_C[i])+","+
                    String.format("%.1f",NV_DT_S[i]+NV_DT_C[i]));
            }
            JOptionPane.showMessageDialog(this, "✓ Xuất CSV thành công!\n"+file.getAbsolutePath(),
                "Xuất báo cáo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi xuất file: "+ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void xuatPDF() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Lưu báo cáo (HTML)");
        fc.setSelectedFile(new File("BaoCaoThongKe_" + currentYear + "_" +
            new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date()) + ".html"));
        fc.setFileFilter(new FileNameExtensionFilter("HTML Files (*.html)", "html"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".html"))
            file = new File(file.getAbsolutePath()+".html");
        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), "UTF-8"))) {
            pw.println("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
            pw.println("<title>Báo cáo thống kê " + currentYear + " - MYCARE PHARMACY</title>");
            pw.println("<style>body{font-family:Arial,sans-serif;margin:30px;} h1{color:#152A4B;} h2{color:#1A73E8;} table{border-collapse:collapse;width:100%;margin:10px 0;} th{background:#152A4B;color:white;padding:8px;} td{border:1px solid #ddd;padding:6px;text-align:right;} td:first-child{text-align:left;} .green{color:#00A76F;font-weight:bold;} @media print{button{display:none;}}</style>");
            pw.println("</head><body>");
            pw.println("<h1>🏥 BÁO CÁO THỐNG KÊ - MYCARE PHARMACY</h1>");
            pw.println("<p><b>Ngày xuất:</b> "+new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date())+" &nbsp;|&nbsp; <b>Năm:</b> "+currentYear+"</p>");
            pw.println("<button onclick='window.print()' style='background:#1A73E8;color:white;padding:8px 16px;border:none;cursor:pointer;border-radius:4px;'>🖨 In / Lưu PDF</button>");
            pw.println("<h2>Doanh thu theo tháng</h2>");
            pw.println("<table><tr><th>Tháng</th><th>Doanh thu (M đ)</th><th>Chi phí (M đ)</th><th>Lợi nhuận (M đ)</th></tr>");
            double tDT=0, tCP=0;
            for (int i=0;i<12;i++) { 
                tDT+=DT_DATA[i]; tCP+=CP_DATA[i]; 
                pw.println("<tr><td>"+THANG[i]+"</td><td>"+String.format("%.1f",DT_DATA[i])+"</td><td>"+String.format("%.1f",CP_DATA[i])+"</td><td class='green'>"+String.format("%.1f",DT_DATA[i]-CP_DATA[i])+"</td></tr>"); 
            }
            pw.println("<tr style='background:#f5f5f5;font-weight:bold;'><td>TỔNG</td><td>"+String.format("%.1f",tDT)+"</td><td>"+String.format("%.1f",tCP)+"</td><td class='green'>"+String.format("%.1f",tDT-tCP)+"</td></tr></table>");
            pw.println("<h2>Thống kê nhân viên</h2>");
            pw.println("<table><tr><th>Tên</th><th>Chức vụ</th><th>HĐ Sáng</th><th>HĐ Chiều</th><th>DT Sáng</th><th>DT Chiều</th><th>Tổng</th></tr>");
            for (int i=0;i<NV_NAMES.length;i++) pw.println("<tr><td>"+NV_NAMES[i]+"</td><td>"+NV_ROLES[i]+"</td><td>"+NV_HD_S[i]+"</td><td>"+NV_HD_C[i]+"</td><td>"+String.format("%.1f",NV_DT_S[i])+"M</td><td>"+String.format("%.1f",NV_DT_C[i])+"M</td><td class='green'>"+String.format("%.1f",NV_DT_S[i]+NV_DT_C[i])+"M</td></tr>");
            pw.println("</table></body></html>");
            try { java.awt.Desktop.getDesktop().open(file); } catch (Exception ignored) {}
            JOptionPane.showMessageDialog(this, "✓ Đã tạo báo cáo HTML!\nMở trình duyệt → Ctrl+P để in/lưu PDF.\n"+file.getAbsolutePath(),
                "Xuất báo cáo", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: "+ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // BẢNG SẢN PHẨM HẾT HẠN
    private JPanel buildSpSapHetHanTable() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(14, 14, 10, 14)));

        JLabel t = new JLabel("⚠️ Sản phẩm sắp hết hạn (Dưới 6 tháng)");
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(Color.decode("#D32F2F"));
        p.add(t, BorderLayout.NORTH);

        String[] cols = {"Mã Lô", "Tên SP", "Kho", "SL Tồn", "Ngày hết hạn"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // Load dữ liệu qua BUS (không truy cập DB trực tiếp từ GUI)
        new Thread(() -> {
            java.util.List<Object[]> list = busThongKe.getSpSapHetHan();
            for (Object[] row : list)
                SwingUtilities.invokeLater(() -> m.addRow(row));
        }).start();

        JTable tbl = new JTable(m);
        tbl.setRowHeight(30);
        tbl.getTableHeader().setBackground(Color.decode("#FFEBEE"));
        p.add(new JScrollPane(tbl), BorderLayout.CENTER);
        return p;
    }
}