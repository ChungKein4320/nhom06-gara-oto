package gui;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
public class StatisticsPanel extends JPanel {
    public StatisticsPanel(){
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(30,32,30,32));
        JPanel root=Theme.stack(0);
        root.add(Theme.label("QUẢN LÝ/THỐNG KÊ",11,true,Theme.TEAL));
        Theme.gap(root,9);
        root.add(Theme.label("Thống kê hoạt động gara",26,true,Theme.NAV));
        Theme.gap(root,20);
        JPanel filter=Theme.card(new FlowLayout(FlowLayout.LEFT,15,8));
        filter.add(Theme.label("Loại thống kê",13,true,Theme.TEXT));
        JComboBox<String> types=new JComboBox<>(new String[]{
            "Thống kê doanh thu","Thống kê khách hàng sử dụng","Thống kê dịch vụ sửa chữa"
        });
        filter.add(types);
        filter.add(Theme.label("Từ ngày",13,true,Theme.TEXT));
        JTextField from=Theme.field("");
        from.setText("01/09/2026");
        from.setColumns(9);
        filter.add(from);
        filter.add(Theme.label("Đến ngày",13,true,Theme.TEXT));
        JTextField to=Theme.field("");
        to.setText("30/09/2026");
        to.setColumns(9);
        filter.add(to);
        JButton show=Theme.button("Xem thống kê",Theme.TEAL,Color.WHITE);
        filter.add(show);
        root.add(filter);
        Theme.gap(root,20);
        JPanel card=Theme.card(new BorderLayout(0,15));
        JLabel title=Theme.label("Doanh thu theo tháng • Dữ liệu minh họa",18,true,Theme.NAV);
        card.add(title,BorderLayout.NORTH);
        JPanel graph=new JPanel(){
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D q=(Graphics2D)g.create();
                q.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int w=getWidth(),h=getHeight();
                q.setColor(Theme.LINE);
                for(int i=1;i<5;i++){
                    int y=i*h/5;
                    q.drawLine(30,y,w-25,y);
                }
                int[] heights={
                    35,58,44,71,63,83,52,91
                };
                int bar=Math.max(25,(w-110)/19);
                for(int i=0;i<heights.length;i++){
                    int x=60+i*(w-110)/9;
                    int bh=(h-65)*heights[i]/100;
                    q.setColor(i==7?Theme.ORANGE:Theme.MINT);
                    q.fillRoundRect(x,h-35-bh,bar,bh,8,8);
                    q.setColor(Theme.TEXT);
                    q.setFont(Theme.font(12,false));
                    q.drawString("T"+(i+1),x+5,h-12);
                }
                q.dispose();
            }
        };
        graph.setBackground(Color.WHITE);
        graph.setPreferredSize(new Dimension(720,310));
        card.add(graph,BorderLayout.CENTER);
        root.add(card);
        types.addActionListener(e->{
            title.setText((String)types.getSelectedItem()+" • Dữ liệu minh họa");graph.repaint();
        });
        show.addActionListener(e->graph.repaint());
        add(root,BorderLayout.NORTH);
    }
}
