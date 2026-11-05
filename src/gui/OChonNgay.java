package gui;

import java.awt.*;

import com.toedter.calendar.JDateChooser;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import javax.swing.JPanel;
import javax.swing.JTextField;

// ===== GUI: OChonNgay =====
/** Ô ngày dùng lịch bật lên của thư viện JCalendar. */
public final class OChonNgay extends JPanel {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private final JDateChooser pnlChooser;
    private String initialText;

    // ===== KHỞI TẠO ĐỐI TƯỢNG: OChonNgay(String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public OChonNgay(String initial) {
        super(new BorderLayout());
        setOpaque(false);
        pnlChooser = new JDateChooser();
        pnlChooser.setLocale(Locale.forLanguageTag("vi-VN"));
        pnlChooser.setDateFormatString("dd/MM/yyyy");
        pnlChooser.setFont(GiaoDien.font(12, false));
        pnlChooser.getCalendarButton().setPreferredSize(new Dimension(32, 38));
        JTextField txtEditor = (JTextField) pnlChooser.getDateEditor().getUiComponent();
        txtEditor.setEditable(false);
        txtEditor.setBackground(GiaoDien.WHITE);
        txtEditor.setForeground(GiaoDien.TEXT);
        setDateText(initial);
        add(pnlChooser, BorderLayout.CENTER);
        setPreferredSize(new Dimension(220, 40));
        setMinimumSize(new Dimension(160, 38));
    }

    // ===== PHƯƠNG THỨC: getDateText() — Lấy ngày văn bản. =====
    /** Giữ định dạng ISO cho lớp lưu dữ liệu SQL. */
    public String getDateText() {
        Date date = pnlChooser.getDate();
        if (date == null)
            return "";
        String selected = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
        return initialText != null && initialText.startsWith(selected) ? initialText : selected;
    }

    // ===== PHƯƠNG THỨC: focusCalendar() — Đặt tiêu điểm vào bộ chọn ngày. =====
    public void focusCalendar() {
        pnlChooser.getCalendarButton().requestFocusInWindow();
    }

    // ===== PHƯƠNG THỨC: setDateText(String) — Gán ngày văn bản. =====
    public void setDateText(String value) {
        initialText = value;
        if (value == null || value.trim().isEmpty()) {
            pnlChooser.setDate(null);
            return;
        }
        LocalDate date = LocalDate.parse(value.trim().substring(0, 10));
        pnlChooser.setDate(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private static final class GiaoDien {

        private static final Color WHITE = Color.WHITE;
        private static final Color TEXT = new Color(0x21383C);

        // ===== PHƯƠNG THỨC: font(int, boolean) — Tạo phông chữ theo cỡ và kiểu chữ. =====
        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
        }
    }
}
