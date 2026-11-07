package dao;

import java.math.BigDecimal;
import static dao.QuyTrinhDAO.*;

import connectDB.KetNoiCSDL;
import java.sql.*;
import java.util.*;

/**
 * ============================================================================
 * DAO: PhieuNhapKhoDAO (Truy vấn và xử lý nghiệp vụ Phiếu nhập kho thực tế)
 * ============================================================================
 * 1. Mục đích:
 *    - Thực hiện truy vấn SQL và nghiệp vụ dữ liệu cho bảng dbo.PhieuNhapKho
 *      (đầu phiếu nhập kho phụ tùng thực tế, khóa chính maPNK).
 * 2. Phục vụ màn hình / nghiệp vụ:
 *    - Nghiệp vụ 3 (Lập và xác nhận phiếu nhập kho thực tế - UC009):
 *      + Bước lập nháp: Lưu phiếu ở trạng thái "Chờ xác nhận nhập kho" (chưa cộng tồn kho).
 *      + Bước xác nhận thực nhập: Gọi confirmActualStockIn() để kiểm tra toàn vẹn,
 *        cộng tồn kho theo soLuongNhap, tự động bù hàng thiếu (TonKhoDAO.fulfillShortages),
 *        đổi trạng thái PhieuNhapKho -> "Đã nhập kho" và YeuCauNhapKho -> "Đã xử lý".
 * 3. Phương thức quan trọng:
 *    - validateReceiptIntegrity(...): Kiểm tra toàn vẹn 1-1 giữa Phiếu nhập kho và
 *      Yêu cầu nhập kho đã duyệt (0 <= soLuongNhap <= soLuongThucNhan <= soLuongYeuCau).
 *    - prepareReceiptFromRequest(...): Kiểm tra điều kiện hợp lệ và trả về Map đầu phiếu YCNK đã duyệt.
 *    - confirmActualStockIn(...): Giao dịch xác nhận nhập kho thực tế và tăng tồn kho.
 * ============================================================================
 */
public class PhieuNhapKhoDAO implements DuLieuDAO {
    // ===== CẤU HÌNH BẢNG VÀ CỘT DỮ LIỆU =====
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

    // ===== NHÓM 1: TRUY VẤN VÀ THAO TÁC CRUD CƠ BẢN (Triển khai DuLieuDAO) =====

    // Lấy toàn bộ danh sách Phiếu nhập kho từ CSDL (loại bỏ các bản ghi đã ẩn mềm).
    @Override
    public List<LinkedHashMap<String, Object>> getAll() throws SQLException {
        try (Connection conn = KetNoiCSDL.getConnection()) {
            List<LinkedHashMap<String, Object>> rows = getAll(conn, null);
            rows.removeIf(row -> HiddenRecords.isHidden(table, String.valueOf(row.get(primary))));
            return rows;
        }
    }

    // Truy vấn danh sách Phiếu nhập kho theo điều kiện lọc (dùng chung Connection trong transaction).
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

    // Xem trước mã phiếu nhập kho tiếp theo từ Sequence dbo.Seq_PhieuNhapKho (ví dụ: PNK001).
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

    // Lấy tiền tố mã phiếu nhập kho ("PNK").
    private String layTienToMa() {
        return "PNK";
    }

    // Thêm mới đầu phiếu nhập kho (mở transaction độc lập).
    @Override
    public void insert(Map<String, Object> row) throws SQLException {
        insert(row, null);
    }

    // Thêm mới đầu phiếu nhập kho trong giao dịch của QuyTrinhDAO.
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
        // BƯỚC 1: Sinh mã maPNK mới từ Sequence dbo.Seq_PhieuNhapKho.
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
        // BƯỚC 3: Thực thi INSERT INTO dbo.[PhieuNhapKho] và trả về mã maPNK vừa tạo.
        String fields = String.join(",", row.keySet().stream().map(s -> "[" + s + "]").toList());
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO dbo.[" + table + "](" + fields
                + ") VALUES(" + String.join(",", Collections.nCopies(row.size(), "?")) + ")")) {
            ganThamSoSQL(ps, row.values());
            ps.executeUpdate();
        }
        return String.valueOf(row.get(primary));
    }

    // Cập nhật đầu phiếu nhập kho theo mã maPNK.
    @Override
    public void update(String id, Map<String, Object> row) throws SQLException {
        if (CAC_KHOA.length != 1)
            throw new SQLException("Cần đầy đủ khóa ghép để sửa chi tiết.");
        QuyTrinhDAO.transaction(conn -> {
            update(conn, Map.of(primary, id), row);
            return null;
        });
    }

    // Cập nhật đầu phiếu nhập kho trong giao dịch đang mở.
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

    // Ẩn mềm bản ghi phiếu nhập kho khỏi danh sách hiển thị.
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

    // ===== NHÓM 2: CÁC PHƯƠNG THỨC NGHIỆP VỤ LẬP VÀ XÁC NHẬN PHIẾU NHẬP KHO =====

    /**
     * NGHIỆP VỤ 3: Kiểm tra tính toàn vẹn của Phiếu nhập kho so với Yêu cầu nhập kho đã duyệt.
     * Được gọi cả khi Lưu phiếu nháp (isConfirming = false) và khi Xác nhận nhập kho (isConfirming = true).
     */
    public static void validateReceiptIntegrity(Connection conn, Map<String, Object> header,
            List<? extends Map<String, Object>> details, String id, boolean isConfirming)
            throws SQLException {
        // BƯỚC 1: Kiểm tra phiếu nhập kho bắt buộc phải tham chiếu tới một mã Yêu cầu nhập kho (maYCNK).
        Object ycnId = header.get("maYCNK");
        require(ycnId != null && !String.valueOf(ycnId).isBlank(),
                "Phiếu nhập kho phải gắn với Yêu cầu nhập kho đã được duyệt.");

        // BƯỚC 2: Khóa dòng YeuCauNhapKho gốc và kiểm tra trạng thái phải là "Đã duyệt".
        Map<String, Object> request = lock(conn, "YeuCauNhapKho", ycnId);
        require("Đã duyệt".equals(request.get("trangThai")), "Yêu cầu nhập kho (" + ycnId
                + ") phải ở trạng thái 'Đã duyệt' (trạng thái hiện tại: " + request.get("trangThai") + ").");

        // BƯỚC 3: Đảm bảo quan hệ 1-1 — một Yêu cầu nhập kho chỉ được lập tối đa một Phiếu nhập kho.
        require(!exists(conn,
                "SELECT 1 FROM dbo.PhieuNhapKho WITH (UPDLOCK,HOLDLOCK) WHERE maYCNK=? AND maPNK<>?", ycnId,
                id == null ? "" : id), "Yêu cầu nhập kho " + ycnId + " đã có phiếu nhập kho khác.");

        // BƯỚC 4: Kiểm tra Nhà cung cấp trên Phiếu nhập kho phải khớp với Nhà cung cấp của Yêu cầu đã duyệt.
        equal(header, request, "maNCC");

        // BƯỚC 5: Đọc danh sách các dòng ChiTietYeuCauNhapKho gốc để làm chuẩn đối chiếu mặt hàng và số lượng.
        List<LinkedHashMap<String, Object>> reqDetails = new dao.ChiTietYeuCauNhapKhoDAO().getAll(conn,
                Map.of("maYCNK", ycnId));
        require(!reqDetails.isEmpty(), "Yêu cầu nhập kho không có chi tiết hạng mục.");

        Map<String, Integer> reqParts = new LinkedHashMap<>();
        for (Map<String, Object> line : reqDetails) {
            reqParts.put(String.valueOf(line.get("maPhuTung")),
                    ((Number) line.get("soLuongYeuCau")).intValue());
        }

        // BƯỚC 6: Duyệt từng dòng chi tiết Phiếu nhập kho để kiểm tra mặt hàng và quy tắc số lượng:
        //         0 <= soLuongNhap (chấp nhận) <= soLuongThucNhan (giao thực tế) <= soLuongYeuCau (đặt mua).
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

        // BƯỚC 7: Đảm bảo tập hợp phụ tùng trên Phiếu nhập kho khớp 1:1 với Yêu cầu nhập kho (không thiếu/thừa dòng).
        require(seenParts.containsAll(reqParts.keySet()) && seenParts.size() == reqParts.size(),
                "Danh sách phụ tùng trong phiếu nhập kho phải khớp 1:1 với yêu cầu nhập kho đã duyệt (không được thêm, bớt hoặc thay đổi mặt hàng).");

        // BƯỚC 8: Nếu đang xác nhận nhập kho thực tế, tổng số lượng thực nhận của cả phiếu phải > 0.
        if (isConfirming) {
            require(totalActual > 0,
                    "Tổng số lượng thực nhận của toàn bộ phiếu phải lớn hơn 0 để có thể xác nhận nhập kho.");
        }
    }

    /**
     * NGHIỆP VỤ 3 (Chuẩn bị lập phiếu nhập kho từ yêu cầu đã duyệt):
     * Kiểm tra phân quyền (KHO/ADMIN), trạng thái "Đã duyệt" của YCNK, đảm bảo YCNK chưa có PNK nào và trả về Map đầu phiếu YeuCauNhapKho (trên GUI nút "Lập phiếu nhập kho" tại ThaoTacNghiepVu trực tiếp đọc YCNK + ChiTietYeuCauNhapKho).
     */
    public static Map<String, Object> prepareReceiptFromRequest(String ycnId, gui.VaiTro role,
            String employeeId) throws SQLException {
        // BƯỚC 1: Kiểm tra quyền thực hiện — chỉ Nhân viên kho hoặc Quản trị viên mới được lập phiếu nhập.
        if (role == null || (role != gui.VaiTro.ADMIN && role != gui.VaiTro.KHO)) {
            throw new SecurityException(
                    "Chỉ Nhân viên kho hoặc Quản trị viên mới có quyền lập phiếu nhận hàng thực tế.");
        }
        if (employeeId == null || employeeId.isBlank()) {
            throw new SecurityException("Mã nhân viên nhận hàng không hợp lệ.");
        }
        // BƯỚC 2: Kiểm tra nhân viên đang làm việc, trạng thái YCNK là "Đã duyệt" và chưa tồn tại PNK gắn với YCNK này.
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

    /**
     * NGHIỆP VỤ 3 (Xác nhận nhập kho thực tế - UC009):
     * - Chỉ Nhân viên kho hoặc Quản trị viên thực hiện trên phiếu đang "Chờ xác nhận nhập kho".
     * - CHỈ TẠI BƯỚC NÀY tồn kho (dbo.TonKho.soLuongHienTai) mới được cộng tăng theo soLuongNhap.
     * - Tự động gọi TonKhoDAO.fulfillShortages() để bù hàng cho các dòng ChiTietPhieuBaoGia / ChiTietPhieuSuaChua đang "Thiếu phụ tùng".
     * - Chuyển trạng thái PhieuNhapKho -> "Đã nhập kho" và YeuCauNhapKho -> "Đã xử lý".
     */
    public static void confirmActualStockIn(String id, gui.VaiTro role, String employeeId)
            throws SQLException {
        // BƯỚC 1: Kiểm tra phân quyền — chỉ Nhân viên kho hoặc Quản trị viên mới có quyền xác nhận nhập kho.
        if (role == null || (role != gui.VaiTro.ADMIN && role != gui.VaiTro.KHO)) {
            throw new SecurityException(
                    "Chỉ Nhân viên kho hoặc Quản trị viên mới có quyền xác nhận nhập kho thực tế.");
        }
        if (employeeId == null || employeeId.isBlank()) {
            throw new SecurityException("Mã nhân viên xác nhận không hợp lệ.");
        }
        // BƯỚC 2: Mở giao dịch SQL (nếu bất kỳ bước nào lỗi sẽ tự động rollback toàn bộ).
        transaction(conn -> {
            // BƯỚC 2.1: Kiểm tra nhân viên xác nhận đang hoạt động và khóa dòng PhieuNhapKho.
            requireActiveEmployee(conn, employeeId, "xác nhận nhập kho");
            Map<String, Object> pnk = lock(conn, "PhieuNhapKho", id);
            require("Chờ xác nhận nhập kho".equals(pnk.get("trangThai")),
                    "Phiếu nhập kho không còn ở trạng thái Chờ xác nhận nhập kho (trạng thái hiện tại: "
                            + pnk.get("trangThai") + ").");
            editable(conn, "PhieuNhapKho", pnk);

            // BƯỚC 2.2: Kiểm tra phiếu nhập kho phải gắn với mã Yêu cầu nhập kho hợp lệ (không phải phiếu lịch sử).
            Object ycnId = pnk.get("maYCNK");
            require(ycnId != null && !String.valueOf(ycnId).isBlank(),
                    "Phiếu nhập kho không gắn với Yêu cầu nhập kho hợp lệ (phiếu lịch sử). Không thể xác nhận nhập kho.");

            // BƯỚC 2.3: Đọc toàn bộ chi tiết phiếu nhập kho và kiểm tra tính toàn vẹn với YCNK đã duyệt.
            List<LinkedHashMap<String, Object>> details = new dao.ChiTietPhieuNhapKhoDAO().getAll(conn,
                    Map.of("maPNK", id));
            require(!details.isEmpty(), "Phiếu nhập chưa có chi tiết.");

            // Kiểm tra toàn vẹn chặt chẽ trước khi tăng tồn kho và đóng phiếu
            validateReceiptIntegrity(conn, pnk, details, id, true);

            // BƯỚC 2.4: Duyệt từng dòng phụ tùng để cộng số lượng chấp nhận (soLuongNhap) vào bảng dbo.TonKho,
            //           đồng thời gọi TonKhoDAO.fulfillShortages() để tự động bù hàng cho các phiếu đang thiếu phụ tùng.
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

            // BƯỚC 2.5: Cập nhật trạng thái PhieuNhapKho thành "Đã nhập kho" và YeuCauNhapKho thành "Đã xử lý".
            execute(conn, "UPDATE dbo.PhieuNhapKho SET trangThai=N'Đã nhập kho' WHERE maPNK=?", id);
            execute(conn, "UPDATE dbo.YeuCauNhapKho SET trangThai=N'Đã xử lý' WHERE maYCNK=?", ycnId);
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
