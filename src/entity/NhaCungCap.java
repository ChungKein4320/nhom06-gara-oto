package entity;

public class NhaCungCap {
    private String maNCC;
    private String tenNCC;
    private String soDienThoai;
    private String email;
    private String diaChi;

    public NhaCungCap() { }

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
        this.tenNCC = value;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String value) {
        this.soDienThoai = value;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        this.email = value;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String value) {
        this.diaChi = value;
    }

}