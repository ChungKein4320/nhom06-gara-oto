package entity;

import java.math.BigDecimal;

public class ChiTietPhieuNhapKho {
    private String maPNK;
    private PhuTung maPhuTung;
    private Integer soLuongYeuCau;
    private Integer soLuongThucNhan;
    private Integer soLuongNhap;
    private BigDecimal donGiaNhap;

    public ChiTietPhieuNhapKho() { }

    public ChiTietPhieuNhapKho(String maPNK, PhuTung maPhuTung, Integer soLuongYeuCau,
            Integer soLuongThucNhan, Integer soLuongNhap, BigDecimal donGiaNhap) {
        this.maPNK = maPNK;
        this.maPhuTung = maPhuTung;
        this.soLuongYeuCau = soLuongYeuCau;
        this.soLuongThucNhan = soLuongThucNhan;
        this.soLuongNhap = soLuongNhap;
        this.donGiaNhap = donGiaNhap;
    }

    public String getMaPNK() {
        return maPNK;
    }

    public void setMaPNK(String value) {
        if (value == null || !value.matches("PNK[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu nhập kho không hợp lệ");
        this.maPNK = value;
    }

    public PhuTung getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(PhuTung value) {
        if (value == null || value.getMaPhuTung() == null || !value.getMaPhuTung().matches("PT[0-9]{3}")
                || "PT000".equals(value.getMaPhuTung()))
            throw new IllegalArgumentException("Mã phụ tùng không hợp lệ");
        this.maPhuTung = value;
    }

    public Integer getSoLuongYeuCau() {
        return soLuongYeuCau;
    }

    public void setSoLuongYeuCau(Integer value) {
        if (value != null && value <= 0)
            throw new IllegalArgumentException("Số lượng yêu cầu phải lớn hơn 0");
        this.soLuongYeuCau = value;
    }

    public Integer getSoLuongThucNhan() {
        return soLuongThucNhan;
    }

    public void setSoLuongThucNhan(Integer value) {
        if (value != null && value < 0)
            throw new IllegalArgumentException("Số lượng thực nhận không hợp lệ");
        if (value != null && soLuongNhap != null && soLuongNhap > value)
            throw new IllegalArgumentException("Số lượng nhập không được vượt số lượng thực nhận");
        if (value != null && soLuongYeuCau != null && value > soLuongYeuCau)
            throw new IllegalArgumentException("Số lượng thực nhận không được vượt số lượng yêu cầu");
        this.soLuongThucNhan = value;
    }

    public Integer getSoLuongNhap() {
        return soLuongNhap;
    }

    public void setSoLuongNhap(Integer value) {
        if (value == null || value < 0)
            throw new IllegalArgumentException("Số lượng nhập không hợp lệ");
        if (soLuongThucNhan != null && value > soLuongThucNhan)
            throw new IllegalArgumentException("Số lượng nhập không được vượt số lượng thực nhận");
        this.soLuongNhap = value;
    }

    public BigDecimal getDonGiaNhap() {
        return donGiaNhap;
    }

    public void setDonGiaNhap(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Đơn giá nhập không hợp lệ");
        this.donGiaNhap = value;
    }

    public BigDecimal tinhThanhTien() {
        if (soLuongNhap == null || donGiaNhap == null)
            throw new IllegalStateException("Chưa đủ số lượng và đơn giá để tính thành tiền");
        return donGiaNhap.multiply(BigDecimal.valueOf(soLuongNhap));
    }

    @Override
    public String toString() {
        return "ChiTietPhieuNhapKho [maPNK=" + maPNK + ", maPhuTung=" + maPhuTung
                + ", soLuongYeuCau=" + soLuongYeuCau + ", soLuongThucNhan=" + soLuongThucNhan
                + ", soLuongNhap=" + soLuongNhap + ", donGiaNhap=" + donGiaNhap + "]";
    }

}
