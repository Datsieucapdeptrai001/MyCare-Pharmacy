package GUI;

import Utils.MenuIcon;
import Utils.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Dialog Mở Ca – hiện ra sau khi Nhân Viên (STAFF) đăng nhập thành công.
 * Thiết kế theo mẫu Figma: chọn loại ca → kiểm đếm tiền đầu ca → xác nhận.
 */
public class ManHinhMoCa extends JDialog {

    // ======================== CONSTANTS ========================
    private static final Color COLOR_GREEN   = Color.decode("#00A76F");
    private static final Color COLOR_ORANGE  = Color.decode("#FF6B00");
    private static final Color COLOR_INDIGO  = Color.decode("#3D52A0");
    private static final Color COLOR_HEADER  = Color.decode("#00A76F");
    private static final Color COLOR_BG      = Color.decode("#F8FAFB");
    private static final Color COLOR_CARD    = Color.WHITE;
    private static final Color COLOR_BORDER  = Color.decode("#E5E9EF");
    private static final Color COLOR_CONFIRM = Color.decode("#00A76F");

    private static final String[] CA_LABELS  = { "Ca Sáng", "Ca Chiều", "Ca Tối" };
    private static final String[] CA_TIMES   = { "06:00 – 14:00", "14:00 – 22:00", "22:00 – 06:00" };
    private static final String[] CA_ICON_TYPES = { "SHIFT_MORNING", "SHIFT_AFTERNOON", "SHIFT_NIGHT" };
    // Màu NỀN của 3 Ca (Sáng - Chiều - Tối)
    private static final Color[]  CA_BG = {
        Color.decode("#FFF3E0"), // Màu cam nhạt (Ca Sáng)
        Color.decode("#FFF8F0"), // Màu cam siêu nhạt (Ca Chiều)
        Color.decode("#EEF2FF")  // Màu xanh dương nhạt (Ca Tối)
    };
    // Màu CHỮ & VIỀN của 3 Ca
    private static final Color[]  CA_FG = {
        Color.decode("#E65100"), // Cam đậm
        Color.decode("#BF360C"), // Đỏ cam
        Color.decode("#3D52A0")  // Xanh dương đậm
    };

    // Mệnh giá VND
    private static final long[]   MENH_GIA     = { 500000, 200000, 100000, 50000, 20000, 10000, 5000, 2000, 1000 };
    private static final String[] MENH_GIA_STR = { "500.000đ","200.000đ","100.000đ","50.000đ","20.000đ","10.000đ","5.000đ","2.000đ","1.000đ" };
    private static final Color[]  BOX_COLORS   = {
        Color.decode("#1A73E8"), Color.decode("#9C27B0"), Color.decode("#4CAF50"),
        Color.decode("#FF9800"), Color.decode("#F44336"), Color.decode("#E91E63"),
        Color.decode("#FF7043"), Color.decode("#1A73E8"), Color.decode("#607D8B")
    };

    // ======================== STATE ========================
    private int selectedCa = 0;          // 0=sáng,1=chiều,2=tối
    private int[] soLuong  = new int[9]; // số tờ mỗi mệnh giá
    private boolean confirmed = false;

    // ======================== UI REFS ========================
    private JButton[] btnCa      = new JButton[3];
    private JLabel[]  lblCount   = new JLabel[9];
    private JLabel    lblTongTien;
    private JLabel    lblCaSubInfo;
    private CardLayout stepCard;
    private JPanel    stepPanel;
    private CardLayout footerCardLayout;
    private JPanel     footerSwitcher;

    // ======================== CONSTRUCTOR ========================
    public ManHinhMoCa(Frame parent) {
        super(parent, "Mở ca làm việc", true);
        setUndecorated(true);
        setSize(680, 680);
        setLocationRelativeTo(parent);
        setBackground(new Color(0, 0, 0, 0));
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // shadow
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(6, 6, getWidth() - 6, getHeight() - 6, 20, 20);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 20, 20);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(0, 0, 8, 8));
        setContentPane(root);

        root.add(buildHeader(), BorderLayout.NORTH);

        stepCard  = new CardLayout();
        stepPanel = new JPanel(stepCard);
        stepPanel.setOpaque(false);
        stepPanel.add(buildStep1(), "step1");
        stepPanel.add(buildStep2(), "step2");
        root.add(stepPanel, BorderLayout.CENTER);

        footerCardLayout = new CardLayout();
        footerSwitcher   = new JPanel(footerCardLayout);
        footerSwitcher.setOpaque(false);
        footerSwitcher.add(buildStep1Footer(), "step1");
        footerSwitcher.add(buildStep2Footer(), "step2");
        root.add(footerSwitcher, BorderLayout.SOUTH);
    }

    // ─── HEADER ──────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0,
                        Color.decode("#00A76F"), getWidth(), 0, Color.decode("#00C98A")));
                // rounded only top corners
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 20, 20, 20);
                g2.dispose();
            }
        };
        hdr.setOpaque(false);
        hdr.setPreferredSize(new Dimension(0, 70));
        hdr.setBorder(new EmptyBorder(0, 20, 0, 20));

        // Logo + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JPanel logoBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.rotate(Math.toRadians(-45), cx, cy);
                g2.drawRoundRect(cx - 11, cy - 5, 22, 10, 8, 8);
                g2.drawLine(cx, cy - 5, cx, cy + 5);
                g2.dispose();
            }
        };
        logoBox.setOpaque(false);
        logoBox.setPreferredSize(new Dimension(44, 44));

        JLabel lblName = new JLabel("<html><b style='color:white;font-size:15px;'>MYCARE PHARMACY</b><br>"
                + "<span style='color:rgba(255,255,255,0.8);font-size:11px;'>⏰ Mở ca làm việc — "
                + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())
                + "</span></html>");

        left.add(logoBox);
        left.add(lblName);

        // User badge
        String initials  = UserSession.getInstance().getInitials();
        String tenNV     = UserSession.getInstance().getTenHienThi();
        String chucVu    = UserSession.getInstance().getChucVuHienThi();

        JPanel userBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        userBadge.setOpaque(false);

        JLabel avatar = new JLabel(initials, SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        avatar.setForeground(Color.WHITE);
        avatar.setOpaque(true);
        avatar.setBackground(Color.decode("#007A52"));
        avatar.setPreferredSize(new Dimension(34, 34));

        JLabel lblUser = new JLabel("<html><b style='color:white;'>" + tenNV + "</b><br>"
                + "<span style='color:rgba(255,255,255,0.75);font-size:10px;'>" + chucVu + "</span></html>");

        userBadge.add(avatar);
        userBadge.add(lblUser);

        hdr.add(left, BorderLayout.WEST);
        hdr.add(userBadge, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(hdr, BorderLayout.CENTER);
        return wrapper;
    }

 // ─── STEP 1: Chọn Ca ─────────────────────────────────────
    private JPanel buildStep1() {
        // ---- ĐÃ FIX: Xác định Ca theo giờ ngay từ đầu TRƯỚC KHI vẽ giao diện ----
        int currentHour = java.time.LocalDateTime.now().getHour();
        if (currentHour >= 6 && currentHour < 14) {
            selectedCa = 0; // Ca Sáng
        } else if (currentHour >= 14 && currentHour < 22) {
            selectedCa = 1; // Ca Chiều
        } else {
            selectedCa = 2; // Ca Tối
        }
        // -------------------------------------------------------------------------

        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
        pnl.setBackground(COLOR_BG);
        pnl.setBorder(new EmptyBorder(20, 24, 10, 24));

        // Section title
        JLabel secTitle1 = makeSectionTitle("INFO_CIRCLE", "BƯỚC 1 — THÔNG TIN CA LÀM VIỆC HIỆN TẠI");
        pnl.add(secTitle1);
        pnl.add(Box.createRigidArea(new Dimension(0, 12)));

        // Ca buttons
        JPanel rowCa = new JPanel(new GridLayout(1, 3, 12, 0));
        rowCa.setOpaque(false);
        rowCa.setAlignmentX(LEFT_ALIGNMENT);
        rowCa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Lúc này vòng lặp vẽ nút sẽ biết chính xác "selectedCa" là ca nào để tô màu
        for (int i = 0; i < 3; i++) {
            btnCa[i] = buildCaButton(i);
            rowCa.add(btnCa[i]);
        }
        pnl.add(rowCa);
        pnl.add(Box.createRigidArea(new Dimension(0, 20)));

        // Section title 2
        JLabel secTitle2 = makeSectionTitle("TAB_DOLLAR", "BƯỚC 2 — KIỂM ĐẾM TIỀN ĐẦU CA");
        pnl.add(secTitle2);
        pnl.add(Box.createRigidArea(new Dimension(0, 12)));

        // Grid tiền
        JPanel gridTien = new JPanel(new GridLayout(3, 3, 12, 10));
        gridTien.setOpaque(false);
        gridTien.setAlignmentX(LEFT_ALIGNMENT);
        gridTien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 195));

        for (int i = 0; i < 9; i++) {
            gridTien.add(buildMoneyBox(i));
        }
        pnl.add(gridTien);
        pnl.add(Box.createRigidArea(new Dimension(0, 12)));

        // Tổng tiền đầu ca
        JPanel totalBox = new JPanel(new BorderLayout());
        totalBox.setBackground(Color.WHITE);
        totalBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                new EmptyBorder(10, 14, 10, 14)));
        totalBox.setAlignmentX(LEFT_ALIGNMENT);
        totalBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        JPanel leftInfo = new JPanel();
        leftInfo.setOpaque(false);
        leftInfo.setLayout(new BoxLayout(leftInfo, BoxLayout.Y_AXIS));
        JLabel lblTongLabel = new JLabel("Tổng tiền đầu ca:");
        lblTongLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        // Cập nhật lại dòng chữ nhỏ xíu bên dưới chuẩn theo Ca
        lblCaSubInfo = new JLabel(CA_LABELS[selectedCa] + " · " + CA_TIMES[selectedCa]);
        lblCaSubInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCaSubInfo.setForeground(Color.GRAY);
        
        leftInfo.add(lblTongLabel);
        leftInfo.add(lblCaSubInfo);

        lblTongTien = new JLabel("0đ", SwingConstants.RIGHT);
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTongTien.setForeground(Color.decode("#212B36"));

        totalBox.add(leftInfo, BorderLayout.WEST);
        totalBox.add(lblTongTien, BorderLayout.EAST);
        pnl.add(totalBox);
        pnl.add(Box.createRigidArea(new Dimension(0, 8)));

        // Hint
        JPanel hint = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        hint.setBackground(Color.decode("#FFFBE6"));
        hint.setBorder(BorderFactory.createLineBorder(Color.decode("#FFD666")));
        hint.setAlignmentX(LEFT_ALIGNMENT);
        hint.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JLabel lblHint = new JLabel("Có thể bỏ qua nếu chưa có tiền mặt đầu ca — nhấn \"Tiếp tục\" để xác nhận 0đ");
        lblHint.setIcon(new MenuIcon("INFO_CIRCLE"));
        lblHint.setIconTextGap(6);
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHint.setForeground(Color.decode("#7A5800"));
        hint.add(lblHint);
        pnl.add(hint);

        return pnl;
    }

    private JPanel buildStep2() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(COLOR_BG);
        pnl.setBorder(new EmptyBorder(20, 0, 20, 0)); 

        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = 500; // Ép form biên lai chuẩn 500px
                return d;
            }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#E6F7F2"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(Color.decode("#B2DFDB"));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 24, 16, 24)); 

        JPanel contentWrap = new JPanel();
        contentWrap.setLayout(new BoxLayout(contentWrap, BoxLayout.Y_AXIS));
        contentWrap.setOpaque(false);

        JLabel lblTitle = new JLabel("PHIẾU XÁC NHẬN MỞ CA", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.decode("#006250")); 
        lblTitle.setAlignmentX(CENTER_ALIGNMENT);
        contentWrap.add(lblTitle);
        contentWrap.add(Box.createRigidArea(new Dimension(0, 4)));

        JLabel lblTime = new JLabel(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTime.setForeground(Color.decode("#00A76F"));
        lblTime.setAlignmentX(CENTER_ALIGNMENT);
        contentWrap.add(lblTime);
        contentWrap.add(Box.createRigidArea(new Dimension(0, 12))); 
        contentWrap.add(createDashedLine());
        contentWrap.add(Box.createRigidArea(new Dimension(0, 12))); 

        JPanel nvRow = buildConfirmRow("USER", UserSession.getInstance().getTenHienThi() + "  •  " + UserSession.getInstance().getChucVuHienThi());
        nvRow.setMaximumSize(new Dimension(450, 40)); 
        nvRow.setAlignmentX(CENTER_ALIGNMENT);
        contentWrap.add(nvRow);
        contentWrap.add(Box.createRigidArea(new Dimension(0, 8))); 

        JPanel caRow = buildConfirmCaRow();
        caRow.setMaximumSize(new Dimension(450, 40)); 
        caRow.setAlignmentX(CENTER_ALIGNMENT);
        contentWrap.add(caRow);
        contentWrap.add(Box.createRigidArea(new Dimension(0, 12))); 
        
        contentWrap.add(createDashedLine());
        contentWrap.add(Box.createRigidArea(new Dimension(0, 12))); 

        JPanel chiTietHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        chiTietHeader.setOpaque(false);
        chiTietHeader.setMaximumSize(new Dimension(450, 20)); 
        chiTietHeader.setAlignmentX(CENTER_ALIGNMENT);
        
        JLabel lblChiTiet = new JLabel("CHI TIẾT MỆNH GIÁ:");
        lblChiTiet.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblChiTiet.setForeground(Color.decode("#637381"));
        chiTietHeader.add(lblChiTiet);
        contentWrap.add(chiTietHeader);
        contentWrap.add(Box.createRigidArea(new Dimension(0, 8))); 

        JPanel chiTietPanel = new JPanel();
        chiTietPanel.setLayout(new BoxLayout(chiTietPanel, BoxLayout.Y_AXIS));
        chiTietPanel.setOpaque(false); 
        chiTietPanel.setAlignmentX(CENTER_ALIGNMENT);

        long tongTien = 0;
        for (int i = 0; i < 9; i++) {
            if (soLuong[i] > 0) {
                long subtotal = soLuong[i] * MENH_GIA[i];
                tongTien += subtotal;
                JPanel row = new JPanel(new BorderLayout());
                row.setOpaque(false); 
                row.setMaximumSize(new Dimension(450, 22)); 
                
                JLabel lLeft = new JLabel(MENH_GIA_STR[i] + "  ×  " + soLuong[i] + " tờ");
                lLeft.setFont(new Font("Segoe UI", Font.PLAIN, 13)); 
                lLeft.setForeground(Color.decode("#212B36"));
                
                JLabel lRight = new JLabel(formatMoney(subtotal), SwingConstants.RIGHT);
                lRight.setFont(new Font("Segoe UI", Font.BOLD, 13)); 
                lRight.setForeground(Color.decode("#212B36"));
                
                row.add(lLeft, BorderLayout.WEST);
                row.add(lRight, BorderLayout.EAST);
                chiTietPanel.add(row);
                chiTietPanel.add(Box.createRigidArea(new Dimension(0, 4))); 
            }
        }
        
        if (tongTien == 0) {
            JLabel empty = new JLabel("(Không có tiền mặt đầu ca)");
            empty.setForeground(Color.GRAY);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            empty.setAlignmentX(CENTER_ALIGNMENT);
            chiTietPanel.add(empty);
        }

        contentWrap.add(chiTietPanel);

        contentWrap.add(Box.createRigidArea(new Dimension(0, 8))); 
        contentWrap.add(createDashedLine());
        contentWrap.add(Box.createRigidArea(new Dimension(0, 12))); 

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setAlignmentX(CENTER_ALIGNMENT);
        totalRow.setMaximumSize(new Dimension(450, 35)); 
        
        JLabel lTotal  = new JLabel("TỔNG TIỀN ĐẦU CA:");
        lTotal.setFont(new Font("Segoe UI", Font.BOLD, 13)); 
        lTotal.setForeground(Color.decode("#212B36"));
        
        JLabel lAmount = new JLabel(formatMoney(tongTien), SwingConstants.RIGHT);
        lAmount.setFont(new Font("Segoe UI", Font.BOLD, 22)); 
        lAmount.setForeground(Color.decode("#006250"));
        
        totalRow.add(lTotal, BorderLayout.WEST);
        totalRow.add(lAmount, BorderLayout.EAST);
        contentWrap.add(totalRow);

        card.add(contentWrap, BorderLayout.CENTER);

        JPanel centerWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerWrap.setOpaque(false);
        centerWrap.add(card);

        pnl.add(centerWrap, BorderLayout.NORTH);
        return pnl;
    }

    private JPanel buildStep1Footer() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER),
                new EmptyBorder(12, 20, 12, 20)));

        JButton btnReset = new JButton("Nhập lại");
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnReset.setForeground(Color.decode("#637381"));
        btnReset.setBackground(Color.WHITE);
        btnReset.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                new EmptyBorder(8, 16, 8, 16)));
        btnReset.setFocusPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> {
            soLuong = new int[9];
            refreshCounts();
            refreshTotal();
            stepCard.show(stepPanel, "step1");
        });

        JButton btnNext = new JButton("Xem lại & Xác nhận");
        btnNext.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnNext.setForeground(Color.WHITE);
        btnNext.setBackground(COLOR_CONFIRM);
        btnNext.setBorder(new EmptyBorder(10, 22, 10, 22));
        btnNext.setFocusPainted(false);
        btnNext.setOpaque(true);
        btnNext.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNext.addActionListener(e -> {
            stepPanel.remove(1);
            stepPanel.add(buildStep2(), "step2", 1);
            stepCard.show(stepPanel, "step2");
            footerCardLayout.show(footerSwitcher, "step2");

            // TUYỆT CHIÊU ÉP CÂN: Lấy Step 1 ra để CardLayout không bị độn chiều cao
            Component step1 = stepPanel.getComponent(0);
            stepPanel.remove(step1);

            // Ép hộp thoại thu nhỏ lại ôm sát sạt vào nội dung của Step 2
            pack();
            setLocationRelativeTo(getParent());

            // Âm thầm nhét Step 1 lại vào vị trí cũ
            stepPanel.add(step1, "step1", 0);
        });

        footer.add(btnReset, BorderLayout.WEST);
        footer.add(btnNext,  BorderLayout.EAST);
        return footer;
    }

    private JPanel buildStep2Footer() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER),
                new EmptyBorder(12, 20, 12, 20)));

        JButton btnSua = new JButton("Sửa lại");
        btnSua.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSua.setForeground(Color.decode("#637381"));
        btnSua.setBackground(Color.WHITE);
        btnSua.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                new EmptyBorder(8, 16, 8, 16)));
        btnSua.setFocusPainted(false);
        btnSua.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSua.addActionListener(e -> {
            stepCard.show(stepPanel, "step1");
            footerCardLayout.show(footerSwitcher, "step1");
            
            // TRẢ LẠI FORM GỐC: Bung cửa sổ to ra lại thành 680x680
            setSize(680, 680); 
            setLocationRelativeTo(getParent());
        });

        JButton btnXacNhan = new JButton("Xác nhận vào ca");
        btnXacNhan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnXacNhan.setForeground(Color.WHITE);
        btnXacNhan.setBackground(COLOR_CONFIRM);
        btnXacNhan.setBorder(new EmptyBorder(10, 22, 10, 22));
        btnXacNhan.setFocusPainted(false);
        btnXacNhan.setOpaque(true);
        btnXacNhan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXacNhan.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        footer.add(btnSua,     BorderLayout.WEST);
        footer.add(btnXacNhan, BorderLayout.EAST);
        return footer;
    }

    // ─── HELPERS ─────────────────────────────────────────────

    private JLabel makeSectionTitle(String iconType, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(Color.decode("#454F5B"));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setIcon(new MenuIcon(iconType));
        lbl.setIconTextGap(6);
        return lbl;
    }

    private JButton buildCaButton(int idx) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = (selectedCa == idx);
                g2.setColor(sel ? CA_BG[idx] : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (sel) {
                    g2.setStroke(new BasicStroke(2));
                    g2.setColor(CA_FG[idx]);
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                } else {
                    g2.setColor(COLOR_BORDER);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                }
                g2.dispose();
            }
        };

        btn.setLayout(new BoxLayout(btn, BoxLayout.Y_AXIS));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        
        // ---- ĐÃ SỬA: Đổi con trỏ chuột về mặc định vì không cho phép bấm nữa ----
        btn.setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); 
        // -----------------------------------------------------------------------
        
        btn.setPreferredSize(new Dimension(0, 90));

        JLabel icon = new JLabel("", SwingConstants.CENTER);
        icon.setIcon(new MenuIcon(CA_ICON_TYPES[idx]) {
            @Override public int getIconWidth()  { return 28; }
            @Override public int getIconHeight() { return 28; }
        });
        
        // Làm mờ màu icon nếu không phải ca được chọn
        if (selectedCa == idx) {
            icon.setForeground(CA_FG[idx]);
        } else {
            icon.setForeground(Color.LIGHT_GRAY);
        }
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel name = new JLabel(CA_LABELS[idx], SwingConstants.CENTER);
        name.setFont(new Font("Segoe UI", Font.BOLD, 14));
        name.setForeground(selectedCa == idx ? CA_FG[idx] : Color.GRAY);
        name.setAlignmentX(CENTER_ALIGNMENT);

        JLabel time = new JLabel(CA_TIMES[idx], SwingConstants.CENTER);
        time.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        time.setForeground(selectedCa == idx ? CA_FG[idx] : Color.LIGHT_GRAY);
        time.setAlignmentX(CENTER_ALIGNMENT);

        btn.add(Box.createVerticalGlue());
        btn.add(icon);
        btn.add(name);
        btn.add(time);
        btn.add(Box.createVerticalGlue());

        // ---- ĐÃ XÓA MẤT DÒNG addActionListener ĐỂ ÉP CỨNG KHÔNG CHO BẤM ----
        // (Nhân viên chỉ được nhìn chứ không được đổi ca)
        
        return btn;
    }

    private JPanel buildMoneyBox(int idx) {
        JPanel box = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BOX_COLORS[idx]);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setBorder(new EmptyBorder(8, 6, 8, 6));
        box.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblGia = new JLabel(MENH_GIA_STR[idx], SwingConstants.CENTER);
        lblGia.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblGia.setForeground(BOX_COLORS[idx]);

        lblCount[idx] = new JLabel("0", SwingConstants.CENTER);
        lblCount[idx].setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCount[idx].setForeground(Color.decode("#212B36"));

        JLabel lblTip = new JLabel("T: tăng  |  P: giảm", SwingConstants.CENTER);
        lblTip.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblTip.setForeground(Color.LIGHT_GRAY);

        box.add(lblGia,   BorderLayout.NORTH);
        box.add(lblCount[idx], BorderLayout.CENTER);
        box.add(lblTip,   BorderLayout.SOUTH);

        box.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    soLuong[idx]++;
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    if (soLuong[idx] > 0) soLuong[idx]--;
                }
                lblCount[idx].setText(String.valueOf(soLuong[idx]));
                refreshTotal();
            }
        });
        return box;
    }

    private JPanel buildConfirmRow(String iconType, String text) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                new EmptyBorder(10, 14, 10, 14)));
        row.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(text);
        lbl.setIcon(new MenuIcon(iconType));
        lbl.setIconTextGap(8);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        row.add(lbl);
        return row;
    }

    private JPanel buildConfirmCaRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(CA_BG[selectedCa]);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#DFE3E8")),
                new EmptyBorder(10, 14, 10, 14)));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lLeft = new JLabel("  " + CA_LABELS[selectedCa]);
        lLeft.setIcon(new MenuIcon(CA_ICON_TYPES[selectedCa]));
        lLeft.setIconTextGap(8);
        lLeft.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lLeft.setForeground(CA_FG[selectedCa]);

        JLabel lRight = new JLabel(CA_TIMES[selectedCa], SwingConstants.RIGHT);
        lRight.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lRight.setForeground(CA_FG[selectedCa]);

        row.add(lLeft,  BorderLayout.WEST);
        row.add(lRight, BorderLayout.EAST);
        return row;
    }

    // ─── LOGIC ───────────────────────────────────────────────

    private void selectCa(int idx) {
        selectedCa = idx;
        for (int i = 0; i < 3; i++) {
            if (btnCa[i] != null) {
                btnCa[i].repaint();
            }
        }
        if (lblCaSubInfo != null) {
            lblCaSubInfo.setText(CA_LABELS[idx] + " · " + CA_TIMES[idx]);
        }
    }

    private void refreshCounts() {
        for (int i = 0; i < 9; i++) {
            lblCount[i].setText("0");
        }
    }

    private void refreshTotal() {
        long tong = 0;
        for (int i = 0; i < 9; i++) tong += soLuong[i] * MENH_GIA[i];
        lblTongTien.setText(formatMoney(tong));
    }

    private String formatMoney(long amount) {
        DecimalFormat df = new DecimalFormat("###,###,###");
        return df.format(amount) + "đ";
    }

    // ─── PUBLIC API ──────────────────────────────────────────

    public boolean isConfirmed()    { return confirmed; }
    public int     getSelectedCa()  { return selectedCa; }
    public String  getCaLabel()     { return CA_LABELS[selectedCa]; }
    public String  getCaTimes()     { return CA_TIMES[selectedCa]; }
    public long getTongTienDauCa() {
        long t = 0;
        for (int i = 0; i < 9; i++) t += soLuong[i] * MENH_GIA[i];
        return t;
    }
 // Tao duong ke dut (dashed line) giong bien lai
    private JComponent createDashedLine() {
        JPanel line = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(COLOR_BORDER);
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
                g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
                g2.dispose();
            }
        };
        line.setOpaque(false);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        return line;
    }
}