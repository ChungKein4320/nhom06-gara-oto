package gui;

import connectDB.KetNoiCSDL;
import dao.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Nhập đầu phiếu và nhiều hạng mục, chỉ ghi khi nhấn Lưu toàn bộ phiếu. */
final class TrinhSuaPhieu {
    private TrinhSuaPhieu() {}
    static void show(Component parent,String table,Map<String,Object> header) {
        show(parent,table,header,List.of());
    }
    static void show(Component parent,String table,Map<String,Object> header,List<Map<String,Object>> initialDetails) {
        open(parent,table,header,initialDetails,false);
    }
    static void view(Component parent,String table,Map<String,Object> header) {
        open(parent,table,header,List.of(),true);
    }
    private static void open(Component parent,String table,Map<String,Object> header,List<Map<String,Object>> initialDetails,boolean viewOnly) {
        String child=QuyTrinhDAO.detailTable(table), pk=Schema.PRIMARY.get(table);
        String id=header==null || header.get(pk)==null ? null : String.valueOf(header.get(pk));
        if(id==null && (table.equals("PhieuSuaChua") || table.equals("HoaDon"))) { JOptionPane.showMessageDialog(parent,"Hãy lập phiếu bằng nút nghiệp vụ của phiếu trước đó."); return; }
        String lockedReason=null;
        List<Map<String,Object>> details=new ArrayList<>();
        if(id==null) for(Map<String,Object> line:initialDetails) details.add(new LinkedHashMap<>(line));
        try(Connection conn=KetNoiCSDL.getConnection()) {
            if(id!=null) {
                var fresh=new CoSoDAO(table).getAll(conn,Map.of(pk,id));
                if(fresh.isEmpty()) throw new SQLException("Phiếu không còn tồn tại. Vui lòng tải lại danh sách.");
                header=fresh.get(0);
                if(!viewOnly) lockedReason=QuyTrinhDAO.documentEditProblem(conn,table,id);
                details.addAll(new CoSoDAO(child).getAll(conn,Map.of(pk,id)));
            }
        } catch(SQLException ex) { error(parent,ex); return; }
        boolean readOnly=viewOnly || lockedReason!=null;
        BieuMauBanGhi pnlHeader=new BieuMauBanGhi(table,header,id==null ? Set.of(pk) : Set.of(),readOnly);
        if(!readOnly && id!=null) pnlHeader.lockField(pk);
        String[] columns=Arrays.stream(Schema.COLUMNS.get(child)).filter(f -> !f.equals(pk) && !f.equals("maChiTietHoaDon")).toArray(String[]::new);
        DefaultTableModel model=new DefaultTableModel(NhanCot.forFields(columns),0) { @Override public boolean isCellEditable(int r,int c) { return false; } };
        JTable tblDetails=GiaoDien.table(model); tblDetails.setAutoCreateRowSorter(true);
        Runnable refresh=() -> { model.setRowCount(0); for(Map<String,Object> row:details) model.addRow(Arrays.stream(columns).map(row::get).toArray()); };
        refresh.run();
        JDialog winDialog=new JDialog(SwingUtilities.getWindowAncestor(parent),(id==null ? "Lập phiếu mới" : readOnly ? "Chi tiết "+id : "Chỉnh sửa "+id),Dialog.ModalityType.APPLICATION_MODAL);
        JPanel pnlContent=new JPanel(new BorderLayout(0,10)); pnlContent.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JScrollPane scrHeader=new JScrollPane(pnlHeader);
        scrHeader.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrHeader.setPreferredSize(new Dimension(840,Math.min(310,pnlHeader.getPreferredSize().height+12)));
        JPanel pnlTop=new JPanel(new BorderLayout(0,8)); pnlTop.add(scrHeader,BorderLayout.CENTER);
        if(lockedReason!=null) pnlTop.add(GiaoDien.label("<html>Chỉ xem: "+lockedReason+"</html>",12,true,DinhDangKeThua.TEXT),BorderLayout.NORTH);
        pnlContent.add(pnlTop,BorderLayout.NORTH);
        JScrollPane scrDetails=new JScrollPane(tblDetails); scrDetails.setPreferredSize(new Dimension(840,240));
        JPanel pnlDetailList=new JPanel(new BorderLayout(0,6));
        pnlDetailList.add(new BoLocTrangThai(tblDetails),BorderLayout.NORTH); pnlDetailList.add(scrDetails,BorderLayout.CENTER);
        pnlContent.add(pnlDetailList,BorderLayout.CENTER);
        BoCucBang.install(tblDetails,scrDetails);
        JPanel pnlButtons=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,2));
        JButton btnAdd=GiaoDien.button("Thêm hạng mục",GiaoDien.TEAL,Color.WHITE);
        JButton btnEdit=GiaoDien.button("Sửa hạng mục",GiaoDien.CREAM,GiaoDien.NAV);
        JButton btnRemove=GiaoDien.button("Bỏ hạng mục",GiaoDien.CREAM,GiaoDien.NAV);
        JButton btnSave=GiaoDien.button(id==null ? "Lưu toàn bộ phiếu" : "Lưu thay đổi",GiaoDien.TEAL,Color.WHITE);
        JButton btnView=GiaoDien.button("Xem hạng mục",GiaoDien.CREAM,GiaoDien.NAV);
        btnView.addActionListener(e -> {
            int selected=tblDetails.getSelectedRow();
            if(selected<0) { JOptionPane.showMessageDialog(winDialog,"Vui lòng chọn hạng mục."); return; }
            BieuMauBanGhi pnlLine=new BieuMauBanGhi(child,details.get(tblDetails.convertRowIndexToModel(selected)),Set.of(),true);
            JScrollPane scrLine=new JScrollPane(pnlLine);
            scrLine.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrLine.setPreferredSize(new Dimension(660,Math.min(380,pnlLine.getPreferredSize().height+12)));
            JOptionPane.showMessageDialog(winDialog,scrLine,"Chi tiết hạng mục",JOptionPane.PLAIN_MESSAGE);
        });
        tblDetails.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent event) { if(event.getClickCount()==2) { if(readOnly) btnView.doClick(); else btnEdit.doClick(); } }
        });
        JButton btnClose=GiaoDien.button("Đóng",GiaoDien.CREAM,GiaoDien.NAV);
        btnAdd.addActionListener(e -> { Map<String,Object> row=editLine(winDialog,child,null,pk); if(row!=null) { details.add(row); refresh.run(); } });
        btnEdit.addActionListener(e -> {
            int selected=tblDetails.getSelectedRow(); if(selected<0) { JOptionPane.showMessageDialog(winDialog,"Vui lòng chọn hạng mục cần sửa."); return; }
            int index=tblDetails.convertRowIndexToModel(selected); Map<String,Object> row=editLine(winDialog,child,details.get(index),pk);
            if(row!=null) { details.set(index,row); refresh.run(); }
        });
        btnRemove.addActionListener(e -> { int selected=tblDetails.getSelectedRow(); if(selected>=0) { details.remove(tblDetails.convertRowIndexToModel(selected)); refresh.run(); } });
        btnSave.addActionListener(e -> {
            btnSave.setEnabled(false);
            try {
                QuyTrinhDAO.saveDocument(table,pnlHeader.read(),details,id); winDialog.dispose();
            } catch(SQLException ex) { error(winDialog,ex); }
            finally { btnSave.setEnabled(true); }
        });
        btnClose.addActionListener(e -> winDialog.dispose());
        pnlButtons.add(btnView);
        if(!readOnly) { pnlButtons.add(btnAdd); pnlButtons.add(btnEdit); pnlButtons.add(btnRemove); pnlButtons.add(btnSave); }
        pnlButtons.add(btnClose); pnlContent.add(pnlButtons,BorderLayout.SOUTH); winDialog.setContentPane(pnlContent); winDialog.pack(); winDialog.setLocationRelativeTo(null); winDialog.setVisible(true);
    }
    private static Map<String,Object> editLine(Component parent,String child,Map<String,Object> row,String foreignKey) {
        // Khóa hạng mục được nhập lại trong bản nháp; đầu phiếu sẽ gắn khi lưu.
        BieuMauBanGhi pnlForm=new BieuMauBanGhi(child,row,Set.of(foreignKey,"maChiTietHoaDon"));
        JScrollPane scrForm=new JScrollPane(pnlForm);
        scrForm.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrForm.setPreferredSize(new Dimension(660,Math.min(380,pnlForm.getPreferredSize().height+12)));
        Object[] options={"Áp dụng vào phiếu","Hủy"};
        while(JOptionPane.showOptionDialog(parent,scrForm,"Chỉnh sửa hạng mục — lưu phiếu để ghi nhận",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,null,options,options[0])==JOptionPane.OK_OPTION) {
            try {
                Map<String,Object> values=pnlForm.read();
                // KiemTraBieuMau bỏ khóa chính của bản ghi đang sửa; giữ lại khóa dịch vụ nếu cần.
                return values;
            } catch(SQLException ex) { error(parent,ex); }
        }
        return null;
    }
    private static void error(Component parent,SQLException ex) { JOptionPane.showMessageDialog(parent,ex.getMessage(),"Không thể lưu phiếu",JOptionPane.ERROR_MESSAGE); }
}
