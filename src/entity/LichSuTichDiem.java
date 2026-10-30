package entity;

import java.sql.Timestamp;

public class LichSuTichDiem {
    private String maLichSuTD;
    private KhachHang maKhachHang;
    private HoaDon maHoaDon;
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

    public KhachHang getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(KhachHang value) {
        this.maKhachHang = value;
    }

    public HoaDon getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(HoaDon value) {
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

     public LichSuTichDiem(String maLichSu,
            KhachHang maKhachHang, HoaDon maHoaDon,
            Timestamp ngayTichDiem, Integer soDiem, String lyDo) {

        this();
        setMaKhachHang(maKhachHang);
        setMaHoaDon(maHoaDon);
        setNgayTichDiem(ngayTichDiem);
        setSoDiem(soDiem);
        setLyDo(lyDo);
    }

    @Override
    public String toString() {
        return "LichSuTichDiem{"
                + "maLichSuTD='" + maLichSuTD + '\''
                + ", maKhachHang=" + maKhachHang
                + ", maHoaDon=" + maHoaDon
                + ", ngayTichDiem=" + ngayTichDiem
                + ", ngayHetHan=" + ngayHetHan
                + ", soDiem=" + soDiem
                + ", lyDo='" + lyDo + '\''
                + '}';
    }

}
