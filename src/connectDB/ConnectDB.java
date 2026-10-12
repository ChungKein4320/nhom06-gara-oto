package connectDB;
import java.sql.Connection;
public final class ConnectDB {
 private ConnectDB(){}
 // TODO SQL: Thêm JDBC driver vào lib khi có SQL Server/MySQL.
 // TODO SQL: Khai báo URL, tài khoản và mật khẩu từ cấu hình an toàn.
 public static Connection getConnection(){throw new UnsupportedOperationException("Chưa cấu hình kết nối SQL");}
}
