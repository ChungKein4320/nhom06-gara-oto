package gui;

import java.util.LinkedHashSet;

public final class DuLieuTrinhDon {
    private DuLieuTrinhDon() { }

    public static final String[] TECH = {
        "Tổng quan", "Tìm kiếm", "Phiếu tiếp nhận", "Phiếu kiểm tra",
        "Phiếu báo giá", "Phiếu sửa chữa", "Bảo hành", "Lịch nhắc bảo dưỡng"
    };
    public static final String[] WARE = {
        "Tổng quan", "Tìm kiếm", "Tồn kho", "Cấp phát phụ tùng",
        "Nhập kho phụ tùng", "Lịch sử sửa chữa"
    };
    public static final String[] CASH = {
        "Tổng quan", "Tìm kiếm", "Hóa đơn", "Khuyến mãi/Tích điểm", "Phụ tùng"
    };
    public static final String[] MANAGER = {
        "Tổng quan", "Tìm kiếm", "Nhà cung cấp", "Nhân viên",
        "Khách hàng", "Phương tiện", "Danh mục dịch vụ", "Danh mục phụ tùng",
        "Tài khoản", "Khuyến mãi", "Thống kê"
    };

    public static String[] forRole(VaiTro role) {
        if (role == VaiTro.ADMIN) {
            LinkedHashSet<String> all = new LinkedHashSet<>();
            for (String value : TECH) all.add(value);
            for (String value : WARE) all.add(value);
            for (String value : CASH) all.add(value);
            for (String value : MANAGER) all.add(value);
            return all.toArray(new String[0]);
        }
        switch (role) {
            case KY_THUAT: return TECH;
            case KHO: return WARE;
            case THU_NGAN: return CASH;
            default: return MANAGER;
        }
    }
}
