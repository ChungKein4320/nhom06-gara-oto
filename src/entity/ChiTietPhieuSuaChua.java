package entity;

import java.sql.Timestamp;

public class ChiTietPhieuSuaChua {
    private PhieuSuaChua maPSC;
    private DichVu maDichVu;
    private PhuTung maPhuTung;
    private String trangThaiHangMuc;
    private Integer soLuongPhuTung;
    private Integer soLuongCapPhat;
    private Integer soLuongSuDung;
    private Integer soLuongHoanTra;
    private Timestamp ngayCapPhat;
    private NhanVien maNhanVienCapPhat;
    private Timestamp ngayHoanTra;
    private NhanVien maNhanVienXacNhanHoanTra;
    private String ketQuaNghiemThu;
    private Timestamp ngayBatDauBaoHanh;
    private Integer thoiHanBaoHanh;
    private Timestamp ngayHetHanBaoHanh;

    public ChiTietPhieuSuaChua() { }

    public PhieuSuaChua getMaPSC() {
        return maPSC;
    }

    public void setMaPSC(PhieuSuaChua value) {
        this.maPSC = value;
    }

    public DichVu getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(DichVu  value) {
        this.maDichVu = value;
    }

    public PhuTung getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(PhuTung value) {
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
    public NhanVien getMaNhanVienCapPhat() { return maNhanVienCapPhat; }
    public void setMaNhanVienCapPhat(NhanVien value) { this.maNhanVienCapPhat = value; }
    public Timestamp getNgayHoanTra() { return ngayHoanTra; }
    public void setNgayHoanTra(Timestamp value) { this.ngayHoanTra = value; }
    public NhanVien getMaNhanVienXacNhanHoanTra() { return maNhanVienXacNhanHoanTra; }
    public void setMaNhanVienXacNhanHoanTra(NhanVien value) { this.maNhanVienXacNhanHoanTra = value; }

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

     public ChiTietPhieuSuaChua(PhieuSuaChua maPSC,
            DichVu maDichVu, PhuTung maPhuTung,
            String trangThaiHangMuc, int soLuongPhuTung,
            String ketQuaNghiemThu, Timestamp ngayBatDauBaoHanh,
            int thoiHanBaoHanh, Timestamp ngayHetHanBaoHanh) {

        this();
        setMaPSC(maPSC);
        setMaDichVu(maDichVu);
        setMaPhuTung(maPhuTung);
        setTrangThaiHangMuc(trangThaiHangMuc);
        setSoLuongPhuTung(soLuongPhuTung);
        setKetQuaNghiemThu(ketQuaNghiemThu);
        setThoiHanBaoHanh(thoiHanBaoHanh);
        setNgayBatDauBaoHanh(ngayBatDauBaoHanh);
        setNgayHetHanBaoHanh(ngayHetHanBaoHanh);
    }

    @Override
    public String toString() {
        return "ChiTietPhieuSuaChua{"
                + "maPSC=" + maPSC
                + ", maDichVu=" + maDichVu
                + ", maPhuTung=" + maPhuTung
                + ", trangThaiHangMuc='" + trangThaiHangMuc + '\''
                + ", soLuongPhuTung=" + soLuongPhuTung
                + ", soLuongCapPhat=" + soLuongCapPhat
                + ", soLuongSuDung=" + soLuongSuDung
                + ", soLuongHoanTra=" + soLuongHoanTra
                + ", ngayCapPhat=" + ngayCapPhat
                + ", maNhanVienCapPhat='" + maNhanVienCapPhat + '\''
                + ", ngayHoanTra=" + ngayHoanTra
                + ", maNhanVienXacNhanHoanTra='"
                + maNhanVienXacNhanHoanTra + '\''
                + ", ketQuaNghiemThu='" + ketQuaNghiemThu + '\''
                + ", ngayBatDauBaoHanh=" + ngayBatDauBaoHanh
                + ", thoiHanBaoHanh=" + thoiHanBaoHanh
                + ", ngayHetHanBaoHanh=" + ngayHetHanBaoHanh
                + '}';
    }
}
