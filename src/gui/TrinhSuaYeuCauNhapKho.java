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
 * Hộp thoại GUI: Lập mới, chỉnh sửa, xem chi tiết và xét duyệt Yêu cầu nhập kho (TrinhSuaYeuCauNhapKho).
 * - Gồm 2 phần trên cùng một cửa sổ:
 *   1. Đầu phiếu (BieuMauYeuCauNhapKho): mã yêu cầu, nhà cung cấp, nhân viên lập, ngày tạo, trạng thái, ghi chú.
 *   2. Bảng chi tiết (ChiTietYeuCauNhapKho): thêm/sửa/bỏ từng dòng phụ tùng qua BieuMauChiTietYeuCauNhapKho.
 * - Khi nhấn 'Lưu toàn bộ phiếu' / 'Lưu thay đổi': gọi QuyTrinhDAO.saveDocument("YeuCauNhapKho", ...).
 * - Khi Quản lý mở xét duyệt (review -> moHopThoaiXetDuyet): gọi YeuCauNhapKhoDAO.approvePurchaseRequest(...).
 */
final class TrinhSuaYeuCauNhapKho {
    // ===== KHỞI TẠO ĐỐI TƯỢNG: TrinhSuaYeuCauNhapKho() — Lớp tiện ích tĩnh, không khởi tạo trực tiếp. =====
    private TrinhSuaYeuCauNhapKho() {
    }

    // ===== PHƯƠNG THỨC: show(Component, String, Map<String,Object>, VaiTro, String) — Mở hộp thoại lập mới hoặc chỉnh sửa yêu cầu nhập kho. =====
    static void show(Component parent, String table, Map<String, Object> header, VaiTro role,
            String employeeId) {
        show(parent, table, header, List.of(), role, employeeId);
    }

    // ===== PHƯƠNG THỨC: show(Component, String, Map<String,Object>, List<Map<String,Object>>, VaiTro, String) — Mở hộp thoại kèm danh sách chi tiết khởi tạo sẵn. =====
    static void show(Component parent, String table, Map<String, Object> header,
            List<Map<String, Object>> initialDetails, VaiTro role, String employeeId) {
        moHopThoaiChinhSuaHoacXem(parent, table, header, initialDetails, false, role, employeeId);
    }

    // ===== PHƯƠNG THỨC: view(Component, String, Map<String,Object>) — Mở hộp thoại xem chi tiết yêu cầu nhập kho (chỉ đọc). =====
    static void view(Component parent, String table, Map<String, Object> header) {
        moHopThoaiChinhSuaHoacXem(parent, table, header, List.of(), true, null, null);
    }

    // ===== PHƯƠNG THỨC: review(Component, String, Map<String,Object>, VaiTro, String) — Mở hộp thoại dành cho Quản lý xét duyệt yêu cầu nhập kho. =====
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

    // ===== PHƯƠNG THỨC: moHopThoaiXetDuyet(...) — Mở hộp thoại thẩm định và phê duyệt / từ chối yêu cầu nhập kho (vai trò Quản lý). =====
    private static void moHopThoaiXetDuyet(Component parent, String table, Map<String, Object> header, VaiTro role,
            String employeeId) {
        // 1. Kiểm tra mã yêu cầu nhập kho cần xét duyệt
        String pk = "maYCNK";
        String id = header == null || header.get(pk) == null ? null : String.valueOf(header.get(pk));
        if (id == null) {
            JOptionPane.showMessageDialog(parent, "Không tìm thấy mã phiếu cần xét duyệt.", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Tải lại thông tin đầu phiếu và danh sách phụ tùng mới nhất từ CSDL
        List<Map<String, Object>> details = new ArrayList<>();
        Map<String, Object> freshHeader = header;
        try (Connection conn = KetNoiCSDL.getConnection()) {
            var fresh = new dao.YeuCauNhapKhoDAO().getAll(conn, Map.of(pk, id));
            if (fresh.isEmpty())
                throw new SQLException("Phiếu không còn tồn tại. Vui lòng tải lại danh sách.");
            freshHeader = fresh.get(0);
            details.addAll(new dao.ChiTietYeuCauNhapKhoDAO().getAll(conn, Map.of(pk, id)));
        } catch (SQLException ex) {
            hienThiLoiLuuPhieu(parent, ex);
            return;
        }

        String status = String.valueOf(freshHeader.get("trangThai"));
        boolean isPending = "Chờ duyệt".equals(status);
        boolean canDecide = isPending && (role == VaiTro.QUAN_LY || role == VaiTro.ADMIN);

        // 3. Tạo biểu mẫu đầu phiếu (chỉ đọc) và bảng chi tiết hạng mục phụ tùng
        BieuMauYeuCauNhapKho pnlHeader = new BieuMauYeuCauNhapKho(table, freshHeader, Set.of(), true, true);

        String[] columns = new String[] { "maPhuTung", "soLuongYeuCau", "donGiaDuKien" };
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "Mã phụ tùng", "Số lượng yêu cầu", "Đơn giá dự kiến" }, 0) {
            @java.io.Serial
            private static final long serialVersionUID = 1L;

            // ===== PHƯƠNG THỨC: isCellEditable(int, int) — Khóa chỉnh sửa bảng chi tiết khi đang xét duyệt. =====
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

        // 4. Đăng ký sự kiện cho nút 'Duyệt yêu cầu' và 'Từ chối yêu cầu' (gọi YeuCauNhapKhoDAO.approvePurchaseRequest)
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

    // ===== PHƯƠNG THỨC: buildDialog(...) — Xây dựng hộp thoại lập mới, chỉnh sửa hoặc xem chi tiết yêu cầu nhập kho. =====
    static JDialog buildDialog(Component parent, String table, Map<String, Object> header,
            List<Map<String, Object>> initialDetails, boolean viewOnly, VaiTro role, String employeeId) {
        // 1. Xác định khóa chính (maYCNK) và tải dữ liệu hiện tại từ CSDL nếu đang sửa/xem phiếu đã có
        String child = QuyTrinhDAO.detailTable(table), pk = "maYCNK";
        String id = header == null || header.get(pk) == null ? null : String.valueOf(header.get(pk));

        String lockedReason = null;
        List<Map<String, Object>> details = new ArrayList<>();
        if (id == null && initialDetails != null) {
            for (Map<String, Object> line : initialDetails)
                details.add(new LinkedHashMap<>(line));
        }
        try (Connection conn = KetNoiCSDL.getConnection()) {
            if (id != null) {
                var fresh = new dao.YeuCauNhapKhoDAO().getAll(conn, Map.of(pk, id));
                if (fresh.isEmpty())
                    throw new SQLException("Phiếu không còn tồn tại. Vui lòng tải lại danh sách.");
                header = fresh.get(0);
                if (!viewOnly)
                    lockedReason = QuyTrinhDAO.documentEditProblem(conn, table, id);
                details.addAll(new dao.ChiTietYeuCauNhapKhoDAO().getAll(conn, Map.of(pk, id)));
            }
        } catch (SQLException ex) {
            hienThiLoiLuuPhieu(parent, ex);
            return null;
        }

        // 2. Khởi tạo biểu mẫu đầu phiếu (BieuMauYeuCauNhapKho) và khóa các trường hệ thống tự sinh
        boolean readOnly = viewOnly || lockedReason != null;
        BieuMauYeuCauNhapKho pnlHeader = new BieuMauYeuCauNhapKho(table, header,
                id == null ? Set.of(pk) : Set.of(), readOnly, id != null);
        if (!readOnly && id != null)
            pnlHeader.lockField(pk);

        if (!readOnly) {
            pnlHeader.lockField("maNhanVien");
            pnlHeader.lockField("trangThai");
            pnlHeader.lockField("maNhanVienDuyet");
            pnlHeader.lockField("ngayDuyet");
        }
        String[] columns = new String[] { "maPhuTung", "soLuongYeuCau", "donGiaDuKien" };

        DefaultTableModel model = new DefaultTableModel(
                new String[] { "Mã phụ tùng", "Số lượng yêu cầu", "Đơn giá dự kiến" }, 0) {
            @java.io.Serial
            private static final long serialVersionUID = 1L;

            // ===== PHƯƠNG THỨC: isCellEditable(int, int) — Khóa sửa trực tiếp trên bảng (YCNK sửa qua hộp thoại BieuMauChiTietYeuCauNhapKho). =====
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable tblDetails = new JTable(model) {
            @java.io.Serial
            private static final long serialVersionUID = 1L;
            private final TableCellRenderer cells = new DinhDangBang();

            // ===== PHƯƠNG THỨC: getCellRenderer(int, int) — Lấy bộ hiển thị định dạng ô bảng. =====
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                return cells;
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
        JButton btnAdd = GiaoDien.button("Thêm hạng mục", GiaoDien.TEAL, Color.WHITE);
        JButton btnEdit = GiaoDien.button("Sửa hạng mục", GiaoDien.CREAM, GiaoDien.NAV);
        JButton btnRemove = GiaoDien.button("Bỏ hạng mục", GiaoDien.CREAM, GiaoDien.NAV);
        JButton btnSave = GiaoDien.button(id == null ? "Lưu toàn bộ phiếu" : "Lưu thay đổi", GiaoDien.TEAL,
                Color.WHITE);
        JButton btnClose = GiaoDien.button("Đóng", GiaoDien.CREAM, GiaoDien.NAV);

        // 3. Đăng ký sự kiện thêm / sửa / bỏ hạng mục phụ tùng và sự kiện Lưu toàn bộ phiếu
        tblDetails.addMouseListener(new java.awt.event.MouseAdapter() {
            // ===== PHƯƠNG THỨC: mouseClicked(java.awt.event.MouseEvent) — Nhấp đúp dòng chi tiết để mở hộp thoại sửa hạng mục. =====
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getClickCount() == 2 && !readOnly)
                    btnEdit.doClick();
            }
        });

        btnAdd.addActionListener(e -> {
            Map<String, Object> row = moHopThoaiHangMucChiTiet(winDialog, child, null, pk);
            if (row != null) {
                details.add(row);
                refresh.run();
            }
        });
        btnEdit.addActionListener(e -> {
            int selected = tblDetails.getSelectedRow();
            if (selected < 0) {
                if (!java.awt.GraphicsEnvironment.isHeadless())
                    JOptionPane.showMessageDialog(winDialog, "Vui lòng chọn hạng mục cần sửa.");
                return;
            }
            int index = tblDetails.convertRowIndexToModel(selected);
            Map<String, Object> row = moHopThoaiHangMucChiTiet(winDialog, child, details.get(index), pk);
            if (row != null) {
                details.set(index, row);
                refresh.run();
            }
        });
        btnRemove.addActionListener(e -> {
            int selected = tblDetails.getSelectedRow();
            if (selected >= 0) {
                details.remove(tblDetails.convertRowIndexToModel(selected));
                refresh.run();
            }
        });

        btnSave.addActionListener(e -> {
            btnSave.setEnabled(false);
            try {
                if (tblDetails.isEditing()) {
                    if (!tblDetails.getCellEditor().stopCellEditing()) {
                        btnSave.setEnabled(true);
                        return;
                    }
                }

                // 6. Gọi QuyTrinhDAO.saveDocument để lưu đầu phiếu và toàn bộ hạng mục trong cùng 1 transaction
                String savedId = QuyTrinhDAO.saveDocument(table, pnlHeader.read(), details, id, role,
                        employeeId);
                String docName = "yêu cầu nhập kho " + savedId;
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
            pnlButtons.add(btnAdd);
            pnlButtons.add(btnRemove);
            pnlButtons.add(btnEdit);
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

    // ===== PHƯƠNG THỨC: moHopThoaiChinhSuaHoacXem(...) — Tạo và hiển thị hộp thoại chỉnh sửa hoặc xem chi tiết yêu cầu nhập kho. =====
    private static void moHopThoaiChinhSuaHoacXem(Component parent, String table, Map<String, Object> header,
            List<Map<String, Object>> initialDetails, boolean viewOnly, VaiTro role, String employeeId) {
        JDialog winDialog = buildDialog(parent, table, header, initialDetails, viewOnly, role, employeeId);
        if (winDialog != null) {
            winDialog.setVisible(true);
        }
    }

    // ===== PHƯƠNG THỨC: moHopThoaiHangMucChiTiet(...) — Mở biểu mẫu BieuMauChiTietYeuCauNhapKho để thêm hoặc sửa 1 dòng phụ tùng. =====
    private static Map<String, Object> moHopThoaiHangMucChiTiet(Component parent, String child, Map<String, Object> row,
            String foreignKey) {
        // Khóa hạng mục được nhập lại trong bản nháp; đầu phiếu sẽ gắn khi lưu.
        BieuMauChiTietYeuCauNhapKho pnlForm = new BieuMauChiTietYeuCauNhapKho(child, row, Set.of(foreignKey));
        JScrollPane scrForm = new JScrollPane(pnlForm);
        scrForm.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrForm.setPreferredSize(new Dimension(660, Math.min(380, pnlForm.getPreferredSize().height + 12)));
        Object[] options = { "Áp dụng vào phiếu", "Hủy" };
        while (JOptionPane.showOptionDialog(parent, scrForm, "Chỉnh sửa hạng mục — lưu phiếu để ghi nhận",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
                options[0]) == JOptionPane.OK_OPTION) {
            try {
                Map<String, Object> values = pnlForm.read();
                // ThongBaoLoiSQL bỏ khóa chính của bản ghi đang sửa; giữ lại khóa dịch vụ nếu cần.
                return values;
            } catch (SQLException ex) {
                hienThiLoiLuuPhieu(parent, ex);
            }
        }
        return null;
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

    // ===== PHƯƠNG THỨC: layTenHienThiCuaCot(String) — Trả về nhãn tiếng Việt cho cột bảng chi tiết yêu cầu nhập kho. =====
    private static String layTenHienThiCuaCot(String field) {
        return switch (field) {
        case "maPhuTung" -> "Mã phụ tùng";
        case "soLuongYeuCau" -> "Số lượng yêu cầu";
        case "donGiaDuKien" -> "Đơn giá dự kiến";
        default -> field;
        };
    }
}
