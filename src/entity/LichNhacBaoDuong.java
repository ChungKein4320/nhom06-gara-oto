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
        this.maLichNhac = value;
    }

    public KhachHang getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(KhachHang   value) {
        this.maKhachHang = value;
    }

    public PhieuSuaChua getMaPSC() {
        return maPSC;
    }

    public void setMaPSC(PhieuSuaChua value) {
        this.maPSC = value;
    }

    public PhuongTien getVIN() {
        return VIN;
    }

    public void setVIN(PhuongTien value) {
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

     public LichNhacBaoDuong(String maLichNhac,
            KhachHang maKhachHang, PhuongTien VIN,
            PhieuSuaChua maPSC, Timestamp ngayNhac,
            String noiDung, String trangThai) {

        this();
        setMaKhachHang(maKhachHang);
        setVIN(VIN);
        setMaPSC(maPSC);
        setNgayNhac(ngayNhac);
        setNoiDung(noiDung);
        setTrangThai(trangThai);
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