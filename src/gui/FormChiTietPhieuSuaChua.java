package gui;

import java.util.Locale;
import java.time.format.DateTimeFormatter;
import java.text.*;
import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.table.TableColumn;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.Component;
import dao.ChiTietPhieuSuaChuaDAO;

import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

// ===== GUI: FormChiTietPhieuSuaChua =====
/** Danh sách và biểu mẫu sử dụng dữ liệu SQL Server thực tế. */
public final class FormChiTietPhieuSuaChua extends JPanel {
    private static final String[] CAC_COT = new String[] { "maPSC", "maDichVu", "maPhuTung",
            "trangThaiHangMuc", "soLuongPhuTung", "soLuongCapPhat", "soLuongSuDung", "soLuongHoanTra",
            "ngayCapPhat", "maNhanVienCapPhat", "ngayHoanTra", "maNhanVienXacNhanHoanTra", "ketQuaNghiemThu",
            "ngayBatDauBaoHanh", "thoiHanBaoHanh", "ngayHetHanBaoHanh" };
    private static final Map<String, String> KIEU_COT = Map.ofEntries(Map.entry("maPSC", "varchar"),
            Map.entry("maDichVu", "varchar"), Map.entry("maPhuTung", "varchar"),
            Map.entry("trangThaiHangMuc", "nvarchar"), Map.entry("soLuongPhuTung", "int"),
            Map.entry("soLuongCapPhat", "int"), Map.entry("soLuongSuDung", "int"),
            Map.entry("soLuongHoanTra", "int"), Map.entry("ngayCapPhat", "datetime2"),
            Map.entry("maNhanVienCapPhat", "varchar"), Map.entry("ngayHoanTra", "datetime2"),
            Map.entry("maNhanVienXacNhanHoanTra", "varchar"), Map.entry("ketQuaNghiemThu", "nvarchar"),
            Map.entry("ngayBatDauBaoHanh", "datetime2"), Map.entry("thoiHanBaoHanh", "int"),
            Map.entry("ngayHetHanBaoHanh", "datetime2"));
    private static final Set<String> COT_BAT_BUOC = Set.of("maPSC", "maDichVu", "maPhuTung",
            "trangThaiHangMuc", "soLuongPhuTung", "ketQuaNghiemThu", "thoiHanBaoHanh");

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final String page;
    private final String tableName;
    private final String employeeId;
    private transient dao.ChiTietPhieuSuaChuaDAO dao;
    private final JTable tblTable;
    private final DefaultTableModel model;
    private final JTextField txtSearch;
    private final JLabel lblCount;
    private final String[] fields;
    private final String[] displayFields;
    private final BoLocTrangThai statusFilters;
    private final JButton btnAllocate;
    private final JButton btnReturn;

    // ===== PHƯƠNG THỨC: readObject(java.io.ObjectInputStream) — Khôi phục trạng thái đối tượng sau khi đọc từ luồng lưu. =====
    @java.io.Serial
    private void readObject(java.io.ObjectInputStream input)
            throws java.io.IOException, ClassNotFoundException {
        input.defaultReadObject();
        dao = new dao.ChiTietPhieuSuaChuaDAO();
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: FormChiTietPhieuSuaChua(String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public FormChiTietPhieuSuaChua(String page) {
        this(page, null, null);
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: FormChiTietPhieuSuaChua(String, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public FormChiTietPhieuSuaChua(String page, String employeeId) {
        this(page, null, employeeId);
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: FormChiTietPhieuSuaChua(String, VaiTro, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public FormChiTietPhieuSuaChua(String page, VaiTro role, String employeeId) {
        this.page = page;
        this.employeeId = employeeId;
        setLayout(new BorderLayout(0, 18));
        setBackground(GiaoDien.BG);
        setBorder(new EmptyBorder(26, 26, 26, 26));

        tableName = "ChiTietPhieuSuaChua";

        dao = new dao.ChiTietPhieuSuaChuaDAO();

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
        boolean canAdd = false;

        btnAdd.setEnabled(canAdd);
        btnAdd.addActionListener(e -> edit(false));

        {
            pnlToolbar.add(btnAdd, BorderLayout.EAST);
        }

        pnlCard.add(pnlToolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[] { "Mã phiếu sửa chữa", "Mã dịch vụ", "Mã phụ tùng",
                "Trạng thái hạng mục", "Số lượng phụ tùng", "Số lượng cấp phát", "Số lượng sử dụng",
                "Số lượng hoàn trả", "Ngày cấp phát", "Nhân viên cấp phát", "Ngày hoàn trả",
                "Nhân viên xác nhận hoàn trả", "Kết quả nghiệm thu", "Ngày bắt đầu bảo hành",
                "Thời hạn bảo hành", "Ngày hết hạn bảo hành" }, 0) {
            // ===== PHƯƠNG THỨC: isCellEditable(int, int) — Kiểm tra ô bảng có cho phép chỉnh sửa không. =====
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblTable = GiaoDien.table(model);
        tblTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        {
            tblTable.setAutoCreateRowSorter(true);
        }

        statusFilters = new BoLocTrangThai(tblTable);
        statusFilters.onChange(() -> {
            updateCount();
            updateAllocationButtonStates();
        });
        statusFilters.setBorder(new EmptyBorder(6, 0, 0, 0));
        pnlToolbar.add(statusFilters, BorderLayout.SOUTH);

        tblTable.setRowHeight(40);
        tblTable.getTableHeader().setResizingAllowed(true);
        if (page.equals("Phiếu cấp phát phụ tùng") || page.equals("Cấp phát phụ tùng")) {
            Set<String> columnsToHide = Set.of("ketQuaNghiemThu", "ngayBatDauBaoHanh", "thoiHanBaoHanh",
                    "ngayHetHanBaoHanh");
            for (int viewColumn = tblTable.getColumnModel().getColumnCount()
                    - 1; viewColumn >= 0; viewColumn--) {
                int modelColumn = tblTable.getColumnModel().getColumn(viewColumn).getModelIndex();
                if (columnsToHide.contains(displayFields[modelColumn])) {
                    tblTable.getColumnModel().removeColumn(tblTable.getColumnModel().getColumn(viewColumn));
                }
            }
        }

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

        final boolean canEdit = false;
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);

        btnRefresh.addActionListener(e -> lamMoi());
        btnEdit.addActionListener(e -> edit(true));
        btnDelete.addActionListener(e -> hideRecord());

        pnlButtons.add(btnRefresh);

        if (page.equals("Phiếu cấp phát phụ tùng") || page.equals("Cấp phát phụ tùng")) {
            btnAllocate = GiaoDien.button("Xác nhận cấp phát", GiaoDien.TEAL, Color.WHITE);
            btnAllocate.setEnabled(false);
            btnAllocate.addActionListener(e -> allocate());
            pnlButtons.add(btnAllocate);
            btnReturn = GiaoDien.button("Ghi nhận trả kho", GiaoDien.CREAM, GiaoDien.NAV);
            btnReturn.setEnabled(false);
            btnReturn.addActionListener(e -> recordReturn());
            pnlButtons.add(btnReturn);
        } else {
            btnAllocate = null;
            btnReturn = null;
        }

        {
            pnlButtons.add(btnEdit);

            pnlButtons.add(btnDelete);
        }

        tblTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selected = tblTable.getSelectedRow();

                updateAllocationButtonStates();
                if (selected < 0) {
                    btnEdit.setEnabled(false);
                } else {
                    int row = tblTable.convertRowIndexToModel(selected);
                    switch (tableName) {
                    case "YeuCauNhapKho" -> {
                        int statusCol = Arrays.asList(displayFields).indexOf("trangThai");
                        boolean isPending = statusCol >= 0
                                && "Chờ duyệt".equals(String.valueOf(model.getValueAt(row, statusCol)));
                        btnEdit.setEnabled(isPending && (role == VaiTro.KHO || role == VaiTro.ADMIN));
                    }
                    case "PhieuNhapKho" -> {
                        int statusCol = Arrays.asList(displayFields).indexOf("trangThai");
                        String st = statusCol >= 0 ? String.valueOf(model.getValueAt(row, statusCol)) : "";
                        boolean isPending = "Chờ xác nhận nhập kho".equals(st);
                        btnEdit.setEnabled(isPending && (role == VaiTro.KHO || role == VaiTro.ADMIN));
                    }
                    default -> btnEdit.setEnabled(canEdit);
                    }
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

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            // ===== PHƯƠNG THỨC: insertUpdate(javax.swing.event.DocumentEvent) — Xử lý khi nội dung được thêm vào. =====
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            // ===== PHƯƠNG THỨC: removeUpdate(javax.swing.event.DocumentEvent) — Xử lý khi nội dung bị xóa. =====
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            // ===== PHƯƠNG THỨC: changedUpdate(javax.swing.event.DocumentEvent) — Cập nhật giao diện khi nội dung thay đổi. =====
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }
        });

        // Khởi tạo các lựa chọn lọc cố định theo quy định nghiệp vụ và trạng thái mặc định
        String statusField = (page.equals("Phiếu cấp phát phụ tùng") || page.equals("Cấp phát phụ tùng"))
                ? "trangThaiHangMuc"
                : "trangThai";
        int statusCol = Arrays.asList(displayFields).indexOf(statusField);
        if (statusCol >= 0) {
            List<String> options = filterOptionsFor(tableName, statusField);
            if (!options.isEmpty()) {
                statusFilters.setPredefinedOptions(statusCol, options);
            }
        }

        String defaultState = switch (page) {
        case "Phiếu báo giá" -> "Còn hiệu lực";
        case "Phiếu sửa chữa" -> "Chờ sửa chữa";
        case "Phiếu cấp phát phụ tùng", "Cấp phát phụ tùng" -> "Chờ sửa chữa";
        case "Phiếu yêu cầu nhập kho", "Danh sách yêu cầu nhập", "Yêu cầu nhập kho" -> "Chờ duyệt";
        case "Phiếu nhập kho phụ tùng", "Nhập kho phụ tùng" -> "Chờ xác nhận nhập kho";
        default -> null;
        };

        if (defaultState != null && statusCol >= 0) {
            statusFilters.selectDefault(statusCol, defaultState);
        }

        load();
    }

    // ===== PHƯƠNG THỨC: lamMoi() — Tải lại dữ liệu và cập nhật nội dung đang hiển thị. =====
    private void lamMoi() {
        txtSearch.setText("");
        statusFilters.reset();
        load();
        tblTable.clearSelection();
        tblTable.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        updateAllocationButtonStates();
    }

    // ===== PHƯƠNG THỨC: load() — Tải dữ liệu cho bảng hiện tại. =====
    private void load() {
        try {
            {
                ChiTietPhieuSuaChuaDAO.refreshRepairShortages();
            }

            model.setRowCount(0);
            List<LinkedHashMap<String, Object>> rows = dao.getAll();

            for (Map<String, Object> row : rows) {

                Object[] values = new Object[displayFields.length];
                for (int i = 0; i < displayFields.length; i++) {
                    String field = displayFields[i];
                    {
                        values[i] = row.get(field);
                    }
                }

                model.addRow(values);
            }

            statusFilters.refresh();
            updateAllocationButtonStates();
        } catch (SQLException ex) {
            lblCount.setText("Lỗi kết nối SQL Server");
            updateAllocationButtonStates();
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

    // ===== PHƯƠNG THỨC: updateAllocationButtonStates() — Cập nhật allocation nút states. =====
    private void updateAllocationButtonStates() {
        if (btnAllocate == null || btnReturn == null) {
            return;
        }
        int selected = tblTable.getSelectedRow();
        if (selected < 0) {
            btnAllocate.setEnabled(false);
            btnReturn.setEnabled(false);
            return;
        }

        int row = tblTable.convertRowIndexToModel(selected);
        if (row < 0 || row >= model.getRowCount()) {
            btnAllocate.setEnabled(false);
            btnReturn.setEnabled(false);
            return;
        }

        String part = getModelString(row, "maPhuTung");
        String status = getModelString(row, "trangThaiHangMuc");
        int soLuongPhuTung = getModelInt(row, "soLuongPhuTung", -1);
        int soLuongCapPhat = getModelInt(row, "soLuongCapPhat", -1);
        int soLuongSuDung = getModelInt(row, "soLuongSuDung", -1);
        int soLuongHoanTra = getModelInt(row, "soLuongHoanTra", -1);

        boolean canAllocate = "Chờ sửa chữa".equals(status) && !part.isEmpty() && !"PT000".equals(part)
                && soLuongPhuTung > 0 && soLuongCapPhat == 0;

        int remaining = soLuongCapPhat - soLuongSuDung - soLuongHoanTra;
        boolean canReturn = "Đang sửa chữa".equals(status) && !part.isEmpty() && !"PT000".equals(part)
                && soLuongCapPhat > 0 && soLuongSuDung >= 0 && soLuongHoanTra >= 0 && remaining > 0;

        btnAllocate.setEnabled(canAllocate);
        btnReturn.setEnabled(canReturn);
    }

    // ===== PHƯƠNG THỨC: getModelValue(int, String) — Lấy mô hình giá trị. =====
    private Object getModelValue(int row, String fieldName) {
        int col = Arrays.asList(displayFields).indexOf(fieldName);
        if (col < 0 || col >= model.getColumnCount() || row < 0 || row >= model.getRowCount()) {
            return null;
        }
        return model.getValueAt(row, col);
    }

    // ===== PHƯƠNG THỨC: getModelString(int, String) — Lấy mô hình string. =====
    private String getModelString(int row, String fieldName) {
        Object val = getModelValue(row, fieldName);
        return val == null ? "" : val.toString().trim();
    }

    // ===== PHƯƠNG THỨC: getModelInt(int, String, int) — Lấy mô hình int. =====
    private int getModelInt(int row, String fieldName, int fallback) {
        Object val = getModelValue(row, fieldName);
        if (val == null)
            return fallback;
        if (val instanceof Number n)
            return n.intValue();
        try {
            String s = val.toString().trim().replace(" ", "").replace(",", "");
            return s.isEmpty() ? fallback : Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    // ===== PHƯƠNG THỨC: allocate() — Cấp phát phụ tùng cho phiếu sửa chữa. =====
    private void allocate() {
        int selected = tblTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hạng mục cần cấp phát.");
            return;
        }

        int row = tblTable.convertRowIndexToModel(selected);

        if (JOptionPane.showConfirmDialog(this, "Xác nhận cấp phát phụ tùng cho hạng mục đã chọn?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        String repair = getModelString(row, "maPSC");
        String service = getModelString(row, "maDichVu");
        String part = getModelString(row, "maPhuTung");
        if (repair.isEmpty())
            repair = String.valueOf(model.getValueAt(row, 0));
        if (service.isEmpty())
            service = String.valueOf(model.getValueAt(row, 1));
        if (part.isEmpty())
            part = String.valueOf(model.getValueAt(row, 2));

        try {
            ChiTietPhieuSuaChuaDAO.allocate(repair, service, part, employeeId);
            load();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể cấp phát",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== PHƯƠNG THỨC: recordReturn() — Ghi nhận việc hoàn trả phụ tùng. =====
    private void recordReturn() {
        int selected = tblTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hạng mục cần ghi nhận trả kho.");
            return;
        }
        int row = tblTable.convertRowIndexToModel(selected);
        try {
            int issued = getModelInt(row, "soLuongCapPhat", 0);
            int used = getModelInt(row, "soLuongSuDung", 0);
            int returned = getModelInt(row, "soLuongHoanTra", 0);
            int remaining = issued - used - returned;
            if (remaining <= 0)
                throw new SQLException("Hạng mục không còn số lượng phụ tùng có thể trả.");
            String quantity = JOptionPane.showInputDialog(this,
                    "Số phụ tùng thực tế nhận lại (còn có thể trả: " + remaining + "):", remaining);
            if (quantity == null)
                return;
            String employee = employeeId;
            if (employee == null || employee.isBlank())
                employee = ThaoTacNghiepVu.choose(this, "NhanVien", "maNhanVien",
                        "Nhân viên kho xác nhận hàng trả");
            if (employee == null)
                return;
            String repair = getModelString(row, "maPSC");
            String service = getModelString(row, "maDichVu");
            String part = getModelString(row, "maPhuTung");
            if (repair.isEmpty())
                repair = String.valueOf(model.getValueAt(row, 0));
            if (service.isEmpty())
                service = String.valueOf(model.getValueAt(row, 1));
            if (part.isEmpty())
                part = String.valueOf(model.getValueAt(row, 2));
            ChiTietPhieuSuaChuaDAO.recordPartReturn(repair, service, part, Integer.parseInt(quantity.trim()),
                    employee);
            load();
        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể ghi nhận trả kho",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== PHƯƠNG THỨC: edit(boolean) — Mở biểu mẫu để chỉnh sửa dữ liệu. =====
    private void edit(boolean existing) {

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

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(20, 22, 12, 22));

        Map<String, JComponent> inputs = new LinkedHashMap<>();

        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            int column = Arrays.asList(displayFields).indexOf(field);
            String value = existing && model.getValueAt(source, column) != null
                    ? model.getValueAt(source, column).toString()
                    : "";

            String type = KIEU_COT.get(field);

            JComponent input;

            OChonThamChieu cmbReference = OChonThamChieu.create(tableName, field, value);
            JComboBox<String> cmbOptions = taoLuaChon(field, value, existing);

            if (cmbReference != null) {
                cmbReference.selectId(value);
                input = cmbReference;
            } else if (cmbOptions != null) {
                input = cmbOptions;
            } else if (type.equals("datetime2")) {
                input = new OChonNgay(value);
            } else {
                JTextField txtInput = GiaoDien.field(value);
                txtInput.setText(value);

                boolean auto = i == 0 && !existing;

                if (auto)
                    txtInput.setText(generatedId);

                if (i == 0) {
                    txtInput.setEditable(false);
                }

                if (auto || (i == 0 && existing)) {
                    DinhDangKeThua.lock(txtInput);
                }

                input = txtInput;
            }

            inputs.put(field, input);

            JPanel pnlCell = new JPanel(new BorderLayout(0, 7));
            pnlCell.setOpaque(false);
            pnlCell.add(GiaoDien.label(nhanCot(field) + (COT_BAT_BUOC.contains(field) ? " *" : ""), 12, true,
                    GiaoDien.TEXT), BorderLayout.NORTH);

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

                winDialog.dispose();
                load();
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

    // ===== PHƯƠNG THỨC: filterOptionsFor(String, String) — Lọc options for. =====
    public static List<String> filterOptionsFor(String table, String field) {
        if ("PhieuNhapKho".equals(table) && "trangThai".equals(field)) {
            return List.of("Chờ xác nhận nhập kho", "Đã nhập kho", "Đã hủy");
        }
        if ("YeuCauNhapKho".equals(table) && "trangThai".equals(field)) {
            return List.of("Chờ duyệt", "Đã duyệt", "Từ chối", "Đã xử lý");
        }
        return Collections.emptyList();
    }

    // ===== PHƯƠNG THỨC: getStatusFilterCombo() — Lấy trạng thái bộ lọc combo. =====
    public JComboBox<String> getStatusFilterCombo() {
        String statusField = (page.equals("Phiếu cấp phát phụ tùng") || page.equals("Cấp phát phụ tùng"))
                ? "trangThaiHangMuc"
                : "trangThai";
        int column = Arrays.asList(displayFields).indexOf(statusField);
        return column >= 0 ? statusFilters.getFilter(column) : null;
    }

    // ===== PHƯƠNG THỨC: getTable() — Lấy bảng. =====
    public JTable getTable() {
        return tblTable;
    }

    // ===== PHƯƠNG THỨC: kiemTraDuLieu(Map<String,JComponent>) — Kiểm tra dữ liệu. =====
    private static Map<String, Object> kiemTraDuLieu(Map<String, JComponent> inputs)
            throws java.sql.SQLException {
        Map<String, Object> values = new LinkedHashMap<>();
        docCot(inputs, values, "maDichVu", "Mã dịch vụ", "varchar:5", true);
        docCot(inputs, values, "maPhuTung", "Mã phụ tùng", "varchar:5", true);
        docCot(inputs, values, "trangThaiHangMuc", "Trạng thái hạng mục", "nvarchar:50", true);
        docCot(inputs, values, "soLuongPhuTung", "Số lượng phụ tùng", "int:", true);
        docCot(inputs, values, "soLuongCapPhat", "Số lượng cấp phát", "int:", false);
        docCot(inputs, values, "soLuongSuDung", "Số lượng sử dụng", "int:", false);
        docCot(inputs, values, "soLuongHoanTra", "Số lượng hoàn trả", "int:", false);
        docCot(inputs, values, "ngayCapPhat", "Ngày cấp phát", "datetime2:0", false);
        docCot(inputs, values, "maNhanVienCapPhat", "Nhân viên cấp phát", "varchar:5", false);
        docCot(inputs, values, "ngayHoanTra", "Ngày hoàn trả", "datetime2:0", false);
        docCot(inputs, values, "maNhanVienXacNhanHoanTra", "Nhân viên xác nhận hoàn trả", "varchar:5", false);
        docCot(inputs, values, "ketQuaNghiemThu", "Kết quả nghiệm thu", "nvarchar:20", true);
        docCot(inputs, values, "ngayBatDauBaoHanh", "Ngày bắt đầu bảo hành", "datetime2:0", false);
        docCot(inputs, values, "thoiHanBaoHanh", "Thời hạn bảo hành", "int:", true);
        docCot(inputs, values, "ngayHetHanBaoHanh", "Ngày hết hạn bảo hành", "datetime2:0", false);
        for (Map.Entry<String, Object> e : values.entrySet()) {
            String field = e.getKey();
            Object value = e.getValue();
            if (value == null)
                continue;
            String raw = value.toString().trim();
            if (value instanceof Number number) {
                java.math.BigDecimal n = new java.math.BigDecimal(number.toString());

                if (field.equals("soLuongPhuTung")) {
                    boolean noPart = "PT000".equals(String.valueOf(values.get("maPhuTung")));
                    if ((noPart && n.signum() != 0) || (!noPart && n.signum() == 0))
                        throw new ThongBaoLoiSQL.Issue(field,
                                noPart ? "PT000 phải có số lượng phụ tùng bằng 0."
                                        : "Số lượng phụ tùng phải lớn hơn 0.");
                }

            }

            if (field.equals("maDichVu") && !raw.matches("DV[0-9]{3}"))
                throw new ThongBaoLoiSQL.Issue(field, "Mã dịch vụ phải có dạng DV001.");

            if (field.equals("maPhuTung") && !raw.matches("PT[0-9]{3}"))
                throw new ThongBaoLoiSQL.Issue(field, "Mã phụ tùng phải có dạng PT001.");

        }
        if (values.get("ngayBatDauBaoHanh") instanceof java.util.Date start
                && values.get("ngayHetHanBaoHanh") instanceof java.util.Date end && end.before(start)) {
            throw new ThongBaoLoiSQL.Issue("ngayHetHanBaoHanh", "Ngày kết thúc không hợp lệ.");
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
        values.put(field, value);
    }

    // ===== PHƯƠNG THỨC: taoLuaChon(String, String, boolean) — Tạo lua chon. =====
    private static JComboBox<String> taoLuaChon(String field, String value, boolean existing) {
        String[] choices = switch (field) {
        case "trangThaiHangMuc" -> new String[] { "Đang sửa chữa", "Thiếu phụ tùng", "Hoàn thành" };
        case "ketQuaNghiemThu" -> new String[] { "Không đạt", "Đạt" };
        default -> null;
        };
        if (choices == null)
            return null;
        JComboBox<String> combo = new OChonTimKiem<>(choices);
        combo.setFont(GiaoDien.font(12, false));
        combo.setEditable(true);
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

        // ===== PHƯƠNG THỨC: field(String) — Tạo thành phần nhập liệu cho một trường. =====
        public static JTextField field(String hint) {
            JTextField txtT = new JTextField();
            txtT.putClientProperty("JTextField.placeholderText", hint);
            txtT.setFont(font(13, false));
            txtT.setForeground(TEXT);
            txtT.setBorder(
                    new CompoundBorder(new LineBorder(LINE, 1, true), new EmptyBorder(10, 12, 10, 12)));
            return txtT;
        }

        // ===== PHƯƠNG THỨC: table(DefaultTableModel) — Lấy bảng dữ liệu tương ứng với màn hình. =====
        public static JTable table(DefaultTableModel m) {
            JTable tblT = new JTable(m) {
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
        case "maDichVu" -> "Mã dịch vụ";
        case "maPhuTung" -> "Mã phụ tùng";
        case "maPSC" -> "Mã phiếu sửa chữa";
        case "soLuongPhuTung" -> "Số lượng phụ tùng";
        case "soLuongCapPhat" -> "Số lượng cấp phát";
        case "soLuongSuDung" -> "Số lượng sử dụng";
        case "soLuongHoanTra" -> "Số lượng hoàn trả";
        case "ngayCapPhat" -> "Ngày cấp phát";
        case "maNhanVienCapPhat" -> "Nhân viên cấp phát";
        case "ngayHoanTra" -> "Ngày hoàn trả";
        case "maNhanVienXacNhanHoanTra" -> "Nhân viên xác nhận hoàn trả";
        case "trangThaiHangMuc" -> "Trạng thái hạng mục";
        case "ketQuaNghiemThu" -> "Kết quả nghiệm thu";
        case "ngayBatDauBaoHanh" -> "Ngày bắt đầu bảo hành";
        case "thoiHanBaoHanh" -> "Thời hạn bảo hành";
        case "ngayHetHanBaoHanh" -> "Ngày hết hạn bảo hành";
        default -> field;
        };
    }

    // ===== PHƯƠNG THỨC: docVanBan(javax.swing.JComponent) — Lấy văn bản từ thành phần nhập liệu. =====
    private static String docVanBan(javax.swing.JComponent control) {
        if (control instanceof OChonNgay pnlPicker)
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
