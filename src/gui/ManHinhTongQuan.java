package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;


public final class ManHinhTongQuan extends JPanel {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    public ManHinhTongQuan(VaiTro role, ManHinhChinh winFrame, String displayName) {
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);
        setBorder(new EmptyBorder(30, 32, 30, 32));
        JPanel pnlMain = GiaoDien.stack(0);
        JLabel lblSectionTitle = GiaoDien.label("TRUNG TÂM ĐIỀU HÀNH/THINKING AUTO", 11, true, GiaoDien.TEAL);
        lblSectionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlMain.add(lblSectionTitle);
        GiaoDien.gap(pnlMain, 9);
        JLabel lblGreeting = GiaoDien.label("Xin chào, " + displayName + "!", 29, true, GiaoDien.NAV);
        lblGreeting.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlMain.add(lblGreeting);
        GiaoDien.gap(pnlMain, 8);
        GiaoDien.gap(pnlMain, 26);
        // Các thẻ chỉ số
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 14, 0));
        pnlCards.setOpaque(false);
        pnlCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        pnlCards.setAlignmentX(Component.CENTER_ALIGNMENT);
        String[] keys = role == VaiTro.KY_THUAT
                ? new String[] { "Xe tiếp nhận", "Phiếu kiểm tra", "Báo giá", "Phiếu sửa chữa" }
                : role == VaiTro.KHO
                        ? new String[] { "Loại phụ tùng", "Sắp hết hàng", "Phiếu nhập", "Yêu cầu cấp" }
                        : role == VaiTro.THU_NGAN
                                ? new String[] { "Hóa đơn", "Chờ thanh toán", "Doanh thu", "Khuyến mãi" }
                                : new String[] { "Khách hàng", "Phương tiện", "Nhân viên", "Doanh thu" };
        // Duyệt và tạo 4 thẻ
        for (String key : keys) {
            JPanel pnlCard = GiaoDien.card(new BorderLayout(0, 13));
            String displayTitle = key.equals("Yêu cầu cấp") ? "Hạng mục thiếu phụ tùng" : key;
            // Tạo dòng tên trong thẻ
            pnlCard.add(GiaoDien.label(displayTitle.toUpperCase(), 11, true, GiaoDien.MUTED),
                    BorderLayout.NORTH);
            int valSize = (key.equals("Doanh thu") || key.toLowerCase().contains("doanh")) ? 23 : 28;
            // sql bên dao
            JLabel lblValue = GiaoDien.label("—", valSize, true, GiaoDien.TEAL);
            pnlCard.add(lblValue, BorderLayout.CENTER);
            try {
                lblValue.setText(dao.TongQuanDAO.metric(key));
            } catch (java.sql.SQLException ex) {
                pnlCard.add(GiaoDien.label("Không tải được dữ liệu", 12, false, GiaoDien.MUTED),
                        BorderLayout.SOUTH);
                lblValue.setToolTipText(ex.getMessage());
            }
            pnlCards.add(pnlCard);
        }
        pnlMain.add(pnlCards);
        GiaoDien.gap(pnlMain, 25);
        // Tạo danh sách đối tượng truy cập nhanh 
        JPanel pnlSection = GiaoDien.card(new BorderLayout(0, 17));
        pnlSection.add(GiaoDien.label(role == VaiTro.ADMIN ? "Danh sách đối tượng theo từng vai trò"
                : "Truy cập nhanh theo nhóm đối tượng", 19, true, GiaoDien.NAV), BorderLayout.NORTH);
        if (role == VaiTro.ADMIN) {
            JPanel pnlColumns = new JPanel(new GridLayout(1, 4, 12, 0));
            pnlColumns.setOpaque(false);
            String[] names = { "NHÂN VIÊN KỸ THUẬT", "NHÂN VIÊN KHO", "NHÂN VIÊN BÁN HÀNG",
                    "NHÂN VIÊN QUẢN LÝ" };
            String[][] groups = { DuLieuTrinhDon.TECH, DuLieuTrinhDon.WARE, DuLieuTrinhDon.CASH,
                    DuLieuTrinhDon.MANAGER };
            for (int i = 0; i < groups.length; i++) {
                JPanel pnlGroup = new JPanel();
                pnlGroup.setLayout(new BoxLayout(pnlGroup, BoxLayout.Y_AXIS));
                pnlGroup.setOpaque(false);
                JLabel lblHeading = GiaoDien.label(names[i], 12, true, GiaoDien.TEAL);
                lblHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
                pnlGroup.add(lblHeading);
                pnlGroup.add(Box.createVerticalStrut(12));
                for (int j = 2; j < groups[i].length; j++) {
                    String page = groups[i][j];
                    JButton btnPage = GiaoDien.button(page, new Color(0xE5F0EE), GiaoDien.NAV);
                    btnPage.setHorizontalAlignment(SwingConstants.LEFT);
                    btnPage.setMaximumSize(new Dimension(Integer.MAX_VALUE, 37));
                    btnPage.setAlignmentX(Component.LEFT_ALIGNMENT);
                    btnPage.addActionListener(e -> winFrame.open(page));
                    pnlGroup.add(btnPage);
                    pnlGroup.add(Box.createVerticalStrut(8));
                }
                pnlColumns.add(pnlGroup);
            }
            pnlSection.add(pnlColumns, BorderLayout.CENTER);
        } else {
            String[] menu = DuLieuTrinhDon.forRole(role);
            JPanel pnlLinks = new JPanel(new GridLayout(0, 3, 14, 14));
            pnlLinks.setOpaque(false);
            for (int i = 1; i < menu.length; i++) {
                String page = menu[i];
                JButton btnPage = GiaoDien.button(page, new Color(0xE5F0EE), GiaoDien.NAV);
                btnPage.setHorizontalAlignment(SwingConstants.LEFT);
                btnPage.addActionListener(e -> winFrame.open(page));
                pnlLinks.add(btnPage);
            }
            pnlSection.add(pnlLinks, BorderLayout.CENTER);
        }
        pnlMain.add(pnlSection);
        GiaoDien.gap(pnlMain, 22);
        add(pnlMain, BorderLayout.NORTH);
    }

    private static final class GiaoDien {

        private static final Color NAV = new Color(0x13272E);
        private static final Color TEAL = new Color(0x487674);
        private static final Color BG = new Color(0xF4F7F6);
        private static final Color WHITE = Color.WHITE;
        private static final Color MUTED = new Color(0x718387);

        // Tạo phông chữ theo cỡ và kiểu chữ. 
        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
        }

        // Xử lý khung.
        public static JPanel panel(Color c) {
            JPanel pnlP = new JPanel();
            pnlP.setBackground(c);
            return pnlP;
        }

        // Tạo đường viền bo góc cho thành phần.
        public static JPanel rounded(Color c, int r) {
            return new JPanel() {
				private static final long serialVersionUID = 1L;

				//  Vẽ nội dung và trạng thái hiện tại của thành phần. 
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

        // Tạo nút giao diện theo kiểu chỉ định.
        public static JButton button(String s, Color bg, Color fg) {
            JButton btnB = new JButton(s) {
				private static final long serialVersionUID = 1L;

				// Vẽ nền thẻ bo góc 
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
            // icon cạnh các nút
            BieuTuong.apply(btnB, s, fg);
            btnB.setContentAreaFilled(false);
            btnB.setBorder(BorderFactory.createEmptyBorder(10, 17, 10, 17));
            btnB.setFocusPainted(false);
            btnB.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return btnB;
        }

        // Tạo nhãn 
        public static JLabel label(String s, int size, boolean bold, Color c) {
            JLabel lblL = new JLabel(s);
            lblL.setFont(font(size, bold));
            lblL.setForeground(c);
            return lblL;
        }

        // Tạo khung giao diện dạng thẻ.
        public static JPanel card(LayoutManager l) {
        	// Tạo nền trắng bo góc
            JPanel pnlP = rounded(WHITE, 18);
            pnlP.setLayout(l);
            pnlP.setBorder(new EmptyBorder(20, 22, 20, 22));
            pnlP.setOpaque(false);
            return pnlP;
        }

        // Xếp các thành phần giao diện theo chiều dọc. 
        public static JPanel stack(int gap) {
            JPanel pnlP = panel(BG);
            pnlP.setLayout(new BoxLayout(pnlP, BoxLayout.Y_AXIS));
            return pnlP;
        }

        // Tạo khoảng cách giữa các thành phần giao diện. 
        public static void gap(JPanel pnlP, int h) {
            pnlP.add(Box.createVerticalStrut(h));
        }
    }
}
