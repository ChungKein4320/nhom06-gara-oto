package entity;

import java.sql.Timestamp;

public class NhanVien {
    private String maNhanVien;
    private String hoTenNV;
    private Timestamp ngaySinh;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private String vaiTro;
    private boolean gioiTinh;
    private String trangThai;

    public NhanVien() { }

    public NhanVien(String maNhanVien, String hoTenNV, Timestamp ngaySinh, String soDienThoai, String email,
			String diaChi, String vaiTro, boolean gioiTinh, String trangThai) {
		super();
		this.maNhanVien = maNhanVien;
		this.hoTenNV = hoTenNV;
		this.ngaySinh = ngaySinh;
		this.soDienThoai = soDienThoai;
		this.email = email;
		this.diaChi = diaChi;
		this.vaiTro = vaiTro;
		this.gioiTinh = gioiTinh;
		this.trangThai = trangThai;
	}

    public boolean isGioiTinh() { return gioiTinh; }
    public boolean getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(boolean value) { gioiTinh = value; }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String value) {
        this.maNhanVien = value;
    }

    public String getHoTenNV() {
        return hoTenNV;
    }

    public void setHoTenNV(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Họ tên nhân viên không được rỗng");
        this.hoTenNV = value.trim();
    }

    public Timestamp getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Timestamp value) {
        if (value == null) throw new IllegalArgumentException("Ngày sinh không được rỗng");
        int tuoi = java.time.Period.between(value.toLocalDateTime().toLocalDate(), java.time.LocalDate.now()).getYears();
        if (tuoi < 18 || tuoi > 65) throw new IllegalArgumentException("Nhân viên phải từ 18 đến 65 tuổi");
        this.ngaySinh = value;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String value) {
        if (value == null || !value.matches("0[35789][0-9]{8}"))
            throw new IllegalArgumentException("Số điện thoại phải gồm 10 chữ số, bắt đầu bằng 03, 05, 07, 08 hoặc 09");
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

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String value) {
        if (!java.util.Set.of("Nhân viên kỹ thuật", "Nhân viên bán hàng", "Nhân viên kho", "Nhân viên quản lý").contains(value))
            throw new IllegalArgumentException("Vai trò không hợp lệ");
        this.vaiTro = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Trạng thái không được rỗng");
        if (!java.util.Set.of("Đang làm", "Đã nghỉ").contains(value))
            throw new IllegalArgumentException("Trạng thái nhân viên không hợp lệ");
        this.trangThai = value;
    }

    public void capNhatThongTin(String hoTenNV, Timestamp ngaySinh, 
    							String soDienThoai, String email,
    							String diaChi, String vaiTro,
    							boolean gioiTinh, String trangThai) {
		setHoTenNV(hoTenNV);
		setNgaySinh(ngaySinh);
		setSoDienThoai(soDienThoai);
		setEmail(email);
		setDiaChi(diaChi);
		setVaiTro(vaiTro);
		setGioiTinh(gioiTinh);
		setTrangThai(trangThai);
	}
    
    @Override
    public String toString() {
        return "NhanVien [maNhanVien=" + maNhanVien
                + ", hoTenNV=" + hoTenNV
                + ", ngaySinh=" + ngaySinh
                + ", soDienThoai=" + soDienThoai
                + ", email=" + email
                + ", diaChi=" + diaChi
                + ", vaiTro=" + vaiTro
                + ", gioiTinh=" + gioiTinh
                + ", trangThai=" + trangThai + "]";
    }

}
