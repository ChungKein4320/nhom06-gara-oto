package dao;

import static dao.QuyTrinhDAO.*;
import entity.HoaDon;
import entity.MaGiamGia;
import java.math.BigDecimal;

import connectDB.KetNoiCSDL;
import java.sql.*;
import java.util.*;

// ===== DAO: TRUY VẤN VÀ GHI MÃ GIẢM GIÁ =====

/** SQL và thao tác dữ liệu riêng của MaGiamGia. */
public class MaGiamGiaDAO implements DuLieuDAO {
    // ===== CẤU HÌNH VÀ TÊN BẢNG/CỘT =====
    private static final String KHOA_CHINH = "maGiamGia";
    private static final String[] CAC_COT = new String[] { "maGiamGia", "maKhachHang", "mucGiam", "ngayTao",
            "ngayHetHan", "trangThai", "mocDiem", "ngayBatDauChuKy" };
    private static final String[] CAC_KHOA = new String[] { "maGiamGia" };
    private static final Map<String, String> KIEU_COT = Map.ofEntries(Map.entry("maGiamGia", "varchar:6"),
            Map.entry("maKhachHang", "varchar:5"), Map.entry("mucGiam", "decimal:5,2"),
            Map.entry("ngayTao", "datetime2:0"), Map.entry("ngayHetHan", "datetime2:0"),
            Map.entry("trangThai", "nvarchar:50"), Map.entry("mocDiem", "int:"),
            Map.entry("ngayBatDauChuKy", "datetime2:0"));
    private static final Set<String> COT_BAT_BUOC = Set.of("maGiamGia", "maKhachHang", "mucGiam", "ngayTao",
            "ngayHetHan", "trangThai", "mocDiem", "ngayBatDauChuKy");

    private final String table = "MaGiamGia";
    private final String primary = KHOA_CHINH;

    // ===== TRUY VẤN VÀ THAO TÁC DỮ LIỆU =====

    // ===== PHƯƠNG THỨC: getAll() — Lấy danh sách bản ghi từ cơ sở dữ liệu. =====
    @Override
    public List<LinkedHashMap<String, Object>> getAll() throws SQLException {
        try (Connection conn = KetNoiCSDL.getConnection()) {
            List<LinkedHashMap<String, Object>> rows = getAll(conn, null);
            rows.removeIf(row -> HiddenRecords.isHidden(table, String.valueOf(row.get(primary))));
            return rows;
        }
    }

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
        return "MGG";
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

    // Các thao tác nghiệp vụ của đối tượng, giữ nguyên giao dịch SQL.
    // ===== PHƯƠNG THỨC: couponDiscount(Connection, String, Object, BigDecimal) — Tính mức giảm giá áp dụng từ mã ưu đãi. =====
    static BigDecimal couponDiscount(Connection conn, String coupon, Object customer, BigDecimal subtotal)
            throws SQLException {
        Map<String, Object> value = lock(conn, "MaGiamGia", coupon);
        require(Objects.equals(value.get("maKhachHang"), customer),
                "Mã giảm giá không thuộc khách hàng này.");
        require("Đang hiệu lực".equals(value.get("trangThai")) && exists(conn,
                "SELECT 1 FROM dbo.MaGiamGia WHERE maGiamGia=? AND ngayTao<=SYSDATETIME() AND ngayHetHan>=SYSDATETIME()",
                coupon), "Mã giảm giá không còn hiệu lực.");
        require(!exists(conn,
                "SELECT 1 FROM dbo.HoaDon WHERE maGiamGia=? AND trangThaiThanhToan=N'Đã thanh toán'", coupon),
                "Mã giảm giá đã được sử dụng.");
        HoaDon hoaDonTinhToan = new HoaDon();
        hoaDonTinhToan.setTienPhuTung(subtotal);
        hoaDonTinhToan.setTienCong(BigDecimal.ZERO);
        hoaDonTinhToan.setDichVuKhac(BigDecimal.ZERO);
        MaGiamGia maGiamGia = new MaGiamGia();
        maGiamGia.setMaGiamGia(coupon);
        maGiamGia.setMucGiam((BigDecimal) value.get("mucGiam"));
        hoaDonTinhToan.setMaGiamGia(maGiamGia);
        return hoaDonTinhToan.tinhGiamGia();
    }

    // ===== PHƯƠNG THỨC: issueThresholdCoupons(Connection, String, Timestamp, Timestamp, Timestamp) — Tạo mã giảm giá khi khách đạt ngưỡng tích điểm. =====
    static void issueThresholdCoupons(Connection conn, String customer, Timestamp cycleStart,
            Timestamp expiration, Timestamp now) throws SQLException {
        int earned = ((Number) QuyTrinhDAO.one(conn,
                "SELECT COALESCE(SUM(soDiem),0) AS points FROM dbo.LichSuTichDiem WHERE maKhachHang=? AND ngayTichDiem>=? AND ngayHetHan>=?",
                customer, cycleStart, now).get("points")).intValue();
        int[][] thresholds = { { 50, 5 }, { 100, 10 }, { 200, 15 }, { 500, 30 } };
        for (int[] threshold : thresholds)
            if (earned >= threshold[0] && !QuyTrinhDAO.exists(conn,
                    "SELECT 1 FROM dbo.MaGiamGia WHERE maKhachHang=? AND mocDiem=? AND ngayBatDauChuKy=?",
                    customer, threshold[0], cycleStart)) {
                Map<String, Object> coupon = new LinkedHashMap<>();
                coupon.put("maKhachHang", customer);
                coupon.put("mucGiam", BigDecimal.valueOf(threshold[1]));
                coupon.put("ngayTao", now);
                coupon.put("ngayHetHan", expiration);
                coupon.put("trangThai", "Đang hiệu lực");
                coupon.put("mocDiem", threshold[0]);
                coupon.put("ngayBatDauChuKy", cycleStart);
                new dao.MaGiamGiaDAO().insert(conn, coupon);
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

                if (field.equals("mucGiam") && n.compareTo(new BigDecimal("100")) > 0)
                    fail(field, "Mức giảm phải từ 0 đến 100%.");
                if (spec[0].equals("decimal")) {
                    String[] size = spec[1].split(",");
                    BigDecimal normalized = n.stripTrailingZeros();
                    if (Math.max(0, normalized.scale()) > Integer.parseInt(size[1].trim()) || Math.max(0,
                            normalized.precision() - normalized.scale()) > Integer.parseInt(size[0].trim())
                                    - Integer.parseInt(size[1].trim()))
                        fail(field, "Số tiền vượt độ chính xác cho phép: " + field);
                }
            }

            if (field.equals("maKhachHang") && !raw.matches("KH[0-9]{3}"))
                fail(field, "Mã khách hàng phải có dạng KH001.");

            if (field.equals("maGiamGia") && !raw.matches("MGG[0-9]{3}"))
                fail(field, "Mã giảm giá phải có dạng MGG001.");

        }
        dates(row, "ngayTao", "ngayHetHan", true);

    }

    // ===== PHƯƠNG THỨC: dates(Map<String,Object>, String, String, boolean) — Đọc và kiểm tra các mốc ngày. =====
    private static void dates(Map<String, Object> row, String start, String end, boolean strict)
            throws SQLException {
        if (row.get(start) instanceof java.util.Date a && row.get(end) instanceof java.util.Date b
                && (strict ? !b.after(a) : b.before(a)))
            fail(end, "Ngày kết thúc không hợp lệ.");
    }

    // ===== PHƯƠNG THỨC: fail(String, String) — Tạo lỗi truy vấn khi thao tác nghiệp vụ không hợp lệ. =====
    private static void fail(String field, String message) throws SQLException {
        throw new LoiDuLieu(field, message);
    }
}
