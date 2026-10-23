package gui;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
public final class ManHinhChinh extends JFrame {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private final VaiTro role;
    private final String displayName;
    private final String employeeId;
    private final JPanel pnlNav,pnlBody;
    private final JLabel lblTitle;
    private String current="Tổng quan";
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
        pnlSidebar.setPreferredSize(new Dimension(246,0));
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
        pnlNav=new JPanel();
        pnlNav.setBackground(GiaoDien.NAV);
        pnlNav.setBorder(new EmptyBorder(7,10,8,10));
        pnlNav.setLayout(new BoxLayout(pnlNav,BoxLayout.Y_AXIS));
        JScrollPane scrNavScroll=new JScrollPane(pnlNav);
        scrNavScroll.setBorder(null);
        scrNavScroll.getVerticalScrollBar().setUnitIncrement(14);
        scrNavScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
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
        JLabel lblUserRole=GiaoDien.label(role.title,11,false,GiaoDien.MINT);
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
    private void section(String label){
        pnlNav.add(Box.createVerticalStrut(19));
        JLabel lblL=GiaoDien.label("  "+label,10,true,GiaoDien.MINT);
        lblL.setMaximumSize(new Dimension(220,22));
        pnlNav.add(lblL);
        pnlNav.add(Box.createVerticalStrut(9));
    }
    private void duongPhanCach(){
        pnlNav.add(Box.createVerticalStrut(14));
        JPanel pnlDuongKe=new JPanel();
        pnlDuongKe.setBackground(new Color(0x355055));
        pnlDuongKe.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));
        pnlDuongKe.setPreferredSize(new Dimension(220,1));
        pnlNav.add(pnlDuongKe);
        pnlNav.add(Box.createVerticalStrut(14));
    }
    public void open(String page){
        current=page;
        lblTitle.setText(page);
        pnlNav.removeAll();
        String[] items=DuLieuTrinhDon.forRole(role);
        section("TỔNG QUAN");
        for(int i=0;i<items.length;i++){
            String s=items[i];
            if(i==2)duongPhanCach();
            if (role == VaiTro.ADMIN && (s.equals("Tồn kho") || s.equals("Hóa đơn") || s.equals("Nhà cung cấp"))) {
                section(s.equals("Tồn kho") ? "NHÂN VIÊN KHO" : s.equals("Hóa đơn") ? "NHÂN VIÊN BÁN HÀNG" : "NHÂN VIÊN QUẢN LÝ");
            }
            if (role == VaiTro.ADMIN && s.equals("Phiếu tiếp nhận")) section("NHÂN VIÊN KỸ THUẬT");
            if (s.equals("Thống kê")) section("BÁO CÁO");
            JButton btnB=new JButton(s);
            btnB.setIcon(BieuTuong.get(s,17,s.equals(page)?Color.WHITE:new Color(0xB8D4D0)));
            btnB.setIconTextGap(12);
            btnB.setHorizontalAlignment(SwingConstants.LEFT);
            btnB.setMaximumSize(new Dimension(222,42));
            btnB.setPreferredSize(new Dimension(222,42));
            btnB.setFont(GiaoDien.font(12,s.equals(page)));
            btnB.setFocusPainted(false);
            btnB.setBorder(new EmptyBorder(0,13,0,5));
            btnB.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnB.setBackground(s.equals(page)?GiaoDien.TEAL:GiaoDien.NAV);
            btnB.setForeground(s.equals(page)?Color.WHITE:new Color(0xD1DFDD));
            btnB.setOpaque(true);
            btnB.addActionListener(e->open(s));
            pnlNav.add(btnB);
            pnlNav.add(Box.createVerticalStrut(3));
        }
        pnlNav.revalidate();
        pnlNav.repaint();
        pnlBody.removeAll();
        JComponent view = page.equals("Tổng quan")
                ? new ManHinhTongQuan(role, this, displayName)
                : page.equals("Tìm kiếm") ? new ManHinhTimKiem(role)
                : page.equals("Thống kê") ? new ManHinhThongKe()
                : page.equals("Phụ tùng") ? new ManHinhBanPhuTung(employeeId)
                : new ManHinhDuLieu(page);
        if (view instanceof ManHinhBanPhuTung) {
            pnlBody.add(view,BorderLayout.CENTER);
        } else {
        JScrollPane scrScroll=new JScrollPane(view, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrScroll.setBorder(null);
        scrScroll.getVerticalScrollBar().setUnitIncrement(20);
        pnlBody.add(scrScroll,BorderLayout.CENTER);
        }
        pnlBody.revalidate();
        pnlBody.repaint();
    }
}
