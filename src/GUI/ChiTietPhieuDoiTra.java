package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;
import Utils.UserSession;
import Utils.MenuIcon;
import BUS.BUS_TraHang;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;

public class ChiTietPhieuDoiTra extends JDialog {
    
    private Color primaryBlue = Color.decode("#1967D2"); 
    private Color primaryRed = Color.decode("#DC2626");  
    private Color textDark = Color.decode("#212B36");
    private Color textGray = Color.decode("#6B7280");
    private Color borderGray = Color.decode("#DFE3E8");
    
    private String maPhieu, loaiPhieu, trangThai, ngayTao, hoaDonGoc, khachHang, lyDo, nhanVien;
    private String tienHoanThucTe, chenhLechThucTe;
    
    private BUS_TraHang busTraHang = new BUS_TraHang(); 

    public ChiTietPhieuDoiTra(Frame parent, String maPhieu, String loaiPhieu, String trangThai, 
            String ngayTao, String hoaDonGoc, String khachHang, String lyDo, String nhanVienTruyenVao,
            String tienHoan, String chenhLech) {
        super(parent, "Chi tiết phiếu " + loaiPhieu, true);
        this.maPhieu = maPhieu;
        this.loaiPhieu = loaiPhieu;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
        this.hoaDonGoc = hoaDonGoc;
        this.khachHang = khachHang;
        this.lyDo = lyDo;
        this.tienHoanThucTe = tienHoan;
        this.chenhLechThucTe = chenhLech;

        String tenNVTuSession = Utils.UserSession.getInstance().getTenHienThi();
        if (tenNVTuSession != null && !tenNVTuSession.isEmpty() && !tenNVTuSession.equals("Người dùng")) {
            this.nhanVien = tenNVTuSession; 
        } else {
            this.nhanVien = nhanVienTruyenVao; 
        }

        initUI();
    }

    private void initUI() {
        setSize(700, 650); 
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

        add(pnlBody, BorderLayout.CENTER);
        pack(); 
        setLocationRelativeTo(getParent());
    }

    private String fetchSoDienThoai(String maHDGoc) {
        return busTraHang.laySoDienThoaiKhachHang(maHDGoc);
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
        JPanel pnl = new JPanel(new GridLayout(4, 1, 0, 2)); 
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
        JPanel pnlWrapper = new JPanel(new BorderLayout(0, 8)); 
        pnlWrapper.setBackground(Color.WHITE);
        pnlWrapper.setBorder(new EmptyBorder(5, 0, 5, 0));

        JPanel pnlGrid = new JPanel(new GridLayout(3, 2, 10, 5));
        pnlGrid.setBackground(Color.WHITE);
        pnlGrid.add(createLabelPair("Ngày tạo: ", ngayTao));
        pnlGrid.add(createLabelPair("NV xử lý: ", nhanVien));
        pnlGrid.add(createLabelPair("Hóa đơn gốc: ", hoaDonGoc));
        pnlGrid.add(createLabelPair("Khách hàng: ", khachHang));
        
        String sdtThucTe = fetchSoDienThoai(hoaDonGoc); 
        pnlGrid.add(createLabelPair("SĐT: ", sdtThucTe)); 
        pnlGrid.add(new JLabel("")); 

        JPanel pnlLyDo = new JPanel(new BorderLayout());
        pnlLyDo.setBackground(Color.WHITE);
        
        JLabel lblLyDoTitle = new JLabel("Lý do: ");
        lblLyDoTitle.setForeground(textGray);
        lblLyDoTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLyDoTitle.setVerticalAlignment(SwingConstants.TOP); 
        lblLyDoTitle.setBorder(new EmptyBorder(0, 0, 0, 5));
        
        JLabel lblLyDoVal = new JLabel("<html><div style='width: 460px; line-height: 1.3;'>" + lyDo + "</div></html>");
        lblLyDoVal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLyDoVal.setForeground(textDark);
        
        pnlLyDo.add(lblLyDoTitle, BorderLayout.WEST);
        pnlLyDo.add(lblLyDoVal, BorderLayout.CENTER);

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
        JPanel pnl = new JPanel(new BorderLayout(0, 5)); 
        pnl.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("SẢN PHẨM TRẢ LẠI:");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnl.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"Sản phẩm", "SL", "Đơn giá", "Thành tiền"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { 
            @Override public boolean isCellEditable(int r, int c) { return false; } 
        };

        // BÓC TÁCH TRỰC TIẾP TỪ GHI CHÚ DATABASE (CHỐNG LỖI IN DƯ VÀ ĐƠN GIÁ 0Đ)
        List<Object[]> dsSP = new java.util.ArrayList<>();
        try {
            BUS.BUS_HoaDon busHD = new BUS.BUS_HoaDon();
            Entity.HoaDon hd = busHD.getHoaDonTheoMa(maPhieu);
            if (hd != null && hd.getGhiChu() != null) {
                String[] parts = hd.getGhiChu().split("\\|");
                for (String p : parts) {
                    if (p.trim().startsWith("TRA:")) {
                        String traStr = p.trim().replace("TRA:", "").trim();
                        if (!traStr.equals("Không có") && !traStr.isEmpty()) {
                            String[] items = traStr.split(";");
                            for (String item : items) {
                                String[] vals = item.trim().split("_");
                                if (vals.length >= 3) dsSP.add(new Object[]{vals[0], vals[1], "Hộp", vals[2]});
                                else if (vals.length == 2) dsSP.add(new Object[]{vals[0], vals[1], "Hộp", "0"});
                            }
                        }
                    }
                }
            }
        } catch (Exception e){}

        long tongTienThucTe = 0;

        if (!dsSP.isEmpty()) {
            for (Object[] sp : dsSP) {
                String ten = sp[0] != null ? sp[0].toString() : "";
                
                int sl = 0;
                try { sl = Integer.parseInt(sp[1].toString()); } catch(Exception e) {}
                
                String dvt = sp[2] != null ? sp[2].toString() : "";
                
                double gia = 0;
                if(sp[3] != null) {
                    try { gia = Double.parseDouble(sp[3].toString()); } catch(Exception e){}
                }

                long thanhTien = (long) (sl * gia);
                tongTienThucTe += thanhTien;

                model.addRow(new Object[]{
                    ten + (dvt.isEmpty() ? "" : " (" + dvt + ")"), 
                    sl, 
                    String.format("%,.0fđ", gia).replace(',', '.'),
                    String.format("%,dđ", thanhTien).replace(',', '.')
                });
            }
        }

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        
        DefaultTableCellRenderer right = new DefaultTableCellRenderer(); 
        right.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(right);
        table.getColumnModel().getColumn(3).setCellRenderer(right);

        JScrollPane spTable = new JScrollPane(table);
        spTable.setPreferredSize(new Dimension(0, (table.getRowCount() * 30) + 25));
        spTable.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderGray));

        JPanel pnlSummary = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlSummary.setBackground(Color.WHITE);
        pnlSummary.add(createTotalRow("Tổng tiền SP trả:", String.format("%,dđ", tongTienThucTe).replace(',', '.'), Color.GRAY, 12));
        
        String labelHoan = loaiPhieu.equalsIgnoreCase("Trả hàng") ? "Số tiền hoàn lại:" : "Chênh lệch / Bù trừ:";
        String valHoan = loaiPhieu.equalsIgnoreCase("Trả hàng") ? tienHoanThucTe : chenhLechThucTe;
        pnlSummary.add(createTotalRow(labelHoan, valHoan, primaryRed, 16));

        pnl.add(spTable, BorderLayout.CENTER);
        pnl.add(pnlSummary, BorderLayout.SOUTH);
        return pnl;
    }

    private JPanel createTotalRow(String label, String value, Color color, int fontSize) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(label); lbl.setForeground(color);
        JLabel val = new JLabel(value); val.setForeground(color);
        val.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        p.add(lbl, BorderLayout.WEST); p.add(val, BorderLayout.EAST);
        return p;
    }

    private JPanel createSignaturePanel() {
        JPanel pnl = new JPanel(new GridLayout(1, 2));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(10, 0, 10, 0)); 

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