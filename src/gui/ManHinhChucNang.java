package gui;
import java.awt.BorderLayout;
import javax.swing.JPanel;
/** Trang nghiệp vụ dùng dữ liệu database. */
public final class ManHinhChucNang extends JPanel {
    @java.io.Serial private static final long serialVersionUID = 1L;
    public static boolean supports(String page) {
        return ManHinhDuLieu.tableFor(page) != null;
    }
    public ManHinhChucNang(String page) {
        super(new BorderLayout());
        add(new ManHinhDuLieu(page), BorderLayout.CENTER);
    }
}