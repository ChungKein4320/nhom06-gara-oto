package dao;

import connectDB.KetNoiCSDL;
import java.sql.*;
import java.util.*;

/** CRUD tham số hóa, khóa đúng khóa đơn/ghép và dùng sequence của database. */
public class CoSoDAO {
    private final String table;
    private final String primary;
    public CoSoDAO(String table) {
        if (!Schema.COLUMNS.containsKey(table)) throw new IllegalArgumentException("Bảng không hợp lệ: " + table);
        this.table = table;
        primary = Schema.PRIMARY.get(table);
    }
    public List<LinkedHashMap<String,Object>> getAll() throws SQLException {
        try (Connection conn = KetNoiCSDL.getConnection()) {
            List<LinkedHashMap<String,Object>> rows=getAll(conn,null);
            rows.removeIf(row -> HiddenRecords.isHidden(table,String.valueOf(row.get(primary))));
            return rows;
        }
    }
    public List<LinkedHashMap<String,Object>> getAll(Connection conn, Map<String,Object> filter) throws SQLException {
        String where = filter == null || filter.isEmpty() ? "" : " WHERE " + conditions(filter);
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM dbo.[" + table + "]" + where + " ORDER BY [" + primary + "]")) {
            if (filter != null) bind(ps, filter.values());
            try (ResultSet rs = ps.executeQuery()) {
                List<LinkedHashMap<String,Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    LinkedHashMap<String,Object> row = new LinkedHashMap<>();
                    for (String field : Schema.COLUMNS.get(table)) row.put(field,rs.getObject(field));
                    rows.add(row);
                }
                return rows;
            }
        }
    }
    public String previewNextId() throws SQLException {
        if (Schema.KEYS.get(table).length != 1 || Schema.IDENTITY.contains(table) || primary.equals("VIN")) return "";
        if (usesReusableCodes()) return prefix() + String.format(Locale.ROOT,"%03d",nextAvailableNumber());
        try (Connection conn = KetNoiCSDL.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT CASE WHEN current_value IS NULL THEN CONVERT(int,start_value) ELSE CONVERT(int,current_value)+1 END FROM sys.sequences WHERE name=? AND schema_id=SCHEMA_ID('dbo')")) {
            ps.setString(1,"Seq_" + table);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? prefix() + String.format(Locale.ROOT,"%03d",rs.getInt(1)) : ""; }
        }
    }
    private String prefix() {
        return switch(table) {
            case "NhanVien" -> "NV"; case "KhachHang" -> "KH"; case "DichVu" -> "DV";
            case "PhuTung" -> "PT"; case "NhaCungCap" -> "NCC"; case "PhuongThucThanhToan" -> "TT";
            case "TaiKhoan" -> "TK"; case "TonKho" -> "TKH"; case "MaGiamGia" -> "MGG";
            case "PhieuTiepNhan" -> "PTN"; case "PhieuKiemTra" -> "PKT"; case "PhieuBaoGia" -> "PBG";
            case "PhieuSuaChua" -> "PSC"; case "PhieuNhapKho" -> "PNK"; case "HoaDon" -> "HD";
            case "LichBaoHanh" -> "LBH"; case "LichNhacBaoDuong" -> "LNBD"; case "LichSuTichDiem" -> "LS";
            default -> throw new IllegalArgumentException("Không có sequence cho " + table);
        };
    }
    private boolean usesReusableCodes() { return table.equals("KhachHang") || table.equals("PhieuTiepNhan"); }
    private int nextAvailableNumber() throws SQLException {
        String sql=";WITH Numbers(n) AS (SELECT CONVERT(int,1) UNION ALL SELECT n+1 FROM Numbers WHERE n<999) "+
                "SELECT MIN(n) FROM Numbers WHERE NOT EXISTS (SELECT 1 FROM dbo.["+table+"]"+
                " AS currentRows WHERE currentRows.["+primary+"]=?+RIGHT('000'+CONVERT(varchar(3),Numbers.n),3)) OPTION (MAXRECURSION 1000)";
        try(Connection conn=KetNoiCSDL.getConnection(); PreparedStatement ps=conn.prepareStatement(sql)) {
            ps.setString(1,prefix());
            try(ResultSet rs=ps.executeQuery()) {
                if(!rs.next() || rs.getObject(1)==null) throw new SQLException("Đã hết dải mã cho bảng "+table+".");
                return rs.getInt(1);
            }
        }
    }
    private int nextAvailableNumber(Connection conn) throws SQLException {
        String lockHint=" WITH (TABLOCKX,HOLDLOCK)";
        String sql=";WITH Numbers(n) AS (SELECT CONVERT(int,1) UNION ALL SELECT n+1 FROM Numbers WHERE n<999) "+
                "SELECT MIN(n) FROM Numbers WHERE NOT EXISTS (SELECT 1 FROM dbo.["+table+"]"+lockHint+
                " AS currentRows WHERE currentRows.["+primary+"]=?+RIGHT('000'+CONVERT(varchar(3),Numbers.n),3)) OPTION (MAXRECURSION 1000)";
        try(PreparedStatement ps=conn.prepareStatement(sql)) {
            ps.setString(1,prefix());
            try(ResultSet rs=ps.executeQuery()) {
                if(!rs.next() || rs.getObject(1)==null) throw new SQLException("Đã hết dải mã cho bảng "+table+".");
                return rs.getInt(1);
            }
        }
    }
    public void insert(Map<String,Object> row) throws SQLException { insert(row,null); }
    public void insert(Map<String,Object> row, String preview) throws SQLException {
        QuyTrinhDAO.transaction(conn -> { insert(conn,row); return null; });
    }
    /** Dùng connection của phiếu để lưu đầu phiếu và chi tiết cùng giao dịch. */
    public String insert(Connection conn, Map<String,Object> values) throws SQLException {
        Map<String,Object> row = new LinkedHashMap<>(values);
        if (Schema.IDENTITY.contains(table)) row.remove(primary);
        else if (Schema.KEYS.get(table).length == 1 && !primary.equals("VIN")) {
            if(usesReusableCodes()) {
                row.put(primary,prefix()+String.format(Locale.ROOT,"%03d",nextAvailableNumber(conn)));
            } else {
                try (Statement ps = conn.createStatement(); ResultSet rs = ps.executeQuery("SELECT NEXT VALUE FOR dbo.[Seq_" + table + "]")) {
                    rs.next(); row.put(primary,prefix() + String.format(Locale.ROOT,"%03d",rs.getInt(1)));
                }
            }
        }
        DataRules.validate(table,row,true);
        QuyTrinhDAO.guardWrite(conn,table,row,null);
        String fields = String.join(",",row.keySet().stream().map(s -> "[" + s + "]").toList());
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO dbo.[" + table + "](" + fields + ") VALUES(" + String.join(",",Collections.nCopies(row.size(),"?")) + ")")) {
            bind(ps,row.values()); ps.executeUpdate();
        }
        return String.valueOf(row.get(primary));
    }
    public void update(String id, Map<String,Object> row) throws SQLException {
        if (Schema.KEYS.get(table).length != 1) throw new SQLException("Cần đầy đủ khóa ghép để sửa chi tiết.");
        QuyTrinhDAO.transaction(conn -> { update(conn,Map.of(primary,id),row); return null; });
    }
    public void update(Connection conn, Map<String,Object> key, Map<String,Object> values) throws SQLException {
        Map<String,Object> row = new LinkedHashMap<>(values);
        for (String field : Schema.KEYS.get(table)) row.remove(field);
        DataRules.validate(table,row,false);
        QuyTrinhDAO.guardWrite(conn,table,row,key);
        if (row.isEmpty()) return;
        String fields = String.join(",",row.keySet().stream().map(s -> "[" + s + "]=?").toList());
        try (PreparedStatement ps = conn.prepareStatement("UPDATE dbo.[" + table + "] SET " + fields + " WHERE " + conditions(key))) {
            List<Object> params = new ArrayList<>(row.values()); params.addAll(key.values()); bind(ps,params);
            if (ps.executeUpdate() != 1) throw new SQLException("Bản ghi đã thay đổi hoặc không còn tồn tại.");
        }
    }
    public void softDelete(String id) throws SQLException { HiddenRecords.hide(table,id); }
    private String conditions(Map<String,Object> values) throws SQLException {
        for (String field : values.keySet()) if (!Schema.TYPES.get(table).containsKey(field)) throw new SQLException("Trường không hợp lệ: " + field);
        return String.join(" AND ",values.keySet().stream().map(s -> "[" + s + "]=?").toList());
    }
    static void bind(PreparedStatement ps, Collection<?> values) throws SQLException {
        int i=1; for (Object value : values) ps.setObject(i++,value);
    }
}
