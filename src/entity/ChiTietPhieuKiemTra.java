package entity;

public class ChiTietPhieuKiemTra {
    private PhieuKiemTra maPKT;
    private DichVu maDichVu;
    private String hangMucKiemTra;
    private String ketQuaChuanDoan;
    private String ketQuaKiemTra;

    public ChiTietPhieuKiemTra() { }

    public PhieuKiemTra getMaPKT() {
        return maPKT;
    }

    public void setMaPKT(PhieuKiemTra value) {
        this.maPKT = value;
    }

    public DichVu getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(DichVu value) {
        this.maDichVu = value;
    }

    public String getHangMucKiemTra() {
        return hangMucKiemTra;
    }

    public void setHangMucKiemTra(String value) {
        this.hangMucKiemTra = value;
    }

    public String getKetQuaChanDoan() {
        return ketQuaChuanDoan;
    }

    public void setKetQuaChanDoan(String value) {
        this.ketQuaChuanDoan = value;
    }

    public String getKetQuaChuanDoan() { return ketQuaChuanDoan; }
    public void setKetQuaChuanDoan(String value) { ketQuaChuanDoan=value; }

    public String getKetQuaKiemTra() {
        return ketQuaKiemTra;
    }

    public void setKetQuaKiemTra(String value) {
        this.ketQuaKiemTra = value;
    }

     public ChiTietPhieuKiemTra(PhieuKiemTra maPKT,
                               DichVu maDichVu,
                               String hangMucKiemTra,
                               String ketQuaChuanDoan,
                               String ketQuaKiemTra) {

        setMaPKT(maPKT);
        setMaDichVu(maDichVu);
        setHangMucKiemTra(hangMucKiemTra);
        setKetQuaChanDoan(ketQuaChuanDoan);
        setKetQuaKiemTra(ketQuaKiemTra);
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
