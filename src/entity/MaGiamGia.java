package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class MaGiamGia {
    private String maGiamGia;
    private String maKhachHang;
    private BigDecimal mucGiam;
    private Timestamp ngayTao;
    private Timestamp ngayHetHan;
    private String trangThai;

    public MaGiamGia() { }

    public String getMaGiamGia() {
        return maGiamGia;
    }

    public void setMaGiamGia(String value) {
        this.maGiamGia = value;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String value) {
        this.maKhachHang = value;
    }

    public BigDecimal getMucGiam() {
        return mucGiam;
    }

    public void setMucGiam(BigDecimal value) {
        this.mucGiam = value;
    }

    public Timestamp getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Timestamp value) {
        this.ngayTao = value;
    }

    public Timestamp getNgayHetHan() {
        return ngayHetHan;
    }

    public void setNgayHetHan(Timestamp value) {
        this.ngayHetHan = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        this.trangThai = value;
    }

}