package entity;

import java.sql.Timestamp;

// ===== ENTITY: LichNhacBaoDuong =====
public class LichNhacBaoDuong {
    private String maLichNhac;
    private KhachHang maKhachHang;
    private PhieuSuaChua maPSC;
    private PhuongTien VIN;
    private Timestamp ngayNhac;
    private String noiDung;
    private String trangThai;

    // ===== KHỞI TẠO ĐỐI TƯỢNG: LichNhacBaoDuong() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public LichNhacBaoDuong() {
    }

    // ===== PHƯƠNG THỨC: getMaLichNhac() — Lấy mã lich nhac. =====
    public String getMaLichNhac() {
        return maLichNhac;
    }

    // ===== PHƯƠNG THỨC: setMaLichNhac(String) — Gán mã lich nhac. =====
    public void setMaLichNhac(String value) {
        if (value == null || !value.matches("LNBD[0-9]{3}"))
            throw new IllegalArgumentException("Mã lịch nhắc không hợp lệ");
        this.maLichNhac = value;
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

    // ===== PHƯƠNG THỨC: getMaPSC() — Lấy mã psc. =====
    public PhieuSuaChua getMaPSC() {
        return maPSC;
    }

    // ===== PHƯƠNG THỨC: setMaPSC(PhieuSuaChua) — Gán mã psc. =====
    public void setMaPSC(PhieuSuaChua value) {
        if (value == null || value.getMaPSC() == null || !value.getMaPSC().matches("PSC[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu sửa chữa không hợp lệ");
        this.maPSC = value;
    }

    // ===== PHƯƠNG THỨC: getVIN() — Lấy vin. =====
    public PhuongTien getVIN() {
        return VIN;
    }

    // ===== PHƯƠNG THỨC: setVIN(PhuongTien) — Gán vin. =====
    public void setVIN(PhuongTien value) {
        if (value == null || value.getVIN() == null || !value.getVIN().matches("[A-HJ-NPR-Z0-9]{17}"))
            throw new IllegalArgumentException("Phương tiện không hợp lệ");
        this.VIN = value;
    }

    // ===== PHƯƠNG THỨC: getNgayNhac() — Lấy ngày nhac. =====
    public Timestamp getNgayNhac() {
        return ngayNhac;
    }

    // ===== PHƯƠNG THỨC: setNgayNhac(Timestamp) — Gán ngày nhac. =====
    public void setNgayNhac(Timestamp value) {
        if (value == null)
            throw new IllegalArgumentException("Ngày nhắc không hợp lệ");
        this.ngayNhac = value;
    }

    // ===== PHƯƠNG THỨC: getNoiDung() — Lấy noi dung. =====
    public String getNoiDung() {
        return noiDung;
    }

    // ===== PHƯƠNG THỨC: setNoiDung(String) — Gán noi dung. =====
    public void setNoiDung(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Nội dung không được rỗng");
        this.noiDung = value.trim();
    }

    // ===== PHƯƠNG THỨC: getTrangThai() — Lấy trạng thái. =====
    public String getTrangThai() {
        return trangThai;
    }

    // ===== PHƯƠNG THỨC: setTrangThai(String) — Gán trạng thái. =====
    public void setTrangThai(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Trạng thái không được rỗng");
        this.trangThai = value.trim();
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: LichNhacBaoDuong(String, KhachHang, PhuongTien, PhieuSuaChua, Timestamp, String, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public LichNhacBaoDuong(String maLichNhac, KhachHang maKhachHang, PhuongTien VIN, PhieuSuaChua maPSC,
            Timestamp ngayNhac, String noiDung, String trangThai) {

        this.maLichNhac = maLichNhac;
        this.maKhachHang = maKhachHang;
        this.VIN = VIN;
        this.maPSC = maPSC;
        this.ngayNhac = ngayNhac;
        this.noiDung = noiDung;
        this.trangThai = trangThai;
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "LichNhacBaoDuong{" + "maLichNhac='" + maLichNhac + '\'' + ", maKhachHang=" + maKhachHang
                + ", maPSC=" + maPSC + ", VIN=" + VIN + ", ngayNhac=" + ngayNhac + ", noiDung='" + noiDung
                + '\'' + ", trangThai='" + trangThai + '\'' + '}';
    }

}
