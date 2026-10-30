package gui;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
public final class ManHinhChinh extends JFrame {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private final VaiTro role;
    private final String displayName;
    private final String employeeId;
    private final JPanel pnlNav,pnlBody;
    private final JLabel lblTitle;
    private static final String SEARCH_PREFIX="Tìm kiếm: ";
    private boolean searchExpanded;
    private final java.util.Set<String> expandedSections = new java.util.HashSet<>();
    private String current="Tổng quan";

    private static final class NavPanel extends JPanel implements Scrollable {
        @java.io.Serial
        private static final long serialVersionUID = 1L;
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return 40; }
    }
    public ManHinhChinh(dao.TaiKhoanDAO.AuthResult account){
        this.role=account.role;
        this.displayName=account.hoTen;
        this.employeeId=account.maNhanVien;
        setTitle("Thinking Auto • "+role.title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1500,900);
        setMinimumSize(new Dimension(1060,680));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        JPanel pnlSidebar=new JPanel(new BorderLayout());
        pnlSidebar.setBackground(GiaoDien.NAV);
        pnlSidebar.setPreferredSize(new Dimension(258,0));
        JPanel pnlBrand=new JPanel();
        pnlBrand.setBackground(GiaoDien.NAV);
        pnlBrand.setBorder(new EmptyBorder(25,18,18,12));
        pnlBrand.setLayout(new BoxLayout(pnlBrand,BoxLayout.Y_AXIS));
        JLabel lblLogo=GiaoDien.label("Thinking Auto",23,true,GiaoDien.MINT);
        lblLogo.setIcon(BieuTuong.get("phương tiện",23,GiaoDien.MINT));
        lblLogo.setIconTextGap(10);
        pnlBrand.add(lblLogo);
        pnlBrand.add(Box.createVerticalStrut(5));
        pnlBrand.add(GiaoDien.label("QUẢN LÝ GARA  •  GÒ VẤP",10,true,GiaoDien.CREAM));
        pnlSidebar.add(pnlBrand,BorderLayout.NORTH);
        pnlNav=new NavPanel();
        pnlNav.setBackground(GiaoDien.NAV);
        pnlNav.setBorder(new EmptyBorder(6,12,8,12));
        pnlNav.setLayout(new BoxLayout(pnlNav,BoxLayout.Y_AXIS));
        JScrollPane scrNavScroll=new JScrollPane(pnlNav);
        scrNavScroll.setBorder(null);
        scrNavScroll.getViewport().setBackground(GiaoDien.NAV);
        scrNavScroll.getVerticalScrollBar().setUnitIncrement(14);
        scrNavScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrNavScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        GiaoDien.styleScroll(scrNavScroll,true);
        pnlSidebar.add(scrNavScroll,BorderLayout.CENTER);
        JPanel pnlBottom=new JPanel();
        pnlBottom.setBackground(GiaoDien.NAV);
        pnlBottom.setBorder(new EmptyBorder(13,15,18,15));
        pnlBottom.setLayout(new BoxLayout(pnlBottom,BoxLayout.Y_AXIS));
        JPanel pnlProfile=new JPanel(new BorderLayout(9,0));
        pnlProfile.setOpaque(false);
        JLabel lblAvatar=GiaoDien.label("  "+displayName.substring(0,1).toUpperCase()+"  ",18,true,Color.WHITE);
        lblAvatar.setOpaque(true);
        lblAvatar.setBackground(GiaoDien.TEAL);
        lblAvatar.setBorder(new EmptyBorder(10,4,10,4));
        pnlProfile.add(lblAvatar,BorderLayout.WEST);
        JPanel pnlUser=new JPanel(new GridLayout(2,1));
        pnlUser.setOpaque(false);
        JLabel lblUserName=GiaoDien.label(displayName,13,true,Color.WHITE);
        JLabel lblUserRole=GiaoDien.label(role.title,11,true,GiaoDien.MINT);
        pnlUser.add(lblUserName);
        pnlUser.add(lblUserRole);
        pnlProfile.add(pnlUser,BorderLayout.CENTER);
        pnlBottom.add(pnlProfile);
        pnlBottom.add(Box.createVerticalStrut(15));
        JButton btnOut=GiaoDien.button("Đăng xuất",new Color(0x28464A),GiaoDien.CREAM);
        btnOut.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));
        btnOut.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnOut.addActionListener(e->{
            dispose();new DangNhap().setVisible(true);
        });
        pnlBottom.add(btnOut);
        pnlSidebar.add(pnlBottom,BorderLayout.SOUTH);
        add(pnlSidebar,BorderLayout.WEST);
        JPanel pnlMain=new JPanel(new BorderLayout());
        pnlMain.setBackground(GiaoDien.BG);
        JPanel pnlHeader=new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(18,30,18,30));
        lblTitle=GiaoDien.label("Tổng quan",18,true,GiaoDien.NAV);
        pnlHeader.add(lblTitle,BorderLayout.WEST);
        pnlHeader.add(GiaoDien.label("GARA THINKING AUTO    •    "+role.title,11,true,GiaoDien.TEAL),BorderLayout.EAST);
        pnlMain.add(pnlHeader,BorderLayout.NORTH);
        pnlBody=new JPanel(new BorderLayout());
        pnlMain.add(pnlBody,BorderLayout.CENTER);
        add(pnlMain,BorderLayout.CENTER);
        open("Tổng quan");
    }
    private void addNavStrut(int height) {
        Box.Filler filler = (Box.Filler) Box.createVerticalStrut(height);
        filler.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlNav.add(filler);
    }
    private void section(String label){
        addNavStrut(14);
        JLabel lblL=GiaoDien.label("  "+label,10,true,GiaoDien.MINT);
        lblL.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblL.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
        pnlNav.add(lblL);
        addNavStrut(6);
    }
    private String sectionForItem(String s) {
        if (s.equals("Phiếu yêu cầu nhập kho") || s.equals("Yêu cầu nhập kho")
                || s.equals("Phiếu nhập kho phụ tùng") || s.equals("Nhập kho phụ tùng")) {
            return role == VaiTro.QUAN_LY ? "NHÂN VIÊN QUẢN LÝ" : "NHÂN VIÊN KHO";
        }
        if (s.equals("Danh sách yêu cầu nhập")) {
            return "NHÂN VIÊN QUẢN LÝ";
        }
        return switch (s) {
            case "Phiếu tiếp nhận", "Phiếu kiểm tra", "Phiếu báo giá", "Phiếu sửa chữa", "Bảo hành", "Lịch nhắc bảo dưỡng" -> "NHÂN VIÊN KỸ THUẬT";
            case "Tồn kho", "Phiếu cấp phát phụ tùng", "Cấp phát phụ tùng" -> "NHÂN VIÊN KHO";
            case "Hóa đơn", "Lịch sử tích điểm", "Khuyến mãi/Tích điểm", "Bán phụ tùng", "Phụ tùng" -> "NHÂN VIÊN BÁN HÀNG";
            case "Nhà cung cấp", "Nhân viên", "Khách hàng", "Phương tiện", "Danh mục dịch vụ", "Danh mục phụ tùng", "Tài khoản", "Khuyến mãi", "Thống kê" -> "NHÂN VIÊN QUẢN LÝ";
            default -> null;
        };
    }
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
            public int getIconWidth(){ return 12; }
            public int getIconHeight(){ return 12; }
            public void paintIcon(Component c,Graphics g,int x,int y){
                Graphics2D q=(Graphics2D)g.create();
                q.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                q.setColor(textColor);
                if(expanded) q.fillPolygon(new int[]{x+1,x+11,x+6},new int[]{y+3,y+3,y+9},3);
                else q.fillPolygon(new int[]{x+3,x+9,x+3},new int[]{y+1,y+6,y+11},3);
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
    private void duongPhanCach(){
        addNavStrut(10);
        JPanel pnlDuongKe=new JPanel();
        pnlDuongKe.setBackground(new Color(0x355055));
        pnlDuongKe.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDuongKe.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));
        pnlDuongKe.setPreferredSize(new Dimension(232,1));
        pnlNav.add(pnlDuongKe);
        addNavStrut(10);
    }
    private void rebuildNavigation(){
        String page=current;
        pnlNav.removeAll();
        String[] items=DuLieuTrinhDon.forRole(role);
        section("TỔNG QUAN");
        String lastGroup=null;
        JButton activeBtnRef=null;
        for(int i=0;i<items.length;i++){
            String s=items[i];
            if(i==2)duongPhanCach();
            String groupName=sectionForItem(s);
            if(groupName!=null) {
                if(!groupName.equals(lastGroup)) {
                    lastGroup=groupName;
                    accordionSection(groupName, expandedSections.contains(groupName));
                }
                if(!expandedSections.contains(groupName)) continue;
            }
            boolean search=s.equals("Công cụ tìm kiếm");
            boolean active=s.equals(page) || (search && page.startsWith(SEARCH_PREFIX));
            JButton btnB=new JButton(s);
            if(search) {
                final boolean expanded=searchExpanded;
                JLabel arrow=new JLabel(new Icon() {
                    public int getIconWidth(){ return 12; }
                    public int getIconHeight(){ return 12; }
                    public void paintIcon(Component c,Graphics g,int x,int y){
                        Graphics2D q=(Graphics2D)g.create();
                        q.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                        q.setColor(active ? Color.WHITE : new Color(0xD1DFDD));
                        if(expanded) q.fillPolygon(new int[]{x+1,x+11,x+6},new int[]{y+3,y+3,y+9},3);
                        else q.fillPolygon(new int[]{x+3,x+9,x+3},new int[]{y+1,y+6,y+11},3);
                        q.dispose();
                    }
                });
                btnB.setLayout(new BorderLayout());
                btnB.add(arrow,BorderLayout.EAST);
            }
            btnB.setIcon(BieuTuong.get(s,17,active?Color.WHITE:new Color(0xB8D4D0)));
            btnB.setIconTextGap(12);
            btnB.setHorizontalAlignment(SwingConstants.LEFT);
            btnB.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnB.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));
            btnB.setPreferredSize(new Dimension(232,40));
            btnB.setFont(GiaoDien.font(12,true));
            btnB.setFocusPainted(false);
            btnB.setBorder(new EmptyBorder(0,13,0,search ? 16 : 8));
            btnB.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnB.setBackground(active?GiaoDien.TEAL:GiaoDien.NAV);
            btnB.setForeground(active?Color.WHITE:new Color(0xD1DFDD));
            btnB.setOpaque(true);
            if(search) btnB.addActionListener(e->{searchExpanded=!searchExpanded; rebuildNavigation();});
            else btnB.addActionListener(e->open(s));
            pnlNav.add(btnB);
            if(active) activeBtnRef=btnB;
            if(search && searchExpanded) {
                for(String group:ManHinhTimKiem.nhomTheoVaiTro(role)) {
                    String route=SEARCH_PREFIX+group;
                    boolean selected=route.equals(current);
                    JButton child=new JButton("<html><div style='width:142px'>"+group+"</div></html>");
                    child.setToolTipText(route);
                    child.getAccessibleContext().setAccessibleName(route);
                    child.setHorizontalAlignment(SwingConstants.LEFT);
                    child.setAlignmentX(Component.LEFT_ALIGNMENT);
                    child.setFont(GiaoDien.font(12,true));
                    child.setFocusPainted(false);
                    child.setBorder(new EmptyBorder(5,36,5,8));
                    child.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    child.setBackground(selected ? GiaoDien.TEAL : GiaoDien.NAV);
                    child.setForeground(selected ? Color.WHITE : new Color(0xD1DFDD));
                    child.setOpaque(true);
                    child.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));
                    child.setPreferredSize(new Dimension(232,42));
                    child.addActionListener(e->open(route));
                    pnlNav.add(child);
                    if(selected) activeBtnRef=child;
                    addNavStrut(2);
                }
            }
            addNavStrut(2);
        }
        pnlNav.revalidate();
        pnlNav.repaint();
        if(activeBtnRef!=null) {
            final JButton targetBtn=activeBtnRef;
            SwingUtilities.invokeLater(() -> targetBtn.scrollRectToVisible(new Rectangle(0,0,targetBtn.getWidth(),targetBtn.getHeight())));
        }
    }
    public void open(String page){
        java.util.Objects.requireNonNull(page,"Phải xác định trang cần mở.");
        if(page.equals("Tìm kiếm")) {
            searchExpanded=!searchExpanded;
            rebuildNavigation();
            return;
        }
        String searchGroup=page.startsWith(SEARCH_PREFIX) ? page.substring(SEARCH_PREFIX.length()) : null;
        if(searchGroup!=null && !ManHinhTimKiem.nhomTheoVaiTro(role).contains(searchGroup))
            throw new SecurityException("Không có quyền tìm kiếm nhóm dữ liệu: "+searchGroup);
        if(searchGroup!=null) searchExpanded=true;
        String targetSection=sectionForItem(page);
        if(targetSection!=null) expandedSections.add(targetSection);
        current=page;
        lblTitle.setText(page);
        rebuildNavigation();
        pnlBody.removeAll();
        JComponent view = page.equals("Tổng quan")
                ? new ManHinhTongQuan(role, this, displayName)
                : searchGroup!=null ? new ManHinhTimKiem(role,searchGroup)
                : page.equals("Thống kê") ? new ManHinhThongKe()
                : (page.equals("Bán phụ tùng") || page.equals("Phụ tùng")) ? new ManHinhBanPhuTung(employeeId)
                : new ManHinhDuLieu(page, role, employeeId);
        if (view instanceof ManHinhBanPhuTung || view instanceof ManHinhDuLieu || view instanceof ManHinhTimKiem) {
            pnlBody.add(view,BorderLayout.CENTER);
        } else {
            JPanel pnlViewportWrap=new JPanel(new BorderLayout()) {
                private static final long serialVersionUID = 1L;
                @Override public Dimension getPreferredSize() {
                    Dimension d=super.getPreferredSize();
                    if(getParent() instanceof JViewport vp && vp.getWidth()>0) return new Dimension(vp.getWidth(),d.height);
                    return d;
                }
            };
            pnlViewportWrap.setBackground(GiaoDien.BG);
            pnlViewportWrap.add(view,BorderLayout.CENTER);
            JScrollPane scrScroll=new JScrollPane(pnlViewportWrap, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrScroll.setBorder(null);
            scrScroll.getViewport().setBackground(GiaoDien.BG);
            scrScroll.getVerticalScrollBar().setUnitIncrement(20);
            GiaoDien.styleScroll(scrScroll,false);
            pnlBody.add(scrScroll,BorderLayout.CENTER);
        }
        GiaoDien.styleAllScrollPanes(pnlBody);
        pnlBody.revalidate();
        pnlBody.repaint();
    }
}
