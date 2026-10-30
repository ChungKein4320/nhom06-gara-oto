package entity;

import java.sql.Timestamp;

public class ChiTietPhieuSuaChua {
    private String maPSC;
    private String maDichVu;
    private String maPhuTung;
    private String trangThaiHangMuc;
    private Integer soLuongPhuTung;
    private Integer soLuongCapPhat;
    private Integer soLuongSuDung;
    private Integer soLuongHoanTra;
    private Timestamp ngayCapPhat;
    private String maNhanVienCapPhat;
    private Timestamp ngayHoanTra;
    private String maNhanVienXacNhanHoanTra;
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

    public Integer getSoLuongCapPhat() { return soLuongCapPhat; }
    public void setSoLuongCapPhat(Integer value) { this.soLuongCapPhat = value; }
    public Integer getSoLuongSuDung() { return soLuongSuDung; }
    public void setSoLuongSuDung(Integer value) { this.soLuongSuDung = value; }
    public Integer getSoLuongHoanTra() { return soLuongHoanTra; }
    public void setSoLuongHoanTra(Integer value) { this.soLuongHoanTra = value; }
    public Timestamp getNgayCapPhat() { return ngayCapPhat; }
    public void setNgayCapPhat(Timestamp value) { this.ngayCapPhat = value; }
    public String getMaNhanVienCapPhat() { return maNhanVienCapPhat; }
    public void setMaNhanVienCapPhat(String value) { this.maNhanVienCapPhat = value; }
    public Timestamp getNgayHoanTra() { return ngayHoanTra; }
    public void setNgayHoanTra(Timestamp value) { this.ngayHoanTra = value; }
    public String getMaNhanVienXacNhanHoanTra() { return maNhanVienXacNhanHoanTra; }
    public void setMaNhanVienXacNhanHoanTra(String value) { this.maNhanVienXacNhanHoanTra = value; }

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
