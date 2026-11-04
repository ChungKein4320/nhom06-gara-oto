package dao;

import java.math.BigDecimal;
import static dao.QuyTrinhDAO.*;

import connectDB.KetNoiCSDL;
import java.sql.*;
import java.util.*;

// ===== DAO: THAO TÁC PHIẾU SỬA CHỮA =====

/** SQL và thao tác dữ liệu riêng của PhieuSuaChua. */
public class PhieuSuaChuaDAO implements DuLieuDAO {
    // ===== CẤU HÌNH VÀ TÊN BẢNG/CỘT =====
    private static final String KHOA_CHINH = "maPSC";
    private static final String[] CAC_COT = new String[] { "maPSC", "maBaoGia", "maKhachHang", "VIN",
            "maNhanVien", "maNguoiDuyetHuy", "ngayLap", "ngayHoanThanh", "trangThai", "lyDoHuy" };
    private static final String[] CAC_KHOA = new String[] { "maPSC" };
    private static final Map<String, String> KIEU_COT = Map.ofEntries(Map.entry("maPSC", "varchar:6"),
            Map.entry("maBaoGia", "varchar:6"), Map.entry("maKhachHang", "varchar:5"),
            Map.entry("VIN", "varchar:17"), Map.entry("maNhanVien", "varchar:5"),
            Map.entry("maNguoiDuyetHuy", "varchar:5"), Map.entry("ngayLap", "datetime2:0"),
            Map.entry("ngayHoanThanh", "datetime2:0"), Map.entry("trangThai", "nvarchar:50"),
            Map.entry("lyDoHuy", "nvarchar:500"));
    private static final Set<String> COT_BAT_BUOC = Set.of("maPSC", "maBaoGia", "maKhachHang", "VIN",
            "maNhanVien", "ngayLap", "trangThai");

    private final String table = "PhieuSuaChua";
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
        return "PSC";
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
    // ===== PHƯƠNG THỨC: createRepair(String, String) — Lập phiếu sửa chữa. =====
    public static String createRepair(String quoteId, String employee) throws SQLException {
        return transaction(conn -> {
            Map<String, Object> quote = lock(conn, "PhieuBaoGia", quoteId);
            require("Đã xác nhận".equals(quote.get("trangThai")), "Hãy xác nhận báo giá trước.");
            Map<String, Object> check = lock(conn, "PhieuKiemTra", quote.get("maPKT"));
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("maBaoGia", quoteId);
            header.put("maKhachHang", check.get("maKhachHang"));
            header.put("VIN", quote.get("VIN"));
            header.put("maNhanVien", employee);
            header.put("ngayLap", now(conn));
            header.put("ngayHoanThanh", null);
            header.put("trangThai", "Chờ sửa chữa");
            List<LinkedHashMap<String, Object>> quoted = new dao.ChiTietPhieuBaoGiaDAO().getAll(conn,
                    Map.of("maBaoGia", quoteId));
            require(!quoted.isEmpty(), "Báo giá chưa có chi tiết.");
            String id = new dao.PhieuSuaChuaDAO().insert(conn, header);
            for (Map<String, Object> line : quoted) {
                String part = String.valueOf(line.get("maPhuTung"));
                int quantity = ((Number) line.get("soLuongPhuTung")).intValue();
                String quoteState = String.valueOf(line.get("trangThai"));

                String itemState;

                if ("PT000".equals(part)) {
                    // Hạng mục không dùng phụ tùng, không cần cấp phát.
                    itemState = "Đang sửa chữa";
                } else {
                    require(quantity > 0, "Số lượng phụ tùng phải lớn hơn 0.");

                    require(Set.of("Chờ sửa chữa", "Thiếu phụ tùng").contains(quoteState),
                            "Trạng thái chi tiết báo giá không hợp lệ.");

                    // Chờ sửa chữa nghĩa là đã giữ hàng khi xác nhận báo giá.
                    boolean reserved = "Chờ sửa chữa".equals(quoteState);

                    // Nếu trước đó thiếu, thử giữ hàng bằng lượng tồn hiện tại.
                    if (!reserved) {
                        reserved = execute(conn,
                                "UPDATE dbo.TonKho " + "SET soLuongHienTai=soLuongHienTai-? "
                                        + "WHERE maPhuTung=? AND soLuongHienTai>=?",
                                quantity, part, quantity) == 1;

                        if (reserved) {
                            execute(conn,
                                    "UPDATE dbo.ChiTietPhieuBaoGia " + "SET trangThai=N'Chờ sửa chữa' "
                                            + "WHERE maBaoGia=? AND maDichVu=? " + "AND maPhuTung=?",
                                    quoteId, line.get("maDichVu"), part);
                        }
                    }

                    itemState = reserved ? "Chờ sửa chữa" : "Thiếu phụ tùng";
                }
                Map<String, Object> detail = new LinkedHashMap<>();
                detail.put("maPSC", id);
                detail.put("maDichVu", line.get("maDichVu"));
                detail.put("maPhuTung", line.get("maPhuTung"));
                detail.put("soLuongPhuTung", line.get("soLuongPhuTung"));
                detail.put("trangThaiHangMuc", itemState);
                detail.put("ketQuaNghiemThu", "Không đạt");
                detail.put("thoiHanBaoHanh", 0);
                new dao.ChiTietPhieuSuaChuaDAO().insert(conn, detail);
            }
            return id;
        });
    }

    // ===== PHƯƠNG THỨC: startRepair(String) — Chuyển phiếu sửa chữa sang trạng thái bắt đầu. =====
    public static void startRepair(String id) throws SQLException {
        transaction(conn -> {
            Map<String, Object> repair = lock(conn, "PhieuSuaChua", id);
            require("Chờ sửa chữa".equals(repair.get("trangThai")), "Phiếu không còn chờ sửa chữa.");
            require(exists(conn, "SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=?", id),
                    "Phiếu chưa có hạng mục.");
            require(!exists(conn,
                    "SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=? AND trangThaiHangMuc<>N'Đang sửa chữa'",
                    id), "Cần cấp phát đủ phụ tùng cho mọi hạng mục trước khi bắt đầu.");
            execute(conn, "UPDATE dbo.PhieuSuaChua SET trangThai=N'Đang sửa chữa' WHERE maPSC=?", id);
            return null;
        });
    }

    // ===== PHƯƠNG THỨC: finishRepair(String) — Hoàn tất sửa chữa. =====
    public static void finishRepair(String id) throws SQLException {
        acceptRepair(id);
    }

    // ===== PHƯƠNG THỨC: acceptRepair(String) — Chấp nhận yêu cầu sửa chữa. =====
    public static void acceptRepair(String id) throws SQLException {
        transaction(conn -> {
            String state = String.valueOf(lock(conn, "PhieuSuaChua", id).get("trangThai"));
            require("Đang sửa chữa".equals(state) || "Đã sửa xong".equals(state),
                    "Chỉ nghiệm thu phiếu đang sửa chữa hoặc phiếu đã sửa xong chưa nghiệm thu.");
            require(exists(conn, "SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=?", id) && !exists(conn,
                    "SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=? AND trangThaiHangMuc<>N'Hoàn thành'",
                    id), "Còn hạng mục chưa hoàn thành.");
            require(exists(conn,
                    "SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=? AND ketQuaNghiemThu<>N'Đạt'", id),
                    "Phiếu đã nghiệm thu đạt, không được bắt đầu lại thời hạn bảo hành.");
            Timestamp start = now(conn);
            execute(conn,
                    "UPDATE dbo.ChiTietPhieuSuaChua SET ketQuaNghiemThu=N'Đạt',ngayBatDauBaoHanh=CASE WHEN thoiHanBaoHanh>0 THEN ? ELSE NULL END,ngayHetHanBaoHanh=CASE WHEN thoiHanBaoHanh>0 THEN DATEADD(month,thoiHanBaoHanh,?) ELSE NULL END WHERE maPSC=?",
                    start, start, id);
            if ("Đang sửa chữa".equals(state))
                execute(conn,
                        "UPDATE dbo.PhieuSuaChua SET trangThai=N'Đã sửa xong',ngayHoanThanh=? WHERE maPSC=?",
                        start, id);
            return null;
        });
    }

    // ===== PHƯƠNG THỨC: handover(String) — Hoàn tất bàn giao phương tiện theo phiếu. =====
    public static void handover(String id) throws SQLException {
        transaction(conn -> {
            lock(conn, "PhieuSuaChua", id);
            require(execute(conn,
                    "UPDATE dbo.PhieuSuaChua SET trangThai=N'Hoàn tất' WHERE maPSC=? AND trangThai=N'Đã thanh toán' AND EXISTS(SELECT 1 FROM dbo.HoaDon WHERE maPSC=? AND trangThaiThanhToan=N'Đã thanh toán')",
                    id, id) == 1, "Chỉ bàn giao sau khi đã thanh toán.");
            return null;
        });
    }

    // ===== PHƯƠNG THỨC: cancelRepair(String) — Hủy nghiệp vụ sửa chữa theo thông tin phiếu. =====
    public static void cancelRepair(String id) throws SQLException {
        cancelRepair(id, "", null);
    }

    // ===== PHƯƠNG THỨC: cancelRepair(String, String, String) — Hủy nghiệp vụ sửa chữa theo thông tin phiếu. =====
    public static void cancelRepair(String id, String reason, String managerEmployee) throws SQLException {
        transaction(conn -> {
            Map<String, Object> repair = lock(conn, "PhieuSuaChua", id);
            require(Set.of("Chờ sửa chữa", "Đang sửa chữa").contains(String.valueOf(repair.get("trangThai"))),
                    "Chỉ hủy phiếu trước nghiệm thu.");
            require(reason != null && !reason.isBlank(), "Bắt buộc ghi lý do hủy phiếu.");
            require(managerEmployee != null && exists(conn,
                    "SELECT 1 FROM dbo.NhanVien WHERE maNhanVien=? AND trangThai=N'Đang làm' AND vaiTro IN (N'Nhân viên quản lý',N'Quản trị viên')",
                    managerEmployee), "Chọn người duyệt hủy là quản lý hoặc quản trị viên đang làm việc.");
            require(!exists(conn, "SELECT 1 FROM dbo.HoaDon WHERE maPSC=?", id),
                    "Phiếu đã có hóa đơn, không thể hủy.");
            List<LinkedHashMap<String, Object>> lines = new dao.ChiTietPhieuSuaChuaDAO().getAll(conn,
                    Map.of("maPSC", id));
            for (Map<String, Object> line : lines)
                if (!"PT000".equals(line.get("maPhuTung"))) {
                    int issued = ((Number) line.get("soLuongCapPhat")).intValue();
                    int used = ((Number) line.get("soLuongSuDung")).intValue();
                    int returned = ((Number) line.get("soLuongHoanTra")).intValue();
                    require(issued == 0 || used + returned == issued,
                            "Cần ghi nhận số lượng đã sử dụng và xác nhận hàng trả thực tế trước khi hủy.");
                    Map<String, Object> quoteLine = one(conn,
                            "SELECT trangThai FROM dbo.ChiTietPhieuBaoGia WHERE maBaoGia=? AND maDichVu=? AND maPhuTung=?",
                            repair.get("maBaoGia"), line.get("maDichVu"), line.get("maPhuTung"));
                    int reserved = "Chờ sửa chữa".equals(quoteLine.get("trangThai"))
                            ? ((Number) line.get("soLuongPhuTung")).intValue() - issued
                            : 0;
                    if (reserved > 0) {
                        require(exists(conn, "SELECT 1 FROM dbo.TonKho WHERE maPhuTung=?",
                                line.get("maPhuTung")), "Không tìm thấy tồn kho để hoàn phần chưa cấp phát.");
                        execute(conn,
                                "UPDATE dbo.TonKho SET soLuongHienTai=soLuongHienTai+? WHERE maPhuTung=?",
                                reserved, line.get("maPhuTung"));
                    }
                }
            execute(conn,
                    "UPDATE dbo.PhieuSuaChua SET trangThai=N'Đã hủy',lyDoHuy=?,maNguoiDuyetHuy=? WHERE maPSC=?",
                    reason.trim(), managerEmployee, id);
            execute(conn,
                    "UPDATE dbo.PhieuBaoGia SET trangThai=N'Hết hiệu lực' WHERE maBaoGia=? AND trangThai=N'Đã xác nhận'",
                    repair.get("maBaoGia"));
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

            if (field.equals("maKhachHang") && !raw.matches("KH[0-9]{3}"))
                fail(field, "Mã khách hàng phải có dạng KH001.");

            if (field.equals("VIN") && !raw.matches("[A-HJ-NPR-Z0-9]{17}"))
                fail(field, "VIN phải gồm 17 ký tự hợp lệ.");

        }
        dates(row, "ngayLap", "ngayHoanThanh", false);

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
