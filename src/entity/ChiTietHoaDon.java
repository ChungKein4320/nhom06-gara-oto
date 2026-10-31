
package entity;

import java.math.BigDecimal;

public class ChiTietHoaDon {
    private HoaDon maHoaDon;
    private DichVu maDichVu;
    private PhuTung maPhuTung;
    private Integer soLuong;
    private BigDecimal donGia;

    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(HoaDon maHoaDon, DichVu maDichVu,
            PhuTung maPhuTung, Integer soLuong, BigDecimal donGia) {

        this.maHoaDon = validateMaHoaDon(maHoaDon);
        this.maDichVu = validateMaDichVu(maDichVu);
        this.maPhuTung = validateMaPhuTung(maPhuTung);
        this.soLuong = validateSoLuong(soLuong);
        this.donGia = validateDonGia(donGia);

        if (maDichVu == null && maPhuTung == null) {
            throw new IllegalArgumentException(
                    "Chi tiết hóa đơn phải có dịch vụ hoặc phụ tùng");
        }
    }

    private static HoaDon validateMaHoaDon(HoaDon value) {
        if (value == null || value.getMaHoaDon() == null
                || value.getMaHoaDon().isBlank()) {
            throw new IllegalArgumentException("Mã hóa đơn không hợp lệ");
        }
        return value;
    }

    private static DichVu validateMaDichVu(DichVu value) {
        if (value != null && (value.getMaDichVu() == null
                || value.getMaDichVu().isBlank())) {
            throw new IllegalArgumentException("Mã dịch vụ không hợp lệ");
        }
        return value;
    }

    private static PhuTung validateMaPhuTung(PhuTung value) {
        if (value != null && (value.getMaPhuTung() == null
                || value.getMaPhuTung().isBlank())) {
            throw new IllegalArgumentException("Mã phụ tùng không hợp lệ");
        }
        return value;
    }

    private static Integer validateSoLuong(Integer value) {
        // SQL cho phép số lượng bằng 0.
        if (value == null || value < 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }
        return value;
    }

    private static BigDecimal validateDonGia(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá không hợp lệ");
        }
        return value;
    }

    public HoaDon getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(HoaDon value) {
        this.maHoaDon = validateMaHoaDon(value);
    }

    public DichVu getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(DichVu value) {
        this.maDichVu = validateMaDichVu(value);
    }

    public PhuTung getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(PhuTung value) {
        this.maPhuTung = validateMaPhuTung(value);
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer value) {
        this.soLuong = validateSoLuong(value);
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal value) {
        this.donGia = validateDonGia(value);
    }

    public BigDecimal tinhThanhTien() {
        if (soLuong == null || donGia == null) {
            throw new IllegalStateException(
                    "Chưa có đủ số lượng và đơn giá để tính thành tiền");
        }

        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }
   @Override
    public String toString() {
        return "ChiTietHoaDon [maHoaDon=" + maHoaDon + ", maDichVu=" + maDichVu
                + ", maPhuTung=" + maPhuTung + ", soLuong=" + soLuong + ", donGia=" + donGia + "]";
    }
}