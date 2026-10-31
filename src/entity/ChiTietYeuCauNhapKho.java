package entity;

import java.math.BigDecimal;

public class ChiTietYeuCauNhapKho {
    private String maYCNK;
    private PhuTung maPhuTung;
    private Integer soLuongYeuCau;
    private BigDecimal donGiaDuKien;

    public ChiTietYeuCauNhapKho() { }

    public ChiTietYeuCauNhapKho(String maYCNK, PhuTung maPhuTung,
            Integer soLuongYeuCau, BigDecimal donGiaDuKien) {
        this.maYCNK = maYCNK;
        this.maPhuTung = maPhuTung;
        this.soLuongYeuCau = soLuongYeuCau;
        this.donGiaDuKien = donGiaDuKien;
    }

    public String getMaYCNK() {
        return maYCNK;
    }

    public void setMaYCNK(String value) {
        if (value == null || !value.matches("YCN[0-9]{3}"))
            throw new IllegalArgumentException("Mã yêu cầu nhập kho không hợp lệ");
        this.maYCNK = value;
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
        if (value == null || value <= 0)
            throw new IllegalArgumentException("Số lượng yêu cầu phải lớn hơn 0");
        this.soLuongYeuCau = value;
    }

    public BigDecimal getDonGiaDuKien() {
        return donGiaDuKien;
    }

    public void setDonGiaDuKien(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Đơn giá dự kiến không hợp lệ");
        this.donGiaDuKien = value;
    }

    @Override
    public String toString() {
        return "ChiTietYeuCauNhapKho [maYCNK=" + maYCNK + ", maPhuTung=" + maPhuTung
                + ", soLuongYeuCau=" + soLuongYeuCau + ", donGiaDuKien=" + donGiaDuKien + "]";
    }
}
