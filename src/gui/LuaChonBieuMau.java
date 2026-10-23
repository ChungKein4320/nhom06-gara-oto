package gui;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JComboBox;

/** Miền giá trị riêng cho từng bảng theo đặc tả nghiệp vụ đã thống nhất. */
public final class LuaChonBieuMau {
    private static final Map<String, String[]> OPTIONS = new LinkedHashMap<>();
    static {
        put("NhanVien", "vaiTro", "Nhân viên kỹ thuật", "Nhân viên bán hàng", "Nhân viên kho", "Nhân viên quản lý");
        put("NhanVien", "trangThai", "Đang làm", "Đã nghỉ");
        put("TaiKhoan", "trangThai", "Đang hoạt động", "Ngừng hoạt động");
        put("PhieuBaoGia", "trangThai", "Còn hiệu lực");
        put("ChiTietPhieuBaoGia", "trangThai", "Chờ xác nhận", "Chờ sửa chữa", "Thiếu phụ tùng + Chờ xác nhận");
        put("PhieuSuaChua", "trangThai", "Chờ sửa chữa");
        put("ChiTietPhieuSuaChua", "trangThaiHangMuc", "Đang sửa chữa", "Thiếu phụ tùng", "Hoàn thành");
        put("PhieuNhapKho", "trangThai", "Chờ duyệt");
        put("DichVu", "trangThai", "Hoạt động", "Ngừng hoạt động");
        put("MaGiamGia", "trangThai", "Đang hiệu lực", "Hết hạn", "Đã sử dụng");
        put("LichNhacBaoDuong", "trangThai", "Chưa gửi", "Đã gửi");
        put("ChiTietPhieuKiemTra", "ketQuaKiemTra", "", "Đạt", "Không đạt");
        put("ChiTietPhieuSuaChua", "ketQuaNghiemThu", "Không đạt", "Đạt");
        put("HoaDon", "trangThaiThanhToan", "Chưa thanh toán");
    }

    private LuaChonBieuMau() { }

    private static void put(String table, String field, String... values) {
        OPTIONS.put(table + "." + field, values);
    }

    public static JComboBox<String> create(String table, String field, String current, boolean existing) {
        String[] options = OPTIONS.get(table + "." + field);
        if (options == null) return null;
        JComboBox<String> cmbCombo = new OChonTimKiem<>(options.clone());
        cmbCombo.setFont(GiaoDien.font(12, false));
        cmbCombo.setEditable(true);
        if (existing) selectExisting(cmbCombo, current);
        // Thanh toán phải được xác nhận qua nghiệp vụ riêng, không đổi ở biểu mẫu.
        if (("HoaDon".equals(table) && "trangThaiThanhToan".equals(field))
                || (field.equals("trangThai") && java.util.Set.of("PhieuBaoGia", "PhieuSuaChua", "PhieuNhapKho").contains(table))) {
            cmbCombo.setEnabled(false);
        }
        return cmbCombo;
    }

    public static void selectExisting(JComboBox<String> cmbCombo, String value) {
        if (value == null || value.isEmpty()) {
            cmbCombo.setSelectedIndex(-1);
            return;
        }
        boolean found = false;
        for (int i = 0; i < cmbCombo.getItemCount(); i++) {
            if (value.equals(cmbCombo.getItemAt(i))) { found = true; break; }
        }
        // Chỉ thêm giá trị cũ cho bản ghi đang sửa, không đưa vào danh sách tạo mới.
        if (!found) cmbCombo.addItem(value);
        cmbCombo.setSelectedItem(value);
    }
}
