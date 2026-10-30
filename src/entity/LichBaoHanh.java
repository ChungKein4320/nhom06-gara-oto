package entity;

import java.sql.Timestamp;

public class LichBaoHanh {
    private String maLBH;
    private String maPSC;
    private String maDichVu;
    private String maPhuTung;
    private String maNhanVien;
    private String maKhachHang;
    private String VIN;
    private Timestamp ngayXuLy;
    private String ketQuaXuLy;
    private String ghiChu;

    public LichBaoHanh() { }

    public String getMaLBH() {
        return maLBH;
    }

    public void setMaLBH(String value) {
        this.maLBH = value;
    }

    public String getMaPSC() {
        return maPSC;
    }

    public void setMaPSC(String value) {
        this.maPSC = value;
    }

    public String getMaDichVu() { return maDichVu; }
    public void setMaDichVu(String value) { this.maDichVu = value; }
    public String getMaPhuTung() { return maPhuTung; }
    public void setMaPhuTung(String value) { this.maPhuTung = value; }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String value) {
        this.maNhanVien = value;
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

    public Timestamp getNgayXuLy() {
        return ngayXuLy;
    }

    public void setNgayXuLy(Timestamp value) {
        this.ngayXuLy = value;
    }

    public String getKetQuaXuLy() {
        return ketQuaXuLy;
    }

    public void setKetQuaXuLy(String value) {
        this.ketQuaXuLy = value;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String value) {
        this.ghiChu = value;
    }

}
