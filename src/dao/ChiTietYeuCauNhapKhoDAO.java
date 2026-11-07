package dao;

import java.math.BigDecimal;

import connectDB.KetNoiCSDL;
import java.sql.*;
import java.util.*;

/**
 * ============================================================================
 * DAO: ChiTietYeuCauNhapKhoDAO (Truy vấn và lưu chi tiết Yêu cầu nhập kho)
 * ============================================================================
 * 1. Mục đích:
 *    - Thực hiện truy vấn SQL và thao tác lưu/cập nhật cho bảng
 *      dbo.ChiTietYeuCauNhapKho (khóa ghép: maYCNK + maPhuTung).
 * 2. Phục vụ màn hình / nghiệp vụ:
 *    - Nghiệp vụ 2 (Lập và duyệt yêu cầu nhập kho): Lưu từng dòng phụ tùng đề xuất
 *      mua cùng giao dịch với đầu phiếu YeuCauNhapKho.
 *    - Nghiệp vụ 3 (Lập phiếu nhập kho từ yêu cầu đã duyệt): Đọc danh sách phụ tùng
 *      và số lượng yêu cầu gốc để khởi tạo và đối chiếu với Phiếu nhập kho.
 * 3. Phương thức quan trọng:
 *    - getAll(Connection, Map): Lấy danh sách các dòng phụ tùng thuộc một mã maYCNK.
 *    - insert(Connection, Map): Lưu từng dòng chi tiết trong cùng transaction với đầu phiếu.
 *    - kiemTraDuLieu(Map, boolean): Kiểm tra soLuongYeuCau > 0, donGiaDuKien >= 0,
 *      mã phụ tùng dạng PTxxx và cấm mã ảo PT000.
 * ============================================================================
 */
public class ChiTietYeuCauNhapKhoDAO implements DuLieuDAO {
    // ===== CẤU HÌNH BẢNG VÀ CỘT DỮ LIỆU =====
    private static final String KHOA_CHINH = "maYCNK";
    private static final String[] CAC_COT = new String[] { "maYCNK", "maPhuTung", "soLuongYeuCau",
            "donGiaDuKien" };
    private static final String[] CAC_KHOA = new String[] { "maYCNK", "maPhuTung" };
    private static final Map<String, String> KIEU_COT = Map.ofEntries(Map.entry("maYCNK", "varchar:6"),
            Map.entry("maPhuTung", "varchar:5"), Map.entry("soLuongYeuCau", "int:"),
            Map.entry("donGiaDuKien", "decimal:18,2"));
    private static final Set<String> COT_BAT_BUOC = Set.of("maYCNK", "maPhuTung", "soLuongYeuCau",
            "donGiaDuKien");

    private final String table = "ChiTietYeuCauNhapKho";
    private final String primary = KHOA_CHINH;

    // ===== NHÓM 1: TRUY VẤN VÀ THAO TÁC CRUD CƠ BẢN (Triển khai DuLieuDAO) =====

    // Lấy toàn bộ các dòng chi tiết yêu cầu nhập kho từ CSDL.
    @Override
    public List<LinkedHashMap<String, Object>> getAll() throws SQLException {
        try (Connection conn = KetNoiCSDL.getConnection()) {
            List<LinkedHashMap<String, Object>> rows = getAll(conn, null);
            rows.removeIf(row -> HiddenRecords.isHidden(table, String.valueOf(row.get(primary))));
            return rows;
        }
    }

    // Truy vấn các dòng chi tiết yêu cầu nhập kho theo điều kiện lọc (thường lọc theo maYCNK).
    @Override
    public List<LinkedHashMap<String, Object>> getAll(Connection conn, Map<String, Object> filter)
            throws SQLException {
        String where = filter == null || filter.isEmpty() ? "" : " WHERE " + taoDieuKienWhere(filter);
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM dbo.[" + table + "]" + where + " ORDER BY [" + primary + "]")) {
            if (filter != null)
                ganThamSoSQL(ps, filter.values());
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

    // Bảng chi tiết dùng khóa ghép (maYCNK, maPhuTung) nên không tự sinh mã đơn lẻ.
    @Override
    public String previewNextId() throws SQLException {
        if (CAC_KHOA.length != 1 || primary.equals("VIN"))
            return "";

        try (Connection conn = KetNoiCSDL.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT CASE WHEN current_value IS NULL THEN CONVERT(int,start_value) ELSE CONVERT(int,current_value)+1 END FROM sys.sequences WHERE name=? AND schema_id=SCHEMA_ID('dbo')")) {
            ps.setString(1, "Seq_" + table);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? layTienToMa() + String.format(Locale.ROOT, "%03d", rs.getInt(1)) : "";
            }
        }
    }

    // Lấy tiền tố mã (trống vì bảng chi tiết dùng khóa ghép).
    private String layTienToMa() {
        return "";
    }

    // Thêm một dòng chi tiết yêu cầu nhập kho (mở transaction độc lập).
    @Override
    public void insert(Map<String, Object> row) throws SQLException {
        insert(row, null);
    }

    // Thêm một dòng chi tiết yêu cầu nhập kho trong giao dịch của QuyTrinhDAO.
    @Override
    public void insert(Map<String, Object> row, String preview) throws SQLException {
        QuyTrinhDAO.transaction(conn -> {
            insert(conn, row);
            return null;
        });
    }

    /** Dùng connection của phiếu để lưu đầu phiếu và chi tiết cùng giao dịch. */
    @Override
    public String insert(Connection conn, Map<String, Object> values) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>(values);
        if (CAC_KHOA.length == 1 && !primary.equals("VIN")) {
            {
                try (Statement ps = conn.createStatement();
                        ResultSet rs = ps.executeQuery("SELECT NEXT VALUE FOR dbo.[Seq_" + table + "]")) {
                    rs.next();
                    row.put(primary, layTienToMa() + String.format(Locale.ROOT, "%03d", rs.getInt(1)));
                }
            }
        }
        // BƯỚC 1: Kiểm tra tính hợp lệ của dòng chi tiết (soLuongYeuCau > 0, donGiaDuKien >= 0, maPhuTung != PT000).
        kiemTraDuLieu(row, true);
        QuyTrinhDAO.guardWrite(conn, table, row, null);
        // BƯỚC 2: Thực thi INSERT INTO dbo.[ChiTietYeuCauNhapKho].
        String fields = String.join(",", row.keySet().stream().map(s -> "[" + s + "]").toList());
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO dbo.[" + table + "](" + fields
                + ") VALUES(" + String.join(",", Collections.nCopies(row.size(), "?")) + ")")) {
            ganThamSoSQL(ps, row.values());
            ps.executeUpdate();
        }
        return String.valueOf(row.get(primary));
    }

    // Bảng chi tiết dùng khóa ghép nên không hỗ trợ update theo 1 mã đơn.
    @Override
    public void update(String id, Map<String, Object> row) throws SQLException {
        if (CAC_KHOA.length != 1)
            throw new SQLException("Cần đầy đủ khóa ghép để sửa chi tiết.");
        QuyTrinhDAO.transaction(conn -> {
            update(conn, Map.of(primary, id), row);
            return null;
        });
    }

    // Cập nhật dòng chi tiết yêu cầu nhập kho theo khóa ghép (maYCNK, maPhuTung).
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
                .prepareStatement("UPDATE dbo.[" + table + "] SET " + fields + " WHERE " + taoDieuKienWhere(key))) {
            List<Object> params = new ArrayList<>(row.values());
            params.addAll(key.values());
            ganThamSoSQL(ps, params);
            if (ps.executeUpdate() != 1)
                throw new SQLException("Bản ghi đã thay đổi hoặc không còn tồn tại.");
        }
    }

    // Ẩn mềm bản ghi khỏi danh sách hiển thị.
    @Override
    public void softDelete(String id) throws SQLException {
        HiddenRecords.hide(table, id);
    }

    // Tạo biểu thức điều kiện WHERE từ Map tiêu chí lọc.
    private String taoDieuKienWhere(Map<String, Object> values) throws SQLException {
        for (String field : values.keySet())
            if (!KIEU_COT.containsKey(field))
                throw new SQLException("Trường không hợp lệ: " + field);
        return String.join(" AND ", values.keySet().stream().map(s -> "[" + s + "]=?").toList());
    }

    // Gán tuần tự các giá trị tham số vào dấu hỏi (?) của PreparedStatement.
    private static void ganThamSoSQL(PreparedStatement ps, Collection<?> values) throws SQLException {
        int i = 1;
        for (Object value : values)
            ps.setObject(i++, value);
    }

    // ===== NHÓM 2: SIÊU DỮ LIỆU BẢNG VÀ KIỂM TRA DỮ LIỆU ĐẦU VÀO =====

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

    // Kiểm tra dữ liệu chi tiết yêu cầu nhập kho (số lượng yêu cầu > 0, đơn giá dự kiến >= 0, cấm PT000).
    private static void kiemTraDuLieu(Map<String, Object> row, boolean creating) throws SQLException {
        if (creating) {
            Set<String> required = COT_BAT_BUOC;
            for (String field : required) {
                Object val = row.get(field);
                if (val == null || (val instanceof String s && s.trim().isEmpty()))
                    nemLoiDuLieu(field, "Bắt buộc nhập: " + field);
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
                nemLoiDuLieu(field, "Vượt quá độ dài cho phép: " + field);
            if (spec[0].equals("bit") && !(value instanceof Boolean))
                nemLoiDuLieu(field, "Giới tính phải là Nam hoặc Nữ.");
            if (value instanceof Number number) {
                BigDecimal n = new BigDecimal(number.toString());
                if (n.signum() < 0)
                    nemLoiDuLieu(field, "Giá trị không được âm: " + field);
                if (field.equals("soLuongYeuCau") && n.signum() == 0)
                    nemLoiDuLieu(field, "Số lượng yêu cầu phải lớn hơn 0.");

                if (spec[0].equals("decimal")) {
                    String[] size = spec[1].split(",");
                    BigDecimal normalized = n.stripTrailingZeros();
                    if (Math.max(0, normalized.scale()) > Integer.parseInt(size[1].trim()) || Math.max(0,
                            normalized.precision() - normalized.scale()) > Integer.parseInt(size[0].trim())
                                    - Integer.parseInt(size[1].trim()))
                        nemLoiDuLieu(field, "Số tiền vượt độ chính xác cho phép: " + field);
                }
            }

            if (field.equals("maPhuTung") && !raw.matches("PT[0-9]{3}"))
                nemLoiDuLieu(field, "Mã phụ tùng phải có dạng PT001.");

        }

        if ("PT000".equals(String.valueOf(row.get("maPhuTung")))) {

            nemLoiDuLieu("maPhuTung",
                    "PT000 chỉ dùng để biểu thị hạng mục không sử dụng phụ tùng; không được nhập kho hoặc bán như phụ tùng.");

        }
    }

    // Ném ngoại lệ LoiDuLieu kèm tên trường bị lỗi để tầng GUI hiển thị thông báo.
    private static void nemLoiDuLieu(String field, String message) throws SQLException {
        throw new LoiDuLieu(field, message);
    }
}
