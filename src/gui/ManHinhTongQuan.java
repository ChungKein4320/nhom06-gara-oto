package gui;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
public final class ManHinhTongQuan extends JPanel {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    public ManHinhTongQuan(VaiTro role,ManHinhChinh winFrame,String displayName){
        setLayout(new BorderLayout());
        setBackground(GiaoDien.BG);
        setBorder(new EmptyBorder(30,32,30,32));
        JPanel pnlMain=GiaoDien.stack(0);
        JLabel lblSectionTitle=GiaoDien.label("TRUNG TÂM ĐIỀU HÀNH/THINKING AUTO",11,true,GiaoDien.TEAL);
        lblSectionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlMain.add(lblSectionTitle);
        GiaoDien.gap(pnlMain,9);
        JLabel lblGreeting=GiaoDien.label("Xin chào, "+displayName+"!",29,true,GiaoDien.NAV);
        lblGreeting.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlMain.add(lblGreeting);
        GiaoDien.gap(pnlMain,8);
        GiaoDien.gap(pnlMain,26);
        JPanel pnlCards=new JPanel(new GridLayout(1,4,14,0));
        pnlCards.setOpaque(false);
        pnlCards.setMaximumSize(new Dimension(9999,130));
        String[] keys = role == VaiTro.KY_THUAT
                ? new String[] {"Xe tiếp nhận", "Phiếu kiểm tra", "Báo giá", "Phiếu sửa chữa"}
                : role == VaiTro.KHO ? new String[] {"Loại phụ tùng", "Sắp hết hàng", "Phiếu nhập", "Yêu cầu cấp"}
                : role == VaiTro.THU_NGAN ? new String[] {"Hóa đơn", "Chờ thanh toán", "Doanh thu", "Khuyến mãi"}
                : new String[] {"Khách hàng", "Phương tiện", "Nhân viên", "Doanh thu"};
        for (String key : keys) {
            JPanel pnlCard = GiaoDien.card(new BorderLayout(0, 13));
            pnlCard.add(GiaoDien.label(key.toUpperCase(), 11, true, GiaoDien.MUTED), BorderLayout.NORTH);
            JLabel lblValue = GiaoDien.label("—", 29, true, GiaoDien.TEAL);
            pnlCard.add(lblValue, BorderLayout.CENTER);
            try {
                lblValue.setText(dao.DashboardDAO.metric(key));
            } catch (java.sql.SQLException ex) {
                pnlCard.add(GiaoDien.label("Không tải được dữ liệu", 12, false, GiaoDien.MUTED), BorderLayout.SOUTH);
                lblValue.setToolTipText(ex.getMessage());
            }
            pnlCards.add(pnlCard);
        }
        pnlMain.add(pnlCards);
        GiaoDien.gap(pnlMain,25);
        JPanel pnlSection = GiaoDien.card(new BorderLayout(0, 17));
        pnlSection.add(GiaoDien.label(role == VaiTro.ADMIN ? "Chức năng theo từng vai trò" :
                "Truy cập nhanh theo nhóm đối tượng", 19, true, GiaoDien.NAV), BorderLayout.NORTH);
        if (role == VaiTro.ADMIN) {
            JPanel pnlColumns = new JPanel(new GridLayout(1, 4, 12, 0));
            pnlColumns.setOpaque(false);
            String[] names = {"NHÂN VIÊN KỸ THUẬT", "NHÂN VIÊN KHO", "NHÂN VIÊN BÁN HÀNG", "NHÂN VIÊN QUẢN LÝ"};
            String[][] groups = {DuLieuTrinhDon.TECH, DuLieuTrinhDon.WARE, DuLieuTrinhDon.CASH, DuLieuTrinhDon.MANAGER};
            for (int i = 0; i < groups.length; i++) {
                JPanel pnlGroup = new JPanel();
                pnlGroup.setLayout(new BoxLayout(pnlGroup, BoxLayout.Y_AXIS));
                pnlGroup.setOpaque(false);
                JLabel lblHeading = GiaoDien.label(names[i], 12, true, GiaoDien.TEAL);
                lblHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
                pnlGroup.add(lblHeading);
                pnlGroup.add(Box.createVerticalStrut(12));
                for (int j = 2; j < groups[i].length; j++) {
                    String page = groups[i][j];
                    JButton btnPage = GiaoDien.button(page, new Color(0xE5F0EE), GiaoDien.NAV);
                    btnPage.setHorizontalAlignment(SwingConstants.LEFT);
                    btnPage.setMaximumSize(new Dimension(Integer.MAX_VALUE, 37));
                    btnPage.setAlignmentX(Component.LEFT_ALIGNMENT);
                    btnPage.addActionListener(e -> winFrame.open(page));
                    pnlGroup.add(btnPage);
                    pnlGroup.add(Box.createVerticalStrut(8));
                }
                pnlColumns.add(pnlGroup);
            }
            pnlSection.add(pnlColumns, BorderLayout.CENTER);
        } else {
            String[] menu = DuLieuTrinhDon.forRole(role);
            JPanel pnlLinks = new JPanel(new GridLayout(0, 3, 14, 14));
            pnlLinks.setOpaque(false);
            for (int i = 1; i < menu.length; i++) {
                String page = menu[i];
                JButton btnPage = GiaoDien.button(page, new Color(0xE5F0EE), GiaoDien.NAV);
                btnPage.setHorizontalAlignment(SwingConstants.LEFT);
                btnPage.addActionListener(e -> winFrame.open(page));
                pnlLinks.add(btnPage);
            }
            pnlSection.add(pnlLinks, BorderLayout.CENTER);
        }
        pnlMain.add(pnlSection);
        GiaoDien.gap(pnlMain,22);
        add(pnlMain,BorderLayout.NORTH);
    }
}
