package dao;

import connectDB.KetNoiCSDL;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/** Mọi thay đổi trạng thái nghiệp vụ bắt buộc đi qua lớp này và nằm trong transaction. */
public final class QuyTrinhDAO {
    private QuyTrinhDAO() {}
    public interface Work<T> { T run(Connection conn) throws SQLException; }
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
            } catch (Throwable t) {
                try {
                    conn.rollback();
                    rolledBack = true;
                } catch (SQLException rbEx) {
                    t.addSuppressed(rbEx);
                }
                if (t instanceof SQLException sqlEx) throw sqlEx;
                if (t instanceof RuntimeException runEx) throw runEx;
                if (t instanceof Error err) throw err;
                throw new SQLException("Lỗi giao dịch", t);
            } finally {
                if (completed || rolledBack) {
                    try {
                        conn.setAutoCommit(originalAutoCommit);
                    } catch (SQLException ignored) { }
                }
            }
        }
    }
    static Map<String,Object> lock(Connection conn,String table,Object id) throws SQLException {
        String pk = Schema.PRIMARY.get(table);
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM dbo.[" + table + "] WITH (UPDLOCK,HOLDLOCK) WHERE [" + pk + "]=?")) {
            ps.setObject(1,id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Bản ghi " + table + " không tồn tại: " + id);
                Map<String,Object> row = new LinkedHashMap<>();
                for (String field : Schema.COLUMNS.get(table)) row.put(field,rs.getObject(field));
                return row;
            }
        }
    }
    static boolean exists(Connection conn,String sql,Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for(int i=0;i<params.length;i++) ps.setObject(i+1,params[i]);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
    public static void requireActiveEmployee(Connection conn, String employeeId, String roleDescription) throws SQLException {
        if (employeeId == null || employeeId.isBlank()) {
            throw new SecurityException("Mã nhân viên " + roleDescription + " không hợp lệ hoặc đang để trống.");
        }
        require(exists(conn, "SELECT 1 FROM dbo.NhanVien WHERE maNhanVien=? AND trangThai=N'Đang làm'", employeeId.trim()),
                "Nhân viên " + roleDescription + " (" + employeeId + ") không tồn tại hoặc đã nghỉ việc.");
    }
    static void require(boolean ok,String message) throws SQLException { if(!ok) throw new SQLException(message); }
    static void equal(Map<String,Object> a,Map<String,Object> b,String... fields) throws SQLException {
        for(String field:fields) require(Objects.equals(a.get(field),b.get(field)),"Hồ sơ không khớp: "+field);
    }
    private static void chronological(Map<String,Object> current,String field,Map<String,Object> previous,String before) throws SQLException {
        if(current.get(field) instanceof java.util.Date date && previous.get(before) instanceof java.util.Date start)
            require(!date.before(start),field+" không được trước "+before+" của phiếu trước.");
    }
    private static final Map<String,String> CHILDREN=Map.of(
        "PhieuTiepNhan","ChiTietPhieuTiepNhan","PhieuKiemTra","ChiTietPhieuKiemTra",
        "PhieuBaoGia","ChiTietPhieuBaoGia","PhieuSuaChua","ChiTietPhieuSuaChua",
        "YeuCauNhapKho","ChiTietYeuCauNhapKho",
        "PhieuNhapKho","ChiTietPhieuNhapKho","HoaDon","ChiTietHoaDon");
    public static String detailTable(String table) { return CHILDREN.get(table); }
    private static String parent(String child) { return CHILDREN.entrySet().stream().filter(e -> e.getValue().equals(child)).map(Map.Entry::getKey).findFirst().orElse(null); }
    public static String documentEditProblem(Connection conn,String table,String id) throws SQLException {
        if(table.equals("PhieuSuaChua")) return "Phiếu sửa chữa được kế thừa từ báo giá; dùng các thao tác cấp phát, nghiệm thu và bàn giao để cập nhật.";
        if(table.equals("HoaDon")) return "Hóa đơn đã lập được khóa nội dung; cập nhật mã giảm giá và thanh toán tại trang Hóa đơn.";
        Map<String,Object> row=lock(conn,table,id);
        try { editable(conn,table,row); return null; }
        catch(SQLException ex) { return ex.getMessage(); }
    }
    static void editable(Connection conn,String table,Map<String,Object> row) throws SQLException {
        String state=String.valueOf(row.get("trangThai")); Object id=row.get(Schema.PRIMARY.get(table));
        switch(table) {
            case "YeuCauNhapKho" -> require(state.equals("Chờ duyệt"),"Yêu cầu nhập kho đã xử lý; không được sửa.");
            case "PhieuBaoGia" -> require(state.equals("Còn hiệu lực") && row.get("thoiGianXacNhan")==null,"Báo giá đã khóa; không được sửa.");
            case "PhieuNhapKho" -> require(state.equals("Chờ xác nhận nhập kho"),"Phiếu nhập đã xử lý; không được sửa hoặc nhập kho lần nữa.");
            case "PhieuSuaChua" -> require(state.equals("Chờ sửa chữa"),"Chỉ sửa nội dung phiếu trước khi bắt đầu sửa chữa.");
            case "HoaDon" -> throw new SQLException("Hóa đơn được lập từ phiếu sửa chữa; dùng nghiệp vụ thanh toán/giảm giá.");
            case "PhieuTiepNhan" -> require(!exists(conn,"SELECT 1 FROM dbo.PhieuKiemTra WHERE maPTN=?",id),"Phiếu tiếp nhận đã có kiểm tra, không được sửa.");
            case "PhieuKiemTra" -> require(!exists(conn,"SELECT 1 FROM dbo.PhieuBaoGia WHERE maPKT=?",id),"Phiếu kiểm tra đã có báo giá, không được sửa.");
            default -> { }
        }
    }
    /** Chặn sửa trạng thái trực tiếp và sửa nội dung sau khi đã được sử dụng. */
    static void guardWrite(Connection conn,String table,Map<String,Object> values,Map<String,Object> key) throws SQLException {
        Map<String,Object> row=new LinkedHashMap<>();
        if(key!=null && Schema.KEYS.get(table).length==1) {
            row.putAll(lock(conn,table,key.get(Schema.PRIMARY.get(table))));
            editable(conn,table,row);
            if(Set.of("PhieuBaoGia","PhieuSuaChua","PhieuNhapKho","YeuCauNhapKho").contains(table)) {
                for(String field:List.of("trangThai","ngayHoanThanh","thoiGianXacNhan","ngayDuyet")) if(values.containsKey(field)) require(Objects.equals(row.get(field),values.get(field)),"Dùng nút nghiệp vụ để thay đổi trạng thái/ngày hoàn thành/ngày duyệt.");
            }
        }
        if(key!=null) row.putAll(key);
        row.putAll(values);
        String owner=parent(table);
        if(owner!=null) {
            String fk=Schema.PRIMARY.get(owner); Object ownerId=row.get(fk);
            if(ownerId==null && key!=null) ownerId=key.get(fk);
            Map<String,Object> header=lock(conn,owner,ownerId); editable(conn,owner,header);
            if(table.equals("ChiTietPhieuSuaChua")) {
                require(key==null,"Không sửa trực tiếp hạng mục sửa chữa; dùng nghiệp vụ cấp phát/tiến độ.");
                String initialState=String.valueOf(row.get("trangThaiHangMuc"));
                boolean noPart="PT000".equals(row.get("maPhuTung"));
                require((noPart && "Đang sửa chữa".equals(initialState)) || (!noPart && Set.of("Chờ sửa chữa","Thiếu phụ tùng").contains(initialState)),"Trạng thái hạng mục ban đầu không hợp lệ.");
                require("Không đạt".equals(row.get("ketQuaNghiemThu")),"Nghiệm thu phải qua nghiệp vụ xác nhận.");
                require(row.get("ngayBatDauBaoHanh")==null && row.get("ngayHetHanBaoHanh")==null,"Ngày bảo hành được tính khi nghiệm thu.");
            }
            return;
        }
        if(key==null) {
            if(table.equals("YeuCauNhapKho")) require("Chờ duyệt".equals(row.get("trangThai")),"Yêu cầu nhập kho mới phải chờ duyệt.");
            if(table.equals("PhieuBaoGia")) require("Còn hiệu lực".equals(row.get("trangThai")) && row.get("thoiGianXacNhan")==null,"Báo giá mới phải còn hiệu lực, chưa xác nhận.");
            if(table.equals("PhieuNhapKho")) require("Chờ xác nhận nhập kho".equals(row.get("trangThai")),"Phiếu nhập mới phải ở trạng thái Chờ xác nhận nhập kho.");
            if(table.equals("PhieuSuaChua")) require("Chờ sửa chữa".equals(row.get("trangThai")) && row.get("ngayHoanThanh")==null,"Phiếu sửa mới phải chờ sửa chữa.");
        }
        
        if (!table.equals("NhanVien") && row.get("maNhanVien") != null) {
            require(
                exists(conn,
                    "SELECT 1 FROM dbo.NhanVien "
                    + "WHERE maNhanVien=? AND trangThai=N'Đang làm'",
                    row.get("maNhanVien")),
                "Nhân viên không còn đang làm."
            );
        }

        switch(table) {
            case "PhieuKiemTra" -> {
                Map<String,Object> receipt=lock(conn,"PhieuTiepNhan",row.get("maPTN"));
                equal(row,receipt,"maKhachHang","VIN"); chronological(row,"ngayKiemTra",receipt,"ngayTiepNhan");
            }
            case "PhieuBaoGia" -> {
                Map<String,Object> check=lock(conn,"PhieuKiemTra",row.get("maPKT"));
                equal(row,check,"maKhachHang","VIN"); chronological(row,"ngayLap",check,"ngayKiemTra");
            }
            case "PhieuSuaChua" -> {
                Map<String,Object> quote=lock(conn,"PhieuBaoGia",row.get("maBaoGia"));
                require("Đã xác nhận".equals(quote.get("trangThai")),"Báo giá chưa được xác nhận.");
                equal(row,quote,"VIN"); equal(row,lock(conn,"PhieuKiemTra",quote.get("maPKT")),"maKhachHang");
                chronological(row,"ngayLap",quote,"thoiGianXacNhan");
                require(!exists(conn,"SELECT 1 FROM dbo.PhieuSuaChua WHERE maBaoGia=? AND maPSC<>? AND trangThai<>N'Đã hủy'",row.get("maBaoGia"),key==null ? "" : key.get("maPSC")),"Báo giá đã có phiếu sửa chữa.");
            }
            case "HoaDon","LichBaoHanh","LichNhacBaoDuong" -> {
                if(table.equals("HoaDon") && row.get("maPSC")==null) {
                    require(row.get("VIN")==null,"Hóa đơn bán phụ tùng riêng không gắn xe.");
                    break;
                }
                Map<String,Object> repair=lock(conn,"PhieuSuaChua",row.get("maPSC")); equal(row,repair,"maKhachHang","VIN");
                if(table.equals("HoaDon")) chronological(row,"ngayLap",repair,"ngayHoanThanh");
                if(table.equals("LichNhacBaoDuong")) {
                    require(Set.of("Đã sửa xong","Chờ thanh toán","Đã thanh toán","Hoàn tất").contains(String.valueOf(repair.get("trangThai"))),"Chỉ lập lịch bảo dưỡng sau khi sửa xong.");
                    chronological(row,"ngayNhac",repair,"ngayHoanThanh");
                }
                if(table.equals("LichBaoHanh")) require(row.get("maDichVu")!=null && row.get("maPhuTung")!=null && exists(conn,"SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=? AND maDichVu=? AND maPhuTung=? AND ketQuaNghiemThu=N'Đạt' AND ngayBatDauBaoHanh<=? AND ngayHetHanBaoHanh>=?",row.get("maPSC"),row.get("maDichVu"),row.get("maPhuTung"),row.get("ngayXuLy"),row.get("ngayXuLy")),"Chọn đúng hạng mục đang còn bảo hành tại ngày xử lý.");
            }
            case "LichSuTichDiem" -> {
                Map<String,Object> invoice=lock(conn,"HoaDon",row.get("maHoaDon")); equal(row,invoice,"maKhachHang");
                require("Đã thanh toán".equals(invoice.get("trangThaiThanhToan")),"Chỉ tích điểm cho hóa đơn đã thanh toán.");
                require(invoice.get("maPSC")!=null,"Chỉ hóa đơn sửa chữa/dịch vụ được tích điểm.");
                BigDecimal total=((BigDecimal)invoice.get("tienPhuTung")).add((BigDecimal)invoice.get("tienCong")).add((BigDecimal)invoice.get("dichVuKhac")).add((BigDecimal)invoice.get("thueVAT"));
                require(((Number)row.get("soDiem")).intValue()==InvoiceMath.points(total),"Số điểm tính trên tổng hóa đơn sau VAT, trước giảm giá: 100.000đ = 1 điểm.");
                chronological(row,"ngayTichDiem",invoice,"ngayLap");
                require(!exists(conn,"SELECT 1 FROM dbo.LichSuTichDiem WHERE maHoaDon=?",row.get("maHoaDon")),"Hóa đơn đã được tích điểm.");
            }
            default -> { }
        }
    }
    /** UC009: Kiểm tra toàn vẹn phiếu nhập kho gắn với đơn duyệt và số lượng hợp lệ. */
    public static void validateReceiptIntegrity(Connection conn, Map<String,Object> header, List<? extends Map<String,Object>> details, String id, boolean isConfirming) throws SQLException {
        Object ycnId = header.get("maYCNK");
        require(ycnId != null && !String.valueOf(ycnId).isBlank(), "Phiếu nhập kho phải gắn với Yêu cầu nhập kho đã được duyệt.");

        Map<String,Object> request = lock(conn, "YeuCauNhapKho", ycnId);
        require("Đã duyệt".equals(request.get("trangThai")),
                "Yêu cầu nhập kho (" + ycnId + ") phải ở trạng thái 'Đã duyệt' (trạng thái hiện tại: " + request.get("trangThai") + ").");

        // Một yêu cầu nhập kho chỉ có duy nhất một phiếu nhập kho
        require(!exists(conn, "SELECT 1 FROM dbo.PhieuNhapKho WITH (UPDLOCK,HOLDLOCK) WHERE maYCNK=? AND maPNK<>?", ycnId, id == null ? "" : id),
                "Yêu cầu nhập kho " + ycnId + " đã có phiếu nhập kho khác.");

        // Nhà cung cấp trên phiếu phải trùng với nhà cung cấp được duyệt
        equal(header, request, "maNCC");

        // Chi tiết yêu cầu nhập kho gốc
        List<LinkedHashMap<String,Object>> reqDetails = new CoSoDAO("ChiTietYeuCauNhapKho").getAll(conn, Map.of("maYCNK", ycnId));
        require(!reqDetails.isEmpty(), "Yêu cầu nhập kho không có chi tiết hạng mục.");

        Map<String, Integer> reqParts = new LinkedHashMap<>();
        for (Map<String,Object> line : reqDetails) {
            reqParts.put(String.valueOf(line.get("maPhuTung")), ((Number)line.get("soLuongYeuCau")).intValue());
        }

        require(details != null && !details.isEmpty(), "Phiếu nhập kho phải có ít nhất một hạng mục chi tiết.");
        Set<String> seenParts = new HashSet<>();
        int totalActual = 0;

        for (Map<String,Object> detail : details) {
            String part = String.valueOf(detail.get("maPhuTung"));
            require(!"PT000".equals(part), "Mã phụ tùng không hợp lệ (PT000).");
            require(seenParts.add(part), "Trùng lặp mặt hàng trong phiếu nhập kho: " + part);
            require(reqParts.containsKey(part), "Mặt hàng " + part + " không nằm trong yêu cầu nhập kho đã duyệt.");

            int expectedReqQty = reqParts.get(part);
            require(detail.get("soLuongYeuCau") != null && ((Number)detail.get("soLuongYeuCau")).intValue() == expectedReqQty,
                    "Số lượng yêu cầu của phụ tùng " + part + " (" + detail.get("soLuongYeuCau") + ") không khớp với đơn duyệt (" + expectedReqQty + ").");

            Object rawActual = detail.get("soLuongThucNhan");
            Object rawAccepted = detail.get("soLuongNhap");

            require(rawActual != null, "Phụ tùng " + part + " chưa ghi nhận số lượng thực nhận. Vui lòng kiểm đếm và nhập số lượng.");
            require(rawAccepted != null, "Phụ tùng " + part + " chưa ghi nhận số lượng nhập kho chấp nhận. Vui lòng kiểm tra và nhập số lượng.");

            int actualQty;
            int acceptedQty;
            if (rawActual instanceof Number num) actualQty = num.intValue();
            else if (rawActual instanceof String s && !s.trim().isEmpty()) actualQty = Integer.parseInt(s.trim());
            else throw new SQLException("Phụ tùng " + part + " chưa ghi nhận số lượng thực nhận hợp lệ.");

            if (rawAccepted instanceof Number num) acceptedQty = num.intValue();
            else if (rawAccepted instanceof String s && !s.trim().isEmpty()) acceptedQty = Integer.parseInt(s.trim());
            else throw new SQLException("Phụ tùng " + part + " chưa ghi nhận số lượng chấp nhận nhập kho hợp lệ.");

            require(actualQty >= 0, "Số lượng thực nhận không được âm: " + part);
            require(acceptedQty >= 0, "Số lượng chấp nhận không được âm: " + part);
            require(actualQty <= expectedReqQty, "Số lượng thực nhận (" + actualQty + ") không được vượt quá số lượng yêu cầu (" + expectedReqQty + ") cho phụ tùng " + part + ".");
            require(acceptedQty <= actualQty, "Số lượng chấp nhận (" + acceptedQty + ") không được vượt quá số lượng thực nhận (" + actualQty + ") cho phụ tùng " + part + ".");

            totalActual += actualQty;
        }

        require(seenParts.containsAll(reqParts.keySet()) && seenParts.size() == reqParts.size(),
                "Danh sách phụ tùng trong phiếu nhập kho phải khớp 1:1 với yêu cầu nhập kho đã duyệt (không được thêm, bớt hoặc thay đổi mặt hàng).");

        if (isConfirming) {
            require(totalActual > 0, "Tổng số lượng thực nhận của toàn bộ phiếu phải lớn hơn 0 để có thể xác nhận nhập kho.");
        }
    }

    public static String saveDocument(String table, Map<String, Object> header, List<Map<String, Object>> details, String id) throws SQLException {
        if (table.equals("YeuCauNhapKho") || table.equals("PhieuNhapKho")) {
            throw new SecurityException("Thao tác lưu " + table + " yêu cầu xác thực vai trò và mã nhân viên.");
        }
        return saveDocument(table, header, details, id, null, null);
    }

    public static String saveDocument(String table, Map<String, Object> header, List<Map<String, Object>> details, String id, gui.VaiTro role, String employeeId) throws SQLException {
        if (table.equals("YeuCauNhapKho")) {
            if (role == null) {
                throw new SecurityException("Thao tác lưu Yêu cầu nhập kho yêu cầu xác thực vai trò.");
            }
            if (role == gui.VaiTro.QUAN_LY) {
                throw new SecurityException("Quản lý chỉ có quyền duyệt hoặc từ chối yêu cầu nhập kho, không được tạo hoặc sửa nội dung yêu cầu.");
            }
            if (role != gui.VaiTro.KHO && role != gui.VaiTro.ADMIN) {
                throw new SecurityException("Chỉ Nhân viên kho hoặc Quản trị viên mới có quyền tạo/sửa yêu cầu nhập kho.");
            }
            if (employeeId == null || employeeId.isBlank()) {
                throw new SecurityException("Mã nhân viên người tạo không hợp lệ.");
            }
        } else if (table.equals("PhieuNhapKho")) {
            if (role == null) {
                throw new SecurityException("Thao tác lưu Phiếu nhập kho yêu cầu xác thực vai trò.");
            }
            if (role == gui.VaiTro.QUAN_LY) {
                throw new SecurityException("Quản lý không có quyền tạo hoặc chỉnh sửa phiếu nhập kho thực tế.");
            }
            if (role != gui.VaiTro.KHO && role != gui.VaiTro.ADMIN) {
                throw new SecurityException("Chỉ Nhân viên kho hoặc Quản trị viên mới có quyền tạo/sửa phiếu nhập kho.");
            }
            if (employeeId == null || employeeId.isBlank()) {
                throw new SecurityException("Mã nhân viên thực hiện không hợp lệ.");
            }
        }

        return transaction(conn -> {
            CoSoDAO dao = new CoSoDAO(table);
            String child = detailTable(table);
            String fk = Schema.PRIMARY.get(table);
            require(child != null && !Set.of("HoaDon", "PhieuSuaChua").contains(table), "Loại phiếu không được lập bằng biểu mẫu này.");
            require(!details.isEmpty(), "Phiếu phải có ít nhất một hạng mục chi tiết.");

            if (table.equals("YeuCauNhapKho")) {
                requireActiveEmployee(conn, employeeId, "người tạo");
                if (id == null) {
                    header.put("maNhanVien", employeeId.trim());
                    header.put("trangThai", "Chờ duyệt");
                } else {
                    Map<String, Object> current = lock(conn, "YeuCauNhapKho", id);
                    require("Chờ duyệt".equals(current.get("trangThai")),
                            "Yêu cầu nhập kho ở trạng thái '" + current.get("trangThai") + "'; chỉ được sửa khi đang 'Chờ duyệt'.");
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
                            "Phiếu nhập kho ở trạng thái '" + current.get("trangThai") + "'; chỉ được chỉnh sửa bản nháp ('Chờ xác nhận nhập kho').");
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
    public record IntakeResult(String maPTN,String maKhachHang,String VIN) { }
    public static IntakeResult createIntake(Map<String,Object> header,boolean createCustomer,String customerId,
            Map<String,Object> customer,boolean createVehicle,String vehicleId,Map<String,Object> vehicle,
            List<Map<String,Object>> details) throws SQLException {
        return transaction(conn -> {
            require(details!=null && !details.isEmpty(),"Phiếu tiếp nhận phải có ít nhất một yêu cầu dịch vụ.");
            String resolvedCustomer=customerId;
            if(createCustomer) {
                require(customer!=null,"Vui lòng nhập hồ sơ khách hàng mới.");
                resolvedCustomer=new CoSoDAO("KhachHang").insert(conn,customer);
            } else {
                lock(conn,"KhachHang",resolvedCustomer);
            }

            BigDecimal odometer=new BigDecimal(String.valueOf(header.get("soODO")));
            String resolvedVin=vehicleId;
            if(createVehicle) {
                require(vehicle!=null,"Vui lòng nhập hồ sơ phương tiện mới.");
                Map<String,Object> vehicleRow=new LinkedHashMap<>(vehicle);
                vehicleRow.put("soKM",odometer);
                resolvedVin=new CoSoDAO("PhuongTien").insert(conn,vehicleRow);
            } else {
                Map<String,Object> currentVehicle=lock(conn,"PhuongTien",resolvedVin);
                BigDecimal currentOdometer=(BigDecimal)currentVehicle.get("soKM");
                require(odometer.compareTo(currentOdometer)>=0,"Số ODO tiếp nhận không được thấp hơn số km đang lưu của phương tiện.");
                if(odometer.compareTo(currentOdometer)>0)
                    execute(conn,"UPDATE dbo.PhuongTien SET soKM=? WHERE VIN=?",odometer,resolvedVin);
            }

            Map<String,Object> receipt=new LinkedHashMap<>(header);
            receipt.put("maKhachHang",resolvedCustomer);
            receipt.put("VIN",resolvedVin);
            guardWrite(conn,"PhieuTiepNhan",receipt,null);
            String receiptId=new CoSoDAO("PhieuTiepNhan").insert(conn,receipt);
            insertDetails(conn,"ChiTietPhieuTiepNhan","maPTN",receiptId,details);
            return new IntakeResult(receiptId,resolvedCustomer,resolvedVin);
        });
    }
  
    private static void insertDetails(Connection conn,String child,String fk,String id,List<Map<String,Object>> details) throws SQLException {
        CoSoDAO dao=new CoSoDAO(child);
        for(Map<String,Object> detail:details) { Map<String,Object> row=new LinkedHashMap<>(detail); row.put(fk,id); dao.insert(conn,row); }
    }
    public static void confirmQuote(String id) throws SQLException {
        transaction(conn -> {
            Map<String,Object> quote=lock(conn,"PhieuBaoGia",id); editable(conn,"PhieuBaoGia",quote);
            require(exists(conn,"SELECT 1 FROM dbo.PhieuBaoGia WHERE maBaoGia=? AND ngayHetHan>=SYSDATETIME()",id),"Báo giá đã hết hạn.");
            require(exists(conn,"SELECT 1 FROM dbo.ChiTietPhieuBaoGia WHERE maBaoGia=?",id),"Báo giá chưa có chi tiết.");
            List<LinkedHashMap<String,Object>> lines=new CoSoDAO("ChiTietPhieuBaoGia").getAll(conn,Map.of("maBaoGia",id));
            for(Map<String,Object> line:lines) {
                String part=String.valueOf(line.get("maPhuTung"));
                int qty=((Number)line.get("soLuongPhuTung")).intValue();
                boolean available="PT000".equals(part) || (qty==0) || execute(conn,"UPDATE dbo.TonKho SET soLuongHienTai=soLuongHienTai-? WHERE maPhuTung=? AND soLuongHienTai>=?",qty,part,qty)==1;
                execute(conn,"UPDATE dbo.ChiTietPhieuBaoGia SET trangThai=? WHERE maBaoGia=? AND maDichVu=? AND maPhuTung=?",available?"Chờ sửa chữa":"Thiếu phụ tùng",id,line.get("maDichVu"),part);
            }
            execute(conn,"UPDATE dbo.PhieuBaoGia SET trangThai=N'Đã xác nhận',thoiGianXacNhan=? WHERE maBaoGia=?",now(conn),id); return null;
        });
    }
    public static String createRepair(String quoteId,String employee) throws SQLException {
        return transaction(conn -> {
            Map<String,Object> quote=lock(conn,"PhieuBaoGia",quoteId);
            require("Đã xác nhận".equals(quote.get("trangThai")),"Hãy xác nhận báo giá trước.");
            Map<String,Object> check=lock(conn,"PhieuKiemTra",quote.get("maPKT"));
            Map<String,Object> header=new LinkedHashMap<>();
            header.put("maBaoGia",quoteId); header.put("maKhachHang",check.get("maKhachHang")); header.put("VIN",quote.get("VIN"));
            header.put("maNhanVien",employee); header.put("ngayLap",now(conn)); header.put("ngayHoanThanh",null); header.put("trangThai","Chờ sửa chữa");
            List<LinkedHashMap<String,Object>> quoted=new CoSoDAO("ChiTietPhieuBaoGia").getAll(conn,Map.of("maBaoGia",quoteId));
            require(!quoted.isEmpty(),"Báo giá chưa có chi tiết.");
            String id=new CoSoDAO("PhieuSuaChua").insert(conn,header);
            for(Map<String,Object> line:quoted) {
                String part = String.valueOf(line.get("maPhuTung"));
                int quantity = ((Number) line.get("soLuongPhuTung")).intValue();
                String quoteState = String.valueOf(line.get("trangThai"));

                String itemState;

                if ("PT000".equals(part)) {
                    // Hạng mục không dùng phụ tùng, không cần cấp phát.
                    itemState = "Đang sửa chữa";
                } else {
                    require(
                            quantity > 0,
                            "Số lượng phụ tùng phải lớn hơn 0."
                    );

                    require(
                            Set.of("Chờ sửa chữa", "Thiếu phụ tùng").contains(quoteState),
                            "Trạng thái chi tiết báo giá không hợp lệ."
                    );

                    // Chờ sửa chữa nghĩa là đã giữ hàng khi xác nhận báo giá.
                    boolean reserved = "Chờ sửa chữa".equals(quoteState);

                    // Nếu trước đó thiếu, thử giữ hàng bằng lượng tồn hiện tại.
                    if (!reserved) {
                        reserved = execute(
                                conn,
                                "UPDATE dbo.TonKho "
                                        + "SET soLuongHienTai=soLuongHienTai-? "
                                        + "WHERE maPhuTung=? AND soLuongHienTai>=?",
                                quantity, part, quantity
                        ) == 1;

                        if (reserved) {
                            execute(
                                    conn,
                                    "UPDATE dbo.ChiTietPhieuBaoGia "
                                            + "SET trangThai=N'Chờ sửa chữa' "
                                            + "WHERE maBaoGia=? AND maDichVu=? "
                                            + "AND maPhuTung=?",
                                    quoteId, line.get("maDichVu"), part
                            );
                        }
                    }

                    itemState = reserved ? "Chờ sửa chữa" : "Thiếu phụ tùng";
                }
                Map<String,Object> detail=new LinkedHashMap<>();
                detail.put("maPSC",id); detail.put("maDichVu",line.get("maDichVu")); detail.put("maPhuTung",line.get("maPhuTung"));
                detail.put("soLuongPhuTung",line.get("soLuongPhuTung"));
                detail.put("trangThaiHangMuc", itemState);
                detail.put("ketQuaNghiemThu","Không đạt"); detail.put("thoiHanBaoHanh",0);
                new CoSoDAO("ChiTietPhieuSuaChua").insert(conn,detail);
            }
            return id;
        });
    }

    /** UC009: Quản lý hoặc Admin duyệt hoặc từ chối yêu cầu nhập kho. Tuyệt đối KHÔNG thay đổi tồn kho. */
    public static void approvePurchaseRequest(String id, boolean approve, gui.VaiTro role, String employeeId) throws SQLException {
        if (role == null || (role != gui.VaiTro.ADMIN && role != gui.VaiTro.QUAN_LY)) {
            throw new SecurityException("Chỉ Quản lý hoặc Quản trị viên mới có quyền duyệt/từ chối yêu cầu nhập kho.");
        }
        if (employeeId == null || employeeId.isBlank()) {
            throw new SecurityException("Mã nhân viên phê duyệt không hợp lệ.");
        }
        transaction(conn -> {
            requireActiveEmployee(conn, employeeId, "phê duyệt");
            Map<String,Object> row = lock(conn, "YeuCauNhapKho", id);
            require("Chờ duyệt".equals(row.get("trangThai")), "Yêu cầu nhập kho đã xử lý (trạng thái: " + row.get("trangThai") + "); không thể duyệt lại.");
            List<LinkedHashMap<String,Object>> details = new CoSoDAO("ChiTietYeuCauNhapKho").getAll(conn, Map.of("maYCNK", id));
            require(!details.isEmpty(), "Yêu cầu nhập kho chưa có chi tiết hạng mục.");
            String newState = approve ? "Đã duyệt" : "Từ chối";
            Timestamp now = now(conn);
            execute(conn, "UPDATE dbo.YeuCauNhapKho SET trangThai=?, maNhanVienDuyet=?, ngayDuyet=? WHERE maYCNK=?",
                    newState, employeeId.trim(), now, id);
            return null;
        });
    }

    /** UC009: Nhân viên kho hoặc Admin chuẩn bị dữ liệu lập phiếu nhập kho thực tế từ yêu cầu đã duyệt. */
    public static Map<String,Object> prepareReceiptFromRequest(String ycnId, gui.VaiTro role, String employeeId) throws SQLException {
        if (role == null || (role != gui.VaiTro.ADMIN && role != gui.VaiTro.KHO)) {
            throw new SecurityException("Chỉ Nhân viên kho hoặc Quản trị viên mới có quyền lập phiếu nhận hàng thực tế.");
        }
        if (employeeId == null || employeeId.isBlank()) {
            throw new SecurityException("Mã nhân viên nhận hàng không hợp lệ.");
        }
        try (Connection conn = KetNoiCSDL.getConnection()) {
            requireActiveEmployee(conn, employeeId, "nhận hàng");
            Map<String,Object> ycn = lock(conn, "YeuCauNhapKho", ycnId);
            require("Đã duyệt".equals(ycn.get("trangThai")), "Chỉ lập phiếu nhập kho cho yêu cầu đã được duyệt.");
            require(!exists(conn, "SELECT 1 FROM dbo.PhieuNhapKho WHERE maYCNK=?", ycnId),
                    "Yêu cầu nhập kho " + ycnId + " đã có phiếu nhập kho. Không thể tạo thêm phiếu mới.");
            return ycn;
        }
    }

    /** UC009: Nhân viên kho hoặc Admin xác nhận nhập kho thực tế. CHỈ TẠI BƯỚC NÀY TỒN KHO MỚI TĂNG theo số lượng chấp nhận. */
    public static void confirmActualStockIn(String id, gui.VaiTro role, String employeeId) throws SQLException {
        if (role == null || (role != gui.VaiTro.ADMIN && role != gui.VaiTro.KHO)) {
            throw new SecurityException("Chỉ Nhân viên kho hoặc Quản trị viên mới có quyền xác nhận nhập kho thực tế.");
        }
        if (employeeId == null || employeeId.isBlank()) {
            throw new SecurityException("Mã nhân viên xác nhận không hợp lệ.");
        }
        transaction(conn -> {
            requireActiveEmployee(conn, employeeId, "xác nhận nhập kho");
            Map<String,Object> pnk = lock(conn, "PhieuNhapKho", id);
            require("Chờ xác nhận nhập kho".equals(pnk.get("trangThai")), "Phiếu nhập kho không còn ở trạng thái Chờ xác nhận nhập kho (trạng thái hiện tại: " + pnk.get("trangThai") + ").");
            editable(conn, "PhieuNhapKho", pnk);

            Object ycnId = pnk.get("maYCNK");
            require(ycnId != null && !String.valueOf(ycnId).isBlank(),
                    "Phiếu nhập kho không gắn với Yêu cầu nhập kho hợp lệ (phiếu lịch sử). Không thể xác nhận nhập kho.");

            List<LinkedHashMap<String,Object>> details = new CoSoDAO("ChiTietPhieuNhapKho").getAll(conn, Map.of("maPNK", id));
            require(!details.isEmpty(), "Phiếu nhập chưa có chi tiết.");

            // Kiểm tra toàn vẹn chặt chẽ trước khi tăng tồn kho và đóng phiếu
            validateReceiptIntegrity(conn, pnk, details, id, true);

            for (Map<String,Object> row : details) {
                Object part = row.get("maPhuTung");
                int accepted = ((Number)row.get("soLuongNhap")).intValue();
                if (accepted > 0) {
                    if (exists(conn, "SELECT 1 FROM dbo.TonKho WITH (UPDLOCK,HOLDLOCK) WHERE maPhuTung=?", part)) {
                        execute(conn, "UPDATE dbo.TonKho SET soLuongHienTai=soLuongHienTai+? WHERE maPhuTung=?", accepted, part);
                    } else {
                        new CoSoDAO("TonKho").insert(conn, Map.of("maPhuTung", part, "soLuongHienTai", accepted, "soLuongToiThieu", 0));
                    }
                    fulfillShortages(conn, String.valueOf(part));
                }
            }

            execute(conn, "UPDATE dbo.PhieuNhapKho SET trangThai=N'Đã nhập kho' WHERE maPNK=?", id);
            execute(conn, "UPDATE dbo.YeuCauNhapKho SET trangThai=N'Đã xử lý' WHERE maYCNK=?", ycnId);
            return null;
        });
    }
    public static void startRepair(String id) throws SQLException {
        transaction(conn -> {
            Map<String,Object> repair=lock(conn,"PhieuSuaChua",id);
            require("Chờ sửa chữa".equals(repair.get("trangThai")),"Phiếu không còn chờ sửa chữa.");
            require(exists(conn,"SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=?",id),"Phiếu chưa có hạng mục.");
            require(!exists(conn,"SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=? AND trangThaiHangMuc<>N'Đang sửa chữa'",id),"Cần cấp phát đủ phụ tùng cho mọi hạng mục trước khi bắt đầu.");
            execute(conn,"UPDATE dbo.PhieuSuaChua SET trangThai=N'Đang sửa chữa' WHERE maPSC=?",id); return null;
        });
    }
    public static void completeRepairItem(
        String id, String service, String part, int months
        ) throws SQLException {
            require(months >= 0, "Thời hạn bảo hành không được âm.");

            transaction(conn -> {
                Map<String, Object> repair = lock(conn, "PhieuSuaChua", id);

                require(
                        "Đang sửa chữa".equals(repair.get("trangThai")),
                        "Chỉ xác nhận hạng mục của phiếu đang sửa chữa."
                );

                Map<String, Object> line = one(
                        conn,
                        "SELECT * FROM dbo.ChiTietPhieuSuaChua "
                                + "WITH (UPDLOCK,HOLDLOCK) "
                                + "WHERE maPSC=? AND maDichVu=? AND maPhuTung=?",
                        id, service, part
                );

                require(
                        "Đang sửa chữa".equals(line.get("trangThaiHangMuc")),
                        "Hạng mục chưa cấp phát hoặc đã hoàn thành."
                );

                int issued = ((Number) line.get("soLuongCapPhat")).intValue();
                int returned = ((Number) line.get("soLuongHoanTra")).intValue();

                require(
                        issued >= 0 && returned >= 0 && returned <= issued,
                        "Số lượng cấp phát hoặc hoàn trả không hợp lệ."
                );

                boolean noPart = "PT000".equals(part);

                require(
                        !noPart || (issued == 0 && returned == 0),
                        "Hạng mục không dùng phụ tùng phải có số lượng bằng 0."
                );

                require(
                        noPart || issued > 0,
                        "Hạng mục chưa được ghi nhận cấp phát phụ tùng."
                );

                int actualUsed = noPart ? 0 : issued - returned;
                int actualMonths = noPart ? 0 : months;

                int updated = execute(
                        conn,
                        "UPDATE dbo.ChiTietPhieuSuaChua "
                                + "SET soLuongSuDung=?, "
                                + "trangThaiHangMuc=N'Hoàn thành', "
                                + "thoiHanBaoHanh=?, "
                                + "ketQuaNghiemThu=N'Không đạt', "
                                + "ngayBatDauBaoHanh=NULL, "
                                + "ngayHetHanBaoHanh=NULL "
                                + "WHERE maPSC=? AND maDichVu=? AND maPhuTung=? "
                                + "AND trangThaiHangMuc=N'Đang sửa chữa'",
                        actualUsed, actualMonths, id, service, part
                );

                require(updated == 1, "Không thể xác nhận hạng mục.");
                return null;
            });
        }
    public static void finishRepair(String id) throws SQLException {
        acceptRepair(id);
    }
    public static void acceptRepair(String id) throws SQLException {
        transaction(conn -> {
            String state=String.valueOf(lock(conn,"PhieuSuaChua",id).get("trangThai"));
            require("Đang sửa chữa".equals(state) || "Đã sửa xong".equals(state),"Chỉ nghiệm thu phiếu đang sửa chữa hoặc phiếu đã sửa xong chưa nghiệm thu.");
            require(exists(conn,"SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=?",id) && !exists(conn,"SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=? AND trangThaiHangMuc<>N'Hoàn thành'",id),"Còn hạng mục chưa hoàn thành.");
            require(exists(conn,"SELECT 1 FROM dbo.ChiTietPhieuSuaChua WHERE maPSC=? AND ketQuaNghiemThu<>N'Đạt'",id),"Phiếu đã nghiệm thu đạt, không được bắt đầu lại thời hạn bảo hành.");
            Timestamp start=now(conn);
            execute(conn,"UPDATE dbo.ChiTietPhieuSuaChua SET ketQuaNghiemThu=N'Đạt',ngayBatDauBaoHanh=CASE WHEN thoiHanBaoHanh>0 THEN ? ELSE NULL END,ngayHetHanBaoHanh=CASE WHEN thoiHanBaoHanh>0 THEN DATEADD(month,thoiHanBaoHanh,?) ELSE NULL END WHERE maPSC=?",start,start,id);
            if("Đang sửa chữa".equals(state)) execute(conn,"UPDATE dbo.PhieuSuaChua SET trangThai=N'Đã sửa xong',ngayHoanThanh=? WHERE maPSC=?",start,id);
            return null;
        });
    }
    public static String createInvoice(String repairId,String employee,String method,String coupon) throws SQLException {
        return transaction(conn -> {
            // Khóa ngoại bắt buộc: giá trị dự kiến sẽ được thay bằng phương thức thực tế khi thu tiền.
            String initialMethod=method;
            if(initialMethod==null || initialMethod.isBlank()) initialMethod=String.valueOf(one(conn,"SELECT TOP (1) maPhuongThuc FROM dbo.PhuongThucThanhToan ORDER BY CASE WHEN tenPhuongThuc=N'Tiền mặt' THEN 0 ELSE 1 END,maPhuongThuc").get("maPhuongThuc"));
            Map<String,Object> repair=lock(conn,"PhieuSuaChua",repairId);
            require("Đã sửa xong".equals(repair.get("trangThai")),"Chỉ lập hóa đơn sau nghiệm thu.");
            require(!exists(conn,"SELECT 1 FROM dbo.HoaDon WHERE maPSC=?",repairId),"Phiếu sửa đã có hóa đơn.");
            List<LinkedHashMap<String,Object>> details=new CoSoDAO("ChiTietPhieuSuaChua").getAll(conn,Map.of("maPSC",repairId));
            require(!details.isEmpty(),"Phiếu sửa không có chi tiết.");
            BigDecimal parts=BigDecimal.ZERO,labor=BigDecimal.ZERO;
            List<Map<String,Object>> lines=new ArrayList<>(); Map<Object,BigDecimal> serviceFees=new LinkedHashMap<>();
            for(Map<String,Object> line:details) {
                require("Hoàn thành".equals(line.get("trangThaiHangMuc")) && "Đạt".equals(line.get("ketQuaNghiemThu")),"Còn hạng mục chưa nghiệm thu đạt.");
                Map<String,Object> price=one(conn,"SELECT donGiaPhuTung,chiPhiNhanCong FROM dbo.ChiTietPhieuBaoGia WHERE maBaoGia=? AND maDichVu=? AND maPhuTung=?",repair.get("maBaoGia"),line.get("maDichVu"),line.get("maPhuTung"));
                BigDecimal unit=(BigDecimal)price.get("donGiaPhuTung"); int quantity=((Number)line.get("soLuongPhuTung")).intValue();
                parts=parts.add(unit.multiply(BigDecimal.valueOf(quantity)));
                if(!"PT000".equals(line.get("maPhuTung"))) {
                    lines.add(Map.of("maDichVu",line.get("maDichVu"),"maPhuTung",line.get("maPhuTung"),"soLuong",quantity,"donGia",unit));
                }
                serviceFees.merge(line.get("maDichVu"),(BigDecimal)price.get("chiPhiNhanCong"),BigDecimal::add);
            }
            for(var fee:serviceFees.entrySet()) { labor=labor.add(fee.getValue()); lines.add(Map.of("maDichVu",fee.getKey(),"soLuong",1,"donGia",fee.getValue())); }
            BigDecimal subtotal=parts.add(labor), vat=InvoiceMath.vat(subtotal), discount=BigDecimal.ZERO;
            if(coupon!=null && !coupon.isBlank()) discount=couponDiscount(conn,coupon,repair.get("maKhachHang"),subtotal);
            Map<String,Object> header=new LinkedHashMap<>(); header.put("maGiamGia",coupon==null || coupon.isBlank() ? null : coupon);
            header.put("maKhachHang",repair.get("maKhachHang")); header.put("maNhanVien",employee); header.put("maPSC",repairId); header.put("VIN",repair.get("VIN"));
            header.put("ngayLap",now(conn)); header.put("tienPhuTung",parts); header.put("tienCong",labor); header.put("dichVuKhac",BigDecimal.ZERO); header.put("thueVAT",vat); header.put("giamGia",discount); header.put("trangThaiThanhToan","Chưa thanh toán"); header.put("maPhuongThuc",initialMethod);
            String id=new CoSoDAO("HoaDon").insert(conn,header);
            for(Map<String,Object> line:lines) {
                Map<String,Object> values=new LinkedHashMap<>(line); values.put("maHoaDon",id);
                // Hóa đơn được khóa và tạo ngay trong cùng giao dịch.
                insertInvoiceLine(conn,values);
            }
            execute(conn,"UPDATE dbo.PhieuSuaChua SET trangThai=N'Chờ thanh toán' WHERE maPSC=?",repairId); return id;
        });
    }
    static void insertInvoiceLine(Connection conn,Map<String,Object> row) throws SQLException {
        DataRules.validate("ChiTietHoaDon",row,true);
        execute(conn,"INSERT INTO dbo.ChiTietHoaDon(maHoaDon,maDichVu,maPhuTung,soLuong,donGia) VALUES(?,?,?,?,?)",row.get("maHoaDon"),row.get("maDichVu"),row.get("maPhuTung"),row.get("soLuong"),row.get("donGia"));
    }
    static BigDecimal couponDiscount(Connection conn,String coupon,Object customer,BigDecimal subtotal) throws SQLException {
        Map<String,Object> value=lock(conn,"MaGiamGia",coupon);
        require(Objects.equals(value.get("maKhachHang"),customer),"Mã giảm giá không thuộc khách hàng này.");
        require("Đang hiệu lực".equals(value.get("trangThai")) && exists(conn,"SELECT 1 FROM dbo.MaGiamGia WHERE maGiamGia=? AND ngayTao<=SYSDATETIME() AND ngayHetHan>=SYSDATETIME()",coupon),"Mã giảm giá không còn hiệu lực.");
        require(!exists(conn,"SELECT 1 FROM dbo.HoaDon WHERE maGiamGia=? AND trangThaiThanhToan=N'Đã thanh toán'",coupon),"Mã giảm giá đã được sử dụng.");
        return InvoiceMath.discount(subtotal,(BigDecimal)value.get("mucGiam"));
    }
    static Timestamp now(Connection conn) throws SQLException {
        Timestamp serverNow=(Timestamp)one(conn,"SELECT SYSDATETIME() AS serverNow").get("serverNow");
        return truncateToSecond(serverNow);
    }
    static Timestamp truncateToSecond(Timestamp time) {
        return Timestamp.valueOf(time.toLocalDateTime().withNano(0));
    }
    public static void markReminder(String id) throws SQLException {
        transaction(conn -> { lock(conn,"LichNhacBaoDuong",id); require(execute(conn,"UPDATE dbo.LichNhacBaoDuong SET trangThai=N'Đã gửi' WHERE maLichNhac=? AND trangThai=N'Chưa gửi'",id)==1,"Lịch nhắc đã gửi hoặc có trạng thái khác."); return null; });
    }
    public static void handover(String id) throws SQLException {
        transaction(conn -> { lock(conn,"PhieuSuaChua",id); require(execute(conn,"UPDATE dbo.PhieuSuaChua SET trangThai=N'Hoàn tất' WHERE maPSC=? AND trangThai=N'Đã thanh toán' AND EXISTS(SELECT 1 FROM dbo.HoaDon WHERE maPSC=? AND trangThaiThanhToan=N'Đã thanh toán')",id,id)==1,"Chỉ bàn giao sau khi đã thanh toán."); return null; });
    }

    public static void cancelRepair(String id) throws SQLException {
        cancelRepair(id,"",null);
    }
    public static void cancelRepair(String id,String reason,String managerEmployee) throws SQLException {
        transaction(conn -> {
            Map<String,Object> repair=lock(conn,"PhieuSuaChua",id);
            require(Set.of("Chờ sửa chữa","Đang sửa chữa").contains(String.valueOf(repair.get("trangThai"))),"Chỉ hủy phiếu trước nghiệm thu.");
            require(reason!=null && !reason.isBlank(),"Bắt buộc ghi lý do hủy phiếu.");
            require(managerEmployee!=null && exists(conn,"SELECT 1 FROM dbo.NhanVien WHERE maNhanVien=? AND trangThai=N'Đang làm' AND vaiTro IN (N'Nhân viên quản lý',N'Quản trị viên')",managerEmployee),"Chọn người duyệt hủy là quản lý hoặc quản trị viên đang làm việc.");
            require(!exists(conn,"SELECT 1 FROM dbo.HoaDon WHERE maPSC=?",id),"Phiếu đã có hóa đơn, không thể hủy.");
            List<LinkedHashMap<String,Object>> lines=new CoSoDAO("ChiTietPhieuSuaChua").getAll(conn,Map.of("maPSC",id));
            for(Map<String,Object> line:lines) if(!"PT000".equals(line.get("maPhuTung"))) {
                int issued=((Number)line.get("soLuongCapPhat")).intValue();
                int used=((Number)line.get("soLuongSuDung")).intValue();
                int returned=((Number)line.get("soLuongHoanTra")).intValue();
                require(issued==0 || used+returned==issued,"Cần ghi nhận số lượng đã sử dụng và xác nhận hàng trả thực tế trước khi hủy.");
                Map<String,Object> quoteLine=one(conn,"SELECT trangThai FROM dbo.ChiTietPhieuBaoGia WHERE maBaoGia=? AND maDichVu=? AND maPhuTung=?",repair.get("maBaoGia"),line.get("maDichVu"),line.get("maPhuTung"));
                int reserved="Chờ sửa chữa".equals(quoteLine.get("trangThai")) ? ((Number)line.get("soLuongPhuTung")).intValue()-issued : 0;
                if(reserved>0) {
                    require(exists(conn,"SELECT 1 FROM dbo.TonKho WHERE maPhuTung=?",line.get("maPhuTung")),"Không tìm thấy tồn kho để hoàn phần chưa cấp phát.");
                    execute(conn,"UPDATE dbo.TonKho SET soLuongHienTai=soLuongHienTai+? WHERE maPhuTung=?",reserved,line.get("maPhuTung"));
                }
            }
            execute(conn,"UPDATE dbo.PhieuSuaChua SET trangThai=N'Đã hủy',lyDoHuy=?,maNguoiDuyetHuy=? WHERE maPSC=?",reason.trim(),managerEmployee,id);
            execute(conn,"UPDATE dbo.PhieuBaoGia SET trangThai=N'Hết hiệu lực' WHERE maBaoGia=? AND trangThai=N'Đã xác nhận'",repair.get("maBaoGia"));
            return null;
        });
    }
    static Map<String,Object> one(Connection conn,String sql,Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for(int i=0;i<params.length;i++) ps.setObject(i+1,params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                if(!rs.next()) throw new SQLException("Không tìm thấy dữ liệu: " + sql);
                Map<String,Object> row = new LinkedHashMap<>();
                for(int i=1;i<=rs.getMetaData().getColumnCount();i++) row.put(rs.getMetaData().getColumnLabel(i),rs.getObject(i));
                return row;
            }
        }
    }
    static int execute(Connection conn,String sql,Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for(int i=0;i<params.length;i++) ps.setObject(i+1,params[i]);
            return ps.executeUpdate();
        }
    }
    public static void recordPartReturn(String repairId,String service,String part,int quantity,String warehouseEmployee) throws SQLException {
        require(quantity>0,"Số lượng trả kho phải lớn hơn 0.");
        transaction(conn -> {
            Map<String,Object> repair=lock(conn,"PhieuSuaChua",repairId);
            require(Set.of("Chờ sửa chữa","Đang sửa chữa").contains(String.valueOf(repair.get("trangThai"))),"Chỉ ghi nhận hoàn trả trước khi nghiệm thu.");
            require(exists(conn,"SELECT 1 FROM dbo.NhanVien WHERE maNhanVien=? AND trangThai=N'Đang làm' AND vaiTro IN (N'Nhân viên kho',N'Nhân viên quản lý',N'Quản trị viên')",warehouseEmployee),"Chỉ nhân viên kho/quản lý đang làm mới được ghi nhận hàng trả.");
            Map<String,Object> line=one(conn,"SELECT * FROM dbo.ChiTietPhieuSuaChua WITH (UPDLOCK,HOLDLOCK) WHERE maPSC=? AND maDichVu=? AND maPhuTung=?",repairId,service,part);
            int issued=((Number)line.get("soLuongCapPhat")).intValue(), used=((Number)line.get("soLuongSuDung")).intValue(), returned=((Number)line.get("soLuongHoanTra")).intValue();
            require(!"PT000".equals(part) && issued>0 && returned+used+quantity<=issued,"Số lượng trả vượt số phụ tùng đã cấp phát còn lại.");
            require(execute(conn,"UPDATE dbo.TonKho SET soLuongHienTai=soLuongHienTai+? WHERE maPhuTung=?",quantity,part)==1,"Không tìm thấy tồn kho để ghi nhận trả phụ tùng.");
            require(execute(conn,"UPDATE dbo.ChiTietPhieuSuaChua SET soLuongHoanTra=soLuongHoanTra+?,ngayHoanTra=?,maNhanVienXacNhanHoanTra=? WHERE maPSC=? AND maDichVu=? AND maPhuTung=?",quantity,now(conn),warehouseEmployee,repairId,service,part)==1,"Không thể cập nhật đối soát phụ tùng trả kho.");
            return null;
        });
    }

    public static void refreshRepairShortages() throws SQLException {
        transaction(conn -> {
            List<Map<String, Object>> candidates = new ArrayList<>();

            String sql =
                    "SELECT ct.maPSC, ct.maDichVu, ct.maPhuTung "
                    + "FROM dbo.ChiTietPhieuSuaChua ct "
                    + "JOIN dbo.PhieuSuaChua p ON p.maPSC=ct.maPSC "
                    + "WHERE ct.trangThaiHangMuc=N'Thiếu phụ tùng' "
                    + "AND ct.maPhuTung<>'PT000' "
                    + "AND ct.soLuongCapPhat=0 "
                    + "AND p.trangThai IN (N'Chờ sửa chữa', N'Đang sửa chữa') "
                    + "ORDER BY p.ngayLap, ct.maPSC, ct.maDichVu, ct.maPhuTung";

            try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    candidates.add(Map.of(
                            "maPSC", rs.getString("maPSC"),
                            "maDichVu", rs.getString("maDichVu"),
                            "maPhuTung", rs.getString("maPhuTung")
                    ));
                }
            }

            for (Map<String, Object> candidate : candidates) {
                Object repairId = candidate.get("maPSC");
                Object service = candidate.get("maDichVu");
                Object part = candidate.get("maPhuTung");

                // Khóa phiếu và đọc lại để tránh dùng trạng thái đã cũ.
                Map<String, Object> repair = lock(
                        conn, "PhieuSuaChua", repairId
                );

                if (!Set.of("Chờ sửa chữa", "Đang sửa chữa")
                        .contains(String.valueOf(repair.get("trangThai")))) {
                    continue;
                }

                Map<String, Object> item = one(
                        conn,
                        "SELECT * FROM dbo.ChiTietPhieuSuaChua "
                                + "WITH (UPDLOCK,HOLDLOCK) "
                                + "WHERE maPSC=? AND maDichVu=? AND maPhuTung=?",
                        repairId, service, part
                );

                if (!"Thiếu phụ tùng".equals(item.get("trangThaiHangMuc"))
                        || ((Number) item.get("soLuongCapPhat")).intValue() != 0) {
                    continue;
                }

                Object quoteId = repair.get("maBaoGia");

                Map<String, Object> quoteItem = one(
                        conn,
                        "SELECT trangThai, soLuongPhuTung "
                                + "FROM dbo.ChiTietPhieuBaoGia "
                                + "WITH (UPDLOCK,HOLDLOCK) "
                                + "WHERE maBaoGia=? AND maDichVu=? AND maPhuTung=?",
                        quoteId, service, part
                );

                int quantity = ((Number) item.get("soLuongPhuTung")).intValue();
                int quotedQuantity =
                        ((Number) quoteItem.get("soLuongPhuTung")).intValue();

                require(
                        quantity > 0 && quantity == quotedQuantity,
                        "Số lượng phiếu sửa chữa không khớp báo giá: "
                                + repairId + " / " + service + " / " + part
                );

                String quoteState = String.valueOf(quoteItem.get("trangThai"));

                if ("Thiếu phụ tùng".equals(quoteState)) {
                    // Kiểm tra đủ tồn và giữ hàng trong cùng một câu lệnh.
                    int reserved = execute(
                            conn,
                            "UPDATE dbo.TonKho "
                                    + "SET soLuongHienTai=soLuongHienTai-? "
                                    + "WHERE maPhuTung=? AND soLuongHienTai>=?",
                            quantity, part, quantity
                    );

                    if (reserved == 0) continue;

                    execute(
                            conn,
                            "UPDATE dbo.ChiTietPhieuBaoGia "
                                    + "SET trangThai=N'Chờ sửa chữa' "
                                    + "WHERE maBaoGia=? AND maDichVu=? AND maPhuTung=?",
                            quoteId, service, part
                    );
                } else if (!"Chờ sửa chữa".equals(quoteState)) {
                    // Không tự suy đoán việc giữ hàng ở trạng thái khác.
                    continue;
                }

                // Báo giá đã Chờ sửa chữa nghĩa là đã giữ hàng:
                // chỉ đồng bộ hạng mục, không trừ tồn thêm lần nữa.
                execute(
                        conn,
                        "UPDATE dbo.ChiTietPhieuSuaChua "
                                + "SET trangThaiHangMuc=N'Chờ sửa chữa' "
                                + "WHERE maPSC=? AND maDichVu=? AND maPhuTung=? "
                                + "AND trangThaiHangMuc=N'Thiếu phụ tùng' "
                                + "AND soLuongCapPhat=0",
                        repairId, service, part
                );
            }

            return null;
        });
    }

    private static void fulfillShortages(Connection conn,String part) throws SQLException {
        List<Map<String,Object>> shortages=new ArrayList<>();
        try(PreparedStatement ps=conn.prepareStatement("SELECT d.maBaoGia,d.maDichVu,d.soLuongPhuTung FROM dbo.ChiTietPhieuBaoGia d WITH (UPDLOCK,ROWLOCK) JOIN dbo.PhieuBaoGia h ON h.maBaoGia=d.maBaoGia WHERE d.maPhuTung=? AND d.trangThai=N'Thiếu phụ tùng' AND h.trangThai=N'Đã xác nhận' ORDER BY h.thoiGianXacNhan,d.maBaoGia")) {
            ps.setString(1,part); try(ResultSet rs=ps.executeQuery()) { while(rs.next()) shortages.add(Map.of("maBaoGia",rs.getString(1),"maDichVu",rs.getString(2),"soLuong",rs.getInt(3))); }
        }
        for(Map<String,Object> row:shortages) {
            int qty=((Number)row.get("soLuong")).intValue();
            int updated=execute(conn,"UPDATE dbo.TonKho SET soLuongHienTai=soLuongHienTai-? WHERE maPhuTung=? AND soLuongHienTai>=?",qty,part,qty);
            if (updated == 0) continue;
            String quote=String.valueOf(row.get("maBaoGia"));
            execute(conn,"UPDATE dbo.ChiTietPhieuBaoGia SET trangThai=N'Chờ sửa chữa' WHERE maBaoGia=? AND maDichVu=? AND maPhuTung=?",quote,row.get("maDichVu"),part);
            execute(conn,"UPDATE s SET trangThaiHangMuc=N'Chờ sửa chữa' FROM dbo.ChiTietPhieuSuaChua s JOIN dbo.PhieuSuaChua h ON h.maPSC=s.maPSC WHERE h.maBaoGia=? AND s.maDichVu=? AND s.maPhuTung=? AND s.trangThaiHangMuc=N'Thiếu phụ tùng'",quote,row.get("maDichVu"),part);
        }
    }
}
