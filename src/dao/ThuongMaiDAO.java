package dao;

import entity.HoaDon;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** Thanh toán, bán bổ sung và cấp phát đều nguyên tử và kiểm tra trạng thái mới nhất. */
public final class ThuongMaiDAO {
    private ThuongMaiDAO() {}
    public static void pay(String invoice,String method) throws SQLException {
        pay(invoice,method,null);
    }
    public static void pay(String invoice,String method,BigDecimal expectedTotal) throws SQLException {
        pay(invoice,method,expectedTotal,null);
    }
    public static void pay(String invoice,String method,BigDecimal expectedTotal,String scannedCustomer) throws SQLException {
        QuyTrinhDAO.transaction(conn -> {
            Map<String,Object> initial=QuyTrinhDAO.one(conn,"SELECT maPSC FROM dbo.HoaDon WHERE maHoaDon=?",invoice);
            Map<String,Object> repair=initial.get("maPSC")==null ? null : QuyTrinhDAO.lock(conn,"PhieuSuaChua",initial.get("maPSC"));
            Map<String,Object> row=QuyTrinhDAO.lock(conn,"HoaDon",invoice);
            HoaDon hoaDon=new HoaDon();
            hoaDon.setTienPhuTung(amount(row,"tienPhuTung"));
            hoaDon.setTienCong(amount(row,"tienCong"));
            hoaDon.setDichVuKhac(amount(row,"dichVuKhac"));
            hoaDon.setThueVAT(amount(row,"thueVAT"));
            hoaDon.setGiamGia(amount(row,"giamGia"));
            hoaDon.setTrangThaiThanhToan(String.valueOf(row.get("trangThaiThanhToan")));
            if(scannedCustomer!=null) QuyTrinhDAO.require(scannedCustomer.matches("KH[0-9]{3}") && scannedCustomer.equals(row.get("maKhachHang")),"QR tích điểm không khớp khách hàng của hóa đơn.");
            QuyTrinhDAO.require(Set.of("Chưa thanh toán","Chờ thanh toán").contains(hoaDon.getTrangThaiThanhToan()),"Hóa đơn không còn chờ thanh toán.");
            QuyTrinhDAO.require(repair==null || Set.of("Đã sửa xong","Chờ thanh toán").contains(String.valueOf(repair.get("trangThai"))),"Phiếu sửa chưa sẵn sàng thanh toán.");
            QuyTrinhDAO.require(QuyTrinhDAO.exists(conn,"SELECT 1 FROM dbo.ChiTietHoaDon WHERE maHoaDon=?",invoice),"Hóa đơn chưa có chi tiết.");
            Map<String,Object> totals=QuyTrinhDAO.one(conn,"SELECT COALESCE(SUM(CASE WHEN maPhuTung IS NOT NULL THEN soLuong*donGia ELSE 0 END),0) AS partsSum, COALESCE(SUM(CASE WHEN maPhuTung IS NULL AND maDichVu IS NOT NULL THEN soLuong*donGia ELSE 0 END),0) AS laborSum FROM dbo.ChiTietHoaDon WHERE maHoaDon=?",invoice);
            QuyTrinhDAO.require(amount(row,"tienPhuTung").compareTo((BigDecimal)totals.get("partsSum"))==0 && amount(row,"tienCong").compareTo((BigDecimal)totals.get("laborSum"))==0,"Tổng tiền đầu hóa đơn không khớp chi tiết. Cần kiểm tra hóa đơn trước khi thu tiền.");
            QuyTrinhDAO.require(QuyTrinhDAO.exists(conn,"SELECT 1 FROM dbo.PhuongThucThanhToan WHERE maPhuongThuc=?",method),"Phương thức thanh toán không tồn tại.");
            if(repair!=null) QuyTrinhDAO.require(!QuyTrinhDAO.exists(conn,"SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=? AND (trangThaiHangMuc<>N'Hoàn thành' OR ketQuaNghiemThu<>N'Đạt')",row.get("maPSC")),"Còn hạng mục chưa nghiệm thu đạt.");
            BigDecimal subtotal=amount(row,"tienPhuTung").add(amount(row,"tienCong")).add(amount(row,"dichVuKhac"));
            BigDecimal discount=hoaDon.getGiamGia()==null ? BigDecimal.ZERO : hoaDon.getGiamGia(); Object coupon=row.get("maGiamGia");
            if(coupon!=null) {
                QuyTrinhDAO.require(repair!=null,"Mã giảm giá điểm chỉ áp dụng cho hóa đơn sửa chữa/dịch vụ.");
                discount=QuyTrinhDAO.couponDiscount(conn,coupon.toString(),row.get("maKhachHang"),subtotal);
                QuyTrinhDAO.execute(conn,"UPDATE dbo.MaGiamGia SET trangThai=N'Đã sử dụng' WHERE maGiamGia=?",coupon);
            }
            hoaDon.setGiamGia(discount);
            BigDecimal paid=hoaDon.tongThanhToan();
            QuyTrinhDAO.require(paid.signum()>=0,"Tổng tiền thanh toán không hợp lệ.");
            QuyTrinhDAO.require(expectedTotal==null || paid.compareTo(expectedTotal)==0,"Số tiền hóa đơn đã thay đổi. Vui lòng tải lại và tạo lại QR trước khi xác nhận.");
            String trangThaiMoi=hoaDon.capNhatTrangThai("Đã thanh toán");
            QuyTrinhDAO.execute(conn,"UPDATE dbo.HoaDon SET trangThaiThanhToan=?,maPhuongThuc=?,giamGia=? WHERE maHoaDon=?",trangThaiMoi,method,discount,invoice);
            if(repair!=null) QuyTrinhDAO.execute(conn,"UPDATE dbo.PhieuSuaChua SET trangThai=N'Đã thanh toán' WHERE maPSC=?",row.get("maPSC"));
            // Một hóa đơn chỉ sinh một lịch sử điểm, kể cả người dùng bấm lại.
            if (repair != null && scannedCustomer != null && !QuyTrinhDAO.exists(
                        conn,"SELECT 1 FROM dbo.LichSuTichDiem "
                                + "WITH (UPDLOCK,HOLDLOCK) WHERE maHoaDon=?",invoice)) {
                Timestamp earnedAt=QuyTrinhDAO.now(conn);
                BigDecimal eligible=amount(row,"tienPhuTung").add(amount(row,"tienCong")).add(amount(row,"dichVuKhac")).add(amount(row,"thueVAT"));
                int points=InvoiceMath.points(eligible);
                Timestamp cycleStart=(Timestamp)QuyTrinhDAO.one(conn,"SELECT MIN(ngayTichDiem) AS firstEarn FROM dbo.LichSuTichDiem WHERE maKhachHang=? AND ngayHetHan>=?",row.get("maKhachHang"),earnedAt).get("firstEarn");
                if(cycleStart==null) cycleStart=earnedAt;
                Timestamp expiration=Timestamp.valueOf(cycleStart.toLocalDateTime().plusYears(1));
                new CoSoDAO("LichSuTichDiem").insert(conn,Map.of("maKhachHang",row.get("maKhachHang"),"maHoaDon",invoice,
                    "ngayTichDiem",earnedAt,"ngayHetHan",expiration,"soDiem",points,"lyDo","Tích điểm trên tổng sau VAT, trước giảm giá; 100.000đ = 1 điểm, làm tròn xuống."));
                issueThresholdCoupons(conn,String.valueOf(row.get("maKhachHang")),cycleStart,expiration,earnedAt);
            }
            if(repair==null) fulfillPartsInvoice(conn,invoice);
            return null;
        });
    }
    private static BigDecimal amount(Map<String,Object> row,String field) { return (BigDecimal)row.get(field); }
    public static void changeCoupon(String invoice,String coupon) throws SQLException {
        QuyTrinhDAO.transaction(conn -> {
            Map<String,Object> initial=QuyTrinhDAO.one(conn,"SELECT maPSC FROM dbo.HoaDon WHERE maHoaDon=?",invoice);
            if(initial.get("maPSC")!=null) QuyTrinhDAO.lock(conn,"PhieuSuaChua",initial.get("maPSC"));
            Map<String,Object> row=QuyTrinhDAO.lock(conn,"HoaDon",invoice);
            QuyTrinhDAO.require(Set.of("Chưa thanh toán","Chờ thanh toán").contains(String.valueOf(row.get("trangThaiThanhToan"))),"Chỉ đổi mã giảm giá trước thanh toán.");
            BigDecimal subtotal=amount(row,"tienPhuTung").add(amount(row,"tienCong")).add(amount(row,"dichVuKhac"));
            boolean empty=coupon==null || coupon.isBlank();
            QuyTrinhDAO.require(empty || initial.get("maPSC")!=null,"Mã giảm giá điểm chỉ áp dụng cho hóa đơn sửa chữa/dịch vụ.");
            BigDecimal discount=empty ? BigDecimal.ZERO : QuyTrinhDAO.couponDiscount(conn,coupon,row.get("maKhachHang"),subtotal);
            QuyTrinhDAO.execute(conn,"UPDATE dbo.HoaDon SET maGiamGia=?,giamGia=? WHERE maHoaDon=?",empty ? null : coupon,discount,invoice); return null;
        });
    }
    public static void sell(String invoice,String service,String part,int quantity,BigDecimal expectedPrice) throws SQLException {
        sellBasket(invoice,service,List.of(new SaleItem(part,quantity,expectedPrice)));
    }
    public record SaleItem(String part,int quantity,BigDecimal expectedPrice) {}
    /** Bán lẻ độc lập: hóa đơn và ChiTietHoaDon là chứng từ bán hàng duy nhất. */
    public static String createPartsInvoice(String customer,String employee,List<SaleItem> items) throws SQLException {
        return createPartsInvoice(customer,null,employee,items);
    }
    /** Lập hóa đơn bán lẻ cho khách hàng mới; khách mới và hóa đơn được lưu cùng giao dịch. */
    public static String createPartsInvoice(Map<String,Object> newCustomer,String employee,List<SaleItem> items) throws SQLException {
        return createPartsInvoice(null,newCustomer,employee,items);
    }
    private static String createPartsInvoice(String customer,Map<String,Object> newCustomer,String employee,List<SaleItem> items) throws SQLException {
        boolean existingCustomer=customer!=null && !customer.isBlank();
        QuyTrinhDAO.require((existingCustomer || newCustomer!=null) && employee!=null && !employee.isBlank(),"Vui lòng nhập thông tin khách hàng và nhân viên bán hàng.");
        QuyTrinhDAO.require(items!=null && !items.isEmpty(),"Giỏ hàng đang trống.");
        List<SaleItem> basket=List.copyOf(items);
        Set<String> codes=new HashSet<>();
        for(SaleItem item:basket) {
            QuyTrinhDAO.require(item!=null && item.part()!=null && item.quantity()>0 && item.expectedPrice()!=null && item.expectedPrice().signum()>=0,"Phụ tùng hoặc số lượng không hợp lệ.");
            QuyTrinhDAO.require(!"PT000".equals(item.part()),"PT000 là mã biểu thị không sử dụng phụ tùng, không thể bán.");
            QuyTrinhDAO.require(codes.add(item.part()),"Giỏ hàng có mã phụ tùng trùng.");
        }
        return QuyTrinhDAO.transaction(conn -> {
            QuyTrinhDAO.require(QuyTrinhDAO.exists(conn,"SELECT 1 FROM dbo.NhanVien WHERE maNhanVien=? AND trangThai=N'Đang làm'",employee),"Nhân viên bán hàng không hợp lệ.");
            String resolvedCustomer;
            if(existingCustomer) {
                QuyTrinhDAO.lock(conn,"KhachHang",customer);
                resolvedCustomer=customer;
            } else {
                resolvedCustomer=new CoSoDAO("KhachHang").insert(conn,newCustomer);
            }
            BigDecimal subtotal=BigDecimal.ZERO;
            for(SaleItem item:basket.stream().sorted(Comparator.comparing(SaleItem::part)).toList()) {
                BigDecimal price=(BigDecimal)QuyTrinhDAO.one(conn,"SELECT donGia FROM dbo.PhuTung WITH (HOLDLOCK) WHERE maPhuTung=?",item.part()).get("donGia");
                QuyTrinhDAO.require(price.compareTo(item.expectedPrice())==0,"Đơn giá đã thay đổi. Vui lòng tải lại.");
                Number available=(Number)QuyTrinhDAO.one(conn,"SELECT soLuongHienTai FROM dbo.TonKho WITH (UPDLOCK,HOLDLOCK) WHERE maPhuTung=?",item.part()).get("soLuongHienTai");
                QuyTrinhDAO.require(available.intValue()>=item.quantity(),"Không đủ tồn kho cho phụ tùng "+item.part()+".");
                subtotal=subtotal.add(price.multiply(BigDecimal.valueOf(item.quantity())));
            }
            Object method=QuyTrinhDAO.one(conn,"SELECT TOP (1) maPhuongThuc FROM dbo.PhuongThucThanhToan ORDER BY CASE WHEN tenPhuongThuc=N'Tiền mặt' THEN 0 ELSE 1 END,maPhuongThuc").get("maPhuongThuc");
            Map<String,Object> header=new LinkedHashMap<>();
            header.put("maKhachHang",resolvedCustomer); header.put("maNhanVien",employee);
            header.put("maPSC",null); header.put("VIN",null); header.put("ngayLap",QuyTrinhDAO.now(conn));
            header.put("tienPhuTung",subtotal); header.put("tienCong",BigDecimal.ZERO); header.put("dichVuKhac",BigDecimal.ZERO);
            HoaDon hoaDonTinhToan=new HoaDon();
            hoaDonTinhToan.setTienPhuTung(subtotal);
            hoaDonTinhToan.setTienCong(BigDecimal.ZERO);
            hoaDonTinhToan.setDichVuKhac(BigDecimal.ZERO);
            BigDecimal vat=hoaDonTinhToan.tinhThueVAT();
            hoaDonTinhToan.setThueVAT(vat);
            hoaDonTinhToan.setGiamGia(BigDecimal.ZERO);
            header.put("thueVAT",vat); header.put("giamGia",BigDecimal.ZERO);
            header.put("trangThaiThanhToan","Chưa thanh toán"); header.put("maPhuongThuc",method);
            String invoice=new CoSoDAO("HoaDon").insert(conn,header);
            for(SaleItem item:basket) QuyTrinhDAO.insertInvoiceLine(conn,Map.of("maHoaDon",invoice,"maPhuTung",item.part(),"soLuong",item.quantity(),"donGia",item.expectedPrice()));
            return invoice;
        });
    }
    public static void sellBasket(String invoice,String service,List<SaleItem> items) throws SQLException {
        QuyTrinhDAO.require(items!=null && !items.isEmpty(),"Giỏ hàng đang trống.");
        List<SaleItem> basket=List.copyOf(items);
        Set<String> codes=new HashSet<>();
        for(SaleItem item:basket) {
            QuyTrinhDAO.require(item.part()!=null && item.quantity()>0 && item.expectedPrice()!=null && item.expectedPrice().signum()>=0,"Phụ tùng hoặc số lượng không hợp lệ.");
            QuyTrinhDAO.require(!"PT000".equals(item.part()),"PT000 là mã biểu thị không sử dụng phụ tùng, không thể bán.");
            QuyTrinhDAO.require(codes.add(item.part()),"Giỏ hàng có mã phụ tùng trùng.");
        }
        QuyTrinhDAO.transaction(conn -> {
            Map<String,Object> initial=QuyTrinhDAO.one(conn,"SELECT maPSC FROM dbo.HoaDon WHERE maHoaDon=?",invoice);
            Map<String,Object> repair=QuyTrinhDAO.lock(conn,"PhieuSuaChua",initial.get("maPSC"));
            Map<String,Object> row=QuyTrinhDAO.lock(conn,"HoaDon",invoice);
            QuyTrinhDAO.require(Set.of("Đã sửa xong","Chờ thanh toán").contains(String.valueOf(repair.get("trangThai"))),"Phiếu sửa không còn cho phép bán bổ sung.");
            QuyTrinhDAO.require(Set.of("Chưa thanh toán","Chờ thanh toán").contains(String.valueOf(row.get("trangThaiThanhToan"))),"Hóa đơn đã thanh toán.");
            if(service!=null && !service.isBlank()) QuyTrinhDAO.require(QuyTrinhDAO.exists(conn,"SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=? AND maDichVu=?",row.get("maPSC"),service),"Dịch vụ không thuộc phiếu sửa chữa.");
            BigDecimal added=BigDecimal.ZERO;
            // Cùng thứ tự khóa tồn kho cho các giỏ hàng khác nhau.
            for(SaleItem item:basket.stream().sorted(Comparator.comparing(SaleItem::part)).toList()) {
            String part=item.part(); int quantity=item.quantity(); BigDecimal expectedPrice=item.expectedPrice();
            QuyTrinhDAO.require(!QuyTrinhDAO.exists(conn,"SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=? AND maPhuTung=?",row.get("maPSC"),part),"Phụ tùng này đã thuộc phiếu sửa chữa; không được bán/trừ kho lần nữa.");
            QuyTrinhDAO.require(!QuyTrinhDAO.exists(conn,"SELECT 1 FROM dbo.ChiTietHoaDon WHERE maHoaDon=? AND maPhuTung=?",invoice,part),"Phụ tùng đã có trong hóa đơn.");
            BigDecimal price=(BigDecimal)QuyTrinhDAO.one(conn,"SELECT donGia FROM dbo.PhuTung WITH (HOLDLOCK) WHERE maPhuTung=?",part).get("donGia");
            QuyTrinhDAO.require(price.compareTo(expectedPrice)==0,"Đơn giá đã thay đổi. Vui lòng tải lại.");
            QuyTrinhDAO.require(QuyTrinhDAO.execute(conn,"UPDATE dbo.TonKho SET soLuongHienTai=soLuongHienTai-? WHERE maPhuTung=? AND soLuongHienTai>=?",quantity,part,quantity)==1,"Không đủ tồn kho.");
            Map<String,Object> line=new LinkedHashMap<>(); line.put("maHoaDon",invoice); line.put("maDichVu",service==null || service.isBlank() ? null : service); line.put("maPhuTung",part); line.put("soLuong",quantity); line.put("donGia",price);
            QuyTrinhDAO.insertInvoiceLine(conn,line);
            added=added.add(price.multiply(BigDecimal.valueOf(quantity)));
            }
            BigDecimal parts=amount(row,"tienPhuTung").add(added);
            BigDecimal subtotal=parts.add(amount(row,"tienCong")).add(amount(row,"dichVuKhac"));
            BigDecimal discount=row.get("maGiamGia")==null ? amount(row,"giamGia") : QuyTrinhDAO.couponDiscount(conn,row.get("maGiamGia").toString(),row.get("maKhachHang"),subtotal);
            HoaDon hoaDonTinhToan=new HoaDon();
            hoaDonTinhToan.setTienPhuTung(parts);
            hoaDonTinhToan.setTienCong(amount(row,"tienCong"));
            hoaDonTinhToan.setDichVuKhac(amount(row,"dichVuKhac"));
            BigDecimal vat=hoaDonTinhToan.tinhThueVAT();
            hoaDonTinhToan.setThueVAT(vat);
            hoaDonTinhToan.setGiamGia(discount);
            QuyTrinhDAO.execute(conn,"UPDATE dbo.HoaDon SET tienPhuTung=?,thueVAT=?,giamGia=? WHERE maHoaDon=?",parts,vat,discount,invoice); return null;
        });
    }
    public static void allocate(String repair,String service,String part) throws SQLException {
        allocate(repair,service,part,null);
    }
    public static void allocate(String repair,String service,String part,String employee) throws SQLException {
        QuyTrinhDAO.require(!"PT000".equals(part),"PT000 không phải phụ tùng cần cấp phát.");
        QuyTrinhDAO.transaction(conn -> {
            Map<String,Object> header=QuyTrinhDAO.lock(conn,"PhieuSuaChua",repair);
            QuyTrinhDAO.require(Set.of("Chờ sửa chữa","Đang sửa chữa").contains(String.valueOf(header.get("trangThai"))),"Không thể cấp phát cho phiếu đã hoàn thành/hủy.");
            String actor=employee==null ? String.valueOf(header.get("maNhanVien")) : employee;
            QuyTrinhDAO.require(QuyTrinhDAO.exists(conn,"SELECT 1 FROM dbo.NhanVien WHERE maNhanVien=? AND trangThai=N'Đang làm' AND vaiTro IN (N'Nhân viên kho',N'Nhân viên quản lý',N'Quản trị viên')",actor),"Chỉ nhân viên kho/quản lý đang làm mới được cấp phát phụ tùng.");
            Map<String,Object> line=QuyTrinhDAO.one(conn,"SELECT * FROM dbo.ChiTietPhieuSuaChua WITH (UPDLOCK,HOLDLOCK) WHERE maPSC=? AND maDichVu=? AND maPhuTung=?",repair,service,part);
            QuyTrinhDAO.require("Chờ sửa chữa".equals(line.get("trangThaiHangMuc")),"Hạng mục chưa đủ tồn để cấp phát hoặc đã cấp phát.");
            int quantity=((Number)line.get("soLuongPhuTung")).intValue();
            QuyTrinhDAO.require(quantity>0 && ((Number)line.get("soLuongCapPhat")).intValue()==0,"Số lượng đã cấp phát không hợp lệ.");
            QuyTrinhDAO.execute(conn,"UPDATE dbo.ChiTietPhieuSuaChua SET trangThaiHangMuc=N'Đang sửa chữa',soLuongCapPhat=?,ngayCapPhat=?,maNhanVienCapPhat=? WHERE maPSC=? AND maDichVu=? AND maPhuTung=?",quantity,QuyTrinhDAO.now(conn),actor,repair,service,part);
            return null;
        });
    }

    private static void issueThresholdCoupons(Connection conn,String customer,Timestamp cycleStart,Timestamp expiration,Timestamp now) throws SQLException {
        int earned=((Number)QuyTrinhDAO.one(conn,"SELECT COALESCE(SUM(soDiem),0) AS points FROM dbo.LichSuTichDiem WHERE maKhachHang=? AND ngayTichDiem>=? AND ngayHetHan>=?",customer,cycleStart,now).get("points")).intValue();
        int[][] thresholds={{50,5},{100,10},{200,15},{500,30}};
        for(int[] threshold:thresholds) if(earned>=threshold[0] && !QuyTrinhDAO.exists(conn,"SELECT 1 FROM dbo.MaGiamGia WHERE maKhachHang=? AND mocDiem=? AND ngayBatDauChuKy=?",customer,threshold[0],cycleStart)) {
            Map<String,Object> coupon=new LinkedHashMap<>(); coupon.put("maKhachHang",customer); coupon.put("mucGiam",BigDecimal.valueOf(threshold[1])); coupon.put("ngayTao",now); coupon.put("ngayHetHan",expiration); coupon.put("trangThai","Đang hiệu lực"); coupon.put("mocDiem",threshold[0]); coupon.put("ngayBatDauChuKy",cycleStart);
            new CoSoDAO("MaGiamGia").insert(conn,coupon);
        }
    }

    private static void fulfillPartsInvoice(Connection conn,String invoiceId) throws SQLException {
        List<Map<String,Object>> items=new ArrayList<>();
        String sql="SELECT maPhuTung,SUM(soLuong) AS soLuong FROM dbo.ChiTietHoaDon WHERE maHoaDon=? AND maPhuTung IS NOT NULL GROUP BY maPhuTung ORDER BY maPhuTung";
        try(PreparedStatement ps=conn.prepareStatement(sql)) {
            CoSoDAO.bind(ps,List.of(invoiceId));
            try(ResultSet rs=ps.executeQuery()) {
                while(rs.next()) { Map<String,Object> item=new LinkedHashMap<>(); item.put("maPhuTung",rs.getString("maPhuTung")); item.put("soLuong",rs.getInt("soLuong")); items.add(item); }
            }
        }
        QuyTrinhDAO.require(!items.isEmpty(),"Hóa đơn bán phụ tùng chưa có dòng phụ tùng.");
        for(Map<String,Object> item:items) {
            String part=String.valueOf(item.get("maPhuTung")); int quantity=((Number)item.get("soLuong")).intValue();
            QuyTrinhDAO.require(QuyTrinhDAO.execute(conn,"UPDATE dbo.TonKho SET soLuongHienTai=soLuongHienTai-? WHERE maPhuTung=? AND soLuongHienTai>=?",quantity,part,quantity)==1,"Không đủ tồn kho khi thanh toán hóa đơn cho phụ tùng "+part+".");
        }
    }
}
