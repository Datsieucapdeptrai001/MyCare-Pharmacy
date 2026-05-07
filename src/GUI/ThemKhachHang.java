package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import Utils.MenuIcon;

import java.awt.*;
import java.awt.event.*;

public class ThemKhachHang extends JDialog {

    private JTextField txtHoTen, txtNgayTao, txtSdt, txtEmail, txtDiaChi; // Đổi txtNgaySinh thành txtNgayTao
    private JComboBox<String> cboGioiTinh;
    private DefaultTableModel mainModel;
    
    private JLabel lblTitle;
    private JButton btnThem;
    private int editRow = -1; // -1 nghĩa là Thêm mới. Nếu >= 0 là đang Chỉnh sửa

    // 1. Constructor dùng cho THÊM MỚI
    public ThemKhachHang(Frame parent, DefaultTableModel model) {
        super(parent, "Thêm khách hàng mới", true);
        this.mainModel = model;
        initUI(parent);
    }

    // 2. Constructor dùng cho CHỈNH SỬA (Đã bổ sung thuộc tính Ngày tạo vào tham số)
    public ThemKhachHang(Frame parent, DefaultTableModel model, int editRow, String hoten, String sdt, String ngayTao) {
        super(parent, "Chỉnh sửa khách hàng", true);
        this.mainModel = model;
        this.editRow = editRow;
        initUI(parent);

        // Đổ dữ liệu cũ vào Form
        lblTitle.setText("Chỉnh sửa khách hàng");
        btnThem.setText("✓ Lưu thay đổi");
        
        txtHoTen.setText(hoten);
        txtHoTen.setForeground(Color.BLACK);
        
        txtSdt.setText(sdt);
        txtSdt.setForeground(Color.BLACK);
        
        // Đổ dữ liệu Ngày tạo có sẵn của dòng vào Textfield
        if(ngayTao != null && !ngayTao.isEmpty()){
            txtNgayTao.setText(ngayTao);
            txtNgayTao.setForeground(Color.BLACK);
        }
    }

    private void initUI(Frame parent) {
        setSize(550, 480);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setLayout(new BorderLayout());

        // --- 1. HEADER ---
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

        // --- 2. BODY ---
        JPanel pnlBody = new JPanel(new GridBagLayout());
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(10, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5); 

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        pnlBody.add(createLabel("Họ và tên", true), gbc);
        
        gbc.gridy = 1;
        txtHoTen = createTextField("Nhập họ và tên đầy đủ");
        pnlBody.add(txtHoTen, gbc);

        gbc.gridwidth = 1; gbc.weightx = 0.5;
        gbc.gridx = 0; gbc.gridy = 2;
        pnlBody.add(createLabel("Giới tính", false), gbc);
        gbc.gridx = 1;
        pnlBody.add(createLabel("Ngày tạo", false), gbc); // Đổi label thành Ngày tạo

        gbc.gridx = 0; gbc.gridy = 3;
        cboGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        cboGioiTinh.setBackground(Color.WHITE);
        cboGioiTinh.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboGioiTinh.setPreferredSize(new Dimension(0, 36));
        pnlBody.add(cboGioiTinh, gbc);

        gbc.gridx = 1;
        txtNgayTao = createTextField("dd/MM/yyyy");
        txtNgayTao.setEditable(false); 
        txtNgayTao.setBackground(Color.decode("#F8F9FA")); // Tô xám nhẹ cho ô Ngày tạo
        
        // --- Cập nhật "Áp sẵn thông tin": Nếu thêm mới thì mặc định điền Ngày hiện tại
        if (editRow == -1) {
            String homNay = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            txtNgayTao.setText(homNay);
            txtNgayTao.setForeground(Color.BLACK);
        }
        pnlBody.add(txtNgayTao, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        pnlBody.add(createLabel("Số điện thoại", true), gbc);
        gbc.gridx = 1;
        pnlBody.add(createLabel("Email", false), gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        txtSdt = createTextField("0912345678");
        pnlBody.add(txtSdt, gbc);

        gbc.gridx = 1;
        txtEmail = createTextField("email@example.com");
        pnlBody.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        pnlBody.add(createLabel("Địa chỉ", false), gbc);
        
        gbc.gridy = 7;
        txtDiaChi = createTextField("VD: 123 Nguyễn Huệ, TP.HCM");
        pnlBody.add(txtDiaChi, gbc);

        add(pnlBody, BorderLayout.CENTER);

        // --- 3. FOOTER ---
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
            
            // Validate: Bỏ qua placeholder
            if(hoten.isEmpty() || hoten.equals("Nhập họ và tên đầy đủ") || sdt.isEmpty() || sdt.equals("0912345678")) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Họ tên và Số điện thoại hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            DAO.DAO_KhachHang daoKH = new DAO.DAO_KhachHang();

            if(mainModel != null) {
                if (editRow != -1) {
                    // CHẾ ĐỘ SỬA
                    String maKH = mainModel.getValueAt(editRow, 0).toString();
                    
                    // Bạn có thể kích hoạt daoKH.capNhatKhachHang(khUpdate) tại đây nếu CSDL cần cập nhật.
                    mainModel.setValueAt(hoten, editRow, 1);
                    mainModel.setValueAt(sdt, editRow, 2);
                    // Dòng Ngày tạo đã bị khóa sửa, nên ta không cần setValueAt vào Table Model
                } else {
                    // CHẾ ĐỘ THÊM MỚI
                    String maMoi = daoKH.phatSinhMaKHTiepTheo();
                    Entity.KhachHang kh = new Entity.KhachHang();
                    kh.setId(maMoi);
                    kh.setHoVaTen(hoten);
                    kh.setSdt(sdt);
                    kh.setDiemTichLuy(0); 
                    
                    boolean isSuccess = daoKH.themKhachHang(kh);

                    if (isSuccess) {
                        mainModel.addRow(new Object[]{ maMoi, hoten, sdt, "0", "0đ", "0", ngayTao, "" });
                    } else {
                        JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi lưu vào Cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
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
}