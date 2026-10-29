package entity;

import java.math.BigDecimal;

public class PhuTung {
    private String maPhuTung;
    private String tenPhuTung;
    private BigDecimal donGia;
    private String loaiPhuTung;
    private Integer thoiHanBaoHanh;

    public PhuTung() { }

    public PhuTung(String maPhuTung, String tenPhuTung, BigDecimal donGia, String loaiPhuTung, Integer thoiHanBaoHanh) {
		super();
		this.maPhuTung = maPhuTung;
		this.tenPhuTung = tenPhuTung;
		this.donGia = donGia;
		this.loaiPhuTung = loaiPhuTung;
		this.thoiHanBaoHanh = thoiHanBaoHanh;
	}

    public String getMaPhuTung() {
        return maPhuTung;
    }

    public void setMaPhuTung(String value) {
        this.maPhuTung = value;
    }

    public String getTenPhuTung() {
        return tenPhuTung;
    }

    public void setTenPhuTung(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Tên phụ tùng không được để trống");
        if (value.trim().length() > 150)
            throw new IllegalArgumentException("Tên phụ tùng không được quá 150 ký tự");
        this.tenPhuTung = value.trim();
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal value) {
        if (value == null || value.signum() < 0)
            throw new IllegalArgumentException("Đơn giá phụ tùng phải là số không âm");
        if (value.scale() > 2 || value.precision() - value.scale() > 16)
        	throw new IllegalArgumentException("Đơn giá quá lớn, vui lòng nhập giá trị nhỏ hơn.");
        this.donGia = value;
    }

    public String getLoaiPhuTung() {
        return loaiPhuTung;
    }

    public void setLoaiPhuTung(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Loại phụ tùng không được để trống");
        if (value.trim().length() > 100)
            throw new IllegalArgumentException("Loại phụ tùng không được quá 100 ký tự");
        this.loaiPhuTung = value.trim();
    }

    public Integer getThoiHanBaoHanh() {
        return thoiHanBaoHanh;
    }

    public void setThoiHanBaoHanh(Integer value) {
    	if (value != null && value < 0)
            throw new IllegalArgumentException("Thời hạn bảo hành không được âm");
        this.thoiHanBaoHanh = value;
    }
    public void capNhatGia(BigDecimal giaMoi) {
        setDonGia(giaMoi);
    }

    @Override
    public String toString() {
        return "PhuTung [maPhuTung=" + maPhuTung
                + ", tenPhuTung=" + tenPhuTung
                + ", donGia=" + donGia
                + ", loaiPhuTung=" + loaiPhuTung
                + ", thoiHanBaoHanh=" + thoiHanBaoHanh + "]";
    }

}