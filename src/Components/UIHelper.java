package Components;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

public class UIHelper {

    public static void setRoundedCorners(JComponent c, int cornerRadius) {
        c.setOpaque(false); 
        c.setBorder(new RoundedBorder(cornerRadius, c.getBackground(), c.getBorder()));
    }

    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color bgColor;
        private final javax.swing.border.Border innerBorder;

        public RoundedBorder(int radius, Color bgColor, javax.swing.border.Border innerBorder) {
            this.radius = radius;
            this.bgColor = bgColor;
            this.innerBorder = innerBorder;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.setColor(bgColor != null ? bgColor : c.getBackground());
            g2d.fillRoundRect(x, y, width - 1, height - 1, radius, radius);

            g2d.setColor(Color.decode("#E0E0E0"));
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);

            if (innerBorder != null) {
                innerBorder.paintBorder(c, g2d, x, y, width, height);
            }
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) { return new Insets(0, 0, 0, 0); }
        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = 0;
            return insets;
        }
    }
}