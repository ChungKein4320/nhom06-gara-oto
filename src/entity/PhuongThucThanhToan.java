package entity;

// ===== ENTITY: ĐỐI TƯỢNG PHƯƠNG THỨC THANH TOÁN =====

public class PhuongThucThanhToan {
    // ===== THUỘC TÍNH =====
    private String maPhuongThuc;
    private String tenPhuongThuc;

    // ===== KHỞI TẠO ĐỐI TƯỢNG =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: PhuongThucThanhToan() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public PhuongThucThanhToan() {
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: PhuongThucThanhToan(String, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public PhuongThucThanhToan(String maPhuongThuc, String tenPhuongThuc) {
        this.maPhuongThuc = maPhuongThuc;
        this.tenPhuongThuc = tenPhuongThuc;
    }

    // ===== GETTER VÀ SETTER =====

    // ===== PHƯƠNG THỨC: getMaPhuongThuc() — Lấy mã phương thuc. =====
    public String getMaPhuongThuc() {
        return maPhuongThuc;
    }

    // ===== PHƯƠNG THỨC: setMaPhuongThuc(String) — Gán mã phương thuc. =====
    public void setMaPhuongThuc(String value) {
        if (value == null || !value.matches("TT[0-9]{3}"))
            throw new IllegalArgumentException("Mã phương thức thanh toán không hợp lệ");
        this.maPhuongThuc = value;
    }

    // ===== PHƯƠNG THỨC: getTenPhuongThuc() — Lấy ten phương thuc. =====
    public String getTenPhuongThuc() {
        return tenPhuongThuc;
    }

    // ===== PHƯƠNG THỨC: setTenPhuongThuc(String) — Gán ten phương thuc. =====
    public void setTenPhuongThuc(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Tên phương thức thanh toán không hợp lệ");
        this.tenPhuongThuc = value.trim();
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "PhuongThucThanhToan [maPhuongThuc=" + maPhuongThuc + ", tenPhuongThuc=" + tenPhuongThuc + "]";
    }

}
