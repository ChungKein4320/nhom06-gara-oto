package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class PhieuTiepNhan {
    private String maPTN;
    private String maKhachHang;
    private String VIN;
    private String maNhanVien;
    private Timestamp ngayTiepNhan;
    private String soODO;

    public PhieuTiepNhan() { }

    public String getMaPTN() {
        return maPTN;
    }

    public void setMaPTN(String value) {
        this.maPTN = value;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String value) {
        this.maKhachHang = value;
    }

    public String getVIN() {
        return VIN;
    }

    public void setVIN(String value) {
        this.VIN = value;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String value) {
        this.maNhanVien = value;
    }

    public Timestamp getNgayTiepNhan() {
        return ngayTiepNhan;
    }

    public void setNgayTiepNhan(Timestamp value) {
        this.ngayTiepNhan = value;
    }

    public String getSoODO() {
        return soODO;
    }

    public void setSoODO(String value) {
        this.soODO = value;
    }

}
