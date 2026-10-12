package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;

/** Biểu mẫu theo từng nghiệp vụ; tất cả dữ liệu hiện chỉ lưu trong bộ nhớ. */
public final class EntryDialog {
    private EntryDialog() { }

    private static String[] fieldsFor(String name, String[] fallback) {
        switch (name) {
            case "Quản lý tài khoản":
                return new String[] {"Tên đăng nhập", "Nhân viên", "Vai trò", "Trạng thái", "Mật khẩu mặc định"};
            case "Quản lý nhân viên":
                return new String[] {"Họ và tên", "Ngày sinh", "Số điện thoại", "Thư điện tử", "Vai trò", "Trạng thái", "Địa chỉ"};
            case "Quản lý khách hàng":
                return new String[] {"Họ và tên", "Số điện thoại", "Thư điện tử", "Địa chỉ", "Ghi chú"};
            case "Quản lý phương tiện":
                return new String[] {"Biển số", "Số khung (VIN)", "Hãng/Dòng xe", "Năm sản xuất", "Số km", "Chủ xe"};
            case "Quản lý nhà cung cấp":
                return new String[] {"Tên nhà cung cấp", "Điện thoại", "Thư điện tử", "Địa chỉ", "Trạng thái"};
            case "Danh mục phụ tùng và dịch vụ":
                return new String[] {"Mã danh mục", "Tên danh mục", "Loại", "Đơn vị", "Đơn giá", "Trạng thái"};
            case "Khuyến mãi":
                return new String[] {"Mã chương trình", "Tên chương trình", "Từ ngày", "Đến ngày", "Mức giảm", "Trạng thái"};
            default:
                return fallback;
        }
    }

    private static boolean fullWidth(String field) {
        String s = field.toLowerCase();
        return s.contains("địa chỉ") || s.contains("ghi chú") || s.contains("nội dung")
                || s.contains("mật khẩu") || s.contains("yêu cầu") || s.contains("chẩn đoán");
    }

    private static JComponent input(String label) {
        String s = label.toLowerCase();
        if (s.contains("vai trò")) {
            return new JComboBox<>(new String[] {"Nhân viên kỹ thuật", "Nhân viên kho", "Thu ngân", "Nhân viên quản lý"});
        }
        if (s.contains("trạng thái")) {
            return new JComboBox<>(new String[] {"Hoạt động", "Đang làm", "Tạm ngưng", "Ngừng hoạt động"});
        }
        if (s.equals("loại")) {
            return new JComboBox<>(new String[] {"Phụ tùng", "Dịch vụ"});
        }
        if (s.equals("nhân viên")) {
            return new JComboBox<>(new String[] {"-- Chọn nhân viên --", "Phạm Thị Ngọc Châu", "Cao Huỳnh Trung Kiên", "Quách Tấn Minh", "Châu Ngọc Khanh"});
        }
        if (s.contains("mật khẩu")) {
            JPasswordField pass = new JPasswordField("123456");
            return pass;
        }
        return Theme.field("");
    }

    private static void setValue(JComponent c, String value) {
        if (c instanceof JTextField) {
            ((JTextField) c).setText(value);
        } else if (c instanceof JComboBox<?>) {
            ((JComboBox<?>) c).setSelectedItem(value);
        }
    }

    private static String value(JComponent c) {
        if (c instanceof JTextField) {
            return ((JTextField) c).getText().trim();
        }
        if (c instanceof JComboBox<?>) {
            Object selected = ((JComboBox<?>) c).getSelectedItem();
            return selected == null ? "" : selected.toString();
        }
        return "";
    }

    private static JPanel cell(String label, JComponent input) {
        JPanel cell = new JPanel(new BorderLayout(0, 7));
        cell.setOpaque(false);
        JLabel caption = Theme.label(label + " *", 12, true, Theme.TEXT);
        cell.add(caption, BorderLayout.NORTH);
        input.setFont(Theme.font(13, false));
        input.setPreferredSize(new Dimension(190, 39));
        cell.add(input, BorderLayout.CENTER);
        return cell;
    }

    /** Trả về giá trị theo đúng thứ tự cột bảng, hoặc null khi hủy. */
    public static String[] show(Component owner, String name, String title,
                                String[] columns, String[] original) {
        String[] labels = fieldsFor(name, columns);
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(owner),
                title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(true);

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(24, 25, 22, 25));
        JLabel heading = Theme.label(title, 19, true, Theme.NAV);
        content.add(heading, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        Map<String, JComponent> controls = new LinkedHashMap<>();
        int row = 0;
        int col = 0;
        for (String label : labels) {
            JComponent input = input(label);
            controls.put(label, input);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = col;
            gbc.gridy = row;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, col == 0 ? 0 : 7, 14, col == 0 ? 7 : 0);
            if (fullWidth(label)) {
                if (col == 1) {
                    row++;
                }
                gbc.gridx = 0;
                gbc.gridy = row;
                gbc.gridwidth = 2;
                gbc.insets = new Insets(0, 0, 14, 0);
                fields.add(cell(label, input), gbc);
                row++;
                col = 0;
            } else {
                fields.add(cell(label, input), gbc);
                col++;
                if (col == 2) {
                    row++;
                    col = 0;
                }
            }
        }
        if (original != null) {
            for (int i = 0; i < columns.length && i < original.length; i++) {
                JComponent input = controls.get(columns[i]);
                if (input != null) {
                    setValue(input, original[i]);
                }
            }
        }
        JScrollPane scroll = new JScrollPane(fields);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(scroll, BorderLayout.CENTER);

        final String[][] result = new String[1][];
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 9, 0));
        footer.setOpaque(false);
        JButton cancel = Theme.button("Hủy", new Color(0xE6EFED), Theme.NAV);
        JButton save = Theme.button("Lưu", Theme.TEAL, Color.WHITE);
        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            String[] values = new String[columns.length];
            for (int i = 0; i < columns.length; i++) {
                JComponent input = controls.get(columns[i]);
                values[i] = input == null ? (original != null && i < original.length ? original[i] : "") : value(input);
            }
            if (values.length > 0 && values[0].isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập trường thông tin đầu tiên.");
                return;
            }
            result[0] = values;
            dialog.dispose();
        });
        footer.add(cancel);
        footer.add(save);
        content.add(footer, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.setSize(490, Math.min(690, Math.max(330, row * 80 + 185)));
        dialog.setMinimumSize(new Dimension(430, 320));
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        return result[0];
    }
}
