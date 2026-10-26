package entity;

import java.sql.Timestamp;

public class PhieuBaoGia {
    private String maBaoGia;
    private String maPKT;
    private String maKhachHang;
    private String maNhanVien;
    private String VIN;
    private Timestamp ngayLap;
    private Timestamp ngayHetHan;
    private Timestamp thoiGianXacNhan;
    private String trangThai;

    public PhieuBaoGia() { }

    public String getMaBaoGia() {
        return maBaoGia;
    }

    public void setMaBaoGia(String value) {
        this.maBaoGia = value;
    }

    public String getMaPKT() {
        return maPKT;
    }

    public void setMaPKT(String value) {
        this.maPKT = value;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String value) {
        this.maKhachHang = value;
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

    public Timestamp getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(Timestamp value) {
        this.ngayLap = value;
    }

    public Timestamp getNgayHetHan() {
        return ngayHetHan;
    }

    public void setNgayHetHan(Timestamp value) {
        this.ngayHetHan = value;
    }

    public Timestamp getThoiGianXacNhan() {
        return thoiGianXacNhan;
    }

    public void setThoiGianXacNhan(Timestamp value) {
        this.thoiGianXacNhan = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        this.trangThai = value;
    }

}
