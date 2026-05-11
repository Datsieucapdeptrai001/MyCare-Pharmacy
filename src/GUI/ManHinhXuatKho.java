package GUI;

import BUS.BUS_Kho;
import BUS.BUS_DonViDoLuong;
import Entity.DonViDoLuong;
import Entity.LoHang;
import Entity.SanPham;
import Utils.MenuIcon;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
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
    private JComboBox<String> cbDonVi;
    private JLabel lblTenSP, lblTonKhoHienTai;

    private JTable table;
    private DefaultTableModel tableModel;

    private final BUS_Kho busKho = new BUS_Kho();
    private final BUS_DonViDoLuong busDonVi = new BUS_DonViDoLuong();

    // Các biến lưu trạng thái hiện tại
    private int currentTonKho = 0;
    private SanPham currentSanPham = null;
    private List<DonViDoLuong> currentDsDonVi = new ArrayList<>();
    private List<LoHang> cacheDanhSachLo = new ArrayList<>();

    public ManHinhXuatKho() {
        setLayout(new BorderLayout());
        setBackground(BG_APP);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        // Tải dữ liệu ngầm an toàn (Thread-safe)
        new Thread(() -> {
            List<LoHang> tempData = busKho.layDSLoHang(true);
            SwingUtilities.invokeLater(() -> {
                cacheDanhSachLo = tempData;
            });
        }).start();

        add(createHeader(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        // ============================================================
        // TỰ ĐỘNG MỞ BẢNG QUÉT MÃ KHI VỪA HIỆN MÀN HÌNH
        // ============================================================
        this.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                SwingUtilities.invokeLater(() -> showQRScannerDialog());
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }

    private JPanel createHeader() {
        JPanel pnlLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlLeft.setOpaque(false);
        JLabel lblIcon = new JLabel(new MenuIcon("XUAT_KHO", 26, DANGER));
        pnlLeft.add(lblIcon);
        pnlLeft.add(new JLabel(
                "<html><b style='color:#0F172A; font-size:24px; font-family: Segoe UI;'>PHIẾU XUẤT / HỦY KHO</b></html>"));
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(0, 0, 20, 0));
        pnl.add(pnlLeft, BorderLayout.WEST);
        return pnl;
    }

    private JPanel createMainContent() {
        JPanel unifiedCard = new JPanel(new BorderLayout(25, 0));
        unifiedCard.setBackground(BG_CARD);
        unifiedCard.setBorder(new CompoundRoundBorder(new SmoothShadowBorder(new Color(0, 0, 0, 12), 16),
                new Insets(20, 25, 20, 25)));
        unifiedCard.add(createInputForm(), BorderLayout.WEST);
        unifiedCard.add(createCartTable(), BorderLayout.CENTER);
        return unifiedCard;
    }

    private JPanel createInputForm() {
        JPanel pnlWrapper = new JPanel(new BorderLayout(0, 10));
        pnlWrapper.setOpaque(false);
        pnlWrapper.setPreferredSize(new Dimension(420, 0));
        pnlWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR), new EmptyBorder(0, 0, 0, 20)));

        JLabel lblTitle = new JLabel("Thông tin lô hàng");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(PRIMARY_BLUE);
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        pnlWrapper.add(lblTitle, BorderLayout.NORTH);

        // Panel chứa các trường nhập liệu
        JPanel pnlFields = new JPanel();
        pnlFields.setLayout(new BoxLayout(pnlFields, BoxLayout.Y_AXIS));
        pnlFields.setOpaque(false);

        pnlFields.add(createLabel("Quét / Nhập Mã Lô *"));
        JPanel pnlMaLo = new JPanel(new BorderLayout(8, 0));
        pnlMaLo.setOpaque(false);
        pnlMaLo.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlMaLo.setMaximumSize(new Dimension(400, 42));
        pnlMaLo.setPreferredSize(new Dimension(400, 42));

        txtMaLo = createTextField();
        txtMaLo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    kiemTraMaLo();
            }
        });

        JButton btnScanQR = createHoverButton("Quét Mã Tem", PRIMARY_BLUE, PRIMARY_HOVER, Color.WHITE);
        btnScanQR.setPreferredSize(new Dimension(130, 42));
        btnScanQR.setIcon(new MenuIcon("SEARCH", 20, Color.WHITE));
        btnScanQR.addActionListener(e -> showQRScannerDialog());
        pnlMaLo.add(txtMaLo, BorderLayout.CENTER);
        pnlMaLo.add(btnScanQR, BorderLayout.EAST);
        pnlFields.add(pnlMaLo);
        pnlFields.add(Box.createVerticalStrut(12));

        pnlFields.add(createLabel("Sản phẩm"));
        lblTenSP = new JLabel("-- Chưa có dữ liệu --");
        lblTenSP.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTenSP.setForeground(TEXT_PRIMARY);
        lblTenSP.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlFields.add(lblTenSP);
        pnlFields.add(Box.createVerticalStrut(12));

        pnlFields.add(createLabel("Tồn kho lô này hiện tại"));
        JPanel pnlTonKho = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTonKho.setOpaque(false);
        pnlTonKho.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTonKhoHienTai = new JLabel("0");
        lblTonKhoHienTai.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTonKhoHienTai.setForeground(DANGER);
        pnlTonKho.add(lblTonKhoHienTai);
        pnlFields.add(pnlTonKho);
        pnlFields.add(Box.createVerticalStrut(12));

        pnlFields.add(createLabel("Số lượng & Đơn vị xuất *"));
        JPanel pnlSoLuong = new JPanel(new BorderLayout(8, 0));
        pnlSoLuong.setOpaque(false);
        pnlSoLuong.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlSoLuong.setMaximumSize(new Dimension(400, 42));
        pnlSoLuong.setPreferredSize(new Dimension(400, 42));

        txtSoLuong = createTextField();
        txtSoLuong.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    themVaoDanhSach();
            }
        });

        cbDonVi = new JComboBox<>();
        cbDonVi.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cbDonVi.setBackground(Color.WHITE);
        cbDonVi.setPreferredSize(new Dimension(120, 42));
        cbDonVi.setBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8));

        pnlSoLuong.add(txtSoLuong, BorderLayout.CENTER);
        pnlSoLuong.add(cbDonVi, BorderLayout.EAST);
        pnlFields.add(pnlSoLuong);
        pnlFields.add(Box.createVerticalStrut(12));

        pnlFields.add(createLabel("Lý do xuất *"));
        // Đã xóa Lý do "Bán hàng" theo yêu cầu
        cbLyDo = new JComboBox<>(new String[] {
                "Xuất hủy hàng hỏng/lỗi",
                "Xuất hủy hàng hết hạn",
                "Xuất trả nhà cung cấp",
                "Xuất tiêu hao nội bộ",
                "Khác"
        });
        cbLyDo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbLyDo.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbLyDo.setMaximumSize(new Dimension(400, 40));
        cbLyDo.setPreferredSize(new Dimension(400, 40));
        cbLyDo.setBackground(Color.WHITE);
        cbLyDo.setBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8));
        pnlFields.add(cbLyDo);
        pnlFields.add(Box.createVerticalStrut(12));

        pnlFields.add(createLabel("Ghi chú"));
        txtGhiChu = createTextField();
        txtGhiChu.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlFields.add(txtGhiChu);
        pnlFields.add(Box.createVerticalStrut(20));

        // ============================================================
        // FIX LỖI ÉP GIAO DIỆN VÀ TẠO SCROLLBAR MƯỢT MÀ
        // ============================================================
        JPanel pnlScrollContent = new JPanel(new BorderLayout());
        pnlScrollContent.setOpaque(false);
        pnlScrollContent.add(pnlFields, BorderLayout.NORTH); // Đẩy hết nội dung lên trên cùng để ko bị ép giãn

        JScrollPane scrollFields = new JScrollPane(pnlScrollContent);
        scrollFields.setBorder(null);
        scrollFields.setOpaque(false);
        scrollFields.getViewport().setOpaque(false);
        scrollFields.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollFields.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollFields.getVerticalScrollBar().setUnitIncrement(16); // Tăng tốc độ cuộn chuột cực mượt
        scrollFields.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0)); // Làm thanh cuộn mỏng tinh tế

        pnlWrapper.add(scrollFields, BorderLayout.CENTER);
        // ============================================================

        JButton btnAdd = createHoverButton("Thêm vào danh sách", SUCCESS, SUCCESS_HOVER, Color.WHITE);
        btnAdd.setIcon(new MenuIcon("ADD", 20, Color.WHITE));
        btnAdd.setPreferredSize(new Dimension(400, 45));
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnAdd.addActionListener(e -> themVaoDanhSach());
        pnlWrapper.add(btnAdd, BorderLayout.SOUTH);

        return pnlWrapper;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 5, 0));
        return lbl;
    }

    // --- BẢNG YÊU CẦU QUÉT MÃ ---
    public void showQRScannerDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (owner == null)
            return;

        JDialog dialog = new JDialog(owner, "YÊU CẦU QUÉT MÃ", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(450, 180);
        dialog.setLocationRelativeTo(this);
        dialog.setUndecorated(true);
        dialog.setShape(new RoundRectangle2D.Double(0, 0, dialog.getWidth(), dialog.getHeight(), 16, 16));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 2));

        JLabel lblIconScan = new JLabel(new MenuIcon("XUAT_KHO", 40, PRIMARY_BLUE));
        lblIconScan.setHorizontalAlignment(SwingConstants.CENTER);
        lblIconScan.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel lblInfo = new JLabel("Vui lòng đưa súng quét đọc mã trên tem...", SwingConstants.CENTER);
        lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblInfo.setForeground(TEXT_PRIMARY);

        JButton btnClose = createHoverButton("Nhập tay", new Color(241, 245, 249), new Color(226, 232, 240),
                TEXT_SECONDARY);
        btnClose.setPreferredSize(new Dimension(120, 38));
        btnClose.addActionListener(e -> dialog.dispose());

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.add(btnClose);

        root.add(lblIconScan, BorderLayout.NORTH);
        root.add(lblInfo, BorderLayout.CENTER);
        root.add(pnlBottom, BorderLayout.SOUTH);

        JTextField txtHiddenQR = new JTextField();
        txtHiddenQR.setOpaque(false);
        txtHiddenQR.setBorder(null);
        txtHiddenQR.setForeground(new Color(0, 0, 0, 0));
        root.add(txtHiddenQR, BorderLayout.WEST);

        txtHiddenQR.addActionListener(e -> {
            String qrData = txtHiddenQR.getText().trim();
            if (!qrData.isEmpty()) {
                dialog.dispose();
                txtMaLo.setText(qrData);
                kiemTraMaLo(); // Quét xong tự chạy kiểm tra luôn
            }
        });

        dialog.addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                txtHiddenQR.requestFocusInWindow();
            }
        });

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private JPanel createCartTable() {
        JPanel pnl = new JPanel(new BorderLayout(0, 15));
        pnl.setOpaque(false);
        pnl.setBorder(new EmptyBorder(0, 10, 0, 0));
        JLabel lblTitle = new JLabel("Danh sách Lô hàng chuẩn bị xuất");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(TEXT_PRIMARY);
        pnl.add(lblTitle, BorderLayout.NORTH);

        // Đã thêm cột Ghi chú.
        // Index hiện tại: 0-Mã lô, 1-Tên SP, 2-SL, 3-Đơn vị, 4-Lý do, 5-Ghi chú, 6-Xóa,
        // 7-SL_QuyDoi_Hidden
        String[] cols = { "Mã lô", "Tên Sản phẩm", "SL Xuất", "Đơn vị", "Lý do", "Ghi chú", "Xóa", "SL_QuyDoi_Hidden" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(44);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER_COLOR);
        table.setIntercellSpacing(new Dimension(0, 0));

        // Căn chỉnh độ rộng cột
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        table.getColumnModel().getColumn(3).setPreferredWidth(60);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);
        table.getColumnModel().getColumn(6).setPreferredWidth(45);

        // Cột ẩn: SL_QuyDoi_Hidden (Số thứ tự là 7)
        table.getColumnModel().getColumn(7).setMinWidth(0);
        table.getColumnModel().getColumn(7).setMaxWidth(0);
        table.getColumnModel().getColumn(7).setWidth(0);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 44));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // Icon Xóa nằm ở cột số 6
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel lbl = new JLabel(new MenuIcon("TRASH", 20, DANGER));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
                return lbl;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 6) // Cột Xóa giờ là số 6
                    tableModel.removeRow(row);
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8));
        scroll.getViewport().setBackground(Color.WHITE);
        pnl.add(scroll, BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlBottom.setOpaque(false);
        JButton btnLamMoi = createHoverButton("Làm mới", Color.WHITE, new Color(248, 250, 252), TEXT_SECONDARY);
        btnLamMoi.setBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8));
        btnLamMoi.setPreferredSize(new Dimension(110, 45));
        btnLamMoi.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLamMoi.addActionListener(e -> resetFormToanBo());

        JButton btnXacNhan = createHoverButton("CHỐT PHIẾU XUẤT KHO", DANGER, DANGER_HOVER, Color.WHITE);
        btnXacNhan.setPreferredSize(new Dimension(220, 45));
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnXacNhan.addActionListener(e -> hoanTatXuatKho());

        pnlBottom.add(btnLamMoi);
        pnlBottom.add(btnXacNhan);
        pnl.add(pnlBottom, BorderLayout.SOUTH);
        return pnl;
    }

    private String formatTonKhoHienThi(String maSP, int soLuongCoBan) {
        List<DonViDoLuong> ds = busDonVi.getDSTheoMaSP(maSP);
        if (ds == null || ds.isEmpty())
            return soLuongCoBan + " Đơn vị";
        DonViDoLuong dvLonNhat = ds.get(0);
        DonViDoLuong dvCoBan = ds.get(0);
        for (DonViDoLuong dv : ds) {
            if (dv.getChuyenDoiSangDonViCoBan() == 1.0)
                dvCoBan = dv;
            if (dv.getChuyenDoiSangDonViCoBan() > dvLonNhat.getChuyenDoiSangDonViCoBan())
                dvLonNhat = dv;
        }
        int heSoQuyDoi = (int) dvLonNhat.getChuyenDoiSangDonViCoBan();
        if (heSoQuyDoi <= 1 || soLuongCoBan == 0)
            return soLuongCoBan + " " + dvCoBan.getTen();
        int soLuongLon = soLuongCoBan / heSoQuyDoi;
        int soLuongLe = soLuongCoBan % heSoQuyDoi;
        String ketQua = "";
        if (soLuongLon > 0)
            ketQua += soLuongLon + " " + dvLonNhat.getTen();
        if (soLuongLe > 0) {
            if (!ketQua.isEmpty())
                ketQua += ", ";
            ketQua += soLuongLe + " " + dvCoBan.getTen();
        }
        return ketQua;
    }

    private void kiemTraMaLo() {
        String maQuet = txtMaLo.getText().trim();
        if (maQuet.isEmpty())
            return;

        LoHang loTimThay = null;
        List<LoHang> dsLoKhop = new ArrayList<>();
        DonViDoLuong dvQuetDuoc = null;

        try {
            dvQuetDuoc = busDonVi.layDonViTheoMaVach(maQuet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (LoHang lh : cacheDanhSachLo) {
            boolean match = false;
            if (dvQuetDuoc != null) {
                if (lh.getSanPhamId() != null && lh.getSanPhamId().getId().equals(dvQuetDuoc.getSanPhamId().getId()))
                    match = true;
            } else {
                boolean khopMaVachNoiBo = lh.getMaVachNoiBo() != null && lh.getMaVachNoiBo().equalsIgnoreCase(maQuet);
                boolean khopSoLo = lh.getSoLoHang() != null && lh.getSoLoHang().equalsIgnoreCase(maQuet);
                boolean khopMaSP = lh.getSanPhamId() != null && lh.getSanPhamId().getId().equalsIgnoreCase(maQuet);
                if (khopMaVachNoiBo || khopSoLo || khopMaSP)
                    match = true;
            }
            if (match)
                dsLoKhop.add(lh);
        }

        if (dsLoKhop.isEmpty()) {
            showCustomNotification("Không tìm thấy", "Mã '" + maQuet + "' không tồn tại trong kho.", "ERROR");
            resetThongTinSP();
            return;
        }

        // Ưu tiên FEFO (Lấy lô cũ nhất)
        dsLoKhop.sort((a, b) -> a.getNgayHetHan().compareTo(b.getNgayHetHan()));
        loTimThay = dsLoKhop.get(0);

        txtMaLo.setText(loTimThay.getSoLoHang());
        lblTenSP.setText(loTimThay.getSanPhamId().getTen());
        currentTonKho = loTimThay.getSoLuongLoHang();
        currentSanPham = loTimThay.getSanPhamId();

        // Hiển thị Tồn kho chuẩn
        lblTonKhoHienTai.setText(formatTonKhoHienThi(currentSanPham.getId(), currentTonKho));

        // Load danh sách ĐVT vào Combobox
        cbDonVi.removeAllItems();
        currentDsDonVi = busDonVi.getDSTheoMaSP(currentSanPham.getId());
        if (currentDsDonVi != null) {
            for (DonViDoLuong dv : currentDsDonVi) {
                cbDonVi.addItem(dv.getTen());
            }
        }

        // Tự động chọn đúng Đơn vị tính nếu người dùng quét mã vạch
        if (dvQuetDuoc != null) {
            cbDonVi.setSelectedItem(dvQuetDuoc.getTen());
        } else if (currentDsDonVi != null && !currentDsDonVi.isEmpty()) {
            DonViDoLuong dvCoBan = currentDsDonVi.get(0);
            for (DonViDoLuong d : currentDsDonVi) {
                if (d.getChuyenDoiSangDonViCoBan() == 1.0)
                    dvCoBan = d;
            }
            cbDonVi.setSelectedItem(dvCoBan.getTen());
        }

        // Dừng lại cho người dùng nhập số lượng
        txtSoLuong.setText("1");
        txtSoLuong.requestFocus();
        txtSoLuong.selectAll();
    }

    private void themVaoDanhSach() {
        if (lblTenSP.getText().equals("-- Chưa có dữ liệu --") || cbDonVi.getSelectedItem() == null) {
            showCustomNotification("Chưa có dữ liệu", "Vui lòng Quét đúng Mã lô trước.", "WARNING");
            return;
        }
        try {
            int slXuatNhapVao = Integer.parseInt(txtSoLuong.getText().trim());
            if (slXuatNhapVao <= 0) {
                showCustomNotification("Lỗi nhập liệu", "Số lượng xuất kho phải lớn hơn 0.", "ERROR");
                return;
            }

            String tenDVChon = cbDonVi.getSelectedItem().toString();
            DonViDoLuong dvChon = null;
            for (DonViDoLuong dv : currentDsDonVi) {
                if (dv.getTen().equals(tenDVChon)) {
                    dvChon = dv;
                    break;
                }
            }
            if (dvChon == null)
                return;

            int slXuatQuyDoi = (int) (slXuatNhapVao * dvChon.getChuyenDoiSangDonViCoBan());
            int tongDaCoTrongGio = 0;
            String maLoHienTai = txtMaLo.getText().trim();

            // Cột ẩn chứa số lượng quy đổi đổi từ index 6 thành 7
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (tableModel.getValueAt(i, 0).toString().equalsIgnoreCase(maLoHienTai)) {
                    tongDaCoTrongGio += Integer.parseInt(tableModel.getValueAt(i, 7).toString());
                }
            }

            if ((slXuatQuyDoi + tongDaCoTrongGio) > currentTonKho) {
                int conLaiCoTheXuat = currentTonKho - tongDaCoTrongGio;
                String msg = "Số lượng xuất lố tồn kho gốc!\n\n";
                msg += "• Kho còn: " + currentTonKho + " ĐV cơ bản\n";
                msg += "• Đang chờ xuất: " + tongDaCoTrongGio + " ĐV cơ bản\n";
                if (conLaiCoTheXuat > 0) {
                    msg += "• Chỉ có thể xuất thêm tối đa: " + conLaiCoTheXuat + " ĐV cơ bản.";
                } else {
                    msg += "• Lô này đã được vét sạch vào giỏ.";
                }
                showCustomNotification("Tồn kho không đủ", msg, "ERROR");
                return;
            }

            tableModel.addRow(new Object[] {
                    maLoHienTai,
                    lblTenSP.getText(),
                    slXuatNhapVao,
                    dvChon.getTen(),
                    cbLyDo.getSelectedItem().toString(),
                    txtGhiChu.getText().trim(), // Cột Ghi chú
                    "",
                    slXuatQuyDoi // Cột 7 Ẩn: Lưu số lượng quy đổi
            });

            resetFormNhap();
            txtMaLo.requestFocus();

        } catch (NumberFormatException e) {
            showCustomNotification("Sai định dạng", "Vui lòng nhập số nguyên hợp lệ.", "ERROR");
        }
    }

    private void hoanTatXuatKho() {
        if (tableModel.getRowCount() == 0) {
            showCustomNotification("Danh sách trống", "Chưa có lô hàng nào trong danh sách.", "WARNING");
            return;
        }
        boolean confirmed = showCustomConfirmDialog("XÁC NHẬN XUẤT KHO", "Xuất <b>" + tableModel.getRowCount()
                + "</b> lô hàng. Hệ thống sẽ trừ trực tiếp vào kho hiện tại.<br>Bạn chắc chắn thực hiện?");
        if (confirmed) {
            List<Object[]> ds = new ArrayList<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                // ds.add(new Object[] { Mã Lô (0), SL_QuyDoi_Ẩn (7), Lý do (4) })
                ds.add(new Object[] { tableModel.getValueAt(i, 0), tableModel.getValueAt(i, 7),
                        tableModel.getValueAt(i, 4) });
            }

            if (busKho.xuatHuyKho(ds, "Nhân Viên Thu Ngân")) {
                showCustomNotification("THÀNH CÔNG", "Đã trừ kho thành công!", "SUCCESS");

                String maPhieuXuat = "PX" + (System.currentTimeMillis() % 100000);
                String ngayXuat = java.time.LocalDate.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                StringBuilder qrData = new StringBuilder();
                qrData.append("PHIẾU XUẤT: ").append(maPhieuXuat).append("\n");
                qrData.append("NGÀY XUẤT: ").append(ngayXuat).append("\n");
                qrData.append("--- CHI TIẾT HÀNG ---\n");

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    qrData.append("• ").append(tableModel.getValueAt(i, 0)).append(" | ");
                    qrData.append(tableModel.getValueAt(i, 1)).append(" | ");
                    qrData.append(tableModel.getValueAt(i, 2)).append(" ");
                    qrData.append(tableModel.getValueAt(i, 3)).append("\n");
                }

                String tenHienThi = "KIỆN HÀNG XUẤT (" + tableModel.getRowCount() + " Món)";

                Window owner = SwingUtilities.getWindowAncestor(this);
                DialogInQRCode dialogQR = new DialogInQRCode(owner, qrData.toString(), maPhieuXuat, "Ngày: " + ngayXuat,
                        tenHienThi);
                dialogQR.setVisible(true);

                resetFormToanBo();
            } else {
                showCustomNotification("THẤT BẠI", "Gặp sự cố khi lưu vào Database.", "ERROR");
            }
        }
    }

    private void resetFormNhap() {
        txtMaLo.setText("");
        txtSoLuong.setText("");
        txtGhiChu.setText("");
        cbLyDo.setSelectedIndex(0);
        resetThongTinSP();
    }

    private void resetThongTinSP() {
        lblTenSP.setText("-- Chưa có dữ liệu --");
        lblTonKhoHienTai.setText("0");
        cbDonVi.removeAllItems();
        currentTonKho = 0;
        currentSanPham = null;
        currentDsDonVi.clear();
    }

    private void resetFormToanBo() {
        resetFormNhap();
        tableModel.setRowCount(0);
        txtMaLo.requestFocus();
    }

    private JTextField createTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txt.setMaximumSize(new Dimension(400, 42));
        txt.setBorder(BorderFactory.createCompoundBorder(new RoundedLineBorder(BORDER_COLOR, 1, 8),
                new EmptyBorder(5, 12, 5, 12)));
        return txt;
    }

    private JButton createHoverButton(String text, Color bg, Color hoverBg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedLineBorder(bg, 1, 8));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    private boolean showCustomConfirmDialog(String titleText, String message) {
        final boolean[] result = { false };
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 2));
        pnlMain.setBackground(Color.WHITE);
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(PRIMARY_BLUE);
        pnlHeader.setPreferredSize(new Dimension(0, 45));
        JLabel lblTitle = new JLabel(titleText.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.CENTER);
        JPanel pnlBody = new JPanel(new BorderLayout());
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(20, 25, 15, 25));
        JLabel msg = new JLabel("<html><center style='color:#333333; font-family:Segoe UI; font-size:14px;'>" + message
                + "</center></html>", SwingConstants.CENTER);
        pnlBody.add(msg, BorderLayout.CENTER);
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        pnlFooter.setBackground(Color.WHITE);
        JButton btnYes = createHoverButton("Xác nhận", DANGER, DANGER_HOVER, Color.WHITE);
        btnYes.setPreferredSize(new Dimension(130, 40));
        btnYes.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });
        JButton btnNo = createHoverButton("Hủy bỏ", TEXT_SECONDARY, new Color(71, 85, 105), Color.WHITE);
        btnNo.setPreferredSize(new Dimension(130, 40));
        btnNo.addActionListener(e -> dialog.dispose());
        pnlFooter.add(btnYes);
        pnlFooter.add(btnNo);
        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlBody, BorderLayout.CENTER);
        pnlMain.add(pnlFooter, BorderLayout.SOUTH);
        dialog.add(pnlMain);
        dialog.setSize(450, 260);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        return result[0];
    }

    private void showCustomNotification(String titleText, String message, String type) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 2));
        pnlMain.setBackground(Color.WHITE);
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(PRIMARY_BLUE);
        pnlHeader.setPreferredSize(new Dimension(0, 45));
        JLabel lblTitle = new JLabel(titleText.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.CENTER);
        JPanel pnlBody = new JPanel(null);
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setPreferredSize(new Dimension(420, 130));
        JPanel pnlIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                if (type.equals("ERROR")) {
                    g2.setColor(new Color(254, 226, 226));
                    g2.fillRoundRect(0, 0, 50, 50, 50, 50);
                    g2.setColor(DANGER);
                    g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(17, 17, 33, 33);
                    g2.drawLine(33, 17, 17, 33);
                } else if (type.equals("SUCCESS")) {
                    g2.setColor(new Color(209, 250, 229));
                    g2.fillRoundRect(0, 0, 50, 50, 50, 50);
                    g2.setColor(SUCCESS);
                    g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(15, 26, 22, 33);
                    g2.drawLine(22, 33, 35, 16);
                } else {
                    g2.setColor(new Color(254, 243, 199));
                    g2.fillRoundRect(0, 0, 50, 50, 50, 50);
                    g2.setColor(new Color(245, 158, 11));
                    g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(25, 14, 25, 28);
                    g2.fillOval(22, 33, 6, 6);
                }
                g2.dispose();
            }
        };
        pnlIcon.setBounds(25, 25, 50, 50);
        pnlIcon.setOpaque(false);
        JTextArea msgArea = new JTextArea(message);
        msgArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        msgArea.setWrapStyleWord(true);
        msgArea.setLineWrap(true);
        msgArea.setOpaque(false);
        msgArea.setEditable(false);
        msgArea.setFocusable(false);
        JScrollPane scroll = new JScrollPane(msgArea);
        scroll.setBounds(95, 20, 305, 95);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        pnlBody.add(pnlIcon);
        pnlBody.add(scroll);
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlFooter.setBackground(Color.WHITE);
        JButton btnClose = createHoverButton("Đóng", PRIMARY_BLUE, PRIMARY_HOVER, Color.WHITE);
        btnClose.setPreferredSize(new Dimension(110, 38));
        btnClose.addActionListener(e -> dialog.dispose());
        pnlFooter.add(btnClose);
        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlBody, BorderLayout.CENTER);
        pnlMain.add(pnlFooter, BorderLayout.SOUTH);
        dialog.add(pnlMain);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        if (type.equals("SUCCESS"))
            new Timer(1500, e -> dialog.dispose()).start();
        dialog.setVisible(true);
    }

    private static class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int thickness, radius;

        public RoundedLineBorder(Color c, int t, int r) {
            color = c;
            thickness = t;
            radius = r;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            for (int i = 0; i < thickness; i++)
                g2.drawRoundRect(x + i, y + i, w - 1 - i * 2, h - 1 - i * 2, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
    }

    private static class SmoothShadowBorder extends AbstractBorder {
        private final Color shadow;
        private final int radius;

        public SmoothShadowBorder(Color s, int r) {
            shadow = s;
            radius = r;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 4, 8, 4);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < 6; i++) {
                g2.setColor(new Color(shadow.getRed(), shadow.getGreen(), shadow.getBlue(),
                        Math.max(1, shadow.getAlpha() - i * 2)));
                g2.drawRoundRect(x + 1, y + 1 + i, w - 3, h - 3 - i, radius, radius);
            }
            g2.dispose();
        }
    }

    private static class CompoundRoundBorder extends AbstractBorder {
        private final AbstractBorder outer;
        private final Insets inner;

        public CompoundRoundBorder(AbstractBorder o, Insets i) {
            outer = o;
            inner = i;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            Insets o = outer.getBorderInsets(c);
            return new Insets(o.top + inner.top, o.left + inner.left, o.bottom + inner.bottom, o.right + inner.right);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            outer.paintBorder(c, g, x, y, w, h);
        }
    }
}