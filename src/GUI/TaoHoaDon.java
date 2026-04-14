package GUI;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import Enumeration.*;
import Utils.*;
import ConnectDB.ConnectDB;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import DAO.DAO_SanPham;

public class TaoHoaDon extends JDialog {
    // --- BIẾN QUẢN LÝ UI KHÁCH HÀNG ---
    private JPanel pnlInputFields, pnlLinkedCustomer;
    private JLabel lblLinkedAvatar, lblLinkedName, lblLinkedSub, lblLinkedPoints, lblLinkedMoney;
    private JLabel lblLinkStatus, lblBadgeLe;
    private boolean isCustomerLinked = false;
    private String linkedTenKH = "", linkedSdtKH = "";
    private JTextField txtSearch;

    // --- CÁC BIẾN LOGIC KHÁC ---
    private long tongHoaDon = 0;
    private long tamTinh = 0;
    private long vat = 0;
    private DefaultTableModel productModel;
    private JLabel lblSubtotalValue, lblVatValue, lblTotalPriceValue;
    
    private int editingModelRow = -1; 
    private String maHDDangSua = "";
    private java.util.List<Object[]> dsKhuyenMaiCache = new java.util.ArrayList<>();
    private JTextField txtVoucherInput;
    private JLabel lblTienThuaValue;
    private DefaultTableModel mainTableModel;
    private JTextField txtName, txtPhone;
    private String phuongThuc = "Tiền mặt";
    private boolean isTableUpdating = false;
    private Color darkBlue = Color.decode("#152A4B");
    private Color accentBlue = Color.decode("#1A73E8");
    private Color successGreen = Color.decode("#10B981");
    private Color orangeLogo = Color.decode("#F59E0B");
    private Color lightGray = Color.decode("#F8F9FA");
    private Color borderColor = Color.decode("#DFE3E8");
    private long tongTienMat = 0;
    private JLabel lblTotalValue;
    private int editingRow = -1;
    
    // --- BIẾN QUẢN LÝ KHUYẾN MÃI ---
    private long tienGiamGia = 0;
    private String maKhuyenMaiApDung = "";
    private JLabel lblDiscountValue;
    
    // --- TÍCH HỢP TÍNH NĂNG DÙNG ĐIỂM (SHOPEE STYLE) ---
    private int diemHienTaiKH = 0;
    private boolean isDungDiem = false;
    private long tienGiamTuDiem = 0;
    private JLabel lblDungDiemValue; 
    private JPanel pnlDungDiem; 
    private JLabel lblDungDiemText;
    private CustomToggleSwitch toggleDungDiem;
    private JPanel pnlVoucherTags;
    public TaoHoaDon(Frame parent, DefaultTableModel mainModel) {
        super(parent, "Tạo hóa đơn bán hàng mới", true);
        this.mainTableModel = mainModel;
        this.editingRow = -1; 
        initUI(parent);
    }

    public TaoHoaDon(Frame parent, DefaultTableModel mainModel, int modelRow, String maHD, String tenKH, String sdt) {
        super(parent, "Chỉnh sửa hóa đơn nháp", true);
        this.mainTableModel = mainModel;
        this.editingModelRow = modelRow; 
        this.maHDDangSua = maHD;
        
        initUI(parent);

        if (tenKH != null && !tenKH.equals("Khách lẻ")) {
            txtName.setText(tenKH);
            txtName.setForeground(Color.BLACK);
        }
        if (sdt != null) {
            txtPhone.setText(sdt);
        }
    }

    private void initUI(Frame parent) {
        setSize(900, 800);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(darkBlue);
        pnlHeader.setPreferredSize(new Dimension(0, 50));
        pnlHeader.setBorder(new EmptyBorder(0, 15, 0, 15));
        JLabel lblTitle = new JLabel(editingModelRow == -1 ? "Tạo hóa đơn bán hàng mới" : "Sửa hóa đơn: " + maHDDangSua);
        lblTitle.setIcon(new MenuIcon("CART"));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(10, 25, 10, 25));
        
        pnlBody.add(createCustomerPanel());
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));
        pnlBody.add(createSectionPanel("Thêm sản phẩm", "PACKAGE", createProductPanel()));
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));
        
        pnlBody.add(createSectionPanel("Mã khuyến mãi", "GIFT", createVoucherPanel()));
        pnlBody.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // GẮN GIAO DIỆN CÔNG TẮC DÙNG ĐIỂM NGAY DƯỚI MÃ KHUYẾN MÃI (ẨN ĐI MẶC ĐỊNH)
        pnlDungDiem = createDungDiemPanel();
        pnlDungDiem.setVisible(false);
        pnlBody.add(pnlDungDiem);
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));
        
        pnlBody.add(createSummaryPanel());

        JScrollPane scrollPane = new JScrollPane(pnlBody);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        JScrollBar customScrollBar = new JScrollBar() {
            @Override
            public void updateUI() {
                setUI(new ModernScrollBarUI()); 
            }
        };
        customScrollBar.setPreferredSize(new Dimension(10, 0));
        customScrollBar.setUnitIncrement(16);
        scrollPane.setVerticalScrollBar(customScrollBar);
        
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor),
            new EmptyBorder(10, 25, 10, 25)
        ));

        if (editingModelRow != -1) {
            JButton btnXoaDon = new JButton("Xóa đơn");
            btnXoaDon.setIcon(new MenuIcon("TRASH")); 
            btnXoaDon.setIconTextGap(8);
            styleButton(btnXoaDon, Color.decode("#EF4444")); 
            
            btnXoaDon.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa hóa đơn nháp này không?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    mainTableModel.removeRow(editingModelRow); 
                    dispose(); 
                }
            });
            pnlFooter.add(btnXoaDon, BorderLayout.WEST);
        }

        JPanel pnlRightFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlRightFooter.setOpaque(false);

        JButton btnLuuNhap = new JButton("Lưu nháp");
        styleButton(btnLuuNhap, orangeLogo);

        JButton btnThanhToan = new JButton("Thanh toán");
        styleButton(btnThanhToan, successGreen);

        // NÚT THANH TOÁN (XỬ LÝ ĐIỂM + KHUYẾN MÃI)
        btnThanhToan.addActionListener(e -> {
            String khach = isCustomerLinked ? linkedTenKH : txtName.getText();
            if(khach.equals("Tên khách (bỏ trống = Khách lẻ)") || khach.trim().isEmpty()) khach = "Khách lẻ";
            String sdt = isCustomerLinked ? linkedSdtKH : txtPhone.getText().replace("Số điện thoại (tuỳ chọn)", "");
            String tongTien = lblTotalPriceValue.getText();

            if (productModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng thêm ít nhất 1 sản phẩm vào hóa đơn!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                java.sql.Connection con = ConnectDB.getInstance().getConnection();

                String maHDMoi = (editingModelRow != -1) ? maHDDangSua.replace("-TEMP", "") : phatSinhMaHoaDon();
                Entity.HoaDon hd = new Entity.HoaDon();
                hd.setId(maHDMoi);
                hd.setLoaiHD(Enumeration.LoaiHoaDon.BAN_HANG);
                
                // Ghi chú nếu có dùng điểm hoặc KM
                String strDiem = isDungDiem ? " | Dùng điểm: -" + tienGiamTuDiem : "";
                String strKM = maKhuyenMaiApDung.isEmpty() ? "" : " | KM: " + maKhuyenMaiApDung;
                
                if (phuongThuc.equals("Tiền mặt")) {
                    hd.setGhiChu("CASH:" + tongTienMat + strKM + strDiem);
                } else {
                    hd.setGhiChu("BANK" + strKM + strDiem); 
                }
                hd.setNgayLapHD(java.time.LocalDateTime.now());

                Entity.NhanVien nv = new Entity.NhanVien();
                try { nv.setNhanVien("DS-0001"); } catch(Exception ex) { /* Ignore nếu sai tên hàm */ }
                hd.setNhanVienId(nv);

                if (isCustomerLinked && !sdt.isEmpty()) {
                    java.sql.PreparedStatement pstKH = con.prepareStatement("SELECT id FROM KhachHang WHERE sdt = ?");
                    pstKH.setString(1, sdt);
                    java.sql.ResultSet rsKH = pstKH.executeQuery();
                    if (rsKH.next()) {
                        Entity.KhachHang kh = new Entity.KhachHang();
                        kh.setId(rsKH.getString("id"));
                        hd.setKhachHangId(kh);
                    }
                }

                Enumeration.PhuongThucThanhToan pt = phuongThuc.equals("Tiền mặt") 
                        ? Enumeration.PhuongThucThanhToan.TIEN_MAT 
                        : Enumeration.PhuongThucThanhToan.CHUYEN_KHOAN_NGAN_HANG;
                hd.setPhuongThucThanhToan(pt);

                java.util.List<Entity.ChiTietHoaDon> dsCTHD = new java.util.ArrayList<>();
                for (int i = 0; i < productModel.getRowCount(); i++) {
                    String tenSP = productModel.getValueAt(i, 0).toString();
                    String tenDVT = productModel.getValueAt(i, 1).toString();
                    int soLuong = Integer.parseInt(productModel.getValueAt(i, 2).toString());

                    String maSP = "";
                    String maDVT = "";
                    String sql = "SELECT sp.id AS MaSP, dv.id AS MaDVT FROM SanPham sp JOIN DonViDoLuong dv ON sp.id = dv.sanPhamId WHERE sp.ten = ? AND dv.ten = ?";
                    java.sql.PreparedStatement pstSP = con.prepareStatement(sql);
                    pstSP.setString(1, tenSP);
                    pstSP.setString(2, tenDVT);
                    java.sql.ResultSet rsSP = pstSP.executeQuery();
                    
                    if(rsSP.next()) {
                        maSP = rsSP.getString("MaSP");
                        maDVT = rsSP.getString("MaDVT");
                    }

                    if (maSP.isEmpty()) throw new Exception("Không tìm thấy mã sản phẩm trong CSDL cho: " + tenSP);

                    Entity.ChiTietHoaDon ct = new Entity.ChiTietHoaDon();
                    ct.setHoaDonId(hd);
                    
                    Entity.SanPham sp = new Entity.SanPham();
                    sp.setId(maSP);
                    ct.setSanPhamId(sp);

                    Entity.DonViDoLuong dv = new Entity.DonViDoLuong();
                    dv.setId(maDVT);
                    ct.setDonViDoLuongId(dv);

                    ct.setSoLuong(soLuong);
                    dsCTHD.add(ct);
                }

                BUS.BUS_HoaDon busHD = new BUS.BUS_HoaDon();
                boolean success = busHD.thanhToan(hd, dsCTHD);

                if (success) {
                	if (isCustomerLinked && !sdt.isEmpty()) {
                        try {
                            // TÍNH TOÁN ĐIỂM: 10k = 1 điểm. (1 điểm = 100đ quy đổi)
                            int diemDaDung = isDungDiem ? (int)(tienGiamTuDiem / 100) : 0;
                            int diemCongMoi = (int) (tongHoaDon / 10000); 
                            
                            // Có thể âm (nếu khách xài nhiều hơn khách được nhận lần này), vẫn đúng!
                            int diemChenhLech = diemCongMoi - diemDaDung; 
                        
                            String sqlUpdateDiem = "UPDATE KhachHang SET diemTichLuy = ISNULL(diemTichLuy, 0) + ? WHERE sdt = ?";
                            java.sql.PreparedStatement pstUpdate = con.prepareStatement(sqlUpdateDiem);
                            pstUpdate.setInt(1, diemChenhLech);
                            pstUpdate.setString(2, sdt);
                            pstUpdate.executeUpdate();

                            // REAL-TIME DASHBOARD UPDATE
                            Window parentWindow = SwingUtilities.getWindowAncestor(this);
                            if (parentWindow instanceof MainDashboard) {
                                DefaultTableModel khModel = ((MainDashboard) parentWindow).getModelKhachHang();
                                if (khModel != null) {
                                    for (int j = 0; j < khModel.getRowCount(); j++) {
                                        String phoneInTable = khModel.getValueAt(j, 2).toString();
                                        if (sdt.equals(phoneInTable)) {
                                            // Cộng/Trừ Điểm
                                            int dHienTai = 0;
                                            try {
                                                String currentDiemStr = khModel.getValueAt(j, 5).toString().replace(".", "").replace(",", "");
                                                dHienTai = Integer.parseInt(currentDiemStr);
                                            } catch (Exception ex) {}
                                            khModel.setValueAt(String.valueOf(dHienTai + diemChenhLech), j, 5);

                                            // Cộng 1 đơn
                                            int donHangHienTai = 0;
                                            try {
                                                donHangHienTai = Integer.parseInt(khModel.getValueAt(j, 3).toString());
                                            } catch(Exception ex) {}
                                            khModel.setValueAt(String.valueOf(donHangHienTai + 1), j, 3);

                                            // Cộng tổng chi tiêu
                                            long chiTieuHienTai = 0;
                                            try {
                                                String currentChiTieu = khModel.getValueAt(j, 4).toString().replaceAll("[^0-9]", "");
                                                chiTieuHienTai = Long.parseLong(currentChiTieu);
                                            } catch(Exception ex) {}
                                            java.text.DecimalFormat df = new java.text.DecimalFormat("#,###đ");
                                            khModel.setValueAt(df.format(chiTieuHienTai + tongHoaDon), j, 4);

                                            break; 
                                        }
                                    }
                                }
                            }
                            
                            String msgThongBao = "Thanh toán thành công!\n";
                            if(diemDaDung > 0) msgThongBao += "➖ Đã sử dụng: " + diemDaDung + " điểm\n";
                            if(diemCongMoi > 0) msgThongBao += "➕ Tích lũy thêm: " + diemCongMoi + " điểm\n";
                            
                            JOptionPane.showMessageDialog(this, msgThongBao, "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(this, "Thanh toán thành công nhưng có lỗi khi cập nhật điểm!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Thanh toán thành công!\nHóa đơn và Tồn kho đã được cập nhật.", "Hoàn tất", JOptionPane.INFORMATION_MESSAGE);
                    }

                    // Cập nhật lên bảng MainTableModel
                    String ngayStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    if (editingModelRow != -1) {
                        mainTableModel.setValueAt(maHDMoi, editingModelRow, 0); 
                        mainTableModel.setValueAt(khach, editingModelRow, 2);         
                        mainTableModel.setValueAt(sdt, editingModelRow, 3);           
                        mainTableModel.setValueAt(phuongThuc, editingModelRow, 4);   
                        mainTableModel.setValueAt(tongTien, editingModelRow, 5);     
                        mainTableModel.setValueAt("Hoàn thành", editingModelRow, 6); 
                    } else {
                        mainTableModel.addRow(new Object[]{maHDMoi, ngayStr, khach, sdt, phuongThuc, tongTien, "Hoàn thành", "", "TPCN"});
                    }
                    dispose(); 
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi Database: Kho không đủ hàng hoặc dữ liệu sai!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } catch(Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi Nghiêm Trọng", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnLuuNhap.addActionListener(e -> luuNhapHoaDon());

        pnlRightFooter.add(btnLuuNhap);
        pnlRightFooter.add(btnThanhToan);
        pnlFooter.add(pnlRightFooter, BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    // --- CÔNG TẮC TOGGLE SHOPEE STYLE ---
    class CustomToggleSwitch extends JPanel {
        private boolean isOn = false;
        private Runnable onToggle;
        
        public CustomToggleSwitch(Runnable onToggle) {
            this.onToggle = onToggle;
            setPreferredSize(new Dimension(50, 26));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setOpaque(false);
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    isOn = !isOn;
                    repaint();
                    if (onToggle != null) onToggle.run();
                }
            });
        }
        public boolean isOn() { return isOn; }
        public void setOn(boolean on) { this.isOn = on; repaint(); }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Bật thì màu Cam Shopee, tắt màu xám nhạt
            g2.setColor(isOn ? Color.decode("#EE4D2D") : Color.decode("#E5E7EB")); 
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            
            // Vòng tròn trắng
            g2.setColor(Color.WHITE);
            int padding = 2;
            int r = getHeight() - (padding * 2); 
            int x = isOn ? getWidth() - r - padding : padding;
            g2.fillOval(x, padding, r, r);
            
            g2.dispose();
        }
    }

    // --- KHỐI GIAO DIỆN DÙNG ĐIỂM (THEO ẢNH SHOPEE) ---
    private JPanel createDungDiemPanel() {
        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(12, 15, 12, 15)
        ));

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlLeft.setOpaque(false);
        
        // Icon Chữ S màu cam (Shopee style)
        JLabel lblIcon = new JLabel("P", SwingConstants.CENTER); // P = Point
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblIcon.setForeground(Color.decode("#EE4D2D"));
        lblIcon.setBorder(BorderFactory.createLineBorder(Color.decode("#EE4D2D"), 2, true));
        lblIcon.setPreferredSize(new Dimension(26, 26));

        lblDungDiemText = new JLabel("Dùng 0 điểm (-0đ)");
        lblDungDiemText.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblDungDiemText.setForeground(Color.decode("#212B36"));

        pnlLeft.add(lblIcon);
        pnlLeft.add(lblDungDiemText);

        toggleDungDiem = new CustomToggleSwitch(() -> {
            isDungDiem = toggleDungDiem.isOn();
            recalculateTotals();
        });

        pnl.add(pnlLeft, BorderLayout.WEST);
        pnl.add(toggleDungDiem, BorderLayout.EAST);
        return pnl;
    }

    private void recalculateTotals() {
        tamTinh = 0;
        for (int i = 0; i < productModel.getRowCount(); i++) {
            String valueStr = productModel.getValueAt(i, 5).toString().replaceAll("[^0-9]", "");
            tamTinh += Long.parseLong(valueStr);
        }
        vat = (long) (tamTinh * 0.05); 
        
        long tongTienDK = tamTinh + vat; 

        // ==============================================================
        // THUẬT TOÁN AUTO-APPLY: QUÉT CACHE TÌM MÃ GIẢM SÂU NHẤT
        // ==============================================================
        long maxGiamGiaTựĐộng = 0;
        String bestMaKM = "";

        for (Object[] km : dsKhuyenMaiCache) {
            String maKM = km[0].toString();
            String loaiKM = km[1] != null ? km[1].toString() : "";
            double giaTri = (double) km[2];
            double donToiThieu = (double) km[3];

            // Nếu đơn hàng lớn hơn hoặc bằng điều kiện đơn tối thiểu
            if (tongTienDK >= donToiThieu) {
                long giamTam = 0;
                if (loaiKM.trim().toUpperCase().contains("PHAN_TRAM")) {
                    giamTam = (long) (tongTienDK * (giaTri / 100.0));
                } else {
                    giamTam = (long) giaTri;
                }

                // Luôn chọn mã mang lại số tiền giảm nhiều nhất cho khách
                if (giamTam > maxGiamGiaTựĐộng) {
                    maxGiamGiaTựĐộng = giamTam;
                    bestMaKM = maKM;
                }
            }
        }

        // Nếu mã tự động tìm được có tiền giảm lớn hơn hoặc bằng mã khách tự nhập tay, ưu tiên mã tự động
        if (maxGiamGiaTựĐộng >= tienGiamGia || maKhuyenMaiApDung.isEmpty()) {
            tienGiamGia = maxGiamGiaTựĐộng;
            maKhuyenMaiApDung = bestMaKM;
            
            // Tự động đẩy tên mã giảm giá lên giao diện để người dùng biết hệ thống đang dùng mã gì
            if (txtVoucherInput != null) {
                if (!bestMaKM.isEmpty()) {
                    txtVoucherInput.setText(bestMaKM);
                    txtVoucherInput.setForeground(Color.decode("#10B981")); // Đổi màu xanh lá báo thành công
                } else {
                    txtVoucherInput.setText("NHẬP MÃ HOẶC CHỌN BÊN DƯỚI...");
                    txtVoucherInput.setForeground(Color.GRAY);
                }
            }
        }
        // ==============================================================

        if (tienGiamGia > tongTienDK) {
            tienGiamGia = tongTienDK; 
        }

        long tienSauKM = tongTienDK - tienGiamGia; 
        
        // --- XỬ LÝ DÙNG ĐIỂM ---
        if (isDungDiem) {
            long maxDiemTien = diemHienTaiKH * 100L; // 1 điểm = 100đ
            tienGiamTuDiem = Math.min(maxDiemTien, tienSauKM); 
        } else {
            tienGiamTuDiem = 0;
        }

        tongHoaDon = tienSauKM - tienGiamTuDiem; 

        if (lblSubtotalValue != null) lblSubtotalValue.setText(String.format("%,d", tamTinh).replace(',', '.') + "đ");
        if (lblVatValue != null) lblVatValue.setText("+" + String.format("%,d", vat).replace(',', '.') + "đ");
        if (lblDiscountValue != null) lblDiscountValue.setText("-" + String.format("%,d", tienGiamGia).replace(',', '.') + "đ");
        if (lblDungDiemValue != null) lblDungDiemValue.setText("-" + String.format("%,d", tienGiamTuDiem).replace(',', '.') + "đ");
        if (lblTotalPriceValue != null) lblTotalPriceValue.setText(String.format("%,d", tongHoaDon).replace(',', '.') + "đ");
        
        capNhatTongTien(); 
        updateVoucherTagsUI();
    }
    
    private JPanel createVoucherPanel() {
        JPanel pnlWrapper = new JPanel(new BorderLayout(10, 10));
        pnlWrapper.setBackground(Color.WHITE);
        pnlWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel pnlInput = new JPanel(new BorderLayout(10, 0));
        pnlInput.setOpaque(false);
        
        txtVoucherInput = createStyledTextField("NHẬP MÃ HOẶC CHỌN BÊN DƯỚI...");
        JButton btnApply = new JButton("Áp dụng");
        styleButton(btnApply, Color.decode("#1967D2")); 
        btnApply.setPreferredSize(new Dimension(100, 36));

        pnlInput.add(txtVoucherInput, BorderLayout.CENTER);
        pnlInput.add(btnApply, BorderLayout.EAST);

        // --- FIX LỖI Ở ĐÂY: KHỞI TẠO PANEL TRƯỚC KHI GỌI setOpaque ---
        pnlVoucherTags = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        pnlVoucherTags.setOpaque(false);
        
        try {
            java.sql.Connection con = ConnectDB.getInstance().getConnection();
            String sqlLoad = "SELECT k.id, h.loaiHinhThuc, h.giaTri AS mucGiam, ISNULL(d.giaTri, 0) AS donToiThieu " +
                             "FROM KhuyenMai k " +
                             "JOIN HinhThucKhuyenMai h ON k.id = h.khuyenMaiId " +
                             "LEFT JOIN DieuKienKhuyenMai d ON k.id = d.khuyenMaiId " +
                             "WHERE k.ngayBatDau <= GETDATE() AND k.ngayKetThuc >= GETDATE()";
                             
            java.sql.Statement st = con.createStatement();
            java.sql.ResultSet rs = st.executeQuery(sqlLoad);
            
            boolean hasVoucher = false;
            dsKhuyenMaiCache.clear(); // Xóa cache cũ trước khi load

            while(rs.next()) {
                hasVoucher = true;
                String maKM = rs.getString("id");
                String loaiKM = rs.getString("loaiHinhThuc"); 
                double giaTri = rs.getDouble("mucGiam");
                double donToiThieu = rs.getDouble("donToiThieu");
                
                // LƯU VÀO CACHE ĐỂ TỰ ĐỘNG XÉT ĐIỀU KIỆN
                dsKhuyenMaiCache.add(new Object[]{maKM, loaiKM, giaTri, donToiThieu});

                String labelStr = "Mã " + maKM + ": ";
                if(loaiKM != null && loaiKM.trim().toUpperCase().contains("PHAN_TRAM")) {
                    labelStr += "Giảm " + (int)giaTri + "%";
                } else {
                    labelStr += "Giảm " + String.format("%,d", (long)giaTri).replace(',', '.') + "đ";
                }
                
                if(donToiThieu > 0) {
                    labelStr += " (Đơn ≥ " + String.format("%,d", (long)donToiThieu).replace(',', '.') + "đ)";
                }

                // Chèn Tag vào Panel quản lý
                pnlVoucherTags.add(createVoucherTag(labelStr, maKM, txtVoucherInput, btnApply));
            }
            
            if(!hasVoucher) {
                JLabel lblEmpty = new JLabel("<html><i>(Hiện chưa có chương trình khuyến mãi nào)</i></html>");
                lblEmpty.setForeground(Color.GRAY);
                pnlVoucherTags.add(lblEmpty);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        pnlWrapper.add(pnlInput, BorderLayout.NORTH);
        pnlWrapper.add(pnlVoucherTags, BorderLayout.CENTER);

        // Xử lý nút áp dụng thủ công
        btnApply.addActionListener(e -> {
            String code = txtVoucherInput.getText().trim();
            if(code.isEmpty() || code.equals("NHẬP MÃ HOẶC CHỌN BÊN DƯỚI...")) return;

            long tongTienDK = tamTinh + vat; 
            
            try {
                java.sql.Connection con = ConnectDB.getInstance().getConnection();
                
                String sqlCheck = "SELECT h.loaiHinhThuc, h.giaTri AS mucGiam, ISNULL(d.giaTri, 0) AS donToiThieu " +
                                  "FROM KhuyenMai k " +
                                  "JOIN HinhThucKhuyenMai h ON k.id = h.khuyenMaiId " +
                                  "LEFT JOIN DieuKienKhuyenMai d ON k.id = d.khuyenMaiId " +
                                  "WHERE k.id = ? AND k.ngayBatDau <= GETDATE() AND k.ngayKetThuc >= GETDATE()";
                                  
                java.sql.PreparedStatement pst = con.prepareStatement(sqlCheck);
                pst.setString(1, code);
                java.sql.ResultSet rsCheck = pst.executeQuery();

                if(rsCheck.next()) {
                    String loaiKM = rsCheck.getString("loaiHinhThuc");
                    double giaTri = rsCheck.getDouble("mucGiam");
                    long donToiThieu = (long) rsCheck.getDouble("donToiThieu");

                    if (tongTienDK < donToiThieu) {
                        JOptionPane.showMessageDialog(this, 
                            "Chưa đạt giá trị đơn tối thiểu (" + String.format("%,d", donToiThieu).replace(',', '.') + "đ) để áp dụng mã này!", 
                            "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    if(loaiKM != null && loaiKM.trim().toUpperCase().contains("PHAN_TRAM")) {
                        tienGiamGia = (long) (tongTienDK * (giaTri / 100.0));
                    } else {
                        tienGiamGia = (long) giaTri; 
                    }

                    maKhuyenMaiApDung = code;
                } else {
                    tienGiamGia = 0;
                    maKhuyenMaiApDung = "";
                    JOptionPane.showMessageDialog(this, "Mã khuyến mãi không tồn tại hoặc đã hết hạn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
                
                recalculateTotals(); 

            } catch(Exception ex) {
                ex.printStackTrace();
            }
        });

        return pnlWrapper;
    }

    private JPanel createSectionPanel(String title, String iconType, JPanel content) {
        JPanel pnl = new JPanel(new BorderLayout(0, 10)); 
        pnl.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel(title);
        if (iconType != null) {
            lblTitle.setIcon(new MenuIcon(iconType)); 
            lblTitle.setIconTextGap(8); 
        }
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.decode("#212B36"));

        pnl.add(lblTitle, BorderLayout.NORTH);
        pnl.add(content, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createCustomerPanel() {
        JPanel pnlWrapper = new JPanel(new BorderLayout(0, 4)); 
        pnlWrapper.setBackground(Color.WHITE);
        pnlWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                new EmptyBorder(5, 15, 8, 15)
        ));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlTitle.setBackground(Color.WHITE);
        
        JLabel lblIcon = new JLabel(new MenuIcon("USER")); 
        lblIcon.setForeground(Color.decode("#6C757D"));
        
        JLabel lblTitleText = new JLabel("Tra cứu khách hàng");
        lblTitleText.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitleText.setForeground(Color.decode("#212B36"));
        
        JLabel lblSubTitle = new JLabel("(tuỳ chọn)");
        lblSubTitle.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblSubTitle.setForeground(Color.decode("#9CA3AF"));
        
        pnlTitle.add(lblIcon);
        pnlTitle.add(lblTitleText);
        pnlTitle.add(lblSubTitle);

        JPanel pnlBadgeWrap = new JPanel(new CardLayout());
        pnlBadgeWrap.setOpaque(false);
        
        lblBadgeLe = new JLabel("Khách lẻ", SwingConstants.CENTER);
        lblBadgeLe.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblBadgeLe.setForeground(Color.decode("#4B5563"));
        lblBadgeLe.setBackground(Color.decode("#F3F4F6"));
        lblBadgeLe.setOpaque(true);
        lblBadgeLe.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        lblLinkStatus = new JLabel("Đã liên kết", SwingConstants.CENTER);
        lblLinkStatus.setIcon(new MenuIcon("CORRECT")); 
        lblLinkStatus.setIconTextGap(4); 
        
        lblLinkStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLinkStatus.setForeground(Color.WHITE); 
        lblLinkStatus.setBackground(Color.decode("#10B981"));
        lblLinkStatus.setOpaque(true);
        lblLinkStatus.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#059669"), 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 10)
        ));
        
        pnlBadgeWrap.add(lblBadgeLe, "LE");
        pnlBadgeWrap.add(lblLinkStatus, "LINKED");
        
        pnlHeader.add(pnlTitle, BorderLayout.WEST);
        pnlHeader.add(pnlBadgeWrap, BorderLayout.EAST);

        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);

        JPanel pnlSearch = new JPanel(new BorderLayout(10, 0));
        pnlSearch.setBackground(Color.WHITE);
        
        txtSearch = createStyledTextField("Nhập SĐT hoặc mã KH để liên kết điểm thưởng...");
        JButton btnSearch = new JButton("Tra cứu");
        btnSearch.setIcon(new MenuIcon("SEARCH")); 
        btnSearch.setIconTextGap(6);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.setBackground(Color.decode("#1967D2"));
        btnSearch.setForeground(Color.WHITE); 
        btnSearch.setFocusPainted(false);
        btnSearch.setBorderPainted(false);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        pnlSearch.add(txtSearch, BorderLayout.CENTER);
        pnlSearch.add(btnSearch, BorderLayout.EAST);

        JPanel pnlWarn = new JPanel(new BorderLayout(10, 0));
        pnlWarn.setBackground(Color.WHITE);
        pnlWarn.setBorder(new EmptyBorder(8, 0, 0, 0));
        
        JLabel lblWarning = new JLabel("Không tìm thấy khách hàng. Bạn có muốn tạo mới?");
        lblWarning.setIcon(new MenuIcon("WARNING"));
        lblWarning.setIconTextGap(8);
        lblWarning.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblWarning.setForeground(Color.decode("#DC2626")); 
        lblWarning.setBackground(Color.decode("#FFFBEB")); 
        lblWarning.setOpaque(true);
        lblWarning.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#FCA5A5"), 1, true), 
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        
        JButton btnAddCustomer = new JButton("Thêm KH mới");
        btnAddCustomer.setIcon(new MenuIcon("USER_ADD"));
        btnAddCustomer.setIconTextGap(6);
        btnAddCustomer.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAddCustomer.setBackground(Color.decode("#E11D48")); 
        btnAddCustomer.setForeground(Color.WHITE);
        btnAddCustomer.setFocusPainted(false);
        btnAddCustomer.setBorderPainted(false);
        btnAddCustomer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        pnlWarn.add(lblWarning, BorderLayout.CENTER);
        pnlWarn.add(btnAddCustomer, BorderLayout.EAST);
        pnlWarn.setVisible(false);

        pnlInputFields = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlInputFields.setBackground(Color.WHITE);
        pnlInputFields.setBorder(new EmptyBorder(8, 0, 0, 0));
        txtName = createStyledTextField("Tên khách (bỏ trống = Khách lẻ)");
        txtPhone = createStyledTextField("Số điện thoại (tuỳ chọn)");
        pnlInputFields.add(txtName);
        pnlInputFields.add(txtPhone);

        pnlLinkedCustomer = new JPanel(new BorderLayout(15, 0));
        pnlLinkedCustomer.setBackground(Color.decode("#F0F9FF")); 
        pnlLinkedCustomer.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(8, 0, 0, 0),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#BAE6FD"), 1, true), 
                new EmptyBorder(10, 15, 10, 15)
            )
        ));
        
        lblLinkedAvatar = new JLabel("N", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#152A4B"));
                g2.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        lblLinkedAvatar.setForeground(Color.WHITE);
        lblLinkedAvatar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLinkedAvatar.setPreferredSize(new Dimension(45, 45));
        
        JPanel pnlNameInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlNameInfo.setOpaque(false);
        lblLinkedName = new JLabel("Nguyễn Văn An");
        lblLinkedName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblLinkedName.setForeground(Color.BLACK);
        lblLinkedSub = new JLabel("KH2024-0001 • 0910000000");
        lblLinkedSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLinkedSub.setForeground(Color.GRAY);
        pnlNameInfo.add(lblLinkedName);
        pnlNameInfo.add(lblLinkedSub);
        
        JPanel pnlPoints = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlPoints.setOpaque(false);
        lblLinkedPoints = new JLabel("200 điểm", SwingConstants.RIGHT);
        lblLinkedPoints.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblLinkedPoints.setForeground(Color.decode("#9333EA")); 
        lblLinkedMoney = new JLabel("≈ 200.000đ", SwingConstants.RIGHT);
        lblLinkedMoney.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLinkedMoney.setForeground(Color.GRAY);
        pnlPoints.add(lblLinkedPoints);
        pnlPoints.add(lblLinkedMoney);
        
        JButton btnUnlink = new JButton(); 
        btnUnlink.setIcon(new MenuIcon("CLOSE")); 
        btnUnlink.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnUnlink.setForeground(Color.GRAY); 
        btnUnlink.setContentAreaFilled(false);
        btnUnlink.setBorderPainted(false);
        btnUnlink.setFocusPainted(false);
        btnUnlink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // HỦY LIÊN KẾT KHÁCH HÀNG -> TẮT DÙNG ĐIỂM
        btnUnlink.addActionListener(e -> {
            isCustomerLinked = false;
            linkedTenKH = ""; linkedSdtKH = "";
            diemHienTaiKH = 0;
            isDungDiem = false;
            toggleDungDiem.setOn(false);
            pnlDungDiem.setVisible(false);
            recalculateTotals();

            pnlLinkedCustomer.setVisible(false);
            pnlInputFields.setVisible(true);
            CardLayout cl = (CardLayout)(pnlBadgeWrap.getLayout());
            cl.show(pnlBadgeWrap, "LE");
            pnlBody.revalidate(); pnlBody.repaint();
        });
        
        JPanel pnlRightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlRightInfo.setOpaque(false);
        pnlRightInfo.add(pnlPoints);
        pnlRightInfo.add(btnUnlink);
        
        pnlLinkedCustomer.add(lblLinkedAvatar, BorderLayout.WEST);
        pnlLinkedCustomer.add(pnlNameInfo, BorderLayout.CENTER);
        pnlLinkedCustomer.add(pnlRightInfo, BorderLayout.EAST);
        pnlLinkedCustomer.setVisible(false); 

        pnlBody.add(pnlSearch);
        pnlBody.add(pnlWarn);
        pnlBody.add(pnlInputFields);
        pnlBody.add(pnlLinkedCustomer);

        // TRA CỨU KHÁCH HÀNG -> HIỂN THỊ CÔNG TẮC DÙNG ĐIỂM NẾU CÓ ĐIỂM
        btnSearch.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();
            if (keyword.isEmpty() || keyword.equals("Nhập SĐT hoặc mã KH để liên kết điểm thưởng...")) return;

            boolean found = false;
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            if (parentWindow instanceof MainDashboard) {
                DefaultTableModel khModel = ((MainDashboard) parentWindow).getModelKhachHang();
                if (khModel != null) {
                    for (int i = 0; i < khModel.getRowCount(); i++) {
                        String maKH = khModel.getValueAt(i, 0).toString();  
                        String tenKH = khModel.getValueAt(i, 1).toString(); 
                        String sdtKH = khModel.getValueAt(i, 2).toString(); 
                        String diemKH = khModel.getValueAt(i, 5).toString(); 
                        
                        if (keyword.equalsIgnoreCase(maKH) || keyword.equals(sdtKH)) {
                            isCustomerLinked = true;
                            linkedTenKH = tenKH;
                            linkedSdtKH = sdtKH;
                            
                            lblLinkedAvatar.setText(tenKH.substring(0, 1).toUpperCase());
                            lblLinkedName.setText(tenKH);
                            lblLinkedSub.setText(maKH + " • " + sdtKH);
                            lblLinkedPoints.setText(diemKH + " điểm");
                            
                            try {
                                diemHienTaiKH = Integer.parseInt(diemKH.replace(".", "").replace(",", ""));
                                long tienQuyDoi = diemHienTaiKH * 100L;
                                lblLinkedMoney.setText("≈ " + String.format("%,d", tienQuyDoi).replace(',', '.') + "đ");

                                // NẾU CÓ ĐIỂM THÌ BẬT PANEL DÙNG ĐIỂM LÊN
                                if (diemHienTaiKH > 0) {
                                    lblDungDiemText.setText(String.format("Dùng %,d điểm", diemHienTaiKH) + " (-" + String.format("%,d", tienQuyDoi).replace(',', '.') + "đ)");
                                    pnlDungDiem.setVisible(true);
                                } else {
                                    pnlDungDiem.setVisible(false);
                                }
                            } catch (Exception ex) {
                                diemHienTaiKH = 0;
                                lblLinkedMoney.setText("≈ 0đ");
                                pnlDungDiem.setVisible(false);
                            }
                            
                            // Mặc định luôn tắt toggle khi mới tìm khách
                            toggleDungDiem.setOn(false);
                            isDungDiem = false;
                            recalculateTotals();

                            found = true;
                            break; 
                        }
                    }
                }
            }

            if (found) {
                pnlWarn.setVisible(false);
                pnlInputFields.setVisible(false);     
                pnlLinkedCustomer.setVisible(true);   
                CardLayout cl = (CardLayout)(pnlBadgeWrap.getLayout());
                cl.show(pnlBadgeWrap, "LINKED");      
            } else {
                pnlWarn.setVisible(true);
                pnlLinkedCustomer.setVisible(false);
                pnlInputFields.setVisible(true);
                CardLayout cl = (CardLayout)(pnlBadgeWrap.getLayout());
                cl.show(pnlBadgeWrap, "LE");
            }
            pnlBody.revalidate(); pnlBody.repaint();
        });

        btnAddCustomer.addActionListener(e -> {
            Window owner = this.getOwner(); 
            if (owner instanceof MainDashboard) {
                luuNhapHoaDon(); 
                ((MainDashboard) owner).chuyenSangTabKhachHang(true); 
            }
        });

        pnlWrapper.add(pnlHeader, BorderLayout.NORTH);
        pnlWrapper.add(pnlBody, BorderLayout.CENTER);

        return pnlWrapper;
    }
    
    private JTextField createStyledTextField(String placeholder) {
        JTextField txt = new JTextField(placeholder);
        txt.setPreferredSize(new Dimension(0, 36)); 
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setForeground(Color.GRAY);
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        
        txt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txt.getText().equals(placeholder)) {
                    txt.setText(""); txt.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txt.getText().isEmpty()) {
                    txt.setForeground(Color.GRAY); txt.setText(placeholder);
                }
            }
        });
        return txt;
    }

    private JPanel createProductPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 15));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel pnlSearchWrapper = new JPanel(new BorderLayout(10, 0));
        pnlSearchWrapper.setBackground(Color.WHITE);
        pnlSearchWrapper.setPreferredSize(new Dimension(0, 42));
        pnlSearchWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#94A3B8"), 1, true),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)
        ));

        JLabel lblSearchIcon = new JLabel(new MenuIcon("SEARCH"));
        lblSearchIcon.setForeground(Color.GRAY);
        pnlSearchWrapper.add(lblSearchIcon, BorderLayout.WEST);

        String placeholderText = "Tìm tên sản phẩm hoặc mã để thêm vào đơn hàng (Ấn Enter để thêm)...";
        JTextField txtSearchProduct = new JTextField(placeholderText);
        txtSearchProduct.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearchProduct.setForeground(Color.GRAY);
        txtSearchProduct.setBorder(null); 
        pnlSearchWrapper.add(txtSearchProduct, BorderLayout.CENTER);

        txtSearchProduct.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txtSearchProduct.getText().equals(placeholderText)) {
                    txtSearchProduct.setText("");
                    txtSearchProduct.setForeground(Color.BLACK);
                    lblSearchIcon.setForeground(Color.decode("#1967D2")); 
                    pnlSearchWrapper.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode("#1967D2"), 1, true),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)
                    ));
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtSearchProduct.getText().trim().isEmpty()) {
                    txtSearchProduct.setForeground(Color.GRAY);
                    txtSearchProduct.setText(placeholderText);
                    lblSearchIcon.setForeground(Color.GRAY); 
                    pnlSearchWrapper.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.decode("#94A3B8"), 1, true),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)
                    ));
                }
            }
        });

        pnl.add(pnlSearchWrapper, BorderLayout.NORTH);

        String[] cols = {"Sản phẩm", "ĐVT", "SL", "Đơn giá", "VAT%", "Thành tiền", ""};
        productModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Cho phép người dùng nháy đúp chuột để sửa Cột 2 (SL) và 3 (Đơn giá)
                return column == 2 || column == 3; 
            }
        };

        // ==============================================================
        // ĐOẠN CODE MỚI: TỰ ĐỘNG TÍNH LẠI TIỀN KHI NGƯỜI DÙNG SỬA SỐ LƯỢNG
        // ==============================================================
     // ==============================================================
        // ĐOẠN CODE MỚI: TỰ ĐỘNG TÍNH LẠI TIỀN (CÓ CHỐT CHỐNG LẶP VÔ HẠN)
        // ==============================================================
        productModel.addTableModelListener(e -> {
            // NẾU ĐANG TRONG QUÁ TRÌNH TỰ ĐỘNG CẬP NHẬT THÌ BỎ QUA ĐỂ CHỐNG LẶP
            if (isTableUpdating) return; 

            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                
                // Chỉ xử lý khi cột Số lượng (index 2) hoặc Đơn giá (index 3) bị thay đổi
                if (row >= 0 && (col == 2 || col == 3)) {
                    SwingUtilities.invokeLater(() -> {
                        isTableUpdating = true; // ĐÓNG CHỐT: Khóa lại không cho event chạy đệ quy
                        try {
                            // 1. Lấy và chuẩn hóa Số Lượng (SL)
                            String slStr = productModel.getValueAt(row, 2).toString().trim();
                            int sl = 1;
                            try {
                                sl = Integer.parseInt(slStr);
                                if (sl <= 0) sl = 1; // Ràng buộc không cho nhập số âm hoặc 0
                            } catch (Exception ex) {
                                sl = 1; // Nếu cố tình gõ chữ (abc) thì tự reset về 1
                            }

                            // 2. Lấy Đơn giá
                            String giaStr = productModel.getValueAt(row, 3).toString().replaceAll("[^0-9]", "");
                            long donGia = 0;
                            try { donGia = Long.parseLong(giaStr); } catch (Exception ex) {}

                            // 3. Tính Thành tiền và Ghi đè lại UI
                            long thanhTien = sl * donGia;
                            productModel.setValueAt(String.valueOf(sl), row, 2); 
                            productModel.setValueAt(String.format("%,d", thanhTien).replace(',', '.') + "đ", row, 5);
                            
                            // 4. Tính lại Tạm tính, Khuyến mãi, Tổng hóa đơn
                            recalculateTotals();
                            
                        } finally {
                            isTableUpdating = false; // MỞ CHỐT: Cập nhật xong thì mở ra lại
                        }
                    });
                }
            }
        });
        // ==============================================================
        // ==============================================================

        JTable tbl = new JTable(productModel) {
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                int tableHeight = getRowCount() * getRowHeight();
                return new Dimension(getPreferredSize().width, tableHeight);
            }
        };
        
        tbl.setRowHeight(45); 
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tbl.setShowGrid(false); 
        tbl.setShowHorizontalLines(true); 
        tbl.setGridColor(Color.decode("#F1F3F5"));

        tbl.getTableHeader().setBackground(Color.decode("#D9EAF7")); 
        tbl.getTableHeader().setForeground(Color.decode("#1E293B"));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tbl.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tbl.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        tbl.getColumnModel().getColumn(0).setPreferredWidth(250); 
        tbl.getColumnModel().getColumn(1).setPreferredWidth(60);  
        tbl.getColumnModel().getColumn(2).setPreferredWidth(50);  
        tbl.getColumnModel().getColumn(3).setPreferredWidth(100); 
        tbl.getColumnModel().getColumn(4).setPreferredWidth(50);  
        tbl.getColumnModel().getColumn(5).setPreferredWidth(100); 
        tbl.getColumnModel().getColumn(6).setPreferredWidth(40);  

        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int i=1; i<=4; i++) tbl.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        
        tbl.getColumnModel().getColumn(5).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSel, hasFocus, r, c);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lbl.setForeground(Color.decode("#111827"));
                lbl.setHorizontalAlignment(JLabel.RIGHT); 
                return lbl;
            }
        });
        
        tbl.getColumnModel().getColumn(6).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSel, hasFocus, r, c);
                lbl.setText(""); 
                lbl.setIcon(new MenuIcon("TRASH")); 
                lbl.setForeground(Color.decode("#EF4444")); 
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return lbl;
            }
        });

        JScrollPane sp = new JScrollPane(tbl);
        sp.getViewport().setBackground(Color.WHITE); 
        sp.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"))); 

        sp.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        sp.getVerticalScrollBar().setUnitIncrement(16);

        pnl.add(sp, BorderLayout.CENTER);

        tbl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) { // Dùng mousePressed để nhạy hơn
                int row = tbl.rowAtPoint(e.getPoint());
                int col = tbl.columnAtPoint(e.getPoint());
                
                if (row >= 0) {
                    // 1. CLICK TRÁI VÀO ICON THÙNG RÁC (Cột 6) -> XÓA DÒNG
                    if (col == 6 && SwingUtilities.isLeftMouseButton(e)) { 
                        productModel.removeRow(row);
                        recalculateTotals(); 
                        sp.revalidate(); sp.repaint();
                        return; // Xóa xong thì thoát luôn lệnh
                    } 
                    
                    // Nếu đang gõ tay số lượng dở dang mà click ra ngoài thì tự lưu lại
                    if (tbl.isEditing()) tbl.getCellEditor().stopCellEditing(); 

                    // 2. TÍNH NĂNG POS CLICK TĂNG/GIẢM TRỰC TIẾP TRÊN BẢNG
                    // Mẹo: Loại trừ cột Số lượng (2), Đơn giá (3) ra để người dùng vẫn có thể nháy đúp gõ tay số lượng lớn.
                    // Chỉ áp dụng khi click vào các cột Tên, ĐVT, VAT, Thành tiền
                    if (col != 2 && col != 3 && col != 6) {
                        int slHienTai = Integer.parseInt(productModel.getValueAt(row, 2).toString());
                        
                        // CHUỘT TRÁI -> TĂNG 1
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            // Cập nhật SL, phần TableModelListener bạn viết ở trên sẽ tự nhân lại giá!
                            productModel.setValueAt(String.valueOf(slHienTai + 1), row, 2);
                        } 
                        // CHUỘT PHẢI -> GIẢM 1
                        else if (SwingUtilities.isRightMouseButton(e)) {
                            if (slHienTai > 1) {
                                productModel.setValueAt(String.valueOf(slHienTai - 1), row, 2);
                            } else {
                                productModel.removeRow(row); // Trừ về 0 thì tự xóa luôn
                                recalculateTotals();
                            }
                        }
                    }
                }
            }
        });

        // 3. TÍNH NĂNG MÁY POS: CHỌN DÒNG + BẤM PHÍM [+] HOẶC [-] TRÊN BÀN PHÍM
        tbl.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                int row = tbl.getSelectedRow();
                if (row >= 0 && !tbl.isEditing()) {
                    // Bấm phím Dấu Cộng (+)
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ADD || e.getKeyCode() == java.awt.event.KeyEvent.VK_EQUALS) {
                        int slHienTai = Integer.parseInt(productModel.getValueAt(row, 2).toString());
                        productModel.setValueAt(String.valueOf(slHienTai + 1), row, 2); 
                    } 
                    // Bấm phím Dấu Trừ (-)
                    else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_SUBTRACT || e.getKeyCode() == java.awt.event.KeyEvent.VK_MINUS) {
                        int slHienTai = Integer.parseInt(productModel.getValueAt(row, 2).toString());
                        if (slHienTai > 1) {
                            productModel.setValueAt(String.valueOf(slHienTai - 1), row, 2); 
                        } else {
                            productModel.removeRow(row);
                            recalculateTotals();
                        }
                    }
                }
            }
        });

        JPopupMenu suggestionPopup = new JPopupMenu();
        suggestionPopup.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8")));
        suggestionPopup.setBackground(Color.WHITE);

        // --- FIX LỖI ĐƠ TÌM KIẾM: SỬ DỤNG DEBOUNCE + SWING WORKER ---
        javax.swing.Timer searchTimer = new javax.swing.Timer(300, e -> {
            String text = txtSearchProduct.getText().trim();

            if (text.isEmpty() || text.length() < 2 || text.equals(placeholderText.toLowerCase()) || text.equals(placeholderText)) {
                suggestionPopup.setVisible(false);
                return;
            }

            new SwingWorker<java.util.List<Object[]>, Void>() {
                @Override
                protected java.util.List<Object[]> doInBackground() throws Exception {
                    DAO.DAO_SanPham daoSP = new DAO.DAO_SanPham();
                    return daoSP.timKiemSanPhamBan(text);
                }

                @Override
                protected void done() {
                    try {
                        java.util.List<Object[]> ketQua = get();
                        suggestionPopup.removeAll();
                        boolean hasResult = false;

                        if (ketQua != null && !ketQua.isEmpty()) {
                            for (Object[] row : ketQua) {
                                String id = row[0].toString();
                                String ten = row[1].toString();
                                String donVi = row[2].toString();
                                long giaBan = Math.round(Double.parseDouble(row[3].toString()));
                                String gia = String.valueOf(giaBan); 
                                String tonKho = row[4].toString();

                                suggestionPopup.add(createSuggestionItem(suggestionPopup, txtSearchProduct, "PILL", ten, donVi, gia, tonKho));
                            }
                            hasResult = true;
                        }

                        if (hasResult) {
                            suggestionPopup.setPreferredSize(new Dimension(pnlSearchWrapper.getWidth(), suggestionPopup.getPreferredSize().height));
                            suggestionPopup.show(pnlSearchWrapper, 0, pnlSearchWrapper.getHeight());
                        } else {
                            JMenuItem emptyItem = new JMenuItem("Không tìm thấy sản phẩm nào phù hợp...");
                            emptyItem.setEnabled(false);
                            suggestionPopup.add(emptyItem);
                            suggestionPopup.setPreferredSize(new Dimension(pnlSearchWrapper.getWidth(), suggestionPopup.getPreferredSize().height));
                            suggestionPopup.show(pnlSearchWrapper, 0, pnlSearchWrapper.getHeight());
                        }
                        txtSearchProduct.requestFocus(); 
                        
                    } catch (Exception ex) {
                        System.err.println("Lỗi khi hiển thị UI tìm kiếm: " + ex.getMessage());
                    }
                }
            }.execute();
        });
        
        searchTimer.setRepeats(false); 

        txtSearchProduct.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP || e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
                    return; 
                }
                searchTimer.restart(); 
            }
        });

        return pnl;
    }

    private JPanel createSummaryPanel() {
        JPanel pnlWrapper = new JPanel(new BorderLayout(0, 15));
        pnlWrapper.setBackground(Color.WHITE);

        JPanel pnlMethod = new JPanel(new BorderLayout(0, 5));
        pnlMethod.setBackground(Color.WHITE);
        
        JLabel lblMethodTitle = new JLabel("Phương thức thanh toán");
        lblMethodTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlMethod.add(lblMethodTitle, BorderLayout.NORTH);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlButtons.setBackground(Color.WHITE);
        
        JButton btnTienMat = new JButton("Tiền mặt");
        JButton btnChuyenKhoan = new JButton("Chuyển khoản");
        
        Dimension btnSize = new Dimension(150, 40);
        btnTienMat.setPreferredSize(btnSize);
        btnChuyenKhoan.setPreferredSize(btnSize);
        
        setPaymentBtnActive(btnTienMat);
        setPaymentBtnInactive(btnChuyenKhoan);
        
        pnlButtons.add(btnTienMat);
        pnlButtons.add(btnChuyenKhoan);
        pnlMethod.add(pnlButtons, BorderLayout.CENTER);

        JPanel pnlTienMatWrapper = createCashGridPanel();

        JPanel pnlNote = new JPanel(new BorderLayout(0, 5));
        pnlNote.setBackground(Color.WHITE);
        JLabel lblNote = new JLabel("Ghi chú");
        lblNote.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JTextField txtNote = new JTextField("Ghi chú thêm...");
        txtNote.setForeground(Color.GRAY);
        txtNote.setPreferredSize(new Dimension(0, 40));
        txtNote.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        
        txtNote.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txtNote.getText().equals("Ghi chú thêm...")) { txtNote.setText(""); txtNote.setForeground(Color.BLACK); }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtNote.getText().isEmpty()) { txtNote.setForeground(Color.GRAY); txtNote.setText("Ghi chú thêm..."); }
            }
        });
        
        pnlNote.add(lblNote, BorderLayout.NORTH);
        pnlNote.add(txtNote, BorderLayout.CENTER);

        // --- ĐÃ ĐỔI THÀNH GRIDLAYOUT(5, 2) ĐỂ CHỨA DÒNG DÙNG ĐIỂM ---
        JPanel pnlFinal = new JPanel(new GridLayout(5, 2, 10, 5));
        pnlFinal.setBackground(darkBlue);
        pnlFinal.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        pnlFinal.add(createWhiteLabel("Tạm tính:")); 
        lblSubtotalValue = createWhiteLabel("0đ", SwingConstants.RIGHT);
        pnlFinal.add(lblSubtotalValue);

        pnlFinal.add(createWhiteLabel("VAT (5%):")); 
        lblVatValue = createWhiteLabel("+0đ", SwingConstants.RIGHT);
        pnlFinal.add(lblVatValue);
        
        // DÒNG KHUYẾN MÃI
        pnlFinal.add(createWhiteLabel("Giảm khuyến mãi:")); 
        lblDiscountValue = createWhiteLabel("-0đ", SwingConstants.RIGHT);
        lblDiscountValue.setForeground(Color.decode("#FCA5A5")); // Đỏ nhạt
        pnlFinal.add(lblDiscountValue);
        
        // DÒNG DÙNG ĐIỂM
        pnlFinal.add(createWhiteLabel("Dùng điểm:")); 
        lblDungDiemValue = createWhiteLabel("-0đ", SwingConstants.RIGHT);
        lblDungDiemValue.setForeground(Color.decode("#FCD34D")); // Vàng cam
        pnlFinal.add(lblDungDiemValue);
        
        JLabel lblTotal = new JLabel("TỔNG THANH TOÁN:");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18)); lblTotal.setForeground(Color.WHITE);
        
        lblTotalPriceValue = new JLabel("0đ");
        lblTotalPriceValue.setFont(new Font("Segoe UI", Font.BOLD, 24)); lblTotalPriceValue.setForeground(Color.YELLOW);
        lblTotalPriceValue.setHorizontalAlignment(SwingConstants.RIGHT);

        pnlFinal.add(lblTotal); pnlFinal.add(lblTotalPriceValue);

        JPanel pnlCenter = new JPanel();
        pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));
        pnlCenter.setBackground(Color.WHITE);
        
        pnlMethod.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlTienMatWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlFinal.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        pnlCenter.add(pnlMethod);
        pnlCenter.add(Box.createRigidArea(new Dimension(0, 10)));
        pnlCenter.add(pnlTienMatWrapper); 
        pnlCenter.add(Box.createRigidArea(new Dimension(0, 15)));
        pnlCenter.add(pnlNote);
        pnlCenter.add(Box.createRigidArea(new Dimension(0, 20)));
        pnlCenter.add(pnlFinal);

        pnlWrapper.add(pnlCenter, BorderLayout.CENTER);
        
        btnTienMat.addActionListener(e -> {
            phuongThuc = "Tiền mặt";
            setPaymentBtnActive(btnTienMat);
            setPaymentBtnInactive(btnChuyenKhoan);
            pnlTienMatWrapper.setVisible(true); 
            pnlWrapper.revalidate(); pnlWrapper.repaint();
        });

        btnChuyenKhoan.addActionListener(e -> {
            phuongThuc = "Chuyển khoản";
            setPaymentBtnActive(btnChuyenKhoan);
            setPaymentBtnInactive(btnTienMat);
            pnlTienMatWrapper.setVisible(false); 
            pnlWrapper.revalidate(); pnlWrapper.repaint();
        });

        return pnlWrapper;
    }

    private JLabel createWhiteLabel(String text) { return createWhiteLabel(text, SwingConstants.LEFT); }
    private JLabel createWhiteLabel(String text, int align) {
        JLabel l = new JLabel(text, align); l.setForeground(Color.WHITE); return l;
    }
    
    private void setPaymentBtnActive(JButton btn) {
        btn.setBackground(Color.decode("#1967D2"));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createLineBorder(Color.decode("#1967D2"), 1, true));
        btn.setFocusPainted(false);
    }

    private void setPaymentBtnInactive(JButton btn) {
        btn.setBackground(Color.WHITE);
        btn.setForeground(Color.decode("#4B5563"));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true));
        btn.setFocusPainted(false);
    }

    private JPanel createCashGridPanel() {
        JPanel pnl = new JPanel(new BorderLayout(0, 10));
        pnl.setBackground(Color.decode("#FFFBEB"));
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#FDE047"), 1, true),
            new EmptyBorder(10, 15, 10, 15)
        ));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("Tiền khách đưa");
        lblTitle.setIconTextGap(8);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(Color.decode("#B45309"));

        JButton btnChon = new JButton("Chọn mệnh giá");
        btnChon.setBackground(Color.decode("#F59E0B"));
        btnChon.setForeground(Color.WHITE);
        btnChon.setFocusPainted(false);
        btnChon.setBorderPainted(false);
        btnChon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnChon.setPreferredSize(new Dimension(130, 30));
        
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(btnChon, BorderLayout.EAST);

        JPanel pnlGrid = new JPanel(new GridLayout(3, 3, 10, 10));
        pnlGrid.setOpaque(false);
        
        pnlGrid.add(createMoneyCell("500.000đ", 500000, "#3B82F6")); 
        pnlGrid.add(createMoneyCell("200.000đ", 200000, "#D946EF")); 
        pnlGrid.add(createMoneyCell("100.000đ", 100000, "#10B981")); 
        pnlGrid.add(createMoneyCell("50.000đ", 50000, "#EAB308"));  
        pnlGrid.add(createMoneyCell("20.000đ", 20000, "#EF4444"));  
        pnlGrid.add(createMoneyCell("10.000đ", 10000, "#F43F5E"));  
        pnlGrid.add(createMoneyCell("5.000đ", 5000, "#EC4899"));   
        pnlGrid.add(createMoneyCell("2.000đ", 2000, "#6B7280"));   
        pnlGrid.add(createMoneyCell("1.000đ", 1000, "#6B7280"));   

        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setOpaque(false);
        pnlFooter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#FDE047")),
            new EmptyBorder(10, 0, 0, 0)
        ));
        
        JLabel lblTotalText = new JLabel("Tổng tiền mặt");
        lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalText.setForeground(Color.decode("#D97706"));
        
        lblTotalValue = new JLabel("0đ");
        lblTotalValue.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotalValue.setForeground(Color.decode("#D97706"));
        
        JPanel pnlInfoRight = new JPanel(new GridLayout(2, 1));
        pnlInfoRight.setOpaque(false);
        pnlInfoRight.add(lblTotalValue);
        
        lblTienThuaValue = new JLabel("Tiền thừa: 0đ", SwingConstants.RIGHT);
        lblTienThuaValue.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlInfoRight.add(lblTienThuaValue);

        pnlFooter.add(lblTotalText, BorderLayout.WEST);
        pnlFooter.add(pnlInfoRight, BorderLayout.EAST);

        pnl.add(pnlHeader, BorderLayout.NORTH);
        pnl.add(pnlGrid, BorderLayout.CENTER);
        pnl.add(pnlFooter, BorderLayout.SOUTH);

        return pnl;
    }

    private JPanel createMoneyCell(String labelText, long faceValue, String hexColor) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode(hexColor), 1, true),
            new EmptyBorder(8, 10, 8, 10) 
        ));
        
        pnl.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblValue = new JLabel(labelText);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblValue.setForeground(Color.decode(hexColor));

        JLabel lblCount = new JLabel("0"); 
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCount.setForeground(Color.decode("#111827"));
        
        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlRight.setOpaque(false);
        pnlRight.add(lblCount);

        final int[] count = {0}; 

        java.awt.event.MouseAdapter clickAdapter = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    count[0]++;
                    lblCount.setText(String.valueOf(count[0]));
                    tongTienMat += faceValue;
                    capNhatTongTien();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    if (count[0] > 0) { 
                        count[0]--;
                        lblCount.setText(String.valueOf(count[0]));
                        tongTienMat -= faceValue;
                        capNhatTongTien(); 
                    }
                }
            }
        };

        pnl.addMouseListener(clickAdapter);
        lblValue.addMouseListener(clickAdapter);
        lblCount.addMouseListener(clickAdapter);
        pnlRight.addMouseListener(clickAdapter);

        pnl.add(lblValue, BorderLayout.WEST);
        pnl.add(pnlRight, BorderLayout.EAST);
        
        return pnl;
    }
    
    private void capNhatTongTien() {
        if (lblTotalValue != null) {
            String formattedString = String.format("%,d", tongTienMat).replace(',', '.');
            lblTotalValue.setText(formattedString + "đ");
            
            long tienThua = tongTienMat - tongHoaDon;
            if (tienThua >= 0) {
                lblTienThuaValue.setText("Tiền thừa: " + String.format("%,d", tienThua).replace(',', '.') + "đ");
                lblTienThuaValue.setForeground(Color.decode("#10B981")); 
            } else {
                lblTienThuaValue.setText("Còn thiếu...");
                lblTienThuaValue.setForeground(Color.decode("#EF4444")); 
            }
        }
    }

    private void luuNhapHoaDon() {
        String khach = isCustomerLinked ? linkedTenKH : txtName.getText();
        if(khach.equals("Tên khách (bỏ trống = Khách lẻ)") || khach.trim().isEmpty()) khach = "Khách lẻ";
        String sdt = isCustomerLinked ? linkedSdtKH : txtPhone.getText().replace("Số điện thoại (tuỳ chọn)", "");
        String tongTien = lblTotalPriceValue.getText();

        if (editingModelRow != -1) {
            mainTableModel.setValueAt(khach, editingModelRow, 2);
            mainTableModel.setValueAt(sdt, editingModelRow, 3);
            mainTableModel.setValueAt(tongTien, editingModelRow, 5);
        } else {
            String maHD = "HD-TEMP-" + (System.currentTimeMillis() % 1000);
            String ngay = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            mainTableModel.addRow(new Object[]{
                maHD, ngay, khach, sdt, phuongThuc, tongTien, "Đang xử lý", "", "TPCN"
            });
        }
        dispose(); 
    }
    
    private JPanel createSuggestionItem(JPopupMenu popup, JTextField txtSearch, String iconType, String name, String unit, String price, String stock) {
        JPanel pnl = new JPanel(new BorderLayout(10, 0));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(10, 15, 10, 15));
        pnl.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // --- 1. TÌM XEM SẢN PHẨM NÀY ĐÃ CÓ BAO NHIÊU CÁI TRONG BẢNG ĐỂ HIỆN SỐ LÊN ---
        int slKhoiTao = 0;
        for (int i = 0; i < productModel.getRowCount(); i++) {
            if (productModel.getValueAt(i, 0).toString().equals(name) &&
                productModel.getValueAt(i, 1).toString().equals(unit)) {
                slKhoiTao = Integer.parseInt(productModel.getValueAt(i, 2).toString());
                break;
            }
        }

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlLeft.setOpaque(false);

        JLabel lblIcon = new JLabel(new MenuIcon(iconType));
        lblIcon.setForeground(Color.decode("#1967D2")); 

        JLabel lblName = new JLabel(name);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setForeground(Color.decode("#111827"));

        JLabel lblUnit = new JLabel(unit);
        lblUnit.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUnit.setForeground(Color.decode("#6B7280"));

        pnlLeft.add(lblIcon);
        pnlLeft.add(lblName);
        pnlLeft.add(lblUnit);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlRight.setOpaque(false);

        // --- 2. THÊM NHÃN SỐ LƯỢNG (GIỐNG TIỀN KHÁCH ĐƯA) ---
        JLabel lblCount = new JLabel(slKhoiTao > 0 ? "[" + slKhoiTao + "]" : "");
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblCount.setForeground(Color.decode("#E11D48")); // Màu đỏ nổi bật
        lblCount.setPreferredSize(new Dimension(35, 20));
        lblCount.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblPrice = new JLabel(String.format("%,d", Integer.parseInt(price)).replace(',', '.') + "đ");
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPrice.setForeground(Color.decode("#111827"));

        JLabel lblStock = new JLabel("Tồn: " + stock);
        lblStock.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStock.setForeground(Color.decode("#059669")); 

        // Gắn vào giao diện bên phải
        pnlRight.add(lblCount);
        pnlRight.add(lblPrice);
        pnlRight.add(lblStock);

        pnl.add(pnlLeft, BorderLayout.WEST);
        pnl.add(pnlRight, BorderLayout.EAST);

        // --- 3. XỬ LÝ CLICK CHUỘT TRÁI / PHẢI ---
        pnl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                pnl.setBackground(Color.decode("#F3F4F6")); 
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                pnl.setBackground(Color.WHITE);
            }
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                boolean isLeftClick = SwingUtilities.isLeftMouseButton(e);
                boolean isRightClick = SwingUtilities.isRightMouseButton(e);

                // Quét lại bảng để tìm vị trí và số lượng chính xác nhất lúc click
                boolean daTonTai = false;
                int rowIndex = -1;
                int currentQty = 0;
                
                for (int i = 0; i < productModel.getRowCount(); i++) {
                    if (productModel.getValueAt(i, 0).toString().equals(name) &&
                        productModel.getValueAt(i, 1).toString().equals(unit)) {
                        daTonTai = true;
                        rowIndex = i;
                        currentQty = Integer.parseInt(productModel.getValueAt(i, 2).toString());
                        break;
                    }
                }
                
                if (isLeftClick) {
                    // CỘNG THÊM
                    currentQty++;
                    if (daTonTai) {
                        productModel.setValueAt(String.valueOf(currentQty), rowIndex, 2);
                        long donGia = Long.parseLong(price);
                        productModel.setValueAt(String.format("%,d", donGia * currentQty).replace(',', '.') + "đ", rowIndex, 5);
                    } else {
                        productModel.addRow(new Object[]{
                            name, unit, "1", price, "5%", String.format("%,d", Integer.parseInt(price)).replace(',', '.') + "đ", ""
                        });
                    }
                    lblCount.setText("[" + currentQty + "]"); // Cập nhật nhãn đếm
                    
                } else if (isRightClick) {
                    // TRỪ ĐI
                    if (daTonTai) {
                        currentQty--;
                        if (currentQty > 0) {
                            productModel.setValueAt(String.valueOf(currentQty), rowIndex, 2);
                            long donGia = Long.parseLong(price);
                            productModel.setValueAt(String.format("%,d", donGia * currentQty).replace(',', '.') + "đ", rowIndex, 5);
                            lblCount.setText("[" + currentQty + "]"); // Cập nhật nhãn đếm
                        } else {
                            productModel.removeRow(rowIndex); // Bằng 0 thì xóa luôn khỏi bảng
                            lblCount.setText(""); // Ẩn nhãn đếm
                        }
                    }
                }
                
                recalculateTotals(); 
            }
        });

        return pnl;
    }
    
    private JPanel createVoucherTag(String tenTag, String maKM, JTextField txtInput, JButton btnApply) {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        pnl.setName(maKM.trim()); // LƯU ID ĐÃ ĐƯỢC LÀM SẠCH
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E5E7EB"), 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
        pnl.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblText = new JLabel(tenTag);
        lblText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblText.setForeground(Color.decode("#4B5563"));

        pnl.add(lblText);

        pnl.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                txtInput.setText(maKM.trim());
                txtInput.setForeground(Color.BLACK);
                btnApply.doClick(); 
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                String currentKM = maKhuyenMaiApDung == null ? "" : maKhuyenMaiApDung.trim();
                // Chỉ đổi màu xám hover nếu tag này CHƯA được chọn
                if (!maKM.trim().equalsIgnoreCase(currentKM)) {
                    pnl.setBackground(Color.decode("#F3F4F6"));
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                String currentKM = maKhuyenMaiApDung == null ? "" : maKhuyenMaiApDung.trim();
                // Trả về nền trắng nếu tag này CHƯA được chọn
                if (!maKM.trim().equalsIgnoreCase(currentKM)) {
                    pnl.setBackground(Color.WHITE);
                }
            }
        });

        return pnl;
    }
    
    private String phatSinhMaHoaDon() {
        int year = java.time.Year.now().getValue();
        String maMoi = "HD-" + year + "-0001"; 
        
        try {
            java.sql.Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT MAX(id) FROM HoaDon WHERE id LIKE 'HD-" + year + "-%'";
            java.sql.Statement st = con.createStatement();
            java.sql.ResultSet rs = st.executeQuery(sql);
            
            if (rs.next()) {
                String maxId = rs.getString(1);
                if (maxId != null) {
                    String[] parts = maxId.split("-");
                    if (parts.length == 3) {
                        int stt = Integer.parseInt(parts[2]) + 1;
                        maMoi = String.format("HD-%d-%04d", year, stt);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maMoi;
    }
    private void updateVoucherTagsUI() {
        if (pnlVoucherTags == null) return;
        
        // Dọn sạch khoảng trắng của biến lưu trữ
        String maApDung = (maKhuyenMaiApDung == null) ? "" : maKhuyenMaiApDung.trim();
        
        for (Component comp : pnlVoucherTags.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel pnl = (JPanel) comp;
                String maKM = pnl.getName(); 
                
                if (maKM != null && pnl.getComponentCount() > 0) {
                    maKM = maKM.trim(); // TRỊ TẬN GỐC KHOẢNG TRẮNG CỦA DATABASE
                    JLabel lblText = (JLabel) pnl.getComponent(0);
                    
                    if (!maApDung.isEmpty() && maKM.equalsIgnoreCase(maApDung)) {
                        // NẾU ĐƯỢC CHỌN: Đổi nền xanh ngọc, viền xanh lá, chữ đậm
                        pnl.setBackground(Color.decode("#D1FAE5")); 
                        pnl.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.decode("#10B981"), 1, true),
                            new EmptyBorder(4, 8, 4, 8)
                        ));
                        lblText.setForeground(Color.decode("#047857"));
                        lblText.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else {
                        // BÌNH THƯỜNG: Trả về nền trắng
                        pnl.setBackground(Color.WHITE);
                        pnl.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.decode("#E5E7EB"), 1, true),
                            new EmptyBorder(4, 8, 4, 8)
                        ));
                        lblText.setForeground(Color.decode("#4B5563"));
                        lblText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    }
                }
            }
        }
        // ÉP GIAO DIỆN VẼ LẠI MÀU NGAY LẬP TỨC
        pnlVoucherTags.revalidate();
        pnlVoucherTags.repaint();
    }
    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(130, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
    }
}