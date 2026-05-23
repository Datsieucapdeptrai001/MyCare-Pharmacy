package GUI;

import BUS.BUS_KiemKeKho;
import BUS.BUS_KiemKeKho.KetQuaKiemKe;
import BUS.BUS_KiemKeKho.KiemKeItem;
import Utils.MenuIcon;
import Utils.SessionDangNhap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
public class ManHinhKiemKeKho extends JDialog {

    public interface ReloadListener {
        void onReload();
    }

    private static final Color BG_TRANSPARENT = new Color(0, 0, 0, 0);
    private static final Color PRIMARY = new Color(14, 116, 144);
    private static final Color PRIMARY_DARK = new Color(8, 96, 120);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color WARNING = new Color(245, 158, 11);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color HEADER_TABLE = new Color(219, 234, 254);
    private static final Color ROW_SELECTED = new Color(224, 242, 254);

    private final BUS_KiemKeKho busKiemKeKho = new BUS_KiemKeKho();
    private final ReloadListener reloadListener;

    private JComboBox<String> cbKhoHang;
    private JTextField txtSearch;
    private JTextField txtGhiChu;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblTongDong;
    private JLabel lblTongChenhLech;
    private JButton btnTimLo;
    private JButton btnNhapExcel;
    private JButton btnXuatMau;
    private JButton btnLamMoi;
    private JButton btnDong;
    private JButton btnLuu;
    private List<KiemKeItem> dsGoc = new ArrayList<>();
    private List<KiemKeItem> dsDangHienThi = new ArrayList<>();

    private boolean dangCapNhatBang = false;

    public ManHinhKiemKeKho(Window owner, ReloadListener reloadListener) {
        super(owner, "Kiểm kê kho", ModalityType.APPLICATION_MODAL);
        this.reloadListener = reloadListener;

        setUndecorated(true);
        setBackground(BG_TRANSPARENT);
        setContentPane(createMainUI());

        setSize(1180, 760);
        setLocationRelativeTo(owner);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));

        loadDanhSachKho();
        loadDataTheoKho();
        setupPhimTatNghiepVuKiemKe();
    }

    private JPanel createMainUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(PRIMARY, 2));

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createBody(), BorderLayout.CENTER);
        root.add(createFooter(), BorderLayout.SOUTH);

        return root;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(14, 22, 14, 18));

        JLabel title = new JLabel("KIỂM KÊ KHO");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JButton btnClose = new JButton("×");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 26));
        btnClose.setForeground(Color.WHITE);
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> closeDialog());

        MouseAdapter dragWindow = new MouseAdapter() {
            int x;
            int y;

            @Override
            public void mousePressed(MouseEvent e) {
                x = e.getX();
                y = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(getLocation().x + e.getX() - x, getLocation().y + e.getY() - y);
            }
        };

        header.addMouseListener(dragWindow);
        header.addMouseMotionListener(dragWindow);

        header.add(title, BorderLayout.WEST);
        header.add(btnClose, BorderLayout.EAST);

        return header;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setBackground(new Color(248, 250, 252));
        body.setBorder(new EmptyBorder(18, 22, 18, 22));

        body.add(createToolbar(), BorderLayout.NORTH);
        body.add(createTablePanel(), BorderLayout.CENTER);
        body.add(createSummaryPanel(), BorderLayout.SOUTH);

        return body;
    }

    private JPanel createToolbar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblKho = new JLabel("Kho kiểm kê:");
        lblKho.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKho.setForeground(TEXT_PRIMARY);

        cbKhoHang = new JComboBox<>();
        cbKhoHang.setPreferredSize(new Dimension(155, 38));
        cbKhoHang.setMaximumSize(new Dimension(155, 38));
        cbKhoHang.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbKhoHang.setEditable(false);
        cbKhoHang.enableInputMethods(false);
        cbKhoHang.setFocusable(false);
        cbKhoHang.addActionListener(e -> loadDataTheoKho());

        JLabel lblSearch = new JLabel("Mã lô / mã vạch:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSearch.setForeground(TEXT_PRIMARY);

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(300, 38));
        txtSearch.setMaximumSize(new Dimension(300, 38));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setToolTipText("F2: tìm lô đã quét bằng điện thoại");
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 12, 0, 12)
        ));
        txtSearch.enableInputMethods(true);
        txtSearch.getDocument().addDocumentListener(new SimpleDocumentListener(this::filterData));

        btnTimLo = createModernButton("Tìm lô [F2]", PRIMARY);
        btnTimLo.setIcon(new MenuIcon("SEARCH", 18, Color.WHITE));
        btnTimLo.setIconTextGap(8);
        btnTimLo.setPreferredSize(new Dimension(145, 38));
        btnTimLo.setMaximumSize(new Dimension(145, 38));
        btnTimLo.addActionListener(e -> focusTimLo());

        btnLamMoi = createModernButton("Làm mới [F6]", new Color(71, 85, 105));
        btnLamMoi.setIcon(new MenuIcon("REFRESH", 18, Color.WHITE));
        btnLamMoi.setIconTextGap(8);
        btnLamMoi.setPreferredSize(new Dimension(145, 38));
        btnLamMoi.setMaximumSize(new Dimension(145, 38));
        btnLamMoi.addActionListener(e -> loadDataTheoKho());

        row1.add(lblKho);
        row1.add(cbKhoHang);
        row1.add(Box.createHorizontalStrut(4));
        row1.add(lblSearch);
        row1.add(txtSearch);
        row1.add(btnTimLo);
        row1.add(btnLamMoi);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row2.setOpaque(false);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblExcel = new JLabel("File kiểm kê:");
        lblExcel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblExcel.setForeground(TEXT_PRIMARY);

        btnNhapExcel = createModernButton("Nhập Excel [F3]", new Color(37, 99, 235));
        btnNhapExcel.setIcon(new MenuIcon("IMPORT", 18, Color.WHITE));
        btnNhapExcel.setIconTextGap(8);
        btnNhapExcel.setPreferredSize(new Dimension(165, 38));
        btnNhapExcel.setMaximumSize(new Dimension(165, 38));
        btnNhapExcel.addActionListener(e -> nhapExcelKiemKe());

        btnXuatMau = createModernButton("Xuất mẫu [F4]", new Color(14, 116, 144));
        btnXuatMau.setIcon(new MenuIcon("EXPORT", 18, Color.WHITE));
        btnXuatMau.setIconTextGap(8);
        btnXuatMau.setPreferredSize(new Dimension(155, 38));
        btnXuatMau.setMaximumSize(new Dimension(155, 38));
        btnXuatMau.addActionListener(e -> xuatMauExcelKiemKe());

        row2.add(lblExcel);
        row2.add(btnNhapExcel);
        row2.add(btnXuatMau);

        panel.add(row1);
        panel.add(Box.createVerticalStrut(10));
        panel.add(row2);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(BORDER));

        String[] columns = {
                "STT",
                "Mã lô",
                "Sản phẩm",
                "Kho",
                "Tồn hệ thống",
                "Tồn thực tế",
                "Chênh lệch",
                "Tình trạng",
                "Lý do / Ghi chú",
                "ID_ẨN"
        };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
            	return column == 5 || column == 7 || column == 8;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 4 || columnIndex == 5 || columnIndex == 6) {
                    return Integer.class;
                }
                return String.class;
            }
        };

        table = new JTable(model);
        styleTable(table);

        table.setDefaultRenderer(Object.class, new KiemKeCellRenderer());
        table.setDefaultRenderer(Integer.class, new KiemKeCellRenderer());

        table.getColumnModel().getColumn(5).setCellEditor(new NumberCellEditor());
        table.getColumnModel().getColumn(7).setCellEditor(createTinhTrangCellEditor());
        table.getColumnModel().getColumn(8).setCellEditor(new TextCellEditor());

        model.addTableModelListener(e -> {
            if (dangCapNhatBang) {
                return;
            }

            int row = e.getFirstRow();
            int col = e.getColumn();

            if (row < 0 || row >= model.getRowCount()) {
                return;
            }

            if (col == 5 || col == 7 || col == 8) {
                capNhatDongSauKhiNhapTon(row);
            }
        });

        setupTableColumns();

        JScrollPane scrollPane = createSmoothScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void setupTableColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(135);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);
        table.getColumnModel().getColumn(7).setPreferredWidth(135);
        table.getColumnModel().getColumn(8).setPreferredWidth(260);

        int hiddenCol = 9;
        table.getColumnModel().getColumn(hiddenCol).setMinWidth(0);
        table.getColumnModel().getColumn(hiddenCol).setMaxWidth(0);
        table.getColumnModel().getColumn(hiddenCol).setWidth(0);
        table.getColumnModel().getColumn(hiddenCol).setPreferredWidth(0);
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        left.setOpaque(false);

        lblTongDong = createSummaryLabel("Số dòng: 0");
        lblTongChenhLech = createSummaryLabel("Tổng chênh lệch: 0");

        left.add(lblTongDong);
        left.add(lblTongChenhLech);

        JPanel right = new JPanel(new BorderLayout(8, 4));
        right.setOpaque(false);

        JLabel lblGhiChu = new JLabel("Ghi chú phiếu kiểm kê:");
        lblGhiChu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblGhiChu.setForeground(TEXT_PRIMARY);

        txtGhiChu = new JTextField();
        txtGhiChu.setPreferredSize(new Dimension(360, 36));
        txtGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtGhiChu.enableInputMethods(true);
        txtGhiChu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(0, 10, 0, 10)
        ));

        right.add(lblGhiChu, BorderLayout.NORTH);
        right.add(txtGhiChu, BorderLayout.CENTER);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);

        return panel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(14, 22, 14, 22)
        ));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);

        btnDong = createModernButton("Đóng [Esc]", new Color(100, 116, 139));
        btnDong.setIcon(new MenuIcon("CLOSE", 18, Color.WHITE));
        btnDong.setIconTextGap(8);

        btnLuu = createModernButton("Lưu phiếu kiểm kê [F10]", SUCCESS);
        btnLuu.setIcon(new MenuIcon("SAVE", 18, Color.WHITE));
        btnLuu.setIconTextGap(8);

        btnDong.addActionListener(e -> closeDialog());
        btnLuu.addActionListener(e -> handleSave());

        actions.add(btnDong);
        actions.add(btnLuu);

        footer.add(actions, BorderLayout.EAST);
        return footer;
    }

    private JLabel createSummaryLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(PRIMARY);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254)),
                new EmptyBorder(8, 14, 8, 14)
        ));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        return label;
    }
    private void setupPhimTatNghiepVuKiemKe() {
        dangKyPhimTat("KK_TIM_LO_F2", KeyEvent.VK_F2, 0, this::focusTimLo);
        dangKyPhimTat("KK_NHAP_EXCEL_F3", KeyEvent.VK_F3, 0, this::nhapExcelKiemKe);
        dangKyPhimTat("KK_XUAT_MAU_F4", KeyEvent.VK_F4, 0, this::xuatMauExcelKiemKe);
        dangKyPhimTat("KK_LAM_MOI_F6", KeyEvent.VK_F6, 0, this::loadDataTheoKho);
        dangKyPhimTat("KK_LUU_F10", KeyEvent.VK_F10, 0, this::handleSave);
        dangKyPhimTat("KK_DONG_ESC", KeyEvent.VK_ESCAPE, 0, this::closeDialog);
    }

    private void dangKyPhimTat(String actionKey, int keyCode, int modifiers, Runnable action) {
        JRootPane rootPane = getRootPane();

        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(keyCode, modifiers), actionKey);

        actionMap.put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (action != null) {
                    action.run();
                }
            }
        });
    }

    private void focusTimLo() {
        if (txtSearch == null) {
            return;
        }

        txtSearch.requestFocusInWindow();
        txtSearch.selectAll();
    }

    private DefaultCellEditor createTinhTrangCellEditor() {
        JComboBox<String> combo = new JComboBox<>(new String[]{
                "Đủ hàng",
                "Thiếu hàng",
                "Dư hàng",
                "Hỏng / vỡ",
                "Không tìm thấy",
                "Sai vị trí",
                "Khác"
        });

        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.enableInputMethods(false);

        return new DefaultCellEditor(combo);
    }
    private String chuanHoaTinhTrang(String tinhTrang) {
        String tt = tinhTrang == null ? "" : tinhTrang.trim();

        if (tt.isEmpty()) {
            return "Đủ hàng";
        }

        String lower = tt.toLowerCase();

        if (lower.contains("du") || lower.contains("đủ")) {
            return "Đủ hàng";
        }

        if (lower.contains("thieu") || lower.contains("thiếu")) {
            return "Thiếu hàng";
        }

        if (lower.contains("du hang") || lower.contains("dư")) {
            return "Dư hàng";
        }

        if (lower.contains("hong") || lower.contains("hỏng") || lower.contains("vo") || lower.contains("vỡ")) {
            return "Hỏng / vỡ";
        }

        if (lower.contains("khong tim") || lower.contains("không tìm")) {
            return "Không tìm thấy";
        }

        if (lower.contains("sai vi tri") || lower.contains("sai vị trí")) {
            return "Sai vị trí";
        }

        return tt;
    }
    private String ghepTinhTrangVaLyDo(String tinhTrang, String lyDo) {
        String tt = chuanHoaTinhTrang(tinhTrang);
        String note = lyDo == null ? "" : lyDo.trim();

        if (note.isEmpty()) {
            return "[" + tt + "]";
        }

        return "[" + tt + "] " + note;
    }

    private String tachTinhTrang(String value, int chenhLech) {
        String text = safe(value).trim();

        if (text.startsWith("[") && text.contains("]")) {
            int end = text.indexOf("]");
            String tt = text.substring(1, end).trim();

            if (!tt.isEmpty()) {
                return tt;
            }
        }

        if (chenhLech < 0) {
            return "Thiếu hàng";
        }

        if (chenhLech > 0) {
            return "Dư hàng";
        }

        return "Đủ hàng";
    }

    private String tachLyDoGoc(String value) {
        String text = safe(value).trim();

        if (text.startsWith("[") && text.contains("]")) {
            int end = text.indexOf("]");

            if (end + 1 < text.length()) {
                return text.substring(end + 1).trim();
            }

            return "";
        }

        return text;
    }

    private String layMaVachNoiBo(KiemKeItem item) {
        if (item == null) {
            return "";
        }

        try {
            Object value = item.getClass().getMethod("getMaVachNoiBo").invoke(item);

            if (value == null) {
                return "";
            }

            return value.toString().trim();
        } catch (Exception ignored) {
            return "";
        }
    }
    private void xuatMauExcelKiemKe() {
        stopEditingIfNeeded();
        dongBoTatCaDongDangHienThi();

        if (dsGoc == null || dsGoc.isEmpty()) {
            showModernAlert("Thông báo", "Không có dữ liệu để xuất mẫu kiểm kê.", WARNING);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Xuất mẫu kiểm kê");
        chooser.setSelectedFile(new File("Mau_KiemKeKho.csv"));

        int result = chooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');

            writer.write("MaLoHoacMaVach,KHO,SanPham,TonHeThong,TonThucTe,TinhTrang,LyDo\n");

            for (KiemKeItem item : dsGoc) {
                writer.write(csv(item.getSoLoHang()));
                writer.write(",");
                writer.write(csv(item.getKhoHangId()));
                writer.write(",");
                writer.write(csv(safe(item.getSanPhamId()) + " - " + safe(item.getTenSanPham())));
                writer.write(",");
                writer.write(csv(String.valueOf(item.getTonHeThong())));
                writer.write(",");
                writer.write(csv(String.valueOf(item.getTonThucTe())));
                writer.write(",");
                writer.write(csv(tachTinhTrang(item.getLyDo(), item.getChenhLech())));
                writer.write(",");
                writer.write(csv(tachLyDoGoc(item.getLyDo())));
                writer.write("\n");
            }

            showModernAlert(
                    "Thành công",
                    "Đã xuất mẫu kiểm kê.\nMở file bằng Excel, nhập Tồn thực tế + Tình trạng + Lý do rồi import lại.",
                    SUCCESS
            );

        } catch (Exception e) {
            e.printStackTrace();
            showModernAlert("Lỗi", "Xuất mẫu kiểm kê thất bại: " + e.getMessage(), DANGER);
        }
    }

    private String csv(Object value) {
        String text = value == null ? "" : value.toString();

        text = text.replace("\"", "\"\"");

        return "\"" + text + "\"";
    }
    private void nhapExcelKiemKe() {
        stopEditingIfNeeded();

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Nhập file kiểm kê CSV");

        int result = chooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        if (file == null || !file.exists()) {
            showModernAlert("Lỗi", "File không tồn tại.", DANGER);
            return;
        }

        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            showModernAlert(
                    "Sai định dạng file",
                    "Chức năng này đang nhận file CSV, không đọc trực tiếp file Excel .xlsx/.xls.\n\n"
                            + "Cách làm đúng:\n"
                            + "1. Bấm Xuất mẫu [F4]\n"
                            + "2. Mở file CSV bằng Excel\n"
                            + "3. Nhập Tồn thực tế, Tình trạng, Lý do\n"
                            + "4. Lưu lại dạng CSV UTF-8\n"
                            + "5. Import lại file CSV đó",
                    WARNING
            );
            return;
        }

        int soDongThanhCong = 0;
        int soDongLoi = 0;
        List<String> loi = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new StringReader(docFileCsvDungTiengViet(file)))) {

            String line;
            int lineNo = 0;

            while ((line = reader.readLine()) != null) {
                lineNo++;

                line = removeBom(line);

                if (line.trim().isEmpty()) {
                    continue;
                }

                List<String> cols = parseCsvLineAuto(line);

                if (cols.isEmpty()) {
                    continue;
                }

                if (laDongTieuDe(cols)) {
                    continue;
                }

                if (cols.size() < 5) {
                    soDongLoi++;
                    loi.add("Dòng " + lineNo + ": thiếu cột dữ liệu. File phải có: Mã lô/mã vạch, Kho, Tồn thực tế, Tình trạng, Lý do.");
                    continue;
                }

                String maLoHoacMaVach;
                String kho;
                String tonText;
                String tinhTrang;
                String lyDo;

                /*
                 * Dạng mẫu chuẩn:
                 * 0 MaLoHoacMaVach
                 * 1 KHO
                 * 2 SanPham
                 * 3 TonHeThong
                 * 4 TonThucTe
                 * 5 TinhTrang
                 * 6 LyDo
                 */
                if (cols.size() >= 7) {
                    maLoHoacMaVach = getCol(cols, 0);
                    kho = getCol(cols, 1);
                    tonText = getCol(cols, 4);
                    tinhTrang = getCol(cols, 5);
                    lyDo = getCol(cols, 6);
                } else {
                    /*
                     * Dạng rút gọn:
                     * 0 MaLoHoacMaVach
                     * 1 KHO
                     * 2 TonThucTe
                     * 3 TinhTrang
                     * 4 LyDo
                     */
                    maLoHoacMaVach = getCol(cols, 0);
                    kho = getCol(cols, 1);
                    tonText = getCol(cols, 2);
                    tinhTrang = getCol(cols, 3);
                    lyDo = getCol(cols, 4);
                }

                if (isBlank(maLoHoacMaVach)) {
                    soDongLoi++;
                    loi.add("Dòng " + lineNo + ": mã lô/mã vạch rỗng");
                    continue;
                }

                int tonThucTe;

                try {
                    String cleaned = tonText.replace(".", "").replace(",", "").replaceAll("[^0-9]", "");

                    if (cleaned.isEmpty()) {
                        throw new NumberFormatException("Tồn thực tế rỗng");
                    }

                    tonThucTe = Integer.parseInt(cleaned);
                } catch (Exception e) {
                    soDongLoi++;
                    loi.add("Dòng " + lineNo + ": tồn thực tế không hợp lệ");
                    continue;
                }

                KiemKeItem item = timItemTheoMaVaKho(maLoHoacMaVach, kho);

                if (item == null) {
                    soDongLoi++;
                    loi.add("Dòng " + lineNo + ": không tìm thấy lô " + maLoHoacMaVach);
                    continue;
                }

                item.setTonThucTe(Math.max(0, tonThucTe));
                item.setLyDo(ghepTinhTrangVaLyDo(tinhTrang, lyDo));
                soDongThanhCong++;
            }

            dsDangHienThi = new ArrayList<>(dsGoc);
            renderTable();

            StringBuilder msg = new StringBuilder();
            msg.append("Đã nhập kiểm kê từ file.\n");
            msg.append("Thành công: ").append(soDongThanhCong).append(" dòng.\n");
            msg.append("Lỗi: ").append(soDongLoi).append(" dòng.");

            if (!loi.isEmpty()) {
                msg.append("\n\nMột số lỗi:\n");

                for (int i = 0; i < Math.min(6, loi.size()); i++) {
                    msg.append("- ").append(loi.get(i)).append("\n");
                }
            }

            showModernAlert("Kết quả nhập file", msg.toString(), soDongLoi == 0 ? SUCCESS : WARNING);

        } catch (Exception e) {
            e.printStackTrace();
            showModernAlert("Lỗi", "Nhập file kiểm kê thất bại: " + e.getMessage(), DANGER);
        }
    }
    private String getCol(List<String> cols, int index) {
        if (cols == null || index < 0 || index >= cols.size()) {
            return "";
        }

        return cols.get(index) == null ? "" : cols.get(index).trim();
    }

    private String removeBom(String line) {
        if (line == null) {
            return "";
        }

        return line.replace("\uFEFF", "");
    }

    private boolean laDongTieuDe(List<String> cols) {
        if (cols == null || cols.isEmpty()) {
            return false;
        }

        String first = getCol(cols, 0).toLowerCase();

        return first.contains("malo")
                || first.contains("mã lô")
                || first.contains("ma lo")
                || first.contains("mavach")
                || first.contains("mã vạch")
                || first.contains("ma vach");
    }

    private List<String> parseCsvLineAuto(String line) {
        List<String> best = parseCsvLineByDelimiter(line, ',');

        List<String> semi = parseCsvLineByDelimiter(line, ';');
        if (semi.size() > best.size()) {
            best = semi;
        }

        List<String> tab = parseCsvLineByDelimiter(line, '\t');
        if (tab.size() > best.size()) {
            best = tab;
        }

        return best;
    }

    private List<String> parseCsvLineByDelimiter(String line, char delimiter) {
        List<String> result = new ArrayList<>();

        if (line == null) {
            return result;
        }

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == delimiter && !inQuotes) {
                result.add(removeBom(current.toString()).trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        result.add(removeBom(current.toString()).trim());

        return result;
    }
    private String docFileCsvDungTiengViet(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());

        String utf8 = new String(bytes, StandardCharsets.UTF_8);

        if (!biLoiEncodingTiengViet(utf8)) {
            return xoaBomNoiDung(utf8);
        }

        String win1258 = new String(bytes, Charset.forName("windows-1258"));

        if (!biLoiEncodingTiengViet(win1258)) {
            return xoaBomNoiDung(win1258);
        }

        String win1252 = new String(bytes, Charset.forName("windows-1252"));

        if (!biLoiEncodingTiengViet(win1252)) {
            return xoaBomNoiDung(win1252);
        }

        return xoaBomNoiDung(utf8);
    }

    private boolean biLoiEncodingTiengViet(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        if (text.contains("�")) {
            return true;
        }

        String lower = text.toLowerCase();

        return lower.contains("ä‘")
                || lower.contains("á»")
                || lower.contains("áº")
                || lower.contains("ã")
                || lower.contains("ð");
    }

    private String xoaBomNoiDung(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("\uFEFF", "");
    }
    
    private KiemKeItem timItemTheoMaVaKho(String maLoHoacMaVach, String kho) {
        String key = safe(maLoHoacMaVach).trim();

        if (key.isEmpty()) {
            return null;
        }

        KiemKeItem found = null;
        int count = 0;

        for (KiemKeItem item : dsGoc) {
            if (item == null) {
                continue;
            }

            boolean khopMa = key.equalsIgnoreCase(safe(item.getSoLoHang()))
                    || key.equalsIgnoreCase(safe(item.getLoHangId()))
                    || key.equalsIgnoreCase(safe(layMaVachNoiBo(item)))
                    || key.equalsIgnoreCase(safe(item.getSanPhamId()));

            boolean khopKho = isBlank(kho)
                    || kho.equalsIgnoreCase(safe(item.getKhoHangId()));

            if (khopMa && khopKho) {
                found = item;
                count++;
            }
        }

        if (count == 1) {
            return found;
        }

        return null;
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();

        if (line == null) {
            return result;
        }

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        result.add(current.toString());

        if (!result.isEmpty() && result.get(0).startsWith("\uFEFF")) {
            result.set(0, result.get(0).replace("\uFEFF", ""));
        }

        return result;
    }
    private void loadDanhSachKho() {
        if (cbKhoHang == null) {
            return;
        }

        cbKhoHang.removeAllItems();

        List<String> dsKho = busKiemKeKho.layDanhSachMaKho();

        if (dsKho != null) {
            for (String maKho : dsKho) {
                if (!isBlank(maKho)) {
                    cbKhoHang.addItem(maKho);
                }
            }
        }
    }

    private void loadDataTheoKho() {
        stopEditingIfNeeded();

        if (cbKhoHang == null || cbKhoHang.getSelectedItem() == null) {
            dsGoc = new ArrayList<>();
            dsDangHienThi = new ArrayList<>();
            renderTable();
            return;
        }

        String khoHangId = cbKhoHang.getSelectedItem().toString();

        if (isBlank(khoHangId)) {
            dsGoc = new ArrayList<>();
            dsDangHienThi = new ArrayList<>();
            renderTable();
            return;
        }

        dsGoc = busKiemKeKho.layDanhSachLoTheoKho(khoHangId);

        if (dsGoc == null) {
            dsGoc = new ArrayList<>();
        }

        dsDangHienThi = new ArrayList<>(dsGoc);
        renderTable();
    }

    private void filterData() {
        stopEditingIfNeeded();

        String keyword = txtSearch.getText() == null
                ? ""
                : txtSearch.getText().trim().toLowerCase();

        dsDangHienThi.clear();

        for (KiemKeItem item : dsGoc) {
        	boolean match = keyword.isEmpty()
        	        || safe(item.getSoLoHang()).toLowerCase().contains(keyword)
        	        || safe(item.getLoHangId()).toLowerCase().contains(keyword)
        	        || safe(layMaVachNoiBo(item)).toLowerCase().contains(keyword)
        	        || safe(item.getSanPhamId()).toLowerCase().contains(keyword)
        	        || safe(item.getTenSanPham()).toLowerCase().contains(keyword)
        	        || safe(item.getKhoHangId()).toLowerCase().contains(keyword);

            if (match) {
                dsDangHienThi.add(item);
            }
        }

        renderTable();
    }

    private void renderTable() {
        if (model == null) {
            return;
        }

        dangCapNhatBang = true;

        try {
            model.setRowCount(0);

            int stt = 1;

            for (KiemKeItem item : dsDangHienThi) {
            	model.addRow(new Object[]{
            	        stt++,
            	        safe(item.getSoLoHang()),
            	        safe(item.getSanPhamId()) + " - " + safe(item.getTenSanPham()),
            	        safe(item.getKhoHangId()),
            	        item.getTonHeThong(),
            	        item.getTonThucTe(),
            	        item.getChenhLech(),
            	        tachTinhTrang(item.getLyDo(), item.getChenhLech()),
            	        tachLyDoGoc(item.getLyDo()),
            	        safe(item.getLoHangId())
            	});
            }
        } finally {
            dangCapNhatBang = false;
        }

        updateSummary();
    }

    private void capNhatDongSauKhiNhapTon(int modelRow) {
        if (dangCapNhatBang) {
            return;
        }

        if (modelRow < 0 || modelRow >= model.getRowCount()) {
            return;
        }

        String loHangId = safe(model.getValueAt(modelRow, 9));
        KiemKeItem item = timItemTheoId(loHangId);

        if (item == null) {
            return;
        }

        int tonThucTe = parseInt(model.getValueAt(modelRow, 5), item.getTonHeThong());

        if (tonThucTe < 0) {
            tonThucTe = 0;
        }

        String tinhTrang = safe(model.getValueAt(modelRow, 7));
        String lyDo = safe(model.getValueAt(modelRow, 8));

        item.setTonThucTe(tonThucTe);
        item.setLyDo(ghepTinhTrangVaLyDo(tinhTrang, lyDo));

        dangCapNhatBang = true;

        try {
            model.setValueAt(item.getChenhLech(), modelRow, 6);

            if (isBlank(tinhTrang)) {
                model.setValueAt(tachTinhTrang(item.getLyDo(), item.getChenhLech()), modelRow, 7);
            }
        } finally {
            dangCapNhatBang = false;
        }

        updateSummary();
    }

    private KiemKeItem timItemTheoId(String loHangId) {
        for (KiemKeItem item : dsGoc) {
            if (safe(item.getLoHangId()).equalsIgnoreCase(safe(loHangId))) {
                return item;
            }
        }

        return null;
    }

    private void updateSummary() {
        if (lblTongDong == null || lblTongChenhLech == null) {
            return;
        }

        int tongChenhLech = 0;

        for (KiemKeItem item : dsGoc) {
            tongChenhLech += item.getChenhLech();
        }

        lblTongDong.setText("Số dòng: " + dsGoc.size());
        lblTongChenhLech.setText("Tổng chênh lệch: " + tongChenhLech);

        if (tongChenhLech == 0) {
            lblTongChenhLech.setForeground(SUCCESS);
        } else if (tongChenhLech > 0) {
            lblTongChenhLech.setForeground(PRIMARY);
        } else {
            lblTongChenhLech.setForeground(DANGER);
        }
    }

    private void handleSave() {
        stopEditingIfNeeded();
        dongBoTatCaDongDangHienThi();

        if (dsGoc == null || dsGoc.isEmpty()) {
            showModernAlert("Thông báo", "Không có dữ liệu kiểm kê để lưu.", WARNING);
            return;
        }

        for (KiemKeItem item : dsGoc) {
            if (item.getTonThucTe() < 0) {
                showModernAlert("Thông báo", "Tồn thực tế không được âm tại lô " + item.getSoLoHang(), WARNING);
                return;
            }

            if (item.getChenhLech() != 0 && isBlank(item.getLyDo())) {
                showModernAlert("Thông báo", "Lô " + item.getSoLoHang() + " có chênh lệch, vui lòng nhập lý do.", WARNING);
                return;
            }
        }

        boolean confirm = showModernConfirm(
                "Xác nhận kiểm kê",
                "Lưu phiếu kiểm kê và cập nhật tồn kho theo tồn thực tế?"
        );

        if (!confirm) {
            return;
        }

        if (cbKhoHang.getSelectedItem() == null) {
            showModernAlert("Thông báo", "Chưa chọn kho kiểm kê.", WARNING);
            return;
        }

        String khoHangId = cbKhoHang.getSelectedItem().toString();
        String nhanVienId = SessionDangNhap.getMaNhanVienOrDefault();
        String ghiChu = txtGhiChu.getText() == null ? "" : txtGhiChu.getText().trim();

        KetQuaKiemKe ketQua = busKiemKeKho.luuPhieuKiemKe(
                khoHangId,
                nhanVienId,
                ghiChu,
                dsGoc
        );

        if (!ketQua.isThanhCong()) {
            showModernAlert("Lỗi", ketQua.getThongBao(), DANGER);
            return;
        }

        boolean printConfirm = showModernConfirm(
                "In phiếu kiểm kê",
                "Đã lưu phiếu kiểm kê " + ketQua.getPhieuKiemKeId()
                        + ".\nBạn có muốn in phiếu kiểm kê không?"
        );

        if (printConfirm) {
            inPhieuKiemKe(
                    ketQua.getPhieuKiemKeId(),
                    khoHangId,
                    nhanVienId,
                    ghiChu,
                    new ArrayList<>(dsGoc)
            );
        } else {
            showModernAlert("Thành công", "Đã lưu phiếu kiểm kê: " + ketQua.getPhieuKiemKeId(), SUCCESS);
        }

        if (reloadListener != null) {
            reloadListener.onReload();
        }

        dispose();
    }

    private void dongBoTatCaDongDangHienThi() {
        if (model == null) {
            return;
        }

        for (int row = 0; row < model.getRowCount(); row++) {
        	String loHangId = safe(model.getValueAt(row, 9));
            KiemKeItem item = timItemTheoId(loHangId);

            if (item == null) {
                continue;
            }

            int tonThucTe = parseInt(model.getValueAt(row, 5), item.getTonHeThong());
            String tinhTrang = safe(model.getValueAt(row, 7));
            String lyDo = safe(model.getValueAt(row, 8));

            item.setTonThucTe(Math.max(0, tonThucTe));
            item.setLyDo(ghepTinhTrangVaLyDo(tinhTrang, lyDo));
        }

        updateSummary();
    }

    private void stopEditingIfNeeded() {
        try {
            if (table != null && table.isEditing() && table.getCellEditor() != null) {
                table.getCellEditor().stopCellEditing();
            }
        } catch (Exception ignored) {
        }
    }

    private void closeDialog() {
        stopEditingIfNeeded();
        dispose();
    }

    private int parseInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        try {
            String text = value.toString().trim().replaceAll("[^0-9]", "");

            if (text.isEmpty()) {
                return defaultValue;
            }

            return Integer.parseInt(text);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private void inPhieuKiemKe(
            String maPhieu,
            String khoHangId,
            String nhanVienId,
            String ghiChu,
            List<KiemKeItem> dsIn
    ) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Phiếu kiểm kê kho " + maPhieu);

        PageFormat pageFormat = job.defaultPage();
        Paper paper = new Paper();

        double width = 595;
        double height = 842;

        paper.setSize(width, height);
        paper.setImageableArea(36, 36, width - 72, height - 72);
        pageFormat.setPaper(paper);
        pageFormat.setOrientation(PageFormat.PORTRAIT);

        job.setPrintable((graphics, pf, pageIndex) -> {
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int itemsPerPage = 18;
            int totalPage = (int) Math.ceil(dsIn.size() / (double) itemsPerPage);

            if (totalPage <= 0) {
                totalPage = 1;
            }

            if (pageIndex >= totalPage) {
                return Printable.NO_SUCH_PAGE;
            }

            int x = (int) pf.getImageableX();
            int y = (int) pf.getImageableY();
            int w = (int) pf.getImageableWidth();

            veHeaderPhieuKiemKe(g2, x, y, w, maPhieu, khoHangId, nhanVienId, ghiChu, pageIndex + 1, totalPage);

            int tableY = y + 160;
            int rowHeight = 28;

            veBangHeaderKiemKe(g2, x, tableY);

            int start = pageIndex * itemsPerPage;
            int end = Math.min(start + itemsPerPage, dsIn.size());

            int currentY = tableY + rowHeight;

            for (int i = start; i < end; i++) {
                KiemKeItem item = dsIn.get(i);
                veDongKiemKe(g2, x, currentY, i + 1, item);
                currentY += rowHeight;
            }

            veFooterPhieuKiemKe(g2, x, y, w, currentY + 35);

            return Printable.PAGE_EXISTS;
        }, pageFormat);

        boolean ok = job.printDialog();

        if (ok) {
            try {
                job.print();
                showModernAlert("Thành công", "Đã gửi phiếu kiểm kê đến máy in.", SUCCESS);
            } catch (PrinterException e) {
                e.printStackTrace();
                showModernAlert("Lỗi", "In phiếu kiểm kê thất bại: " + e.getMessage(), DANGER);
            }
        }
    }

    private void veHeaderPhieuKiemKe(
            Graphics2D g2,
            int x,
            int y,
            int w,
            String maPhieu,
            String khoHangId,
            String nhanVienId,
            String ghiChu,
            int page,
            int totalPage
    ) {
        g2.setColor(Color.BLACK);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2.drawString("MYCARE PHARMACY", x, y + 20);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.drawString("Phiếu kiểm kê kho - Hệ thống quản lý lô hàng", x, y + 38);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
        String title = "PHIẾU KIỂM KÊ KHO";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, x + (w - titleWidth) / 2, y + 70);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        String ngayIn = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());

        int leftX = x;
        int rightX = x + w / 2 + 20;
        int infoY = y + 100;

        g2.drawString("Mã phiếu: " + safe(maPhieu), leftX, infoY);
        g2.drawString("Kho kiểm kê: " + safe(khoHangId), leftX, infoY + 18);
        g2.drawString("Người thực hiện: " + safe(nhanVienId), leftX, infoY + 36);

        g2.drawString("Ngày in: " + ngayIn, rightX, infoY);
        g2.drawString("Trang: " + page + "/" + totalPage, rightX, infoY + 18);

        if (!isBlank(ghiChu)) {
            g2.drawString("Ghi chú: " + catChuoi(ghiChu, 55), leftX, infoY + 58);
        }

        g2.setColor(new Color(180, 180, 180));
        g2.drawLine(x, y + 145, x + w, y + 145);
    }

    private void veBangHeaderKiemKe(Graphics2D g2, int x, int y) {
        int rowHeight = 28;

        int[] colWidths = {
                35,
                95,
                165,
                70,
                70,
                70,
                70
        };

        String[] headers = {
                "STT",
                "Mã lô",
                "Sản phẩm",
                "Tồn HT",
                "Tồn TT",
                "Lệch",
                "Lý do"
        };

        g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
        g2.setColor(new Color(219, 234, 254));
        g2.fillRect(x, y, 575, rowHeight);

        g2.setColor(Color.BLACK);

        int currentX = x;

        for (int i = 0; i < headers.length; i++) {
            g2.drawRect(currentX, y, colWidths[i], rowHeight);
            g2.drawString(headers[i], currentX + 4, y + 18);
            currentX += colWidths[i];
        }
    }

    private void veDongKiemKe(
            Graphics2D g2,
            int x,
            int y,
            int stt,
            KiemKeItem item
    ) {
        int rowHeight = 28;

        int[] colWidths = {
                35,
                95,
                165,
                70,
                70,
                70,
                70
        };

        String sanPham = safe(item.getSanPhamId()) + " - " + safe(item.getTenSanPham());

        String[] values = {
                String.valueOf(stt),
                safe(item.getSoLoHang()),
                catChuoi(sanPham, 26),
                String.valueOf(item.getTonHeThong()),
                String.valueOf(item.getTonThucTe()),
                String.valueOf(item.getChenhLech()),
                catChuoi(safe(item.getLyDo()), 14)
        };

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        g2.setColor(Color.BLACK);

        int currentX = x;

        for (int i = 0; i < values.length; i++) {
            g2.drawRect(currentX, y, colWidths[i], rowHeight);

            if (i >= 3 && i <= 5) {
                int textWidth = g2.getFontMetrics().stringWidth(values[i]);
                g2.drawString(values[i], currentX + colWidths[i] - textWidth - 5, y + 18);
            } else {
                g2.drawString(values[i], currentX + 4, y + 18);
            }

            currentX += colWidths[i];
        }
    }

    private void veFooterPhieuKiemKe(Graphics2D g2, int x, int y, int w, int footerY) {
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        int col1 = x + 50;
        int col2 = x + w / 2 - 40;
        int col3 = x + w - 150;

        g2.drawString("Người kiểm kê", col1, footerY);
        g2.drawString("Thủ kho", col2, footerY);
        g2.drawString("Quản lý", col3, footerY);

        g2.setFont(new Font("Segoe UI", Font.ITALIC, 9));
        g2.drawString("(Ký, ghi rõ họ tên)", col1, footerY + 18);
        g2.drawString("(Ký, ghi rõ họ tên)", col2, footerY + 18);
        g2.drawString("(Ký, ghi rõ họ tên)", col3, footerY + 18);
    }

    private String catChuoi(String text, int max) {
        if (text == null) {
            return "";
        }

        String value = text.trim();

        if (value.length() <= max) {
            return value;
        }

        return value.substring(0, Math.max(0, max - 3)) + "...";
    }

    private String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void styleTable(JTable table) {
        table.setRowHeight(42);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(ROW_SELECTED);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(new Color(241, 245, 249));
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSurrendersFocusOnKeystroke(true);
        table.setFocusable(true);
        table.enableInputMethods(false);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        JTableHeader header = table.getTableHeader();

        if (header != null) {
            header.setFont(new Font("Segoe UI", Font.BOLD, 14));
            header.setBackground(HEADER_TABLE);
            header.setForeground(TEXT_PRIMARY);
            header.setPreferredSize(new Dimension(header.getWidth(), 38));
            header.setReorderingAllowed(false);
            header.enableInputMethods(false);
            header.setFocusable(false);
        }
    }

    private JScrollPane createSmoothScrollPane(Component view) {
        JScrollPane scroll = new JScrollPane(view);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(Color.WHITE);

        scroll.getVerticalScrollBar().setUI(new SmoothScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setBlockIncrement(80);

        scroll.getHorizontalScrollBar().setUI(new SmoothScrollBarUI());
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        scroll.getHorizontalScrollBar().setUnitIncrement(18);
        scroll.getHorizontalScrollBar().setBlockIncrement(80);

        return scroll;
    }

    private JButton createModernButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.enableInputMethods(false);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(color.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(color);
            }
        });

        return btn;
    }

    private boolean showModernConfirm(String title, String message) {
        final boolean[] result = {false};

        JDialog dialog = new JDialog(this, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(PRIMARY, 2));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);

        JButton btnX = new JButton("×");
        btnX.setFont(new Font("Segoe UI", Font.BOLD, 22));
        btnX.setForeground(Color.WHITE);
        btnX.setFocusPainted(false);
        btnX.setBorderPainted(false);
        btnX.setContentAreaFilled(false);
        btnX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnX.addActionListener(e -> dialog.dispose());

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnX, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(22, 26, 18, 26));

        JLabel icon = new JLabel("?");
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setVerticalAlignment(SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 30));
        icon.setForeground(PRIMARY);
        icon.setPreferredSize(new Dimension(52, 52));

        JLabel msg = new JLabel("<html><div style='width:380px; font-family:Segoe UI; font-size:13px; color:#0f172a;'>"
                + escapeHtml(message).replace("\n", "<br>")
                + "</div></html>");

        body.add(icon, BorderLayout.WEST);
        body.add(msg, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footer.setBackground(Color.WHITE);

        JButton btnNo = createModernButton("Hủy", new Color(100, 116, 139));
        JButton btnYes = createModernButton("Xác nhận", PRIMARY);

        btnNo.addActionListener(e -> dialog.dispose());
        btnYes.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });

        footer.add(btnNo);
        footer.add(btnYes);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return result[0];
    }

    private void showModernAlert(String title, String message, Color color) {
        JDialog dialog = new JDialog(this, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(color, 2));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(color);
        header.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);

        JButton btnX = new JButton("×");
        btnX.setFont(new Font("Segoe UI", Font.BOLD, 22));
        btnX.setForeground(Color.WHITE);
        btnX.setFocusPainted(false);
        btnX.setBorderPainted(false);
        btnX.setContentAreaFilled(false);
        btnX.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnX.addActionListener(e -> dialog.dispose());

        header.add(lblTitle, BorderLayout.WEST);
        header.add(btnX, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(22, 26, 18, 26));

        JLabel icon = new JLabel(color == SUCCESS ? "✓" : "!");
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setVerticalAlignment(SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 30));
        icon.setForeground(color);
        icon.setPreferredSize(new Dimension(52, 52));

        JLabel msg = new JLabel("<html><div style='width:380px; font-family:Segoe UI; font-size:13px; color:#0f172a;'>"
                + escapeHtml(message).replace("\n", "<br>")
                + "</div></html>");

        body.add(icon, BorderLayout.WEST);
        body.add(msg, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footer.setBackground(Color.WHITE);

        JButton btnClose = createModernButton("Đóng", color);
        btnClose.addActionListener(e -> dialog.dispose());

        footer.add(btnClose);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String escapeHtml(String s) {
        if (s == null) {
            return "";
        }

        return s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private class NumberCellEditor extends DefaultCellEditor {
        private final JTextField field;

        public NumberCellEditor() {
            super(new JTextField());
            field = (JTextField) getComponent();
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            field.setHorizontalAlignment(SwingConstants.RIGHT);
            field.setBorder(BorderFactory.createLineBorder(PRIMARY, 1));
            field.enableInputMethods(false);

            ((AbstractDocument) field.getDocument()).setDocumentFilter(new DigitsOnlyFilter(9));

            field.addActionListener(e -> stopCellEditing());

            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    SwingUtilities.invokeLater(field::selectAll);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    try {
                        stopCellEditing();
                    } catch (Exception ignored) {
                    }
                }
            });

            setClickCountToStart(1);
        }

        @Override
        public Object getCellEditorValue() {
            String text = field.getText() == null ? "" : field.getText().trim();

            if (text.isEmpty()) {
                return 0;
            }

            try {
                return Integer.parseInt(text);
            } catch (Exception e) {
                return 0;
            }
        }
    }

    private class TextCellEditor extends DefaultCellEditor {
        private final JTextField field;

        public TextCellEditor() {
            super(new JTextField());
            field = (JTextField) getComponent();
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            field.setBorder(BorderFactory.createLineBorder(PRIMARY, 1));
            field.enableInputMethods(true);

            field.addActionListener(e -> stopCellEditing());

            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    SwingUtilities.invokeLater(field::selectAll);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    try {
                        stopCellEditing();
                    } catch (Exception ignored) {
                    }
                }
            });

            setClickCountToStart(1);
        }

        @Override
        public Object getCellEditorValue() {
            return field.getText() == null ? "" : field.getText().trim();
        }
    }

    private static class DigitsOnlyFilter extends DocumentFilter {
        private final int maxLength;

        public DigitsOnlyFilter(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string != null) {
                replace(fb, offset, 0, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text == null) {
                return;
            }

            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String next = current.substring(0, offset) + text + current.substring(offset + length);

            if (next.matches("\\d*") && next.length() <= maxLength) {
                fb.replace(offset, length, text, attrs);
            }
        }
    }

    private class KiemKeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table,
                    value,
                    isSelected,
                    hasFocus,
                    row,
                    column
            );

            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setBorder(new EmptyBorder(0, 8, 0, 8));
            label.setOpaque(true);

            if (isSelected) {
                label.setBackground(ROW_SELECTED);
                label.setForeground(TEXT_PRIMARY);
            } else {
                label.setBackground(row % 2 == 0 ? Color.WHITE : new Color(252, 253, 255));
                label.setForeground(TEXT_PRIMARY);
            }

            if (column == 0 || column == 3 || column == 4 || column == 5 || column == 6) {
                label.setHorizontalAlignment(SwingConstants.RIGHT);
            } else {
                label.setHorizontalAlignment(SwingConstants.LEFT);
            }

            if (column == 5) {
                label.setFont(new Font("Segoe UI", Font.BOLD, 14));
                label.setForeground(PRIMARY);
            }

            if (column == 6) {
                int chenh = parseInt(value, 0);
                label.setFont(new Font("Segoe UI", Font.BOLD, 14));

                if (chenh > 0) {
                    label.setForeground(SUCCESS);
                } else if (chenh < 0) {
                    label.setForeground(DANGER);
                } else {
                    label.setForeground(TEXT_SECONDARY);
                }
            }

            return label;
        }
    }

    private static class SmoothScrollBarUI extends BasicScrollBarUI {
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(0, 0));
            btn.setMinimumSize(new Dimension(0, 0));
            btn.setMaximumSize(new Dimension(0, 0));
            btn.setBorder(null);
            return btn;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(248, 250, 252));
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds == null || thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color thumb = isDragging
                    ? new Color(100, 116, 139)
                    : new Color(148, 163, 184);

            g2.setColor(thumb);

            int pad = 3;

            if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
                int x = thumbBounds.x + pad;
                int y = thumbBounds.y + pad;
                int width = thumbBounds.width - pad * 2;
                int height = thumbBounds.height - pad * 2;
                g2.fillRoundRect(x, y, Math.max(width, 4), Math.max(height, 18), 8, 8);
            } else {
                int x = thumbBounds.x + pad;
                int y = thumbBounds.y + pad;
                int width = thumbBounds.width - pad * 2;
                int height = thumbBounds.height - pad * 2;
                g2.fillRoundRect(x, y, Math.max(width, 18), Math.max(height, 4), 8, 8);
            }

            g2.dispose();
        }
    }

    private interface ChangeCallback {
        void run();
    }

    private static class SimpleDocumentListener implements DocumentListener {
        private final ChangeCallback callback;

        public SimpleDocumentListener(ChangeCallback callback) {
            this.callback = callback;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            callback.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            callback.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            callback.run();
        }
    }
}