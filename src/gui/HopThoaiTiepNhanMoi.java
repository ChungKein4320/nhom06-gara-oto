package gui;

import java.util.Locale;
import java.time.format.DateTimeFormatter;
import java.text.*;
import javax.swing.table.*;
import javax.swing.border.*;
import dao.ChonDAO;

import dao.QuyTrinhDAO;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

// ===== GUI: HopThoaiTiepNhanMoi =====
/** Form tiếp nhận xe đồng thời tra cứu hoặc tạo hồ sơ khách hàng và phương tiện. */
final class HopThoaiTiepNhanMoi {
    // ===== KHỞI TẠO ĐỐI TƯỢNG: HopThoaiTiepNhanMoi() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    private HopThoaiTiepNhanMoi() {
    }

    // ===== PHƯƠNG THỨC: show(Component) — Hiển thị nghiệp vụ tương ứng. =====
    static void show(Component parent) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Phiếu tiếp nhận",
                Dialog.ModalityType.APPLICATION_MODAL);
        BieuMauPhieuTiepNhan receiptForm = new BieuMauPhieuTiepNhan("PhieuTiepNhan", null,
                Set.of("maPTN", "maKhachHang", "VIN", "ngayTiepNhan"));
        receiptForm.removeHorizontalPadding();
        BieuMauKhachHang customerForm = new BieuMauKhachHang("KhachHang", null, Set.of("maKhachHang"));
        BieuMauPhuongTien vehicleForm = new BieuMauPhuongTien("PhuongTien", null, Set.of("soKM"));
        OChonThamChieu existingCustomer = OChonThamChieu.create("PhieuTiepNhan", "maKhachHang");
        OChonThamChieu existingVehicle = OChonThamChieu.create("PhieuTiepNhan", "VIN");

        JLabel lblReceiptCode = GiaoDien.label("Mã phiếu: " + nextCode("PhieuTiepNhan"), 13, true,
                GiaoDien.TEAL);
        JLabel lblCustomerCode = GiaoDien.label("Mã khách hàng: " + nextCode("KhachHang"), 12, false,
                GiaoDien.MUTED);

        ButtonGroup customerMode = new ButtonGroup();
        JRadioButton radExistingCustomer = modeButton("Khách hàng đã có hồ sơ", true, customerMode);
        JRadioButton radNewCustomer = modeButton("Tạo hồ sơ khách hàng mới", false, customerMode);
        JPanel customerCards = new JPanel(new CardLayout());
        customerCards.setOpaque(false);
        JPanel existingCustomerPanel = new JPanel(new BorderLayout(0, 5));
        existingCustomerPanel.setOpaque(false);
        existingCustomerPanel.add(GiaoDien.label("Chọn khách hàng", 12, true, GiaoDien.TEXT),
                BorderLayout.NORTH);
        existingCustomerPanel.add(compactChooser(existingCustomer), BorderLayout.CENTER);
        JPanel newCustomerPanel = new JPanel(new BorderLayout(0, 4));
        newCustomerPanel.setOpaque(false);
        newCustomerPanel.add(lblCustomerCode, BorderLayout.NORTH);
        newCustomerPanel.add(customerForm, BorderLayout.CENTER);
        customerCards.add(existingCustomerPanel, "existing");
        customerCards.add(newCustomerPanel, "new");
        customerCards.setPreferredSize(existingCustomerPanel.getPreferredSize());
        JPanel customerModes = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        customerModes.setOpaque(false);
        customerModes.add(radExistingCustomer);
        customerModes.add(radNewCustomer);
        JPanel customerCard = GiaoDien.card(new BorderLayout(0, 10));
        JPanel customerContent = new JPanel();
        customerContent.setOpaque(false);
        customerContent.setLayout(new BoxLayout(customerContent, BoxLayout.Y_AXIS));
        customerModes.setAlignmentX(Component.LEFT_ALIGNMENT);
        customerCards.setAlignmentX(Component.LEFT_ALIGNMENT);
        customerContent.add(customerModes);
        customerContent.add(Box.createVerticalStrut(8));
        customerContent.add(customerCards);
        customerCard.add(GiaoDien.label("1. Thông tin khách hàng", 15, true, GiaoDien.NAV),
                BorderLayout.NORTH);
        customerCard.add(customerContent, BorderLayout.CENTER);

        ButtonGroup vehicleMode = new ButtonGroup();
        JRadioButton radExistingVehicle = modeButton("Phương tiện đã có hồ sơ", true, vehicleMode);
        JRadioButton radNewVehicle = modeButton("Tạo hồ sơ phương tiện mới", false, vehicleMode);
        JPanel vehicleCards = new JPanel(new CardLayout());
        vehicleCards.setOpaque(false);
        JPanel existingVehiclePanel = new JPanel(new BorderLayout(0, 5));
        existingVehiclePanel.setOpaque(false);
        existingVehiclePanel.add(
                GiaoDien.label("Phương tiện thuộc khách hàng đã chọn", 12, true, GiaoDien.TEXT),
                BorderLayout.NORTH);
        existingVehiclePanel.add(compactChooser(existingVehicle), BorderLayout.CENTER);
        JPanel newVehiclePanel = new JPanel(new BorderLayout(0, 4));
        newVehiclePanel.setOpaque(false);
        newVehiclePanel
                .add(GiaoDien.label("Nhập VIN và thông tin xe. Số km hiện tại sẽ lấy từ ODO tiếp nhận.", 11,
                        false, GiaoDien.MUTED), BorderLayout.NORTH);
        newVehiclePanel.add(vehicleForm, BorderLayout.CENTER);
        vehicleCards.add(existingVehiclePanel, "existing");
        vehicleCards.add(newVehiclePanel, "new");
        vehicleCards.setPreferredSize(existingVehiclePanel.getPreferredSize());
        JPanel vehicleModes = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        vehicleModes.setOpaque(false);
        vehicleModes.add(radExistingVehicle);
        vehicleModes.add(radNewVehicle);
        JPanel vehicleCard = GiaoDien.card(new BorderLayout(0, 10));
        JPanel vehicleContent = new JPanel();
        vehicleContent.setOpaque(false);
        vehicleContent.setLayout(new BoxLayout(vehicleContent, BoxLayout.Y_AXIS));
        vehicleModes.setAlignmentX(Component.LEFT_ALIGNMENT);
        vehicleCards.setAlignmentX(Component.LEFT_ALIGNMENT);
        vehicleContent.add(vehicleModes);
        vehicleContent.add(Box.createVerticalStrut(8));
        vehicleContent.add(vehicleCards);
        vehicleCard.add(GiaoDien.label("2. Thông tin phương tiện", 15, true, GiaoDien.NAV),
                BorderLayout.NORTH);
        vehicleCard.add(vehicleContent, BorderLayout.CENTER);

        radExistingCustomer
                .addActionListener(e -> showCard(customerCards, "existing", existingCustomerPanel));
        radNewCustomer.addActionListener(e -> showCard(customerCards, "new", newCustomerPanel));
        radExistingVehicle.addActionListener(e -> showCard(vehicleCards, "existing", existingVehiclePanel));
        radNewVehicle.addActionListener(e -> showCard(vehicleCards, "new", newVehiclePanel));

        Runnable filterCustomerVehicles = () -> {
            existingVehicle.selectId("");
            try {
                existingVehicle.filterVehiclesByCustomer(existingCustomer.selectedId());
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Không tải được phương tiện của khách hàng: " + ex.getMessage(), "Lỗi tải danh sách",
                        JOptionPane.ERROR_MESSAGE);
            }
        };
        existingCustomer.addActionListener(e -> filterCustomerVehicles.run());
        radExistingCustomer.addActionListener(e -> filterCustomerVehicles.run());
        radNewCustomer.addActionListener(e -> {
            existingVehicle.selectId("");
            try {
                existingVehicle.clearVehicleCustomerFilter();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Không tải được danh sách phương tiện: " + ex.getMessage(), "Lỗi tải danh sách",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        try {
            existingVehicle.filterVehiclesByCustomer("");
        } catch (SQLException ex) {
            existingVehicle.setToolTipText("Không tải được phương tiện: " + ex.getMessage());
        }

        JPanel receiptCard = GiaoDien.card(new BorderLayout(0, 5));
        receiptCard.add(GiaoDien.label("3. Thông tin tiếp nhận", 15, true, GiaoDien.NAV), BorderLayout.NORTH);
        OChonNgay intakeDate = new OChonNgay(LocalDate.now().toString());
        JSpinner intakeTime = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
        intakeTime.setEditor(new JSpinner.DateEditor(intakeTime, "HH:mm"));
        intakeTime.setPreferredSize(new Dimension(130, 38));
        JPanel dateTime = new JPanel(new GridLayout(1, 2, 10, 0));
        dateTime.setOpaque(false);
        dateTime.add(labeled("Ngày tiếp nhận *", intakeDate));
        dateTime.add(labeled("Giờ tiếp nhận *", intakeTime));
        JPanel receiptContent = new JPanel(new BorderLayout(0, 4));
        receiptContent.setOpaque(false);
        receiptContent.add(dateTime, BorderLayout.NORTH);
        receiptContent.add(receiptForm, BorderLayout.CENTER);
        receiptCard.add(receiptContent, BorderLayout.CENTER);

        JPanel codeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        codeRow.setOpaque(false);
        codeRow.add(lblReceiptCode);
        JPanel profiles = new JPanel(new GridLayout(1, 2, 10, 0));
        profiles.setOpaque(false);
        profiles.add(customerCard);
        profiles.add(vehicleCard);

        List<Map<String, Object>> details = new ArrayList<>();
        DefaultTableModel model = new DefaultTableModel(new String[] { "Mã dịch vụ", "Yêu cầu dịch vụ" }, 0) {
            // ===== PHƯƠNG THỨC: isCellEditable(int, int) — Kiểm tra ô bảng có cho phép chỉnh sửa không. =====
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = GiaoDien.table(model);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(36);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Map<String, Object> detail : details)
                model.addRow(new Object[] { detail.get("maDichVu"), detail.get("yeuCauDichVu") });
        };
        JScrollPane scrDetails = new JScrollPane(table);
        scrDetails.setPreferredSize(new Dimension(1080, 185));
        JPanel detailSection = GiaoDien.card(new BorderLayout(0, 6));
        detailSection.add(GiaoDien.label("4. Hạng mục và yêu cầu dịch vụ", 15, true, GiaoDien.NAV),
                BorderLayout.NORTH);
        detailSection.add(scrDetails, BorderLayout.CENTER);

        JButton btnAdd = GiaoDien.button("Thêm dịch vụ / yêu cầu", GiaoDien.TEAL, Color.WHITE);
        JButton btnEdit = GiaoDien.button("Sửa yêu cầu", GiaoDien.CREAM, GiaoDien.NAV);
        JButton btnRemove = GiaoDien.button("Bỏ yêu cầu", GiaoDien.CREAM, GiaoDien.NAV);
        JButton btnSave = GiaoDien.button("Lưu phiếu tiếp nhận", GiaoDien.TEAL, Color.WHITE);
        JButton btnClose = GiaoDien.button("Đóng", GiaoDien.CREAM, GiaoDien.NAV);
        btnAdd.addActionListener(e -> {
            Map<String, Object> row = editDetail(dialog, null);
            if (row != null) {
                details.add(row);
                refresh.run();
            }
        });
        btnEdit.addActionListener(e -> {
            int selected = table.getSelectedRow();
            if (selected < 0) {
                JOptionPane.showMessageDialog(dialog, "Chọn yêu cầu dịch vụ cần sửa.");
                return;
            }
            int index = table.convertRowIndexToModel(selected);
            Map<String, Object> row = editDetail(dialog, details.get(index));
            if (row != null) {
                details.set(index, row);
                refresh.run();
            }
        });
        btnRemove.addActionListener(e -> {
            int selected = table.getSelectedRow();
            if (selected < 0) {
                JOptionPane.showMessageDialog(dialog, "Chọn yêu cầu dịch vụ cần bỏ.");
                return;
            }
            details.remove(table.convertRowIndexToModel(selected));
            refresh.run();
        });
        btnSave.addActionListener(e -> {
            btnSave.setEnabled(false);
            try {
                Map<String, Object> header = receiptForm.read();
                if (intakeDate.getDateText().isBlank())
                    throw new SQLException("Vui lòng chọn ngày tiếp nhận.");
                LocalDate date = LocalDate.parse(intakeDate.getDateText());
                String timeText = new java.text.SimpleDateFormat("HH:mm")
                        .format((Date) intakeTime.getValue());
                LocalTime time = LocalTime.parse(timeText);
                header.put("ngayTiepNhan",
                        Timestamp.valueOf(LocalDateTime.of(date, time).withSecond(0).withNano(0)));
                boolean createCustomer = radNewCustomer.isSelected();
                String customerId = null;
                Map<String, Object> customer = null;
                if (createCustomer)
                    customer = customerForm.read();
                else {
                    existingCustomer.validateSelection();
                    customerId = existingCustomer.selectedId();
                    if (customerId.isBlank())
                        throw new SQLException("Vui lòng chọn khách hàng hoặc chuyển sang tạo hồ sơ mới.");
                }
                boolean createVehicle = radNewVehicle.isSelected();
                String vin = null;
                Map<String, Object> vehicle = null;
                if (createVehicle)
                    vehicle = vehicleForm.read();
                else {
                    existingVehicle.validateSelection();
                    vin = existingVehicle.selectedId();
                    if (vin.isBlank())
                        throw new SQLException("Vui lòng chọn phương tiện hoặc chuyển sang tạo hồ sơ mới.");
                }
                QuyTrinhDAO.IntakeResult created = dao.PhieuTiepNhanDAO.createIntake(header, createCustomer,
                        customerId, customer, createVehicle, vin, vehicle, details);
                JOptionPane.showMessageDialog(dialog,
                        "Đã lưu phiếu " + created.maPTN() + " cho khách hàng " + created.maKhachHang() + ".",
                        "Tiếp nhận thành công", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, ThongBaoLoiSQL.userMessage(ex, "PhieuTiepNhan"),
                        "Không thể lưu phiếu tiếp nhận", JOptionPane.ERROR_MESSAGE);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(dialog,
                        ex.getMessage() == null ? "Dữ liệu nhập chưa hợp lệ." : ex.getMessage(),
                        "Không thể lưu phiếu tiếp nhận", JOptionPane.ERROR_MESSAGE);
            } finally {
                btnSave.setEnabled(true);
            }
        });
        btnClose.addActionListener(e -> dialog.dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        buttons.add(btnAdd);
        buttons.add(btnEdit);
        buttons.add(btnRemove);
        buttons.add(btnSave);
        buttons.add(btnClose);
        JPanel body = new IntakeBody();
        body.setOpaque(false);
        addSection(body, codeRow, 0);
        addSection(body, profiles, 1);
        addSection(body, receiptCard, 2);
        addSection(body, detailSection, 3);
        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = 4;
        filler.weighty = 1;
        body.add(Box.createVerticalGlue(), filler);
        JScrollPane scrBody = new JScrollPane(body);
        scrBody.setBorder(null);
        scrBody.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrBody.getVerticalScrollBar().setUnitIncrement(16);
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        content.add(scrBody, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        GraphicsConfiguration screen = dialog.getGraphicsConfiguration();
        Rectangle bounds = screen.getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(screen);
        int availableWidth = bounds.width - screenInsets.left - screenInsets.right;
        int availableHeight = bounds.height - screenInsets.top - screenInsets.bottom;
        dialog.setMinimumSize(new Dimension(Math.min(1024, availableWidth), Math.min(560, availableHeight)));
        dialog.setSize(Math.min(1320, availableWidth), Math.min(850, availableHeight));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static final class IntakeBody extends JPanel implements Scrollable {
        IntakeBody() {
            super(new GridBagLayout());
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(16, visibleRect.height - 16);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    // ===== PHƯƠNG THỨC: modeButton(String, boolean, ButtonGroup) — Mở nút. =====
    private static JRadioButton modeButton(String text, boolean selected, ButtonGroup group) {
        JRadioButton button = new JRadioButton(text, selected);
        button.setOpaque(false);
        button.setFont(GiaoDien.font(12, false));
        group.add(button);
        return button;
    }

    // ===== PHƯƠNG THỨC: labeled(String, JComponent) — Xử lý labeled. =====
    private static JPanel labeled(String title, JComponent control) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        panel.add(GiaoDien.label(title, 12, true, GiaoDien.TEXT), BorderLayout.NORTH);
        panel.add(control, BorderLayout.CENTER);
        return panel;
    }

    // ===== PHƯƠNG THỨC: compactChooser(JComponent) — Thu gọn cách hiển thị của bộ chọn. =====
    private static JPanel compactChooser(JComponent chooser) {
        chooser.setPreferredSize(new Dimension(420, 40));
        chooser.setMinimumSize(new Dimension(300, 40));
        chooser.setMaximumSize(new Dimension(420, 40));
        chooser.setBackground(GiaoDien.WHITE);
        chooser.setForeground(GiaoDien.TEXT);
        chooser.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel holder = new JPanel();
        holder.setOpaque(false);
        holder.setLayout(new BoxLayout(holder, BoxLayout.Y_AXIS));
        holder.add(chooser);
        holder.add(Box.createVerticalGlue());
        return holder;
    }

    // ===== PHƯƠNG THỨC: showCard(JPanel, String, Component) — Hiển thị card. =====
    private static void showCard(JPanel cards, String name, Component visibleCard) {
        ((CardLayout) cards.getLayout()).show(cards, name);
        cards.setPreferredSize(visibleCard.getPreferredSize());
        cards.revalidate();
        if (cards.getParent() != null)
            cards.getParent().revalidate();
    }

    // ===== PHƯƠNG THỨC: addSection(JPanel, Component, int) — Thêm mục. =====
    private static void addSection(JPanel parent, Component child, int row) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(row == 0 ? 0 : 0, 0, 10, 0);
        parent.add(child, c);
    }

    // ===== PHƯƠNG THỨC: nextCode(String) — Tạo mã kế tiếp cho bản ghi mới. =====
    private static String nextCode(String table) {
        try {
            String code = ChonDAO.choBang(table).previewNextId();
            return code.isBlank() ? "sẽ cấp khi lưu" : code;
        } catch (SQLException ex) {
            return "sẽ cấp khi lưu";
        }
    }

    // ===== PHƯƠNG THỨC: editDetail(Component, Map<String,Object>) — Mở biểu mẫu chỉnh sửa dòng chi tiết. =====
    private static Map<String, Object> editDetail(Component parent, Map<String, Object> existing) {
        BieuMauChiTietPhieuTiepNhan form = new BieuMauChiTietPhieuTiepNhan("ChiTietPhieuTiepNhan", existing,
                Set.of("maPTN"));
        JScrollPane scroll = new JScrollPane(form);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(650, 150));
        while (JOptionPane.showConfirmDialog(parent, scroll,
                existing == null ? "Thêm yêu cầu dịch vụ" : "Sửa yêu cầu dịch vụ",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                return form.read();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Thông tin chưa hợp lệ",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    private static final class GiaoDien {
        private static final Color LINE = new Color(0xE4EBE9);

        private static final Color NAV = new Color(0x13272E);
        private static final Color TEAL = new Color(0x487674);
        private static final Color CREAM = new Color(0xE8C69B);
        private static final Color WHITE = Color.WHITE;
        private static final Color TEXT = new Color(0x21383C);
        private static final Color MUTED = new Color(0x718387);

        // ===== PHƯƠNG THỨC: font(int, boolean) — Tạo phông chữ theo cỡ và kiểu chữ. =====
        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
        }

        // ===== PHƯƠNG THỨC: rounded(Color, int) — Tạo đường viền bo góc cho thành phần. =====
        public static JPanel rounded(Color c, int r) {
            return new JPanel() {
                // ===== PHƯƠNG THỨC: paintComponent(Graphics) — Vẽ nội dung và trạng thái hiện tại của thành phần. =====
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D q = (Graphics2D) g.create();
                    q.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    q.setColor(c);
                    q.fillRoundRect(0, 0, getWidth(), getHeight(), r, r);
                    q.dispose();
                }
            };
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
                        column.setHeaderValue(nhanCot(getModel().getColumnName(column.getModelIndex())));
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

        // ===== PHƯƠNG THỨC: card(LayoutManager) — Tạo khung giao diện dạng thẻ. =====
        public static JPanel card(LayoutManager l) {
            JPanel pnlP = rounded(WHITE, 18);
            pnlP.setLayout(l);
            pnlP.setBorder(new EmptyBorder(20, 22, 20, 22));
            pnlP.setOpaque(false);
            return pnlP;
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

    // ===== PHƯƠNG THỨC: nhanCot(String) — Tạo tiêu đề hiển thị cho cột bảng. =====
    private static String nhanCot(String field) {
        return switch (field) {
        case "maDichVu" -> "Mã dịch vụ";
        case "yeuCauDichVu" -> "Yêu cầu dịch vụ";
        default -> field;
        };
    }
}
