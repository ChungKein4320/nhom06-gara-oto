package entity;

import java.sql.Timestamp;

// ===== ENTITY: PhieuNhapKho =====
public class PhieuNhapKho {
    private String maPNK;
    private YeuCauNhapKho maYCNK;
    private NhaCungCap maNCC;
    private NhanVien maNhanVien;
    private Timestamp ngayNhap;
    private String trangThai;

    // ===== KHỞI TẠO ĐỐI TƯỢNG: PhieuNhapKho() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public PhieuNhapKho() {
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: PhieuNhapKho(String, YeuCauNhapKho, NhaCungCap, NhanVien, Timestamp, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public PhieuNhapKho(String maPNK, YeuCauNhapKho maYCNK, NhaCungCap maNCC, NhanVien maNhanVien,
            Timestamp ngayNhap, String trangThai) {
        this.maPNK = maPNK;
        this.maYCNK = maYCNK;
        this.maNCC = maNCC;
        this.maNhanVien = maNhanVien;
        this.ngayNhap = ngayNhap;
        this.trangThai = trangThai;
    }

    // ===== PHƯƠNG THỨC: getMaPNK() — Lấy mã pnk. =====
    public String getMaPNK() {
        return maPNK;
    }

    // ===== PHƯƠNG THỨC: setMaPNK(String) — Gán mã pnk. =====
    public void setMaPNK(String value) {
        if (value == null || !value.matches("PNK[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu nhập kho không hợp lệ");
        this.maPNK = value;
    }

    // ===== PHƯƠNG THỨC: getMaYCNK() — Lấy mã ycnk. =====
    public YeuCauNhapKho getMaYCNK() {
        return maYCNK;
    }

    // ===== PHƯƠNG THỨC: setMaYCNK(YeuCauNhapKho) — Gán mã ycnk. =====
    public void setMaYCNK(YeuCauNhapKho value) {
        if (value != null && (value.getMaYCNK() == null || !value.getMaYCNK().matches("YCN[0-9]{3}")))
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

    // ===== PHƯƠNG THỨC: getNgayNhap() — Lấy ngày nhập. =====
    public Timestamp getNgayNhap() {
        return ngayNhap;
    }

    // ===== PHƯƠNG THỨC: setNgayNhap(Timestamp) — Gán ngày nhập. =====
    public void setNgayNhap(Timestamp value) {
        if (value == null || value.after(new Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày nhập không hợp lệ");
        this.ngayNhap = value;
    }

    // ===== PHƯƠNG THỨC: getTrangThai() — Lấy trạng thái. =====
    public String getTrangThai() {
        return trangThai;
    }

    // ===== PHƯƠNG THỨC: setTrangThai(String) — Gán trạng thái. =====
    public void setTrangThai(String value) {
        java.util.Set<String> trangThaiHopLe = maYCNK == null
                ? java.util.Set.of("Chờ duyệt", "Đã duyệt", "Không duyệt")
                : java.util.Set.of("Chờ xác nhận nhập kho", "Đã nhập kho", "Đã hủy");
        if (value == null || !trangThaiHopLe.contains(value))
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        this.trangThai = value;
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "PhieuNhapKho [maPNK=" + maPNK + ", maYCNK=" + maYCNK + ", maNCC=" + maNCC + ", maNhanVien="
                + maNhanVien + ", ngayNhap=" + ngayNhap + ", trangThai=" + trangThai + "]";
    }
}
