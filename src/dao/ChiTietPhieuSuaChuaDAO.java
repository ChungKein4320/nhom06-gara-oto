package dao;

import java.math.BigDecimal;
import static dao.QuyTrinhDAO.*;

import connectDB.KetNoiCSDL;
import java.sql.*;
import java.util.*;

// ===== DAO: THAO TÁC CHI TIẾT PHIẾU SỬA CHỮA =====

/** SQL và thao tác dữ liệu riêng của ChiTietPhieuSuaChua. */
public class ChiTietPhieuSuaChuaDAO implements DuLieuDAO {
    // ===== CẤU HÌNH VÀ TÊN BẢNG/CỘT =====
    private static final String KHOA_CHINH = "maPSC";
    private static final String[] CAC_COT = new String[] { "maPSC", "maDichVu", "maPhuTung",
            "trangThaiHangMuc", "soLuongPhuTung", "soLuongCapPhat", "soLuongSuDung", "soLuongHoanTra",
            "ngayCapPhat", "maNhanVienCapPhat", "ngayHoanTra", "maNhanVienXacNhanHoanTra", "ketQuaNghiemThu",
            "ngayBatDauBaoHanh", "thoiHanBaoHanh", "ngayHetHanBaoHanh" };
    private static final String[] CAC_KHOA = new String[] { "maPSC", "maDichVu", "maPhuTung" };
    private static final Map<String, String> KIEU_COT = Map.ofEntries(Map.entry("maPSC", "varchar:6"),
            Map.entry("maDichVu", "varchar:5"), Map.entry("maPhuTung", "varchar:5"),
            Map.entry("trangThaiHangMuc", "nvarchar:50"), Map.entry("soLuongPhuTung", "int:"),
            Map.entry("soLuongCapPhat", "int:"), Map.entry("soLuongSuDung", "int:"),
            Map.entry("soLuongHoanTra", "int:"), Map.entry("ngayCapPhat", "datetime2:0"),
            Map.entry("maNhanVienCapPhat", "varchar:5"), Map.entry("ngayHoanTra", "datetime2:0"),
            Map.entry("maNhanVienXacNhanHoanTra", "varchar:5"), Map.entry("ketQuaNghiemThu", "nvarchar:20"),
            Map.entry("ngayBatDauBaoHanh", "datetime2:0"), Map.entry("thoiHanBaoHanh", "int:"),
            Map.entry("ngayHetHanBaoHanh", "datetime2:0"));
    private static final Set<String> COT_BAT_BUOC = Set.of("maPSC", "maDichVu", "maPhuTung",
            "trangThaiHangMuc", "soLuongPhuTung", "ketQuaNghiemThu", "thoiHanBaoHanh");

    private final String table = "ChiTietPhieuSuaChua";
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
        return "";
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
    // ===== PHƯƠNG THỨC: completeRepairItem(String, String, String, int) — Hoàn tất một hạng mục sửa chữa. =====
    public static void completeRepairItem(String id, String service, String part, int months)
            throws SQLException {
        require(months >= 0, "Thời hạn bảo hành không được âm.");

        transaction(conn -> {
            Map<String, Object> repair = lock(conn, "PhieuSuaChua", id);

            require("Đang sửa chữa".equals(repair.get("trangThai")),
                    "Chỉ xác nhận hạng mục của phiếu đang sửa chữa.");

            Map<String, Object> line = one(conn, "SELECT * FROM dbo.ChiTietPhieuSuaChua "
                    + "WITH (UPDLOCK,HOLDLOCK) " + "WHERE maPSC=? AND maDichVu=? AND maPhuTung=?", id,
                    service, part);

            require("Đang sửa chữa".equals(line.get("trangThaiHangMuc")),
                    "Hạng mục chưa cấp phát hoặc đã hoàn thành.");

            int issued = ((Number) line.get("soLuongCapPhat")).intValue();
            int returned = ((Number) line.get("soLuongHoanTra")).intValue();

            require(issued >= 0 && returned >= 0 && returned <= issued,
                    "Số lượng cấp phát hoặc hoàn trả không hợp lệ.");

            boolean noPart = "PT000".equals(part);

            require(!noPart || (issued == 0 && returned == 0),
                    "Hạng mục không dùng phụ tùng phải có số lượng bằng 0.");

            require(noPart || issued > 0, "Hạng mục chưa được ghi nhận cấp phát phụ tùng.");

            int actualUsed = noPart ? 0 : issued - returned;
            int actualMonths = noPart ? 0 : months;

            int updated = execute(conn,
                    "UPDATE dbo.ChiTietPhieuSuaChua " + "SET soLuongSuDung=?, "
                            + "trangThaiHangMuc=N'Hoàn thành', " + "thoiHanBaoHanh=?, "
                            + "ketQuaNghiemThu=N'Không đạt', " + "ngayBatDauBaoHanh=NULL, "
                            + "ngayHetHanBaoHanh=NULL " + "WHERE maPSC=? AND maDichVu=? AND maPhuTung=? "
                            + "AND trangThaiHangMuc=N'Đang sửa chữa'",
                    actualUsed, actualMonths, id, service, part);

            require(updated == 1, "Không thể xác nhận hạng mục.");
            return null;
        });
    }

    // ===== PHƯƠNG THỨC: recordPartReturn(String, String, String, int, String) — Ghi nhận số lượng phụ tùng được hoàn trả. =====
    public static void recordPartReturn(String repairId, String service, String part, int quantity,
            String warehouseEmployee) throws SQLException {
        require(quantity > 0, "Số lượng trả kho phải lớn hơn 0.");
        transaction(conn -> {
            Map<String, Object> repair = lock(conn, "PhieuSuaChua", repairId);
            require(Set.of("Chờ sửa chữa", "Đang sửa chữa").contains(String.valueOf(repair.get("trangThai"))),
                    "Chỉ ghi nhận hoàn trả trước khi nghiệm thu.");
            require(exists(conn,
                    "SELECT 1 FROM dbo.NhanVien WHERE maNhanVien=? AND trangThai=N'Đang làm' AND vaiTro IN (N'Nhân viên kho',N'Nhân viên quản lý',N'Quản trị viên')",
                    warehouseEmployee), "Chỉ nhân viên kho/quản lý đang làm mới được ghi nhận hàng trả.");
            Map<String, Object> line = one(conn,
                    "SELECT * FROM dbo.ChiTietPhieuSuaChua WITH (UPDLOCK,HOLDLOCK) WHERE maPSC=? AND maDichVu=? AND maPhuTung=?",
                    repairId, service, part);
            int issued = ((Number) line.get("soLuongCapPhat")).intValue(),
                    used = ((Number) line.get("soLuongSuDung")).intValue(),
                    returned = ((Number) line.get("soLuongHoanTra")).intValue();
            require(!"PT000".equals(part) && issued > 0 && returned + used + quantity <= issued,
                    "Số lượng trả vượt số phụ tùng đã cấp phát còn lại.");
            require(execute(conn, "UPDATE dbo.TonKho SET soLuongHienTai=soLuongHienTai+? WHERE maPhuTung=?",
                    quantity, part) == 1, "Không tìm thấy tồn kho để ghi nhận trả phụ tùng.");
            require(execute(conn,
                    "UPDATE dbo.ChiTietPhieuSuaChua SET soLuongHoanTra=soLuongHoanTra+?,ngayHoanTra=?,maNhanVienXacNhanHoanTra=? WHERE maPSC=? AND maDichVu=? AND maPhuTung=?",
                    quantity, now(conn), warehouseEmployee, repairId, service, part) == 1,
                    "Không thể cập nhật đối soát phụ tùng trả kho.");
            return null;
        });
    }

    // ===== PHƯƠNG THỨC: refreshRepairShortages() — Làm mới sửa chữa shortages. =====
    public static void refreshRepairShortages() throws SQLException {
        transaction(conn -> {
            List<Map<String, Object>> candidates = new ArrayList<>();

            String sql = "SELECT ct.maPSC, ct.maDichVu, ct.maPhuTung " + "FROM dbo.ChiTietPhieuSuaChua ct "
                    + "JOIN dbo.PhieuSuaChua p ON p.maPSC=ct.maPSC "
                    + "WHERE ct.trangThaiHangMuc=N'Thiếu phụ tùng' " + "AND ct.maPhuTung<>'PT000' "
                    + "AND ct.soLuongCapPhat=0 " + "AND p.trangThai IN (N'Chờ sửa chữa', N'Đang sửa chữa') "
                    + "ORDER BY p.ngayLap, ct.maPSC, ct.maDichVu, ct.maPhuTung";

            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    candidates.add(Map.of("maPSC", rs.getString("maPSC"), "maDichVu",
                            rs.getString("maDichVu"), "maPhuTung", rs.getString("maPhuTung")));
                }
            }

            for (Map<String, Object> candidate : candidates) {
                Object repairId = candidate.get("maPSC");
                Object service = candidate.get("maDichVu");
                Object part = candidate.get("maPhuTung");

                // Khóa phiếu và đọc lại để tránh dùng trạng thái đã cũ.
                Map<String, Object> repair = lock(conn, "PhieuSuaChua", repairId);

                if (!Set.of("Chờ sửa chữa", "Đang sửa chữa")
                        .contains(String.valueOf(repair.get("trangThai")))) {
                    continue;
                }

                Map<String, Object> item = one(conn, "SELECT * FROM dbo.ChiTietPhieuSuaChua "
                        + "WITH (UPDLOCK,HOLDLOCK) " + "WHERE maPSC=? AND maDichVu=? AND maPhuTung=?",
                        repairId, service, part);

                if (!"Thiếu phụ tùng".equals(item.get("trangThaiHangMuc"))
                        || ((Number) item.get("soLuongCapPhat")).intValue() != 0) {
                    continue;
                }

                Object quoteId = repair.get("maBaoGia");

                Map<String, Object> quoteItem = one(conn,
                        "SELECT trangThai, soLuongPhuTung " + "FROM dbo.ChiTietPhieuBaoGia "
                                + "WITH (UPDLOCK,HOLDLOCK) "
                                + "WHERE maBaoGia=? AND maDichVu=? AND maPhuTung=?",
                        quoteId, service, part);

                int quantity = ((Number) item.get("soLuongPhuTung")).intValue();
                int quotedQuantity = ((Number) quoteItem.get("soLuongPhuTung")).intValue();

                require(quantity > 0 && quantity == quotedQuantity,
                        "Số lượng phiếu sửa chữa không khớp báo giá: " + repairId + " / " + service + " / "
                                + part);

                String quoteState = String.valueOf(quoteItem.get("trangThai"));

                if ("Thiếu phụ tùng".equals(quoteState)) {
                    // Kiểm tra đủ tồn và giữ hàng trong cùng một câu lệnh.
                    int reserved = execute(conn, "UPDATE dbo.TonKho " + "SET soLuongHienTai=soLuongHienTai-? "
                            + "WHERE maPhuTung=? AND soLuongHienTai>=?", quantity, part, quantity);

                    if (reserved == 0)
                        continue;

                    execute(conn,
                            "UPDATE dbo.ChiTietPhieuBaoGia " + "SET trangThai=N'Chờ sửa chữa' "
                                    + "WHERE maBaoGia=? AND maDichVu=? AND maPhuTung=?",
                            quoteId, service, part);
                } else if (!"Chờ sửa chữa".equals(quoteState)) {
                    // Không tự suy đoán việc giữ hàng ở trạng thái khác.
                    continue;
                }

                // Báo giá đã Chờ sửa chữa nghĩa là đã giữ hàng:
                // chỉ đồng bộ hạng mục, không trừ tồn thêm lần nữa.
                execute(conn,
                        "UPDATE dbo.ChiTietPhieuSuaChua " + "SET trangThaiHangMuc=N'Chờ sửa chữa' "
                                + "WHERE maPSC=? AND maDichVu=? AND maPhuTung=? "
                                + "AND trangThaiHangMuc=N'Thiếu phụ tùng' " + "AND soLuongCapPhat=0",
                        repairId, service, part);
            }

            return null;
        });
    }

    // ===== PHƯƠNG THỨC: allocate(String, String, String) — Cấp phát phụ tùng cho phiếu sửa chữa. =====
    public static void allocate(String repair, String service, String part) throws SQLException {
        allocate(repair, service, part, null);
    }

    // ===== PHƯƠNG THỨC: allocate(String, String, String, String) — Cấp phát phụ tùng cho phiếu sửa chữa. =====
    public static void allocate(String repair, String service, String part, String employee)
            throws SQLException {
        QuyTrinhDAO.require(!"PT000".equals(part), "PT000 không phải phụ tùng cần cấp phát.");
        QuyTrinhDAO.transaction(conn -> {
            Map<String, Object> header = QuyTrinhDAO.lock(conn, "PhieuSuaChua", repair);
            QuyTrinhDAO.require(
                    Set.of("Chờ sửa chữa", "Đang sửa chữa").contains(String.valueOf(header.get("trangThai"))),
                    "Không thể cấp phát cho phiếu đã hoàn thành/hủy.");
            String actor = employee == null ? String.valueOf(header.get("maNhanVien")) : employee;
            QuyTrinhDAO.require(QuyTrinhDAO.exists(conn,
                    "SELECT 1 FROM dbo.NhanVien WHERE maNhanVien=? AND trangThai=N'Đang làm' AND vaiTro IN (N'Nhân viên kho',N'Nhân viên quản lý',N'Quản trị viên')",
                    actor), "Chỉ nhân viên kho/quản lý đang làm mới được cấp phát phụ tùng.");
            Map<String, Object> line = QuyTrinhDAO.one(conn,
                    "SELECT * FROM dbo.ChiTietPhieuSuaChua WITH (UPDLOCK,HOLDLOCK) WHERE maPSC=? AND maDichVu=? AND maPhuTung=?",
                    repair, service, part);
            QuyTrinhDAO.require("Chờ sửa chữa".equals(line.get("trangThaiHangMuc")),
                    "Hạng mục chưa đủ tồn để cấp phát hoặc đã cấp phát.");
            int quantity = ((Number) line.get("soLuongPhuTung")).intValue();
            QuyTrinhDAO.require(quantity > 0 && ((Number) line.get("soLuongCapPhat")).intValue() == 0,
                    "Số lượng đã cấp phát không hợp lệ.");
            QuyTrinhDAO.execute(conn,
                    "UPDATE dbo.ChiTietPhieuSuaChua SET trangThaiHangMuc=N'Đang sửa chữa',soLuongCapPhat=?,ngayCapPhat=?,maNhanVienCapPhat=? WHERE maPSC=? AND maDichVu=? AND maPhuTung=?",
                    quantity, QuyTrinhDAO.now(conn), actor, repair, service, part);
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

                if (field.equals("soLuongPhuTung")) {
                    boolean noPart = "PT000".equals(String.valueOf(row.get("maPhuTung")));
                    if ((noPart && n.signum() != 0) || (!noPart && n.signum() == 0))
                        fail(field, noPart ? "PT000 phải có số lượng phụ tùng bằng 0."
                                : "Số lượng phụ tùng phải lớn hơn 0.");
                }

                if (spec[0].equals("decimal")) {
                    String[] size = spec[1].split(",");
                    BigDecimal normalized = n.stripTrailingZeros();
                    if (Math.max(0, normalized.scale()) > Integer.parseInt(size[1].trim()) || Math.max(0,
                            normalized.precision() - normalized.scale()) > Integer.parseInt(size[0].trim())
                                    - Integer.parseInt(size[1].trim()))
                        fail(field, "Số tiền vượt độ chính xác cho phép: " + field);
                }
            }

            if (field.equals("maDichVu") && !raw.matches("DV[0-9]{3}"))
                fail(field, "Mã dịch vụ phải có dạng DV001.");

            if (field.equals("maPhuTung") && !raw.matches("PT[0-9]{3}"))
                fail(field, "Mã phụ tùng phải có dạng PT001.");

        }
        dates(row, "ngayBatDauBaoHanh", "ngayHetHanBaoHanh", false);

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
