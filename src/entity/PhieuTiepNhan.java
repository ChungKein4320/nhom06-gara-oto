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
        if (value == null || !value.matches("PTN[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu tiếp nhận không hợp lệ");
        this.maPTN = value;
    }

    public KhachHang getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(KhachHang value) {
        if (value == null || value.getMaKhachHang() == null || !value.getMaKhachHang().matches("KH[0-9]{3}"))
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ");
        this.maKhachHang = value;
    }

    public PhuongTien getVIN() {
        return VIN;
    }

    public void setVIN(PhuongTien value) {
        if (value == null || value.getVIN() == null || !value.getVIN().matches("[A-HJ-NPR-Z0-9]{17}"))
            throw new IllegalArgumentException("Số VIN không hợp lệ");
        this.VIN = value;
    }

    public NhanVien getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(NhanVien value) {
        if (value == null || value.getMaNhanVien() == null || !value.getMaNhanVien().matches("NV[0-9]{3}"))
            throw new IllegalArgumentException("Mã nhân viên không hợp lệ");
        this.maNhanVien = value;
    }

    public Timestamp getNgayTiepNhan() {
        return ngayTiepNhan;
    }

    public void setNgayTiepNhan(Timestamp value) {
        if (value == null) throw new IllegalArgumentException("Ngày tiếp nhận không hợp lệ");
        this.ngayTiepNhan = value;
    }

    public String getSoODO() {
        return soODO;
    }

    public void setSoODO(String value) {
        if (value == null || !value.matches("[0-9]{6}"))
            throw new IllegalArgumentException("Số ODO không hợp lệ");
        this.soODO = value;
    }

     public PhieuTiepNhan(String maPTN, KhachHang maKhachHang,
                         NhanVien maNhanVien, PhuongTien VIN,
                         Timestamp ngayTiepNhan, String soODO) {

        this.maPTN = maPTN;
        this.maKhachHang = maKhachHang;
        this.maNhanVien = maNhanVien;
        this.VIN = VIN;
        this.ngayTiepNhan = ngayTiepNhan;
        this.soODO = soODO;
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
