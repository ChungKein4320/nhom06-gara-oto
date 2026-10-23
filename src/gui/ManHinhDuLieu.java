package gui;

import dao.BaseDAO;
import dao.CommerceDAO;
import dao.Schema;
import dao.WorkflowDAO;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/** Danh sách và biểu mẫu sử dụng dữ liệu SQL Server thực tế. */
public final class ManHinhDuLieu extends JPanel {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private static final Map<String, String> ROUTES = new LinkedHashMap<>();
    static {
        ROUTES.put("Phiếu tiếp nhận", "PhieuTiepNhan");
        ROUTES.put("Phiếu kiểm tra", "PhieuKiemTra");
        ROUTES.put("Phiếu báo giá", "PhieuBaoGia");
        ROUTES.put("Phiếu sửa chữa", "PhieuSuaChua");
        ROUTES.put("Bảo hành", "LichBaoHanh");
        ROUTES.put("Lịch nhắc bảo dưỡng", "LichNhacBaoDuong");
        ROUTES.put("Tồn kho", "TonKho");
        ROUTES.put("Cấp phát phụ tùng", "ChiTietPhieuSuaChua");
        ROUTES.put("Nhập kho phụ tùng", "PhieuNhapKho");
        ROUTES.put("Lịch sử sửa chữa", "PhieuSuaChua");
        ROUTES.put("Hóa đơn", "HoaDon");
        ROUTES.put("Khuyến mãi/Tích điểm", "LichSuTichDiem");
        ROUTES.put("Phụ tùng", "PhuTung");
        ROUTES.put("Nhà cung cấp", "NhaCungCap");
        ROUTES.put("Nhân viên", "NhanVien");
        ROUTES.put("Khách hàng", "KhachHang");
        ROUTES.put("Phương tiện", "PhuongTien");
        ROUTES.put("Danh mục dịch vụ", "DichVu");
        ROUTES.put("Danh mục phụ tùng", "PhuTung");
        ROUTES.put("Tài khoản", "TaiKhoan");
        ROUTES.put("Khuyến mãi", "MaGiamGia");
    }
    public static String tableFor(String page) {
        return ROUTES.get(page);
    }
    private final String tableName;
    private transient BaseDAO dao;
    private final JTable tblTable;
    private final DefaultTableModel model;
    private final JTextField txtSearch;
    private final JLabel lblCount;
    private final String[] fields;
    private final String[] displayFields;
    private final BoLocTrangThai statusFilters;

    @java.io.Serial
    private void readObject(java.io.ObjectInputStream input) throws java.io.IOException, ClassNotFoundException {
        input.defaultReadObject();
        dao = tableName.equals("TonKho") ? new dao.TonKhoDAO() : tableName.equals("HoaDon") ? new dao.HoaDonDAO() : new BaseDAO(tableName);
    }

    public ManHinhDuLieu(String page) {
        setLayout(new BorderLayout(0, 18));
        setBackground(GiaoDien.BG);
        setBorder(new EmptyBorder(26, 26, 26, 26));
        tableName = ROUTES.get(page);
        if (tableName == null) throw new IllegalArgumentException("Chưa có nguồn dữ liệu cho: " + page);
        dao = tableName.equals("TonKho") ? new dao.TonKhoDAO() : tableName.equals("HoaDon") ? new dao.HoaDonDAO() : new BaseDAO(tableName);
        fields = Schema.COLUMNS.get(tableName);
        displayFields = tableName.equals("TonKho")
                ? new String[]{"maTonKho","maPhuTung","tenPhuTung","soLuongHienTai","soLuongToiThieu"}
                : tableName.equals("HoaDon") ? new String[]{"maHoaDon","khachHangHienThi","maPSC","ngayLap","tongThanhToan","trangThaiThanhToan"} : fields;

        JPanel pnlHeading = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlHeading.setOpaque(false);
        JLabel lblPath = GiaoDien.label("THINKING AUTO/" + page.toUpperCase(), 11, true, GiaoDien.TEAL);
        JLabel lblTitle = GiaoDien.label(page, 26, true, GiaoDien.NAV);
        for (JLabel lblLabel : new JLabel[] {lblPath, lblTitle}) {
            lblLabel.setHorizontalAlignment(SwingConstants.CENTER);
            pnlHeading.add(lblLabel);
        }
        add(pnlHeading, BorderLayout.NORTH);

        JPanel pnlCard = GiaoDien.card(new BorderLayout(0, 14));
        JPanel pnlToolbar = new JPanel(new BorderLayout(12, 0));
        pnlToolbar.setOpaque(false);
        txtSearch = GiaoDien.field("Tìm theo mã, tên hoặc thuộc tính...");
        txtSearch.setToolTipText("Nhập nội dung cần tìm trong bảng");
        JPanel pnlInput = new JPanel(new BorderLayout(8, 0));
        pnlInput.setOpaque(false);
        JLabel lblSearchLabel = GiaoDien.label("Tìm kiếm", 12, true, GiaoDien.TEAL);
        lblSearchLabel.setIcon(BieuTuong.get("Tìm kiếm", 16, GiaoDien.TEAL));
        lblSearchLabel.setIconTextGap(6);
        pnlInput.add(lblSearchLabel, BorderLayout.WEST);
        pnlInput.add(txtSearch, BorderLayout.CENTER);
        pnlToolbar.add(pnlInput, BorderLayout.CENTER);
        JButton btnAdd = GiaoDien.button("Thêm mới", GiaoDien.TEAL, Color.WHITE);
        btnAdd.setEnabled(!tableName.startsWith("ChiTiet") && !Set.of("PhieuSuaChua", "HoaDon", "LichSuTichDiem").contains(tableName));
        btnAdd.addActionListener(e -> edit(false));
        if(tableName.equals("HoaDon")) {
            JButton btnCreateInvoice=GiaoDien.button("Lập hóa đơn",GiaoDien.TEAL,Color.WHITE);
            btnCreateInvoice.addActionListener(e -> {HopThoaiLapHoaDon.show(this);load();});pnlToolbar.add(btnCreateInvoice,BorderLayout.EAST);
        } else if(!tableName.equals("TonKho")) pnlToolbar.add(btnAdd, BorderLayout.EAST);
        pnlCard.add(pnlToolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(NhanCot.forFields(displayFields), 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tblTable = GiaoDien.table(model);
        if(tableName.equals("TonKho")) {
            tblTable.putClientProperty("tableLayout.fixedWidths",Map.of(2,220));
            tblTable.getColumnModel().getColumn(2).setMaxWidth(220);
        }
        tblTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblTable.setAutoCreateRowSorter(true);
        statusFilters=new BoLocTrangThai(tblTable);
        statusFilters.onChange(this::updateCount);
        pnlToolbar.add(statusFilters,BorderLayout.SOUTH);
        tblTable.setRowHeight(40);
        tblTable.getTableHeader().setResizingAllowed(true);
        JScrollPane scrScroll = new JScrollPane(tblTable);
        scrScroll.setPreferredSize(new Dimension(850, 440));
        BoCucBang.install(tblTable, scrScroll);
        pnlCard.add(scrScroll, BorderLayout.CENTER);
        JPanel pnlActions = new JPanel(new BorderLayout());
        pnlActions.setOpaque(false);
        lblCount = GiaoDien.label("Đang tải dữ liệu...", 12, false, GiaoDien.MUTED);
        pnlActions.add(lblCount, BorderLayout.WEST);
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlButtons.setOpaque(false);
        JButton btnRefresh = GiaoDien.button("Làm mới", GiaoDien.CREAM, GiaoDien.NAV);
        JButton btnEdit = GiaoDien.button("Chỉnh sửa", GiaoDien.TEAL, Color.WHITE);
        JButton btnDelete = GiaoDien.button("Xóa", GiaoDien.ORANGE, Color.WHITE);
        btnEdit.setEnabled(!tableName.startsWith("ChiTiet") && !Set.of("HoaDon","LichSuTichDiem").contains(tableName));
        btnDelete.setEnabled(!tableName.startsWith("ChiTiet") && WorkflowDAO.detailTable(tableName)==null && !tableName.equals("LichSuTichDiem"));
        btnRefresh.addActionListener(e -> load());
        btnEdit.addActionListener(e -> edit(true));
        btnDelete.addActionListener(e -> hideRecord());
        pnlButtons.add(btnRefresh);
        if (WorkflowDAO.detailTable(tableName) != null) {
            JButton btnDetails = GiaoDien.button(tableName.equals("HoaDon") ? "Chi tiết hóa đơn" : "Chi tiết phiếu", GiaoDien.CREAM, GiaoDien.NAV);
            btnDetails.addActionListener(e -> openDocument());
            pnlButtons.add(btnDetails);
        }
        ThaoTacNghiepVu.install(pnlButtons, tableName, this::selectedRecord, this::load, this);
        if(page.equals("Cấp phát phụ tùng")) {
            JButton btnAllocate=GiaoDien.button("Xác nhận cấp phát",GiaoDien.TEAL,Color.WHITE);
            btnAllocate.addActionListener(e -> allocate());
            pnlButtons.add(btnAllocate);
        }
        if(!Set.of("TonKho","HoaDon").contains(tableName)) {
            pnlButtons.add(btnEdit);
            pnlButtons.add(btnDelete);
        }
        JScrollPane scrActions = new JScrollPane(pnlButtons,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrActions.setBorder(null);
        scrActions.setPreferredSize(new Dimension(850,65));
        pnlActions.add(scrActions, BorderLayout.SOUTH);
        pnlCard.add(pnlActions, BorderLayout.SOUTH);
        add(pnlCard, BorderLayout.CENTER);
        if(tableName.equals("HoaDon")) tblTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent event) { if(event.getClickCount()==2 && tblTable.rowAtPoint(event.getPoint())>=0) openDocument(); }
        });
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });
        load();
    }

    private void load() {
        try {
            model.setRowCount(0);
            List<LinkedHashMap<String, Object>> rows = dao.getAll();
            for (Map<String, Object> row : rows) {
                if(tableName.equals("HoaDon")) {
                    java.math.BigDecimal total=java.math.BigDecimal.ZERO;
                    for(String field:List.of("tienPhuTung","tienCong","dichVuKhac","thueVAT")) total=total.add((java.math.BigDecimal)row.get(field));
                    row.put("tongThanhToan",total.subtract((java.math.BigDecimal)row.get("giamGia")));
                }
                Object[] values = new Object[displayFields.length];
                for (int i = 0; i < displayFields.length; i++) values[i] = displayFields[i].equals("gioiTinh")
                        ? (Boolean.TRUE.equals(row.get(displayFields[i])) || "1".equals(String.valueOf(row.get(displayFields[i]))) ? "Nam" : "Nữ") : row.get(displayFields[i]);
                if(tableName.equals("LichSuTichDiem")) {
                    int reasonColumn=Arrays.asList(displayFields).indexOf("lyDo");
                    if(reasonColumn>=0) values[reasonColumn]="Ghi nhận tích điểm từ "+Objects.toString(row.get("maHoaDon"),"—");
                }
                model.addRow(values);
            }
            statusFilters.refresh();
        } catch (SQLException ex) {
            lblCount.setText("Lỗi kết nối SQL Server");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filter() {
        statusFilters.search(txtSearch.getText());
    }
    private void updateCount() { lblCount.setText("Hiển thị: "+tblTable.getRowCount()+" / "+model.getRowCount()+" bản ghi"); }

    private void allocate() {
        int selected = tblTable.getSelectedRow();
        if (selected < 0) { JOptionPane.showMessageDialog(this, "Vui lòng chọn hạng mục cần cấp phát."); return; }
        int row = tblTable.convertRowIndexToModel(selected);
        if (JOptionPane.showConfirmDialog(this, "Xác nhận cấp phát phụ tùng cho hạng mục đã chọn?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            CommerceDAO.allocate(String.valueOf(model.getValueAt(row, 0)), String.valueOf(model.getValueAt(row, 1)), String.valueOf(model.getValueAt(row, 2)));
            load();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể cấp phát", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void edit(boolean existing) {
        if(existing && tableName.equals("PhieuSuaChua")) {
            Map<String,Object> record=selectedRecord();if(record==null)return;
            if(Set.of("Chờ sửa chữa","Đang sửa chữa","Đã sửa xong").contains(String.valueOf(record.get("trangThai")))) {HopThoaiTienDoSuaChua.show(this,String.valueOf(record.get("maPSC")));load();return;}
        }
        if (WorkflowDAO.detailTable(tableName) != null) {
            Map<String,Object> record = existing ? selectedRecord() : null;
            if (existing && record == null) return;
            TrinhSuaPhieu.show(this,tableName,record);
            load(); return;
        }
        int row = tblTable.getSelectedRow();
        if (existing && row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi.");
            return;
        }
        int source = existing ? tblTable.convertRowIndexToModel(row) : -1;
        String generatedId = null;
        if (!existing && !fields[0].equals("VIN")) {
            try {
                generatedId = dao.previewNextId();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể sinh mã", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(20, 22, 12, 22));
        Map<String, JComponent> inputs = new LinkedHashMap<>();
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            int column = Arrays.asList(displayFields).indexOf(field);
            String value = existing && model.getValueAt(source, column) != null
                    ? model.getValueAt(source, column).toString() : "";
            String type = Schema.TYPES.get(tableName).get(field).split(":")[0];
            JComponent input;
        OChonThamChieu cmbReference = OChonThamChieu.create(tableName, field);
            JComboBox<String> cmbOptions = LuaChonBieuMau.create(tableName, field, value, existing);
            if (cmbReference != null) {
                cmbReference.selectId(value);
                input = cmbReference;
            } else if (cmbOptions != null) {
                input = cmbOptions;
            } else if (type.equals("datetime") || type.equals("datetime2") || type.equals("date")) {
                input = new OChonNgay(value);
            } else if (field.equalsIgnoreCase("gioiTinh")) {
                JPanel pnlGioiTinh = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
                pnlGioiTinh.setOpaque(false);
                JRadioButton radNam = new JRadioButton("Nam");
                JRadioButton radNu = new JRadioButton("Nữ");
                ButtonGroup group = new ButtonGroup();
                group.add(radNam);
                group.add(radNu);
                radNam.setSelected(existing && !value.equalsIgnoreCase("Nữ"));
                radNu.setSelected(value.equalsIgnoreCase("Nữ"));
                pnlGioiTinh.add(radNam);
                pnlGioiTinh.add(radNu);
                input = pnlGioiTinh;
            } else {
                JTextField txtInput = GiaoDien.field(value);
                txtInput.setText(value);
                boolean auto = i == 0 && !field.equals("VIN") && !existing;
                if (auto) txtInput.setText(generatedId);
                if (i == 0) txtInput.setEditable(field.equals("VIN") && !existing);
                if (auto || (i == 0 && existing)) txtInput.setBackground(new Color(0xE5F0EE));
                input = txtInput;
            }
            inputs.put(field, input);
            JPanel pnlCell = new JPanel(new BorderLayout(0, 7));
            pnlCell.setOpaque(false);
            pnlCell.add(GiaoDien.label(NhanCot.forField(field) + (Schema.REQUIRED.get(tableName).contains(field) ? " *" : ""),
                    12, true, GiaoDien.TEXT), BorderLayout.NORTH);
            input.setPreferredSize(new Dimension(250, 40));
            pnlCell.add(input, BorderLayout.CENTER);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = i % 2;
            gbc.gridy = i / 2;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, i % 2 == 0 ? 0 : 9, 16, i % 2 == 0 ? 9 : 0);
            pnlForm.add(pnlCell, gbc);
        }
        TruongKeThua.Binding inherited = TruongKeThua.bind(tableName,inputs);
        JScrollPane scrSc = new JScrollPane(pnlForm);
        scrSc.setBorder(null);
        scrSc.setPreferredSize(new Dimension(690, Math.min(520, ((fields.length + 1) / 2) * 80 + 25)));
        JDialog winDialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                existing ? "Chỉnh sửa thông tin" : "Thêm thông tin mới", Dialog.ModalityType.APPLICATION_MODAL);
        winDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel pnlContent = new JPanel(new BorderLayout(0, 12));
        pnlContent.setBorder(new EmptyBorder(12, 12, 12, 12));
        pnlContent.add(scrSc, BorderLayout.CENTER);
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = GiaoDien.button("Lưu", GiaoDien.TEAL, Color.WHITE);
        JButton btnCancel = GiaoDien.button("Hủy", GiaoDien.CREAM, GiaoDien.NAV);
        pnlActions.add(btnSave);
        pnlActions.add(btnCancel);
        pnlContent.add(pnlActions, BorderLayout.SOUTH);
        winDialog.setContentPane(pnlContent);
        winDialog.getRootPane().setDefaultButton(btnSave);
        winDialog.getRootPane().registerKeyboardAction(e -> winDialog.dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        btnCancel.addActionListener(e -> winDialog.dispose());
        String[] expectedId = {generatedId};
        Map<JComponent, javax.swing.border.Border> borders = new LinkedHashMap<>();
        for (JComponent input : inputs.values()) borders.put(input, input.getBorder());
        btnSave.addActionListener(e -> {
            btnSave.setEnabled(false);
            borders.forEach(JComponent::setBorder);
            try {
                if(inherited!=null) inherited.validate();
                Map<String, Object> values = KiemTraBieuMau.read(tableName, inputs, existing);
                if (existing) dao.update(model.getValueAt(source, 0).toString(), values);
                else dao.insert(values, expectedId[0]);
                winDialog.dispose();
                load();
            } catch (Exception ex) {
                String field = KiemTraBieuMau.errorField(ex, tableName);
                String message = ex.getMessage();
                if (!existing && fields[0].equals(field) && !field.equals("VIN")) {
                    try {
                        expectedId[0] = dao.previewNextId();
                        ((JTextField) inputs.get(field)).setText(expectedId[0]);
                        message = "Mã đã thay đổi. Mã mới là " + expectedId[0] + ". Dữ liệu bạn nhập vẫn được giữ nguyên; vui lòng nhấn Lưu lại.";
                    } catch (SQLException refreshError) {
                        message = message + "\nKhông lấy được mã mới: " + refreshError.getMessage();
                    }
                } else if (ex instanceof SQLException sql && field != null) {
                    String label = NhanCot.forField(field);
                    if (sql.getErrorCode() == 2601 || sql.getErrorCode() == 2627) message = label + " đã tồn tại. Vui lòng nhập giá trị khác.";
                    else if (sql.getErrorCode() == 547 && message != null && message.contains("FK_")) message = label + " không tồn tại hoặc không hợp lệ. Vui lòng kiểm tra lại.";
                }
                JComponent invalid = inputs.get(field);
                if (invalid != null) invalid.setBorder(BorderFactory.createLineBorder(GiaoDien.ORANGE, 2));
                JOptionPane.showMessageDialog(winDialog, message == null ? "Không thể lưu. Vui lòng kiểm tra lại thông tin." : message,
                        "Không thể lưu", JOptionPane.ERROR_MESSAGE);
                JComponent target = invalid;
                if (target == null || !target.isEnabled() || (target instanceof JTextField txtText && !txtText.isEditable())) {
                    target = inputs.values().stream().filter(input -> input.isEnabled()
                            && (!(input instanceof JTextField txtText) || txtText.isEditable())).findFirst().orElse(btnSave);
                }
                JComponent focus = target;
                SwingUtilities.invokeLater(() -> {
                    focus.scrollRectToVisible(new Rectangle(0, 0, focus.getWidth(), focus.getHeight()));
                    if (focus instanceof OChonNgay pnlPicker) pnlPicker.focusCalendar();
                    else {
                        focus.requestFocusInWindow();
                        if (focus instanceof JTextField txtText) txtText.selectAll();
                    }
                });
            } finally {
                btnSave.setEnabled(true);
            }
        });
        winDialog.pack();
        winDialog.setLocationRelativeTo(null);
        winDialog.setVisible(true);
    }
    private Map<String,Object> selectedRecord() {
        int selected=tblTable.getSelectedRow();
        if(selected<0) { JOptionPane.showMessageDialog(this,"Vui lòng chọn một bản ghi."); return null; }
        int row=tblTable.convertRowIndexToModel(selected);
        Map<String,Object> record=new LinkedHashMap<>();
        for(String field:fields) {
            int column=Arrays.asList(displayFields).indexOf(field);
            if(column>=0) record.put(field,model.getValueAt(row,column));
        }
        return record;
    }
    private void openDocument() {
        if(tableName.equals("HoaDon")) {
            int selected=tblTable.getSelectedRow();
            if(selected<0) { JOptionPane.showMessageDialog(this,"Vui lòng chọn hóa đơn."); return; }
            String invoice=String.valueOf(model.getValueAt(tblTable.convertRowIndexToModel(selected),0));
            JDialog winInvoice=new JDialog(SwingUtilities.getWindowAncestor(this),"Chi tiết hóa đơn "+invoice,Dialog.ModalityType.APPLICATION_MODAL);
            JScrollPane scrInvoice=new JScrollPane(new ManHinhHoaDon(invoice)); scrInvoice.getVerticalScrollBar().setUnitIncrement(20);
            Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
            scrInvoice.setPreferredSize(new Dimension(Math.min(1050,screen.width-100),Math.min(740,screen.height-140)));
            winInvoice.setContentPane(scrInvoice); winInvoice.pack(); winInvoice.setLocationRelativeTo(null); winInvoice.setVisible(true); load(); return;
        }
        Map<String,Object> record=selectedRecord();
        if(record!=null) { TrinhSuaPhieu.view(this,tableName,record); load(); }
    }
    private void hideRecord() {
        int row = tblTable.getSelectedRow();
        if (row < 0) return;
        if (JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn xoá dòng này ra khỏi danh sách hiển thị?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            dao.softDelete(model.getValueAt(tblTable.convertRowIndexToModel(row), 0).toString());
            load();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
        }
    }
}
