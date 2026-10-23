package dao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** Thanh toán, bán bổ sung và cấp phát đều nguyên tử và kiểm tra trạng thái mới nhất. */
public final class CommerceDAO {
    private CommerceDAO() {}
    public static void pay(String invoice,String method) throws SQLException {
        pay(invoice,method,null);
    }
    public static void pay(String invoice,String method,BigDecimal expectedTotal) throws SQLException {
        pay(invoice,method,expectedTotal,null);
    }
    public static void pay(String invoice,String method,BigDecimal expectedTotal,String scannedCustomer) throws SQLException {
        WorkflowDAO.transaction(conn -> {
            Map<String,Object> initial=WorkflowDAO.one(conn,"SELECT maPSC FROM dbo.HoaDon WHERE maHoaDon=?",invoice);
            Map<String,Object> repair=initial.get("maPSC")==null ? null : WorkflowDAO.lock(conn,"PhieuSuaChua",initial.get("maPSC"));
            Map<String,Object> row=WorkflowDAO.lock(conn,"HoaDon",invoice);
            if(scannedCustomer!=null) WorkflowDAO.require(scannedCustomer.matches("KH[0-9]{3}") && scannedCustomer.equals(row.get("maKhachHang")),"QR tích điểm không khớp khách hàng của hóa đơn.");
            WorkflowDAO.require(Set.of("Chưa thanh toán","Chờ thanh toán").contains(String.valueOf(row.get("trangThaiThanhToan"))),"Hóa đơn không còn chờ thanh toán.");
            WorkflowDAO.require(repair==null || Set.of("Đã sửa xong","Chờ thanh toán").contains(String.valueOf(repair.get("trangThai"))),"Phiếu sửa chưa sẵn sàng thanh toán.");
            WorkflowDAO.require(WorkflowDAO.exists(conn,"SELECT 1 FROM dbo.ChiTietHoaDon WHERE maHoaDon=?",invoice),"Hóa đơn chưa có chi tiết.");
            Map<String,Object> totals=WorkflowDAO.one(conn,"SELECT COALESCE(SUM(CASE WHEN maPhuTung IS NOT NULL THEN soLuong*donGia ELSE 0 END),0) AS partsSum, COALESCE(SUM(CASE WHEN maPhuTung IS NULL AND maDichVu IS NOT NULL THEN soLuong*donGia ELSE 0 END),0) AS laborSum FROM dbo.ChiTietHoaDon WHERE maHoaDon=?",invoice);
            WorkflowDAO.require(amount(row,"tienPhuTung").compareTo((BigDecimal)totals.get("partsSum"))==0 && amount(row,"tienCong").compareTo((BigDecimal)totals.get("laborSum"))==0,"Tổng tiền đầu hóa đơn không khớp chi tiết. Cần kiểm tra hóa đơn trước khi thu tiền.");
            WorkflowDAO.require(WorkflowDAO.exists(conn,"SELECT 1 FROM dbo.PhuongThucThanhToan WHERE maPhuongThuc=?",method),"Phương thức thanh toán không tồn tại.");
            if(repair!=null) WorkflowDAO.require(!WorkflowDAO.exists(conn,"SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=? AND (trangThaiHangMuc<>N'Hoàn thành' OR ketQuaNghiemThu<>N'Đạt')",row.get("maPSC")),"Còn hạng mục chưa nghiệm thu đạt.");
            BigDecimal subtotal=amount(row,"tienPhuTung").add(amount(row,"tienCong")).add(amount(row,"dichVuKhac"));
            BigDecimal discount=amount(row,"giamGia"); Object coupon=row.get("maGiamGia");
            if(coupon!=null) {
                discount=WorkflowDAO.couponDiscount(conn,coupon.toString(),row.get("maKhachHang"),subtotal);
                WorkflowDAO.execute(conn,"UPDATE dbo.MaGiamGia SET trangThai=N'Đã sử dụng' WHERE maGiamGia=?",coupon);
            }
            BigDecimal paid=subtotal.add(amount(row,"thueVAT")).subtract(discount);
            WorkflowDAO.require(paid.signum()>=0,"Tổng tiền thanh toán không hợp lệ.");
            WorkflowDAO.require(expectedTotal==null || paid.compareTo(expectedTotal)==0,"Số tiền hóa đơn đã thay đổi. Vui lòng tải lại và tạo lại QR trước khi xác nhận.");
            WorkflowDAO.execute(conn,"UPDATE dbo.HoaDon SET trangThaiThanhToan=N'Đã thanh toán',maPhuongThuc=?,giamGia=? WHERE maHoaDon=?",method,discount,invoice);
            if(repair!=null) WorkflowDAO.execute(conn,"UPDATE dbo.PhieuSuaChua SET trangThai=N'Đã thanh toán' WHERE maPSC=?",row.get("maPSC"));
            // Một hóa đơn chỉ sinh một lịch sử điểm, kể cả người dùng bấm lại.
            if(scannedCustomer!=null && !WorkflowDAO.exists(conn,"SELECT 1 FROM dbo.LichSuTichDiem WITH (UPDLOCK,HOLDLOCK) WHERE maHoaDon=?",invoice)) {
                new BaseDAO("LichSuTichDiem").insert(conn,Map.of("maKhachHang",row.get("maKhachHang"),"maHoaDon",invoice,
                    "ngayTichDiem",WorkflowDAO.now(conn),"soDiem",InvoiceMath.points(paid),"lyDo","Tích điểm trên tiền thực trả: 100.000đ = 1 điểm, làm tròn xuống."));
            }
            return null;
        });
    }
    private static BigDecimal amount(Map<String,Object> row,String field) { return (BigDecimal)row.get(field); }
    public static void changeCoupon(String invoice,String coupon) throws SQLException {
        WorkflowDAO.transaction(conn -> {
            Map<String,Object> initial=WorkflowDAO.one(conn,"SELECT maPSC FROM dbo.HoaDon WHERE maHoaDon=?",invoice);
            if(initial.get("maPSC")!=null) WorkflowDAO.lock(conn,"PhieuSuaChua",initial.get("maPSC"));
            Map<String,Object> row=WorkflowDAO.lock(conn,"HoaDon",invoice);
            WorkflowDAO.require(Set.of("Chưa thanh toán","Chờ thanh toán").contains(String.valueOf(row.get("trangThaiThanhToan"))),"Chỉ đổi mã giảm giá trước thanh toán.");
            BigDecimal subtotal=amount(row,"tienPhuTung").add(amount(row,"tienCong")).add(amount(row,"dichVuKhac"));
            boolean empty=coupon==null || coupon.isBlank();
            BigDecimal discount=empty ? BigDecimal.ZERO : WorkflowDAO.couponDiscount(conn,coupon,row.get("maKhachHang"),subtotal);
            WorkflowDAO.execute(conn,"UPDATE dbo.HoaDon SET maGiamGia=?,giamGia=? WHERE maHoaDon=?",empty ? null : coupon,discount,invoice); return null;
        });
    }
    public static void sell(String invoice,String service,String part,int quantity,BigDecimal expectedPrice) throws SQLException {
        sellBasket(invoice,service,List.of(new SaleItem(part,quantity,expectedPrice)));
    }
    public record SaleItem(String part,int quantity,BigDecimal expectedPrice) {}
    /** Bán lẻ độc lập, không tạo phiếu sửa chữa hoặc xe giả. */
    public static String createPartsInvoice(String customer,String employee,List<SaleItem> items) throws SQLException {
        WorkflowDAO.require(customer!=null && !customer.isBlank() && employee!=null && !employee.isBlank(),"Vui lòng chọn khách hàng và nhân viên bán hàng.");
        WorkflowDAO.require(items!=null && !items.isEmpty(),"Giỏ hàng đang trống.");
        List<SaleItem> basket=List.copyOf(items);
        Set<String> codes=new HashSet<>();
        for(SaleItem item:basket) {
            WorkflowDAO.require(item!=null && item.part()!=null && item.quantity()>0 && item.expectedPrice()!=null && item.expectedPrice().signum()>=0,"Phụ tùng hoặc số lượng không hợp lệ.");
            WorkflowDAO.require(codes.add(item.part()),"Giỏ hàng có mã phụ tùng trùng.");
        }
        return WorkflowDAO.transaction(conn -> {
            WorkflowDAO.lock(conn,"KhachHang",customer);
            WorkflowDAO.require(WorkflowDAO.exists(conn,"SELECT 1 FROM dbo.NhanVien WHERE maNhanVien=? AND trangThai=N'Đang làm'",employee),"Nhân viên bán hàng không hợp lệ.");
            BigDecimal subtotal=BigDecimal.ZERO;
            for(SaleItem item:basket.stream().sorted(Comparator.comparing(SaleItem::part)).toList()) {
                BigDecimal price=(BigDecimal)WorkflowDAO.one(conn,"SELECT donGia FROM dbo.PhuTung WITH (HOLDLOCK) WHERE maPhuTung=?",item.part()).get("donGia");
                WorkflowDAO.require(price.compareTo(item.expectedPrice())==0,"Đơn giá đã thay đổi. Vui lòng tải lại.");
                WorkflowDAO.require(WorkflowDAO.execute(conn,"UPDATE dbo.TonKho SET soLuongHienTai=soLuongHienTai-? WHERE maPhuTung=? AND soLuongHienTai>=?",item.quantity(),item.part(),item.quantity())==1,"Không đủ tồn kho cho "+item.part()+".");
                subtotal=subtotal.add(price.multiply(BigDecimal.valueOf(item.quantity())));
            }
            Object method=WorkflowDAO.one(conn,"SELECT TOP (1) maPhuongThuc FROM dbo.PhuongThucThanhToan ORDER BY CASE WHEN tenPhuongThuc=N'Tiền mặt' THEN 0 ELSE 1 END,maPhuongThuc").get("maPhuongThuc");
            Map<String,Object> header=new LinkedHashMap<>();
            header.put("maKhachHang",customer); header.put("maNhanVien",employee);
            header.put("maPSC",null); header.put("VIN",null); header.put("ngayLap",WorkflowDAO.now(conn));
            header.put("tienPhuTung",subtotal); header.put("tienCong",BigDecimal.ZERO); header.put("dichVuKhac",BigDecimal.ZERO);
            header.put("thueVAT",InvoiceMath.vat(subtotal)); header.put("giamGia",BigDecimal.ZERO);
            header.put("trangThaiThanhToan","Chưa thanh toán"); header.put("maPhuongThuc",method);
            String invoice=new BaseDAO("HoaDon").insert(conn,header);
            for(SaleItem item:basket) WorkflowDAO.insertInvoiceLine(conn,Map.of("maHoaDon",invoice,"maPhuTung",item.part(),"soLuong",item.quantity(),"donGia",item.expectedPrice()));
            return invoice;
        });
    }
    public static void sellBasket(String invoice,String service,List<SaleItem> items) throws SQLException {
        WorkflowDAO.require(items!=null && !items.isEmpty(),"Giỏ hàng đang trống.");
        List<SaleItem> basket=List.copyOf(items);
        Set<String> codes=new HashSet<>();
        for(SaleItem item:basket) {
            WorkflowDAO.require(item.part()!=null && item.quantity()>0 && item.expectedPrice()!=null && item.expectedPrice().signum()>=0,"Phụ tùng hoặc số lượng không hợp lệ.");
            WorkflowDAO.require(codes.add(item.part()),"Giỏ hàng có mã phụ tùng trùng.");
        }
        WorkflowDAO.transaction(conn -> {
            Map<String,Object> initial=WorkflowDAO.one(conn,"SELECT maPSC FROM dbo.HoaDon WHERE maHoaDon=?",invoice);
            Map<String,Object> repair=WorkflowDAO.lock(conn,"PhieuSuaChua",initial.get("maPSC"));
            Map<String,Object> row=WorkflowDAO.lock(conn,"HoaDon",invoice);
            WorkflowDAO.require(Set.of("Đã sửa xong","Chờ thanh toán").contains(String.valueOf(repair.get("trangThai"))),"Phiếu sửa không còn cho phép bán bổ sung.");
            WorkflowDAO.require(Set.of("Chưa thanh toán","Chờ thanh toán").contains(String.valueOf(row.get("trangThaiThanhToan"))),"Hóa đơn đã thanh toán.");
            if(service!=null && !service.isBlank()) WorkflowDAO.require(WorkflowDAO.exists(conn,"SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=? AND maDichVu=?",row.get("maPSC"),service),"Dịch vụ không thuộc phiếu sửa chữa.");
            BigDecimal added=BigDecimal.ZERO;
            // Cùng thứ tự khóa tồn kho cho các giỏ hàng khác nhau.
            for(SaleItem item:basket.stream().sorted(Comparator.comparing(SaleItem::part)).toList()) {
            String part=item.part(); int quantity=item.quantity(); BigDecimal expectedPrice=item.expectedPrice();
            WorkflowDAO.require(!WorkflowDAO.exists(conn,"SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=? AND maPhuTung=?",row.get("maPSC"),part),"Phụ tùng này đã thuộc phiếu sửa chữa; không được bán/trừ kho lần nữa.");
            WorkflowDAO.require(!WorkflowDAO.exists(conn,"SELECT 1 FROM dbo.ChiTietHoaDon WHERE maHoaDon=? AND maPhuTung=?",invoice,part),"Phụ tùng đã có trong hóa đơn.");
            BigDecimal price=(BigDecimal)WorkflowDAO.one(conn,"SELECT donGia FROM dbo.PhuTung WITH (HOLDLOCK) WHERE maPhuTung=?",part).get("donGia");
            WorkflowDAO.require(price.compareTo(expectedPrice)==0,"Đơn giá đã thay đổi. Vui lòng tải lại.");
            WorkflowDAO.require(WorkflowDAO.execute(conn,"UPDATE dbo.TonKho SET soLuongHienTai=soLuongHienTai-? WHERE maPhuTung=? AND soLuongHienTai>=?",quantity,part,quantity)==1,"Không đủ tồn kho.");
            Map<String,Object> line=new LinkedHashMap<>(); line.put("maHoaDon",invoice); line.put("maDichVu",service==null || service.isBlank() ? null : service); line.put("maPhuTung",part); line.put("soLuong",quantity); line.put("donGia",price);
            WorkflowDAO.insertInvoiceLine(conn,line);
            added=added.add(price.multiply(BigDecimal.valueOf(quantity)));
            }
            BigDecimal parts=amount(row,"tienPhuTung").add(added);
            BigDecimal subtotal=parts.add(amount(row,"tienCong")).add(amount(row,"dichVuKhac"));
            BigDecimal discount=row.get("maGiamGia")==null ? amount(row,"giamGia") : WorkflowDAO.couponDiscount(conn,row.get("maGiamGia").toString(),row.get("maKhachHang"),subtotal);
            WorkflowDAO.execute(conn,"UPDATE dbo.HoaDon SET tienPhuTung=?,thueVAT=?,giamGia=? WHERE maHoaDon=?",parts,InvoiceMath.vat(subtotal),discount,invoice); return null;
        });
    }
    public static void allocate(String repair,String service,String part) throws SQLException {
        WorkflowDAO.transaction(conn -> {
            Map<String,Object> header=WorkflowDAO.lock(conn,"PhieuSuaChua",repair);
            WorkflowDAO.require(Set.of("Chờ sửa chữa","Đang sửa chữa").contains(String.valueOf(header.get("trangThai"))),"Không thể cấp phát cho phiếu đã hoàn thành/hủy.");
            Map<String,Object> line=WorkflowDAO.one(conn,"SELECT * FROM dbo.ChiTietPhieuSuaChua WITH (UPDLOCK,HOLDLOCK) WHERE maPSC=? AND maDichVu=? AND maPhuTung=?",repair,service,part);
            WorkflowDAO.require("Thiếu phụ tùng".equals(line.get("trangThaiHangMuc")),"Hạng mục đã cấp phát hoặc không thiếu phụ tùng.");
            int quantity=((Number)line.get("soLuongPhuTung")).intValue();
            WorkflowDAO.require(quantity>0 && WorkflowDAO.execute(conn,"UPDATE dbo.TonKho SET soLuongHienTai=soLuongHienTai-? WHERE maPhuTung=? AND soLuongHienTai>=?",quantity,part,quantity)==1,"Không đủ tồn kho để cấp phát.");
            WorkflowDAO.execute(conn,"UPDATE dbo.ChiTietPhieuSuaChua SET trangThaiHangMuc=N'Đang sửa chữa' WHERE maPSC=? AND maDichVu=? AND maPhuTung=?",repair,service,part); return null;
        });
    }
}
