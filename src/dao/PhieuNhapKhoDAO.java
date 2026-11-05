package dao;

import java.math.BigDecimal;
import static dao.QuyTrinhDAO.*;

import connectDB.KetNoiCSDL;
import java.sql.*;
import java.util.*;

// ===== DAO: PhieuNhapKhoDAO =====
/** SQL và thao tác dữ liệu riêng của PhieuNhapKho. */
public class PhieuNhapKhoDAO implements DuLieuDAO {
    private static final String KHOA_CHINH = "maPNK";
    private static final String[] CAC_COT = new String[] { "maPNK", "maNCC", "maNhanVien", "ngayNhap",
            "trangThai", "maYCNK" };
    private static final String[] CAC_KHOA = new String[] { "maPNK" };
    private static final Map<String, String> KIEU_COT = Map.ofEntries(Map.entry("maPNK", "varchar:6"),
            Map.entry("maNCC", "varchar:6"), Map.entry("maNhanVien", "varchar:5"),
            Map.entry("ngayNhap", "datetime2:0"), Map.entry("trangThai", "nvarchar:30"),
            Map.entry("maYCNK", "varchar:6"));
    private static final Set<String> COT_BAT_BUOC = Set.of("maPNK", "maNCC", "maNhanVien", "ngayNhap",
            "trangThai");

    private final String table = "PhieuNhapKho";
    private final String primary = KHOA_CHINH;

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
        return "PNK";
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
    // ===== PHƯƠNG THỨC: validateReceiptIntegrity(Connection, Map<String,Object>, List<? extends Map<String,Object>>, String, boolean) — Kiểm tra tính hợp lệ của receipt integrity. =====
    public static void validateReceiptIntegrity(Connection conn, Map<String, Object> header,
            List<? extends Map<String, Object>> details, String id, boolean isConfirming)
            throws SQLException {
        Object ycnId = header.get("maYCNK");
        require(ycnId != null && !String.valueOf(ycnId).isBlank(),
                "Phiếu nhập kho phải gắn với Yêu cầu nhập kho đã được duyệt.");

        Map<String, Object> request = lock(conn, "YeuCauNhapKho", ycnId);
        require("Đã duyệt".equals(request.get("trangThai")), "Yêu cầu nhập kho (" + ycnId
                + ") phải ở trạng thái 'Đã duyệt' (trạng thái hiện tại: " + request.get("trangThai") + ").");

        // Một yêu cầu nhập kho chỉ có duy nhất một phiếu nhập kho
        require(!exists(conn,
                "SELECT 1 FROM dbo.PhieuNhapKho WITH (UPDLOCK,HOLDLOCK) WHERE maYCNK=? AND maPNK<>?", ycnId,
                id == null ? "" : id), "Yêu cầu nhập kho " + ycnId + " đã có phiếu nhập kho khác.");

        // Nhà cung cấp trên phiếu phải trùng với nhà cung cấp được duyệt
        equal(header, request, "maNCC");

        // Chi tiết yêu cầu nhập kho gốc
        List<LinkedHashMap<String, Object>> reqDetails = new dao.ChiTietYeuCauNhapKhoDAO().getAll(conn,
                Map.of("maYCNK", ycnId));
        require(!reqDetails.isEmpty(), "Yêu cầu nhập kho không có chi tiết hạng mục.");

        Map<String, Integer> reqParts = new LinkedHashMap<>();
        for (Map<String, Object> line : reqDetails) {
            reqParts.put(String.valueOf(line.get("maPhuTung")),
                    ((Number) line.get("soLuongYeuCau")).intValue());
        }

        require(details != null && !details.isEmpty(),
                "Phiếu nhập kho phải có ít nhất một hạng mục chi tiết.");
        Set<String> seenParts = new HashSet<>();
        int totalActual = 0;

        for (Map<String, Object> detail : details) {
            String part = String.valueOf(detail.get("maPhuTung"));
            require(!"PT000".equals(part), "Mã phụ tùng không hợp lệ (PT000).");
            require(seenParts.add(part), "Trùng lặp mặt hàng trong phiếu nhập kho: " + part);
            require(reqParts.containsKey(part),
                    "Mặt hàng " + part + " không nằm trong yêu cầu nhập kho đã duyệt.");

            int expectedReqQty = reqParts.get(part);
            require(detail.get("soLuongYeuCau") != null
                    && ((Number) detail.get("soLuongYeuCau")).intValue() == expectedReqQty,
                    "Số lượng yêu cầu của phụ tùng " + part + " (" + detail.get("soLuongYeuCau")
                            + ") không khớp với đơn duyệt (" + expectedReqQty + ").");

            Object rawActual = detail.get("soLuongThucNhan");
            Object rawAccepted = detail.get("soLuongNhap");

            require(rawActual != null, "Phụ tùng " + part
                    + " chưa ghi nhận số lượng thực nhận. Vui lòng kiểm đếm và nhập số lượng.");
            require(rawAccepted != null, "Phụ tùng " + part
                    + " chưa ghi nhận số lượng nhập kho chấp nhận. Vui lòng kiểm tra và nhập số lượng.");

            int actualQty;
            int acceptedQty;
            if (rawActual instanceof Number num)
                actualQty = num.intValue();
            else if (rawActual instanceof String s && !s.trim().isEmpty())
                actualQty = Integer.parseInt(s.trim());
            else
                throw new SQLException("Phụ tùng " + part + " chưa ghi nhận số lượng thực nhận hợp lệ.");

            if (rawAccepted instanceof Number num)
                acceptedQty = num.intValue();
            else if (rawAccepted instanceof String s && !s.trim().isEmpty())
                acceptedQty = Integer.parseInt(s.trim());
            else
                throw new SQLException(
                        "Phụ tùng " + part + " chưa ghi nhận số lượng chấp nhận nhập kho hợp lệ.");

            require(actualQty >= 0, "Số lượng thực nhận không được âm: " + part);
            require(acceptedQty >= 0, "Số lượng chấp nhận không được âm: " + part);
            require(actualQty <= expectedReqQty,
                    "Số lượng thực nhận (" + actualQty + ") không được vượt quá số lượng yêu cầu ("
                            + expectedReqQty + ") cho phụ tùng " + part + ".");
            require(acceptedQty <= actualQty,
                    "Số lượng chấp nhận (" + acceptedQty + ") không được vượt quá số lượng thực nhận ("
                            + actualQty + ") cho phụ tùng " + part + ".");

            totalActual += actualQty;
        }

        require(seenParts.containsAll(reqParts.keySet()) && seenParts.size() == reqParts.size(),
                "Danh sách phụ tùng trong phiếu nhập kho phải khớp 1:1 với yêu cầu nhập kho đã duyệt (không được thêm, bớt hoặc thay đổi mặt hàng).");

        if (isConfirming) {
            require(totalActual > 0,
                    "Tổng số lượng thực nhận của toàn bộ phiếu phải lớn hơn 0 để có thể xác nhận nhập kho.");
        }
    }

    // ===== PHƯƠNG THỨC: prepareReceiptFromRequest(String, gui.VaiTro, String) — Tạo phiếu nhập kho từ yêu cầu đã duyệt. =====
    public static Map<String, Object> prepareReceiptFromRequest(String ycnId, gui.VaiTro role,
            String employeeId) throws SQLException {
        if (role == null || (role != gui.VaiTro.ADMIN && role != gui.VaiTro.KHO)) {
            throw new SecurityException(
                    "Chỉ Nhân viên kho hoặc Quản trị viên mới có quyền lập phiếu nhận hàng thực tế.");
        }
        if (employeeId == null || employeeId.isBlank()) {
            throw new SecurityException("Mã nhân viên nhận hàng không hợp lệ.");
        }
        try (Connection conn = KetNoiCSDL.getConnection()) {
            requireActiveEmployee(conn, employeeId, "nhận hàng");
            Map<String, Object> ycn = lock(conn, "YeuCauNhapKho", ycnId);
            require("Đã duyệt".equals(ycn.get("trangThai")),
                    "Chỉ lập phiếu nhập kho cho yêu cầu đã được duyệt.");
            require(!exists(conn, "SELECT 1 FROM dbo.PhieuNhapKho WHERE maYCNK=?", ycnId),
                    "Yêu cầu nhập kho " + ycnId + " đã có phiếu nhập kho. Không thể tạo thêm phiếu mới.");
            return ycn;
        }
    }

    // ===== PHƯƠNG THỨC: confirmActualStockIn(String, gui.VaiTro, String) — Xác nhận số lượng phụ tùng thực nhập. =====
    public static void confirmActualStockIn(String id, gui.VaiTro role, String employeeId)
            throws SQLException {
        if (role == null || (role != gui.VaiTro.ADMIN && role != gui.VaiTro.KHO)) {
            throw new SecurityException(
                    "Chỉ Nhân viên kho hoặc Quản trị viên mới có quyền xác nhận nhập kho thực tế.");
        }
        if (employeeId == null || employeeId.isBlank()) {
            throw new SecurityException("Mã nhân viên xác nhận không hợp lệ.");
        }
        transaction(conn -> {
            requireActiveEmployee(conn, employeeId, "xác nhận nhập kho");
            Map<String, Object> pnk = lock(conn, "PhieuNhapKho", id);
            require("Chờ xác nhận nhập kho".equals(pnk.get("trangThai")),
                    "Phiếu nhập kho không còn ở trạng thái Chờ xác nhận nhập kho (trạng thái hiện tại: "
                            + pnk.get("trangThai") + ").");
            editable(conn, "PhieuNhapKho", pnk);

            Object ycnId = pnk.get("maYCNK");
            require(ycnId != null && !String.valueOf(ycnId).isBlank(),
                    "Phiếu nhập kho không gắn với Yêu cầu nhập kho hợp lệ (phiếu lịch sử). Không thể xác nhận nhập kho.");

            List<LinkedHashMap<String, Object>> details = new dao.ChiTietPhieuNhapKhoDAO().getAll(conn,
                    Map.of("maPNK", id));
            require(!details.isEmpty(), "Phiếu nhập chưa có chi tiết.");

            // Kiểm tra toàn vẹn chặt chẽ trước khi tăng tồn kho và đóng phiếu
            validateReceiptIntegrity(conn, pnk, details, id, true);

            for (Map<String, Object> row : details) {
                Object part = row.get("maPhuTung");
                int accepted = ((Number) row.get("soLuongNhap")).intValue();
                if (accepted > 0) {
                    if (exists(conn, "SELECT 1 FROM dbo.TonKho WITH (UPDLOCK,HOLDLOCK) WHERE maPhuTung=?",
                            part)) {
                        execute(conn,
                                "UPDATE dbo.TonKho SET soLuongHienTai=soLuongHienTai+? WHERE maPhuTung=?",
                                accepted, part);
                    } else {
                        new dao.TonKhoDAO().insert(conn,
                                Map.of("maPhuTung", part, "soLuongHienTai", accepted, "soLuongToiThieu", 0));
                    }
                    TonKhoDAO.fulfillShortages(conn, String.valueOf(part));
                }
            }

            execute(conn, "UPDATE dbo.PhieuNhapKho SET trangThai=N'Đã nhập kho' WHERE maPNK=?", id);
            execute(conn, "UPDATE dbo.YeuCauNhapKho SET trangThai=N'Đã xử lý' WHERE maYCNK=?", ycnId);
            return null;
        });
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

            if (field.equals("maNCC") && !raw.matches("NCC[0-9]{3}"))
                fail(field, "Mã nhà cung cấp phải có dạng NCC001.");

        }

    }

    // ===== PHƯƠNG THỨC: fail(String, String) — Tạo lỗi truy vấn khi thao tác nghiệp vụ không hợp lệ. =====
    private static void fail(String field, String message) throws SQLException {
        throw new LoiDuLieu(field, message);
    }
}
