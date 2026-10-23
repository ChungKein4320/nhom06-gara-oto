package gui;
import java.awt.BorderLayout;
import javax.swing.JPanel;
/** Trang quản lý dùng dữ liệu database. */
public final class ManHinhDoiTuong extends JPanel {
    @java.io.Serial private static final long serialVersionUID = 1L;
    public ManHinhDoiTuong(String page) {
        super(new BorderLayout());
        add(new ManHinhDuLieu(page), BorderLayout.CENTER);
    }
}