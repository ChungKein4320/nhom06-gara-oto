package entity;

import java.sql.Timestamp;

/**
 * ============================================================================
 * ENTITY: YeuCauNhapKho (Đầu phiếu yêu cầu nhập kho)
 * ============================================================================
 * 1. Mục đích:
 *    - Đại diện cho thông tin chung (header) của Yêu cầu nhập kho phụ tùng
 *      (ánh xạ bảng dbo.YeuCauNhapKho, khóa chính maYCNK).
 * 2. Phục vụ nghiệp vụ:
 *    - Nghiệp vụ 2: Nhân viên kho lập yêu cầu nhập kho ("Chờ duyệt") và
 *      Quản lý/Admin phê duyệt ("Đã duyệt") hoặc từ chối ("Từ chối").
 *    - Nghiệp vụ 3: Khi Phiếu nhập kho thực tế được xác nhận, trạng thái YCNK
 *      chuyển sang "Đã xử lý".
 * 3. Phương thức quan trọng:
 *    - setMaYCNK(String), setTrangThai(String), setNgayDuyet(Timestamp).
 * ============================================================================
 */
public class YeuCauNhapKho {
    // ===== THUỘC TÍNH (Tương ứng các cột bảng dbo.YeuCauNhapKho) =====
    private String maYCNK;
    private NhaCungCap maNCC;
    private NhanVien maNhanVien;
    private Timestamp ngayTao;
    private String trangThai;
    private String maNhanVienDuyet;
    private Timestamp ngayDuyet;
    private String ghiChu;

    // ===== CONSTRUCTOR =====

    // Khởi tạo đối tượng rỗng (mặc định).
    public YeuCauNhapKho() {
    }

    // Khởi tạo đầy đủ thông tin đầu phiếu yêu cầu nhập kho.
    public YeuCauNhapKho(String maYCNK, NhaCungCap maNCC, NhanVien maNhanVien, Timestamp ngayTao,
            String trangThai, String maNhanVienDuyet, Timestamp ngayDuyet, String ghiChu) {
        this.maYCNK = maYCNK;
        this.maNCC = maNCC;
        this.maNhanVien = maNhanVien;
        this.ngayTao = ngayTao;
        this.trangThai = trangThai;
        this.maNhanVienDuyet = maNhanVienDuyet;
        this.ngayDuyet = ngayDuyet;
        this.ghiChu = ghiChu;
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

    // Lấy đối tượng Nhà cung cấp dự kiến đặt mua.
    public NhaCungCap getMaNCC() {
        return maNCC;
    }

    // Gán đối tượng Nhà cung cấp (bắt buộc đúng định dạng NCCxxx).
    public void setMaNCC(NhaCungCap value) {
        if (value == null || value.getMaNCC() == null || !value.getMaNCC().matches("NCC[0-9]{3}"))
            throw new IllegalArgumentException("Mã nhà cung cấp không hợp lệ");
        this.maNCC = value;
    }

    // Lấy đối tượng Nhân viên kho lập yêu cầu.
    public NhanVien getMaNhanVien() {
        return maNhanVien;
    }

    // Gán đối tượng Nhân viên lập yêu cầu (bắt buộc đúng định dạng NVxxx).
    public void setMaNhanVien(NhanVien value) {
        if (value == null || value.getMaNhanVien() == null || !value.getMaNhanVien().matches("NV[0-9]{3}"))
            throw new IllegalArgumentException("Mã nhân viên không hợp lệ");
        this.maNhanVien = value;
    }

    // Lấy thời điểm tạo yêu cầu nhập kho.
    public Timestamp getNgayTao() {
        return ngayTao;
    }

    // Gán thời điểm tạo yêu cầu (không được vượt quá thời gian hiện tại).
    public void setNgayTao(Timestamp value) {
        if (value == null || value.after(new Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày tạo không hợp lệ");
        this.ngayTao = value;
    }

    // Lấy trạng thái hiện tại của yêu cầu nhập kho.
    public String getTrangThai() {
        return trangThai;
    }

    // Gán trạng thái yêu cầu (chỉ chấp nhận: Chờ duyệt, Đã duyệt, Từ chối, Đã xử lý).
    public void setTrangThai(String value) {
        if (value == null
                || !java.util.Set.of("Chờ duyệt", "Đã duyệt", "Từ chối", "Đã xử lý").contains(value))
            throw new IllegalArgumentException("Trạng thái yêu cầu nhập kho không hợp lệ");
        this.trangThai = value;
    }

    // Lấy mã nhân viên Quản lý/Admin phê duyệt hoặc từ chối yêu cầu.
    public String getMaNhanVienDuyet() {
        return maNhanVienDuyet;
    }

    // Gán mã nhân viên phê duyệt (nếu có phải đúng định dạng NVxxx).
    public void setMaNhanVienDuyet(String value) {
        if (value != null && !value.matches("NV[0-9]{3}"))
            throw new IllegalArgumentException("Mã nhân viên duyệt không hợp lệ");
        this.maNhanVienDuyet = value;
    }

    // Lấy thời điểm phê duyệt hoặc từ chối yêu cầu.
    public Timestamp getNgayDuyet() {
        return ngayDuyet;
    }

    // Gán thời điểm phê duyệt (nếu có phải >= ngayTao).
    public void setNgayDuyet(Timestamp value) {
        if (value != null && (ngayTao == null || value.before(ngayTao)))
            throw new IllegalArgumentException("Ngày duyệt phải bằng hoặc sau ngày tạo yêu cầu");
        this.ngayDuyet = value;
    }

    // Lấy nội dung ghi chú của yêu cầu nhập kho.
    public String getGhiChu() {
        return ghiChu;
    }

    // Gán nội dung ghi chú (tối đa 255 ký tự).
    public void setGhiChu(String value) {
        if (value != null && value.length() > 255)
            throw new IllegalArgumentException("Ghi chú không được quá 255 ký tự");
        this.ghiChu = value;
    }

    // Chuyển thông tin yêu cầu nhập kho thành chuỗi phục vụ kiểm tra/log.
    @Override
    public String toString() {
        return "YeuCauNhapKho [maYCNK=" + maYCNK + ", maNCC=" + maNCC + ", maNhanVien=" + maNhanVien
                + ", ngayTao=" + ngayTao + ", trangThai=" + trangThai + ", maNhanVienDuyet=" + maNhanVienDuyet
                + ", ngayDuyet=" + ngayDuyet + ", ghiChu=" + ghiChu + "]";
    }

}
