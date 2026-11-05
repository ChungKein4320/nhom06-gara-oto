package entity;

import java.sql.Timestamp;

// ===== ENTITY: LichBaoHanh =====
public class LichBaoHanh {
    private String maLBH;
    private PhieuSuaChua maPSC;
    private DichVu maDichVu;
    private PhuTung maPhuTung;
    private NhanVien maNhanVien;
    private KhachHang maKhachHang;
    private PhuongTien VIN;
    private Timestamp ngayXuLy;
    private String ketQuaXuLy;
    private String ghiChu;

    // ===== KHỞI TẠO ĐỐI TƯỢNG: LichBaoHanh() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public LichBaoHanh() {
    }

    // ===== PHƯƠNG THỨC: getMaLBH() — Lấy mã lbh. =====
    public String getMaLBH() {
        return maLBH;
    }

    // ===== PHƯƠNG THỨC: setMaLBH(String) — Gán mã lbh. =====
    public void setMaLBH(String value) {
        if (value == null || !value.matches("LBH[0-9]{3}"))
            throw new IllegalArgumentException("Mã lịch bảo hành không hợp lệ");
        this.maLBH = value;
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

    // ===== PHƯƠNG THỨC: getMaDichVu() — Lấy mã dịch vụ. =====
    public DichVu getMaDichVu() {
        return maDichVu;
    }

    // ===== PHƯƠNG THỨC: setMaDichVu(DichVu) — Gán mã dịch vụ. =====
    public void setMaDichVu(DichVu value) {
        if (value != null && (value.getMaDichVu() == null || !value.getMaDichVu().matches("DV[0-9]{3}")))
            throw new IllegalArgumentException("Mã dịch vụ không hợp lệ");
        this.maDichVu = value;
    }

    // ===== PHƯƠNG THỨC: getMaPhuTung() — Lấy mã phụ tùng. =====
    public PhuTung getMaPhuTung() {
        return maPhuTung;
    }

    // ===== PHƯƠNG THỨC: setMaPhuTung(PhuTung) — Gán mã phụ tùng. =====
    public void setMaPhuTung(PhuTung value) {
        if (value != null && (value.getMaPhuTung() == null || !value.getMaPhuTung().matches("PT[0-9]{3}")))
            throw new IllegalArgumentException("Mã phụ tùng không hợp lệ");
        this.maPhuTung = value;
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
            throw new IllegalArgumentException("Mã phương tiện không hợp lệ");
        this.VIN = value;
    }

    // ===== PHƯƠNG THỨC: getNgayXuLy() — Lấy ngày xu ly. =====
    public Timestamp getNgayXuLy() {
        return ngayXuLy;
    }

    // ===== PHƯƠNG THỨC: setNgayXuLy(Timestamp) — Gán ngày xu ly. =====
    public void setNgayXuLy(Timestamp value) {
        if (value == null || value.after(new Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày xử lý không hợp lệ");
        this.ngayXuLy = value;
    }

    // ===== PHƯƠNG THỨC: getKetQuaXuLy() — Lấy ket qua xu ly. =====
    public String getKetQuaXuLy() {
        return ketQuaXuLy;
    }

    // ===== PHƯƠNG THỨC: setKetQuaXuLy(String) — Gán ket qua xu ly. =====
    public void setKetQuaXuLy(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Kết quả xử lý không được rỗng");
        this.ketQuaXuLy = value.trim();
    }

    // ===== PHƯƠNG THỨC: getGhiChu() — Lấy ghi chu. =====
    public String getGhiChu() {
        return ghiChu;
    }

    // ===== PHƯƠNG THỨC: setGhiChu(String) — Gán ghi chu. =====
    public void setGhiChu(String value) {
        this.ghiChu = value;
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: LichBaoHanh(String, PhieuSuaChua, NhanVien, KhachHang, PhuongTien, Timestamp, String, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public LichBaoHanh(String maLBH, PhieuSuaChua maPSC, NhanVien maNhanVien, KhachHang maKhachHang,
            PhuongTien VIN, Timestamp ngayXuLy, String ketQuaXuLy, String ghiChu) {

        this.maLBH = maLBH;
        this.maPSC = maPSC;
        this.maNhanVien = maNhanVien;
        this.maKhachHang = maKhachHang;
        this.VIN = VIN;
        this.ngayXuLy = ngayXuLy;
        this.ketQuaXuLy = ketQuaXuLy;
        this.ghiChu = ghiChu;
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "LichBaoHanh{" + "maLBH='" + maLBH + '\'' + ", maPSC=" + maPSC + ", maDichVu=" + getMaDichVu()
                + ", maPhuTung=" + getMaPhuTung() + ", maNhanVien=" + maNhanVien + ", maKhachHang="
                + maKhachHang + ", VIN=" + VIN + ", ngayXuLy=" + ngayXuLy + ", ketQuaXuLy='" + ketQuaXuLy
                + '\'' + ", ghiChu='" + ghiChu + '\'' + '}';
    }
}
