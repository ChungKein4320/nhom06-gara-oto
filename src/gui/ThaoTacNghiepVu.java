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
        boolean direct=Set.of("PhieuTiepNhan","PhieuKiemTra","PhieuBaoGia","PhieuSuaChua","PhieuNhapKho").contains(table);
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
            if(action.equals("Hủy")) message="Bạn đã thu hồi ĐẦY ĐỦ phụ tùng đã cấp về kho và muốn hủy "+id+"? Tồn kho sẽ được hoàn lại.";
            if(JOptionPane.showConfirmDialog(parent,message,"Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
            btnApply.setEnabled(false);
            try {
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
                    case "Hủy" -> QuyTrinhDAO.cancelRepair(id);
                    default -> { }
                }
                refresh.run();
            } catch(SQLException ex) { JOptionPane.showMessageDialog(parent,ex.getMessage(),"Không thể thực hiện",JOptionPane.ERROR_MESSAGE); }
            finally { btnApply.setEnabled(true); }
        });
    }
    private static String choose(Component parent,String owner,String field,String title) {
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
