package GUI;

import BUS.BUS_KhuyenMai;
import Entity.DieuKienKhuyenMai;
import Entity.HinhThucKhuyenMai;
import Entity.KhuyenMai;
import Enumeration.DoiTuongApDung;
import Enumeration.LoaiHinhThuc;
import Utils.MenuIcon;
import Utils.ModernDatePicker;
import Utils.ModernScrollBarUI;
import Utils.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManHinhKhuyenMai extends JPanel {

    private final Color COLOR_BG = Color.decode("#F4F6F9");
    private final Color COLOR_CARD = Color.decode("#FFFFFF");
    private final Color COLOR_PRIMARY = Color.decode("#1F3A52"); 
    private final Color COLOR_DANGER = Color.decode("#E1304C");  
    private final Color COLOR_INFO = Color.decode("#007BFF");    
    private final Color COLOR_SUCCESS = Color.decode("#10B981"); 
    private final Color COLOR_TEXT_MAIN = Color.decode("#212B36");
    private final Color COLOR_TEXT_MUTED = Color.decode("#6C757D");
    private final Color COLOR_BORDER = Color.decode("#DFE3E8");
    private final Color COLOR_PURPLE = Color.decode("#8A2BE2");  

    private final Font FONT_H1 = new Font("Segoe UI", Font.BOLD, 18);
    private final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    private BUS_KhuyenMai busKhuyenMai; 

    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTable tblKhuyenMai;
    private SearchField searchField;
    private List<RoundedButton> filterButtons = new ArrayList<>();
    
    private JLabel lblKpiActive, lblKpiUpcoming, lblKpiPaused;
    private JLabel lblCount;

    private JPanel pnlDetail;
    private JLabel lblDetAvatar, lblDetName, lblDetId;
    private JLabel lblDetType, lblDetDiscount, lblDetMinOrder, lblDetTarget, lblDetTime;
    private JLabel lblDetStatus;
    
    private RoundedButton btnDetailEdit, btnDetailToggle;

    private JTextField txtTienMua, txtDiemThuong, txtTienDoi, txtDiemToiThieu;

    public ManHinhKhuyenMai() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        busKhuyenMai = new BUS_KhuyenMai(); 
        
        this.setBackground(COLOR_BG);
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(15, 25, 20, 25)); 
        
        JPanel mainContent = createMainContent();
        
        JScrollPane scrollMain = new JScrollPane(mainContent);
        scrollMain.setBorder(null);
        scrollMain.getViewport().setBackground(COLOR_BG);
        scrollMain.getVerticalScrollBar().setUnitIncrement(16);
        scrollMain.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollMain.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        
        this.add(scrollMain, BorderLayout.CENTER);
        
        loadDataFromDatabase(); 
        loadCauHinhTichDiem();
    }

    private JPanel createMainContent() {
        JPanel pnlMain = new JPanel();
        pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.Y_AXIS));
        pnlMain.setOpaque(false);

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(new EmptyBorder(0, 0, 15, 0));
        pnlHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        JLabel lblTitle = new JLabel("QUẢN LÝ KHUYẾN MẠI");
        lblTitle.setFont(FONT_H1);
        lblTitle.setForeground(COLOR_PRIMARY);
        lblTitle.setIcon(new MenuIcon("GIFT"));
        
        JPanel pnlRightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRightHeader.setOpaque(false);
        
        searchField = new SearchField(250, "Tìm khuyến mại...");
        searchField.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterData(); }
            @Override public void removeUpdate(DocumentEvent e) { filterData(); }
            @Override public void changedUpdate(DocumentEvent e) { filterData(); }
            private void filterData() {
                String text = searchField.getTextField().getText();
                if (text.trim().isEmpty() || text.equals("Tìm khuyến mại...")) {
                    if (rowSorter != null) rowSorter.setRowFilter(null);
                } else {
                    if (rowSorter != null) rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
                }
                updateKPI();
            }
        });

        RoundedButton btnRefresh = new RoundedButton("Làm mới", COLOR_TEXT_MUTED, Color.WHITE);
        btnRefresh.setIcon(new MenuIcon("REFRESH"));
        btnRefresh.setPreferredSize(new Dimension(140, 36));
        btnRefresh.addActionListener(e -> {
            searchField.getTextField().setText("Tìm khuyến mại..."); 
            searchField.getTextField().setForeground(Color.decode("#ADB5BD"));
            for (RoundedButton b : filterButtons) {
                b.setColors(COLOR_CARD, COLOR_TEXT_MUTED);
                b.setBorderColor(COLOR_BORDER);
            }
            if (!filterButtons.isEmpty()) {
                filterButtons.get(0).setColors(COLOR_INFO, Color.WHITE);
                filterButtons.get(0).setBorderColor(COLOR_INFO);
            }
            if(rowSorter != null) rowSorter.setRowFilter(null);
            loadDataFromDatabase();
            updateKPI();
        });

        RoundedButton btnAdd = new RoundedButton("Thêm mới", COLOR_DANGER, Color.WHITE);
        btnAdd.setIcon(new MenuIcon("ADD"));
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

        JPanel pnlKPI = createTopKPISecton();
        JPanel pnlTichDiem = createTichDiemSection();

        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlFilters.setOpaque(false);
        pnlFilters.setBorder(new EmptyBorder(15, 0, 10, 0));
        pnlFilters.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        JLabel lblIconFilter = new JLabel("Lọc trạng thái: ");
        lblIconFilter.setIcon(new MenuIcon("CHART"));  
        lblIconFilter.setFont(FONT_BOLD);
        lblIconFilter.setForeground(COLOR_TEXT_MUTED);
        pnlFilters.add(lblIconFilter);
        
        String[] filters = {"Tất cả", "Đang hoạt động", "Sắp diễn ra", "Đã kết thúc", "Tạm dừng"};
        String[] filterIcons = {"LIST", "CHECK_CIRCLE", "CLOCK", "CLOSE", "WARNING"};
        
        for (int i = 0; i < filters.length; i++) {
            String filterName = filters[i];
            boolean isActive = filterName.equals("Tất cả");
            Color bgColor = isActive ? COLOR_INFO : COLOR_CARD;
            Color fgColor = isActive ? Color.WHITE : COLOR_TEXT_MUTED;
            
            RoundedButton btnFilter = new RoundedButton(filterName, bgColor, fgColor);
            btnFilter.setIcon(new MenuIcon(filterIcons[i]));
            if (!isActive) btnFilter.setBorderColor(COLOR_BORDER);
            
            btnFilter.setPreferredSize(new Dimension(130, 34)); 
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
                updateKPI();
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

        JPanel pnlTableAndSidebar = new JPanel(new BorderLayout(20, 0));
        pnlTableAndSidebar.setOpaque(false);
        
        JPanel pnlTableWrapper = createTableSection();
        pnlDetail = createDetailSidebar();
        pnlDetail.setVisible(false); 
        
        pnlTableAndSidebar.add(pnlTableWrapper, BorderLayout.CENTER);
        pnlTableAndSidebar.add(pnlDetail, BorderLayout.EAST);

        pnlMain.add(pnlHeader);
        pnlMain.add(Box.createVerticalStrut(10));
        pnlMain.add(pnlKPI);
        pnlMain.add(Box.createVerticalStrut(15));
        pnlMain.add(pnlTichDiem);
        pnlMain.add(Box.createVerticalStrut(5));
        pnlMain.add(pnlFilterWrapper);
        pnlMain.add(Box.createVerticalStrut(5));
        pnlMain.add(pnlTableAndSidebar);

        return pnlMain;
    }

    private JPanel createTopKPISecton() {
        JPanel pnlWrapper = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlWrapper.setOpaque(false);
        pnlWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110)); 
        pnlWrapper.setPreferredSize(new Dimension(0, 110));
        
        KPICard cardActive = new KPICard("Đang hoạt động", "0", Color.decode("#10B981"), Color.decode("#F0FDF4"), "CHECK_CIRCLE");
        lblKpiActive = cardActive.getLblValue(); 
        
        KPICard cardUpcoming = new KPICard("Sắp diễn ra", "0", Color.decode("#F59E0B"), Color.decode("#FFFBEB"), "CLOCK");
        lblKpiUpcoming = cardUpcoming.getLblValue();
        
        KPICard cardPaused = new KPICard("Đã kết thúc / Tạm dừng", "0", Color.decode("#E1304C"), Color.decode("#FEF2F2"), "CANCEL");
        lblKpiPaused = cardPaused.getLblValue();
        
        pnlWrapper.add(cardActive);
        pnlWrapper.add(cardUpcoming);
        pnlWrapper.add(cardPaused);
        
        return pnlWrapper;
    }

    private JPanel createTichDiemSection() {
        JPanel pnlWrap = new JPanel(new BorderLayout(0, 15)); 
        pnlWrap.setBackground(Color.WHITE); 
        UIHelper.setRoundedCorners(pnlWrap, 20);  
        pnlWrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#D8B4FE"), 2), 
            BorderFactory.createEmptyBorder(20, 25, 20, 25) 
        ));
        pnlWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        JPanel pnlHead = new JPanel(new BorderLayout());
        pnlHead.setOpaque(false);
        JLabel lblTitle = new JLabel(" CHƯƠNG TRÌNH TÍCH ĐIỂM THƯỞNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(COLOR_PURPLE);
        lblTitle.setIcon(new MenuIcon("TAB_DOLLAR"));  
        
        RoundedButton btnSaveSetup = new RoundedButton("Lưu cài đặt", COLOR_PURPLE, Color.WHITE);
        btnSaveSetup.setIcon(new MenuIcon("SAVE"));
        btnSaveSetup.setPreferredSize(new Dimension(140, 36));
        btnSaveSetup.setFont(FONT_BOLD);
        
        btnSaveSetup.addActionListener(e -> {
            try {
                int tienMua = Integer.parseInt(txtTienMua.getText().trim());
                int diemThuong = Integer.parseInt(txtDiemThuong.getText().trim());
                int tienDoi = Integer.parseInt(txtTienDoi.getText().trim());
                int diemToiThieu = Integer.parseInt(txtDiemToiThieu.getText().trim());

                boolean success = busKhuyenMai.luuCauHinhTichDiem(tienMua, diemThuong, tienDoi, diemToiThieu);
                if (success) {
                    showNotification("Thành công", "Đã cập nhật hệ thống tích điểm thành công!\nMàn hình bán hàng đã có thể sử dụng cấu hình mới.", "success");
                } else {
                    showNotification("Lỗi", "Lỗi cập nhật cấu hình tích điểm vào CSDL!", "error");
                }
            } catch (NumberFormatException ex) {
                showNotification("Cảnh báo", "Vui lòng chỉ nhập số vào các ô cấu hình!", "warning");
            } 
        }); 

        pnlHead.add(lblTitle, BorderLayout.WEST);
        pnlHead.add(btnSaveSetup, BorderLayout.EAST);

        JPanel pnlBody = new JPanel(new GridLayout(1, 3, 20, 0)); 
        pnlBody.setOpaque(false);
        
        txtTienMua = new JTextField("10000"); txtDiemThuong = new JTextField("1");
        txtTienDoi = new JTextField("100"); txtDiemToiThieu = new JTextField("100");

        JLabel lblDiem1 = new JLabel("1 Điểm  =");
        lblDiem1.setFont(FONT_BOLD);
        JLabel lblToiThieu = new JLabel("Tối thiểu");
        lblToiThieu.setFont(FONT_BOLD);

        pnlBody.add(createPointConfigCard("Tích điểm khi mua", "CART", Color.decode("#10B981"), txtTienMua, "VNĐ  =", txtDiemThuong, "Điểm", "Khách mua hóa đơn đạt mức này sẽ được cộng điểm."));
        pnlBody.add(createPointConfigCard("Giá trị quy đổi", "REFRESH", Color.decode("#3B82F6"), lblDiem1, null, txtTienDoi, "VNĐ", "Mỗi điểm tương ứng với số tiền được giảm trừ."));
        pnlBody.add(createPointConfigCard("Điều kiện áp dụng", "CHECK_CIRCLE", Color.decode("#F59E0B"), lblToiThieu, null, txtDiemToiThieu, "Điểm", "Số điểm tối thiểu khách cần có để sử dụng thanh toán."));

        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlFooter.setBackground(Color.decode("#FAF5FF")); 
        UIHelper.setRoundedCorners(pnlFooter, 12);
        
        JLabel lblSummary = new JLabel("<html><b>📌 Lưu ý:</b> Hệ thống sẽ tự động cộng điểm cho khách sau khi thanh toán hóa đơn thành công. Khách có thể dùng điểm để trừ thẳng vào tiền hàng ở lần mua tiếp theo.</html>");
        lblSummary.setFont(FONT_SMALL);
        lblSummary.setForeground(Color.decode("#6B21A8")); 
        pnlFooter.add(lblSummary);

        pnlWrap.add(pnlHead, BorderLayout.NORTH);
        pnlWrap.add(pnlBody, BorderLayout.CENTER);
        pnlWrap.add(pnlFooter, BorderLayout.SOUTH);

        return pnlWrap;
    }

    private JPanel createPointConfigCard(String title, String iconName, Color color, Component input1, String textMid, Component input2, String textEnd, String hint) {
        JPanel pnlCard = new JPanel(new BorderLayout(0, 10));
        pnlCard.setBackground(COLOR_BG); 
        UIHelper.setRoundedCorners(pnlCard, 15);  
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.decode("#E5E7EB"), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15) 
        ));

        JLabel lblTop = new JLabel(" " + title);
        lblTop.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTop.setForeground(color);
        lblTop.setIcon(new MenuIcon(iconName)); 
        
        JPanel pnlInput = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlInput.setOpaque(false);
        
        if (input1 instanceof JTextField) styleTextField((JTextField) input1);
        if (input2 instanceof JTextField) styleTextField((JTextField) input2);

        pnlInput.add(input1);
        if(textMid != null) {
            JLabel lblMid = new JLabel(textMid);
            lblMid.setFont(FONT_BOLD);
            pnlInput.add(lblMid);
        }
        if(input2 != null) pnlInput.add(input2);
        if(textEnd != null) {
            JLabel lblEnd = new JLabel(textEnd);
            lblEnd.setFont(FONT_BOLD);
            pnlInput.add(lblEnd);
        }

        JLabel lblHint = new JLabel("<html><i>" + hint + "</i></html>");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHint.setForeground(COLOR_TEXT_MUTED);

        pnlCard.add(lblTop, BorderLayout.NORTH);
        pnlCard.add(pnlInput, BorderLayout.CENTER);
        pnlCard.add(lblHint, BorderLayout.SOUTH);
        
        return pnlCard;
    }

    private void styleTextField(JTextField txt) {
        txt.setPreferredSize(new Dimension(80, 32));
        txt.setHorizontalAlignment(JTextField.CENTER);
        txt.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txt.setForeground(COLOR_DANGER); 
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));
    }

    private void loadCauHinhTichDiem() {
        try {
            int[] cauHinh = busKhuyenMai.layCauHinhTichDiem();
            if (cauHinh != null && cauHinh.length == 4) {
                txtTienMua.setText(String.valueOf(cauHinh[0]));
                txtDiemThuong.setText(String.valueOf(cauHinh[1]));
                txtTienDoi.setText(String.valueOf(cauHinh[2]));
                txtDiemToiThieu.setText(String.valueOf(cauHinh[3]));
            }
        } catch (Exception ignored) {}
    }

    private JPanel createTableSection() {
        JPanel pnlWrapper = new JPanel(new BorderLayout());
        pnlWrapper.setBackground(COLOR_CARD);
        UIHelper.setRoundedCorners(pnlWrapper, 20);  
        pnlWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        JPanel pnlTableHeaderWrapper = new JPanel(new BorderLayout());
        pnlTableHeaderWrapper.setBackground(COLOR_PRIMARY); 
        pnlTableHeaderWrapper.setBorder(new EmptyBorder(12, 15, 12, 15));
        UIHelper.setRoundedCorners(pnlTableHeaderWrapper, 20);
        
        JLabel lblListTitle = new JLabel("Danh sách chương trình khuyến mại");
        lblListTitle.setFont(FONT_BOLD);
        lblListTitle.setForeground(Color.WHITE);
        lblListTitle.setIcon(new MenuIcon("BOX"));  
        
        JLabel lblListHint = new JLabel("Bấm vào dòng để xem chi tiết - Nhấn Toggle để bật/tắt");
        lblListHint.setFont(FONT_SMALL);
        lblListHint.setForeground(Color.decode("#A0AAB5"));
        
        pnlTableHeaderWrapper.add(lblListTitle, BorderLayout.WEST);
        pnlTableHeaderWrapper.add(lblListHint, BorderLayout.EAST);

        String[] columns = {"Mã KM", "Tên chương trình", "Loại", "Giá trị / Kèm theo", "Đơn tối thiểu", "Áp dụng", "Thời gian", "Trạng thái", "Chi tiết", "Bật/Tắt"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) { return (columnIndex == 9) ? Boolean.class : String.class; }
        };
        
        tblKhuyenMai = new JTable(tableModel);
        tblKhuyenMai.setRowHeight(55); 
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

        DefaultTableCellRenderer highlightRenderer = new DefaultTableCellRenderer() {
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
        tblKhuyenMai.getColumnModel().getColumn(0).setMaxWidth(80);
        tblKhuyenMai.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);   
        tblKhuyenMai.getColumnModel().getColumn(2).setCellRenderer(leftRenderer);   
        tblKhuyenMai.getColumnModel().getColumn(3).setCellRenderer(highlightRenderer); 
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

                if (viewCol == 9) { 
                    boolean currentState = (boolean) tableModel.getValueAt(modelRow, 9);
                    boolean newState = !currentState;
                    String maKM = tableModel.getValueAt(modelRow, 0).toString();
                    
                    if (newState) {
                        String timeStr = tableModel.getValueAt(modelRow, 6).toString();
                        try {
                            if (timeStr.contains(" - ")) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                                Date start = sdf.parse(timeStr.split(" - ")[0]);
                                Date end = sdf.parse(timeStr.split(" - ")[1]);
                                Date now = new Date();
                                
                                if (now.after(end)) {
                                    showNotification("Khuyến mãi hết hạn", 
                                        "Chương trình này đã hết hạn.\nVui lòng sửa lại ngày hiệu lực để kích hoạt.", 
                                        "error");
                                    return;
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    
                    busKhuyenMai.capNhatTrangThai(maKM, newState);
                    tableModel.setValueAt(newState, modelRow, 9);
                    if (newState) {
                        String timeStr = tableModel.getValueAt(modelRow, 6).toString();
                        String tStatus = "Đang hoạt động";
                        try {
                            if (timeStr.contains(" - ")) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                                Date start = sdf.parse(timeStr.split(" - ")[0]);
                                Date end = sdf.parse(timeStr.split(" - ")[1]);
                                Date now = new Date();
                                if (now.before(start)) tStatus = "Sắp diễn ra";
                                else if (now.after(end)) tStatus = "Đã kết thúc";
                            }
                        } catch (Exception ex) {}
                        tableModel.setValueAt(tStatus, modelRow, 7);
                    } else {
                        tableModel.setValueAt("Tạm dừng", modelRow, 7);
                    }
                    
                    updateKPI();
                    if(pnlDetail.isVisible() && lblDetId.getText().equals(tableModel.getValueAt(modelRow, 0).toString())) {
                        updateDetailSidebar(modelRow);
                    }
                    tblKhuyenMai.repaint();
                    showNotification("Thành công", "Cập nhật trạng thái thành công!", "success"); 
                } else {
                    updateDetailSidebar(modelRow);
                    pnlDetail.setVisible(true);
                    revalidate();
                    repaint();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblKhuyenMai);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

        pnlWrapper.add(pnlTableHeaderWrapper, BorderLayout.NORTH);
        pnlWrapper.add(scrollPane, BorderLayout.CENTER);
        
        return pnlWrapper;
    }

    private JPanel createDetailSidebar() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setPreferredSize(new Dimension(320, 0));
        pnl.setBackground(COLOR_CARD);
        UIHelper.setRoundedCorners(pnl, 20);  
        pnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        JPanel pnlDetHeader = new JPanel(new BorderLayout(10, 0));
        pnlDetHeader.setBackground(Color.WHITE);
        pnlDetHeader.setBorder(new EmptyBorder(15, 15, 10, 10));

        lblDetAvatar = new JLabel("%", SwingConstants.CENTER);
        lblDetAvatar.setOpaque(true);
        lblDetAvatar.setBackground(COLOR_DANGER); 
        lblDetAvatar.setForeground(Color.WHITE);
        lblDetAvatar.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblDetAvatar.setPreferredSize(new Dimension(50, 50));
        UIHelper.setRoundedCorners(lblDetAvatar, 25);

        JPanel pnlName = new JPanel(new GridLayout(2, 1));
        pnlName.setBackground(Color.WHITE);
        lblDetName = new JLabel("---");
        lblDetName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblDetId = new JLabel("---");
        lblDetId.setForeground(Color.GRAY);
        pnlName.add(lblDetName);
        pnlName.add(lblDetId);

        JButton btnCloseDet = new JButton("×");
        btnCloseDet.setFont(new Font("Arial", Font.BOLD, 20));
        btnCloseDet.setForeground(Color.GRAY);
        btnCloseDet.setBorderPainted(false);
        btnCloseDet.setContentAreaFilled(false);
        btnCloseDet.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCloseDet.addActionListener(e -> pnlDetail.setVisible(false));

        pnlDetHeader.add(lblDetAvatar, BorderLayout.WEST);
        pnlDetHeader.add(pnlName, BorderLayout.CENTER);
        pnlDetHeader.add(btnCloseDet, BorderLayout.EAST);

        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(10, 15, 10, 15));

        lblDetType = addDetailRow(pnlBody, "Hình thức:", "---");
        lblDetDiscount = addDetailRow(pnlBody, "Giá trị:", "---");
        lblDetMinOrder = addDetailRow(pnlBody, "Đơn tối thiểu:", "---");
        lblDetTarget = addDetailRow(pnlBody, "Áp dụng cho:", "---");
        lblDetTime = addDetailRow(pnlBody, "Thời gian:", "---");

        JPanel pnlStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlStatus.setBackground(Color.WHITE);
        pnlStatus.setBorder(new EmptyBorder(10, 0, 0, 0));
        JLabel lblSt = new JLabel("Trạng thái: ");
        lblSt.setPreferredSize(new Dimension(100, 25));
        lblSt.setForeground(Color.GRAY);
        lblDetStatus = new JLabel("---");
        lblDetStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlStatus.add(lblSt); pnlStatus.add(lblDetStatus);
        pnlBody.add(pnlStatus);

        JPanel pnlFooterActions = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlFooterActions.setBackground(Color.WHITE);
        pnlFooterActions.setBorder(new EmptyBorder(10, 15, 15, 15));

        btnDetailEdit = new RoundedButton("Chỉnh sửa", COLOR_INFO, Color.WHITE);
        btnDetailEdit.setIcon(new MenuIcon("EDIT"));
        
        btnDetailToggle = new RoundedButton("Tạm dừng / Kích hoạt", Color.decode("#F3F4F6"), COLOR_TEXT_MAIN);
        btnDetailToggle.setBorderColor(COLOR_BORDER);
        btnDetailToggle.setIcon(new MenuIcon("CANCEL"));

        btnDetailEdit.addActionListener(e -> {
            int row = -1;
            String currentId = lblDetId.getText();
            for(int i=0; i<tableModel.getRowCount(); i++) {
                if(tableModel.getValueAt(i, 0).toString().equals(currentId)) {
                    row = i;
                    break;
                }
            }
            
            if (row >= 0) {
                Map<String, Object> dataRow = new HashMap<>();
                for (int i = 0; i < tableModel.getColumnCount(); i++) dataRow.put(String.valueOf(i), tableModel.getValueAt(row, i));
                new PromoDialog(dataRow).showDialog();
            }
        });

        btnDetailToggle.addActionListener(e -> {
            int row = -1;
            String currentId = lblDetId.getText();
            for(int i=0; i<tableModel.getRowCount(); i++) {
                if(tableModel.getValueAt(i, 0).toString().equals(currentId)) {
                    row = i;
                    break;
                }
            }
            
            if (row >= 0) {
                boolean currentState = (boolean) tableModel.getValueAt(row, 9);
                boolean newState = !currentState;
                
                if (newState) {
                    String timeStr = tableModel.getValueAt(row, 6).toString();
                    try {
                        if (timeStr.contains(" - ")) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            Date start = sdf.parse(timeStr.split(" - ")[0]);
                            Date end = sdf.parse(timeStr.split(" - ")[1]);
                            Date now = new Date();
                            
                            if (now.after(end)) {
                                showNotification("Khuyến mãi hết hạn", 
                                    "Chương trình này đã hết hạn.\nVui lòng sửa lại ngày hiệu lực để kích hoạt.", 
                                    "error");
                                return;
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                
                busKhuyenMai.capNhatTrangThai(currentId, newState);
                tableModel.setValueAt(newState, row, 9);
                
                if (newState) {
                    String timeStr = tableModel.getValueAt(row, 6).toString();
                    String tStatus = "Đang hoạt động";
                    try {
                        if (timeStr.contains(" - ")) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            Date start = sdf.parse(timeStr.split(" - ")[0]);
                            Date end = sdf.parse(timeStr.split(" - ")[1]);
                            Date now = new Date();
                            if (now.before(start)) tStatus = "Sắp diễn ra";
                            else if (now.after(end)) tStatus = "Đã kết thúc";
                        }
                    } catch (Exception ex) {}
                    tableModel.setValueAt(tStatus, row, 7);
                } else {
                    tableModel.setValueAt("Tạm dừng", row, 7);
                }
                
                updateKPI();
                updateDetailSidebar(row);
                tblKhuyenMai.repaint();
                showNotification("Thành công", "Cập nhật trạng thái thành công!", "success");
            }
        });

        pnlFooterActions.add(btnDetailEdit);
        pnlFooterActions.add(btnDetailToggle);

        pnl.add(pnlDetHeader, BorderLayout.NORTH);
        JScrollPane scrollDet = new JScrollPane(pnlBody);
        scrollDet.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        scrollDet.getHorizontalScrollBar().setUI(new ModernScrollBarUI());
        pnl.add(scrollDet, BorderLayout.CENTER);
        pnl.add(pnlFooterActions, BorderLayout.SOUTH);

        return pnl;
    }

    private JLabel addDetailRow(JPanel parent, String label, String val) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel lbl = new JLabel(label);
        lbl.setPreferredSize(new Dimension(100, 25));
        lbl.setForeground(Color.GRAY);
        lbl.setIcon(new MenuIcon("GIFT"));  
        JLabel value = new JLabel(val);
        value.setFont(new Font("Segoe UI", Font.BOLD, 13));
        value.setForeground(COLOR_TEXT_MAIN);
        row.add(lbl); row.add(value);
        parent.add(row);
        return value;
    }

    private void updateDetailSidebar(int modelRow) {
        String id = tableModel.getValueAt(modelRow, 0).toString();
        String name = tableModel.getValueAt(modelRow, 1).toString();
        String type = tableModel.getValueAt(modelRow, 2).toString();
        String discount = tableModel.getValueAt(modelRow, 3).toString();
        String minOrder = tableModel.getValueAt(modelRow, 4).toString();
        String target = tableModel.getValueAt(modelRow, 5).toString();
        String time = tableModel.getValueAt(modelRow, 6).toString();
        String status = tableModel.getValueAt(modelRow, 7).toString();

        lblDetName.setText("<html><p style='width:180px'>"+name+"</p></html>");
        lblDetId.setText(id);
        lblDetType.setText(type);
        lblDetDiscount.setText(discount);
        lblDetDiscount.setForeground(COLOR_DANGER); 
        lblDetMinOrder.setText(minOrder);
        lblDetTarget.setText(target);
        lblDetTime.setText(time);
        lblDetStatus.setText(status);
        
        if(type.equals("Sản phẩm kèm theo")) {
            lblDetAvatar.setText("🎁");
            lblDetAvatar.setIcon(new MenuIcon("PACKAGE"));
        } else {
            lblDetAvatar.setText("%");
            lblDetAvatar.setIcon(null);
        }

        if(status.equalsIgnoreCase("Đã kết thúc") || status.equalsIgnoreCase("Tạm dừng")) {
            lblDetStatus.setForeground(COLOR_TEXT_MUTED);
            lblDetAvatar.setBackground(COLOR_TEXT_MUTED);
            btnDetailToggle.setText("Kích hoạt lại");
            btnDetailToggle.setIcon(new MenuIcon("REFRESH"));
        } else if(status.equalsIgnoreCase("Sắp diễn ra")) {
            lblDetStatus.setForeground(Color.decode("#D97706"));
            lblDetAvatar.setBackground(Color.decode("#D97706"));
            btnDetailToggle.setText("Tạm dừng");
            btnDetailToggle.setIcon(new MenuIcon("CANCEL"));
        } else {
            lblDetStatus.setForeground(COLOR_SUCCESS);
            lblDetAvatar.setBackground(COLOR_DANGER);
            btnDetailToggle.setText("Tạm dừng");
            btnDetailToggle.setIcon(new MenuIcon("CANCEL"));
        }
    }


    private class PromoDialog {
        private final JDialog dialog;
        private final FloatingField fldMaKM, fldName, fldMinOrder;
        private final RoundedComboBox cbType, cbTarget;
        private final ModernDateField dpStart, dpEnd;
        private final FloatingField fldSoNgay; 
        
        private final JPanel pnlGiamGia;
        private final FloatingField fldMucGiam;
        
        private final JPanel pnlTangPham;
        private final SuggestionField fldMaSPMua, fldMaSPTang;
        private final FloatingField fldSoLuongMua, fldSoLuongTang;
        
        private final JPanel pnlDieuKienWrapper;
        private final JPanel pnlDieuKien;
        private final boolean isEditMode;
        
        private boolean isSyncingDates = false; 

        public PromoDialog(Map<String, Object> existingData) {
            isEditMode = (existingData != null);
            dialog = new JDialog(SwingUtilities.getWindowAncestor(ManHinhKhuyenMai.this), "", Dialog.ModalityType.APPLICATION_MODAL);
            
            dialog.setUndecorated(true);
            dialog.setBackground(new Color(0, 0, 0, 0));

            // THÊM DROP SHADOW CHO WRAPPER ĐỂ FORM NỔI LÊN KHÔNG BỊ CHÌM DƯỚI NỀN
            JPanel wrapper = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Vẽ các lớp bóng mờ (shadow)
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.fillRoundRect(6, 6, getWidth() - 12, getHeight() - 12, 20, 20);
                    g2.setColor(new Color(0, 0, 0, 15));
                    g2.fillRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 22, 22);
                    
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            wrapper.setOpaque(false);
            wrapper.setBorder(new EmptyBorder(10, 10, 15, 10)); // Khoảng trống cho Shadow

            JPanel mainPanel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                    
                    g2.setColor(COLOR_PRIMARY); 
                    g2.setStroke(new BasicStroke(2.0f)); 
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
                    
                    g2.dispose();
                }
            };
            mainPanel.setOpaque(false);

            JPanel pnlHeader = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(COLOR_PRIMARY);
                    g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() + 15, 15, 15); 
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pnlHeader.setOpaque(false);
            pnlHeader.setPreferredSize(new Dimension(0, 50));
            pnlHeader.setBorder(new EmptyBorder(0, 20, 0, 15));
            
            String titleText = isEditMode ? "Chỉnh sửa chương trình" : "Thêm chương trình khuyến mại mới";
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
            pnlBody.setBorder(new EmptyBorder(15, 20, 15, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL; 
            gbc.insets = new Insets(6, 8, 6, 8); 
            gbc.weightx = 1.0;

            fldMaKM = new FloatingField("Mã chương trình", "VD: KM001 (Tự tạo)", "BOX");
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; pnlBody.add(fldMaKM, gbc);

            fldName = new FloatingField("<html>Tên chương trình <font color='#E1304C'>*</font></html>", "VD: Khuyến mãi mua 3 tặng 1...", "LIST");
            gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; pnlBody.add(fldName, gbc);

            String[] types = {"Giảm theo phần trăm (%)", "Sản phẩm kèm theo"};
            cbType = new RoundedComboBox(types);
            gbc.gridwidth = 2; gbc.gridx = 0; gbc.gridy = 2; 
            pnlBody.add(labeled("Loại hình thức", cbType, null), gbc); 
            
            pnlGiamGia = new JPanel(new BorderLayout(0, 10)); 
            pnlGiamGia.setOpaque(false);
            pnlGiamGia.setBorder(new EmptyBorder(15, 5, 5, 5));
            
            fldMucGiam = new FloatingField("Mức giảm (%) (Từ 1-100)", "VD: 10", "TAB_CHART");
            fldMucGiam.getTextField().addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    if (!Character.isDigit(e.getKeyChar())) e.consume();
                }
                public void keyReleased(KeyEvent e) {
                    try {
                        int val = Integer.parseInt(fldMucGiam.getTextField().getText());
                        if (val > 100) fldMucGiam.getTextField().setText("100");
                        if (val < 1 && !fldMucGiam.getTextField().getText().isEmpty()) fldMucGiam.getTextField().setText("1");
                    } catch (Exception ignored) {}
                }
            });
            pnlGiamGia.add(fldMucGiam, BorderLayout.NORTH); 
            
            JPanel pnlQuickWrapper = new JPanel(new BorderLayout());
            pnlQuickWrapper.setOpaque(false);
            pnlQuickWrapper.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_BORDER), "Chọn mức giảm nhanh"));
            
            JPanel pnlQuickPercents = new JPanel(new GridLayout(1, 6, 10, 0));
            pnlQuickPercents.setOpaque(false);
            pnlQuickPercents.setBorder(new EmptyBorder(5, 5, 5, 5));
            
            String[] quickVals = {"5", "10", "15", "20", "30", "50"};
            Color[] quickColors = {
                Color.decode("#007BFF"), Color.decode("#A200FF"), Color.decode("#10B981"), 
                Color.decode("#F59E0B"), Color.decode("#E1304C"), Color.decode("#6C757D")
            };
            
            // THAY NÚT MẶC ĐỊNH BẰNG ROUNDEDBUTTON CHO ĐỠ THÔ
            for (int i = 0; i < quickVals.length; i++) {
                String val = quickVals[i];
                RoundedButton btnQuick = new RoundedButton(val + "%", Color.WHITE, quickColors[i]);
                btnQuick.setBorderColor(quickColors[i]);
                btnQuick.setPreferredSize(new Dimension(60, 32));
                btnQuick.addActionListener(e -> fldMucGiam.getTextField().setText(val));
                pnlQuickPercents.add(btnQuick);
            }
            pnlQuickWrapper.add(pnlQuickPercents, BorderLayout.CENTER);
            pnlGiamGia.add(pnlQuickWrapper, BorderLayout.CENTER);
            
            pnlTangPham = new JPanel(new BorderLayout(0, 10));
            pnlTangPham.setOpaque(false);
            pnlTangPham.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(COLOR_BORDER), "Cấu hình Mua X tặng Y"));
            
            JPanel pnlFields = new JPanel(new GridLayout(2, 1, 0, 10));
            pnlFields.setOpaque(false);

            List<String> khoSanPham = busKhuyenMai.layDanhSachTenSanPham();
            if (khoSanPham == null || khoSanPham.isEmpty()) {
                khoSanPham = Arrays.asList("Không có dữ liệu sản phẩm");
            }

            fldMaSPMua = new SuggestionField("Sản phẩm cần mua", "VD: Panadol", khoSanPham, "PACKAGE");
            fldSoLuongMua = new FloatingField("Số lượng", "VD: 3", "CART");

            fldMaSPTang = new SuggestionField("Sản phẩm được tặng", "VD: Khẩu trang", khoSanPham, "GIFT");
            fldSoLuongTang = new FloatingField("Số lượng tặng", "VD: 1", "GIFT");

            JPanel pnlMua = new JPanel(new GridBagLayout()); pnlMua.setOpaque(false);
            GridBagConstraints gM = new GridBagConstraints(); gM.fill = GridBagConstraints.HORIZONTAL;
            gM.gridx=0; gM.weightx=0.7; gM.insets=new Insets(0,0,0,5); pnlMua.add(fldMaSPMua, gM);
            gM.gridx=1; gM.weightx=0.3; gM.insets=new Insets(0,5,0,0); pnlMua.add(fldSoLuongMua, gM);

            JPanel pnlTang = new JPanel(new GridBagLayout()); pnlTang.setOpaque(false);
            GridBagConstraints gT = new GridBagConstraints(); gT.fill = GridBagConstraints.HORIZONTAL;
            gT.gridx=0; gT.weightx=0.7; gT.insets=new Insets(0,0,0,5); pnlTang.add(fldMaSPTang, gT);
            gT.gridx=1; gT.weightx=0.3; gT.insets=new Insets(0,5,0,0); pnlTang.add(fldSoLuongTang, gT);

            pnlFields.add(pnlMua);
            pnlFields.add(pnlTang);
            
            pnlTangPham.add(pnlFields, BorderLayout.NORTH); 

            CardLayout cardLayout = new CardLayout();
            JPanel pnlDynamicOptions = new JPanel(cardLayout);
            pnlDynamicOptions.setOpaque(false);
            pnlDynamicOptions.add(pnlGiamGia, "GIAM_GIA");
            pnlDynamicOptions.add(pnlTangPham, "TANG_PHAM");
            
            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
            pnlBody.add(pnlDynamicOptions, gbc);
            
            CardLayout condLayout = new CardLayout();
            pnlDieuKienWrapper = new JPanel(condLayout);
            pnlDieuKienWrapper.setOpaque(false);

            pnlDieuKien = new JPanel(new GridLayout(1, 2, 10, 10));
            pnlDieuKien.setOpaque(false);
            
            fldMinOrder = new FloatingField("Đơn tối thiểu (VNĐ)", "VD: 200000", "TAB_DOLLAR");
            fldMinOrder.getTextField().addKeyListener(new KeyAdapter() { public void keyTyped(KeyEvent e) { if(!Character.isDigit(e.getKeyChar())) e.consume(); }});
            
            String[] targets = {"Hóa đơn", "Sản phẩm"};
            cbTarget = new RoundedComboBox(targets);

            pnlDieuKien.add(fldMinOrder);
            pnlDieuKien.add(labeled("Đối tượng áp dụng", cbTarget, null));
            
            JPanel pnlNoDieuKien = new JPanel(new BorderLayout());
            pnlNoDieuKien.setOpaque(false);
            JLabel lblNoCond = new JLabel("<html><i>* Khuyến mãi tặng phẩm áp dụng cho mọi hóa đơn thỏa mãn số lượng mua.</i></html>");
            lblNoCond.setForeground(COLOR_TEXT_MUTED);
            lblNoCond.setBorder(new EmptyBorder(10, 5, 10, 5));
            pnlNoDieuKien.add(lblNoCond, BorderLayout.CENTER);

            pnlDieuKienWrapper.add(pnlDieuKien, "GIAM_GIA");
            pnlDieuKienWrapper.add(pnlNoDieuKien, "TANG_PHAM");

            gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; 
            pnlBody.add(pnlDieuKienWrapper, gbc);

            cbType.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    boolean isGift = cbType.getSelectedIndex() == 1;
                    cardLayout.show(pnlDynamicOptions, isGift ? "TANG_PHAM" : "GIAM_GIA");
                    condLayout.show(pnlDieuKienWrapper, isGift ? "TANG_PHAM" : "GIAM_GIA");
                }
            });

            dpStart = new ModernDateField(dialog); 
            dpEnd = new ModernDateField(dialog);
            fldSoNgay = new FloatingField("Số ngày hiệu lực", "VD: 7", "CLOCK");
            
            JPanel pnlDates = new JPanel(new GridLayout(1, 3, 15, 0)); 
            pnlDates.setOpaque(false); 
            pnlDates.add(labeled("<html>Từ ngày <font color='#E1304C'>*</font></html>", dpStart, null)); 
            pnlDates.add(labeled("<html>Đến ngày <font color='#E1304C'>*</font></html>", dpEnd, null));
            pnlDates.add(fldSoNgay);

            gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
            pnlBody.add(pnlDates, gbc);
            
            fldSoNgay.getTextField().addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) { if(!Character.isDigit(e.getKeyChar())) e.consume(); }
            });

            fldSoNgay.getTextField().getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { syncEnd(); }
                public void removeUpdate(DocumentEvent e) { syncEnd(); }
                public void changedUpdate(DocumentEvent e) { syncEnd(); }
                private void syncEnd() {
                    if(isSyncingDates) return;
                    try {
                        int days = Integer.parseInt(fldSoNgay.getTextField().getText().trim());
                        if(days >= 0) {
                            isSyncingDates = true;
                            LocalDate start = LocalDate.parse(dpStart.getText(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                            dpEnd.txtDate.setText(start.plusDays(days).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                            isSyncingDates = false;
                        }
                    } catch (Exception ex) { isSyncingDates = false; }
                }
            });

            DocumentListener dateSync = new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { syncDays(); }
                public void removeUpdate(DocumentEvent e) { syncDays(); }
                public void changedUpdate(DocumentEvent e) { syncDays(); }
                private void syncDays() {
                    if(isSyncingDates) return;
                    try {
                        isSyncingDates = true;
                        LocalDate start = LocalDate.parse(dpStart.getText(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        LocalDate end = LocalDate.parse(dpEnd.getText(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
                        if(days >= 0) fldSoNgay.getTextField().setText(String.valueOf(days));
                        else fldSoNgay.getTextField().setText("0");
                        isSyncingDates = false;
                    } catch (Exception ex) { isSyncingDates = false; }
                }
            };
            dpStart.txtDate.getDocument().addDocumentListener(dateSync);
            dpEnd.txtDate.getDocument().addDocumentListener(dateSync);

            if (isEditMode) {
                fldMaKM.getTextField().setText(existingData.get("0").toString());
                fldMaKM.getTextField().setEditable(false); 
                fldName.getTextField().setText(existingData.get("1").toString());
                
                if (existingData.get("2").toString().equals("Sản phẩm kèm theo")) {
                    cbType.setSelectedIndex(1);
                    String valUI = existingData.get("3").toString();
                    if(valUI.startsWith("Tặng ")) {
                        String[] pts = valUI.replace("Tặng ", "").split(" ", 2);
                        if(pts.length == 2) { fldSoLuongTang.getTextField().setText(pts[0]); fldMaSPTang.getTextField().setText(pts[1]); }
                    }
                } else {
                    cbType.setSelectedIndex(0);
                    String sVal = existingData.get("3").toString().split("%")[0].trim();
                    if(sVal.endsWith(".0")) sVal = sVal.substring(0, sVal.length()-2);
                    fldMucGiam.getTextField().setText(sVal);
                }
                fldMinOrder.getTextField().setText(existingData.get("4").toString().replaceAll("[^0-9]", ""));
                String timeStr = existingData.get("6").toString();
                if(timeStr.contains(" - ")) { dpStart.txtDate.setText(timeStr.split(" - ")[0]); dpEnd.txtDate.setText(timeStr.split(" - ")[1]); }
            }

            SwingUtilities.invokeLater(() -> {
                isSyncingDates = false;
                dpStart.txtDate.setText(dpStart.getText()); 
            });

            // SỬA CHỖ NÀY: SET OPAQUE(FALSE) ĐỂ FOOTER KHÔNG CHE MẤT GÓC BO TRÒN BÊN DƯỚI
            JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(COLOR_BORDER);
                    g2.drawLine(20, 0, getWidth() - 20, 0); // Vẽ nét viền mỏng ngang trên cùng thay cho setBorder
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pnlFooter.setOpaque(false); // Quan trọng nhất: Để trong suốt
            
            RoundedButton btnCancel = new RoundedButton("Hủy bỏ", Color.WHITE, COLOR_TEXT_MAIN);
            btnCancel.setIcon(new MenuIcon("CLOSE"));
            btnCancel.setBorderColor(COLOR_BORDER); btnCancel.setPreferredSize(new Dimension(120, 38));
            btnCancel.addActionListener(e -> dialog.dispose());

            RoundedButton btnSave = new RoundedButton(isEditMode ? "Cập nhật" : "Lưu chương trình", COLOR_DANGER, Color.WHITE);
            btnSave.setIcon(new MenuIcon("SAVE"));
            btnSave.setPreferredSize(new Dimension(150, 38));
            
            btnSave.addActionListener(e -> {
                String id = fldMaKM.getTextField().getText().trim();
                if(id.isEmpty() || id.startsWith("VD:")) id = "KM" + (System.currentTimeMillis() % 10000000);
                
                String ten = fldName.getTextField().getText().trim();
                if(ten.isEmpty() || ten.startsWith("VD:")) {
                    showNotification("Cảnh báo", "Vui lòng nhập Tên chương trình!", "warning");
                    return;
                }
                
                String donToiThieuStr = fldMinOrder.getTextField().getText().trim();
                if(donToiThieuStr.isEmpty() || donToiThieuStr.startsWith("VD:")) donToiThieuStr = "0";

                String ngayBatDauStr = dpStart.getText();
                String ngayKetThucStr = dpEnd.getText();
                
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate startD = LocalDate.parse(ngayBatDauStr, formatter);
                    LocalDate endD = LocalDate.parse(ngayKetThucStr, formatter);
                    
                    LocalDateTime start = startD.atStartOfDay();
                    LocalDateTime end = endD.atTime(LocalTime.MAX);
                    
                    if(start.isAfter(end)) {
                        showNotification("Cảnh báo", "Ngày kết thúc không được nhỏ hơn ngày bắt đầu!", "warning");
                        return;
                    }

                    KhuyenMai km = new KhuyenMai(id, ten, ten, LocalDateTime.now(), start, end);
                    HinhThucKhuyenMai ht = new HinhThucKhuyenMai();
                    ht.setId("HT" + id);
                    ht.setKhuyenMaiId(km);
                    
                    DieuKienKhuyenMai dk = null;
                    double donToiThieu = Double.parseDouble(donToiThieuStr);
                    if (donToiThieu > 0) {
                        dk = new DieuKienKhuyenMai("DK" + id, "GIA_TRI", "HOA_DON", donToiThieu, id);
                    }

                    if (cbType.getSelectedIndex() == 0) { 
                        String mucGiamStr = fldMucGiam.getTextField().getText().trim();
                        if(mucGiamStr.isEmpty() || mucGiamStr.startsWith("VD:")) mucGiamStr = "0";
                        
                        ht.setLoaiHinhThuc(LoaiHinhThuc.GIAM_THEO_PHAN_TRAM);
                        ht.setDoiTuongApDung(cbTarget.getSelectedIndex() == 0 ? DoiTuongApDung.HOA_DON : DoiTuongApDung.SAN_PHAM);
                        ht.setGiaTri(Double.parseDouble(mucGiamStr));
                        
                    } else { 
                        String spMua = fldMaSPMua.getTextField().getText().trim();
                        String spTang = fldMaSPTang.getTextField().getText().trim();
                        
                        if (spMua.isEmpty() || spMua.startsWith("VD:") || spTang.isEmpty() || spTang.startsWith("VD:")) {
                            showNotification("Cảnh báo", "Vui lòng nhập đủ mã sản phẩm Mua và Tặng!", "warning");
                            return;
                        }

                        int slMua = 1; try { slMua = Integer.parseInt(fldSoLuongMua.getTextField().getText().trim()); } catch(Exception ignored){}
                        int slTang = 1; try { slTang = Integer.parseInt(fldSoLuongTang.getTextField().getText().trim()); } catch(Exception ignored){}
                        
                        ht.setLoaiHinhThuc(LoaiHinhThuc.SAN_PHAM_KEM_THEO);
                        ht.setDoiTuongApDung(DoiTuongApDung.SAN_PHAM);
                        ht.setGiaTri(0);
                        ht.setSpYeuCau(spMua);
                        ht.setSlYeuCau(slMua);
                        ht.setSpTang(spTang);
                        ht.setSlTang(slTang);
                        
                        dk = null; 
                    }

                    boolean success = false;
                    if (!isEditMode) { 
                        success = busKhuyenMai.themKhuyenMaiToanDien(km, ht, dk);
                        if (success) showNotification("Thành công", "Tạo chương trình khuyến mại thành công!", "success");
                    } else { 
                        success = busKhuyenMai.capNhatKhuyenMaiToanDien(km, ht, dk);
                        if (success) showNotification("Thành công", "Cập nhật chương trình thành công!", "success");
                    }
                    
                    if (success) {
                        dialog.dispose();
                        
                        loadDataFromDatabase(); 
                        
                        try {
                        } catch (Exception ex) {}
                        
                        final String safeId = id;
                        if (isEditMode && pnlDetail.isVisible() && lblDetId.getText().equals(safeId)) {
                            SwingUtilities.invokeLater(() -> {
                                for(int i=0; i<tableModel.getRowCount(); i++) {
                                    if(tableModel.getValueAt(i, 0).toString().equals(safeId)) {
                                        updateDetailSidebar(i);
                                        break;
                                    }
                                }
                            });
                        }
                    } else {
                        showNotification("Lỗi", "Lưu thất bại! Vui lòng kiểm tra lại CSDL.", "error");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    showNotification("Lỗi", "Lỗi dữ liệu: " + ex.getMessage(), "error");
                }
            });

            pnlFooter.add(btnCancel); pnlFooter.add(btnSave);

            mainPanel.add(pnlHeader, BorderLayout.NORTH);
            mainPanel.add(pnlBody, BorderLayout.CENTER);
            mainPanel.add(pnlFooter, BorderLayout.SOUTH);

            wrapper.add(mainPanel, BorderLayout.CENTER);
            dialog.add(wrapper); 
            
            dialog.pack(); 
            // CỘNG THÊM KHÔNG GIAN BÙ CHO PHẦN ĐỔ BÓNG SHADOW CỦA WRAPPER
            dialog.setSize(750, dialog.getHeight() + 25);
            dialog.setLocationRelativeTo(null);
        }
        public void showDialog() { dialog.setVisible(true); }
    }

    private JPanel labeled(String labelText, JComponent component, String iconName) {
        JPanel panel = new JPanel(new BorderLayout(0, 8)); 
        panel.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(COLOR_TEXT_MAIN);
        
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }

    class TextFieldWithPlaceholder extends JTextField {
        private String placeholder;
        public TextFieldWithPlaceholder(String placeholder) {
            this.placeholder = placeholder;
            this.setFont(FONT_REGULAR);
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && placeholder != null && !placeholder.isEmpty()) {
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
        public FloatingField(String label, String placeholder, String iconName) {
            setLayout(new BorderLayout(0, 8)); 
            setOpaque(false);
            
            JLabel lbl = new JLabel(label); 
            lbl.setFont(FONT_BOLD); 
            lbl.setForeground(COLOR_TEXT_MAIN);
            
            textField = new TextFieldWithPlaceholder(placeholder);
            textField.setBorder(null); 
            textField.setOpaque(false); 
            textField.setPreferredSize(new Dimension(0, 36)); 
            
            JPanel pnlInput = new JPanel(new BorderLayout(10, 0));
            pnlInput.setBackground(Color.WHITE); 
            
            pnlInput.setBorder(new EmptyBorder(2, 10, 2, 10)); 
            UIHelper.setRoundedCorners(pnlInput, 8); 

            if (iconName != null && !iconName.isEmpty()) {
                JLabel lblIcon = new JLabel(new MenuIcon(iconName));
                lblIcon.setForeground(Color.decode("#9CA3AF")); 
                pnlInput.add(lblIcon, BorderLayout.WEST);
            }
            
            textField.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { pnlInput.setBackground(Color.decode("#F0F8FF")); pnlInput.repaint(); }
                public void focusLost(FocusEvent e) { pnlInput.setBackground(Color.WHITE); pnlInput.repaint(); }
            });
            
            pnlInput.add(textField, BorderLayout.CENTER);
            
            add(lbl, BorderLayout.NORTH); 
            add(pnlInput, BorderLayout.CENTER);
        }
        public JTextField getTextField() { return textField; }
    }

    class SuggestionField extends FloatingField {
        private JPopupMenu popupMenu;
        private List<String> dictionary;
        private boolean isSelecting = false; 

        public SuggestionField(String label, String placeholder, List<String> dictionary, String iconName) {
            super(label, placeholder, iconName);
            this.dictionary = dictionary;
            popupMenu = new JPopupMenu();
            popupMenu.setFocusable(false);

            getTextField().getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { if(!isSelecting) showSuggestions(); }
                public void removeUpdate(DocumentEvent e) { if(!isSelecting) showSuggestions(); }
                public void changedUpdate(DocumentEvent e) { if(!isSelecting) showSuggestions(); }
            });
        }

        private void showSuggestions() {
            SwingUtilities.invokeLater(() -> {
                popupMenu.setVisible(false); 
                popupMenu.removeAll();
                
                String rawText = getTextField().getText();
                if (rawText == null || rawText.trim().isEmpty()) { 
                    return; 
                }
                
                String text = rawText.toLowerCase();

                boolean hasItems = false;
                int count = 0; 
                
                for (String word : dictionary) {
                    if (word.toLowerCase().contains(text)) {
                        JMenuItem item = new JMenuItem(word);
                        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        item.setBackground(Color.WHITE);
                        item.setFont(FONT_REGULAR);
                        
                        item.addActionListener(e -> {
                            isSelecting = true; 
                            getTextField().setText(word);
                            getTextField().setForeground(COLOR_TEXT_MAIN);
                            popupMenu.setVisible(false);
                            isSelecting = false; 
                        });
                        
                        popupMenu.add(item);
                        hasItems = true;
                        count++;
                        
                        if (count >= 10) break; 
                    }
                }

                if (hasItems) {
                    popupMenu.pack(); 
                    popupMenu.show(getTextField(), 0, getTextField().getHeight());
                    getTextField().requestFocus(); 
                }
            });
        }
    }

    class RoundedComboBox extends JComboBox<String> {
        public RoundedComboBox(String[] items) {
            super(items); 
            setFont(FONT_REGULAR); 
            setPreferredSize(new Dimension(0, 38)); 
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                new EmptyBorder(2, 8, 2, 8)
            ));
        }
    }

    class SearchField extends JPanel {
        private TextFieldWithPlaceholder textField;
        public SearchField(int width, String placeholder) {
            setLayout(new BorderLayout(10, 0)); setOpaque(false);
            setPreferredSize(new Dimension(width, 36)); setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1), 
                BorderFactory.createEmptyBorder(0, 12, 0, 12)
            ));
            
            JLabel lblIcon = new JLabel(); lblIcon.setIcon(new MenuIcon("SEARCH"));
            
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
        
        public void setColors(Color bg, Color fg) { 
            this.bgColor = bg; 
            this.fgColor = fg; 
            setForeground(fg); 
            repaint(); 
        }
        public void setBorderColor(Color border) { this.borderColor = border; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor); 
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); 
            if (borderColor != null) { g2.setColor(borderColor); g2.setStroke(new BasicStroke(1.5f)); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10); }
            super.paintComponent(g); 
            g2.dispose();
        }
    }

    class KPICard extends JPanel {
        private JLabel lblValue;
        public KPICard(String title, String val, Color c, Color bg, String iconName) {
            setLayout(new BorderLayout());
            setBackground(bg); 
            UIHelper.setRoundedCorners(this, 10);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(c, 1), 
                BorderFactory.createEmptyBorder(15, 10, 15, 10)
            ));

            JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            pnlTop.setOpaque(false);
            
            JLabel lblIcon = new JLabel();
            lblIcon.setIcon(new MenuIcon(iconName)); 
            lblIcon.setForeground(c); 
            
            JLabel lblTitle = new JLabel(title); 
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
            lblTitle.setForeground(COLOR_TEXT_MAIN); 
            
            pnlTop.add(lblIcon);
            pnlTop.add(lblTitle);

            lblValue = new JLabel(val, SwingConstants.CENTER); 
            lblValue.setFont(new Font("Segoe UI", Font.BOLD, 32)); 
            lblValue.setForeground(c); 
            lblValue.setBorder(new EmptyBorder(5, 0, 0, 0));
            
            add(pnlTop, BorderLayout.NORTH);
            add(lblValue, BorderLayout.CENTER);
        }
        public JLabel getLblValue() { return lblValue; }
    }

    class ModernDateField extends JPanel {
        public JTextField txtDate; 
        public ModernDateField(JDialog parentDialog) {
            setLayout(new BorderLayout()); setOpaque(false);
            JPanel pnlWrapper = new JPanel(new BorderLayout(10, 0)); 
            pnlWrapper.setBackground(Color.WHITE); 
            
            pnlWrapper.setBorder(new EmptyBorder(2, 10, 2, 6));
            UIHelper.setRoundedCorners(pnlWrapper, 8); 

            txtDate = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            txtDate.setBorder(null);
            txtDate.setOpaque(false);
            txtDate.setFont(FONT_REGULAR); 
            txtDate.setPreferredSize(new Dimension(0, 34));
            
            JButton btnCalendar = new JButton(); 
            btnCalendar.setIcon(new MenuIcon("CALENDAR")); 
            btnCalendar.setForeground(COLOR_TEXT_MUTED); 
            btnCalendar.setContentAreaFilled(false); 
            btnCalendar.setBorderPainted(false);
            btnCalendar.setFocusPainted(false);
            btnCalendar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            txtDate.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { pnlWrapper.setBackground(Color.decode("#F0F8FF")); pnlWrapper.repaint(); }
                public void focusLost(FocusEvent e) { pnlWrapper.setBackground(Color.WHITE); pnlWrapper.repaint(); }
            });

            btnCalendar.addActionListener(e -> {
                ModernDatePicker picker = new ModernDatePicker(parentDialog, txtDate);
                picker.setVisible(true);
            });
            
            pnlWrapper.add(txtDate, BorderLayout.CENTER); pnlWrapper.add(btnCalendar, BorderLayout.EAST); add(pnlWrapper, BorderLayout.CENTER);
        }
        public String getText() { return txtDate.getText(); }
    }

    class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String status = (value == null) ? "" : value.toString();
            JPanel pnl = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = Color.WHITE, fg = COLOR_TEXT_MAIN;
                    if (status.equals("Đang hoạt động")) { bg = Color.decode("#D1FAE5"); fg = COLOR_SUCCESS; }
                    else if (status.equals("Sắp diễn ra")) { bg = Color.decode("#DBEAFE"); fg = Color.decode("#3B82F6"); }
                    else if (status.equals("Tạm dừng") || status.equals("Đã kết thúc")) { bg = Color.decode("#F3F4F6"); fg = Color.decode("#6B7280"); }
                    
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
            JLabel lblAction = new JLabel("Xem", SwingConstants.CENTER); 
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

    private void loadDataFromDatabase() {
        if (rowSorter != null) rowSorter.setRowFilter(null);
        tableModel.setRowCount(0);
        
        try {
            List<Object[]> listData = busKhuyenMai.layDanhSachKhuyenMaiChoTable();
            
            if (listData != null) {
                for (Object[] row : listData) {
                    tableModel.addRow(row);
                }
            }
        } catch (Exception ex) { 
            ex.printStackTrace();
        }
        
        tableModel.fireTableDataChanged();
        updateKPI();
    }

    private void updateKPI() {
        int dangHoatDong = 0, sapDienRa = 0, daKetThuc = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String status = tableModel.getValueAt(i, 7).toString();
            if (status.equals("Đang hoạt động")) dangHoatDong++;
            else if (status.equals("Sắp diễn ra")) sapDienRa++;
            else daKetThuc++;
        }
        if (lblKpiActive != null) lblKpiActive.setText(String.valueOf(dangHoatDong));
        if (lblKpiUpcoming != null) lblKpiUpcoming.setText(String.valueOf(sapDienRa));
        if (lblKpiPaused != null) lblKpiPaused.setText(String.valueOf(daKetThuc));
        
        if (lblCount != null) {
            int visible = rowSorter != null ? rowSorter.getViewRowCount() : tableModel.getRowCount();
            lblCount.setText(visible + " / " + tableModel.getRowCount() + " chương trình");
        }
    }

    public void setReadOnly(boolean readOnly) {
        if (!readOnly) return;
        disableButtonsByText(this, "Thêm mới", "Nhập Excel", "Thêm", "Xóa", "Sửa", "Lưu", "Lưu cài đặt", "Chỉnh sửa", "Xóa chương trình", "Lưu chương trình", "Cập nhật");
    }

    private void disableButtonsByText(java.awt.Container container, String... texts) {
        for (java.awt.Component c : container.getComponents()) {
            if (c instanceof javax.swing.JButton) {
                javax.swing.JButton btn = (javax.swing.JButton) c;
                for (String t : texts) {
                    if (t.equals(btn.getText())) { btn.setVisible(false); break; }
                }
            } else if (c instanceof java.awt.Container) {
                disableButtonsByText((java.awt.Container) c, texts);
            }
        }
    }

    private void showNotification(String title, String message, String type) {
        SwingUtilities.invokeLater(() -> {
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setUndecorated(true);
            dialog.setBackground(new Color(0, 0, 0, 0)); 
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            Color headerBg, iconBg, borderColor;
            String iconType;
            
            if (type.equals("success")) {
                headerBg = Color.decode("#10B981");
                iconBg = Color.decode("#D1FAE5");
                borderColor = Color.decode("#10B981");
                iconType = "CORRECT"; 
            } else if (type.equals("error")) {
                headerBg = Color.decode("#E1304C");
                iconBg = Color.decode("#FEE2E2");
                borderColor = Color.decode("#E1304C");
                iconType = "CANCEL"; 
            } else { // warning
                headerBg = Color.decode("#F59E0B");
                iconBg = Color.decode("#FEF3C7");
                borderColor = Color.decode("#F59E0B");
                iconType = "WARNING"; 
            }

            JPanel wrapper = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 20, 20);
                    g2.setColor(new Color(0, 0, 0, 15));
                    g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 22, 22);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            wrapper.setOpaque(false);
            wrapper.setBorder(new EmptyBorder(10, 10, 15, 10));

            JPanel mainPanel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                    g2.setColor(borderColor);
                    g2.setStroke(new BasicStroke(2.0f)); 
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
                    g2.dispose();
                }
            };
            mainPanel.setOpaque(false);

            JPanel pnlHeader = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(headerBg);
                    g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() + 15, 15, 15); 
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pnlHeader.setOpaque(false);
            pnlHeader.setPreferredSize(new Dimension(0, 50));
            pnlHeader.setBorder(new EmptyBorder(0, 20, 0, 15));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setForeground(Color.WHITE);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

            JButton btnClose = new JButton("×");
            btnClose.setFont(new Font("Arial", Font.BOLD, 22));
            btnClose.setForeground(Color.WHITE);
            btnClose.setBorder(null);
            btnClose.setContentAreaFilled(false);
            btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnClose.addActionListener(e -> dialog.dispose());

            pnlHeader.add(lblTitle, BorderLayout.WEST);
            pnlHeader.add(btnClose, BorderLayout.EAST);

            JPanel pnlBody = new JPanel(new BorderLayout());
            pnlBody.setOpaque(false);
            pnlBody.setBorder(new EmptyBorder(20, 20, 20, 20));

            JPanel pnlIconBox = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(iconBg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                    g2.dispose();
                }
            };
            pnlIconBox.setOpaque(false);
            pnlIconBox.setPreferredSize(new Dimension(60, 60));
            
            JLabel lblIcon = new JLabel();
            lblIcon.setIcon(new MenuIcon(iconType));
            lblIcon.setForeground(headerBg); 
            lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
            pnlIconBox.add(lblIcon, BorderLayout.CENTER);

            String formattedMessage = message.replace("\n", "<br>");
            String htmlMsg = "<html><p style='width: 250px; margin: 0; padding: 0; line-height: 1.3;'>" + formattedMessage + "</p></html>";
            
            JLabel lblMessage = new JLabel(htmlMsg);
            lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblMessage.setForeground(COLOR_TEXT_MAIN);
            lblMessage.setBorder(new EmptyBorder(5, 15, 0, 0)); 

            pnlBody.add(pnlIconBox, BorderLayout.WEST);
            pnlBody.add(lblMessage, BorderLayout.CENTER);

            JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(COLOR_BORDER);
                    g2.drawLine(20, 0, getWidth() - 20, 0); 
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pnlFooter.setOpaque(false);

            RoundedButton btnOk = new RoundedButton("OK", headerBg, Color.WHITE);
            btnOk.setIcon(new MenuIcon("CHECK_CIRCLE"));  
            btnOk.setPreferredSize(new Dimension(100, 38));
            btnOk.addActionListener(e -> dialog.dispose());

            pnlFooter.add(btnOk);

            mainPanel.add(pnlHeader, BorderLayout.NORTH);
            mainPanel.add(pnlBody, BorderLayout.CENTER);
            mainPanel.add(pnlFooter, BorderLayout.SOUTH);

            wrapper.add(mainPanel, BorderLayout.CENTER);
            dialog.add(wrapper);
            
            dialog.pack();
            int safeHeight = Math.max(dialog.getHeight() + 25, 220);
            dialog.setSize(460, safeHeight);
            
            dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
        });
    }

    private void showConfirmation(String title, String message, Runnable onConfirm) {
        SwingUtilities.invokeLater(() -> {
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setUndecorated(true);
            dialog.setBackground(new Color(0, 0, 0, 0));
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JPanel wrapper = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 20, 20);
                    g2.setColor(new Color(0, 0, 0, 15));
                    g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 22, 22);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            wrapper.setOpaque(false);
            wrapper.setBorder(new EmptyBorder(10, 10, 15, 10));

            JPanel mainPanel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                    g2.setColor(Color.decode("#F59E0B"));
                    g2.setStroke(new BasicStroke(2.0f)); 
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
                    g2.dispose();
                }
            };
            mainPanel.setOpaque(false);

            JPanel pnlHeader = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.decode("#F59E0B"));
                    g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() + 15, 15, 15);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pnlHeader.setOpaque(false);
            pnlHeader.setPreferredSize(new Dimension(0, 50));
            pnlHeader.setBorder(new EmptyBorder(0, 20, 0, 15));

            JLabel lblTitle = new JLabel(title);
            lblTitle.setForeground(Color.WHITE);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

            JButton btnClose = new JButton("×");
            btnClose.setFont(new Font("Arial", Font.BOLD, 22));
            btnClose.setForeground(Color.WHITE);
            btnClose.setBorder(null);
            btnClose.setContentAreaFilled(false);
            btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnClose.addActionListener(e -> dialog.dispose());

            pnlHeader.add(lblTitle, BorderLayout.WEST);
            pnlHeader.add(btnClose, BorderLayout.EAST);

            JPanel pnlBody = new JPanel(new BorderLayout());
            pnlBody.setOpaque(false);
            pnlBody.setBorder(new EmptyBorder(20, 20, 20, 20));

            JPanel pnlIconBox = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.decode("#FEF3C7"));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                    g2.dispose();
                }
            };
            pnlIconBox.setOpaque(false);
            pnlIconBox.setPreferredSize(new Dimension(60, 60));
            
            JLabel lblIcon = new JLabel();
            lblIcon.setIcon(new MenuIcon("HELP"));
            lblIcon.setForeground(Color.decode("#F59E0B")); 
            lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
            pnlIconBox.add(lblIcon, BorderLayout.CENTER);

            String formattedMessage = message.replace("\n", "<br>");
            String htmlMsg = "<html><p style='width: 250px; margin: 0; padding: 0; line-height: 1.3;'>" + formattedMessage + "</p></html>";
            
            JLabel lblMessage = new JLabel(htmlMsg);
            lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblMessage.setForeground(COLOR_TEXT_MAIN);
            lblMessage.setBorder(new EmptyBorder(5, 15, 0, 0));

            pnlBody.add(pnlIconBox, BorderLayout.WEST);
            pnlBody.add(lblMessage, BorderLayout.CENTER);

            JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(COLOR_BORDER);
                    g2.drawLine(20, 0, getWidth() - 20, 0); 
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            pnlFooter.setOpaque(false);

            RoundedButton btnCancel = new RoundedButton("Không", Color.WHITE, COLOR_TEXT_MAIN);
            btnCancel.setIcon(new MenuIcon("CLOSE"));  
            btnCancel.setBorderColor(COLOR_BORDER);
            btnCancel.setPreferredSize(new Dimension(100, 38));
            btnCancel.addActionListener(e -> dialog.dispose());

            RoundedButton btnYes = new RoundedButton("Có", Color.decode("#F59E0B"), Color.WHITE);
            btnYes.setIcon(new MenuIcon("CHECK_CIRCLE"));  
            btnYes.setPreferredSize(new Dimension(100, 38));
            btnYes.addActionListener(e -> {
                dialog.dispose();
                onConfirm.run();
            });

            pnlFooter.add(btnCancel);
            pnlFooter.add(btnYes);

            mainPanel.add(pnlHeader, BorderLayout.NORTH);
            mainPanel.add(pnlBody, BorderLayout.CENTER);
            mainPanel.add(pnlFooter, BorderLayout.SOUTH);

            wrapper.add(mainPanel, BorderLayout.CENTER);
            dialog.add(wrapper);
            
            dialog.pack();
            int safeHeight = Math.max(dialog.getHeight() + 25, 220);
            dialog.setSize(460, safeHeight);
            
            dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
        });
    }
}