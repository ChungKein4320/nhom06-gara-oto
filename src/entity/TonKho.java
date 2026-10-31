package entity;

public class TonKho {
    private String maTonKho;
    private PhuTung maPhuTung;
    private String tenPhuTung;
    private Integer soLuongHienTai;
    private Integer soLuongToiThieu;

    public TonKho() { }



    public TonKho(String maTonKho, PhuTung maPhuTung, Integer soLuongHienTai, Integer soLuongToiThieu) {
        this.maTonKho = maTonKho;
        this.maPhuTung = maPhuTung;
        this.soLuongHienTai = soLuongHienTai;
        this.soLuongToiThieu = soLuongToiThieu;
    }

    public TonKho(String maTonKho, PhuTung maPhuTung, String tenPhuTung,
            Integer soLuongHienTai, Integer soLuongToiThieu) {
        this.maTonKho = maTonKho;
        this.maPhuTung = maPhuTung;
        this.tenPhuTung = tenPhuTung;
        this.soLuongHienTai = soLuongHienTai;
        this.soLuongToiThieu = soLuongToiThieu;
    }



    public String getMaTonKho() {
        return maTonKho;
    }

    public void setMaTonKho(String value) {
        if (value == null || !value.matches("TKH[0-9]{3}"))
            throw new IllegalArgumentException("Mã tồn kho không hợp lệ");
        this.maTonKho = value;
    }

    public PhuTung getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(PhuTung value) {
        if (value == null || value.getMaPhuTung() == null || !value.getMaPhuTung().matches("PT[0-9]{3}"))
            throw new IllegalArgumentException("Mã phụ tùng không hợp lệ");
        this.maPhuTung = value;
    }

    public String getTenPhuTung() {
        return tenPhuTung;
    }

    public void setTenPhuTung(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Tên phụ tùng không được rỗng");
        this.tenPhuTung = value.trim();
    }

    public Integer getSoLuongHienTai() {
        return soLuongHienTai;
    }

    public void setSoLuongHienTai(Integer value) {
        if (value == null || value < 0)
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        this.soLuongHienTai = value;
    }

    public Integer getSoLuongToiThieu() {
        return soLuongToiThieu;
    }

    public void setSoLuongToiThieu(Integer value) {
        if (value == null || value < 0)
            throw new IllegalArgumentException("Số lượng tối thiểu không hợp lệ");
        this.soLuongToiThieu = value;
    }

    public void nhapKho(Integer soLuong) {
        if (soLuong == null || soLuong <= 0)
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        if (soLuongHienTai == null || soLuongHienTai < 0)
            throw new IllegalStateException("Số lượng tồn kho hiện tại chưa hợp lệ");
        this.soLuongHienTai = Math.addExact(soLuongHienTai, soLuong);
    }

    public void xuatKho(Integer soLuong) {
        if (soLuong == null || soLuong <= 0)
            throw new IllegalArgumentException("Số lượng xuất phải lớn hơn 0");
        if (soLuongHienTai == null || soLuong > soLuongHienTai)
            throw new IllegalArgumentException("Không đủ số lượng tồn kho");
        this.soLuongHienTai -= soLuong;
    }

    public boolean kiemTraDuHang(Integer soLuongYeuCau) {
        if (soLuongYeuCau == null || soLuongYeuCau < 0)
            throw new IllegalArgumentException("Số lượng yêu cầu không hợp lệ");
        return soLuongHienTai != null && soLuongHienTai >= soLuongYeuCau;
    }



    @Override
    public String toString() {
        return "TonKho [maTonKho=" + maTonKho + ", maPhuTung=" + maPhuTung + ", tenPhuTung=" + tenPhuTung
                + ", soLuongHienTai=" + soLuongHienTai
                + ", soLuongToiThieu=" + soLuongToiThieu + "]";
    }



}
