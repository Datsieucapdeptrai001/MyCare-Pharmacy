package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ManHinhHuongDan extends JPanel {

    private JTextField txtSearch;
    private JPanel contentPanel;
    private JScrollPane scrollPane;

    private final List<TaiLieuItem> dsTaiLieu = new ArrayList<>();
    private final List<FaqItem> dsFaq = new ArrayList<>();

    // ================== THEME ==================
    private static final Color BG_APP = new Color(243, 246, 250);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BG_FAQ = new Color(248, 250, 252);
    private static final Color PRIMARY = new Color(37, 99, 235);
    private static final Color PRIMARY_SOFT = new Color(219, 234, 254);
    private static final Color TEXT = new Color(15, 23, 42);
    private static final Color SUBTEXT = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color SHADOW = new Color(15, 23, 42, 16);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 17);
    private static final Font FONT_BOLD_14 = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_BOLD_13 = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_PLAIN_14 = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_PLAIN_13 = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_PLAIN_12 = new Font("Segoe UI", Font.PLAIN, 12);

    public ManHinhHuongDan() {
        initData();
        initUI();
        renderContent();
    }

    // ================== INIT ==================
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(BG_APP);
        setBorder(new EmptyBorder(16, 18, 16, 18));

        RoundedPanel main = new RoundedPanel(24, BG_CARD, true);
        main.setLayout(new BorderLayout(0, 16));
        main.setBorder(new EmptyBorder(18, 18, 18, 18));

        main.add(createHeader(), BorderLayout.NORTH);
        main.add(createBody(), BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("HƯỚNG DẪN SỬ DỤNG");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(TEXT);

        JLabel lblSub = new JLabel("Tài liệu thao tác và câu hỏi thường gặp");
        lblSub.setFont(FONT_PLAIN_13);
        lblSub.setForeground(SUBTEXT);
        lblSub.setBorder(new EmptyBorder(4, 0, 0, 0));

        titleBox.add(lblTitle);
        titleBox.add(lblSub);

        JButton btnCollapse = createGhostButton("Thu gọn tất cả");
        btnCollapse.addActionListener(e -> {
            for (FaqItem faq : dsFaq) {
                faq.expanded = false;
            }
            renderContent();
        });

        top.add(titleBox, BorderLayout.WEST);
        top.add(btnCollapse, BorderLayout.EAST);

        header.add(top);
        header.add(Box.createVerticalStrut(12));
        header.add(createSearchBox());

        return header;
    }

    private JComponent createSearchBox() {
        txtSearch = new JTextField();
        txtSearch.setFont(FONT_PLAIN_14);
        txtSearch.setPreferredSize(new Dimension(420, 42));
        txtSearch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtSearch.setToolTipText("Tìm kiếm tài liệu hoặc câu hỏi...");
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                renderContent();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                renderContent();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                renderContent();
            }
        });

        return txtSearch;
    }

    private JComponent createBody() {
        contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(
                contentPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG_CARD);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        styleScrollBar(scrollPane);

        return scrollPane;
    }

    // ================== RENDER ==================
    private void renderContent() {
        contentPanel.removeAll();

        String keyword = txtSearch == null ? "" : txtSearch.getText().trim().toLowerCase();
        int count = 0;

        contentPanel.add(createSectionTitle("📘 TÀI LIỆU HƯỚNG DẪN"));
        contentPanel.add(Box.createVerticalStrut(8));

        for (TaiLieuItem item : dsTaiLieu) {
            if (matchKeyword(keyword, item.title, item.description, item.tag)) {
                contentPanel.add(createDocumentCard(item));
                contentPanel.add(Box.createVerticalStrut(10));
                count++;
            }
        }

        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(createSectionTitle("❓ CÂU HỎI THƯỜNG GẶP"));
        contentPanel.add(Box.createVerticalStrut(8));

        for (FaqItem faq : dsFaq) {
            if (matchKeyword(keyword, faq.question, faq.answer)) {
                contentPanel.add(createFaqCard(faq));
                contentPanel.add(Box.createVerticalStrut(8));
                count++;
            }
        }

        if (count == 0) {
            contentPanel.add(createEmptyState());
        }

        contentPanel.add(Box.createVerticalGlue());
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JLabel createSectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(TEXT);
        lbl.setBorder(new EmptyBorder(2, 0, 0, 0));
        return lbl;
    }

    private JPanel createEmptyState() {
        RoundedPanel panel = new RoundedPanel(16, BG_FAQ, false);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JLabel lbl = new JLabel("Không tìm thấy nội dung phù hợp");
        lbl.setFont(FONT_PLAIN_13);
        lbl.setForeground(SUBTEXT);

        panel.add(lbl, BorderLayout.WEST);
        return panel;
    }

    // ================== DOCUMENT CARD ==================
    private JPanel createDocumentCard(TaiLieuItem item) {
        HoverRoundedPanel card = new HoverRoundedPanel(18, BG_CARD, new Color(248, 250, 252));
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(12, 12, 12, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 94));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblIcon = new JLabel(item.icon, SwingConstants.CENTER);
        lblIcon.setOpaque(true);
        lblIcon.setBackground(item.featured ? PRIMARY : new Color(241, 245, 249));
        lblIcon.setForeground(item.featured ? Color.WHITE : PRIMARY);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        lblIcon.setPreferredSize(new Dimension(46, 46));

        JPanel infoBox = new JPanel();
        infoBox.setOpaque(false);
        infoBox.setLayout(new BoxLayout(infoBox, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(item.title);
        lblTitle.setFont(FONT_BOLD_14);
        lblTitle.setForeground(TEXT);

        JLabel lblDesc = new JLabel(item.description);
        lblDesc.setFont(FONT_PLAIN_12);
        lblDesc.setForeground(SUBTEXT);

        JLabel lblMeta = new JLabel(item.duration + "  •  " + item.tag);
        lblMeta.setFont(FONT_BOLD_13);
        lblMeta.setForeground(new Color(79, 70, 229));

        infoBox.add(lblTitle);
        infoBox.add(Box.createVerticalStrut(3));
        infoBox.add(lblDesc);
        infoBox.add(Box.createVerticalStrut(6));
        infoBox.add(lblMeta);

        JButton btnOpen = createPrimaryButton("Mở");
        btnOpen.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        this,
                        "Bạn vừa chọn: " + item.title,
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE
                )
        );

        JPanel rightBox = new JPanel(new BorderLayout());
        rightBox.setOpaque(false);
        rightBox.add(btnOpen, BorderLayout.NORTH);

        card.add(lblIcon, BorderLayout.WEST);
        card.add(infoBox, BorderLayout.CENTER);
        card.add(rightBox, BorderLayout.EAST);

        MouseAdapter clickOpen = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                btnOpen.doClick();
            }
        };
        card.addMouseListener(clickOpen);

        return card;
    }

    // ================== FAQ CARD ==================
    private JPanel createFaqCard(FaqItem faq) {
        RoundedPanel card = new RoundedPanel(16, BG_FAQ, false);
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, faq.expanded ? 155 : 54));

        JButton header = new JButton();
        header.setLayout(new BorderLayout());
        header.setFocusPainted(false);
        header.setBorder(new EmptyBorder(12, 14, 12, 14));
        header.setBackground(Color.WHITE);
        header.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblQuestion = new JLabel(faq.index + ". " + faq.question);
        lblQuestion.setFont(FONT_BOLD_14);
        lblQuestion.setForeground(TEXT);

        JLabel lblArrow = new JLabel(faq.expanded ? "▾" : "▸");
        lblArrow.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblArrow.setForeground(SUBTEXT);

        header.add(lblQuestion, BorderLayout.CENTER);
        header.add(lblArrow, BorderLayout.EAST);

        JTextArea txtAnswer = new JTextArea(faq.answer);
        txtAnswer.setLineWrap(true);
        txtAnswer.setWrapStyleWord(true);
        txtAnswer.setEditable(false);
        txtAnswer.setVisible(faq.expanded);
        txtAnswer.setBackground(BG_FAQ);
        txtAnswer.setForeground(new Color(71, 85, 105));
        txtAnswer.setFont(FONT_PLAIN_13);
        txtAnswer.setBorder(new EmptyBorder(0, 14, 12, 14));

        header.addActionListener(e -> {
            boolean newState = !faq.expanded;
            for (FaqItem item : dsFaq) {
                item.expanded = false;
            }
            faq.expanded = newState;
            renderContent();
        });

        card.add(header, BorderLayout.NORTH);
        card.add(txtAnswer, BorderLayout.CENTER);

        return card;
    }

    // ================== HELPERS ==================
    private boolean matchKeyword(String keyword, String... values) {
        if (keyword == null || keyword.isEmpty()) return true;
        for (String value : values) {
            if (value != null && value.toLowerCase().contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        return btn;
    }

    private JButton createGhostButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(TEXT);
        btn.setBackground(new Color(241, 245, 249));
        btn.setBorder(new EmptyBorder(9, 14, 9, 14));
        return btn;
    }

    private void styleScrollBar(JScrollPane pane) {
        pane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(203, 213, 225);
                trackColor = new Color(241, 245, 249);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
    }

    // ================== DATA ==================
    private void initData() {
        dsTaiLieu.clear();
        dsFaq.clear();

        dsTaiLieu.add(new TaiLieuItem("📘", "Hướng dẫn sử dụng cơ bản",
                "Làm quen giao diện và các chức năng chính của hệ thống", "10 phút", "Cơ bản", false));

        dsTaiLieu.add(new TaiLieuItem("🎥", "Video quản lý kho dược",
                "Hướng dẫn nhập kho, xuất kho và kiểm kê tồn", "15 phút", "Video", false));

        dsTaiLieu.add(new TaiLieuItem("📄", "Tài liệu kỹ thuật",
                "Cấu hình hệ thống, sao lưu và phục hồi dữ liệu", "20 phút", "Nâng cao", true));

        dsTaiLieu.add(new TaiLieuItem("📖", "Quy trình bán hàng",
                "Tạo đơn hàng, thanh toán và hoàn tất giao dịch", "8 phút", "Quy trình", false));

        dsTaiLieu.add(new TaiLieuItem("📊", "Báo cáo và thống kê",
                "Theo dõi doanh thu và xuất báo cáo Excel hoặc PDF", "12 phút", "Báo cáo", false));

        dsTaiLieu.add(new TaiLieuItem("🔐", "Chính sách bảo mật",
                "Phân quyền tài khoản và bảo vệ dữ liệu người dùng", "5 phút", "Bảo mật", false));

        dsFaq.add(new FaqItem(1, "Làm thế nào để thêm sản phẩm mới?",
                "Vào menu Sản phẩm, nhấn Thêm mới, nhập đầy đủ thông tin sản phẩm rồi bấm Lưu.", true));

        dsFaq.add(new FaqItem(2, "Cách xuất báo cáo Excel?",
                "Mở màn hình Thống kê, chọn khoảng thời gian cần xem rồi nhấn nút Xuất Excel.", false));

        dsFaq.add(new FaqItem(3, "Làm thế nào để cập nhật thông tin sản phẩm?",
                "Tại màn hình Sản phẩm, chọn dòng cần sửa, nhấn Cập nhật, chỉnh thông tin rồi bấm Lưu.", false));

        dsFaq.add(new FaqItem(4, "Cách tạo khuyến mại mới?",
                "Vào menu Khuyến mại, tạo chương trình mới, chọn điều kiện áp dụng rồi lưu lại.", false));

        dsFaq.add(new FaqItem(5, "Làm sao để xem thống kê doanh thu?",
                "Mở màn hình Thống kê, chọn ngày bắt đầu và ngày kết thúc để xem số liệu tổng hợp.", false));

        dsFaq.add(new FaqItem(6, "Cách phân quyền nhân viên?",
                "Vào menu Nhân viên hoặc Tài khoản, chọn vai trò tương ứng rồi cập nhật quyền truy cập.", false));
    }

    // ================== MODELS ==================
    private static class TaiLieuItem {
        String icon;
        String title;
        String description;
        String duration;
        String tag;
        boolean featured;

        TaiLieuItem(String icon, String title, String description, String duration, String tag, boolean featured) {
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.duration = duration;
            this.tag = tag;
            this.featured = featured;
        }
    }

    private static class FaqItem {
        int index;
        String question;
        String answer;
        boolean expanded;

        FaqItem(int index, String question, String answer, boolean expanded) {
            this.index = index;
            this.question = question;
            this.answer = answer;
            this.expanded = expanded;
        }
    }

    // ================== CUSTOM PANEL ==================
    static class RoundedPanel extends JPanel {
        protected int radius;
        protected Color bgColor;
        protected boolean drawShadow;

        RoundedPanel(int radius, Color bgColor, boolean drawShadow) {
            this.radius = radius;
            this.bgColor = bgColor;
            this.drawShadow = drawShadow;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (drawShadow) {
                g2.setColor(SHADOW);
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, radius, radius);
            }

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, radius, radius);

            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class HoverRoundedPanel extends RoundedPanel {
        private final Color normalColor;
        private final Color hoverColor;
        private boolean hovered = false;

        HoverRoundedPanel(int radius, Color normalColor, Color hoverColor) {
            super(radius, normalColor, false);
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;

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
}