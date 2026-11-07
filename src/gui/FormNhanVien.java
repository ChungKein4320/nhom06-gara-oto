package gui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import java.text.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public final class FormNhanVien extends JPanel {
    private static final String[] CAC_COT = new String[] { "maNhanVien", "hoTenNV", "ngaySinh", "soDienThoai",
            "email", "diaChi", "vaiTro", "gioiTinh", "trangThai" };
    private static final Map<String, String> KIEU_COT = Map.ofEntries(Map.entry("maNhanVien", "varchar"),
            Map.entry("hoTenNV", "nvarchar"), Map.entry("ngaySinh", "date"),
            Map.entry("soDienThoai", "varchar"), Map.entry("email", "varchar"),
            Map.entry("diaChi", "nvarchar"), Map.entry("vaiTro", "nvarchar"), Map.entry("gioiTinh", "bit"),
            Map.entry("trangThai", "nvarchar"));
    private static final Set<String> COT_BAT_BUOC = Set.of("maNhanVien", "hoTenNV", "ngaySinh", "soDienThoai",
            "email", "vaiTro", "gioiTinh", "trangThai");

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final String tableName;
    private transient dao.NhanVienDAO dao;
    private final JTable tblTable;
    private final DefaultTableModel model;
    private final JTextField txtSearch;
    private final JLabel lblCount;
    private final String[] fields;
    private final String[] displayFields;
    private final BoLocTrangThai statusFilters;

    // Khôi phục trạng thái đối tượng sau khi đọc từ luồng lưu.
    @java.io.Serial
    private void readObject(java.io.ObjectInputStream input)
            throws java.io.IOException, ClassNotFoundException {
        input.defaultReadObject();
        dao = new dao.NhanVienDAO();
    }

    public FormNhanVien(String page) {
        this(page, null, null);
    }

    public FormNhanVien(String page, String employeeId) {
        this(page, null, employeeId);
    }

    public FormNhanVien(String page, VaiTro role, String employeeId) {
        setLayout(new BorderLayout(0, 18));
        setBackground(GiaoDien.BG);
        setBorder(new EmptyBorder(26, 26, 26, 26));

        tableName = "NhanVien";

        dao = new dao.NhanVienDAO();

        fields = CAC_COT;
        displayFields = fields;

        JPanel pnlHeading = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlHeading.setOpaque(false);

        JLabel lblPath = GiaoDien.label("THINKING AUTO/" + page.toUpperCase(), 11, true, GiaoDien.TEAL);
        JLabel lblTitle = GiaoDien.label(page, 26, true, GiaoDien.NAV);

        for (JLabel lblLabel : new JLabel[] { lblPath, lblTitle }) {
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
        boolean canAdd = true;

        btnAdd.setEnabled(canAdd);
        btnAdd.addActionListener(e -> edit(false));

        {
            pnlToolbar.add(btnAdd, BorderLayout.EAST);
        }

        pnlCard.add(pnlToolbar, BorderLayout.NORTH);
        // Tạo model chứa dữ liệu.
        model = new DefaultTableModel(new String[] { "Mã nhân viên", "Họ tên nhân viên", "Ngày sinh",
                "Số điện thoại", "Email", "Địa chỉ", "Vai trò", "Giới tính", "Trạng thái" }, 0) {
					private static final long serialVersionUID = 1L;

			// Không sửa trực tiếp phải bấm nút Chỉnh sửa
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        // Tạo table từ model
        tblTable = GiaoDien.table(model);
        tblTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        {
            tblTable.setAutoCreateRowSorter(true);
        }
        // Bộ lọc trạng thái dựa trên cột của bảng
        statusFilters = new BoLocTrangThai(tblTable);
        // Cập nhật số dòng
        statusFilters.onChange(() -> {
            updateCount();
        });
        statusFilters.setBorder(new EmptyBorder(6, 0, 0, 0));
        // Đưa bộ lọc xuống dưới ô tìm kiếm
        pnlToolbar.add(statusFilters, BorderLayout.SOUTH);

        tblTable.setRowHeight(40);
        tblTable.getTableHeader().setResizingAllowed(true);

        JScrollPane scrScroll = new JScrollPane(tblTable);
        scrScroll.setPreferredSize(new Dimension(850, 440));
        BoCucBang.install(tblTable, scrScroll);
        pnlCard.add(scrScroll, BorderLayout.CENTER);

        JPanel pnlActions = new JPanel(new BorderLayout(0, 4));
        pnlActions.setOpaque(false);

        lblCount = GiaoDien.label("Đang tải dữ liệu...", 12, false, GiaoDien.MUTED);
        pnlActions.add(lblCount, BorderLayout.WEST);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        pnlButtons.setOpaque(false);

        JButton btnRefresh = GiaoDien.button("Làm mới", GiaoDien.CREAM, GiaoDien.NAV);
        JButton btnEdit = GiaoDien.button("Chỉnh sửa", GiaoDien.TEAL, Color.WHITE);
        JButton btnDelete = GiaoDien.button("Xóa", GiaoDien.ORANGE, Color.WHITE);

        final boolean canEdit = true;
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(true);

        btnRefresh.addActionListener(e -> lamMoi());
        btnEdit.addActionListener(e -> edit(true));
        btnDelete.addActionListener(e -> hideRecord());

        pnlButtons.add(btnRefresh);

        pnlButtons.add(btnEdit);
        pnlButtons.add(btnDelete);
        // Phải chọn 1 dòng thì nút Chỉnh được bật
        tblTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selected = tblTable.getSelectedRow();
                if (selected < 0) {
                    btnEdit.setEnabled(false);
                } else {
                    btnEdit.setEnabled(canEdit);
                }
            }
        });

        for (Component comp : pnlButtons.getComponents()) {
            if (comp instanceof JButton btnAction) {
                Dimension pref = btnAction.getPreferredSize();
                btnAction.setPreferredSize(new Dimension(Math.max(pref.width + 14, 100), 38));
            }
        }

        JScrollPane scrActions = new JScrollPane(pnlButtons, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrActions.setBorder(null);
        scrActions.setOpaque(false);
        scrActions.getViewport().setOpaque(false);
        scrActions.setPreferredSize(new Dimension(850, 42));

        pnlActions.add(scrActions, BorderLayout.SOUTH);
        pnlCard.add(pnlActions, BorderLayout.SOUTH);
        add(pnlCard, BorderLayout.CENTER);
        // Lấy nội dung nhập, theo dõi khi nd thay đổi
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            // Khi gõ thêm chữ.
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            //  Khi xóa chữ.
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            // Cập nhật giao diện khi nội dung thay đổi.
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }
        });

        // tải dữ liệu lên bảng

        load();
    }

    // ===== PHƯƠNG THỨC: lamMoi() — Tải lại dữ liệu và cập nhật nội dung đang hiển thị. =====
    private void lamMoi() {
        txtSearch.setText("");
        statusFilters.reset();
        load();
        tblTable.clearSelection();
        tblTable.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
    }

    // ===== PHƯƠNG THỨC: load() — Tải dữ liệu cho bảng hiện tại. =====
    private void load() {
        try {
            // Xóa data tải lại, tránh trùng
            model.setRowCount(0);
            List<LinkedHashMap<String, Object>> rows = dao.getAll();

            for (Map<String, Object> row : rows) {

                Object[] values = new Object[displayFields.length];
                // duyệt từng hàng
                for (int i = 0; i < displayFields.length; i++) {
                    // Lấy tên trường theo đúng thứ tự cột hiển thị.
                    String field = displayFields[i];
                    // Giới tính có bit = 1 thành Nam, ngc lại
                    if (field.equals("gioiTinh")) {
                        values[i] = Boolean.TRUE.equals(row.get(field))
                                || "1".equals(String.valueOf(row.get(field))) ? "Nam" : "Nữ";
                    } else {
                        values[i] = row.get(field);
                    }
                }
                // Thêm dữ liệu vào bảng
                model.addRow(values);
            }

            statusFilters.refresh();
        } catch (SQLException ex) {
            lblCount.setText("Lỗi kết nối SQL Server");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi cơ sở dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== PHƯƠNG THỨC: filter() — Lọc dữ liệu theo tiêu chí đang chọn. =====
    private void filter() {
        statusFilters.search(txtSearch.getText());
    }

    // ===== PHƯƠNG THỨC: updateCount() — Cập nhật số lượng. =====
    private void updateCount() {
        lblCount.setText("Hiển thị: " + tblTable.getRowCount() + " / " + model.getRowCount() + " bản ghi");
    }

    // ===== PHƯƠNG THỨC: edit(boolean) — Mở biểu mẫu để chỉnh sửa dữ liệu. =====
    private void edit(boolean existing) {
        // Kiểm tra có bản ghi khi chọn sửa chưa
        if (existing) {
            Map<String, Object> record = requireSelectedRecord("chỉnh sửa");
            if (record == null)
                return;

        }

        int row = tblTable.getSelectedRow();
        if (existing && row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bản ghi.");
            return;
        }
        // Xác định dòng dữ liệu hoặc sinh mã mới
        int source = existing ? tblTable.convertRowIndexToModel(row) : -1;
        String generatedId = null;

        if (!existing && !fields[0].equals("VIN")) {
            try {
                generatedId = dao.previewNextId();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể sinh mã",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        // Tạo các ô nhập
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(20, 22, 12, 22));
        // nơi lưu các thành phần nhập 
        Map<String, JComponent> inputs = new LinkedHashMap<>();
        // Vòng lặp duyệt qua các cột tạo từng ô nhập, nếu sửa thì đổ data vào ô
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            int column = Arrays.asList(displayFields).indexOf(field);
            String value = existing && model.getValueAt(source, column) != null
                    ? model.getValueAt(source, column).toString()
                    : "";

            String type = KIEU_COT.get(field);
            // Tạo ô nhập hoặc ô chọn
            JComponent input;
            // Kiểm tra có tham chiếu đến bảng khác không
            OChonThamChieu cmbReference = OChonThamChieu.create(tableName, field, value);
            JComboBox<String> cmbOptions = taoLuaChon(field, value, existing);

            if (cmbReference != null) {
                cmbReference.selectId(value);
                input = cmbReference;
            } else if (cmbOptions != null) {
                input = cmbOptions;
            } else if (type.equals("date")) {
                input = new OChonNgay(value);
            } else if (field.equalsIgnoreCase("gioiTinh")) {
                JPanel pnlGioiTinh = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
                pnlGioiTinh.setOpaque(false);

                JRadioButton radNam = new JRadioButton("Nam");
                JRadioButton radNu = new JRadioButton("Nữ");

                radNam.setOpaque(false);
                radNu.setOpaque(false);
                radNam.setFont(GiaoDien.font(13, false));
                radNu.setFont(GiaoDien.font(13, false));

                ButtonGroup group = new ButtonGroup();
                group.add(radNam);
                group.add(radNu);
                // hiện lựa chọn ban đầu khi mở form chỉnh sửa
                radNam.setSelected(existing && !value.equalsIgnoreCase("Nữ"));
                radNu.setSelected(value.equalsIgnoreCase("Nữ"));

                pnlGioiTinh.add(radNam);
                pnlGioiTinh.add(radNu);
                input = pnlGioiTinh;
            } else {
                // Tạo ô nhập
                JTextField txtInput = GiaoDien.field(value);
                // Đưa dữ liệu lên ô nhập
                txtInput.setText(value);
                // Ô mã mà thêm thì auto = true
                boolean auto = i == 0 && !existing;

                if (auto)
                    txtInput.setText(generatedId);

                if (i == 0) {
                    txtInput.setEditable(false);
                }

                if (auto || (i == 0 && existing)) {
                    // Khóa ô mã nền xanh nhạt
                    DinhDangKeThua.lock(txtInput);
                }

                input = txtInput;
            }
            // Lưu từng ô vào inputs
            inputs.put(field, input);
            // Tạo nhãn và đặt ô nhập dưới nhãn
            JPanel pnlCell = new JPanel(new BorderLayout(0, 7));
            pnlCell.setOpaque(false);
            // Tạo nhãn
            pnlCell.add(GiaoDien.label(nhanCot(field) + (COT_BAT_BUOC.contains(field) ? " *" : ""), 12, true,
                    GiaoDien.TEXT), BorderLayout.NORTH);
            //Ô nhập dưới nhãn
            input.setPreferredSize(new Dimension(250, 40));
            pnlCell.add(input, BorderLayout.CENTER);
            // Chia bô cục
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = i % 2;
            gbc.gridy = i / 2;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, i % 2 == 0 ? 0 : 9, 16, i % 2 == 0 ? 9 : 0);
            pnlForm.add(pnlCell, gbc);
        }

        JScrollPane scrSc = new JScrollPane(pnlForm);
        scrSc.setBorder(null);
        scrSc.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        int formH = pnlForm.getPreferredSize().height + 8;
        scrSc.setPreferredSize(new Dimension(700, Math.min(540, formH)));

        JDialog winDialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                existing ? "Chỉnh sửa thông tin" : "Thêm thông tin mới",
                Dialog.ModalityType.APPLICATION_MODAL);
        winDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel pnlContent = new JPanel(new BorderLayout(0, 12));
        pnlContent.setBorder(new EmptyBorder(12, 12, 12, 12));
        pnlContent.add(scrSc, BorderLayout.CENTER);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        JButton btnSave = GiaoDien.button("Lưu", GiaoDien.TEAL, Color.WHITE);
        JButton btnCancel = GiaoDien.button("Hủy", GiaoDien.CREAM, GiaoDien.NAV);

        btnSave.setPreferredSize(new Dimension(115, 40));
        btnCancel.setPreferredSize(new Dimension(115, 40));
        pnlActions.add(btnSave);
        pnlActions.add(btnCancel);
        pnlContent.add(pnlActions, BorderLayout.SOUTH);

        winDialog.setContentPane(pnlContent);
        winDialog.getRootPane().setDefaultButton(btnSave);
        winDialog.getRootPane().registerKeyboardAction(e -> winDialog.dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        btnCancel.addActionListener(e -> winDialog.dispose());

        String[] expectedId = { generatedId };
        Map<JComponent, javax.swing.border.Border> borders = new LinkedHashMap<>();

        for (JComponent input : inputs.values()) {
            borders.put(input, input.getBorder());
        }

        btnSave.addActionListener(e -> {
            btnSave.setEnabled(false);
            borders.forEach(JComponent::setBorder);

            try {

                Map<String, Object> values = kiemTraDuLieu(inputs);

                if (existing) {
                    dao.update(model.getValueAt(source, 0).toString(), values);
                } else {
                    dao.insert(values, expectedId[0]);
                }

                winDialog.dispose(); // đóng cửa sổ thêm/sửa
                load(); // tải lại data
            } catch (SQLException ex) {
                String field = ThongBaoLoiSQL.errorField(ex, tableName);
                String message = ex.getMessage();

                if (!existing && fields[0].equals(field)) {
                    try {
                        expectedId[0] = dao.previewNextId();
                        ((JTextField) inputs.get(field)).setText(expectedId[0]);
                        message = "Mã đã thay đổi. Mã mới là " + expectedId[0]
                                + ". Dữ liệu bạn nhập vẫn được giữ nguyên;" + " vui lòng nhấn Lưu lại.";
                    } catch (SQLException refreshError) {
                        message = message + "\nKhông lấy được mã mới: " + refreshError.getMessage();
                    }
                } else if (field != null) {
                    String label = nhanCot(field);

                    if (ex.getErrorCode() == 2601 || ex.getErrorCode() == 2627) {
                        message = label + " đã tồn tại. Vui lòng nhập giá trị khác.";
                    } else if (ex.getErrorCode() == 547 && message != null && message.contains("FK_")) {
                        message = label + " không tồn tại hoặc không hợp lệ." + " Vui lòng kiểm tra lại.";
                    }
                }

                JComponent invalid = inputs.get(field);
                if (invalid != null) {
                    invalid.setBorder(BorderFactory.createLineBorder(GiaoDien.ORANGE, 2));
                }

                JOptionPane.showMessageDialog(winDialog,
                        message == null ? "Không thể lưu." + " Vui lòng kiểm tra lại thông tin." : message,
                        "Không thể lưu", JOptionPane.ERROR_MESSAGE);

                JComponent target = invalid;
                if (target == null || !target.isEnabled()
                        || (target instanceof JTextField txtText && !txtText.isEditable())) {
                    target = inputs.values().stream()
                            .filter(input -> input.isEnabled()
                                    && (!(input instanceof JTextField txtText) || txtText.isEditable()))
                            .findFirst().orElse(btnSave);
                }

                JComponent focus = target;
                SwingUtilities.invokeLater(() -> {
                    focus.scrollRectToVisible(new Rectangle(0, 0, focus.getWidth(), focus.getHeight()));

                    if (focus instanceof OChonNgay pnlPicker) {
                        pnlPicker.focusCalendar();
                    } else {
                        focus.requestFocusInWindow();
                        if (focus instanceof JTextField txtText) {
                            txtText.selectAll();
                        }
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

    // ===== PHƯƠNG THỨC: currentRecord() — Lấy bản ghi đang được chọn. =====
    private Map<String, Object> currentRecord() {
        int selected = tblTable.getSelectedRow();
        if (selected < 0)
            return null;

        int row = tblTable.convertRowIndexToModel(selected);
        Map<String, Object> record = new LinkedHashMap<>();

        for (String field : fields) {
            int column = Arrays.asList(displayFields).indexOf(field);
            if (column >= 0) {
                record.put(field, model.getValueAt(row, column));
            }
        }
        return record;
    }

    // ===== PHƯƠNG THỨC: requireSelectedRecord(String) — Kiểm tra và lấy bản ghi đang được chọn. =====
    private Map<String, Object> requireSelectedRecord(String actionDesc) {
        Map<String, Object> record = currentRecord();
        if (record == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một bản ghi" + (actionDesc == null ? "." : " để " + actionDesc + "."),
                    "Chưa chọn bản ghi", JOptionPane.WARNING_MESSAGE);
        }
        return record;
    }

    // ===== PHƯƠNG THỨC: hideRecord() — Ẩn bản ghi đã chọn khỏi danh sách. =====
    private void hideRecord() {
        int row = tblTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một bản ghi để xóa.", "Chưa chọn bản ghi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (JOptionPane.showConfirmDialog(this,
                "Bạn chắc chắn muốn xoá dòng này" + " ra khỏi danh sách hiển thị?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            dao.softDelete(model.getValueAt(tblTable.convertRowIndexToModel(row), 0).toString());
            load();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== PHƯƠNG THỨC: getStatusFilterCombo() — Lấy trạng thái bộ lọc combo. =====
    public JComboBox<String> getStatusFilterCombo() {
        String statusField = "trangThai";
        int column = Arrays.asList(displayFields).indexOf(statusField);
        return column >= 0 ? statusFilters.getFilter(column) : null;
    }

    // ===== PHƯƠNG THỨC: getTable() — Lấy bảng. =====
    public JTable getTable() {
        return tblTable;
    }

    // Kiểm tra dữ liệu.

	private static Map<String, Object> kiemTraDuLieu(
	        Map<String, JComponent> inputs) throws java.sql.SQLException {
	
	    Map<String, Object> values = new LinkedHashMap<>();
	
	    docCot(inputs, values, "hoTenNV", "Họ tên nhân viên", "nvarchar:100", true);
	
	    if (values.get("hoTenNV") instanceof String hoTen
	            && !hoTen.matches("\\p{Lu}[\\p{Ll}\\p{M}]*(?: +\\p{Lu}[\\p{Ll}\\p{M}]*)+")) {
	        throw new ThongBaoLoiSQL.Issue("hoTenNV",
	                "Họ tên phải có ít nhất 2 từ, mỗi từ viết hoa chữ cái đầu.");
	    }
	
	    docCot(inputs, values, "ngaySinh", "Ngày sinh", "date:", true);
	
	    if (values.get("ngaySinh") != null) {
	        java.time.LocalDate ngaySinh = java.time.LocalDate.parse(
	                values.get("ngaySinh").toString().substring(0, 10));
	        java.time.LocalDate homNay = java.time.LocalDate.now();
	
	        if (ngaySinh.isAfter(homNay.minusYears(18))
	                || !ngaySinh.isAfter(homNay.minusYears(63))) {
	            throw new ThongBaoLoiSQL.Issue("ngaySinh",
	                    "Nhân viên phải từ 18 đến 62 tuổi.");
	        }
	    }
	
	    docCot(inputs, values, "soDienThoai", "Số điện thoại", "varchar:10", true);
	
	    if (values.get("soDienThoai") instanceof String sdt
	            && !sdt.matches("0[35789][0-9]{8}")) {
	        throw new ThongBaoLoiSQL.Issue("soDienThoai",
	                "Số điện thoại phải gồm 10 chữ số, đầu số 03, 05, 07, 08 hoặc 09.");
	    }
	
		docCot(inputs, values, "email", "Email", "varchar:254", true);
		
		if (values.get("email") instanceof String email
		        && !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
		    throw new ThongBaoLoiSQL.Issue("email",
		            "Email không đúng định dạng.");
		}
	
	    docCot(inputs, values, "diaChi", "Địa chỉ", "nvarchar:255", false);
	
	    docCot(inputs, values, "vaiTro", "Vai trò", "nvarchar:50", true);
	
	    if (values.get("vaiTro") instanceof String vaiTro
	            && !java.util.Set.of(
	                    "Nhân viên kỹ thuật",
	                    "Nhân viên kho",
	                    "Nhân viên bán hàng",
	                    "Nhân viên quản lý").contains(vaiTro)) {
	        throw new ThongBaoLoiSQL.Issue("vaiTro",
	                "Vai trò nhân viên không hợp lệ.");
	    }
	
	    docCot(inputs, values, "gioiTinh", "Giới tính", "bit:", true);
	
	    docCot(inputs, values, "trangThai", "Trạng thái", "nvarchar:30", true);
	
	    if (values.get("trangThai") instanceof String trangThai
	            && !java.util.Set.of("Đang làm", "Đã nghỉ").contains(trangThai)) {
	        throw new ThongBaoLoiSQL.Issue("trangThai",
	                "Trạng thái nhân viên không hợp lệ.");
	    }
	
	    return values;
	}


    // ===== PHƯƠNG THỨC: docCot(Map<String,JComponent>, Map<String,Object>, String, String, String, boolean) — Đọc, kiểm tra và chuyển giá trị của một cột biểu mẫu. =====
    private static void docCot(Map<String, JComponent> inputs, Map<String, Object> values,
            String field, String label, String specification, boolean required) throws ThongBaoLoiSQL.Issue {
        if (!inputs.containsKey(field)) return;
        if (inputs.get(field) instanceof OChonThamChieu cmbCombo) cmbCombo.validateSelection();
        if(inputs.get(field) instanceof JCheckBox checkBox) {
            values.put(field,checkBox.isSelected());
            return;
        }
        // Đọc field là tên cột đó
        String raw = docVanBan(inputs.get(field));
        if (raw.isEmpty()) {
            if (required) throw new ThongBaoLoiSQL.Issue(field, "Vui lòng nhập " + label.toLowerCase() + ".");
            values.put(field, null);
            return;
        }
        String[] spec = specification.split(":", 2);
        Object value;
        try {
            value = switch (spec[0]) {
                case "int" -> Integer.valueOf(raw);
                case "decimal" -> new java.math.BigDecimal(raw);
                case "datetime", "datetime2" -> java.sql.Timestamp.valueOf(raw.length() == 10 ? raw + " 00:00:00" : raw);
                // Chuyển chuỗi thành ngày sql
                case "date" -> java.sql.Date.valueOf(raw);
                case "bit" -> {
                    if (!raw.equals("Nam") && !raw.equals("Nữ")) throw new IllegalArgumentException();
                    yield raw.equals("Nam");
                }
                default -> raw;
            };
        } catch (IllegalArgumentException ex) {
            String expected = spec[0].equals("int") ? "số nguyên hợp lệ"
                    : spec[0].equals("decimal") ? "số hợp lệ, dùng dấu chấm cho phần thập phân" : "ngày hợp lệ";
            throw new ThongBaoLoiSQL.Issue(field, label + " phải là " + expected + ".");
        }
        if ((spec[0].equals("varchar") || spec[0].equals("nvarchar"))
                && raw.length() > Integer.parseInt(spec[1].trim())) {
            throw new ThongBaoLoiSQL.Issue(field, label + " không được vượt quá " + spec[1].trim() + " ký tự.");
        }
        if (value instanceof Number number) {
            java.math.BigDecimal numberValue = new java.math.BigDecimal(number.toString());
            if (numberValue.signum() < 0) throw new ThongBaoLoiSQL.Issue(field, label + " không được âm.");
            if (spec[0].equals("decimal")) {
                String[] size = spec[1].split(",");
                int precision = Integer.parseInt(size[0].trim());
                int scale = Integer.parseInt(size[1].trim());
                java.math.BigDecimal normalized = numberValue.stripTrailingZeros();
                if (Math.max(0, normalized.precision() - normalized.scale()) > precision - scale
                        || Math.max(0, normalized.scale()) > scale) {
                    throw new ThongBaoLoiSQL.Issue(field, label + " chỉ nhận tối đa " + (precision - scale)
                            + " chữ số phần nguyên và " + scale + " chữ số thập phân.");
                }
            }
        }
        // Lưu giá trị gửi DAO
        values.put(field, value);
    }

    // Tạo lua chon.
    private static JComboBox<String> taoLuaChon(String field, String value, boolean existing) {
        String[] choices = switch (field) {
        case "vaiTro" ->
            new String[] { "Nhân viên kỹ thuật", "Nhân viên bán hàng", "Nhân viên kho", "Nhân viên quản lý" };
        case "trangThai" -> new String[] { "Đang làm", "Đã nghỉ" };
        default -> null;
        };
        if (choices == null)
            return null;
        // Combobox chứa các vai trò và trạng thái
        JComboBox<String> combo = new OChonTimKiem<>(choices);
        combo.setFont(GiaoDien.font(12, false));
        // Chỉ được chọn trạng thái có sẵn, không cho nhập.
        combo.setEditable(false);
        if (existing) {
            if (value == null || value.isEmpty()) {
                combo.setSelectedIndex(-1);
            } else {
                boolean found = false;
                for (int i = 0; i < combo.getItemCount(); i++) {
                    if (value.equals(combo.getItemAt(i))) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    combo.addItem(value);
                combo.setSelectedItem(value);
            }
        }
        return combo;
    }

    private static final class BoCucBang {

        // ===== PHƯƠNG THỨC: install(JTable, JScrollPane) — Đăng ký bộ điều chỉnh độ rộng cột cho bảng. =====
        public static void install(JTable tblTable, JScrollPane scrScroll) {
            if (Boolean.TRUE.equals(tblTable.getClientProperty("tableLayout.installed")))
                return;
            tblTable.putClientProperty("tableLayout.installed", Boolean.TRUE);
            scrScroll.getViewport().addComponentListener(new ComponentAdapter() {
                // ===== PHƯƠNG THỨC: componentResized(ComponentEvent) — Cập nhật bố cục khi kích thước thành phần thay đổi. =====
                @Override
                public void componentResized(ComponentEvent event) {
                    SwingUtilities.invokeLater(() -> fit(tblTable, scrScroll));
                }
            });
            tblTable.getModel()
                    .addTableModelListener(e -> SwingUtilities.invokeLater(() -> fit(tblTable, scrScroll)));
            SwingUtilities.invokeLater(() -> fit(tblTable, scrScroll));
        }

        // ===== PHƯƠNG THỨC: fit(JTable, JScrollPane) — Điều chỉnh độ rộng cột theo vùng hiển thị của bảng. =====
        public static void fit(JTable tblTable, JScrollPane scrScroll) {
            int columns = tblTable.getColumnCount();
            int available = scrScroll.getViewport().getWidth();
            if (columns == 0 || available <= 0)
                return;
            int[] widths = new int[columns];
            Object fixedProperty = tblTable.getClientProperty("tableLayout.fixedWidths");
            java.util.Map<?, ?> fixed = fixedProperty instanceof java.util.Map<?, ?> map ? map
                    : java.util.Map.of();
            int total = 0;
            for (int col = 0; col < columns; col++) {
                TableColumn column = tblTable.getColumnModel().getColumn(col);
                int width = Math.max(90, tblTable.getFontMetrics(tblTable.getFont())
                        .stringWidth(String.valueOf(column.getHeaderValue())) + 28);
                int limit = Math.min(tblTable.getRowCount(), 120);
                for (int row = 0; row < limit; row++) {
                    Component rendered = tblTable.prepareRenderer(tblTable.getCellRenderer(row, col), row,
                            col);
                    width = Math.max(width, rendered.getPreferredSize().width + 8);
                }
                widths[col] = Math.min(width,
                        DinhDangBang.contentAlignment(tblTable, col) == javax.swing.SwingConstants.LEFT ? 320
                                : 210);
                if (fixed.get(column.getModelIndex()) instanceof Number fixedWidth)
                    widths[col] = fixedWidth.intValue();
                total += widths[col];
            }
            if (total < available) {
                int spare = available - total;
                int flexible = 0, last = -1;
                for (int i = 0; i < columns; i++)
                    if (!fixed.containsKey(tblTable.convertColumnIndexToModel(i))) {
                        flexible++;
                        last = i;
                    }
                if (flexible > 0) {
                    int each = spare / flexible;
                    for (int i = 0; i < columns; i++)
                        if (!fixed.containsKey(tblTable.convertColumnIndexToModel(i)))
                            widths[i] += each;
                    widths[last] += spare - each * flexible;
                }
            }
            tblTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int i = 0; i < columns; i++) {
                tblTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
                tblTable.getColumnModel().getColumn(i).setWidth(widths[i]);
            }
            tblTable.revalidate();
        }
    }

    private static final class GiaoDien {

        private static final Color NAV = new Color(0x13272E);
        private static final Color TEAL = new Color(0x487674);
        private static final Color CREAM = new Color(0xE8C69B);
        private static final Color ORANGE = new Color(0xCB6330);
        private static final Color BG = new Color(0xF4F7F6);
        private static final Color WHITE = Color.WHITE;
        private static final Color TEXT = new Color(0x21383C);
        private static final Color MUTED = new Color(0x718387);
        private static final Color LINE = new Color(0xE4EBE9);

        // ===== PHƯƠNG THỨC: font(int, boolean) — Tạo phông chữ theo cỡ và kiểu chữ. =====
        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
        }

        // ===== PHƯƠNG THỨC: rounded(Color, int) — Tạo đường viền bo góc cho thành phần. =====
        public static JPanel rounded(Color c, int r) {
            return new JPanel() {
				private static final long serialVersionUID = 1L;

				// ===== PHƯƠNG THỨC: paintComponent(Graphics) — Vẽ nội dung và trạng thái hiện tại của thành phần. =====
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D q = (Graphics2D) g.create();
                    q.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    q.setColor(c);
                    q.fillRoundRect(0, 0, getWidth(), getHeight(), r, r);
                    q.dispose();
                }
            };
        }

        // ===== PHƯƠNG THỨC: button(String, Color, Color) — Tạo nút giao diện theo kiểu chỉ định. =====
        public static JButton button(String s, Color bg, Color fg) {
            JButton btnB = new JButton(s) {
				private static final long serialVersionUID = 1L;

				// ===== PHƯƠNG THỨC: paintComponent(Graphics) — Vẽ nội dung và trạng thái hiện tại của thành phần. =====
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D q = (Graphics2D) g.create();
                    q.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    q.setColor(getModel().isRollover() ? bg.brighter() : bg);
                    q.fillRoundRect(0, 0, getWidth(), getHeight(), 13, 13);
                    q.dispose();
                    super.paintComponent(g);
                }
            };
            btnB.setForeground(fg);
            BieuTuong.apply(btnB, s, fg);
            btnB.setContentAreaFilled(false);
            btnB.setBorder(BorderFactory.createEmptyBorder(10, 17, 10, 17));
            btnB.setFocusPainted(false);
            btnB.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return btnB;
        }

        // ===== PHƯƠNG THỨC: label(String, int, boolean, Color) — Tạo nhãn giao diện. =====
        public static JLabel label(String s, int size, boolean bold, Color c) {
            JLabel lblL = new JLabel(s);
            lblL.setFont(font(size, bold));
            lblL.setForeground(c);
            return lblL;
        }

        //  Tạo ô có hiển thị gợi ý nhập
        public static JTextField field(String hint) {
            JTextField txtT = new JTextField() {
				private static final long serialVersionUID = 1L;

				@Override
                protected void paintComponent(Graphics g) {
		            // Vẽ ô nhập liệu bình thường.
					super.paintComponent(g);
                    // ô chưa nhập chữ thì hiện gợi ý
                    if (getText().isEmpty() && hint != null && !hint.isEmpty()) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setFont(getFont());
                        // xám nhạt
                        g2.setColor(new Color(0x9AA7A9));
                        // xác định vị trí chữ trong ô
                        Insets insets = getInsets();
                        int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2
                                + g2.getFontMetrics().getAscent();

                        g2.drawString(hint, insets.left, y);
                        g2.dispose();
                    }
                }
            };

            txtT.setFont(font(13, false));
            txtT.setForeground(TEXT);
            txtT.setBorder(
                new CompoundBorder(
                    new LineBorder(LINE, 1, true),
                    new EmptyBorder(10, 12, 10, 12)
                )
            );

            return txtT;
        }

        // ===== PHƯƠNG THỨC: table(DefaultTableModel) — Lấy bảng dữ liệu tương ứng với màn hình. =====
        public static JTable table(DefaultTableModel m) {
            JTable tblT = new JTable(m) {
				private static final long serialVersionUID = 1L;
				private final TableCellRenderer cells = new DinhDangBang();

                // ===== PHƯƠNG THỨC: getCellRenderer(int, int) — Lấy ô renderer. =====
                @Override
                public TableCellRenderer getCellRenderer(int row, int column) {
                    return cells;
                }

                // ===== PHƯƠNG THỨC: createDefaultColumnsFromModel() — Tạo các cột mặc định theo mô hình bảng. =====
                @Override
                public void createDefaultColumnsFromModel() {
                    super.createDefaultColumnsFromModel();
                    for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
                        TableColumn column = getColumnModel().getColumn(i);
                        column.setHeaderValue(nhanCot(getModel().getColumnName(column.getModelIndex())));
                    }
                }
            };
            tblT.setRowHeight(40);
            tblT.setShowGrid(false);
            tblT.setIntercellSpacing(new Dimension(0, 0));
            tblT.setSelectionBackground(new Color(0xE3F1EF));
            tblT.setSelectionForeground(TEXT);
            tblT.setForeground(TEXT);
            tblT.setFont(font(12, false));
            tblT.setFillsViewportHeight(true);
            tblT.getTableHeader().setBackground(new Color(0xEDF3F1));
            tblT.getTableHeader().setForeground(TEAL);
            tblT.getTableHeader().setPreferredSize(new Dimension(0, 43));
            tblT.getTableHeader().setReorderingAllowed(false);
            DinhDangBang.header(tblT);
            tblT.setDefaultEditor(Object.class, null);
            return tblT;
        }

        // ===== PHƯƠNG THỨC: card(LayoutManager) — Tạo khung giao diện dạng thẻ. =====
        public static JPanel card(LayoutManager l) {
            JPanel pnlP = rounded(WHITE, 18);
            pnlP.setLayout(l);
            pnlP.setBorder(new EmptyBorder(20, 22, 20, 22));
            pnlP.setOpaque(false);
            return pnlP;
        }
    }

    private static final class DinhDangBang extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;
        private final NumberFormat numbers = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

        // ===== KHỞI TẠO ĐỐI TƯỢNG: DinhDangBang() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
        DinhDangBang() {
            numbers.setMaximumFractionDigits(2);
            putClientProperty("html.disable", Boolean.TRUE);
        }

        // ===== PHƯƠNG THỨC: name(JTable, int) — Lấy tên hiển thị của đối tượng. =====
        static String name(JTable table, int column) {
            String name = table.getModel().getColumnName(table.convertColumnIndexToModel(column));
            return Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "").replace('đ', 'd')
                    .replace('Đ', 'D').toLowerCase(Locale.ROOT);
        }

        // ===== PHƯƠNG THỨC: alignment(JTable, int) — Căn chỉnh nội dung trong cột bảng. =====
        static int alignment(JTable table, int column) {
            return SwingConstants.CENTER;
        }

        // ===== PHƯƠNG THỨC: isMoneyColumn(JTable, int) — Kiểm tra money cột. =====
        static boolean isMoneyColumn(JTable table, int column) {
            String name = name(table, column);
            if (name.startsWith("ma "))
                return false;
            return name.startsWith("tong ") || name.startsWith("don gia") || name.equals("thanh tien")
                    || name.contains("chi phi") || name.equals("muc giam") || name.equals("tien phu tung")
                    || name.equals("tien cong") || name.equals("dich vu khac") || name.contains("thue")
                    || name.equals("giam gia");
        }

        // ===== PHƯƠNG THỨC: contentAlignment(JTable, int) — Căn chỉnh nội dung hiển thị trong ô bảng. =====
        static int contentAlignment(JTable table, int column) {
            String name = name(table, column);
            if (name.startsWith("ma ") || name.equals("ma") || name.equals("stt") || name.contains("ngay")
                    || name.contains("thoi gian") || name.contains("gioi tinh") || name.contains("trang thai")
                    || name.contains("ket qua kiem tra") || name.contains("nghiem thu")
                    || name.contains("bien so") || name.contains("vin") || name.contains("nam san xuat")
                    || name.contains("odo"))
                return SwingConstants.CENTER;
            if (isMoneyColumn(table, column) || name.startsWith("tong ") || name.contains("tien")
                    || name.contains("gia") || name.contains("so luong") || name.equals("sl")
                    || name.contains("diem") || name.contains("thue") || name.contains("so km")
                    || name.contains("thoi han"))
                return SwingConstants.RIGHT;
            return SwingConstants.LEFT;
        }

        // ===== PHƯƠNG THỨC: getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int) — Lấy bảng ô renderer thành phần. =====
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            String text;
            if (value == null)
                text = "—";
            else if (value instanceof java.sql.Date date)
                text = date.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            else if (value instanceof java.sql.Timestamp date)
                text = date.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            else if (value instanceof java.util.Date date)
                text = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
            else if (value instanceof Boolean flag)
                text = name(table, column).contains("gioi tinh") ? (flag ? "Nam" : "Nữ")
                        : (flag ? "Có" : "Không");
            else if (value instanceof Number number
                    && contentAlignment(table, column) == SwingConstants.RIGHT)
                text = numbers.format(number);
            else
                text = value.toString();
            boolean money = value != null && isMoneyColumn(table, column);
            setText(text);
            setToolTipText(value == null ? null : text);
            setHorizontalAlignment(money ? SwingConstants.RIGHT : alignment(table, column));
            setVerticalAlignment(SwingConstants.CENTER);
            setFont(table.getFont());
            setForeground(selected ? table.getSelectionForeground() : GiaoDien.TEXT);
            setBackground(selected ? table.getSelectionBackground()
                    : row % 2 == 0 ? GiaoDien.WHITE : new Color(0xF5F8F7));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, GiaoDien.LINE),
                    BorderFactory.createEmptyBorder(5, 10, 5, money ? 16 : 10)));
            return this;
        }

        // ===== PHƯƠNG THỨC: header(JTable) — Thiết lập tiêu đề và cách hiển thị cột bảng. =====
        static void header(JTable table) {
            JTableHeader header = table.getTableHeader();
            TableCellRenderer original = header.getDefaultRenderer();
            header.setDefaultRenderer((tbl, value, selected, focus, row, column) -> {
                Component component = original.getTableCellRendererComponent(tbl, value, selected, focus, row,
                        column);
                if (component instanceof JLabel label) {
                    label.setHorizontalAlignment(alignment(tbl, column));
                    label.setVerticalAlignment(SwingConstants.CENTER);
                    label.setFont(GiaoDien.font(12, true));
                    label.setForeground(GiaoDien.TEAL);
                    label.setBackground(new Color(0xEDF3F1));
                    label.setOpaque(true);
                    label.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 1, GiaoDien.LINE),
                            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
                    label.setToolTipText(String.valueOf(value));
                }
                return component;
            });
        }
    }

    // ===== PHƯƠNG THỨC: nhanCot(String) — Tạo tiêu đề hiển thị cho cột bảng. =====
    private static String nhanCot(String field) {
        return switch (field) {
        case "gioiTinh" -> "Giới tính";
        case "maNhanVien" -> "Mã nhân viên";
        case "hoTenNV" -> "Họ tên nhân viên";
        case "ngaySinh" -> "Ngày sinh";
        case "soDienThoai" -> "Số điện thoại";
        case "email" -> "Email";
        case "diaChi" -> "Địa chỉ";
        case "vaiTro" -> "Vai trò";
        case "trangThai" -> "Trạng thái";
        default -> field;
        };
    }

    // ===== PHƯƠNG THỨC: docVanBan(javax.swing.JComponent) — Lấy văn bản từ thành phần nhập liệu. =====
    private static String docVanBan(javax.swing.JComponent control) {
        if (control instanceof OChonNgay pnlPicker)
        	// Lấy ngày đã chọn
            return pnlPicker.getDateText();
        if (control instanceof OChonThamChieu cmbCombo)
            return cmbCombo.selectedId();
        if (control instanceof javax.swing.JComboBox<?> cmbCombo)
            return cmbCombo.getSelectedItem() == null ? "" : cmbCombo.getSelectedItem().toString().trim();
        if (control instanceof javax.swing.JTextField txtField)
            return txtField.getText().trim();
        if (control instanceof javax.swing.JPanel pnlPanel) {
            for (java.awt.Component component : pnlPanel.getComponents())
                if (component instanceof javax.swing.JRadioButton radRadio && radRadio.isSelected())
                    return radRadio.getText();
        }
        return "";
    }

}
