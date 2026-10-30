package entity;

public class ChiTietPhieuTiepNhan {
    private PhieuTiepNhan maPTN;
    private DichVu maDichVu;
    private String yeuCauDichVu;

    public ChiTietPhieuTiepNhan() { }

    public PhieuTiepNhan getMaPTN() {
        return maPTN;
    }

    public void setMaPTN(PhieuTiepNhan value) {
        this.maPTN = value;
    }

    public DichVu getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(DichVu value) {
        this.maDichVu = value;
    }

    public String getYeuCauDichVu() {
        return yeuCauDichVu;
    }

    public void setYeuCauDichVu(String value) {
        this.yeuCauDichVu = value;
    }

     public ChiTietPhieuTiepNhan(PhieuTiepNhan maPTN,
                               DichVu maDichVu,
                               String yeuCauDichVu) {

        setMaPTN(maPTN);
        setMaDichVu(maDichVu);
        setYeuCauDichVu(yeuCauDichVu);
    }

    @Override
    public String toString() {
        return "ChiTietPhieuTiepNhan{" +
                "maPTN=" + maPTN +
                ", maDichVu=" + maDichVu +
                ", yeuCauDichVu='" + yeuCauDichVu + '\'' +
                '}';
    }

}