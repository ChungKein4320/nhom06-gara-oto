package entity;

// ===== ENTITY: ĐỐI TƯỢNG KHÁCH HÀNG =====

public class KhachHang {
    // ===== THUỘC TÍNH =====
    private String maKhachHang;
    private String hoTenKH;
    private String soDienThoai;
    private boolean gioiTinh;
    private String email;
    private String diaChi;

    // ===== KHỞI TẠO ĐỐI TƯỢNG =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: KhachHang() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public KhachHang() {
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: KhachHang(String, String, String, boolean, String, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
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

    // ===== PHƯƠNG THỨC: isGioiTinh() — Kiểm tra giới tính. =====
    public boolean isGioiTinh() {
        return gioiTinh;
    }

    // ===== GETTER VÀ SETTER =====

    // ===== PHƯƠNG THỨC: getGioiTinh() — Lấy giới tính. =====
    public boolean getGioiTinh() {
        return gioiTinh;
    }

    // ===== PHƯƠNG THỨC: setGioiTinh(boolean) — Gán giới tính. =====
    public void setGioiTinh(boolean value) {
        gioiTinh = value;
    }

    // ===== PHƯƠNG THỨC: getMaKhachHang() — Lấy mã khách hàng. =====
    public String getMaKhachHang() {
        return maKhachHang;
    }

    // ===== PHƯƠNG THỨC: setMaKhachHang(String) — Gán mã khách hàng. =====
    public void setMaKhachHang(String value) {
        if (value == null || !value.matches("KH[0-9]{3}"))
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ");
        this.maKhachHang = value;
    }

    // ===== PHƯƠNG THỨC: getHoTenKH() — Lấy ho ten kh. =====
    public String getHoTenKH() {
        return hoTenKH;
    }

    // ===== PHƯƠNG THỨC: setHoTenKH(String) — Gán ho ten kh. =====
    public void setHoTenKH(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Họ tên khách hàng không được rỗng");
        this.hoTenKH = value.trim();
    }

    // ===== PHƯƠNG THỨC: getSoDienThoai() — Lấy số dien thoai. =====
    public String getSoDienThoai() {
        return soDienThoai;
    }

    // ===== PHƯƠNG THỨC: setSoDienThoai(String) — Gán số dien thoai. =====
    public void setSoDienThoai(String value) {
        if (value == null || !value.matches("0[35789][0-9]{8}"))
            throw new IllegalArgumentException("Số điện thoại không hợp lệ");
        this.soDienThoai = value;
    }

    // ===== PHƯƠNG THỨC: getEmail() — Lấy email. =====
    public String getEmail() {
        return email;
    }

    // ===== PHƯƠNG THỨC: setEmail(String) — Gán email. =====
    public void setEmail(String value) {
        if (value == null || !value.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            throw new IllegalArgumentException("Email không đúng định dạng");
        this.email = value.trim();
    }

    // ===== PHƯƠNG THỨC: getDiaChi() — Lấy địa chi. =====
    public String getDiaChi() {
        return diaChi;
    }

    // ===== PHƯƠNG THỨC: setDiaChi(String) — Gán địa chi. =====
    public void setDiaChi(String value) {
        this.diaChi = value;
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "KhachHang [maKhachHang=" + maKhachHang + ", hoTenKH=" + hoTenKH + ", soDienThoai="
                + soDienThoai + ", gioiTinh=" + gioiTinh + ", email=" + email + ", diaChi=" + diaChi + "]";
    }

}
