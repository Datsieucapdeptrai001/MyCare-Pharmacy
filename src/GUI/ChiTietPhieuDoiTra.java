package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import Utils.MenuIcon;
import ConnectDB.ConnectDB;

public class ChiTietPhieuDoiTra extends JDialog {
    
    private Color primaryBlue = Color.decode("#1967D2"); 
    private Color primaryRed = Color.decode("#DC2626");  
    private Color textDark = Color.decode("#212B36");
    private Color textGray = Color.decode("#6B7280");
    private Color borderGray = Color.decode("#DFE3E8");
    
    private String maPhieu, loaiPhieu, trangThai, ngayTao, hoaDonGoc, khachHang, lyDo, nhanVien;
    private String tienHoanThucTe, chenhLechThucTe;

    public ChiTietPhieuDoiTra(Frame parent, String maPhieu, String loaiPhieu, String trangThai, 
                              String ngayTao, String hoaDonGoc, String khachHang, String lyDo, String nhanVien,
                              String tienHoan, String chenhLech) {
        super(parent, "Chi tiết phiếu " + loaiPhieu, true);
        this.maPhieu = maPhieu;
        this.loaiPhieu = loaiPhieu;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.hoaDonGoc = hoaDonGoc;
        this.khachHang = khachHang;
        this.lyDo = lyDo;
        this.nhanVien = nhanVien;
        this.tienHoanThucTe = tienHoan;
        this.chenhLechThucTe = chenhLech;

        initUI();
    }

    private void initUI() {
        // 1. Tăng nhẹ kích thước để không gian thoáng hơn
        setSize(800, 750); 
        setLocationRelativeTo(getParent());
        setUndecorated(true);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        Color headerColor = loaiPhieu.equalsIgnoreCase("Trả hàng") ? primaryRed : primaryBlue;
        add(createHeaderPanel(headerColor), BorderLayout.NORTH);

        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(10, 20, 10, 20)); 

        pnlBody.add(createCompanyTitlePanel());
        pnlBody.add(createDashedLine());
        pnlBody.add(createInfoPanel());
        pnlBody.add(createDashedLine());
        pnlBody.add(createProductListPanel());
        pnlBody.add(createDashedLine());
        pnlBody.add(createSignaturePanel());

        JScrollPane scrollPane = new JScrollPane(pnlBody);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // ==========================================
        // 2. KHU VỰC XỬ LÝ THANH CUỘN (SCROLLBAR)
        // ==========================================
        // Tắt vĩnh viễn thanh cuộn ngang
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // Cho phép cuộn dọc nhưng ẨN hình dạng của nó đi (Đưa width = 0)
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0)); 
        
        add(scrollPane, BorderLayout.CENTER);
    }

    private String fetchSoDienThoai(String maHDGoc) {
        String sdt = "Không cung cấp";
        String sql = "SELECT kh.sdt FROM HoaDon hd JOIN KhachHang kh ON hd.khachHangId = kh.id WHERE hd.id = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maHDGoc);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String phone = rs.getString("sdt");
                    if (phone != null && !phone.isEmpty()) sdt = phone;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return sdt;
    }

    private List<Object[]> layDanhSachSanPhamDoi(String maPhieu) {
        List<Object[]> list = new ArrayList<>();
        // SỬA LỖI: Thay vì sp.tenSanPham, ta dùng sp.ten cho khớp với Database
        String sql = "SELECT sp.ten, ct.soLuong FROM ChiTietHoaDon ct " +
                     "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                     "WHERE ct.hoaDonId = ? AND ct.ghiChu = 'DOI_LAY'"; 
                     
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maPhieu);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    // Trích xuất đúng cột ten
                    list.add(new Object[]{rs.getString("ten"), rs.getInt("soLuong")});
                }
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return list;
    }

    private JPanel createHeaderPanel(Color bgColor) {
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(bgColor);
        pnlHeader.setBorder(new EmptyBorder(8, 15, 8, 15));

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlLeft.setOpaque(false);
        JLabel lblTitle = new JLabel("Phiếu " + loaiPhieu + " — " + maPhieu);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblStatus = new JLabel(trangThai);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(Color.WHITE);
        lblStatus.setBackground(new Color(255, 255, 255, 50)); 
        lblStatus.setOpaque(true);
        lblStatus.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(255, 255, 255, 100), 1, true),
            new EmptyBorder(2, 8, 2, 8)
        ));

        pnlLeft.add(new JLabel(new MenuIcon("DOCUMENT", 18)));
        pnlLeft.add(lblTitle);
        pnlLeft.add(lblStatus);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRight.setOpaque(false);

        JButton btnPrint = new JButton("In");
        btnPrint.setIcon(new MenuIcon("PRINT", 16)); 
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPrint.setContentAreaFilled(false);
        btnPrint.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(255, 255, 255, 100), 1, true),
            new EmptyBorder(4, 12, 4, 12)
        ));
        btnPrint.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnClose = new JButton("X");
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        pnlRight.add(btnPrint);
        pnlRight.add(btnClose);

        pnlHeader.add(pnlLeft, BorderLayout.WEST);
        pnlHeader.add(pnlRight, BorderLayout.EAST);
        return pnlHeader;
    }

    private JPanel createCompanyTitlePanel() {
        JPanel pnl = new JPanel(new GridLayout(4, 1, 0, 2)); // Cắt khoảng trắng dòng
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(5, 0, 5, 0));

        JLabel lblName = new JLabel("MYCARE PHARMACY", SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblName.setForeground(Color.decode("#152A4B"));
        
        JLabel lblAddress = new JLabel("123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh", SwingConstants.CENTER);
        lblAddress.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblAddress.setForeground(textGray);
        
        JLabel lblContact = new JLabel("SĐT: 028 3812 3456", SwingConstants.CENTER);
        lblContact.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblContact.setForeground(textGray);
        
        String title = loaiPhieu.equalsIgnoreCase("Trả hàng") ? "PHIẾU TRẢ HÀNG & HOÀN TIỀN" : "PHIẾU ĐỔI HÀNG";
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(textDark);
        lblTitle.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel lblMa = new JLabel(maPhieu, SwingConstants.CENTER);
        lblMa.setFont(new Font("Monospaced", Font.PLAIN, 12));
        lblMa.setForeground(textGray);

        pnl.add(lblName); pnl.add(lblAddress); pnl.add(lblContact);
        
        JPanel pnlTitleWrapper = new JPanel(new BorderLayout());
        pnlTitleWrapper.setBackground(Color.WHITE);
        pnlTitleWrapper.add(lblTitle, BorderLayout.NORTH);
        pnlTitleWrapper.add(lblMa, BorderLayout.CENTER);

        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBackground(Color.WHITE);
        pnlMain.add(pnl, BorderLayout.NORTH);
        pnlMain.add(pnlTitleWrapper, BorderLayout.CENTER);
        return pnlMain;
    }

    private JPanel createInfoPanel() {
        // Thay vì dùng GridLayout cho tất cả, ta tách Lý do ra riêng để không làm phình cột
        JPanel pnlWrapper = new JPanel(new BorderLayout(0, 8)); 
        pnlWrapper.setBackground(Color.WHITE);
        pnlWrapper.setBorder(new EmptyBorder(5, 0, 5, 0));

        // 1. Grid 3 dòng x 2 cột cho các thông tin ngắn
        JPanel pnlGrid = new JPanel(new GridLayout(3, 2, 10, 5));
        pnlGrid.setBackground(Color.WHITE);
        pnlGrid.add(createLabelPair("Ngày tạo: ", ngayTao));
        pnlGrid.add(createLabelPair("NV xử lý: ", nhanVien));
        pnlGrid.add(createLabelPair("Hóa đơn gốc: ", hoaDonGoc));
        pnlGrid.add(createLabelPair("Khách hàng: ", khachHang));
        
        String sdtThucTe = fetchSoDienThoai(hoaDonGoc); 
        pnlGrid.add(createLabelPair("SĐT: ", sdtThucTe)); 
        pnlGrid.add(new JLabel("")); // Ô trống cho đều grid

        // 2. Panel riêng cho Lý do nằm trải dài hết chiều ngang
        JPanel pnlLyDo = new JPanel(new BorderLayout());
        pnlLyDo.setBackground(Color.WHITE);
        
        JLabel lblLyDoTitle = new JLabel("Lý do: ");
        lblLyDoTitle.setForeground(textGray);
        lblLyDoTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLyDoTitle.setVerticalAlignment(SwingConstants.TOP); // Ép chữ lên trên cùng nếu rớt dòng
        lblLyDoTitle.setBorder(new EmptyBorder(0, 0, 0, 5));
        
        // Thẻ div giới hạn độ rộng khoảng 460px để tự động xuống dòng mượt mà
        JLabel lblLyDoVal = new JLabel("<html><div style='width: 460px; line-height: 1.3;'>" + lyDo + "</div></html>");
        lblLyDoVal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLyDoVal.setForeground(textDark);
        
        pnlLyDo.add(lblLyDoTitle, BorderLayout.WEST);
        pnlLyDo.add(lblLyDoVal, BorderLayout.CENTER);

        // Gom 2 khối lại
        pnlWrapper.add(pnlGrid, BorderLayout.NORTH);
        pnlWrapper.add(pnlLyDo, BorderLayout.CENTER);
        
        return pnlWrapper;
    }

    private JPanel createLabelPair(String title, String value) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnl.setBackground(Color.WHITE);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(textGray);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblValue.setForeground(textDark);
        
        pnl.add(lblTitle); pnl.add(lblValue);
        return pnl;
    }

    private JPanel createProductListPanel() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(5, 0, 5, 0));

        JLabel lblTitle = new JLabel("SẢN PHẨM TRẢ LẠI:");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(textDark);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.add(lblTitle);
        pnl.add(Box.createVerticalStrut(5));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderGray));
        
        JLabel h1 = new JLabel("Sản phẩm"); h1.setForeground(textGray); h1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JPanel pnlRightHeader = new JPanel(new GridLayout(1, 3, 5, 0));
        pnlRightHeader.setOpaque(false);
        pnlRightHeader.setPreferredSize(new Dimension(220, 25));
        JLabel h2 = new JLabel("SL", SwingConstants.CENTER); h2.setForeground(textGray); h2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel h3 = new JLabel("Đơn giá", SwingConstants.RIGHT); h3.setForeground(textGray); h3.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel h4 = new JLabel("Thành tiền", SwingConstants.RIGHT); h4.setForeground(textGray); h4.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlRightHeader.add(h2); pnlRightHeader.add(h3); pnlRightHeader.add(h4);
        
        pnlHeader.add(h1, BorderLayout.CENTER); pnlHeader.add(pnlRightHeader, BorderLayout.EAST);
        pnl.add(pnlHeader); pnl.add(Box.createVerticalStrut(5));

        long tongTienSPTra = 0;
        try {
            BUS.BUS_TraHang busTra = new BUS.BUS_TraHang();
            List<Object[]> dsSP = busTra.layChiTietPhieu(maPhieu); 
            if (dsSP != null) {
                for (Object[] sp : dsSP) {
                    String tenSP = sp[0] != null ? sp[0].toString() : "Sản phẩm lỗi";
                    String soLuong = sp[1] != null ? sp[1].toString() : "0";
                    long gia = 0;
                    if(sp[3] != null) gia = Long.parseLong(sp[3].toString().replaceAll("[^0-9]", ""));
                    long thanhTien = gia * Integer.parseInt(soLuong);
                    tongTienSPTra += thanhTien;

                    JPanel row = new JPanel(new BorderLayout());
                    row.setBackground(Color.WHITE);
                    row.setBorder(new EmptyBorder(5, 0, 5, 0));
                    
                    JLabel lblName = new JLabel(tenSP);
                    lblName.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    
                    JPanel rightVals = new JPanel(new GridLayout(1, 3, 5, 0));
                    rightVals.setOpaque(false);
                    rightVals.setPreferredSize(new Dimension(220, 25));
                    
                    JLabel lblSL = new JLabel(soLuong, SwingConstants.CENTER); lblSL.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    JLabel lblGia = new JLabel(String.format("%,d", gia) + "đ", SwingConstants.RIGHT); lblGia.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    JLabel lblThanhTien = new JLabel(String.format("%,d", thanhTien) + "đ", SwingConstants.RIGHT); lblThanhTien.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    
                    rightVals.add(lblSL); rightVals.add(lblGia); rightVals.add(lblThanhTien);
                    row.add(lblName, BorderLayout.CENTER); row.add(rightVals, BorderLayout.EAST);
                    pnl.add(row);
                }
            }
        } catch (Exception ex) { }

        pnl.add(createDashedLine());

        JPanel pnlTongSP = new JPanel(new BorderLayout());
        pnlTongSP.setBackground(Color.WHITE);
        pnlTongSP.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel lblTongTxt = new JLabel("Tổng tiền SP trả:"); lblTongTxt.setForeground(textGray); lblTongTxt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel lblTongVal = new JLabel(String.format("%,d", tongTienSPTra) + "đ"); lblTongVal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnlTongSP.add(lblTongTxt, BorderLayout.WEST); pnlTongSP.add(lblTongVal, BorderLayout.EAST);
        pnl.add(pnlTongSP);

        if (loaiPhieu.equalsIgnoreCase("Đổi hàng")) {
            JPanel pnlDoi = new JPanel();
            pnlDoi.setLayout(new BoxLayout(pnlDoi, BoxLayout.Y_AXIS));
            pnlDoi.setBackground(Color.decode("#F8F9FA"));
            pnlDoi.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.decode("#F1F3F5"), 1, true), new EmptyBorder(10, 10, 10, 10)
            ));
            
            JLabel lblDoiTxt = new JLabel("SẢN PHẨM ĐỔI LẤY:");
            lblDoiTxt.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblDoiTxt.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlDoi.add(lblDoiTxt); pnlDoi.add(Box.createVerticalStrut(5));
            
            List<Object[]> dsSPDoi = layDanhSachSanPhamDoi(maPhieu);
            if (dsSPDoi != null && !dsSPDoi.isEmpty()) {
                for (Object[] spDoi : dsSPDoi) {
                    JLabel lblSPDoi = new JLabel("• " + spDoi[0].toString() + " (SL: " + spDoi[1].toString() + ")"); 
                    lblSPDoi.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    lblSPDoi.setAlignmentX(Component.LEFT_ALIGNMENT);
                    pnlDoi.add(lblSPDoi); pnlDoi.add(Box.createVerticalStrut(2));
                }
            } else {
                JLabel lblEmpty = new JLabel("Chưa có SP đổi lấy"); lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 12)); lblEmpty.setForeground(Color.GRAY); lblEmpty.setAlignmentX(Component.LEFT_ALIGNMENT);
                pnlDoi.add(lblEmpty);
            }
            pnl.add(Box.createVerticalStrut(5)); pnl.add(pnlDoi);
        }

        JPanel pnlHoan = new JPanel(new BorderLayout());
        pnlHoan.setBackground(Color.WHITE);
        pnlHoan.setBorder(new EmptyBorder(10, 0, 5, 0));
        
        JLabel lblHoanTxt = new JLabel(loaiPhieu.equalsIgnoreCase("Trả hàng") ? "Số tiền hoàn lại khách:" : "Chênh lệch / Bù trừ:");
        lblHoanTxt.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHoanTxt.setForeground(loaiPhieu.equalsIgnoreCase("Trả hàng") ? primaryRed : primaryBlue); 
        
        // Gắn tiền thực tế đã truyền qua
        String realMoney = loaiPhieu.equalsIgnoreCase("Trả hàng") ? tienHoanThucTe : chenhLechThucTe;
        JLabel lblHoanVal = new JLabel(realMoney);
        lblHoanVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHoanVal.setForeground(lblHoanTxt.getForeground());

        pnlHoan.add(lblHoanTxt, BorderLayout.WEST); pnlHoan.add(lblHoanVal, BorderLayout.EAST);
        pnl.add(pnlHoan);

        return pnl;
    }

    private JPanel createSignaturePanel() {
        JPanel pnl = new JPanel(new GridLayout(1, 2));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(10, 0, 10, 0)); // Ép nhỏ lề dưới

        JPanel pnlKhach = new JPanel(new GridLayout(2, 1, 0, 3));
        pnlKhach.setBackground(Color.WHITE);
        JLabel lbl1 = new JLabel("Khách hàng", SwingConstants.CENTER); lbl1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel lbl2 = new JLabel("(Ký & Ghi rõ họ tên)", SwingConstants.CENTER); lbl2.setFont(new Font("Segoe UI", Font.ITALIC, 11)); lbl2.setForeground(textGray);
        pnlKhach.add(lbl1); pnlKhach.add(lbl2);

        JPanel pnlNV = new JPanel(new GridLayout(3, 1, 0, 3)); 
        pnlNV.setBackground(Color.WHITE);
        JLabel lbl3 = new JLabel("Nhân viên xử lý", SwingConstants.CENTER); lbl3.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel lbl4 = new JLabel("(Ký & Ghi rõ họ tên)", SwingConstants.CENTER); lbl4.setFont(new Font("Segoe UI", Font.ITALIC, 11)); lbl4.setForeground(textGray);
        JLabel lbl5 = new JLabel(nhanVien, SwingConstants.CENTER); lbl5.setFont(new Font("Segoe UI", Font.BOLD, 12)); lbl5.setBorder(new EmptyBorder(20, 0, 0, 0));

        pnlNV.add(lbl3); pnlNV.add(lbl4); pnlNV.add(lbl5);

        pnl.add(pnlKhach); pnl.add(pnlNV);
        return pnl;
    }

    private JPanel createDashedLine() {
        JPanel linePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(borderGray);
                g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
                g2d.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
            }
        };
        linePanel.setPreferredSize(new Dimension(100, 15));
        linePanel.setBackground(Color.WHITE);
        linePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 15));
        return linePanel;
    }
}