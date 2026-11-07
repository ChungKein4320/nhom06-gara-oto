package gui;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

// ===== GUI: BỘ LỌC TRẠNG THÁI DANH SÁCH =====

final class BoLocTrangThai extends JPanel {
    // ===== THÀNH PHẦN GIAO DIỆN =====
    // ===== Chỉ hiển thị phiếu và hóa đơn trong 30 ngày lịch gần nhất, tính cả hôm nay =====
    static boolean trongBaMuoiNgay(String tableName, Map<String, Object> row) {
        String dateField = switch (tableName) {
            case "HoaDon", "PhieuBaoGia", "PhieuSuaChua" -> "ngayLap";
            case "PhieuTiepNhan" -> "ngayTiepNhan";
            case "PhieuKiemTra" -> "ngayKiemTra";
            case "PhieuNhapKho" -> "ngayNhap";
            case "YeuCauNhapKho" -> "ngayTao";
            default -> null;
        };
        if (dateField == null) return true;
        Object raw = row.get(dateField);
        if (raw == null) return false;
        java.time.LocalDate date;
        if (raw instanceof java.sql.Timestamp timestamp) date = timestamp.toLocalDateTime().toLocalDate();
        else if (raw instanceof java.sql.Date sqlDate) date = sqlDate.toLocalDate();
        else if (raw instanceof java.time.LocalDateTime dateTime) date = dateTime.toLocalDate();
        else if (raw instanceof java.time.LocalDate localDate) date = localDate;
        else date = java.time.LocalDate.parse(raw.toString().substring(0, 10));
        java.time.LocalDate today = java.time.LocalDate.now();
        return !date.isBefore(today.minusDays(29)) && !date.isAfter(today);
    }
    private static final long serialVersionUID = 1L;
    private final JTable table;
    private final Map<Integer, JComboBox<String>> filters = new LinkedHashMap<>();
    private final Map<Integer, String> defaultStates = new HashMap<>();
    private final Map<Integer, List<String>> predefinedOptions = new LinkedHashMap<>();
    private String search = "";
    private boolean rebuilding, queued;
    private Runnable changed = () -> {
    };

    // ===== KHỞI TẠO ĐỐI TƯỢNG: BoLocTrangThai(JTable) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    BoLocTrangThai(JTable table) {
        super(new FlowLayout(FlowLayout.LEFT, 8, 3));
        this.table = table;
        setOpaque(false);
        for (int i = 0; i < table.getModel().getColumnCount(); i++) {
            // Đọc tên cột của bảng
            String name = table.getModel().getColumnName(i);
            // Chỉ tạo combobox cho cột Trạng thái
            if (!OChonTimKiem.normalize(nhanCot(name)).contains("trang thai"))
                continue;
            JComboBox<String> cmbStatus = new OChonTimKiem<>();
            cmbStatus.setEditable(false);
            cmbStatus.setPreferredSize(new Dimension(190, 30));
            // Tạo nhãn trạng thái
            add(GiaoDien.label(nhanCot(name), 12, true, GiaoDien.NAV));
            // Tạo ô chọn
            add(cmbStatus);
            filters.put(i, cmbStatus);
            cmbStatus.addActionListener(e -> {
                if (!rebuilding)
                    apply();
            });
        }
        setVisible(!filters.isEmpty());
        table.getModel().addTableModelListener(e -> {
            if (rebuilding)
                return;
            if (!queued) {
                queued = true;
                SwingUtilities.invokeLater(() -> {
                    queued = false;
                    if (!rebuilding)
                        refresh();
                });
            }
        });
        refresh();
    }

    // ===== PHƯƠNG THỨC: onChange(Runnable) — Đăng ký hành động chạy khi dữ liệu thay đổi. =====
    void onChange(Runnable changed) {
        this.changed = changed;
    }

    // Tìm kiếm tương ứng.
    void search(String value) {
        search = OChonTimKiem.normalize(value.trim());
        apply();
    }

    // ===== PHƯƠNG THỨC: reset() — Đặt lại các điều kiện lọc. =====
    void reset() {
        rebuilding = true;
        try {
            search = "";
            for (JComboBox<String> combo : filters.values()) {
                combo.setSelectedItem("Tất cả");
            }
        } finally {
            rebuilding = false;
        }
        if (table.getRowSorter() != null)
            table.getRowSorter().setSortKeys(null);
        table.clearSelection();
        apply();
    }

    // ===== PHƯƠNG THỨC: setPredefinedOptions(int, List<String>) — Gán predefined options. =====
    void setPredefinedOptions(int column, List<String> options) {
        if (options != null && !options.isEmpty()) {
            predefinedOptions.put(column, new ArrayList<>(options));
        } else {
            predefinedOptions.remove(column);
        }
        refresh();
    }

    // ===== PHƯƠNG THỨC: selectDefault(int, String) — Chọn mặc định. =====
    void selectDefault(int column, String state) {
        JComboBox<String> combo = filters.get(column);
        if (combo == null)
            return;

        defaultStates.put(column, state);
        combo.setSelectedItem(state);
        apply();
    }

    // ===== PHƯƠNG THỨC: getFilter(int) — Lấy bộ lọc. =====
    JComboBox<String> getFilter(int column) {
        return filters.get(column);
    }

    // ===== PHƯƠNG THỨC: refresh() — Tải lại dữ liệu và làm mới giao diện. =====
    void refresh() {
        rebuilding = true;
        try {
            for (var entry : filters.entrySet()) {
                int column = entry.getKey();
                JComboBox<String> combo = entry.getValue();
                Object selected = combo.getSelectedItem();

                List<String> fixed = predefinedOptions.get(column);
                Collection<String> values;
                if (fixed != null && !fixed.isEmpty()) {
                    values = new LinkedHashSet<>(fixed);
                } else {
                    values = new TreeSet<>();
                    for (int row = 0; row < table.getModel().getRowCount(); row++) {
                        Object value = table.getModel().getValueAt(row, column);
                        if (value != null && !value.toString().isBlank()) {
                            values.add(value.toString());
                        }
                    }

                    String defaultState = defaultStates.get(column);
                    if (defaultState != null) {
                        values.add(defaultState);
                    }

                    if (selected != null && !"Tất cả".equals(selected)) {
                        values.add(selected.toString());
                    }
                }

                String targetSelection;
                if (selected != null && values.contains(selected.toString())) {
                    targetSelection = selected.toString();
                } else if (selected != null && "Tất cả".equals(selected)
                        && !defaultStates.containsKey(column)) {
                    targetSelection = "Tất cả";
                } else if (defaultStates.containsKey(column)) {
                    targetSelection = defaultStates.get(column);
                } else {
                    targetSelection = "Tất cả";
                }

                List<String> items = new ArrayList<>();
                items.add("Tất cả");
                items.addAll(values);

                boolean modelMatches = false;
                if (combo.getItemCount() == items.size()) {
                    modelMatches = true;
                    for (int i = 0; i < items.size(); i++) {
                        if (!Objects.equals(combo.getItemAt(i), items.get(i))) {
                            modelMatches = false;
                            break;
                        }
                    }
                }

                if (!modelMatches) {
                    combo.setModel(new DefaultComboBoxModel<>(items.toArray(String[]::new)));
                }
                if (!Objects.equals(combo.getSelectedItem(), targetSelection)) {
                    combo.setSelectedItem(targetSelection);
                }
            }
        } finally {
            rebuilding = false;
        }
        apply();
    }

    // Áp dụng các tùy chọn đã chọn.
    @SuppressWarnings("unchecked")
    private void apply() {
        try {
            TableRowSorter<TableModel> sorter;
            if (table.getRowSorter() instanceof TableRowSorter<?> current) {
                sorter = (TableRowSorter<TableModel>) current;
            } else {
                sorter = new TableRowSorter<>(table.getModel());
                table.setRowSorter(sorter);
            }
            sorter.setRowFilter(new RowFilter<>() {
                // Quyết định có đưa bản ghi vào kết quả lọc.
                @Override
                public boolean include(Entry<? extends TableModel, ? extends Integer> row) {
                    //Có trạng thái thì lọc theo trạng thái
                    for (var filter : filters.entrySet()) {
                        Object state = filter.getValue().getSelectedItem();
                        if (state != null && !state.equals("Tất cả")
                                && !state.toString().equals(row.getStringValue(filter.getKey()))) {
                            return false;
                        }
                    }
                    // Không nhập thì hiển thị tất cả
                    if (search.isEmpty())
                        return true;
                    for (int col = 0; col < row.getValueCount(); col++) {
                        // Duyệt số cột của dòng, lấy dữ liệu kiểm tra có từ khóa
                        if (OChonTimKiem.normalize(row.getStringValue(col)).contains(search)) {
                            return true; // giữ dòng
                        }
                    }
                    return false; //ẩn dòng
                }
            });
        } catch (Exception ignored) {
        }
        changed.run();
    }

    private static final class GiaoDien {

        private static final Color NAV = new Color(0x13272E);

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
}
