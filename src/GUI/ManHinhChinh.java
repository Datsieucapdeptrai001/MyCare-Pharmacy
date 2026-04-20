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

public class ManHinhChinh extends JPanel {

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JButton btnTongQuan, btnDoiChieu;
    private int[] soLuongTien = new int[9];
    private final long[] MENH_GIA = {500000, 200000, 100000, 50000, 20000, 10000, 5000, 2000, 1000};
    private final String[] MENH_GIA_STR = {"500.000đ", "200.000đ", "100.000đ", "50.000đ", "20.000đ", "10.000đ", "5.000đ", "2.000đ", "1.000đ"};

    private static final Color GREEN = Color.decode("#00A76F");
    private static final Color BLUE = Color.decode("#1A73E8");
    private static final Color RED = Color.decode("#FF5630");
    private static final Color ORANGE = Color.decode("#FFAB00");
    private static final Color BG = Color.decode("#F4F6F8");

    private final BUS_ThongKe busThongKe;
    private final BUS_CaLamViec busCaLamViec;

    // --- BIẾN TOÀN CỤC CHO FILTER ---
    private int filterCa = 0; // 0: All, 1: Sáng, 2: Chiều, 3: Tối
    private String filterMaNV = "";
    private JButton[] caButtons = new JButton[4];
    private JComboBox<String> cbxNhanVien;
    private List<String> listMaNV = new ArrayList<>();

    public ManHinhChinh() {
        busThongKe = new BUS_ThongKe();
        busCaLamViec = new BUS_CaLamViec();

        // Nếu là nhân viên thì gán cứng mã NV, không cho xem người khác
        if (!UserSession.getInstance().isAdmin()) {
            filterMaNV = UserSession.getInstance().getMaNhanVien();
        }

        setLayout(new BorderLayout());
        setBackground(BG);

        // Header chứa Nút Tab + Filter Bar (Nằm chết ở trên cùng)
        JPanel headerWrapper = new JPanel();
        headerWrapper.setLayout(new BoxLayout(headerWrapper, BoxLayout.Y_AXIS));
        headerWrapper.add(createTopHeader());
        if (UserSession.getInstance().isAdmin()) {
            headerWrapper.add(buildAdminFilterBar()); // Chỉ hiện Filter cho Quản lý
        }
        add(headerWrapper, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BG);

        loadCardPanels(); // Khởi tạo giao diện lần đầu

        add(cardPanel, BorderLayout.CENTER);
    }

    // Hàm Refresh Giao diện khi chọn Filter
    private void loadCardPanels() {
        cardPanel.removeAll();
        cardPanel.add(createTongQuanPanel(), "TongQuan");
        cardPanel.add(createDoiChieuPanel(), "DoiChieu");
        cardPanel.revalidate();
        cardPanel.repaint();

        if (btnDoiChieu != null && btnDoiChieu.getForeground().equals(BLUE)) {
            cardLayout.show(cardPanel, "DoiChieu");
        } else {
            cardLayout.show(cardPanel, "TongQuan");
        }
    }

    // ── HEADER ──────────────────────────────────────────────
    private JPanel createTopHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(Color.WHITE);
        hdr.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));
        hdr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabs.setBackground(Color.WHITE);
        btnTongQuan = makeTabBtn("Tổng quan", true, "TAB_CHART");
        btnDoiChieu = makeTabBtn("Đối chiếu doanh thu", false, "TAB_DOLLAR");
        btnTongQuan.addActionListener(e -> { setActiveTab(btnTongQuan, btnDoiChieu); cardLayout.show(cardPanel, "TongQuan"); });
        btnDoiChieu.addActionListener(e -> { setActiveTab(btnDoiChieu, btnTongQuan); cardLayout.show(cardPanel, "DoiChieu"); });
        tabs.add(btnTongQuan); tabs.add(btnDoiChieu);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        right.setBackground(Color.WHITE);

        String ten = UserSession.getInstance().getTenHienThi();
        JLabel lblOn = new JLabel("● Ca đang mở — " + ten);
        lblOn.setFont(new Font("Segoe UI", Font.BOLD, 11)); lblOn.setForeground(GREEN);
        lblOn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GREEN, 1), new EmptyBorder(4, 10, 4, 10)));

        JButton btnKet = new JButton("⏹ Kết ca");
        btnKet.setFont(new Font("Segoe UI", Font.BOLD, 11)); btnKet.setForeground(RED);
        btnKet.setBackground(Color.WHITE); btnKet.setFocusPainted(false);
        btnKet.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(RED, 1), new EmptyBorder(4, 10, 4, 10)));
        btnKet.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnKet.addActionListener(e -> openKetCaDialog());

        if (UserSession.getInstance().isAdmin()) {
            lblOn.setVisible(false); btnKet.setVisible(false);
        }
        right.add(lblOn); right.add(btnKet);
        hdr.add(tabs, BorderLayout.WEST); hdr.add(right, BorderLayout.EAST);
        return hdr;
    }

    // ── THANH CÔNG CỤ FILTER CHO QUẢN LÝ ────────────────────
    private JPanel buildAdminFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")), new EmptyBorder(2, 10, 2, 10)));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lblTitle = new JLabel("Lọc dữ liệu:");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12)); lblTitle.setForeground(Color.decode("#637381"));

        caButtons[0] = makeFilterBtn("Toàn thời gian");
        caButtons[1] = makeFilterBtn("Ca Sáng (06-14h)");
        caButtons[2] = makeFilterBtn("Ca Chiều (14-22h)");
        caButtons[3] = makeFilterBtn("Ca Tối (22-06h)");

        for (int i = 0; i < 4; i++) {
            final int caIndex = i;
            caButtons[i].addActionListener(e -> {
                filterCa = caIndex;
                updateCaButtonsUI();
                loadCardPanels(); // Chạy lại logic load số khi đổi Ca
            });
            bar.add(caButtons[i]);
        }
        updateCaButtonsUI();

        // Combobox Nhân Viên Real từ Database
        cbxNhanVien = new JComboBox<>();
        cbxNhanVien.setFont(new Font("Segoe UI", Font.PLAIN, 12)); cbxNhanVien.setBackground(Color.WHITE);
        
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
                loadCardPanels(); // Chạy lại logic load số khi đổi NV
            }
        });

        bar.add(new JLabel(" |  Nhân viên:"));
        bar.add(cbxNhanVien);

        return bar;
    }

    private JButton makeFilterBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11)); b.setBackground(Color.WHITE);
        b.setForeground(Color.GRAY); b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
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

    private JButton makeTabBtn(String text, boolean active, String icon) {
        JButton b = new JButton(text);
        b.setIcon(new MenuIcon(icon)); b.setIconTextGap(8);
        b.setPreferredSize(new Dimension(200, 44));
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false); b.setContentAreaFilled(false); b.setOpaque(true);
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createMatteBorder(0, 0, active ? 3 : 0, 0, BLUE));
        b.setForeground(active ? BLUE : Color.GRAY);
        return b;
    }

    private void setActiveTab(JButton a, JButton b) {
        a.setForeground(BLUE); a.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, BLUE));
        b.setForeground(Color.GRAY); b.setBorder(BorderFactory.createEmptyBorder());
    }

    // ── TỔNG QUAN ───────────────────────────────────────────
    private JPanel createTongQuanPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(BG); root.setBorder(new EmptyBorder(14, 20, 14, 20));
        String titleCa = filterCa == 0 ? "Hôm nay" : ("Ca " + filterCa);
        JLabel title = new JLabel("<html><b style='font-size:15px;'>MÀN HÌNH CHÍNH</b><br><span style='color:gray;font-size:11px;'>Tổng quan hoạt động — " + titleCa + "</span></html>");
        root.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel(); body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS)); body.setBackground(BG);

        if (!UserSession.getInstance().isAdmin() && UserSession.getInstance().daMoCa()) {
            body.add(buildTienDauCaBanner()); body.add(box(12));
        }

        body.add(buildKPIRow());   body.add(box(14));
        body.add(buildChartRow()); body.add(box(14));
        body.add(buildBottomRow()); body.add(box(14));
        body.add(buildExpiringPanel());

        JScrollPane sc = new JScrollPane(body); sc.setBorder(null); sc.getVerticalScrollBar().setUnitIncrement(14);
        root.add(sc, BorderLayout.CENTER); return root;
    }

    private JPanel buildTienDauCaBanner() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(Color.decode("#E6F7F2"));
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#B2DFDB")), new EmptyBorder(10, 16, 10, 16)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        CaLamViec ca = UserSession.getInstance().getCaHienTai();
        long tienDau = UserSession.getInstance().getTienDauCa();
        String caLabel = ca != null ? (ca.getLoaiCa() == 0 ? "Ca Sáng" : ca.getLoaiCa() == 1 ? "Ca Chiều" : "Ca Tối") : "";
        String thoiGian = ca != null ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Timestamp.valueOf(ca.getThoiGianBatDau())) : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());

        JPanel left = new JPanel(); left.setOpaque(false); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel l1 = new JLabel("TIỀN ĐẦU CA  " + thoiGian + "  ·  " + UserSession.getInstance().getTenHienThi());
        l1.setFont(new Font("Segoe UI", Font.BOLD, 11)); l1.setForeground(Color.decode("#006250"));
        JLabel l2 = new JLabel(formatMoney(tienDau) + " × 1   (" + caLabel + ")"); l2.setFont(new Font("Segoe UI", Font.PLAIN, 11)); l2.setForeground(Color.decode("#637381"));
        left.add(l1); left.add(box(3)); left.add(l2);

        JPanel right = new JPanel(); right.setOpaque(false); right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        JLabel lTl = new JLabel("Tổng tiền đầu ca", SwingConstants.RIGHT); lTl.setFont(new Font("Segoe UI", Font.PLAIN, 10)); lTl.setForeground(Color.GRAY); lTl.setAlignmentX(RIGHT_ALIGNMENT);
        JLabel lAm = new JLabel(formatMoney(tienDau), SwingConstants.RIGHT); lAm.setFont(new Font("Segoe UI", Font.BOLD, 20)); lAm.setForeground(Color.decode("#006250")); lAm.setAlignmentX(RIGHT_ALIGNMENT);
        right.add(lTl); right.add(lAm);

        p.add(left, BorderLayout.WEST); p.add(right, BorderLayout.EAST); return p;
    }

    private JPanel buildKPIRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false); 
        // ĐÃ TĂNG CHIỀU CAO TỐI ĐA LÊN 130 ĐỂ CHỮ KHÔNG BỊ ÉP CẮT CHÂN
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        // ÁP DỤNG BIẾN FILTER VÀO LOGIC LẤY SỐ
        int sp = busThongKe.getTongSanPham();
        int hd = busThongKe.getHoaDonHomNay(filterMaNV, filterCa);
        double dt = busThongKe.getDoanhThuHomNay(filterMaNV, filterCa);
        int kh = busThongKe.getTongKhachHang();

        row.add(kpiCard("Tổng sản phẩm", String.valueOf(sp), "Cửa hàng", "#EEF2FF", "#3D52A0", "💊"));
        row.add(kpiCard("Hóa đơn", String.valueOf(hd), "Đã lọc", "#ECFDF5", "#00A76F", "🧾"));
        row.add(kpiCard("Doanh thu", compactMoney(dt), "đ VND", "#FFF7ED", "#FF6B00", "💰"));
        row.add(kpiCard("Tổng khách hàng", String.valueOf(kh), "Cửa hàng", "#FFF0F0", "#FF5630", "👥"));
        return row;
    }

    private JPanel kpiCard(String ttl, String val, String sub, String bg, String fg, String icon) {
        JPanel p = new JPanel(new BorderLayout(6, 6)); 
        p.setBackground(Color.WHITE); 
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#EEF2F6")), new EmptyBorder(12, 14, 12, 14)));
        
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        JLabel lT = new JLabel(ttl); lT.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lT.setForeground(Color.GRAY);
        JLabel ico = new JLabel(icon, SwingConstants.CENTER); ico.setOpaque(true); ico.setBackground(Color.decode(bg)); ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16)); ico.setPreferredSize(new Dimension(36, 36));
        top.add(lT, BorderLayout.CENTER); top.add(ico, BorderLayout.EAST);
        
        // Cậu tha hồ thấy chữ to rõ không lo bị lẹm nữa nha
        JLabel lV = new JLabel(val); lV.setFont(new Font("Segoe UI", Font.BOLD, 24)); lV.setForeground(Color.decode(fg));
        JLabel lS = new JLabel(sub); lS.setFont(new Font("Segoe UI", Font.PLAIN, 11)); lS.setForeground(Color.GRAY);
        
        p.add(top, BorderLayout.NORTH); p.add(lV, BorderLayout.CENTER); p.add(lS, BorderLayout.SOUTH); 
        return p;
    }

    private JPanel buildChartRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));

        int currentYear = java.time.Year.now().getValue();
        double[] monthly = busThongKe.getDoanhThu12Thang(currentYear, filterMaNV);
        if (monthly == null) monthly = new double[12];
        for(int i = 0; i < monthly.length; i++) monthly[i] = monthly[i] * 1_000_000;
        
        int[] cats = busThongKe.getSoLuongTheoLoaiSP(currentYear, filterMaNV);
        if (cats == null) cats = new int[]{0, 0, 0, 0};

        row.add(barChart("Doanh thu theo tháng (" + currentYear + ")", monthly));
        row.add(donutChart("Phân loại sản phẩm", cats, new String[]{"Thuốc kê đơn", "Thuốc không kê đơn", "TPCN", "Mỹ phẩm"}, new Color[]{RED, BLUE, GREEN, ORANGE}));
        return row;
    }

    private JPanel buildBottomRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        // BẢNG HÓA ĐƠN
        JPanel inv = wCard(); inv.setLayout(new BorderLayout(0, 8));
        JLabel lbI = new JLabel("Hóa đơn đã lọc theo Ca/NV"); lbI.setFont(new Font("Segoe UI", Font.BOLD, 12));
        inv.add(lbI, BorderLayout.NORTH);

        DefaultTableModel mInv = new DefaultTableModel(new String[]{"Mã HĐ", "Khách hàng", "Tiền", "TT"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable tInv = new JTable(mInv); tInv.setFont(new Font("Segoe UI", Font.PLAIN, 11)); tInv.setRowHeight(26); tInv.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));

        // Gọi data truyền biến Filter
        List<Object[]> hdList = busThongKe.getHoaDonGanDayTrongCa(filterMaNV, filterCa);
        if (hdList != null && !hdList.isEmpty()) {
            for (Object[] o : hdList) mInv.addRow(new Object[]{o[0], o[1], formatMoney((long)((double)o[2])), "✓"});
        } else {
            mInv.addRow(new Object[]{"—", "Không có hóa đơn", "—", "—"});
        }
        inv.add(new JScrollPane(tInv), BorderLayout.CENTER);
        row.add(inv);

        // BẢNG SẢN PHẨM SẮP HẾT
        JPanel stk = wCard(); stk.setLayout(new BorderLayout(0, 8));
        JLabel lbS = new JLabel("⚠ Sản phẩm sắp hết hàng"); lbS.setFont(new Font("Segoe UI", Font.BOLD, 12)); lbS.setForeground(ORANGE);
        stk.add(lbS, BorderLayout.NORTH);
        JPanel listS = new JPanel(); listS.setLayout(new BoxLayout(listS, BoxLayout.Y_AXIS)); listS.setBackground(Color.WHITE);

        List<Object[]> ls = busThongKe.getTop4SanPhamSapHetHang();
        int sizeLS = ls != null ? ls.size() : 0;
        if (ls != null) {
            for (Object[] it : ls) {
                String nm = (String)it[0]; int tn = (int)it[1];
                JPanel r2 = new JPanel(new BorderLayout(0, 2)); r2.setOpaque(false); r2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
                JPanel t2 = new JPanel(new BorderLayout()); t2.setOpaque(false);
                JLabel ln = new JLabel(nm.length() > 32 ? nm.substring(0, 32) + "…" : nm); ln.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                JLabel lv = new JLabel(tn + " Hộp", SwingConstants.RIGHT); lv.setFont(new Font("Segoe UI", Font.BOLD, 11)); lv.setForeground(RED);
                t2.add(ln, BorderLayout.WEST); t2.add(lv, BorderLayout.EAST);
                JProgressBar pb = new JProgressBar(0, 50); pb.setValue(Math.min(tn, 50)); pb.setBackground(Color.decode("#FFF0E0")); pb.setForeground(ORANGE); pb.setPreferredSize(new Dimension(0, 5));
                r2.add(t2, BorderLayout.NORTH); r2.add(pb, BorderLayout.SOUTH); listS.add(r2); listS.add(box(5));
            }
        }
        int duTon = busThongKe.getSoSanPhamDuTon();
        JLabel lSum = new JLabel("<html><span style='color:#00A76F;'>✓ " + duTon + " sản phẩm đủ tồn</span>  <span style='color:#FF5630;'>⏰ " + sizeLS + " cần nhập</span></html>");
        lSum.setFont(new Font("Segoe UI", Font.PLAIN, 10)); listS.add(lSum);
        stk.add(new JScrollPane(listS) {{ setBorder(null); }}, BorderLayout.CENTER);
        row.add(stk); return row;
    }

    private JPanel buildExpiringPanel() {
        JPanel p = wCard(); p.setLayout(new BorderLayout(0, 8)); p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 270));
        int cnt = busThongKe.getSoLoHangSapHetHanKhoang(90);
        JPanel h2 = new JPanel(new BorderLayout()); h2.setOpaque(false);
        JLabel lbT = new JLabel("⚠ Lô hàng sắp hết hạn (trong 90 ngày)"); lbT.setFont(new Font("Segoe UI", Font.BOLD, 12)); lbT.setForeground(ORANGE);
        JLabel badge = new JLabel(" " + cnt + " lô ", SwingConstants.CENTER); badge.setOpaque(true); badge.setBackground(ORANGE); badge.setForeground(Color.WHITE); badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        h2.add(lbT, BorderLayout.WEST); h2.add(badge, BorderLayout.EAST); p.add(h2, BorderLayout.NORTH);

        DefaultTableModel m = new DefaultTableModel(new String[]{"Sản phẩm", "Mã lô", "Tồn", "HSD", "Còn lại"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable t = new JTable(m); t.setFont(new Font("Segoe UI", Font.PLAIN, 11)); t.setRowHeight(26); t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        List<Object[]> dsHetHan = busThongKe.getLoHangSapHetHanNhanh(90);
        if (dsHetHan != null) {
            for(Object[] rs : dsHetHan) {
                int cl = (int)rs[4];
                m.addRow(new Object[]{rs[0], rs[1], String.format("%,d", (int)rs[2]), sdf.format((Timestamp)rs[3]), cl <= 0 ? "Đã hết hạn" : cl + " ngày"});
            }
        }
        t.getColumnModel().getColumn(4).setCellRenderer((tbl2, val, sel, foc, r, c) -> {
            JLabel l = new JLabel(String.valueOf(val), SwingConstants.CENTER); l.setOpaque(true); l.setFont(new Font("Segoe UI", Font.BOLD, 10));
            String v = String.valueOf(val);
            if (v.equals("Đã hết hạn")) { l.setBackground(Color.decode("#FFEBEE")); l.setForeground(RED); }
            else if (v.contains("ngày") && Integer.parseInt(v.replace(" ngày", "")) <= 30) { l.setBackground(Color.decode("#FFF3E0")); l.setForeground(ORANGE); }
            else { l.setBackground(Color.WHITE); l.setForeground(Color.DARK_GRAY); }
            return l;
        });
        p.add(new JScrollPane(t), BorderLayout.CENTER); return p;
    }

    // ── ĐỐI CHIẾU ───────────────────────────────────────────
    private JPanel createDoiChieuPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(BG); root.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel("<html><b style='font-size:15px;'>ĐỐI CHIẾU DOANH THU</b><br><span style='color:gray;font-size:11px;'>Đã áp dụng bộ lọc Ca/Nhân viên</span></html>");

        JPanel topRow = new JPanel(new BorderLayout()); topRow.setOpaque(false);
        topRow.add(title, BorderLayout.WEST);
        root.add(topRow, BorderLayout.NORTH);

        boolean isAdmin = UserSession.getInstance().isAdmin();

        // ÁP DỤNG FILTER
        int soHD = busThongKe.getHoaDonHomNay(filterMaNV, filterCa);
        double tHang = busThongKe.getDoanhThuHomNay(filterMaNV, filterCa);
        double tMat = busThongKe.getDoanhThuTienMatHomNay(filterMaNV, filterCa);
        double ck = tHang - tMat;

        JPanel body = new JPanel(new GridLayout(1, 3, 12, 0)); body.setBackground(BG);

        // Card HÔM NAY
        JPanel c1 = wCard(); c1.setLayout(new BoxLayout(c1, BoxLayout.Y_AXIS));
        addLine(c1, "📊 KẾT QUẢ ĐÃ LỌC", null, BLUE, true);
        addLine(c1, "Số hóa đơn:", String.valueOf(soHD), Color.BLACK, false);
        addLine(c1, "Tiền hàng:", formatMoney((long)tHang), Color.BLACK, false);
        addLine(c1, "Khuyến mãi:", "-0đ", GREEN, false);
        c1.add(new JSeparator() {{ setMaximumSize(new Dimension(Integer.MAX_VALUE, 1)); }});
        addLine(c1, "Tổng thanh toán:", formatMoney((long)tHang), BLUE, true);
        c1.add(box(8));
        addLine(c1, "ĐỐI CHIẾU THU CHI", null, Color.GRAY, false);
        addLine(c1, "⇄ Tiền mặt thực thu", formatMoney((long)tMat), Color.BLACK, false);
        addLine(c1, "↔ Chuyển khoản", formatMoney((long)ck), Color.BLACK, false);
        c1.add(new JSeparator() {{ setMaximumSize(new Dimension(Integer.MAX_VALUE, 1)); }});
        addLine(c1, "Tổng thực thu", formatMoney((long)tHang), BLUE, true);
        body.add(c1);

        // Card 7 NGÀY QUA
        JPanel c2 = wCard(); c2.setLayout(new BoxLayout(c2, BoxLayout.Y_AXIS));
        addLine(c2, "📊 7 NGÀY QUA", null, BLUE, true);
        if (!isAdmin) {
            JLabel lk = new JLabel("<html><center>🔒<br>Chỉ dành cho Quản lý</center></html>", SwingConstants.CENTER); lk.setFont(new Font("Segoe UI", Font.PLAIN, 11)); lk.setForeground(Color.GRAY); lk.setAlignmentX(CENTER_ALIGNMENT);
            c2.add(Box.createVerticalGlue()); c2.add(lk); c2.add(Box.createVerticalGlue());
        } else {
            double dt7 = busThongKe.getDoanhThu7NgayQua();
            int hd7 = busThongKe.getSoHoaDon7NgayQua();
            addLine(c2, "Số hóa đơn:", String.valueOf(hd7), Color.BLACK, false);
            addLine(c2, "Doanh thu:", formatMoney((long)dt7), GREEN, true);
        }
        body.add(c2);

        // Card ĐỔI TRẢ
        JPanel c3 = wCard(); c3.setLayout(new BoxLayout(c3, BoxLayout.Y_AXIS));
        addLine(c3, "↩ ĐỔI / TRẢ HÀNG", null, RED, true);
        int tongPhieu = busThongKe.getTongPhieuDoiTra();
        int choXuLy   = busThongKe.getPhieuDoiTraChoXuLy();
        addLine(c3, "Tổng phiếu:", String.valueOf(tongPhieu), Color.BLACK, false);
        addLine(c3, "Chờ xử lý:", String.valueOf(choXuLy), RED, false);
        body.add(c3);

        root.add(body, BorderLayout.CENTER); return root;
    }

 // ── KẾT CA DIALOG (ĐÃ CĂN GIỮA TOÀN BỘ) ───────────────────────────────────────
    private void openKetCaDialog() {
        soLuongTien = new int[9];
        Window pw = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog((Frame)pw, "MyCare Pharmacy - Kết Ca & Kiểm Kê", true);
        dlg.setSize(900, 720); dlg.setLocationRelativeTo(pw); dlg.setLayout(new BorderLayout());

        JPanel hdr = new JPanel(new BorderLayout()); 
        hdr.setBackground(Color.decode("#D32F2F")); 
        hdr.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel hT = new JLabel("⏹  BÁO CÁO TỔNG KẾT & KIỂM ĐẾM CUỐI CA"); 
        hT.setFont(new Font("Segoe UI", Font.BOLD, 18)); hT.setForeground(Color.WHITE);
        hdr.add(hT, BorderLayout.WEST); 
        dlg.add(hdr, BorderLayout.NORTH);

        JPanel body = new JPanel(); 
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS)); 
        body.setBackground(Color.decode("#F4F6F8")); 
        body.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel pDoanhThu = new JPanel(new BorderLayout());
        pDoanhThu.setBackground(Color.WHITE);
        pDoanhThu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1), 
            new EmptyBorder(15, 15, 15, 15)
        ));
        pDoanhThu.setMaximumSize(new Dimension(850, 120)); // Cố định chiều rộng
        pDoanhThu.setAlignmentX(CENTER_ALIGNMENT); // Căn giữa
        pDoanhThu.add(buildTongKetInDialog(), BorderLayout.CENTER);
        body.add(pDoanhThu); 
        body.add(box(25));

        JLabel lKD = new JLabel("💵 KIỂM ĐẾM TIỀN MẶT THỰC TẾ TRONG KÉT"); 
        lKD.setFont(new Font("Segoe UI", Font.BOLD, 14)); lKD.setForeground(Color.DARK_GRAY); 
        lKD.setAlignmentX(CENTER_ALIGNMENT); // Căn giữa
        
        JLabel lHint = new JLabel("Thao tác: Trái chuột (+) Tăng tờ  |  Phải chuột (-) Giảm tờ"); 
        lHint.setFont(new Font("Segoe UI", Font.ITALIC, 12)); lHint.setForeground(Color.GRAY); 
        lHint.setAlignmentX(CENTER_ALIGNMENT); // Căn giữa
        
        body.add(lKD); body.add(box(5)); body.add(lHint); body.add(box(15));

        Color[] bc = {BLUE, Color.decode("#9C27B0"), Color.decode("#4CAF50"), Color.decode("#FF9800"), RED, Color.decode("#E91E63"), Color.decode("#FF7043"), BLUE, Color.decode("#607D8B")};
        JPanel grid = new JPanel(new GridLayout(3, 3, 20, 15)); 
        grid.setBackground(Color.decode("#F4F6F8"));
        grid.setAlignmentX(CENTER_ALIGNMENT); // CĂN GIỮA GRID TIỀN
        grid.setMaximumSize(new Dimension(850, 350)); // Rộng ra 850px để ôm form đẹp
        
        JLabel lTong = new JLabel("Tổng đếm: 0đ", SwingConstants.RIGHT); 

        for (int i = 0; i < 9; i++) {
            final int idx = i;
            JPanel box2 = new JPanel(new BorderLayout(0, 5)) { 
                @Override protected void paintComponent(Graphics g) { 
                    Graphics2D g2 = (Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
                    g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15); 
                    g2.setColor(bc[idx]); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15); 
                    g2.dispose(); 
                } 
            };
            box2.setOpaque(false); box2.setBorder(new EmptyBorder(15, 10, 15, 10)); box2.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            JLabel lG = new JLabel(MENH_GIA_STR[i], SwingConstants.CENTER); 
            lG.setFont(new Font("Segoe UI", Font.BOLD, 14)); lG.setForeground(bc[i]);
            
            JLabel lC = new JLabel("0 tờ", SwingConstants.CENTER); 
            lC.setFont(new Font("Segoe UI", Font.BOLD, 20)); lC.setForeground(Color.DARK_GRAY);
            
            box2.add(lG, BorderLayout.NORTH); box2.add(lC, BorderLayout.CENTER);
            
            box2.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) soLuongTien[idx]++;
                    else if (SwingUtilities.isRightMouseButton(e) && soLuongTien[idx] > 0) soLuongTien[idx]--;
                    lC.setText(soLuongTien[idx] + " tờ");
                    long t = 0; for (int j = 0; j < 9; j++) t += soLuongTien[j] * MENH_GIA[j];
                    lTong.setText("Tổng đếm: " + formatMoney(t));
                }
            });
            grid.add(box2);
        }
        body.add(grid); body.add(box(20));

        JPanel pTong = new JPanel(new BorderLayout());
        pTong.setBackground(Color.WHITE);
        pTong.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#E0E0E0"), 1), new EmptyBorder(10, 20, 10, 20)));
        pTong.setAlignmentX(CENTER_ALIGNMENT); // Căn giữa thanh tổng tiền
        pTong.setMaximumSize(new Dimension(850, 60)); // Rộng bằng cái Grid
        
        lTong.setFont(new Font("Segoe UI", Font.BOLD, 22)); lTong.setForeground(Color.decode("#00A76F"));
        pTong.add(new JLabel("<html><i style='color:gray'>Kết quả kiểm đếm thực tế:</i></html>"), BorderLayout.WEST);
        pTong.add(lTong, BorderLayout.EAST);
        body.add(pTong); body.add(box(20));

        JScrollPane scroll = new JScrollPane(body); 
        scroll.setBorder(null);
        dlg.add(scroll, BorderLayout.CENTER);

        JPanel ft = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15)); 
        ft.setBackground(Color.WHITE); 
        ft.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#DFE3E8")));
        
        JButton bHuy = new JButton("Hủy bỏ"); 
        bHuy.setFont(new Font("Segoe UI", Font.PLAIN, 14)); bHuy.setPreferredSize(new Dimension(100, 40));
        bHuy.addActionListener(e -> dlg.dispose());
        
        JButton bXN  = new JButton("Tiếp tục chốt sổ ➔"); 
        bXN.setFont(new Font("Segoe UI", Font.BOLD, 14)); bXN.setPreferredSize(new Dimension(180, 40));
        bXN.setBackground(Color.decode("#D32F2F")); bXN.setForeground(Color.WHITE); bXN.setFocusPainted(false);
        bXN.addActionListener(e -> {
            long tongKC = 0; for (int j = 0; j < 9; j++) tongKC += soLuongTien[j] * MENH_GIA[j];
            String maNV = UserSession.getInstance().getMaNhanVien();
            CaLamViec ca = UserSession.getInstance().getCaHienTai();
            double dtCa = busThongKe.getDoanhThuTheoCa(maNV, ca != null ? ca.getThoiGianBatDau() : LocalDateTime.now());
            double tmCa = busThongKe.getDoanhThuTienMatTheoCa(maNV, ca != null ? ca.getThoiGianBatDau() : LocalDateTime.now());
            hienThiPhieuXacNhanCuoi(dlg, ca, maNV, dtCa, tmCa, tongKC);
        });
        
        ft.add(bHuy); ft.add(bXN);
        dlg.add(ft, BorderLayout.SOUTH); dlg.setVisible(true);
    }

    // ── FORM XÁC NHẬN CUỐI CÙNG (ĐẸP & SẠCH SẼ) ──────────────────────
    private void hienThiPhieuXacNhanCuoi(JDialog parentDlg, CaLamViec ca, String maNV, double dtCa, double tmCa, long tongKC) {
        JDialog confDlg = new JDialog(parentDlg, "Xác Nhận Xuất Ca", true);
        confDlg.setSize(450, 520); confDlg.setLocationRelativeTo(parentDlg);
        
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE); p.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel title = new JLabel("PHIẾU XÁC NHẬN KẾT CA", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18)); title.setAlignmentX(CENTER_ALIGNMENT);
        p.add(title); p.add(box(5));
        
        JLabel sub = new JLabel("Vui lòng kiểm tra kỹ số liệu trước khi đăng xuất", SwingConstants.CENTER);
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 12)); sub.setForeground(Color.GRAY); sub.setAlignmentX(CENTER_ALIGNMENT);
        p.add(sub); p.add(box(20));
        
        long tienDauCa = UserSession.getInstance().getTienDauCa();
        long tienCanCo = tienDauCa + (long)tmCa; 
        
        p.add(sRow("Nhân viên:", UserSession.getInstance().getTenHienThi()));
        p.add(box(10)); p.add(new JSeparator()); p.add(box(10));
        
        p.add(sRow("Tiền nạp đầu ca:", formatMoney(tienDauCa)));
        p.add(sRow("Doanh thu Tiền mặt:", formatMoney((long)tmCa)));
        p.add(sRow("Doanh thu Chuyển khoản:", formatMoney((long)(dtCa - tmCa))));
        p.add(box(10));
        
        JPanel pCanCo = new JPanel(new BorderLayout()); pCanCo.setBackground(Color.decode("#FFF0F0")); pCanCo.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel l1 = new JLabel("=> CẦN CÓ TRONG KÉT:"); l1.setFont(new Font("Segoe UI", Font.BOLD, 14)); l1.setForeground(RED);
        JLabel l2 = new JLabel(formatMoney(tienCanCo)); l2.setFont(new Font("Segoe UI", Font.BOLD, 16)); l2.setForeground(RED);
        pCanCo.add(l1, BorderLayout.WEST); pCanCo.add(l2, BorderLayout.EAST);
        pCanCo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.add(pCanCo); p.add(box(15));
        
        p.add(sRow("TIỀN KIỂM ĐẾM THỰC TẾ:", formatMoney(tongKC)));
        long lech = tongKC - tienCanCo; 
        JLabel lL = new JLabel((lech > 0 ? "+ " : lech < 0 ? "- " : "") + formatMoney(Math.abs(lech)));
        lL.setFont(new Font("Segoe UI", Font.BOLD, 14)); lL.setForeground(lech == 0 ? GREEN : RED); 
        
        JPanel pLech = new JPanel(new BorderLayout()); pLech.setOpaque(false); pLech.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        JLabel lLechTitle = new JLabel("Độ lệch quỹ:"); lLechTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pLech.add(lLechTitle, BorderLayout.WEST); pLech.add(lL, BorderLayout.EAST);
        p.add(pLech); p.add(box(25));

        JButton bOk = new JButton("XÁC NHẬN & ĐĂNG XUẤT");
        bOk.setFont(new Font("Segoe UI", Font.BOLD, 14)); bOk.setPreferredSize(new Dimension(100, 45));
        bOk.setBackground(RED); bOk.setForeground(Color.WHITE); bOk.setFocusPainted(false);
        bOk.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        bOk.addActionListener(e -> {
            if (ca != null) {
                ca.setTienKetCa(tongKC); ca.setTienHeThongGhiNhan(dtCa);
                busCaLamViec.ketThucCa(ca);
            }
            UserSession.getInstance().logout();
            confDlg.dispose(); parentDlg.dispose();
            Window win = SwingUtilities.getWindowAncestor(this);
            if (win != null) win.dispose();
            // KHỞI ĐỘNG LẠI MÀN HÌNH ĐĂNG NHẬP
            SwingUtilities.invokeLater(() -> new ManHinhDangNhap().setVisible(true));
        });
        
        p.add(bOk);
        confDlg.add(p); confDlg.setVisible(true);
    }

    private JPanel sRow(String lbl, String val) {
        JPanel r = new JPanel(new BorderLayout()); r.setOpaque(false); r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        JLabel l = new JLabel(lbl); l.setFont(new Font("Segoe UI", Font.PLAIN, 13)); l.setForeground(Color.DARK_GRAY);
        JLabel v = new JLabel(val); v.setFont(new Font("Segoe UI", Font.BOLD, 13));
        r.add(l, BorderLayout.WEST); r.add(v, BorderLayout.EAST); return r;
    }

    private JPanel buildTongKetInDialog() {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        
        double dtCa = 0; int soHD2 = 0; double tmCa2 = 0;
        String maNV = UserSession.getInstance().getMaNhanVien();
        CaLamViec ca = UserSession.getInstance().getCaHienTai();
        long tienDauCa = UserSession.getInstance().getTienDauCa(); 
        
        if (ca != null && maNV != null && !maNV.isEmpty()) {
            soHD2 = busThongKe.getSoHoaDonTheoCa(maNV, ca.getThoiGianBatDau());
            dtCa  = busThongKe.getDoanhThuTheoCa(maNV, ca.getThoiGianBatDau());
            tmCa2 = busThongKe.getDoanhThuTienMatTheoCa(maNV, ca.getThoiGianBatDau());
        }
        long tienCanCo = tienDauCa + (long)tmCa2; 

        JPanel row2 = new JPanel(new GridLayout(1, 5, 12, 0)); row2.setOpaque(false);
        row2.add(sBox("Tiền Nạp Đầu Ca", formatMoney(tienDauCa), "#FFF3E0", "#FF6B00"));
        row2.add(sBox("Doanh Thu TM", formatMoney((long)tmCa2), "#E8F5E9", "#00A76F"));
        row2.add(sBox("Chuyển Khoản", formatMoney((long)(dtCa - tmCa2)), "#E3F2FD", "#1A73E8"));
        row2.add(sBox("Tổng Doanh Thu", formatMoney((long)dtCa), "#F3E5F5", "#9C27B0"));
        
        JPanel bCanCo = sBox("=> CẦN TRONG KÉT", formatMoney(tienCanCo), "#FFEBEE", "#D32F2F");
        bCanCo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#D32F2F"), 2), new EmptyBorder(10, 5, 10, 5)));
        row2.add(bCanCo);

        p.add(row2, BorderLayout.CENTER); return p;
    }

    private JPanel sBox(String t, String v, String bg, String fg) { 
        JPanel b = new JPanel(new BorderLayout()); 
        b.setBackground(Color.decode(bg)); 
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode(fg), 1), new EmptyBorder(12, 5, 12, 5))); 
        JLabel l1 = new JLabel(t, SwingConstants.CENTER); l1.setFont(new Font("Segoe UI", Font.BOLD, 11)); l1.setForeground(Color.DARK_GRAY); 
        JLabel l2 = new JLabel(v, SwingConstants.CENTER); l2.setFont(new Font("Segoe UI", Font.BOLD, 15)); l2.setForeground(Color.decode(fg)); 
        b.add(l1, BorderLayout.NORTH); b.add(l2, BorderLayout.CENTER); return b; 
    }

    // ── CHART HELPERS ───────────────────────────────────────
    private JPanel barChart(String title, double[] data) {
        JPanel wp = wCard(); wp.setLayout(new BorderLayout(0, 6));
        JLabel lb = new JLabel(title); lb.setFont(new Font("Segoe UI", Font.BOLD, 12)); wp.add(lb, BorderLayout.NORTH);
        double[] fd = data;
        JPanel ch = new JPanel() {
            int hoveredIndex = -1;
            { addMouseMotionListener(new MouseAdapter() { @Override public void mouseMoved(MouseEvent e) { int W = getWidth(), bw = (W - 20) / 12; int newHover = -1; for (int i = 0; i < 12; i++) { int x = 10 + i * bw; if (e.getX() >= x && e.getX() <= x + bw) { newHover = i; break; } } if (hoveredIndex != newHover) { hoveredIndex = newHover; repaint(); } } });
              addMouseListener(new MouseAdapter() { @Override public void mouseExited(MouseEvent e) { hoveredIndex = -1; repaint(); } }); }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int W = getWidth(), H = getHeight() - 20; double mx = 1; for (double d : fd) if (d > mx) mx = d;
                int bw = (W - 20) / 12; String[] ms = {"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
                for (int i = 0; i < 12; i++) {
                    int bh = (int) ((fd[i] / mx) * (H - 25)); int x = 10 + i * bw, y = H - bh - 5;
                    g2.setColor(i == hoveredIndex ? Color.decode("#0052CC") : (fd[i] > 0 ? BLUE : Color.decode("#EEF2F6")));
                    g2.fillRoundRect(x + 2, y, bw - 5, bh, 3, 3); g2.setColor(Color.GRAY); g2.setFont(new Font("Segoe UI", Font.PLAIN, 8)); g2.drawString(ms[i], x + 2, H + 14);
                    if (i == hoveredIndex && fd[i] > 0) { String val = compactMoney(fd[i]); g2.setColor(new Color(0, 0, 0, 200)); g2.fillRoundRect(x - 5, y - 25, 40, 20, 5, 5); g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI", Font.BOLD, 10)); g2.drawString(val, x - 2, y - 11); }
                } g2.dispose();
            }
        }; ch.setBackground(Color.WHITE); wp.add(ch, BorderLayout.CENTER); return wp;
    }

    private JPanel donutChart(String title, int[] vals, String[] names, Color[] colors) {
        JPanel wp = wCard(); wp.setLayout(new BorderLayout(0, 6));
        JLabel lb = new JLabel(title); lb.setFont(new Font("Segoe UI", Font.BOLD, 12)); wp.add(lb, BorderLayout.NORTH);
        JPanel ch = new JPanel() {
            int hoveredIndex = -1;
            { addMouseMotionListener(new MouseAdapter() { @Override public void mouseMoved(MouseEvent e) { int cx = getWidth() / 2 - 40, cy = getHeight() / 2, r = Math.min(cx, cy) - 15; double dx = e.getX() - cx, dy = e.getY() - cy; double dist = Math.sqrt(dx * dx + dy * dy); if (dist >= r / 2.0 && dist <= r) { double angle = Math.toDegrees(Math.atan2(dy, dx)); if (angle < 0) angle += 360; double currentAngle = 0; int tot = 0; for (int v : vals) tot += v; if (tot == 0) tot = 1; int newHover = -1; for (int i = 0; i < vals.length; i++) { double sweep = 360.0 * vals[i] / tot; double start = (currentAngle - 90 < 0) ? currentAngle + 270 : currentAngle - 90; double end = start + sweep; if (end > 360) { if (angle >= start || angle <= end - 360) newHover = i; } else { if (angle >= start && angle <= end) newHover = i; } currentAngle += sweep; } if (hoveredIndex != newHover) { hoveredIndex = newHover; repaint(); } } else { if (hoveredIndex != -1) { hoveredIndex = -1; repaint(); } } } }); }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int tot = 0; for (int v : vals) tot += v; if (tot == 0) tot = 1;
                int cx = getWidth() / 2 - 40, cy = getHeight() / 2, r = Math.min(cx, cy) - 15; double ang = -90;
                for (int i = 0; i < vals.length; i++) { double sw = 360.0 * vals[i] / tot; g2.setColor(i == hoveredIndex ? colors[i].darker() : colors[i]); g2.fillArc(cx - r, cy - r, 2 * r, 2 * r, (int) ang, (int) Math.ceil(sw)); ang += sw; }
                g2.setColor(Color.WHITE); g2.fillOval(cx - r / 2, cy - r / 2, r, r);
                if (hoveredIndex != -1 && vals[hoveredIndex] > 0) { g2.setColor(Color.DARK_GRAY); g2.setFont(new Font("Segoe UI", Font.BOLD, 14)); String v = vals[hoveredIndex] + " SP"; int w = g2.getFontMetrics().stringWidth(v); g2.drawString(v, cx - w/2, cy + 5); }
                int lx = cx + r + 8, ly = cy - vals.length * 10;
                for (int i = 0; i < vals.length; i++) { g2.setColor(colors[i]); g2.fillRect(lx, ly + i * 18, 8, 8); g2.setColor(Color.DARK_GRAY); g2.setFont(new Font("Segoe UI", i == hoveredIndex ? Font.BOLD : Font.PLAIN, 10)); g2.drawString(names[i] + " (" + vals[i] + ")", lx + 12, ly + i * 18 + 8); }
                g2.dispose();
            }
        }; ch.setBackground(Color.WHITE); wp.add(ch, BorderLayout.CENTER); return wp;
    }

    // ── UI HELPERS ──────────────────────────────────────────
    private JPanel wCard() { JPanel p = new JPanel(); p.setBackground(Color.WHITE); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#EEF2F6")), new EmptyBorder(12, 14, 12, 14))); return p; }
    private void addLine(JPanel p, String lbl, String val, Color fg, boolean bold) { JPanel row = new JPanel(new BorderLayout()); row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26)); JLabel l = new JLabel(lbl); l.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 11)); if (val == null) { l.setForeground(fg); row.add(l, BorderLayout.WEST); } else { JLabel v = new JLabel(val, SwingConstants.RIGHT); v.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 11)); v.setForeground(fg); row.add(l, BorderLayout.WEST); row.add(v, BorderLayout.EAST); } p.add(row); p.add(box(3)); }
    private static Component box(int h) { return Box.createRigidArea(new Dimension(0, h)); }
    private String formatMoney(long v) { return new DecimalFormat("###,###,###").format(v) + "đ"; }
    private String compactMoney(double v) { if (v >= 1_000_000_000) return String.format("%.1fTỷ", v / 1_000_000_000); if (v >= 1_000_000) return String.format("%.2fM", v / 1_000_000); if (v >= 1_000) return String.format("%.1fK", v / 1_000); return String.valueOf((long)v); }
}