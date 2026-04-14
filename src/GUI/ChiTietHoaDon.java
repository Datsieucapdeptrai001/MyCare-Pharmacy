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

import Utils.*;
import java.awt.*;
import java.util.List;

public class ChiTietHoaDon extends JDialog {

    private Color primaryGreen = Color.decode("#009643"); 
    private Color bgLight = Color.decode("#F8F9FA"); 
    private Color bgYellow = Color.decode("#FEF9C3"); 
    private Color textDark = Color.decode("#1F2937"); 
    private Color textGray = Color.decode("#6B7280"); 
    private Color borderGray = Color.decode("#E5E7EB"); 

    private List<Object[]> dsSanPham;
    private String tenNhanVien; 

    // Các biến dùng chung để đồng bộ giữa UI và In Hóa Đơn
    private long tongThanhToanThucTe = 0;
    private long tamTinhThucTe = 0;
    private long vatThucTe = 0;
    private long tienKhachDuaThucTe = 0;
    private long tienThoiThucTe = 0;

    public ChiTietHoaDon(Frame parent, String maHD, String ngay, String khachHang, String sdt, String phuongThuc, String tongTienCu, String tenNhanVien, List<Object[]> dsSanPham) {
        super(parent, "Chi tiết hóa đơn", true);
        this.dsSanPham = dsSanPham;
        this.tenNhanVien = tenNhanVien;

        // --- BƯỚC FIX QUAN TRỌNG: Tự động tính toán lại Tổng tiền 100% chính xác từ Bảng ---
        for (Object[] sp : dsSanPham) {
            try {
                long donGia = Long.parseLong(sp[4].toString().replaceAll("[^0-9]", ""));
                int sl = Integer.parseInt(sp[3].toString());
                long thanhTienCuaSP = Long.parseLong(sp[6].toString().replaceAll("[^0-9]", ""));
                
                long tienChuaVat = donGia * sl;
                tamTinhThucTe += tienChuaVat;
                tongThanhToanThucTe += thanhTienCuaSP;
                vatThucTe += (thanhTienCuaSP - tienChuaVat);
            } catch (Exception e) {}
        }
        
        // --- BƯỚC FIX QUAN TRỌNG: Móc Tiền Khách Đưa từ DB lên ---
        tienKhachDuaThucTe = tongThanhToanThucTe; 
        if (phuongThuc.equals("Tiền mặt")) {
            try {
                DAO.DAO_HoaDon daoHD = new DAO.DAO_HoaDon();
                Entity.HoaDon hd = daoHD.layHoaDonTheoMa(maHD);
                // Tìm đoạn text "CASH:500000" mà lúc Thanh Toán ta đã giấu vào
                if (hd != null && hd.getGhiChu() != null && hd.getGhiChu().startsWith("CASH:")) {
                    tienKhachDuaThucTe = Long.parseLong(hd.getGhiChu().split(":")[1]);
                }
            } catch (Exception e) {}
        }
        
        tienThoiThucTe = tienKhachDuaThucTe - tongThanhToanThucTe;
        if (tienThoiThucTe < 0) tienThoiThucTe = 0;

        String tongTienDungStr = String.format("%,d", tongThanhToanThucTe).replace(',', '.') + "đ";

        setSize(900, 750); 
        setLocationRelativeTo(parent);
        setUndecorated(true); 
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        add(createHeaderPanel(maHD, ngay, khachHang, sdt, phuongThuc, tongTienDungStr), BorderLayout.NORTH); 

        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(15, 20, 15, 20));

        pnlBody.add(createCompanyInfoPanel(maHD, ngay));
        pnlBody.add(Box.createRigidArea(new Dimension(0, 15))); 
        pnlBody.add(createCustomerAndInvoicePanel(khachHang, sdt, phuongThuc)); 
        pnlBody.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlBody.add(createProductTablePanel());
        pnlBody.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlBody.add(createSummaryPanel(phuongThuc)); 
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));
        pnlBody.add(createFooterTextPanel()); 

        JScrollPane scrollPane = new JScrollPane(pnlBody);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel(String maHD, String ngay, String khachHang, String sdt, String phuongThuc, String tongTien) {
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
        lblStatus.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.WHITE, 1, true), new EmptyBorder(2, 6, 2, 6)));
        pnlTitle.add(lblTitle); pnlTitle.add(lblStatus);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.setOpaque(false);
        JButton btnPrint = new JButton("In");
        btnPrint.setIcon(new MenuIcon("PRINT")); 
        btnPrint.setIconTextGap(8); btnPrint.setForeground(Color.WHITE);
        btnPrint.setContentAreaFilled(false); btnPrint.setFocusPainted(false);
        btnPrint.setBorder(new EmptyBorder(4, 12, 4, 12));
        
        btnPrint.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Chọn vị trí lưu hóa đơn");
            fileChooser.setSelectedFile(new File("HoaDon_" + maHD + ".txt"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
                    writer.println("================================================");
                    writer.println("               MYCARE PHARMACY");
                    writer.println("   123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh");
                    writer.println("================================================");
                    writer.println("Số: " + maHD + " | Ngày: " + ngay);
                    writer.println("Nhân viên: " + tenNhanVien); 
                    writer.println("------------------------------------------------");
                    writer.println("KHÁCH HÀNG: " + khachHang + " | SĐT: " + (sdt.isEmpty() ? "Trống" : sdt));
                    writer.println("THANH TOÁN: " + phuongThuc);
                    writer.println("------------------------------------------------");
                    writer.printf("%-5s | %-20s | %-5s | %-10s\n", "SL", "TÊN SẢN PHẨM", "ĐVT", "THÀNH TIỀN");
                    writer.println("------------------------------------------------");
                    
                    for (Object[] sp : dsSanPham) {
                        String tenSP = sp[1].toString().replaceAll("<[^>]*>", "").trim();
                        if (tenSP.length() > 20) {
                            tenSP = tenSP.substring(0, 17) + "..."; 
                        }
                        writer.printf("%-5s | %-20s | %-5s | %-10s\n", sp[3].toString(), tenSP, sp[2].toString(), sp[6].toString());
                    }
                    
                    writer.println("------------------------------------------------");
                    writer.printf("%-35s %s\n", "TỔNG THANH TOÁN:", tongTien);
                    if (phuongThuc.equals("Tiền mặt")) {
                        writer.printf("%-35s %s\n", "TIỀN KHÁCH ĐƯA:", String.format("%,d", tienKhachDuaThucTe).replace(',', '.') + "đ");
                        writer.printf("%-35s %s\n", "TIỀN THỐI LẠI:", String.format("%,d", tienThoiThucTe).replace(',', '.') + "đ");
                    }
                    writer.println("================================================");
                    JOptionPane.showMessageDialog(this, "Đã lưu hóa đơn thành công!");
                } catch (IOException ex) {}
            }
        });
        
        JButton btnClose = new JButton("X");
        btnClose.setForeground(Color.WHITE); btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false); btnClose.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        
        pnlActions.add(btnPrint); pnlActions.add(btnClose);
        pnlHeader.add(pnlTitle, BorderLayout.WEST); pnlHeader.add(pnlActions, BorderLayout.EAST);
        return pnlHeader;
    }

    private JPanel createCompanyInfoPanel(String maHD, String ngay) {
        JPanel pnl = new JPanel(new GridLayout(4, 1, 0, 3)); 
        pnl.setBackground(Color.WHITE);
        JLabel lblName = new JLabel("MYCARE PHARMACY", SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 18)); lblName.setForeground(Color.decode("#152A4B"));
        JLabel lblAddress = new JLabel("123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh", SwingConstants.CENTER);
        lblAddress.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lblAddress.setForeground(textGray);
        JLabel lblContact = new JLabel("Hotline: 1800 6868 · Email: support@mycare.vn", SwingConstants.CENTER);
        lblContact.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lblContact.setForeground(textGray);
        JLabel lblTitleHD = new JLabel("HÓA ĐƠN BÁN HÀNG", SwingConstants.CENTER);
        lblTitleHD.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblTitleHD.setBorder(new EmptyBorder(10, 0, 5, 0));
        JLabel lblDateInfo = new JLabel("Số: " + maHD + " · Ngày: " + ngay, SwingConstants.CENTER);
        lblDateInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lblDateInfo.setForeground(textGray);

        pnl.add(lblName); pnl.add(lblAddress); pnl.add(lblContact);
        JPanel wrapper = new JPanel(new BorderLayout()); wrapper.setBackground(Color.WHITE);
        wrapper.add(pnl, BorderLayout.NORTH);
        
        JPanel pnlTitle = new JPanel(new GridLayout(2, 1)); pnlTitle.setBackground(Color.WHITE);
        pnlTitle.add(lblTitleHD); pnlTitle.add(lblDateInfo);
        wrapper.add(pnlTitle, BorderLayout.CENTER);
        
        JPanel linePanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2d = (Graphics2D) g; g2d.setColor(borderGray);
                g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
                g2d.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            }
        };
        linePanel.setPreferredSize(new Dimension(100, 15)); linePanel.setBackground(Color.WHITE);
        wrapper.add(linePanel, BorderLayout.SOUTH);
        return wrapper;
    }

    private JPanel createCustomerAndInvoicePanel(String khachHang, String sdt, String phuongThuc) { 
        JPanel pnl = new JPanel(new GridLayout(1, 2, 15, 0)); pnl.setBackground(Color.WHITE);
        JPanel pnlKhachHang = createInfoBox("THÔNG TIN KHÁCH HÀNG");
        pnlKhachHang.add(new JLabel("<html><b>" + khachHang + "</b></html>"));
        pnlKhachHang.add(new JLabel("SĐT: " + (sdt.isEmpty() ? "Không cung cấp" : sdt)));

        JPanel pnlHoaDon = createInfoBox("THÔNG TIN HÓA ĐƠN");
        pnlHoaDon.add(new JLabel("<html>Nhân viên: <b>" + this.tenNhanVien + "</b></html>"));
        pnlHoaDon.add(new JLabel("<html>Phương thức: <font color='#009643'><b>" + phuongThuc + "</b></font></html>")); 
        pnl.add(pnlKhachHang); pnl.add(pnlHoaDon); return pnl;
    }

    private JPanel createInfoBox(String title) {
        JPanel box = new JPanel(); box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(bgLight); box.setBorder(BorderFactory.createCompoundBorder(new LineBorder(bgLight, 1, true), new EmptyBorder(10, 10, 10, 10)));
        JLabel lblTitle = new JLabel(title); lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11)); lblTitle.setForeground(Color.decode("#4F46E5")); 
        lblTitle.setBorder(new EmptyBorder(0, 0, 8, 0)); box.add(lblTitle); return box;
    }

    private JPanel createProductTablePanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 5)); pnl.setBackground(Color.WHITE);
        JLabel lblTitle = new JLabel("DANH SÁCH SẢN PHẨM"); lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11)); lblTitle.setForeground(textGray);
        pnl.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"STT", "Tên sản phẩm", "ĐVT", "SL", "Đơn giá", "VAT", "Thành tiền"};
        Object[][] data = new Object[dsSanPham.size()][7];
        for (int i = 0; i < dsSanPham.size(); i++) { data[i] = dsSanPham.get(i); }

        DefaultTableModel model = new DefaultTableModel(data, cols) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(model);
        table.setRowHeight(45); table.setShowGrid(false); table.setIntercellSpacing(new Dimension(0, 0)); table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JTableHeader header = table.getTableHeader(); header.setBackground(Color.decode("#E0F2FE")); header.setFont(new Font("Segoe UI", Font.BOLD, 12)); header.setBorder(null);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer(); center.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer(); right.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(0).setCellRenderer(center); table.getColumnModel().getColumn(2).setCellRenderer(center);
        table.getColumnModel().getColumn(3).setCellRenderer(center); table.getColumnModel().getColumn(4).setCellRenderer(right);
        table.getColumnModel().getColumn(5).setCellRenderer(center); table.getColumnModel().getColumn(6).setCellRenderer(right);
        table.getColumnModel().getColumn(0).setPreferredWidth(30); table.getColumnModel().getColumn(1).setPreferredWidth(220);

        JScrollPane sp = new JScrollPane(table); sp.getViewport().setBackground(Color.WHITE); sp.setBorder(null); sp.setPreferredSize(new Dimension(650, 120)); 
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER); sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pnl.add(sp, BorderLayout.CENTER); return pnl;
    }

    private JPanel createSummaryPanel(String phuongThuc) {
        JPanel pnl = new JPanel(new GridLayout(1, 2, 15, 0)); pnl.setBackground(Color.WHITE);

        JPanel pnlTotal = new JPanel(new GridLayout(3, 2, 0, 8)); pnlTotal.setBackground(Color.WHITE);
        pnlTotal.setBorder(BorderFactory.createCompoundBorder(new LineBorder(borderGray, 1, true), new EmptyBorder(10, 10, 10, 10)));

        pnlTotal.add(new JLabel("Tạm tính:")); pnlTotal.add(createRightAlignLabel(String.format("%,d", tamTinhThucTe).replace(',', '.') + "đ"));
        pnlTotal.add(new JLabel("VAT:")); pnlTotal.add(createRightAlignLabel("+" + String.format("%,d", vatThucTe).replace(',', '.') + "đ"));
        JLabel lblTotalText = new JLabel("TỔNG THANH TOÁN:"); lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel lblTotalAmount = createRightAlignLabel(String.format("%,d", tongThanhToanThucTe).replace(',', '.') + "đ"); 
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblTotalAmount.setForeground(primaryGreen);
        pnlTotal.add(lblTotalText); pnlTotal.add(lblTotalAmount);

        JPanel pnlPayment = new JPanel(new GridLayout(3, 2, 0, 8)); pnlPayment.setBackground(bgYellow);
        pnlPayment.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.decode("#FDE047"), 1, true), new EmptyBorder(10, 10, 10, 10)));

        JLabel lblPayTitle = new JLabel("THANH TOÁN " + phuongThuc.toUpperCase());
        lblPayTitle.setFont(new Font("Segoe UI", Font.BOLD, 13)); lblPayTitle.setForeground(Color.decode("#CA8A04"));
        pnlPayment.add(lblPayTitle); pnlPayment.add(new JLabel(""));
        
        if(phuongThuc.equalsIgnoreCase("Chuyển khoản")) {
            JLabel lblStatusText = new JLabel("Trạng thái:");
            JLabel lblStatusValue = createRightAlignLabel("Đã nhận chuyển khoản");
            lblStatusValue.setForeground(primaryGreen); lblStatusValue.setFont(new Font("Segoe UI", Font.BOLD, 12));
            pnlPayment.add(lblStatusText); pnlPayment.add(lblStatusValue);
            pnlPayment.add(new JLabel("")); pnlPayment.add(new JLabel("")); 
        } else {
            JLabel lblKhachDuaText = new JLabel("Tiền KH đưa:");
            JLabel lblKhachDuaValue = createRightAlignLabel(String.format("%,d", tienKhachDuaThucTe).replace(',', '.') + "đ");
            lblKhachDuaValue.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblKhachDuaValue.setForeground(primaryGreen);

            JLabel lblThoiLaiText = new JLabel("Tiền thối lại:");
            lblThoiLaiText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblThoiLaiText.setForeground(textGray);
            JLabel lblThoiLaiValue = createRightAlignLabel(String.format("%,d", tienThoiThucTe).replace(',', '.') + "đ");
            lblThoiLaiValue.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblThoiLaiValue.setForeground(textGray);

            pnlPayment.add(lblKhachDuaText); pnlPayment.add(lblKhachDuaValue);
            pnlPayment.add(lblThoiLaiText); pnlPayment.add(lblThoiLaiValue);
        }

        pnl.add(pnlTotal); pnl.add(pnlPayment); return pnl;
    }

    private JLabel createRightAlignLabel(String text) { JLabel label = new JLabel(text); label.setHorizontalAlignment(SwingConstants.RIGHT); return label; }

    private JPanel createFooterTextPanel() {
        JPanel pnl = new JPanel(new GridLayout(3, 1, 0, 2)); pnl.setBackground(Color.WHITE);
        JLabel l1 = new JLabel("Cảm ơn quý khách đã tin dùng MYCARE PHARMACY!", SwingConstants.CENTER); l1.setFont(new Font("Segoe UI", Font.PLAIN, 11)); l1.setForeground(textGray);
        JLabel l2 = new JLabel("Chính sách đổi trả trong vòng 3 ngày kể từ ngày mua", SwingConstants.CENTER); l2.setFont(new Font("Segoe UI", Font.PLAIN, 11)); l2.setForeground(textGray);
        JLabel l3 = new JLabel("NV xác nhận: " + this.tenNhanVien, SwingConstants.CENTER); 
        l3.setFont(new Font("Segoe UI", Font.PLAIN, 11)); l3.setForeground(textGray);
        pnl.add(l1); pnl.add(l2); pnl.add(l3); return pnl;
    }
}