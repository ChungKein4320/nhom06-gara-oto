package gui;

import dao.Schema;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;

/** Kiểm tra biểu mẫu và xác định ô cần sửa khi lưu thất bại. */
final class KiemTraBieuMau {
    static final class Issue extends SQLException {
        @java.io.Serial private static final long serialVersionUID = 1L;
        final String field;
        Issue(String field, String message) { super(message); this.field = field; }
    }
    private KiemTraBieuMau() { }

    static String userMessage(Throwable error,String table) {
        String message=error==null || error.getMessage()==null ? "Dữ liệu chưa hợp lệ." : error.getMessage();
        if(error instanceof Issue || error instanceof dao.DataRules.Invalid) return message;
        if(message.toUpperCase(Locale.ROOT).contains("CK_CHITIETPHIEUKIEMTRA_KETQUA"))
            return "Kết quả kiểm tra không hợp lệ. Hãy chọn “Đạt”, “Không đạt” hoặc để trống nếu chưa có kết quả.";
        if(table.equals("PhieuTiepNhan") && message.toUpperCase(Locale.ROOT).contains("CK_PHIEUTIEPNHAN_NGAY"))
            return "Ngày tiếp nhận không hợp lệ. Vui lòng chọn ngày, giờ hiện tại hoặc trước đó.";
        if(table.equals("PhieuTiepNhan") && message.toLowerCase(Locale.ROOT).matches("(?s).*incorrect syntax near (the )?keyword ['\"]?as['\"]?.*"))
            return "Không thể lưu phiếu do lỗi xử lý dữ liệu. Đây là lỗi của ứng dụng, không phải thông tin bạn nhập; vui lòng cập nhật ứng dụng rồi thử lại.";
        if(table.equals("PhieuTiepNhan") && message.toLowerCase(Locale.ROOT).matches("(?s).*incorrect syntax near.*"))
            return "Không thể lưu phiếu do lỗi xử lý dữ liệu. Đây là lỗi của ứng dụng, không phải thông tin bạn nhập; vui lòng cập nhật ứng dụng rồi thử lại.";
        if(error instanceof SQLException sqlError) {
            int code=sqlError.getErrorCode();
            String state=sqlError.getSQLState();
            if(code==547 && message.toUpperCase(Locale.ROOT).contains("CK_"))
                return "Thông tin phiếu không hợp lệ. Một giá trị chưa đáp ứng quy định; vui lòng kiểm tra lại các trường và hạng mục.";
            if(code==547 && message.toUpperCase(Locale.ROOT).contains("FK_"))
                return "Không thể lưu phiếu vì một thông tin liên quan không còn tồn tại. Hãy tải lại danh sách và chọn lại dữ liệu liên quan.";
            if(code==2601 || code==2627)
                return "Mã hoặc thông tin này đã tồn tại. Hãy kiểm tra lại dữ liệu rồi lưu lại.";
            if(code==515)
                return "Còn thiếu thông tin bắt buộc. Hãy điền đầy đủ các trường có dấu * rồi lưu lại.";
            if(code==2628 || code==8152)
                return "Một nội dung nhập vượt quá độ dài cho phép. Hãy rút ngắn nội dung rồi lưu lại.";
            if(code==245 || code==8114 || code==8115)
                return "Một giá trị số không đúng định dạng hoặc vượt giới hạn. Hãy kiểm tra lại các ô số.";
            if(code==1205)
                return "Phiếu chưa được lưu vì hệ thống đang xử lý một giao dịch khác. Vui lòng thử lưu lại.";
            if((state!=null && state.startsWith("08")) || code==53 || code==2)
                return "Không thể kết nối cơ sở dữ liệu nên phiếu chưa được lưu. Hãy kiểm tra kết nối rồi thử lại.";
            if(code==102 || code==156)
                return "Không thể lưu phiếu do lỗi xử lý dữ liệu trong ứng dụng. Vui lòng cập nhật ứng dụng hoặc liên hệ quản trị viên.";
            if(code!=0 || state!=null) {
                System.err.println("Lỗi cơ sở dữ liệu khi lưu phiếu. SQLState="+state+", mã lỗi="+code);
                return "Không thể lưu phiếu do lỗi cơ sở dữ liệu. Phiếu chưa được ghi; vui lòng thử lại hoặc liên hệ quản trị viên (mã lỗi "+code+").";
            }
        }
        return message;
    }

    static Map<String, Object> read(String table, Map<String, JComponent> inputs, boolean existing) throws Issue {
        Map<String, Object> values = new LinkedHashMap<>();
        String primary = Schema.PRIMARY.get(table);
        for (String field : Schema.COLUMNS.get(table)) {
            if (!inputs.containsKey(field)) continue;
            if (field.equals(primary) && (existing || !field.equals("VIN"))) continue;
            if (inputs.get(field) instanceof OChonThamChieu cmbCombo) cmbCombo.validateSelection();
            if(inputs.get(field) instanceof JCheckBox checkBox) {
                values.put(field,checkBox.isSelected());
                continue;
            }
            String raw = text(inputs.get(field));
            String label = NhanCot.forField(field);
            if (raw.isEmpty()) {
                if (Schema.REQUIRED.get(table).contains(field)) throw new Issue(field, "Vui lòng nhập " + label.toLowerCase() + ".");
                values.put(field, null);
                continue;
            }
            String[] spec = Schema.TYPES.get(table).get(field).split(":", 2);
            Object value;
            try {
                value = switch (spec[0]) {
                    case "int" -> Integer.valueOf(raw);
                    case "decimal" -> new BigDecimal(raw);
                    case "datetime", "datetime2" -> Timestamp.valueOf(raw.length() == 10 ? raw + " 00:00:00" : raw);
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
                throw new Issue(field, label + " phải là " + expected + ".");
            }
            if ((spec[0].equals("varchar") || spec[0].equals("nvarchar"))
                    && raw.length() > Integer.parseInt(spec[1].trim())) {
                throw new Issue(field, label + " không được vượt quá " + spec[1].trim() + " ký tự.");
            }
            if (value instanceof Number number) {
                BigDecimal numberValue = new BigDecimal(number.toString());
                if (numberValue.signum() < 0) throw new Issue(field, label + " không được âm.");
                if (spec[0].equals("decimal")) {
                    String[] size = spec[1].split(",");
                    int precision = Integer.parseInt(size[0].trim());
                    int scale = Integer.parseInt(size[1].trim());
                    BigDecimal normalized = numberValue.stripTrailingZeros();
                    if (Math.max(0, normalized.precision() - normalized.scale()) > precision - scale
                            || Math.max(0, normalized.scale()) > scale) {
                        throw new Issue(field, label + " chỉ nhận tối đa " + (precision - scale)
                                + " chữ số phần nguyên và " + scale + " chữ số thập phân.");
                    }
                }
            }
            if (field.equals("soDienThoai") && !raw.matches("0[35789][0-9]{8}")) throw new Issue(field, "Số điện thoại phải gồm 10 chữ số, đầu số 03, 05, 07, 08 hoặc 09.");
            if (field.equals("email") && !raw.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) throw new Issue(field, "Email không đúng định dạng.");
            if (field.equals("VIN") && !raw.matches("[A-HJ-NPR-Z0-9]{17}")) throw new Issue(field, "Số khung (VIN) phải gồm 17 ký tự hợp lệ.");
            if (field.equals("ketQuaKiemTra") && !raw.isEmpty() && !raw.equals("Đạt") && !raw.equals("Không đạt"))
                throw new Issue(field, "Kết quả kiểm tra không hợp lệ. Hãy chọn “Đạt”, “Không đạt” hoặc để trống nếu chưa có kết quả.");
            values.put(field, value);
        }
        checkDates(values, "ngayTao", "ngayHetHan");
        checkDates(values, "ngayLap", "ngayHetHan");
        checkDates(values, "ngayLap", "ngayHoanThanh");
        try { dao.DataRules.validate(table, values, false); }
        catch (dao.DataRules.Invalid ex) { throw new Issue(ex.field, ex.getMessage()); }
        catch (SQLException ex) { throw new Issue(null, ex.getMessage()); }
        return values;
    }

    private static void checkDates(Map<String, Object> values, String start, String end) throws Issue {
        if (values.get(start) instanceof java.util.Date from && values.get(end) instanceof java.util.Date to && to.before(from)) {
            throw new Issue(end, NhanCot.forField(end) + " không được trước " + NhanCot.forField(start).toLowerCase() + ".");
        }
    }

    static String text(JComponent control) {
        if (control instanceof OChonNgay pnlPicker) return pnlPicker.getDateText();
        if (control instanceof OChonThamChieu cmbCombo) return cmbCombo.selectedId();
        if (control instanceof JComboBox<?> cmbCombo) return cmbCombo.getSelectedItem() == null ? "" : cmbCombo.getSelectedItem().toString().trim();
        if (control instanceof JTextField txtField) return txtField.getText().trim();
        if (control instanceof JPanel pnlPanel) {
            for(java.awt.Component component : pnlPanel.getComponents())
                if(component instanceof JRadioButton radRadio && radRadio.isSelected()) return radRadio.getText();
        }
        return "";
    }

    static String errorField(Throwable error, String table) {
        if (error instanceof Issue issue) return issue.field;
        if (error instanceof dao.DataRules.Invalid issue) return issue.field;
        String message = error.getMessage() == null ? "" : error.getMessage();
        Matcher constraint = Pattern.compile("\\b(FK|UQ|CK|PK)_([A-Za-z0-9_]+)").matcher(message);
        if (constraint.find()) {
            String kind = constraint.group(1);
            String name = constraint.group(2);
            if (kind.equals("PK")) return Schema.PRIMARY.get(table);
            String suffix = name.substring(name.lastIndexOf('_') + 1);
            if (kind.equals("FK")) {
                String target = switch (suffix) {
                    case "PSC" -> "PhieuSuaChua";
                    default -> suffix;
                };
                String primary = Schema.PRIMARY.get(target);
                if (primary != null && Schema.TYPES.get(table).containsKey(primary)) return primary;
            }
            if (suffix.equalsIgnoreCase("SDT")) return "soDienThoai";
            if (suffix.equalsIgnoreCase("Ten") && table.equals("PhuongThucThanhToan")) return "tenPhuongThuc";
            if (suffix.equalsIgnoreCase("SoLuong") && table.equals("TonKho")) return "soLuongHienTai";
            if (suffix.equalsIgnoreCase("Ngay")) return switch (table) {
                case "PhieuSuaChua" -> "ngayHoanThanh";
                case "PhieuTiepNhan" -> "ngayTiepNhan";
                default -> "ngayHetHan";
            };
            for (String field : Schema.COLUMNS.get(table)) {
                if (field.equalsIgnoreCase(suffix) || field.equalsIgnoreCase("ten" + suffix) || field.equalsIgnoreCase("ma" + suffix)) return field;
            }
        }
        for (String field : Schema.COLUMNS.get(table)) {
            if (Pattern.compile("(?i)(?<![A-Za-z0-9_])" + Pattern.quote(field) + "(?![A-Za-z0-9_])").matcher(message).find()) return field;
        }
        if (message.contains("Số điện thoại")) return "soDienThoai";
        if (message.contains("Email")) return "email";
        if (message.contains("VIN")) return "VIN";
        if (message.startsWith("Mã ") && message.contains("đã thay đổi")) return Schema.PRIMARY.get(table);
        return null;
    }
}
