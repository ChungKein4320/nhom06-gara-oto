package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class KetNoiCSDL {

    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=GaraOto;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "sapassword";
    private static final KetNoiCSDL instance = new KetNoiCSDL();

    private KetNoiCSDL() { }

    public static KetNoiCSDL getInstance() {
        return instance;
    }

    /** Mỗi DAO sở hữu một kết nối riêng và tự đóng sau khi dùng. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
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
