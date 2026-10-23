package dao;

public class TonKhoDAO extends BaseDAO {
    @Override
    public java.util.List<java.util.LinkedHashMap<String,Object>> getAll() throws java.sql.SQLException {
        var rows=new java.util.ArrayList<java.util.LinkedHashMap<String,Object>>();
        String sql="SELECT t.*,p.tenPhuTung FROM dbo.TonKho t LEFT JOIN dbo.PhuTung p ON p.maPhuTung=t.maPhuTung ORDER BY t.maTonKho";
        try(var conn=connectDB.ConnectDB.getConnection(); var ps=conn.prepareStatement(sql); var rs=ps.executeQuery()) {
            while(rs.next()) {
                if(HiddenRecords.isHidden("TonKho",rs.getString("maTonKho"))) continue;
                var row=new java.util.LinkedHashMap<String,Object>();
                for(String field:Schema.COLUMNS.get("TonKho")) row.put(field,rs.getObject(field));
                row.put("tenPhuTung",rs.getString("tenPhuTung")); rows.add(row);
            }
        }
        return rows;
    }
    public TonKhoDAO() {
        super("TonKho");
    }
}
