package entity;

import java.math.BigDecimal;

public class PhuongTien {
    private String VIN;
    private String bienSo;
    private String hangXe;
    private String dongXe;
    private String mauXe;
    private Integer namSanXuat;
    private String soMay;
    private BigDecimal soKm;

    public PhuongTien() { }

    public String getVIN() {
        return VIN;
    }

    public void setVIN(String value) {
        this.VIN = value;
    }

    public String getBienSo() {
        return bienSo;
    }

    public void setBienSo(String value) {
        this.bienSo = value;
    }

    public String getHangXe() {
        return hangXe;
    }

    public void setHangXe(String value) {
        this.hangXe = value;
    }

    public String getDongXe() {
        return dongXe;
    }

    public void setDongXe(String value) {
        this.dongXe = value;
    }

    public String getMauXe() {
        return mauXe;
    }

    public void setMauXe(String value) {
        this.mauXe = value;
    }

    public Integer getNamSanXuat() {
        return namSanXuat;
    }

    public void setNamSanXuat(Integer value) {
        this.namSanXuat = value;
    }

    public String getSoMay() {
        return soMay;
    }

    public void setSoMay(String value) {
        this.soMay = value;
    }

    public BigDecimal getSoKm() {
        return soKm;
    }

    public void setSoKm(BigDecimal value) {
        this.soKm = value;
    }

}