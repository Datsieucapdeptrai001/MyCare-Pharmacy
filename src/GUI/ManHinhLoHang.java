package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ManHinhLoHang extends JPanel {

    private final List<BatchItem> dsTatCa = new ArrayList<>();

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

    private JTable table;
    private DefaultTableModel tableModel;

    private TrangThaiFilter filter = TrangThaiFilter.TAT_CA;

    public ManHinhLoHang() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Color.decode("#F4F6F8"));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);

        loadMockData();
        updateStats();
        refreshTable();
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
            setActiveFilterButton(btnTatCa);
            refreshTable();
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

        table = new JTable(tableModel);
        table.setRowHeight(48);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(235, 244, 255));
        table.setGridColor(new Color(230, 235, 242));
        table.setShowVerticalLines(false);
        table.setDefaultRenderer(Object.class, new LoHangCellRenderer());
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(214, 231, 246));
        table.getTableHeader().setForeground(new Color(55, 65, 81));

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 8) {
                    String soLo = String.valueOf(table.getValueAt(row, 1));
                    xoaLo(soLo);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private void loadMockData() {
        dsTatCa.clear();
        dsTatCa.add(new BatchItem("OM001-2023", "Omega 3 Fish Oil", 900, 22000, "30/11/2025", "Quá 130 ngày", "Được bán"));
        dsTatCa.add(new BatchItem("VC001-2023", "Vitamin C 1000mg", 2000, 4500, "31/12/2025", "Quá 99 ngày", "Được bán"));
        dsTatCa.add(new BatchItem("PB001-2024", "Probiotic Lactomin Plus", 1800, 8000, "31/01/2026", "Quá 68 ngày", "Được bán"));
        dsTatCa.add(new BatchItem("RO001-2024", "Thuốc nhỏ mắt Rohto", 1500, 28000, "28/02/2026", "Quá 40 ngày", "Được bán"));
        dsTatCa.add(new BatchItem("DX001-2024", "Dextromethorphan 15mg", 2800, 900, "31/03/2026", "Quá 9 ngày", "Được bán"));
        dsTatCa.add(new BatchItem("OR001-2024", "Thuốc bột ORS", 8000, 3500, "31/03/2026", "Quá 9 ngày", "Được bán"));
        dsTatCa.add(new BatchItem("PR001-2024", "Siro ho Prospan", 800, 85000, "30/04/2026", "21 ngày", "Được bán"));
        dsTatCa.add(new BatchItem("CF001-2024", "Cefuroxime 500mg", 2000, 18000, "30/04/2026", "21 ngày", "Được bán"));
        dsTatCa.add(new BatchItem("AM001-2024", "Amlodipine 5mg", 5000, 1500, "31/05/2026", "52 ngày", "Được bán"));
        dsTatCa.add(new BatchItem("VC002-2023", "Vitamin C 1000mg", 1500, 4800, "30/06/2026", "82 ngày", "Được bán"));
        dsTatCa.add(new BatchItem("PA001-2024", "Paracetamol 500mg", 5000, 800, "30/06/2026", "82 ngày", "Được bán"));
        dsTatCa.add(new BatchItem("AZ001-2024", "Azithromycin 250mg", 2500, 12000, "30/06/2026", "82 ngày", "Được bán"));
    }

    private void refreshTable() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();

        List<BatchItem> filtered = new ArrayList<>();
        for (BatchItem item : dsTatCa) {
            boolean matchKw = keyword.isEmpty()
                    || item.soLo.toLowerCase().contains(keyword)
                    || item.tenSanPham.toLowerCase().contains(keyword);

            boolean matchFilter = switch (filter) {
                case DUOC_BAN -> "Được bán".equals(item.trangThai);
                case HET_HAN -> item.conLai.startsWith("Quá");
                case TAM_NGUNG -> "Tạm ngừng".equals(item.trangThai);
                case GAN_HET_HAN_90 -> !item.conLai.startsWith("Quá") && parseDays(item.conLai) <= 90;
                default -> true;
            };

            if (matchKw && matchFilter) filtered.add(item);
        }

        tableModel.setRowCount(0);
        int stt = 1;
        for (BatchItem item : filtered) {
            tableModel.addRow(new Object[]{
                    stt++,
                    item.soLo,
                    item.tenSanPham,
                    formatNumber(item.tonKho),
                    formatCurrency(item.giaNhap),
                    item.hanSuDung,
                    item.conLai,
                    item.trangThai,
                    "🗑"
            });
        }

        lblTotal.setText(filtered.size() + " / " + dsTatCa.size() + " lô hàng");
    }

    private void updateStats() {
        int expired = 0;
        int near = 0;
        int warning = 0;
        int good = 0;

        for (BatchItem item : dsTatCa) {
            if (item.conLai.startsWith("Quá")) {
                expired++;
            } else {
                int days = parseDays(item.conLai);
                if (days <= 30) near++;
                else if (days <= 90) warning++;
                else good++;
            }
        }

        lblExpired.setText(String.valueOf(expired));
        lblNear.setText(String.valueOf(near));
        lblWarning.setText(String.valueOf(warning));
        lblGood.setText(String.valueOf(good));
        lblHeaderAlert.setText(" " + (near + warning) + " lô gần hết hạn! ");
    }

    private int parseDays(String text) {
        try {
            return Integer.parseInt(text.replace("ngày", "").trim());
        } catch (Exception e) {
            return 999;
        }
    }

    private void openNhapLoDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nhập lô hàng mới", true);
        dialog.setSize(620, 430);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 66, 110));
        header.setBorder(new EmptyBorder(16, 18, 16, 18));
        JLabel lbl = new JLabel("📦 Nhập lô hàng mới");
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.add(lbl, BorderLayout.WEST);

        JPanel form = new JPanel(new GridLayout(6, 2, 12, 12));
        form.setBorder(new EmptyBorder(18, 18, 18, 18));
        form.setBackground(Color.WHITE);

        JTextField txtTenSP = new JTextField();
        JTextField txtSoLo = new JTextField();
        JTextField txtSoLuong = new JTextField("0");
        JTextField txtGiaNhap = new JTextField("0");
        JTextField txtHanSuDung = new JTextField("dd/MM/yyyy");
        JComboBox<String> cboTrangThai = new JComboBox<>(new String[]{"Được bán", "Tạm ngừng"});

        form.add(new JLabel("Tên sản phẩm *"));
        form.add(txtTenSP);
        form.add(new JLabel("Mã lô *"));
        form.add(txtSoLo);
        form.add(new JLabel("Số lượng *"));
        form.add(txtSoLuong);
        form.add(new JLabel("Giá nhập (đ/ĐV)"));
        form.add(txtGiaNhap);
        form.add(new JLabel("Hạn sử dụng *"));
        form.add(txtHanSuDung);
        form.add(new JLabel("Tình trạng"));
        form.add(cboTrangThai);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(new Color(248, 250, 252));

        JButton btnHuy = createOutlineButton("Hủy");
        JButton btnThem = createBlueButton("+ Thêm lô hàng");

        btnHuy.addActionListener(e -> dialog.dispose());
        btnThem.addActionListener(e -> {
            try {
                String tenSP = txtTenSP.getText().trim();
                String soLo = txtSoLo.getText().trim();
                int soLuong = Integer.parseInt(txtSoLuong.getText().trim());
                int gia = Integer.parseInt(txtGiaNhap.getText().trim());
                String hsd = txtHanSuDung.getText().trim();
                String trangThai = String.valueOf(cboTrangThai.getSelectedItem());

                if (tenSP.isEmpty() || soLo.isEmpty() || hsd.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Nhập đầy đủ thông tin bắt buộc.");
                    return;
                }

                String conLai = tinhConLai(hsd);
                dsTatCa.add(new BatchItem(soLo, tenSP, soLuong, gia, hsd, conLai, trangThai));

                updateStats();
                refreshTable();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Dữ liệu không hợp lệ.");
            }
        });

        footer.add(btnHuy);
        footer.add(btnThem);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private String tinhConLai(String hsd) {
        try {
            LocalDate date = LocalDate.parse(hsd, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), date);
            if (days < 0) return "Quá " + Math.abs(days) + " ngày";
            return days + " ngày";
        } catch (Exception e) {
            return "90 ngày";
        }
    }

    private void xoaLo(String soLo) {
        dsTatCa.removeIf(x -> x.soLo.equals(soLo));
        updateStats();
        refreshTable();
    }

    private void switchFilter(TrangThaiFilter newFilter, JButton source) {
        filter = newFilter;
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

    private String formatCurrency(int value) {
        return String.format("%,dđ", value).replace(",", ",");
    }

    private String formatNumber(int value) {
        return String.format("%,d", value).replace(",", ",");
    }

    private enum TrangThaiFilter {
        TAT_CA, DUOC_BAN, HET_HAN, TAM_NGUNG, GAN_HET_HAN_90
    }

    private static class BatchItem {
        String soLo;
        String tenSanPham;
        int tonKho;
        int giaNhap;
        String hanSuDung;
        String conLai;
        String trangThai;

        BatchItem(String soLo, String tenSanPham, int tonKho, int giaNhap, String hanSuDung, String conLai, String trangThai) {
            this.soLo = soLo;
            this.tenSanPham = tenSanPham;
            this.tonKho = tonKho;
            this.giaNhap = giaNhap;
            this.hanSuDung = hanSuDung;
            this.conLai = conLai;
            this.trangThai = trangThai;
        }
    }

    private static class LoHangCellRenderer extends DefaultTableCellRenderer {
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