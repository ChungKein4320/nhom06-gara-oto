package gui;

import dao.CoSoDAO;
import dao.QuyTrinhDAO;
import dao.Schema;
import dao.ThuongMaiDAO;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** Danh sách và biểu mẫu sử dụng dữ liệu SQL Server thực tế. */
public final class ManHinhDuLieu extends JPanel {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private static final Map<String, String> ROUTES = new LinkedHashMap<>();

    static {
        ROUTES.put("Phiếu tiếp nhận", "PhieuTiepNhan");
        ROUTES.put("Phiếu kiểm tra", "PhieuKiemTra");
        ROUTES.put("Phiếu báo giá", "PhieuBaoGia");
        ROUTES.put("Phiếu sửa chữa", "PhieuSuaChua");
        ROUTES.put("Bảo hành", "LichBaoHanh");
        ROUTES.put("Lịch nhắc bảo dưỡng", "LichNhacBaoDuong");
        ROUTES.put("Tồn kho", "TonKho");
        ROUTES.put("Phiếu cấp phát phụ tùng", "ChiTietPhieuSuaChua");
        ROUTES.put("Cấp phát phụ tùng", "ChiTietPhieuSuaChua");
        ROUTES.put("Phiếu nhập kho phụ tùng", "PhieuNhapKho");
        ROUTES.put("Nhập kho phụ tùng", "PhieuNhapKho");
        ROUTES.put("Lịch sử sửa chữa", "PhieuSuaChua");
        ROUTES.put("Hóa đơn", "HoaDon");
        ROUTES.put("Lịch sử tích điểm", "LichSuTichDiem");
        ROUTES.put("Khuyến mãi/Tích điểm", "LichSuTichDiem");
        ROUTES.put("Nhà cung cấp", "NhaCungCap");
        ROUTES.put("Nhân viên", "NhanVien");
        ROUTES.put("Khách hàng", "KhachHang");
        ROUTES.put("Phương tiện", "PhuongTien");
        ROUTES.put("Danh mục dịch vụ", "DichVu");
        ROUTES.put("Danh mục phụ tùng", "PhuTung");
        ROUTES.put("Tài khoản", "TaiKhoan");
        ROUTES.put("Khuyến mãi", "MaGiamGia");
        ROUTES.put("Phiếu yêu cầu nhập kho", "YeuCauNhapKho");
        ROUTES.put("Danh sách yêu cầu nhập", "YeuCauNhapKho");
        ROUTES.put("Yêu cầu nhập kho", "YeuCauNhapKho");
    }

    public static String tableFor(String page) {
        return ROUTES.get(page);
    }

    private final String page;
    private final String tableName;
    private final VaiTro role;
    private final String employeeId;
    private final boolean isManagerReview;
    private transient CoSoDAO dao;
    private final JTable tblTable;
    private final DefaultTableModel model;
    private final JTextField txtSearch;
    private final JLabel lblCount;
    private final String[] fields;
    private final String[] displayFields;
    private final BoLocTrangThai statusFilters;

    @java.io.Serial
    private void readObject(java.io.ObjectInputStream input)
            throws java.io.IOException, ClassNotFoundException {
        input.defaultReadObject();
        dao = tableName.equals("TonKho")
                ? new dao.TonKhoDAO()
                : tableName.equals("HoaDon")
                        ? new dao.HoaDonDAO()
                        : tableName.equals("KhachHang")
                                ? new dao.KhachHangDAO()
                                : tableName.equals("YeuCauNhapKho")
                                        ? new dao.YeuCauNhapKhoDAO()
                                        : new CoSoDAO(tableName);
    }

    public ManHinhDuLieu(String page) {
        this(page, null, null);
    }

    public ManHinhDuLieu(String page, String employeeId) {
        this(page, null, employeeId);
    }

    public ManHinhDuLieu(String page, VaiTro role, String employeeId) {
        this.page = page;
        this.role = role;
        this.employeeId = employeeId;
        setLayout(new BorderLayout(0, 18));
        setBackground(GiaoDien.BG);
        setBorder(new EmptyBorder(26, 26, 26, 26));

        tableName = ROUTES.get(page);
        if (tableName == null) {
            throw new IllegalArgumentException(
                    "Chưa có nguồn dữ liệu cho: " + page
            );
        }
        isManagerReview = "Danh sách yêu cầu nhập".equals(page)
                || ("YeuCauNhapKho".equals(tableName) && role == VaiTro.QUAN_LY);

        dao = tableName.equals("TonKho")
                ? new dao.TonKhoDAO()
                : tableName.equals("HoaDon")
                        ? new dao.HoaDonDAO()
                        : tableName.equals("KhachHang")
                                ? new dao.KhachHangDAO()
                                : tableName.equals("YeuCauNhapKho")
                                        ? new dao.YeuCauNhapKhoDAO()
                                        : new CoSoDAO(tableName);

        fields = Schema.COLUMNS.get(tableName);
        displayFields = tableName.equals("TonKho")
                ? new String[]{
                    "maTonKho", "maPhuTung", "tenPhuTung",
                    "soLuongHienTai", "soLuongToiThieu"
                }
                : tableName.equals("HoaDon")
                        ? new String[]{
                            "maHoaDon", "khachHangHienThi", "maPSC",
                            "ngayLap", "tongThanhToan",
                            "trangThaiThanhToan"
                        }
                        : tableName.equals("KhachHang")
                                ? new String[]{
                                    "maKhachHang", "hoTenKH", "soDienThoai",
                                    "gioiTinh", "email", "diaChi", "tongDiemTichLuy"
                                }
                                : fields;

        JPanel pnlHeading = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlHeading.setOpaque(false);

        JLabel lblPath = GiaoDien.label(
                "THINKING AUTO/" + page.toUpperCase(),
                11, true, GiaoDien.TEAL
        );
        JLabel lblTitle = GiaoDien.label(
                page, 26, true, GiaoDien.NAV
        );

        for (JLabel lblLabel : new JLabel[]{lblPath, lblTitle}) {
            lblLabel.setHorizontalAlignment(SwingConstants.CENTER);
            pnlHeading.add(lblLabel);
        }
        add(pnlHeading, BorderLayout.NORTH);

        JPanel pnlCard = GiaoDien.card(new BorderLayout(0, 14));
        JPanel pnlToolbar = new JPanel(new BorderLayout(12, 0));
        pnlToolbar.setOpaque(false);

        txtSearch = GiaoDien.field(
                "Tìm theo mã, tên hoặc thuộc tính..."
        );
        txtSearch.setToolTipText(
                "Nhập nội dung cần tìm trong bảng"
        );

        JPanel pnlInput = new JPanel(new BorderLayout(8, 0));
        pnlInput.setOpaque(false);

        JLabel lblSearchLabel = GiaoDien.label(
                "Tìm kiếm", 12, true, GiaoDien.TEAL
        );
        lblSearchLabel.setIcon(
                BieuTuong.get("Tìm kiếm", 16, GiaoDien.TEAL)
        );
        lblSearchLabel.setIconTextGap(6);

        pnlInput.add(lblSearchLabel, BorderLayout.WEST);
        pnlInput.add(txtSearch, BorderLayout.CENTER);
        pnlToolbar.add(pnlInput, BorderLayout.CENTER);

        JButton btnAdd = GiaoDien.button(
                "Thêm mới",
                GiaoDien.TEAL, Color.WHITE
        );
        boolean canAdd = !tableName.startsWith("ChiTiet")
                && !Set.of(
                        "PhieuSuaChua", "HoaDon", "LichSuTichDiem", "PhieuNhapKho"
                ).contains(tableName);
        if (isManagerReview) {
            canAdd = false;
        }
        btnAdd.setEnabled(canAdd);
        btnAdd.addActionListener(e -> edit(false));

        if (tableName.equals("HoaDon")) {
            JButton btnCreateInvoice = GiaoDien.button(
                    "Lập hóa đơn", GiaoDien.TEAL, Color.WHITE
            );
            btnCreateInvoice.addActionListener(e -> {
                HopThoaiLapHoaDon.show(this);
                load();
            });
            pnlToolbar.add(btnCreateInvoice, BorderLayout.EAST);
        } else if (!tableName.equals("TonKho") && !isManagerReview) {
            pnlToolbar.add(btnAdd, BorderLayout.EAST);
        }

        pnlCard.add(pnlToolbar, BorderLayout.NORTH);

        model = new DefaultTableModel(
                NhanCot.forFields(displayFields), 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tblTable = GiaoDien.table(model);
        tblTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblTable.setAutoCreateRowSorter(true);

        statusFilters = new BoLocTrangThai(tblTable);
        statusFilters.onChange(this::updateCount);
        statusFilters.setBorder(new EmptyBorder(6, 0, 0, 0));
        pnlToolbar.add(statusFilters, BorderLayout.SOUTH);

        tblTable.setRowHeight(40);
        tblTable.getTableHeader().setResizingAllowed(true);

        JScrollPane scrScroll = new JScrollPane(tblTable);
        scrScroll.setPreferredSize(new Dimension(850, 440));
        BoCucBang.install(tblTable, scrScroll);
        pnlCard.add(scrScroll, BorderLayout.CENTER);

        JPanel pnlActions = new JPanel(new BorderLayout(0, 4));
        pnlActions.setOpaque(false);

        lblCount = GiaoDien.label(
                "Đang tải dữ liệu...", 12, false, GiaoDien.MUTED
        );
        pnlActions.add(lblCount, BorderLayout.WEST);

        JPanel pnlButtons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 2)
        );
        pnlButtons.setOpaque(false);

        JButton btnRefresh = GiaoDien.button(
                "Làm mới", GiaoDien.CREAM, GiaoDien.NAV
        );
        JButton btnEdit = GiaoDien.button(
                "Chỉnh sửa", GiaoDien.TEAL, Color.WHITE
        );
        JButton btnDelete = GiaoDien.button(
                "Xóa", GiaoDien.ORANGE, Color.WHITE
        );

        final boolean canEdit = !tableName.startsWith("ChiTiet")
                && !Set.of(
                        "HoaDon", "LichSuTichDiem"
                ).contains(tableName)
                && !isManagerReview
                && !(role == VaiTro.QUAN_LY && (tableName.equals("YeuCauNhapKho") || tableName.equals("PhieuNhapKho")));
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(
                !tableName.startsWith("ChiTiet")
                && QuyTrinhDAO.detailTable(tableName) == null
                && !Set.of("LichSuTichDiem").contains(tableName)
        );

        btnRefresh.addActionListener(e -> lamMoi());
        btnEdit.addActionListener(e -> edit(true));
        btnDelete.addActionListener(e -> hideRecord());

        pnlButtons.add(btnRefresh);

        if (QuyTrinhDAO.detailTable(tableName) != null) {
            JButton btnDetails = GiaoDien.button(
                    tableName.equals("HoaDon")
                            ? "Chi tiết hóa đơn"
                            : isManagerReview ? "Xem chi tiết" : "Chi tiết phiếu",
                    GiaoDien.CREAM, GiaoDien.NAV
            );
            btnDetails.addActionListener(e -> openDocument());
            pnlButtons.add(btnDetails);
        }

        ThaoTacNghiepVu.install(
                pnlButtons, tableName, page,
                this::currentRecord, this::load, this,
                role, employeeId, tblTable
        );

        if (page.equals("Phiếu cấp phát phụ tùng") || page.equals("Cấp phát phụ tùng")) {
            JButton btnAllocate = GiaoDien.button(
                    "Xác nhận cấp phát", GiaoDien.TEAL, Color.WHITE
            );
            btnAllocate.addActionListener(e -> allocate());
            pnlButtons.add(btnAllocate);
            JButton btnReturn = GiaoDien.button("Ghi nhận trả kho", GiaoDien.CREAM, GiaoDien.NAV);
            btnReturn.addActionListener(e -> recordReturn());
            pnlButtons.add(btnReturn);
        }

        final JButton btnThreshold;
        if (tableName.equals("TonKho") && (role == VaiTro.KHO || role == VaiTro.ADMIN)) {
            btnThreshold = GiaoDien.button(
                    "Cập nhật mức tồn tối thiểu", GiaoDien.TEAL, Color.WHITE
            );
            btnThreshold.setEnabled(false);
            btnThreshold.addActionListener(e -> updateMinimumThreshold());
            pnlButtons.add(btnThreshold);
        } else {
            btnThreshold = null;
        }

        if (!Set.of("TonKho", "HoaDon").contains(tableName) && !isManagerReview) {
            pnlButtons.add(btnEdit);

            boolean hideDelete = Set.of(
                    "Phiếu tiếp nhận",
                    "Phiếu sửa chữa",
                    "Phiếu báo giá",
                    "Phiếu kiểm tra",
                    "Phiếu yêu cầu nhập kho",
                    "Yêu cầu nhập kho",
                    "Phiếu nhập kho phụ tùng",
                    "Nhập kho phụ tùng"
            ).contains(page) || QuyTrinhDAO.detailTable(tableName) != null;

            if (!hideDelete) {
                pnlButtons.add(btnDelete);
            }
        }

        tblTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selected = tblTable.getSelectedRow();
                if (btnThreshold != null) {
                    btnThreshold.setEnabled(selected >= 0);
                }
                if (selected < 0) {
                    btnEdit.setEnabled(false);
                } else {
                    int row = tblTable.convertRowIndexToModel(selected);
                    if (tableName.equals("YeuCauNhapKho")) {
                        int statusCol = Arrays.asList(displayFields).indexOf("trangThai");
                        boolean isPending = statusCol >= 0 && "Chờ duyệt".equals(String.valueOf(model.getValueAt(row, statusCol)));
                        btnEdit.setEnabled(isPending && !isManagerReview && (role == VaiTro.KHO || role == VaiTro.ADMIN));
                    } else if (tableName.equals("PhieuNhapKho")) {
                        int statusCol = Arrays.asList(displayFields).indexOf("trangThai");
                        String st = statusCol >= 0 ? String.valueOf(model.getValueAt(row, statusCol)) : "";
                        boolean isPending = "Chờ xác nhận nhập kho".equals(st);
                        btnEdit.setEnabled(isPending && (role == VaiTro.KHO || role == VaiTro.ADMIN));
                    } else {
                        btnEdit.setEnabled(canEdit);
                    }
                }
            }
        });

        for (Component comp : pnlButtons.getComponents()) {
            if (comp instanceof JButton btnAction) {
                Dimension pref = btnAction.getPreferredSize();
                btnAction.setPreferredSize(new Dimension(
                        Math.max(pref.width + 14, 100), 38
                ));
            }
        }

        JScrollPane scrActions = new JScrollPane(
                pnlButtons,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        scrActions.setBorder(null);
        scrActions.setOpaque(false);
        scrActions.getViewport().setOpaque(false);
        scrActions.setPreferredSize(new Dimension(850, 42));

        pnlActions.add(scrActions, BorderLayout.SOUTH);
        pnlCard.add(pnlActions, BorderLayout.SOUTH);
        add(pnlCard, BorderLayout.CENTER);

        tblTable.addMouseListener(
                new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(
                    java.awt.event.MouseEvent event
            ) {
                if (event.getClickCount() == 2
                        && tblTable.rowAtPoint(
                                event.getPoint()
                        ) >= 0) {
                    if (tableName.equals("HoaDon") || QuyTrinhDAO.detailTable(tableName) != null) {
                        openDocument();
                    } else if (tableName.equals("TonKho") && (role == VaiTro.KHO || role == VaiTro.ADMIN)) {
                        updateMinimumThreshold();
                    }
                }
            }
        });

        txtSearch.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(
                    javax.swing.event.DocumentEvent e
            ) {
                filter();
            }

            @Override
            public void removeUpdate(
                    javax.swing.event.DocumentEvent e
            ) {
                filter();
            }

            @Override
            public void changedUpdate(
                    javax.swing.event.DocumentEvent e
            ) {
                filter();
            }
        });

        // Khởi tạo các lựa chọn lọc cố định theo quy định nghiệp vụ và trạng thái mặc định
        String statusField = (page.equals("Phiếu cấp phát phụ tùng") || page.equals("Cấp phát phụ tùng"))
                ? "trangThaiHangMuc"
                : "trangThai";
        int statusCol = Arrays.asList(displayFields).indexOf(statusField);
        if (statusCol >= 0) {
            List<String> options = filterOptionsFor(tableName, statusField);
            if (!options.isEmpty()) {
                statusFilters.setPredefinedOptions(statusCol, options);
            }
        }

        String defaultState = switch (page) {
            case "Phiếu báo giá" -> "Còn hiệu lực";
            case "Phiếu sửa chữa" -> "Chờ sửa chữa";
            case "Phiếu cấp phát phụ tùng", "Cấp phát phụ tùng" -> "Chờ sửa chữa";
            case "Phiếu yêu cầu nhập kho", "Danh sách yêu cầu nhập", "Yêu cầu nhập kho" -> "Chờ duyệt";
            case "Phiếu nhập kho phụ tùng", "Nhập kho phụ tùng" -> "Chờ xác nhận nhập kho";
            default -> null;
        };

        if (defaultState != null && statusCol >= 0) {
            statusFilters.selectDefault(statusCol, defaultState);
        }

        load();
    }

    private void lamMoi() {
        txtSearch.setText("");
        statusFilters.reset();
        load();
        tblTable.clearSelection();
        tblTable.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
    }

    private void load() {
        try {
            if ("ChiTietPhieuSuaChua".equals(tableName)) {
                QuyTrinhDAO.refreshRepairShortages();
            }

            model.setRowCount(0);
            List<LinkedHashMap<String, Object>> rows = dao.getAll();

            for (Map<String, Object> row : rows) {
                if (tableName.equals("PhieuNhapKho")) {
                    Object rawYcn = row.get("maYCNK");
                    Object rawStatus = row.get("trangThai");
                    boolean isUc009 = rawYcn != null && !rawYcn.toString().isBlank();
                    if (isUc009 && "Chờ duyệt".equals(rawStatus)) {
                        row.put("trangThai", "Chờ xác nhận nhập kho");
                    }
                }

                if (tableName.equals("HoaDon")) {
                    java.math.BigDecimal total =
                            java.math.BigDecimal.ZERO;

                    for (String field : List.of(
                            "tienPhuTung", "tienCong",
                            "dichVuKhac", "thueVAT"
                    )) {
                        total = total.add(
                                (java.math.BigDecimal) row.get(field)
                        );
                    }

                    row.put(
                            "tongThanhToan",
                            total.subtract(
                                     (java.math.BigDecimal)
                                            row.get("giamGia")
                            )
                    );
                }

                Object[] values = new Object[displayFields.length];
                for (int i = 0; i < displayFields.length; i++) {
                    String field = displayFields[i];
                    if (field.equals("gioiTinh")) {
                        values[i] = Boolean.TRUE.equals(row.get(field))
                                || "1".equals(
                                        String.valueOf(row.get(field))
                                ) ? "Nam" : "Nữ";
                    } else {
                        values[i] = row.get(field);
                    }
                }

                if (tableName.equals("LichSuTichDiem")) {
                    int reasonColumn = Arrays.asList(displayFields)
                            .indexOf("lyDo");
                    if (reasonColumn >= 0) {
                        values[reasonColumn] =
                                "Ghi nhận tích điểm từ "
                                + Objects.toString(
                                        row.get("maHoaDon"), "—"
                                );
                    }
                }

                model.addRow(values);
            }

            statusFilters.refresh();
        } catch (SQLException ex) {
            lblCount.setText("Lỗi kết nối SQL Server");
            JOptionPane.showMessageDialog(
                    this, ex.getMessage(),
                    "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void filter() {
        statusFilters.search(txtSearch.getText());
    }

    private void updateCount() {
        lblCount.setText(
                "Hiển thị: " + tblTable.getRowCount()
                + " / " + model.getRowCount() + " bản ghi"
        );
    }

    private void allocate() {
        int selected = tblTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng chọn hạng mục cần cấp phát."
            );
            return;
        }

        int row = tblTable.convertRowIndexToModel(selected);

        if (JOptionPane.showConfirmDialog(
                this,
                "Xác nhận cấp phát phụ tùng cho hạng mục đã chọn?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        ) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            ThuongMaiDAO.allocate(
                    String.valueOf(model.getValueAt(row, 0)),
                    String.valueOf(model.getValueAt(row, 1)),
                    String.valueOf(model.getValueAt(row, 2)), employeeId
            );
            load();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this, ex.getMessage(),
                    "Không thể cấp phát", JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void recordReturn() {
        int selected=tblTable.getSelectedRow();
        if(selected<0) { JOptionPane.showMessageDialog(this,"Vui lòng chọn hạng mục cần ghi nhận trả kho."); return; }
        int row=tblTable.convertRowIndexToModel(selected);
        try {
            int issued=Integer.parseInt(String.valueOf(model.getValueAt(row,Arrays.asList(displayFields).indexOf("soLuongCapPhat"))));
            int used=Integer.parseInt(String.valueOf(model.getValueAt(row,Arrays.asList(displayFields).indexOf("soLuongSuDung"))));
            int returned=Integer.parseInt(String.valueOf(model.getValueAt(row,Arrays.asList(displayFields).indexOf("soLuongHoanTra"))));
            int remaining=issued-used-returned;
            if(remaining<=0) throw new SQLException("Hạng mục không còn số lượng phụ tùng có thể trả.");
            String quantity=JOptionPane.showInputDialog(this,"Số phụ tùng thực tế nhận lại (còn có thể trả: "+remaining+"):",remaining);
            if(quantity==null) return;
            String employee=employeeId;
            if(employee==null || employee.isBlank()) employee=ThaoTacNghiepVu.choose(this,"NhanVien","maNhanVien","Nhân viên kho xác nhận hàng trả");
            if(employee==null) return;
            QuyTrinhDAO.recordPartReturn(
                    String.valueOf(model.getValueAt(row,0)),String.valueOf(model.getValueAt(row,1)),
                    String.valueOf(model.getValueAt(row,2)),Integer.parseInt(quantity.trim()),employee
            );
            load();
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this,ex.getMessage(),"Không thể ghi nhận trả kho",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMinimumThreshold() {
        int selected = tblTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng chọn phụ tùng cần cập nhật mức tồn tối thiểu.",
                    "Chưa chọn bản ghi", JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        int row = tblTable.convertRowIndexToModel(selected);
        Map<String, Object> record = new LinkedHashMap<>();
        for (String field : displayFields) {
            int col = Arrays.asList(displayFields).indexOf(field);
            if (col >= 0) {
                record.put(field, model.getValueAt(row, col));
            }
        }
        HopThoaiCapNhatTonToiThieu.show(this, record, role, this::load);
    }

    private void edit(boolean existing) {
        if (isManagerReview) {
            if (!existing) {
                JOptionPane.showMessageDialog(this,
                        "Quản lý chỉ có quyền xem xét và duyệt yêu cầu nhập kho, không được tạo mới yêu cầu.",
                        "Phân quyền", JOptionPane.WARNING_MESSAGE);
            } else {
                openDocument();
            }
            return;
        }

        if (!existing && tableName.equals("PhieuTiepNhan")) {
            HopThoaiTiepNhanMoi.show(this);
            load();
            return;
        }
        if (existing && tableName.equals("PhieuSuaChua")) {
            Map<String, Object> record = requireSelectedRecord("chỉnh sửa");
            if (record == null) return;

            if (Set.of(
                    "Chờ sửa chữa", "Đang sửa chữa", "Đã sửa xong"
            ).contains(String.valueOf(record.get("trangThai")))) {
                HopThoaiTienDoSuaChua.show(
                        this, String.valueOf(record.get("maPSC"))
                );
                load();
                return;
            }
        }

        if (!existing && tableName.equals("PhieuNhapKho")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Phiếu nhập kho phải được lập từ Yêu cầu nhập kho đã duyệt.\n"
                    + "Vui lòng chọn menu 'Phiếu yêu cầu nhập kho', chọn yêu cầu có trạng thái 'Đã duyệt' và nhấn 'Lập phiếu nhập kho'.",
                    "Quy trình nhập kho",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        if (existing) {
            Map<String, Object> record = requireSelectedRecord("chỉnh sửa");
            if (record == null) return;

            if (QuyTrinhDAO.detailTable(tableName) != null) {
                TrinhSuaPhieu.show(this, tableName, record, role, employeeId);
                load();
                return;
            }
        } else if (QuyTrinhDAO.detailTable(tableName) != null) {
            Map<String, Object> record = null;
            if (employeeId != null) {
                record = new LinkedHashMap<>();
                record.put("maNhanVien", employeeId);
            }
            TrinhSuaPhieu.show(this, tableName, record, role, employeeId);
            load();
            return;
        }

        int row = tblTable.getSelectedRow();
        if (existing && row < 0) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng chọn bản ghi."
            );
            return;
        }

        int source = existing
                ? tblTable.convertRowIndexToModel(row) : -1;
        String generatedId = null;

        if (!existing && !fields[0].equals("VIN")) {
            try {
                generatedId = dao.previewNextId();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        this, ex.getMessage(),
                        "Không thể sinh mã", JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(20, 22, 12, 22));

        Map<String, JComponent> inputs = new LinkedHashMap<>();

        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            int column = Arrays.asList(displayFields).indexOf(field);
            String value = existing
                    && model.getValueAt(source, column) != null
                            ? model.getValueAt(source, column).toString()
                            : "";

            String type = Schema.TYPES.get(tableName)
                    .get(field).split(":")[0];

            JComponent input;

            OChonThamChieu cmbReference =
                    OChonThamChieu.create(tableName, field);
            JComboBox<String> cmbOptions =
                    LuaChonBieuMau.create(
                            tableName, field, value, existing
                    );

            if (cmbReference != null) {
                cmbReference.selectId(value);
                input = cmbReference;
            } else if (cmbOptions != null) {
                input = cmbOptions;
            } else if (type.equals("datetime")
                    || type.equals("datetime2")
                    || type.equals("date")) {
                input = new OChonNgay(value);
            } else if (field.equalsIgnoreCase("gioiTinh")) {
                JPanel pnlGioiTinh = new JPanel(
                        new FlowLayout(FlowLayout.LEFT, 14, 8)
                );
                pnlGioiTinh.setOpaque(false);

                JRadioButton radNam = new JRadioButton("Nam");
                JRadioButton radNu = new JRadioButton("Nữ");

                radNam.setOpaque(false);
                radNu.setOpaque(false);
                radNam.setFont(GiaoDien.font(13, false));
                radNu.setFont(GiaoDien.font(13, false));

                ButtonGroup group = new ButtonGroup();
                group.add(radNam);
                group.add(radNu);

                radNam.setSelected(
                        existing && !value.equalsIgnoreCase("Nữ")
                );
                radNu.setSelected(value.equalsIgnoreCase("Nữ"));

                pnlGioiTinh.add(radNam);
                pnlGioiTinh.add(radNu);
                input = pnlGioiTinh;
            } else {
                JTextField txtInput = GiaoDien.field(value);
                txtInput.setText(value);

                boolean auto = i == 0
                        && !field.equals("VIN") && !existing;

                if (auto) txtInput.setText(generatedId);

                if (i == 0) {
                    txtInput.setEditable(
                            field.equals("VIN") && !existing
                    );
                }

                if (auto || (i == 0 && existing)) {
                DinhDangKeThua.lock(txtInput);
                }

                input = txtInput;
            }

            inputs.put(field, input);

            JPanel pnlCell = new JPanel(new BorderLayout(0, 7));
            pnlCell.setOpaque(false);
            pnlCell.add(
                    GiaoDien.label(
                            NhanCot.forField(field)
                            + (Schema.REQUIRED.get(tableName)
                                    .contains(field) ? " *" : ""),
                            12, true, GiaoDien.TEXT
                    ),
                    BorderLayout.NORTH
            );

            input.setPreferredSize(new Dimension(250, 40));
            pnlCell.add(input, BorderLayout.CENTER);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = i % 2;
            gbc.gridy = i / 2;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(
                    0, i % 2 == 0 ? 0 : 9,
                    16, i % 2 == 0 ? 9 : 0
            );
            pnlForm.add(pnlCell, gbc);
        }

        TruongKeThua.Binding inherited =
                TruongKeThua.bind(tableName, inputs);

        JScrollPane scrSc = new JScrollPane(pnlForm);
        scrSc.setBorder(null);
        scrSc.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        int formH = pnlForm.getPreferredSize().height + 8;
        scrSc.setPreferredSize(
                new Dimension(700, Math.min(540, formH))
        );

        JDialog winDialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                existing ? "Chỉnh sửa thông tin" : "Thêm thông tin mới",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        winDialog.setDefaultCloseOperation(
                WindowConstants.DISPOSE_ON_CLOSE
        );

        JPanel pnlContent = new JPanel(new BorderLayout(0, 12));
        pnlContent.setBorder(new EmptyBorder(12, 12, 12, 12));
        pnlContent.add(scrSc, BorderLayout.CENTER);

        JPanel pnlActions = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 8, 2)
        );
        JButton btnSave = GiaoDien.button(
                "Lưu", GiaoDien.TEAL, Color.WHITE
        );
        JButton btnCancel = GiaoDien.button(
                "Hủy", GiaoDien.CREAM, GiaoDien.NAV
        );

        btnSave.setPreferredSize(new Dimension(115, 40));
        btnCancel.setPreferredSize(new Dimension(115, 40));
        pnlActions.add(btnSave);
        pnlActions.add(btnCancel);
        pnlContent.add(pnlActions, BorderLayout.SOUTH);

        winDialog.setContentPane(pnlContent);
        winDialog.getRootPane().setDefaultButton(btnSave);
        winDialog.getRootPane().registerKeyboardAction(
                e -> winDialog.dispose(),
                KeyStroke.getKeyStroke(
                        java.awt.event.KeyEvent.VK_ESCAPE, 0
                ),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        btnCancel.addActionListener(e -> winDialog.dispose());

        String[] expectedId = {generatedId};
        Map<JComponent, javax.swing.border.Border> borders =
                new LinkedHashMap<>();

        for (JComponent input : inputs.values()) {
            borders.put(input, input.getBorder());
        }

        btnSave.addActionListener(e -> {
            btnSave.setEnabled(false);
            borders.forEach(JComponent::setBorder);

            try {
                if (inherited != null) inherited.validate();

                Map<String, Object> values =
                        KiemTraBieuMau.read(
                                tableName, inputs, existing
                        );

                if (existing) {
                    dao.update(
                            model.getValueAt(source, 0).toString(),
                            values
                    );
                } else {
                    dao.insert(values, expectedId[0]);
                }

                winDialog.dispose();
                load();
            } catch (Exception ex) {
                String field =
                        KiemTraBieuMau.errorField(ex, tableName);
                String message = ex.getMessage();

                if (!existing && fields[0].equals(field)
                        && !field.equals("VIN")) {
                    try {
                        expectedId[0] = dao.previewNextId();
                        ((JTextField) inputs.get(field))
                                .setText(expectedId[0]);
                        message = "Mã đã thay đổi. Mã mới là "
                                + expectedId[0]
                                + ". Dữ liệu bạn nhập vẫn được giữ nguyên;"
                                + " vui lòng nhấn Lưu lại.";
                    } catch (SQLException refreshError) {
                        message = message
                                + "\nKhông lấy được mã mới: "
                                + refreshError.getMessage();
                    }
                } else if (ex instanceof SQLException sql
                        && field != null) {
                    String label = NhanCot.forField(field);

                    if (sql.getErrorCode() == 2601
                            || sql.getErrorCode() == 2627) {
                        message = label
                                + " đã tồn tại. Vui lòng nhập giá trị khác.";
                    } else if (sql.getErrorCode() == 547
                            && message != null
                            && message.contains("FK_")) {
                        message = label
                                + " không tồn tại hoặc không hợp lệ."
                                + " Vui lòng kiểm tra lại.";
                    }
                }

                JComponent invalid = inputs.get(field);
                if (invalid != null) {
                    invalid.setBorder(
                            BorderFactory.createLineBorder(
                                    GiaoDien.ORANGE, 2
                            )
                    );
                }

                JOptionPane.showMessageDialog(
                        winDialog,
                        message == null
                                ? "Không thể lưu."
                                        + " Vui lòng kiểm tra lại thông tin."
                                : message,
                        "Không thể lưu",
                        JOptionPane.ERROR_MESSAGE
                );

                JComponent target = invalid;
                if (target == null || !target.isEnabled()
                        || (target instanceof JTextField txtText
                                && !txtText.isEditable())) {
                    target = inputs.values().stream()
                            .filter(input -> input.isEnabled()
                                    && (!(input instanceof
                                            JTextField txtText)
                                            || txtText.isEditable()))
                            .findFirst()
                            .orElse(btnSave);
                }

                JComponent focus = target;
                SwingUtilities.invokeLater(() -> {
                    focus.scrollRectToVisible(
                            new Rectangle(
                                    0, 0, focus.getWidth(),
                                    focus.getHeight()
                            )
                    );

                    if (focus instanceof OChonNgay pnlPicker) {
                        pnlPicker.focusCalendar();
                    } else {
                        focus.requestFocusInWindow();
                        if (focus instanceof JTextField txtText) {
                            txtText.selectAll();
                        }
                    }
                });
            } finally {
                btnSave.setEnabled(true);
            }
        });

        winDialog.pack();
        winDialog.setLocationRelativeTo(null);
        winDialog.setVisible(true);
    }

    private Map<String, Object> currentRecord() {
        int selected = tblTable.getSelectedRow();
        if (selected < 0) return null;

        int row = tblTable.convertRowIndexToModel(selected);
        Map<String, Object> record = new LinkedHashMap<>();

        for (String field : fields) {
            int column = Arrays.asList(displayFields).indexOf(field);
            if (column >= 0) {
                record.put(field, model.getValueAt(row, column));
            }
        }
        return record;
    }

    private Map<String, Object> requireSelectedRecord(String actionDesc) {
        Map<String, Object> record = currentRecord();
        if (record == null) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng chọn một bản ghi" + (actionDesc == null ? "." : " để " + actionDesc + "."),
                    "Chưa chọn bản ghi", JOptionPane.WARNING_MESSAGE
            );
        }
        return record;
    }

    private void openDocument() {
        if (tableName.equals("HoaDon")) {
            int selected = tblTable.getSelectedRow();
            if (selected < 0) {
                JOptionPane.showMessageDialog(
                        this, "Vui lòng chọn hóa đơn."
                );
                return;
            }

            String invoice = String.valueOf(
                    model.getValueAt(
                            tblTable.convertRowIndexToModel(selected), 0
                    )
            );

            JDialog winInvoice = new JDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Chi tiết hóa đơn " + invoice,
                    Dialog.ModalityType.APPLICATION_MODAL
            );

            JScrollPane scrInvoice = new JScrollPane(
                    new ManHinhHoaDon(invoice)
            );
            scrInvoice.getVerticalScrollBar().setUnitIncrement(20);
            scrInvoice.setHorizontalScrollBarPolicy(
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
            );

            Dimension screen = Toolkit.getDefaultToolkit()
                    .getScreenSize();
            scrInvoice.setPreferredSize(new Dimension(
                    Math.min(1050, screen.width - 100),
                    Math.min(740, screen.height - 140)
            ));

            winInvoice.setContentPane(scrInvoice);
            winInvoice.pack();
            winInvoice.setLocationRelativeTo(null);
            winInvoice.setVisible(true);
            load();
            return;
        }

        Map<String, Object> record = requireSelectedRecord("xem chi tiết");
        if (record != null) {
            if (isManagerReview) {
                TrinhSuaPhieu.review(this, tableName, record, role, employeeId);
            } else {
                TrinhSuaPhieu.view(this, tableName, record);
            }
            load();
        }
    }

    private void hideRecord() {
        int row = tblTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this, "Vui lòng chọn một bản ghi để xóa.",
                    "Chưa chọn bản ghi", JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (JOptionPane.showConfirmDialog(
                this,
                "Bạn chắc chắn muốn xoá dòng này"
                        + " ra khỏi danh sách hiển thị?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        ) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            dao.softDelete(
                    model.getValueAt(
                            tblTable.convertRowIndexToModel(row), 0
                    ).toString()
            );
            load();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this, ex.getMessage(),
                    "Lỗi SQL", JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static List<String> filterOptionsFor(String table, String field) {
        if ("PhieuNhapKho".equals(table) && "trangThai".equals(field)) {
            return List.of("Chờ xác nhận nhập kho", "Đã nhập kho", "Đã hủy");
        }
        if ("YeuCauNhapKho".equals(table) && "trangThai".equals(field)) {
            return List.of("Chờ duyệt", "Đã duyệt", "Từ chối", "Đã xử lý");
        }
        return Collections.emptyList();
    }

    public JComboBox<String> getStatusFilterCombo() {
        String statusField = (page.equals("Phiếu cấp phát phụ tùng") || page.equals("Cấp phát phụ tùng"))
                ? "trangThaiHangMuc"
                : "trangThai";
        int column = Arrays.asList(displayFields).indexOf(statusField);
        return column >= 0 ? statusFilters.getFilter(column) : null;
    }

    public BoLocTrangThai getStatusFilters() {
        return statusFilters;
    }

    public JTable getTable() {
        return tblTable;
    }
}
