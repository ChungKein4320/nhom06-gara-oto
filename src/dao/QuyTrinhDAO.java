package dao;

import connectDB.KetNoiCSDL;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

// ===== DAO: GIAO DỊCH VÀ QUY TẮC GHI DỮ LIỆU =====

/** Mọi thay đổi trạng thái nghiệp vụ bắt buộc đi qua lớp này và nằm trong transaction. */
public final class QuyTrinhDAO {
    // ===== CẤU HÌNH VÀ TÊN BẢNG/CỘT =====
    // ===== KHỞI TẠO ĐỐI TƯỢNG: QuyTrinhDAO() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    private QuyTrinhDAO() {
    }

    // ===== TRUY VẤN VÀ THAO TÁC DỮ LIỆU =====

    public interface Work<T> {
        // ===== PHƯƠNG THỨC: run(Connection) — Thực hiện tác vụ nền. =====
        T run(Connection conn) throws SQLException;
    }

    // ===== PHƯƠNG THỨC: transaction(Work<T>) — Chạy giao dịch cơ sở dữ liệu và hoàn tác khi lỗi. =====
    public static <T> T transaction(Work<T> work) throws SQLException {
        try (Connection conn = KetNoiCSDL.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            boolean completed = false;
            boolean rolledBack = false;
            try {
                T result = work.run(conn);
                conn.commit();
                completed = true;
                return result;
            } catch (SQLException t) {
                try {
                    conn.rollback();
                    rolledBack = true;
                } catch (SQLException rbEx) {
                    t.addSuppressed(rbEx);
                }
                throw t;
            } finally {
                if (completed || rolledBack) {
                    try {
                        conn.setAutoCommit(originalAutoCommit);
                    } catch (SQLException ignored) {
                    }
                }
            }
        }
    }

    // ===== PHƯƠNG THỨC: lock(Connection, String, Object) — Lọc nghiệp vụ tương ứng. =====
    static Map<String, Object> lock(Connection conn, String table, Object id) throws SQLException {
        String pk = ChonDAO.khoaChinh(table);
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM dbo.[" + table + "] WITH (UPDLOCK,HOLDLOCK) WHERE [" + pk + "]=?")) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    throw new SQLException("Bản ghi " + table + " không tồn tại: " + id);
                Map<String, Object> row = new LinkedHashMap<>();
                for (String field : ChonDAO.cacCot(table))
                    row.put(field, rs.getObject(field));
                return row;
            }
        }
    }

    // ===== PHƯƠNG THỨC: exists(Connection, String, Object) — Kiểm tra bản ghi có tồn tại trong cơ sở dữ liệu. =====
    static boolean exists(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++)
                ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ===== PHƯƠNG THỨC: requireActiveEmployee(Connection, String, String) — Bảo đảm nhân viên hiện tại còn hoạt động. =====
    public static void requireActiveEmployee(Connection conn, String employeeId, String roleDescription)
            throws SQLException {
        NhanVienDAO.requireActiveEmployee(conn, employeeId, roleDescription);
    }

    // ===== PHƯƠNG THỨC: require(boolean, String) — Kiểm tra điều kiện nghiệp vụ và báo lỗi nếu không đạt. =====
    static void require(boolean ok, String message) throws SQLException {
        if (!ok)
            throw new SQLException(message);
    }

    // ===== PHƯƠNG THỨC: equal(Map<String,Object>, Map<String,Object>, String) — So sánh giá trị của các trường dữ liệu. =====
    static void equal(Map<String, Object> a, Map<String, Object> b, String... fields) throws SQLException {
        for (String field : fields)
            require(Objects.equals(a.get(field), b.get(field)), "Hồ sơ không khớp: " + field);
    }

    // ===== PHƯƠNG THỨC: chronological(Map<String,Object>, String, Map<String,Object>, String) — Sắp xếp dữ liệu theo trình tự thời gian. =====
    static void chronological(Map<String, Object> current, String field, Map<String, Object> previous,
            String before) throws SQLException {
        if (current.get(field) instanceof java.util.Date date
                && previous.get(before) instanceof java.util.Date start)
            require(!date.before(start), field + " không được trước " + before + " của phiếu trước.");
    }

    private static final Map<String, String> CHILDREN = Map.of("PhieuTiepNhan", "ChiTietPhieuTiepNhan",
            "PhieuKiemTra", "ChiTietPhieuKiemTra", "PhieuBaoGia", "ChiTietPhieuBaoGia", "PhieuSuaChua",
            "ChiTietPhieuSuaChua", "YeuCauNhapKho", "ChiTietYeuCauNhapKho", "PhieuNhapKho",
            "ChiTietPhieuNhapKho", "HoaDon", "ChiTietHoaDon");

    // ===== PHƯƠNG THỨC: detailTable(String) — Xác định bảng chi tiết tương ứng với chứng từ. =====
    public static String detailTable(String table) {
        return CHILDREN.get(table);
    }

    // ===== PHƯƠNG THỨC: parent(String) — Xử lý đối tượng cha. =====
    static String parent(String child) {
        return CHILDREN.entrySet().stream().filter(e -> e.getValue().equals(child)).map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    // ===== PHƯƠNG THỨC: documentEditProblem(Connection, String, String) — Đọc edit problem. =====
    public static String documentEditProblem(Connection conn, String table, String id) throws SQLException {
        if (table.equals("PhieuSuaChua"))
            return "Phiếu sửa chữa được kế thừa từ báo giá; dùng các thao tác cấp phát, nghiệm thu và bàn giao để cập nhật.";
        if (table.equals("HoaDon"))
            return "Hóa đơn đã lập được khóa nội dung; cập nhật mã giảm giá và thanh toán tại trang Hóa đơn.";
        Map<String, Object> row = lock(conn, table, id);
        try {
            editable(conn, table, row);
            return null;
        } catch (SQLException ex) {
            return ex.getMessage();
        }
    }

    // ===== PHƯƠNG THỨC: editable(Connection, String, Map<String,Object>) — Kiểm tra chứng từ có được phép chỉnh sửa. =====
    static void editable(Connection conn, String table, Map<String, Object> row) throws SQLException {
        String state = String.valueOf(row.get("trangThai"));
        Object id = row.get(ChonDAO.khoaChinh(table));
        switch (table) {
        case "YeuCauNhapKho" ->
            require(state.equals("Chờ duyệt"), "Yêu cầu nhập kho đã xử lý; không được sửa.");
        case "PhieuBaoGia" -> require(state.equals("Còn hiệu lực") && row.get("thoiGianXacNhan") == null,
                "Báo giá đã khóa; không được sửa.");
        case "PhieuNhapKho" -> require(state.equals("Chờ xác nhận nhập kho"),
                "Phiếu nhập đã xử lý; không được sửa hoặc nhập kho lần nữa.");
        case "PhieuSuaChua" ->
            require(state.equals("Chờ sửa chữa"), "Chỉ sửa nội dung phiếu trước khi bắt đầu sửa chữa.");
        case "HoaDon" ->
            throw new SQLException("Hóa đơn được lập từ phiếu sửa chữa; dùng nghiệp vụ thanh toán/giảm giá.");
        case "PhieuTiepNhan" -> require(!exists(conn, "SELECT 1 FROM dbo.PhieuKiemTra WHERE maPTN=?", id),
                "Phiếu tiếp nhận đã có kiểm tra, không được sửa.");
        case "PhieuKiemTra" -> require(!exists(conn, "SELECT 1 FROM dbo.PhieuBaoGia WHERE maPKT=?", id),
                "Phiếu kiểm tra đã có báo giá, không được sửa.");
        default -> {
        }
        }
    }

    // ===== PHƯƠNG THỨC: guardWrite(Connection, String, Map<String,Object>, Map<String,Object>) — Kiểm tra quyền và trạng thái trước khi ghi dữ liệu. =====
    /** Chặn sửa trạng thái trực tiếp và sửa nội dung sau khi đã được sử dụng. */
    static void guardWrite(Connection conn, String table, Map<String, Object> values, Map<String, Object> key)
            throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        if (key != null && ChonDAO.cacKhoa(table).length == 1) {
            row.putAll(lock(conn, table, key.get(ChonDAO.khoaChinh(table))));
            editable(conn, table, row);
            if (Set.of("PhieuBaoGia", "PhieuSuaChua", "PhieuNhapKho", "YeuCauNhapKho").contains(table)) {
                for (String field : List.of("trangThai", "ngayHoanThanh", "thoiGianXacNhan", "ngayDuyet"))
                    if (values.containsKey(field))
                        require(Objects.equals(row.get(field), values.get(field)),
                                "Dùng nút nghiệp vụ để thay đổi trạng thái/ngày hoàn thành/ngày duyệt.");
            }
        }
        if (key != null)
            row.putAll(key);
        row.putAll(values);
        String owner = parent(table);
        if (owner != null) {
            String fk = ChonDAO.khoaChinh(owner);
            Object ownerId = row.get(fk);
            if (ownerId == null && key != null)
                ownerId = key.get(fk);
            Map<String, Object> header = lock(conn, owner, ownerId);
            editable(conn, owner, header);
            if (table.equals("ChiTietPhieuSuaChua")) {
                require(key == null,
                        "Không sửa trực tiếp hạng mục sửa chữa; dùng nghiệp vụ cấp phát/tiến độ.");
                String initialState = String.valueOf(row.get("trangThaiHangMuc"));
                boolean noPart = "PT000".equals(row.get("maPhuTung"));
                require((noPart && "Đang sửa chữa".equals(initialState))
                        || (!noPart && Set.of("Chờ sửa chữa", "Thiếu phụ tùng").contains(initialState)),
                        "Trạng thái hạng mục ban đầu không hợp lệ.");
                require("Không đạt".equals(row.get("ketQuaNghiemThu")),
                        "Nghiệm thu phải qua nghiệp vụ xác nhận.");
                require(row.get("ngayBatDauBaoHanh") == null && row.get("ngayHetHanBaoHanh") == null,
                        "Ngày bảo hành được tính khi nghiệm thu.");
            }
            return;
        }
        if (key == null) {
            if (table.equals("YeuCauNhapKho"))
                require("Chờ duyệt".equals(row.get("trangThai")), "Yêu cầu nhập kho mới phải chờ duyệt.");
            if (table.equals("PhieuBaoGia"))
                require("Còn hiệu lực".equals(row.get("trangThai")) && row.get("thoiGianXacNhan") == null,
                        "Báo giá mới phải còn hiệu lực, chưa xác nhận.");
            if (table.equals("PhieuNhapKho"))
                require("Chờ xác nhận nhập kho".equals(row.get("trangThai")),
                        "Phiếu nhập mới phải ở trạng thái Chờ xác nhận nhập kho.");
            if (table.equals("PhieuSuaChua"))
                require("Chờ sửa chữa".equals(row.get("trangThai")) && row.get("ngayHoanThanh") == null,
                        "Phiếu sửa mới phải chờ sửa chữa.");
        }

        if (!table.equals("NhanVien") && row.get("maNhanVien") != null) {
            require(exists(conn,
                    "SELECT 1 FROM dbo.NhanVien " + "WHERE maNhanVien=? AND trangThai=N'Đang làm'",
                    row.get("maNhanVien")), "Nhân viên không còn đang làm.");
        }

        switch (table) {
        case "PhieuKiemTra" -> {
            Map<String, Object> receipt = lock(conn, "PhieuTiepNhan", row.get("maPTN"));
            equal(row, receipt, "maKhachHang", "VIN");
            chronological(row, "ngayKiemTra", receipt, "ngayTiepNhan");
        }
        case "PhieuBaoGia" -> {
            Map<String, Object> check = lock(conn, "PhieuKiemTra", row.get("maPKT"));
            equal(row, check, "maKhachHang", "VIN");
            chronological(row, "ngayLap", check, "ngayKiemTra");
        }
        case "PhieuSuaChua" -> {
            Map<String, Object> quote = lock(conn, "PhieuBaoGia", row.get("maBaoGia"));
            require("Đã xác nhận".equals(quote.get("trangThai")), "Báo giá chưa được xác nhận.");
            equal(row, quote, "VIN");
            equal(row, lock(conn, "PhieuKiemTra", quote.get("maPKT")), "maKhachHang");
            chronological(row, "ngayLap", quote, "thoiGianXacNhan");
            require(!exists(conn,
                    "SELECT 1 FROM dbo.PhieuSuaChua WHERE maBaoGia=? AND maPSC<>? AND trangThai<>N'Đã hủy'",
                    row.get("maBaoGia"), key == null ? "" : key.get("maPSC")),
                    "Báo giá đã có phiếu sửa chữa.");
        }
        case "HoaDon", "LichBaoHanh", "LichNhacBaoDuong" -> {
            if (table.equals("HoaDon") && row.get("maPSC") == null) {
                require(row.get("VIN") == null, "Hóa đơn bán phụ tùng riêng không gắn xe.");
                break;
            }
            Map<String, Object> repair = lock(conn, "PhieuSuaChua", row.get("maPSC"));
            equal(row, repair, "maKhachHang", "VIN");
            if (table.equals("HoaDon"))
                chronological(row, "ngayLap", repair, "ngayHoanThanh");
            if (table.equals("LichNhacBaoDuong")) {
                require(Set.of("Đã sửa xong", "Chờ thanh toán", "Đã thanh toán", "Hoàn tất").contains(
                        String.valueOf(repair.get("trangThai"))), "Chỉ lập lịch bảo dưỡng sau khi sửa xong.");
                chronological(row, "ngayNhac", repair, "ngayHoanThanh");
            }
            if (table.equals("LichBaoHanh"))
                require(row.get("maDichVu") != null && row.get("maPhuTung") != null && exists(conn,
                        "SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=? AND maDichVu=? AND maPhuTung=? AND ketQuaNghiemThu=N'Đạt' AND ngayBatDauBaoHanh<=? AND ngayHetHanBaoHanh>=?",
                        row.get("maPSC"), row.get("maDichVu"), row.get("maPhuTung"), row.get("ngayXuLy"),
                        row.get("ngayXuLy")), "Chọn đúng hạng mục đang còn bảo hành tại ngày xử lý.");
        }
        case "LichSuTichDiem" -> {
            Map<String, Object> invoice = lock(conn, "HoaDon", row.get("maHoaDon"));
            equal(row, invoice, "maKhachHang");
            require("Đã thanh toán".equals(invoice.get("trangThaiThanhToan")),
                    "Chỉ tích điểm cho hóa đơn đã thanh toán.");
            require(invoice.get("maPSC") != null, "Chỉ hóa đơn sửa chữa/dịch vụ được tích điểm.");
            BigDecimal total = ((BigDecimal) invoice.get("tienPhuTung"))
                    .add((BigDecimal) invoice.get("tienCong")).add((BigDecimal) invoice.get("dichVuKhac"))
                    .add((BigDecimal) invoice.get("thueVAT"));
            require(((Number) row.get("soDiem")).intValue() == InvoiceMath.points(total),
                    "Số điểm tính trên tổng hóa đơn sau VAT, trước giảm giá: 100.000đ = 1 điểm.");
            chronological(row, "ngayTichDiem", invoice, "ngayLap");
            require(!exists(conn, "SELECT 1 FROM dbo.LichSuTichDiem WHERE maHoaDon=?", row.get("maHoaDon")),
                    "Hóa đơn đã được tích điểm.");
        }
        default -> {
        }
        }
    }

    // ===== PHƯƠNG THỨC: validateReceiptIntegrity(Connection, Map<String,Object>, List<? extends Map<String,Object>>, String, boolean) — Kiểm tra tính hợp lệ của receipt integrity. =====
    /** UC009: Kiểm tra toàn vẹn phiếu nhập kho gắn với đơn duyệt và số lượng hợp lệ. */
    public static void validateReceiptIntegrity(Connection conn, Map<String, Object> header,
            List<? extends Map<String, Object>> details, String id, boolean isConfirming)
            throws SQLException {
        PhieuNhapKhoDAO.validateReceiptIntegrity(conn, header, details, id, isConfirming);
    }

    // ===== PHƯƠNG THỨC: saveDocument(String, Map<String,Object>, List<Map<String,Object>>, String) — Lưu document. =====
    public static String saveDocument(String table, Map<String, Object> header,
            List<Map<String, Object>> details, String id) throws SQLException {
        if (table.equals("YeuCauNhapKho") || table.equals("PhieuNhapKho")) {
            throw new SecurityException(
                    "Thao tác lưu " + table + " yêu cầu xác thực vai trò và mã nhân viên.");
        }
        return saveDocument(table, header, details, id, null, null);
    }

    // ===== PHƯƠNG THỨC: saveDocument(String, Map<String,Object>, List<Map<String,Object>>, String, gui.VaiTro, String) — Lưu document. =====
    public static String saveDocument(String table, Map<String, Object> header,
            List<Map<String, Object>> details, String id, gui.VaiTro role, String employeeId)
            throws SQLException {
        if (table.equals("YeuCauNhapKho")) {
            if (role == null) {
                throw new SecurityException("Thao tác lưu Yêu cầu nhập kho yêu cầu xác thực vai trò.");
            }
            if (role == gui.VaiTro.QUAN_LY) {
                throw new SecurityException(
                        "Quản lý chỉ có quyền duyệt hoặc từ chối yêu cầu nhập kho, không được tạo hoặc sửa nội dung yêu cầu.");
            }
            if (role != gui.VaiTro.KHO && role != gui.VaiTro.ADMIN) {
                throw new SecurityException(
                        "Chỉ Nhân viên kho hoặc Quản trị viên mới có quyền tạo/sửa yêu cầu nhập kho.");
            }
            if (employeeId == null || employeeId.isBlank()) {
                throw new SecurityException("Mã nhân viên người tạo không hợp lệ.");
            }
        } else if (table.equals("PhieuNhapKho")) {
            if (role == null) {
                throw new SecurityException("Thao tác lưu Phiếu nhập kho yêu cầu xác thực vai trò.");
            }
            if (role == gui.VaiTro.QUAN_LY) {
                throw new SecurityException(
                        "Quản lý không có quyền tạo hoặc chỉnh sửa phiếu nhập kho thực tế.");
            }
            if (role != gui.VaiTro.KHO && role != gui.VaiTro.ADMIN) {
                throw new SecurityException(
                        "Chỉ Nhân viên kho hoặc Quản trị viên mới có quyền tạo/sửa phiếu nhập kho.");
            }
            if (employeeId == null || employeeId.isBlank()) {
                throw new SecurityException("Mã nhân viên thực hiện không hợp lệ.");
            }
        }

        return transaction(conn -> {
            DuLieuDAO dao = ChonDAO.choBang(table);
            String child = detailTable(table);
            String fk = ChonDAO.khoaChinh(table);
            require(child != null && !Set.of("HoaDon", "PhieuSuaChua").contains(table),
                    "Loại phiếu không được lập bằng biểu mẫu này.");
            require(!details.isEmpty(), "Phiếu phải có ít nhất một hạng mục chi tiết.");

            if (table.equals("YeuCauNhapKho")) {
                requireActiveEmployee(conn, employeeId, "người tạo");
                if (id == null) {
                    header.put("maNhanVien", employeeId.trim());
                    header.put("trangThai", "Chờ duyệt");
                } else {
                    Map<String, Object> current = lock(conn, "YeuCauNhapKho", id);
                    require("Chờ duyệt".equals(current.get("trangThai")), "Yêu cầu nhập kho ở trạng thái '"
                            + current.get("trangThai") + "'; chỉ được sửa khi đang 'Chờ duyệt'.");
                    header.put("maNhanVien", current.get("maNhanVien"));
                    header.put("trangThai", "Chờ duyệt");
                }
            } else if (table.equals("PhieuNhapKho")) {
                requireActiveEmployee(conn, employeeId, "thực hiện");
                if (id == null) {
                    header.put("maNhanVien", employeeId.trim());
                    header.put("trangThai", "Chờ xác nhận nhập kho");
                } else {
                    Map<String, Object> current = lock(conn, "PhieuNhapKho", id);
                    require("Chờ xác nhận nhập kho".equals(current.get("trangThai")),
                            "Phiếu nhập kho ở trạng thái '" + current.get("trangThai")
                                    + "'; chỉ được chỉnh sửa bản nháp ('Chờ xác nhận nhập kho').");
                    header.put("maNhanVien", current.get("maNhanVien"));
                    header.put("trangThai", "Chờ xác nhận nhập kho");
                }
                validateReceiptIntegrity(conn, header, details, id, false);
            }

            String savedId;
            if (id == null) {
                savedId = dao.insert(conn, header);
                insertDetails(conn, child, fk, savedId, details);
            } else {
                savedId = id;
                editable(conn, table, lock(conn, table, id));
                dao.update(conn, Map.of(fk, id), header);
                execute(conn, "DELETE FROM dbo.[" + child + "] WHERE [" + fk + "]=?", id);
                insertDetails(conn, child, fk, id, details);
            }
            return savedId;
        });
    }

    public record IntakeResult(String maPTN, String maKhachHang, String VIN) {
    }

    // ===== PHƯƠNG THỨC: createIntake(Map<String,Object>, boolean, String, Map<String,Object>, boolean, String, Map<String,Object>, List<Map<String,Object>>) — Lập phiếu tiếp nhận phương tiện. =====
    public static IntakeResult createIntake(Map<String, Object> header, boolean createCustomer,
            String customerId, Map<String, Object> customer, boolean createVehicle, String vehicleId,
            Map<String, Object> vehicle, List<Map<String, Object>> details) throws SQLException {
        return PhieuTiepNhanDAO.createIntake(header, createCustomer, customerId, customer, createVehicle,
                vehicleId, vehicle, details);
    }

    // ===== PHƯƠNG THỨC: insertDetails(Connection, String, String, String, List<Map<String,Object>>) — Thêm dòng chi tiết. =====
    static void insertDetails(Connection conn, String child, String fk, String id,
            List<Map<String, Object>> details) throws SQLException {
        DuLieuDAO dao = ChonDAO.choBang(child);
        for (Map<String, Object> detail : details) {
            Map<String, Object> row = new LinkedHashMap<>(detail);
            row.put(fk, id);
            dao.insert(conn, row);
        }
    }

    // ===== PHƯƠNG THỨC: confirmQuote(String) — Xác nhận báo giá. =====
    public static void confirmQuote(String id) throws SQLException {
        PhieuBaoGiaDAO.confirmQuote(id);
    }

    // ===== PHƯƠNG THỨC: createRepair(String, String) — Lập phiếu sửa chữa. =====
    public static String createRepair(String quoteId, String employee) throws SQLException {
        return PhieuSuaChuaDAO.createRepair(quoteId, employee);
    }

    // ===== PHƯƠNG THỨC: approvePurchaseRequest(String, boolean, gui.VaiTro, String) — Phê duyệt hoặc từ chối yêu cầu nhập kho. =====
    /** UC009: Quản lý hoặc Admin duyệt hoặc từ chối yêu cầu nhập kho. Tuyệt đối KHÔNG thay đổi tồn kho. */
    public static void approvePurchaseRequest(String id, boolean approve, gui.VaiTro role, String employeeId)
            throws SQLException {
        YeuCauNhapKhoDAO.approvePurchaseRequest(id, approve, role, employeeId);
    }

    // ===== PHƯƠNG THỨC: prepareReceiptFromRequest(String, gui.VaiTro, String) — Tạo phiếu nhập kho từ yêu cầu đã duyệt. =====
    /** UC009: Nhân viên kho hoặc Admin chuẩn bị dữ liệu lập phiếu nhập kho thực tế từ yêu cầu đã duyệt. */
    public static Map<String, Object> prepareReceiptFromRequest(String ycnId, gui.VaiTro role,
            String employeeId) throws SQLException {
        return PhieuNhapKhoDAO.prepareReceiptFromRequest(ycnId, role, employeeId);
    }

    // ===== PHƯƠNG THỨC: confirmActualStockIn(String, gui.VaiTro, String) — Xác nhận số lượng phụ tùng thực nhập. =====
    /** UC009: Nhân viên kho hoặc Admin xác nhận nhập kho thực tế. CHỈ TẠI BƯỚC NÀY TỒN KHO MỚI TĂNG theo số lượng chấp nhận. */
    public static void confirmActualStockIn(String id, gui.VaiTro role, String employeeId)
            throws SQLException {
        PhieuNhapKhoDAO.confirmActualStockIn(id, role, employeeId);
    }

    // ===== PHƯƠNG THỨC: startRepair(String) — Chuyển phiếu sửa chữa sang trạng thái bắt đầu. =====
    public static void startRepair(String id) throws SQLException {
        PhieuSuaChuaDAO.startRepair(id);
    }

    // ===== PHƯƠNG THỨC: completeRepairItem(String, String, String, int) — Hoàn tất một hạng mục sửa chữa. =====
    public static void completeRepairItem(String id, String service, String part, int months)
            throws SQLException {
        ChiTietPhieuSuaChuaDAO.completeRepairItem(id, service, part, months);
    }

    // ===== PHƯƠNG THỨC: finishRepair(String) — Hoàn tất sửa chữa. =====
    public static void finishRepair(String id) throws SQLException {
        PhieuSuaChuaDAO.finishRepair(id);
    }

    // ===== PHƯƠNG THỨC: acceptRepair(String) — Chấp nhận yêu cầu sửa chữa. =====
    public static void acceptRepair(String id) throws SQLException {
        PhieuSuaChuaDAO.acceptRepair(id);
    }

    // ===== PHƯƠNG THỨC: createInvoice(String, String, String, String) — Lập hóa đơn từ thông tin giao dịch. =====
    public static String createInvoice(String repairId, String employee, String method, String coupon)
            throws SQLException {
        return HoaDonDAO.createInvoice(repairId, employee, method, coupon);
    }

    // ===== PHƯƠNG THỨC: insertInvoiceLine(Connection, Map<String,Object>) — Thêm hóa đơn line. =====
    static void insertInvoiceLine(Connection conn, Map<String, Object> row) throws SQLException {
        ChiTietHoaDonDAO.insertInvoiceLine(conn, row);
    }

    // ===== PHƯƠNG THỨC: couponDiscount(Connection, String, Object, BigDecimal) — Tính mức giảm giá áp dụng từ mã ưu đãi. =====
    static BigDecimal couponDiscount(Connection conn, String coupon, Object customer, BigDecimal subtotal)
            throws SQLException {
        return MaGiamGiaDAO.couponDiscount(conn, coupon, customer, subtotal);
    }

    // ===== PHƯƠNG THỨC: now(Connection) — Lấy thời gian hiện tại từ cơ sở dữ liệu. =====
    static Timestamp now(Connection conn) throws SQLException {
        Timestamp serverNow = (Timestamp) one(conn, "SELECT SYSDATETIME() AS serverNow").get("serverNow");
        return truncateToSecond(serverNow);
    }

    // ===== PHƯƠNG THỨC: truncateToSecond(Timestamp) — Bỏ phần mili giây khỏi mốc thời gian. =====
    static Timestamp truncateToSecond(Timestamp time) {
        return Timestamp.valueOf(time.toLocalDateTime().withNano(0));
    }

    // ===== PHƯƠNG THỨC: markReminder(String) — Đánh dấu lịch nhắc bảo dưỡng đã được xử lý. =====
    public static void markReminder(String id) throws SQLException {
        LichNhacBaoDuongDAO.markReminder(id);
    }

    // ===== PHƯƠNG THỨC: handover(String) — Hoàn tất bàn giao phương tiện theo phiếu. =====
    public static void handover(String id) throws SQLException {
        PhieuSuaChuaDAO.handover(id);
    }

    // ===== PHƯƠNG THỨC: cancelRepair(String) — Hủy nghiệp vụ sửa chữa theo thông tin phiếu. =====
    public static void cancelRepair(String id) throws SQLException {
        PhieuSuaChuaDAO.cancelRepair(id);
    }

    // ===== PHƯƠNG THỨC: cancelRepair(String, String, String) — Hủy nghiệp vụ sửa chữa theo thông tin phiếu. =====
    public static void cancelRepair(String id, String reason, String managerEmployee) throws SQLException {
        PhieuSuaChuaDAO.cancelRepair(id, reason, managerEmployee);
    }

    // ===== PHƯƠNG THỨC: one(Connection, String, Object) — Đọc một bản ghi theo điều kiện truy vấn. =====
    static Map<String, Object> one(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++)
                ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    throw new SQLException("Không tìm thấy dữ liệu: " + sql);
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++)
                    row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                return row;
            }
        }
    }

    // ===== PHƯƠNG THỨC: execute(Connection, String, Object) — Thực thi nghiệp vụ tương ứng. =====
    static int execute(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++)
                ps.setObject(i + 1, params[i]);
            return ps.executeUpdate();
        }
    }

    // ===== PHƯƠNG THỨC: recordPartReturn(String, String, String, int, String) — Ghi nhận số lượng phụ tùng được hoàn trả. =====
    public static void recordPartReturn(String repairId, String service, String part, int quantity,
            String warehouseEmployee) throws SQLException {
        ChiTietPhieuSuaChuaDAO.recordPartReturn(repairId, service, part, quantity, warehouseEmployee);
    }

    // ===== PHƯƠNG THỨC: refreshRepairShortages() — Làm mới sửa chữa shortages. =====
    public static void refreshRepairShortages() throws SQLException {
        ChiTietPhieuSuaChuaDAO.refreshRepairShortages();
    }

    // ===== PHƯƠNG THỨC: fulfillShortages(Connection, String) — Cấp phụ tùng để bù số lượng còn thiếu. =====
    static void fulfillShortages(Connection conn, String part) throws SQLException {
        TonKhoDAO.fulfillShortages(conn, part);
    }
}
