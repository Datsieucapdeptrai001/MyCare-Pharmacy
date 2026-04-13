package GUI;

import Utils.MenuIcon;
import ConnectDB.ConnectDB;
import Entity.CaLamViec;
import Utils.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;
import java.util.List;

public class ManHinhChinh extends JPanel {

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JButton btnTongQuan, btnDoiChieu;
    private int[] soLuongTien = new int[9];
    private final long[]   MENH_GIA     = {500000,200000,100000,50000,20000,10000,5000,2000,1000};
    private final String[] MENH_GIA_STR = {"500.000đ","200.000đ","100.000đ","50.000đ",
                                            "20.000đ","10.000đ","5.000đ","2.000đ","1.000đ"};

    private static final Color GREEN  = Color.decode("#00A76F");
    private static final Color BLUE   = Color.decode("#1A73E8");
    private static final Color RED    = Color.decode("#FF5630");
    private static final Color ORANGE = Color.decode("#FFAB00");
    private static final Color BG     = Color.decode("#F4F6F8");

    public ManHinhChinh() {
        setLayout(new BorderLayout());
        setBackground(BG);
        add(createTopHeader(), BorderLayout.NORTH);
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(BG);
        cardPanel.add(createTongQuanPanel(), "TongQuan");
        cardPanel.add(createDoiChieuPanel(), "DoiChieu");
        add(cardPanel, BorderLayout.CENTER);
    }

    // ── HEADER ──────────────────────────────────────────────
    private JPanel createTopHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(Color.WHITE);
        hdr.setBorder(BorderFactory.createMatteBorder(0,0,1,0, Color.decode("#DFE3E8")));

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabs.setBackground(Color.WHITE);
        btnTongQuan = makeTabBtn("Tổng quan", true, "TAB_CHART");
        btnDoiChieu = makeTabBtn("Đối chiếu doanh thu", false, "TAB_DOLLAR");
        btnTongQuan.addActionListener(e -> { setActiveTab(btnTongQuan,btnDoiChieu); cardLayout.show(cardPanel,"TongQuan"); });
        btnDoiChieu.addActionListener(e -> { setActiveTab(btnDoiChieu,btnTongQuan); cardLayout.show(cardPanel,"DoiChieu"); });
        tabs.add(btnTongQuan); tabs.add(btnDoiChieu);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        right.setBackground(Color.WHITE);

        String ten = UserSession.getInstance().getTenHienThi();
        JLabel lblOn = new JLabel("● Ca đang mở — " + ten);
        lblOn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblOn.setForeground(GREEN);
        lblOn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GREEN,1), new EmptyBorder(4,10,4,10)));

        JButton btnKet = new JButton("⏹ Kết ca");
        btnKet.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnKet.setForeground(RED);
        btnKet.setBackground(Color.WHITE);
        btnKet.setFocusPainted(false);
        btnKet.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(RED,1), new EmptyBorder(4,10,4,10)));
        btnKet.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnKet.addActionListener(e -> openKetCaDialog());

        if (UserSession.getInstance().isAdmin()) {
            lblOn.setVisible(false); btnKet.setVisible(false);
        }
        right.add(lblOn); right.add(btnKet);
        hdr.add(tabs, BorderLayout.WEST);
        hdr.add(right, BorderLayout.EAST);
        return hdr;
    }

    private JButton makeTabBtn(String text, boolean active, String icon) {
        JButton b = new JButton(text);
        b.setIcon(new MenuIcon(icon)); b.setIconTextGap(8);
        b.setPreferredSize(new Dimension(200,44));
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false); b.setContentAreaFilled(false); b.setOpaque(true);
        b.setBackground(Color.WHITE);
        b.setBorder(BorderFactory.createMatteBorder(0,0,active?3:0,0,BLUE));
        b.setForeground(active?BLUE:Color.GRAY);
        return b;
    }

    private void setActiveTab(JButton a, JButton b) {
        a.setForeground(BLUE); a.setBorder(BorderFactory.createMatteBorder(0,0,3,0,BLUE));
        b.setForeground(Color.GRAY); b.setBorder(BorderFactory.createEmptyBorder());
    }

    // ── TỔNG QUAN ───────────────────────────────────────────
    private JPanel createTongQuanPanel() {
        JPanel root = new JPanel(new BorderLayout(0,14));
        root.setBackground(BG); root.setBorder(new EmptyBorder(14,20,14,20));
        JLabel title = new JLabel("<html><b style='font-size:15px;'>MÀN HÌNH CHÍNH</b>" +
            "<br><span style='color:gray;font-size:11px;'>Tổng quan hoạt động — Hôm nay</span></html>");
        root.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG);

        // Banner tiền đầu ca (STAFF)
        if (!UserSession.getInstance().isAdmin() && UserSession.getInstance().daMoCa()) {
            body.add(buildTienDauCaBanner()); body.add(box(12));
        }

        body.add(buildKPIRow());   body.add(box(14));
        body.add(buildChartRow()); body.add(box(14));
        body.add(buildBottomRow()); body.add(box(14));
        body.add(buildExpiringPanel());

        JScrollPane sc = new JScrollPane(body);
        sc.setBorder(null); sc.getVerticalScrollBar().setUnitIncrement(14);
        root.add(sc, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildTienDauCaBanner() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.decode("#E6F7F2"));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#B2DFDB")), new EmptyBorder(10,16,10,16)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));

        CaLamViec ca = UserSession.getInstance().getCaHienTai();
        long tienDau = UserSession.getInstance().getTienDauCa();
        String[] caLabels = {"Ca Sáng","Ca Chiều","Ca Tối"};
        String caLabel = caLabels[UserSession.getInstance().getLoaiCa()];
        String thoiGian = ca != null
            ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Timestamp.valueOf(ca.getThoiGianBatDau()))
            : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());

        JPanel left = new JPanel(); left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel l1 = new JLabel("TIỀN ĐẦU CA  " + thoiGian + "  ·  "
            + UserSession.getInstance().getTenHienThi() + " (" + UserSession.getInstance().getChucVuHienThi() + ")");
        l1.setFont(new Font("Segoe UI", Font.BOLD, 11)); l1.setForeground(Color.decode("#006250"));
        JLabel l2 = new JLabel(formatMoney(tienDau) + " × 1   (" + caLabel + ")");
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 11)); l2.setForeground(Color.decode("#637381"));
        left.add(l1); left.add(box(3)); left.add(l2);

        JPanel right = new JPanel(); right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        JLabel lTl = new JLabel("Tổng tiền đầu ca", SwingConstants.RIGHT);
        lTl.setFont(new Font("Segoe UI",Font.PLAIN,10)); lTl.setForeground(Color.GRAY); lTl.setAlignmentX(RIGHT_ALIGNMENT);
        JLabel lAm = new JLabel(formatMoney(tienDau), SwingConstants.RIGHT);
        lAm.setFont(new Font("Segoe UI",Font.BOLD,20)); lAm.setForeground(Color.decode("#006250")); lAm.setAlignmentX(RIGHT_ALIGNMENT);
        JLabel lH  = new JLabel("✓ Tiền mặt kiểm đếm khi vào ca", SwingConstants.RIGHT);
        lH.setFont(new Font("Segoe UI",Font.ITALIC,9)); lH.setForeground(Color.GRAY); lH.setAlignmentX(RIGHT_ALIGNMENT);
        right.add(lTl); right.add(lAm); right.add(lH);

        p.add(left, BorderLayout.WEST); p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel buildKPIRow() {
        JPanel row = new JPanel(new GridLayout(1,4,12,0));
        row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

        int    sp   = qi("SELECT COUNT(*) FROM SanPham");
        int    hd   = qi("SELECT COUNT(*) FROM HoaDon WHERE CONVERT(DATE,ngayLapHD)=CONVERT(DATE,GETDATE()) AND loaiHD='BAN_HANG'");
        double dt   = qd("SELECT ISNULL(SUM(ct.soLuong*dvl.gia),0) FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId WHERE CONVERT(DATE,hd.ngayLapHD)=CONVERT(DATE,GETDATE()) AND hd.loaiHD='BAN_HANG'");
        int    kh   = qi("SELECT COUNT(*) FROM KhachHang");

        row.add(kpiCard("Tổng sản phẩm",    String.valueOf(sp),   "+3 tháng này",    "#EEF2FF","#3D52A0","💊"));
        row.add(kpiCard("Hóa đơn hôm nay",  String.valueOf(hd),   "+5 so hôm qua",   "#ECFDF5","#00A76F","🧾"));
        row.add(kpiCard("Doanh thu hôm nay", compactMoney(dt),     "đ VND",           "#FFF7ED","#FF6B00","💰"));
        row.add(kpiCard("Tổng khách hàng",   String.valueOf(kh),   "+12 tháng này",   "#FFF0F0","#FF5630","👥"));
        return row;
    }

    private JPanel kpiCard(String ttl, String val, String sub, String bg, String fg, String icon) {
        JPanel p = new JPanel(new BorderLayout(6,4));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#EEF2F6")), new EmptyBorder(12,14,12,14)));
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        JLabel lT = new JLabel(ttl); lT.setFont(new Font("Segoe UI",Font.PLAIN,11)); lT.setForeground(Color.GRAY);
        JLabel ico = new JLabel(icon, SwingConstants.CENTER);
        ico.setOpaque(true); ico.setBackground(Color.decode(bg));
        ico.setFont(new Font("Segoe UI Emoji",Font.PLAIN,15)); ico.setPreferredSize(new Dimension(36,36));
        top.add(lT, BorderLayout.CENTER); top.add(ico, BorderLayout.EAST);
        JLabel lV = new JLabel(val); lV.setFont(new Font("Segoe UI",Font.BOLD,22)); lV.setForeground(Color.decode(fg));
        JLabel lS = new JLabel(sub); lS.setFont(new Font("Segoe UI",Font.PLAIN,10)); lS.setForeground(Color.GRAY);
        p.add(top, BorderLayout.NORTH); p.add(lV, BorderLayout.CENTER); p.add(lS, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildChartRow() {
        JPanel row = new JPanel(new GridLayout(1,2,12,0));
        row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));

        double[] monthly = new double[12];
        try (Statement st = con().createStatement();
             ResultSet rs = st.executeQuery("SELECT MONTH(hd.ngayLapHD) m, ISNULL(SUM(ct.soLuong*dvl.gia),0) dt FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId WHERE YEAR(hd.ngayLapHD)=YEAR(GETDATE()) AND hd.loaiHD='BAN_HANG' GROUP BY MONTH(hd.ngayLapHD)")) {
            while (rs.next()) { int m=rs.getInt("m"); if(m>=1&&m<=12) monthly[m-1]=rs.getDouble("dt"); }
        } catch (Exception ignored) {}

        int[] cats = new int[4];
        String[] catDB = {"THUOC_KE_DON","THUOC_KHONG_KE_DON","THUC_PHAM_CHUC_NANG","MY_PHAM"};
        for (int i=0;i<4;i++) cats[i] = qi("SELECT COUNT(*) FROM SanPham WHERE danhMuc='"+catDB[i]+"'");

        row.add(barChart("Doanh thu theo tháng (" + java.time.Year.now().getValue() + ")", monthly));
        row.add(donutChart("Phân loại sản phẩm", cats,
            new String[]{"Thuốc kê đơn","Thuốc không kê đơn","TPCN","Mỹ phẩm"},
            new Color[]{RED, BLUE, GREEN, ORANGE}));
        return row;
    }

    private JPanel buildBottomRow() {
        JPanel row = new JPanel(new GridLayout(1,2,12,0));
        row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        // Hóa đơn trong ca
        JPanel inv = wCard();
        inv.setLayout(new BorderLayout(0,8));
        JLabel lbI = new JLabel("Hóa đơn gần đây");
        lbI.setFont(new Font("Segoe UI",Font.BOLD,12));
        inv.add(lbI, BorderLayout.NORTH);

        DefaultTableModel mInv = new DefaultTableModel(new String[]{"Mã HĐ","Khách hàng","Tiền","TT"},0){
            public boolean isCellEditable(int r,int c){return false;}};
        JTable tInv = new JTable(mInv); tInv.setFont(new Font("Segoe UI",Font.PLAIN,11));
        tInv.setRowHeight(26); tInv.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,11));

        String maNV = UserSession.getInstance().getMaNhanVien();
        String nvCond = maNV.isEmpty() ? "" : " AND hd.nhanVienId='"+maNV+"'";
        try (Statement st = con().createStatement();
             ResultSet rs = st.executeQuery("SELECT TOP 6 hd.id, ISNULL(kh.hoVaTen,'Khách lẻ') kh, ISNULL(SUM(ct.soLuong*dvl.gia),0) tong FROM HoaDon hd LEFT JOIN KhachHang kh ON hd.khachHangId=kh.id LEFT JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId LEFT JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId WHERE hd.loaiHD='BAN_HANG' AND CONVERT(DATE,hd.ngayLapHD)=CONVERT(DATE,GETDATE())"+nvCond+" GROUP BY hd.id,kh.hoVaTen ORDER BY hd.id DESC")) {
            while(rs.next()) mInv.addRow(new Object[]{rs.getString("id"),rs.getString("kh"),formatMoney((long)rs.getDouble("tong")),"✓"});
        } catch (Exception ignored) {}
        if (mInv.getRowCount()==0) mInv.addRow(new Object[]{"—","Chưa có hóa đơn nào trong ca này","—","—"});
        inv.add(new JScrollPane(tInv), BorderLayout.CENTER);
        row.add(inv);

        // Sản phẩm gần hết hàng
        JPanel stk = wCard(); stk.setLayout(new BorderLayout(0,8));
        JLabel lbS = new JLabel("⚠ Sản phẩm sắp hết hàng"); lbS.setFont(new Font("Segoe UI",Font.BOLD,12)); lbS.setForeground(ORANGE);
        stk.add(lbS, BorderLayout.NORTH);
        JPanel listS = new JPanel(); listS.setLayout(new BoxLayout(listS,BoxLayout.Y_AXIS)); listS.setBackground(Color.WHITE);

        List<Object[]> ls = new ArrayList<>();
        try (Statement st = con().createStatement();
             ResultSet rs = st.executeQuery("SELECT TOP 4 sp.ten, ISNULL(SUM(lh.soLuongLoHang),0) ton FROM SanPham sp LEFT JOIN LoHang lh ON sp.id=lh.sanPhamId GROUP BY sp.ten ORDER BY ton ASC")) {
            while(rs.next()) ls.add(new Object[]{rs.getString("ten"), rs.getInt("ton"), 50});
        } catch (Exception ignored) {}

        for (Object[] it : ls) {
            String nm=(String)it[0]; int tn=(int)it[1];
            JPanel r2=new JPanel(new BorderLayout(0,2)); r2.setOpaque(false); r2.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
            JPanel t2=new JPanel(new BorderLayout()); t2.setOpaque(false);
            JLabel ln=new JLabel(nm.length()>32?nm.substring(0,32)+"…":nm); ln.setFont(new Font("Segoe UI",Font.PLAIN,11));
            JLabel lv=new JLabel(tn+" Hộp",SwingConstants.RIGHT); lv.setFont(new Font("Segoe UI",Font.BOLD,11)); lv.setForeground(RED);
            t2.add(ln, BorderLayout.WEST); t2.add(lv, BorderLayout.EAST);
            JProgressBar pb=new JProgressBar(0,50); pb.setValue(Math.min(tn,50));
            pb.setBackground(Color.decode("#FFF0E0")); pb.setForeground(ORANGE); pb.setPreferredSize(new Dimension(0,5));
            r2.add(t2, BorderLayout.NORTH); r2.add(pb, BorderLayout.SOUTH);
            listS.add(r2); listS.add(box(5));
        }
        int duTon = qi("SELECT COUNT(DISTINCT sp.id) FROM SanPham sp JOIN LoHang lh ON sp.id=lh.sanPhamId");
        JLabel lSum = new JLabel("<html><span style='color:#00A76F;'>✓ "+duTon+" sản phẩm đủ tồn</span>  <span style='color:#FF5630;'>⏰ "+ls.size()+" cần nhập thêm</span></html>");
        lSum.setFont(new Font("Segoe UI",Font.PLAIN,10)); listS.add(lSum);
        stk.add(new JScrollPane(listS){{ setBorder(null); }}, BorderLayout.CENTER);
        row.add(stk);
        return row;
    }

    private JPanel buildExpiringPanel() {
        JPanel p = wCard(); p.setLayout(new BorderLayout(0,8));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 270));

        int cnt = qi("SELECT COUNT(*) FROM LoHang WHERE ngayHetHan IS NOT NULL AND DATEDIFF(DAY,GETDATE(),ngayHetHan)<=90");
        JPanel h2 = new JPanel(new BorderLayout()); h2.setOpaque(false);
        JLabel lbT = new JLabel("⚠ Lô hàng sắp hết hạn (trong 90 ngày)"); lbT.setFont(new Font("Segoe UI",Font.BOLD,12)); lbT.setForeground(ORANGE);
        JLabel badge = new JLabel(" "+cnt+" lô ", SwingConstants.CENTER);
        badge.setOpaque(true); badge.setBackground(ORANGE); badge.setForeground(Color.WHITE); badge.setFont(new Font("Segoe UI",Font.BOLD,10));
        h2.add(lbT, BorderLayout.WEST); h2.add(badge, BorderLayout.EAST);
        p.add(h2, BorderLayout.NORTH);

        DefaultTableModel m = new DefaultTableModel(new String[]{"Sản phẩm","Mã lô","Tồn","HSD","Còn lại"},0){
            public boolean isCellEditable(int r,int c){return false;}};
        JTable t = new JTable(m); t.setFont(new Font("Segoe UI",Font.PLAIN,11)); t.setRowHeight(26);
        t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,11));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try (Statement st = con().createStatement();
             ResultSet rs = st.executeQuery("SELECT TOP 8 sp.ten, lh.soLoHang, lh.soLuongLoHang, lh.ngayHetHan, DATEDIFF(DAY,GETDATE(),lh.ngayHetHan) cl FROM LoHang lh JOIN SanPham sp ON lh.sanPhamId=sp.id WHERE lh.ngayHetHan IS NOT NULL AND DATEDIFF(DAY,GETDATE(),lh.ngayHetHan)<=90 ORDER BY lh.ngayHetHan ASC")) {
            while(rs.next()) {
                int cl = rs.getInt("cl");
                m.addRow(new Object[]{rs.getString("ten"), rs.getString("soLoHang"),
                    String.format("%,d",rs.getInt("soLuongLoHang")),
                    sdf.format(rs.getTimestamp("ngayHetHan")),
                    cl<=0?"Đã hết hạn":cl+" ngày"});
            }
        } catch (Exception ignored) {}

        t.getColumnModel().getColumn(4).setCellRenderer((tbl2,val,sel,foc,r,c) -> {
            JLabel l = new JLabel(String.valueOf(val), SwingConstants.CENTER);
            l.setOpaque(true); l.setFont(new Font("Segoe UI",Font.BOLD,10));
            String v = String.valueOf(val);
            if (v.equals("Đã hết hạn")) { l.setBackground(Color.decode("#FFEBEE")); l.setForeground(RED); }
            else if (v.contains("ngày") && Integer.parseInt(v.replace(" ngày",""))<=30)
                { l.setBackground(Color.decode("#FFF3E0")); l.setForeground(ORANGE); }
            else { l.setBackground(Color.WHITE); l.setForeground(Color.DARK_GRAY); }
            return l;
        });
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        return p;
    }

    // ── ĐỐI CHIẾU ───────────────────────────────────────────
    private JPanel createDoiChieuPanel() {
        JPanel root = new JPanel(new BorderLayout(0,14));
        root.setBackground(BG); root.setBorder(new EmptyBorder(14,20,14,20));
        JLabel title = new JLabel("<html><b style='font-size:15px;'>ĐỐI CHIẾU DOANH THU</b>" +
            "<br><span style='color:gray;font-size:11px;'>Sổ sách — Tiền mặt — Chuyển khoản</span></html>");

        // Nút xem giới hạn quyền
        JButton btnGH = new JButton("◉ Xem có giới hạn theo quyền");
        btnGH.setFont(new Font("Segoe UI",Font.PLAIN,11));
        btnGH.setForeground(Color.decode("#637381")); btnGH.setBackground(Color.WHITE);
        btnGH.setFocusPainted(false);
        btnGH.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8")), new EmptyBorder(4,10,4,10)));

        JPanel topRow = new JPanel(new BorderLayout()); topRow.setOpaque(false);
        topRow.add(title, BorderLayout.WEST); topRow.add(btnGH, BorderLayout.EAST);
        root.add(topRow, BorderLayout.NORTH);

        String maNV = UserSession.getInstance().getMaNhanVien();
        String cond  = maNV.isEmpty() ? "" : " AND hd.nhanVienId='"+maNV+"'";
        boolean isAdmin = UserSession.getInstance().isAdmin();

        int    soHD   = qi("SELECT COUNT(*) FROM HoaDon hd WHERE CONVERT(DATE,ngayLapHD)=CONVERT(DATE,GETDATE()) AND loaiHD='BAN_HANG'"+cond);
        double tHang  = qd("SELECT ISNULL(SUM(ct.soLuong*dvl.gia),0) FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId WHERE CONVERT(DATE,hd.ngayLapHD)=CONVERT(DATE,GETDATE()) AND hd.loaiHD='BAN_HANG'"+cond);
        double tMat   = qd("SELECT ISNULL(SUM(ct.soLuong*dvl.gia),0) FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId WHERE hd.phuongThucThanhToan='TIEN_MAT' AND CONVERT(DATE,hd.ngayLapHD)=CONVERT(DATE,GETDATE()) AND hd.loaiHD='BAN_HANG'"+cond);
        double ck     = tHang - tMat;
        long   tDau   = UserSession.getInstance().getTienDauCa();

        JPanel body = new JPanel(new GridLayout(1,3,12,0)); body.setBackground(BG);

        // Card HÔM NAY
        JPanel c1 = wCard(); c1.setLayout(new BoxLayout(c1,BoxLayout.Y_AXIS));
        addLine(c1,"📊 HÔM NAY", null, BLUE, true);
        addLine(c1,"Số hóa đơn:", String.valueOf(soHD), Color.BLACK, false);
        addLine(c1,"Tiền hàng:", formatMoney((long)tHang), Color.BLACK, false);
        addLine(c1,"Khuyến mãi:", "-0đ", GREEN, false);
        addLine(c1,"VAT:", "+0đ", RED, false);
        c1.add(new JSeparator(){{ setMaximumSize(new Dimension(Integer.MAX_VALUE,1)); }});
        addLine(c1,"Tổng thanh toán:", formatMoney((long)tHang), BLUE, true);
        c1.add(box(8));
        addLine(c1,"ĐỐI CHIẾU THU CHI", null, Color.GRAY, false);
        addLine(c1,"⇄ Tiền mặt thực thu", formatMoney((long)tMat), Color.BLACK, false);
        addLine(c1,"↔ Chuyển khoản", formatMoney((long)ck), Color.BLACK, false);
        c1.add(new JSeparator(){{ setMaximumSize(new Dimension(Integer.MAX_VALUE,1)); }});
        addLine(c1,"Tổng thực thu", formatMoney((long)tHang), BLUE, true);
        body.add(c1);

        // Card 7 NGÀY QUA
        JPanel c2 = wCard(); c2.setLayout(new BoxLayout(c2,BoxLayout.Y_AXIS));
        addLine(c2,"📊 7 NGÀY QUA", null, BLUE, true);
        if (!isAdmin) {
            JLabel lk = new JLabel("<html><center>🔒<br>Cần quyền view_revenue<br>Chức năng này chỉ dành cho<br>Dược sĩ trưởng / Quản lý</center></html>", SwingConstants.CENTER);
            lk.setFont(new Font("Segoe UI",Font.PLAIN,11)); lk.setForeground(Color.GRAY); lk.setAlignmentX(CENTER_ALIGNMENT);
            c2.add(Box.createVerticalGlue()); c2.add(lk); c2.add(Box.createVerticalGlue());
        } else {
            double dt7 = qd("SELECT ISNULL(SUM(ct.soLuong*dvl.gia),0) FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId WHERE hd.ngayLapHD>=DATEADD(DAY,-7,GETDATE()) AND hd.loaiHD='BAN_HANG'");
            int hd7 = qi("SELECT COUNT(*) FROM HoaDon WHERE ngayLapHD>=DATEADD(DAY,-7,GETDATE()) AND loaiHD='BAN_HANG'");
            addLine(c2,"Số hóa đơn:", String.valueOf(hd7), Color.BLACK, false);
            addLine(c2,"Doanh thu:", formatMoney((long)dt7), GREEN, true);
        }
        body.add(c2);

        // Card ĐỔI TRẢ
        JPanel c3 = wCard(); c3.setLayout(new BoxLayout(c3,BoxLayout.Y_AXIS));
        addLine(c3,"↩ ĐỔI / TRẢ HÀNG", null, RED, true);
        int tongPhieu = qi("SELECT COUNT(*) FROM HoaDon WHERE loaiHD='DOI_TRA'");
        int choXuLy   = qi("SELECT COUNT(*) FROM HoaDon WHERE loaiHD='DOI_TRA' AND ghiChu IS NULL");
        addLine(c3,"Tổng phiếu:", String.valueOf(tongPhieu), Color.BLACK, false);
        addLine(c3,"Chờ xử lý:", String.valueOf(choXuLy), RED, false);
        body.add(c3);

        root.add(body, BorderLayout.CENTER);
        return root;
    }

    // ── KẾT CA DIALOG ───────────────────────────────────────
    private void openKetCaDialog() {
        soLuongTien = new int[9];
        Window pw = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog((Frame)pw, "Kết ca — Báo cáo & Kiểm kê", true);
        dlg.setSize(840, 660); dlg.setLocationRelativeTo(pw); dlg.setLayout(new BorderLayout());

        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hdr.setBackground(Color.decode("#E02327")); hdr.setBorder(new EmptyBorder(8,12,8,12));
        JLabel hT = new JLabel("⏹  Kết ca — Báo cáo & Kiểm kê");
        hT.setFont(new Font("Segoe UI",Font.BOLD,16)); hT.setForeground(Color.WHITE);
        hdr.add(hT); dlg.add(hdr, BorderLayout.NORTH);

        JPanel body = new JPanel(); body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE); body.setBorder(new EmptyBorder(18,18,18,18));

        // Tổng kết doanh thu
        body.add(buildTongKetInDialog()); body.add(box(14));

        JLabel lKD = new JLabel("💵  KIỂM ĐẾM TIỀN MẶT THỰC TẾ CUỐI CA");
        lKD.setFont(new Font("Segoe UI",Font.BOLD,12)); lKD.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lHint = new JLabel("  Click chuột TRÁI tăng tờ  ·  Click chuột PHẢI giảm tờ");
        lHint.setFont(new Font("Segoe UI",Font.ITALIC,10)); lHint.setForeground(Color.GRAY); lHint.setAlignmentX(LEFT_ALIGNMENT);
        body.add(lKD); body.add(box(4)); body.add(lHint); body.add(box(6));

        JLabel lTong = new JLabel("Tổng cộng: 0đ", SwingConstants.RIGHT);
        lTong.setFont(new Font("Segoe UI",Font.BOLD,14)); lTong.setForeground(Color.WHITE);
        lTong.setOpaque(true); lTong.setBackground(Color.decode("#152A4B"));
        lTong.setBorder(new EmptyBorder(6,12,6,12)); lTong.setAlignmentX(LEFT_ALIGNMENT);
        lTong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        Color[] bc = {BLUE,Color.decode("#9C27B0"),Color.decode("#4CAF50"),
            Color.decode("#FF9800"),RED,Color.decode("#E91E63"),
            Color.decode("#FF7043"),BLUE,Color.decode("#607D8B")};

        JPanel grid = new JPanel(new GridLayout(3,3,10,8));
        grid.setBackground(Color.WHITE); grid.setAlignmentX(LEFT_ALIGNMENT);
        JLabel[] cnts = new JLabel[9];
        for (int i=0;i<9;i++) {
            final int idx=i;
            JPanel box2 = new JPanel(new BorderLayout(0,2)){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                    g2.setColor(bc[idx]); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                    g2.dispose();
                }
            };
            box2.setOpaque(false); box2.setBorder(new EmptyBorder(7,5,7,5));
            box2.setCursor(new Cursor(Cursor.HAND_CURSOR));
            JLabel lG = new JLabel(MENH_GIA_STR[i],SwingConstants.CENTER);
            lG.setFont(new Font("Segoe UI",Font.BOLD,11)); lG.setForeground(bc[i]);
            cnts[i] = new JLabel("0",SwingConstants.CENTER);
            cnts[i].setFont(new Font("Segoe UI",Font.BOLD,17));
            box2.add(lG, BorderLayout.NORTH); box2.add(cnts[i], BorderLayout.CENTER);
            box2.addMouseListener(new MouseAdapter(){
                @Override public void mouseClicked(MouseEvent e){
                    if(SwingUtilities.isLeftMouseButton(e)) soLuongTien[idx]++;
                    else if(SwingUtilities.isRightMouseButton(e)&&soLuongTien[idx]>0) soLuongTien[idx]--;
                    cnts[idx].setText(String.valueOf(soLuongTien[idx]));
                    long t=0; for(int j=0;j<9;j++) t+=soLuongTien[j]*MENH_GIA[j];
                    lTong.setText("Tổng cộng: "+formatMoney(t));
                }
            });
            grid.add(box2);
        }
        body.add(grid); body.add(box(8)); body.add(lTong); body.add(box(10));

        JLabel lGC = new JLabel("Ghi chú kết ca:"); lGC.setFont(new Font("Segoe UI",Font.BOLD,11)); lGC.setAlignmentX(LEFT_ALIGNMENT);
        body.add(lGC);
        JTextArea ta = new JTextArea(3,20); ta.setLineWrap(true); ta.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JScrollPane sGC = new JScrollPane(ta); sGC.setAlignmentX(LEFT_ALIGNMENT); body.add(sGC);

        JScrollPane scroll = new JScrollPane(body); scroll.getVerticalScrollBar().setUnitIncrement(14);
        dlg.add(scroll, BorderLayout.CENTER);

        // Footer
        JPanel ft = new JPanel(new BorderLayout()); ft.setBackground(Color.WHITE); ft.setBorder(new EmptyBorder(10,18,10,18));
        JButton bHuy = new JButton("Hủy"); bHuy.addActionListener(e->dlg.dispose());
        JButton bXN  = new JButton("✓  Xác nhận kết ca");
        bXN.setBackground(Color.decode("#E02327")); bXN.setForeground(Color.WHITE); bXN.setFocusPainted(false);
        bXN.addActionListener(e->{
            long tongKC=0; for(int j=0;j<9;j++) tongKC+=soLuongTien[j]*MENH_GIA[j];
            double dtCa=0;
            String maNV = UserSession.getInstance().getMaNhanVien();
            CaLamViec ca = UserSession.getInstance().getCaHienTai();
            if (ca!=null && !maNV.isEmpty()){
                dtCa=qd("SELECT ISNULL(SUM(ct.soLuong*dvl.gia),0) FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId WHERE hd.nhanVienId='"+maNV+"' AND hd.ngayLapHD>='"+ca.getThoiGianBatDau()+"' AND hd.loaiHD='BAN_HANG'");
                ca.setThoiGianKetThuc(LocalDateTime.now());
                ca.setTienHeThongGhiNhan(dtCa);
                ca.setTienKetCa(tongKC);
                new DAO.DAO_CaLamViec().capNhatCa(ca);
                UserSession.getInstance().setCaHienTai(null);
            }
            dlg.dispose();
            JOptionPane.showMessageDialog(this,
                "Kết ca thành công!\nDoanh thu ca: "+formatMoney((long)dtCa)+"\nTiền mặt kiểm kê: "+formatMoney(tongKC),
                "Kết ca", JOptionPane.INFORMATION_MESSAGE);
        });
        JPanel rFt=new JPanel(new FlowLayout(FlowLayout.RIGHT)); rFt.setBackground(Color.WHITE); rFt.add(bXN);
        ft.add(bHuy,BorderLayout.WEST); ft.add(rFt,BorderLayout.EAST);
        dlg.add(ft, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private JPanel buildTongKetInDialog() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE); p.setAlignmentX(LEFT_ALIGNMENT);
        JLabel t = new JLabel("$ TỔNG KẾT DOANH THU TRONG CA");
        t.setFont(new Font("Segoe UI",Font.BOLD,12)); t.setForeground(Color.decode("#E02327")); t.setAlignmentX(LEFT_ALIGNMENT);
        p.add(t); p.add(box(8));

        double dtCa=0; int soHD2=0; double tmCa2=0;
        String maNV = UserSession.getInstance().getMaNhanVien();
        CaLamViec ca = UserSession.getInstance().getCaHienTai();
        if (ca!=null && !maNV.isEmpty()){
            soHD2=qi("SELECT COUNT(*) FROM HoaDon WHERE nhanVienId='"+maNV+"' AND ngayLapHD>='"+ca.getThoiGianBatDau()+"' AND loaiHD='BAN_HANG'");
            dtCa=qd("SELECT ISNULL(SUM(ct.soLuong*dvl.gia),0) FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId WHERE hd.nhanVienId='"+maNV+"' AND hd.ngayLapHD>='"+ca.getThoiGianBatDau()+"' AND hd.loaiHD='BAN_HANG'");
            tmCa2=qd("SELECT ISNULL(SUM(ct.soLuong*dvl.gia),0) FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId WHERE hd.nhanVienId='"+maNV+"' AND hd.phuongThucThanhToan='TIEN_MAT' AND hd.ngayLapHD>='"+ca.getThoiGianBatDau()+"' AND hd.loaiHD='BAN_HANG'");
        }
        JPanel row2=new JPanel(new GridLayout(1,4,10,0)); row2.setBackground(Color.WHITE); row2.setAlignmentX(LEFT_ALIGNMENT);
        row2.add(sBox("Số hóa đơn", soHD2+" HĐ","#E3F2FD","#1A73E8"));
        row2.add(sBox("Tổng doanh thu",formatMoney((long)dtCa),"#E8F5E9","#00A76F"));
        row2.add(sBox("Tiền mặt",formatMoney((long)tmCa2),"#E8F5E9","#00A76F"));
        row2.add(sBox("Chuyển khoản",formatMoney((long)(dtCa-tmCa2)),"#E3F2FD","#1A73E8"));
        p.add(row2);
        return p;
    }

    private JPanel sBox(String t, String v, String bg, String fg){
        JPanel b=new JPanel(new GridLayout(2,1));
        b.setBackground(Color.decode(bg));
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode(fg)),new EmptyBorder(8,0,8,0)));
        JLabel l1=new JLabel(t,SwingConstants.CENTER); l1.setForeground(Color.GRAY);
        JLabel l2=new JLabel(v,SwingConstants.CENTER); l2.setFont(new Font("Segoe UI",Font.BOLD,14)); l2.setForeground(Color.decode(fg));
        b.add(l1); b.add(l2); return b;
    }

    // ── CHART HELPERS ───────────────────────────────────────
    private JPanel barChart(String title, double[] data) {
        JPanel wp = wCard(); wp.setLayout(new BorderLayout(0,6));
        JLabel lb = new JLabel(title); lb.setFont(new Font("Segoe UI",Font.BOLD,12));
        wp.add(lb, BorderLayout.NORTH);
        double[] fd = data;
        JPanel ch = new JPanel(){
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int W=getWidth(),H=getHeight()-20;
                double mx=1; for(double d:fd) if(d>mx) mx=d;
                int bw=(W-20)/12;
                String[] ms={"T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"};
                for(int i=0;i<12;i++){
                    int bh=(int)((fd[i]/mx)*(H-25)); int x=10+i*bw; int y=H-bh-5;
                    g2.setColor(fd[i]>0?BLUE:Color.decode("#EEF2F6"));
                    g2.fillRoundRect(x+2,y,bw-5,bh,3,3);
                    g2.setColor(Color.GRAY); g2.setFont(new Font("Segoe UI",Font.PLAIN,8));
                    g2.drawString(ms[i],x+2,H+14);
                }
                g2.dispose();
            }
        };
        ch.setBackground(Color.WHITE); wp.add(ch, BorderLayout.CENTER); return wp;
    }

    private JPanel donutChart(String title, int[] vals, String[] names, Color[] colors) {
        JPanel wp = wCard(); wp.setLayout(new BorderLayout(0,6));
        JLabel lb = new JLabel(title); lb.setFont(new Font("Segoe UI",Font.BOLD,12));
        wp.add(lb, BorderLayout.NORTH);
        JPanel ch = new JPanel(){
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int tot=0; for(int v:vals) tot+=v; if(tot==0) tot=1;
                int cx=getWidth()/2-40, cy=getHeight()/2, r=Math.min(cx,cy)-15;
                double ang=-90;
                for(int i=0;i<vals.length;i++){
                    double sw=360.0*vals[i]/tot; g2.setColor(colors[i]);
                    g2.fillArc(cx-r,cy-r,2*r,2*r,(int)ang,(int)sw); ang+=sw;
                }
                g2.setColor(Color.WHITE); g2.fillOval(cx-r/2,cy-r/2,r,r);
                int lx=cx+r+8, ly=cy-vals.length*10;
                for(int i=0;i<vals.length;i++){
                    g2.setColor(colors[i]); g2.fillRect(lx,ly+i*18,8,8);
                    g2.setColor(Color.DARK_GRAY); g2.setFont(new Font("Segoe UI",Font.PLAIN,8));
                    g2.drawString(names[i],lx+12,ly+i*18+8);
                }
                g2.dispose();
            }
        };
        ch.setBackground(Color.WHITE); wp.add(ch, BorderLayout.CENTER); return wp;
    }

    // ── UI HELPERS ──────────────────────────────────────────
    private JPanel wCard() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#EEF2F6")), new EmptyBorder(12,14,12,14)));
        return p;
    }

    private void addLine(JPanel p, String lbl, String val, Color fg, boolean bold) {
        JPanel row = new JPanel(new BorderLayout()); row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        JLabel l = new JLabel(lbl); l.setFont(new Font("Segoe UI",bold?Font.BOLD:Font.PLAIN,11));
        if (val==null){ l.setForeground(fg); row.add(l, BorderLayout.WEST); }
        else {
            JLabel v=new JLabel(val,SwingConstants.RIGHT);
            v.setFont(new Font("Segoe UI",bold?Font.BOLD:Font.PLAIN,11)); v.setForeground(fg);
            row.add(l, BorderLayout.WEST); row.add(v, BorderLayout.EAST);
        }
        p.add(row); p.add(box(3));
    }

    private static Component box(int h) { return Box.createRigidArea(new Dimension(0,h)); }

    // ── DB HELPERS ──────────────────────────────────────────
    private int    qi(String sql) { try(Statement st=con().createStatement();ResultSet rs=st.executeQuery(sql)){if(rs.next())return rs.getInt(1);}catch(Exception e){} return 0; }
    private double qd(String sql) { try(Statement st=con().createStatement();ResultSet rs=st.executeQuery(sql)){if(rs.next())return rs.getDouble(1);}catch(Exception e){} return 0; }
    private Connection con() { return ConnectDB.getInstance().getConnection(); }

    private String formatMoney(long v) { return new DecimalFormat("###,###,###").format(v)+"đ"; }
    private String compactMoney(double v) {
        if(v>=1_000_000_000) return String.format("%.1fTỷ",v/1_000_000_000);
        if(v>=1_000_000)     return String.format("%.2fM",v/1_000_000);
        if(v>=1_000)         return String.format("%.1fK",v/1_000);
        return String.valueOf((long)v);
    }
}
