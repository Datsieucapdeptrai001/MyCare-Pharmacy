package GUI;

import BUS.BUS_Kho;
import BUS.BUS_DonViDoLuong;
import Entity.DonViDoLuong;
import Entity.LoHang;
import Utils.MenuIcon;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ManHinhXuatKho extends JPanel {

    private static final Color BG_APP = new Color(241, 245, 249);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color PRIMARY_BLUE = new Color(14, 116, 144);
    private static final Color PRIMARY_HOVER = new Color(22, 133, 163);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color SUCCESS_HOVER = new Color(22, 163, 74);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color DANGER_HOVER = new Color(220, 38, 38);

    private JTextField txtMaLo, txtSoLuong, txtGhiChu;
    private JComboBox<String> cbLyDo;
    private JLabel lblTenSP, lblTonKhoHienTai, lblDonVi;
    
    private JTable table;
    private DefaultTableModel tableModel;
    
    private final BUS_Kho busKho = new BUS_Kho();
    private final BUS_DonViDoLuong busDonVi = new BUS_DonViDoLuong();
    private int currentTonKho = 0; 
    private boolean isSelectingSuggest = false; 
    
    private List<LoHang> cacheDanhSachLo = new ArrayList<>();
    private JPopupMenu suggestPopup;
    private JList<String> suggestList;
    private DefaultListModel<String> listModel;

    public ManHinhXuatKho() {
        setLayout(new BorderLayout());
        setBackground(BG_APP);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        new Thread(() -> { cacheDanhSachLo = busKho.layDSLoHang(true); }).start();

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlLeft.setOpaque(false);
        JLabel lblIcon = new JLabel(new MenuIcon("XUAT_KHO", 26, DANGER)); 
        pnlLeft.add(lblIcon);
        pnlLeft.add(new JLabel("<html><b style='color:#0F172A; font-size:24px; font-family: Segoe UI;'>PHIẾU XUẤT KHO / XUẤT HỦY</b></html>"));
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(0, 0, 20, 0));
        pnl.add(pnlLeft, BorderLayout.WEST);
        return pnl;
    }

    private JPanel createMainContent() {
        JPanel unifiedCard = new JPanel(new BorderLayout(25, 0));
        unifiedCard.setBackground(BG_CARD);
        unifiedCard.setBorder(new CompoundRoundBorder(new SmoothShadowBorder(new Color(0, 0, 0, 12), 16), new Insets(20, 25, 20, 25)));
        unifiedCard.add(createInputForm(), BorderLayout.WEST);
        unifiedCard.add(createCartTable(), BorderLayout.CENTER);
        return unifiedCard;
    }

    private JPanel createInputForm() {
        JPanel pnlWrapper = new JPanel(new BorderLayout(0, 10));
        pnlWrapper.setOpaque(false); pnlWrapper.setPreferredSize(new Dimension(360, 0));
        pnlWrapper.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR), new EmptyBorder(0, 0, 0, 20)));

        JLabel lblTitle = new JLabel("Thông tin xuất kho");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18)); lblTitle.setForeground(PRIMARY_BLUE);
        pnlWrapper.add(lblTitle, BorderLayout.NORTH);

        JPanel pnlFields = new JPanel(); pnlFields.setLayout(new BoxLayout(pnlFields, BoxLayout.Y_AXIS)); pnlFields.setOpaque(false);
        pnlFields.add(Box.createVerticalStrut(5));

        pnlFields.add(createLabel("Nhập / Chọn Mã Lô hàng *"));
        JPanel pnlMaLo = new JPanel(new BorderLayout(8, 0)); pnlMaLo.setOpaque(false);
        pnlMaLo.setMaximumSize(new Dimension(340, 38)); pnlMaLo.setPreferredSize(new Dimension(340, 38));
        
        txtMaLo = createTextField(); setupAutoSuggest(); 
        
        JButton btnCheck = createHoverButton("Kiểm tra", PRIMARY_BLUE, PRIMARY_HOVER, Color.WHITE);
        btnCheck.addActionListener(e -> kiemTraMaLo());
        pnlMaLo.add(txtMaLo, BorderLayout.CENTER); pnlMaLo.add(btnCheck, BorderLayout.EAST);
        pnlFields.add(pnlMaLo); pnlFields.add(Box.createVerticalStrut(12));

        pnlFields.add(createLabel("Sản phẩm"));
        lblTenSP = new JLabel("-- Chưa chọn --"); lblTenSP.setFont(new Font("Segoe UI", Font.BOLD, 14)); lblTenSP.setForeground(TEXT_PRIMARY);
        pnlFields.add(lblTenSP); pnlFields.add(Box.createVerticalStrut(12));

        pnlFields.add(createLabel("Tồn kho hiện tại"));
        JPanel pnlTonKho = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); pnlTonKho.setOpaque(false);
        lblTonKhoHienTai = new JLabel("0"); lblTonKhoHienTai.setFont(new Font("Segoe UI", Font.BOLD, 18)); lblTonKhoHienTai.setForeground(DANGER);
        lblDonVi = new JLabel(" Đơn vị"); lblDonVi.setFont(new Font("Segoe UI", Font.PLAIN, 14)); lblDonVi.setForeground(TEXT_SECONDARY);
        pnlTonKho.add(lblTonKhoHienTai); pnlTonKho.add(lblDonVi);
        pnlFields.add(pnlTonKho); pnlFields.add(Box.createVerticalStrut(12));

        pnlFields.add(createLabel("Số lượng xuất *"));
        txtSoLuong = createTextField(); pnlFields.add(txtSoLuong); pnlFields.add(Box.createVerticalStrut(12));

        pnlFields.add(createLabel("Lý do xuất *"));
        cbLyDo = new JComboBox<>(new String[]{"Xuất hủy hàng hỏng/lỗi", "Xuất hủy hàng hết hạn", "Xuất trả nhà cung cấp", "Xuất tiêu hao nội bộ", "Lý do khác"});
        cbLyDo.setFont(new Font("Segoe UI", Font.PLAIN, 14)); cbLyDo.setMaximumSize(new Dimension(340, 38)); cbLyDo.setPreferredSize(new Dimension(340, 38));
        cbLyDo.setBackground(Color.WHITE); cbLyDo.setBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8));
        pnlFields.add(cbLyDo); pnlFields.add(Box.createVerticalStrut(12));

        pnlFields.add(createLabel("Ghi chú"));
        txtGhiChu = createTextField(); pnlFields.add(txtGhiChu); pnlFields.add(Box.createVerticalStrut(20));

        JPanel pnlScrollContent = new JPanel(new BorderLayout()); pnlScrollContent.setOpaque(false); pnlScrollContent.add(pnlFields, BorderLayout.NORTH);
        JScrollPane scrollFields = new JScrollPane(pnlScrollContent); scrollFields.setBorder(null); scrollFields.setOpaque(false); scrollFields.getViewport().setOpaque(false);
        scrollFields.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); scrollFields.getVerticalScrollBar().setUnitIncrement(16); scrollFields.getVerticalScrollBar().setPreferredSize(new Dimension(5, 0));
        pnlWrapper.add(scrollFields, BorderLayout.CENTER);

        JButton btnAdd = createHoverButton("Thêm vào danh sách", SUCCESS, SUCCESS_HOVER, Color.WHITE);
        btnAdd.setIcon(new MenuIcon("ADD")); btnAdd.setPreferredSize(new Dimension(340, 45)); btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnAdd.addActionListener(e -> themVaoDanhSach());
        pnlWrapper.add(btnAdd, BorderLayout.SOUTH);

        return pnlWrapper;
    }

    private JPanel createCartTable() {
        JPanel pnl = new JPanel(new BorderLayout(0, 15)); pnl.setOpaque(false); pnl.setBorder(new EmptyBorder(0, 10, 0, 0));
        JLabel lblTitle = new JLabel("Danh sách Lô hàng chờ xuất"); lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18)); lblTitle.setForeground(TEXT_PRIMARY);
        pnl.add(lblTitle, BorderLayout.NORTH);

        String[] cols = {"Mã lô", "Tên Sản phẩm", "SL Xuất", "Đơn vị", "Lý do", "Xóa"};
        tableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        table = new JTable(tableModel); table.setRowHeight(44); table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(239, 246, 255)); table.setSelectionForeground(TEXT_PRIMARY); table.setShowVerticalLines(false); table.setGridColor(BORDER_COLOR); table.setIntercellSpacing(new Dimension(0, 0));
        
        JTableHeader header = table.getTableHeader(); header.setFont(new Font("Segoe UI", Font.BOLD, 14)); header.setBackground(new Color(248, 250, 252)); header.setForeground(TEXT_SECONDARY); header.setPreferredSize(new Dimension(header.getWidth(), 44)); header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(new MenuIcon("TRASH")); lbl.setHorizontalAlignment(SwingConstants.CENTER); lbl.setForeground(DANGER); lbl.setOpaque(true); lbl.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE); return lbl;
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint()); int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 5) tableModel.removeRow(row);
            }
        });

        JScrollPane scroll = new JScrollPane(table); scroll.setBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8)); scroll.getViewport().setBackground(Color.WHITE); pnl.add(scroll, BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); pnlBottom.setOpaque(false);
        JButton btnLamMoi = createHoverButton("Làm mới", Color.WHITE, new Color(248, 250, 252), TEXT_SECONDARY); btnLamMoi.setBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8)); btnLamMoi.setPreferredSize(new Dimension(110, 45)); btnLamMoi.setFont(new Font("Segoe UI", Font.BOLD, 14)); btnLamMoi.addActionListener(e -> resetFormToanBo());
        JButton btnXacNhan = createHoverButton("XÁC NHẬN XUẤT KHO", DANGER, DANGER_HOVER, Color.WHITE); btnXacNhan.setPreferredSize(new Dimension(200, 45)); btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 14)); btnXacNhan.addActionListener(e -> hoanTatXuatKho());
        pnlBottom.add(btnLamMoi); pnlBottom.add(btnXacNhan); pnl.add(pnlBottom, BorderLayout.SOUTH);
        return pnl;
    }

    private void setupAutoSuggest() {
        suggestPopup = new JPopupMenu(); suggestPopup.setFocusable(false);
        listModel = new DefaultListModel<>(); suggestList = new JList<>(listModel); suggestList.setFont(new Font("Segoe UI", Font.PLAIN, 14)); suggestList.setFixedCellHeight(30); suggestList.setSelectionBackground(new Color(239, 246, 255));
        JScrollPane scroll = new JScrollPane(suggestList); scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR)); scroll.setPreferredSize(new Dimension(340, 150)); suggestPopup.add(scroll);

        txtMaLo.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { if (!isSelectingSuggest) updateSuggest(); }
            public void removeUpdate(DocumentEvent e) { if (!isSelectingSuggest) updateSuggest(); }
            public void changedUpdate(DocumentEvent e) { if (!isSelectingSuggest) updateSuggest(); }
        });
        suggestList.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 1) chonGoiY(); } });
    }

    private void updateSuggest() {
        String keyword = txtMaLo.getText().trim().toLowerCase(); listModel.clear();
        if (keyword.isEmpty()) { suggestPopup.setVisible(false); return; }
        for (LoHang lh : cacheDanhSachLo) {
            String soLo = lh.getSoLoHang().trim(); String tenSP = (lh.getSanPhamId() != null) ? lh.getSanPhamId().getTen() : "";
            if (soLo.toLowerCase().contains(keyword) || tenSP.toLowerCase().contains(keyword)) listModel.addElement(soLo + " - " + tenSP);
        }
        if (listModel.getSize() > 0) { suggestPopup.show(txtMaLo, 0, txtMaLo.getHeight()); txtMaLo.requestFocus(); } else suggestPopup.setVisible(false);
    }

    private void chonGoiY() {
        String selected = suggestList.getSelectedValue();
        if (selected != null) {
            isSelectingSuggest = true; txtMaLo.setText(selected.split(" - ")[0].trim()); isSelectingSuggest = false; suggestPopup.setVisible(false); kiemTraMaLo(); 
        }
    }

    private void kiemTraMaLo() {
        String ma = txtMaLo.getText().trim();
        if (ma.isEmpty()) { showCustomNotification("Thiếu thông tin", "Vui lòng nhập hoặc chọn Mã lô hàng cần xuất.", "WARNING"); return; }
        LoHang loTimThay = null;
        for (LoHang lh : cacheDanhSachLo) { if (lh.getSoLoHang().equalsIgnoreCase(ma)) { loTimThay = lh; break; } }
        if (loTimThay == null) { showCustomNotification("Không tìm thấy lô hàng", "Mã lô '" + ma + "' không tồn tại trong hệ thống.\nVui lòng kiểm tra lại.", "ERROR"); resetThongTinSP(); return; }

        lblTenSP.setText(loTimThay.getSanPhamId().getTen()); currentTonKho = loTimThay.getSoLuongLoHang(); lblTonKhoHienTai.setText(String.format("%,d", currentTonKho));
        List<DonViDoLuong> ds = busDonVi.getDSTheoMaSP(loTimThay.getSanPhamId().getId()); String dvStr = " Đơn vị";
        if (ds != null && !ds.isEmpty()) { dvStr = " " + ds.get(0).getTen(); for (DonViDoLuong dv : ds) if (dv.getChuyenDoiSangDonViCoBan() == 1.0) { dvStr = " " + dv.getTen(); break; } }
        lblDonVi.setText(dvStr); txtSoLuong.requestFocus();
    }

    private void themVaoDanhSach() {
        if (lblTenSP.getText().equals("-- Chưa chọn --")) { showCustomNotification("Chưa chọn sản phẩm", "Vui lòng nhập và kiểm tra Mã lô hợp lệ trước khi thêm.", "WARNING"); return; }
        try {
            int slXuat = Integer.parseInt(txtSoLuong.getText().trim());
            if (slXuat <= 0) { showCustomNotification("Số lượng không hợp lệ", "Số lượng xuất kho phải lớn hơn 0.", "ERROR"); return; }
            if (slXuat > currentTonKho) { showCustomNotification("Vượt quá tồn kho", "Số lượng yêu cầu vượt quá số tồn khả dụng (" + currentTonKho + ").", "ERROR"); return; }
            tableModel.addRow(new Object[]{txtMaLo.getText().trim(), lblTenSP.getText(), slXuat, lblDonVi.getText().trim(), cbLyDo.getSelectedItem().toString(), ""}); resetFormNhap();
        } catch (NumberFormatException e) { showCustomNotification("Sai định dạng", "Số lượng xuất không hợp lệ. Vui lòng chỉ nhập số nguyên.", "ERROR"); }
    }

    private void hoanTatXuatKho() {
        if (tableModel.getRowCount() == 0) { showCustomNotification("Danh sách trống", "Chưa có lô hàng nào trong danh sách chờ xuất.", "WARNING"); return; }

        boolean confirmed = showCustomConfirmDialog(
            "XÁC NHẬN XUẤT KHO",
            "Bạn đang yêu cầu xuất <b>" + tableModel.getRowCount() + "</b> lô hàng.<br><br>" +
            "<i>Lưu ý: Hệ thống sẽ trừ trực tiếp số lượng vào kho.<br>" +
            "Giá trị xuất này sẽ <b style='color:#EF4444;'>không</b> được tính vào doanh thu.</i><br><br>" +
            "Bạn có chắc chắn muốn tiếp tục?"
        );

        if (confirmed) {
            List<Object[]> ds = new ArrayList<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                ds.add(new Object[]{tableModel.getValueAt(i, 0), tableModel.getValueAt(i, 2), tableModel.getValueAt(i, 4)});
            }

            // [LƯU Ý]: Chỗ này truyền cứng "Nguyễn Quản Lý" để test, mày có thể thay bằng biến Tài khoản đăng nhập thật
            if (busKho.xuatHuyKho(ds, "Nguyễn Quản Lý")) {
                showCustomNotification("XUẤT KHO THÀNH CÔNG", "Đã cập nhật tồn kho thành công!\nCửa sổ sẽ tự động đóng sau giây lát...", "SUCCESS");
                Window window = SwingUtilities.getWindowAncestor(this); if (window != null) window.dispose();
            } else {
                showCustomNotification("Cập nhật thất bại", "Hệ thống gặp sự cố khi lưu dữ liệu. Vui lòng thử lại.", "ERROR");
            }
        }
    }

    private void resetFormNhap() { txtMaLo.setText(""); txtSoLuong.setText(""); txtGhiChu.setText(""); cbLyDo.setSelectedIndex(0); resetThongTinSP(); txtMaLo.requestFocus();}
    private void resetThongTinSP() { lblTenSP.setText("-- Chưa chọn --"); lblTonKhoHienTai.setText("0"); lblDonVi.setText(" Đơn vị"); currentTonKho = 0; }
    private void resetFormToanBo() { resetFormNhap(); tableModel.setRowCount(0); }

    private JLabel createLabel(String text) { JLabel lbl = new JLabel(text); lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14)); lbl.setForeground(TEXT_SECONDARY); lbl.setBorder(new EmptyBorder(0, 0, 5, 0)); return lbl; }
    private JTextField createTextField() { JTextField txt = new JTextField(); txt.setFont(new Font("Segoe UI", Font.PLAIN, 15)); txt.setBorder(BorderFactory.createCompoundBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8), new EmptyBorder(5, 12, 5, 12))); txt.setPreferredSize(new Dimension(340, 40)); return txt; }
    private JButton createHoverButton(String text, Color bg, Color hoverBg, Color fg) {
        JButton btn = new JButton(text); btn.setFont(new Font("Segoe UI", Font.BOLD, 14)); btn.setForeground(fg); btn.setBackground(bg); btn.setFocusPainted(false); btn.setBorder(new RoundedLineBorder(bg, 1, 8)); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() { @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hoverBg); } @Override public void mouseExited(MouseEvent e) { btn.setBackground(bg); } }); return btn;
    }

    private boolean showCustomConfirmDialog(String titleText, String message) {
        final boolean[] result = {false}; Window owner = SwingUtilities.getWindowAncestor(this); JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true); dialog.setBackground(new Color(0, 0, 0, 0));
        JPanel pnlMain = new JPanel(new BorderLayout()); pnlMain.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 2)); pnlMain.setBackground(Color.WHITE);
        JPanel pnlHeader = new JPanel(new BorderLayout()); pnlHeader.setBackground(PRIMARY_BLUE); pnlHeader.setPreferredSize(new Dimension(0, 45)); JLabel lblTitle = new JLabel(titleText.toUpperCase(), SwingConstants.CENTER); lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblTitle.setForeground(Color.WHITE); pnlHeader.add(lblTitle, BorderLayout.CENTER);
        JPanel pnlBody = new JPanel(new BorderLayout()); pnlBody.setBackground(Color.WHITE); pnlBody.setBorder(new EmptyBorder(20, 25, 15, 25));
        JLabel msg = new JLabel("<html><center style='color:#333333; font-family:Segoe UI; font-size:14px;'>" + message + "</center></html>", SwingConstants.CENTER); msg.setVerticalAlignment(SwingConstants.CENTER); pnlBody.add(msg, BorderLayout.CENTER);
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20)); pnlFooter.setBackground(Color.WHITE);
        JButton btnYes = createHoverButton("Xác nhận", DANGER, DANGER_HOVER, Color.WHITE); btnYes.setPreferredSize(new Dimension(130, 40)); btnYes.addActionListener(e -> { result[0] = true; dialog.dispose(); });
        JButton btnNo = createHoverButton("Hủy bỏ", TEXT_SECONDARY, new Color(71, 85, 105), Color.WHITE); btnNo.setPreferredSize(new Dimension(130, 40)); btnNo.addActionListener(e -> dialog.dispose());
        pnlFooter.add(btnYes); pnlFooter.add(btnNo); pnlMain.add(pnlHeader, BorderLayout.NORTH); pnlMain.add(pnlBody, BorderLayout.CENTER); pnlMain.add(pnlFooter, BorderLayout.SOUTH);
        dialog.add(pnlMain); dialog.setSize(450, 290); dialog.setLocationRelativeTo(this); dialog.setVisible(true); return result[0];
    }

    private void showCustomNotification(String titleText, String message, String type) {
        Window owner = SwingUtilities.getWindowAncestor(this); JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true); dialog.setBackground(new Color(0, 0, 0, 0));
        JPanel pnlMain = new JPanel(new BorderLayout()); pnlMain.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 2)); pnlMain.setBackground(Color.WHITE);
        JPanel pnlHeader = new JPanel(new BorderLayout()); pnlHeader.setBackground(PRIMARY_BLUE); pnlHeader.setPreferredSize(new Dimension(0, 45)); JLabel lblTitle = new JLabel(titleText.toUpperCase(), SwingConstants.CENTER); lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16)); lblTitle.setForeground(Color.WHITE); pnlHeader.add(lblTitle, BorderLayout.CENTER);
        JPanel pnlBody = new JPanel(null); pnlBody.setBackground(Color.WHITE); pnlBody.setPreferredSize(new Dimension(420, 110));
        JPanel pnlIcon = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = type.equals("ERROR") ? DANGER : (type.equals("SUCCESS") ? SUCCESS : new Color(245, 158, 11)); g2.setColor(type.equals("ERROR") ? new Color(254, 242, 242) : (type.equals("SUCCESS") ? new Color(209, 250, 229) : new Color(254, 243, 199)));
                g2.fillOval(0, 0, 50, 50); g2.setColor(c); g2.setStroke(new BasicStroke(3f)); g2.drawOval(0, 0, 50, 50); g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                String s = type.equals("ERROR") ? "X" : (type.equals("SUCCESS") ? "V" : "!"); g2.drawString(s, (50 - g2.getFontMetrics().stringWidth(s)) / 2, 35); g2.dispose();
            }
        };
        pnlIcon.setBounds(25, 25, 50, 50); pnlIcon.setOpaque(false);
        JTextArea msgArea = new JTextArea(message); msgArea.setFont(new Font("Segoe UI", Font.PLAIN, 15)); msgArea.setWrapStyleWord(true); msgArea.setLineWrap(true); msgArea.setOpaque(false); msgArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(msgArea); scroll.setBounds(95, 20, 305, 80); scroll.setBorder(null); scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        pnlBody.add(pnlIcon); pnlBody.add(scroll);
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15)); pnlFooter.setBackground(Color.WHITE);
        JButton btnClose = createHoverButton("Đóng", PRIMARY_BLUE, PRIMARY_HOVER, Color.WHITE); btnClose.setPreferredSize(new Dimension(110, 38)); btnClose.addActionListener(e -> dialog.dispose()); pnlFooter.add(btnClose);
        pnlMain.add(pnlHeader, BorderLayout.NORTH); pnlMain.add(pnlBody, BorderLayout.CENTER); pnlMain.add(pnlFooter, BorderLayout.SOUTH);
        dialog.add(pnlMain); dialog.pack(); dialog.setLocationRelativeTo(this);
        if (type.equals("SUCCESS")) { new Timer(1500, e -> dialog.dispose()).start(); } dialog.setVisible(true); 
    }

    private static class RoundedLineBorder extends AbstractBorder {
        private final Color color; private final int thickness, radius; public RoundedLineBorder(Color c, int t, int r) { color = c; thickness = t; radius = r; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(color); for (int i = 0; i < thickness; i++) { g2.drawRoundRect(x+i, y+i, w-1-i*2, h-1-i*2, radius, radius); } g2.dispose(); }
        @Override public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
    }
    private static class SmoothShadowBorder extends AbstractBorder {
        private final Color shadow; private final int radius; public SmoothShadowBorder(Color s, int r) { shadow = s; radius = r; }
        @Override public Insets getBorderInsets(Component c) { return new Insets(4, 4, 8, 4); }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); for (int i = 0; i < 6; i++) { g2.setColor(new Color(shadow.getRed(), shadow.getGreen(), shadow.getBlue(), Math.max(1, shadow.getAlpha() - i * 2))); g2.drawRoundRect(x+1, y+1+i, w-3, h-3-i, radius, radius); } g2.dispose(); }
    }
    private static class CompoundRoundBorder extends AbstractBorder {
        private final AbstractBorder outer; private final Insets inner; public CompoundRoundBorder(AbstractBorder o, Insets i) { outer = o; inner = i; }
        @Override public Insets getBorderInsets(Component c) { Insets o = outer.getBorderInsets(c); return new Insets(o.top+inner.top, o.left+inner.left, o.bottom+inner.bottom, o.right+inner.right); }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) { outer.paintBorder(c, g, x, y, w, h); }
    }
}