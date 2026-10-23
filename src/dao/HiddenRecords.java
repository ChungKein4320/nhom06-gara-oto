package dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Lưu các dòng được ẩn trong giao diện vào cấu hình của máy tính.
 * Không thực hiện UPDATE/DELETE/INSERT trên SQL Server.
 * Danh sách ẩn có hiệu lực trên máy này, không đồng bộ sang máy khác.
 */
public final class HiddenRecords {

    private static final Path FILE = Paths.get(
            System.getProperty("user.home"),
            ".thinking-auto",
            "hidden-records.properties"
    );

    private HiddenRecords() {
    }

    private static String key(String table, String id) {
        return table + "." + id;
    }

    private static Properties read() throws IOException {
        Properties properties = new Properties();
        if (Files.isRegularFile(FILE)) {
            try (InputStream input = Files.newInputStream(FILE)) {
                properties.load(input);
            }
        }
        return properties;
    }

    public static synchronized boolean isHidden(String table, String id) {
        try {
            return "true".equals(read().getProperty(key(table, id)));
        } catch (IOException exception) {
            // Không tự ý ẩn dữ liệu khi file cấu hình không đọc được.
            return false;
        }
    }

    public static synchronized void hide(String table, String id)
            throws java.sql.SQLException {
        try {
            Properties properties = read();
            properties.setProperty(key(table, id), "true");
            Files.createDirectories(FILE.getParent());
            try (OutputStream output = Files.newOutputStream(FILE)) {
                properties.store(output, "Thinking Auto - hidden table rows");
            }
        } catch (IOException exception) {
            throw new java.sql.SQLException(
                    "Không thể lưu trạng thái ẩn trên máy này: "
                            + exception.getMessage(),
                    exception
            );
        }
    }
}
