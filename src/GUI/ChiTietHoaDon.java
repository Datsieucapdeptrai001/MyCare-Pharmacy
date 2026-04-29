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
    
    // --- THÊM 2 BIẾN MỚI ĐỂ XỬ LÝ ĐIỂM ---
    private long tienGiamTuDiemThucTe = 0; 
    private int diemHienTai = 0;
    private boolean isDaHuy = false; // Biến kiểm tra trạng thái hủy
    private Color bgRed = Color.decode("#FEF2F2"); // Màu nền đỏ nhạt
    private Color textRed = Color.decode("#DC2626"); // Màu chữ/viền đỏ đậm
    public ChiTietHoaDon(Frame parent, String maHD, String ngay, String khachHang, String sdt, String phuongThuc, String tongTienCu, String tenNhanVien, List<Object[]> dsSanPham) {
        super(parent, "Chi tiết hóa đơn", true);
        this.dsSanPham = dsSanPham;
        this.tenNhanVien = tenNhanVien;

        try { tongThanhToanThucTe = Long.parseLong(tongTienCu.replaceAll("[^0-9]", "")); } catch (Exception e) {}

        // Tính tiền ban đầu dựa trên danh sách sản phẩm
        for (Object[] sp : dsSanPham) {
            try {
                long donGia = Long.parseLong(sp[4].toString().replaceAll("[^0-9]", ""));
                int sl = Integer.parseInt(sp[3].toString());
                long thanhTienCuaSP = Long.parseLong(sp[6].toString().replaceAll("[^0-9]", ""));
                
                long tienChuaVat = donGia * sl;
                tamTinhThucTe += tienChuaVat;
                vatThucTe += (thanhTienCuaSP - tienChuaVat);
            } catch (Exception e) {}
        }
        
        // ========================================================
        // 1. LẤY ĐIỂM TÍCH LŨY (GỌI QUA TẦNG BUS_KhachHang)
        // ========================================================
        if (sdt != null && !sdt.isEmpty()) {
            try {
                BUS.BUS_KhachHang busKH = new BUS.BUS_KhachHang();
                Entity.KhachHang kh = busKH.getKhachHangTheoSDT(sdt);
                if (kh != null) diemHienTai = kh.getDiemTichLuy();
            } catch (Exception e) { System.out.println("Lỗi lấy KH: " + e.getMessage()); }
        }

        tienKhachDuaThucTe = tongThanhToanThucTe;
        boolean hasKhuyenMai = false; 
        
        // ========================================================
        // 2. BÓC TÁCH GHI CHÚ & LẤY TÊN NHÂN VIÊN (GỌI QUA BUS_HoaDon)
        // ========================================================
        try {
            BUS.BUS_HoaDon busHD = new BUS.BUS_HoaDon();
            Entity.HoaDon hd = busHD.layHoaDonTheoMa(maHD);
            
            if (hd != null) {
                // FIX: Lấy tên chuẩn xác từ Database chống lỗi đổi ca
            	if (hd.getNhanVienId() != null && hd.getNhanVienId().getHoVaTen() != null) {
                    this.tenNhanVien = hd.getNhanVienId().getHoVaTen();
                }

                String ghiChu = hd.getGhiChu() != null ? hd.getGhiChu() : "";
                
                if (ghiChu.startsWith("Đã hủy") || ghiChu.contains("Đã hủy")) {
                    this.isDaHuy = true;
                    this.primaryGreen = textRed; 
                    this.bgLight = bgRed;
                }
                
                if (!ghiChu.isEmpty()) {
                    if (ghiChu.contains("KM:")) {
                        hasKhuyenMai = true;
                    }
                    
                    String[] parts = ghiChu.split("\\|");
                    for (String p : parts) {
                        p = p.trim();
                        if (phuongThuc.equals("Tiền mặt") && p.startsWith("CASH:")) {
                            String moneyStr = p.substring(5).replaceAll("[^0-9]", "");
                            if (!moneyStr.isEmpty()) {
                                try { tienKhachDuaThucTe = Long.parseLong(moneyStr); } catch (Exception ex) {}
                            }
                        } else if (p.startsWith("Dùng điểm: -")) {
                            String diemStr = p.substring(12).replaceAll("[^0-9]", "");
                            if (!diemStr.isEmpty()) {
                                try { tienGiamTuDiemThucTe = Long.parseLong(diemStr); } catch (Exception ex) {}
                            }
                        } else if (p.startsWith("BS:")) bacSi = p.substring(3).trim();
                        else if (p.startsWith("CS:")) coSo = p.substring(3).trim();
                        else if (p.startsWith("CD:")) chuanDoan = p.substring(3).trim();
                    }
                }
            }
        } catch (Exception e) { System.out.println("Lỗi gọi HD: " + e.getMessage()); }

        long targetGross = tongThanhToanThucTe + tienGiamTuDiemThucTe; 
        long currentGross = tamTinhThucTe + vatThucTe; 
        
        if (!hasKhuyenMai && currentGross != targetGross && currentGross > 0) {
            tamTinhThucTe = 0;
            vatThucTe = 0;
            
            for (Object[] sp : dsSanPham) {
                long donGiaHienTai = Long.parseLong(sp[4].toString().replaceAll("[^0-9]", ""));
                int sl = Integer.parseInt(sp[3].toString());
                double vatPercent = 5.0; 
                try {
                    vatPercent = Double.parseDouble(sp[5].toString().replace("%", "").trim());
                } catch(Exception ex) {}

                long currentSPGross = Math.round(donGiaHienTai * sl * (1 + vatPercent / 100.0));
                double tiLeSP = (double) currentSPGross / currentGross;
                long targetSPGross = Math.round(targetGross * tiLeSP); 

                long donGiaCu = donGiaHienTai;
                double vatCuPercent = vatPercent;

                // THUẬT TOÁN TÌM GIÁ TRÒN ĐẸP: 
                // Thử các mốc VAT chuẩn để tìm lại giá gốc nguyên thủy (thường là số chẵn hàng trăm)
                double minError = Double.MAX_VALUE;
                double[] standardVats = {5.0, 8.0, 10.0, 0.0};
                
                for (double v : standardVats) {
                    double theoreticalPrice = targetSPGross / (sl * (1.0 + v / 100.0));
                    // Tìm số tiền làm tròn đến hàng trăm gần nhất (vd: 3402.77 -> 3400.0)
                    double roundedPrice100 = Math.round(theoreticalPrice / 100.0) * 100.0;
                    
                    // Đo độ sai lệch. Sai lệch càng thấp (gần 0) tức là giá đó càng chuẩn xác
                    double error = Math.abs(theoreticalPrice - roundedPrice100);
                    
                    if (error < minError) {
                        minError = error;
                        donGiaCu = (long) roundedPrice100;
                        vatCuPercent = v;
                    }
                }
                
                // Nếu giá gốc thực sự lẻ (minError > 10đ), fallback tính giá chính xác theo VAT hiện tại
                if (minError > 10.0) { 
                    donGiaCu = Math.round(targetSPGross / (sl * (1.0 + vatPercent / 100.0)));
                    vatCuPercent = vatPercent;
                }

                // Cập nhật lại phép toán dựa trên Giá Cũ và VAT Cũ đã tìm được
                long tienChuaVatCu = donGiaCu * sl;
                long vatCu = Math.round(tienChuaVatCu * (vatCuPercent / 100.0));
                long thanhTienCu = tienChuaVatCu + vatCu;
                
                sp[4] = String.format("%,d", donGiaCu).replace(',', '.') + "đ";
                sp[5] = (int) vatCuPercent + "%"; 
                sp[6] = String.format("%,d", thanhTienCu).replace(',', '.') + "đ";
                
                tamTinhThucTe += tienChuaVatCu;
                vatThucTe += vatCu;
            }
            
            // Bù trừ vài đồng lẻ do phép chia lấy tròn
            long lech = targetGross - (tamTinhThucTe + vatThucTe);
            vatThucTe += lech; 
            
            if (dsSanPham.size() > 0) {
                Object[] lastSp = dsSanPham.get(dsSanPham.size() - 1);
                long oldThanhTien = Long.parseLong(lastSp[6].toString().replaceAll("[^0-9]", ""));
                lastSp[6] = String.format("%,d", oldThanhTien + lech).replace(',', '.') + "đ";
            }
        }
        
        // =========================================================================
        // 4. CHỐT SỔ TIỀN THANH TOÁN
        // =========================================================================
        if (hasKhuyenMai) {
            tienGiamGiaThucTe = (tamTinhThucTe + vatThucTe) - tongThanhToanThucTe - tienGiamTuDiemThucTe;
            if (tienGiamGiaThucTe < 0) tienGiamGiaThucTe = 0;
        } else {
            tienGiamGiaThucTe = 0; 
            tongThanhToanThucTe = tamTinhThucTe + vatThucTe - tienGiamTuDiemThucTe; 
        }
        
        if (tienKhachDuaThucTe < tongThanhToanThucTe) {
             tienKhachDuaThucTe = tongThanhToanThucTe;
        }
        
        tienThoiThucTe = tienKhachDuaThucTe - tongThanhToanThucTe;
        if (tienThoiThucTe < 0) tienThoiThucTe = 0;
        
        String tongTienDungStr = String.format("%,d", tongThanhToanThucTe).replace(',', '.') + "đ";

        // --- KHỞI TẠO UI ---
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
        
        // 1. ĐỔI MÀU NỀN THEO TRẠNG THÁI HỦY
        pnlHeader.setBackground(isDaHuy ? textRed : primaryGreen);
        pnlHeader.setBorder(new EmptyBorder(8, 15, 8, 15)); 

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlTitle.setOpaque(false);
        
        // 2. ĐỔI TIÊU ĐỀ THEO TRẠNG THÁI
        String title = isDaHuy ? "Hóa đơn đã hủy — " : "Hóa đơn bán hàng — ";
        JLabel lblTitle = new JLabel(title + maHD);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        
        // 3. ĐỔI NHÃN TRẠNG THÁI
        JLabel lblStatus = new JLabel(isDaHuy ? "Đã hủy" : "Hoàn thành");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblStatus.setForeground(isDaHuy ? textRed : primaryGreen); 
        lblStatus.setBackground(Color.WHITE);
        lblStatus.setOpaque(true); // Nền trắng chữ màu cho nổi bật
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
                    
                    if (isDaHuy) {
                        writer.println("            *** HÓA ĐƠN ĐÃ HỦY ***");
                        writer.println("================================================");
                    }
                    
                    writer.println("Số: " + maHD + " | Ngày: " + ngay);
                    writer.println("Nhân viên: " + tenNhanVien); 
                    writer.println("------------------------------------------------");
                    
                    // Xóa chữ "Trống" - Nếu không có SĐT thì ẩn luôn đi
                    String sdtStr = (sdt == null || sdt.isEmpty()) ? "" : " | SĐT: " + sdt;
                    writer.println("KHÁCH HÀNG: " + khachHang + sdtStr);
                    
                    if (sdt != null && !sdt.isEmpty()) writer.println("Điểm tích lũy hiện tại: " + String.format("%,d", diemHienTai) + " điểm");
                    writer.println("THANH TOÁN: " + phuongThuc);
                    
                    // --- BỔ SUNG IN ĐƠN THUỐC BỊ THIẾU ---
                    if (bacSi != null && !bacSi.isEmpty()) {
                        writer.println("------------------------------------------------");
                        writer.println("THÔNG TIN KÊ ĐƠN:");
                        writer.println("Bác sĩ: " + bacSi);
                        writer.println("Cơ sở: " + coSo);
                        if (chuanDoan != null && !chuanDoan.isEmpty()) writer.println("Chẩn đoán: " + chuanDoan);
                    }
                    
                    writer.println("------------------------------------------------");
                    writer.printf("%-4s | %-21s | %-4s | %-10s\n", "SL", "TÊN SẢN PHẨM", "ĐVT", "THÀNH TIỀN");
                    writer.println("------------------------------------------------");
                    
                    for (Object[] sp : dsSanPham) {
                        String tenSP = sp[1].toString().replaceAll("<[^>]*>", "").trim();
                        if (tenSP.length() > 21) {
                            tenSP = tenSP.substring(0, 18) + "..."; 
                        }
                        writer.printf("%-4s | %-21s | %-4s | %-10s\n", sp[3].toString(), tenSP, sp[2].toString(), sp[6].toString());
                    }
                    
                    writer.println("------------------------------------------------");
                    
                    // FIX LỖI KHOẢNG TRỐNG: Chỉnh lại format cho gọn gàng, sát số tiền hơn
                    writer.printf("%-25s %20s\n", "Tạm tính:", String.format("%,d", tamTinhThucTe).replace(',', '.') + "đ");
                    writer.printf("%-25s %20s\n", "VAT (5%):", "+" + String.format("%,d", vatThucTe).replace(',', '.') + "đ");
                    if(tienGiamGiaThucTe > 0) {
                        writer.printf("%-25s %20s\n", "Giảm khuyến mãi:", "-" + String.format("%,d", tienGiamGiaThucTe).replace(',', '.') + "đ");
                    }
                    if(tienGiamTuDiemThucTe > 0) {
                        writer.printf("%-25s %20s\n", String.format("Dùng %d điểm:", tienGiamTuDiemThucTe/100), "-" + String.format("%,d", tienGiamTuDiemThucTe).replace(',', '.') + "đ");
                    }
                    writer.println("------------------------------------------------");
                    writer.printf("%-25s %20s\n", "TỔNG THANH TOÁN:", tongTien);
                    
                    // KHÔNG IN TIỀN KHÁCH ĐƯA NẾU LÀ HÓA ĐƠN HỦY
                    if (!isDaHuy && phuongThuc.equals("Tiền mặt")) {
                        writer.printf("%-25s %20s\n", "TIỀN KHÁCH ĐƯA:", String.format("%,d", tienKhachDuaThucTe).replace(',', '.') + "đ");
                        writer.printf("%-25s %20s\n", "TIỀN THỐI LẠI:", String.format("%,d", tienThoiThucTe).replace(',', '.') + "đ");
                    }
                    writer.println("================================================");
                    JOptionPane.showMessageDialog(this, "Đã xuất file hóa đơn thành công!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi lưu file!");
                }
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
        
        // --- FIX LỖI TÊN HÓA ĐƠN Ở ĐÂY ---
        String titleText = isDaHuy ? "HÓA ĐƠN ĐÃ HỦY" : "HÓA ĐƠN BÁN HÀNG";
        JLabel lblTitleHD = new JLabel(titleText, SwingConstants.CENTER);
        lblTitleHD.setFont(new Font("Segoe UI", Font.BOLD, 16)); 
        lblTitleHD.setForeground(isDaHuy ? textRed : Color.decode("#152A4B")); // Đổi màu đỏ nếu hủy
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
        // Đếm số lượng ô cần hiển thị: Nếu có Tên bác sĩ thì chia làm 3 cột, nếu không thì 2 cột như cũ
        int cols = (bacSi != null && !bacSi.isEmpty()) ? 3 : 2;
        JPanel pnl = new JPanel(new GridLayout(1, cols, 15, 0)); 
        pnl.setBackground(Color.WHITE);
        
        // --- CỘT 1: THÔNG TIN KHÁCH HÀNG ---
        JPanel pnlKhachHang = createInfoBox("THÔNG TIN KHÁCH HÀNG");
        pnlKhachHang.add(new JLabel("<html><b>" + khachHang + "</b></html>"));
        pnlKhachHang.add(new JLabel("SĐT: " + (sdt == null || sdt.isEmpty() ? "Không cung cấp" : sdt)));
        
        if (sdt != null && !sdt.isEmpty()) {
            JLabel lblDiem = new JLabel("Điểm tích lũy: " + String.format("%,d", diemHienTai) + " điểm");
            lblDiem.setForeground(Color.decode("#E1304C"));
            lblDiem.setFont(new Font("Segoe UI", Font.BOLD, 12));
            pnlKhachHang.add(lblDiem);
        }

        // --- CỘT 2: THÔNG TIN HÓA ĐƠN ---
        JPanel pnlHoaDon = createInfoBox("THÔNG TIN HÓA ĐƠN");
        pnlHoaDon.add(new JLabel("<html>Nhân viên: <b>" + this.tenNhanVien + "</b></html>"));
        pnlHoaDon.add(new JLabel("<html>Phương thức: <font color='#009643'><b>" + phuongThuc + "</b></font></html>")); 
        
        pnl.add(pnlKhachHang); 
        pnl.add(pnlHoaDon); 
        
        // --- CỘT 3: THÔNG TIN KÊ ĐƠN (CHỈ XUẤT HIỆN KHI CÓ KÊ ĐƠN) ---
        if (bacSi != null && !bacSi.isEmpty()) {
            JPanel pnlKeDon = createInfoBox("THÔNG TIN KÊ ĐƠN");
            pnlKeDon.add(new JLabel("<html>Bác sĩ: <b>" + bacSi + "</b></html>"));
            pnlKeDon.add(new JLabel("<html>Cơ sở: <b>" + coSo + "</b></html>"));
            if (chuanDoan != null && !chuanDoan.isEmpty()) {
                pnlKeDon.add(new JLabel("<html>C.Đoán: <b>" + chuanDoan + "</b></html>"));
            }
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

        int totalRows = 4;
        if (tienGiamTuDiemThucTe > 0) totalRows++; 

        JPanel pnlTotal = new JPanel(new GridLayout(totalRows, 2, 0, 8)); pnlTotal.setBackground(Color.WHITE);
        pnlTotal.setBorder(BorderFactory.createCompoundBorder(new LineBorder(borderGray, 1, true), new EmptyBorder(10, 10, 10, 10)));

        pnlTotal.add(new JLabel("Tạm tính:")); pnlTotal.add(createRightAlignLabel(String.format("%,d", tamTinhThucTe).replace(',', '.') + "đ"));
        pnlTotal.add(new JLabel("VAT:")); pnlTotal.add(createRightAlignLabel("+" + String.format("%,d", vatThucTe).replace(',', '.') + "đ"));
        
        JLabel lblGiamGiaText = new JLabel("Giảm khuyến mãi:");
        lblGiamGiaText.setForeground(Color.decode("#EF4444")); 
        JLabel lblGiamGiaValue = createRightAlignLabel("-" + String.format("%,d", tienGiamGiaThucTe).replace(',', '.') + "đ");
        lblGiamGiaValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGiamGiaValue.setForeground(Color.decode("#EF4444")); 
        pnlTotal.add(lblGiamGiaText); pnlTotal.add(lblGiamGiaValue);

        if (tienGiamTuDiemThucTe > 0) {
            long diemDaDung = tienGiamTuDiemThucTe / 100;
            JLabel lblDiemText = new JLabel(String.format("Dùng %d điểm:", diemDaDung));
            lblDiemText.setForeground(Color.decode("#F59E0B")); 
            JLabel lblDiemValue = createRightAlignLabel("-" + String.format("%,d", tienGiamTuDiemThucTe).replace(',', '.') + "đ");
            lblDiemValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblDiemValue.setForeground(Color.decode("#F59E0B")); 
            pnlTotal.add(lblDiemText); pnlTotal.add(lblDiemValue);
        }

        JLabel lblTotalText = new JLabel("TỔNG THANH TOÁN:"); lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel lblTotalAmount = createRightAlignLabel(String.format("%,d", tongThanhToanThucTe).replace(',', '.') + "đ"); 
        
        // NẾU HỦY ĐỔI MÀU TỔNG TIỀN THÀNH ĐỎ
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 16)); 
        lblTotalAmount.setForeground(isDaHuy ? textRed : primaryGreen);
        pnlTotal.add(lblTotalText); pnlTotal.add(lblTotalAmount);

        // --- FIX LỖI ẨN TIỀN KHÁCH ĐƯA Ở ĐÂY ---
        JPanel pnlPayment = new JPanel(new GridLayout(3, 2, 0, 8)); 
        pnlPayment.setBackground(isDaHuy ? bgRed : bgYellow);
        pnlPayment.setBorder(BorderFactory.createCompoundBorder(new LineBorder(isDaHuy ? textRed : Color.decode("#FDE047"), 1, true), new EmptyBorder(10, 10, 10, 10)));

        JLabel lblPayTitle = new JLabel(isDaHuy ? "TRẠNG THÁI HÓA ĐƠN" : "THANH TOÁN " + phuongThuc.toUpperCase());
        lblPayTitle.setFont(new Font("Segoe UI", Font.BOLD, 13)); 
        lblPayTitle.setForeground(isDaHuy ? textRed : Color.decode("#CA8A04"));
        pnlPayment.add(lblPayTitle); pnlPayment.add(new JLabel(""));
        
        if (isDaHuy) {
            // NẾU LÀ HÓA ĐƠN HỦY -> HIỂN THỊ CHỮ ĐÃ BỊ HỦY (KHÔNG HIỆN TIỀN)
            JLabel lblHuy = new JLabel("ĐÃ BỊ HỦY");
            lblHuy.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblHuy.setForeground(textRed);
            pnlPayment.add(lblHuy); pnlPayment.add(new JLabel(""));
            pnlPayment.add(new JLabel("")); pnlPayment.add(new JLabel(""));
        } else if(phuongThuc.equalsIgnoreCase("Chuyển khoản")) {
            JLabel lblStatusText = new JLabel("Trạng thái:");
            JLabel lblStatusValue = createRightAlignLabel("Đã nhận khoản");
            lblStatusValue.setForeground(primaryGreen); lblStatusValue.setFont(new Font("Segoe UI", Font.BOLD, 12));
            pnlPayment.add(lblStatusText); pnlPayment.add(lblStatusValue);
            pnlPayment.add(new JLabel("")); pnlPayment.add(new JLabel("")); 
        } else {
            // HÓA ĐƠN BÌNH THƯỜNG -> IN TIỀN MẶT
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