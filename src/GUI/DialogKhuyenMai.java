package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.table.DefaultTableModel;

/**
 * DialogKhuyenMai.java - Phiên bản Refactor & UI/UX Polish
 * Chuyên nghiệp, hiện đại, tối ưu trải nghiệm người dùng trên Windows.
 */
public class DialogKhuyenMai extends JDialog {

    // --- BẢNG MÀU UI/UX CHUẨN ---
    private final Color COLOR_PRIMARY = Color.decode("#1A73E8");
    private final Color COLOR_TEXT = Color.decode("#1E293B");
    private final Color COLOR_LABEL = Color.decode("#475569");
    private final Color COLOR_BORDER = Color.decode("#CBD5E1");
    private final Color COLOR_BG_ITEM = Color.decode("#F1F5F9");
    
    private final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private final Font FONT_INPUT = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);

    // --- THÀNH PHẦN FORM ---
    private JTextField txtTen, txtGiam, txtNgayBD, txtNgayKT, txtMin;
    private JComboBox<String> cbLoai, cbApDung, cbTrangThai;
    private DefaultTableModel tableModel;
    private int rowToEdit;

    public DialogKhuyenMai(Window parent, DefaultTableModel tableModel, int rowToEdit) {
        super(parent, rowToEdit == -1 ? "Thêm chương trình mới" : "Cập nhật khuyến mại", ModalityType.APPLICATION_MODAL);
        this.tableModel = tableModel;
        this.rowToEdit = rowToEdit;

        setSize(650, 580);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // 1. Header Title
        JLabel lblHeader = new JLabel(rowToEdit == -1 ? "TẠO CHƯƠNG TRÌNH KHUYẾN MẠI" : "CHỈNH SỬA CHƯƠNG TRÌNH", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader.setForeground(COLOR_PRIMARY);
        lblHeader.setBorder(new EmptyBorder(25, 0, 15, 0));
        add(lblHeader, BorderLayout.NORTH);

        // 2. Form Content (GridBagLayout với Insets rộng rãi)
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(10, 20, 10, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(10, 15, 10, 15); // Tăng khoảng cách theo yêu cầu
        g.weightx = 1.0;

        // Row 0: Tên chương trình (Full width)
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        pnlForm.add(createStyledLabel("Tên chương trình khuyến mại *"), g);
        g.gridy = 1; 
        txtTen = new JTextField();
        styleComponent(txtTen);
        pnlForm.add(txtTen, g);

        // Row 1: Loại & Mức giảm
        g.gridwidth = 1; g.gridy = 2; g.gridx = 0;
        pnlForm.add(createStyledLabel("Loại khuyến mại"), g);
        g.gridx = 1;
        pnlForm.add(createStyledLabel("Mức giảm giá"), g);
        
        g.gridy = 3; g.gridx = 0;
        cbLoai = new JComboBox<>(new String[]{"Giảm phần trăm", "Giảm tiền cố định", "Mua X tặng Y"});
        styleComponent(cbLoai);
        pnlForm.add(cbLoai, g);
        g.gridx = 1;
        txtGiam = new JTextField();
        styleComponent(txtGiam);
        pnlForm.add(txtGiam, g);

        // Row 2: Ngày bắt đầu & Kết thúc
        g.gridy = 4; g.gridx = 0;
        pnlForm.add(createStyledLabel("Ngày bắt đầu (dd/mm/yyyy)"), g);
        g.gridx = 1;
        pnlForm.add(createStyledLabel("Ngày kết thúc (dd/mm/yyyy)"), g);
        
        g.gridy = 5; g.gridx = 0;
        txtNgayBD = new JTextField("09/04/2024");
        styleComponent(txtNgayBD);
        pnlForm.add(txtNgayBD, g);
        g.gridx = 1;
        txtNgayKT = new JTextField("30/04/2024");
        styleComponent(txtNgayKT);
        pnlForm.add(txtNgayKT, g);

        // Row 3: Đơn tối thiểu & Áp dụng
        g.gridy = 6; g.gridx = 0;
        pnlForm.add(createStyledLabel("Đơn hàng tối thiểu (đ)"), g);
        g.gridx = 1;
        pnlForm.add(createStyledLabel("Áp dụng cho"), g);
        
        g.gridy = 7; g.gridx = 0;
        txtMin = new JTextField("0");
        styleComponent(txtMin);
        pnlForm.add(txtMin, g);
        g.gridx = 1;
        cbApDung = new JComboBox<>(new String[]{"Tất cả", "Thuốc không kê đơn", "Sản phẩm chức năng"});
        styleComponent(cbApDung);
        pnlForm.add(cbApDung, g);

        // Row 4: Trạng thái
        g.gridy = 8; g.gridx = 0;
        pnlForm.add(createStyledLabel("Trạng thái"), g);
        g.gridy = 9;
        cbTrangThai = new JComboBox<>(new String[]{"Đang hoạt động", "Sắp diễn ra", "Tạm dừng"});
        styleComponent(cbTrangThai);
        pnlForm.add(cbTrangThai, g);

        add(pnlForm, BorderLayout.CENTER);

        // 3. Footer Buttons
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 20));
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(new EmptyBorder(0, 0, 10, 20));

        JButton btnHuy = createStyledButton("Hủy bỏ", Color.WHITE, Color.BLACK, false);
        btnHuy.setBorder(new LineBorder(COLOR_BORDER, 1));
        btnHuy.addActionListener(e -> dispose());

        JButton btnLuu = createStyledButton(rowToEdit == -1 ? "+ Thêm mới" : "Lưu thay đổi", COLOR_PRIMARY, Color.WHITE, true);
        btnLuu.addActionListener(e -> handleSave());

        pnlFooter.add(btnHuy);
        pnlFooter.add(btnLuu);
        add(pnlFooter, BorderLayout.SOUTH);

        // Điền dữ liệu nếu ở chế độ Sửa
        if (rowToEdit != -1) fillData();
    }

    /**
     * Hàm tiện ích áp dụng Style cho JTextField và JComboBox
     * Đảm bảo chữ không dính sát viền và có trải nghiệm tốt.
     */
    private void styleComponent(JComponent comp) {
        comp.setFont(FONT_INPUT);
        comp.setForeground(COLOR_TEXT);
        comp.setBackground(Color.WHITE);
        
        // Border với Padding 8, 12, 8, 12 tạo không gian thở
        comp.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_BORDER, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));

        if (comp instanceof JComboBox) {
            JComboBox<?> combo = (JComboBox<?>) comp;
            combo.setRenderer(new CustomComboBoxRenderer());
            // Loại bỏ viền mặc định của Windows ComboBox để đồng bộ style
            combo.setFocusable(false);
        }
    }

    private JLabel createStyledLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(COLOR_LABEL);
        return lbl;
    }

    private JButton createStyledButton(String text, Color bg, Color fg, boolean isPrimary) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setFont(FONT_BUTTON);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        
        if (isPrimary) {
            btn.setBorderPainted(false);
        }
        
        // Tăng padding cho nút to ra và sang trọng hơn
        btn.setBorder(new EmptyBorder(10, 25, 10, 25));
        
        return btn;
    }

    private void fillData() {
        txtTen.setText(tableModel.getValueAt(rowToEdit, 1).toString());
        cbLoai.setSelectedItem(tableModel.getValueAt(rowToEdit, 2).toString());
        txtGiam.setText(tableModel.getValueAt(rowToEdit, 3).toString().replace("% ", ""));
        txtMin.setText(tableModel.getValueAt(rowToEdit, 4).toString().replace("đ", ""));
        cbApDung.setSelectedItem(tableModel.getValueAt(rowToEdit, 5).toString());
        cbTrangThai.setSelectedItem(tableModel.getValueAt(rowToEdit, 7).toString());
        
        String time = tableModel.getValueAt(rowToEdit, 6).toString();
        if (time.contains(" - ")) {
            txtNgayBD.setText(time.split(" - ")[0]);
            txtNgayKT.setText(time.split(" - ")[1]);
        }
    }

    private void handleSave() {
        String ten = txtTen.getText().trim();
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên chương trình không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String status = cbTrangThai.getSelectedItem().toString();
        boolean isOn = status.equals("Đang hoạt động");

        if (rowToEdit == -1) {
            // Thêm mới vào Model
            tableModel.addRow(new Object[]{
                tableModel.getRowCount() + 1,
                ten,
                cbLoai.getSelectedItem(),
                "% " + txtGiam.getText(),
                txtMin.getText() + "đ",
                cbApDung.getSelectedItem(),
                txtNgayBD.getText() + " - " + txtNgayKT.getText(),
                status,
                "Edit",
                isOn
            });
        } else {
            // Cập nhật dòng hiện tại
            tableModel.setValueAt(ten, rowToEdit, 1);
            tableModel.setValueAt(cbLoai.getSelectedItem(), rowToEdit, 2);
            tableModel.setValueAt("% " + txtGiam.getText(), rowToEdit, 3);
            tableModel.setValueAt(txtMin.getText() + "đ", rowToEdit, 4);
            tableModel.setValueAt(cbApDung.getSelectedItem(), rowToEdit, 5);
            tableModel.setValueAt(txtNgayBD.getText() + " - " + txtNgayKT.getText(), rowToEdit, 6);
            tableModel.setValueAt(status, rowToEdit, 7);
            tableModel.setValueAt(isOn, rowToEdit, 9);
        }
        dispose();
    }

    /**
     * Custom Renderer cho JComboBox để tối ưu trải nghiệm người dùng
     * Thêm padding cho item và đổi màu khi hover/select.
     */
    private class CustomComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            // Thêm Padding cho từng item trong danh sách thả xuống
            label.setBorder(new EmptyBorder(8, 12, 8, 12));
            label.setFont(FONT_INPUT);

            if (isSelected) {
                label.setBackground(COLOR_BG_ITEM);
                label.setForeground(COLOR_PRIMARY);
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(COLOR_TEXT);
            }
            
            return label;
        }
    }
}