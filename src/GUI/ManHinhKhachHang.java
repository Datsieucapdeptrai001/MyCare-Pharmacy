package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

import Utils.MenuIcon;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import BUS.BUS_KhachHang;

public class ManHinhKhachHang extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;
    
    private JLabel lblTotalKhachHang; 
    private JLabel lblTotalPoints;
    
    // Đã thêm CardLayout để chuyển đổi qua lại giữa Thông tin và Lịch sử
    private CardLayout cardLayoutBody;
    private JPanel pnlCardBody;
    private JLabel lblTabThongTin, lblTabLichSuDiem;
    
    private JLabel lblDiemLichSu, lblGiaTriLichSu;
    private JPanel pnlHistoryList;
    private JPanel pnlDetail;
    private JLabel lblDetAvatar, lblDetName, lblDetId;
    private JLabel lblDetNgayTao, lblDetPhone, lblDetEmail, lblDetAddress; // Đổi lblDetGenderDOB thành lblDetNgayTao
    private JLabel lblDetOrders, lblDetPoints, lblDetTotalSpend, lblDetLastVisit;
    private JButton btnEdit;
    private JButton btnAdd;
    private boolean isStaffRole = false;

    public ManHinhKhachHang() {
        initUI();
    }

    private void initUI() {
        this.setLayout(new BorderLayout(0, 20));
        this.setBackground(Color.decode("#F3F4F6")); 
        this.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        // ==================== 1. PHẦN ĐẦU ====================
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ KHÁCH HÀNG");
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
            loadData(); // Tải lại dữ liệu lên bảng
            
            // Cập nhật lại thanh Detail Sidebar bên phải nếu nó đang được mở
            if (pnlDetail != null && pnlDetail.isVisible()) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    updateDetailSidebar(table.convertRowIndexToModel(row));
                } else {
                    pnlDetail.setVisible(false); // Ẩn đi nếu không chọn dòng nào
                }
            }
        });
        btnAdd = createActionBtn("+ Thêm KH", "#E11D48", null);
        btnAdd.addActionListener(e -> {
            Window p = SwingUtilities.getWindowAncestor(this);
            ThemKhachHang dialog = new ThemKhachHang((Frame) p, model);
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
        
        pnlCards.add(createSummaryCard("Tổng khách hàng", "0", 1)); 
        pnlCards.add(createSummaryCard("Tổng điểm tích lũy", "0", 2));

        JPanel pnlTop = new JPanel(new BorderLayout(0, 20));
        pnlTop.setOpaque(false);
        pnlTop.add(pnlHeader, BorderLayout.NORTH);
        pnlTop.add(pnlCards, BorderLayout.CENTER);

        this.add(pnlTop, BorderLayout.NORTH);

        // ==================== 3. KHU VỰC BẢNG & SIDEBAR ====================
        JPanel pnlCenter = new JPanel(new BorderLayout(20, 0)); 
        pnlCenter.setOpaque(false);

        // -- BẢNG DỮ LIỆU --
        String[] cols = {"Mã KH", "Họ tên", "Điện thoại", "Đơn hàng", "Chi tiêu", "Điểm", "Ngày tạo", "Thao tác"};
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

        table.setDefaultRenderer(Object.class, new CustomerTableRenderer());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8")));
        sp.getVerticalScrollBar().setUI(new Utils.ModernScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new Utils.ModernScrollBarUI());
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

        // --- SỰ KIỆN CLICK NÚT SỬA ---
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                String hoten = model.getValueAt(modelRow, 1).toString();
                String sdt = model.getValueAt(modelRow, 2).toString();
                String ngayTao = model.getValueAt(modelRow, 6).toString(); // Lấy ngày tạo đẩy sang Form Sửa
                
                Window p = SwingUtilities.getWindowAncestor(this);
                // Khởi tạo Form Sửa có áp sẵn Ngày tạo
                ThemKhachHang dialog = new ThemKhachHang((Frame) p, model, modelRow, hoten, sdt, ngayTao);
                dialog.setVisible(true);
                
                updateDetailSidebar(modelRow);
            }
        });

        model.addTableModelListener(e -> {
            if (lblTotalKhachHang != null) lblTotalKhachHang.setText(String.valueOf(model.getRowCount()));
            tinhTongDiemTichLuy(); 
            
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                SwingUtilities.invokeLater(() -> {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        int modelRow = table.convertRowIndexToModel(selectedRow);
                        if (e.getFirstRow() == modelRow && pnlDetail != null && pnlDetail.isVisible()) {
                            updateDetailSidebar(modelRow); 
                        }
                    }
                });
            }
        });

        loadData();
    }

    private void tinhTongDiemTichLuy() {
        long tongDiem = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                String diemStr = model.getValueAt(i, 5).toString().replace(".", "").replace(",", "");
                tongDiem += Long.parseLong(diemStr);
            } catch (Exception ex) {}
        }
        if (lblTotalPoints != null) {
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
            lblTotalPoints.setText(df.format(tongDiem));
        }
    }

    private JPanel createHistoryItem(String maHD, String ngay, String diemCong, String tongDiemLucDo) {
        JPanel pnlRow = new JPanel(new BorderLayout(5, 0)); // Thêm khoảng cách 5px giữa 2 bên
        pnlRow.setBackground(Color.WHITE);
        pnlRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#F1F5F9")));
        pnlRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        pnlRow.setBorder(new EmptyBorder(10, 0, 10, 0));

        // --- BÊN TRÁI: Mã HD & Điểm ---
        JPanel pnlLeft = new JPanel(new GridLayout(2, 1, 0, 4));
        pnlLeft.setBackground(Color.WHITE);
        pnlLeft.setPreferredSize(new Dimension(75, 0)); // Cố định chiều rộng cột trái để nhường chỗ cho bên phải
        
        JLabel lblMaHD = new JLabel(maHD);
        lblMaHD.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Ép nhỏ chữ từ 13 xuống 11
        lblMaHD.setForeground(Color.decode("#1967D2"));
        
        JLabel lblDiemCong = new JLabel(diemCong); 
        lblDiemCong.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Ép nhỏ từ 12 xuống 11

        pnlLeft.add(lblMaHD);
        pnlLeft.add(lblDiemCong);

        // --- BÊN PHẢI: Ngày & Ghi chú ---
        JPanel pnlRight = new JPanel(new GridLayout(2, 1, 0, 4));
        pnlRight.setBackground(Color.WHITE);
        
        JLabel lblNgay = new JLabel(ngay, SwingConstants.RIGHT);
        lblNgay.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Ép nhỏ chữ Ngày từ 12 xuống 10
        lblNgay.setForeground(Color.decode("#9CA3AF"));
        
        String textGhiChu = tongDiemLucDo.isEmpty() ? "" : "→ " + tongDiemLucDo;
        JLabel lblTong = new JLabel(textGhiChu, SwingConstants.RIGHT);
        lblTong.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Ép nhỏ chữ Ghi chú từ 12 xuống 10
        lblTong.setForeground(Color.decode("#6B7280"));
        lblTong.setToolTipText(textGhiChu); // Vẫn giữ Tooltip để lỡ có chữ quá dài khách hàng rê chuột vào xem được

        pnlRight.add(lblNgay);
        pnlRight.add(lblTong);

        pnlRow.add(pnlLeft, BorderLayout.WEST);
        
        // QUAN TRỌNG: Phải dùng CENTER ở đây thì chữ mới tự động co lại cho vừa khung, dùng EAST sẽ sinh ra thanh cuộn
        pnlRow.add(pnlRight, BorderLayout.CENTER); 
        
        return pnlRow;
    }

    private JPanel createDetailSidebar() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setPreferredSize(new Dimension(270, 0)); 
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1));

        // --- HEADER ---
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
        lblDetName = new JLabel("Nguyễn Văn An");
        lblDetName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDetId = new JLabel("KH2024-0001");
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

        // --- TABS (Click để đổi Card) ---
        JPanel pnlTabs = new JPanel(new GridLayout(1, 2));
        pnlTabs.setBackground(Color.WHITE);
        pnlTabs.setPreferredSize(new Dimension(0, 35));
        
        lblTabThongTin = new JLabel("Thông tin", SwingConstants.CENTER);
        lblTabThongTin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTabThongTin.setForeground(Color.decode("#1967D2"));
        lblTabThongTin.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#1967D2")));
        lblTabThongTin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        lblTabLichSuDiem = new JLabel("Lịch sử điểm", SwingConstants.CENTER);
        lblTabLichSuDiem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTabLichSuDiem.setForeground(Color.GRAY);
        lblTabLichSuDiem.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));
        lblTabLichSuDiem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        pnlTabs.add(lblTabThongTin);
        pnlTabs.add(lblTabLichSuDiem);

        // --- CARD LAYOUT BODY (Chứa Thông Tin + Lịch Sử) ---
        cardLayoutBody = new CardLayout();
        pnlCardBody = new JPanel(cardLayoutBody);

        // 1. Giao diện THÔNG TIN
        JPanel pnlInfo = new JPanel();
        pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.Y_AXIS));
        pnlInfo.setBackground(Color.WHITE);

        // Sử dụng GridLayout(0, 1) để tự động co giãn. 
        JPanel pnlInfoList = new JPanel(new GridLayout(0, 1, 0, 10));
        pnlInfoList.setBackground(Color.WHITE);
        pnlInfoList.setBorder(new EmptyBorder(15, 15, 15, 15));
        pnlInfoList.setAlignmentX(Component.LEFT_ALIGNMENT); 

        // Đổi thành Ngày tạo thay vì Giới tính / Ngày sinh
        lblDetNgayTao = new JLabel(" Ngày tạo: 01/01/2024");
        lblDetNgayTao.setIcon(new MenuIcon("CALENDAR")); 
        lblDetPhone = new JLabel(" 0910000000");
        lblDetPhone.setIcon(new MenuIcon("PHONE"));
        
        // Vẫn giữ khởi tạo Email và Address để không vi phạm nguyên tắc "không xóa code cũ" và không lỗi Null
        lblDetEmail = new JLabel(" khachhang@email.com");
        lblDetEmail.setIcon(new MenuIcon("MAIL"));
        lblDetAddress = new JLabel(" TP. Hồ Chí Minh");
        lblDetAddress.setIcon(new MenuIcon("LOCATION"));

        // FIX LỖI KHOẢNG TRẮNG: Chỉ add đúng 2 Label đang thực sự hiển thị vào khung
        for(JLabel l : new JLabel[]{lblDetNgayTao, lblDetPhone}) {
            l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            l.setForeground(Color.decode("#4B5563"));
            l.setIconTextGap(10);
            pnlInfoList.add(l);
        }

        JPanel pnlStats = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlStats.setBackground(Color.WHITE);
        pnlStats.setBorder(new EmptyBorder(0, 15, 15, 15));
        pnlStats.setMaximumSize(new Dimension(1000, 70));
        pnlStats.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel box1 = new JPanel(new GridLayout(2, 1));
        box1.setBackground(Color.decode("#F0F9FF")); 
        box1.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel l1 = new JLabel("Đơn hàng", SwingConstants.CENTER);
        l1.setForeground(Color.GRAY);
        lblDetOrders = new JLabel("0", SwingConstants.CENTER);
        lblDetOrders.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblDetOrders.setForeground(Color.decode("#1967D2"));
        box1.add(l1); box1.add(lblDetOrders);

        JPanel box2 = new JPanel(new GridLayout(2, 1));
        box2.setBackground(Color.decode("#FAF5FF")); 
        box2.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel l2 = new JLabel("Điểm hiện có", SwingConstants.CENTER);
        l2.setForeground(Color.GRAY);
        lblDetPoints = new JLabel("0", SwingConstants.CENTER);
        lblDetPoints.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblDetPoints.setForeground(Color.decode("#9333EA"));
        box2.add(l2); box2.add(lblDetPoints);

        pnlStats.add(box1); pnlStats.add(box2);

        JPanel pnlSpend = new JPanel();
        pnlSpend.setLayout(new BoxLayout(pnlSpend, BoxLayout.Y_AXIS));
        pnlSpend.setBackground(Color.WHITE);
        pnlSpend.setBorder(new EmptyBorder(0, 15, 10, 15)); 
        pnlSpend.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSpendText = new JLabel("Tổng chi tiêu:");
        lblSpendText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSpendText.setForeground(Color.GRAY);
        
        lblDetTotalSpend = new JLabel("0đ");
        lblDetTotalSpend.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDetTotalSpend.setForeground(Color.decode("#16A34A")); 
        lblDetTotalSpend.setBorder(new EmptyBorder(2, 0, 4, 0));
        
        lblDetLastVisit = new JLabel("Lần cuối: -");
        lblDetLastVisit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDetLastVisit.setForeground(Color.GRAY);
        
        pnlSpend.add(lblSpendText);
        pnlSpend.add(lblDetTotalSpend);
        pnlSpend.add(lblDetLastVisit);

        pnlInfo.add(pnlInfoList);
        pnlInfo.add(pnlStats);
        pnlInfo.add(pnlSpend);
        
        // Đã bọc pnlInfo vào BorderLayout.NORTH để chống lỗi JScrollPane tự động kéo giãn nội dung
        JPanel pnlInfoWrapper = new JPanel(new BorderLayout());
        pnlInfoWrapper.setBackground(Color.WHITE);
        pnlInfoWrapper.add(pnlInfo, BorderLayout.NORTH);

        JScrollPane spInfo = new JScrollPane(pnlInfoWrapper);
        spInfo.setBorder(null);
        spInfo.getVerticalScrollBar().setUI(new Utils.ModernScrollBarUI());
        
        // 2. Giao diện LỊCH SỬ ĐIỂM
        JPanel pnlHistoryWrapper = new JPanel(new BorderLayout());
        pnlHistoryWrapper.setBackground(Color.WHITE);
        
        JPanel pnlHisHeader = new JPanel(new GridLayout(2, 1));
        pnlHisHeader.setBackground(Color.decode("#FAF5FF"));
        pnlHisHeader.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        lblDiemLichSu = new JLabel("0 điểm", SwingConstants.CENTER);
        lblDiemLichSu.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDiemLichSu.setForeground(Color.decode("#9333EA"));
        
        lblGiaTriLichSu = new JLabel("≈ 0đ giá trị đổi thưởng", SwingConstants.CENTER);
        lblGiaTriLichSu.setForeground(Color.GRAY);
        
        pnlHisHeader.add(lblDiemLichSu);
        pnlHisHeader.add(lblGiaTriLichSu);
        
        pnlHistoryList = new JPanel();
        pnlHistoryList.setLayout(new BoxLayout(pnlHistoryList, BoxLayout.Y_AXIS));
        pnlHistoryList.setBackground(Color.WHITE);
        pnlHistoryList.setBorder(new EmptyBorder(5, 15, 10, 15));
        
        // Đã bọc pnlHistoryList vào BorderLayout.NORTH để chống lỗi JScrollPane tự động kéo giãn nội dung
        JPanel pnlHistoryListWrapper = new JPanel(new BorderLayout());
        pnlHistoryListWrapper.setBackground(Color.WHITE);
        pnlHistoryListWrapper.add(pnlHistoryList, BorderLayout.NORTH);

        JScrollPane spHistory = new JScrollPane(pnlHistoryListWrapper);
        spHistory.setBorder(null);
        spHistory.getVerticalScrollBar().setUI(new Utils.ModernScrollBarUI());
        
        // --- THÊM DÒNG NÀY VÀO ĐỂ KHÓA THANH CUỘN NGANG ---
        spHistory.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        pnlHistoryWrapper.add(pnlHisHeader, BorderLayout.NORTH);
        pnlHistoryWrapper.add(spHistory, BorderLayout.CENTER);

        // Thêm 2 giao diện vào CardLayout
        pnlCardBody.add(spInfo, "INFO");
        pnlCardBody.add(pnlHistoryWrapper, "HISTORY");

        // Sự kiện chuyển Tab
        lblTabThongTin.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                cardLayoutBody.show(pnlCardBody, "INFO");
                lblTabThongTin.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#1967D2")));
                lblTabThongTin.setForeground(Color.decode("#1967D2"));
                lblTabThongTin.setFont(new Font("Segoe UI", Font.BOLD, 13));

                lblTabLichSuDiem.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));
                lblTabLichSuDiem.setForeground(Color.GRAY);
                lblTabLichSuDiem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
        });

        lblTabLichSuDiem.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                cardLayoutBody.show(pnlCardBody, "HISTORY");
                lblTabLichSuDiem.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.decode("#1967D2")));
                lblTabLichSuDiem.setForeground(Color.decode("#1967D2"));
                lblTabLichSuDiem.setFont(new Font("Segoe UI", Font.BOLD, 13));

                lblTabThongTin.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));
                lblTabThongTin.setForeground(Color.GRAY);
                lblTabThongTin.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
        });

        // --- FOOTER ---
        JPanel pnlFooterActions = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlFooterActions.setBackground(Color.WHITE);
        pnlFooterActions.setBorder(new EmptyBorder(10, 15, 15, 15));

        btnEdit = new JButton("Chỉnh sửa");
        btnEdit.setIcon(new MenuIcon("EDIT"));
        btnEdit.setIconTextGap(8);
        btnEdit.setBackground(Color.decode("#1967D2"));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setFocusPainted(false);
        btnEdit.setBorderPainted(false);
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEdit.setPreferredSize(new Dimension(0, 36));
        btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnDelete = new JButton("Xóa khách hàng");
        btnDelete.setIcon(new MenuIcon("TRASH"));
        btnDelete.setIconTextGap(8);
        btnDelete.setBackground(Color.decode("#FEF2F2")); 
        btnDelete.setForeground(Color.decode("#DC2626"));
        btnDelete.setFocusPainted(false);
        btnDelete.setBorder(BorderFactory.createLineBorder(Color.decode("#FECACA")));
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDelete.setPreferredSize(new Dimension(0, 36));
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnlFooterActions.add(btnEdit);
        pnlFooterActions.add(btnDelete);

        JPanel pnlTopWrap = new JPanel(new BorderLayout());
        pnlTopWrap.add(pnlDetHeader, BorderLayout.NORTH);
        pnlTopWrap.add(pnlTabs, BorderLayout.SOUTH);

        pnl.add(pnlTopWrap, BorderLayout.NORTH);
        pnl.add(pnlCardBody, BorderLayout.CENTER);
        pnl.add(pnlFooterActions, BorderLayout.SOUTH);

        return pnl;
    }

    private void updateDetailSidebar(int modelRow) {
    	String id = model.getValueAt(modelRow, 0).toString().trim(); 
        String name = model.getValueAt(modelRow, 1).toString();
        String phone = model.getValueAt(modelRow, 2).toString();
        String orders = model.getValueAt(modelRow, 3).toString();
        String spend = model.getValueAt(modelRow, 4).toString();
        String points = model.getValueAt(modelRow, 5).toString();
        String ngayTao = model.getValueAt(modelRow, 6).toString(); 

        // 1. Gán dữ liệu như bình thường
        lblDetAvatar.setText(name.substring(0, 1).toUpperCase()); 
        lblDetName.setText(name);
        lblDetId.setText(id);
        lblDetNgayTao.setText(" Ngày tạo: " + ngayTao);
        lblDetPhone.setText(" " + phone);

        // 2. PHẦN QUAN TRỌNG: Đảm bảo Tên và SĐT luôn hiển thị
        lblDetName.setVisible(true);
        lblDetPhone.setVisible(true);
        lblDetAvatar.setVisible(true);
        lblDetId.setVisible(true);

        // 3. CHỈ ẨN RIÊNG EMAIL VÀ ĐỊA CHỈ
        // Tuyệt đối không dùng .getParent().setVisible(false) nữa
        if (lblDetEmail != null) {
            lblDetEmail.setVisible(false); 
        }
        if (lblDetAddress != null) {
            lblDetAddress.setVisible(false);
        }
        
        // Lưu ý: Nếu icon nằm ở một Label riêng (ví dụ lblIconEmail), 
        // bạn cần gọi lblIconEmail.setVisible(false) cho label đó nữa nhé.

        // --- Các phần xử lý lịch sử phía dưới giữ nguyên ---
        lblDetOrders.setText(orders);
        lblDetPoints.setText(points);
        lblDetTotalSpend.setText(spend);
        lblDetLastVisit.setText("Lần cuối: " + (ngayTao.isEmpty() ? "-" : ngayTao)); 

        lblDiemLichSu.setText(points + " điểm");
        try {
            long pts = Long.parseLong(points.replace(".", "").replace(",", ""));
            long giaTri = pts * 100; 
            lblGiaTriLichSu.setText("≈ " + String.format("%,d", giaTri).replace(',', '.') + "đ giá trị đổi thưởng");
        } catch (Exception ex) {}

        pnlHistoryList.removeAll();
        JLabel lblLoading = new JLabel("Đang tải dữ liệu...");
        lblLoading.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblLoading.setForeground(Color.GRAY);
        pnlHistoryList.add(lblLoading);
        pnlHistoryList.revalidate(); 
        pnlHistoryList.repaint();

        SwingWorker<List<String[]>, Void> worker = new SwingWorker<List<String[]>, Void>() {
            @Override
            protected List<String[]> doInBackground() throws Exception {
                BUS_KhachHang bus = new BUS_KhachHang();
                return bus.getLichSuDiem(id);
            }

            @Override
            protected void done() {
                pnlHistoryList.removeAll(); 
                
                try {
                    List<String[]> danhSach = get(); 
                    
                    if (danhSach == null || danhSach.isEmpty()) {
                        JLabel lbl = new JLabel("Chưa có lịch sử điểm nào.");
                        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                        lbl.setForeground(Color.GRAY);
                        lbl.setBorder(new EmptyBorder(10, 0, 0, 0));
                        pnlHistoryList.add(lbl);
                    } else {
                        for (String[] row : danhSach) {
                            boolean laTich = "TICH".equals(row[1]);
                            String diemHienThi = (laTich ? "+" : "-") + row[2] + " điểm";
                            JPanel item = createHistoryItem(row[0], row[4], diemHienThi, row[3]);
                            
                            try {
                                JPanel pnlLeft = (JPanel) item.getComponent(0);
                                JLabel lblDiem = (JLabel) pnlLeft.getComponent(1);
                                lblDiem.setForeground(laTich 
                                    ? Color.decode("#16A34A") 
                                    : Color.decode("#DC2626"));
                                lblDiem.setFont(new Font("Segoe UI", Font.BOLD, 12));
                            } catch (Exception ignored) {}
                            
                            pnlHistoryList.add(item);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JLabel lblErr = new JLabel("Lỗi khi tải lịch sử điểm. Vui lòng xem Console!");
                    lblErr.setForeground(Color.RED);
                    pnlHistoryList.add(lblErr);
                }
                
                // --- FIX 4: Ép Frame cha (Wrapper) phải vẽ lại để đánh thức JScrollPane ---
                pnlHistoryList.revalidate();
                pnlHistoryList.repaint();
                if (pnlHistoryList.getParent() != null) {
                    pnlHistoryList.getParent().revalidate();
                    pnlHistoryList.getParent().repaint();
                }
            }
        };
        worker.execute();
    }

    private JPanel createSummaryCard(String title, String value, int type) {
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

        if (type == 1) lblTotalKhachHang = lblValue; 
        if (type == 2) lblTotalPoints = lblValue; 

        pnl.add(lblTitle);
        pnl.add(lblValue);
        return pnl;
    }

    class CustomerTableRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasF, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasF, r, c);
            if (c == 3 || c == 5 || c == 6 || c == 7) lbl.setHorizontalAlignment(CENTER);
            else lbl.setHorizontalAlignment(LEFT);

            lbl.setOpaque(true);
            lbl.setBackground(isSel ? Color.decode("#E0F2FE") : Color.WHITE);
            lbl.setForeground(Color.decode("#374151")); 
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lbl.setBorder(new EmptyBorder(0, 10, 0, 10)); 
            lbl.setIcon(null);

            if (c == 0) lbl.setForeground(Color.decode("#1967D2")); 
            if (c == 1) lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
            if (c == 4) { lbl.setForeground(Color.decode("#1D4ED8")); lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); }
            if (c == 5) { lbl.setForeground(Color.decode("#9333EA")); lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); }
            if (c == 7) { 
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

 // Sửa 'private' thành 'public'
    public void loadData() {
        model.setRowCount(0);
        DAO.DAO_KhachHang daoKH = new DAO.DAO_KhachHang();
        List<Object[]> dsKhachHang = daoKH.layDanhSachKhachHangChoBang();
        
        for (Object[] row : dsKhachHang) {
            model.addRow(row);
        }
        
        if (lblTotalKhachHang != null) {
            lblTotalKhachHang.setText(String.valueOf(model.getRowCount()));
        }
        tinhTongDiemTichLuy();
    }
    public void moFormThemMoi() {
        Window p = SwingUtilities.getWindowAncestor(this);
        ThemKhachHang dialog = new ThemKhachHang((Frame) p, model);
        dialog.setVisible(true);
    }

    public DefaultTableModel getModel() {
        return model;
    }

    public void setReadOnly(boolean readOnly) {
        this.isStaffRole = readOnly;
        if (!readOnly) return;
        disableButtonsByText(this, "Xóa", "Xóa khách hàng", "Xóa KH", "Delete");
    }

    private void disableButtonsByText(Container container, String... texts) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton) {
                JButton btn = (JButton) c;
                for (String t : texts) {
                    if (t.equalsIgnoreCase(btn.getText())) {
                        btn.setVisible(false);
                        break;
                    }
                }
            } else if (c instanceof Container) {
                disableButtonsByText((Container) c, texts);
            }
        }
    }
}