package entity;

import java.sql.Date;

// ===== ENTITY: ĐỐI TƯỢNG NHÂN VIÊN =====

public class NhanVien {
    // ===== THUỘC TÍNH =====
    private String maNhanVien;
    private String hoTenNV;
    private Date ngaySinh;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private String vaiTro;
    private boolean gioiTinh;
    private String trangThai;

    // ===== KHỞI TẠO ĐỐI TƯỢNG =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: NhanVien() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public NhanVien() {
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: NhanVien(String, String, Date, String, String, String, String, boolean, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public NhanVien(String maNhanVien, String hoTenNV, Date ngaySinh, String soDienThoai, String email,
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

    // ===== PHƯƠNG THỨC: getMaNhanVien() — Lấy mã nhân viên. =====
    public String getMaNhanVien() {
        return maNhanVien;
    }

    // ===== PHƯƠNG THỨC: setMaNhanVien(String) — Gán mã nhân viên. =====
    public void setMaNhanVien(String value) {
        if (value == null || !value.matches("NV[0-9]{3}"))
            throw new IllegalArgumentException("Mã nhân viên không hợp lệ");
        this.maNhanVien = value;
    }

    // ===== PHƯƠNG THỨC: getHoTenNV() — Lấy ho ten nv. =====
    public String getHoTenNV() {
        return hoTenNV;
    }

    // ===== PHƯƠNG THỨC: setHoTenNV(String) — Gán ho ten nv. =====
    public void setHoTenNV(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Họ tên nhân viên không được rỗng");
        this.hoTenNV = value.trim();
    }

    // ===== PHƯƠNG THỨC: getNgaySinh() — Lấy ngày sinh. =====
    public Date getNgaySinh() {
        return ngaySinh;
    }

    // ===== PHƯƠNG THỨC: setNgaySinh(Date) — Gán ngày sinh. =====
    public void setNgaySinh(Date value) {
        if (value == null)
            throw new IllegalArgumentException("Ngày sinh không được rỗng");
        int tuoi = java.time.Period.between(value.toLocalDate(), java.time.LocalDate.now()).getYears();
        if (tuoi < 18 || tuoi > 65)
            throw new IllegalArgumentException("Nhân viên phải từ 18 đến 65 tuổi");
        this.ngaySinh = value;
    }

    // ===== PHƯƠNG THỨC: getSoDienThoai() — Lấy số dien thoai. =====
    public String getSoDienThoai() {
        return soDienThoai;
    }

    // ===== PHƯƠNG THỨC: setSoDienThoai(String) — Gán số dien thoai. =====
    public void setSoDienThoai(String value) {
        if (value == null || !value.matches("0[35789][0-9]{8}"))
            throw new IllegalArgumentException(
                    "Số điện thoại phải gồm 10 chữ số, bắt đầu bằng 03, 05, 07, 08 hoặc 09");
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

    // ===== PHƯƠNG THỨC: getVaiTro() — Lấy vai tro. =====
    public String getVaiTro() {
        return vaiTro;
    }

    // ===== PHƯƠNG THỨC: setVaiTro(String) — Gán vai tro. =====
    public void setVaiTro(String value) {
        if (value == null || !java.util.Set
                .of("Nhân viên kỹ thuật", "Nhân viên bán hàng", "Nhân viên kho", "Nhân viên quản lý")
                .contains(value))
            throw new IllegalArgumentException("Vai trò không hợp lệ");
        this.vaiTro = value;
    }

    // ===== PHƯƠNG THỨC: getTrangThai() — Lấy trạng thái. =====
    public String getTrangThai() {
        return trangThai;
    }

    // ===== PHƯƠNG THỨC: setTrangThai(String) — Gán trạng thái. =====
    public void setTrangThai(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Trạng thái không được rỗng");
        if (!java.util.Set.of("Đang làm", "Đã nghỉ").contains(value))
            throw new IllegalArgumentException("Trạng thái nhân viên không hợp lệ");
        this.trangThai = value;
    }

    // ===== PHƯƠNG THỨC: capNhatThongTin(String, Date, String, String, String, String, boolean, String) — Cập nhật nhat thông tin. =====
    public void capNhatThongTin(String hoTenNV, Date ngaySinh, String soDienThoai, String email,
            String diaChi, String vaiTro, boolean gioiTinh, String trangThai) {
        setHoTenNV(hoTenNV);
        setNgaySinh(ngaySinh);
        setSoDienThoai(soDienThoai);
        setEmail(email);
        setDiaChi(diaChi);
        setVaiTro(vaiTro);
        setGioiTinh(gioiTinh);
        setTrangThai(trangThai);
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "NhanVien [maNhanVien=" + maNhanVien + ", hoTenNV=" + hoTenNV + ", ngaySinh=" + ngaySinh
                + ", soDienThoai=" + soDienThoai + ", email=" + email + ", diaChi=" + diaChi + ", vaiTro="
                + vaiTro + ", gioiTinh=" + gioiTinh + ", trangThai=" + trangThai + "]";
    }

}
