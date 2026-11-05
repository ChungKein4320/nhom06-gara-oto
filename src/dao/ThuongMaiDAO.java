package dao;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

// ===== DAO: NGHIỆP VỤ BÁN HÀNG VÀ THANH TOÁN =====

/** Thanh toán, bán bổ sung và cấp phát đều nguyên tử và kiểm tra trạng thái mới nhất. */
public final class ThuongMaiDAO {
    // ===== CÁC NGHIỆP VỤ BÁN HÀNG =====
    // ===== KHỞI TẠO ĐỐI TƯỢNG: ThuongMaiDAO() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    private ThuongMaiDAO() {
    }

    // ===== TRUY VẤN VÀ THAO TÁC DỮ LIỆU =====

    // ===== PHƯƠNG THỨC: pay(String, String) — Ghi nhận thanh toán hóa đơn. =====
    public static void pay(String invoice, String method) throws SQLException {
        HoaDonDAO.pay(invoice, method);
    }

    // ===== PHƯƠNG THỨC: pay(String, String, BigDecimal) — Ghi nhận thanh toán hóa đơn. =====
    public static void pay(String invoice, String method, BigDecimal expectedTotal) throws SQLException {
        HoaDonDAO.pay(invoice, method, expectedTotal);
    }

    // ===== PHƯƠNG THỨC: pay(String, String, BigDecimal, String) — Ghi nhận thanh toán hóa đơn. =====
    public static void pay(String invoice, String method, BigDecimal expectedTotal, String scannedCustomer)
            throws SQLException {
        HoaDonDAO.pay(invoice, method, expectedTotal, scannedCustomer);
    }

    // ===== PHƯƠNG THỨC: changeCoupon(String, String) — Cập nhật mã giảm giá trên hóa đơn. =====
    public static void changeCoupon(String invoice, String coupon) throws SQLException {
        HoaDonDAO.changeCoupon(invoice, coupon);
    }

    // ===== PHƯƠNG THỨC: sell(String, String, String, int, BigDecimal) — Ghi nhận bán dịch vụ hoặc phụ tùng vào hóa đơn. =====
    public static void sell(String invoice, String service, String part, int quantity,
            BigDecimal expectedPrice) throws SQLException {
        HoaDonDAO.sell(invoice, service, part, quantity, expectedPrice);
    }

    public record SaleItem(String part, int quantity, BigDecimal expectedPrice) {
    }

    // ===== PHƯƠNG THỨC: sellBasket(String, String, List<SaleItem>) — Ghi nhận nhiều mặt hàng bán trong một lượt. =====
    public static void sellBasket(String invoice, String service, List<SaleItem> items) throws SQLException {
        HoaDonDAO.sellBasket(invoice, service, items);
    }

    // ===== PHƯƠNG THỨC: allocate(String, String, String) — Cấp phát phụ tùng cho phiếu sửa chữa. =====
    public static void allocate(String repair, String service, String part) throws SQLException {
        ChiTietPhieuSuaChuaDAO.allocate(repair, service, part);
    }

    // ===== PHƯƠNG THỨC: allocate(String, String, String, String) — Cấp phát phụ tùng cho phiếu sửa chữa. =====
    public static void allocate(String repair, String service, String part, String employee)
            throws SQLException {
        ChiTietPhieuSuaChuaDAO.allocate(repair, service, part, employee);
    }

    // ===== PHƯƠNG THỨC: issueThresholdCoupons(Connection, String, Timestamp, Timestamp, Timestamp) — Tạo mã giảm giá khi khách đạt ngưỡng tích điểm. =====
    static void issueThresholdCoupons(Connection conn, String customer, Timestamp cycleStart,
            Timestamp expiration, Timestamp now) throws SQLException {
        MaGiamGiaDAO.issueThresholdCoupons(conn, customer, cycleStart, expiration, now);
    }

}
