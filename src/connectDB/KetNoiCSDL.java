package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class KetNoiCSDL {

    private static final String DEFAULT_DATABASE = "GaraOto";
    private static final Pattern SAFE_DB_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]+$");
    private static final String USER = "sa";
    private static final String PASSWORD = "sapassword";
    private static final KetNoiCSDL instance = new KetNoiCSDL();

    private KetNoiCSDL() { }

    public static KetNoiCSDL getInstance() {
        return instance;
    }

    /**
     * Phân tích và kiểm tra tính hợp lệ của tên database theo nguyên tắc fail-closed:
     * - Nếu biến môi trường không được đặt hoặc là chuỗi trống: giữ nguyên database mặc định [GaraOto].
     * - Nếu biến môi trường được đặt nhưng chứa ký tự không hợp lệ: ném ngoại lệ dừng kết nối ngay lập tức,
     *   tuyệt đối không tự động fallback về database mặc định để bảo vệ an toàn cho database chính.
     */
    public static String parseDatabaseName(String envDb) throws SQLException {
        if (envDb == null || envDb.isBlank()) {
            return DEFAULT_DATABASE;
        }
        String candidate = envDb.trim();
        if (!SAFE_DB_NAME_PATTERN.matcher(candidate).matches()) {
            throw new SQLException("Tên cơ sở dữ liệu không hợp lệ trong biến môi trường GARA_DATABASE_NAME: '"
                    + candidate + "'. Tên database chỉ được chứa chữ cái, chữ số và dấu gạch dưới ([A-Za-z0-9_]). Dừng kết nối an toàn.");
        }
        return candidate;
    }

    public static String getDatabaseName() throws SQLException {
        return parseDatabaseName(System.getenv("GARA_DATABASE_NAME"));
    }

    public static String getUrl() throws SQLException {
        return "jdbc:sqlserver://localhost:1433;databaseName=" + getDatabaseName() + ";encrypt=true;trustServerCertificate=true;";
    }

    /** Mỗi DAO sở hữu một kết nối riêng và tự đóng sau khi dùng. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getUrl(), USER, PASSWORD);
    }

    public void connect() throws SQLException {
        try (Connection conn = getConnection()) {
            if (conn.isClosed()) throw new SQLException("Kết nối đã đóng.");
            System.out.println("Ket noi SQL Server thanh cong!");
        }
    }

    public static void disconnect() {
        // Không dùng kết nối chia sẻ: DAO tự đóng kết nối của mình.
    }
}
