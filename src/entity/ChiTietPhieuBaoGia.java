package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ChiTietPhieuBaoGia {
    private String maBaoGia;
    private String maDichVu;
    private String maPhuTung;
    private Integer soLuongPhuTung;
    private BigDecimal donGiaPhuTung;
    private BigDecimal chiPhiNhanCong;
    private String trangThai;

    public ChiTietPhieuBaoGia() { }

    public String getMaBaoGia() {
        return maBaoGia;
    }

    public void setMaBaoGia(String value) {
        this.maBaoGia = value;
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

    public Integer getSoLuongPhuTung() {
        return soLuongPhuTung;
    }

    public void setSoLuongPhuTung(Integer value) {
        this.soLuongPhuTung = value;
    }

    public BigDecimal getDonGiaPhuTung() {
        return donGiaPhuTung;
    }

    public void setDonGiaPhuTung(BigDecimal value) {
        this.donGiaPhuTung = value;
    }

    public BigDecimal getChiPhiNhanCong() {
        return chiPhiNhanCong;
    }

    public void setChiPhiNhanCong(BigDecimal value) {
        this.chiPhiNhanCong = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        this.trangThai = value;
    }

}