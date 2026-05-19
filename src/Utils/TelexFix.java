package Utils;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.im.InputContext;
import java.util.Locale;

public class TelexFix {

    private TelexFix() {
    }

    public static void setupGlobalTelexFix() {
        try {
            Locale.setDefault(new Locale("vi", "VN"));
            UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("TextArea.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("TextPane.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("EditorPane.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 14));
        } catch (Exception ignored) {
        }
    }

    public static void applyDeep(Component root) {
        if (root == null) {
            return;
        }

        applyToComponent(root);

        if (root instanceof Container) {
            Component[] children = ((Container) root).getComponents();

            for (Component child : children) {
                applyDeep(child);
            }
        }

        if (root instanceof JMenu) {
            for (Component child : ((JMenu) root).getMenuComponents()) {
                applyDeep(child);
            }
        }
    }

    public static void applyWindow(Window window) {
        if (window == null) {
            return;
        }

        applyDeep(window);

        try {
            InputContext inputContext = window.getInputContext();

            if (inputContext != null) {
                inputContext.selectInputMethod(new Locale("vi", "VN"));
            }
        } catch (Exception ignored) {
        }
    }

    public static void applyToTextComponent(JTextComponent textComponent) {
        if (textComponent == null) {
            return;
        }

        textComponent.enableInputMethods(true);
        textComponent.setLocale(new Locale("vi", "VN"));
        textComponent.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        if (textComponent.getFont() == null || !"Segoe UI".equalsIgnoreCase(textComponent.getFont().getFamily())) {
            textComponent.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }

        try {
            InputContext inputContext = textComponent.getInputContext();

            if (inputContext != null) {
                inputContext.selectInputMethod(new Locale("vi", "VN"));
            }
        } catch (Exception ignored) {
        }
    }

    public static void applyToComboBox(JComboBox<?> comboBox) {
        if (comboBox == null) {
            return;
        }

        comboBox.enableInputMethods(true);
        comboBox.setLocale(new Locale("vi", "VN"));
        comboBox.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        Component editor = comboBox.getEditor() != null
                ? comboBox.getEditor().getEditorComponent()
                : null;

        if (editor instanceof JTextComponent) {
            applyToTextComponent((JTextComponent) editor);
        }
    }

    private static void applyToComponent(Component component) {
        try {
            component.enableInputMethods(true);
            component.setLocale(new Locale("vi", "VN"));
            component.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        } catch (Exception ignored) {
        }

        if (component instanceof JTextComponent) {
            applyToTextComponent((JTextComponent) component);
        }

        if (component instanceof JComboBox) {
            applyToComboBox((JComboBox<?>) component);
        }

        if (component instanceof JTable) {
            JTable table = (JTable) component;
            table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            table.setLocale(new Locale("vi", "VN"));
        }

        if (component instanceof JLabel) {
            JLabel label = (JLabel) component;

            if (label.getFont() == null) {
                label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            }
        }

        if (component instanceof JButton) {
            JButton button = (JButton) component;

            if (button.getFont() == null) {
                button.setFont(new Font("Segoe UI", Font.BOLD, 14));
            }
        }
    }
}