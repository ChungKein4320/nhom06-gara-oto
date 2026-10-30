package entity;

import java.sql.Timestamp;

public class PhieuTiepNhan {
    private String maPTN;
    private KhachHang maKhachHang;
    private PhuongTien VIN;
    private NhanVien maNhanVien;
    private Timestamp ngayTiepNhan;
    private String soODO;

    public PhieuTiepNhan() { }

    public String getMaPTN() {
        return maPTN;
    }

    public void setMaPTN(String value) {
        this.maPTN = value;
    }

    public KhachHang getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(KhachHang value) {
        this.maKhachHang = value;
    }

    public PhuongTien getVIN() {
        return VIN;
    }

    public void setVIN(PhuongTien value) {
        this.VIN = value;
    }

    public NhanVien getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(NhanVien value) {
        this.maNhanVien = value;
    }

    public Timestamp getNgayTiepNhan() {
        return ngayTiepNhan;
    }

    public void setNgayTiepNhan(Timestamp value) {
        this.ngayTiepNhan = value;
    }

    public String getSoODO() {
        return soODO;
    }

    public void setSoODO(String value) {
        this.soODO = value;
    }

     public PhieuTiepNhan(String maPTN, KhachHang maKhachHang,
                         NhanVien maNhanVien, PhuongTien VIN,
                         Timestamp ngayTiepNhan, String soODO) {

        this.maPTN = getMaPTN();
        setMaKhachHang(maKhachHang);
        setMaNhanVien(maNhanVien);
        setVIN(VIN);
        setNgayTiepNhan(ngayTiepNhan);
        setSoODO(soODO);
    }

     @Override
    public String toString() {
        return "PhieuTiepNhan{" +
                "maPTN='" + maPTN + '\'' +
                ", maKhachHang=" + maKhachHang +
                ", maNhanVien=" + maNhanVien +
                ", VIN=" + VIN +
                ", ngayTiepNhan=" + ngayTiepNhan +
                ", soODO='" + soODO + '\'' +
                '}';
    }
}
