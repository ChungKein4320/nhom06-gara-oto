package gui;

import connectDB.KetNoiCSDL;
import dao.ThuongMaiDAO;
import entity.HoaDon;
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
    private final java.util.Map<String, String> printInfo = new java.util.LinkedHashMap<>();
    private ThongTinCuaHang store;
    private boolean invoiceReady;
    private final JLabel lblIssuer = GiaoDien.label("", 13, true, GiaoDien.TEXT);
    private final JLabel[] lblInfo = new JLabel[6];
    private final JPanel invoiceInfoCard = GiaoDien.card(new GridLayout(2, 3, 18, 14));
    private JPanel repairInfoItem;
    private final JLabel[] lblTotals = new JLabel[5];
    private final JTextField txtLoyalty=GiaoDien.field("Quét QR mã khách hàng rồi Enter");
    private final JLabel lblLoyalty=GiaoDien.label("Chưa quét mã — không tích điểm.",12,true,GiaoDien.TEAL);
    private String scannedCustomer,invoiceCustomer,loadedInvoice;
    private final DefaultTableModel services = model();
    private final NumberFormat money = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    private final DefaultTableModel parts = new DefaultTableModel(
            new String[]{
                "STT", "Mã", "Tên", "Số lượng",
                "Đơn giá", "Thành tiền", "Ngày hết hạn bảo hành"
            },
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

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
        JButton btnStore = GiaoDien.button("Thông tin cửa hàng", GiaoDien.CREAM, GiaoDien.NAV);
        btnStore.addActionListener(e -> {
            try { store = new ThongTinCuaHang(); store.edit(this); }
            catch (java.io.IOException ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi lưu thông tin cửa hàng", JOptionPane.ERROR_MESSAGE); }
        });
        pnlChoose.add(btnStore);
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

        JPanel pnlCard = invoiceInfoCard;
        String[] keys = {"SỐ HÓA ĐƠN", "KHÁCH HÀNG", "BIỂN SỐ/VIN", "PHIẾU SỬA CHỮA", "NGÀY LẬP", "TRẠNG THÁI"};
        for (int i = 0; i < keys.length; i++) {
            JPanel pnlItem = new JPanel(new GridLayout(2, 1));
            pnlItem.setOpaque(false);
            pnlItem.add(GiaoDien.label(keys[i], 11, true, GiaoDien.MUTED));
            lblInfo[i] = GiaoDien.label("—", 14, true, GiaoDien.TEXT);
            pnlItem.add(lblInfo[i]);
            if (i == 3) repairInfoItem = pnlItem;
            pnlCard.add(pnlItem);
        }
        pnlRoot.add(pnlCard);
        pnlRoot.add(lblIssuer);
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
        invoiceReady = false;
        showRepairInfo(true);
        printInfo.clear(); lblIssuer.setText("");
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
        String sql = "SELECT h.*, k.hoTenKH, k.soDienThoai AS dienThoaiKH, k.email AS emailKH, k.diaChi AS diaChiKH, "
                + "n.hoTenNV, n.vaiTro, p.bienSo, p.hangXe, p.dongXe, t.tenPhuongThuc FROM dbo.HoaDon h "
                + "LEFT JOIN dbo.KhachHang k ON h.maKhachHang = k.maKhachHang "
                + "LEFT JOIN dbo.NhanVien n ON n.maNhanVien=h.maNhanVien "
                + "LEFT JOIN dbo.PhuongTien p ON p.VIN=h.VIN "
                + "LEFT JOIN dbo.PhuongThucThanhToan t ON t.maPhuongThuc=h.maPhuongThuc "
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
                        String plate = result.getString("bienSo");
                        lblInfo[2].setText((plate == null ? "" : plate + " / ") + (vin == null ? "—" : vin));
                        printInfo.put("Nhân viên lập", display(result.getString("maNhanVien")) + " — " + display(result.getString("hoTenNV")));
                        printInfo.put("Vai trò", display(result.getString("vaiTro")));
                        printInfo.put("Điện thoại khách hàng", display(result.getString("dienThoaiKH")));
                        printInfo.put("Địa chỉ khách hàng", display(result.getString("diaChiKH")));
                        printInfo.put("Email khách hàng", display(result.getString("emailKH")));
                        printInfo.put("Hãng / dòng xe", vin == null ? "Không áp dụng" : display(result.getString("hangXe")) + " / " + display(result.getString("dongXe")));
                        printInfo.put("Phương thức thanh toán", display(result.getString("tenPhuongThuc")));
                        printInfo.put("Loại hóa đơn", repair == null ? "Bán phụ tùng" : "Dịch vụ sửa chữa");
                        lblIssuer.setText("Nhân viên lập: " + printInfo.get("Nhân viên lập") + "    |    Thanh toán: " + printInfo.get("Phương thức thanh toán"));
                        lblInfo[3].setText(repair==null ? "Bán phụ tùng" : repair);
                        showRepairInfo(repair != null);
                        lblInfo[4].setText(result.getTimestamp("ngayLap").toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                        lblInfo[5].setText(result.getString("trangThaiThanhToan"));
                        String coupon=result.getString("maGiamGia");
                        couponId=coupon;
                        lblCoupon.setText("Mã giảm giá: "+(coupon==null ? "Chưa áp dụng" : coupon));
                        java.math.BigDecimal labor=result.getBigDecimal("tienCong").add(result.getBigDecimal("dichVuKhac"));
                        java.math.BigDecimal part=result.getBigDecimal("tienPhuTung"),vat=result.getBigDecimal("thueVAT"),discount=result.getBigDecimal("giamGia");
                        HoaDon hoaDon=new HoaDon();
                        hoaDon.setTienPhuTung(part);
                        hoaDon.setTienCong(result.getBigDecimal("tienCong"));
                        hoaDon.setDichVuKhac(result.getBigDecimal("dichVuKhac"));
                        hoaDon.setThueVAT(vat);
                        hoaDon.setGiamGia(discount);
                        lblTotals[0].setText(money.format(labor)+" đ");lblTotals[1].setText(money.format(part)+" đ");
                        lblTotals[2].setText(money.format(vat)+" đ");lblTotals[3].setText("− "+money.format(discount)+" đ");
                        lblTotals[4].setText(money.format(hoaDon.tongThanhToan())+" đ");
                    } else throw new SQLException("Không tìm thấy hóa đơn "+id+".");
                }
            }
            String detailSql =
                "SELECT ct.maDichVu, dv.tenDichVu, "
                + "ct.maPhuTung, pt.tenPhuTung, "
                + "ct.soLuong, ct.donGia, "
                + "sc.maPSC AS maPSCBoHanh, "
                + "sc.thoiHanBaoHanh, sc.ngayHetHanBaoHanh "
                + "FROM dbo.ChiTietHoaDon ct "
                + "JOIN dbo.HoaDon h ON h.maHoaDon=ct.maHoaDon "
                + "LEFT JOIN dbo.DichVu dv ON dv.maDichVu=ct.maDichVu "
                + "LEFT JOIN dbo.PhuTung pt ON pt.maPhuTung=ct.maPhuTung "
                + "LEFT JOIN dbo.ChiTietPhieuSuaChua sc "
                + "ON sc.maPSC=h.maPSC "
                + "AND sc.maDichVu=ct.maDichVu "
                + "AND sc.maPhuTung=ct.maPhuTung "
                + "WHERE ct.maHoaDon=? "
                + "ORDER BY ct.maDichVu, ct.maPhuTung";
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
                            Timestamp warrantyEnd = result.getTimestamp("ngayHetHanBaoHanh");
                            String warrantyText;

                            if (result.getString("maPSCBoHanh") == null) {
                                warrantyText = "—";
                            } else if (result.getInt("thoiHanBaoHanh") == 0) {
                                warrantyText = "Không bảo hành";
                            } else if (warrantyEnd == null) {
                                warrantyText = "Chưa xác định";
                            } else {
                                warrantyText = warrantyEnd.toLocalDateTime().format(
                                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                );
                            }

                            parts.addRow(new Object[]{
                                partIndex++,
                                partCode,
                                result.getString("tenPhuTung"),
                                quantity,
                                format(price),
                                format(price * quantity),
                                warrantyText
                            });
                        }
                    }
                }
            }
            invoiceReady = true;
            btnPay.setEnabled(lblInfo[5].getText().equals("Chưa thanh toán") || lblInfo[5].getText().equals("Chờ thanh toán"));
            loadCoupons(connection,customerId,couponId);
            cmbCoupon.setEnabled(btnPay.isEnabled());
            txtLoyalty.setEnabled(btnPay.isEnabled());
            if(!btnPay.isEnabled()) {scannedCustomer=null;lblLoyalty.setText("Hóa đơn đã thanh toán — không quét tích điểm bổ sung.");}
            else if(scannedCustomer==null) lblLoyalty.setText("Chưa quét mã — không tích điểm.");
        } catch (SQLException ex) {
            invoiceReady = false;
            btnPay.setEnabled(false);
            cmbCoupon.setEnabled(false); lblCoupon.setText("Không tải được chi tiết hóa đơn.");
            services.setRowCount(0);
            parts.setRowCount(0);
            for (JLabel lblLabel : lblInfo) lblLabel.setText("—");
            for (JLabel lblLabel : lblTotals) lblLabel.setText("—");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi đọc chi tiết hóa đơn", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRepairInfo(boolean visible) {
        if (visible && repairInfoItem.getParent() == null) invoiceInfoCard.add(repairInfoItem, 3);
        else if (!visible && repairInfoItem.getParent() == invoiceInfoCard) invoiceInfoCard.remove(repairInfoItem);
        invoiceInfoCard.revalidate();
        invoiceInfoCard.repaint();
    }


    private void printInvoice() {
        if (!invoiceReady || invoiceId() == null || lblInfo[0].getText().equals("—")) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn đã tải thành công.");
            return;
        }
        try {
            store = new ThongTinCuaHang();

            if (!store.complete() && !store.edit(this)) {
                return;
            }

            printAllPages();
        } catch (PrinterException | java.io.IOException ex) {
            String message;

            if (ex instanceof java.awt.print.PrinterAbortException) {
                message = """
                        Quá trình in hóa đơn đã bị hủy hoặc gián đoạn.
                        Vui lòng kiểm tra máy in rồi thử in lại.""";

            } else if (ex instanceof java.io.IOException) {
                message = """
                        Không thể đọc hoặc lưu thông tin cửa hàng để in hóa đơn.
                        Vui lòng kiểm tra thông tin cửa hàng và quyền truy cập tệp rồi thử lại.""";

            } else {
                message = """
                        Không thể in hóa đơn.
                        Vui lòng kiểm tra máy in đã được kết nối và sẵn sàng,
                        hoặc chọn Microsoft Print to PDF để lưu hóa đơn rồi thử lại.""";
            }

            String detail = ex.getMessage();

            if (detail != null && !detail.isBlank()) {
                message += "\n\nChi tiết: " + detail;
            }

            JTextArea content = new JTextArea(message);
            content.setEditable(false);
            content.setOpaque(false);
            content.setFont(GiaoDien.font(13, false));
            content.setLineWrap(true);
            content.setWrapStyleWord(true);
            content.setColumns(46);

            JOptionPane.showMessageDialog(
                    this,
                    content,
                    "Lỗi in hóa đơn",
                    JOptionPane.ERROR_MESSAGE
            );
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
        String code = txtLoyalty.getText().trim().toUpperCase(Locale.ROOT);
        scannedCustomer = null;

        if (!txtLoyalty.isEnabled()
                || !code.matches("KH[0-9]{3}")
                || !code.equals(invoiceCustomer)) {
            lblLoyalty.setText(
                    "Mã quét không khớp khách hàng của hóa đơn."
            );
            return;
        }

        String sql =
                "SELECT k.hoTenKH, COALESCE(d.tongDiemTichLuy, 0) AS tongDiem "
                + "FROM dbo.KhachHang k "
                + "LEFT JOIN dbo.vw_KhachHangDiemTichLuy d "
                + "ON d.maKhachHang = k.maKhachHang "
                + "WHERE k.maKhachHang = ?";

        try (Connection conn = KetNoiCSDL.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Không tìm thấy khách hàng.");
                }

                long totalPoints = rs.getLong("tongDiem");

                scannedCustomer = code;
                lblLoyalty.setText(
                        "Đã nhận: " + code + " — " + rs.getString("hoTenKH")
                        + " | Tổng điểm hiện tại: " + totalPoints
                        + " | Cộng điểm mới sau thanh toán."
                );

                txtLoyalty.setText("");
            }
        } catch (SQLException ex) {
            lblLoyalty.setText("Không đọc được tổng điểm — chưa đăng ký tích điểm.");
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Không thể đọc điểm",
                    JOptionPane.ERROR_MESSAGE
            );
        }
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
        job.setPrintable(document.getPrintable(
                null,
                new java.text.MessageFormat("Hóa đơn " + invoiceId() + " — Trang {0}")), job.validatePage(format));
        if (!job.printDialog()) return false;
        job.print();
        return true;
    }

    private String invoicePrintHtml() {
        if (store == null) throw new IllegalStateException("Chưa tải thông tin cửa hàng.");
        boolean sale = "Bán phụ tùng".equals(printInfo.get("Loại hóa đơn"));
        StringBuilder html = new StringBuilder("<html><head><style>"
                + "body { font-family: Arial; font-size: 9pt; color: #172f3a; margin: 0; }"
                + "h1 { text-align: center; font-size: 15pt; margin: 10pt 0 4pt 0; }"
                + "h2 { font-size: 10pt; color: #13272e; margin: 9pt 0 4pt 0; }"
                + "p { margin: 4pt 0; } table { width: 100%; } td, th { padding: 3px; }"
                + "th { background-color: #edf3f2; color: #13272e; }"
                + ".grand td { background-color: #edf3f2; font-size: 11pt; padding: 7px; }"
                + "</style></head><body>");
        html.append(store.headerHtml()).append("<hr size='1' color='#487674'>");
        html.append("<h1>").append(sale ? "HÓA ĐƠN BÁN PHỤ TÙNG" : "HÓA ĐƠN THANH TOÁN DỊCH VỤ")
                .append("</h1><p align='center'>Số hóa đơn: <b>").append(escapeHtml(lblInfo[0].getText()))
                .append("</b> &nbsp; | &nbsp; Ngày lập: ").append(escapeHtml(lblInfo[4].getText())).append("</p>");
        html.append("<h2>THÔNG TIN KHÁCH HÀNG</h2><table cellspacing='0'>");
        infoRow(html, "Khách hàng", lblInfo[1].getText(), "Điện thoại", printInfo.get("Điện thoại khách hàng"));
        infoRow(html, "Địa chỉ", printInfo.get("Địa chỉ khách hàng"), "Email", printInfo.get("Email khách hàng"));
        if (!sale) {
            infoRow(html, "Biển số / VIN", lblInfo[2].getText(), "Hãng / dòng xe", printInfo.get("Hãng / dòng xe"));
            infoRow(html, "Phiếu sửa chữa", lblInfo[3].getText(), "Loại hóa đơn", printInfo.get("Loại hóa đơn"));
        }
        html.append("</table><h2>CHI TIẾT THANH TOÁN</h2>");
        if (services.getRowCount() > 0) appendPrintTable(html, "I. DỊCH VỤ / TIỀN CÔNG", services);
        if (parts.getRowCount() > 0) appendPrintTable(html, services.getRowCount() > 0 ? "II. PHỤ TÙNG SỬ DỤNG" : "PHỤ TÙNG", parts);
        html.append("<table cellspacing='0'><tr><td width='45%' valign='top'><h2>THÔNG TIN THANH TOÁN</h2>");
        html.append("<p><b>Phương thức:</b> ").append(escapeHtml(printInfo.get("Phương thức thanh toán")))
                .append("</p><p><b>Trạng thái:</b> ").append(escapeHtml(lblInfo[5].getText()))
                .append("</p><p>").append(escapeHtml(lblCoupon.getText()))
                .append("</p></td><td width='55%' valign='top'><h2>TỔNG CỘNG</h2><table cellspacing='0'>");
        String[] totals = {"Tiền công / dịch vụ", "Tiền phụ tùng", "Thuế VAT 8%", "Giảm giá", "TỔNG THANH TOÁN"};
        for (int i = 0; i < totals.length; i++) {
            html.append(i == 4 ? "<tr class='grand'>" : "<tr>").append("<td><b>").append(totals[i])
                    .append("</b></td><td align='right' nowrap>").append(escapeHtml(lblTotals[i].getText())).append("</td></tr>");
        }
        html.append("</table></td></tr></table><p><b>Xác nhận thu tiền: </b>")
                .append("Đã thanh toán".equals(lblInfo[5].getText()) ? "Đã thu đủ tiền." : "Chưa xác nhận thanh toán.")
                .append("</p><br><table cellspacing='0'><tr><td align='center' width='50%'><b>KHÁCH HÀNG</b><br>(Ký, ghi rõ họ tên)</td>")
                .append("<td align='center'><b>NHÂN VIÊN LẬP HÓA ĐƠN</b><br>(Ký, ghi rõ họ tên)</td></tr>")
                .append("<tr><td height='45'></td><td></td></tr><tr><td align='center'>")
                .append(escapeHtml(lblInfo[1].getText())).append("</td><td align='center'>")
                .append(escapeHtml(printInfo.get("Nhân viên lập"))).append("<br>")
                .append(escapeHtml(printInfo.get("Vai trò"))).append("</td></tr></table>")
                .append("<p align='center'>Cảm ơn Quý khách đã tin tưởng và sử dụng dịch vụ!</p>");
        return html.append("</body></html>").toString();
    }

    private static void infoRow(StringBuilder html, String leftLabel, String left, String rightLabel, String right) {
        html.append("<tr><td width='58%'><b>").append(escapeHtml(leftLabel)).append(": </b>")
                .append(escapeHtml(left)).append("</td><td width='42%'><b>").append(escapeHtml(rightLabel))
                .append(": </b>").append(escapeHtml(right)).append("</td></tr>");
    }


    private static void appendPrintTable(
            StringBuilder html,
            String title,
            DefaultTableModel model) {

        html.append("<p><b>")
            .append(escapeHtml(title))
            .append("</b></p>")
            .append("<table border='1' cellspacing='0' cellpadding='3'><tr>");

        String[] widths;

        if (model.getColumnCount() == 7) {
            // Bảng phụ tùng: 7 cột
            widths = new String[]{
                "5%", "10%", "25%", "8%", "15%", "17%", "20%"
            };
        } else {
            // Bảng dịch vụ: 6 cột
            widths = new String[]{
                "5%", "10%", "35%", "10%", "19%", "21%"
        };
        }

        for (int column = 0; column < model.getColumnCount(); column++) {
            String width = column < widths.length ? widths[column] : "";

            html.append("<th width='")
                .append(width)
                .append("'>")
                .append(escapeHtml(model.getColumnName(column)))
                .append("</th>");
        }

        html.append("</tr>");

        for (int row = 0; row < model.getRowCount(); row++) {
            html.append("<tr>");

            for (int column = 0; column < model.getColumnCount(); column++) {
                Object value = model.getValueAt(row, column);

                html.append("<td align='")
                    .append(column >= 4 ? "right" : column == 2 ? "left" : "center")
                    .append("'>")
                    .append(escapeHtml(value == null ? "" : value.toString()))
                    .append("</td>");
            }

            html.append("</tr>");
        }

        html.append("</table>");
    }


    private static String display(String text) {
        return text == null || text.isBlank() ? "Chưa cập nhật" : text;
    }

    private static String escapeHtml(String text) {
        return text == null ? "" : text.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
