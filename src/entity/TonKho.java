package entity;

// ===== ENTITY: ĐỐI TƯỢNG TỒN KHO =====

public class TonKho {
    // ===== THUỘC TÍNH =====
    private String maTonKho;
    private PhuTung maPhuTung;
    private String tenPhuTung;
    private Integer soLuongHienTai;
    private Integer soLuongToiThieu;

    // ===== KHỞI TẠO ĐỐI TƯỢNG =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: TonKho() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public TonKho() {
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: TonKho(String, PhuTung, Integer, Integer) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public TonKho(String maTonKho, PhuTung maPhuTung, Integer soLuongHienTai, Integer soLuongToiThieu) {
        this.maTonKho = maTonKho;
        this.maPhuTung = maPhuTung;
        this.soLuongHienTai = soLuongHienTai;
        this.soLuongToiThieu = soLuongToiThieu;
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: TonKho(String, PhuTung, String, Integer, Integer) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public TonKho(String maTonKho, PhuTung maPhuTung, String tenPhuTung, Integer soLuongHienTai,
            Integer soLuongToiThieu) {
        this.maTonKho = maTonKho;
        this.maPhuTung = maPhuTung;
        this.tenPhuTung = tenPhuTung;
        this.soLuongHienTai = soLuongHienTai;
        this.soLuongToiThieu = soLuongToiThieu;
    }

    // ===== GETTER VÀ SETTER =====

    // ===== PHƯƠNG THỨC: getMaTonKho() — Lấy mã tồn kho. =====
    public String getMaTonKho() {
        return maTonKho;
    }

    // ===== PHƯƠNG THỨC: setMaTonKho(String) — Gán mã tồn kho. =====
    public void setMaTonKho(String value) {
        if (value == null || !value.matches("TKH[0-9]{3}"))
            throw new IllegalArgumentException("Mã tồn kho không hợp lệ");
        this.maTonKho = value;
    }

    // ===== PHƯƠNG THỨC: getMaPhuTung() — Lấy mã phụ tùng. =====
    public PhuTung getMaPhuTung() {
        return maPhuTung;
    }

    // ===== PHƯƠNG THỨC: setMaPhuTung(PhuTung) — Gán mã phụ tùng. =====
    public void setMaPhuTung(PhuTung value) {
        if (value == null || value.getMaPhuTung() == null || !value.getMaPhuTung().matches("PT[0-9]{3}"))
            throw new IllegalArgumentException("Mã phụ tùng không hợp lệ");
        this.maPhuTung = value;
    }

    // ===== PHƯƠNG THỨC: getTenPhuTung() — Lấy ten phụ tùng. =====
    public String getTenPhuTung() {
        return tenPhuTung;
    }

    // ===== PHƯƠNG THỨC: setTenPhuTung(String) — Gán ten phụ tùng. =====
    public void setTenPhuTung(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Tên phụ tùng không được rỗng");
        this.tenPhuTung = value.trim();
    }

    // ===== PHƯƠNG THỨC: getSoLuongHienTai() — Lấy số lượng hien tài. =====
    public Integer getSoLuongHienTai() {
        return soLuongHienTai;
    }

    // ===== PHƯƠNG THỨC: setSoLuongHienTai(Integer) — Gán số lượng hien tài. =====
    public void setSoLuongHienTai(Integer value) {
        if (value == null || value < 0)
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        this.soLuongHienTai = value;
    }

    // ===== PHƯƠNG THỨC: getSoLuongToiThieu() — Lấy số lượng toi thieu. =====
    public Integer getSoLuongToiThieu() {
        return soLuongToiThieu;
    }

    // ===== PHƯƠNG THỨC: setSoLuongToiThieu(Integer) — Gán số lượng toi thieu. =====
    public void setSoLuongToiThieu(Integer value) {
        if (value == null || value < 0)
            throw new IllegalArgumentException("Số lượng tối thiểu không hợp lệ");
        this.soLuongToiThieu = value;
    }

    // ===== PHƯƠNG THỨC: nhapKho(Integer) — Tăng số lượng tồn kho khi nhập hàng. =====
    public void nhapKho(Integer soLuong) {
        if (soLuong == null || soLuong <= 0)
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        if (soLuongHienTai == null || soLuongHienTai < 0)
            throw new IllegalStateException("Số lượng tồn kho hiện tại chưa hợp lệ");
        this.soLuongHienTai = Math.addExact(soLuongHienTai, soLuong);
    }

    // ===== PHƯƠNG THỨC: xuatKho(Integer) — Giảm số lượng tồn kho khi xuất hàng. =====
    public void xuatKho(Integer soLuong) {
        if (soLuong == null || soLuong <= 0)
            throw new IllegalArgumentException("Số lượng xuất phải lớn hơn 0");
        if (soLuongHienTai == null || soLuong > soLuongHienTai)
            throw new IllegalArgumentException("Không đủ số lượng tồn kho");
        this.soLuongHienTai -= soLuong;
    }

    // ===== PHƯƠNG THỨC: kiemTraDuHang(Integer) — Kiểm tra dữ hàng. =====
    public boolean kiemTraDuHang(Integer soLuongYeuCau) {
        if (soLuongYeuCau == null || soLuongYeuCau < 0)
            throw new IllegalArgumentException("Số lượng yêu cầu không hợp lệ");
        return soLuongHienTai != null && soLuongHienTai >= soLuongYeuCau;
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "TonKho [maTonKho=" + maTonKho + ", maPhuTung=" + maPhuTung + ", tenPhuTung=" + tenPhuTung
                + ", soLuongHienTai=" + soLuongHienTai + ", soLuongToiThieu=" + soLuongToiThieu + "]";
    }

}
