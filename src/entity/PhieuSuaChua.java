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
        if (value == null || !value.matches("PSC[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu sửa chữa không hợp lệ");
        this.maPSC = value;
    }

    public PhieuBaoGia getMaBaoGia() {
        return maBaoGia;
    }

    public void setMaBaoGia(PhieuBaoGia value) {
        if (value == null || value.getMaBaoGia() == null || !value.getMaBaoGia().matches("PBG[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu báo giá không hợp lệ");
        this.maBaoGia = value;
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
            throw new IllegalArgumentException("Mã VIN không hợp lệ");
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

    public Timestamp getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(Timestamp value) {
        if (value == null || value.after(new Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày lập không hợp lệ");
        this.ngayLap = value;
    }

    public Timestamp getNgayHoanThanh() {
        return ngayHoanThanh;
    }

    public void setNgayHoanThanh(Timestamp value) {
        if (value != null && (ngayLap == null || value.before(ngayLap)))
            throw new IllegalArgumentException("Ngày hoàn thành không hợp lệ, phải >= ngayLap");
        this.ngayHoanThanh = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        if (value == null || !java.util.Set.of("Chờ sửa chữa", "Đang sửa chữa", "Đã sửa xong",
                "Chờ thanh toán", "Đã thanh toán", "Hoàn tất", "Đã hủy").contains(value))
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        this.trangThai = value;
    }

    public String getLyDoHuy() { return lyDoHuy; }
    public void setLyDoHuy(String value) {
        if (value != null && value.length() > 500)
            throw new IllegalArgumentException("Lý do hủy không được quá 500 ký tự");
        this.lyDoHuy = value;
    }
    public NhanVien getMaNguoiDuyetHuy() { return maNguoiDuyetHuy; }
    public void setMaNguoiDuyetHuy(NhanVien value) {
        if (value != null && (value.getMaNhanVien() == null || !value.getMaNhanVien().matches("NV[0-9]{3}")))
            throw new IllegalArgumentException("Mã người duyệt hủy không hợp lệ");
        this.maNguoiDuyetHuy = value;
    }

    public PhieuSuaChua(String maPSC, PhieuBaoGia maBaoGia,
            KhachHang maKhachHang, PhuongTien VIN,
            NhanVien maNhanVien, Timestamp ngayLap,
            Timestamp ngayHoanThanh, String trangThai) {

        this.maPSC = maPSC;
        this.maBaoGia = maBaoGia;
        this.maKhachHang = maKhachHang;
        this.VIN = VIN;
        this.maNhanVien = maNhanVien;
        this.ngayLap = ngayLap;
        this.ngayHoanThanh = ngayHoanThanh;
        this.trangThai = trangThai;
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
