package dao;

import java.math.BigDecimal;
import static dao.QuyTrinhDAO.*;

import connectDB.KetNoiCSDL;
import java.sql.*;
import java.util.*;

/**
 * ============================================================================
 * DAO: YeuCauNhapKhoDAO (Truy vấn và xử lý nghiệp vụ Yêu cầu nhập kho)
 * ============================================================================
 * 1. Mục đích:
 *    - Thực hiện truy vấn SQL và nghiệp vụ dữ liệu cho bảng dbo.YeuCauNhapKho
 *      (đầu phiếu yêu cầu nhập kho phụ tùng, khóa chính maYCNK).
 * 2. Phục vụ màn hình / nghiệp vụ:
 *    - Nghiệp vụ 2 (Lập và phê duyệt yêu cầu nhập kho):
 *      + Nhân viên kho tạo/sửa yêu cầu ở trạng thái "Chờ duyệt" thông qua
 *        FormYeuCauNhapKho -> TrinhSuaYeuCauNhapKho -> QuyTrinhDAO.saveDocument().
 *      + Quản lý hoặc Admin duyệt ("Đã duyệt") / từ chối ("Từ chối") thông qua
 *        approvePurchaseRequest().
 * 3. Phương thức quan trọng:
 *    - insert(Connection, Map): Sinh mã YCNxxx từ Sequence dbo.Seq_YeuCauNhapKho và lưu đầu phiếu.
 *    - approvePurchaseRequest(String, boolean, VaiTro, String): Phê duyệt hoặc từ chối
 *      yêu cầu nhập kho (tuyệt đối chưa làm thay đổi tồn kho ở bước này).
 * ============================================================================
 */
public class YeuCauNhapKhoDAO implements DuLieuDAO {
    // ===== CẤU HÌNH BẢNG VÀ CỘT DỮ LIỆU =====
    private static final String KHOA_CHINH = "maYCNK";
    private static final String[] CAC_COT = new String[] { "maYCNK", "maNCC", "maNhanVien", "ngayTao",
            "trangThai", "maNhanVienDuyet", "ngayDuyet", "ghiChu" };
    private static final String[] CAC_KHOA = new String[] { "maYCNK" };
    private static final Map<String, String> KIEU_COT = Map.ofEntries(Map.entry("maYCNK", "varchar:6"),
            Map.entry("maNCC", "varchar:6"), Map.entry("maNhanVien", "varchar:5"),
            Map.entry("ngayTao", "datetime2:0"), Map.entry("trangThai", "nvarchar:30"),
            Map.entry("maNhanVienDuyet", "varchar:5"), Map.entry("ngayDuyet", "datetime2:0"),
            Map.entry("ghiChu", "nvarchar:255"));
    private static final Set<String> COT_BAT_BUOC = Set.of("maYCNK", "maNCC", "maNhanVien", "ngayTao",
            "trangThai");

    private final String table = "YeuCauNhapKho";
    private final String primary = KHOA_CHINH;

    // ===== NHÓM 1: TRUY VẤN VÀ THAO TÁC CRUD CƠ BẢN (Triển khai DuLieuDAO) =====

    // Lấy toàn bộ danh sách Yêu cầu nhập kho từ CSDL (loại bỏ các bản ghi đã ẩn mềm).
    @Override
    public List<LinkedHashMap<String, Object>> getAll() throws SQLException {
        try (Connection conn = KetNoiCSDL.getConnection()) {
            List<LinkedHashMap<String, Object>> rows = getAll(conn, null);
            rows.removeIf(row -> HiddenRecords.isHidden(table, String.valueOf(row.get(primary))));
            return rows;
        }
    }

    // Truy vấn danh sách Yêu cầu nhập kho theo điều kiện lọc (dùng chung Connection trong transaction).
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

    // Xem trước mã yêu cầu nhập kho tiếp theo từ Sequence dbo.Seq_YeuCauNhapKho (ví dụ: YCN001).
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

    // Lấy tiền tố mã yêu cầu nhập kho ("YCN").
    private String layTienToMa() {
        return "YCN";
    }

    // Thêm mới đầu phiếu yêu cầu nhập kho (mở transaction độc lập).
    @Override
    public void insert(Map<String, Object> row) throws SQLException {
        insert(row, null);
    }

    // Thêm mới đầu phiếu yêu cầu nhập kho trong giao dịch của QuyTrinhDAO.
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
        // BƯỚC 1: Sinh mã maYCNK mới từ Sequence dbo.Seq_YeuCauNhapKho.
        if (CAC_KHOA.length == 1 && !primary.equals("VIN")) {
            {
                try (Statement ps = conn.createStatement();
                        ResultSet rs = ps.executeQuery("SELECT NEXT VALUE FOR dbo.[Seq_" + table + "]")) {
                    rs.next();
                    row.put(primary, layTienToMa() + String.format(Locale.ROOT, "%03d", rs.getInt(1)));
                }
            }
        }
        // BƯỚC 2: Kiểm tra dữ liệu đầu phiếu và quy tắc toàn vẹn trước khi INSERT.
        kiemTraDuLieu(row, true);
        QuyTrinhDAO.guardWrite(conn, table, row, null);
        // BƯỚC 3: Thực thi INSERT INTO dbo.[YeuCauNhapKho] và trả về mã phiếu vừa tạo.
        String fields = String.join(",", row.keySet().stream().map(s -> "[" + s + "]").toList());
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO dbo.[" + table + "](" + fields
                + ") VALUES(" + String.join(",", Collections.nCopies(row.size(), "?")) + ")")) {
            ganThamSoSQL(ps, row.values());
            ps.executeUpdate();
        }
        return String.valueOf(row.get(primary));
    }

    // Cập nhật đầu phiếu yêu cầu nhập kho theo mã maYCNK.
    @Override
    public void update(String id, Map<String, Object> row) throws SQLException {
        if (CAC_KHOA.length != 1)
            throw new SQLException("Cần đầy đủ khóa ghép để sửa chi tiết.");
        QuyTrinhDAO.transaction(conn -> {
            update(conn, Map.of(primary, id), row);
            return null;
        });
    }

    // Cập nhật đầu phiếu yêu cầu nhập kho trong giao dịch đang mở.
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

    // Ẩn mềm bản ghi yêu cầu nhập kho khỏi danh sách hiển thị.
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

    // ===== NHÓM 2: PHƯƠNG THỨC NGHIỆP VỤ PHÊ DUYỆT YÊU CẦU NHẬP KHO =====

    /**
     * NGHIỆP VỤ 2: Phê duyệt hoặc từ chối Yêu cầu nhập kho.
     * - Chỉ Quản lý (QUAN_LY) hoặc Quản trị viên (ADMIN) mới có quyền thực hiện.
     * - Chỉ xử lý khi phiếu đang ở trạng thái "Chờ duyệt" và có ít nhất 1 dòng chi tiết.
     * - Tuyệt đối KHÔNG làm tăng/giảm tồn kho tại bước duyệt yêu cầu.
     */
    public static void approvePurchaseRequest(String id, boolean approve, gui.VaiTro role, String employeeId)
            throws SQLException {
        // BƯỚC 1: Kiểm tra phân quyền — chỉ Quản lý hoặc Quản trị viên mới được duyệt/từ chối.
        if (role == null || (role != gui.VaiTro.ADMIN && role != gui.VaiTro.QUAN_LY)) {
            throw new SecurityException(
                    "Chỉ Quản lý hoặc Quản trị viên mới có quyền duyệt/từ chối yêu cầu nhập kho.");
        }
        if (employeeId == null || employeeId.isBlank()) {
            throw new SecurityException("Mã nhân viên phê duyệt không hợp lệ.");
        }
        // BƯỚC 2: Mở giao dịch SQL để khóa phiếu và cập nhật trạng thái an toàn.
        transaction(conn -> {
            // BƯỚC 2.1: Xác minh nhân viên phê duyệt đang hoạt động trong hệ thống.
            requireActiveEmployee(conn, employeeId, "phê duyệt");
            // BƯỚC 2.2: Khóa dòng YeuCauNhapKho (UPDLOCK, HOLDLOCK) và kiểm tra trạng thái phải là "Chờ duyệt".
            Map<String, Object> row = lock(conn, "YeuCauNhapKho", id);
            require("Chờ duyệt".equals(row.get("trangThai")), "Yêu cầu nhập kho đã xử lý (trạng thái: "
                    + row.get("trangThai") + "); không thể duyệt lại.");
            // BƯỚC 2.3: Kiểm tra phiếu yêu cầu phải có ít nhất một hạng mục chi tiết phụ tùng.
            List<LinkedHashMap<String, Object>> details = new dao.ChiTietYeuCauNhapKhoDAO().getAll(conn,
                    Map.of("maYCNK", id));
            require(!details.isEmpty(), "Yêu cầu nhập kho chưa có chi tiết hạng mục.");
            // BƯỚC 2.4: Cập nhật trạng thái mới ("Đã duyệt" hoặc "Từ chối"), người duyệt và thời điểm duyệt.
            String newState = approve ? "Đã duyệt" : "Từ chối";
            Timestamp now = now(conn);
            execute(conn,
                    "UPDATE dbo.YeuCauNhapKho SET trangThai=?, maNhanVienDuyet=?, ngayDuyet=? WHERE maYCNK=?",
                    newState, employeeId.trim(), now, id);
            return null;
        });
    }

    // ===== NHÓM 3: SIÊU DỮ LIỆU BẢNG VÀ KIỂM TRA DỮ LIỆU ĐẦU VÀO =====

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

    // Kiểm tra các trường bắt buộc, độ dài chuỗi và định dạng mã nhân viên / mã nhà cung cấp.
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

                if (spec[0].equals("decimal")) {
                    String[] size = spec[1].split(",");
                    BigDecimal normalized = n.stripTrailingZeros();
                    if (Math.max(0, normalized.scale()) > Integer.parseInt(size[1].trim()) || Math.max(0,
                            normalized.precision() - normalized.scale()) > Integer.parseInt(size[0].trim())
                                    - Integer.parseInt(size[1].trim()))
                        nemLoiDuLieu(field, "Số tiền vượt độ chính xác cho phép: " + field);
                }
            }

            if (field.equals("maNhanVien") && !raw.matches("NV[0-9]{3}"))
                nemLoiDuLieu(field, "Mã nhân viên phải có dạng NV001.");

            if (field.equals("maNCC") && !raw.matches("NCC[0-9]{3}"))
                nemLoiDuLieu(field, "Mã nhà cung cấp phải có dạng NCC001.");

        }

    }

    // Ném ngoại lệ LoiDuLieu kèm tên trường bị lỗi để tầng GUI hiển thị thông báo.
    private static void nemLoiDuLieu(String field, String message) throws SQLException {
        throw new LoiDuLieu(field, message);
    }
}
