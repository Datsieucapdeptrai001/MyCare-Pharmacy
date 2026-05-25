package GUI;

import BUS.BUS_NhanVien;
import Entity.NhanVien;
import Entity.TaiKhoan;
import Enumeration.ChucVu;
import Enumeration.TrangThaiLamViec;
import Utils.MenuIcon;
import Utils.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ManHinhNhanVien extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;
    private JLabel lblTotalNhanVien, lblDangLamViec; 

    // Các thành phần của Sidebar chi tiết
    private JPanel pnlDetail;
    private JLabel lblDetAvatar, lblDetName, lblDetId;
    private JLabel lblDetPhone, lblDetEmail, lblDetAddress;
    private JLabel lblDetRole, lblDetCCHN, lblDetStatus;
    private JButton btnEdit;
    private JButton btnAdd;
    
    // Khai báo BUS xử lý nghiệp vụ
    private BUS_NhanVien busNhanVien;

    public ManHinhNhanVien() {
        busNhanVien = new BUS_NhanVien();
        initUI();
    }

    private void initUI() {
        this.setLayout(new BorderLayout(0, 20));
        this.setBackground(Color.decode("#F3F4F6")); 
        this.setBorder(new EmptyBorder(20, 25, 20, 25));

        // ==================== 1. PHẦU ĐẦU ====================
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ NHÂN VIÊN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.decode("#1E293B"));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setOpaque(false);

        JPanel pnlSearchWrapper = new JPanel(new BorderLayout(8, 0));
        pnlSearchWrapper.setBackground(Color.WHITE);
        pnlSearchWrapper.setPreferredSize(new Dimension(280, 40));
        pnlSearchWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)));

        pnlSearchWrapper.add(new JLabel(new MenuIcon("SEARCH")), BorderLayout.WEST);

        String placeholder = "Tên, mã, SĐT...";
        txtSearch = new JTextField(placeholder);
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setBorder(null);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txtSearch.getText().equals(placeholder)) { txtSearch.setText(""); txtSearch.setForeground(Color.BLACK); }
            }
            public void focusLost(FocusEvent e) {
                if (txtSearch.getText().isEmpty()) { txtSearch.setText(placeholder); txtSearch.setForeground(Color.GRAY); }
            }
        });
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { applyFilter(); }
        });
        pnlSearchWrapper.add(txtSearch, BorderLayout.CENTER);

        JButton btnReset = createActionBtn("Làm mới", "#6C757D", "REFRESH");
        btnReset.addActionListener(e -> {
            txtSearch.setText(placeholder);
            txtSearch.setForeground(Color.GRAY);
            applyFilter();
            loadData(); 
        });

        btnAdd = createActionBtn("+ Thêm NV", "#DC2626", null);
        btnAdd.addActionListener(e -> {
            Window p = SwingUtilities.getWindowAncestor(this);
            DialogThemNhanVien dialog = new DialogThemNhanVien((Frame) p, this, -1, null, null, null, null, null, null, null, null, null);
            dialog.setVisible(true);
        });
        
        pnlActions.add(pnlSearchWrapper);
        pnlActions.add(btnReset);
        pnlActions.add(btnAdd);
        pnlHeader.add(pnlActions, BorderLayout.EAST);

        // ==================== 2. THẺ THỐNG KÊ ====================
        JPanel pnlCards = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlCards.setOpaque(false);
        pnlCards.setPreferredSize(new Dimension(0, 90));
        
        pnlCards.add(createSummaryCard("Tổng nhân viên", "0", true)); 
        pnlCards.add(createSummaryCard("Đang làm việc", "0", false));

        JPanel pnlTop = new JPanel(new BorderLayout(0, 20));
        pnlTop.setOpaque(false);
        pnlTop.add(pnlHeader, BorderLayout.NORTH);
        pnlTop.add(pnlCards, BorderLayout.CENTER);

        this.add(pnlTop, BorderLayout.NORTH);

        // ==================== 3. KHU VỰC BẢNG ====================
        JPanel pnlCenter = new JPanel(new BorderLayout(20, 0)); 
        pnlCenter.setOpaque(false);

        // Đã thêm cột Địa chỉ vào đây
        String[] cols = {"Mã NV", "Họ tên", "Số CCHN", "Điện thoại", "Email", "Địa chỉ", "Chức vụ", "Trạng thái", "Thao tác"};
        model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        
        table.setRowHeight(50);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Color.decode("#F1F3F5"));
        table.setSelectionBackground(Color.decode("#E0F2FE")); 

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 45));
        header.setBackground(Color.decode("#E0F2FE")); 
        header.setForeground(Color.decode("#1E293B"));
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));

        table.setDefaultRenderer(Object.class, new NhanVienTableRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(160); 
        table.getColumnModel().getColumn(5).setPreferredWidth(180); // Cột Địa chỉ
        table.getColumnModel().getColumn(8).setPreferredWidth(60);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8")));
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        
        // FIX LỖI LAG THANH CUỘN CHO BẢNG
        sp.getVerticalScrollBar().setUnitIncrement(20);
        sp.getHorizontalScrollBar().setUnitIncrement(20);

        // -- SIDEBAR CHI TIẾT --
        pnlDetail = createDetailSidebar();
        pnlDetail.setVisible(false); 

        pnlCenter.add(sp, BorderLayout.CENTER);
        pnlCenter.add(pnlDetail, BorderLayout.EAST);
        this.add(pnlCenter, BorderLayout.CENTER);

        // --- SỰ KIỆN CLICK BẢNG ---
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int modelRow = table.convertRowIndexToModel(row);
                    updateDetailSidebar(modelRow); 
                    pnlDetail.setVisible(true);    
                    revalidate();
                    repaint();
                }
            }
        });

        // --- SỰ KIỆN CLICK NÚT SỬA TRÊN SIDEBAR ---
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                String maNV = model.getValueAt(modelRow, 0).toString();
                String hoten = model.getValueAt(modelRow, 1).toString();
                String cchn = model.getValueAt(modelRow, 2).toString();
                String sdt = model.getValueAt(modelRow, 3).toString();
                String email = model.getValueAt(modelRow, 4).toString();
                String diaChi = model.getValueAt(modelRow, 5).toString(); // Lấy địa chỉ
                String chucVu = model.getValueAt(modelRow, 6).toString();
                String trangThai = model.getValueAt(modelRow, 7).toString();
                
                // Trích xuất thông tin tài khoản từ BUS
                TaiKhoan tk = busNhanVien.layTaiKhoanTheoMaNV(maNV);
                String tenDangNhap = (tk != null && tk.getTenDangNhap() != null) ? tk.getTenDangNhap() : "";
                String matKhau = (tk != null && tk.getMatKhau() != null) ? tk.getMatKhau() : "";

                Window p = SwingUtilities.getWindowAncestor(this);
                DialogThemNhanVien dialog = new DialogThemNhanVien((Frame) p, this, modelRow, hoten, cchn, sdt, email, diaChi, chucVu, trangThai, tenDangNhap, matKhau);
                dialog.setVisible(true);
            }
        });

        // Khởi tạo tải dữ liệu
        loadData();

        // RÀNG BUỘC QUYỀN HẠN
        if (!UserSession.getInstance().isAdmin()) {
            setReadOnly(true);
        }
    }

 // ==================== TẢI DỮ LIỆU TỪ TẦNG BUS ====================
    public void loadData() {
        model.setRowCount(0); 
        List<NhanVien> dsNhanVien = busNhanVien.layDSNhanVien();
        
        // --- THÊM 2 BIẾN NÀY ĐỂ LẤY THÔNG TIN NGƯỜI ĐANG ĐĂNG NHẬP ---
        boolean isAdmin = UserSession.getInstance().isAdmin();
        String currentUserName = UserSession.getInstance().getTenHienThi();
        
        if (dsNhanVien != null) {
            for (NhanVien nv : dsNhanVien) {
                
                // 🛡️ CHẶN DỮ LIỆU: Nếu không phải ADMIN và tên không trùng với người đang đăng nhập -> Bỏ qua, không hiển thị!
                if (!isAdmin && !nv.getHoVaTen().equals(currentUserName)) {
                    continue; 
                }

                String chucVuUI = (nv.getChucVu() == ChucVu.NGUOI_QUAN_LY) ? "Quản lý" : "Dược sĩ";
                
                String trangThaiUI = "Đang làm việc";
                if (nv.getTrangThaiLamViec() == TrangThaiLamViec.NGHI_PHEP) trangThaiUI = "Nghỉ phép";
                else if (nv.getTrangThaiLamViec() == TrangThaiLamViec.THOI_VIEC) trangThaiUI = "Đã nghỉ việc";

                model.addRow(new Object[]{
                    nv.getNhanVien(), // <--- TRẢ LẠI HÀM ZIN CỦA ÔNG RỒI NÈ
                    nv.getHoVaTen(), 
                    nv.getSoChungChiHanhNghe() == null ? "" : nv.getSoChungChiHanhNghe(), 
                    nv.getSdt(), 
                    nv.getEmail() == null ? "" : nv.getEmail(), 
                    nv.getDiaChi() == null ? "" : nv.getDiaChi(), // Hiển thị địa chỉ
                    chucVuUI, 
                    trangThaiUI, 
                    ""
                });
            }
        }
        updateStats();
    }

    // ==================== CÁC HÀM TIỆN ÍCH & SIDEBAR ====================
    public void updateStats() {
        if (lblTotalNhanVien != null) lblTotalNhanVien.setText(String.valueOf(model.getRowCount()));
        if (lblDangLamViec != null) {
            int count = 0;
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 7).toString().equalsIgnoreCase("Đang làm việc")) count++;
            }
            lblDangLamViec.setText(String.valueOf(count));
        }
    }
    
    public void refreshSidebarIfVisible(int modelRow) {
        if (pnlDetail.isVisible() && table.getSelectedRow() != -1) {
            updateDetailSidebar(modelRow);
        }
    }
    
    public DefaultTableModel getModel() {
        return model;
    }
    
    public BUS_NhanVien getBusNhanVien() {
        return busNhanVien;
    }

    private JPanel createDetailSidebar() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setPreferredSize(new Dimension(280, 0)); 
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1));

        // HEADER
        JPanel pnlDetHeader = new JPanel(new BorderLayout(10, 0));
        pnlDetHeader.setBackground(Color.WHITE);
        pnlDetHeader.setBorder(new EmptyBorder(15, 15, 10, 10));

        lblDetAvatar = new JLabel("N", SwingConstants.CENTER);
        lblDetAvatar.setOpaque(true);
        lblDetAvatar.setBackground(Color.decode("#152A4B"));
        lblDetAvatar.setForeground(Color.WHITE);
        lblDetAvatar.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblDetAvatar.setPreferredSize(new Dimension(45, 45));

        JPanel pnlName = new JPanel(new GridLayout(2, 1));
        pnlName.setBackground(Color.WHITE);
        lblDetName = new JLabel("Nguyễn Văn A");
        lblDetName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDetId = new JLabel("NV001");
        lblDetId.setForeground(Color.GRAY);
        pnlName.add(lblDetName);
        pnlName.add(lblDetId);

        JButton btnCloseDet = new JButton("×"); 
        btnCloseDet.setFont(new Font("Arial", Font.BOLD, 20));
        btnCloseDet.setForeground(Color.GRAY);
        btnCloseDet.setBorderPainted(false);
        btnCloseDet.setContentAreaFilled(false);
        btnCloseDet.setFocusPainted(false);
        btnCloseDet.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCloseDet.addActionListener(e -> pnlDetail.setVisible(false));

        pnlDetHeader.add(lblDetAvatar, BorderLayout.WEST);
        pnlDetHeader.add(pnlName, BorderLayout.CENTER);
        pnlDetHeader.add(btnCloseDet, BorderLayout.EAST);

        // TABS
        JPanel pnlTabs = new JPanel(new GridLayout(1, 2));
        pnlTabs.setBackground(Color.WHITE);
        pnlTabs.setPreferredSize(new Dimension(0, 35));
        JLabel lblTab1 = new JLabel("Thông tin", SwingConstants.CENTER);
        lblTab1.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTab1.setForeground(Color.decode("#1967D2"));
        lblTab1.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#1967D2")));
        JLabel lblTab2 = new JLabel("Phân quyền", SwingConstants.CENTER);
        lblTab2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTab2.setForeground(Color.GRAY);
        lblTab2.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));
        pnlTabs.add(lblTab1);
        pnlTabs.add(lblTab2);

        // BODY
        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);

        JPanel pnlInfoList = new JPanel(new GridLayout(3, 1, 0, 10)); 
        pnlInfoList.setBackground(Color.WHITE);
        pnlInfoList.setBorder(new EmptyBorder(15, 15, 15, 15));
        pnlInfoList.setAlignmentX(Component.LEFT_ALIGNMENT); 

        lblDetPhone = new JLabel(" 0910000000");
        lblDetPhone.setIcon(new MenuIcon("PHONE"));
        lblDetEmail = new JLabel(" nhanvien@mycare.vn");
        lblDetEmail.setIcon(new MenuIcon("MAIL"));
        lblDetAddress = new JLabel(" TP. Hồ Chí Minh");
        lblDetAddress.setIcon(new MenuIcon("LOCATION"));

        for(JLabel l : new JLabel[]{lblDetPhone, lblDetEmail, lblDetAddress}) {
            l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            l.setForeground(Color.decode("#4B5563"));
            l.setIconTextGap(10);
            pnlInfoList.add(l);
        }

        // Hộp thống kê
        JPanel pnlStats = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlStats.setBackground(Color.WHITE);
        pnlStats.setBorder(new EmptyBorder(0, 15, 15, 15));
        pnlStats.setMaximumSize(new Dimension(1000, 70));
        pnlStats.setAlignmentX(Component.LEFT_ALIGNMENT); 

        JPanel box1 = new JPanel(new GridLayout(2, 1));
        box1.setBackground(Color.decode("#F0F9FF")); 
        box1.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel l1 = new JLabel("Chức vụ", SwingConstants.CENTER);
        l1.setForeground(Color.GRAY);
        lblDetRole = new JLabel("Dược sĩ", SwingConstants.CENTER);
        lblDetRole.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDetRole.setForeground(Color.decode("#1967D2"));
        box1.add(l1); box1.add(lblDetRole);

        JPanel box2 = new JPanel(new GridLayout(2, 1));
        box2.setBackground(Color.decode("#FAF5FF")); 
        box2.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel l2 = new JLabel("Số CCHN", SwingConstants.CENTER);
        l2.setForeground(Color.GRAY);
        lblDetCCHN = new JLabel("CCHN-1234", SwingConstants.CENTER);
        lblDetCCHN.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDetCCHN.setForeground(Color.decode("#9333EA"));
        box2.add(l2); box2.add(lblDetCCHN);

        pnlStats.add(box1); pnlStats.add(box2);

        // Trạng thái
        JPanel pnlStatus = new JPanel();
        pnlStatus.setLayout(new BoxLayout(pnlStatus, BoxLayout.Y_AXIS));
        pnlStatus.setBackground(Color.WHITE);
        pnlStatus.setBorder(new EmptyBorder(0, 15, 10, 15)); 
        pnlStatus.setAlignmentX(Component.LEFT_ALIGNMENT); 

        JLabel lblStatusText = new JLabel("Trạng thái hiện tại:");
        lblStatusText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStatusText.setForeground(Color.GRAY);
        lblStatusText.setAlignmentX(Component.LEFT_ALIGNMENT); 

        lblDetStatus = new JLabel("Đang làm việc");
        lblDetStatus.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDetStatus.setForeground(Color.decode("#16A34A")); 
        lblDetStatus.setBorder(new EmptyBorder(2, 0, 4, 0));
        lblDetStatus.setAlignmentX(Component.LEFT_ALIGNMENT); 

        pnlStatus.add(lblStatusText);
        pnlStatus.add(lblDetStatus);

        pnlBody.add(pnlInfoList);
        pnlBody.add(pnlStats);
        pnlBody.add(pnlStatus);

        // FOOTER ACTIONS ĐƯỢC CHUYỂN VÀO TRONG BODY ĐỂ LUÔN NẰM NGAY DƯỚI TRẠNG THÁI
        JPanel pnlFooterActions = new JPanel(new GridLayout(1, 1, 0, 8));
        pnlFooterActions.setBackground(Color.WHITE);
        pnlFooterActions.setBorder(new EmptyBorder(10, 15, 15, 15));
        pnlFooterActions.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlFooterActions.setMaximumSize(new Dimension(1000, 70));

        btnEdit = new JButton("Chỉnh sửa");
        btnEdit.setIcon(new MenuIcon("EDIT"));
        btnEdit.setIconTextGap(8);
        btnEdit.setBackground(Color.decode("#1967D2"));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setFocusPainted(false);
        btnEdit.setBorderPainted(false);
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnlFooterActions.add(btnEdit);
        pnlBody.add(pnlFooterActions);

        // FIX LỖI GIÃN DỌC KHI PHÓNG TO: Bọc pnlBody vào BorderLayout.NORTH
        JPanel pnlBodyWrapper = new JPanel(new BorderLayout());
        pnlBodyWrapper.setBackground(Color.WHITE);
        pnlBodyWrapper.add(pnlBody, BorderLayout.NORTH);
        
        JScrollPane spBody = new JScrollPane(pnlBodyWrapper);
        spBody.setBorder(null);

        // FIX LỖI LAG THANH CUỘN CHO SIDEBAR
        spBody.getVerticalScrollBar().setUnitIncrement(20);
        spBody.getHorizontalScrollBar().setUnitIncrement(20);

        JPanel pnlTopWrap = new JPanel(new BorderLayout());
        pnlTopWrap.add(pnlDetHeader, BorderLayout.NORTH);
        pnlTopWrap.add(pnlTabs, BorderLayout.SOUTH);

        pnl.add(pnlTopWrap, BorderLayout.NORTH);
        pnl.add(spBody, BorderLayout.CENTER);

        return pnl;
    }

    private void updateDetailSidebar(int modelRow) {
        String id = model.getValueAt(modelRow, 0).toString();
        String name = model.getValueAt(modelRow, 1).toString();
        String cchn = model.getValueAt(modelRow, 2).toString();
        String phone = model.getValueAt(modelRow, 3).toString();
        String email = model.getValueAt(modelRow, 4).toString();
        String diaChi = model.getValueAt(modelRow, 5).toString(); // Đọc địa chỉ từ model
        String role = model.getValueAt(modelRow, 6).toString();
        String status = model.getValueAt(modelRow, 7).toString();

        lblDetAvatar.setText(name.substring(0, 1).toUpperCase()); 
        lblDetName.setText(name);
        lblDetId.setText(id);

        lblDetPhone.setText(" " + phone);
        lblDetEmail.setText(" " + (email.isEmpty() ? "Chưa cập nhật" : email));
        lblDetAddress.setText(" " + (diaChi.isEmpty() ? "Chưa cập nhật" : diaChi)); // Cập nhật nhãn địa chỉ

        lblDetRole.setText(role);
        lblDetCCHN.setText(cchn.isEmpty() ? "---" : cchn);
        lblDetStatus.setText(status);
        
        if(status.equalsIgnoreCase("Đã nghỉ việc")) lblDetStatus.setForeground(Color.decode("#DC2626"));
        else if(status.equalsIgnoreCase("Nghỉ phép")) lblDetStatus.setForeground(Color.decode("#D97706"));
        else lblDetStatus.setForeground(Color.decode("#16A34A"));
    }

    private JPanel createSummaryCard(String title, String value, boolean isTotalCard) {
        JPanel pnl = new JPanel(new GridLayout(2, 1, 0, 5));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1),
            new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(Color.decode("#6B7280"));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblValue.setForeground(Color.decode("#111827"));

        if (isTotalCard) lblTotalNhanVien = lblValue; 
        else lblDangLamViec = lblValue;

        pnl.add(lblTitle);
        pnl.add(lblValue);
        return pnl;
    }

    class NhanVienTableRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasF, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasF, r, c);
            
            // Cập nhật lại chỉ số cột canh giữa do đã thêm cột Địa chỉ
            if (c == 6 || c == 7 || c == 8) lbl.setHorizontalAlignment(CENTER);
            else lbl.setHorizontalAlignment(LEFT);

            lbl.setOpaque(true);
            lbl.setBackground(isSel ? Color.decode("#E0F2FE") : Color.WHITE);
            lbl.setForeground(Color.decode("#374151")); 
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lbl.setBorder(new EmptyBorder(0, 10, 0, 10)); 
            lbl.setIcon(null);

            if (c == 0) lbl.setForeground(Color.decode("#1967D2")); 
            if (c == 1) lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
            if (c == 6) { lbl.setForeground(Color.decode("#9333EA")); lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); }
            if (c == 7) { 
                String st = lbl.getText();
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                if(st.equals("Đang làm việc")) lbl.setForeground(Color.decode("#16A34A"));
                else if(st.equals("Nghỉ phép")) lbl.setForeground(Color.decode("#D97706"));
                else lbl.setForeground(Color.decode("#DC2626"));
            }
            if (c == 8) { 
                lbl.setText(""); 
                lbl.setIcon(new MenuIcon("EDIT")); 
                lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            return lbl;
        }
    }

    private JButton createActionBtn(String txt, String hexColor, String iconName) {
        JButton btn = new JButton(txt);
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setBackground(Color.decode(hexColor));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        if (iconName != null) { btn.setIcon(new MenuIcon(iconName)); btn.setIconTextGap(6); }
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void applyFilter() {
        String search = txtSearch.getText().trim();
        if (search.isEmpty() || search.equals("Tên, mã, SĐT...")) sorter.setRowFilter(null);
        else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + search));
    }

    public void setReadOnly(boolean readOnly) {
        if (!readOnly) return;
        
        if (btnAdd != null) btnAdd.setVisible(false);
        if (btnEdit != null) btnEdit.setVisible(false);
        disableButtonsByText(this, "Thêm mới", "Nhập Excel", "Thêm", "Xóa", "Sửa", "Lưu");
        
        if (table != null) {
            try {
                javax.swing.table.TableColumn colThaoTac = table.getColumn("Thao tác"); 
                table.removeColumn(colThaoTac);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    private void disableButtonsByText(java.awt.Container container, String... texts) {
        for (java.awt.Component c : container.getComponents()) {
            if (c instanceof javax.swing.JButton) {
                javax.swing.JButton btn = (javax.swing.JButton) c;
                for (String t : texts) {
                    if (t.equals(btn.getText())) { btn.setVisible(false); break; }
                }
            } else if (c instanceof java.awt.Container) {
                disableButtonsByText((java.awt.Container) c, texts);
            }
        }
    }
}

// ====================================================================================
// 2. CLASS PHỤ: DIALOG THÊM / SỬA NHÂN VIÊN (GIAO TIẾP VỚI BUS)
// ====================================================================================
class DialogThemNhanVien extends JDialog {

    private JTextField txtHoTen, txtCCHN, txtSdt, txtEmail, txtDiaChi; // Thêm biến txtDiaChi
    private JTextField txtTenDangNhap, txtMatKhau;
    private JComboBox<String> cboTrangThai;
    private JLabel lblErrHoTen, lblErrSdt, lblErrEmail, lblErrCCHN, lblErrDiaChi, lblErrTenDangNhap, lblErrMatKhau; // Thêm lblErrDiaChi
    private ManHinhNhanVien parentScreen;
    
    private JLabel lblTitle;
    private JButton btnThem;
    private int editRow; 
    private String currentChucVu = "Dược sĩ";

    // Cập nhật constructor để nhận thêm biến diaChi
    public DialogThemNhanVien(Frame parent, ManHinhNhanVien parentScreen, int editRow, String hoten, String cchn, String sdt, String email, String diaChi, String chucVu, String trangThai, String tenDangNhap, String matKhau) {
        super(parent, editRow == -1 ? "Thêm nhân viên mới" : "Chỉnh sửa nhân viên", true);
        this.parentScreen = parentScreen;
        this.editRow = editRow;
        
        if (chucVu != null && !chucVu.isEmpty()) {
            this.currentChucVu = chucVu;
        }
        
        initUI(parent);

        if (editRow != -1) {
            lblTitle.setText("Chỉnh sửa nhân viên");
            btnThem.setText("Lưu thay đổi");
            
            if (hoten != null && !hoten.trim().isEmpty() && !hoten.equals("Nhập họ và tên đầy đủ")) { 
                txtHoTen.setText(hoten); 
                txtHoTen.setForeground(Color.BLACK); 
            }
            if (cchn != null && !cchn.trim().isEmpty() && !cchn.equals("---") && !cchn.equals("CCHN-xxxx")) { 
                txtCCHN.setText(cchn); 
                txtCCHN.setForeground(Color.BLACK); 
            }
            if (sdt != null && !sdt.trim().isEmpty() && !sdt.equals("0912345678")) { 
                txtSdt.setText(sdt); 
                txtSdt.setForeground(Color.BLACK); 
            }
            if (email != null && !email.trim().isEmpty() && !email.equals("Chưa cập nhật") && !email.equals("email@mycare.vn")) { 
                txtEmail.setText(email); 
                txtEmail.setForeground(Color.BLACK); 
            }
            // Điền địa chỉ cũ vào TextField khi Edit
            if (diaChi != null && !diaChi.trim().isEmpty() && !diaChi.equals("Nhập địa chỉ")) { 
                txtDiaChi.setText(diaChi); 
                txtDiaChi.setForeground(Color.BLACK); 
            }
            
            if (tenDangNhap != null && !tenDangNhap.trim().isEmpty()) {
                txtTenDangNhap.setText(tenDangNhap);
                txtTenDangNhap.setForeground(Color.BLACK);
            }
            
            cboTrangThai.setSelectedItem(trangThai);
        }
    }

    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel("");
        lbl.setForeground(Color.decode("#DC2626"));
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lbl.setVisible(false);
        return lbl;
    }

    private void initUI(Frame parent) {
        setSize(600, 650);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setLayout(new BorderLayout());

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.decode("#152A4B")); 
        pnlHeader.setBorder(new EmptyBorder(12, 15, 12, 15));

        lblTitle = new JLabel("Thêm nhân viên mới");
        lblTitle.setIcon(new MenuIcon("USER")); 
        lblTitle.setIconTextGap(10);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JButton btnClose = new JButton("X");
        btnClose.setForeground(Color.WHITE);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(btnClose, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlBody = new JPanel(new GridBagLayout());
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(15, 25, 15, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8); 

        // Khởi tạo các nhãn lỗi
        lblErrHoTen = createErrorLabel();
        lblErrSdt = createErrorLabel();
        lblErrEmail = createErrorLabel();
        lblErrCCHN = createErrorLabel();
        lblErrDiaChi = createErrorLabel(); // Khởi tạo lỗi cho Địa chỉ
        lblErrTenDangNhap = createErrorLabel();
        lblErrMatKhau = createErrorLabel();

        // Dòng 1: Họ tên
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        pnlBody.add(createLabel("Họ và tên", true), gbc);
        
        gbc.gridy = 1;
        txtHoTen = createTextField("Nhập họ và tên đầy đủ");
        pnlBody.add(txtHoTen, gbc);
        
        gbc.gridy = 2;
        pnlBody.add(lblErrHoTen, gbc);

        // Dòng 2: SĐT, Email
        gbc.gridwidth = 1; gbc.weightx = 0.5;
        gbc.gridx = 0; gbc.gridy = 3;
        pnlBody.add(createLabel("Số điện thoại", true), gbc);
        
        gbc.gridx = 1;
        pnlBody.add(createLabel("Email", false), gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        txtSdt = createTextField("0912345678");
        pnlBody.add(txtSdt, gbc);
        
        gbc.gridx = 1;
        txtEmail = createTextField("email@mycare.vn");
        pnlBody.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        pnlBody.add(lblErrSdt, gbc);
        
        gbc.gridx = 1;
        pnlBody.add(lblErrEmail, gbc);

        // Dòng 3: Số CCHN và Địa chỉ
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 6;
        pnlBody.add(createLabel("Số CCHN", false), gbc);
        
        gbc.gridx = 1;
        pnlBody.add(createLabel("Địa chỉ", true), gbc); // Địa chỉ bắt buộc

        gbc.gridx = 0; gbc.gridy = 7;
        txtCCHN = createTextField("CCHN-xxxx");
        pnlBody.add(txtCCHN, gbc);
        
        gbc.gridx = 1;
        txtDiaChi = createTextField("Nhập địa chỉ");
        pnlBody.add(txtDiaChi, gbc);
        
        gbc.gridx = 0; gbc.gridy = 8;
        pnlBody.add(lblErrCCHN, gbc);
        
        gbc.gridx = 1;
        pnlBody.add(lblErrDiaChi, gbc);

        // Dòng 4: Thông tin Đăng nhập
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 9;
        pnlBody.add(createLabel("Tên đăng nhập", true), gbc);
        
        gbc.gridx = 1;
        pnlBody.add(createLabel("Mật khẩu", true), gbc);

        gbc.gridx = 0; gbc.gridy = 10;
        txtTenDangNhap = createTextField("Tên đăng nhập (viết liền)");
        pnlBody.add(txtTenDangNhap, gbc);
        
        gbc.gridx = 1;
        txtMatKhau = createTextField(editRow != -1 ? "(Để trống nếu không đổi)" : "Nhập mật khẩu");
        pnlBody.add(txtMatKhau, gbc);

        gbc.gridx = 0; gbc.gridy = 11;
        pnlBody.add(lblErrTenDangNhap, gbc);
        
        gbc.gridx = 1;
        pnlBody.add(lblErrMatKhau, gbc);

        // Dòng 5: Trạng thái
        gbc.gridx = 0; gbc.gridy = 12; gbc.gridwidth = 2;
        pnlBody.add(createLabel("Trạng thái", true), gbc);

        gbc.gridy = 13;
        cboTrangThai = createComboBox(new String[]{"Đang làm việc", "Nghỉ phép", "Đã nghỉ việc"});
        pnlBody.add(cboTrangThai, gbc);

        // Bọc pnlBody vào JScrollPane để chống lẹm trên/dưới khi có lỗi hiển thị thêm
        JScrollPane scrollPane = new JScrollPane(pnlBody);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#DFE3E8")));

        JButton btnHuy = new JButton("Hủy bỏ");
        btnHuy.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnHuy.setBackground(Color.WHITE);
        btnHuy.setForeground(Color.decode("#6B7280"));
        btnHuy.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#D1D5DB"), 1, true),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        btnHuy.setFocusPainted(false);
        btnHuy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHuy.addActionListener(e -> dispose());

        btnThem = new JButton(editRow != -1 ? "Lưu thay đổi" : "Thêm nhân viên");
        btnThem.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnThem.setBackground(Color.decode("#2563EB")); 
        btnThem.setForeground(Color.WHITE);
        btnThem.setIcon(new MenuIcon(editRow != -1 ? "SAVE" : "USER_ADD"));
        btnThem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#2563EB"), 1, true),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        btnThem.setFocusPainted(false);
        btnThem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // ================= GỌI TẦNG BUS ĐỂ THỰC THI NGHIỆP VỤ =================
        btnThem.addActionListener(e -> {
            String hoten = txtHoTen.getText().trim();
            String cchn = txtCCHN.getText().trim();
            String sdt = txtSdt.getText().trim();
            String email = txtEmail.getText().trim();
            String diaChi = txtDiaChi.getText().trim(); // Lấy dữ liệu địa chỉ
            String trangThaiUI = cboTrangThai.getSelectedItem().toString();
            
            String tenDangNhap = txtTenDangNhap.getText().trim();
            String matKhau = txtMatKhau.getText().trim();
            
            // Lọc placeholder
            if(hoten.equals("Nhập họ và tên đầy đủ")) hoten = "";
            if(sdt.equals("0912345678")) sdt = "";
            if(email.equals("email@mycare.vn")) email = "";
            if(cchn.equals("CCHN-xxxx")) cchn = "";
            if(diaChi.equals("Nhập địa chỉ")) diaChi = "";
            if(tenDangNhap.equals("Tên đăng nhập (viết liền)")) tenDangNhap = "";
            if(matKhau.equals("Nhập mật khẩu")) matKhau = "";

            // Reset trạng thái lỗi trước khi kiểm tra
            resetError(txtHoTen, lblErrHoTen);
            resetError(txtSdt, lblErrSdt);
            resetError(txtEmail, lblErrEmail);
            resetError(txtCCHN, lblErrCCHN);
            resetError(txtDiaChi, lblErrDiaChi);
            resetError(txtTenDangNhap, lblErrTenDangNhap);
            resetError(txtMatKhau, lblErrMatKhau);

            boolean hasError = false;

            if (hoten.isEmpty()) {
                setError(txtHoTen, lblErrHoTen, "* Vui lòng nhập họ và tên đầy đủ");
                hasError = true;
            }
            if (sdt.isEmpty()) {
                setError(txtSdt, lblErrSdt, "* Vui lòng nhập số điện thoại");
                hasError = true;
            } else if (!sdt.matches("^(0|\\+84)[0-9]{9}$")) {
                setError(txtSdt, lblErrSdt, "* Số điện thoại không hợp lệ (gồm 10 số)");
                hasError = true;
            }
            if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                setError(txtEmail, lblErrEmail, "* Email không đúng định dạng");
                hasError = true;
            }
            if (diaChi.isEmpty()) { // Kiểm tra rỗng cho địa chỉ
                setError(txtDiaChi, lblErrDiaChi, "* Vui lòng nhập địa chỉ");
                hasError = true;
            }
            if (tenDangNhap.isEmpty()) {
                setError(txtTenDangNhap, lblErrTenDangNhap, "* Vui lòng nhập tên đăng nhập");
                hasError = true;
            } else if (tenDangNhap.contains(" ")) {
                setError(txtTenDangNhap, lblErrTenDangNhap, "* Tên đăng nhập không được chứa khoảng trắng");
                hasError = true;
            }
            if (matKhau.isEmpty()) {
                setError(txtMatKhau, lblErrMatKhau, "* Vui lòng nhập mật khẩu");
                hasError = true;
            }

            if (hasError) {
                return;
            }

            // Gán Enumeration: Mặc định là Dược sĩ do bỏ mục chọn (Chỉ có 1 Quản lý)
            ChucVu chucVuEnum = ChucVu.DUOC_SI;
            String chucVuUI = "Dược sĩ";
            
            // Nếu là cập nhật, giữ nguyên chức vụ cũ
            if (editRow != -1) {
                chucVuUI = currentChucVu;
                chucVuEnum = chucVuUI.equals("Quản lý") ? ChucVu.NGUOI_QUAN_LY : ChucVu.DUOC_SI;
            }
            
            TrangThaiLamViec trangThaiEnum = TrangThaiLamViec.DANG_LAM_VIEC;
            if (trangThaiUI.equals("Nghỉ phép")) trangThaiEnum = TrangThaiLamViec.NGHI_PHEP;
            else if (trangThaiUI.equals("Đã nghỉ việc")) trangThaiEnum = TrangThaiLamViec.THOI_VIEC;

            BUS_NhanVien busNV = parentScreen.getBusNhanVien();
            DefaultTableModel mainModel = parentScreen.getModel();

            if (editRow != -1) {
                // Thực thi quy trình Cập nhật
                String id = mainModel.getValueAt(editRow, 0).toString();
                // BẮT BUỘC Entity.NhanVien phải có thuộc tính diaChi trong Constructor
                NhanVien nv = new NhanVien(id, hoten, cchn, sdt, email, diaChi, chucVuEnum, trangThaiEnum); 
                
                String ketQua = busNV.capNhatNhanVienVaTaiKhoan(nv, tenDangNhap, matKhau);
                
                if (ketQua.equals("SUCCESS")) {
                    mainModel.setValueAt(hoten, editRow, 1);
                    mainModel.setValueAt(cchn, editRow, 2);
                    mainModel.setValueAt(sdt, editRow, 3);
                    mainModel.setValueAt(email, editRow, 4);
                    mainModel.setValueAt(diaChi, editRow, 5); // Cập nhật lại UI địa chỉ
                    mainModel.setValueAt(chucVuUI, editRow, 6);
                    mainModel.setValueAt(trangThaiUI, editRow, 7);
                    
                    parentScreen.updateStats();
                    parentScreen.refreshSidebarIfVisible(editRow);
                    showNotification("Thành công", "Cập nhật nhân viên và tài khoản thành công!", "success");
                    dispose();
                } else {
                    // Phân tích kết quả từ BUS để map vào ô báo lỗi phù hợp
                    String lowerKetQua = ketQua.toLowerCase();
                    if (lowerKetQua.contains("tên đăng nhập")) {
                        setError(txtTenDangNhap, lblErrTenDangNhap, "* " + ketQua);
                    } else if (lowerKetQua.contains("số điện thoại") || lowerKetQua.contains("sđt")) {
                        setError(txtSdt, lblErrSdt, "* " + ketQua);
                    } else if (lowerKetQua.contains("cchn") || lowerKetQua.contains("chứng chỉ")) {
                        setError(txtCCHN, lblErrCCHN, "* " + ketQua);
                    } else if (lowerKetQua.contains("email")) {
                        setError(txtEmail, lblErrEmail, "* " + ketQua);
                    } else {
                        showNotification("Lỗi cập nhật", ketQua, "error");
                    }
                }
            } else {
                // TỰ ĐỘNG PHÁT SINH MÃ NHÂN VIÊN THEO CHỨC VỤ (QL-xxxx / DS-xxxx)
                String prefixId = chucVuEnum == ChucVu.NGUOI_QUAN_LY ? "QL-" : "DS-";
                int maxIdNum = 0;
                
                // Trích xuất mã lớn nhất hiện có trong CSDL (thông qua tầng BUS)
                List<NhanVien> dsTatCaNV = busNV.layDSNhanVien();
                if (dsTatCaNV != null) {
                    for (NhanVien nv : dsTatCaNV) {
                        String currentId = nv.getNhanVien();
                        if (currentId != null && currentId.startsWith(prefixId)) {
                            try {
                                int num = Integer.parseInt(currentId.substring(3));
                                if (num > maxIdNum) {
                                    maxIdNum = num;
                                }
                            } catch (NumberFormatException ex) {
                                // Bỏ qua nếu có lỗi parse định dạng
                            }
                        }
                    }
                }
                
                // Tạo ID mới tự động tăng 4 chữ số
                String idMoi = prefixId + String.format("%04d", maxIdNum + 1); 
                
                // BẮT BUỘC Entity.NhanVien phải có thuộc tính diaChi trong Constructor
                NhanVien nv = new NhanVien(idMoi, hoten, cchn, sdt, email, diaChi, chucVuEnum, trangThaiEnum); 
                
                String ketQua = busNV.themNhanVienVaTaiKhoan(nv, tenDangNhap, matKhau);
                
                if (ketQua.equals("SUCCESS")) {
                    mainModel.addRow(new Object[]{ idMoi, hoten, cchn, sdt, email, diaChi, chucVuUI, trangThaiUI, "" });
                    parentScreen.updateStats();
                    showNotification("Thành công", "Thêm nhân viên và tạo tài khoản thành công!<br>Mã tự tạo: <b>" + idMoi + "</b>", "success");
                    dispose();
                } else {
                    // Phân tích kết quả từ BUS để map vào ô báo lỗi phù hợp
                    String lowerKetQua = ketQua.toLowerCase();
                    if (lowerKetQua.contains("tên đăng nhập")) {
                        setError(txtTenDangNhap, lblErrTenDangNhap, "* " + ketQua);
                    } else if (lowerKetQua.contains("số điện thoại") || lowerKetQua.contains("sđt")) {
                        setError(txtSdt, lblErrSdt, "* " + ketQua);
                    } else if (lowerKetQua.contains("cchn") || lowerKetQua.contains("chứng chỉ")) {
                        setError(txtCCHN, lblErrCCHN, "* " + ketQua);
                    } else if (lowerKetQua.contains("email")) {
                        setError(txtEmail, lblErrEmail, "* " + ketQua);
                    } else {
                        showNotification("Lỗi thêm mới", ketQua, "error");
                    }
                }
            }
        });

        pnlFooter.add(btnHuy);
        pnlFooter.add(btnThem);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private void setError(JTextField txt, JLabel lbl, String msg) {
        lbl.setText(msg);
        lbl.setVisible(true);
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DC2626"), 2, true),
            BorderFactory.createEmptyBorder(0, 11, 0, 11)
        ));
    }

    private void resetError(JTextField txt, JLabel lbl) {
        lbl.setVisible(false);
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#D1D5DB"), 1, true),
            BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));
    }

    private JLabel createLabel(String text, boolean isRequired) {
        String html = "<html><span style='color:#374151; font-family:Segoe UI; font-size:14px; font-weight:bold;'>" + text + "</span>";
        if (isRequired) html += " <span style='color:#DC2626;'>*</span>";
        html += "</html>";
        return new JLabel(html);
    }

    private JTextField createTextField(String placeholder) {
        JTextField txt = new JTextField(placeholder);
        txt.setPreferredSize(new Dimension(100, 40));
        txt.setMinimumSize(new Dimension(100, 40));
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setForeground(Color.GRAY);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#D1D5DB"), 1, true),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));
        txt.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txt.getText().equals(placeholder)) { txt.setText(""); txt.setForeground(Color.BLACK); }
                txt.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#3B82F6"), 2, true),
                    BorderFactory.createEmptyBorder(0, 11, 0, 11)
                ));
            }
            public void focusLost(FocusEvent e) {
                if (txt.getText().isEmpty()) { txt.setForeground(Color.GRAY); txt.setText(placeholder); }
                txt.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.decode("#D1D5DB"), 1, true),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)
                ));
            }
        });
        return txt;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> cbo = new JComboBox<>(items);
        cbo.setBackground(Color.WHITE);
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbo.setPreferredSize(new Dimension(100, 40));
        cbo.setMinimumSize(new Dimension(100, 40));
        return cbo;
    }

    // =======================================================================
    // HÀM HIỂN THỊ THÔNG BÁO TÙY CHỈNH (THAY THẾ CHO JOPTIONPANE)
    // =======================================================================
    private void showNotification(String title, String message, String type) {
        JDialog dialog = new JDialog(this, "", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);

        Color headerBg, iconBg, borderColor;
        String iconType;
        
        if (type.equals("success")) {
            headerBg = Color.decode("#00A76F");
            iconBg = Color.decode("#E8F5E9");
            borderColor = Color.decode("#00A76F");
            iconType = "CORRECT"; 
        } else if (type.equals("error")) {
            headerBg = Color.decode("#DC2626"); 
            iconBg = Color.decode("#FFEBEE");
            borderColor = Color.decode("#DC2626");
            iconType = "CANCEL"; 
        } else { // warning
            headerBg = Color.decode("#F59E0B");
            iconBg = Color.decode("#FEF3C7");
            borderColor = Color.decode("#F59E0B");
            iconType = "WARNING"; 
        }

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(borderColor, 2));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(headerBg);
        pnlHeader.setPreferredSize(new Dimension(0, 50));
        pnlHeader.setBorder(new EmptyBorder(0, 20, 0, 15));

        JLabel lblTitleNoti = new JLabel(title);
        lblTitleNoti.setForeground(Color.WHITE);
        lblTitleNoti.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JButton btnCloseNoti = new JButton("×");
        btnCloseNoti.setFont(new Font("Arial", Font.BOLD, 22));
        btnCloseNoti.setForeground(Color.WHITE);
        btnCloseNoti.setBorder(null);
        btnCloseNoti.setContentAreaFilled(false);
        btnCloseNoti.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCloseNoti.addActionListener(e -> dialog.dispose());

        pnlHeader.add(lblTitleNoti, BorderLayout.WEST);
        pnlHeader.add(btnCloseNoti, BorderLayout.EAST);

        JPanel pnlBodyNoti = new JPanel(new BorderLayout());
        pnlBodyNoti.setOpaque(false);
        pnlBodyNoti.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel pnlIconBox = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
            }
        };
        pnlIconBox.setOpaque(false);
        pnlIconBox.setPreferredSize(new Dimension(60, 60));
        
        JLabel lblIcon = new JLabel();
        lblIcon.setIcon(new MenuIcon(iconType));
        lblIcon.setForeground(headerBg); 
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        pnlIconBox.add(lblIcon, BorderLayout.CENTER);

        String formattedMessage = message.replace("\n", "<br>");
        String htmlMsg = "<html><p style='width: 250px; margin: 0; padding: 0; line-height: 1.3;'>" + formattedMessage + "</p></html>";
        
        JLabel lblMessage = new JLabel(htmlMsg);
        lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMessage.setForeground(Color.decode("#374151"));
        lblMessage.setBorder(new EmptyBorder(5, 15, 0, 0)); 

        pnlBodyNoti.add(pnlIconBox, BorderLayout.WEST);
        pnlBodyNoti.add(lblMessage, BorderLayout.CENTER);

        JPanel pnlFooterNoti = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlFooterNoti.setBackground(Color.WHITE);
        pnlFooterNoti.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#DFE3E8")));

        JButton btnOk = new JButton("OK") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(headerBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnOk.setForeground(Color.WHITE);
        btnOk.setIcon(new MenuIcon("CHECK_CIRCLE"));  
        btnOk.setPreferredSize(new Dimension(100, 38));
        btnOk.setContentAreaFilled(false);
        btnOk.setBorderPainted(false);
        btnOk.setFocusPainted(false);
        btnOk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOk.addActionListener(e -> dialog.dispose());

        pnlFooterNoti.add(btnOk);

        mainPanel.add(pnlHeader, BorderLayout.NORTH);
        mainPanel.add(pnlBodyNoti, BorderLayout.CENTER);
        mainPanel.add(pnlFooterNoti, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        
        dialog.pack();
        int safeHeight = Math.max(dialog.getHeight() + 25, 220);
        dialog.setSize(460, safeHeight);
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}