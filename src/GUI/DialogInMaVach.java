package GUI;

import Utils.MenuIcon;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class DialogInMaVach extends JDialog {

    private static final Color PRIMARY = new Color(14, 116, 144);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color SUCCESS_HOVER = new Color(22, 163, 74);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);

    public DialogInMaVach(Window owner, String maLo, String tenSP) {
        super(owner, "In Tem Mã Lô", ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setSize(400, 380); // Chỉnh lại chiều cao cho vừa vặn với mã 1D
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

        JLabel lblTenSP = new JLabel("<html><div style='text-align: center; width: 300px;'>" + tenSP + "</div></html>",
                SwingConstants.CENTER);
        lblTenSP.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTenSP.setForeground(TEXT_PRIMARY);
        lblTenSP.setAlignmentX(Component.CENTER_ALIGNMENT);

        // TẠO HÌNH ẢNH MÃ VẠCH 1D (CODE 128) TỪ MÃ LÔ
        JLabel lblBarcode = new JLabel();
        lblBarcode.setAlignmentX(Component.CENTER_ALIGNMENT);
        try {
            Code128Writer barcodeWriter = new Code128Writer();
            // Mã vạch 1D cần chiều rộng lớn và chiều cao thấp (VD: 280 x 80)
            BitMatrix bitMatrix = barcodeWriter.encode(maLo, BarcodeFormat.CODE_128, 280, 80);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
            lblBarcode.setIcon(new ImageIcon(image));
        } catch (Exception e) {
            lblBarcode.setText("Lỗi tạo mã vạch");
            lblBarcode.setForeground(Color.RED);
            e.printStackTrace();
        }

        JLabel lblMaLo = new JLabel(maLo, SwingConstants.CENTER);
        lblMaLo.setFont(new Font("Consolas", Font.BOLD, 16)); // Dùng font monospace cho giống số mã vạch
        lblMaLo.setForeground(TEXT_PRIMARY);
        lblMaLo.setAlignmentX(Component.CENTER_ALIGNMENT);

        body.add(lblTenSP);
        body.add(Box.createVerticalStrut(25));
        body.add(lblBarcode); // In mã vạch
        body.add(Box.createVerticalStrut(5));
        body.add(lblMaLo); // In chữ mã lô ngay dưới mã vạch
        body.add(Box.createVerticalStrut(20));

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

        JButton btnInTem = new JButton("In tem nhãn");
        stylePrimaryButton(btnInTem);
        // btnInTem.setIcon(new MenuIcon("PRINT"));
        btnInTem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Đang gửi lệnh in tới máy in mã vạch...", "In thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
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
}