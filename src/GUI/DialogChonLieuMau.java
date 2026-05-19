package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

public class DialogChonLieuMau extends JDialog {

    private TaoHoaDon parentForm; // Lưu trữ form cha để gọi hàm đổ dữ liệu ngược về

    // Chuyển các biến toàn cục từ form cũ sang đây
    private JPanel pnlDanhSachThuoc;
    private JLabel lblTongTienCombo, lblThongTinCombo, lblThanhPhan;
    private int soNgayUong = 5;
    private JTextArea txtHuongDan;
    private List<RowThuocCombo> danhSachRowThuoc = new ArrayList<>();
    private BUS.BUS_LieuMau busLieuMau = new BUS.BUS_LieuMau();

    public DialogChonLieuMau(TaoHoaDon parent) {
        super((Frame) SwingUtilities.getWindowAncestor(parent), "Thêm Thuốc Cắt Liều / Combo mẫu", true);
        this.parentForm = parent;

        setUndecorated(true);
        setSize(1280, 720);
        setLocationRelativeTo(parent);

        // --- KHUNG BAO NGOÀI CÙNG ---
        JPanel pnlMainBorder = new JPanel(new BorderLayout());
        pnlMainBorder.setBorder(BorderFactory.createLineBorder(Color.decode("#152A4B"), 2));

        // --- HEADER BAR ---
        JPanel pnlHeaderBar = new JPanel(new BorderLayout());
        pnlHeaderBar.setBackground(Color.decode("#152A4B"));
        pnlHeaderBar.setPreferredSize(new Dimension(0, 45));
        pnlHeaderBar.setBorder(new EmptyBorder(0, 15, 0, 10));

        JLabel lblDialogTitle = new JLabel("Thêm Thuốc Cắt Liều / Combo mẫu");
        lblDialogTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDialogTitle.setForeground(Color.WHITE);
        pnlHeaderBar.add(lblDialogTitle, BorderLayout.WEST);

        JButton btnClose = new JButton("✕");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnClose.setForeground(Color.WHITE);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> this.dispose());
        pnlHeaderBar.add(btnClose, BorderLayout.EAST);

        final Point[] dragPoint = new Point[1];
        pnlHeaderBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) { dragPoint[0] = e.getPoint(); }
        });
        pnlHeaderBar.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent e) {
                Point curr = e.getLocationOnScreen();
                setLocation(curr.x - dragPoint[0].x, curr.y - dragPoint[0].y);
            }
        });

        pnlMainBorder.add(pnlHeaderBar, BorderLayout.NORTH);

        // --- NỘI DUNG CHÍNH ---
        JPanel pnlContent = new JPanel(new BorderLayout());
        pnlContent.setBackground(Color.decode("#F8F9FA"));

        // ===== CỘT TRÁI =====
        JPanel pnlLeft = new JPanel(new BorderLayout(0, 15));
        pnlLeft.setPreferredSize(new Dimension(230, 0));
        pnlLeft.setBackground(Color.WHITE);
        pnlLeft.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, Color.decode("#DFE3E8")),
            new EmptyBorder(20, 15, 20, 15)
        ));

        JPanel pnlChonLieu = new JPanel(new BorderLayout(0, 8));
        pnlChonLieu.setOpaque(false);
        JLabel lblChonLieu = new JLabel("CHỌN LIỀU THUỐC MẪU");
        lblChonLieu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblChonLieu.setForeground(Color.decode("#4B5563"));
        
        JComboBox<String> cboLieuMau = new JComboBox<>();
        cboLieuMau.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cboLieuMau.setPreferredSize(new Dimension(0, 30));
        cboLieuMau.setBackground(Color.WHITE);

        cboLieuMau.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                if (value != null) {
                    String text = value.toString();
                    if (text.startsWith("[")) {
                        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        label.setForeground(Color.decode("#1E3A8A"));
                        label.setBackground(Color.decode("#F3F4F6"));
                        label.setText("  " + text); 
                        label.setIcon(UIManager.getIcon("FileView.directoryIcon"));
                    } else {
                        label.setForeground(Color.DARK_GRAY);
                        if (index != -1) label.setBorder(BorderFactory.createEmptyBorder(2, 15, 2, 5));
                        label.setIcon(UIManager.getIcon("FileView.fileIcon"));
                    }
                }
                return label;
            }
        });

        JButton btnThemLieuMoi = new JButton("+");
        btnThemLieuMoi.setFont(new Font("Segoe UI", Font.BOLD, 8));
        btnThemLieuMoi.setBackground(Color.decode("#10B981")); 
        btnThemLieuMoi.setForeground(Color.WHITE);
        btnThemLieuMoi.setFocusPainted(false);
        btnThemLieuMoi.setBorderPainted(false);
        btnThemLieuMoi.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnThemLieuMoi.setPreferredSize(new Dimension(35, 30));

        btnThemLieuMoi.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Tính năng tạo liều mới đang phát triển!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel pnlComboAndBtn = new JPanel(new BorderLayout(5, 0));
        pnlComboAndBtn.setOpaque(false);
        pnlComboAndBtn.add(cboLieuMau, BorderLayout.CENTER);
        pnlComboAndBtn.add(btnThemLieuMoi, BorderLayout.EAST);

        lblThanhPhan = new JLabel("0 thành phần");
        lblThanhPhan.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblThanhPhan.setForeground(Color.GRAY);
        lblThanhPhan.setBorder(new EmptyBorder(4, 5, 0, 0));

        pnlChonLieu.add(lblChonLieu, BorderLayout.NORTH);
        pnlChonLieu.add(pnlComboAndBtn, BorderLayout.CENTER);
        pnlChonLieu.add(lblThanhPhan, BorderLayout.SOUTH);

        JPanel pnlSoNgay = new JPanel(new BorderLayout(0, 8));
        pnlSoNgay.setOpaque(false);
        JLabel lblSoNgay = new JLabel("SỐ NGÀY UỐNG");
        lblSoNgay.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSoNgay.setForeground(Color.decode("#4B5563"));
        
        JPanel pnlCounter = new JPanel(new GridLayout(1, 3));
        pnlCounter.setPreferredSize(new Dimension(0, 40));
        pnlCounter.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true));
        
        JButton btnMinus = new JButton("−");
        btnMinus.setBackground(Color.WHITE); btnMinus.setFocusPainted(false); btnMinus.setBorder(null);
        btnMinus.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        JLabel lblDays = new JLabel("5", SwingConstants.CENTER);
        lblDays.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblDays.setForeground(Color.decode("#152A4B"));
        lblDays.setOpaque(true); lblDays.setBackground(Color.WHITE);
        
        JButton btnPlus = new JButton("+");
        btnPlus.setBackground(Color.WHITE); btnPlus.setFocusPainted(false); btnPlus.setBorder(null);
        btnPlus.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        pnlCounter.add(btnMinus); pnlCounter.add(lblDays); pnlCounter.add(btnPlus);
        
        JLabel lblSubDays = new JLabel("Số ngày × Số lượng/Ngày = Tổng xuất kho");
        lblSubDays.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblSubDays.setForeground(Color.GRAY);

        pnlSoNgay.add(lblSoNgay, BorderLayout.NORTH);
        pnlSoNgay.add(pnlCounter, BorderLayout.CENTER);
        pnlSoNgay.add(lblSubDays, BorderLayout.SOUTH);

        JPanel pnlLeftTop = new JPanel();
        pnlLeftTop.setLayout(new BoxLayout(pnlLeftTop, BoxLayout.Y_AXIS));
        pnlLeftTop.setOpaque(false);
        pnlLeftTop.add(pnlChonLieu);
        pnlLeftTop.add(Box.createRigidArea(new Dimension(0, 20)));
        pnlLeftTop.add(pnlSoNgay);
        pnlLeft.add(pnlLeftTop, BorderLayout.NORTH);

        // ===== CỘT GIỮA =====
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 10));
        pnlCenter.setBackground(Color.WHITE);
        pnlCenter.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JTextField txtSearchCenter = new JTextField("   Quét mã vạch hoặc nhập tên thuốc để tìm kiếm...");
        txtSearchCenter.setPreferredSize(new Dimension(0, 45));
        txtSearchCenter.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        txtSearchCenter.setForeground(Color.GRAY);
        txtSearchCenter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#3B82F6"), 2, true),
            new EmptyBorder(0, 10, 0, 10)
        ));

        txtSearchCenter.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txtSearchCenter.getText().contains("Quét mã vạch hoặc nhập tên")) {
                    txtSearchCenter.setText(""); txtSearchCenter.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtSearchCenter.getText().trim().isEmpty()) {
                    txtSearchCenter.setForeground(Color.GRAY);
                    txtSearchCenter.setText("   Quét mã vạch hoặc nhập tên thuốc để tìm kiếm...");
                }
            }
        });

        txtSearchCenter.addActionListener(e -> {
            String qrCode = txtSearchCenter.getText().trim();
            if (qrCode.isEmpty() || qrCode.contains("Quét mã vạch")) return;
            txtSearchCenter.setText(""); 
            
            try {
                BUS.BUS_SanPham busSP = new BUS.BUS_SanPham();
                List<Object[]> ketQua = busSP.timKiemSanPhamBan(qrCode);
                
                if (ketQua != null && !ketQua.isEmpty()) {
                    String maSPTimDuoc = ketQua.get(0)[0].toString();
                    String soLoTimDuoc = (ketQua.get(0).length > 7 && ketQua.get(0)[7] != null) ? ketQua.get(0)[7].toString() : "";
                    
                    boolean found = false;
                    for (RowThuocCombo row : danhSachRowThuoc) {
                        if (row.sanPhamId.equals(maSPTimDuoc)) {
                            row.chkChon.setSelected(true); 
                            if (!soLoTimDuoc.isEmpty()) {
                                for (int i = 0; i < row.cboLo.getItemCount(); i++) {
                                    if (row.cboLo.getItemAt(i).startsWith(soLoTimDuoc)) {
                                        row.cboLo.setSelectedIndex(i); break;
                                    }
                                }
                            }
                            row.lblStatus.setText("Đã xác thực QR");
                            row.lblStatus.setForeground(Color.decode("#059669"));
                            row.lblStatus.setBackground(Color.decode("#D1FAE5"));
                            row.lblStatus.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(Color.decode("#34D399"), 1, true),
                                new EmptyBorder(6, 8, 6, 8)
                            ));
                            
                            found = true;
                            capNhatTongTienCombo();
                            break;
                        }
                    }
                    if (!found) JOptionPane.showMessageDialog(this, "Sản phẩm vừa quét không nằm trong Liều mẫu này!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Mã QR / Mã vạch không hợp lệ hoặc không có trong kho!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        
        JPanel pnlHeaderTable = new JPanel(new GridBagLayout());
        pnlHeaderTable.setBackground(Color.decode("#F8FAFC"));
        pnlHeaderTable.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E2E8F0")));
        GridBagConstraints gbcH = new GridBagConstraints();
        gbcH.fill = GridBagConstraints.BOTH; gbcH.insets = new Insets(10, 5, 10, 5);
        
        String[] headers = {"Thuốc thành phần", "Vị trí kệ", "Số lượng/Ngày", "Lô & HSD", "Trạng thái"};
        double[] weights = {4.0, 1.2, 1.2, 2.0, 2.0}; 
        
        for (int i = 0; i < headers.length; i++) {
            JLabel lbl = new JLabel(headers[i], i > 1 ? SwingConstants.CENTER : SwingConstants.LEFT);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lbl.setForeground(Color.decode("#475569"));
            lbl.setMinimumSize(new Dimension(60, 30)); 
            
            gbcH.gridx = i; gbcH.weightx = weights[i];
            if (i == 0) gbcH.insets = new Insets(10, 35, 10, 5); 
            else gbcH.insets = new Insets(10, 5, 10, 5);
            
            pnlHeaderTable.add(lbl, gbcH);
        }

        pnlDanhSachThuoc = new JPanel();
        pnlDanhSachThuoc.setLayout(new BoxLayout(pnlDanhSachThuoc, BoxLayout.Y_AXIS));
        pnlDanhSachThuoc.setBackground(Color.WHITE);
        
        JScrollPane scrollThuoc = new JScrollPane(pnlDanhSachThuoc);
        scrollThuoc.setBorder(BorderFactory.createLineBorder(Color.decode("#E2E8F0")));
        scrollThuoc.getVerticalScrollBar().setUnitIncrement(16);
        scrollThuoc.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel pnlTableWrapper = new JPanel(new BorderLayout());
        pnlTableWrapper.setOpaque(false);
        pnlTableWrapper.add(pnlHeaderTable, BorderLayout.NORTH);
        pnlTableWrapper.add(scrollThuoc, BorderLayout.CENTER);

        pnlCenter.add(txtSearchCenter, BorderLayout.NORTH);
        pnlCenter.add(pnlTableWrapper, BorderLayout.CENTER);

        // ===== CỘT PHẢI =====
        JPanel pnlRight = new JPanel(new BorderLayout(0, 15));
        pnlRight.setPreferredSize(new Dimension(320, 0));
        pnlRight.setBackground(Color.WHITE);
        pnlRight.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, Color.decode("#DFE3E8")),
            new EmptyBorder(20, 15, 20, 15)
        ));

        JPanel pnlHuongDan = new JPanel(new BorderLayout(0, 8));
        pnlHuongDan.setOpaque(false);
        JLabel lblHuongDan = new JLabel("HƯỚNG DẪN SỬ DỤNG (IN LÊN NHÃN)");
        lblHuongDan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblHuongDan.setForeground(Color.decode("#4B5563"));
        
        txtHuongDan = new JTextArea();
        txtHuongDan.setLineWrap(true); txtHuongDan.setWrapStyleWord(true);
        txtHuongDan.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtHuongDan.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(10, 10, 10, 10)
        ));
        pnlHuongDan.add(lblHuongDan, BorderLayout.NORTH);
        pnlHuongDan.add(txtHuongDan, BorderLayout.CENTER);

        JPanel pnlBottomTotal = new JPanel(new BorderLayout());
        pnlBottomTotal.setBackground(Color.decode("#152A4B"));
        pnlBottomTotal.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel lblTotalTitle = new JLabel("TỔNG TIỀN COMBO");
        lblTotalTitle.setForeground(Color.decode("#94A3B8"));
        lblTotalTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        lblTongTienCombo = new JLabel("0đ");
        lblTongTienCombo.setForeground(Color.WHITE);
        lblTongTienCombo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        
        lblThongTinCombo = new JLabel("5 ngày - 0 viên");
        lblThongTinCombo.setForeground(Color.decode("#94A3B8"));
        lblThongTinCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel pnlTotalText = new JPanel(new GridLayout(3, 1));
        pnlTotalText.setOpaque(false);
        pnlTotalText.add(lblTotalTitle); pnlTotalText.add(lblTongTienCombo); pnlTotalText.add(lblThongTinCombo);
        pnlBottomTotal.add(pnlTotalText, BorderLayout.CENTER);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.setOpaque(false);
        pnlActions.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton btnHuy = new JButton("Hủy");
        btnHuy.setPreferredSize(new Dimension(80, 38));
        btnHuy.setBackground(Color.WHITE);
        btnHuy.setFocusPainted(false);
        btnHuy.addActionListener(e -> this.dispose());
        
        JButton btnThem = new JButton("+ Thêm vào đơn hàng");
        btnThem.setPreferredSize(new Dimension(170, 38));
        btnThem.setBackground(Color.decode("#152A4B"));
        btnThem.setForeground(Color.WHITE);
        btnThem.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnThem.setFocusPainted(false);
        
        pnlActions.add(btnHuy); pnlActions.add(btnThem);

        JPanel pnlRightBottom = new JPanel(new BorderLayout());
        pnlRightBottom.setOpaque(false);
        pnlRightBottom.add(pnlBottomTotal, BorderLayout.CENTER);
        pnlRightBottom.add(pnlActions, BorderLayout.SOUTH);

        pnlRight.add(pnlHuongDan, BorderLayout.CENTER);
        pnlRight.add(pnlRightBottom, BorderLayout.SOUTH);

        pnlContent.add(pnlLeft, BorderLayout.WEST);
        pnlContent.add(pnlCenter, BorderLayout.CENTER);
        pnlContent.add(pnlRight, BorderLayout.EAST);
        
        pnlMainBorder.add(pnlContent, BorderLayout.CENTER);
        this.add(pnlMainBorder);

        // ===== LOGIC SỰ KIỆN NẠP DỮ LIỆU TỪ DATABASE =====
        soNgayUong = 5; 
        
        List<Entity.LieuMau> dsLieuMau = busLieuMau.getTatCaLieuMau();
        cboLieuMau.addItem("--- Chọn liều mẫu ---");
        Map<String, String> mapLieuMau = new java.util.HashMap<>();
        
        Map<String, List<Entity.LieuMau>> groupMap = new LinkedHashMap<>();
        for (Entity.LieuMau lm : dsLieuMau) {
            String nhom = (lm.getNhomBenh() != null && !lm.getNhomBenh().trim().isEmpty()) ? lm.getNhomBenh().trim() : "Nhóm khác";
            groupMap.computeIfAbsent(nhom, k -> new ArrayList<>()).add(lm);
        }

        for (Map.Entry<String, List<Entity.LieuMau>> entry : groupMap.entrySet()) {
            cboLieuMau.addItem("[" + entry.getKey().toUpperCase() + "]"); 
            for (Entity.LieuMau lm : entry.getValue()) {
                String displayName = "  ➔ " + lm.getTenLieu();
                cboLieuMau.addItem(displayName);
                mapLieuMau.put(displayName, lm.getId()); 
            }
        }

        cboLieuMau.addActionListener(e -> {
            String selected = (String) cboLieuMau.getSelectedItem();
            if (selected == null || selected.startsWith("[") || selected.startsWith("---")) {
                pnlDanhSachThuoc.removeAll();
                pnlDanhSachThuoc.revalidate(); pnlDanhSachThuoc.repaint();
                capNhatTongTienCombo();
                txtHuongDan.setText("");
                lblThanhPhan.setText("0 thành phần");
                return;
            }
            
            String idLieuMau = mapLieuMau.get(selected);
            taiDanhSachThuocCuaLieu(idLieuMau);
            
            for(Entity.LieuMau lm : dsLieuMau) {
                if(lm.getId().equals(idLieuMau)) {
                    txtHuongDan.setText(lm.getMoTa());
                    break;
                }
            }
        });

        btnPlus.addActionListener(e -> { soNgayUong++; lblDays.setText(soNgayUong + ""); capNhatTongTienCombo(); });
        btnMinus.addActionListener(e -> { if(soNgayUong>1) { soNgayUong--; lblDays.setText(soNgayUong + ""); capNhatTongTienCombo(); }});

     // ===== SỰ KIỆN GỌI VỀ FORM CHA KHI BẤM "THÊM VÀO ĐƠN HÀNG" =====
        btnThem.addActionListener(e -> {
            boolean hasChecked = false;
            boolean hasError = false;
            String unverifiedName = "";
            
            // TẠO MAP ĐỂ CHỈ LƯU NHỮNG THUỐC ĐƯỢC TÍCH CHỌN VÀ SỐ LƯỢNG MỚI NHẤT
            java.util.Map<String, Integer> mapThuocChon = new java.util.HashMap<>();

            for (RowThuocCombo row : danhSachRowThuoc) {
                if (row.chkChon.isSelected()) { // Chỉ lấy dòng có dấu tích
                    hasChecked = true;
                    mapThuocChon.put(row.sanPhamId, row.soLuongGoc); // Ghi nhận ID thuốc và số lượng
                    
                    if (!row.lblStatus.getText().equals("Đã xác thực QR")) {
                        hasError = true; unverifiedName = row.tenSP; break;
                    }
                }
            }

            if (!hasChecked) {
                JOptionPane.showMessageDialog(this, "Bạn chưa chọn thuốc nào trong liều!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return; 
            }
            if (hasError) {
                JOptionPane.showMessageDialog(this, "Thuốc [" + unverifiedName + "] chưa được quét mã QR xác thực!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return; 
            }

            if (cboLieuMau.getSelectedItem() != null) {
                String selectedItem = cboLieuMau.getSelectedItem().toString();
                if (selectedItem.contains("➔")) {
                    String tenLieu = selectedItem.replace("➔", "").trim();
                    
                    // TRUYỀN THÊM mapThuocChon VÀO HÀM (Sẽ bị báo đỏ tạm thời do chưa sửa form cha)
                    parentForm.thucThiDoThuocTuLieuVaoGio(tenLieu, soNgayUong, mapThuocChon);
                    
                    this.dispose(); // Đóng popup lại
                }
            }
        });
    }

    // ===== LỚP PHỤ TRỢ (INNER CLASS) CỦA DIALOG =====
    class RowThuocCombo {
        public JPanel pnlRow;
        public JCheckBox chkChon;
        public String sanPhamId, tenSP, donViTinh;
        public int soLuongGoc;
        public double donGia;
        public JComboBox<String> cboLo;
        public JLabel lblStatus;
        public JSpinner spnSoLuong;

        public RowThuocCombo(String id, String ten, String tacDung, int sl, String dvt, double gia, boolean isOddRow) {
            this.sanPhamId = id; this.tenSP = ten; this.donViTinh = dvt;
            this.soLuongGoc = sl; this.donGia = gia;

            pnlRow = new JPanel(new GridBagLayout());
            pnlRow.setBackground(isOddRow ? Color.WHITE : Color.decode("#F8FAFC"));
            pnlRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E2E8F0")));
            pnlRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65)); 

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH; gbc.insets = new Insets(10, 5, 10, 5);

            JPanel pnlTen = new JPanel(new BorderLayout(5, 0));
            pnlTen.setOpaque(false);
            chkChon = new JCheckBox(); chkChon.setSelected(true); chkChon.setOpaque(false);
            chkChon.addActionListener(e -> capNhatTongTienCombo()); 
            
            String htmlTen = "<html><div style='max-width: 250px;'><span style='font-weight:bold; font-size:14px; color:#111827;'>" + ten + "</span><br>"
                           + "<span style='font-size:12px; color:#6B7280;'>(" + (tacDung!=null?tacDung:"Thành phần") + ")</span></div></html>";
            JLabel lblTen = new JLabel(htmlTen);
            
            pnlTen.add(chkChon, BorderLayout.WEST); pnlTen.add(lblTen, BorderLayout.CENTER);
            gbc.gridx = 0; gbc.weightx = 4.0; pnlRow.add(pnlTen, gbc);

            JLabel lblViTri = new JLabel("<html><span style='color:#6B7280; font-size:13px;'>Tủ A<br>- Ngăn 1</span></html>", SwingConstants.CENTER);
            gbc.gridx = 1; gbc.weightx = 1.2; pnlRow.add(lblViTri, gbc);

            spnSoLuong = new JSpinner(new SpinnerNumberModel(sl, 1, 999, 1));
            spnSoLuong.setFont(new Font("Segoe UI", Font.BOLD, 14));
            spnSoLuong.addChangeListener(e -> {
                this.soLuongGoc = (int) spnSoLuong.getValue(); 
                capNhatTongTienCombo(); 
            });
            
            JPanel pnlSpinnerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            pnlSpinnerWrapper.setOpaque(false); pnlSpinnerWrapper.add(spnSoLuong);
            gbc.gridx = 2; gbc.weightx = 1.2; pnlRow.add(pnlSpinnerWrapper, gbc);

            cboLo = new JComboBox<>();
            cboLo.setBackground(Color.WHITE); cboLo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            try {
                BUS.BUS_SanPham busSP = new BUS.BUS_SanPham();
                List<Object[]> ketQua = busSP.timKiemSanPhamBan(id); 
                if (ketQua != null && !ketQua.isEmpty()) {
                    for (Object[] row : ketQua) {
                        String soLo = (row.length > 7 && row[7] != null) ? row[7].toString() : "";
                        String hsd = (row.length > 8 && row[8] != null) ? row[8].toString() : "";
                        if (!soLo.isEmpty()) cboLo.addItem(soLo + (hsd.isEmpty() ? "" : " — " + hsd));
                    }
                }
            } catch(Exception e) {}
            if (cboLo.getItemCount() == 0) cboLo.addItem("Chưa có lô");
            gbc.gridx = 3; gbc.weightx = 2.0; pnlRow.add(cboLo, gbc);

            lblStatus = new JLabel("Chưa quét", SwingConstants.CENTER);
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblStatus.setForeground(Color.decode("#94A3B8"));
            lblStatus.setBackground(Color.decode("#F1F5F9")); lblStatus.setOpaque(true);
            lblStatus.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            
            JPanel pnlStatusWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            pnlStatusWrapper.setOpaque(false); pnlStatusWrapper.add(lblStatus);
            gbc.gridx = 4; gbc.weightx = 2.0; pnlRow.add(pnlStatusWrapper, gbc);
        }
    }

    // ===== HÀM PHỤ TRỢ (HELPER) CỦA DIALOG =====
    private void taiDanhSachThuocCuaLieu(String idLieuMau) {
        pnlDanhSachThuoc.removeAll(); danhSachRowThuoc.clear();
        List<Object[]> dsChiTiet = busLieuMau.getChiTietThuocCuaLieu(idLieuMau);
        
        boolean isOdd = true;
        for (Object[] row : dsChiTiet) {
            String idSP = row[0].toString(); String tenSP = row[1].toString();
            String nhomBenh = row[2] != null ? row[2].toString() : "Thuốc";
            int sl = Integer.parseInt(row[3].toString());
            String dvt = row[4].toString(); double gia = Double.parseDouble(row[5].toString());

            RowThuocCombo rowUI = new RowThuocCombo(idSP, tenSP, nhomBenh, sl, dvt, gia, isOdd);
            danhSachRowThuoc.add(rowUI); pnlDanhSachThuoc.add(rowUI.pnlRow);
            isOdd = !isOdd;
        }
        
        if (lblThanhPhan != null) lblThanhPhan.setText(danhSachRowThuoc.size() + " thành phần");
        pnlDanhSachThuoc.revalidate(); pnlDanhSachThuoc.repaint();
        capNhatTongTienCombo();
    }

    private void capNhatTongTienCombo() {
        long tongTien = 0; int tongVien = 0;
        for (RowThuocCombo row : danhSachRowThuoc) {
            if (row.chkChon.isSelected()) {
                int slTong = row.soLuongGoc * soNgayUong;
                tongVien += slTong; tongTien += (long)(row.donGia * slTong);
            }
        }
        lblTongTienCombo.setText(String.format("%,d", tongTien).replace(',', '.') + "đ");
        lblThongTinCombo.setText(soNgayUong + " ngày - " + tongVien + " viên/gói");
    }
}