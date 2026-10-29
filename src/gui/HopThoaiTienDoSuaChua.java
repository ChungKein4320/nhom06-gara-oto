package gui;

import connectDB.KetNoiCSDL;
import dao.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import javax.swing.*;

/** Xác nhận từng hạng mục và bảo hành trước bước nghiệm thu. */
final class HopThoaiTienDoSuaChua {
    static void show(Component parent,String id) {
        JDialog winDialog=new JDialog(SwingUtilities.getWindowAncestor(parent),"Tiến độ sửa chữa "+id,Dialog.ModalityType.APPLICATION_MODAL);
        JPanel pnlRows=new JPanel() {
            @Override public Dimension getPreferredSize() {
                Dimension d=super.getPreferredSize();
                if(getParent() instanceof JViewport vp && vp.getWidth()>0) return new Dimension(vp.getWidth(),d.height);
                return d;
            }
        };
        pnlRows.setLayout(new BoxLayout(pnlRows,BoxLayout.Y_AXIS));
        pnlRows.setBackground(GiaoDien.BG);
        pnlRows.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        JButton btnFinish=GiaoDien.button("Nghiệm thu đạt",GiaoDien.TEAL,Color.WHITE);
        JButton btnStart=GiaoDien.button("Bắt đầu sửa chữa",GiaoDien.CREAM,GiaoDien.NAV);
        Runnable[] load=new Runnable[1];
        load[0]=() -> {
            try(Connection conn=KetNoiCSDL.getConnection()) {
                var headers=new CoSoDAO("PhieuSuaChua").getAll(conn,Map.of("maPSC",id));
                if(headers.isEmpty()) throw new SQLException("Phiếu không còn tồn tại.");
                var header=headers.get(0); boolean ongoing="Đang sửa chữa".equals(header.get("trangThai"));
                btnStart.setVisible("Chờ sửa chữa".equals(header.get("trangThai")));
                var lines=new CoSoDAO("ChiTietPhieuSuaChua").getAll(conn,Map.of("maPSC",id));
                pnlRows.removeAll(); pnlRows.add(new BieuMauBanGhi("PhieuSuaChua",header,Set.of(),true)); boolean all=!lines.isEmpty(),pending=false;
                for(var line:lines) {
                    boolean done="Hoàn thành".equals(line.get("trangThaiHangMuc")); all &= done;
                    pending |= !"Đạt".equals(line.get("ketQuaNghiemThu"));
                    JPanel pnlLine = GiaoDien.card(new BorderLayout(8, 8));
                    String service = String.valueOf(line.get("maDichVu"));
                    String partId = String.valueOf(line.get("maPhuTung"));
                    boolean noPart = "PT000".equals(partId);

                    String part = noPart ? "Không sử dụng phụ tùng" : partId;
                    int quantity = ((Number) line.get("soLuongPhuTung")).intValue();
                    int savedMonths = ((Number) line.get("thoiHanBaoHanh")).intValue();

                    String warrantyText = savedMonths > 0
                            ? savedMonths + " tháng"
                            : "Không bảo hành";

                    JPanel pnlInfo = new JPanel(new GridLayout(2, 1, 0, 5));
                    pnlInfo.setOpaque(false);

                    pnlInfo.add(GiaoDien.label(
                            service + " / " + part + " — " + line.get("trangThaiHangMuc"),
                            13, true, DinhDangKeThua.TEXT
                    ));

                    pnlInfo.add(GiaoDien.label(
                            "Số lượng phụ tùng: " + quantity
                                    + " — Bảo hành: " + warrantyText,
                            12, false, DinhDangKeThua.TEXT
                    ));
                    pnlLine.add(pnlInfo, BorderLayout.CENTER);
                    boolean waitingForParts =
                            "Thiếu phụ tùng".equals(line.get("trangThaiHangMuc"))
                            || "Chờ sửa chữa".equals(line.get("trangThaiHangMuc"));

                    String buttonText = done
                            ? "Đã hoàn thành"
                            : waitingForParts
                                    ? "Chờ cấp phát phụ tùng"
                                    : "Xác nhận đã sửa";

                    JButton btnDone = GiaoDien.button(
                            buttonText, GiaoDien.CREAM, GiaoDien.NAV
                    );
                    btnDone.setEnabled(
                            ongoing
                            && "Đang sửa chữa".equals(line.get("trangThaiHangMuc"))
                    );
                    btnDone.addActionListener(e -> {
                        try {
                            int months = 0;

                            // Lấy thời hạn bảo hành từ đúng phụ tùng của hạng mục.
                            // Mở kết nối riêng vì kết nối tải màn hình đã đóng.
                            if (!noPart) {
                                try (Connection warrantyConn = KetNoiCSDL.getConnection()) {
                                    var parts = new CoSoDAO("PhuTung").getAll(
                                            warrantyConn,
                                            Map.of("maPhuTung", partId)
                                    );

                                    if (parts.isEmpty()) {
                                        throw new SQLException(
                                                "Không tìm thấy phụ tùng " + partId
                                        );
                                    }

                                    Object value = parts.get(0).get("thoiHanBaoHanh");
                                    if (!(value instanceof Number)) {
                                        throw new SQLException(
                                                "Phụ tùng chưa có thời hạn bảo hành hợp lệ."
                                        );
                                    }

                                    months = ((Number) value).intValue();

                                    if (months < 0) {
                                        throw new SQLException(
                                                "Thời hạn bảo hành không được âm."
                                        );
                                    }
                                }
                            }

                            String warrantyDisplay = months == 0
                                    ? "Không bảo hành"
                                    : months + " tháng";

                            JPanel pnlWarranty = new JPanel(new GridLayout(0, 1, 0, 8));
                            pnlWarranty.add(new JLabel("Phụ tùng: " + part));
                            pnlWarranty.add(new JLabel(
                                    "Thời hạn bảo hành: " + warrantyDisplay
                            ));

                            Object[] options = {"Xác nhận", "Hủy"};

                            int choice = JOptionPane.showOptionDialog(
                                    winDialog,
                                    pnlWarranty,
                                    "Xác nhận đã sửa " + service + " / " + part,
                                    JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    options,
                                    options[0]
                            );

                            // Bấm Hủy hoặc đóng cửa sổ thì không lưu.
                            if (choice != 0) return;

                            QuyTrinhDAO.completeRepairItem(
                                    id, service, partId, months
                            );

                            load[0].run();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(
                                    winDialog,
                                    ex.getMessage(),
                                    "Không thể xác nhận",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    });
                    pnlLine.add(btnDone,BorderLayout.EAST); pnlLine.setMaximumSize(new Dimension(Integer.MAX_VALUE,85)); pnlRows.add(pnlLine); pnlRows.add(Box.createVerticalStrut(6));
                }
                btnFinish.setEnabled((ongoing || "Đã sửa xong".equals(header.get("trangThai"))) && all && pending); pnlRows.revalidate(); pnlRows.repaint();
            } catch(SQLException ex) { btnStart.setEnabled(false); btnFinish.setEnabled(false); JOptionPane.showMessageDialog(winDialog,ex.getMessage(),"Không tải được phiếu",JOptionPane.ERROR_MESSAGE); }
        };
        btnStart.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(winDialog,"Bắt đầu sửa chữa phiếu "+id+"?","Xác nhận",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
            try { QuyTrinhDAO.startRepair(id); load[0].run(); }
            catch(SQLException ex) { JOptionPane.showMessageDialog(winDialog,ex.getMessage()); }
        });
        btnFinish.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(winDialog,"Xác nhận tất cả hạng mục nghiệm thu đạt và chuyển phiếu sang Đã sửa xong?","Nghiệm thu đạt",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
            try { QuyTrinhDAO.acceptRepair(id); winDialog.dispose(); } catch(SQLException ex) { JOptionPane.showMessageDialog(winDialog,ex.getMessage()); load[0].run(); }
        });
        JPanel pnlContent=new JPanel(new BorderLayout(8,8)); pnlContent.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JScrollPane scrRows=new JScrollPane(pnlRows); scrRows.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrRows.setPreferredSize(new Dimension(860,520)); scrRows.getVerticalScrollBar().setUnitIncrement(20); pnlContent.add(scrRows);
        JPanel pnlButtons=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,2)); JButton btnClose=GiaoDien.button("Đóng",GiaoDien.CREAM,GiaoDien.NAV); btnClose.addActionListener(e -> winDialog.dispose()); pnlButtons.add(btnStart); pnlButtons.add(btnFinish); pnlButtons.add(btnClose); pnlContent.add(pnlButtons,BorderLayout.SOUTH);
        winDialog.setContentPane(pnlContent); load[0].run(); winDialog.pack(); winDialog.setLocationRelativeTo(null); winDialog.setVisible(true);
        javax.swing.Timer refreshTimer = new javax.swing.Timer(3000, e -> {
            if (winDialog.isShowing() && winDialog.isActive()) {
                load[0].run();
            }
        });

        winDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                refreshTimer.start();
            }

            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                refreshTimer.stop();
            }
        });

        winDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            }
}
