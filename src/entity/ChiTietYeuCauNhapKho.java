package entity;

import java.math.BigDecimal;

public class ChiTietYeuCauNhapKho {
    private String maYCNK;
    private String maPhuTung;
    private Integer soLuongYeuCau;
    private BigDecimal donGiaDuKien;

    public ChiTietYeuCauNhapKho() { }

    public String getMaYCNK() {
        return maYCNK;
    }

    public void setMaYCNK(String value) {
        this.maYCNK = value;
    }

    public String getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(String value) {
        this.maPhuTung = value;
    }

    public Integer getSoLuongYeuCau() {
        return soLuongYeuCau;
    }

    public void setSoLuongYeuCau(Integer value) {
        this.soLuongYeuCau = value;
    }

    public BigDecimal getDonGiaDuKien() {
        return donGiaDuKien;
    }

    public void setDonGiaDuKien(BigDecimal value) {
        this.donGiaDuKien = value;
    }
}
