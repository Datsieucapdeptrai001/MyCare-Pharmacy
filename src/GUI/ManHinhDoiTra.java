package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

import Utils.MenuIcon;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ManHinhDoiTra extends JPanel {
	
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton[] statusBtns;
    private String filterStatus = "Tất cả";
    private JTextField txtSearch;
    
    public ManHinhDoiTra() {
        initUI();
    }

    private void initUI() {
        this.setLayout(new BorderLayout(0, 0));
        this.setBackground(Color.WHITE);
        this.setBorder(new EmptyBorder(0, 30, 20, 30));

        // ==================== 1. THANH TABS ====================
        JPanel pnlTabs = new JPanel(new BorderLayout());
        pnlTabs.setBackground(Color.WHITE);
        pnlTabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));

        JPanel pnlLeftTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlLeftTabs.setOpaque(false);

        // --- Tab Bán hàng (Để quay lại) ---
        JLabel lblBanHang = new JLabel("Bán hàng");
        lblBanHang.setIcon(new MenuIcon("CART"));
        lblBanHang.setIconTextGap(8);
        lblBanHang.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblBanHang.setForeground(Color.decode("#6C757D"));
        lblBanHang.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 40));
        lblBanHang.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        lblBanHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Container parent = ManHinhDoiTra.this.getParent();
                if (parent != null && parent.getLayout() instanceof CardLayout) {
                    parent.add(new ManHinhBanHang(), "BanHang"); 
                    CardLayout cl = (CardLayout) parent.getLayout();
                    cl.show(parent, "BanHang");
                }
            }
        });

        // --- Tab Đổi / Trả hàng (Tab đang chọn - Màu đỏ) ---
        JLabel lblDoiTra = new JLabel("Đổi / Trả hàng");
        lblDoiTra.setIcon(new MenuIcon("BOX"));
        lblDoiTra.setIconTextGap(8);
        lblDoiTra.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDoiTra.setForeground(Color.decode("#E11D48")); 
        lblDoiTra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, Color.decode("#E11D48")),
                BorderFactory.createEmptyBorder(20, 0, 20, 40)));

        pnlLeftTabs.add(lblBanHang);
        pnlLeftTabs.add(lblDoiTra);
        pnlTabs.add(pnlLeftTabs, BorderLayout.WEST);

        // ==================== 2. HEADER (TITLE & SEARCH) ====================
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(25, 0, 15, 0));

        JLabel lblTitle = new JLabel("ĐỔI / TRẢ HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.decode("#212B36"));
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setOpaque(false);

        JPanel pnlSearchWrapper = new JPanel(new BorderLayout(8, 0));
        pnlSearchWrapper.setBackground(Color.WHITE);
        pnlSearchWrapper.setPreferredSize(new Dimension(300, 45));
        pnlSearchWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                BorderFactory.createEmptyBorder(0, 15, 0, 15)));

        pnlSearchWrapper.add(new JLabel(new MenuIcon("SEARCH")), BorderLayout.WEST);

        String placeholder = "Mã phiếu, hóa đơn...";
        txtSearch = new JTextField(placeholder);
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setBorder(null);
        txtSearch.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if (txtSearch.getText().equals(placeholder)) { txtSearch.setText(""); txtSearch.setForeground(Color.BLACK); } }
            public void focusLost(FocusEvent e) { if (txtSearch.getText().isEmpty()) { txtSearch.setText(placeholder); txtSearch.setForeground(Color.GRAY); } }
        });
        txtSearch.addKeyListener(new KeyAdapter() { public void keyReleased(KeyEvent e) { applyFilter(); } });
        pnlSearchWrapper.add(txtSearch, BorderLayout.CENTER);

        JButton btnReset = createActionBtn("Làm mới", "#6C757D");
        btnReset.setIcon(new MenuIcon("REFRESH"));
        
        JButton btnCreate = createActionBtn("Tạo phiếu", "#E11D48"); 
        
        btnCreate.addActionListener(e -> {
            Window p = SwingUtilities.getWindowAncestor(this);
            TaoPhieuDoiTra dialogTaoPhieu = new TaoPhieuDoiTra((Frame) p, model);
            dialogTaoPhieu.setVisible(true);
        });
        pnlActions.add(pnlSearchWrapper);
        pnlActions.add(btnReset);
        pnlActions.add(btnCreate);
        pnlHeader.add(pnlActions, BorderLayout.EAST);

        // ==================== 3. BỘ LỌC TRẠNG THÁI ====================
        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlFilters.setOpaque(false);
        pnlFilters.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        statusBtns = new JButton[]{
            createFilterBtn("Tất cả", true), createFilterBtn("Chờ xử lý", false),
            createFilterBtn("Đang xử lý", false), createFilterBtn("Hoàn thành", false), createFilterBtn("Từ chối", false)
        };
        for (JButton b : statusBtns) { pnlFilters.add(b); b.addActionListener(e -> xuLyStatus(b)); }

        JPanel pnlNorth = new JPanel();
        pnlNorth.setLayout(new BoxLayout(pnlNorth, BoxLayout.Y_AXIS));
        pnlNorth.add(pnlTabs);
        pnlNorth.add(pnlHeader);
        pnlNorth.add(pnlFilters);
        this.add(pnlNorth, BorderLayout.NORTH);

        // ==================== 4. BẢNG DỮ LIỆU ĐỔI TRẢ ====================
        String[] cols = {"Mã phiếu", "Hóa đơn gốc", "Khách hàng", "Loại", "Lỗi", "Tiền hoàn", "Chênh lệch ĐH", "Trạng thái", "Ngày", "Xử lý"};
        model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setRowHeight(55);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Color.decode("#F1F3F5"));
        table.setSelectionBackground(Color.decode("#F8F9FA")); 

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(100, 50));
        header.setBackground(Color.decode("#F9FAFB"));
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));

        table.setDefaultRenderer(Object.class, new DoiTraTableRenderer());
        
        // BỔ SUNG 1: Cấp chiều rộng cho cột cuối để chứa đủ 2 nút
        table.getColumnModel().getColumn(9).setPreferredWidth(180);

        // BỔ SUNG 2: Bắt sự kiện click chuột để xử lý Tiếp nhận / Từ chối
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                
                // Cột số 9 là cột "Xử lý"
                if (row >= 0 && col == 9) {
                    int modelRow = table.convertRowIndexToModel(row);
                    String status = model.getValueAt(modelRow, 7).toString(); // Cột 7 là Trạng thái
                    
                    if (status.equals("Chờ xử lý")) {
                        // Tính tọa độ chuột để biết bấm nút trái hay phải
                        Rectangle cellRect = table.getCellRect(row, col, false);
                        int clickX = e.getX() - cellRect.x;
                        
                        if (clickX < cellRect.width / 2) {
                            // Click nửa trái -> TIẾP NHẬN
                            int opt = JOptionPane.showConfirmDialog(ManHinhDoiTra.this, "Xác nhận TIẾP NHẬN phiếu đổi/trả này?", "Tiếp nhận", JOptionPane.YES_NO_OPTION);
                            if(opt == JOptionPane.YES_OPTION) model.setValueAt("Hoàn thành", modelRow, 7);
                        } else {
                            // Click nửa phải -> TỪ CHỐI
                            int opt = JOptionPane.showConfirmDialog(ManHinhDoiTra.this, "Xác nhận TỪ CHỐI phiếu đổi/trả này?", "Từ chối", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if(opt == JOptionPane.YES_OPTION) model.setValueAt("Từ chối", modelRow, 7);
                        }
                        table.repaint(); // Vẽ lại giao diện ngay
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8")));
        this.add(sp, BorderLayout.CENTER);
        
        
    }
    
    // --- BỔ SUNG 3: RENDERER MỚI CÓ CHỨA NÚT BẤM VÀ MÀU SẮC MỚI ---
    class DoiTraTableRenderer extends DefaultTableCellRenderer {
        
        JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 12));
        JButton btnTiepNhan = new JButton("Tiếp nhận");
        JButton btnTuChoi = new JButton("Từ chối");
        
        public DoiTraTableRenderer() {
            pnlAction.setOpaque(true);
            
            btnTiepNhan.setBackground(Color.decode("#3B82F6")); // Màu xanh dương
            btnTiepNhan.setForeground(Color.WHITE);
            btnTiepNhan.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnTiepNhan.setBorderPainted(false);
            btnTiepNhan.setFocusPainted(false);
            btnTiepNhan.setPreferredSize(new Dimension(85, 30));
            btnTiepNhan.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            btnTuChoi.setBackground(Color.decode("#EF4444")); // Màu Đỏ
            btnTuChoi.setForeground(Color.WHITE);
            btnTuChoi.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnTuChoi.setBorderPainted(false);
            btnTuChoi.setFocusPainted(false);
            btnTuChoi.setPreferredSize(new Dimension(75, 30));
            btnTuChoi.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            pnlAction.add(btnTiepNhan);
            pnlAction.add(btnTuChoi);
        }

        public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasF, int r, int c) {
            
            // Xử lý cột 9 (Nút bấm)
            if (c == 9) {
                String status = t.getValueAt(r, 7).toString(); 
                pnlAction.setBackground(isSel ? Color.decode("#F8F9FA") : Color.WHITE);
                
                if (status.equals("Chờ xử lý")) {
                    return pnlAction; 
                } else {
                    JLabel empty = new JLabel();
                    empty.setOpaque(true);
                    empty.setBackground(isSel ? Color.decode("#F8F9FA") : Color.WHITE);
                    return empty;
                }
            }
            
            // Các cột chữ bình thường
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, isSel, hasF, r, c);
            lbl.setHorizontalAlignment(CENTER);
            lbl.setOpaque(true);
            lbl.setBackground(isSel ? Color.decode("#F8F9FA") : Color.WHITE);
            lbl.setForeground(Color.decode("#212B36"));

            if (c == 0) lbl.setForeground(Color.decode("#1967D2")); // Mã phiếu màu xanh dương
            
            if (c == 3 && v != null && !v.toString().equals("")) { // Cột LOẠI
                lbl.setBackground(v.toString().equals("Trả hàng") ? Color.decode("#FEE2E2") : Color.decode("#E0F2FE"));
                lbl.setForeground(v.toString().equals("Trả hàng") ? Color.decode("#EF4444") : Color.decode("#0284C7"));
            }
            
            if (c == 4 && v != null && !v.toString().equals("")) { // Cột LỖI 
                lbl.setBackground(Color.decode("#FFEDD5"));
                lbl.setForeground(Color.decode("#D97706"));
            }

            if (c == 5 && v != null && !v.toString().equals("---")) { // Cột TIỀN HOÀN 
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lbl.setForeground(Color.decode("#DC2626"));
            }
            
            if (c == 6 && v != null && !v.toString().equals("---")) { // Cột CHÊNH LỆCH ĐH
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lbl.setForeground(Color.decode("#DC2626"));
            }

            if (c == 7) { // Cột TRẠNG THÁI
                if (v.equals("Hoàn thành")) { lbl.setBackground(Color.decode("#DCFCE7")); lbl.setForeground(Color.decode("#10B981")); }
                else if (v.equals("Chờ xử lý")) { lbl.setBackground(Color.decode("#FEF3C7")); lbl.setForeground(Color.decode("#D97706")); } // Vàng cam
                else if (v.equals("Từ chối")) { lbl.setBackground(Color.decode("#FEE2E2")); lbl.setForeground(Color.decode("#EF4444")); } // Đỏ
                else { lbl.setBackground(Color.decode("#F3F4F6")); lbl.setForeground(Color.decode("#6B7280")); }
            }
            return lbl;
        }
    }

    private void xuLyStatus(JButton b) {
        for (JButton btn : statusBtns) setBtnNormal(btn);
        setBtnActive(b);
        filterStatus = b.getText().trim();
        applyFilter();
    }

    private void applyFilter() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (!filterStatus.equals("Tất cả")) filters.add(RowFilter.regexFilter("^" + filterStatus + "$", 7));
        String search = txtSearch.getText().trim();
        if (!search.isEmpty() && !search.equals("Mã phiếu, hóa đơn...")) filters.add(RowFilter.regexFilter("(?i)" + search));
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
    }

    private JButton createActionBtn(String txt, String hex) {
        JButton btn = new JButton(txt);
        btn.setPreferredSize(new Dimension(130, 45));
        btn.setBackground(Color.decode(hex));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        return btn;
    }

    private JButton createFilterBtn(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (isActive) setBtnActive(btn); 
        else setBtnNormal(btn);
        
        return btn;
    }

    private void setBtnActive(JButton btn) {
        btn.setBackground(Color.decode("#1967D2"));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#1967D2"), 1), BorderFactory.createEmptyBorder(8, 15, 8, 15)));
    }

    private void setBtnNormal(JButton btn) {
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.decode("#374151"));
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1), BorderFactory.createEmptyBorder(8, 15, 8, 15)));
    }

    
}