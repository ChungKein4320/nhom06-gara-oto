package gui;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** VietQR Quick Link: https://www.vietqr.io/qr-dong . */
final class MaThanhToanNganHang {
    static final String BANK="Vietcombank";
    static final String BANK_BIN="970436";
    static final String ACCOUNT="123456789";
    static final String OWNER="Nguyễn Văn A";
    private MaThanhToanNganHang() {}
    static URI imageUri(String invoice,BigDecimal amount) {
        if(invoice==null || !invoice.matches("HD[0-9]{3}")) throw new IllegalArgumentException("Mã hóa đơn không hợp lệ.");
        if(amount==null || amount.signum()<=0 || amount.stripTrailingZeros().scale()>0 || amount.precision()-amount.scale()>13)
            throw new IllegalArgumentException("QR yêu cầu số tiền nguyên đồng, lớn hơn 0 và tối đa 13 chữ số.");
        return URI.create("https://img.vietqr.io/image/"+BANK_BIN+"-"+ACCOUNT+"-qr_only.png?amount="+amount.toBigIntegerExact()+"&addInfo="+encode("THANH TOAN "+invoice)+"&accountName="+encode(OWNER));
    }
    private static String encode(String text) { return URLEncoder.encode(text,StandardCharsets.UTF_8); }
}
