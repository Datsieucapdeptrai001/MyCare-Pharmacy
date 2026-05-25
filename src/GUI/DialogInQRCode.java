package GUI;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.HashMap;
import java.util.Map;

public class DialogInQRCode extends JDialog {

    private static final Color PRIMARY_BLUE = new Color(14, 116, 144);
    private static final Color SUCCESS_GREEN = new Color(34, 197, 94);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color BG_LIGHT = new Color(241, 245, 249);

    public DialogInQRCode(Window owner, String qrData, String maPhieu, String ngayXuat, String tenKienHang) {
        super(owner, "In Tem QR Code Phiếu Xuất", ModalityType.APPLICATION_MODAL);
        setSize(420, 520);
        setLocationRelativeTo(owner);
        setUndecorated(true);

        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBackground(Color.WHITE);
        pnlMain.setBorder(new LineBorder(PRIMARY_BLUE, 2));

        // Header
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(PRIMARY_BLUE);
        pnlHeader.setPreferredSize(new Dimension(0, 50));
        JLabel lblHeader = new JLabel("IN TEM QR KIỆN HÀNG XUẤT", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader.setForeground(Color.WHITE);
        pnlHeader.add(lblHeader, BorderLayout.CENTER);

        // Body
        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(new EmptyBorder(25, 20, 20, 20));

        JLabel lblTenKien = new JLabel(tenKienHang, SwingConstants.CENTER);
        lblTenKien.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTenKien.setForeground(TEXT_PRIMARY);
        lblTenKien.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblNgay = new JLabel("Ngày xuất: " + ngayXuat, SwingConstants.CENTER);
        lblNgay.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNgay.setForeground(TEXT_SECONDARY);
        lblNgay.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nơi vẽ hình ảnh mã QR Code
        JLabel lblQRCode = new JLabel();
        lblQRCode.setAlignmentX(Component.CENTER_ALIGNMENT);
        try {
            BufferedImage qrImage = generateQR(qrData, 220, 220);
            if (qrImage != null) {
                lblQRCode.setIcon(new ImageIcon(qrImage));
            }
        } catch (Exception e) {
            lblQRCode.setText("[Lỗi thư viện tạo mã QR ZXing]");
            lblQRCode.setForeground(Color.RED);
        }

        JLabel lblMaPhieu = new JLabel("Phiếu: " + maPhieu, SwingConstants.CENTER);
        lblMaPhieu.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblMaPhieu.setForeground(TEXT_PRIMARY);
        lblMaPhieu.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblHuongDan = new JLabel("(Dán tem này lên kiện hàng để quét bằng súng 2D)", SwingConstants.CENTER);
        lblHuongDan.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblHuongDan.setForeground(new Color(148, 163, 184));
        lblHuongDan.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlBody.add(lblTenKien);
        pnlBody.add(Box.createVerticalStrut(5));
        pnlBody.add(lblNgay);
        pnlBody.add(Box.createVerticalStrut(20));
        pnlBody.add(lblQRCode);
        pnlBody.add(Box.createVerticalStrut(15));
        pnlBody.add(lblMaPhieu);
        pnlBody.add(Box.createVerticalStrut(25));
        pnlBody.add(lblHuongDan);

        // Footer chứa nút bấm
        JPanel pnlFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        pnlFooter.setBackground(Color.WHITE);

        JButton btnClose = createButton("Đóng", BG_LIGHT, TEXT_SECONDARY);
        btnClose.addActionListener(e -> dispose());

        JButton btnPrint = createButton("In ra máy in", SUCCESS_GREEN, Color.WHITE);
        btnPrint.addActionListener(e -> {
            // Giấu 2 nút bấm đi để tờ giấy in ra không bị dính hình cái nút
            pnlFooter.setVisible(false);

            // Gọi hàm đẩy dữ liệu xuống máy in thật
            inRaMayIn(pnlMain);

            // Lệnh in kết thúc (hoặc bị người dùng bấm Hủy), thì hiện lại 2 nút bấm
            pnlFooter.setVisible(true);
        });

        pnlFooter.add(btnClose);
        pnlFooter.add(btnPrint);

        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlMain.add(pnlBody, BorderLayout.CENTER);
        pnlMain.add(pnlFooter, BorderLayout.SOUTH);

        setContentPane(pnlMain);
    }

    // =========================================================================
    // HÀM XỬ LÝ IN RA MÁY IN THẬT (KẾT NỐI VỚI BỘ ĐỆM WINDOWS/MACOS)
    // =========================================================================
    private void inRaMayIn(JPanel panelCanIn) {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setJobName("In Tem Kien Hang - " + System.currentTimeMillis());

        printerJob.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;

                // Dịch chuyển con trỏ vẽ vào vùng có thể in của tờ giấy (tránh bị lẹm viền
                // margin)
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                // Tính toán tỷ lệ thu phóng (Scale) để tem nhãn vừa vặn khổ giấy máy in
                double widthScale = pageFormat.getImageableWidth() / panelCanIn.getWidth();
                double heightScale = pageFormat.getImageableHeight() / panelCanIn.getHeight();
                double scale = Math.min(widthScale, heightScale); // Lấy tỷ lệ nhỏ nhất để không bị méo

                // Căn giữa tem trên trang giấy in
                double xOffset = (pageFormat.getImageableWidth() - (panelCanIn.getWidth() * scale)) / 2.0;
                double yOffset = (pageFormat.getImageableHeight() - (panelCanIn.getHeight() * scale)) / 2.0;

                g2d.translate(xOffset, yOffset);
                g2d.scale(scale, scale);

                // Lệnh quan trọng nhất: Yêu cầu Panel vẽ lại chính nó lên luồng dữ liệu của máy
                // in
                panelCanIn.printAll(g2d);

                return Printable.PAGE_EXISTS;
            }
        });

        // Bật cửa sổ "Print Dialog" tiêu chuẩn của hệ điều hành để ông chọn Máy In
        // (Xprinter, Canon, HP...)
        boolean isPrintConfirmed = printerJob.printDialog();

        if (isPrintConfirmed) {
            try {
                printerJob.print(); // Máy in bắt đầu chạy "rẹt rẹt"
                JOptionPane.showMessageDialog(this, "Đã gửi lệnh đến máy in thành công!", "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose(); // In xong thì tự đóng form
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Có lỗi giao tiếp với máy in: " + ex.getMessage(), "Lỗi máy in",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    // =========================================================================

    // Thuật toán vẽ QRCode
    private BufferedImage generateQR(String text, int width, int height) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // Chuẩn tiếng việt có dấu
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return image;
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 30, 10, 30));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.darker());
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }
}