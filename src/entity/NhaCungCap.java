package entity;

public class NhaCungCap {
    private String maNCC;
    private String tenNCC;
    private String soDienThoai;
    private String email;
    private String diaChi;

    public NhaCungCap() { }

    public NhaCungCap(String maNCC, String tenNCC, String soDienThoai, String email, String diaChi) {
		super();
		this.maNCC = maNCC;
		this.tenNCC = tenNCC;
		this.soDienThoai = soDienThoai;
		this.email = email;
		this.diaChi = diaChi;
	}

    public String getMaNCC() {
        return maNCC;
    }

    public void setMaNCC(String value) {
        this.maNCC = value;
    }

    public String getTenNCC() {
        return tenNCC;
    }

    public void setTenNCC(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống");
        if (value.trim().length() > 150)
            throw new IllegalArgumentException("Tên nhà cung cấp không được quá 150 ký tự");
        this.tenNCC = value.trim();
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String value) {
        if (value == null || !value.matches("0[35789][0-9]{8}"))
            throw new IllegalArgumentException("Số điện thoại nhà cung cấp phải gồm 10 chữ số hợp lệ");
        this.soDienThoai = value;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        if (value != null && !value.isBlank()
                && !value.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            throw new IllegalArgumentException("Email nhà cung cấp không đúng định dạng");
        if (value != null && value.length() > 254)
            throw new IllegalArgumentException("Email nhà cung cấp không được quá 254 ký tự");
        this.email = value == null ? null : value.trim();
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String value) {
        if (value != null && value.length() > 255)
            throw new IllegalArgumentException("Địa chỉ nhà cung cấp không được quá 255 ký tự");
        this.diaChi = value;
    }

    public void capNhatThongTin(String tenNCC, String soDienThoai, String email, String diaChi) {
		setTenNCC(tenNCC);
		setSoDienThoai(soDienThoai);
		setEmail(email);
		setDiaChi(diaChi);
	}
    
    @Override
    public String toString() {
        return "NhaCungCap [maNCC=" + maNCC
                + ", tenNCC=" + tenNCC
                + ", soDienThoai=" + soDienThoai
                + ", email=" + email
                + ", diaChi=" + diaChi + "]";
    }

}