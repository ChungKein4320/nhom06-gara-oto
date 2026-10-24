package gui;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;

/** Thông tin đơn vị dùng trên chứng từ; lưu UTF-8 bên ngoài mã nguồn. */
final class ThongTinCuaHang {
    static final String[] KEYS = {"ten", "chiNhanh", "diaChi", "dienThoai", "email", "maSoThue"};
    static final String[] LABELS = {"Tên cửa hàng", "Chi nhánh", "Địa chỉ", "Điện thoại", "Email", "Mã số thuế"};
    private final Properties values = new Properties();
    private final Path file;

    ThongTinCuaHang() throws IOException {
        Path root;
        try {
        root = Paths.get(
            ThongTinCuaHang.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
        );

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
        if (Files.exists(file)) try (Reader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) { values.load(in); }
    }

    String get(String key) { return values.getProperty(key, "").trim(); }
    boolean complete() { return !get("ten").isEmpty() && !get("diaChi").isEmpty() && !get("dienThoai").isEmpty(); }

    boolean edit(java.awt.Component owner) throws IOException {
        HopThoaiThongTinCuaHang dialog = new HopThoaiThongTinCuaHang(owner, this);
        dialog.setVisible(true);
        return dialog.saved();
    }

    void save(Properties updated) throws IOException {
            Files.createDirectories(file.getParent());
            Path temporary = Files.createTempFile(file.getParent(), "cua-hang-", ".tmp");
            try {
                try (Writer out = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) { updated.store(out, "Thong tin cua hang tren hoa don"); }
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            } finally { Files.deleteIfExists(temporary); }
            values.putAll(updated);
    }

    String headerHtml() { return headerHtml(values); }

    static String headerHtml(Properties data) {
        return "<table width='100%' cellspacing='0' cellpadding='0'><tr><td width='58%' valign='top'>"
                + "<font size='5' color='#13272e'><b>" + html(data.getProperty("ten", "")) + "</b></font><br>"
                + html(data.getProperty("chiNhanh", "")) + "<br>Địa chỉ: " + html(data.getProperty("diaChi", ""))
                + "</td><td width='42%' valign='top' align='right'>Điện thoại: " + html(data.getProperty("dienThoai", ""))
                + optional(data, "email", "Email") + optional(data, "maSoThue", "Mã số thuế")
                + "</td></tr></table>";
    }

    private static String optional(Properties data, String key, String label) {
        String value = data.getProperty(key, "").trim();
        return value.isEmpty() ? "" : "<br>" + label + ": " + html(value);
    }

    static String html(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
