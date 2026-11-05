package gui;

import javax.swing.border.*;

import dao.TonKhoDAO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// ===== GUI: HopThoaiCapNhatTonToiThieu =====
/**
 * Hộp thoại cập nhật số lượng tồn kho tối thiểu cho phụ tùng.
 * Phục vụ nghiệp vụ BUG-03 / AUD-15.
 * Chỉ cho phép điều chỉnh TonKho.soLuongToiThieu; tuyệt đối không thay đổi soLuongHienTai.
 */
final class HopThoaiCapNhatTonToiThieu extends JDialog {
    private static final long serialVersionUID = 1L;

    // ===== PHƯƠNG THỨC: show(Component, Map<String,Object>, VaiTro, Runnable) — Hiển thị nghiệp vụ tương ứng. =====
    public static void show(Component parent, Map<String, Object> record, VaiTro role, Runnable onSuccess) {
        if (record == null) {
            JOptionPane.showMessageDialog(parent, "Vui lòng chọn phụ tùng cần cập nhật mức tồn tối thiểu.",
                    "Chưa chọn bản ghi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (role != VaiTro.KHO && role != VaiTro.ADMIN) {
            JOptionPane.showMessageDialog(parent,
                    "Chỉ Nhân viên kho hoặc Quản trị viên mới có quyền cập nhật mức tồn kho tối thiểu.",
                    "Không có quyền", JOptionPane.WARNING_MESSAGE);
            return;
        }

        HopThoaiCapNhatTonToiThieu dialog = new HopThoaiCapNhatTonToiThieu(parent, record, role, onSuccess);
        dialog.setVisible(true);
    }

    private final VaiTro role;
    private final Runnable onSuccess;
    private final JTextField txtNewThreshold;
    private final String maTonKho;
    private final String maPhuTung;

    // ===== KHỞI TẠO ĐỐI TƯỢNG: HopThoaiCapNhatTonToiThieu(Component, Map<String,Object>, VaiTro, Runnable) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    private HopThoaiCapNhatTonToiThieu(Component parent, Map<String, Object> record, VaiTro role,
            Runnable onSuccess) {
        super(parent == null ? null : SwingUtilities.getWindowAncestor(parent), "Cập nhật mức tồn tối thiểu",
                ModalityType.APPLICATION_MODAL);
        this.role = role;
        this.onSuccess = onSuccess;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        maTonKho = Objects.toString(record.get("maTonKho"), "");
        maPhuTung = Objects.toString(record.get("maPhuTung"), "");
        String tenPhuTung = Objects.toString(record.get("tenPhuTung"), "");
        String soLuongHienTai = Objects.toString(record.get("soLuongHienTai"), "0");
        String soLuongToiThieu = Objects.toString(record.get("soLuongToiThieu"), "0");

        JPanel root = GiaoDien.panel(GiaoDien.BG);
        root.setLayout(new BorderLayout(0, 16));
        root.setBorder(new EmptyBorder(22, 24, 20, 24));

        // Heading
        JPanel pnlHeading = new JPanel(new GridLayout(0, 1, 0, 4));
        pnlHeading.setOpaque(false);
        pnlHeading.add(GiaoDien.label("QUẢN LÝ TỒN KHO • THINKING AUTO", 11, true, GiaoDien.TEAL));
        pnlHeading.add(GiaoDien.label("Cập nhật mức tồn tối thiểu", 22, true, GiaoDien.NAV));
        pnlHeading.add(
                GiaoDien.label("Điều chỉnh ngưỡng cảnh báo tồn kho. Số lượng thực tế được bảo vệ nguyên vẹn.",
                        12, false, GiaoDien.MUTED));
        root.add(pnlHeading, BorderLayout.NORTH);

        // Form Card
        JPanel pnlCard = GiaoDien.card(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.insets = new Insets(0, 0, 10, 0);

        int rowIdx = 0;

        // 1. Mã tồn kho
        c.gridy = rowIdx++;
        pnlCard.add(createFieldPanel("Mã tồn kho", maTonKho, true), c);

        // 2. Phụ tùng
        c.gridy = rowIdx++;
        String partDisplay = maPhuTung + (tenPhuTung.isBlank() ? "" : " — " + tenPhuTung);
        pnlCard.add(createFieldPanel("Phụ tùng", partDisplay, true), c);

        // 3. Số lượng hiện tại (được bảo vệ, chỉ đọc)
        c.gridy = rowIdx++;
        pnlCard.add(createFieldPanel("Số lượng tồn hiện tại (thực tế trong kho)",
                soLuongHienTai + "  (Chỉ thay đổi qua nhập kho / xuất kho / hoàn trả)", true), c);

        // 4. Số lượng tối thiểu hiện tại
        c.gridy = rowIdx++;
        pnlCard.add(createFieldPanel("Mức tồn tối thiểu hiện tại", soLuongToiThieu, true), c);

        // 5. Mức tồn tối thiểu mới (cho phép nhập)
        c.gridy = rowIdx++;
        txtNewThreshold = GiaoDien.field("Nhập số nguyên >= 0");
        txtNewThreshold.setText(soLuongToiThieu);
        txtNewThreshold.setPreferredSize(new Dimension(320, 38));
        JPanel pnlNewInput = new JPanel(new BorderLayout(0, 4));
        pnlNewInput.setOpaque(false);
        JLabel lblNew = GiaoDien.label("Mức tồn tối thiểu mới *", 12, true, GiaoDien.TEXT);
        lblNew.setLabelFor(txtNewThreshold);
        pnlNewInput.add(lblNew, BorderLayout.NORTH);
        pnlNewInput.add(txtNewThreshold, BorderLayout.CENTER);
        pnlCard.add(pnlNewInput, c);

        // 6. Ghi chú quy tắc
        c.gridy = rowIdx++;
        c.insets = new Insets(4, 0, 0, 0);
        JLabel lblNote = GiaoDien.label(
                "<html><i>* Lưu ý: Cảnh báo \"Sắp hết hàng\" được kích hoạt khi Số lượng hiện tại &lt; Số lượng tối thiểu. Giá trị 0 được chấp nhận.</i></html>",
                11, false, GiaoDien.MUTED);
        pnlCard.add(lblNote, c);

        root.add(pnlCard, BorderLayout.CENTER);

        // Actions
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.setOpaque(false);

        JButton btnCancel = GiaoDien.button("Hủy", GiaoDien.CREAM, GiaoDien.NAV);
        JButton btnSave = GiaoDien.button("Lưu thay đổi", GiaoDien.TEAL, Color.WHITE);
        btnCancel.setPreferredSize(new Dimension(110, 38));
        btnSave.setPreferredSize(new Dimension(130, 38));

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> save());

        pnlActions.add(btnCancel);
        pnlActions.add(btnSave);
        root.add(pnlActions, BorderLayout.SOUTH);

        setContentPane(root);
        getRootPane().setDefaultButton(btnSave);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocationRelativeTo(parent);

        SwingUtilities.invokeLater(() -> {
            txtNewThreshold.requestFocusInWindow();
            txtNewThreshold.selectAll();
        });
    }

    // ===== PHƯƠNG THỨC: createFieldPanel(String, String, boolean) — Tạo khung chứa nhãn và ô nhập liệu. =====
    private JPanel createFieldPanel(String label, String value, boolean locked) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.add(GiaoDien.label(label, 12, true, GiaoDien.TEXT), BorderLayout.NORTH);
        JTextField txt = GiaoDien.field("");
        txt.setText(value);
        txt.setPreferredSize(new Dimension(320, 38));
        if (locked) {
            DinhDangKeThua.lock(txt);
        }
        panel.add(txt, BorderLayout.CENTER);
        return panel;
    }

    // ===== PHƯƠNG THỨC: save() — Lưu nghiệp vụ tương ứng. =====
    private void save() {
        if (role != VaiTro.KHO && role != VaiTro.ADMIN) {
            JOptionPane.showMessageDialog(this,
                    "Chỉ Nhân viên kho hoặc Quản trị viên mới có quyền cập nhật mức tồn kho tối thiểu.",
                    "Không có quyền", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String raw = txtNewThreshold.getText().trim();
        if (raw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số lượng tồn kho tối thiểu.",
                    "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            txtNewThreshold.requestFocusInWindow();
            return;
        }

        int newThreshold;
        try {
            newThreshold = Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Mức tồn tối thiểu phải là số nguyên hợp lệ (không chứa ký tự hoặc chữ cái).",
                    "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            txtNewThreshold.requestFocusInWindow();
            txtNewThreshold.selectAll();
            return;
        }

        if (newThreshold < 0) {
            JOptionPane.showMessageDialog(this, "Mức tồn tối thiểu không được nhỏ hơn 0.",
                    "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            txtNewThreshold.requestFocusInWindow();
            txtNewThreshold.selectAll();
            return;
        }

        try {
            new TonKhoDAO().updateMinimumThreshold(maTonKho, newThreshold, role);
            JOptionPane.showMessageDialog(this, "Đã cập nhật mức tồn tối thiểu cho phụ tùng " + maPhuTung
                    + " thành " + newThreshold + ".", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            if (onSuccess != null) {
                onSuccess.run();
            }
        } catch (SQLException | SecurityException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Không thể cập nhật mức tồn tối thiểu:\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static final class GiaoDien {

        private static final Color NAV = new Color(0x13272E);
        private static final Color TEAL = new Color(0x487674);
        private static final Color CREAM = new Color(0xE8C69B);
        private static final Color BG = new Color(0xF4F7F6);
        private static final Color WHITE = Color.WHITE;
        private static final Color TEXT = new Color(0x21383C);
        private static final Color MUTED = new Color(0x718387);
        private static final Color LINE = new Color(0xE4EBE9);

        // ===== PHƯƠNG THỨC: font(int, boolean) — Tạo phông chữ theo cỡ và kiểu chữ. =====
        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
        }

        // ===== PHƯƠNG THỨC: panel(Color) — Xử lý khung. =====
        public static JPanel panel(Color c) {
            JPanel pnlP = new JPanel();
            pnlP.setBackground(c);
            return pnlP;
        }

        // ===== PHƯƠNG THỨC: rounded(Color, int) — Tạo đường viền bo góc cho thành phần. =====
        public static JPanel rounded(Color c, int r) {
            return new JPanel() {
                // ===== PHƯƠNG THỨC: paintComponent(Graphics) — Vẽ nội dung và trạng thái hiện tại của thành phần. =====
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D q = (Graphics2D) g.create();
                    q.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    q.setColor(c);
                    q.fillRoundRect(0, 0, getWidth(), getHeight(), r, r);
                    q.dispose();
                }
            };
        }

        // ===== PHƯƠNG THỨC: button(String, Color, Color) — Tạo nút giao diện theo kiểu chỉ định. =====
        public static JButton button(String s, Color bg, Color fg) {
            JButton btnB = new JButton(s) {
                // ===== PHƯƠNG THỨC: paintComponent(Graphics) — Vẽ nội dung và trạng thái hiện tại của thành phần. =====
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D q = (Graphics2D) g.create();
                    q.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    q.setColor(getModel().isRollover() ? bg.brighter() : bg);
                    q.fillRoundRect(0, 0, getWidth(), getHeight(), 13, 13);
                    q.dispose();
                    super.paintComponent(g);
                }
            };
            btnB.setForeground(fg);
            BieuTuong.apply(btnB, s, fg);
            btnB.setContentAreaFilled(false);
            btnB.setBorder(BorderFactory.createEmptyBorder(10, 17, 10, 17));
            btnB.setFocusPainted(false);
            btnB.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return btnB;
        }

        // ===== PHƯƠNG THỨC: label(String, int, boolean, Color) — Tạo nhãn giao diện. =====
        public static JLabel label(String s, int size, boolean bold, Color c) {
            JLabel lblL = new JLabel(s);
            lblL.setFont(font(size, bold));
            lblL.setForeground(c);
            return lblL;
        }

        // ===== PHƯƠNG THỨC: field(String) — Tạo thành phần nhập liệu cho một trường. =====
        public static JTextField field(String hint) {
            JTextField txtT = new JTextField();
            txtT.putClientProperty("JTextField.placeholderText", hint);
            txtT.setFont(font(13, false));
            txtT.setForeground(TEXT);
            txtT.setBorder(
                    new CompoundBorder(new LineBorder(LINE, 1, true), new EmptyBorder(10, 12, 10, 12)));
            return txtT;
        }

        // ===== PHƯƠNG THỨC: card(LayoutManager) — Tạo khung giao diện dạng thẻ. =====
        public static JPanel card(LayoutManager l) {
            JPanel pnlP = rounded(WHITE, 18);
            pnlP.setLayout(l);
            pnlP.setBorder(new EmptyBorder(20, 22, 20, 22));
            pnlP.setOpaque(false);
            return pnlP;
        }
    }
}
