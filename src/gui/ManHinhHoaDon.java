package gui;

import connectDB.KetNoiCSDL;
import dao.ThuongMaiDAO;
import java.awt.*;
import java.awt.print.PrinterException;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** Bố cục hóa đơn cũ, nội dung và bảng chi tiết lấy trực tiếp từ SQL Server. */
public final class ManHinhHoaDon extends JPanel {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private final JComboBox<String> cmbInvoices = new OChonTimKiem<>();
    private final JButton btnPay = GiaoDien.button("Thanh toán", GiaoDien.ORANGE, Color.WHITE);
    private final JComboBox<LuaChonDuLieu.Choice> cmbCoupon = new OChonTimKiem<>();
    private final String fixedInvoice;
    private boolean loadingCoupons;
    private String currentCoupon="";
    private final JLabel lblCoupon = GiaoDien.label("",12,true,GiaoDien.TEAL);
    private boolean loadingInvoices;
    private final JLabel[] lblInfo = new JLabel[6];
    private final JLabel[] lblTotals = new JLabel[5];
    private final JTextField txtLoyalty=GiaoDien.field("Quét QR mã khách hàng rồi Enter");
    private final JLabel lblLoyalty=GiaoDien.label("Chưa quét mã — không tích điểm.",12,true,GiaoDien.TEAL);
    private String scannedCustomer,invoiceCustomer,loadedInvoice;
    private final DefaultTableModel services = model();
    private final DefaultTableModel parts = model();
    private final NumberFormat money = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    public ManHinhHoaDon() {
        this(null);
    }
    public ManHinhHoaDon(String selectedInvoice) {
        fixedInvoice=selectedInvoice;
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);
        setBorder(new EmptyBorder(24, 28, 28, 28));
        JPanel pnlRoot = GiaoDien.stack(0);
        JLabel lblTitle = GiaoDien.label("Hóa đơn thanh toán", 26, true, GiaoDien.NAV);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlRoot.add(lblTitle);
        GiaoDien.gap(pnlRoot, 10);
        JPanel pnlChoose = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlChoose.setOpaque(false);
        if(selectedInvoice==null) pnlChoose.add(GiaoDien.label("Chọn mã hóa đơn", 13, true, GiaoDien.TEAL));
        cmbInvoices.setPreferredSize(new Dimension(190, 36));
        if(selectedInvoice==null) pnlChoose.add(cmbInvoices);
        JButton btnRefresh = GiaoDien.button("Làm mới", GiaoDien.CREAM, GiaoDien.NAV);
        btnRefresh.addActionListener(e -> { if(selectedInvoice==null) loadInvoices(); else loadSelected(); });
        pnlChoose.add(btnRefresh);
        btnPay.setEnabled(false);
        btnPay.addActionListener(e -> openPayment());
        pnlChoose.add(btnPay);
        JButton btnPrint = GiaoDien.button("In hóa đơn", GiaoDien.TEAL, Color.WHITE);
        btnPrint.addActionListener(e -> printInvoice()); pnlChoose.add(btnPrint);
        cmbCoupon.setEnabled(false); cmbCoupon.setPreferredSize(new Dimension(330,36));
        cmbCoupon.addActionListener(e -> changeCoupon());
        pnlRoot.add(pnlChoose);
        JPanel pnlCoupon=new JPanel(new FlowLayout(FlowLayout.LEFT,10,5)); pnlCoupon.setOpaque(false);
        pnlCoupon.add(GiaoDien.label("Mã giảm giá",13,true,GiaoDien.TEAL)); pnlCoupon.add(cmbCoupon); pnlRoot.add(pnlCoupon);
        lblCoupon.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlRoot.add(lblCoupon);
        JPanel pnlLoyalty=new JPanel(new BorderLayout(8,6));pnlLoyalty.setOpaque(false);
        pnlLoyalty.add(GiaoDien.label("Quét mã tích điểm",13,true,GiaoDien.TEAL),BorderLayout.WEST);
        txtLoyalty.setPreferredSize(new Dimension(300,34));pnlLoyalty.add(txtLoyalty,BorderLayout.CENTER);
        JButton btnClearScan=GiaoDien.button("Bỏ tích điểm",GiaoDien.CREAM,GiaoDien.NAV);pnlLoyalty.add(btnClearScan,BorderLayout.EAST);
        pnlLoyalty.add(lblLoyalty,BorderLayout.SOUTH);pnlRoot.add(pnlLoyalty);
        txtLoyalty.addActionListener(e -> scanCustomer());
        btnClearScan.addActionListener(e -> {scannedCustomer=null;txtLoyalty.setText("");lblLoyalty.setText("Chưa quét mã — không tích điểm.");});
        GiaoDien.gap(pnlRoot, 12);

        JPanel pnlCard = GiaoDien.card(new GridLayout(2, 3, 18, 14));
        String[] keys = {"SỐ HÓA ĐƠN", "KHÁCH HÀNG", "BIỂN SỐ/VIN", "PHIẾU SỬA CHỮA", "NGÀY LẬP", "TRẠNG THÁI"};
        for (int i = 0; i < keys.length; i++) {
            JPanel pnlItem = new JPanel(new GridLayout(2, 1));
            pnlItem.setOpaque(false);
            pnlItem.add(GiaoDien.label(keys[i], 11, true, GiaoDien.MUTED));
            lblInfo[i] = GiaoDien.label("—", 14, true, GiaoDien.TEXT);
            pnlItem.add(lblInfo[i]);
            pnlCard.add(pnlItem);
        }
        pnlRoot.add(pnlCard);
        GiaoDien.gap(pnlRoot, 16);
        pnlRoot.add(tableCard("I. DỊCH VỤ/TIỀN CÔNG", services));
        GiaoDien.gap(pnlRoot, 14);
        pnlRoot.add(tableCard("II. PHỤ TÙNG SỬ DỤNG", parts));
        GiaoDien.gap(pnlRoot, 16);
        JPanel pnlTotalCard = GiaoDien.card(new GridLayout(5, 2, 8, 8));
        String[] labels = {"Tổng tiền công/dịch vụ", "Tổng tiền phụ tùng", "Thuế VAT 8%", "Giảm giá", "TỔNG THANH TOÁN"};
        for (int i = 0; i < labels.length; i++) {
            pnlTotalCard.add(GiaoDien.label(labels[i], 13, i == 4, GiaoDien.TEXT));
            lblTotals[i] = GiaoDien.label("0 đ", 15, true, i == 4 ? GiaoDien.ORANGE : GiaoDien.TEAL);
            lblTotals[i].setHorizontalAlignment(SwingConstants.RIGHT);
            pnlTotalCard.add(lblTotals[i]);
        }
        pnlRoot.add(pnlTotalCard);
        GiaoDien.gap(pnlRoot, 14);
        add(pnlRoot, BorderLayout.NORTH);
        cmbInvoices.addActionListener(e -> loadSelected());
        if(selectedInvoice==null) loadInvoices();
        else { loadingInvoices=true; cmbInvoices.addItem(selectedInvoice); loadingInvoices=false; loadSelected(); }
    }
    @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (getParent() instanceof JViewport vp && vp.getWidth() > 0) return new Dimension(vp.getWidth(), d.height);
        return d;
    }
    private String invoiceId() { return fixedInvoice!=null ? fixedInvoice : (String)cmbInvoices.getSelectedItem(); }
    void openPayment() {
        if(btnPay.isEnabled()) HopThoaiThanhToan.show(this,invoiceId(),() -> {
            loadSelected();
            if("Đã thanh toán".equals(lblInfo[5].getText()) && JOptionPane.showConfirmDialog(this,"Đã thanh toán. In hóa đơn ngay?","In hóa đơn",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) printInvoice();
        },scannedCustomer);
    }

    private DefaultTableModel model() {
        return new DefaultTableModel(new String[] {"STT", "Mã", "Tên", "Số lượng", "Đơn giá", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private JPanel tableCard(String title, DefaultTableModel model) {
        JPanel pnlCard = GiaoDien.card(new BorderLayout(0, 12));
        pnlCard.add(GiaoDien.label(title, 16, true, GiaoDien.NAV), BorderLayout.NORTH);
        JTable tblTable = GiaoDien.table(model);
        tblTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrScroll = new JScrollPane(tblTable);
        scrScroll.setPreferredSize(new Dimension(900, 130));
        BoCucBang.install(tblTable, scrScroll);
        pnlCard.add(scrScroll, BorderLayout.CENTER);
        return pnlCard;
    }

    private String format(double value) {
        return money.format(value) + " đ";
    }

    private void loadInvoices() {
        String old = (String) cmbInvoices.getSelectedItem();
        loadingInvoices = true;
        cmbInvoices.removeAllItems();
        try (Connection connection = KetNoiCSDL.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT maHoaDon FROM dbo.HoaDon ORDER BY maHoaDon ASC");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) cmbInvoices.addItem(result.getString(1));
            if (old != null) {
                for (int i = 0; i < cmbInvoices.getItemCount(); i++) {
                    if (old.equals(cmbInvoices.getItemAt(i))) { cmbInvoices.setSelectedIndex(i); break; }
                }
            }
        } catch (SQLException ex) {
            cmbInvoices.removeAllItems();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi tải hóa đơn", JOptionPane.ERROR_MESSAGE);
        }
        loadingInvoices = false;
        loadSelected();
    }

    private void loadSelected() {
        if (loadingInvoices) return;
        btnPay.setEnabled(false);
        cmbCoupon.setEnabled(false); lblCoupon.setText("");
        String id = invoiceId();
        txtLoyalty.setEnabled(false);
        if(!java.util.Objects.equals(id,loadedInvoice)) {scannedCustomer=null;txtLoyalty.setText("");}loadedInvoice=id;
        services.setRowCount(0);
        parts.setRowCount(0);
        for (JLabel lblLabel : lblInfo) lblLabel.setText("—");
        for (JLabel lblLabel : lblTotals) lblLabel.setText("—");
        if (id == null) return;
        String sql = "SELECT h.*, k.hoTenKH FROM dbo.HoaDon h "
                + "LEFT JOIN dbo.KhachHang k ON h.maKhachHang = k.maKhachHang "
                + "WHERE h.maHoaDon = ?";
        try (Connection connection = KetNoiCSDL.getConnection()) {
            String customerId=null,couponId=null;
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, id);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        customerId=result.getString("maKhachHang");
                        invoiceCustomer=customerId;
                        lblInfo[0].setText(id);
                        String customerName=result.getString("hoTenKH");
                        lblInfo[1].setText(customerId+(customerName==null || customerName.isBlank() ? "" : " — "+customerName));
                        lblInfo[1].setToolTipText(lblInfo[1].getText());
                        String vin=result.getString("VIN"),repair=result.getString("maPSC");
                        lblInfo[2].setText(vin==null ? "—" : vin);
                        lblInfo[3].setText(repair==null ? "Bán phụ tùng" : repair);
                        lblInfo[4].setText(String.valueOf(result.getDate("ngayLap")));
                        lblInfo[5].setText(result.getString("trangThaiThanhToan"));
                        String coupon=result.getString("maGiamGia");
                        couponId=coupon;
                        lblCoupon.setText("Mã giảm giá: "+(coupon==null ? "Chưa áp dụng" : coupon));
                        java.math.BigDecimal labor=result.getBigDecimal("tienCong").add(result.getBigDecimal("dichVuKhac"));
                        java.math.BigDecimal part=result.getBigDecimal("tienPhuTung"),vat=result.getBigDecimal("thueVAT"),discount=result.getBigDecimal("giamGia");
                        lblTotals[0].setText(money.format(labor)+" đ");lblTotals[1].setText(money.format(part)+" đ");
                        lblTotals[2].setText(money.format(vat)+" đ");lblTotals[3].setText("− "+money.format(discount)+" đ");
                        lblTotals[4].setText(money.format(labor.add(part).add(vat).subtract(discount))+" đ");
                    } else throw new SQLException("Không tìm thấy hóa đơn "+id+".");
                }
            }
            String detailSql = "SELECT ct.maDichVu, dv.tenDichVu, ct.maPhuTung, pt.tenPhuTung, "
                    + "ct.soLuong, ct.donGia FROM dbo.ChiTietHoaDon ct "
                    + "LEFT JOIN dbo.DichVu dv ON dv.maDichVu = ct.maDichVu "
                    + "LEFT JOIN dbo.PhuTung pt ON pt.maPhuTung = ct.maPhuTung "
                    + "WHERE ct.maHoaDon = ? ORDER BY ct.maDichVu";
            try (PreparedStatement statement = connection.prepareStatement(detailSql)) {
                statement.setString(1, id);
                try (ResultSet result = statement.executeQuery()) {
                    int serviceIndex = 1;
                    int partIndex = 1;
                    while (result.next()) {
                        int quantity = result.getInt("soLuong");
                        double price = result.getDouble("donGia");
                        String partCode = result.getString("maPhuTung");
                        if (partCode == null || partCode.isEmpty()) {
                            services.addRow(new Object[] {serviceIndex++, result.getString("maDichVu"),
                                    result.getString("tenDichVu"), quantity, format(price), format(price * quantity)});
                        } else {
                            parts.addRow(new Object[] {partIndex++, partCode,
                                    result.getString("tenPhuTung"), quantity, format(price), format(price * quantity)});
                        }
                    }
                }
            }
            btnPay.setEnabled(lblInfo[5].getText().equals("Chưa thanh toán") || lblInfo[5].getText().equals("Chờ thanh toán"));
            loadCoupons(connection,customerId,couponId);
            cmbCoupon.setEnabled(btnPay.isEnabled());
            txtLoyalty.setEnabled(btnPay.isEnabled());
            if(!btnPay.isEnabled()) {scannedCustomer=null;lblLoyalty.setText("Hóa đơn đã thanh toán — không quét tích điểm bổ sung.");}
            else if(scannedCustomer==null) lblLoyalty.setText("Chưa quét mã — không tích điểm.");
        } catch (SQLException ex) {
            btnPay.setEnabled(false);
            cmbCoupon.setEnabled(false); lblCoupon.setText("Không tải được chi tiết hóa đơn.");
            services.setRowCount(0);
            parts.setRowCount(0);
            for (JLabel lblLabel : lblInfo) lblLabel.setText("—");
            for (JLabel lblLabel : lblTotals) lblLabel.setText("—");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi đọc chi tiết hóa đơn", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printInvoice() {
        if (invoiceId() == null || lblInfo[0].getText().equals("—")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn đã tải thành công.");
            return;
        }
        try {
            boolean printed = printAllPages();
            if (!printed) return;
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi in hóa đơn", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void changeCoupon() {
        String invoice=invoiceId();
        if(loadingCoupons || invoice==null || !cmbCoupon.isEnabled()) return;
        Object value=cmbCoupon.getSelectedItem();
        if(!(value instanceof LuaChonDuLieu.Choice choice) || choice.id().equals(currentCoupon)) return;
        cmbCoupon.setEnabled(false);
        try { ThuongMaiDAO.changeCoupon(invoice,choice.id()); loadSelected(); }
        catch(SQLException ex) { JOptionPane.showMessageDialog(this,ex.getMessage(),"Không thể áp dụng mã giảm giá",JOptionPane.ERROR_MESSAGE); loadSelected(); }
    }
    private void scanCustomer() {
        String code=txtLoyalty.getText().trim().toUpperCase(Locale.ROOT); scannedCustomer=null;
        if(!txtLoyalty.isEnabled() || !code.matches("KH[0-9]{3}") || !code.equals(invoiceCustomer)) {
            lblLoyalty.setText("Mã quét không khớp khách hàng của hóa đơn — chưa đăng ký tích điểm.");return;
        }
        scannedCustomer=code;lblLoyalty.setText("Đã nhận: "+lblInfo[1].getText()+" — tích điểm sau thanh toán thành công.");txtLoyalty.setText("");
    }
    private void loadCoupons(Connection connection,String customer,String applied) throws SQLException {
        loadingCoupons=true;
        try {
            currentCoupon=applied==null ? "" : applied;
            cmbCoupon.removeAllItems(); cmbCoupon.addItem(new LuaChonDuLieu.Choice("","Không áp dụng"));
            String query="SELECT g.maGiamGia,g.mucGiam FROM dbo.MaGiamGia g WHERE g.maKhachHang=? AND ((g.trangThai=N'Đang hiệu lực' AND g.ngayTao<=SYSDATETIME() AND g.ngayHetHan>=SYSDATETIME() AND NOT EXISTS(SELECT 1 FROM dbo.HoaDon h WHERE h.maGiamGia=g.maGiamGia AND h.trangThaiThanhToan=N'Đã thanh toán')) OR g.maGiamGia=?) ORDER BY g.maGiamGia";
            try(PreparedStatement ps=connection.prepareStatement(query)) {
                ps.setString(1,customer); ps.setString(2,currentCoupon);
                try(ResultSet rs=ps.executeQuery()) { while(rs.next()) cmbCoupon.addItem(new LuaChonDuLieu.Choice(rs.getString(1),rs.getString(1)+" — Giảm "+rs.getBigDecimal(2).stripTrailingZeros().toPlainString()+"%")); }
            }
            cmbCoupon.setSelectedIndex(0);
            for(int i=0;i<cmbCoupon.getItemCount();i++) if(cmbCoupon.getItemAt(i).id().equals(currentCoupon)) { cmbCoupon.setSelectedIndex(i); break; }
        } finally { loadingCoupons=false; }
    }

    private boolean printAllPages() throws PrinterException {
        // Print a separate document so controls and scroll viewports never enter the output.
        JEditorPane document = new JEditorPane("text/html", invoicePrintHtml());
        document.setEditable(false);
        document.setBackground(Color.WHITE);
        document.setBorder(null);
        document.setSize(760, Short.MAX_VALUE);
        document.setSize(760, document.getPreferredSize().height);
        java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
        job.setJobName("Hoa don " + invoiceId());
        java.awt.print.Paper paper = new java.awt.print.Paper();
        double width = 210 * 72.0 / 25.4, height = 297 * 72.0 / 25.4;
        paper.setSize(width, height);
        paper.setImageableArea(36, 36, width - 72, height - 72);
        java.awt.print.PageFormat format = job.defaultPage();
        format.setPaper(paper);
        job.setPrintable((graphics, page, index) -> {
            if (index != 0) return java.awt.print.Printable.NO_SUCH_PAGE;
            Graphics2D g = (Graphics2D) graphics.create();
            try {
                double scale = Math.min(page.getImageableWidth() / document.getWidth(),
                        page.getImageableHeight() / Math.max(1, document.getHeight()));
                g.translate(page.getImageableX(), page.getImageableY());
                g.scale(scale, scale);
                document.printAll(g);
            } finally { g.dispose(); }
            return java.awt.print.Printable.PAGE_EXISTS;
        }, job.validatePage(format));
        if (!job.printDialog()) return false;
        job.print();
        return true;
    }

    private String invoicePrintHtml() {
        StringBuilder html = new StringBuilder("<html><head><style>"
                + "body { font-family: Arial; font-size: 9pt; color: #172f3a; margin: 0; }"
                + "h1 { text-align: center; font-size: 16pt; margin: 0 0 8pt 0; }"
                + "h2 { font-size: 10pt; margin: 8pt 0 4pt 0; }"
                + "p { margin: 4pt 0; }"
                + "table { width: 100%; } td, th { padding: 3px; }"
                + "th { background-color: #edf3f2; }"
                + "</style></head><body><h1>Hóa đơn thanh toán</h1><table>");
        String[] keys = {"Số hóa đơn", "Khách hàng", "Biển số/VIN", "Phiếu sửa chữa", "Ngày lập", "Trạng thái"};
        int[] order = {0, 4, 1, 3, 2, 5};
        for (int row = 0; row < order.length; row += 2) {
            html.append("<tr>");
            for (int column = row; column < row + 2; column++) {
                int i = order[column];
                html.append("<td width='50%'><b>").append(keys[i]).append(": </b>")
                        .append(escapeHtml(lblInfo[i].getText())).append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</table><p>").append(escapeHtml(lblCoupon.getText())).append("</p>");
        appendPrintTable(html, "I. DỊCH VỤ/TIỀN CÔNG", services);
        appendPrintTable(html, "II. PHỤ TÙNG SỬ DỤNG", parts);
        html.append("<h2>TỔNG KẾT</h2><table>");
        String[] totals = {"Tổng tiền công/dịch vụ", "Tổng tiền phụ tùng", "Thuế VAT 8%", "Giảm giá", "TỔNG THANH TOÁN"};
        for (int i = 0; i < totals.length; i++) {
            html.append("<tr><td><b>").append(totals[i]).append("</b></td><td align='right'>")
                    .append(escapeHtml(lblTotals[i].getText())).append("</td></tr>");
        }
        return html.append("</table></body></html>").toString();
    }

    private static void appendPrintTable(StringBuilder html, String title, DefaultTableModel model) {
        html.append("<h2>").append(title).append("</h2><table border='1' cellspacing='0'><tr>");
        for (int column = 0; column < model.getColumnCount(); column++) {
            html.append("<th>").append(escapeHtml(model.getColumnName(column))).append("</th>");
        }
        html.append("</tr>");
        for (int row = 0; row < model.getRowCount(); row++) {
            html.append("<tr>");
            for (int column = 0; column < model.getColumnCount(); column++) {
                Object value = model.getValueAt(row, column);
                html.append("<td align='center'>").append(escapeHtml(value == null ? "" : value.toString())).append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</table>");
    }

    private static String escapeHtml(String text) {
        return text == null ? "" : text.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
