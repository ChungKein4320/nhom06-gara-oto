package entity;

import java.sql.Timestamp;

public class PhieuBaoGia {
    private String maBaoGia;
    private PhieuKiemTra maPKT;
    private KhachHang maKhachHang;
    private NhanVien maNhanVien;
    private PhuongTien VIN;
    private Timestamp ngayLap;
    private Timestamp ngayHetHan;
    private Timestamp thoiGianXacNhan;
    private String trangThai;

    public PhieuBaoGia() { }

    public String getMaBaoGia() {
        return maBaoGia;
    }

    public void setMaBaoGia(String value) {
        this.maBaoGia = value;
    }

    public PhieuKiemTra getMaPKT() {
        return maPKT;
    }

    public void setMaPKT(PhieuKiemTra value) {
        this.maPKT = value;
    }

    public KhachHang getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(KhachHang value) {
        this.maKhachHang = value;
    }

    public NhanVien getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(NhanVien value) {
        this.maNhanVien = value;
    }

    public PhuongTien getVIN() {
        return VIN;
    }

    public void setVIN(PhuongTien value) {
        this.VIN = value;
    }

    public Timestamp getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(Timestamp value) {
        this.ngayLap = value;
    }

    public Timestamp getNgayHetHan() {
        return ngayHetHan;
    }

    public void setNgayHetHan(Timestamp value) {
        this.ngayHetHan = value;
    }

    public Timestamp getThoiGianXacNhan() {
        return thoiGianXacNhan;
    }

    public void setThoiGianXacNhan(Timestamp value) {
        this.thoiGianXacNhan = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        this.trangThai = value;
    }

    public PhieuBaoGia(String maBaoGia,
                       PhieuKiemTra maPKT,
                       NhanVien maNhanVien,
                       KhachHang maKhachHang,
                       PhuongTien VIN,
                       Timestamp ngayLap,
                       Timestamp ngayHetHan,
                       Timestamp thoiGianXacNhan) {

        this.maBaoGia = getMaBaoGia();

        setMaPKT(maPKT);
        setMaNhanVien(maNhanVien);
        setMaKhachHang(maKhachHang);
        setVIN(VIN);
        setNgayLap(ngayLap);
        setNgayHetHan(ngayHetHan);
        setThoiGianXacNhan(thoiGianXacNhan);

        this.trangThai = "Còn hiệu lực";
    }

    @Override
    public String toString() {
        return "PhieuBaoGia{" +
                "maBaoGia='" + maBaoGia + '\'' +
                ", maPKT=" + maPKT +
                ", maNhanVien=" + maNhanVien +
                ", maKhachHang=" + maKhachHang +
                ", VIN=" + VIN +
                ", ngayLap=" + ngayLap +
                ", ngayHetHan=" + ngayHetHan +
                ", thoiGianXacNhan=" + thoiGianXacNhan +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
