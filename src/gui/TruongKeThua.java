package gui;

import connectDB.KetNoiCSDL;
import dao.CoSoDAO;
import dao.Schema;
import java.sql.*;
import java.util.*;
import javax.swing.*;

/** Tự điền hồ sơ từ phiếu nguồn; không tạo thêm cột trong database. */
final class TruongKeThua {
    record Link(String field,String table,String... inherited) {}
    static Link link(String table) {
        return switch(table) {
            case "PhieuKiemTra" -> new Link("maPTN","PhieuTiepNhan","maKhachHang","VIN");
            case "PhieuBaoGia" -> new Link("maPKT","PhieuKiemTra","maKhachHang","VIN");
            case "PhieuSuaChua" -> new Link("maBaoGia","PhieuBaoGia","maKhachHang","VIN");
            case "HoaDon","LichBaoHanh","LichNhacBaoDuong" -> new Link("maPSC","PhieuSuaChua","maKhachHang","VIN");
            default -> null;
        };
    }
    static Binding bind(String table,Map<String,JComponent> inputs) {
        Link link=link(table);
        if(link==null || !(inputs.get(link.field()) instanceof OChonThamChieu source)) return null;
        Binding binding=new Binding(link,inputs,source);
        source.addActionListener(e -> binding.refresh()); binding.refresh(); return binding;
    }
    static final class Binding {
        final JLabel lblSummary=new JLabel();
        private final Link link;
        private final Map<String,JComponent> inputs;
        private final OChonThamChieu source;
        private String loadedId="";
        private String problem;
        private boolean updating;
        Binding(Link link,Map<String,JComponent> inputs,OChonThamChieu source) {
            this.link=link; this.inputs=inputs; this.source=source;
            lblSummary.setFont(GiaoDien.font(12,false)); lblSummary.setForeground(GiaoDien.TEAL);
            for(String field:link.inherited()) {
                JComponent control=inputs.get(field);
                if(control!=null) { DinhDangKeThua.lock(control); control.setToolTipText("Tự lấy từ "+NhanCot.forField(link.field()).toLowerCase()); }
            }
        }
        void refresh() {
            if(updating) return; updating=true;
            String id=source.selectedId(); loadedId=""; problem=null;
            for(String field:link.inherited()) set(field,"");
            if(id.isEmpty()) { lblSummary.setText("Chọn phiếu nguồn để tự điền khách hàng và xe."); updating=false; return; }
            try(Connection conn=KetNoiCSDL.getConnection()) {
                Map<String,Object> origin=load(conn,link.table(),id);
                Map<String,Object> receipt=origin; String current=link.table();
                while(!current.equals("PhieuTiepNhan")) {
                    Link previous=link(current);
                    if(previous==null) break;
                    receipt=load(conn,previous.table(),receipt.get(previous.field())); current=previous.table();
                }
                for(String field:link.inherited()) set(field,Objects.toString(origin.getOrDefault(field,receipt.get(field)),""));
                JComponent employee=inputs.get("maNhanVien");
                if(employee instanceof OChonThamChieu cmbEmployee && cmbEmployee.selectedId().isEmpty()) cmbEmployee.selectId(Objects.toString(origin.get("maNhanVien"),""));
                String customer=Objects.toString(receipt.get("maKhachHang"),""); String vin=Objects.toString(receipt.get("VIN"),"");
                String info="Khách: "+customer+" | VIN: "+vin+" | ODO: "+Objects.toString(receipt.get("soODO"),"—");
                try(PreparedStatement ps=conn.prepareStatement("SELECT k.hoTenKH,v.bienSo,v.hangXe,v.dongXe FROM dbo.KhachHang k CROSS JOIN dbo.PhuongTien v WHERE k.maKhachHang=? AND v.VIN=?")) {
                    ps.setString(1,customer); ps.setString(2,vin);
                    try(ResultSet rs=ps.executeQuery()) { if(rs.next()) info="Khách: "+customer+" — "+rs.getString(1)+" | Xe: "+rs.getString(2)+" — "+rs.getString(3)+" "+rs.getString(4)+" | ODO: "+Objects.toString(receipt.get("soODO"),"—"); }
                }
                lblSummary.setText("<html>"+escape(info)+"<br>Thông tin hồ sơ tự điền; nhân viên thực hiện có thể chọn riêng.</html>");
                loadedId=id;
            } catch(SQLException ex) {
                problem="Không lấy được hồ sơ phiếu nguồn: "+ex.getMessage();
                for(String field:link.inherited()) set(field,""); lblSummary.setText("Không tải được hồ sơ. Vui lòng chọn lại phiếu nguồn."); lblSummary.setToolTipText(problem);
            } finally { updating=false; }
        }
        void validate() throws KiemTraBieuMau.Issue {
            if(!source.selectedId().equals(loadedId)) refresh();
            if(problem!=null || loadedId.isEmpty()) throw new KiemTraBieuMau.Issue(link.field(),problem==null ? "Vui lòng chọn phiếu nguồn hợp lệ." : problem);
        }
        private void set(String field,String value) {
            JComponent control=inputs.get(field);
            if(control instanceof OChonThamChieu cmbValue) cmbValue.selectId(value);
            else if(control instanceof JTextField txtValue) txtValue.setText(value);
        }
    }
    private static Map<String,Object> load(Connection conn,String table,Object id) throws SQLException {
        var rows=new CoSoDAO(table).getAll(conn,Map.of(Schema.PRIMARY.get(table),id));
        if(rows.isEmpty()) throw new SQLException("Không tìm thấy "+table+": "+id);
        return rows.get(0);
    }
    private static String escape(String text) { return text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;"); }
}
