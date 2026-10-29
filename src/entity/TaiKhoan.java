package entity;

import java.sql.Timestamp;

public class TaiKhoan {
    private String maTaiKhoan;
    private String maNhanVien;
    private String tenDangNhap;
    private String matKhau;
    private String trangThai;
    private Timestamp ngayTao;

    public TaiKhoan() { }

    public TaiKhoan(String maTaiKhoan, String maNhanVien, String tenDangNhap, String matKhau, String trangThai,
			Timestamp ngayTao) {
		super();
		this.maTaiKhoan = maTaiKhoan;
		this.maNhanVien = maNhanVien;
		this.tenDangNhap = tenDangNhap;
		this.matKhau = matKhau;
		this.trangThai = trangThai;
		this.ngayTao = ngayTao;
	}

    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public void setMaTaiKhoan(String value) {
        this.maTaiKhoan = value;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String value) {
        this.maNhanVien = value;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Tên đăng nhập không được rỗng");
        this.tenDangNhap = value.trim();
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Mật khẩu không được rỗng");
        this.matKhau = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        if (!java.util.Set.of("Đang hoạt động", "Ngừng hoạt động").contains(value))
            throw new IllegalArgumentException("Trạng thái tài khoản không hợp lệ");
        this.trangThai = value;
    }

    public Timestamp getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Timestamp value) {
        if (value == null) throw new IllegalArgumentException("Ngày tạo không được rỗng");
        this.ngayTao = value;
    }
    
    public void doiMatKhau(String matKhauMoi) {
        if (matKhauMoi == null || matKhauMoi.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Mật khẩu mới không được để trống");
        }

        setMatKhau(matKhauMoi);
    }

    @Override
    public String toString() {
        return "TaiKhoan [maTaiKhoan=" + maTaiKhoan
                + ", maNhanVien=" + maNhanVien
                + ", tenDangNhap=" + tenDangNhap
                + ", trangThai=" + trangThai
                + ", ngayTao=" + ngayTao + "]";
    }
}