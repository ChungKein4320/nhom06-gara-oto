package dao;

import connectDB.KetNoiCSDL;
import gui.VaiTro;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class TaiKhoanDAO implements DuLieuDAO {
    private static final String KHOA_CHINH = "maTaiKhoan";
    private static final String[] CAC_COT = new String[] { "maTaiKhoan", "maNhanVien", "tenDangNhap",
            "matKhau", "trangThai", "ngayTao" };
    private static final String[] CAC_KHOA = new String[] { "maTaiKhoan" };
    private static final Map<String, String> KIEU_COT = Map.ofEntries(Map.entry("maTaiKhoan", "varchar:5"),
            Map.entry("maNhanVien", "varchar:5"), Map.entry("tenDangNhap", "varchar:50"),
            Map.entry("matKhau", "varchar:255"), Map.entry("trangThai", "nvarchar:30"),
            Map.entry("ngayTao", "datetime2:0"));
    private static final Set<String> COT_BAT_BUOC = Set.of("maTaiKhoan", "maNhanVien", "tenDangNhap",
            "matKhau", "trangThai", "ngayTao");

    private final String table = "TaiKhoan";
    private final String primary = KHOA_CHINH;

    // Lấy toàn bộ danh sách tài khoản.
    @Override
    public List<LinkedHashMap<String, Object>> getAll() throws SQLException {
        try (Connection conn = KetNoiCSDL.getConnection()) {
            List<LinkedHashMap<String, Object>> rows = getAll(conn, null);
            rows.removeIf(row -> HiddenRecords.isHidden(table, String.valueOf(row.get(primary))));
            return rows;
        }
    }

    // Lấy danh sách bằng kết nối có sẵn, có locj bản ghi ẩn
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

    // Tạo mã tiếp theo để xem trước khi lưu. 
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
        return "TK";
    }

    // Nhận dữ liệu cần thêm 
    @Override
    public void insert(Map<String, Object> row) throws SQLException {
        insert(row, null);
    }

    // Thêm tài khoản qua phương thức có thêm tham số mã xem trước
    @Override
    public void insert(Map<String, Object> row, String preview) throws SQLException {
        QuyTrinhDAO.transaction(conn -> {
            insert(conn, row);
            return null;
        });
    }

    // Thêm tài khoản bằng kết nối SQL có sẵn
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

    // Sửa tài khoản theo mã
    @Override
    public void update(String id, Map<String, Object> row) throws SQLException {
        if (CAC_KHOA.length != 1)
            throw new SQLException("Cần đầy đủ khóa ghép để sửa chi tiết.");
        QuyTrinhDAO.transaction(conn -> {
            update(conn, Map.of(primary, id), row);
            return null;
        });
    }

    // Sửa tài khoản bằng kết nối có sẵn và bộ khóa
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

    // Đánh dấu bản ghi đã xóa mà vẫn giữ dữ liệu. 
    @Override
    public void softDelete(String id) throws SQLException {
        HiddenRecords.hide(table, id);
    }

    // Tạo điều kiện truy vấn theo tiêu chí lọc.
    private String conditions(Map<String, Object> values) throws SQLException {
        for (String field : values.keySet())
            if (!KIEU_COT.containsKey(field))
                throw new SQLException("Trường không hợp lệ: " + field);
        return String.join(" AND ", values.keySet().stream().map(s -> "[" + s + "]=?").toList());
    }

    // Gán giá trị tham số cho câu lệnh SQL. 
    private static void bind(PreparedStatement ps, Collection<?> values) throws SQLException {
        int i = 1;
        for (Object value : values)
            ps.setObject(i++, value);
    }

    public static class AuthResult {
        public final String maNhanVien;
        public final String hoTen;
        public final VaiTro role;

        // Đối tượng lưu kết quả đăng nhập.
        public AuthResult(String maNhanVien, String hoTen, VaiTro role) {
            this.maNhanVien = maNhanVien;
            this.hoTen = hoTen;
            this.role = role;
        }
    }

    // Xác thực thông tin đăng nhập.
    public AuthResult authenticate(String user, String password) throws SQLException {
        String sql = "SELECT tk.maNhanVien, nv.hoTenNV, nv.vaiTro, tk.matKhau "
                + "FROM dbo.TaiKhoan tk JOIN dbo.NhanVien nv ON nv.maNhanVien = tk.maNhanVien "
                + "WHERE tk.tenDangNhap = ? AND tk.trangThai IN (N'Hoạt động', N'Đang hoạt động') "
                + "AND nv.trangThai = N'Đang làm'";
        try (Connection conn = KetNoiCSDL.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next())
                    return null;
                // SQL của nhóm hiện lưu mật khẩu dạng văn bản thuần.
                // Cần chuyển sang BCrypt/PBKDF2 trước khi sử dụng ngoài môi trường bài tập.
                if (!password.equals(rs.getString("matKhau")))
                    return null;
                String id = rs.getString("maNhanVien");
                String title = rs.getString("vaiTro");
                VaiTro role;
                if (user.equalsIgnoreCase("admin") && id.equals("NV005"))
                    role = VaiTro.ADMIN;
                else if (title.contains("kỹ thuật"))
                    role = VaiTro.KY_THUAT;
                else if (title.contains("kho"))
                    role = VaiTro.KHO;
                else if (title.contains("bán hàng") || title.contains("Thu ngân"))
                    role = VaiTro.THU_NGAN;
                else
                    role = VaiTro.QUAN_LY;
                return new AuthResult(id, rs.getString("hoTenNV"), role);
            }
        }
    }

    // Liệt kê các cột khóa của bản ghi.
    @Override
    public String[] cacKhoa() {
        return CAC_KHOA.clone();
    }

    // Xác định cột khóa chính của bảng dữ liệu.
    @Override
    public String khoaChinh() {
        return KHOA_CHINH;
    }

    // Liệt kê các cột dữ liệu của đối tượng.
    @Override
    public String[] cacCot() {
        return CAC_COT.clone();
    }

    // Khai báo kiểu dữ liệu của từng cột.
    @Override
    public Map<String, String> kieuCot() {
        return KIEU_COT;
    }

    // Liệt kê các cột bắt buộc nhập.
    @Override
    public Set<String> cotBatBuoc() {
        return COT_BAT_BUOC;
    }

    // Kiểm tra dữ liệu.
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

            if (field.equals("maTaiKhoan") && !raw.matches("TK[0-9]{3}"))
                fail(field, "Mã tài khoản phải có dạng TK001.");

            if (field.equals("trangThai")
                    && !java.util.Set.of("Đang hoạt động", "Ngừng hoạt động").contains(raw))
                fail(field, "Trạng thái tài khoản không hợp lệ.");

        }

    }

    // Tạo lỗi truy vấn khi thao tác nghiệp vụ không hợp lệ.
    private static void fail(String field, String message) throws SQLException {
        throw new LoiDuLieu(field, message);
    }
}
