package GUI;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

import Utils.*;

import java.awt.*;
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
    
    private JLabel lblTienThuaValue;
    private DefaultTableModel mainTableModel;
    private JTextField txtName, txtPhone;
    private String phuongThuc = "Tiền mặt";
    
    private Color darkBlue = Color.decode("#152A4B");
    private Color accentBlue = Color.decode("#1A73E8");
    private Color successGreen = Color.decode("#10B981");
    private Color orangeLogo = Color.decode("#F59E0B");
    private Color lightGray = Color.decode("#F8F9FA");
    private Color borderColor = Color.decode("#DFE3E8");
    private long tongTienMat = 0;
    private JLabel lblTotalValue;
    private int editingRow = -1;
    
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

        btnThanhToan.addActionListener(e -> {
            String khach = isCustomerLinked ? linkedTenKH : txtName.getText();
            if(khach.equals("Tên khách (bỏ trống = Khách lẻ)") || khach.trim().isEmpty()) khach = "Khách lẻ";
            String sdt = isCustomerLinked ? linkedSdtKH : txtPhone.getText().replace("Số điện thoại (tuỳ chọn)", "");
            String tongTien = lblTotalPriceValue.getText();

            if (editingModelRow != -1) {
                mainTableModel.setValueAt(maHDDangSua.replace("-TEMP", ""), editingModelRow, 0); 
                mainTableModel.setValueAt(khach, editingModelRow, 2);         
                mainTableModel.setValueAt(sdt, editingModelRow, 3);           
                mainTableModel.setValueAt(phuongThuc, editingModelRow, 4);   
                mainTableModel.setValueAt(tongTien, editingModelRow, 5);     
                mainTableModel.setValueAt("Hoàn thành", editingModelRow, 6); 
            } else {
                String maMoi = "HD-" + (System.currentTimeMillis() % 10000);
                mainTableModel.addRow(new Object[]{maMoi, "11/04/2026", khach, sdt, phuongThuc, tongTien, "Hoàn thành", "", "TPCN"});
            }
            dispose();
        });

        btnLuuNhap.addActionListener(e -> {
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
        });

        pnlRightFooter.add(btnLuuNhap);
        pnlRightFooter.add(btnThanhToan);
        pnlFooter.add(pnlRightFooter, BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private void recalculateTotals() {
        tamTinh = 0;
        for (int i = 0; i < productModel.getRowCount(); i++) {
            String valueStr = productModel.getValueAt(i, 5).toString().replaceAll("[^0-9]", "");
            tamTinh += Long.parseLong(valueStr);
        }
        vat = (long) (tamTinh * 0.05); 
        tongHoaDon = tamTinh + vat;

        if (lblSubtotalValue != null) lblSubtotalValue.setText(String.format("%,d", tamTinh).replace(',', '.') + "đ");
        if (lblVatValue != null) lblVatValue.setText("+" + String.format("%,d", vat).replace(',', '.') + "đ");
        if (lblTotalPriceValue != null) lblTotalPriceValue.setText(String.format("%,d", tongHoaDon).replace(',', '.') + "đ");
        
        capNhatTongTien(); 
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

        // --- 1. HEADER KHÁCH HÀNG ---
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
        lblLinkStatus.setIcon(new MenuIcon("CORRECT")); // Gắn icon dấu tích V
        lblLinkStatus.setIconTextGap(4); // Tạo khoảng cách giữa icon và chữ
        
        lblLinkStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLinkStatus.setForeground(Color.WHITE); // Chữ trắng -> Icon cũng sẽ tự động màu trắng
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

        // --- 2. BODY NHẬP LIỆU ---
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
        
        JButton btnUnlink = new JButton(); // Bỏ chữ X cứng đi
        btnUnlink.setIcon(new MenuIcon("CLOSE")); // Gắn icon dấu X mềm
        
        btnUnlink.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnUnlink.setForeground(Color.GRAY); // Màu xám -> Icon tự động màu xám
        btnUnlink.setContentAreaFilled(false);
        btnUnlink.setBorderPainted(false);
        btnUnlink.setFocusPainted(false);
        btnUnlink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnUnlink.addActionListener(e -> {
            isCustomerLinked = false;
            linkedTenKH = ""; linkedSdtKH = "";
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
                                long diem = Long.parseLong(diemKH.replace(".", ""));
                                lblLinkedMoney.setText("≈ " + String.format("%,d", diem * 1000).replace(',', '.') + "đ");
                            } catch (Exception ex) {
                                lblLinkedMoney.setText("≈ 0đ");
                            }
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
                return column == 2 || column == 3; 
            }
        };

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
                
                // --- THÊM 2 DÒNG NÀY ĐỂ LIÊN KẾT ICON ---
                lbl.setText(""); // Xóa ký tự emoji cũ
                lbl.setIcon(new MenuIcon("TRASH")); // Bật MenuIcon thùng rác
                
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
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = tbl.rowAtPoint(e.getPoint());
                int col = tbl.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 6) { 
                    productModel.removeRow(row);
                    recalculateTotals(); 
                    sp.revalidate(); sp.repaint();
                }
            }
        });

        // --- ĐÃ CHUYỂN ĐOẠN DROPDOWN VỀ ĐÚNG HÀM TẠO SẢN PHẨM ---
        JPopupMenu suggestionPopup = new JPopupMenu();
        suggestionPopup.setBorder(BorderFactory.createLineBorder(Color.decode("#DFE3E8")));
        suggestionPopup.setBackground(Color.WHITE);

        txtSearchProduct.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String text = txtSearchProduct.getText().trim().toLowerCase();
                
                // Ẩn bảng nếu không có chữ
                if (text.isEmpty() || text.equals(placeholderText.toLowerCase())) {
                    suggestionPopup.setVisible(false);
                    return;
                }

                suggestionPopup.removeAll();
                boolean hasResult = false;

                // --- TÌM KIẾM SẢN PHẨM TỪ DATABASE ---
                DAO_SanPham daoSP = new DAO_SanPham();
                // Khai báo java.util.List để tránh nhầm với java.awt.List
                java.util.List<Object[]> ketQua = daoSP.timKiemSanPhamBan(text);

                if (ketQua != null && !ketQua.isEmpty()) {
                    for (Object[] row : ketQua) {
                        String id = row[0].toString();
                        String ten = row[1].toString();
                        String donVi = row[2].toString();
                        String gia = row[3].toString();
                        String tonKho = row[4].toString();

                        // Thêm từng sản phẩm tìm được vào Popup Gợi ý
                        // Tham số: Popup, TextBox, Icon mặc định ("PILL"), Tên, ĐVT, Giá, Tồn kho
                        suggestionPopup.add(createSuggestionItem(suggestionPopup, txtSearchProduct, "PILL", ten, donVi, gia, tonKho));
                    }
                    hasResult = true;
                }

                if (hasResult) {
                    // Show bảng đổ xuống với kích thước ôm sát thanh tìm kiếm
                    suggestionPopup.setPreferredSize(new Dimension(pnlSearchWrapper.getWidth(), suggestionPopup.getPreferredSize().height));
                    suggestionPopup.show(pnlSearchWrapper, 0, pnlSearchWrapper.getHeight());
                    txtSearchProduct.requestFocus(); 
                } else {
                    // Nếu không tìm thấy, có thể hiện một thông báo nhỏ hoặc ẩn đi
                    JMenuItem emptyItem = new JMenuItem("Không tìm thấy sản phẩm nào phù hợp...");
                    emptyItem.setEnabled(false);
                    suggestionPopup.add(emptyItem);
                    suggestionPopup.setPreferredSize(new Dimension(pnlSearchWrapper.getWidth(), suggestionPopup.getPreferredSize().height));
                    suggestionPopup.show(pnlSearchWrapper, 0, pnlSearchWrapper.getHeight());
                    txtSearchProduct.requestFocus();
                }
            }
        });
        // --------------------------------------------------------

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

        JPanel pnlFinal = new JPanel(new GridLayout(3, 2, 10, 5));
        pnlFinal.setBackground(darkBlue);
        pnlFinal.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        pnlFinal.add(createWhiteLabel("Tạm tính:")); 
        lblSubtotalValue = createWhiteLabel("0đ", SwingConstants.RIGHT);
        pnlFinal.add(lblSubtotalValue);

        pnlFinal.add(createWhiteLabel("VAT (5%):")); 
        lblVatValue = createWhiteLabel("+0đ", SwingConstants.RIGHT);
        pnlFinal.add(lblVatValue);
        
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
            new EmptyBorder(8, 10, 8, 10) // Nới lỏng padding cho ô to đẹp hơn
        ));
        
        // Biến toàn bộ ô thành nút bấm (hiện con trỏ bàn tay)
        pnl.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblValue = new JLabel(labelText);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblValue.setForeground(Color.decode(hexColor));

        // Chỉ giữ lại con số đếm số lượng, bỏ hẳn chữ + và -
        JLabel lblCount = new JLabel("0"); 
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCount.setForeground(Color.decode("#111827"));
        
        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlRight.setOpaque(false);
        pnlRight.add(lblCount);

        final int[] count = {0}; 

        // --- XỬ LÝ SỰ KIỆN CHUỘT TRÁI / PHẢI ---
        java.awt.event.MouseAdapter clickAdapter = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    // CLICK CHUỘT TRÁI -> CỘNG
                    count[0]++;
                    lblCount.setText(String.valueOf(count[0]));
                    tongTienMat += faceValue;
                    capNhatTongTien();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    // CLICK CHUỘT PHẢI -> TRỪ
                    if (count[0] > 0) { 
                        count[0]--;
                        lblCount.setText(String.valueOf(count[0]));
                        tongTienMat -= faceValue;
                        capNhatTongTien(); 
                    }
                }
            }
        };

        // Gắn sự kiện click cho toàn bộ Panel và các thành phần con (để click góc nào cũng nhận)
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

        // LEFT: Icon + Tên + ĐVT
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

        // RIGHT: Đơn giá + Tồn kho
        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlRight.setOpaque(false);

        JLabel lblPrice = new JLabel(String.format("%,d", Integer.parseInt(price)).replace(',', '.') + "đ");
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPrice.setForeground(Color.decode("#111827"));

        JLabel lblStock = new JLabel("Tồn: " + stock);
        lblStock.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblStock.setForeground(Color.decode("#059669")); 

        pnlRight.add(lblPrice);
        pnlRight.add(lblStock);

        pnl.add(pnlLeft, BorderLayout.WEST);
        pnl.add(pnlRight, BorderLayout.EAST);

        // HOVER VÀ CLICK EVENT TỰ ĐỘNG THÊM VÀO BẢNG
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
                productModel.addRow(new Object[]{
                    // Đổi "🗑" thành "" ở cuối cùng
                    name, unit, "1", price, "5%", String.format("%,d", Integer.parseInt(price)).replace(',', '.') + "đ", ""
                });
                
                popup.setVisible(false);
                txtSearch.setText("");
                txtSearch.requestFocus();
                recalculateTotals(); 
            }
        });

        return pnl;
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