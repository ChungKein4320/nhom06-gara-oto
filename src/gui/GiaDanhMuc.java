package gui;

import connectDB.ConnectDB;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import javax.swing.*;

/** Giá báo giá được lấy trực tiếp theo dịch vụ và phụ tùng đã chọn. */
final class GiaDanhMuc {
    private final List<Price> prices=new ArrayList<>();
    private GiaDanhMuc() {}
    static GiaDanhMuc bind(String table,Map<String,JComponent> inputs) {
        if(!table.equals("ChiTietPhieuBaoGia")) return null;
        GiaDanhMuc binding=new GiaDanhMuc();
        binding.add(inputs,"maDichVu","DichVu","chiPhiNhanCong");
        binding.add(inputs,"maPhuTung","PhuTung","donGiaPhuTung");
        return binding;
    }
    private void add(Map<String,JComponent> inputs,String key,String table,String field) {
        if(inputs.get(key) instanceof OChonThamChieu source && inputs.get(field) instanceof JTextField target) {
            DinhDangKeThua.lock(target); target.setToolTipText("Tự lấy đơn giá từ "+NhanCot.forField(key).toLowerCase());
            Price price=new Price(source,target,key,table,field); prices.add(price);
            source.addActionListener(e -> price.refresh()); price.refresh();
        }
    }
    void validate() throws SQLException { for(Price price:prices) price.validate(); }
    private static final class Price {
        final OChonThamChieu source;
        final JTextField target;
        final String key,table,field;
        String problem;
        Price(OChonThamChieu source,JTextField target,String key,String table,String field) { this.source=source; this.target=target; this.key=key; this.table=table; this.field=field; }
        BigDecimal fetch() throws SQLException {
            if(source.selectedId().isEmpty()) throw new SQLException("Vui lòng chọn "+NhanCot.forField(key).toLowerCase()+".");
            try(Connection conn=ConnectDB.getConnection(); PreparedStatement ps=conn.prepareStatement("SELECT donGia FROM dbo.["+table+"] WHERE ["+key+"]=?")) {
                ps.setString(1,source.selectedId());
                try(ResultSet rs=ps.executeQuery()) {
                    if(!rs.next()) throw new SQLException("Không tìm thấy "+NhanCot.forField(key).toLowerCase()+".");
                    BigDecimal price=rs.getBigDecimal("donGia");
                    if(price==null || price.signum()<0) throw new SQLException("Đơn giá danh mục không hợp lệ.");
                    return price;
                }
            }
        }
        void refresh() {
            problem=null; target.setText("");
            if(source.selectedId().isEmpty()) return;
            try { target.setText(fetch().toPlainString()); }
            catch(SQLException ex) { problem=ex.getMessage(); target.setToolTipText(problem); }
        }
        void validate() throws SQLException {
            if(problem!=null) { refresh(); if(problem!=null) throw new KiemTraBieuMau.Issue(key,problem); }
            BigDecimal current;
            try { current=fetch(); } catch(SQLException ex) { target.setText(""); throw new KiemTraBieuMau.Issue(key,ex.getMessage()); }
            if(target.getText().isBlank() || current.compareTo(new BigDecimal(target.getText()))!=0) {
                target.setText(current.toPlainString());
                throw new KiemTraBieuMau.Issue(field,"Giá danh mục đã thay đổi, giá mới đã được cập nhật. Vui lòng kiểm tra rồi lưu lại.");
            }
        }
    }
}
