package gui;

import connectDB.KetNoiCSDL;
import dao.ChonDAO;
import java.awt.*;
import java.sql.*;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

// ===== GUI: Ô CHỌN DỮ LIỆU THAM CHIẾU =====

/** Danh sách khóa ngoại: hiển thị mã và mô tả, giá trị lưu chỉ là mã. */
final class OChonThamChieu extends OChonTimKiem<OChonThamChieu.MucThamChieu> {
    // ===== THÀNH PHẦN GIAO DIỆN =====
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private final String owner;
    private final String field;
    private final String target;
    private final String baseQuery;
    private String query;
    private String[] queryParams = new String[0];
    private String preservedId;
    private boolean refreshing;

    // ===== PHƯƠNG THỨC: fireActionEvent() — Gửi sự kiện thao tác đến các bộ lắng nghe. =====
    @Override
    protected void fireActionEvent() {
        if (!refreshing)
            super.fireActionEvent();
    }

    // ===== PHƯƠNG THỨC: targetTable(String, String) — Xác định bảng đích theo loại đối tượng. =====
    static String targetTable(String owner, String field) {
        if (field.equals(ChonDAO.khoaChinh(owner)))
            return null;
        for (Map.Entry<String, String> entry : ChonDAO.khoaTheoBang().entrySet()) {
            if (!entry.getKey().startsWith("ChiTiet") && entry.getValue().equals(field))
                return entry.getKey();
        }
        return null;
    }

    // ===== PHƯƠNG THỨC: create(String, String) — Tạo bản ghi hoặc thành phần từ dữ liệu đầu vào. =====
    static OChonThamChieu create(String owner, String field) {
        return create(owner, field, null);
    }

    // ===== PHƯƠNG THỨC: create(String, String, String) — Tạo bản ghi hoặc thành phần từ dữ liệu đầu vào. =====
    static OChonThamChieu create(String owner, String field, String initialValue) {
        String target = targetTable(owner, field);
        return target == null ? null : new OChonThamChieu(owner, field, target, initialValue);
    }
    // ===== KHỞI TẠO VÀ XỬ LÝ GIAO DIỆN =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: OChonThamChieu(String, String, String, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    private OChonThamChieu(String owner, String field, String target, String initialValue) {
        this.owner = owner;
        this.field = field;
        this.target = target;
        this.preservedId = (initialValue != null && !initialValue.isBlank()) ? initialValue.trim() : null;
        String primary = ChonDAO.khoaChinh(target);
        String description = switch (target) {
        case "NhanVien" -> "hoTenNV";
        case "KhachHang" -> "hoTenKH";
        case "PhuongTien" -> "CONCAT(bienSo, N' — ', hangXe, N' ', dongXe)";
        case "DichVu" -> "tenDichVu";
        case "PhuTung" -> "tenPhuTung";
        case "NhaCungCap" -> "tenNCC";
        case "PhuongThucThanhToan" -> "tenPhuongThuc";
        case "PhieuTiepNhan" ->
            "CONCAT(maKhachHang, N' — ', VIN, N' — ', CONVERT(nvarchar(10),ngayTiepNhan,103))";
        case "PhieuKiemTra" ->
            "CONCAT(maKhachHang, N' — ', VIN, N' — ', CONVERT(nvarchar(10),ngayKiemTra,103))";
        case "PhieuBaoGia", "PhieuSuaChua" ->
            "CONCAT(VIN, N' — ', CONVERT(nvarchar(10),ngayLap,103), N' — ', trangThai)";
        case "YeuCauNhapKho" -> "CONCAT(maNCC, N' — ', CONVERT(nvarchar(10),ngayTao,103), N' — ', trangThai)";
        case "HoaDon" ->
            "CONCAT(maKhachHang, N' — ', CONVERT(nvarchar(10),ngayLap,103), N' — ', trangThaiThanhToan)";
        case "MaGiamGia" -> "CONCAT(maKhachHang, N' — ', mucGiam, N' — ', trangThai)";
        default -> primary;
        };
        String filter = "LichBaoHanh".equals(owner) && "maPSC".equals(field)
                ? " WHERE [trangThai]=N'Hoàn tất'"
                : "";
        if ("PhuTung".equals(target) && Set
                .of("TonKho", "ChiTietPhieuNhapKho", "ChiTietYeuCauNhapKho", "ChiTietHoaDon").contains(owner))
            filter = " WHERE [maPhuTung]<>'PT000'";
        baseQuery = "SELECT [" + primary + "], CONCAT([" + primary + "], N' — ', " + description
                + ") FROM dbo.[" + target + "]" + filter + " ORDER BY [" + primary + "]";
        query = baseQuery;
        setFont(GiaoDien.font(12, false));
        setEditable(true);
        setMaximumRowCount(10);
        setPrototypeDisplayValue(new MucThamChieu("", "Mã — Thông tin lựa chọn"));
        try {
            refresh();
        } catch (SQLException ex) {
            setToolTipText("Không tải được danh sách: " + ex.getMessage());
        }
        addPopupMenuListener(new PopupMenuListener() {
            // ===== PHƯƠNG THỨC: popupMenuWillBecomeVisible(PopupMenuEvent) — Chuẩn bị nội dung trước khi menu ngữ cảnh hiện. =====
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent event) {
                try {
                    refresh();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(OChonThamChieu.this,
                            "Không tải được " + nhanCot(field).toLowerCase() + ": " + ex.getMessage(),
                            "Lỗi tải danh sách", JOptionPane.ERROR_MESSAGE);
                }
            }

            // ===== PHƯƠNG THỨC: popupMenuWillBecomeInvisible(PopupMenuEvent) — Cập nhật giao diện khi menu ngữ cảnh ẩn. =====
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent event) {
            }

            // ===== PHƯƠNG THỨC: popupMenuCanceled(PopupMenuEvent) — Khôi phục trạng thái giao diện khi đóng menu ngữ cảnh. =====
            @Override
            public void popupMenuCanceled(PopupMenuEvent event) {
            }
        });
    }

    // ===== PHƯƠNG THỨC: shouldFilterHidden() — Kiểm tra danh sách có cần loại bản ghi đã ẩn. =====
    private boolean shouldFilterHidden() {
        return ("YeuCauNhapKho".equals(owner) && "maNCC".equals(field))
                || ("ChiTietYeuCauNhapKho".equals(owner) && "maPhuTung".equals(field));
    }

    // ===== PHƯƠNG THỨC: refresh() — Tải lại dữ liệu và làm mới giao diện. =====
    void refresh() throws SQLException {
        String previous = selectedId();
        refreshing = true;
        try {
            napDanhSachThamChieu();
            insertItemAt(new MucThamChieu("", "— Chọn " + nhanCot(field).toLowerCase() + " —"), 0);
            setSelectedIndex(0);
            if (!previous.isEmpty()) {
                for (int i = 1; i < getItemCount(); i++) {
                    if (previous.equals(getItemAt(i).id())) {
                        setSelectedIndex(i);
                        break;
                    }
                }
            }
            setToolTipText(getItemCount() == 1 ? "Chưa có dữ liệu để chọn" : null);
        } catch (SQLException ex) {
            removeAllItems();
            throw ex;
        } finally {
            refreshing = false;
            if (!previous.equals(selectedId()))
                super.fireActionEvent();
        }
    }

    // ===== PHƯƠNG THỨC: filterVehiclesByCustomer(String) — Lọc vehicles by khách hàng. =====
    void filterVehiclesByCustomer(String customerId) throws SQLException {
        if (!"PhieuTiepNhan".equals(owner) || !"VIN".equals(field) || !"PhuongTien".equals(target))
            throw new IllegalStateException(
                    "Bộ lọc khách hàng chỉ áp dụng cho danh sách phương tiện của phiếu tiếp nhận.");

        if (customerId == null || customerId.isBlank()) {
            int orderBy = baseQuery.lastIndexOf(" ORDER BY ");
            query = baseQuery.substring(0, orderBy) + " WHERE 1=0" + baseQuery.substring(orderBy);
            queryParams = new String[0];
        } else {
            int orderBy = baseQuery.lastIndexOf(" ORDER BY ");
            String beforeOrder = baseQuery.substring(0, orderBy);
            String predicate = beforeOrder.contains(" WHERE ") ? " AND " : " WHERE ";
            query = beforeOrder + predicate + "[maKhachHang]=?" + baseQuery.substring(orderBy);
            queryParams = new String[] { customerId.trim() };
        }
        refresh();
        if (customerId != null && !customerId.isBlank() && getItemCount() == 2)
            setSelectedIndex(1);
    }

    // ===== PHƯƠNG THỨC: clearVehicleCustomerFilter() — Xóa bộ lọc khách hàng để hiển thị lại phương tiện. =====
    void clearVehicleCustomerFilter() throws SQLException {
        if (!"PhieuTiepNhan".equals(owner) || !"VIN".equals(field) || !"PhuongTien".equals(target))
            throw new IllegalStateException(
                    "Bộ lọc khách hàng chỉ áp dụng cho danh sách phương tiện của phiếu tiếp nhận.");
        query = baseQuery;
        queryParams = new String[0];
        refresh();
    }

    // ===== PHƯƠNG THỨC: selectedId() — Chọn id. =====
    String selectedId() {
        MucThamChieu selected = (MucThamChieu) getSelectedItem();
        String id = selected == null ? null : selected.id();
        return id == null ? "" : id;
    }

    // ===== PHƯƠNG THỨC: selectId(String) — Chọn id. =====
    void selectId(String id) {
        if (id == null || id.isBlank()) {
            if (getItemCount() > 0)
                setSelectedIndex(0);
            else
                setSelectedIndex(-1);
            return;
        }
        for (int i = 0; i < getItemCount(); i++)
            if (id.equals(getItemAt(i).id())) {
                setSelectedIndex(i);
                return;
            }
        if (shouldFilterHidden() && (preservedId == null || !preservedId.equals(id.trim()))) {
            preservedId = id.trim();
            try {
                refresh();
                for (int i = 0; i < getItemCount(); i++)
                    if (id.equals(getItemAt(i).id())) {
                        setSelectedIndex(i);
                        return;
                    }
            } catch (SQLException ignored) {
            }
        }
        addItem(new MucThamChieu(id, id));
        setSelectedIndex(getItemCount() - 1);
    }

    // ===== PHƯƠNG THỨC: validateSelection() — Kiểm tra tính hợp lệ của lựa chọn. =====
    void validateSelection() throws ThongBaoLoiSQL.Issue {
        if (isEnabled() && getSelectedItem() == null)
            throw new ThongBaoLoiSQL.Issue(field,
                    "Vui lòng chọn một kết quả hợp lệ cho " + nhanCot(field).toLowerCase() + ".");
        String previous = selectedId();
        try {
            refresh();
        } catch (SQLException ex) {
            throw new ThongBaoLoiSQL.Issue(field,
                    "Không kiểm tra được " + nhanCot(field).toLowerCase() + ": " + ex.getMessage());
        }
        if (!previous.isEmpty() && !previous.equals(selectedId())) {
            throw new ThongBaoLoiSQL.Issue(field, "Lựa chọn " + previous
                    + " không còn trong database. Vui lòng chọn lại " + nhanCot(field).toLowerCase() + ".");
        }
    }

    private static final class GiaoDien {

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

    record MucThamChieu(String id, String label) implements java.io.Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return label;
    }

    }

    // ===== PHƯƠNG THỨC: napDanhSachThamChieu() — Tải danh sách dữ liệu cho các ô tham chiếu. =====
    private void napDanhSachThamChieu() throws SQLException {
        boolean filterHidden = shouldFilterHidden();
        removeAllItems();
        try (Connection conn = KetNoiCSDL.getConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            for (int i = 0; i < queryParams.length; i++)
                ps.setString(i + 1, queryParams[i]);
            try (ResultSet rs = ps.executeQuery()) {
                // Mỗi KH được đưa vào cmb dưới dạng MucThamChieu
                while (rs.next()) {
                    String id = rs.getString(1);
                    String label = rs.getString(2);
                    if (filterHidden && target != null && dao.HiddenRecords.isHidden(target, id)) {
                        if (preservedId != null && !preservedId.isBlank() && preservedId.equals(id)) {
                            addItem(new MucThamChieu(id, "[Đã ẩn] " + label));
                        }
                        continue;
                    }
                    addItem(new MucThamChieu(id, label));
                }
            }
        } catch (SQLException ex) {
            removeAllItems();
            throw ex;
        }
    }
}
