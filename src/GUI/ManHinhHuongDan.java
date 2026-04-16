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
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
    private static final Color BG_FAQ_CONTENT = new Color(250, 252, 255);
    private static final Color PRIMARY = new Color(30, 75, 138);
    private static final Color PRIMARY_SOFT = new Color(239, 246, 255);
    private static final Color TEXT = new Color(20, 28, 45);
    private static final Color SUBTEXT = new Color(100, 116, 139);
    private static final Color BORDER = new Color(223, 228, 235);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color SUCCESS_SOFT = new Color(240, 253, 244);
    private static final Color DANGER = new Color(239, 68, 68);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_BOLD_14 = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_PLAIN_14 = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_PLAIN_13 = new Font("Segoe UI", Font.PLAIN, 13);

    public ManHinhHuongDan() {
        initData();
        initUI();
        renderContent();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 0));
        setBackground(BG_APP);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(createSidebar(), BorderLayout.WEST);

        RoundedPanel rightMain = new RoundedPanel(20, BG_CARD, true);
        rightMain.setLayout(new BorderLayout(0, 16));
        rightMain.setBorder(new EmptyBorder(20, 24, 20, 24));
        rightMain.add(createHeader(), BorderLayout.NORTH);
        rightMain.add(createBody(), BorderLayout.CENTER);

        add(rightMain, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        RoundedPanel sidebar = new RoundedPanel(20, BG_CARD, true);
        sidebar.setLayout(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(new EmptyBorder(20, 16, 20, 16));

        JPanel topBox = new JPanel();
        topBox.setLayout(new BoxLayout(topBox, BoxLayout.Y_AXIS));
        topBox.setOpaque(false);

        JLabel lblTitle = new JLabel("DANH MỤC HỖ TRỢ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(SUBTEXT);
        lblTitle.setBorder(new EmptyBorder(0, 8, 16, 0));
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

        JPanel contactBox = new JPanel(new BorderLayout());
        contactBox.setOpaque(false);
        contactBox.setBorder(new EmptyBorder(16, 8, 0, 8));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        row1.setOpaque(false);
        JLabel icPhone = new JLabel(new MenuIcon("PHONE"));
        icPhone.setForeground(PRIMARY);
        JLabel txt1 = new JLabel("Cần thêm trợ giúp?");
        txt1.setFont(FONT_PLAIN_13);
        txt1.setForeground(SUBTEXT);
        row1.add(icPhone);
        row1.add(txt1);

        JLabel lblPhone = new JLabel("1900 9999", SwingConstants.CENTER);
        lblPhone.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPhone.setForeground(PRIMARY);
        lblPhone.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(row1);
        inner.add(Box.createVerticalStrut(6));
        inner.add(lblPhone);

        contactBox.add(inner, BorderLayout.CENTER);
        sidebar.add(contactBox, BorderLayout.SOUTH);

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

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        JLabel lblIcon = new JLabel(new MenuIcon("HELP"));
        lblIcon.setForeground(PRIMARY);

        JLabel lblTitle = new JLabel("Nội dung Hỗ trợ");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(PRIMARY);

        titleRow.add(lblIcon);
        titleRow.add(lblTitle);

        JLabel lblSub = new JLabel("Khám phá tài liệu, FAQ và video hướng dẫn");
        lblSub.setFont(FONT_PLAIN_13);
        lblSub.setForeground(SUBTEXT);

        titleBox.add(titleRow);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(lblSub);

        header.add(titleBox, BorderLayout.WEST);
        header.add(createSearchBox(), BorderLayout.EAST);

        return header;
    }

    private JComponent createSearchBox() {
        RoundedPanel searchContainer = new RoundedPanel(20, BG_APP, false);
        searchContainer.setLayout(new BorderLayout(10, 0));
        searchContainer.setBorder(new EmptyBorder(8, 16, 8, 16));
        searchContainer.setPreferredSize(new Dimension(300, 44));

        JLabel lblIcon = new JLabel(new MenuIcon("SEARCH"));
        lblIcon.setForeground(SUBTEXT);

        txtSearch = new JTextField();
        txtSearch.setFont(FONT_PLAIN_14);
        txtSearch.setForeground(SUBTEXT);
        txtSearch.setBackground(BG_APP);
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

    private JComponent createBody() {
        contentPanel = new JPanel();
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
                        contentPanel.add(Box.createVerticalStrut(12));
                        hasTitle = true;
                    }
                    contentPanel.add(createDocumentCard(item));
                    contentPanel.add(Box.createVerticalStrut(12));
                    count++;
                }
            }
        }

        if (activeTab == 0 || activeTab == 2) {
            boolean hasTitle = false;
            for (FaqItem faq : dsFaq) {
                if (matchKeyword(keyword, faq.question, faq.answer)) {
                    if (!hasTitle) {
                        contentPanel.add(Box.createVerticalStrut(8));
                        contentPanel.add(createSectionTitle("HELP", "CÂU HỎI THƯỜNG GẶP (FAQ)"));
                        contentPanel.add(Box.createVerticalStrut(12));
                        hasTitle = true;
                    }
                    contentPanel.add(createFaqCard(faq));
                    contentPanel.add(Box.createVerticalStrut(10));
                    count++;
                }
            }
        }

        if (count == 0) {
            contentPanel.add(createEmptyState());
        }

        contentPanel.add(Box.createVerticalGlue());
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createSectionTitle(String iconType, String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);

        JLabel icon = new JLabel(new MenuIcon(iconType));
        icon.setForeground(PRIMARY);

        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(TEXT);

        row.add(icon);
        row.add(lbl);
        return row;
    }

    private JPanel createEmptyState() {
        RoundedPanel panel = new RoundedPanel(16, BG_APP, false);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
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
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(BORDER, 1, 16),
                new EmptyBorder(14, 16, 14, 16)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // BẮT ĐẦU ĐOẠN CODE ĐÃ ĐƯỢC FIX LỖI HIỂN THỊ ICON
        JLabel lblIcon = new JLabel(new MenuIcon(item.icon), SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Vẽ nền bo góc trước
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();

                // Gọi super để vẽ cái Icon đè lên trên cái nền
                super.paintComponent(g);
            }
        };

        lblIcon.setOpaque(false); // Bắt buộc false để không bị vẽ nền vuông mặc định
        lblIcon.setBackground(item.featured ? PRIMARY : BG_APP);
        lblIcon.setForeground(item.featured ? Color.WHITE : PRIMARY);
        lblIcon.setPreferredSize(new Dimension(56, 56)); // Chỉnh to ra một chút cho cân đối
        // KẾT THÚC ĐOẠN FIX

        JPanel infoBox = new JPanel();
        infoBox.setOpaque(false);
        infoBox.setLayout(new BoxLayout(infoBox, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(item.title);
        lblTitle.setFont(FONT_BOLD_14);
        lblTitle.setForeground(TEXT);

        JLabel lblDesc = new JLabel(item.description);
        lblDesc.setFont(FONT_PLAIN_13);
        lblDesc.setForeground(SUBTEXT);

        infoBox.add(lblTitle);
        infoBox.add(Box.createVerticalStrut(4));
        infoBox.add(lblDesc);

        JPanel rightBox = new JPanel();
        rightBox.setOpaque(false);
        rightBox.setLayout(new BoxLayout(rightBox, BoxLayout.Y_AXIS));

        JLabel lblMeta1 = createMetaLabel("TIME", item.duration);
        JLabel lblMeta2 = createMetaLabel(item.isVideo() ? "VIDEO" : "DOCUMENT", item.tag);

        rightBox.add(Box.createVerticalGlue());
        rightBox.add(lblMeta1);
        rightBox.add(Box.createVerticalStrut(6));
        rightBox.add(lblMeta2);

        if (item.isVideo()) {
            JLabel lblLink = createMetaLabel("EXPORT", "Mở video");
            lblLink.setForeground(SUCCESS);
            rightBox.add(Box.createVerticalStrut(6));
            rightBox.add(lblLink);
        }

        rightBox.add(Box.createVerticalGlue());

        card.add(lblIcon, BorderLayout.WEST);
        card.add(infoBox, BorderLayout.CENTER);
        card.add(rightBox, BorderLayout.EAST);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (item.isVideo() && item.videoUrl != null && !item.videoUrl.isBlank()) {
                    moLinkVideo(item.videoUrl);
                } else {
                    JOptionPane.showMessageDialog(
                            ManHinhHuongDan.this,
                            "Đang mở tài liệu: " + item.title
                    );
                }
            }
        });

        return card;
    }

    private JLabel createMetaLabel(String iconType, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setIcon(new MenuIcon(iconType));
        lbl.setIconTextGap(6);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(PRIMARY);
        return lbl;
    }

    private JPanel createFaqCard(FaqItem faq) {
        RoundedPanel card = new RoundedPanel(16, BG_FAQ_CONTENT, false);
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, faq.expanded ? 170 : 56));
        card.setBorder(new RoundedLineBorder(BORDER, 1, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 16, 16, 16));
        header.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel icQ = new JLabel(new MenuIcon("HELP"));
        icQ.setForeground(PRIMARY);

        JLabel lblQuestion = new JLabel(faq.question);
        lblQuestion.setFont(FONT_BOLD_14);
        lblQuestion.setForeground(TEXT);

        left.add(icQ);
        left.add(lblQuestion);

        JLabel lblArrow = new JLabel(new MenuIcon(faq.expanded ? "CLOSE" : "EXPORT"));
        lblArrow.setForeground(SUBTEXT);

        JTextArea txtAnswer = new JTextArea(faq.answer);
        txtAnswer.setLineWrap(true);
        txtAnswer.setWrapStyleWord(true);
        txtAnswer.setEditable(false);
        txtAnswer.setVisible(faq.expanded);
        txtAnswer.setBackground(BG_FAQ_CONTENT);
        txtAnswer.setForeground(new Color(71, 85, 105));
        txtAnswer.setFont(FONT_PLAIN_14);
        txtAnswer.setBorder(new EmptyBorder(0, 36, 16, 20));

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

        // 1. TÀI LIỆU CƠ BẢN
        dsTaiLieu.add(new TaiLieuItem(
                "DOCUMENT",
                "Hướng dẫn sử dụng cơ bản",
                "Làm quen giao diện, thanh công cụ và các chức năng chính của hệ thống MYCARE.",
                "10 phút",
                "Cơ bản",
                false,
                null
        ));

        // 2. VIDEO QUẢN LÝ KHO (Thêm Icon BOX + Link YouTube)
        dsTaiLieu.add(new TaiLieuItem(
                "BOX", 
                "Video: Quản lý kho dược & Cảnh báo",
                "Hướng dẫn chi tiết cách nhập kho, xuất kho và xem cảnh báo thuốc sắp hết hạn.",
                "15 phút",
                "Video",
                true,
                "https://www.youtube.com/watch?v=J---aiyznGQ" // Link mẫu, bạn có thể thay đổi
        ));

        // 3. TÀI LIỆU LÔ HÀNG (Icon PILL)
        dsTaiLieu.add(new TaiLieuItem(
                "PILL",
                "Quy trình tạo & quản lý Lô hàng",
                "Cách nhập lô thuốc mới, theo dõi số lượng tồn kho và vòng đời lô hàng.",
                "12 phút",
                "Nghiệp vụ",
                false,
                null
        ));

        // 4. TÀI LIỆU BÁN HÀNG (Icon CART)
        dsTaiLieu.add(new TaiLieuItem(
                "CART",
                "Quy trình Bán lẻ & Kê đơn",
                "Các bước tạo hóa đơn nhanh, áp dụng mã khuyến mãi và in biên lai thanh toán.",
                "8 phút",
                "Quy trình",
                false,
                null
        ));

        // 5. VIDEO BÁO CÁO THỐNG KÊ (Icon CHART + Link YouTube)
        dsTaiLieu.add(new TaiLieuItem(
                "CHART",
                "Video: Xem thống kê & Báo cáo doanh thu",
                "Hướng dẫn đọc biểu đồ, lọc dữ liệu theo thời gian và xuất báo cáo ra file Excel.",
                "20 phút",
                "Video",
                true,
                "https://www.youtube.com/watch?v=dQw4w9WgXcQ" // Link mẫu
        ));

        // 6. TÀI LIỆU QUẢN LÝ NHÂN VIÊN (Icon USERS)
        dsTaiLieu.add(new TaiLieuItem(
                "USERS",
                "Quản lý Nhân sự & Phân quyền",
                "Thêm tài khoản nhân viên mới, cấp quyền truy cập và kiểm tra lịch sử đăng nhập.",
                "10 phút",
                "Hệ thống",
                false,
                null
        ));

        // 7. TÀI LIỆU CẤU HÌNH (Icon SETTING)
        dsTaiLieu.add(new TaiLieuItem(
                "SETTING",
                "Thiết lập Danh mục & Nhà cung cấp",
                "Cách thêm mới thông tin thuốc, tạo đơn vị tính và lưu trữ hồ sơ Nhà cung cấp.",
                "15 phút",
                "Hệ thống",
                false,
                null
        ));

        // --- CÂU HỎI THƯỜNG GẶP (FAQ) ---
        dsFaq.add(new FaqItem(
                "Làm thế nào để thêm một loại thuốc mới vào hệ thống?",
                "Vào menu Sản phẩm > Chọn Thêm mới. Nhập đầy đủ thông tin bắt buộc (Mã thuốc, Tên thuốc, Giá bán, Lô SX, Hạn sử dụng) rồi bấm Lưu lại.",
                false
        ));
        dsFaq.add(new FaqItem(
                "Phần mềm có tự động cảnh báo thuốc hết hạn không?",
                "Có. Hệ thống tự động kiểm tra mỗi ngày và sẽ hiển thị danh sách các loại thuốc còn dưới 30 ngày sử dụng tại màn hình Trang chủ (Dashboard) cũng như nháy đỏ ở phần Lô Hàng.",
                false
        ));
        dsFaq.add(new FaqItem(
                "Cách xuất báo cáo doanh thu ra file Excel?",
                "Mở màn hình Thống kê > Chọn khoảng thời gian cần xuất ở góc trên màn hình > Nhấn nút 'Xuất Excel' hoặc bấm vào Icon Tải xuống.",
                false
        ));
        dsFaq.add(new FaqItem(
                "Tôi quên mật khẩu tài khoản nhân viên, phải làm sao?",
                "Bạn có thể nhấn vào 'Quên mật khẩu' ở màn hình Đăng nhập để nhận mã OTP qua Email, hoặc nhờ Quản lý (Admin) vào mục Tài khoản để đặt lại mật khẩu giúp bạn.",
                false
        ));
    }

    private boolean matchKeyword(String keyword, String... values) {
        if (keyword == null || keyword.isEmpty()) return true;
        for (String value : values) {
            if (value != null && value.toLowerCase().contains(keyword)) return true;
        }
        return false;
    }

    private void styleScrollBar(JScrollPane pane) {
        pane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = BORDER;
                trackColor = BG_CARD;
            }

            @Override
            protected JButton createDecreaseButton(int o) {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }

            @Override
            protected JButton createIncreaseButton(int o) {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(
                        thumbBounds.x + 2,
                        thumbBounds.y + 2,
                        thumbBounds.width - 4,
                        thumbBounds.height - 4,
                        8, 8
                );
                g2.dispose();
            }
        });
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
            setPreferredSize(new Dimension(Integer.MAX_VALUE, 46));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
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
                g2.fillRoundRect(0, 10, 4, getHeight() - 20, 4, 4);
            }

            JLabel iconLabel = new JLabel(new MenuIcon(icon));
            iconLabel.setForeground(activeTab == index ? PRIMARY : TEXT);
            iconLabel.setSize(22, 22);

            Graphics2D gIcon = (Graphics2D) g2.create(12, 12, 22, 22);
            iconLabel.paint(gIcon);
            gIcon.dispose();

            g2.setColor(activeTab == index ? PRIMARY : TEXT);
            g2.setFont(activeTab == index ? FONT_BOLD_14 : FONT_PLAIN_14);
            g2.drawString(text, 44, 28);

            g2.dispose();
        }
    }

    private static class TaiLieuItem {
        String icon, title, description, duration, tag, videoUrl;
        boolean featured;

        TaiLieuItem(String i, String t, String d, String du, String ta, boolean f, String url) {
            icon = i;
            title = t;
            description = d;
            duration = du;
            tag = ta;
            featured = f;
            videoUrl = url;
        }

        boolean isVideo() {
            return "Video".equalsIgnoreCase(tag);
        }
    }

    private static class FaqItem {
        String question, answer;
        boolean expanded;

        FaqItem(String q, String a, boolean e) {
            question = q;
            answer = a;
            expanded = e;
        }
    }

    static class RoundedPanel extends JPanel {
        protected int radius;
        protected Color bgColor;
        protected boolean drawShadow;

        RoundedPanel(int r, Color c, boolean s) {
            radius = r;
            bgColor = c;
            drawShadow = s;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (drawShadow) {
                for (int i = 0; i < 4; i++) {
                    g2.setColor(new Color(0, 0, 0, 4 - i));
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
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            bgColor = hovered ? hoverColor : normalColor;
            super.paintComponent(g);
        }
    }

    static class RoundedLineBorder extends AbstractBorder {
        Color color;
        int thickness, radius;

        RoundedLineBorder(Color c, int t, int r) {
            color = c;
            thickness = t;
            radius = r;
        }

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
    }

    static class CompoundRoundBorder extends AbstractBorder {
        AbstractBorder outer;
        EmptyBorder inner;

        CompoundRoundBorder(AbstractBorder o, EmptyBorder i) {
            outer = o;
            inner = i;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return inner.getBorderInsets(c);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            outer.paintBorder(c, g, x, y, w, h);
        }
    }
}