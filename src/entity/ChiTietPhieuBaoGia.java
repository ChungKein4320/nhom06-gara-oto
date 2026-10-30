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
        this.maBaoGia = value;
    }

    public DichVu getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(DichVu value) {
        this.maDichVu = value;
    }

    public PhuTung getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(PhuTung value) {
        this.maPhuTung = value;
    }

    public Integer getSoLuongPhuTung() {
        return soLuongPhuTung;
    }

    public void setSoLuongPhuTung(Integer value) {
        this.soLuongPhuTung = value;
    }

    public BigDecimal getDonGiaPhuTung() {
        return donGiaPhuTung;
    }

    public void setDonGiaPhuTung(BigDecimal value) {
        this.donGiaPhuTung = value;
    }

    public BigDecimal getChiPhiNhanCong() {
        return chiPhiNhanCong;
    }

    public void setChiPhiNhanCong(BigDecimal value) {
        this.chiPhiNhanCong = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        this.trangThai = value;
    }

    public ChiTietPhieuBaoGia(PhieuBaoGia maBaoGia,
                              DichVu maDichVu,
                              PhuTung maPhuTung,
                              Integer soLuongPhuTung,
                              BigDecimal donGiaPhuTung,
                              BigDecimal chiPhiNhanCong) {

        setMaBaoGia(maBaoGia);
        setMaDichVu(maDichVu);
        setMaPhuTung(maPhuTung);
        setSoLuongPhuTung(soLuongPhuTung);
        setDonGiaPhuTung(donGiaPhuTung);
        setChiPhiNhanCong(chiPhiNhanCong);

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