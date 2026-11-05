package entity;

import java.sql.Timestamp;

// ===== ENTITY: YeuCauNhapKho =====
public class YeuCauNhapKho {
    private String maYCNK;
    private NhaCungCap maNCC;
    private NhanVien maNhanVien;
    private Timestamp ngayTao;
    private String trangThai;
    private String maNhanVienDuyet;
    private Timestamp ngayDuyet;
    private String ghiChu;

    // ===== KHỞI TẠO ĐỐI TƯỢNG: YeuCauNhapKho() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public YeuCauNhapKho() {
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: YeuCauNhapKho(String, NhaCungCap, NhanVien, Timestamp, String, String, Timestamp, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public YeuCauNhapKho(String maYCNK, NhaCungCap maNCC, NhanVien maNhanVien, Timestamp ngayTao,
            String trangThai, String maNhanVienDuyet, Timestamp ngayDuyet, String ghiChu) {
        this.maYCNK = maYCNK;
        this.maNCC = maNCC;
        this.maNhanVien = maNhanVien;
        this.ngayTao = ngayTao;
        this.trangThai = trangThai;
        this.maNhanVienDuyet = maNhanVienDuyet;
        this.ngayDuyet = ngayDuyet;
        this.ghiChu = ghiChu;
    }

    // ===== PHƯƠNG THỨC: getMaYCNK() — Lấy mã ycnk. =====
    public String getMaYCNK() {
        return maYCNK;
    }

    // ===== PHƯƠNG THỨC: setMaYCNK(String) — Gán mã ycnk. =====
    public void setMaYCNK(String value) {
        if (value == null || !value.matches("YCN[0-9]{3}"))
            throw new IllegalArgumentException("Mã yêu cầu nhập kho không hợp lệ");
        this.maYCNK = value;
    }

    // ===== PHƯƠNG THỨC: getMaNCC() — Lấy mã ncc. =====
    public NhaCungCap getMaNCC() {
        return maNCC;
    }

    // ===== PHƯƠNG THỨC: setMaNCC(NhaCungCap) — Gán mã ncc. =====
    public void setMaNCC(NhaCungCap value) {
        if (value == null || value.getMaNCC() == null || !value.getMaNCC().matches("NCC[0-9]{3}"))
            throw new IllegalArgumentException("Mã nhà cung cấp không hợp lệ");
        this.maNCC = value;
    }

    // ===== PHƯƠNG THỨC: getMaNhanVien() — Lấy mã nhân viên. =====
    public NhanVien getMaNhanVien() {
        return maNhanVien;
    }

    // ===== PHƯƠNG THỨC: setMaNhanVien(NhanVien) — Gán mã nhân viên. =====
    public void setMaNhanVien(NhanVien value) {
        if (value == null || value.getMaNhanVien() == null || !value.getMaNhanVien().matches("NV[0-9]{3}"))
            throw new IllegalArgumentException("Mã nhân viên không hợp lệ");
        this.maNhanVien = value;
    }

    // ===== PHƯƠNG THỨC: getNgayTao() — Lấy ngày tao. =====
    public Timestamp getNgayTao() {
        return ngayTao;
    }

    // ===== PHƯƠNG THỨC: setNgayTao(Timestamp) — Gán ngày tao. =====
    public void setNgayTao(Timestamp value) {
        if (value == null || value.after(new Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày tạo không hợp lệ");
        this.ngayTao = value;
    }

    // ===== PHƯƠNG THỨC: getTrangThai() — Lấy trạng thái. =====
    public String getTrangThai() {
        return trangThai;
    }

    // ===== PHƯƠNG THỨC: setTrangThai(String) — Gán trạng thái. =====
    public void setTrangThai(String value) {
        if (value == null
                || !java.util.Set.of("Chờ duyệt", "Đã duyệt", "Từ chối", "Đã xử lý").contains(value))
            throw new IllegalArgumentException("Trạng thái yêu cầu nhập kho không hợp lệ");
        this.trangThai = value;
    }

    // ===== PHƯƠNG THỨC: getMaNhanVienDuyet() — Lấy mã nhân viên duyet. =====
    public String getMaNhanVienDuyet() {
        return maNhanVienDuyet;
    }

    // ===== PHƯƠNG THỨC: setMaNhanVienDuyet(String) — Gán mã nhân viên duyet. =====
    public void setMaNhanVienDuyet(String value) {
        if (value != null && !value.matches("NV[0-9]{3}"))
            throw new IllegalArgumentException("Mã nhân viên duyệt không hợp lệ");
        this.maNhanVienDuyet = value;
    }

    // ===== PHƯƠNG THỨC: getNgayDuyet() — Lấy ngày duyet. =====
    public Timestamp getNgayDuyet() {
        return ngayDuyet;
    }

    // ===== PHƯƠNG THỨC: setNgayDuyet(Timestamp) — Gán ngày duyet. =====
    public void setNgayDuyet(Timestamp value) {
        if (value != null && (ngayTao == null || value.before(ngayTao)))
            throw new IllegalArgumentException("Ngày duyệt phải bằng hoặc sau ngày tạo yêu cầu");
        this.ngayDuyet = value;
    }

    // ===== PHƯƠNG THỨC: getGhiChu() — Lấy ghi chu. =====
    public String getGhiChu() {
        return ghiChu;
    }

    // ===== PHƯƠNG THỨC: setGhiChu(String) — Gán ghi chu. =====
    public void setGhiChu(String value) {
        if (value != null && value.length() > 255)
            throw new IllegalArgumentException("Ghi chú không được quá 255 ký tự");
        this.ghiChu = value;
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "YeuCauNhapKho [maYCNK=" + maYCNK + ", maNCC=" + maNCC + ", maNhanVien=" + maNhanVien
                + ", ngayTao=" + ngayTao + ", trangThai=" + trangThai + ", maNhanVienDuyet=" + maNhanVienDuyet
                + ", ngayDuyet=" + ngayDuyet + ", ghiChu=" + ghiChu + "]";
    }

}
