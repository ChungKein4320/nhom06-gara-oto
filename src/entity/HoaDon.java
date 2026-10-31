package entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
public class HoaDon {
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

    public HoaDon() {
    }

    public HoaDon(String maHoaDon, KhachHang maKhachHang, NhanVien maNhanVien, PhuongTien VIN,
            PhieuSuaChua maPSC, Timestamp ngayLap, BigDecimal tienPhuTung, BigDecimal tienCong,
            BigDecimal dichVuKhac, BigDecimal thueVAT, BigDecimal giamGia,
            PhuongThucThanhToan maPhuongThuc, String trangThaiThanhToan, MaGiamGia maGiamGia) {
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

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = validateMaHoaDon(maHoaDon);
    }

    private static String validateMaHoaDon(String maHoaDon) {
        if (maHoaDon == null || !maHoaDon.matches("HD[0-9]{3}")) {
            throw new IllegalArgumentException("Mã hóa đơn phải có định dạng HD và 3 chữ số");
        }
        return maHoaDon;
    }

    public KhachHang getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(KhachHang maKhachHang) {
        this.maKhachHang = validateMaKhachHang(maKhachHang);
    }

    private static KhachHang validateMaKhachHang(KhachHang maKhachHang) {
        if (maKhachHang == null || maKhachHang.getMaKhachHang() == null
                || maKhachHang.getMaKhachHang().isBlank()) {
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ");
        }
        return maKhachHang;
    }

    public NhanVien getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(NhanVien maNhanVien) {
        this.maNhanVien = validateMaNhanVien(maNhanVien);
    }

    private static NhanVien validateMaNhanVien(NhanVien maNhanVien) {
        if (maNhanVien == null || maNhanVien.getMaNhanVien() == null
                || maNhanVien.getMaNhanVien().isBlank()) {
            throw new IllegalArgumentException("Mã nhân viên không hợp lệ");
        }
        return maNhanVien;
    }

    public PhuongTien getVIN() {
        return VIN;
    }

    public void setVIN(PhuongTien VIN) {
        this.VIN = validateVIN(VIN);
    }

    private static PhuongTien validateVIN(PhuongTien VIN) {
        if (VIN != null && (VIN.getVIN() == null || VIN.getVIN().isBlank())) {
            throw new IllegalArgumentException("Mã phương tiện không hợp lệ");
        }
        return VIN;
    }

    public PhieuSuaChua getMaPSC() {
        return maPSC;
    }

    public void setMaPSC(PhieuSuaChua maPSC) {
        this.maPSC = validateMaPSC(maPSC);
    }

    private static PhieuSuaChua validateMaPSC(PhieuSuaChua maPSC) {
        if (maPSC != null && (maPSC.getMaPSC() == null || maPSC.getMaPSC().isBlank())) {
            throw new IllegalArgumentException("Mã phiếu sửa chữa không hợp lệ");
        }
        return maPSC;
    }

    public Timestamp getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(Timestamp ngayLap) {
        this.ngayLap = validateNgayLap(ngayLap);
    }

    private static Timestamp validateNgayLap(Timestamp ngayLap) {
        if (ngayLap == null || ngayLap.after(new Timestamp(System.currentTimeMillis()))) {
            throw new IllegalArgumentException("Ngày lập không hợp lệ");
        }
        return ngayLap;
    }

    public BigDecimal getTienPhuTung() {
        return tienPhuTung;
    }

    public void setTienPhuTung(BigDecimal tienPhuTung) {
        this.tienPhuTung = validateTienPhuTung(tienPhuTung);
    }

    private static BigDecimal validateTienPhuTung(BigDecimal tienPhuTung) {
        if (tienPhuTung == null || tienPhuTung.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tiền phụ tùng không hợp lệ");
        }
        return tienPhuTung;
    }

    public BigDecimal getTienCong() {
        return tienCong;
    }

    public void setTienCong(BigDecimal tienCong) {
        this.tienCong = validateTienCong(tienCong);
    }

    private static BigDecimal validateTienCong(BigDecimal tienCong) {
        if (tienCong == null || tienCong.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tiền công không hợp lệ");
        }
        return tienCong;
    }

    public BigDecimal getDichVuKhac() {
        return dichVuKhac;
    }

    public void setDichVuKhac(BigDecimal dichVuKhac) {
        this.dichVuKhac = validateDichVuKhac(dichVuKhac);
    }

    private static BigDecimal validateDichVuKhac(BigDecimal dichVuKhac) {
        if (dichVuKhac == null || dichVuKhac.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Chi phí dịch vụ không hợp lệ");
        }
        return dichVuKhac;
    }

    public BigDecimal getThueVAT() {
        return thueVAT;
    }

    public void setThueVAT(BigDecimal thueVAT) {
        this.thueVAT = validateThueVAT(thueVAT);
    }

    private static BigDecimal validateThueVAT(BigDecimal thueVAT) {
        if (thueVAT == null || thueVAT.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Thuế VAT không hợp lệ");
        }
        return thueVAT;
    }

    public BigDecimal getGiamGia() {
        return giamGia;
    }

    public void setGiamGia(BigDecimal giamGia) {
        this.giamGia = validateGiamGia(giamGia, tienPhuTung, tienCong, dichVuKhac, thueVAT);
    }

    private static BigDecimal validateGiamGia(BigDecimal giamGia, BigDecimal tienPhuTung,
            BigDecimal tienCong, BigDecimal dichVuKhac, BigDecimal thueVAT) {
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

    public PhuongThucThanhToan getMaPhuongThuc() {
        return maPhuongThuc;
    }

    public void setMaPhuongThuc(PhuongThucThanhToan maPhuongThuc) {
        this.maPhuongThuc = validateMaPhuongThuc(maPhuongThuc);
    }

    private static PhuongThucThanhToan validateMaPhuongThuc(PhuongThucThanhToan maPhuongThuc) {
        if (maPhuongThuc == null || maPhuongThuc.getMaPhuongThuc() == null
                || maPhuongThuc.getMaPhuongThuc().isBlank()) {
            throw new IllegalArgumentException("Mã phương thức không được rỗng");
        }
        return maPhuongThuc;
    }

    public String getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    public void setTrangThaiThanhToan(String trangThaiThanhToan) {
        this.trangThaiThanhToan = validateTrangThaiThanhToan(trangThaiThanhToan);
    }

    private static String validateTrangThaiThanhToan(String trangThaiThanhToan) {
        if (trangThaiThanhToan == null || trangThaiThanhToan.isBlank()) {
            throw new IllegalArgumentException("Trạng thái thanh toán không được rỗng");
        }
        return trangThaiThanhToan.trim();
    }

    public MaGiamGia getMaGiamGia() {
        return maGiamGia;
    }

    public void setMaGiamGia(MaGiamGia maGiamGia) {
        this.maGiamGia = validateMaGiamGia(maGiamGia);
    }

    private static MaGiamGia validateMaGiamGia(MaGiamGia maGiamGia) {
        if (maGiamGia != null && (maGiamGia.getMaGiamGia() == null || maGiamGia.getMaGiamGia().isBlank())) {
            throw new IllegalArgumentException("Mã giảm giá không hợp lệ");
        }
        return maGiamGia;
    }

    /** Tổng tiền phụ tùng, tiền công, dịch vụ khác và VAT, trừ giảm giá. */
    public BigDecimal tongThanhToan() {
        return (tienPhuTung == null ? BigDecimal.ZERO : tienPhuTung)
                .add(tienCong == null ? BigDecimal.ZERO : tienCong)
                .add(dichVuKhac == null ? BigDecimal.ZERO : dichVuKhac)
                .add(thueVAT == null ? BigDecimal.ZERO : thueVAT)
                .subtract(giamGia == null ? BigDecimal.ZERO : giamGia);
    }

    /** Tính VAT 8% trên tiền phụ tùng, tiền công và dịch vụ khác. */
    public BigDecimal tinhThueVAT() {
        BigDecimal tongTienTruocVAT = (tienPhuTung == null ? BigDecimal.ZERO : tienPhuTung)
                .add(tienCong == null ? BigDecimal.ZERO : tienCong)
                .add(dichVuKhac == null ? BigDecimal.ZERO : dichVuKhac);
        return tongTienTruocVAT.multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);
    }

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
        return tongTienTruocVAT.multiply(mucGiam)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    /** Cập nhật trạng thái thanh toán và trả về trạng thái mới. */
    public String capNhatTrangThai(String trangThaiMoi) {
        setTrangThaiThanhToan(trangThaiMoi);
        return trangThaiThanhToan;
    }

    @Override
    public String toString() {
        return "HoaDon [maHoaDon=" + maHoaDon + ", maKhachHang=" + maKhachHang
                + ", maNhanVien=" + maNhanVien + ", VIN=" + VIN + ", maPSC=" + maPSC
                + ", ngayLap=" + ngayLap + ", tienPhuTung=" + tienPhuTung + ", tienCong=" + tienCong
                + ", dichVuKhac=" + dichVuKhac + ", thueVAT=" + thueVAT + ", giamGia=" + giamGia
                + ", maPhuongThuc=" + maPhuongThuc + ", trangThaiThanhToan=" + trangThaiThanhToan
                + ", maGiamGia=" + maGiamGia + "]";
    }
}
