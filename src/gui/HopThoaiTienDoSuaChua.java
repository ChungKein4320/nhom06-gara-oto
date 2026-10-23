package gui;

import connectDB.ConnectDB;
import dao.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;

/** Xác nhận từng hạng mục và bảo hành trước bước nghiệm thu. */
final class HopThoaiTienDoSuaChua {
    static void show(Component parent,String id) {
        JDialog winDialog=new JDialog(SwingUtilities.getWindowAncestor(parent),"Tiến độ sửa chữa "+id,Dialog.ModalityType.APPLICATION_MODAL);
        JPanel pnlRows=new JPanel(); pnlRows.setLayout(new BoxLayout(pnlRows,BoxLayout.Y_AXIS));
        JButton btnFinish=GiaoDien.button("Nghiệm thu đạt",GiaoDien.TEAL,Color.WHITE);
        JButton btnStart=GiaoDien.button("Bắt đầu sửa chữa",GiaoDien.CREAM,GiaoDien.NAV);
        Runnable[] load=new Runnable[1];
        load[0]=() -> {
            try(Connection conn=ConnectDB.getConnection()) {
                var headers=new BaseDAO("PhieuSuaChua").getAll(conn,Map.of("maPSC",id));
                if(headers.isEmpty()) throw new SQLException("Phiếu không còn tồn tại.");
                var header=headers.get(0); boolean ongoing="Đang sửa chữa".equals(header.get("trangThai"));
                btnStart.setVisible("Chờ sửa chữa".equals(header.get("trangThai")));
                var lines=new BaseDAO("ChiTietPhieuSuaChua").getAll(conn,Map.of("maPSC",id));
                pnlRows.removeAll(); pnlRows.add(new BieuMauBanGhi("PhieuSuaChua",header,Set.of(),true)); boolean all=!lines.isEmpty(),pending=false;
                for(var line:lines) {
                    boolean done="Hoàn thành".equals(line.get("trangThaiHangMuc")); all &= done;
                    pending |= !"Đạt".equals(line.get("ketQuaNghiemThu"));
                    JPanel pnlLine=GiaoDien.card(new BorderLayout(8,8));
                    String service=String.valueOf(line.get("maDichVu")),part=String.valueOf(line.get("maPhuTung"));
                    pnlLine.add(GiaoDien.label(service+" / "+part+" — "+line.get("trangThaiHangMuc")+" — Bảo hành: "+line.get("thoiHanBaoHanh")+" tháng",13,true,DinhDangKeThua.TEXT),BorderLayout.CENTER);
                    boolean needsParts="Thiếu phụ tùng".equals(line.get("trangThaiHangMuc"));
                    JButton btnDone=GiaoDien.button(done ? "Đã hoàn thành" : needsParts ? "Cấp phát phụ tùng" : "Xác nhận đã sửa",GiaoDien.CREAM,GiaoDien.NAV);
                    btnDone.setEnabled((needsParts && (ongoing || "Chờ sửa chữa".equals(header.get("trangThai")))) || (ongoing && "Đang sửa chữa".equals(line.get("trangThaiHangMuc"))));
                    btnDone.addActionListener(e -> {
                        if(needsParts) {
                            if(JOptionPane.showConfirmDialog(winDialog,"Cấp phát phụ tùng cho "+service+" / "+part+"?","Xác nhận cấp phát",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
                            try { CommerceDAO.allocate(id,service,part); load[0].run(); }
                            catch(SQLException ex) { JOptionPane.showMessageDialog(winDialog,ex.getMessage(),"Không thể cấp phát",JOptionPane.ERROR_MESSAGE); }
                            return;
                        }
                        JSpinner spnMonths=new JSpinner(new SpinnerNumberModel(((Number)line.get("thoiHanBaoHanh")).intValue(),0,1200,1));
                        JPanel pnlWarranty=new JPanel(new BorderLayout(8,8)); pnlWarranty.add(new JLabel("Thời hạn bảo hành hạng mục (tháng, 0 = không bảo hành)"),BorderLayout.NORTH); pnlWarranty.add(spnMonths);
                        if(JOptionPane.showConfirmDialog(winDialog,pnlWarranty,"Xác nhận đã sửa "+service+" / "+part,JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION) return;
                        try { spnMonths.commitEdit(); WorkflowDAO.completeRepairItem(id,service,part,((Number)spnMonths.getValue()).intValue()); load[0].run(); }
                        catch(Exception ex) { JOptionPane.showMessageDialog(winDialog,ex.getMessage(),"Không thể xác nhận",JOptionPane.ERROR_MESSAGE); }
                    });
                    pnlLine.add(btnDone,BorderLayout.EAST); pnlLine.setMaximumSize(new Dimension(Integer.MAX_VALUE,85)); pnlRows.add(pnlLine); pnlRows.add(Box.createVerticalStrut(6));
                }
                btnFinish.setEnabled((ongoing || "Đã sửa xong".equals(header.get("trangThai"))) && all && pending); pnlRows.revalidate(); pnlRows.repaint();
            } catch(SQLException ex) { btnStart.setEnabled(false); btnFinish.setEnabled(false); JOptionPane.showMessageDialog(winDialog,ex.getMessage(),"Không tải được phiếu",JOptionPane.ERROR_MESSAGE); }
        };
        btnStart.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(winDialog,"Bắt đầu sửa chữa phiếu "+id+"?","Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
            try { WorkflowDAO.startRepair(id); load[0].run(); }
            catch(SQLException ex) { JOptionPane.showMessageDialog(winDialog,ex.getMessage()); }
        });
        btnFinish.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(winDialog,"Xác nhận tất cả hạng mục nghiệm thu đạt và chuyển phiếu sang Đã sửa xong?","Nghiệm thu đạt",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
            try { WorkflowDAO.acceptRepair(id); winDialog.dispose(); } catch(SQLException ex) { JOptionPane.showMessageDialog(winDialog,ex.getMessage()); load[0].run(); }
        });
        JPanel pnlContent=new JPanel(new BorderLayout(8,8)); pnlContent.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JScrollPane scrRows=new JScrollPane(pnlRows); scrRows.setPreferredSize(new Dimension(840,510)); scrRows.getVerticalScrollBar().setUnitIncrement(20); pnlContent.add(scrRows);
        JPanel pnlButtons=new JPanel(new FlowLayout(FlowLayout.RIGHT)); JButton btnClose=GiaoDien.button("Đóng",GiaoDien.CREAM,GiaoDien.NAV); btnClose.addActionListener(e -> winDialog.dispose()); pnlButtons.add(btnStart); pnlButtons.add(btnFinish); pnlButtons.add(btnClose); pnlContent.add(pnlButtons,BorderLayout.SOUTH);
        winDialog.setContentPane(pnlContent); load[0].run(); winDialog.pack(); winDialog.setLocationRelativeTo(null); winDialog.setVisible(true);
    }
}
