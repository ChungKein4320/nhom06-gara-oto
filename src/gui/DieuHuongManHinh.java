package gui;

import javax.swing.JComponent;

// ===== GUI: DieuHuongManHinh =====
/** Chọn màn hình riêng theo mục menu; không xây dựng giao diện tại đây. */
public final class DieuHuongManHinh {
    // ===== KHỞI TẠO ĐỐI TƯỢNG: DieuHuongManHinh() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    private DieuHuongManHinh() {
    }

    // ===== PHƯƠNG THỨC: tableFor(String) — Ánh xạ màn hình nghiệp vụ sang bảng dữ liệu. =====
    public static String tableFor(String page) {
        return switch (page) {
        case "Phiếu tiếp nhận" -> "PhieuTiepNhan";
        case "Phiếu kiểm tra" -> "PhieuKiemTra";
        case "Phiếu báo giá" -> "PhieuBaoGia";
        case "Phiếu sửa chữa" -> "PhieuSuaChua";
        case "Bảo hành" -> "LichBaoHanh";
        case "Lịch nhắc bảo dưỡng" -> "LichNhacBaoDuong";
        case "Tồn kho" -> "TonKho";
        case "Phiếu cấp phát phụ tùng" -> "ChiTietPhieuSuaChua";
        case "Cấp phát phụ tùng" -> "ChiTietPhieuSuaChua";
        case "Phiếu nhập kho phụ tùng" -> "PhieuNhapKho";
        case "Nhập kho phụ tùng" -> "PhieuNhapKho";
        case "Hóa đơn" -> "HoaDon";
        case "Lịch sử tích điểm" -> "LichSuTichDiem";
        case "Khuyến mãi/Tích điểm" -> "LichSuTichDiem";
        case "Nhà cung cấp" -> "NhaCungCap";
        case "Nhân viên" -> "NhanVien";
        case "Khách hàng" -> "KhachHang";
        case "Phương tiện" -> "PhuongTien";
        case "Danh mục dịch vụ" -> "DichVu";
        case "Danh mục phụ tùng" -> "PhuTung";
        case "Tài khoản" -> "TaiKhoan";
        case "Khuyến mãi" -> "MaGiamGia";
        case "Phiếu yêu cầu nhập kho" -> "YeuCauNhapKho";
        case "Danh sách yêu cầu nhập" -> "YeuCauNhapKho";
        case "Yêu cầu nhập kho" -> "YeuCauNhapKho";
        default -> null;
        };
    }

    // ===== PHƯƠNG THỨC: tao(String) — Tạo nghiệp vụ tương ứng. =====
    public static JComponent tao(String page) {
        return tao(page, null, null);
    }

    // ===== PHƯƠNG THỨC: tao(String, VaiTro, String) — Tạo nghiệp vụ tương ứng. =====
    public static JComponent tao(String page, VaiTro role, String employeeId) {
        return switch (page) {
        case "Phiếu tiếp nhận" -> new FormPhieuTiepNhan(page, role, employeeId);
        case "Phiếu kiểm tra" -> new FormPhieuKiemTra(page, role, employeeId);
        case "Phiếu báo giá" -> new FormPhieuBaoGia(page, role, employeeId);
        case "Phiếu sửa chữa" -> new FormPhieuSuaChua(page, role, employeeId);
        case "Bảo hành" -> new FormLichBaoHanh(page, role, employeeId);
        case "Lịch nhắc bảo dưỡng" -> new FormLichNhacBaoDuong(page, role, employeeId);
        case "Tồn kho" -> new FormTonKho(page, role, employeeId);
        case "Phiếu cấp phát phụ tùng", "Cấp phát phụ tùng" ->
            new FormChiTietPhieuSuaChua(page, role, employeeId);
        case "Phiếu nhập kho phụ tùng", "Nhập kho phụ tùng" -> new FormPhieuNhapKho(page, role, employeeId);
        case "Hóa đơn" -> new FormHoaDon(page, role, employeeId);
        case "Lịch sử tích điểm", "Khuyến mãi/Tích điểm" -> new FormLichSuTichDiem(page, role, employeeId);
        case "Nhà cung cấp" -> new FormNhaCungCap(page, role, employeeId);
        case "Nhân viên" -> new FormNhanVien(page, role, employeeId);
        case "Khách hàng" -> new FormKhachHang(page, role, employeeId);
        case "Phương tiện" -> new FormPhuongTien(page, role, employeeId);
        case "Danh mục dịch vụ" -> new FormDichVu(page, role, employeeId);
        case "Danh mục phụ tùng" -> new FormPhuTung(page, role, employeeId);
        case "Tài khoản" -> new FormTaiKhoan(page, role, employeeId);
        case "Khuyến mãi" -> new FormMaGiamGia(page, role, employeeId);
        case "Phiếu yêu cầu nhập kho", "Danh sách yêu cầu nhập", "Yêu cầu nhập kho" ->
            new FormYeuCauNhapKho(page, role, employeeId);
        default -> throw new IllegalArgumentException("Chưa có màn hình cho: " + page);
        };
    }
}
