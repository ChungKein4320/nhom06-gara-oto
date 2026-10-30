package entity;

public class KhachHang {
    private String maKhachHang;
    private String hoTenKH;
    private String soDienThoai;
    private boolean gioiTinh;
    private String email;
    private String diaChi;

    public KhachHang() { }

    public KhachHang(String maKhachHang, String hoTenKH, String soDienThoai, boolean gioiTinh, String email,
			String diaChi) {
		super();
		this.maKhachHang = maKhachHang;
		this.hoTenKH = hoTenKH;
		this.soDienThoai = soDienThoai;
		this.gioiTinh = gioiTinh;
		this.email = email;
		this.diaChi = diaChi;
	}

    public boolean isGioiTinh() { return gioiTinh; }
    public boolean getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(boolean value) { gioiTinh = value; }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String value) {
        this.maKhachHang = value;
    }

    public String getHoTenKH() {
        return hoTenKH;
    }

    public void setHoTenKH(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Họ tên khách hàng không được rỗng");
        this.hoTenKH = value.trim();
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String value) {
        if (value == null || !value.matches("0[35789][0-9]{8}"))
            throw new IllegalArgumentException("Số điện thoại không hợp lệ");
        this.soDienThoai = value;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        if (value == null || !value.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            throw new IllegalArgumentException("Email không đúng định dạng");
        this.email = value.trim();
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String value) {
        this.diaChi = value;
    }

    @Override
    public String toString() {
        return "KhachHang [maKhachHang=" + maKhachHang
                + ", hoTenKH=" + hoTenKH
                + ", soDienThoai=" + soDienThoai
                + ", gioiTinh=" + gioiTinh
                + ", email=" + email
                + ", diaChi=" + diaChi + "]";
    }

}
