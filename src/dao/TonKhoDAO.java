package dao;

public class TonKhoDAO extends CoSoDAO {
    @Override
    public java.util.List<java.util.LinkedHashMap<String,Object>> getAll() throws java.sql.SQLException {
        var rows=new java.util.ArrayList<java.util.LinkedHashMap<String,Object>>();
        String sql="SELECT t.*,p.tenPhuTung FROM dbo.TonKho t LEFT JOIN dbo.PhuTung p ON p.maPhuTung=t.maPhuTung ORDER BY t.maTonKho";
        try(var conn=connectDB.KetNoiCSDL.getConnection(); var ps=conn.prepareStatement(sql); var rs=ps.executeQuery()) {
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

    /**
     * Cập nhật số lượng tồn kho tối thiểu cho một bản ghi tồn kho.
     * Chỉ cho phép NHÂN VIÊN KHO hoặc ADMIN thực hiện.
     * Tuyệt đối KHÔNG thay đổi soLuongHienTai, maPhuTung hay maTonKho.
     *
     * @param maTonKho mã bản ghi tồn kho (bắt buộc)
     * @param soLuongToiThieu mức tồn tối thiểu mới (>= 0)
     * @param role vai trò người thực hiện (KHO hoặc ADMIN)
     * @throws java.sql.SQLException nếu lỗi cơ sở dữ liệu
     * @throws SecurityException nếu vai trò không được phép
     * @throws IllegalArgumentException nếu tham số không hợp lệ
     */
    public void updateMinimumThreshold(String maTonKho, int soLuongToiThieu, gui.VaiTro role) throws java.sql.SQLException {
        if (role == null || (role != gui.VaiTro.ADMIN && role != gui.VaiTro.KHO)) {
            throw new SecurityException("Chỉ Nhân viên kho hoặc Quản trị viên mới có quyền cập nhật mức tồn kho tối thiểu.");
        }
        if (maTonKho == null || maTonKho.isBlank()) {
            throw new IllegalArgumentException("Mã tồn kho không được để trống.");
        }
        if (soLuongToiThieu < 0) {
            throw new IllegalArgumentException("Mức tồn tối thiểu không được nhỏ hơn 0.");
        }

        String sql = "UPDATE dbo.TonKho SET soLuongToiThieu = ? WHERE maTonKho = ?";
        try (var conn = connectDB.KetNoiCSDL.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soLuongToiThieu);
            ps.setString(2, maTonKho);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new java.sql.SQLException("Không tìm thấy bản ghi tồn kho " + maTonKho + " để cập nhật.");
            }
        }
    }
}
