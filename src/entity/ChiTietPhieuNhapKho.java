package entity;

import java.math.BigDecimal;

public class ChiTietPhieuNhapKho {
    private String maPNK;
    private String maPhuTung;
    private Integer soLuongYeuCau;
    private Integer soLuongThucNhan;
    private Integer soLuongNhap;
    private BigDecimal donGiaNhap;

    public ChiTietPhieuNhapKho() { }

    public String getMaPNK() {
        return maPNK;
    }

    public void setMaPNK(String value) {
        this.maPNK = value;
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

    public Integer getSoLuongThucNhan() {
        return soLuongThucNhan;
    }

    public void setSoLuongThucNhan(Integer value) {
        this.soLuongThucNhan = value;
    }

    public Integer getSoLuongNhap() {
        return soLuongNhap;
    }

    public void setSoLuongNhap(Integer value) {
        this.soLuongNhap = value;
    }

    public BigDecimal getDonGiaNhap() {
        return donGiaNhap;
    }

    public void setDonGiaNhap(BigDecimal value) {
        this.donGiaNhap = value;
    }

}
