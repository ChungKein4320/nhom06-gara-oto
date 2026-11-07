package dao;

import connectDB.KetNoiCSDL;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class NhaCungCapDAO implements DuLieuDAO {
    private static final String KHOA_CHINH = "maNCC";
    private static final String[] CAC_COT = new String[] { "maNCC", "tenNCC", "soDienThoai", "email",
            "diaChi" };
    private static final String[] CAC_KHOA = new String[] { "maNCC" };
    private static final Map<String, String> KIEU_COT = Map.ofEntries(Map.entry("maNCC", "varchar:6"),
            Map.entry("tenNCC", "nvarchar:150"), Map.entry("soDienThoai", "varchar:10"),
            Map.entry("email", "varchar:254"), Map.entry("diaChi", "nvarchar:255"));
    private static final Set<String> COT_BAT_BUOC = Set.of("maNCC", "tenNCC", "soDienThoai");

    private final String table = "NhaCungCap";
    private final String primary = KHOA_CHINH;

    // Lấy toàn bộ danh sách để hiển thị, bỏ bản ghi bị ẩn
    @Override
    public List<LinkedHashMap<String, Object>> getAll() throws SQLException {
        try (Connection conn = KetNoiCSDL.getConnection()) {
            List<LinkedHashMap<String, Object>> rows = getAll(conn, null);
            // Loại bỏ các dòng được đánh dấu ẩn
            rows.removeIf(row -> HiddenRecords.isHidden(table, String.valueOf(row.get(primary))));
            return rows;
        }
    }

    // Thực hiện truy vấn SQL, có thể lọc theo điều kiện
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

    // Tạo số mã tiếp theo để xem trước khi lưu.
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

    // Xử lý tiền tố
    private String prefix() {
        return "NCC";
    }

    // Thêm đơn giản
    @Override
    public void insert(Map<String, Object> row) throws SQLException {
        insert(row, null);
    }

    // Tạo transaction rồi gọi phương thức thêm thực tế
    @Override
    public void insert(Map<String, Object> row, String preview) throws SQLException {
        QuyTrinhDAO.transaction(conn -> {
            insert(conn, row);
            return null;
        });
    }

    // Sinh mã, kiểm tra và thực hiện SQL INSERT
    @Override
    public String insert(Connection conn, Map<String, Object> values) throws SQLException {
        // Sao chép value thành row để bổ sung mã
    	Map<String, Object> row = new LinkedHashMap<>(values);
        if (CAC_KHOA.length == 1 && !primary.equals("VIN")) {
            {
                try (Statement ps = conn.createStatement();
                		// lấy số tiếp theo
                        ResultSet rs = ps.executeQuery("SELECT NEXT VALUE FOR dbo.[Seq_" + table + "]")) {
                    rs.next();
                    //ghép mã với 3 số
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

    // Nhận mã cần sửa, mở transaction
    @Override
    public void update(String id, Map<String, Object> row) throws SQLException {
        if (CAC_KHOA.length != 1)
            throw new SQLException("Cần đầy đủ khóa ghép để sửa chi tiết.");
        QuyTrinhDAO.transaction(conn -> {
            update(conn, Map.of(primary, id), row);
            return null;
        });
    }

    // Kiểm tra và thực hiện SQL UPDATE 
    @Override
    public void update(Connection conn, Map<String, Object> key, Map<String, Object> values)
            throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>(values);
        for (String field : CAC_KHOA)
        	//Không thay đổi khóa chính
            row.remove(field);
        kiemTraDuLieu(row, false);
        // Kiểm tra ràng buộc
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

    // Đánh dấu bản ghi đã xóa mà vẫn giữ dữ liệu. 
    @Override
    public void softDelete(String id) throws SQLException {
        HiddenRecords.hide(table, id);
    }

    // Tạo điều kiện truy vấn theo tiêu chí lọc. (tạo ở chỗ WHERE)
    private String conditions(Map<String, Object> values) throws SQLException {
        for (String field : values.keySet())
            if (!KIEU_COT.containsKey(field))
                throw new SQLException("Trường không hợp lệ: " + field);
        return String.join(" AND ", values.keySet().stream().map(s -> "[" + s + "]=?").toList());
    }

    // Gán giá trị tham số vào dấu ? cho câu SQL.
    private static void bind(PreparedStatement ps, Collection<?> values) throws SQLException {
        int i = 1;
        for (Object value : values)
            ps.setObject(i++, value);
    }

    @Override
    public String[] cacKhoa() {
        return CAC_KHOA.clone();
    }

    @Override
    public String khoaChinh() {
        return KHOA_CHINH;
    }

    @Override
    public String[] cacCot() {
        return CAC_COT.clone();
    }

    @Override
    public Map<String, String> kieuCot() {
        return KIEU_COT;
    }

    @Override
    public Set<String> cotBatBuoc() {
        return COT_BAT_BUOC;
    }

    // Kiểm tra dữ liệu. 
    private static void kiemTraDuLieu(Map<String, Object> row, boolean creating) throws SQLException {
        // true = insert, false = update
    	if (creating) {
            Set<String> required = COT_BAT_BUOC;
            for (String field : required) {
                Object val = row.get(field);
                if (val == null || (val instanceof String s && s.trim().isEmpty()))
                    fail(field, "Bắt buộc nhập: " + field);
            }
        }
    	//Kiểm tra từng giá trị
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

            if (field.equals("maNCC") && !raw.matches("NCC[0-9]{3}"))
                fail(field, "Mã nhà cung cấp phải có dạng NCC001.");

            if (field.equals("tenNCC") && !raw.matches("(?s).*[\\p{L}].*"))
                fail(field, "Tên nhà cung cấp phải chứa ít nhất một chữ cái.");

            if (field.equals("soDienThoai") && !raw.matches("0[35789][0-9]{8}"))
                fail(field, "Số điện thoại gồm 10 chữ số, đầu số 03, 05, 07, 08 hoặc 09.");
            if (field.equals("email") && !raw.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
                fail(field, "Email không hợp lệ.");

        }

    }

    // Tạo lỗi truy vấn khi thao tác nghiệp vụ không hợp lệ. 
    private static void fail(String field, String message) throws SQLException {
        throw new LoiDuLieu(field, message);
    }
}
