package entity;

import java.sql.Timestamp;

// ===== ENTITY: CHI TIẾT PHIẾU SỬA CHỮA =====

public class ChiTietPhieuSuaChua {
    // ===== THUỘC TÍNH =====
    private PhieuSuaChua maPSC;
    private DichVu maDichVu;
    private PhuTung maPhuTung;
    private String trangThaiHangMuc;
    private Integer soLuongPhuTung;
    private Integer soLuongCapPhat;
    private Integer soLuongSuDung;
    private Integer soLuongHoanTra;
    private Timestamp ngayCapPhat;
    private NhanVien maNhanVienCapPhat;
    private Timestamp ngayHoanTra;
    private NhanVien maNhanVienXacNhanHoanTra;
    private String ketQuaNghiemThu;
    private Timestamp ngayBatDauBaoHanh;
    private Integer thoiHanBaoHanh;
    private Timestamp ngayHetHanBaoHanh;

    // ===== KHỞI TẠO ĐỐI TƯỢNG =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ChiTietPhieuSuaChua() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public ChiTietPhieuSuaChua() {
    }

    // ===== GETTER VÀ SETTER =====

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

    // ===== PHƯƠNG THỨC: getTrangThaiHangMuc() — Lấy trạng thái hàng muc. =====
    public String getTrangThaiHangMuc() {
        return trangThaiHangMuc;
    }

    // ===== PHƯƠNG THỨC: setTrangThaiHangMuc(String) — Gán trạng thái hàng muc. =====
    public void setTrangThaiHangMuc(String value) {
        if (value == null || !java.util.Set
                .of("Chờ sửa chữa", "Đang sửa chữa", "Thiếu phụ tùng", "Hoàn thành").contains(value))
            throw new IllegalArgumentException("Trạng thái hạng mục không hợp lệ");
        this.trangThaiHangMuc = value;
    }

    // ===== PHƯƠNG THỨC: getSoLuongPhuTung() — Lấy số lượng phụ tùng. =====
    public Integer getSoLuongPhuTung() {
        return soLuongPhuTung;
    }

    // ===== PHƯƠNG THỨC: setSoLuongPhuTung(Integer) — Gán số lượng phụ tùng. =====
    public void setSoLuongPhuTung(Integer value) {
        if (value != null && value < 0)
            throw new IllegalArgumentException("Số lượng phụ tùng không hợp lệ");
        if (value != null && maPhuTung != null && !"PT000".equals(maPhuTung.getMaPhuTung()) && value == 0)
            throw new IllegalArgumentException("Số lượng phụ tùng phải lớn hơn 0");
        if (value != null && soLuongCapPhat != null && soLuongCapPhat > value)
            throw new IllegalArgumentException("Số lượng cấp phát không được vượt số lượng phụ tùng");
        this.soLuongPhuTung = value;
    }

    // ===== PHƯƠNG THỨC: getSoLuongCapPhat() — Lấy số lượng cấp phat. =====
    public Integer getSoLuongCapPhat() {
        return soLuongCapPhat;
    }

    // ===== PHƯƠNG THỨC: setSoLuongCapPhat(Integer) — Gán số lượng cấp phat. =====
    public void setSoLuongCapPhat(Integer value) {
        if (value != null && (value < 0 || (soLuongPhuTung != null && value > soLuongPhuTung)))
            throw new IllegalArgumentException("Số lượng cấp phát không hợp lệ");
        if (value != null && soLuongSuDung != null && soLuongSuDung > value)
            throw new IllegalArgumentException("Số lượng sử dụng không được vượt số lượng cấp phát");
        if (value != null && soLuongHoanTra != null && soLuongSuDung != null
                && soLuongSuDung + soLuongHoanTra > value)
            throw new IllegalArgumentException("Tổng số lượng sử dụng và hoàn trả vượt số lượng cấp phát");
        this.soLuongCapPhat = value;
    }

    // ===== PHƯƠNG THỨC: getSoLuongSuDung() — Lấy số lượng su dung. =====
    public Integer getSoLuongSuDung() {
        return soLuongSuDung;
    }

    // ===== PHƯƠNG THỨC: setSoLuongSuDung(Integer) — Gán số lượng su dung. =====
    public void setSoLuongSuDung(Integer value) {
        if (value != null && (value < 0 || (soLuongCapPhat != null && value > soLuongCapPhat)))
            throw new IllegalArgumentException("Số lượng sử dụng không hợp lệ");
        if (value != null && soLuongHoanTra != null && soLuongCapPhat != null
                && value + soLuongHoanTra > soLuongCapPhat)
            throw new IllegalArgumentException("Tổng số lượng sử dụng và hoàn trả vượt số lượng cấp phát");
        this.soLuongSuDung = value;
    }

    // ===== PHƯƠNG THỨC: getSoLuongHoanTra() — Lấy số lượng hoan tra. =====
    public Integer getSoLuongHoanTra() {
        return soLuongHoanTra;
    }

    // ===== PHƯƠNG THỨC: setSoLuongHoanTra(Integer) — Gán số lượng hoan tra. =====
    public void setSoLuongHoanTra(Integer value) {
        if (value != null && (value < 0 || (soLuongCapPhat != null && value > soLuongCapPhat)))
            throw new IllegalArgumentException("Số lượng hoàn trả không hợp lệ");
        if (value != null && soLuongSuDung != null && soLuongCapPhat != null
                && value + soLuongSuDung > soLuongCapPhat)
            throw new IllegalArgumentException("Tổng số lượng sử dụng và hoàn trả vượt số lượng cấp phát");
        this.soLuongHoanTra = value;
    }

    // ===== PHƯƠNG THỨC: getNgayCapPhat() — Lấy ngày cấp phat. =====
    public Timestamp getNgayCapPhat() {
        return ngayCapPhat;
    }

    // ===== PHƯƠNG THỨC: setNgayCapPhat(Timestamp) — Gán ngày cấp phat. =====
    public void setNgayCapPhat(Timestamp value) {
        if (value != null && ngayHoanTra != null && ngayHoanTra.before(value))
            throw new IllegalArgumentException("Ngày cấp phát phải trước hoặc bằng ngày hoàn trả");
        this.ngayCapPhat = value;
    }

    // ===== PHƯƠNG THỨC: getMaNhanVienCapPhat() — Lấy mã nhân viên cấp phat. =====
    public NhanVien getMaNhanVienCapPhat() {
        return maNhanVienCapPhat;
    }

    // ===== PHƯƠNG THỨC: setMaNhanVienCapPhat(NhanVien) — Gán mã nhân viên cấp phat. =====
    public void setMaNhanVienCapPhat(NhanVien value) {
        if (value != null && (value.getMaNhanVien() == null || !value.getMaNhanVien().matches("NV[0-9]{3}")))
            throw new IllegalArgumentException("Mã nhân viên cấp phát không hợp lệ");
        this.maNhanVienCapPhat = value;
    }

    // ===== PHƯƠNG THỨC: getNgayHoanTra() — Lấy ngày hoan tra. =====
    public Timestamp getNgayHoanTra() {
        return ngayHoanTra;
    }

    // ===== PHƯƠNG THỨC: setNgayHoanTra(Timestamp) — Gán ngày hoan tra. =====
    public void setNgayHoanTra(Timestamp value) {
        if (value != null && ngayCapPhat != null && value.before(ngayCapPhat))
            throw new IllegalArgumentException("Ngày hoàn trả không được trước ngày cấp phát");
        this.ngayHoanTra = value;
    }

    // ===== PHƯƠNG THỨC: getMaNhanVienXacNhanHoanTra() — Lấy mã nhân viên xac nhân hoan tra. =====
    public NhanVien getMaNhanVienXacNhanHoanTra() {
        return maNhanVienXacNhanHoanTra;
    }

    // ===== PHƯƠNG THỨC: setMaNhanVienXacNhanHoanTra(NhanVien) — Gán mã nhân viên xac nhân hoan tra. =====
    public void setMaNhanVienXacNhanHoanTra(NhanVien value) {
        if (value != null && (value.getMaNhanVien() == null || !value.getMaNhanVien().matches("NV[0-9]{3}")))
            throw new IllegalArgumentException("Mã nhân viên xác nhận hoàn trả không hợp lệ");
        this.maNhanVienXacNhanHoanTra = value;
    }

    // ===== PHƯƠNG THỨC: getKetQuaNghiemThu() — Lấy ket qua nghiem thu. =====
    public String getKetQuaNghiemThu() {
        return ketQuaNghiemThu;
    }

    // ===== PHƯƠNG THỨC: setKetQuaNghiemThu(String) — Gán ket qua nghiem thu. =====
    public void setKetQuaNghiemThu(String value) {
        if (value != null && !java.util.Set.of("Đạt", "Không đạt").contains(value))
            throw new IllegalArgumentException("Kết quả nghiệm thu không hợp lệ");
        this.ketQuaNghiemThu = value;
    }

    // ===== PHƯƠNG THỨC: getNgayBatDauBaoHanh() — Lấy ngày bắt dau báo hành. =====
    public Timestamp getNgayBatDauBaoHanh() {
        return ngayBatDauBaoHanh;
    }

    // ===== PHƯƠNG THỨC: setNgayBatDauBaoHanh(Timestamp) — Gán ngày bắt dau báo hành. =====
    public void setNgayBatDauBaoHanh(Timestamp value) {
        if (value != null && ngayHetHanBaoHanh != null && ngayHetHanBaoHanh.before(value))
            throw new IllegalArgumentException("Ngày bắt đầu bảo hành phải trước hoặc bằng ngày hết hạn");
        this.ngayBatDauBaoHanh = value;
    }

    // ===== PHƯƠNG THỨC: getThoiHanBaoHanh() — Lấy thoi han báo hành. =====
    public Integer getThoiHanBaoHanh() {
        return thoiHanBaoHanh;
    }

    // ===== PHƯƠNG THỨC: setThoiHanBaoHanh(Integer) — Gán thoi han báo hành. =====
    public void setThoiHanBaoHanh(Integer value) {
        if (value != null && value < 0)
            throw new IllegalArgumentException("Thời hạn bảo hành không được âm");
        this.thoiHanBaoHanh = value;
    }

    // ===== PHƯƠNG THỨC: getNgayHetHanBaoHanh() — Lấy ngày het han báo hành. =====
    public Timestamp getNgayHetHanBaoHanh() {
        return ngayHetHanBaoHanh;
    }

    // ===== PHƯƠNG THỨC: setNgayHetHanBaoHanh(Timestamp) — Gán ngày het han báo hành. =====
    public void setNgayHetHanBaoHanh(Timestamp value) {
        if (value != null && ngayBatDauBaoHanh != null && value.before(ngayBatDauBaoHanh))
            throw new IllegalArgumentException("Ngày hết hạn bảo hành không được trước ngày bắt đầu");
        this.ngayHetHanBaoHanh = value;
    }

    // ===== PHƯƠNG THỨC: capNhatTrangThai(String) — Cập nhật nhat trạng thái. =====
    public void capNhatTrangThai(String trangThaiMoi) {
        setTrangThaiHangMuc(trangThaiMoi);
    }

    // ===== PHƯƠNG THỨC: kiemTraBaoHanh() — Kiểm tra báo hành. =====
    public boolean kiemTraBaoHanh() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return ngayBatDauBaoHanh != null && ngayHetHanBaoHanh != null && !now.before(ngayBatDauBaoHanh)
                && !now.after(ngayHetHanBaoHanh);
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ChiTietPhieuSuaChua(PhieuSuaChua, DichVu, PhuTung, String, Integer, String, Timestamp, Integer, Timestamp) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public ChiTietPhieuSuaChua(PhieuSuaChua maPSC, DichVu maDichVu, PhuTung maPhuTung,
            String trangThaiHangMuc, Integer soLuongPhuTung, String ketQuaNghiemThu,
            Timestamp ngayBatDauBaoHanh, Integer thoiHanBaoHanh, Timestamp ngayHetHanBaoHanh) {

        this.maPSC = maPSC;
        this.maDichVu = maDichVu;
        this.maPhuTung = maPhuTung;
        this.trangThaiHangMuc = trangThaiHangMuc;
        this.soLuongPhuTung = soLuongPhuTung;
        this.ketQuaNghiemThu = ketQuaNghiemThu;
        this.thoiHanBaoHanh = thoiHanBaoHanh;
        this.ngayBatDauBaoHanh = ngayBatDauBaoHanh;
        this.ngayHetHanBaoHanh = ngayHetHanBaoHanh;
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "ChiTietPhieuSuaChua{" + "maPSC=" + maPSC + ", maDichVu=" + maDichVu + ", maPhuTung="
                + maPhuTung + ", trangThaiHangMuc='" + trangThaiHangMuc + '\'' + ", soLuongPhuTung="
                + soLuongPhuTung + ", soLuongCapPhat=" + soLuongCapPhat + ", soLuongSuDung=" + soLuongSuDung
                + ", soLuongHoanTra=" + soLuongHoanTra + ", ngayCapPhat=" + ngayCapPhat
                + ", maNhanVienCapPhat='" + maNhanVienCapPhat + '\'' + ", ngayHoanTra=" + ngayHoanTra
                + ", maNhanVienXacNhanHoanTra='" + maNhanVienXacNhanHoanTra + '\'' + ", ketQuaNghiemThu='"
                + ketQuaNghiemThu + '\'' + ", ngayBatDauBaoHanh=" + ngayBatDauBaoHanh + ", thoiHanBaoHanh="
                + thoiHanBaoHanh + ", ngayHetHanBaoHanh=" + ngayHetHanBaoHanh + '}';
    }
}
