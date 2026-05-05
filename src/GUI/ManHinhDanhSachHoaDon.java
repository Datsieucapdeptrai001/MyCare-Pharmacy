package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import BUS.BUS_HoaDon;
import Utils.MenuIcon;
import Utils.ModernScrollBarUI;
import Utils.UserSession;

public class ManHinhDanhSachHoaDon extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JPanel pnlCards;
    private JTextField txtSearch;
    private BUS_HoaDon bus_HoaDon;
    private TableRowSorter<DefaultTableModel> rowSorter; 

    public ManHinhDanhSachHoaDon() {
        // XÓA DÒNG NÀY: dao_HoaDon = new DAO_HoaDon();
        
        // THAY BẰNG DÒNG NÀY:
        bus_HoaDon = new BUS_HoaDon(); 
        
        initUI();
        loadData();
    }

    private void initUI() {
        this.setLayout(new BorderLayout(0, 25)); 
        this.setBackground(Color.WHITE);
        // Chỉnh padding top thành 0 để thanh tabs sát lên trên cùng
        this.setBorder(new EmptyBorder(0, 30, 20, 30)); 

        // ==================== 1. THANH TABS (GIỮ NGUYÊN NHƯ BỨC ẢNH) ====================
        JPanel pnlTabs = new JPanel(new BorderLayout());
        pnlTabs.setBackground(Color.WHITE);
        pnlTabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));

        JPanel pnlLeftTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlLeftTabs.setOpaque(false);

        // Nút Bán Hàng (Trạng thái tắt)
        JLabel lblBanHang = new JLabel("Bán hàng");
        lblBanHang.setIcon(new MenuIcon("CART"));
        lblBanHang.setIconTextGap(8);
        lblBanHang.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblBanHang.setForeground(Color.decode("#6C757D"));
        lblBanHang.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 40));
        lblBanHang.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblBanHang.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) { // ĐỔI SANG MOUSE PRESSED
            	chuyenManHinh("Bán hàng & Đổi trả");
            }
        });

        // Nút Đổi / Trả Hàng (Trạng thái tắt)
        JLabel lblDoiTra = new JLabel("Đổi / Trả hàng");
        lblDoiTra.setIcon(new MenuIcon("BOX"));
        lblDoiTra.setIconTextGap(8);
        lblDoiTra.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblDoiTra.setForeground(Color.decode("#6C757D"));
        lblDoiTra.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 40));
        lblDoiTra.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblDoiTra.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) { // ĐỔI SANG MOUSE PRESSED
                chuyenManHinh("DoiTra");
            }
        });
        // Nút Danh Sách Hóa Đơn (TRẠNG THÁI BẬT SÁNG XANH)
        JLabel lblDanhSachHD = new JLabel("Danh Sách Hóa Đơn");
        lblDanhSachHD.setIcon(new MenuIcon("LIST")); 
        lblDanhSachHD.setIconTextGap(8);
        lblDanhSachHD.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDanhSachHD.setForeground(Color.decode("#1967D2"));
        lblDanhSachHD.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, Color.decode("#1967D2")),
                BorderFactory.createEmptyBorder(20, 0, 20, 40)));

        pnlLeftTabs.add(lblBanHang);
        pnlLeftTabs.add(lblDoiTra);
        pnlLeftTabs.add(lblDanhSachHD);
        pnlTabs.add(pnlLeftTabs, BorderLayout.WEST);

        // ==================== 2. THỂ THỐNG KÊ ====================
        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.setBackground(Color.WHITE);
        
        pnlNorth.add(pnlTabs, BorderLayout.NORTH); // Gắn thanh Tab lên trên cùng

        pnlCards = new JPanel(new GridLayout(1, 4, 25, 0)); 
        pnlCards.setBackground(Color.WHITE);
        pnlCards.setBorder(new EmptyBorder(20, 0, 10, 0)); 
        pnlNorth.add(pnlCards, BorderLayout.CENTER); // Gắn thẻ thống kê dưới Tab
        
        this.add(pnlNorth, BorderLayout.NORTH);

        // ==================== 3. PHẦN GIỮA: TÌM KIẾM VÀ BẢNG ====================
        JPanel pnlContent = new JPanel(new BorderLayout(0, 15));
        pnlContent.setBackground(Color.WHITE);

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlSearch.setBackground(Color.WHITE);
        
        JPanel pnlSearchWrapper = new JPanel(new BorderLayout(10, 0));
        pnlSearchWrapper.setBackground(Color.WHITE);
        pnlSearchWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        txtSearch = new JTextField(30);
        txtSearch.setBorder(null);
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm mã hóa đơn, tên khách hàng...");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { search(); }
            public void removeUpdate(DocumentEvent e) { search(); }
            public void changedUpdate(DocumentEvent e) { search(); }
        });
        
        pnlSearchWrapper.add(new JLabel(new MenuIcon("SEARCH")), BorderLayout.WEST);
        pnlSearchWrapper.add(txtSearch, BorderLayout.CENTER);
        pnlSearch.add(pnlSearchWrapper);
        
        pnlContent.add(pnlSearch, BorderLayout.NORTH);

        // BẢNG HÓA ĐƠN
        String[] cols = {"Mã HD", "Ngày lập", "Khách hàng", "SĐT", "Thanh toán", "Tổng tiền", "Trạng thái"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        
        rowSorter = new TableRowSorter<>(model);
        table.setRowSorter(rowSorter);
        
        table.setRowHeight(45); 
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(Color.decode("#F9FAFB"));
        table.getTableHeader().setPreferredSize(new Dimension(100, 45));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        table.setDefaultRenderer(Object.class, new ModernTableRenderer()); // Đảm bảo class này đã có dưới đáy file
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.decode("#E5E7EB")));
        scroll.getViewport().setBackground(Color.WHITE);
        
        scroll.getVerticalScrollBar().setUI(new ModernScrollBarUI()); 
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0)); 
        
        pnlContent.add(scroll, BorderLayout.CENTER);

        this.add(pnlContent, BorderLayout.CENTER);
    }
    public void refreshData() {
        model.setRowCount(0); // Xóa dữ liệu cũ trên bảng
        loadData();           // Gọi hàm loadData đã có của bạn để đổ lại dữ liệu mới nhất
    }
    public void loadData() {
        model.setRowCount(0);
        boolean isAdmin = UserSession.getInstance().isAdmin();
        List<Object[]> ds;
        
        if (isAdmin) {
            // SỬA dao_HoaDon THÀNH bus_HoaDon
            ds = bus_HoaDon.layDanhSachHoaDonChoBang();
        } else {
            String maNV = UserSession.getInstance().getMaNhanVien(); 
            // SỬA dao_HoaDon THÀNH bus_HoaDon
            ds = bus_HoaDon.layDanhSachHoaDonCuaNhanVien(maNV);
        }
        
        int countSuccess = 0, countPending = 0, countCancel = 0, countReturn = 0;

        for (Object[] row : ds) {
            model.addRow(row);
            if(row.length > 6 && row[6] != null) {
                String status = row[6].toString();
                if (status.equals("Hoàn thành")) countSuccess++;
                else if (status.equals("Đang xử lý")) countPending++;
                else if (status.equals("Đã hủy")) countCancel++;
                else if (status.equals("Đổi trả")) countReturn++;
            }
        }

        pnlCards.removeAll(); 
        pnlCards.add(createSummaryCard("HOÀN THÀNH", countSuccess, "#10B981", "CHECK_CIRCLE")); 
        pnlCards.add(createSummaryCard("ĐANG XỬ LÝ", countPending, "#F59E0B", "CLOCK")); 
        pnlCards.add(createSummaryCard("ĐÃ HỦY", countCancel, "#EF4444", "CANCEL"));      
        pnlCards.add(createSummaryCard("ĐỔI TRẢ", countReturn, "#8B5CF6", "BOX"));     
        
        pnlCards.revalidate();
        pnlCards.repaint();
    }

    private JPanel createSummaryCard(String title, int value, String hexColor, String iconType) {
        Color bgColor = Color.decode(hexColor);
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); 
                g2.dispose();
            }
        };
        card.setOpaque(false); 
        card.setBorder(new EmptyBorder(18, 25, 18, 25)); 
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(new Color(255, 255, 255, 200)); 
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JLabel lblValue = new JLabel(String.valueOf(value));
        lblValue.setForeground(Color.WHITE);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 36));
        
        JPanel pnlText = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlText.setOpaque(false);
        pnlText.add(lblTitle);
        pnlText.add(lblValue);
        
        // GẮN ICON LỚN VÀO GÓC PHẢI THẺ
        JLabel lblIcon = new JLabel(new MenuIcon(iconType));
        lblIcon.setForeground(new Color(255, 255, 255, 150)); // Cho icon mờ nhẹ
        
        card.add(pnlText, BorderLayout.CENTER);
        card.add(lblIcon, BorderLayout.EAST);
        return card;
    }

    // --- HÀM TẠO THANH TABS ---
    private JPanel createTabs() {
        JPanel pnlTabs = new JPanel(new BorderLayout());
        pnlTabs.setBackground(Color.WHITE);
        pnlTabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));

        JPanel pnlLeftTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlLeftTabs.setOpaque(false);

     // Nút Bán Hàng (Trạng thái tắt)
        JLabel lblBanHang = new JLabel("Bán hàng");
        lblBanHang.setIcon(new MenuIcon("CART"));
        lblBanHang.setIconTextGap(8);
        lblBanHang.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblBanHang.setForeground(Color.decode("#6C757D"));
        lblBanHang.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 40));
        lblBanHang.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblBanHang.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) { // Đổi sang mousePressed cho cực nhạy
                Container parent = ManHinhDanhSachHoaDon.this.getParent();
                while (parent != null && !(parent.getLayout() instanceof CardLayout)) {
                    parent = parent.getParent();
                }
                if (parent != null) {
                    CardLayout cl = (CardLayout) parent.getLayout();
                    cl.show(parent, "BanHang"); // Đảm bảo "BanHang" khớp với MainDashboard
                } else {
                    JOptionPane.showMessageDialog(ManHinhDanhSachHoaDon.this, "Lỗi: Không tìm thấy màn hình gốc!");
                }
            }
        });

        // Nút Đổi / Trả Hàng (Trạng thái tắt)
        JLabel lblDoiTra = new JLabel("Đổi / Trả hàng");
        lblDoiTra.setIcon(new MenuIcon("BOX"));
        lblDoiTra.setIconTextGap(8);
        lblDoiTra.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblDoiTra.setForeground(Color.decode("#6C757D"));
        lblDoiTra.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 40));
        lblDoiTra.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblDoiTra.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) { // Đổi sang mousePressed cho cực nhạy
                Container parent = ManHinhDanhSachHoaDon.this.getParent();
                while (parent != null && !(parent.getLayout() instanceof CardLayout)) {
                    parent = parent.getParent();
                }
                if (parent != null) {
                    CardLayout cl = (CardLayout) parent.getLayout();
                    cl.show(parent, "DoiTra"); // Đảm bảo "DoiTra" khớp với MainDashboard
                }
            }
        });

        // 3. Tab Danh Sách Hóa Đơn (ĐANG ACTIVE - KHÔNG CLICK)
        JLabel lblDanhSachHD = new JLabel("Danh Sách Hóa Đơn");
        lblDanhSachHD.setIcon(new MenuIcon("LIST")); 
        lblDanhSachHD.setIconTextGap(8);
        lblDanhSachHD.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDanhSachHD.setForeground(Color.decode("#1967D2"));
        lblDanhSachHD.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, Color.decode("#1967D2")),
                BorderFactory.createEmptyBorder(20, 0, 20, 40)));

        return pnlTabs;
    }
 // --- HÀM CHUYỂN MÀN HÌNH ĐA NĂNG (CHỐNG LIỆT NÚT, GIỮ CHUẨN GIAO DIỆN) ---
    private void chuyenManHinh(String tenManHinh) {
        SwingUtilities.invokeLater(() -> {
            Container parent = this.getParent();
            while (parent != null && !(parent.getLayout() instanceof CardLayout)) {
                parent = parent.getParent();
            }
            
            if (parent != null) {
                CardLayout cl = (CardLayout) parent.getLayout();
                cl.show(parent, tenManHinh);
                
                // --- THÊM ĐOẠN NÀY ĐỂ ĐỒNG BỘ VỚI MENU BÊN TRÁI ---
                Container topLevel = parent.getParent();
                while (topLevel != null && !(topLevel instanceof MainDashboard)) {
                    topLevel = topLevel.getParent();
                }
                if (topLevel instanceof MainDashboard) {
                    // Nếu chuyển về Bán hàng thì làm sáng nút "Bán hàng & Đổi trả" trên Sidebar
                    if (tenManHinh.equals("Bán hàng & Đổi trả") || 
                        tenManHinh.equals("DanhSachHD") || 
                        tenManHinh.equals("DoiTra")) {
                        ((MainDashboard) topLevel).chuyenSangTabBanHang(); // Ông thêm hàm này ở Bước 4
                    }
                }
            }
        });
    }
    private void search() {
        String text = txtSearch.getText().trim();
        if (text.length() == 0) {
            rowSorter.setRowFilter(null); 
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text)); 
        }
    }
    private JPanel createSummaryCard(String title, int value, String hexColor) {
        Color bgColor = Color.decode(hexColor);
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); 
                g2.dispose();
            }
        };
        card.setOpaque(false); 
        card.setBorder(new EmptyBorder(18, 25, 18, 25)); 
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(new Color(255, 255, 255, 200)); 
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JLabel lblValue = new JLabel(String.valueOf(value));
        lblValue.setForeground(Color.WHITE);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 36));
        
        JPanel pnlText = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlText.setOpaque(false);
        pnlText.add(lblTitle);
        pnlText.add(lblValue);
        
        card.add(pnlText, BorderLayout.CENTER);
        return card;
    }

    class ModernTableRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSel, boolean hasFocus, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, value, isSel, hasFocus, r, c);
            JLabel lbl = (JLabel) comp;
            lbl.setBorder(new EmptyBorder(0, 10, 0, 10)); 
            
            if (c == 6) { 
                String status = value != null ? value.toString() : "";
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                
                if (status.equals("Hoàn thành")) {
                    lbl.setBackground(Color.decode("#D1FAE5")); 
                    lbl.setForeground(Color.decode("#065F46")); 
                } else if (status.equals("Đang xử lý")) {
                    lbl.setBackground(Color.decode("#FEF3C7")); 
                    lbl.setForeground(Color.decode("#92400E")); 
                } else if (status.equals("Đã hủy") || status.equals("Từ chối")) {
                    lbl.setBackground(Color.decode("#FEE2E2")); 
                    lbl.setForeground(Color.decode("#991B1B"));
                } else if (status.equals("Đổi trả")) {
                    lbl.setBackground(Color.decode("#E0E7FF")); 
                    lbl.setForeground(Color.decode("#3730A3"));
                } else {
                    lbl.setBackground(Color.WHITE);
                    lbl.setForeground(Color.BLACK);
                }
                lbl.setOpaque(true);
            } 
            else {
                lbl.setBackground(isSel ? Color.decode("#F8F9FA") : Color.WHITE);
                lbl.setForeground(Color.decode("#212B36"));
                lbl.setHorizontalAlignment(c == 5 ? SwingConstants.RIGHT : SwingConstants.LEFT); 
                if (c == 0) lbl.setForeground(Color.decode("#1967D2")); 
                lbl.setOpaque(true);
            }
            return lbl;
        }
    }
    public void setReadOnly(boolean readOnly) {
        loadData(); 
    }
}