package entity;

import java.sql.Timestamp;

public class PhieuNhapKho {
    private String maPNK;
    private YeuCauNhapKho maYCNK;
    private NhaCungCap maNCC;
    private NhanVien maNhanVien;
    private Timestamp ngayNhap;
    private String trangThai;

    public PhieuNhapKho() { }

    public PhieuNhapKho(String maPNK, YeuCauNhapKho maYCNK, NhaCungCap maNCC,
            NhanVien maNhanVien, Timestamp ngayNhap, String trangThai) {
        this.maPNK = maPNK;
        this.maYCNK = maYCNK;
        this.maNCC = maNCC;
        this.maNhanVien = maNhanVien;
        this.ngayNhap = ngayNhap;
        this.trangThai = trangThai;
    }

    public String getMaPNK() {
        return maPNK;
    }

    public void setMaPNK(String value) {
        if (value == null || !value.matches("PNK[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu nhập kho không hợp lệ");
        this.maPNK = value;
    }

    public YeuCauNhapKho getMaYCNK() {
        return maYCNK;
    }

    public void setMaYCNK(YeuCauNhapKho value) {
        if (value != null && (value.getMaYCNK() == null || !value.getMaYCNK().matches("YCN[0-9]{3}")))
            throw new IllegalArgumentException("Mã yêu cầu nhập kho không hợp lệ");
        this.maYCNK = value;
    }

    public NhaCungCap getMaNCC() {
        return maNCC;
    }

    public void setMaNCC(NhaCungCap value) {
        if (value == null || value.getMaNCC() == null || !value.getMaNCC().matches("NCC[0-9]{3}"))
            throw new IllegalArgumentException("Mã nhà cung cấp không hợp lệ");
        this.maNCC = value;
    }

    public NhanVien getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(NhanVien value) {
        if (value == null || value.getMaNhanVien() == null || !value.getMaNhanVien().matches("NV[0-9]{3}"))
            throw new IllegalArgumentException("Mã nhân viên không hợp lệ");
        this.maNhanVien = value;
    }

    public Timestamp getNgayNhap() {
        return ngayNhap;
    }

    public void setNgayNhap(Timestamp value) {
        if (value == null || value.after(new Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày nhập không hợp lệ");
        this.ngayNhap = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        java.util.Set<String> trangThaiHopLe = maYCNK == null
                ? java.util.Set.of("Chờ duyệt", "Đã duyệt", "Không duyệt")
                : java.util.Set.of("Chờ xác nhận nhập kho", "Đã nhập kho", "Đã hủy");
        if (value == null || !trangThaiHopLe.contains(value))
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        this.trangThai = value;
    }

    @Override
    public String toString() {
        return "PhieuNhapKho [maPNK=" + maPNK + ", maYCNK=" + maYCNK + ", maNCC=" + maNCC
                + ", maNhanVien=" + maNhanVien + ", ngayNhap=" + ngayNhap + ", trangThai=" + trangThai + "]";
    }
}
