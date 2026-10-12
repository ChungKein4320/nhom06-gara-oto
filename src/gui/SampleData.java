package gui;
import java.util.*;
public final class SampleData {
    public static final class Definition {
        public final String[] cols;
        public final Object[][] rows;
        public final String action;
        Definition(String a,String[] b,Object[][] c){
            action=a;
            cols=b;
            rows=c;
        }
    }
    private static Definition d(String action,String cols,String... rows){
        String[] h=cols.split("\\|");
        Object[][] data=new Object[rows.length][];
        for(int i=0;i<rows.length;i++)data[i]=rows[i].split("\\|",-1);
        return new Definition(action,h,data);
    }
    public static Definition get(String name){
        switch(name){
            case "Phiếu tiếp nhận":return d("Tiếp nhận xe mới","Mã phiếu|Ngày tiếp nhận|Khách hàng|Phương tiện|Yêu cầu|Trạng thái","TN001|18/09/2026|Nguyễn Văn Thành|Toyota Camry · 51H-123.45|Thay dầu, bảo dưỡng|Đang sửa chữa","TN002|19/09/2026|Phạm Văn Minh|Mazda CX-5 · 51H-456.78|Kiểm tra phanh|Hoàn tất","TN003|20/09/2026|Lê Thị Hoa|Honda CR-V · 51H-789.01|Bảo dưỡng định kỳ|Đang báo giá");
            case "Phiếu kiểm tra":return d("Lập phiếu kiểm tra","Mã phiếu|Phiếu tiếp nhận|Ngày kiểm tra|Người kiểm tra|Chẩn đoán|Trạng thái","KT001|TN001|18/09/2026|Châu|Thay dầu, lọc dầu|Đã kiểm tra","KT002|TN002|19/09/2026|Châu|Má phanh mòn|Đã kiểm tra","KT003|TN003|20/09/2026|Châu|Kiểm tra tổng quát|Đã kiểm tra");
            case "Phiếu báo giá":return d("Lập báo giá","Mã báo giá|Phiếu kiểm tra|Ngày lập|Khách hàng|Tổng tiền|Trạng thái","BG001|KT001|18/09/2026|Nguyễn Văn Thành|2.775.000 đ|Đã xác nhận","BG002|KT002|19/09/2026|Phạm Văn Minh|1.026.000 đ|Đã xác nhận","BG003|KT003|20/09/2026|Lê Thị Hoa|1.598.000 đ|Chờ xác nhận");
            case "Phiếu sửa chữa":return d("Lập phiếu sửa chữa","Mã phiếu|Phiếu kiểm tra|Ngày bắt đầu|Khách hàng|Hạng mục|Trạng thái","SC001|KT001|18/09/2026|Nguyễn Văn Thành|2 dịch vụ, 3 phụ tùng|Đang sửa chữa","SC002|KT002|19/09/2026|Phạm Văn Minh|2 dịch vụ, 1 phụ tùng|Đã hoàn tất");
            case "Bảo hành":return d("Tiếp nhận bảo hành","Mã sửa chữa|Biển số|Khách hàng|Dịch vụ|Thời hạn|Trạng thái","SC001|51H-123.45|Nguyễn Văn Thành|Thay dầu động cơ|18/03/2027|Còn bảo hành","SC002|51H-456.78|Phạm Văn Minh|Kiểm tra phanh|19/03/2027|Còn bảo hành");
            case "Bảo dưỡng":return d("Tạo lịch nhắc","Khách hàng|Biển số|Ngày nhắc|Nội dung|Trạng thái","Nguyễn Văn Thành|51H-123.45|18/03/2027|Bảo dưỡng định kỳ 6 tháng|Chờ gửi","Phạm Văn Minh|51H-456.78|19/03/2027|Kiểm tra phanh sau 6 tháng|Chờ gửi");
            case "Quản lý tồn kho":return d("Xem chi tiết tồn kho","Mã phụ tùng|Tên phụ tùng|Đơn vị|Tồn kho|Tối thiểu|Đơn giá|Trạng thái","PT001|Lọc dầu|Cái|45|10|180.000 đ|Đủ hàng","PT002|Lọc gió động cơ|Cái|32|8|220.000 đ|Đủ hàng","PT003|Má phanh trước|Bộ|15|5|650.000 đ|Đủ hàng","PT004|Dầu động cơ 5W-30|Lít|8|20|150.000 đ|Sắp hết","PT005|Bugi|Cái|60|15|120.000 đ|Đủ hàng");
            case "Cấp phát phụ tùng":return d("Xác nhận cấp phát","Mã phụ tùng|Tên phụ tùng|Phiếu sửa chữa|Yêu cầu|Tồn kho|Kỹ thuật viên|Trạng thái","PT001|Lọc dầu|SC001|1|45|Châu|Đã cấp","PT002|Lọc gió động cơ|SC001|1|32|Châu|Đã cấp","PT003|Má phanh trước|SC002|1|15|Châu|Chờ cấp");
            case "Nhập kho phụ tùng":return d("Tạo phiếu nhập","Mã phiếu|Ngày nhập|Nhà cung cấp|Số loại phụ tùng|Tổng tiền|Trạng thái","NK001|15/09/2026|Công ty Phụ tùng Minh Đức|2|13.580.000 đ|Đã xác nhận","NK002|16/09/2026|Thành Phát Auto Parts|1|6.000.000 đ|Đã xác nhận");
            case "Tra cứu lịch sử sửa chữa":return d("Tra cứu","Mã sửa chữa|Ngày|Khách hàng|Biển số|Dịch vụ|Chi phí|Trạng thái","SC001|18/09/2026|Nguyễn Văn Thành|51H-123.45|2 dịch vụ|2.775.000 đ|Đang sửa","SC002|19/09/2026|Phạm Văn Minh|51H-456.78|2 dịch vụ|1.026.000 đ|Hoàn tất");
            case "Quản lý nhà cung cấp":return d("Thêm nhà cung cấp","Mã NCC|Tên nhà cung cấp|Điện thoại|Thư điện tử|Địa chỉ|Trạng thái","NCC001|Công ty Phụ tùng Minh Đức|028 3734 5678|info@minhducauto.vn|Gò Vấp, TP.HCM|Hoạt động","NCC002|Thành Phát Auto Parts|028 3845 6789|contact@thanhphat.vn|Tân Bình, TP.HCM|Hoạt động");
            case "Quản lý nhân viên":return d("Thêm nhân viên","Mã NV|Họ và tên|Vai trò|Trạng thái","NV001|Phạm Thị Ngọc Châu|Nhân viên kỹ thuật|Đang làm","NV002|Cao Huỳnh Trung Kiên|Nhân viên kho|Đang làm","NV003|Quách Tấn Minh|Thu ngân|Đang làm","NV004|Châu Ngọc Khanh|Nhân viên quản lý|Đang làm");
            case "Quản lý khách hàng":return d("Thêm khách hàng","Mã KH|Họ tên|Điện thoại|Thư điện tử|Số xe|Điểm tích lũy","KH001|Nguyễn Văn Thành|0901234567|thanh@gmail.com|2|1200","KH002|Phạm Văn Minh|0912345678|minh@gmail.com|1|850","KH003|Lê Thị Hoa|0923456789|hoa@gmail.com|1|560");
            case "Quản lý phương tiện":return d("Thêm phương tiện","Biển số|Số khung (VIN)|Hãng/Dòng xe|Năm SX|Số km|Chủ xe","51H-123.45|JTNB11HK000000001|Toyota Camry|2020|45.000|Nguyễn Văn Thành","51H-456.78|JMZKF123000000002|Mazda CX-5|2021|32.000|Phạm Văn Minh","51H-789.01|MRHRE123000000003|Honda CR-V|2019|68.000|Lê Thị Hoa");
            case "Danh mục phụ tùng và dịch vụ":return d("Thêm danh mục","Mã|Tên danh mục|Loại|Đơn vị|Đơn giá|Trạng thái","PT001|Lọc dầu|Phụ tùng|Cái|180.000 đ|Đang kinh doanh","PT003|Má phanh trước|Phụ tùng|Bộ|650.000 đ|Đang kinh doanh","DV001|Thay dầu động cơ|Dịch vụ|Lần|500.000 đ|Đang áp dụng","DV002|Bảo dưỡng định kỳ|Dịch vụ|Lần|1.200.000 đ|Đang áp dụng","DV003|Kiểm tra phanh|Dịch vụ|Lần|300.000 đ|Đang áp dụng");
            case "Quản lý tài khoản":return d("Thêm tài khoản","Tên đăng nhập|Họ tên|Vai trò|Trạng thái","chau|Phạm Thị Ngọc Châu|Nhân viên kỹ thuật|Hoạt động","kien|Cao Huỳnh Trung Kiên|Nhân viên kho|Hoạt động","minh|Quách Tấn Minh|Thu ngân|Hoạt động","khanh|Châu Ngọc Khanh|Nhân viên quản lý|Hoạt động");
            default:return d("Thêm chương trình","Mã|Tên chương trình|Từ ngày|Đến ngày|Mức giảm|Trạng thái","KM001|Bảo dưỡng hè 2026|01/07/2026|30/10/2026|10%|Đang áp dụng","KM002|Thay dầu giảm giá|01/09/2026|30/10/2026|5%|Đang áp dụng","KM003|Khách hàng thân thiết|01/01/2026|31/12/2026|15%|Đang áp dụng");
        }
    }
}
