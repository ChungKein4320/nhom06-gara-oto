package entity;

import java.math.BigDecimal;

public class DichVu {
    private String maDichVu;
    private String tenDichVu;
    private String moTa;
    private BigDecimal donGia;
    private String trangThai;

    public DichVu() { }

    public DichVu(String maDichVu, String tenDichVu, String moTa, BigDecimal donGia, String trangThai) {
		super();
		this.maDichVu = maDichVu;
		this.tenDichVu = tenDichVu;
		this.moTa = moTa;
		this.donGia = donGia;
		this.trangThai = trangThai;
	}

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String value) {
        this.maDichVu = value;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public void setTenDichVu(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Tên dịch vụ không được để trống");
        if (value.trim().length() > 150)
            throw new IllegalArgumentException("Tên dịch vụ không được quá 150 ký tự");
        this.tenDichVu = value.trim();
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String value) {
        if (value != null && value.length() > 500)
            throw new IllegalArgumentException("Mô tả không được quá 500 ký tự");
        this.moTa = value;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal value) {
        if (value == null || value.signum() < 0)
            throw new IllegalArgumentException("Đơn giá dịch vụ phải là số không âm");
        if (value.scale() > 2 || value.precision() - value.scale() > 16)
            throw new IllegalArgumentException("Đơn giá quá lớn, vui lòng nhập giá trị nhỏ hơn.");
        this.donGia = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Trạng thái dịch vụ không được để trống");
        if (value.trim().length() > 50)
            throw new IllegalArgumentException("Trạng thái dịch vụ không được quá 50 ký tự");
        this.trangThai = value.trim();
    }

    public void capNhatDonGia(BigDecimal donGiaMoi) {
        setDonGia(donGiaMoi);
    }

    @Override
    public String toString() {
        return "DichVu [maDichVu=" + maDichVu
                + ", tenDichVu=" + tenDichVu
                + ", moTa=" + moTa
                + ", donGia=" + donGia
                + ", trangThai=" + trangThai + "]";
    }

}