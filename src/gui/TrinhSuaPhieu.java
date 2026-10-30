package gui;

import connectDB.KetNoiCSDL;
import dao.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

/** Nhập đầu phiếu và nhiều hạng mục, chỉ ghi khi nhấn Lưu toàn bộ phiếu. */
final class TrinhSuaPhieu {
    private TrinhSuaPhieu() {}
    static void show(Component parent,String table,Map<String,Object> header) {
        show(parent,table,header,List.of(),null,null);
    }
    static void show(Component parent,String table,Map<String,Object> header,List<Map<String,Object>> initialDetails) {
        open(parent,table,header,initialDetails,false,null,null);
    }
    static void show(Component parent,String table,Map<String,Object> header,VaiTro role,String employeeId) {
        show(parent,table,header,List.of(),role,employeeId);
    }
    static void show(Component parent,String table,Map<String,Object> header,List<Map<String,Object>> initialDetails,VaiTro role,String employeeId) {
        open(parent,table,header,initialDetails,false,role,employeeId);
    }
    static void view(Component parent,String table,Map<String,Object> header) {
        open(parent,table,header,List.of(),true,null,null);
    }
    static void review(Component parent, String table, Map<String, Object> header, VaiTro role, String employeeId) {
        openReview(parent, table, header, role, employeeId);
    }
    private static Window getWindowOwner(Component parent) {
        if (parent == null) return null;
        if (parent instanceof Window) return (Window) parent;
        return SwingUtilities.getWindowAncestor(parent);
    }

    private static void openReview(Component parent, String table, Map<String, Object> header, VaiTro role, String employeeId) {
        String child = QuyTrinhDAO.detailTable(table), pk = Schema.PRIMARY.get(table);
        String id = header == null || header.get(pk) == null ? null : String.valueOf(header.get(pk));
        if (id == null) {
            JOptionPane.showMessageDialog(parent, "Không tìm thấy mã phiếu cần xét duyệt.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Map<String, Object>> details = new ArrayList<>();
        Map<String, Object> freshHeader = header;
        try (Connection conn = KetNoiCSDL.getConnection()) {
            var fresh = new CoSoDAO(table).getAll(conn, Map.of(pk, id));
            if (fresh.isEmpty()) throw new SQLException("Phiếu không còn tồn tại. Vui lòng tải lại danh sách.");
            freshHeader = fresh.get(0);
            details.addAll(new CoSoDAO(child).getAll(conn, Map.of(pk, id)));
        } catch (SQLException ex) {
            error(parent, ex);
            return;
        }

        String status = String.valueOf(freshHeader.get("trangThai"));
        boolean isPending = "Chờ duyệt".equals(status);
        boolean canDecide = isPending && (role == VaiTro.QUAN_LY || role == VaiTro.ADMIN);

        BieuMauBanGhi pnlHeader = new BieuMauBanGhi(table, freshHeader, Set.of(), true, true);

        String[] columns = Arrays.stream(Schema.COLUMNS.get(child))
                .filter(f -> !f.equals(pk) && !f.equals("maChiTietHoaDon"))
                .toArray(String[]::new);
        DefaultTableModel model = new DefaultTableModel(NhanCot.forFields(columns), 0) {
            @java.io.Serial private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblDetails = GiaoDien.table(model);
        tblDetails.setAutoCreateRowSorter(true);
        for (Map<String, Object> row : details) {
            model.addRow(Arrays.stream(columns).map(row::get).toArray());
        }

        String title = (canDecide ? "Xét duyệt yêu cầu nhập kho " : "Chi tiết yêu cầu nhập kho ") + id + " [" + status + "]";
        JDialog winDialog = new JDialog(getWindowOwner(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        JPanel pnlContent = new JPanel(new BorderLayout(0, 10));
        pnlContent.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane scrHeader = new JScrollPane(pnlHeader);
        scrHeader.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrHeader.setPreferredSize(new Dimension(840, Math.min(310, pnlHeader.getPreferredSize().height + 12)));

        JPanel pnlTop = new JPanel(new BorderLayout(0, 8));
        pnlTop.add(scrHeader, BorderLayout.CENTER);
        JLabel lblStatusInfo = GiaoDien.label("Trạng thái hiện tại: " + status + (canDecide ? " (Vui lòng thẩm định nội dung và ra quyết định)" : " (Yêu cầu đã được xử lý)"), 12, true, canDecide ? GiaoDien.NAV : GiaoDien.MUTED);
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
                        "Phê duyệt yêu cầu nhập kho " + id + "? Yêu cầu sẽ chuyển sang trạng thái Đã duyệt để đặt hàng.",
                        "Xác nhận duyệt", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
                try {
                    QuyTrinhDAO.approvePurchaseRequest(id, true, role, employeeId);
                    JOptionPane.showMessageDialog(winDialog, "Đã duyệt yêu cầu nhập kho " + id + ".");
                    winDialog.dispose();
                } catch (SQLException | SecurityException ex) {
                    JOptionPane.showMessageDialog(winDialog, ex.getMessage(), "Lỗi phê duyệt", JOptionPane.ERROR_MESSAGE);
                }
            });

            JButton btnReject = GiaoDien.button("Từ chối yêu cầu", GiaoDien.ORANGE, Color.WHITE);
            btnReject.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(winDialog,
                        "Từ chối yêu cầu nhập kho " + id + "?",
                        "Xác nhận từ chối", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
                try {
                    QuyTrinhDAO.approvePurchaseRequest(id, false, role, employeeId);
                    JOptionPane.showMessageDialog(winDialog, "Đã từ chối yêu cầu nhập kho " + id + ".");
                    winDialog.dispose();
                } catch (SQLException | SecurityException ex) {
                    JOptionPane.showMessageDialog(winDialog, ex.getMessage(), "Lỗi từ chối", JOptionPane.ERROR_MESSAGE);
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
    static JDialog buildDialog(Component parent, String table, Map<String, Object> header, List<Map<String, Object>> initialDetails, boolean viewOnly, VaiTro role, String employeeId) {
        String child = QuyTrinhDAO.detailTable(table), pk = Schema.PRIMARY.get(table);
        String id = header == null || header.get(pk) == null ? null : String.valueOf(header.get(pk));
        if (id == null && (table.equals("PhieuSuaChua") || table.equals("HoaDon") || (table.equals("PhieuNhapKho") && (header == null || header.get("maYCNK") == null)))) {
            if (!java.awt.GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(parent, table.equals("PhieuNhapKho")
                        ? "Phiếu nhập kho phải được lập từ Yêu cầu nhập kho đã duyệt. Vui lòng chọn yêu cầu đã duyệt và bấm 'Lập phiếu nhập kho'."
                        : "Hãy lập phiếu bằng nút nghiệp vụ của phiếu trước đó.");
            }
            return null;
        }
        String lockedReason = null;
        List<Map<String, Object>> details = new ArrayList<>();
        if (id == null && initialDetails != null) {
            for (Map<String, Object> line : initialDetails) details.add(new LinkedHashMap<>(line));
        }
        try (Connection conn = KetNoiCSDL.getConnection()) {
            if (id != null) {
                var fresh = new CoSoDAO(table).getAll(conn, Map.of(pk, id));
                if (fresh.isEmpty()) throw new SQLException("Phiếu không còn tồn tại. Vui lòng tải lại danh sách.");
                header = fresh.get(0);
                if (!viewOnly) lockedReason = QuyTrinhDAO.documentEditProblem(conn, table, id);
                details.addAll(new CoSoDAO(child).getAll(conn, Map.of(pk, id)));
            }
        } catch (SQLException ex) {
            error(parent, ex);
            return null;
        }
        boolean readOnly = viewOnly || lockedReason != null;
        BieuMauBanGhi pnlHeader = new BieuMauBanGhi(table, header, id == null ? Set.of(pk) : Set.of(), readOnly, id != null);
        if (!readOnly && id != null) pnlHeader.lockField(pk);
        if (!readOnly && table.equals("PhieuNhapKho")) {
            pnlHeader.lockField("maYCNK");
            pnlHeader.lockField("maNCC");
            pnlHeader.lockField("trangThai");
            pnlHeader.lockField("ngayNhap");
            if (id != null) pnlHeader.lockField("maNhanVien");
        }
        if (!readOnly && table.equals("YeuCauNhapKho")) {
            pnlHeader.lockField("maNhanVien");
            pnlHeader.lockField("trangThai");
            pnlHeader.lockField("maNhanVienDuyet");
            pnlHeader.lockField("ngayDuyet");
        }
        String[] columns = Arrays.stream(Schema.COLUMNS.get(child)).filter(f -> !f.equals(pk) && !f.equals("maChiTietHoaDon")).toArray(String[]::new);
        boolean isPnk = table.equals("PhieuNhapKho");

        DefaultTableModel model = new DefaultTableModel(NhanCot.forFields(columns), 0) {
            @java.io.Serial private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int r, int c) {
                if (readOnly) return false;
                if (isPnk) {
                    String col = columns[c];
                    return "soLuongThucNhan".equals(col) || "soLuongNhap".equals(col) || "donGiaNhap".equals(col);
                }
                return false;
            }
            @Override public void setValueAt(Object aValue, int r, int c) {
                super.setValueAt(aValue, r, c);
                if (isPnk && r < details.size()) {
                    String col = columns[c];
                    details.get(r).put(col, aValue);
                }
            }
        };

        JTextField tfEditor = new JTextField();
        tfEditor.setFont(GiaoDien.font(12, false));
        tfEditor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GiaoDien.TEAL, 1, true),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        tfEditor.setHorizontalAlignment(SwingConstants.RIGHT);

        JTable tblDetails = new JTable(model) {
            @java.io.Serial private static final long serialVersionUID = 1L;
            private final TableCellRenderer cells = new DinhDangBang();
            @Override public TableCellRenderer getCellRenderer(int row, int column) { return cells; }
            @Override public TableCellEditor getCellEditor(int row, int column) {
                if (isPnk && !readOnly) {
                    int modelCol = convertColumnIndexToModel(column);
                    String col = columns[modelCol];
                    if ("soLuongThucNhan".equals(col) || "soLuongNhap".equals(col) || "donGiaNhap".equals(col)) {
                        TableCellEditor ed = getColumnModel().getColumn(column).getCellEditor();
                        if (ed != null) return ed;
                    }
                }
                return super.getCellEditor(row, column);
            }
            @Override public void createDefaultColumnsFromModel() {
                super.createDefaultColumnsFromModel();
                for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
                    TableColumn column = getColumnModel().getColumn(i);
                    column.setHeaderValue(NhanCot.forField(getModel().getColumnName(column.getModelIndex())));
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

        PnkCellEditor pnkEditor = new PnkCellEditor(tfEditor, tblDetails, model, details, columns, parent);
        if (isPnk && !readOnly) {
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

        JDialog winDialog = new JDialog(getWindowOwner(parent),
                (id == null ? "Lập phiếu mới" : readOnly ? "Chi tiết " + id : "Chỉnh sửa " + id),
                Dialog.ModalityType.APPLICATION_MODAL);
        JPanel pnlContent = new JPanel(new BorderLayout(0, 10));
        pnlContent.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JScrollPane scrHeader = new JScrollPane(pnlHeader);
        scrHeader.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrHeader.setPreferredSize(new Dimension(840, Math.min(310, pnlHeader.getPreferredSize().height + 12)));
        JPanel pnlTop = new JPanel(new BorderLayout(0, 8));
        pnlTop.add(scrHeader, BorderLayout.CENTER);
        if (lockedReason != null) {
            pnlTop.add(GiaoDien.label("<html>Chỉ xem: " + lockedReason + "</html>", 12, true, DinhDangKeThua.TEXT), BorderLayout.NORTH);
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
        JButton btnSave = GiaoDien.button(id == null ? "Lưu toàn bộ phiếu" : "Lưu thay đổi", GiaoDien.TEAL, Color.WHITE);
        JButton btnClose = GiaoDien.button("Đóng", GiaoDien.CREAM, GiaoDien.NAV);

        if (!isPnk && !table.equals("YeuCauNhapKho")) {
            JButton btnView = GiaoDien.button("Xem hạng mục", GiaoDien.CREAM, GiaoDien.NAV);
            btnView.addActionListener(e -> {
                int selected = tblDetails.getSelectedRow();
                if (selected < 0) {
                    if (!java.awt.GraphicsEnvironment.isHeadless()) JOptionPane.showMessageDialog(winDialog, "Vui lòng chọn hạng mục.");
                    return;
                }
                BieuMauBanGhi pnlLine = new BieuMauBanGhi(child, details.get(tblDetails.convertRowIndexToModel(selected)), Set.of(), true);
                JScrollPane scrLine = new JScrollPane(pnlLine);
                scrLine.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                scrLine.setPreferredSize(new Dimension(660, Math.min(380, pnlLine.getPreferredSize().height + 12)));
                if (!java.awt.GraphicsEnvironment.isHeadless()) JOptionPane.showMessageDialog(winDialog, scrLine, "Chi tiết hạng mục", JOptionPane.PLAIN_MESSAGE);
            });
            pnlButtons.add(btnView);
        }

        if (!isPnk) {
            tblDetails.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent event) {
                    if (event.getClickCount() == 2 && !readOnly) btnEdit.doClick();
                }
            });
        }

        btnAdd.addActionListener(e -> {
            Map<String, Object> row = editLine(winDialog, child, null, pk);
            if (row != null) { details.add(row); refresh.run(); }
        });
        btnEdit.addActionListener(e -> {
            int selected = tblDetails.getSelectedRow();
            if (selected < 0) {
                if (!java.awt.GraphicsEnvironment.isHeadless()) JOptionPane.showMessageDialog(winDialog, "Vui lòng chọn hạng mục cần sửa.");
                return;
            }
            int index = tblDetails.convertRowIndexToModel(selected);
            Map<String, Object> row = editLine(winDialog, child, details.get(index), pk);
            if (row != null) { details.set(index, row); refresh.run(); }
        });
        btnRemove.addActionListener(e -> {
            int selected = tblDetails.getSelectedRow();
            if (selected >= 0) { details.remove(tblDetails.convertRowIndexToModel(selected)); refresh.run(); }
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

                if (isPnk) {
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
                            throw new IllegalArgumentException("Dòng " + (r + 1) + " (" + part + "): Vui lòng nhập số lượng thực nhận.");
                        }
                        if (valAccepted == null || valAccepted.toString().trim().isEmpty()) {
                            throw new IllegalArgumentException("Dòng " + (r + 1) + " (" + part + "): Vui lòng nhập số lượng nhập chấp nhận.");
                        }

                        int actual;
                        int accepted;
                        try {
                            actual = Integer.parseInt(valActual.toString().trim().replace(" ", ""));
                        } catch (NumberFormatException nfe) {
                            throw new IllegalArgumentException("Dòng " + (r + 1) + " (" + part + "): Số lượng thực nhận phải là số nguyên.");
                        }
                        try {
                            accepted = Integer.parseInt(valAccepted.toString().trim().replace(" ", ""));
                        } catch (NumberFormatException nfe) {
                            throw new IllegalArgumentException("Dòng " + (r + 1) + " (" + part + "): Số lượng nhập chấp nhận phải là số nguyên.");
                        }

                        if (actual < 0) {
                            throw new IllegalArgumentException("Dòng " + (r + 1) + " (" + part + "): Số lượng thực nhận không được âm.");
                        }
                        if (accepted < 0) {
                            throw new IllegalArgumentException("Dòng " + (r + 1) + " (" + part + "): Số lượng nhập không được âm.");
                        }
                        if (actual > req) {
                            throw new IllegalArgumentException("Dòng " + (r + 1) + " (" + part + "): Số lượng thực nhận (" + actual + ") không được vượt quá số lượng yêu cầu (" + req + ").");
                        }
                        if (accepted > actual) {
                            throw new IllegalArgumentException("Dòng " + (r + 1) + " (" + part + "): Số lượng nhập chấp nhận (" + accepted + ") không được vượt quá số lượng thực nhận (" + actual + ").");
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
                                throw new IllegalArgumentException("Dòng " + (r + 1) + " (" + part + "): Đơn giá nhập không hợp lệ.");
                            }
                            if (price.compareTo(java.math.BigDecimal.ZERO) < 0) {
                                throw new IllegalArgumentException("Dòng " + (r + 1) + " (" + part + "): Đơn giá nhập không được âm.");
                            }
                        }

                        detail.put("soLuongThucNhan", actual);
                        detail.put("soLuongNhap", accepted);
                        detail.put("donGiaNhap", price);
                    }
                }

                String savedId = QuyTrinhDAO.saveDocument(table, pnlHeader.read(), details, id, role, employeeId);
                String docName = isPnk ? "phiếu nhập kho " + savedId
                        : table.equals("YeuCauNhapKho") ? "yêu cầu nhập kho " + savedId
                        : "phiếu " + savedId;
                if (!java.awt.GraphicsEnvironment.isHeadless() && !Boolean.getBoolean("test.suppressDialogs")) {
                    JOptionPane.showMessageDialog(winDialog, "Đã lưu " + docName + " thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                }
                winDialog.dispose();
            } catch (Exception ex) {
                if (!java.awt.GraphicsEnvironment.isHeadless() && !Boolean.getBoolean("test.suppressDialogs")) {
                    JOptionPane.showMessageDialog(winDialog, ex.getMessage(), "Không thể lưu phiếu", JOptionPane.ERROR_MESSAGE);
                } else {
                    System.err.println("[SAVE ERROR] " + ex.getMessage());
                }
            } finally {
                btnSave.setEnabled(true);
            }
        });
        btnClose.addActionListener(e -> winDialog.dispose());

        if (!readOnly) {
            if (!isPnk) {
                pnlButtons.add(btnAdd);
                pnlButtons.add(btnRemove);
                pnlButtons.add(btnEdit);
            }
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

    private static void open(Component parent, String table, Map<String, Object> header, List<Map<String, Object>> initialDetails, boolean viewOnly, VaiTro role, String employeeId) {
        JDialog winDialog = buildDialog(parent, table, header, initialDetails, viewOnly, role, employeeId);
        if (winDialog != null) {
            winDialog.setVisible(true);
        }
    }

    private static class PnkCellEditor extends DefaultCellEditor {
        private static final long serialVersionUID = 1L;
        private final JTextField textField;
        private final JTable table;
        private final DefaultTableModel model;
        private final List<Map<String, Object>> details;
        private final String[] columns;
        private final Component parentComponent;
        private boolean validating = false;
        private Object parsedValue = null;

        PnkCellEditor(JTextField tf, JTable table, DefaultTableModel model, List<Map<String, Object>> details, String[] columns, Component parentComponent) {
            super(tf);
            this.textField = tf;
            this.table = table;
            this.model = model;
            this.details = details;
            this.columns = columns;
            this.parentComponent = parentComponent;
            setClickCountToStart(2);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
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

        @Override
        public Object getCellEditorValue() {
            return parsedValue != null ? parsedValue : textField.getText().trim();
        }

        @Override
        public boolean stopCellEditing() {
            if (validating) return false;
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
                if ("soLuongThucNhan".equals(colName)) {
                    if (text.isEmpty()) {
                        showError("Dòng " + (modelRow + 1) + " (" + part + "): Vui lòng nhập số lượng thực nhận.");
                        return false;
                    }
                    int actual;
                    try {
                        actual = Integer.parseInt(text.replace(" ", ""));
                    } catch (NumberFormatException e) {
                        showError("Dòng " + (modelRow + 1) + " (" + part + "): Số lượng thực nhận phải là số nguyên.");
                        return false;
                    }
                    if (actual < 0) {
                        showError("Dòng " + (modelRow + 1) + " (" + part + "): Số lượng thực nhận không được âm (" + actual + ").");
                        return false;
                    }
                    if (actual > req) {
                        showError("Dòng " + (modelRow + 1) + " (" + part + "): Số lượng thực nhận (" + actual + ") không được vượt quá số lượng yêu cầu (" + req + ").");
                        return false;
                    }
                    int colAcceptedIdx = Arrays.asList(columns).indexOf("soLuongNhap");
                    Object curAcceptedObj = colAcceptedIdx >= 0 ? model.getValueAt(modelRow, colAcceptedIdx) : null;
                    if (curAcceptedObj != null) {
                        try {
                            int curAccepted = Integer.parseInt(curAcceptedObj.toString().trim().replace(" ", ""));
                            if (actual < curAccepted) {
                                showError("Dòng " + (modelRow + 1) + " (" + part + "): Số lượng thực nhận (" + actual + ") không được nhỏ hơn số lượng nhập chấp nhận (" + curAccepted + "). Vui lòng điều chỉnh số lượng nhập trước.");
                                return false;
                            }
                        } catch (Exception ignored) {}
                    }
                    parsedValue = actual;
                } else if ("soLuongNhap".equals(colName)) {
                    if (text.isEmpty()) {
                        showError("Dòng " + (modelRow + 1) + " (" + part + "): Vui lòng nhập số lượng nhập chấp nhận.");
                        return false;
                    }
                    int accepted;
                    try {
                        accepted = Integer.parseInt(text.replace(" ", ""));
                    } catch (NumberFormatException e) {
                        showError("Dòng " + (modelRow + 1) + " (" + part + "): Số lượng nhập chấp nhận phải là số nguyên.");
                        return false;
                    }
                    if (accepted < 0) {
                        showError("Dòng " + (modelRow + 1) + " (" + part + "): Số lượng nhập không được âm (" + accepted + ").");
                        return false;
                    }
                    int colActualIdx = Arrays.asList(columns).indexOf("soLuongThucNhan");
                    Object curActualObj = colActualIdx >= 0 ? model.getValueAt(modelRow, colActualIdx) : null;
                    int actual = req;
                    if (curActualObj != null) {
                        try {
                            actual = Integer.parseInt(curActualObj.toString().trim().replace(" ", ""));
                        } catch (Exception ignored) {}
                    }
                    if (accepted > actual) {
                        showError("Dòng " + (modelRow + 1) + " (" + part + "): Số lượng nhập chấp nhận (" + accepted + ") không được vượt quá số lượng thực nhận (" + actual + ").");
                        return false;
                    }
                    parsedValue = accepted;
                } else if ("donGiaNhap".equals(colName)) {
                    if (text.isEmpty()) {
                        showError("Dòng " + (modelRow + 1) + " (" + part + "): Vui lòng nhập đơn giá nhập.");
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
                        showError("Dòng " + (modelRow + 1) + " (" + part + "): Đơn giá nhập không hợp lệ.");
                        return false;
                    }
                    if (price.compareTo(java.math.BigDecimal.ZERO) < 0) {
                        showError("Dòng " + (modelRow + 1) + " (" + part + "): Đơn giá nhập không được âm.");
                        return false;
                    }
                    parsedValue = price;
                }
            } finally {
                validating = false;
            }

            return super.stopCellEditing();
        }

        private void showError(String msg) {
            Component win = parentComponent;
            if (win == null || !win.isShowing()) {
                win = SwingUtilities.getWindowAncestor(table);
            }
            if (!java.awt.GraphicsEnvironment.isHeadless() && !Boolean.getBoolean("test.suppressDialogs")) {
                JOptionPane.showMessageDialog(win != null ? win : table, msg, "Dữ liệu không hợp lệ", JOptionPane.ERROR_MESSAGE);
            } else {
                System.err.println("[VALIDATION ERROR] " + msg);
            }
        }
    }
    private static Map<String,Object> editLine(Component parent,String child,Map<String,Object> row,String foreignKey) {
        // Khóa hạng mục được nhập lại trong bản nháp; đầu phiếu sẽ gắn khi lưu.
        BieuMauBanGhi pnlForm=new BieuMauBanGhi(child,row,Set.of(foreignKey,"maChiTietHoaDon"));
        if(child.equals("ChiTietPhieuNhapKho") && row!=null && row.get("soLuongYeuCau")!=null) {
            pnlForm.lockField("maPhuTung");
            pnlForm.lockField("soLuongYeuCau");
        }
        JScrollPane scrForm=new JScrollPane(pnlForm);
        scrForm.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrForm.setPreferredSize(new Dimension(660,Math.min(380,pnlForm.getPreferredSize().height+12)));
        Object[] options={"Áp dụng vào phiếu","Hủy"};
        while(JOptionPane.showOptionDialog(parent,scrForm,"Chỉnh sửa hạng mục — lưu phiếu để ghi nhận",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE,null,options,options[0])==JOptionPane.OK_OPTION) {
            try {
                Map<String,Object> values=pnlForm.read();
                // KiemTraBieuMau bỏ khóa chính của bản ghi đang sửa; giữ lại khóa dịch vụ nếu cần.
                return values;
            } catch(SQLException ex) { error(parent,ex); }
        }
        return null;
    }
    private static void error(Component parent,SQLException ex) { JOptionPane.showMessageDialog(parent,KiemTraBieuMau.userMessage(ex,""),"Không thể lưu phiếu",JOptionPane.ERROR_MESSAGE); }
}
