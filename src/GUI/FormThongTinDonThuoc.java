package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FormThongTinDonThuoc extends JDialog {
    private boolean isTiepTuc = false;
    private JTextField txtBacSi, txtCoSo, txtChuanDoan;

    public FormThongTinDonThuoc(Window owner) {
        super(owner, "Thông tin đơn thuốc", Dialog.ModalityType.APPLICATION_MODAL);
        initUI();
    }

    private void initUI() {
        setSize(550, 400);
        setLocationRelativeTo(getOwner());
        setUndecorated(true); // Bỏ viền mặc định của Windows

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.decode("#1A73E8"), 2));

        // --- 1. HEADER (STEPPER) ---
        JPanel pnlStepper = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        pnlStepper.setBackground(Color.WHITE);
        pnlStepper.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.decode("#EEEEEE")));
        
        // Mô phỏng Stepper đơn giản bằng JLabel
        JLabel step1 = new JLabel("<html><div style='text-align:center;'><b style='color:#1A73E8; font-size:16px;'>❶</b><br><span style='color:#1A73E8;'>Thông tin đơn thuốc</span></div></html>");
        JLabel line = new JLabel(" ────── "); line.setForeground(Color.LIGHT_GRAY);
        JLabel step2 = new JLabel("<html><div style='text-align:center;'><b style='color:gray; font-size:16px;'>❷</b><br><span style='color:gray;'>Thanh toán</span></div></html>");
        
        pnlStepper.add(step1); pnlStepper.add(line); pnlStepper.add(step2);
        mainPanel.add(pnlStepper, BorderLayout.NORTH);

        // --- 2. BODY (FORM NHẬP LIỆU) ---
        JPanel pnlForm = new JPanel(new GridLayout(3, 1, 0, 15));
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        txtBacSi = createTextField("Tên bác sĩ kê đơn (*)");
        txtCoSo = createTextField("Cơ sở khám bệnh (*)");
        txtChuanDoan = createTextField("Chuẩn đoán");

        pnlForm.add(txtBacSi);
        pnlForm.add(txtCoSo);
        pnlForm.add(txtChuanDoan);
        mainPanel.add(pnlForm, BorderLayout.CENTER);

        // --- 3. FOOTER (NÚT BẤM) ---
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlFooter.setBackground(Color.WHITE);

        JButton btnQuayLai = new JButton("Quay lại");
        btnQuayLai.setPreferredSize(new Dimension(100, 35));
        btnQuayLai.setBackground(Color.WHITE);
        btnQuayLai.setFocusPainted(false);
        btnQuayLai.addActionListener(e -> dispose()); // Đóng form, hủy thanh toán

        JButton btnTiepTuc = new JButton("Tiếp tục");
        btnTiepTuc.setPreferredSize(new Dimension(100, 35));
        btnTiepTuc.setBackground(Color.decode("#1A73E8"));
        btnTiepTuc.setForeground(Color.WHITE);
        btnTiepTuc.setFocusPainted(false);
        btnTiepTuc.addActionListener(e -> {
            if(txtBacSi.getText().trim().isEmpty() || txtCoSo.getText().trim().isEmpty()) {
            	JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin bắt buộc (*)", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            isTiepTuc = true;
            dispose(); // Đóng form và đi tới thanh toán
        });

        pnlFooter.add(btnQuayLai);
        pnlFooter.add(btnTiepTuc);
        mainPanel.add(pnlFooter, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JTextField createTextField(String title) {
        JPanel pnl = new JPanel(new BorderLayout(0, 5));
        pnl.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        if(title.contains("(*)")) lbl.setForeground(Color.RED);

        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(0, 35));
        pnl.add(lbl, BorderLayout.NORTH);
        pnl.add(txt, BorderLayout.CENTER);
        return txt; // Trả về txt để add vào form chung (Mẹo để lấy dữ liệu sau này)
    }

    public boolean isTiepTuc() { return isTiepTuc; }
    
    // Các hàm getter để lấy dữ liệu nếu cần lưu vào Database
    public String getTenBacSi() { return txtBacSi.getText(); }
    public String getCoSoKham() { return txtCoSo.getText(); }
    public String getChuanDoan() { return txtChuanDoan.getText(); }
}