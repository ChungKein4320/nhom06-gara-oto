package entity;

import java.sql.Timestamp;

// ===== ENTITY: PhieuTiepNhan =====
public class PhieuTiepNhan {
    private String maPTN;
    private KhachHang maKhachHang;
    private PhuongTien VIN;
    private NhanVien maNhanVien;
    private Timestamp ngayTiepNhan;
    private String soODO;

    // ===== KHỞI TẠO ĐỐI TƯỢNG: PhieuTiepNhan() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public PhieuTiepNhan() {
    }

    // ===== PHƯƠNG THỨC: getMaPTN() — Lấy mã ptn. =====
    public String getMaPTN() {
        return maPTN;
    }

    // ===== PHƯƠNG THỨC: setMaPTN(String) — Gán mã ptn. =====
    public void setMaPTN(String value) {
        if (value == null || !value.matches("PTN[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu tiếp nhận không hợp lệ");
        this.maPTN = value;
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

    // ===== PHƯƠNG THỨC: getVIN() — Lấy vin. =====
    public PhuongTien getVIN() {
        return VIN;
    }

    // ===== PHƯƠNG THỨC: setVIN(PhuongTien) — Gán vin. =====
    public void setVIN(PhuongTien value) {
        if (value == null || value.getVIN() == null || !value.getVIN().matches("[A-HJ-NPR-Z0-9]{17}"))
            throw new IllegalArgumentException("Số VIN không hợp lệ");
        this.VIN = value;
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

    // ===== PHƯƠNG THỨC: getNgayTiepNhan() — Lấy ngày tiếp nhân. =====
    public Timestamp getNgayTiepNhan() {
        return ngayTiepNhan;
    }

    // ===== PHƯƠNG THỨC: setNgayTiepNhan(Timestamp) — Gán ngày tiếp nhân. =====
    public void setNgayTiepNhan(Timestamp value) {
        if (value == null)
            throw new IllegalArgumentException("Ngày tiếp nhận không hợp lệ");
        this.ngayTiepNhan = value;
    }

    // ===== PHƯƠNG THỨC: getSoODO() — Lấy số odo. =====
    public String getSoODO() {
        return soODO;
    }

    // ===== PHƯƠNG THỨC: setSoODO(String) — Gán số odo. =====
    public void setSoODO(String value) {
        if (value == null || !value.matches("[0-9]{6}"))
            throw new IllegalArgumentException("Số ODO không hợp lệ");
        this.soODO = value;
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: PhieuTiepNhan(String, KhachHang, NhanVien, PhuongTien, Timestamp, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public PhieuTiepNhan(String maPTN, KhachHang maKhachHang, NhanVien maNhanVien, PhuongTien VIN,
            Timestamp ngayTiepNhan, String soODO) {

        this.maPTN = maPTN;
        this.maKhachHang = maKhachHang;
        this.maNhanVien = maNhanVien;
        this.VIN = VIN;
        this.ngayTiepNhan = ngayTiepNhan;
        this.soODO = soODO;
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "PhieuTiepNhan{" + "maPTN='" + maPTN + '\'' + ", maKhachHang=" + maKhachHang + ", maNhanVien="
                + maNhanVien + ", VIN=" + VIN + ", ngayTiepNhan=" + ngayTiepNhan + ", soODO='" + soODO + '\''
                + '}';
    }
}
