package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ChiTietHoaDon {
    private Integer maChiTietHoaDon;
    private String maHoaDon;
    private String maDichVu;
    private String maPhuTung;
    private Integer soLuong;
    private BigDecimal donGia;

    public ChiTietHoaDon() { }

    public Integer getMaChiTietHoaDon() { return maChiTietHoaDon; }
    public void setMaChiTietHoaDon(Integer value) { maChiTietHoaDon=value; }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String value) {
        this.maHoaDon = value;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String value) {
        this.maDichVu = value;
    }

    public String getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(String value) {
        this.maPhuTung = value;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer value) {
        this.soLuong = value;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal value) {
        this.donGia = value;
    }

}
