package gui;

public enum VaiTro {
    // Mã cố định và tên hiển thị
    KY_THUAT("Nhân viên kỹ thuật"), 
    KHO("Nhân viên kho"), 
    THU_NGAN("Nhân viên bán hàng"),
    QUAN_LY("Nhân viên quản lý"), 
    ADMIN("Quản trị viên");

    public final String title;

    // Gán tên hiển thị cho từng enum
    VaiTro(String title) {
        this.title = title;
    }

    // Trả về tên hiển thị
    @Override
    public String toString() {
        return title;
    }
}