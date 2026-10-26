package gui;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
public final class DangNhap extends JFrame {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    public DangNhap(){
        GiaoDien.init();
        setTitle("Thinking Auto • Đăng nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1060,650);
        setMinimumSize(new Dimension(850,550));
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1,2));
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
        JPanel pnlRight=new JPanel(new GridBagLayout());
        pnlRight.setBackground(GiaoDien.BG);
        JPanel pnlForm=GiaoDien.card(new GridBagLayout());
        pnlForm.setPreferredSize(new Dimension(385,475));
        GridBagConstraints c=new GridBagConstraints();
        c.gridx=0;
        c.fill=GridBagConstraints.HORIZONTAL;
        c.weightx=1;
        c.insets=new Insets(0,0,13,0);
        int y=0;
        c.gridy=y++;
        JLabel lblWelcome=GiaoDien.label("Chào mừng trở lại",25,true,GiaoDien.NAV);
        lblWelcome.setHorizontalAlignment(SwingConstants.CENTER);
        pnlForm.add(lblWelcome,c);
        c.gridy=y++;
        JLabel lblIntro=GiaoDien.label("Đăng nhập để tiếp tục công việc tại gara",13,false,GiaoDien.MUTED);
        lblIntro.setHorizontalAlignment(SwingConstants.CENTER);
        pnlForm.add(lblIntro,c);
        c.insets=new Insets(12,0,6,0);
        c.gridy=y++;
        pnlForm.add(GiaoDien.label("Tên đăng nhập",12,true,GiaoDien.TEXT),c);
        txtUsername=GiaoDien.field("Nhập tên đăng nhập");
        c.insets=new Insets(0,0,8,0);
        c.gridy=y++;
        pnlForm.add(txtUsername,c);
        c.insets=new Insets(10,0,6,0);
        c.gridy=y++;
        pnlForm.add(GiaoDien.label("Mật khẩu",12,true,GiaoDien.TEXT),c);
        txtPassword=new JPasswordField();
        txtPassword.setBorder(new CompoundBorder(new LineBorder(GiaoDien.LINE,1,true),new EmptyBorder(10,12,10,12)));
        c.insets=new Insets(0,0,8,0);
        c.gridy=y++;
        pnlForm.add(txtPassword,c);
        JButton btnLogin=GiaoDien.button("ĐĂNG NHẬP",GiaoDien.TEAL,Color.WHITE);
        btnLogin.setFont(GiaoDien.font(13,true));
        btnLogin.setIcon(null);
        btnLogin.setIconTextGap(0);
        btnLogin.setHorizontalAlignment(SwingConstants.CENTER);
        btnLogin.addActionListener(e->login());
        c.gridy=y++;
        c.insets=new Insets(8,0,10,0);
        pnlForm.add(btnLogin,c);
        pnlRight.add(pnlForm);
        add(pnlRight);
        getRootPane().setDefaultButton(btnLogin);
    }
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
                JOptionPane.showMessageDialog(this, "Sai thông tin đăng nhập hoặc tài khoản đã ngừng hoạt động.");
                return;
            }
            new ManHinhChinh(result).setVisible(true);
            dispose();
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể truy cập SQL Server: " + ex.getMessage());
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(()->new DangNhap().setVisible(true));
    }
}
