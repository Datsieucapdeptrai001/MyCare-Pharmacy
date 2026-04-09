package GUI;

import DAO.DAO_LoHang;
import DAO.DAO_SanPham;
import Entity.LoHang;
import Entity.SanPham;
import Enum.TrangThaiLoHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ManHinhLoHang extends JPanel {

    private final DAO_LoHang daoLoHang = new DAO_LoHang();
    private final DAO_SanPham daoSanPham = new DAO_SanPham();
    private final NumberFormat numberVN = NumberFormat.getInstance(new Locale("vi", "VN"));
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final List<LoHang> dsTatCa = new ArrayList<>();
    private final Map<String, SanPham> cacheSanPham = new HashMap<>();

    private JTextField txtSearch;
    private JLabel lblExpired;
    private JLabel lblNear;
    private JLabel lblWarning;
    private JLabel lblGood;
    private JLabel lblHeaderAlert;
    private JLabel lblTotal;

    private JButton btnTatCa;
    private JButton btnDuocBan;
    private JButton btnHetHan;
    private JButton btnTamNgung;

    private JTable tblLoHang;
    private DefaultTableModel tableModel;

    private TrangThaiFilter filter = TrangThaiFilter.TAT_CA;

    public ManHinhLoHang() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Color.decode("#F4F6F8"));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        napCacheSanPham();
        taiDuLieu();
    }

    private JPanel createHeader() {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        JPanel row1 = new JPanel(new BorderLayout(12, 0));
        row1.setOpaque(false);
        row1.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ LÔ HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(new Color(22, 54, 97));

        lblHeaderAlert = new JLabel(" 0 lô gần hết hạn! ");
        lblHeaderAlert.setOpaque(true);
        lblHeaderAlert.setBackground(new Color(238, 108, 112));
        lblHeaderAlert.setForeground(Color.WHITE);
        lblHeaderAlert.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeaderAlert.setBorder(new EmptyBorder(4, 10, 4, 10));

        left.add(lblTitle);
        left.add(lblHeaderAlert);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(240, 40));
        txtSearch.setToolTipText("Tìm mã lô, tên sản phẩm...");
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(196, 208, 223), 1, true),
                new EmptyBorder(0, 12, 0, 12)
        ));
        txtSearch.addActionListener(e -> refreshTable());

        JButton btnRefresh = createDarkButton("⟳ Làm mới");
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            filter = TrangThaiFilter.TAT_CA;
            taiDuLieu();
        });

        JButton btnNear90 = createOutlineButton("■ Chỉ sắp hết hạn (≤90 ngày)");
        btnNear90.addActionListener(e -> {
            filter = TrangThaiFilter.GAN_HET_HAN_90;
            setActiveFilterButton(null);
            refreshTable();
        });

        JButton btnNhapLo = createDangerButton("+ Nhập lô hàng");
        btnNhapLo.addActionListener(e -> openNhapLoDialog());

        right.add(txtSearch);
        right.add(btnRefresh);
        right.add(btnNear90);
        right.add(btnNhapLo);

        row1.add(left, BorderLayout.WEST);
        row1.add(right, BorderLayout.EAST);

        JPanel row2 = new JPanel(new GridLayout(1, 4, 14, 0));
        row2.setOpaque(false);
        row2.setBorder(new EmptyBorder(0, 0, 14, 0));

        lblExpired = statValueLabel();
        lblNear = statValueLabel();
        lblWarning = statValueLabel();
        lblGood = statValueLabel();

        row2.add(createStatCard("Đã hết hạn", lblExpired, new Color(255, 245, 246), new Color(220, 38, 38)));
        row2.add(createStatCard("Gần hết hạn (≤30 ngày)", lblNear, new Color(255, 249, 237), new Color(234, 88, 12)));
        row2.add(createStatCard("Cảnh báo (31-90 ngày)", lblWarning, new Color(255, 252, 232), new Color(202, 138, 4)));
        row2.add(createStatCard("Còn hạn tốt (>90 ngày)", lblGood, new Color(240, 253, 244), new Color(22, 163, 74)));

        JPanel row3 = new JPanel(new BorderLayout());
        row3.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel lblFilter = new JLabel("Tình trạng:");
        lblFilter.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblFilter.setForeground(new Color(75, 85, 99));

        btnTatCa = createFilterButton("Tất cả");
        btnDuocBan = createFilterButton("Được bán");
        btnHetHan = createFilterButton("Hết hạn");
        btnTamNgung = createFilterButton("Tạm ngừng");

        btnTatCa.addActionListener(e -> switchFilter(TrangThaiFilter.TAT_CA, btnTatCa));
        btnDuocBan.addActionListener(e -> switchFilter(TrangThaiFilter.DUOC_BAN, btnDuocBan));
        btnHetHan.addActionListener(e -> switchFilter(TrangThaiFilter.HET_HAN, btnHetHan));
        btnTamNgung.addActionListener(e -> switchFilter(TrangThaiFilter.TAM_NGUNG, btnTamNgung));

        filterPanel.add(lblFilter);
        filterPanel.add(btnTatCa);
        filterPanel.add(btnDuocBan);
        filterPanel.add(btnHetHan);
        filterPanel.add(btnTamNgung);

        lblTotal = new JLabel("0 / 0 lô hàng");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotal.setForeground(new Color(100, 116, 139));

        row3.add(filterPanel, BorderLayout.WEST);
        row3.add(lblTotal, BorderLayout.EAST);

        wrapper.add(row1);
        wrapper.add(row2);
        wrapper.add(row3);

        setActiveFilterButton(btnTatCa);
        return wrapper;
    }

    private JScrollPane createTablePanel() {
        String[] cols = {"#", "Mã lô", "Tên sản phẩm", "Tồn kho", "Giá nhập", "Hạn sử dụng", "Còn lại", "Tình trạng", "Xóa"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblLoHang = new JTable(tableModel);
        tblLoHang.setRowHeight(48);
        tblLoHang.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblLoHang.setSelectionBackground(new Color(235, 244, 255));
        tblLoHang.setGridColor(new Color(230, 235, 242));
        tblLoHang.setShowVerticalLines(false);
        tblLoHang.setDefaultRenderer(Object.class, new LoHangCellRenderer());
        tblLoHang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tblLoHang.getTableHeader().setBackground(new Color(214, 231, 246));
        tblLoHang.getTableHeader().setForeground(new Color(55, 65, 81));

        tblLoHang.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblLoHang.rowAtPoint(e.getPoint());
                int col = tblLoHang.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 8) {
                    String soLo = String.valueOf(tblLoHang.getValueAt(row, 1));
                    xoaLo(soLo);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblLoHang);
        scroll.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private void taiDuLieu() {
        dsTatCa.clear();
        try {
            List<LoHang> ds = daoLoHang.layDSLoHang();
            if (ds != null) dsTatCa.addAll(ds);
            updateStats();
            refreshTable();
            setActiveFilterButton(btnTatCa);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Không tải được dữ liệu lô hàng.\n" + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void napCacheSanPham() {
        cacheSanPham.clear();
        try {
            List<SanPham> ds = daoSanPham.getDsThuoc();
            if (ds != null) {
                for (SanPham sp : ds) {
                    cacheSanPham.put(sp.getId(), sp);
                }
            }
        } catch (Exception e) {
            System.out.println("Không tải được cache sản phẩm: " + e.getMessage());
        }
    }

    private void refreshTable() {
        List<LoHang> filtered = dsTatCa.stream()
                .filter(this::matchFilter)
                .filter(this::matchKeyword)
                .sorted(Comparator.comparing(this::getNgayHetHanSafe))
                .collect(Collectors.toList());

        tableModel.setRowCount(0);
        int stt = 1;
        for (LoHang lo : filtered) {
            tableModel.addRow(new Object[]{
                    stt++,
                    safe(lo.getSoLoHang()),
                    getTenSanPham(lo),
                    formatNumber(lo.getSoLuongLoHang()),
                    formatCurrency(lo.getGia()),
                    formatDate(lo.getNgayHetHan()),
                    getConLaiText(lo),
                    getTrangThaiText(lo),
                    "🗑"
            });
        }
        lblTotal.setText(filtered.size() + " / " + dsTatCa.size() + " lô hàng");
    }

    private boolean matchKeyword(LoHang lo) {
        String kw = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) return true;
        return safe(lo.getSoLoHang()).toLowerCase().contains(kw)
                || getTenSanPham(lo).toLowerCase().contains(kw);
    }

    private boolean matchFilter(LoHang lo) {
        long days = getRemainingDays(lo);
        boolean expired = isExpired(lo);
        boolean paused = isPaused(lo);
        boolean available = !expired && !paused;

        switch (filter) {
            case DUOC_BAN:
                return available;
            case HET_HAN:
                return expired;
            case TAM_NGUNG:
                return paused;
            case GAN_HET_HAN_90:
                return !expired && days <= 90;
            default:
                return true;
        }
    }

    private void updateStats() {
        long expired = dsTatCa.stream().filter(this::isExpired).count();
        long near = dsTatCa.stream().filter(lo -> !isExpired(lo) && getRemainingDays(lo) <= 30).count();
        long warning = dsTatCa.stream().filter(lo -> !isExpired(lo) && getRemainingDays(lo) >= 31 && getRemainingDays(lo) <= 90).count();
        long good = dsTatCa.stream().filter(lo -> !isExpired(lo) && getRemainingDays(lo) > 90).count();

        lblExpired.setText(String.valueOf(expired));
        lblNear.setText(String.valueOf(near));
        lblWarning.setText(String.valueOf(warning));
        lblGood.setText(String.valueOf(good));
        lblHeaderAlert.setText(" " + (near + warning) + " lô gần hết hạn! ");
    }

    private void openNhapLoDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nhập lô hàng mới", true);
        dialog.setSize(620, 430);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getRootPane().setBorder(new LineBorder(new Color(220, 226, 234), 1, true));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 66, 110));
        header.setBorder(new EmptyBorder(16, 18, 16, 18));
        JLabel lbl = new JLabel("📦 Nhập lô hàng mới");
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.add(lbl, BorderLayout.WEST);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> cboSanPham = new JComboBox<>();
        for (SanPham sp : cacheSanPham.values()) {
            cboSanPham.addItem(sp.getId() + " - " + safe(sp.getTen()));
        }

        JTextField txtSoLo = new JTextField();
        JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{"Được bán", "Tạm ngừng"});
        JTextField txtSoLuong = new JTextField("0");
        JTextField txtGiaNhap = new JTextField("0");
        JTextField txtHanSuDung = new JTextField();
        txtHanSuDung.setToolTipText("Định dạng: dd/MM/yyyy");

        addField(form, gbc, 0, "Sản phẩm *", cboSanPham, 2);
        addField(form, gbc, 1, "Mã lô *", txtSoLo, 1);
        addField(form, gbc, 1, "Tình trạng", cboTrangThai, 1, 1);
        addField(form, gbc, 2, "Số lượng *", txtSoLuong, 1);
        addField(form, gbc, 2, "Giá nhập (đ/ĐV)", txtGiaNhap, 1, 1);
        addField(form, gbc, 3, "Hạn sử dụng *", txtHanSuDung, 2);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(new Color(248, 250, 252));
        JButton btnHuy = createOutlineButton("Hủy");
        JButton btnLuu = createBlueButton("+ Thêm lô hàng");

        btnHuy.addActionListener(e -> dialog.dispose());
        btnLuu.addActionListener(e -> {
            try {
                if (cboSanPham.getSelectedItem() == null) {
                    throw new IllegalArgumentException("Chưa có dữ liệu sản phẩm để nhập lô.");
                }
                String selected = String.valueOf(cboSanPham.getSelectedItem());
                String sanPhamId = selected.split(" - ")[0].trim();
                String soLo = txtSoLo.getText().trim();
                int soLuong = Integer.parseInt(txtSoLuong.getText().trim());
                int gia = Integer.parseInt(txtGiaNhap.getText().trim());
                LocalDate hsd = LocalDate.parse(txtHanSuDung.getText().trim(), dateFormat);
                boolean tamNgung = "Tạm ngừng".equals(String.valueOf(cboTrangThai.getSelectedItem()));

                if (soLo.isEmpty()) throw new IllegalArgumentException("Mã lô không được rỗng.");
                if (soLuong < 0) throw new IllegalArgumentException("Số lượng phải >= 0.");
                if (gia < 0) throw new IllegalArgumentException("Giá nhập phải >= 0.");

                LoHang lo = new LoHang();
                lo.setId(UUID.randomUUID().toString());
                lo.setSoLoHang(soLo);
                lo.setSoLuongLoHang(soLuong);
                lo.setGia(gia);
                lo.setNgayNhap(LocalDateTime.now());
                lo.setNgayHetHan(hsd.atStartOfDay());

                SanPham sp = cacheSanPham.get(sanPhamId);
                if (sp == null) {
                    sp = new SanPham();
                    sp.setId(sanPhamId);
                }
                lo.setSanPhamId(sp);

                if (hsd.isBefore(LocalDate.now())) {
                    lo.setTrangThai(TrangThaiLoHang.HET_HAN);
                } else if (tamNgung || soLuong == 0) {
                    lo.setTrangThai(TrangThaiLoHang.HET_HANG);
                } else {
                    lo.setTrangThai(TrangThaiLoHang.CON_HANG);
                }

                boolean inserted = false;
                try {
                    inserted = daoLoHang.themLoHang(lo);
                } catch (Exception ex) {
                    System.out.println("DAO_LoHang chưa có themLoHang(lo): " + ex.getMessage());
                }

                if (inserted) {
                    JOptionPane.showMessageDialog(dialog, "Đã thêm lô hàng mới.");
                    dialog.dispose();
                    taiDuLieu();
                } else {
                    dsTatCa.add(lo);
                    JOptionPane.showMessageDialog(dialog,
                            "Đã thêm vào giao diện.\nNếu muốn lưu DB thật, thêm method DAO_LoHang.themLoHang(lo).",
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    updateStats();
                    refreshTable();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Lỗi nhập lô", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(btnHuy);
        footer.add(btnLuu);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void xoaLo(String soLo) {
        LoHang lo = dsTatCa.stream().filter(x -> soLo.equals(x.getSoLoHang())).findFirst().orElse(null);
        if (lo == null) return;

        int choose = JOptionPane.showConfirmDialog(this,
                "Ngừng bán / xóa lô " + soLo + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (choose != JOptionPane.YES_OPTION) return;

        boolean ok = false;
        try {
            ok = daoLoHang.capNhatTrangThaiLo(lo.getId(), TrangThaiLoHang.HET_HANG);
        } catch (Exception ex) {
            System.out.println("DAO update trạng thái lỗi: " + ex.getMessage());
        }

        if (ok) {
            taiDuLieu();
        } else {
            dsTatCa.remove(lo);
            updateStats();
            refreshTable();
        }
    }

    private void switchFilter(TrangThaiFilter newFilter, JButton source) {
        this.filter = newFilter;
        setActiveFilterButton(source);
        refreshTable();
    }

    private void setActiveFilterButton(JButton active) {
        JButton[] list = {btnTatCa, btnDuocBan, btnHetHan, btnTamNgung};
        for (JButton b : list) {
            if (b == null) continue;
            b.setBackground(Color.WHITE);
            b.setForeground(new Color(71, 85, 105));
            b.setBorder(new EmptyBorder(9, 16, 9, 16));
        }
        if (active != null) {
            active.setBackground(new Color(37, 99, 235));
            active.setForeground(Color.WHITE);
        }
    }

    private String getTenSanPham(LoHang lo) {
        if (lo.getSanPhamId() == null || lo.getSanPhamId().getId() == null) return "Chưa có sản phẩm";
        String id = lo.getSanPhamId().getId();
        SanPham sp = cacheSanPham.get(id);
        if (sp != null && sp.getTen() != null) return sp.getTen();
        try {
            sp = daoSanPham.getSanPhamTheoMa(id);
            if (sp != null) {
                cacheSanPham.put(id, sp);
                return safe(sp.getTen());
            }
        } catch (Exception ignored) {}
        return id;
    }

    private boolean isExpired(LoHang lo) {
        return getRemainingDays(lo) < 0 || lo.getTrangThai() == TrangThaiLoHang.HET_HAN;
    }

    private boolean isPaused(LoHang lo) {
        return lo.getSoLuongLoHang() <= 0 || lo.getTrangThai() == TrangThaiLoHang.HET_HANG;
    }

    private long getRemainingDays(LoHang lo) {
        return ChronoUnit.DAYS.between(LocalDate.now(), getNgayHetHanSafe(lo));
    }

    private LocalDate getNgayHetHanSafe(LoHang lo) {
        LocalDateTime dt = lo.getNgayHetHan();
        return dt == null ? LocalDate.MAX : dt.toLocalDate();
    }

    private String getConLaiText(LoHang lo) {
        long d = getRemainingDays(lo);
        if (d < 0) return "Quá " + Math.abs(d) + " ngày";
        return d + " ngày";
    }

    private String getTrangThaiText(LoHang lo) {
        if (isExpired(lo)) return "Hết hạn";
        if (isPaused(lo)) return "Tạm ngừng";
        return "Được bán";
    }

    private String formatDate(LocalDateTime dt) {
        if (dt == null) return "--/--/----";
        return dt.toLocalDate().format(dateFormat);
    }

    private String formatCurrency(int value) {
        return numberVN.format(value) + "đ";
    }

    private String formatNumber(int value) {
        return numberVN.format(value);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private JPanel createStatCard(String title, JLabel value, Color bg, Color accent) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(bg);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(228, 232, 237), 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setForeground(new Color(83, 97, 114));
        value.setForeground(accent);
        p.add(t, BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private JLabel statValueLabel() {
        JLabel lbl = new JLabel("0");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 40));
        return lbl;
    }

    private JButton createFilterButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 16, 9, 16));
        return btn;
    }

    private JButton createDarkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(75, 85, 99));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        return btn;
    }

    private JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(239, 68, 68));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        return btn;
    }

    private JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(51, 65, 85));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(214, 220, 230), 1, true),
                new EmptyBorder(10, 18, 10, 18)
        ));
        return btn;
    }

    private JButton createBlueButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(37, 99, 235));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        return btn;
    }

    private void addField(JPanel form, GridBagConstraints gbc, int row, String label, JComponent comp, int width) {
        addField(form, gbc, row, label, comp, width, 0);
    }

    private void addField(JPanel form, GridBagConstraints gbc, int row, String label, JComponent comp, int width, int colStart) {
        gbc.gridx = colStart;
        gbc.gridy = row * 2;
        gbc.gridwidth = width;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(75, 85, 99));
        form.add(lbl, gbc);

        gbc.gridy = row * 2 + 1;
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        if (comp instanceof JTextField) {
            ((JTextField) comp).setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(203, 213, 225), 1, true),
                    new EmptyBorder(10, 12, 10, 12)
            ));
        }
        form.add(comp, gbc);
    }

    private enum TrangThaiFilter {
        TAT_CA,
        DUOC_BAN,
        HET_HAN,
        TAM_NGUNG,
        GAN_HET_HAN_90
    }

    private class LoHangCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
            if (!isSelected) {
                lbl.setBackground(Color.WHITE);
                lbl.setForeground(new Color(45, 55, 72));
            }

            if (column == 1) {
                lbl.setForeground(new Color(0, 82, 204));
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            } else if (column == 6) {
                String text = String.valueOf(value);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                if (text.startsWith("Quá")) lbl.setForeground(new Color(220, 38, 38));
                else lbl.setForeground(new Color(234, 88, 12));
            } else if (column == 7) {
                String text = String.valueOf(value);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                if ("Được bán".equals(text)) lbl.setForeground(new Color(22, 163, 74));
                else if ("Hết hạn".equals(text)) lbl.setForeground(new Color(220, 38, 38));
                else lbl.setForeground(new Color(245, 158, 11));
            } else if (column == 8) {
                lbl.setForeground(new Color(239, 68, 68));
                lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
            }
            return lbl;
        }
    }
}

