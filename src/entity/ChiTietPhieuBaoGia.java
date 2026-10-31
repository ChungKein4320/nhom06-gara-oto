package entity;

import java.math.BigDecimal;

public class ChiTietPhieuBaoGia {
    private PhieuBaoGia maBaoGia;
    private DichVu maDichVu;
    private PhuTung maPhuTung;
    private Integer soLuongPhuTung;
    private BigDecimal donGiaPhuTung;
    private BigDecimal chiPhiNhanCong;
    private String trangThai;

    public ChiTietPhieuBaoGia() { }

    public PhieuBaoGia getMaBaoGia() {
        return maBaoGia;
    }

    public void setMaBaoGia(PhieuBaoGia value) {
        if (value == null || value.getMaBaoGia() == null
                || !value.getMaBaoGia().matches("PBG[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu báo giá không hợp lệ");
        this.maBaoGia = value;
    }

    public DichVu getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(DichVu value) {
        if (value == null || value.getMaDichVu() == null
                || !value.getMaDichVu().matches("DV[0-9]{3}"))
            throw new IllegalArgumentException("Mã dịch vụ không hợp lệ");
        this.maDichVu = value;
    }

    public PhuTung getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(PhuTung value) {
        if (value != null && (value.getMaPhuTung() == null
                || !value.getMaPhuTung().matches("PT[0-9]{3}")))
            throw new IllegalArgumentException("Mã phụ tùng không hợp lệ");
        this.maPhuTung = value;
    }

    public Integer getSoLuongPhuTung() {
        return soLuongPhuTung;
    }

    public void setSoLuongPhuTung(Integer value) {
        if (value == null || value <= 0)
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        this.soLuongPhuTung = value;
    }

    public BigDecimal getDonGiaPhuTung() {
        return donGiaPhuTung;
    }

    public void setDonGiaPhuTung(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Đơn giá không hợp lệ");
        this.donGiaPhuTung = value;
    }

    public BigDecimal getChiPhiNhanCong() {
        return chiPhiNhanCong;
    }

    public void setChiPhiNhanCong(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Chi phí nhân công không hợp lệ");
        this.chiPhiNhanCong = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        if (value == null || !java.util.Set.of("Chờ xác nhận", "Chờ sửa chữa", "Thiếu phụ tùng",
                "Thiếu phụ tùng + Chờ xác nhận").contains(value))
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        this.trangThai = value;
    }


    public ChiTietPhieuBaoGia(
            PhieuBaoGia maBaoGia,
            DichVu maDichVu,
            PhuTung maPhuTung,
            Integer soLuongPhuTung,
            BigDecimal donGiaPhuTung,
            BigDecimal chiPhiNhanCong) {

        this.maBaoGia = maBaoGia;
        this.maDichVu = maDichVu;
        this.maPhuTung = maPhuTung;
        this.soLuongPhuTung = soLuongPhuTung;
        this.donGiaPhuTung = donGiaPhuTung;
        this.chiPhiNhanCong = chiPhiNhanCong;

        this.trangThai = "Chờ xác nhận";
    }

    @Override
    public String toString() {
        return "ChiTietPhieuBaoGia{" +
                "maBaoGia=" + maBaoGia +
                ", maDichVu=" + maDichVu +
                ", maPhuTung=" + maPhuTung +
                ", soLuongPhuTung=" + soLuongPhuTung +
                ", donGiaPhuTung=" + donGiaPhuTung +
                ", chiPhiNhanCong=" + chiPhiNhanCong +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }

}
