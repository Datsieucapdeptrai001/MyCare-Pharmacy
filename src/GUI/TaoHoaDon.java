package GUI;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import Components.MenuIcon;
public class TaoHoaDon extends JDialog {

    // Khai báo bảng màu chuẩn theo ảnh
    private Color darkBlue = Color.decode("#152A4B");
    private Color accentBlue = Color.decode("#1A73E8");
    private Color successGreen = Color.decode("#10B981");
    private Color orangeLogo = Color.decode("#F59E0B");
    private Color lightGray = Color.decode("#F8F9FA");
    private Color borderColor = Color.decode("#DFE3E8");

    public TaoHoaDon(Frame parent) {
        super(parent, "Tạo hóa đơn bán hàng mới", true);
        setSize(900, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // --- 1. HEADER (Thanh tiêu đề xanh đậm) ---
     // --- 1. HEADER (Thanh tiêu đề xanh đậm) ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(darkBlue);
        pnlHeader.setPreferredSize(new Dimension(0, 50));
        pnlHeader.setBorder(new EmptyBorder(0, 15, 0, 15)); // Thêm lề cho đẹp
        
        // --- SỬA TIÊU ĐỀ Ở ĐÂY ---
        JLabel lblTitle = new JLabel("Tạo hóa đơn bán hàng mới"); // Đã xóa emoji 🛒
        lblTitle.setIcon(new MenuIcon("CART")); // Gắn icon giỏ hàng vẽ tay vào
        lblTitle.setIconTextGap(10); // Tạo khoảng cách giữa icon và chữ
        lblTitle.setForeground(Color.WHITE); // Icon sẽ tự động thành màu trắng theo chữ
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        
        
        
        add(pnlHeader, BorderLayout.NORTH);

        // --- 2. BODY (Nội dung chính trong ScrollPane) ---
        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(10, 25, 10, 25));

        // Phần 1: Tra cứu khách hàng (Bản thân panel này đã có header riêng nên gọi thẳng)
        pnlBody.add(createCustomerPanel());
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));

        // Phần 2: Thêm sản phẩm (Gắn MenuIcon PACKAGE)
        pnlBody.add(createSectionPanel("Thêm sản phẩm", "PACKAGE", createProductPanel()));
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));

        // Phần 3: Khuyến mãi & Thanh toán (Gắn MenuIcon GIFT)
        pnlBody.add(createSectionPanel("Mã khuyến mãi", "GIFT", createVoucherPanel()));
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Phần 4: Tiền khách đưa & Tổng cộng
        pnlBody.add(createSummaryPanel());

        JScrollPane scrollPane = new JScrollPane(pnlBody);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. FOOTER (Các nút chức năng) ---
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));

        JButton btnDong = new JButton("✕ Đóng");
        btnDong.setPreferredSize(new Dimension(100, 40));
        btnDong.addActionListener(e -> dispose());

        JButton btnLuuNhap = new JButton("💾 Lưu nháp");
        styleButton(btnLuuNhap, orangeLogo);

        JButton btnThanhToan = new JButton("✓ Thanh toán");
        styleButton(btnThanhToan, successGreen);

        pnlFooter.add(btnDong);
        pnlFooter.add(btnLuuNhap);
        pnlFooter.add(btnThanhToan);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    
 // Hàm tạo khung bao quanh mỗi Section có tích hợp MenuIcon
    private JPanel createSectionPanel(String title, String iconType, JPanel content) {
        JPanel pnl = new JPanel(new BorderLayout(0, 10)); // Khoảng cách giữa tiêu đề và nội dung là 10px
        pnl.setBackground(Color.WHITE);

        // Tạo tiêu đề
        JLabel lblTitle = new JLabel(title);
        if (iconType != null) {
            lblTitle.setIcon(new MenuIcon(iconType)); // Gắn icon
            lblTitle.setIconTextGap(8); // Chỉnh khoảng cách giữa icon và chữ
        }
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.decode("#212B36"));

        pnl.add(lblTitle, BorderLayout.NORTH);
        pnl.add(content, BorderLayout.CENTER);
        return pnl;
    }

 // ========================================================
    // PANEL KHÁCH HÀNG (Có hiệu ứng hiện cảnh báo khi tìm không thấy)
    // ========================================================
    private JPanel createCustomerPanel() {
        JPanel pnlWrapper = new JPanel(new BorderLayout(0, 8)); 
        pnlWrapper.setBackground(Color.WHITE);
        pnlWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // --- 1. HEADER KHÁCH HÀNG ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlTitle.setBackground(Color.WHITE);
        
        JLabel lblIcon = new JLabel(new MenuIcon("USER")); 
        lblIcon.setForeground(Color.decode("#6C757D"));
        
        JLabel lblTitleText = new JLabel("Tra cứu khách hàng");
        lblTitleText.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitleText.setForeground(Color.decode("#212B36"));
        
        JLabel lblSubTitle = new JLabel("(tuỳ chọn)");
        lblSubTitle.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblSubTitle.setForeground(Color.decode("#9CA3AF"));
        
        pnlTitle.add(lblIcon);
        pnlTitle.add(lblTitleText);
        pnlTitle.add(lblSubTitle);

        JLabel lblBadge = new JLabel("Khách lẻ", SwingConstants.CENTER);
        lblBadge.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblBadge.setForeground(Color.decode("#4B5563"));
        lblBadge.setBackground(Color.decode("#F3F4F6"));
        lblBadge.setOpaque(true);
        lblBadge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        pnlHeader.add(pnlTitle, BorderLayout.WEST);
        pnlHeader.add(lblBadge, BorderLayout.EAST);

        // --- 2. BODY NHẬP LIỆU (GridBagLayout) ---
        JPanel pnlInput = new JPanel(new GridBagLayout());
        pnlInput.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        // ----------------------------------------------------
        // HÀNG 0: Ô TÌM KIẾM + NÚT TRA CỨU
        // ----------------------------------------------------
        gbc.insets = new Insets(0, 0, 8, 10); 
        JTextField txtSearch = createStyledTextField("Nhập SĐT hoặc mã KH để liên kết điểm thưởng...");
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.75; 
        pnlInput.add(txtSearch, gbc);

        JButton btnSearch = new JButton("Tra cứu");
        btnSearch.setIcon(new MenuIcon("SEARCH")); 
        btnSearch.setIconTextGap(6);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.setBackground(Color.decode("#1967D2"));
        btnSearch.setForeground(Color.WHITE); 
        btnSearch.setFocusPainted(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1; gbc.weightx = 0.25; gbc.insets = new Insets(0, 0, 8, 0); 
        pnlInput.add(btnSearch, gbc);

        // ----------------------------------------------------
        // HÀNG 1: THÔNG BÁO LỖI + NÚT TẠO MỚI (MẶC ĐỊNH ẨN)
        // ----------------------------------------------------
        gbc.insets = new Insets(0, 0, 8, 10); 
        JLabel lblWarning = new JLabel("Không tìm thấy khách hàng. Bạn có muốn tạo mới?");
        lblWarning.setIcon(new MenuIcon("WARNING"));
        lblWarning.setIconTextGap(8);
        lblWarning.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblWarning.setForeground(Color.decode("#DC2626")); // Màu chữ đỏ
        lblWarning.setBackground(Color.decode("#FFFBEB")); // Màu nền vàng nhạt
        lblWarning.setOpaque(true);
        lblWarning.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#FCA5A5"), 1, true), // Viền đỏ nhạt
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        lblWarning.setVisible(false); // Cài đặt ẩn mặc định
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.75;
        pnlInput.add(lblWarning, gbc);

        JButton btnAddCustomer = new JButton("Thêm KH mới");
        btnAddCustomer.setIcon(new MenuIcon("USER_ADD"));
        btnAddCustomer.setIconTextGap(6);
        btnAddCustomer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddCustomer.setBackground(Color.decode("#E11D48")); // Màu hồng đỏ
        btnAddCustomer.setForeground(Color.WHITE);
        btnAddCustomer.setFocusPainted(false);
        btnAddCustomer.setBorderPainted(false);
        btnAddCustomer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAddCustomer.setVisible(false); // Cài đặt ẩn mặc định
        gbc.gridx = 1; gbc.weightx = 0.25; gbc.insets = new Insets(0, 0, 8, 0); 
        pnlInput.add(btnAddCustomer, gbc);

        // ----------------------------------------------------
        // HÀNG 2: TÊN KHÁCH + SĐT
        // ----------------------------------------------------
        gbc.insets = new Insets(0, 0, 0, 10); 
        JTextField txtName = createStyledTextField("Tên khách (bỏ trống = Khách lẻ)");
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.5; // Chuyển xuống dòng 2
        pnlInput.add(txtName, gbc);

        JTextField txtPhone = createStyledTextField("Số điện thoại (tuỳ chọn)");
        gbc.gridx = 1; gbc.weightx = 0.5; gbc.insets = new Insets(0, 0, 0, 0);
        pnlInput.add(txtPhone, gbc);

        btnSearch.addActionListener(e -> {
            // Khi bấm nút, cho hiện thông báo và nút Tạo mới lên
            lblWarning.setVisible(true);
            btnAddCustomer.setVisible(true);
            
            // Cập nhật lại giao diện bên trong Panel
            pnlInput.revalidate();
            pnlInput.repaint();
            
            // ĐÃ XÓA dòng window.pack() để giữ nguyên kích thước cửa sổ 800x700
        });

        pnlWrapper.add(pnlHeader, BorderLayout.NORTH);
        pnlWrapper.add(pnlInput, BorderLayout.CENTER);

        return pnlWrapper;
    }
    private JTextField createStyledTextField(String placeholder) {
        JTextField txt = new JTextField(placeholder);
        
        // SỬA CHỖ NÀY: Giảm chiều cao từ 42 xuống 36 để các ô nhập liệu nhỏ gọn lại
        txt.setPreferredSize(new Dimension(0, 36)); 
        
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setForeground(Color.GRAY);
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        
        // Sự kiện placeholder
        txt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txt.getText().equals(placeholder)) {
                    txt.setText(""); txt.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txt.getText().isEmpty()) {
                    txt.setForeground(Color.GRAY); txt.setText(placeholder);
                }
            }
        });
        return txt;
    }

    private JPanel createProductPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 15));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(15, 20, 15, 20));

        // --- 1. THANH TÌM KIẾM SẢN PHẨM ---
        JPanel pnlSearchWrapper = new JPanel(new BorderLayout(10, 0));
        pnlSearchWrapper.setBackground(Color.WHITE);
        pnlSearchWrapper.setPreferredSize(new Dimension(0, 42));
        pnlSearchWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#94A3B8"), 1, true),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));

        JLabel lblSearchIcon = new JLabel(new MenuIcon("SEARCH"));
        lblSearchIcon.setForeground(Color.GRAY);
        pnlSearchWrapper.add(lblSearchIcon, BorderLayout.WEST);

        String placeholderText = "Tìm tên sản phẩm hoặc mã để thêm vào đơn hàng (Ấn Enter để thêm)...";
        JTextField txtSearch = new JTextField(placeholderText);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setForeground(Color.GRAY);
        txtSearch.setBorder(null); 
        pnlSearchWrapper.add(txtSearch, BorderLayout.CENTER);

        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txtSearch.getText().equals(placeholderText)) {
                    txtSearch.setText("");
                    txtSearch.setForeground(Color.BLACK);
                    lblSearchIcon.setForeground(Color.decode("#1967D2")); 
                    pnlSearchWrapper.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode("#1967D2"), 1, true),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)
                    ));
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtSearch.getText().trim().isEmpty()) {
                    txtSearch.setForeground(Color.GRAY);
                    txtSearch.setText(placeholderText);
                    lblSearchIcon.setForeground(Color.GRAY); 
                    pnlSearchWrapper.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode("#94A3B8"), 1, true),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)
                    ));
                }
            }
        });

        pnl.add(pnlSearchWrapper, BorderLayout.NORTH);

        // --- 2. BẢNG DANH SÁCH SẢN PHẨM ---
        String[] cols = {"Sản phẩm", "ĐVT", "SL", "Đơn giá", "VAT%", "Thành tiền", ""};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2 || column == 3; 
            }
        };

        // BÍ QUYẾT FIX LỖI KHOẢNG TRẮNG: Ghi đè phương thức tính toán chiều cao của JTable
        JTable tbl = new JTable(model) {
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                // Chiều cao tự động = số lượng dòng * chiều cao của 1 dòng (45px)
                // Nếu bảng trống, chiều cao sẽ = 0 (chỉ hiện Header)
                int tableHeight = getRowCount() * getRowHeight();
                return new Dimension(getPreferredSize().width, tableHeight);
            }
        };
        
        tbl.setRowHeight(45); 
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tbl.setShowGrid(false); 
        tbl.setShowHorizontalLines(true); 
        tbl.setGridColor(Color.decode("#F1F3F5"));

        tbl.getTableHeader().setBackground(Color.decode("#D9EAF7")); 
        tbl.getTableHeader().setForeground(Color.decode("#1E293B"));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tbl.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tbl.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        tbl.getColumnModel().getColumn(0).setPreferredWidth(250); 
        tbl.getColumnModel().getColumn(1).setPreferredWidth(60);  
        tbl.getColumnModel().getColumn(2).setPreferredWidth(50);  
        tbl.getColumnModel().getColumn(3).setPreferredWidth(100); 
        tbl.getColumnModel().getColumn(4).setPreferredWidth(50);  
        tbl.getColumnModel().getColumn(5).setPreferredWidth(100); 
        tbl.getColumnModel().getColumn(6).setPreferredWidth(40);  

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tbl.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tbl.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tbl.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tbl.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        
        tbl.getColumnModel().getColumn(5).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSel, hasFocus, r, c);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lbl.setForeground(Color.decode("#111827"));
                lbl.setHorizontalAlignment(JLabel.RIGHT); 
                return lbl;
            }
        });
        
        tbl.getColumnModel().getColumn(6).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSel, hasFocus, r, c);
                lbl.setForeground(Color.decode("#EF4444")); 
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return lbl;
            }
        });

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true)); 
        sp.getViewport().setBackground(Color.WHITE); 
        pnl.add(sp, BorderLayout.CENTER);

        // ========================================================
        // SỰ KIỆN: ẤN ENTER ĐỂ THÊM SẢN PHẨM VÀO BẢNG
        // ========================================================
        txtSearch.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();
            if (!keyword.isEmpty() && !keyword.equals(placeholderText)) {
                model.addRow(new Object[]{
                    keyword, 
                    "Hộp", "1", "25000", "5%", "25.000đ", "🗑"
                });
                
                txtSearch.setText("");
                txtSearch.requestFocus();
                
                // Ép ScrollPane và Table tính toán lại chiều cao sau khi thêm dòng
                sp.revalidate();
                sp.repaint();
            }
        });

        // ========================================================
        // SỰ KIỆN: CLICK VÀO ICON THÙNG RÁC ĐỂ XÓA DÒNG
        // ========================================================
        tbl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tbl.rowAtPoint(e.getPoint());
                int col = tbl.columnAtPoint(e.getPoint());
                
                if (row >= 0 && col == 6) { 
                    model.removeRow(row);
                    
                    // Ép ScrollPane và Table tính toán lại chiều cao sau khi xóa dòng
                    sp.revalidate();
                    sp.repaint();
                }
            }
        });

        return pnl;
    }

    // Panel Mã khuyến mãi
    private JPanel createVoucherPanel() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnl.setBackground(Color.WHITE);
        pnl.add(new JTextField(20));
        JButton btnApDung = new JButton("Áp dụng");
        btnApDung.setBackground(accentBlue); btnApDung.setForeground(Color.WHITE);
        pnl.add(btnApDung);
        return pnl;
    }

    // Panel Tổng kết màu xanh đậm (Hình 2)
    private JPanel createSummaryPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(Color.WHITE);

        // Ô nhập tiền khách đưa (Màu vàng nhạt)
        JPanel pnlTienKhach = new JPanel(new BorderLayout());
        pnlTienKhach.setBackground(Color.decode("#FFFBEB"));
        pnlTienKhach.setBorder(BorderFactory.createLineBorder(Color.decode("#FEF3C7"), 2));
        pnlTienKhach.add(new JLabel(" 💵 Tiền khách đưa:"), BorderLayout.NORTH);
        pnlTienKhach.add(new JTextField(), BorderLayout.CENTER);
        pnl.add(pnlTienKhach, BorderLayout.NORTH);

        // Khối tổng thanh toán xanh đậm
        JPanel pnlFinal = new JPanel(new GridLayout(3, 2, 10, 5));
        pnlFinal.setBackground(darkBlue);
        pnlFinal.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        pnlFinal.add(createWhiteLabel("Tạm tính:")); pnlFinal.add(createWhiteLabel("25.000đ", SwingConstants.RIGHT));
        pnlFinal.add(createWhiteLabel("VAT (5%):")); pnlFinal.add(createWhiteLabel("+1.250đ", SwingConstants.RIGHT));
        
        JLabel lblTotal = new JLabel("TỔNG THANH TOÁN:");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18)); lblTotal.setForeground(Color.WHITE);
        JLabel lblPrice = new JLabel("26.250đ");
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 24)); lblPrice.setForeground(Color.YELLOW);
        lblPrice.setHorizontalAlignment(SwingConstants.RIGHT);

        pnlFinal.add(lblTotal); pnlFinal.add(lblPrice);
        pnl.add(pnlFinal, BorderLayout.CENTER);

        return pnl;
    }

    private JLabel createWhiteLabel(String text) { return createWhiteLabel(text, SwingConstants.LEFT); }
    private JLabel createWhiteLabel(String text, int align) {
        JLabel l = new JLabel(text, align); l.setForeground(Color.WHITE); return l;
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(130, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
    }

    private GridBagConstraints getGbc(int x, int y, double wx) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y; g.weightx = wx; g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 5, 5, 5);
        return g;
    }
}