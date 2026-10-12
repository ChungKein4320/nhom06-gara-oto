package gui;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
public class InvoicePanel extends JPanel {
    public InvoicePanel(){
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(28,30,30,30));
        JPanel root=Theme.stack(0);
        root.add(Theme.label("THU NGÂN/HÓA ĐƠN",11,true,Theme.TEAL));
        Theme.gap(root,9);
        root.add(Theme.label("Hóa đơn thanh toán",26,true,Theme.NAV));
        Theme.gap(root,7);
        root.add(Theme.label("Chi tiết tiền công dịch vụ và phụ tùng được tách thành hai bảng theo phiếu sửa chữa.",13,false,Theme.MUTED));
        Theme.gap(root,23);
        JPanel info=Theme.card(new GridLayout(2,3,18,16));
        for(String[] item:new String[][]{
            {
                "SỐ HÓA ĐƠN","HD002"
            },{
                "KHÁCH HÀNG","Phạm Văn Minh"
            },{
                "BIỂN SỐ","51H-456.78"
            },{
                "PHIẾU SỬA CHỮA","SC002"
            },{
                "NGÀY LẬP","19/09/2026"
            },{
                "TRẠNG THÁI","Đã thanh toán"
            }
        }){
            JPanel c=new JPanel(new GridLayout(2,1));
            c.setOpaque(false);
            c.add(Theme.label(item[0],11,true,Theme.MUTED));
            c.add(Theme.label(item[1],15,true,Theme.TEXT));
            info.add(c);
        }
        root.add(info);
        Theme.gap(root,17);
        root.add(tableCard("I. DỊCH VỤ/TIỀN CÔNG",new String[]{
            "STT","Mã dịch vụ","Tên dịch vụ","Số lượng","Đơn giá","Thành tiền"
        },new Object[][]{
            {
                "1","DV003","Kiểm tra hệ thống phanh","1","300.000 đ","300.000 đ"
            }
        }));
        Theme.gap(root,15);
        root.add(tableCard("II. PHỤ TÙNG SỬ DỤNG",new String[]{
            "STT","Mã phụ tùng","Tên phụ tùng","Số lượng","Đơn giá","Thành tiền"
        },new Object[][]{
            {
                "1","PT003","Má phanh trước","1","650.000 đ","650.000 đ"
            }
        }));
        Theme.gap(root,17);
        JPanel total=Theme.card(new GridLayout(4,2,0,8));
        for(String[] s:new String[][]{
            {
                "Tổng tiền dịch vụ","300.000 đ"
            },{
                "Tổng tiền phụ tùng","650.000 đ"
            },{
                "Thuế GTGT (8%)","76.000 đ"
            },{
                "TỔNG THANH TOÁN","1.026.000 đ"
            }
        }){
            total.add(Theme.label(s[0],14,s[0].startsWith("TỔNG"),Theme.TEXT));
            total.add(Theme.label(s[1],16,true,s[0].startsWith("TỔNG")?Theme.ORANGE:Theme.TEAL));
        }
        root.add(total);
        Theme.gap(root,12);
        JButton print=Theme.button("In hóa đơn",Theme.TEAL,Color.WHITE);
        print.setAlignmentX(LEFT_ALIGNMENT);
        print.addActionListener(e->JOptionPane.showMessageDialog(this,"Bản xem thử chưa kết nối máy in. Sẽ bổ sung chức năng in khi tích hợp dữ liệu."));
        root.add(print);
        add(root,BorderLayout.NORTH);
    }
    private JPanel tableCard(String title,String[] cols,Object[][] data){
        JPanel c=Theme.card(new BorderLayout(0,13));
        c.add(Theme.label(title,16,true,Theme.NAV),BorderLayout.NORTH);
        DefaultTableModel m=new DefaultTableModel(data,cols);
        JTable t=Theme.table(m);
        JScrollPane s=new JScrollPane(t);
        s.setPreferredSize(new Dimension(600,95));
        s.setBorder(new LineBorder(Theme.LINE));
        c.add(s,BorderLayout.CENTER);
        return c;
    }
}
