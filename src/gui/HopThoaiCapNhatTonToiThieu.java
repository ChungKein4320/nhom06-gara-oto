package gui;

import javax.swing.border.*;

import dao.TonKhoDAO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import javax.swing.*;

/**
 * ============================================================================
 * GUI: HopThoaiCapNhatTonToiThieu (Hộp thoại cập nhật mức tồn tối thiểu)
 * ============================================================================
 * 1. Mục đích:
 *    - Hiển thị cửa sổ modal cho phép Nhân viên kho hoặc Quản trị viên điều chỉnh
 *      định mức tồn kho tối thiểu (TonKho.soLuongToiThieu) của một phụ tùng.
 *    - Bảo vệ nghiêm ngặt số lượng tồn kho thực tế (soLuongHienTai chỉ hiển thị
 *      ở chế độ khóa chỉ đọc, tuyệt đối không cho sửa tay tại đây).
 * 2. Phục vụ màn hình / nghiệp vụ:
 *    - Nghiệp vụ 1 (Quản lý tồn kho và mức tồn tối thiểu): Được mở từ FormTonKho
 *      khi người dùng bấm nút "Cập nhật mức tồn tối thiểu" hoặc nhấp đúp vào dòng.
 * 3. Gọi DAO nào:
 *    - Gọi TonKhoDAO.updateMinimumThreshold(maTonKho, newThreshold, role).
 * 4. Phương thức quan trọng:
 *    - show(Component, Map, VaiTro, Runnable): Kiểm tra quyền và mở hộp thoại.
 *    - luuMucTonToiThieu(): Đọc dữ liệu ô nhập, kiểm tra số nguyên >= 0, gọi DAO
 *      cập nhật CSDL và kích hoạt callback tải lại bảng.
 * ============================================================================
 */
final class HopThoaiCapNhatTonToiThieu extends JDialog {
    private static final long serialVersionUID = 1L;

    // ===== NHÓM 1: ĐIỂM VÀO MỞ HỘP THOẠI & KIỂM TRA QUYỀN TRƯỚC KHI HIỂN THỊ =====

    // Kiểm tra bản ghi được chọn và phân quyền (KHO / ADMIN) trước khi mở hộp thoại.
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

    // ===== THUỘC TÍNH GIAO DIỆN & DỮ LIỆU BẢN GHI =====
    private final VaiTro role;
    private final Runnable onSuccess;
    private final JTextField txtNewThreshold;
    private final String maTonKho;
    private final String maPhuTung;

    // ===== NHÓM 2: KHỞI TẠO GIAO DIỆN, TẠO THÀNH PHẦN SWING & ĐĂNG KÝ SỰ KIỆN =====

    private HopThoaiCapNhatTonToiThieu(Component parent, Map<String, Object> record, VaiTro role,
            Runnable onSuccess) {
        super(parent == null ? null : SwingUtilities.getWindowAncestor(parent), "Cập nhật mức tồn tối thiểu",
                ModalityType.APPLICATION_MODAL);
        this.role = role;
        this.onSuccess = onSuccess;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // Lấy thông tin bản ghi tồn kho đang chọn để hiển thị lên biểu mẫu.
        maTonKho = Objects.toString(record.get("maTonKho"), "");
        maPhuTung = Objects.toString(record.get("maPhuTung"), "");
        String tenPhuTung = Objects.toString(record.get("tenPhuTung"), "");
        String soLuongHienTai = Objects.toString(record.get("soLuongHienTai"), "0");
        String soLuongToiThieu = Objects.toString(record.get("soLuongToiThieu"), "0");

        JPanel root = GiaoDien.panel(GiaoDien.BG);
        root.setLayout(new BorderLayout(0, 16));
        root.setBorder(new EmptyBorder(22, 24, 20, 24));

        // 1. Tiêu đề hộp thoại (Heading)
        JPanel pnlHeading = new JPanel(new GridLayout(0, 1, 0, 4));
        pnlHeading.setOpaque(false);
        pnlHeading.add(GiaoDien.label("QUẢN LÝ TỒN KHO • THINKING AUTO", 11, true, GiaoDien.TEAL));
        pnlHeading.add(GiaoDien.label("Cập nhật mức tồn tối thiểu", 22, true, GiaoDien.NAV));
        pnlHeading.add(
                GiaoDien.label("Điều chỉnh ngưỡng cảnh báo tồn kho. Số lượng thực tế được bảo vệ nguyên vẹn.",
                        12, false, GiaoDien.MUTED));
        root.add(pnlHeading, BorderLayout.NORTH);

        // 2. Thẻ biểu mẫu thông tin tồn kho (Form Card)
        JPanel pnlCard = GiaoDien.card(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.insets = new Insets(0, 0, 10, 0);

        int rowIdx = 0;

        // Trường 1: Mã tồn kho (khóa chỉ đọc)
        c.gridy = rowIdx++;
        pnlCard.add(taoKhungTruongThongTin("Mã tồn kho", maTonKho, true), c);

        // Trường 2: Phụ tùng (khóa chỉ đọc)
        c.gridy = rowIdx++;
        String partDisplay = maPhuTung + (tenPhuTung.isBlank() ? "" : " — " + tenPhuTung);
        pnlCard.add(taoKhungTruongThongTin("Phụ tùng", partDisplay, true), c);

        // Trường 3: Số lượng hiện tại (được bảo vệ, chỉ đọc)
        c.gridy = rowIdx++;
        pnlCard.add(taoKhungTruongThongTin("Số lượng tồn hiện tại (thực tế trong kho)",
                soLuongHienTai + "  (Chỉ thay đổi qua nhập kho / xuất kho / hoàn trả)", true), c);

        // Trường 4: Số lượng tối thiểu hiện tại (khóa chỉ đọc)
        c.gridy = rowIdx++;
        pnlCard.add(taoKhungTruongThongTin("Mức tồn tối thiểu hiện tại", soLuongToiThieu, true), c);

        // Trường 5: Mức tồn tối thiểu mới (ô nhập liệu duy nhất cho phép chỉnh sửa)
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

        // Trường 6: Ghi chú quy tắc cảnh báo tồn kho
        c.gridy = rowIdx++;
        c.insets = new Insets(4, 0, 0, 0);
        JLabel lblNote = GiaoDien.label(
                "<html><i>* Lưu ý: Cảnh báo \"Sắp hết hàng\" được kích hoạt khi Số lượng hiện tại &lt; Số lượng tối thiểu. Giá trị 0 được chấp nhận.</i></html>",
                11, false, GiaoDien.MUTED);
        pnlCard.add(lblNote, c);

        root.add(pnlCard, BorderLayout.CENTER);

        // 3. Thanh nút chức năng và đăng ký sự kiện (Hủy / Lưu thay đổi)
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlActions.setOpaque(false);

        JButton btnCancel = GiaoDien.button("Hủy", GiaoDien.CREAM, GiaoDien.NAV);
        JButton btnSave = GiaoDien.button("Lưu thay đổi", GiaoDien.TEAL, Color.WHITE);
        btnCancel.setPreferredSize(new Dimension(110, 38));
        btnSave.setPreferredSize(new Dimension(130, 38));

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> luuMucTonToiThieu());

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

    // Tạo một khung trường thông tin gồm nhãn tiêu đề và ô văn bản (có thể khóa chỉ đọc).
    private JPanel taoKhungTruongThongTin(String label, String value, boolean locked) {
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

    // ===== NHÓM 3: LẤY DỮ LIỆU, KIỂM TRA HỢP LỆ, GỌI DAO & TẢI LẠI BẢNG =====

    /**
     * Đọc mức tồn tối thiểu mới từ ô nhập, kiểm tra điều kiện số nguyên >= 0,
     * gọi TonKhoDAO.updateMinimumThreshold() và chạy callback onSuccess để tải lại bảng.
     */
    private void luuMucTonToiThieu() {
        // BƯỚC 1: Kiểm tra lại quyền thực hiện (chỉ KHO hoặc ADMIN).
        if (role != VaiTro.KHO && role != VaiTro.ADMIN) {
            JOptionPane.showMessageDialog(this,
                    "Chỉ Nhân viên kho hoặc Quản trị viên mới có quyền cập nhật mức tồn kho tối thiểu.",
                    "Không có quyền", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // BƯỚC 2: Lấy dữ liệu người dùng nhập và kiểm tra không được để trống.
        String raw = txtNewThreshold.getText().trim();
        if (raw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số lượng tồn kho tối thiểu.",
                    "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
            txtNewThreshold.requestFocusInWindow();
            return;
        }

        // BƯỚC 3: Kiểm tra giá trị nhập phải là số nguyên hợp lệ và >= 0.
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

        // BƯỚC 4: Gọi TonKhoDAO cập nhật cột soLuongToiThieu, thông báo kết quả và tải lại danh sách.
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

    // ===== LỚP TIỆN ÍCH GIAO DIỆN NỘI BỘ =====
    private static final class GiaoDien {

        private static final Color NAV = new Color(0x13272E);
        private static final Color TEAL = new Color(0x487674);
        private static final Color CREAM = new Color(0xE8C69B);
        private static final Color BG = new Color(0xF4F7F6);
        private static final Color WHITE = Color.WHITE;
        private static final Color TEXT = new Color(0x21383C);
        private static final Color MUTED = new Color(0x718387);
        private static final Color LINE = new Color(0xE4EBE9);

        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
        }

        public static JPanel panel(Color c) {
            JPanel pnlP = new JPanel();
            pnlP.setBackground(c);
            return pnlP;
        }

        public static JPanel rounded(Color c, int r) {
            return new JPanel() {
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

        public static JButton button(String s, Color bg, Color fg) {
            JButton btnB = new JButton(s) {
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

        public static JLabel label(String s, int size, boolean bold, Color c) {
            JLabel lblL = new JLabel(s);
            lblL.setFont(font(size, bold));
            lblL.setForeground(c);
            return lblL;
        }

        public static JTextField field(String hint) {
            JTextField txtT = new JTextField();
            txtT.putClientProperty("JTextField.placeholderText", hint);
            txtT.setFont(font(13, false));
            txtT.setForeground(TEXT);
            txtT.setBorder(
                    new CompoundBorder(new LineBorder(LINE, 1, true), new EmptyBorder(10, 12, 10, 12)));
            return txtT;
        }

        public static JPanel card(LayoutManager l) {
            JPanel pnlP = rounded(WHITE, 18);
            pnlP.setLayout(l);
            pnlP.setBorder(new EmptyBorder(20, 22, 20, 22));
            pnlP.setOpaque(false);
            return pnlP;
        }
    }
}
