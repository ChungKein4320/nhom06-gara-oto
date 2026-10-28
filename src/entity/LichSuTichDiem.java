package entity;

import java.sql.Timestamp;

public class LichSuTichDiem {
    private String maLichSuTD;
    private String maKhachHang;
    private String maHoaDon;
    private Timestamp ngayTichDiem;
    private Timestamp ngayHetHan;
    private Integer soDiem;
    private String lyDo;

    public LichSuTichDiem() { }

    public String getMaLichSuTD() {
        return maLichSuTD;
    }

    public void setMaLichSuTD(String value) {
        this.maLichSuTD = value;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String value) {
        this.maKhachHang = value;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String value) {
        this.maHoaDon = value;
    }

    public Timestamp getNgayTichDiem() {
        return ngayTichDiem;
    }

    public void setNgayTichDiem(Timestamp value) {
        this.ngayTichDiem = value;
    }

    public Timestamp getNgayHetHan() { return ngayHetHan; }
    public void setNgayHetHan(Timestamp value) { this.ngayHetHan = value; }

    public Integer getSoDiem() {
        return soDiem;
    }

    public void setSoDiem(Integer value) {
        this.soDiem = value;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String value) {
        this.lyDo = value;
    }

}
