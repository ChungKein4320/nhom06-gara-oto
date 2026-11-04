package dao;

import java.sql.SQLException;

// ===== DAO: LoiDuLieu =====
/** Lỗi dữ liệu kèm tên trường cần sửa. */
public final class LoiDuLieu extends SQLException {
    private static final long serialVersionUID = 1L;
    public final String field;

    // ===== KHỞI TẠO ĐỐI TƯỢNG: LoiDuLieu(String, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public LoiDuLieu(String field, String message) {
        super(message);
        this.field = field;
    }
}
