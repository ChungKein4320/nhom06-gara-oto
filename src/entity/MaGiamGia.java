package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

// ===== ENTITY: ĐỐI TƯỢNG MÃ GIẢM GIÁ =====

public class MaGiamGia {
    // ===== THUỘC TÍNH =====
    private String maGiamGia;
    private KhachHang maKhachHang;
    private BigDecimal mucGiam;
    private Timestamp ngayTao;
    private Timestamp ngayHetHan;
    private String trangThai;
    private Integer mocDiem;
    private Timestamp ngayBatDauChuKy;

    // ===== KHỞI TẠO ĐỐI TƯỢNG =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: MaGiamGia() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public MaGiamGia() {
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: MaGiamGia(String, KhachHang, BigDecimal, Timestamp, Timestamp, String, Integer, Timestamp) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public MaGiamGia(String maGiamGia, KhachHang maKhachHang, BigDecimal mucGiam, Timestamp ngayTao,
            Timestamp ngayHetHan, String trangThai, Integer mocDiem, Timestamp ngayBatDauChuKy) {
        super();
        this.maGiamGia = maGiamGia;
        this.maKhachHang = maKhachHang;
        this.mucGiam = mucGiam;
        this.ngayTao = ngayTao;
        this.ngayHetHan = ngayHetHan;
        this.trangThai = trangThai;
        this.mocDiem = mocDiem;
        this.ngayBatDauChuKy = ngayBatDauChuKy;
    }

    // ===== GETTER VÀ SETTER =====

    // ===== PHƯƠNG THỨC: getMaGiamGia() — Lấy mã giảm giá. =====
    public String getMaGiamGia() {
        return maGiamGia;
    }

    // ===== PHƯƠNG THỨC: setMaGiamGia(String) — Gán mã giảm giá. =====
    public void setMaGiamGia(String value) {
        if (value == null || !value.matches("MGG[0-9]{3}"))
            throw new IllegalArgumentException("Mã giảm giá không hợp lệ");
        this.maGiamGia = value;
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

    // ===== PHƯƠNG THỨC: getMucGiam() — Lấy muc giảm. =====
    public BigDecimal getMucGiam() {
        return mucGiam;
    }

    // ===== PHƯƠNG THỨC: setMucGiam(BigDecimal) — Gán muc giảm. =====
    public void setMucGiam(BigDecimal value) {
        if (value == null || value.compareTo(java.math.BigDecimal.ZERO) < 0
                || value.compareTo(new java.math.BigDecimal("100")) > 0)
            throw new IllegalArgumentException("Mức giảm phải từ 0 đến 100 phần trăm");
        this.mucGiam = value;
    }

    // ===== PHƯƠNG THỨC: getNgayTao() — Lấy ngày tao. =====
    public Timestamp getNgayTao() {
        return ngayTao;
    }

    // ===== PHƯƠNG THỨC: setNgayTao(Timestamp) — Gán ngày tao. =====
    public void setNgayTao(Timestamp value) {
        if (value == null || value.after(new java.sql.Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày tạo không được lớn hơn hiện tại");
        this.ngayTao = value;
    }

    // ===== PHƯƠNG THỨC: getNgayHetHan() — Lấy ngày het han. =====
    public Timestamp getNgayHetHan() {
        return ngayHetHan;
    }

    // ===== PHƯƠNG THỨC: setNgayHetHan(Timestamp) — Gán ngày het han. =====
    public void setNgayHetHan(Timestamp value) {
        if (value == null)
            throw new IllegalArgumentException("Ngày hết hạn không được rỗng");
        if (ngayTao != null && !value.after(ngayTao))
            throw new IllegalArgumentException("Ngày hết hạn phải sau ngày tạo");
        this.ngayHetHan = value;
    }

    // ===== PHƯƠNG THỨC: getTrangThai() — Lấy trạng thái. =====
    public String getTrangThai() {
        return trangThai;
    }

    // ===== PHƯƠNG THỨC: setTrangThai(String) — Gán trạng thái. =====
    public void setTrangThai(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Trạng thái không được rỗng");
        this.trangThai = value.trim();
    }

    // ===== PHƯƠNG THỨC: getMocDiem() — Lấy moc điểm. =====
    public Integer getMocDiem() {
        return mocDiem;
    }

    // ===== PHƯƠNG THỨC: setMocDiem(Integer) — Gán moc điểm. =====
    public void setMocDiem(Integer value) {
        if (value != null && value < 0)
            throw new IllegalArgumentException("Mốc điểm không hợp lệ");
        this.mocDiem = value;
    }

    // ===== PHƯƠNG THỨC: getNgayBatDauChuKy() — Lấy ngày bắt dau chu ky. =====
    public Timestamp getNgayBatDauChuKy() {
        return ngayBatDauChuKy;
    }

    // ===== PHƯƠNG THỨC: setNgayBatDauChuKy(Timestamp) — Gán ngày bắt dau chu ky. =====
    public void setNgayBatDauChuKy(Timestamp value) {
        if (value != null && value.after(new Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày bắt đầu chu kỳ không hợp lệ");
        this.ngayBatDauChuKy = value;
    }

    // ===== PHƯƠNG THỨC: kiemTraHieuLuc() — Kiểm tra hieu luc. =====
    public boolean kiemTraHieuLuc() {
        if (ngayHetHan == null) {
            return false;
        }

        Timestamp hienTai = new Timestamp(System.currentTimeMillis());
        return "Đang hiệu lực".equals(trangThai) && ngayTao != null && !ngayTao.after(hienTai)
                && !ngayHetHan.before(hienTai);
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "MaGiamGia [maGiamGia=" + maGiamGia + ", maKhachHang=" + maKhachHang + ", mucGiam=" + mucGiam
                + ", ngayTao=" + ngayTao + ", ngayHetHan=" + ngayHetHan + ", trangThai=" + trangThai
                + ", mocDiem=" + mocDiem + ", ngayBatDauChuKy=" + ngayBatDauChuKy + "]";
    }

}
