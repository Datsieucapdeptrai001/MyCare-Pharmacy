package GUI;

import Utils.MenuIcon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class ManHinhHuongDan extends JPanel {

    private JTextField txtSearch;
    private JPanel contentPanel;
    private JScrollPane scrollPane;
    private JPanel sidebarPanel;

    private final List<TaiLieuItem> dsTaiLieu = new ArrayList<>();
    private final List<FaqItem> dsFaq = new ArrayList<>();
    private final List<SidebarBtn> sidebarBtns = new ArrayList<>();

    private int activeTab = 0; // 0: Tất cả, 1: Tài liệu, 2: FAQ, 3: Video

    private static final Color BG_APP = new Color(241, 245, 249);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BG_FAQ_CONTENT = new Color(248, 250, 252);
    
    private static final Color PRIMARY = new Color(14, 116, 144); 
    private static final Color PRIMARY_SOFT = new Color(224, 242, 254); 
    
    private static final Color TEXT = new Color(15, 23, 42);
    private static final Color SUBTEXT = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color DANGER = new Color(239, 68, 68);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_BOLD_14 = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_PLAIN_14 = new Font("Segoe UI", Font.PLAIN, 14);

    public ManHinhHuongDan() {
        initData();
        initUI();
        renderContent();
    }

    private void initUI() {
        setLayout(new BorderLayout(24, 0));
        setBackground(BG_APP);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(createSidebar(), BorderLayout.WEST);

        RoundedPanel rightMain = new RoundedPanel(24, BG_CARD, true);
        rightMain.setLayout(new BorderLayout(0, 20));
        rightMain.setBorder(new EmptyBorder(24, 28, 24, 28));
        rightMain.add(createHeader(), BorderLayout.NORTH);
        rightMain.add(createBody(), BorderLayout.CENTER);

        add(rightMain, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        RoundedPanel sidebar = new RoundedPanel(24, BG_CARD, true);
        sidebar.setLayout(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(new EmptyBorder(24, 20, 24, 20));

        JPanel topBox = new JPanel();
        topBox.setLayout(new BoxLayout(topBox, BoxLayout.Y_AXIS));
        topBox.setOpaque(false);

        JLabel lblTitle = new JLabel("DANH MỤC HỖ TRỢ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(SUBTEXT);
        lblTitle.setBorder(new EmptyBorder(0, 12, 16, 0));
        topBox.add(lblTitle);

        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setOpaque(false);

        addSidebarBtn("HOME", "Tất cả nội dung", 0);
        addSidebarBtn("DOCUMENT", "Tài liệu hướng dẫn", 1);
        addSidebarBtn("HELP", "Câu hỏi thường gặp", 2);
        addSidebarBtn("VIDEO", "Video hướng dẫn", 3);

        topBox.add(sidebarPanel);
        sidebar.add(topBox, BorderLayout.NORTH);

        RoundedPanel contactBox = new RoundedPanel(16, PRIMARY_SOFT, false);
        contactBox.setLayout(new BorderLayout());
        contactBox.setBorder(new EmptyBorder(20, 16, 20, 16));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel icPhone = new JLabel(new MenuIcon("PHONE"));
        icPhone.setForeground(PRIMARY);
        icPhone.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel txt1 = new JLabel("Cần trợ giúp trực tiếp?");
        txt1.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txt1.setForeground(PRIMARY);
        txt1.setAlignmentX(Component.CENTER_ALIGNMENT);
        txt1.setBorder(new EmptyBorder(8, 0, 4, 0));

        JLabel lblPhone = new JLabel("1900 9999");
        lblPhone.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblPhone.setForeground(PRIMARY);
        lblPhone.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(icPhone);
        inner.add(txt1);
        inner.add(lblPhone);

        contactBox.add(inner, BorderLayout.CENTER);
        
        JPanel bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setOpaque(false);
        bottomWrapper.add(contactBox, BorderLayout.SOUTH);
        
        sidebar.add(bottomWrapper, BorderLayout.CENTER);

        return sidebar;
    }

    private void addSidebarBtn(String icon, String text, int index) {
        SidebarBtn btn = new SidebarBtn(icon, text, index);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                activeTab = index;
                for (SidebarBtn b : sidebarBtns) b.repaint();
                txtSearch.setText("Tìm kiếm...");
                txtSearch.setForeground(SUBTEXT);
                renderContent();
            }
        });
        sidebarBtns.add(btn);
        sidebarPanel.add(btn);
        sidebarPanel.add(Box.createVerticalStrut(8));
    }

    // --- FIX BỐ CỤC HEADER ĐỂ TRÁNH BỊ CHE SEARCH BOX ---
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0)); // Dùng BorderLayout
        header.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);

        JLabel lblIcon = new JLabel(new MenuIcon("HELP"));
        lblIcon.setForeground(PRIMARY);

        JLabel lblTitle = new JLabel("Nội dung Hỗ trợ");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(TEXT); 

        titleRow.add(lblIcon);
        titleRow.add(lblTitle);

        JLabel lblSub = new JLabel("Khám phá tài liệu, FAQ và video hướng dẫn"); 
        lblSub.setFont(FONT_PLAIN_14);
        lblSub.setForeground(SUBTEXT);
        lblSub.setBorder(new EmptyBorder(0, 10, 0, 0));

        titleBox.add(titleRow);
        titleBox.add(Box.createVerticalStrut(6));
        titleBox.add(lblSub);

        header.add(titleBox, BorderLayout.CENTER); // Cho TitleBox chiếm phần còn lại

        JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); // Gói lại để không bị giãn dọc
        searchWrapper.setOpaque(false);
        searchWrapper.add(createSearchBox());

        header.add(searchWrapper, BorderLayout.EAST); // Cố định SearchBox ở góc phải

        return header;
    }

    private JComponent createSearchBox() {
        JPanel searchContainer = new JPanel(new BorderLayout(10, 0));
        searchContainer.setBackground(Color.WHITE);
        searchContainer.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(BORDER, 1, 24),
                new EmptyBorder(0, 16, 0, 16)
        ));
        searchContainer.setPreferredSize(new Dimension(280, 46));

        JLabel lblIcon = new JLabel(new MenuIcon("SEARCH"));
        lblIcon.setForeground(SUBTEXT);

        txtSearch = new JTextField();
        txtSearch.setFont(FONT_PLAIN_14);
        txtSearch.setForeground(SUBTEXT);
        txtSearch.setBackground(Color.WHITE);
        txtSearch.setBorder(null);
        txtSearch.setText("Tìm kiếm...");

        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().equals("Tìm kiếm...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(TEXT);
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setForeground(SUBTEXT);
                    txtSearch.setText("Tìm kiếm...");
                }
            }
        });

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { triggerSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { triggerSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { triggerSearch(); }
        });

        searchContainer.add(lblIcon, BorderLayout.WEST);
        searchContainer.add(txtSearch, BorderLayout.CENTER);
        return searchContainer;
    }

    private void triggerSearch() {
        if (!txtSearch.getText().equals("Tìm kiếm...")) {
            renderContent();
        }
    }

    // --- SỬ DỤNG CLASS SCROLLABLE ĐỂ ÉP CONTENT KHÔNG VƯỢT QUÁ KHUNG ---
    private JComponent createBody() {
        contentPanel = new ScrollablePanel(); // Fix lỗi tràn viền (bị che)
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_CARD);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        styleScrollBar(scrollPane);

        return scrollPane;
    }

    private void renderContent() {
        contentPanel.removeAll();
        String keyword = txtSearch == null || txtSearch.getText().equals("Tìm kiếm...")
                ? "" : txtSearch.getText().trim().toLowerCase();
        int count = 0;

        if (activeTab == 0 || activeTab == 1 || activeTab == 3) {
            boolean hasTitle = false;
            for (TaiLieuItem item : dsTaiLieu) {
                if (activeTab == 3 && !item.isVideo()) continue;

                if (matchKeyword(keyword, item.title, item.description, item.tag)) {
                    if (!hasTitle) {
                        if (activeTab == 3) {
                            contentPanel.add(createSectionTitle("VIDEO", "VIDEO HƯỚNG DẪN"));
                        } else {
                            contentPanel.add(createSectionTitle("DOCUMENT", "TÀI LIỆU HỆ THỐNG"));
                        }
                        contentPanel.add(Box.createVerticalStrut(16));
                        hasTitle = true;
                    }
                    contentPanel.add(createDocumentCard(item));
                    contentPanel.add(Box.createVerticalStrut(16));
                    count++;
                }
            }
        }

        if (activeTab == 0 || activeTab == 2) {
            boolean hasTitle = false;
            for (FaqItem faq : dsFaq) {
                if (matchKeyword(keyword, faq.question, faq.answer)) {
                    if (!hasTitle) {
                        contentPanel.add(Box.createVerticalStrut(12));
                        contentPanel.add(createSectionTitle("HELP", "CÂU HỎI THƯỜNG GẶP (FAQ)"));
                        contentPanel.add(Box.createVerticalStrut(16));
                        hasTitle = true;
                    }
                    contentPanel.add(createFaqCard(faq));
                    contentPanel.add(Box.createVerticalStrut(12));
                    count++;
                }
            }
        }

        if (count == 0) {
            contentPanel.add(createEmptyState());
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createSectionTitle(String iconType, String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);

        JLabel icon = new JLabel(new MenuIcon(iconType));
        icon.setForeground(PRIMARY);

        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(SUBTEXT);

        row.add(icon);
        row.add(lbl);
        return row;
    }

    private JPanel createEmptyState() {
        RoundedPanel panel = new RoundedPanel(16, BG_APP, false);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(32, 24, 32, 24));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        row.setOpaque(false);

        JLabel icon = new JLabel(new MenuIcon("SEARCH"));
        icon.setForeground(SUBTEXT);

        JLabel lbl = new JLabel("Không tìm thấy kết quả nào. Vui lòng thử từ khóa khác.");
        lbl.setFont(FONT_PLAIN_14);
        lbl.setForeground(SUBTEXT);

        row.add(icon);
        row.add(lbl);
        panel.add(row, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDocumentCard(TaiLieuItem item) {
        HoverRoundedPanel card = new HoverRoundedPanel(16, BG_CARD, new Color(248, 250, 252));
        card.setLayout(new BorderLayout(20, 0));
        card.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(BORDER, 1, 16),
                new EmptyBorder(16, 20, 16, 20)
        ));
        
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel iconBadge = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(item.featured ? PRIMARY : PRIMARY_SOFT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        iconBadge.setOpaque(false);
        iconBadge.setPreferredSize(new Dimension(60, 60));
        
        JLabel lblIcon = new JLabel(new MenuIcon(item.icon), SwingConstants.CENTER);
        lblIcon.setForeground(item.featured ? Color.WHITE : PRIMARY);
        iconBadge.add(lblIcon, BorderLayout.CENTER);

        JPanel infoBox = new JPanel();
        infoBox.setOpaque(false);
        infoBox.setLayout(new BoxLayout(infoBox, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(item.title);
        lblTitle.setFont(FONT_BOLD_14);
        lblTitle.setForeground(TEXT);

        JLabel lblDesc = new JLabel(item.description);
        lblDesc.setFont(FONT_PLAIN_14);
        lblDesc.setForeground(SUBTEXT);

        infoBox.add(Box.createVerticalStrut(4));
        infoBox.add(lblTitle);
        infoBox.add(Box.createVerticalStrut(6));
        infoBox.add(lblDesc);

        JPanel rightBox = new JPanel();
        rightBox.setOpaque(false);
        rightBox.setLayout(new BoxLayout(rightBox, BoxLayout.Y_AXIS));

        JLabel lblMeta1 = createMetaLabel("TIME", item.duration, SUBTEXT);
        lblMeta1.setAlignmentX(Component.RIGHT_ALIGNMENT); // Ép sát vào mép phải

        JLabel lblMeta2 = createMetaLabel(item.isVideo() ? "VIDEO" : "DOCUMENT", item.tag, SUBTEXT);
        lblMeta2.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rightBox.add(Box.createVerticalGlue());
        rightBox.add(lblMeta1);
        rightBox.add(Box.createVerticalStrut(8));
        rightBox.add(lblMeta2);

        if (item.isVideo()) {
            JLabel lblLink = createMetaLabel("EXPORT", "Mở video", PRIMARY);
            lblLink.setAlignmentX(Component.RIGHT_ALIGNMENT);
            rightBox.add(Box.createVerticalStrut(8));
            rightBox.add(lblLink);
        }

        rightBox.add(Box.createVerticalGlue());

        card.add(iconBadge, BorderLayout.WEST);
        card.add(infoBox, BorderLayout.CENTER);
        card.add(rightBox, BorderLayout.EAST);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (item.isVideo() && item.videoUrl != null && !item.videoUrl.isBlank()) {
                    moLinkVideo(item.videoUrl);
                } else {
                    JOptionPane.showMessageDialog(ManHinhHuongDan.this, "Đang mở tài liệu: " + item.title);
                }
            }
        });

        return card;
    }

    private JLabel createMetaLabel(String iconType, String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setIcon(new MenuIcon(iconType));
        lbl.setIconTextGap(8);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(color);
        return lbl;
    }

    private JPanel createFaqCard(FaqItem faq) {
        RoundedPanel card = new RoundedPanel(16, faq.expanded ? BG_FAQ_CONTENT : Color.WHITE, false);
        card.setLayout(new BorderLayout());
        
        // Bỏ cứng chiều cao để thả phanh cho câu trả lời dài
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, faq.expanded ? Integer.MAX_VALUE : 64));
        card.setBorder(new RoundedLineBorder(faq.expanded ? PRIMARY : BORDER, 1, 16)); 

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        header.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel icQ = new JLabel(new MenuIcon("HELP"));
        icQ.setForeground(faq.expanded ? PRIMARY : SUBTEXT);

        JLabel lblQuestion = new JLabel(faq.question);
        lblQuestion.setFont(FONT_BOLD_14);
        lblQuestion.setForeground(faq.expanded ? PRIMARY : TEXT);

        left.add(icQ);
        left.add(lblQuestion);

        JLabel lblArrow = new JLabel(new MenuIcon(faq.expanded ? "CLOSE" : "ADD")); 
        lblArrow.setForeground(faq.expanded ? PRIMARY : SUBTEXT);

        JTextArea txtAnswer = new JTextArea(faq.answer);
        txtAnswer.setLineWrap(true);
        txtAnswer.setWrapStyleWord(true);
        txtAnswer.setEditable(false);
        txtAnswer.setVisible(faq.expanded);
        txtAnswer.setBackground(BG_FAQ_CONTENT);
        txtAnswer.setForeground(new Color(51, 65, 85));
        txtAnswer.setFont(FONT_PLAIN_14);
        txtAnswer.setBorder(new EmptyBorder(0, 52, 20, 24)); 

        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                faq.expanded = !faq.expanded;
                renderContent();
            }
        });

        header.add(left, BorderLayout.CENTER);
        header.add(lblArrow, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);
        card.add(txtAnswer, BorderLayout.CENTER);

        return card;
    }

    private void moLinkVideo(String url) {
        try {
            if (!Desktop.isDesktopSupported()) {
                JOptionPane.showMessageDialog(this, "Máy tính của bạn không hỗ trợ mở trình duyệt web.");
                return;
            }
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(new URI(url));
            } else {
                JOptionPane.showMessageDialog(this, "Không thể mở link video do thiếu quyền hệ thống.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi cố gắng mở link video!");
        }
    }

    private void initData() {
        dsTaiLieu.clear();
        dsFaq.clear();

        dsTaiLieu.add(new TaiLieuItem(
                "DOCUMENT", "Hướng dẫn sử dụng cơ bản", "Làm quen giao diện, thanh công cụ và các chức năng chính của hệ thống MYCARE.", "10 phút", "Cơ bản", false, null
        ));
        dsTaiLieu.add(new TaiLieuItem(
                "BOX", "Video: Quản lý kho dược & Cảnh báo", "Hướng dẫn chi tiết cách nhập kho, xuất kho và xem cảnh báo thuốc sắp hết hạn.", "15 phút", "Video", true, "https://www.youtube.com/watch?v=J---aiyznGQ" 
        ));
        dsTaiLieu.add(new TaiLieuItem(
                "PILL", "Quy trình tạo & quản lý Lô hàng", "Cách nhập lô thuốc mới, theo dõi số lượng tồn kho và vòng đời lô hàng.", "12 phút", "Nghiệp vụ", false, null
        ));
        dsTaiLieu.add(new TaiLieuItem(
                "CART", "Quy trình Bán lẻ & Kê đơn", "Các bước tạo hóa đơn nhanh, áp dụng mã khuyến mãi và in biên lai thanh toán.", "8 phút", "Quy trình", false, null
        ));
        dsTaiLieu.add(new TaiLieuItem(
                "CHART", "Video: Xem thống kê & Báo cáo doanh thu", "Hướng dẫn đọc biểu đồ, lọc dữ liệu theo thời gian và xuất báo cáo ra file Excel.", "20 phút", "Video", true, "https://www.youtube.com/watch?v=dQw4w9WgXcQ" 
        ));
        dsTaiLieu.add(new TaiLieuItem(
                "USERS", "Quản lý Nhân sự & Phân quyền", "Thêm tài khoản nhân viên mới, cấp quyền truy cập và kiểm tra lịch sử đăng nhập.", "10 phút", "Hệ thống", false, null
        ));
        dsTaiLieu.add(new TaiLieuItem(
                "SETTING", "Thiết lập Danh mục & Nhà cung cấp", "Cách thêm mới thông tin thuốc, tạo đơn vị tính và lưu trữ hồ sơ Nhà cung cấp.", "15 phút", "Hệ thống", false, null
        ));

        dsFaq.add(new FaqItem("Làm thế nào để thêm một loại thuốc mới vào hệ thống?", "Vào menu Sản phẩm > Chọn Thêm mới. Nhập đầy đủ thông tin bắt buộc (Mã thuốc, Tên thuốc, Giá bán, Lô SX, Hạn sử dụng) rồi bấm Lưu lại.", false));
        dsFaq.add(new FaqItem("Phần mềm có tự động cảnh báo thuốc hết hạn không?", "Có. Hệ thống tự động kiểm tra mỗi ngày và sẽ hiển thị danh sách các loại thuốc còn dưới 30 ngày sử dụng tại màn hình Trang chủ (Dashboard) cũng như nháy đỏ ở phần Lô Hàng.", false));
        dsFaq.add(new FaqItem("Cách xuất báo cáo doanh thu ra file Excel?", "Mở màn hình Thống kê > Chọn khoảng thời gian cần xuất ở góc trên màn hình > Nhấn nút 'Xuất Excel' hoặc bấm vào Icon Tải xuống.", false));
        dsFaq.add(new FaqItem("Tôi quên mật khẩu tài khoản nhân viên, phải làm sao?", "Bạn có thể nhấn vào 'Quên mật khẩu' ở màn hình Đăng nhập để nhận mã OTP qua Email, hoặc nhờ Quản lý (Admin) vào mục Tài khoản để đặt lại mật khẩu giúp bạn.", false));
    }

    private boolean matchKeyword(String keyword, String... values) {
        if (keyword == null || keyword.isEmpty()) return true;
        for (String value : values) {
            if (value != null && value.toLowerCase().contains(keyword)) return true;
        }
        return false;
    }

    private void styleScrollBar(JScrollPane pane) {
        pane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        pane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(203, 213, 225);
                trackColor = BG_CARD;
            }

            @Override protected JButton createDecreaseButton(int o) { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
            @Override protected JButton createIncreaseButton(int o) { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 6, 6);
                g2.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                g.setColor(trackColor);
                g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            }
        });
    }

    // --- CLASS QUAN TRỌNG ĐỂ ÉP CONTENT PANEL VỪA KHÍT VỚI KÍCH THƯỚC MÀN HÌNH ---
    private class ScrollablePanel extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return super.getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 20; }
        @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return 60; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; } // BẮT BUỘC TRUE ĐỂ KHÔNG BỊ TRÀN VIỀN
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    class SidebarBtn extends JPanel {
        int index;
        String text;
        String icon;

        SidebarBtn(String icon, String text, int index) {
            this.icon = icon;
            this.text = text;
            this.index = index;
            setOpaque(false);
            setPreferredSize(new Dimension(Integer.MAX_VALUE, 48));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (activeTab == index) {
                g2.setColor(PRIMARY_SOFT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(PRIMARY);
                g2.fillRoundRect(0, 12, 4, getHeight() - 24, 4, 4); 
            }

            JLabel iconLabel = new JLabel(new MenuIcon(icon));
            iconLabel.setForeground(activeTab == index ? PRIMARY : SUBTEXT);
            iconLabel.setSize(22, 22);

            Graphics2D gIcon = (Graphics2D) g2.create(16, 13, 22, 22);
            iconLabel.paint(gIcon);
            gIcon.dispose();

            g2.setColor(activeTab == index ? PRIMARY : TEXT);
            g2.setFont(activeTab == index ? FONT_BOLD_14 : FONT_PLAIN_14);
            g2.drawString(text, 50, 29);

            g2.dispose();
        }
    }

    private static class TaiLieuItem {
        String icon, title, description, duration, tag, videoUrl;
        boolean featured;
        TaiLieuItem(String i, String t, String d, String du, String ta, boolean f, String url) {
            icon = i; title = t; description = d; duration = du; tag = ta; featured = f; videoUrl = url;
        }
        boolean isVideo() { return "Video".equalsIgnoreCase(tag); }
    }

    private static class FaqItem {
        String question, answer;
        boolean expanded;
        FaqItem(String q, String a, boolean e) { question = q; answer = a; expanded = e; }
    }

    static class RoundedPanel extends JPanel {
        protected int radius;
        protected Color bgColor;
        protected boolean drawShadow;

        RoundedPanel(int r, Color c, boolean s) { radius = r; bgColor = c; drawShadow = s; setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (drawShadow) {
                for (int i = 0; i < 4; i++) {
                    g2.setColor(new Color(0, 0, 0, 3 - i));
                    g2.fillRoundRect(i, i + 2, getWidth() - i * 2, getHeight() - i * 2, radius, radius);
                }
            }
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class HoverRoundedPanel extends RoundedPanel {
        Color hoverColor, normalColor;
        boolean hovered = false;

        HoverRoundedPanel(int r, Color normal, Color hover) {
            super(r, normal, false);
            this.normalColor = normal;
            this.hoverColor = hover;
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) { bgColor = hovered ? hoverColor : normalColor; super.paintComponent(g); }
    }

    static class RoundedLineBorder extends AbstractBorder {
        Color color; int thickness, radius;
        RoundedLineBorder(Color c, int t, int r) { color = c; thickness = t; radius = r; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            for (int i = 0; i < thickness; i++) {
                g2.drawRoundRect(x + i, y + i, width - 1 - (i * 2), height - 1 - (i * 2), radius, radius);
            }
            g2.dispose();
        }
        
        @Override
        public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
    }

    static class CompoundRoundBorder extends AbstractBorder {
        AbstractBorder outer; EmptyBorder inner;
        CompoundRoundBorder(AbstractBorder o, EmptyBorder i) { outer = o; inner = i; }

        @Override
        public Insets getBorderInsets(Component c) {
            Insets o = outer.getBorderInsets(c); Insets i = inner.getBorderInsets(c);
            return new Insets(o.top + i.top, o.left + i.left, o.bottom + i.bottom, o.right + i.right);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) { outer.paintBorder(c, g, x, y, w, h); }
    }
}