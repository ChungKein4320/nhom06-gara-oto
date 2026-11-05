package entity;

import java.math.BigDecimal;

// ===== ENTITY: ĐỐI TƯỢNG PHỤ TÙNG =====

public class PhuTung {
    // ===== THUỘC TÍNH =====
    private String maPhuTung;
    private String tenPhuTung;
    private BigDecimal donGia;
    private String loaiPhuTung;
    private Integer thoiHanBaoHanh;

    // ===== KHỞI TẠO ĐỐI TƯỢNG =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: PhuTung() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public PhuTung() {
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: PhuTung(String, String, BigDecimal, String, Integer) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public PhuTung(String maPhuTung, String tenPhuTung, BigDecimal donGia, String loaiPhuTung,
            Integer thoiHanBaoHanh) {
        super();
        this.maPhuTung = maPhuTung;
        this.tenPhuTung = tenPhuTung;
        this.donGia = donGia;
        this.loaiPhuTung = loaiPhuTung;
        this.thoiHanBaoHanh = thoiHanBaoHanh;
    }

    // ===== GETTER VÀ SETTER =====

    // ===== PHƯƠNG THỨC: getMaPhuTung() — Lấy mã phụ tùng. =====
    public String getMaPhuTung() {
        return maPhuTung;
    }

    // ===== PHƯƠNG THỨC: setMaPhuTung(String) — Gán mã phụ tùng. =====
    public void setMaPhuTung(String value) {
        if (value == null || !value.matches("PT[0-9]{3}"))
            throw new IllegalArgumentException("Mã phụ tùng không hợp lệ");
        this.maPhuTung = value;
    }

    // ===== PHƯƠNG THỨC: getTenPhuTung() — Lấy ten phụ tùng. =====
    public String getTenPhuTung() {
        return tenPhuTung;
    }

    // ===== PHƯƠNG THỨC: setTenPhuTung(String) — Gán ten phụ tùng. =====
    public void setTenPhuTung(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Tên phụ tùng không được để trống");
        if (value.trim().length() > 150)
            throw new IllegalArgumentException("Tên phụ tùng không được quá 150 ký tự");
        this.tenPhuTung = value.trim();
    }

    // ===== PHƯƠNG THỨC: getDonGia() — Lấy đơn giá. =====
    public BigDecimal getDonGia() {
        return donGia;
    }

    // ===== PHƯƠNG THỨC: setDonGia(BigDecimal) — Gán đơn giá. =====
    public void setDonGia(BigDecimal value) {
        if (value == null || value.signum() < 0)
            throw new IllegalArgumentException("Đơn giá phụ tùng phải là số không âm");
        if (value.scale() > 2 || value.precision() - value.scale() > 16)
            throw new IllegalArgumentException("Đơn giá quá lớn, vui lòng nhập giá trị nhỏ hơn.");
        this.donGia = value;
    }

    // ===== PHƯƠNG THỨC: getLoaiPhuTung() — Lấy loai phụ tùng. =====
    public String getLoaiPhuTung() {
        return loaiPhuTung;
    }

    // ===== PHƯƠNG THỨC: setLoaiPhuTung(String) — Gán loai phụ tùng. =====
    public void setLoaiPhuTung(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Loại phụ tùng không được để trống");
        if (value.trim().length() > 100)
            throw new IllegalArgumentException("Loại phụ tùng không được quá 100 ký tự");
        this.loaiPhuTung = value.trim();
    }

    // ===== PHƯƠNG THỨC: getThoiHanBaoHanh() — Lấy thoi han báo hành. =====
    public Integer getThoiHanBaoHanh() {
        return thoiHanBaoHanh;
    }

    // ===== PHƯƠNG THỨC: setThoiHanBaoHanh(Integer) — Gán thoi han báo hành. =====
    public void setThoiHanBaoHanh(Integer value) {
        if (value == null || value < 0)
            throw new IllegalArgumentException("Thời hạn bảo hành không được rỗng hoặc âm");
        this.thoiHanBaoHanh = value;
    }

    // ===== PHƯƠNG THỨC: capNhatGia(BigDecimal) — Cập nhật nhat giá. =====
    public void capNhatGia(BigDecimal giaMoi) {
        setDonGia(giaMoi);
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "PhuTung [maPhuTung=" + maPhuTung + ", tenPhuTung=" + tenPhuTung + ", donGia=" + donGia
                + ", loaiPhuTung=" + loaiPhuTung + ", thoiHanBaoHanh=" + thoiHanBaoHanh + "]";
    }
}
