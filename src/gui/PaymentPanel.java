package gui;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
public class PaymentPanel extends JPanel {
    public PaymentPanel(){
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(30,32,30,32));
        JPanel root=Theme.stack(0);
        root.add(Theme.label("THU NGÂN/THANH TOÁN",11,true,Theme.TEAL));
        Theme.gap(root,8);
        root.add(Theme.label("Xác nhận thanh toán",26,true,Theme.NAV));
        Theme.gap(root,23);
        JPanel cols=new JPanel(new GridLayout(1,2,18,0));
        cols.setOpaque(false);
        JPanel left=Theme.card(new BorderLayout(0,16));
        left.add(Theme.label("Hóa đơn chờ thanh toán",18,true,Theme.NAV),BorderLayout.NORTH);
        JPanel details=Theme.stack(0);
        details.setBackground(Color.WHITE);
        for(String line:new String[]{
            "HD-2026-001   •   Nguyễn Văn Thành","Biển số: 51H-123.45","Dịch vụ và phụ tùng: 1.165.000 đ","Thuế GTGT: 93.200 đ","","TỔNG CẦN THU: 1.258.200 đ"
        }){
            details.add(Theme.label(line,15,line.startsWith("TỔNG"),line.startsWith("TỔNG")?Theme.ORANGE:Theme.TEXT));
            Theme.gap(details,17);
        }
        left.add(details,BorderLayout.CENTER);
        cols.add(left);
        JPanel right=Theme.card(new BorderLayout(0,15));
        right.add(Theme.label("Phương thức thanh toán",18,true,Theme.NAV),BorderLayout.NORTH);
        JPanel methods=Theme.stack(0);
        methods.setBackground(Color.WHITE);
        JRadioButton cash=new JRadioButton("Tiền mặt",true),transfer=new JRadioButton("Chuyển khoản/mã thanh toán");
        cash.setBackground(Color.WHITE);
        transfer.setBackground(Color.WHITE);
        ButtonGroup group=new ButtonGroup();
        group.add(cash);
        group.add(transfer);
        methods.add(cash);
        Theme.gap(methods,15);
        methods.add(transfer);
        Theme.gap(methods,25);
        methods.add(Theme.label("Thông tin thanh toán sẽ được xác minh khi tích hợp SQL.",12,false,Theme.MUTED));
        right.add(methods,BorderLayout.CENTER);
        JButton confirm=Theme.button("✓  Xác nhận đã thanh toán",Theme.TEAL,Color.WHITE);
        confirm.addActionListener(e->JOptionPane.showMessageDialog(this,"Đã ghi nhận thao tác trong bản xem thử. Chưa thay đổi hóa đơn trong cơ sở dữ liệu.","Thanh toán thử",JOptionPane.INFORMATION_MESSAGE));
        right.add(confirm,BorderLayout.SOUTH);
        cols.add(right);
        root.add(cols);
        add(root,BorderLayout.NORTH);
    }
}
