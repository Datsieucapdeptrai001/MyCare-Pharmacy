package Components;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class ModernScrollBarUI extends BasicScrollBarUI {
    
    // Xóa bỏ nút mũi tên lên/xuống mặc định
    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createZeroButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createZeroButton();
    }

    private JButton createZeroButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        return button;
    }

    // Vẽ lại phần rãnh trượt (Track) - Để nền xám cực nhạt hoặc trong suốt
    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.setColor(new Color(245, 246, 250)); // Màu nền rãnh trượt
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
    }

    // Vẽ lại phần thanh cầm (Thumb) - Bo góc và có màu xám đậm hơn giống ảnh
    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }
        
        Graphics2D g2 = (Graphics2D) g.create();
        // Bật khử răng cưa để góc bo tròn được mượt mà
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Màu xám của thanh cuộn (bạn có thể đổi mã màu tùy ý)
        g2.setColor(new Color(180, 185, 190)); 
        
        // Vẽ thanh cuộn thụt vào một chút (x+2, width-4) để tạo khoảng không gian 2 bên
        g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 10, 10);
        
        g2.dispose();
    }
}