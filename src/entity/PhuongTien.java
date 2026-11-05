package entity;

import java.math.BigDecimal;

// ===== ENTITY: PHƯƠNG TIỆN =====

public class PhuongTien {
    // ===== THUỘC TÍNH =====
    private String VIN;
    private KhachHang maKhachHang;
    private String bienSo;
    private String hangXe;
    private String dongXe;
    private String mauXe;
    private Integer namSanXuat;
    private String soMay;
    private BigDecimal soKm;

    // ===== KHỞI TẠO ĐỐI TƯỢNG =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: PhuongTien() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public PhuongTien() {
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: PhuongTien(String, KhachHang, String, String, String, String, Integer, String, BigDecimal) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public PhuongTien(String vIN, KhachHang maKhachHang, String bienSo, String hangXe, String dongXe,
            String mauXe, Integer namSanXuat, String soMay, BigDecimal soKm) {
        super();
        VIN = vIN;
        this.maKhachHang = maKhachHang;
        this.bienSo = bienSo;
        this.hangXe = hangXe;
        this.dongXe = dongXe;
        this.mauXe = mauXe;
        this.namSanXuat = namSanXuat;
        this.soMay = soMay;
        this.soKm = soKm;
    }

    // ===== GETTER VÀ SETTER =====

    // ===== PHƯƠNG THỨC: getVIN() — Lấy vin. =====
    public String getVIN() {
        return VIN;
    }

    // ===== PHƯƠNG THỨC: setVIN(String) — Gán vin. =====
    public void setVIN(String value) {
        if (value == null || !value.matches("[A-HJ-NPR-Z0-9]{17}"))
            throw new IllegalArgumentException("VIN phải gồm 17 ký tự in hoa hoặc chữ số, không có I, O, Q");
        this.VIN = value;
    }

    // ===== PHƯƠNG THỨC: getMaKhachHang() — Lấy mã khách hàng. =====
    public KhachHang getMaKhachHang() {
        return maKhachHang;
    }

    // ===== PHƯƠNG THỨC: setMaKhachHang(KhachHang) — Gán mã khách hàng. =====
    public void setMaKhachHang(KhachHang value) {
        if (value == null || value.getMaKhachHang() == null || !value.getMaKhachHang().matches("KH[0-9]{3}"))
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ");
        this.maKhachHang = value;
    }

    // ===== PHƯƠNG THỨC: getBienSo() — Lấy bien số. =====
    public String getBienSo() {
        return bienSo;
    }

    // ===== PHƯƠNG THỨC: setBienSo(String) — Gán bien số. =====
    public void setBienSo(String value) {
        if (value == null || !value.matches("[0-9]{2}[A-Z][0-9]{5}"))
            throw new IllegalArgumentException("Biển số phải có dạng 30F25658");
        this.bienSo = value;
    }

    // ===== PHƯƠNG THỨC: getHangXe() — Lấy hàng xe. =====
    public String getHangXe() {
        return hangXe;
    }

    // ===== PHƯƠNG THỨC: setHangXe(String) — Gán hàng xe. =====
    public void setHangXe(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Hãng xe không được rỗng");
        this.hangXe = value.trim();
    }

    // ===== PHƯƠNG THỨC: getDongXe() — Lấy dong xe. =====
    public String getDongXe() {
        return dongXe;
    }

    // ===== PHƯƠNG THỨC: setDongXe(String) — Gán dong xe. =====
    public void setDongXe(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Dòng xe không được rỗng");
        this.dongXe = value.trim();
    }

    // ===== PHƯƠNG THỨC: getMauXe() — Lấy mau xe. =====
    public String getMauXe() {
        return mauXe;
    }

    // ===== PHƯƠNG THỨC: setMauXe(String) — Gán mau xe. =====
    public void setMauXe(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Màu xe không được rỗng");
        this.mauXe = value.trim();
    }

    // ===== PHƯƠNG THỨC: getNamSanXuat() — Lấy nam san xuat. =====
    public Integer getNamSanXuat() {
        return namSanXuat;
    }

    // ===== PHƯƠNG THỨC: setNamSanXuat(Integer) — Gán nam san xuat. =====
    public void setNamSanXuat(Integer value) {
        if (value == null || value > java.time.Year.now().getValue() || value < 1886)
            throw new IllegalArgumentException("Năm sản xuất không hợp lệ");
        this.namSanXuat = value;
    }

    // ===== PHƯƠNG THỨC: getSoMay() — Lấy số may. =====
    public String getSoMay() {
        return soMay;
    }

    // ===== PHƯƠNG THỨC: setSoMay(String) — Gán số may. =====
    public void setSoMay(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Số máy không được rỗng");
        this.soMay = value.trim();
    }

    // ===== PHƯƠNG THỨC: getSoKm() — Lấy số km. =====
    public BigDecimal getSoKm() {
        return soKm;
    }

    // ===== PHƯƠNG THỨC: setSoKm(BigDecimal) — Gán số km. =====
    public void setSoKm(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Số km không được nhỏ hơn 0");
        this.soKm = value;
    }

    // ===== PHƯƠNG THỨC: capNhatBienSo(String) — Cập nhật nhat bien số. =====
    public void capNhatBienSo(String bienSoMoi) {
        setBienSo(bienSoMoi);
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "PhuongTien [VIN=" + VIN + ", maKhachHang=" + maKhachHang + ", bienSo=" + bienSo + ", hangXe="
                + hangXe + ", dongXe=" + dongXe + ", mauXe=" + mauXe + ", namSanXuat=" + namSanXuat
                + ", soMay=" + soMay + ", soKm=" + soKm + "]";
    }

}
