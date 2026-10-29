package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;

/** Định dạng chung cho thông tin kế thừa và trường chỉ đọc. */
final class DinhDangKeThua {
    static final Color TEXT = new Color(0x145A75);
    static final Color BACKGROUND = new Color(0xEAF5FA);

    private DinhDangKeThua() {}

    static void lock(JComponent control) {
        if (control instanceof JTextComponent text) {
            text.setEditable(false);
            applyStyle(text);
            return;
        }

        if (control instanceof JComboBox<?> combo) {
            combo.setPopupVisible(false);
            combo.setEnabled(false);

            // Giữ nguyên UI và editor của OChonTimKiem.
            applyStyle(combo);
            applyStyle(combo.getEditor().getEditorComponent());
            return;
        }

        disableDeep(control);
    }

    static void applyStyle(Component component) {
        component.setBackground(BACKGROUND);
        component.setForeground(TEXT);
        component.setFont(GiaoDien.font(12, true));

        if (component instanceof JTextComponent text) {
            text.setDisabledTextColor(TEXT);
            text.setCaretColor(TEXT);
            text.setOpaque(true);
        }

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyStyle(child);
            }
        }

        component.repaint();
    }

    private static void disableDeep(Component component) {
        component.setEnabled(false);

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                disableDeep(child);
            }
        }

        applyStyle(component);
    }
}