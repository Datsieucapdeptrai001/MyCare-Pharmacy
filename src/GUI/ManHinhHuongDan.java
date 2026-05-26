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

    private int activeTab = 0; // 0: Tất cả, 1: Tài liệu, 2: FAQ

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

        JLabel lblSub = new JLabel("Khám phá tài liệu hướng dẫn và câu hỏi thường gặp");
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
                ? ""
                : txtSearch.getText().trim().toLowerCase();

        int count = 0;

        if (activeTab == 0 || activeTab == 1) {
            boolean hasTitle = false;

            for (TaiLieuItem item : dsTaiLieu) {
                if (matchKeyword(keyword, item.title, item.description, item.tag)) {
                    if (!hasTitle) {
                        contentPanel.add(createSectionTitle("DOCUMENT", "TÀI LIỆU HƯỚNG DẪN"));
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
        card.setLayout(new BorderLayout(18, 0));
        card.setBorder(new CompoundRoundBorder(
                new RoundedLineBorder(item.expanded ? PRIMARY : BORDER, 1, 16),
                new EmptyBorder(16, 20, 16, 20)
        ));

        card.setMaximumSize(new Dimension(
                Integer.MAX_VALUE,
                item.expanded ? Integer.MAX_VALUE : 120
        ));
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
        iconBadge.setPreferredSize(new Dimension(58, 58));

        JLabel lblIcon = new JLabel(new MenuIcon(item.icon), SwingConstants.CENTER);
        lblIcon.setForeground(item.featured ? Color.WHITE : PRIMARY);
        iconBadge.add(lblIcon, BorderLayout.CENTER);

        JPanel centerBox = new JPanel();
        centerBox.setOpaque(false);
        centerBox.setLayout(new BoxLayout(centerBox, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel(item.title);
        lblTitle.setFont(FONT_BOLD_14);
        lblTitle.setForeground(item.expanded ? PRIMARY : TEXT);

        JLabel lblArrow = new JLabel(new MenuIcon(item.expanded ? "CLOSE" : "ADD"));
        lblArrow.setForeground(item.expanded ? PRIMARY : SUBTEXT);

        titleRow.add(lblTitle, BorderLayout.CENTER);
        titleRow.add(lblArrow, BorderLayout.EAST);

        JLabel lblDesc = new JLabel("<html><div style='width:650px;'>"
                + escapeHtml(item.description)
                + "</div></html>");
        lblDesc.setFont(FONT_PLAIN_14);
        lblDesc.setForeground(SUBTEXT);

        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        metaRow.setOpaque(false);
        metaRow.add(createMetaLabel("TIME", item.duration, SUBTEXT));
        metaRow.add(createMetaLabel("DOCUMENT", item.tag, SUBTEXT));

        centerBox.add(titleRow);
        centerBox.add(Box.createVerticalStrut(6));
        centerBox.add(lblDesc);
        centerBox.add(Box.createVerticalStrut(10));
        centerBox.add(metaRow);

        if (item.expanded) {
            JTextArea txtContent = new JTextArea(item.content);
            txtContent.setEditable(false);
            txtContent.setFocusable(false);
            txtContent.setLineWrap(true);
            txtContent.setWrapStyleWord(true);
            txtContent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txtContent.setForeground(new Color(51, 65, 85));
            txtContent.setBackground(new Color(248, 250, 252));
            txtContent.setBorder(new EmptyBorder(14, 16, 14, 16));

            RoundedPanel contentWrapper = new RoundedPanel(14, new Color(248, 250, 252), false);
            contentWrapper.setLayout(new BorderLayout());
            contentWrapper.setBorder(new RoundedLineBorder(BORDER, 1, 14));
            contentWrapper.add(txtContent, BorderLayout.CENTER);
            contentWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

            centerBox.add(Box.createVerticalStrut(12));
            centerBox.add(contentWrapper);
        }

        card.add(iconBadge, BorderLayout.WEST);
        card.add(centerBox, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                item.expanded = !item.expanded;
                renderContent();
            }
        });

        return card;
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


    private void initData() {
        dsTaiLieu.clear();
        dsFaq.clear();

        dsTaiLieu.add(new TaiLieuItem(
                "DOCUMENT",
                "Hướng dẫn sử dụng cơ bản",
                "Làm quen giao diện, thanh công cụ và các chức năng chính của hệ thống MYCARE.",
                "10 phút",
                "Cơ bản",
                true,
                "1. Đăng nhập bằng tài khoản được cấp.\n"
                        + "2. Tại màn hình chính, chọn chức năng cần thao tác ở thanh menu bên trái.\n"
                        + "3. Các màn hình chính gồm: Bán hàng, Sản phẩm, Lô hàng, Nhập kho, Xuất kho, Kiểm kê, Thống kê.\n"
                        + "4. Sử dụng ô tìm kiếm để lọc nhanh dữ liệu theo mã, tên hoặc trạng thái.\n"
                        + "5. Khi hoàn tất thao tác, kiểm tra lại thông báo hệ thống để biết thao tác thành công hay thất bại."
        ));

        dsTaiLieu.add(new TaiLieuItem(
                "PILL",
                "Quy trình tạo & quản lý lô hàng",
                "Cách nhập lô thuốc mới, theo dõi số lượng tồn kho và vòng đời lô hàng.",
                "12 phút",
                "Nghiệp vụ",
                false,
                "1. Vào màn hình Lô hàng hoặc Nhập lô hàng.\n"
                        + "2. Chọn sản phẩm cần nhập lô.\n"
                        + "3. Nhập số lô, hạn sử dụng, số lượng, giá vốn và kho lưu trữ.\n"
                        + "4. Hệ thống tạo mã vạch nội bộ cho lô hàng.\n"
                        + "5. Lô hàng được theo dõi theo số lượng tồn, hạn dùng và trạng thái.\n"
                        + "6. Lô hết hàng hoặc bị ẩn sẽ không được hiển thị ở màn hình xuất kho.\n"
                        + "7. Khi bán/xuất kho, ưu tiên lô gần hết hạn trước theo nguyên tắc FEFO."
        ));

        dsTaiLieu.add(new TaiLieuItem(
                "CART",
                "Quy trình bán lẻ & kê đơn",
                "Các bước tạo hóa đơn nhanh, áp dụng mã khuyến mãi và in biên lai thanh toán.",
                "8 phút",
                "Quy trình",
                false,
                "1. Vào màn hình Bán hàng.\n"
                        + "2. Quét mã vạch hoặc tìm sản phẩm theo tên/mã.\n"
                        + "3. Chọn đúng đơn vị bán như hộp, vỉ hoặc viên.\n"
                        + "4. Nhập số lượng bán.\n"
                        + "5. Kiểm tra giỏ hàng, tổng tiền và thông tin khách hàng nếu có.\n"
                        + "6. Xác nhận thanh toán.\n"
                        + "7. Hệ thống trừ tồn kho và lưu hóa đơn bán hàng."
        ));

        dsTaiLieu.add(new TaiLieuItem(
                "BOX",
                "Quy trình xuất kho / xuất hủy",
                "Hướng dẫn xuất lô hàng khỏi kho do bán hàng, trả nhà cung cấp, hủy hàng hoặc tiêu hao nội bộ.",
                "10 phút",
                "Kho",
                false,
                "1. Vào màn hình Xuất kho / Xuất hủy.\n"
                        + "2. Quét QR hoặc nhập mã lô/mã vạch nội bộ.\n"
                        + "3. Kiểm tra đúng sản phẩm, kho và tồn hiện tại.\n"
                        + "4. Nhập số lượng xuất và chọn lý do xuất.\n"
                        + "5. Bấm Thêm vào phiếu.\n"
                        + "6. Kiểm tra danh sách chờ xuất.\n"
                        + "7. Bấm Xác nhận xuất kho.\n"
                        + "8. Hệ thống trừ tồn kho và cho xem trước phiếu xuất trước khi in."
        ));

        dsTaiLieu.add(new TaiLieuItem(
                "CHECK_CIRCLE",
                "Quy trình kiểm kê kho",
                "Đối chiếu tồn hệ thống với tồn thực tế ngoài kho/kệ.",
                "15 phút",
                "Kho",
                false,
                "1. Vào màn hình Kiểm kê kho.\n"
                        + "2. Chọn kho cần kiểm kê.\n"
                        + "3. Có thể nhập tồn thực tế trực tiếp trên bảng hoặc xuất file mẫu kiểm kê.\n"
                        + "4. Nếu dùng file mẫu, nhân viên đi kiểm thực tế rồi nhập tồn thực tế, tình trạng và lý do.\n"
                        + "5. Import lại file kiểm kê vào phần mềm.\n"
                        + "6. Hệ thống tự tính chênh lệch = tồn thực tế - tồn hệ thống.\n"
                        + "7. Nếu có chênh lệch, phải nhập lý do.\n"
                        + "8. Bấm Lưu phiếu kiểm kê để cập nhật tồn kho.\n"
                        + "9. Sau khi lưu, có thể xem trước phiếu kiểm kê rồi in."
        ));

        dsTaiLieu.add(new TaiLieuItem(
                "USERS",
                "Quản lý nhân sự & phân quyền",
                "Thêm tài khoản nhân viên mới, cấp quyền truy cập và kiểm tra lịch sử đăng nhập.",
                "10 phút",
                "Hệ thống",
                false,
                "1. Chỉ tài khoản Quản lý được chỉnh thông tin nhân sự và phân quyền.\n"
                        + "2. Vào màn hình Nhân viên hoặc Tài khoản.\n"
                        + "3. Thêm thông tin nhân viên: họ tên, số điện thoại, email và vai trò.\n"
                        + "4. Vai trò chính trong hệ thống gồm Quản lý và Dược sĩ.\n"
                        + "5. Quản lý có quyền xem báo cáo, quản lý danh mục, kiểm kê và phân quyền.\n"
                        + "6. Dược sĩ có thể bán hàng, nhập/xuất kho theo quyền được cấp."
        ));

        dsTaiLieu.add(new TaiLieuItem(
                "SETTING",
                "Thiết lập danh mục & nhà cung cấp",
                "Cách thêm mới thông tin thuốc, tạo đơn vị tính và lưu trữ hồ sơ nhà cung cấp.",
                "15 phút",
                "Hệ thống",
                false,
                "1. Vào màn hình Sản phẩm để thêm hoặc sửa thông tin thuốc.\n"
                        + "2. Khai báo đầy đủ tên thuốc, nhóm thuốc, giá bán, đơn vị tính và mã vạch nếu có.\n"
                        + "3. Thiết lập đơn vị quy đổi như hộp, vỉ, viên để bán/xuất đúng số lượng.\n"
                        + "4. Vào màn hình Nhà cung cấp để lưu thông tin đơn vị cung ứng.\n"
                        + "5. Khi nhập lô hàng, chọn đúng nhà cung cấp để phục vụ tra cứu lịch sử nhập."
        ));

        dsFaq.add(new FaqItem(
                "Làm thế nào để thêm một loại thuốc mới vào hệ thống?",
                "Vào menu Sản phẩm > Chọn Thêm mới. Nhập đầy đủ thông tin bắt buộc như mã thuốc, tên thuốc, đơn vị tính, giá bán rồi bấm Lưu.",
                false
        ));

        dsFaq.add(new FaqItem(
                "Phần mềm có tự động cảnh báo thuốc hết hạn không?",
                "Có. Hệ thống cảnh báo lô đã hết hạn, lô sắp hết hạn trong 30 ngày và lô cận hạn trong 31-180 ngày để ưu tiên xử lý.",
                false
        ));

        dsFaq.add(new FaqItem(
                "Kiểm kê kho có tạo lô mới không?",
                "Không. Kiểm kê chỉ đối chiếu tồn hệ thống với tồn thực tế của các lô đã có. Nếu phát hiện lô mới, phải nhập lô ở màn hình Nhập lô hàng trước.",
                false
        ));

        dsFaq.add(new FaqItem(
                "Tôi quên mật khẩu tài khoản nhân viên, phải làm sao?",
                "Có thể dùng chức năng Quên mật khẩu ở màn hình đăng nhập hoặc nhờ Quản lý đặt lại mật khẩu.",
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
        String icon;
        String title;
        String description;
        String duration;
        String tag;
        String content;
        boolean featured;
        boolean expanded;

        TaiLieuItem(String i, String t, String d, String du, String ta, boolean f, String c) {
            icon = i;
            title = t;
            description = d;
            duration = du;
            tag = ta;
            featured = f;
            content = c;
            expanded = false;
        }
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