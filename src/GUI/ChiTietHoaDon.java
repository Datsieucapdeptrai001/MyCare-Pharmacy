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
import java.util.ArrayList; // Thêm import này
import java.util.List;
import ConnectDB.ConnectDB; // Thêm import này

public class ChiTietHoaDon extends JDialog {
    private String bacSi = "";
    private String coSo = "";
    private String chuanDoan = "";
    private Color primaryGreen = Color.decode("#009643"); 
    private Color bgLight = Color.decode("#F8F9FA"); 
    private Color bgYellow = Color.decode("#FEF9C3"); 
    private Color textDark = Color.decode("#1F2937"); 
    private Color textGray = Color.decode("#6B7280"); 
    private Color borderGray = Color.decode("#E5E7EB"); 

    private List<Object[]> dsSanPham;
    private String tenNhanVien; 

    private long tongThanhToanThucTe = 0;
    private long tamTinhThucTe = 0;
    private long vatThucTe = 0;
    private long tienKhachDuaThucTe = 0;
    private long tienThoiThucTe = 0;
    private long tienGiamGiaThucTe = 0;
    
    private long tienGiamTuDiemThucTe = 0; 
    private int diemHienTai = 0;
    private boolean isDaHuy = false; 
    private Color bgRed = Color.decode("#FEF2F2"); 
    private Color textRed = Color.decode("#DC2626"); 

    public ChiTietHoaDon(Frame parent, String maHD, String ngay, String khachHang, String sdt, String phuongThuc, String tongTienCu, String tenNhanVien, List<Object[]> dsSanPham) {
        super(parent, "Chi tiết hóa đơn", true);
        
        // --- FIX 1: Chuyển sang ArrayList để có thể add thêm quà tặng ---
        this.dsSanPham = new ArrayList<>(dsSanPham);
        this.tenNhanVien = tenNhanVien;

        long targetGross = 0;
        try { targetGross = Long.parseLong(tongTienCu.replaceAll("[^0-9]", "")); } catch (Exception e) {}

        boolean hasKhuyenMai = false;
        String ghiChuGoc = ""; // Lưu lại ghi chú để xử lý quà

        try {
            BUS.BUS_KhachHang busKH = new BUS.BUS_KhachHang();
            Entity.KhachHang kh = busKH.getKhachHangTheoSDT(sdt);
            if (kh != null) this.diemHienTai = kh.getDiemTichLuy();

            BUS.BUS_HoaDon busHD = new BUS.BUS_HoaDon();
            Entity.HoaDon hd = busHD.layHoaDonTheoMa(maHD);
            
            if (hd != null) {
                if (hd.getNhanVienId() != null && hd.getNhanVienId().getHoVaTen() != null) {
                    this.tenNhanVien = hd.getNhanVienId().getHoVaTen();
                }
                ghiChuGoc = hd.getGhiChu() != null ? hd.getGhiChu() : "";
                if (ghiChuGoc.contains("Đã hủy")) {
                    this.isDaHuy = true;
                    this.primaryGreen = Color.decode("#EF4444");
                    this.bgLight = Color.decode("#FEE2E2");
                }
                if (ghiChuGoc.contains("KM:")) hasKhuyenMai = true;

                String[] parts = ghiChuGoc.split("\\|");
                for (String p : parts) {
                    p = p.trim();
                    if (p.startsWith("CASH:")) {
                        this.tienKhachDuaThucTe = Long.parseLong(p.substring(5).replaceAll("[^0-9]", ""));
                    } else if (p.startsWith("Dùng điểm: -")) {
                        this.tienGiamTuDiemThucTe = Long.parseLong(p.substring(12).replaceAll("[^0-9]", ""));
                    } else if (p.startsWith("BS:")) bacSi = p.substring(3).trim();
                    else if (p.startsWith("CS:")) coSo = p.substring(3).trim();
                    else if (p.startsWith("CD:")) chuanDoan = p.substring(3).trim();
                }
            }
        } catch (Exception e) {}

        // --- FIX 2: TÁI HIỆN QUÀ TẶNG "🎁" DỰA TRÊN GHI CHÚ ---
        if (hasKhuyenMai && ghiChuGoc.contains("KM:")) {
            try (java.sql.Connection con = ConnectDB.getInstance().getConnection()) {
                // Lấy đoạn chứa mã KM (VD: KM001, KM002)
                String kmPart = ghiChuGoc.substring(ghiChuGoc.indexOf("KM:") + 3).split("\\|")[0].trim();
                String[] codes = kmPart.split(",\\s*");

                for (String code : codes) {
                    String sql = "SELECT h.giaTri, d.giaTri FROM KhuyenMai k " +
                                 "JOIN HinhThucKhuyenMai h ON k.id = h.khuyenMaiId " +
                                 "JOIN DieuKienKhuyenMai d ON k.id = d.khuyenMaiId " +
                                 "WHERE k.id = ? AND h.loaiHinhThuc LIKE '%TANG%'";
                    
                    try (java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
                        pst.setString(1, code);
                        try (java.sql.ResultSet rs = pst.executeQuery()) {
                            if (rs.next()) {
                                long slTangGoc = (long) rs.getDouble(1);
                                long dieuKien = (long) rs.getDouble(2);
                                
                                int tongSLThuc = 0;
                                for(Object[] r : dsSanPham) tongSLThuc += Integer.parseInt(r[3].toString());
                                
                                long slDuocTang = (dieuKien > 0) ? (tongSLThuc / dieuKien) * (slTangGoc > 0 ? slTangGoc : 1) : 0;
                                
                                if (slDuocTang > 0 && !this.dsSanPham.isEmpty()) {
                                    // Thêm vào danh sách hiển thị
                                    Object[] itemGoc = this.dsSanPham.get(0);
                                    this.dsSanPham.add(new Object[]{
                                        this.dsSanPham.size() + 1, "🎁 " + itemGoc[1], itemGoc[2], String.valueOf(slDuocTang), "0đ", "0%", "0đ"
                                    });
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) { System.err.println("Lỗi tái hiện quà: " + e.getMessage()); }
        }

        // Tính toán tổng tiền (Giữ nguyên thuật toán của bạn)
        this.tamTinhThucTe = 0;
        this.vatThucTe = 0;
        for (Object[] sp : this.dsSanPham) {
            long donGia = Long.parseLong(sp[4].toString().replaceAll("[^0-9]", ""));
            int sl = Integer.parseInt(sp[3].toString());
            double vatPercent = 0.0;
            try { vatPercent = Double.parseDouble(sp[5].toString().replace("%", "").trim()); } catch(Exception ex) {}
            long tienChuaVat = donGia * sl; 
            long tienVat = Math.round(tienChuaVat * (vatPercent / 100.0));
            sp[4] = String.format("%,d", donGia).replace(',', '.') + "đ";
            sp[6] = String.format("%,d", tienChuaVat).replace(',', '.') + "đ";
            this.tamTinhThucTe += tienChuaVat;
            this.vatThucTe += tienVat;
        }

        this.tongThanhToanThucTe = this.tamTinhThucTe + this.vatThucTe - this.tienGiamTuDiemThucTe;
        if (hasKhuyenMai && targetGross > 0 && targetGross < this.tongThanhToanThucTe) {
            this.tienGiamGiaThucTe = this.tongThanhToanThucTe - targetGross;
            this.tongThanhToanThucTe = targetGross;
        }

        if (this.tienKhachDuaThucTe == 0) this.tienKhachDuaThucTe = this.tongThanhToanThucTe;
        this.tienThoiThucTe = this.tienKhachDuaThucTe - this.tongThanhToanThucTe;
        if (this.tienThoiThucTe < 0) this.tienThoiThucTe = 0;

        // --- GIAO DIỆN ---
        String tongTienDungStr = String.format("%,d", this.tongThanhToanThucTe).replace(',', '.') + "đ";
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
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    // --- GIỮ NGUYÊN CÁC HÀM GIAO DIỆN BÊN DƯỚI CỦA BẠN ---
    // (Bao gồm createHeaderPanel, createCompanyInfoPanel, createProductTablePanel, v.v...)
    // ... Copy các hàm private từ file cũ của bạn dán vào đây ...
    // (Tôi đã tối ưu logic bên trên, các hàm vẽ giao diện sẽ tự động nhận diện dsSanPham mới)

    private JPanel createHeaderPanel(String maHD, String ngay, String khachHang, String sdt, String phuongThuc, String tongTien) {
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(isDaHuy ? textRed : primaryGreen);
        pnlHeader.setBorder(new EmptyBorder(8, 15, 8, 15)); 
        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlTitle.setOpaque(false);
        String title = isDaHuy ? "Hóa đơn đã hủy — " : "Hóa đơn bán hàng — ";
        JLabel lblTitle = new JLabel(title + maHD);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        JLabel lblStatus = new JLabel(isDaHuy ? "Đã hủy" : "Hoàn thành");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatus.setForeground(isDaHuy ? textRed : primaryGreen); 
        lblStatus.setBackground(Color.WHITE);
        lblStatus.setOpaque(true);
        lblStatus.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.WHITE, 1, true), new EmptyBorder(2, 6, 2, 6)));
        pnlTitle.add(lblTitle); pnlTitle.add(lblStatus);
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.setOpaque(false);
        JButton btnPrint = new JButton("In");
        btnPrint.setIcon(new MenuIcon("PRINT")); 
        btnPrint.setIconTextGap(8); btnPrint.setForeground(Color.WHITE);
        btnPrint.setContentAreaFilled(false); btnPrint.setFocusPainted(false);
        btnPrint.setBorder(new EmptyBorder(4, 12, 4, 12));
        
        // Listener cho nút In (Giữ nguyên của bạn)
        btnPrint.addActionListener(e -> { /* Code in của bạn */ });

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
        JPanel pnl = new JPanel(new GridLayout(4, 1, 0, 3)); pnl.setBackground(Color.WHITE);
        JLabel lblName = new JLabel("MYCARE PHARMACY", SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 18)); lblName.setForeground(Color.decode("#152A4B"));
        JLabel lblAddress = new JLabel("123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh", SwingConstants.CENTER);
        lblAddress.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lblAddress.setForeground(textGray);
        JLabel lblContact = new JLabel("Hotline: 1800 6868 · Email: support@mycare.vn", SwingConstants.CENTER);
        lblContact.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lblContact.setForeground(textGray);
        String titleText = isDaHuy ? "HÓA ĐƠN ĐÃ HỦY" : "HÓA ĐƠN BÁN HÀNG";
        JLabel lblTitleHD = new JLabel(titleText, SwingConstants.CENTER);
        lblTitleHD.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblTitleHD.setForeground(isDaHuy ? textRed : Color.decode("#152A4B"));
        lblTitleHD.setBorder(new EmptyBorder(10, 0, 5, 0));
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
        int cols = (bacSi != null && !bacSi.isEmpty()) ? 3 : 2;
        JPanel pnl = new JPanel(new GridLayout(1, cols, 15, 0)); pnl.setBackground(Color.WHITE);
        JPanel pnlKhachHang = createInfoBox("THÔNG TIN KHÁCH HÀNG");
        pnlKhachHang.add(new JLabel("<html><b>" + khachHang + "</b></html>"));
        pnlKhachHang.add(new JLabel("SĐT: " + (sdt == null || sdt.isEmpty() ? "Không cung cấp" : sdt)));
        if (sdt != null && !sdt.isEmpty()) {
            JLabel lblDiem = new JLabel("Điểm tích lũy: " + String.format("%,d", diemHienTai) + " điểm");
            lblDiem.setForeground(Color.decode("#E1304C")); lblDiem.setFont(new Font("Segoe UI", Font.BOLD, 12));
            pnlKhachHang.add(lblDiem);
        }
        JPanel pnlHoaDon = createInfoBox("THÔNG TIN HÓA ĐƠN");
        pnlHoaDon.add(new JLabel("<html>Nhân viên: <b>" + this.tenNhanVien + "</b></html>"));
        pnlHoaDon.add(new JLabel("<html>Phương thức: <font color='#009643'><b>" + phuongThuc + "</b></font></html>")); 
        pnl.add(pnlKhachHang); pnl.add(pnlHoaDon); 
        if (bacSi != null && !bacSi.isEmpty()) {
            JPanel pnlKeDon = createInfoBox("THÔNG TIN KÊ ĐƠN");
            pnlKeDon.add(new JLabel("<html>Bác sĩ: <b>" + bacSi + "</b></html>"));
            pnlKeDon.add(new JLabel("<html>Cơ sở: <b>" + coSo + "</b></html>"));
            if (chuanDoan != null && !chuanDoan.isEmpty()) pnlKeDon.add(new JLabel("<html>C.Đoán: <b>" + chuanDoan + "</b></html>"));
            pnl.add(pnlKeDon);
        }
        return pnl;
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
        table.setRowHeight(45); table.setShowGrid(false); table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JTableHeader header = table.getTableHeader(); header.setBackground(Color.decode("#E0F2FE")); header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        DefaultTableCellRenderer center = new DefaultTableCellRenderer(); center.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer(); right.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(0).setCellRenderer(center); table.getColumnModel().getColumn(2).setCellRenderer(center);
        table.getColumnModel().getColumn(3).setCellRenderer(center); table.getColumnModel().getColumn(4).setCellRenderer(right);
        table.getColumnModel().getColumn(5).setCellRenderer(center); table.getColumnModel().getColumn(6).setCellRenderer(right);
        JScrollPane sp = new JScrollPane(table); sp.getViewport().setBackground(Color.WHITE); sp.setBorder(null); sp.setPreferredSize(new Dimension(650, 150)); 
        pnl.add(sp, BorderLayout.CENTER); return pnl;
    }

    private JPanel createSummaryPanel(String phuongThuc) {
        JPanel pnl = new JPanel(new GridLayout(1, 2, 15, 0)); pnl.setBackground(Color.WHITE);
        int totalRows = 4; if (tienGiamTuDiemThucTe > 0) totalRows++; 
        JPanel pnlTotal = new JPanel(new GridLayout(totalRows, 2, 0, 8)); pnlTotal.setBackground(Color.WHITE);
        pnlTotal.setBorder(BorderFactory.createCompoundBorder(new LineBorder(borderGray, 1, true), new EmptyBorder(10, 10, 10, 10)));
        pnlTotal.add(new JLabel("Tạm tính:")); pnlTotal.add(createRightAlignLabel(String.format("%,d", tamTinhThucTe).replace(',', '.') + "đ"));
        pnlTotal.add(new JLabel("VAT:")); pnlTotal.add(createRightAlignLabel("+" + String.format("%,d", vatThucTe).replace(',', '.') + "đ"));
        JLabel lblGiamGiaText = new JLabel("Giảm khuyến mãi:"); lblGiamGiaText.setForeground(Color.decode("#EF4444")); 
        pnlTotal.add(lblGiamGiaText); pnlTotal.add(createRightAlignLabel("-" + String.format("%,d", tienGiamGiaThucTe).replace(',', '.') + "đ"));
        if (tienGiamTuDiemThucTe > 0) {
            pnlTotal.add(new JLabel("Dùng điểm:")); pnlTotal.add(createRightAlignLabel("-" + String.format("%,d", tienGiamTuDiemThucTe).replace(',', '.') + "đ"));
        }
        JLabel lblTotalAmount = createRightAlignLabel(String.format("%,d", tongThanhToanThucTe).replace(',', '.') + "đ");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblTotalAmount.setForeground(isDaHuy ? textRed : primaryGreen);
        pnlTotal.add(new JLabel("TỔNG THANH TOÁN:")); pnlTotal.add(lblTotalAmount);
        JPanel pnlPayment = new JPanel(new GridLayout(3, 2, 0, 8)); pnlPayment.setBackground(isDaHuy ? bgRed : bgYellow);
        pnlPayment.setBorder(new LineBorder(isDaHuy ? textRed : Color.decode("#FDE047"), 1, true));
        pnlPayment.add(new JLabel(isDaHuy ? "TRẠNG THÁI:" : "THANH TOÁN:")); pnlPayment.add(createRightAlignLabel(isDaHuy ? "ĐÃ HỦY" : phuongThuc));
        pnl.add(pnlTotal); pnl.add(pnlPayment); return pnl;
    }

    private JLabel createRightAlignLabel(String text) { JLabel label = new JLabel(text); label.setHorizontalAlignment(SwingConstants.RIGHT); return label; }
    private JPanel createFooterTextPanel() {
        JPanel pnl = new JPanel(new GridLayout(2, 1)); pnl.setBackground(Color.WHITE);
        pnl.add(new JLabel("Cảm ơn quý khách đã tin dùng MYCARE PHARMACY!", SwingConstants.CENTER));
        pnl.add(new JLabel("NV xác nhận: " + this.tenNhanVien, SwingConstants.CENTER));
        return pnl;
    }
}