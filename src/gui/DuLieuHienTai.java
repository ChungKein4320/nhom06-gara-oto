package gui;
import javax.swing.JComponent;
/** Chọn trang quản lý kết nối database. */
public final class DuLieuHienTai {
    private DuLieuHienTai() { }
    public static JComponent attach(String page, JComponent original) {
        return ManHinhDuLieu.tableFor(page) == null ? original : new ManHinhDuLieu(page);
    }
}