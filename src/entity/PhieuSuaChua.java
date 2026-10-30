package entity;

import java.sql.Timestamp;

public class PhieuSuaChua {
    private String maPSC;
    private PhieuBaoGia maBaoGia;
    private KhachHang maKhachHang;
    private PhuongTien VIN;
    private NhanVien maNhanVien;
    private Timestamp ngayLap;
    private Timestamp ngayHoanThanh;
    private String trangThai;
    private String lyDoHuy;
    private NhanVien maNguoiDuyetHuy;

    public PhieuSuaChua() { }

    public String getMaPSC() {
        return maPSC;
    }

    public void setMaPSC(String value) {
        this.maPSC = value;
    }

    public PhieuBaoGia getMaBaoGia() {
        return maBaoGia;
    }

    public void setMaBaoGia(PhieuBaoGia value) {
        this.maBaoGia = value;
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

    public Timestamp getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(Timestamp value) {
        this.ngayLap = value;
    }

    public Timestamp getNgayHoanThanh() {
        return ngayHoanThanh;
    }

    public void setNgayHoanThanh(Timestamp value) {
        this.ngayHoanThanh = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        this.trangThai = value;
    }

    public String getLyDoHuy() { return lyDoHuy; }
    public void setLyDoHuy(String value) { this.lyDoHuy = value; }
    public NhanVien getMaNguoiDuyetHuy() { return maNguoiDuyetHuy; }
    public void setMaNguoiDuyetHuy(NhanVien value) { this.maNguoiDuyetHuy = value; }

    PhieuSuaChua(String maPSC, PhieuBaoGia maBaoGia,
            KhachHang maKhachHang, PhuongTien VIN,
            NhanVien maNhanVien, Timestamp ngayLap,
            Timestamp ngayHoanThanh, String trangThai) {

        this();
        setMaBaoGia(maBaoGia);
        setMaKhachHang(maKhachHang);
        setVIN(VIN);
        setMaNhanVien(maNhanVien);
        setNgayLap(ngayLap);
        setNgayHoanThanh(ngayHoanThanh);
        setTrangThai(trangThai);
    }

    @Override
    public String toString() {
        return "PhieuSuaChua{"
                + "maPSC='" + maPSC + '\''
                + ", maBaoGia=" + maBaoGia
                + ", maKhachHang=" + maKhachHang
                + ", VIN=" + VIN
                + ", maNhanVien=" + maNhanVien
                + ", maNguoiDuyetHuy='" + maNguoiDuyetHuy + '\''
                + ", ngayLap=" + ngayLap
                + ", ngayHoanThanh=" + ngayHoanThanh
                + ", trangThai='" + trangThai + '\''
                + ", lyDoHuy='" + lyDoHuy + '\''
                + '}';
    }
}
