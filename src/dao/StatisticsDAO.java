package dao;

import connectDB.ConnectDB;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class StatisticsDAO {
    public record Bucket(String label, double value) implements java.io.Serializable {
        @java.io.Serial private static final long serialVersionUID = 1L;
    }
    public record Result(List<Bucket> buckets, BigDecimal total) { }
    private StatisticsDAO() { }

    public static Result load(int category, LocalDate from, LocalDate to, boolean daily) throws SQLException {
        if (from.isAfter(to)) throw new IllegalArgumentException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc.");
        String date = category == 1 ? "ngayTiepNhan" : "ngayLap";
        String group = "CONVERT(char(" + (daily ? "10" : "7") + "), " + date + ", 120)";
        String source;
        String value;
        if (category == 0) {
            source = " FROM dbo.HoaDon WHERE trangThaiThanhToan=N'Đã thanh toán' AND ngayLap>=? AND ngayLap<?";
            value = "SUM(tienPhuTung+tienCong+dichVuKhac+thueVAT-giamGia)";
        } else if (category == 1) {
            source = " FROM dbo.PhieuTiepNhan WHERE ngayTiepNhan>=? AND ngayTiepNhan<?";
            value = "COUNT(DISTINCT maKhachHang)";
        } else if (category == 2) {
            source = " FROM dbo.ChiTietHoaDon ct JOIN dbo.HoaDon h ON h.maHoaDon=ct.maHoaDon "
                    + "JOIN dbo.DichVu d ON d.maDichVu=ct.maDichVu WHERE h.ngayLap>=? AND h.ngayLap<?";
            // Mỗi chi tiết có khóa (hóa đơn, dịch vụ); số lượng ở chi tiết có thể là số phụ tùng.
            value = "COUNT(*)";
            group = "d.maDichVu, d.tenDichVu";
        } else throw new IllegalArgumentException("Phân loại không hợp lệ.");
        String label = category == 2 ? "CONCAT(d.maDichVu,N' — ',d.tenDichVu)" : group;
        String sql = "SELECT " + label + " nhom," + value + " giaTri" + source
                + " GROUP BY " + group + (category == 2 ? " ORDER BY giaTri DESC,d.maDichVu" : " ORDER BY nhom");
        List<Bucket> buckets = new ArrayList<>();
        BigDecimal total;
        try (Connection conn = ConnectDB.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                range(ps, from, to);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) buckets.add(new Bucket(rs.getString(1), rs.getDouble(2)));
                }
            }
            // Khách hàng được đếm duy nhất cho cả kỳ, không cộng trùng giữa các ngày/tháng.
            try (PreparedStatement ps = conn.prepareStatement("SELECT " + value + source)) {
                range(ps, from, to);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next(); total = rs.getBigDecimal(1);
                    if (total == null) total = BigDecimal.ZERO;
                }
            }
        }
        if (category < 2 && !buckets.isEmpty()) {
            java.util.Map<String, Double> byDate = new java.util.HashMap<>();
            for (Bucket bucket : buckets) byDate.put(bucket.label(), bucket.value());
            buckets.clear();
            LocalDate cursor = daily ? from : from.withDayOfMonth(1);
            while (!cursor.isAfter(to)) {
                String key = daily ? cursor.toString() : cursor.toString().substring(0, 7);
                buckets.add(new Bucket(key, byDate.getOrDefault(key, 0.0)));
                cursor = daily ? cursor.plusDays(1) : cursor.plusMonths(1);
            }
        }
        return new Result(List.copyOf(buckets), total);
    }

    private static void range(PreparedStatement ps, LocalDate from, LocalDate to) throws SQLException {
        ps.setDate(1, Date.valueOf(from));
        ps.setDate(2, Date.valueOf(to.plusDays(1)));
    }
}
