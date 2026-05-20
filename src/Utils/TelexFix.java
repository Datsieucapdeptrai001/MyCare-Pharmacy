package Utils;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.im.InputContext;
import java.util.Locale;

public class TelexFix {

    private static final Locale VI_LOCALE = new Locale("vi", "VN");
    private static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);

    private TelexFix() {
    }

    public static void setupGlobalTelexFix() {
        try {
            Locale.setDefault(VI_LOCALE);

            UIManager.put("TextField.font", FONT_NORMAL);
            UIManager.put("TextArea.font", FONT_NORMAL);
            UIManager.put("TextPane.font", FONT_NORMAL);
            UIManager.put("EditorPane.font", FONT_NORMAL);
            UIManager.put("PasswordField.font", FONT_NORMAL);
            UIManager.put("ComboBox.font", FONT_NORMAL);
            UIManager.put("Table.font", FONT_NORMAL);
            UIManager.put("TableHeader.font", FONT_BOLD);
            UIManager.put("Label.font", FONT_NORMAL);
            UIManager.put("Button.font", FONT_BOLD);
            UIManager.put("CheckBox.font", FONT_NORMAL);
            UIManager.put("RadioButton.font", FONT_NORMAL);
        } catch (Exception ignored) {
        }
    }

    public static void applyLater(Component root) {
        SwingUtilities.invokeLater(() -> applyDeep(root));
    }

    public static void applyWindowLater(Window window) {
        SwingUtilities.invokeLater(() -> applyWindow(window));
    }

    public static void applyWindow(Window window) {
        if (window == null) {
            return;
        }

        applyDeep(window);

        /*
         * Không ép InputContext của cả Window sang tiếng Việt,
         * vì làm vậy dễ khiến Telex bám vào JTable/JPanel.
         * Chỉ ô nhập liệu mới được bật input method riêng.
         */
    }

    public static void applyDeep(Component root) {
        if (root == null) {
            return;
        }

        applyToComponent(root);

        if (root instanceof JMenu) {
            Component[] menuChildren = ((JMenu) root).getMenuComponents();
            for (Component child : menuChildren) {
                applyDeep(child);
            }
        }

        if (root instanceof Container) {
            Component[] children = ((Container) root).getComponents();
            for (Component child : children) {
                applyDeep(child);
            }
        }
    }

    private static void applyToComponent(Component component) {
        if (component == null) {
            return;
        }

        try {
            component.setLocale(VI_LOCALE);
            component.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        } catch (Exception ignored) {
        }

        if (component instanceof JTextComponent) {
            applyToTextComponent((JTextComponent) component);
            return;
        }

        if (component instanceof JComboBox) {
            applyToComboBox((JComboBox<?>) component);
            return;
        }

        if (component instanceof JTable) {
            applyToTable((JTable) component);
            return;
        }

        /*
         * Quan trọng:
         * Những component không nhập chữ thì tắt input method.
         * Nếu không tắt, Telex sẽ hiện khung gõ đè UI như hình.
         */
        disableInputMethod(component);

        if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            if (label.getFont() == null) {
                label.setFont(FONT_NORMAL);
            }
            label.setFocusable(false);
        }

        if (component instanceof JButton) {
            JButton button = (JButton) component;
            if (button.getFont() == null) {
                button.setFont(FONT_BOLD);
            }
            button.setFocusable(false);
        }

        if (component instanceof JCheckBox) {
            JCheckBox checkBox = (JCheckBox) component;
            if (checkBox.getFont() == null) {
                checkBox.setFont(FONT_NORMAL);
            }
            checkBox.setFocusable(false);
        }

        if (component instanceof JRadioButton) {
            JRadioButton radioButton = (JRadioButton) component;
            if (radioButton.getFont() == null) {
                radioButton.setFont(FONT_NORMAL);
            }
            radioButton.setFocusable(false);
        }

        if (component instanceof JPanel
                || component instanceof JScrollPane
                || component instanceof JViewport
                || component instanceof JTabbedPane
                || component instanceof JSplitPane
                || component instanceof JToolBar
                || component instanceof JSeparator) {
            component.setFocusable(false);
        }
    }

    public static void applyToTextComponent(JTextComponent textComponent) {
        if (textComponent == null) {
            return;
        }

        try {
            textComponent.enableInputMethods(true);
            textComponent.setLocale(VI_LOCALE);
            textComponent.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            textComponent.setFont(FONT_NORMAL);

            InputContext inputContext = textComponent.getInputContext();
            if (inputContext != null) {
                inputContext.selectInputMethod(VI_LOCALE);
            }
        } catch (Exception ignored) {
        }
    }

    public static void applyToComboBox(JComboBox<?> comboBox) {
        if (comboBox == null) {
            return;
        }

        try {
            comboBox.setLocale(VI_LOCALE);
            comboBox.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
            comboBox.setFont(FONT_NORMAL);
        } catch (Exception ignored) {
        }

        if (comboBox.isEditable()) {
            try {
                comboBox.enableInputMethods(true);

                Component editor = comboBox.getEditor() != null
                        ? comboBox.getEditor().getEditorComponent()
                        : null;

                if (editor instanceof JTextComponent) {
                    applyToTextComponent((JTextComponent) editor);
                }
            } catch (Exception ignored) {
            }
        } else {
            disableInputMethod(comboBox);
            comboBox.setFocusable(false);
        }
    }

    public static void applyToTable(JTable table) {
        if (table == null) {
            return;
        }

        try {
            table.setFont(FONT_NORMAL);
            table.setLocale(VI_LOCALE);
            table.setRowSelectionAllowed(true);
            table.setColumnSelectionAllowed(false);

            /*
             * Quan trọng nhất để hết khung Telex đè trên bảng.
             */
            table.enableInputMethods(false);
            table.setSurrendersFocusOnKeystroke(false);

            /*
             * Nếu bảng của m không cần gõ phím để edit cell,
             * tắt focus luôn vẫn click chọn dòng bình thường.
             */
            table.setFocusable(false);

            if (table.getTableHeader() != null) {
                table.getTableHeader().enableInputMethods(false);
                table.getTableHeader().setFocusable(false);
                table.getTableHeader().setFont(FONT_BOLD);
            }
        } catch (Exception ignored) {
        }
    }

    private static void disableInputMethod(Component component) {
        if (component == null) {
            return;
        }

        try {
            component.enableInputMethods(false);
        } catch (Exception ignored) {
        }
    }

    /*
     * Gọi hàm này cho màn hình nào bị Telex đè nặng,
     * ví dụ ManHinhLoHang sau khi load bảng xong.
     */
    public static void hardFixTables(Component root) {
        if (root == null) {
            return;
        }

        if (root instanceof JTable) {
            applyToTable((JTable) root);
        }

        if (root instanceof Container) {
            for (Component child : ((Container) root).getComponents()) {
                hardFixTables(child);
            }
        }
    }

    public static void hardFixTablesLater(Component root) {
        SwingUtilities.invokeLater(() -> hardFixTables(root));
    }
}