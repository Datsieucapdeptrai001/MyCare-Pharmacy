package GUI;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

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
        setSize(800, 700);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // --- 1. HEADER (Thanh tiêu đề xanh đậm) ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(darkBlue);
        pnlHeader.setPreferredSize(new Dimension(0, 50));
        
        JLabel lblTitle = new JLabel("  🛒 Tạo hóa đơn bán hàng mới");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        
        JButton btnExit = new JButton("✕ ");
        btnExit.setForeground(Color.WHITE);
        btnExit.setBackground(darkBlue);
        btnExit.setBorderPainted(false);
        btnExit.setFocusPainted(false);
        btnExit.addActionListener(e -> dispose());
        pnlHeader.add(btnExit, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // --- 2. BODY (Nội dung chính trong ScrollPane) ---
        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(10, 25, 10, 25));

        // Phần 1: Tra cứu khách hàng
        pnlBody.add(createSectionPanel("👤 Tra cứu khách hàng (tuỳ chọn)", createCustomerPanel()));
        pnlBody.add(Box.createRigidArea(new Dimension(0, 15)));

        // Phần 2: Thêm sản phẩm
        pnlBody.add(createSectionPanel("📦 Thêm sản phẩm", createProductPanel()));
        pnlBody.add(Box.createRigidArea(new Dimension(0, 15)));

        // Phần 3: Khuyến mãi & Thanh toán (Hình ảnh 2)
        pnlBody.add(createSectionPanel("🏷 Mã khuyến mãi", createVoucherPanel()));
        pnlBody.add(Box.createRigidArea(new Dimension(0, 15)));
        
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

    // Hàm tạo khung bao quanh mỗi Section
    private JPanel createSectionPanel(String title, JPanel content) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(borderColor, 1, true), 
            title, TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 13), Color.GRAY
        ));
        pnl.add(content, BorderLayout.CENTER);
        return pnl;
    }

    // Panel Khách hàng
 // ========================================================
    // PANEL KHÁCH HÀNG (FIX THEO ẢNH CHUẨN)
    // ========================================================
    private JPanel createCustomerPanel() {
        // Tạo vỏ bọc ngoài cùng có viền xám nhạt bo góc
        JPanel pnlWrapper = new JPanel(new BorderLayout(0, 15));
        pnlWrapper.setBackground(Color.WHITE);
        pnlWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        // --- 1. HEADER KHÁCH HÀNG ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlTitle.setOpaque(false);
        JLabel lblIcon = new JLabel("👤"); 
        lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
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

        // Nhãn "Khách lẻ" góc phải
        JLabel lblBadge = new JLabel("Khách lẻ", SwingConstants.CENTER);
        lblBadge.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblBadge.setForeground(Color.decode("#4B5563"));
        lblBadge.setBackground(Color.decode("#F3F4F6"));
        lblBadge.setOpaque(true);
        lblBadge.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

        pnlHeader.add(pnlTitle, BorderLayout.WEST);
        pnlHeader.add(lblBadge, BorderLayout.EAST);

        // --- 2. BODY NHẬP LIỆU ---
        JPanel pnlInput = new JPanel(new GridBagLayout());
        pnlInput.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 10, 10);

        // Hàng 1: Ô tìm kiếm + Nút Tra cứu
        JTextField txtSearch = createStyledTextField(" Nhập SĐT hoặc mã KH để liên kết điểm thưởng...");
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.8; 
        pnlInput.add(txtSearch, gbc);

        JButton btnSearch = new JButton("📞 Tra cứu");
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.setBackground(Color.decode("#1967D2"));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFocusPainted(false);
        btnSearch.setOpaque(true);
        btnSearch.setContentAreaFilled(true);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridx = 1; gbc.weightx = 0.2; gbc.insets = new Insets(0, 0, 10, 0);
        pnlInput.add(btnSearch, gbc);

        // Hàng 2: Tên khách + SĐT
        gbc.insets = new Insets(0, 0, 0, 15); // Lề phải 15px cho ô Tên khách
        JTextField txtName = createStyledTextField(" Tên khách (bỏ trống = Khách lẻ)");
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.5;
        pnlInput.add(txtName, gbc);

        JTextField txtPhone = createStyledTextField(" Số điện thoại (tuỳ chọn)");
        gbc.gridx = 1; gbc.weightx = 0.5; gbc.insets = new Insets(0, 0, 0, 0);
        pnlInput.add(txtPhone, gbc);

        pnlWrapper.add(pnlHeader, BorderLayout.NORTH);
        pnlWrapper.add(pnlInput, BorderLayout.CENTER);

        return pnlWrapper;
    }

    // Hàm hỗ trợ tạo TextField đẹp (Tự ẩn chữ khi click)
    private JTextField createStyledTextField(String placeholder) {
        JTextField txt = new JTextField(placeholder);
        txt.setPreferredSize(new Dimension(0, 42)); // Tăng chiều cao lên 42px
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

    // Panel Sản phẩm & Bảng
    private JPanel createProductPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextField txtSp = new JTextField(" 🔍 Tìm tên sản phẩm...");
        txtSp.setPreferredSize(new Dimension(0, 35));
        pnl.add(txtSp, BorderLayout.NORTH);

        String[] cols = {"Sản phẩm", "ĐVT", "SL", "Đơn giá", "VAT%", "Thành tiền", ""};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        model.addRow(new Object[]{"Paracetamol 500mg", "Hộp", 1, "25.000", "5%", "25.000đ", "🗑"});
        JTable tbl = new JTable(model);
        tbl.setRowHeight(35);
        pnl.add(new JScrollPane(tbl), BorderLayout.CENTER);

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