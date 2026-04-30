package Utils;

import javax.swing.Icon;
import java.awt.*;

public class MenuIcon implements Icon {
    private String type;
    private int size = 22;

    public MenuIcon(String type) {
        this.type = type;
    }

    public MenuIcon(String type, int size) {
        this.type = type;
        this.size = size;
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

        if (c != null) {
            g2d.setColor(c.getForeground()); 
        } else {
            g2d.setColor(Color.GRAY);
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
                g2d.drawLine(x + 11, y + 11, x + 4, y + 7);  
                g2d.drawLine(x + 11, y + 11, x + 18, y + 7); 
                g2d.drawLine(x + 11, y + 11, x + 11, y + 19); 
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
            case "TRASH": 
                g2d.drawLine(x + 6, y + 6, x + 18, y + 6); 
                g2d.drawRect(x + 10, y + 3, 4, 3);        
                g2d.drawPolyline(new int[]{x + 7, x + 8, x + 16, x + 17}, new int[]{y + 6, y + 19, y + 19, y + 6}, 4);    
                g2d.drawLine(x + 10, y + 9, x + 10, y + 16);
                g2d.drawLine(x + 14, y + 9, x + 14, y + 16);
                break;
            case "WARNING":
                g2d.drawPolygon(new int[]{x + 11, x + 20, x + 2}, new int[]{y + 3, y + 18, y + 18}, 3);
                g2d.drawLine(x + 11, y + 8, x + 11, y + 13);
                g2d.fillOval(x + 10, y + 15, 2, 2);
                break;
            case "DOCUMENT": 
                g2d.drawRect(x + 5, y + 2, 12, 18); 
                g2d.drawLine(x + 8, y + 6, x + 14, y + 6); 
                g2d.drawLine(x + 8, y + 10, x + 14, y + 10); 
                g2d.drawLine(x + 8, y + 14, x + 12, y + 14); 
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
                g2d.drawLine(x + 15, y + 5, x + 19, y + 5); 
                g2d.drawLine(x + 17, y + 3, x + 17, y + 7); 
                break;
            case "IMPORT": 
                g2d.drawLine(x + 11, y + 2, x + 11, y + 14); 
                g2d.drawLine(x + 7, y + 10, x + 11, y + 14); 
                g2d.drawLine(x + 15, y + 10, x + 11, y + 14); 
                g2d.drawLine(x + 4, y + 18, x + 18, y + 18); 
                g2d.drawLine(x + 4, y + 15, x + 4, y + 18); 
                g2d.drawLine(x + 18, y + 15, x + 18, y + 18); 
                break;                
            case "EXPORT": 
                g2d.drawLine(x + 11, y + 14, x + 11, y + 2); 
                g2d.drawLine(x + 7, y + 6, x + 11, y + 2); 
                g2d.drawLine(x + 15, y + 6, x + 11, y + 2); 
                g2d.drawLine(x + 4, y + 18, x + 18, y + 18); 
                g2d.drawLine(x + 4, y + 15, x + 4, y + 18); 
                g2d.drawLine(x + 18, y + 15, x + 18, y + 18); 
                break;
            case "CANCEL": 
                g2d.drawOval(x + 3, y + 3, 16, 16); 
                g2d.drawLine(x + 8, y + 8, x + 14, y + 14); 
                g2d.drawLine(x + 14, y + 8, x + 8, y + 14); 
                break;             
            case "SHIFT_MORNING": 
                g2d.drawOval(x + 7, y + 7, 8, 8);
                g2d.drawLine(x + 11, y + 2, x + 11, y + 5);  
                g2d.drawLine(x + 11, y + 17, x + 11, y + 20); 
                g2d.drawLine(x + 2, y + 11, x + 5, y + 11);   
                g2d.drawLine(x + 17, y + 11, x + 20, y + 11); 
                g2d.drawLine(x + 4, y + 4, x + 6, y + 6);     
                g2d.drawLine(x + 16, y + 4, x + 18, y + 6);   
                g2d.drawLine(x + 4, y + 18, x + 6, y + 16);   
                g2d.drawLine(x + 16, y + 18, x + 18, y + 16); 
                break;
            case "SHIFT_AFTERNOON": 
                g2d.drawLine(x + 2, y + 14, x + 20, y + 14);
                g2d.drawArc(x + 6, y + 7, 10, 10, 0, 180);
                g2d.drawLine(x + 11, y + 3, x + 11, y + 6);   
                g2d.drawLine(x + 5, y + 5, x + 7, y + 8);     
                g2d.drawLine(x + 17, y + 5, x + 15, y + 8);   
                g2d.drawLine(x + 2, y + 9, x + 5, y + 11);    
                g2d.drawLine(x + 20, y + 9, x + 17, y + 11);  
                g2d.drawArc(x + 2, y + 15, 6, 4, 180, -180);
                g2d.drawArc(x + 8, y + 15, 6, 4, 180, -180);
                g2d.drawArc(x + 14, y + 15, 6, 4, 180, -180);
                break;
            case "SHIFT_NIGHT": 
                g2d.drawArc(x + 5, y + 3, 14, 16, 90, 180);   
                g2d.drawArc(x + 8, y + 5, 10, 12, 90, 180);   
                g2d.fillOval(x + 15, y + 3, 2, 2);
                g2d.fillOval(x + 17, y + 8, 2, 2);
                g2d.fillOval(x + 14, y + 12, 2, 2);
                break;
            case "LIST": 
                g2d.drawLine(x + 8, y + 6, x + 18, y + 6);
                g2d.drawOval(x + 3, y + 5, 2, 2);
                g2d.drawLine(x + 8, y + 11, x + 18, y + 11);
                g2d.drawOval(x + 3, y + 10, 2, 2);
                g2d.drawLine(x + 8, y + 16, x + 18, y + 16);
                g2d.drawOval(x + 3, y + 15, 2, 2);
                break;
            case "CLOCK": 
            case "TIME":
                g2d.drawOval(x + 3, y + 3, 16, 16);
                g2d.drawLine(x + 11, y + 6, x + 11, y + 11);
                g2d.drawLine(x + 11, y + 11, x + 15, y + 11);
                break;
            case "COSMETIC":
                g2d.fillRoundRect(8, 10, 8, 10, 2, 2); 
                g2d.fillRect(10, 6, 4, 4);             
                g2d.drawLine(7, 10, 17, 10);           
                break;
            case "LEAF":
                g2d.fillOval(7, 6, 10, 12);
                g2d.drawArc(7, 6, 10, 12, 45, 90); 
                g2d.drawLine(12, 18, 12, 20);          
                break;
            case "MEDICAL_TOOL":
                g2d.drawRoundRect(5, 7, 14, 10, 2, 2); 
                g2d.fillRect(11, 9, 2, 6);             
                g2d.fillRect(9, 11, 6, 2);             
                break;
            case "CALENDAR":
                g2d.drawRect(x + 3, y + 5, 16, 14); 
                g2d.drawLine(x + 3, y + 9, x + 19, y + 9); 
                g2d.drawLine(x + 6, y + 3, x + 6, y + 6); 
                g2d.drawLine(x + 16, y + 3, x + 16, y + 6);
                g2d.drawRect(x + 7, y + 12, 2, 2); 
                g2d.drawRect(x + 11, y + 12, 2, 2);
                break;
            case "CHEVRON_LEFT":
                g2d.drawPolyline(new int[]{x + 13, x + 8, x + 13}, new int[]{y + 6, y + 11, y + 16}, 3);
                break;
            case "CHEVRON_RIGHT":
                g2d.drawPolyline(new int[]{x + 9, x + 14, x + 9}, new int[]{y + 6, y + 11, y + 16}, 3);
                break;
            case "CORRECT": // Icon dấu Tick (V) báo thành công
                g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawPolyline(new int[]{x + 5, x + 10, x + 18}, new int[]{y + 12, y + 16, y + 6}, 3);
                break;

            case "STOP": // Icon vuông đặc (Dùng cho nút Kết ca / Dừng)
                g2d.fillRoundRect(x + 5, y + 5, 12, 12, 3, 3);
                break;

            case "LOCK": // Ổ khóa (Dùng cho chức năng chỉ dành cho Quản lý)
                g2d.drawRoundRect(x + 4, y + 10, 14, 10, 3, 3); // Thân ổ khóa
                g2d.drawArc(x + 7, y + 3, 8, 10, 0, 180);       // Cung tròn trên
                g2d.drawLine(x + 7, y + 8, x + 7, y + 10);      // Nối cung vào thân trái
                g2d.drawLine(x + 15, y + 8, x + 15, y + 10);    // Nối cung vào thân phải
                g2d.fillOval(x + 10, y + 14, 2, 2);             // Lỗ khóa
                break;

            case "RETURN": // Mũi tên hoàn trả (↩ Đổi / Trả hàng)
                g2d.drawArc(x + 3, y + 5, 14, 10, 0, 180);      // Cung trên
                g2d.drawLine(x + 3, y + 10, x + 3, y + 15);     // Đuôi đi xuống
                g2d.drawPolyline(
                    new int[]{x + 1, x + 4, x + 7},
                    new int[]{y + 13, y + 16, y + 13},
                    3  // Đầu mũi tên
                );
                break;

//            case "CALENDAR": // Lịch (Dùng cho bộ lọc năm / section title ngày)
//                g2d.drawRoundRect(x + 2, y + 4, 18, 15, 3, 3);  // Khung lịch
//                g2d.drawLine(x + 2, y + 8, x + 20, y + 8);      // Đường kẻ ngang tiêu đề
//                g2d.drawLine(x + 7,  y + 2, x + 7,  y + 6);     // Núm trái
//                g2d.drawLine(x + 15, y + 2, x + 15, y + 6);     // Núm phải
//                // 6 chấm ô ngày (2 hàng × 3 cột)
//                g2d.fillOval(x + 5,  y + 11, 2, 2);
//                g2d.fillOval(x + 10, y + 11, 2, 2);
//                g2d.fillOval(x + 15, y + 11, 2, 2);
//                g2d.fillOval(x + 5,  y + 15, 2, 2);
//                g2d.fillOval(x + 10, y + 15, 2, 2);
//                g2d.fillOval(x + 15, y + 15, 2, 2);
//                break;

            case "TROPHY": // Cúp chiến thắng (Top sản phẩm bán chạy)
                g2d.drawArc(x + 5, y + 2, 12, 12, 0, 180);      // Miệng cúp
                g2d.drawLine(x + 5,  y + 8,  x + 5,  y + 12);  // Thân trái
                g2d.drawLine(x + 17, y + 8,  x + 17, y + 12);  // Thân phải
                g2d.drawArc(x + 8, y + 10, 6, 6, 180, 180);    // Đáy cúp tròn
                g2d.drawLine(x + 11, y + 16, x + 11, y + 18);  // Cổ đế
                g2d.drawLine(x + 7,  y + 18, x + 15, y + 18);  // Đế
                g2d.drawArc(x + 1,  y + 4, 6, 6, 90,  180);   // Tai trái
                g2d.drawArc(x + 15, y + 4, 6, 6, 270, 180);   // Tai phải
                break;

            case "INFO_CIRCLE": // Vòng tròn + chữ i (Thông tin / Bước hướng dẫn)
                g2d.drawOval(x + 2, y + 2, 18, 18);                          // Vòng tròn
                g2d.fillOval(x + 10, y + 6, 2, 2);                           // Chấm trên chữ i
                g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(x + 11, y + 10, x + 11, y + 16);               // Thân chữ i
                break;

            case "BELL": // Chuông thông báo
                g2d.drawLine(x + 11, y + 1, x + 11, y + 4);                 // Dây treo
                g2d.drawArc(x + 4, y + 3, 14, 12, 0, 180);                  // Mái chuông
                g2d.drawLine(x + 4,  y + 9,  x + 4,  y + 15);              // Thân trái
                g2d.drawLine(x + 18, y + 9,  x + 18, y + 15);              // Thân phải
                g2d.drawLine(x + 2,  y + 15, x + 20, y + 15);              // Miệng chuông
                g2d.drawArc(x + 8, y + 15, 6, 4, 180, 180);                 // Núm lắc
                break;
            case "QUA_TANG":
                // 1. Thân hộp quà
                g2d.drawRect(x + 5, y + 8, 12, 10);
                // 2. Nắp hộp nhô ra 2 bên
                g2d.drawRect(x + 3, y + 5, 16, 3);
                // 3. Dây ruy-băng buộc dọc
                g2d.drawLine(x + 11, y + 8, x + 11, y + 18);
                // 4. Dây nơ bên trái
                g2d.drawOval(x + 6, y + 1, 5, 5);
                // 5. Dây nơ bên phải
                g2d.drawOval(x + 11, y + 1, 5, 5);
                // 6. Nút thắt nơ tròn ở chính giữa
                g2d.fillOval(x + 9, y + 4, 4, 3);
                break;
        }
    }
}