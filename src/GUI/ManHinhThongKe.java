package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

public class ManHinhThongKe extends JPanel {

    // ==========================================
    // DỮ LIỆU MẪU
    // ==========================================
    static final String[] NV_NAMES  = {"Nguyễn Thu Hà","Trần Minh Tuấn","Phạm Văn Đức","Võ Thị Lan","Lê Thị Mai","Hoàng Minh Khoa"};
    static final String[] NV_ROLES  = {"Dược sĩ","Dược sĩ","Dược sĩ","Dược sĩ","Thu ngân","Dược sĩ"};
    static final String[] NV_SHORT  = {"Hà","Tuấn","Đức","Lan","Mai","Khoa"};
    static final Color[]  NV_COLORS = {
        Color.decode("#1A73E8"), Color.decode("#00A76F"),
        Color.decode("#FFAB00"), Color.decode("#9C27B0"),
        Color.decode("#FF5630"), Color.decode("#00BCD4")
    };
    // HĐ sáng / chiều / DT sáng / DT chiều (triệu đ)
    static final int[]    NV_HD_S   = {78, 70, 45, 40, 55, 34};
    static final int[]    NV_HD_C   = {67, 62, 42, 36, 43, 30};
    static final double[] NV_DT_S   = {20.8,18.9,12.2,10.5,15.4, 9.1};
    static final double[] NV_DT_C   = {17.7,16.3,11.2, 9.3,13.2, 7.8};

    // Dữ liệu 10 ngày: mỗi nhân viên
    static final String[] DATES_10 = {"26/04","27/04","28/04","29/04","30/04","01/05","02/05","03/05","04/05","05/05"};
    static final int[][] NV_DAILY = {
        {8,7,13,8,5,13,11,10,11,8},
        {7,6,7,7,3,8,8,7,6,7},
        {6,8,6,7,4,7,7,8,7,6},
        {5,4,5,4,4,6,6,5,5,5},
        {6,5,7,6,3,7,7,6,6,5},
        {4,3,4,4,2,5,4,4,4,3}
    };

    // Dữ liệu doanh thu 12 tháng (M đ)
    static final int[] DT_DATA  = {42,52,48,60,55,65,72,76,78,82,80,95};
    static final int[] CP_DATA  = {28,33,30,38,35,42,46,48,50,52,51,60};
    static final String[] THANG = {"T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"};

    // Dữ liệu Donut SP
    static final int[]    DONUT_VALS    = {42, 28, 18, 12};
    static final String[] DONUT_LABELS  = {"TPCN","Thuốc KKĐ","Thuốc KĐ","Mỹ phẩm"};
    static final Color[]  DONUT_COLORS  = {
        Color.decode("#00A76F"), Color.decode("#1A73E8"),
        Color.decode("#FF5630"), Color.decode("#FFAB00")
    };

    // ==========================================
    // FIELDS
    // ==========================================
    private CardLayout cardBody;
    private JPanel pnlBody;
    private JButton btnDT, btnNV;
    private JComboBox<String> cboNam, cboKyLoc, cboNhanVien;
    private JLabel lblYearBadge;

    // Chart references để có thể repaint khi filter
    private BarChartMain  chartBarMain;
    private LineChartDaily chartLineDaily;
    private DonutChart    chartDonut;
    private NVDailyChart  chartNVDaily;
    private NVShiftChart  chartNVShift;
    private NVDetailTable tblNVDetail;

    private int selectedNVIdx = -1; // -1 = Tất cả

    // ==========================================
    // CONSTRUCTOR
    // ==========================================
    public ManHinhThongKe() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.decode("#F4F6F8"));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        JPanel topInfo = new JPanel(new BorderLayout(0, 8));
        topInfo.setOpaque(false);
        topInfo.add(buildFilterBar(), BorderLayout.NORTH);
        topInfo.add(buildShiftBanner(), BorderLayout.SOUTH);
        center.add(topInfo, BorderLayout.NORTH);

        cardBody = new CardLayout();
        pnlBody  = new JPanel(cardBody);
        pnlBody.setOpaque(false);
        pnlBody.add(buildViewDT(), "DT");
        pnlBody.add(buildViewNV(), "NV");
        center.add(pnlBody, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        setupEvents();
    }

    // ==========================================
    // HEADER
    // ==========================================
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        lblYearBadge = new JLabel("Cả năm 2024");
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
        left.add(title);
        left.add(lblYearBadge);

        JButton btnXuat = new JButton("📥 Xuất báo cáo ▼");
        btnXuat.setBackground(Color.decode("#152A4B"));
        btnXuat.setForeground(Color.WHITE);
        btnXuat.setFocusPainted(false);
        btnXuat.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnXuat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenuItem("Xuất Excel (.xlsx)"));
        menu.add(new JMenuItem("Xuất PDF (.pdf)"));
        btnXuat.addActionListener(e -> menu.show(btnXuat, 0, btnXuat.getHeight()));

        p.add(left, BorderLayout.WEST);
        p.add(btnXuat, BorderLayout.EAST);
        return p;
    }

    // ==========================================
    // FILTER BAR
    // ==========================================
    private JPanel buildFilterBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true));

        btnDT = makeTabBtn("📈 Doanh thu & SP", true);
        btnNV = makeTabBtn("👥 Nhân viên",       false);

        cboNam = new JComboBox<>(new String[]{"2022","2023","2024","2025","2026"});
        cboNam.setSelectedItem("2024");

        cboKyLoc = new JComboBox<>(new String[]{"Cả năm","Tháng","Quý","Tùy chỉnh"});

        String[] nvList = new String[NV_NAMES.length + 1];
        nvList[0] = "Tất cả";
        System.arraycopy(NV_NAMES, 0, nvList, 1, NV_NAMES.length);
        cboNhanVien = new JComboBox<>(nvList);

        JButton btnThucHien = new JButton("📊 Thực hiện thống kê");
        btnThucHien.setBackground(Color.decode("#EF4444"));
        btnThucHien.setForeground(Color.WHITE);
        btnThucHien.setFocusPainted(false);
        btnThucHien.setBorder(new EmptyBorder(6, 14, 6, 14));
        btnThucHien.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnThucHien.addActionListener(e -> applyFilter());

        JButton btnReset = new JButton("↺ Reset");
        btnReset.setFocusPainted(false);
        btnReset.setBorder(new EmptyBorder(6, 12, 6, 12));
        btnReset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> {
            cboNam.setSelectedItem("2024");
            cboKyLoc.setSelectedIndex(0);
            cboNhanVien.setSelectedIndex(0);
            applyFilter();
        });

        p.add(btnDT); p.add(btnNV);
        p.add(new JLabel(" | Năm:")); p.add(cboNam);
        p.add(new JLabel(" Kỳ:"));   p.add(cboKyLoc);
        p.add(new JLabel(" | Dược sĩ:")); p.add(cboNhanVien);
        p.add(btnThucHien); p.add(btnReset);
        return p;
    }

    // ==========================================
    // SHIFT BANNER
    // ==========================================
    private JPanel buildShiftBanner() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.decode("#E8F5E9"));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#A5D6A7"), 1, true),
            new EmptyBorder(10, 16, 10, 16)));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        JLabel lbl1 = new JLabel("<html><b style='font-size:13px;'>TIỀN ĐẦU CA</b>" +
            " <span style='background:#fff;border:1px solid #A5D6A7;border-radius:8px;" +
            "padding:1px 8px;font-size:11px;color:#2E7D32;'>11/04/2026 19:31</span>" +
            " · Mai Trung Kiên (Dược sĩ)</html>");
        JLabel lbl2 = new JLabel("200.000đ × 1");
        lbl2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl2.setForeground(Color.decode("#888888"));
        left.add(lbl1); left.add(lbl2);

        JPanel right = new JPanel(new GridLayout(3, 1));
        right.setOpaque(false);
        JLabel r1 = new JLabel("Tổng tiền đầu ca", SwingConstants.RIGHT);
        r1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        r1.setForeground(Color.decode("#888888"));
        JLabel r2 = new JLabel("200.000đ", SwingConstants.RIGHT);
        r2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        r2.setForeground(Color.decode("#00796B"));
        JLabel r3 = new JLabel("↗ Tiền mặt kiểm đếm khi vào ca", SwingConstants.RIGHT);
        r3.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        r3.setForeground(Color.decode("#00796B"));
        right.add(r1); right.add(r2); right.add(r3);

        p.add(left,  BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ==========================================
    // TAB 1: DOANH THU & SẢN PHẨM
    // ==========================================
    private JPanel buildViewDT() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);

        // --- 4 stat cards ---
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        cards.add(statCard("Tổng doanh thu",    "795.0M đ",  "Cả năm 2024",       "#1A73E8", "📈", "#EEF2FF"));
        cards.add(statCard("Số lượng đơn hàng", "5.450",     "TB mỗi kỳ: 454 đơn","#9C27B0", "🛒", "#F3E5F5"));
        cards.add(statCard("Lợi nhuận",         "299.0M đ",  "Tỷ lệ: 37.6%",      "#00A76F", "📈", "#E8F5E9"));
        cards.add(statCard("Giá trị TB / đơn",  "146K đ",    "Tháng cao nhất: T12","#FF9800", "🎁", "#FFF3E0"));
        root.add(cards);
        root.add(Box.createVerticalStrut(12));

        // --- Biểu đồ cột chính (DT-CP-LN 12 tháng) ---
        chartBarMain = new BarChartMain();
        JPanel cBarCard = wrapChart("Doanh thu – Chi phí – Lợi nhuận · Cả năm 2024", "12 kỳ dữ liệu", chartBarMain, 280);
        cBarCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));
        root.add(cBarCard);
        root.add(Box.createVerticalStrut(12));

        // --- Line chart theo ngày + Donut ---
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
        JPanel miniStats = new JPanel(new GridLayout(1, 3, 8, 0));
        miniStats.setOpaque(false);
        miniStats.add(miniStatInline("Ngày cao nhất", "05/04", "3.6M đ", "#1A73E8", "#E3F2FD"));
        miniStats.add(miniStatInline("DT TB / ngày",  "2.8M đ","bình quân","#00A76F","#E8F5E9"));
        miniStats.add(miniStatInline("Đơn TB / ngày", "21",    "đơn / ngày","#FF9800","#FFF3E0"));
        lineCard.add(ltitle,      BorderLayout.NORTH);
        lineCard.add(miniStats,   BorderLayout.CENTER);
        lineCard.add(chartLineDaily, BorderLayout.SOUTH);
        chartLineDaily.setPreferredSize(new Dimension(0, 160));
        row2.add(lineCard);

        chartDonut = new DonutChart();
        row2.add(wrapChart("Doanh thu theo loại SP", "Cơ cấu sản phẩm", chartDonut, 200));

        root.add(row2);
        root.add(Box.createVerticalStrut(12));

        // --- Top SP + VAT ---
        JPanel row3 = new JPanel(new GridLayout(1, 2, 12, 0));
        row3.setOpaque(false);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        row3.add(buildTopSPTable());
        row3.add(buildVATTable());
        root.add(row3);
        root.add(Box.createVerticalStrut(8));

        JScrollPane sp = new JScrollPane(root);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(14);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(sp, BorderLayout.CENTER);
        return wrapper;
    }

    // ==========================================
    // TAB 2: NHÂN VIÊN
    // ==========================================
    private JPanel buildViewNV() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setOpaque(false);

        // --- 3 stat cards ---
        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        cards.add(statCard("Tổng nhân viên",     "6",        "đang hoạt động · Cả năm 2024","#1A73E8","👥","#EEF2FF"));
        cards.add(statCard("DT trung bình / NV", "27.1M đ",  "mỗi nhân viên",               "#9C27B0","📈","#F3E5F5"));
        cards.add(statCard("Tổng doanh thu NV",  "162.4M đ", "tất cả nhân viên",            "#00A76F","📈","#E8F5E9"));
        root.add(cards);
        root.add(Box.createVerticalStrut(12));

        // --- NV Daily Bar Chart ---
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

        // Filter dropdown nhân viên (cho chart này)
        JComboBox<String> cboNVChart = new JComboBox<>();
        cboNVChart.addItem("Tất cả");
        for (String n : NV_NAMES) cboNVChart.addItem(n);
        cboNVChart.addActionListener(e -> {
            int idx = cboNVChart.getSelectedIndex() - 1;
            chartNVDaily.setFilter(idx);
        });
        nvDailyHeader.add(nvDailyTitles, BorderLayout.WEST);
        nvDailyHeader.add(cboNVChart,    BorderLayout.EAST);

        chartNVDaily.setPreferredSize(new Dimension(0, 200));
        nvDailyCard.add(nvDailyHeader, BorderLayout.NORTH);
        nvDailyCard.add(chartNVDaily,  BorderLayout.CENTER);
        nvDailyCard.add(buildNVDailyLegend(), BorderLayout.SOUTH);
        root.add(nvDailyCard);
        root.add(Box.createVerticalStrut(12));

        // --- Shift chart + Detail table ---
        JPanel row2 = new JPanel(new GridLayout(1, 2, 12, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));

        // Shift chart panel
        JPanel shiftCard = new JPanel(new BorderLayout(0, 8));
        shiftCard.setBackground(Color.WHITE);
        shiftCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(14, 14, 10, 14)));
        JLabel shTitle = new JLabel("⏱ HĐ theo ca làm việc");
        shTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        shTitle.setForeground(Color.decode("#152A4B"));

        // Mini shift summary
        JPanel shMini = new JPanel(new GridLayout(1, 2, 8, 0));
        shMini.setOpaque(false);
        shMini.add(shiftMiniCard("Ca sáng (8h–14h)", "322 HĐ", "#FFF8E1", "#F59E0B", "#D97706"));
        shMini.add(shiftMiniCard("Ca chiều (14h–20h)", "280 HĐ", "#EEF2FF", "#6366F1", "#4F46E5"));

        chartNVShift = new NVShiftChart();
        chartNVShift.setPreferredSize(new Dimension(0, 160));

        JPanel shLegend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        shLegend.setOpaque(false);
        shLegend.add(legendDot(Color.decode("#FFAB00"), "Ca sáng"));
        shLegend.add(legendDot(Color.decode("#4F46E5"), "Ca chiều"));

        shiftCard.add(shTitle,    BorderLayout.NORTH);
        JPanel shCenter = new JPanel(new BorderLayout(0, 6));
        shCenter.setOpaque(false);
        shCenter.add(shMini,       BorderLayout.NORTH);
        shCenter.add(chartNVShift, BorderLayout.CENTER);
        shCenter.add(shLegend,     BorderLayout.SOUTH);
        shiftCard.add(shCenter, BorderLayout.CENTER);
        row2.add(shiftCard);

        // Detail table
        tblNVDetail = new NVDetailTable();
        row2.add(tblNVDetail);
        root.add(row2);
        root.add(Box.createVerticalStrut(12));

        // --- Top 7 ngày DT cao nhất ---
        root.add(buildTop7Table());
        root.add(Box.createVerticalStrut(8));

        JScrollPane sp = new JScrollPane(root);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(14);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(sp, BorderLayout.CENTER);
        return wrapper;
    }

    // ==========================================
    // SỰ KIỆN
    // ==========================================
    private void setupEvents() {
        btnDT.addActionListener(e -> { toggleTab(true);  cardBody.show(pnlBody, "DT"); });
        btnNV.addActionListener(e -> { toggleTab(false); cardBody.show(pnlBody, "NV"); });
        cboNam.addActionListener(e -> lblYearBadge.setText("Cả năm " + cboNam.getSelectedItem()));
        cboNhanVien.addActionListener(e -> applyFilter());
    }

    private void applyFilter() {
        int idx = cboNhanVien.getSelectedIndex() - 1; // -1 = Tất cả
        selectedNVIdx = idx;
        if (chartNVDaily  != null) { chartNVDaily.setFilter(idx); }
        if (chartNVShift  != null) { chartNVShift.setFilter(idx); }
        if (tblNVDetail   != null) { tblNVDetail.setFilter(idx);  }
        repaint();
    }

    private void toggleTab(boolean isDT) {
        btnDT.setBackground(isDT ? Color.decode("#E8F0FE") : Color.WHITE);
        btnDT.setForeground(isDT ? Color.decode("#1A73E8") : Color.decode("#444444"));
        btnNV.setBackground(!isDT ? Color.decode("#F3E5F5") : Color.WHITE);
        btnNV.setForeground(!isDT ? Color.decode("#9C27B0") : Color.decode("#444444"));
    }

    // ==========================================
    // BIỂU ĐỒ CỘT CHÍNH (DT-CP-LN)
    // ==========================================
    class BarChartMain extends JPanel {
        private int hoverIdx = -1;
        private Point tooltipPt = null;

        BarChartMain() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(0, 260));
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    hoverIdx = -1; tooltipPt = null;
                    int barW = 16, gap = 4;
                    int groupW = barW * 3 + gap * 2 + 14;
                    int startX = 50;
                    int maxH = getHeight() - 60;
                    int maxVal = 110;
                    for (int i = 0; i < 12; i++) {
                        int gx = startX + i * groupW;
                        // Check hover on any bar of this group
                        for (int b = 0; b < 3; b++) {
                            int bx = gx + b * (barW + gap);
                            int val = (b == 0) ? DT_DATA[i] : (b == 1) ? CP_DATA[i] : DT_DATA[i] - CP_DATA[i];
                            int bh = (int)((double)val / maxVal * maxH);
                            int by = getHeight() - 35 - bh;
                            if (e.getX() >= bx && e.getX() <= bx + barW && e.getY() >= by && e.getY() <= by + bh) {
                                hoverIdx = i; tooltipPt = e.getPoint(); break;
                            }
                        }
                        if (hoverIdx >= 0) break;
                    }
                    repaint();
                }
                @Override public void mouseExited(MouseEvent e) { hoverIdx = -1; tooltipPt = null; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int barW = 16, gap = 4, groupW = barW * 3 + gap * 2 + 14;
            int startX = 50, baseY = h - 35, maxH = h - 60;
            int maxVal = 110;
            Color[] barColors = {Color.decode("#1A73E8"), Color.decode("#FFAB00"), Color.decode("#00A76F")};

            // Grid lines
            g2.setStroke(new BasicStroke(0.5f));
            for (int v = 0; v <= maxVal; v += 25) {
                int y = baseY - (int)((double)v / maxVal * maxH);
                g2.setColor(Color.decode("#F0F0F0"));
                g2.drawLine(startX, y, w - 10, y);
                g2.setColor(Color.decode("#AAAAAA"));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.drawString(v + "M", 4, y + 4);
            }

            for (int i = 0; i < 12; i++) {
                int gx = startX + i * groupW;
                int[] vals = {DT_DATA[i], CP_DATA[i], DT_DATA[i] - CP_DATA[i]};
                for (int b = 0; b < 3; b++) {
                    int bh = (int)((double)vals[b] / maxVal * maxH);
                    int bx = gx + b * (barW + gap);
                    int by = baseY - bh;
                    Color c = barColors[b];
                    if (i == hoverIdx) {
                        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
                        c = Color.getHSBColor(hsb[0], hsb[1], Math.min(1f, hsb[2] * 1.2f));
                    }
                    g2.setColor(c);
                    g2.fillRoundRect(bx, by, barW, bh, 3, 3);
                }
                // Label tháng
                g2.setColor(Color.decode("#888888"));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.drawString(THANG[i], gx + 4, h - 16);
            }

            // Tooltip
            if (hoverIdx >= 0 && tooltipPt != null) {
                int i = hoverIdx;
                String txt = String.format("T%d  DT: %dM | CP: %dM | LN: %dM", i+1, DT_DATA[i], CP_DATA[i], DT_DATA[i]-CP_DATA[i]);
                drawTooltip(g2, tooltipPt.x + 10, tooltipPt.y - 30, txt);
            }
        }
    }

    // ==========================================
    // BIỂU ĐỒ ĐƯỜNG (DAILY)
    // ==========================================
    class LineChartDaily extends JPanel {
        private final double[] vals;
        private int hoverIdx = -1;
        private Point tooltipPt = null;

        LineChartDaily() {
            setBackground(Color.WHITE);
            vals = new double[30];
            Random r = new Random(42);
            for (int i = 0; i < 30; i++) vals[i] = 1.5 + Math.sin(i / 4.5) * 0.8 + r.nextDouble() * 0.5;

            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    int n = vals.length, w = getWidth(), h = getHeight();
                    float step = (float)(w - 40) / (n - 1);
                    hoverIdx = -1; tooltipPt = null;
                    for (int i = 0; i < n; i++) {
                        int px = 20 + (int)(i * step);
                        int py = h - 20 - (int)((vals[i] / 4.5) * (h - 30));
                        if (Math.abs(e.getX() - px) < 10 && Math.abs(e.getY() - py) < 10) {
                            hoverIdx = i; tooltipPt = e.getPoint(); break;
                        }
                    }
                    repaint();
                }
                @Override public void mouseExited(MouseEvent e) { hoverIdx = -1; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int n = vals.length, w = getWidth(), h = getHeight();
            float step = (float)(w - 40) / (n - 1);

            // Fill area
            GeneralPath area = new GeneralPath();
            area.moveTo(20, h - 20);
            for (int i = 0; i < n; i++) {
                int px = 20 + (int)(i * step);
                int py = h - 20 - (int)((vals[i] / 4.5) * (h - 30));
                if (i == 0) area.lineTo(px, py); else area.lineTo(px, py);
            }
            area.lineTo(20 + (int)((n-1)*step), h - 20);
            area.closePath();
            g2.setColor(new Color(26, 115, 232, 25));
            g2.fill(area);

            // Line
            g2.setColor(Color.decode("#1A73E8"));
            g2.setStroke(new BasicStroke(2f));
            int[] xs = new int[n], ys = new int[n];
            for (int i = 0; i < n; i++) {
                xs[i] = 20 + (int)(i * step);
                ys[i] = h - 20 - (int)((vals[i] / 4.5) * (h - 30));
            }
            for (int i = 0; i < n - 1; i++) g2.drawLine(xs[i], ys[i], xs[i+1], ys[i+1]);

            // Points
            for (int i = 0; i < n; i++) {
                if (i == hoverIdx) {
                    g2.setColor(Color.decode("#1A73E8"));
                    g2.fillOval(xs[i]-5, ys[i]-5, 10, 10);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(xs[i]-3, ys[i]-3, 6, 6);
                } else {
                    g2.setColor(Color.decode("#1A73E8"));
                    g2.fillOval(xs[i]-2, ys[i]-2, 5, 5);
                }
            }

            // X labels
            g2.setColor(Color.decode("#AAAAAA"));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            int[] labelIdxs = {0, 4, 9, 14, 19, 24, 29};
            for (int li : labelIdxs) {
                String d = (li+1 < 10 ? "0" : "") + (li+1) + "/04";
                g2.drawString(d, xs[li] - 8, h - 4);
            }

            // Tooltip
            if (hoverIdx >= 0 && tooltipPt != null) {
                String txt = String.format("Ngày %02d/04: %.1fM đ", hoverIdx+1, vals[hoverIdx]);
                drawTooltip(g2, tooltipPt.x + 8, tooltipPt.y - 28, txt);
            }
        }
    }

    // ==========================================
    // BIỂU ĐỒ DONUT
    // ==========================================
    class DonutChart extends JPanel {
        private int hoverIdx = -1;
        private Point tooltipPt = null;

        DonutChart() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(0, 200));
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    double cx = getWidth() / 2.0, cy = getHeight() / 2.0;
                    double dist = Math.hypot(e.getX() - cx, e.getY() - cy);
                    int r = Math.min(getWidth(), getHeight()) / 2 - 10;
                    int inner = r - 50;
                    hoverIdx = -1; tooltipPt = null;
                    if (dist >= inner && dist <= r) {
                        double angle = Math.toDegrees(Math.atan2(-(e.getY()-cy), e.getX()-cx));
                        if (angle < 0) angle += 360;
                        double cur = 0;
                        for (int i = 0; i < DONUT_VALS.length; i++) {
                            double sweep = DONUT_VALS[i] * 3.6;
                            if (angle >= cur && angle < cur + sweep) { hoverIdx = i; tooltipPt = e.getPoint(); break; }
                            cur += sweep;
                        }
                    }
                    repaint();
                }
                @Override public void mouseExited(MouseEvent e) { hoverIdx = -1; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int size = Math.min(w, h) - 30;
            int x = (w - size) / 2, y = (h - size) / 2;
            int startAngle = 0;
            for (int i = 0; i < DONUT_VALS.length; i++) {
                int sweep = (int)(DONUT_VALS[i] * 3.6);
                Color c = DONUT_COLORS[i];
                if (i == hoverIdx) {
                    float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
                    c = Color.getHSBColor(hsb[0], hsb[1], Math.min(1f, hsb[2] * 1.15f));
                    g2.setColor(c);
                    g2.fillArc(x-5, y-5, size+10, size+10, startAngle, sweep);
                } else {
                    g2.setColor(c);
                    g2.fillArc(x, y, size, size, startAngle, sweep);
                }
                // Label %
                double mid = Math.toRadians(startAngle + sweep / 2.0);
                int lx = (int)(x + size/2.0 + (size/2.0 - 35) * Math.cos(mid));
                int ly = (int)(y + size/2.0 - (size/2.0 - 35) * Math.sin(mid));
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g2.drawString(DONUT_VALS[i] + "%", lx - 10, ly + 4);
                startAngle += sweep;
            }
            // Hole
            g2.setColor(Color.WHITE);
            int hole = size - 100;
            g2.fillOval(x + 50, y + 50, hole, hole);

            // Tooltip
            if (hoverIdx >= 0 && tooltipPt != null) {
                drawTooltip(g2, tooltipPt.x + 8, tooltipPt.y - 28,
                    DONUT_LABELS[hoverIdx] + ": " + DONUT_VALS[hoverIdx] + "%");
            }
        }
    }

    // ==========================================
    // BIỂU ĐỒ CỘT DƯỢC SĨ THEO NGÀY
    // ==========================================
    class NVDailyChart extends JPanel {
        private int filterIdx = -1; // -1 = tất cả
        private int hoverDay  = -1;
        private Point tooltipPt = null;

        NVDailyChart() {
            setBackground(Color.WHITE);
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    hoverDay = -1; tooltipPt = null;
                    int n = DATES_10.length;
                    int startX = 45, w = getWidth();
                    int groupW = (w - startX - 10) / n;
                    for (int d = 0; d < n; d++) {
                        int gx = startX + d * groupW;
                        if (e.getX() >= gx && e.getX() < gx + groupW) {
                            hoverDay = d; tooltipPt = e.getPoint(); break;
                        }
                    }
                    repaint();
                }
                @Override public void mouseExited(MouseEvent e) { hoverDay = -1; repaint(); }
            });
        }

        void setFilter(int idx) { this.filterIdx = idx; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int n = DATES_10.length, startX = 45, baseY = h - 30, maxH = h - 45;
            int groupW = (w - startX - 10) / n;
            int maxVal = 16;

            // Grid
            g2.setStroke(new BasicStroke(0.5f));
            for (int v = 0; v <= maxVal; v += 4) {
                int y = baseY - (int)((double)v / maxVal * maxH);
                g2.setColor(Color.decode("#F0F0F0")); g2.drawLine(startX, y, w - 10, y);
                g2.setColor(Color.decode("#AAAAAA")); g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(String.valueOf(v), 4, y + 3);
            }
            g2.setColor(Color.decode("#AAAAAA")); g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g2.drawString("HĐ", 4, 15);

            int nvCount = (filterIdx >= 0) ? 1 : NV_NAMES.length;
            int barW = Math.max(5, (groupW - 8) / Math.max(1, nvCount));

            for (int d = 0; d < n; d++) {
                int gx = startX + d * groupW + 3;
                boolean isHover = (d == hoverDay);
                int bIdx = 0;
                for (int nv = 0; nv < NV_NAMES.length; nv++) {
                    if (filterIdx >= 0 && nv != filterIdx) continue;
                    int val = NV_DAILY[nv][d];
                    int bh = (int)((double)val / maxVal * maxH);
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
                // Label ngày
                g2.setColor(Color.decode("#888888")); g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(DATES_10[d], gx, h - 12);
            }

            // Tooltip
            if (hoverDay >= 0 && tooltipPt != null) {
                int d = hoverDay;
                StringBuilder sb = new StringBuilder("Ngày " + DATES_10[d] + ": ");
                if (filterIdx >= 0) {
                    sb.append(NV_SHORT[filterIdx]).append("=").append(NV_DAILY[filterIdx][d]).append(" HĐ");
                } else {
                    for (int nv = 0; nv < NV_NAMES.length; nv++) {
                        if (nv > 0) sb.append("  ");
                        sb.append(NV_SHORT[nv]).append("=").append(NV_DAILY[nv][d]);
                    }
                }
                drawTooltip(g2, Math.min(tooltipPt.x + 8, w - 160), tooltipPt.y - 28, sb.toString());
            }
        }
    }

    // ==========================================
    // BIỂU ĐỒ CỘT CA LÀM VIỆC
    // ==========================================
    class NVShiftChart extends JPanel {
        private int filterIdx = -1;
        private int hoverIdx  = -1;
        private Point tooltipPt = null;

        NVShiftChart() {
            setBackground(Color.WHITE);
            addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    hoverIdx = -1; tooltipPt = null;
                    int n = (filterIdx >= 0) ? 1 : NV_NAMES.length;
                    int startX = 30, groupW = (getWidth() - startX - 10) / n;
                    for (int i = 0; i < n; i++) {
                        if (e.getX() >= startX + i*groupW && e.getX() < startX + (i+1)*groupW) {
                            hoverIdx = i; tooltipPt = e.getPoint(); break;
                        }
                    }
                    repaint();
                }
                @Override public void mouseExited(MouseEvent e) { hoverIdx = -1; repaint(); }
            });
        }

        void setFilter(int idx) { this.filterIdx = idx; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int maxVal = 90;
            int startX = 30, baseY = h - 28, maxH = h - 40;
            int[] nvIdxs = (filterIdx >= 0) ? new int[]{filterIdx} : new int[]{0,1,2,3,4,5};
            int n = nvIdxs.length;
            int groupW = (w - startX - 10) / n;
            int barW = Math.max(8, (groupW - 6) / 2);

            for (int v = 0; v <= maxVal; v += 20) {
                int y = baseY - (int)((double)v / maxVal * maxH);
                g2.setColor(Color.decode("#F0F0F0")); g2.setStroke(new BasicStroke(0.5f));
                g2.drawLine(startX, y, w - 5, y);
                g2.setColor(Color.decode("#AAAAAA")); g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(String.valueOf(v), 2, y + 3);
            }

            for (int i = 0; i < n; i++) {
                int nv = nvIdxs[i];
                int gx = startX + i * groupW + 2;
                boolean hov = (i == hoverIdx);

                int shS = NV_HD_S[nv], shC = NV_HD_C[nv];
                int hS = (int)((double)shS / maxVal * maxH);
                int hC = (int)((double)shC / maxVal * maxH);

                Color cS = hov ? Color.decode("#FFD54F") : Color.decode("#FFAB00");
                Color cC = hov ? Color.decode("#7986CB") : Color.decode("#4F46E5");

                g2.setColor(cS); g2.fillRoundRect(gx, baseY - hS, barW, hS, 3, 3);
                g2.setColor(cC); g2.fillRoundRect(gx + barW + 2, baseY - hC, barW, hC, 3, 3);

                g2.setColor(Color.decode("#888888")); g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString(NV_SHORT[nv], gx + 2, h - 12);
            }

            if (hoverIdx >= 0 && tooltipPt != null) {
                int nv = nvIdxs[hoverIdx];
                String txt = String.format("%s  Sáng: %d HĐ | Chiều: %d HĐ", NV_NAMES[nv], NV_HD_S[nv], NV_HD_C[nv]);
                drawTooltip(g2, Math.min(tooltipPt.x+8, w-200), tooltipPt.y - 28, txt);
            }
        }
    }

    // ==========================================
    // BẢNG CHI TIẾT NHÂN VIÊN THEO CA
    // ==========================================
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
            JPanel titles = new JPanel(new GridLayout(2,1,0,2));
            titles.setOpaque(false);
            JLabel t1 = new JLabel("⏰ Chi tiết dược sĩ theo ca · Cả năm 2024");
            t1.setFont(new Font("Segoe UI", Font.BOLD, 13));
            t1.setForeground(Color.decode("#152A4B"));
            JTextField search = new JTextField("🔍 Tìm nhân viên...");
            search.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            search.setPreferredSize(new Dimension(150, 26));
            search.setForeground(Color.GRAY);
            search.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true));
            titles.add(t1);
            header.add(titles, BorderLayout.WEST);
            header.add(search,  BorderLayout.EAST);
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

            // Custom renderer for money columns
            DefaultTableCellRenderer rightRender = new DefaultTableCellRenderer();
            rightRender.setHorizontalAlignment(SwingConstants.RIGHT);
            tbl.getColumnModel().getColumn(1).setCellRenderer(new ShiftRenderer(Color.decode("#D97706")));
            tbl.getColumnModel().getColumn(2).setCellRenderer(rightRender);
            tbl.getColumnModel().getColumn(3).setCellRenderer(new ShiftRenderer(Color.decode("#4F46E5")));
            tbl.getColumnModel().getColumn(4).setCellRenderer(rightRender);
            tbl.getColumnModel().getColumn(5).setCellRenderer(new ShiftRenderer(Color.decode("#152A4B")));
            tbl.getColumnModel().getColumn(6).setCellRenderer(new DTRenderer());

            refreshData();
            JScrollPane sp = new JScrollPane(tbl);
            sp.setBorder(BorderFactory.createEmptyBorder());
            add(sp, BorderLayout.CENTER);
        }

        void setFilter(int idx) { this.filterIdx = idx; refreshData(); }

        void refreshData() {
            model.setRowCount(0);
            double totalDTS = 0, totalDTC = 0;
            int totalHDS = 0, totalHDC = 0;
            for (int i = 0; i < NV_NAMES.length; i++) {
                if (filterIdx >= 0 && i != filterIdx) continue;
                totalHDS += NV_HD_S[i]; totalHDC += NV_HD_C[i];
                totalDTS += NV_DT_S[i]; totalDTC += NV_DT_C[i];
                model.addRow(new Object[]{
                    NV_NAMES[i] + "\n" + NV_ROLES[i],
                    NV_HD_S[i], String.format("%.1fM đ", NV_DT_S[i]),
                    NV_HD_C[i], String.format("%.1fM đ", NV_DT_C[i]),
                    NV_HD_S[i]+NV_HD_C[i],
                    String.format("%.1fM đ", NV_DT_S[i]+NV_DT_C[i])
                });
            }
            model.addRow(new Object[]{
                "TỔNG:", totalHDS, String.format("%.1fM đ", totalDTS),
                totalHDC, String.format("%.1fM đ", totalDTC),
                totalHDS+totalHDC, String.format("%.1fM đ", totalDTS+totalDTC)
            });
        }
    }

    static class ShiftRenderer extends DefaultTableCellRenderer {
        Color color;
        ShiftRenderer(Color c) { this.color = c; setHorizontalAlignment(CENTER); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            setForeground(color); setFont(new Font("Segoe UI", Font.BOLD, 12));
            return this;
        }
    }

    static class DTRenderer extends DefaultTableCellRenderer {
        DTRenderer() { setHorizontalAlignment(RIGHT); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            super.getTableCellRendererComponent(t, v, s, f, r, c);
            setForeground(Color.decode("#00A76F")); setFont(new Font("Segoe UI", Font.BOLD, 12));
            return this;
        }
    }

    // ==========================================
    // BẢNG TOP SẢN PHẨM
    // ==========================================
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
        JTextField search = new JTextField("🔍 Tìm sản phẩm...");
        search.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        search.setPreferredSize(new Dimension(140, 26));
        search.setForeground(Color.GRAY);
        header.add(t,      BorderLayout.WEST);
        header.add(search, BorderLayout.EAST);
        p.add(header, BorderLayout.NORTH);

        String[] cols = {"#","Sản phẩm","Loại","SL","Doanh thu"};
        Object[][] data = {
            {"1","Vitamin C 1000mg","TPCN","1,850","8.3M đ"},
            {"2","Paracetamol 500mg","Thuốc KKĐ","3,200","2.6M đ"},
            {"3","Omega 3 Fish Oil","TPCN","620","13.6M đ"},
            {"4","Calcium + Vit D3","TPCN","980","7.8M đ"},
            {"5","Multivitamin Daily","TPCN","750","11.3M đ"},
            {"6","Amoxicillin 500mg","Thuốc KĐ","540","4.6M đ"},
            {"7","Kem dưỡng ẩm Vaseline","Mỹ phẩm","680","5.8M đ"},
            {"8","Siro ho Prospan","Thuốc KKĐ","420","3.8M đ"},
        };
        DefaultTableModel m = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(m);
        tbl.setRowHeight(30);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tbl.getTableHeader().setBackground(Color.decode("#F0F4FF"));
        tbl.setShowHorizontalLines(true);
        tbl.setGridColor(Color.decode("#F0F0F0"));
        tbl.setSelectionBackground(Color.decode("#F0F4FF"));

        // Badge renderer for Loại column
        tbl.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = new JLabel(v.toString(), SwingConstants.CENTER);
                lbl.setOpaque(true); lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
                switch(v.toString()) {
                    case "TPCN":      lbl.setBackground(Color.decode("#E8F5E9")); lbl.setForeground(Color.decode("#2E7D32")); break;
                    case "Thuốc KĐ": lbl.setBackground(Color.decode("#FCE4EC")); lbl.setForeground(Color.decode("#C62828")); break;
                    case "Thuốc KKĐ":lbl.setBackground(Color.decode("#E3F2FD")); lbl.setForeground(Color.decode("#1565C0")); break;
                    default:          lbl.setBackground(Color.decode("#FFF3E0")); lbl.setForeground(Color.decode("#E65100")); break;
                }
                return lbl;
            }
        });
        tbl.getColumnModel().getColumn(4).setCellRenderer(new DTRenderer());
        JScrollPane sp = new JScrollPane(tbl); sp.setBorder(BorderFactory.createEmptyBorder());
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ==========================================
    // BẢNG VAT
    // ==========================================
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
        JLabel total = new JLabel("Tổng VAT: 3.983.250đ");
        total.setFont(new Font("Segoe UI", Font.BOLD, 11));
        total.setForeground(Color.decode("#3730A3"));
        total.setBackground(Color.decode("#EEF2FF")); total.setOpaque(true);
        total.setBorder(new EmptyBorder(3, 10, 3, 10));
        header.add(t,     BorderLayout.WEST);
        header.add(total, BorderLayout.EAST);
        p.add(header, BorderLayout.NORTH);

        String[] cols = {"Mã SP","Tên sản phẩm","Kê đơn","VAT (%)","Tiền thuế"};
        Object[][] data = {
            {"SP001","Paracetamol 500mg","Có","5%","128.000đ"},
            {"SP002","Vitamin C 1000mg","Không","5%","416.250đ"},
            {"SP003","Omega 3 Fish Oil","Không","10%","1.364.000đ"},
            {"SP004","Amoxicillin 500mg","Có","5%","245.000đ"},
            {"SP005","Collagen Plus","Không","10%","890.000đ"},
            {"SP006","Calcium + Vit D3","Không","10%","784.000đ"},
            {"SP007","Ibuprofen 400mg","Có","5%","156.000đ"},
        };
        DefaultTableModel m = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(m);
        tbl.setRowHeight(30); tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tbl.getTableHeader().setBackground(Color.decode("#F5F7FA"));
        tbl.setShowHorizontalLines(true); tbl.setGridColor(Color.decode("#F0F0F0"));
        tbl.setSelectionBackground(Color.decode("#F0F4FF"));

        tbl.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t,v,s,f,r,c);
                setForeground(Color.decode("#1A73E8")); setFont(new Font("Segoe UI",Font.BOLD,11)); return this;
            }
        });
        tbl.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = new JLabel(v.toString(), SwingConstants.CENTER); l.setOpaque(true);
                l.setFont(new Font("Segoe UI", Font.BOLD, 10));
                if ("Có".equals(v.toString())) { l.setBackground(Color.decode("#E8F5E9")); l.setForeground(Color.decode("#2E7D32")); }
                else { l.setBackground(Color.decode("#FAFAFA")); l.setForeground(Color.GRAY); }
                return l;
            }
        });
        tbl.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = new JLabel(v.toString(), SwingConstants.CENTER); l.setOpaque(true);
                l.setFont(new Font("Segoe UI", Font.BOLD, 10));
                if ("10%".equals(v.toString())) { l.setBackground(Color.decode("#FFF3E0")); l.setForeground(Color.decode("#B45309")); }
                else { l.setBackground(Color.decode("#EEF2FF")); l.setForeground(Color.decode("#3730A3")); }
                return l;
            }
        });
        tbl.getColumnModel().getColumn(4).setCellRenderer(new DTRenderer());
        JScrollPane sp = new JScrollPane(tbl); sp.setBorder(BorderFactory.createEmptyBorder());
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ==========================================
    // BẢNG TOP 7 NGÀY
    // ==========================================
    private JPanel buildTop7Table() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(14, 14, 10, 14)));

        JLabel t = new JLabel("🏅 Top 7 ngày doanh thu cao nhất");
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(Color.decode("#152A4B"));
        p.add(t, BorderLayout.NORTH);

        String[] cols = {"#","Ngày","Doanh thu","Đơn"};
        Object[][] data = {
            {"🥇","05/04","3.6M đ","27"},{"🥈","21/04","3.6M đ","17"},
            {"🥉","20/04","3.6M đ","15"},{"4","04/04","3.5M đ","26"},
            {"5","06/04","3.5M đ","27"},{"6","22/04","3.5M đ","20"},
            {"7","19/04","3.4M đ","13"},
        };
        DefaultTableModel m = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(m);
        tbl.setRowHeight(32); tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tbl.getTableHeader().setBackground(Color.decode("#F0F4FF"));
        tbl.setShowHorizontalLines(true); tbl.setGridColor(Color.decode("#F0F0F0"));
        tbl.setSelectionBackground(Color.decode("#F0F4FF"));
        tbl.getColumnModel().getColumn(2).setCellRenderer(new DTRenderer());
        JScrollPane sp = new JScrollPane(tbl); sp.setBorder(BorderFactory.createEmptyBorder());
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ==========================================
    // LEGEND CHO DƯỢC SĨ
    // ==========================================
    private JPanel buildNVDailyLegend() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 2));
        p.setOpaque(false);
        for (int i = 0; i < NV_NAMES.length; i++) p.add(legendDot(NV_COLORS[i], NV_SHORT[i]));
        JLabel sub = new JLabel("Số hóa đơn xử lý mỗi ngày theo từng dược sĩ");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        sub.setForeground(Color.decode("#AAAAAA"));
        p.add(sub);
        return p;
    }

    // ==========================================
    // HELPERS
    // ==========================================

    /** Vẽ tooltip trực tiếp trên Graphics2D */
    private void drawTooltip(Graphics2D g2, int x, int y, String text) {
        FontMetrics fm = g2.getFontMetrics(new Font("Segoe UI", Font.PLAIN, 11));
        int tw = fm.stringWidth(text) + 16, th = fm.getHeight() + 10;
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        // Clamp x so it never overflows right edge (caller passes component width check)
        g2.setColor(new Color(21, 42, 75, 220));
        g2.fillRoundRect(x, y, tw, th, 6, 6);
        g2.setColor(Color.WHITE);
        g2.drawString(text, x + 8, y + th - 7);
    }

    private JPanel wrapChart(String title, String sub, JComponent chart, int chartH) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(14, 14, 10, 14)));
        JLabel lt = new JLabel(title);
        lt.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lt.setForeground(Color.decode("#152A4B"));
        JLabel ls = new JLabel(sub);
        ls.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ls.setForeground(Color.decode("#888888"));
        JPanel titles = new JPanel(new GridLayout(2,1,0,2)); titles.setOpaque(false);
        titles.add(lt); titles.add(ls);
        chart.setPreferredSize(new Dimension(0, chartH));
        p.add(titles, BorderLayout.NORTH);
        p.add(chart,  BorderLayout.CENTER);
        return p;
    }

    private JPanel statCard(String label, String val, String sub, String color, String icon, String iconBg) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(12, 14, 12, 14)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Color.decode("#888888"));
        JLabel ico = new JLabel(icon, SwingConstants.CENTER);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        ico.setOpaque(true);
        ico.setBackground(Color.decode(iconBg));
        ico.setPreferredSize(new Dimension(32, 32));
        ico.setBorder(BorderFactory.createLineBorder(Color.decode(iconBg), 0, true));
        top.add(lbl, BorderLayout.WEST);
        top.add(ico, BorderLayout.EAST);

        JLabel vl = new JLabel(val);
        vl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        vl.setForeground(Color.decode(color));

        JLabel sl = new JLabel(sub);
        sl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sl.setForeground(Color.decode("#999999"));

        JPanel bottom = new JPanel(new GridLayout(2,1,0,2)); bottom.setOpaque(false);
        bottom.add(vl); bottom.add(sl);

        p.add(top,    BorderLayout.NORTH);
        p.add(bottom, BorderLayout.CENTER);
        return p;
    }

    private JPanel miniStatInline(String label, String val, String sub, String color, String bgColor) {
        JPanel p = new JPanel(new GridLayout(3,1,0,1));
        p.setBackground(Color.decode(bgColor));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode(bgColor.replace("FF","AA")), 1, true),
            new EmptyBorder(6, 10, 6, 10)));
        JLabel l1 = new JLabel(label); l1.setFont(new Font("Segoe UI",Font.PLAIN,10)); l1.setForeground(Color.decode("#888888"));
        JLabel l2 = new JLabel(val);   l2.setFont(new Font("Segoe UI",Font.BOLD, 16)); l2.setForeground(Color.decode(color));
        JLabel l3 = new JLabel(sub);   l3.setFont(new Font("Segoe UI",Font.PLAIN,10)); l3.setForeground(Color.decode("#999999"));
        p.add(l1); p.add(l2); p.add(l3);
        return p;
    }

    private JPanel shiftMiniCard(String title, String val, String bg, String titleColor, String valColor) {
        JPanel p = new JPanel(new GridLayout(2,1,0,2));
        p.setBackground(Color.decode(bg));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode(bg.replace("FF","CC")), 1, true),
            new EmptyBorder(8, 12, 8, 12)));
        JLabel t = new JLabel(title, SwingConstants.CENTER); t.setFont(new Font("Segoe UI",Font.BOLD,11)); t.setForeground(Color.decode(titleColor));
        JLabel v = new JLabel(val,   SwingConstants.CENTER); v.setFont(new Font("Segoe UI",Font.BOLD,20)); v.setForeground(Color.decode(valColor));
        p.add(t); p.add(v);
        return p;
    }

    private JLabel legendDot(Color c, String text) {
        JLabel l = new JLabel("  " + text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(Color.decode("#555555"));
        l.setIcon(new Icon() {
            public void paintIcon(Component comp, Graphics g, int x, int y) {
                g.setColor(c); g.fillRoundRect(x, y, 10, 10, 3, 3);
            }
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
        if (active) b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return b;
    }
}