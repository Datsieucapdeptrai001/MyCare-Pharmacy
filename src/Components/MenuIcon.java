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
        // Tự động lấy màu theo Foreground của Component gắn icon

        g2d.setColor(c.getForeground()); 
        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (type) {

            case "ADD": // Dấu cộng (Thêm mới/Tạo mới)
                g2d.drawLine(x + 5, y + 11, x + 17, y + 11); // Thanh ngang
                g2d.drawLine(x + 11, y + 5, x + 11, y + 17); // Thanh dọc
                break;

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
            case "EYE": // Icon con mắt (Thường dùng để Xem/Hiển thị)
                // Vẽ viền ngoài của mắt (hình ellipse dẹt)
                g2d.drawOval(x + 2, y + 7, 18, 8);
                // Vẽ tròng đen (vòng tròn ở giữa)
                g2d.drawOval(x + 8, y + 8, 6, 6);
                // Vẽ đồng tử (chấm đặc nhỏ xíu ở giữa tròng đen)
                g2d.fillOval(x + 10, y + 10, 2, 2);
                break;
            case "EYE_HIDE": // Icon con mắt bị gạch chéo (Thường dùng để Ẩn/Che)
                g2d.drawOval(x + 2, y + 7, 18, 8);
                g2d.drawOval(x + 8, y + 8, 6, 6);
                // Dùng một đường thẳng chéo cắt ngang qua con mắt
                g2d.drawLine(x + 3, y + 4, x + 19, y + 18);
                break;
            case "PACKAGE": // Icon hộp sản phẩm 3D (dành cho "Thêm sản phẩm")
                // 1. Vẽ viền ngoài hình lục giác (Hexagon)
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

            case "REFRESH": 
                g2d.drawArc(x + 4, y + 4, 14, 14, 90, -270);
                g2d.drawPolyline(new int[]{x + 1, x + 4, x + 7}, new int[]{y + 14, y + 11, y + 14}, 3);

                break;
            case "SEARCH":
                // Đã fix: Cộng thêm tọa độ x, y và căn chỉnh lại kích thước cho đẹp
                g2d.drawOval(x + 5, y + 5, 9, 9); // Vòng tròn kính lúp
                g2d.drawLine(x + 12, y + 12, x + 18, y + 18); // Cán kính lúp
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
            case "WARNING": // Biểu tượng cảnh báo (Tam giác có dấu chấm than)
                g2d.drawPolygon(new int[]{x + 11, x + 20, x + 2}, new int[]{y + 3, y + 18, y + 18}, 3);
                g2d.drawLine(x + 11, y + 8, x + 11, y + 13); // Thân dấu !
                g2d.fillOval(x + 10, y + 15, 2, 2); // Chấm của dấu !
                break;
                
            
                
            case "DOCUMENT": // Icon tờ giấy (Dành cho nút Lưu nháp)
                g2d.drawRect(x + 5, y + 2, 12, 18); // Khung viền tờ giấy
                g2d.drawLine(x + 8, y + 6, x + 14, y + 6); // Dòng 1
                g2d.drawLine(x + 8, y + 10, x + 14, y + 10); // Dòng 2
                g2d.drawLine(x + 8, y + 14, x + 12, y + 14); // Dòng 3 (ngắn hơn)
                break;
            case "CLOSE": // Dấu X (Gỡ liên kết / Đóng)
                g2d.drawLine(x + 6, y + 6, x + 16, y + 16); // Chéo trái sang phải
                g2d.drawLine(x + 16, y + 6, x + 6, y + 16); // Chéo phải sang trái
                break;
            case "CHECK_CIRCLE": // Icon vòng tròn có dấu tích (Dành cho nút Thanh toán)
                g2d.drawOval(x + 2, y + 2, 18, 18); // Vòng tròn ngoài
                g2d.drawPolyline(new int[]{x + 6, x + 10, x + 16}, new int[]{y + 11, y + 15, y + 7}, 3); // Dấu check bên trong
                break;
            case "PRINT": // Icon Máy in
                // 1. Khay giấy vào (Phía trên)
                g2d.drawRect(x + 6, y + 3, 10, 5);
                
                // 2. Thân máy in (Bo góc cho mềm mại)
                g2d.drawRoundRect(x + 3, y + 8, 16, 8, 4, 4);
                
                // 3. Khe giấy ra (Vẽ một đường ngang trên thân máy)
                g2d.drawLine(x + 5, y + 12, x + 17, y + 12);
                
                // 4. Tờ giấy đang in ra (Phía dưới)
                g2d.drawRect(x + 6, y + 12, 10, 7);
                
                // 5. Các nét mực trên tờ giấy
                g2d.drawLine(x + 8, y + 15, x + 14, y + 15);
                g2d.drawLine(x + 8, y + 17, x + 14, y + 17);
                break;
            case "EDIT": 
                g2d.drawRect(x + 4, y + 14, 12, 3); // Đế
                g2d.drawLine(x + 6, y + 4, x + 15, y + 13); // Ngòi
                break;
            case "PHONE": // Icon Điện thoại
                g2d.drawRoundRect(x + 5, y + 2, 10, 16, 4, 4);
                g2d.drawLine(x + 8, y + 15, x + 12, y + 15);
                break;
            case "MAIL": // Icon Bức thư
                g2d.drawRect(x + 2, y + 5, 16, 10);
                g2d.drawLine(x + 2, y + 5, x + 10, y + 10);
                g2d.drawLine(x + 18, y + 5, x + 10, y + 10);
                break;
            case "LOCATION": // Icon Vị trí
                g2d.drawOval(x + 5, y + 2, 10, 10);
                g2d.drawLine(x + 10, y + 12, x + 10, y + 18);
                g2d.drawLine(x + 7, y + 18, x + 13, y + 18);
                break;
            case "SAVE": // Vẽ biểu tượng lưu trữ (đĩa mềm mini)
                g2d.drawRect(x + 3, y + 3, 14, 14);
                g2d.drawRect(x + 6, y + 3, 8, 5);
                g2d.drawLine(x + 5, y + 12, x + 15, y + 12);
                g2d.drawLine(x + 5, y + 15, x + 15, y + 15);
                break;
            case "USER_ADD": // Vẽ biểu tượng thêm người dùng
                g2d.setStroke(new BasicStroke(2.0f));
                // Vẽ đầu
                g2d.drawOval(x + 5, y + 2, 8, 8);
                // Vẽ thân (cung tròn)
                g2d.drawArc(x + 2, y + 11, 14, 8, 0, 180);
                // Vẽ dấu cộng nhỏ bên cạnh
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawLine(x + 15, y + 5, x + 19, y + 5); // Ngang
                g2d.drawLine(x + 17, y + 3, x + 17, y + 7); // Dọc
                break;
            case "CORRECT": // Dấu tích V (Thành công/Đã liên kết)
                g2d.setColor(Color.WHITE); // <--- THÊM DÒNG NÀY ĐỂ ÉP CỨNG MÀU TRẮNG
                g2d.drawLine(x + 5, y + 12, x + 9, y + 16); // Nét ngắn
                g2d.drawLine(x + 9, y + 16, x + 17, y + 6); // Nét dài
                break;
             // 5 ICON MỚI THÊM VÀO ĐỂ PHỤC VỤ MÀN HÌNH SẢN PHẨM
            case "CANCEL": // Dấu X (Dành cho nút Hủy)
                g2d.drawLine(x+6, y+6, x+16, y+16);
                g2d.drawLine(x+16, y+6, x+6, y+16);
                break;
            case "EXPORT": // Mũi tên trỏ xuống khay (Xuất Excel)
                g2d.drawLine(x+11, y+4, x+11, y+14); 
                g2d.drawLine(x+7, y+10, x+11, y+14); 
                g2d.drawLine(x+15, y+10, x+11, y+14); 
                g2d.drawLine(x+5, y+18, x+17, y+18); 
                break;
            case "IMPORT": // Mũi tên trỏ lên khỏi khay (Nhập Excel)
                g2d.drawLine(x+11, y+14, x+11, y+4); 
                g2d.drawLine(x+7, y+8, x+11, y+4); 
                g2d.drawLine(x+15, y+8, x+11, y+4); 
                g2d.drawLine(x+5, y+18, x+17, y+18); 
                break;
            case "LOCK": // ICON MỚI THÊM CHO TRƯỜNG MẬT KHẨU
                g2d.drawRoundRect(x + 4, y + 8, size - 8, size / 2 - 2, 2, 2); 
                g2d.drawArc(x + size / 2 - 4, y + 2, 8, 8, 0, 180); 
                break;
        }
        g2d.dispose();
    }
}