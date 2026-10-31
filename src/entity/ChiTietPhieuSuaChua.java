package entity;

import java.sql.Timestamp;

public class ChiTietPhieuSuaChua {
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

    public ChiTietPhieuSuaChua() { }

    public PhieuSuaChua getMaPSC() {
        return maPSC;
    }

    public void setMaPSC(PhieuSuaChua value) {
        if (value == null || value.getMaPSC() == null || !value.getMaPSC().matches("PSC[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu sửa chữa không hợp lệ");
        this.maPSC = value;
    }

    public DichVu getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(DichVu  value) {
        if (value != null && (value.getMaDichVu() == null || !value.getMaDichVu().matches("DV[0-9]{3}")))
            throw new IllegalArgumentException("Mã dịch vụ không hợp lệ");
        this.maDichVu = value;
    }

    public PhuTung getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(PhuTung value) {
        if (value != null && (value.getMaPhuTung() == null || !value.getMaPhuTung().matches("PT[0-9]{3}")))
            throw new IllegalArgumentException("Mã phụ tùng không hợp lệ");
        this.maPhuTung = value;
    }

    public String getTrangThaiHangMuc() {
        return trangThaiHangMuc;
    }

    public void setTrangThaiHangMuc(String value) {
        if (value == null || !java.util.Set.of("Chờ sửa chữa", "Đang sửa chữa", "Thiếu phụ tùng", "Hoàn thành").contains(value))
            throw new IllegalArgumentException("Trạng thái hạng mục không hợp lệ");
        this.trangThaiHangMuc = value;
    }

    public Integer getSoLuongPhuTung() {
        return soLuongPhuTung;
    }

    public void setSoLuongPhuTung(Integer value) {
        if (value != null && value < 0)
            throw new IllegalArgumentException("Số lượng phụ tùng không hợp lệ");
        if (value != null && maPhuTung != null && !"PT000".equals(maPhuTung.getMaPhuTung()) && value == 0)
            throw new IllegalArgumentException("Số lượng phụ tùng phải lớn hơn 0");
        if (value != null && soLuongCapPhat != null && soLuongCapPhat > value)
            throw new IllegalArgumentException("Số lượng cấp phát không được vượt số lượng phụ tùng");
        this.soLuongPhuTung = value;
    }

    public Integer getSoLuongCapPhat() { return soLuongCapPhat; }
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
    public Integer getSoLuongSuDung() { return soLuongSuDung; }
    public void setSoLuongSuDung(Integer value) {
        if (value != null && (value < 0 || (soLuongCapPhat != null && value > soLuongCapPhat)))
            throw new IllegalArgumentException("Số lượng sử dụng không hợp lệ");
        if (value != null && soLuongHoanTra != null && soLuongCapPhat != null
                && value + soLuongHoanTra > soLuongCapPhat)
            throw new IllegalArgumentException("Tổng số lượng sử dụng và hoàn trả vượt số lượng cấp phát");
        this.soLuongSuDung = value;
    }
    public Integer getSoLuongHoanTra() { return soLuongHoanTra; }
    public void setSoLuongHoanTra(Integer value) {
        if (value != null && (value < 0 || (soLuongCapPhat != null && value > soLuongCapPhat)))
            throw new IllegalArgumentException("Số lượng hoàn trả không hợp lệ");
        if (value != null && soLuongSuDung != null && soLuongCapPhat != null
                && value + soLuongSuDung > soLuongCapPhat)
            throw new IllegalArgumentException("Tổng số lượng sử dụng và hoàn trả vượt số lượng cấp phát");
        this.soLuongHoanTra = value;
    }
    public Timestamp getNgayCapPhat() { return ngayCapPhat; }
    public void setNgayCapPhat(Timestamp value) {
        if (value != null && ngayHoanTra != null && ngayHoanTra.before(value))
            throw new IllegalArgumentException("Ngày cấp phát phải trước hoặc bằng ngày hoàn trả");
        this.ngayCapPhat = value;
    }
    public NhanVien getMaNhanVienCapPhat() { return maNhanVienCapPhat; }
    public void setMaNhanVienCapPhat(NhanVien value) {
        if (value != null && (value.getMaNhanVien() == null || !value.getMaNhanVien().matches("NV[0-9]{3}")))
            throw new IllegalArgumentException("Mã nhân viên cấp phát không hợp lệ");
        this.maNhanVienCapPhat = value;
    }
    public Timestamp getNgayHoanTra() { return ngayHoanTra; }
    public void setNgayHoanTra(Timestamp value) {
        if (value != null && ngayCapPhat != null && value.before(ngayCapPhat))
            throw new IllegalArgumentException("Ngày hoàn trả không được trước ngày cấp phát");
        this.ngayHoanTra = value;
    }
    public NhanVien getMaNhanVienXacNhanHoanTra() { return maNhanVienXacNhanHoanTra; }
    public void setMaNhanVienXacNhanHoanTra(NhanVien value) {
        if (value != null && (value.getMaNhanVien() == null || !value.getMaNhanVien().matches("NV[0-9]{3}")))
            throw new IllegalArgumentException("Mã nhân viên xác nhận hoàn trả không hợp lệ");
        this.maNhanVienXacNhanHoanTra = value;
    }

    public String getKetQuaNghiemThu() {
        return ketQuaNghiemThu;
    }

    public void setKetQuaNghiemThu(String value) {
        if (value != null && !java.util.Set.of("Đạt", "Không đạt").contains(value))
            throw new IllegalArgumentException("Kết quả nghiệm thu không hợp lệ");
        this.ketQuaNghiemThu = value;
    }

    public Timestamp getNgayBatDauBaoHanh() {
        return ngayBatDauBaoHanh;
    }

    public void setNgayBatDauBaoHanh(Timestamp value) {
        if (value != null && ngayHetHanBaoHanh != null && ngayHetHanBaoHanh.before(value))
            throw new IllegalArgumentException("Ngày bắt đầu bảo hành phải trước hoặc bằng ngày hết hạn");
        this.ngayBatDauBaoHanh = value;
    }

    public Integer getThoiHanBaoHanh() {
        return thoiHanBaoHanh;
    }

    public void setThoiHanBaoHanh(Integer value) {
        if (value != null && value < 0)
            throw new IllegalArgumentException("Thời hạn bảo hành không được âm");
        this.thoiHanBaoHanh = value;
    }

    public Timestamp getNgayHetHanBaoHanh() {
        return ngayHetHanBaoHanh;
    }

    public void setNgayHetHanBaoHanh(Timestamp value) {
        if (value != null && ngayBatDauBaoHanh != null && value.before(ngayBatDauBaoHanh))
            throw new IllegalArgumentException("Ngày hết hạn bảo hành không được trước ngày bắt đầu");
        this.ngayHetHanBaoHanh = value;
    }

    public void capNhatTrangThai(String trangThaiMoi) {
        setTrangThaiHangMuc(trangThaiMoi);
    }

    public boolean kiemTraBaoHanh() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return ngayBatDauBaoHanh != null && ngayHetHanBaoHanh != null
                && !now.before(ngayBatDauBaoHanh) && !now.after(ngayHetHanBaoHanh);
    }


    public ChiTietPhieuSuaChua(
            PhieuSuaChua maPSC,
            DichVu maDichVu,
            PhuTung maPhuTung,
            String trangThaiHangMuc,
            Integer soLuongPhuTung,
            String ketQuaNghiemThu,
            Timestamp ngayBatDauBaoHanh,
            Integer thoiHanBaoHanh,
            Timestamp ngayHetHanBaoHanh) {

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


    @Override
    public String toString() {
        return "ChiTietPhieuSuaChua{"
                + "maPSC=" + maPSC
                + ", maDichVu=" + maDichVu
                + ", maPhuTung=" + maPhuTung
                + ", trangThaiHangMuc='" + trangThaiHangMuc + '\''
                + ", soLuongPhuTung=" + soLuongPhuTung
                + ", soLuongCapPhat=" + soLuongCapPhat
                + ", soLuongSuDung=" + soLuongSuDung
                + ", soLuongHoanTra=" + soLuongHoanTra
                + ", ngayCapPhat=" + ngayCapPhat
                + ", maNhanVienCapPhat='" + maNhanVienCapPhat + '\''
                + ", ngayHoanTra=" + ngayHoanTra
                + ", maNhanVienXacNhanHoanTra='"
                + maNhanVienXacNhanHoanTra + '\''
                + ", ketQuaNghiemThu='" + ketQuaNghiemThu + '\''
                + ", ngayBatDauBaoHanh=" + ngayBatDauBaoHanh
                + ", thoiHanBaoHanh=" + thoiHanBaoHanh
                + ", ngayHetHanBaoHanh=" + ngayHetHanBaoHanh
                + '}';
    }
}
