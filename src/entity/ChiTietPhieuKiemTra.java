
package entity;

public class ChiTietPhieuKiemTra {
    private PhieuKiemTra maPKT;
    private DichVu maDichVu;
    private String hangMucKiemTra;
    private String ketQuaChuanDoan;
    private String ketQuaKiemTra;

    public ChiTietPhieuKiemTra() {
    }

    public ChiTietPhieuKiemTra(
            PhieuKiemTra maPKT,
            DichVu maDichVu,
            String hangMucKiemTra,
            String ketQuaChuanDoan,
            String ketQuaKiemTra) {

        this.maPKT = maPKT;
        this.maDichVu = maDichVu;
        this.hangMucKiemTra = hangMucKiemTra;
        this.ketQuaChuanDoan = ketQuaChuanDoan;
        this.ketQuaKiemTra = ketQuaKiemTra;
    }

    public PhieuKiemTra getMaPKT() {
        return maPKT;
    }

    public void setMaPKT(PhieuKiemTra value) {
        if (value == null || value.getMaPKT() == null || !value.getMaPKT().matches("PKT[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu kiểm tra không hợp lệ");
        this.maPKT = value;
    }

    public DichVu getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(DichVu value) {
        if (value == null || value.getMaDichVu() == null || !value.getMaDichVu().matches("DV[0-9]{3}"))
            throw new IllegalArgumentException("Mã dịch vụ không hợp lệ");
        this.maDichVu = value;
    }

    public String getHangMucKiemTra() {
        return hangMucKiemTra;
    }

    public void setHangMucKiemTra(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Hạng mục kiểm tra không hợp lệ");
        this.hangMucKiemTra = value.trim();
    }

    public String getKetQuaChanDoan() {
        return ketQuaChuanDoan;
    }

    public void setKetQuaChanDoan(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Kết quả chẩn đoán không hợp lệ");
        this.ketQuaChuanDoan = value.trim();
    }

    public String getKetQuaChuanDoan() {
        return ketQuaChuanDoan;
    }

    public void setKetQuaChuanDoan(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Kết quả chẩn đoán không hợp lệ");
        this.ketQuaChuanDoan = value.trim();
    }

    public String getKetQuaKiemTra() {
        return ketQuaKiemTra;
    }

    public void setKetQuaKiemTra(String value) {
        if (value != null && !java.util.Set.of("Đạt", "Không đạt").contains(value))
            throw new IllegalArgumentException("Kết quả kiểm tra không hợp lệ");
        this.ketQuaKiemTra = value;
    }

    @Override
    public String toString() {
        return "ChiTietPhieuKiemTra{" +
                "maPKT=" + maPKT +
                ", maDichVu=" + maDichVu +
                ", hangMucKiemTra='" + hangMucKiemTra + '\'' +
                ", ketQuaChuanDoan='" + ketQuaChuanDoan + '\'' +
                ", ketQuaKiemTra='" + ketQuaKiemTra + '\'' +
                '}';
    }
}
