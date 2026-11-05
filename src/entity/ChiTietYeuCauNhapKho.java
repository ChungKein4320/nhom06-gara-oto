package entity;

import java.math.BigDecimal;

// ===== ENTITY: ChiTietYeuCauNhapKho =====
public class ChiTietYeuCauNhapKho {
    private String maYCNK;
    private PhuTung maPhuTung;
    private Integer soLuongYeuCau;
    private BigDecimal donGiaDuKien;

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ChiTietYeuCauNhapKho() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public ChiTietYeuCauNhapKho() {
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ChiTietYeuCauNhapKho(String, PhuTung, Integer, BigDecimal) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public ChiTietYeuCauNhapKho(String maYCNK, PhuTung maPhuTung, Integer soLuongYeuCau,
            BigDecimal donGiaDuKien) {
        this.maYCNK = maYCNK;
        this.maPhuTung = maPhuTung;
        this.soLuongYeuCau = soLuongYeuCau;
        this.donGiaDuKien = donGiaDuKien;
    }

    // ===== PHƯƠNG THỨC: getMaYCNK() — Lấy mã ycnk. =====
    public String getMaYCNK() {
        return maYCNK;
    }

    // ===== PHƯƠNG THỨC: setMaYCNK(String) — Gán mã ycnk. =====
    public void setMaYCNK(String value) {
        if (value == null || !value.matches("YCN[0-9]{3}"))
            throw new IllegalArgumentException("Mã yêu cầu nhập kho không hợp lệ");
        this.maYCNK = value;
    }

    // ===== PHƯƠNG THỨC: getMaPhuTung() — Lấy mã phụ tùng. =====
    public PhuTung getMaPhuTung() {
        return maPhuTung;
    }

    // ===== PHƯƠNG THỨC: setMaPhuTung(PhuTung) — Gán mã phụ tùng. =====
    public void setMaPhuTung(PhuTung value) {
        if (value == null || value.getMaPhuTung() == null || !value.getMaPhuTung().matches("PT[0-9]{3}")
                || "PT000".equals(value.getMaPhuTung()))
            throw new IllegalArgumentException("Mã phụ tùng không hợp lệ");
        this.maPhuTung = value;
    }

    // ===== PHƯƠNG THỨC: getSoLuongYeuCau() — Lấy số lượng yêu cầu. =====
    public Integer getSoLuongYeuCau() {
        return soLuongYeuCau;
    }

    // ===== PHƯƠNG THỨC: setSoLuongYeuCau(Integer) — Gán số lượng yêu cầu. =====
    public void setSoLuongYeuCau(Integer value) {
        if (value == null || value <= 0)
            throw new IllegalArgumentException("Số lượng yêu cầu phải lớn hơn 0");
        this.soLuongYeuCau = value;
    }

    // ===== PHƯƠNG THỨC: getDonGiaDuKien() — Lấy đơn giá dữ kien. =====
    public BigDecimal getDonGiaDuKien() {
        return donGiaDuKien;
    }

    // ===== PHƯƠNG THỨC: setDonGiaDuKien(BigDecimal) — Gán đơn giá dữ kien. =====
    public void setDonGiaDuKien(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Đơn giá dự kiến không hợp lệ");
        this.donGiaDuKien = value;
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "ChiTietYeuCauNhapKho [maYCNK=" + maYCNK + ", maPhuTung=" + maPhuTung + ", soLuongYeuCau="
                + soLuongYeuCau + ", donGiaDuKien=" + donGiaDuKien + "]";
    }
}
