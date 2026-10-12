package gui;
import java.util.*;
public final class MenuData {
    public static final String[] TECH={
        "Tổng quan","Phiếu tiếp nhận","Phiếu kiểm tra","Phiếu báo giá","Phiếu sửa chữa","Bảo hành","Lịch nhắc bảo dưỡng"
    };
    public static final String[] WARE={
        "Tổng quan","Quản lý tồn kho","Cấp phát phụ tùng","Nhập kho phụ tùng","Tra cứu lịch sử sửa chữa"
    };
    public static final String[] CASH={
        "Tổng quan","Hóa đơn","Thanh toán","Khuyến mãi/Tích điểm","Bán phụ tùng"
    };
    public static final String[] MANAGER={
        "Tổng quan","Quản lý nhà cung cấp","Quản lý nhân viên","Quản lý khách hàng","Quản lý phương tiện","Danh mục phụ tùng và dịch vụ","Quản lý tài khoản","Khuyến mãi","Thống kê"
    };
    public static String[] forRole(Role r){
        switch(r){
            case KY_THUAT:return TECH;
            case KHO:return WARE;
            case THU_NGAN:return CASH;
            default:return MANAGER;
        }
    }
    public static String icon(String s){
        if(s.contains("Tổng quan"))return "⌂";
        if(s.contains("Phiếu"))return "▤";
        if(s.contains("kho")||s.contains("Kho")||s.contains("phụ tùng"))return "▣";
        if(s.contains("Thanh toán")||s.contains("Hóa đơn"))return "₫";
        if(s.contains("Thống kê"))return "▥";
        if(s.contains("khách hàng"))return "♙";
        if(s.contains("nhân viên"))return "♟";
        return "◇";
    }
}
