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
    private long tienGiamTuDiemThucTe = 0; 
    private int diemHienTai = 0;
    private boolean isDaHuy = false; 
    private Color bgRed = Color.decode("#FEF2F2"); 
    private Color textRed = Color.decode("#DC2626"); 

    public ChiTietHoaDon(Frame parent, String maHD, String ngay, String khachHang, String sdt, String phuongThuc, String tongTienCu, String tenNhanVien, List<Object[]> dsSanPhamGoc) {
        super(parent, "Chi tiết hóa đơn", true);
        this.dsSanPham = new java.util.ArrayList<>(dsSanPhamGoc);
        this.tenNhanVien = tenNhanVien;
        String maKMs = "";
        this.tienKhachDuaThucTe = 0;
        this.tienGiamTuDiemThucTe = 0;
        this.tienGiamGiaThucTe = 0;

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
                String ghiChu = hd.getGhiChu() != null ? hd.getGhiChu() : "";
                if (ghiChu.contains("Đã hủy")) {
                    this.isDaHuy = true;
                    this.primaryGreen = Color.decode("#EF4444");
                    this.bgLight = Color.decode("#FEE2E2");
                }

                String[] parts = ghiChu.split("\\|");
                for (String p : parts) {
                    p = p.trim();
                    if (p.startsWith("CASH:")) this.tienKhachDuaThucTe = Long.parseLong(p.substring(5).replaceAll("[^0-9]", ""));
                    else if (p.startsWith("Dùng điểm: -")) this.tienGiamTuDiemThucTe = Long.parseLong(p.substring(12).replaceAll("[^0-9]", ""));
                    else if (p.startsWith("BS:")) bacSi = p.substring(3).trim();
                    else if (p.startsWith("CS:")) coSo = p.substring(3).trim();
                    else if (p.startsWith("CD:")) chuanDoan = p.substring(3).trim();
                    else if (p.startsWith("KM:")) maKMs = p.substring(3).trim(); 
                    else if (p.startsWith("TANG:")) {
                        try {
                            String[] giftData = p.substring(5).split(";");
                            if (giftData.length >= 3) {
                                Object[] giftRow = new Object[7];
                                giftRow[0] = "GIFT"; 
                                giftRow[1] = "[QUÀ TẶNG] " + giftData[0]; 
                                giftRow[2] = giftData[2];                 
                                giftRow[3] = giftData[1];                 
                                giftRow[4] = "0đ";                        
                                giftRow[5] = "0%";                        
                                giftRow[6] = "0đ";                        
                                this.dsSanPham.add(giftRow);
                            }
                        } catch (Exception ex) { }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        // Tính toán tiền hàng
        this.tamTinhThucTe = 0;
        this.vatThucTe = 0;
        
        for (Object[] sp : dsSanPham) {
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

        long tongTienTruocGiam = this.tamTinhThucTe + this.vatThucTe;
        this.tienGiamGiaThucTe = 0;

        if (!maKMs.isEmpty()) {
            String[] codes = maKMs.split(",");
            try (java.sql.Connection con = ConnectDB.ConnectDB.getInstance().getConnection()) {
                for (String code : codes) {
                    code = code.trim();
                    String sqlCheck = "SELECT h.loaiHinhThuc, h.giaTri AS mucGiam FROM KhuyenMai k JOIN HinhThucKhuyenMai h ON k.id = h.khuyenMaiId WHERE k.id = ?";
                    try (java.sql.PreparedStatement pst = con.prepareStatement(sqlCheck)) {
                        pst.setString(1, code);
                        try (java.sql.ResultSet rsCheck = pst.executeQuery()) {
                            if (rsCheck.next()) {
                                String loaiKM = rsCheck.getString("loaiHinhThuc");
                                double giaTri = rsCheck.getDouble("mucGiam");
                                if (loaiKM != null && !loaiKM.contains("TANG") && !loaiKM.contains("SAN_PHAM_KEM_THEO")) {
                                    if (loaiKM.contains("PHAN_TRAM") || loaiKM.contains("%")) {
                                        this.tienGiamGiaThucTe += (long) (tongTienTruocGiam * (giaTri / 100.0));
                                    } else {
                                        this.tienGiamGiaThucTe += (long) giaTri;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {}
        }

        this.tongThanhToanThucTe = tongTienTruocGiam - this.tienGiamGiaThucTe - this.tienGiamTuDiemThucTe;
        if (this.tongThanhToanThucTe < 0) this.tongThanhToanThucTe = 0;

        if (this.tienKhachDuaThucTe == 0) this.tienKhachDuaThucTe = this.tongThanhToanThucTe;
        this.tienThoiThucTe = this.tienKhachDuaThucTe - this.tongThanhToanThucTe;
        if (this.tienThoiThucTe < 0) this.tienThoiThucTe = 0;

        String tongTienDungStr = String.format("%,d", this.tongThanhToanThucTe).replace(',', '.') + "đ";

        // ==========================================
        // UI SETUP - ĐÃ ÉP NHỎ XUỐNG 580px x 650px
        // ==========================================
        setSize(800, 750);
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
        pnlBody.add(Box.createRigidArea(new Dimension(0, 10))); 
        pnlBody.add(createCustomerAndInvoicePanel(khachHang, sdt, phuongThuc)); 
        pnlBody.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlBody.add(createProductTablePanel());
        pnlBody.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlBody.add(createSummaryPanel(phuongThuc));
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));
        pnlBody.add(createFooterTextPanel()); 

        JScrollPane scrollPane = new JScrollPane(pnlBody);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Tắt cuộn ngang
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // BẬT LẠI THANH CUỘN & Áp dụng giao diện ModernScrollBarUI tinh tế
        scrollPane.getVerticalScrollBar().setUI(new Utils.ModernScrollBarUI()); 
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0)); 
        // Lăn chuột mượt hơn
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); 

        add(scrollPane, BorderLayout.CENTER);
    }

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
        btnPrint.setIcon(new MenuIcon("PRINT", 18)); 
        btnPrint.setIconTextGap(5); btnPrint.setForeground(Color.WHITE);
        btnPrint.setContentAreaFilled(false); btnPrint.setFocusPainted(false);
        btnPrint.setBorder(new EmptyBorder(4, 8, 4, 8));
        
        btnPrint.addActionListener(e -> JOptionPane.showMessageDialog(this, "Chức năng xuất hóa đơn TXT đang bảo trì."));
        
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
        lblAddress.setFont(new Font("Segoe UI", Font.PLAIN, 11)); lblAddress.setForeground(textGray);
        JLabel lblContact = new JLabel("Hotline: 1800 6868 · Email: support@mycare.vn", SwingConstants.CENTER);
        lblContact.setFont(new Font("Segoe UI", Font.PLAIN, 11)); lblContact.setForeground(textGray);
        
        String titleText = isDaHuy ? "HÓA ĐƠN ĐÃ HỦY" : "HÓA ĐƠN BÁN HÀNG";
        JLabel lblTitleHD = new JLabel(titleText, SwingConstants.CENTER);
        lblTitleHD.setFont(new Font("Segoe UI", Font.BOLD, 16)); 
        lblTitleHD.setForeground(isDaHuy ? textRed : Color.decode("#152A4B")); 
        lblTitleHD.setBorder(new EmptyBorder(10, 0, 5, 0));
        
        JLabel lblDateInfo = new JLabel("Số: " + maHD + " · Ngày: " + ngay, SwingConstants.CENTER);
        lblDateInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lblDateInfo.setForeground(textGray);

        pnl.add(lblName); pnl.add(lblAddress); pnl.add(lblContact);
        JPanel wrapper = new JPanel(new BorderLayout()); wrapper.setBackground(Color.WHITE);
        wrapper.add(pnl, BorderLayout.NORTH);
        
        JPanel pnlTitle = new JPanel(new GridLayout(2, 1)); pnlTitle.setBackground(Color.WHITE);
        pnlTitle.add(lblTitleHD); pnlTitle.add(lblDateInfo);
        wrapper.add(pnlTitle, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createCustomerAndInvoicePanel(String khachHang, String sdt, String phuongThuc) { 
        // 1. Sử dụng BorderLayout tổng để kiểm soát chặt chẽ vị trí
        JPanel pnl = new JPanel(new BorderLayout(0, 10)); 
        pnl.setBackground(Color.WHITE);
        
        // 2. Panel Top chứa Khách hàng & Hóa đơn
        JPanel pnlTop = new JPanel(new GridLayout(1, 2, 10, 0)); 
        pnlTop.setBackground(Color.WHITE);

        // KHÁCH HÀNG
        JPanel pnlKhachHang = createInfoBox("THÔNG TIN KHÁCH HÀNG");
        JLabel lblKH = new JLabel("<html><div style='width:250px;'><b>" + khachHang + "</b></div></html>");
        lblKH.setAlignmentX(Component.LEFT_ALIGNMENT); // Căn trái
        pnlKhachHang.add(lblKH);
        
        JLabel lblSDT = new JLabel("SĐT: " + (sdt == null || sdt.isEmpty() ? "Không cung cấp" : sdt));
        lblSDT.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlKhachHang.add(lblSDT);
        
        if (sdt != null && !sdt.isEmpty()) {
            JLabel lblDiem = new JLabel("Điểm tích lũy: " + String.format("%,d", diemHienTai) + " điểm");
            lblDiem.setForeground(Color.decode("#E1304C"));
            lblDiem.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblDiem.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlKhachHang.add(lblDiem);
        }

        // HÓA ĐƠN
        JPanel pnlHoaDon = createInfoBox("THÔNG TIN HÓA ĐƠN");
        JLabel lblNV = new JLabel("<html><div style='width:250px;'>Nhân viên: <b>" + this.tenNhanVien + "</b></div></html>");
        lblNV.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlHoaDon.add(lblNV);
        
        JLabel lblPT = new JLabel("<html><div style='width:250px;'>Phương thức: <font color='#009643'><b>" + phuongThuc + "</b></font></div></html>"); 
        lblPT.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlHoaDon.add(lblPT); 
        
        pnlTop.add(pnlKhachHang); 
        pnlTop.add(pnlHoaDon); 
        
        // Chốt cụm Top lên hướng Bắc
        pnl.add(pnlTop, BorderLayout.NORTH);
        
        // 3. Panel Kê đơn (Nếu có)
        if (bacSi != null && !bacSi.isEmpty()) {
            JPanel pnlKeDon = createInfoBox("THÔNG TIN KÊ ĐƠN");
            
            JLabel lblBS = new JLabel("<html><div style='width:500px;'>Bác sĩ: <b>" + bacSi + "</b></div></html>");
            lblBS.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlKeDon.add(lblBS);
            
            JLabel lblCS = new JLabel("<html><div style='width:500px;'>Cơ sở: <b>" + coSo + "</b></div></html>");
            lblCS.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlKeDon.add(lblCS);
            
            if (chuanDoan != null && !chuanDoan.isEmpty()) {
                JLabel lblCD = new JLabel("<html><div style='width:500px;'>C.Đoán: <b>" + chuanDoan + "</b></div></html>");
                lblCD.setAlignmentX(Component.LEFT_ALIGNMENT);
                pnlKeDon.add(lblCD);
            }
            
            // Đẩy bảng Kê Đơn xuống Center
            pnl.add(pnlKeDon, BorderLayout.CENTER);
        }

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
        
        // Quan trọng: Ép box mở rộng tối đa theo chiều ngang
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        
        JLabel lblTitle = new JLabel(title); 
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11)); 
        lblTitle.setForeground(Color.decode("#4F46E5")); 
        lblTitle.setBorder(new EmptyBorder(0, 0, 8, 0)); 
        
        // Khóa lề trái cho tiêu đề
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
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

        // BỔ SUNG: Tách riêng cột ĐVT và SL thành 5 cột
        String[] cols = {"Sản phẩm", "ĐVT", "SL", "Đơn giá", "Thành tiền"};
        Object[][] data = new Object[dsSanPham.size()][5];
        
        for (int i = 0; i < dsSanPham.size(); i++) { 
            Object[] sp = dsSanPham.get(i);
            data[i][0] = sp[1]; // Tên sản phẩm                          
            data[i][1] = sp[2]; // Đơn vị tính (Hộp, Viên...)
            data[i][2] = sp[3]; // Số lượng
            data[i][3] = sp[4]; // Đơn giá                          
            data[i][4] = sp[6]; // Thành tiền                          
        }

        DefaultTableModel model = new DefaultTableModel(data, cols) { 
            @Override public boolean isCellEditable(int r, int c) { return false; } 
        };
        
        JTable table = new JTable(model);
        table.setRowHeight(35); 
        table.setShowGrid(false); 
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        // Bật tự động giãn cột
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JTableHeader header = table.getTableHeader(); 
        header.setBackground(Color.decode("#F1F5F9")); 
        header.setForeground(textDark);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12)); 
        header.setPreferredSize(new Dimension(0, 35));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderGray));

        // Render Icon Quà Tặng
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isS, boolean hasF, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, value, isS, hasF, r, c);
                String text = (value != null) ? value.toString() : "";
                
                if (text.contains("[QUÀ TẶNG]") || text.contains("QUÀ TẶNG")) {
                    text = text.replace("[QUÀ TẶNG]", "").trim();
                    lbl.setIcon(new MenuIcon("QUA_TANG", 14)); 
                    lbl.setForeground(Color.decode("#DC2626")); 
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                } else {
                    lbl.setIcon(null);
                    lbl.setForeground(textDark);
                    lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                }
                
                lbl.setText(text);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                return lbl;
            }
        });

        DefaultTableCellRenderer center = new DefaultTableCellRenderer(); center.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer(); right.setHorizontalAlignment(JLabel.RIGHT);
        
        // Căn giữa ĐVT và SL
        table.getColumnModel().getColumn(1).setCellRenderer(center); 
        table.getColumnModel().getColumn(2).setCellRenderer(center); 
        table.getColumnModel().getColumn(3).setCellRenderer(right);  
        table.getColumnModel().getColumn(4).setCellRenderer(right);  

        // KHÓA CỨNG ĐỘ RỘNG 4 CỘT CUỐI ĐỂ BẢNG ĐẸP HƠN
        table.getColumnModel().getColumn(1).setMinWidth(55); // Cột ĐVT
        table.getColumnModel().getColumn(1).setMaxWidth(65);
        
        table.getColumnModel().getColumn(2).setMinWidth(40); // Cột SL
        table.getColumnModel().getColumn(2).setMaxWidth(50);
        
        table.getColumnModel().getColumn(3).setMinWidth(85); // Cột Đơn giá
        table.getColumnModel().getColumn(3).setMaxWidth(95);
        
        table.getColumnModel().getColumn(4).setMinWidth(95); // Cột Thành tiền
        table.getColumnModel().getColumn(4).setMaxWidth(105);

        // Cột 0 (Sản phẩm) tự do giãn để lấp đầy phần diện tích còn trống
        table.getColumnModel().getColumn(0).setMinWidth(150);

        JScrollPane sp = new JScrollPane(table); 
        sp.getViewport().setBackground(Color.WHITE); 
        sp.setBorder(BorderFactory.createLineBorder(borderGray));
        
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        
        int actualTableHeight = (table.getRowCount() * 35) + 35;
        
        sp.setPreferredSize(new Dimension(0, actualTableHeight));
        sp.setMinimumSize(new Dimension(0, actualTableHeight));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, actualTableHeight));
        
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, actualTableHeight + 35));

        pnl.add(sp, BorderLayout.CENTER); 
        return pnl;
    }
    private JPanel createSummaryPanel(String phuongThuc) {
        JPanel pnl = new JPanel(new GridLayout(1, 2, 10, 0)); pnl.setBackground(Color.WHITE);

        int totalRows = 4;
        if (tienGiamTuDiemThucTe > 0) totalRows++; 

        JPanel pnlTotal = new JPanel(new GridLayout(totalRows, 2, 0, 8)); pnlTotal.setBackground(Color.WHITE);
        pnlTotal.setBorder(BorderFactory.createCompoundBorder(new LineBorder(borderGray, 1, true), new EmptyBorder(10, 10, 10, 10)));

        pnlTotal.add(new JLabel("Tạm tính:")); pnlTotal.add(createRightAlignLabel(String.format("%,d", tamTinhThucTe).replace(',', '.') + "đ"));
        pnlTotal.add(new JLabel("VAT:")); pnlTotal.add(createRightAlignLabel("+" + String.format("%,d", vatThucTe).replace(',', '.') + "đ"));
        
        JLabel lblGiamGiaText = new JLabel("Khuyến mãi:"); lblGiamGiaText.setForeground(Color.decode("#EF4444")); 
        JLabel lblGiamGiaValue = createRightAlignLabel("-" + String.format("%,d", tienGiamGiaThucTe).replace(',', '.') + "đ");
        lblGiamGiaValue.setFont(new Font("Segoe UI", Font.BOLD, 13)); lblGiamGiaValue.setForeground(Color.decode("#EF4444")); 
        pnlTotal.add(lblGiamGiaText); pnlTotal.add(lblGiamGiaValue);

        if (tienGiamTuDiemThucTe > 0) {
            long diemDaDung = tienGiamTuDiemThucTe / 100;
            JLabel lblDiemText = new JLabel(String.format("Dùng %d điểm:", diemDaDung)); lblDiemText.setForeground(Color.decode("#F59E0B")); 
            JLabel lblDiemValue = createRightAlignLabel("-" + String.format("%,d", tienGiamTuDiemThucTe).replace(',', '.') + "đ");
            lblDiemValue.setFont(new Font("Segoe UI", Font.BOLD, 13)); lblDiemValue.setForeground(Color.decode("#F59E0B")); 
            pnlTotal.add(lblDiemText); pnlTotal.add(lblDiemValue);
        }

        JLabel lblTotalText = new JLabel("TỔNG TIỀN:"); lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel lblTotalAmount = createRightAlignLabel(String.format("%,d", tongThanhToanThucTe).replace(',', '.') + "đ"); 
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 15)); lblTotalAmount.setForeground(isDaHuy ? textRed : primaryGreen);
        pnlTotal.add(lblTotalText); pnlTotal.add(lblTotalAmount);

        JPanel pnlPayment = new JPanel(new GridLayout(3, 2, 0, 8)); 
        pnlPayment.setBackground(isDaHuy ? bgRed : bgYellow);
        pnlPayment.setBorder(BorderFactory.createCompoundBorder(new LineBorder(isDaHuy ? textRed : Color.decode("#FDE047"), 1, true), new EmptyBorder(10, 10, 10, 10)));

        JLabel lblPayTitle = new JLabel(isDaHuy ? "TRẠNG THÁI" : "THANH TOÁN");
        lblPayTitle.setFont(new Font("Segoe UI", Font.BOLD, 12)); lblPayTitle.setForeground(isDaHuy ? textRed : Color.decode("#CA8A04"));
        pnlPayment.add(lblPayTitle); pnlPayment.add(new JLabel(""));
        
        if (isDaHuy) {
            JLabel lblHuy = new JLabel("ĐÃ BỊ HỦY"); lblHuy.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblHuy.setForeground(textRed);
            pnlPayment.add(lblHuy); pnlPayment.add(new JLabel("")); pnlPayment.add(new JLabel("")); pnlPayment.add(new JLabel(""));
        } else if(phuongThuc.equalsIgnoreCase("Chuyển khoản")) {
            pnlPayment.add(new JLabel("Trạng thái:")); 
            JLabel lblStatusValue = createRightAlignLabel("Đã nhận"); lblStatusValue.setForeground(primaryGreen); lblStatusValue.setFont(new Font("Segoe UI", Font.BOLD, 12));
            pnlPayment.add(lblStatusValue); pnlPayment.add(new JLabel("")); pnlPayment.add(new JLabel("")); 
        } else {
            pnlPayment.add(new JLabel("Khách đưa:"));
            JLabel lblKhachDuaValue = createRightAlignLabel(String.format("%,d", tienKhachDuaThucTe).replace(',', '.') + "đ");
            lblKhachDuaValue.setFont(new Font("Segoe UI", Font.BOLD, 12)); lblKhachDuaValue.setForeground(primaryGreen);
            pnlPayment.add(lblKhachDuaValue);

            JLabel lblThoiLaiText = new JLabel("Thối lại:"); lblThoiLaiText.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lblThoiLaiText.setForeground(textGray);
            JLabel lblThoiLaiValue = createRightAlignLabel(String.format("%,d", tienThoiThucTe).replace(',', '.') + "đ");
            lblThoiLaiValue.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lblThoiLaiValue.setForeground(textGray);
            pnlPayment.add(lblThoiLaiText); pnlPayment.add(lblThoiLaiValue);
        }

        pnl.add(pnlTotal); pnl.add(pnlPayment); return pnl;
    }

    private JLabel createRightAlignLabel(String text) { JLabel label = new JLabel(text); label.setHorizontalAlignment(SwingConstants.RIGHT); return label; }

    private JPanel createFooterTextPanel() {
        // Tách ra 2 cột: Cột trái (Cảm ơn), Cột phải (Chữ ký nhân viên) để tránh đẩy khung
        JPanel pnl = new JPanel(new GridLayout(1, 2, 10, 0)); 
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Khối Trái (Cảm ơn)
        JPanel pnlLeft = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlLeft.setBackground(Color.WHITE);
        JLabel l1 = new JLabel("Cảm ơn quý khách đã tin dùng!", SwingConstants.LEFT); 
        l1.setFont(new Font("Segoe UI", Font.PLAIN, 11)); l1.setForeground(textGray);
        JLabel l2 = new JLabel("Đổi trả trong 3 ngày kể từ lúc mua.", SwingConstants.LEFT); 
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 11)); l2.setForeground(textGray);
        pnlLeft.add(l1); pnlLeft.add(l2);

        // Khối Phải (Ký tên)
        JPanel pnlRight = new JPanel(new GridLayout(3, 1, 0, 3));
        pnlRight.setBackground(Color.WHITE);
        JLabel lbl3 = new JLabel("Nhân viên xác nhận", SwingConstants.CENTER); 
        lbl3.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel lbl4 = new JLabel("(Ký & Ghi rõ họ tên)", SwingConstants.CENTER); 
        lbl4.setFont(new Font("Segoe UI", Font.ITALIC, 11)); lbl4.setForeground(textGray);
        
        // Cắt gọn tên nhân viên nếu quá dài
        String tenRutGon = this.tenNhanVien;
        if(tenRutGon != null && tenRutGon.length() > 20) {
             tenRutGon = tenRutGon.substring(0, 18) + "...";
        }
        JLabel lbl5 = new JLabel(tenRutGon, SwingConstants.CENTER); 
        lbl5.setFont(new Font("Segoe UI", Font.BOLD, 12)); lbl5.setBorder(new EmptyBorder(20, 0, 0, 0));

        pnlRight.add(lbl3); pnlRight.add(lbl4); pnlRight.add(lbl5);

        pnl.add(pnlLeft); 
        pnl.add(pnlRight); 
        return pnl;
    }
}