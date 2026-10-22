package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class KhachHang {
    private String maKhachHang;
    private String hoTenKH;
    private String soDienThoai;
    private boolean gioiTinh;
    private String email;
    private String diaChi;

    public KhachHang() { }

    public boolean isGioiTinh() { return gioiTinh; }
    public boolean getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(boolean value) { gioiTinh = value; }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String value) {
        this.maKhachHang = value;
    }

    public String getHoTenKH() {
        return hoTenKH;
    }

    public void setHoTenKH(String value) {
        this.hoTenKH = value;
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

}
