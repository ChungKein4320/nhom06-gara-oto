package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ChiTietPhieuSuaChua {
    private String maPSC;
    private String maDichVu;
    private String maPhuTung;
    private String trangThaiHangMuc;
    private Integer soLuongPhuTung;
    private String ketQuaNghiemThu;
    private Timestamp ngayBatDauBaoHanh;
    private Integer thoiHanBaoHanh;
    private Timestamp ngayHetHanBaoHanh;

    public ChiTietPhieuSuaChua() { }

    public String getMaPSC() {
        return maPSC;
    }

    public void setMaPSC(String value) {
        this.maPSC = value;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String value) {
        this.maDichVu = value;
    }

    public String getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(String value) {
        this.maPhuTung = value;
    }

    public String getTrangThaiHangMuc() {
        return trangThaiHangMuc;
    }

    public void setTrangThaiHangMuc(String value) {
        this.trangThaiHangMuc = value;
    }

    public Integer getSoLuongPhuTung() {
        return soLuongPhuTung;
    }

    public void setSoLuongPhuTung(Integer value) {
        this.soLuongPhuTung = value;
    }

    public String getKetQuaNghiemThu() {
        return ketQuaNghiemThu;
    }

    public void setKetQuaNghiemThu(String value) {
        this.ketQuaNghiemThu = value;
    }

    public Timestamp getNgayBatDauBaoHanh() {
        return ngayBatDauBaoHanh;
    }

    public void setNgayBatDauBaoHanh(Timestamp value) {
        this.ngayBatDauBaoHanh = value;
    }

    public Integer getThoiHanBaoHanh() {
        return thoiHanBaoHanh;
    }

    public void setThoiHanBaoHanh(Integer value) {
        this.thoiHanBaoHanh = value;
    }

    public Timestamp getNgayHetHanBaoHanh() {
        return ngayHetHanBaoHanh;
    }

    public void setNgayHetHanBaoHanh(Timestamp value) {
        this.ngayHetHanBaoHanh = value;
    }

}