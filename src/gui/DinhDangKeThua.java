package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;

// ===== GUI: ĐỊNH DẠNG GIAO DIỆN DÙNG CHUNG =====

/** Định dạng chung cho thông tin kế thừa và trường chỉ đọc. */
final class DinhDangKeThua {
    // ===== THÀNH PHẦN GIAO DIỆN =====
    static final Color TEXT = new Color(0x145A75);
    static final Color BACKGROUND = new Color(0xEAF5FA);
    // ===== KHỞI TẠO VÀ XỬ LÝ GIAO DIỆN =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: DinhDangKeThua() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    private DinhDangKeThua() {
    }

    // ===== PHƯƠNG THỨC: lock(JComponent) — Lọc nghiệp vụ tương ứng. =====
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

    // ===== PHƯƠNG THỨC: applyStyle(Component) — Áp dụng kiểu giao diện cho thành phần. =====
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

    // ===== PHƯƠNG THỨC: disableDeep(Component) — Vô hiệu hóa thành phần và các thành phần con. =====
    private static void disableDeep(Component component) {
        component.setEnabled(false);

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                disableDeep(child);
            }
        }

        applyStyle(component);
    }

    private static final class GiaoDien {

        // ===== PHƯƠNG THỨC: font(int, boolean) — Tạo phông chữ theo cỡ và kiểu chữ. =====
        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
        }
    }
}
