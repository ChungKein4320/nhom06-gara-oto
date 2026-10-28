package dao;

import java.util.*;

/** Metadata đồng bộ với GaraOto.sql, gồm khóa ghép và cột tự tăng. */
public final class Schema {
    private Schema() {
    }

    public static final Map<String, String[]> COLUMNS = new LinkedHashMap<>();
    public static final Map<String, String> PRIMARY = new LinkedHashMap<>();
    public static final Map<String, String[]> KEYS = new LinkedHashMap<>();
    public static final Map<String, Set<String>> REQUIRED = new LinkedHashMap<>();
    public static final Map<String, Map<String, String>> TYPES = new LinkedHashMap<>();
    public static final Set<String> IDENTITY = new HashSet<>();
    static {
<<<<<<< Updated upstream
        define("NhanVien", new String[]{"maNhanVien"}, new String[]{"maNhanVien","hoTenNV","ngaySinh","soDienThoai","email","vaiTro","gioiTinh","trangThai"}, "maNhanVien=varchar:5","hoTenNV=nvarchar:100","ngaySinh=date:","soDienThoai=varchar:10","email=varchar:254","diaChi=nvarchar:255","vaiTro=nvarchar:50","gioiTinh=bit:","trangThai=nvarchar:30");
        define("KhachHang", new String[]{"maKhachHang"}, new String[]{"maKhachHang","hoTenKH","soDienThoai","gioiTinh","email"}, "maKhachHang=varchar:5","hoTenKH=nvarchar:100","soDienThoai=varchar:10","gioiTinh=bit:","email=varchar:254","diaChi=nvarchar:255");
        define("PhuongTien", new String[]{"VIN"}, new String[]{"VIN","bienSo","hangXe","dongXe","mauXe","namSanXuat","soMay","soKM"}, "VIN=varchar:17","bienSo=varchar:8","hangXe=nvarchar:100","dongXe=nvarchar:100","mauXe=nvarchar:50","namSanXuat=int:","soMay=varchar:100","soKM=decimal:12,2");
        define("DichVu", new String[]{"maDichVu"}, new String[]{"maDichVu","tenDichVu","donGia","trangThai"}, "maDichVu=varchar:5","tenDichVu=nvarchar:150","moTa=nvarchar:500","donGia=decimal:18,2","trangThai=nvarchar:50");
        define("PhuTung", new String[]{"maPhuTung"}, new String[]{"maPhuTung","tenPhuTung","donGia","loaiPhuTung"}, "maPhuTung=varchar:5","tenPhuTung=nvarchar:150","donGia=decimal:18,2","loaiPhuTung=nvarchar:100");
        define("NhaCungCap", new String[]{"maNCC"}, new String[]{"maNCC","tenNCC","soDienThoai"}, "maNCC=varchar:6","tenNCC=nvarchar:150","soDienThoai=varchar:10","email=varchar:254","diaChi=nvarchar:255");
        define("PhuongThucThanhToan", new String[]{"maPhuongThuc"}, new String[]{"maPhuongThuc","tenPhuongThuc"}, "maPhuongThuc=varchar:5","tenPhuongThuc=nvarchar:100");
        define("TaiKhoan", new String[]{"maTaiKhoan"}, new String[]{"maTaiKhoan","maNhanVien","tenDangNhap","matKhau","trangThai","ngayTao"}, "maTaiKhoan=varchar:5","maNhanVien=varchar:5","tenDangNhap=varchar:50","matKhau=varchar:255","trangThai=nvarchar:30","ngayTao=datetime2:0");
        define("TonKho", new String[]{"maTonKho"}, new String[]{"maTonKho","maPhuTung","soLuongHienTai","soLuongToiThieu"}, "maTonKho=varchar:6","maPhuTung=varchar:5","soLuongHienTai=int:","soLuongToiThieu=int:");
        define("MaGiamGia", new String[]{"maGiamGia"}, new String[]{"maGiamGia","maKhachHang","mucGiam","ngayTao","ngayHetHan","trangThai"}, "maGiamGia=varchar:6","maKhachHang=varchar:5","mucGiam=decimal:5,2","ngayTao=datetime2:0","ngayHetHan=datetime2:0","trangThai=nvarchar:50");
        define("PhieuTiepNhan", new String[]{"maPTN"}, new String[]{"maPTN","maKhachHang","maNhanVien","VIN","ngayTiepNhan","soODO"}, "maPTN=varchar:6","maKhachHang=varchar:5","maNhanVien=varchar:5","VIN=varchar:17","ngayTiepNhan=datetime2:0","soODO=varchar:6");
        define("ChiTietPhieuTiepNhan", new String[]{"maPTN","maDichVu"}, new String[]{"maPTN","maDichVu","yeuCauDichVu"}, "maPTN=varchar:6","maDichVu=varchar:5","yeuCauDichVu=nvarchar:500");
        define("PhieuKiemTra", new String[]{"maPKT"}, new String[]{"maPKT","maPTN","maNhanVien","maKhachHang","VIN","ngayKiemTra"}, "maPKT=varchar:6","maPTN=varchar:6","maNhanVien=varchar:5","maKhachHang=varchar:5","VIN=varchar:17","ngayKiemTra=datetime2:0");
        define("ChiTietPhieuKiemTra", new String[]{"maPKT","maDichVu"}, new String[]{"maPKT","maDichVu","hangMucKiemTra","ketQuaChuanDoan"}, "maPKT=varchar:6","maDichVu=varchar:5","hangMucKiemTra=nvarchar:255","ketQuaChuanDoan=nvarchar:500","ketQuaKiemTra=nvarchar:20");
        define("PhieuBaoGia", new String[]{"maBaoGia"}, new String[]{"maBaoGia","maPKT","maNhanVien","VIN","ngayLap","ngayHetHan","trangThai"}, "maBaoGia=varchar:6","maPKT=varchar:6","maNhanVien=varchar:5","VIN=varchar:17","ngayLap=datetime2:0","ngayHetHan=datetime2:0","thoiGianXacNhan=datetime2:0","trangThai=nvarchar:50");
        define("ChiTietPhieuBaoGia", new String[]{"maBaoGia","maDichVu","maPhuTung"}, new String[]{"maBaoGia","maDichVu","maPhuTung","soLuongPhuTung","donGiaPhuTung","chiPhiNhanCong","trangThai"}, "maBaoGia=varchar:6","maDichVu=varchar:5","maPhuTung=varchar:5","soLuongPhuTung=int:","donGiaPhuTung=decimal:18,2","chiPhiNhanCong=decimal:18,2","trangThai=nvarchar:100");
        define("PhieuSuaChua", new String[]{"maPSC"}, new String[]{"maPSC","maBaoGia","maKhachHang","VIN","maNhanVien","ngayLap","trangThai"}, "maPSC=varchar:6","maBaoGia=varchar:6","maKhachHang=varchar:5","VIN=varchar:17","maNhanVien=varchar:5","ngayLap=datetime2:0","ngayHoanThanh=datetime2:0","trangThai=nvarchar:50");
        define("ChiTietPhieuSuaChua", new String[]{"maPSC","maDichVu","maPhuTung"}, new String[]{"maPSC","maDichVu","maPhuTung","trangThaiHangMuc","soLuongPhuTung","ketQuaNghiemThu","thoiHanBaoHanh"}, "maPSC=varchar:6","maDichVu=varchar:5","maPhuTung=varchar:5","trangThaiHangMuc=nvarchar:50","soLuongPhuTung=int:","ketQuaNghiemThu=nvarchar:20","ngayBatDauBaoHanh=datetime2:0","thoiHanBaoHanh=int:","ngayHetHanBaoHanh=datetime2:0");
        define("PhieuNhapKho", new String[]{"maPNK"}, new String[]{"maPNK","maNCC","maNhanVien","ngayNhap","trangThai"}, "maPNK=varchar:6","maNCC=varchar:6","maNhanVien=varchar:5","ngayNhap=datetime2:0","trangThai=nvarchar:30");
        define("ChiTietPhieuNhapKho", new String[]{"maPNK","maPhuTung"}, new String[]{"maPNK","maPhuTung","soLuongNhap","donGiaNhap"}, "maPNK=varchar:6","maPhuTung=varchar:5","soLuongNhap=int:","donGiaNhap=decimal:18,2");
        define("HoaDon", new String[]{"maHoaDon"}, new String[]{"maHoaDon","maKhachHang","maNhanVien","ngayLap","tienPhuTung","tienCong","dichVuKhac","thueVAT","giamGia","trangThaiThanhToan","maPhuongThuc"}, "maHoaDon=varchar:5","maGiamGia=varchar:6","maKhachHang=varchar:5","maNhanVien=varchar:5","maPSC=varchar:6","VIN=varchar:17","ngayLap=datetime2:0","tienPhuTung=decimal:18,2","tienCong=decimal:18,2","dichVuKhac=decimal:18,2","thueVAT=decimal:18,2","giamGia=decimal:18,2","trangThaiThanhToan=nvarchar:50","maPhuongThuc=varchar:5");
=======
        define("NhanVien", new String[] { "maNhanVien" },
                new String[] { "maNhanVien", "hoTenNV", "ngaySinh", "soDienThoai", "email", "vaiTro", "gioiTinh",
                        "trangThai" },
                "maNhanVien=varchar:5", "hoTenNV=nvarchar:100", "ngaySinh=date:", "soDienThoai=varchar:10",
                "email=varchar:254", "diaChi=nvarchar:255", "vaiTro=nvarchar:50", "gioiTinh=bit:",
                "trangThai=nvarchar:30");
        define("KhachHang", new String[] { "maKhachHang" },
                new String[] { "maKhachHang", "hoTenKH", "soDienThoai", "gioiTinh", "email" }, "maKhachHang=varchar:5",
                "hoTenKH=nvarchar:100", "soDienThoai=varchar:10", "gioiTinh=bit:", "email=varchar:254",
                "diaChi=nvarchar:255");
        define("PhuongTien", new String[] { "VIN" },
                new String[] { "VIN", "bienSo", "hangXe", "dongXe", "mauXe", "namSanXuat", "soMay", "soKM" },
                "VIN=varchar:17", "bienSo=varchar:8", "hangXe=nvarchar:100", "dongXe=nvarchar:100", "mauXe=nvarchar:50",
                "namSanXuat=int:", "soMay=varchar:100", "soKM=decimal:12,2");
        define("DichVu", new String[] { "maDichVu" }, new String[] { "maDichVu", "tenDichVu", "donGia", "trangThai" },
                "maDichVu=varchar:5", "tenDichVu=nvarchar:150", "moTa=nvarchar:500", "donGia=decimal:18,2",
                "trangThai=nvarchar:50");
        define("PhuTung", new String[] { "maPhuTung" },
                new String[] { "maPhuTung", "tenPhuTung", "donGia", "loaiPhuTung", "thoiHanBaoHanh" },
                "maPhuTung=varchar:5", "tenPhuTung=nvarchar:150", "donGia=decimal:18,2", "loaiPhuTung=nvarchar:100",
                "thoiHanBaoHanh=int:");
        define("NhaCungCap", new String[] { "maNCC" }, new String[] { "maNCC", "tenNCC", "soDienThoai" },
                "maNCC=varchar:6", "tenNCC=nvarchar:150", "soDienThoai=varchar:10", "email=varchar:254",
                "diaChi=nvarchar:255");
        define("PhuongThucThanhToan", new String[] { "maPhuongThuc" }, new String[] { "maPhuongThuc", "tenPhuongThuc" },
                "maPhuongThuc=varchar:5", "tenPhuongThuc=nvarchar:100");
        define("TaiKhoan", new String[] { "maTaiKhoan" },
                new String[] { "maTaiKhoan", "maNhanVien", "tenDangNhap", "matKhau", "trangThai", "ngayTao" },
                "maTaiKhoan=varchar:5", "maNhanVien=varchar:5", "tenDangNhap=varchar:50", "matKhau=varchar:255",
                "trangThai=nvarchar:30", "ngayTao=datetime2:0");
        define("TonKho", new String[] { "maTonKho" },
                new String[] { "maTonKho", "maPhuTung", "soLuongHienTai", "soLuongToiThieu" }, "maTonKho=varchar:6",
                "maPhuTung=varchar:5", "soLuongHienTai=int:", "soLuongToiThieu=int:");
        define("MaGiamGia", new String[] { "maGiamGia" },
                new String[] { "maGiamGia", "maKhachHang", "mucGiam", "ngayTao", "ngayHetHan", "trangThai" },
                "maGiamGia=varchar:6", "maKhachHang=varchar:5", "mucGiam=decimal:5,2", "ngayTao=datetime2:0",
                "ngayHetHan=datetime2:0", "trangThai=nvarchar:50");
        define("PhieuTiepNhan", new String[] { "maPTN" },
                new String[] { "maPTN", "maKhachHang", "maNhanVien", "VIN", "ngayTiepNhan", "soODO" },
                "maPTN=varchar:6", "maKhachHang=varchar:5", "maNhanVien=varchar:5", "VIN=varchar:17",
                "ngayTiepNhan=datetime2:0", "soODO=varchar:6");
        define("ChiTietPhieuTiepNhan", new String[] { "maPTN", "maDichVu" },
                new String[] { "maPTN", "maDichVu", "yeuCauDichVu" }, "maPTN=varchar:6", "maDichVu=varchar:5",
                "yeuCauDichVu=nvarchar:500");
        define("PhieuKiemTra", new String[] { "maPKT" },
                new String[] { "maPKT", "maPTN", "maNhanVien", "maKhachHang", "VIN", "ngayKiemTra" }, "maPKT=varchar:6",
                "maPTN=varchar:6", "maNhanVien=varchar:5", "maKhachHang=varchar:5", "VIN=varchar:17",
                "ngayKiemTra=datetime2:0");
        define("ChiTietPhieuKiemTra", new String[] { "maPKT", "maDichVu" },
                new String[] { "maPKT", "maDichVu", "hangMucKiemTra", "ketQuaChuanDoan" }, "maPKT=varchar:6",
                "maDichVu=varchar:5", "hangMucKiemTra=nvarchar:255", "ketQuaChuanDoan=nvarchar:500",
                "ketQuaKiemTra=nvarchar:20");
        define("PhieuBaoGia", new String[] { "maBaoGia" },
                new String[] { "maBaoGia", "maPKT", "maKhachHang", "maNhanVien", "VIN", "ngayLap", "ngayHetHan",
                        "trangThai" },
                "maBaoGia=varchar:6", "maPKT=varchar:6", "maKhachHang=varchar:5", "maNhanVien=varchar:5",
                "VIN=varchar:17", "ngayLap=datetime2:0", "ngayHetHan=datetime2:0", "thoiGianXacNhan=datetime2:0",
                "trangThai=nvarchar:50");
        define("ChiTietPhieuBaoGia", new String[] { "maBaoGia", "maDichVu", "maPhuTung" },
                new String[] { "maBaoGia", "maDichVu", "maPhuTung", "soLuongPhuTung", "donGiaPhuTung", "chiPhiNhanCong",
                        "trangThai" },
                "maBaoGia=varchar:6", "maDichVu=varchar:5", "maPhuTung=varchar:5", "soLuongPhuTung=int:",
                "donGiaPhuTung=decimal:18,2", "chiPhiNhanCong=decimal:18,2", "trangThai=nvarchar:100");
        define("PhieuSuaChua", new String[] { "maPSC" },
                new String[] { "maPSC", "maBaoGia", "maKhachHang", "VIN", "maNhanVien", "ngayLap", "trangThai" },
                "maPSC=varchar:6", "maBaoGia=varchar:6", "maKhachHang=varchar:5", "VIN=varchar:17",
                "maNhanVien=varchar:5", "ngayLap=datetime2:0", "ngayHoanThanh=datetime2:0", "trangThai=nvarchar:50");
        define("ChiTietPhieuSuaChua", new String[] { "maPSC", "maDichVu", "maPhuTung" },
                new String[] { "maPSC", "maDichVu", "maPhuTung", "trangThaiHangMuc", "soLuongPhuTung",
                        "ketQuaNghiemThu", "thoiHanBaoHanh" },
                "maPSC=varchar:6", "maDichVu=varchar:5", "maPhuTung=varchar:5", "trangThaiHangMuc=nvarchar:50",
                "soLuongPhuTung=int:", "ketQuaNghiemThu=nvarchar:20", "ngayBatDauBaoHanh=datetime2:0",
                "thoiHanBaoHanh=int:", "ngayHetHanBaoHanh=datetime2:0");
        define("PhieuNhapKho", new String[] { "maPNK" },
                new String[] { "maPNK", "maNCC", "maNhanVien", "ngayNhap", "trangThai" }, "maPNK=varchar:6",
                "maNCC=varchar:6", "maNhanVien=varchar:5", "ngayNhap=datetime2:0", "trangThai=nvarchar:30");
        define("ChiTietPhieuNhapKho", new String[] { "maPNK", "maPhuTung" },
                new String[] { "maPNK", "maPhuTung", "soLuongNhap", "donGiaNhap" }, "maPNK=varchar:6",
                "maPhuTung=varchar:5", "soLuongNhap=int:", "donGiaNhap=decimal:18,2");
        define("HoaDon", new String[] { "maHoaDon" },
                new String[] { "maHoaDon", "maKhachHang", "maNhanVien", "ngayLap", "tienPhuTung", "tienCong",
                        "dichVuKhac", "thueVAT", "giamGia", "trangThaiThanhToan", "maPhuongThuc" },
                "maHoaDon=varchar:5", "maGiamGia=varchar:6", "maKhachHang=varchar:5", "maNhanVien=varchar:5",
                "maPSC=varchar:6", "VIN=varchar:17", "ngayLap=datetime2:0", "tienPhuTung=decimal:18,2",
                "tienCong=decimal:18,2", "dichVuKhac=decimal:18,2", "thueVAT=decimal:18,2", "giamGia=decimal:18,2",
                "trangThaiThanhToan=nvarchar:50", "maPhuongThuc=varchar:5");
>>>>>>> Stashed changes
        IDENTITY.add("ChiTietHoaDon");
        define("ChiTietHoaDon", new String[] { "maChiTietHoaDon" },
                new String[] { "maChiTietHoaDon", "maHoaDon", "soLuong", "donGia" }, "maChiTietHoaDon=int:",
                "maHoaDon=varchar:5", "maDichVu=varchar:5", "maPhuTung=varchar:5", "soLuong=int:",
                "donGia=decimal:18,2");
        define("LichBaoHanh", new String[] { "maLBH" },
                new String[] { "maLBH", "maPSC", "maNhanVien", "maKhachHang", "VIN", "ngayXuLy", "ketQuaXuLy" },
                "maLBH=varchar:6", "maPSC=varchar:6", "maNhanVien=varchar:5", "maKhachHang=varchar:5", "VIN=varchar:17",
                "ngayXuLy=datetime2:0", "ketQuaXuLy=nvarchar:500", "ghiChu=nvarchar:500");
        define("LichNhacBaoDuong", new String[] { "maLichNhac" },
                new String[] { "maLichNhac", "maKhachHang", "maPSC", "VIN", "ngayNhac", "noiDung", "trangThai" },
                "maLichNhac=varchar:7", "maKhachHang=varchar:5", "maPSC=varchar:6", "VIN=varchar:17",
                "ngayNhac=datetime2:0", "noiDung=nvarchar:500", "trangThai=nvarchar:50");
        define("LichSuTichDiem", new String[] { "maLichSuTD" },
                new String[] { "maLichSuTD", "maKhachHang", "maHoaDon", "ngayTichDiem", "soDiem", "lyDo" },
                "maLichSuTD=varchar:5", "maKhachHang=varchar:5", "maHoaDon=varchar:5", "ngayTichDiem=datetime2:0",
                "soDiem=int:", "lyDo=nvarchar:255");
    }

    private static void define(String table, String[] keys, String[] required, String... specs) {
        Map<String, String> types = new LinkedHashMap<>();
        for (String spec : specs) {
            String[] pair = spec.split("=", 2);
            types.put(pair[0], pair[1]);
        }
        COLUMNS.put(table, types.keySet().toArray(String[]::new));
        TYPES.put(table, types);
        PRIMARY.put(table, keys[0]);
        KEYS.put(table, keys);
        REQUIRED.put(table, new HashSet<>(Arrays.asList(required)));
    }
}