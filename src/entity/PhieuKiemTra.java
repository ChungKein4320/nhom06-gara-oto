package entity;

import java.sql.Timestamp;

// ===== ENTITY: PhieuKiemTra =====
public class PhieuKiemTra {
    private String maPKT;
    private PhieuTiepNhan maPTN;
    private NhanVien maNhanVien;
    private PhuongTien VIN;
    private Timestamp ngayKiemTra;
    private KhachHang maKhachHang;

    // ===== KHỞI TẠO ĐỐI TƯỢNG: PhieuKiemTra() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public PhieuKiemTra() {
    }

    // ===== PHƯƠNG THỨC: getMaPKT() — Lấy mã pkt. =====
    public String getMaPKT() {
        return maPKT;
    }

    // ===== PHƯƠNG THỨC: setMaPKT(String) — Gán mã pkt. =====
    public void setMaPKT(String value) {
        if (value == null || !value.matches("PKT[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu kiểm tra không hợp lệ");
        this.maPKT = value;
    }

    // ===== PHƯƠNG THỨC: getMaPTN() — Lấy mã ptn. =====
    public PhieuTiepNhan getMaPTN() {
        return maPTN;
    }

    // ===== PHƯƠNG THỨC: setMaPTN(PhieuTiepNhan) — Gán mã ptn. =====
    public void setMaPTN(PhieuTiepNhan value) {
        if (value == null || value.getMaPTN() == null || !value.getMaPTN().matches("PTN[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu tiếp nhận không hợp lệ");
        this.maPTN = value;
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

    // ===== PHƯƠNG THỨC: getVIN() — Lấy vin. =====
    public PhuongTien getVIN() {
        return VIN;
    }

    // ===== PHƯƠNG THỨC: setVIN(PhuongTien) — Gán vin. =====
    public void setVIN(PhuongTien value) {
        if (value == null || value.getVIN() == null || !value.getVIN().matches("[A-HJ-NPR-Z0-9]{17}"))
            throw new IllegalArgumentException("Mã phương tiện không hợp lệ");
        this.VIN = value;
    }

    // ===== PHƯƠNG THỨC: getNgayKiemTra() — Lấy ngày kiểm tra. =====
    public Timestamp getNgayKiemTra() {
        return ngayKiemTra;
    }

    // ===== PHƯƠNG THỨC: setNgayKiemTra(Timestamp) — Gán ngày kiểm tra. =====
    public void setNgayKiemTra(Timestamp value) {
        if (value == null || value.after(new Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày kiểm tra không hợp lệ");
        this.ngayKiemTra = value;
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

    // ===== KHỞI TẠO ĐỐI TƯỢNG: PhieuKiemTra(String, PhieuTiepNhan, NhanVien, KhachHang, PhuongTien, Timestamp) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public PhieuKiemTra(String maPKT, PhieuTiepNhan maPTN, NhanVien maNhanVien, KhachHang maKhachHang,
            PhuongTien VIN, Timestamp ngayKiemTra) {

        this.maPKT = maPKT;
        this.maPTN = maPTN;
        this.maNhanVien = maNhanVien;
        this.maKhachHang = maKhachHang;
        this.VIN = VIN;
        this.ngayKiemTra = ngayKiemTra;
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "PhieuKiemTra{" + "maPKT='" + maPKT + '\'' + ", maPTN=" + maPTN + ", maNhanVien=" + maNhanVien
                + ", maKhachHang=" + maKhachHang + ", VIN=" + VIN + ", ngayKiemTra=" + ngayKiemTra + '}';
    }
}
