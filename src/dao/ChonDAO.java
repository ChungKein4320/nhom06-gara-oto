package dao;

// ===== DAO: ChonDAO =====
/** Chỉ chọn DAO; mọi truy vấn được viết trong DAO của từng đối tượng. */
public final class ChonDAO {
    // ===== KHỞI TẠO ĐỐI TƯỢNG: ChonDAO() — Khởi tạo đối tượng và thiết lập trạng thái ban đầu. =====
    private ChonDAO() {
    }

    // ===== PHƯƠNG THỨC: choBang(String) — Lấy DAO tương ứng với tên bảng dữ liệu. =====
    public static DuLieuDAO choBang(String table) {
        return switch (table) {
        case "ChiTietHoaDon" -> new ChiTietHoaDonDAO();
        case "ChiTietPhieuBaoGia" -> new ChiTietPhieuBaoGiaDAO();
        case "ChiTietPhieuKiemTra" -> new ChiTietPhieuKiemTraDAO();
        case "ChiTietPhieuNhapKho" -> new ChiTietPhieuNhapKhoDAO();
        case "ChiTietPhieuSuaChua" -> new ChiTietPhieuSuaChuaDAO();
        case "ChiTietPhieuTiepNhan" -> new ChiTietPhieuTiepNhanDAO();
        case "ChiTietYeuCauNhapKho" -> new ChiTietYeuCauNhapKhoDAO();
        case "DichVu" -> new DichVuDAO();
        case "HoaDon" -> new HoaDonDAO();
        case "KhachHang" -> new KhachHangDAO();
        case "LichBaoHanh" -> new LichBaoHanhDAO();
        case "LichNhacBaoDuong" -> new LichNhacBaoDuongDAO();
        case "LichSuTichDiem" -> new LichSuTichDiemDAO();
        case "MaGiamGia" -> new MaGiamGiaDAO();
        case "NhaCungCap" -> new NhaCungCapDAO();
        case "NhanVien" -> new NhanVienDAO();
        case "PhieuBaoGia" -> new PhieuBaoGiaDAO();
        case "PhieuKiemTra" -> new PhieuKiemTraDAO();
        case "PhieuNhapKho" -> new PhieuNhapKhoDAO();
        case "PhieuSuaChua" -> new PhieuSuaChuaDAO();
        case "PhieuTiepNhan" -> new PhieuTiepNhanDAO();
        case "PhuongThucThanhToan" -> new PhuongThucThanhToanDAO();
        case "PhuongTien" -> new PhuongTienDAO();
        case "PhuTung" -> new PhuTungDAO();
        case "TaiKhoan" -> new TaiKhoanDAO();
        case "TonKho" -> new TonKhoDAO();
        case "YeuCauNhapKho" -> new YeuCauNhapKhoDAO();
        default -> throw new IllegalArgumentException("Bảng không hợp lệ: " + table);
        };
    }

    // ===== PHƯƠNG THỨC: cacKhoa(String) — Liệt kê các cột khóa của bản ghi. =====
    public static String[] cacKhoa(String table) {
        return choBang(table).cacKhoa();
    }

    // ===== PHƯƠNG THỨC: khoaChinh(String) — Xác định cột khóa chính của bảng dữ liệu. =====
    public static String khoaChinh(String table) {
        DuLieuDAO dao = timDAO(table);
        return dao == null ? null : dao.khoaChinh();
    }

    // ===== PHƯƠNG THỨC: cacCot(String) — Liệt kê các cột dữ liệu của đối tượng. =====
    public static String[] cacCot(String table) {
        DuLieuDAO dao = timDAO(table);
        return dao == null ? null : dao.cacCot();
    }

    // ===== PHƯƠNG THỨC: kieuCot(String) — Khai báo kiểu dữ liệu của từng cột. =====
    public static java.util.Map<String, String> kieuCot(String table) {
        DuLieuDAO dao = timDAO(table);
        return dao == null ? null : dao.kieuCot();
    }

    // ===== PHƯƠNG THỨC: cotBatBuoc(String) — Liệt kê các cột bắt buộc nhập. =====
    public static java.util.Set<String> cotBatBuoc(String table) {
        DuLieuDAO dao = timDAO(table);
        return dao == null ? null : dao.cotBatBuoc();
    }

    // ===== PHƯƠNG THỨC: timDAO(String) — Tìm dao. =====
    private static DuLieuDAO timDAO(String table) {
        if (table == null)
            return null;
        try {
            return choBang(table);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    // ===== PHƯƠNG THỨC: khoaTheoBang() — Xác định cột khóa chính theo bảng. =====
    public static java.util.Map<String, String> khoaTheoBang() {
        java.util.Map<String, String> keys = new java.util.LinkedHashMap<>();
        for (String table : new String[] { "NhanVien", "KhachHang", "PhuongTien", "DichVu", "PhuTung",
                "NhaCungCap", "PhuongThucThanhToan", "TaiKhoan", "TonKho", "MaGiamGia", "PhieuTiepNhan",
                "ChiTietPhieuTiepNhan", "PhieuKiemTra", "ChiTietPhieuKiemTra", "PhieuBaoGia",
                "ChiTietPhieuBaoGia", "PhieuSuaChua", "ChiTietPhieuSuaChua", "YeuCauNhapKho",
                "ChiTietYeuCauNhapKho", "PhieuNhapKho", "ChiTietPhieuNhapKho", "HoaDon", "ChiTietHoaDon",
                "LichBaoHanh", "LichNhacBaoDuong", "LichSuTichDiem" }) {
            keys.put(table, choBang(table).khoaChinh());
        }
        return keys;
    }
}
