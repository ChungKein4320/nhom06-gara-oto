package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Properties;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/** Biên tập thông tin cửa hàng và xem trước phần đầu chứng từ. */
final class HopThoaiThongTinCuaHang extends JDialog {
    private final ThongTinCuaHang store;
    private final JTextField[] fields = new JTextField[6];
    private final JLabel message = GiaoDien.label(" ", 12, false, GiaoDien.ORANGE);
    private final JEditorPane preview = new JEditorPane("text/html", "");
    private boolean saved;

    HopThoaiThongTinCuaHang(Component owner, ThongTinCuaHang store) {
        super(owner == null ? null : SwingUtilities.getWindowAncestor(owner),
                "Thông tin cửa hàng trên hóa đơn", ModalityType.APPLICATION_MODAL);
        this.store = store;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel root = GiaoDien.panel(GiaoDien.BG);
        root.setLayout(new BorderLayout(0, 18));
        root.setBorder(new EmptyBorder(24, 26, 20, 26));

        JPanel heading = new JPanel(new GridLayout(0, 1, 0, 5)); heading.setOpaque(false);
        heading.add(GiaoDien.label("THIẾT LẬP CHỨNG TỪ", 11, true, GiaoDien.TEAL));
        heading.add(GiaoDien.label("Thông tin cửa hàng", 25, true, GiaoDien.NAV));
        heading.add(GiaoDien.label("Thông tin được dùng chung cho hóa đơn dịch vụ và bán phụ tùng.", 13, false, GiaoDien.MUTED));
        root.add(heading, BorderLayout.NORTH);

        JPanel form = GiaoDien.card(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        c.insets = new Insets(0, 0, 12, 0);
        form.add(GiaoDien.label("Thông tin hiển thị", 16, true, GiaoDien.NAV), c);
        for (int i = 0; i < fields.length; i++) {
            fields[i] = GiaoDien.field(ThongTinCuaHang.LABELS[i]);
            fields[i].setText(store.get(ThongTinCuaHang.KEYS[i]));
            fields[i].setName(ThongTinCuaHang.KEYS[i]);
            fields[i].getAccessibleContext().setAccessibleName(ThongTinCuaHang.LABELS[i]);
            fields[i].setPreferredSize(new Dimension(180, 42));
            fields[i].addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { ((JTextField)e.getComponent()).setBorder(border(GiaoDien.TEAL)); }
                @Override public void focusLost(FocusEvent e) { ((JTextField)e.getComponent()).setBorder(border(GiaoDien.LINE)); }
            });
            JPanel field = new JPanel(new BorderLayout(0, 6)); field.setOpaque(false);
            String label = ThongTinCuaHang.LABELS[i] + (i == 0 || i == 2 || i == 3 ? " *" : "");
            JLabel title = GiaoDien.label(label, 12, true, GiaoDien.TEXT); title.setLabelFor(fields[i]);
            field.add(title, BorderLayout.NORTH); field.add(fields[i], BorderLayout.CENTER);
            c.gridy = i < 3 ? i + 1 : i == 4 ? 5 : 4;
            c.gridx = i == 5 ? 1 : 0; c.gridwidth = i == 3 || i == 5 ? 1 : 2;
            c.insets = new Insets(0, i == 5 ? 7 : 0, 12, i == 3 ? 7 : 0);
            form.add(field, c);
        }
        c.gridy = 6; c.gridx = 0; c.gridwidth = 2; c.weighty = 1; c.anchor = GridBagConstraints.NORTH;
        form.add(GiaoDien.label("* Thông tin bắt buộc", 11, false, GiaoDien.MUTED), c);

        JPanel sample = GiaoDien.card(new BorderLayout(0, 12));
        JPanel sampleHeading = new JPanel(new GridLayout(0, 1, 0, 5)); sampleHeading.setOpaque(false);
        sampleHeading.add(GiaoDien.label("Xem trước trên hóa đơn", 16, true, GiaoDien.NAV));
        sampleHeading.add(GiaoDien.label("Nội dung thay đổi ngay khi bạn nhập.", 12, false, GiaoDien.MUTED));
        sample.add(sampleHeading, BorderLayout.NORTH);
        preview.setEditable(false); preview.setBackground(Color.WHITE); preview.setBorder(new EmptyBorder(14, 12, 14, 12));
        JScrollPane paper = new JScrollPane(preview); paper.setBorder(new LineBorder(GiaoDien.LINE));
        paper.setPreferredSize(new Dimension(390, 340)); sample.add(paper, BorderLayout.CENTER);
        JLabel note = GiaoDien.label("<html>Kiểm tra thông tin liên hệ trước khi lưu.<br>Dữ liệu mẫu được giữ nhãn mô phỏng trên bản in.</html>", 12, false, GiaoDien.MUTED);
        sample.add(note, BorderLayout.SOUTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 18, 0)); content.setOpaque(false);
        content.add(form); content.add(sample);
        JScrollPane scroll = new JScrollPane(content); scroll.setBorder(null); scroll.getViewport().setBackground(GiaoDien.BG);
        scroll.getVerticalScrollBar().setUnitIncrement(20); root.add(scroll, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(12, 0)); footer.setOpaque(false);
        footer.add(message, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); actions.setOpaque(false);
        JButton cancel = GiaoDien.button("Hủy", GiaoDien.CREAM, GiaoDien.NAV);
        JButton save = GiaoDien.button("Lưu thông tin", GiaoDien.ORANGE, Color.WHITE);
        cancel.addActionListener(e -> dispose()); save.addActionListener(e -> save());
        actions.add(cancel); actions.add(save); footer.add(actions, BorderLayout.EAST); root.add(footer, BorderLayout.SOUTH);
        setContentPane(root); getRootPane().setDefaultButton(save);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        DocumentListener changes = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updatePreview(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updatePreview(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updatePreview(); }
        };
        for (JTextField field : fields) field.getDocument().addDocumentListener(changes);
        updatePreview();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1040, screen.width - 60), Math.min(730, screen.height - 80));
        setMinimumSize(new Dimension(Math.min(800, screen.width - 60), Math.min(500, screen.height - 80)));
        setLocationRelativeTo(owner);
    }

    private static Border border(Color color) {
        return new CompoundBorder(new LineBorder(color, 1, true), new EmptyBorder(10, 12, 10, 12));
    }

    private Properties data() {
        Properties data = new Properties();
        for (int i = 0; i < fields.length; i++) data.setProperty(ThongTinCuaHang.KEYS[i], fields[i].getText().trim());
        return data;
    }

    private void updatePreview() {
        preview.setText("<html><head><style>body{font-family:Segoe UI;font-size:9pt;color:#21383c;}td{padding:4px;}h2{text-align:center;font-size:13pt;color:#13272e;}</style></head><body>"
                + ThongTinCuaHang.headerHtml(data())
                + "<hr><h2>HÓA ĐƠN THANH TOÁN</h2><p align='center'>Số hóa đơn: HD025 &nbsp; | &nbsp; Ngày lập: 22/10/2026</p>"
                + "<p><b>Khách hàng:</b> Nguyễn Văn An</p><p><b>Biển số:</b> 51A-123.45</p>"
                + "<table width='100%' bgcolor='#edf3f2'><tr><td><b>Nội dung</b></td><td align='right'><b>Thành tiền</b></td></tr>"
                + "<tr bgcolor='white'><td>Dịch vụ / Phụ tùng</td><td align='right'>1.000.000 đ</td></tr></table>"
                + "<p align='right'><b>TỔNG THANH TOÁN &nbsp; 1.080.000 đ</b></p><br><p align='center'>Cảm ơn Quý khách!</p></body></html>");
        preview.setCaretPosition(0);
    }

    private void save() {
        for (int i : new int[]{0, 2, 3}) if (fields[i].getText().isBlank()) {
            invalid(i, "Vui lòng nhập " + ThongTinCuaHang.LABELS[i].toLowerCase() + "."); return;
        }
        String phone = fields[3].getText().replaceAll("[\\s().+\\-]", "");
        if (!phone.matches("[0-9]{9,15}")) { invalid(3, "Điện thoại cần có từ 9 đến 15 chữ số."); return; }
        if (!fields[4].getText().isBlank() && !fields[4].getText().trim().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            invalid(4, "Email chưa đúng định dạng."); return;
        }
        try { store.save(data()); saved = true; dispose(); }
        catch (IOException ex) { message.setText("Không lưu được. Vui lòng thử lại."); message.setToolTipText(ex.getMessage()); }
    }

    private void invalid(int index, String text) {
        message.setText(text); fields[index].requestFocusInWindow(); fields[index].selectAll();
    }

    boolean saved() { return saved; }
}
