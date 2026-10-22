package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class TaiKhoan {
    private String maTaiKhoan;
    private String maNhanVien;
    private String tenDangNhap;
    private String matKhau;
    private String trangThai;
    private Timestamp ngayTao;

    public TaiKhoan() { }

    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public void setMaTaiKhoan(String value) {
        this.maTaiKhoan = value;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String value) {
        this.maNhanVien = value;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String value) {
        this.tenDangNhap = value;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String value) {
        this.matKhau = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        this.trangThai = value;
    }

    public Timestamp getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Timestamp value) {
        this.ngayTao = value;
    }

}