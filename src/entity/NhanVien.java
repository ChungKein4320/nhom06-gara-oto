package entity;

import java.sql.Timestamp;

public class NhanVien {
    private String maNhanVien;
    private String hoTenNV;
    private Timestamp ngaySinh;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private String vaiTro;
    private boolean gioiTinh;
    private String trangThai;

    public NhanVien() { }

    public boolean isGioiTinh() { return gioiTinh; }
    public boolean getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(boolean value) { gioiTinh = value; }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String value) {
        this.maNhanVien = value;
    }

    public String getHoTenNV() {
        return hoTenNV;
    }

    public void setHoTenNV(String value) {
        this.hoTenNV = value;
    }

    public Timestamp getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Timestamp value) {
        this.ngaySinh = value;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String value) {
        this.soDienThoai = value;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        this.email = value;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String value) {
        this.diaChi = value;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String value) {
        this.vaiTro = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        this.trangThai = value;
    }

}
