package dao;

import connectDB.ConnectDB;
import gui.VaiTro;
import java.sql.*;

public class TaiKhoanDAO extends BaseDAO {
    public TaiKhoanDAO() {
        super("TaiKhoan");
    }

    public static class AuthResult {
        public final String maNhanVien;
        public final String hoTen;
        public final VaiTro role;

        public AuthResult(String maNhanVien, String hoTen, VaiTro role) {
            this.maNhanVien = maNhanVien;
            this.hoTen = hoTen;
            this.role = role;
        }
    }

    public AuthResult authenticate(String user, String password) throws SQLException {
        String sql = "SELECT tk.maNhanVien, nv.hoTenNV, nv.vaiTro, tk.matKhau "
                + "FROM dbo.TaiKhoan tk JOIN dbo.NhanVien nv ON nv.maNhanVien = tk.maNhanVien "
                + "WHERE tk.tenDangNhap = ? AND tk.trangThai IN (N'Hoạt động', N'Đang hoạt động') "
                + "AND nv.trangThai = N'Đang làm'";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                if (!password.equals(rs.getString("matKhau"))) return null;
                String id = rs.getString("maNhanVien");
                String title = rs.getString("vaiTro");
                VaiTro role;
                if (user.equalsIgnoreCase("admin") && id.equals("NV005")) role = VaiTro.ADMIN;
                else if (title.contains("kỹ thuật")) role = VaiTro.KY_THUAT;
                else if (title.contains("kho")) role = VaiTro.KHO;
                else if (title.contains("bán hàng") || title.contains("Thu ngân")) role = VaiTro.THU_NGAN;
                else role = VaiTro.QUAN_LY;
                return new AuthResult(id, rs.getString("hoTenNV"), role);
            }
        }
    }
}
