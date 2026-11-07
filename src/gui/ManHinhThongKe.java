package gui;

import dao.ThongKeDAO;
import dao.ThongKeDAO.Bucket;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public final class ManHinhThongKe extends JPanel {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    // cmb 8 loại thống kê
    private final JComboBox<String> cmbCategory = new OChonTimKiem<>(
            new String[] { "Doanh thu", "Số lượng khách hàng", "Khách hàng", "Dịch vụ", "Phụ tùng",
                    "Phương tiện", "Doanh số của nhân viên bán hàng", "Giá trị hóa đơn" });
    private final JComboBox<String> cmbSubtype = new OChonTimKiem<>();
    private final JPanel pnlSubtype = cell("Loại phụ tùng", cmbSubtype);
    private final JLabel lblSubtype = (JLabel) pnlSubtype.getComponent(0);
    private final JCheckBox chkTheoMa = new JCheckBox("Thống kê theo mã phương tiện");
    // cmb chọn thời gian
    private final JComboBox<String> cmbPeriod = new OChonTimKiem<>(
            new String[] { "Theo tháng", "Theo quý", "Theo năm", "Thời gian tùy chọn" });
    // theo danh sách tháng, quý, năm, tùy chọn
    private final JComboBox<String> cmbMonth = new OChonTimKiem<>();
    private final JComboBox<String> cmbQuarter = new OChonTimKiem<>(
            new String[] { "Quý I", "Quý II", "Quý III", "Quý IV" });
    private final JComboBox<Integer> cmbYear = new OChonTimKiem<>();
    private final OChonNgay pnlStart = new OChonNgay(LocalDate.now().withDayOfMonth(1).toString());
    private final OChonNgay pnlEnd = new OChonNgay(LocalDate.now().toString());

    private final JPanel pnlMonthCell, pnlQuarterCell, pnlYearCell, pnlCustom;
    private final JLabel[] lblMetricTitles = new JLabel[3];
    private final JLabel[] lblMetricValues = new JLabel[3];
    private final JLabel lblChartTitle = GiaoDien.label("", 18, true, GiaoDien.NAV);
    private final JLabel lblSummary = GiaoDien.label("", 12, false, GiaoDien.MUTED);
    private final Chart pnlChart = new Chart();

    private final DefaultTableModel detailModel = new DefaultTableModel(
            new Object[] { "STT", "Nội dung thống kê", "Số liệu" }, 0) {
        private static final long serialVersionUID = 1L;

        // Chỉ xem không sửa trực tiếp.
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable detailTable = new JTable(detailModel);
    // Nút xem thống kê
    private final JButton btnView = GiaoDien.button("Xem thống kê", GiaoDien.TEAL, Color.WHITE);
    private int requestVersion;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat NUMBER = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ManHinhThongKe() — Khởi tạo đối tượng và thiết lập
    // trạng thái ban đầu. =====
    public ManHinhThongKe() {
        super(new BorderLayout(0, 20));
        setBackground(GiaoDien.BG);
        setBorder(new EmptyBorder(26, 28, 28, 28));
        // Tiêu đề màn hình
        JPanel pnlHeading = new JPanel(new GridLayout(2, 1, 0, 6));
        pnlHeading.setOpaque(false);
        pnlHeading.add(GiaoDien.label("THINKING AUTO / BÁO CÁO", 11, true, GiaoDien.TEAL));
        pnlHeading.add(GiaoDien.label("Thống kê hoạt động gara", 27, true, GiaoDien.NAV));
        add(pnlHeading, BorderLayout.NORTH);
        JPanel pnlBody = new JPanel(new BorderLayout(0, 18));
        pnlBody.setOpaque(false);
        JPanel pnlTop = new JPanel(new BorderLayout(0, 18));
        pnlTop.setOpaque(false);
        JPanel pnlFilters = GiaoDien.card(new BorderLayout(0, 12));
        JPanel pnlSelectors = new JPanel(new GridLayout(1, 2, 16, 0));
        pnlSelectors.setOpaque(false);
        // Thêm phân loại, thời gian
        pnlSelectors.add(cell("Phân loại", cmbCategory));
        pnlSelectors.add(cell("Thời gian", cmbPeriod));
        pnlFilters.add(pnlSelectors, BorderLayout.NORTH);
        JPanel pnlExtra = new JPanel(new BorderLayout(0, 6));
        pnlExtra.setOpaque(false);
        JPanel pnlSubtypeWrap = new JPanel(new BorderLayout());
        pnlSubtypeWrap.setOpaque(false);
        pnlSubtypeWrap.add(pnlSubtype, BorderLayout.WEST);
        chkTheoMa.setOpaque(false);
        chkTheoMa.setFont(GiaoDien.font(13, false));
        pnlExtra.add(pnlSubtypeWrap, BorderLayout.CENTER);
        pnlExtra.add(chkTheoMa, BorderLayout.SOUTH);
        pnlFilters.add(pnlExtra, BorderLayout.SOUTH);
        cmbSubtype.setPreferredSize(new Dimension(240, 36));
        for (int i = 1; i <= 12; i++)
            cmbMonth.addItem("Tháng " + i);
        cmbMonth.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        cmbQuarter.setSelectedIndex((LocalDate.now().getMonthValue() - 1) / 3);

        // Mặc định hiện theo năm
        cmbPeriod.setSelectedIndex(2);
        // Lấy năm hiện tại từ máy tính
        int currentYear = LocalDate.now().getYear();
        // Tạo ds từ năm nay về năm 2000
        for (int year = currentYear; year >= 2000; year--) {
            cmbYear.addItem(year);
        }

        cmbYear.setSelectedItem(currentYear);
        cmbYear.setPreferredSize(new Dimension(140, 36));
        cmbYear.setFont(GiaoDien.font(13, false));

        JPanel pnlOptions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlOptions.setOpaque(false);
        pnlMonthCell = cell("Tháng", cmbMonth);
        pnlQuarterCell = cell("Quý", cmbQuarter);
        pnlYearCell = cell("Năm", cmbYear);
        pnlOptions.add(pnlMonthCell);
        pnlOptions.add(pnlQuarterCell);
        pnlOptions.add(pnlYearCell);
        pnlCustom = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlCustom.setOpaque(false);
        pnlCustom.add(cell("Từ ngày", pnlStart));
        pnlCustom.add(cell("Đến ngày", pnlEnd));
        pnlOptions.add(pnlCustom);
        JPanel pnlFilterBottom = new JPanel(new BorderLayout(12, 0));
        pnlFilterBottom.setOpaque(false);
        pnlFilterBottom.add(pnlOptions, BorderLayout.CENTER);
        JPanel pnlButtonWrap = new JPanel(new BorderLayout());
        pnlButtonWrap.setOpaque(false);
        pnlButtonWrap.setBorder(new EmptyBorder(17, 0, 0, 0));
        pnlButtonWrap.add(btnView, BorderLayout.NORTH);
        pnlFilterBottom.add(pnlButtonWrap, BorderLayout.EAST);
        pnlFilters.add(pnlFilterBottom, BorderLayout.CENTER);
        pnlTop.add(pnlFilters, BorderLayout.NORTH);
        JPanel pnlMetrics = new JPanel(new GridLayout(1, 3, 16, 0));
        pnlMetrics.setOpaque(false);
        for (int i = 0; i < 3; i++) {
            JPanel pnlCard = GiaoDien.card(new BorderLayout(0, 12));
            lblMetricTitles[i] = GiaoDien.label("", 11, true, GiaoDien.MUTED);
            lblMetricValues[i] = GiaoDien.label("—", 25, true, i == 0 ? GiaoDien.TEAL : GiaoDien.NAV);
            pnlCard.add(lblMetricTitles[i], BorderLayout.NORTH);
            pnlCard.add(lblMetricValues[i], BorderLayout.CENTER);
            pnlMetrics.add(pnlCard);
        }
        pnlTop.add(pnlMetrics, BorderLayout.SOUTH);
        pnlBody.add(pnlTop, BorderLayout.NORTH);
        JPanel pnlChartCard = GiaoDien.card(new BorderLayout(0, 14));
        JPanel pnlChartHeading = new JPanel(new BorderLayout());
        pnlChartHeading.setOpaque(false);
        pnlChartHeading.add(lblChartTitle, BorderLayout.CENTER);
        pnlChartCard.add(pnlChartHeading, BorderLayout.NORTH);
        JPanel chartAndTable = new JPanel(new BorderLayout(0, 12));
        chartAndTable.setOpaque(false);
        chartAndTable.add(pnlChart, BorderLayout.CENTER);
        JPanel tableSection = new JPanel(new BorderLayout(0, 6));
        tableSection.setOpaque(false);
        tableSection.add(GiaoDien.label("BẢNG SỐ LIỆU CHI TIẾT", 13, true, GiaoDien.NAV), BorderLayout.NORTH);
        detailTable.setRowHeight(28);
        detailTable.setAutoCreateRowSorter(true);
        javax.swing.table.DefaultTableCellRenderer center = new javax.swing.table.DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        detailTable.setDefaultRenderer(Object.class, center);
        detailTable.setDefaultRenderer(Number.class, center);
        ((javax.swing.table.DefaultTableCellRenderer) detailTable.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);
        JScrollPane detailScroll = new JScrollPane(detailTable);
        detailScroll.setPreferredSize(new Dimension(700, 215));
        detailTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        detailTable.setFillsViewportHeight(false);
        tableSection.add(detailScroll, BorderLayout.CENTER);
        chartAndTable.add(tableSection, BorderLayout.SOUTH);
        pnlChartCard.add(chartAndTable, BorderLayout.CENTER);
        pnlChartCard.add(lblSummary, BorderLayout.SOUTH);
        pnlBody.add(pnlChartCard, BorderLayout.CENTER);
        add(pnlBody, BorderLayout.CENTER);
        for (JComboBox<String> cmbCombo : List.of(cmbCategory, cmbPeriod, cmbMonth, cmbQuarter)) {
            cmbCombo.setFont(GiaoDien.font(13, false));
            cmbCombo.setPreferredSize(new Dimension(160, 36));
        }
        // Chọn nút thì load, thay các cmb cũng load
        btnView.addActionListener(e -> load());
        cmbCategory.addActionListener(e -> {
            capNhatBoLoc();
            load();
        });
        cmbSubtype.addActionListener(e -> load());
        chkTheoMa.addActionListener(e -> load());
        cmbPeriod.addActionListener(e -> {
            updatePeriod();
            load();
        });
        cmbMonth.addActionListener(e -> load());
        cmbQuarter.addActionListener(e -> load());
        cmbYear.addActionListener(e -> load());
        updatePeriod();
        capNhatBoLoc();
        load();
    }

    // Cập nhật nhat bo loc.
    private void capNhatBoLoc() {
        int loai = cmbCategory.getSelectedIndex();
        // Chọn Phụ tùng và phương tiện thì hiện
        pnlSubtype.setVisible(loai == 4 || loai == 5);
        lblSubtype.setText(loai == 5 ? "Hãng xe" : "Loại phụ tùng");
        chkTheoMa.setVisible(loai == 5);
        cmbSubtype.removeAllItems();
        cmbSubtype.addItem("Tất cả");
        if (loai == 4 || loai == 5) {
            try {
                // Hãng xe dùng danh sách lọc (không khai báo cứng)
                for (String ten : ThongKeDAO.danhSachLoc(loai == 4))
                    cmbSubtype.addItem(ten);
            } catch (java.sql.SQLException ex) {
                System.err.println("Không tải được danh mục: " + ex.getMessage());
            }
        }
        cmbSubtype.setSelectedItem("Tất cả");
        revalidate();
        repaint();
    }

    // Chuyển chỉ số phân loại trên GUI sang mã thống kê DAO sử dụng.
    private static int loaiCu(int loai) {
        return switch (loai) {
            case 0 -> 0;
            case 1 -> 1;
            case 2 -> 9;
            case 3 -> 2;
            case 4 -> 3;
            case 5 -> 8;
            case 6 -> 11;
            default -> 12;
        };
    }

    // Tạo ô giao diện kèm nhãn.
    private static JPanel cell(String label, JComponent component) {
        JPanel pnlCell = new JPanel(new BorderLayout(0, 7));
        pnlCell.setOpaque(false);
        pnlCell.add(GiaoDien.label(label, 12, true, GiaoDien.TEXT), BorderLayout.NORTH);
        pnlCell.add(component, BorderLayout.CENTER);
        return pnlCell;
    }

    // Cập nhật ô thời gian nào được hiện
    private void updatePeriod() {
        int p = cmbPeriod.getSelectedIndex();
        pnlMonthCell.setVisible(p == 0);
        pnlQuarterCell.setVisible(p == 1);
        pnlYearCell.setVisible(p == 2);
        pnlCustom.setVisible(p == 3);
        // Cập nhật bố cục
        revalidate();
        // Vẽ lại giao diện
        repaint();
    }

    // Tính ngày bắt đầu và kết thúc theo tháng, quý, năm hoặc khoảng tùy chọn.
    static LocalDate[] dateRange(int mode, int year, int month, int quarter, String from, String to) {
        LocalDate first, last;
        switch (mode) {
            case 0 -> {
                YearMonth ym = YearMonth.of(year, month);
                first = ym.atDay(1);
                last = ym.atEndOfMonth();
            }
            // Theo quý (1-3, 4-6, 7-9, 10-12)
            case 1 -> {
                if (quarter < 1 || quarter > 4) {
                    throw new IllegalArgumentException("Quý không hợp lệ.");
                }
                first = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
                last = first.plusMonths(3).minusDays(1);
            }
            // Theo năm
            case 2 -> {
                first = LocalDate.of(year, 1, 1);
                last = LocalDate.of(year, 12, 31);
            }
            case 3 -> {
                if (from.isBlank() || to.isBlank()) {
                    throw new IllegalArgumentException("Vui lòng chọn đầy đủ Từ ngày và Đến ngày.");
                }
                first = LocalDate.parse(from);
                last = LocalDate.parse(to);
            }
            default -> throw new IllegalArgumentException("Khoảng thời gian không hợp lệ.");
        }
        if (first.isAfter(last))
            throw new IllegalArgumentException("Từ ngày phải trước hoặc bằng Đến ngày.");
        if (last.getYear() > 9998)
            throw new IllegalArgumentException("Năm phải nhỏ hơn 9999.");
        return new LocalDate[] { first, last };
    }

    // Tải dữ liệu cho bảng hiện tại.
    private void load() {
        // đánh dấu lần yêu cầu thống kê hiện tại.
        final int version = ++requestVersion;
        // Chọn sô loại thống kê trên giao diện
        final int type = cmbCategory.getSelectedIndex();
        // Đối chiếu với loại cũ bên DAO
        final int oldType = loaiCu(type);
        // Có thống kê theo từng phương tiện?
        final boolean theoMa = type == 5 && chkTheoMa.isSelected();
        final String subFilter = (type == 4 || type == 5) && cmbSubtype.getSelectedItem() != null
                ? cmbSubtype.getSelectedItem().toString()
                : "Tất cả";
        final LocalDate[] range;
        try {
            range = dateRange(cmbPeriod.getSelectedIndex(), (Integer) cmbYear.getSelectedItem(),
                    cmbMonth.getSelectedIndex() + 1, cmbQuarter.getSelectedIndex() + 1,
                    pnlStart.getDateText(), pnlEnd.getDateText());
        } catch (Exception ex) {
            clear("Khoảng thời gian chưa hợp lệ.");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Kiểm tra thời gian",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean daily = cmbPeriod.getSelectedIndex() == 0
                || (cmbPeriod.getSelectedIndex() == 3 && ChronoUnit.DAYS.between(range[0], range[1]) <= 62);
        lblChartTitle.setText(type == 0 ? "Xu hướng doanh thu"
                : type == 1 ? "Số lượng khách hàng" : cmbCategory.getSelectedItem().toString());
        lblMetricTitles[0].setText(switch (type) {
            case 0 -> "TỔNG DOANH THU";
            case 1 -> "KHÁCH HÀNG DUY NHẤT";
            case 2 -> "TỔNG TIỀN KHÁCH ĐÃ THANH TOÁN";
            case 3 -> "LƯỢT DỊCH VỤ";
            case 4 -> "SỐ PHỤ TÙNG ĐÃ THAY";
            case 5 -> "SỐ XE SỬA CHỮA";
            case 6 -> "DOANH SỐ ĐÃ THU";
            default -> "GIÁ TRỊ HÓA ĐƠN ĐÃ THU";
        });
        lblMetricTitles[1].setText("SỐ NHÓM CÓ PHÁT SINH");
        lblMetricTitles[2].setText("GIÁ TRỊ NHÓM CAO NHẤT");
        clear("Đang tải số liệu…");
        btnView.setEnabled(false);

        // Chạy sql
        new SwingWorker<ThongKeDAO.Result, Void>() {
            private ThongKeDAO.BangChiTiet bang;

            // Thực hiện truy vấn dữ liệu ở luồng nền.
            @Override
            protected ThongKeDAO.Result doInBackground() throws Exception {
                // Bảng Jtable nhiều cột
                bang = ThongKeDAO.taiBangLoc(oldType, range[0], range[1], subFilter, theoMa);
                // Biểu đồ và 3 ô chỉ số
                return ThongKeDAO.taiBieuDoLoc(oldType, range[0], range[1], daily, subFilter, theoMa);
            }

            // Nhận kết quả rồi hiển thị
            @Override
            protected void done() {
                if (version != requestVersion)
                    return;
                btnView.setEnabled(true);
                try {
                    // get nhận kết quả tính từ doIn..()
                    ThongKeDAO.Result result = get();
                    List<Bucket> buckets = result.buckets();

                    lblMetricValues[0].setText(
                            format(result.total(), (type == 0 || type == 2 || type == 6 || type == 7)));
                    // Tính số nhóm có phát sinh giá trị > 0
                    long soNhom = buckets.stream()
                            .filter(b -> b.value() > 0)
                            .count();

                    lblMetricValues[1].setText(NUMBER.format(soNhom));

                    detailModel.setColumnCount(0);
                    detailModel.addColumn("STT");
                    // Xóa các cột mặc định của Model để tạo bảng mới phù hợp loại thống kê.
                    for (String col : bang.cot())
                        detailModel.addColumn(col);
                    detailModel.setRowCount(0);
                    for (int i = 0; i < bang.dong().size(); i++) {
                        Object[] raw = bang.dong().get(i), row = new Object[raw.length + 1];
                        row[0] = i + 1;
                        for (int j = 0; j < raw.length; j++) {
                            // xử lý số tiền thành 100.000 ko có thập phân
                            if (raw[j] instanceof BigDecimal number) {
                                row[j + 1] = NUMBER
                                        .format(number.setScale(0, java.math.RoundingMode.HALF_UP));
                            } else {
                                row[j + 1] = raw[j];
                            }
                        }
                        // Chuyển từng dòng dữ liệu SQL thành một dòng JTable
                        detailModel.addRow(row);
                    }
                    detailTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                    for (int j = 0; j < detailTable.getColumnCount(); j++)
                        detailTable.getColumnModel().getColumn(j).setPreferredWidth(j == 0 ? 45
                                : Math.max(100, 700 / Math.max(1, detailTable.getColumnCount() - 1)));
                    double peak = buckets.stream().mapToDouble(Bucket::value).max().orElse(0);
                    lblMetricValues[2].setText(format(BigDecimal.valueOf(peak),
                            (type == 0 || type == 2 || type == 6 || type == 7)));
                    // Gửi dữ liệu qua biểu đồ để vẽ
                    // type = 0 (đường), type = 1 (cột dứng), type = 2 (ngang)
                    pnlChart.update(buckets, type <= 1 ? type : 2, daily);
                    lblSummary.setText(DATE.format(range[0]) + " – " + DATE.format(range[1])
                            + (buckets.isEmpty() ? "  •  Không có dữ liệu trong kỳ đã chọn."
                                    : type == 1 ? "  •  Tổng kỳ không cộng trùng khách giữa các mốc." : ""));

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    clear("Quá trình tải thống kê bị gián đoạn.");

                    JOptionPane.showMessageDialog(ManHinhThongKe.this,
                            "Quá trình tải thống kê bị gián đoạn. Vui lòng thử lại.", "Lỗi thống kê",
                            JOptionPane.ERROR_MESSAGE);

                } catch (java.util.concurrent.ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    String message = cause != null && cause.getMessage() != null ? cause.getMessage()
                            : ex.getMessage();

                    clear("Không tải được số liệu. Bạn có thể bấm Xem thống kê để thử lại.");

                    JOptionPane.showMessageDialog(ManHinhThongKe.this, message, "Lỗi thống kê",
                            JOptionPane.ERROR_MESSAGE);
                }

            }
        }.execute();
    }

    // Xóa nội dung hoặc lựa chọn hiện tại.
    private void clear(String message) {
        for (JLabel lblValue : lblMetricValues)
            lblValue.setText("—");
        detailModel.setRowCount(0);
        pnlChart.update(List.of(), cmbCategory.getSelectedIndex(), false);
        lblSummary.setText(message);
        btnView.setEnabled(true);
    }

    // Định dạng tiền
    private static String format(BigDecimal value, boolean money) {
        return NUMBER.format(value) + (money ? " đ" : "");
    }

    // JPanel tự vẽ 3 kiểu biểu đồ: đường cho doanh thu, cột đứng cho khách hàng và
    // thanh ngang.
    private static final class Chart extends JPanel {
        @java.io.Serial
        private static final long serialVersionUID = 1L;
        // Dữ liệu từ DAO trả về
        private ArrayList<Bucket> data = new ArrayList<>();
        // kiểu vẽ
        private int type;
        // theo ngày hay tháng
        private boolean daily;

        // Thiết lập nền, kích thước mặc định và bật tooltip.
        Chart() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(800, 410));
            setToolTipText(" ");
        }

        // Nhận dữ liệu từ DAO qua GUI, lưu kiểu vẽ rồi yêu cầu Swing vẽ lại.
        void update(List<Bucket> buckets, int category, boolean days) {
            data = new ArrayList<>(buckets);
            // Kiểu vẽ
            type = category;
            // Cách hiển thị thời gian
            daily = days;
            setPreferredSize(new Dimension(800,
                    category >= 2 ? Math.max(380, Math.min(12, data.size()) * 36 + 40) : 410));
            revalidate();
            repaint();
        }

        // Vẽ nội dung và trạng thái hiện tại của thành phần.
        @Override
        protected void paintComponent(Graphics graphics) {
            // vẽ nền
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            // làm đường và góc bo mịn hơn
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(GiaoDien.font(12, false));
            if (data.isEmpty()) {
                g.setColor(GiaoDien.MUTED);
                String text = "Chưa có số liệu để vẽ biểu đồ";
                g.drawString(text, Math.max(12, (getWidth() - g.getFontMetrics().stringWidth(text)) / 2),
                        getHeight() / 2);
                g.dispose();
                return;
            }
            if (type >= 2)
                drawHorizontal(g); // vẽ thanh ngang
            else
                drawTimeline(g); // vẽ đường, cột
            g.dispose();
        }

        // Lấy nội dung gợi ý tại vị trí con trỏ.
        @Override
        public String getToolTipText(MouseEvent e) {
            if (data == null || data.isEmpty()) {
                return null;
            }
            // vị trí chuột
            int mouseX = e.getX();
            int mouseY = e.getY();

            if (type >= 2) {
                int count = Math.min(12, data.size());
                int left = Math.min(300, getWidth() / 3);
                int right = getWidth() - 75;
                int slot = Math.max(30, (getHeight() - 24) / count);

                double max = Math.max(1, data.stream().mapToDouble(Bucket::value).max().orElse(1));

                for (int i = 0; i < count; i++) {
                    Bucket item = data.get(i);

                    int y = 12 + i * slot;
                    int barHeight = Math.min(24, slot - 9);
                    int barWidth = (int) ((right - left) * item.value() / max);
                    // chuột nằm trong côt của biểu đồ ngang thì hiện giá trị
                    if (mouseX >= left && mouseX <= left + Math.max(2, barWidth) && mouseY >= y
                            && mouseY <= y + barHeight) {

                        return tooltip(item, false);
                    }
                }

            } else {
                int count = data.size();

                int left = 82;
                int right = Math.max(left + 1, getWidth() - 30);
                int top = 32;
                int bottom = Math.max(top + 1, getHeight() - 62);

                int width = right - left;
                int height = bottom - top;

                double max = data.stream().mapToDouble(Bucket::value).max().orElse(1);

                double axis = type == 1 ? Math.max(4, Math.ceil(max / 4) * 4) : Math.max(1, max * 1.15);

                double slot = width / (double) count;

                for (int i = 0; i < count; i++) {
                    int x = left + (int) ((i + 0.5) * slot);
                    int y = bottom - (int) (height * data.get(i).value() / axis);

                    boolean hovered;

                    if (type == 0) {
                        // Chuột ở gần điểm tròn
                        hovered = Math.abs(mouseX - x) <= 9 && Math.abs(mouseY - y) <= 9;
                    } else {
                        int bar = Math.max(2, Math.min(44, (int) (slot * 0.55)));
                        // chuột trong ô đứng
                        hovered = mouseX >= x - bar / 2 && mouseX <= x - bar / 2 + bar && mouseY >= y
                                && mouseY <= bottom;
                    }

                    if (hovered) {
                        return tooltip(data.get(i), type == 0);
                    }
                }
            }

            return null;
        }

        // Chuyển đổi thành nghiệp vụ tương ứng.
        private String tooltip(Bucket item, boolean money) {
            String label = item.label();
            String value = NUMBER.format(item.value());

            if (money) {
                value += " đ";
            }

            return "<html>" + "<b>" + escapeHtml(label) + "</b>" + "<br>Giá trị: " + escapeHtml(value)
                    + "</html>";
        }

        // Mã hóa ký tự đặc biệt để tạo HTML an toàn.
        private String escapeHtml(String text) {
            return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"",
                    "&quot;");
        }

        // Vẽ đường kẻ ngang trên biểu đồ.
        private void drawHorizontal(Graphics2D g) {
            // tối đa 12 nhóm được vẽ, bảng chi tiết có thể hiện nhiều hơn
            int count = Math.min(12, data.size()), left = Math.min(300, getWidth() / 3),
                    right = getWidth() - 75;
            // Tìm nhóm có giá trị lớn nhất
            double max = Math.max(1, data.stream().mapToDouble(Bucket::value).max().orElse(1));
            // Chia vị trí cho từng hàng
            int slot = Math.max(30, (getHeight() - 24) / count);
            for (int i = 0; i < count; i++) {
                Bucket item = data.get(i);
                int y = 12 + i * slot, barHeight = Math.min(24, slot - 9);
                // tên nhóm và nền thanh
                g.setColor(GiaoDien.TEXT);
                g.drawString(ellipsize(g, item.label(), left - 18), 0, y + 17);
                // thanh nền màu nhạt có chiều rộng tối đa
                g.setColor(new Color(0xEDF3F1));
                g.fillRoundRect(left, y, Math.max(1, right - left), barHeight, 10, 10);
                // chiều dài thanh dữ liệu, giá trị lớn pixel lớn
                int width = (int) ((right - left) * item.value() / max);
                g.setPaint(new GradientPaint(left, y, GiaoDien.TEAL, right, y, GiaoDien.MINT));
                if (width > 0)
                    g.fillRoundRect(left, y, Math.max(2, width), barHeight, 10, 10);
                // Ghi giá trị bên phải mỗi thanh.
                g.setColor(GiaoDien.NAV);
                g.drawString(NUMBER.format(item.value()), right + 12, y + 17);
            }
        }

        // Vẽ biểu đồ theo thời gian: doanh thu dạng đường, số khách dạng cột đứng.
        private void drawTimeline(Graphics2D g) {
            // Tạo vùng vẽ và hệ tọa độ
            int left = 82, right = Math.max(left + 1, getWidth() - 30), top = 32,
                    bottom = Math.max(top + 1, getHeight() - 62);
            int width = right - left, height = bottom - top;

            // Tìm giá trị lớn nhất để chia trục Y
            double max = data.stream().mapToDouble(Bucket::value).max().orElse(1);
            double axis = type == 1 ? Math.max(4, Math.ceil(max / 4) * 4) : Math.max(1, max * 1.15);

            // Vẽ bốn khoảng chia trục Y
            for (int i = 0; i <= 4; i++) {
                int y = bottom - i * height / 4;
                g.setColor(new Color(0xE8EFED));
                // vẽ đường kẻ ngang.
                g.drawLine(left, y, right, y);
                g.setColor(GiaoDien.MUTED);
                // Rút gọn số tiền
                String tick = compact(axis * i / 4);
                // ghi số lên trục Y.
                g.drawString(tick, left - 10 - g.getFontMetrics().stringWidth(tick), y + 4);
            }
            // Tính tọa độ từng điểm dữ liệu
            int count = data.size(), stride = Math.max(1, (int) Math.ceil(count / 10.0));
            double slot = width / (double) count;
            int[] xs = new int[count], ys = new int[count];

            for (int i = 0; i < count; i++) {
                // Tính vị trí điểm để vẽ
                xs[i] = left + (int) ((i + .5) * slot);
                ys[i] = bottom - (int) (height * data.get(i).value() / axis);
                // Ghi nhãn ngày hoặc tháng dưới biểu đồ
                if (i % stride == 0 || i == count - 1) {
                    String raw = data.get(i).label();
                    String label = daily ? raw.substring(8, 10) + "/" + raw.substring(5, 7)
                            : raw.substring(5, 7) + "/" + raw.substring(0, 4);
                    g.setColor(GiaoDien.MUTED);
                    g.drawString(label, xs[i] - g.getFontMetrics().stringWidth(label) / 2, bottom + 24);
                }
            }
            // Nhánh vẽ doanh thu
            if (type == 0) {
                // Tạo vùng tô bên dưới đường
                Path2D area = new Path2D.Double();
                area.moveTo(xs[0], bottom);
                for (int i = 0; i < count; i++)
                    area.lineTo(xs[i], ys[i]);
                area.lineTo(xs[count - 1], bottom);
                area.closePath();
                // tạo màu chuyển sắc
                g.setPaint(new GradientPaint(0, top, new Color(0x83B4AE), 0, bottom, new Color(0xF4F9F7)));
                g.fill(area);

                // Vẽ đường nối các điểm
                g.setColor(GiaoDien.TEAL);
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 1; i < count; i++)
                    g.drawLine(xs[i - 1], ys[i - 1], xs[i], ys[i]);

                for (int i = 0; i < count; i++)
                    if (count <= 35 || i % stride == 0 || i == count - 1) {
                        // Vẽ những chấm tròn trên đường
                        g.setColor(Color.WHITE);
                        // tạo hình tròn màu trắng
                        g.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
                        g.setColor(GiaoDien.TEAL);
                        // tạo viền xanh
                        g.drawOval(xs[i] - 4, ys[i] - 4, 8, 8);
                    }
            } else {
                // Nhánh vẽ số lượng khách hàng
                // chiều rộng mỗi cột, tối đa 44 px.
                // vẽ hình chữ nhật đứng từ điểm xuống trục X.
                int bar = Math.max(2, Math.min(44, (int) (slot * .55)));
                for (int i = 0; i < count; i++) {
                    g.setPaint(new GradientPaint(0, ys[i], GiaoDien.TEAL, 0, bottom, GiaoDien.MINT));
                    if (ys[i] < bottom)
                        g.fillRoundRect(xs[i] - bar / 2, ys[i], bar, bottom - ys[i], 8, 8);
                    if (count <= 15) {
                        g.setColor(GiaoDien.NAV);
                        String value = NUMBER.format(data.get(i).value());
                        g.drawString(value, xs[i] - g.getFontMetrics().stringWidth(value) / 2, ys[i] - 9);
                    }
                }
            }
        }

        // Rút gọn cách hiển thị giá trị số.
        private static String compact(double value) {
            if (value >= 1_000_000_000)
                return String.format(Locale.forLanguageTag("vi-VN"), "%.1f tỷ", value / 1_000_000_000);
            if (value >= 1_000_000)
                return String.format(Locale.forLanguageTag("vi-VN"), "%.1f tr", value / 1_000_000);
            if (value >= 1_000)
                return String.format(Locale.forLanguageTag("vi-VN"), "%.0f nghìn", value / 1_000);
            return NUMBER.format(value);
        }

        // Rút gọn văn bản dài để vừa vùng hiển thị.
        private static String ellipsize(Graphics2D g, String value, int width) {
            if (g.getFontMetrics().stringWidth(value) <= width)
                return value;
            int end = value.length();
            while (end > 0 && g.getFontMetrics().stringWidth(value.substring(0, end) + "…") > width)
                end--;
            return value.substring(0, end) + "…";
        }
    }

    private static final class GiaoDien {

        private static final Color NAV = new Color(0x13272E);
        private static final Color TEAL = new Color(0x487674);
        private static final Color MINT = new Color(0x83B4AE);
        private static final Color BG = new Color(0xF4F7F6);
        private static final Color WHITE = Color.WHITE;
        private static final Color TEXT = new Color(0x21383C);
        private static final Color MUTED = new Color(0x718387);

        // ===== PHƯƠNG THỨC: font(int, boolean) — Tạo phông chữ theo cỡ và kiểu chữ.
        // =====
        public static Font font(int size, boolean bold) {
            return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
        }

        // ===== PHƯƠNG THỨC: rounded(Color, int) — Tạo đường viền bo góc cho thành
        // phần. =====
        public static JPanel rounded(Color c, int r) {
            return new JPanel() {
                private static final long serialVersionUID = 1L;

                // ===== PHƯƠNG THỨC: paintComponent(Graphics) — Vẽ nội dung và trạng thái hiện
                // tại của thành phần. =====
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

        // ===== PHƯƠNG THỨC: button(String, Color, Color) — Tạo nút giao diện theo kiểu
        // chỉ định. =====
        public static JButton button(String s, Color bg, Color fg) {
            JButton btnB = new JButton(s) {
                private static final long serialVersionUID = 1L;

                // ===== PHƯƠNG THỨC: paintComponent(Graphics) — Vẽ nội dung và trạng thái hiện
                // tại của thành phần. =====
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

        // ===== PHƯƠNG THỨC: label(String, int, boolean, Color) — Tạo nhãn giao diện.
        // =====
        public static JLabel label(String s, int size, boolean bold, Color c) {
            JLabel lblL = new JLabel(s);
            lblL.setFont(font(size, bold));
            lblL.setForeground(c);
            return lblL;
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
}
