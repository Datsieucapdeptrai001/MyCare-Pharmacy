package Utils;

import javax.swing.ToolTipManager;

public class TooltipConfig {

    private TooltipConfig() {
    }

    public static void tatTatCaTooltip() {
        ToolTipManager.sharedInstance().setEnabled(false);

        ToolTipManager.sharedInstance().setInitialDelay(Integer.MAX_VALUE);
        ToolTipManager.sharedInstance().setDismissDelay(0);
        ToolTipManager.sharedInstance().setReshowDelay(Integer.MAX_VALUE);
    }
}