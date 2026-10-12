package gui;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
public class PartsSalePanel extends JPanel {
    private int qty=0;
    private JLabel total;
    public PartsSalePanel(){
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(30,32,30,32));
        JPanel root=Theme.stack(0);
        root.add(Theme.label("THU NGÂN/BÁN PHỤ TÙNG",11,true,Theme.TEAL));
        Theme.gap(root,8);
        root.add(Theme.label("Bán phụ tùng tại quầy",26,true,Theme.NAV));
        Theme.gap(root,20);
        JPanel cols=new JPanel(new GridLayout(1,2,18,0));
        cols.setOpaque(false);
        JPanel products=Theme.card(new BorderLayout(0,15));
        products.add(Theme.label("Danh mục phụ tùng",18,true,Theme.NAV),BorderLayout.NORTH);
        JPanel list=new JPanel(new GridLayout(0,1,0,12));
        list.setOpaque(false);
        for(String[] item:new String[][]{
            {
                "Lọc dầu","180.000 đ"
            },{
                "Lọc gió động cơ","220.000 đ"
            },{
                "Má phanh trước","650.000 đ"
            },{
                "Bugi","120.000 đ"
            }
        }){
            JButton b=Theme.button("＋  "+item[0]+"   •   "+item[1],new Color(0xE6F1EF),Theme.TEXT);
            b.addActionListener(e->{
                qty++;total.setText("Sản phẩm đã chọn: "+qty);
            });
            list.add(b);
        }
        products.add(list,BorderLayout.CENTER);
        cols.add(products);
        JPanel cart=Theme.card(new BorderLayout(0,15));
        cart.add(Theme.label("Giỏ hàng",18,true,Theme.NAV),BorderLayout.NORTH);
        total=Theme.label("Sản phẩm đã chọn: 0",17,true,Theme.TEAL);
        cart.add(total,BorderLayout.CENTER);
        JButton confirm=Theme.button("Xác nhận bán hàng",Theme.TEAL,Color.WHITE);
        confirm.addActionListener(e->JOptionPane.showMessageDialog(this,qty==0?"Vui lòng chọn phụ tùng.":"Đã xác nhận bản xem thử. Chưa xuất kho hoặc tạo hóa đơn SQL."));
        cart.add(confirm,BorderLayout.SOUTH);
        cols.add(cart);
        root.add(cols);
        add(root,BorderLayout.NORTH);
    }
}
