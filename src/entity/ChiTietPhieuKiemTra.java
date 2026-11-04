
package entity;

// ===== ENTITY: ChiTietPhieuKiemTra =====
public class ChiTietPhieuKiemTra {
    private PhieuKiemTra maPKT;
    private DichVu maDichVu;
    private String hangMucKiemTra;
    private String ketQuaChuanDoan;
    private String ketQuaKiemTra;

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ChiTietPhieuKiemTra() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public ChiTietPhieuKiemTra() {
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: ChiTietPhieuKiemTra(PhieuKiemTra, DichVu, String, String, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public ChiTietPhieuKiemTra(PhieuKiemTra maPKT, DichVu maDichVu, String hangMucKiemTra,
            String ketQuaChuanDoan, String ketQuaKiemTra) {

        this.maPKT = maPKT;
        this.maDichVu = maDichVu;
        this.hangMucKiemTra = hangMucKiemTra;
        this.ketQuaChuanDoan = ketQuaChuanDoan;
        this.ketQuaKiemTra = ketQuaKiemTra;
    }

    // ===== PHƯƠNG THỨC: getMaPKT() — Lấy mã pkt. =====
    public PhieuKiemTra getMaPKT() {
        return maPKT;
    }

    // ===== PHƯƠNG THỨC: setMaPKT(PhieuKiemTra) — Gán mã pkt. =====
    public void setMaPKT(PhieuKiemTra value) {
        if (value == null || value.getMaPKT() == null || !value.getMaPKT().matches("PKT[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu kiểm tra không hợp lệ");
        this.maPKT = value;
    }

    // ===== PHƯƠNG THỨC: getMaDichVu() — Lấy mã dịch vụ. =====
    public DichVu getMaDichVu() {
        return maDichVu;
    }

    // ===== PHƯƠNG THỨC: setMaDichVu(DichVu) — Gán mã dịch vụ. =====
    public void setMaDichVu(DichVu value) {
        if (value == null || value.getMaDichVu() == null || !value.getMaDichVu().matches("DV[0-9]{3}"))
            throw new IllegalArgumentException("Mã dịch vụ không hợp lệ");
        this.maDichVu = value;
    }

    // ===== PHƯƠNG THỨC: getHangMucKiemTra() — Lấy hàng muc kiểm tra. =====
    public String getHangMucKiemTra() {
        return hangMucKiemTra;
    }

    // ===== PHƯƠNG THỨC: setHangMucKiemTra(String) — Gán hàng muc kiểm tra. =====
    public void setHangMucKiemTra(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Hạng mục kiểm tra không hợp lệ");
        this.hangMucKiemTra = value.trim();
    }

    // ===== PHƯƠNG THỨC: getKetQuaChanDoan() — Lấy ket qua chan doan. =====
    public String getKetQuaChanDoan() {
        return ketQuaChuanDoan;
    }

    // ===== PHƯƠNG THỨC: setKetQuaChanDoan(String) — Gán ket qua chan doan. =====
    public void setKetQuaChanDoan(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Kết quả chẩn đoán không hợp lệ");
        this.ketQuaChuanDoan = value.trim();
    }

    // ===== PHƯƠNG THỨC: getKetQuaChuanDoan() — Lấy ket qua chuan doan. =====
    public String getKetQuaChuanDoan() {
        return ketQuaChuanDoan;
    }

    // ===== PHƯƠNG THỨC: setKetQuaChuanDoan(String) — Gán ket qua chuan doan. =====
    public void setKetQuaChuanDoan(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Kết quả chẩn đoán không hợp lệ");
        this.ketQuaChuanDoan = value.trim();
    }

    // ===== PHƯƠNG THỨC: getKetQuaKiemTra() — Lấy ket qua kiểm tra. =====
    public String getKetQuaKiemTra() {
        return ketQuaKiemTra;
    }

    // ===== PHƯƠNG THỨC: setKetQuaKiemTra(String) — Gán ket qua kiểm tra. =====
    public void setKetQuaKiemTra(String value) {
        if (value != null && !java.util.Set.of("Đạt", "Không đạt").contains(value))
            throw new IllegalArgumentException("Kết quả kiểm tra không hợp lệ");
        this.ketQuaKiemTra = value;
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "ChiTietPhieuKiemTra{" + "maPKT=" + maPKT + ", maDichVu=" + maDichVu + ", hangMucKiemTra='"
                + hangMucKiemTra + '\'' + ", ketQuaChuanDoan='" + ketQuaChuanDoan + '\'' + ", ketQuaKiemTra='"
                + ketQuaKiemTra + '\'' + '}';
    }
}
