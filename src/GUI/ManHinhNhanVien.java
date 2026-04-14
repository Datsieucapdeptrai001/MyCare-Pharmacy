package GUI;

import Utils.MenuIcon;
import ConnectDB.ConnectDB; 

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// ====================================================================================
// 1. CLASS CHÍNH: MÀN HÌNH NHÂN VIÊN
// ====================================================================================
public class ManHinhNhanVien extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;
    private JLabel lblTotalNhanVien, lblDangLamViec; 

    // Sidebar variables
    private JPanel pnlDetail;
    private JLabel lblDetAvatar, lblDetName, lblDetId;
    private JLabel lblDetPhone, lblDetEmail, lblDetAddress;
    private JLabel lblDetRole, lblDetCCHN, lblDetStatus;
    private JButton btnEdit;
    private JButton btnDelete; // Sửa thành biến toàn cục để thêm sự kiện
    private JButton btnAdd;

    public ManHinhNhanVien() {
        initUI();
    }

    private void initUI() {
        this.setLayout(new BorderLayout(0, 20));
        this.setBackground(Color.decode("#F3F4F6")); 
        this.setBorder(new EmptyBorder(20, 25, 20, 25));

        // ==================== 1. PHẦN ĐẦU ====================
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
            loadDataFromDatabase(); 
        });

        btnAdd = createActionBtn("+ Thêm NV", "#DC2626", null);
        btnAdd.addActionListener(e -> {
            Window p = SwingUtilities.getWindowAncestor(this);
            DialogThemNhanVien dialog = new DialogThemNhanVien((Frame) p, this, -1, null, null, null, null, null, null);
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

        String[] cols = {"Mã NV", "Họ tên", "Số CCHN", "Điện thoại", "Email", "Chức vụ", "Trạng thái", "Thao tác"};
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
        table.getColumnModel().getColumn(4).setPreferredWidth(180); // Email rộng hơn
        table.getColumnModel().getColumn(7).setPreferredWidth(60);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8")));
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));

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
                String hoten = model.getValueAt(modelRow, 1).toString();
                String cchn = model.getValueAt(modelRow, 2).toString();
                String sdt = model.getValueAt(modelRow, 3).toString();
                String email = model.getValueAt(modelRow, 4).toString();
                String chucVu = model.getValueAt(modelRow, 5).toString();
                String trangThai = model.getValueAt(modelRow, 6).toString();
                
                Window p = SwingUtilities.getWindowAncestor(this);
                DialogThemNhanVien dialog = new DialogThemNhanVien((Frame) p, this, modelRow, hoten, cchn, sdt, email, chucVu, trangThai);
                dialog.setVisible(true);
            }
        });

        // Lấy dữ liệu từ DB ngay khi khởi tạo
        loadDataFromDatabase();
    }

    // ==================== HÀM LẤY DỮ LIỆU TỪ DATABASE ====================
    public void loadDataFromDatabase() {
        model.setRowCount(0); 
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            if (con == null) return;
            
            // Dựa vào DB bạn cung cấp
            String sql = "SELECT id, hoVaTen, soChungChiHanhNghe, sdt, email, chucVu, trangThaiLamViec FROM NhanVien"; 
            PreparedStatement stmt = con.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String id = rs.getString("id");
                String hoTen = rs.getString("hoVaTen");
                String cchn = rs.getString("soChungChiHanhNghe");
                String sdt = rs.getString("sdt");
                String email = rs.getString("email");
                String chucVuDB = rs.getString("chucVu");
                String trangThaiDB = rs.getString("trangThaiLamViec");
                
                // MAP DỮ LIỆU DB SANG UI TIẾNG VIỆT
                String chucVuUI = "Chưa rõ";
                if ("QUAN_LY".equals(chucVuDB)) chucVuUI = "Quản lý";
                else if ("DUOC_SI".equals(chucVuDB)) chucVuUI = "Dược sĩ";

                String trangThaiUI = "Chưa rõ";
                if ("DANG_LAM_VIEC".equals(trangThaiDB)) trangThaiUI = "Đang làm việc";
                else if ("NGHI_PHEP".equals(trangThaiDB)) trangThaiUI = "Nghỉ phép";
                else if ("DA_NGHI_VIEC".equals(trangThaiDB)) trangThaiUI = "Đã nghỉ việc";

                if (cchn == null) cchn = "";
                if (email == null) email = "";
                
                model.addRow(new Object[]{id, hoTen, cchn, sdt, email, chucVuUI, trangThaiUI, ""});
            }
            updateStats();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu từ CSDL: " + e.getMessage());
        }
    }

    // ==================== CÁC HÀM TIỆN ÍCH & SIDEBAR ====================
    public void updateStats() {
        if (lblTotalNhanVien != null) lblTotalNhanVien.setText(String.valueOf(model.getRowCount()));
        if (lblDangLamViec != null) {
            int count = 0;
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 6).toString().equalsIgnoreCase("Đang làm việc")) count++;
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

        JPanel pnlInfoList = new JPanel(new GridLayout(3, 1, 0, 10)); // Chỉ còn SĐT, Email, Address
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

        // Hộp thống kê (Chức vụ & CCHN)
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
        
        JScrollPane spBody = new JScrollPane(pnlBody);
        spBody.setBorder(null);

        // FOOTER ACTIONS
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
        btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnDelete = new JButton("Xóa nhân viên");
        btnDelete.setIcon(new MenuIcon("TRASH"));
        btnDelete.setIconTextGap(8);
        btnDelete.setBackground(Color.decode("#FEF2F2")); 
        btnDelete.setForeground(Color.decode("#DC2626"));
        btnDelete.setFocusPainted(false);
        btnDelete.setBorder(BorderFactory.createLineBorder(Color.decode("#FECACA")));
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // THÊM SỰ KIỆN XÓA (CẬP NHẬT TRẠNG THÁI NGHỈ VIỆC)
        btnDelete.addActionListener(e -> {
            // Chặn từ server-side: STAFF không được xóa dù nút vô tình hiển thị
            if (!Utils.UserSession.getInstance().isAdmin()) {
                JOptionPane.showMessageDialog(pnlDetail,
                    "Bạn không có quyền thực hiện thao tác này.",
                    "Từ chối", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = table.getSelectedRow();
            if (row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                String id = model.getValueAt(modelRow, 0).toString();
                String name = model.getValueAt(modelRow, 1).toString();

                int confirm = JOptionPane.showConfirmDialog(pnlDetail,
                    "Bạn có chắc chắn muốn chuyển nhân viên [" + name + "] sang trạng thái Đã nghỉ việc?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        Connection con = ConnectDB.getInstance().getConnection();
                        String sql = "UPDATE NhanVien SET trangThaiLamViec='DA_NGHI_VIEC' WHERE id=?";
                        PreparedStatement stmt = con.prepareStatement(sql);
                        stmt.setString(1, id);
                        int affected = stmt.executeUpdate();
                        if (affected > 0) {
                            JOptionPane.showMessageDialog(pnlDetail, "Đã chuyển sang trạng thái Đã nghỉ việc.");
                            loadDataFromDatabase();
                            pnlDetail.setVisible(false);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(pnlDetail, "Lỗi khi cập nhật CSDL: " + ex.getMessage());
                    }
                }
            }
        });

        pnlFooterActions.add(btnEdit);
        pnlFooterActions.add(btnDelete);

        JPanel pnlTopWrap = new JPanel(new BorderLayout());
        pnlTopWrap.add(pnlDetHeader, BorderLayout.NORTH);
        pnlTopWrap.add(pnlTabs, BorderLayout.SOUTH);

        pnl.add(pnlTopWrap, BorderLayout.NORTH);
        pnl.add(spBody, BorderLayout.CENTER);
        pnl.add(pnlFooterActions, BorderLayout.SOUTH);

        return pnl;
    }

    private void updateDetailSidebar(int modelRow) {
        String id = model.getValueAt(modelRow, 0).toString();
        String name = model.getValueAt(modelRow, 1).toString();
        String cchn = model.getValueAt(modelRow, 2).toString();
        String phone = model.getValueAt(modelRow, 3).toString();
        String email = model.getValueAt(modelRow, 4).toString();
        String role = model.getValueAt(modelRow, 5).toString();
        String status = model.getValueAt(modelRow, 6).toString();

        lblDetAvatar.setText(name.substring(0, 1).toUpperCase()); 
        lblDetName.setText(name);
        lblDetId.setText(id);

        lblDetPhone.setText(" " + phone);
        lblDetEmail.setText(" " + (email.isEmpty() ? "Chưa cập nhật" : email));

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
            if (c == 5 || c == 6 || c == 7) lbl.setHorizontalAlignment(CENTER);
            else lbl.setHorizontalAlignment(LEFT);

            lbl.setOpaque(true);
            lbl.setBackground(isSel ? Color.decode("#E0F2FE") : Color.WHITE);
            lbl.setForeground(Color.decode("#374151")); 
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lbl.setBorder(new EmptyBorder(0, 10, 0, 10)); 
            lbl.setIcon(null);

            if (c == 0) lbl.setForeground(Color.decode("#1967D2")); 
            if (c == 1) lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
            if (c == 5) { lbl.setForeground(Color.decode("#9333EA")); lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); }
            if (c == 6) { 
                String st = lbl.getText();
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                if(st.equals("Đang làm việc")) lbl.setForeground(Color.decode("#16A34A"));
                else if(st.equals("Nghỉ phép")) lbl.setForeground(Color.decode("#D97706"));
                else lbl.setForeground(Color.decode("#DC2626"));
            }
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

	public void setReadOnly(boolean b) {
		// TODO Auto-generated method stub
		
	}
}

// ====================================================================================
// 2. CLASS PHỤ: DIALOG THÊM / SỬA NHÂN VIÊN TƯƠNG TÁC DB
// ====================================================================================
class DialogThemNhanVien extends JDialog {

    private JTextField txtHoTen, txtCCHN, txtSdt, txtEmail;
    private JComboBox<String> cboChucVu, cboTrangThai;
    private ManHinhNhanVien parentScreen;
    
    private JLabel lblTitle;
    private JButton btnThem;
    private int editRow;
	private JComponent btnDelete;
	private Object btnEdit; 

    public DialogThemNhanVien(Frame parent, ManHinhNhanVien parentScreen, int editRow, String hoten, String cchn, String sdt, String email, String chucVu, String trangThai) {
        super(parent, editRow == -1 ? "Thêm nhân viên mới" : "Chỉnh sửa nhân viên", true);
        this.parentScreen = parentScreen;
        this.editRow = editRow;
        initUI(parent);

        if (editRow != -1) {
            lblTitle.setText("Chỉnh sửa nhân viên");
            btnThem.setText("✓ Lưu thay đổi");
            
            txtHoTen.setText(hoten); txtHoTen.setForeground(Color.BLACK);
            txtCCHN.setText(cchn); txtCCHN.setForeground(Color.BLACK);
            txtSdt.setText(sdt); txtSdt.setForeground(Color.BLACK);
            txtEmail.setText(email); txtEmail.setForeground(Color.BLACK);
            
            cboChucVu.setSelectedItem(chucVu);
            cboTrangThai.setSelectedItem(trangThai);
        }
    }

    private void initUI(Frame parent) {
        setSize(550, 460); // Đã rút gọn vì bớt control
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
        pnlBody.setBorder(new EmptyBorder(10, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5); 

        // Dòng 1: Họ tên
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        pnlBody.add(createLabel("Họ và tên", true), gbc);
        gbc.gridy = 1;
        txtHoTen = createTextField("Nhập họ và tên đầy đủ");
        pnlBody.add(txtHoTen, gbc);

        // Dòng 2: SĐT, Email
        gbc.gridwidth = 1; gbc.weightx = 0.5;
        gbc.gridx = 0; gbc.gridy = 2;
        pnlBody.add(createLabel("Số điện thoại", true), gbc);
        gbc.gridx = 1;
        pnlBody.add(createLabel("Email", false), gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        txtSdt = createTextField("0912345678");
        pnlBody.add(txtSdt, gbc);
        gbc.gridx = 1;
        txtEmail = createTextField("email@mycare.vn");
        pnlBody.add(txtEmail, gbc);

        // Dòng 3: Số CCHN, Chức vụ
        gbc.gridx = 0; gbc.gridy = 4;
        pnlBody.add(createLabel("Số CCHN", false), gbc);
        gbc.gridx = 1;
        pnlBody.add(createLabel("Chức vụ", true), gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        txtCCHN = createTextField("CCHN-xxxx");
        pnlBody.add(txtCCHN, gbc);
        
        gbc.gridx = 1;
        cboChucVu = createComboBox(new String[]{"Quản lý", "Dược sĩ"});
        pnlBody.add(cboChucVu, gbc);

        // Dòng 4: Trạng thái (CHỈ CÒN ĐANG LÀM VIỆC VÀ NGHỈ PHÉP)
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        pnlBody.add(createLabel("Trạng thái", true), gbc);

        gbc.gridy = 7;
        cboTrangThai = createComboBox(new String[]{"Đang làm việc", "Nghỉ phép"});
        pnlBody.add(cboTrangThai, gbc);

        add(pnlBody, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#DFE3E8")));

        JButton btnHuy = new JButton("× Hủy");
        btnHuy.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnHuy.setBackground(Color.WHITE);
        btnHuy.setForeground(Color.decode("#374151"));
        btnHuy.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        btnHuy.setFocusPainted(false);
        btnHuy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHuy.addActionListener(e -> dispose());

        btnThem = new JButton(editRow != -1 ? "Lưu thay đổi" : "Thêm nhân viên");
        btnThem.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnThem.setBackground(Color.decode("#DC2626")); 
        btnThem.setForeground(Color.WHITE);
        btnThem.setIcon(new MenuIcon(editRow != -1 ? "SAVE" : "USER_ADD"));
        btnThem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DC2626"), 1, true),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        btnThem.setFocusPainted(false);
        btnThem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // ================= THỰC THI SQL INSERT/UPDATE =================
        btnThem.addActionListener(e -> {
            String hoten = txtHoTen.getText().trim();
            String cchn = txtCCHN.getText().trim();
            String sdt = txtSdt.getText().trim();
            String email = txtEmail.getText().trim();
            String chucVuUI = cboChucVu.getSelectedItem().toString();
            String trangThaiUI = cboTrangThai.getSelectedItem().toString();
            
            if(hoten.isEmpty() || hoten.equals("Nhập họ và tên đầy đủ") || sdt.isEmpty() || sdt.equals("0912345678")) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Họ tên và Số điện thoại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if(email.equals("email@mycare.vn")) email = "";
            if(cchn.equals("CCHN-xxxx")) cchn = "";

            // MAP UI SANG DATABASE
            String dbChucVu = chucVuUI.equals("Quản lý") ? "QUAN_LY" : "DUOC_SI";
            String dbTrangThai = trangThaiUI.equals("Đang làm việc") ? "DANG_LAM_VIEC" : "NGHI_PHEP";

            DefaultTableModel mainModel = parentScreen.getModel();

            try {
                Connection con = ConnectDB.getInstance().getConnection();
                
                if (editRow != -1) {
                    // --- CẬP NHẬT (UPDATE) ---
                    String id = mainModel.getValueAt(editRow, 0).toString();
                    
                    String sql = "UPDATE NhanVien SET hoVaTen=?, soChungChiHanhNghe=?, sdt=?, email=?, chucVu=?, trangThaiLamViec=? WHERE id=?";
                    PreparedStatement stmt = con.prepareStatement(sql);
                    stmt.setString(1, hoten);
                    stmt.setString(2, cchn);
                    stmt.setString(3, sdt);
                    stmt.setString(4, email);
                    stmt.setString(5, dbChucVu);
                    stmt.setString(6, dbTrangThai);
                    stmt.setString(7, id);
                    stmt.executeUpdate();

                    // Cập nhật lại UI Model
                    mainModel.setValueAt(hoten, editRow, 1);
                    mainModel.setValueAt(cchn, editRow, 2);
                    mainModel.setValueAt(sdt, editRow, 3);
                    mainModel.setValueAt(email, editRow, 4);
                    mainModel.setValueAt(chucVuUI, editRow, 5);
                    mainModel.setValueAt(trangThaiUI, editRow, 6);
                    
                    parentScreen.updateStats();
                    parentScreen.refreshSidebarIfVisible(editRow);
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                } else {
                    // --- THÊM MỚI (INSERT) ---
                    String idMoi = "NV" + String.format("%03d", System.currentTimeMillis() % 1000); 
                    
                    String sql = "INSERT INTO NhanVien (id, hoVaTen, soChungChiHanhNghe, sdt, email, chucVu, trangThaiLamViec) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement stmt = con.prepareStatement(sql);
                    stmt.setString(1, idMoi);
                    stmt.setString(2, hoten);
                    stmt.setString(3, cchn);
                    stmt.setString(4, sdt);
                    stmt.setString(5, email);
                    stmt.setString(6, dbChucVu);
                    stmt.setString(7, dbTrangThai);
                    stmt.executeUpdate();

                    // Cập nhật lại UI Model
                    mainModel.addRow(new Object[]{ idMoi, hoten, cchn, sdt, email, chucVuUI, trangThaiUI, "" });
                    parentScreen.updateStats();
                    JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!");
                }
                dispose(); 
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu vào Database: " + ex.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
            }
        });

        pnlFooter.add(btnHuy);
        pnlFooter.add(btnThem);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text, boolean isRequired) {
        String html = "<html><span style='color:#374151; font-family:Segoe UI; font-size:13px; font-weight:bold;'>" + text + "</span>";
        if (isRequired) html += " <span style='color:#DC2626;'>*</span>";
        html += "</html>";
        return new JLabel(html);
    }

    private JTextField createTextField(String placeholder) {
        JTextField txt = new JTextField(placeholder);
        txt.setPreferredSize(new Dimension(0, 36));
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setForeground(Color.GRAY);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        txt.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txt.getText().equals(placeholder)) { txt.setText(""); txt.setForeground(Color.BLACK); }
            }
            public void focusLost(FocusEvent e) {
                if (txt.getText().isEmpty()) { txt.setForeground(Color.GRAY); txt.setText(placeholder); }
            }
        });
        return txt;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> cbo = new JComboBox<>(items);
        cbo.setBackground(Color.WHITE);
        cbo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbo.setPreferredSize(new Dimension(0, 36));
        return cbo;
    }
    /**
     * Phân quyền: Ẩn nút Thêm/Sửa/Xóa cho STAFF.
     */
    public void setReadOnly(boolean readOnly) {
        if (!readOnly) return;
        // Ẩn trực tiếp các nút có reference toàn cục
        if (btnThem   != null) btnThem.setVisible(false);
        if (btnDelete != null) btnDelete.setVisible(false);   // "Xóa nhân viên" – phải ẩn tường minh
        if (btnEdit   != null) btnEdit.setVisible(false);
        // Duyệt đệ quy tìm nút còn sót theo text
        disableButtonsByText(this, "Thêm mới", "Nhập Excel", "Thêm", "Xóa", "Xóa nhân viên", "Sửa", "Lưu", "Chỉnh sửa");
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