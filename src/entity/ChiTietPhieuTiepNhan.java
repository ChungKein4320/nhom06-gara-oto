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
        if (value == null || value.getMaPTN() == null || !value.getMaPTN().matches("PTN[0-9]{3}"))
            throw new IllegalArgumentException("Mã phiếu tiếp nhận không tồn tại");
        this.maPTN = value;
    }

    public DichVu getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(DichVu value) {
        if (value == null || value.getMaDichVu() == null || !value.getMaDichVu().matches("DV[0-9]{3}"))
            throw new IllegalArgumentException("Mã dịch vụ không tồn tại");
        this.maDichVu = value;
    }

    public String getYeuCauDichVu() {
        return yeuCauDichVu;
    }

    public void setYeuCauDichVu(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Yêu cầu dịch vụ không được rỗng");
        this.yeuCauDichVu = value.trim();
    }


    public ChiTietPhieuTiepNhan(
            PhieuTiepNhan maPTN,
            DichVu maDichVu,
            String yeuCauDichVu) {

        this.maPTN = maPTN;
        this.maDichVu = maDichVu;
        this.yeuCauDichVu = yeuCauDichVu;
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
