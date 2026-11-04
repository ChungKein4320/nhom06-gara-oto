package entity;

import java.sql.Timestamp;

// ===== ENTITY: PhieuBaoGia =====
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

    // ===== KHỞI TẠO ĐỐI TƯỢNG: PhieuBaoGia() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public PhieuBaoGia() {
    }

    // ===== PHƯƠNG THỨC: getMaBaoGia() — Lấy mã báo giá. =====
    public String getMaBaoGia() {
        return maBaoGia;
    }

    // ===== PHƯƠNG THỨC: setMaBaoGia(String) — Gán mã báo giá. =====
    public void setMaBaoGia(String value) {
        if (value == null || !value.matches("PBG[0-9]{3}"))
            throw new IllegalArgumentException("Mã báo giá không hợp lệ");
        this.maBaoGia = value;
    }

    // ===== PHƯƠNG THỨC: getMaPKT() — Lấy mã pkt. =====
    public PhieuKiemTra getMaPKT() {
        return maPKT;
    }

    // ===== PHƯƠNG THỨC: setMaPKT(PhieuKiemTra) — Gán mã pkt. =====
    public void setMaPKT(PhieuKiemTra value) {
        if (value == null || value.getMaPKT() == null || !value.getMaPKT().matches("PKT[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu kiểm tra không hợp lệ");
        this.maPKT = value;
    }

    // ===== PHƯƠNG THỨC: getMaKhachHang() — Lấy mã khách hàng. =====
    public KhachHang getMaKhachHang() {
        return maKhachHang;
    }

    // ===== PHƯƠNG THỨC: setMaKhachHang(KhachHang) — Gán mã khách hàng. =====
    public void setMaKhachHang(KhachHang value) {
        if (value == null || value.getMaKhachHang() == null || !value.getMaKhachHang().matches("KH[0-9]{3}"))
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ");
        this.maKhachHang = value;
    }

    // ===== PHƯƠNG THỨC: getMaNhanVien() — Lấy mã nhân viên. =====
    public NhanVien getMaNhanVien() {
        return maNhanVien;
    }

    // ===== PHƯƠNG THỨC: setMaNhanVien(NhanVien) — Gán mã nhân viên. =====
    public void setMaNhanVien(NhanVien value) {
        if (value == null || value.getMaNhanVien() == null || !value.getMaNhanVien().matches("NV[0-9]{3}"))
            throw new IllegalArgumentException("Mã nhân viên không hợp lệ");
        this.maNhanVien = value;
    }

    // ===== PHƯƠNG THỨC: getVIN() — Lấy vin. =====
    public PhuongTien getVIN() {
        return VIN;
    }

    // ===== PHƯƠNG THỨC: setVIN(PhuongTien) — Gán vin. =====
    public void setVIN(PhuongTien value) {
        if (value == null || value.getVIN() == null || !value.getVIN().matches("[A-HJ-NPR-Z0-9]{17}"))
            throw new IllegalArgumentException("Mã phương tiện không hợp lệ");
        this.VIN = value;
    }

    // ===== PHƯƠNG THỨC: getNgayLap() — Lấy ngày lap. =====
    public Timestamp getNgayLap() {
        return ngayLap;
    }

    // ===== PHƯƠNG THỨC: setNgayLap(Timestamp) — Gán ngày lap. =====
    public void setNgayLap(Timestamp value) {
        if (value == null || value.after(new Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày lập không hợp lệ");
        this.ngayLap = value;
    }

    // ===== PHƯƠNG THỨC: getNgayHetHan() — Lấy ngày het han. =====
    public Timestamp getNgayHetHan() {
        return ngayHetHan;
    }

    // ===== PHƯƠNG THỨC: setNgayHetHan(Timestamp) — Gán ngày het han. =====
    public void setNgayHetHan(Timestamp value) {
        if (value == null || ngayLap == null || value.before(ngayLap))
            throw new IllegalArgumentException("Ngày hết hạn không hợp lệ, phải >= ngayLap");
        this.ngayHetHan = value;
    }

    // ===== PHƯƠNG THỨC: getThoiGianXacNhan() — Lấy thoi gian xac nhân. =====
    public Timestamp getThoiGianXacNhan() {
        return thoiGianXacNhan;
    }

    // ===== PHƯƠNG THỨC: setThoiGianXacNhan(Timestamp) — Gán thoi gian xac nhân. =====
    public void setThoiGianXacNhan(Timestamp value) {
        if (value != null && (ngayLap == null || value.before(ngayLap)))
            throw new IllegalArgumentException("Thời gian xác nhận không hợp lệ, phải >= ngayLap");
        this.thoiGianXacNhan = value;
    }

    // ===== PHƯƠNG THỨC: getTrangThai() — Lấy trạng thái. =====
    public String getTrangThai() {
        return trangThai;
    }

    // ===== PHƯƠNG THỨC: setTrangThai(String) — Gán trạng thái. =====
    public void setTrangThai(String value) {
        if (value == null || !java.util.Set.of("Còn hiệu lực", "Hết hiệu lực", "Đã xác nhận").contains(value))
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        this.trangThai = value;
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: PhieuBaoGia(String, PhieuKiemTra, NhanVien, KhachHang, PhuongTien, Timestamp, Timestamp, Timestamp) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public PhieuBaoGia(String maBaoGia, PhieuKiemTra maPKT, NhanVien maNhanVien, KhachHang maKhachHang,
            PhuongTien VIN, Timestamp ngayLap, Timestamp ngayHetHan, Timestamp thoiGianXacNhan) {

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

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "PhieuBaoGia{" + "maBaoGia='" + maBaoGia + '\'' + ", maPKT=" + maPKT + ", maNhanVien="
                + maNhanVien + ", maKhachHang=" + maKhachHang + ", VIN=" + VIN + ", ngayLap=" + ngayLap
                + ", ngayHetHan=" + ngayHetHan + ", thoiGianXacNhan=" + thoiGianXacNhan + ", trangThai='"
                + trangThai + '\'' + '}';
    }
}
