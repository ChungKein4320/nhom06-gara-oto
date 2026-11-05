package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;


public final class DangNhap extends JFrame {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private final JTextField txtUsername;
    private final JPasswordField txtPassword;

    public DangNhap() {
        // Thiết lập tiêu đề, kích thước, bố cục
    	GiaoDien.init();
        setTitle("Thinking Auto • Đăng nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1060, 650);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(null);
        // Chia cửa sổ thành 2 vùng
        setLayout(new GridLayout(1, 2));
        
        // Thương hiệu
        JPanel pnlLeft = new JPanel(new GridBagLayout());
        pnlLeft.setBackground(GiaoDien.NAV);
        pnlLeft.setBorder(new EmptyBorder(40, 28, 40, 28));
        JPanel pnlBrand = new JPanel();
        pnlBrand.setOpaque(false);
        pnlBrand.setLayout(new BoxLayout(pnlBrand, BoxLayout.Y_AXIS));
        JLabel lblBrandName = GiaoDien.label("THINKING AUTO", 38, true, GiaoDien.MINT);
        JLabel lblBrandSub = GiaoDien.label("XƯỞNG DỊCH VỤ Ô TÔ • GÒ VẤP", 15, true, GiaoDien.CREAM);
        lblBrandName.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblBrandSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlBrand.add(lblBrandName);
        pnlBrand.add(Box.createVerticalStrut(15));
        pnlBrand.add(lblBrandSub);
        pnlLeft.add(pnlBrand);
        add(pnlLeft);
        
        // Khung và lời chào
        JPanel pnlRight = new JPanel(new GridBagLayout());
        pnlRight.setBackground(GiaoDien.BG);
        JPanel pnlForm = GiaoDien.card(new GridBagLayout());
        pnlForm.setPreferredSize(new Dimension(385, 475));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 13, 0);
        int y = 0;
        c.gridy = y++;
        
        JLabel lblWelcome = GiaoDien.label("Chào mừng trở lại", 25, true, GiaoDien.NAV);
        lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
        pnlForm.add(lblWelcome, c);
        c.gridy = y++;
        JLabel lblIntro = GiaoDien.label("Đăng nhập để tiếp tục công việc tại gara", 13, false,
                GiaoDien.MUTED);
        lblIntro.setHorizontalAlignment(SwingConstants.CENTER);
        pnlForm.add(lblIntro, c);
        c.insets = new Insets(12, 0, 6, 0);
        c.gridy = y++;
        
        // Tài khoản
        pnlForm.add(GiaoDien.label("Tên đăng nhập", 12, true, GiaoDien.TEXT), c);
        txtUsername = GiaoDien.field("Nhập tên đăng nhập");
        c.insets = new Insets(0, 0, 8, 0);
        c.gridy = y++;
        pnlForm.add(txtUsername, c);
        c.insets = new Insets(10, 0, 6, 0);
        c.gridy = y++;
        
        // Mật khẩu
        pnlForm.add(GiaoDien.label("Mật khẩu", 12, true, GiaoDien.TEXT), c);
        txtPassword = new JPasswordField();
        txtPassword.setBorder(
                new CompoundBorder(new LineBorder(GiaoDien.LINE, 1, true), new EmptyBorder(10, 12, 10, 12)));
        c.insets = new Insets(0, 0, 8, 0);
        c.gridy = y++;
        pnlForm.add(txtPassword, c);
        
        // Xử lý đăng nhập
        JButton btnLogin = GiaoDien.button("ĐĂNG NHẬP", GiaoDien.TEAL, Color.WHITE);
        btnLogin.setFont(GiaoDien.font(13, true));
        btnLogin.setIcon(null);
        btnLogin.setIconTextGap(0);
        btnLogin.setHorizontalAlignment(SwingConstants.CENTER);
        btnLogin.addActionListener(e -> login());
        c.gridy = y++;
        c.insets = new Insets(8, 0, 10, 0);
        pnlForm.add(btnLogin, c);
        pnlRight.add(pnlForm);
        add(pnlRight);
        getRootPane().setDefaultButton(btnLogin);
    }

    // Xác thực người dùng
    private void login() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tài khoản và mật khẩu.");
            return;
        }
        try {
            dao.TaiKhoanDAO.AuthResult result = new dao.TaiKhoanDAO().authenticate(user, pass);
            if (result == null) {
                JOptionPane.showMessageDialog(this,
                        "Sai thông tin đăng nhập hoặc tài khoản đã ngừng hoạt động.");
                return;
            }
            new ManHinhChinh(result).setVisible(true);
            dispose();
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể truy cập SQL Server: " + ex.getMessage());
        }
    }

    // Mở màn hình đăng nhập.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DangNhap().setVisible(true));
    }

    private static final class GiaoDien {
        private static boolean dialogCenteringInstalled;

        // Thiết lập giao diện chung, font, màu và hành vi hộp thoại
        public static void init() {
            if (!dialogCenteringInstalled) {
                Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
                    if (event instanceof java.awt.event.WindowEvent windowEvent
                            && windowEvent.getID() == java.awt.event.WindowEvent.WINDOW_OPENED) {
                        styleAllScrollPanes(windowEvent.getWindow());
                        if (windowEvent.getWindow() instanceof Dialog dialog) {
                            dialog.setLocationRelativeTo(null);
                        }
                    } else if (event instanceof java.awt.event.ContainerEvent containerEvent
                            && containerEvent.getID() == java.awt.event.ContainerEvent.COMPONENT_ADDED) {
                        styleAllScrollPanes(containerEvent.getChild());
                    }
                }, AWTEvent.WINDOW_EVENT_MASK | AWTEvent.CONTAINER_EVENT_MASK);
                dialogCenteringInstalled = true;
            }
            UIManager.put("Label.font", font(13, false));
            UIManager.put("Button.font", font(13, true));
            UIManager.put("TextField.font", font(13, false));
            UIManager.put("Table.font", font(13, false));
            UIManager.put("TableHeader.font", font(12, true));
            UIManager.put("OptionPane.messageFont", font(13, false));
            UIManager.put("ScrollBar.width", 8);
            UIManager.put("ComboBox.background", WHITE);
            UIManager.put("ComboBox.foreground", TEXT);
            UIManager.put("ComboBox.selectionBackground", TEAL);
            UIManager.put("ComboBox.selectionForeground", WHITE);
            UIManager.put("ComboBox.font", font(12, false));
        }

        // Áp dụng kiểu thanh cuộn cho toàn bộ khung cuộn.
        public static void styleAllScrollPanes(Component root) {
            if (root == null)
                return;
            if (root instanceof JScrollPane scr) {
                Component view = scr.getViewport() != null ? scr.getViewport().getView() : null;
                boolean dark = view != null && NAV.equals(view.getBackground());
                styleScroll(scr, dark);
            }
            if (root instanceof Container container) {
                for (Component child : container.getComponents())
                    styleAllScrollPanes(child);
            }
        }

        // Thiết lập kiểu hiển thị cho thanh cuộn.
        public static void styleScroll(JScrollPane scr, boolean dark) {
            if (scr == null)
                return;
            styleScrollBar(scr.getVerticalScrollBar(), dark);
            styleScrollBar(scr.getHorizontalScrollBar(), dark);
            if (scr.getViewport() != null && scr.getViewport().getView() instanceof JTable) {
                scr.setBorder(new LineBorder(LINE, 1, true));
                scr.getViewport().setBackground(WHITE);
            }
        }

        // Tùy chỉnh thanh cuộn
        public static void styleScrollBar(JScrollBar bar, boolean dark) {
            if (bar == null)
                return;
            Boolean applied = (Boolean) bar.getClientProperty("giaoDien.styled");
            Boolean wasDark = (Boolean) bar.getClientProperty("giaoDien.dark");
            if (Boolean.TRUE.equals(applied) && java.util.Objects.equals(wasDark, dark))
                return;
            bar.putClientProperty("giaoDien.styled", Boolean.TRUE);
            bar.putClientProperty("giaoDien.dark", dark);
            bar.setOpaque(true);
            bar.setBackground(dark ? NAV : WHITE);
            bar.setPreferredSize(new Dimension(dark ? 6 : 8, dark ? 6 : 8));
            bar.setUI(new BasicScrollBarUI() {
                // Thiết lập màu thanh cuộn.
                @Override
                protected void configureScrollBarColors() {
                    trackColor = dark ? NAV : new Color(0xF0F4F3);
                    thumbColor = dark ? new Color(0x3D5E63) : new Color(0xB8C9C6);
                }

                // Tạo nút xóa nội dung ô nhập liệu.
                private JButton zeroButton() {
                    JButton b = new JButton();
                    Dimension d = new Dimension(0, 0);
                    b.setPreferredSize(d);
                    b.setMinimumSize(d);
                    b.setMaximumSize(d);
                    b.setBorder(null);
                    b.setFocusable(false);
                    b.setOpaque(false);
                    return b;
                }

                // Tạo nút giảm giá trị.
                @Override
                protected JButton createDecreaseButton(int orientation) {
                    return zeroButton();
                }

                // Tạo nút tăng giá trị. 
                @Override
                protected JButton createIncreaseButton(int orientation) {
                    return zeroButton();
                }

                // Vẽ rãnh của thanh cuộn.
                @Override
                protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(dark ? NAV
                            : (c.getParent() instanceof JScrollPane sp && sp.getViewport() != null
                                    && sp.getViewport().getView() instanceof JTable ? WHITE : BG));
                    g2.fillRect(r.x, r.y, r.width, r.height);
                    g2.dispose();
                }

                // Vẽ tay nắm của thanh cuộn.
                @Override
                protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                    if (r.isEmpty() || !scrollbar.isEnabled())
                        return;
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isDragging || isThumbRollover() ? (dark ? MINT : TEAL)
                            : (dark ? new Color(0x3D5E63) : new Color(0xB8C9C6)));
                    int arc = 6;
                    if (scrollbar.getOrientation() == Adjustable.VERTICAL) {
                        int w = Math.max(4, r.width - 2);
                        int x = r.x + (r.width - w) / 2;
                        g2.fillRoundRect(x, r.y + 1, w, Math.max(12, r.height - 2), arc, arc);
                    } else {
                        int h = Math.max(4, r.height - 2);
                        int y = r.y + (r.height - h) / 2;
                        g2.fillRoundRect(r.x + 1, y, Math.max(12, r.width - 2), h, arc, arc);
                    }
                    g2.dispose();
                }
            });
        }

        private static final Color LINE = new Color(0xE4EBE9);

        private static final Color NAV = new Color(0x13272E);
        private static final Color TEAL = new Color(0x487674);
        private static final Color MINT = new Color(0x83B4AE);
        private static final Color CREAM = new Color(0xE8C69B);
        private static final Color BG = new Color(0xF4F7F6);
        private static final Color WHITE = Color.WHITE;
        private static final Color TEXT = new Color(0x21383C);
        private static final Color MUTED = new Color(0x718387);

        // Tạo phông chữ theo cỡ và kiểu chữ.
        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
        }

        // Tạo đường viền bo góc cho thành phần.
        public static JPanel rounded(Color c, int r) {
            return new JPanel() {
				private static final long serialVersionUID = 1L;

				// Vẽ nội dung và trạng thái hiện tại của thành phần.
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

        // Tạo nút có màu nền, bo góc
        public static JButton button(String s, Color bg, Color fg) {
            JButton btnB = new JButton(s) {
				private static final long serialVersionUID = 1L;

				// Vẽ nội dung và trạng thái hiện tại của thành phần.
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

        // Tạo nhãn giao diện.
        public static JLabel label(String s, int size, boolean bold, Color c) {
            JLabel lblL = new JLabel(s);
            lblL.setFont(font(size, bold));
            lblL.setForeground(c);
            return lblL;
        }

        // Tạo ô nhập liệu 
        public static JTextField field(String hint) {
            JTextField txtT = new JTextField();
            txtT.putClientProperty("JTextField.placeholderText", hint);
            txtT.setFont(font(13, false));
            txtT.setForeground(TEXT);
            txtT.setBorder(
                    new CompoundBorder(new LineBorder(LINE, 1, true), new EmptyBorder(10, 12, 10, 12)));
            return txtT;
        }

        // Tạo khung giao diện dạng thẻ. 
        public static JPanel card(LayoutManager l) {
            JPanel pnlP = rounded(WHITE, 18);
            pnlP.setLayout(l);
            pnlP.setBorder(new EmptyBorder(20, 22, 20, 22));
            pnlP.setOpaque(false);
            return pnlP;
        }
    }
}
