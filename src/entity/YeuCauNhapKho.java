package entity;

import java.sql.Timestamp;

public class YeuCauNhapKho {
    private String maYCNK;
    private String maNCC;
    private String maNhanVien;
    private Timestamp ngayTao;
    private String trangThai;
    private String maNhanVienDuyet;
    private Timestamp ngayDuyet;
    private String ghiChu;

    public YeuCauNhapKho() { }

    public String getMaYCNK() {
        return maYCNK;
    }

    public void setMaYCNK(String value) {
        this.maYCNK = value;
    }

    public String getMaNCC() {
        return maNCC;
    }

    public void setMaNCC(String value) {
        this.maNCC = value;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String value) {
        this.maNhanVien = value;
    }

    public Timestamp getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Timestamp value) {
        this.ngayTao = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        this.trangThai = value;
    }

    public String getMaNhanVienDuyet() {
        return maNhanVienDuyet;
    }

    public void setMaNhanVienDuyet(String value) {
        this.maNhanVienDuyet = value;
    }

    public Timestamp getNgayDuyet() {
        return ngayDuyet;
    }

    public void setNgayDuyet(Timestamp value) {
        this.ngayDuyet = value;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String value) {
        this.ghiChu = value;
    }
}
