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
        if (value == null || !value.matches("LS[0-9]{3}"))
            throw new IllegalArgumentException("Mã lịch sử không hợp lệ");
        this.maLichSuTD = value;
    }

    public KhachHang getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(KhachHang value) {
        if (value == null || value.getMaKhachHang() == null
                || !value.getMaKhachHang().matches("KH[0-9]{3}"))
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ");
        this.maKhachHang = value;
    }

    public HoaDon getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(HoaDon value) {
        if (value == null || value.getMaHoaDon() == null
                || !value.getMaHoaDon().matches("HD[0-9]{3}"))
            throw new IllegalArgumentException("Mã hóa đơn không hợp lệ");
        this.maHoaDon = value;
    }

    public Timestamp getNgayTichDiem() {
        return ngayTichDiem;
    }

    public void setNgayTichDiem(Timestamp value) {
        if (value == null || value.after(new Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày tích điểm không hợp lệ");
        this.ngayTichDiem = value;
    }

    public Timestamp getNgayHetHan() { return ngayHetHan; }
    public void setNgayHetHan(Timestamp value) {
        if (value != null && ngayTichDiem != null && value.before(ngayTichDiem))
            throw new IllegalArgumentException("Ngày hết hạn không được trước ngày tích điểm");
        this.ngayHetHan = value;
    }

    public Integer getSoDiem() {
        return soDiem;
    }

    public void setSoDiem(Integer value) {
        if (value == null || value <= 0)
            throw new IllegalArgumentException("Số điểm phải lớn hơn 0");
        this.soDiem = value;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Lý do không được rỗng");
        this.lyDo = value.trim();
    }

     public LichSuTichDiem(String maLichSu,
            KhachHang maKhachHang, HoaDon maHoaDon,
            Timestamp ngayTichDiem, Integer soDiem, String lyDo) {

        this.maLichSuTD = maLichSu;
        this.maKhachHang = maKhachHang;
        this.maHoaDon = maHoaDon;
        this.ngayTichDiem = ngayTichDiem;
        this.soDiem = soDiem;
        this.lyDo = lyDo;
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
