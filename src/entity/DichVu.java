package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class DichVu {
    private String maDichVu;
    private String tenDichVu;
    private String moTa;
    private BigDecimal donGia;
    private String trangThai;

    public DichVu() { }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String value) {
        this.maDichVu = value;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String value) {
        this.tenDichVu = value;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String value) {
        this.moTa = value;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal value) {
        this.donGia = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        this.trangThai = value;
    }

}