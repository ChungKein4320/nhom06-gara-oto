package gui;

import java.util.Locale;
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
import dao.ChonDAO;

import dao.KhachHangDAO;
import java.awt.*;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

// ===== GUI: MÀN HÌNH CÔNG CỤ TÌM KIẾM =====

public final class ManHinhTimKiem extends JPanel {
    // ===== THÀNH PHẦN GIAO DIỆN =====
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private final List<String> nhomDuocPhep;
    private final String nhom;

    // ===== PHƯƠNG THỨC: nhomTheoVaiTro(VaiTro) — Chọn nhóm chức năng theo vai trò người dùng. =====
    static List<String> nhomTheoVaiTro(VaiTro role) {
        Objects.requireNonNull(role,"Phải xác định vai trò tìm kiếm.");
        List<String> kyThuat=List.of("Phiếu tiếp nhận","Phiếu kiểm tra","Phiếu báo giá","Phiếu sửa chữa","Bảo hành","Lịch nhắc bảo dưỡng");
        List<String> kho=List.of("Tồn kho","Phiếu yêu cầu nhập kho","Phiếu nhập kho phụ tùng");
        List<String> banHang=List.of("Hóa đơn");
        List<String> quanLy=List.of("Nhà cung cấp","Nhân viên","Khách hàng","Phương tiện","Dịch vụ","Phụ tùng","Tài khoản","Khuyến mãi","Danh sách yêu cầu nhập");
        return switch(role) {
            case KY_THUAT -> kyThuat;
            case KHO -> kho;
            case THU_NGAN -> banHang;
            case QUAN_LY -> quanLy;
            case ADMIN -> {
                List<String> all=new ArrayList<>(); all.addAll(kyThuat); all.addAll(kho); all.addAll(banHang); all.addAll(quanLy);
                yield List.copyOf(all);
            }
        };
    }

    private final JPanel pnlBieuMau = new JPanel(new GridBagLayout());
    private final Map<String, JTextField> inputs = new LinkedHashMap<>();
    private final JRadioButton radTatCa = new JRadioButton("Tất cả", true);
    private final JRadioButton radNam = new JRadioButton("Nam");
    private final JRadioButton radNu = new JRadioButton("Nữ");
    private final DefaultTableModel model = new DefaultTableModel() {
        private static final long serialVersionUID = 1L;

        // ===== PHƯƠNG THỨC: isCellEditable(int, int) — Kiểm tra ô bảng có cho phép chỉnh sửa không. =====
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tblKetQua = GiaoDien.table(model);
    private final JButton btnTimKiem = GiaoDien.button("Tìm kiếm", GiaoDien.TEAL, Color.WHITE);
    private final JButton btnLamMoi = GiaoDien.button("Làm mới", GiaoDien.CREAM, GiaoDien.NAV);
    private final JLabel lblKetQua = GiaoDien.label("Chưa tìm kiếm", 12, false, GiaoDien.MUTED);
    private final JScrollPane scrBieuMau = new JScrollPane(pnlBieuMau);
    private SwingWorker<List<LinkedHashMap<String, Object>>, Void> worker;
    private String table;
    private String[] fields;
    private String[] displayFields;
    private int generation;
    // ===== KHỞI TẠO VÀ XỬ LÝ GIAO DIỆN =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ManHinhTimKiem(VaiTro) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public ManHinhTimKiem(VaiTro role) {
        this(role, nhomTheoVaiTro(role).get(0));
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ManHinhTimKiem(VaiTro, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public ManHinhTimKiem(VaiTro role, String nhom) {
        super(new BorderLayout(0, 14));
        nhomDuocPhep = nhomTheoVaiTro(role);
        if (nhom == null || !nhomDuocPhep.contains(nhom))
            throw new SecurityException("Không có quyền tìm kiếm nhóm dữ liệu: " + nhom);
        this.nhom = nhom;
        ButtonGroup grpGioiTinh = new ButtonGroup();
        grpGioiTinh.add(radTatCa);
        grpGioiTinh.add(radNam);
        grpGioiTinh.add(radNu);
        setBackground(GiaoDien.BG);
        setBorder(new EmptyBorder(24, 25, 24, 25));
        setPreferredSize(new Dimension(960, 740));
        JPanel pnlHeader = new JPanel(new BorderLayout(10, 14));
        pnlHeader.setOpaque(false);
        JLabel lblTitle = GiaoDien.label("Tìm kiếm: " + nhom, 25, true, GiaoDien.NAV);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        pnlHeader.add(lblTitle, BorderLayout.NORTH);
        add(pnlHeader, BorderLayout.NORTH);
        JPanel pnlDieuKien = GiaoDien.card(new BorderLayout(0, 8));
        pnlDieuKien.add(GiaoDien.label("Nhập thông tin cần tìm", 13, true, GiaoDien.TEXT),
                BorderLayout.NORTH);
        pnlBieuMau.setBackground(GiaoDien.WHITE);
        scrBieuMau.setBorder(null);
        scrBieuMau.getVerticalScrollBar().setUnitIncrement(20);
        pnlDieuKien.add(scrBieuMau, BorderLayout.CENTER);
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlButtons.setOpaque(false);
        pnlButtons.add(btnTimKiem);
        pnlButtons.add(btnLamMoi);
        pnlDieuKien.add(pnlButtons, BorderLayout.SOUTH);
        JPanel pnlKetQua = GiaoDien.card(new BorderLayout(0, 8));
        pnlKetQua.add(lblKetQua, BorderLayout.NORTH);
        tblKetQua.setAutoCreateRowSorter(true);
        JScrollPane scrKetQua = new JScrollPane(tblKetQua);
        BoCucBang.install(tblKetQua, scrKetQua);
        pnlKetQua.add(scrKetQua, BorderLayout.CENTER);
        JPanel pnlNoiDung = new JPanel(new BorderLayout(0, 14));
        pnlNoiDung.setOpaque(false);
        pnlNoiDung.add(pnlDieuKien, BorderLayout.NORTH);
        pnlNoiDung.add(pnlKetQua, BorderLayout.CENTER);
        scrKetQua.setPreferredSize(new Dimension(800, tblKetQua.getRowHeight() * 10 + 45));
        JScrollPane scrTrang = new JScrollPane(pnlNoiDung);
        scrTrang.setBorder(null);
        scrTrang.getVerticalScrollBar().setUnitIncrement(24);
        add(scrTrang, BorderLayout.CENTER);
        btnTimKiem.addActionListener(e -> timKiem());
        btnLamMoi.addActionListener(e -> lamMoi());
        taoBieuMau();
    }

    // ===== PHƯƠNG THỨC: taoBieuMau() — Tạo bieu mau. =====
    private void taoBieuMau() {
        String selected = nhom;
        if (!nhomDuocPhep.contains(selected))
            return;
        table = switch (selected) {
        case "Dịch vụ", "Danh mục dịch vụ" -> "DichVu";
        case "Phụ tùng", "Danh mục phụ tùng" -> "PhuTung";
        default -> DieuHuongManHinh.tableFor(selected);
        };
        String[] configuredFields = table == null ? null : ChonDAO.cacCot(table);
        if (configuredFields == null || configuredFields.length == 0)
            throw new IllegalArgumentException("Chưa cấu hình bảng dữ liệu để tìm kiếm nhóm: " + selected);
        fields = Arrays.stream(configuredFields).filter(field -> !field.equals("matKhau"))
                .toArray(String[]::new);
        displayFields = table.equals("KhachHang") ? Arrays.copyOf(fields, fields.length + 2) : fields.clone();
        if (table.equals("KhachHang")) {
            displayFields[displayFields.length - 2] = "bienSoXe";
            displayFields[displayFields.length - 1] = "tongDiemTichLuy";
        }
        inputs.clear();
        pnlBieuMau.removeAll();
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i], type = ChonDAO.kieuCot(table).get(field).split(":", 2)[0];
            JComponent input;
            if (field.equals("gioiTinh")) {
                JPanel pnlGioiTinh = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 7));
                pnlGioiTinh.setOpaque(false);
                for (JRadioButton radGioiTinh : new JRadioButton[] { radTatCa, radNam, radNu }) {
                    radGioiTinh.setOpaque(false);
                    radGioiTinh.setFont(GiaoDien.font(13, false));
                    pnlGioiTinh.add(radGioiTinh);
                }
                pnlGioiTinh.setToolTipText("Chọn Tất cả, Nam hoặc Nữ để lọc giới tính.");
                pnlGioiTinh.setPreferredSize(new Dimension(260, 38));
                input = pnlGioiTinh;
            } else {
                JTextField txtGiaTri = GiaoDien.field("");
                txtGiaTri.setPreferredSize(new Dimension(260, 38));
                txtGiaTri.setToolTipText(type.equals("date") || type.startsWith("datetime")
                        ? "Nhập ngày dd/MM/yyyy, yyyy-MM-dd hoặc một phần ngày cần tìm."
                        : "Có thể nhập một phần thông tin; không bắt buộc.");
                txtGiaTri.addActionListener(e -> timKiem());
                inputs.put(field, txtGiaTri);
                input = txtGiaTri;
            }
            JPanel pnlCell = new JPanel(new BorderLayout(0, 5));
            pnlCell.setOpaque(false);
            pnlCell.add(GiaoDien.label(nhanCot(field), 12, true, GiaoDien.TEXT), BorderLayout.NORTH);
            pnlCell.add(input, BorderLayout.CENTER);
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = i % 2;
            c.gridy = i / 2;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.insets = new Insets(5, 7, 5, 7);
            pnlBieuMau.add(pnlCell, c);
        }
        scrBieuMau.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        int prefH = pnlBieuMau.getPreferredSize().height + 8;
        scrBieuMau.setPreferredSize(new Dimension(800, Math.min(340, prefH)));
        lamMoi();
        pnlBieuMau.revalidate();
        pnlBieuMau.repaint();
        SwingUtilities.invokeLater(() -> scrBieuMau.getVerticalScrollBar().setValue(0));
    }

    // ===== PHƯƠNG THỨC: lamMoi() — Tải lại dữ liệu và cập nhật nội dung đang hiển thị. =====
    private void lamMoi() {
        generation++;
        if (worker != null)
            worker.cancel(true);
        inputs.values().forEach(input -> input.setText(""));
        model.setDataVector(new Object[0][], tieuDeCot(displayFields));
        radTatCa.setSelected(true);
        tblKetQua.clearSelection();
        tblKetQua.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        lblKetQua.setText("Chưa tìm kiếm");
        btnTimKiem.setEnabled(true);
        scrBieuMau.getVerticalScrollBar().setValue(0);
        if (tblKetQua.getRowSorter() != null)
            tblKetQua.getRowSorter().setSortKeys(null);
        timKiem(); // Tải lại toàn bộ dữ liệu theo quyền hiện tại, không chỉ xóa bảng.
    }

    // ===== PHƯƠNG THỨC: timKiem() — Tìm kiểm. =====
    private void timKiem() {
        if (!btnTimKiem.isEnabled())
            return;
        if (!nhomDuocPhep.contains(nhom)) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhóm dữ liệu được phép tìm kiếm.");
            return;
        }
        Map<String, String> criteria = new LinkedHashMap<>();
        inputs.forEach((field, input) -> {
            if (!input.getText().isBlank())
                criteria.put(field, input.getText().trim());
        });
        if (Arrays.asList(fields).contains("gioiTinh") && !radTatCa.isSelected())
            criteria.put("gioiTinh", radNam.isSelected() ? "Nam" : "Nữ");
        final String selectedTable = table;
        final String[] selectedFields = displayFields.clone();
        final int request = ++generation;
        btnTimKiem.setEnabled(false);
        model.setRowCount(0);
        lblKetQua.setText("Đang tìm kiếm…");
        worker = new SwingWorker<>() {
            // ===== PHƯƠNG THỨC: doInBackground() — Thực hiện truy vấn dữ liệu ở luồng nền. =====
            @Override
            protected List<LinkedHashMap<String, Object>> doInBackground() throws Exception {
                List<LinkedHashMap<String, Object>> rows = (selectedTable.equals("KhachHang")
                        ? new KhachHangDAO()
                        : ChonDAO.choBang(selectedTable)).getAll();
                rows.removeIf(row -> !BoLocTrangThai.trongBaMuoiNgay(selectedTable, row) || !khopDieuKien(row, criteria));
                return rows.size() > 10 ? new ArrayList<>(rows.subList(0, 10)) : rows;
            }

            // ===== PHƯƠNG THỨC: done() — Cập nhật giao diện sau khi tác vụ nền hoàn tất. =====
            @Override
            protected void done() {
                if (request != generation || isCancelled())
                    return;
                try {
                    List<LinkedHashMap<String, Object>> rows = get();
                    for (Map<String, Object> row : rows) {
                        Object[] values = new Object[selectedFields.length];
                        for (int i = 0; i < selectedFields.length; i++) {
                            Object value = row.get(selectedFields[i]);
                            values[i] = selectedFields[i].equals("gioiTinh") && value != null
                                    ? (Boolean.TRUE.equals(value) || "1".equals(value.toString()) ? "Nam"
                                            : "Nữ")
                                    : value;
                        }
                        model.addRow(values);
                    }
                    lblKetQua.setText(rows.isEmpty() ? "Không tìm thấy dữ liệu phù hợp."
                            : "Tìm thấy: " + rows.size() + " bản ghi");
                } catch (CancellationException ignored) {
                } catch (InterruptedException | ExecutionException ex) {
                    lblKetQua.setText("Không tải được kết quả.");
                    Throwable cause = ex.getCause();
                    if (cause == null)
                        cause = ex;
                    JOptionPane.showMessageDialog(ManHinhTimKiem.this, cause.getMessage(), "Lỗi tìm kiếm",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    if (request == generation)
                        btnTimKiem.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    // ===== PHƯƠNG THỨC: khopDieuKien(Map<String,Object>, Map<String,String>) — Kiểm tra bản ghi có thỏa điều kiện lọc. =====
    static boolean khopDieuKien(Map<String, Object> row, Map<String, String> criteria) {
        for (var condition : criteria.entrySet()) {
            Object value = row.get(condition.getKey());
            if (value == null)
                return false;
            String text = value.toString();
            if (condition.getKey().equals("gioiTinh"))
                text = Boolean.TRUE.equals(value) || "1".equals(text) ? "Nam 1" : "Nữ 0";
            else if (value instanceof Timestamp date)
                text += " "
                        + date.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            else if (value instanceof java.sql.Date date)
                text += " " + date.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            if (!OChonTimKiem.normalize(text).contains(OChonTimKiem.normalize(condition.getValue().trim())))
                return false;
        }
        return true;
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
        case "khachHangHienThi" -> "Khách hàng";
        case "tongThanhToan" -> "Tổng thanh toán";
        case "tongDiemTichLuy" -> "Tổng điểm tích lũy";
        case "gioiTinh" -> "Giới tính";
        case "soKM" -> "Số km";
        case "ketQuaChuanDoan" -> "Kết quả chẩn đoán";
        case "maNhanVien" -> "Mã nhân viên";
        case "maNV" -> "Mã nhân viên";
        case "hoTenNV" -> "Họ tên nhân viên";
        case "ngaySinh" -> "Ngày sinh";
        case "soDienThoai" -> "Số điện thoại";
        case "email" -> "Email";
        case "diaChi" -> "Địa chỉ";
        case "vaiTro" -> "Vai trò";
        case "trangThai" -> "Trạng thái";
        case "maKhachHang" -> "Mã khách hàng";
        case "hoTenKH" -> "Họ tên khách hàng";
        case "bienSoXe" -> "Biển số xe";
        case "maNguoiDuyetHuy" -> "Nhân viên duyệt hủy";
        case "lyDoHuy" -> "Lý do hủy";
        case "VIN" -> "Số khung (VIN)";
        case "bienSo" -> "Biển số";
        case "hangXe" -> "Hãng xe";
        case "dongXe" -> "Dòng xe";
        case "mauXe" -> "Màu xe";
        case "namSanXuat" -> "Năm sản xuất";
        case "soMay" -> "Số máy";
        case "soKm" -> "Số km";
        case "maDichVu" -> "Mã dịch vụ";
        case "tenDichVu" -> "Tên dịch vụ";
        case "moTa" -> "Mô tả";
        case "donGia" -> "Đơn giá";
        case "maPhuTung" -> "Mã phụ tùng";
        case "tenPhuTung" -> "Tên phụ tùng";
        case "loaiPhuTung" -> "Loại phụ tùng";
        case "maNCC" -> "Mã nhà cung cấp";
        case "tenNCC" -> "Tên nhà cung cấp";
        case "maPhuongThuc" -> "Mã phương thức thanh toán";
        case "tenPhuongThuc" -> "Tên phương thức thanh toán";
        case "maGiamGia" -> "Mã giảm giá";
        case "mucGiam" -> "Mức giảm";
        case "ngayTao" -> "Ngày tạo";
        case "ngayHetHan" -> "Ngày hết hạn";
        case "maTaiKhoan" -> "Mã tài khoản";
        case "tenDangNhap" -> "Tên đăng nhập";
        case "matKhau" -> "Mật khẩu";
        case "maTonKho" -> "Mã tồn kho";
        case "soLuongHienTai" -> "Số lượng hiện tại";
        case "soLuongToiThieu" -> "Số lượng tối thiểu";
        case "maPTN" -> "Mã phiếu tiếp nhận";
        case "ngayTiepNhan" -> "Ngày tiếp nhận";
        case "soODO" -> "Số ODO";
        case "maPKT" -> "Mã phiếu kiểm tra";
        case "ngayKiemTra" -> "Ngày kiểm tra";
        case "maBaoGia" -> "Mã báo giá";
        case "ngayLap" -> "Ngày lập";
        case "thoiGianXacNhan" -> "Thời gian xác nhận";
        case "maPSC" -> "Mã phiếu sửa chữa";
        case "ngayHoanThanh" -> "Ngày hoàn thành";
        case "maYCNK" -> "Mã yêu cầu nhập kho";
        case "maNhanVienDuyet" -> "Nhân viên duyệt";
        case "ngayDuyet" -> "Ngày duyệt";
        case "soLuongYeuCau" -> "Số lượng yêu cầu";
        case "donGiaDuKien" -> "Đơn giá dự kiến";
        case "soLuongThucNhan" -> "Số lượng thực nhận";
        case "maPNK" -> "Mã phiếu nhập kho";
        case "ngayNhap" -> "Ngày nhập";
        case "yeuCauDichVu" -> "Yêu cầu dịch vụ";
        case "hangMucKiemTra" -> "Hạng mục kiểm tra";
        case "ketQuaChanDoan" -> "Kết quả chẩn đoán";
        case "ketQuaKiemTra" -> "Kết quả kiểm tra";
        case "soLuongPhuTung" -> "Số lượng phụ tùng";
        case "donGiaPhuTung" -> "Đơn giá phụ tùng";
        case "soLuongCapPhat" -> "Số lượng cấp phát";
        case "soLuongSuDung" -> "Số lượng sử dụng";
        case "soLuongHoanTra" -> "Số lượng hoàn trả";
        case "ngayCapPhat" -> "Ngày cấp phát";
        case "maNhanVienCapPhat" -> "Nhân viên cấp phát";
        case "ngayHoanTra" -> "Ngày hoàn trả";
        case "maNhanVienXacNhanHoanTra" -> "Nhân viên xác nhận hoàn trả";
        case "chiPhiNhanCong" -> "Chi phí nhân công";
        case "trangThaiHangMuc" -> "Trạng thái hạng mục";
        case "ketQuaNghiemThu" -> "Kết quả nghiệm thu";
        case "ngayBatDauBaoHanh" -> "Ngày bắt đầu bảo hành";
        case "thoiHanBaoHanh" -> "Thời hạn bảo hành";
        case "ngayHetHanBaoHanh" -> "Ngày hết hạn bảo hành";
        case "soLuongNhap" -> "Số lượng nhập";
        case "donGiaNhap" -> "Đơn giá nhập";
        case "maHoaDon" -> "Mã hóa đơn";
        case "tienPhuTung" -> "Tiền phụ tùng";
        case "tienCong" -> "Tiền công";
        case "dichVuKhac" -> "Dịch vụ khác";
        case "thueVAT" -> "Thuế VAT";
        case "giamGia" -> "Giảm giá";
        case "trangThaiThanhToan" -> "Trạng thái thanh toán";
        case "soLuong" -> "Số lượng";
        case "maLBH" -> "Mã lịch bảo hành";
        case "ngayXuLy" -> "Ngày xử lý";
        case "mocDiem" -> "Mốc điểm";
        case "ngayBatDauChuKy" -> "Ngày đầu chu kỳ";
        case "ketQuaXuLy" -> "Kết quả xử lý";
        case "ghiChu" -> "Ghi chú";
        case "maLichNhac" -> "Mã lịch nhắc";
        case "ngayNhac" -> "Ngày nhắc";
        case "noiDung" -> "Nội dung";
        case "maLichSuTD" -> "Mã lịch sử tích điểm";
        case "ngayTichDiem" -> "Ngày tích điểm";
        case "soDiem" -> "Số điểm";
        case "lyDo" -> "Lý do";
        default -> field;
        };
    }

    // ===== PHƯƠNG THỨC: tieuDeCot(String[]) — Lấy nhãn hiển thị cho cột dữ liệu. =====
    private static String[] tieuDeCot(String[] fields) {
        return java.util.Arrays.stream(fields).map(field -> nhanCot(field)).toArray(String[]::new);
    }
}
