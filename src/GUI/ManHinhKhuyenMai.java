package GUI;

import BUS.BUS_KhuyenMai;
import ConnectDB.ConnectDB;
import DAO.DAO_DieuKienKhuyenMai;
import DAO.DAO_HinhThucKhuyenMai;
import Entity.DieuKienKhuyenMai;
import Entity.HinhThucKhuyenMai;
import Entity.KhuyenMai;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tác giả: Nguyễn Văn Phương Nam-23652641 -DHKHMT19ATT
 * Project: MyCarePharmacy
 * Version: Full UI Refactor + Database Integration
 */
public class ManHinhKhuyenMai extends JPanel {

    // ==========================================
    // 1. MÀU SẮC & PHÔNG CHỮ CHUẨN DESIGN
    // ==========================================
    private final Color COLOR_BG = Color.decode("#F4F6F9");
    private final Color COLOR_CARD = Color.decode("#FFFFFF");
    private final Color COLOR_PRIMARY = Color.decode("#1F3A52"); 
    private final Color COLOR_DANGER = Color.decode("#E1304C");  
    private final Color COLOR_INFO = Color.decode("#007BFF");    
    private final Color COLOR_SUCCESS = Color.decode("#28A745"); 
    private final Color COLOR_TEXT_MAIN = Color.decode("#212B36");
    private final Color COLOR_TEXT_MUTED = Color.decode("#6C757D");
    private final Color COLOR_BORDER = Color.decode("#DFE3E8");
    private final Color COLOR_PURPLE = Color.decode("#8A2BE2");  

    private final Font FONT_H1 = new Font("Segoe UI", Font.BOLD, 18);
    private final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    // ==========================================
    // 2. CÁC BIẾN GIAO DIỆN
    // ==========================================
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTable tblKhuyenMai;
    private SearchField searchField;
    private List<RoundedButton> filterButtons = new ArrayList<>();
    private JPanel pnlKPI;
    private JLabel lblCount;

    private BUS_KhuyenMai busKhuyenMai = new BUS_KhuyenMai();
    private DAO_HinhThucKhuyenMai daoHinhThucKhuyenMai = new DAO_HinhThucKhuyenMai();
    private DAO_DieuKienKhuyenMai daoDieuKienKhuyenMai = new DAO_DieuKienKhuyenMai();

    // ==========================================
    // 3. KHỞI TẠO
    // ==========================================
    public ManHinhKhuyenMai() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        ConnectDB.getInstance().connect();
        
        this.setBackground(COLOR_BG);
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(15, 25, 20, 25)); 
        
        JPanel mainContent = createMainContent();
        
        JScrollPane scrollMain = new JScrollPane(mainContent);
        scrollMain.setBorder(null);
        scrollMain.getViewport().setBackground(COLOR_BG);
        scrollMain.getVerticalScrollBar().setUnitIncrement(16);
        
        this.add(scrollMain, BorderLayout.CENTER);
        
        loadDataToTable();
    }

    // ==========================================
    // 4. XÂY DỰNG BỐ CỤC CHÍNH
    // ==========================================
    private JPanel createMainContent() {
        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlMain.setOpaque(false);

        // --- 1. HEADER CHÍNH ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(0, 0, 15, 0));
        pnlHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        JLabel lblTitle = new JLabel("QUẢN LÝ KHUYẾN MẠI");
        lblTitle.setFont(FONT_H1);
        lblTitle.setForeground(COLOR_PRIMARY);
        
        JPanel pnlRightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRightHeader.setOpaque(false);
        
        searchField = new SearchField(250, "Tìm khuyến mại...");
        searchField.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterData(); }
            @Override public void removeUpdate(DocumentEvent e) { filterData(); }
            @Override public void changedUpdate(DocumentEvent e) { filterData(); }
            private void filterData() {
                String text = searchField.getTextField().getText();
                if (text.trim().isEmpty()) {
                    if (rowSorter != null) rowSorter.setRowFilter(null);
                } else {
                    if (rowSorter != null) rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
                }
            }
        });

        RoundedButton btnRefresh = new RoundedButton("Làm mới", COLOR_TEXT_MUTED, Color.WHITE);
        btnRefresh.setPreferredSize(new Dimension(110, 36));
        btnRefresh.addActionListener(e -> {
            searchField.getTextField().setText(""); 
            for (RoundedButton b : filterButtons) {
                b.setColors(COLOR_CARD, COLOR_TEXT_MUTED);
                b.setBorderColor(COLOR_BORDER);
            }
            if (!filterButtons.isEmpty()) {
                filterButtons.get(0).setColors(COLOR_INFO, Color.WHITE);
                filterButtons.get(0).setBorderColor(COLOR_INFO);
            }
            if(rowSorter != null) rowSorter.setRowFilter(null);
            loadDataToTable();
        });

        RoundedButton btnAdd = new RoundedButton("+ Thêm mới", COLOR_DANGER, Color.WHITE);
        btnAdd.setPreferredSize(new Dimension(130, 36));
        btnAdd.addActionListener(e -> {
            PromoDialog dialog = new PromoDialog(null);
            dialog.showDialog();
        });

        pnlRightHeader.add(searchField);
        pnlRightHeader.add(btnRefresh);
        pnlRightHeader.add(btnAdd);

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(pnlRightHeader, BorderLayout.EAST);

        // --- 2. KPI CARDS ---
        pnlKPI = createTopKPISecton(0, 0, 0);

        // --- 3. TÍCH ĐIỂM THƯỞNG SECTION ---
        JPanel pnlTichDiem = createTichDiemSection();

        // --- 4. BỘ LỌC (FILTERS) ---
        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlFilters.setOpaque(false);
        pnlFilters.setBorder(new EmptyBorder(15, 0, 10, 0));
        pnlFilters.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        JLabel lblIconFilter = new JLabel("Lọc trạng thái: "); 
        lblIconFilter.setFont(FONT_BOLD);
        lblIconFilter.setForeground(COLOR_TEXT_MUTED);
        pnlFilters.add(lblIconFilter);
        
        String[] filters = {"Tất cả", "Đang hoạt động", "Sắp diễn ra", "Đã kết thúc", "Tạm dừng"};
        for (String filterName : filters) {
            boolean isActive = filterName.equals("Tất cả");
            Color bgColor = isActive ? COLOR_INFO : COLOR_CARD;
            Color fgColor = isActive ? Color.WHITE : COLOR_TEXT_MUTED;
            
            RoundedButton btnFilter = new RoundedButton(filterName, bgColor, fgColor);
            if (!isActive) btnFilter.setBorderColor(COLOR_BORDER);
            
            btnFilter.setPreferredSize(new Dimension(120, 32));
            filterButtons.add(btnFilter);
            pnlFilters.add(btnFilter);
            
            btnFilter.addActionListener(e -> {
                for (RoundedButton b : filterButtons) {
                    b.setColors(COLOR_CARD, COLOR_TEXT_MUTED);
                    b.setBorderColor(COLOR_BORDER);
                }
                btnFilter.setColors(COLOR_INFO, Color.WHITE);
                btnFilter.setBorderColor(COLOR_INFO);
                
                if (filterName.equals("Tất cả")) rowSorter.setRowFilter(null);
                else rowSorter.setRowFilter(RowFilter.regexFilter("^" + filterName + "$", 7));
            });
        }
        
        lblCount = new JLabel("0 / 0 chương trình");
        lblCount.setFont(FONT_SMALL);
        lblCount.setForeground(COLOR_TEXT_MUTED);
        JPanel pnlFilterRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlFilterRight.setOpaque(false);
        pnlFilterRight.add(lblCount);
        
        JPanel pnlFilterWrapper = new JPanel(new BorderLayout());
        pnlFilterWrapper.setOpaque(false);
        pnlFilterWrapper.add(pnlFilters, BorderLayout.WEST);
        pnlFilterWrapper.add(pnlFilterRight, BorderLayout.EAST);
        pnlFilterWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // --- 5. BẢNG DỮ LIỆU CHÍNH ---
        JPanel pnlTableWrapper = createTableSection();

        pnlMain.add(pnlHeader);
        pnlMain.add(Box.createVerticalStrut(10));
        pnlMain.add(pnlKPI);
        pnlMain.add(Box.createVerticalStrut(15));
        pnlMain.add(pnlTichDiem);
        pnlMain.add(Box.createVerticalStrut(5));
        pnlMain.add(pnlFilterWrapper);
        pnlMain.add(Box.createVerticalStrut(5));
        pnlMain.add(pnlTableWrapper);

        return pnlMain;
    }

    private JPanel createTopKPISecton(int dangHD, int sapDienRa, int daKetThuc) {
        JPanel pnlWrapper = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlWrapper.setOpaque(false);
        pnlWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        pnlWrapper.setPreferredSize(new Dimension(0, 80));
        
        pnlWrapper.add(new KPICard("Đang hoạt động", String.valueOf(dangHD), COLOR_SUCCESS, Color.decode("#E8F5E9")));
        pnlWrapper.add(new KPICard("Sắp diễn ra", String.valueOf(sapDienRa), COLOR_INFO, Color.decode("#E3F2FD")));
        pnlWrapper.add(new KPICard("Đã kết thúc / Tạm dừng", String.valueOf(daKetThuc), COLOR_TEXT_MAIN, Color.decode("#F8F9FA")));
        
        return pnlWrapper;
    }

    private JPanel createTichDiemSection() {
        JPanel pnlWrap = new JPanel(new BorderLayout());
        pnlWrap.setBackground(Color.decode("#FAFAFA"));
        pnlWrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E1D4F4"), 1), 
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        pnlWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JPanel pnlHead = new JPanel(new BorderLayout());
        pnlHead.setOpaque(false);
        JLabel lblTitle = new JLabel("Chương trình tích điểm thưởng");
        lblTitle.setFont(FONT_BOLD);
        lblTitle.setForeground(COLOR_PURPLE);
        
        RoundedButton btnSaveSetup = new RoundedButton("Lưu cài đặt", Color.decode("#8A2BE2"), Color.WHITE);
        btnSaveSetup.setPreferredSize(new Dimension(100, 30));
        btnSaveSetup.setFont(FONT_SMALL);
        btnSaveSetup.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Đã lưu cài đặt tích điểm thưởng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });
        
        pnlHead.add(lblTitle, BorderLayout.WEST);
        pnlHead.add(btnSaveSetup, BorderLayout.EAST);

        JPanel pnlBody = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlBody.setOpaque(false);
        pnlBody.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        pnlBody.add(createPointConfigCard("Tích điểm khi mua", "10000", "đ = 1 điểm", "Ví dụ: mua 100.000đ → được 10 điểm"));
        pnlBody.add(createPointConfigCard("Giá trị đổi điểm", "1000", "1 điểm =", "100 điểm = 100.000đ giảm giá"));
        pnlBody.add(createPointConfigCard("Điểm tối thiểu đổi", "1000", "điểm", "Phải có ít nhất 1000 điểm mới được đổi"));

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlFooter.setOpaque(false);
        pnlFooter.setBackground(Color.decode("#F9EEFF")); 
        pnlFooter.setOpaque(true);
        pnlFooter.setBorder(new EmptyBorder(8, 10, 8, 10));
        
        JLabel lblSummary = new JLabel("<html><b>Tóm tắt quy tắc:</b> Mua <b>10.000đ</b> → 1 điểm. Mỗi 1 điểm đổi được <b>1.000đ</b> giảm giá. Tối thiểu <b>1000</b> điểm mới được đổi.</html>");
        lblSummary.setFont(FONT_SMALL);
        lblSummary.setForeground(COLOR_PURPLE);
        pnlFooter.add(lblSummary);

        pnlWrap.add(pnlHead, BorderLayout.NORTH);
        pnlWrap.add(pnlBody, BorderLayout.CENTER);
        pnlWrap.add(pnlFooter, BorderLayout.SOUTH);

        return pnlWrap;
    }

    private JPanel createPointConfigCard(String title, String inputVal, String suffix, String hint) {
        JPanel pnlCard = new JPanel(new BorderLayout(0, 5));
        pnlCard.setBackground(COLOR_CARD);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E1D4F4"), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JLabel lblTop = new JLabel(title);
        lblTop.setFont(FONT_BOLD);
        
        JPanel pnlInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlInput.setOpaque(false);
        
        JTextField txtInput = new JTextField(inputVal);
        txtInput.setPreferredSize(new Dimension(80, 30));
        txtInput.setHorizontalAlignment(JTextField.CENTER);
        txtInput.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        
        txtInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume(); 
                }
            }
        });
        
        if (suffix.startsWith("đ")) {
            pnlInput.add(txtInput); pnlInput.add(new JLabel(suffix));
        } else if (suffix.startsWith("1 điểm")) {
            pnlInput.add(new JLabel("1 điểm = ")); pnlInput.add(txtInput); pnlInput.add(new JLabel(" đ"));
        } else {
            pnlInput.add(txtInput); pnlInput.add(new JLabel(suffix));
        }

        JLabel lblHint = new JLabel(hint);
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHint.setForeground(COLOR_TEXT_MUTED);

        pnlCard.add(lblTop, BorderLayout.NORTH);
        pnlCard.add(pnlInput, BorderLayout.CENTER);
        pnlCard.add(lblHint, BorderLayout.SOUTH);
        
        return pnlCard;
    }

    private JPanel createTableSection() {
        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setBackground(COLOR_CARD);
        pnlWrapper.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));

        JPanel pnlTableHeaderWrapper = new JPanel(new BorderLayout());
        pnlTableHeaderWrapper.setBackground(COLOR_PRIMARY); 
        pnlTableHeaderWrapper.setBorder(new EmptyBorder(8, 15, 8, 15));
        
        JLabel lblListTitle = new JLabel("Danh sách chương trình khuyến mại");
        lblListTitle.setFont(FONT_BOLD);
        lblListTitle.setForeground(Color.WHITE);
        
        JLabel lblListHint = new JLabel("Nhấn vào chữ Sửa để cập nhật - Nhấn Toggle để bật/tắt");
        lblListHint.setFont(FONT_SMALL);
        lblListHint.setForeground(Color.decode("#A0AAB5"));
        
        pnlTableHeaderWrapper.add(lblListTitle, BorderLayout.WEST);
        pnlTableHeaderWrapper.add(lblListHint, BorderLayout.EAST);

        String[] columns = {"#", "Tên chương trình", "Loại", "Giảm giá", "Đơn tối thiểu", "Áp dụng", "Thời gian", "Trạng thái", "Sửa", "Bật/Tắt"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) { return (columnIndex == 9) ? Boolean.class : String.class; }
        };
        
        tblKhuyenMai = new JTable(tableModel);
        tblKhuyenMai.setRowHeight(55); // Tăng chiều cao dòng cho giống bản design
        tblKhuyenMai.setShowGrid(false); 
        tblKhuyenMai.setShowHorizontalLines(true); 
        tblKhuyenMai.setIntercellSpacing(new Dimension(0, 0)); 
        tblKhuyenMai.setGridColor(Color.decode("#F1F3F5")); 
        tblKhuyenMai.setFont(FONT_REGULAR);
        tblKhuyenMai.setSelectionBackground(Color.decode("#F8F9FA"));
        tblKhuyenMai.setSelectionForeground(COLOR_TEXT_MAIN);

        JTableHeader header = tblKhuyenMai.getTableHeader();
        header.setBackground(Color.WHITE);
        header.setFont(FONT_BOLD);
        header.setForeground(COLOR_TEXT_MAIN);
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_BORDER));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(JLabel.LEFT);

        DefaultTableCellRenderer discountRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                comp.setForeground(COLOR_DANGER); 
                comp.setFont(FONT_BOLD);
                ((JLabel)comp).setHorizontalAlignment(JLabel.CENTER);
                if (hasF) ((JComponent)comp).setBorder(new EmptyBorder(0,0,0,0));
                return comp;
            }
        };

        tblKhuyenMai.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); 
        tblKhuyenMai.getColumnModel().getColumn(0).setMaxWidth(50);
        tblKhuyenMai.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);   
        tblKhuyenMai.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);   
        tblKhuyenMai.getColumnModel().getColumn(3).setCellRenderer(discountRenderer); 
        tblKhuyenMai.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); 
        tblKhuyenMai.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); 
        tblKhuyenMai.getColumnModel().getColumn(6).setCellRenderer(centerRenderer); 
        
        rowSorter = new TableRowSorter<>(tableModel);
        tblKhuyenMai.setRowSorter(rowSorter);

        tblKhuyenMai.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());
        tblKhuyenMai.getColumnModel().getColumn(8).setCellRenderer(new ActionRenderer());
        tblKhuyenMai.getColumnModel().getColumn(9).setCellRenderer(new ToggleSwitchRenderer());

        tblKhuyenMai.addMouseListener(new MouseAdapter() {
            @Override 
            public void mouseClicked(MouseEvent e) {
                int viewRow = tblKhuyenMai.rowAtPoint(e.getPoint());
                int viewCol = tblKhuyenMai.columnAtPoint(e.getPoint());
                if (viewRow < 0) return;
                
                int modelRow = tblKhuyenMai.convertRowIndexToModel(viewRow);

                if (viewCol == 8) {
                    Map<String, Object> dataRow = new HashMap<>();
                    for (int i = 0; i < tableModel.getColumnCount(); i++) dataRow.put(String.valueOf(i), tableModel.getValueAt(modelRow, i));
                    new PromoDialog(dataRow).showDialog();
                    
                } else if (viewCol == 9) {
                    boolean currentState = (boolean) tableModel.getValueAt(modelRow, 9);
                    boolean newState = !currentState;
                    
                    tableModel.setValueAt(newState, modelRow, 9);
                    if (newState) {
                        tableModel.setValueAt("Đang hoạt động", modelRow, 7);
                    } else {
                        tableModel.setValueAt("Tạm dừng", modelRow, 7);
                    }
                    updateKPI();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblKhuyenMai);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        pnlWrapper.add(pnlTableHeaderWrapper, BorderLayout.NORTH);
        pnlWrapper.add(scrollPane, BorderLayout.CENTER);
        
        return pnlWrapper;
    }

    // ==========================================
    // 5. DIALOG THÊM / SỬA KHUYẾN MÃI
    // ==========================================
    private class PromoDialog {
        private final JDialog dialog;
        private final FloatingField fldName, fldMinOrder, fldMaxDiscount, fldCustomer;
        private final RoundedComboBox cbType, cbTarget;
        private final DatePicker dpStart, dpEnd;

        public PromoDialog(Map<String, Object> existingData) {
            dialog = new JDialog((Window)null, "", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setUndecorated(true);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(Color.WHITE);
            mainPanel.setBorder(BorderFactory.createLineBorder(COLOR_PRIMARY, 1));

            JPanel pnlHeader = new JPanel(new BorderLayout());
            pnlHeader.setBackground(COLOR_PRIMARY); 
            pnlHeader.setPreferredSize(new Dimension(0, 50));
            pnlHeader.setBorder(new EmptyBorder(0, 20, 0, 15));
            
            String titleText = (existingData == null) ? "Thêm chương trình khuyến mại mới" : "Chỉnh sửa chương trình";
            JLabel lblTitle = new JLabel(titleText);
            lblTitle.setForeground(Color.WHITE); 
            lblTitle.setFont(FONT_BOLD);
            
            JButton btnClose = new JButton("X");
            btnClose.setForeground(Color.WHITE); 
            btnClose.setFont(new Font("Segoe UI", Font.BOLD, 18));
            btnClose.setBorder(null); 
            btnClose.setContentAreaFilled(false); 
            btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnClose.addActionListener(e -> dialog.dispose());
            
            pnlHeader.add(lblTitle, BorderLayout.WEST); 
            pnlHeader.add(btnClose, BorderLayout.EAST);

            JPanel pnlBody = new JPanel(new GridBagLayout());
            pnlBody.setBackground(Color.WHITE);
            pnlBody.setBorder(new EmptyBorder(25, 30, 25, 30));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL; 
            gbc.insets = new Insets(10, 10, 10, 10); 
            gbc.weightx = 1.0;

            fldCustomer = new FloatingField("Mã chương trình", "Hệ thống tự tạo nếu để trống");
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; pnlBody.add(fldCustomer, gbc);

            fldName = new FloatingField("<html>Tên chương trình <font color='#E1304C'>*</font></html>", "Nhập tên chương trình");
            if (existingData != null) fldName.getTextField().setText(existingData.get("1").toString());
            gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; pnlBody.add(fldName, gbc);

            String[] types = {"Giảm phần trăm (%)", "Giảm tiền mặt (VNĐ)"};
            cbType = new RoundedComboBox(types);
            
            JSpinner spPercent = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
            spPercent.setPreferredSize(new Dimension(0, 42));
            spPercent.setFont(FONT_REGULAR);
            ((JSpinner.DefaultEditor)spPercent.getEditor()).getTextField().setBorder(new EmptyBorder(0,10,0,0));
            spPercent.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
            
            gbc.gridwidth = 1; gbc.gridx = 0; gbc.gridy = 2; 
            pnlBody.add(labeled("Loại hình thức", cbType), gbc);
            gbc.gridx = 1; gbc.gridy = 2; 
            pnlBody.add(labeled("Mức giảm", spPercent), gbc);

            fldMinOrder = new FloatingField("Đơn tối thiểu (VNĐ)", "Nhập số tiền...");
            fldMaxDiscount = new FloatingField("Giảm tối đa (VNĐ)", "Nhập số tiền...");
            
            fldMinOrder.getTextField().addKeyListener(new KeyAdapter() { public void keyTyped(KeyEvent e) { if(!Character.isDigit(e.getKeyChar())) e.consume(); }});
            fldMaxDiscount.getTextField().addKeyListener(new KeyAdapter() { public void keyTyped(KeyEvent e) { if(!Character.isDigit(e.getKeyChar())) e.consume(); }});
            
            gbc.gridx = 0; gbc.gridy = 3; pnlBody.add(fldMinOrder, gbc);
            gbc.gridx = 1; gbc.gridy = 3; pnlBody.add(fldMaxDiscount, gbc);

            String[] targets = {"Tất cả", "Sản phẩm chức năng", "Thuốc không kê đơn"};
            cbTarget = new RoundedComboBox(targets);
            gbc.gridx = 0; gbc.gridy = 4; pnlBody.add(labeled("Đối tượng áp dụng", cbTarget), gbc);

            dpStart = new DatePicker(); dpEnd = new DatePicker();
            JPanel pnlDates = new JPanel(new GridLayout(1, 2, 10, 0)); pnlDates.setOpaque(false); pnlDates.add(dpStart); pnlDates.add(dpEnd);
            gbc.gridx = 1; gbc.gridy = 4; 
            pnlBody.add(labeled("<html>Thời gian <font color='#E1304C'>*</font></html>", pnlDates), gbc);

            JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
            pnlFooter.setBackground(Color.WHITE);
            pnlFooter.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));
            
            RoundedButton btnCancel = new RoundedButton("Hủy bỏ", Color.WHITE, COLOR_TEXT_MAIN);
            btnCancel.setBorderColor(COLOR_BORDER); btnCancel.setPreferredSize(new Dimension(120, 38));
            btnCancel.addActionListener(e -> dialog.dispose());

            RoundedButton btnSave = new RoundedButton(existingData == null ? "Lưu chương trình" : "Cập nhật", COLOR_DANGER, Color.WHITE);
            btnSave.setPreferredSize(new Dimension(150, 38));
            btnSave.addActionListener(e -> {
                JOptionPane.showMessageDialog(dialog, existingData == null ? "Tạo chương trình khuyến mại thành công!" : "Cập nhật thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadDataToTable();
            });

            pnlFooter.add(btnCancel); pnlFooter.add(btnSave);

            mainPanel.add(pnlHeader, BorderLayout.NORTH);
            mainPanel.add(pnlBody, BorderLayout.CENTER);
            mainPanel.add(pnlFooter, BorderLayout.SOUTH);

            dialog.add(mainPanel); dialog.pack(); dialog.setSize(800, dialog.getHeight());
            dialog.setLocationRelativeTo(null);
        }
        public void showDialog() { dialog.setVisible(true); }
    }

    private JPanel labeled(String labelText, JComponent component) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(COLOR_TEXT_MAIN);
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // 6. CÁC CLASS UI (CÓ TÍCH HỢP CHỮ MỜ - PLACEHOLDER XỊN)
    // ==========================================

    class TextFieldWithPlaceholder extends JTextField {
        private String placeholder;
        public TextFieldWithPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            this.setFont(FONT_REGULAR);
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && placeholder != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#ADB5BD")); 
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, getInsets().left, y);
                g2.dispose();
            }
        }
    }

    class FloatingField extends JPanel {
        private TextFieldWithPlaceholder textField;
        public FloatingField(String label, String placeholder) {
            setLayout(new BorderLayout(0, 6)); setOpaque(false);
            JLabel lbl = new JLabel(label); lbl.setFont(FONT_BOLD); lbl.setForeground(COLOR_TEXT_MAIN);
            
            textField = new TextFieldWithPlaceholder(placeholder);
            textField.setPreferredSize(new Dimension(0, 42));
            textField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1), BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            textField.setBackground(Color.WHITE);
            
            add(lbl, BorderLayout.NORTH); add(textField, BorderLayout.CENTER);
        }
        public JTextField getTextField() { return textField; }
    }

    class RoundedComboBox extends JComboBox<String> {
        public RoundedComboBox(String[] items) {
            super(items); setFont(FONT_REGULAR); setPreferredSize(new Dimension(0, 42)); setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1));
        }
    }

    class SearchField extends JPanel {
        private TextFieldWithPlaceholder textField;
        public SearchField(int width, String placeholder) {
            setLayout(new BorderLayout(10, 0)); setOpaque(false);
            setPreferredSize(new Dimension(width, 36)); setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_BORDER, 1), BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            JLabel lblIcon = new JLabel("Tim:"); lblIcon.setFont(FONT_REGULAR); lblIcon.setForeground(COLOR_TEXT_MUTED);
            
            textField = new TextFieldWithPlaceholder(placeholder); 
            textField.setBorder(null); 
            textField.setForeground(COLOR_TEXT_MAIN);
            
            add(lblIcon, BorderLayout.WEST); add(textField, BorderLayout.CENTER);
        }
        public JTextField getTextField() { return textField; }
    }

    class RoundedButton extends JButton {
        private Color bgColor, fgColor, borderColor;
        public RoundedButton(String text, Color bg, Color fg) {
            super(text); this.bgColor = bg; this.fgColor = fg;
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setFont(FONT_BOLD); setForeground(fgColor); setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        public void setColors(Color bg, Color fg) { this.bgColor = bg; this.fgColor = fg; repaint(); }
        public void setBorderColor(Color border) { this.borderColor = border; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4); 
            if (borderColor != null) { g2.setColor(borderColor); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4); }
            g2.setColor(fgColor); FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    class KPICard extends JPanel {
        public KPICard(String title, String val, Color c, Color bg) {
            setOpaque(false); setLayout(new BorderLayout());
            JPanel inner = new JPanel(new GridLayout(2, 1));
            inner.setBackground(bg);
            inner.setBorder(new EmptyBorder(15, 20, 15, 20));
            JLabel lblTitle = new JLabel(title); lblTitle.setFont(FONT_SMALL); lblTitle.setForeground(COLOR_TEXT_MUTED);
            JLabel lblValue = new JLabel(val); lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22)); lblValue.setForeground(c);
            inner.add(lblTitle); inner.add(lblValue);
            
            JPanel pnlOuter = new JPanel(new BorderLayout());
            pnlOuter.setOpaque(false);
            pnlOuter.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
            pnlOuter.add(inner, BorderLayout.CENTER);
            add(pnlOuter);
        }
    }

    // ==========================================
    // 7. HỆ THỐNG LỊCH (DATEPICKER)
    // ==========================================
    class CalendarPanel extends JPanel {
        private JLabel lblMonthYear; private JPanel pnlDays; private Calendar cal;
        private java.util.function.Consumer<Date> onSelect;

        public CalendarPanel(java.util.function.Consumer<Date> onSelect) {
            this.onSelect = onSelect; this.cal = Calendar.getInstance();
            setLayout(new BorderLayout(0, 5)); setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_INFO, 1), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            
            JPanel pnlHead = new JPanel(new BorderLayout()); pnlHead.setBackground(Color.WHITE);
            JButton btnPrev = new JButton("<"); btnPrev.setFont(FONT_BOLD); btnPrev.setContentAreaFilled(false); btnPrev.setBorder(null); btnPrev.setCursor(new Cursor(Cursor.HAND_CURSOR));
            JButton btnNext = new JButton(">"); btnNext.setFont(FONT_BOLD); btnNext.setContentAreaFilled(false); btnNext.setBorder(null); btnNext.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lblMonthYear = new JLabel("", SwingConstants.CENTER); lblMonthYear.setFont(FONT_BOLD); lblMonthYear.setForeground(COLOR_INFO);
            
            pnlHead.add(btnPrev, BorderLayout.WEST); pnlHead.add(lblMonthYear, BorderLayout.CENTER); pnlHead.add(btnNext, BorderLayout.EAST);
            
            pnlDays = new JPanel(new GridLayout(7, 7, 2, 2)); pnlDays.setBackground(Color.WHITE);
            
            this.add(pnlHead, BorderLayout.NORTH); this.add(pnlDays, BorderLayout.CENTER);
            
            btnPrev.addActionListener(e -> { cal.add(Calendar.MONTH, -1); updateCalendar(); });
            btnNext.addActionListener(e -> { cal.add(Calendar.MONTH, 1); updateCalendar(); });
            
            updateCalendar(); this.setPreferredSize(new Dimension(280, 250));
        }

        private void updateCalendar() {
            pnlDays.removeAll();
            String[] weekDays = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
            for (String dayName : weekDays) { JLabel lblDay = new JLabel(dayName, SwingConstants.CENTER); lblDay.setFont(FONT_SMALL); lblDay.setForeground(Color.GRAY); pnlDays.add(lblDay); }
            Calendar tempCal = (Calendar) cal.clone(); tempCal.set(Calendar.DAY_OF_MONTH, 1);
            int offset = tempCal.get(Calendar.DAY_OF_WEEK) - 1; tempCal.add(Calendar.DAY_OF_MONTH, -offset);
            for (int i = 0; i < 42; i++) {
                Date cellDate = tempCal.getTime(); int dayOfMonth = tempCal.get(Calendar.DAY_OF_MONTH);
                JButton btnDay = new JButton(String.valueOf(dayOfMonth)); btnDay.setFont(FONT_SMALL); btnDay.setFocusPainted(false); btnDay.setBackground(Color.WHITE);
                btnDay.setBorder(BorderFactory.createLineBorder(Color.decode("#F0F0F0"))); btnDay.setCursor(new Cursor(Cursor.HAND_CURSOR));
                if (tempCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH)) btnDay.setForeground(Color.BLACK); else btnDay.setForeground(Color.LIGHT_GRAY);
                btnDay.addActionListener(e -> { if (onSelect != null) onSelect.accept(cellDate); });
                pnlDays.add(btnDay); tempCal.add(Calendar.DAY_OF_MONTH, 1);
            }
            lblMonthYear.setText("Tháng " + (cal.get(Calendar.MONTH) + 1) + " / " + cal.get(Calendar.YEAR));
            pnlDays.revalidate(); pnlDays.repaint();
        }
    }

    class DatePicker extends JPanel {
        private JTextField txtDate; private JDialog popupDialog; private CalendarPanel calendarPanel;
        public DatePicker() {
            setLayout(new BorderLayout()); setOpaque(false);
            JPanel pnlWrapper = new JPanel(new BorderLayout()); pnlWrapper.setBackground(Color.WHITE); pnlWrapper.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
            txtDate = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            txtDate.setFont(FONT_REGULAR); txtDate.setBorder(new EmptyBorder(0, 10, 0, 0)); txtDate.setPreferredSize(new Dimension(0, 42));
            JButton btnCalendar = new JButton("Lịch"); btnCalendar.setFont(FONT_SMALL); btnCalendar.setBorder(new EmptyBorder(0,5,0,5)); btnCalendar.setContentAreaFilled(false); btnCalendar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            calendarPanel = new CalendarPanel(d -> {
                txtDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(d)); 
                if (popupDialog != null) popupDialog.dispose();
            });
            btnCalendar.addActionListener(e -> {
                if (popupDialog != null && popupDialog.isVisible()) { popupDialog.dispose(); return; }
                popupDialog = new JDialog(SwingUtilities.getWindowAncestor(DatePicker.this)); popupDialog.setUndecorated(true); popupDialog.add(calendarPanel); popupDialog.pack();
                Point location = txtDate.getLocationOnScreen(); popupDialog.setLocation(location.x, location.y + txtDate.getHeight() + 2); popupDialog.setVisible(true);
                popupDialog.addWindowFocusListener(new WindowFocusListener() { public void windowGainedFocus(WindowEvent ev) {} public void windowLostFocus(WindowEvent ev) { popupDialog.dispose(); } });
            });
            pnlWrapper.add(txtDate, BorderLayout.CENTER); pnlWrapper.add(btnCalendar, BorderLayout.EAST); add(pnlWrapper, BorderLayout.CENTER);
        }
        public String getText() { return txtDate.getText(); }
    }

    // ==========================================
    // 8. RENDERER BẢNG MÀU CHUẨN DESIGN
    // ==========================================
    class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String status = (value == null) ? "" : value.toString();
            JPanel pnl = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = Color.WHITE, fg = COLOR_TEXT_MAIN;
                    if (status.equals("Đang hoạt động")) { bg = Color.decode("#D1E7DD"); fg = Color.decode("#0F5132"); }
                    else if (status.equals("Sắp diễn ra")) { bg = Color.decode("#CFF4FC"); fg = Color.decode("#055160"); }
                    else if (status.equals("Tạm dừng") || status.equals("Đã kết thúc")) { bg = Color.decode("#E2E3E5"); fg = Color.decode("#383D41"); }
                    
                    int w = 100, h = 24, x = (getWidth() - w)/2, y = (getHeight() - h)/2;
                    g2.setColor(bg); g2.fillRoundRect(x, y, w, h, h, h); 
                    g2.setColor(fg); g2.setFont(FONT_SMALL);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(status, x + (w - fm.stringWidth(status))/2, y + (h - fm.getHeight())/2 + fm.getAscent());
                    g2.dispose();
                }
            };
            pnl.setOpaque(true); pnl.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return pnl;
        }
    }

    class ActionRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            JLabel lblAction = new JLabel("Sửa", SwingConstants.CENTER); 
            lblAction.setFont(FONT_BOLD); lblAction.setForeground(COLOR_INFO); 
            if (isS) { lblAction.setOpaque(true); lblAction.setBackground(t.getSelectionBackground()); } 
            else { lblAction.setOpaque(true); lblAction.setBackground(Color.WHITE); }
            return lblAction;
        }
    }

    class ToggleSwitchRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
            boolean isOn = (v instanceof Boolean) && (Boolean) v;
            JPanel pnlSwitch = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create(); 
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isOn ? COLOR_SUCCESS : COLOR_BORDER); 
                    int x = (getWidth() - 34) / 2;
                    int y = (getHeight() - 18) / 2;
                    g2.fillRoundRect(x, y, 34, 18, 18, 18); 
                    g2.setColor(Color.WHITE); 
                    g2.fillOval(isOn ? x + 17 : x + 2, y + 2, 14, 14); 
                    g2.dispose();
                }
            };
            if (isS) { pnlSwitch.setOpaque(true); pnlSwitch.setBackground(t.getSelectionBackground()); } 
            else { pnlSwitch.setOpaque(true); pnlSwitch.setBackground(Color.WHITE); }
            return pnlSwitch;
        }
    }

    // ==========================================
    // 9. LOGIC ĐỔ DỮ LIỆU DB
    // ==========================================
    private void loadDataToTable() {
        if (rowSorter != null) rowSorter.setRowFilter(null);
        tableModel.setRowCount(0);
        
        try {
            // Lấy dữ liệu từ Database thông qua BUS
            List<KhuyenMai> dsKhuyenMai = busKhuyenMai.layDsKhuyenMai();
            
            if (dsKhuyenMai == null || dsKhuyenMai.isEmpty()) {
                tableModel.addRow(new Object[]{ 1, "Giảm 10% Vitamin C", "Giảm phần trăm", "% 10", "100.000đ", "Sản phẩm chức năng", "01/01/2024 - 31/03/2024", "Tạm dừng", "Sửa", false });
                tableModel.addRow(new Object[]{ 2, "Tặng 50k đơn từ 500k", "Giảm tiền cố định", "% 50000 đ", "500.000đ", "Tất cả", "15/02/2024 - 28/02/2024", "Tạm dừng", "Sửa", false });
            } else {
                int stt = 1;
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (KhuyenMai km : dsKhuyenMai) {
                    String thoiGian = "N/A";
                    if (km.getNgayBatDau() != null && km.getNgayKetThuc() != null) {
                        thoiGian = km.getNgayBatDau().format(dtf) + " - " + km.getNgayKetThuc().format(dtf);
                    }
                    
                    // Logic DB của mày: Gọi DB map ra trường tương ứng. 
                    // Tao đang ví dụ map 1 số trường mặc định, mày sửa lại các getter() của Entity KhuyenMai cho khớp nha.
                    tableModel.addRow(new Object[]{ 
                        stt++, 
                        km.getTenKhuyenMai() != null ? km.getTenKhuyenMai() : "Không tên", 
                        "Phần trăm", // Có thể móc từ bảng Hình thức KM 
                        "% 10",      // Get từ km.getMucGiam()
                        "0đ", 
                        "Tất cả", 
                        thoiGian, 
                        "Đang hoạt động", 
                        "Sửa", 
                        true 
                    });
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        
        tableModel.fireTableDataChanged();
        updateKPI();
        
        if (lblCount != null) {
            lblCount.setText(tableModel.getRowCount() + " / " + tableModel.getRowCount() + " chương trình");
        }
    }

    private void updateKPI() {
        int dangHoatDong = 0, sapDienRa = 0, daKetThuc = 0;
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String status = tableModel.getValueAt(i, 7).toString();
            if (status.equals("Đang hoạt động")) dangHoatDong++;
            else if (status.equals("Sắp diễn ra")) sapDienRa++;
            else daKetThuc++;
        }
        
        if (pnlKPI != null && pnlKPI.getParent() != null) {
            Container parent = pnlKPI.getParent();
            parent.remove(pnlKPI);
            pnlKPI = createTopKPISecton(dangHoatDong, sapDienRa, daKetThuc);
            parent.add(pnlKPI, BorderLayout.NORTH);
            parent.revalidate(); parent.repaint();
        }
    }
}