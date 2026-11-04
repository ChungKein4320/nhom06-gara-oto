package gui;

// ===== GUI: VAI TRÒ NGƯỜI DÙNG =====

public enum VaiTro {
    // ===== THÀNH PHẦN GIAO DIỆN =====
    KY_THUAT("Nhân viên kỹ thuật"), KHO("Nhân viên kho"), THU_NGAN("Nhân viên bán hàng"),
    QUAN_LY("Nhân viên quản lý"), ADMIN("Quản trị viên");

    public final String title;

    // ===== KHỞI TẠO ĐỐI TƯỢNG: VaiTro(String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    VaiTro(String title) {
        this.title = title;
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return title;
    }
}