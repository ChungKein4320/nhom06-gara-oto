package dao;

import java.math.*;
import java.time.LocalDateTime;

// ===== DAO: TÍNH TOÁN TIỀN, THUẾ, ĐIỂM VÀ BẢO HÀNH =====

public final class InvoiceMath {
    // ===== HÀM TÍNH TOÁN HÓA ĐƠN =====
    // ===== KHỞI TẠO ĐỐI TƯỢNG: InvoiceMath() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    private InvoiceMath() {
    }

    // ===== CÁC HÀM TÍNH TOÁN =====

    // ===== PHƯƠNG THỨC: vat(BigDecimal) — Tính tiền thuế giá trị gia tăng. =====
    public static BigDecimal vat(BigDecimal subtotal) {
        return subtotal.multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);
    }

    // ===== PHƯƠNG THỨC: discount(BigDecimal, BigDecimal) — Tính số tiền giảm giá theo quy tắc áp dụng. =====
    public static BigDecimal discount(BigDecimal subtotal, BigDecimal percent) {
        return subtotal.multiply(percent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    // ===== PHƯƠNG THỨC: points(BigDecimal) — Tính điểm tích lũy từ số tiền đủ điều kiện. =====
    public static int points(BigDecimal paid) {
        return paid.divideToIntegralValue(new BigDecimal("100000")).intValueExact();
    }

    // ===== PHƯƠNG THỨC: warrantyEnd(LocalDateTime, int) — Tính thời điểm kết thúc bảo hành. =====
    public static LocalDateTime warrantyEnd(LocalDateTime start, int months) {
        if (months < 0)
            throw new IllegalArgumentException("Thời hạn bảo hành không được âm.");
        return start.plusMonths(months);
    }
}
