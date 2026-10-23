package entity;

import java.sql.Timestamp;

public class LichNhacBaoDuong {
    private String maLichNhac;
    private String maKhachHang;
    private String maPSC;
    private String VIN;
    private Timestamp ngayNhac;
    private String noiDung;
    private String trangThai;

    public LichNhacBaoDuong() { }

    public String getMaLichNhac() {
        return maLichNhac;
    }

    public void setMaLichNhac(String value) {
        this.maLichNhac = value;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String value) {
        this.maKhachHang = value;
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

    public Timestamp getNgayNhac() {
        return ngayNhac;
    }

    public void setNgayNhac(Timestamp value) {
        this.ngayNhac = value;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String value) {
        this.noiDung = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        this.trangThai = value;
    }

}