package entity;

import java.sql.Timestamp;

// ===== ENTITY: ĐỐI TƯỢNG LỊCH SỬ TÍCH ĐIỂM =====

public class LichSuTichDiem {
    // ===== THUỘC TÍNH =====
    private String maLichSuTD;
    private KhachHang maKhachHang;
    private HoaDon maHoaDon;
    private Timestamp ngayTichDiem;
    private Timestamp ngayHetHan;
    private Integer soDiem;
    private String lyDo;

    // ===== KHỞI TẠO ĐỐI TƯỢNG =====

    // ===== KHỞI TẠO ĐỐI TƯỢNG: LichSuTichDiem() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public LichSuTichDiem() {
    }

    // ===== GETTER VÀ SETTER =====

    // ===== PHƯƠNG THỨC: getMaLichSuTD() — Lấy mã lich su td. =====
    public String getMaLichSuTD() {
        return maLichSuTD;
    }

    // ===== PHƯƠNG THỨC: setMaLichSuTD(String) — Gán mã lich su td. =====
    public void setMaLichSuTD(String value) {
        if (value == null || !value.matches("LS[0-9]{3}"))
            throw new IllegalArgumentException("Mã lịch sử không hợp lệ");
        this.maLichSuTD = value;
    }

    // ===== PHƯƠNG THỨC: getMaKhachHang() — Lấy mã khách hàng. =====
    public KhachHang getMaKhachHang() {
        return maKhachHang;
    }

    // ===== PHƯƠNG THỨC: setMaKhachHang(KhachHang) — Gán mã khách hàng. =====
    public void setMaKhachHang(KhachHang value) {
        if (value == null || value.getMaKhachHang() == null || !value.getMaKhachHang().matches("KH[0-9]{3}"))
            throw new IllegalArgumentException("Mã khách hàng không hợp lệ");
        this.maKhachHang = value;
    }

    // ===== PHƯƠNG THỨC: getMaHoaDon() — Lấy mã hóa đơn. =====
    public HoaDon getMaHoaDon() {
        return maHoaDon;
    }

    // ===== PHƯƠNG THỨC: setMaHoaDon(HoaDon) — Gán mã hóa đơn. =====
    public void setMaHoaDon(HoaDon value) {
        if (value == null || value.getMaHoaDon() == null || !value.getMaHoaDon().matches("HD[0-9]{3}"))
            throw new IllegalArgumentException("Mã hóa đơn không hợp lệ");
        this.maHoaDon = value;
    }

    // ===== PHƯƠNG THỨC: getNgayTichDiem() — Lấy ngày tích điểm. =====
    public Timestamp getNgayTichDiem() {
        return ngayTichDiem;
    }

    // ===== PHƯƠNG THỨC: setNgayTichDiem(Timestamp) — Gán ngày tích điểm. =====
    public void setNgayTichDiem(Timestamp value) {
        if (value == null || value.after(new Timestamp(System.currentTimeMillis())))
            throw new IllegalArgumentException("Ngày tích điểm không hợp lệ");
        this.ngayTichDiem = value;
    }

    // ===== PHƯƠNG THỨC: getNgayHetHan() — Lấy ngày het han. =====
    public Timestamp getNgayHetHan() {
        return ngayHetHan;
    }

    // ===== PHƯƠNG THỨC: setNgayHetHan(Timestamp) — Gán ngày het han. =====
    public void setNgayHetHan(Timestamp value) {
        if (value != null && ngayTichDiem != null && value.before(ngayTichDiem))
            throw new IllegalArgumentException("Ngày hết hạn không được trước ngày tích điểm");
        this.ngayHetHan = value;
    }

    // ===== PHƯƠNG THỨC: getSoDiem() — Lấy số điểm. =====
    public Integer getSoDiem() {
        return soDiem;
    }

    // ===== PHƯƠNG THỨC: setSoDiem(Integer) — Gán số điểm. =====
    public void setSoDiem(Integer value) {
        if (value == null || value <= 0)
            throw new IllegalArgumentException("Số điểm phải lớn hơn 0");
        this.soDiem = value;
    }

    // ===== PHƯƠNG THỨC: getLyDo() — Lấy ly do. =====
    public String getLyDo() {
        return lyDo;
    }

    // ===== PHƯƠNG THỨC: setLyDo(String) — Gán ly do. =====
    public void setLyDo(String value) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Lý do không được rỗng");
        this.lyDo = value.trim();
    }

    // ===== KHỞI TẠO ĐỐI TƯỢNG: LichSuTichDiem(String, KhachHang, HoaDon, Timestamp, Integer, String) — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    public LichSuTichDiem(String maLichSu, KhachHang maKhachHang, HoaDon maHoaDon, Timestamp ngayTichDiem,
            Integer soDiem, String lyDo) {

        this.maLichSuTD = maLichSu;
        this.maKhachHang = maKhachHang;
        this.maHoaDon = maHoaDon;
        this.ngayTichDiem = ngayTichDiem;
        this.soDiem = soDiem;
        this.lyDo = lyDo;
    }

    // ===== PHƯƠNG THỨC: toString() — Chuyển thông tin đối tượng thành chuỗi. =====
    @Override
    public String toString() {
        return "LichSuTichDiem{" + "maLichSuTD='" + maLichSuTD + '\'' + ", maKhachHang=" + maKhachHang
                + ", maHoaDon=" + maHoaDon + ", ngayTichDiem=" + ngayTichDiem + ", ngayHetHan=" + ngayHetHan
                + ", soDiem=" + soDiem + ", lyDo='" + lyDo + '\'' + '}';
    }

}
