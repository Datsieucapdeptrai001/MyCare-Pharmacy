
package GUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ManHinhHuongDan extends JPanel {

    private JTextField txtSearch;
    private final List<FaqBlock> dsFaq = new ArrayList<>();
    private final List<DocumentCard> dsDocs = new ArrayList<>();

    public ManHinhHuongDan() {
        setLayout(new BorderLayout(0, 16));
        setBackground(Color.decode("#F4F6F8"));
        setBorder(new EmptyBorder(16, 20, 16, 20));

        seedData();

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("HƯỚNG DẪN SỬ DỤNG");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(new Color(22, 54, 97));

        JLabel sub = new JLabel("Tài liệu và câu hỏi thường gặp");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        sub.setForeground(new Color(100, 116, 139));
        sub.setBorder(new EmptyBorder(0, 0, 12, 0));

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(520, 40));
        txtSearch.setMaximumSize(new Dimension(560, 40));
        txtSearch.setToolTipText("Tìm kiếm hướng dẫn, câu hỏi...");
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(0, 12, 0, 12)
        ));
        txtSearch.addActionListener(e -> reloadBody());

        header.add(title);
        header.add(sub);
        header.add(txtSearch);
        return header;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new GridLayout(1, 2, 18, 0));
        body.setOpaque(false);
        body.add(createDocColumn());
        body.add(createFaqColumn());
        return body;
    }

    private JPanel createDocColumn() {
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("📖 Tài liệu & Video");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(35, 49, 66));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        left.add(title);

        String kw = txtSearch == null ? "" : txtSearch.getText().trim().toLowerCase();
        for (DocumentCard doc : dsDocs) {
            if (kw.isEmpty() || doc.title.toLowerCase().contains(kw) || doc.desc.toLowerCase().contains(kw)) {
                left.add(createDocumentCard(doc));
                left.add(Box.createVerticalStrut(10));
            }
        }
        return left;
    }

    private JPanel createFaqColumn() {
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("❔ Câu hỏi thường gặp");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(35, 49, 66));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        right.add(title);

        String kw = txtSearch == null ? "" : txtSearch.getText().trim().toLowerCase();
        for (FaqBlock faq : dsFaq) {
            if (kw.isEmpty() || faq.question.toLowerCase().contains(kw) || faq.answer.toLowerCase().contains(kw)) {
                right.add(createFaqCard(faq));
                right.add(Box.createVerticalStrut(8));
            }
        }
        return right;
    }

    private JPanel createDocumentCard(DocumentCard doc) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(doc.active ? new Color(96, 165, 250) : new Color(226, 232, 240), 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 94));

        JLabel icon = new JLabel(doc.icon, SwingConstants.CENTER);
        icon.setOpaque(true);
        icon.setBackground(doc.active ? new Color(37, 99, 235) : new Color(226, 232, 240));
        icon.setForeground(doc.active ? Color.WHITE : new Color(37, 99, 235));
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        icon.setPreferredSize(new Dimension(42, 42));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(new EmptyBorder(0, 12, 0, 0));

        JLabel t = new JLabel(doc.title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 16));
        t.setForeground(new Color(30, 41, 59));

        JLabel d = new JLabel(doc.desc);
        d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        d.setForeground(new Color(100, 116, 139));

        JLabel time = new JLabel("⏱ " + doc.time);
        time.setFont(new Font("Segoe UI", Font.BOLD, 14));
        time.setForeground(new Color(79, 70, 229));

        center.add(t);
        center.add(Box.createVerticalStrut(4));
        center.add(d);
        center.add(Box.createVerticalStrut(6));
        center.add(time);

        card.add(icon, BorderLayout.WEST);
        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFaqCard(FaqBlock faq) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, faq.expanded ? 120 : 54));

        JButton header = new JButton(faq.index + "    " + faq.question + (faq.expanded ? "      ▾" : "      ▸"));
        header.setHorizontalAlignment(SwingConstants.LEFT);
        header.setFocusPainted(false);
        header.setBorder(new EmptyBorder(12, 14, 12, 14));
        header.setBackground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setForeground(new Color(30, 41, 59));

        JTextArea answer = new JTextArea(faq.answer);
        answer.setLineWrap(true);
        answer.setWrapStyleWord(true);
        answer.setEditable(false);
        answer.setVisible(faq.expanded);
        answer.setBackground(new Color(241, 245, 249));
        answer.setForeground(new Color(71, 85, 105));
        answer.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        answer.setBorder(new EmptyBorder(10, 14, 12, 14));

        header.addActionListener(e -> {
            faq.expanded = !faq.expanded;
            removeAll();
            setLayout(new BorderLayout(0, 16));
            setBackground(Color.decode("#F4F6F8"));
            setBorder(new EmptyBorder(16, 20, 16, 20));
            add(createHeader(), BorderLayout.NORTH);
            add(createBody(), BorderLayout.CENTER);
            revalidate();
            repaint();
        });

        card.add(header, BorderLayout.NORTH);
        card.add(answer, BorderLayout.CENTER);
        return card;
    }

    private void reloadBody() {
        removeAll();
        setLayout(new BorderLayout(0, 16));
        setBackground(Color.decode("#F4F6F8"));
        setBorder(new EmptyBorder(16, 20, 16, 20));
        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void seedData() {
        dsDocs.add(new DocumentCard("📘", "Hướng dẫn sử dụng cơ bản", "Làm quen với giao diện và các tính năng chính", "10 phút", false));
        dsDocs.add(new DocumentCard("🎥", "Video: Quản lý kho dược", "Hướng dẫn nhập/xuất kho, kiểm kê tồn kho", "15 phút", false));
        dsDocs.add(new DocumentCard("📄", "Tài liệu kỹ thuật", "Cấu hình hệ thống, backup dữ liệu", "20 phút", true));
        dsDocs.add(new DocumentCard("📖", "Quy trình bán hàng", "Từ tạo đơn đến hoàn thành, quản lý thanh toán", "8 phút", false));
        dsDocs.add(new DocumentCard("🎥", "Video: Báo cáo & Thống kê", "Phân tích doanh thu, xuất báo cáo Excel/PDF", "12 phút", false));
        dsDocs.add(new DocumentCard("📄", "Chính sách bảo mật", "Phân quyền, bảo mật tài khoản người dùng", "5 phút", false));

        dsFaq.add(new FaqBlock(1, "Làm thế nào để thêm sản phẩm mới?",
                "Vào menu 'Sản phẩm' → Click nút 'Thêm mới' (màu đỏ góc trên phải) → Điền đầy đủ thông tin sản phẩm → Click 'Thêm sản phẩm'. Mã sản phẩm sẽ được tự động tạo.", true));
        dsFaq.add(new FaqBlock(2, "Cách xuất báo cáo Excel?",
                "Mở màn hình Thống kê → chọn khoảng thời gian → bấm Xuất Excel. Nếu project đang dùng Apache POI/Jasper thì nối action tại đây.", false));
        dsFaq.add(new FaqBlock(3, "Làm thế nào để cập nhật thông tin sản phẩm?",
                "Tại màn hình Sản phẩm, chọn dòng cần sửa → bấm cập nhật → chỉnh thông tin → lưu lại.", false));
        dsFaq.add(new FaqBlock(4, "Cách tạo khuyến mại mới?",
                "Vào menu Khuyến mại → tạo chương trình mới → chọn điều kiện áp dụng và hình thức khuyến mại → lưu.", false));
        dsFaq.add(new FaqBlock(5, "Làm sao để xem thống kê doanh thu?",
                "Mở màn hình Thống kê → chọn ngày bắt đầu / kết thúc → xem biểu đồ và bảng tổng hợp doanh thu.", false));
        dsFaq.add(new FaqBlock(6, "Cách phân quyền nhân viên?",
                "Vào menu Nhân viên hoặc Tài khoản → chỉnh vai trò → cập nhật quyền truy cập theo chức vụ.", false));
        dsFaq.add(new FaqBlock(7, "Làm thế nào để nhập hàng từ file Excel?",
                "Bạn có thể tạo nút import tại màn hình lô hàng rồi dùng Apache POI đọc file Excel và map dữ liệu vào Entity/DTO.", false));
        dsFaq.add(new FaqBlock(8, "Hệ thống hỗ trợ bao nhiêu đơn vị quy đổi?",
                "Có thể mở rộng bằng cách tách bảng DonViQuyDoi và ChiTietQuyDoi thay vì lưu cứng trong bảng sản phẩm.", false));
    }

    private static class DocumentCard {
        String icon;
        String title;
        String desc;
        String time;
        boolean active;

        DocumentCard(String icon, String title, String desc, String time, boolean active) {
            this.icon = icon;
            this.title = title;
            this.desc = desc;
            this.time = time;
            this.active = active;
        }
    }

    private static class FaqBlock {
        int index;
        String question;
        String answer;
        boolean expanded;

        FaqBlock(int index, String question, String answer, boolean expanded) {
            this.index = index;
            this.question = question;
            this.answer = answer;
            this.expanded = expanded;
        }
    }
}
