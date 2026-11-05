package gui;

import dao.ChonDAO;

import connectDB.KetNoiCSDL;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.*;

// ===== GUI: THAO TÁC NGHIỆP VỤ DÙNG CHUNG =====

/** Các thao tác gắn với phiếu đang chọn, không nhập tay trạng thái nghiệp vụ. */
final class ThaoTacNghiepVu {
    // ===== THÀNH PHẦN GIAO DIỆN =====
    // ===== KHỞI TẠO GIAO DIỆN =====
    // ===== KHỞI TẠO VÀ XỬ LÝ GIAO DIỆN =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ThaoTacNghiepVu() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    private ThaoTacNghiepVu() {
    }

    // ===== PHƯƠNG THỨC: install(JPanel, String, String, Supplier<Map<String,Object>>, Runnable, Component, VaiTro, String, JTable) — Đăng ký bộ điều chỉnh độ rộng cột cho bảng. =====
    static void install(JPanel pnlButtons, String table, String page, Supplier<Map<String, Object>> selection,
            Runnable refresh, Component parent, VaiTro role, String employeeId, JTable tblTable) {
        String[] actions = switch (table) {
        case "PhieuTiepNhan" -> new String[] { "Lập phiếu kiểm tra" };
        case "PhieuKiemTra" -> new String[] { "Lập phiếu báo giá" };
        case "PhieuBaoGia" -> new String[] { "Xác nhận báo giá", "Lập phiếu sửa chữa" };
        case "PhieuSuaChua" -> new String[] { "Hủy", "Bàn giao" };
        case "YeuCauNhapKho" -> getYeuCauNhapKhoActions(page, role);
        case "PhieuNhapKho" -> getPhieuNhapKhoActions(role);
        case "LichNhacBaoDuong" -> new String[] { "Ghi nhận đã gửi" };
        default -> new String[0];
        };
        if (actions.length == 0)
            return;
        boolean direct = Set.of("PhieuTiepNhan", "PhieuKiemTra", "PhieuBaoGia", "PhieuSuaChua",
                "PhieuNhapKho", "YeuCauNhapKho", "LichNhacBaoDuong").contains(table);
        if (direct) {
            for (String action : actions)
                installButton(pnlButtons, table, selection, refresh, parent, action, null, role, employeeId,
                        tblTable);
        } else {
            JComboBox<String> cmbAction = new OChonTimKiem<>(actions);
            pnlButtons.add(cmbAction);
            installButton(pnlButtons, table, selection, refresh, parent, null, cmbAction, role, employeeId,
                    tblTable);
        }
    }

    // ===== PHƯƠNG THỨC: getYeuCauNhapKhoActions(String, VaiTro) — Lấy yêu cầu nhập kho actions. =====
    private static String[] getYeuCauNhapKhoActions(String page, VaiTro role) {
        if ("Danh sách yêu cầu nhập".equals(page)) {
            return new String[] { "Duyệt yêu cầu", "Từ chối yêu cầu" };
        }
        if ("Phiếu yêu cầu nhập kho".equals(page) || "Yêu cầu nhập kho".equals(page)) {
            return new String[] { "Lập phiếu nhập kho" };
        }
        if (role == VaiTro.QUAN_LY) {
            return new String[] { "Duyệt yêu cầu", "Từ chối yêu cầu" };
        }
        if (role == VaiTro.KHO) {
            return new String[] { "Lập phiếu nhập kho" };
        }
        if (role == VaiTro.ADMIN) {
            return new String[] { "Duyệt yêu cầu", "Từ chối yêu cầu", "Lập phiếu nhập kho" };
        }
        return new String[0];
    }

    // ===== PHƯƠNG THỨC: getPhieuNhapKhoActions(VaiTro) — Lấy phiếu nhập kho actions. =====
    private static String[] getPhieuNhapKhoActions(VaiTro role) {
        if (role == VaiTro.KHO || role == VaiTro.ADMIN) {
            return new String[] { "Xác nhận nhập kho" };
        }
        return new String[0];
    }

    // ===== PHƯƠNG THỨC: installButton(JPanel, String, Supplier<Map<String,Object>>, Runnable, Component, String, JComboBox<String>, VaiTro, String, JTable) — Gắn hành động và kiểu hiển thị cho nút. =====
    private static void installButton(JPanel pnlButtons, String table,
            Supplier<Map<String, Object>> selection, Runnable refresh, Component parent, String fixedAction,
            JComboBox<String> cmbAction, VaiTro role, String employeeId, JTable tblTable) {
        JButton btnApply = GiaoDien.button(fixedAction == null ? "Thực hiện" : fixedAction,
                "Từ chối yêu cầu".equals(fixedAction) ? GiaoDien.ORANGE : GiaoDien.TEAL, Color.WHITE);
        pnlButtons.add(btnApply);

        if (tblTable != null && fixedAction != null) {
            switch (fixedAction) {
            case "Duyệt yêu cầu", "Từ chối yêu cầu" -> {
                btnApply.setVisible(false);
                tblTable.getSelectionModel().addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        Map<String, Object> row = selection.get();
                        boolean isPending = row != null && "Chờ duyệt".equals(row.get("trangThai"));
                        btnApply.setVisible(isPending);
                        btnApply.setEnabled(isPending);
                        pnlButtons.revalidate();
                        pnlButtons.repaint();
                    }
                });
            }
            case "Lập phiếu nhập kho" -> {
                btnApply.setVisible(false);
                tblTable.getSelectionModel().addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        Map<String, Object> row = selection.get();
                        boolean isApproved = row != null && "Đã duyệt".equals(row.get("trangThai"));
                        btnApply.setVisible(isApproved);
                        btnApply.setEnabled(isApproved);
                        pnlButtons.revalidate();
                        pnlButtons.repaint();
                    }
                });
            }
            case "Xác nhận nhập kho" -> {
                btnApply.setVisible(false);
                tblTable.getSelectionModel().addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        Map<String, Object> row = selection.get();
                        Object st = row != null ? row.get("trangThai") : null;
                        boolean isDraft = row != null && "Chờ xác nhận nhập kho".equals(st);
                        btnApply.setVisible(isDraft);
                        btnApply.setEnabled(isDraft);
                        pnlButtons.revalidate();
                        pnlButtons.repaint();
                    }
                });
            }
            default -> {
            }
            }
        }
        btnApply.addActionListener(e -> {
            Map<String, Object> row = selection.get();
            if (row == null) {
                JOptionPane.showMessageDialog(parent,
                        "Vui lòng chọn một bản ghi trong bảng trước khi thực hiện thao tác.",
                        "Chưa chọn bản ghi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String action = fixedAction != null ? fixedAction : (String) cmbAction.getSelectedItem();
            if (action == null) {
                JOptionPane.showMessageDialog(parent, "Vui lòng chọn một nghiệp vụ trong danh sách.");
                return;
            }
            String id = String.valueOf(row.get(ChonDAO.khoaChinh(table)));
            String message = action + " cho " + id + "?";
            if (action.equals("Nghiệm thu đạt"))
                message = "Xác nhận TẤT CẢ hạng mục đã hoàn thành và nghiệm thu Đạt cho " + id + "?";
            if (action.equals("Ghi nhận đã gửi"))
                message = "Bạn đã gửi thông báo cho khách và muốn ghi nhận lịch này là Đã gửi?";
            if (action.equals("Hủy"))
                message = "Hủy phiếu chỉ sau khi đối soát phần đã cấp phát và phần trả kho thực tế. Tiếp tục?";
            if (action.equals("Duyệt yêu cầu"))
                message = "Phê duyệt yêu cầu nhập kho " + id
                        + "? Yêu cầu sẽ chuyển sang trạng thái Đã duyệt để đặt hàng.";
            if (action.equals("Từ chối yêu cầu"))
                message = "Từ chối yêu cầu nhập kho " + id + "?";
            if (action.equals("Lập phiếu nhập kho"))
                message = "Tiếp nhận hàng thực tế và lập phiếu nhập kho cho yêu cầu " + id + "?";
            if (action.equals("Xác nhận nhập kho"))
                message = "Xác nhận hàng thực tế đã nhập kho cho " + id
                        + "? Tồn kho sẽ được tăng theo số lượng chấp nhận.";

            if (JOptionPane.showConfirmDialog(parent, message, "Xác nhận",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                return;
            btnApply.setEnabled(false);
            try {
                String cancellationReason = null, manager = null;
                if (action.equals("Hủy")) {
                    CancellationDetails details = askCancellationDetails(parent, id);
                    if (details == null)
                        return;
                    cancellationReason = details.reason();
                    manager = details.approverId();
                }
                switch (action) {
                case "Lập phiếu kiểm tra" -> {
                    String employee = choose(parent, "NhanVien", "maNhanVien", "Nhân viên kiểm tra");
                    if (employee == null)
                        return;
                    Map<String, Object> seed = new LinkedHashMap<>();
                    seed.put("maPTN", id);
                    seed.put("VIN", row.get("VIN"));
                    seed.put("maKhachHang", row.get("maKhachHang"));
                    seed.put("maNhanVien", employee);
                    seed.put("ngayKiemTra", new Timestamp(System.currentTimeMillis()));
                    TrinhSuaPhieu.show(parent, "PhieuKiemTra", seed, role, employeeId);
                }
                case "Lập phiếu báo giá" -> {
                    String employee = choose(parent, "NhanVien", "maNhanVien", "Nhân viên lập báo giá");
                    if (employee == null)
                        return;
                    Map<String, Object> seed = new LinkedHashMap<>();
                    seed.put("maPKT", id);
                    seed.put("VIN", row.get("VIN"));
                    seed.put("maNhanVien", employee);
                    seed.put("ngayLap", new Timestamp(System.currentTimeMillis()));
                    seed.put("trangThai", "Còn hiệu lực");
                    TrinhSuaPhieu.show(parent, "PhieuBaoGia", seed, role, employeeId);
                }
                case "Xác nhận báo giá" -> dao.PhieuBaoGiaDAO.confirmQuote(id);
                case "Lập phiếu sửa chữa" -> {
                    String employee = choose(parent, "NhanVien", "maNhanVien",
                            "Nhân viên phụ trách sửa chữa");
                    if (employee == null)
                        return;
                    String created = dao.PhieuSuaChuaDAO.createRepair(id, employee);
                    JOptionPane.showMessageDialog(parent,
                            "Đã tạo " + created + ". Cấp phát phụ tùng trước khi bắt đầu sửa chữa.");
                }
                case "Bắt đầu sửa chữa" -> dao.PhieuSuaChuaDAO.startRepair(id);
                case "Nghiệm thu đạt" -> dao.PhieuSuaChuaDAO.acceptRepair(id);
                case "Duyệt yêu cầu" -> {
                    if (role != gui.VaiTro.QUAN_LY && role != gui.VaiTro.ADMIN) {
                        JOptionPane.showMessageDialog(parent,
                                "Chỉ Quản lý hoặc Quản trị viên mới có quyền duyệt yêu cầu nhập kho.",
                                "Phân quyền", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    dao.YeuCauNhapKhoDAO.approvePurchaseRequest(id, true, role, employeeId);
                    JOptionPane.showMessageDialog(parent, "Đã duyệt yêu cầu nhập kho " + id + ".");
                }
                case "Từ chối yêu cầu" -> {
                    if (role != gui.VaiTro.QUAN_LY && role != gui.VaiTro.ADMIN) {
                        JOptionPane.showMessageDialog(parent,
                                "Chỉ Quản lý hoặc Quản trị viên mới có quyền từ chối yêu cầu nhập kho.",
                                "Phân quyền", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    dao.YeuCauNhapKhoDAO.approvePurchaseRequest(id, false, role, employeeId);
                    JOptionPane.showMessageDialog(parent, "Đã từ chối yêu cầu nhập kho " + id + ".");
                }
                case "Lập phiếu nhập kho" -> {
                    if (role == gui.VaiTro.QUAN_LY) {
                        JOptionPane.showMessageDialog(parent,
                                "Quản lý chỉ có quyền duyệt hoặc từ chối yêu cầu, không được lập phiếu nhập kho thực tế.",
                                "Phân quyền", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (role != gui.VaiTro.KHO && role != gui.VaiTro.ADMIN) {
                        JOptionPane.showMessageDialog(parent,
                                "Chỉ Nhân viên kho hoặc Quản trị viên mới có quyền lập phiếu nhập kho.",
                                "Phân quyền", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    String state = String.valueOf(row.get("trangThai"));
                    if (!"Đã duyệt".equals(state)) {
                        JOptionPane.showMessageDialog(parent,
                                "Chỉ có thể lập phiếu nhập kho cho yêu cầu ở trạng thái 'Đã duyệt'.\nTrạng thái hiện tại: "
                                        + state,
                                "Không thể lập phiếu", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Kiểm tra xem yêu cầu này đã có phiếu nhập kho nào chưa (1 yêu cầu chỉ có tối đa 1 phiếu)
                    try (Connection conn = KetNoiCSDL.getConnection();
                            PreparedStatement ps = conn.prepareStatement(
                                    "SELECT maPNK, trangThai FROM dbo.PhieuNhapKho WHERE maYCNK=?")) {
                        ps.setString(1, id);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                String existingPnk = rs.getString("maPNK");
                                String pnkState = rs.getString("trangThai");
                                if ("Chờ xác nhận nhập kho".equals(pnkState)) {
                                    JOptionPane.showMessageDialog(parent,
                                            "Yêu cầu nhập kho này đã có phiếu nhập kho nháp (" + existingPnk
                                                    + ").\n"
                                                    + "Hệ thống sẽ mở phiếu nháp hiện tại để bạn tiếp tục chỉnh sửa.",
                                            "Mở phiếu nháp hiện có", JOptionPane.INFORMATION_MESSAGE);
                                    List<LinkedHashMap<String, Object>> pnkFresh = new dao.PhieuNhapKhoDAO()
                                            .getAll(conn, Map.of("maPNK", existingPnk));
                                    if (!pnkFresh.isEmpty()) {
                                        TrinhSuaPhieu.show(parent, "PhieuNhapKho", pnkFresh.get(0), role,
                                                employeeId);
                                        return;
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(parent,
                                            "Yêu cầu nhập kho " + id + " đã được xử lý bởi phiếu nhập kho "
                                                    + existingPnk + " (trạng thái: '" + pnkState
                                                    + "').\nKhông thể tạo thêm phiếu nhập kho mới.",
                                            "Yêu cầu đã xử lý", JOptionPane.WARNING_MESSAGE);
                                    return;
                                }
                            }
                        }
                    }

                    String emp = employeeId;
                    if (emp == null || emp.isBlank()) {
                        JOptionPane.showMessageDialog(parent,
                                "Không tìm thấy thông tin nhân viên trong phiên làm việc. Vui lòng đăng nhập lại.",
                                "Lỗi danh tính", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    Map<String, Object> header = new LinkedHashMap<>();
                    header.put("maYCNK", id);
                    header.put("maNCC", row.get("maNCC"));
                    header.put("maNhanVien", emp);
                    header.put("ngayNhap", new Timestamp(System.currentTimeMillis() / 1000 * 1000));
                    header.put("trangThai", "Chờ xác nhận nhập kho");

                    List<LinkedHashMap<String, Object>> reqLines;
                    try (Connection conn = KetNoiCSDL.getConnection()) {
                        reqLines = new dao.ChiTietYeuCauNhapKhoDAO().getAll(conn, Map.of("maYCNK", id));
                    }
                    if (reqLines.isEmpty()) {
                        JOptionPane.showMessageDialog(parent,
                                "Yêu cầu nhập kho này không có hạng mục chi tiết phụ tùng.", "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    List<Map<String, Object>> initialDetails = new ArrayList<>();
                    for (Map<String, Object> line : reqLines) {
                        Map<String, Object> detail = new LinkedHashMap<>();
                        detail.put("maPhuTung", line.get("maPhuTung"));
                        int qty = ((Number) line.get("soLuongYeuCau")).intValue();
                        detail.put("soLuongYeuCau", qty);
                        detail.put("soLuongThucNhan", null);
                        detail.put("soLuongNhap", null);
                        detail.put("donGiaNhap", line.get("donGiaDuKien"));
                        initialDetails.add(detail);
                    }
                    TrinhSuaPhieu.show(parent, "PhieuNhapKho", header, initialDetails, role, employeeId);
                }
                case "Xác nhận nhập kho" -> {
                    if (role != gui.VaiTro.KHO && role != gui.VaiTro.ADMIN) {
                        JOptionPane.showMessageDialog(parent,
                                "Chỉ Nhân viên kho hoặc Quản trị viên mới có quyền xác nhận nhập kho thực tế.",
                                "Phân quyền", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    String pnkState = String.valueOf(row.get("trangThai"));
                    if (!"Chờ xác nhận nhập kho".equals(pnkState)) {
                        JOptionPane.showMessageDialog(parent,
                                "Chỉ có thể xác nhận nhập kho cho phiếu ở trạng thái 'Chờ xác nhận nhập kho'.\nTrạng thái hiện tại: '"
                                        + pnkState + "'.",
                                "Phiếu đã xử lý", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    Object ycnId = row.get("maYCNK");
                    if (ycnId == null || String.valueOf(ycnId).isBlank()) {
                        JOptionPane.showMessageDialog(parent, """
                                Phiếu nhập kho này là phiếu lịch sử (không gắn Yêu cầu nhập kho).
                                Không thuộc quy trình xác nhận nhập kho thực tế UC009.
                                """, "Bản ghi lịch sử", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (employeeId == null || employeeId.isBlank()) {
                        JOptionPane.showMessageDialog(parent,
                                "Không tìm thấy thông tin nhân viên xác nhận trong phiên làm việc. Vui lòng đăng nhập lại.",
                                "Lỗi danh tính", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    dao.PhieuNhapKhoDAO.confirmActualStockIn(id, role, employeeId);
                    JOptionPane.showMessageDialog(parent, "Đã xác nhận nhập kho thực tế thành công cho " + id
                            + ".\nTồn kho phụ tùng đã được cập nhật.");
                }
                case "Ghi nhận đã gửi" -> dao.LichNhacBaoDuongDAO.markReminder(id);
                case "Bàn giao" -> dao.PhieuSuaChuaDAO.handover(id);
                case "Hủy" -> dao.PhieuSuaChuaDAO.cancelRepair(id, cancellationReason, manager);
                default -> {
                }
                }
                refresh.run();
            } catch (SQLException | SecurityException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Không thể thực hiện",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                btnApply.setEnabled(true);
            }
        });
    }

    private record CancellationDetails(String reason, String approverId) {
    }

    // ===== PHƯƠNG THỨC: askCancellationDetails(Component, String) — Thu thập lý do và thông tin khi hủy. =====
    private static CancellationDetails askCancellationDetails(Component parent, String repairId)
            throws SQLException {
        JTextArea txtReason = new JTextArea(3, 28);
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        OChonTimKiem<MucNguoiDuyet> cmbApprover = new OChonTimKiem<>();
        cmbApprover.setEditable(false);
        cmbApprover.setPreferredSize(new Dimension(360, 32));
        napNguoiDuyetHuy(cmbApprover);
        if (cmbApprover.getItemCount() == 0) {
            JOptionPane.showMessageDialog(parent,
                    "Không có quản lý hoặc quản trị viên đang làm việc để chọn duyệt hủy.",
                    "Không có người duyệt", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        cmbApprover.insertItemAt(new MucNguoiDuyet("", "— Chọn người duyệt hủy —"), 0);
        cmbApprover.setSelectedIndex(0);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 4, 4, 4);
        c.gridy = 0;
        form.add(new JLabel("Lý do hủy phiếu:"), c);
        c.gridy = 1;
        form.add(new JScrollPane(txtReason), c);
        c.gridy = 2;
        form.add(new JLabel("Người duyệt hủy (quản lý/quản trị viên đang làm):"), c);
        c.gridy = 3;
        form.add(cmbApprover, c);

        while (true) {
            int result = JOptionPane.showConfirmDialog(parent, form, "Hủy phiếu " + repairId,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result != JOptionPane.OK_OPTION)
                return null;
            String reason = txtReason.getText().trim();
            String approver = maDangChon(cmbApprover);
            if (reason.isBlank()) {
                JOptionPane.showMessageDialog(parent, "Vui lòng nhập lý do hủy phiếu.", "Thiếu lý do hủy",
                        JOptionPane.WARNING_MESSAGE);
                continue;
            }
            if (approver == null || approver.isBlank()) {
                JOptionPane.showMessageDialog(parent, "Vui lòng chọn người duyệt hủy.",
                        "Chưa chọn người duyệt", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            return new CancellationDetails(reason, approver);
        }
    }

    // ===== PHƯƠNG THỨC: choose(Component, String, String, String) — Hiển thị lựa chọn và trả về mục được chọn. =====
    static String choose(Component parent, String owner, String field, String title) {
        String selected = chooseOptional(parent, owner, field, title);
        if (selected != null && selected.isBlank()) {
            JOptionPane.showMessageDialog(parent, "Vui lòng chọn " + nhanCot(field).toLowerCase() + ".");
            return null;
        }
        return selected;
    }

    // ===== PHƯƠNG THỨC: chooseOptional(Component, String, String, String) — Cho phép chọn hoặc bỏ qua mục tham chiếu. =====
    private static String chooseOptional(Component parent, String owner, String field, String title) {
        // Với NhanVien, dùng một bảng khác để trường này được nhận diện là khóa ngoại.
        OChonThamChieu cmbValue = OChonThamChieu.create(owner.equals("NhanVien") ? "PhieuTiepNhan" : owner,
                field);
        if (cmbValue == null)
            return null;
        JDialog winChoose = new JDialog(SwingUtilities.getWindowAncestor(parent), title,
                Dialog.ModalityType.APPLICATION_MODAL);
        JPanel pnlChoose = new JPanel(new BorderLayout(8, 10));
        pnlChoose.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        pnlChoose.add(new JLabel("Bấm chọn một kết quả để xác nhận ngay."), BorderLayout.NORTH);
        pnlChoose.add(cmbValue, BorderLayout.CENTER);
        JButton btnCancel = GiaoDien.button("Hủy", GiaoDien.CREAM, GiaoDien.NAV);
        btnCancel.addActionListener(e -> winChoose.dispose());
        pnlChoose.add(btnCancel, BorderLayout.SOUTH);
        String[] result = { null };
        cmbValue.addActionListener(e -> {
            if (cmbValue.getSelectedItem() != null && !cmbValue.selectedId().isBlank()) {
                result[0] = cmbValue.selectedId();
                winChoose.dispose();
            }
        });
        winChoose.setContentPane(pnlChoose);
        winChoose.setMinimumSize(new Dimension(440, 160));
        winChoose.pack();
        winChoose.setLocationRelativeTo(null);
        winChoose.setVisible(true);
        return result[0];
    }

    private static final class GiaoDien {

        private static final Color NAV = new Color(0x13272E);
        private static final Color TEAL = new Color(0x487674);
        private static final Color CREAM = new Color(0xE8C69B);
        private static final Color ORANGE = new Color(0xCB6330);

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

    private record MucNguoiDuyet(String id, String label) implements java.io.Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 1L;

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return label;
    }

    }

    // ===== PHƯƠNG THỨC: napNguoiDuyetHuy(JComboBox<MucNguoiDuyet>) — Tải thông tin nhân viên duyệt thao tác hủy. =====
    private static void napNguoiDuyetHuy(JComboBox<MucNguoiDuyet> combo) throws SQLException {
        combo.removeAllItems();
        for (var entry : new dao.NhanVienDAO().layNguoiDuyetHuyPhieu().entrySet()) {
            combo.addItem(new MucNguoiDuyet(entry.getKey(), entry.getValue()));
        }
    }

    // ===== PHƯƠNG THỨC: maDangChon(JComboBox<MucNguoiDuyet>) — Lấy mã của mục đang được chọn. =====
    private static String maDangChon(JComboBox<MucNguoiDuyet> combo) {
        MucNguoiDuyet selected = (MucNguoiDuyet) combo.getSelectedItem();
        return selected == null ? null : selected.id();
    }
}
