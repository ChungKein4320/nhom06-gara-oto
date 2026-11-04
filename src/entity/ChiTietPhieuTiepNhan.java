package entity;

// ===== ENTITY: ChiTietPhieuTiepNhan =====
public class ChiTietPhieuTiepNhan {
    private PhieuTiepNhan maPTN;
    private DichVu maDichVu;
    private String yeuCauDichVu;

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ChiTietPhieuTiepNhan() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public ChiTietPhieuTiepNhan() {
    }

    // ===== PHƯƠNG THỨC: getMaPTN() — Lấy mã ptn. =====
    public PhieuTiepNhan getMaPTN() {
        return maPTN;
    }

    // ===== PHƯƠNG THỨC: setMaPTN(PhieuTiepNhan) — Gán mã ptn. =====
    public void setMaPTN(PhieuTiepNhan value) {
        if (value == null || value.getMaPTN() == null || !value.getMaPTN().matches("PTN[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu tiếp nhận không tồn tại");
        this.maPTN = value;
    }

    // ===== PHƯƠNG THỨC: getMaDichVu() — Lấy mã dịch vụ. =====
    public DichVu getMaDichVu() {
        return maDichVu;
    }

    // ===== PHƯƠNG THỨC: setMaDichVu(DichVu) — Gán mã dịch vụ. =====
    public void setMaDichVu(DichVu value) {
        if (value == null || value.getMaDichVu() == null || !value.getMaDichVu().matches("DV[0-9]{3}"))
            throw new IllegalArgumentException("Mã dịch vụ không tồn tại");
        this.maDichVu = value;
    }

    // ===== PHƯƠNG THỨC: getYeuCauDichVu() — Lấy yêu cầu dịch vụ. =====
    public String getYeuCauDichVu() {
        return yeuCauDichVu;
    }

    // ===== PHƯƠNG THỨC: setYeuCauDichVu(String) — Gán yêu cầu dịch vụ. =====
    public void setYeuCauDichVu(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Yêu cầu dịch vụ không được rỗng");
        this.yeuCauDichVu = value.trim();
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ChiTietPhieuTiepNhan(PhieuTiepNhan, DichVu, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public ChiTietPhieuTiepNhan(PhieuTiepNhan maPTN, DichVu maDichVu, String yeuCauDichVu) {

        this.maPTN = maPTN;
        this.maDichVu = maDichVu;
        this.yeuCauDichVu = yeuCauDichVu;
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "ChiTietPhieuTiepNhan{" + "maPTN=" + maPTN + ", maDichVu=" + maDichVu + ", yeuCauDichVu='"
                + yeuCauDichVu + '\'' + '}';
    }

}
