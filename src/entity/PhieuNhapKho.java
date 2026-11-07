package entity;

import java.sql.Timestamp;

/**
 * ============================================================================
 * ENTITY: PhieuNhapKho (Đầu phiếu nhập kho phụ tùng)
 * ============================================================================
 * 1. Mục đích:
 *    - Đại diện cho thông tin chung (header) của Phiếu nhập kho phụ tùng
 *      (ánh xạ bảng dbo.PhieuNhapKho, khóa chính maPNK).
 * 2. Phục vụ nghiệp vụ:
 *    - Nghiệp vụ 3: Lập và xác nhận phiếu nhập kho thực tế từ Yêu cầu nhập kho đã duyệt.
 * 3. Quy tắc dữ liệu quan trọng:
 *    - Khi gắn với Yêu cầu nhập kho (maYCNK != null), trạng thái hợp lệ gồm:
 *      "Chờ xác nhận nhập kho" (bản nháp), "Đã nhập kho" (đã tăng tồn kho), "Đã hủy".
 * 4. Phương thức quan trọng:
 *    - setMaPNK(String), setMaYCNK(YeuCauNhapKho), setNgayNhap(Timestamp), setTrangThai(String).
 * ============================================================================
 */
public class PhieuNhapKho {
    // ===== THUỘC TÍNH (Tương ứng các cột bảng dbo.PhieuNhapKho) =====
    private String maPNK;
    private YeuCauNhapKho maYCNK;
    private NhaCungCap maNCC;
    private NhanVien maNhanVien;
    private Timestamp ngayNhap;
    private String trangThai;

    // ===== CONSTRUCTOR =====

    // Khởi tạo đối tượng rỗng (mặc định).
    public PhieuNhapKho() {
    }

    // Khởi tạo đầy đủ thông tin đầu phiếu nhập kho.
    public PhieuNhapKho(String maPNK, YeuCauNhapKho maYCNK, NhaCungCap maNCC, NhanVien maNhanVien,
            Timestamp ngayNhap, String trangThai) {
        this.maPNK = maPNK;
        this.maYCNK = maYCNK;
        this.maNCC = maNCC;
        this.maNhanVien = maNhanVien;
        this.ngayNhap = ngayNhap;
        this.trangThai = trangThai;
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

    // Lấy đối tượng Yêu cầu nhập kho gốc mà phiếu nhập kho này tham chiếu tới.
    public YeuCauNhapKho getMaYCNK() {
        return maYCNK;
    }

    // Gán đối tượng Yêu cầu nhập kho gốc (nếu có phải đúng định dạng YCNxxx).
    public void setMaYCNK(YeuCauNhapKho value) {
        if (value != null && (value.getMaYCNK() == null || !value.getMaYCNK().matches("YCN[0-9]{3}")))
            throw new IllegalArgumentException("Mã yêu cầu nhập kho không hợp lệ");
        this.maYCNK = value;
    }

    // Lấy đối tượng Nhà cung cấp giao hàng.
    public NhaCungCap getMaNCC() {
        return maNCC;
    }

    // Gán đối tượng Nhà cung cấp (bắt buộc đúng định dạng NCCxxx).
    public void setMaNCC(NhaCungCap value) {
        if (value == null || value.getMaNCC() == null || !value.getMaNCC().matches("NCC[0-9]{3}"))
            throw new IllegalArgumentException("Mã nhà cung cấp không hợp lệ");
        this.maNCC = value;
    }

    // Lấy đối tượng Nhân viên kho thực hiện lập/xác nhận phiếu nhập kho.
    public NhanVien getMaNhanVien() {
        return maNhanVien;
    }

    // Gán đối tượng Nhân viên thực hiện (bắt buộc đúng định dạng NVxxx).
    public void setMaNhanVien(NhanVien value) {
        if (value == null || value.getMaNhanVien() == null || !value.getMaNhanVien().matches("NV[0-9]{3}"))
            throw new IllegalArgumentException("Mã nhân viên không hợp lệ");
        this.maNhanVien = value;
    }

    // Lấy thời điểm lập/nhập kho.
    public Timestamp getNgayNhap() {
        return ngayNhap;
    }

    // Gán thời điểm nhập kho (không được vượt quá thời gian hiện tại).
    public void setNgayNhap(Timestamp value) {
        if (value == null || value.after(new Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày nhập không hợp lệ");
        this.ngayNhap = value;
    }

    // Lấy trạng thái hiện tại của phiếu nhập kho.
    public String getTrangThai() {
        return trangThai;
    }

    // Gán trạng thái phiếu nhập kho (kiểm tra tập trạng thái hợp lệ theo loại phiếu).
    public void setTrangThai(String value) {
        java.util.Set<String> trangThaiHopLe = maYCNK == null
                ? java.util.Set.of("Chờ duyệt", "Đã duyệt", "Không duyệt")
                : java.util.Set.of("Chờ xác nhận nhập kho", "Đã nhập kho", "Đã hủy");
        if (value == null || !trangThaiHopLe.contains(value))
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        this.trangThai = value;
    }

    // Chuyển thông tin đầu phiếu nhập kho thành chuỗi phục vụ kiểm tra/log.
    @Override
    public String toString() {
        return "PhieuNhapKho [maPNK=" + maPNK + ", maYCNK=" + maYCNK + ", maNCC=" + maNCC + ", maNhanVien="
                + maNhanVien + ", ngayNhap=" + ngayNhap + ", trangThai=" + trangThai + "]";
    }
}
