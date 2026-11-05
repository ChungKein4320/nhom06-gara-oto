package entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;

// ===== ENTITY: ĐỐI TƯỢNG HÓA ĐƠN =====

public class HoaDon {
    // ===== THUỘC TÍNH =====
    private String maHoaDon;
    private KhachHang maKhachHang;
    private NhanVien maNhanVien;
    private PhuongTien VIN;
    private PhieuSuaChua maPSC;
    private Timestamp ngayLap;
    private BigDecimal tienPhuTung;
    private BigDecimal tienCong;
    private BigDecimal dichVuKhac;
    private BigDecimal thueVAT;
    private BigDecimal giamGia;
    private PhuongThucThanhToan maPhuongThuc;
    private String trangThaiThanhToan;
    private MaGiamGia maGiamGia;

    // ===== KHỞI TẠO ĐỐI TƯỢNG =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: HoaDon() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public HoaDon() {
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: HoaDon(String, KhachHang, NhanVien, PhuongTien, PhieuSuaChua, Timestamp, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal, PhuongThucThanhToan, String, MaGiamGia) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public HoaDon(String maHoaDon, KhachHang maKhachHang, NhanVien maNhanVien, PhuongTien VIN,
            PhieuSuaChua maPSC, Timestamp ngayLap, BigDecimal tienPhuTung, BigDecimal tienCong,
            BigDecimal dichVuKhac, BigDecimal thueVAT, BigDecimal giamGia, PhuongThucThanhToan maPhuongThuc,
            String trangThaiThanhToan, MaGiamGia maGiamGia) {
        this.maHoaDon = validateMaHoaDon(maHoaDon);
        this.maKhachHang = validateMaKhachHang(maKhachHang);
        this.maNhanVien = validateMaNhanVien(maNhanVien);
        this.VIN = validateVIN(VIN);
        this.maPSC = validateMaPSC(maPSC);
        this.ngayLap = validateNgayLap(ngayLap);
        this.tienPhuTung = validateTienPhuTung(tienPhuTung);
        this.tienCong = validateTienCong(tienCong);
        this.dichVuKhac = validateDichVuKhac(dichVuKhac);
        this.thueVAT = validateThueVAT(thueVAT);
        this.giamGia = validateGiamGia(giamGia, tienPhuTung, tienCong, dichVuKhac, thueVAT);
        this.maPhuongThuc = validateMaPhuongThuc(maPhuongThuc);
        this.trangThaiThanhToan = validateTrangThaiThanhToan(trangThaiThanhToan);
        this.maGiamGia = validateMaGiamGia(maGiamGia);
    }

    // ===== GETTER VÀ SETTER =====

    // ===== PHƯƠNG THỨC: getMaHoaDon() — Lấy mã hóa đơn. =====
    public String getMaHoaDon() {
        return maHoaDon;
    }

    // ===== PHƯƠNG THỨC: setMaHoaDon(String) — Gán mã hóa đơn. =====
    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = validateMaHoaDon(maHoaDon);
    }

    // ===== PHƯƠNG THỨC: validateMaHoaDon(String) — Kiểm tra tính hợp lệ của mã hóa đơn. =====
    private static String validateMaHoaDon(String maHoaDon) {
        if (maHoaDon == null || !maHoaDon.matches("HD[0-9]{3}")) {
            throw new IllegalArgumentException("Mã hóa đơn phải có định dạng HD và 3 chữ số");
        }
        return maHoaDon;
    }

    // ===== PHƯƠNG THỨC: getMaKhachHang() — Lấy mã khách hàng. =====
    public KhachHang getMaKhachHang() {
        return maKhachHang;
    }

    // ===== PHƯƠNG THỨC: setMaKhachHang(KhachHang) — Gán mã khách hàng. =====
    public void setMaKhachHang(KhachHang maKhachHang) {
        this.maKhachHang = validateMaKhachHang(maKhachHang);
    }

    // ===== PHƯƠNG THỨC: validateMaKhachHang(KhachHang) — Kiểm tra tính hợp lệ của mã khách hàng. =====
    private static KhachHang validateMaKhachHang(KhachHang maKhachHang) {
        if (maKhachHang == null || maKhachHang.getMaKhachHang() == null
                || maKhachHang.getMaKhachHang().isBlank()) {
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ");
        }
        return maKhachHang;
    }

    // ===== PHƯƠNG THỨC: getMaNhanVien() — Lấy mã nhân viên. =====
    public NhanVien getMaNhanVien() {
        return maNhanVien;
    }

    // ===== PHƯƠNG THỨC: setMaNhanVien(NhanVien) — Gán mã nhân viên. =====
    public void setMaNhanVien(NhanVien maNhanVien) {
        this.maNhanVien = validateMaNhanVien(maNhanVien);
    }

    // ===== PHƯƠNG THỨC: validateMaNhanVien(NhanVien) — Kiểm tra tính hợp lệ của mã nhân viên. =====
    private static NhanVien validateMaNhanVien(NhanVien maNhanVien) {
        if (maNhanVien == null || maNhanVien.getMaNhanVien() == null
                || maNhanVien.getMaNhanVien().isBlank()) {
            throw new IllegalArgumentException("Mã nhân viên không hợp lệ");
        }
        return maNhanVien;
    }

    // ===== PHƯƠNG THỨC: getVIN() — Lấy vin. =====
    public PhuongTien getVIN() {
        return VIN;
    }

    // ===== PHƯƠNG THỨC: setVIN(PhuongTien) — Gán vin. =====
    public void setVIN(PhuongTien VIN) {
        this.VIN = validateVIN(VIN);
    }

    // ===== PHƯƠNG THỨC: validateVIN(PhuongTien) — Kiểm tra tính hợp lệ của vin. =====
    private static PhuongTien validateVIN(PhuongTien VIN) {
        if (VIN != null && (VIN.getVIN() == null || VIN.getVIN().isBlank())) {
            throw new IllegalArgumentException("Mã phương tiện không hợp lệ");
        }
        return VIN;
    }

    // ===== PHƯƠNG THỨC: getMaPSC() — Lấy mã psc. =====
    public PhieuSuaChua getMaPSC() {
        return maPSC;
    }

    // ===== PHƯƠNG THỨC: setMaPSC(PhieuSuaChua) — Gán mã psc. =====
    public void setMaPSC(PhieuSuaChua maPSC) {
        this.maPSC = validateMaPSC(maPSC);
    }

    // ===== PHƯƠNG THỨC: validateMaPSC(PhieuSuaChua) — Kiểm tra tính hợp lệ của mã psc. =====
    private static PhieuSuaChua validateMaPSC(PhieuSuaChua maPSC) {
        if (maPSC != null && (maPSC.getMaPSC() == null || maPSC.getMaPSC().isBlank())) {
            throw new IllegalArgumentException("Mã phiếu sửa chữa không hợp lệ");
        }
        return maPSC;
    }

    // ===== PHƯƠNG THỨC: getNgayLap() — Lấy ngày lap. =====
    public Timestamp getNgayLap() {
        return ngayLap;
    }

    // ===== PHƯƠNG THỨC: setNgayLap(Timestamp) — Gán ngày lap. =====
    public void setNgayLap(Timestamp ngayLap) {
        this.ngayLap = validateNgayLap(ngayLap);
    }

    // ===== PHƯƠNG THỨC: validateNgayLap(Timestamp) — Kiểm tra tính hợp lệ của ngày lap. =====
    private static Timestamp validateNgayLap(Timestamp ngayLap) {
        if (ngayLap == null || ngayLap.after(new Timestamp(System.currentTimeMillis()))) {
            throw new IllegalArgumentException("Ngày lập không hợp lệ");
        }
        return ngayLap;
    }

    // ===== PHƯƠNG THỨC: getTienPhuTung() — Lấy tiền phụ tùng. =====
    public BigDecimal getTienPhuTung() {
        return tienPhuTung;
    }

    // ===== PHƯƠNG THỨC: setTienPhuTung(BigDecimal) — Gán tiền phụ tùng. =====
    public void setTienPhuTung(BigDecimal tienPhuTung) {
        this.tienPhuTung = validateTienPhuTung(tienPhuTung);
    }

    // ===== PHƯƠNG THỨC: validateTienPhuTung(BigDecimal) — Kiểm tra tính hợp lệ của tiền phụ tùng. =====
    private static BigDecimal validateTienPhuTung(BigDecimal tienPhuTung) {
        if (tienPhuTung == null || tienPhuTung.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tiền phụ tùng không hợp lệ");
        }
        return tienPhuTung;
    }

    // ===== PHƯƠNG THỨC: getTienCong() — Lấy tiền cong. =====
    public BigDecimal getTienCong() {
        return tienCong;
    }

    // ===== PHƯƠNG THỨC: setTienCong(BigDecimal) — Gán tiền cong. =====
    public void setTienCong(BigDecimal tienCong) {
        this.tienCong = validateTienCong(tienCong);
    }

    // ===== PHƯƠNG THỨC: validateTienCong(BigDecimal) — Kiểm tra tính hợp lệ của tiền cong. =====
    private static BigDecimal validateTienCong(BigDecimal tienCong) {
        if (tienCong == null || tienCong.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tiền công không hợp lệ");
        }
        return tienCong;
    }

    // ===== PHƯƠNG THỨC: getDichVuKhac() — Lấy dịch vụ khac. =====
    public BigDecimal getDichVuKhac() {
        return dichVuKhac;
    }

    // ===== PHƯƠNG THỨC: setDichVuKhac(BigDecimal) — Gán dịch vụ khac. =====
    public void setDichVuKhac(BigDecimal dichVuKhac) {
        this.dichVuKhac = validateDichVuKhac(dichVuKhac);
    }

    // ===== PHƯƠNG THỨC: validateDichVuKhac(BigDecimal) — Kiểm tra tính hợp lệ của dịch vụ khac. =====
    private static BigDecimal validateDichVuKhac(BigDecimal dichVuKhac) {
        if (dichVuKhac == null || dichVuKhac.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Chi phí dịch vụ không hợp lệ");
        }
        return dichVuKhac;
    }

    // ===== PHƯƠNG THỨC: getThueVAT() — Lấy thue vat. =====
    public BigDecimal getThueVAT() {
        return thueVAT;
    }

    // ===== PHƯƠNG THỨC: setThueVAT(BigDecimal) — Gán thue vat. =====
    public void setThueVAT(BigDecimal thueVAT) {
        this.thueVAT = validateThueVAT(thueVAT);
    }

    // ===== PHƯƠNG THỨC: validateThueVAT(BigDecimal) — Kiểm tra tính hợp lệ của thue vat. =====
    private static BigDecimal validateThueVAT(BigDecimal thueVAT) {
        if (thueVAT == null || thueVAT.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Thuế VAT không hợp lệ");
        }
        return thueVAT;
    }

    // ===== PHƯƠNG THỨC: getGiamGia() — Lấy giảm giá. =====
    public BigDecimal getGiamGia() {
        return giamGia;
    }

    // ===== PHƯƠNG THỨC: setGiamGia(BigDecimal) — Gán giảm giá. =====
    public void setGiamGia(BigDecimal giamGia) {
        this.giamGia = validateGiamGia(giamGia, tienPhuTung, tienCong, dichVuKhac, thueVAT);
    }

    // ===== PHƯƠNG THỨC: validateGiamGia(BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal) — Kiểm tra tính hợp lệ của giảm giá. =====
    private static BigDecimal validateGiamGia(BigDecimal giamGia, BigDecimal tienPhuTung, BigDecimal tienCong,
            BigDecimal dichVuKhac, BigDecimal thueVAT) {
        if (giamGia == null) {
            return null;
        }
        BigDecimal tongTruocGiam = (tienPhuTung == null ? BigDecimal.ZERO : tienPhuTung)
                .add(tienCong == null ? BigDecimal.ZERO : tienCong)
                .add(dichVuKhac == null ? BigDecimal.ZERO : dichVuKhac)
                .add(thueVAT == null ? BigDecimal.ZERO : thueVAT);
        if (giamGia.compareTo(BigDecimal.ZERO) < 0 || giamGia.compareTo(tongTruocGiam) > 0) {
            throw new IllegalArgumentException("Mức giảm giá không hợp lệ");
        }
        return giamGia;
    }

    // ===== PHƯƠNG THỨC: getMaPhuongThuc() — Lấy mã phương thuc. =====
    public PhuongThucThanhToan getMaPhuongThuc() {
        return maPhuongThuc;
    }

    // ===== PHƯƠNG THỨC: setMaPhuongThuc(PhuongThucThanhToan) — Gán mã phương thuc. =====
    public void setMaPhuongThuc(PhuongThucThanhToan maPhuongThuc) {
        this.maPhuongThuc = validateMaPhuongThuc(maPhuongThuc);
    }

    // ===== PHƯƠNG THỨC: validateMaPhuongThuc(PhuongThucThanhToan) — Kiểm tra tính hợp lệ của mã phương thuc. =====
    private static PhuongThucThanhToan validateMaPhuongThuc(PhuongThucThanhToan maPhuongThuc) {
        if (maPhuongThuc == null || maPhuongThuc.getMaPhuongThuc() == null
                || maPhuongThuc.getMaPhuongThuc().isBlank()) {
            throw new IllegalArgumentException("Mã phương thức không được rỗng");
        }
        return maPhuongThuc;
    }

    // ===== PHƯƠNG THỨC: getTrangThaiThanhToan() — Lấy trạng thái thành toan. =====
    public String getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    // ===== PHƯƠNG THỨC: setTrangThaiThanhToan(String) — Gán trạng thái thành toan. =====
    public void setTrangThaiThanhToan(String trangThaiThanhToan) {
        this.trangThaiThanhToan = validateTrangThaiThanhToan(trangThaiThanhToan);
    }

    // ===== PHƯƠNG THỨC: validateTrangThaiThanhToan(String) — Kiểm tra tính hợp lệ của trạng thái thành toan. =====
    private static String validateTrangThaiThanhToan(String trangThaiThanhToan) {
        if (trangThaiThanhToan == null || trangThaiThanhToan.isBlank()) {
            throw new IllegalArgumentException("Trạng thái thanh toán không được rỗng");
        }
        return trangThaiThanhToan.trim();
    }

    // ===== PHƯƠNG THỨC: getMaGiamGia() — Lấy mã giảm giá. =====
    public MaGiamGia getMaGiamGia() {
        return maGiamGia;
    }

    // ===== PHƯƠNG THỨC: setMaGiamGia(MaGiamGia) — Gán mã giảm giá. =====
    public void setMaGiamGia(MaGiamGia maGiamGia) {
        this.maGiamGia = validateMaGiamGia(maGiamGia);
    }

    // ===== PHƯƠNG THỨC: validateMaGiamGia(MaGiamGia) — Kiểm tra tính hợp lệ của mã giảm giá. =====
    private static MaGiamGia validateMaGiamGia(MaGiamGia maGiamGia) {
        if (maGiamGia != null && (maGiamGia.getMaGiamGia() == null || maGiamGia.getMaGiamGia().isBlank())) {
            throw new IllegalArgumentException("Mã giảm giá không hợp lệ");
        }
        return maGiamGia;
    }

    // ===== PHƯƠNG THỨC: tongThanhToan() — Chuyển đổi thành thành toan. =====
    /** Tổng tiền phụ tùng, tiền công, dịch vụ khác và VAT, trừ giảm giá. */
    public BigDecimal tongThanhToan() {
        return (tienPhuTung == null ? BigDecimal.ZERO : tienPhuTung)
                .add(tienCong == null ? BigDecimal.ZERO : tienCong)
                .add(dichVuKhac == null ? BigDecimal.ZERO : dichVuKhac)
                .add(thueVAT == null ? BigDecimal.ZERO : thueVAT)
                .subtract(giamGia == null ? BigDecimal.ZERO : giamGia);
    }

    // ===== PHƯƠNG THỨC: tinhThueVAT() — Tính thue vat. =====
    /** Tính VAT 8% trên tiền phụ tùng, tiền công và dịch vụ khác. */
    public BigDecimal tinhThueVAT() {
        BigDecimal tongTienTruocVAT = (tienPhuTung == null ? BigDecimal.ZERO : tienPhuTung)
                .add(tienCong == null ? BigDecimal.ZERO : tienCong)
                .add(dichVuKhac == null ? BigDecimal.ZERO : dichVuKhac);
        return tongTienTruocVAT.multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);
    }

    // ===== PHƯƠNG THỨC: tinhGiamGia() — Tính giảm giá. =====
    /** Tính giảm giá theo phần trăm của mã giảm giá, trước VAT. */
    public BigDecimal tinhGiamGia() {
        if (maGiamGia == null) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal mucGiam = maGiamGia.getMucGiam();
        if (mucGiam == null || mucGiam.compareTo(BigDecimal.ZERO) < 0
                || mucGiam.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalStateException("Mức giảm của mã giảm giá không hợp lệ");
        }
        BigDecimal tongTienTruocVAT = (tienPhuTung == null ? BigDecimal.ZERO : tienPhuTung)
                .add(tienCong == null ? BigDecimal.ZERO : tienCong)
                .add(dichVuKhac == null ? BigDecimal.ZERO : dichVuKhac);
        return tongTienTruocVAT.multiply(mucGiam).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    // ===== PHƯƠNG THỨC: capNhatTrangThai(String) — Cập nhật nhat trạng thái. =====
    /** Cập nhật trạng thái thanh toán và trả về trạng thái mới. */
    public String capNhatTrangThai(String trangThaiMoi) {
        setTrangThaiThanhToan(trangThaiMoi);
        return trangThaiThanhToan;
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "HoaDon [maHoaDon=" + maHoaDon + ", maKhachHang=" + maKhachHang + ", maNhanVien=" + maNhanVien
                + ", VIN=" + VIN + ", maPSC=" + maPSC + ", ngayLap=" + ngayLap + ", tienPhuTung="
                + tienPhuTung + ", tienCong=" + tienCong + ", dichVuKhac=" + dichVuKhac + ", thueVAT="
                + thueVAT + ", giamGia=" + giamGia + ", maPhuongThuc=" + maPhuongThuc
                + ", trangThaiThanhToan=" + trangThaiThanhToan + ", maGiamGia=" + maGiamGia + "]";
    }
}
