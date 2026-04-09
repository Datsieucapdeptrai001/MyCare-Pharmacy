package Components;

import javax.swing.Icon;
import java.awt.*;

public class MenuIcon implements Icon {
    private String type;
    private int size = 22; 

    public MenuIcon(String type) {
        this.type = type;
    }

    @Override
    public int getIconWidth() { return size; }

    @Override
    public int getIconHeight() { return size; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // BÙA CHÚ TỰ ĐỔI MÀU THEO CHỮ CỦA COMPONENT
        g2d.setColor(c.getForeground()); 
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (type) {
            case "HOME": 
                g2d.drawPolygon(new int[]{x + 11, x + 20, x + 2}, new int[]{y + 4, y + 12, y + 12}, 3);
                g2d.drawRect(x + 5, y + 12, 12, 8);
                break;
            case "CART": 
                g2d.drawPolyline(new int[]{x + 2, x + 4, x + 7, x + 18, x + 16, x + 6, x + 4}, 
                                 new int[]{y + 3, y + 3, y + 13, y + 13, y + 7, y + 7, y + 3}, 7);
                g2d.drawOval(x + 6, y + 15, 3, 3);
                g2d.drawOval(x + 14, y + 15, 3, 3);
                break;
            case "PILL": 
                g2d.drawRoundRect(x + 2, y + 6, 18, 10, 10, 10);
                g2d.drawLine(x + 11, y + 6, x + 11, y + 16);
                break;
            case "BOX": 
                g2d.drawRect(x + 4, y + 6, 14, 12);
                g2d.drawLine(x + 4, y + 6, x + 11, y + 10);
                g2d.drawLine(x + 18, y + 6, x + 11, y + 10);
                g2d.drawLine(x + 11, y + 10, x + 11, y + 18);
                break;
            case "GIFT": 
                g2d.drawRect(x + 5, y + 8, 12, 10);
                g2d.drawRect(x + 3, y + 5, 16, 3);
                g2d.drawLine(x + 11, y + 5, x + 11, y + 18);
                g2d.drawOval(x + 7, y + 2, 4, 3);
                g2d.drawOval(x + 11, y + 2, 4, 3);
                break;
            case "CHART": 
                g2d.drawLine(x + 2, y + 18, x + 20, y + 18); 
                g2d.drawRect(x + 4, y + 10, 4, 8);
                g2d.drawRect(x + 9, y + 5, 4, 13);
                g2d.drawRect(x + 14, y + 12, 4, 6);
                break;
            case "USER": 
                g2d.drawOval(x + 7, y + 2, 8, 8);
                g2d.drawArc(x + 3, y + 12, 16, 16, 0, 180);
                break;
            case "USERS": 
                g2d.drawOval(x + 11, y + 2, 6, 6);
                g2d.drawArc(x + 9, y + 10, 12, 12, 0, 180);
                g2d.setColor(c.getBackground());
                g2d.fillOval(x + 3, y + 4, 8, 8);
                g2d.fillArc(x + -1, y + 12, 16, 16, 0, 180);
                g2d.setColor(c.getForeground());
                g2d.drawOval(x + 3, y + 4, 8, 8);
                g2d.drawArc(x + -1, y + 12, 16, 16, 0, 180);
                break;
            case "HELP": 
                g2d.drawOval(x + 2, y + 2, 18, 18);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2d.drawString("?", x + 8, y + 16);
                break;
            case "LOGOUT": 
                g2d.drawRect(x + 4, y + 3, 10, 16);
                g2d.drawLine(x + 14, y + 11, x + 20, y + 11);
                g2d.drawPolyline(new int[]{x + 17, x + 20, x + 17}, new int[]{y + 8, y + 11, y + 14}, 3);
                break;
                
            // ==========================================
            // CÁC ICON MỚI CHO MÀN HÌNH CHÍNH NÈ ĐẠT ƠI
            // ==========================================
            case "TAB_CHART": // Icon biểu đồ cột đặc ruột cho tab Tổng quan
                g2d.drawLine(x + 2, y + 18, x + 20, y + 18); 
                g2d.fillRect(x + 4, y + 10, 4, 8);
                g2d.fillRect(x + 10, y + 4, 4, 14);
                g2d.fillRect(x + 16, y + 12, 4, 6);
                break;
            case "TAB_DOLLAR": // Icon chữ $ cho tab Đối chiếu
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2d.drawString("$", x + 6, y + 18);
                break;
            case "DOT_FILL": // Chấm tròn đặc cho trạng thái Ca
                g2d.fillOval(x + 6, y + 6, 10, 10);
                break;
            case "CIRCLE_HOLLOW": // Vòng tròn có dấu chấm giữa cho nút Kết ca
                g2d.drawOval(x + 4, y + 4, 14, 14);
                g2d.fillOval(x + 9, y + 9, 4, 4);
                break;
        }
        g2d.dispose();
    }
}