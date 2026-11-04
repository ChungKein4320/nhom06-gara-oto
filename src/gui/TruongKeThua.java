package gui;

import java.awt.*;
import dao.ChonDAO;

import connectDB.KetNoiCSDL;
import java.sql.*;
import java.util.*;
import javax.swing.*;

// ===== GUI: TRƯỜNG DỮ LIỆU KẾ THỪA =====

/** Tự điền hồ sơ từ phiếu nguồn; không tạo thêm cột trong database. */
final class TruongKeThua {
    // ===== THÀNH PHẦN GIAO DIỆN =====
    record Link(String field, String table, String... inherited) {
    }

    // ===== PHƯƠNG THỨC: link(String) — Xử lý link. =====
    static Link link(String table) {
        return switch (table) {
        case "PhieuKiemTra" -> new Link("maPTN", "PhieuTiepNhan", "maKhachHang", "VIN");
        case "PhieuBaoGia" -> new Link("maPKT", "PhieuKiemTra", "maKhachHang", "VIN");
        case "PhieuSuaChua" -> new Link("maBaoGia", "PhieuBaoGia", "maKhachHang", "VIN");
        case "HoaDon", "LichBaoHanh", "LichNhacBaoDuong" ->
            new Link("maPSC", "PhieuSuaChua", "maKhachHang", "VIN");
        case "PhieuNhapKho" -> new Link("maYCNK", "YeuCauNhapKho", "maNCC");
        default -> null;
        };
    }

    // ===== PHƯƠNG THỨC: bind(String, Map<String,JComponent>) — Gán giá trị tham số cho câu lệnh SQL. =====
    static Binding bind(String table, Map<String, JComponent> inputs) {
        Link link = link(table);
        if (link == null || !(inputs.get(link.field()) instanceof OChonThamChieu source))
            return null;
        Binding binding = new Binding(link, inputs, source);
        source.addActionListener(e -> binding.refresh());
        binding.refresh();
        return binding;
    }

    static final class Binding {
        final JLabel lblSummary = new JLabel();
        private final Link link;
        private final Map<String, JComponent> inputs;
        private final OChonThamChieu source;
        private String loadedId = "";
        private String problem;
        private boolean updating;

        // ===== KHỞI TẠO ĐỐI TƯỢNG: Binding(Link, Map<String,JComponent>, OChonThamChieu) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
        Binding(Link link, Map<String, JComponent> inputs, OChonThamChieu source) {
            this.link = link;
            this.inputs = inputs;
            this.source = source;
            lblSummary.setFont(GiaoDien.font(12, false));
            lblSummary.setForeground(GiaoDien.TEAL);
            for (String field : link.inherited()) {
                JComponent control = inputs.get(field);
                if (control != null) {
                    DinhDangKeThua.lock(control);
                    control.setToolTipText("Tự lấy từ " + nhanCot(link.field()).toLowerCase());
                }
            }
        }

        // ===== PHƯƠNG THỨC: refresh() — Tải lại dữ liệu và làm mới giao diện. =====
        void refresh() {
            if (updating)
                return;
            updating = true;
            String id = source.selectedId();
            loadedId = "";
            problem = null;
            for (String field : link.inherited())
                set(field, "");
            if (id.isEmpty()) {
                lblSummary.setText(link.table().equals("YeuCauNhapKho")
                        ? "Chọn yêu cầu nhập kho đã duyệt để tự điền nhà cung cấp."
                        : "Chọn phiếu nguồn để tự điền khách hàng và xe.");
                updating = false;
                return;
            }
            try (Connection conn = KetNoiCSDL.getConnection()) {
                Map<String, Object> origin = load(conn, link.table(), id);
                if (link.table().equals("YeuCauNhapKho")) {
                    String state = Objects.toString(origin.get("trangThai"), "");
                    if (!"Đã duyệt".equals(state)) {
                        throw new SQLException(
                                "Yêu cầu nhập kho " + id + " chưa được duyệt (trạng thái: " + state + ").");
                    }
                    for (String field : link.inherited())
                        set(field, Objects.toString(origin.get(field), ""));
                    String ncc = Objects.toString(origin.get("maNCC"), "");
                    String tenNCC = "";
                    try (PreparedStatement ps = conn
                            .prepareStatement("SELECT tenNCC FROM dbo.NhaCungCap WHERE maNCC=?")) {
                        ps.setString(1, ncc);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next())
                                tenNCC = rs.getString(1);
                        }
                    }
                    String info = "Yêu cầu: " + id + " | Nhà cung cấp: " + ncc
                            + (tenNCC.isEmpty() ? "" : " — " + tenNCC) + " | Trạng thái: " + state;
                    lblSummary.setText("<html>" + escape(info)
                            + "<br>Thông tin nhà cung cấp tự điền từ yêu cầu nhập kho đã duyệt.</html>");
                    loadedId = id;
                } else {
                    Map<String, Object> receipt = origin;
                    String current = link.table();
                    while (!current.equals("PhieuTiepNhan")) {
                        Link previous = link(current);
                        if (previous == null)
                            break;
                        receipt = load(conn, previous.table(), receipt.get(previous.field()));
                        current = previous.table();
                    }
                    for (String field : link.inherited())
                        set(field, Objects.toString(origin.getOrDefault(field, receipt.get(field)), ""));
                    JComponent employee = inputs.get("maNhanVien");
                    if (employee instanceof OChonThamChieu cmbEmployee && cmbEmployee.selectedId().isEmpty())
                        cmbEmployee.selectId(Objects.toString(origin.get("maNhanVien"), ""));
                    String customer = Objects.toString(receipt.get("maKhachHang"), "");
                    String vin = Objects.toString(receipt.get("VIN"), "");
                    String info = "Khách: " + customer + " | VIN: " + vin + " | ODO: "
                            + Objects.toString(receipt.get("soODO"), "—");
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT k.hoTenKH,v.bienSo,v.hangXe,v.dongXe FROM dbo.KhachHang k CROSS JOIN dbo.PhuongTien v WHERE k.maKhachHang=? AND v.VIN=?")) {
                        ps.setString(1, customer);
                        ps.setString(2, vin);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next())
                                info = "Khách: " + customer + " — " + rs.getString(1) + " | Xe: "
                                        + rs.getString(2) + " — " + rs.getString(3) + " " + rs.getString(4)
                                        + " | ODO: " + Objects.toString(receipt.get("soODO"), "—");
                        }
                    }
                    lblSummary.setText("<html>" + escape(info)
                            + "<br>Thông tin hồ sơ tự điền; nhân viên thực hiện có thể chọn riêng.</html>");
                    loadedId = id;
                }
            } catch (SQLException ex) {
                problem = "Không lấy được hồ sơ phiếu nguồn: " + ex.getMessage();
                for (String field : link.inherited())
                    set(field, "");
                lblSummary.setText("Không tải được hồ sơ. Vui lòng chọn lại phiếu nguồn.");
                lblSummary.setToolTipText(problem);
            } finally {
                updating = false;
            }
        }

        // ===== PHƯƠNG THỨC: validate() — Kiểm tra tính hợp lệ của nghiệp vụ tương ứng. =====
        void validate() throws ThongBaoLoiSQL.Issue {
            if (!source.selectedId().equals(loadedId))
                refresh();
            if (problem != null || loadedId.isEmpty())
                throw new ThongBaoLoiSQL.Issue(link.field(),
                        problem == null ? "Vui lòng chọn phiếu nguồn hợp lệ." : problem);
        }

        // ===== PHƯƠNG THỨC: set(String, String) — Gán nghiệp vụ tương ứng. =====
        private void set(String field, String value) {
            JComponent control = inputs.get(field);
            switch (control) {
            case OChonThamChieu cmbValue -> cmbValue.selectId(value);
            case JTextField txtValue -> txtValue.setText(value);
            case null, default -> {
            }
            }
        }
    }

    // ===== PHƯƠNG THỨC: load(Connection, String, Object) — Tải dữ liệu cho bảng hiện tại. =====
    private static Map<String, Object> load(Connection conn, String table, Object id) throws SQLException {
        var rows = ChonDAO.choBang(table).getAll(conn, Map.of(ChonDAO.khoaChinh(table), id));
        if (rows.isEmpty())
            throw new SQLException("Không tìm thấy " + table + ": " + id);
        return rows.get(0);
    }

    // ===== PHƯƠNG THỨC: escape(String) — Mã hóa ký tự đặc biệt trước khi ghép nội dung. =====
    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static final class GiaoDien {

        private static final Color TEAL = new Color(0x487674);

        // ===== PHƯƠNG THỨC: font(int, boolean) — Tạo phông chữ theo cỡ và kiểu chữ. =====
        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
        }
    }

    // ===== PHƯƠNG THỨC: nhanCot(String) — Tạo tiêu đề hiển thị cho cột bảng. =====
    private static String nhanCot(String field) {
        return switch (field) {
        case "khachHangHienThi" -> "Khách hàng";
        case "tongThanhToan" -> "Tổng thanh toán";
        case "tongDiemTichLuy" -> "Tổng điểm tích lũy";
        case "gioiTinh" -> "Giới tính";
        case "soKM" -> "Số km";
        case "ketQuaChuanDoan" -> "Kết quả chẩn đoán";
        case "maNhanVien" -> "Mã nhân viên";
        case "maNV" -> "Mã nhân viên";
        case "hoTenNV" -> "Họ tên nhân viên";
        case "ngaySinh" -> "Ngày sinh";
        case "soDienThoai" -> "Số điện thoại";
        case "email" -> "Email";
        case "diaChi" -> "Địa chỉ";
        case "vaiTro" -> "Vai trò";
        case "trangThai" -> "Trạng thái";
        case "maKhachHang" -> "Mã khách hàng";
        case "hoTenKH" -> "Họ tên khách hàng";
        case "bienSoXe" -> "Biển số xe";
        case "maNguoiDuyetHuy" -> "Nhân viên duyệt hủy";
        case "lyDoHuy" -> "Lý do hủy";
        case "VIN" -> "Số khung (VIN)";
        case "bienSo" -> "Biển số";
        case "hangXe" -> "Hãng xe";
        case "dongXe" -> "Dòng xe";
        case "mauXe" -> "Màu xe";
        case "namSanXuat" -> "Năm sản xuất";
        case "soMay" -> "Số máy";
        case "soKm" -> "Số km";
        case "maDichVu" -> "Mã dịch vụ";
        case "tenDichVu" -> "Tên dịch vụ";
        case "moTa" -> "Mô tả";
        case "donGia" -> "Đơn giá";
        case "maPhuTung" -> "Mã phụ tùng";
        case "tenPhuTung" -> "Tên phụ tùng";
        case "loaiPhuTung" -> "Loại phụ tùng";
        case "maNCC" -> "Mã nhà cung cấp";
        case "tenNCC" -> "Tên nhà cung cấp";
        case "maPhuongThuc" -> "Mã phương thức thanh toán";
        case "tenPhuongThuc" -> "Tên phương thức thanh toán";
        case "maGiamGia" -> "Mã giảm giá";
        case "mucGiam" -> "Mức giảm";
        case "ngayTao" -> "Ngày tạo";
        case "ngayHetHan" -> "Ngày hết hạn";
        case "maTaiKhoan" -> "Mã tài khoản";
        case "tenDangNhap" -> "Tên đăng nhập";
        case "matKhau" -> "Mật khẩu";
        case "maTonKho" -> "Mã tồn kho";
        case "soLuongHienTai" -> "Số lượng hiện tại";
        case "soLuongToiThieu" -> "Số lượng tối thiểu";
        case "maPTN" -> "Mã phiếu tiếp nhận";
        case "ngayTiepNhan" -> "Ngày tiếp nhận";
        case "soODO" -> "Số ODO";
        case "maPKT" -> "Mã phiếu kiểm tra";
        case "ngayKiemTra" -> "Ngày kiểm tra";
        case "maBaoGia" -> "Mã báo giá";
        case "ngayLap" -> "Ngày lập";
        case "thoiGianXacNhan" -> "Thời gian xác nhận";
        case "maPSC" -> "Mã phiếu sửa chữa";
        case "ngayHoanThanh" -> "Ngày hoàn thành";
        case "maYCNK" -> "Mã yêu cầu nhập kho";
        case "maNhanVienDuyet" -> "Nhân viên duyệt";
        case "ngayDuyet" -> "Ngày duyệt";
        case "soLuongYeuCau" -> "Số lượng yêu cầu";
        case "donGiaDuKien" -> "Đơn giá dự kiến";
        case "soLuongThucNhan" -> "Số lượng thực nhận";
        case "maPNK" -> "Mã phiếu nhập kho";
        case "ngayNhap" -> "Ngày nhập";
        case "yeuCauDichVu" -> "Yêu cầu dịch vụ";
        case "hangMucKiemTra" -> "Hạng mục kiểm tra";
        case "ketQuaChanDoan" -> "Kết quả chẩn đoán";
        case "ketQuaKiemTra" -> "Kết quả kiểm tra";
        case "soLuongPhuTung" -> "Số lượng phụ tùng";
        case "donGiaPhuTung" -> "Đơn giá phụ tùng";
        case "soLuongCapPhat" -> "Số lượng cấp phát";
        case "soLuongSuDung" -> "Số lượng sử dụng";
        case "soLuongHoanTra" -> "Số lượng hoàn trả";
        case "ngayCapPhat" -> "Ngày cấp phát";
        case "maNhanVienCapPhat" -> "Nhân viên cấp phát";
        case "ngayHoanTra" -> "Ngày hoàn trả";
        case "maNhanVienXacNhanHoanTra" -> "Nhân viên xác nhận hoàn trả";
        case "chiPhiNhanCong" -> "Chi phí nhân công";
        case "trangThaiHangMuc" -> "Trạng thái hạng mục";
        case "ketQuaNghiemThu" -> "Kết quả nghiệm thu";
        case "ngayBatDauBaoHanh" -> "Ngày bắt đầu bảo hành";
        case "thoiHanBaoHanh" -> "Thời hạn bảo hành";
        case "ngayHetHanBaoHanh" -> "Ngày hết hạn bảo hành";
        case "soLuongNhap" -> "Số lượng nhập";
        case "donGiaNhap" -> "Đơn giá nhập";
        case "maHoaDon" -> "Mã hóa đơn";
        case "tienPhuTung" -> "Tiền phụ tùng";
        case "tienCong" -> "Tiền công";
        case "dichVuKhac" -> "Dịch vụ khác";
        case "thueVAT" -> "Thuế VAT";
        case "giamGia" -> "Giảm giá";
        case "trangThaiThanhToan" -> "Trạng thái thanh toán";
        case "soLuong" -> "Số lượng";
        case "maLBH" -> "Mã lịch bảo hành";
        case "ngayXuLy" -> "Ngày xử lý";
        case "mocDiem" -> "Mốc điểm";
        case "ngayBatDauChuKy" -> "Ngày đầu chu kỳ";
        case "ketQuaXuLy" -> "Kết quả xử lý";
        case "ghiChu" -> "Ghi chú";
        case "maLichNhac" -> "Mã lịch nhắc";
        case "ngayNhac" -> "Ngày nhắc";
        case "noiDung" -> "Nội dung";
        case "maLichSuTD" -> "Mã lịch sử tích điểm";
        case "ngayTichDiem" -> "Ngày tích điểm";
        case "soDiem" -> "Số điểm";
        case "lyDo" -> "Lý do";
        default -> field;
        };
    }
}
