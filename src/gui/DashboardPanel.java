package gui;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
public class DashboardPanel extends JPanel {
    public DashboardPanel(Role role,MainFrame frame){
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(30,32,30,32));
        JPanel main=Theme.stack(0);
        JLabel sectionTitle=Theme.label("TRUNG TÂM ĐIỀU HÀNH/THINKING AUTO",11,true,Theme.TEAL);
        sectionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        main.add(sectionTitle);
        Theme.gap(main,9);
        JLabel greeting=Theme.label("Xin chào, "+role.name+"!",29,true,Theme.NAV);
        greeting.setAlignmentX(Component.CENTER_ALIGNMENT);
        main.add(greeting);
        Theme.gap(main,8);
        Theme.gap(main,26);
        JPanel cards=new JPanel(new GridLayout(1,4,14,0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(9999,130));
        String[][] metrics=role==Role.KY_THUAT?new String[][]{
            {
                "XE TIẾP NHẬN","03","Hôm nay"
            },{
                "PHIẾU KIỂM TRA","03","Đã kiểm tra"
            },{
                "BÁO GIÁ","03","Đang theo dõi"
            },{
                "PHIẾU SỬA CHỮA","02","Đang xử lý"
            }
        }
        :role==Role.KHO?new String[][]{
            {
                "LOẠI PHỤ TÙNG","08","Đang quản lý"
            },{
                "SẮP HẾT HÀNG","02","Cần nhập thêm"
            },{
                "PHIẾU NHẬP","02","Đã xác nhận"
            },{
                "YÊU CẦU CẤP","03","Đang theo dõi"
            }
        }
        :role==Role.THU_NGAN?new String[][]{
            {
                "HÓA ĐƠN","02","Trong kỳ"
            },{
                "CHỜ THANH TOÁN","01","Cần xử lý"
            },{
                "DOANH THU","1,03 tr","Minh họa"
            },{
                "KHUYẾN MÃI","03","Đang áp dụng"
            }
        }
        :new String[][]{
            {
                "KHÁCH HÀNG","03","Hồ sơ"
            },{
                "PHƯƠNG TIỆN","03","Đã lưu"
            },{
                "NHÂN VIÊN","04","Đang làm"
            },{
                "DOANH THU","1,03 tr","Minh họa"
            }
        };
        for(int i=0;i<metrics.length;i++){
            JPanel c=Theme.card(new BorderLayout(0,13));
            c.add(Theme.label(metrics[i][0],11,true,Theme.MUTED),BorderLayout.NORTH);
            c.add(Theme.label(metrics[i][1],29,true,i==1?Theme.ORANGE:Theme.TEAL),BorderLayout.CENTER);
            c.add(Theme.label(metrics[i][2],12,false,Theme.MUTED),BorderLayout.SOUTH);
            cards.add(c);
        }
        main.add(cards);
        Theme.gap(main,25);
        JPanel section=Theme.card(new BorderLayout(0,17));
        section.add(Theme.label("Truy cập nhanh theo nhóm đối tượng",19,true,Theme.NAV),BorderLayout.NORTH);
        String[] menu=MenuData.forRole(role);
        JPanel links=new JPanel(new GridLayout(0,3,14,14));
        links.setOpaque(false);
        for(int i=1;i<menu.length;i++){
            String s=menu[i];
            JButton b=Theme.button(s,new Color(0xE5F0EE),Theme.NAV);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.addActionListener(e->frame.open(s));
            links.add(b);
        }
        section.add(links,BorderLayout.CENTER);
        main.add(section);
        Theme.gap(main,22);
        add(main,BorderLayout.NORTH);
    }
}
