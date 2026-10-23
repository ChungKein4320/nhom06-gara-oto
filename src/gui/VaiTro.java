package gui;
public enum VaiTro {
    KY_THUAT("Nhân viên kỹ thuật"),
    KHO("Nhân viên kho"),
    THU_NGAN("Nhân viên bán hàng"),
    QUAN_LY("Nhân viên quản lý"),
    ADMIN("Quản trị viên");
    public final String title;
    VaiTro(String title) { this.title = title; }
    @Override public String toString() { return title; }
}