package entity;

import java.math.BigDecimal;

// ===== ENTITY: ChiTietPhieuNhapKho =====
public class ChiTietPhieuNhapKho {
    private String maPNK;
    private PhuTung maPhuTung;
    private Integer soLuongYeuCau;
    private Integer soLuongThucNhan;
    private Integer soLuongNhap;
    private BigDecimal donGiaNhap;

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ChiTietPhieuNhapKho() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public ChiTietPhieuNhapKho() {
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ChiTietPhieuNhapKho(String, PhuTung, Integer, Integer, Integer, BigDecimal) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public ChiTietPhieuNhapKho(String maPNK, PhuTung maPhuTung, Integer soLuongYeuCau,
            Integer soLuongThucNhan, Integer soLuongNhap, BigDecimal donGiaNhap) {
        this.maPNK = maPNK;
        this.maPhuTung = maPhuTung;
        this.soLuongYeuCau = soLuongYeuCau;
        this.soLuongThucNhan = soLuongThucNhan;
        this.soLuongNhap = soLuongNhap;
        this.donGiaNhap = donGiaNhap;
    }

    // ===== PHƯƠNG THỨC: getMaPNK() — Lấy mã pnk. =====
    public String getMaPNK() {
        return maPNK;
    }

    // ===== PHƯƠNG THỨC: setMaPNK(String) — Gán mã pnk. =====
    public void setMaPNK(String value) {
        if (value == null || !value.matches("PNK[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu nhập kho không hợp lệ");
        this.maPNK = value;
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
        if (value != null && value <= 0)
            throw new IllegalArgumentException("Số lượng yêu cầu phải lớn hơn 0");
        this.soLuongYeuCau = value;
    }

    // ===== PHƯƠNG THỨC: getSoLuongThucNhan() — Lấy số lượng thuc nhân. =====
    public Integer getSoLuongThucNhan() {
        return soLuongThucNhan;
    }

    // ===== PHƯƠNG THỨC: setSoLuongThucNhan(Integer) — Gán số lượng thuc nhân. =====
    public void setSoLuongThucNhan(Integer value) {
        if (value != null && value < 0)
            throw new IllegalArgumentException("Số lượng thực nhận không hợp lệ");
        if (value != null && soLuongNhap != null && soLuongNhap > value)
            throw new IllegalArgumentException("Số lượng nhập không được vượt số lượng thực nhận");
        if (value != null && soLuongYeuCau != null && value > soLuongYeuCau)
            throw new IllegalArgumentException("Số lượng thực nhận không được vượt số lượng yêu cầu");
        this.soLuongThucNhan = value;
    }

    // ===== PHƯƠNG THỨC: getSoLuongNhap() — Lấy số lượng nhập. =====
    public Integer getSoLuongNhap() {
        return soLuongNhap;
    }

    // ===== PHƯƠNG THỨC: setSoLuongNhap(Integer) — Gán số lượng nhập. =====
    public void setSoLuongNhap(Integer value) {
        if (value == null || value < 0)
            throw new IllegalArgumentException("Số lượng nhập không hợp lệ");
        if (soLuongThucNhan != null && value > soLuongThucNhan)
            throw new IllegalArgumentException("Số lượng nhập không được vượt số lượng thực nhận");
        this.soLuongNhap = value;
    }

    // ===== PHƯƠNG THỨC: getDonGiaNhap() — Lấy đơn giá nhập. =====
    public BigDecimal getDonGiaNhap() {
        return donGiaNhap;
    }

    // ===== PHƯƠNG THỨC: setDonGiaNhap(BigDecimal) — Gán đơn giá nhập. =====
    public void setDonGiaNhap(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Đơn giá nhập không hợp lệ");
        this.donGiaNhap = value;
    }

    // ===== PHƯƠNG THỨC: tinhThanhTien() — Tính thành tiền. =====
    public BigDecimal tinhThanhTien() {
        if (soLuongNhap == null || donGiaNhap == null)
            throw new IllegalStateException("Chưa đủ số lượng và đơn giá để tính thành tiền");
        return donGiaNhap.multiply(BigDecimal.valueOf(soLuongNhap));
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "ChiTietPhieuNhapKho [maPNK=" + maPNK + ", maPhuTung=" + maPhuTung + ", soLuongYeuCau="
                + soLuongYeuCau + ", soLuongThucNhan=" + soLuongThucNhan + ", soLuongNhap=" + soLuongNhap
                + ", donGiaNhap=" + donGiaNhap + "]";
    }

}
