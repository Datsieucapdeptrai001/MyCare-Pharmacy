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
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import java.awt.image.BufferedImage;
public class ChiTietPhieuDoiTra extends JDialog {
    
    private Color primaryBlue = Color.decode("#1967D2"); 
    private Color primaryRed = Color.decode("#DC2626");  
    private Color textDark = Color.decode("#212B36");
    private Color textGray = Color.decode("#6B7280");
    private Color borderGray = Color.decode("#DFE3E8");
    
    private String maPhieu, loaiPhieu, trangThai, ngayTao, hoaDonGoc, khachHang, lyDo, nhanVien;
    private String tienHoanThucTe, chenhLechThucTe;
    
    private BUS_TraHang busTraHang = new BUS_TraHang(); 

    public ChiTietPhieuDoiTra(Frame parent, String maPhieu) {
        super(parent, "Chi tiết phiếu đổi/trả", true);
        this.maPhieu = maPhieu;

        // ── Tải dữ liệu hoàn toàn từ BUS ──────────────────────────────────────
        try {
            BUS.BUS_HoaDon busHD = new BUS.BUS_HoaDon();
            Entity.HoaDon hd = busHD.layHoaDonTheoMa(maPhieu);

            if (hd != null) {
                // Loại phiếu
                String loaiHDStr = hd.getLoaiHD() != null ? hd.getLoaiHD().toString() : "";
                this.loaiPhieu = "TRA_HANG".equals(loaiHDStr) ? "Trả hàng" : "Đổi hàng";

                // Trạng thái — mặc định "Hoàn thành"; có thể mở rộng theo hd.getTrangThai() nếu entity hỗ trợ
                this.trangThai = "Hoàn thành";

                // Ngày tạo
                this.ngayTao = hd.getNgayLapHD() != null
                        ? hd.getNgayLapHD().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
                        : "—";

                // Khách hàng
                this.khachHang = hd.getKhachHangId() != null
                        ? hd.getKhachHangId().getHoVaTen() : "Khách lẻ";

                // Nhân viên — ưu tiên từ HoaDon entity, fallback về Session
                if (hd.getNhanVienId() != null && hd.getNhanVienId().getHoVaTen() != null) {
                    this.nhanVien = hd.getNhanVienId().getHoVaTen();
                } else {
                    String tenNVSession = Utils.UserSession.getInstance().getTenHienThi();
                    this.nhanVien = (tenNVSession != null && !tenNVSession.isEmpty())
                            ? tenNVSession : "Nhân viên hệ thống";
                }

                // Lấy hoaDonGoc từ entity (field hoaDonGocId), không parse ghiChu
                Entity.HoaDon hdGoc = hd.getHoaDonGocId();
                this.hoaDonGoc = (hdGoc != null && hdGoc.getId() != null) ? hdGoc.getId() : "";

                // Lấy lyDo từ parts[1] của ghiChu (format: "trangThai|lyDo|tienHoan|chenhLech|TRA:...|DOI:...")
                String ghiChu = hd.getGhiChu() != null ? hd.getGhiChu() : "";
                String[] parts = ghiChu.split("\\|");
                this.lyDo = parts.length > 1 ? parts[1].trim()
                        : (!ghiChu.isEmpty() ? ghiChu : "Không có ghi chú");

                // tienHoanThucTe và chenhLechThucTe:
                // createProductListPanel() sẽ tự tính từ busTraHang → truyền chuỗi rỗng là an toàn.
                // (Nếu rỗng, summary panel sẽ hiển thị giá trị đã tính từ danh sách sản phẩm.)
                this.tienHoanThucTe  = "";
                this.chenhLechThucTe = "";

            } else {
                // HoaDon không tìm thấy — set giá trị mặc định an toàn
                this.loaiPhieu       = "Đổi hàng";
                this.trangThai       = "—";
                this.ngayTao         = "—";
                this.khachHang       = "Khách lẻ";
                this.nhanVien        = "Nhân viên hệ thống";
                this.hoaDonGoc       = "";
                this.lyDo            = "Không tìm thấy hóa đơn";
                this.tienHoanThucTe  = "";
                this.chenhLechThucTe = "";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            this.loaiPhieu       = "Đổi hàng";
            this.trangThai       = "—";
            this.ngayTao         = "—";
            this.khachHang       = "Khách lẻ";
            this.nhanVien        = "Nhân viên hệ thống";
            this.hoaDonGoc       = "";
            this.lyDo            = "Lỗi tải dữ liệu";
            this.tienHoanThucTe  = "";
            this.chenhLechThucTe = "";
        }

        // Cập nhật tiêu đề dialog sau khi đã biết loại phiếu
        setTitle("Chi tiết phiếu " + loaiPhieu);
        initUI();
    }

    /**
     * Constructor đầy đủ tham số — dùng khi gọi từ ManHinhBanHang (đã có dữ liệu sẵn).
     */
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
        if (nhanVienTruyenVao != null && !nhanVienTruyenVao.trim().isEmpty()) {
            this.nhanVien = nhanVienTruyenVao;
        } else {
            this.nhanVien = "Nhân viên hệ thống";
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
        pnlBody.add(createFooterTextPanel(maPhieu));

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
        
        // GỌI HÀM VÀ THÊM PHƯƠNG THỨC THANH TOÁN VÀO CỘT BÊN PHẢI (DƯỚI KHÁCH HÀNG)
        String phuongThuc = fetchPhuongThucThanhToan(maPhieu);
        pnlGrid.add(createLabelPair("Thanh toán: ", phuongThuc));
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

        JLabel lblTitle = new JLabel(loaiPhieu.equalsIgnoreCase("Trả hàng") ? "SẢN PHẨM TRẢ LẠI:" : "CHI TIẾT SẢN PHẨM ĐỔI / TRẢ:");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnl.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"Sản phẩm", "ĐVT", "SL", "Đơn giá", "Thành tiền"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { 
            @Override public boolean isCellEditable(int r, int c) { return false; } 
        };

        long tongTienTra = 0;
        long tongTienDoi = 0;

        List<Object[]> dsTra = busTraHang.layChiTietPhieu(maPhieu); 
        List<Object[]> dsDoi = null;
        if (loaiPhieu.equalsIgnoreCase("Đổi hàng")) {
            dsDoi = busTraHang.layDanhSachSanPhamDoi(maPhieu); 
        }

        DAO.DAO_HoaDon daoHD = new DAO.DAO_HoaDon();

        // ==========================================================
        // BƯỚC 1: TÍNH PHẦN TRĂM HOÀN TIỀN DỰA VÀO LÝ DO VÀ THỜI GIAN
        // ==========================================================
        double phanTramHoan = 1.0;
        if (lyDo.contains("Từ chối")) {
            phanTramHoan = 0.0;
        } else if (lyDo.contains("Khách hàng đổi ý") || lyDo.contains("Hỗ trợ 100%")) {
            long soGioDaMua = 0;
            try {
                BUS.BUS_HoaDon busHD = new BUS.BUS_HoaDon();
                Entity.HoaDon hdG = busHD.getHoaDonTheoMa(hoaDonGoc);
                Entity.HoaDon hdDT = busHD.getHoaDonTheoMa(maPhieu);
                if (hdG != null && hdDT != null) {
                    soGioDaMua = java.time.Duration.between(hdG.getNgayLapHD(), hdDT.getNgayLapHD()).toHours();
                }
            } catch(Exception e){}
            
            if (soGioDaMua <= 24) phanTramHoan = 1.0;
            else if (soGioDaMua > 24 && soGioDaMua <= 72) phanTramHoan = 0.8;
            else phanTramHoan = 0.0;
        } else if (lyDo.contains("Hỗ trợ 80%")) {
            phanTramHoan = 0.8;
        }

        // ==========================================
        // 1. ĐỔ DỮ LIỆU SẢN PHẨM KHÁCH TRẢ LẠI
        // ==========================================
        if (dsTra != null) {
            for (Object[] sp : dsTra) {
                String ten = sp[0] != null ? sp[0].toString() : "Sản phẩm";
                
                int sl = 1; 
                try { 
                    if (sp[1] != null) sl = Integer.parseInt(sp[1].toString().replaceAll("[^0-9]", "")); 
                } catch(Exception e){}
                
                // [2]=dvt, [3]=donGia (sau khi BUS_TraHang đã fix thứ tự đúng)
                String dvt = sp[2] != null ? sp[2].toString() : "Hộp";
                long gia = 0;
                
                // LỚP BẢO VỆ 1: Lọc sạch chuỗi, chỉ lấy số
                try { 
                    if (sp[3] != null) {
                        String giaStr = sp[3].toString().replaceAll(",00$|\\.00$|,0$|\\.0$", "");
                        giaStr = giaStr.replaceAll("[^0-9]", "");
                        if (!giaStr.isEmpty()) gia = Long.parseLong(giaStr);
                    }
                } catch(Exception e){}
                
                // LỚP BẢO VỆ 2: Truy vấn thẳng vào Hóa Đơn Gốc nếu giá <= 1
                if (gia <= 1) {
                    try {
                        Object[] info = daoHD.layThongTinGiaTuHDGoc(hoaDonGoc, ten);
                        if (info != null) {
                            if (info[0] != null) dvt = info[0].toString();
                            if (info[1] != null) {
                                String infoGia = info[1].toString().replaceAll(",00$|\\.00$|,0$|\\.0$", "").replaceAll("[^0-9]", "");
                                if (!infoGia.isEmpty()) gia = Long.parseLong(infoGia);
                            }
                        }
                    } catch(Exception ex){}
                }

                long giaGomVAT = gia;

                // NHÂN PHẦN TRĂM HOÀN VÀO THÀNH TIỀN ĐỂ ÉP VỀ 0 HOẶC TRỪ %
                long thanhTien = (long) (giaGomVAT * sl * phanTramHoan);
                tongTienTra += thanhTien;
                
                model.addRow(new Object[]{
                    "[TRẢ] " + ten, dvt, sl, 
                    String.format("%,dđ", giaGomVAT).replace(',', '.'), // <-- Hiển thị giá đã có VAT
                    String.format("%,dđ", thanhTien).replace(',', '.')
                });
            }
        }

        // ==========================================
        // 2. ĐỔ DỮ LIỆU SẢN PHẨM KHÁCH ĐỔI LẤY MỚI
        // ==========================================
     // ==========================================
        // 2. ĐỔ DỮ LIỆU SẢN PHẨM KHÁCH ĐỔI LẤY MỚI
        // ==========================================
        if (dsDoi != null) {
            for (Object[] sp : dsDoi) {
                String ten = sp[0] != null ? sp[0].toString() : "Sản phẩm";
                
                int sl = 1; 
                try { 
                    if (sp[1] != null) sl = Integer.parseInt(sp[1].toString().replaceAll("[^0-9]", "")); 
                } catch(Exception e){}
                
                String dvt = sp[2] != null ? sp[2].toString() : "Hộp";
                long gia = 0;
                
                try { 
                    if (sp[3] != null) {
                        String giaStr = sp[3].toString().replaceAll("[^0-9]", "");
                        if (!giaStr.isEmpty()) gia = Long.parseLong(giaStr);
                    }
                } catch(Exception e){}

                // --- SỬA Ở ĐÂY: LẤY VAT CỦA SẢN PHẨM ĐỔI MỚI ---
                double thueVat = new DAO.DAO_SanPham().layThueVATTheoTenSP(ten);
                if (thueVat > 0 && thueVat < 1) thueVat = thueVat * 100;
                
                // Cộng VAT vào đơn giá
                long giaGomVAT = Math.round(gia * (1.0 + thueVat / 100.0));
                
                // Tính thành tiền = Đơn giá đã có VAT * Số lượng
                long thanhTien = giaGomVAT * sl;
                tongTienDoi += thanhTien;
                
                model.addRow(new Object[]{
                    "[ĐỔI LẤY] " + ten, dvt, sl, 
                    String.format("%,dđ", giaGomVAT).replace(',', '.'), 
                    String.format("%,dđ", thanhTien).replace(',', '.')
                });
            }
        }
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 

        table.getColumnModel().getColumn(0).setPreferredWidth(280); 
        table.getColumnModel().getColumn(1).setPreferredWidth(60);  
        table.getColumnModel().getColumn(2).setPreferredWidth(50);  
        table.getColumnModel().getColumn(3).setPreferredWidth(100); 
        table.getColumnModel().getColumn(4).setPreferredWidth(110); 

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer(); 
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        JScrollPane spTable = new JScrollPane(table);
        spTable.setPreferredSize(new Dimension(0, (table.getRowCount() * 30) + 25));
        spTable.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderGray));
        spTable.getViewport().setBackground(Color.WHITE);

        // ==========================================
        // 3. KHU VỰC SUMMARY TỔNG TIỀN VÀ CHÊNH LỆCH
        // ==========================================
        JPanel pnlSummary = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlSummary.setBackground(Color.WHITE);
        
        if (trangThai.equalsIgnoreCase("Từ chối")) {
            // NẾU TỪ CHỐI -> ÉP HIỂN THỊ TRẠNG THÁI HỦY BỎ GIAO DỊCH
            pnlSummary.add(createTotalRow("Giá trị SP Mới / Trả:", 
                String.format("%,dđ", tongTienDoi).replace(',', '.') + " / " + String.format("%,dđ", tongTienTra).replace(',', '.'), 
                Color.GRAY, 12));
            pnlSummary.add(createTotalRow("Trạng thái giao dịch:", "ĐÃ TỪ CHỐI", primaryRed, 18));
            
        } else {
            // GIAO DỊCH HỢP LỆ (CHỜ XỬ LÝ / HOÀN THÀNH)
            if (loaiPhieu.equalsIgnoreCase("Trả hàng")) {
                pnlSummary.add(createTotalRow("Tổng tiền SP trả:", String.format("%,dđ", tongTienTra).replace(',', '.'), Color.GRAY, 12));
                
                String soTienHoan = (tienHoanThucTe != null && !tienHoanThucTe.trim().isEmpty()) ? tienHoanThucTe : "0đ";
                pnlSummary.add(createTotalRow("Số tiền hoàn lại:", soTienHoan, primaryRed, 16));
            } else {
                pnlSummary.add(createTotalRow("Giá trị SP Mới / Trả:", 
                    String.format("%,dđ", tongTienDoi).replace(',', '.') + " / " + String.format("%,dđ", tongTienTra).replace(',', '.'), 
                    Color.GRAY, 12));
                    
                long chenhLech = tongTienDoi - tongTienTra;
                boolean isKhachBu = (chenhLech > 0); 
                
                String textChenhLech = isKhachBu ? "Khách bù thêm:" : "Hoàn lại khách:";
                String valueChenhLech = String.format("%,dđ", Math.abs(chenhLech)).replace(',', '.'); 
                Color colorChenhLech = isKhachBu ? Color.decode("#D97706") : Color.decode("#059669"); 

                pnlSummary.add(createTotalRow(textChenhLech, valueChenhLech, colorChenhLech, 18)); 
            }
        }

        pnl.add(spTable, BorderLayout.CENTER);
        pnl.add(pnlSummary, BorderLayout.SOUTH);
        return pnl;
    }
    
    private String fetchPhuongThucThanhToan(String maPhieu) {
        String phuongThuc = "Tiền mặt";
        try {
            BUS.BUS_HoaDon busHD = new BUS.BUS_HoaDon();
            Entity.HoaDon hd = busHD.getHoaDonTheoMa(maPhieu);
            if (hd != null && hd.getPhuongThucThanhToan() != null) {
                phuongThuc = hd.getPhuongThucThanhToan().toString().contains("CHUYEN_KHOAN") ? "Chuyển khoản" : "Tiền mặt";
            }
        } catch (Exception e) {}
        return phuongThuc;
    }
 // THAY THẾ HÀM createTotalRow() CŨ BẰNG HÀM NÀY:
    private JPanel createTotalRow(String label, String value, Color color, int fontSize) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        
        JLabel lbl = new JLabel(label); 
        lbl.setForeground(textDark); // FIX: Luôn để chữ màu đậm chuẩn, không bị đỏ chói
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        JLabel val = new JLabel(value); 
        val.setForeground(color); // Màu đỏ/xanh/vàng chỉ áp dụng cho Số tiền
        val.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        
        p.add(lbl, BorderLayout.WEST); 
        p.add(val, BorderLayout.EAST);
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
    private ImageIcon generateBarcode1D(String data, int width, int height) {
        try {
            Code128Writer barcodeWriter = new Code128Writer();
            BitMatrix bitMatrix = barcodeWriter.encode(data, BarcodeFormat.CODE_128, width, height);
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            img.createGraphics();
            Graphics2D g = (Graphics2D) img.getGraphics();
            
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
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

    private JPanel createFooterTextPanel(String maPhieu) {
        JPanel pnl = new JPanel(); 
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS)); 
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(15, 0, 20, 0));

        // 1. MÃ VẠCH (Canh giữa)
        ImageIcon barcodeIcon = generateBarcode1D(maPhieu, 220, 50); 
        if (barcodeIcon != null) {
            JLabel lblBarcode = new JLabel(barcodeIcon);
            lblBarcode.setAlignmentX(Component.CENTER_ALIGNMENT); 
            pnl.add(lblBarcode);
            
            JLabel lblBarcodeText = new JLabel("Mã Phiếu: " + maPhieu);
            lblBarcodeText.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblBarcodeText.setForeground(textDark);
            lblBarcodeText.setAlignmentX(Component.CENTER_ALIGNMENT); 
            pnl.add(lblBarcodeText);
        }
        
        pnl.add(Box.createRigidArea(new Dimension(0, 15))); 

        // 2. LỜI CẢM ƠN & GHI CHÚ ĐÃ ĐỔI TRẢ
        JLabel l1 = new JLabel("Cảm ơn quý khách đã tin dùng!"); 
        l1.setFont(new Font("Segoe UI", Font.PLAIN, 11)); 
        l1.setForeground(textGray);
        l1.setAlignmentX(Component.CENTER_ALIGNMENT); 
        
        // KIỂM TRA TRẠNG THÁI ĐỂ HIỂN THỊ CÂU CHỮ CHO ĐÚNG
        boolean isTuChoi = trangThai.equalsIgnoreCase("Từ chối");
        String footerText = isTuChoi ? "Yêu cầu đổi trả đã bị từ chối." : "Hóa đơn này đã được đổi trả.";
        
        JLabel l2 = new JLabel(footerText); 
        l2.setFont(new Font("Segoe UI", Font.ITALIC, 12)); 
        l2.setForeground(isTuChoi ? primaryRed : textDark); 
        l2.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        pnl.add(l1); 
        pnl.add(l2);

        return pnl;
    }
}