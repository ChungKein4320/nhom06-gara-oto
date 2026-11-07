package entity;

import java.math.BigDecimal;

/**
 * ============================================================================
 * ENTITY: ChiTietYeuCauNhapKho (Chi tiết yêu cầu nhập kho)
 * ============================================================================
 * 1. Mục đích:
 *    - Đại diện cho một dòng hạng mục phụ tùng đề xuất mua trong Yêu cầu nhập kho
 *      (ánh xạ bảng dbo.ChiTietYeuCauNhapKho gồm khóa ghép: maYCNK + maPhuTung).
 * 2. Phục vụ nghiệp vụ:
 *    - Nghiệp vụ 2: Lập và phê duyệt yêu cầu nhập kho.
 * 3. Quy tắc dữ liệu quan trọng:
 *    - soLuongYeuCau > 0, donGiaDuKien >= 0, mã phụ tùng dạng PTxxx (khác PT000).
 * 4. Phương thức quan trọng:
 *    - setMaPhuTung(PhuTung), setSoLuongYeuCau(Integer), setDonGiaDuKien(BigDecimal).
 * ============================================================================
 */
public class ChiTietYeuCauNhapKho {
    // ===== THUỘC TÍNH (Tương ứng các cột bảng dbo.ChiTietYeuCauNhapKho) =====
    private String maYCNK;
    private PhuTung maPhuTung;
    private Integer soLuongYeuCau;
    private BigDecimal donGiaDuKien;

    // ===== CONSTRUCTOR =====

    // Khởi tạo đối tượng rỗng (mặc định).
    public ChiTietYeuCauNhapKho() {
    }

    // Khởi tạo đầy đủ thông tin một dòng chi tiết yêu cầu nhập kho.
    public ChiTietYeuCauNhapKho(String maYCNK, PhuTung maPhuTung, Integer soLuongYeuCau,
            BigDecimal donGiaDuKien) {
        this.maYCNK = maYCNK;
        this.maPhuTung = maPhuTung;
        this.soLuongYeuCau = soLuongYeuCau;
        this.donGiaDuKien = donGiaDuKien;
    }

    // ===== GETTER & SETTER (Kèm kiểm tra tính hợp lệ của dữ liệu) =====

    // Lấy mã yêu cầu nhập kho (dạng YCNxxx).
    public String getMaYCNK() {
        return maYCNK;
    }

    // Gán mã yêu cầu nhập kho (bắt buộc đúng định dạng YCN + 3 chữ số).
    public void setMaYCNK(String value) {
        if (value == null || !value.matches("YCN[0-9]{3}"))
            throw new IllegalArgumentException("Mã yêu cầu nhập kho không hợp lệ");
        this.maYCNK = value;
    }

    // Lấy đối tượng phụ tùng đề xuất nhập.
    public PhuTung getMaPhuTung() {
        return maPhuTung;
    }

    // Gán đối tượng phụ tùng (mã phải có dạng PTxxx và không được là mã ảo PT000).
    public void setMaPhuTung(PhuTung value) {
        if (value == null || value.getMaPhuTung() == null || !value.getMaPhuTung().matches("PT[0-9]{3}")
                || "PT000".equals(value.getMaPhuTung()))
            throw new IllegalArgumentException("Mã phụ tùng không hợp lệ");
        this.maPhuTung = value;
    }

    // Lấy số lượng phụ tùng yêu cầu đặt mua.
    public Integer getSoLuongYeuCau() {
        return soLuongYeuCau;
    }

    // Gán số lượng yêu cầu đặt mua (bắt buộc là số nguyên dương > 0).
    public void setSoLuongYeuCau(Integer value) {
        if (value == null || value <= 0)
            throw new IllegalArgumentException("Số lượng yêu cầu phải lớn hơn 0");
        this.soLuongYeuCau = value;
    }

    // Lấy đơn giá dự kiến của phụ tùng khi lập yêu cầu.
    public BigDecimal getDonGiaDuKien() {
        return donGiaDuKien;
    }

    // Gán đơn giá dự kiến (bắt buộc không âm >= 0).
    public void setDonGiaDuKien(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Đơn giá dự kiến không hợp lệ");
        this.donGiaDuKien = value;
    }

    // Chuyển thông tin dòng chi tiết yêu cầu nhập kho thành chuỗi phục vụ kiểm tra/log.
    @Override
    public String toString() {
        return "ChiTietYeuCauNhapKho [maYCNK=" + maYCNK + ", maPhuTung=" + maPhuTung + ", soLuongYeuCau="
                + soLuongYeuCau + ", donGiaDuKien=" + donGiaDuKien + "]";
    }
}
