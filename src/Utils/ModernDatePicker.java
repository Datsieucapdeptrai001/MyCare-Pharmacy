package Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.awt.Window;

public class ModernDatePicker extends JDialog {
    private Calendar calendar = Calendar.getInstance();
    private JLabel lblMonthYear;
    private JPanel pnlDays;
    private JTextField targetField;

    public ModernDatePicker(Window parent, JTextField targetField) {
    	super(parent);
        setModal(true);
        this.targetField = targetField;
        setUndecorated(true);
        setSize(280, 280);
        getContentPane().setBackground(Color.WHITE);
        getRootPane().setBorder(BorderFactory.createLineBorder(Color.decode("#1967D2"), 1));

        // Tính toán vị trí: Nổi lên ngay bên dưới ô Nhập ngày sinh
        Point p = targetField.getLocationOnScreen();
        setLocation(p.x, p.y + targetField.getHeight() + 2);

        setLayout(new BorderLayout());

        // HEADER: Nút bấm chuyển tháng
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.decode("#1967D2"));
        pnlHeader.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnPrev = new JButton("<");
        styleNavButton(btnPrev);
        btnPrev.addActionListener(e -> { calendar.add(Calendar.MONTH, -1); updateCalendar(); });

        lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setForeground(Color.WHITE);
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JButton btnNext = new JButton(">");
        styleNavButton(btnNext);
        btnNext.addActionListener(e -> { calendar.add(Calendar.MONTH, 1); updateCalendar(); });

        pnlHeader.add(btnPrev, BorderLayout.WEST);
        pnlHeader.add(lblMonthYear, BorderLayout.CENTER);
        pnlHeader.add(btnNext, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // --- BODY: Lưới ngày tháng ---
        JPanel pnlGrid = new JPanel(new BorderLayout());
        JPanel pnlDow = new JPanel(new GridLayout(1, 7));
        pnlDow.setBackground(Color.WHITE);
        String[] dow = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        for (String d : dow) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(Color.GRAY);
            pnlDow.add(lbl);
        }
        pnlGrid.add(pnlDow, BorderLayout.NORTH);

        pnlDays = new JPanel(new GridLayout(6, 7));
        pnlDays.setBackground(Color.WHITE);
        pnlGrid.add(pnlDays, BorderLayout.CENTER);
        add(pnlGrid, BorderLayout.CENTER);

        // --- FOOTER: Nút Đóng ---
        JButton btnClose = new JButton("Đóng");
        btnClose.setForeground(Color.decode("#DC2626"));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());
        add(btnClose, BorderLayout.SOUTH);

        updateCalendar();

        // Tự động đóng lịch khi bấm ra chỗ khác
        addWindowFocusListener(new WindowAdapter() {
            public void windowLostFocus(WindowEvent e) { dispose(); }
        });
    }

    private void styleNavButton(JButton btn) {
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void updateCalendar() {
        pnlDays.removeAll();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
        lblMonthYear.setText("Tháng " + sdf.format(calendar.getTime()));

        Calendar temp = (Calendar) calendar.clone();
        temp.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = temp.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = temp.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < 42; i++) {
            if (i < firstDayOfWeek || i >= firstDayOfWeek + daysInMonth) {
                pnlDays.add(new JLabel("")); // Ô trống
            } else {
                int day = i - firstDayOfWeek + 1;
                JButton btnDay = new JButton(String.valueOf(day));
                btnDay.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                btnDay.setBackground(Color.WHITE);
                btnDay.setFocusPainted(false);
                btnDay.setBorder(BorderFactory.createLineBorder(Color.decode("#F3F4F6")));
                btnDay.setCursor(new Cursor(Cursor.HAND_CURSOR));
                
                // Hiệu ứng rê chuột
                btnDay.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { btnDay.setBackground(Color.decode("#E0F2FE")); }
                    public void mouseExited(MouseEvent e) { btnDay.setBackground(Color.WHITE); }
                });

                // Xử lý khi chọn ngày
                btnDay.addActionListener(e -> {
                    String m = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
                    String d = String.format("%02d", day);
                    String y = String.valueOf(calendar.get(Calendar.YEAR));
                    targetField.setText(d + "/" + m + "/" + y);
                    targetField.setForeground(Color.BLACK);
                    dispose(); // Chọn xong tự tắt lịch
                });
                pnlDays.add(btnDay);
            }
        }
        pnlDays.revalidate();
        pnlDays.repaint();
    }
}