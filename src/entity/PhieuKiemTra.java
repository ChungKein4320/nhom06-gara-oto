package entity;

import java.sql.Timestamp;

public class PhieuKiemTra {
    private String maPKT;
    private PhieuTiepNhan maPTN;
    private NhanVien maNhanVien;
    private PhuongTien VIN;
    private Timestamp ngayKiemTra;
    private KhachHang maKhachHang;

    public PhieuKiemTra() { }

    public String getMaPKT() {
        return maPKT;
    }

    public void setMaPKT(String value) {
        this.maPKT = value;
    }

    public PhieuTiepNhan getMaPTN() {
        return maPTN;
    }

    public void setMaPTN(PhieuTiepNhan value) {
        this.maPTN = value;
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

    public Timestamp getNgayKiemTra() {
        return ngayKiemTra;
    }

    public void setNgayKiemTra(Timestamp value) {
        this.ngayKiemTra = value;
    }

    public KhachHang getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(KhachHang value) {
        this.maKhachHang = value;
    }

    public PhieuKiemTra(String maPKT,
                        PhieuTiepNhan maPTN,
                        NhanVien maNhanVien,
                        KhachHang maKhachHang,
                        PhuongTien VIN,
                        Timestamp ngayKiemTra) {

        this.maPKT = getMaPKT();
        setMaPTN(maPTN);
        setMaNhanVien(maNhanVien);
        setMaKhachHang(maKhachHang);
        setVIN(VIN);
        setNgayKiemTra(ngayKiemTra);
    }

    @Override
    public String toString() {
        return "PhieuKiemTra{" +
                "maPKT='" + maPKT + '\'' +
                ", maPTN=" + maPTN +
                ", maNhanVien=" + maNhanVien +
                ", maKhachHang=" + maKhachHang +
                ", VIN=" + VIN +
                ", ngayKiemTra=" + ngayKiemTra +
                '}';
    }
}