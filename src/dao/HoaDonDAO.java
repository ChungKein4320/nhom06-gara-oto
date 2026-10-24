package dao;

public class HoaDonDAO extends CoSoDAO {
    @Override
    public java.util.List<java.util.LinkedHashMap<String,Object>> getAll() throws java.sql.SQLException {
        var rows=new java.util.ArrayList<java.util.LinkedHashMap<String,Object>>();
        String sql="SELECT h.*, CONCAT(h.maKhachHang, CASE WHEN k.hoTenKH IS NULL OR LTRIM(RTRIM(k.hoTenKH))=N'' THEN N'' ELSE N' — '+k.hoTenKH END) AS khachHangHienThi FROM dbo.HoaDon h LEFT JOIN dbo.KhachHang k ON k.maKhachHang=h.maKhachHang ORDER BY h.maHoaDon";
       try(var conn=connectDB.KetNoiCSDL.getConnection(); var ps=conn.prepareStatement(sql); var rs=ps.executeQuery()) {
            while(rs.next()) {
                if(HiddenRecords.isHidden("HoaDon",rs.getString("maHoaDon"))) continue;
                var row=new java.util.LinkedHashMap<String,Object>();
                for(String field:Schema.COLUMNS.get("HoaDon")) row.put(field,rs.getObject(field));
                row.put("khachHangHienThi",rs.getString("khachHangHienThi")); rows.add(row);
            }
        }
        return rows;
    }
    public HoaDonDAO() {
        super("HoaDon");
    }
}
