package entity;

import java.sql.Timestamp;

public class PhieuNhapKho {
    private String maPNK;
    private String maNCC;
    private String maNhanVien;
    private Timestamp ngayNhap;
    private String trangThai;

    public PhieuNhapKho() { }

    public String getMaPNK() {
        return maPNK;
    }

    public void setMaPNK(String value) {
        this.maPNK = value;
    }

    public String getMaNCC() {
        return maNCC;
    }

    public void setMaNCC(String value) {
        this.maNCC = value;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String value) {
        this.maNhanVien = value;
    }

    public Timestamp getNgayNhap() {
        return ngayNhap;
    }

    public void setNgayNhap(Timestamp value) {
        this.ngayNhap = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        this.trangThai = value;
    }

}