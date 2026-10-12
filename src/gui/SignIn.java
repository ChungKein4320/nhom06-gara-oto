package gui;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
public class SignIn extends JFrame {
    private JComboBox<Role> roles;
    private JTextField username;
    private JPasswordField password;
    public SignIn(){
        Theme.init();
        setTitle("Thinking Auto • Đăng nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1060,650);
        setMinimumSize(new Dimension(850,550));
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1,2));
        JPanel left = new JPanel(new GridBagLayout());
        left.setBackground(Theme.NAV);
        left.setBorder(new EmptyBorder(40, 28, 40, 28));
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        JLabel brandName = Theme.label("THINKING AUTO", 38, true, Theme.MINT);
        JLabel brandSub = Theme.label("XƯỞNG DỊCH VỤ Ô TÔ • GÒ VẤP", 15, true, Theme.CREAM);
        brandName.setAlignmentX(Component.CENTER_ALIGNMENT);
        brandSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        brand.add(brandName);
        brand.add(Box.createVerticalStrut(15));
        brand.add(brandSub);
        left.add(brand);
        add(left);
        JPanel right=new JPanel(new GridBagLayout());
        right.setBackground(Theme.BG);
        JPanel form=Theme.card(new GridBagLayout());
        form.setPreferredSize(new Dimension(385,475));
        GridBagConstraints c=new GridBagConstraints();
        c.gridx=0;
        c.fill=GridBagConstraints.HORIZONTAL;
        c.weightx=1;
        c.insets=new Insets(0,0,13,0);
        int y=0;
        c.gridy=y++;
        form.add(Theme.label("Chào mừng trở lại",25,true,Theme.NAV),c);
        c.gridy=y++;
        form.add(Theme.label("Đăng nhập để tiếp tục công việc tại gara",13,false,Theme.MUTED),c);
        c.insets=new Insets(12,0,6,0);
        c.gridy=y++;
        form.add(Theme.label("Tên đăng nhập",12,true,Theme.TEXT),c);
        username=Theme.field("Nhập tên đăng nhập");
        c.insets=new Insets(0,0,8,0);
        c.gridy=y++;
        form.add(username,c);
        c.insets=new Insets(10,0,6,0);
        c.gridy=y++;
        form.add(Theme.label("Mật khẩu",12,true,Theme.TEXT),c);
        password=new JPasswordField();
        password.setBorder(new CompoundBorder(new LineBorder(Theme.LINE,1,true),new EmptyBorder(10,12,10,12)));
        c.insets=new Insets(0,0,8,0);
        c.gridy=y++;
        form.add(password,c);
        c.insets=new Insets(10,0,6,0);
        c.gridy=y++;
        form.add(Theme.label("Vai trò xem thử giao diện",12,true,Theme.TEXT),c);
        roles=new JComboBox<>(Role.values());
        roles.setFont(Theme.font(13,false));
        c.insets=new Insets(0,0,10,0);
        c.gridy=y++;
        form.add(roles,c);
        JButton login=Theme.button("ĐĂNG NHẬP  →",Theme.TEAL,Color.WHITE);
        login.addActionListener(e->login());
        c.gridy=y++;
        c.insets=new Insets(8,0,10,0);
        form.add(login,c);
        c.gridy=y++;
        form.add(Theme.label("Chế độ xem thử • Chưa kết nối cơ sở dữ liệu",11,false,Theme.ORANGE),c);
        right.add(form);
        add(right);
        getRootPane().setDefaultButton(login);
    }
    private void login(){
        Role r=(Role)roles.getSelectedItem();
        // TODO SQL: Khi có cơ sở dữ liệu, bắt buộc kiểm tra tên đăng nhập + mật khẩu băm,
        // trạng thái hoạt động, vai trò lấy từ tài khoản trong SQL (KHÔNG tin vào vai trò chọn ở đây).
        // TODO SQL: XÓA chế độ bỏ qua xác thực dưới đây trước khi triển khai thực tế.
        new MainFrame(r).setVisible(true);
        dispose();
    }
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->new SignIn().setVisible(true));
    }
}
