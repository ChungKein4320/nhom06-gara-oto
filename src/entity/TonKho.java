package entity;

/**
 * ============================================================================
 * ENTITY: TonKho (Thông tin tồn kho phụ tùng)
 * ============================================================================
 * 1. Mục đích:
 *    - Đại diện cho bản ghi tồn kho của một phụ tùng trong kho (ánh xạ bảng
 *      dbo.TonKho, khóa chính maTonKho, liên kết 1-1 với PhuTung qua maPhuTung).
 * 2. Phục vụ nghiệp vụ:
 *    - Nghiệp vụ 1: Quản lý tồn kho và cập nhật mức tồn tối thiểu.
 *    - Nghiệp vụ 3, 4, 5: Tăng/giảm tồn kho khi nhập kho thực tế, cấp phát và hoàn trả.
 * 3. Phương thức quan trọng:
 *    - setSoLuongToiThieu(Integer): Kiểm tra định mức tồn tối thiểu >= 0.
 *    - nhapKho(Integer), xuatKho(Integer), kiemTraDuHang(Integer): Các phép toán tồn kho.
 * ============================================================================
 */
public class TonKho {
    // ===== THUỘC TÍNH (Tương ứng các cột bảng dbo.TonKho và tên hiển thị) =====
    private String maTonKho;
    private PhuTung maPhuTung;
    private String tenPhuTung;
    private Integer soLuongHienTai;
    private Integer soLuongToiThieu;

    // ===== CONSTRUCTOR =====

    // Khởi tạo đối tượng rỗng (mặc định).
    public TonKho() {
    }

    // Khởi tạo đối tượng tồn kho theo các cột gốc trong bảng dbo.TonKho.
    public TonKho(String maTonKho, PhuTung maPhuTung, Integer soLuongHienTai, Integer soLuongToiThieu) {
        this.maTonKho = maTonKho;
        this.maPhuTung = maPhuTung;
        this.soLuongHienTai = soLuongHienTai;
        this.soLuongToiThieu = soLuongToiThieu;
    }

    // Khởi tạo đầy đủ đối tượng tồn kho kèm tên phụ tùng hiển thị.
    public TonKho(String maTonKho, PhuTung maPhuTung, String tenPhuTung, Integer soLuongHienTai,
            Integer soLuongToiThieu) {
        this.maTonKho = maTonKho;
        this.maPhuTung = maPhuTung;
        this.tenPhuTung = tenPhuTung;
        this.soLuongHienTai = soLuongHienTai;
        this.soLuongToiThieu = soLuongToiThieu;
    }

    // ===== GETTER & SETTER (Kèm kiểm tra tính hợp lệ của dữ liệu) =====

    // Lấy mã tồn kho (dạng TKHxxx).
    public String getMaTonKho() {
        return maTonKho;
    }

    // Gán mã tồn kho (bắt buộc đúng định dạng TKH + 3 chữ số).
    public void setMaTonKho(String value) {
        if (value == null || !value.matches("TKH[0-9]{3}"))
            throw new IllegalArgumentException("Mã tồn kho không hợp lệ");
        this.maTonKho = value;
    }

    // Lấy đối tượng phụ tùng tương ứng với bản ghi tồn kho.
    public PhuTung getMaPhuTung() {
        return maPhuTung;
    }

    // Gán đối tượng phụ tùng (mã phải đúng định dạng PTxxx).
    public void setMaPhuTung(PhuTung value) {
        if (value == null || value.getMaPhuTung() == null || !value.getMaPhuTung().matches("PT[0-9]{3}"))
            throw new IllegalArgumentException("Mã phụ tùng không hợp lệ");
        this.maPhuTung = value;
    }

    // Lấy tên phụ tùng hiển thị.
    public String getTenPhuTung() {
        return tenPhuTung;
    }

    // Gán tên phụ tùng hiển thị (không được để trống).
    public void setTenPhuTung(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Tên phụ tùng không được rỗng");
        this.tenPhuTung = value.trim();
    }

    // Lấy số lượng tồn kho hiện tại trong kho.
    public Integer getSoLuongHienTai() {
        return soLuongHienTai;
    }

    // Gán số lượng tồn kho hiện tại (bắt buộc >= 0).
    public void setSoLuongHienTai(Integer value) {
        if (value == null || value < 0)
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        this.soLuongHienTai = value;
    }

    // Lấy định mức tồn kho tối thiểu (ngưỡng cảnh báo sắp hết hàng).
    public Integer getSoLuongToiThieu() {
        return soLuongToiThieu;
    }

    // Gán định mức tồn kho tối thiểu (bắt buộc >= 0).
    public void setSoLuongToiThieu(Integer value) {
        if (value == null || value < 0)
            throw new IllegalArgumentException("Số lượng tối thiểu không hợp lệ");
        this.soLuongToiThieu = value;
    }

    // ===== PHƯƠNG THỨC NGHIỆP VỤ =====

    // Cộng thêm số lượng vào tồn kho hiện tại khi nhập hàng (soLuong > 0).
    public void nhapKho(Integer soLuong) {
        if (soLuong == null || soLuong <= 0)
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        if (soLuongHienTai == null || soLuongHienTai < 0)
            throw new IllegalStateException("Số lượng tồn kho hiện tại chưa hợp lệ");
        this.soLuongHienTai = Math.addExact(soLuongHienTai, soLuong);
    }

    // Trừ số lượng tồn kho hiện tại khi xuất/cấp phát phụ tùng (0 < soLuong <= soLuongHienTai).
    public void xuatKho(Integer soLuong) {
        if (soLuong == null || soLuong <= 0)
            throw new IllegalArgumentException("Số lượng xuất phải lớn hơn 0");
        if (soLuongHienTai == null || soLuong > soLuongHienTai)
            throw new IllegalArgumentException("Không đủ số lượng tồn kho");
        this.soLuongHienTai -= soLuong;
    }

    // Kiểm tra số lượng tồn kho hiện tại có đủ đáp ứng số lượng yêu cầu hay không.
    public boolean kiemTraDuHang(Integer soLuongYeuCau) {
        if (soLuongYeuCau == null || soLuongYeuCau < 0)
            throw new IllegalArgumentException("Số lượng yêu cầu không hợp lệ");
        return soLuongHienTai != null && soLuongHienTai >= soLuongYeuCau;
    }

    // Chuyển thông tin tồn kho thành chuỗi phục vụ kiểm tra/log.
    @Override
    public String toString() {
        return "TonKho [maTonKho=" + maTonKho + ", maPhuTung=" + maPhuTung + ", tenPhuTung=" + tenPhuTung
                + ", soLuongHienTai=" + soLuongHienTai + ", soLuongToiThieu=" + soLuongToiThieu + "]";
    }

}
