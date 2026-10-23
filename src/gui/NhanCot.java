package gui;

import java.util.HashMap;
import java.util.Map;

/** Tên hiển thị của cột; không thay đổi tên trường dùng để đọc/ghi SQL. */
public final class NhanCot {
    private static final Map<String, String> LABELS = new HashMap<>();

    static {
        String[][] labels = {
            {"khachHangHienThi", "Khách hàng"},
            {"tongThanhToan", "Tổng thanh toán"},
            {"gioiTinh", "Giới tính"}, {"soKM", "Số km"},
            {"ketQuaChuanDoan", "Kết quả chẩn đoán"}, {"maChiTietHoaDon", "Mã dòng hóa đơn"},
            {"maNhanVien", "Mã nhân viên"}, {"maNV", "Mã nhân viên"},
            {"hoTenNV", "Họ tên nhân viên"}, {"ngaySinh", "Ngày sinh"},
            {"soDienThoai", "Số điện thoại"}, {"email", "Email"},
            {"diaChi", "Địa chỉ"}, {"vaiTro", "Vai trò"}, {"trangThai", "Trạng thái"},
            {"maKhachHang", "Mã khách hàng"}, {"hoTenKH", "Họ tên khách hàng"},
            {"VIN", "Số khung (VIN)"}, {"bienSo", "Biển số"}, {"hangXe", "Hãng xe"},
            {"dongXe", "Dòng xe"}, {"mauXe", "Màu xe"}, {"namSanXuat", "Năm sản xuất"},
            {"soMay", "Số máy"}, {"soKm", "Số km"},
            {"maDichVu", "Mã dịch vụ"}, {"tenDichVu", "Tên dịch vụ"},
            {"moTa", "Mô tả"}, {"donGia", "Đơn giá"},
            {"maPhuTung", "Mã phụ tùng"}, {"tenPhuTung", "Tên phụ tùng"},
            {"loaiPhuTung", "Loại phụ tùng"},
            {"maNCC", "Mã nhà cung cấp"}, {"tenNCC", "Tên nhà cung cấp"},
            {"maPhuongThuc", "Mã phương thức thanh toán"},
            {"tenPhuongThuc", "Tên phương thức thanh toán"},
            {"maGiamGia", "Mã giảm giá"}, {"mucGiam", "Mức giảm"},
            {"ngayTao", "Ngày tạo"}, {"ngayHetHan", "Ngày hết hạn"},
            {"maTaiKhoan", "Mã tài khoản"}, {"tenDangNhap", "Tên đăng nhập"},
            {"matKhau", "Mật khẩu"}, {"maTonKho", "Mã tồn kho"},
            {"soLuongHienTai", "Số lượng hiện tại"}, {"soLuongToiThieu", "Số lượng tối thiểu"},
            {"maPTN", "Mã phiếu tiếp nhận"}, {"ngayTiepNhan", "Ngày tiếp nhận"},
            {"soODO", "Số ODO"}, {"maPKT", "Mã phiếu kiểm tra"},
            {"ngayKiemTra", "Ngày kiểm tra"}, {"maBaoGia", "Mã báo giá"},
            {"ngayLap", "Ngày lập"}, {"thoiGianXacNhan", "Thời gian xác nhận"},
            {"maPSC", "Mã phiếu sửa chữa"}, {"ngayHoanThanh", "Ngày hoàn thành"},
            {"maPNK", "Mã phiếu nhập kho"}, {"ngayNhap", "Ngày nhập"},
            {"yeuCauDichVu", "Yêu cầu dịch vụ"}, {"hangMucKiemTra", "Hạng mục kiểm tra"},
            {"ketQuaChanDoan", "Kết quả chẩn đoán"}, {"ketQuaKiemTra", "Kết quả kiểm tra"},
            {"soLuongPhuTung", "Số lượng phụ tùng"}, {"donGiaPhuTung", "Đơn giá phụ tùng"},
            {"chiPhiNhanCong", "Chi phí nhân công"}, {"trangThaiHangMuc", "Trạng thái hạng mục"},
            {"ketQuaNghiemThu", "Kết quả nghiệm thu"},
            {"ngayBatDauBaoHanh", "Ngày bắt đầu bảo hành"}, {"thoiHanBaoHanh", "Thời hạn bảo hành"},
            {"ngayHetHanBaoHanh", "Ngày hết hạn bảo hành"},
            {"soLuongNhap", "Số lượng nhập"}, {"donGiaNhap", "Đơn giá nhập"},
            {"maHoaDon", "Mã hóa đơn"}, {"tienPhuTung", "Tiền phụ tùng"},
            {"tienCong", "Tiền công"}, {"dichVuKhac", "Dịch vụ khác"},
            {"thueVAT", "Thuế VAT"}, {"giamGia", "Giảm giá"},
            {"trangThaiThanhToan", "Trạng thái thanh toán"}, {"soLuong", "Số lượng"},
            {"maLBH", "Mã lịch bảo hành"}, {"ngayXuLy", "Ngày xử lý"},
            {"ketQuaXuLy", "Kết quả xử lý"}, {"ghiChu", "Ghi chú"},
            {"maLichNhac", "Mã lịch nhắc"}, {"ngayNhac", "Ngày nhắc"}, {"noiDung", "Nội dung"},
            {"maLichSuTD", "Mã lịch sử tích điểm"}, {"ngayTichDiem", "Ngày tích điểm"},
            {"soDiem", "Số điểm"}, {"lyDo", "Lý do"}
        };
        for (String[] label : labels) LABELS.put(label[0], label[1]);
    }

    private NhanCot() { }

    public static String forField(String field) {
        return LABELS.getOrDefault(field, field);
    }

    public static String[] forFields(String[] fields) {
        String[] labels = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            labels[i] = forField(fields[i]);
        }
        return labels;
    }
}
