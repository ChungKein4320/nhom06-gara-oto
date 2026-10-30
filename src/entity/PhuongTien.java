package entity;

import java.math.BigDecimal;

public class PhuongTien {
    private String VIN;
    private String maKhachHang;
    private String bienSo;
    private String hangXe;
    private String dongXe;
    private String mauXe;
    private Integer namSanXuat;
    private String soMay;
    private BigDecimal soKm;

    public PhuongTien() { }

    public PhuongTien(String vIN,String maKhachHang, String bienSo, String hangXe, String dongXe, String mauXe, Integer namSanXuat,
			String soMay, BigDecimal soKm) {
		super();
		VIN = vIN;
        this.maKhachHang = maKhachHang;
		this.bienSo = bienSo;
		this.hangXe = hangXe;
		this.dongXe = dongXe;
		this.mauXe = mauXe;
		this.namSanXuat = namSanXuat;
		this.soMay = soMay;
		this.soKm = soKm;
	}


    public String getVIN() {
        return VIN;
    }

    public void setVIN(String value) {
        if (value == null || !value.matches("[A-HJ-NPR-Z0-9]{17}"))
            throw new IllegalArgumentException("VIN phải gồm 17 ký tự in hoa hoặc chữ số, không có I, O, Q");
        this.VIN = value;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String value) {
        if (value == null || !value.matches("KH[0-9]{3}"))
            throw new IllegalArgumentException("Mã khách hàng chủ sở hữu phải có dạng KH001.");
        this.maKhachHang = value;
    }

    public String getBienSo() {
        return bienSo;
    }

    public void setBienSo(String value) {
        if (value == null || !value.matches("[0-9]{2}[A-Z][0-9]{5}"))
            throw new IllegalArgumentException("Biển số phải có dạng 30F25658");
        this.bienSo = value;
    }

    public String getHangXe() {
        return hangXe;
    }

    public void setHangXe(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Hãng xe không được rỗng");
        this.hangXe = value.trim();
    }

    public String getDongXe() {
        return dongXe;
    }

    public void setDongXe(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Dòng xe không được rỗng");
        this.dongXe = value.trim();
    }

    public String getMauXe() {
        return mauXe;
    }

    public void setMauXe(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Màu xe không được rỗng");
        this.mauXe = value.trim();
    }

    public Integer getNamSanXuat() {
        return namSanXuat;
    }

    public void setNamSanXuat(Integer value) {
        if (value == null || value > java.time.Year.now().getValue() || value < 1886)
            throw new IllegalArgumentException("Năm sản xuất không hợp lệ");
        this.namSanXuat = value;
    }

    public String getSoMay() {
        return soMay;
    }

    public void setSoMay(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Số máy không được rỗng");
        this.soMay = value.trim();
    }

    public BigDecimal getSoKm() {
        return soKm;
    }

    public void setSoKm(BigDecimal value) {
        this.soKm = value;
    }

    public void capNhatBienSo(String bienSoMoi) {
        setBienSo(bienSoMoi);
    }

    @Override
    public String toString() {
        return "PhuongTien [VIN=" + VIN
                + ", maKhachHang=" + maKhachHang
                + ", bienSo=" + bienSo
                + ", hangXe=" + hangXe
                + ", dongXe=" + dongXe
                + ", mauXe=" + mauXe
                + ", namSanXuat=" + namSanXuat
                + ", soMay=" + soMay
                + ", soKm=" + soKm + "]";
    }

}
