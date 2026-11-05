package dao;

import static dao.QuyTrinhDAO.*;
import static dao.ThuongMaiDAO.*;
import entity.HoaDon;
import java.math.BigDecimal;

import connectDB.KetNoiCSDL;
import java.sql.*;
import java.util.*;

// ===== DAO: TRUY VẤN VÀ GHI HÓA ĐƠN =====

/** SQL và thao tác dữ liệu riêng của HoaDon. */
public class HoaDonDAO implements DuLieuDAO {
    // ===== CẤU HÌNH VÀ TÊN BẢNG/CỘT =====
    private static final String KHOA_CHINH = "maHoaDon";
    private static final String[] CAC_COT = new String[] { "maHoaDon", "maGiamGia", "maKhachHang",
            "maNhanVien", "maPSC", "VIN", "ngayLap", "tienPhuTung", "tienCong", "dichVuKhac", "thueVAT",
            "giamGia", "trangThaiThanhToan", "maPhuongThuc" };
    private static final String[] CAC_KHOA = new String[] { "maHoaDon" };
    private static final Map<String, String> KIEU_COT = Map.ofEntries(Map.entry("maHoaDon", "varchar:5"),
            Map.entry("maGiamGia", "varchar:6"), Map.entry("maKhachHang", "varchar:5"),
            Map.entry("maNhanVien", "varchar:5"), Map.entry("maPSC", "varchar:6"),
            Map.entry("VIN", "varchar:17"), Map.entry("ngayLap", "datetime2:0"),
            Map.entry("tienPhuTung", "decimal:18,2"), Map.entry("tienCong", "decimal:18,2"),
            Map.entry("dichVuKhac", "decimal:18,2"), Map.entry("thueVAT", "decimal:18,2"),
            Map.entry("giamGia", "decimal:18,2"), Map.entry("trangThaiThanhToan", "nvarchar:50"),
            Map.entry("maPhuongThuc", "varchar:5"));
    private static final Set<String> COT_BAT_BUOC = Set.of("maHoaDon", "maKhachHang", "maNhanVien", "ngayLap",
            "tienPhuTung", "tienCong", "dichVuKhac", "thueVAT", "giamGia", "trangThaiThanhToan",
            "maPhuongThuc");

    private final String table = "HoaDon";
    private final String primary = KHOA_CHINH;

    // ===== TRUY VẤN VÀ THAO TÁC DỮ LIỆU =====

    // ===== PHƯƠNG THỨC: getAll(Connection, Map<String,Object>) — Lấy danh sách bản ghi từ cơ sở dữ liệu. =====
    @Override
    public List<LinkedHashMap<String, Object>> getAll(Connection conn, Map<String, Object> filter)
            throws SQLException {
        String where = filter == null || filter.isEmpty() ? "" : " WHERE " + conditions(filter);
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM dbo.[" + table + "]" + where + " ORDER BY [" + primary + "]")) {
            if (filter != null)
                bind(ps, filter.values());
            try (ResultSet rs = ps.executeQuery()) {
                List<LinkedHashMap<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    LinkedHashMap<String, Object> row = new LinkedHashMap<>();
                    for (String field : CAC_COT)
                        row.put(field, rs.getObject(field));
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    // ===== PHƯƠNG THỨC: previewNextId() — Tạo mã tiếp theo để xem trước khi lưu. =====
    @Override
    public String previewNextId() throws SQLException {
        if (CAC_KHOA.length != 1 || primary.equals("VIN"))
            return "";

        try (Connection conn = KetNoiCSDL.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT CASE WHEN current_value IS NULL THEN CONVERT(int,start_value) ELSE CONVERT(int,current_value)+1 END FROM sys.sequences WHERE name=? AND schema_id=SCHEMA_ID('dbo')")) {
            ps.setString(1, "Seq_" + table);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? prefix() + String.format(Locale.ROOT, "%03d", rs.getInt(1)) : "";
            }
        }
    }

    // ===== PHƯƠNG THỨC: prefix() — Xử lý prefix. =====
    private String prefix() {
        return "HD";
    }

    // ===== PHƯƠNG THỨC: insert(Map<String,Object>) — Thêm bản ghi vào cơ sở dữ liệu. =====
    @Override
    public void insert(Map<String, Object> row) throws SQLException {
        insert(row, null);
    }

    // ===== PHƯƠNG THỨC: insert(Map<String,Object>, String) — Thêm bản ghi vào cơ sở dữ liệu. =====
    @Override
    public void insert(Map<String, Object> row, String preview) throws SQLException {
        QuyTrinhDAO.transaction(conn -> {
            insert(conn, row);
            return null;
        });
    }

    // ===== PHƯƠNG THỨC: insert(Connection, Map<String,Object>) — Thêm bản ghi vào cơ sở dữ liệu. =====
    /** Dùng connection của phiếu để lưu đầu phiếu và chi tiết cùng giao dịch. */
    @Override
    public String insert(Connection conn, Map<String, Object> values) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>(values);
        if (CAC_KHOA.length == 1 && !primary.equals("VIN")) {
            {
                try (Statement ps = conn.createStatement();
                        ResultSet rs = ps.executeQuery("SELECT NEXT VALUE FOR dbo.[Seq_" + table + "]")) {
                    rs.next();
                    row.put(primary, prefix() + String.format(Locale.ROOT, "%03d", rs.getInt(1)));
                }
            }
        }
        kiemTraDuLieu(row, true);
        QuyTrinhDAO.guardWrite(conn, table, row, null);
        String fields = String.join(",", row.keySet().stream().map(s -> "[" + s + "]").toList());
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO dbo.[" + table + "](" + fields
                + ") VALUES(" + String.join(",", Collections.nCopies(row.size(), "?")) + ")")) {
            bind(ps, row.values());
            ps.executeUpdate();
        }
        return String.valueOf(row.get(primary));
    }

    // ===== PHƯƠNG THỨC: update(String, Map<String,Object>) — Cập nhật bản ghi trong cơ sở dữ liệu. =====
    @Override
    public void update(String id, Map<String, Object> row) throws SQLException {
        if (CAC_KHOA.length != 1)
            throw new SQLException("Cần đầy đủ khóa ghép để sửa chi tiết.");
        QuyTrinhDAO.transaction(conn -> {
            update(conn, Map.of(primary, id), row);
            return null;
        });
    }

    // ===== PHƯƠNG THỨC: update(Connection, Map<String,Object>, Map<String,Object>) — Cập nhật bản ghi trong cơ sở dữ liệu. =====
    @Override
    public void update(Connection conn, Map<String, Object> key, Map<String, Object> values)
            throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>(values);
        for (String field : CAC_KHOA)
            row.remove(field);
        kiemTraDuLieu(row, false);
        QuyTrinhDAO.guardWrite(conn, table, row, key);
        if (row.isEmpty())
            return;
        String fields = String.join(",", row.keySet().stream().map(s -> "[" + s + "]=?").toList());
        try (PreparedStatement ps = conn
                .prepareStatement("UPDATE dbo.[" + table + "] SET " + fields + " WHERE " + conditions(key))) {
            List<Object> params = new ArrayList<>(row.values());
            params.addAll(key.values());
            bind(ps, params);
            if (ps.executeUpdate() != 1)
                throw new SQLException("Bản ghi đã thay đổi hoặc không còn tồn tại.");
        }
    }

    // ===== PHƯƠNG THỨC: softDelete(String) — Đánh dấu bản ghi đã xóa mà vẫn giữ dữ liệu. =====
    @Override
    public void softDelete(String id) throws SQLException {
        HiddenRecords.hide(table, id);
    }

    // ===== PHƯƠNG THỨC: conditions(Map<String,Object>) — Tạo điều kiện truy vấn theo tiêu chí lọc. =====
    private String conditions(Map<String, Object> values) throws SQLException {
        for (String field : values.keySet())
            if (!KIEU_COT.containsKey(field))
                throw new SQLException("Trường không hợp lệ: " + field);
        return String.join(" AND ", values.keySet().stream().map(s -> "[" + s + "]=?").toList());
    }

    // ===== PHƯƠNG THỨC: bind(PreparedStatement, Collection<?>) — Gán giá trị tham số cho câu lệnh SQL. =====
    private static void bind(PreparedStatement ps, Collection<?> values) throws SQLException {
        int i = 1;
        for (Object value : values)
            ps.setObject(i++, value);
    }

    // ===== PHƯƠNG THỨC: getAll() — Lấy danh sách bản ghi từ cơ sở dữ liệu. =====
    @Override
    public java.util.List<java.util.LinkedHashMap<String, Object>> getAll() throws java.sql.SQLException {
        var rows = new java.util.ArrayList<java.util.LinkedHashMap<String, Object>>();
        String sql = "SELECT h.*, CONCAT(h.maKhachHang, CASE WHEN k.hoTenKH IS NULL OR LTRIM(RTRIM(k.hoTenKH))=N'' THEN N'' ELSE N' — '+k.hoTenKH END) AS khachHangHienThi FROM dbo.HoaDon h LEFT JOIN dbo.KhachHang k ON k.maKhachHang=h.maKhachHang ORDER BY h.maHoaDon";
        try (var conn = connectDB.KetNoiCSDL.getConnection();
                var ps = conn.prepareStatement(sql);
                var rs = ps.executeQuery()) {
            while (rs.next()) {
                if (HiddenRecords.isHidden("HoaDon", rs.getString("maHoaDon")))
                    continue;
                var row = new java.util.LinkedHashMap<String, Object>();
                for (String field : CAC_COT)
                    row.put(field, rs.getObject(field));
                row.put("khachHangHienThi", rs.getString("khachHangHienThi"));
                rows.add(row);
            }
        }
        return rows;
    }

    // Các thao tác nghiệp vụ của đối tượng, giữ nguyên giao dịch SQL.
    // ===== PHƯƠNG THỨC: createInvoice(String, String, String, String) — Lập hóa đơn từ thông tin giao dịch. =====
    public static String createInvoice(String repairId, String employee, String method, String coupon)
            throws SQLException {
        return transaction(conn -> {
            // Khóa ngoại bắt buộc: giá trị dự kiến sẽ được thay bằng phương thức thực tế khi thu tiền.
            String initialMethod = method;
            if (initialMethod == null || initialMethod.isBlank())
                initialMethod = String.valueOf(one(conn,
                        "SELECT TOP (1) maPhuongThuc FROM dbo.PhuongThucThanhToan ORDER BY CASE WHEN tenPhuongThuc=N'Tiền mặt' THEN 0 ELSE 1 END,maPhuongThuc")
                        .get("maPhuongThuc"));
            Map<String, Object> repair = lock(conn, "PhieuSuaChua", repairId);
            require("Đã sửa xong".equals(repair.get("trangThai")), "Chỉ lập hóa đơn sau nghiệm thu.");
            require(!exists(conn, "SELECT 1 FROM dbo.HoaDon WHERE maPSC=?", repairId),
                    "Phiếu sửa đã có hóa đơn.");
            List<LinkedHashMap<String, Object>> details = new dao.ChiTietPhieuSuaChuaDAO().getAll(conn,
                    Map.of("maPSC", repairId));
            require(!details.isEmpty(), "Phiếu sửa không có chi tiết.");
            BigDecimal parts = BigDecimal.ZERO, labor = BigDecimal.ZERO;
            List<Map<String, Object>> lines = new ArrayList<>();
            Map<Object, BigDecimal> serviceFees = new LinkedHashMap<>();
            for (Map<String, Object> line : details) {
                require("Hoàn thành".equals(line.get("trangThaiHangMuc"))
                        && "Đạt".equals(line.get("ketQuaNghiemThu")), "Còn hạng mục chưa nghiệm thu đạt.");
                Map<String, Object> price = one(conn,
                        "SELECT donGiaPhuTung,chiPhiNhanCong FROM dbo.ChiTietPhieuBaoGia WHERE maBaoGia=? AND maDichVu=? AND maPhuTung=?",
                        repair.get("maBaoGia"), line.get("maDichVu"), line.get("maPhuTung"));
                BigDecimal unit = (BigDecimal) price.get("donGiaPhuTung");
                int quantity = ((Number) line.get("soLuongPhuTung")).intValue();
                parts = parts.add(unit.multiply(BigDecimal.valueOf(quantity)));
                if (!"PT000".equals(line.get("maPhuTung"))) {
                    lines.add(Map.of("maDichVu", line.get("maDichVu"), "maPhuTung", line.get("maPhuTung"),
                            "soLuong", quantity, "donGia", unit));
                }
                serviceFees.merge(line.get("maDichVu"), (BigDecimal) price.get("chiPhiNhanCong"),
                        BigDecimal::add);
            }
            for (var fee : serviceFees.entrySet()) {
                labor = labor.add(fee.getValue());
                lines.add(Map.of("maDichVu", fee.getKey(), "soLuong", 1, "donGia", fee.getValue()));
            }
            BigDecimal subtotal = parts.add(labor);
            HoaDon hoaDonTinhToan = new HoaDon();
            hoaDonTinhToan.setTienPhuTung(parts);
            hoaDonTinhToan.setTienCong(labor);
            hoaDonTinhToan.setDichVuKhac(BigDecimal.ZERO);
            BigDecimal vat = hoaDonTinhToan.tinhThueVAT(), discount = BigDecimal.ZERO;
            if (coupon != null && !coupon.isBlank())
                discount = couponDiscount(conn, coupon, repair.get("maKhachHang"), subtotal);
            hoaDonTinhToan.setThueVAT(vat);
            hoaDonTinhToan.setGiamGia(discount);
            if (hoaDonTinhToan.tongThanhToan().signum() < 0)
                throw new SQLException("Tổng tiền hóa đơn không hợp lệ.");
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("maGiamGia", coupon == null || coupon.isBlank() ? null : coupon);
            header.put("maKhachHang", repair.get("maKhachHang"));
            header.put("maNhanVien", employee);
            header.put("maPSC", repairId);
            header.put("VIN", repair.get("VIN"));
            header.put("ngayLap", now(conn));
            header.put("tienPhuTung", parts);
            header.put("tienCong", labor);
            header.put("dichVuKhac", BigDecimal.ZERO);
            header.put("thueVAT", vat);
            header.put("giamGia", discount);
            header.put("trangThaiThanhToan", "Chưa thanh toán");
            header.put("maPhuongThuc", initialMethod);
            String id = new dao.HoaDonDAO().insert(conn, header);
            for (Map<String, Object> line : lines) {
                Map<String, Object> values = new LinkedHashMap<>(line);
                values.put("maHoaDon", id);
                // Hóa đơn được khóa và tạo ngay trong cùng giao dịch.
                insertInvoiceLine(conn, values);
            }
            execute(conn, "UPDATE dbo.PhieuSuaChua SET trangThai=N'Chờ thanh toán' WHERE maPSC=?", repairId);
            return id;
        });
    }

    // ===== PHƯƠNG THỨC: pay(String, String) — Ghi nhận thanh toán hóa đơn. =====
    public static void pay(String invoice, String method) throws SQLException {
        pay(invoice, method, null);
    }

    // ===== PHƯƠNG THỨC: pay(String, String, BigDecimal) — Ghi nhận thanh toán hóa đơn. =====
    public static void pay(String invoice, String method, BigDecimal expectedTotal) throws SQLException {
        pay(invoice, method, expectedTotal, null);
    }

    // ===== PHƯƠNG THỨC: pay(String, String, BigDecimal, String) — Ghi nhận thanh toán hóa đơn. =====
    public static void pay(String invoice, String method, BigDecimal expectedTotal, String scannedCustomer)
            throws SQLException {
        QuyTrinhDAO.transaction(conn -> {
            Map<String, Object> initial = QuyTrinhDAO.one(conn,
                    "SELECT maPSC FROM dbo.HoaDon WHERE maHoaDon=?", invoice);
            Map<String, Object> repair = initial.get("maPSC") == null ? null
                    : QuyTrinhDAO.lock(conn, "PhieuSuaChua", initial.get("maPSC"));
            Map<String, Object> row = QuyTrinhDAO.lock(conn, "HoaDon", invoice);
            HoaDon hoaDon = new HoaDon();
            hoaDon.setTienPhuTung(amount(row, "tienPhuTung"));
            hoaDon.setTienCong(amount(row, "tienCong"));
            hoaDon.setDichVuKhac(amount(row, "dichVuKhac"));
            hoaDon.setThueVAT(amount(row, "thueVAT"));
            hoaDon.setGiamGia(amount(row, "giamGia"));
            hoaDon.setTrangThaiThanhToan(String.valueOf(row.get("trangThaiThanhToan")));
            if (scannedCustomer != null)
                QuyTrinhDAO.require(
                        scannedCustomer.matches("KH[0-9]{3}")
                                && scannedCustomer.equals(row.get("maKhachHang")),
                        "QR tích điểm không khớp khách hàng của hóa đơn.");
            QuyTrinhDAO.require(
                    Set.of("Chưa thanh toán", "Chờ thanh toán").contains(hoaDon.getTrangThaiThanhToan()),
                    "Hóa đơn không còn chờ thanh toán.");
            QuyTrinhDAO.require(
                    repair == null || Set.of("Đã sửa xong", "Chờ thanh toán")
                            .contains(String.valueOf(repair.get("trangThai"))),
                    "Phiếu sửa chưa sẵn sàng thanh toán.");
            QuyTrinhDAO.require(
                    QuyTrinhDAO.exists(conn, "SELECT 1 FROM dbo.ChiTietHoaDon WHERE maHoaDon=?", invoice),
                    "Hóa đơn chưa có chi tiết.");
            Map<String, Object> totals = QuyTrinhDAO.one(conn,
                    "SELECT COALESCE(SUM(CASE WHEN maPhuTung IS NOT NULL THEN soLuong*donGia ELSE 0 END),0) AS partsSum, COALESCE(SUM(CASE WHEN maPhuTung IS NULL AND maDichVu IS NOT NULL THEN soLuong*donGia ELSE 0 END),0) AS laborSum FROM dbo.ChiTietHoaDon WHERE maHoaDon=?",
                    invoice);
            QuyTrinhDAO.require(
                    amount(row, "tienPhuTung").compareTo((BigDecimal) totals.get("partsSum")) == 0
                            && amount(row, "tienCong").compareTo((BigDecimal) totals.get("laborSum")) == 0,
                    "Tổng tiền đầu hóa đơn không khớp chi tiết. Cần kiểm tra hóa đơn trước khi thu tiền.");
            QuyTrinhDAO
                    .require(
                            QuyTrinhDAO.exists(conn,
                                    "SELECT 1 FROM dbo.PhuongThucThanhToan WHERE maPhuongThuc=?", method),
                            "Phương thức thanh toán không tồn tại.");
            if (repair != null)
                QuyTrinhDAO.require(!QuyTrinhDAO.exists(conn,
                        "SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=? AND (trangThaiHangMuc<>N'Hoàn thành' OR ketQuaNghiemThu<>N'Đạt')",
                        row.get("maPSC")), "Còn hạng mục chưa nghiệm thu đạt.");
            BigDecimal subtotal = amount(row, "tienPhuTung").add(amount(row, "tienCong"))
                    .add(amount(row, "dichVuKhac"));
            BigDecimal discount = hoaDon.getGiamGia() == null ? BigDecimal.ZERO : hoaDon.getGiamGia();
            Object coupon = row.get("maGiamGia");
            if (coupon != null) {
                QuyTrinhDAO.require(repair != null,
                        "Mã giảm giá điểm chỉ áp dụng cho hóa đơn sửa chữa/dịch vụ.");
                discount = QuyTrinhDAO.couponDiscount(conn, coupon.toString(), row.get("maKhachHang"),
                        subtotal);
                QuyTrinhDAO.execute(conn,
                        "UPDATE dbo.MaGiamGia SET trangThai=N'Đã sử dụng' WHERE maGiamGia=?", coupon);
            }
            hoaDon.setGiamGia(discount);
            BigDecimal paid = hoaDon.tongThanhToan();
            QuyTrinhDAO.require(paid.signum() >= 0, "Tổng tiền thanh toán không hợp lệ.");
            QuyTrinhDAO.require(expectedTotal == null || paid.compareTo(expectedTotal) == 0,
                    "Số tiền hóa đơn đã thay đổi. Vui lòng tải lại và tạo lại QR trước khi xác nhận.");
            String trangThaiMoi = hoaDon.capNhatTrangThai("Đã thanh toán");
            QuyTrinhDAO.execute(conn,
                    "UPDATE dbo.HoaDon SET trangThaiThanhToan=?,maPhuongThuc=?,giamGia=? WHERE maHoaDon=?",
                    trangThaiMoi, method, discount, invoice);
            if (repair != null)
                QuyTrinhDAO.execute(conn,
                        "UPDATE dbo.PhieuSuaChua SET trangThai=N'Đã thanh toán' WHERE maPSC=?",
                        row.get("maPSC"));
            // Một hóa đơn chỉ sinh một lịch sử điểm, kể cả người dùng bấm lại.
            if (repair != null && scannedCustomer != null
                    && !QuyTrinhDAO.exists(conn,
                            "SELECT 1 FROM dbo.LichSuTichDiem " + "WITH (UPDLOCK,HOLDLOCK) WHERE maHoaDon=?",
                            invoice)) {
                Timestamp earnedAt = QuyTrinhDAO.now(conn);
                BigDecimal eligible = amount(row, "tienPhuTung").add(amount(row, "tienCong"))
                        .add(amount(row, "dichVuKhac")).add(amount(row, "thueVAT"));
                int points = InvoiceMath.points(eligible);
                Timestamp cycleStart = (Timestamp) QuyTrinhDAO.one(conn,
                        "SELECT MIN(ngayTichDiem) AS firstEarn FROM dbo.LichSuTichDiem WHERE maKhachHang=? AND ngayHetHan>=?",
                        row.get("maKhachHang"), earnedAt).get("firstEarn");
                if (cycleStart == null)
                    cycleStart = earnedAt;
                Timestamp expiration = Timestamp.valueOf(cycleStart.toLocalDateTime().plusYears(1));
                new dao.LichSuTichDiemDAO().insert(conn, Map.of("maKhachHang", row.get("maKhachHang"),
                        "maHoaDon", invoice, "ngayTichDiem", earnedAt, "ngayHetHan", expiration, "soDiem",
                        points, "lyDo",
                        "Tích điểm trên tổng sau VAT, trước giảm giá; 100.000đ = 1 điểm, làm tròn xuống."));
                issueThresholdCoupons(conn, String.valueOf(row.get("maKhachHang")), cycleStart, expiration,
                        earnedAt);
            }
            if (repair == null)
                fulfillPartsInvoice(conn, invoice);
            return null;
        });
    }

    // ===== PHƯƠNG THỨC: amount(Map<String,Object>, String) — Đọc giá trị tiền từ một cột dữ liệu. =====
    static BigDecimal amount(Map<String, Object> row, String field) {
        return (BigDecimal) row.get(field);
    }

    // ===== PHƯƠNG THỨC: changeCoupon(String, String) — Cập nhật mã giảm giá trên hóa đơn. =====
    public static void changeCoupon(String invoice, String coupon) throws SQLException {
        QuyTrinhDAO.transaction(conn -> {
            Map<String, Object> initial = QuyTrinhDAO.one(conn,
                    "SELECT maPSC FROM dbo.HoaDon WHERE maHoaDon=?", invoice);
            if (initial.get("maPSC") != null)
                QuyTrinhDAO.lock(conn, "PhieuSuaChua", initial.get("maPSC"));
            Map<String, Object> row = QuyTrinhDAO.lock(conn, "HoaDon", invoice);
            QuyTrinhDAO.require(
                    Set.of("Chưa thanh toán", "Chờ thanh toán")
                            .contains(String.valueOf(row.get("trangThaiThanhToan"))),
                    "Chỉ đổi mã giảm giá trước thanh toán.");
            BigDecimal subtotal = amount(row, "tienPhuTung").add(amount(row, "tienCong"))
                    .add(amount(row, "dichVuKhac"));
            boolean empty = coupon == null || coupon.isBlank();
            QuyTrinhDAO.require(empty || initial.get("maPSC") != null,
                    "Mã giảm giá điểm chỉ áp dụng cho hóa đơn sửa chữa/dịch vụ.");
            BigDecimal discount = empty ? BigDecimal.ZERO
                    : QuyTrinhDAO.couponDiscount(conn, coupon, row.get("maKhachHang"), subtotal);
            QuyTrinhDAO.execute(conn, "UPDATE dbo.HoaDon SET maGiamGia=?,giamGia=? WHERE maHoaDon=?",
                    empty ? null : coupon, discount, invoice);
            return null;
        });
    }

    // ===== PHƯƠNG THỨC: sell(String, String, String, int, BigDecimal) — Ghi nhận bán dịch vụ hoặc phụ tùng vào hóa đơn. =====
    public static void sell(String invoice, String service, String part, int quantity,
            BigDecimal expectedPrice) throws SQLException {
        sellBasket(invoice, service, List.of(new SaleItem(part, quantity, expectedPrice)));
    }

    // ===== PHƯƠNG THỨC: createPartsInvoice(String, String, List<SaleItem>) — Lập hóa đơn bán phụ tùng. =====
    public static String createPartsInvoice(String customer, String employee, List<SaleItem> items)
            throws SQLException {
        return createPartsInvoice(customer, null, employee, items);
    }

    // ===== PHƯƠNG THỨC: createPartsInvoice(Map<String,Object>, String, List<SaleItem>) — Lập hóa đơn bán phụ tùng. =====
    public static String createPartsInvoice(Map<String, Object> newCustomer, String employee,
            List<SaleItem> items) throws SQLException {
        return createPartsInvoice(null, newCustomer, employee, items);
    }

    // ===== PHƯƠNG THỨC: createPartsInvoice(String, Map<String,Object>, String, List<SaleItem>) — Lập hóa đơn bán phụ tùng. =====
    static String createPartsInvoice(String customer, Map<String, Object> newCustomer, String employee,
            List<SaleItem> items) throws SQLException {
        boolean existingCustomer = customer != null && !customer.isBlank();
        QuyTrinhDAO.require(
                (existingCustomer || newCustomer != null) && employee != null && !employee.isBlank(),
                "Vui lòng nhập thông tin khách hàng và nhân viên bán hàng.");
        QuyTrinhDAO.require(items != null && !items.isEmpty(), "Giỏ hàng đang trống.");
        List<SaleItem> basket = List.copyOf(items);
        Set<String> codes = new HashSet<>();
        for (SaleItem item : basket) {
            QuyTrinhDAO
                    .require(
                            item != null && item.part() != null && item.quantity() > 0
                                    && item.expectedPrice() != null && item.expectedPrice().signum() >= 0,
                            "Phụ tùng hoặc số lượng không hợp lệ.");
            QuyTrinhDAO.require(!"PT000".equals(item.part()),
                    "PT000 là mã biểu thị không sử dụng phụ tùng, không thể bán.");
            QuyTrinhDAO.require(codes.add(item.part()), "Giỏ hàng có mã phụ tùng trùng.");
        }
        return QuyTrinhDAO.transaction(conn -> {
            QuyTrinhDAO.require(QuyTrinhDAO.exists(conn,
                    "SELECT 1 FROM dbo.NhanVien WHERE maNhanVien=? AND trangThai=N'Đang làm'", employee),
                    "Nhân viên bán hàng không hợp lệ.");
            String resolvedCustomer;
            if (existingCustomer) {
                QuyTrinhDAO.lock(conn, "KhachHang", customer);
                resolvedCustomer = customer;
            } else {
                resolvedCustomer = new dao.KhachHangDAO().insert(conn, newCustomer);
            }
            BigDecimal subtotal = BigDecimal.ZERO;
            for (SaleItem item : basket.stream().sorted(Comparator.comparing(SaleItem::part)).toList()) {
                BigDecimal price = (BigDecimal) QuyTrinhDAO.one(conn,
                        "SELECT donGia FROM dbo.PhuTung WITH (HOLDLOCK) WHERE maPhuTung=?", item.part())
                        .get("donGia");
                QuyTrinhDAO.require(price.compareTo(item.expectedPrice()) == 0,
                        "Đơn giá đã thay đổi. Vui lòng tải lại.");
                Number available = (Number) QuyTrinhDAO.one(conn,
                        "SELECT soLuongHienTai FROM dbo.TonKho WITH (UPDLOCK,HOLDLOCK) WHERE maPhuTung=?",
                        item.part()).get("soLuongHienTai");
                QuyTrinhDAO.require(available.intValue() >= item.quantity(),
                        "Không đủ tồn kho cho phụ tùng " + item.part() + ".");
                subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(item.quantity())));
            }
            Object method = QuyTrinhDAO.one(conn,
                    "SELECT TOP (1) maPhuongThuc FROM dbo.PhuongThucThanhToan ORDER BY CASE WHEN tenPhuongThuc=N'Tiền mặt' THEN 0 ELSE 1 END,maPhuongThuc")
                    .get("maPhuongThuc");
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("maKhachHang", resolvedCustomer);
            header.put("maNhanVien", employee);
            header.put("maPSC", null);
            header.put("VIN", null);
            header.put("ngayLap", QuyTrinhDAO.now(conn));
            header.put("tienPhuTung", subtotal);
            header.put("tienCong", BigDecimal.ZERO);
            header.put("dichVuKhac", BigDecimal.ZERO);
            HoaDon hoaDonTinhToan = new HoaDon();
            hoaDonTinhToan.setTienPhuTung(subtotal);
            hoaDonTinhToan.setTienCong(BigDecimal.ZERO);
            hoaDonTinhToan.setDichVuKhac(BigDecimal.ZERO);
            BigDecimal vat = hoaDonTinhToan.tinhThueVAT();
            hoaDonTinhToan.setThueVAT(vat);
            hoaDonTinhToan.setGiamGia(BigDecimal.ZERO);
            header.put("thueVAT", vat);
            header.put("giamGia", BigDecimal.ZERO);
            header.put("trangThaiThanhToan", "Chưa thanh toán");
            header.put("maPhuongThuc", method);
            String invoice = new dao.HoaDonDAO().insert(conn, header);
            for (SaleItem item : basket)
                QuyTrinhDAO.insertInvoiceLine(conn, Map.of("maHoaDon", invoice, "maPhuTung", item.part(),
                        "soLuong", item.quantity(), "donGia", item.expectedPrice()));
            return invoice;
        });
    }

    // ===== PHƯƠNG THỨC: sellBasket(String, String, List<SaleItem>) — Ghi nhận nhiều mặt hàng bán trong một lượt. =====
    public static void sellBasket(String invoice, String service, List<SaleItem> items) throws SQLException {
        QuyTrinhDAO.require(items != null && !items.isEmpty(), "Giỏ hàng đang trống.");
        List<SaleItem> basket = List.copyOf(items);
        Set<String> codes = new HashSet<>();
        for (SaleItem item : basket) {
            QuyTrinhDAO.require(item.part() != null && item.quantity() > 0 && item.expectedPrice() != null
                    && item.expectedPrice().signum() >= 0, "Phụ tùng hoặc số lượng không hợp lệ.");
            QuyTrinhDAO.require(!"PT000".equals(item.part()),
                    "PT000 là mã biểu thị không sử dụng phụ tùng, không thể bán.");
            QuyTrinhDAO.require(codes.add(item.part()), "Giỏ hàng có mã phụ tùng trùng.");
        }
        QuyTrinhDAO.transaction(conn -> {
            Map<String, Object> initial = QuyTrinhDAO.one(conn,
                    "SELECT maPSC FROM dbo.HoaDon WHERE maHoaDon=?", invoice);
            Map<String, Object> repair = QuyTrinhDAO.lock(conn, "PhieuSuaChua", initial.get("maPSC"));
            Map<String, Object> row = QuyTrinhDAO.lock(conn, "HoaDon", invoice);
            QuyTrinhDAO.require(
                    Set.of("Đã sửa xong", "Chờ thanh toán").contains(String.valueOf(repair.get("trangThai"))),
                    "Phiếu sửa không còn cho phép bán bổ sung.");
            QuyTrinhDAO.require(Set.of("Chưa thanh toán", "Chờ thanh toán")
                    .contains(String.valueOf(row.get("trangThaiThanhToan"))), "Hóa đơn đã thanh toán.");
            if (service != null && !service.isBlank())
                QuyTrinhDAO.require(QuyTrinhDAO.exists(conn,
                        "SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=? AND maDichVu=?",
                        row.get("maPSC"), service), "Dịch vụ không thuộc phiếu sửa chữa.");
            BigDecimal added = BigDecimal.ZERO;
            // Cùng thứ tự khóa tồn kho cho các giỏ hàng khác nhau.
            for (SaleItem item : basket.stream().sorted(Comparator.comparing(SaleItem::part)).toList()) {
                String part = item.part();
                int quantity = item.quantity();
                BigDecimal expectedPrice = item.expectedPrice();
                QuyTrinhDAO.require(
                        !QuyTrinhDAO.exists(conn,
                                "SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=? AND maPhuTung=?",
                                row.get("maPSC"), part),
                        "Phụ tùng này đã thuộc phiếu sửa chữa; không được bán/trừ kho lần nữa.");
                QuyTrinhDAO.require(!QuyTrinhDAO.exists(conn,
                        "SELECT 1 FROM dbo.ChiTietHoaDon WHERE maHoaDon=? AND maPhuTung=?", invoice, part),
                        "Phụ tùng đã có trong hóa đơn.");
                BigDecimal price = (BigDecimal) QuyTrinhDAO
                        .one(conn, "SELECT donGia FROM dbo.PhuTung WITH (HOLDLOCK) WHERE maPhuTung=?", part)
                        .get("donGia");
                QuyTrinhDAO.require(price.compareTo(expectedPrice) == 0,
                        "Đơn giá đã thay đổi. Vui lòng tải lại.");
                QuyTrinhDAO.require(QuyTrinhDAO.execute(conn,
                        "UPDATE dbo.TonKho SET soLuongHienTai=soLuongHienTai-? WHERE maPhuTung=? AND soLuongHienTai>=?",
                        quantity, part, quantity) == 1, "Không đủ tồn kho.");
                Map<String, Object> line = new LinkedHashMap<>();
                line.put("maHoaDon", invoice);
                line.put("maDichVu", service == null || service.isBlank() ? null : service);
                line.put("maPhuTung", part);
                line.put("soLuong", quantity);
                line.put("donGia", price);
                QuyTrinhDAO.insertInvoiceLine(conn, line);
                added = added.add(price.multiply(BigDecimal.valueOf(quantity)));
            }
            BigDecimal parts = amount(row, "tienPhuTung").add(added);
            BigDecimal subtotal = parts.add(amount(row, "tienCong")).add(amount(row, "dichVuKhac"));
            BigDecimal discount = row.get("maGiamGia") == null ? amount(row, "giamGia")
                    : QuyTrinhDAO.couponDiscount(conn, row.get("maGiamGia").toString(),
                            row.get("maKhachHang"), subtotal);
            HoaDon hoaDonTinhToan = new HoaDon();
            hoaDonTinhToan.setTienPhuTung(parts);
            hoaDonTinhToan.setTienCong(amount(row, "tienCong"));
            hoaDonTinhToan.setDichVuKhac(amount(row, "dichVuKhac"));
            BigDecimal vat = hoaDonTinhToan.tinhThueVAT();
            hoaDonTinhToan.setThueVAT(vat);
            hoaDonTinhToan.setGiamGia(discount);
            QuyTrinhDAO.execute(conn,
                    "UPDATE dbo.HoaDon SET tienPhuTung=?,thueVAT=?,giamGia=? WHERE maHoaDon=?", parts, vat,
                    discount, invoice);
            return null;
        });
    }

    // ===== PHƯƠNG THỨC: fulfillPartsInvoice(Connection, String) — Trừ tồn kho cho phụ tùng đã bán. =====
    static void fulfillPartsInvoice(Connection conn, String invoiceId) throws SQLException {
        List<Map<String, Object>> items = new ArrayList<>();
        String sql = "SELECT maPhuTung,SUM(soLuong) AS soLuong FROM dbo.ChiTietHoaDon WHERE maHoaDon=? AND maPhuTung IS NOT NULL GROUP BY maPhuTung ORDER BY maPhuTung";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("maPhuTung", rs.getString("maPhuTung"));
                    item.put("soLuong", rs.getInt("soLuong"));
                    items.add(item);
                }
            }
        }
        QuyTrinhDAO.require(!items.isEmpty(), "Hóa đơn bán phụ tùng chưa có dòng phụ tùng.");
        for (Map<String, Object> item : items) {
            String part = String.valueOf(item.get("maPhuTung"));
            int quantity = ((Number) item.get("soLuong")).intValue();
            QuyTrinhDAO.require(QuyTrinhDAO.execute(conn,
                    "UPDATE dbo.TonKho SET soLuongHienTai=soLuongHienTai-? WHERE maPhuTung=? AND soLuongHienTai>=?",
                    quantity, part, quantity) == 1,
                    "Không đủ tồn kho khi thanh toán hóa đơn cho phụ tùng " + part + ".");
        }
    }

    // ===== PHƯƠNG THỨC: cacKhoa() — Liệt kê các cột khóa của bản ghi. =====
    @Override
    public String[] cacKhoa() {
        return CAC_KHOA.clone();
    }

    // ===== PHƯƠNG THỨC: khoaChinh() — Xác định cột khóa chính của bảng dữ liệu. =====
    @Override
    public String khoaChinh() {
        return KHOA_CHINH;
    }

    // ===== PHƯƠNG THỨC: cacCot() — Liệt kê các cột dữ liệu của đối tượng. =====
    @Override
    public String[] cacCot() {
        return CAC_COT.clone();
    }

    // ===== PHƯƠNG THỨC: kieuCot() — Khai báo kiểu dữ liệu của từng cột. =====
    @Override
    public Map<String, String> kieuCot() {
        return KIEU_COT;
    }

    // ===== PHƯƠNG THỨC: cotBatBuoc() — Liệt kê các cột bắt buộc nhập. =====
    @Override
    public Set<String> cotBatBuoc() {
        return COT_BAT_BUOC;
    }

    // ===== PHƯƠNG THỨC: kiemTraDuLieu(Map<String,Object>, boolean) — Kiểm tra dữ liệu. =====
    private static void kiemTraDuLieu(Map<String, Object> row, boolean creating) throws SQLException {
        if (creating) {
            Set<String> required = COT_BAT_BUOC;
            for (String field : required) {
                Object val = row.get(field);
                if (val == null || (val instanceof String s && s.trim().isEmpty()))
                    fail(field, "Bắt buộc nhập: " + field);
            }
        }
        for (Map.Entry<String, Object> e : row.entrySet()) {
            String field = e.getKey();
            Object value = e.getValue();
            if (value == null)
                continue;
            String raw = value.toString().trim();
            String[] spec = KIEU_COT.get(field).split(":", 2);
            if ((spec[0].equals("varchar") || spec[0].equals("nvarchar"))
                    && raw.length() > Integer.parseInt(spec[1]))
                fail(field, "Vượt quá độ dài cho phép: " + field);
            if (spec[0].equals("bit") && !(value instanceof Boolean))
                fail(field, "Giới tính phải là Nam hoặc Nữ.");
            if (value instanceof Number number) {
                BigDecimal n = new BigDecimal(number.toString());
                if (n.signum() < 0)
                    fail(field, "Giá trị không được âm: " + field);

                if (spec[0].equals("decimal")) {
                    String[] size = spec[1].split(",");
                    BigDecimal normalized = n.stripTrailingZeros();
                    if (Math.max(0, normalized.scale()) > Integer.parseInt(size[1].trim()) || Math.max(0,
                            normalized.precision() - normalized.scale()) > Integer.parseInt(size[0].trim())
                                    - Integer.parseInt(size[1].trim()))
                        fail(field, "Số tiền vượt độ chính xác cho phép: " + field);
                }
            }

            if (field.equals("maNhanVien") && !raw.matches("NV[0-9]{3}"))
                fail(field, "Mã nhân viên phải có dạng NV001.");

            if (field.equals("maKhachHang") && !raw.matches("KH[0-9]{3}"))
                fail(field, "Mã khách hàng phải có dạng KH001.");

            if (field.equals("maGiamGia") && !raw.matches("MGG[0-9]{3}"))
                fail(field, "Mã giảm giá phải có dạng MGG001.");

            if (field.equals("VIN") && !raw.matches("[A-HJ-NPR-Z0-9]{17}"))
                fail(field, "VIN phải gồm 17 ký tự hợp lệ.");

        }

    }

    // ===== PHƯƠNG THỨC: fail(String, String) — Tạo lỗi truy vấn khi thao tác nghiệp vụ không hợp lệ. =====
    private static void fail(String field, String message) throws SQLException {
        throw new LoiDuLieu(field, message);
    }
}
