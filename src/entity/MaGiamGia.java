package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class MaGiamGia {
    private String maGiamGia;
    private KhachHang maKhachHang;
    private BigDecimal mucGiam;
    private Timestamp ngayTao;
    private Timestamp ngayHetHan;
    private String trangThai;
    private Integer mocDiem;
    private Timestamp ngayBatDauChuKy;

    public MaGiamGia() { }

    public MaGiamGia(String maGiamGia, KhachHang maKhachHang, BigDecimal mucGiam, Timestamp ngayTao, Timestamp ngayHetHan,
			String trangThai, Integer mocDiem, Timestamp ngayBatDauChuKy) {
		super();
		this.maGiamGia = maGiamGia;
		this.maKhachHang = maKhachHang;
		this.mucGiam = mucGiam;
		this.ngayTao = ngayTao;
		this.ngayHetHan = ngayHetHan;
		this.trangThai = trangThai;
		this.mocDiem = mocDiem;
		this.ngayBatDauChuKy = ngayBatDauChuKy;
	}
    

    public String getMaGiamGia() {
        return maGiamGia;
    }

    public void setMaGiamGia(String value) {
        if (value == null || !value.matches("MGG[0-9]{3}"))
            throw new IllegalArgumentException("Mã giảm giá không hợp lệ");
        this.maGiamGia = value;
    }

    public KhachHang getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(KhachHang value) {
        if (value == null || value.getMaKhachHang() == null
                || !value.getMaKhachHang().matches("KH[0-9]{3}"))
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ");
        this.maKhachHang = value;
    }

    public BigDecimal getMucGiam() {
        return mucGiam;
    }

    public void setMucGiam(BigDecimal value) {
        if (value == null || value.compareTo(java.math.BigDecimal.ZERO) < 0 || value.compareTo(new java.math.BigDecimal("100")) > 0)
            throw new IllegalArgumentException("Mức giảm phải từ 0 đến 100 phần trăm");
        this.mucGiam = value;
    }

    public Timestamp getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Timestamp value) {
        if (value == null || value.after(new java.sql.Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày tạo không được lớn hơn hiện tại");
        this.ngayTao = value;
    }

    public Timestamp getNgayHetHan() {
        return ngayHetHan;
    }

    public void setNgayHetHan(Timestamp value) {
        if (value == null) throw new IllegalArgumentException("Ngày hết hạn không được rỗng");
        if (ngayTao != null && !value.after(ngayTao)) throw new IllegalArgumentException("Ngày hết hạn phải sau ngày tạo");
        this.ngayHetHan = value;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Trạng thái không được rỗng");
        this.trangThai = value.trim();
    }

    public Integer getMocDiem() { return mocDiem; }
    public void setMocDiem(Integer value) {
        if (value != null && value < 0) throw new IllegalArgumentException("Mốc điểm không hợp lệ");
        this.mocDiem = value;
    }
    public Timestamp getNgayBatDauChuKy() { return ngayBatDauChuKy; }
    public void setNgayBatDauChuKy(Timestamp value) {
        if (value != null && value.after(new Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày bắt đầu chu kỳ không hợp lệ");
        this.ngayBatDauChuKy = value;
    }
    
    public boolean kiemTraHieuLuc() {
        if (ngayHetHan == null) {
            return false;
        }

        Timestamp hienTai = new Timestamp(System.currentTimeMillis());
        return "Đang hiệu lực".equals(trangThai)
                && ngayTao != null && !ngayTao.after(hienTai)
                && !ngayHetHan.before(hienTai);
    }

    @Override
    public String toString() {
        return "MaGiamGia [maGiamGia=" + maGiamGia
                + ", maKhachHang=" + maKhachHang
                + ", mucGiam=" + mucGiam
                + ", ngayTao=" + ngayTao
                + ", ngayHetHan=" + ngayHetHan
                + ", trangThai=" + trangThai
                + ", mocDiem=" + mocDiem
                + ", ngayBatDauChuKy=" + ngayBatDauChuKy + "]";
    }

}
