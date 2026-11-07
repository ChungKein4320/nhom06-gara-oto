package dao;

import java.math.BigDecimal;
import static dao.QuyTrinhDAO.*;

import connectDB.KetNoiCSDL;
import java.sql.*;
import java.util.*;

/**
 * ============================================================================
 * DAO: TonKhoDAO (Truy vấn và cập nhật dữ liệu Tồn kho phụ tùng)
 * ============================================================================
 * 1. Mục đích:
 *    - Thực hiện truy vấn SQL và nghiệp vụ dữ liệu cho bảng dbo.TonKho
 *      (kết nối LEFT JOIN với bảng dbo.PhuTung để lấy tên phụ tùng hiển thị).
 * 2. Phục vụ màn hình / nghiệp vụ:
 *    - Nghiệp vụ 1 (Quản lý tồn kho & mức tồn tối thiểu): Được gọi bởi FormTonKho
 *      và HopThoaiCapNhatTonToiThieu.
 *    - Nghiệp vụ 3 (Xác nhận nhập kho thực tế): Được gọi bởi PhieuNhapKhoDAO
 *      để cộng tồn kho và tự động bù hàng thiếu (fulfillShortages).
 * 3. Phương thức quan trọng:
 *    - getAll(): Truy vấn danh sách tồn kho kèm tên phụ tùng (JOIN dbo.PhuTung).
 *    - updateMinimumThreshold(String, int, VaiTro): Cập nhật riêng cột soLuongToiThieu (WHERE maTonKho = ?).
 *    - fulfillShortages(Connection, String): Tự động trừ giữ hàng (khi đủ soLuongPhuTung của dòng) và chuyển
 *      ChiTietPhieuBaoGia.trangThai, ChiTietPhieuSuaChua.trangThaiHangMuc từ "Thiếu phụ tùng" sang "Chờ sửa chữa".
 * ============================================================================
 */
public class TonKhoDAO implements DuLieuDAO {
    // ===== CẤU HÌNH BẢNG VÀ CỘT DỮ LIỆU =====
    private static final String KHOA_CHINH = "maTonKho";
    private static final String[] CAC_COT = new String[] { "maTonKho", "maPhuTung", "soLuongHienTai",
            "soLuongToiThieu" };
    private static final String[] CAC_KHOA = new String[] { "maTonKho" };
    private static final Map<String, String> KIEU_COT = Map.ofEntries(Map.entry("maTonKho", "varchar:6"),
            Map.entry("maPhuTung", "varchar:5"), Map.entry("soLuongHienTai", "int:"),
            Map.entry("soLuongToiThieu", "int:"));
    private static final Set<String> COT_BAT_BUOC = Set.of("maTonKho", "maPhuTung", "soLuongHienTai",
            "soLuongToiThieu");

    private final String table = "TonKho";
    private final String primary = KHOA_CHINH;

    // ===== NHÓM 1: TRUY VẤN VÀ THAO TÁC CRUD CƠ BẢN (Triển khai DuLieuDAO) =====

    // Truy vấn danh sách bản ghi TonKho theo điều kiện lọc (dùng chung Connection trong transaction).
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

    // Xem trước mã tồn kho tiếp theo từ Sequence dbo.Seq_TonKho (ví dụ: TKH001) trước khi lưu.
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

    // Lấy tiền tố mã bản ghi tồn kho ("TKH").
    private String layTienToMa() {
        return "TKH";
    }

    // Thêm mới một bản ghi tồn kho (mở transaction độc lập).
    @Override
    public void insert(Map<String, Object> row) throws SQLException {
        insert(row, null);
    }

    // Thêm mới một bản ghi tồn kho trong giao dịch của QuyTrinhDAO.
    @Override
    public void insert(Map<String, Object> row, String preview) throws SQLException {
        QuyTrinhDAO.transaction(conn -> {
            insert(conn, row);
            return null;
        });
    }

    /**
     * Thêm bản ghi tồn kho mới bằng Connection của giao dịch đang mở
     * (dùng khi nhập kho thực tế gặp phụ tùng chưa có dòng tồn kho).
     */
    @Override
    public String insert(Connection conn, Map<String, Object> values) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>(values);
        // BƯỚC 1: Sinh mã maTonKho mới từ Sequence dbo.Seq_TonKho.
        if (CAC_KHOA.length == 1 && !primary.equals("VIN")) {
            {
                try (Statement ps = conn.createStatement();
                        ResultSet rs = ps.executeQuery("SELECT NEXT VALUE FOR dbo.[Seq_" + table + "]")) {
                    rs.next();
                    row.put(primary, layTienToMa() + String.format(Locale.ROOT, "%03d", rs.getInt(1)));
                }
            }
        }
        // BƯỚC 2: Kiểm tra định dạng dữ liệu và ràng buộc nghiệp vụ trước khi INSERT.
        kiemTraDuLieu(row, true);
        QuyTrinhDAO.guardWrite(conn, table, row, null);
        // BƯỚC 3: Thực thi câu lệnh INSERT INTO dbo.[TonKho].
        String fields = String.join(",", row.keySet().stream().map(s -> "[" + s + "]").toList());
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO dbo.[" + table + "](" + fields
                + ") VALUES(" + String.join(",", Collections.nCopies(row.size(), "?")) + ")")) {
            ganThamSoSQL(ps, row.values());
            ps.executeUpdate();
        }
        return String.valueOf(row.get(primary));
    }

    // Cập nhật bản ghi tồn kho theo mã maTonKho (mở transaction độc lập).
    @Override
    public void update(String id, Map<String, Object> row) throws SQLException {
        if (CAC_KHOA.length != 1)
            throw new SQLException("Cần đầy đủ khóa ghép để sửa chi tiết.");
        QuyTrinhDAO.transaction(conn -> {
            update(conn, Map.of(primary, id), row);
            return null;
        });
    }

    // Cập nhật bản ghi tồn kho trong giao dịch đang mở.
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

    // Ẩn bản ghi khỏi danh sách hiển thị (xóa mềm thông qua HiddenRecords).
    @Override
    public void softDelete(String id) throws SQLException {
        HiddenRecords.hide(table, id);
    }

    // Tạo biểu thức điều kiện WHERE dạng "[cot1]=? AND [cot2]=?" từ Map tiêu chí lọc.
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

    /**
     * Lấy toàn bộ danh sách tồn kho hiển thị lên màn hình FormTonKho.
     * Kết nối LEFT JOIN với bảng dbo.PhuTung để lấy thêm cột tenPhuTung.
     */
    @Override
    public java.util.List<java.util.LinkedHashMap<String, Object>> getAll() throws java.sql.SQLException {
        var rows = new java.util.ArrayList<java.util.LinkedHashMap<String, Object>>();
        // BƯỚC 1: Truy vấn bảng TonKho kết hợp bảng PhuTung để lấy tên phụ tùng hiển thị.
        String sql = "SELECT t.*,p.tenPhuTung FROM dbo.TonKho t LEFT JOIN dbo.PhuTung p ON p.maPhuTung=t.maPhuTung ORDER BY t.maTonKho";
        try (var conn = connectDB.KetNoiCSDL.getConnection();
                var ps = conn.prepareStatement(sql);
                var rs = ps.executeQuery()) {
            // BƯỚC 2: Đọc từng dòng kết quả, bỏ qua các bản ghi đã bị ẩn mềm và đưa vào danh sách Map.
            while (rs.next()) {
                if (HiddenRecords.isHidden("TonKho", rs.getString("maTonKho")))
                    continue;
                var row = new java.util.LinkedHashMap<String, Object>();
                for (String field : CAC_COT)
                    row.put(field, rs.getObject(field));
                row.put("tenPhuTung", rs.getString("tenPhuTung"));
                rows.add(row);
            }
        }
        return rows;
    }

    // ===== NHÓM 2: CÁC PHƯƠNG THỨC NGHIỆP VỤ KHO QUAN TRỌNG =====

    /**
     * NGHIỆP VỤ 1: Cập nhật số lượng tồn kho tối thiểu (định mức cảnh báo) cho một phụ tùng.
     * - Chỉ cho phép NHÂN VIÊN KHO hoặc ADMIN thực hiện.
     * - Tuyệt đối KHÔNG thay đổi soLuongHienTai, maPhuTung hay maTonKho.
     *
     * @param maTonKho mã bản ghi tồn kho (bắt buộc)
     * @param soLuongToiThieu mức tồn tối thiểu mới (>= 0)
     * @param role vai trò người thực hiện (KHO hoặc ADMIN)
     * @throws java.sql.SQLException nếu lỗi cơ sở dữ liệu
     * @throws SecurityException nếu vai trò không được phép
     * @throws IllegalArgumentException nếu tham số không hợp lệ
     */
    public void updateMinimumThreshold(String maTonKho, int soLuongToiThieu, gui.VaiTro role)
            throws java.sql.SQLException {
        // BƯỚC 1: Kiểm tra phân quyền — chỉ Nhân viên kho hoặc Quản trị viên mới được phép sửa định mức.
        if (role == null || (role != gui.VaiTro.ADMIN && role != gui.VaiTro.KHO)) {
            throw new SecurityException(
                    "Chỉ Nhân viên kho hoặc Quản trị viên mới có quyền cập nhật mức tồn kho tối thiểu.");
        }
        // BƯỚC 2: Kiểm tra tính hợp lệ của mã tồn kho và giá trị định mức tối thiểu (>= 0).
        if (maTonKho == null || maTonKho.isBlank()) {
            throw new IllegalArgumentException("Mã tồn kho không được để trống.");
        }
        if (soLuongToiThieu < 0) {
            throw new IllegalArgumentException("Mức tồn tối thiểu không được nhỏ hơn 0.");
        }

        // BƯỚC 3: Thực thi câu lệnh UPDATE chỉ cập nhật duy nhất cột soLuongToiThieu trên bảng dbo.TonKho.
        String sql = "UPDATE dbo.TonKho SET soLuongToiThieu = ? WHERE maTonKho = ?";
        try (var conn = connectDB.KetNoiCSDL.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soLuongToiThieu);
            ps.setString(2, maTonKho);
            int updated = ps.executeUpdate();
            // BƯỚC 4: Xác nhận bản ghi tồn tại trong CSDL (nếu không có dòng nào được cập nhật thì báo lỗi).
            if (updated == 0) {
                throw new java.sql.SQLException(
                        "Không tìm thấy bản ghi tồn kho " + maTonKho + " để cập nhật.");
            }
        }
    }

    /**
     * NGHIỆP VỤ 3 (Tự động bù hàng thiếu sau khi nhập kho):
     * Khi phụ tùng vừa được xác nhận nhập kho và tăng tồn kho, hệ thống tự động tìm các
     * hạng mục Báo giá đã xác nhận đang bị "Thiếu phụ tùng" theo thứ tự ưu tiên thời gian
     * xác nhận (FIFO) để giữ hàng (chỉ bù đủ toàn bộ dòng khi soLuongHienTai >= soLuongPhuTung) và chuyển ChiTietPhieuBaoGia.trangThai cùng ChiTietPhieuSuaChua.trangThaiHangMuc sang "Chờ sửa chữa".
     */
    static void fulfillShortages(Connection conn, String part) throws SQLException {
        // BƯỚC 1: Tìm danh sách các dòng ChiTietPhieuBaoGia của phụ tùng này đang "Thiếu phụ tùng"
        // thuộc các PhieuBaoGia "Đã xác nhận", sắp xếp theo thời gian xác nhận báo giá tăng dần (FIFO).
        List<Map<String, Object>> shortages = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT d.maBaoGia,d.maDichVu,d.soLuongPhuTung FROM dbo.ChiTietPhieuBaoGia d WITH (UPDLOCK,ROWLOCK) JOIN dbo.PhieuBaoGia h ON h.maBaoGia=d.maBaoGia WHERE d.maPhuTung=? AND d.trangThai=N'Thiếu phụ tùng' AND h.trangThai=N'Đã xác nhận' ORDER BY h.thoiGianXacNhan,d.maBaoGia")) {
            ps.setString(1, part);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    shortages.add(Map.of("maBaoGia", rs.getString(1), "maDichVu", rs.getString(2), "soLuong",
                            rs.getInt(3)));
            }
        }
        // BƯỚC 2: Duyệt từng hạng mục thiếu hàng, nếu tồn kho hiện tại đủ số lượng cần thì trừ giữ chỗ ngay.
        for (Map<String, Object> row : shortages) {
            int qty = ((Number) row.get("soLuong")).intValue();
            // BƯỚC 2.1: Trừ tồn kho giữ hàng nếu soLuongHienTai >= số lượng cần của hạng mục.
            int updated = execute(conn,
                    "UPDATE dbo.TonKho SET soLuongHienTai=soLuongHienTai-? WHERE maPhuTung=? AND soLuongHienTai>=?",
                    qty, part, qty);
            if (updated == 0)
                continue;
            String quote = String.valueOf(row.get("maBaoGia"));
            // BƯỚC 2.2: Chuyển trạng thái dòng ChiTietPhieuBaoGia từ "Thiếu phụ tùng" sang "Chờ sửa chữa".
            execute(conn,
                    "UPDATE dbo.ChiTietPhieuBaoGia SET trangThai=N'Chờ sửa chữa' WHERE maBaoGia=? AND maDichVu=? AND maPhuTung=?",
                    quote, row.get("maDichVu"), part);
            // BƯỚC 2.3: Đồng bộ trạng thái dòng ChiTietPhieuSuaChua tương ứng sang "Chờ sửa chữa" để kho có thể cấp phát.
            execute(conn,
                    "UPDATE s SET trangThaiHangMuc=N'Chờ sửa chữa' FROM dbo.ChiTietPhieuSuaChua s JOIN dbo.PhieuSuaChua h ON h.maPSC=s.maPSC WHERE h.maBaoGia=? AND s.maDichVu=? AND s.maPhuTung=? AND s.trangThaiHangMuc=N'Thiếu phụ tùng'",
                    quote, row.get("maDichVu"), part);
        }
    }

    // ===== NHÓM 3: SIÊU DỮ LIỆU BẢNG VÀ KIỂM TRA DỮ LIỆU ĐẦU VÀO =====

    // Liệt kê các cột khóa của bảng TonKho.
    @Override
    public String[] cacKhoa() {
        return CAC_KHOA.clone();
    }

    // Xác định cột khóa chính của bảng TonKho ("maTonKho").
    @Override
    public String khoaChinh() {
        return KHOA_CHINH;
    }

    // Liệt kê danh sách cột lưu trong bảng dbo.TonKho.
    @Override
    public String[] cacCot() {
        return CAC_COT.clone();
    }

    // Khai báo kiểu dữ liệu SQL của từng cột trong bảng TonKho.
    @Override
    public Map<String, String> kieuCot() {
        return KIEU_COT;
    }

    // Liệt kê các cột bắt buộc phải có giá trị khi thêm mới bản ghi TonKho.
    @Override
    public Set<String> cotBatBuoc() {
        return COT_BAT_BUOC;
    }

    // Kiểm tra các ràng buộc dữ liệu đầu vào (bắt buộc nhập, độ dài, số không âm, mã phụ tùng hợp lệ).
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

            if (field.equals("maPhuTung") && !raw.matches("PT[0-9]{3}"))
                nemLoiDuLieu(field, "Mã phụ tùng phải có dạng PT001.");

        }

        if ("PT000".equals(String.valueOf(row.get("maPhuTung")))) {

            nemLoiDuLieu("maPhuTung",
                    "PT000 chỉ dùng để biểu thị hạng mục không sử dụng phụ tùng; không được nhập kho hoặc bán như phụ tùng.");

        }
    }

    // Ném ngoại lệ LoiDuLieu kèm tên trường bị lỗi để tầng GUI hiển thị thông báo rõ ràng.
    private static void nemLoiDuLieu(String field, String message) throws SQLException {
        throw new LoiDuLieu(field, message);
    }
}
