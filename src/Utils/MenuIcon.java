package Utils;

import javax.swing.Icon;
import java.awt.*;

public class MenuIcon implements Icon {
    private String type;
    private int size = 22;

    public MenuIcon(String type) {
        this.type = type;
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // BÙA CHÚ TỰ ĐỔI MÀU THEO CHỮ CỦA COMPONENT
        // Tự động lấy màu theo Foreground của Component gắn icon

        if (c != null) {
            g2d.setColor(c.getForeground()); 
        } else {
            g2d.setColor(Color.GRAY); // Lỡ c bị null thì vẽ màu xám
        }
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (type) {

            case "ADD":
                g2d.drawLine(x + 5, y + 11, x + 17, y + 11);
                g2d.drawLine(x + 11, y + 5, x + 11, y + 17);
                break;
            case "HOME": 
                g2d.drawPolygon(new int[]{x + 11, x + 20, x + 2}, new int[]{y + 4, y + 12, y + 12}, 3);
                g2d.drawRect(x + 5, y + 12, 12, 8);
                break;

            case "CART":
                g2d.drawPolyline(
                        new int[]{x + 2, x + 4, x + 7, x + 18, x + 16, x + 6, x + 4},
                        new int[]{y + 3, y + 3, y + 13, y + 13, y + 7, y + 7, y + 3},
                        7
                );
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
                g2d.fillArc(x - 1, y + 12, 16, 16, 0, 180);
                g2d.setColor(c.getForeground());
                g2d.drawOval(x + 3, y + 4, 8, 8);
                g2d.drawArc(x - 1, y + 12, 16, 16, 0, 180);
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

            case "EYE":
                g2d.drawOval(x + 2, y + 7, 18, 8);
                g2d.drawOval(x + 8, y + 8, 6, 6);
                g2d.fillOval(x + 10, y + 10, 2, 2);
                break;

            case "EYE_HIDE":
                g2d.drawOval(x + 2, y + 7, 18, 8);
                g2d.drawOval(x + 8, y + 8, 6, 6);
                g2d.drawLine(x + 3, y + 4, x + 19, y + 18);
                break;

            case "PACKAGE":
                int[] hexX = {x + 11, x + 18, x + 18, x + 11, x + 4, x + 4};
                int[] hexY = {y + 3, y + 7, y + 15, y + 19, y + 15, y + 7};
                g2d.drawPolygon(hexX, hexY, 6);
                // 2. Vẽ 3 đường thẳng nối từ tâm ra 3 góc (tạo khối 3D chữ Y)
                g2d.drawLine(x + 11, y + 11, x + 4, y + 7);   // Tâm lên góc trên-trái
                g2d.drawLine(x + 11, y + 11, x + 18, y + 7);  // Tâm lên góc trên-phải
                g2d.drawLine(x + 11, y + 11, x + 11, y + 19); // Tâm thẳng xuống đáy
                // 3. Vẽ đường nếp gấp / vạch băng keo vắt ngang trên nắp hộp
                g2d.drawLine(x + 7, y + 5, x + 15, y + 9);
                break;

            case "TAB_CHART":
                g2d.drawLine(x + 2, y + 18, x + 20, y + 18);
                g2d.fillRect(x + 4, y + 10, 4, 8);
                g2d.fillRect(x + 10, y + 4, 4, 14);
                g2d.fillRect(x + 16, y + 12, 4, 6);
                break;

            case "TAB_DOLLAR":
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                g2d.drawString("$", x + 6, y + 18);
                break;

            case "DOT_FILL":
                g2d.fillOval(x + 6, y + 6, 10, 10);
                break;

            case "CIRCLE_HOLLOW":
                g2d.drawOval(x + 4, y + 4, 14, 14);
                g2d.fillOval(x + 9, y + 9, 4, 4);
                break;
            case "REFRESH": 
                g2d.drawArc(x + 4, y + 4, 14, 14, 90, -270);
                g2d.drawPolyline(new int[]{x + 1, x + 4, x + 7}, new int[]{y + 14, y + 11, y + 14}, 3);
                break;
            case "SEARCH":
                g2d.drawOval(x + 5, y + 5, 9, 9);
                g2d.drawLine(x + 12, y + 12, x + 18, y + 18);
                break;
            case "TRASH": // Icon thùng rác (Dùng cho nút Xóa/Delete)
                // Nắp thùng rác
                g2d.drawLine(x + 6, y + 6, x + 18, y + 6); // Đường ngang
                g2d.drawRect(x + 10, y + 3, 4, 3);         // Tay cầm nắp
                // Thân thùng rác (hơi vát chéo xuống)
                g2d.drawPolyline(new int[]{x + 7, x + 8, x + 16, x + 17}, new int[]{y + 6, y + 19, y + 19, y + 6}, 4);    
                // Các sọc dọc trên thân
                g2d.drawLine(x + 10, y + 9, x + 10, y + 16);
                g2d.drawLine(x + 14, y + 9, x + 14, y + 16);
                break;

            case "WARNING":
                g2d.drawPolygon(new int[]{x + 11, x + 20, x + 2}, new int[]{y + 3, y + 18, y + 18}, 3);
                g2d.drawLine(x + 11, y + 8, x + 11, y + 13);
                g2d.fillOval(x + 10, y + 15, 2, 2);
                break;
                
            case "DOCUMENT": // Icon tờ giấy (Dành cho nút Lưu nháp)
                g2d.drawRect(x + 5, y + 2, 12, 18); // Khung viền tờ giấy
                g2d.drawLine(x + 8, y + 6, x + 14, y + 6); // Dòng 1
                g2d.drawLine(x + 8, y + 10, x + 14, y + 10); // Dòng 2
                g2d.drawLine(x + 8, y + 14, x + 12, y + 14); // Dòng 3 (ngắn hơn)
                break;

            case "CLOSE":
                g2d.drawLine(x + 6, y + 6, x + 16, y + 16);
                g2d.drawLine(x + 16, y + 6, x + 6, y + 16);
                break;

            case "CHECK_CIRCLE":
                g2d.drawOval(x + 2, y + 2, 18, 18);
                g2d.drawPolyline(new int[]{x + 6, x + 10, x + 16}, new int[]{y + 11, y + 15, y + 7}, 3);
                break;

            case "PRINT":
                g2d.drawRect(x + 6, y + 3, 10, 5);
                g2d.drawRoundRect(x + 3, y + 8, 16, 8, 4, 4);
                g2d.drawLine(x + 5, y + 12, x + 17, y + 12);
                g2d.drawRect(x + 6, y + 12, 10, 7);
                g2d.drawLine(x + 8, y + 15, x + 14, y + 15);
                g2d.drawLine(x + 8, y + 17, x + 14, y + 17);
                break;

            case "EDIT":
                g2d.drawRect(x + 4, y + 14, 12, 3);
                g2d.drawLine(x + 6, y + 4, x + 15, y + 13);
                break;

            case "PHONE":
                g2d.drawRoundRect(x + 5, y + 2, 10, 16, 4, 4);
                g2d.drawLine(x + 8, y + 15, x + 12, y + 15);
                break;

            case "MAIL":
                g2d.drawRect(x + 2, y + 5, 16, 10);
                g2d.drawLine(x + 2, y + 5, x + 10, y + 10);
                g2d.drawLine(x + 18, y + 5, x + 10, y + 10);
                break;

            case "LOCATION":
                g2d.drawOval(x + 5, y + 2, 10, 10);
                g2d.drawLine(x + 10, y + 12, x + 10, y + 18);
                g2d.drawLine(x + 7, y + 18, x + 13, y + 18);
                break;

            case "SAVE":
                g2d.drawRect(x + 3, y + 3, 14, 14);
                g2d.drawRect(x + 6, y + 3, 8, 5);
                g2d.drawLine(x + 5, y + 12, x + 15, y + 12);
                g2d.drawLine(x + 5, y + 15, x + 15, y + 15);
                break;

            case "USER_ADD":
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawOval(x + 5, y + 2, 8, 8);
                g2d.drawArc(x + 2, y + 11, 14, 8, 0, 180);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawLine(x + 15, y + 5, x + 19, y + 5); // Ngang
                g2d.drawLine(x + 17, y + 3, x + 17, y + 7); // Dọc
                break;
            case "IMPORT": // Icon Nhập Excel (Mũi tên hướng xuống khay)
                g2d.drawLine(x + 11, y + 2, x + 11, y + 14); // Trục mũi tên dọc
                g2d.drawLine(x + 7, y + 10, x + 11, y + 14); // Cánh mũi tên trái
                g2d.drawLine(x + 15, y + 10, x + 11, y + 14); // Cánh mũi tên phải
                g2d.drawLine(x + 4, y + 18, x + 18, y + 18); // Khay ngang
                g2d.drawLine(x + 4, y + 15, x + 4, y + 18); // Mép khay trái
                g2d.drawLine(x + 18, y + 15, x + 18, y + 18); // Mép khay phải
                break;                
            case "EXPORT": // Icon Xuất Excel (Mũi tên hướng lên từ khay)
                g2d.drawLine(x + 11, y + 14, x + 11, y + 2); // Trục mũi tên dọc
                g2d.drawLine(x + 7, y + 6, x + 11, y + 2); // Cánh mũi tên trái
                g2d.drawLine(x + 15, y + 6, x + 11, y + 2); // Cánh mũi tên phải
                g2d.drawLine(x + 4, y + 18, x + 18, y + 18); // Khay ngang
                g2d.drawLine(x + 4, y + 15, x + 4, y + 18); // Mép khay trái
                g2d.drawLine(x + 18, y + 15, x + 18, y + 18); // Mép khay phải
                break;
            case "CANCEL": // Icon Hủy (Vòng tròn có dấu X bên trong)
                g2d.drawOval(x + 3, y + 3, 16, 16); // Vòng tròn ngoài
                g2d.drawLine(x + 8, y + 8, x + 14, y + 14); // Đường chéo 1
                g2d.drawLine(x + 14, y + 8, x + 8, y + 14); // Đường chéo 2
                break;             
            case "SHIFT_MORNING": // ☀ Mặt trời cho Ca Sáng
                // Vòng tròn giữa
                g2d.drawOval(x + 7, y + 7, 8, 8);
                // 8 tia sáng xung quanh
                g2d.drawLine(x + 11, y + 2, x + 11, y + 5);  // trên
                g2d.drawLine(x + 11, y + 17, x + 11, y + 20); // dưới
                g2d.drawLine(x + 2, y + 11, x + 5, y + 11);   // trái
                g2d.drawLine(x + 17, y + 11, x + 20, y + 11); // phải
                g2d.drawLine(x + 4, y + 4, x + 6, y + 6);     // góc TT
                g2d.drawLine(x + 16, y + 4, x + 18, y + 6);   // góc TP
                g2d.drawLine(x + 4, y + 18, x + 6, y + 16);   // góc DT
                g2d.drawLine(x + 16, y + 18, x + 18, y + 16); // góc DP
                break;
            case "SHIFT_AFTERNOON": // 🌅 Hoàng hôn cho Ca Chiều
                // Đường chân trời
                g2d.drawLine(x + 2, y + 14, x + 20, y + 14);
                // Nửa mặt trời nổi lên
                g2d.drawArc(x + 6, y + 7, 10, 10, 0, 180);
                // 5 tia sáng phía trên
                g2d.drawLine(x + 11, y + 3, x + 11, y + 6);   // tia giữa
                g2d.drawLine(x + 5, y + 5, x + 7, y + 8);     // tia trái
                g2d.drawLine(x + 17, y + 5, x + 15, y + 8);   // tia phải
                g2d.drawLine(x + 2, y + 9, x + 5, y + 11);    // tia trái xa
                g2d.drawLine(x + 20, y + 9, x + 17, y + 11);  // tia phải xa
                // Sóng nước phía dưới
                g2d.drawArc(x + 2, y + 15, 6, 4, 180, -180);
                g2d.drawArc(x + 8, y + 15, 6, 4, 180, -180);
                g2d.drawArc(x + 14, y + 15, 6, 4, 180, -180);
                break;
            case "SHIFT_NIGHT": // 🌙 Trăng lưỡi liềm cho Ca Tối
                // Vẽ trăng lưỡi liềm bằng cách vẽ 2 arc
                g2d.drawArc(x + 5, y + 3, 14, 16, 90, 180);   // Cung trăng ngoài
                g2d.drawArc(x + 8, y + 5, 10, 12, 90, 180);   // Cung trong (tạo lưỡi liềm)
                // Vài ngôi sao nhỏ
                g2d.fillOval(x + 15, y + 3, 2, 2);
                g2d.fillOval(x + 17, y + 8, 2, 2);
                g2d.fillOval(x + 14, y + 12, 2, 2);
                break;
            case "LIST": // Icon Danh Sách (3 gạch ngang có dấu chấm)
                // Dòng 1
                g2d.drawLine(x + 8, y + 6, x + 18, y + 6);
                g2d.drawOval(x + 3, y + 5, 2, 2);
                // Dòng 2
                g2d.drawLine(x + 8, y + 11, x + 18, y + 11);
                g2d.drawOval(x + 3, y + 10, 2, 2);
                // Dòng 3
                g2d.drawLine(x + 8, y + 16, x + 18, y + 16);
                g2d.drawOval(x + 3, y + 15, 2, 2);
                break;
            case "CLOCK": // Icon Đồng hồ (Dành cho thẻ Đang xử lý)
                g2d.drawOval(x + 3, y + 3, 16, 16);
                g2d.drawLine(x + 11, y + 6, x + 11, y + 11);
                g2d.drawLine(x + 11, y + 11, x + 15, y + 11);
                break;
            case "COSMETIC":
                // Vẽ hình chai mỹ phẩm đơn giản
                g2d.fillRoundRect(8, 10, 8, 10, 2, 2); // Thân chai
                g2d.fillRect(10, 6, 4, 4);             // Nắp chai
                g2d.drawLine(7, 10, 17, 10);           // Viền nắp
                break;

            case "LEAF":
                // Vẽ hình chiếc lá (TPCN/Thảo dược)
                g2d.fillOval(7, 6, 10, 12);
                g2d.drawArc(7, 6, 10, 12, 45, 90); 
                g2d.drawLine(12, 18, 12, 20);          // Cuống lá
                break;

            case "MEDICAL_TOOL":
                // Vẽ hình dấu thập y tế (Vật tư)
                g2d.drawRoundRect(5, 7, 14, 10, 2, 2); 
                g2d.fillRect(11, 9, 2, 6);             // Thập dọc
                g2d.fillRect(9, 11, 6, 2);             // Thập ngang
                break;

            
        }

        g2d.dispose();
    }
}