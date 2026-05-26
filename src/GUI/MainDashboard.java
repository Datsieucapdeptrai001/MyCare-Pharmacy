package GUI;

import Utils.MenuIcon;
import ConnectDB.ConnectDB;
import Enumeration.VaiTro;
import Utils.UserSession;
import Utils.UIHelper;
import BUS.BUS_NhanVien;
import BUS.BUS_TaiKhoan;
import BUS.BUS_CaLamViec;
import Entity.TaiKhoan;
import Entity.NhanVien;
import Entity.CaLamViec;
import Utils.TooltipConfig;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import Utils.TelexFix;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainDashboard extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private List<JButton> menuButtons;
    private ManHinhChinh mhChinh;
    private ManHinhThongKe mhThongKe;

    private JLabel lblTieuDeTrang;

    private final String[] ALL_MENU_ITEMS = {
            "Màn hình chính", "Bán hàng & Đổi trả", "Sản phẩm", "Lô hàng",
            "Khuyến mại", "Thống kê", "Nhân viên", "Khách hàng", "Hướng dẫn"
    };

    private final String[] STAFF_MENU_ITEMS = {
            "Màn hình chính", "Bán hàng & Đổi trả", "Sản phẩm", "Lô hàng",
            "Khuyến mại", "Nhân viên", "Khách hàng", "Hướng dẫn"
    };
    private String[] menuItems;

    public void lamMoiManHinhChinh() {
        if (mhChinh != null) {
            cardPanel.remove(mhChinh);
        }
        mhChinh = new ManHinhChinh();
        cardPanel.add(mhChinh, "Màn hình chính");
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    public void lamMoiManHinhThongKe() {
        if (mhThongKe != null) cardPanel.remove(mhThongKe);
        mhThongKe = new ManHinhThongKe();
        cardPanel.add(mhThongKe, "Thống kê");
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private void xuLyDangXuat() {
        if (UserSession.getInstance().getCaHienTai() != null) {
            Utils.ThongBao.show(this, "CHƯA KẾT CA",
                    "Bạn đang trong ca làm việc. Vui lòng thực hiện KẾT CA để bàn giao tiền trước khi rời khỏi hệ thống!",
                    "WARNING");
            return;
        }
        hienThiThongBaoDangXuat();
    }

    public MainDashboard() {
        boolean isAdmin = UserSession.getInstance().isAdmin();
        menuItems = isAdmin ? ALL_MENU_ITEMS : STAFF_MENU_ITEMS;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        try {
            String imagePath = "data/logo.png";
            File file = new File(imagePath);
            if (file.exists()) {
                Image appIcon = ImageIO.read(file);
                this.setIconImage(appIcon);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        menuButtons = new ArrayList<>();
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Color.decode("#F4F6F8"));

        mhChinh = new ManHinhChinh();
        ManHinhBanHang mhBanHang = new ManHinhBanHang();
        ManHinhSanPham mhSanPham = new ManHinhSanPham();
        ManHinhLoHang mhLoHang = new ManHinhLoHang();
        ManHinhKhuyenMai mhKhuyenMai = new ManHinhKhuyenMai();
        mhThongKe = new ManHinhThongKe();
        ManHinhNhanVien mhNhanVien = new ManHinhNhanVien();
        ManHinhKhachHang mhKhachHang = new ManHinhKhachHang();
        ManHinhHuongDan mhHuongDan = new ManHinhHuongDan();
        ManHinhDoiTra mhDoiTra = new ManHinhDoiTra();

        cardPanel.add(mhChinh, "Màn hình chính");
        cardPanel.add(mhBanHang, "Bán hàng & Đổi trả");
        cardPanel.add(mhSanPham, "Sản phẩm");
        cardPanel.add(mhLoHang, "Lô hàng");
        cardPanel.add(mhKhuyenMai, "Khuyến mại");
        cardPanel.add(mhThongKe, "Thống kê");
        cardPanel.add(mhNhanVien, "Nhân viên");
        cardPanel.add(mhKhachHang, "Khách hàng");
        cardPanel.add(mhHuongDan, "Hướng dẫn");
        cardPanel.add(mhDoiTra, "DoiTra");

        if (!isAdmin) {
            mhSanPham.setReadOnly(true);
            mhLoHang.setReadOnly(true);
            mhKhuyenMai.setReadOnly(true);
            mhNhanVien.setReadOnly(true);
            mhKhachHang.setReadOnly(true);
        }

        JPanel sidebar = createSidebar();
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(createTopHeader(), BorderLayout.NORTH);
        rightPanel.add(cardPanel, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // Kích hoạt phím tắt toàn cục cho các tab
        setupKeyBindings();

        cardLayout.show(cardPanel, "Màn hình chính");
        if (lblTieuDeTrang != null) {
            lblTieuDeTrang.setText("MÀN HÌNH CHÍNH");
        }
    }

    // =================================================================================
    // CÀI ĐẶT PHÍM TẮT HOTKEYS (F1 - F9, F12)
    // =================================================================================
    private void setupKeyBindings() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        // Gán phím F1 -> F9 cho các nút menu (Dựa theo index danh sách quyền của tài
        // khoản)
        for (int i = 0; i < menuButtons.size(); i++) {
            JButton btn = menuButtons.get(i);
            int hotkeyNum = i + 1;

            if (hotkeyNum <= 9) { // Chỉ map đến F9 cho menu
                String actionKey = "Tab_F" + hotkeyNum;
                KeyStroke keyStroke = KeyStroke.getKeyStroke("F" + hotkeyNum);

                inputMap.put(keyStroke, actionKey);
                actionMap.put(actionKey, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        btn.doClick(); // Kích hoạt chuyển tab y như click chuột
                    }
                });

                // Thêm tooltip để hướng dẫn người dùng khi rê chuột vào menu
                btn.setToolTipText("Phím tắt nhanh: F" + hotkeyNum);

//                if (btn instanceof MenuShortcutButton) {
//                    ((MenuShortcutButton) btn).setShortcutText("F" + hotkeyNum);
//                }
            }
        }

        // Gán phím F12 cho chức năng Đăng xuất an toàn
        inputMap.put(KeyStroke.getKeyStroke("F12"), "LogoutAction");
        actionMap.put("LogoutAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                xuLyDangXuat();
            }
        });
    }

    public void chuyenSangTabBanHang() {
        for (JButton btn : menuButtons) {
            if (btn.getText().contains("Bán hàng")) {
                setActiveButton(btn);
                break;
            }
        }
    }

    public void switchTabAndFilter(String tabName, String keyword) {
        boolean hasAccess = false;
        for (JButton btn : menuButtons) {
            if (btn.getText().equals(tabName)) {
                hasAccess = true;
                break;
            }
        }
        if (!hasAccess) {
            Utils.ThongBao.show(this, "TỪ CHỐI TRUY CẬP", "Tài khoản của bạn không có quyền truy cập vào mục này!",
                    "WARNING");
            return;
        }

        cardLayout.show(cardPanel, tabName);
        if (tabName.equals("Thống kê") && mhThongKe != null) {
            mhThongKe.refreshAll();
        }
        for (JButton btn : menuButtons) {
            if (btn.getText().equals(tabName)) {
                setActiveButton(btn);
                break;
            }
        }
        for (Component c : cardPanel.getComponents()) {
            if (c instanceof ManHinhLoHang && tabName.equals("Lô hàng")) {
                ((ManHinhLoHang) c).filterData(keyword);
            }
        }
    }

    public void chuyenSangTabNhapLoHangMoiVaFill(String tenSP) {
        switchTabAndFilter("Lô hàng", "");
        for (Component c : cardPanel.getComponents()) {
            if (c instanceof ManHinhLoHang) {
                ((ManHinhLoHang) c).moManHinhNhapLoMoi(tenSP);
                break;
            }
        }
    }

    public void chuyenSangTabKhuyenMaiVaTaoMoi(java.util.Map<String, Object> autoFillData) {
        switchTabAndFilter("Khuyến mại", "");
        for (Component c : cardPanel.getComponents()) {
            if (c instanceof ManHinhKhuyenMai) {
                ((ManHinhKhuyenMai) c).openAddDialogWithAutoFill(autoFillData);
                break;
            }
        }
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));

        lblTieuDeTrang = new JLabel("MÀN HÌNH CHÍNH");
        lblTieuDeTrang.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTieuDeTrang.setForeground(Color.decode("#152A4B"));
        lblTieuDeTrang.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        lblTieuDeTrang.setIcon(new MenuIcon("DOT_FILL"));
        lblTieuDeTrang.setIconTextGap(10);
        header.add(lblTieuDeTrang, BorderLayout.WEST);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlRight.setBackground(Color.WHITE);

        JLabel lblTime = new JLabel();
        lblTime.setForeground(Color.decode("#637381"));
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        new Timer(1000, ev -> lblTime.setText(
                new SimpleDateFormat("HH:mm:ss — E dd/MM/yyyy").format(new Date()))).start();

        JButton btnChuyenTaiKhoan = new JButton(
                UserSession.getInstance().getMaNhanVien() + " - " + UserSession.getInstance().getTenHienThi());
        btnChuyenTaiKhoan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnChuyenTaiKhoan.setIcon(new MenuIcon("USER"));
        btnChuyenTaiKhoan.setIconTextGap(8);
        btnChuyenTaiKhoan.setBackground(Color.WHITE);
        btnChuyenTaiKhoan.setForeground(Color.decode("#152A4B"));
        btnChuyenTaiKhoan.setFocusPainted(false);
        btnChuyenTaiKhoan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChuyenTaiKhoan.setPreferredSize(new Dimension(240, 35));
        btnChuyenTaiKhoan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));

        JPopupMenu menuTaiKhoan = new JPopupMenu();
        menuTaiKhoan.setBackground(Color.WHITE);
        menuTaiKhoan.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1));

        BUS_NhanVien busNhanVien = new BUS_NhanVien();
        List<NhanVien> tatCaNhanVien = busNhanVien.layDSNhanVien();
        String maNhanVienHienTai = UserSession.getInstance().getMaNhanVien() != null
                ? UserSession.getInstance().getMaNhanVien().trim()
                : "";

        boolean coTaiKhoanKhac = false;
        for (NhanVien nhanVien : tatCaNhanVien) {
            if (nhanVien.getNhanVien() != null && !nhanVien.getNhanVien().trim().equalsIgnoreCase(maNhanVienHienTai)) {
                JMenuItem item = new JMenuItem(nhanVien.getNhanVien() + " - " + nhanVien.getHoVaTen());
                item.setFont(new Font("Segoe UI", Font.BOLD, 13));
                item.setIcon(new MenuIcon("USERS"));
                item.setBackground(Color.WHITE);
                item.setForeground(Color.decode("#212B36"));
                item.setCursor(new Cursor(Cursor.HAND_CURSOR));
                item.setPreferredSize(new Dimension(240, 35));

                item.addActionListener(e -> {
                    SwingUtilities.invokeLater(() -> xulyChuyenTaiKhoan(nhanVien));
                });

                menuTaiKhoan.add(item);
                coTaiKhoanKhac = true;
            }
        }

        if (!coTaiKhoanKhac) {
            JMenuItem emptyItem = new JMenuItem("Không có tài khoản khác");
            emptyItem.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            emptyItem.setForeground(Color.GRAY);
            emptyItem.setBackground(Color.WHITE);
            emptyItem.setEnabled(false);
            menuTaiKhoan.add(emptyItem);
        }

        btnChuyenTaiKhoan.addActionListener(e -> {
            menuTaiKhoan.show(btnChuyenTaiKhoan, 0, btnChuyenTaiKhoan.getHeight());
        });

        pnlRight.add(lblTime);
        pnlRight.add(Box.createRigidArea(new Dimension(10, 0)));
        pnlRight.add(btnChuyenTaiKhoan);

        header.add(pnlRight, BorderLayout.EAST);
        return header;
    }

    private void xulyChuyenTaiKhoan(NhanVien nhanVienDich) {
        BUS_TaiKhoan busTaiKhoan = new BUS_TaiKhoan();
        BUS_NhanVien busNhanVien = new BUS_NhanVien();

        TaiKhoan taiKhoanDich = busNhanVien.layTaiKhoanTheoMaNV(nhanVienDich.getNhanVien());
        if (taiKhoanDich == null) {
            hienThiThongBaoHeThong("Cảnh báo", "Nhân viên này hiện chưa được cấp tài khoản hệ thống!", "WARNING",
                    Color.decode("#FFAB00"));
            return;
        }

        if (!UserSession.getInstance().isAdmin()) {
            String matKhauNhapVao = hienThiDialogNhapMatKhau(taiKhoanDich);

            if (matKhauNhapVao == null) {
                return;
            }

            if (!busTaiKhoan.authenticate(taiKhoanDich.getTenDangNhap(), matKhauNhapVao)) {
                hienThiThongBaoHeThong("Lỗi xác thực", "Mật khẩu không chính xác, không thể chuyển đổi tài khoản!",
                        "ERROR", Color.decode("#EF4444"));
                return;
            }
        }

        if (taiKhoanDich.getVaiTro() == VaiTro.ADMIN) {
            UserSession.getInstance().setTaiKhoan(taiKhoanDich);
            UserSession.getInstance().setCaHienTai(null);
            hienThiThongBaoHeThong("Thành công", "Đã chuyển sang tài khoản QUẢN LÝ: " + nhanVienDich.getHoVaTen(),
                    "SUCCESS", Color.decode("#10B981"));
            this.dispose();
            new MainDashboard().setVisible(true);
            return;
        }

        BUS_CaLamViec busCaLamViec = new BUS_CaLamViec();
        CaLamViec caHienTaiCuaNhanVien = busCaLamViec.getCaHienTai(nhanVienDich.getNhanVien());

        if (caHienTaiCuaNhanVien == null) {
            TaiKhoan taiKhoanCu = UserSession.getInstance().getTaiKhoan();
            CaLamViec caCu = UserSession.getInstance().getCaHienTai();

            UserSession.getInstance().setTaiKhoan(taiKhoanDich);

            ManHinhMoCa manHinhMoCa = new ManHinhMoCa(this);

            JButton btnCloseMoCa = new JButton(new MenuIcon("CLOSE"));
            btnCloseMoCa.setBounds(manHinhMoCa.getWidth() - 40, 20, 30, 30);
            btnCloseMoCa.setContentAreaFilled(false);
            btnCloseMoCa.setBorderPainted(false);
            btnCloseMoCa.setFocusPainted(false);
            btnCloseMoCa.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnCloseMoCa.addActionListener(ev -> manHinhMoCa.dispose());
            manHinhMoCa.getLayeredPane().add(btnCloseMoCa, JLayeredPane.POPUP_LAYER);

            manHinhMoCa.setVisible(true);

            if (manHinhMoCa.isConfirmed()) {
                CaLamViec caLamViecMoi = new CaLamViec();
                caLamViecMoi.setId("CA" + System.currentTimeMillis() % 100000);
                caLamViecMoi.setNhanVienId(taiKhoanDich.getNhanVienId());
                caLamViecMoi.setTienDauCa((double) manHinhMoCa.getTongTienDauCa());
                caLamViecMoi.setLoaiCa(manHinhMoCa.getSelectedCa());
                caLamViecMoi.setThoiGianBatDau(java.time.LocalDateTime.now());
                caLamViecMoi.setTienHeThongGhiNhan(caLamViecMoi.getTienDauCa());

                if (busCaLamViec.moCa(caLamViecMoi)) {
                    caHienTaiCuaNhanVien = busCaLamViec.getCaHienTai(nhanVienDich.getNhanVien());
                } else {
                    hienThiThongBaoHeThong("Lỗi CSDL", "Không thể lưu thông tin ca làm việc vào hệ thống!", "ERROR",
                            Color.decode("#EF4444"));
                }
            }

            if (caHienTaiCuaNhanVien == null) {
                UserSession.getInstance().setTaiKhoan(taiKhoanCu);
                UserSession.getInstance().setCaHienTai(caCu);
                return;
            }
        }

        UserSession.getInstance().setTaiKhoan(taiKhoanDich);
        UserSession.getInstance().setCaHienTai(caHienTaiCuaNhanVien);

        hienThiThongBaoHeThong("Thành công", "Đã chuyển sang tài khoản: " + nhanVienDich.getHoVaTen(), "SUCCESS",
                Color.decode("#10B981"));
        this.dispose();
        new MainDashboard().setVisible(true);
    }
    class MenuShortcutButton extends JButton {
        private String shortcutText = "";

        public MenuShortcutButton(String text) {
            super(text);
        }

        public void setShortcutText(String shortcutText) {
            this.shortcutText = shortcutText == null ? "" : shortcutText;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

            g2.dispose();

            super.paintComponent(g);

            if (shortcutText != null && !shortcutText.trim().isEmpty()) {
                Graphics2D gShortcut = (Graphics2D) g.create();
                gShortcut.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                gShortcut.setFont(new Font("Segoe UI", Font.BOLD, 12));
                gShortcut.setColor(new Color(203, 213, 225));

                FontMetrics fm = gShortcut.getFontMetrics();
                int x = getWidth() - fm.stringWidth(shortcutText) - 14;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

                gShortcut.drawString(shortcutText, x, y);
                gShortcut.dispose();
            }
        }
    }
    // =================================================================================
    // CÁC HÀM UI CUSTOM BO GÓC (RoundedButton, RoundedPanel)
    // =================================================================================
    class RoundedButton extends JButton {
        private Color bgColor;

        public RoundedButton(String text, Color bgColor) {
            super(text);
            this.bgColor = bgColor;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    class RoundedBorderPanel extends JPanel {
        private Color borderColor;

        public RoundedBorderPanel(Color bgColor, Color borderColor) {
            super(new BorderLayout());
            setOpaque(false);
            setBackground(bgColor);
            this.borderColor = borderColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // =================================================================================
    // FORM NHẬP MẬT KHẨU
    // =================================================================================
    private String hienThiDialogNhapMatKhau(TaiKhoan taiKhoanDich) {
        JDialog dialog = new JDialog(this, "Xác thực tài khoản", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        RoundedBorderPanel mainPanel = new RoundedBorderPanel(Color.WHITE, Color.decode("#1A73E8"));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(20, 20, 10, 20));
        JLabel lblTitle = new JLabel("XÁC THỰC TÀI KHOẢN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.decode("#1A73E8"));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlBody = new JPanel(new BorderLayout(0, 15));
        pnlBody.setOpaque(false);
        pnlBody.setBorder(new EmptyBorder(15, 20, 25, 20));

        JLabel lMsg = new JLabel(
                "<html><div style='text-align:center;'>Nhập mật khẩu cho tài khoản: <br><b style='color:#152A4B; font-size:15px;'>"
                        + taiKhoanDich.getTenDangNhap() + "</b></div></html>",
                SwingConstants.CENTER);
        lMsg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lMsg.setForeground(Color.decode("#333333"));
        pnlBody.add(lMsg, BorderLayout.NORTH);

        JPanel pnlPassWrapper = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#F4F6F8"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(Color.decode("#DFE3E8"));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pnlPassWrapper.setOpaque(false);
        pnlPassWrapper.setPreferredSize(new Dimension(320, 45));

        JPasswordField txtPass = new JPasswordField(20);
        txtPass.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtPass.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        txtPass.setMargin(new Insets(0, 0, 0, 0));
        txtPass.setOpaque(false);
        txtPass.setBackground(new Color(0, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 15, 0, 15);
        pnlPassWrapper.add(txtPass, gbc);

        JPanel centerWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerWrap.setOpaque(false);
        centerWrap.add(pnlPassWrapper);

        pnlBody.add(centerWrap, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlFooter.setOpaque(false);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#E5E9EF")));

        String[] ketQua = new String[] { null };

        RoundedButton btnCancel = new RoundedButton("Hủy bỏ", Color.decode("#F4F6F8"));
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancel.setForeground(Color.decode("#333333"));
        btnCancel.setIcon(new MenuIcon("CLOSE"));
        btnCancel.setPreferredSize(new Dimension(110, 38));
        btnCancel.addActionListener(e -> dialog.dispose());

        RoundedButton btnYes = new RoundedButton("Đăng nhập", Color.decode("#1A73E8"));
        btnYes.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnYes.setForeground(Color.WHITE);
        btnYes.setIcon(new MenuIcon("RETURN"));
        btnYes.setPreferredSize(new Dimension(130, 38));
        btnYes.addActionListener(e -> {
            ketQua[0] = new String(txtPass.getPassword());
            dialog.dispose();
        });

        txtPass.addActionListener(e -> btnYes.doClick());

        pnlFooter.add(btnCancel);
        pnlFooter.add(btnYes);

        mainPanel.add(pnlHeader, BorderLayout.NORTH);
        mainPanel.add(pnlBody, BorderLayout.CENTER);
        mainPanel.add(pnlFooter, BorderLayout.SOUTH);

        dialog.add(mainPanel);

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent e) {
                txtPass.requestFocusInWindow();
            }
        });

        dialog.pack();
        dialog.setSize(420, dialog.getHeight());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return ketQua[0];
    }

    private void hienThiThongBaoHeThong(String tieuDe, String noiDung, String type, Color fallbackColor) {
        Utils.ThongBao.show(this, tieuDe, noiDung, type);
    }

    // =================================================================================
    // FORM ĐĂNG XUẤT
    // =================================================================================
    private void hienThiThongBaoDangXuat() {
        JDialog dialog = new JDialog(this, "Xác nhận", true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        RoundedBorderPanel mainPanel = new RoundedBorderPanel(Color.WHITE, Color.decode("#EF4444"));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(20, 20, 5, 20));
        JLabel lblTitle = new JLabel("XÁC NHẬN ĐĂNG XUẤT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.decode("#EF4444"));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlBody = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        pnlBody.setOpaque(false);
        pnlBody.setBorder(new EmptyBorder(10, 0, 15, 20));

        JLabel iconLbl = new JLabel(new MenuIcon("LOGOUT", 32, Color.decode("#EF4444")));
        pnlBody.add(iconLbl);

        JLabel lblMsg = new JLabel(
                "<html><div style='width:260px; line-height:1.4;'>Bạn có chắc chắn muốn đăng xuất khỏi hệ thống ngay bây giờ?</div></html>");
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMsg.setForeground(Color.decode("#333333"));
        pnlBody.add(lblMsg);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlFooter.setOpaque(false);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#E5E9EF")));

        RoundedButton btnCancel = new RoundedButton("Không", Color.decode("#F4F6F8"));
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancel.setForeground(Color.decode("#333333"));
        btnCancel.setIcon(new MenuIcon("CLOSE"));
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnCancel.addActionListener(e -> dialog.dispose());

        RoundedButton btnYes = new RoundedButton("Có", Color.decode("#EF4444"));
        btnYes.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnYes.setForeground(Color.WHITE);
        btnYes.setIcon(new MenuIcon("CHECK_CIRCLE"));
        btnYes.setPreferredSize(new Dimension(100, 38));
        btnYes.addActionListener(e -> {
            dialog.dispose();
            UserSession.getInstance().logout();
            this.dispose();
            new ManHinhDangNhap().setVisible(true);
        });

        pnlFooter.add(btnCancel);
        pnlFooter.add(btnYes);

        mainPanel.add(pnlHeader, BorderLayout.NORTH);
        mainPanel.add(pnlBody, BorderLayout.CENTER);
        mainPanel.add(pnlFooter, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.pack();
        dialog.setSize(420, dialog.getHeight());
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.decode("#152A4B"));
        sidebar.setPreferredSize(new Dimension(240, 0));

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 20));
        logoPanel.setBackground(Color.decode("#152A4B"));
        logoPanel.setMaximumSize(new Dimension(240, 80));

        try {
            File fileAnh = new File("data/logo.png");
            if (fileAnh.exists()) {
                Image img = ImageIO.read(fileAnh);
                ImageIcon iconLogoDuyNhat = new ImageIcon(img.getScaledInstance(45, 45, Image.SCALE_SMOOTH));
                logoPanel.add(new JLabel(iconLogoDuyNhat));
            }
        } catch (Exception e) {
            System.err.println("Lỗi load logo góc trái: " + e.getMessage());
        }

        JLabel lblTenNhaThuoc = new JLabel(
                "<html><b style='color:white;font-size:16px;'>MYCARE</b><br><span style='color:#00A76F;font-size:13px;font-weight:bold;'>PHARMACY</span></html>");
        logoPanel.add(lblTenNhaThuoc);
        sidebar.add(logoPanel);

        String initials = UserSession.getInstance().getInitials();
        String tenNV = UserSession.getInstance().getTenHienThi();
        String chucVu = UserSession.getInstance().getChucVuHienThi();
        boolean isAdmin = UserSession.getInstance().isAdmin();

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        userPanel.setBackground(Color.decode("#152A4B"));
        userPanel.setMaximumSize(new Dimension(240, 65));

        JLabel lblAvatar = new JLabel(initials, SwingConstants.CENTER);
        lblAvatar.setOpaque(true);
        lblAvatar.setBackground(isAdmin ? Color.decode("#C62828") : Color.decode("#1362B1"));
        lblAvatar.setForeground(Color.WHITE);
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAvatar.setPreferredSize(new Dimension(40, 40));
        UIHelper.setRoundedCorners(lblAvatar, 40);

        userPanel.add(lblAvatar);
        userPanel.add(new JLabel("<html><b style='color:white;font-size:13px;'>" + tenNV + "</b><br>"
                + "<span style='color:#00BFFF;font-size:11px;'>" + chucVu + "</span></html>"));
        sidebar.add(userPanel);

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 2));
        badgePanel.setBackground(Color.decode("#152A4B"));
        badgePanel.setMaximumSize(new Dimension(240, 30));
        JLabel lblBadge = new JLabel(isAdmin ? "● Quản lý · Toàn quyền" : "● Nhân viên · Dược sĩ");
        lblBadge.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblBadge.setForeground(isAdmin ? Color.decode("#FFAB00") : Color.decode("#00A76F"));
        badgePanel.add(lblBadge);
        sidebar.add(badgePanel);

        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));

        for (String item : menuItems) {
            final JButton btn = createMenuButton(item, false);

            if (item.equals("Màn hình chính"))
                btn.setIcon(new MenuIcon("HOME"));
            else if (item.equals("Bán hàng & Đổi trả"))
                btn.setIcon(new MenuIcon("CART"));
            else if (item.equals("Sản phẩm"))
                btn.setIcon(new MenuIcon("PILL"));
            else if (item.equals("Lô hàng"))
                btn.setIcon(new MenuIcon("BOX"));
            else if (item.equals("Khuyến mại"))
                btn.setIcon(new MenuIcon("GIFT"));
            else if (item.equals("Thống kê"))
                btn.setIcon(new MenuIcon("CHART"));
            else if (item.equals("Nhân viên"))
                btn.setIcon(new MenuIcon("USER"));
            else if (item.equals("Khách hàng"))
                btn.setIcon(new MenuIcon("USERS"));
            else if (item.equals("Hướng dẫn"))
                btn.setIcon(new MenuIcon("HELP"));
            menuButtons.add(btn);

            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false);
            wrap.setBorder(new EmptyBorder(2, 10, 2, 10));
            wrap.add(btn, BorderLayout.CENTER);
            wrap.setMaximumSize(new Dimension(240, 50));
            sidebar.add(wrap);
        }

        setActiveButton(menuButtons.get(0));
        sidebar.add(Box.createVerticalGlue());

        final JButton btnLogout = createMenuButton("Đăng xuất", true);
        btnLogout.setIcon(new MenuIcon("LOGOUT"));
        btnLogout.setToolTipText("Phím tắt nhanh: F12");

//        if (btnLogout instanceof MenuShortcutButton) {
//            ((MenuShortcutButton) btnLogout).setShortcutText("F12");
//        }
        btnLogout.addActionListener(e -> hienThiThongBaoDangXuat());
        JPanel wrapLogout = new JPanel(new BorderLayout());
        wrapLogout.setOpaque(false);
        wrapLogout.setBorder(new EmptyBorder(10, 10, 15, 10));
        wrapLogout.add(btnLogout, BorderLayout.CENTER);
        wrapLogout.setMaximumSize(new Dimension(240, 75));
        sidebar.add(wrapLogout);

        return sidebar;
    }

    private JButton createMenuButton(String text, boolean isLogout) {
    	final MenuShortcutButton btn = new MenuShortcutButton(text);
        btn.setPreferredSize(new Dimension(220, 46));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setIconTextGap(15);
        btn.setBackground(Color.decode("#152A4B"));
        btn.setForeground(isLogout ? Color.decode("#FF4D4D") : Color.decode("#E8F0FE"));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        if (!isLogout) {
            btn.addActionListener(e -> {
                setActiveButton(btn);
                cardLayout.show(cardPanel, text);
            });
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (!btn.getBackground().equals(Color.decode("#1A73E8"))) {
                        btn.setBackground(Color.decode("#1E3F70"));
                        btn.repaint();
                    }
                }

                public void mouseExited(MouseEvent e) {
                    if (!btn.getBackground().equals(Color.decode("#1A73E8"))) {
                        btn.setBackground(Color.decode("#152A4B"));
                        btn.repaint();
                    }
                }
            });
        } else {
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    btn.setBackground(new Color(255, 77, 77, 30));
                    btn.repaint();
                }

                public void mouseExited(MouseEvent e) {
                    btn.setBackground(Color.decode("#152A4B"));
                    btn.repaint();
                }
            });
        }
        return btn;
    }

    private void setActiveButton(JButton activeBtn) {
        for (JButton btn : menuButtons) {
            boolean active = (btn == activeBtn);
            btn.setBackground(active ? Color.decode("#1A73E8") : Color.decode("#152A4B"));
            btn.setForeground(active ? Color.WHITE : Color.decode("#E8F0FE"));
        }
        if (lblTieuDeTrang != null && activeBtn != null) {
            lblTieuDeTrang.setText(activeBtn.getText().toUpperCase());
        }
    }

    public void chuyenSangTabKhachHang(boolean moFormThem) {
        cardLayout.show(cardPanel, "Khách hàng");
        for (JButton btn : menuButtons) {
            if (btn.getText().contains("Khách hàng")) {
                setActiveButton(btn);
                break;
            }
        }
        if (moFormThem) {
            for (Component c : cardPanel.getComponents()) {
                if (c instanceof ManHinhKhachHang) {
                    ((ManHinhKhachHang) c).moFormThemMoi();
                    break;
                }
            }
        }
    }

    public DefaultTableModel getModelKhachHang() {
        for (Component c : cardPanel.getComponents()) {
            if (c instanceof ManHinhKhachHang)
                return ((ManHinhKhachHang) c).getModel();
        }
        return null;
    }

    public ManHinhChinh getManHinhChinh() {
        return mhChinh;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ConnectDB.getInstance().connect();
            TooltipConfig.tatTatCaTooltip();
            TelexFix.setupGlobalTelexFix();

            ManHinhDangNhap login = new ManHinhDangNhap();
            TelexFix.applyWindow(login);
            login.setVisible(true);
        });
    }
}