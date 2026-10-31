package entity;

import java.sql.Timestamp;

public class LichNhacBaoDuong {
    private String maLichNhac;
    private KhachHang maKhachHang;
    private PhieuSuaChua maPSC;
    private PhuongTien VIN;
    private Timestamp ngayNhac;
    private String noiDung;
    private String trangThai;

    public LichNhacBaoDuong() { }

    public String getMaLichNhac() {
        return maLichNhac;
    }

    public void setMaLichNhac(String value) {
        if (value == null || !value.matches("LNBD[0-9]{3}"))
            throw new IllegalArgumentException("Mã lịch nhắc không hợp lệ");
        this.maLichNhac = value;
    }

    public KhachHang getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(KhachHang   value) {
        if (value == null || value.getMaKhachHang() == null || !value.getMaKhachHang().matches("KH[0-9]{3}"))
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ");
        this.maKhachHang = value;
    }

    public PhieuSuaChua getMaPSC() {
        return maPSC;
    }

    public void setMaPSC(PhieuSuaChua value) {
        if (value == null || value.getMaPSC() == null || !value.getMaPSC().matches("PSC[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu sửa chữa không hợp lệ");
        this.maPSC = value;
    }

    public PhuongTien getVIN() {
        return VIN;
    }

    public void setVIN(PhuongTien value) {
        if (value == null || value.getVIN() == null || !value.getVIN().matches("[A-HJ-NPR-Z0-9]{17}"))
            throw new IllegalArgumentException("Phương tiện không hợp lệ");
        this.VIN = value;
    }

    public Timestamp getNgayNhac() {
        return ngayNhac;
    }

    public void setNgayNhac(Timestamp value) {
        if (value == null) throw new IllegalArgumentException("Ngày nhắc không hợp lệ");
        this.ngayNhac = value;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Nội dung không được rỗng");
        this.noiDung = value.trim();
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Trạng thái không được rỗng");
        this.trangThai = value.trim();
    }

     public LichNhacBaoDuong(String maLichNhac,
            KhachHang maKhachHang, PhuongTien VIN,
            PhieuSuaChua maPSC, Timestamp ngayNhac,
            String noiDung, String trangThai) {

        this.maLichNhac = maLichNhac;
        this.maKhachHang = maKhachHang;
        this.VIN = VIN;
        this.maPSC = maPSC;
        this.ngayNhac = ngayNhac;
        this.noiDung = noiDung;
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {
        return "LichNhacBaoDuong{"
                + "maLichNhac='" + maLichNhac + '\''
                + ", maKhachHang=" + maKhachHang
                + ", maPSC=" + maPSC
                + ", VIN=" + VIN
                + ", ngayNhac=" + ngayNhac
                + ", noiDung='" + noiDung + '\''
                + ", trangThai='" + trangThai + '\''
                + '}';
    }

}
