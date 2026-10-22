package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class PhieuKiemTra {
    private String maPKT;
    private String maPTN;
    private String maNhanVien;
    private String VIN;
    private Timestamp ngayKiemTra;
    private String maKhachHang;

    public PhieuKiemTra() { }

    public String getMaPKT() {
        return maPKT;
    }

    public void setMaPKT(String value) {
        this.maPKT = value;
    }

    public String getMaPTN() {
        return maPTN;
    }

    public void setMaPTN(String value) {
        this.maPTN = value;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String value) {
        this.maNhanVien = value;
    }

    public String getVIN() {
        return VIN;
    }

    public void setVIN(String value) {
        this.VIN = value;
    }

    public Timestamp getNgayKiemTra() {
        return ngayKiemTra;
    }

    public void setNgayKiemTra(Timestamp value) {
        this.ngayKiemTra = value;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String value) {
        this.maKhachHang = value;
    }

}