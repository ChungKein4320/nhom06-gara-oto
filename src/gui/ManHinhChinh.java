package gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;


public final class ManHinhChinh extends JFrame {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private final VaiTro role;
    private final String displayName;
    private final String employeeId;
    private final JPanel pnlNav, pnlBody;
    private final JLabel lblTitle;
    private static final String SEARCH_PREFIX = "Tìm kiếm: ";
    private boolean searchExpanded;
    private final java.util.Set<String> expandedSections = new java.util.HashSet<>();
    private String current = "Tổng quan";

    private static final class NavPanel extends JPanel implements Scrollable {
        @java.io.Serial
        private static final long serialVersionUID = 1L;

        // Lấy scrollable tracks viewport width. 
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        // Lấy scrollable tracks viewport height. 
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        // Lấy preferred scrollable viewport kích thước. 
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        // Lấy scrollable unit increment. 
        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        // Lấy scrollable block increment.
        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 40;
        }
    }

    public ManHinhChinh(dao.TaiKhoanDAO.AuthResult account) {
        // Thông tin đăng nhập
    	this.role = account.role;
        this.displayName = account.hoTen;
        this.employeeId = account.maNhanVien;
        
        setTitle("Thinking Auto • " + role.title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1500, 900);
        setMinimumSize(new Dimension(1060, 680));
        setLocationRelativeTo(null);
        setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        // Sidebar
        JPanel pnlSidebar = new JPanel(new BorderLayout());
        pnlSidebar.setBackground(GiaoDien.NAV);
        pnlSidebar.setPreferredSize(new Dimension(258, 0));
        JPanel pnlBrand = new JPanel();
        pnlBrand.setBackground(GiaoDien.NAV);
        pnlBrand.setBorder(new EmptyBorder(25, 18, 18, 12));
        pnlBrand.setLayout(new BoxLayout(pnlBrand, BoxLayout.Y_AXIS));
        JLabel lblLogo = GiaoDien.label("Thinking Auto", 23, true, GiaoDien.MINT);
        lblLogo.setIcon(BieuTuong.get("phương tiện", 23, GiaoDien.MINT));
        lblLogo.setIconTextGap(10);
        pnlBrand.add(lblLogo);
        pnlBrand.add(Box.createVerticalStrut(5));
        pnlBrand.add(GiaoDien.label("QUẢN LÝ GARA  •  GÒ VẤP", 10, true, GiaoDien.CREAM));
        pnlSidebar.add(pnlBrand, BorderLayout.NORTH);
        pnlNav = new NavPanel();
        pnlNav.setBackground(GiaoDien.NAV);
        pnlNav.setBorder(new EmptyBorder(6, 12, 8, 12));
        pnlNav.setLayout(new BoxLayout(pnlNav, BoxLayout.Y_AXIS));
        JScrollPane scrNavScroll = new JScrollPane(pnlNav);
        scrNavScroll.setBorder(null);
        scrNavScroll.getViewport().setBackground(GiaoDien.NAV);
        scrNavScroll.getVerticalScrollBar().setUnitIncrement(14);
        scrNavScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrNavScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        GiaoDien.styleScroll(scrNavScroll, true);
        pnlSidebar.add(scrNavScroll, BorderLayout.CENTER);
        // Thông tin nhân viên
        JPanel pnlBottom = new JPanel();
        pnlBottom.setBackground(GiaoDien.NAV);
        pnlBottom.setBorder(new EmptyBorder(13, 15, 18, 15));
        pnlBottom.setLayout(new BoxLayout(pnlBottom, BoxLayout.Y_AXIS));
        JPanel pnlProfile = new JPanel(new BorderLayout(9, 0));
        pnlProfile.setOpaque(false);
        JLabel lblAvatar = GiaoDien.label("  " + displayName.substring(0, 1).toUpperCase() + "  ", 18, true,
                Color.WHITE);
        lblAvatar.setOpaque(true);
        lblAvatar.setBackground(GiaoDien.TEAL);
        lblAvatar.setBorder(new EmptyBorder(10, 4, 10, 4));
        pnlProfile.add(lblAvatar, BorderLayout.WEST);
        JPanel pnlUser = new JPanel(new GridLayout(2, 1));
        pnlUser.setOpaque(false);
        JLabel lblUserName = GiaoDien.label(displayName, 13, true, Color.WHITE);
        JLabel lblUserRole = GiaoDien.label(role.title, 11, true, GiaoDien.MINT);
        pnlUser.add(lblUserName);
        pnlUser.add(lblUserRole);
        pnlProfile.add(pnlUser, BorderLayout.CENTER);
        pnlBottom.add(pnlProfile);
        pnlBottom.add(Box.createVerticalStrut(15));
        // Đăng xuất
        JButton btnOut = GiaoDien.button("Đăng xuất", new Color(0x28464A), GiaoDien.CREAM);
        btnOut.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnOut.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnOut.addActionListener(e -> {
            dispose();
            new DangNhap().setVisible(true);
        });
        pnlBottom.add(btnOut);
        pnlSidebar.add(pnlBottom, BorderLayout.SOUTH);
        add(pnlSidebar, BorderLayout.WEST);
        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.setBackground(GiaoDien.BG);
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(18, 30, 18, 30));
        lblTitle = GiaoDien.label("Tổng quan", 18, true, GiaoDien.NAV);
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(GiaoDien.label("GARA THINKING AUTO    •    " + role.title, 11, true, GiaoDien.TEAL),
                BorderLayout.EAST);
        pnlMain.add(pnlHeader, BorderLayout.NORTH);
        pnlBody = new JPanel(new BorderLayout());
        pnlMain.add(pnlBody, BorderLayout.CENTER);
        add(pnlMain, BorderLayout.CENTER);
        open("Tổng quan");
    }

    // Thêm điều hướng strut. 
    private void addNavStrut(int height) {
        Box.Filler filler = (Box.Filler) Box.createVerticalStrut(height);
        filler.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlNav.add(filler);
    }

    // Tạo một khu vực giao diện có tiêu đề. 
    private void section(String label) {
        addNavStrut(14);
        JLabel lblL = GiaoDien.label("  " + label, 10, true, GiaoDien.MINT);
        lblL.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblL.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        pnlNav.add(lblL);
        addNavStrut(6);
    }

    // Tìm khu vực menu chứa chức năng đã chọn. 
    private String sectionForItem(String s) {
        if (s.equals("Phiếu yêu cầu nhập kho") || s.equals("Yêu cầu nhập kho")
                || s.equals("Phiếu nhập kho phụ tùng") || s.equals("Nhập kho phụ tùng")) {
            return role == VaiTro.QUAN_LY ? "NHÂN VIÊN QUẢN LÝ" : "NHÂN VIÊN KHO";
        }
        if (s.equals("Danh sách yêu cầu nhập")) {
            return "NHÂN VIÊN QUẢN LÝ";
        }
        return switch (s) {
        case "Phiếu tiếp nhận", "Phiếu kiểm tra", "Phiếu báo giá", "Phiếu sửa chữa", "Bảo hành",
                "Lịch nhắc bảo dưỡng" ->
            "NHÂN VIÊN KỸ THUẬT";
        case "Tồn kho", "Phiếu cấp phát phụ tùng", "Cấp phát phụ tùng" -> "NHÂN VIÊN KHO";
        case "Hóa đơn", "Lịch sử tích điểm", "Khuyến mãi/Tích điểm", "Bán phụ tùng", "Phụ tùng" ->
            "NHÂN VIÊN BÁN HÀNG";
        case "Nhà cung cấp", "Nhân viên", "Khách hàng", "Phương tiện", "Danh mục dịch vụ",
                "Danh mục phụ tùng", "Tài khoản", "Khuyến mãi", "Thống kê" ->
            "NHÂN VIÊN QUẢN LÝ";
        default -> null;
        };
    }

    // Tạo mục điều hướng có thể thu gọn.
    private void accordionSection(String label, boolean expanded) {
        addNavStrut(6);
        Color textColor = GiaoDien.MINT;
        Color expandedBg = new Color(0x1D3840);
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(true);
        row.setBackground(expanded ? expandedBg : GiaoDien.NAV);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        row.setPreferredSize(new Dimension(232, 34));
        JLabel title = GiaoDien.label("  " + label, 11, true, textColor);
        JButton btnArrow = new JButton(new Icon() {
            @Override
            public int getIconWidth() {
                return 12;
            }

            @Override
            public int getIconHeight() {
                return 12;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D q = (Graphics2D) g.create();
                q.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                q.setColor(textColor);
                if (expanded)
                    q.fillPolygon(new int[] { x + 1, x + 11, x + 6 }, new int[] { y + 3, y + 3, y + 9 }, 3);
                else
                    q.fillPolygon(new int[] { x + 3, x + 9, x + 3 }, new int[] { y + 1, y + 6, y + 11 }, 3);
                q.dispose();
            }
        });
        btnArrow.setToolTipText((expanded ? "Thu gọn " : "Mở rộng ") + label);
        btnArrow.getAccessibleContext().setAccessibleName((expanded ? "Thu gọn " : "Mở rộng ") + label);
        btnArrow.setPreferredSize(new Dimension(30, 34));
        btnArrow.setFocusPainted(false);
        btnArrow.setContentAreaFilled(false);
        btnArrow.setBorder(new EmptyBorder(0, 4, 0, 8));
        btnArrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnArrow.setBackground(row.getBackground());
        btnArrow.setOpaque(false);
        btnArrow.addActionListener(e -> {
            if (expandedSections.contains(label)) {
                expandedSections.remove(label);
            } else {
                expandedSections.add(label);
            }
            rebuildNavigation();
        });
        row.add(title, BorderLayout.CENTER);
        row.add(btnArrow, BorderLayout.EAST);
        pnlNav.add(row);
        addNavStrut(4);
    }

    // Tạo đường phân cách trong giao diện.
    private void duongPhanCach() {
        addNavStrut(10);
        JPanel pnlDuongKe = new JPanel();
        pnlDuongKe.setBackground(new Color(0x355055));
        pnlDuongKe.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDuongKe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        pnlDuongKe.setPreferredSize(new Dimension(232, 1));
        pnlNav.add(pnlDuongKe);
        addNavStrut(10);
    }

    // Menu theo vai trò. 
    private void rebuildNavigation() {
        String page = current;
        // Xóa menu cũ
        pnlNav.removeAll();
        String[] items = DuLieuTrinhDon.forRole(role);
        section("TỔNG QUAN");
        String lastGroup = null;
        JButton activeBtnRef = null;
        for (int i = 0; i < items.length; i++) {
            String s = items[i];
            if (i == 2)
                duongPhanCach();
            String groupName = sectionForItem(s);
            if (groupName != null) {
                if (!groupName.equals(lastGroup)) {
                    lastGroup = groupName;
                    accordionSection(groupName, expandedSections.contains(groupName));
                }
                if (!expandedSections.contains(groupName))
                    continue;
            }
            boolean search = s.equals("Công cụ tìm kiếm");
            boolean active = s.equals(page) || (search && page.startsWith(SEARCH_PREFIX));
            JButton btnB = new JButton(s);
            if (search) {
                final boolean expanded = searchExpanded;
                JLabel arrow = new JLabel(new Icon() {
                    // Lấy biểu tượng width.
                    @Override
                    public int getIconWidth() {
                        return 12;
                    }

                    // Lấy biểu tượng height. 
                    @Override
                    public int getIconHeight() {
                        return 12;
                    }

                    // Vẽ biểu tượng lên vị trí được chỉ định.
                    @Override
                    public void paintIcon(Component c, Graphics g, int x, int y) {
                        Graphics2D q = (Graphics2D) g.create();
                        q.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
                        q.setColor(active ? Color.WHITE : new Color(0xD1DFDD));
                        if (expanded)
                            q.fillPolygon(new int[] { x + 1, x + 11, x + 6 },
                                    new int[] { y + 3, y + 3, y + 9 }, 3);
                        else
                            q.fillPolygon(new int[] { x + 3, x + 9, x + 3 },
                                    new int[] { y + 1, y + 6, y + 11 }, 3);
                        q.dispose();
                    }
                });
                btnB.setLayout(new BorderLayout());
                btnB.add(arrow, BorderLayout.EAST);
            }
            btnB.setIcon(BieuTuong.get(s, 17, active ? Color.WHITE : new Color(0xB8D4D0)));
            btnB.setIconTextGap(12);
            btnB.setHorizontalAlignment(SwingConstants.LEFT);
            btnB.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnB.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            btnB.setPreferredSize(new Dimension(232, 40));
            btnB.setFont(GiaoDien.font(12, true));
            btnB.setFocusPainted(false);
            btnB.setBorder(new EmptyBorder(0, 13, 0, search ? 16 : 8));
            btnB.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnB.setBackground(active ? GiaoDien.TEAL : GiaoDien.NAV);
            btnB.setForeground(active ? Color.WHITE : new Color(0xD1DFDD));
            btnB.setOpaque(true);
            if (search)
                btnB.addActionListener(e -> {
                    searchExpanded = !searchExpanded;
                    rebuildNavigation();
                });
            else
                btnB.addActionListener(e -> open(s));
            pnlNav.add(btnB);
            if (active)
                activeBtnRef = btnB;
            if (search && searchExpanded) {
                for (String group : ManHinhTimKiem.nhomTheoVaiTro(role)) {
                    String route = SEARCH_PREFIX + group;
                    boolean selected = route.equals(current);
                    JButton child = new JButton("<html><div style='width:142px'>" + group + "</div></html>");
                    child.setToolTipText(route);
                    child.getAccessibleContext().setAccessibleName(route);
                    child.setHorizontalAlignment(SwingConstants.LEFT);
                    child.setAlignmentX(Component.LEFT_ALIGNMENT);
                    child.setFont(GiaoDien.font(12, true));
                    child.setFocusPainted(false);
                    child.setBorder(new EmptyBorder(5, 36, 5, 8));
                    child.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    child.setBackground(selected ? GiaoDien.TEAL : GiaoDien.NAV);
                    child.setForeground(selected ? Color.WHITE : new Color(0xD1DFDD));
                    child.setOpaque(true);
                    child.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
                    child.setPreferredSize(new Dimension(232, 42));
                    child.addActionListener(e -> open(route));
                    pnlNav.add(child);
                    if (selected)
                        activeBtnRef = child;
                    addNavStrut(2);
                }
            }
            addNavStrut(2);
        }
        pnlNav.revalidate();
        pnlNav.repaint();
        if (activeBtnRef != null) {
            final JButton targetBtn = activeBtnRef;
            SwingUtilities.invokeLater(() -> targetBtn
                    .scrollRectToVisible(new Rectangle(0, 0, targetBtn.getWidth(), targetBtn.getHeight())));
        }
    }

    // Mở nghiệp vụ tương ứng. 
    public void open(String page) {
        java.util.Objects.requireNonNull(page, "Phải xác định trang cần mở.");
        if (page.equals("Tìm kiếm")) {
            searchExpanded = !searchExpanded;
            rebuildNavigation();
            return;
        }
        String searchGroup = page.startsWith(SEARCH_PREFIX) ? page.substring(SEARCH_PREFIX.length()) : null;
        if (searchGroup != null && !ManHinhTimKiem.nhomTheoVaiTro(role).contains(searchGroup))
            throw new SecurityException("Không có quyền tìm kiếm nhóm dữ liệu: " + searchGroup);
        if (searchGroup != null)
            searchExpanded = true;
        String targetSection = sectionForItem(page);
        if (targetSection != null)
            expandedSections.add(targetSection);
        current = page;
        lblTitle.setText(page);
        rebuildNavigation();
        pnlBody.removeAll();
        JComponent view = page.equals("Tổng quan") ? new ManHinhTongQuan(role, this, displayName)
                : searchGroup != null ? new ManHinhTimKiem(role, searchGroup)
                        : page.equals("Thống kê") ? new ManHinhThongKe()
                                : (page.equals("Bán phụ tùng") || page.equals("Phụ tùng"))
                                        ? new ManHinhBanPhuTung(employeeId)
                                        : DieuHuongManHinh.tao(page, role, employeeId);
        if (view instanceof ManHinhBanPhuTung || DieuHuongManHinh.tableFor(page) != null
                || view instanceof ManHinhTimKiem) {
            pnlBody.add(view, BorderLayout.CENTER);
        } else {
            JPanel pnlViewportWrap = new JPanel(new BorderLayout()) {
                private static final long serialVersionUID = 1L;

                // Tính kích thước ưu tiên của thành phần. 
                @Override
                public Dimension getPreferredSize() {
                    Dimension d = super.getPreferredSize();
                    if (getParent() instanceof JViewport vp && vp.getWidth() > 0)
                        return new Dimension(vp.getWidth(), d.height);
                    return d;
                }
            };
            pnlViewportWrap.setBackground(GiaoDien.BG);
            pnlViewportWrap.add(view, BorderLayout.CENTER);
            JScrollPane scrScroll = new JScrollPane(pnlViewportWrap,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrScroll.setBorder(null);
            scrScroll.getViewport().setBackground(GiaoDien.BG);
            scrScroll.getVerticalScrollBar().setUnitIncrement(20);
            GiaoDien.styleScroll(scrScroll, false);
            pnlBody.add(scrScroll, BorderLayout.CENTER);
        }
        GiaoDien.styleAllScrollPanes(pnlBody);
        pnlBody.revalidate();
        pnlBody.repaint();
    }

    private static final class GiaoDien {

        private static final Color NAV = new Color(0x13272E);
        private static final Color TEAL = new Color(0x487674);
        private static final Color MINT = new Color(0x83B4AE);
        private static final Color CREAM = new Color(0xE8C69B);
        private static final Color BG = new Color(0xF4F7F6);
        private static final Color WHITE = Color.WHITE;
        private static final Color LINE = new Color(0xE4EBE9);

        // Tạo phông chữ theo cỡ và kiểu chữ.
        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
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
            styleScrollBar(scr.getVerticalScrollBar(), dark); //cuộn dọc
            styleScrollBar(scr.getHorizontalScrollBar(), dark); //cuộn ngang
            // Nếu là table thì có viền mảnh và nền trắng
            if (scr.getViewport() != null && scr.getViewport().getView() instanceof JTable) {
                scr.setBorder(new LineBorder(LINE, 1, true));
                scr.getViewport().setBackground(WHITE);
            }
        }

        // Thiết lập giao diện cho một thanh cuộn. 
        public static void styleScrollBar(JScrollBar bar, boolean dark) {
            if (bar == null)
                return;
            // Kiểm tra đã áp dụng đúng giao diện chưa
            Boolean applied = (Boolean) bar.getClientProperty("giaoDien.styled");
            Boolean wasDark = (Boolean) bar.getClientProperty("giaoDien.dark");
            if (Boolean.TRUE.equals(applied) && java.util.Objects.equals(wasDark, dark))
                return;
            bar.putClientProperty("giaoDien.styled", Boolean.TRUE);
            bar.putClientProperty("giaoDien.dark", dark);
            bar.setOpaque(true);
            // Nền và kích thước
            bar.setBackground(dark ? NAV : WHITE);
            bar.setPreferredSize(new Dimension(dark ? 6 : 8, dark ? 6 : 8));
            bar.setUI(new BasicScrollBarUI() {
                // Thiết lập màu thanh cuộn (màu trong và ngoài).
                @Override
                protected void configureScrollBarColors() {
                    trackColor = dark ? NAV : new Color(0xF0F4F3);
                    thumbColor = dark ? new Color(0x3D5E63) : new Color(0xB8C9C6);
                }

                //  Làm thanh cuộn không còn mũi tên lên xuống 
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

                // Vẽ rãnh cuộn. 
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
                    // Bị rỗng hoặc bị vô hiệu hóa
                	if (r.isEmpty() || !scrollbar.isEnabled())
                        return;
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Check có đang kéo hay chuột hay nằm trên tay nắm
                    g2.setColor(isDragging || isThumbRollover() ? (dark ? MINT : TEAL)
                            : (dark ? new Color(0x3D5E63) : new Color(0xB8C9C6)));
                    int arc = 6;
                    // Xác định thanh cuộn dọc và ngang
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

        // Tạo nút giao diện.
        public static JButton button(String s, Color bg, Color fg) {
            JButton btnB = new JButton(s) {
				private static final long serialVersionUID = 1L;

				// ===== PHƯƠNG THỨC: paintComponent(Graphics) — Vẽ nội dung và trạng thái hiện tại của thành phần. =====
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D q = (Graphics2D) g.create();
                    q.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Để chuột vào nút màu nền sáng hơn
                    q.setColor(getModel().isRollover() ? bg.brighter() : bg);
                    q.fillRoundRect(0, 0, getWidth(), getHeight(), 13, 13);
                    q.dispose();
                    super.paintComponent(g);
                }
            };
            btnB.setForeground(fg);
            // Áp dụng biểu tượng phù hợp với nội dung nút này
            BieuTuong.apply(btnB, s, fg);
            btnB.setContentAreaFilled(false);
            btnB.setBorder(BorderFactory.createEmptyBorder(10, 17, 10, 17));
            btnB.setFocusPainted(false);
            // Đưa chuột vào nút con trỏ trở thành bàn tay
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
    }
}
