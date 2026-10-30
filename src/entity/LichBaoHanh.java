package entity;

import java.sql.Timestamp;

public class LichBaoHanh {
    private String maLBH;
    private PhieuSuaChua maPSC;
    private DichVu maDichVu;
    private PhuTung maPhuTung;
    private NhanVien maNhanVien;
    private KhachHang maKhachHang;
    private PhuongTien VIN;
    private Timestamp ngayXuLy;
    private String ketQuaXuLy;
    private String ghiChu;

    public LichBaoHanh() { }

    public String getMaLBH() {
        return maLBH;
    }

    public void setMaLBH(String value) {
        this.maLBH = value;
    }

    public PhieuSuaChua getMaPSC() {
        return maPSC;
    }

    public void setMaPSC(PhieuSuaChua value) {
        this.maPSC = value;
    }

    public DichVu getMaDichVu() { return maDichVu; }
    public void setMaDichVu(DichVu value) { this.maDichVu = value; }
    public PhuTung getMaPhuTung() { return maPhuTung; }
    public void setMaPhuTung(PhuTung value) { this.maPhuTung = value; }

    public NhanVien getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(NhanVien value) {
        this.maNhanVien = value;
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

    public Timestamp getNgayXuLy() {
        return ngayXuLy;
    }

    public void setNgayXuLy(Timestamp value) {
        this.ngayXuLy = value;
    }

    public String getKetQuaXuLy() {
        return ketQuaXuLy;
    }

    public void setKetQuaXuLy(String value) {
        this.ketQuaXuLy = value;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String value) {
        this.ghiChu = value;
    }

    public LichBaoHanh(String maLBH, PhieuSuaChua maPSC,
            NhanVien maNhanVien, KhachHang maKhachHang,
            PhuongTien VIN, Timestamp ngayXuLy,
            String ketQuaXuLy, String ghiChu) {

        this();
        setMaPSC(maPSC);
        setMaNhanVien(maNhanVien);
        setMaKhachHang(maKhachHang);
        setVIN(VIN);
        setNgayXuLy(ngayXuLy);
        setKetQuaXuLy(ketQuaXuLy);
        setGhiChu(ghiChu);
    }

    @Override
    public String toString() {
        return "LichBaoHanh{"
                + "maLBH='" + maLBH + '\''
                + ", maPSC=" + maPSC
                + ", maDichVu=" + getMaDichVu()
                + ", maPhuTung=" + getMaPhuTung()
                + ", maNhanVien=" + maNhanVien
                + ", maKhachHang=" + maKhachHang
                + ", VIN=" + VIN
                + ", ngayXuLy=" + ngayXuLy
                + ", ketQuaXuLy='" + ketQuaXuLy + '\''
                + ", ghiChu='" + ghiChu + '\''
                + '}';
    }
}
