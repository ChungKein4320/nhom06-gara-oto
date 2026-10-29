package entity;

import java.sql.Timestamp;

public class PhieuSuaChua {
    private String maPSC;
    private String maBaoGia;
    private String maKhachHang;
    private String VIN;
    private String maNhanVien;
    private Timestamp ngayLap;
    private Timestamp ngayHoanThanh;
    private String trangThai;
    private String lyDoHuy;
    private String maNguoiDuyetHuy;

    public PhieuSuaChua() { }

    public String getMaPSC() {
        return maPSC;
    }

    public void setMaPSC(String value) {
        this.maPSC = value;
    }

    public String getMaBaoGia() {
        return maBaoGia;
    }

    public void setMaBaoGia(String value) {
        this.maBaoGia = value;
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

    public Timestamp getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(Timestamp value) {
        this.ngayLap = value;
    }

    public Timestamp getNgayHoanThanh() {
        return ngayHoanThanh;
    }

    public void setNgayHoanThanh(Timestamp value) {
        this.ngayHoanThanh = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        this.trangThai = value;
    }

    public String getLyDoHuy() { return lyDoHuy; }
    public void setLyDoHuy(String value) { this.lyDoHuy = value; }
    public String getMaNguoiDuyetHuy() { return maNguoiDuyetHuy; }
    public void setMaNguoiDuyetHuy(String value) { this.maNguoiDuyetHuy = value; }

}
