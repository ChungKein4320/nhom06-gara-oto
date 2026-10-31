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
        if (value == null || !value.matches("LBH[0-9]{3}"))
            throw new IllegalArgumentException("Mã lịch bảo hành không hợp lệ");
        this.maLBH = value;
    }

    public PhieuSuaChua getMaPSC() {
        return maPSC;
    }

    public void setMaPSC(PhieuSuaChua value) {
        if (value == null || value.getMaPSC() == null || !value.getMaPSC().matches("PSC[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu sửa chữa không hợp lệ");
        this.maPSC = value;
    }

    public DichVu getMaDichVu() { return maDichVu; }
    public void setMaDichVu(DichVu value) {
        if (value != null && (value.getMaDichVu() == null || !value.getMaDichVu().matches("DV[0-9]{3}")))
            throw new IllegalArgumentException("Mã dịch vụ không hợp lệ");
        this.maDichVu = value;
    }
    public PhuTung getMaPhuTung() { return maPhuTung; }
    public void setMaPhuTung(PhuTung value) {
        if (value != null && (value.getMaPhuTung() == null || !value.getMaPhuTung().matches("PT[0-9]{3}")))
            throw new IllegalArgumentException("Mã phụ tùng không hợp lệ");
        this.maPhuTung = value;
    }

    public NhanVien getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(NhanVien value) {
        if (value == null || value.getMaNhanVien() == null || !value.getMaNhanVien().matches("NV[0-9]{3}"))
            throw new IllegalArgumentException("Mã nhân viên không hợp lệ");
        this.maNhanVien = value;
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
            throw new IllegalArgumentException("Mã phương tiện không hợp lệ");
        this.VIN = value;
    }

    public Timestamp getNgayXuLy() {
        return ngayXuLy;
    }

    public void setNgayXuLy(Timestamp value) {
        if (value == null || value.after(new Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày xử lý không hợp lệ");
        this.ngayXuLy = value;
    }

    public String getKetQuaXuLy() {
        return ketQuaXuLy;
    }

    public void setKetQuaXuLy(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Kết quả xử lý không được rỗng");
        this.ketQuaXuLy = value.trim();
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String value) {
        this.ghiChu = value;
    }


    public LichBaoHanh(
            String maLBH,
            PhieuSuaChua maPSC,
            NhanVien maNhanVien,
            KhachHang maKhachHang,
            PhuongTien VIN,
            Timestamp ngayXuLy,
            String ketQuaXuLy,
            String ghiChu) {

        this.maLBH = maLBH;
        this.maPSC = maPSC;
        this.maNhanVien = maNhanVien;
        this.maKhachHang = maKhachHang;
        this.VIN = VIN;
        this.ngayXuLy = ngayXuLy;
        this.ketQuaXuLy = ketQuaXuLy;
        this.ghiChu = ghiChu;
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
