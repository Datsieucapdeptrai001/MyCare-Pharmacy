package GUI;

import Utils.MenuIcon;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

public class DialogInMaVach extends JDialog {

    private static final Color PRIMARY = new Color(14, 116, 144);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color SUCCESS_HOVER = new Color(22, 163, 74);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);

    // HÀM KHỞI TẠO ĐÃ NHẬN 4 THAM SỐ (Có thêm maSP)
    public DialogInMaVach(Window owner, String maLo, String maSP, String tenSP) {
        super(owner, "In Tem Mã Lô", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setSize(400, 440);
        setLocationRelativeTo(owner);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(PRIMARY, 2));

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setPreferredSize(new Dimension(0, 50));
        JLabel lblTitle = new JLabel("IN TEM MÃ VẠCH LÔ HÀNG", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        header.add(lblTitle, BorderLayout.CENTER);

        // BODY
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel pnlTemNhan = new JPanel();
        pnlTemNhan.setLayout(new BoxLayout(pnlTemNhan, BoxLayout.Y_AXIS));
        pnlTemNhan.setBackground(Color.WHITE);
        pnlTemNhan.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTenSP = new JLabel("<html><div style='text-align: center; width: 300px;'>" + tenSP + "</div></html>",
                SwingConstants.CENTER);
        lblTenSP.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTenSP.setForeground(TEXT_PRIMARY);
        lblTenSP.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMaSP = new JLabel("Mã SP: " + maSP, SwingConstants.CENTER);
        lblMaSP.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMaSP.setForeground(TEXT_SECONDARY);
        lblMaSP.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblBarcode = new JLabel();
        lblBarcode.setAlignmentX(Component.CENTER_ALIGNMENT);
        try {
            Code128Writer barcodeWriter = new Code128Writer();
            BitMatrix bitMatrix = barcodeWriter.encode(maLo, BarcodeFormat.CODE_128, 280, 80);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            lblBarcode.setIcon(new ImageIcon(image));
        } catch (Exception e) {
            lblBarcode.setText("Lỗi tạo mã vạch");
            lblBarcode.setForeground(Color.RED);
        }

        JLabel lblMaLo = new JLabel("Lô: " + maLo, SwingConstants.CENTER);
        lblMaLo.setFont(new Font("Consolas", Font.BOLD, 16));
        lblMaLo.setForeground(TEXT_PRIMARY);
        lblMaLo.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlTemNhan.add(lblTenSP);
        pnlTemNhan.add(Box.createVerticalStrut(4));
        pnlTemNhan.add(lblMaSP);
        pnlTemNhan.add(Box.createVerticalStrut(15));
        pnlTemNhan.add(lblBarcode);
        pnlTemNhan.add(Box.createVerticalStrut(5));
        pnlTemNhan.add(lblMaLo);

        body.add(pnlTemNhan);
        body.add(Box.createVerticalStrut(25));

        JLabel lblGuide = new JLabel("(Dán tem này lên vỏ hộp để quét bằng súng bắn mã vạch)");
        lblGuide.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblGuide.setForeground(new Color(156, 163, 175));
        lblGuide.setAlignmentX(Component.CENTER_ALIGNMENT);
        body.add(lblGuide);

        // FOOTER
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        footer.setBackground(Color.WHITE);

        JButton btnDong = new JButton("Đóng");
        styleSecondaryButton(btnDong);
        btnDong.addActionListener(e -> dispose());

        JButton btnInTem = new JButton("In ra máy in");
        stylePrimaryButton(btnInTem);
        btnInTem.addActionListener(e -> {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("In Tem Ma Lo: " + maLo);

            job.setPrintable(new Printable() {
                @Override
                public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                    if (pageIndex > 0)
                        return Printable.NO_SUCH_PAGE;
                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                    pnlTemNhan.printAll(g2d);
                    return Printable.PAGE_EXISTS;
                }
            });

            boolean doPrint = job.printDialog();
            if (doPrint) {
                try {
                    job.print();
                    showModernAlert("Đã gửi lệnh in thành công!", true);
                    Timer timer = new Timer(1500, evt -> dispose());
                    timer.setRepeats(false);
                    timer.start();
                } catch (PrinterException ex) {
                    showModernAlert("Lỗi máy in: " + ex.getMessage(), false);
                }
            }
        });

        footer.add(btnDong);
        footer.add(btnInTem);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(SUCCESS);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 40));
        btn.setOpaque(true);
        btn.setBorder(null);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(SUCCESS_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(SUCCESS);
            }
        });
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setFocusPainted(false);
        btn.setForeground(TEXT_SECONDARY);
        btn.setBackground(new Color(241, 245, 249));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 40));
        btn.setOpaque(true);
        btn.setBorder(null);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(226, 232, 240));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(241, 245, 249));
            }
        });
    }

    private void showModernAlert(String message, boolean isSuccess) {
        JDialog dialog = new JDialog(this, "Thông báo", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setSize(380, 160);
        dialog.setLocationRelativeTo(this);
        dialog.setShape(new RoundRectangle2D.Double(0, 0, dialog.getWidth(), dialog.getHeight(), 16, 16));
        Color themeColor = isSuccess ? SUCCESS : new Color(239, 68, 68);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createLineBorder(themeColor, 2));
        JPanel pnlContent = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 35));
        pnlContent.setBackground(Color.WHITE);
        JLabel lblIcon = new JLabel(new MenuIcon(isSuccess ? "CHECK_CIRCLE" : "WARNING"));
        lblIcon.setForeground(themeColor);
        JLabel lblMessage = new JLabel(message);
        lblMessage.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMessage.setForeground(TEXT_PRIMARY);
        pnlContent.add(lblIcon);
        pnlContent.add(lblMessage);
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 16));
        pnlBottom.setBackground(Color.WHITE);
        JButton btnOk = new JButton("OK");
        btnOk.setFocusPainted(false);
        btnOk.setForeground(Color.WHITE);
        btnOk.setBackground(themeColor);
        btnOk.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnOk.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnOk.setBorder(new EmptyBorder(8, 40, 8, 40));
        btnOk.setOpaque(true);
        btnOk.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnOk.setBackground(isSuccess ? SUCCESS_HOVER : themeColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnOk.setBackground(themeColor);
            }
        });
        btnOk.addActionListener(e -> dialog.dispose());
        dialog.getRootPane().setDefaultButton(btnOk);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        pnlBottom.add(btnOk);
        root.add(pnlContent, BorderLayout.CENTER);
        root.add(pnlBottom, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.setVisible(true);
    }
}