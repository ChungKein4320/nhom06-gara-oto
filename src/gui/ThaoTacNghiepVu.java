package gui;

import connectDB.KetNoiCSDL;
import dao.CoSoDAO;
import dao.QuyTrinhDAO;
import dao.Schema;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.*;

/** Các thao tác gắn với phiếu đang chọn, không nhập tay trạng thái nghiệp vụ. */
final class ThaoTacNghiepVu {
    private ThaoTacNghiepVu() {}

    static void install(JPanel pnlButtons, String table, Supplier<Map<String,Object>> selection, Runnable refresh, Component parent) {
        install(pnlButtons, table, selection, refresh, parent, null, null);
    }

    static void install(JPanel pnlButtons, String table, Supplier<Map<String,Object>> selection, Runnable refresh, Component parent, VaiTro role, String employeeId) {
        install(pnlButtons, table, null, selection, refresh, parent, role, employeeId, null);
    }

    static void install(JPanel pnlButtons, String table, String page, Supplier<Map<String,Object>> selection, Runnable refresh, Component parent, VaiTro role, String employeeId, JTable tblTable) {
        String[] actions = switch(table) {
            case "PhieuTiepNhan" -> new String[]{"Lập phiếu kiểm tra"};
            case "PhieuKiemTra" -> new String[]{"Lập phiếu báo giá"};
            case "PhieuBaoGia" -> new String[]{"Xác nhận báo giá", "Lập phiếu sửa chữa"};
            case "PhieuSuaChua" -> new String[]{"Hủy", "Bàn giao"};
            case "YeuCauNhapKho" -> getYeuCauNhapKhoActions(page, role);
            case "PhieuNhapKho" -> getPhieuNhapKhoActions(page, role);
            case "LichNhacBaoDuong" -> new String[]{"Ghi nhận đã gửi"};
            default -> new String[0];
        };
        if (actions.length == 0) return;
        boolean direct = Set.of("PhieuTiepNhan", "PhieuKiemTra", "PhieuBaoGia", "PhieuSuaChua", "PhieuNhapKho", "YeuCauNhapKho").contains(table);
        if (direct) {
            for (String action : actions) installButton(pnlButtons, table, selection, refresh, parent, action, null, role, employeeId, tblTable);
        } else {
            JComboBox<String> cmbAction = new OChonTimKiem<>(actions);
            pnlButtons.add(cmbAction);
            installButton(pnlButtons, table, selection, refresh, parent, null, cmbAction, role, employeeId, tblTable);
        }
    }

    private static String[] getYeuCauNhapKhoActions(String page, VaiTro role) {
        if ("Danh sách yêu cầu nhập".equals(page)) {
            return new String[]{"Duyệt yêu cầu", "Từ chối yêu cầu"};
        }
        if ("Phiếu yêu cầu nhập kho".equals(page) || "Yêu cầu nhập kho".equals(page)) {
            return new String[]{"Lập phiếu nhập kho"};
        }
        if (role == VaiTro.QUAN_LY) {
            return new String[]{"Duyệt yêu cầu", "Từ chối yêu cầu"};
        }
        if (role == VaiTro.KHO) {
            return new String[]{"Lập phiếu nhập kho"};
        }
        if (role == VaiTro.ADMIN) {
            return new String[]{"Duyệt yêu cầu", "Từ chối yêu cầu", "Lập phiếu nhập kho"};
        }
        return new String[0];
    }

    private static String[] getPhieuNhapKhoActions(String page, VaiTro role) {
        if (role == VaiTro.QUAN_LY) {
            return new String[0];
        }
        if (role == VaiTro.KHO || role == VaiTro.ADMIN) {
            return new String[]{"Xác nhận nhập kho"};
        }
        return new String[0];
    }

    private static void installButton(JPanel pnlButtons, String table, Supplier<Map<String,Object>> selection, Runnable refresh, Component parent, String fixedAction, JComboBox<String> cmbAction, VaiTro role, String employeeId, JTable tblTable) {
        JButton btnApply = GiaoDien.button(
                fixedAction == null ? "Thực hiện" : fixedAction,
                "Từ chối yêu cầu".equals(fixedAction) ? GiaoDien.ORANGE : GiaoDien.TEAL,
                Color.WHITE
        );
        pnlButtons.add(btnApply);

        if (tblTable != null && fixedAction != null) {
            if ("Duyệt yêu cầu".equals(fixedAction) || "Từ chối yêu cầu".equals(fixedAction)) {
                btnApply.setVisible(false);
                tblTable.getSelectionModel().addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        Map<String,Object> row = selection.get();
                        boolean isPending = row != null && "Chờ duyệt".equals(row.get("trangThai"));
                        btnApply.setVisible(isPending);
                        btnApply.setEnabled(isPending);
                        pnlButtons.revalidate();
                        pnlButtons.repaint();
                    }
                });
            } else if ("Lập phiếu nhập kho".equals(fixedAction)) {
                btnApply.setVisible(false);
                tblTable.getSelectionModel().addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        Map<String,Object> row = selection.get();
                        boolean isApproved = row != null && "Đã duyệt".equals(row.get("trangThai"));
                        btnApply.setVisible(isApproved);
                        btnApply.setEnabled(isApproved);
                        pnlButtons.revalidate();
                        pnlButtons.repaint();
                    }
                });
            } else if ("Xác nhận nhập kho".equals(fixedAction)) {
                btnApply.setVisible(false);
                tblTable.getSelectionModel().addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        Map<String,Object> row = selection.get();
                        Object st = row != null ? row.get("trangThai") : null;
                        boolean isDraft = row != null && "Chờ xác nhận nhập kho".equals(st);
                        btnApply.setVisible(isDraft);
                        btnApply.setEnabled(isDraft);
                        pnlButtons.revalidate();
                        pnlButtons.repaint();
                    }
                });
            }
        }
        btnApply.addActionListener(e -> {
            Map<String,Object> row = selection.get();
            if (row == null) {
                JOptionPane.showMessageDialog(parent, "Vui lòng chọn một bản ghi trong bảng trước khi thực hiện thao tác.", "Chưa chọn bản ghi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String action = fixedAction != null ? fixedAction : (String) cmbAction.getSelectedItem();
            if (action == null) {
                JOptionPane.showMessageDialog(parent, "Vui lòng chọn một nghiệp vụ trong danh sách.");
                return;
            }
            String id = String.valueOf(row.get(Schema.PRIMARY.get(table)));
            String message = action + " cho " + id + "?";
            if (action.equals("Nghiệm thu đạt")) message = "Xác nhận TẤT CẢ hạng mục đã hoàn thành và nghiệm thu Đạt cho " + id + "?";
            if (action.equals("Ghi nhận đã gửi")) message = "Bạn đã gửi thông báo cho khách và muốn ghi nhận lịch này là Đã gửi?";
            if (action.equals("Hủy")) message = "Bạn đã thu hồi ĐẦY ĐỦ phụ tùng đã cấp về kho và muốn hủy " + id + "? Tồn kho sẽ được hoàn lại.";
            if (action.equals("Duyệt yêu cầu")) message = "Phê duyệt yêu cầu nhập kho " + id + "? Yêu cầu sẽ chuyển sang trạng thái Đã duyệt để đặt hàng.";
            if (action.equals("Từ chối yêu cầu")) message = "Từ chối yêu cầu nhập kho " + id + "?";
            if (action.equals("Lập phiếu nhập kho")) message = "Tiếp nhận hàng thực tế và lập phiếu nhập kho cho yêu cầu " + id + "?";
            if (action.equals("Xác nhận nhập kho")) message = "Xác nhận hàng thực tế đã nhập kho cho " + id + "? Tồn kho sẽ được tăng theo số lượng chấp nhận.";

            if (JOptionPane.showConfirmDialog(parent, message, "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            btnApply.setEnabled(false);
            try {
                switch (action) {
                    case "Lập phiếu kiểm tra" -> {
                        String employee = choose(parent, "NhanVien", "maNhanVien", "Nhân viên kiểm tra");
                        if (employee == null) return;
                        Map<String,Object> seed = new LinkedHashMap<>();
                        seed.put("maPTN", id);
                        seed.put("VIN", row.get("VIN"));
                        seed.put("maKhachHang", row.get("maKhachHang"));
                        seed.put("maNhanVien", employee);
                        seed.put("ngayKiemTra", new Timestamp(System.currentTimeMillis()));
                        TrinhSuaPhieu.show(parent, "PhieuKiemTra", seed, role, employeeId);
                    }
                    case "Lập phiếu báo giá" -> {
                        String employee = choose(parent, "NhanVien", "maNhanVien", "Nhân viên lập báo giá");
                        if (employee == null) return;
                        Map<String,Object> seed = new LinkedHashMap<>();
                        seed.put("maPKT", id);
                        seed.put("VIN", row.get("VIN"));
                        seed.put("maNhanVien", employee);
                        seed.put("ngayLap", new Timestamp(System.currentTimeMillis()));
                        seed.put("trangThai", "Còn hiệu lực");
                        TrinhSuaPhieu.show(parent, "PhieuBaoGia", seed, role, employeeId);
                    }
                    case "Xác nhận báo giá" -> QuyTrinhDAO.confirmQuote(id);
                    case "Lập phiếu sửa chữa" -> {
                        String employee = choose(parent, "NhanVien", "maNhanVien", "Nhân viên phụ trách sửa chữa");
                        if (employee == null) return;
                        String created = QuyTrinhDAO.createRepair(id, employee);
                        JOptionPane.showMessageDialog(parent, "Đã tạo " + created + ". Cấp phát phụ tùng trước khi bắt đầu sửa chữa.");
                    }
                    case "Bắt đầu sửa chữa" -> QuyTrinhDAO.startRepair(id);
                    case "Nghiệm thu đạt" -> QuyTrinhDAO.acceptRepair(id);
                    case "Duyệt yêu cầu" -> {
                        if (role != gui.VaiTro.QUAN_LY && role != gui.VaiTro.ADMIN) {
                            JOptionPane.showMessageDialog(parent,
                                    "Chỉ Quản lý hoặc Quản trị viên mới có quyền duyệt yêu cầu nhập kho.",
                                    "Phân quyền", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        QuyTrinhDAO.approvePurchaseRequest(id, true, role, employeeId);
                        JOptionPane.showMessageDialog(parent, "Đã duyệt yêu cầu nhập kho " + id + ".");
                    }
                    case "Từ chối yêu cầu" -> {
                        if (role != gui.VaiTro.QUAN_LY && role != gui.VaiTro.ADMIN) {
                            JOptionPane.showMessageDialog(parent,
                                    "Chỉ Quản lý hoặc Quản trị viên mới có quyền từ chối yêu cầu nhập kho.",
                                    "Phân quyền", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        QuyTrinhDAO.approvePurchaseRequest(id, false, role, employeeId);
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
                                    "Chỉ có thể lập phiếu nhập kho cho yêu cầu ở trạng thái 'Đã duyệt'.\nTrạng thái hiện tại: " + state,
                                    "Không thể lập phiếu", JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        // Kiểm tra xem yêu cầu này đã có phiếu nhập kho nào chưa (1 yêu cầu chỉ có tối đa 1 phiếu)
                        try (Connection conn = KetNoiCSDL.getConnection();
                             PreparedStatement ps = conn.prepareStatement("SELECT maPNK, trangThai FROM dbo.PhieuNhapKho WHERE maYCNK=?")) {
                            ps.setString(1, id);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    String existingPnk = rs.getString("maPNK");
                                    String pnkState = rs.getString("trangThai");
                                    if ("Chờ xác nhận nhập kho".equals(pnkState)) {
                                        JOptionPane.showMessageDialog(parent,
                                                "Yêu cầu nhập kho này đã có phiếu nhập kho nháp (" + existingPnk + ").\n"
                                                + "Hệ thống sẽ mở phiếu nháp hiện tại để bạn tiếp tục chỉnh sửa.",
                                                "Mở phiếu nháp hiện có", JOptionPane.INFORMATION_MESSAGE);
                                        List<LinkedHashMap<String,Object>> pnkFresh = new CoSoDAO("PhieuNhapKho").getAll(conn, Map.of("maPNK", existingPnk));
                                        if (!pnkFresh.isEmpty()) {
                                            TrinhSuaPhieu.show(parent, "PhieuNhapKho", pnkFresh.get(0), role, employeeId);
                                            return;
                                        }
                                    } else {
                                        JOptionPane.showMessageDialog(parent,
                                                "Yêu cầu nhập kho " + id + " đã được xử lý bởi phiếu nhập kho " + existingPnk
                                                + " (trạng thái: '" + pnkState + "').\nKhông thể tạo thêm phiếu nhập kho mới.",
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

                        Map<String,Object> header = new LinkedHashMap<>();
                        header.put("maYCNK", id);
                        header.put("maNCC", row.get("maNCC"));
                        header.put("maNhanVien", emp);
                        header.put("ngayNhap", new Timestamp(System.currentTimeMillis() / 1000 * 1000));
                        header.put("trangThai", "Chờ xác nhận nhập kho");

                        List<LinkedHashMap<String,Object>> reqLines;
                        try (Connection conn = KetNoiCSDL.getConnection()) {
                            reqLines = new CoSoDAO("ChiTietYeuCauNhapKho").getAll(conn, Map.of("maYCNK", id));
                        }
                        if (reqLines.isEmpty()) {
                            JOptionPane.showMessageDialog(parent, "Yêu cầu nhập kho này không có hạng mục chi tiết phụ tùng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        List<Map<String,Object>> initialDetails = new ArrayList<>();
                        for (Map<String,Object> line : reqLines) {
                            Map<String,Object> detail = new LinkedHashMap<>();
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
                                    "Chỉ có thể xác nhận nhập kho cho phiếu ở trạng thái 'Chờ xác nhận nhập kho'.\nTrạng thái hiện tại: '" + pnkState + "'.",
                                    "Phiếu đã xử lý", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        Object ycnId = row.get("maYCNK");
                        if (ycnId == null || String.valueOf(ycnId).isBlank()) {
                            JOptionPane.showMessageDialog(parent,
                                    "Phiếu nhập kho này là phiếu lịch sử (không gắn Yêu cầu nhập kho).\n"
                                    + "Không thuộc quy trình xác nhận nhập kho thực tế UC009.",
                                    "Bản ghi lịch sử", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        if (employeeId == null || employeeId.isBlank()) {
                            JOptionPane.showMessageDialog(parent,
                                    "Không tìm thấy thông tin nhân viên xác nhận trong phiên làm việc. Vui lòng đăng nhập lại.",
                                    "Lỗi danh tính", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        QuyTrinhDAO.confirmActualStockIn(id, role, employeeId);
                        JOptionPane.showMessageDialog(parent, "Đã xác nhận nhập kho thực tế thành công cho " + id + ".\nTồn kho phụ tùng đã được cập nhật.");
                    }
                    case "Ghi nhận đã gửi" -> QuyTrinhDAO.markReminder(id);
                    case "Bàn giao" -> QuyTrinhDAO.handover(id);
                    case "Hủy" -> QuyTrinhDAO.cancelRepair(id);
                    default -> { }
                }
                refresh.run();
            } catch (SQLException | SecurityException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Không thể thực hiện", JOptionPane.ERROR_MESSAGE);
            } finally {
                btnApply.setEnabled(true);
            }
        });
    }

    private static String choose(Component parent, String owner, String field, String title) {
        String selected = chooseOptional(parent, owner, field, title);
        if (selected != null && selected.isBlank()) {
            JOptionPane.showMessageDialog(parent, "Vui lòng chọn " + NhanCot.forField(field).toLowerCase() + ".");
            return null;
        }
        return selected;
    }

    private static String chooseOptional(Component parent, String owner, String field, String title) {
        OChonThamChieu cmbValue = OChonThamChieu.create(owner.equals("NhanVien") ? "PhieuTiepNhan" : owner, field);
        if (cmbValue == null) return null;
        JDialog winChoose = new JDialog(SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        JPanel pnlChoose = new JPanel(new BorderLayout(8, 10));
        pnlChoose.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        pnlChoose.add(new JLabel("Bấm chọn một kết quả để xác nhận ngay."), BorderLayout.NORTH);
        pnlChoose.add(cmbValue, BorderLayout.CENTER);
        JButton btnCancel = GiaoDien.button("Hủy", GiaoDien.CREAM, GiaoDien.NAV);
        btnCancel.addActionListener(e -> winChoose.dispose());
        pnlChoose.add(btnCancel, BorderLayout.SOUTH);
        String[] result = {null};
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
}
