package dao;

import java.math.*;
import java.time.LocalDateTime;

public final class InvoiceMath {
    private InvoiceMath() {}
    public static BigDecimal vat(BigDecimal subtotal) { return subtotal.multiply(new BigDecimal("0.08")).setScale(2,RoundingMode.HALF_UP); }
    public static BigDecimal discount(BigDecimal subtotal, BigDecimal percent) { return subtotal.multiply(percent).divide(new BigDecimal("100"),2,RoundingMode.HALF_UP); }
    public static int points(BigDecimal paid) { return paid.divideToIntegralValue(new BigDecimal("100000")).intValueExact(); }
    public static LocalDateTime warrantyEnd(LocalDateTime start,int months) {
        if(months<0) throw new IllegalArgumentException("Thời hạn bảo hành không được âm.");
        return start.plusMonths(months);
    }
}
