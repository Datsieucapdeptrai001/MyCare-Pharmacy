package GUI;

import Components.MenuIcon;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

public class ManHinhChinh extends JPanel {

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JButton btnTongQuan, btnDoiChieu;

    private int[] soLuongTien = new int[9];
    private final long[] menhGia = {500000, 200000, 100000, 50000, 20000, 10000, 5000, 2000, 1000};

    public ManHinhChinh() {
        setLayout(new BorderLayout());
        setBackground(Color.decode("#F4F6F8"));

        JPanel topHeader = createTopHeader();
        add(topHeader, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Color.decode("#F4F6F8"));

        cardPanel.add(createTongQuanPanel(), "TongQuan");
        cardPanel.add(createDoiChieuPanel(), "DoiChieu");

        add(cardPanel, BorderLayout.CENTER);
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#DFE3E8")));

        JPanel leftTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftTabs.setBackground(Color.WHITE);

        // Truyền thêm mã Icon vào hàm tạo nút
        btnTongQuan = createTabButton("Tổng quan", true, "TAB_CHART");
        btnDoiChieu = createTabButton("Đối chiếu doanh thu", false, "TAB_DOLLAR");

        btnTongQuan.addActionListener(e -> {
            setActiveTab(btnTongQuan, btnDoiChieu);
            cardLayout.show(cardPanel, "TongQuan");
        });
        btnDoiChieu.addActionListener(e -> {
            setActiveTab(btnDoiChieu, btnTongQuan);
            cardLayout.show(cardPanel, "DoiChieu");
        });

        leftTabs.add(btnTongQuan);
        leftTabs.add(btnDoiChieu);

        JPanel rightStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightStatus.setBackground(Color.WHITE);

        // Label Ca đang mở
        JLabel lblStatus = new JLabel("Ca đang mở - 1g 17p");
        lblStatus.setIcon(new MenuIcon("DOT_FILL")); // Tự lấy màu xanh lá
        lblStatus.setIconTextGap(8);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(Color.decode("#00A76F"));
        lblStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#00A76F"), 1),
                new EmptyBorder(5, 10, 5, 10)
        ));

        // Nút Kết ca
        JButton btnKetCa = new JButton("Kết ca");
        btnKetCa.setIcon(new MenuIcon("CIRCLE_HOLLOW")); // Tự lấy màu đỏ
        btnKetCa.setIconTextGap(8);
        btnKetCa.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnKetCa.setForeground(Color.decode("#FF5630"));
        btnKetCa.setBackground(Color.WHITE);
        btnKetCa.setFocusPainted(false);
        btnKetCa.addActionListener(e -> openKetCaDialog());

        rightStatus.add(lblStatus);
        rightStatus.add(btnKetCa);

        header.add(leftTabs, BorderLayout.WEST);
        header.add(rightStatus, BorderLayout.EAST);

        return header;
    }

    // Cập nhật hàm tạo nút để nhận thêm tham số iconType
    private JButton createTabButton(String text, boolean isActive, String iconType) {
        JButton btn = new JButton(text);
        btn.setIcon(new MenuIcon(iconType)); // Gắn icon
        btn.setIconTextGap(10);
        btn.setPreferredSize(new Dimension(200, 45)); // Mở rộng xíu cho chữ thoải mái
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createMatteBorder(0, 0, isActive ? 3 : 0, 0, Color.decode("#1A73E8")));
        btn.setForeground(isActive ? Color.decode("#1A73E8") : Color.GRAY);
        return btn;
    }

    private void setActiveTab(JButton active, JButton inactive) {
        active.setForeground(Color.decode("#1A73E8"));
        active.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.decode("#1A73E8")));
        inactive.setForeground(Color.GRAY);
        inactive.setBorder(BorderFactory.createEmptyBorder());
    }

    private JPanel createTongQuanPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.decode("#F4F6F8"));
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel title = new JLabel("<html><h2 style='margin:0;'>MÀN HÌNH CHÍNH</h2><p style='color:gray; margin:0;'>Tổng quan hoạt động — Hôm nay</p></html>");
        pnl.add(title, BorderLayout.NORTH);
        return pnl;
    }

    private JPanel createDoiChieuPanel() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.decode("#F4F6F8"));
        pnl.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel title = new JLabel("<html><h2 style='margin:0;'>ĐỐI CHIẾU DOANH THU</h2><p style='color:gray; margin:0;'>Sổ sách — Tiền mặt — Chuyển khoản</p></html>");
        pnl.add(title, BorderLayout.NORTH);
        return pnl;
    }

    private void openKetCaDialog() {
        soLuongTien = new int[9];

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog((Frame) parentWindow, "Kết ca - Báo cáo & Kiểm kê", true);
        dialog.setSize(900, 750); 
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setLayout(new BorderLayout());

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(Color.decode("#E02327"));
        header.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Gắn icon rỗng màu trắng cho tiêu đề popup luôn cho ngầu
        JLabel headerTitle = new JLabel(" Kết ca — Báo cáo & Kiểm kê (Ca sáng)");
        headerTitle.setIcon(new MenuIcon("CIRCLE_HOLLOW"));
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerTitle.setForeground(Color.WHITE);
        header.add(headerTitle);
        dialog.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 20, 20, 20));

        body.add(createTongKetDoanhThuPanel());
        body.add(Box.createRigidArea(new Dimension(0, 20))); 

        JLabel lblKiemDem = new JLabel("💵 KIỂM ĐẾM TIỀN MẶT THỰC TẾ CUỐI CA");
        lblKiemDem.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKiemDem.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(lblKiemDem);

        JPanel gridTien = new JPanel(new GridLayout(3, 3, 15, 15));
        gridTien.setBackground(Color.WHITE);
        gridTien.setBorder(new EmptyBorder(10, 0, 10, 0));
        gridTien.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTongCach = new JLabel("Tổng cộng: 0đ", SwingConstants.RIGHT);
        lblTongCach.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTongCach.setForeground(Color.WHITE);
        lblTongCach.setOpaque(true);
        lblTongCach.setBackground(Color.decode("#152A4B"));
        lblTongCach.setBorder(new EmptyBorder(10, 15, 10, 15));
        lblTongCach.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTongCach.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        String[] menhGiaStr = {"500.000đ", "200.000đ", "100.000đ", "50.000đ", "20.000đ", "10.000đ", "5.000đ", "2.000đ", "1.000đ"};
        Color[] boxColors = {Color.decode("#1A73E8"), Color.decode("#9C27B0"), Color.decode("#4CAF50"), 
                             Color.decode("#FF9800"), Color.decode("#F44336"), Color.decode("#E91E63"),
                             Color.decode("#E91E63"), Color.decode("#1A73E8"), Color.decode("#607D8B")};

        for (int i = 0; i < menhGiaStr.length; i++) {
            final int index = i; 
            
            JPanel box = new JPanel(new BorderLayout());
            box.setBorder(BorderFactory.createLineBorder(boxColors[i], 1));
            box.setBackground(Color.WHITE);
            box.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
            
            JLabel lblGia = new JLabel(menhGiaStr[i], SwingConstants.CENTER);
            lblGia.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblGia.setForeground(boxColors[i]);
            lblGia.setBorder(new EmptyBorder(5, 0, 5, 0));
            
            JLabel lblCount = new JLabel("0", SwingConstants.CENTER);
            lblCount.setFont(new Font("Segoe UI", Font.BOLD, 18));
            
            box.add(lblGia, BorderLayout.NORTH);
            box.add(lblCount, BorderLayout.CENTER);

            box.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        soLuongTien[index]++; 
                    } else if (SwingUtilities.isRightMouseButton(e)) {
                        if (soLuongTien[index] > 0) soLuongTien[index]--; 
                    }
                    lblCount.setText(String.valueOf(soLuongTien[index]));
                    
                    long tongTien = 0;
                    for (int j = 0; j < 9; j++) {
                        tongTien += soLuongTien[j] * menhGia[j];
                    }
                    lblTongCach.setText("Tổng cộng: " + formatMoney(tongTien));
                }
            });
            gridTien.add(box);
        }
        
        JLabel lblHuongDan = new JLabel("<i>Mẹo: Click chuột trái để thêm 1 tờ, Click chuột phải để bớt 1 tờ</i>");
        lblHuongDan.setForeground(Color.GRAY);
        lblHuongDan.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(lblHuongDan);
        body.add(gridTien);
        body.add(lblTongCach);
        body.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel lblGhiChu = new JLabel("Ghi chú kết ca / Bàn giao:");
        lblGhiChu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblGhiChu.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(lblGhiChu);

        JTextArea txtGhiChu = new JTextArea(3, 20);
        txtGhiChu.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        txtGhiChu.setLineWrap(true);
        JScrollPane scrollGhiChu = new JScrollPane(txtGhiChu);
        scrollGhiChu.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(scrollGhiChu);

        JScrollPane mainScroll = new JScrollPane(body);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(mainScroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(15, 20, 15, 20));

        JButton btnHuy = new JButton("Hủy");
        btnHuy.setBackground(Color.WHITE);
        btnHuy.addActionListener(e -> dialog.dispose());

        JPanel rightFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightFooter.setBackground(Color.WHITE);
        JLabel lblFooterTong = new JLabel("Tổng doanh thu ca: 0đ   ");
        lblFooterTong.setForeground(Color.decode("#00A76F"));
        lblFooterTong.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JButton btnXacNhan = new JButton("Xác nhận kết ca");
        btnXacNhan.setIcon(new MenuIcon("CIRCLE_HOLLOW"));
        btnXacNhan.setBackground(Color.decode("#E02327"));
        btnXacNhan.setForeground(Color.WHITE);

        rightFooter.add(lblFooterTong);
        rightFooter.add(btnXacNhan);

        footer.add(btnHuy, BorderLayout.WEST);
        footer.add(rightFooter, BorderLayout.EAST);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JPanel createTongKetDoanhThuPanel() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBackground(Color.WHITE);
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("$ TỔNG KẾT DOANH THU TRONG CA");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Color.decode("#E02327"));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.add(title);
        pnl.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel row1 = new JPanel(new GridLayout(1, 4, 10, 0));
        row1.setBackground(Color.WHITE);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.add(createStatBox("Số hóa đơn", "0 HĐ", "#E3F2FD", "#1A73E8"));
        row1.add(createStatBox("Tổng doanh thu", "0đ", "#E8F5E9", "#00A76F"));
        row1.add(createStatBox("Tiền mặt", "0đ", "#E8F5E9", "#00A76F"));
        row1.add(createStatBox("Chuyển khoản", "0đ", "#E3F2FD", "#1A73E8"));
        pnl.add(row1);
        pnl.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel row2 = new JPanel(new GridLayout(1, 3, 15, 0));
        row2.setBackground(Color.WHITE);
        row2.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8")),
            new EmptyBorder(10, 15, 10, 15)
        ));
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);

        row2.add(createDetailBox("KHUYẾN MÃI", "Giảm giá KM:", "-0đ", "VAT thu:", "+0đ"));
        row2.add(createDetailBox("QUỸ TIỀN MẶT", "Đầu ca:", "0đ", "Thu trong ca:", "+0đ"));
        
        JPanel tonQuy = new JPanel(new GridLayout(3, 1));
        tonQuy.setBackground(Color.WHITE);
        JLabel lbl1 = new JLabel("TỒN QUỸ DỰ KIẾN", SwingConstants.CENTER);
        lbl1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl1.setForeground(Color.GRAY);
        JLabel lbl2 = new JLabel("0đ", SwingConstants.CENTER);
        lbl2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl2.setForeground(Color.decode("#152A4B"));
        JLabel lbl3 = new JLabel("= Đầu ca + Thu TM", SwingConstants.CENTER);
        lbl3.setForeground(Color.GRAY);
        tonQuy.add(lbl1); tonQuy.add(lbl2); tonQuy.add(lbl3);
        row2.add(tonQuy);

        pnl.add(row2);
        return pnl;
    }

    private JPanel createStatBox(String title, String value, String bgColor, String fgColor) {
        JPanel box = new JPanel(new GridLayout(2, 1));
        box.setBackground(Color.decode(bgColor));
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode(fgColor), 1), 
            new EmptyBorder(10, 0, 10, 0)
        ));
        
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setForeground(Color.GRAY);
        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblValue.setForeground(Color.decode(fgColor));
        
        box.add(lblTitle);
        box.add(lblValue);
        return box;
    }

    private JPanel createDetailBox(String title, String item1, String val1, String item2, String val2) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.WHITE);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(Color.GRAY);
        
        JPanel grid = new JPanel(new GridLayout(2, 2));
        grid.setBackground(Color.WHITE);
        grid.add(new JLabel(item1)); 
        JLabel v1 = new JLabel(val1, SwingConstants.RIGHT);
        v1.setForeground(val1.contains("-") ? Color.decode("#00A76F") : Color.BLACK);
        grid.add(v1);
        
        grid.add(new JLabel(item2)); 
        JLabel v2 = new JLabel(val2, SwingConstants.RIGHT);
        v2.setForeground(val2.contains("+") ? Color.decode("#E02327") : Color.BLACK);
        grid.add(v2);

        box.add(lblTitle, BorderLayout.NORTH);
        box.add(grid, BorderLayout.CENTER);
        return box;
    }

    private String formatMoney(long amount) {
        DecimalFormat formatter = new DecimalFormat("###,###,###đ");
        return formatter.format(amount);
    }
}