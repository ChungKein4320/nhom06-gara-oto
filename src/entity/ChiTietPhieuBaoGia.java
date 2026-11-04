package entity;

import java.math.BigDecimal;

// ===== ENTITY: ChiTietPhieuBaoGia =====
public class ChiTietPhieuBaoGia {
    private PhieuBaoGia maBaoGia;
    private DichVu maDichVu;
    private PhuTung maPhuTung;
    private Integer soLuongPhuTung;
    private BigDecimal donGiaPhuTung;
    private BigDecimal chiPhiNhanCong;
    private String trangThai;

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ChiTietPhieuBaoGia() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public ChiTietPhieuBaoGia() {
    }

    // ===== PHƯƠNG THỨC: getMaBaoGia() — Lấy mã báo giá. =====
    public PhieuBaoGia getMaBaoGia() {
        return maBaoGia;
    }

    // ===== PHƯƠNG THỨC: setMaBaoGia(PhieuBaoGia) — Gán mã báo giá. =====
    public void setMaBaoGia(PhieuBaoGia value) {
        if (value == null || value.getMaBaoGia() == null || !value.getMaBaoGia().matches("PBG[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu báo giá không hợp lệ");
        this.maBaoGia = value;
    }

    // ===== PHƯƠNG THỨC: getMaDichVu() — Lấy mã dịch vụ. =====
    public DichVu getMaDichVu() {
        return maDichVu;
    }

    // ===== PHƯƠNG THỨC: setMaDichVu(DichVu) — Gán mã dịch vụ. =====
    public void setMaDichVu(DichVu value) {
        if (value == null || value.getMaDichVu() == null || !value.getMaDichVu().matches("DV[0-9]{3}"))
            throw new IllegalArgumentException("Mã dịch vụ không hợp lệ");
        this.maDichVu = value;
    }

    // ===== PHƯƠNG THỨC: getMaPhuTung() — Lấy mã phụ tùng. =====
    public PhuTung getMaPhuTung() {
        return maPhuTung;
    }

    // ===== PHƯƠNG THỨC: setMaPhuTung(PhuTung) — Gán mã phụ tùng. =====
    public void setMaPhuTung(PhuTung value) {
        if (value != null && (value.getMaPhuTung() == null || !value.getMaPhuTung().matches("PT[0-9]{3}")))
            throw new IllegalArgumentException("Mã phụ tùng không hợp lệ");
        this.maPhuTung = value;
    }

    // ===== PHƯƠNG THỨC: getSoLuongPhuTung() — Lấy số lượng phụ tùng. =====
    public Integer getSoLuongPhuTung() {
        return soLuongPhuTung;
    }

    // ===== PHƯƠNG THỨC: setSoLuongPhuTung(Integer) — Gán số lượng phụ tùng. =====
    public void setSoLuongPhuTung(Integer value) {
        if (value == null || value <= 0)
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        this.soLuongPhuTung = value;
    }

    // ===== PHƯƠNG THỨC: getDonGiaPhuTung() — Lấy đơn giá phụ tùng. =====
    public BigDecimal getDonGiaPhuTung() {
        return donGiaPhuTung;
    }

    // ===== PHƯƠNG THỨC: setDonGiaPhuTung(BigDecimal) — Gán đơn giá phụ tùng. =====
    public void setDonGiaPhuTung(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Đơn giá không hợp lệ");
        this.donGiaPhuTung = value;
    }

    // ===== PHƯƠNG THỨC: getChiPhiNhanCong() — Lấy chi phi nhân cong. =====
    public BigDecimal getChiPhiNhanCong() {
        return chiPhiNhanCong;
    }

    // ===== PHƯƠNG THỨC: setChiPhiNhanCong(BigDecimal) — Gán chi phi nhân cong. =====
    public void setChiPhiNhanCong(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Chi phí nhân công không hợp lệ");
        this.chiPhiNhanCong = value;
    }

    // ===== PHƯƠNG THỨC: getTrangThai() — Lấy trạng thái. =====
    public String getTrangThai() {
        return trangThai;
    }

    // ===== PHƯƠNG THỨC: setTrangThai(String) — Gán trạng thái. =====
    public void setTrangThai(String value) {
        if (value == null || !java.util.Set
                .of("Chờ xác nhận", "Chờ sửa chữa", "Thiếu phụ tùng", "Thiếu phụ tùng + Chờ xác nhận")
                .contains(value))
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        this.trangThai = value;
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ChiTietPhieuBaoGia(PhieuBaoGia, DichVu, PhuTung, Integer, BigDecimal, BigDecimal) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public ChiTietPhieuBaoGia(PhieuBaoGia maBaoGia, DichVu maDichVu, PhuTung maPhuTung,
            Integer soLuongPhuTung, BigDecimal donGiaPhuTung, BigDecimal chiPhiNhanCong) {

        this.maBaoGia = maBaoGia;
        this.maDichVu = maDichVu;
        this.maPhuTung = maPhuTung;
        this.soLuongPhuTung = soLuongPhuTung;
        this.donGiaPhuTung = donGiaPhuTung;
        this.chiPhiNhanCong = chiPhiNhanCong;

        this.trangThai = "Chờ xác nhận";
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "ChiTietPhieuBaoGia{" + "maBaoGia=" + maBaoGia + ", maDichVu=" + maDichVu + ", maPhuTung="
                + maPhuTung + ", soLuongPhuTung=" + soLuongPhuTung + ", donGiaPhuTung=" + donGiaPhuTung
                + ", chiPhiNhanCong=" + chiPhiNhanCong + ", trangThai='" + trangThai + '\'' + '}';
    }

}
