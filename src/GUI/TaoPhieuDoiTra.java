package GUI;

import Components.MenuIcon;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class TaoPhieuDoiTra extends JDialog {

    private JPanel pnlSuccessInfo; // Khung xanh lá báo thành công
    private JPanel pnlDetailsForm; // Khu vực form điền chi tiết
    private JLabel lblError;       // Dòng chữ đỏ báo lỗi
    private JTextField txtSearch;
    private JButton btnTraHang, btnDoiHang;
    private DefaultTableModel mainModel;
    private JComboBox<String> cboLyDo; 
    
    
    private Color primaryRed = Color.decode("#DC2626"); // Đỏ chủ đạo
    private Color borderGray = Color.decode("#DFE3E8");

    public TaoPhieuDoiTra(Frame parent,DefaultTableModel model) {
        super(parent, "Tạo phiếu đổi / trả hàng", true);
        setSize(750, 240);
        this.mainModel = model;
        setLocationRelativeTo(parent);
        setUndecorated(true);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- 1. HEADER (Màu đỏ) ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(primaryRed);
        pnlHeader.setBorder(new EmptyBorder(12, 15, 12, 15));
        
        JLabel lblTitle = new JLabel("Tạo phiếu đổi / trả hàng");
        lblTitle.setIcon(new MenuIcon("SYNC")); // Giả định có icon SYNC (2 mũi tên vòng)
        lblTitle.setIconTextGap(10);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        JButton btnClose = new JButton("X");
        btnClose.setForeground(Color.WHITE);
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        
        // --- THÊM DÒNG NÀY ĐỂ XÓA TRIỆT ĐỂ VIỀN FOCUS ---
        btnClose.setFocusPainted(false); 
        
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(btnClose, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // --- 2. BODY ---
        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(20, 25, 20, 25));

        // 2.1 Khu vực Tìm kiếm
        pnlBody.add(createSearchPanel());
        pnlBody.add(Box.createRigidArea(new Dimension(0, 15)));

        // 2.2 Khu vực Thông báo tìm thấy (Mặc định ẨN)
        pnlSuccessInfo = createSuccessPanel();
        pnlSuccessInfo.setVisible(false); 
        pnlBody.add(pnlSuccessInfo);
        pnlBody.add(Box.createRigidArea(new Dimension(0, 20)));

        // 2.3 Khu vực Form chi tiết (Mặc định ẨN)
        pnlDetailsForm = createDetailsPanel();
        pnlDetailsForm.setVisible(false);
        pnlBody.add(pnlDetailsForm);

        // Bọc vào ScrollPane đề phòng màn hình nhỏ
        JScrollPane scrollPane = new JScrollPane(pnlBody);
        scrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderGray));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. FOOTER ---
        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(Color.WHITE);
        pnlFooter.setBorder(new EmptyBorder(15, 25, 15, 25));

        JButton btnHuy = new JButton("Hủy");
        btnHuy.setIcon(new MenuIcon("CLOSE"));
        btnHuy.setPreferredSize(new Dimension(100, 40));
        btnHuy.setBackground(Color.WHITE);
        btnHuy.setBorder(BorderFactory.createLineBorder(borderGray));
        btnHuy.setFocusPainted(false);
        btnHuy.addActionListener(e -> dispose());

        JButton btnTaoPhieu = new JButton("Tạo phiếu đổi/trả");
        btnTaoPhieu.setIcon(new MenuIcon("SYNC"));
        btnTaoPhieu.setIconTextGap(8);
        btnTaoPhieu.setPreferredSize(new Dimension(220, 40));
        btnTaoPhieu.setBackground(primaryRed);
        btnTaoPhieu.setForeground(Color.WHITE);
        btnTaoPhieu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTaoPhieu.setBorderPainted(false);
        btnTaoPhieu.setFocusPainted(false);

        // LOGIC NÚT TẠO PHIẾU
     // LOGIC NÚT TẠO PHIẾU
        btnTaoPhieu.addActionListener(e -> {
            if (!pnlDetailsForm.isVisible()) {
                lblError.setVisible(true);
            } else {
                // ĐẨY DỮ LIỆU XUỐNG BẢNG
                if(mainModel != null) {
                    String maPhieu = "DTH-" + (System.currentTimeMillis() % 1000);
                    String hdGoc = txtSearch.getText().trim();
                    String loai = btnTraHang.getBackground().equals(Color.decode("#FF3B30")) ? "Trả hàng" : "Đổi hàng";
                    String loi = cboLyDo.getSelectedItem().toString();
                    String ngay = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    
                    // Thêm dòng mới vào bảng
                    mainModel.addRow(new Object[]{
                        maPhieu, hdGoc, "Khách lẻ", loai, loi, "25.000đ", "---", "Hoàn thành", ngay, ""
                    });
                }
                
                // ĐÃ XÓA DÒNG JOptionPane Ở ĐÂY
                
                dispose(); // Vẫn giữ lại dòng này để nó tự động tắt bảng Tạo Phiếu
            }
        });

        pnlFooter.add(btnHuy, BorderLayout.WEST);
        pnlFooter.add(btnTaoPhieu, BorderLayout.EAST);
        add(pnlFooter, BorderLayout.SOUTH);
    }

    // --- CÁC HÀM XÂY DỰNG COMPONENT ---

    private JPanel createSearchPanel() {
        JPanel pnl = new JPanel(new BorderLayout(10, 5));
        pnl.setBackground(Color.WHITE);
        pnl.setMaximumSize(new Dimension(1000, 75)); // Khống chế chiều cao trong BoxLayout

        JLabel lblTraCuu = new JLabel("Tra cứu hóa đơn gốc");
        lblTraCuu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTraCuu.setForeground(Color.decode("#374151"));
        pnl.add(lblTraCuu, BorderLayout.NORTH);

        JPanel pnlInput = new JPanel(new BorderLayout(10, 0));
        pnlInput.setBackground(Color.WHITE);
        
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(0, 40));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderGray),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        pnlInput.add(txtSearch, BorderLayout.CENTER);

        JButton btnTim = new JButton("Tìm");
        btnTim.setIcon(new MenuIcon("SEARCH"));
        btnTim.setBackground(Color.decode("#1967D2"));
        btnTim.setForeground(Color.WHITE);
        btnTim.setPreferredSize(new Dimension(90, 40));
        btnTim.setFocusPainted(false);
        pnlInput.add(btnTim, BorderLayout.EAST);
        
        pnl.add(pnlInput, BorderLayout.CENTER);

        lblError = new JLabel("Phải tra cứu hóa đơn hợp lệ trước");
        lblError.setForeground(primaryRed);
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblError.setVisible(false); // Ẩn mặc định
        pnl.add(lblError, BorderLayout.SOUTH);

        // LOGIC NÚT TÌM
        btnTim.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();
            if (!keyword.isEmpty()) {
                // Tắt lỗi, Bật Form và Khung thành công
                lblError.setVisible(false);
                pnlSuccessInfo.setVisible(true);
                pnlDetailsForm.setVisible(true);
                
                // BỔ SUNG LỆNH PHÓNG TO BẢNG VÀ CĂN GIỮA LẠI
                setSize(750, 700);
                setLocationRelativeTo(getOwner()); 
                
                revalidate();
                repaint();
            } else {
                lblError.setText("Vui lòng nhập mã hóa đơn!");
                lblError.setVisible(true);
            }
        });

        return pnl;
    }

    private JPanel createSuccessPanel() {
        JPanel pnl = new JPanel(new GridLayout(2, 1, 0, 5));
        pnl.setBackground(Color.decode("#F0FDF4")); // Nền xanh nhạt
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#BBF7D0")),
            new EmptyBorder(10, 15, 10, 15)
        ));
        pnl.setMaximumSize(new Dimension(1000, 70));

        JLabel lblMaHD = new JLabel("Hóa đơn HD-2024-0015");
        lblMaHD.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMaHD.setForeground(Color.decode("#16A34A")); // Chữ xanh đậm

        JLabel lblDetails = new JLabel("KH: Khách lẻ | Ngày: 11/04/2026 | Tổng: 26.250đ");
        lblDetails.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDetails.setForeground(Color.decode("#4B5563"));

        pnl.add(lblMaHD);
        pnl.add(lblDetails);
        return pnl;
    }

    private JPanel createDetailsPanel() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBackground(Color.WHITE);

        // 1. Dòng Loại yêu cầu & Nguyên nhân
        JPanel pnlRow1 = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlRow1.setBackground(Color.WHITE);
        pnlRow1.setMaximumSize(new Dimension(1000, 60));

        // Cột trái: Loại yêu cầu (2 nút Toggle)
        JPanel pnlLoai = new JPanel(new BorderLayout(0, 5));
        pnlLoai.setBackground(Color.WHITE);
        pnlLoai.add(new JLabel("<html><b>Loại yêu cầu</b></html>"), BorderLayout.NORTH);
        
        JPanel pnlToggle = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlToggle.setBackground(Color.WHITE);
        
        btnTraHang = new JButton("Trả hàng");
        btnTraHang.setBackground(Color.decode("#FF3B30")); // Đỏ sáng
        btnTraHang.setForeground(Color.WHITE);
        btnTraHang.setFocusPainted(false);
        
        btnDoiHang = new JButton("Đổi hàng");
        btnDoiHang.setBackground(Color.WHITE);
        btnDoiHang.setForeground(Color.decode("#4B5563"));
        btnDoiHang.setBorder(BorderFactory.createLineBorder(borderGray));
        btnDoiHang.setFocusPainted(false);
        
        // Logic đổi màu khi ấn (Toggle)
        btnTraHang.addActionListener(e -> setToggleState(true));
        btnDoiHang.addActionListener(e -> setToggleState(false));

        pnlToggle.add(btnTraHang);
        pnlToggle.add(btnDoiHang);
        pnlLoai.add(pnlToggle, BorderLayout.CENTER);

        // Cột phải: Nguyên nhân
        JPanel pnlLyDo = new JPanel(new BorderLayout(0, 5));
        pnlLyDo.setBackground(Color.WHITE);
        pnlLyDo.add(new JLabel("<html><b>Nguyên nhân</b></html>"), BorderLayout.NORTH);
        
     // FIX CHUẨN: Chỉ gán trực tiếp vào biến toàn cục đã khai báo ở trên cùng
        cboLyDo = new JComboBox<>(new String[]{"Lỗi nhà sản xuất", "Khách đổi ý", "Hết hạn sử dụng"});
        cboLyDo.setBackground(Color.WHITE);
        pnlLyDo.add(cboLyDo, BorderLayout.CENTER);

        pnlRow1.add(pnlLoai);
        pnlRow1.add(pnlLyDo);
        pnl.add(pnlRow1);
        pnl.add(Box.createRigidArea(new Dimension(0, 20)));

        // 2. Bảng Sản phẩm
        JPanel pnlTable = new JPanel(new BorderLayout(0, 5));
        pnlTable.setBackground(Color.WHITE);
        
        JLabel lblSPTitle = new JLabel("<html><b>Sản phẩm đổi/trả</b><br><font color='red' size='3'>Chọn ít nhất 1 sản phẩm</font></html>");
        pnlTable.add(lblSPTitle, BorderLayout.NORTH);

        String[] cols = {"Sản phẩm", "Số lượng", "Giá", "Thành tiền"};
        Object[][] data = {{"Paracetamol 500mg", "1", "25.000đ", "25.000đ"}};
        DefaultTableModel model = new DefaultTableModel(data, cols);
        JTable table = new JTable(model);
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,0));
        
        // Căn lề bảng
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRender);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRender);
        
        DefaultTableCellRenderer rightRender = new DefaultTableCellRenderer();
        rightRender.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(3).setCellRenderer(rightRender);

        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderGray));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.setPreferredSize(new Dimension(0, 100)); // Chiều cao vừa đủ 1-2 dòng
        sp.getViewport().setBackground(Color.WHITE);
        pnlTable.add(sp, BorderLayout.CENTER);

        // Nút Hoàn tiền
        JLabel lblHoanTien = new JLabel("Hoàn tiền: 25.000đ", SwingConstants.RIGHT);
        lblHoanTien.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHoanTien.setForeground(primaryRed);
        lblHoanTien.setBorder(new EmptyBorder(10, 0, 0, 0));
        pnlTable.add(lblHoanTien, BorderLayout.SOUTH);

        pnl.add(pnlTable);
        pnl.add(Box.createRigidArea(new Dimension(0, 20)));

        // 3. Ghi chú
        JPanel pnlNote = new JPanel(new BorderLayout(0, 5));
        pnlNote.setBackground(Color.WHITE);
        pnlNote.add(new JLabel("<html><b>Ghi chú</b></html>"), BorderLayout.NORTH);
        
        JTextArea txtNote = new JTextArea("Mô tả lý do, tình trạng sản phẩm...");
        txtNote.setForeground(Color.GRAY);
        txtNote.setLineWrap(true);
        txtNote.setRows(3);
        
        JScrollPane spNote = new JScrollPane(txtNote);
        spNote.setBorder(BorderFactory.createLineBorder(borderGray));
        pnlNote.add(spNote, BorderLayout.CENTER);

        pnl.add(pnlNote);

        return pnl;
    }

    private void setToggleState(boolean isTraHang) {
        if (isTraHang) {
            btnTraHang.setBackground(Color.decode("#FF3B30"));
            btnTraHang.setForeground(Color.WHITE);
            btnDoiHang.setBackground(Color.WHITE);
            btnDoiHang.setForeground(Color.decode("#4B5563"));
        } else {
            btnDoiHang.setBackground(Color.decode("#FF3B30"));
            btnDoiHang.setForeground(Color.WHITE);
            btnTraHang.setBackground(Color.WHITE);
            btnTraHang.setForeground(Color.decode("#4B5563"));
        }
    }
    
}