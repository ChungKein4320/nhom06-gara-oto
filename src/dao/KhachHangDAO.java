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
        String sql = "SELECT k.*, COALESCE(d.tongDiemTichLuy, CONVERT(bigint,0)) AS tongDiemTichLuy "
                + "FROM dbo.KhachHang k LEFT JOIN dbo.vw_KhachHangDiemTichLuy d "
                + "ON d.maKhachHang=k.maKhachHang ORDER BY k.maKhachHang";
        List<LinkedHashMap<String, Object>> rows = new ArrayList<>();
        try (Connection connection = KetNoiCSDL.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                LinkedHashMap<String, Object> row = new LinkedHashMap<>();
                for (String field : Schema.COLUMNS.get("KhachHang")) {
                    row.put(field, result.getObject(field));
                }
                row.put("tongDiemTichLuy", result.getLong("tongDiemTichLuy"));
                String customerId = String.valueOf(row.get("maKhachHang"));
                if (!HiddenRecords.isHidden("KhachHang", customerId)) rows.add(row);
            }
        }
        return rows;
    }
}
