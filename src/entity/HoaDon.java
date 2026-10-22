package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class HoaDon {
    private String maHoaDon;
    private String maKhachHang;
    private String maNhanVien;
    private String maPSC;
    private String VIN;
    private Timestamp ngayLap;
    private BigDecimal tienPhuTung;
    private BigDecimal tienCong;
    private BigDecimal dichVuKhac;
    private BigDecimal thueVAT;
    private BigDecimal giamGia;
    private String maPhuongThuc;
    private String trangThaiThanhToan;
    private String maGiamGia;

    public HoaDon() { }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String value) {
        this.maHoaDon = value;
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

    public String getMaPSC() {
        return maPSC;
    }

    public void setMaPSC(String value) {
        this.maPSC = value;
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

    public BigDecimal getTienPhuTung() {
        return tienPhuTung;
    }

    public void setTienPhuTung(BigDecimal value) {
        this.tienPhuTung = value;
    }

    public BigDecimal getTienCong() {
        return tienCong;
    }

    public void setTienCong(BigDecimal value) {
        this.tienCong = value;
    }

    public BigDecimal getDichVuKhac() {
        return dichVuKhac;
    }

    public void setDichVuKhac(BigDecimal value) {
        this.dichVuKhac = value;
    }

    public BigDecimal getThueVAT() {
        return thueVAT;
    }

    public void setThueVAT(BigDecimal value) {
        this.thueVAT = value;
    }

    public BigDecimal getGiamGia() {
        return giamGia;
    }

    public void setGiamGia(BigDecimal value) {
        this.giamGia = value;
    }

    public String getMaPhuongThuc() {
        return maPhuongThuc;
    }

    public void setMaPhuongThuc(String value) {
        this.maPhuongThuc = value;
    }

    public String getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    public void setTrangThaiThanhToan(String value) {
        this.trangThaiThanhToan = value;
    }

    public String getMaGiamGia() {
        return maGiamGia;
    }

    public void setMaGiamGia(String value) {
        this.maGiamGia = value;
    }

}