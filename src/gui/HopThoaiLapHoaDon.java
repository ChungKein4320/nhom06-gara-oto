package gui;

import connectDB.KetNoiCSDL;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.*;
import java.text.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.*;

// ===== GUI: HỘP THOẠI LẬP HÓA ĐƠN =====

final class HopThoaiLapHoaDon {
    // ===== THÀNH PHẦN GIAO DIỆN =====
    // HỘP THOẠI CHỌN PHIẾU SỬA CHỮA ĐỂ LẬP HÓA ĐƠN
    // ===== PHƯƠNG THỨC: show(Component) — Hiển thị nghiệp vụ tương ứng. =====
    static void show(Component parent) {
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "Mã phiếu sửa chữa", "Khách hàng", "Xe/VIN", "Ngày hoàn thành" }, 0) {
            // ===== PHƯƠNG THỨC: isCellEditable(int, int) — Kiểm tra ô bảng có cho phép chỉnh sửa không. =====
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql = "SELECT s.maPSC, " + "CONCAT(s.maKhachHang, N' — ', k.hoTenKH), "
                + "s.VIN, s.ngayHoanThanh " + "FROM dbo.PhieuSuaChua s " + "JOIN dbo.KhachHang k "
                + "ON k.maKhachHang = s.maKhachHang " + "WHERE s.trangThai = N'Đã sửa xong' " + "AND EXISTS ("
                + "SELECT 1 FROM dbo.ChiTietPhieuSuaChua c " + "WHERE c.maPSC = s.maPSC) "
                + "AND NOT EXISTS (" + "SELECT 1 FROM dbo.ChiTietPhieuSuaChua c " + "WHERE c.maPSC = s.maPSC "
                + "AND (c.trangThaiHangMuc <> N'Hoàn thành' " + "OR c.ketQuaNghiemThu <> N'Đạt')) "
                + "AND NOT EXISTS (" + "SELECT 1 FROM dbo.HoaDon h " + "WHERE h.maPSC = s.maPSC) "
                + "ORDER BY s.maPSC";

        try (Connection connection = KetNoiCSDL.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                model.addRow(new Object[] { resultSet.getString(1), resultSet.getString(2),
                        resultSet.getString(3), resultSet.getTimestamp(4) });
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(parent, exception.getMessage(), "Không tải được phiếu sửa chữa",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(parent,
                    "Không có phiếu đã sửa xong, nghiệm thu Đạt " + "và chưa lập hóa đơn.");
            return;
        }

        JTable repairTable = GiaoDien.table(model);
        repairTable.setAutoCreateRowSorter(true);
        repairTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // BẢNG PHIẾU SỬA CHỮA VÀ Ô CHỌN NHÂN VIÊN
        JPanel formPanel = new JPanel(new BorderLayout(0, 10));
        JScrollPane listScrollPane = new JScrollPane(repairTable);
        listScrollPane.setPreferredSize(new Dimension(830, 310));
        BoCucBang.install(repairTable, listScrollPane);
        formPanel.add(listScrollPane);

        OChonThamChieu employeeChooser = OChonThamChieu.create("HoaDon", "maNhanVien");
        JPanel employeePanel = new JPanel(new BorderLayout(8, 0));
        employeePanel.add(new JLabel("Nhân viên lập hóa đơn"), BorderLayout.WEST);
        employeePanel.add(employeeChooser);
        formPanel.add(employeePanel, BorderLayout.SOUTH);

        Object[] options = { "Xác nhận lập hóa đơn", "Hủy" };
        while (JOptionPane.showOptionDialog(parent, formPanel, "Chọn phiếu sửa chữa đã nghiệm thu",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]) == 0) {
            int selectedRow = repairTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(parent, "Vui lòng chọn phiếu sửa chữa.");
                continue;
            }

            try {
                employeeChooser.validateSelection();
                if (employeeChooser.selectedId().isEmpty()) {
                    throw new SQLException("Vui lòng chọn nhân viên.");
                }

                int modelRow = repairTable.convertRowIndexToModel(selectedRow);
                String invoiceId = dao.HoaDonDAO.createInvoice(String.valueOf(model.getValueAt(modelRow, 0)),
                        employeeChooser.selectedId(), null, null);
                JOptionPane.showMessageDialog(parent, "Đã lập hóa đơn " + invoiceId);
                return;
            } catch (SQLException exception) {
                JOptionPane.showMessageDialog(parent, exception.getMessage(), "Không thể lập hóa đơn",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // TỰ CĂN CHỈNH ĐỘ RỘNG CỘT BẢNG
    private static final class BoCucBang {

        // ===== PHƯƠNG THỨC: install(JTable, JScrollPane) — Đăng ký bộ điều chỉnh độ rộng cột cho bảng. =====
        public static void install(JTable table, JScrollPane scrollPane) {
            if (Boolean.TRUE.equals(table.getClientProperty("tableLayout.installed"))) {
                return;
            }

            table.putClientProperty("tableLayout.installed", Boolean.TRUE);
            scrollPane.getViewport().addComponentListener(new ComponentAdapter() {
                // ===== PHƯƠNG THỨC: componentResized(ComponentEvent) — Cập nhật bố cục khi kích thước thành phần thay đổi. =====
                @Override
                public void componentResized(ComponentEvent event) {
                    SwingUtilities.invokeLater(() -> fit(table, scrollPane));
                }
            });
            table.getModel()
                    .addTableModelListener(event -> SwingUtilities.invokeLater(() -> fit(table, scrollPane)));
            SwingUtilities.invokeLater(() -> fit(table, scrollPane));
        }

        // ===== PHƯƠNG THỨC: fit(JTable, JScrollPane) — Điều chỉnh độ rộng cột theo vùng hiển thị của bảng. =====
        public static void fit(JTable table, JScrollPane scrollPane) {
            int columnCount = table.getColumnCount();
            int availableWidth = scrollPane.getViewport().getWidth();
            if (columnCount == 0 || availableWidth <= 0) {
                return;
            }

            int[] widths = new int[columnCount];
            Object fixedProperty = table.getClientProperty("tableLayout.fixedWidths");
            java.util.Map<?, ?> fixedWidths = fixedProperty instanceof java.util.Map<?, ?> map ? map
                    : java.util.Map.of();
            int totalWidth = 0;

            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                TableColumn column = table.getColumnModel().getColumn(columnIndex);
                int width = Math.max(90, table.getFontMetrics(table.getFont())
                        .stringWidth(String.valueOf(column.getHeaderValue())) + 28);
                int rowLimit = Math.min(table.getRowCount(), 120);

                for (int rowIndex = 0; rowIndex < rowLimit; rowIndex++) {
                    Component rendered = table.prepareRenderer(table.getCellRenderer(rowIndex, columnIndex),
                            rowIndex, columnIndex);
                    width = Math.max(width, rendered.getPreferredSize().width + 8);
                }

                int maximumWidth = DinhDangBang.contentAlignment(table, columnIndex) == SwingConstants.LEFT
                        ? 320
                        : 210;
                widths[columnIndex] = Math.min(width, maximumWidth);

                Object fixedWidth = fixedWidths.get(column.getModelIndex());
                if (fixedWidth instanceof Number number) {
                    widths[columnIndex] = number.intValue();
                }
                totalWidth += widths[columnIndex];
            }

            if (totalWidth < availableWidth) {
                int spareWidth = availableWidth - totalWidth;
                int flexibleColumnCount = 0;
                int lastFlexibleColumn = -1;

                for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                    int modelColumn = table.convertColumnIndexToModel(columnIndex);
                    if (!fixedWidths.containsKey(modelColumn)) {
                        flexibleColumnCount++;
                        lastFlexibleColumn = columnIndex;
                    }
                }

                if (flexibleColumnCount > 0) {
                    int extraPerColumn = spareWidth / flexibleColumnCount;
                    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                        int modelColumn = table.convertColumnIndexToModel(columnIndex);
                        if (!fixedWidths.containsKey(modelColumn)) {
                            widths[columnIndex] += extraPerColumn;
                        }
                    }
                    widths[lastFlexibleColumn] += spareWidth - extraPerColumn * flexibleColumnCount;
                }
            }

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                TableColumn column = table.getColumnModel().getColumn(columnIndex);
                column.setPreferredWidth(widths[columnIndex]);
                column.setWidth(widths[columnIndex]);
            }
            table.revalidate();
        }
    }

    // TẠO VÀ ĐỊNH DẠNG BẢNG
    private static final class GiaoDien {

        private static final Color WHITE = Color.WHITE;
        private static final Color LINE = new Color(0xE4EBE9);
        private static final Color TEAL = new Color(0x487674);
        private static final Color TEXT = new Color(0x21383C);

        // ===== PHƯƠNG THỨC: font(int, boolean) — Tạo phông chữ theo cỡ và kiểu chữ. =====
        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
        }

        // ===== PHƯƠNG THỨC: table(DefaultTableModel) — Lấy bảng dữ liệu tương ứng với màn hình. =====
        public static JTable table(DefaultTableModel model) {
            JTable table = new JTable(model) {
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
                    for (int index = 0; index < getColumnModel().getColumnCount(); index++) {
                        TableColumn column = getColumnModel().getColumn(index);
                        column.setHeaderValue(nhanCot(getModel().getColumnName(column.getModelIndex())));
                    }
                }
            };

            table.setRowHeight(40);
            table.setShowGrid(false);
            table.setIntercellSpacing(new Dimension(0, 0));
            table.setSelectionBackground(new Color(0xE3F1EF));
            table.setSelectionForeground(TEXT);
            table.setForeground(TEXT);
            table.setFont(font(12, false));
            table.setFillsViewportHeight(true);
            table.getTableHeader().setBackground(new Color(0xEDF3F1));
            table.getTableHeader().setForeground(TEAL);
            table.getTableHeader().setPreferredSize(new Dimension(0, 43));
            table.getTableHeader().setReorderingAllowed(false);
            DinhDangBang.header(table);
            table.setDefaultEditor(Object.class, null);
            return table;
        }
    }

    // ĐỊNH DẠNG Ô VÀ TIÊU ĐỀ CỘT
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
            if (name.startsWith("ma ")) {
                return false;
            }
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
                    || name.contains("odo")) {
                return SwingConstants.CENTER;
            }

            if (isMoneyColumn(table, column) || name.startsWith("tong ") || name.contains("tien")
                    || name.contains("gia") || name.contains("so luong") || name.equals("sl")
                    || name.contains("diem") || name.contains("thue") || name.contains("so km")
                    || name.contains("thoi han")) {
                return SwingConstants.RIGHT;
            }
            return SwingConstants.LEFT;
        }

        // ===== PHƯƠNG THỨC: getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int) — Lấy bảng ô renderer thành phần. =====
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focus, row, column);

            String text;
            if (value == null) {
                text = "—";
            } else if (value instanceof java.sql.Date date) {
                text = date.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else if (value instanceof java.sql.Timestamp date) {
                text = date.toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            } else if (value instanceof java.util.Date date) {
                text = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
            } else if (value instanceof Boolean flag) {
                text = name(table, column).contains("gioi tinh") ? (flag ? "Nam" : "Nữ")
                        : (flag ? "Có" : "Không");
            } else if (value instanceof Number number
                    && contentAlignment(table, column) == SwingConstants.RIGHT) {
                text = numbers.format(number);
            } else {
                text = value.toString();
            }

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
            header.setDefaultRenderer((tableComponent, value, selected, focus, row, column) -> {
                Component component = original.getTableCellRendererComponent(tableComponent, value, selected,
                        focus, row, column);

                if (component instanceof JLabel label) {
                    label.setHorizontalAlignment(alignment(tableComponent, column));
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

    // ÁNH XẠ TÊN TRƯỜNG SANG NHÃN TIẾNG VIỆT
    // ===== PHƯƠNG THỨC: nhanCot(String) — Tạo tiêu đề hiển thị cho cột bảng. =====
    private static String nhanCot(String field) {
        return switch (field) {
        case "khachHangHienThi" -> "Khách hàng";
        case "tongThanhToan" -> "Tổng thanh toán";
        case "tongDiemTichLuy" -> "Tổng điểm tích lũy";
        case "gioiTinh" -> "Giới tính";
        case "soKM" -> "Số km";
        case "ketQuaChuanDoan" -> "Kết quả chẩn đoán";
        case "maNhanVien", "maNV" -> "Mã nhân viên";
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
