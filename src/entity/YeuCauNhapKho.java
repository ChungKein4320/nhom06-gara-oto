package entity;

import java.sql.Timestamp;

public class YeuCauNhapKho {
    private String maYCNK;
    private NhaCungCap maNCC;
    private NhanVien maNhanVien;
    private Timestamp ngayTao;
    private String trangThai;
    private String maNhanVienDuyet;
    private Timestamp ngayDuyet;
    private String ghiChu;

    public YeuCauNhapKho() { }


    public YeuCauNhapKho(String maYCNK, NhaCungCap maNCC, NhanVien maNhanVien, Timestamp ngayTao, String trangThai,
            String maNhanVienDuyet, Timestamp ngayDuyet, String ghiChu) {
        this.maYCNK = maYCNK;
        this.maNCC = maNCC;
        this.maNhanVien = maNhanVien;
        this.ngayTao = ngayTao;
        this.trangThai = trangThai;
        this.maNhanVienDuyet = maNhanVienDuyet;
        this.ngayDuyet = ngayDuyet;
        this.ghiChu = ghiChu;
    }



    public String getMaYCNK() {
        return maYCNK;
    }

    public void setMaYCNK(String value) {
        if (value == null || !value.matches("YCN[0-9]{3}"))
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

    public Timestamp getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Timestamp value) {
        if (value == null || value.after(new Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày tạo không hợp lệ");
        this.ngayTao = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        if (value == null || !java.util.Set.of("Chờ duyệt", "Đã duyệt", "Từ chối", "Đã xử lý").contains(value))
            throw new IllegalArgumentException("Trạng thái yêu cầu nhập kho không hợp lệ");
        this.trangThai = value;
    }

    public String getMaNhanVienDuyet() {
        return maNhanVienDuyet;
    }

    public void setMaNhanVienDuyet(String value) {
        if (value != null && !value.matches("NV[0-9]{3}"))
            throw new IllegalArgumentException("Mã nhân viên duyệt không hợp lệ");
        this.maNhanVienDuyet = value;
    }

    public Timestamp getNgayDuyet() {
        return ngayDuyet;
    }

    public void setNgayDuyet(Timestamp value) {
        if (value != null && (ngayTao == null || value.before(ngayTao)))
            throw new IllegalArgumentException("Ngày duyệt phải bằng hoặc sau ngày tạo yêu cầu");
        this.ngayDuyet = value;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String value) {
        if (value != null && value.length() > 255)
            throw new IllegalArgumentException("Ghi chú không được quá 255 ký tự");
        this.ghiChu = value;
    }



    @Override
    public String toString() {
        return "YeuCauNhapKho [maYCNK=" + maYCNK + ", maNCC=" + maNCC + ", maNhanVien=" + maNhanVien + ", ngayTao="
                + ngayTao + ", trangThai=" + trangThai + ", maNhanVienDuyet=" + maNhanVienDuyet + ", ngayDuyet="
                + ngayDuyet + ", ghiChu=" + ghiChu + "]";
    }

}
