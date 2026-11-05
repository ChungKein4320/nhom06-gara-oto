
package entity;

import java.math.BigDecimal;

// ===== ENTITY: ĐỐI TƯỢNG CHI TIẾT HÓA ĐƠN =====

public class ChiTietHoaDon {
    // ===== THUỘC TÍNH =====
    private HoaDon maHoaDon;
    private DichVu maDichVu;
    private PhuTung maPhuTung;
    private Integer soLuong;
    private BigDecimal donGia;

    // ===== KHỞI TẠO ĐỐI TƯỢNG =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ChiTietHoaDon() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public ChiTietHoaDon() {
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ChiTietHoaDon(HoaDon, DichVu, PhuTung, Integer, BigDecimal) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public ChiTietHoaDon(HoaDon maHoaDon, DichVu maDichVu, PhuTung maPhuTung, Integer soLuong,
            BigDecimal donGia) {

        this.maHoaDon = validateMaHoaDon(maHoaDon);
        this.maDichVu = validateMaDichVu(maDichVu);
        this.maPhuTung = validateMaPhuTung(maPhuTung);
        this.soLuong = validateSoLuong(soLuong);
        this.donGia = validateDonGia(donGia);

        if (maDichVu == null && maPhuTung == null) {
            throw new IllegalArgumentException("Chi tiết hóa đơn phải có dịch vụ hoặc phụ tùng");
        }
    }

    // ===== PHƯƠNG THỨC: validateMaHoaDon(HoaDon) — Kiểm tra tính hợp lệ của mã hóa đơn. =====
    private static HoaDon validateMaHoaDon(HoaDon value) {
        if (value == null || value.getMaHoaDon() == null || value.getMaHoaDon().isBlank()) {
            throw new IllegalArgumentException("Mã hóa đơn không hợp lệ");
        }
        return value;
    }

    // ===== PHƯƠNG THỨC: validateMaDichVu(DichVu) — Kiểm tra tính hợp lệ của mã dịch vụ. =====
    private static DichVu validateMaDichVu(DichVu value) {
        if (value != null && (value.getMaDichVu() == null || value.getMaDichVu().isBlank())) {
            throw new IllegalArgumentException("Mã dịch vụ không hợp lệ");
        }
        return value;
    }

    // ===== PHƯƠNG THỨC: validateMaPhuTung(PhuTung) — Kiểm tra tính hợp lệ của mã phụ tùng. =====
    private static PhuTung validateMaPhuTung(PhuTung value) {
        if (value != null && (value.getMaPhuTung() == null || value.getMaPhuTung().isBlank())) {
            throw new IllegalArgumentException("Mã phụ tùng không hợp lệ");
        }
        return value;
    }

    // ===== PHƯƠNG THỨC: validateSoLuong(Integer) — Kiểm tra tính hợp lệ của số lượng. =====
    private static Integer validateSoLuong(Integer value) {
        // SQL cho phép số lượng bằng 0.
        if (value == null || value < 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }
        return value;
    }

    // ===== PHƯƠNG THỨC: validateDonGia(BigDecimal) — Kiểm tra tính hợp lệ của đơn giá. =====
    private static BigDecimal validateDonGia(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá không hợp lệ");
        }
        return value;
    }

    // ===== GETTER VÀ SETTER =====

    // ===== PHƯƠNG THỨC: getMaHoaDon() — Lấy mã hóa đơn. =====
    public HoaDon getMaHoaDon() {
        return maHoaDon;
    }

    // ===== PHƯƠNG THỨC: setMaHoaDon(HoaDon) — Gán mã hóa đơn. =====
    public void setMaHoaDon(HoaDon value) {
        this.maHoaDon = validateMaHoaDon(value);
    }

    // ===== PHƯƠNG THỨC: getMaDichVu() — Lấy mã dịch vụ. =====
    public DichVu getMaDichVu() {
        return maDichVu;
    }

    // ===== PHƯƠNG THỨC: setMaDichVu(DichVu) — Gán mã dịch vụ. =====
    public void setMaDichVu(DichVu value) {
        this.maDichVu = validateMaDichVu(value);
    }

    // ===== PHƯƠNG THỨC: getMaPhuTung() — Lấy mã phụ tùng. =====
    public PhuTung getMaPhuTung() {
        return maPhuTung;
    }

    // ===== PHƯƠNG THỨC: setMaPhuTung(PhuTung) — Gán mã phụ tùng. =====
    public void setMaPhuTung(PhuTung value) {
        this.maPhuTung = validateMaPhuTung(value);
    }

    // ===== PHƯƠNG THỨC: getSoLuong() — Lấy số lượng. =====
    public Integer getSoLuong() {
        return soLuong;
    }

    // ===== PHƯƠNG THỨC: setSoLuong(Integer) — Gán số lượng. =====
    public void setSoLuong(Integer value) {
        this.soLuong = validateSoLuong(value);
    }

    // ===== PHƯƠNG THỨC: getDonGia() — Lấy đơn giá. =====
    public BigDecimal getDonGia() {
        return donGia;
    }

    // ===== PHƯƠNG THỨC: setDonGia(BigDecimal) — Gán đơn giá. =====
    public void setDonGia(BigDecimal value) {
        this.donGia = validateDonGia(value);
    }

    // ===== PHƯƠNG THỨC: tinhThanhTien() — Tính thành tiền. =====
    public BigDecimal tinhThanhTien() {
        if (soLuong == null || donGia == null) {
            throw new IllegalStateException("Chưa có đủ số lượng và đơn giá để tính thành tiền");
        }

        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "ChiTietHoaDon [maHoaDon=" + maHoaDon + ", maDichVu=" + maDichVu + ", maPhuTung=" + maPhuTung
                + ", soLuong=" + soLuong + ", donGia=" + donGia + "]";
    }
}