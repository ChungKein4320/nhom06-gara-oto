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
        if (value == null || !value.matches("PKT[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu kiểm tra không hợp lệ");
        this.maPKT = value;
    }

    public PhieuTiepNhan getMaPTN() {
        return maPTN;
    }

    public void setMaPTN(PhieuTiepNhan value) {
        if (value == null || value.getMaPTN() == null || !value.getMaPTN().matches("PTN[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu tiếp nhận không hợp lệ");
        this.maPTN = value;
    }

    public NhanVien getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(NhanVien value) {
        if (value == null || value.getMaNhanVien() == null || !value.getMaNhanVien().matches("NV[0-9]{3}"))
            throw new IllegalArgumentException("Mã nhân viên không hợp lệ");
        this.maNhanVien = value;
    }

    public PhuongTien getVIN() {
        return VIN;
    }

    public void setVIN(PhuongTien value) {
        if (value == null || value.getVIN() == null || !value.getVIN().matches("[A-HJ-NPR-Z0-9]{17}"))
            throw new IllegalArgumentException("Mã phương tiện không hợp lệ");
        this.VIN = value;
    }

    public Timestamp getNgayKiemTra() {
        return ngayKiemTra;
    }

    public void setNgayKiemTra(Timestamp value) {
        if (value == null || value.after(new Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày kiểm tra không hợp lệ");
        this.ngayKiemTra = value;
    }

    public KhachHang getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(KhachHang value) {
        if (value == null || value.getMaKhachHang() == null || !value.getMaKhachHang().matches("KH[0-9]{3}"))
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ");
        this.maKhachHang = value;
    }

    public PhieuKiemTra(String maPKT,
                        PhieuTiepNhan maPTN,
                        NhanVien maNhanVien,
                        KhachHang maKhachHang,
                        PhuongTien VIN,
                        Timestamp ngayKiemTra) {

        this.maPKT = maPKT;
        this.maPTN = maPTN;
        this.maNhanVien = maNhanVien;
        this.maKhachHang = maKhachHang;
        this.VIN = VIN;
        this.ngayKiemTra = ngayKiemTra;
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
