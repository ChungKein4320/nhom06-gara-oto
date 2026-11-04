package gui;

import java.util.List;
import javax.swing.border.*;
import java.sql.*;
import java.math.BigDecimal;
import connectDB.KetNoiCSDL;

import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import javax.swing.*;

// ===== GUI: BieuMauChiTietPhieuBaoGia =====
final class BieuMauChiTietPhieuBaoGia extends JPanel {
    private static final String[] CAC_COT = new String[] { "maBaoGia", "maDichVu", "maPhuTung",
            "soLuongPhuTung", "donGiaPhuTung", "chiPhiNhanCong", "trangThai" };
    private static final Map<String, String> KIEU_COT = Map.ofEntries(Map.entry("maBaoGia", "varchar"),
            Map.entry("maDichVu", "varchar"), Map.entry("maPhuTung", "varchar"),
            Map.entry("soLuongPhuTung", "int"), Map.entry("donGiaPhuTung", "decimal"),
            Map.entry("chiPhiNhanCong", "decimal"), Map.entry("trangThai", "nvarchar"));
    private static final Set<String> COT_BAT_BUOC = Set.of("maBaoGia", "maDichVu", "maPhuTung",
            "soLuongPhuTung", "donGiaPhuTung", "chiPhiNhanCong", "trangThai");
    private static final String[] CAC_KHOA = new String[] { "maBaoGia", "maDichVu", "maPhuTung" };

    private static final long serialVersionUID = 1L;
    private final Map<String, JComponent> inputs = new LinkedHashMap<>();
    private final GiaDanhMuc prices;

    // ===== KHỞI TẠO ĐỐI TƯỢNG: BieuMauChiTietPhieuBaoGia(String, Map<String,Object>, Set<String>) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    BieuMauChiTietPhieuBaoGia(String table, Map<String, Object> row, Set<String> excluded) {
        this(table, row, excluded, false);
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: BieuMauChiTietPhieuBaoGia(String, Map<String,Object>, Set<String>, boolean) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    BieuMauChiTietPhieuBaoGia(String table, Map<String, Object> row, Set<String> excluded, boolean readOnly) {
        this(table, row, excluded, readOnly, isExisting(table, row));
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: BieuMauChiTietPhieuBaoGia(String, Map<String,Object>, Set<String>, boolean, boolean) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    BieuMauChiTietPhieuBaoGia(String table, Map<String, Object> row, Set<String> excluded, boolean readOnly,
            boolean existing) {
        super(new GridBagLayout());
        table = "ChiTietPhieuBaoGia";
        setBackground(GiaoDien.WHITE);
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        int index = 0;
        for (String field : CAC_COT) {
            if (excluded.contains(field))
                continue;
            String type = KIEU_COT.get(field);
            Object old = row == null ? null : row.get(field);
            String value = old == null ? "" : old.toString();
            JComponent input;
            OChonThamChieu cmbReference = readOnly ? null : OChonThamChieu.create(table, field, value);
            JComboBox<String> cmbOptions = taoLuaChon(field, value, existing);
            if (readOnly) {
                JTextField txtDisplay = GiaoDien.field("");
                if (old != null) {
                    switch (old) {
                    case Boolean flag -> value = flag ? "Nam" : "Nữ";
                    case java.sql.Timestamp date -> value = date.toLocalDateTime()
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    case java.sql.Date date -> value = date.toLocalDate()
                            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    default -> {
                    }
                    }
                }
                txtDisplay.setText(value);
                txtDisplay.setToolTipText(value);
                DinhDangKeThua.lock(txtDisplay);
                input = txtDisplay;
            } else if (cmbReference != null) {
                cmbReference.selectId(value);
                input = cmbReference;
            } else if (cmbOptions != null)
                input = cmbOptions;
            else {
                JTextField txtValue = GiaoDien.field("");
                if (!existing && Set.of("soLuongPhuTung").contains(field))
                    value = "1";
                if (!existing && (type.equals("int") || type.equals("decimal")) && value.isEmpty())
                    value = "0";
                txtValue.setText(value);
                input = txtValue;

            }
            inputs.put(field, input);
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = index % 2;
            c.gridy = index / 2;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.insets = new Insets(6, 8, 6, 8);
            JPanel pnlCell = new JPanel(new BorderLayout(0, 5));
            pnlCell.setOpaque(false);
            String fieldLabel = nhanCot(field);
            String label = fieldLabel + (COT_BAT_BUOC.contains(field) ? " *" : "");

            pnlCell.add(GiaoDien.label(label, 12, true, GiaoDien.TEXT), BorderLayout.NORTH);
            input.setPreferredSize(new Dimension(260, 38));
            pnlCell.add(input, BorderLayout.CENTER);
            add(pnlCell, c);
            index++;
        }
        if (!readOnly && inputs.get("maPhuTung") instanceof OChonThamChieu part
                && inputs.get("soLuongPhuTung") instanceof JTextField quantity) {
            Runnable syncQuantity = () -> {
                boolean noPart = "PT000".equals(part.selectedId());
                if (noPart)
                    quantity.setText("0");
                quantity.setEnabled(!noPart);
                if (!noPart && quantity.getText().trim().equals("0"))
                    quantity.setText("1");
            };
            part.addActionListener(e -> syncQuantity.run());
            syncQuantity.run();
        }
        prices = readOnly ? null : GiaDanhMuc.bind(inputs);
    }

    // ===== PHƯƠNG THỨC: isExisting(String, Map<String,Object>) — Kiểm tra dữ liệu đã tồn tại. =====
    static boolean isExisting(String table, Map<String, Object> row) {
        if (row == null || row.isEmpty())
            return false;

        String[] keys = CAC_KHOA;
        if (keys != null && keys.length > 1) {
            for (int i = 1; i < keys.length; i++) {
                Object val = row.get(keys[i]);
                if (val != null && !String.valueOf(val).trim().isEmpty())
                    return true;
            }
            return false;
        }
        return true;
    }

    // ===== PHƯƠNG THỨC: read() — Đọc và chuyển đổi dữ liệu từ biểu mẫu. =====
    Map<String, Object> read() throws SQLException {
        if (prices != null)
            prices.validate();
        return kiemTraDuLieu(inputs);
    }
    // ===== PHƯƠNG THỨC: lockField(String) — Khóa trường dữ liệu để không cho chỉnh sửa. =====
    void lockField(String field) {
        if (inputs.get(field) != null)
            DinhDangKeThua.lock(inputs.get(field));
    }

    // ===== PHƯƠNG THỨC: getPreferredSize() — Tính kích thước ưu tiên của thành phần. =====
    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (getParent() instanceof JViewport vp && vp.getWidth() > 0)
            return new Dimension(vp.getWidth(), d.height);
        return d;
    }

    // ===== PHƯƠNG THỨC: kiemTraDuLieu(Map<String,JComponent>) — Kiểm tra dữ liệu. =====
    private static Map<String, Object> kiemTraDuLieu(Map<String, JComponent> inputs)
            throws java.sql.SQLException {
        Map<String, Object> values = new LinkedHashMap<>();
        docCot(inputs, values, "maDichVu", "Mã dịch vụ", "varchar:5", true);
        docCot(inputs, values, "maPhuTung", "Mã phụ tùng", "varchar:5", true);
        docCot(inputs, values, "soLuongPhuTung", "Số lượng phụ tùng", "int:", true);
        docCot(inputs, values, "donGiaPhuTung", "Đơn giá phụ tùng", "decimal:18,2", true);
        docCot(inputs, values, "chiPhiNhanCong", "Chi phí nhân công", "decimal:18,2", true);
        docCot(inputs, values, "trangThai", "Trạng thái", "nvarchar:100", true);
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

        if ("PT000".equals(String.valueOf(values.get("maPhuTung")))) {

            if (values.get("donGiaPhuTung") instanceof Number price
                    && new java.math.BigDecimal(price.toString()).signum() != 0)
                throw new ThongBaoLoiSQL.Issue("donGiaPhuTung", "Đơn giá phụ tùng của PT000 phải bằng 0.");
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
        case "trangThai" -> new String[] { "Chờ xác nhận", "Chờ sửa chữa", "Thiếu phụ tùng + Chờ xác nhận" };
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

    private static final class GiaDanhMuc {

        private final List<Price> prices = new ArrayList<>();

        // ===== KHỞI TẠO ĐỐI TƯỢNG: GiaDanhMuc() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
        private GiaDanhMuc() {
        }

        // ===== PHƯƠNG THỨC: bind(Map<String,JComponent>) — Gán giá trị tham số cho câu lệnh SQL. =====
        static GiaDanhMuc bind(Map<String, JComponent> inputs) {
            GiaDanhMuc binding = new GiaDanhMuc();
            binding.add(inputs, "maDichVu", "DichVu", "chiPhiNhanCong");
            binding.add(inputs, "maPhuTung", "PhuTung", "donGiaPhuTung");
            return binding;
        }

        // ===== PHƯƠNG THỨC: add(Map<String,JComponent>, String, String, String) — Thêm nghiệp vụ tương ứng. =====
        private void add(Map<String, JComponent> inputs, String key, String table, String field) {
            if (inputs.get(key) instanceof OChonThamChieu source
                    && inputs.get(field) instanceof JTextField target) {
                DinhDangKeThua.lock(target);
                target.setToolTipText("Tự lấy đơn giá từ " + nhanCot(key).toLowerCase());
                Price price = new Price(source, target, key, table, field);
                prices.add(price);
                source.addActionListener(e -> price.refresh());
                price.refresh();
            }
        }

        // ===== PHƯƠNG THỨC: validate() — Kiểm tra tính hợp lệ của nghiệp vụ tương ứng. =====
        void validate() throws SQLException {
            for (Price price : prices)
                price.validate();
        }

        private static final class Price {
            final OChonThamChieu source;
            final JTextField target;
            final String key, table, field;
            String problem;

            // ===== KHỞI TẠO ĐỐI TƯỢNG: Price(OChonThamChieu, JTextField, String, String, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
            Price(OChonThamChieu source, JTextField target, String key, String table, String field) {
                this.source = source;
                this.target = target;
                this.key = key;
                this.table = table;
                this.field = field;
            }

            // ===== PHƯƠNG THỨC: fetch() — Đọc bản ghi cần thiết từ cơ sở dữ liệu. =====
            BigDecimal fetch() throws SQLException {
                if (source.selectedId().isEmpty())
                    throw new SQLException("Vui lòng chọn " + nhanCot(key).toLowerCase() + ".");
                try (Connection conn = KetNoiCSDL.getConnection();
                        PreparedStatement ps = conn.prepareStatement(
                                "SELECT donGia FROM dbo.[" + table + "] WHERE [" + key + "]=?")) {
                    ps.setString(1, source.selectedId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next())
                            throw new SQLException("Không tìm thấy " + nhanCot(key).toLowerCase() + ".");
                        BigDecimal price = rs.getBigDecimal("donGia");
                        if (price == null || price.signum() < 0)
                            throw new SQLException("Đơn giá danh mục không hợp lệ.");
                        return price;
                    }
                }
            }

            // ===== PHƯƠNG THỨC: refresh() — Tải lại dữ liệu và làm mới giao diện. =====
            void refresh() {
                problem = null;
                target.setText("");
                if (source.selectedId().isEmpty())
                    return;
                try {
                    target.setText(fetch().toPlainString());
                } catch (SQLException ex) {
                    problem = ex.getMessage();
                    target.setToolTipText(problem);
                }
            }

            // ===== PHƯƠNG THỨC: validate() — Kiểm tra tính hợp lệ của nghiệp vụ tương ứng. =====
            void validate() throws SQLException {
                if (problem != null) {
                    refresh();
                    if (problem != null)
                        throw new ThongBaoLoiSQL.Issue(key, problem);
                }
                BigDecimal current;
                try {
                    current = fetch();
                } catch (SQLException ex) {
                    target.setText("");
                    throw new ThongBaoLoiSQL.Issue(key, ex.getMessage());
                }
                if (target.getText().isBlank() || current.compareTo(new BigDecimal(target.getText())) != 0) {
                    target.setText(current.toPlainString());
                    throw new ThongBaoLoiSQL.Issue(field,
                            "Giá danh mục đã thay đổi, giá mới đã được cập nhật. Vui lòng kiểm tra rồi lưu lại.");
                }
            }
        }
    }

    private static final class GiaoDien {

        private static final Color WHITE = Color.WHITE;
        private static final Color TEXT = new Color(0x21383C);
        private static final Color LINE = new Color(0xE4EBE9);

        // ===== PHƯƠNG THỨC: font(int, boolean) — Tạo phông chữ theo cỡ và kiểu chữ. =====
        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
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
    }

    // ===== PHƯƠNG THỨC: nhanCot(String) — Tạo tiêu đề hiển thị cho cột bảng. =====
    private static String nhanCot(String field) {
        return switch (field) {
        case "trangThai" -> "Trạng thái";
        case "maDichVu" -> "Mã dịch vụ";
        case "maPhuTung" -> "Mã phụ tùng";
        case "maBaoGia" -> "Mã báo giá";
        case "soLuongPhuTung" -> "Số lượng phụ tùng";
        case "donGiaPhuTung" -> "Đơn giá phụ tùng";
        case "chiPhiNhanCong" -> "Chi phí nhân công";
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
