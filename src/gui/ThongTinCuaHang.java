package gui;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;

// ===== GUI: THÔNG TIN CỬA HÀNG =====

/** Thông tin đơn vị dùng trên chứng từ; lưu UTF-8 bên ngoài mã nguồn. */
final class ThongTinCuaHang {
    // ===== THÀNH PHẦN GIAO DIỆN =====
    static final String[] KEYS = { "ten", "chiNhanh", "diaChi", "dienThoai", "email", "maSoThue" };
    static final String[] LABELS = { "Tên cửa hàng", "Chi nhánh", "Địa chỉ", "Điện thoại", "Email",
            "Mã số thuế" };
    private final Properties values = new Properties();
    private final Path file;

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ThongTinCuaHang() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    ThongTinCuaHang() throws IOException {
        Path root;
        try {
            root = Paths
                    .get(ThongTinCuaHang.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            if (!Files.isDirectory(root)) {
                root = root.getParent();
            }

            while (root != null && !Files.isDirectory(root.resolve("data"))) {
                root = root.getParent();
            }

        } catch (java.net.URISyntaxException | SecurityException | NullPointerException ex) {
            root = null;
        }
        file = (root == null ? Paths.get("").toAbsolutePath() : root).resolve("data/cua-hang.properties");
        values.setProperty("ten", "THANH PHONG AUTO");
        values.setProperty("chiNhanh", "Xưởng dịch vụ Quận 7");
        if (Files.exists(file))
            try (Reader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                values.load(in);
            }
    }

    // ===== PHƯƠNG THỨC: get(String) — Lấy nghiệp vụ tương ứng. =====
    String get(String key) {
        return values.getProperty(key, "").trim();
    }

    // ===== PHƯƠNG THỨC: complete() — Đánh dấu nghiệp vụ đã hoàn tất. =====
    boolean complete() {
        return !get("ten").isEmpty() && !get("diaChi").isEmpty() && !get("dienThoai").isEmpty();
    }

    // ===== PHƯƠNG THỨC: edit(java.awt.Component) — Mở biểu mẫu để chỉnh sửa dữ liệu. =====
    boolean edit(java.awt.Component owner) throws IOException {
        HopThoaiThongTinCuaHang dialog = new HopThoaiThongTinCuaHang(owner, this);
        dialog.setVisible(true);
        return dialog.saved();
    }

    // ===== PHƯƠNG THỨC: save(Properties) — Lưu nghiệp vụ tương ứng. =====
    void save(Properties updated) throws IOException {
        Files.createDirectories(file.getParent());
        Path temporary = Files.createTempFile(file.getParent(), "cua-hang-", ".tmp");
        try {
            try (Writer out = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                updated.store(out, "Thong tin cua hang tren hoa don");
            }
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
        values.putAll(updated);
    }

    // ===== PHƯƠNG THỨC: headerHtml() — Tạo phần đầu HTML cho chứng từ in. =====
    String headerHtml() {
        return headerHtml(values);
    }

    // ===== PHƯƠNG THỨC: headerHtml(Properties) — Tạo phần đầu HTML cho chứng từ in. =====
    static String headerHtml(Properties data) {
        return "<table width='100%' cellspacing='0' cellpadding='0'><tr><td width='58%' valign='top'>"
                + "<font size='5' color='#13272e'><b>" + html(data.getProperty("ten", "")) + "</b></font><br>"
                + html(data.getProperty("chiNhanh", "")) + "<br>Địa chỉ: "
                + html(data.getProperty("diaChi", ""))
                + "</td><td width='42%' valign='top' align='right'>Điện thoại: "
                + html(data.getProperty("dienThoai", "")) + optional(data, "email", "Email")
                + optional(data, "maSoThue", "Mã số thuế") + "</td></tr></table>";
    }

    // ===== PHƯƠNG THỨC: optional(Properties, String, String) — Chuyển đổi giá trị tùy chọn nếu có nhập. =====
    private static String optional(Properties data, String key, String label) {
        String value = data.getProperty(key, "").trim();
        return value.isEmpty() ? "" : "<br>" + label + ": " + html(value);
    }

    // ===== PHƯƠNG THỨC: html(String) — Tạo nội dung HTML để hiển thị hoặc in. =====
    static String html(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
