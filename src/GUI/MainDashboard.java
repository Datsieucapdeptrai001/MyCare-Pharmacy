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

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
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
    private void xuLyDangXuat() {
       
        if (UserSession.getInstance().getCaHienTai() != null) {
            Utils.ThongBao.show(this, "CHƯA KẾT CA", 
                "Bạn đang trong ca làm việc. Vui lòng thực hiện KẾT CA để bàn giao tiền trước khi rời khỏi hệ thống!", 
                "WARNING");
            return; 
        }
        int check = JOptionPane.showConfirmDialog(this, "Xác nhận đăng xuất?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (check == JOptionPane.YES_OPTION) {
            UserSession.getInstance().logout();
            this.dispose();
            new ManHinhDangNhap().setVisible(true);
        }
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
            } else {
                System.out.println("LỖI: Không tìm thấy file logo tại: " + file.getAbsolutePath());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        menuButtons = new ArrayList<>();
        cardLayout  = new CardLayout();
        cardPanel   = new JPanel(cardLayout);

        mhChinh = new ManHinhChinh();
        ManHinhBanHang   mhBanHang  = new ManHinhBanHang();
        ManHinhSanPham   mhSanPham  = new ManHinhSanPham();
        ManHinhLoHang    mhLoHang   = new ManHinhLoHang();
        ManHinhKhuyenMai mhKhuyenMai= new ManHinhKhuyenMai();
        ManHinhThongKe   mhThongKe  = new ManHinhThongKe();
        ManHinhNhanVien  mhNhanVien = new ManHinhNhanVien();
        ManHinhKhachHang mhKhachHang= new ManHinhKhachHang();
        ManHinhHuongDan  mhHuongDan = new ManHinhHuongDan();
        ManHinhDoiTra    mhDoiTra   = new ManHinhDoiTra();

        cardPanel.add(mhChinh,      "Màn hình chính");
        cardPanel.add(mhBanHang,    "Bán hàng & Đổi trả");
        cardPanel.add(mhSanPham,    "Sản phẩm");
        cardPanel.add(mhLoHang,     "Lô hàng");
        cardPanel.add(mhKhuyenMai,  "Khuyến mại");
        cardPanel.add(mhThongKe,    "Thống kê");
        cardPanel.add(mhNhanVien,   "Nhân viên");
        cardPanel.add(mhKhachHang,  "Khách hàng");
        cardPanel.add(mhHuongDan,   "Hướng dẫn");
        cardPanel.add(mhDoiTra,     "DoiTra");

        if (!isAdmin) {
            mhSanPham.setReadOnly(true); 
            mhLoHang.setReadOnly(true);
            mhKhuyenMai.setReadOnly(true);
            mhNhanVien.setReadOnly(true);
            mhKhachHang.setReadOnly(true);
        }

        JPanel sidebar    = createSidebar();
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(createTopHeader(), BorderLayout.NORTH);
        rightPanel.add(cardPanel,         BorderLayout.CENTER);

        add(sidebar,    BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        cardLayout.show(cardPanel, "Màn hình chính");
        if (lblTieuDeTrang != null) {
            lblTieuDeTrang.setText("MÀN HÌNH CHÍNH");
        }
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
            Utils.ThongBao.show(this, "TỪ CHỐI TRUY CẬP", "Tài khoản của bạn không có quyền truy cập vào mục này!", "WARNING");
            return;
        }

        cardLayout.show(cardPanel, tabName);
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
    
    private class ThongTinNhanVienCombobox {
        private NhanVien nhanVien;
        public ThongTinNhanVienCombobox(NhanVien nhanVien) { 
            this.nhanVien = nhanVien; 
        }
        public NhanVien getNhanVien() { 
            return nhanVien; 
        }
        @Override 
        public String toString() { 
            return nhanVien.getNhanVien() + " - " + nhanVien.getHoVaTen(); 
        }
    }

    private class ComboboxTaiKhoanRenderer extends DefaultListCellRenderer {
        private final Icon iconNhanVien = new MenuIcon("USER");

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ThongTinNhanVienCombobox) {
                setText(value.toString());
                setIcon(iconNhanVien);
                setIconTextGap(8);
            }
            return this;
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

        boolean laQuanLy = UserSession.getInstance().isAdmin();
        
        JComboBox<ThongTinNhanVienCombobox> danhSachChuyenTaiKhoan = new JComboBox<>();
        danhSachChuyenTaiKhoan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        danhSachChuyenTaiKhoan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        danhSachChuyenTaiKhoan.setPreferredSize(new Dimension(240, 35));
        danhSachChuyenTaiKhoan.setRenderer(new ComboboxTaiKhoanRenderer());
        
        BUS_NhanVien busNhanVien = new BUS_NhanVien();
        List<NhanVien> tatCaNhanVien = busNhanVien.layDSNhanVien();
        ThongTinNhanVienCombobox mucDuocChonHienTai = null;
        String maNhanVienHienTai = UserSession.getInstance().getMaNhanVien();
        
        boolean[] dangCaiDatDuLieu = {true};
        
        for (NhanVien nhanVien : tatCaNhanVien) {
            ThongTinNhanVienCombobox mucTaiKhoan = new ThongTinNhanVienCombobox(nhanVien);
            danhSachChuyenTaiKhoan.addItem(mucTaiKhoan);
            if (nhanVien.getNhanVien().equals(maNhanVienHienTai)) {
                mucDuocChonHienTai = mucTaiKhoan;
            }
        }
        
        if (mucDuocChonHienTai != null) {
            danhSachChuyenTaiKhoan.setSelectedItem(mucDuocChonHienTai);
        }
        dangCaiDatDuLieu[0] = false;
        
        danhSachChuyenTaiKhoan.addActionListener(e -> {
            if (dangCaiDatDuLieu[0]) return;
            ThongTinNhanVienCombobox mucDuocChon = (ThongTinNhanVienCombobox) danhSachChuyenTaiKhoan.getSelectedItem();
            if (mucDuocChon == null || mucDuocChon.getNhanVien().getNhanVien().equals(UserSession.getInstance().getMaNhanVien())) {
                return; 
            }
            xulyChuyenTaiKhoan(mucDuocChon.getNhanVien(), danhSachChuyenTaiKhoan, dangCaiDatDuLieu);
        });

        pnlRight.add(lblTime);
        pnlRight.add(Box.createRigidArea(new Dimension(10, 0)));
        pnlRight.add(danhSachChuyenTaiKhoan);
        
        header.add(pnlRight, BorderLayout.EAST);
        return header;
    }

    private void xulyChuyenTaiKhoan(NhanVien nhanVienDich, JComboBox<ThongTinNhanVienCombobox> danhSachChuyenTaiKhoan, boolean[] dangCaiDatDuLieu) {
        BUS_TaiKhoan busTaiKhoan = new BUS_TaiKhoan();
        BUS_NhanVien busNhanVien = new BUS_NhanVien();
        
        TaiKhoan taiKhoanDich = busNhanVien.layTaiKhoanTheoMaNV(nhanVienDich.getNhanVien());
        if (taiKhoanDich == null) {
            hienThiThongBaoHeThong("Cảnh báo", "Nhân viên này hiện chưa được cấp tài khoản hệ thống!", "WARNING", Color.decode("#FFAB00"));
            khoiPhucComboboxTaiKhoan(danhSachChuyenTaiKhoan, dangCaiDatDuLieu);
            return;
        }
        
        if (!UserSession.getInstance().isAdmin()) {
            String matKhauNhapVao = hienThiDialogNhapMatKhau(taiKhoanDich);
            
            if (matKhauNhapVao == null) {
                khoiPhucComboboxTaiKhoan(danhSachChuyenTaiKhoan, dangCaiDatDuLieu);
                return;
            }
            
            if (!busTaiKhoan.authenticate(taiKhoanDich.getTenDangNhap(), matKhauNhapVao)) {
                hienThiThongBaoHeThong("Lỗi xác thực", "Mật khẩu không chính xác, không thể chuyển đổi tài khoản!", "WARNING", Color.decode("#FF4D4D"));
                khoiPhucComboboxTaiKhoan(danhSachChuyenTaiKhoan, dangCaiDatDuLieu);
                return;
            }
        }

        if (taiKhoanDich.getVaiTro() == VaiTro.ADMIN) {
            UserSession.getInstance().setTaiKhoan(taiKhoanDich);
            UserSession.getInstance().setCaHienTai(null); 
            hienThiThongBaoHeThong("Thành công", "Đã chuyển sang tài khoản QUẢN LÝ: " + nhanVienDich.getHoVaTen(), "CHECK_CIRCLE", Color.decode("#00A76F"));
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
                    hienThiThongBaoHeThong("Lỗi CSDL", "Không thể lưu thông tin ca làm việc vào hệ thống!", "WARNING", Color.decode("#FF4D4D"));
                }
            }
            
            if (caHienTaiCuaNhanVien == null) {
                UserSession.getInstance().setTaiKhoan(taiKhoanCu);
                UserSession.getInstance().setCaHienTai(caCu);
                khoiPhucComboboxTaiKhoan(danhSachChuyenTaiKhoan, dangCaiDatDuLieu);
                return; 
            }
        }
        
        UserSession.getInstance().setTaiKhoan(taiKhoanDich);
        UserSession.getInstance().setCaHienTai(caHienTaiCuaNhanVien);
        
        hienThiThongBaoHeThong("Thành công", "Đã chuyển sang tài khoản: " + nhanVienDich.getHoVaTen(), "CHECK_CIRCLE", Color.decode("#00A76F"));
        this.dispose();
        new MainDashboard().setVisible(true);
    }

    private void khoiPhucComboboxTaiKhoan(JComboBox<ThongTinNhanVienCombobox> danhSachChuyenTaiKhoan, boolean[] dangCaiDatDuLieu) {
        dangCaiDatDuLieu[0] = true;
        for (int i = 0; i < danhSachChuyenTaiKhoan.getItemCount(); i++) {
            ThongTinNhanVienCombobox mucHienTai = danhSachChuyenTaiKhoan.getItemAt(i);
            if (mucHienTai.getNhanVien().getNhanVien().equals(UserSession.getInstance().getMaNhanVien())) {
                danhSachChuyenTaiKhoan.setSelectedIndex(i);
                break;
            }
        }
        dangCaiDatDuLieu[0] = false;
    }

    // =================================================================================
    // FORM NHẬP MẬT KHẨU - CHUẨN DESIGN MÀN HÌNH KHUYẾN MÃI (Nền trắng, nút góc phải)
    // =================================================================================
    private String hienThiDialogNhapMatKhau(TaiKhoan taiKhoanDich) {
        JDialog dialog = new JDialog(this, "Xác thực tài khoản", true);
        dialog.setUndecorated(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.decode("#E5E9EF"), 1));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(15, 20, 5, 20));
        JLabel lblTitle = new JLabel("XÁC THỰC TÀI KHOẢN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.decode("#212B36"));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlBody = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JLabel lMsg = new JLabel("<html><center>Nhập mật khẩu cho tài khoản: <br><b style='color:#1A73E8; font-size:15px;'>" + taiKhoanDich.getTenDangNhap() + "</b></center></html>");
        lMsg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lMsg.setForeground(Color.decode("#212B36"));
        pnlBody.add(lMsg);
        
        JPasswordField txtPass = new JPasswordField(20);
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPass.setPreferredSize(new Dimension(320, 40));
        txtPass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#1A73E8"), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        pnlBody.add(txtPass);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#E5E9EF")));

        String[] ketQua = new String[]{null};

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setForeground(Color.decode("#212B36"));
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createLineBorder(Color.decode("#E5E9EF"), 1));
        btnCancel.setIcon(new MenuIcon("CLOSE"));
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnYes = new JButton("Đăng nhập");
        btnYes.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnYes.setBackground(Color.decode("#1A73E8"));
        btnYes.setForeground(Color.WHITE);
        btnYes.setFocusPainted(false);
        btnYes.setBorderPainted(false);
        btnYes.setIcon(new MenuIcon("RETURN"));
        btnYes.setPreferredSize(new Dimension(130, 38));
        btnYes.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        dialog.setSize(new Dimension(420, Math.max(dialog.getHeight() + 20, 220)));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        
        return ketQua[0];
    }

    // =================================================================================
    // FORM THÔNG BÁO HỆ THỐNG - CHUẨN DESIGN MÀN HÌNH KHUYẾN MÃI (Y CHANG HÌNH BẠN GỬI)
    // =================================================================================
    private void hienThiThongBaoHeThong(String tieuDe, String noiDung, String tenIcon, Color mauSac) {
        JDialog dialog = new JDialog(this, tieuDe, true);
        dialog.setUndecorated(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.decode("#E5E9EF"), 1));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(15, 20, 5, 20));
        JLabel lblTitle = new JLabel(tieuDe.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(mauSac); 
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlBody = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.add(new JLabel(new MenuIcon(tenIcon)));
        JLabel lblMsg = new JLabel("<html><div style='width:280px;'>" + noiDung + "</div></html>");
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMsg.setForeground(Color.decode("#212B36"));
        pnlBody.add(lblMsg);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#E5E9EF")));

        JButton btnOK = new JButton("Xác nhận");
        btnOK.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnOK.setBackground(mauSac);
        btnOK.setForeground(Color.WHITE);
        btnOK.setFocusPainted(false);
        btnOK.setBorderPainted(false);
        btnOK.setIcon(new MenuIcon("CHECK_CIRCLE"));
        btnOK.setPreferredSize(new Dimension(120, 38));
        btnOK.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOK.addActionListener(e -> dialog.dispose());

        pnlFooter.add(btnOK);

        mainPanel.add(pnlHeader, BorderLayout.NORTH);
        mainPanel.add(pnlBody, BorderLayout.CENTER);
        mainPanel.add(pnlFooter, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.pack();
        dialog.setSize(new Dimension(420, Math.max(dialog.getHeight() + 20, 200)));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // =================================================================================
    // FORM ĐĂNG XUẤT - CHUẨN DESIGN MÀN HÌNH KHUYẾN MÃI
    // =================================================================================
    private void hienThiThongBaoDangXuat() {
        JDialog dialog = new JDialog(this, "Xác nhận", true);
        dialog.setUndecorated(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.decode("#E5E9EF"), 1));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(15, 20, 5, 20));
        JLabel lblTitle = new JLabel("XÁC NHẬN ĐĂNG XUẤT");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.decode("#212B36"));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlBody = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.add(new JLabel(new MenuIcon("WARNING")));
        JLabel lblMsg = new JLabel("<html><div style='width:280px;'>Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?</div></html>");
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMsg.setForeground(Color.decode("#212B36"));
        pnlBody.add(lblMsg);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#E5E9EF")));

        JButton btnCancel = new JButton("Không");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancel.setBackground(Color.WHITE);
        btnCancel.setForeground(Color.decode("#212B36"));
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createLineBorder(Color.decode("#E5E9EF"), 1));
        btnCancel.setIcon(new MenuIcon("CLOSE"));
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnYes = new JButton("Có");
        btnYes.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnYes.setBackground(Color.decode("#FF4D4D")); 
        btnYes.setForeground(Color.WHITE);
        btnYes.setFocusPainted(false);
        btnYes.setBorderPainted(false);
        btnYes.setIcon(new MenuIcon("CHECK_CIRCLE"));
        btnYes.setPreferredSize(new Dimension(100, 38));
        btnYes.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        dialog.setSize(new Dimension(420, Math.max(dialog.getHeight() + 20, 200)));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.decode("#152A4B"));
        sidebar.setPreferredSize(new Dimension(230, 0));

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 20));
        logoPanel.setBackground(Color.decode("#152A4B"));
        logoPanel.setMaximumSize(new Dimension(230, 80));
        
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
        
        JLabel lblTenNhaThuoc = new JLabel("<html><b style='color:white;font-size:16px;'>MYCARE</b><br><span style='color:#00A76F;font-size:13px;font-weight:bold;'>PHARMACY</span></html>");
        logoPanel.add(lblTenNhaThuoc);
        sidebar.add(logoPanel);

        String initials = UserSession.getInstance().getInitials();
        String tenNV    = UserSession.getInstance().getTenHienThi();
        String chucVu   = UserSession.getInstance().getChucVuHienThi();
        boolean isAdmin = UserSession.getInstance().isAdmin();

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        userPanel.setBackground(Color.decode("#152A4B"));
        userPanel.setMaximumSize(new Dimension(230, 65));

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
        badgePanel.setMaximumSize(new Dimension(230, 26));
        JLabel lblBadge = new JLabel(isAdmin ? "● Quản lý · Toàn quyền" : "● Nhân viên · Dược sĩ");
        lblBadge.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblBadge.setForeground(isAdmin ? Color.decode("#FFAB00") : Color.decode("#00A76F"));
        badgePanel.add(lblBadge);
        sidebar.add(badgePanel);

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        for (String item : menuItems) {
            JButton btn = createMenuButton(item, false);
            if      (item.equals("Màn hình chính"))      btn.setIcon(new MenuIcon("HOME"));
            else if (item.equals("Bán hàng & Đổi trả"))  btn.setIcon(new MenuIcon("CART"));
            else if (item.equals("Sản phẩm"))             btn.setIcon(new MenuIcon("PILL"));
            else if (item.equals("Lô hàng"))              btn.setIcon(new MenuIcon("BOX"));
            else if (item.equals("Khuyến mại"))           btn.setIcon(new MenuIcon("GIFT"));
            else if (item.equals("Thống kê"))             btn.setIcon(new MenuIcon("CHART"));
            else if (item.equals("Nhân viên"))            btn.setIcon(new MenuIcon("USER"));
            else if (item.equals("Khách hàng"))           btn.setIcon(new MenuIcon("USERS"));
            else if (item.equals("Hướng dẫn"))            btn.setIcon(new MenuIcon("HELP"));
            menuButtons.add(btn);
            sidebar.add(btn);
        }

        setActiveButton(menuButtons.get(0));
        sidebar.add(Box.createVerticalGlue());

        JButton btnLogout = createMenuButton("Đăng xuất", true);
        btnLogout.setIcon(new MenuIcon("LOGOUT"));
        btnLogout.addActionListener(e -> hienThiThongBaoDangXuat());
        sidebar.add(btnLogout);

        return sidebar;
    }

    private JButton createMenuButton(String text, boolean isLogout) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(230, 50));
        btn.setPreferredSize(new Dimension(230, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false); btn.setOpaque(true); btn.setBorderPainted(true);
        btn.setIconTextGap(15);
        btn.setBackground(Color.decode("#152A4B"));
        btn.setForeground(isLogout ? Color.decode("#FF4D4D") : Color.decode("#E8F0FE"));
        javax.swing.border.Border bottom  = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#1E3F70"));
        javax.swing.border.Border padding = BorderFactory.createEmptyBorder(0, 20, 0, 0);
        btn.setBorder(BorderFactory.createCompoundBorder(bottom, padding));
        if (!isLogout) {
            btn.addActionListener(e -> { 
                setActiveButton(btn); 
                cardLayout.show(cardPanel, text); 
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
            if (btn.getText().contains("Khách hàng")) { setActiveButton(btn); break; }
        }
        if (moFormThem) {
            for (Component c : cardPanel.getComponents()) {
                if (c instanceof ManHinhKhachHang) { ((ManHinhKhachHang) c).moFormThemMoi(); break; }
            }
        }
    }

    public DefaultTableModel getModelKhachHang() {
        for (Component c : cardPanel.getComponents()) {
            if (c instanceof ManHinhKhachHang) return ((ManHinhKhachHang) c).getModel();
        }
        return null;
    }

    public ManHinhChinh getManHinhChinh() {
        return mhChinh;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ConnectDB.getInstance().connect();
            Utils.MigrationTool.main(new String[]{}); 
            new ManHinhDangNhap().setVisible(true);
        });
    }
}