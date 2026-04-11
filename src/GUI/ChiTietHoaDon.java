package GUI;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import Components.*;

public class ChiTietHoaDon extends JDialog {

    private Color primaryGreen = Color.decode("#009643"); 
    private Color bgLight = Color.decode("#F8F9FA"); 
    private Color bgYellow = Color.decode("#FEF9C3"); 
    private Color textDark = Color.decode("#1F2937"); 
    private Color textGray = Color.decode("#6B7280"); 
    private Color borderGray = Color.decode("#E5E7EB"); 

    // ĐÃ SỬA: Thêm tham số String phuongThuc vào đây
    public ChiTietHoaDon(Frame parent, String maHD, String phuongThuc) {
        super(parent, "Chi tiết hóa đơn", true);
        setSize(700, 650); 
        setLocationRelativeTo(parent);
        setUndecorated(true); 
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Header
        add(createHeaderPanel(maHD, phuongThuc), BorderLayout.NORTH); // Truyền phuongThuc vào để in file

        // Body
        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(15, 20, 15, 20));

        pnlBody.add(createCompanyInfoPanel(maHD));
        pnlBody.add(Box.createRigidArea(new Dimension(0, 15))); 
        pnlBody.add(createCustomerAndInvoicePanel(phuongThuc));
        pnlBody.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlBody.add(createProductTablePanel());
        pnlBody.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlBody.add(createSummaryPanel(phuongThuc)); // Truyền phuongThuc vào để đổi tiêu đề panel Vàng
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));
        pnlBody.add(createFooterTextPanel());

        JScrollPane scrollPane = new JScrollPane(pnlBody);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ĐÃ SỬA: Nhận thêm phuongThuc để xuất ra file
    private JPanel createHeaderPanel(String maHD, String phuongThuc) {
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(primaryGreen);
        pnlHeader.setBorder(new EmptyBorder(8, 15, 8, 15)); 

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlTitle.setOpaque(false);
        
        JLabel lblTitle = new JLabel("Hóa đơn bán hàng — " + maHD);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblStatus = new JLabel("Hoàn thành");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(Color.WHITE);
        lblStatus.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.WHITE, 1, true),
                new EmptyBorder(2, 6, 2, 6)
        ));

        pnlTitle.add(lblTitle);
        pnlTitle.add(lblStatus);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.setOpaque(false);
        JButton btnPrint = new JButton("In");
        btnPrint.setIcon(new MenuIcon("PRINT")); 
        btnPrint.setIconTextGap(8); 
        btnPrint.setForeground(Color.WHITE);
        
        btnPrint.setContentAreaFilled(false); 
        btnPrint.setOpaque(false); 
        
        btnPrint.setFocusPainted(false);
        btnPrint.setBorder(new EmptyBorder(4, 12, 4, 12));
        btnPrint.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Chọn vị trí lưu hóa đơn");
            fileChooser.setSelectedFile(new File("HoaDon_" + maHD + ".txt"));

            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();

                try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
                    writer.println("================================================");
                    writer.println("               MYCARE PHARMACY");
                    writer.println("   123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh");
                    writer.println("   Hotline: 1800 6868 - Email: support@mycare.vn");
                    writer.println("================================================");
                    writer.println("HÓA ĐƠN BÁN HÀNG");
                    writer.println("Số: " + maHD);
                    writer.println("Ngày: 07/04/2026");
                    writer.println("------------------------------------------------");
                    writer.println("THÔNG TIN KHÁCH HÀNG:");
                    writer.println("- Tên KH: Nguyễn Văn An");
                    writer.println("- SĐT: 0912345678");
                    writer.println("\nTHÔNG TIN HÓA ĐƠN:");
                    writer.println("- Nhân viên: Nguyễn Thu Hà");
                    
                    // ĐÃ SỬA: Sử dụng biến phuongThuc thay vì gõ cứng
                    writer.println("- Phương thức: " + phuongThuc);
                    
                    writer.println("------------------------------------------------");
                    writer.println("DANH SÁCH SẢN PHẨM:");
                    writer.printf("%-5s | %-20s | %-5s | %-10s\n", "SL", "TÊN SẢN PHẨM", "ĐVT", "THÀNH TIỀN");
                    writer.println("------------------------------------------------");
                    
                    writer.printf("%-5s | %-20s | %-5s | %-10s\n", "2", "Paracetamol 500mg", "Hộp", "50.000đ");
                    writer.printf("%-5s | %-20s | %-5s | %-10s\n", "1", "Vitamin C 1000mg", "Hộp", "45.000đ");
                    
                    writer.println("------------------------------------------------");
                    writer.printf("%-35s %s\n", "Tạm tính:", "95.000đ");
                    writer.printf("%-35s %s\n", "VAT (10%):", "+9.000đ");
                    writer.println("================================================");
                    writer.printf("%-35s %s\n", "TỔNG THANH TOÁN:", "104.000đ");
                    writer.println("================================================");
                    
                    // Cập nhật text in tùy theo phương thức thanh toán
                    if(phuongThuc.equalsIgnoreCase("Chuyển khoản")) {
                        writer.printf("%-35s %s\n", "Trạng thái:", "Đã nhận chuyển khoản");
                    } else {
                        writer.printf("%-35s %s\n", "Tiền KH đưa:", "Không ghi nhận");
                        writer.printf("%-35s %s\n", "Tiền thối lại:", "--");
                    }
                    
                    writer.println("\nCảm ơn quý khách đã tin dùng MYCARE PHARMACY!");
                    writer.println("Chính sách đổi trả 3 ngày (trừ thuốc kê đơn).");

                    JOptionPane.showMessageDialog(this, "Đã lưu hóa đơn thành công tại:\n" + fileToSave.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi lưu file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JButton btnClose = new JButton("X");
        btnClose.setForeground(Color.WHITE);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        pnlActions.add(btnPrint);
        pnlActions.add(btnClose);
        pnlHeader.add(pnlTitle, BorderLayout.WEST);
        pnlHeader.add(pnlActions, BorderLayout.EAST);
        return pnlHeader;
    }

    private JPanel createCompanyInfoPanel(String maHD) {
        JPanel pnl = new JPanel(new GridLayout(4, 1, 0, 3)); 
        pnl.setBackground(Color.WHITE);
        
        JLabel lblName = new JLabel("MYCARE PHARMACY", SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 18)); 
        lblName.setForeground(Color.decode("#152A4B"));

        JLabel lblAddress = new JLabel("123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh", SwingConstants.CENTER);
        lblAddress.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAddress.setForeground(textGray);

        JLabel lblContact = new JLabel("Hotline: 1800 6868 · Email: support@mycare.vn", SwingConstants.CENTER);
        lblContact.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblContact.setForeground(textGray);

        JLabel lblTitleHD = new JLabel("HÓA ĐƠN BÁN HÀNG", SwingConstants.CENTER);
        lblTitleHD.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitleHD.setBorder(new EmptyBorder(10, 0, 5, 0));

        JLabel lblDateInfo = new JLabel("Số: " + maHD + " · Ngày: 07/04/2026", SwingConstants.CENTER);
        lblDateInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDateInfo.setForeground(textGray);

        pnl.add(lblName);
        pnl.add(lblAddress);
        pnl.add(lblContact);
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(pnl, BorderLayout.NORTH);
        
        JPanel pnlTitle = new JPanel(new GridLayout(2, 1));
        pnlTitle.setBackground(Color.WHITE);
        pnlTitle.add(lblTitleHD);
        pnlTitle.add(lblDateInfo);
        
        wrapper.add(pnlTitle, BorderLayout.CENTER);
        
        JPanel linePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(borderGray);
                Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0);
                g2d.setStroke(dashed);
                g2d.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        linePanel.setPreferredSize(new Dimension(100, 15));
        linePanel.setBackground(Color.WHITE);
        wrapper.add(linePanel, BorderLayout.SOUTH);

        return wrapper;
    }

    private JPanel createCustomerAndInvoicePanel(String phuongThuc) { 
        JPanel pnl = new JPanel(new GridLayout(1, 2, 15, 0)); 
        pnl.setBackground(Color.WHITE);

        JPanel pnlKhachHang = createInfoBox("THÔNG TIN KHÁCH HÀNG");
        pnlKhachHang.add(new JLabel("<html><b>Nguyễn Văn An</b></html>"));
        pnlKhachHang.add(new JLabel("SĐT: 0912345678"));

        JPanel pnlHoaDon = createInfoBox("THÔNG TIN HÓA ĐƠN");
        pnlHoaDon.add(new JLabel("<html>Nhân viên: <b>Nguyễn Thu Hà</b></html>"));
        
        pnlHoaDon.add(new JLabel("<html>Phương thức: <font color='#009643'><b>" + phuongThuc + "</b></font></html>")); 

        pnl.add(pnlKhachHang);
        pnl.add(pnlHoaDon);
        return pnl;
    }

    private JPanel createInfoBox(String title) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(bgLight);
        box.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(bgLight, 1, true),
                new EmptyBorder(10, 10, 10, 10) 
        ));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(Color.decode("#4F46E5")); 
        lblTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        box.add(lblTitle);

        return box;
    }

    private JPanel createProductTablePanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("DANH SÁCH SẢN PHẨM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(textGray);
        pnl.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"STT", "Tên sản phẩm", "ĐVT", "SL", "Đơn giá", "VAT", "Thành tiền"};
        Object[][] data = {
            {"1", "<html>Paracetamol 500mg<br><font color='#6B7280'>Thuốc không kê đơn</font></html>", "Hộp", "2", "25.000đ", "5%", "50.000đ"},
            {"2", "<html>Vitamin C 1000mg<br><font color='#6B7280'>TPCN</font></html>", "Hộp", "1", "45.000đ", "10%", "45.000đ"}
        };

        DefaultTableModel model = new DefaultTableModel(data, cols) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(45); 
        table.setShowGrid(false); 
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.decode("#E0F2FE"));
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(null);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(650, 120)); 

        pnl.add(scrollPane, BorderLayout.CENTER);
        return pnl;
    }

    // ĐÃ SỬA: Đổi tiêu đề Panel Vàng thành tên phương thức tương ứng
    private JPanel createSummaryPanel(String phuongThuc) {
        JPanel pnl = new JPanel(new GridLayout(1, 2, 15, 0));
        pnl.setBackground(Color.WHITE);

        JPanel pnlTotal = new JPanel(new GridLayout(3, 2, 0, 8));
        pnlTotal.setBackground(Color.WHITE);
        pnlTotal.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderGray, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        pnlTotal.add(new JLabel("Tạm tính:"));
        pnlTotal.add(createRightAlignLabel("95.000đ"));
        pnlTotal.add(new JLabel("VAT:"));
        pnlTotal.add(createRightAlignLabel("+9.000đ"));
        
        JLabel lblTotalText = new JLabel("TỔNG THANH TOÁN:");
        lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel lblTotalAmount = createRightAlignLabel("104.000đ");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalAmount.setForeground(primaryGreen);
        
        pnlTotal.add(lblTotalText);
        pnlTotal.add(lblTotalAmount);

        JPanel pnlPayment = new JPanel(new GridLayout(3, 2, 0, 8));
        pnlPayment.setBackground(bgYellow);
        pnlPayment.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#FDE047"), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        // Nếu là chuyển khoản thì đổi chữ "THANH TOÁN TIỀN MẶT" thành "THANH TOÁN CHUYỂN KHOẢN"
        JLabel lblPayTitle = new JLabel("THANH TOÁN " + phuongThuc.toUpperCase());
        lblPayTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPayTitle.setForeground(Color.decode("#CA8A04"));

        pnlPayment.add(lblPayTitle);
        pnlPayment.add(new JLabel(""));
        
        // Ẩn hiển thị thối tiền nếu là chuyển khoản
        if(phuongThuc.equalsIgnoreCase("Chuyển khoản")) {
            JLabel lblStatusText = new JLabel("Trạng thái:");
            JLabel lblStatusValue = createRightAlignLabel("Đã nhận đủ");
            lblStatusValue.setForeground(primaryGreen);
            lblStatusValue.setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            pnlPayment.add(lblStatusText);
            pnlPayment.add(lblStatusValue);
            pnlPayment.add(new JLabel("")); // ô trống
            pnlPayment.add(new JLabel("")); // ô trống
        } else {
            JLabel lblKhachDuaText = new JLabel("Tiền KH đưa:");
            JLabel lblKhachDuaValue = createRightAlignLabel("Không ghi nhận");
            lblKhachDuaValue.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            lblKhachDuaValue.setForeground(textGray);

            JLabel lblThoiLaiText = new JLabel("Tiền thối lại:");
            lblThoiLaiText.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblThoiLaiText.setForeground(Color.RED);
            JLabel lblThoiLaiValue = createRightAlignLabel("--");
            lblThoiLaiValue.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblThoiLaiValue.setForeground(Color.RED);

            pnlPayment.add(lblKhachDuaText);
            pnlPayment.add(lblKhachDuaValue);
            pnlPayment.add(lblThoiLaiText);
            pnlPayment.add(lblThoiLaiValue);
        }

        pnl.add(pnlTotal);
        pnl.add(pnlPayment);

        return pnl;
    }

    private JLabel createRightAlignLabel(String text) {
        JLabel label = new JLabel(text);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    private JPanel createFooterTextPanel() {
        JPanel pnl = new JPanel(new GridLayout(3, 1, 0, 2));
        pnl.setBackground(Color.WHITE);
        
        JLabel l1 = new JLabel("Cảm ơn quý khách đã tin dùng MYCARE PHARMACY!", SwingConstants.CENTER);
        l1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l1.setForeground(textGray);

        JLabel l2 = new JLabel("Chính sách đổi trả trong vòng 3 ngày kể từ ngày mua (trừ thuốc kê đơn)", SwingConstants.CENTER);
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l2.setForeground(textGray);
        
        JLabel l3 = new JLabel("NV xác nhận: Nguyễn Thu Hà", SwingConstants.CENTER);
        l3.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l3.setForeground(textGray);

        pnl.add(l1);
        pnl.add(l2);
        pnl.add(l3);

        return pnl;
    }
}