package gui;

import dao.Schema;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import javax.swing.*;

final class BieuMauBanGhi extends JPanel {
    private static final long serialVersionUID = 1L;
    private final String table;
    private final boolean existing;
    private final Map<String,JComponent> inputs=new LinkedHashMap<>();
    private final TruongKeThua.Binding inherited;
    private final GiaDanhMuc prices;
    BieuMauBanGhi(String table,Map<String,Object> row,Set<String> excluded) {
        this(table,row,excluded,false);
    }
    BieuMauBanGhi(String table,Map<String,Object> row,Set<String> excluded,boolean readOnly) {
        super(new GridBagLayout()); this.table=table; existing=row!=null;
        setBackground(GiaoDien.WHITE); setBorder(BorderFactory.createEmptyBorder(8,10,8,10)); int index=0;
        for(String field:Schema.COLUMNS.get(table)) {
            if(excluded.contains(field)) continue;
            String type=Schema.TYPES.get(table).get(field).split(":",2)[0];
            Object old=row==null ? null : row.get(field); String value=old==null ? "" : old.toString();
            JComponent input;
            OChonThamChieu cmbReference=readOnly ? null : OChonThamChieu.create(table,field);
            JComboBox<String> cmbOptions=LuaChonBieuMau.create(table,field,value,existing);
            if(readOnly) {
                JTextField txtDisplay=GiaoDien.field("");
                if(old instanceof Boolean flag) value=flag ? "Nam" : "Nữ";
                else if(old instanceof java.sql.Timestamp date) value=date.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                else if(old instanceof java.sql.Date date) value=date.toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                txtDisplay.setText(value); txtDisplay.setToolTipText(value); DinhDangKeThua.lock(txtDisplay); input=txtDisplay;
            }
            else if(cmbReference!=null) { cmbReference.selectId(value); input=cmbReference; }
            else if(cmbOptions!=null) input=cmbOptions;
            else if(type.equals("date") || type.startsWith("datetime")) {
                if(!existing && Set.of("ngayLap","ngayTao","ngayNhap","ngayTiepNhan","ngayKiemTra").contains(field))
                    value=type.equals("date") ? LocalDate.now().toString() : new java.sql.Timestamp(System.currentTimeMillis()/1000*1000).toString();
                input=new OChonNgay(value);
                if(field.equals("ngayHoanThanh") || field.equals("thoiGianXacNhan")) setEnabledDeep(input,false);
            } else if(type.equals("bit")) {
                JPanel pnlGender=new JPanel(new FlowLayout(FlowLayout.LEFT,14,7)); pnlGender.setOpaque(false);
                ButtonGroup group=new ButtonGroup();
                for(String label:new String[]{"Nam","Nữ"}) {
                    JRadioButton radGender=new JRadioButton(label); radGender.setOpaque(false); radGender.setFont(GiaoDien.font(13,false));
                    group.add(radGender); pnlGender.add(radGender);
                    if(old!=null) radGender.setSelected(label.equals("Nam")== (Boolean.TRUE.equals(old) || value.equals("1") || value.equals("Nam")));
                }
                input=pnlGender;
            } else {
                JTextField txtValue=GiaoDien.field("");
                if(!existing && Set.of("soLuongPhuTung","soLuongNhap","soLuong").contains(field)) value="1";
                if(!existing && !field.equals("namSanXuat") && (type.equals("int") || type.equals("decimal")) && value.isEmpty()) value="0";
                txtValue.setText(value); input=txtValue;
            }
            inputs.put(field,input);
            GridBagConstraints c=new GridBagConstraints(); c.gridx=index%2; c.gridy=index/2; c.fill=GridBagConstraints.HORIZONTAL; c.weightx=1; c.insets=new Insets(6,8,6,8);
            JPanel pnlCell=new JPanel(new BorderLayout(0,5)); pnlCell.setOpaque(false);
            String label=NhanCot.forField(field)+(Schema.REQUIRED.get(table).contains(field) ? " *" : "");
            if(field.equals("thoiHanBaoHanh")) label+=" (tháng)";
            pnlCell.add(GiaoDien.label(label,12,true,GiaoDien.TEXT),BorderLayout.NORTH); input.setPreferredSize(new Dimension(260,38)); pnlCell.add(input,BorderLayout.CENTER); add(pnlCell,c); index++;
        }
        if(!readOnly && (table.equals("ChiTietPhieuBaoGia") || table.equals("ChiTietPhieuSuaChua"))
                && inputs.get("maPhuTung") instanceof OChonThamChieu part
                && inputs.get("soLuongPhuTung") instanceof JTextField quantity) {
            Runnable syncQuantity=() -> {
                boolean noPart="PT000".equals(part.selectedId());
                if(noPart) quantity.setText("0");
                quantity.setEnabled(!noPart);
                if(!noPart && quantity.getText().trim().equals("0")) quantity.setText("1");
            };
            part.addActionListener(e -> syncQuantity.run());
            syncQuantity.run();
        }
        inherited=readOnly ? null : TruongKeThua.bind(table,inputs);
        prices=readOnly ? null : GiaDanhMuc.bind(table,inputs);
    }
    Map<String,Object> read() throws SQLException {
        if(inherited!=null) inherited.validate();
        if(prices!=null) prices.validate();
        return KiemTraBieuMau.read(table,inputs,existing);
    }
    void removeHorizontalPadding() {
        setBorder(BorderFactory.createEmptyBorder(8,0,8,0));
        GridBagLayout layout=(GridBagLayout)getLayout();
        for(Component component:getComponents()) {
            GridBagConstraints constraints=layout.getConstraints(component);
            constraints.insets.left=0;
            constraints.insets.right=0;
            layout.setConstraints(component,constraints);
        }
    }
    static void setEnabledDeep(Component component, boolean enabled) {
        component.setEnabled(enabled);

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                setEnabledDeep(child, enabled);
            }
        }

        if (!enabled) {
            DinhDangKeThua.applyStyle(component);
        }
    }
    void lockField(String field) { if(inputs.get(field)!=null) DinhDangKeThua.lock(inputs.get(field)); }
    @Override public Dimension getPreferredSize() {
        Dimension d=super.getPreferredSize();
        if(getParent() instanceof JViewport vp && vp.getWidth()>0) return new Dimension(vp.getWidth(),d.height);
        return d;
    }
}
