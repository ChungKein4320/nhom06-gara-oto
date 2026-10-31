package gui;

import dao.CoSoDAO;
import dao.QuyTrinhDAO;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Form tiếp nhận xe đồng thời tra cứu hoặc tạo hồ sơ khách hàng và phương tiện. */
final class HopThoaiTiepNhanMoi {
    private HopThoaiTiepNhanMoi() { }

    static void show(Component parent) {
        JDialog dialog=new JDialog(SwingUtilities.getWindowAncestor(parent),"Phiếu tiếp nhận",Dialog.ModalityType.APPLICATION_MODAL);
        BieuMauBanGhi receiptForm=new BieuMauBanGhi("PhieuTiepNhan",null,Set.of("maPTN","maKhachHang","VIN","ngayTiepNhan"));
        receiptForm.removeHorizontalPadding();
        BieuMauBanGhi customerForm=new BieuMauBanGhi("KhachHang",null,Set.of("maKhachHang"));
        BieuMauBanGhi vehicleForm=new BieuMauBanGhi("PhuongTien",null,Set.of("soKM"));
        OChonThamChieu existingCustomer=OChonThamChieu.create("PhieuTiepNhan","maKhachHang");
        OChonThamChieu existingVehicle=OChonThamChieu.create("PhieuTiepNhan","VIN");

        JLabel lblReceiptCode=GiaoDien.label("Mã phiếu: "+nextCode("PhieuTiepNhan"),13,true,GiaoDien.TEAL);
        JLabel lblCustomerCode=GiaoDien.label("Mã khách hàng: "+nextCode("KhachHang"),12,false,GiaoDien.MUTED);

        ButtonGroup customerMode=new ButtonGroup();
        JRadioButton radExistingCustomer=modeButton("Khách hàng đã có hồ sơ",true,customerMode);
        JRadioButton radNewCustomer=modeButton("Tạo hồ sơ khách hàng mới",false,customerMode);
        JPanel customerCards=new JPanel(new CardLayout()); customerCards.setOpaque(false);
        JPanel existingCustomerPanel=new JPanel(new BorderLayout(0,5)); existingCustomerPanel.setOpaque(false);
        existingCustomerPanel.add(GiaoDien.label("Chọn khách hàng",12,true,GiaoDien.TEXT),BorderLayout.NORTH);
        existingCustomerPanel.add(compactChooser(existingCustomer),BorderLayout.CENTER);
        JPanel newCustomerPanel=new JPanel(new BorderLayout(0,4)); newCustomerPanel.setOpaque(false);
        newCustomerPanel.add(lblCustomerCode,BorderLayout.NORTH); newCustomerPanel.add(customerForm,BorderLayout.CENTER);
        customerCards.add(existingCustomerPanel,"existing"); customerCards.add(newCustomerPanel,"new");
        customerCards.setPreferredSize(existingCustomerPanel.getPreferredSize());
        JPanel customerModes=new JPanel(new FlowLayout(FlowLayout.LEFT,12,0)); customerModes.setOpaque(false);
        customerModes.add(radExistingCustomer); customerModes.add(radNewCustomer);
        JPanel customerCard=GiaoDien.card(new BorderLayout(0,10));
        JPanel customerContent=new JPanel(); customerContent.setOpaque(false); customerContent.setLayout(new BoxLayout(customerContent,BoxLayout.Y_AXIS));
        customerModes.setAlignmentX(Component.LEFT_ALIGNMENT); customerCards.setAlignmentX(Component.LEFT_ALIGNMENT);
        customerContent.add(customerModes); customerContent.add(Box.createVerticalStrut(8)); customerContent.add(customerCards);
        customerCard.add(GiaoDien.label("1. Thông tin khách hàng",15,true,GiaoDien.NAV),BorderLayout.NORTH);
        customerCard.add(customerContent,BorderLayout.CENTER);

        ButtonGroup vehicleMode=new ButtonGroup();
        JRadioButton radExistingVehicle=modeButton("Phương tiện đã có hồ sơ",true,vehicleMode);
        JRadioButton radNewVehicle=modeButton("Tạo hồ sơ phương tiện mới",false,vehicleMode);
        JPanel vehicleCards=new JPanel(new CardLayout()); vehicleCards.setOpaque(false);
        JPanel existingVehiclePanel=new JPanel(new BorderLayout(0,5)); existingVehiclePanel.setOpaque(false);
        existingVehiclePanel.add(GiaoDien.label("Phương tiện thuộc khách hàng đã chọn",12,true,GiaoDien.TEXT),BorderLayout.NORTH);
        existingVehiclePanel.add(compactChooser(existingVehicle),BorderLayout.CENTER);
        JPanel newVehiclePanel=new JPanel(new BorderLayout(0,4)); newVehiclePanel.setOpaque(false);
        newVehiclePanel.add(GiaoDien.label("Nhập VIN và thông tin xe. Số km hiện tại sẽ lấy từ ODO tiếp nhận.",11,false,GiaoDien.MUTED),BorderLayout.NORTH);
        newVehiclePanel.add(vehicleForm,BorderLayout.CENTER);
        vehicleCards.add(existingVehiclePanel,"existing"); vehicleCards.add(newVehiclePanel,"new");
        vehicleCards.setPreferredSize(existingVehiclePanel.getPreferredSize());
        JPanel vehicleModes=new JPanel(new FlowLayout(FlowLayout.LEFT,12,0)); vehicleModes.setOpaque(false);
        vehicleModes.add(radExistingVehicle); vehicleModes.add(radNewVehicle);
        JPanel vehicleCard=GiaoDien.card(new BorderLayout(0,10));
        JPanel vehicleContent=new JPanel(); vehicleContent.setOpaque(false); vehicleContent.setLayout(new BoxLayout(vehicleContent,BoxLayout.Y_AXIS));
        vehicleModes.setAlignmentX(Component.LEFT_ALIGNMENT); vehicleCards.setAlignmentX(Component.LEFT_ALIGNMENT);
        vehicleContent.add(vehicleModes); vehicleContent.add(Box.createVerticalStrut(8)); vehicleContent.add(vehicleCards);
        vehicleCard.add(GiaoDien.label("2. Thông tin phương tiện",15,true,GiaoDien.NAV),BorderLayout.NORTH);
        vehicleCard.add(vehicleContent,BorderLayout.CENTER);

        radExistingCustomer.addActionListener(e -> showCard(customerCards,"existing",existingCustomerPanel));
        radNewCustomer.addActionListener(e -> showCard(customerCards,"new",newCustomerPanel));
        radExistingVehicle.addActionListener(e -> showCard(vehicleCards,"existing",existingVehiclePanel));
        radNewVehicle.addActionListener(e -> showCard(vehicleCards,"new",newVehiclePanel));

        Runnable filterCustomerVehicles=() -> {
            existingVehicle.selectId("");
            try {
                existingVehicle.filterVehiclesByCustomer(existingCustomer.selectedId());
            } catch(SQLException ex) {
                JOptionPane.showMessageDialog(dialog,"Không tải được phương tiện của khách hàng: "+ex.getMessage(),"Lỗi tải danh sách",JOptionPane.ERROR_MESSAGE);
            }
        };
        existingCustomer.addActionListener(e -> filterCustomerVehicles.run());
        radExistingCustomer.addActionListener(e -> filterCustomerVehicles.run());
        radNewCustomer.addActionListener(e -> {
            existingVehicle.selectId("");
            try { existingVehicle.clearVehicleCustomerFilter(); }
            catch(SQLException ex) {
                JOptionPane.showMessageDialog(dialog,"Không tải được danh sách phương tiện: "+ex.getMessage(),"Lỗi tải danh sách",JOptionPane.ERROR_MESSAGE);
            }
        });
        try { existingVehicle.filterVehiclesByCustomer(""); }
        catch(SQLException ex) { existingVehicle.setToolTipText("Không tải được phương tiện: "+ex.getMessage()); }

        JPanel receiptCard=GiaoDien.card(new BorderLayout(0,5));
        receiptCard.add(GiaoDien.label("3. Thông tin tiếp nhận",15,true,GiaoDien.NAV),BorderLayout.NORTH);
        OChonNgay intakeDate=new OChonNgay(LocalDate.now().toString());
        JSpinner intakeTime=new JSpinner(new SpinnerDateModel(new Date(),null,null,Calendar.MINUTE));
        intakeTime.setEditor(new JSpinner.DateEditor(intakeTime,"HH:mm")); intakeTime.setPreferredSize(new Dimension(130,38));
        JPanel dateTime=new JPanel(new GridLayout(1,2,10,0)); dateTime.setOpaque(false);
        dateTime.add(labeled("Ngày tiếp nhận *",intakeDate)); dateTime.add(labeled("Giờ tiếp nhận *",intakeTime));
        JPanel receiptContent=new JPanel(new BorderLayout(0,4)); receiptContent.setOpaque(false);
        receiptContent.add(dateTime,BorderLayout.NORTH); receiptContent.add(receiptForm,BorderLayout.CENTER);
        receiptCard.add(receiptContent,BorderLayout.CENTER);

        JPanel codeRow=new JPanel(new FlowLayout(FlowLayout.LEFT,4,0)); codeRow.setOpaque(false); codeRow.add(lblReceiptCode);
        JPanel profiles=new JPanel(new GridLayout(1,2,10,0)); profiles.setOpaque(false); profiles.add(customerCard); profiles.add(vehicleCard);

        List<Map<String,Object>> details=new ArrayList<>();
        String[] detailColumns={"maDichVu","yeuCauDichVu"};
        DefaultTableModel model=new DefaultTableModel(NhanCot.forFields(detailColumns),0) {
            @Override public boolean isCellEditable(int row,int column) { return false; }
        };
        JTable table=GiaoDien.table(model); table.setAutoCreateRowSorter(true); table.setRowHeight(36);
        Runnable refresh=() -> { model.setRowCount(0); for(Map<String,Object> detail:details) model.addRow(new Object[]{detail.get("maDichVu"),detail.get("yeuCauDichVu")}); };
        JScrollPane scrDetails=new JScrollPane(table); scrDetails.setPreferredSize(new Dimension(1080,185));
        JPanel detailSection=GiaoDien.card(new BorderLayout(0,6));
        detailSection.add(GiaoDien.label("4. Hạng mục và yêu cầu dịch vụ",15,true,GiaoDien.NAV),BorderLayout.NORTH);
        detailSection.add(scrDetails,BorderLayout.CENTER);

        JButton btnAdd=GiaoDien.button("Thêm dịch vụ / yêu cầu",GiaoDien.TEAL,Color.WHITE);
        JButton btnEdit=GiaoDien.button("Sửa yêu cầu",GiaoDien.CREAM,GiaoDien.NAV);
        JButton btnRemove=GiaoDien.button("Bỏ yêu cầu",GiaoDien.CREAM,GiaoDien.NAV);
        JButton btnSave=GiaoDien.button("Lưu phiếu tiếp nhận",GiaoDien.TEAL,Color.WHITE);
        JButton btnClose=GiaoDien.button("Đóng",GiaoDien.CREAM,GiaoDien.NAV);
        btnAdd.addActionListener(e -> { Map<String,Object> row=editDetail(dialog,null); if(row!=null) { details.add(row); refresh.run(); } });
        btnEdit.addActionListener(e -> {
            int selected=table.getSelectedRow(); if(selected<0) { JOptionPane.showMessageDialog(dialog,"Chọn yêu cầu dịch vụ cần sửa."); return; }
            int index=table.convertRowIndexToModel(selected); Map<String,Object> row=editDetail(dialog,details.get(index));
            if(row!=null) { details.set(index,row); refresh.run(); }
        });
        btnRemove.addActionListener(e -> {
            int selected=table.getSelectedRow(); if(selected<0) { JOptionPane.showMessageDialog(dialog,"Chọn yêu cầu dịch vụ cần bỏ."); return; }
            details.remove(table.convertRowIndexToModel(selected)); refresh.run();
        });
        btnSave.addActionListener(e -> {
            btnSave.setEnabled(false);
            try {
                Map<String,Object> header=receiptForm.read();
                if(intakeDate.getDateText().isBlank()) throw new SQLException("Vui lòng chọn ngày tiếp nhận.");
                LocalDate date=LocalDate.parse(intakeDate.getDateText());
                String timeText=new java.text.SimpleDateFormat("HH:mm").format((Date)intakeTime.getValue());
                LocalTime time=LocalTime.parse(timeText);
                header.put("ngayTiepNhan",Timestamp.valueOf(LocalDateTime.of(date,time).withSecond(0).withNano(0)));
                boolean createCustomer=radNewCustomer.isSelected();
                String customerId=null; Map<String,Object> customer=null;
                if(createCustomer) customer=customerForm.read();
                else {
                    existingCustomer.validateSelection(); customerId=existingCustomer.selectedId();
                    if(customerId.isBlank()) throw new SQLException("Vui lòng chọn khách hàng hoặc chuyển sang tạo hồ sơ mới.");
                }
                boolean createVehicle=radNewVehicle.isSelected();
                String vin=null; Map<String,Object> vehicle=null;
                if(createVehicle) vehicle=vehicleForm.read();
                else {
                    existingVehicle.validateSelection(); vin=existingVehicle.selectedId();
                    if(vin.isBlank()) throw new SQLException("Vui lòng chọn phương tiện hoặc chuyển sang tạo hồ sơ mới.");
                }
                QuyTrinhDAO.IntakeResult created=QuyTrinhDAO.createIntake(header,createCustomer,customerId,customer,createVehicle,vin,vehicle,details);
                JOptionPane.showMessageDialog(dialog,"Đã lưu phiếu "+created.maPTN()+" cho khách hàng "+created.maKhachHang()+".","Tiếp nhận thành công",JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch(SQLException ex) {
                JOptionPane.showMessageDialog(dialog,KiemTraBieuMau.userMessage(ex,"PhieuTiepNhan"),"Không thể lưu phiếu tiếp nhận",JOptionPane.ERROR_MESSAGE);
            } catch(RuntimeException ex) {
                JOptionPane.showMessageDialog(dialog,ex.getMessage()==null?"Dữ liệu nhập chưa hợp lệ.":ex.getMessage(),"Không thể lưu phiếu tiếp nhận",JOptionPane.ERROR_MESSAGE);
            } finally { btnSave.setEnabled(true); }
        });
        btnClose.addActionListener(e -> dialog.dispose());

        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,2));
        buttons.add(btnAdd); buttons.add(btnEdit); buttons.add(btnRemove); buttons.add(btnSave); buttons.add(btnClose);
        JPanel body=new JPanel(new GridBagLayout()); body.setOpaque(false);
        addSection(body,codeRow,0);
        addSection(body,profiles,1);
        addSection(body,receiptCard,2);
        addSection(body,detailSection,3);
        JScrollPane scrBody=new JScrollPane(body); scrBody.setBorder(null);
        scrBody.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrBody.getVerticalScrollBar().setUnitIncrement(16);
        JPanel content=new JPanel(new BorderLayout(8,8)); content.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        content.add(scrBody,BorderLayout.CENTER); content.add(buttons,BorderLayout.SOUTH);
        dialog.setContentPane(content); dialog.setMinimumSize(new Dimension(1180,720)); dialog.setSize(1320,850); dialog.setLocationRelativeTo(parent); dialog.setVisible(true);
    }

    private static JRadioButton modeButton(String text,boolean selected,ButtonGroup group) {
        JRadioButton button=new JRadioButton(text,selected); button.setOpaque(false); button.setFont(GiaoDien.font(12,false)); group.add(button); return button;
    }

    private static JPanel labeled(String title,JComponent control) {
        JPanel panel=new JPanel(new BorderLayout(0,5)); panel.setOpaque(false);
        panel.add(GiaoDien.label(title,12,true,GiaoDien.TEXT),BorderLayout.NORTH); panel.add(control,BorderLayout.CENTER); return panel;
    }

    private static JPanel compactChooser(JComponent chooser) {
        chooser.setPreferredSize(new Dimension(420,40));
        chooser.setMinimumSize(new Dimension(300,40));
        chooser.setMaximumSize(new Dimension(420,40));
        chooser.setBackground(GiaoDien.WHITE);
        chooser.setForeground(GiaoDien.TEXT);
        chooser.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel holder=new JPanel(); holder.setOpaque(false);
        holder.setLayout(new BoxLayout(holder,BoxLayout.Y_AXIS));
        holder.add(chooser);
        holder.add(Box.createVerticalGlue());
        return holder;
    }

    private static void showCard(JPanel cards,String name,Component visibleCard) {
        ((CardLayout)cards.getLayout()).show(cards,name);
        cards.setPreferredSize(visibleCard.getPreferredSize());
        cards.revalidate();
        if(cards.getParent()!=null) cards.getParent().revalidate();
    }

    private static void addSection(JPanel parent,Component child,int row) {
        GridBagConstraints c=new GridBagConstraints();
        c.gridx=0; c.gridy=row; c.weightx=1; c.weighty=0; c.fill=GridBagConstraints.HORIZONTAL;
        c.anchor=GridBagConstraints.NORTHWEST; c.insets=new Insets(row==0?0:0,0,10,0);
        parent.add(child,c);
    }

    private static String nextCode(String table) {
        try { String code=new CoSoDAO(table).previewNextId(); return code.isBlank() ? "sẽ cấp khi lưu" : code; }
        catch(SQLException ex) { return "sẽ cấp khi lưu"; }
    }

    private static Map<String,Object> editDetail(Component parent,Map<String,Object> existing) {
        BieuMauBanGhi form=new BieuMauBanGhi("ChiTietPhieuTiepNhan",existing,Set.of("maPTN"));
        JScrollPane scroll=new JScrollPane(form); scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(650,150));
        while(JOptionPane.showConfirmDialog(parent,scroll,existing==null ? "Thêm yêu cầu dịch vụ" : "Sửa yêu cầu dịch vụ",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION) {
            try { return form.read(); }
            catch(SQLException ex) { JOptionPane.showMessageDialog(parent,ex.getMessage(),"Thông tin chưa hợp lệ",JOptionPane.ERROR_MESSAGE); }
        }
        return null;
    }
}
