package GUI;

import BUS.BUS_SanPham;
import Entity.LoHang;
import Entity.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ManHinhSanPham extends JPanel {

    private BUS_SanPham busSanPham = new BUS_SanPham();

    private JTable tblSanPham, tblDonVi, tblLoHang;
    private DefaultTableModel modelSanPham, modelDonVi, modelLoHang;
    private JTextField txtTimKiem;
    
    private JTextField txtTen, txtMa, txtMaVach, txtVietTat, txtLoaiCT;
    private JTextField txtDang, txtHoatChat, txtNhaSX, txtHamLuong, txtVAT, txtDVT;
    private JTextArea txtMoTa;

    // Bảng màu chuẩn theo ảnh thiết kế
    private final Color COLOR_PRIMARY = Color.decode("#1E3A8A"); 
    private final Color COLOR_HEADER_TABLE = Color.decode("#2D435E"); 
    private final Color COLOR_BORDER = Color.decode("#DFE3E8"); 
    private final Color COLOR_LIGHT_BLUE = Color.decode("#E0F2FE"); 
    
    private final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);

    public ManHinhSanPham() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 1. TOPBAR
        add(createTopbar(), BorderLayout.NORTH);

        // Khung chứa Body để các phần sát vào nhau
        JPanel pnlBody = new JPanel(new BorderLayout());
        pnlBody.setBackground(Color.WHITE);
        
        // 2. SIDEBAR
        pnlBody.add(createSidebarFilter(), BorderLayout.WEST);

        // 3. CENTER
        pnlBody.add(createCenterList(), BorderLayout.CENTER);

        // 4. RIGHT
        pnlBody.add(createRightDetail(), BorderLayout.EAST);

        add(pnlBody, BorderLayout.CENTER);

        taiDuLieuSanPham();
    }

    // ========================================================================
    // 1. THANH TOPBAR
    // ========================================================================
    private JPanel createTopbar() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
            new EmptyBorder(10, 20, 10, 20)
        ));

        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlLeft.setOpaque(false);
        pnlLeft.add(new JLabel("<html><font size='5' color='#1E3A8A'>📦</font></html>"));
        pnlLeft.add(new JLabel("<html><b style='color:#1E3A8A; font-size:16px;'>QUẢN LÝ SẢN PHẨM</b></html>"));
        JLabel lblCount = new JLabel("(39 sản phẩm)");
        lblCount.setForeground(Color.GRAY);
        pnlLeft.add(lblCount);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlRight.setOpaque(false);
        
        txtTimKiem = new JTextField("Tên, mã, hoạt chất...");
        txtTimKiem.setPreferredSize(new Dimension(200, 35));
        txtTimKiem.setForeground(Color.GRAY);
        
        pnlRight.add(new JLabel("Tìm kiếm: "));
        pnlRight.add(txtTimKiem);
        pnlRight.add(createBtn("🔍 Tìm", "#1D68B2"));
        pnlRight.add(createBtn("↻ Làm mới", "#64748B"));
        pnlRight.add(createBtn("📥 Xuất Excel", "#22C55E"));
        pnlRight.add(createBtn("📤 Nhập Excel", "#0EA5E9"));
        pnlRight.add(createBtn("+ Thêm mới", "#E11D48"));

        pnl.add(pnlLeft, BorderLayout.WEST);
        pnl.add(pnlRight, BorderLayout.EAST);
        return pnl;
    }

    // ========================================================================
    // 2. SIDEBAR LỌC
    // ========================================================================
    private JPanel createSidebarFilter() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setPreferredSize(new Dimension(200, 0));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, COLOR_BORDER));

        pnl.add(createLabelFilter("Loại"));
        pnl.add(createRadioGroup(new String[]{"Tất cả", "Thuốc kê đơn", "Thuốc không kê đơn", "Sản phẩm chức năng"}));
        
        pnl.add(Box.createVerticalStrut(15));
        
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(180, 1));
        sep.setForeground(COLOR_BORDER);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.add(sep);
        
        pnl.add(Box.createVerticalStrut(10));
        pnl.add(createLabelFilter("Dạng bào chế"));
        pnl.add(createRadioGroup(new String[]{"Tất cả", "Viên nén", "Viên nang", "Viên sủi", "Thuốc bột", "Kẹo ngậm", "Dung dịch", "Hỗn dịch", "Thuốc nhỏ giọt", "Súc miệng"}));
        
        return pnl;
    }

    private JPanel createRadioGroup(String[] options) {
        JPanel p = new JPanel(); 
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); 
        p.setBackground(Color.WHITE);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        ButtonGroup group = new ButtonGroup();
        
        for (int i = 0; i < options.length; i++) {
            JRadioButton rb = new JRadioButton(options[i]); 
            rb.setBackground(Color.WHITE);
            rb.setFont(FONT_NORMAL); 
            rb.setBorder(new EmptyBorder(5, 15, 5, 0));
            rb.setFocusPainted(false);
            
            if(i == 0) {
                rb.setSelected(true);
                rb.setForeground(Color.decode("#1D68B2"));
                rb.setFont(FONT_BOLD);
            }
            
            rb.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    rb.setForeground(Color.decode("#1D68B2"));
                    rb.setFont(FONT_BOLD);
                } else {
                    rb.setForeground(Color.BLACK);
                    rb.setFont(FONT_NORMAL);
                }
            });
            
            group.add(rb); 
            p.add(rb);
        }
        return p;
    }

    // ========================================================================
    // 3. BẢNG DANH SÁCH SẢN PHẨM Ở GIỮA
    // ========================================================================
    private JPanel createCenterList() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(null);

        JLabel lblHeader = new JLabel("  Danh sách sản phẩm");
        lblHeader.setOpaque(true);
        lblHeader.setBackground(COLOR_HEADER_TABLE);
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(FONT_BOLD);
        lblHeader.setPreferredSize(new Dimension(0, 38));
        pnl.add(lblHeader, BorderLayout.NORTH);

        modelSanPham = new DefaultTableModel(new String[]{"Mã", "Tên", "Loại", "Hoạt chất"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblSanPham = new JTable(modelSanPham);
        setupTableStyle(tblSanPham); 
        
        tblSanPham.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(tblSanPham.getSelectedRow() != -1) {
                    String ma = tblSanPham.getValueAt(tblSanPham.getSelectedRow(), 0).toString();
                    hienThiChiTietSanPham(ma);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblSanPham);
        scroll.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        pnl.add(scroll, BorderLayout.CENTER);

        JPanel pnlPage = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        pnlPage.setBackground(Color.WHITE);
        
        JButton btnPrev = new JButton("< Trang trước"); 
        btnPrev.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnPrev.setBackground(Color.WHITE); btnPrev.setFocusPainted(false);
        btnPrev.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(5, 10, 5, 10)));
        
        JButton btnNext = new JButton("Trang tiếp >"); 
        btnNext.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnNext.setBackground(Color.WHITE); btnNext.setFocusPainted(false);
        btnNext.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(5, 10, 5, 10)));
        
        JComboBox<String> cbLimit = new JComboBox<>(new String[]{"10", "20", "50"}); 
        cbLimit.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbLimit.setBackground(Color.WHITE);
        
        JLabel lblPage = new JLabel("Trang 1/4 (Tổng: 39)");
        lblPage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        pnlPage.add(btnPrev);
        pnlPage.add(btnNext);
        pnlPage.add(cbLimit);
        pnlPage.add(lblPage);
        
        pnl.add(pnlPage, BorderLayout.SOUTH);

        return pnl;
    }

    // ========================================================================
    // 4. CHI TIẾT SẢN PHẨM (BÊN PHẢI)
    // ========================================================================
    private JPanel createRightDetail() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setPreferredSize(new Dimension(430, 0));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, COLOR_BORDER));

        // Header Chi tiết
        JPanel pnlH = new JPanel(new BorderLayout());
        pnlH.setBackground(COLOR_HEADER_TABLE);
        pnlH.setPreferredSize(new Dimension(0, 38));
        
        JLabel lblCT = new JLabel("  Chi tiết sản phẩm");
        lblCT.setForeground(Color.WHITE); 
        lblCT.setFont(FONT_BOLD);
        pnlH.add(lblCT, BorderLayout.WEST);
        
        // Đã fix lỗi LookAndFeel đè màu cho nút Sửa
        JButton btnSua = createBtn("✎ Sửa", "#F59E0B");
        btnSua.setPreferredSize(new Dimension(80, 28)); // Kích thước nhỏ gọn cho Header
        
        JPanel pnlBtnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlBtnWrap.setOpaque(false);
        pnlBtnWrap.add(btnSua);
        pnlH.add(pnlBtnWrap, BorderLayout.EAST);
        
        pnl.add(pnlH, BorderLayout.NORTH);

        JPanel pnlScrollContent = new JPanel();
        pnlScrollContent.setLayout(new BoxLayout(pnlScrollContent, BoxLayout.Y_AXIS));
        pnlScrollContent.setBackground(Color.WHITE);

        // ---- A. FORM ĐIỀN THÔNG TIN ----
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(10, 15, 10, 15));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(3, 5, 3, 5); g.weightx = 1.0;

        addFormField(pnlForm, "Tên", txtTen = new JTextField(), g, 0, 0, 2);
        addFormField(pnlForm, "Mã", txtMa = new JTextField(), g, 0, 2, 1);
        addFormField(pnlForm, "Mã vạch", txtMaVach = new JTextField(), g, 1, 2, 1);
        
        txtVietTat = new JTextField(); txtVietTat.setForeground(Color.decode("#2179E0")); txtVietTat.setBackground(Color.decode("#F0F7FF"));
        addFormField(pnlForm, "Tên viết tắt", txtVietTat, g, 0, 4, 1);
        txtLoaiCT = new JTextField(); txtLoaiCT.setForeground(Color.decode("#10B981"));
        addFormField(pnlForm, "Loại", txtLoaiCT, g, 1, 4, 1);

        addFormField(pnlForm, "Dạng", txtDang = new JTextField(), g, 0, 6, 1);
        addFormField(pnlForm, "Hoạt chất", txtHoatChat = new JTextField(), g, 1, 6, 1);
        addFormField(pnlForm, "Nhà sản xuất", txtNhaSX = new JTextField(), g, 0, 8, 1);
        addFormField(pnlForm, "Hàm lượng", txtHamLuong = new JTextField(), g, 1, 8, 1);
        addFormField(pnlForm, "VAT (%)", txtVAT = new JTextField(), g, 0, 10, 1);
        addFormField(pnlForm, "ĐVT gốc", txtDVT = new JTextField(), g, 1, 10, 1);

        g.gridx = 0; g.gridy = 12; g.gridwidth = 2;
        JLabel lblMoTa = new JLabel("Mô tả"); lblMoTa.setFont(FONT_BOLD); lblMoTa.setForeground(Color.decode("#64748B"));
        pnlForm.add(lblMoTa, g);
        g.gridy++; txtMoTa = new JTextArea(3, 20); txtMoTa.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        txtMoTa.setFont(FONT_NORMAL);
        pnlForm.add(new JScrollPane(txtMoTa), g);

        pnlScrollContent.add(pnlForm);

        // ---- B. BẢNG ĐƠN VỊ QUY ĐỔI ----
        JPanel pnlDonVi = createSubTable("Đơn vị quy đổi", new String[]{"Mã ĐV", "Tên ĐV", "Quy đổi"});
        
        // Bổ sung 2 dòng này để lấy DataModel ra gán vào biến modelDonVi
        tblDonVi = (JTable) ((JScrollPane) pnlDonVi.getComponent(1)).getViewport().getView();
        modelDonVi = (DefaultTableModel) tblDonVi.getModel();
        
        pnlScrollContent.add(pnlDonVi);
        
        // ---- C. BẢNG LÔ HÀNG ----
        JPanel pnlLot = createSubTable("Lô & hạn sử dụng", new String[]{"Mã lô", "SL", "Giá", "HSD", "TT"});
        tblLoHang = (JTable) ((JScrollPane) pnlLot.getComponent(1)).getViewport().getView();
        modelLoHang = (DefaultTableModel) tblLoHang.getModel();
        
        tblLoHang.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(value != null ? value.toString() : "", SwingConstants.CENTER);
                lbl.setOpaque(true); lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lbl.setBorder(new EmptyBorder(2, 6, 2, 6));
                if ("Được bán".equals(value)) {
                    lbl.setBackground(Color.decode("#DCFCE7")); lbl.setForeground(Color.decode("#15803D"));
                }
                JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
                wrap.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                wrap.add(lbl);
                return wrap;
            }
        });
        pnlScrollContent.add(pnlLot);

        JScrollPane mainScroll = new JScrollPane(pnlScrollContent);
        mainScroll.setBorder(null); mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        pnl.add(mainScroll, BorderLayout.CENTER);

        // Bottom Action
        JPanel pnlAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlAction.setBackground(Color.WHITE);
        pnlAction.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));
        
        JButton btnCapNhat = createBtn("✎ Cập nhật", "#1D68B2");
        btnCapNhat.setPreferredSize(new Dimension(120, 36)); 
        
        JButton btnXoa = createBtn("🗑 Xóa", "#EF4444");
        btnXoa.setPreferredSize(new Dimension(90, 36));
        
        pnlAction.add(btnCapNhat);
        pnlAction.add(btnXoa);
        pnl.add(pnlAction, BorderLayout.SOUTH);

        return pnl;
    }

    // ========================================================================
    // CÁC HÀM HỖ TRỢ XÂY DỰNG GIAO DIỆN
    // ========================================================================
    private JButton createBtn(String text, String hexColor) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.decode(hexColor));
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BOLD);
        
        // CÁC THUỘC TÍNH NÀY SẼ FIX LỖI NÚT "SỬA" BỊ TRẮNG/VIỀN LẠ
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true); // Quan trọng: Ép Java tô màu nền 100%
        
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 36));
        return btn;
    }

    private JLabel createLabelFilter(String text) {
        JLabel lbl = new JLabel(text); lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setBorder(new EmptyBorder(10, 15, 5, 0)); 
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void addFormField(JPanel p, String label, JTextField txt, GridBagConstraints g, int x, int y, int w) {
        g.gridx = x; g.gridy = y; g.gridwidth = w;
        JLabel lbl = new JLabel(label); lbl.setFont(new Font("Segoe UI", Font.BOLD, 12)); lbl.setForeground(Color.decode("#64748B"));
        p.add(lbl, g);
        g.gridy++; txt.setPreferredSize(new Dimension(0, 32));
        txt.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_BORDER), new EmptyBorder(0, 8, 0, 8)));
        p.add(txt, g);
    }

    private JPanel createSubTable(String title, String[] cols) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(10, 15, 10, 15)); p.setOpaque(false);
        
        JLabel lbl = new JLabel("  " + title); 
        lbl.setOpaque(true); 
        lbl.setBackground(COLOR_HEADER_TABLE); 
        lbl.setForeground(Color.WHITE); 
        lbl.setFont(FONT_BOLD); 
        lbl.setPreferredSize(new Dimension(0, 30));
        
        JTable t = new JTable(new DefaultTableModel(cols, 0));
        setupTableStyle(t); 
        
        JScrollPane sp = new JScrollPane(t); sp.setPreferredSize(new Dimension(0, 130)); sp.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        p.add(lbl, BorderLayout.NORTH); p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private void setupTableStyle(JTable t) {
        t.setRowHeight(35); 
        t.setFont(FONT_NORMAL); 
        t.setShowVerticalLines(false); 
        t.setGridColor(COLOR_BORDER);
        t.setSelectionBackground(Color.decode("#E0F2FE")); 
        t.setSelectionForeground(Color.BLACK);
        
        JTableHeader h = t.getTableHeader(); 
        h.setBackground(COLOR_LIGHT_BLUE); 
        h.setForeground(Color.BLACK); 
        h.setFont(FONT_BOLD);
        h.setPreferredSize(new Dimension(0, 35));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
    }

    // ========================================================================
    // LOGIC TẢI DỮ LIỆU
    // ========================================================================
    private void taiDuLieuSanPham() {
        modelSanPham.setRowCount(0);
        List<SanPham> ds = busSanPham.traCuuSanPham("");
        for (SanPham sp : ds) {
            modelSanPham.addRow(new Object[]{ sp.getId(), sp.getTen(), "Sản phẩm chức năng", sp.getHoatChat() });
        }
    }

    private void hienThiChiTietSanPham(String maSP) {
        txtMa.setText(maSP);
        txtTen.setText("Siro tăng sức đề kháng");
        txtMaVach.setText("8936079260055");
        txtVietTat.setText("Siro ĐK");
        txtLoaiCT.setText("Sản phẩm chức năng");
        txtDang.setText("Dung dịch");
        txtHoatChat.setText("Various");
        txtNhaSX.setText("Traphaco");
        txtHamLuong.setText("100ml");
        txtVAT.setText("10");
        txtDVT.setText("Chai");
        txtMoTa.setText("Bổ sung Vitamin C, tăng cường sức đề kháng");

        modelDonVi.setRowCount(0);
        modelDonVi.addRow(new Object[]{"T01", "Tuýp", "0.067"});
        modelDonVi.addRow(new Object[]{"H01", "Hộp", "1.000"});

        modelLoHang.setRowCount(0);
        modelLoHang.addRow(new Object[]{"VC001-2023", "2.000", "4.500", "31/12/2025", "Được bán"});
        modelLoHang.addRow(new Object[]{"VC002-2023", "1.500", "4.800", "30/06/2026", "Được bán"});
    }
}