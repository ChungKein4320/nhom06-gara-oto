package entity;

import java.sql.Timestamp;

// ===== ENTITY: ĐỐI TƯỢNG TÀI KHOẢN =====

public class TaiKhoan {
    // ===== THUỘC TÍNH =====
    private String maTaiKhoan;
    private NhanVien maNhanVien;
    private String tenDangNhap;
    private String matKhau;
    private String trangThai;
    private Timestamp ngayTao;

    // ===== KHỞI TẠO ĐỐI TƯỢNG =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: TaiKhoan() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public TaiKhoan() {
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: TaiKhoan(String, NhanVien, String, String, String, Timestamp) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public TaiKhoan(String maTaiKhoan, NhanVien maNhanVien, String tenDangNhap, String matKhau,
            String trangThai, Timestamp ngayTao) {
        super();
        this.maTaiKhoan = maTaiKhoan;
        this.maNhanVien = maNhanVien;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
    }

    // ===== GETTER VÀ SETTER =====

    // ===== PHƯƠNG THỨC: getMaTaiKhoan() — Lấy mã tài khoản. =====
    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    // ===== PHƯƠNG THỨC: setMaTaiKhoan(String) — Gán mã tài khoản. =====
    public void setMaTaiKhoan(String value) {
        if (value == null || !value.matches("TK[0-9]{3}"))
            throw new IllegalArgumentException("Mã tài khoản không hợp lệ");
        this.maTaiKhoan = value;
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

    // ===== PHƯƠNG THỨC: getTenDangNhap() — Lấy ten dang nhập. =====
    public String getTenDangNhap() {
        return tenDangNhap;
    }

    // ===== PHƯƠNG THỨC: setTenDangNhap(String) — Gán ten dang nhập. =====
    public void setTenDangNhap(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Tên đăng nhập không được rỗng");
        this.tenDangNhap = value.trim();
    }

    // ===== PHƯƠNG THỨC: getMatKhau() — Lấy mat khau. =====
    public String getMatKhau() {
        return matKhau;
    }

    // ===== PHƯƠNG THỨC: setMatKhau(String) — Gán mat khau. =====
    public void setMatKhau(String value) {
        if (value == null || value.length() != 8)
            throw new IllegalArgumentException("Mật khẩu không được rỗng và phải đủ 8 ký tự");
        this.matKhau = value;
    }

    // ===== PHƯƠNG THỨC: getTrangThai() — Lấy trạng thái. =====
    public String getTrangThai() {
        return trangThai;
    }

    // ===== PHƯƠNG THỨC: setTrangThai(String) — Gán trạng thái. =====
    public void setTrangThai(String value) {
        if (value == null || !java.util.Set.of("Đang hoạt động", "Ngừng hoạt động").contains(value))
            throw new IllegalArgumentException("Trạng thái tài khoản không hợp lệ");
        this.trangThai = value;
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

    // ===== PHƯƠNG THỨC: doiMatKhau(String) — Cập nhật mật khẩu tài khoản. =====
    public void doiMatKhau(String matKhauMoi) {
        if (matKhauMoi == null || matKhauMoi.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu mới không được để trống");
        }

        setMatKhau(matKhauMoi);
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "TaiKhoan [maTaiKhoan=" + maTaiKhoan + ", maNhanVien=" + maNhanVien + ", tenDangNhap="
                + tenDangNhap + ", trangThai=" + trangThai + ", ngayTao=" + ngayTao + "]";
    }
}
