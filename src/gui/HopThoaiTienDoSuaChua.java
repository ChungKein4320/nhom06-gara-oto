package gui;

import javax.swing.border.*;

import connectDB.KetNoiCSDL;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.util.*;
import javax.swing.*;

// ===== GUI: HopThoaiTienDoSuaChua =====
/** Xác nhận từng hạng mục và bảo hành trước bước nghiệm thu. */
final class HopThoaiTienDoSuaChua {

    // ===== PHƯƠNG THỨC: show(Component, String) — Hiển thị nghiệp vụ tương ứng. =====
    static void show(Component parent, String id) {
        JDialog winDialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Tiến độ sửa chữa " + id,
                Dialog.ModalityType.APPLICATION_MODAL);
        winDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel pnlRows = new JPanel() {
            // ===== PHƯƠNG THỨC: getPreferredSize() — Tính kích thước ưu tiên của thành phần. =====
            @Override
            public Dimension getPreferredSize() {
                Dimension size = super.getPreferredSize();

                if (getParent() instanceof JViewport viewport && viewport.getWidth() > 0) {
                    return new Dimension(viewport.getWidth(), size.height);
                }

                return size;
            }
        };

        pnlRows.setLayout(new BoxLayout(pnlRows, BoxLayout.Y_AXIS));
        pnlRows.setBackground(GiaoDien.BG);
        pnlRows.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton btnFinish = GiaoDien.button("Nghiệm thu đạt", GiaoDien.TEAL, Color.WHITE);

        JButton btnStart = GiaoDien.button("Bắt đầu sửa chữa", GiaoDien.CREAM, GiaoDien.NAV);

        Runnable[] load = new Runnable[1];

        load[0] = () -> {
            try (Connection conn = KetNoiCSDL.getConnection()) {
                var headers = new dao.PhieuSuaChuaDAO().getAll(conn, Map.of("maPSC", id));

                if (headers.isEmpty()) {
                    throw new SQLException("Phiếu không còn tồn tại.");
                }

                var header = headers.get(0);
                boolean ongoing = "Đang sửa chữa".equals(header.get("trangThai"));

                btnStart.setVisible("Chờ sửa chữa".equals(header.get("trangThai")));
                btnStart.setEnabled(true);

                var lines = new dao.ChiTietPhieuSuaChuaDAO().getAll(conn, Map.of("maPSC", id));

                pnlRows.removeAll();
                pnlRows.add(new BieuMauChiTietPhieuSuaChua("PhieuSuaChua", header, Set.of(), true));

                boolean all = !lines.isEmpty();
                boolean pending = false;

                for (var line : lines) {
                    boolean done = "Hoàn thành".equals(line.get("trangThaiHangMuc"));

                    all &= done;
                    pending |= !"Đạt".equals(line.get("ketQuaNghiemThu"));

                    JPanel pnlLine = GiaoDien.card(new BorderLayout(8, 8));

                    String service = String.valueOf(line.get("maDichVu"));
                    String partId = String.valueOf(line.get("maPhuTung"));
                    boolean noPart = "PT000".equals(partId);

                    String part = noPart ? "Không sử dụng phụ tùng" : partId;

                    int quantity = ((Number) line.get("soLuongPhuTung")).intValue();

                    int savedMonths = ((Number) line.get("thoiHanBaoHanh")).intValue();

                    String warrantyText = savedMonths > 0 ? savedMonths + " tháng" : "Không bảo hành";

                    JPanel pnlInfo = new JPanel(new GridLayout(2, 1, 0, 5));
                    pnlInfo.setOpaque(false);

                    pnlInfo.add(GiaoDien.label(service + " / " + part + " — " + line.get("trangThaiHangMuc"),
                            13, true, DinhDangKeThua.TEXT));

                    pnlInfo.add(
                            GiaoDien.label("Số lượng phụ tùng: " + quantity + " — Bảo hành: " + warrantyText,
                                    12, false, DinhDangKeThua.TEXT));

                    pnlLine.add(pnlInfo, BorderLayout.CENTER);

                    boolean waitingForParts = "Thiếu phụ tùng".equals(line.get("trangThaiHangMuc"))
                            || "Chờ sửa chữa".equals(line.get("trangThaiHangMuc"));

                    String buttonText = done ? "Đã hoàn thành"
                            : waitingForParts ? "Chờ cấp phát phụ tùng" : "Xác nhận đã sửa";

                    JButton btnDone = GiaoDien.button(buttonText, GiaoDien.CREAM, GiaoDien.NAV);

                    btnDone.setEnabled(ongoing && "Đang sửa chữa".equals(line.get("trangThaiHangMuc")));

                    btnDone.addActionListener(e -> {
                        try {
                            int months = 0;

                            // Đọc bảo hành từ phụ tùng của hạng mục.
                            if (!noPart) {
                                try (Connection warrantyConn = KetNoiCSDL.getConnection()) {

                                    var parts = new dao.PhuTungDAO().getAll(warrantyConn,
                                            Map.of("maPhuTung", partId));

                                    if (parts.isEmpty()) {
                                        throw new SQLException("Không tìm thấy phụ tùng " + partId);
                                    }

                                    Object value = parts.get(0).get("thoiHanBaoHanh");

                                    if (!(value instanceof Number)) {
                                        throw new SQLException(
                                                "Phụ tùng chưa có thời hạn " + "bảo hành hợp lệ.");
                                    }

                                    months = ((Number) value).intValue();

                                    if (months < 0) {
                                        throw new SQLException("Thời hạn bảo hành không được âm.");
                                    }
                                }
                            }

                            String warrantyDisplay = months == 0 ? "Không bảo hành" : months + " tháng";

                            if (!confirmWarranty(winDialog, service, part, warrantyDisplay)) {
                                return;
                            }

                            dao.ChiTietPhieuSuaChuaDAO.completeRepairItem(id, service, partId, months);

                            load[0].run();
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(winDialog, ex.getMessage(), "Không thể xác nhận",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    });

                    pnlLine.add(btnDone, BorderLayout.EAST);
                    pnlLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

                    pnlRows.add(pnlLine);
                    pnlRows.add(Box.createVerticalStrut(6));
                }

                btnFinish.setEnabled(
                        (ongoing || "Đã sửa xong".equals(header.get("trangThai"))) && all && pending);

                pnlRows.revalidate();
                pnlRows.repaint();
            } catch (SQLException ex) {
                btnStart.setEnabled(false);
                btnFinish.setEnabled(false);

                JOptionPane.showMessageDialog(winDialog, ex.getMessage(), "Không tải được phiếu",
                        JOptionPane.ERROR_MESSAGE);
            }
        };

        btnStart.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(winDialog, "Bắt đầu sửa chữa phiếu " + id + "?",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);

            if (choice != JOptionPane.YES_OPTION)
                return;

            try {
                dao.PhieuSuaChuaDAO.startRepair(id);
                load[0].run();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(winDialog, ex.getMessage(), "Không thể bắt đầu sửa chữa",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        btnFinish.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(winDialog,
                    "Xác nhận tất cả hạng mục nghiệm thu đạt " + "và chuyển phiếu sang Đã sửa xong?",
                    "Nghiệm thu đạt", JOptionPane.YES_NO_OPTION);

            if (choice != JOptionPane.YES_OPTION)
                return;

            try {
                dao.PhieuSuaChuaDAO.acceptRepair(id);
                winDialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(winDialog, ex.getMessage(), "Không thể nghiệm thu",
                        JOptionPane.ERROR_MESSAGE);
                load[0].run();
            }
        });

        JPanel pnlContent = new JPanel(new BorderLayout(8, 8));
        pnlContent.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane scrRows = new JScrollPane(pnlRows);
        scrRows.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrRows.setPreferredSize(new Dimension(860, 520));
        scrRows.getVerticalScrollBar().setUnitIncrement(20);
        pnlContent.add(scrRows, BorderLayout.CENTER);

        JButton btnClose = GiaoDien.button("Đóng", GiaoDien.CREAM, GiaoDien.NAV);
        btnClose.addActionListener(e -> winDialog.dispose());

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        pnlButtons.add(btnStart);
        pnlButtons.add(btnFinish);
        pnlButtons.add(btnClose);
        pnlContent.add(pnlButtons, BorderLayout.SOUTH);

        // Đăng ký trước khi mở dialog để nhận được windowOpened.
        javax.swing.Timer refreshTimer = new javax.swing.Timer(3000, e -> {
            if (winDialog.isShowing() && winDialog.isActive()) {
                load[0].run();
            }
        });

        winDialog.addWindowListener(new WindowAdapter() {
            // ===== PHƯƠNG THỨC: windowOpened(WindowEvent) — Khởi tạo nội dung khi cửa sổ được mở. =====
            @Override
            public void windowOpened(WindowEvent e) {
                refreshTimer.start();
            }

            // ===== PHƯƠNG THỨC: windowClosed(WindowEvent) — Dọn trạng thái sau khi cửa sổ đóng. =====
            @Override
            public void windowClosed(WindowEvent e) {
                refreshTimer.stop();
            }
        });

        winDialog.setContentPane(pnlContent);
        load[0].run();
        winDialog.pack();
        winDialog.setLocationRelativeTo(null);
        winDialog.setVisible(true);
    }

    // ===== PHƯƠNG THỨC: confirmWarranty(Window, String, String, String) — Xác nhận thông tin bảo hành. =====
    /**
     * Popup xác nhận bảo hành theo kiểu biểu mẫu của ứng dụng.
     * Chỉ trả về lựa chọn; việc lưu được thực hiện ở hàm gọi.
     */
    private static boolean confirmWarranty(Window owner, String service, String part, String warranty) {
        JDialog dialog = new JDialog(owner, "Xác nhận đã sửa", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        boolean[] confirmed = { false };

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(new Color(0xEEEEEE));
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel form = new JPanel(new GridLayout(0, 2, 16, 16));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        form.add(warrantyField("Mã dịch vụ", service));
        form.add(warrantyField("Phụ tùng", part));
        form.add(warrantyField("Thời hạn bảo hành", warranty));
        form.add(warrantyField("Trạng thái sau xác nhận", "Hoàn thành"));

        root.add(form, BorderLayout.CENTER);

        JButton btnConfirm = GiaoDien.button("Xác nhận", GiaoDien.TEAL, Color.WHITE);
        JButton btnCancel = GiaoDien.button("Hủy", GiaoDien.CREAM, GiaoDien.NAV);

        btnConfirm.setPreferredSize(new Dimension(120, 40));
        btnCancel.setPreferredSize(new Dimension(115, 40));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(btnConfirm);
        buttons.add(btnCancel);
        root.add(buttons, BorderLayout.SOUTH);

        btnConfirm.addActionListener(e -> {
            confirmed[0] = true;
            dialog.dispose();
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setContentPane(root);
        dialog.getRootPane().setDefaultButton(btnConfirm);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);

        return confirmed[0];
    }

    // ===== PHƯƠNG THỨC: warrantyField(String, String) — Xác định trường thời hạn bảo hành. =====
    /** Tạo ô thông tin chỉ đọc với màu kế thừa dùng chung. */
    private static JPanel warrantyField(String title, String value) {
        JPanel cell = new JPanel(new BorderLayout(0, 7));
        cell.setOpaque(false);

        cell.add(GiaoDien.label(title, 12, true, GiaoDien.TEXT), BorderLayout.NORTH);

        JTextField field = GiaoDien.field("");
        field.setText(value);
        field.setToolTipText(value);
        field.setPreferredSize(new Dimension(250, 38));
        DinhDangKeThua.lock(field);

        cell.add(field, BorderLayout.CENTER);
        return cell;
    }

    private static final class GiaoDien {

        private static final Color NAV = new Color(0x13272E);
        private static final Color TEAL = new Color(0x487674);
        private static final Color CREAM = new Color(0xE8C69B);
        private static final Color BG = new Color(0xF4F7F6);
        private static final Color WHITE = Color.WHITE;
        private static final Color TEXT = new Color(0x21383C);
        private static final Color LINE = new Color(0xE4EBE9);

        // ===== PHƯƠNG THỨC: font(int, boolean) — Tạo phông chữ theo cỡ và kiểu chữ. =====
        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
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
