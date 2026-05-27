package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

import Entity.SanPham; 

public class DialogChonLieuMau extends JDialog {

    private TaoHoaDon parentForm;

    private JPanel pnlDanhSachThuoc;
    private JLabel lblTongTienCombo, lblThongTinCombo, lblThanhPhan;
    private int soNgayUong = 5;
    private JTextArea txtHuongDan;
    private List<RowThuocCombo> danhSachRowThuoc = new ArrayList<>();
    
    private BUS.BUS_SanPham busSanPham = new BUS.BUS_SanPham();

    public DialogChonLieuMau(TaoHoaDon parent) {
        super((Frame) SwingUtilities.getWindowAncestor(parent), "Thêm Thuốc Cắt Liều / Combo mẫu", true);
        this.parentForm = parent;

        setUndecorated(true);
        // FIX: Tăng kích thước tổng của Dialog để bảng rộng rãi
        setSize(1366, 720);
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

        JButton btnClose = new JButton(); 
        btnClose.setIcon(Utils.MenuIcon.of("CLOSE", 16, Color.WHITE));
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
        // FIX: Tăng kích thước cột trái để ComboBox thoải mái hơn
        pnlLeft.setPreferredSize(new Dimension(280, 0));
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

        JPanel pnlComboAndBtn = new JPanel(new BorderLayout(5, 0));
        pnlComboAndBtn.setOpaque(false);
        pnlComboAndBtn.add(cboLieuMau, BorderLayout.CENTER);
        // FIX: Đã xóa nút thêm liều mới khỏi giao diện

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

        JPanel pnlHuongDan = new JPanel(new BorderLayout(0, 8));
        pnlHuongDan.setOpaque(false);
        JLabel lblHuongDan = new JLabel("HƯỚNG DẪN SỬ DỤNG");
        lblHuongDan.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblHuongDan.setForeground(Color.decode("#4B5563"));
        
        txtHuongDan = new JTextArea();
        txtHuongDan.setLineWrap(true); txtHuongDan.setWrapStyleWord(true);
        txtHuongDan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane scrollHD = new JScrollPane(txtHuongDan);
        scrollHD.setPreferredSize(new Dimension(0, 150)); 
        scrollHD.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
            new EmptyBorder(5, 5, 5, 5)
        ));
        
        pnlHuongDan.add(lblHuongDan, BorderLayout.NORTH);
        pnlHuongDan.add(scrollHD, BorderLayout.CENTER);

        JPanel pnlLeftTop = new JPanel();
        pnlLeftTop.setLayout(new BoxLayout(pnlLeftTop, BoxLayout.Y_AXIS));
        pnlLeftTop.setOpaque(false);
        pnlLeftTop.add(pnlChonLieu);
        pnlLeftTop.add(Box.createRigidArea(new Dimension(0, 20)));
        pnlLeftTop.add(pnlSoNgay);
        pnlLeftTop.add(Box.createRigidArea(new Dimension(0, 20)));
        pnlLeftTop.add(pnlHuongDan); 
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
                List<Object[]> ketQua = busSanPham.timKiemSanPhamBan(qrCode);
                
                if (ketQua != null && !ketQua.isEmpty()) {
                    boolean found = false;
                    String debugIdDB = "";
                    String debugTenDB = "";
                    
                    for (Object[] kq : ketQua) {
                        String idDB = kq[0] != null ? kq[0].toString().trim() : "";
                        String tenDB = (kq.length > 1 && kq[1] != null) ? kq[1].toString().trim() : "";
                        String soLoDB = (kq.length > 7 && kq[7] != null) ? kq[7].toString().trim() : "";
                        
                        debugIdDB = idDB;
                        debugTenDB = tenDB;
                        
                        for (RowThuocCombo row : danhSachRowThuoc) {
                            if (row.sanPhamId.trim().equalsIgnoreCase(idDB) || 
                                row.tenSP.trim().equalsIgnoreCase(tenDB)) {
                                
                                row.chkChon.setSelected(true); 
                                
                                if (!soLoDB.isEmpty() && row.cboLo.getItemCount() > 0) {
                                    for (int i = 0; i < row.cboLo.getItemCount(); i++) {
                                        if (row.cboLo.getItemAt(i).startsWith(soLoDB)) {
                                            row.cboLo.setSelectedIndex(i); break;
                                        }
                                    }
                                }
                                
                                row.lblStatus.setText("Đã xác thực QR");
                                row.lblStatus.setForeground(Color.decode("#059669"));
                                row.lblStatus.setBackground(Color.decode("#D1FAE5"));
                                row.lblStatus.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(Color.decode("#34D399"), 1, true),
                                    new EmptyBorder(4, 4, 4, 4) 
                                ));
                                
                                found = true;
                                break;
                            }
                        }
                        if (found) break; 
                    }
                    
                    if (found) {
                        capNhatTongTienCombo();
                        pnlDanhSachThuoc.revalidate(); 
                        pnlDanhSachThuoc.repaint();
                    } else {
                        String mauGiaoDien = danhSachRowThuoc.isEmpty() ? "Trống" : 
                                            ("[" + danhSachRowThuoc.get(0).sanPhamId + "] " + danhSachRowThuoc.get(0).tenSP);
                        
                        Utils.ThongBao.show(this, 
                            "Cảnh báo lệch dữ liệu", 
                            "Sản phẩm quét được không khớp với danh sách Liều!\n\n" +
                            "📌 SP quét ra từ DB: [" + debugIdDB + "] " + debugTenDB + "\n" +
                            "📌 SP đang có trên UI: " + mauGiaoDien + "\n\n" +
                            "Gợi ý: Hãy kiểm tra lại xem ID hoặc Tên sản phẩm khi lưu Liều mẫu có bị sai lệch không.", 
                            "WARNING");
                    }
                } else {
                    Utils.ThongBao.show(this, "Lỗi tìm kiếm", "Mã QR / Mã vạch không hợp lệ hoặc sản phẩm này đang hết tồn kho!", "ERROR");
                }
            } catch (Exception ex) { 
                ex.printStackTrace(); 
                Utils.ThongBao.show(this, "Lỗi hệ thống", "Đã xảy ra lỗi khi quét mã: " + ex.getMessage(), "ERROR");
            }
        });
        
        JPanel pnlHeaderTable = new JPanel(new GridBagLayout());
        pnlHeaderTable.setBackground(Color.decode("#F8FAFC"));
        pnlHeaderTable.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E2E8F0")));
        GridBagConstraints gbcH = new GridBagConstraints();
        gbcH.fill = GridBagConstraints.BOTH; gbcH.insets = new Insets(10, 5, 10, 5);
        
        String[] headers = {"Thuốc thành phần", "Vị trí kệ", "Số lượng/Ngày", "Lô & HSD", "Trạng thái"};
        // FIX: Chỉnh tỷ lệ Header cân xứng
        double[] weights = {4.5, 1.0, 1.0, 1.8, 1.7}; 
        
        for (int i = 0; i < headers.length; i++) {
            JLabel lbl = new JLabel(headers[i], i > 1 ? SwingConstants.CENTER : SwingConstants.LEFT);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
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

        pnlRight.add(pnlRightBottom, BorderLayout.SOUTH);

        pnlContent.add(pnlLeft, BorderLayout.WEST);
        pnlContent.add(pnlCenter, BorderLayout.CENTER);
        pnlContent.add(pnlRight, BorderLayout.EAST);
        
        pnlMainBorder.add(pnlContent, BorderLayout.CENTER);
        this.add(pnlMainBorder);

        // ===== LOGIC SỰ KIỆN NẠP DỮ LIỆU TỪ DATABASE =====
        soNgayUong = 5; 
        
        List<Object[]> dsLieuMau = busSanPham.layDanhSachComboNangCao();
        cboLieuMau.addItem("--- Chọn liều mẫu ---");
        Map<String, String> mapLieuMau = new java.util.HashMap<>();
        
        Map<String, List<Object[]>> groupMap = new LinkedHashMap<>();
        for (Object[] r : dsLieuMau) {
            String nhom = (r[2] != null && !r[2].toString().trim().isEmpty()) ? r[2].toString().trim() : "Nhóm khác";
            groupMap.computeIfAbsent(nhom, k -> new ArrayList<>()).add(r);
        }

        for (Map.Entry<String, List<Object[]>> entry : groupMap.entrySet()) {
            cboLieuMau.addItem("[" + entry.getKey().toUpperCase() + "]"); 
            for (Object[] r : entry.getValue()) {
                String displayName = "  ➔ " + r[1].toString(); 
                cboLieuMau.addItem(displayName);
                mapLieuMau.put(displayName, r[0].toString()); 
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
        });

        btnPlus.addActionListener(e -> { soNgayUong++; lblDays.setText(soNgayUong + ""); capNhatTongTienCombo(); });
        btnMinus.addActionListener(e -> { if(soNgayUong>1) { soNgayUong--; lblDays.setText(soNgayUong + ""); capNhatTongTienCombo(); }});

        btnThem.addActionListener(e -> {
            boolean hasChecked = false;
            boolean hasError = false;
            String unverifiedName = "";
            
            java.util.Map<String, Integer> mapThuocChon = new java.util.HashMap<>();

            for (RowThuocCombo row : danhSachRowThuoc) {
                if (row.chkChon.isSelected()) { 
                    hasChecked = true;
                    mapThuocChon.put(row.sanPhamId, row.soLuongGoc); 
                    
                    if (!row.lblStatus.getText().equals("Đã xác thực QR")) {
                        hasError = true; unverifiedName = row.tenSP; break;
                    }
                }
            }

            if (!hasChecked) {
                Utils.ThongBao.show(this, "Cảnh báo", "Bạn chưa chọn thuốc nào trong liều!", "WARNING");
                return; 
            }
            if (hasError) {
                Utils.ThongBao.show(this, "Cảnh báo xác thực", "Thuốc [" + unverifiedName + "] chưa được quét mã QR xác thực!", "WARNING");
                return; 
            }

            if (cboLieuMau.getSelectedItem() != null) {
                String selectedItem = cboLieuMau.getSelectedItem().toString();
                if (selectedItem.contains("➔")) {
                    String tenLieu = selectedItem.replace("➔", "").trim();
                    
                    // --- ĐOẠN CODE THÊM MỚI LẤY HƯỚNG DẪN TỪ TEXTAREA ---
                    String huongDan = txtHuongDan.getText().trim();
                    
                    // GỌI HÀM VỚI 4 THAM SỐ THAY VÌ 3
                    parentForm.thucThiDoThuocTuLieuVaoGio(tenLieu, soNgayUong, mapThuocChon, huongDan);
                    
                    this.dispose(); 
                }
            }
        });
    }

    // ===== CÁC HÀM PHỤ TRỢ =====
    private void taiDanhSachThuocCuaLieu(String idLieuMau) {
        pnlDanhSachThuoc.removeAll(); danhSachRowThuoc.clear();
        
        // 1. Lấy dữ liệu Liều từ busSanPham để có các thông số Sáng/Trưa/Chiều/Tối
        SanPham.MauLieu mauLieu = busSanPham.layComboByIdNangCao(idLieuMau);
        
        // 2. Lấy thêm danh sách chứa VỊ TRÍ từ BUS_LieuMau mà ta đã sửa ở file DAO
        BUS.BUS_LieuMau busLieuMau = new BUS.BUS_LieuMau();
        List<Object[]> dsChiTietViTri = busLieuMau.getChiTietThuocCuaLieu(idLieuMau);
        
        if (mauLieu != null && mauLieu.getDsChiTiet() != null) {
            boolean isOdd = true;
            for (SanPham.ChiTietLieu ct : mauLieu.getDsChiTiet()) {
                String idSP = ct.getSanPhamId();
                String tenSP = ct.getTenSanPham();
                String cachDung = ct.getCachDung() != null ? ct.getCachDung() : "";
                
                int sl1Ngay = (int) (ct.getSang() + ct.getTrua() + ct.getChieu() + ct.getToi());
                if (sl1Ngay <= 0 && ct.getSoNgay() > 0) sl1Ngay = ct.getTongSoLuong() / ct.getSoNgay(); 
                if (sl1Ngay <= 0) sl1Ngay = 1;

                // --- 3. TÌM VỊ TRÍ TƯƠNG ỨNG TRONG dsChiTietViTri ---
                String viTriThuoc = "Chưa xếp vị trí";
                for (Object[] rowViTri : dsChiTietViTri) {
                    if (rowViTri[0] != null && rowViTri[0].toString().equals(idSP)) {
                        // Vị trí nằm ở cột thứ 7 (index = 6) trong Object[]
                        viTriThuoc = (rowViTri[6] != null) ? rowViTri[6].toString() : "Chưa xếp vị trí";
                        break;
                    }
                }

                // Truyền thêm viTriThuoc vào hàm khởi tạo RowThuocCombo
                RowThuocCombo rowUI = new RowThuocCombo(idSP, tenSP, cachDung, sl1Ngay, ct.getDvt(), ct.getGiaDonVi(), ct.getSang(), ct.getTrua(), ct.getChieu(), ct.getToi(), viTriThuoc, isOdd);
                danhSachRowThuoc.add(rowUI); 
                pnlDanhSachThuoc.add(rowUI.pnlRow);
                isOdd = !isOdd;
            }
        }
        
        if (lblThanhPhan != null) lblThanhPhan.setText(danhSachRowThuoc.size() + " thành phần");
        pnlDanhSachThuoc.revalidate(); pnlDanhSachThuoc.repaint();
        capNhatTongTienCombo();
        capNhatHuongDanSuDung(); 
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

    private void capNhatHuongDanSuDung() {
        StringBuilder sb = new StringBuilder();
        for (RowThuocCombo row : danhSachRowThuoc) {
            if (row.chkChon.isSelected()) { 
                sb.append("- ").append(row.tenSP).append(": ");
                
                List<String> buoi = new ArrayList<>();
                if (row.sang > 0) buoi.add("Sáng " + formatDose(row.sang));
                if (row.trua > 0) buoi.add("Trưa " + formatDose(row.trua));
                if (row.chieu > 0) buoi.add("Chiều " + formatDose(row.chieu));
                if (row.toi > 0) buoi.add("Tối " + formatDose(row.toi));

                if (!buoi.isEmpty()) {
                    sb.append(String.join(", ", buoi)).append(" ").append(row.donViTinh).append(". ");
                } else {
                    sb.append(row.soLuongGoc).append(" ").append(row.donViTinh).append("/ngày. ");
                }

                if (row.cachDung != null && !row.cachDung.trim().isEmpty() && !row.cachDung.equals("Thành phần")) {
                    sb.append("(").append(row.cachDung.trim()).append(")");
                }
                sb.append("\n");
            }
        }
        txtHuongDan.setText(sb.toString().trim());
    }

    private String formatDose(double dose) {
        if (dose == (long) dose) return String.format("%d", (long)dose); 
        else return String.format("%s", dose); 
    }

    // ===== LỚP PHỤ TRỢ (INNER CLASS) =====
    class RowThuocCombo {
        public JPanel pnlRow;
        public JCheckBox chkChon;
        public String sanPhamId, tenSP, donViTinh, cachDung;
        public int soLuongGoc;
        public double donGia, sang, trua, chieu, toi;
        public JComboBox<String> cboLo;
        public JLabel lblStatus;
        public JSpinner spnSoLuong;

        public RowThuocCombo(String id, String ten, String cachDung, int sl, String dvt, double gia, double sang, double trua, double chieu, double toi, String viTriThuoc, boolean isOddRow) {
            this.sanPhamId = id; this.tenSP = ten; this.donViTinh = dvt; this.cachDung = cachDung;
            this.soLuongGoc = sl; this.donGia = gia;
            this.sang = sang; this.trua = trua; this.chieu = chieu; this.toi = toi;

            pnlRow = new JPanel(new GridBagLayout());
            pnlRow.setBackground(isOddRow ? Color.WHITE : Color.decode("#F8FAFC"));
            pnlRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#E2E8F0")));
            pnlRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65)); 

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH; gbc.insets = new Insets(5, 5, 5, 5);

            JPanel pnlTen = new JPanel(new BorderLayout(5, 0));
            pnlTen.setOpaque(false);
            // FIX: Ngăn chặn layout bị vỡ khi tên thuốc dài
            pnlTen.setPreferredSize(new Dimension(50, 50)); 
            
            chkChon = new JCheckBox(); chkChon.setSelected(true); chkChon.setOpaque(false);
            chkChon.addActionListener(e -> {
                capNhatTongTienCombo();
                capNhatHuongDanSuDung(); 
            });
            
            // FIX: Chữ nhỏ lại, bỏ in đậm, và loại bỏ hoàn toàn phần "cách dùng" bên dưới
            String htmlTen = "<html><div style='width: 100%; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;'><span style='font-weight:normal; font-size:11px; color:#111827;'>" + ten + "</span></div></html>";
            final JLabel lblTen = new JLabel(htmlTen);
            
            pnlTen.add(chkChon, BorderLayout.WEST); pnlTen.add(lblTen, BorderLayout.CENTER);
            
            // FIX: Chỉnh lại tỷ lệ chia của GridBagLayout cho chuẩn
            gbc.gridx = 0; gbc.weightx = 4.5; pnlRow.add(pnlTen, gbc);

            String viTriHienThi = (viTriThuoc != null && !viTriThuoc.trim().isEmpty() && !viTriThuoc.equals(" - ")) ? viTriThuoc : "Chưa xếp vị trí";
            JLabel lblViTri = new JLabel("<html><span style='color:#6B7280; font-size:10px;'>" + viTriHienThi + "</span></html>", SwingConstants.CENTER);
            gbc.gridx = 1; gbc.weightx = 1.0; pnlRow.add(lblViTri, gbc);

            spnSoLuong = new JSpinner(new SpinnerNumberModel(sl, 1, 999, 1));
            spnSoLuong.setFont(new Font("Segoe UI", Font.BOLD, 12));
            spnSoLuong.addChangeListener(e -> {
                this.soLuongGoc = (int) spnSoLuong.getValue(); 
                capNhatTongTienCombo(); 
            });
            
            JPanel pnlSpinnerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            pnlSpinnerWrapper.setOpaque(false); pnlSpinnerWrapper.add(spnSoLuong);
            gbc.gridx = 2; gbc.weightx = 1.0; pnlRow.add(pnlSpinnerWrapper, gbc);
            cboLo = new JComboBox<>();
            cboLo.setBackground(Color.WHITE); cboLo.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            
            try {
                List<Object[]> ketQua = busSanPham.timKiemSanPhamBan(id); 
                if (ketQua != null && !ketQua.isEmpty()) {
                    // Cờ để đánh dấu thuốc có bị cận/hết hạn hay không
                    boolean isCanHan = false;
                    boolean isHetHan = false;
                    String hsdCanhBao = "";

                    for (Object[] row : ketQua) {
                        String soLo = (row.length > 7 && row[7] != null) ? row[7].toString() : "";
                        String hsd = (row.length > 8 && row[8] != null) ? row[8].toString() : "";
                        if (!soLo.isEmpty()) cboLo.addItem(soLo + (hsd.isEmpty() ? "" : " — " + hsd));
                    }

                    // --- BỔ SUNG LOGIC KIỂM TRA CẬN HẠN SAU KHI ĐÃ ĐỔ DỮ LIỆU VÀO COMBOBOX ---
                    if (cboLo.getItemCount() > 0) {
                        String selectedLo = cboLo.getItemAt(0); // Lấy lô tốt nhất (FEFO) đang được chọn
                        if (selectedLo.contains(" — ")) {
                            String hsd = selectedLo.split(" — ")[1].trim();
                            if (!hsd.equalsIgnoreCase("N/A")) {
                                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                java.time.LocalDate expiryDate = java.time.LocalDate.parse(hsd, formatter);
                                java.time.LocalDate today = java.time.LocalDate.now();
                                if (expiryDate.isBefore(today.plusMonths(6))) {
                                    // KHÔNG CẦN CÁC LỆNH ĐỔI MÀU CỦA CBO (Bỏ qua đoạn cboLo.setForeground...)
                                    
                                    // THAY VÀO ĐÓ, HIỆN THÔNG BÁO MẠNH MẼ ĐỂ NHÂN VIÊN PHẢI BẤM ĐÓNG
                                    if (expiryDate.isBefore(today)) {
                                        // Trường hợp thuốc đã hết hạn
                                        Utils.ThongBao.show(parentForm, "CẢNH BÁO: THUỐC HẾT HẠN", 
                                            "Thuốc [" + ten + "] trong liều này ĐÃ HẾT HẠN (HSD: " + hsd + ").\n" +
                                            "Hệ thống không cho phép bán sản phẩm này!", "ERROR");
                                    } else {
                                        // Trường hợp thuốc cận hạn (dưới 6 tháng)
                                        Utils.ThongBao.show(parentForm, "LƯU Ý: THUỐC CẬN HẠN", 
                                            "Thuốc [" + ten + "] trong liều này còn dưới 6 tháng là hết hạn (HSD: " + hsd + ").\n" +
                                            "Vui lòng kiểm tra kỹ trước khi thêm vào đơn!", "WARNING");
                                    }
                                }
                            }
                        }
                    }

                    // NẾU CÓ CẢNH BÁO -> ĐỔI MÀU GIAO DIỆN & HIỂN THỊ ICON NGUY HIỂM 
                    if (isCanHan) {
                        cboLo.setForeground(Color.decode("#DC2626")); // Chữ màu đỏ
                        cboLo.setBorder(BorderFactory.createLineBorder(Color.decode("#FCA5A5"))); // Viền đỏ nhạt

                        // Thêm tooltip báo hiệu cho bác sĩ/người bán
                        String tooltipMsg = isHetHan ? "THUỐC ĐÃ HẾT HẠN!" : "THUỐC CẬN HẠN (Dưới 6 tháng)";
                        cboLo.setToolTipText(tooltipMsg);

                        // Thêm một icon cảnh báo nhỏ kế bên Tên Thuốc để gây sự chú ý
                        String htmlTenCu = lblTen.getText();
                        String htmlTenMoi = htmlTenCu.replace("</div></html>", 
                                "<span style='color:red; font-size:14px; font-weight:bold;'> ⚠</span></div></html>");
                        lblTen.setText(htmlTenMoi);
                        
                        // Chủ động hiện một thông báo Toast nổi lên (nếu muốn)
                        if (isHetHan) {
                            Utils.ThongBao.show(parentForm, "CẢNH BÁO LIỀU MẪU", 
                                "Thuốc [" + ten + "] trong Liều này ĐÃ HẾT HẠN SỬ DỤNG (HSD: " + hsdCanhBao + ").\n" +
                                "Khuyến cáo KHÔNG bán liều này hoặc thay bằng thuốc khác!", "ERROR");
                        } else {
                            Utils.ThongBao.show(parentForm, "LƯU Ý LIỀU MẪU", 
                                "Thuốc [" + ten + "] trong Liều này SẮP HẾT HẠN (HSD: " + hsdCanhBao + ").", "WARNING");
                        }
                    }
                }
            } catch(Exception e) {}
            if (cboLo.getItemCount() == 0) cboLo.addItem("Chưa có lô");
            gbc.gridx = 3; gbc.weightx = 1.8; pnlRow.add(cboLo, gbc);

            lblStatus = new JLabel("Chưa quét", SwingConstants.CENTER);
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblStatus.setForeground(Color.decode("#94A3B8"));
            lblStatus.setBackground(Color.decode("#F1F5F9")); 
            lblStatus.setOpaque(true);

            lblStatus.setBorder(BorderFactory.createCompoundBorder(
            	    BorderFactory.createLineBorder(Color.decode("#F1F5F9"), 1, true),
            	    new EmptyBorder(4, 4, 4, 4) 
            	));

            JPanel pnlStatusWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            pnlStatusWrapper.setOpaque(false); 
            pnlStatusWrapper.add(lblStatus);
            gbc.gridx = 4; gbc.weightx = 1.7; pnlRow.add(pnlStatusWrapper, gbc);
        }
    }
}