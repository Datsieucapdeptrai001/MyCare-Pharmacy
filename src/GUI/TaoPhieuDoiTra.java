package GUI;

import javax.swing.*;
import Entity.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import Utils.MenuIcon;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import BUS.*;

public class TaoPhieuDoiTra extends JDialog {
	private DefaultTableModel spMoiModel; 
    private JTable tableSPMoi;
    private boolean isUpdatingTable = false; // Cờ chống lỗi vòng lặp bảng
    private boolean isUpdatingCart = false;
    private JPanel pnlSuccessInfo, pnlDetailsForm, pnlNewProduct;
    private JLabel lblError, lblMaHD, lblDetails, lblHoanTien; 
    private JTextField txtSearch, txtSearchNew;
    private JLabel lblNewSPName, lblNewSPPrice;
    private JButton btnTraHang, btnDoiHang, btnTaoPhieu, btnTimMoi;
    private JComboBox<String> cboLyDo; 
    private JTable table;
    
    private DefaultTableModel mainModel;
    private DefaultTableModel chiTietModel; 
    
    private BUS_HoaDon busHD = new BUS_HoaDon();
    private LocalDateTime ngayHoaDonGoc; 
    private JPopupMenu suggestionMenu;
    // Biến lưu trữ giá trị
    private double tongTienGoc = 0;
    private double giaMoi = 0; // Giá trị món đồ mới
    private String tenMoi = "";
    
    private Color primaryRed = Color.decode("#DC2626"); 
    private Color borderGray = Color.decode("#DFE3E8");
    
    public TaoPhieuDoiTra(Frame parent, DefaultTableModel model) {
        super(parent, "Tạo phiếu đổi / trả hàng", true);
        setSize(750, 240);
        this.mainModel = model;
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- 1. HEADER ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(primaryRed);
        pnlHeader.setBorder(new EmptyBorder(12, 15, 12, 15));
        
        JLabel lblTitle = new JLabel("Tạo phiếu đổi / trả hàng");
        lblTitle.setIcon(new MenuIcon("SYNC")); 
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

        // --- 2. BODY ---
        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(20, 25, 20, 25));

        initDetailsComponents();

        pnlBody.add(createSearchPanel());
        pnlBody.add(Box.createRigidArea(new Dimension(0, 15)));

        pnlSuccessInfo = createSuccessPanel();
        pnlSuccessInfo.setVisible(false); 
        pnlBody.add(pnlSuccessInfo);
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));

        pnlDetailsForm = createDetailsPanel();
        pnlDetailsForm.setVisible(false);
        pnlBody.add(pnlDetailsForm);

        JScrollPane scrollPane = new JScrollPane(pnlBody);
        scrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderGray));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. FOOTER ---
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(new EmptyBorder(15, 25, 15, 25));

        JButton btnHuy = new JButton("Hủy");
        btnHuy.setPreferredSize(new Dimension(100, 40));
        btnHuy.setBackground(Color.WHITE);
        btnHuy.setBorder(BorderFactory.createLineBorder(borderGray));
        btnHuy.setFocusPainted(false);
        btnHuy.addActionListener(e -> dispose());

        btnTaoPhieu = new JButton("Tạo phiếu đổi/trả");
        btnTaoPhieu.setIcon(new MenuIcon("SYNC"));
        btnTaoPhieu.setPreferredSize(new Dimension(220, 40));
        btnTaoPhieu.setBackground(primaryRed);
        btnTaoPhieu.setForeground(Color.WHITE);
        btnTaoPhieu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTaoPhieu.setBorderPainted(false);
        btnTaoPhieu.setFocusPainted(false);

        btnTaoPhieu.addActionListener(e -> xuLyTaoPhieu());

        pnlFooter.add(btnHuy, BorderLayout.WEST);
        pnlFooter.add(btnTaoPhieu, BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    // --- CÁC HÀM XÂY DỰNG COMPONENT ---
    private void initDetailsComponents() {
        cboLyDo = new JComboBox<>(new String[]{"Khách đổi ý", "Lỗi nhà sản xuất", "Lỗi từ phía khách hàng"});
        lblHoanTien = new JLabel("Vui lòng tích chọn sản phẩm trên bảng để tính tiền!", SwingConstants.RIGHT);
        btnTraHang = new JButton("Trả hàng");
        btnDoiHang = new JButton("Đổi hàng");
        
        // --- 1. BẢNG SẢN PHẨM CŨ (THÊM CHECKBOX VÀ SỐ LƯỢNG) ---
        String[] cols = {"Chọn", "Sản phẩm", "SL Mua", "SL Đổi/Trả", "Đơn giá", "Thành tiền"};
        chiTietModel = new DefaultTableModel(cols, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : String.class; // Cột 0 là Checkbox
            }
            @Override
            public boolean isCellEditable(int row, int column) { 
                return column == 0 || column == 3; // Chỉ cho phép sửa Checkbox và Cột SL Đổi/Trả
            }
        };

        chiTietModel.addTableModelListener(e -> {
            if (isUpdatingTable) return;
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                
                // Nếu sửa số lượng
                if (row >= 0 && col == 3) {
                    try {
                        int slMua = Integer.parseInt(chiTietModel.getValueAt(row, 2).toString());
                        int slThaoTac = Integer.parseInt(chiTietModel.getValueAt(row, 3).toString());
                        
                        if (slThaoTac < 1) slThaoTac = 1;
                        if (slThaoTac > slMua) slThaoTac = slMua; // Không được trả lố số đã mua
                        
                        double donGia = Double.parseDouble(chiTietModel.getValueAt(row, 4).toString().replaceAll("[^0-9]", ""));
                        
                        isUpdatingTable = true;
                        chiTietModel.setValueAt(slThaoTac, row, 3);
                        chiTietModel.setValueAt(String.format("%,.0fđ", slThaoTac * donGia), row, 5); // Cập nhật lại thành tiền
                        // Tự động Tick chọn nếu người dùng sửa số lượng
                        chiTietModel.setValueAt(true, row, 0); 
                        isUpdatingTable = false;
                        
                        capNhatDieuKienDoiTra();
                    } catch (Exception ex) { isUpdatingTable = false; }
                }
                // Nếu click Checkbox
                else if (row >= 0 && col == 0) {
                    capNhatDieuKienDoiTra();
                }
            }
        });

        // --- 2. BẢNG SẢN PHẨM MỚI (GIỎ HÀNG) ---
        spMoiModel = new DefaultTableModel(new String[]{"Sản phẩm mới", "SL", "Đơn giá", "Thành tiền"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 1; } // Cho phép nhập số lượng
        };
        
        spMoiModel.addTableModelListener(e -> {
            if (isUpdatingCart) return;
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 1) {
                int row = e.getFirstRow();
                try {
                    int sl = Integer.parseInt(spMoiModel.getValueAt(row, 1).toString());
                    if (sl < 1) sl = 1;
                    double donGia = Double.parseDouble(spMoiModel.getValueAt(row, 2).toString().replaceAll("[^0-9]", ""));
                    
                    isUpdatingCart = true;
                    spMoiModel.setValueAt(sl, row, 1);
                    spMoiModel.setValueAt(String.format("%,.0fđ", sl * donGia), row, 3);
                    isUpdatingCart = false;
                    
                    tinhTongTienSPMoi();
                } catch(Exception ex) { isUpdatingCart = false; }
            }
        });
    }
    
    // Hàm phụ trợ cộng tổng tiền giỏ hàng mới
    private void tinhTongTienSPMoi() {
        giaMoi = 0;
        tenMoi = "";
        for (int i = 0; i < spMoiModel.getRowCount(); i++) {
            giaMoi += Double.parseDouble(spMoiModel.getValueAt(i, 3).toString().replaceAll("[^0-9]", ""));
            tenMoi += spMoiModel.getValueAt(i, 0).toString() + ", ";
        }
        if(tenMoi.endsWith(", ")) tenMoi = tenMoi.substring(0, tenMoi.length() - 2);
        capNhatDieuKienDoiTra();
    }

    private JPanel createSearchPanel() {
        JPanel pnl = new JPanel(new BorderLayout(12, 5));
        pnl.setBackground(Color.WHITE);
        
        // --- 1. LÀM ĐẸP Ô TEXTFIELD (Có Placeholder) ---
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(0, 42)); 
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15)); 
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#CBD5E1")), 
            BorderFactory.createEmptyBorder(0, 15, 0, 15)
        ));

        // Vẽ placeholder mờ "Tìm kiếm mã hóa đơn..."
        txtSearch.setText("Nhập mã hóa đơn (VD: HD2024-0001)...");
        txtSearch.setForeground(Color.GRAY);
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().equals("Nhập mã hóa đơn (VD: HD2024-0001)...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setForeground(Color.GRAY);
                    txtSearch.setText("Nhập mã hóa đơn (VD: HD2024-0001)...");
                }
            }
        });

        // --- TẠO POPUP GỢI Ý HÓA ĐƠN ---
        JPopupMenu hdSuggestionMenu = new JPopupMenu();
        hdSuggestionMenu.setFocusable(false);
        
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { goiYHoaDon(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { goiYHoaDon(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { goiYHoaDon(); }
            
            private void goiYHoaDon() {
                SwingUtilities.invokeLater(() -> {
                    String kw = txtSearch.getText().trim();
                    if (kw.isEmpty() || kw.equals("Nhập mã hóa đơn (VD: HD2024-0001)...")) {
                        hdSuggestionMenu.setVisible(false);
                        return;
                    }

                    List<HoaDon> dsHoaDon = busHD.layTatCaHoaDon(); 
                    if (dsHoaDon == null) return;
                    
                    // FIX: Tránh hiện lại popup sau khi đã click chọn xong 1 mã hóa đơn
                    for (HoaDon hd : dsHoaDon) {
                        if (hd.getId().equalsIgnoreCase(kw)) {
                            hdSuggestionMenu.setVisible(false);
                            return; 
                        }
                    }
                    
                    JPanel pnlList = new JPanel();
                    pnlList.setLayout(new BoxLayout(pnlList, BoxLayout.Y_AXIS));
                    pnlList.setBackground(Color.WHITE);
                    
                    int count = 0;
                    for (HoaDon hd : dsHoaDon) {
                        if (hd.getId().toLowerCase().contains(kw.toLowerCase())) {
                            
                            JLabel lblItem = new JLabel(hd.getId() + "   -   " + hd.getNgayLapHD().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            lblItem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                            lblItem.setOpaque(true); 
                            lblItem.setBackground(Color.WHITE);
                            lblItem.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#F1F5F9")), 
                                BorderFactory.createEmptyBorder(10, 15, 10, 15) 
                            ));
                            lblItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
                            lblItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42)); 
                            
                            lblItem.addMouseListener(new java.awt.event.MouseAdapter() {
                                public void mouseEntered(java.awt.event.MouseEvent evt) {
                                    lblItem.setBackground(Color.decode("#F8FAFC")); 
                                }
                                public void mouseExited(java.awt.event.MouseEvent evt) {
                                    lblItem.setBackground(Color.WHITE); 
                                }
                                public void mousePressed(java.awt.event.MouseEvent evt) {
                                    hdSuggestionMenu.setVisible(false); 
                                    SwingUtilities.invokeLater(() -> {
                                        // CHỈ LẤY ID CỦA HÓA ĐƠN, KHÔNG LẤY NGÀY THÁNG
                                        txtSearch.setText(hd.getId()); 
                                        txtSearch.setForeground(Color.BLACK);
                                        xuLyTimKiemHD(); 
                                    });
                                }
                            });
                            
                            pnlList.add(lblItem);
                            count++;
                            if (count >= 8) break; 
                        }
                    }
                    
                    if (count > 0) {
                        JScrollPane scrollPane = new JScrollPane(pnlList);
                        scrollPane.setBorder(null);
                        scrollPane.getViewport().setBackground(Color.WHITE);
                        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                        
                        int listHeight = pnlList.getPreferredSize().height;
                        scrollPane.setPreferredSize(new Dimension(txtSearch.getWidth(), Math.min(listHeight, 220)));
                        scrollPane.getVerticalScrollBar().setUI(new Utils.ModernScrollBarUI());
                        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0)); 
                        
                        hdSuggestionMenu.removeAll();
                        hdSuggestionMenu.setBorder(BorderFactory.createLineBorder(Color.decode("#CBD5E1")));
                        hdSuggestionMenu.add(scrollPane);
                        hdSuggestionMenu.pack();
                        
                        // FIX LỖI NHẢY CHỮ: Chỉ show khi popup chưa hiển thị, KHÔNG gọi requestFocus()
                        if (!hdSuggestionMenu.isVisible()) {
                            hdSuggestionMenu.show(txtSearch, 0, txtSearch.getHeight() - 1);
                        }
                    } else {
                        hdSuggestionMenu.setVisible(false);
                    }
                });
            }
        });

        // --- 2. LÀM ĐẸP NÚT TÌM KIẾM ---
        JButton btnTim = new JButton("Tìm kiếm");
        btnTim.setPreferredSize(new Dimension(120, 42));
        btnTim.setBackground(Color.decode("#2563EB")); 
        btnTim.setForeground(Color.WHITE);
        btnTim.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTim.setFocusPainted(false); 
        btnTim.setBorderPainted(false); 
        btnTim.setCursor(new Cursor(Cursor.HAND_CURSOR)); 

        btnTim.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnTim.setBackground(Color.decode("#1D4ED8")); 
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnTim.setBackground(Color.decode("#2563EB")); 
            }
        });

        // Tách logic tìm kiếm ra một hàm riêng để gọi từ phím Enter
        btnTim.addActionListener(e -> xuLyTimKiemHD());

        // Gán sự kiện phím Enter cho ô tìm kiếm
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    hdSuggestionMenu.setVisible(false);
                    xuLyTimKiemHD();
                }
            }
        });

        pnl.add(txtSearch, BorderLayout.CENTER);
        pnl.add(btnTim, BorderLayout.EAST);
        
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.ITALIC, 13)); 
        lblError.setForeground(primaryRed);
        lblError.setBorder(new EmptyBorder(5, 5, 0, 0)); 
        
        pnl.add(lblError, BorderLayout.SOUTH);
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        return pnl;
    }
    private void xuLyTimKiemHD() {
        String maHD = txtSearch.getText().trim();
        if (maHD.isEmpty() || maHD.equals("Nhập mã hóa đơn (VD: HD2024-0001)...")) {
            lblError.setText("Vui lòng nhập mã hóa đơn!");
            lblError.setVisible(true);
            return;
        }

        HoaDon hd = busHD.getHoaDonTheoMa(maHD); 
        if (hd != null) {
            ngayHoaDonGoc = hd.getNgayLapHD(); 
            chiTietModel.setRowCount(0); 
            tongTienGoc = 0; 
            
            BUS_ChiTietHoaDon busCTHD = new BUS_ChiTietHoaDon(); 
            List<Object[]> dsChiTiet = busCTHD.layDuLieuDoiTra(hd.getId()); 

            if (dsChiTiet != null && !dsChiTiet.isEmpty()) {
                for (Object[] rowData : dsChiTiet) {
                    String tenSP = rowData[0].toString();
                    String soLuong = rowData[2].toString();
                    String donGia = rowData[3].toString();
                    String thanhTien = rowData[5].toString();
                    
                    // Mặc định false (Chưa tick), hiển thị SL Mua = SL Đổi Trả
                    chiTietModel.addRow(new Object[]{false, tenSP, soLuong, soLuong, donGia, thanhTien});
                }
            }

            lblMaHD.setText("Hóa đơn " + hd.getId());
            lblDetails.setText(String.format("Ngày mua: %s", hd.getNgayLapHD().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

            lblError.setVisible(false);
            pnlSuccessInfo.setVisible(true);
            pnlDetailsForm.setVisible(true);
            
            setSize(750, 750);
            setLocationRelativeTo(getOwner());
            capNhatDieuKienDoiTra(); 
        } else {
            lblError.setText("Hệ thống không tìm thấy hóa đơn: " + maHD);
            lblError.setVisible(true);
        }
    }
    private JPanel createSuccessPanel() {
        // ... (Giữ nguyên như cũ)
        JPanel pnl = new JPanel(new GridLayout(2, 1, 0, 5));
        pnl.setBackground(Color.decode("#F0FDF4"));
        pnl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.decode("#BBF7D0")), new EmptyBorder(10, 15, 10, 15)));
        lblMaHD = new JLabel("Hóa đơn..."); 
        lblMaHD.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMaHD.setForeground(Color.decode("#16A34A"));
        lblDetails = new JLabel("...");
        lblDetails.setForeground(Color.decode("#4B5563"));
        pnl.add(lblMaHD);
        pnl.add(lblDetails);
        return pnl;
    }

    private JPanel createDetailsPanel() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBackground(Color.WHITE);

        // 1. Loại yêu cầu & Lý do
        JPanel pnlRow1 = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlRow1.setBackground(Color.WHITE);
        pnlRow1.setMaximumSize(new Dimension(1000, 60));

        JPanel pnlLoai = new JPanel(new BorderLayout(0, 5));
        pnlLoai.setBackground(Color.WHITE);
        pnlLoai.add(new JLabel("<html><b>Loại yêu cầu</b></html>"), BorderLayout.NORTH);
        
        JPanel pnlToggle = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlToggle.setBackground(Color.WHITE);
        btnTraHang.setFocusPainted(false);
        btnDoiHang.setFocusPainted(false);
        btnTraHang.addActionListener(e -> setToggleState(true));
        btnDoiHang.addActionListener(e -> setToggleState(false));
        pnlToggle.add(btnTraHang);
        pnlToggle.add(btnDoiHang);
        pnlLoai.add(pnlToggle, BorderLayout.CENTER);

        JPanel pnlLyDo = new JPanel(new BorderLayout(0, 5));
        pnlLyDo.setBackground(Color.WHITE);
        pnlLyDo.add(new JLabel("<html><b>Nguyên nhân</b></html>"), BorderLayout.NORTH);
        cboLyDo.setBackground(Color.WHITE);
        pnlLyDo.add(cboLyDo, BorderLayout.CENTER);

        pnlRow1.add(pnlLoai);
        pnlRow1.add(pnlLyDo);
        pnl.add(pnlRow1);
        pnl.add(Box.createRigidArea(new Dimension(0, 15)));

        // 2. Bảng sản phẩm cũ
        JPanel pnlTable = new JPanel(new BorderLayout(0, 5));
        pnlTable.setBackground(Color.WHITE);
        pnlTable.add(new JLabel("<html><b>Sản phẩm khách muốn đổi/trả</b> <font color='red'>(Click chọn 1 dòng)</font></html>"), BorderLayout.NORTH);

        table = new JTable(chiTietModel);
        table.setRowHeight(35);
        table.getTableHeader().setBackground(Color.WHITE);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) capNhatDieuKienDoiTra(); // Tự tính lại tiền khi click chọn dòng
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(0, 120)); 
        sp.getViewport().setBackground(Color.WHITE);
        pnlTable.add(sp, BorderLayout.CENTER);
        pnl.add(pnlTable);
        pnl.add(Box.createRigidArea(new Dimension(0, 15)));

        // 3. KHU VỰC TÌM SP MỚI (CHỈ HIỆN KHI ĐỔI HÀNG)
        pnlNewProduct = createNewProductPanel();
        pnl.add(pnlNewProduct);
        pnl.add(Box.createRigidArea(new Dimension(0, 15)));

        // Nút Tính tiền
        lblHoanTien.setFont(new Font("Segoe UI", Font.BOLD, 15));
        pnl.add(lblHoanTien);
        pnl.add(Box.createRigidArea(new Dimension(0, 10)));

        cboLyDo.addActionListener(e -> capNhatDieuKienDoiTra());
        setToggleState(true); 
        
        return pnl;
    }

    // --- PANEL TÌM KIẾM SẢN PHẨM MỚI ---
    private JPanel createNewProductPanel() {
        JPanel pnl = new JPanel(new BorderLayout(10, 5));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createTitledBorder("Giỏ hàng sản phẩm mới (Có thể chọn nhiều)"));

        txtSearchNew = new JTextField();
        txtSearchNew.setPreferredSize(new Dimension(0, 38));
        txtSearchNew.setText("Gõ tên để tìm sản phẩm...");
        txtSearchNew.setForeground(Color.GRAY);
        txtSearchNew.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtSearchNew.getText().equals("Gõ tên để tìm sản phẩm...")) {
                    txtSearchNew.setText("");
                    txtSearchNew.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtSearchNew.getText().isEmpty()) {
                    txtSearchNew.setForeground(Color.GRAY);
                    txtSearchNew.setText("Gõ tên để tìm sản phẩm...");
                }
            }
        });

        // (Phần khởi tạo suggestionMenu giữ nguyên y hệt như cũ)
        suggestionMenu = new JPopupMenu();
        suggestionMenu.setFocusable(false);
        suggestionMenu.setBackground(Color.WHITE);
        txtSearchNew.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { timKiemLive(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { timKiemLive(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { timKiemLive(); }
        });

        // BẢNG GIỎ HÀNG THAY CHO DÒNG CHỮ
        tableSPMoi = new JTable(spMoiModel);
        tableSPMoi.setRowHeight(30);
        tableSPMoi.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        JScrollPane scrollSPMoi = new JScrollPane(tableSPMoi);
        scrollSPMoi.setPreferredSize(new Dimension(0, 100));

        JPanel pnlTop = new JPanel(new BorderLayout(5, 0));
        pnlTop.setOpaque(false);
        pnlTop.add(txtSearchNew, BorderLayout.CENTER);
        
        JButton btnXoa = new JButton("Xóa món chọn");
        btnXoa.setBackground(Color.decode("#FEE2E2"));
        btnXoa.setForeground(Color.decode("#DC2626"));
        btnXoa.setFocusPainted(false);
        btnXoa.addActionListener(e -> {
            int row = tableSPMoi.getSelectedRow();
            if(row >= 0) {
                spMoiModel.removeRow(row);
                tinhTongTienSPMoi();
            }
        });
        pnlTop.add(btnXoa, BorderLayout.EAST);

        pnl.add(pnlTop, BorderLayout.NORTH);
        pnl.add(scrollSPMoi, BorderLayout.CENTER);
        return pnl;
    }

    // --- LOGIC TÍNH TOÁN NGHIỆP VỤ ---
    private void capNhatDieuKienDoiTra() {
        if (lblHoanTien == null || cboLyDo == null || ngayHoaDonGoc == null) return;

        String lyDo = cboLyDo.getSelectedItem().toString();
        boolean isTraHang = btnTraHang.getBackground().equals(Color.decode("#FF3B30"));
        
        // 1. Quét bảng, chỉ tính tiền các món có Dấu Checkbox = true
        double giaTriGoc = 0;
        int countSelected = 0;
        for (int i = 0; i < chiTietModel.getRowCount(); i++) {
            boolean isSelected = (boolean) chiTietModel.getValueAt(i, 0);
            if (isSelected) {
                String thanhTienStr = chiTietModel.getValueAt(i, 5).toString();
                giaTriGoc += Double.parseDouble(thanhTienStr.replaceAll("[^0-9]", ""));
                countSelected++;
            }
        }

        if (countSelected == 0) {
            lblHoanTien.setText("Vui lòng TÍCH CHỌN ít nhất 1 sản phẩm cũ để tính toán!");
            lblHoanTien.setForeground(Color.RED);
            if(btnTaoPhieu != null) btnTaoPhieu.setEnabled(false);
            return;
        }

        // 2. Tính tỷ lệ (như cũ)
        LocalDateTime hienTai = LocalDateTime.now();
        long hours = Duration.between(ngayHoaDonGoc, hienTai).toHours();
        double tiLe = 1.0;

        if (lyDo.equals("Lỗi từ phía khách hàng") || (hours > 72 && !lyDo.equals("Lỗi nhà sản xuất"))) {
            lblHoanTien.setText(lyDo.equals("Lỗi từ phía khách hàng") ? "Lỗi KH: Từ chối Đổi/Trả" : "Quá 3 ngày: Không đủ điều kiện.");
            lblHoanTien.setForeground(Color.RED);
            if(btnTaoPhieu != null) btnTaoPhieu.setEnabled(false);
            return;
        } else if (lyDo.equals("Lỗi nhà sản xuất")) {
            tiLe = 1.0;
        } else {
            tiLe = (hours <= 24) ? 1.0 : 0.8;
        }

        if(btnTaoPhieu != null) btnTaoPhieu.setEnabled(true);
        double v_qd = giaTriGoc * tiLe; 

        // 3. Hiển thị text hoàn tiền
        if (isTraHang) {
            lblHoanTien.setText(String.format("Tổng hoàn tiền: %,.0fđ (Phí khấu trừ: %,.0fđ)", v_qd, giaTriGoc - v_qd));
            lblHoanTien.setForeground(Color.BLUE);
        } else {
            if (spMoiModel.getRowCount() == 0) {
                lblHoanTien.setText(String.format("Quy đổi SP cũ: %,.0fđ. Vui lòng thêm SP mới!", v_qd));
                lblHoanTien.setForeground(Color.decode("#E67E22"));
                if(btnTaoPhieu != null) btnTaoPhieu.setEnabled(false);
            } else {
                double chenhLech = giaMoi - v_qd;
                String msg = (chenhLech > 0) ? "Khách bù thêm:" : "Thối lại khách:";
                lblHoanTien.setText(String.format("Tiền chênh lệch: %s %,.0fđ", msg, Math.abs(chenhLech)));
                lblHoanTien.setForeground(Color.decode("#16A34A")); 
            }
        }
    }
    private void timKiemLive() {
        SwingUtilities.invokeLater(() -> {
            String kw = txtSearchNew.getText().trim();
            if (kw.isEmpty() || kw.equals("Gõ tên để tìm sản phẩm...")) {
                suggestionMenu.setVisible(false);
                return;
            }

            BUS_SanPham busSP = new BUS_SanPham();
            List<SanPham> dsGoiY = busSP.traCuuSanPham(kw); 

            if (dsGoiY != null && !dsGoiY.isEmpty()) {
                
                // FIX: Ẩn popup nếu nhập khớp hoàn toàn (người dùng vừa click chọn)
                for (SanPham sp : dsGoiY) {
                    if (sp.getTen().equalsIgnoreCase(kw)) {
                        suggestionMenu.setVisible(false);
                        return;
                    }
                }
                
                suggestionMenu.removeAll(); 
                JPanel pnlList = new JPanel();
                pnlList.setLayout(new BoxLayout(pnlList, BoxLayout.Y_AXIS));
                pnlList.setBackground(Color.WHITE);

                for (SanPham sp : dsGoiY) {
                    JPanel pnlItem = new JPanel(new BorderLayout(15, 5));
                    pnlItem.setBackground(Color.WHITE);
                    pnlItem.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E5E7EB")), 
                        new EmptyBorder(8, 12, 8, 12) 
                    ));
                    pnlItem.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    pnlItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55)); 

                    JPanel pnlInfo = new JPanel(new BorderLayout(0, 3)); 
                    pnlInfo.setOpaque(false); 
                    JLabel lblTen = new JLabel(sp.getTen());
                    lblTen.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    
                    JPanel pnlSub = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                    pnlSub.setOpaque(false);
                    JLabel lblMa = new JLabel(sp.getId());
                    lblMa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    lblMa.setForeground(Color.decode("#6B7280")); 
                    
                    String tenDanhMuc = "Khác";
                    Color bgTag = Color.decode("#F3F4F6"), fgTag = Color.decode("#4B5563"); 
                    if (sp.getDanhMuc() != null) {
                        switch (sp.getDanhMuc()) {
                            case THUOC_KE_DON: tenDanhMuc = "Thuốc kê đơn"; bgTag = Color.decode("#FEE2E2"); fgTag = Color.decode("#DC2626"); break;
                            case THUOC_KHONG_KE_DON: tenDanhMuc = "Không kê đơn"; bgTag = Color.decode("#DCFCE7"); fgTag = Color.decode("#16A34A"); break;
                            case THUC_PHAM_CHUC_NANG: tenDanhMuc = "Thực phẩm chức năng"; bgTag = Color.decode("#DBEAFE"); fgTag = Color.decode("#1D4ED8"); break;
                            case MY_PHAM: tenDanhMuc = "Mỹ phẩm"; bgTag = Color.decode("#FCE7F3"); fgTag = Color.decode("#BE185D"); break;
                        }
                    }

                    final Color finalBgTag = bgTag;
                    JLabel lblTag = new JLabel(tenDanhMuc) {
                        @Override
                        protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(finalBgTag);
                            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); 
                            super.paintComponent(g);
                            g2.dispose();
                        }
                    };
                    lblTag.setFont(new Font("Segoe UI", Font.BOLD, 10)); 
                    lblTag.setForeground(fgTag);
                    lblTag.setOpaque(false); 
                    lblTag.setBorder(new EmptyBorder(1, 6, 1, 6)); 
                    
                    pnlSub.add(lblMa);
                    pnlSub.add(Box.createRigidArea(new Dimension(8, 0))); 
                    pnlSub.add(lblTag);
                    pnlInfo.add(lblTen, BorderLayout.NORTH);
                    pnlInfo.add(pnlSub, BorderLayout.CENTER);

                    BUS_DonViDoLuong busDVDL = new BUS_DonViDoLuong();
                    List<DonViDoLuong> dsDVT = busDVDL.getDSTheoMaSP(sp.getId());
                    double gia = 0; String dvt = "Hộp";
                    if (dsDVT != null && !dsDVT.isEmpty()) {
                        gia = dsDVT.get(0).getGia(); dvt = dsDVT.get(0).getTen();
                    }
                    final double finalGia = gia; 

                    JLabel lblGia = new JLabel(String.format("%,.0fđ / %s", gia, dvt));
                    lblGia.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    lblGia.setForeground(Color.decode("#1967D2")); 

                    pnlItem.add(pnlInfo, BorderLayout.CENTER);
                    pnlItem.add(lblGia, BorderLayout.EAST);

                    pnlItem.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseEntered(java.awt.event.MouseEvent evt) { pnlItem.setBackground(Color.decode("#F3F4F6")); }
                        public void mouseExited(java.awt.event.MouseEvent evt) { pnlItem.setBackground(Color.WHITE); }
                        public void mousePressed(java.awt.event.MouseEvent evt) {
                            suggestionMenu.setVisible(false); 
                            SwingUtilities.invokeLater(() -> {
                                txtSearchNew.setText(""); // Xóa text đi để gõ tiếp món khác
                                
                                boolean exists = false;
                                for(int i=0; i < spMoiModel.getRowCount(); i++) {
                                    if(spMoiModel.getValueAt(i, 0).equals(sp.getTen())) {
                                        int oldSL = Integer.parseInt(spMoiModel.getValueAt(i, 1).toString());
                                        spMoiModel.setValueAt(oldSL + 1, i, 1); 
                                        exists = true; break;
                                    }
                                }
                                if(!exists) {
                                    isUpdatingCart = true;
                                    spMoiModel.addRow(new Object[]{ sp.getTen(), 1, String.format("%,.0fđ", finalGia), String.format("%,.0fđ", finalGia) });
                                    isUpdatingCart = false;
                                }
                                tinhTongTienSPMoi(); 
                            });
                        }
                    });
                    pnlList.add(pnlItem);
                }
                
                JScrollPane scrollPane = new JScrollPane(pnlList);
                scrollPane.setBorder(null);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); 
                scrollPane.setPreferredSize(new Dimension(txtSearchNew.getWidth(), Math.min(pnlList.getPreferredSize().height, 250)));
                scrollPane.getVerticalScrollBar().setUI(new Utils.ModernScrollBarUI());
                scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0)); 
                
                suggestionMenu.add(scrollPane);
                suggestionMenu.pack(); 
                
                // FIX LỖI NHẢY CHỮ
                if (!suggestionMenu.isVisible()) {
                    suggestionMenu.show(txtSearchNew, 0, txtSearchNew.getHeight());
                }
                
            } else {
                suggestionMenu.setVisible(false);
            }
        });
    }
    private void setToggleState(boolean isTraHang) {
        if (isTraHang) {
            btnTraHang.setBackground(Color.decode("#FF3B30"));
            btnTraHang.setForeground(Color.WHITE);
            btnDoiHang.setBackground(Color.WHITE);
            btnDoiHang.setForeground(Color.decode("#4B5563"));
            btnDoiHang.setBorder(BorderFactory.createLineBorder(borderGray));
        } else {
            btnDoiHang.setBackground(Color.decode("#FF3B30"));
            btnDoiHang.setForeground(Color.WHITE);
            btnTraHang.setBackground(Color.WHITE);
            btnTraHang.setForeground(Color.decode("#4B5563"));
            btnTraHang.setBorder(BorderFactory.createLineBorder(borderGray));
        }
        
        if (pnlNewProduct != null) {
            pnlNewProduct.setVisible(!isTraHang); // Ẩn/hiện khu vực tìm SP mới
        }
        capNhatDieuKienDoiTra();
    }

    // --- LƯU PHIẾU ---
    private void xuLyTaoPhieu() {
        // 1. CHỐT CHẶN 1: Kiểm tra xem đã tìm thấy hóa đơn chưa?
        if (ngayHoaDonGoc == null || txtSearch.getText().contains("Nhập mã hóa đơn")) {
            JOptionPane.showMessageDialog(this, "Vui lòng tìm kiếm hóa đơn hợp lệ trước khi tạo phiếu!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return; // Dừng lại, không cho chạy tiếp
        }

        // 2. CHỐT CHẶN 2: Kiểm tra xem đã click chọn 1 sản phẩm cũ trong bảng chưa?
        int rowSelected = table.getSelectedRow(); 
        if (rowSelected == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng click chọn 1 sản phẩm trong bảng cần đổi/trả!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String loai = btnTraHang.getBackground().equals(Color.decode("#FF3B30")) ? "Trả hàng" : "Đổi hàng";
        
        // 3. CHỐT CHẶN 3: Nếu là "Đổi hàng", bắt buộc phải chọn sản phẩm mới
        if (loai.equals("Đổi hàng") && giaMoi == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng tìm và chọn sản phẩm mới muốn đổi sang!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String lyDo = cboLyDo.getSelectedItem().toString();
        
        // Lấy dòng text hiển thị để tách tiền
        String thongBaoText = lblHoanTien.getText();
        String colHoanTien = "0đ";
        String colChenhLech = "0đ";

        try {
            if (loai.equals("Trả hàng")) {
                colHoanTien = thongBaoText.split("\\(")[0].replace("Hoàn tiền:", "").trim();
            } else {
                colChenhLech = thongBaoText.split(":")[1].trim(); 
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tính toán tiền tệ. Vui lòng kiểm tra lại điều kiện đổi trả!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if(mainModel != null) {
            String maPhieu = "DTH-" + (System.currentTimeMillis() % 10000);
            String maHDGoc = txtSearch.getText().trim();
            String ngayTao = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            
            // Cắt chuỗi lấy tên khách an toàn hơn để tránh lỗi mảng (Array Out of Bounds)
            String khach = "Khách lẻ";
            if (lblDetails.getText().contains("|")) {
                khach = lblDetails.getText().split("\\|")[0].replace("Ngày mua:", "").trim(); 
            }
            
            // Note lại SP mới nếu có
            String ghiChu = loai.equals("Đổi hàng") ? "Đổi sang: " + tenMoi : "";

            mainModel.addRow(new Object[]{
                maPhieu, maHDGoc, khach, loai, lyDo, 
                colHoanTien, colChenhLech, "Tiếp nhận", ngayTao, ghiChu
            });

            JOptionPane.showMessageDialog(this, "Đã tiếp nhận yêu cầu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }
}