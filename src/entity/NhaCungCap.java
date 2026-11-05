package entity;

// ===== ENTITY: NhaCungCap =====
public class NhaCungCap {
    private String maNCC;
    private String tenNCC;
    private String soDienThoai;
    private String email;
    private String diaChi;

    // ===== KHỞI TẠO ĐỐI TƯỢNG: NhaCungCap() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public NhaCungCap() {
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: NhaCungCap(String, String, String, String, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public NhaCungCap(String maNCC, String tenNCC, String soDienThoai, String email, String diaChi) {
        super();
        this.maNCC = maNCC;
        this.tenNCC = tenNCC;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.diaChi = diaChi;
    }

    // ===== PHƯƠNG THỨC: getMaNCC() — Lấy mã ncc. =====
    public String getMaNCC() {
        return maNCC;
    }

    // ===== PHƯƠNG THỨC: setMaNCC(String) — Gán mã ncc. =====
    public void setMaNCC(String value) {
        if (value == null || !value.matches("NCC[0-9]{3}"))
            throw new IllegalArgumentException("Mã nhà cung cấp không hợp lệ");
        this.maNCC = value;
    }

    // ===== PHƯƠNG THỨC: getTenNCC() — Lấy ten ncc. =====
    public String getTenNCC() {
        return tenNCC;
    }

    // ===== PHƯƠNG THỨC: setTenNCC(String) — Gán ten ncc. =====
    public void setTenNCC(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống");
        if (value.trim().length() > 150)
            throw new IllegalArgumentException("Tên nhà cung cấp không được quá 150 ký tự");
        this.tenNCC = value.trim();
    }

    // ===== PHƯƠNG THỨC: getSoDienThoai() — Lấy số dien thoai. =====
    public String getSoDienThoai() {
        return soDienThoai;
    }

    // ===== PHƯƠNG THỨC: setSoDienThoai(String) — Gán số dien thoai. =====
    public void setSoDienThoai(String value) {
        if (value == null || !value.matches("0[35789][0-9]{8}"))
            throw new IllegalArgumentException("Số điện thoại nhà cung cấp phải gồm 10 chữ số hợp lệ");
        this.soDienThoai = value;
    }

    // ===== PHƯƠNG THỨC: getEmail() — Lấy email. =====
    public String getEmail() {
        return email;
    }

    // ===== PHƯƠNG THỨC: setEmail(String) — Gán email. =====
    public void setEmail(String value) {
        if (value != null && !value.isBlank()
                && !value.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            throw new IllegalArgumentException("Email nhà cung cấp không đúng định dạng");
        if (value != null && value.length() > 254)
            throw new IllegalArgumentException("Email nhà cung cấp không được quá 254 ký tự");
        this.email = value == null ? null : value.trim();
    }

    // ===== PHƯƠNG THỨC: getDiaChi() — Lấy địa chi. =====
    public String getDiaChi() {
        return diaChi;
    }

    // ===== PHƯƠNG THỨC: setDiaChi(String) — Gán địa chi. =====
    public void setDiaChi(String value) {
        if (value != null && value.length() > 255)
            throw new IllegalArgumentException("Địa chỉ nhà cung cấp không được quá 255 ký tự");
        this.diaChi = value;
    }

    // ===== PHƯƠNG THỨC: capNhatThongTin(String, String, String, String) — Cập nhật nhat thông tin. =====
    public void capNhatThongTin(String tenNCC, String soDienThoai, String email, String diaChi) {
        setTenNCC(tenNCC);
        setSoDienThoai(soDienThoai);
        setEmail(email);
        setDiaChi(diaChi);
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "NhaCungCap [maNCC=" + maNCC + ", tenNCC=" + tenNCC + ", soDienThoai=" + soDienThoai
                + ", email=" + email + ", diaChi=" + diaChi + "]";
    }

}
