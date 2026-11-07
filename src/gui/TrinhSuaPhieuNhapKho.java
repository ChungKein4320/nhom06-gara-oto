package gui;

import java.util.Locale;
import java.time.format.DateTimeFormatter;
import java.text.*;
import javax.swing.table.TableColumn;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.Component;

import connectDB.KetNoiCSDL;
import dao.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Hộp thoại GUI: Lập mới (từ YCNK đã duyệt), chỉnh sửa và xem chi tiết Phiếu nhập kho (TrinhSuaPhieuNhapKho).
 * - Gồm 2 phần trên cùng một cửa sổ:
 *   1. Đầu phiếu (BieuMauPhieuNhapKho): mã PNK, mã YCNK, mã NCC, mã nhân viên, ngày nhập, trạng thái (khóa các trường kế thừa từ YCNK).
 *   2. Bảng chi tiết (ChiTietPhieuNhapKho): khóa danh sách phụ tùng 1-1 theo YCNK; cho phép nhấp đúp sửa trực tiếp 3 cột
 *      'soLuongThucNhan', 'soLuongNhap', 'donGiaNhap' thông qua BoChinhSuaOHangMucPNK.
 * - Khi nhấn 'Lưu toàn bộ phiếu' / 'Lưu thay đổi': kiểm tra 0 <= soLuongNhap <= soLuongThucNhan <= soLuongYeuCau
 *   và gọi QuyTrinhDAO.saveDocument("PhieuNhapKho", ...).
 */
final class TrinhSuaPhieuNhapKho {
    // ===== KHỞI TẠO ĐỐI TƯỢNG: TrinhSuaPhieuNhapKho() — Lớp tiện ích tĩnh, không khởi tạo trực tiếp. =====
    private TrinhSuaPhieuNhapKho() {
    }

    // ===== PHƯƠNG THỨC: show(Component, String, Map<String,Object>, VaiTro, String) — Mở hộp thoại chỉnh sửa phiếu nhập kho. =====
    static void show(Component parent, String table, Map<String, Object> header, VaiTro role,
            String employeeId) {
        show(parent, table, header, List.of(), role, employeeId);
    }

    // ===== PHƯƠNG THỨC: show(Component, String, Map<String,Object>, List<Map<String,Object>>, VaiTro, String) — Mở hộp thoại lập phiếu nhập kho từ YCNK đã duyệt kèm danh sách hạng mục. =====
    static void show(Component parent, String table, Map<String, Object> header,
            List<Map<String, Object>> initialDetails, VaiTro role, String employeeId) {
        moHopThoaiChinhSuaHoacXem(parent, table, header, initialDetails, false, role, employeeId);
    }

    // ===== PHƯƠNG THỨC: view(Component, String, Map<String,Object>) — Mở hộp thoại xem chi tiết phiếu nhập kho (chỉ đọc). =====
    static void view(Component parent, String table, Map<String, Object> header) {
        moHopThoaiChinhSuaHoacXem(parent, table, header, List.of(), true, null, null);
    }

    // ===== PHƯƠNG THỨC: review(Component, String, Map<String,Object>, VaiTro, String) — Điểm vào xem xét chứng từ (giữ tương thích cấu trúc). =====
    static void review(Component parent, String table, Map<String, Object> header, VaiTro role,
            String employeeId) {
        moHopThoaiXetDuyet(parent, table, header, role, employeeId);
    }

    // ===== PHƯƠNG THỨC: layCuaSoCha(Component) — Tìm cửa sổ Window cha để gắn JDialog modal. =====
    private static Window layCuaSoCha(Component parent) {
        if (parent == null)
            return null;
        if (parent instanceof Window window)
            return window;
        return SwingUtilities.getWindowAncestor(parent);
    }

    // ===== PHƯƠNG THỨC: moHopThoaiXetDuyet(...) — Hộp thoại xét duyệt giữ lại để đồng bộ cấu trúc với lớp tách từ TrinhSuaPhieu. =====
    private static void moHopThoaiXetDuyet(Component parent, String table, Map<String, Object> header, VaiTro role,
            String employeeId) {
        String pk = "maPNK";
        String id = header == null || header.get(pk) == null ? null : String.valueOf(header.get(pk));
        if (id == null) {
            JOptionPane.showMessageDialog(parent, "Không tìm thấy mã phiếu cần xét duyệt.", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Map<String, Object>> details = new ArrayList<>();
        Map<String, Object> freshHeader = header;
        try (Connection conn = KetNoiCSDL.getConnection()) {
            var fresh = new dao.PhieuNhapKhoDAO().getAll(conn, Map.of(pk, id));
            if (fresh.isEmpty())
                throw new SQLException("Phiếu không còn tồn tại. Vui lòng tải lại danh sách.");
            freshHeader = fresh.get(0);
            details.addAll(new dao.ChiTietPhieuNhapKhoDAO().getAll(conn, Map.of(pk, id)));
        } catch (SQLException ex) {
            hienThiLoiLuuPhieu(parent, ex);
            return;
        }

        String status = String.valueOf(freshHeader.get("trangThai"));
        boolean isPending = "Chờ duyệt".equals(status);
        boolean canDecide = isPending && (role == VaiTro.QUAN_LY || role == VaiTro.ADMIN);

        BieuMauPhieuNhapKho pnlHeader = new BieuMauPhieuNhapKho(table, freshHeader, Set.of(), true, true);

        String[] columns = new String[] { "maPhuTung", "soLuongYeuCau", "soLuongThucNhan", "soLuongNhap",
                "donGiaNhap" };
        DefaultTableModel model = new DefaultTableModel(new String[] { "Mã phụ tùng", "Số lượng yêu cầu",
                "Số lượng thực nhận", "Số lượng nhập", "Đơn giá nhập" }, 0) {
            @java.io.Serial
            private static final long serialVersionUID = 1L;

            // ===== PHƯƠNG THỨC: isCellEditable(int, int) — Kiểm tra ô bảng có cho phép chỉnh sửa không. =====
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable tblDetails = GiaoDien.table(model);
        tblDetails.setAutoCreateRowSorter(true);
        for (Map<String, Object> row : details) {
            model.addRow(Arrays.stream(columns).map(row::get).toArray());
        }

        String title = (canDecide ? "Xét duyệt yêu cầu nhập kho " : "Chi tiết yêu cầu nhập kho ") + id + " ["
                + status + "]";
        JDialog winDialog = new JDialog(layCuaSoCha(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        JPanel pnlContent = new JPanel(new BorderLayout(0, 10));
        pnlContent.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane scrHeader = new JScrollPane(pnlHeader);
        scrHeader.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrHeader.setPreferredSize(
                new Dimension(840, Math.min(310, pnlHeader.getPreferredSize().height + 12)));

        JPanel pnlTop = new JPanel(new BorderLayout(0, 8));
        pnlTop.add(scrHeader, BorderLayout.CENTER);
        JLabel lblStatusInfo = GiaoDien.label(
                "Trạng thái hiện tại: " + status
                        + (canDecide ? " (Vui lòng thẩm định nội dung và ra quyết định)"
                                : " (Yêu cầu đã được xử lý)"),
                12, true, canDecide ? GiaoDien.NAV : GiaoDien.MUTED);
        pnlTop.add(lblStatusInfo, BorderLayout.NORTH);
        pnlContent.add(pnlTop, BorderLayout.NORTH);

        JScrollPane scrDetails = new JScrollPane(tblDetails);
        scrDetails.setPreferredSize(new Dimension(840, 240));
        JPanel pnlDetailList = new JPanel(new BorderLayout(0, 6));
        pnlDetailList.add(new BoLocTrangThai(tblDetails), BorderLayout.NORTH);
        pnlDetailList.add(scrDetails, BorderLayout.CENTER);
        pnlContent.add(pnlDetailList, BorderLayout.CENTER);
        BoCucBang.install(tblDetails, scrDetails);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));

        if (canDecide) {
            JButton btnApprove = GiaoDien.button("Duyệt yêu cầu", GiaoDien.TEAL, Color.WHITE);
            btnApprove.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(winDialog,
                        "Phê duyệt yêu cầu nhập kho " + id
                                + "? Yêu cầu sẽ chuyển sang trạng thái Đã duyệt để đặt hàng.",
                        "Xác nhận duyệt", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                    return;
                try {
                    dao.YeuCauNhapKhoDAO.approvePurchaseRequest(id, true, role, employeeId);
                    JOptionPane.showMessageDialog(winDialog, "Đã duyệt yêu cầu nhập kho " + id + ".");
                    winDialog.dispose();
                } catch (SQLException | SecurityException ex) {
                    JOptionPane.showMessageDialog(winDialog, ex.getMessage(), "Lỗi phê duyệt",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            JButton btnReject = GiaoDien.button("Từ chối yêu cầu", GiaoDien.ORANGE, Color.WHITE);
            btnReject.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(winDialog, "Từ chối yêu cầu nhập kho " + id + "?",
                        "Xác nhận từ chối", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                    return;
                try {
                    dao.YeuCauNhapKhoDAO.approvePurchaseRequest(id, false, role, employeeId);
                    JOptionPane.showMessageDialog(winDialog, "Đã từ chối yêu cầu nhập kho " + id + ".");
                    winDialog.dispose();
                } catch (SQLException | SecurityException ex) {
                    JOptionPane.showMessageDialog(winDialog, ex.getMessage(), "Lỗi từ chối",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            pnlButtons.add(btnApprove);
            pnlButtons.add(btnReject);
        }

        JButton btnClose = GiaoDien.button("Đóng", GiaoDien.CREAM, GiaoDien.NAV);
        btnClose.addActionListener(e -> winDialog.dispose());
        pnlButtons.add(btnClose);

        for (Component comp : pnlButtons.getComponents()) {
            if (comp instanceof JButton btn) {
                Dimension pref = btn.getPreferredSize();
                btn.setPreferredSize(new Dimension(Math.max(pref.width + 14, 100), 38));
            }
        }

        pnlContent.add(pnlButtons, BorderLayout.SOUTH);
        winDialog.setContentPane(pnlContent);
        winDialog.pack();
        winDialog.setLocationRelativeTo(null);
        winDialog.setVisible(true);
    }

    // ===== PHƯƠNG THỨC: buildDialog(...) — Xây dựng hộp thoại lập mới (từ YCNK), chỉnh sửa hoặc xem chi tiết phiếu nhập kho. =====
    static JDialog buildDialog(Component parent, String table, Map<String, Object> header,
            List<Map<String, Object>> initialDetails, boolean viewOnly, VaiTro role, String employeeId) {
        // 1. Kiểm tra điều kiện bắt buộc: Phiếu nhập kho mới (id == null) phải gắn với maYCNK đã duyệt
        String child = QuyTrinhDAO.detailTable(table), pk = "maPNK";
        String id = header == null || header.get(pk) == null ? null : String.valueOf(header.get(pk));
        if (id == null && (((header == null || header.get("maYCNK") == null)))) {
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(parent,
                        "Phiếu nhập kho phải được lập từ Yêu cầu nhập kho đã duyệt. Vui lòng chọn yêu cầu đã duyệt và bấm 'Lập phiếu nhập kho'.");
            }
            return null;
        }

        // 2. Tải dữ liệu đầu phiếu và chi tiết hạng mục từ CSDL (nếu đang mở phiếu đã tồn tại)
        String lockedReason = null;
        List<Map<String, Object>> details = new ArrayList<>();
        if (id == null && initialDetails != null) {
            for (Map<String, Object> line : initialDetails)
                details.add(new LinkedHashMap<>(line));
        }
        try (Connection conn = KetNoiCSDL.getConnection()) {
            if (id != null) {
                var fresh = new dao.PhieuNhapKhoDAO().getAll(conn, Map.of(pk, id));
                if (fresh.isEmpty())
                    throw new SQLException("Phiếu không còn tồn tại. Vui lòng tải lại danh sách.");
                header = fresh.get(0);
                if (!viewOnly)
                    lockedReason = QuyTrinhDAO.documentEditProblem(conn, table, id);
                details.addAll(new dao.ChiTietPhieuNhapKhoDAO().getAll(conn, Map.of(pk, id)));
            }
        } catch (SQLException ex) {
            hienThiLoiLuuPhieu(parent, ex);
            return null;
        }

        // 3. Khởi tạo biểu mẫu đầu phiếu (BieuMauPhieuNhapKho) và khóa các trường kế thừa từ YCNK
        boolean readOnly = viewOnly || lockedReason != null;
        BieuMauPhieuNhapKho pnlHeader = new BieuMauPhieuNhapKho(table, header,
                id == null ? Set.of(pk) : Set.of(), readOnly, id != null);
        if (!readOnly && id != null)
            pnlHeader.lockField(pk);
        if (!readOnly) {
            pnlHeader.lockField("maYCNK");
            pnlHeader.lockField("maNCC");
            pnlHeader.lockField("trangThai");
            pnlHeader.lockField("ngayNhap");
            if (id != null)
                pnlHeader.lockField("maNhanVien");
        }

        String[] columns = new String[] { "maPhuTung", "soLuongYeuCau", "soLuongThucNhan", "soLuongNhap",
                "donGiaNhap" };
        // Với Phiếu nhập kho, danh sách phụ tùng được khóa 1-1 theo YCNK đã duyệt;
        // người dùng nhấp đúp sửa trực tiếp 3 cột 'soLuongThucNhan', 'soLuongNhap', 'donGiaNhap' trên bảng qua BoChinhSuaOHangMucPNK.

        DefaultTableModel model = new DefaultTableModel(new String[] { "Mã phụ tùng", "Số lượng yêu cầu",
                "Số lượng thực nhận", "Số lượng nhập", "Đơn giá nhập" }, 0) {
            @java.io.Serial
            private static final long serialVersionUID = 1L;

            // ===== PHƯƠNG THỨC: isCellEditable(int, int) — Chỉ cho phép sửa trực tiếp 3 cột thực nhận, số lượng nhập và đơn giá nhập. =====
            @Override
            public boolean isCellEditable(int r, int c) {
                if (readOnly)
                    return false;
                String col = columns[c];
                return "soLuongThucNhan".equals(col) || "soLuongNhap".equals(col)
                        || "donGiaNhap".equals(col);
            }

            // ===== PHƯƠNG THỨC: setValueAt(Object, int, int) — Cập nhật giá trị ô bảng và đồng bộ vào danh sách details. =====
            @Override
            public void setValueAt(Object aValue, int r, int c) {
                super.setValueAt(aValue, r, c);
                if (r < details.size()) {
                    String col = columns[c];
                    details.get(r).put(col, aValue);
                }
            }
        };

        JTextField tfEditor = new JTextField();
        tfEditor.setFont(GiaoDien.font(12, false));
        tfEditor.setBorder(
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GiaoDien.TEAL, 1, true),
                        BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        tfEditor.setHorizontalAlignment(SwingConstants.RIGHT);

        JTable tblDetails = new JTable(model) {
            @java.io.Serial
            private static final long serialVersionUID = 1L;
            private final TableCellRenderer cells = new DinhDangBang();

            // ===== PHƯƠNG THỨC: getCellRenderer(int, int) — Lấy bộ hiển thị định dạng ô bảng. =====
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                return cells;
            }

            // ===== PHƯƠNG THỨC: getCellEditor(int, int) — Trả về BoChinhSuaOHangMucPNK cho 3 cột cho phép nhập trực tiếp. =====
            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (!readOnly) {
                    int modelCol = convertColumnIndexToModel(column);
                    String col = columns[modelCol];
                    if ("soLuongThucNhan".equals(col) || "soLuongNhap".equals(col)
                            || "donGiaNhap".equals(col)) {
                        TableCellEditor ed = getColumnModel().getColumn(column).getCellEditor();
                        if (ed != null)
                            return ed;
                    }
                }
                return super.getCellEditor(row, column);
            }

            // ===== PHƯƠNG THỨC: createDefaultColumnsFromModel() — Gán tiêu đề tiếng Việt cho các cột của bảng. =====
            @Override
            public void createDefaultColumnsFromModel() {
                super.createDefaultColumnsFromModel();
                for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
                    TableColumn column = getColumnModel().getColumn(i);
                    column.setHeaderValue(layTenHienThiCuaCot(getModel().getColumnName(column.getModelIndex())));
                }
            }
        };

        tblDetails.setRowHeight(40);
        tblDetails.setShowGrid(false);
        tblDetails.setIntercellSpacing(new Dimension(0, 0));
        tblDetails.setSelectionBackground(new Color(0xE3F1EF));
        tblDetails.setSelectionForeground(GiaoDien.TEXT);
        tblDetails.setForeground(GiaoDien.TEXT);
        tblDetails.setFont(GiaoDien.font(12, false));
        tblDetails.setFillsViewportHeight(true);
        tblDetails.getTableHeader().setBackground(new Color(0xEDF3F1));
        tblDetails.getTableHeader().setForeground(GiaoDien.TEAL);
        tblDetails.getTableHeader().setPreferredSize(new Dimension(0, 43));
        tblDetails.getTableHeader().setReorderingAllowed(false);
        DinhDangBang.header(tblDetails);
        tblDetails.setAutoCreateRowSorter(true);

        // 4. Gắn bộ chỉnh sửa trực tiếp trên bảng (BoChinhSuaOHangMucPNK) cho 3 cột thực nhận, số lượng nhập và đơn giá nhập
        BoChinhSuaOHangMucPNK pnkEditor = new BoChinhSuaOHangMucPNK(tfEditor, tblDetails, model, details, columns, parent);
        if (!readOnly) {
            tblDetails.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
            for (int i = 0; i < tblDetails.getColumnCount(); i++) {
                int modelCol = tblDetails.convertColumnIndexToModel(i);
                String col = columns[modelCol];
                if ("soLuongThucNhan".equals(col) || "soLuongNhap".equals(col) || "donGiaNhap".equals(col)) {
                    tblDetails.getColumnModel().getColumn(i).setCellEditor(pnkEditor);
                }
            }
        }

        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Map<String, Object> row : details) {
                model.addRow(Arrays.stream(columns).map(row::get).toArray());
            }
        };
        refresh.run();

        JDialog winDialog = new JDialog(layCuaSoCha(parent),
                (id == null ? "Lập phiếu mới" : readOnly ? "Chi tiết " + id : "Chỉnh sửa " + id),
                Dialog.ModalityType.APPLICATION_MODAL);
        JPanel pnlContent = new JPanel(new BorderLayout(0, 10));
        pnlContent.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JScrollPane scrHeader = new JScrollPane(pnlHeader);
        scrHeader.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrHeader.setPreferredSize(
                new Dimension(840, Math.min(310, pnlHeader.getPreferredSize().height + 12)));
        JPanel pnlTop = new JPanel(new BorderLayout(0, 8));
        pnlTop.add(scrHeader, BorderLayout.CENTER);
        if (lockedReason != null) {
            pnlTop.add(GiaoDien.label("<html>Chỉ xem: " + lockedReason + "</html>", 12, true,
                    DinhDangKeThua.TEXT), BorderLayout.NORTH);
        }
        pnlContent.add(pnlTop, BorderLayout.NORTH);
        JScrollPane scrDetails = new JScrollPane(tblDetails);
        scrDetails.setPreferredSize(new Dimension(840, 240));
        JPanel pnlDetailList = new JPanel(new BorderLayout(0, 6));
        pnlDetailList.add(new BoLocTrangThai(tblDetails), BorderLayout.NORTH);
        pnlDetailList.add(scrDetails, BorderLayout.CENTER);
        pnlContent.add(pnlDetailList, BorderLayout.CENTER);
        BoCucBang.install(tblDetails, scrDetails);
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        JButton btnSave = GiaoDien.button(id == null ? "Lưu toàn bộ phiếu" : "Lưu thay đổi", GiaoDien.TEAL,
                Color.WHITE);
        JButton btnClose = GiaoDien.button("Đóng", GiaoDien.CREAM, GiaoDien.NAV);

        // 5. Sự kiện nhấn nút 'Lưu toàn bộ phiếu' / 'Lưu thay đổi': kiểm tra ràng buộc số lượng và lưu qua QuyTrinhDAO
        btnSave.addActionListener(e -> {
            btnSave.setEnabled(false);
            try {
                if (tblDetails.isEditing()) {
                    if (!tblDetails.getCellEditor().stopCellEditing()) {
                        btnSave.setEnabled(true);
                        return;
                    }
                }

                // Kiểm tra từng dòng phụ tùng trên bảng: 0 <= soLuongNhap <= soLuongThucNhan <= soLuongYeuCau và donGiaNhap >= 0
                for (int r = 0; r < model.getRowCount(); r++) {
                    Map<String, Object> detail = details.get(r);
                    String part = String.valueOf(detail.get("maPhuTung"));
                    int req = ((Number) detail.get("soLuongYeuCau")).intValue();

                    int colActual = Arrays.asList(columns).indexOf("soLuongThucNhan");
                    int colAccepted = Arrays.asList(columns).indexOf("soLuongNhap");
                    int colPrice = Arrays.asList(columns).indexOf("donGiaNhap");

                    Object valActual = colActual >= 0 ? model.getValueAt(r, colActual) : null;
                    Object valAccepted = colAccepted >= 0 ? model.getValueAt(r, colAccepted) : null;
                    Object valPrice = colPrice >= 0 ? model.getValueAt(r, colPrice) : null;

                    if (valActual == null || valActual.toString().trim().isEmpty()) {
                        throw new IllegalArgumentException(
                                "Dòng " + (r + 1) + " (" + part + "): Vui lòng nhập số lượng thực nhận.");
                    }
                    if (valAccepted == null || valAccepted.toString().trim().isEmpty()) {
                        throw new IllegalArgumentException("Dòng " + (r + 1) + " (" + part
                                + "): Vui lòng nhập số lượng nhập chấp nhận.");
                    }

                    int actual;
                    int accepted;
                    try {
                        actual = Integer.parseInt(valActual.toString().trim().replace(" ", ""));
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException("Dòng " + (r + 1) + " (" + part
                                + "): Số lượng thực nhận phải là số nguyên.");
                    }
                    try {
                        accepted = Integer.parseInt(valAccepted.toString().trim().replace(" ", ""));
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException("Dòng " + (r + 1) + " (" + part
                                + "): Số lượng nhập chấp nhận phải là số nguyên.");
                    }

                    if (actual < 0) {
                        throw new IllegalArgumentException(
                                "Dòng " + (r + 1) + " (" + part + "): Số lượng thực nhận không được âm.");
                    }
                    if (accepted < 0) {
                        throw new IllegalArgumentException(
                                "Dòng " + (r + 1) + " (" + part + "): Số lượng nhập không được âm.");
                    }
                    if (actual > req) {
                        throw new IllegalArgumentException(
                                "Dòng " + (r + 1) + " (" + part + "): Số lượng thực nhận (" + actual
                                        + ") không được vượt quá số lượng yêu cầu (" + req + ").");
                    }
                    if (accepted > actual) {
                        throw new IllegalArgumentException("Dòng " + (r + 1) + " (" + part
                                + "): Số lượng nhập chấp nhận (" + accepted
                                + ") không được vượt quá số lượng thực nhận (" + actual + ").");
                    }

                    java.math.BigDecimal price = java.math.BigDecimal.ZERO;
                    if (valPrice != null && !valPrice.toString().trim().isEmpty()) {
                        String pText = valPrice.toString().trim().replace(" ", "");
                        if (pText.contains(",") && pText.contains(".")) {
                            if (pText.lastIndexOf(',') > pText.lastIndexOf('.')) {
                                pText = pText.replace(".", "").replace(",", ".");
                            } else {
                                pText = pText.replace(",", "");
                            }
                        } else if (pText.contains(",")) {
                            pText = pText.replace(",", ".");
                        }
                        try {
                            price = new java.math.BigDecimal(pText);
                        } catch (Exception pe) {
                            throw new IllegalArgumentException(
                                    "Dòng " + (r + 1) + " (" + part + "): Đơn giá nhập không hợp lệ.");
                        }
                        if (price.compareTo(java.math.BigDecimal.ZERO) < 0) {
                            throw new IllegalArgumentException(
                                    "Dòng " + (r + 1) + " (" + part + "): Đơn giá nhập không được âm.");
                        }
                    }

                    detail.put("soLuongThucNhan", actual);
                    detail.put("soLuongNhap", accepted);
                    detail.put("donGiaNhap", price);
                }

                // 6. Gọi QuyTrinhDAO.saveDocument để lưu phiếu nhập kho và chi tiết hạng mục
                String savedId = QuyTrinhDAO.saveDocument(table, pnlHeader.read(), details, id, role,
                        employeeId);
                String docName = "phiếu nhập kho " + savedId;
                if (!java.awt.GraphicsEnvironment.isHeadless()
                        && !Boolean.getBoolean("test.suppressDialogs")) {
                    JOptionPane.showMessageDialog(winDialog, "Đã lưu " + docName + " thành công.",
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                }
                winDialog.dispose();
            } catch (HeadlessException | IllegalArgumentException | SQLException ex) {
                if (!java.awt.GraphicsEnvironment.isHeadless()
                        && !Boolean.getBoolean("test.suppressDialogs")) {
                    JOptionPane.showMessageDialog(winDialog, ex.getMessage(), "Không thể lưu phiếu",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    System.err.println("[SAVE ERROR] " + ex.getMessage());
                }
            } finally {
                btnSave.setEnabled(true);
            }
        });
        btnClose.addActionListener(e -> winDialog.dispose());

        if (!readOnly) {
            pnlButtons.add(btnSave);
        }
        pnlButtons.add(btnClose);
        pnlContent.add(pnlButtons, BorderLayout.SOUTH);
        winDialog.setContentPane(pnlContent);
        winDialog.pack();
        winDialog.setLocationRelativeTo(null);

        winDialog.getRootPane().putClientProperty("tblDetails", tblDetails);
        winDialog.getRootPane().putClientProperty("btnSave", btnSave);
        winDialog.getRootPane().putClientProperty("model", model);
        winDialog.getRootPane().putClientProperty("details", details);

        return winDialog;
    }

    // ===== PHƯƠNG THỨC: moHopThoaiChinhSuaHoacXem(...) — Tạo và hiển thị hộp thoại chỉnh sửa hoặc xem chi tiết phiếu nhập kho. =====
    private static void moHopThoaiChinhSuaHoacXem(Component parent, String table, Map<String, Object> header,
            List<Map<String, Object>> initialDetails, boolean viewOnly, VaiTro role, String employeeId) {
        JDialog winDialog = buildDialog(parent, table, header, initialDetails, viewOnly, role, employeeId);
        if (winDialog != null) {
            winDialog.setVisible(true);
        }
    }

    /**
     * Bộ chỉnh sửa trực tiếp trên ô bảng chi tiết Phiếu nhập kho (BoChinhSuaOHangMucPNK).
     * Cho phép nhấp đúp vào các cột 'soLuongThucNhan', 'soLuongNhap', 'donGiaNhap' và kiểm tra hợp lệ ngay khi rời ô.
     */
    private static class BoChinhSuaOHangMucPNK extends DefaultCellEditor {
        private static final long serialVersionUID = 1L;
        private final JTextField textField;
        private final JTable table;
        private final DefaultTableModel model;
        private final List<Map<String, Object>> details;
        private final String[] columns;
        private final Component parentComponent;
        private boolean validating = false;
        private Object parsedValue = null;

        // ===== KHỞI TẠO ĐỐI TƯỢNG: BoChinhSuaOHangMucPNK(...) — Khởi tạo bộ chỉnh sửa ô bảng chi tiết PNK. =====
        BoChinhSuaOHangMucPNK(JTextField tf, JTable table, DefaultTableModel model, List<Map<String, Object>> details,
                String[] columns, Component parentComponent) {
            super(tf);
            this.textField = tf;
            this.table = table;
            this.model = model;
            this.details = details;
            this.columns = columns;
            this.parentComponent = parentComponent;
            setClickCountToStart(2);
        }

        // ===== PHƯƠNG THỨC: getTableCellEditorComponent(...) — Chuẩn bị ô nhập văn bản khi người dùng nhấp đúp vào ô bảng. =====
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
            if (value instanceof java.math.BigDecimal bd) {
                textField.setText(bd.stripTrailingZeros().toPlainString());
            } else if (value != null) {
                textField.setText(value.toString());
            } else {
                textField.setText("");
            }
            parsedValue = null;
            SwingUtilities.invokeLater(textField::selectAll);
            return c;
        }

        // ===== PHƯƠNG THỨC: getCellEditorValue() — Trả về giá trị đã kiểm tra hợp lệ sau khi kết thúc sửa ô. =====
        @Override
        public Object getCellEditorValue() {
            return parsedValue != null ? parsedValue : textField.getText().trim();
        }

        // ===== PHƯƠNG THỨC: stopCellEditing() — Kiểm tra ràng buộc 0 <= soLuongNhap <= soLuongThucNhan <= soLuongYeuCau và donGiaNhap >= 0. =====
        @Override
        public boolean stopCellEditing() {
            if (validating)
                return false;
            int viewRow = table.getEditingRow();
            int viewCol = table.getEditingColumn();
            if (viewRow < 0 || viewCol < 0) {
                return super.stopCellEditing();
            }

            int modelRow = table.convertRowIndexToModel(viewRow);
            int modelCol = table.convertColumnIndexToModel(viewCol);
            if (modelCol < 0 || modelCol >= columns.length) {
                return super.stopCellEditing();
            }
            String colName = columns[modelCol];
            String text = textField.getText().trim();

            if (modelRow < 0 || modelRow >= details.size()) {
                return super.stopCellEditing();
            }

            Map<String, Object> detail = details.get(modelRow);
            String part = String.valueOf(detail.get("maPhuTung"));
            int req = ((Number) detail.get("soLuongYeuCau")).intValue();

            validating = true;
            try {
                if (null != colName)
                    switch (colName) {
                    case "soLuongThucNhan" -> {
                        if (text.isEmpty()) {
                            hienThiLoiNhapO("Dòng " + (modelRow + 1) + " (" + part
                                    + "): Vui lòng nhập số lượng thực nhận.");
                            return false;
                        }
                        int actual;
                        try {
                            actual = Integer.parseInt(text.replace(" ", ""));
                        } catch (NumberFormatException e) {
                            hienThiLoiNhapO("Dòng " + (modelRow + 1) + " (" + part
                                    + "): Số lượng thực nhận phải là số nguyên.");
                            return false;
                        }
                        if (actual < 0) {
                            hienThiLoiNhapO("Dòng " + (modelRow + 1) + " (" + part
                                    + "): Số lượng thực nhận không được âm (" + actual + ").");
                            return false;
                        }
                        if (actual > req) {
                            hienThiLoiNhapO("Dòng " + (modelRow + 1) + " (" + part + "): Số lượng thực nhận ("
                                    + actual + ") không được vượt quá số lượng yêu cầu (" + req + ").");
                            return false;
                        }
                        int colAcceptedIdx = Arrays.asList(columns).indexOf("soLuongNhap");
                        Object curAcceptedObj = colAcceptedIdx >= 0
                                ? model.getValueAt(modelRow, colAcceptedIdx)
                                : null;
                        if (curAcceptedObj != null) {
                            try {
                                int curAccepted = Integer
                                        .parseInt(curAcceptedObj.toString().trim().replace(" ", ""));
                                if (actual < curAccepted) {
                                    hienThiLoiNhapO("Dòng " + (modelRow + 1) + " (" + part
                                            + "): Số lượng thực nhận (" + actual
                                            + ") không được nhỏ hơn số lượng nhập chấp nhận (" + curAccepted
                                            + "). Vui lòng điều chỉnh số lượng nhập trước.");
                                    return false;
                                }
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        parsedValue = actual;
                    }
                    case "soLuongNhap" -> {
                        if (text.isEmpty()) {
                            hienThiLoiNhapO("Dòng " + (modelRow + 1) + " (" + part
                                    + "): Vui lòng nhập số lượng nhập chấp nhận.");
                            return false;
                        }
                        int accepted;
                        try {
                            accepted = Integer.parseInt(text.replace(" ", ""));
                        } catch (NumberFormatException e) {
                            hienThiLoiNhapO("Dòng " + (modelRow + 1) + " (" + part
                                    + "): Số lượng nhập chấp nhận phải là số nguyên.");
                            return false;
                        }
                        if (accepted < 0) {
                            hienThiLoiNhapO("Dòng " + (modelRow + 1) + " (" + part
                                    + "): Số lượng nhập không được âm (" + accepted + ").");
                            return false;
                        }
                        int colActualIdx = Arrays.asList(columns).indexOf("soLuongThucNhan");
                        Object curActualObj = colActualIdx >= 0 ? model.getValueAt(modelRow, colActualIdx)
                                : null;
                        int actual = req;
                        if (curActualObj != null) {
                            try {
                                actual = Integer.parseInt(curActualObj.toString().trim().replace(" ", ""));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        if (accepted > actual) {
                            hienThiLoiNhapO("Dòng " + (modelRow + 1) + " (" + part + "): Số lượng nhập chấp nhận ("
                                    + accepted + ") không được vượt quá số lượng thực nhận (" + actual
                                    + ").");
                            return false;
                        }
                        parsedValue = accepted;
                    }
                    case "donGiaNhap" -> {
                        if (text.isEmpty()) {
                            hienThiLoiNhapO("Dòng " + (modelRow + 1) + " (" + part
                                    + "): Vui lòng nhập đơn giá nhập.");
                            return false;
                        }
                        String clean = text.replace(" ", "");
                        if (clean.contains(",") && clean.contains(".")) {
                            if (clean.lastIndexOf(',') > clean.lastIndexOf('.')) {
                                clean = clean.replace(".", "").replace(",", ".");
                            } else {
                                clean = clean.replace(",", "");
                            }
                        } else if (clean.contains(",")) {
                            clean = clean.replace(",", ".");
                        }
                        java.math.BigDecimal price;
                        try {
                            price = new java.math.BigDecimal(clean);
                        } catch (Exception e) {
                            hienThiLoiNhapO(
                                    "Dòng " + (modelRow + 1) + " (" + part + "): Đơn giá nhập không hợp lệ.");
                            return false;
                        }
                        if (price.compareTo(java.math.BigDecimal.ZERO) < 0) {
                            hienThiLoiNhapO("Dòng " + (modelRow + 1) + " (" + part
                                    + "): Đơn giá nhập không được âm.");
                            return false;
                        }
                        parsedValue = price;
                    }
                    default -> {
                    }
                    }
            } finally {
                validating = false;
            }

            return super.stopCellEditing();
        }

        // ===== PHƯƠNG THỨC: hienThiLoiNhapO(String) — Hiển thị thông báo lỗi khi giá trị nhập trong ô bảng không hợp lệ. =====
        private void hienThiLoiNhapO(String msg) {
            Component win = parentComponent;
            if (win == null || !win.isShowing()) {
                win = SwingUtilities.getWindowAncestor(table);
            }
            if (!java.awt.GraphicsEnvironment.isHeadless() && !Boolean.getBoolean("test.suppressDialogs")) {
                JOptionPane.showMessageDialog(win != null ? win : table, msg, "Dữ liệu không hợp lệ",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                System.err.println("[VALIDATION ERROR] " + msg);
            }
        }
    }

    // ===== PHƯƠNG THỨC: hienThiLoiLuuPhieu(Component, SQLException) — Hiển thị hộp thoại báo lỗi SQL thân thiện cho người dùng. =====
    private static void hienThiLoiLuuPhieu(Component parent, SQLException ex) {
        JOptionPane.showMessageDialog(parent, ThongBaoLoiSQL.userMessage(ex, ""), "Không thể lưu phiếu",
                JOptionPane.ERROR_MESSAGE);
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
        private static final Color WHITE = Color.WHITE;
        private static final Color LINE = new Color(0xE4EBE9);

        private static final Color NAV = new Color(0x13272E);
        private static final Color TEAL = new Color(0x487674);
        private static final Color CREAM = new Color(0xE8C69B);
        private static final Color ORANGE = new Color(0xCB6330);
        private static final Color TEXT = new Color(0x21383C);
        private static final Color MUTED = new Color(0x718387);

        // ===== PHƯƠNG THỨC: font(int, boolean) — Tạo phông chữ theo cỡ và kiểu chữ. =====
        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
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

    // ===== PHƯƠNG THỨC: layTenHienThiCuaCot(String) — Trả về nhãn tiếng Việt cho cột bảng chi tiết phiếu nhập kho. =====
    private static String layTenHienThiCuaCot(String field) {
        return switch (field) {
        case "maPhuTung" -> "Mã phụ tùng";
        case "soLuongYeuCau" -> "Số lượng yêu cầu";
        case "soLuongThucNhan" -> "Số lượng thực nhận";
        case "soLuongNhap" -> "Số lượng nhập";
        case "donGiaNhap" -> "Đơn giá nhập";
        default -> field;
        };
    }
}
