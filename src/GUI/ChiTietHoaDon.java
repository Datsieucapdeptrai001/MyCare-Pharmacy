package GUI;
import java.awt.print.PrinterJob;
import java.awt.print.Printable;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import com.google.zxing.oned.Code128Writer;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import java.awt.image.BufferedImage;
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
    private java.util.Map<String, String> mapThuocLieuMau = new java.util.HashMap<>();
    private java.util.Set<String> dsThuocCutLieu = new java.util.HashSet<>(); // <-- BẠN ĐANG THIẾU DÒNG NÀY
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
    private String ghiChuHoaDon = "";
    public ChiTietHoaDon(Frame parent, String maHD, String ngay, String khachHang, String sdt, String phuongThuc, String tongTienCu, String tenNhanVien, List<Object[]> dsSanPhamGoc) {
        super(parent, "Chi tiết hóa đơn", true);
        try {
            BUS.BUS_ChiTietHoaDon busCT = new BUS.BUS_ChiTietHoaDon();
            List<Object[]> dsMoi = busCT.layDanhSachSanPhamTheoMaHD(maHD);
            if (dsMoi != null && !dsMoi.isEmpty()) {
                this.dsSanPham = dsMoi; 
            } else {
                this.dsSanPham = new java.util.ArrayList<>(dsSanPhamGoc);
            }
        } catch (Exception e) {
            this.dsSanPham = new java.util.ArrayList<>(dsSanPhamGoc);
            e.printStackTrace();
        }
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
                for (int i = 0; i < parts.length; i++) {
                    String p = parts[i].trim();
                    
                    // 1. BÓC TÁCH GHI CHÚ (Logic mới):
                    // Nếu là phần tử đầu tiên (i==0) và không phải là các keyword định sẵn
                    if (i == 0 && !p.isEmpty() && !p.startsWith("CASH:") && !p.startsWith("BANK") 
                        && !p.startsWith("Lưu nháp") && !p.startsWith("BS:") && !p.startsWith("Dùng điểm") 
                        && !p.startsWith("BATCH:") && !p.startsWith("LIEU_MAU:")) {
                        this.ghiChuHoaDon = p; // Lưu vào biến toàn cục đã khai báo ở Bước 1
                    }

                    // 2. CÁC XỬ LÝ NGHIỆP VỤ CŨ:
                    if (p.startsWith("CASH:")) {
                        this.tienKhachDuaThucTe = Long.parseLong(p.substring(5).replaceAll("[^0-9]", ""));
                    } else if (p.startsWith("Dùng điểm: -")) {
                        this.tienGiamTuDiemThucTe = Long.parseLong(p.substring(12).replaceAll("[^0-9]", ""));
                    } else if (p.startsWith("BS:")) {
                        bacSi = p.substring(3).trim();
                    } else if (p.startsWith("CS:")) {
                        coSo = p.substring(3).trim();
                    } else if (p.startsWith("CD:")) {
                        chuanDoan = p.substring(3).trim();
                    } else if (p.startsWith("KM:")) {
                        maKMs = p.substring(3).trim();
                    } else if (p.startsWith("TANG:")) {
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
                    else if (p.startsWith("LIEU_MAU:")) {
                        String danhSachTen = p.substring(9).trim(); 
                        String[] mangTen = danhSachTen.split("~"); // Đã đổi sang cắt bằng dấu ~
                        for (String item : mangTen) {
                            if (item.contains("=")) {
                                String[] splitItem = item.split("=");
                                this.mapThuocLieuMau.put(splitItem[0].trim().toLowerCase(), splitItem[1].trim());
                            } 
                        }
                    }
                    else if (p.startsWith("CUT_LIEU:")) {
                        String danhSachTen = p.substring(9).trim();
                        String[] mangTen = danhSachTen.split(",");
                        for (String ten : mangTen) {
                            this.dsThuocCutLieu.add(ten.trim().toLowerCase());
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        // === BƯỚC 1: Tổng tiền gốc (chưa VAT, chưa KM) để tính tỉ lệ KM ===
        this.tamTinhThucTe = 0;
        this.vatThucTe = 0;

        for (Object[] sp : dsSanPham) {
            long donGia = Long.parseLong(sp[4].toString().replaceAll("[^0-9]", ""));
            int sl = Integer.parseInt(sp[3].toString());
            this.tamTinhThucTe += donGia * sl;
        }

        // === BƯỚC 2: Lấy tiền giảm KM từ DB — tính trên giá GỐC (chưa VAT) ===
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
                                        // Tính KM% trên giá GỐC (chưa VAT) — đúng pháp lý
                                        this.tienGiamGiaThucTe += (long) (this.tamTinhThucTe * (giaTri / 100.0));
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

        // === BƯỚC 3: Tính VAT đúng pháp lý — trên từng dòng SAU KM ===
        double kmRatio = (tamTinhThucTe > 0) ? (double) tienGiamGiaThucTe / tamTinhThucTe : 0.0;
        String kmPctText = (kmRatio > 0) ? String.format("%.0f%%", kmRatio * 100) : "0%";

        for (Object[] sp : dsSanPham) {
            long donGia = Long.parseLong(sp[4].toString().replaceAll("[^0-9]", ""));
            int sl = Integer.parseInt(sp[3].toString());
            double vatPercent = 0.0;
            try { vatPercent = Double.parseDouble(sp[5].toString().replace("%", "").trim()); } catch(Exception ex) {}

            long tienGoc = donGia * sl;
            long tienGiam = Math.round(tienGoc * kmRatio);              // Giảm KM trước
            long thuanChuaThue = tienGoc - tienGiam;                    // Thành tiền sau KM, chưa thuế
            long tienVat = Math.round(thuanChuaThue * (vatPercent / 100.0)); // VAT tính trên giá sau KM

            sp[4] = String.format("%,d", donGia).replace(',', '.') + "đ";   // Đơn giá
            sp[5] = kmPctText;                                               // KM%
            sp[6] = String.format("%,d", thuanChuaThue).replace(',', '.') + "đ"; // Thành tiền (sau KM, chưa thuế)

            this.vatThucTe += tienVat;
        }

        long tongSauGiam = this.tamTinhThucTe - this.tienGiamGiaThucTe;
        this.tongThanhToanThucTe = tongSauGiam + this.vatThucTe - this.tienGiamTuDiemThucTe;
        if (this.tongThanhToanThucTe < 0) this.tongThanhToanThucTe = 0;

        if (this.tienKhachDuaThucTe == 0) this.tienKhachDuaThucTe = this.tongThanhToanThucTe;
        this.tienThoiThucTe = this.tienKhachDuaThucTe - this.tongThanhToanThucTe;
        if (this.tienThoiThucTe < 0) this.tienThoiThucTe = 0;

        String tongTienDungStr = String.format("%,d", this.tongThanhToanThucTe).replace(',', '.') + "đ";

        // ==========================================
        // UI SETUP - ĐÃ ÉP NHỎ XUỐNG 580px x 650px
        // ==========================================
        setSize(850, 750);
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
        pnlBody.add(createFooterTextPanel(maHD));

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
    private ImageIcon generateQR(String data, int size) {
        try {
            QRCodeWriter barcodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = barcodeWriter.encode(data, BarcodeFormat.QR_CODE, size, size);
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            img.createGraphics();
            Graphics2D g = (Graphics2D) img.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, size, size);
            g.setColor(Color.BLACK);
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (bitMatrix.get(i, j)) {
                        g.fillRect(i, j, 1, 1);
                    }
                }
            }
            return new ImageIcon(img);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private ImageIcon generateBarcode1D(String data, int width, int height) {
        try {
            Code128Writer barcodeWriter = new Code128Writer();
            // Sinh ma trận điểm ảnh cho mã vạch
            BitMatrix bitMatrix = barcodeWriter.encode(data, BarcodeFormat.CODE_128, width, height);
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            img.createGraphics();
            Graphics2D g = (Graphics2D) img.getGraphics();
            
            // Nền trắng
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            // Vạch đen
            g.setColor(Color.BLACK);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (bitMatrix.get(i, j)) {
                        g.fillRect(i, j, 1, 1);
                    }
                }
            }
            return new ImageIcon(img);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
        
        btnPrint.addActionListener(e -> {
            inHoaDonRaPDF();
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
        JPanel pnlWrapper = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlWrapper.setBackground(Color.WHITE);

        // ================= CỘT TRÁI =================
        JPanel pnlLeft = new JPanel();
        pnlLeft.setLayout(new BoxLayout(pnlLeft, BoxLayout.Y_AXIS));
        pnlLeft.setBackground(Color.WHITE);

        JPanel pnlKhachHang = createInfoBox("THÔNG TIN KHÁCH HÀNG");
        pnlKhachHang.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlKhachHang.add(new JLabel("<html><b>" + khachHang + "</b></html>"));
        pnlKhachHang.add(new JLabel("SĐT: " + (sdt == null || sdt.isEmpty() ? "Không cung cấp" : sdt)));
        if (sdt != null && !sdt.isEmpty()) {
            JLabel lblDiem = new JLabel("Điểm tích lũy: " + String.format("%,d", diemHienTai) + " điểm");
            lblDiem.setForeground(Color.decode("#E1304C"));
            lblDiem.setFont(new Font("Segoe UI", Font.BOLD, 12));
            pnlKhachHang.add(lblDiem);
        }
        pnlLeft.add(pnlKhachHang);

        String displayNote = (ghiChuHoaDon == null || ghiChuHoaDon.isEmpty()) ? "" : ghiChuHoaDon;
        if (!displayNote.isEmpty()) {
            pnlLeft.add(Box.createRigidArea(new Dimension(0, 10)));
            JPanel pnlGhiChu = createInfoBox("GHI CHÚ HÓA ĐƠN");
            pnlGhiChu.setAlignmentX(Component.LEFT_ALIGNMENT);

         // Đổi 260px thành 220px
            JLabel lblGhiChu = new JLabel("<html><div style='width:220px; line-height: 1.4;'>" + displayNote.replace("\n", "<br>") + "</div></html>");
            lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblGhiChu.setForeground(Color.decode("#1F2937"));
            pnlGhiChu.add(lblGhiChu);
            pnlLeft.add(pnlGhiChu);
        }

        // ================= CỘT PHẢI =================
        JPanel pnlRight = new JPanel();
        pnlRight.setLayout(new BoxLayout(pnlRight, BoxLayout.Y_AXIS));
        pnlRight.setBackground(Color.WHITE);

        JPanel pnlHoaDon = createInfoBox("THÔNG TIN HÓA ĐƠN");
        pnlHoaDon.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlHoaDon.add(new JLabel("<html>Nhân viên: <b>" + this.tenNhanVien + "</b></html>"));
        pnlHoaDon.add(new JLabel("<html>Phương thức: <font color='#009643'><b>" + phuongThuc + "</b></font></html>"));
        pnlRight.add(pnlHoaDon);

        if (bacSi != null && !bacSi.isEmpty()) {
            pnlRight.add(Box.createRigidArea(new Dimension(0, 10)));
            JPanel pnlKeDon = createInfoBox("THÔNG TIN KÊ ĐƠN");
            pnlKeDon.setAlignmentX(Component.LEFT_ALIGNMENT);

            // Đã bóp width xuống 260px để vừa vặn Form nhỏ
         // Đổi 260px thành 220px
            JLabel lblBS = new JLabel("<html><div style='width:220px; line-height: 1.5;'>"
                                    + "Bác sĩ: <b>" + bacSi + "</b><br>"
                                    + "Cơ sở: <b>" + coSo + "</b>"
                                    + (chuanDoan.isEmpty() ? "" : "<br>Chuẩn đoán: <b>" + chuanDoan + "</b>")
                                    + "</div></html>");
            pnlKeDon.add(lblBS);
            pnlRight.add(pnlKeDon);
        }

        JPanel wrapLeft = new JPanel(new BorderLayout());
        wrapLeft.setBackground(Color.WHITE);
        wrapLeft.add(pnlLeft, BorderLayout.NORTH);

        JPanel wrapRight = new JPanel(new BorderLayout());
        wrapRight.setBackground(Color.WHITE);
        wrapRight.add(pnlRight, BorderLayout.NORTH);

        pnlWrapper.add(wrapLeft);
        pnlWrapper.add(wrapRight);

        JPanel finalWrapper = new JPanel(new BorderLayout());
        finalWrapper.setBackground(Color.WHITE);
        finalWrapper.add(pnlWrapper, BorderLayout.NORTH); 

        return finalWrapper;
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

        String[] cols = {"Sản phẩm", "ĐVT", "SL", "Đơn giá", "KM%", "Thành tiền"};
        
        java.util.List<Object[]> displayList = new java.util.ArrayList<>();
        java.util.Map<String, java.util.List<Object[]>> lieuGroups = new java.util.LinkedHashMap<>();
        java.util.List<Object[]> standaloneItems = new java.util.ArrayList<>();
        java.util.List<Object[]> giftItems = new java.util.ArrayList<>();

        for (Object[] sp : dsSanPham) {
            String tenSP = sp[1] != null ? sp[1].toString() : "";
            if (tenSP.contains("QUÀ TẶNG") || tenSP.contains("[QUÀ TẶNG]")) {
                giftItems.add(sp);
                continue;
            }

            String tenCheck = tenSP.trim().toLowerCase();
            String tenLieuCuaThuoc = null;

            for (java.util.Map.Entry<String, String> entry : this.mapThuocLieuMau.entrySet()) {
                if (tenCheck.contains(entry.getKey()) || entry.getKey().contains(tenCheck)) {
                    tenLieuCuaThuoc = entry.getValue();
                    break;
                }
            }

            if (tenLieuCuaThuoc != null) {
                lieuGroups.computeIfAbsent(tenLieuCuaThuoc, k -> new java.util.ArrayList<>()).add(sp);
            } else {
                standaloneItems.add(sp);
            }
        }

        for (java.util.Map.Entry<String, java.util.List<Object[]>> entry : lieuGroups.entrySet()) {
            String tenLieu = entry.getKey();
            java.util.List<Object[]> itemsInLieu = entry.getValue();

            Object[] headerLieu = new Object[7];
            headerLieu[0] = "HEADER";
            headerLieu[1] = "<html><span style='font-family: Segoe UI; font-size: 12px; font-weight: bold; color: #1E40AF;'>[LIỀU] " + tenLieu.toUpperCase() + "</span></html>";
            headerLieu[2] = "Liều"; 
            headerLieu[3] = ""; headerLieu[4] = ""; headerLieu[5] = ""; headerLieu[6] = ""; 
            displayList.add(headerLieu);

            for (Object[] sp : itemsInLieu) {
                Object[] childRow = sp.clone();
                String name = sp[1].toString();
                String hsdInfo = "";
                
                if (sp.length > 7 && sp[7] != null) {
                    String hsdRaw = sp[7].toString();
                    if (hsdRaw.startsWith("HSD_")) {
                        String[] splitHsd = hsdRaw.split("HSD_.*?(?=\\d{2}/)");
                        if (splitHsd.length > 1) hsdInfo = "<br><span style='color: #059669; font-size: 10.5px;'>HSD: " + splitHsd[1] + "</span>";
                    } else if (!hsdRaw.trim().isEmpty()) {
                        hsdInfo = "<br><span style='color: #059669; font-size: 10.5px;'>HSD: " + hsdRaw + "</span>";
                    }
                }

                childRow[1] = "<html><div style='padding-top: 2px; margin-left: 15px;'>" 
                           + "<span style='font-family: Segoe UI; font-size: 12px; color: #111827;'>- " + name + "</span>" + hsdInfo
                           + "</div></html>";
                displayList.add(childRow);
            }
        }

        for (Object[] sp : standaloneItems) {
            Object[] normalRow = sp.clone();
            String name = sp[1].toString();
            String hsdInfo = "";
            
            if (sp.length > 7 && sp[7] != null) {
                String hsdRaw = sp[7].toString();
                if (hsdRaw.startsWith("HSD_")) {
                    String[] splitHsd = hsdRaw.split("HSD_.*?(?=\\d{2}/)");
                    if (splitHsd.length > 1) hsdInfo = "<br><span style='color: #059669; font-size: 10.5px;'>HSD: " + splitHsd[1] + "</span>";
                } else if (!hsdRaw.trim().isEmpty()) {
                    hsdInfo = "<br><span style='color: #059669; font-size: 10.5px;'>HSD: " + hsdRaw + "</span>";
                }
            }

            String tenCheck = name.trim().toLowerCase();
            if (this.dsThuocCutLieu.contains(tenCheck)) {
                normalRow[1] = "<html><div style='padding-top: 2px;'>"
                           + "<span style='font-family: Segoe UI; font-size: 12px; color: #111827;'>" + name + "</span>" + hsdInfo + "<br>"
                           + "<span style='font-family: Segoe UI; font-size: 10px; font-style: italic; color: #6B7280;'>(Thuốc cắt liều)</span>"
                           + "</div></html>";
            } else {
                normalRow[1] = "<html><span style='font-family: Segoe UI; font-size: 12px; color: #111827;'>" + name + hsdInfo + "</span></html>";
            }
            displayList.add(normalRow);
        }

        for (Object[] sp : giftItems) {
            Object[] giftRow = sp.clone();
            giftRow[1] = "<html><span style='font-family: Segoe UI; font-size: 12px; color: #111827;'>" + sp[1].toString() + "</span></html>";
            displayList.add(giftRow);
        }

        Object[][] data = new Object[displayList.size()][6];
        for (int i = 0; i < displayList.size(); i++) {
            Object[] rowObj = displayList.get(i);
            data[i][0] = rowObj[1]; data[i][1] = rowObj[2]; data[i][2] = rowObj[3];
            data[i][3] = rowObj[4]; data[i][4] = rowObj[5]; data[i][5] = rowObj[6];
        }

        DefaultTableModel model = new DefaultTableModel(data, cols) { 
            @Override public boolean isCellEditable(int r, int c) { return false; } 
        };
        
        JTable table = new JTable(model);
        int totalTableHeight = 0;
        
        // ==== CHIỀU CAO ĐÃ ĐƯỢC ÉP LẠI NHỎ HƠN ====
        for (int row = 0; row < table.getRowCount(); row++) {
            int rowHeight = 24; // <-- Sửa xuống 24
            Object val = table.getValueAt(row, 0); 
            if (val != null) {
                String valStr = val.toString().toLowerCase();
                if (valStr.contains("[liều]")) {
                    rowHeight = 22; // <-- Sửa xuống 22
                } else if (valStr.contains("<br")) {
                    rowHeight = 32; // <-- Sửa xuống 32 (cho dòng có HSD)
                }
            }
            table.setRowHeight(row, rowHeight);
            totalTableHeight += rowHeight;
        }
        
        table.setShowGrid(false); 
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JTableHeader header = table.getTableHeader(); 
        header.setBackground(Color.decode("#F1F5F9")); 
        header.setForeground(textDark);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12)); 
        header.setPreferredSize(new Dimension(0, 32)); // Header cũng bo hẹp xíu
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderGray));

        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isS, boolean hasF, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, value, isS, hasF, r, c);
                String text = (value != null) ? value.toString() : "";
                
                if (text.contains("[QUÀ TẶNG]") || text.contains("QUÀ TẶNG")) {
                    text = text.replace("[QUÀ TẶNG]", "").trim();
                    lbl.setIcon(new MenuIcon("QUA_TANG", 14)); 
                    lbl.setForeground(Color.decode("#DC2626")); 
                } else {
                    lbl.setIcon(null);
                }
                
                lbl.setText(text);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
                return lbl;
            }
        });

        DefaultTableCellRenderer center = new DefaultTableCellRenderer(); center.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer(); right.setHorizontalAlignment(JLabel.RIGHT);
        
        // ÉP CHẶT THÊM CÁC CỘT BÊN PHẢI ĐỂ CỘT SẢN PHẨM KHÔNG CÒN KHOẢNG TRẮNG DƯ THỪA
        table.getColumnModel().getColumn(1).setMinWidth(45); table.getColumnModel().getColumn(1).setMaxWidth(55);
        table.getColumnModel().getColumn(2).setMinWidth(30); table.getColumnModel().getColumn(2).setMaxWidth(40);
        table.getColumnModel().getColumn(3).setMinWidth(75); table.getColumnModel().getColumn(3).setMaxWidth(85);
        table.getColumnModel().getColumn(4).setCellRenderer(center); table.getColumnModel().getColumn(4).setMinWidth(40); table.getColumnModel().getColumn(4).setMaxWidth(50);
        table.getColumnModel().getColumn(5).setCellRenderer(right); table.getColumnModel().getColumn(5).setMinWidth(85); table.getColumnModel().getColumn(5).setMaxWidth(95);
        table.getColumnModel().getColumn(0).setMinWidth(150);

        JScrollPane sp = new JScrollPane(table); 
        sp.getViewport().setBackground(Color.WHITE); 
        sp.setBorder(BorderFactory.createLineBorder(borderGray));
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        
        int actualTableHeight = totalTableHeight + 32; 
        sp.setPreferredSize(new Dimension(0, actualTableHeight));
        sp.setMinimumSize(new Dimension(0, actualTableHeight));
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, actualTableHeight));
        
        pnl.setMaximumSize(new Dimension(Integer.MAX_VALUE, actualTableHeight + 32));
        pnl.add(sp, BorderLayout.CENTER); 
        return pnl;
    }
    private JPanel createSummaryPanel(String phuongThuc) {
        JPanel pnlWrapper = new JPanel(new GridLayout(1, 2, 10, 0)); 
        pnlWrapper.setBackground(Color.WHITE);

        int totalRows = 6; 
        if (tienGiamTuDiemThucTe > 0) totalRows++;

        // ================= CỘT TRÁI (TỔNG TIỀN) =================
        JPanel pnlTotal = new JPanel(new GridLayout(totalRows, 2, 0, 8)); 
        pnlTotal.setBackground(Color.WHITE);
        pnlTotal.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borderGray, 1, true), 
            new EmptyBorder(10, 10, 10, 10)
        ));

        long tongSauGiam = tamTinhThucTe - tienGiamGiaThucTe;
        int tongSoSanPham = 0;
        for (Object[] sp : dsSanPham) {
            tongSoSanPham += Integer.parseInt(sp[3].toString()); 
        }

        pnlTotal.add(new JLabel("Tổng số sản phẩm:"));
        JLabel lblTongSPValue = createRightAlignLabel(String.valueOf(tongSoSanPham));
        lblTongSPValue.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlTotal.add(lblTongSPValue);

        pnlTotal.add(new JLabel("Tạm tính (giá gốc):"));
        pnlTotal.add(createRightAlignLabel(String.format("%,d", tamTinhThucTe).replace(',', '.') + "đ"));

        JLabel lblGiamGiaText = new JLabel("Khuyến mãi:"); lblGiamGiaText.setForeground(Color.decode("#EF4444"));
        JLabel lblGiamGiaValue = createRightAlignLabel("-" + String.format("%,d", tienGiamGiaThucTe).replace(',', '.') + "đ");
        lblGiamGiaValue.setFont(new Font("Segoe UI", Font.BOLD, 13)); lblGiamGiaValue.setForeground(Color.decode("#EF4444"));
        pnlTotal.add(lblGiamGiaText); pnlTotal.add(lblGiamGiaValue);

        pnlTotal.add(new JLabel("Tổng sau giảm (chưa thuế):"));
        pnlTotal.add(createRightAlignLabel(String.format("%,d", tongSauGiam).replace(',', '.') + "đ"));

        pnlTotal.add(new JLabel("Thuế VAT:"));
        pnlTotal.add(createRightAlignLabel("+" + String.format("%,d", vatThucTe).replace(',', '.') + "đ"));

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

        // ================= CỘT PHẢI (THANH TOÁN) =================
        JPanel pnlPayment = new JPanel();
        pnlPayment.setLayout(new BoxLayout(pnlPayment, BoxLayout.Y_AXIS));
        pnlPayment.setBackground(isDaHuy ? bgRed : bgYellow);
        // Đã giảm Padding xuống để ô màu vàng bo nhỏ và ôm sát chữ hơn
        pnlPayment.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(isDaHuy ? textRed : Color.decode("#FDE047"), 1, true), 
            new EmptyBorder(8, 10, 8, 10) 
        ));

        // Dòng 1: Tiêu đề
        JLabel lblPayTitle = new JLabel(isDaHuy ? "TRẠNG THÁI" : "THANH TOÁN");
        lblPayTitle.setFont(new Font("Segoe UI", Font.BOLD, 12)); 
        lblPayTitle.setForeground(isDaHuy ? textRed : Color.decode("#CA8A04"));
        lblPayTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlPayment.add(lblPayTitle);
        
        pnlPayment.add(Box.createRigidArea(new Dimension(0, 8))); // Khoảng cách

        // Dòng 2 & 3: Các thông số được bọc trong BorderLayout để ÉP dính lề Trái-Phải
        if (isDaHuy) {
            JLabel lblHuy = new JLabel("ĐÃ BỊ HỦY"); 
            lblHuy.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
            lblHuy.setForeground(textRed);
            lblHuy.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlPayment.add(lblHuy);
        } else if(phuongThuc.equalsIgnoreCase("Chuyển khoản")) {
            JPanel rowStatus = new JPanel(new BorderLayout());
            rowStatus.setOpaque(false);
            rowStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20)); 
            rowStatus.add(new JLabel("Trạng thái:"), BorderLayout.WEST);
            
            JLabel lblStatusValue = new JLabel("Đã nhận");
            lblStatusValue.setForeground(primaryGreen); 
            lblStatusValue.setFont(new Font("Segoe UI", Font.BOLD, 12));
            rowStatus.add(lblStatusValue, BorderLayout.EAST);
            
            rowStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlPayment.add(rowStatus);
        } else {
            // Khách đưa
            JPanel rowKhachDua = new JPanel(new BorderLayout());
            rowKhachDua.setOpaque(false);
            rowKhachDua.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20)); 
            rowKhachDua.add(new JLabel("Khách đưa:"), BorderLayout.WEST);
            
            JLabel lblKhachDuaValue = new JLabel(String.format("%,d", tienKhachDuaThucTe).replace(',', '.') + "đ");
            lblKhachDuaValue.setFont(new Font("Segoe UI", Font.BOLD, 12)); 
            lblKhachDuaValue.setForeground(primaryGreen);
            rowKhachDua.add(lblKhachDuaValue, BorderLayout.EAST);
            
            rowKhachDua.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlPayment.add(rowKhachDua);
            
            pnlPayment.add(Box.createRigidArea(new Dimension(0, 6))); // Khoảng cách nhỏ giữa 2 dòng

            // Thối lại
            JPanel rowThoiLai = new JPanel(new BorderLayout());
            rowThoiLai.setOpaque(false);
            rowThoiLai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            
            JLabel lblThoiLaiText = new JLabel("Thối lại:"); 
            lblThoiLaiText.setFont(new Font("Segoe UI", Font.PLAIN, 12)); 
            lblThoiLaiText.setForeground(textGray);
            rowThoiLai.add(lblThoiLaiText, BorderLayout.WEST);
            
            JLabel lblThoiLaiValue = new JLabel(String.format("%,d", tienThoiThucTe).replace(',', '.') + "đ");
            lblThoiLaiValue.setFont(new Font("Segoe UI", Font.PLAIN, 12)); 
            lblThoiLaiValue.setForeground(textGray);
            rowThoiLai.add(lblThoiLaiValue, BorderLayout.EAST);
            
            rowThoiLai.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlPayment.add(rowThoiLai);
        }

        // ================= RÁP LẠI VÀ CHỐNG KÉO DÃN =================
        JPanel wrapLeft = new JPanel(new BorderLayout());
        wrapLeft.setBackground(Color.WHITE);
        wrapLeft.add(pnlTotal, BorderLayout.NORTH);

        JPanel wrapRight = new JPanel(new BorderLayout());
        wrapRight.setBackground(Color.WHITE);
        wrapRight.add(pnlPayment, BorderLayout.NORTH);

        pnlWrapper.add(wrapLeft); 
        pnlWrapper.add(wrapRight); 

        JPanel finalWrapper = new JPanel(new BorderLayout());
        finalWrapper.setBackground(Color.WHITE);
        finalWrapper.add(pnlWrapper, BorderLayout.NORTH);

        return finalWrapper;
    }

    private JLabel createRightAlignLabel(String text) { JLabel label = new JLabel(text); label.setHorizontalAlignment(SwingConstants.RIGHT); return label; }

    private JPanel createFooterTextPanel(String maHD) {
        JPanel pnl = new JPanel(); 
        // Dùng BoxLayout xếp dọc từ trên xuống
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS)); 
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(15, 0, 20, 0));

        // 1. MÃ VẠCH (Canh giữa)
        ImageIcon barcodeIcon = generateBarcode1D(maHD, 220, 50); 
        if (barcodeIcon != null) {
            JLabel lblBarcode = new JLabel(barcodeIcon);
            lblBarcode.setAlignmentX(Component.CENTER_ALIGNMENT); // Căn giữa
            pnl.add(lblBarcode);
            
            JLabel lblBarcodeText = new JLabel("Mã HĐ: " + maHD);
            lblBarcodeText.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblBarcodeText.setForeground(textDark);
            lblBarcodeText.setAlignmentX(Component.CENTER_ALIGNMENT); // Căn giữa
            pnl.add(lblBarcodeText);
        }
        
        // Tạo khoảng trắng 15px
        pnl.add(Box.createRigidArea(new Dimension(0, 15))); 

        // 2. LỜI CẢM ƠN (Canh giữa)
        JLabel l1 = new JLabel("Cảm ơn quý khách đã tin dùng!"); 
        l1.setFont(new Font("Segoe UI", Font.PLAIN, 11)); 
        l1.setForeground(textGray);
        l1.setAlignmentX(Component.CENTER_ALIGNMENT); 
        
        JLabel l2 = new JLabel("Đổi trả trong 3 ngày kể từ lúc mua. Hóa đơn chỉ đổi trả 1 lần."); 
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 11)); 
        l2.setForeground(textGray);
        l2.setAlignmentX(Component.CENTER_ALIGNMENT);;
        
        pnl.add(l1); 
        pnl.add(l2);

        return pnl;
    }
    private void inHoaDonRaPDF() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) return NO_SUCH_PAGE;
                
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                
                // Thu nhỏ toàn bộ nội dung cửa sổ để vừa với kích thước giấy in
                double widthScale = pageFormat.getImageableWidth() / getContentPane().getWidth();
                g2d.scale(widthScale, widthScale);
                
                // In toàn bộ giao diện (Nó sẽ tự động in thành trắng đen đẹp mắt trên giấy)
                getContentPane().printAll(g2d);
                return PAGE_EXISTS;
            }
        });
        
        // Mở hộp thoại để người dùng chọn máy in (Chọn "Microsoft Print to PDF" để lưu PDF)
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi in ấn: " + ex.getMessage());
            }
        }
    }
}