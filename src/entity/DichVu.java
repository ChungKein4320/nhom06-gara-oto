package entity;

import java.math.BigDecimal;

// ===== ENTITY: DỊCH VỤ =====

public class DichVu {
    // ===== THUỘC TÍNH =====
    private String maDichVu;
    private String tenDichVu;
    private String moTa;
    private BigDecimal donGia;
    private String trangThai;

    // ===== KHỞI TẠO ĐỐI TƯỢNG =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: DichVu() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public DichVu() {
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: DichVu(String, String, String, BigDecimal, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public DichVu(String maDichVu, String tenDichVu, String moTa, BigDecimal donGia, String trangThai) {
        super();
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.moTa = moTa;
        this.donGia = donGia;
        this.trangThai = trangThai;
    }

    // ===== GETTER VÀ SETTER =====

    // ===== PHƯƠNG THỨC: getMaDichVu() — Lấy mã dịch vụ. =====
    public String getMaDichVu() {
        return maDichVu;
    }

    // ===== PHƯƠNG THỨC: setMaDichVu(String) — Gán mã dịch vụ. =====
    public void setMaDichVu(String value) {
        if (value == null || !value.matches("DV[0-9]{3}"))
            throw new IllegalArgumentException("Mã dịch vụ không hợp lệ");
        this.maDichVu = value;
    }

    // ===== PHƯƠNG THỨC: getTenDichVu() — Lấy ten dịch vụ. =====
    public String getTenDichVu() {
        return tenDichVu;
    }

    // ===== PHƯƠNG THỨC: setTenDichVu(String) — Gán ten dịch vụ. =====
    public void setTenDichVu(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Tên dịch vụ không được để trống");
        if (value.trim().length() > 150)
            throw new IllegalArgumentException("Tên dịch vụ không được quá 150 ký tự");
        this.tenDichVu = value.trim();
    }

    // ===== PHƯƠNG THỨC: getMoTa() — Lấy mo ta. =====
    public String getMoTa() {
        return moTa;
    }

    // ===== PHƯƠNG THỨC: setMoTa(String) — Gán mo ta. =====
    public void setMoTa(String value) {
        if (value != null && value.length() > 500)
            throw new IllegalArgumentException("Mô tả không được quá 500 ký tự");
        this.moTa = value;
    }

    // ===== PHƯƠNG THỨC: getDonGia() — Lấy đơn giá. =====
    public BigDecimal getDonGia() {
        return donGia;
    }

    // ===== PHƯƠNG THỨC: setDonGia(BigDecimal) — Gán đơn giá. =====
    public void setDonGia(BigDecimal value) {
        if (value == null || value.signum() <= 0)
            throw new IllegalArgumentException("Đơn giá phải > 0");
        if (value.scale() > 2 || value.precision() - value.scale() > 16)
            throw new IllegalArgumentException("Đơn giá quá lớn, vui lòng nhập giá trị nhỏ hơn.");
        this.donGia = value;
    }

    // ===== PHƯƠNG THỨC: getTrangThai() — Lấy trạng thái. =====
    public String getTrangThai() {
        return trangThai;
    }

    // ===== PHƯƠNG THỨC: setTrangThai(String) — Gán trạng thái. =====
    public void setTrangThai(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Trạng thái dịch vụ không được để trống");
        if (value.trim().length() > 50)
            throw new IllegalArgumentException("Trạng thái dịch vụ không được quá 50 ký tự");
        this.trangThai = value.trim();
    }

    // ===== PHƯƠNG THỨC: capNhatDonGia(BigDecimal) — Cập nhật nhat đơn giá. =====
    public void capNhatDonGia(BigDecimal donGiaMoi) {
        setDonGia(donGiaMoi);
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "DichVu [maDichVu=" + maDichVu + ", tenDichVu=" + tenDichVu + ", moTa=" + moTa + ", donGia="
                + donGia + ", trangThai=" + trangThai + "]";
    }

}
