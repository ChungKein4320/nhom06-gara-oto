package gui;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.Locale;
/** Các bố cục nghiệp vụ riêng, không tái sử dụng một bảng duy nhất cho mọi màn hình. */ public class SpecialPanel extends JPanel {
    private final String name;
    public static boolean supports(String n){
        String normalizedName=n.toLowerCase(Locale.ROOT);
        return n.contains("Phiếu")||normalizedName.contains("bảo hành")||n.contains("Lịch nhắc")||normalizedName.contains("tồn kho")||n.contains("Cấp phát")||n.contains("Nhập kho")||normalizedName.contains("lịch sử")||normalizedName.contains("khuyến mãi");
    }
    public SpecialPanel(String n){
        name=n;
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(26,24,30,24));
        JPanel root=Theme.stack(0);
        JLabel breadcrumb = Theme.label("THINKING AUTO/" + n.toUpperCase(), 11, true, Theme.TEAL);
        breadcrumb.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(breadcrumb);
        Theme.gap(root,8);
        JLabel pageTitle=Theme.label(n,26,true,Theme.NAV);
        pageTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(pageTitle);
        Theme.gap(root,8);
        JLabel subtitle=Theme.label(description(),13,false,Theme.MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        root.add(subtitle);
        Theme.gap(root,20);
        if(n.contains("Phiếu"))forms(root);
        else if(n.contains("tồn kho"))stock(root);
        else if(n.contains("Cấp phát")||n.contains("Nhập kho"))warehouse(root);
        else if(n.toLowerCase(Locale.ROOT).contains("bảo hành")||n.contains("lịch sử"))lookup(root);
        else if(n.contains("Lịch nhắc"))reminder(root);
        else promotions(root);
        add(root,BorderLayout.NORTH);
    }
    private String description(){
        if(name.contains("Phiếu"))return "Theo dõi tiến độ và chuyển tiếp hồ sơ theo quy trình tiếp nhận – kiểm tra – báo giá – sửa chữa.";
        if(name.contains("kho")||name.contains("Cấp phát"))return "Theo dõi số lượng phụ tùng, chứng từ và biến động kho.";
        return "Tra cứu, theo dõi và xử lý hồ sơ theo nghiệp vụ của bộ phận.";
    }
    private JPanel heading(String title){
        JPanel p=new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(Theme.label(title,17,true,Theme.NAV),BorderLayout.WEST);
        return p;
    }
    private JPanel cardTable(String title,String[] cols,Object[][] rows,int height){
        JPanel c=Theme.card(new BorderLayout(0,14));
        c.add(heading(title),BorderLayout.NORTH);
        JTable t=Theme.table(new DefaultTableModel(rows,cols){
            public boolean isCellEditable(int r,int col){
                return false;
            }
        });
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane sc=new JScrollPane(t);
        sc.setBorder(new LineBorder(Theme.LINE));
        sc.setPreferredSize(new Dimension(420,height));
        c.add(sc,BorderLayout.CENTER);
        return c;
    }
    private void formButton(JPanel p,String text){
        JButton b=Theme.button(text,Theme.TEAL,Color.WHITE);
        b.setPreferredSize(new Dimension(Math.max(175, b.getFontMetrics(b.getFont()).stringWidth(text)+42),39));
        b.addActionListener(e -> {
            SampleData.Definition definition = SampleData.get(name);
            String[] values = EntryDialog.show(this, name, text, definition.cols, null);
            if (values != null) {
                JOptionPane.showMessageDialog(this,
                        "Đã nhận thông tin xem thử. Chưa lưu vào cơ sở dữ liệu.");
            }
        });
        p.add(b,BorderLayout.EAST);
    }
    private void forms(JPanel root){
        String[] cols={
            "Mã phiếu","Phiếu liên quan","Khách hàng","Phương tiện","Ngày lập","Trạng thái"
        };
        Object[][] rows={
            {
                "PT001","TN001","Nguyễn Văn Thành","Toyota Camry","18/09/2026","Đã hoàn tất"
            },{
                "PT002","TN002","Phạm Văn Minh","Mazda CX-5","19/09/2026","Đang xử lý"
            },{
                "PT003","TN003","Lê Thị Hoa","Honda CR-V","20/09/2026","Chờ xác nhận"
            }
        };
        JPanel top=Theme.card(new BorderLayout(12,0));
        top.add(Theme.label("Hồ sơ theo quy trình",17,true,Theme.NAV),BorderLayout.WEST);
        formButton(top,"Tạo phiếu mới");
        root.add(top);
        Theme.gap(root,16);
        if(name.contains("kiểm tra")||name.contains("báo giá")){
            root.add(cardTable(name.contains("kiểm tra")?"Xe đang chờ kiểm tra":"Phiếu kiểm tra chờ lập báo giá",new String[]{
                "Mã tiếp nhận","Khách hàng","Biển số","Yêu cầu","Trạng thái"
            },new Object[][]{
                {
                    "TN004","Nguyễn Văn Thành","51H-123.45","Kiểm tra hệ thống phanh","Chờ xử lý"
                }
            },120));
            Theme.gap(root,17);
        }
        root.add(cardTable("Danh sách "+name.toLowerCase(),cols,rows,230));
        Theme.gap(root,17);
        JPanel detail=Theme.card(new BorderLayout(10,12));
        detail.add(heading("Thông tin và tiến độ hồ sơ"),BorderLayout.NORTH);
        JPanel grid=new JPanel(new GridLayout(2,3,15,15));
        grid.setOpaque(false);
        for(String[] x:new String[][]{
            {
                "KHÁCH HÀNG","Nguyễn Văn Thành"
            },{
                "BIỂN SỐ","51H-123.45"
            },{
                "PHIẾU TIẾP NHẬN","TN001"
            },{
                "KIỂM TRA","Đã kiểm tra"
            },{
                "BÁO GIÁ","Đã xác nhận"
            },{
                "SỬA CHỮA","Đang thực hiện"
            }
        }){
            JPanel item=new JPanel(new GridLayout(2,1));
            item.setOpaque(false);
            item.add(Theme.label(x[0],10,true,Theme.MUTED));
            item.add(Theme.label(x[1],14,true,Theme.TEAL));
            grid.add(item);
        }
        detail.add(grid,BorderLayout.CENTER);
        root.add(detail);
    }
    private void stock(JPanel root){
        JPanel stats=new JPanel(new GridLayout(1,4,14,0));
        stats.setOpaque(false);
        for(String[] v:new String[][]{
            {
                "LOẠI PHỤ TÙNG","8"
            },{
                "CÒN HÀNG","6"
            },{
                "SẮP HẾT","2"
            },{
                "ĐÃ XUẤT TRONG KỲ","16"
            }
        }){
            JPanel c=Theme.card(new GridLayout(2,1));
            c.add(Theme.label(v[0],11,true,Theme.MUTED));
            c.add(Theme.label(v[1],26,true,Theme.TEAL));
            stats.add(c);
        }
        root.add(stats);
        Theme.gap(root,17);
        root.add(cardTable("Danh sách tồn kho",new String[]{
            "Mã","Tên phụ tùng","Đơn vị","Tồn kho","Tồn tối thiểu","Đơn giá","Trạng thái"
        },new Object[][]{
            {
                "PT001","Lọc dầu","Cái",45,10,"180.000 đ","Đủ hàng"
            },{
                "PT002","Lọc gió động cơ","Cái",32,8,"220.000 đ","Đủ hàng"
            },{
                "PT003","Má phanh trước","Bộ",15,5,"650.000 đ","Đủ hàng"
            },{
                "PT004","Dầu động cơ 5W-30","Lít",8,20,"150.000 đ","Sắp hết"
            }
        },270));
        Theme.gap(root,17);
        root.add(cardTable("Lịch sử nhập – xuất – hoàn trả",new String[]{
            "Ngày","Mã phụ tùng","Loại biến động","Số lượng","Phiếu liên quan"
        },new Object[][]{
            {
                "18/09/2026","PT003","Xuất kho",1,"SC001"
            },{
                "19/09/2026","PT001","Nhập kho",10,"NK002"
            }
        },145));
    }
    private void warehouse(JPanel root){
        JPanel bar=Theme.card(new BorderLayout());
        bar.add(Theme.label(name.contains("Cấp phát")?"Yêu cầu cấp phát chờ xử lý":"Danh sách phiếu nhập phụ tùng",18,true,Theme.NAV),BorderLayout.WEST);
        formButton(bar,name.contains("Cấp phát")?"Xác nhận cấp phát":"Tạo phiếu nhập");
        root.add(bar);
        Theme.gap(root,16);
        root.add(cardTable("Chứng từ và trạng thái",new String[]{
            "Mã phiếu","Ngày","Phụ tùng","Số lượng","Người phụ trách","Trạng thái"
        },new Object[][]{
            {
                "NK001","18/09/2026","Lọc dầu",12,"Kiên","Đã xác nhận"
            },{
                "NK002","19/09/2026","Má phanh trước",4,"Kiên","Đang xử lý"
            }
        },260));
        Theme.gap(root,16);
        root.add(cardTable("Lịch sử xử lý",new String[]{
            "Thời gian","Nghiệp vụ","Mã chứng từ","Kết quả"
        },new Object[][]{
            {
                "18/09/2026","Kiểm tra số lượng","NK001","Hoàn tất"
            }
        },125));
    }
    private void lookup(JPanel root){
        JPanel search=Theme.card(new BorderLayout(12,0));
        JTextField f=Theme.field("Tìm theo số khung, biển số, khách hàng hoặc mã phiếu...");
        search.add(f,BorderLayout.CENTER);
        JButton b=Theme.button("Tra cứu",Theme.TEAL,Color.WHITE);
        search.add(b,BorderLayout.EAST);
        root.add(search);
        Theme.gap(root,16);
        JPanel split=new JPanel(new GridLayout(1,2,12,0));
        split.setOpaque(false);
        split.add(cardTable("Hồ sơ phương tiện",new String[]{
            "Phiếu","Biển số","Khách hàng","Trạng thái"
        },new Object[][]{
            {
                "SC001","51H-123.45","Nguyễn Văn Thành","Hoàn tất"
            },{
                "SC002","51H-456.78","Phạm Văn Minh","Đang xử lý"
            }
        },300));
        JPanel right=Theme.card(new BorderLayout(0,16));
        boolean warranty=name.toLowerCase(Locale.ROOT).contains("bảo hành");
        right.add(heading(warranty?"Kiểm tra thời hạn bảo hành":"Chi tiết lần sửa chữa"),BorderLayout.NORTH);
        JTextArea info=new JTextArea("Khách hàng: Nguyễn Văn Thành\nPhương tiện: Toyota Camry\nBiển số: 51H-123.45\n\nDịch vụ: Thay dầu động cơ\nPhụ tùng: Lọc dầu\n\nTrạng thái: Còn thời hạn bảo hành");
        info.setFont(Theme.font(14,false));
        info.setEditable(false);
        info.setOpaque(false);
        right.add(info,BorderLayout.CENTER);
        JButton action=Theme.button(warranty?"Tiếp nhận bảo hành":"Xem hồ sơ",Theme.TEAL,Color.WHITE);
        action.addActionListener(e->JOptionPane.showMessageDialog(this,"Đây là giao diện xem thử, chưa lưu SQL."));
        JPanel actionLine=new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0));
        actionLine.setOpaque(false);
        action.setPreferredSize(new Dimension(205,40));
        actionLine.add(action);
        right.add(actionLine,BorderLayout.SOUTH);
        split.add(right);
        root.add(split);
    }
    private void reminder(JPanel root){
        JPanel bar=Theme.card(new BorderLayout());
        bar.add(heading("Lịch chăm sóc và nhắc bảo dưỡng"),BorderLayout.WEST);
        formButton(bar,"Tạo lịch nhắc");
        root.add(bar);
        Theme.gap(root,16);
        root.add(cardTable("Lịch nhắc sắp đến hạn",new String[]{
            "Khách hàng","Biển số","Ngày nhắc","Nội dung","Trạng thái"
        },new Object[][]{
            {
                "Nguyễn Văn Thành","51H-123.45","18/03/2027","Bảo dưỡng định kỳ 6 tháng","Chờ gửi"
            },{
                "Phạm Văn Minh","51H-456.78","19/03/2027","Kiểm tra phanh","Chờ gửi"
            }
        },290));
    }
    private void promotions(JPanel root){
        JPanel bar=Theme.card(new BorderLayout());
        bar.add(heading("Chương trình ưu đãi và tích điểm"),BorderLayout.WEST);
        formButton(bar,"Thêm khuyến mãi");
        root.add(bar);
        Theme.gap(root,16);
        root.add(cardTable("Danh sách chương trình khuyến mãi",new String[]{
            "Mã","Tên chương trình","Từ ngày","Đến ngày","Mức giảm","Trạng thái"
        },new Object[][]{
            {
                "KM001","Bảo dưỡng hè","01/07/2026","30/09/2026","10%","Đang áp dụng"
            },{
                "KM002","Thay dầu giảm giá","01/09/2026","30/09/2026","5%","Đang áp dụng"
            }
        },260));
        Theme.gap(root,16);
        root.add(cardTable("Tích điểm khách hàng",new String[]{
            "Khách hàng","Số điện thoại","Điểm hiện tại","Hạng"
        },new Object[][]{
            {
                "Nguyễn Văn Thành","0901234567",1200,"Thân thiết"
            },{
                "Phạm Văn Minh","0912345678",850,"Thân thiết"
            }
        },150));
    }
}
