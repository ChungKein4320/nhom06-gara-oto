package dao;

import connectDB.KetNoiCSDL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class KhachHangDAO extends CoSoDAO {
    public KhachHangDAO() {
        super("KhachHang");
    }

    @Override
    public List<LinkedHashMap<String, Object>> getAll() throws SQLException {
        String sql = "SELECT k.*, COALESCE(d.tongDiemTichLuy, CONVERT(bigint,0)) AS tongDiemTichLuy, "
                + "COALESCE(vehicles.bienSoXe, N'Chưa có xe đăng ký') AS bienSoXe "
                + "FROM dbo.KhachHang k LEFT JOIN dbo.vw_KhachHangDiemTichLuy d "
                + "ON d.maKhachHang=k.maKhachHang "
                + "OUTER APPLY (SELECT STUFF((SELECT N', ' + p.bienSo FROM dbo.PhuongTien p "
                + "WHERE p.maKhachHang=k.maKhachHang ORDER BY p.bienSo FOR XML PATH(''), TYPE)"
                + ".value('.', 'nvarchar(max)'),1,2,N'') AS bienSoXe) vehicles "
                + "ORDER BY k.maKhachHang";
        List<LinkedHashMap<String, Object>> rows = new ArrayList<>();
        try (Connection connection = KetNoiCSDL.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                LinkedHashMap<String, Object> row = new LinkedHashMap<>();
                for (String field : Schema.COLUMNS.get("KhachHang")) {
                    row.put(field, result.getObject(field));
                }
                row.put("bienSoXe", result.getString("bienSoXe"));
                row.put("tongDiemTichLuy", result.getLong("tongDiemTichLuy"));
                String customerId = String.valueOf(row.get("maKhachHang"));
                if (!HiddenRecords.isHidden("KhachHang", customerId)) rows.add(row);
            }
        }
        return rows;
    }
}
