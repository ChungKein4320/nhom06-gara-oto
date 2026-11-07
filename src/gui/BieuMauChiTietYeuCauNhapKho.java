package gui;

import javax.swing.border.*;

import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import javax.swing.*;

/**
 * ============================================================================
 * GUI: BieuMauChiTietYeuCauNhapKho (Biểu mẫu một dòng chi tiết Yêu cầu nhập kho)
 * ============================================================================
 * 1. Mục đích:
 *    - Tạo panel chứa các ô nhập liệu của một dòng phụ tùng đề xuất mua trong
 *      Yêu cầu nhập kho (maPhuTung, soLuongYeuCau, donGiaDuKien).
 * 2. Phục vụ màn hình / nghiệp vụ:
 *    - Nghiệp vụ 2 (Lập yêu cầu nhập kho): Được mở từ TrinhSuaYeuCauNhapKho khi
 *      Nhân viên kho bấm "Thêm hạng mục", "Sửa hạng mục" hoặc nhấp đúp vào một
 *      dòng trong bảng chi tiết.
 * 3. Phương thức quan trọng:
 *    - read() -> kiemTraDuLieu(Map): Đọc dữ liệu từ các ô nhập, kiểm tra
 *      soLuongYeuCau > 0, donGiaDuKien >= 0, mã phụ tùng dạng PTxxx (khác PT000).
 *    - docVaKiemTraGiaTriCot(...): Chuyển đổi chuỗi sang int/decimal và kiểm tra hợp lệ.
 * ============================================================================
 */
final class BieuMauChiTietYeuCauNhapKho extends JPanel {
    // ===== CẤU HÌNH CÁC CỘT TRÊN BIỂU MẪU =====
    private static final String[] CAC_COT = new String[] { "maYCNK", "maPhuTung", "soLuongYeuCau",
            "donGiaDuKien" };
    private static final Map<String, String> KIEU_COT = Map.ofEntries(Map.entry("maYCNK", "varchar"),
            Map.entry("maPhuTung", "varchar"), Map.entry("soLuongYeuCau", "int"),
            Map.entry("donGiaDuKien", "decimal"));
    private static final Set<String> COT_BAT_BUOC = Set.of("maYCNK", "maPhuTung", "soLuongYeuCau",
            "donGiaDuKien");
    private static final String[] CAC_KHOA = new String[] { "maYCNK", "maPhuTung" };

    private static final long serialVersionUID = 1L;
    private final Map<String, JComponent> inputs = new LinkedHashMap<>();

    // ===== NHÓM 1: KHỞI TẠO GIAO DIỆN BIỂU MẪU CHI TIẾT =====

    BieuMauChiTietYeuCauNhapKho(String table, Map<String, Object> row, Set<String> excluded) {
        this(table, row, excluded, false);
    }

    BieuMauChiTietYeuCauNhapKho(String table, Map<String, Object> row, Set<String> excluded,
            boolean readOnly) {
        this(table, row, excluded, readOnly, isExisting(table, row));
    }

    BieuMauChiTietYeuCauNhapKho(String table, Map<String, Object> row, Set<String> excluded, boolean readOnly,
            boolean existing) {
        super(new GridBagLayout());
        table = "ChiTietYeuCauNhapKho";
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
                if (!existing && Set.of("soLuongYeuCau").contains(field))
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
            String fieldLabel = layTenHienThiCuaCot(field);
            String label = fieldLabel + (COT_BAT_BUOC.contains(field) ? " *" : "");

            pnlCell.add(GiaoDien.label(label, 12, true, GiaoDien.TEXT), BorderLayout.NORTH);
            input.setPreferredSize(new Dimension(260, 38));
            pnlCell.add(input, BorderLayout.CENTER);
            add(pnlCell, c);
            index++;
        }
    }

    // Kiểm tra dòng dữ liệu truyền vào là dòng đã tồn tại hay dòng tạo mới.
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

    // ===== NHÓM 2: ĐỌC DỮ LIỆU, KHÓA TRƯỜNG & KIỂM TRA HỢP LỆ =====

    // Đọc và kiểm tra toàn bộ dữ liệu trên biểu mẫu chi tiết yêu cầu nhập kho.
    Map<String, Object> read() throws SQLException {
        return kiemTraDuLieu(inputs);
    }

    // Khóa một trường trên biểu mẫu không cho người dùng sửa.
    void lockField(String field) {
        if (inputs.get(field) != null)
            DinhDangKeThua.lock(inputs.get(field));
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (getParent() instanceof JViewport vp && vp.getWidth() > 0)
            return new Dimension(vp.getWidth(), d.height);
        return d;
    }

    // Kiểm tra các trường của dòng chi tiết yêu cầu nhập kho (soLuongYeuCau > 0, donGiaDuKien >= 0, cấm PT000).
    private static Map<String, Object> kiemTraDuLieu(Map<String, JComponent> inputs)
            throws java.sql.SQLException {
        Map<String, Object> values = new LinkedHashMap<>();
        docVaKiemTraGiaTriCot(inputs, values, "maPhuTung", "Mã phụ tùng", "varchar:5", true);
        docVaKiemTraGiaTriCot(inputs, values, "soLuongYeuCau", "Số lượng yêu cầu", "int:", true);
        docVaKiemTraGiaTriCot(inputs, values, "donGiaDuKien", "Đơn giá dự kiến", "decimal:18,2", true);
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

        if ("PT000".equals(String.valueOf(values.get("maPhuTung")))) {

            throw new ThongBaoLoiSQL.Issue("maPhuTung",
                    "PT000 chỉ dùng để biểu thị hạng mục không sử dụng phụ tùng; không được nhập kho hoặc bán như phụ tùng.");

        }
        return values;
    }

    // Đọc giá trị văn bản từ một ô nhập, kiểm tra bắt buộc và chuyển đổi sang kiểu dữ liệu tương ứng.
    private static void docVaKiemTraGiaTriCot(Map<String, JComponent> inputs, Map<String, Object> values,
            String field, String label, String specification, boolean required) throws ThongBaoLoiSQL.Issue {
        if (!inputs.containsKey(field)) return;
        if (inputs.get(field) instanceof OChonThamChieu cmbCombo) cmbCombo.validateSelection();
        if(inputs.get(field) instanceof JCheckBox checkBox) {
            values.put(field,checkBox.isSelected());
            return;
        }
        String raw = layVanBanTuONhap(inputs.get(field));
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

    // ===== NHÓM 3: TIỆN ÍCH GIAO DIỆN & ĐỌC VĂN BẢN TỪ COMPONENT =====

    private static final class GiaoDien {

        private static final Color WHITE = Color.WHITE;
        private static final Color TEXT = new Color(0x21383C);
        private static final Color LINE = new Color(0xE4EBE9);

        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
        }

        public static JLabel label(String s, int size, boolean bold, Color c) {
            JLabel lblL = new JLabel(s);
            lblL.setFont(font(size, bold));
            lblL.setForeground(c);
            return lblL;
        }

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

    // Lấy tên nhãn tiếng Việt hiển thị cho từng cột dữ liệu.
    private static String layTenHienThiCuaCot(String field) {
        return switch (field) {
        case "maPhuTung" -> "Mã phụ tùng";
        case "maYCNK" -> "Mã yêu cầu nhập kho";
        case "soLuongYeuCau" -> "Số lượng yêu cầu";
        case "donGiaDuKien" -> "Đơn giá dự kiến";
        default -> field;
        };
    }

    // Trích xuất chuỗi văn bản người dùng đã nhập hoặc chọn từ một JComponent.
    private static String layVanBanTuONhap(javax.swing.JComponent control) {
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
