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
        if (value == null || !value.matches("PBG[0-9]{3}"))
            throw new IllegalArgumentException("Mã báo giá không hợp lệ");
        this.maBaoGia = value;
    }

    public PhieuKiemTra getMaPKT() {
        return maPKT;
    }

    public void setMaPKT(PhieuKiemTra value) {
        if (value == null || value.getMaPKT() == null || !value.getMaPKT().matches("PKT[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu kiểm tra không hợp lệ");
        this.maPKT = value;
    }

    public KhachHang getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(KhachHang value) {
        if (value == null || value.getMaKhachHang() == null || !value.getMaKhachHang().matches("KH[0-9]{3}"))
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ");
        this.maKhachHang = value;
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

    public Timestamp getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(Timestamp value) {
        if (value == null || value.after(new Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày lập không hợp lệ");
        this.ngayLap = value;
    }

    public Timestamp getNgayHetHan() {
        return ngayHetHan;
    }

    public void setNgayHetHan(Timestamp value) {
        if (value == null || ngayLap == null || value.before(ngayLap))
            throw new IllegalArgumentException("Ngày hết hạn không hợp lệ, phải >= ngayLap");
        this.ngayHetHan = value;
    }

    public Timestamp getThoiGianXacNhan() {
        return thoiGianXacNhan;
    }

    public void setThoiGianXacNhan(Timestamp value) {
        if (value != null && (ngayLap == null || value.before(ngayLap)))
            throw new IllegalArgumentException("Thời gian xác nhận không hợp lệ, phải >= ngayLap");
        this.thoiGianXacNhan = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        if (value == null || !java.util.Set.of("Còn hiệu lực", "Hết hiệu lực", "Đã xác nhận").contains(value))
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
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

        this.maBaoGia = maBaoGia;
        this.maPKT = maPKT;
        this.maNhanVien = maNhanVien;
        this.maKhachHang = maKhachHang;
        this.VIN = VIN;
        this.ngayLap = ngayLap;
        this.ngayHetHan = ngayHetHan;
        this.thoiGianXacNhan = thoiGianXacNhan;

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
