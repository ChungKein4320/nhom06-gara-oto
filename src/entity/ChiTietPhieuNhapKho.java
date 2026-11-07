package entity;

import java.math.BigDecimal;

/**
 * ============================================================================
 * ENTITY: ChiTietPhieuNhapKho (Chi tiết phiếu nhập kho)
 * ============================================================================
 * 1. Mục đích:
 *    - Đại diện cho một dòng hạng mục phụ tùng trong Phiếu nhập kho (ánh xạ bảng
 *      dbo.ChiTietPhieuNhapKho gồm khóa ghép: maPNK + maPhuTung).
 * 2. Phục vụ nghiệp vụ:
 *    - Nghiệp vụ 3: Lập và xác nhận phiếu nhập kho thực tế (UC009).
 * 3. Quy tắc dữ liệu quan trọng:
 *    - 0 <= soLuongNhap (chấp nhận nhập kho) <= soLuongThucNhan (giao thực tế) <= soLuongYeuCau (đặt mua).
 *    - Không cho phép dùng mã phụ tùng ảo PT000 để nhập kho.
 * 4. Phương thức quan trọng:
 *    - setSoLuongThucNhan(Integer), setSoLuongNhap(Integer): Kiểm tra ràng buộc số lượng.
 *    - tinhThanhTien(): Tính thành tiền = donGiaNhap * soLuongNhap.
 * ============================================================================
 */
public class ChiTietPhieuNhapKho {
    // ===== THUỘC TÍNH (Tương ứng các cột bảng dbo.ChiTietPhieuNhapKho) =====
    private String maPNK;
    private PhuTung maPhuTung;
    private Integer soLuongYeuCau;
    private Integer soLuongThucNhan;
    private Integer soLuongNhap;
    private BigDecimal donGiaNhap;

    // ===== CONSTRUCTOR =====

    // Khởi tạo đối tượng rỗng (mặc định).
    public ChiTietPhieuNhapKho() {
    }

    // Khởi tạo đầy đủ thông tin một dòng chi tiết phiếu nhập kho.
    public ChiTietPhieuNhapKho(String maPNK, PhuTung maPhuTung, Integer soLuongYeuCau,
            Integer soLuongThucNhan, Integer soLuongNhap, BigDecimal donGiaNhap) {
        this.maPNK = maPNK;
        this.maPhuTung = maPhuTung;
        this.soLuongYeuCau = soLuongYeuCau;
        this.soLuongThucNhan = soLuongThucNhan;
        this.soLuongNhap = soLuongNhap;
        this.donGiaNhap = donGiaNhap;
    }

    // ===== GETTER & SETTER (Kèm kiểm tra tính hợp lệ của dữ liệu) =====

    // Lấy mã phiếu nhập kho (dạng PNKxxx).
    public String getMaPNK() {
        return maPNK;
    }

    // Gán mã phiếu nhập kho (bắt buộc đúng định dạng PNK + 3 chữ số).
    public void setMaPNK(String value) {
        if (value == null || !value.matches("PNK[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu nhập kho không hợp lệ");
        this.maPNK = value;
    }

    // Lấy đối tượng phụ tùng của dòng chi tiết.
    public PhuTung getMaPhuTung() {
        return maPhuTung;
    }

    // Gán đối tượng phụ tùng (mã phải có dạng PTxxx và không được là PT000).
    public void setMaPhuTung(PhuTung value) {
        if (value == null || value.getMaPhuTung() == null || !value.getMaPhuTung().matches("PT[0-9]{3}")
                || "PT000".equals(value.getMaPhuTung()))
            throw new IllegalArgumentException("Mã phụ tùng không hợp lệ");
        this.maPhuTung = value;
    }

    // Lấy số lượng yêu cầu đặt mua ban đầu (theo Yêu cầu nhập kho đã duyệt).
    public Integer getSoLuongYeuCau() {
        return soLuongYeuCau;
    }

    // Gán số lượng yêu cầu đặt mua (nếu có phải > 0).
    public void setSoLuongYeuCau(Integer value) {
        if (value != null && value <= 0)
            throw new IllegalArgumentException("Số lượng yêu cầu phải lớn hơn 0");
        this.soLuongYeuCau = value;
    }

    // Lấy số lượng thực nhận từ nhà cung cấp.
    public Integer getSoLuongThucNhan() {
        return soLuongThucNhan;
    }

    // Gán số lượng thực nhận (phải >= 0, >= soLuongNhap và <= soLuongYeuCau).
    public void setSoLuongThucNhan(Integer value) {
        if (value != null && value < 0)
            throw new IllegalArgumentException("Số lượng thực nhận không hợp lệ");
        if (value != null && soLuongNhap != null && soLuongNhap > value)
            throw new IllegalArgumentException("Số lượng nhập không được vượt số lượng thực nhận");
        if (value != null && soLuongYeuCau != null && value > soLuongYeuCau)
            throw new IllegalArgumentException("Số lượng thực nhận không được vượt số lượng yêu cầu");
        this.soLuongThucNhan = value;
    }

    // Lấy số lượng chấp nhận nhập kho thực tế (dùng để cộng vào tồn kho khi xác nhận).
    public Integer getSoLuongNhap() {
        return soLuongNhap;
    }

    // Gán số lượng chấp nhận nhập kho (phải >= 0 và <= soLuongThucNhan).
    public void setSoLuongNhap(Integer value) {
        if (value == null || value < 0)
            throw new IllegalArgumentException("Số lượng nhập không hợp lệ");
        if (soLuongThucNhan != null && value > soLuongThucNhan)
            throw new IllegalArgumentException("Số lượng nhập không được vượt số lượng thực nhận");
        this.soLuongNhap = value;
    }

    // Lấy đơn giá nhập thực tế của phụ tùng.
    public BigDecimal getDonGiaNhap() {
        return donGiaNhap;
    }

    // Gán đơn giá nhập thực tế (bắt buộc không âm).
    public void setDonGiaNhap(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Đơn giá nhập không hợp lệ");
        this.donGiaNhap = value;
    }

    // ===== PHƯƠNG THỨC NGHIỆP VỤ =====

    // Tính thành tiền của dòng phụ tùng nhập kho = donGiaNhap * soLuongNhap.
    public BigDecimal tinhThanhTien() {
        if (soLuongNhap == null || donGiaNhap == null)
            throw new IllegalStateException("Chưa đủ số lượng và đơn giá để tính thành tiền");
        return donGiaNhap.multiply(BigDecimal.valueOf(soLuongNhap));
    }

    // Chuyển thông tin dòng chi tiết phiếu nhập kho thành chuỗi phục vụ kiểm tra/log.
    @Override
    public String toString() {
        return "ChiTietPhieuNhapKho [maPNK=" + maPNK + ", maPhuTung=" + maPhuTung + ", soLuongYeuCau="
                + soLuongYeuCau + ", soLuongThucNhan=" + soLuongThucNhan + ", soLuongNhap=" + soLuongNhap
                + ", donGiaNhap=" + donGiaNhap + "]";
    }

}
