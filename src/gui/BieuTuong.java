package gui;
import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.net.*;
import javax.swing.*;
/** Biểu tượng PNG được đóng gói trong src/IMG/icons, không tải từ mạng. */ public final class BieuTuong {
    private BieuTuong(){
    }
    public static ImageIcon get(String key,int size,Color tint){
        String name=key.toLowerCase();
        String file="document";
        if(name.contains("tổng quan")||name.contains("trang chủ"))file="home";
        else if(name.contains("tiếp nhận")||name.contains("nhập kho"))file="inbox";
        else if(name.contains("kiểm tra")||name.contains("báo giá"))file="clipboard";
        else if(name.contains("sửa chữa")||name.contains("bảo hành"))file="wrench";
        else if((name.contains("lịch")&&!name.contains("tích điểm"))||name.contains("nhắc"))file="calendar";
        else if(name.contains("nhân viên")||name.contains("kỹ thuật"))file="user";
        else if(name.contains("khách hàng"))file="users";
        else if(name.contains("phương tiện")||name.contains("xe"))file="car";
        else if(name.contains("kho")||name.contains("phụ tùng")||name.contains("cấp phát"))file="package";
        else if(name.contains("hóa đơn")||name.contains("thanh toán"))file="receipt";
        else if(name.contains("thống kê")||name.contains("doanh thu"))file="bar-chart";
        else if(name.contains("khuyến mãi")||name.contains("tích điểm"))file="gift";
        else if(name.contains("tài khoản")||name.contains("phân quyền"))file="shield";
        else if(name.contains("bán"))file="shopping-bag";
        else if(name.contains("đăng xuất"))file="logout";
        else if(name.contains("thêm")||name.contains("tạo"))file="plus";
        else if(name.contains("tìm"))file="search";
        else if(name.contains("chi tiết"))file="info";
        else if(name.contains("xóa"))file="help-circle";
        else if(name.contains("sửa")||name.contains("chỉnh"))file="settings";
        else if(name.contains("xác nhận"))file="check";
        try{
            URL url=BieuTuong.class.getResource("/IMG/icons/"+file+".png");
            if(url==null)return null;
            BufferedImage img=javax.imageio.ImageIO.read(url);
            BufferedImage out=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g=out.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(img,0,0,size,size,null);
            g.setComposite(AlphaComposite.SrcIn);
            g.setColor(tint);
            g.fillRect(0,0,size,size);
            g.dispose();
            return new ImageIcon(out);
        }
        catch(IOException e){
            return null;
        }
    }
    public static void apply(AbstractButton b,String key,Color color){
        b.setIcon(get(key,16,color));
        b.setIconTextGap(9);
    }
}
