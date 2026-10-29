package gui;

import dao.QuyTrinhDAO;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.function.Supplier;
import javax.swing.*;

/** Các thao tác gắn với phiếu đang chọn, không nhập tay trạng thái nghiệp vụ. */
final class ThaoTacNghiepVu {
    private ThaoTacNghiepVu() {}
    static void install(JPanel pnlButtons,String table,Supplier<Map<String,Object>> selection,Runnable refresh,Component parent) {
        String[] actions=switch(table) {
            case "PhieuTiepNhan" -> new String[]{"Lập phiếu kiểm tra"};
            case "PhieuKiemTra" -> new String[]{"Lập phiếu báo giá"};
            case "PhieuBaoGia" -> new String[]{"Xác nhận báo giá","Lập phiếu sửa chữa"};
            case "PhieuSuaChua" -> new String[]{"Hủy","Bàn giao"};
            case "PhieuNhapKho" -> new String[]{"Duyệt nhập kho","Từ chối nhập kho"};
            case "LichNhacBaoDuong" -> new String[]{"Ghi nhận đã gửi"};
            default -> new String[0];
        };
        if(actions.length==0) return;
        boolean direct = Set.of(
                "PhieuTiepNhan",
                "PhieuKiemTra",
                "PhieuBaoGia",
                "PhieuSuaChua",
                "PhieuNhapKho",
                "LichNhacBaoDuong"
        ).contains(table);
        if(direct) {
            for(String action:actions) installButton(pnlButtons,table,selection,refresh,parent,action,null);
        } else {
            JComboBox<String> cmbAction=new OChonTimKiem<>(actions); pnlButtons.add(cmbAction);
            installButton(pnlButtons,table,selection,refresh,parent,null,cmbAction);
        }
    }
    private static void installButton(JPanel pnlButtons,String table,Supplier<Map<String,Object>> selection,Runnable refresh,Component parent,String fixedAction,JComboBox<String> cmbAction) {
        JButton btnApply=GiaoDien.button(fixedAction==null ? "Thực hiện" : fixedAction,GiaoDien.TEAL,Color.WHITE); pnlButtons.add(btnApply);
        btnApply.addActionListener(e -> {
            Map<String,Object> row=selection.get(); if(row==null) return;
            String action=fixedAction!=null ? fixedAction : (String)cmbAction.getSelectedItem();
            if(action==null) { JOptionPane.showMessageDialog(parent,"Vui lòng chọn một nghiệp vụ trong danh sách."); return; }
            String id=String.valueOf(row.get(dao.Schema.PRIMARY.get(table)));
            String message=action+" cho "+id+"?";
            if(action.equals("Nghiệm thu đạt")) message="Xác nhận TẤT CẢ hạng mục đã hoàn thành và nghiệm thu Đạt cho "+id+"?";
            if(action.equals("Ghi nhận đã gửi")) message="Bạn đã gửi thông báo cho khách và muốn ghi nhận lịch này là Đã gửi?";
            if(action.equals("Hủy")) message="Hủy phiếu chỉ sau khi đối soát phần đã cấp phát và phần trả kho thực tế. Tiếp tục?";
            if(JOptionPane.showConfirmDialog(parent,message,"Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
            btnApply.setEnabled(false);
            try {
                String cancellationReason=null, manager=null;
                if(action.equals("Hủy")) {
                    CancellationDetails details=askCancellationDetails(parent,id);
                    if(details==null) return;
                    cancellationReason=details.reason();
                    manager=details.approverId();
                }
                switch(action) {
                    case "Lập phiếu kiểm tra" -> {
                        String employee=choose(parent,"NhanVien","maNhanVien","Nhân viên kiểm tra"); if(employee==null) return;
                        Map<String,Object> seed=new LinkedHashMap<>(); seed.put("maPTN",id); seed.put("VIN",row.get("VIN")); seed.put("maKhachHang",row.get("maKhachHang")); seed.put("maNhanVien",employee); seed.put("ngayKiemTra",new Timestamp(System.currentTimeMillis()));
                        TrinhSuaPhieu.show(parent,"PhieuKiemTra",seed);
                    }
                    case "Lập phiếu báo giá" -> {
                        String employee=choose(parent,"NhanVien","maNhanVien","Nhân viên lập báo giá"); if(employee==null) return;
                        Map<String,Object> seed=new LinkedHashMap<>(); seed.put("maPKT",id); seed.put("VIN",row.get("VIN")); seed.put("maNhanVien",employee); seed.put("ngayLap",new Timestamp(System.currentTimeMillis())); seed.put("trangThai","Còn hiệu lực");
                        TrinhSuaPhieu.show(parent,"PhieuBaoGia",seed);
                    }
                    case "Xác nhận báo giá" -> QuyTrinhDAO.confirmQuote(id);
                    case "Lập phiếu sửa chữa" -> {
                        String employee=choose(parent,"NhanVien","maNhanVien","Nhân viên phụ trách sửa chữa"); if(employee==null) return;
                        String created=QuyTrinhDAO.createRepair(id,employee);
                        JOptionPane.showMessageDialog(parent,"Đã tạo "+created+". Cấp phát phụ tùng trước khi bắt đầu sửa chữa.");
                    }
                    case "Bắt đầu sửa chữa" -> QuyTrinhDAO.startRepair(id);
                    case "Nghiệm thu đạt" -> QuyTrinhDAO.acceptRepair(id);
                    case "Duyệt nhập kho" -> QuyTrinhDAO.approveReceipt(id,true);
                    case "Từ chối nhập kho" -> QuyTrinhDAO.approveReceipt(id,false);
                    case "Ghi nhận đã gửi" -> QuyTrinhDAO.markReminder(id);
                    case "Bàn giao" -> QuyTrinhDAO.handover(id);
                    case "Hủy" -> QuyTrinhDAO.cancelRepair(id,cancellationReason,manager);
                    default -> { }
                }
                refresh.run();
            } catch(SQLException ex) { JOptionPane.showMessageDialog(parent,ex.getMessage(),"Không thể thực hiện",JOptionPane.ERROR_MESSAGE); }
            finally { btnApply.setEnabled(true); }
        });
    }
    private record CancellationDetails(String reason,String approverId) { }

    private static CancellationDetails askCancellationDetails(Component parent,String repairId) throws SQLException {
        JTextArea txtReason=new JTextArea(3,28);
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        OChonTimKiem<LuaChonDuLieu.Choice> cmbApprover=new OChonTimKiem<>();
        cmbApprover.setEditable(false);
        cmbApprover.setPreferredSize(new Dimension(360,32));
        LuaChonDuLieu.load(cmbApprover,
                "SELECT maNhanVien,CONCAT(maNhanVien,N' — ',hoTenNV) FROM dbo.NhanVien "
                        + "WHERE trangThai=N'Đang làm' AND vaiTro IN (N'Nhân viên quản lý',N'Quản trị viên') "
                        + "ORDER BY maNhanVien");
        if(cmbApprover.getItemCount()==0) {
            JOptionPane.showMessageDialog(parent,
                    "Không có quản lý hoặc quản trị viên đang làm việc để chọn duyệt hủy.",
                    "Không có người duyệt",JOptionPane.WARNING_MESSAGE);
            return null;
        }
        cmbApprover.insertItemAt(new LuaChonDuLieu.Choice("","— Chọn người duyệt hủy —"),0);
        cmbApprover.setSelectedIndex(0);

        JPanel form=new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        GridBagConstraints c=new GridBagConstraints();
        c.gridx=0; c.weightx=1; c.fill=GridBagConstraints.HORIZONTAL; c.anchor=GridBagConstraints.WEST;
        c.insets=new Insets(4,4,4,4);
        c.gridy=0; form.add(new JLabel("Lý do hủy phiếu:"),c);
        c.gridy=1; form.add(new JScrollPane(txtReason),c);
        c.gridy=2; form.add(new JLabel("Người duyệt hủy (quản lý/quản trị viên đang làm):"),c);
        c.gridy=3; form.add(cmbApprover,c);

        while(true) {
            int result=JOptionPane.showConfirmDialog(parent,form,"Hủy phiếu "+repairId,
                    JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
            if(result!=JOptionPane.OK_OPTION) return null;
            String reason=txtReason.getText().trim();
            String approver=LuaChonDuLieu.id(cmbApprover);
            if(reason.isBlank()) {
                JOptionPane.showMessageDialog(parent,"Vui lòng nhập lý do hủy phiếu.","Thiếu lý do hủy",JOptionPane.WARNING_MESSAGE);
                continue;
            }
            if(approver==null || approver.isBlank()) {
                JOptionPane.showMessageDialog(parent,"Vui lòng chọn người duyệt hủy.","Chưa chọn người duyệt",JOptionPane.WARNING_MESSAGE);
                continue;
            }
            return new CancellationDetails(reason,approver);
        }
    }

    static String choose(Component parent,String owner,String field,String title) {
        String selected=chooseOptional(parent,owner,field,title);
        if(selected!=null && selected.isBlank()) { JOptionPane.showMessageDialog(parent,"Vui lòng chọn "+NhanCot.forField(field).toLowerCase()+"."); return null; }
        return selected;
    }
    private static String chooseOptional(Component parent,String owner,String field,String title) {
        // Với NhanVien, dùng một bảng khác để trường này được nhận diện là khóa ngoại.
        OChonThamChieu cmbValue=OChonThamChieu.create(owner.equals("NhanVien") ? "PhieuTiepNhan" : owner,field);
        if(cmbValue==null) return null;
        JDialog winChoose=new JDialog(SwingUtilities.getWindowAncestor(parent),title,Dialog.ModalityType.APPLICATION_MODAL);
        JPanel pnlChoose=new JPanel(new BorderLayout(8,10)); pnlChoose.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        pnlChoose.add(new JLabel("Bấm chọn một kết quả để xác nhận ngay."),BorderLayout.NORTH);
        pnlChoose.add(cmbValue,BorderLayout.CENTER);
        JButton btnCancel=GiaoDien.button("Hủy",GiaoDien.CREAM,GiaoDien.NAV); btnCancel.addActionListener(e -> winChoose.dispose());
        pnlChoose.add(btnCancel,BorderLayout.SOUTH);
        String[] result={null};
        cmbValue.addActionListener(e -> {
            if(cmbValue.getSelectedItem()!=null && !cmbValue.selectedId().isBlank()) {
                result[0]=cmbValue.selectedId(); winChoose.dispose();
            }
        });
        winChoose.setContentPane(pnlChoose); winChoose.setMinimumSize(new Dimension(440,160)); winChoose.pack(); winChoose.setLocationRelativeTo(null); winChoose.setVisible(true);
        return result[0];
    }
}
