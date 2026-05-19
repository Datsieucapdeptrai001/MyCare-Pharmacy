package GUI;

import BUS.BUS_CaLamViec;
import BUS.BUS_ThongKe;
import Entity.CaLamViec;
import Utils.MenuIcon;
import Utils.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ManHinhChinh extends JPanel {

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JButton btnTongQuan, btnDoiChieu;
    private JPanel doiChieuPanel;
    private int[] soLuongTien = new int[9];
    private final long[] MENH_GIA = { 500000, 200000, 100000, 50000, 20000, 10000, 5000, 2000, 1000 };
    private final String[] MENH_GIA_STR = { "500.000đ", "200.000đ", "100.000đ", "50.000đ", "20.000đ", "10.000đ",
            "5.000đ", "2.000đ", "1.000đ" };

    private static final Color GREEN = Color.decode("#00A76F");
    private static final Color BLUE = Color.decode("#1A73E8");
    private static final Color RED = Color.decode("#FF5630");
    private static final Color ORANGE = Color.decode("#FFAB00");
    private static final Color BG = Color.decode("#F4F6F8");

    private final BUS_ThongKe busThongKe;
    private final BUS_CaLamViec busCaLamViec;

    // 0: Ca hiện tại, 1: Hôm nay, 2: Tuần này, 3: Tháng này
    private int currentDoiChieuFilterIndex = 0;
    private int filterCa = 0;
    private String filterMaNV = "";
    private JButton[] caButtons = new JButton[4];
    private JComboBox<String> cbxNhanVien;
    private List<String> listMaNV = new ArrayList<>();

    private BUS.BUS_ThongKe.ThongKeFilter getHienTaiFilter() {
        BUS.BUS_ThongKe.ThongKeFilter f = new BUS.BUS_ThongKe.ThongKeFilter();
        f.maNV = filterMaNV;
        if (!UserSession.getInstance().isAdmin() && UserSession.getInstance().getCaHienTai() != null) {
            f.startTime = UserSession.getInstance().getCaHienTai().getThoiGianBatDau();
        } else {
            f.ca = (filterCa > 0) ? filterCa : null;
        }
        return f;
    }

    public ManHinhChinh() {
        busThongKe = new BUS_ThongKe();
        busCaLamViec = new BUS_CaLamViec();

        if (!UserSession.getInstance().isAdmin()) {
            filterMaNV = UserSession.getInstance().getMaNhanVien();
        }

        setLayout(new BorderLayout());
        setBackground(BG);

        JPanel headerWrapper = new JPanel();
        headerWrapper.setLayout(new BoxLayout(headerWrapper, BoxLayout.Y_AXIS));
        headerWrapper.add(createTopHeader());
        if (UserSession.getInstance().isAdmin()) {
            headerWrapper.add(buildAdminFilterBar());
        }
        add(headerWrapper, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BG);

        loadCardPanels();

        add(cardPanel, BorderLayout.CENTER);
    }

    @Override
    public void setVisible(boolean aFlag) {
        if (aFlag) {
            loadCardPanels();
        }
        super.setVisible(aFlag);
    }

    public void loadCardPanels() {
        cardPanel.removeAll();
        cardPanel.add(createTongQuanPanel(), "TongQuan");
        doiChieuPanel = createDoiChieuPanel();
        cardPanel.add(doiChieuPanel, "DoiChieu");
        cardPanel.revalidate();
        cardPanel.repaint();

        if (btnDoiChieu != null && btnDoiChieu.getForeground().equals(BLUE)) {
            cardLayout.show(cardPanel, "DoiChieu");
        } else {
            cardLayout.show(cardPanel, "TongQuan");
        }
    }

    private JPanel createTopHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(Color.WHITE);
        hdr.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));
        hdr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabs.setBackground(Color.WHITE);
        btnTongQuan = makeTabBtn("Tổng quan", true, MenuIcon.IC_TAB_CHART);
        btnDoiChieu = makeTabBtn("Đối chiếu doanh thu", false, MenuIcon.IC_TAB_DOLLAR);
        btnTongQuan.addActionListener(e -> {
            setActiveTab(btnTongQuan, btnDoiChieu);
            cardLayout.show(cardPanel, "TongQuan");
        });
        btnDoiChieu.addActionListener(e -> {
            setActiveTab(btnDoiChieu, btnTongQuan);
            refreshDoiChieuPanel();
            cardLayout.show(cardPanel, "DoiChieu");
        });
        tabs.add(btnTongQuan);
        tabs.add(btnDoiChieu);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        right.setBackground(Color.WHITE);

        String ten = UserSession.getInstance().getTenHienThi();
        JLabel lblOn = new JLabel("Ca đang mở — " + ten);
        lblOn.setIcon(MenuIcon.IC_DOT_FILL);
        lblOn.setIconTextGap(6);
        lblOn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblOn.setForeground(GREEN);
        lblOn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GREEN, 1),
                new EmptyBorder(4, 10, 4, 10)));

        JButton btnKet = new JButton("Kết ca");
        btnKet.setIcon(MenuIcon.IC_STOP);
        btnKet.setIconTextGap(6);
        btnKet.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnKet.setForeground(RED);
        btnKet.setBackground(Color.WHITE);
        btnKet.setFocusPainted(false);
        btnKet.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(RED, 1),
                new EmptyBorder(4, 10, 4, 10)));
        btnKet.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnKet.addActionListener(e -> openKetCaDialog());

        JButton btnNapTien = new JButton("Nạp quỹ");
        btnNapTien.setIcon(MenuIcon.IC_ADD);
        btnNapTien.setIconTextGap(6);
        btnNapTien.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnNapTien.setForeground(BLUE);
        btnNapTien.setBackground(Color.WHITE);
        btnNapTien.setFocusPainted(false);
        btnNapTien.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BLUE, 1),
                new EmptyBorder(4, 10, 4, 10)));
        btnNapTien.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNapTien.addActionListener(e -> showNapTienDialog());

        if (UserSession.getInstance().getCaHienTai() == null) {
            lblOn.setVisible(false);
            btnNapTien.setVisible(false);
            btnKet.setVisible(false);
        }

        right.add(lblOn);
        right.add(btnNapTien);
        right.add(btnKet);

        hdr.add(tabs, BorderLayout.WEST);
        hdr.add(right, BorderLayout.EAST);
        return hdr;
    }

    private JPanel buildAdminFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")), new EmptyBorder(2, 10, 2, 10)));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lblTitle = new JLabel("Lọc dữ liệu:");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(Color.decode("#637381"));

        caButtons[0] = makeFilterBtn("Toàn thời gian");
        caButtons[1] = makeFilterBtn("Ca Sáng (06-14h)");
        caButtons[2] = makeFilterBtn("Ca Chiều (14-22h)");
        caButtons[3] = makeFilterBtn("Ca Tối (22-06h)");

        for (int i = 0; i < 4; i++) {
            final int caIndex = i;
            caButtons[i].addActionListener(e -> {
                filterCa = caIndex;
                updateCaButtonsUI();
                loadCardPanels();
            });
            bar.add(caButtons[i]);
        }
        updateCaButtonsUI();

        cbxNhanVien = new JComboBox<>();
        cbxNhanVien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbxNhanVien.setBackground(Color.WHITE);

        cbxNhanVien.addItem("Tất cả nhân viên");
        listMaNV.add("");

        List<String[]> nvs = busThongKe.getDanhSachNhanVien();
        if (nvs != null) {
            for (String[] nv : nvs) {
                cbxNhanVien.addItem(nv[0] + " - " + nv[1]);
                listMaNV.add(nv[0]);
            }
        }

        cbxNhanVien.addActionListener(e -> {
            int idx = cbxNhanVien.getSelectedIndex();
            if (idx >= 0) {
                filterMaNV = listMaNV.get(idx);
                loadCardPanels();
            }
        });

        bar.add(new JLabel(" |  Nhân viên:"));
        bar.add(cbxNhanVien);

        return bar;
    }

    private JButton makeFilterBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setBackground(Color.WHITE);
        b.setForeground(Color.GRAY);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    /**
     * Xây dựng cột điều kiện SQL cho HoaDon dựa trên filterMaNV và filterCa.
     * Đồng bộ với cách ManHinhThongKe.buildCondHD() hoạt động.
     */
    private String buildCondHD() {
        StringBuilder sb = new StringBuilder();
        if (filterMaNV != null && !filterMaNV.isEmpty()) {
            sb.append(" AND hd.nhanVienId='").append(filterMaNV).append("'");
        }
        switch (filterCa) {
            case 1:
                sb.append(" AND DATEPART(HOUR, hd.ngayLapHD) BETWEEN 6 AND 13");
                break;
            case 2:
                sb.append(" AND DATEPART(HOUR, hd.ngayLapHD) BETWEEN 14 AND 21");
                break;
            case 3:
                sb.append(" AND (DATEPART(HOUR, hd.ngayLapHD) >= 22 OR DATEPART(HOUR, hd.ngayLapHD) < 6)");
                break;
            default:
                break; // case 0: Toàn thời gian — không thêm điều kiện
        }
        return sb.toString();
    }

    private void updateCaButtonsUI() {
        for (int i = 0; i < 4; i++) {
            if (i == filterCa) {
                caButtons[i].setForeground(BLUE);
                caButtons[i].setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BLUE));
            } else {
                caButtons[i].setForeground(Color.GRAY);
                caButtons[i].setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            }
        }
    }

    private JButton makeTabBtn(String text, boolean active, Icon icon) {
        JButton b = new JButton(text);
        b.setIcon(icon);
        b.setIconTextGap(8);
        b.setPreferredSize(new Dimension(200, 44));
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createMatteBorder(0, 0, active ? 3 : 0, 0, BLUE));
        b.setForeground(active ? BLUE : Color.GRAY);
        return b;
    }

    private void setActiveTab(JButton a, JButton b) {
        a.setForeground(BLUE);
        a.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, BLUE));
        b.setForeground(Color.GRAY);
        b.setBorder(BorderFactory.createEmptyBorder());
    }

    private JPanel createTongQuanPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(14, 20, 14, 20));
        String titleCa = filterCa == 0 ? "Hôm nay" : ("Ca " + filterCa);
        JLabel title = new JLabel(
                "<html><b style='font-size:15px;'>MÀN HÌNH CHÍNH</b><br><span style='color:gray;font-size:11px;'>Tổng quan hoạt động — "
                        + titleCa + "</span></html>");
        root.add(title, BorderLayout.NORTH);

        class ScrollableBody extends JPanel implements Scrollable {
            @Override
            public Dimension getPreferredScrollableViewportSize() { return super.getPreferredSize(); }
            @Override
            public int getScrollableUnitIncrement(Rectangle v, int o, int d) { return 14; }
            @Override
            public int getScrollableBlockIncrement(Rectangle v, int o, int d) { return 50; }
            @Override
            public boolean getScrollableTracksViewportWidth() {
                Container vp = SwingUtilities.getUnwrappedParent(this);
                // Chỉ bóp nhỏ khi màn hình TO HƠN 1050px. Nhỏ hơn sẽ ngừng bóp và hiện cuộn ngang (Chống ép chữ)
                return (vp instanceof JViewport) && (vp.getWidth() > 1050);
            }
            @Override
            public boolean getScrollableTracksViewportHeight() {
                Container vp = SwingUtilities.getUnwrappedParent(this);
                // Tự động kéo dãn chiều cao lấp đầy khoảng trống bên dưới
                return (vp instanceof JViewport) && (vp.getHeight() > super.getPreferredSize().height);
            }
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = Math.max(1050, d.width); // Chốt chiều rộng tối thiểu 1050px
                return d;
            }
        }

        ScrollableBody body = new ScrollableBody();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG);

        if (UserSession.getInstance().getCaHienTai() != null) {
            body.add(buildTienDauCaBanner());
            body.add(box(12));
        }

        body.add(buildTickerPanel());
        body.add(box(8));
        body.add(buildKPIRow());
        body.add(box(12));
        body.add(buildChartRow());
        body.add(box(14));
        body.add(buildBottomRow());
        body.add(box(14));
        body.add(buildExpiringPanel());

        JScrollPane sc = new JScrollPane(body);
        sc.setBorder(null);
        sc.getVerticalScrollBar().setUnitIncrement(14);

        sc.getVerticalScrollBar().setUI(new Utils.ModernScrollBarUI());
        sc.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        
        sc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sc.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));

        root.add(sc, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildTienDauCaBanner() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.decode("#E6F7F2"));
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#B2DFDB")),
                new EmptyBorder(10, 16, 10, 16)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        CaLamViec ca = UserSession.getInstance().getCaHienTai();
        long tienDau = ca != null ? (long) ca.getTienDauCa() : 0;

        String caLabel = ca != null ? (ca.getLoaiCa() == 0 ? "Ca Sáng" : ca.getLoaiCa() == 1 ? "Ca Chiều" : "Ca Tối")
                : "";
        String thoiGian = ca != null
                ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Timestamp.valueOf(ca.getThoiGianBatDau()))
                : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel l1 = new JLabel("TIỀN ĐẦU CA  " + thoiGian + "  ·  " + UserSession.getInstance().getTenHienThi());
        l1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l1.setForeground(Color.decode("#006250"));
        JLabel l2 = new JLabel(formatMoney(tienDau) + " × 1   (" + caLabel + ")");
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l2.setForeground(Color.decode("#637381"));
        left.add(l1);
        left.add(box(3));
        left.add(l2);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        JLabel lTl = new JLabel("Tổng tiền đầu ca", SwingConstants.RIGHT);
        lTl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lTl.setForeground(Color.GRAY);
        lTl.setAlignmentX(RIGHT_ALIGNMENT);
        JLabel lAm = new JLabel(formatMoney(tienDau), SwingConstants.RIGHT);
        lAm.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lAm.setForeground(Color.decode("#006250"));
        lAm.setAlignmentX(RIGHT_ALIGNMENT);
        right.add(lTl);
        right.add(lAm);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel buildKPIRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        boolean isAdminKPI = UserSession.getInstance().isAdmin();
        BUS.BUS_ThongKe.ThongKeFilter caFilterNV = getHienTaiFilter();
        String kpiLabel = isAdminKPI ? "Toàn cửa hàng" : "Ca hiện tại";
        double[] kpiData = busThongKe.getKpiDoiChieu(caFilterNV);
        List<Object[]> listTopSP = busThongKe.getTopSPTrongNgay(java.time.LocalDate.now().toString(), caFilterNV);
        // Tổng số lượng SP thực tế = BAN_HANG + DOI_HANG(xuất) - TRA_HANG - DOI_HANG(trả)
        int sp = busThongKe.getTongSoLuongSPHomNay(caFilterNV);
        
        int hd = (int) kpiData[0];
        int kh = busThongKe.getTongKhachHang();
        
        double dtGopThucTe = kpiData[4]; // Tổng doanh thu bán ra (trước khi trừ trả hàng)
        if (kpiData.length > 7 && kpiData[7] > 0) dtGopThucTe = Math.max(0, kpiData[4] - kpiData[7]); // Trừ trả hàng

        // KPI 1: Top SP
        row.add(kpiCard("Sản phẩm bán ra hôm nay", String.valueOf(sp), "SP (đã tính đổi/trả)", "#EEF2FF", "#3D52A0", "PILL",
                () -> {
                    DefaultTableModel model = new DefaultTableModel(new String[] { "Sản phẩm", "Số lượng", "Doanh thu" }, 0) {
                        public boolean isCellEditable(int r, int c) { return false; }
                    };
                    List<Object[]> list = busThongKe.getTopSPTrongNgay(java.time.LocalDate.now().toString(), caFilterNV);
                    if (list != null)
                        for (Object[] obj : list)
                            model.addRow(new Object[] { obj[0], obj[1], formatMoney(Math.round((double) obj[2] * 1_000_000)) });
                    showKpiPopup(isAdminKPI ? "TOP SẢN PHẨM — TOÀN CỬA HÀNG" : "TOP SẢN PHẨM — CA CỦA BẠN", model);
                }));

        // KPI 2: Hóa đơn
        row.add(kpiCard("Hóa đơn hôm nay", String.valueOf(hd), "Đã lọc theo ca/NV", "#ECFDF5", "#00A76F", "DOCUMENT",
                () -> {
                    DefaultTableModel model = new DefaultTableModel(new String[] { "Mã HĐ", "Loại", "Khách hàng", "Giao dịch", "Giờ lập" }, 0) {
                        public boolean isCellEditable(int r, int c) { return false; }
                    };
                    List<Object[]> hdList = busThongKe.getHoaDonGanNhat(caFilterNV, 100);
                    if (hdList != null)
                        for (Object[] o : hdList) {
                            double tien = (Double) o[2];
                            String loaiGoc = (String) o[4];
                            String loaiHD = "Bán hàng";
                            if ("TRA_HANG".equals(loaiGoc)) loaiHD = "Trả hàng";
                            else if ("DOI_HANG".equals(o[4])) loaiHD = "Đổi hàng";
                            
                            String tienStr = formatMoney(Math.round(Math.abs(tien)));
                            if (tien < 0) tienStr = "- " + tienStr; // Hiển thị dấu âm màu đỏ
                            
                            model.addRow(new Object[] { o[0], loaiHD, o[1], tienStr, o[3] });
                        }
                    showKpiPopup("DANH SÁCH GIAO DỊCH HÓA ĐƠN", model);
                }));

        // KPI 3: Tổng tiền thanh toán (tổng tiền hóa đơn, chưa trừ VAT)
        String dtLabel = isAdminKPI ? "Toàn cửa hàng (VNĐ)" : "Ca hiện tại (VNĐ)";
        row.add(kpiCard("Tổng tiền thanh toán", formatMoney(Math.round(dtGopThucTe)), dtLabel, "#FFF7ED", "#FF6B00", "TAB_DOLLAR",
                () -> {
                    // Lấy lại số liệu mới nhất trước khi hiển thị popup
                    double[] kpiDataFresh = busThongKe.getKpiDoiChieu(caFilterNV);
                    double dtGopFresh = kpiDataFresh[4];
                    if (kpiDataFresh.length > 7 && kpiDataFresh[7] > 0)
                        dtGopFresh = Math.max(0, kpiDataFresh[4] - kpiDataFresh[7]);

                    DefaultTableModel model = new DefaultTableModel(new String[] { "Giờ", "Tổng tiền thanh toán" }, 0) {
                        public boolean isCellEditable(int r, int c) { return false; }
                    };
                    double[] dts = busThongKe.getDTTheoGioTrongNgay(java.time.LocalDate.now().toString(), caFilterNV);
                    double total = 0;
                    if (dts != null) {
                        for (int i = 0; i < dts.length; i++)
                            if (dts[i] != 0) {
                                model.addRow(new Object[] { i + ":00 - " + (i + 1) + ":00", formatMoney(Math.round(dts[i] * 1_000_000)) });
                                total += dts[i];
                            }
                        model.addRow(new Object[] { "TỔNG CỘNG", formatMoney(Math.round(total * 1_000_000)) });
                    }
                    showKpiPopup(isAdminKPI ? "TỔNG TIỀN THANH TOÁN THEO GIỜ — TOÀN CỬA HÀNG" : "TỔNG TIỀN THANH TOÁN THEO GIỜ — CA CỦA BẠN", model);
                    // Reload KPI row để cập nhật số liệu mới nhất sau khi popup đóng
                    SwingUtilities.invokeLater(() -> loadCardPanels());
                }));

        // KPI 4: Khách hàng
        row.add(kpiCard("Tổng khách hàng", String.valueOf(kh), "Cửa hàng", "#FFF0F0", "#FF5630", "USERS", () -> {
            DefaultTableModel model = new DefaultTableModel(new String[] { "Tên khách hàng", "SĐT", "Số HĐ", "Doanh thu", "Điểm tích lũy" }, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            List<Object[]> list = busThongKe.getTopKhachHangTheoDiem(5);
            if (list != null)
                for (Object[] obj : list)
                    model.addRow(new Object[] { obj[0], obj[1], obj[2], formatMoney(Math.round((double) obj[3] * 1_000_000)), obj[4] });
            showKpiPopup("TOP 5 KHÁCH HÀNG (Theo điểm tích lũy)", model);
        }));
        return row;
    }

    private void showKpiPopup(String title, DefaultTableModel model) {
        Window pw = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog((Frame) pw, true);
        dlg.setUndecorated(true);
        dlg.setSize(600, 400);
        dlg.setLocationRelativeTo(pw);

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

        // Header với gradient
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
        JLabel lblTitle = new JLabel(title, SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setIcon(MenuIcon.of("CHART", 18, Color.WHITE));
        lblTitle.setIconTextGap(10);

        JButton btnClose = new JButton(MenuIcon.of("CLOSE", 14, new Color(255, 255, 255, 180)));
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnClose.setForeground(new Color(255, 255, 255, 180));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dlg.dispose());
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnClose.setForeground(Color.WHITE);
            }

            public void mouseExited(MouseEvent e) {
                btnClose.setForeground(new Color(255, 255, 255, 180));
            }
        });
        hdr.add(lblTitle, BorderLayout.CENTER);
        hdr.add(btnClose, BorderLayout.EAST);

        // Table chuyên nghiệp
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Color.decode("#F1F3F5"));
        table.setSelectionBackground(Color.decode("#EBF5FF"));
        table.setIntercellSpacing(new Dimension(0, 0));

        // Header bảng đậm
        javax.swing.table.JTableHeader tHeader = table.getTableHeader();
        tHeader.setPreferredSize(new Dimension(0, 38));
        tHeader.setBackground(Color.decode("#F4F6F8"));
        tHeader.setForeground(Color.decode("#637381"));
        tHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#DFE3E8")));

        // Renderer: Alternating rows + căn phải cột số tiền/điểm
        table.setDefaultRenderer(Object.class, (tbl, val, sel, foc, r, c) -> {
            JLabel l = new JLabel(val != null ? val.toString() : "", SwingConstants.LEFT);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            l.setBorder(new EmptyBorder(4, 12, 4, 12));
            l.setOpaque(true);
            l.setBackground(sel ? Color.decode("#EBF5FF") : (r % 2 == 0 ? Color.WHITE : Color.decode("#FAFBFC")));
            l.setForeground(Color.decode("#212B36"));
            // Căn phải cột Doanh thu, Điểm, Số lượng, Thực thu
            String colName = tbl.getColumnName(c).toLowerCase();
            if (colName.contains("doanh thu") || colName.contains("điểm") || colName.contains("thực thu")) {
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setForeground(Color.decode("#1A73E8"));
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
            }
            if (colName.contains("số") && !colName.contains("sđt")) {
                l.setHorizontalAlignment(SwingConstants.CENTER);
            }
            // Bold dòng tổng cộng
            if (val != null && val.toString().startsWith("TỔNG")) {
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                l.setForeground(Color.decode("#152A4B"));
                l.setBackground(Color.decode("#EEF2FF"));
            }
            return l;
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(0, 8, 8, 8));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUI(new Utils.ModernScrollBarUI());

        // Footer với info
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        footer.setBackground(Color.decode("#F9FAFB"));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#EEF2F6")));
        JLabel lblCount = new JLabel(model.getRowCount() + " bản ghi");
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCount.setForeground(Color.GRAY);
        footer.add(lblCount);

        root.add(hdr, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // ── TICKER THÔNG MINH (dùng MenuIcon) ────────────────────────────
    private static class TickerItem {
        final String iconType, text, navTab, navKey;
        final Color color;

        TickerItem(String iconType, String text, Color color, String navTab, String navKey) {
            this.iconType = iconType;
            this.text = text;
            this.color = color;
            this.navTab = navTab;
            this.navKey = navKey;
        }
    }

    private List<TickerItem> loadTickerData() {
        List<TickerItem> items = new ArrayList<>();
        try {
            List<Object[]> topSP = busThongKe.getTopSPTrongNgay(java.time.LocalDate.now().toString());
            if (topSP != null)
                for (int i = 0; i < Math.min(3, topSP.size()); i++) {
                    Object[] o = topSP.get(i);
                    items.add(new TickerItem("FIRE", o[0] + " bán chạy — " + o[1] + " hộp hôm nay",
                            Color.decode("#00A76F"), "Thống kê", ""));
                }
            List<Object[]> hetHang = busThongKe.getTop4SanPhamSapHetHang();
            if (hetHang != null)
                for (Object[] o : hetHang)
                    items.add(new TickerItem("ALERT", o[0] + " sắp hết — còn " + o[1] + " hộp", Color.decode("#FF6B00"),
                            "Lô hàng", String.valueOf(o[0])));
            List<Object[]> hetHan = busThongKe.getLoHangSapHetHanNhanh(30);
            if (hetHan != null) {
                java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("dd/MM/yyyy");
                for (int i = 0; i < Math.min(3, hetHan.size()); i++) {
                    Object[] o = hetHan.get(i);
                    String hsd = (o[3] instanceof java.sql.Timestamp) ? sdf2.format((java.sql.Timestamp) o[3])
                            : String.valueOf(o[3]);
                    items.add(new TickerItem("CLOCK_WARN", o[0] + " hết hạn ngày " + hsd, Color.decode("#E11D48"),
                            "Lô hàng", String.valueOf(o[0])));
                }
            }
            // Phân quyền ticker — Dược sĩ chỉ thấy HĐ do mình tạo
            boolean isAdminTicker = UserSession.getInstance().isAdmin();
            String tickerMaNV = isAdminTicker ? null : UserSession.getInstance().getMaNhanVien();
            List<Object[]> hdCao = busThongKe.getHoaDonGiaTriCaoHomNay(3, tickerMaNV);
            if (hdCao != null)
                for (Object[] o : hdCao)
                    items.add(new TickerItem("MONEY_BAG",
                            "HĐ #" + o[0] + " trị giá " + formatMoney(Math.round((double) o[2])),
                            Color.decode("#6A1B9A"), "Bán hàng & Đổi trả", String.valueOf(o[0])));
            List<Object[]> vip = busThongKe.getKhachHangVIPMuaHomNay(3);
            if (vip != null)
                for (Object[] o : vip)
                    items.add(new TickerItem("STAR_FILL", "VIP " + o[0] + " mua " + o[2] + " SP",
                            Color.decode("#1A73E8"), "Khách hàng", ""));
        } catch (Exception ignored) {
        }
        if (items.isEmpty())
            items.add(new TickerItem("CHECK_OK", "Mọi thứ đều ổn định", Color.decode("#00A76F"), "", ""));
        return items;
    }

    class SmartTickerPanel extends JPanel {
        private static final int IC_SZ = 18, IC_GAP = 6, IT_GAP = 40;
        private List<TickerItem> items;
        private int scrollX = 0, hoveredIdx = -1;

        // Bổ sung biến để giữ Timer
        private javax.swing.Timer scrollTimer;
        private javax.swing.Timer reloadTimer;

        SmartTickerPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(0, 28));
            items = loadTickerData();
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int n = hitTest(e.getX());
                    if (n != hoveredIdx) {
                        hoveredIdx = n;
                        repaint();
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hoveredIdx = -1;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    int idx = hitTest(e.getX());
                    if (idx < 0 || idx >= items.size())
                        return;
                    TickerItem item = items.get(idx);
                    if (item.navTab == null || item.navTab.isEmpty())
                        return;

                    Window pw = SwingUtilities.getWindowAncestor(ManHinhChinh.this);
                    if (!(pw instanceof MainDashboard))
                        return;
                    MainDashboard md = (MainDashboard) pw;

                    if (item.navTab.equals("Bán hàng & Đổi trả") && item.navKey != null && !item.navKey.isEmpty()) {
                        // ── PHÂN NHÁNH THEO LOẠI HÓA ĐƠN ──────────────────────────────────
                        // Tra cứu loại hóa đơn trước khi điều hướng để tránh mở nhầm màn hình
                        BUS.BUS_HoaDon busHDNav = new BUS.BUS_HoaDon();
                        Entity.HoaDon hdNav = busHDNav.layHoaDonTheoMa(item.navKey);
                        String loaiHDNav = (hdNav != null && hdNav.getLoaiHD() != null)
                                ? hdNav.getLoaiHD().toString() : "";

                        if ("DOI_HANG".equals(loaiHDNav) || "TRA_HANG".equals(loaiHDNav)) {
                            // Hóa đơn Đổi/Trả → mở ManHinhDoiTra với đúng phiếu
                            ManHinhDoiTra.pendingDoiTraIdToOpen = item.navKey;
                            md.switchTabAndFilter("Bán hàng & Đổi trả", "");
                            // Sau khi tab ngoài hiện ra, chuyển card bên trong sang "DoiTra"
                            SwingUtilities.invokeLater(() -> chuyenCardDoiTra(md));
                        } else {
                            // Hóa đơn Bán hàng thông thường → mở ManHinhBanHang
                            ManHinhBanHang.pendingDraftIdToOpen = item.navKey;
                            md.switchTabAndFilter("Bán hàng & Đổi trả", "");
                        }
                        return;
                    }
                    if (item.navTab.equals("Lô hàng") && item.navKey != null && !item.navKey.isEmpty()) {
                        md.switchTabAndFilter("Lô hàng", item.navKey);
                        return;
                    }
                    md.switchTabAndFilter(item.navTab, item.navKey != null ? item.navKey : "");
                }
            });

            // Gán Timer vào biến thay vì thả trôi
            scrollTimer = new javax.swing.Timer(30, e -> {
                scrollX -= 2;
                repaint();
            });
            scrollTimer.start();

            reloadTimer = new javax.swing.Timer(60_000, e -> {
                items = loadTickerData();
                repaint();
            });
            reloadTimer.setRepeats(true);
            reloadTimer.start();
        }

        // TỰ ĐỘNG HỦY TIMER KHI PANEL BỊ XÓA (CHỐNG LAG/TRÀN RAM)
        @Override
        public void removeNotify() {
            super.removeNotify();
            if (scrollTimer != null)
                scrollTimer.stop();
            if (reloadTimer != null)
                reloadTimer.stop();
        }

        private int iw(FontMetrics fm, TickerItem it) {
            return IC_SZ + IC_GAP + fm.stringWidth(it.text) + IT_GAP;
        }

        private int tw(FontMetrics fm) {
            int w = 0;
            for (TickerItem it : items)
                w += iw(fm, it);
            return Math.max(w, 1);
        }

        private int hitTest(int mx) {
            Graphics2D g2 = (Graphics2D) getGraphics();
            if (g2 == null || items.isEmpty())
                return -1;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();
            int tw = tw(fm), sx = scrollX % tw;
            if (sx > 0)
                sx -= tw;
            int x = sx;
            while (x < getWidth()) {
                for (int i = 0; i < items.size(); i++) {
                    int w = iw(fm, items.get(i));
                    if (mx >= x && mx < x + w) {
                        g2.dispose();
                        return i;
                    }
                    x += w;
                }
            }
            g2.dispose();
            return -1;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (items.isEmpty())
                return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();
            int tw = tw(fm), sx = scrollX % tw;
            if (sx > 0)
                sx -= tw;
            int x = sx;
            int textY = getHeight() / 2 + fm.getAscent() / 2 - 1;
            int iconY = (getHeight() - IC_SZ) / 2;
            while (x < getWidth() + tw) {
                for (int i = 0; i < items.size(); i++) {
                    TickerItem it = items.get(i);
                    int w = iw(fm, it);
                    if (x + w > 0 && x < getWidth()) {
                        Color c = hoveredIdx == i ? it.color.darker() : it.color;
                        MenuIcon.of(it.iconType, IC_SZ, c).paintIcon(this, g2, x, iconY);
                        g2.setFont(new Font("Segoe UI", hoveredIdx == i ? Font.BOLD : Font.PLAIN, 12));
                        g2.setColor(c);
                        g2.drawString(it.text, x + IC_SZ + IC_GAP, textY);
                        g2.setColor(new Color(200, 200, 200));
                        g2.fillOval(x + w - 14, getHeight() / 2 - 2, 4, 4);
                    }
                    x += w;
                }
            }
            g2.dispose();
        }
    }

    private JPanel buildTickerPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(Color.decode("#FFF1E6"));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#FF6B00")),
                new EmptyBorder(3, 14, 3, 14)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        JLabel tag = new JLabel("  LIVE ");
        tag.setIcon(MenuIcon.of("FIRE", 14, Color.WHITE));
        tag.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tag.setForeground(Color.WHITE);
        tag.setOpaque(true);
        tag.setBackground(Color.decode("#FF6B00"));
        tag.setBorder(new EmptyBorder(3, 6, 3, 8));
        p.add(tag, BorderLayout.WEST);
        p.add(new SmartTickerPanel(), BorderLayout.CENTER);
        return p;
    }

    private JPanel kpiCard(String ttl, String val, String sub, String bg, String fg, String iconType,
            Runnable onClick) {

        Color fgColor = Color.decode(fg);
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#EEF2F6")),
                new EmptyBorder(12, 14, 12, 14)));
        p.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                p.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(fgColor, 2),
                        new EmptyBorder(12, 14, 12, 14)));
                p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                p.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode("#EEF2F6")),
                        new EmptyBorder(12, 14, 12, 14)));
                p.setCursor(Cursor.getDefaultCursor());
            }

            public void mouseClicked(java.awt.event.MouseEvent e) {
                // Flash effect khi click
                Color orig = p.getBackground();
                p.setBackground(Color.decode("#EEF2F6"));
                new javax.swing.Timer(180, ev -> {
                    p.setBackground(orig);
                    ((javax.swing.Timer) ev.getSource()).stop();
                }).start();
                if (onClick != null)
                    onClick.run();
            }
        });

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel lT = new JLabel(ttl);
        lT.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lT.setForeground(Color.GRAY);
        JLabel ico = new JLabel("", SwingConstants.CENTER);
        ico.setIcon(MenuIcon.of(iconType, 20));
        ico.setOpaque(true);
        ico.setBackground(Color.decode(bg));
        ico.setForeground(fgColor);
        ico.setPreferredSize(new Dimension(36, 36));
        top.add(lT, BorderLayout.CENTER);
        top.add(ico, BorderLayout.EAST);

        JLabel lV = new JLabel(val);
        lV.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lV.setForeground(fgColor);
        JLabel lS = new JLabel(sub);
        lS.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lS.setForeground(Color.GRAY);

        p.add(top, BorderLayout.NORTH);
        p.add(lV, BorderLayout.CENTER);
        p.add(lS, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildChartRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(1050, 260));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));

        int currentYear = java.time.Year.now().getValue();
        // condHD: xây từ filterMaNV và filterCa, đồng bộ với các hàm khác
        BUS.BUS_ThongKe.ThongKeFilter condHD = getHienTaiFilter();
        double[] monthly = busThongKe.getDoanhThu12Thang(currentYear, condHD);
        if (monthly == null)
            monthly = new double[12];
        for (int i = 0; i < monthly.length; i++)
            monthly[i] = monthly[i] * 1_000_000;

        int[] cats = busThongKe.getSoLuongTheoLoaiSP(currentYear, condHD);
        if (cats == null)
            cats = new int[] { 0, 0, 0, 0 };

        row.add(barChart("Doanh thu theo tháng (" + currentYear + ")", monthly));
        row.add(donutChart("Phân loại sản phẩm", cats,
                new String[] { "Thuốc kê đơn", "Thuốc không kê đơn", "TPCN", "Mỹ phẩm" },
                new Color[] { RED, BLUE, GREEN, ORANGE }));
        return row;
    }

    private JPanel buildBottomRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(1050, 260));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel inv = wCard();
        inv.setLayout(new BorderLayout(0, 8));
        JLabel lbI = new JLabel("Hóa đơn đã lọc theo Ca/NV");
        lbI.setFont(new Font("Segoe UI", Font.BOLD, 12));
        inv.add(lbI, BorderLayout.NORTH);

        DefaultTableModel mInv = new DefaultTableModel(new String[] { "Mã HĐ", "Khách hàng", "Tiền", "Thanh toán" },
                0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tInv = new JTable(mInv);
        tInv.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tInv.setRowHeight(26);
        tInv.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tInv.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tInv.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int r = tInv.getSelectedRow();
                if (r >= 0 && !tInv.getValueAt(r, 0).equals("—")) {
                    String maHD = tInv.getValueAt(r, 0).toString();
                    BUS.BUS_HoaDon busHD = new BUS.BUS_HoaDon();
                    Entity.HoaDon hd = busHD.layHoaDonTheoMa(maHD);
                    if (hd != null) {
                        // ── PHÂN NHÁNH THEO LOẠI HÓA ĐƠN ──────────────────────────────────
                        // Phiếu đổi/trả hàng (DTH-xxxx) → mở ChiTietPhieuDoiTra
                        // để dialog tự load đầy đủ thông tin qua BUS, GUI không xử lý logic.
                        String loaiHDStr = hd.getLoaiHD() != null ? hd.getLoaiHD().toString() : "";
                        if ("DOI_HANG".equals(loaiHDStr) || "TRA_HANG".equals(loaiHDStr)) {
                            Window pw = SwingUtilities.getWindowAncestor(ManHinhChinh.this);
                            ChiTietPhieuDoiTra dlg = new ChiTietPhieuDoiTra((Frame) pw, maHD);
                            dlg.setVisible(true);
                        } else {
                            // Hóa đơn bán hàng thông thường → giữ nguyên ChiTietHoaDon
                            String ngay = hd.getNgayLapHD()
                                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                            String khach = hd.getKhachHangId() != null ? hd.getKhachHangId().getHoVaTen() : "Khách lẻ";
                            String sdt = hd.getKhachHangId() != null ? hd.getKhachHangId().getSdt() : "";
                            String pt = tInv.getValueAt(r, 3).toString();
                            String tenNV = hd.getNhanVienId() != null ? hd.getNhanVienId().getHoVaTen() : "";
                            BUS.BUS_ChiTietHoaDon busCTHD = new BUS.BUS_ChiTietHoaDon();
                            List<Object[]> lsSP = busCTHD.layDanhSachSanPhamTheoMaHD(maHD);
                            Window pw = SwingUtilities.getWindowAncestor(ManHinhChinh.this);
                            ChiTietHoaDon dlg = new ChiTietHoaDon((Frame) pw, maHD, ngay, khach, sdt, pt, "0", tenNV, lsSP);
                            dlg.setVisible(true);
                        }
                    }
                }
            }
        });

        List<Object[]> hdList = busThongKe.getHoaDonGanDayTrongCa(getHienTaiFilter());
        if (hdList != null && !hdList.isEmpty()) {
            for (Object[] o : hdList) {
                String ptThanhToan = (String) o[3];
                String loaiHD = (String) o[4]; // Nhận loại HD
                String hienThiTT = "Khác";
                if ("TIEN_MAT".equalsIgnoreCase(ptThanhToan)) {
                    hienThiTT = "Tiền mặt";
                } else if ("CHUYEN_KHOAN_NGAN_HANG".equalsIgnoreCase(ptThanhToan) || "CHUYEN_KHOAN".equalsIgnoreCase(ptThanhToan)) {
                    hienThiTT = "Chuyển khoản";
                }
                
                double tien = (Double) o[2];
                String tienStr = formatMoney(Math.round(Math.abs(tien)));
                // Nếu là trả hàng (số tiền âm), thêm dấu trừ để Table Formatter hiện màu đỏ
                if (tien < 0 || "TRA_HANG".equals(loaiHD)) {
                    tienStr = "- " + tienStr;
                    hienThiTT = "Hoàn " + hienThiTT.toLowerCase();
                }
                
                mInv.addRow(new Object[] { o[0], o[1], tienStr, hienThiTT });
            }
        } else {
            mInv.addRow(new Object[] { "—", "Không có hóa đơn", "—", "—" });
        }

        JScrollPane scrollHoaDon = new JScrollPane(tInv);
        scrollHoaDon.getVerticalScrollBar().setUI(new Utils.ModernScrollBarUI());
        scrollHoaDon.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        inv.add(scrollHoaDon, BorderLayout.CENTER);
        row.add(inv);

        JPanel stk = wCard();
        stk.setLayout(new BorderLayout(0, 8));
        JLabel lbS = new JLabel("Sản phẩm sắp hết hàng");
        lbS.setIcon(MenuIcon.IC_WARNING);
        lbS.setIconTextGap(6);
        lbS.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbS.setForeground(ORANGE);
        stk.add(lbS, BorderLayout.NORTH);
        JPanel listS = new JPanel();
        listS.setLayout(new BoxLayout(listS, BoxLayout.Y_AXIS));
        listS.setBackground(Color.WHITE);

        List<Object[]> ls = busThongKe.getTop4SanPhamSapHetHang();
        int sizeLS = ls != null ? ls.size() : 0;
        if (ls != null) {
            for (Object[] it : ls) {
                String nm = (String) it[0];
                int tn = (int) it[1];
                JPanel r2 = new JPanel(new BorderLayout(0, 2));
                r2.setOpaque(false);
                r2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                JPanel t2 = new JPanel(new BorderLayout());
                t2.setOpaque(false);
                JLabel ln = new JLabel(nm.length() > 32 ? nm.substring(0, 32) + "…" : nm);
                ln.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                JLabel lv = new JLabel(tn + " Hộp", SwingConstants.RIGHT);
                lv.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lv.setForeground(RED);
                t2.add(ln, BorderLayout.WEST);
                t2.add(lv, BorderLayout.EAST);
                JProgressBar pb = new JProgressBar(0, 50);
                pb.setValue(Math.min(tn, 50));
                pb.setBackground(Color.decode("#FFF0E0"));
                pb.setForeground(ORANGE);
                pb.setPreferredSize(new Dimension(0, 5));
                r2.add(t2, BorderLayout.NORTH);
                r2.add(pb, BorderLayout.SOUTH);

                r2.setCursor(new Cursor(Cursor.HAND_CURSOR));
                r2.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Window pw = SwingUtilities.getWindowAncestor(ManHinhChinh.this);
                        if (pw instanceof MainDashboard) {
                            ((MainDashboard) pw).chuyenSangTabNhapLoHangMoiVaFill(nm);
                        }
                    }
                });

                listS.add(r2);
                listS.add(box(5));
            }
        }
        int duTon = busThongKe.getSoSanPhamDuTon();
        JLabel lSum = new JLabel("<html><span style='color:#00A76F;'>✓ " + duTon
                + " sản phẩm đủ tồn</span>  <span style='color:#FF5630;'>⏰ " + sizeLS + " cần nhập</span></html>");
        lSum.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        listS.add(lSum);

        JScrollPane scrollSpHet = new JScrollPane(listS);
        scrollSpHet.setBorder(null);
        scrollSpHet.getVerticalScrollBar().setUI(new Utils.ModernScrollBarUI());
        scrollSpHet.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        stk.add(scrollSpHet, BorderLayout.CENTER);
        row.add(stk);

        return row;
    }

    private JPanel buildExpiringPanel() {
        JPanel p = wCard();
        p.setLayout(new BorderLayout(0, 8));
        p.setPreferredSize(new Dimension(1050, 260));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        int cnt = busThongKe.getSoLoHangSapHetHanKhoang(90);
        JPanel h2 = new JPanel(new BorderLayout());
        h2.setOpaque(false);
        JLabel lbT = new JLabel("Lô hàng sắp hết hạn (trong 90 ngày)");
        lbT.setIcon(MenuIcon.IC_WARNING);
        lbT.setIconTextGap(6);
        lbT.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbT.setForeground(ORANGE);
        JLabel badge = new JLabel(" " + cnt + " lô ", SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setBackground(ORANGE);
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        h2.add(lbT, BorderLayout.WEST);
        h2.add(badge, BorderLayout.EAST);
        p.add(h2, BorderLayout.NORTH);

        DefaultTableModel m = new DefaultTableModel(new String[] { "Sản phẩm", "Mã lô", "Tồn", "HSD", "Còn lại" }, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(m);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        t.setRowHeight(26);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        List<Object[]> dsHetHan = busThongKe.getLoHangSapHetHanNhanh(90);
        if (dsHetHan != null) {
            for (Object[] rs : dsHetHan) {
                int cl = (int) rs[4];
                m.addRow(new Object[] { rs[0], rs[1], String.format("%,d", (int) rs[2]), sdf.format((Timestamp) rs[3]),
                        cl <= 0 ? "Đã hết hạn" : cl + " ngày" });
            }
        }
        t.getColumnModel().getColumn(4).setCellRenderer((tbl2, val, sel, foc, r, c) -> {
            JLabel l = new JLabel(String.valueOf(val), SwingConstants.CENTER);
            l.setOpaque(true);
            l.setFont(new Font("Segoe UI", Font.BOLD, 10));
            String v = String.valueOf(val);
            if (v.equals("Đã hết hạn")) {
                l.setBackground(Color.decode("#FFEBEE"));
                l.setForeground(RED);
            } else if (v.contains("ngày") && Integer.parseInt(v.replace(" ngày", "")) <= 30) {
                l.setBackground(Color.decode("#FFF3E0"));
                l.setForeground(ORANGE);
            } else {
                l.setBackground(Color.WHITE);
                l.setForeground(Color.DARK_GRAY);
            }
            return l;
        });

        t.setCursor(new Cursor(Cursor.HAND_CURSOR));
        t.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 || e.getClickCount() == 2) {
                    int row = t.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        String maLo = String.valueOf(t.getValueAt(row, 1));
                        Window pw = SwingUtilities.getWindowAncestor(ManHinhChinh.this);
                        if (pw instanceof MainDashboard) {
                            ((MainDashboard) pw).switchTabAndFilter("Lô hàng", maLo);
                        }
                    }
                }
            }
        });

        JScrollPane scrollLoHang = new JScrollPane(t);
        scrollLoHang.getVerticalScrollBar().setUI(new Utils.ModernScrollBarUI());
        scrollLoHang.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        p.add(scrollLoHang, BorderLayout.CENTER);
        return p;
    }

    private JPanel createDoiChieuPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        // --- 1. HEADER & PHÂN QUYỀN ---
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel title = new JLabel("<html><b style='font-size:15px;'>ĐỐI CHIẾU DOANH THU</b></html>");
        topRow.add(title, BorderLayout.WEST);

        boolean isAdmin = false;
        try {
            isAdmin = Utils.UserSession.getInstance().isAdmin();
        } catch (Exception e) { e.printStackTrace(); }

        BUS.BUS_ThongKe.ThongKeFilter filter = new BUS.BUS_ThongKe.ThongKeFilter();

        if (isAdmin) {
            String[] locOptions = {"Ca hiện tại", "Hôm nay", "Tuần này", "Tháng này"};
            JComboBox<String> cboLoc = new JComboBox<>(locOptions);
            cboLoc.setSelectedIndex(currentDoiChieuFilterIndex);
            
            cboLoc.addActionListener(e -> {
                currentDoiChieuFilterIndex = cboLoc.getSelectedIndex();
                doiChieuPanel.removeAll();
                doiChieuPanel.add(createDoiChieuPanel(), BorderLayout.CENTER);
                doiChieuPanel.revalidate();
                doiChieuPanel.repaint();
            });
            
            JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            pnlFilter.setOpaque(false);
            pnlFilter.add(new JLabel("Bộ lọc thời gian: "));
            pnlFilter.add(cboLoc);
            topRow.add(pnlFilter, BorderLayout.EAST);

            java.time.LocalDate now = java.time.LocalDate.now();
            if (currentDoiChieuFilterIndex == 0) {
                filter = getHienTaiFilter(); 
            } else if (currentDoiChieuFilterIndex == 1) { 
                filter.fromDate = java.sql.Date.valueOf(now);
                filter.toDate = java.sql.Date.valueOf(now);
            } else if (currentDoiChieuFilterIndex == 2) { 
                filter.fromDate = java.sql.Date.valueOf(now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)));
                filter.toDate = java.sql.Date.valueOf(now.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY)));
            } else if (currentDoiChieuFilterIndex == 3) { 
                filter.fromDate = java.sql.Date.valueOf(now.withDayOfMonth(1));
                filter.toDate = java.sql.Date.valueOf(now.withDayOfMonth(now.lengthOfMonth()));
            }
        } else {
            JLabel lblSub = new JLabel("<html><span style='color:gray;font-size:11px;'>(Chỉ hiển thị ca làm việc của bạn)</span></html>");
            topRow.add(lblSub, BorderLayout.SOUTH);
            filter = getHienTaiFilter();
            try {
                filter.maNV = Utils.UserSession.getInstance().getMaNhanVien();
            } catch(Exception e) {}
        }
        root.add(topRow, BorderLayout.NORTH);

        // --- 2. LẤY DỮ LIỆU TỪ DB LÊN DTO ---
        BUS.KetQuaDoiChieuCa kq = busThongKe.layDoiChieuDoanhThuTheoCa(filter);
        
        // Lấy tiền CK theo đúng filter (Quản lý hay NV đều đúng)
        double[] kpi = busThongKe.getKpiDoiChieu(filter);
        double ck = (kpi.length > 6) ? kpi[6] : 0;
        double tienMat = Math.max(0, kq.get_c_TongThucThu() - ck);

        // --- 3. KHỞI TẠO BODY PANEL NHƯ CŨ ---
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);

        // KHỐI 1: CHI TIẾT BÁN HÀNG
        JPanel pnlBanHang = createBlockPanel("CHI TIẾT BÁN HÀNG (Hóa đơn: " + kq.soHdBan + ")", new Color(230, 247, 255));
        pnlBanHang.add(createRowMoi("Giá gốc chưa thuế:", formatMoney((long)kq.a_giaGocChuaThue)));
        pnlBanHang.add(createRowMoi("(-) Khuyến mãi:", formatMoney((long)kq.a_khuyenMai)));
        pnlBanHang.add(createRowMoi("(=) Doanh thu thuần:", formatMoney((long)kq.get_a_DoanhThuThuan())));
        pnlBanHang.add(createRowMoi("(+) Thuế VAT bán ra:", formatMoney((long)kq.a_vat)));
        pnlBanHang.add(createHighlightedRowMoi("(A) Tổng doanh thu bán ra:", formatMoney((long)kq.get_a_TongDoanhThu()), Color.BLUE));
        
        // KHỐI 2: ĐỔI / TRẢ HÀNG
        JPanel pnlTraHang = createBlockPanel("ĐỔI / TRẢ HÀNG (Phiếu: " + kq.soHdTra + ")", new Color(255, 241, 240));
        pnlTraHang.add(createRowMoi("Giá gốc món trả:", formatMoney((long)kq.b_giaGocMonTra)));
        pnlTraHang.add(createRowMoi("Khuyến mãi hoàn trả:", formatMoney((long)kq.b_khuyenMaiHoanTra)));
        pnlTraHang.add(createRowMoi("Doanh thu thuần giảm trừ:", formatMoney((long)kq.get_b_DoanhThuThuan())));
        pnlTraHang.add(createRowMoi("Thuế VAT hoàn trả:", formatMoney((long)kq.b_vatHoanTra)));
        pnlTraHang.add(createHighlightedRowMoi("(B) Tổng tiền chi trả hàng:", formatMoney((long)kq.get_b_TongChiTra()), Color.RED));

        // KHỐI 3: THỰC THU TỔNG HỢP
        JPanel pnlThucThu = createBlockPanel("THỰC TẾ CHỐT SỔ", new Color(246, 255, 237));
        pnlThucThu.add(createRowMoi("Tiền mặt thu về:", formatMoney((long)tienMat)));
        pnlThucThu.add(createRowMoi("Chuyển khoản / Thẻ:", formatMoney((long)ck)));
        pnlThucThu.add(createHighlightedRowMoi("(C) TỔNG THỰC THU (A - B):", formatMoney((long)kq.get_c_TongThucThu()), new Color(0, 153, 51)));

        body.add(pnlBanHang);
        body.add(Box.createRigidArea(new Dimension(0, 15))); 
        body.add(pnlTraHang);
        body.add(Box.createRigidArea(new Dimension(0, 15)));
        body.add(pnlThucThu);
        body.add(Box.createVerticalGlue());

        root.add(body, BorderLayout.CENTER);
        return root;
    }
    private JPanel createBlockPanel(String title, Color bgColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
                title, 0, 0, new Font("Segoe UI", Font.BOLD, 14)));
        return panel;
    }

    private JPanel createRowMoi(String labelText, String valueText) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(new JLabel(labelText), BorderLayout.WEST);
        row.add(new JLabel(valueText), BorderLayout.EAST);
        row.setBorder(new EmptyBorder(5, 10, 5, 10));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return row;
    }

    private JPanel createHighlightedRowMoi(String labelText, String valueText, Color color) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel lblLeft = new JLabel(labelText);
        lblLeft.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblLeft.setForeground(color);
        JLabel lblRight = new JLabel(valueText);
        lblRight.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblRight.setForeground(color);
        row.add(lblLeft, BorderLayout.WEST);
        row.add(lblRight, BorderLayout.EAST);
        row.setBorder(new EmptyBorder(10, 10, 10, 10));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return row;
    }
    
    private void refreshDoiChieuPanel() {
        if (doiChieuPanel == null) return;
        cardPanel.remove(doiChieuPanel);
        doiChieuPanel = createDoiChieuPanel();
        cardPanel.add(doiChieuPanel, "DoiChieu");
        cardLayout.show(cardPanel, "DoiChieu");
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private void openKetCaDialog() {
        CaLamViec ca = UserSession.getInstance().getCaHienTai();
        String maNV = UserSession.getInstance().getMaNhanVien();

        if (ca == null) {
            JOptionPane.showMessageDialog(this, "Chưa có ca làm việc nào được mở!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double dtCa      = busThongKe.getDoanhThuTheoCa(maNV, ca.getThoiGianBatDau());
        double tmBanHang = busThongKe.getTienMatBanHangTheoCa(maNV, ca.getThoiGianBatDau());
        double ckBanHang = busThongKe.getTienCKBanHangTheoCa(maNV, ca.getThoiGianBatDau()); // [CK] doanh thu chuyển khoản
        double tmHoanTra = busThongKe.getTienHoanTraTheoCa(maNV, ca.getThoiGianBatDau());

        Window pw = SwingUtilities.getWindowAncestor(this);
        hienThiPhieuXacNhanCuoi((Frame) pw, ca, maNV, dtCa, tmBanHang, ckBanHang, tmHoanTra);
    }

    // ── FORM QUẢN LÝ NẠP THÊM TIỀN ───────
    private void showNapTienDialog() {
        // ADMIN: mở thẳng form nhập số tiền, không cần xác thực lại
        if (UserSession.getInstance().isAdmin()) {
            showNapTienAdminDialog();
            return;
        }
        // NHÂN VIÊN: phải qua Dialog xác thực Quản lý
        showNapTienWithAuthDialog();
    }

    /** Dành cho Admin đã đăng nhập — bỏ qua bước xác thực, nhập thẳng số tiền nạp. */
    private void showNapTienAdminDialog() {
        Window pw = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog((Frame) pw, true);
        dlg.setUndecorated(true);
        dlg.setBackground(new Color(0, 0, 0, 0));
        dlg.setSize(400, 280);
        dlg.setLocationRelativeTo(pw);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 0; i < 6; i++) {
                    g2.setColor(new Color(0, 0, 0, 12 - i * 2));
                    g2.fillRoundRect(i, i, getWidth() - i * 2, getHeight() - i * 2, 20, 20);
                }
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(6, 6, getWidth() - 12, getHeight() - 12, 16, 16);
                g2.setColor(Color.decode("#DFE3E8"));
                g2.drawRoundRect(6, 6, getWidth() - 13, getHeight() - 13, 16, 16);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(15, 20, 10, 20));
        JLabel lblTitle = new JLabel("NẠP QUỸ", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.decode("#152A4B"));
        JButton btnClose = new JButton(new MenuIcon("CLOSE"));
        btnClose.setForeground(Color.decode("#919EAB"));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setPreferredSize(new Dimension(35, 35));
        btnClose.addActionListener(e -> dlg.dispose());
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnClose.setForeground(Color.RED); }
            public void mouseExited(MouseEvent e)  { btnClose.setForeground(Color.decode("#919EAB")); }
        });
        hdr.add(lblTitle, BorderLayout.CENTER);
        hdr.add(btnClose, BorderLayout.EAST);
        content.add(hdr, BorderLayout.NORTH);

        // Body — chỉ có ô nhập số tiền
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 30, 20, 30));
        body.add(createLabel("Số tiền nạp thêm (VND):"));
        body.add(Box.createRigidArea(new Dimension(0, 8)));

        JTextField txtTien = new JTextField();
        styleInput(txtTien);
        txtTien.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtTien.setForeground(Color.decode("#00A76F"));
        txtTien.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private boolean updating = false;
            private void fmt() {
                if (updating) return;
                SwingUtilities.invokeLater(() -> {
                    updating = true;
                    String raw = txtTien.getText().replaceAll("[^0-9]", "");
                    if (!raw.isEmpty()) {
                        try {
                            long v = Long.parseLong(raw);
                            String s = new DecimalFormat("###,###,###").format(v).replace(",", ".") + "đ";
                            txtTien.setText(s);
                            txtTien.setCaretPosition(s.length() - 1);
                        } catch (NumberFormatException ignored) {}
                    } else { txtTien.setText(""); }
                    updating = false;
                });
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { fmt(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { fmt(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { fmt(); }
        });
        body.add(txtTien);
        content.add(body, BorderLayout.CENTER);

        // Footer
        JPanel ft = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        ft.setOpaque(false);
        ft.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#EEF2F6")));

        JButton btnHuy = new JButton("Hủy bỏ");
        btnHuy.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnHuy.setIcon(MenuIcon.of("CANCEL", 16, Color.GRAY));
        btnHuy.setIconTextGap(6);
        btnHuy.setForeground(Color.GRAY);
        btnHuy.setPreferredSize(new Dimension(100, 38));
        btnHuy.setContentAreaFilled(false);
        btnHuy.setBorderPainted(false);
        btnHuy.setFocusPainted(false);
        btnHuy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHuy.addActionListener(e -> dlg.dispose());

        JButton btnXacNhan = new JButton("Xác nhận nạp") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#1A73E8"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g2);
            }
        };
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnXacNhan.setIcon(MenuIcon.of("CHECK_CIRCLE", 16, Color.WHITE));
        btnXacNhan.setIconTextGap(6);
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setPreferredSize(new Dimension(140, 38));
        btnXacNhan.setContentAreaFilled(false);
        btnXacNhan.setBorderPainted(false);
        btnXacNhan.setFocusPainted(false);
        btnXacNhan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXacNhan.addActionListener(e -> {
            String tStr = txtTien.getText().replaceAll("[^0-9]", "");
            if (tStr.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập số tiền cần nạp!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            long tienNap = Long.parseLong(tStr);
            long tienDauCaHienTai = UserSession.getInstance().getTienDauCa();
            long tienDauCaMoi = tienDauCaHienTai + tienNap;
            CaLamViec caHienTai = UserSession.getInstance().getCaHienTai();
            DAO.DAO_CaLamViec daoCa = new DAO.DAO_CaLamViec();
            if (daoCa.capNhatTienDauCa(caHienTai.getId(), tienDauCaMoi)) {
                UserSession.getInstance().setTienDauCa(tienDauCaMoi);
                caHienTai.setTienDauCa((double) tienDauCaMoi);
                JOptionPane.showMessageDialog(dlg, "Nạp thành công " + formatMoney(tienNap)
                        + " vào quỹ!\nTổng tiền quỹ hiện tại: " + formatMoney(tienDauCaMoi));
                dlg.dispose();
                loadCardPanels();
            } else {
                JOptionPane.showMessageDialog(dlg, "Lỗi cập nhật Database!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        ft.add(btnHuy);
        ft.add(btnXacNhan);
        content.add(ft, BorderLayout.SOUTH);
        root.add(content, BorderLayout.CENTER);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    /** Dành cho Nhân viên — yêu cầu xác thực tài khoản Quản lý trước khi nạp quỹ. */
    private void showNapTienWithAuthDialog() {
        Window pw = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog((Frame) pw, true);

        dlg.setUndecorated(true);
        dlg.setBackground(new Color(0, 0, 0, 0));
        dlg.setSize(440, 440);
        dlg.setLocationRelativeTo(pw);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int shadowSize = 6;
                for (int i = 0; i < shadowSize; i++) {
                    g2.setColor(new Color(0, 0, 0, 12 - i * 2));
                    g2.fillRoundRect(i, i, getWidth() - i * 2, getHeight() - i * 2, 20, 20);
                }

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(shadowSize, shadowSize, getWidth() - shadowSize * 2, getHeight() - shadowSize * 2, 16,
                        16);

                g2.setColor(Color.decode("#DFE3E8"));
                g2.drawRoundRect(shadowSize, shadowSize, getWidth() - shadowSize * 2 - 1,
                        getHeight() - shadowSize * 2 - 1, 16, 16);

                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel contentContainer = new JPanel(new BorderLayout());
        contentContainer.setOpaque(false);

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(15, 20, 10, 20));

        JLabel lblTitle = new JLabel("XÁC THỰC QUẢN LÝ", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.decode("#152A4B"));

        JButton btnClose = new JButton(new MenuIcon("CLOSE"));
        btnClose.setForeground(Color.decode("#919EAB"));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setPreferredSize(new Dimension(35, 35));
        btnClose.addActionListener(e -> dlg.dispose());

        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnClose.setForeground(Color.RED);
            }

            public void mouseExited(MouseEvent e) {
                btnClose.setForeground(Color.decode("#919EAB"));
            }
        });

        hdr.add(lblTitle, BorderLayout.CENTER);
        hdr.add(btnClose, BorderLayout.EAST);
        contentContainer.add(hdr, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 30, 20, 30));

        body.add(createLabel("Tài khoản Quản lý:"));
        body.add(Box.createRigidArea(new Dimension(0, 5)));
        JTextField txtUser = new JTextField();
        styleInput(txtUser);
        body.add(txtUser);
        body.add(Box.createRigidArea(new Dimension(0, 15)));

        body.add(createLabel("Mật khẩu:"));
        body.add(Box.createRigidArea(new Dimension(0, 5)));
        JPasswordField txtPass = new JPasswordField();
        styleInput(txtPass);
        body.add(txtPass);
        body.add(Box.createRigidArea(new Dimension(0, 15)));

        body.add(createLabel("Số tiền nạp thêm (VND):"));
        body.add(Box.createRigidArea(new Dimension(0, 5)));

        JTextField txtTien = new JTextField();
        styleInput(txtTien);
        txtTien.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtTien.setForeground(Color.decode("#00A76F"));

        txtTien.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private boolean isUpdating = false;

            private void formatMoney() {
                if (isUpdating)
                    return;

                SwingUtilities.invokeLater(() -> {
                    isUpdating = true;
                    String rawText = txtTien.getText().replaceAll("[^0-9]", "");

                    if (!rawText.isEmpty()) {
                        try {
                            long val = Long.parseLong(rawText);
                            DecimalFormat df = new DecimalFormat("###,###,###");
                            String formatted = df.format(val).replace(",", ".") + "đ";

                            txtTien.setText(formatted);
                            txtTien.setCaretPosition(formatted.length() - 1);
                        } catch (NumberFormatException ex) {
                        }
                    } else {
                        txtTien.setText("");
                    }
                    isUpdating = false;
                });
            }

            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                formatMoney();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                formatMoney();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                formatMoney();
            }
        });

        body.add(txtTien);

        contentContainer.add(body, BorderLayout.CENTER);

        JPanel ft = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        ft.setOpaque(false);
        ft.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#EEF2F6")));

        JButton btnHuy = new JButton("Hủy bỏ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#F4F6F8"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g2);
            }
        };
        btnHuy.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnHuy.setIcon(MenuIcon.of("CANCEL", 16, Color.GRAY));
        btnHuy.setIconTextGap(6);
        btnHuy.setForeground(Color.GRAY);
        btnHuy.setPreferredSize(new Dimension(100, 38));
        btnHuy.setContentAreaFilled(false);
        btnHuy.setBorderPainted(false);
        btnHuy.setFocusPainted(false);
        btnHuy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHuy.addActionListener(e -> dlg.dispose());

        JButton btnXacNhan = new JButton("Xác nhận nạp") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#1A73E8"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g2);
            }
        };
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnXacNhan.setIcon(MenuIcon.of("CHECK_CIRCLE", 16, Color.WHITE));
        btnXacNhan.setIconTextGap(6);
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setPreferredSize(new Dimension(140, 38));
        btnXacNhan.setContentAreaFilled(false);
        btnXacNhan.setBorderPainted(false);
        btnXacNhan.setFocusPainted(false);
        btnXacNhan.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnXacNhan.addActionListener(e -> {
            String u = txtUser.getText().trim();
            String p = new String(txtPass.getPassword()).trim();
            String tStr = txtTien.getText().replaceAll("[^0-9]", "");

            if (u.isEmpty() || p.isEmpty() || tStr.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập đầy đủ thông tin!", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            BUS.BUS_TaiKhoan busTk = new BUS.BUS_TaiKhoan();
            if (busTk.authenticate(u, p)) {
                Entity.TaiKhoan tkAdmin = busTk.getTaiKhoanDayDu(u);
                if (tkAdmin != null && tkAdmin.getVaiTro() == Enumeration.VaiTro.ADMIN) {
                    long tienNap = Long.parseLong(tStr);
                    long tienDauCaHienTai = UserSession.getInstance().getTienDauCa();
                    long tienDauCaMoi = tienDauCaHienTai + tienNap;

                    CaLamViec caHienTai = UserSession.getInstance().getCaHienTai();
                    DAO.DAO_CaLamViec daoCa = new DAO.DAO_CaLamViec();

                    if (daoCa.capNhatTienDauCa(caHienTai.getId(), tienDauCaMoi)) {
                        UserSession.getInstance().setTienDauCa(tienDauCaMoi);
                        caHienTai.setTienDauCa((double) tienDauCaMoi);

                        JOptionPane.showMessageDialog(dlg, "Nạp thành công " + formatMoney(tienNap)
                                + " vào quỹ!\nTổng tiền quỹ hiện tại: " + formatMoney(tienDauCaMoi));
                        dlg.dispose();
                        loadCardPanels();
                    } else {
                        JOptionPane.showMessageDialog(dlg, "Lỗi cập nhật Database!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(dlg, "Tài khoản này không có quyền Quản lý!", "Từ chối",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dlg, "Sai tài khoản hoặc mật khẩu Quản lý!", "Từ chối",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        ft.add(btnHuy);
        ft.add(btnXacNhan);
        contentContainer.add(ft, BorderLayout.SOUTH);

        root.add(contentContainer, BorderLayout.CENTER);
        dlg.setContentPane(root);

        dlg.setVisible(true);
    }

    private void hienThiPhieuXacNhanCuoi(Frame parentFrame, CaLamViec ca, String maNV, double dtCa, double tmBanHang,
            double ckBanHang, double tmHoanTra) {

        // ── Tính toán dữ liệu (giữ nguyên logic cũ) ────────────────────────────
        long tienDauCa   = UserSession.getInstance().getTienDauCa();
        long tienBanHang = (long) tmBanHang;
        long tienTraHang = (long) tmHoanTra;

        double[] doiMat = {0, 0};
        if (ca != null && ca.getThoiGianBatDau() != null && maNV != null) {
            doiMat = busThongKe.getTienDoiHangMatTheoCa(maNV, ca.getThoiGianBatDau());
        }
        // Tổng thu tiền mặt = bán hàng + thu thêm từ đổi hàng
        long tongThuTM  = tienBanHang + (long) doiMat[0];
        // Tổng chi tiền mặt = hoàn lại đổi hàng + trả hàng
        long tongChiTM  = (long) doiMat[1] + tienTraHang;
        // Tiền hệ thống = đầu ca + thu - chi
        long tienHT     = tienDauCa + tongThuTM - tongChiTM;

        // Thông tin nhân viên / ca
        String[] CA_LABELS = {"Ca Sáng", "Ca Chiều", "Ca Tối"};
        String tenCa = (ca != null && ca.getLoaiCa() >= 0 && ca.getLoaiCa() <= 2) ? CA_LABELS[ca.getLoaiCa()] : "—";
        String tenNV = UserSession.getInstance().getTenHienThi();
        String gioBD = "—";
        if (ca != null && ca.getThoiGianBatDau() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy  HH:mm");
            gioBD = sdf.format(Timestamp.valueOf(ca.getThoiGianBatDau()));
        }

        // ── Dialog ngang ─────────────────────────────────────────────────────
        JDialog dlg = new JDialog(parentFrame, true);
        dlg.setUndecorated(true);
        dlg.getRootPane().setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1));
        dlg.setSize(900, 540);
        dlg.setLocationRelativeTo(parentFrame);
        dlg.setLayout(new BorderLayout());
        dlg.setResizable(false);
        dlg.getContentPane().setBackground(Color.WHITE);

        // ══ HEADER ═══════════════════════════════════════════════════════════
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Color.decode("#152A4B"), getWidth(), 0, Color.decode("#1A73E8")));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(14, 24, 14, 18));

        JPanel hdrLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        hdrLeft.setOpaque(false);
        JLabel hTitle = new JLabel("ĐÓNG CA LÀM VIỆC");
        hTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        hTitle.setForeground(Color.WHITE);
        JLabel hSubtitle = new JLabel("   " + tenNV + "  ·  " + tenCa + "  ·  Bắt đầu: " + gioBD);
        hSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hSubtitle.setForeground(new Color(255, 255, 255, 180));
        hdrLeft.add(hTitle);
        hdrLeft.add(hSubtitle);

        JButton btnClose = new JButton(new MenuIcon("CLOSE"));
        btnClose.setForeground(new Color(255, 255, 255, 160));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setPreferredSize(new Dimension(32, 32));
        btnClose.addActionListener(e -> dlg.dispose());
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnClose.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent  e) { btnClose.setForeground(new Color(255, 255, 255, 160)); }
        });

        hdr.add(hdrLeft,  BorderLayout.WEST);
        hdr.add(btnClose, BorderLayout.EAST);
        dlg.add(hdr, BorderLayout.NORTH);

        // ══ BODY — 2 CỘT ════════════════════════════════════════════════════
        JPanel body = new JPanel(new GridLayout(1, 2, 1, 0));
        body.setBackground(Color.decode("#DFE3E8")); // đường ngăn cách giữa 2 cột
        dlg.add(body, BorderLayout.CENTER);

        // ── CỘT TRÁI: Hệ thống tính toán ───────────────────────────────────
        JPanel colLeft = new JPanel();
        colLeft.setLayout(new BoxLayout(colLeft, BoxLayout.Y_AXIS));
        colLeft.setBackground(Color.WHITE);
        colLeft.setBorder(new EmptyBorder(28, 32, 28, 28));

        // Tiêu đề cột
        JLabel lblLeftTitle = new JLabel("Hệ thống tính toán");
        lblLeftTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLeftTitle.setForeground(Color.decode("#637381"));
        lblLeftTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        colLeft.add(lblLeftTitle);
        colLeft.add(Box.createRigidArea(new Dimension(0, 18)));

        // Helper: tạo 1 dòng thông tin lớn (label + số tiền)
        // ─── Dòng 1: Tiền đầu ca
        colLeft.add(buildKetCaRow(
            "Tiền đầu ca",
            formatMoney(tienDauCa),
            Color.decode("#212B36"),
            Color.decode("#212B36"),
            false));
        colLeft.add(Box.createRigidArea(new Dimension(0, 10)));

        // Dải phân cách
        JSeparator sep1 = new JSeparator();
        sep1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep1.setForeground(Color.decode("#EEF2F6"));
        sep1.setAlignmentX(Component.LEFT_ALIGNMENT);
        colLeft.add(sep1);
        colLeft.add(Box.createRigidArea(new Dimension(0, 10)));

        // ─── Dòng 2: Tổng thu tiền mặt
        colLeft.add(buildKetCaRow(
            "Tổng thu tiền mặt  (Bán mới + Bù đổi hàng)",
            "+ " + formatMoney(tongThuTM),
            Color.decode("#637381"),
            Color.decode("#00A76F"),
            false));
        colLeft.add(Box.createRigidArea(new Dimension(0, 10)));

        // ─── Dòng 3: Tổng chi tiền mặt
        colLeft.add(buildKetCaRow(
            "Tổng chi tiền mặt  (Hoàn trả khách)",
            "− " + formatMoney(tongChiTM),
            Color.decode("#637381"),
            Color.decode("#FF5630"),
            false));
        colLeft.add(Box.createRigidArea(new Dimension(0, 10)));

        // ─── Dòng 3b: Tổng thu Chuyển khoản / Thẻ (hiển thị riêng, KHÔNG cộng vào tiền cần nộp)
        colLeft.add(buildKetCaRow(
            "Tổng thu Chuyển khoản / Thẻ  (không tính vào quỹ)",
            formatMoney((long) ckBanHang),
            Color.decode("#637381"),
            Color.decode("#1A73E8"),
            false));
        colLeft.add(Box.createRigidArea(new Dimension(0, 16)));

        // Đường kẻ đậm trước dòng tổng
        JSeparator sep2 = new JSeparator();
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep2.setForeground(Color.decode("#152A4B"));
        sep2.setBackground(Color.decode("#152A4B"));
        sep2.setAlignmentX(Component.LEFT_ALIGNMENT);
        colLeft.add(sep2);
        colLeft.add(Box.createRigidArea(new Dimension(0, 16)));

        // ─── Dòng 4: TIỀN CẦN NỘP — chỉ tính tiền mặt (to, đỏ, đậm)
        // Công thức: Đầu ca + Thu TM (bán + bù đổi) − Chi TM (hoàn trả)
        colLeft.add(buildKetCaRow(
            "TIỀN CẦN NỘP  (Tiền mặt)",
            formatMoney(tienHT),
            Color.decode("#152A4B"),
            Color.decode("#FF5630"),
            true));  // bold = true

        // Đẩy nội dung lên trên
        colLeft.add(Box.createVerticalGlue());

        body.add(colLeft);

        // ── CỘT PHẢI: Nhân viên nhập liệu ──────────────────────────────────
        JPanel colRight = new JPanel();
        colRight.setLayout(new BoxLayout(colRight, BoxLayout.Y_AXIS));
        colRight.setBackground(Color.decode("#F4F6F8"));
        colRight.setBorder(new EmptyBorder(28, 28, 28, 32));

        JLabel lblRightTitle = new JLabel("Nhân viên xác nhận");
        lblRightTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRightTitle.setForeground(Color.decode("#637381"));
        lblRightTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        colRight.add(lblRightTitle);
        colRight.add(Box.createRigidArea(new Dimension(0, 20)));

        // Label "Tiền thực đếm"
        JLabel lblThucTe = new JLabel("Tiền thực đếm được (VNĐ)");
        lblThucTe.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblThucTe.setForeground(Color.decode("#212B36"));
        lblThucTe.setAlignmentX(Component.LEFT_ALIGNMENT);
        colRight.add(lblThucTe);
        colRight.add(Box.createRigidArea(new Dimension(0, 8)));

        // TextField tiền thực đếm — to, nổi bật
        JTextField txtThucTe = new JTextField(String.valueOf(tienHT));
        txtThucTe.setFont(new Font("Segoe UI", Font.BOLD, 22));
        txtThucTe.setForeground(Color.decode("#1A73E8"));
        txtThucTe.setHorizontalAlignment(SwingConstants.RIGHT);
        txtThucTe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        txtThucTe.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtThucTe.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#1A73E8"), 2, true),
                new EmptyBorder(8, 14, 8, 14)));
        txtThucTe.setBackground(Color.WHITE);
        colRight.add(txtThucTe);
        colRight.add(Box.createRigidArea(new Dimension(0, 6)));

        // Label chênh lệch — cập nhật động
        JLabel lblChenh = new JLabel("Khớp: 0 đ", SwingConstants.RIGHT);
        lblChenh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblChenh.setForeground(Color.decode("#00A76F"));
        lblChenh.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblChenh.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        colRight.add(lblChenh);
        colRight.add(Box.createRigidArea(new Dimension(0, 20)));

        // DocumentListener cập nhật chênh lệch theo thời gian thực
        txtThucTe.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private boolean isUpdating = false;
            private void update() {
                if (isUpdating) return;
                SwingUtilities.invokeLater(() -> {
                    isUpdating = true;
                    String rawText = txtThucTe.getText().replaceAll("[^0-9]", "");
                    if (!rawText.isEmpty()) {
                        try {
                            long thuc = Long.parseLong(rawText);
                            long lech = thuc - tienHT;
                            if (lech == 0) {
                                lblChenh.setText("✓  Khớp với hệ thống");
                                lblChenh.setForeground(Color.decode("#00A76F"));
                            } else {
                                lblChenh.setText("Lệch: " + formatChenhLech(lech));
                                lblChenh.setForeground(Color.decode("#FF5630"));
                            }
                            DecimalFormat df = new DecimalFormat("###,###,###");
                            String formatted = df.format(thuc).replace(",", ".");
                            int caret = txtThucTe.getCaretPosition();
                            txtThucTe.setText(formatted);
                            txtThucTe.setCaretPosition(Math.min(caret, formatted.length()));
                        } catch (NumberFormatException ignored) {}
                    } else {
                        lblChenh.setText("—");
                        lblChenh.setForeground(Color.GRAY);
                    }
                    isUpdating = false;
                });
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        // Label "Ghi chú"
        JLabel lblNote = new JLabel("Ghi chú  (nếu lệch tiền)");
        lblNote.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNote.setForeground(Color.decode("#212B36"));
        lblNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        colRight.add(lblNote);
        colRight.add(Box.createRigidArea(new Dimension(0, 8)));

        // TextArea ghi chú — không có thanh cuộn, gọn gàng
        JTextArea txtNote = new JTextArea(4, 20);
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        txtNote.setBackground(Color.WHITE);
        txtNote.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        txtNote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        txtNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        colRight.add(txtNote);

        colRight.add(Box.createVerticalGlue());
        body.add(colRight);

        // ══ FOOTER ═══════════════════════════════════════════════════════════
        JPanel ft = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        ft.setBackground(Color.WHITE);
        ft.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#DFE3E8")));

        JButton bHuy = new JButton("Hủy bỏ");
        bHuy.setIcon(MenuIcon.of("CANCEL", 15, Color.GRAY));
        bHuy.setIconTextGap(6);
        bHuy.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bHuy.setPreferredSize(new Dimension(110, 40));
        bHuy.setFocusPainted(false);
        bHuy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bHuy.addActionListener(e -> dlg.dispose());

        JButton bOk = new JButton("Xác nhận kết ca") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#1A73E8"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        bOk.setIcon(MenuIcon.of("CHECK_CIRCLE", 16, Color.WHITE));
        bOk.setIconTextGap(6);
        bOk.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bOk.setPreferredSize(new Dimension(165, 40));
        bOk.setForeground(Color.WHITE);
        bOk.setContentAreaFilled(false);
        bOk.setBorderPainted(false);
        bOk.setFocusPainted(false);
        bOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bOk.addActionListener(e -> {
            long thucTe;
            try {
                thucTe = Long.parseLong(txtThucTe.getText().replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ex) {
                thucTe = tienHT;
            }
            if (ca != null) {
                ca.setTienKetCa(thucTe);
                ca.setTienHeThongGhiNhan((long) dtCa);
                ca.setTienDauCa(tienDauCa);
                ca.setGhiChuKetCa(txtNote.getText().trim());
                busCaLamViec.ketThucCa(ca);
            }
            UserSession.getInstance().logout();
            dlg.dispose();
            if (parentFrame != null)
                parentFrame.dispose();
            SwingUtilities.invokeLater(() -> new ManHinhDangNhap().setVisible(true));
        });

        ft.add(bHuy);
        ft.add(bOk);
        dlg.add(ft, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    /**
     * Helper: tạo 1 dòng hiển thị thông tin kết ca (label + số tiền) cho cột trái.
     * @param labelText   Nhãn mô tả
     * @param valueText   Giá trị tiền
     * @param labelColor  Màu nhãn
     * @param valueColor  Màu giá trị
     * @param isBig       true = font to đậm nổi bật (dòng TIỀN CẦN NỘP)
     */
    static JPanel buildKetCaRow(String labelText, String valueText,
                                  Color labelColor, Color valueColor, boolean isBig) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, isBig ? 56 : 40));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", isBig ? Font.BOLD : Font.PLAIN, isBig ? 15 : 13));
        lbl.setForeground(labelColor);

        JLabel val = new JLabel(valueText, SwingConstants.RIGHT);
        val.setFont(new Font("Segoe UI", Font.BOLD, isBig ? 22 : 15));
        val.setForeground(valueColor);

        if (isBig) {
            // Nền nổi bật cho dòng tổng
            JPanel valBox = new JPanel(new BorderLayout());
            valBox.setBackground(new Color(255, 86, 48, 18));  // đỏ nhạt
            valBox.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#FF5630"), 2, true),
                    new EmptyBorder(6, 14, 6, 14)));
            valBox.add(val, BorderLayout.CENTER);
            row.add(lbl,    BorderLayout.WEST);
            row.add(valBox, BorderLayout.EAST);
        } else {
            row.add(lbl, BorderLayout.WEST);
            row.add(val, BorderLayout.EAST);
        }
        return row;
    }

    private JPanel buildSection(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        p.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Color.GRAY);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        p.add(lbl, BorderLayout.WEST);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));
        return p;
    }

    private JPanel buildInfoRow(String label, String value, boolean bold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(2, 0, 2, 0));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(Color.DARK_GRAY);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 13));
        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    private JPanel buildMoneyRow(String label, String value, Color valueColor, boolean editable) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(3, 0, 3, 0));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", editable ? Font.BOLD : Font.PLAIN, 13));
        l.setForeground(editable ? Color.decode("#152A4B") : Color.DARK_GRAY);
        JPanel fakeField = new JPanel(new BorderLayout());
        if (editable) {
            // Vùng 3 — tổng: nền nổi bật #EEF2FF, viền xanh đậm
            fakeField.setBackground(Color.decode("#EEF2FF"));
            fakeField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#152A4B"), 1, true),
                    new EmptyBorder(5, 12, 5, 12)));
            fakeField.setPreferredSize(new Dimension(200, 32));
        } else {
            fakeField.setBackground(Color.decode("#F0FAFB"));
            fakeField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#B2EBF2"), 1, true),
                    new EmptyBorder(4, 10, 4, 10)));
            fakeField.setPreferredSize(new Dimension(200, 30));
        }
        JLabel v = new JLabel(value, SwingConstants.RIGHT);
        v.setFont(new Font("Segoe UI", editable ? Font.BOLD : Font.PLAIN, editable ? 14 : 13));
        v.setForeground(valueColor);
        fakeField.add(v, BorderLayout.EAST);
        row.add(l, BorderLayout.WEST);
        row.add(fakeField, BorderLayout.EAST);
        return row;
    }

    private JPanel buildLabelFieldRow(String label, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(3, 0, 3, 0));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(Color.DARK_GRAY);
        field.setPreferredSize(new Dimension(200, 30));
        row.add(l, BorderLayout.WEST);
        row.add(field, BorderLayout.EAST);
        return row;
    }

    private JPanel buildLabelValueRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(3, 0, 3, 0));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(Color.DARK_GRAY);
        row.add(l, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JTextField buildMoneyField(String initialValue) {
        JTextField f = new JTextField(initialValue);
        f.setFont(new Font("Segoe UI", Font.BOLD, 13));
        f.setHorizontalAlignment(SwingConstants.RIGHT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#1A73E8"), 1, true),
                new EmptyBorder(4, 10, 4, 10)));
        return f;
    }

    static String formatChenhLech(long lech) {
        if (lech == 0)
            return "0 đ";
        return (lech > 0 ? "+ " : "- ") + formatMoney(Math.abs(lech));
    }

    private JPanel barChart(String title, double[] data) {
        JPanel wp = wCard();
        wp.setLayout(new BorderLayout(0, 6));
        JLabel lb = new JLabel(title);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 12));
        wp.add(lb, BorderLayout.NORTH);
        double[] fd = data;
        JPanel ch = new JPanel() {
            int hoveredIndex = -1;
            {
                addMouseMotionListener(new MouseAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        int W = getWidth(), bw = (W - 20) / 12;
                        int newHover = -1;
                        for (int i = 0; i < 12; i++) {
                            int x = 10 + i * bw;
                            if (e.getX() >= x && e.getX() <= x + bw) {
                                newHover = i;
                                break;
                            }
                        }
                        if (hoveredIndex != newHover) {
                            hoveredIndex = newHover;
                            repaint();
                        }
                    }
                });
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseExited(MouseEvent e) {
                        hoveredIndex = -1;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int W = getWidth(), H = getHeight() - 20;
                double mx = 1;
                for (double d : fd)
                    if (d > mx)
                        mx = d;
                int bw = (W - 20) / 12;
                String[] ms = { "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12" };
                for (int i = 0; i < 12; i++) {
                    int bh = (int) ((fd[i] / mx) * (H - 25));
                    int x = 10 + i * bw, y = H - bh - 5;
                    g2.setColor(
                            i == hoveredIndex ? Color.decode("#0052CC") : (fd[i] > 0 ? BLUE : Color.decode("#EEF2F6")));
                    g2.fillRoundRect(x + 2, y, bw - 5, bh, 3, 3);
                    g2.setColor(Color.GRAY);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
                    g2.drawString(ms[i], x + 2, H + 14);
                    if (i == hoveredIndex && fd[i] > 0) {
                        String val = formatMoney(Math.round(fd[i]));
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        int textWidth = g2.getFontMetrics().stringWidth(val);
                        int boxWidth = textWidth + 10;
                        g2.setColor(new Color(0, 0, 0, 200));
                        g2.fillRoundRect(x - 5, y - 25, boxWidth, 20, 5, 5);
                        g2.setColor(Color.WHITE);
                        g2.drawString(val, x, y - 11);
                    }
                }
                g2.dispose();
            }
        };
        ch.setBackground(Color.WHITE);
        wp.add(ch, BorderLayout.CENTER);
        return wp;
    }

    private JPanel donutChart(String title, int[] vals, String[] names, Color[] colors) {
        JPanel wp = wCard();
        wp.setLayout(new BorderLayout(0, 6));
        JLabel lb = new JLabel(title);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 12));
        wp.add(lb, BorderLayout.NORTH);
        JPanel ch = new JPanel() {
            int hoveredIndex = -1;
            {
                addMouseMotionListener(new MouseAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        int cx = getWidth() / 2 - 40, cy = getHeight() / 2;
                        int r = Math.min(cx, cy) - 15;
                        double dx = e.getX() - cx, dy = e.getY() - cy;
                        double dist = Math.sqrt(dx * dx + dy * dy);
                        if (dist >= r / 2.0 && dist <= r) {
                            // +90 để đồng bộ với góc vẽ bắt đầu -90 (12 giờ)
                            double angle = (Math.toDegrees(Math.atan2(dy, dx)) + 90 + 360) % 360;
                            int tot = 0;
                            for (int v : vals)
                                tot += v;
                            if (tot == 0)
                                tot = 1;
                            double currentAngle = 0;
                            int newHover = -1;
                            for (int i = 0; i < vals.length; i++) {
                                double sweep = 360.0 * vals[i] / tot;
                                if (angle >= currentAngle && angle < currentAngle + sweep) {
                                    newHover = i;
                                    break;
                                }
                                currentAngle += sweep;
                            }
                            if (hoveredIndex != newHover) {
                                hoveredIndex = newHover;
                                repaint();
                            }
                        } else {
                            if (hoveredIndex != -1) {
                                hoveredIndex = -1;
                                repaint();
                            }
                        }
                    }
                });
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseExited(MouseEvent e) {
                        hoveredIndex = -1;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int tot = 0;
                for (int v : vals)
                    tot += v;
                if (tot == 0)
                    tot = 1;
                int cx = getWidth() / 2 - 40, cy = getHeight() / 2, r = Math.min(cx, cy) - 15;
                double ang = 90;
                for (int i = 0; i < vals.length; i++) {
                    double sw = 360.0 * vals[i] / tot;
                    g2.setColor(i == hoveredIndex ? colors[i].darker() : colors[i]);
                    g2.fillArc(cx - r, cy - r, 2 * r, 2 * r, (int) Math.round(ang), (int) Math.ceil(-sw));
                    ang -= sw;
                }
                g2.setColor(Color.WHITE);
                g2.fillOval(cx - r / 2, cy - r / 2, r, r);
                // Luôn hiển thị label trung tâm
                g2.setColor(Color.DARK_GRAY);
                String centerLabel = hoveredIndex >= 0 ? (vals[hoveredIndex] + " SP") : (tot + " SP");
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                int w = g2.getFontMetrics().stringWidth(centerLabel);
                g2.drawString(centerLabel, cx - w / 2, cy + 5);
                // Legend
                int lx = cx + r + 8, ly = cy - vals.length * 10;
                for (int i = 0; i < vals.length; i++) {
                    g2.setColor(colors[i]);
                    g2.fillRect(lx, ly + i * 18, 8, 8);
                    g2.setColor(i == hoveredIndex ? colors[i].darker() : Color.DARK_GRAY);
                    g2.setFont(new Font("Segoe UI", i == hoveredIndex ? Font.BOLD : Font.PLAIN, 10));
                    g2.drawString(names[i] + " (" + vals[i] + ")", lx + 12, ly + i * 18 + 8);
                }
                g2.dispose();
            }
        };
        ch.setBackground(Color.WHITE);
        wp.add(ch, BorderLayout.CENTER);
        return wp;
    }

    private JPanel wCard() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#EEF2F6")),
                new EmptyBorder(12, 14, 12, 14)));
        return p;
    }

    private void addLine(JPanel p, String lbl, String val, Color fg, boolean bold) {
        addLine(p, null, lbl, val, fg, bold);
    }

    private void addLine(JPanel p, String iconType, String lbl, String val, Color fg, boolean bold) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        JLabel l = new JLabel(lbl);
        if (iconType != null) {
            l.setIcon(new MenuIcon(iconType));
            l.setIconTextGap(6);
        }
        l.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 11));
        if (val == null) {
            l.setForeground(fg);
            row.add(l, BorderLayout.WEST);
        } else {
            JLabel v = new JLabel(val, SwingConstants.RIGHT);
            v.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 11));
            v.setForeground(fg);
            row.add(l, BorderLayout.WEST);
            row.add(v, BorderLayout.EAST);
        }
        p.add(row);
        p.add(box(3));
    }

    private static Component box(int h) {
        return Box.createRigidArea(new Dimension(0, h));
    }

    static String formatMoney(long v) {
        return new DecimalFormat("###,###,###").format(v).replace(",", ".") + " đ";
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(Color.DARK_GRAY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void styleInput(JTextField txt) {
        txt.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8")),
                new EmptyBorder(5, 12, 5, 12)));
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void chuyenCardDoiTra(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof ManHinhBanHang) {
                Container parent = c.getParent();
                if (parent != null && parent.getLayout() instanceof CardLayout) {
                    ((CardLayout) parent.getLayout()).show(parent, "DoiTra");
                    return;
                }
            }
            if (c instanceof Container) {
                chuyenCardDoiTra((Container) c);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // XEM LẠI BILL KẾT CA – Chế độ View-Only dành cho Màn Hình Thống Kê
    // Được gọi từ ManHinhThongKe khi double-click vào một nhân viên.
    // Không có nút Xác nhận, toàn bộ số liệu là read-only, lấy từ CaLamViec đã lưu.
    // ══════════════════════════════════════════════════════════════════════════
    public static void hienThiBillKetCaLichSu(Frame parentFrame, Entity.CaLamViec ca, String tenNV) {
        if (ca == null) return;

        // ── Số liệu lấy trực tiếp từ ca đã đóng ─────────────────────────────
        long tienDauCa = (long) ca.getTienDauCa();
        long tienHT    = (long) ca.getTienHeThongGhiNhan(); // đã lưu khi kết ca
        long tienKetCa = (long) ca.getTienKetCa();
        long chenh     = tienKetCa - tienHT;
        String ghiChu  = ca.getGhiChuKetCa() != null ? ca.getGhiChuKetCa() : "";

        // ── Thông tin header ─────────────────────────────────────────────────
        String[] CA_LABELS = { "Ca Sáng", "Ca Chiều", "Ca Tối" };
        String tenCa = (ca.getLoaiCa() >= 0 && ca.getLoaiCa() <= 2) ? CA_LABELS[ca.getLoaiCa()] : "—";
        String gioBD = "—", gioKT = "—";
        if (ca.getThoiGianBatDau() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy  HH:mm");
            gioBD = sdf.format(java.sql.Timestamp.valueOf(ca.getThoiGianBatDau()));
        }
        if (ca.getThoiGianKetThuc() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
            gioKT = sdf.format(java.sql.Timestamp.valueOf(ca.getThoiGianKetThuc()));
        }

        // ── Dialog ───────────────────────────────────────────────────────────
        JDialog dlg = new JDialog(parentFrame, true);
        dlg.setUndecorated(true);
        dlg.getRootPane().setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1));
        dlg.setSize(900, 520);
        dlg.setLocationRelativeTo(parentFrame);
        dlg.setLayout(new BorderLayout());
        dlg.setResizable(false);
        dlg.getContentPane().setBackground(Color.WHITE);

        // ══ HEADER ════════════════════════════════════════════════════════════
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Color.decode("#152A4B"), getWidth(), 0, Color.decode("#1A73E8")));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(14, 24, 14, 18));

        JPanel hdrLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        hdrLeft.setOpaque(false);

        JLabel hTitle = new JLabel("XEM LẠI BILL KẾT CA");
        hTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        hTitle.setForeground(Color.WHITE);

        JLabel hSubtitle = new JLabel(
                "   " + tenNV + "  ·  " + tenCa + "  ·  Bắt đầu: " + gioBD + "  ·  Kết thúc: " + gioKT);
        hSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hSubtitle.setForeground(new Color(255, 255, 255, 180));

        hdrLeft.add(hTitle);
        hdrLeft.add(hSubtitle);

        JButton btnClose = new JButton(new MenuIcon("CLOSE"));
        btnClose.setForeground(new Color(255, 255, 255, 160));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setPreferredSize(new Dimension(32, 32));
        btnClose.addActionListener(e -> dlg.dispose());
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnClose.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent  e) { btnClose.setForeground(new Color(255, 255, 255, 160)); }
        });

        hdr.add(hdrLeft,  BorderLayout.WEST);
        hdr.add(btnClose, BorderLayout.EAST);
        dlg.add(hdr, BorderLayout.NORTH);

        // ══ BODY — 2 CỘT ══════════════════════════════════════════════════════
        JPanel body = new JPanel(new GridLayout(1, 2, 1, 0));
        body.setBackground(Color.decode("#DFE3E8"));
        dlg.add(body, BorderLayout.CENTER);

        // ── CỘT TRÁI: Số liệu hệ thống đã ghi nhận (read-only) ───────────────
        JPanel colLeft = new JPanel();
        colLeft.setLayout(new BoxLayout(colLeft, BoxLayout.Y_AXIS));
        colLeft.setBackground(Color.WHITE);
        colLeft.setBorder(new EmptyBorder(28, 32, 28, 28));

        JLabel lblLeftTitle = new JLabel("Số liệu hệ thống đã ghi nhận");
        lblLeftTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLeftTitle.setForeground(Color.decode("#637381"));
        lblLeftTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        colLeft.add(lblLeftTitle);
        colLeft.add(Box.createRigidArea(new Dimension(0, 18)));

        // Tiền đầu ca
        colLeft.add(buildKetCaRow(
            "Tiền đầu ca",
            formatMoney(tienDauCa),
            Color.decode("#212B36"), Color.decode("#212B36"), false));
        colLeft.add(Box.createRigidArea(new Dimension(0, 10)));

        JSeparator sep1 = new JSeparator();
        sep1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep1.setForeground(Color.decode("#EEF2F6"));
        sep1.setAlignmentX(Component.LEFT_ALIGNMENT);
        colLeft.add(sep1);
        colLeft.add(Box.createRigidArea(new Dimension(0, 10)));

        // Tiền phát sinh ròng trong ca (= tienHT - tienDauCa)
        long netFlow = tienHT - tienDauCa;
        String netFlowStr = (netFlow >= 0 ? "+ " : "− ") + formatMoney(Math.abs(netFlow));
        colLeft.add(buildKetCaRow(
            "Tiền phát sinh ròng trong ca (TM)",
            netFlowStr,
            Color.decode("#637381"),
            netFlow >= 0 ? Color.decode("#00A76F") : Color.decode("#FF5630"),
            false));
        colLeft.add(Box.createRigidArea(new Dimension(0, 16)));

        // Đường kẻ đậm trước dòng tổng
        JSeparator sep2 = new JSeparator();
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep2.setForeground(Color.decode("#152A4B"));
        sep2.setBackground(Color.decode("#152A4B"));
        sep2.setAlignmentX(Component.LEFT_ALIGNMENT);
        colLeft.add(sep2);
        colLeft.add(Box.createRigidArea(new Dimension(0, 16)));

        // TIỀN CẦN NỘP (đã ghi nhận)
        colLeft.add(buildKetCaRow(
            "TIỀN CẦN NỘP  (Tiền mặt — đã lưu)",
            formatMoney(tienHT),
            Color.decode("#152A4B"), Color.decode("#FF5630"), true));

        colLeft.add(Box.createVerticalGlue());
        body.add(colLeft);

        // ── CỘT PHẢI: Xác nhận nhân viên (read-only) ─────────────────────────
        JPanel colRight = new JPanel();
        colRight.setLayout(new BoxLayout(colRight, BoxLayout.Y_AXIS));
        colRight.setBackground(Color.decode("#F4F6F8"));
        colRight.setBorder(new EmptyBorder(28, 28, 28, 32));

        // Badge "Chỉ xem"
        JLabel badgeXemLai = new JLabel("  CHỈ XEM  ");
        badgeXemLai.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeXemLai.setForeground(Color.WHITE);
        badgeXemLai.setBackground(Color.decode("#637381"));
        badgeXemLai.setOpaque(true);
        badgeXemLai.setBorder(new EmptyBorder(2, 6, 2, 6));
        badgeXemLai.setAlignmentX(Component.LEFT_ALIGNMENT);
        colRight.add(badgeXemLai);
        colRight.add(Box.createRigidArea(new Dimension(0, 14)));

        JLabel lblThucTe = new JLabel("Tiền thực đếm được (VNĐ)");
        lblThucTe.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblThucTe.setForeground(Color.decode("#212B36"));
        lblThucTe.setAlignmentX(Component.LEFT_ALIGNMENT);
        colRight.add(lblThucTe);
        colRight.add(Box.createRigidArea(new Dimension(0, 8)));

        // Hiển thị tienKetCa — non-editable, styled như original
        JTextField txtThucTe = new JTextField(
                new java.text.DecimalFormat("###,###,###").format(tienKetCa).replace(",", "."));
        txtThucTe.setFont(new Font("Segoe UI", Font.BOLD, 22));
        txtThucTe.setForeground(Color.decode("#1A73E8"));
        txtThucTe.setHorizontalAlignment(SwingConstants.RIGHT);
        txtThucTe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        txtThucTe.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtThucTe.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 2, true),
                new EmptyBorder(8, 14, 8, 14)));
        txtThucTe.setBackground(Color.decode("#F4F6F8"));
        txtThucTe.setEditable(false);
        colRight.add(txtThucTe);
        colRight.add(Box.createRigidArea(new Dimension(0, 6)));

        // Chênh lệch tĩnh
        String chenhStr;
        Color chenhColor;
        if (chenh == 0) {
            chenhStr  = "✓  Khớp với hệ thống";
            chenhColor = Color.decode("#00A76F");
        } else {
            chenhStr  = "Lệch: " + formatChenhLech(chenh);
            chenhColor = Color.decode("#FF5630");
        }
        JLabel lblChenh = new JLabel(chenhStr, SwingConstants.RIGHT);
        lblChenh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblChenh.setForeground(chenhColor);
        lblChenh.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblChenh.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        colRight.add(lblChenh);
        colRight.add(Box.createRigidArea(new Dimension(0, 20)));

        // Ghi chú
        JLabel lblNote = new JLabel("Ghi chú");
        lblNote.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNote.setForeground(Color.decode("#212B36"));
        lblNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        colRight.add(lblNote);
        colRight.add(Box.createRigidArea(new Dimension(0, 8)));

        JTextArea txtNote = new JTextArea(ghiChu, 4, 20);
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        txtNote.setBackground(Color.decode("#EDEFF1"));
        txtNote.setEditable(false);
        txtNote.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        txtNote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        txtNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (ghiChu.isEmpty()) {
            txtNote.setText("(Không có ghi chú)");
            txtNote.setForeground(Color.GRAY);
        }
        colRight.add(txtNote);
        colRight.add(Box.createVerticalGlue());
        body.add(colRight);

        // ══ FOOTER ════════════════════════════════════════════════════════════
        JPanel ft = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        ft.setBackground(Color.WHITE);
        ft.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#DFE3E8")));

        JButton bDong = new JButton("Đóng") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#637381"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        bDong.setIcon(MenuIcon.of("CLOSE", 15, Color.WHITE));
        bDong.setIconTextGap(6);
        bDong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bDong.setPreferredSize(new Dimension(120, 40));
        bDong.setForeground(Color.WHITE);
        bDong.setContentAreaFilled(false);
        bDong.setBorderPainted(false);
        bDong.setFocusPainted(false);
        bDong.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bDong.addActionListener(e -> dlg.dispose());

        ft.add(bDong);
        dlg.add(ft, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}