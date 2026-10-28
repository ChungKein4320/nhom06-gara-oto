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
                    JPanel pnlLine=GiaoDien.card(new BorderLayout(8,8));
                    String service=String.valueOf(line.get("maDichVu"));
                    String part="PT000".equals(line.get("maPhuTung")) ? "Không sử dụng phụ tùng" : String.valueOf(line.get("maPhuTung"));
                    pnlLine.add(GiaoDien.label(service+" / "+part+" — "+line.get("trangThaiHangMuc")+" — Bảo hành: "+line.get("thoiHanBaoHanh")+" tháng",13,true,DinhDangKeThua.TEXT),BorderLayout.CENTER);
                    boolean needsParts =
                            "Thiếu phụ tùng".equals(line.get("trangThaiHangMuc"));

                    String buttonText = done
                            ? "Đã hoàn thành"
                            : needsParts
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
                        JComboBox<String> cboWarranty = new JComboBox<>(
                                new String[]{"2 năm", "5 năm", "10 năm"}
                        );
                        int[] warrantyMonths = {24, 60, 120};

                        JPanel pnlWarranty = new JPanel(new BorderLayout(8, 8));
                        pnlWarranty.add(
                                new JLabel("Thời hạn bảo hành hạng mục"),
                                BorderLayout.NORTH
                        );
                        pnlWarranty.add(cboWarranty, BorderLayout.CENTER);

                        if (JOptionPane.showConfirmDialog(
                                winDialog,
                                pnlWarranty,
                                "Xác nhận đã sửa " + service + " / " + part,
                                JOptionPane.OK_CANCEL_OPTION
                        ) != JOptionPane.OK_OPTION) {
                            return;
                        }

                        try {
                            QuyTrinhDAO.completeRepairItem(
                                    id, service, part,
                                    warrantyMonths[cboWarranty.getSelectedIndex()]
                            );
                            load[0].run();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(
                                    winDialog, ex.getMessage(),
                                    "Không thể xác nhận", JOptionPane.ERROR_MESSAGE
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
