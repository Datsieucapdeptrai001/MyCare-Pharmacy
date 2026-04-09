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
        setSize(1000, 900);
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
    private JPanel createCustomerPanel() {
        JPanel pnl = new JPanel(new GridBagLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5, 5, 5, 5);

        JTextField txtSearch = new JTextField(" Nhập SĐT hoặc mã KH...");
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.8; pnl.add(txtSearch, gbc);

        JButton btnSearch = new JButton("📞 Tra cứu");
        btnSearch.setBackground(accentBlue); btnSearch.setForeground(Color.WHITE);
        gbc.gridx = 1; gbc.weightx = 0.2; pnl.add(btnSearch, gbc);

        pnl.add(new JTextField(" Tên khách (mặc định: Khách lẻ)"), getGbc(0, 1, 1));
        pnl.add(new JTextField(" Số điện thoại (tuỳ chọn)"), getGbc(1, 1, 1));

        return pnl;
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