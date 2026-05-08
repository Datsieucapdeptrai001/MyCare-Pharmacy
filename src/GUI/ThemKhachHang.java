package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import Utils.MenuIcon;
import java.awt.*;
import java.awt.event.*;

public class ThemKhachHang extends JDialog {

    private JTextField txtHoTen, txtNgayTao, txtSdt; 
    private JComboBox<String> cboGioiTinh;
    private DefaultTableModel mainModel;
    
    private JLabel lblTitle;
    private JButton btnThem;
    private int editRow = -1; 

    public ThemKhachHang(Frame parent, DefaultTableModel model) {
        super(parent, "Thêm khách hàng mới", true);
        this.mainModel = model;
        initUI(parent);
    }

    public ThemKhachHang(Frame parent, DefaultTableModel model, int editRow, String hoten, String sdt, String ngayTao) {
        super(parent, "Chỉnh sửa khách hàng", true);
        this.mainModel = model;
        this.editRow = editRow;
        initUI(parent);

        lblTitle.setText("Chỉnh sửa khách hàng");
        btnThem.setText("Lưu thay đổi");
        
        txtHoTen.setText(hoten);
        txtHoTen.setForeground(Color.BLACK);
        
        txtSdt.setText(sdt);
        txtSdt.setForeground(Color.BLACK);
        
        if(ngayTao != null && !ngayTao.isEmpty()){
            txtNgayTao.setText(ngayTao);
            txtNgayTao.setForeground(Color.BLACK);
        }
    }

    private void initUI(Frame parent) {
        setSize(550, 400); // Đã thu nhỏ chiều cao vì bớt đi 2 trường nhập liệu
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setLayout(new BorderLayout());

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.decode("#152A4B")); 
        pnlHeader.setBorder(new EmptyBorder(12, 15, 12, 15));

        lblTitle = new JLabel("Thêm khách hàng mới");
        lblTitle.setIcon(new MenuIcon("USER")); 
        lblTitle.setIconTextGap(10);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JButton btnClose = new JButton("X");
        btnClose.setForeground(Color.WHITE);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(btnClose, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        JPanel pnlBody = new JPanel(new GridBagLayout());
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(10, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5); 

        // Row 0: Nhập họ tên
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        pnlBody.add(createLabel("Họ và tên", true), gbc);
        
        gbc.gridy = 1;
        txtHoTen = createTextField("Nhập họ và tên đầy đủ");
        pnlBody.add(txtHoTen, gbc);

        // Row 1: Giới tính & Ngày tạo
        gbc.gridwidth = 1; gbc.weightx = 0.5;
        gbc.gridx = 0; gbc.gridy = 2;
        pnlBody.add(createLabel("Giới tính", false), gbc);
        gbc.gridx = 1;
        pnlBody.add(createLabel("Ngày tạo", false), gbc); 

        gbc.gridx = 0; gbc.gridy = 3;
        cboGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        cboGioiTinh.setBackground(Color.WHITE);
        cboGioiTinh.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboGioiTinh.setPreferredSize(new Dimension(0, 36));
        pnlBody.add(cboGioiTinh, gbc);

        gbc.gridx = 1;
        txtNgayTao = createTextField("dd/MM/yyyy");
        txtNgayTao.setEditable(false); 
        txtNgayTao.setBackground(Color.decode("#F8F9FA")); 
        
        if (editRow == -1) {
            String homNay = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            txtNgayTao.setText(homNay);
            txtNgayTao.setForeground(Color.BLACK);
        }
        pnlBody.add(txtNgayTao, gbc);

        // Row 2: Số điện thoại (Kéo dài hết 2 cột)
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        pnlBody.add(createLabel("Số điện thoại", true), gbc);

        gbc.gridy = 5;
        txtSdt = createTextField("0912345678");
        pnlBody.add(txtSdt, gbc);

        add(pnlBody, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#DFE3E8")));

        JButton btnHuy = new JButton("× Hủy");
        btnHuy.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnHuy.setBackground(Color.WHITE);
        btnHuy.setForeground(Color.decode("#374151"));
        btnHuy.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        btnHuy.setFocusPainted(false);
        btnHuy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHuy.addActionListener(e -> dispose());

        btnThem = new JButton("Thêm khách hàng");
        btnThem.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnThem.setBackground(Color.decode("#DC2626")); 
        btnThem.setForeground(Color.WHITE);
        
        if (editRow != -1) {
            btnThem.setText("Lưu thay đổi");
            btnThem.setIcon(new MenuIcon("SAVE")); 
        } else {
            btnThem.setText("Thêm khách hàng");
            btnThem.setIcon(new MenuIcon("USER_ADD")); 
        }
        
        btnThem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DC2626"), 1, true),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        btnThem.setIconTextGap(10);
        btnThem.setFocusPainted(false);
        btnThem.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // LOGIC LƯU DỮ LIỆU
        btnThem.addActionListener(e -> {
            String hoten = txtHoTen.getText().trim();
            String sdt = txtSdt.getText().trim();
            String ngayTao = txtNgayTao.getText().trim();

            if (hoten.isEmpty() || hoten.equals("Nhập họ và tên đầy đủ")) {
                showCustomNotification("LỖI NHẬP LIỆU", "Vui lòng nhập họ và tên khách hàng!", "ERROR");
                txtHoTen.requestFocusInWindow();
                return;
            }
            if (!hoten.matches("^[\\p{L}\\s]+$")) {
                showCustomNotification("LỖI ĐỊNH DẠNG", "Họ tên không hợp lệ!\n(Không được chứa số hoặc ký tự đặc biệt)", "ERROR");
                txtHoTen.requestFocusInWindow();
                return;
            }

            if (sdt.isEmpty() || sdt.equals("0912345678")) {
                showCustomNotification("LỖI NHẬP LIỆU", "Vui lòng nhập số điện thoại!", "ERROR");
                txtSdt.requestFocusInWindow();
                return;
            }
            if (!sdt.matches("^0\\d{9}$")) {
                showCustomNotification("LỖI ĐỊNH DẠNG", "Số điện thoại không hợp lệ!\n(Phải gồm đúng 10 chữ số và bắt đầu bằng số 0)", "ERROR");
                txtSdt.requestFocusInWindow();
                return;
            }

            BUS.BUS_KhachHang busKH = new BUS.BUS_KhachHang();
            Entity.KhachHang khCheck = busKH.getKhachHangTheoSDT(sdt);
            
            if (editRow != -1) {
                String maKHDangSua = mainModel.getValueAt(editRow, 0).toString();
                if (khCheck != null && !khCheck.getId().equals(maKHDangSua)) {
                    showCustomNotification("TRÙNG DỮ LIỆU", "Số điện thoại này đã được sử dụng bởi khách hàng khác!\nVui lòng nhập số khác.", "ERROR");
                    txtSdt.requestFocusInWindow(); 
                    return;
                }
            } else {
                if (khCheck != null) {
                    showCustomNotification("TRÙNG DỮ LIỆU", "Số điện thoại này đã tồn tại trong hệ thống!\nVui lòng kiểm tra lại.", "ERROR");
                    txtSdt.requestFocusInWindow(); 
                    return;
                }
            }

            if (mainModel != null) {
                if (editRow != -1) {
                    String maKH = mainModel.getValueAt(editRow, 0).toString();
                    
                    Entity.KhachHang khUpdate = busKH.timKhachHangTheoMa(maKH);
                    if (khUpdate == null) {
                        khUpdate = new Entity.KhachHang();
                        khUpdate.setId(maKH);
                        khUpdate.setDiemTichLuy(0);
                    }
                    khUpdate.setHoVaTen(hoten);
                    khUpdate.setSdt(sdt);
                    
                    boolean isUpdated = busKH.capNhatKhachHang(khUpdate);
                    
                    if (isUpdated) {
                        mainModel.setValueAt(hoten, editRow, 1);
                        mainModel.setValueAt(sdt, editRow, 2);
                        showCustomNotification("THÀNH CÔNG", "Cập nhật thông tin khách hàng thành công!", "SUCCESS");
                    } else {
                        showCustomNotification("LỖI HỆ THỐNG", "Có lỗi xảy ra khi cập nhật vào Cơ sở dữ liệu!", "ERROR");
                        return;
                    }
                    
                } else {
                    String maMoi = busKH.phatSinhMaKHTiepTheo();
                    Entity.KhachHang kh = new Entity.KhachHang();
                    kh.setId(maMoi);
                    kh.setHoVaTen(hoten);
                    kh.setSdt(sdt);
                    kh.setDiemTichLuy(0); 
                    
                    boolean isSuccess = busKH.themKhachHang(kh);

                    if (isSuccess) {
                        mainModel.addRow(new Object[]{ maMoi, hoten, sdt, "0", "0đ", "0", ngayTao, "" });
                        
                        try {
                            if (GUI.ManHinhBanHang.pendingPhoneToLink != null || GUI.ManHinhBanHang.pendingDraftIdToOpen != null) {
                                showCustomNotification("HƯỚNG DẪN", 
                                    "Đã thêm khách hàng thành công!\n\n" +
                                    "👉 Hãy bấm quay lại tab 'Bán Hàng', hệ thống sẽ tự động mở lại hóa đơn và liên kết khách hàng này cho bạn.", 
                                    "SUCCESS");
                            } else {
                                showCustomNotification("THÀNH CÔNG", "Thêm khách hàng thành công!", "SUCCESS");
                            }
                        } catch (Exception ex) {
                            showCustomNotification("THÀNH CÔNG", "Thêm khách hàng thành công!", "SUCCESS");
                        }
                    } else {
                        showCustomNotification("LỖI HỆ THỐNG", "Có lỗi xảy ra khi lưu vào Cơ sở dữ liệu!", "ERROR");
                        return; 
                    }
                }
            }
            dispose(); 
        });

        pnlFooter.add(btnHuy);
        pnlFooter.add(btnThem);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text, boolean isRequired) {
        String html = "<html><span style='color:#374151; font-family:Segoe UI; font-size:13px; font-weight:bold;'>" + text + "</span>";
        if (isRequired) html += " <span style='color:#DC2626;'>*</span>";
        html += "</html>";
        return new JLabel(html);
    }

    private JTextField createTextField(String placeholder) {
        JTextField txt = new JTextField(placeholder);
        txt.setPreferredSize(new Dimension(0, 36));
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setForeground(Color.GRAY);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8"), 1, true),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        txt.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txt.getText().equals(placeholder)) { txt.setText(""); txt.setForeground(Color.BLACK); }
            }
            public void focusLost(FocusEvent e) {
                if (txt.getText().isEmpty()) { txt.setForeground(Color.GRAY); txt.setText(placeholder); }
            }
        });
        return txt;
    }

    private void showCustomNotification(String titleText, String message, String type) {
        JDialog dialog = new JDialog(this, true); 
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createLineBorder(Color.decode("#1E3A8A"), 2));
        pnlMain.setBackground(Color.WHITE);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.decode("#1E3A8A"));
        pnlHeader.setPreferredSize(new Dimension(0, 40));
        JLabel lblTitle = new JLabel(titleText.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.CENTER);

        JPanel pnlBody = new JPanel(new BorderLayout(15, 0));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel pnlIcon = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color mainColor = type.equals("ERROR") ? Color.decode("#EF4444") : 
                                  (type.equals("SUCCESS") ? Color.decode("#10B981") : Color.decode("#F59E0B"));
                Color bgColor = type.equals("ERROR") ? Color.decode("#FEE2E2") : 
                                (type.equals("SUCCESS") ? Color.decode("#D1FAE5") : Color.decode("#FEF3C7"));
                
                g2.setColor(bgColor);
                g2.fillOval(0, 0, 50, 50);
                g2.setColor(mainColor);
                g2.setStroke(new java.awt.BasicStroke(3f));
                g2.drawOval(0, 0, 50, 50);
                
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                FontMetrics fm = g2.getFontMetrics();
                String symbol = type.equals("ERROR") ? "X" : (type.equals("SUCCESS") ? "V" : "!");
                int x = (50 - fm.stringWidth(symbol)) / 2;
                int y = ((50 - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(symbol, x, y);
                g2.dispose();
            }
        };
        pnlIcon.setPreferredSize(new Dimension(50, 50));
        
        JPanel iconWrapper = new JPanel(new BorderLayout());
        iconWrapper.setOpaque(false);
        iconWrapper.add(pnlIcon, BorderLayout.NORTH);

        String htmlContent = "<html><div style='width: 320px; line-height: 1.4; word-wrap: break-word;'>" 
                + message.replace("\n", "<br>") 
                + "</div></html>";

        JLabel msg = new JLabel(htmlContent);
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        msg.setForeground(Color.decode("#333333"));
        msg.setVerticalAlignment(SwingConstants.TOP); 

        pnlBody.add(iconWrapper, BorderLayout.WEST);
        pnlBody.add(msg, BorderLayout.CENTER);

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlFooter.setBackground(Color.WHITE);
        JButton btnClose = new JButton("Đóng");
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.setBackground(Color.decode("#1E3A8A"));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dialog.dispose());
        pnlFooter.add(btnClose);

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlBody, BorderLayout.CENTER);
        pnlMain.add(pnlFooter, BorderLayout.SOUTH);

        dialog.add(pnlMain);
        dialog.pack(); 
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}