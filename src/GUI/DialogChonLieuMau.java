package GUI;

import BUS.BUS_SanPham;
import Entity.SanPham;
import Utils.MenuIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class DialogChonLieuMau extends JDialog {

    // ── Tham chiếu ngược để đẩy dữ liệu vào giỏ ────────────────────────────
    private final TaoHoaDon parentForm;

    // ── BUS ─────────────────────────────────────────────────────────────────
    private final BUS_SanPham busSP = new BUS_SanPham();

    // ── Cache danh sách combo đang hiển thị (để tra comboId theo row) ───────
    // Mỗi phần tử: [comboId, tenCombo, nhomBenh, giaBanCombo, ghiChu, soChiTiet]
    private List<Object[]> dsHienThi;

    // ── UI trái ──────────────────────────────────────────────────────────────
    private JTextField    txtTimKiem;
    private JComboBox<String> cbNhomLoc;
    private DefaultTableModel mdlCombo;
    private JTable        tblCombo;

    // ── UI phải ──────────────────────────────────────────────────────────────
    private JLabel        lblTenCombo, lblNhom, lblGia, lblGhiChu, lblSoThuoc;
    private DefaultTableModel mdlChiTiet;
    private JTable        tblChiTiet;

    // ── Trạng thái đang chọn ────────────────────────────────────────────────
    private String        comboIdDangChon = null;
    private JButton       btnBan;

    // ════════════════════════════════════════════════════════════════════════
    public DialogChonLieuMau(TaoHoaDon parent) {
        super(parent, "Chọn mẫu thuốc cắt liều", true);
        this.parentForm = parent;
        setSize(1080, 620);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        add(buildHeader(),               BorderLayout.NORTH);
        add(buildBody(),                 BorderLayout.CENTER);
        add(buildFooter(),               BorderLayout.SOUTH);

        // Load dữ liệu sau khi UI dựng xong
        loadNhomLoc();
        loadCombo("", "");
    }

    // ════════════════════════════════════════════════════════════════════════
    // BUILD UI
    // ════════════════════════════════════════════════════════════════════════

    private JPanel buildHeader() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        p.setBackground(Color.decode("#1E3A8A"));
        JLabel ico = new JLabel(new MenuIcon("PILL"));
        JLabel lbl = new JLabel("Chọn mẫu thuốc cắt liều để bán");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(Color.WHITE);
        p.add(ico); p.add(lbl);
        return p;
    }

    private JSplitPane buildBody() {
        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, buildLeft(), buildRight());
        split.setDividerLocation(420);
        split.setBorder(null);
        return split;
    }

    // ── Panel trái: bộ lọc + bảng danh sách ─────────────────────────────────
    private JPanel buildLeft() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(10, 10, 6, 6));

        // Bộ lọc 2 dòng
        JPanel pnlFilter = new JPanel(new GridLayout(2, 2, 8, 5));
        pnlFilter.setBackground(Color.WHITE);

        pnlFilter.add(bold("Nhóm bệnh:"));
        cbNhomLoc = new JComboBox<>();
        cbNhomLoc.addActionListener(e -> onFilter());
        pnlFilter.add(cbNhomLoc);

        pnlFilter.add(bold("Tìm tên combo:"));
        txtTimKiem = new JTextField();
        txtTimKiem.getDocument().addDocumentListener(simpleDocListener(this::onFilter));
        pnlFilter.add(txtTimKiem);

        pnl.add(pnlFilter, BorderLayout.NORTH);

        // Bảng combo — 4 cột hiển thị (không lộ comboId)
        String[] cols = {"Tên mẫu liều", "Nhóm bệnh", "Số thuốc", "Giá combo"};
        mdlCombo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCombo = new JTable(mdlCombo);
        tblCombo.setRowHeight(28);
        tblCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblCombo.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblCombo.getTableHeader().setBackground(Color.decode("#F1F5F9"));
        tblCombo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCombo.setShowGrid(false);
        tblCombo.setIntercellSpacing(new Dimension(0, 0));

        // Căn giá & số thuốc về phải/giữa
        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer rightR = new DefaultTableCellRenderer();
        rightR.setHorizontalAlignment(JLabel.RIGHT);
        tblCombo.getColumnModel().getColumn(2).setCellRenderer(centerR);
        tblCombo.getColumnModel().getColumn(3).setCellRenderer(rightR);

        tblCombo.getColumnModel().getColumn(0).setPreferredWidth(190);
        tblCombo.getColumnModel().getColumn(1).setPreferredWidth(110);
        tblCombo.getColumnModel().getColumn(2).setPreferredWidth(55);
        tblCombo.getColumnModel().getColumn(3).setPreferredWidth(90);

        // Chọn dòng → load chi tiết bên phải
        tblCombo.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onChonCombo();
        });
        // Double-click → chọn luôn
        tblCombo.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) doBan();
            }
        });

        pnl.add(new JScrollPane(tblCombo), BorderLayout.CENTER);

        JLabel hint = new JLabel("Double-click để bán ngay");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        hint.setBorder(new EmptyBorder(4, 0, 0, 0));
        pnl.add(hint, BorderLayout.SOUTH);

        return pnl;
    }

    // ── Panel phải: thông tin combo + bảng chi tiết thuốc ────────────────────
    private JPanel buildRight() {
        JPanel pnl = new JPanel(new BorderLayout(0, 8));
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(new EmptyBorder(10, 6, 6, 10));

        // Card thông tin tóm tắt
        JPanel pnlInfo = new JPanel(new GridLayout(5, 1, 0, 3));
        pnlInfo.setBackground(Color.decode("#F8FAFC"));
        pnlInfo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#CBD5E1")),
            new EmptyBorder(8, 12, 8, 12)));

        lblTenCombo = infoLabel("(Chọn một combo ở bên trái)",
            new Font("Segoe UI", Font.BOLD, 15), "#0F172A");
        lblNhom    = infoLabel("Nhóm bệnh: —",
            new Font("Segoe UI", Font.PLAIN, 13), "#475569");
        lblSoThuoc = infoLabel("Số loại thuốc: —",
            new Font("Segoe UI", Font.PLAIN, 13), "#2563EB");
        lblGia     = infoLabel("Giá combo: —",
            new Font("Segoe UI", Font.BOLD, 14), "#DC2626");
        lblGhiChu  = infoLabel("Ghi chú: —",
            new Font("Segoe UI", Font.ITALIC, 12), "#64748B");

        pnlInfo.add(lblTenCombo);
        pnlInfo.add(lblNhom);
        pnlInfo.add(lblSoThuoc);
        pnlInfo.add(lblGia);
        pnlInfo.add(lblGhiChu);
        pnl.add(pnlInfo, BorderLayout.NORTH);

        // Bảng chi tiết từng thuốc trong combo
        String[] colsCT = {
            "Tên thuốc", "ĐVT",
            "Sáng", "Trưa", "Chiều", "Tối",
            "Số ngày", "Tổng SL", "Cách dùng"
        };
        mdlChiTiet = new DefaultTableModel(colsCT, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblChiTiet = new JTable(mdlChiTiet);
        tblChiTiet.setRowHeight(26);
        tblChiTiet.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblChiTiet.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblChiTiet.getTableHeader().setBackground(Color.decode("#F1F5F9"));
        tblChiTiet.setShowGrid(true);
        tblChiTiet.setGridColor(Color.decode("#F1F5F9"));

        // Các cột số căn giữa
        DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
        cr.setHorizontalAlignment(JLabel.CENTER);
        for (int c = 2; c <= 7; c++)
            tblChiTiet.getColumnModel().getColumn(c).setCellRenderer(cr);

        tblChiTiet.getColumnModel().getColumn(0).setPreferredWidth(160);
        tblChiTiet.getColumnModel().getColumn(8).setPreferredWidth(160);

        JLabel lblTitle = bold("Chi tiết thuốc trong combo:");
        lblTitle.setBorder(new EmptyBorder(4, 0, 2, 0));

        JPanel pnlCenter = new JPanel(new BorderLayout(0, 4));
        pnlCenter.setBackground(Color.WHITE);
        pnlCenter.add(lblTitle, BorderLayout.NORTH);
        pnlCenter.add(new JScrollPane(tblChiTiet), BorderLayout.CENTER);
        pnl.add(pnlCenter, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel buildFooter() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#E2E8F0")),
            new EmptyBorder(10, 15, 10, 15)));

        JLabel note = new JLabel(
            "<html><span style='color:#64748B;font-style:italic'>" +
            "Combo được tạo từ tab \"Mẫu thuốc cắt liều\" trong Quản lý Sản phẩm.</span></html>");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnl.add(note, BorderLayout.WEST);

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBtns.setOpaque(false);

        JButton btnDong = makeBtn("Đóng", "#94A3B8", 110, 40);
        btnDong.addActionListener(e -> dispose());

        btnBan = makeBtn("✓  Bán combo này", "#2563EB", 180, 40);
        btnBan.setEnabled(false);
        btnBan.addActionListener(e -> doBan());

        pnlBtns.add(btnDong);
        pnlBtns.add(btnBan);
        pnl.add(pnlBtns, BorderLayout.EAST);
        return pnl;
    }

    // ════════════════════════════════════════════════════════════════════════
    // LOAD DỮ LIỆU
    // ════════════════════════════════════════════════════════════════════════

    /** Nạp nhóm bệnh từ DB vào ComboBox lọc */
    private void loadNhomLoc() {
        cbNhomLoc.removeAllItems();
        cbNhomLoc.addItem("-- Tất cả --");
        List<String> ds = busSP.layDanhSachNhomBenhLieu();
        if (ds != null) ds.forEach(cbNhomLoc::addItem);
    }

    /**
     * Load danh sách combo ra bảng trái.
     * @param tuKhoa  chuỗi tìm theo tên (rỗng = không lọc)
     * @param nhom    nhóm bệnh cần lọc (rỗng = tất cả)
     */
    private void loadCombo(String tuKhoa, String nhom) {
        mdlCombo.setRowCount(0);
        // layDanhSachComboNangCao trả về:
        // [0]=comboId [1]=tenCombo [2]=nhomBenh [3]=giaBanCombo [4]=ghiChu [5]=soChiTiet
        dsHienThi = busSP.layDanhSachComboNangCao();
        if (dsHienThi == null) return;

        String kw = tuKhoa.toLowerCase().trim();
        for (Object[] r : dsHienThi) {
            String ten  = r[1] != null ? r[1].toString() : "";
            String nb   = r[2] != null ? r[2].toString() : "";
            double gia  = r[3] instanceof Number ? ((Number) r[3]).doubleValue() : 0;
            int    soct = r[5] instanceof Number ? ((Number) r[5]).intValue()    : 0;

            boolean okNhom = nhom.isEmpty() || nb.equalsIgnoreCase(nhom);
            boolean okKey  = kw.isEmpty()   || ten.toLowerCase().contains(kw);
            if (okNhom && okKey) {
                mdlCombo.addRow(new Object[]{
                    ten,
                    nb,
                    soct + " loại",
                    String.format("%,.0f đ", gia)
                });
            }
        }

        // Reset bên phải
        resetChiTiet();
        comboIdDangChon = null;
        btnBan.setEnabled(false);
    }

    // ════════════════════════════════════════════════════════════════════════
    // SỰ KIỆN
    // ════════════════════════════════════════════════════════════════════════

    /** Gọi khi bộ lọc thay đổi */
    private void onFilter() {
        String nhom = "";
        if (cbNhomLoc.getSelectedItem() != null) {
            String sel = cbNhomLoc.getSelectedItem().toString();
            if (!sel.startsWith("-- Tất cả")) nhom = sel;
        }
        loadCombo(txtTimKiem.getText(), nhom);
    }

    /** Gọi khi chọn 1 dòng trong bảng combo trái */
    private void onChonCombo() {
        int row = tblCombo.getSelectedRow();
        if (row < 0 || dsHienThi == null) return;

        // Tìm comboId tương ứng với dòng đang hiển thị
        // (bảng có thể đã lọc, phải đối chiếu tên + nhóm)
        String tenChon  = mdlCombo.getValueAt(row, 0).toString();
        String nhomChon = mdlCombo.getValueAt(row, 1).toString();
        comboIdDangChon = null;
        for (Object[] r : dsHienThi) {
            if (r[1].toString().equals(tenChon) && r[2].toString().equals(nhomChon)) {
                comboIdDangChon = r[0].toString();
                break;
            }
        }
        if (comboIdDangChon == null) return;

        // Lấy chi tiết đầy đủ từ DB
        SanPham.MauLieu mau = busSP.layComboByIdNangCao(comboIdDangChon);
        if (mau == null) return;

        // Cập nhật card thông tin
        lblTenCombo.setText(mau.getTenCombo());
        lblNhom.setText("Nhóm bệnh: " + safe(mau.getNhomBenh(), "—"));
        int soThuoc = mau.getDsChiTiet() != null ? mau.getDsChiTiet().size() : 0;
        lblSoThuoc.setText("Số loại thuốc: " + soThuoc + " loại");
        lblGia.setText("Giá combo: " + String.format("%,.0f đ", mau.getGiaBanCombo()));
        String gc = mau.getGhiChu();
        lblGhiChu.setText("<html>Ghi chú: " + safe(gc, "—") + "</html>");

        // Cập nhật bảng chi tiết
        mdlChiTiet.setRowCount(0);
        if (mau.getDsChiTiet() != null) {
            for (SanPham.ChiTietLieu ct : mau.getDsChiTiet()) {
                mdlChiTiet.addRow(new Object[]{
                    ct.getTenSanPham(),
                    ct.getDvt(),
                    formatLieu(ct.getSang()),
                    formatLieu(ct.getTrua()),
                    formatLieu(ct.getChieu()),
                    formatLieu(ct.getToi()),
                    ct.getSoNgay(),
                    ct.getTongSoLuong(),
                    ct.getCachDung()
                });
            }
        }

        btnBan.setEnabled(true);
    }

    /** Xóa panel chi tiết bên phải về trạng thái ban đầu */
    private void resetChiTiet() {
        lblTenCombo.setText("(Chọn một combo ở bên trái)");
        lblNhom.setText("Nhóm bệnh: —");
        lblSoThuoc.setText("Số loại thuốc: —");
        lblGia.setText("Giá combo: —");
        lblGhiChu.setText("Ghi chú: —");
        mdlChiTiet.setRowCount(0);
    }

    /** Bán combo: gọi hàm trong TaoHoaDon rồi đóng dialog */
    private void doBan() {
        if (comboIdDangChon == null) return;
        parentForm.thucThiDoThuocTuLieuVaoGio_TuComboId(comboIdDangChon);
        dispose();
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private JLabel bold(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return l;
    }

    private JLabel infoLabel(String text, Font font, String hex) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(Color.decode(hex));
        return l;
    }

    private JButton makeBtn(String text, String hex, int w, int h) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(Color.decode(hex));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(w, h));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Hiển thị liều: nếu = 0 thì hiện "-", nguyên thì bỏ ".0" */
    private String formatLieu(double v) {
        if (v == 0) return "-";
        return v == (long) v ? String.valueOf((long) v) : String.valueOf(v);
    }

    private String safe(String s, String fallback) {
        return (s == null || s.trim().isEmpty()) ? fallback : s;
    }

    private DocumentListener simpleDocListener(Runnable action) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { action.run(); }
            public void removeUpdate(DocumentEvent e)  { action.run(); }
            public void changedUpdate(DocumentEvent e) { action.run(); }
        };
    }
}