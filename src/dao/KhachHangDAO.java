package dao;

import java.math.BigDecimal;

import connectDB.KetNoiCSDL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.*;

// ===== DAO: TRUY VẤN VÀ GHI KHÁCH HÀNG =====

/** SQL và thao tác dữ liệu riêng của KhachHang. */
public class KhachHangDAO implements DuLieuDAO {
    // ===== CẤU HÌNH VÀ TÊN BẢNG/CỘT =====
    private static final String KHOA_CHINH = "maKhachHang";
    private static final String[] CAC_COT = new String[] { "maKhachHang", "hoTenKH", "soDienThoai",
            "gioiTinh", "email", "diaChi" };
    private static final String[] CAC_KHOA = new String[] { "maKhachHang" };
    private static final Map<String, String> KIEU_COT = Map.ofEntries(Map.entry("maKhachHang", "varchar:5"),
            Map.entry("hoTenKH", "nvarchar:100"), Map.entry("soDienThoai", "varchar:10"),
            Map.entry("gioiTinh", "bit:"), Map.entry("email", "varchar:254"),
            Map.entry("diaChi", "nvarchar:255"));
    private static final Set<String> COT_BAT_BUOC = Set.of("maKhachHang", "hoTenKH", "soDienThoai",
            "gioiTinh", "email");

    private final String table = "KhachHang";
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
        return prefix() + String.format(Locale.ROOT, "%03d", nextAvailableNumber());
    }

    // ===== PHƯƠNG THỨC: prefix() — Xử lý prefix. =====
    private String prefix() {
        return "KH";
    }

    // ===== PHƯƠNG THỨC: nextAvailableNumber() — Tìm số mã kế tiếp chưa được sử dụng. =====
    private int nextAvailableNumber() throws SQLException {
        String sql = ";WITH Numbers(n) AS (SELECT CONVERT(int,1) UNION ALL SELECT n+1 FROM Numbers WHERE n<999) "
                + "SELECT MIN(n) FROM Numbers WHERE NOT EXISTS (SELECT 1 FROM dbo.[" + table + "]"
                + " AS currentRows WHERE currentRows.[" + primary
                + "]=?+RIGHT('000'+CONVERT(varchar(3),Numbers.n),3)) OPTION (MAXRECURSION 1000)";
        try (Connection conn = KetNoiCSDL.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getObject(1) == null)
                    throw new SQLException("Đã hết dải mã cho bảng " + table + ".");
                return rs.getInt(1);
            }
        }
    }

    // ===== PHƯƠNG THỨC: nextAvailableNumber(Connection) — Tìm số mã kế tiếp chưa được sử dụng. =====
    private int nextAvailableNumber(Connection conn) throws SQLException {
        String lockHint = " WITH (TABLOCKX,HOLDLOCK)";
        String sql = ";WITH Numbers(n) AS (SELECT CONVERT(int,1) UNION ALL SELECT n+1 FROM Numbers WHERE n<999) "
                + "SELECT MIN(n) FROM Numbers WHERE NOT EXISTS (SELECT 1 FROM dbo.[" + table
                + "] AS currentRows" + lockHint + " WHERE currentRows.[" + primary
                + "]=?+RIGHT('000'+CONVERT(varchar(3),Numbers.n),3)) OPTION (MAXRECURSION 1000)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getObject(1) == null)
                    throw new SQLException("Đã hết dải mã cho bảng " + table + ".");
                return rs.getInt(1);
            }
        }
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
                row.put(primary, prefix() + String.format(Locale.ROOT, "%03d", nextAvailableNumber(conn)));
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
    public List<LinkedHashMap<String, Object>> getAll() throws SQLException {
        String sql = "SELECT k.*, COALESCE(d.tongDiemTichLuy, CONVERT(bigint,0)) AS tongDiemTichLuy, "
                + "COALESCE(vehicles.bienSoXe, N'Chưa có xe đăng ký') AS bienSoXe "
                + "FROM dbo.KhachHang k LEFT JOIN dbo.vw_KhachHangDiemTichLuy d "
                + "ON d.maKhachHang=k.maKhachHang "
                + "OUTER APPLY (SELECT STUFF((SELECT N', ' + p.bienSo FROM dbo.PhuongTien p "
                + "WHERE p.maKhachHang=k.maKhachHang ORDER BY p.bienSo FOR XML PATH(''), TYPE)"
                + ".value('.', 'nvarchar(max)'),1,2,N'') AS bienSoXe) vehicles " + "ORDER BY k.maKhachHang";
        List<LinkedHashMap<String, Object>> rows = new ArrayList<>();
        try (Connection connection = KetNoiCSDL.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                LinkedHashMap<String, Object> row = new LinkedHashMap<>();
                for (String field : CAC_COT) {
                    row.put(field, result.getObject(field));
                }
                row.put("bienSoXe", result.getString("bienSoXe"));
                row.put("tongDiemTichLuy", result.getLong("tongDiemTichLuy"));
                String customerId = String.valueOf(row.get("maKhachHang"));
                if (!HiddenRecords.isHidden("KhachHang", customerId))
                    rows.add(row);
            }
        }
        return rows;
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

            if (field.equals("maKhachHang") && !raw.matches("KH[0-9]{3}"))
                fail(field, "Mã khách hàng phải có dạng KH001.");

            if ((field.equals("hoTenKH"))) {

                if (!raw.matches("[\\p{L}\\p{M}]+(?:[ ]+[\\p{L}\\p{M}]+)*"))
                    fail(field, "Họ tên chỉ được chứa chữ cái và khoảng trắng.");
            }

            if (field.equals("soDienThoai") && !raw.matches("0[35789][0-9]{8}"))
                fail(field, "Số điện thoại gồm 10 chữ số, đầu số 03, 05, 07, 08 hoặc 09.");
            if (field.equals("email") && !raw.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
                fail(field, "Email không hợp lệ.");

        }

    }

    // ===== PHƯƠNG THỨC: fail(String, String) — Tạo lỗi truy vấn khi thao tác nghiệp vụ không hợp lệ. =====
    private static void fail(String field, String message) throws SQLException {
        throw new LoiDuLieu(field, message);
    }

    // ===== PHƯƠNG THỨC: layKhachHangChoManHinhBanPhuTung() — Lấy khách hàng cho man hinh ban phụ tùng. =====
    public Map<String, String> layKhachHangChoManHinhBanPhuTung() throws SQLException {
        String sql = "SELECT maKhachHang,maKhachHang+N' — '+hoTenKH FROM dbo.KhachHang ORDER BY maKhachHang";
        Map<String, String> choices = new LinkedHashMap<>();
        try (Connection connection = KetNoiCSDL.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                choices.put(result.getString(1), result.getString(2));
            }
        }
        return choices;
    }
}
