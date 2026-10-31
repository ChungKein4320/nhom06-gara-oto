package gui;

import connectDB.KetNoiCSDL;
import dao.ThuongMaiDAO;
import entity.HoaDon;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Thanh toán hóa đơn đang chọn: nhân viên xác nhận sau khi thực nhận tiền. */
final class HopThoaiThanhToan extends JDialog {
    private final String invoice;
    private final Runnable refreshed;
    private final String scannedCustomer;
    private final JComboBox<Method> cmbMethod=new OChonTimKiem<>();
    private final JLabel lblAmount=GiaoDien.label("",20,true,GiaoDien.ORANGE);
    private final JLabel lblQr=new JLabel("",SwingConstants.CENTER);
    private final JLabel lblInstruction=new JLabel("",SwingConstants.CENTER);
    private final JButton btnConfirm=GiaoDien.button("Xác nhận đã nhận tiền",GiaoDien.TEAL,Color.WHITE);
    private final JButton btnClose=GiaoDien.button("Đóng",GiaoDien.CREAM,GiaoDien.NAV);
    private SwingWorker<BufferedImage,Void> qrWorker;
    private BigDecimal total;
    private boolean busy,loading,qrReady;
    record Method(String id,String label,boolean qr) { @Override public String toString() { return label; } }
    static void show(Component parent,String invoice,Runnable refreshed) {
        show(parent,invoice,refreshed,null);
    }
    static void show(Component parent,String invoice,Runnable refreshed,String scannedCustomer) {
        HopThoaiThanhToan winPayment=new HopThoaiThanhToan(parent,invoice,refreshed,scannedCustomer);
        if(winPayment.reload()) { winPayment.pack(); winPayment.setLocationRelativeTo(null); winPayment.setVisible(true); }
        else winPayment.dispose();
    }
    private HopThoaiThanhToan(Component parent,String invoice,Runnable refreshed,String scannedCustomer) {
        super(SwingUtilities.getWindowAncestor(parent),"Thanh toán hóa đơn "+invoice,ModalityType.APPLICATION_MODAL);
        this.invoice=invoice; this.refreshed=refreshed;
        this.scannedCustomer=scannedCustomer;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { close(); } });
        JPanel pnlRoot=new JPanel(new BorderLayout(10,10)); pnlRoot.setBackground(GiaoDien.WHITE); pnlRoot.setBorder(new EmptyBorder(14,18,14,18));
        JPanel pnlHeader=new JPanel(new GridLayout(0,1,0,6)); pnlHeader.setOpaque(false);
        pnlHeader.add(GiaoDien.label("HÓA ĐƠN "+invoice,16,true,GiaoDien.NAV)); pnlHeader.add(lblAmount);
        pnlHeader.add(GiaoDien.label("Phương thức thanh toán",12,true,GiaoDien.TEXT)); pnlHeader.add(cmbMethod);
        pnlRoot.add(pnlHeader,BorderLayout.NORTH);
        JPanel pnlTransfer=new JPanel(new BorderLayout(0,6)); pnlTransfer.setOpaque(false);
        lblQr.setPreferredSize(new Dimension(500,350)); pnlTransfer.add(lblQr,BorderLayout.CENTER);
        // Reserve space before packing, including when cash is selected first.
        lblInstruction.setPreferredSize(new Dimension(500,90));
        pnlTransfer.add(lblInstruction,BorderLayout.SOUTH); pnlRoot.add(pnlTransfer,BorderLayout.CENTER);
        JPanel pnlButtons = new JPanel(new GridLayout(1, 2, 8, 8));
        pnlButtons.setOpaque(false);
        pnlButtons.add(btnClose);
        pnlButtons.add(btnConfirm);
        pnlRoot.add(pnlButtons, BorderLayout.SOUTH);
        setContentPane(pnlRoot);
        cmbMethod.addActionListener(e -> { if(!loading) showMethod(); });
        btnClose.addActionListener(e -> close()); btnConfirm.addActionListener(e -> confirm());
    }
    private boolean reload() {
        cancelQr(); total=null; btnConfirm.setEnabled(false); loading=true;
        Method previous=(Method)cmbMethod.getSelectedItem(); cmbMethod.removeAllItems();
        try(Connection conn=KetNoiCSDL.getConnection()) {
            try(PreparedStatement ps=conn.prepareStatement("SELECT tienPhuTung,tienCong,dichVuKhac,thueVAT,giamGia,trangThaiThanhToan FROM dbo.HoaDon WHERE maHoaDon=?")) {
                ps.setString(1,invoice);
                try(ResultSet rs=ps.executeQuery()) {
                    if(!rs.next()) throw new SQLException("Hóa đơn không còn tồn tại.");
                    String state=rs.getString("trangThaiThanhToan");
                    if(!state.equals("Chưa thanh toán") && !state.equals("Chờ thanh toán")) throw new SQLException("Hóa đơn không còn chờ thanh toán.");
                    HoaDon hoaDon=new HoaDon();
                    hoaDon.setTienPhuTung(rs.getBigDecimal("tienPhuTung"));
                    hoaDon.setTienCong(rs.getBigDecimal("tienCong"));
                    hoaDon.setDichVuKhac(rs.getBigDecimal("dichVuKhac"));
                    hoaDon.setThueVAT(rs.getBigDecimal("thueVAT"));
                    hoaDon.setGiamGia(rs.getBigDecimal("giamGia"));
                    total=hoaDon.tongThanhToan();
                    if(total.signum()<0) throw new SQLException("Số tiền hóa đơn không hợp lệ.");
                }
            }
            try(PreparedStatement ps=conn.prepareStatement("SELECT maPhuongThuc,tenPhuongThuc FROM dbo.PhuongThucThanhToan ORDER BY maPhuongThuc");ResultSet rs=ps.executeQuery()) {
                boolean cashAdded=false,qrAdded=false;
                while(rs.next()) {
                    String name=rs.getString(2),lower=name.toLowerCase(Locale.ROOT);
                    if(lower.contains("tiền mặt") && !cashAdded) { cmbMethod.addItem(new Method(rs.getString(1),"Tiền mặt",false)); cashAdded=true; }
                    else if(lower.contains("qr") && !qrAdded) { cmbMethod.addItem(new Method(rs.getString(1),"QR chuyển khoản",true)); qrAdded=true; }
                }
            }
            if(cmbMethod.getItemCount()==0) throw new SQLException("Chưa có phương thức Tiền mặt/QR Code trong dữ liệu.");
            int selectedIndex=0;
            if(previous!=null) for(int i=0;i<cmbMethod.getItemCount();i++) if(previous.id().equals(cmbMethod.getItemAt(i).id())) selectedIndex=i;
            cmbMethod.setSelectedIndex(selectedIndex);
            lblAmount.setText("CẦN THU: "+NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(total)+" đ");
        } catch(SQLException ex) { total=null; lblQr.setIcon(null); lblQr.setText("Không tải được hóa đơn."); error(ex); return false; }
        finally { loading=false; }
        showMethod(); return true;
    }
    private void showMethod() {
        cancelQr();
        qrReady = false;
        lblQr.setIcon(null);
        lblQr.setText("");
        btnConfirm.setEnabled(false);

        Method method = (Method) cmbMethod.getSelectedItem();
        if (method == null || total == null) return;

        if (!method.qr()) {
            lblQr.setText(
                    "<html><div style='text-align:center'>"
                    + "THANH TOÁN TIỀN MẶT<br><br>"
                    + "Nhận đủ tiền từ khách trước khi xác nhận."
                    + "</div></html>"
            );

            lblInstruction.setText(
                    scannedCustomer == null
                            ? "Không quét mã khách hàng — không tích điểm."
                            : "Đã quét " + scannedCustomer
                                    + " — tích điểm khi thanh toán."
            );

            btnConfirm.setEnabled(!busy);
            return;
        }

        String amountText = NumberFormat.getNumberInstance(
                Locale.forLanguageTag("vi-VN")
        ).format(total);

        lblInstruction.setText(
                "<html><div style='text-align:center'>"
                + "Số tiền cần chuyển: " + amountText + " đ<br>"
                + "Nội dung: THANH TOAN " + invoice + "<br>"
                + "Số tài khoản: " + MaThanhToanNganHang.ACCOUNT + "<br>"
                + "Tên tài khoản: " + MaThanhToanNganHang.OWNER
                + "</div></html>"
        );

        try {
            java.net.URL resource = HopThoaiThanhToan.class.getResource(
                    "/IMG/QR_ThanhToan.jpg"
            );

            if (resource == null) {
                throw new java.io.IOException(
                        "Không tìm thấy ảnh /IMG/QR_ThanhToan.jpg"
                );
            }

            BufferedImage qr = ImageIO.read(resource);
            if (qr == null) {
                throw new java.io.IOException("File QR không phải ảnh hợp lệ.");
            }

            // Giữ đúng tỷ lệ ảnh.
            double scale = Math.min(
                    320.0 / qr.getWidth(),
                    320.0 / qr.getHeight()
            );

            int width = Math.max(1, (int) Math.round(qr.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(qr.getHeight() * scale));

            lblQr.setIcon(new ImageIcon(
                    qr.getScaledInstance(width, height, Image.SCALE_REPLICATE)
            ));

            qrReady = true;
            btnConfirm.setEnabled(!busy);
        } catch (Exception ex) {
            lblQr.setIcon(null);
            lblQr.setText("Không đọc được ảnh QR.");
            lblQr.setToolTipText(ex.getMessage());
            error(ex);
        }
    }
    private void confirm() {
        Method method=(Method)cmbMethod.getSelectedItem();
        if(busy || total==null || method==null || (method.qr() && !qrReady)) return;
        if(JOptionPane.showConfirmDialog(this,"Xác nhận đã nhận đủ "+lblAmount.getText().replace("CẦN THU: ","")+" bằng "+method.label()+"?","Xác nhận nhận tiền",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        BigDecimal expected=total; busy=true; toggle(false);
        new SwingWorker<Void,Void>() {
            @Override protected Void doInBackground() throws Exception { ThuongMaiDAO.pay(invoice,method.id(),expected,scannedCustomer); return null; }
            @Override protected void done() {
                busy=false;
                try { get(); cancelQr(); dispose(); JOptionPane.showMessageDialog(getOwner(),scannedCustomer==null ? "Đã thanh toán. Không tích điểm do chưa quét mã khách hàng." : "Đã thanh toán và ghi nhận điểm tích lũy."); refreshed.run(); }
                catch(Exception ex) { toggle(true); error(ex.getCause()==null ? ex : ex.getCause()); reload(); refreshed.run(); }
            }
        }.execute();
    }
    private void toggle(boolean enabled) { cmbMethod.setEnabled(enabled); btnConfirm.setEnabled(enabled); btnClose.setEnabled(enabled); }
    private void cancelQr() { if(qrWorker!=null) qrWorker.cancel(true); }
    private void close() { if(!busy) { cancelQr(); dispose(); } }
    private void error(Throwable ex) { JOptionPane.showMessageDialog(this,ex.getMessage(),"Không thể thanh toán",JOptionPane.ERROR_MESSAGE); }
}
