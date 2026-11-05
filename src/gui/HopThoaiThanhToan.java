package gui;

import connectDB.KetNoiCSDL;
import entity.HoaDon;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// ===== GUI: HỘP THOẠI THANH TOÁN HÓA ĐƠN =====

/** Thanh toán hóa đơn đang chọn: nhân viên xác nhận sau khi thực nhận tiền. */
final class HopThoaiThanhToan extends JDialog {
    // ===== THÀNH PHẦN GIAO DIỆN =====
    // DỮ LIỆU VÀ THÀNH PHẦN CỬA SỔ
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final String invoice;
    private final Runnable refreshed;
    private final String scannedCustomer;
    private final JComboBox<Method> cmbMethod = new OChonTimKiem<>();
    private final JLabel lblAmount = GiaoDien.label("", 20, true, GiaoDien.ORANGE);
    private final JLabel lblQr = new JLabel("", SwingConstants.CENTER);
    private final JLabel lblInstruction = new JLabel("", SwingConstants.CENTER);
    private final JButton btnConfirm = GiaoDien.button("Xác nhận đã nhận tiền", GiaoDien.TEAL, Color.WHITE);
    private final JButton btnClose = GiaoDien.button("Đóng", GiaoDien.CREAM, GiaoDien.NAV);
    private SwingWorker<BufferedImage, Void> qrWorker;
    private BigDecimal total;
    private boolean busy;
    private boolean loading;
    private boolean qrReady;

    record Method(String id, String label, boolean qr) {
        // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
        @Override
        public String toString() {
            return label;
        }
    }

    // MỞ HỘP THOẠI THANH TOÁN
    // ===== PHƯƠNG THỨC: show(Component, String, Runnable, String) — Hiển thị nghiệp vụ tương ứng. =====
    static void show(Component parent, String invoice, Runnable refreshed, String scannedCustomer) {
        HopThoaiThanhToan winPayment = new HopThoaiThanhToan(parent, invoice, refreshed, scannedCustomer);
        if (winPayment.reload()) {
            winPayment.pack();
            winPayment.setLocationRelativeTo(null);
            winPayment.setVisible(true);
        } else {
            winPayment.dispose();
        }
    }

    // KHỞI TẠO BỐ CỤC CỬA SỔ
    // ===== KHỞI TẠO VÀ XỬ LÝ GIAO DIỆN =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: HopThoaiThanhToan(Component, String, Runnable, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    private HopThoaiThanhToan(Component parent, String invoice, Runnable refreshed, String scannedCustomer) {
        super(SwingUtilities.getWindowAncestor(parent), "Thanh toán hóa đơn " + invoice,
                ModalityType.APPLICATION_MODAL);
        this.invoice = invoice;
        this.refreshed = refreshed;
        this.scannedCustomer = scannedCustomer;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            // ===== PHƯƠNG THỨC: windowClosing(WindowEvent) — Xử lý yêu cầu đóng cửa sổ. =====
            @Override
            public void windowClosing(WindowEvent event) {
                close();
            }
        });

        JPanel pnlRoot = new JPanel(new BorderLayout(10, 10));
        pnlRoot.setBackground(GiaoDien.WHITE);
        pnlRoot.setBorder(new EmptyBorder(14, 18, 14, 18));

        // TIÊU ĐỀ HÓA ĐƠN VÀ CHỌN PHƯƠNG THỨC
        JPanel pnlHeader = new JPanel(new GridLayout(0, 1, 0, 6));
        pnlHeader.setOpaque(false);
        pnlHeader.add(GiaoDien.label("HÓA ĐƠN " + invoice, 16, true, GiaoDien.NAV));
        pnlHeader.add(lblAmount);
        pnlHeader.add(GiaoDien.label("Phương thức thanh toán", 12, true, GiaoDien.TEXT));
        pnlHeader.add(cmbMethod);
        pnlRoot.add(pnlHeader, BorderLayout.NORTH);

        // MÃ QR VÀ HƯỚNG DẪN THANH TOÁN
        JPanel pnlTransfer = new JPanel(new BorderLayout(0, 6));
        pnlTransfer.setOpaque(false);
        lblQr.setPreferredSize(new Dimension(500, 350));
        pnlTransfer.add(lblQr, BorderLayout.CENTER);
        lblInstruction.setPreferredSize(new Dimension(500, 90));
        pnlTransfer.add(lblInstruction, BorderLayout.SOUTH);
        pnlRoot.add(pnlTransfer, BorderLayout.CENTER);

        // NÚT ĐÓNG VÀ XÁC NHẬN
        JPanel pnlButtons = new JPanel(new GridLayout(1, 2, 8, 8));
        pnlButtons.setOpaque(false);
        pnlButtons.add(btnClose);
        pnlButtons.add(btnConfirm);
        pnlRoot.add(pnlButtons, BorderLayout.SOUTH);
        setContentPane(pnlRoot);

        cmbMethod.addActionListener(event -> {
            if (!loading) {
                showMethod();
            }
        });
        btnClose.addActionListener(event -> close());
        btnConfirm.addActionListener(event -> confirm());
    }

    // TẢI THÔNG TIN HÓA ĐƠN VÀ PHƯƠNG THỨC THANH TOÁN
    // ===== PHƯƠNG THỨC: reload() — Tải lại dữ liệu từ nguồn hiện tại. =====
    private boolean reload() {
        cancelQr();
        total = null;
        btnConfirm.setEnabled(false);
        loading = true;

        Method previous = (Method) cmbMethod.getSelectedItem();
        cmbMethod.removeAllItems();
        try (Connection conn = KetNoiCSDL.getConnection()) {
            try (PreparedStatement ps = conn
                    .prepareStatement("SELECT tienPhuTung, tienCong, dichVuKhac, thueVAT, "
                            + "giamGia, trangThaiThanhToan " + "FROM dbo.HoaDon WHERE maHoaDon = ?")) {
                ps.setString(1, invoice);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Hóa đơn không còn tồn tại.");
                    }

                    String state = rs.getString("trangThaiThanhToan");
                    if (!state.equals("Chưa thanh toán") && !state.equals("Chờ thanh toán")) {
                        throw new SQLException("Hóa đơn không còn chờ thanh toán.");
                    }

                    HoaDon hoaDon = new HoaDon();
                    hoaDon.setTienPhuTung(rs.getBigDecimal("tienPhuTung"));
                    hoaDon.setTienCong(rs.getBigDecimal("tienCong"));
                    hoaDon.setDichVuKhac(rs.getBigDecimal("dichVuKhac"));
                    hoaDon.setThueVAT(rs.getBigDecimal("thueVAT"));
                    hoaDon.setGiamGia(rs.getBigDecimal("giamGia"));
                    total = hoaDon.tongThanhToan();
                    if (total.signum() < 0) {
                        throw new SQLException("Số tiền hóa đơn không hợp lệ.");
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement("SELECT maPhuongThuc, tenPhuongThuc "
                    + "FROM dbo.PhuongThucThanhToan " + "ORDER BY maPhuongThuc");
                    ResultSet rs = ps.executeQuery()) {
                boolean cashAdded = false;
                boolean qrAdded = false;
                while (rs.next()) {
                    String name = rs.getString(2);
                    String lower = name.toLowerCase(Locale.ROOT);
                    if (lower.contains("tiền mặt") && !cashAdded) {
                        cmbMethod.addItem(new Method(rs.getString(1), "Tiền mặt", false));
                        cashAdded = true;
                    } else if (lower.contains("qr") && !qrAdded) {
                        cmbMethod.addItem(new Method(rs.getString(1), "QR chuyển khoản", true));
                        qrAdded = true;
                    }
                }
            }

            if (cmbMethod.getItemCount() == 0) {
                throw new SQLException("Chưa có phương thức Tiền mặt/QR Code trong dữ liệu.");
            }

            int selectedIndex = 0;
            if (previous != null) {
                for (int i = 0; i < cmbMethod.getItemCount(); i++) {
                    if (previous.id().equals(cmbMethod.getItemAt(i).id())) {
                        selectedIndex = i;
                    }
                }
            }
            cmbMethod.setSelectedIndex(selectedIndex);
            lblAmount.setText("CẦN THU: "
                    + NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(total) + " đ");
        } catch (SQLException ex) {
            total = null;
            lblQr.setIcon(null);
            lblQr.setText("Không tải được hóa đơn.");
            error(ex);
            return false;
        } finally {
            loading = false;
        }

        showMethod();
        return true;
    }

    // HIỂN THỊ NỘI DUNG THEO PHƯƠNG THỨC THANH TOÁN
    // ===== PHƯƠNG THỨC: showMethod() — Hiển thị phương thức. =====
    private void showMethod() {
        cancelQr();
        qrReady = false;
        lblQr.setIcon(null);
        lblQr.setText("");
        btnConfirm.setEnabled(false);

        Method method = (Method) cmbMethod.getSelectedItem();
        if (method == null || total == null) {
            return;
        }

        if (!method.qr()) {
            lblQr.setText("<html><div style='text-align:center'>" + "THANH TOÁN TIỀN MẶT<br><br>"
                    + "Nhận đủ tiền từ khách trước khi xác nhận." + "</div></html>");

            lblInstruction.setText(scannedCustomer == null ? "Không quét mã khách hàng — không tích điểm."
                    : "Đã quét " + scannedCustomer + " — tích điểm khi thanh toán.");

            btnConfirm.setEnabled(!busy);
            return;
        }

        String amountText = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(total);

        lblInstruction.setText("<html><div style='text-align:center'>" + "Số tiền cần chuyển: " + amountText
                + " đ<br>" + "Nội dung: THANH TOAN " + invoice + "<br>" + "Số tài khoản: "
                + MaThanhToanNganHang.ACCOUNT + "<br>" + "Tên tài khoản: " + MaThanhToanNganHang.OWNER
                + "</div></html>");

        try {
            java.net.URL resource = HopThoaiThanhToan.class.getResource("/IMG/QR_ThanhToan.jpg");

            if (resource == null) {
                throw new java.io.IOException("Không tìm thấy ảnh /IMG/QR_ThanhToan.jpg");
            }

            BufferedImage qr = ImageIO.read(resource);
            if (qr == null) {
                throw new java.io.IOException("File QR không phải ảnh hợp lệ.");
            }

            // Giữ đúng tỷ lệ ảnh.
            double scale = Math.min(320.0 / qr.getWidth(), 320.0 / qr.getHeight());

            int width = Math.max(1, (int) Math.round(qr.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(qr.getHeight() * scale));

            lblQr.setIcon(new ImageIcon(qr.getScaledInstance(width, height, Image.SCALE_REPLICATE)));

            qrReady = true;
            btnConfirm.setEnabled(!busy);
        } catch (IOException ex) {
            lblQr.setIcon(null);
            lblQr.setText("Không đọc được ảnh QR.");
            lblQr.setToolTipText(ex.getMessage());
            error(ex);
        }
    }

    // XÁC NHẬN VÀ GHI NHẬN THANH TOÁN
    // ===== PHƯƠNG THỨC: confirm() — Yêu cầu xác nhận trước khi thực hiện thao tác. =====
    private void confirm() {
        Method method = (Method) cmbMethod.getSelectedItem();
        if (busy || total == null || method == null || (method.qr() && !qrReady)) {
            return;
        }

        if (JOptionPane.showConfirmDialog(this,
                "Xác nhận đã nhận đủ " + lblAmount.getText().replace("CẦN THU: ", "") + " bằng "
                        + method.label() + "?",
                "Xác nhận nhận tiền", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        BigDecimal expected = total;
        busy = true;
        toggle(false);
        new SwingWorker<Void, Void>() {
            // ===== PHƯƠNG THỨC: doInBackground() — Thực hiện truy vấn dữ liệu ở luồng nền. =====
            @Override
            protected Void doInBackground() throws Exception {
                dao.HoaDonDAO.pay(invoice, method.id(), expected, scannedCustomer);
                return null;
            }

            // ===== PHƯƠNG THỨC: done() — Cập nhật giao diện sau khi tác vụ nền hoàn tất. =====
            @Override
            protected void done() {
                busy = false;
                try {
                    get();
                    cancelQr();
                    dispose();
                    JOptionPane.showMessageDialog(getOwner(),
                            scannedCustomer == null
                                    ? "Đã thanh toán. Không tích điểm do " + "chưa quét mã khách hàng."
                                    : "Đã thanh toán và ghi nhận điểm tích lũy.");
                    refreshed.run();
                } catch (HeadlessException | InterruptedException | ExecutionException ex) {
                    toggle(true);
                    Throwable cause = ex.getCause();
                    error(cause == null ? ex : cause);
                    reload();
                    refreshed.run();
                }
            }
        }.execute();
    }

    // HÀM HỖ TRỢ THANH TOÁN
    // ===== PHƯƠNG THỨC: toggle(boolean) — Chuyển đổi thành nghiệp vụ tương ứng. =====
    private void toggle(boolean enabled) {
        cmbMethod.setEnabled(enabled);
        btnConfirm.setEnabled(enabled);
        btnClose.setEnabled(enabled);
    }

    // ===== PHƯƠNG THỨC: cancelQr() — Dừng tác vụ tạo mã QR. =====
    private void cancelQr() {
        if (qrWorker != null) {
            qrWorker.cancel(true);
        }
    }

    // ===== PHƯƠNG THỨC: close() — Đóng thành phần hoặc cửa sổ hiện tại. =====
    private void close() {
        if (!busy) {
            cancelQr();
            dispose();
        }
    }

    // ===== PHƯƠNG THỨC: error(Throwable) — Hiển thị lỗi cho người dùng. =====
    private void error(Throwable ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể thanh toán",
                JOptionPane.ERROR_MESSAGE);
    }

    // CÁC THÀNH PHẦN GIAO DIỆN DÙNG CHUNG
    private static final class GiaoDien {

        private static final Color NAV = new Color(0x13272E);
        private static final Color TEAL = new Color(0x487674);
        private static final Color CREAM = new Color(0xE8C69B);
        private static final Color ORANGE = new Color(0xCB6330);
        private static final Color WHITE = Color.WHITE;
        private static final Color TEXT = new Color(0x21383C);

        // ===== PHƯƠNG THỨC: font(int, boolean) — Tạo phông chữ theo cỡ và kiểu chữ. =====
        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
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
    }
}
