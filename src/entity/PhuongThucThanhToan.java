package entity;

public class PhuongThucThanhToan {
    private String maPhuongThuc;
    private String tenPhuongThuc;

    public PhuongThucThanhToan() { }



    public PhuongThucThanhToan(String maPhuongThuc, String tenPhuongThuc) {
        this.maPhuongThuc = maPhuongThuc;
        this.tenPhuongThuc = tenPhuongThuc;
    }



    public String getMaPhuongThuc() {
        return maPhuongThuc;
    }

    public void setMaPhuongThuc(String value) {
        if (value == null || !value.matches("TT[0-9]{3}"))
            throw new IllegalArgumentException("Mã phương thức thanh toán không hợp lệ");
        this.maPhuongThuc = value;
    }

    public String getTenPhuongThuc() {
        return tenPhuongThuc;
    }

    public void setTenPhuongThuc(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Tên phương thức thanh toán không hợp lệ");
        this.tenPhuongThuc = value.trim();
    }



    @Override
    public String toString() {
        return "PhuongThucThanhToan [maPhuongThuc=" + maPhuongThuc + ", tenPhuongThuc=" + tenPhuongThuc + "]";
    }



}
