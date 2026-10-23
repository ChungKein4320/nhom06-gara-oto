package entity;

import java.math.BigDecimal;

public class PhuTung {
    private String maPhuTung;
    private String tenPhuTung;
    private BigDecimal donGia;
    private String loaiPhuTung;

    public PhuTung() { }

    public String getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(String value) {
        this.maPhuTung = value;
    }

    public String getTenPhuTung() {
        return tenPhuTung;
    }

    public void setTenPhuTung(String value) {
        this.tenPhuTung = value;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal value) {
        this.donGia = value;
    }

    public String getLoaiPhuTung() {
        return loaiPhuTung;
    }

    public void setLoaiPhuTung(String value) {
        this.loaiPhuTung = value;
    }

}