package dao;

import connectDB.ConnectDB;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

public final class DashboardDAO {
    private DashboardDAO() { }
    public static String metric(String key) throws SQLException {
        String sql = switch (key) {
            case "Xe tiếp nhận" -> "SELECT COUNT(*) FROM dbo.PhieuTiepNhan";
            case "Phiếu kiểm tra" -> "SELECT COUNT(*) FROM dbo.PhieuKiemTra";
            case "Báo giá" -> "SELECT COUNT(*) FROM dbo.PhieuBaoGia";
            case "Phiếu sửa chữa" -> "SELECT COUNT(*) FROM dbo.PhieuSuaChua";
            case "Loại phụ tùng" -> "SELECT COUNT(*) FROM dbo.PhuTung";
            case "Sắp hết hàng" -> "SELECT COUNT(*) FROM dbo.TonKho WHERE soLuongHienTai < soLuongToiThieu";
            case "Phiếu nhập" -> "SELECT COUNT(*) FROM dbo.PhieuNhapKho";
            case "Yêu cầu cấp" -> "SELECT COUNT(*) FROM dbo.ChiTietPhieuSuaChua WHERE trangThaiHangMuc = N'Thiếu phụ tùng'";
            case "Hóa đơn" -> "SELECT COUNT(*) FROM dbo.HoaDon";
            case "Chờ thanh toán" -> "SELECT COUNT(*) FROM dbo.HoaDon WHERE trangThaiThanhToan IN (N'Chưa thanh toán',N'Chờ thanh toán')";
            case "Khuyến mãi" -> "SELECT COUNT(*) FROM dbo.MaGiamGia WHERE trangThai = N'Đang hiệu lực' AND ngayHetHan >= CONVERT(date,GETDATE())";
            case "Khách hàng" -> "SELECT COUNT(*) FROM dbo.KhachHang";
            case "Phương tiện" -> "SELECT COUNT(*) FROM dbo.PhuongTien";
            case "Nhân viên" -> "SELECT COUNT(*) FROM dbo.NhanVien";
            case "Doanh thu" -> "SELECT COALESCE(SUM(tienPhuTung+tienCong+dichVuKhac+thueVAT-giamGia),0) FROM dbo.HoaDon WHERE trangThaiThanhToan = N'Đã thanh toán'";
            default -> throw new IllegalArgumentException("Chỉ số không hợp lệ: " + key);
        };
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) throw new SQLException("Không đọc được chỉ số.");
            return key.equals("Doanh thu")
                    ? NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(rs.getBigDecimal(1)) + " đ"
                    : rs.getString(1);
        }
    }
}
