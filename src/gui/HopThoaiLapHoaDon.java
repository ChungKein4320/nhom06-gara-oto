package gui;

import connectDB.ConnectDB;
import dao.WorkflowDAO;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

final class HopThoaiLapHoaDon {
    static void show(Component parent) {
        DefaultTableModel model=new DefaultTableModel(new String[]{"Mã phiếu sửa chữa","Khách hàng","Xe/VIN","Ngày hoàn thành"},0){public boolean isCellEditable(int r,int c){return false;}};
        String sql="SELECT s.maPSC,CONCAT(s.maKhachHang,N' — ',k.hoTenKH),s.VIN,s.ngayHoanThanh FROM dbo.PhieuSuaChua s JOIN dbo.KhachHang k ON k.maKhachHang=s.maKhachHang WHERE s.trangThai=N'Đã sửa xong' AND EXISTS(SELECT 1 FROM dbo.ChiTietPhieuSuaChua c WHERE c.maPSC=s.maPSC) AND NOT EXISTS(SELECT 1 FROM dbo.ChiTietPhieuSuaChua c WHERE c.maPSC=s.maPSC AND (c.trangThaiHangMuc<>N'Hoàn thành' OR c.ketQuaNghiemThu<>N'Đạt')) AND NOT EXISTS(SELECT 1 FROM dbo.HoaDon h WHERE h.maPSC=s.maPSC) ORDER BY s.maPSC";
        try(Connection conn=ConnectDB.getConnection();PreparedStatement ps=conn.prepareStatement(sql);ResultSet rs=ps.executeQuery()) {while(rs.next()) model.addRow(new Object[]{rs.getString(1),rs.getString(2),rs.getString(3),rs.getTimestamp(4)});}
        catch(SQLException ex){JOptionPane.showMessageDialog(parent,ex.getMessage(),"Không tải được phiếu sửa chữa",JOptionPane.ERROR_MESSAGE);return;}
        if(model.getRowCount()==0){JOptionPane.showMessageDialog(parent,"Không có phiếu đã sửa xong, nghiệm thu Đạt và chưa lập hóa đơn.");return;}
        JTable tblRepairs=GiaoDien.table(model);tblRepairs.setAutoCreateRowSorter(true);tblRepairs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel pnlForm=new JPanel(new BorderLayout(0,10)); JScrollPane scrList=new JScrollPane(tblRepairs);scrList.setPreferredSize(new Dimension(830,310));BoCucBang.install(tblRepairs,scrList);pnlForm.add(scrList);
        OChonThamChieu cmbEmployee=OChonThamChieu.create("HoaDon","maNhanVien");JPanel pnlEmployee=new JPanel(new BorderLayout(8,0));pnlEmployee.add(new JLabel("Nhân viên lập hóa đơn"),BorderLayout.WEST);pnlEmployee.add(cmbEmployee);pnlForm.add(pnlEmployee,BorderLayout.SOUTH);
        Object[] options={"Xác nhận lập hóa đơn","Hủy"};
        while(JOptionPane.showOptionDialog(parent,pnlForm,"Chọn phiếu sửa chữa đã nghiệm thu",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,null,options,options[0])==0){
            int row=tblRepairs.getSelectedRow();if(row<0){JOptionPane.showMessageDialog(parent,"Vui lòng chọn phiếu sửa chữa.");continue;}
            try{cmbEmployee.validateSelection();if(cmbEmployee.selectedId().isEmpty())throw new SQLException("Vui lòng chọn nhân viên.");String id=WorkflowDAO.createInvoice(String.valueOf(model.getValueAt(tblRepairs.convertRowIndexToModel(row),0)),cmbEmployee.selectedId(),null,null);JOptionPane.showMessageDialog(parent,"Đã lập hóa đơn "+id);return;}
            catch(SQLException ex){JOptionPane.showMessageDialog(parent,ex.getMessage(),"Không thể lập hóa đơn",JOptionPane.ERROR_MESSAGE);}
        }
    }
}
