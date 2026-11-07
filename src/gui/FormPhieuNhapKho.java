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

import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Màn hình GUI: Quản lý danh sách Phiếu nhập kho (FormPhieuNhapKho).
 * - Nghiệp vụ:
 *   + Không cho phép thêm mới trực tiếp (bắt buộc lập từ Yêu cầu nhập kho 'Đã duyệt').
 *   + Chỉnh sửa số lượng thực nhận, số lượng nhập chấp nhận, đơn giá nhập của phiếu ở trạng thái 'Chờ xác nhận nhập kho'.
 *   + Gắn nút 'Xác nhận nhập kho' (qua ThaoTacNghiepVu -> PhieuNhapKhoDAO.confirmActualStockIn) để cộng tồn kho và hoàn ứng phụ tùng thiếu.
 * - Luồng chính: taiDanhSachTuCSDL() -> moFormThemHoacSua(true) / moChiTietPhieu() -> TrinhSuaPhieuNhapKho.
 */
public final class FormPhieuNhapKho extends JPanel {
    private static final String[] CAC_COT = new String[] { "maPNK", "maNCC", "maNhanVien", "ngayNhap",
            "trangThai", "maYCNK" };

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private final String tableName;
    private final VaiTro role;
    private final String employeeId;
    private transient dao.PhieuNhapKhoDAO dao;
    private final JTable tblTable;
    private final DefaultTableModel model;
    private final JTextField txtSearch;
    private final JLabel lblCount;
    private final String[] fields;
    private final String[] displayFields;
    private final BoLocTrangThai statusFilters;

    // ===== PHƯƠNG THỨC: readObject(java.io.ObjectInputStream) — Khôi phục DAO sau khi giải tuần tự hóa. =====
    @java.io.Serial
    private void readObject(java.io.ObjectInputStream input)
            throws java.io.IOException, ClassNotFoundException {
        input.defaultReadObject();
        dao = new dao.PhieuNhapKhoDAO();
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: FormPhieuNhapKho(String) — Khởi tạo màn hình phiếu nhập kho theo tên trang. =====
    public FormPhieuNhapKho(String page) {
        this(page, null, null);
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: FormPhieuNhapKho(String, String) — Khởi tạo màn hình phiếu nhập kho kèm mã nhân viên. =====
    public FormPhieuNhapKho(String page, String employeeId) {
        this(page, null, employeeId);
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: FormPhieuNhapKho(String, VaiTro, String) — Khởi tạo đầy đủ màn hình phiếu nhập kho và phân quyền. =====
    public FormPhieuNhapKho(String page, VaiTro role, String employeeId) {
        // 1. Khởi tạo giao diện chính và cấu hình DAO
        this.role = role;
        this.employeeId = employeeId;
        setLayout(new BorderLayout(0, 18));
        setBackground(GiaoDien.BG);
        setBorder(new EmptyBorder(26, 26, 26, 26));

        tableName = "PhieuNhapKho";

        dao = new dao.PhieuNhapKhoDAO();

        fields = CAC_COT;
        displayFields = fields;

        // 2. Tạo các thành phần Swing (tiêu đề, thanh tìm kiếm, bảng danh sách, thanh nút chức năng)
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

        // Nút 'Thêm mới' bị khóa (canAdd = false) vì PNK bắt buộc lập từ YCNK đã duyệt
        JButton btnAdd = GiaoDien.button("Thêm mới", GiaoDien.TEAL, Color.WHITE);
        boolean canAdd = false;

        btnAdd.setEnabled(canAdd);
        btnAdd.addActionListener(e -> moFormThemHoacSua(false));

        {
            pnlToolbar.add(btnAdd, BorderLayout.EAST);
        }

        pnlCard.add(pnlToolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[] { "Mã phiếu nhập kho", "Mã nhà cung cấp", "Mã nhân viên",
                "Ngày nhập", "Trạng thái", "Mã yêu cầu nhập kho" }, 0) {
            // ===== PHƯƠNG THỨC: isCellEditable(int, int) — Khóa chỉnh sửa trực tiếp trên bảng danh sách. =====
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
            capNhatSoLuongBanGhi();
        });
        statusFilters.setBorder(new EmptyBorder(6, 0, 0, 0));
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

        btnEdit.setEnabled(false);

        // 3. Đăng ký sự kiện cho các nút chức năng, gắn nút nghiệp vụ từ ThaoTacNghiepVu và sự kiện chọn dòng
        btnRefresh.addActionListener(e -> lamMoi());
        btnEdit.addActionListener(e -> moFormThemHoacSua(true));

        pnlButtons.add(btnRefresh);

        {
            JButton btnDetails = GiaoDien.button("Chi tiết phiếu", GiaoDien.CREAM, GiaoDien.NAV);
            btnDetails.addActionListener(e -> moChiTietPhieu());
            pnlButtons.add(btnDetails);
        }

        ThaoTacNghiepVu.install(pnlButtons, tableName, page, this::layBanGhiDangChon, this::taiDanhSachTuCSDL, pnlButtons,
                role, employeeId, tblTable);

        {
            pnlButtons.add(btnEdit);

        }

        tblTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selected = tblTable.getSelectedRow();
                if (selected < 0) {
                    btnEdit.setEnabled(false);
                } else {
                    int row = tblTable.convertRowIndexToModel(selected);
                    int statusCol = Arrays.asList(displayFields).indexOf("trangThai");
                    String st = statusCol >= 0 ? String.valueOf(model.getValueAt(row, statusCol)) : "";
                    boolean isPending = "Chờ xác nhận nhập kho".equals(st);
                    btnEdit.setEnabled(isPending && (role == VaiTro.KHO || role == VaiTro.ADMIN));
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

        tblTable.addMouseListener(new java.awt.event.MouseAdapter() {
            // ===== PHƯƠNG THỨC: mouseClicked(java.awt.event.MouseEvent) — Nhấp đúp dòng để mở chi tiết phiếu nhập kho. =====
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2 && tblTable.rowAtPoint(event.getPoint()) >= 0) {
                    {
                        moChiTietPhieu();
                    }
                }
            }
        });

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            // ===== PHƯƠNG THỨC: insertUpdate(javax.swing.event.DocumentEvent) — Lọc danh sách khi thêm ký tự tìm kiếm. =====
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                locDanhSachTheoTimKiem();
            }

            // ===== PHƯƠNG THỨC: removeUpdate(javax.swing.event.DocumentEvent) — Lọc danh sách khi xóa ký tự tìm kiếm. =====
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                locDanhSachTheoTimKiem();
            }

            // ===== PHƯƠNG THỨC: changedUpdate(javax.swing.event.DocumentEvent) — Lọc danh sách khi thuộc tính văn bản thay đổi. =====
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                locDanhSachTheoTimKiem();
            }
        });

        // Khởi tạo các lựa chọn lọc cố định theo quy định nghiệp vụ và trạng thái mặc định ('Chờ xác nhận nhập kho')
        String statusField = "trangThai";
        int statusCol = Arrays.asList(displayFields).indexOf(statusField);
        if (statusCol >= 0) {
            List<String> options = filterOptionsFor(tableName, statusField);
            if (!options.isEmpty()) {
                statusFilters.setPredefinedOptions(statusCol, options);
            }
        }

        if (statusCol >= 0) {
            statusFilters.selectDefault(statusCol, "Chờ xác nhận nhập kho");
        }

        // 8. Tải dữ liệu ban đầu từ SQL Server lên bảng
        taiDanhSachTuCSDL();
    }

    // ===== PHƯƠNG THỨC: lamMoi() — Xóa bộ lọc tìm kiếm và tải lại danh sách phiếu nhập kho. =====
    private void lamMoi() {
        txtSearch.setText("");
        statusFilters.reset();
        taiDanhSachTuCSDL();
        tblTable.clearSelection();
        tblTable.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
    }

    // ===== PHƯƠNG THỨC: taiDanhSachTuCSDL() — Gọi PhieuNhapKhoDAO.getAll() để nạp danh sách phiếu nhập kho lên bảng. =====
    private void taiDanhSachTuCSDL() {
        try {
            // 6. Gọi DAO lấy danh sách phiếu nhập kho và lọc trong 30 ngày gần nhất
            model.setRowCount(0);
            List<LinkedHashMap<String, Object>> rows = dao.getAll();
            rows.removeIf(row -> !BoLocTrangThai.trongBaMuoiNgay(tableName, row));

            // 8. Đổ từng bản ghi vào model của JTable (đồng bộ trạng thái cũ 'Chờ duyệt' thành 'Chờ xác nhận nhập kho' nếu có maYCNK)
            for (Map<String, Object> row : rows) {
                {
                    Object rawYcn = row.get("maYCNK");
                    Object rawStatus = row.get("trangThai");
                    boolean isUc009 = rawYcn != null && !rawYcn.toString().isBlank();
                    if (isUc009 && "Chờ duyệt".equals(rawStatus)) {
                        row.put("trangThai", "Chờ xác nhận nhập kho");
                    }
                }

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
        } catch (SQLException ex) {
            // 7. Hiển thị thông báo lỗi nếu mất kết nối SQL Server
            lblCount.setText("Lỗi kết nối SQL Server");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi cơ sở dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== PHƯƠNG THỨC: locDanhSachTheoTimKiem() — Lọc các dòng trên bảng theo từ khóa trong ô tìm kiếm. =====
    private void locDanhSachTheoTimKiem() {
        statusFilters.search(txtSearch.getText());
    }

    // ===== PHƯƠNG THỨC: capNhatSoLuongBanGhi() — Cập nhật nhãn tổng số bản ghi đang hiển thị trên bảng. =====
    private void capNhatSoLuongBanGhi() {
        lblCount.setText("Hiển thị: " + tblTable.getRowCount() + " / " + model.getRowCount() + " bản ghi");
    }

    // ===== PHƯƠNG THỨC: moFormThemHoacSua(boolean) — Chặn thêm mới trực tiếp hoặc mở TrinhSuaPhieuNhapKho để sửa phiếu. =====
    private void moFormThemHoacSua(boolean existing) {

        if (!existing) {
            JOptionPane.showMessageDialog(this,
                    """
                            Phiếu nhập kho phải được lập từ Yêu cầu nhập kho đã duyệt.
                            Vui lòng chọn menu 'Phiếu yêu cầu nhập kho', chọn yêu cầu có trạng thái 'Đã duyệt' và nhấn 'Lập phiếu nhập kho'.
                            """
                            .strip(),
                    "Quy trình nhập kho", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Map<String, Object> record = layBanGhiDaChonHoacThongBao("chỉnh sửa");
        if (record == null)
            return;

        TrinhSuaPhieuNhapKho.show(this, tableName, record, role, employeeId);
        taiDanhSachTuCSDL();

    }

    // ===== PHƯƠNG THỨC: layBanGhiDangChon() — Lấy dữ liệu của dòng phiếu nhập kho đang chọn trên bảng. =====
    private Map<String, Object> layBanGhiDangChon() {
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

    // ===== PHƯƠNG THỨC: layBanGhiDaChonHoacThongBao(String) — Lấy dòng đang chọn hoặc hiển thị cảnh báo nếu chưa chọn. =====
    private Map<String, Object> layBanGhiDaChonHoacThongBao(String actionDesc) {
        Map<String, Object> record = layBanGhiDangChon();
        if (record == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một bản ghi" + (actionDesc == null ? "." : " để " + actionDesc + "."),
                    "Chưa chọn bản ghi", JOptionPane.WARNING_MESSAGE);
        }
        return record;
    }

    // ===== PHƯƠNG THỨC: moChiTietPhieu() — Mở cửa sổ xem chi tiết phiếu nhập kho đã chọn. =====
    private void moChiTietPhieu() {
        // 4. Lấy bản ghi đang chọn trên bảng và mở cửa sổ TrinhSuaPhieuNhapKho ở chế độ chỉ xem
        Map<String, Object> record = layBanGhiDaChonHoacThongBao("xem chi tiết");
        if (record != null) {
            {
                TrinhSuaPhieuNhapKho.view(this, tableName, record);
            }
            taiDanhSachTuCSDL();
        }
    }

    // ===== PHƯƠNG THỨC: filterOptionsFor(String, String) — Trả về danh sách trạng thái lọc cố định cho bảng. =====
    public static List<String> filterOptionsFor(String table, String field) {
        if ("PhieuNhapKho".equals(table) && "trangThai".equals(field)) {
            return List.of("Chờ xác nhận nhập kho", "Đã nhập kho", "Đã hủy");
        }
        if ("YeuCauNhapKho".equals(table) && "trangThai".equals(field)) {
            return List.of("Chờ duyệt", "Đã duyệt", "Từ chối", "Đã xử lý");
        }
        return Collections.emptyList();
    }

    // ===== PHƯƠNG THỨC: getStatusFilterCombo() — Lấy combobox lọc trạng thái trên thanh công cụ. =====
    public JComboBox<String> getStatusFilterCombo() {
        String statusField = "trangThai";
        int column = Arrays.asList(displayFields).indexOf(statusField);
        return column >= 0 ? statusFilters.getFilter(column) : null;
    }

    // ===== PHƯƠNG THỨC: getTable() — Lấy đối tượng JTable hiển thị danh sách phiếu nhập kho. =====
    public JTable getTable() {
        return tblTable;
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
                        column.setHeaderValue(layTenHienThiCuaCot(getModel().getColumnName(column.getModelIndex())));
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

    // ===== PHƯƠNG THỨC: layTenHienThiCuaCot(String) — Trả về nhãn tiếng Việt cho tên cột phiếu nhập kho. =====
    private static String layTenHienThiCuaCot(String field) {
        return switch (field) {
        case "maNhanVien" -> "Mã nhân viên";
        case "trangThai" -> "Trạng thái";
        case "maNCC" -> "Mã nhà cung cấp";
        case "maYCNK" -> "Mã yêu cầu nhập kho";
        case "maPNK" -> "Mã phiếu nhập kho";
        case "ngayNhap" -> "Ngày nhập";
        default -> field;
        };
    }
}
