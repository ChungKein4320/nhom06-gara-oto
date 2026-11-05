package gui;

import javax.swing.border.*;

import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import javax.swing.*;

// ===== GUI: BieuMauChiTietPhieuNhapKho =====
final class BieuMauChiTietPhieuNhapKho extends JPanel {
    private static final String[] CAC_COT = new String[] { "maPNK", "maPhuTung", "soLuongYeuCau",
            "soLuongThucNhan", "soLuongNhap", "donGiaNhap" };
    private static final Map<String, String> KIEU_COT = Map.ofEntries(Map.entry("maPNK", "varchar"),
            Map.entry("maPhuTung", "varchar"), Map.entry("soLuongYeuCau", "int"),
            Map.entry("soLuongThucNhan", "int"), Map.entry("soLuongNhap", "int"),
            Map.entry("donGiaNhap", "decimal"));
    private static final Set<String> COT_BAT_BUOC = Set.of("maPNK", "maPhuTung", "soLuongThucNhan",
            "soLuongNhap", "donGiaNhap");
    private static final String[] CAC_KHOA = new String[] { "maPNK", "maPhuTung" };

    private static final long serialVersionUID = 1L;
    private final Map<String, JComponent> inputs = new LinkedHashMap<>();

    // ===== KHỞI TẠO ĐỐI TƯỢNG: BieuMauChiTietPhieuNhapKho(String, Map<String,Object>, Set<String>) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    BieuMauChiTietPhieuNhapKho(String table, Map<String, Object> row, Set<String> excluded) {
        this(table, row, excluded, false);
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: BieuMauChiTietPhieuNhapKho(String, Map<String,Object>, Set<String>, boolean) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    BieuMauChiTietPhieuNhapKho(String table, Map<String, Object> row, Set<String> excluded,
            boolean readOnly) {
        this(table, row, excluded, readOnly, isExisting(table, row));
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: BieuMauChiTietPhieuNhapKho(String, Map<String,Object>, Set<String>, boolean, boolean) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    BieuMauChiTietPhieuNhapKho(String table, Map<String, Object> row, Set<String> excluded, boolean readOnly,
            boolean existing) {
        super(new GridBagLayout());
        table = "ChiTietPhieuNhapKho";
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
            }

            else {
                JTextField txtValue = GiaoDien.field("");

                if (!existing && !((field.equals("soLuongThucNhan") || field.equals("soLuongNhap")))
                        && (type.equals("int") || type.equals("decimal")) && value.isEmpty())
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
        docCot(inputs, values, "maPhuTung", "Mã phụ tùng", "varchar:5", true);
        docCot(inputs, values, "soLuongYeuCau", "Số lượng yêu cầu", "int:", false);
        docCot(inputs, values, "soLuongThucNhan", "Số lượng thực nhận", "int:", true);
        docCot(inputs, values, "soLuongNhap", "Số lượng nhập", "int:", true);
        docCot(inputs, values, "donGiaNhap", "Đơn giá nhập", "decimal:18,2", true);
        for (Map.Entry<String, Object> e : values.entrySet()) {
            String field = e.getKey();
            Object value = e.getValue();
            if (value == null)
                continue;
            String raw = value.toString().trim();
            if (value instanceof Number number) {
                java.math.BigDecimal n = new java.math.BigDecimal(number.toString());
                if (field.equals("soLuongYeuCau") && n.signum() == 0)
                    throw new ThongBaoLoiSQL.Issue(field, "Số lượng yêu cầu phải lớn hơn 0.");

            }

            if (field.equals("maPhuTung") && !raw.matches("PT[0-9]{3}"))
                throw new ThongBaoLoiSQL.Issue(field, "Mã phụ tùng phải có dạng PT001.");

        }

        {
            Object reqObj = values.get("soLuongYeuCau");
            Object recObj = values.get("soLuongThucNhan");
            Object accObj = values.get("soLuongNhap");

            if (recObj instanceof Number rec && accObj instanceof Number acc) {
                if (acc.intValue() > rec.intValue())
                    throw new ThongBaoLoiSQL.Issue("soLuongNhap", "Số lượng chấp nhận (" + acc.intValue()
                            + ") không được vượt quá số lượng thực nhận (" + rec.intValue() + ").");
            }
            if (reqObj instanceof Number req && recObj instanceof Number rec) {
                if (rec.intValue() > req.intValue())
                    throw new ThongBaoLoiSQL.Issue("soLuongThucNhan", "Số lượng thực nhận (" + rec.intValue()
                            + ") không được vượt quá số lượng yêu cầu (" + req.intValue() + ").");
            }
        }

        if ("PT000".equals(String.valueOf(values.get("maPhuTung")))) {

            throw new ThongBaoLoiSQL.Issue("maPhuTung",
                    "PT000 chỉ dùng để biểu thị hạng mục không sử dụng phụ tùng; không được nhập kho hoặc bán như phụ tùng.");

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
        case "maPhuTung" -> "Mã phụ tùng";
        case "soLuongYeuCau" -> "Số lượng yêu cầu";
        case "soLuongThucNhan" -> "Số lượng thực nhận";
        case "maPNK" -> "Mã phiếu nhập kho";
        case "soLuongNhap" -> "Số lượng nhập";
        case "donGiaNhap" -> "Đơn giá nhập";
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
