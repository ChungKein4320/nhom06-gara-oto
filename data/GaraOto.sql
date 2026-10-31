-- ============================================================================
-- FILE SQL DUY NHẤT: TẠO / CHỌN DATABASE, TẠO SCHEMA VÀ NẠP DỮ LIỆU MẪU [GaraOto]
-- Tự tạo database nếu chưa có; không xóa database hoặc ghi đè bản ghi đã tồn tại.
-- Thứ tự chạy: database -> sequence/bảng -> khóa và trigger/view -> dữ liệu mẫu -> kiểm tra.
-- Kiểu cột SQL ánh xạ từ Entity Java: BigDecimal -> decimal; Timestamp -> datetime2(0); Date -> date; Integer -> int; boolean -> bit; String -> varchar/nvarchar.
-- Gồm 27 bảng, 36 phiếu nhập kho lịch sử, dữ liệu UC009 và mẫu hóa đơn bán lẻ.
-- ============================================================================
USE [master];
GO
IF DB_ID(N'GaraOto') IS NULL
BEGIN
    PRINT N'[SETUP] Database [GaraOto] chưa tồn tại. Đang khởi tạo database mới...';
    CREATE DATABASE [GaraOto];
    PRINT N'[SETUP] Đã tạo database [GaraOto] thành công.';
END
ELSE
BEGIN
    PRINT N'[SETUP] Database [GaraOto] đã tồn tại. Tiến hành kiểm tra, đồng bộ và nâng cấp schema...';
END
GO
USE [GaraOto];
GO
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET XACT_ABORT ON;
GO

-- ============================================================================
-- 1. TẠO SEQUENCE VÀ CÁC BẢNG (IDEMPOTENT)
-- ============================================================================
BEGIN TRY
    BEGIN TRANSACTION;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_NhanVien' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_NhanVien] AS int START WITH 14 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_KhachHang' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_KhachHang] AS int START WITH 61 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_DichVu' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_DichVu] AS int START WITH 21 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_PhuTung' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_PhuTung] AS int START WITH 51 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_NhaCungCap' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_NhaCungCap] AS int START WITH 13 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_PhuongThucThanhToan' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_PhuongThucThanhToan] AS int START WITH 5 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_TaiKhoan' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_TaiKhoan] AS int START WITH 14 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_TonKho' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_TonKho] AS int START WITH 51 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_MaGiamGia' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_MaGiamGia] AS int START WITH 37 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_PhieuTiepNhan' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_PhieuTiepNhan] AS int START WITH 101 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_PhieuKiemTra' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_PhieuKiemTra] AS int START WITH 91 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_PhieuBaoGia' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_PhieuBaoGia] AS int START WITH 81 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_PhieuSuaChua' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_PhieuSuaChua] AS int START WITH 71 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_YeuCauNhapKho' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_YeuCauNhapKho] AS int START WITH 14 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_PhieuNhapKho' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_PhieuNhapKho] AS int START WITH 3 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_HoaDon' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_HoaDon] AS int START WITH 31 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_LichBaoHanh' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_LichBaoHanh] AS int START WITH 21 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_LichNhacBaoDuong' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_LichNhacBaoDuong] AS int START WITH 41 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;
IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = N'Seq_LichSuTichDiem' AND schema_id = SCHEMA_ID(N'dbo'))
    CREATE SEQUENCE dbo.[Seq_LichSuTichDiem] AS int START WITH 21 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 NO CYCLE;

-- 01. NhanVien
IF OBJECT_ID(N'dbo.[NhanVien]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[NhanVien] (
    [maNhanVien] varchar(5) NOT NULL CONSTRAINT [DF_NhanVien_Ma] DEFAULT ('NV' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_NhanVien]),3)),
    [hoTenNV] nvarchar(100) NOT NULL,
    [ngaySinh] date NOT NULL,
    [soDienThoai] varchar(10) NOT NULL,
    [email] varchar(254) NOT NULL,
    [diaChi] nvarchar(255) NULL,
    [vaiTro] nvarchar(50) NOT NULL,
    [gioiTinh] bit NOT NULL,
    [trangThai] nvarchar(30) NOT NULL,
    CONSTRAINT [PK_NhanVien] PRIMARY KEY ([maNhanVien]),
    CONSTRAINT [CK_NhanVien_Ma] CHECK (DATALENGTH([maNhanVien]) = 5 AND [maNhanVien] COLLATE Latin1_General_100_BIN2 LIKE 'NV[0-9][0-9][0-9]'),
    CONSTRAINT [CK_NhanVien_HoTen] CHECK (LEN(LTRIM(RTRIM([hoTenNV]))) > 0),
    CONSTRAINT [CK_NhanVien_Tuoi] CHECK (ngaySinh <= DATEADD(year,-18,CONVERT(date,SYSDATETIME())) AND ngaySinh > DATEADD(year,-66,CONVERT(date,SYSDATETIME()))),
    CONSTRAINT [CK_NhanVien_SDT] CHECK (DATALENGTH([soDienThoai]) = 10 AND [soDienThoai] COLLATE Latin1_General_100_BIN2 NOT LIKE '%[^0-9]%' AND LEFT([soDienThoai],2) IN ('03','05','07','08','09')),
    CONSTRAINT [CK_NhanVien_Email] CHECK ([email] LIKE '%_@_%._%' AND [email] NOT LIKE '% %' AND LEN([email])-LEN(REPLACE([email], '@', ''))=1),
    CONSTRAINT [CK_NhanVien_VaiTro] CHECK ([vaiTro] IN (N'Nhân viên kỹ thuật', N'Nhân viên kho', N'Nhân viên bán hàng', N'Nhân viên quản lý')),
    CONSTRAINT [CK_NhanVien_TrangThai] CHECK ([trangThai] IN (N'Đang làm', N'Đã nghỉ'))
);
END;

-- 02. KhachHang
IF OBJECT_ID(N'dbo.[KhachHang]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[KhachHang] (
    [maKhachHang] varchar(5) NOT NULL CONSTRAINT [DF_KhachHang_Ma] DEFAULT ('KH' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_KhachHang]),3)),
    [hoTenKH] nvarchar(100) NOT NULL,
    [soDienThoai] varchar(10) NOT NULL,
    [gioiTinh] bit NOT NULL,
    [email] varchar(254) NOT NULL,
    [diaChi] nvarchar(255) NULL,
    CONSTRAINT [PK_KhachHang] PRIMARY KEY ([maKhachHang]),
    CONSTRAINT [CK_KhachHang_Ma] CHECK (DATALENGTH([maKhachHang]) = 5 AND [maKhachHang] COLLATE Latin1_General_100_BIN2 LIKE 'KH[0-9][0-9][0-9]'),
    CONSTRAINT [CK_KhachHang_HoTen] CHECK (LEN(LTRIM(RTRIM([hoTenKH]))) > 0),
    CONSTRAINT [CK_KhachHang_SDT] CHECK (DATALENGTH([soDienThoai]) = 10 AND [soDienThoai] COLLATE Latin1_General_100_BIN2 NOT LIKE '%[^0-9]%' AND LEFT([soDienThoai],2) IN ('03','05','07','08','09')),
    CONSTRAINT [CK_KhachHang_Email] CHECK ([email] LIKE '%_@_%._%' AND [email] NOT LIKE '% %' AND LEN([email])-LEN(REPLACE([email], '@', ''))=1)
);
END;

-- 03. PhuongTien
IF OBJECT_ID(N'dbo.[PhuongTien]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[PhuongTien] (
    [VIN] varchar(17) NOT NULL,
    [maKhachHang] varchar(5) NULL,
    [bienSo] varchar(8) NOT NULL,
    [hangXe] nvarchar(100) NOT NULL,
    [dongXe] nvarchar(100) NOT NULL,
    [mauXe] nvarchar(50) NOT NULL,
    [namSanXuat] int NOT NULL,
    [soMay] varchar(100) NOT NULL,
    [soKM] decimal(12,2) NOT NULL,
    CONSTRAINT [PK_PhuongTien] PRIMARY KEY ([VIN]),
    CONSTRAINT [UQ_PhuongTien_BienSo] UNIQUE ([bienSo]),
    CONSTRAINT [UQ_PhuongTien_SoMay] UNIQUE ([soMay]),
    CONSTRAINT [CK_PhuongTien_VIN] CHECK (DATALENGTH(VIN)=17 AND VIN COLLATE Latin1_General_100_BIN2 NOT LIKE '%[^A-HJ-NPR-Z0-9]%'),
    CONSTRAINT [CK_PhuongTien_BienSo] CHECK (DATALENGTH(bienSo)=8 AND bienSo COLLATE Latin1_General_100_BIN2 LIKE '[0-9][0-9][A-Z][0-9][0-9][0-9][0-9][0-9]'),
    CONSTRAINT [CK_PhuongTien_HangXe] CHECK (LEN(LTRIM(RTRIM([hangXe]))) > 0),
    CONSTRAINT [CK_PhuongTien_DongXe] CHECK (LEN(LTRIM(RTRIM([dongXe]))) > 0),
    CONSTRAINT [CK_PhuongTien_MauXe] CHECK (LEN(LTRIM(RTRIM([mauXe]))) > 0),
    CONSTRAINT [CK_PhuongTien_Nam] CHECK (namSanXuat BETWEEN 1886 AND YEAR(SYSDATETIME())),
    CONSTRAINT [CK_PhuongTien_SoMay] CHECK (LEN(LTRIM(RTRIM([soMay]))) > 0),
    CONSTRAINT [CK_PhuongTien_SoKM] CHECK (soKM>=0)
);
END;

-- 04. DichVu
IF OBJECT_ID(N'dbo.[DichVu]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[DichVu] (
    [maDichVu] varchar(5) NOT NULL CONSTRAINT [DF_DichVu_Ma] DEFAULT ('DV' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_DichVu]),3)),
    [tenDichVu] nvarchar(150) NOT NULL,
    [moTa] nvarchar(500) NULL,
    [donGia] decimal(18,2) NOT NULL,
    [trangThai] nvarchar(50) NOT NULL,
    CONSTRAINT [PK_DichVu] PRIMARY KEY ([maDichVu]),
    CONSTRAINT [CK_DichVu_Ma] CHECK (DATALENGTH([maDichVu]) = 5 AND [maDichVu] COLLATE Latin1_General_100_BIN2 LIKE 'DV[0-9][0-9][0-9]'),
    CONSTRAINT [CK_DichVu_Ten] CHECK (LEN(LTRIM(RTRIM([tenDichVu]))) > 0),
    CONSTRAINT [CK_DichVu_Gia] CHECK (donGia>=0),
    CONSTRAINT [CK_DichVu_TrangThai] CHECK (LEN(LTRIM(RTRIM([trangThai]))) > 0)
);
END;

-- 05. PhuTung
IF OBJECT_ID(N'dbo.[PhuTung]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[PhuTung] (
    [maPhuTung] varchar(5) NOT NULL CONSTRAINT [DF_PhuTung_Ma] DEFAULT ('PT' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_PhuTung]),3)),
    [tenPhuTung] nvarchar(150) NOT NULL,
    [donGia] decimal(18,2) NOT NULL,
    [loaiPhuTung] nvarchar(100) NOT NULL,
    [thoiHanBaoHanh] int NOT NULL CONSTRAINT [DF_PhuTung_ThoiHanBaoHanh] DEFAULT (0),
    CONSTRAINT [PK_PhuTung] PRIMARY KEY ([maPhuTung]),
    CONSTRAINT [CK_PhuTung_Ma] CHECK (DATALENGTH([maPhuTung]) = 5 AND [maPhuTung] COLLATE Latin1_General_100_BIN2 LIKE 'PT[0-9][0-9][0-9]'),
    CONSTRAINT [CK_PhuTung_Ten] CHECK (LEN(LTRIM(RTRIM([tenPhuTung]))) > 0),
    CONSTRAINT [CK_PhuTung_Gia] CHECK (donGia>=0),
    CONSTRAINT [CK_PhuTung_Loai] CHECK (LEN(LTRIM(RTRIM([loaiPhuTung]))) > 0)
);
END;

-- 06. NhaCungCap
IF OBJECT_ID(N'dbo.[NhaCungCap]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[NhaCungCap] (
    [maNCC] varchar(6) NOT NULL CONSTRAINT [DF_NhaCungCap_Ma] DEFAULT ('NCC' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_NhaCungCap]),3)),
    [tenNCC] nvarchar(150) NOT NULL,
    [soDienThoai] varchar(10) NOT NULL,
    [email] varchar(254) NULL,
    [diaChi] nvarchar(255) NULL,
    CONSTRAINT [PK_NhaCungCap] PRIMARY KEY ([maNCC]),
    CONSTRAINT [CK_NhaCungCap_Ma] CHECK (DATALENGTH([maNCC]) = 6 AND [maNCC] COLLATE Latin1_General_100_BIN2 LIKE 'NCC[0-9][0-9][0-9]'),
    CONSTRAINT [CK_NhaCungCap_Ten] CHECK (LEN(LTRIM(RTRIM([tenNCC]))) > 0),
    CONSTRAINT [CK_NhaCungCap_SDT] CHECK (DATALENGTH([soDienThoai]) = 10 AND [soDienThoai] COLLATE Latin1_General_100_BIN2 NOT LIKE '%[^0-9]%' AND LEFT([soDienThoai],2) IN ('03','05','07','08','09')),
    CONSTRAINT [CK_NhaCungCap_Email] CHECK ([email] IS NULL OR ([email] LIKE '%_@_%._%' AND [email] NOT LIKE '% %' AND LEN([email])-LEN(REPLACE([email], '@', ''))=1))
);
END;

-- 07. PhuongThucThanhToan
IF OBJECT_ID(N'dbo.[PhuongThucThanhToan]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[PhuongThucThanhToan] (
    [maPhuongThuc] varchar(5) NOT NULL CONSTRAINT [DF_PhuongThucThanhToan_Ma] DEFAULT ('TT' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_PhuongThucThanhToan]),3)),
    [tenPhuongThuc] nvarchar(100) NOT NULL,
    CONSTRAINT [PK_PhuongThucThanhToan] PRIMARY KEY ([maPhuongThuc]),
    CONSTRAINT [CK_PhuongThucThanhToan_Ma] CHECK (DATALENGTH([maPhuongThuc]) = 5 AND [maPhuongThuc] COLLATE Latin1_General_100_BIN2 LIKE 'TT[0-9][0-9][0-9]'),
    CONSTRAINT [CK_PhuongThucThanhToan_Ten] CHECK (LEN(LTRIM(RTRIM([tenPhuongThuc]))) > 0)
);
END;

-- 08. TaiKhoan
IF OBJECT_ID(N'dbo.[TaiKhoan]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[TaiKhoan] (
    [maTaiKhoan] varchar(5) NOT NULL CONSTRAINT [DF_TaiKhoan_Ma] DEFAULT ('TK' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_TaiKhoan]),3)),
    [maNhanVien] varchar(5) NOT NULL,
    [tenDangNhap] varchar(50) NOT NULL,
    [matKhau] varchar(255) NOT NULL,
    [trangThai] nvarchar(30) NOT NULL,
    [ngayTao] datetime2(0) NOT NULL,
    CONSTRAINT [PK_TaiKhoan] PRIMARY KEY ([maTaiKhoan]),
    CONSTRAINT [UQ_TaiKhoan_TenDangNhap] UNIQUE ([tenDangNhap]),
    CONSTRAINT [UQ_TaiKhoan_NhanVien] UNIQUE ([maNhanVien]),
    CONSTRAINT [CK_TaiKhoan_Ma] CHECK (DATALENGTH([maTaiKhoan]) = 5 AND [maTaiKhoan] COLLATE Latin1_General_100_BIN2 LIKE 'TK[0-9][0-9][0-9]'),
    CONSTRAINT [CK_TaiKhoan_TenDangNhap] CHECK (LEN(LTRIM(RTRIM([tenDangNhap]))) > 0),
    CONSTRAINT [CK_TaiKhoan_TrangThai] CHECK ([trangThai] IN (N'Đang hoạt động', N'Ngừng hoạt động'))
);
END;

-- 09. TonKho
IF OBJECT_ID(N'dbo.[TonKho]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[TonKho] (
    [maTonKho] varchar(6) NOT NULL CONSTRAINT [DF_TonKho_Ma] DEFAULT ('TKH' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_TonKho]),3)),
    [maPhuTung] varchar(5) NOT NULL,
    [soLuongHienTai] int NOT NULL,
    [soLuongToiThieu] int NOT NULL,
    CONSTRAINT [PK_TonKho] PRIMARY KEY ([maTonKho]),
    CONSTRAINT [UQ_TonKho_PhuTung] UNIQUE ([maPhuTung]),
    CONSTRAINT [CK_TonKho_Ma] CHECK (DATALENGTH([maTonKho]) = 6 AND [maTonKho] COLLATE Latin1_General_100_BIN2 LIKE 'TKH[0-9][0-9][0-9]'),
    CONSTRAINT [CK_TonKho_SoLuong] CHECK (soLuongHienTai>=0),
    CONSTRAINT [CK_TonKho_ToiThieu] CHECK (soLuongToiThieu>=0),
    CONSTRAINT [CK_TonKho_KhongPT000] CHECK ([maPhuTung]<>'PT000')
);
END;

-- 10. MaGiamGia
IF OBJECT_ID(N'dbo.[MaGiamGia]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[MaGiamGia] (
    [maGiamGia] varchar(6) NOT NULL CONSTRAINT [DF_MaGiamGia_Ma] DEFAULT ('MGG' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_MaGiamGia]),3)),
    [maKhachHang] varchar(5) NOT NULL,
    [mucGiam] decimal(5,2) NOT NULL,
    [ngayTao] datetime2(0) NOT NULL,
    [ngayHetHan] datetime2(0) NOT NULL,
    [trangThai] nvarchar(50) NOT NULL,
    [mocDiem] int NULL,
    [ngayBatDauChuKy] datetime2(0) NULL,
    CONSTRAINT [PK_MaGiamGia] PRIMARY KEY ([maGiamGia]),
    CONSTRAINT [UQ_MaGiamGia_MaKhachHang] UNIQUE ([maGiamGia], [maKhachHang]),
    CONSTRAINT [CK_MaGiamGia_Ma] CHECK (DATALENGTH([maGiamGia]) = 6 AND [maGiamGia] COLLATE Latin1_General_100_BIN2 LIKE 'MGG[0-9][0-9][0-9]'),
    CONSTRAINT [CK_MaGiamGia_MucGiam] CHECK (mucGiam BETWEEN 0 AND 100),
    CONSTRAINT [CK_MaGiamGia_Ngay] CHECK (ngayTao<=SYSDATETIME() AND ngayHetHan>ngayTao),
    CONSTRAINT [CK_MaGiamGia_TrangThai] CHECK (LEN(LTRIM(RTRIM([trangThai]))) > 0)
);
END;

-- 11. PhieuTiepNhan
IF OBJECT_ID(N'dbo.[PhieuTiepNhan]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[PhieuTiepNhan] (
    [maPTN] varchar(6) NOT NULL CONSTRAINT [DF_PhieuTiepNhan_Ma] DEFAULT ('PTN' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_PhieuTiepNhan]),3)),
    [maKhachHang] varchar(5) NOT NULL,
    [maNhanVien] varchar(5) NOT NULL,
    [VIN] varchar(17) NOT NULL,
    [ngayTiepNhan] datetime2(0) NOT NULL,
    [soODO] varchar(6) NOT NULL,
    CONSTRAINT [PK_PhieuTiepNhan] PRIMARY KEY ([maPTN]),
    CONSTRAINT [UQ_PhieuTiepNhan_HoSo] UNIQUE ([maPTN], [maKhachHang], [VIN]),
    CONSTRAINT [CK_PhieuTiepNhan_Ma] CHECK (DATALENGTH([maPTN]) = 6 AND [maPTN] COLLATE Latin1_General_100_BIN2 LIKE 'PTN[0-9][0-9][0-9]'),
    CONSTRAINT [CK_PhieuTiepNhan_ODO] CHECK (DATALENGTH(soODO)=6 AND soODO COLLATE Latin1_General_100_BIN2 NOT LIKE '%[^0-9]%'),
    CONSTRAINT [CK_PhieuTiepNhan_Ngay] CHECK (ngayTiepNhan<=SYSDATETIME())
);
END;

-- 12. ChiTietPhieuTiepNhan
IF OBJECT_ID(N'dbo.[ChiTietPhieuTiepNhan]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[ChiTietPhieuTiepNhan] (
    [maPTN] varchar(6) NOT NULL,
    [maDichVu] varchar(5) NOT NULL,
    [yeuCauDichVu] nvarchar(500) NOT NULL,
    CONSTRAINT [PK_ChiTietPhieuTiepNhan] PRIMARY KEY ([maPTN], [maDichVu]),
    CONSTRAINT [CK_ChiTietPhieuTiepNhan_YeuCau] CHECK (LEN(LTRIM(RTRIM([yeuCauDichVu]))) > 0)
);
END;

-- 13. PhieuKiemTra
IF OBJECT_ID(N'dbo.[PhieuKiemTra]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[PhieuKiemTra] (
    [maPKT] varchar(6) NOT NULL CONSTRAINT [DF_PhieuKiemTra_Ma] DEFAULT ('PKT' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_PhieuKiemTra]),3)),
    [maPTN] varchar(6) NOT NULL,
    [maNhanVien] varchar(5) NOT NULL,
    [maKhachHang] varchar(5) NOT NULL,
    [VIN] varchar(17) NOT NULL,
    [ngayKiemTra] datetime2(0) NOT NULL,
    CONSTRAINT [PK_PhieuKiemTra] PRIMARY KEY ([maPKT]),
    CONSTRAINT [UQ_PhieuKiemTra_HoSo] UNIQUE ([maPKT], [maKhachHang], [VIN]),
    CONSTRAINT [CK_PhieuKiemTra_Ma] CHECK (DATALENGTH([maPKT]) = 6 AND [maPKT] COLLATE Latin1_General_100_BIN2 LIKE 'PKT[0-9][0-9][0-9]'),
    CONSTRAINT [CK_PhieuKiemTra_Ngay] CHECK (ngayKiemTra<=SYSDATETIME())
);
END;

-- 14. ChiTietPhieuKiemTra
IF OBJECT_ID(N'dbo.[ChiTietPhieuKiemTra]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[ChiTietPhieuKiemTra] (
    [maPKT] varchar(6) NOT NULL,
    [maDichVu] varchar(5) NOT NULL,
    [hangMucKiemTra] nvarchar(255) NOT NULL,
    [ketQuaChuanDoan] nvarchar(500) NOT NULL,
    [ketQuaKiemTra] nvarchar(20) NULL,
    CONSTRAINT [PK_ChiTietPhieuKiemTra] PRIMARY KEY ([maPKT], [maDichVu]),
    CONSTRAINT [CK_ChiTietPhieuKiemTra_HangMuc] CHECK (LEN(LTRIM(RTRIM([hangMucKiemTra]))) > 0),
    CONSTRAINT [CK_ChiTietPhieuKiemTra_ChanDoan] CHECK (LEN(LTRIM(RTRIM([ketQuaChuanDoan]))) > 0),
    CONSTRAINT [CK_ChiTietPhieuKiemTra_KetQua] CHECK (ketQuaKiemTra IS NULL OR ketQuaKiemTra IN (N'Đạt',N'Không đạt'))
);
END;

-- 15. PhieuBaoGia
IF OBJECT_ID(N'dbo.[PhieuBaoGia]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[PhieuBaoGia] (
    [maBaoGia] varchar(6) NOT NULL CONSTRAINT [DF_PhieuBaoGia_Ma] DEFAULT ('PBG' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_PhieuBaoGia]),3)),
    [maPKT] varchar(6) NOT NULL,
    [maKhachHang] varchar(5) NOT NULL,
    [maNhanVien] varchar(5) NOT NULL,
    [VIN] varchar(17) NOT NULL,
    [ngayLap] datetime2(0) NOT NULL,
    [ngayHetHan] datetime2(0) NOT NULL,
    [thoiGianXacNhan] datetime2(0) NULL,
    [trangThai] nvarchar(50) NOT NULL,
    CONSTRAINT [PK_PhieuBaoGia] PRIMARY KEY ([maBaoGia]),
    CONSTRAINT [UQ_PhieuBaoGia_HoSo] UNIQUE ([maBaoGia], [maKhachHang], [VIN]),
    CONSTRAINT [CK_PhieuBaoGia_Ma] CHECK (DATALENGTH([maBaoGia]) = 6 AND [maBaoGia] COLLATE Latin1_General_100_BIN2 LIKE 'PBG[0-9][0-9][0-9]'),
    CONSTRAINT [CK_PhieuBaoGia_Ngay] CHECK (ngayLap<=SYSDATETIME() AND ngayHetHan>=ngayLap AND (thoiGianXacNhan IS NULL OR thoiGianXacNhan>=ngayLap)),
    CONSTRAINT [CK_PhieuBaoGia_TrangThai] CHECK ([trangThai] IN (N'Còn hiệu lực', N'Hết hiệu lực', N'Đã xác nhận'))
);
END;

-- 16. ChiTietPhieuBaoGia
IF OBJECT_ID(N'dbo.[ChiTietPhieuBaoGia]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[ChiTietPhieuBaoGia] (
    [maBaoGia] varchar(6) NOT NULL,
    [maDichVu] varchar(5) NOT NULL,
    [maPhuTung] varchar(5) NOT NULL,
    [soLuongPhuTung] int NOT NULL,
    [donGiaPhuTung] decimal(18,2) NOT NULL,
    [chiPhiNhanCong] decimal(18,2) NOT NULL,
    [trangThai] nvarchar(100) NOT NULL,
    CONSTRAINT [PK_ChiTietPhieuBaoGia] PRIMARY KEY ([maBaoGia], [maDichVu], [maPhuTung]),
    CONSTRAINT [CK_ChiTietPhieuBaoGia_SoLuong] CHECK (([maPhuTung]='PT000' AND soLuongPhuTung=0) OR ([maPhuTung]<>'PT000' AND soLuongPhuTung>0)),
    CONSTRAINT [CK_ChiTietPhieuBaoGia_DonGia] CHECK (donGiaPhuTung>=0 AND ([maPhuTung]<>'PT000' OR donGiaPhuTung=0)),
    CONSTRAINT [CK_ChiTietPhieuBaoGia_NhanCong] CHECK (chiPhiNhanCong>=0),
    CONSTRAINT [CK_ChiTietPhieuBaoGia_TrangThai] CHECK ([trangThai] IN (N'Chờ xác nhận', N'Chờ sửa chữa', N'Thiếu phụ tùng', N'Thiếu phụ tùng + Chờ xác nhận'))
);
END;

-- 17. PhieuSuaChua
IF OBJECT_ID(N'dbo.[PhieuSuaChua]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[PhieuSuaChua] (
    [maPSC] varchar(6) NOT NULL CONSTRAINT [DF_PhieuSuaChua_Ma] DEFAULT ('PSC' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_PhieuSuaChua]),3)),
    [maBaoGia] varchar(6) NOT NULL,
    [maKhachHang] varchar(5) NOT NULL,
    [VIN] varchar(17) NOT NULL,
    [maNhanVien] varchar(5) NOT NULL,
    [maNguoiDuyetHuy] varchar(5) NULL,
    [ngayLap] datetime2(0) NOT NULL,
    [ngayHoanThanh] datetime2(0) NULL,
    [trangThai] nvarchar(50) NOT NULL,
    [lyDoHuy] nvarchar(500) NULL,
    CONSTRAINT [PK_PhieuSuaChua] PRIMARY KEY ([maPSC]),
    CONSTRAINT [UQ_PhieuSuaChua_HoSo] UNIQUE ([maPSC], [maKhachHang], [VIN]),
    CONSTRAINT [CK_PhieuSuaChua_Ma] CHECK (DATALENGTH([maPSC]) = 6 AND [maPSC] COLLATE Latin1_General_100_BIN2 LIKE 'PSC[0-9][0-9][0-9]'),
    CONSTRAINT [CK_PhieuSuaChua_Ngay] CHECK (ngayLap<=SYSDATETIME() AND (ngayHoanThanh IS NULL OR ngayHoanThanh>=ngayLap)),
    CONSTRAINT [CK_PhieuSuaChua_TrangThai] CHECK ([trangThai] IN (N'Chờ sửa chữa', N'Đang sửa chữa', N'Đã sửa xong', N'Chờ thanh toán', N'Đã thanh toán', N'Hoàn tất', N'Đã hủy'))
);
END;

-- 18. ChiTietPhieuSuaChua
IF OBJECT_ID(N'dbo.[ChiTietPhieuSuaChua]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[ChiTietPhieuSuaChua] (
    [maPSC] varchar(6) NOT NULL,
    [maDichVu] varchar(5) NOT NULL,
    [maPhuTung] varchar(5) NOT NULL,
    [trangThaiHangMuc] nvarchar(50) NOT NULL,
    [soLuongPhuTung] int NOT NULL,
    [soLuongCapPhat] int NOT NULL CONSTRAINT [DF_ChiTietPSC_CapPhat] DEFAULT (0),
    [soLuongSuDung] int NOT NULL CONSTRAINT [DF_ChiTietPSC_SuDung] DEFAULT (0),
    [soLuongHoanTra] int NOT NULL CONSTRAINT [DF_ChiTietPSC_HoanTra] DEFAULT (0),
    [ngayCapPhat] datetime2(0) NULL,
    [maNhanVienCapPhat] varchar(5) NULL,
    [ngayHoanTra] datetime2(0) NULL,
    [maNhanVienXacNhanHoanTra] varchar(5) NULL,
    [ketQuaNghiemThu] nvarchar(20) NOT NULL,
    [ngayBatDauBaoHanh] datetime2(0) NULL,
    [thoiHanBaoHanh] int NOT NULL,
    [ngayHetHanBaoHanh] datetime2(0) NULL,
    CONSTRAINT [PK_ChiTietPhieuSuaChua] PRIMARY KEY ([maPSC], [maDichVu], [maPhuTung]),
    CONSTRAINT [CK_ChiTietPhieuSuaChua_TrangThai] CHECK ([trangThaiHangMuc] IN (N'Chờ sửa chữa', N'Đang sửa chữa', N'Thiếu phụ tùng', N'Hoàn thành')),
    CONSTRAINT [CK_ChiTietPhieuSuaChua_SoLuong] CHECK (([maPhuTung]='PT000' AND soLuongPhuTung=0) OR ([maPhuTung]<>'PT000' AND soLuongPhuTung>0)),
    CONSTRAINT [CK_ChiTietPSC_DoiSoat] CHECK (soLuongCapPhat>=0 AND soLuongSuDung>=0 AND soLuongHoanTra>=0 AND soLuongSuDung+soLuongHoanTra<=soLuongCapPhat AND soLuongCapPhat<=soLuongPhuTung AND (soLuongCapPhat=0 OR (ngayCapPhat IS NOT NULL AND maNhanVienCapPhat IS NOT NULL)) AND (soLuongHoanTra=0 OR (ngayHoanTra IS NOT NULL AND maNhanVienXacNhanHoanTra IS NOT NULL))),
    CONSTRAINT [CK_ChiTietPhieuSuaChua_KetQua] CHECK ([ketQuaNghiemThu] IN (N'Đạt', N'Không đạt')),
    CONSTRAINT [CK_ChiTietPhieuSuaChua_ThoiHan] CHECK (thoiHanBaoHanh>=0),
    CONSTRAINT [CK_ChiTietPhieuSuaChua_NgayBaoHanh] CHECK ((ngayBatDauBaoHanh IS NULL AND ngayHetHanBaoHanh IS NULL) OR (ngayBatDauBaoHanh IS NOT NULL AND ngayHetHanBaoHanh IS NOT NULL AND ngayHetHanBaoHanh>=ngayBatDauBaoHanh))
);
END;

-- 19a. YeuCauNhapKho
IF OBJECT_ID(N'dbo.[YeuCauNhapKho]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[YeuCauNhapKho] (
    [maYCNK] varchar(6) NOT NULL CONSTRAINT [DF_YeuCauNhapKho_Ma] DEFAULT ('YCN' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_YeuCauNhapKho]),3)),
    [maNCC] varchar(6) NOT NULL,
    [maNhanVien] varchar(5) NOT NULL,
    [ngayTao] datetime2(0) NOT NULL CONSTRAINT [DF_YeuCauNhapKho_NgayTao] DEFAULT (SYSDATETIME()),
    [trangThai] nvarchar(30) NOT NULL CONSTRAINT [DF_YeuCauNhapKho_TrangThai] DEFAULT (N'Chờ duyệt'),
    [maNhanVienDuyet] varchar(5) NULL,
    [ngayDuyet] datetime2(0) NULL,
    [ghiChu] nvarchar(255) NULL,
    CONSTRAINT [PK_YeuCauNhapKho] PRIMARY KEY ([maYCNK]),
    CONSTRAINT [CK_YeuCauNhapKho_Ma] CHECK (DATALENGTH([maYCNK]) = 6 AND [maYCNK] COLLATE Latin1_General_100_BIN2 LIKE 'YCN[0-9][0-9][0-9]'),
    CONSTRAINT [CK_YeuCauNhapKho_NgayTao] CHECK (ngayTao<=SYSDATETIME()),
    CONSTRAINT [CK_YeuCauNhapKho_TrangThai] CHECK ([trangThai] IN (N'Chờ duyệt', N'Đã duyệt', N'Từ chối', N'Đã xử lý')),
    CONSTRAINT [CK_YeuCauNhapKho_NgayDuyet] CHECK (ngayDuyet IS NULL OR ngayDuyet>=ngayTao)
);
END;

-- 19b. ChiTietYeuCauNhapKho
IF OBJECT_ID(N'dbo.[ChiTietYeuCauNhapKho]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[ChiTietYeuCauNhapKho] (
    [maYCNK] varchar(6) NOT NULL,
    [maPhuTung] varchar(5) NOT NULL,
    [soLuongYeuCau] int NOT NULL,
    [donGiaDuKien] decimal(18,2) NOT NULL,
    CONSTRAINT [PK_ChiTietYeuCauNhapKho] PRIMARY KEY ([maYCNK], [maPhuTung]),
    CONSTRAINT [CK_ChiTietYeuCauNhapKho_SoLuong] CHECK (soLuongYeuCau>0),
    CONSTRAINT [CK_ChiTietYeuCauNhapKho_DonGia] CHECK (donGiaDuKien>=0),
    CONSTRAINT [CK_ChiTietYeuCauNhapKho_KhongPT000] CHECK ([maPhuTung]<>'PT000')
);
END;

-- 19. PhieuNhapKho
IF OBJECT_ID(N'dbo.[PhieuNhapKho]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[PhieuNhapKho] (
    [maPNK] varchar(6) NOT NULL CONSTRAINT [DF_PhieuNhapKho_Ma] DEFAULT ('PNK' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_PhieuNhapKho]),3)),
    [maNCC] varchar(6) NOT NULL,
    [maNhanVien] varchar(5) NOT NULL,
    [ngayNhap] datetime2(0) NOT NULL,
    [trangThai] nvarchar(30) NOT NULL,
    [maYCNK] varchar(6) NULL,
    CONSTRAINT [PK_PhieuNhapKho] PRIMARY KEY ([maPNK]),
    CONSTRAINT [CK_PhieuNhapKho_Ma] CHECK (DATALENGTH([maPNK]) = 6 AND [maPNK] COLLATE Latin1_General_100_BIN2 LIKE 'PNK[0-9][0-9][0-9]'),
    CONSTRAINT [CK_PhieuNhapKho_Ngay] CHECK (ngayNhap<=SYSDATETIME()),
    CONSTRAINT [CK_PhieuNhapKho_TrangThai] CHECK (
        (
            [maYCNK] IS NOT NULL
            AND [trangThai] IN (
                N'Chờ xác nhận nhập kho',
                N'Đã nhập kho',
                N'Đã hủy'
            )
        )
        OR
        (
            [maYCNK] IS NULL
            AND [trangThai] IN (
                N'Chờ duyệt',
                N'Đã duyệt',
                N'Không duyệt'
            )
        )
    )
);
END;

-- 20. ChiTietPhieuNhapKho
IF OBJECT_ID(N'dbo.[ChiTietPhieuNhapKho]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[ChiTietPhieuNhapKho] (
    [maPNK] varchar(6) NOT NULL,
    [maPhuTung] varchar(5) NOT NULL,
    [soLuongNhap] int NOT NULL,
    [donGiaNhap] decimal(18,2) NOT NULL,
    [soLuongYeuCau] int NULL,
    [soLuongThucNhan] int NULL,
    CONSTRAINT [PK_ChiTietPhieuNhapKho] PRIMARY KEY ([maPNK], [maPhuTung]),
    CONSTRAINT [CK_ChiTietPhieuNhapKho_SoLuong] CHECK (soLuongNhap>=0),
    CONSTRAINT [CK_ChiTietPhieuNhapKho_DonGia] CHECK (donGiaNhap>=0),
    CONSTRAINT [CK_ChiTietPhieuNhapKho_KhongPT000] CHECK ([maPhuTung]<>'PT000'),
    CONSTRAINT [CK_ChiTietPhieuNhapKho_SoLuongYeuCau] CHECK (soLuongYeuCau IS NULL OR soLuongYeuCau>0),
    CONSTRAINT [CK_ChiTietPhieuNhapKho_SoLuongThucNhan] CHECK (soLuongThucNhan IS NULL OR soLuongThucNhan>=0),
    CONSTRAINT [CK_ChiTietPhieuNhapKho_HopLeThucNhan] CHECK (soLuongThucNhan IS NULL OR (soLuongNhap<=soLuongThucNhan AND (soLuongYeuCau IS NULL OR soLuongThucNhan<=soLuongYeuCau)))
);
END;

-- 21. HoaDon
IF OBJECT_ID(N'dbo.[HoaDon]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[HoaDon] (
    [maHoaDon] varchar(5) NOT NULL CONSTRAINT [DF_HoaDon_Ma] DEFAULT ('HD' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_HoaDon]),3)),
    [maGiamGia] varchar(6) NULL,
    [maKhachHang] varchar(5) NOT NULL,
    [maNhanVien] varchar(5) NOT NULL,
    [maPSC] varchar(6) NULL,
    [VIN] varchar(17) NULL,
    [ngayLap] datetime2(0) NOT NULL,
    [tienPhuTung] decimal(18,2) NOT NULL,
    [tienCong] decimal(18,2) NOT NULL,
    [dichVuKhac] decimal(18,2) NOT NULL,
    [thueVAT] decimal(18,2) NOT NULL,
    [giamGia] decimal(18,2) NULL,
    [trangThaiThanhToan] nvarchar(50) NOT NULL,
    [maPhuongThuc] varchar(5) NOT NULL,
    CONSTRAINT [PK_HoaDon] PRIMARY KEY ([maHoaDon]),
    CONSTRAINT [CK_HoaDon_Ma] CHECK (DATALENGTH([maHoaDon]) = 5 AND [maHoaDon] COLLATE Latin1_General_100_BIN2 LIKE 'HD[0-9][0-9][0-9]'),
    CONSTRAINT [CK_HoaDon_Ngay] CHECK (ngayLap<=SYSDATETIME()),
    CONSTRAINT [CK_HoaDon_Tien] CHECK (tienPhuTung>=0 AND tienCong>=0 AND dichVuKhac>=0 AND thueVAT>=0 AND giamGia>=0),
    CONSTRAINT [CK_HoaDon_GiamGia] CHECK (giamGia<=tienPhuTung+tienCong+dichVuKhac+thueVAT),
    CONSTRAINT [CK_HoaDon_TrangThai] CHECK (LEN(LTRIM(RTRIM([trangThaiThanhToan]))) > 0)
);
END;

-- 22. ChiTietHoaDon
IF OBJECT_ID(N'dbo.[ChiTietHoaDon]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[ChiTietHoaDon] (
    [maHoaDon] varchar(5) NOT NULL,
    [maDichVu] varchar(5) NULL,
    [maPhuTung] varchar(5) NULL,
    [soLuong] int NOT NULL,
    [donGia] decimal(18,2) NOT NULL,
    CONSTRAINT [CK_ChiTietHoaDon_NoiDung] CHECK (maDichVu IS NOT NULL OR maPhuTung IS NOT NULL),
    CONSTRAINT [CK_ChiTietHoaDon_KhongPT000] CHECK (maPhuTung IS NULL OR maPhuTung<>'PT000'),
    CONSTRAINT [CK_ChiTietHoaDon_SoLuong] CHECK (soLuong>=0),
    CONSTRAINT [CK_ChiTietHoaDon_DonGia] CHECK (donGia>=0)
);
END;

-- 23. LichBaoHanh
IF OBJECT_ID(N'dbo.[LichBaoHanh]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[LichBaoHanh] (
    [maLBH] varchar(6) NOT NULL CONSTRAINT [DF_LichBaoHanh_Ma] DEFAULT ('LBH' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_LichBaoHanh]),3)),
    [maPSC] varchar(6) NOT NULL,
    [maDichVu] varchar(5) NULL,
    [maPhuTung] varchar(5) NULL,
    [maNhanVien] varchar(5) NOT NULL,
    [maKhachHang] varchar(5) NOT NULL,
    [VIN] varchar(17) NOT NULL,
    [ngayXuLy] datetime2(0) NOT NULL,
    [ketQuaXuLy] nvarchar(500) NOT NULL,
    [ghiChu] nvarchar(500) NULL,
    CONSTRAINT [PK_LichBaoHanh] PRIMARY KEY ([maLBH]),
    CONSTRAINT [CK_LichBaoHanh_Ma] CHECK (DATALENGTH([maLBH]) = 6 AND [maLBH] COLLATE Latin1_General_100_BIN2 LIKE 'LBH[0-9][0-9][0-9]'),
    CONSTRAINT [CK_LichBaoHanh_Ngay] CHECK (ngayXuLy<=SYSDATETIME()),
    CONSTRAINT [CK_LichBaoHanh_HangMuc] CHECK ((maDichVu IS NULL AND maPhuTung IS NULL) OR (maDichVu IS NOT NULL AND maPhuTung IS NOT NULL)),
    CONSTRAINT [CK_LichBaoHanh_KetQua] CHECK (LEN(LTRIM(RTRIM([ketQuaXuLy]))) > 0)
);
END;

-- 24. LichNhacBaoDuong
IF OBJECT_ID(N'dbo.[LichNhacBaoDuong]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[LichNhacBaoDuong] (
    [maLichNhac] varchar(7) NOT NULL CONSTRAINT [DF_LichNhacBaoDuong_Ma] DEFAULT ('LNBD' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_LichNhacBaoDuong]),3)),
    [maKhachHang] varchar(5) NOT NULL,
    [maPSC] varchar(6) NOT NULL,
    [VIN] varchar(17) NOT NULL,
    [ngayNhac] datetime2(0) NOT NULL,
    [noiDung] nvarchar(500) NOT NULL,
    [trangThai] nvarchar(50) NOT NULL,
    CONSTRAINT [PK_LichNhacBaoDuong] PRIMARY KEY ([maLichNhac]),
    CONSTRAINT [CK_LichNhacBaoDuong_Ma] CHECK (DATALENGTH([maLichNhac]) = 7 AND [maLichNhac] COLLATE Latin1_General_100_BIN2 LIKE 'LNBD[0-9][0-9][0-9]'),
    CONSTRAINT [CK_LichNhacBaoDuong_NoiDung] CHECK (LEN(LTRIM(RTRIM([noiDung]))) > 0),
    CONSTRAINT [CK_LichNhacBaoDuong_TrangThai] CHECK (LEN(LTRIM(RTRIM([trangThai]))) > 0)
);
END;

-- 25. LichSuTichDiem
IF OBJECT_ID(N'dbo.[LichSuTichDiem]', N'U') IS NULL
BEGIN
CREATE TABLE dbo.[LichSuTichDiem] (
    [maLichSuTD] varchar(5) NOT NULL CONSTRAINT [DF_LichSuTichDiem_Ma] DEFAULT ('LS' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_LichSuTichDiem]),3)),
    [maKhachHang] varchar(5) NOT NULL,
    [maHoaDon] varchar(5) NOT NULL,
    [ngayTichDiem] datetime2(0) NOT NULL,
    [ngayHetHan] datetime2(0) NULL,
    [soDiem] int NOT NULL,
    [lyDo] nvarchar(255) NOT NULL,
    CONSTRAINT [PK_LichSuTichDiem] PRIMARY KEY ([maLichSuTD]),
    CONSTRAINT [CK_LichSuTichDiem_Ma] CHECK (DATALENGTH([maLichSuTD]) = 5 AND [maLichSuTD] COLLATE Latin1_General_100_BIN2 LIKE 'LS[0-9][0-9][0-9]'),
    CONSTRAINT [CK_LichSuTichDiem_Ngay] CHECK (ngayTichDiem<=SYSDATETIME()),
    CONSTRAINT [CK_LichSuTichDiem_Diem] CHECK (soDiem>=0),
    CONSTRAINT [CK_LichSuTichDiem_LyDo] CHECK (LEN(LTRIM(RTRIM([lyDo]))) > 0)
);
END;


    COMMIT TRANSACTION;
    PRINT N'[SCHEMA] Sequence và bảng đã sẵn sàng.';
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

-- ============================================================================
-- 2. BỔ SUNG CỘT SCHEMA CHO DATABASE ĐÃ TỒN TẠI
-- ============================================================================
BEGIN TRY
    BEGIN TRANSACTION;

-- ============================================================================
-- A.1. BỔ SUNG CỘT SCHEMA CHO DATABASE ĐÃ CÓ
-- ============================================================================
-- 1. Bổ sung các cột nghiệp vụ upstream (sửa chữa, phương tiện, mã giảm giá, tích điểm, bảo hành)
IF COL_LENGTH(N'dbo.PhuTung', N'thoiHanBaoHanh') IS NULL
    ALTER TABLE dbo.PhuTung ADD thoiHanBaoHanh int NOT NULL CONSTRAINT DF_PhuTung_ThoiHanBaoHanh DEFAULT (0) WITH VALUES;
IF COL_LENGTH(N'dbo.PhuongTien', N'maKhachHang') IS NULL
    ALTER TABLE dbo.PhuongTien ADD maKhachHang varchar(5) NULL;
IF COL_LENGTH(N'dbo.PhieuBaoGia', N'maKhachHang') IS NULL
    ALTER TABLE dbo.PhieuBaoGia ADD maKhachHang varchar(5) NULL;
IF COL_LENGTH(N'dbo.MaGiamGia', N'mocDiem') IS NULL
    ALTER TABLE dbo.MaGiamGia ADD mocDiem int NULL;
IF COL_LENGTH(N'dbo.MaGiamGia', N'ngayBatDauChuKy') IS NULL
    ALTER TABLE dbo.MaGiamGia ADD ngayBatDauChuKy datetime2(0) NULL;
IF COL_LENGTH(N'dbo.ChiTietPhieuSuaChua', N'soLuongCapPhat') IS NULL
    ALTER TABLE dbo.ChiTietPhieuSuaChua ADD soLuongCapPhat int NOT NULL CONSTRAINT DF_Mig_ChiTietPSC_CapPhat DEFAULT(0) WITH VALUES;
IF COL_LENGTH(N'dbo.ChiTietPhieuSuaChua', N'soLuongSuDung') IS NULL
    ALTER TABLE dbo.ChiTietPhieuSuaChua ADD soLuongSuDung int NOT NULL CONSTRAINT DF_Mig_ChiTietPSC_SuDung DEFAULT(0) WITH VALUES;
IF COL_LENGTH(N'dbo.ChiTietPhieuSuaChua', N'soLuongHoanTra') IS NULL
    ALTER TABLE dbo.ChiTietPhieuSuaChua ADD soLuongHoanTra int NOT NULL CONSTRAINT DF_Mig_ChiTietPSC_HoanTra DEFAULT(0) WITH VALUES;
IF COL_LENGTH(N'dbo.ChiTietPhieuSuaChua', N'ngayCapPhat') IS NULL
    ALTER TABLE dbo.ChiTietPhieuSuaChua ADD ngayCapPhat datetime2(0) NULL;
IF COL_LENGTH(N'dbo.ChiTietPhieuSuaChua', N'maNhanVienCapPhat') IS NULL
    ALTER TABLE dbo.ChiTietPhieuSuaChua ADD maNhanVienCapPhat varchar(5) NULL;
IF COL_LENGTH(N'dbo.ChiTietPhieuSuaChua', N'ngayHoanTra') IS NULL
    ALTER TABLE dbo.ChiTietPhieuSuaChua ADD ngayHoanTra datetime2(0) NULL;
IF COL_LENGTH(N'dbo.ChiTietPhieuSuaChua', N'maNhanVienXacNhanHoanTra') IS NULL
    ALTER TABLE dbo.ChiTietPhieuSuaChua ADD maNhanVienXacNhanHoanTra varchar(5) NULL;
IF COL_LENGTH(N'dbo.PhieuSuaChua', N'maNguoiDuyetHuy') IS NULL
    ALTER TABLE dbo.PhieuSuaChua ADD maNguoiDuyetHuy varchar(5) NULL;
IF COL_LENGTH(N'dbo.PhieuSuaChua', N'lyDoHuy') IS NULL
    ALTER TABLE dbo.PhieuSuaChua ADD lyDoHuy nvarchar(500) NULL;
IF COL_LENGTH(N'dbo.LichBaoHanh', N'maDichVu') IS NULL
    ALTER TABLE dbo.LichBaoHanh ADD maDichVu varchar(5) NULL;
IF COL_LENGTH(N'dbo.LichBaoHanh', N'maPhuTung') IS NULL
    ALTER TABLE dbo.LichBaoHanh ADD maPhuTung varchar(5) NULL;
IF COL_LENGTH(N'dbo.LichSuTichDiem', N'ngayHetHan') IS NULL
    ALTER TABLE dbo.LichSuTichDiem ADD ngayHetHan datetime2(0) NULL;

-- 2. Bổ sung các cột nghiệp vụ UC009 cho PhieuNhapKho và ChiTietPhieuNhapKho
IF COL_LENGTH(N'dbo.PhieuNhapKho', N'maYCNK') IS NULL
    ALTER TABLE dbo.PhieuNhapKho ADD maYCNK varchar(6) NULL;
IF COL_LENGTH(N'dbo.ChiTietPhieuNhapKho', N'soLuongYeuCau') IS NULL
    ALTER TABLE dbo.ChiTietPhieuNhapKho ADD soLuongYeuCau int NULL;
IF COL_LENGTH(N'dbo.ChiTietPhieuNhapKho', N'soLuongThucNhan') IS NULL
    ALTER TABLE dbo.ChiTietPhieuNhapKho ADD soLuongThucNhan int NULL;

    COMMIT TRANSACTION;
    PRINT N'[SCHEMA] Bổ sung cột mới thành công.';
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

-- ============================================================================
-- 3. BACKFILL, KHÓA NGOẠI, RÀNG BUỘC CHECK VÀ CHỈ MỤC
-- ============================================================================
BEGIN TRY
    BEGIN TRANSACTION;

-- ============================================================================
-- A.2. BACKFILL DỮ LIỆU LỊCH SỬ CHO DATABASE ĐÃ CÓ
-- ============================================================================
-- 1. Backfill an toàn cho các bản ghi lịch sử trong ChiTietPhieuNhapKho
UPDATE dbo.[ChiTietPhieuNhapKho]
SET [soLuongYeuCau] = COALESCE([soLuongYeuCau], [soLuongNhap]),
    [soLuongThucNhan] = COALESCE([soLuongThucNhan], [soLuongNhap])
WHERE [soLuongYeuCau] IS NULL OR [soLuongThucNhan] IS NULL;

-- Gán chủ sở hữu mẫu cho xe chưa có quan hệ; giữ nguyên chủ xe đã được lưu.
;WITH CustomerOrder AS (
    SELECT maKhachHang,ROW_NUMBER() OVER (ORDER BY maKhachHang) AS rn
    FROM dbo.KhachHang
), CustomerCount AS (
    SELECT COUNT_BIG(*) AS total FROM CustomerOrder
), VehicleOrder AS (
    SELECT VIN,ROW_NUMBER() OVER (ORDER BY VIN) AS rn
    FROM dbo.PhuongTien
    WHERE maKhachHang IS NULL
)
UPDATE vehicle
SET maKhachHang=customer.maKhachHang
FROM dbo.PhuongTien vehicle
JOIN VehicleOrder v ON v.VIN=vehicle.VIN
CROSS JOIN CustomerCount countRows
JOIN CustomerOrder customer ON customer.rn=((v.rn-1)%NULLIF(countRows.total,0))+1;

-- 2. Backfill maKhachHang cho PhieuBaoGia & đồng bộ ràng buộc hồ sơ (HoSo) 3 cột
IF COL_LENGTH(N'dbo.PhieuBaoGia', N'maKhachHang') IS NOT NULL
BEGIN
    UPDATE b SET b.maKhachHang = k.maKhachHang
    FROM dbo.PhieuBaoGia b JOIN dbo.PhieuKiemTra k ON k.maPKT = b.maPKT
    WHERE b.maKhachHang IS NULL;

    IF COLUMNPROPERTY(OBJECT_ID(N'dbo.PhieuBaoGia'), N'maKhachHang', 'AllowsNull') = 1
        ALTER TABLE dbo.PhieuBaoGia ALTER COLUMN maKhachHang varchar(5) NOT NULL;
END;

IF (SELECT COUNT(*) FROM sys.index_columns WHERE object_id = OBJECT_ID(N'dbo.PhieuKiemTra') AND index_id = INDEXPROPERTY(OBJECT_ID(N'dbo.PhieuKiemTra'), N'UQ_PhieuKiemTra_HoSo', 'IndexID')) < 3
BEGIN
    IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_PhieuBaoGia_HoSoKiemTra')
        ALTER TABLE dbo.PhieuBaoGia DROP CONSTRAINT FK_PhieuBaoGia_HoSoKiemTra;
    ALTER TABLE dbo.PhieuKiemTra DROP CONSTRAINT UQ_PhieuKiemTra_HoSo;
    ALTER TABLE dbo.PhieuKiemTra ADD CONSTRAINT UQ_PhieuKiemTra_HoSo UNIQUE (maPKT, maKhachHang, VIN);
END;

IF (SELECT COUNT(*) FROM sys.index_columns WHERE object_id = OBJECT_ID(N'dbo.PhieuBaoGia') AND index_id = INDEXPROPERTY(OBJECT_ID(N'dbo.PhieuBaoGia'), N'UQ_PhieuBaoGia_HoSo', 'IndexID')) < 3
BEGIN
    IF EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_PhieuSuaChua_HoSoBaoGia')
        ALTER TABLE dbo.PhieuSuaChua DROP CONSTRAINT FK_PhieuSuaChua_HoSoBaoGia;
    ALTER TABLE dbo.PhieuBaoGia DROP CONSTRAINT UQ_PhieuBaoGia_HoSo;
    ALTER TABLE dbo.PhieuBaoGia ADD CONSTRAINT UQ_PhieuBaoGia_HoSo UNIQUE (maBaoGia, maKhachHang, VIN);
END;

-- 3. Backfill các trường bổ sung của upstream
;WITH WarrantyCandidates AS (
    SELECT l.maLBH, MIN(d.maDichVu) AS maDichVu, MIN(d.maPhuTung) AS maPhuTung, COUNT_BIG(*) AS candidateCount
    FROM dbo.LichBaoHanh l JOIN dbo.ChiTietPhieuSuaChua d ON d.maPSC=l.maPSC
    WHERE d.ketQuaNghiemThu=N'Đạt' AND d.ngayBatDauBaoHanh<=l.ngayXuLy AND d.ngayHetHanBaoHanh>=l.ngayXuLy
    GROUP BY l.maLBH
)
UPDATE l SET maDichVu=c.maDichVu,maPhuTung=c.maPhuTung
FROM dbo.LichBaoHanh l JOIN WarrantyCandidates c ON c.maLBH=l.maLBH
WHERE l.maDichVu IS NULL AND l.maPhuTung IS NULL AND c.candidateCount=1;

UPDATE historyRow SET ngayHetHan=DATEADD(year,1,cycleStart.firstEarn)
FROM dbo.LichSuTichDiem historyRow
CROSS APPLY(SELECT MIN(ngayTichDiem) AS firstEarn FROM dbo.LichSuTichDiem WHERE maKhachHang=historyRow.maKhachHang) cycleStart
WHERE historyRow.ngayHetHan IS NULL;



IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_TaiKhoan_NhanVien')
    ALTER TABLE dbo.[TaiKhoan] WITH CHECK ADD CONSTRAINT [FK_TaiKhoan_NhanVien] FOREIGN KEY ([maNhanVien]) REFERENCES dbo.[NhanVien] ([maNhanVien]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_TonKho_PhuTung')
    ALTER TABLE dbo.[TonKho] WITH CHECK ADD CONSTRAINT [FK_TonKho_PhuTung] FOREIGN KEY ([maPhuTung]) REFERENCES dbo.[PhuTung] ([maPhuTung]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_MaGiamGia_KhachHang')
    ALTER TABLE dbo.[MaGiamGia] WITH CHECK ADD CONSTRAINT [FK_MaGiamGia_KhachHang] FOREIGN KEY ([maKhachHang]) REFERENCES dbo.[KhachHang] ([maKhachHang]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_PhieuTiepNhan_KhachHang')
    ALTER TABLE dbo.[PhieuTiepNhan] WITH CHECK ADD CONSTRAINT [FK_PhieuTiepNhan_KhachHang] FOREIGN KEY ([maKhachHang]) REFERENCES dbo.[KhachHang] ([maKhachHang]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_PhieuTiepNhan_NhanVien')
    ALTER TABLE dbo.[PhieuTiepNhan] WITH CHECK ADD CONSTRAINT [FK_PhieuTiepNhan_NhanVien] FOREIGN KEY ([maNhanVien]) REFERENCES dbo.[NhanVien] ([maNhanVien]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_PhieuTiepNhan_PhuongTien')
    ALTER TABLE dbo.[PhieuTiepNhan] WITH CHECK ADD CONSTRAINT [FK_PhieuTiepNhan_PhuongTien] FOREIGN KEY ([VIN]) REFERENCES dbo.[PhuongTien] ([VIN]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_PhuongTien_KhachHang')
    ALTER TABLE dbo.[PhuongTien] WITH CHECK ADD CONSTRAINT [FK_PhuongTien_KhachHang] FOREIGN KEY ([maKhachHang]) REFERENCES dbo.[KhachHang] ([maKhachHang]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietPhieuTiepNhan_PhieuTiepNhan')
    ALTER TABLE dbo.[ChiTietPhieuTiepNhan] WITH CHECK ADD CONSTRAINT [FK_ChiTietPhieuTiepNhan_PhieuTiepNhan] FOREIGN KEY ([maPTN]) REFERENCES dbo.[PhieuTiepNhan] ([maPTN]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietPhieuTiepNhan_DichVu')
    ALTER TABLE dbo.[ChiTietPhieuTiepNhan] WITH CHECK ADD CONSTRAINT [FK_ChiTietPhieuTiepNhan_DichVu] FOREIGN KEY ([maDichVu]) REFERENCES dbo.[DichVu] ([maDichVu]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_PhieuKiemTra_HoSoTiepNhan')
    ALTER TABLE dbo.[PhieuKiemTra] WITH CHECK ADD CONSTRAINT [FK_PhieuKiemTra_HoSoTiepNhan] FOREIGN KEY ([maPTN], [maKhachHang], [VIN]) REFERENCES dbo.[PhieuTiepNhan] ([maPTN], [maKhachHang], [VIN]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_PhieuKiemTra_NhanVien')
    ALTER TABLE dbo.[PhieuKiemTra] WITH CHECK ADD CONSTRAINT [FK_PhieuKiemTra_NhanVien] FOREIGN KEY ([maNhanVien]) REFERENCES dbo.[NhanVien] ([maNhanVien]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietPhieuKiemTra_PhieuKiemTra')
    ALTER TABLE dbo.[ChiTietPhieuKiemTra] WITH CHECK ADD CONSTRAINT [FK_ChiTietPhieuKiemTra_PhieuKiemTra] FOREIGN KEY ([maPKT]) REFERENCES dbo.[PhieuKiemTra] ([maPKT]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietPhieuKiemTra_DichVu')
    ALTER TABLE dbo.[ChiTietPhieuKiemTra] WITH CHECK ADD CONSTRAINT [FK_ChiTietPhieuKiemTra_DichVu] FOREIGN KEY ([maDichVu]) REFERENCES dbo.[DichVu] ([maDichVu]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_PhieuBaoGia_HoSoKiemTra')
    ALTER TABLE dbo.[PhieuBaoGia] WITH CHECK ADD CONSTRAINT [FK_PhieuBaoGia_HoSoKiemTra] FOREIGN KEY ([maPKT], [maKhachHang], [VIN]) REFERENCES dbo.[PhieuKiemTra] ([maPKT], [maKhachHang], [VIN]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_PhieuBaoGia_KhachHang')
    ALTER TABLE dbo.[PhieuBaoGia] WITH CHECK ADD CONSTRAINT [FK_PhieuBaoGia_KhachHang] FOREIGN KEY ([maKhachHang]) REFERENCES dbo.[KhachHang] ([maKhachHang]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_PhieuBaoGia_NhanVien')
    ALTER TABLE dbo.[PhieuBaoGia] WITH CHECK ADD CONSTRAINT [FK_PhieuBaoGia_NhanVien] FOREIGN KEY ([maNhanVien]) REFERENCES dbo.[NhanVien] ([maNhanVien]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietPhieuBaoGia_PhieuBaoGia')
    ALTER TABLE dbo.[ChiTietPhieuBaoGia] WITH CHECK ADD CONSTRAINT [FK_ChiTietPhieuBaoGia_PhieuBaoGia] FOREIGN KEY ([maBaoGia]) REFERENCES dbo.[PhieuBaoGia] ([maBaoGia]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietPhieuBaoGia_DichVu')
    ALTER TABLE dbo.[ChiTietPhieuBaoGia] WITH CHECK ADD CONSTRAINT [FK_ChiTietPhieuBaoGia_DichVu] FOREIGN KEY ([maDichVu]) REFERENCES dbo.[DichVu] ([maDichVu]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietPhieuBaoGia_PhuTung')
    ALTER TABLE dbo.[ChiTietPhieuBaoGia] WITH CHECK ADD CONSTRAINT [FK_ChiTietPhieuBaoGia_PhuTung] FOREIGN KEY ([maPhuTung]) REFERENCES dbo.[PhuTung] ([maPhuTung]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_PhieuSuaChua_HoSoBaoGia')
    ALTER TABLE dbo.[PhieuSuaChua] WITH CHECK ADD CONSTRAINT [FK_PhieuSuaChua_HoSoBaoGia] FOREIGN KEY ([maBaoGia], [maKhachHang], [VIN]) REFERENCES dbo.[PhieuBaoGia] ([maBaoGia], [maKhachHang], [VIN]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_PhieuSuaChua_KhachHang')
    ALTER TABLE dbo.[PhieuSuaChua] WITH CHECK ADD CONSTRAINT [FK_PhieuSuaChua_KhachHang] FOREIGN KEY ([maKhachHang]) REFERENCES dbo.[KhachHang] ([maKhachHang]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_PhieuSuaChua_NhanVien')
    ALTER TABLE dbo.[PhieuSuaChua] WITH CHECK ADD CONSTRAINT [FK_PhieuSuaChua_NhanVien] FOREIGN KEY ([maNhanVien]) REFERENCES dbo.[NhanVien] ([maNhanVien]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_PhieuSuaChua_NguoiDuyetHuy')
    ALTER TABLE dbo.[PhieuSuaChua] WITH CHECK ADD CONSTRAINT [FK_PhieuSuaChua_NguoiDuyetHuy] FOREIGN KEY ([maNguoiDuyetHuy]) REFERENCES dbo.[NhanVien] ([maNhanVien]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietPhieuSuaChua_PhieuSuaChua')
    ALTER TABLE dbo.[ChiTietPhieuSuaChua] WITH CHECK ADD CONSTRAINT [FK_ChiTietPhieuSuaChua_PhieuSuaChua] FOREIGN KEY ([maPSC]) REFERENCES dbo.[PhieuSuaChua] ([maPSC]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietPhieuSuaChua_DichVu')
    ALTER TABLE dbo.[ChiTietPhieuSuaChua] WITH CHECK ADD CONSTRAINT [FK_ChiTietPhieuSuaChua_DichVu] FOREIGN KEY ([maDichVu]) REFERENCES dbo.[DichVu] ([maDichVu]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietPhieuSuaChua_PhuTung')
    ALTER TABLE dbo.[ChiTietPhieuSuaChua] WITH CHECK ADD CONSTRAINT [FK_ChiTietPhieuSuaChua_PhuTung] FOREIGN KEY ([maPhuTung]) REFERENCES dbo.[PhuTung] ([maPhuTung]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietPSC_NhanVienCapPhat')
    ALTER TABLE dbo.[ChiTietPhieuSuaChua] WITH CHECK ADD CONSTRAINT [FK_ChiTietPSC_NhanVienCapPhat] FOREIGN KEY ([maNhanVienCapPhat]) REFERENCES dbo.[NhanVien] ([maNhanVien]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietPSC_NhanVienHoanTra')
    ALTER TABLE dbo.[ChiTietPhieuSuaChua] WITH CHECK ADD CONSTRAINT [FK_ChiTietPSC_NhanVienHoanTra] FOREIGN KEY ([maNhanVienXacNhanHoanTra]) REFERENCES dbo.[NhanVien] ([maNhanVien]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_LichBaoHanh_HangMuc')
    ALTER TABLE dbo.[LichBaoHanh] WITH CHECK ADD CONSTRAINT [FK_LichBaoHanh_HangMuc] FOREIGN KEY ([maPSC],[maDichVu],[maPhuTung]) REFERENCES dbo.[ChiTietPhieuSuaChua] ([maPSC],[maDichVu],[maPhuTung]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_YeuCauNhapKho_NhaCungCap')
    ALTER TABLE dbo.[YeuCauNhapKho] WITH CHECK ADD CONSTRAINT [FK_YeuCauNhapKho_NhaCungCap] FOREIGN KEY ([maNCC]) REFERENCES dbo.[NhaCungCap] ([maNCC]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_YeuCauNhapKho_NhanVien')
    ALTER TABLE dbo.[YeuCauNhapKho] WITH CHECK ADD CONSTRAINT [FK_YeuCauNhapKho_NhanVien] FOREIGN KEY ([maNhanVien]) REFERENCES dbo.[NhanVien] ([maNhanVien]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_YeuCauNhapKho_NhanVienDuyet')
    ALTER TABLE dbo.[YeuCauNhapKho] WITH CHECK ADD CONSTRAINT [FK_YeuCauNhapKho_NhanVienDuyet] FOREIGN KEY ([maNhanVienDuyet]) REFERENCES dbo.[NhanVien] ([maNhanVien]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietYeuCauNhapKho_YeuCauNhapKho')
    ALTER TABLE dbo.[ChiTietYeuCauNhapKho] WITH CHECK ADD CONSTRAINT [FK_ChiTietYeuCauNhapKho_YeuCauNhapKho] FOREIGN KEY ([maYCNK]) REFERENCES dbo.[YeuCauNhapKho] ([maYCNK]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietYeuCauNhapKho_PhuTung')
    ALTER TABLE dbo.[ChiTietYeuCauNhapKho] WITH CHECK ADD CONSTRAINT [FK_ChiTietYeuCauNhapKho_PhuTung] FOREIGN KEY ([maPhuTung]) REFERENCES dbo.[PhuTung] ([maPhuTung]);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UQ_PhieuNhapKho_maYCNK' AND object_id = OBJECT_ID(N'dbo.PhieuNhapKho'))
    CREATE UNIQUE NONCLUSTERED INDEX [UQ_PhieuNhapKho_maYCNK] ON dbo.[PhieuNhapKho] ([maYCNK]) WHERE [maYCNK] IS NOT NULL;
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_PhieuNhapKho_YeuCauNhapKho')
    ALTER TABLE dbo.[PhieuNhapKho] WITH CHECK ADD CONSTRAINT [FK_PhieuNhapKho_YeuCauNhapKho] FOREIGN KEY ([maYCNK]) REFERENCES dbo.[YeuCauNhapKho] ([maYCNK]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_PhieuNhapKho_NhaCungCap')
    ALTER TABLE dbo.[PhieuNhapKho] WITH CHECK ADD CONSTRAINT [FK_PhieuNhapKho_NhaCungCap] FOREIGN KEY ([maNCC]) REFERENCES dbo.[NhaCungCap] ([maNCC]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_PhieuNhapKho_NhanVien')
    ALTER TABLE dbo.[PhieuNhapKho] WITH CHECK ADD CONSTRAINT [FK_PhieuNhapKho_NhanVien] FOREIGN KEY ([maNhanVien]) REFERENCES dbo.[NhanVien] ([maNhanVien]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietPhieuNhapKho_PhieuNhapKho')
    ALTER TABLE dbo.[ChiTietPhieuNhapKho] WITH CHECK ADD CONSTRAINT [FK_ChiTietPhieuNhapKho_PhieuNhapKho] FOREIGN KEY ([maPNK]) REFERENCES dbo.[PhieuNhapKho] ([maPNK]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietPhieuNhapKho_PhuTung')
    ALTER TABLE dbo.[ChiTietPhieuNhapKho] WITH CHECK ADD CONSTRAINT [FK_ChiTietPhieuNhapKho_PhuTung] FOREIGN KEY ([maPhuTung]) REFERENCES dbo.[PhuTung] ([maPhuTung]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_HoaDon_HoSoSuaChua')
    ALTER TABLE dbo.[HoaDon] WITH CHECK ADD CONSTRAINT [FK_HoaDon_HoSoSuaChua] FOREIGN KEY ([maPSC], [maKhachHang], [VIN]) REFERENCES dbo.[PhieuSuaChua] ([maPSC], [maKhachHang], [VIN]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_LichBaoHanh_HoSoSuaChua')
    ALTER TABLE dbo.[LichBaoHanh] WITH CHECK ADD CONSTRAINT [FK_LichBaoHanh_HoSoSuaChua] FOREIGN KEY ([maPSC], [maKhachHang], [VIN]) REFERENCES dbo.[PhieuSuaChua] ([maPSC], [maKhachHang], [VIN]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_LichNhacBaoDuong_HoSoSuaChua')
    ALTER TABLE dbo.[LichNhacBaoDuong] WITH CHECK ADD CONSTRAINT [FK_LichNhacBaoDuong_HoSoSuaChua] FOREIGN KEY ([maPSC], [maKhachHang], [VIN]) REFERENCES dbo.[PhieuSuaChua] ([maPSC], [maKhachHang], [VIN]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_HoaDon_NhanVien')
    ALTER TABLE dbo.[HoaDon] WITH CHECK ADD CONSTRAINT [FK_HoaDon_NhanVien] FOREIGN KEY ([maNhanVien]) REFERENCES dbo.[NhanVien] ([maNhanVien]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_LichBaoHanh_NhanVien')
    ALTER TABLE dbo.[LichBaoHanh] WITH CHECK ADD CONSTRAINT [FK_LichBaoHanh_NhanVien] FOREIGN KEY ([maNhanVien]) REFERENCES dbo.[NhanVien] ([maNhanVien]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_HoaDon_MaGiamGiaKhachHang')
    ALTER TABLE dbo.[HoaDon] WITH CHECK ADD CONSTRAINT [FK_HoaDon_MaGiamGiaKhachHang] FOREIGN KEY ([maGiamGia], [maKhachHang]) REFERENCES dbo.[MaGiamGia] ([maGiamGia], [maKhachHang]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_HoaDon_PhuongThucThanhToan')
    ALTER TABLE dbo.[HoaDon] WITH CHECK ADD CONSTRAINT [FK_HoaDon_PhuongThucThanhToan] FOREIGN KEY ([maPhuongThuc]) REFERENCES dbo.[PhuongThucThanhToan] ([maPhuongThuc]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietHoaDon_HoaDon')
    ALTER TABLE dbo.[ChiTietHoaDon] WITH CHECK ADD CONSTRAINT [FK_ChiTietHoaDon_HoaDon] FOREIGN KEY ([maHoaDon]) REFERENCES dbo.[HoaDon] ([maHoaDon]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietHoaDon_DichVu')
    ALTER TABLE dbo.[ChiTietHoaDon] WITH CHECK ADD CONSTRAINT [FK_ChiTietHoaDon_DichVu] FOREIGN KEY ([maDichVu]) REFERENCES dbo.[DichVu] ([maDichVu]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_ChiTietHoaDon_PhuTung')
    ALTER TABLE dbo.[ChiTietHoaDon] WITH CHECK ADD CONSTRAINT [FK_ChiTietHoaDon_PhuTung] FOREIGN KEY ([maPhuTung]) REFERENCES dbo.[PhuTung] ([maPhuTung]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_LichSuTichDiem_KhachHang')
    ALTER TABLE dbo.[LichSuTichDiem] WITH CHECK ADD CONSTRAINT [FK_LichSuTichDiem_KhachHang] FOREIGN KEY ([maKhachHang]) REFERENCES dbo.[KhachHang] ([maKhachHang]);
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_LichSuTichDiem_HoaDon')
    ALTER TABLE dbo.[LichSuTichDiem] WITH CHECK ADD CONSTRAINT [FK_LichSuTichDiem_HoaDon] FOREIGN KEY ([maHoaDon]) REFERENCES dbo.[HoaDon] ([maHoaDon]);

-- Định danh dòng hóa đơn với các FK tùy chọn, ngăn trùng cùng nội dung một hóa đơn.
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_ChiTietHoaDon_DichVu' AND object_id = OBJECT_ID(N'dbo.ChiTietHoaDon'))
    CREATE UNIQUE INDEX [UX_ChiTietHoaDon_DichVu] ON dbo.[ChiTietHoaDon] (maHoaDon,maDichVu)
    WHERE maDichVu IS NOT NULL AND maPhuTung IS NULL;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_ChiTietHoaDon_PhuTung' AND object_id = OBJECT_ID(N'dbo.ChiTietHoaDon'))
    CREATE UNIQUE INDEX [UX_ChiTietHoaDon_PhuTung] ON dbo.[ChiTietHoaDon] (maHoaDon,maPhuTung)
    WHERE maDichVu IS NULL AND maPhuTung IS NOT NULL;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_ChiTietHoaDon_CaHai' AND object_id = OBJECT_ID(N'dbo.ChiTietHoaDon'))
    CREATE UNIQUE INDEX [UX_ChiTietHoaDon_CaHai] ON dbo.[ChiTietHoaDon] (maHoaDon,maDichVu,maPhuTung)
    WHERE maDichVu IS NOT NULL AND maPhuTung IS NOT NULL;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_PhieuTiepNhan_VIN_Ngay' AND object_id = OBJECT_ID(N'dbo.PhieuTiepNhan'))
    CREATE INDEX [IX_PhieuTiepNhan_VIN_Ngay] ON dbo.[PhieuTiepNhan] (VIN,ngayTiepNhan);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_PhieuTiepNhan_KhachHang' AND object_id = OBJECT_ID(N'dbo.PhieuTiepNhan'))
    CREATE INDEX [IX_PhieuTiepNhan_KhachHang] ON dbo.[PhieuTiepNhan] (maKhachHang);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_PhieuSuaChua_TrangThai' AND object_id = OBJECT_ID(N'dbo.PhieuSuaChua'))
    CREATE INDEX [IX_PhieuSuaChua_TrangThai] ON dbo.[PhieuSuaChua] (trangThai,ngayLap);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_HoaDon_KhachHang_Ngay' AND object_id = OBJECT_ID(N'dbo.HoaDon'))
    CREATE INDEX [IX_HoaDon_KhachHang_Ngay] ON dbo.[HoaDon] (maKhachHang,ngayLap);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_HoaDon_PhieuSuaChua' AND object_id = OBJECT_ID(N'dbo.HoaDon'))
    CREATE INDEX [IX_HoaDon_PhieuSuaChua] ON dbo.[HoaDon] (maPSC);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_LichNhac_Ngay_TrangThai' AND object_id = OBJECT_ID(N'dbo.LichNhacBaoDuong'))
    CREATE INDEX [IX_LichNhac_Ngay_TrangThai] ON dbo.[LichNhacBaoDuong] (ngayNhac,trangThai);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_LichSuTichDiem_KhachHang' AND object_id = OBJECT_ID(N'dbo.LichSuTichDiem'))
    CREATE INDEX [IX_LichSuTichDiem_KhachHang] ON dbo.[LichSuTichDiem] (maKhachHang,ngayTichDiem);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_PhuongTien_KhachHang' AND object_id = OBJECT_ID(N'dbo.PhuongTien'))
    CREATE INDEX [IX_PhuongTien_KhachHang] ON dbo.[PhuongTien] (maKhachHang);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_MaGiamGia_CoDiem' AND object_id = OBJECT_ID(N'dbo.MaGiamGia'))
    CREATE UNIQUE INDEX [UX_MaGiamGia_CoDiem] ON dbo.[MaGiamGia] (maKhachHang,ngayBatDauChuKy,mocDiem) WHERE mocDiem IS NOT NULL AND ngayBatDauChuKy IS NOT NULL;


-- ============================================================================
-- A.2. RÀNG BUỘC CHECK ĐỒNG BỘ CHO CẢ UC009 VÀ DỮ LIỆU LỊCH SỬ
-- ============================================================================
-- 1. CK_PhieuNhapKho_TrangThai: Siết chặt trạng thái hợp lệ - UC009 bắt buộc có maYCNK, 36 PNK lịch sử giữ nguyên trạng thái gốc
IF OBJECT_ID(N'dbo.[PhieuNhapKho]', N'U') IS NOT NULL
BEGIN
    -- Guard: Kiểm tra toàn vẹn dữ liệu PNK trước khi áp dụng constraint
    IF EXISTS (
        SELECT 1
        FROM dbo.[PhieuNhapKho]
        WHERE [trangThai] IS NULL
           OR (
                [maYCNK] IS NULL
                AND [trangThai] NOT IN (
                    N'Chờ duyệt',
                    N'Đã duyệt',
                    N'Không duyệt'
                )
           )
           OR (
                [maYCNK] IS NOT NULL
                AND [trangThai] NOT IN (
                    N'Chờ xác nhận nhập kho',
                    N'Đã nhập kho',
                    N'Đã hủy'
                )
           )
    )
    BEGIN
        SELECT [maPNK], [maYCNK], [trangThai]
        FROM dbo.[PhieuNhapKho]
        WHERE [trangThai] IS NULL
           OR (
                [maYCNK] IS NULL
                AND [trangThai] NOT IN (
                    N'Chờ duyệt',
                    N'Đã duyệt',
                    N'Không duyệt'
                )
           )
           OR (
                [maYCNK] IS NOT NULL
                AND [trangThai] NOT IN (
                    N'Chờ xác nhận nhập kho',
                    N'Đã nhập kho',
                    N'Đã hủy'
                )
           );

        THROW 51000,
            N'Dữ liệu PNK hiện tại không tương thích với CK_PhieuNhapKho_TrangThai. Cần kiểm tra trước khi nâng cấp.',
            1;
    END;

    -- Kiểm tra idempotency và định nghĩa hiện hành của constraint
    DECLARE @pnk_ck_def nvarchar(max);
    DECLARE @pnk_ck_disabled bit;
    DECLARE @pnk_ck_not_trusted bit;

    SELECT
        @pnk_ck_def = [definition],
        @pnk_ck_disabled = is_disabled,
        @pnk_ck_not_trusted = is_not_trusted
    FROM sys.check_constraints
    WHERE name = N'CK_PhieuNhapKho_TrangThai' AND parent_object_id = OBJECT_ID(N'dbo.PhieuNhapKho');

    IF @pnk_ck_def IS NULL
       OR @pnk_ck_def NOT LIKE N'%maYCNK%IS NOT NULL%'
       OR @pnk_ck_def NOT LIKE N'%maYCNK%IS NULL%'
       OR @pnk_ck_def LIKE N'%maYCNK%IS NULL%Chờ xác nhận nhập kho%'
       OR @pnk_ck_def LIKE N'%maYCNK%IS NULL%Đã nhập kho%'
       OR @pnk_ck_def LIKE N'%maYCNK%IS NULL%Đã hủy%'
    BEGIN
        IF @pnk_ck_def IS NOT NULL
            ALTER TABLE dbo.[PhieuNhapKho] DROP CONSTRAINT [CK_PhieuNhapKho_TrangThai];

        ALTER TABLE dbo.[PhieuNhapKho] WITH CHECK ADD CONSTRAINT [CK_PhieuNhapKho_TrangThai]
            CHECK (
                (
                    [maYCNK] IS NOT NULL
                    AND [trangThai] IN (
                        N'Chờ xác nhận nhập kho',
                        N'Đã nhập kho',
                        N'Đã hủy'
                    )
                )
                OR
                (
                    [maYCNK] IS NULL
                    AND [trangThai] IN (
                        N'Chờ duyệt',
                        N'Đã duyệt',
                        N'Không duyệt'
                    )
                )
            );
    END;

    -- Đảm bảo constraint được ENABLED và TRUSTED
    IF EXISTS (
        SELECT 1
        FROM sys.check_constraints
        WHERE name = N'CK_PhieuNhapKho_TrangThai'
          AND parent_object_id = OBJECT_ID(N'dbo.PhieuNhapKho')
          AND (is_disabled = 1 OR is_not_trusted = 1)
    )
    BEGIN
        ALTER TABLE dbo.[PhieuNhapKho] WITH CHECK CHECK CONSTRAINT [CK_PhieuNhapKho_TrangThai];
    END;
END;

-- Đồng bộ ràng buộc trạng thái cho ChiTietPhieuSuaChua và ChiTietPhieuBaoGia theo schema chuẩn
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_ChiTietPhieuSuaChua_TrangThai')
    ALTER TABLE dbo.[ChiTietPhieuSuaChua] DROP CONSTRAINT [CK_ChiTietPhieuSuaChua_TrangThai];
ALTER TABLE dbo.[ChiTietPhieuSuaChua] WITH CHECK ADD CONSTRAINT [CK_ChiTietPhieuSuaChua_TrangThai]
    CHECK ([trangThaiHangMuc] IN (N'Chờ sửa chữa', N'Đang sửa chữa', N'Thiếu phụ tùng', N'Hoàn thành'));

IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_ChiTietPhieuBaoGia_TrangThai')
    ALTER TABLE dbo.[ChiTietPhieuBaoGia] DROP CONSTRAINT [CK_ChiTietPhieuBaoGia_TrangThai];
ALTER TABLE dbo.[ChiTietPhieuBaoGia] WITH CHECK ADD CONSTRAINT [CK_ChiTietPhieuBaoGia_TrangThai]
    CHECK ([trangThai] IN (N'Chờ xác nhận', N'Chờ sửa chữa', N'Thiếu phụ tùng', N'Thiếu phụ tùng + Chờ xác nhận'));

-- 2. Ràng buộc số lượng trên ChiTietPhieuNhapKho (thỏa mãn cho cả bản ghi lịch sử và mới)
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_ChiTietPhieuNhapKho_SoLuong')
    ALTER TABLE dbo.[ChiTietPhieuNhapKho] DROP CONSTRAINT [CK_ChiTietPhieuNhapKho_SoLuong];
ALTER TABLE dbo.[ChiTietPhieuNhapKho] WITH CHECK ADD CONSTRAINT [CK_ChiTietPhieuNhapKho_SoLuong] CHECK ([soLuongNhap] >= 0);

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_ChiTietPhieuNhapKho_SoLuongYeuCau')
    ALTER TABLE dbo.[ChiTietPhieuNhapKho] WITH CHECK ADD CONSTRAINT [CK_ChiTietPhieuNhapKho_SoLuongYeuCau] CHECK ([soLuongYeuCau] IS NULL OR [soLuongYeuCau] > 0);

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_ChiTietPhieuNhapKho_SoLuongThucNhan')
    ALTER TABLE dbo.[ChiTietPhieuNhapKho] WITH CHECK ADD CONSTRAINT [CK_ChiTietPhieuNhapKho_SoLuongThucNhan] CHECK ([soLuongThucNhan] IS NULL OR [soLuongThucNhan] >= 0);

IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = N'CK_ChiTietPhieuNhapKho_HopLeThucNhan')
    ALTER TABLE dbo.[ChiTietPhieuNhapKho] WITH CHECK ADD CONSTRAINT [CK_ChiTietPhieuNhapKho_HopLeThucNhan]
    CHECK ([soLuongThucNhan] IS NULL OR ([soLuongNhap] <= [soLuongThucNhan] AND ([soLuongYeuCau] IS NULL OR [soLuongThucNhan] <= [soLuongYeuCau])));

-- 3. Ràng buộc upstream đối soát sửa chữa và bảo hành
IF NOT EXISTS(SELECT 1 FROM sys.check_constraints WHERE name=N'CK_ChiTietPSC_DoiSoat')
    ALTER TABLE dbo.ChiTietPhieuSuaChua WITH CHECK ADD CONSTRAINT CK_ChiTietPSC_DoiSoat CHECK(soLuongCapPhat>=0 AND soLuongSuDung>=0 AND soLuongHoanTra>=0 AND soLuongSuDung+soLuongHoanTra<=soLuongCapPhat AND soLuongCapPhat<=soLuongPhuTung AND (soLuongCapPhat=0 OR (ngayCapPhat IS NOT NULL AND maNhanVienCapPhat IS NOT NULL)) AND (soLuongHoanTra=0 OR (ngayHoanTra IS NOT NULL AND maNhanVienXacNhanHoanTra IS NOT NULL)));

IF NOT EXISTS(SELECT 1 FROM sys.check_constraints WHERE name=N'CK_LichBaoHanh_HangMuc')
    ALTER TABLE dbo.LichBaoHanh WITH CHECK ADD CONSTRAINT CK_LichBaoHanh_HangMuc CHECK((maDichVu IS NULL AND maPhuTung IS NULL) OR (maDichVu IS NOT NULL AND maPhuTung IS NOT NULL));

    COMMIT TRANSACTION;
    PRINT N'[SCHEMA] Backfill, khóa ngoại và ràng buộc CHECK đã được thiết lập.';
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO
-- B. RÀNG BUỘC LIÊN BẢNG (set-based, hỗ trợ INSERT/UPDATE nhiều dòng).
-- Không thêm trạng thái hay cơ chế kho/điểm mới ngoài hợp đồng của OOAD.
CREATE OR ALTER TRIGGER dbo.TR_PhieuSuaChua_HoSo ON dbo.PhieuSuaChua AFTER INSERT, UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.PhieuBaoGia bg ON bg.maBaoGia=i.maBaoGia
              JOIN dbo.PhieuKiemTra kt ON kt.maPKT=bg.maPKT WHERE i.maKhachHang<>kt.maKhachHang)
        THROW 51001,N'Khách hàng phiếu sửa chữa không khớp hồ sơ kiểm tra/báo giá.',1;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.ChiTietPhieuSuaChua c ON c.maPSC=i.maPSC
              WHERE i.ngayHoanThanh IS NOT NULL AND c.ngayBatDauBaoHanh<i.ngayHoanThanh)
        THROW 51002,N'Ngày bắt đầu bảo hành không được trước ngày hoàn thành sửa chữa.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.TR_PhieuKiemTra_HoSo ON dbo.PhieuKiemTra AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.PhieuBaoGia bg ON bg.maPKT=i.maPKT
              JOIN dbo.PhieuSuaChua sc ON sc.maBaoGia=bg.maBaoGia WHERE sc.maKhachHang<>i.maKhachHang)
        THROW 51003,N'Không được đổi hồ sơ kiểm tra làm sai khách hàng của phiếu sửa chữa đã liên kết.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.TR_PhieuBaoGia_HoSo ON dbo.PhieuBaoGia AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.PhieuKiemTra kt ON kt.maPKT=i.maPKT
              JOIN dbo.PhieuSuaChua sc ON sc.maBaoGia=i.maBaoGia WHERE sc.maKhachHang<>kt.maKhachHang)
        THROW 51004,N'Không được đổi báo giá làm sai khách hàng của phiếu sửa chữa đã liên kết.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.TR_ChiTietPSC_BaoHanh ON dbo.ChiTietPhieuSuaChua AFTER INSERT, UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.PhieuSuaChua sc ON sc.maPSC=i.maPSC
              WHERE sc.ngayHoanThanh IS NOT NULL AND i.ngayBatDauBaoHanh<sc.ngayHoanThanh)
        THROW 51005,N'Ngày bắt đầu bảo hành không được trước ngày hoàn thành sửa chữa.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.TR_ChiTietBaoGia_PT000 ON dbo.ChiTietPhieuBaoGia AFTER INSERT, UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT c.maBaoGia,c.maDichVu
        FROM dbo.ChiTietPhieuBaoGia c
        JOIN (SELECT DISTINCT maBaoGia,maDichVu FROM inserted) i
          ON i.maBaoGia=c.maBaoGia AND i.maDichVu=c.maDichVu
        GROUP BY c.maBaoGia,c.maDichVu
        HAVING SUM(CASE WHEN c.maPhuTung='PT000' THEN 1 ELSE 0 END)>0
           AND SUM(CASE WHEN c.maPhuTung<>'PT000' THEN 1 ELSE 0 END)>0
    ) THROW 51008,N'Một dịch vụ trong báo giá không thể đồng thời có PT000 và phụ tùng thật.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.TR_ChiTietPSC_PT000 ON dbo.ChiTietPhieuSuaChua AFTER INSERT, UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT c.maPSC,c.maDichVu
        FROM dbo.ChiTietPhieuSuaChua c
        JOIN (SELECT DISTINCT maPSC,maDichVu FROM inserted) i
          ON i.maPSC=c.maPSC AND i.maDichVu=c.maDichVu
        GROUP BY c.maPSC,c.maDichVu
        HAVING SUM(CASE WHEN c.maPhuTung='PT000' THEN 1 ELSE 0 END)>0
           AND SUM(CASE WHEN c.maPhuTung<>'PT000' THEN 1 ELSE 0 END)>0
    ) THROW 51009,N'Một dịch vụ trong phiếu sửa chữa không thể đồng thời có PT000 và phụ tùng thật.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.TR_PhuTung_PT000 ON dbo.PhuTung AFTER UPDATE, DELETE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (SELECT 1 FROM deleted WHERE maPhuTung='PT000')
        THROW 51010,N'PT000 là mã hệ thống, không được sửa hoặc xóa.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.TR_LichSuTichDiem_KhachHang ON dbo.LichSuTichDiem AFTER INSERT, UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.HoaDon h ON h.maHoaDon=i.maHoaDon
              WHERE i.maKhachHang<>h.maKhachHang OR i.ngayTichDiem<h.ngayLap)
        THROW 51006,N'Lịch sử điểm phải đúng khách hàng và không trước ngày lập hóa đơn.',1;
END;
GO
CREATE OR ALTER TRIGGER dbo.TR_HoaDon_LichSu ON dbo.HoaDon AFTER UPDATE AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.LichSuTichDiem l ON l.maHoaDon=i.maHoaDon
              WHERE l.maKhachHang<>i.maKhachHang OR l.ngayTichDiem<i.ngayLap)
        THROW 51007,N'Cập nhật hóa đơn làm sai khách hàng/ngày của lịch sử tích điểm.',1;
END;
GO


GO
-- C. VIEW TÍNH TOÁN ĐỌC, KHÔNG THAY ĐỔI NGHIỆP VỤ.
CREATE OR ALTER VIEW dbo.vw_TongTienBaoGia AS
SELECT b.maBaoGia,CONVERT(decimal(18,2),COALESCE(SUM(c.soLuongPhuTung*c.donGiaPhuTung+c.chiPhiNhanCong),0)) AS tongChiPhi
FROM dbo.PhieuBaoGia b LEFT JOIN dbo.ChiTietPhieuBaoGia c ON c.maBaoGia=b.maBaoGia
GROUP BY b.maBaoGia;
GO
CREATE OR ALTER VIEW dbo.vw_TongTienNhapKho AS
SELECT n.maPNK,CONVERT(decimal(18,2),COALESCE(SUM(c.soLuongNhap*c.donGiaNhap),0)) AS tongTien
FROM dbo.PhieuNhapKho n LEFT JOIN dbo.ChiTietPhieuNhapKho c ON c.maPNK=n.maPNK
GROUP BY n.maPNK;
GO
CREATE OR ALTER VIEW dbo.vw_HoaDonTongHop AS
SELECT h.*,CONVERT(decimal(18,2),h.tienPhuTung+h.tienCong+h.dichVuKhac+h.thueVAT-h.giamGia) AS tongThanhToan
FROM dbo.HoaDon h;
GO
CREATE OR ALTER VIEW dbo.vw_KhachHangDiemTichLuy AS
SELECT k.maKhachHang,k.hoTenKH,COALESCE(SUM(CONVERT(bigint,l.soDiem)),0) AS tongDiemTichLuy
FROM dbo.KhachHang k LEFT JOIN dbo.LichSuTichDiem l ON l.maKhachHang=k.maKhachHang AND l.ngayHetHan>SYSDATETIME()
GROUP BY k.maKhachHang,k.hoTenKH;
GO
CREATE OR ALTER VIEW dbo.vw_NhanVienHienThi AS
SELECT n.*,CASE WHEN n.gioiTinh=1 THEN N'Nam' ELSE N'Nữ' END AS tenGioiTinh
FROM dbo.NhanVien n;
GO

GO

-- ============================================================================
-- D. DỮ LIỆU MẪU CƠ SỞ (Tự động nạp khi cài đặt mới, giữ nguyên khi nâng cấp)
-- ============================================================================
IF NOT EXISTS (SELECT 1 FROM dbo.NhanVien WHERE maNhanVien = N'NV001')
BEGIN
    PRINT N'[SEED] Đang nạp bộ dữ liệu mẫu cơ sở...';
    DECLARE @NgayMau datetime2(0) = CONVERT(date, SYSDATETIME());
    BEGIN TRY
        BEGIN TRANSACTION;


-- NhanVien: 4 thành viên và 1 admin
INSERT INTO dbo.NhanVien
    (maNhanVien, hoTenNV, ngaySinh, soDienThoai, email,
     diaChi, vaiTro, gioiTinh, trangThai)
VALUES
    ('NV001', N'Quách Tấn Minh', '20050412', '0901000001',
     'minh.123@garage.vn', N'Quận 1, TP. Hồ Chí Minh',
     N'Nhân viên bán hàng', 1, N'Đang làm'),

    ('NV002', N'Cao Huỳnh Trung Kiên', '20050920', '0901000002',
     'kien.123@garage.vn', N'Quận 3, TP. Hồ Chí Minh',
     N'Nhân viên kho', 1, N'Đang làm'),

    ('NV003', N'Phạm Thị Ngọc Châu', '20050108', '0901000003',
     'chau.123@garage.vn', N'Quận Bình Thạnh, TP. Hồ Chí Minh',
     N'Nhân viên kỹ thuật', 0, N'Đang làm'),

    ('NV004', N'Châu Ngọc Khanh', '20051117', '0901000004',
     'khanh.123@garage.vn', N'Thành phố Thủ Đức, TP. Hồ Chí Minh',
     N'Nhân viên quản lý', 1, N'Đang làm'),

    ('NV005', N'Admin', '20050505', '0909888777',
     'admin.123@garage.vn', N'Quận 7, TP. Hồ Chí Minh',
     N'Nhân viên quản lý', 1, N'Đang làm'),
    ('NV006', N'Trần Minh Quân', '19990115', '0902000006', 'quan.tran@garage.vn', N'Quận 1, TP. Hồ Chí Minh', N'Nhân viên bán hàng', 1, N'Đang làm'),
    ('NV007', N'Lê Ngọc Phương', '20010322', '0902000007', 'phuong.le@garage.vn', N'Quận 3, TP. Hồ Chí Minh', N'Nhân viên bán hàng', 0, N'Đang làm'),
    ('NV008', N'Phạm Quốc Hưng', '19980711', '0902000008', 'hung.pham@garage.vn', N'Quận 5, TP. Hồ Chí Minh', N'Nhân viên bán hàng', 1, N'Đang làm'),
    ('NV009', N'Nguyễn Thành Đạt', '19970218', '0902000009', 'dat.nguyen@garage.vn', N'Gò Vấp, TP. Hồ Chí Minh', N'Nhân viên kỹ thuật', 1, N'Đang làm'),
    ('NV010', N'Võ Minh Tâm', '20000526', '0902000010', 'tam.vo@garage.vn', N'Bình Thạnh, TP. Hồ Chí Minh', N'Nhân viên kỹ thuật', 1, N'Đang làm'),
    ('NV011', N'Đỗ Hoàng Long', '19991003', '0902000011', 'long.do@garage.vn', N'Tân Bình, TP. Hồ Chí Minh', N'Nhân viên kỹ thuật', 1, N'Đang làm'),
    ('NV012', N'Bùi Thanh Phúc', '20010812', '0902000012', 'phuc.bui@garage.vn', N'Quận 10, TP. Hồ Chí Minh', N'Nhân viên kho', 1, N'Đang làm'),
    ('NV013', N'Nguyễn Thị Thu Hà', '19981204', '0902000013', 'ha.nguyen@garage.vn', N'Thành phố Thủ Đức, TP. Hồ Chí Minh', N'Nhân viên kho', 0, N'Đang làm');

-- KhachHang: 60 bản ghi
INSERT INTO dbo.[KhachHang] ([maKhachHang], [hoTenKH], [soDienThoai], [gioiTinh], [email], [diaChi])
VALUES
    (N'KH001', N'Nguyễn Văn An', N'0522000001', 1, N'an.nguyen@gmail.com', N'21 Lê Lợi, Quận 1, TP. Hồ Chí Minh'),
    (N'KH002', N'Nguyễn Ngọc Mai', N'0722000002', 0, N'mai.nguyen@gmail.com', N'15 Trần Phú, Ninh Kiều, Cần Thơ'),
    (N'KH003', N'Trần Minh Đức', N'0822000003', 1, N'duc.tran@gmail.com', N'88 Quang Trung, Nha Trang, Khánh Hòa'),
    (N'KH004', N'Trần Thị Lan', N'0922000004', 0, N'lan.tran@gmail.com', N'104 Nguyễn Trãi, Quận 5, TP. Hồ Chí Minh'),
    (N'KH005', N'Lê Quốc Bảo', N'0322000005', 1, N'bao.le@gmail.com', N'52 Hùng Vương, Tân An, Long An'),
    (N'KH006', N'Lê Thu Hà', N'0522000006', 0, N'ha.le@gmail.com', N'36 Đường 30 Tháng 4, Ninh Kiều, Cần Thơ'),
    (N'KH007', N'Phạm Gia Huy', N'0722000007', 1, N'huy.pham@gmail.com', N'74 Thống Nhất, Nha Trang, Khánh Hòa'),
    (N'KH008', N'Phạm Bảo Ngọc', N'0822000008', 0, N'ngoc.pham@gmail.com', N'28 Cách Mạng Tháng 8, Quận 3, TP. Hồ Chí Minh'),
    (N'KH009', N'Hoàng Tuấn Kiệt', N'0922000009', 1, N'kiet.hoang@gmail.com', N'12 Nguyễn Huệ, Bến Lức, Long An'),
    (N'KH010', N'Hoàng Minh Anh', N'0322000010', 0, N'anh.hoang@gmail.com', NULL),
    (N'KH011', N'Võ Thanh Tùng', N'0522000011', 1, N'tung.vo@gmail.com', N'63 Lê Thánh Tôn, Nha Trang, Khánh Hòa'),
    (N'KH012', N'Võ Thanh Thảo', N'0722000012', 0, N'thao.vo@gmail.com', N'145 Hoàng Diệu, Quận 4, TP. Hồ Chí Minh'),
    (N'KH013', N'Đặng Hữu Phúc', N'0822000013', 1, N'phuc.dang@gmail.com', N'99 Trương Định, Tân An, Long An'),
    (N'KH014', N'Đặng Kim Chi', N'0922000014', 0, N'chi.dang@gmail.com', N'42 Mậu Thân, Ninh Kiều, Cần Thơ'),
    (N'KH015', N'Bùi Trung Hiếu', N'0322000015', 1, N'hieu.bui@gmail.com', N'27 Yersin, Nha Trang, Khánh Hòa'),
    (N'KH016', N'Bùi Ngọc Hân', N'0522000016', 0, N'han.bui@gmail.com', N'83 Nguyễn Thị Minh Khai, Quận 1, TP. Hồ Chí Minh'),
    (N'KH017', N'Đỗ Quang Vinh', N'0722000017', 1, N'vinh.do@gmail.com', N'17 Mai Thị Tốt, Tân An, Long An'),
    (N'KH018', N'Đỗ Phương Linh', N'0822000018', 0, N'linh.do@gmail.com', N'58 Nguyễn Văn Cừ, Ninh Kiều, Cần Thơ'),
    (N'KH019', N'Hồ Nhật Nam', N'0922000019', 1, N'nam.ho@gmail.com', N'110 Thái Nguyên, Nha Trang, Khánh Hòa'),
    (N'KH020', N'Hồ Thùy Dung', N'0322000020', 0, N'dung.ho@gmail.com', NULL),
    (N'KH021', N'Dương Anh Khoa', N'0522000021', 1, N'khoa.duong@gmail.com', N'64 Châu Thị Kim, Tân An, Long An'),
    (N'KH022', N'Dương Khánh Vy', N'0722000022', 0, N'vy.duong@gmail.com', N'77 Hòa Bình, Ninh Kiều, Cần Thơ'),
    (N'KH023', N'Mai Thành Đạt', N'0822000023', 1, N'dat.mai@gmail.com', N'39 Hoàng Hoa Thám, Nha Trang, Khánh Hòa'),
    (N'KH024', N'Mai Diệu Nhi', N'0922000024', 0, N'nhi.mai@gmail.com', N'188 Nguyễn Văn Trỗi, Phú Nhuận, TP. Hồ Chí Minh'),
    (N'KH025', N'Phan Trọng Tấn', N'0322000025', 1, N'tan.phan@gmail.com', N'128 Nguyễn Trãi, Quận 1, TP. Hồ Chí Minh'),
    (N'KH026', N'Trịnh Thúy Nga', N'0522000026', 0, N'nga.trinh@gmail.com', N'45 Hai Bà Trưng, Quận 3, TP. Hồ Chí Minh'),
    (N'KH027', N'Lương Thế Vinh', N'0722000027', 1, N'vinh.luong@gmail.com', N'89 Phan Xích Long, Phú Nhuận, TP. Hồ Chí Minh'),
    (N'KH028', N'Cao Hồng Ngọc', N'0822000028', 0, N'ngoc.cao@gmail.com', N'67 Võ Văn Ngân, TP. Thủ Đức, TP. Hồ Chí Minh'),
    (N'KH029', N'Tạ Quang Bửu', N'0922000029', 1, N'buu.ta@gmail.com', N'34 Hoàng Hoa Thám, Bình Thạnh, TP. Hồ Chí Minh'),
    (N'KH030', N'Vũ Thu Thủy', N'0322000030', 0, N'thuy.vu@gmail.com', NULL),
    (N'KH031', N'Đoàn Nhật Minh', N'0522000031', 1, N'minh.doan@gmail.com', N'15 Trần Hưng Đạo, Quận 5, TP. Hồ Chí Minh'),
    (N'KH032', N'Huỳnh Mỹ Duyên', N'0722000032', 0, N'duyen.huynh@gmail.com', N'204 Cộng Hòa, Tân Bình, TP. Hồ Chí Minh'),
    (N'KH033', N'Lâm Vĩnh Hưng', N'0822000033', 1, N'hung.lam@gmail.com', N'92 Lê Văn Sỹ, Quận 3, TP. Hồ Chí Minh'),
    (N'KH034', N'Tôn Nữ Bích Ngọc', N'0922000034', 0, N'ngoc.tonnu@gmail.com', N'78 Nguyễn Thị Minh Khai, Quận 1, TP. Hồ Chí Minh'),
    (N'KH035', N'Chu Đình Dũng', N'0322000035', 1, N'dung.chu@gmail.com', N'115 Đường 3 Tháng 2, Quận 10, TP. Hồ Chí Minh'),
    (N'KH036', N'Quách Tuyết Mai', N'0522000036', 0, N'mai.quach@gmail.com', N'56 Hùng Vương, Ninh Kiều, Cần Thơ'),
    (N'KH037', N'Đinh Tiến Đạt', N'0722000037', 1, N'dat.dinh@gmail.com', N'230 Quang Trung, Gò Vấp, TP. Hồ Chí Minh'),
    (N'KH038', N'Nhan Cẩm Tú', N'0822000038', 0, N'tu.nhan@gmail.com', N'412 Cách Mạng Tháng 8, Quận 10, TP. Hồ Chí Minh'),
    (N'KH039', N'Trương Hữu Long', N'0922000039', 1, N'long.truong@gmail.com', N'19 Pasteur, Quận 1, TP. Hồ Chí Minh'),
    (N'KH040', N'Tiêu Thảo My', N'0322000040', 0, N'my.tieu@gmail.com', NULL),
    (N'KH041', N'Diệp Kiến Thành', N'0522000041', 1, N'thanh.diep@gmail.com', N'88 Điện Biên Phủ, Bình Thạnh, TP. Hồ Chí Minh'),
    (N'KH042', N'Tô Phương Uyên', N'0722000042', 0, N'uyen.to@gmail.com', N'103 Lý Thường Kiệt, Tân Bình, TP. Hồ Chí Minh'),
    (N'KH043', N'Lại Văn Sâm', N'0822000043', 1, N'sam.lai@gmail.com', N'12 Bến Vân Đồn, Quận 4, TP. Hồ Chí Minh'),
    (N'KH044', N'Mạch Diệu Huyền', N'0922000044', 0, N'huyen.mach@gmail.com', N'65 Nam Kỳ Khởi Nghĩa, Quận 1, TP. Hồ Chí Minh'),
    (N'KH045', N'Hứa Vĩ Văn', N'0322000045', 1, N'van.hua@gmail.com', N'47 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh'),
    (N'KH046', N'Thiều Bảo Trâm', N'0522000046', 0, N'tram.thieu@gmail.com', N'82 Trần Não, TP. Thủ Đức, TP. Hồ Chí Minh'),
    (N'KH047', N'Tạ Đình Phong', N'0722000047', 1, N'phong.ta@gmail.com', N'195 Xô Viết Nghệ Tĩnh, Bình Thạnh, TP. Hồ Chí Minh'),
    (N'KH048', N'Cổ Ngân Hà', N'0822000048', 0, N'ha.co@gmail.com', N'39 Thảo Điền, TP. Thủ Đức, TP. Hồ Chí Minh'),
    (N'KH049', N'Khổng Minh Tuấn', N'0922000049', 1, N'tuan.khong@gmail.com', N'150 Nguyễn Đình Chiểu, Quận 3, TP. Hồ Chí Minh'),
    (N'KH050', N'Ôn Bích Hà', N'0322000050', 0, N'ha.on@gmail.com', NULL),
    (N'KH051', N'Sầm Quốc Cường', N'0522000051', 1, N'cuong.sam@gmail.com', N'90 Đồng Khởi, Quận 1, TP. Hồ Chí Minh'),
    (N'KH052', N'Vi Thục Đoan', N'0722000052', 0, N'doan.vi@gmail.com', N'32 Lê Duẩn, Quận 1, TP. Hồ Chí Minh'),
    (N'KH053', N'Chế Thanh Phong', N'0822000053', 1, N'phong.che@gmail.com', N'174 Trường Chinh, Tân Bình, TP. Hồ Chí Minh'),
    (N'KH054', N'La Lan Anh', N'0922000054', 0, N'anh.la@gmail.com', N'220 Phạm Văn Đồng, TP. Thủ Đức, TP. Hồ Chí Minh'),
    (N'KH055', N'Âu Dương Tuấn', N'0322000055', 1, N'tuan.auduong@gmail.com', N'58 Bạch Đằng, Tân Bình, TP. Hồ Chí Minh'),
    (N'KH056', N'Hạ Ái Nhi', N'0522000056', 0, N'nhi.ha@gmail.com', N'14 Võ Văn Tần, Quận 3, TP. Hồ Chí Minh'),
    (N'KH057', N'Thái Hòa Phát', N'0722000057', 1, N'phat.thai@gmail.com', N'71 Nguyễn Oanh, Gò Vấp, TP. Hồ Chí Minh'),
    (N'KH058', N'Cù Thị Yến', N'0822000058', 0, N'yen.cu@gmail.com', N'163 Hoàng Văn Thụ, Phú Nhuận, TP. Hồ Chí Minh'),
    (N'KH059', N'Quàng Văn Hải', N'0922000059', 1, N'hai.quang@gmail.com', N'84 Nguyễn Tất Thành, Quận 4, TP. Hồ Chí Minh'),
    (N'KH060', N'Bào Thanh Trúc', N'0322000060', 0, N'truc.bao@gmail.com', NULL);

-- PhuongTien: 72 bản ghi
INSERT INTO dbo.[PhuongTien] ([VIN], [bienSo], [hangXe], [dongXe], [mauXe], [namSanXuat], [soMay], [soKM])
VALUES
    (N'MR0BT9F31JA310001', N'31B10001', N'Toyota', N'Vios', N'Đen', 2018, N'2NR-5100001', 0.00),
    (N'MRHGM6627FA310002', N'32C10002', N'Honda', N'City', N'Bạc', 2015, N'L15Z1-5100001', 3466.00),
    (N'KMHCT41B5JA310003', N'33D10003', N'Hyundai', N'Accent', N'Đỏ', 2018, N'G4LCJU410001', 5199.00),
    (N'KNDEP81A3LA310004', N'34E10004', N'Kia', N'Seltos', N'Xanh', 2020, N'G4FGLU410001', 6932.00),
    (N'JM3KF2W77HA310005', N'35F10005', N'Mazda', N'CX-5', N'Xám', 2017, N'PE-5100001', 8665.00),
    (N'MNCUMFF55FA310006', N'36G10006', N'Ford', N'Ranger', N'Vàng', 2015, N'P4AT-5100001', 10398.00),
    (N'MK2NC1W48KA310007', N'37H10007', N'Mitsubishi', N'Xpander', N'Nâu', 2019, N'4A91-5100001', 12131.00),
    (N'MHYANC226MA310008', N'38I10008', N'Suzuki', N'XL7', N'Trắng', 2021, N'K15B-5100001', 13864.00),
    (N'MNTCB4D20GA310009', N'39J10009', N'Nissan', N'Navara', N'Đen', 2016, N'YD25-5100001', 15597.00),
    (N'WDDWF4JB5FA310010', N'40K10010', N'Mercedes-Benz', N'C200', N'Bạc', 2015, N'27492030510001', 17330.00),
    (N'WBA5R1105KA310011', N'41L10011', N'BMW', N'320i', N'Đỏ', 2019, N'61000001', 19063.00),
    (N'RLLV1AFA6PA310012', N'42M10012', N'VinFast', N'VF8', N'Xanh', 2023, N'202371000001', 20796.00),
    (N'MR0BT9F36KA310013', N'43N10013', N'Toyota', N'Vios', N'Xám', 2019, N'2NR-5100002', 22529.00),
    (N'MRHGM6621GA310014', N'44O10014', N'Honda', N'City', N'Vàng', 2016, N'L15Z1-5100002', 24262.00),
    (N'KMHCT41BXKA310015', N'45P10015', N'Hyundai', N'Accent', N'Nâu', 2019, N'G4LCKU410002', 25995.00),
    (N'KNDEP81A8MA310016', N'46Q10016', N'Kia', N'Seltos', N'Trắng', 2021, N'G4FGMU410002', 27728.00),
    (N'JM3KF2W76JA310017', N'47R10017', N'Mazda', N'CX-5', N'Đen', 2018, N'PE-5100002', 29461.00),
    (N'MNCUMFF5XGA310018', N'48S10018', N'Ford', N'Ranger', N'Bạc', 2016, N'P4AT-5100002', 31194.00),
    (N'MK2NC1W42LA310019', N'49T10019', N'Mitsubishi', N'Xpander', N'Đỏ', 2020, N'4A91-5100002', 32927.00),
    (N'MHYANC225NA310020', N'50A10020', N'Suzuki', N'XL7', N'Xanh', 2022, N'K15B-5100002', 34660.00),
    (N'MNTCB4D2XHA310021', N'51B10021', N'Nissan', N'Navara', N'Xám', 2017, N'YD25-5100002', 36393.00),
    (N'WDDWF4JBXGA310022', N'52C10022', N'Mercedes-Benz', N'C200', N'Vàng', 2016, N'27492030510002', 38126.00),
    (N'WBA5R110XLA310023', N'53D10023', N'BMW', N'320i', N'Nâu', 2020, N'61000002', 39859.00),
    (N'RLLV1AFA9RA310024', N'54E10024', N'VinFast', N'VF8', N'Trắng', 2024, N'202471000002', 41592.00),
    (N'MR0BT9F30LA310025', N'55F10025', N'Toyota', N'Vios', N'Đen', 2020, N'2NR-5100003', 43325.00),
    (N'MRHGM6626HA310026', N'56G10026', N'Honda', N'City', N'Bạc', 2017, N'L15Z1-5100003', 45058.00),
    (N'KMHCT41B4LA310027', N'57H10027', N'Hyundai', N'Accent', N'Đỏ', 2020, N'G4LCLU410003', 46791.00),
    (N'KNDEP81A2NA310028', N'58I10028', N'Kia', N'Seltos', N'Xanh', 2022, N'G4FGNU410003', 48524.00),
    (N'JM3KF2W70KA310029', N'59J10029', N'Mazda', N'CX-5', N'Xám', 2019, N'PE-5100003', 50257.00),
    (N'MNCUMFF59HA310030', N'60K10030', N'Ford', N'Ranger', N'Vàng', 2017, N'P4AT-5100003', 51990.00),
    (N'MK2NC1W41MA310031', N'61L10031', N'Mitsubishi', N'Xpander', N'Nâu', 2021, N'4A91-5100003', 53723.00),
    (N'MHYANC228PA310032', N'62M10032', N'Suzuki', N'XL7', N'Trắng', 2023, N'K15B-5100003', 55456.00),
    (N'MNTCB4D29JA310033', N'63N10033', N'Nissan', N'Navara', N'Đen', 2018, N'YD25-5100003', 57189.00),
    (N'WDDWF4JB4HA310034', N'64O10034', N'Mercedes-Benz', N'C200', N'Bạc', 2017, N'27492030510003', 58922.00),
    (N'WBA5R1104MA310035', N'65P10035', N'BMW', N'320i', N'Đỏ', 2021, N'61000003', 60655.00),
    (N'RLLV1AFA8SA310036', N'66Q10036', N'VinFast', N'VF8', N'Xanh', 2025, N'202571000003', 62388.00),
    (N'MR0BT9F35MA310037', N'67R10037', N'Toyota', N'Vios', N'Xám', 2021, N'2NR-5100004', 64121.00),
    (N'MRHGM6625JA310038', N'68S10038', N'Honda', N'City', N'Vàng', 2018, N'L15Z1-5100004', 65854.00),
    (N'KMHCT41B9MA310039', N'69T10039', N'Hyundai', N'Accent', N'Nâu', 2021, N'G4LCMU410004', 67587.00),
    (N'KNDEP81AXPA310040', N'30A10040', N'Kia', N'Seltos', N'Trắng', 2023, N'G4FGPU410004', 69320.00),
    (N'JM3KF2W7XLA310041', N'31B10041', N'Mazda', N'CX-5', N'Đen', 2020, N'PE-5100004', 71053.00),
    (N'MNCUMFF58JA310042', N'32C10042', N'Ford', N'Ranger', N'Bạc', 2018, N'P4AT-5100004', 72786.00),
    (N'MK2NC1W46NA310043', N'33D10043', N'Mitsubishi', N'Xpander', N'Đỏ', 2022, N'4A91-5100004', 74519.00),
    (N'MHYANC22XMA310044', N'34E10044', N'Suzuki', N'XL7', N'Xanh', 2021, N'K15B-5100004', 76252.00),
    (N'MNTCB4D23KA310045', N'35F10045', N'Nissan', N'Navara', N'Xám', 2019, N'YD25-5100004', 77985.00),
    (N'WDDWF4JB3JA310046', N'36G10046', N'Mercedes-Benz', N'C200', N'Vàng', 2018, N'27492030510004', 79718.00),
    (N'WBA5R1109NA310047', N'37H10047', N'BMW', N'320i', N'Nâu', 2022, N'61000004', 81451.00),
    (N'RLLV1AFA5PA310048', N'38I10048', N'VinFast', N'VF8', N'Trắng', 2023, N'202371000004', 83184.00),
    (N'MR0BT9F3XNA310049', N'39J10049', N'Toyota', N'Vios', N'Đen', 2022, N'2NR-5100005', 84917.00),
    (N'MRHGM6624KA310050', N'40K10050', N'Honda', N'City', N'Bạc', 2019, N'L15Z1-5100005', 86650.00),
    (N'KMHCT41B8NA310051', N'41L10051', N'Hyundai', N'Accent', N'Đỏ', 2022, N'G4LCNU410005', 88383.00),
    (N'KNDEP81A3LA310052', N'42M10052', N'Kia', N'Seltos', N'Xanh', 2020, N'G4FGLU410005', 90116.00),
    (N'JM3KF2W74MA310053', N'43N10053', N'Mazda', N'CX-5', N'Xám', 2021, N'PE-5100005', 91849.00),
    (N'MNCUMFF52KA310054', N'44O10054', N'Ford', N'Ranger', N'Vàng', 2019, N'P4AT-5100005', 93582.00),
    (N'MK2NC1W49PA310055', N'45P10055', N'Mitsubishi', N'Xpander', N'Nâu', 2023, N'4A91-5100005', 95315.00),
    (N'MHYANC224NA310056', N'46Q10056', N'Suzuki', N'XL7', N'Trắng', 2022, N'K15B-5100005', 97048.00),
    (N'MNTCB4D28LA310057', N'47R10057', N'Nissan', N'Navara', N'Đen', 2020, N'YD25-5100005', 98781.00),
    (N'WDDWF4JB0FA310058', N'48S10058', N'Mercedes-Benz', N'C200', N'Bạc', 2015, N'27492030510005', 100514.00),
    (N'WBA5R1101PA310059', N'49T10059', N'BMW', N'320i', N'Đỏ', 2023, N'61000005', 102247.00),
    (N'RLLV1AFA2RA310060', N'50A10060', N'VinFast', N'VF8', N'Xanh', 2024, N'202471000005', 103980.00),
    (N'MR0BT9F37PA310061', N'51B10061', N'Toyota', N'Vios', N'Xám', 2023, N'2NR-5100006', 105713.00),
    (N'MRHGM6623FA310062', N'52C10062', N'Honda', N'City', N'Vàng', 2015, N'L15Z1-5100006', 107446.00),
    (N'KMHCT41B0PA310063', N'53D10063', N'Hyundai', N'Accent', N'Nâu', 2023, N'G4LCPU410006', 109179.00),
    (N'KNDEP81A8MA310064', N'54E10064', N'Kia', N'Seltos', N'Trắng', 2021, N'G4FGMU410006', 110912.00),
    (N'JM3KF2W79NA310065', N'55F10065', N'Mazda', N'CX-5', N'Đen', 2022, N'PE-5100006', 112645.00),
    (N'MNCUMFF57LA310066', N'56G10066', N'Ford', N'Ranger', N'Bạc', 2020, N'P4AT-5100006', 114378.00),
    (N'MK2NC1W44KA310067', N'57H10067', N'Mitsubishi', N'Xpander', N'Đỏ', 2019, N'4A91-5100006', 116111.00),
    (N'MHYANC227PA310068', N'58I10068', N'Suzuki', N'XL7', N'Xanh', 2023, N'K15B-5100006', 117844.00),
    (N'MNTCB4D27GA310069', N'59J10069', N'Nissan', N'Navara', N'Xám', 2016, N'YD25-5100006', 119577.00),
    (N'WDDWF4JBXGA310070', N'60K10070', N'Mercedes-Benz', N'C200', N'Vàng', 2016, N'27492030510006', 121310.00),
    (N'WBA5R1101KA310071', N'61L10071', N'BMW', N'320i', N'Nâu', 2019, N'61000006', 123043.00),
    (N'RLLV1AFA1SA310072', N'62M10072', N'VinFast', N'VF8', N'Trắng', 2025, N'202571000006', 124776.00);

;WITH CustomerOrder AS (
    SELECT maKhachHang,ROW_NUMBER() OVER (ORDER BY maKhachHang) AS rn
    FROM dbo.KhachHang
), CustomerCount AS (
    SELECT COUNT_BIG(*) AS total FROM CustomerOrder
), VehicleOrder AS (
    SELECT VIN,ROW_NUMBER() OVER (ORDER BY VIN) AS rn
    FROM dbo.PhuongTien
    WHERE maKhachHang IS NULL
)
UPDATE vehicle
SET maKhachHang=customer.maKhachHang
FROM dbo.PhuongTien vehicle
JOIN VehicleOrder v ON v.VIN=vehicle.VIN
CROSS JOIN CustomerCount countRows
JOIN CustomerOrder customer ON customer.rn=((v.rn-1)%NULLIF(countRows.total,0))+1;

-- DichVu: 20 bản ghi
INSERT INTO dbo.[DichVu] ([maDichVu], [tenDichVu], [moTa], [donGia], [trangThai])
VALUES
    (N'DV001', N'Thay dầu động cơ', N'Dịch vụ thay dầu động cơ theo yêu cầu khách hàng.', 150000.00, N'Hoạt động'),
    (N'DV002', N'Thay lọc dầu', N'Dịch vụ thay lọc dầu theo yêu cầu khách hàng.', 100000.00, N'Hoạt động'),
    (N'DV003', N'Kiểm tra phanh', N'Dịch vụ kiểm tra phanh theo yêu cầu khách hàng.', 250000.00, N'Hoạt động'),
    (N'DV004', N'Thay má phanh', N'Dịch vụ thay má phanh theo yêu cầu khách hàng.', 300000.00, N'Hoạt động'),
    (N'DV005', N'Thay lọc gió điều hòa', N'Dịch vụ thay lọc gió điều hòa không khí khoang lái theo yêu cầu.', 120000.00, N'Hoạt động'),
    (N'DV006', N'Vệ sinh điều hòa', N'Dịch vụ vệ sinh điều hòa theo yêu cầu khách hàng.', 650000.00, N'Hoạt động'),
    (N'DV007', N'Thay ắc quy', N'Dịch vụ thay ắc quy theo yêu cầu khách hàng.', 200000.00, N'Hoạt động'),
    (N'DV008', N'Thay lốp xe', N'Dịch vụ thay lốp xe theo yêu cầu khách hàng.', 180000.00, N'Hoạt động'),
    (N'DV009', N'Cân bằng động bánh xe', N'Dịch vụ cân bằng động bánh xe theo yêu cầu khách hàng.', 250000.00, N'Hoạt động'),
    (N'DV010', N'Thay bugi', N'Dịch vụ kiểm tra và thay thế bộ bugi đánh lửa động cơ theo yêu cầu.', 180000.00, N'Hoạt động'),
    (N'DV011', N'Thay dây curoa', N'Dịch vụ thay dây curoa theo yêu cầu khách hàng.', 450000.00, N'Hoạt động'),
    (N'DV012', N'Thay nước làm mát', N'Dịch vụ thay nước làm mát theo yêu cầu khách hàng.', 150000.00, N'Hoạt động'),
    (N'DV013', N'Thay giảm xóc', N'Dịch vụ thay giảm xóc theo yêu cầu khách hàng.', 800000.00, N'Hoạt động'),
    (N'DV014', N'Sửa hệ thống điện', N'Dịch vụ sửa hệ thống điện theo yêu cầu khách hàng.', 1500000.00, N'Hoạt động'),
    (N'DV015', N'Đại tu động cơ', N'Dịch vụ đại tu, phục hồi và căn chỉnh buồng đốt động cơ.', 12000000.00, N'Hoạt động'),
    (N'DV016', N'Sửa hộp số', N'Dịch vụ sửa hộp số theo yêu cầu khách hàng.', 8500000.00, N'Hoạt động'),
    (N'DV017', N'Bảo dưỡng miễn phí', N'Dịch vụ bảo dưỡng miễn phí theo yêu cầu khách hàng.', 0.00, N'Hoạt động'),
    (N'DV018', N'Kiểm tra sau bảo hành', N'Dịch vụ kiểm tra sau bảo hành theo yêu cầu khách hàng.', 0.00, N'Ngừng hoạt động'),
    (N'DV019', N'Chăm sóc ngoại thất', N'Dịch vụ chăm sóc ngoại thất theo yêu cầu khách hàng.', 1200000.00, N'Ngừng hoạt động'),
    (N'DV020', N'Kiểm tra kỹ thuật chuyên sâu', N'Dịch vụ kiểm tra, quét lỗi và chẩn đoán kỹ thuật chuyên sâu.', 2500000.00, N'Ngừng hoạt động');

-- PhuTung: 51 bản ghi (PT000 là mã hệ thống, không có tồn kho)
INSERT INTO dbo.[PhuTung] ([maPhuTung], [tenPhuTung], [donGia], [loaiPhuTung], [thoiHanBaoHanh])
VALUES
    (N'PT000', N'Không sử dụng phụ tùng', 0.00, N'Hệ thống', 0),
    (N'PT001', N'Dầu động cơ 5W-30', 850000.00, N'Dầu nhớt', 24),
    (N'PT002', N'Lọc dầu Toyota', 180000.00, N'Bộ lọc', 24),
    (N'PT003', N'Má phanh trước', 950000.00, N'Hệ thống phanh', 24),
    (N'PT004', N'Lọc gió điều hòa', 220000.00, N'Điều hòa', 24),
    (N'PT005', N'Ắc quy 12V 60Ah', 1850000.00, N'Điện', 24),
    (N'PT006', N'Bugi đánh lửa', 150000.00, N'Đánh lửa', 24),
    (N'PT007', N'Dây curoa', 650000.00, N'Truyền động', 24),
    (N'PT008', N'Nước làm mát', 250000.00, N'Làm mát', 24),
    (N'PT009', N'Lốp 205/55R16', 2300000.00, N'Lốp', 60),
    (N'PT010', N'Giảm xóc trước', 3500000.00, N'Hệ thống treo', 24),
    (N'PT011', N'Cảm biến ABS', 1250000.00, N'Điện', 24),
    (N'PT012', N'Bơm nhiên liệu', 2800000.00, N'Nhiên liệu', 60),
    (N'PT013', N'Đĩa phanh', 1800000.00, N'Hệ thống phanh', 60),
    (N'PT014', N'Lọc gió động cơ', 320000.00, N'Bộ lọc', 24),
    (N'PT015', N'Bóng đèn pha', 480000.00, N'Điện', 24),
    (N'PT016', N'Gạt mưa', 350000.00, N'Phụ kiện', 24),
    (N'PT017', N'Cao su chân máy', 1450000.00, N'Động cơ', 60),
    (N'PT018', N'Dầu hộp số', 950000.00, N'Dầu nhớt', 24),
    (N'PT019', N'Bộ ly hợp', 6500000.00, N'Truyền động', 60),
    (N'PT020', N'Van hằng nhiệt', 850000.00, N'Làm mát', 24),
    (N'PT021', N'Động cơ thay thế', 40000000.00, N'Động cơ', 120),
    (N'PT022', N'Hộp số tự động', 35000000.00, N'Truyền động', 120),
    (N'PT023', N'Két nước', 3200000.00, N'Làm mát', 60),
    (N'PT024', N'Máy phát điện', 4800000.00, N'Điện', 60),
    (N'PT025', N'Máy khởi động', 3800000.00, N'Điện', 60),
    (N'PT026', N'Thước lái', 8500000.00, N'Hệ thống lái', 120),
    (N'PT027', N'Rotuyn lái', 750000.00, N'Hệ thống lái', 60),
    (N'PT028', N'Bạc đạn bánh xe', 950000.00, N'Hệ thống treo', 60),
    (N'PT029', N'Bơm nước', 1850000.00, N'Làm mát', 60),
    (N'PT030', N'Bơm dầu', 2600000.00, N'Động cơ', 60),
    (N'PT031', N'Ron nắp máy', 680000.00, N'Động cơ', 24),
    (N'PT032', N'Cảm biến oxy', 1650000.00, N'Điện', 60),
    (N'PT033', N'Kim phun', 3200000.00, N'Nhiên liệu', 60),
    (N'PT034', N'Lọc nhiên liệu', 450000.00, N'Bộ lọc', 24),
    (N'PT035', N'Dầu phanh', 180000.00, N'Dầu nhớt', 24),
    (N'PT036', N'Dung dịch vệ sinh', 95000.00, N'Vật tư', 24),
    (N'PT037', N'Cầu chì', 15000.00, N'Điện', 24),
    (N'PT038', N'Ốc xả dầu', 25000.00, N'Vật tư', 24),
    (N'PT039', N'Gioăng tặng kèm', 0.00, N'Vật tư', 24),
    (N'PT040', N'Phụ tùng bảo hành miễn phí', 0.00, N'Bảo hành', 24),
    (N'PT041', N'Bugi Iridium Denso', 320000.00, N'Đánh lửa', 24),
    (N'PT042', N'Dây curoa tổng Gates', 850000.00, N'Truyền động', 24),
    (N'PT043', N'Má phanh sau Brembo', 1150000.00, N'Hệ thống phanh', 24),
    (N'PT044', N'Giảm xóc sau KYB', 2900000.00, N'Hệ thống treo', 60),
    (N'PT045', N'Càng chữ A dưới', 1750000.00, N'Hệ thống treo', 60),
    (N'PT046', N'Lốc điều hòa Sanden', 7200000.00, N'Điều hòa', 60),
    (N'PT047', N'Cảm biến áp suất lốp TPMS', 1450000.00, N'Điện', 24),
    (N'PT048', N'Cụm bơm xăng Bosch', 3600000.00, N'Nhiên liệu', 60),
    (N'PT049', N'Cụm thước lái trợ lực điện EPS', 14500000.00, N'Hệ thống lái', 120),
    (N'PT050', N'Bộ vi sai cầu sau', 18500000.00, N'Truyền động', 120);

-- NhaCungCap: 12 bản ghi
INSERT INTO dbo.[NhaCungCap] ([maNCC], [tenNCC], [soDienThoai], [email], [diaChi])
VALUES
    (N'NCC001', N'Công ty Phụ tùng An Phát', N'0533000001', N'anphat.parts@gmail.com', N'120 Quốc lộ 1A, Bình Chánh, TP. Hồ Chí Minh'),
    (N'NCC002', N'Công ty Dầu nhớt Minh Long', N'0733000002', N'minhlong.oil@gmail.com', N'45 Xa Lộ Hà Nội, Biên Hòa, Đồng Nai'),
    (N'NCC003', N'Công ty Thiết bị Hưng Thịnh', N'0833000003', NULL, N'88 Đại Lộ Bình Dương, Thuận An, Bình Dương'),
    (N'NCC004', N'Đại lý Lốp Ô tô Việt Phát', N'0933000004', N'vietphat.tires@gmail.com', NULL),
    (N'NCC005', N'Công ty Điện ô tô Thành Công', N'0333000005', N'thanhcong.auto@gmail.com', N'35 Đường 30/4, TP. Tây Ninh'),
    (N'NCC006', N'Công ty Vật tư Đông Nam', N'0533000006', NULL, N'16 Ba Cu, TP. Vũng Tàu'),
    (N'NCC007', N'Công ty Phụ tùng Nhật Việt', N'0733000007', N'nhatviet.autoparts@gmail.com', N'72 Cách Mạng Tháng 8, Cần Thơ'),
    (N'NCC008', N'Công ty Phụ tùng Hàn Việt', N'0833000008', N'hanviet.parts@gmail.com', NULL),
    (N'NCC009', N'Công ty Cơ khí Ô tô Bình Minh', N'0933000009', NULL, N'250 Giải Phóng, Hoàng Mai, Hà Nội'),
    (N'NCC010', N'Công ty Phụ kiện Đại Phúc', N'0333000010', N'daiphuc.auto@gmail.com', N'90 2 Tháng 4, Nha Trang, Khánh Hòa'),
    (N'NCC011', N'Công ty Bảo dưỡng Toàn Cầu', N'0533000011', N'toancau.service@gmail.com', N'14 Trần Phú, TP. Đà Lạt, Lâm Đồng'),
    (N'NCC012', N'Công ty Vật tư Phú Gia', N'0733000012', NULL, NULL);

-- PhuongThucThanhToan: 4 bản ghi
INSERT INTO dbo.[PhuongThucThanhToan] ([maPhuongThuc], [tenPhuongThuc])
VALUES
    (N'TT001', N'Tiền mặt'),
    (N'TT002', N'Chuyển khoản QR Code'),
    (N'TT003', N'Chuyển khoản ngân hàng (Internet Banking)'),
    (N'TT004', N'Thẻ thanh toán POS (Visa/Mastercard/Napas)');

-- TaiKhoan: 4 thành viên và 1 admin
INSERT INTO dbo.TaiKhoan
    (maTaiKhoan, maNhanVien, tenDangNhap, matKhau, trangThai, ngayTao)
VALUES
    ('TK001', 'NV001', 'minh123',  'minh123',  N'Đang hoạt động', '20261012'),
    ('TK002', 'NV002', 'kien123',  'kien123',  N'Đang hoạt động', '20261012'),
    ('TK003', 'NV003', 'chau123',  'chau123',  N'Đang hoạt động', '20261012'),
    ('TK004', 'NV004', 'khanh123', 'khanh123', N'Đang hoạt động', '20261012'),
    ('TK005', 'NV005', 'admin', 'admin', N'Đang hoạt động', '20261012'),
    ('TK006', 'NV006', 'quan123', 'quan123', N'Đang hoạt động', '20261012'),
    ('TK007', 'NV007', 'phuong123', 'phuong123', N'Đang hoạt động', '20261012'),
    ('TK008', 'NV008', 'hung123', 'hung123', N'Đang hoạt động', '20261012'),
    ('TK009', 'NV009', 'dat123', 'dat123', N'Đang hoạt động', '20261012'),
    ('TK010', 'NV010', 'tam123', 'tam123', N'Đang hoạt động', '20261012'),
    ('TK011', 'NV011', 'long123', 'long123', N'Đang hoạt động', '20261012'),
    ('TK012', 'NV012', 'phuc123', 'phuc123', N'Đang hoạt động', '20261012'),
    ('TK013', 'NV013', 'ha123', 'ha123', N'Đang hoạt động', '20261012');

-- TonKho: 50 bản ghi
INSERT INTO dbo.[TonKho] ([maTonKho], [maPhuTung], [soLuongHienTai], [soLuongToiThieu])
VALUES
    (N'TKH001', N'PT001', 12, 6),
    (N'TKH002', N'PT002', 7, 7),
    (N'TKH003', N'PT003', 10, 8),
    (N'TKH004', N'PT004', 14, 9),
    (N'TKH005', N'PT005', 15, 10),
    (N'TKH006', N'PT006', 5, 5),
    (N'TKH007', N'PT007', 0, 0),
    (N'TKH008', N'PT008', 8, 7),
    (N'TKH009', N'PT009', 59, 8),
    (N'TKH010', N'PT010', 0, 9),
    (N'TKH011', N'PT011', 10, 10),
    (N'TKH012', N'PT012', 4, 5),
    (N'TKH013', N'PT013', 7, 6),
    (N'TKH014', N'PT014', 64, 0),
    (N'TKH015', N'PT015', 0, 8),
    (N'TKH016', N'PT016', 9, 9),
    (N'TKH017', N'PT017', 9, 10),
    (N'TKH018', N'PT018', 6, 5),
    (N'TKH019', N'PT019', 69, 6),
    (N'TKH020', N'PT020', 0, 7),
    (N'TKH021', N'PT021', 0, 0),
    (N'TKH022', N'PT022', 8, 9),
    (N'TKH023', N'PT023', 11, 10),
    (N'TKH024', N'PT024', 74, 5),
    (N'TKH025', N'PT025', 0, 6),
    (N'TKH026', N'PT026', 7, 7),
    (N'TKH027', N'PT027', 7, 8),
    (N'TKH028', N'PT028', 1, 0),
    (N'TKH029', N'PT029', 79, 10),
    (N'TKH030', N'PT030', 0, 5),
    (N'TKH031', N'PT031', 6, 6),
    (N'TKH032', N'PT032', 6, 7),
    (N'TKH033', N'PT033', 9, 8),
    (N'TKH034', N'PT034', 84, 9),
    (N'TKH035', N'PT035', 0, 0),
    (N'TKH036', N'PT036', 5, 5),
    (N'TKH037', N'PT037', 5, 6),
    (N'TKH038', N'PT038', 8, 7),
    (N'TKH039', N'PT039', 89, 8),
    (N'TKH040', N'PT040', 0, 9),
    (N'TKH041', N'PT041', 15, 5),
    (N'TKH042', N'PT042', 12, 4),
    (N'TKH043', N'PT043', 20, 6),
    (N'TKH044', N'PT044', 8, 3),
    (N'TKH045', N'PT045', 10, 4),
    (N'TKH046', N'PT046', 5, 2),
    (N'TKH047', N'PT047', 25, 8),
    (N'TKH048', N'PT048', 6, 2),
    (N'TKH049', N'PT049', 4, 1),
    (N'TKH050', N'PT050', 3, 1);

-- MaGiamGia: 36 bản ghi
INSERT INTO dbo.[MaGiamGia] ([maGiamGia], [maKhachHang], [mucGiam], [ngayTao], [ngayHetHan], [trangThai])
VALUES
    (N'MGG001', N'KH041', 0.00, DATEADD(minute,0,DATEADD(day,-583,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-223,@NgayMau)), N'Đã sử dụng'),
    (N'MGG002', N'KH042', 5.00, DATEADD(minute,0,DATEADD(day,-596,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-236,@NgayMau)), N'Đã sử dụng'),
    (N'MGG003', N'KH043', 10.00, DATEADD(minute,0,DATEADD(day,-609,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-249,@NgayMau)), N'Đã sử dụng'),
    (N'MGG004', N'KH044', 15.00, DATEADD(minute,0,DATEADD(day,-622,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-262,@NgayMau)), N'Đã sử dụng'),
    (N'MGG005', N'KH045', 20.00, DATEADD(minute,0,DATEADD(day,-635,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-275,@NgayMau)), N'Đã sử dụng'),
    (N'MGG006', N'KH046', 30.00, DATEADD(minute,0,DATEADD(day,-648,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-288,@NgayMau)), N'Đã sử dụng'),
    (N'MGG007', N'KH047', 0.00, DATEADD(minute,0,DATEADD(day,-661,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-301,@NgayMau)), N'Đã sử dụng'),
    (N'MGG008', N'KH048', 5.00, DATEADD(minute,0,DATEADD(day,-674,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-314,@NgayMau)), N'Đã sử dụng'),
    (N'MGG009', N'KH001', 10.00, DATEADD(minute,0,DATEADD(day,-687,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-327,@NgayMau)), N'Đã sử dụng'),
    (N'MGG010', N'KH002', 15.00, DATEADD(minute,0,DATEADD(day,-50,@NgayMau)), DATEADD(minute,1439,DATEADD(day,310,@NgayMau)), N'Đã sử dụng'),
    (N'MGG011', N'KH003', 20.00, DATEADD(minute,0,DATEADD(day,-63,@NgayMau)), DATEADD(minute,1439,DATEADD(day,297,@NgayMau)), N'Đã sử dụng'),
    (N'MGG012', N'KH004', 30.00, DATEADD(minute,0,DATEADD(day,-76,@NgayMau)), DATEADD(minute,1439,DATEADD(day,284,@NgayMau)), N'Đã sử dụng'),
    (N'MGG013', N'KH031', 0.00, DATEADD(minute,0,DATEADD(day,-800,@NgayMau)), DATEADD(minute,1439,DATEADD(day,365,@NgayMau)), N'Đang hiệu lực'),
    (N'MGG014', N'KH032', 5.00, DATEADD(minute,0,DATEADD(day,-800,@NgayMau)), DATEADD(minute,1439,DATEADD(day,365,@NgayMau)), N'Đang hiệu lực'),
    (N'MGG015', N'KH033', 10.00, DATEADD(minute,0,DATEADD(day,-800,@NgayMau)), DATEADD(minute,1439,DATEADD(day,365,@NgayMau)), N'Đang hiệu lực'),
    (N'MGG016', N'KH034', 15.00, DATEADD(minute,0,DATEADD(day,-800,@NgayMau)), DATEADD(minute,1439,DATEADD(day,365,@NgayMau)), N'Đang hiệu lực'),
    (N'MGG017', N'KH035', 20.00, DATEADD(minute,0,DATEADD(day,-800,@NgayMau)), DATEADD(minute,1439,DATEADD(day,365,@NgayMau)), N'Đang hiệu lực'),
    (N'MGG018', N'KH036', 30.00, DATEADD(minute,0,DATEADD(day,-800,@NgayMau)), DATEADD(minute,1439,DATEADD(day,365,@NgayMau)), N'Đang hiệu lực'),
    (N'MGG019', N'KH037', 0.00, DATEADD(minute,0,DATEADD(day,-800,@NgayMau)), DATEADD(minute,1439,DATEADD(day,365,@NgayMau)), N'Đang hiệu lực'),
    (N'MGG020', N'KH038', 5.00, DATEADD(minute,0,DATEADD(day,-800,@NgayMau)), DATEADD(minute,1439,DATEADD(day,365,@NgayMau)), N'Đang hiệu lực'),
    (N'MGG021', N'KH039', 10.00, DATEADD(minute,0,DATEADD(day,-800,@NgayMau)), DATEADD(minute,1439,DATEADD(day,365,@NgayMau)), N'Đang hiệu lực'),
    (N'MGG022', N'KH040', 15.00, DATEADD(minute,0,DATEADD(day,-800,@NgayMau)), DATEADD(minute,1439,DATEADD(day,365,@NgayMau)), N'Đang hiệu lực'),
    (N'MGG023', N'KH041', 20.00, DATEADD(minute,0,DATEADD(day,-800,@NgayMau)), DATEADD(minute,1439,DATEADD(day,365,@NgayMau)), N'Đang hiệu lực'),
    (N'MGG024', N'KH042', 30.00, DATEADD(minute,0,DATEADD(day,-800,@NgayMau)), DATEADD(minute,1439,DATEADD(day,365,@NgayMau)), N'Đang hiệu lực'),
    (N'MGG025', N'KH001', 0.00, DATEADD(minute,0,DATEADD(day,-400,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-35,@NgayMau)), N'Hết hạn'),
    (N'MGG026', N'KH002', 5.00, DATEADD(minute,0,DATEADD(day,-400,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-35,@NgayMau)), N'Hết hạn'),
    (N'MGG027', N'KH003', 10.00, DATEADD(minute,0,DATEADD(day,-400,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-35,@NgayMau)), N'Hết hạn'),
    (N'MGG028', N'KH004', 15.00, DATEADD(minute,0,DATEADD(day,-400,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-35,@NgayMau)), N'Hết hạn'),
    (N'MGG029', N'KH005', 20.00, DATEADD(minute,0,DATEADD(day,-400,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-35,@NgayMau)), N'Hết hạn'),
    (N'MGG030', N'KH006', 30.00, DATEADD(minute,0,DATEADD(day,-400,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-35,@NgayMau)), N'Hết hạn'),
    (N'MGG031', N'KH007', 0.00, DATEADD(minute,0,DATEADD(day,-400,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-35,@NgayMau)), N'Hết hạn'),
    (N'MGG032', N'KH008', 5.00, DATEADD(minute,0,DATEADD(day,-400,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-35,@NgayMau)), N'Hết hạn'),
    (N'MGG033', N'KH009', 10.00, DATEADD(minute,0,DATEADD(day,-400,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-35,@NgayMau)), N'Hết hạn'),
    (N'MGG034', N'KH010', 15.00, DATEADD(minute,0,DATEADD(day,-400,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-35,@NgayMau)), N'Hết hạn'),
    (N'MGG035', N'KH011', 20.00, DATEADD(minute,0,DATEADD(day,-400,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-35,@NgayMau)), N'Hết hạn'),
    (N'MGG036', N'KH012', 30.00, DATEADD(minute,0,DATEADD(day,-400,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-35,@NgayMau)), N'Hết hạn');

-- PhieuTiepNhan: 100 bản ghi
INSERT INTO dbo.[PhieuTiepNhan] ([maPTN], [maKhachHang], [maNhanVien], [VIN], [ngayTiepNhan], [soODO])
VALUES
    (N'PTN001', N'KH001', N'NV001', N'MR0BT9F31JA310001', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'000000'),
    (N'PTN002', N'KH002', N'NV001', N'MRHGM6627FA310002', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'003436'),
    (N'PTN003', N'KH003', N'NV001', N'KMHCT41B5JA310003', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'005154'),
    (N'PTN004', N'KH004', N'NV001', N'KNDEP81A3LA310004', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'006872'),
    (N'PTN005', N'KH005', N'NV001', N'JM3KF2W77HA310005', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'008590'),
    (N'PTN006', N'KH006', N'NV001', N'MNCUMFF55FA310006', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'010308'),
    (N'PTN007', N'KH007', N'NV001', N'MK2NC1W48KA310007', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'012026'),
    (N'PTN008', N'KH008', N'NV001', N'MHYANC226MA310008', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'013744'),
    (N'PTN009', N'KH009', N'NV001', N'MNTCB4D20GA310009', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'015462'),
    (N'PTN010', N'KH010', N'NV001', N'WDDWF4JB5FA310010', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'017180'),
    (N'PTN011', N'KH011', N'NV001', N'WBA5R1105KA310011', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'018898'),
    (N'PTN012', N'KH012', N'NV001', N'RLLV1AFA6PA310012', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'020616'),
    (N'PTN013', N'KH013', N'NV001', N'MR0BT9F36KA310013', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'022334'),
    (N'PTN014', N'KH014', N'NV001', N'MRHGM6621GA310014', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'024052'),
    (N'PTN015', N'KH015', N'NV001', N'KMHCT41BXKA310015', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'025770'),
    (N'PTN016', N'KH016', N'NV001', N'KNDEP81A8MA310016', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'027488'),
    (N'PTN017', N'KH017', N'NV001', N'JM3KF2W76JA310017', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'029206'),
    (N'PTN018', N'KH018', N'NV001', N'MNCUMFF5XGA310018', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'030924'),
    (N'PTN019', N'KH019', N'NV001', N'MK2NC1W42LA310019', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'032642'),
    (N'PTN020', N'KH020', N'NV001', N'MHYANC225NA310020', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'034360'),
    (N'PTN021', N'KH021', N'NV001', N'MNTCB4D2XHA310021', DATEADD(minute,480,DATEADD(day,-293,@NgayMau)), N'036078'),
    (N'PTN022', N'KH022', N'NV001', N'WDDWF4JBXGA310022', DATEADD(minute,480,DATEADD(day,-306,@NgayMau)), N'037796'),
    (N'PTN023', N'KH023', N'NV001', N'WBA5R110XLA310023', DATEADD(minute,480,DATEADD(day,-319,@NgayMau)), N'039514'),
    (N'PTN024', N'KH024', N'NV001', N'RLLV1AFA9RA310024', DATEADD(minute,480,DATEADD(day,-332,@NgayMau)), N'041232'),
    (N'PTN025', N'KH025', N'NV001', N'MR0BT9F30LA310025', DATEADD(minute,480,DATEADD(day,-345,@NgayMau)), N'042950'),
    (N'PTN026', N'KH026', N'NV001', N'MRHGM6626HA310026', DATEADD(minute,480,DATEADD(day,-358,@NgayMau)), N'044668'),
    (N'PTN027', N'KH027', N'NV001', N'KMHCT41B4LA310027', DATEADD(minute,480,DATEADD(day,-371,@NgayMau)), N'046386'),
    (N'PTN028', N'KH028', N'NV001', N'KNDEP81A2NA310028', DATEADD(minute,480,DATEADD(day,-384,@NgayMau)), N'048104'),
    (N'PTN029', N'KH029', N'NV001', N'JM3KF2W70KA310029', DATEADD(minute,480,DATEADD(day,-397,@NgayMau)), N'049822'),
    (N'PTN030', N'KH030', N'NV001', N'MNCUMFF59HA310030', DATEADD(minute,480,DATEADD(day,-410,@NgayMau)), N'051540'),
    (N'PTN031', N'KH031', N'NV001', N'MK2NC1W41MA310031', DATEADD(minute,480,DATEADD(day,-423,@NgayMau)), N'053258'),
    (N'PTN032', N'KH032', N'NV001', N'MHYANC228PA310032', DATEADD(minute,480,DATEADD(day,-436,@NgayMau)), N'054976'),
    (N'PTN033', N'KH033', N'NV001', N'MNTCB4D29JA310033', DATEADD(minute,480,DATEADD(day,-449,@NgayMau)), N'056694'),
    (N'PTN034', N'KH034', N'NV001', N'WDDWF4JB4HA310034', DATEADD(minute,480,DATEADD(day,-462,@NgayMau)), N'058412'),
    (N'PTN035', N'KH035', N'NV001', N'WBA5R1104MA310035', DATEADD(minute,480,DATEADD(day,-475,@NgayMau)), N'060130'),
    (N'PTN036', N'KH036', N'NV001', N'RLLV1AFA8SA310036', DATEADD(minute,480,DATEADD(day,-488,@NgayMau)), N'061848'),
    (N'PTN037', N'KH037', N'NV001', N'MR0BT9F35MA310037', DATEADD(minute,480,DATEADD(day,-501,@NgayMau)), N'063566'),
    (N'PTN038', N'KH038', N'NV001', N'MRHGM6625JA310038', DATEADD(minute,480,DATEADD(day,-514,@NgayMau)), N'065284'),
    (N'PTN039', N'KH039', N'NV001', N'KMHCT41B9MA310039', DATEADD(minute,480,DATEADD(day,-527,@NgayMau)), N'067002'),
    (N'PTN040', N'KH040', N'NV001', N'KNDEP81AXPA310040', DATEADD(minute,480,DATEADD(day,-540,@NgayMau)), N'068720'),
    (N'PTN041', N'KH041', N'NV001', N'JM3KF2W7XLA310041', DATEADD(minute,480,DATEADD(day,-553,@NgayMau)), N'070438'),
    (N'PTN042', N'KH042', N'NV001', N'MNCUMFF58JA310042', DATEADD(minute,480,DATEADD(day,-566,@NgayMau)), N'072156'),
    (N'PTN043', N'KH043', N'NV001', N'MK2NC1W46NA310043', DATEADD(minute,480,DATEADD(day,-579,@NgayMau)), N'073874'),
    (N'PTN044', N'KH044', N'NV001', N'MHYANC22XMA310044', DATEADD(minute,480,DATEADD(day,-592,@NgayMau)), N'075592'),
    (N'PTN045', N'KH045', N'NV001', N'MNTCB4D23KA310045', DATEADD(minute,480,DATEADD(day,-605,@NgayMau)), N'077310'),
    (N'PTN046', N'KH046', N'NV001', N'WDDWF4JB3JA310046', DATEADD(minute,480,DATEADD(day,-618,@NgayMau)), N'079028'),
    (N'PTN047', N'KH047', N'NV001', N'WBA5R1109NA310047', DATEADD(minute,480,DATEADD(day,-631,@NgayMau)), N'080746'),
    (N'PTN048', N'KH048', N'NV001', N'RLLV1AFA5PA310048', DATEADD(minute,480,DATEADD(day,-644,@NgayMau)), N'082464'),
    (N'PTN049', N'KH001', N'NV001', N'MR0BT9F3XNA310049', DATEADD(minute,480,DATEADD(day,-657,@NgayMau)), N'084182'),
    (N'PTN050', N'KH002', N'NV001', N'MRHGM6624KA310050', DATEADD(minute,480,DATEADD(day,-20,@NgayMau)), N'085900'),
    (N'PTN051', N'KH003', N'NV001', N'KMHCT41B8NA310051', DATEADD(minute,480,DATEADD(day,-33,@NgayMau)), N'087618'),
    (N'PTN052', N'KH004', N'NV001', N'KNDEP81A3LA310052', DATEADD(minute,480,DATEADD(day,-46,@NgayMau)), N'089336'),
    (N'PTN053', N'KH005', N'NV001', N'JM3KF2W74MA310053', DATEADD(minute,480,DATEADD(day,-59,@NgayMau)), N'091054'),
    (N'PTN054', N'KH006', N'NV001', N'MNCUMFF52KA310054', DATEADD(minute,480,DATEADD(day,-72,@NgayMau)), N'092772'),
    (N'PTN055', N'KH007', N'NV001', N'MK2NC1W49PA310055', DATEADD(minute,480,DATEADD(day,-85,@NgayMau)), N'094490'),
    (N'PTN056', N'KH008', N'NV001', N'MHYANC224NA310056', DATEADD(minute,480,DATEADD(day,-98,@NgayMau)), N'096208'),
    (N'PTN057', N'KH009', N'NV001', N'MNTCB4D28LA310057', DATEADD(minute,480,DATEADD(day,-111,@NgayMau)), N'097926'),
    (N'PTN058', N'KH010', N'NV001', N'WDDWF4JB0FA310058', DATEADD(minute,480,DATEADD(day,-124,@NgayMau)), N'099644'),
    (N'PTN059', N'KH011', N'NV001', N'WBA5R1101PA310059', DATEADD(minute,480,DATEADD(day,-137,@NgayMau)), N'101362'),
    (N'PTN060', N'KH012', N'NV001', N'RLLV1AFA2RA310060', DATEADD(minute,480,DATEADD(day,-150,@NgayMau)), N'103080'),
    (N'PTN061', N'KH013', N'NV001', N'MR0BT9F37PA310061', DATEADD(minute,480,DATEADD(day,-163,@NgayMau)), N'104798'),
    (N'PTN062', N'KH014', N'NV001', N'MRHGM6623FA310062', DATEADD(minute,480,DATEADD(day,-176,@NgayMau)), N'106516'),
    (N'PTN063', N'KH015', N'NV001', N'KMHCT41B0PA310063', DATEADD(minute,480,DATEADD(day,-189,@NgayMau)), N'108234'),
    (N'PTN064', N'KH016', N'NV001', N'KNDEP81A8MA310064', DATEADD(minute,480,DATEADD(day,-202,@NgayMau)), N'109952'),
    (N'PTN065', N'KH017', N'NV001', N'JM3KF2W79NA310065', DATEADD(minute,480,DATEADD(day,-215,@NgayMau)), N'111670'),
    (N'PTN066', N'KH018', N'NV001', N'MNCUMFF57LA310066', DATEADD(minute,480,DATEADD(day,-228,@NgayMau)), N'113388'),
    (N'PTN067', N'KH019', N'NV001', N'MK2NC1W44KA310067', DATEADD(minute,480,DATEADD(day,-241,@NgayMau)), N'115106'),
    (N'PTN068', N'KH020', N'NV001', N'MHYANC227PA310068', DATEADD(minute,480,DATEADD(day,-254,@NgayMau)), N'116824'),
    (N'PTN069', N'KH021', N'NV001', N'MNTCB4D27GA310069', DATEADD(minute,480,DATEADD(day,-267,@NgayMau)), N'118542'),
    (N'PTN070', N'KH022', N'NV001', N'WDDWF4JBXGA310070', DATEADD(minute,480,DATEADD(day,-280,@NgayMau)), N'120260'),
    (N'PTN071', N'KH023', N'NV001', N'WBA5R1101KA310071', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'121978'),
    (N'PTN072', N'KH024', N'NV001', N'RLLV1AFA1SA310072', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'123696'),
    (N'PTN073', N'KH001', N'NV001', N'MR0BT9F31JA310001', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'000000'),
    (N'PTN074', N'KH002', N'NV001', N'MRHGM6627FA310002', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'002356'),
    (N'PTN075', N'KH003', N'NV001', N'KMHCT41B5JA310003', DATEADD(minute,480,DATEADD(day,-2,@NgayMau)), N'004074'),
    (N'PTN076', N'KH004', N'NV001', N'KNDEP81A3LA310004', DATEADD(minute,480,DATEADD(day,-358,@NgayMau)), N'005792'),
    (N'PTN077', N'KH005', N'NV001', N'JM3KF2W77HA310005', DATEADD(minute,480,DATEADD(day,-371,@NgayMau)), N'007510'),
    (N'PTN078', N'KH006', N'NV001', N'MNCUMFF55FA310006', DATEADD(minute,480,DATEADD(day,-384,@NgayMau)), N'009228'),
    (N'PTN079', N'KH007', N'NV001', N'MK2NC1W48KA310007', DATEADD(minute,480,DATEADD(day,-397,@NgayMau)), N'010946'),
    (N'PTN080', N'KH008', N'NV001', N'MHYANC226MA310008', DATEADD(minute,480,DATEADD(day,-410,@NgayMau)), N'012664'),
    (N'PTN081', N'KH009', N'NV001', N'MNTCB4D20GA310009', DATEADD(minute,480,DATEADD(day,-423,@NgayMau)), N'014382'),
    (N'PTN082', N'KH010', N'NV001', N'WDDWF4JB5FA310010', DATEADD(minute,480,DATEADD(day,-436,@NgayMau)), N'016100'),
    (N'PTN083', N'KH011', N'NV001', N'WBA5R1105KA310011', DATEADD(minute,480,DATEADD(day,-449,@NgayMau)), N'017818'),
    (N'PTN084', N'KH012', N'NV001', N'RLLV1AFA6PA310012', DATEADD(minute,480,DATEADD(day,-462,@NgayMau)), N'019536'),
    (N'PTN085', N'KH013', N'NV001', N'MR0BT9F36KA310013', DATEADD(minute,480,DATEADD(day,-475,@NgayMau)), N'021254'),
    (N'PTN086', N'KH014', N'NV001', N'MRHGM6621GA310014', DATEADD(minute,480,DATEADD(day,-488,@NgayMau)), N'022972'),
    (N'PTN087', N'KH015', N'NV001', N'KMHCT41BXKA310015', DATEADD(minute,480,DATEADD(day,-501,@NgayMau)), N'024690'),
    (N'PTN088', N'KH016', N'NV001', N'KNDEP81A8MA310016', DATEADD(minute,480,DATEADD(day,-514,@NgayMau)), N'026408'),
    (N'PTN089', N'KH017', N'NV001', N'JM3KF2W76JA310017', DATEADD(minute,480,DATEADD(day,-527,@NgayMau)), N'028126'),
    (N'PTN090', N'KH018', N'NV001', N'MNCUMFF5XGA310018', DATEADD(minute,480,DATEADD(day,-540,@NgayMau)), N'029844'),
    (N'PTN091', N'KH019', N'NV001', N'MK2NC1W42LA310019', DATEADD(minute,480,DATEADD(day,-553,@NgayMau)), N'031562'),
    (N'PTN092', N'KH020', N'NV001', N'MHYANC225NA310020', DATEADD(minute,480,DATEADD(day,-566,@NgayMau)), N'033280'),
    (N'PTN093', N'KH021', N'NV001', N'MNTCB4D2XHA310021', DATEADD(minute,480,DATEADD(day,-579,@NgayMau)), N'034998'),
    (N'PTN094', N'KH022', N'NV001', N'WDDWF4JBXGA310022', DATEADD(minute,480,DATEADD(day,-592,@NgayMau)), N'036716'),
    (N'PTN095', N'KH023', N'NV001', N'WBA5R110XLA310023', DATEADD(minute,480,DATEADD(day,-605,@NgayMau)), N'038434'),
    (N'PTN096', N'KH024', N'NV001', N'RLLV1AFA9RA310024', DATEADD(minute,480,DATEADD(day,-618,@NgayMau)), N'040152'),
    (N'PTN097', N'KH025', N'NV001', N'MR0BT9F30LA310025', DATEADD(minute,480,DATEADD(day,-631,@NgayMau)), N'041870'),
    (N'PTN098', N'KH026', N'NV001', N'MRHGM6626HA310026', DATEADD(minute,480,DATEADD(day,-644,@NgayMau)), N'043588'),
    (N'PTN099', N'KH027', N'NV001', N'KMHCT41B4LA310027', DATEADD(minute,480,DATEADD(day,-657,@NgayMau)), N'045306'),
    (N'PTN100', N'KH028', N'NV001', N'KNDEP81A2NA310028', DATEADD(minute,480,DATEADD(day,-20,@NgayMau)), N'047024');

-- ChiTietPhieuTiepNhan: 200 bản ghi
INSERT INTO dbo.[ChiTietPhieuTiepNhan] ([maPTN], [maDichVu], [yeuCauDichVu])
VALUES
    (N'PTN001', N'DV001', N'Khách yêu cầu thay dầu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN001', N'DV002', N'Khách yêu cầu thay lọc dầu; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN002', N'DV002', N'Khách yêu cầu thay lọc dầu; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN002', N'DV003', N'Khách yêu cầu kiểm tra phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN003', N'DV003', N'Khách yêu cầu kiểm tra phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN003', N'DV004', N'Khách yêu cầu thay má phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN004', N'DV004', N'Khách yêu cầu thay má phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN004', N'DV005', N'Khách yêu cầu thay lọc gió điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN005', N'DV005', N'Khách yêu cầu thay lọc gió điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN005', N'DV006', N'Khách yêu cầu vệ sinh điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN006', N'DV006', N'Khách yêu cầu vệ sinh điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN006', N'DV007', N'Khách yêu cầu thay ắc quy; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN007', N'DV007', N'Khách yêu cầu thay ắc quy; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN007', N'DV008', N'Khách yêu cầu thay lốp xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN008', N'DV008', N'Khách yêu cầu thay lốp xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN008', N'DV009', N'Khách yêu cầu cân bằng động bánh xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN009', N'DV009', N'Khách yêu cầu cân bằng động bánh xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN009', N'DV010', N'Khách yêu cầu thay bugi; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN010', N'DV010', N'Khách yêu cầu thay bugi; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN010', N'DV011', N'Khách yêu cầu thay dây curoa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN011', N'DV011', N'Khách yêu cầu thay dây curoa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN011', N'DV012', N'Khách yêu cầu thay nước làm mát; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN012', N'DV012', N'Khách yêu cầu thay nước làm mát; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN012', N'DV013', N'Khách yêu cầu thay giảm xóc; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN013', N'DV013', N'Khách yêu cầu thay giảm xóc; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN013', N'DV014', N'Khách yêu cầu sửa hệ thống điện; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN014', N'DV014', N'Khách yêu cầu sửa hệ thống điện; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN014', N'DV015', N'Khách yêu cầu đại tu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN015', N'DV015', N'Khách yêu cầu đại tu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN015', N'DV016', N'Khách yêu cầu sửa hộp số; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN016', N'DV016', N'Khách yêu cầu sửa hộp số; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN016', N'DV017', N'Khách yêu cầu bảo dưỡng miễn phí; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN017', N'DV017', N'Khách yêu cầu bảo dưỡng miễn phí; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN017', N'DV001', N'Khách yêu cầu thay dầu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN018', N'DV001', N'Khách yêu cầu thay dầu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN018', N'DV002', N'Khách yêu cầu thay lọc dầu; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN019', N'DV002', N'Khách yêu cầu thay lọc dầu; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN019', N'DV003', N'Khách yêu cầu kiểm tra phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN020', N'DV003', N'Khách yêu cầu kiểm tra phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN020', N'DV004', N'Khách yêu cầu thay má phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN021', N'DV004', N'Khách yêu cầu thay má phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN021', N'DV005', N'Khách yêu cầu thay lọc gió điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN022', N'DV005', N'Khách yêu cầu thay lọc gió điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN022', N'DV006', N'Khách yêu cầu vệ sinh điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN023', N'DV006', N'Khách yêu cầu vệ sinh điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN023', N'DV007', N'Khách yêu cầu thay ắc quy; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN024', N'DV007', N'Khách yêu cầu thay ắc quy; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN024', N'DV008', N'Khách yêu cầu thay lốp xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN025', N'DV008', N'Khách yêu cầu thay lốp xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN025', N'DV009', N'Khách yêu cầu cân bằng động bánh xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN026', N'DV009', N'Khách yêu cầu cân bằng động bánh xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN026', N'DV010', N'Khách yêu cầu thay bugi; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN027', N'DV010', N'Khách yêu cầu thay bugi; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN027', N'DV011', N'Khách yêu cầu thay dây curoa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN028', N'DV011', N'Khách yêu cầu thay dây curoa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN028', N'DV012', N'Khách yêu cầu thay nước làm mát; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN029', N'DV012', N'Khách yêu cầu thay nước làm mát; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN029', N'DV013', N'Khách yêu cầu thay giảm xóc; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN030', N'DV013', N'Khách yêu cầu thay giảm xóc; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN030', N'DV014', N'Khách yêu cầu sửa hệ thống điện; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN031', N'DV014', N'Khách yêu cầu sửa hệ thống điện; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN031', N'DV015', N'Khách yêu cầu đại tu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN032', N'DV015', N'Khách yêu cầu đại tu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN032', N'DV016', N'Khách yêu cầu sửa hộp số; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN033', N'DV016', N'Khách yêu cầu sửa hộp số; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN033', N'DV017', N'Khách yêu cầu bảo dưỡng miễn phí; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN034', N'DV017', N'Khách yêu cầu bảo dưỡng miễn phí; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN034', N'DV001', N'Khách yêu cầu thay dầu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN035', N'DV001', N'Khách yêu cầu thay dầu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN035', N'DV002', N'Khách yêu cầu thay lọc dầu; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN036', N'DV002', N'Khách yêu cầu thay lọc dầu; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN036', N'DV003', N'Khách yêu cầu kiểm tra phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN037', N'DV003', N'Khách yêu cầu kiểm tra phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN037', N'DV004', N'Khách yêu cầu thay má phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN038', N'DV004', N'Khách yêu cầu thay má phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN038', N'DV005', N'Khách yêu cầu thay lọc gió điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN039', N'DV005', N'Khách yêu cầu thay lọc gió điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN039', N'DV006', N'Khách yêu cầu vệ sinh điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN040', N'DV006', N'Khách yêu cầu vệ sinh điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN040', N'DV007', N'Khách yêu cầu thay ắc quy; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN041', N'DV007', N'Khách yêu cầu thay ắc quy; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN041', N'DV008', N'Khách yêu cầu thay lốp xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN042', N'DV008', N'Khách yêu cầu thay lốp xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN042', N'DV009', N'Khách yêu cầu cân bằng động bánh xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN043', N'DV009', N'Khách yêu cầu cân bằng động bánh xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN043', N'DV010', N'Khách yêu cầu thay bugi; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN044', N'DV010', N'Khách yêu cầu thay bugi; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN044', N'DV011', N'Khách yêu cầu thay dây curoa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN045', N'DV011', N'Khách yêu cầu thay dây curoa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN045', N'DV012', N'Khách yêu cầu thay nước làm mát; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN046', N'DV012', N'Khách yêu cầu thay nước làm mát; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN046', N'DV013', N'Khách yêu cầu thay giảm xóc; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN047', N'DV013', N'Khách yêu cầu thay giảm xóc; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN047', N'DV014', N'Khách yêu cầu sửa hệ thống điện; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN048', N'DV014', N'Khách yêu cầu sửa hệ thống điện; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN048', N'DV015', N'Khách yêu cầu đại tu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN049', N'DV015', N'Khách yêu cầu đại tu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN049', N'DV016', N'Khách yêu cầu sửa hộp số; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN050', N'DV016', N'Khách yêu cầu sửa hộp số; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN050', N'DV017', N'Khách yêu cầu bảo dưỡng miễn phí; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN051', N'DV017', N'Khách yêu cầu bảo dưỡng miễn phí; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN051', N'DV018', N'Khách yêu cầu kiểm tra sau bảo hành; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN052', N'DV017', N'Khách yêu cầu bảo dưỡng miễn phí; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN052', N'DV018', N'Khách yêu cầu kiểm tra sau bảo hành; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN053', N'DV002', N'Khách yêu cầu thay lọc dầu; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN053', N'DV003', N'Khách yêu cầu kiểm tra phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN054', N'DV003', N'Khách yêu cầu kiểm tra phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN054', N'DV004', N'Khách yêu cầu thay má phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN055', N'DV004', N'Khách yêu cầu thay má phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN055', N'DV005', N'Khách yêu cầu thay lọc gió điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN056', N'DV005', N'Khách yêu cầu thay lọc gió điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN056', N'DV006', N'Khách yêu cầu vệ sinh điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN057', N'DV006', N'Khách yêu cầu vệ sinh điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN057', N'DV007', N'Khách yêu cầu thay ắc quy; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN058', N'DV007', N'Khách yêu cầu thay ắc quy; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN058', N'DV008', N'Khách yêu cầu thay lốp xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN059', N'DV008', N'Khách yêu cầu thay lốp xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN059', N'DV009', N'Khách yêu cầu cân bằng động bánh xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN060', N'DV009', N'Khách yêu cầu cân bằng động bánh xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN060', N'DV010', N'Khách yêu cầu thay bugi; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN061', N'DV010', N'Khách yêu cầu thay bugi; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN061', N'DV011', N'Khách yêu cầu thay dây curoa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN062', N'DV011', N'Khách yêu cầu thay dây curoa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN062', N'DV012', N'Khách yêu cầu thay nước làm mát; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN063', N'DV012', N'Khách yêu cầu thay nước làm mát; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN063', N'DV013', N'Khách yêu cầu thay giảm xóc; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN064', N'DV013', N'Khách yêu cầu thay giảm xóc; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN064', N'DV014', N'Khách yêu cầu sửa hệ thống điện; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN065', N'DV014', N'Khách yêu cầu sửa hệ thống điện; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN065', N'DV015', N'Khách yêu cầu đại tu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN066', N'DV015', N'Khách yêu cầu đại tu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN066', N'DV016', N'Khách yêu cầu sửa hộp số; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN067', N'DV016', N'Khách yêu cầu sửa hộp số; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN067', N'DV017', N'Khách yêu cầu bảo dưỡng miễn phí; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN068', N'DV017', N'Khách yêu cầu bảo dưỡng miễn phí; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN068', N'DV001', N'Khách yêu cầu thay dầu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN069', N'DV001', N'Khách yêu cầu thay dầu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN069', N'DV002', N'Khách yêu cầu thay lọc dầu; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN070', N'DV002', N'Khách yêu cầu thay lọc dầu; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN070', N'DV003', N'Khách yêu cầu kiểm tra phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN071', N'DV003', N'Khách yêu cầu kiểm tra phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN071', N'DV004', N'Khách yêu cầu thay má phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN072', N'DV004', N'Khách yêu cầu thay má phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN072', N'DV005', N'Khách yêu cầu thay lọc gió điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN073', N'DV005', N'Khách yêu cầu thay lọc gió điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN073', N'DV006', N'Khách yêu cầu vệ sinh điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN074', N'DV006', N'Khách yêu cầu vệ sinh điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN074', N'DV007', N'Khách yêu cầu thay ắc quy; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN075', N'DV007', N'Khách yêu cầu thay ắc quy; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN075', N'DV008', N'Khách yêu cầu thay lốp xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN076', N'DV008', N'Khách yêu cầu thay lốp xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN076', N'DV009', N'Khách yêu cầu cân bằng động bánh xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN077', N'DV009', N'Khách yêu cầu cân bằng động bánh xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN077', N'DV010', N'Khách yêu cầu thay bugi; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN078', N'DV010', N'Khách yêu cầu thay bugi; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN078', N'DV011', N'Khách yêu cầu thay dây curoa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN079', N'DV011', N'Khách yêu cầu thay dây curoa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN079', N'DV012', N'Khách yêu cầu thay nước làm mát; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN080', N'DV012', N'Khách yêu cầu thay nước làm mát; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN080', N'DV013', N'Khách yêu cầu thay giảm xóc; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN081', N'DV013', N'Khách yêu cầu thay giảm xóc; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN081', N'DV014', N'Khách yêu cầu sửa hệ thống điện; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN082', N'DV014', N'Khách yêu cầu sửa hệ thống điện; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN082', N'DV015', N'Khách yêu cầu đại tu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN083', N'DV015', N'Khách yêu cầu đại tu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN083', N'DV016', N'Khách yêu cầu sửa hộp số; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN084', N'DV016', N'Khách yêu cầu sửa hộp số; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN084', N'DV017', N'Khách yêu cầu bảo dưỡng miễn phí; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN085', N'DV017', N'Khách yêu cầu bảo dưỡng miễn phí; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN085', N'DV001', N'Khách yêu cầu thay dầu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN086', N'DV001', N'Khách yêu cầu thay dầu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN086', N'DV002', N'Khách yêu cầu thay lọc dầu; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN087', N'DV002', N'Khách yêu cầu thay lọc dầu; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN087', N'DV003', N'Khách yêu cầu kiểm tra phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN088', N'DV003', N'Khách yêu cầu kiểm tra phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN088', N'DV004', N'Khách yêu cầu thay má phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN089', N'DV004', N'Khách yêu cầu thay má phanh; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN089', N'DV005', N'Khách yêu cầu thay lọc gió điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN090', N'DV005', N'Khách yêu cầu thay lọc gió điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN090', N'DV006', N'Khách yêu cầu vệ sinh điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN091', N'DV006', N'Khách yêu cầu vệ sinh điều hòa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN091', N'DV007', N'Khách yêu cầu thay ắc quy; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN092', N'DV007', N'Khách yêu cầu thay ắc quy; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN092', N'DV008', N'Khách yêu cầu thay lốp xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN093', N'DV008', N'Khách yêu cầu thay lốp xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN093', N'DV009', N'Khách yêu cầu cân bằng động bánh xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN094', N'DV009', N'Khách yêu cầu cân bằng động bánh xe; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN094', N'DV010', N'Khách yêu cầu thay bugi; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN095', N'DV010', N'Khách yêu cầu thay bugi; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN095', N'DV011', N'Khách yêu cầu thay dây curoa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN096', N'DV011', N'Khách yêu cầu thay dây curoa; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN096', N'DV012', N'Khách yêu cầu thay nước làm mát; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN097', N'DV012', N'Khách yêu cầu thay nước làm mát; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN097', N'DV013', N'Khách yêu cầu thay giảm xóc; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN098', N'DV013', N'Khách yêu cầu thay giảm xóc; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN098', N'DV014', N'Khách yêu cầu sửa hệ thống điện; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN099', N'DV014', N'Khách yêu cầu sửa hệ thống điện; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN099', N'DV015', N'Khách yêu cầu đại tu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN100', N'DV015', N'Khách yêu cầu đại tu động cơ; kiểm tra và báo giá trước khi thực hiện.'),
    (N'PTN100', N'DV016', N'Khách yêu cầu sửa hộp số; kiểm tra và báo giá trước khi thực hiện.');

-- PhieuKiemTra: 90 bản ghi
INSERT INTO dbo.[PhieuKiemTra] ([maPKT], [maPTN], [maNhanVien], [maKhachHang], [VIN], [ngayKiemTra])
VALUES
    (N'PKT001', N'PTN001', N'NV003', N'KH001', N'MR0BT9F31JA310001', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT002', N'PTN002', N'NV003', N'KH002', N'MRHGM6627FA310002', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT003', N'PTN003', N'NV003', N'KH003', N'KMHCT41B5JA310003', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT004', N'PTN004', N'NV003', N'KH004', N'KNDEP81A3LA310004', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT005', N'PTN005', N'NV003', N'KH005', N'JM3KF2W77HA310005', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT006', N'PTN006', N'NV003', N'KH006', N'MNCUMFF55FA310006', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT007', N'PTN007', N'NV003', N'KH007', N'MK2NC1W48KA310007', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT008', N'PTN008', N'NV003', N'KH008', N'MHYANC226MA310008', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT009', N'PTN009', N'NV003', N'KH009', N'MNTCB4D20GA310009', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT010', N'PTN010', N'NV003', N'KH010', N'WDDWF4JB5FA310010', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT011', N'PTN011', N'NV003', N'KH011', N'WBA5R1105KA310011', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT012', N'PTN012', N'NV003', N'KH012', N'RLLV1AFA6PA310012', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT013', N'PTN013', N'NV003', N'KH013', N'MR0BT9F36KA310013', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT014', N'PTN014', N'NV003', N'KH014', N'MRHGM6621GA310014', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT015', N'PTN015', N'NV003', N'KH015', N'KMHCT41BXKA310015', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT016', N'PTN016', N'NV003', N'KH016', N'KNDEP81A8MA310016', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT017', N'PTN017', N'NV003', N'KH017', N'JM3KF2W76JA310017', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT018', N'PTN018', N'NV003', N'KH018', N'MNCUMFF5XGA310018', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT019', N'PTN019', N'NV003', N'KH019', N'MK2NC1W42LA310019', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT020', N'PTN020', N'NV003', N'KH020', N'MHYANC225NA310020', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT021', N'PTN021', N'NV003', N'KH021', N'MNTCB4D2XHA310021', DATEADD(minute,510,DATEADD(day,-293,@NgayMau))),
    (N'PKT022', N'PTN022', N'NV003', N'KH022', N'WDDWF4JBXGA310022', DATEADD(minute,510,DATEADD(day,-306,@NgayMau))),
    (N'PKT023', N'PTN023', N'NV003', N'KH023', N'WBA5R110XLA310023', DATEADD(minute,510,DATEADD(day,-319,@NgayMau))),
    (N'PKT024', N'PTN024', N'NV003', N'KH024', N'RLLV1AFA9RA310024', DATEADD(minute,510,DATEADD(day,-332,@NgayMau))),
    (N'PKT025', N'PTN025', N'NV003', N'KH025', N'MR0BT9F30LA310025', DATEADD(minute,510,DATEADD(day,-345,@NgayMau))),
    (N'PKT026', N'PTN026', N'NV003', N'KH026', N'MRHGM6626HA310026', DATEADD(minute,510,DATEADD(day,-358,@NgayMau))),
    (N'PKT027', N'PTN027', N'NV003', N'KH027', N'KMHCT41B4LA310027', DATEADD(minute,510,DATEADD(day,-371,@NgayMau))),
    (N'PKT028', N'PTN028', N'NV003', N'KH028', N'KNDEP81A2NA310028', DATEADD(minute,510,DATEADD(day,-384,@NgayMau))),
    (N'PKT029', N'PTN029', N'NV003', N'KH029', N'JM3KF2W70KA310029', DATEADD(minute,510,DATEADD(day,-397,@NgayMau))),
    (N'PKT030', N'PTN030', N'NV003', N'KH030', N'MNCUMFF59HA310030', DATEADD(minute,510,DATEADD(day,-410,@NgayMau))),
    (N'PKT031', N'PTN031', N'NV003', N'KH031', N'MK2NC1W41MA310031', DATEADD(minute,510,DATEADD(day,-423,@NgayMau))),
    (N'PKT032', N'PTN032', N'NV003', N'KH032', N'MHYANC228PA310032', DATEADD(minute,510,DATEADD(day,-436,@NgayMau))),
    (N'PKT033', N'PTN033', N'NV003', N'KH033', N'MNTCB4D29JA310033', DATEADD(minute,510,DATEADD(day,-449,@NgayMau))),
    (N'PKT034', N'PTN034', N'NV003', N'KH034', N'WDDWF4JB4HA310034', DATEADD(minute,510,DATEADD(day,-462,@NgayMau))),
    (N'PKT035', N'PTN035', N'NV003', N'KH035', N'WBA5R1104MA310035', DATEADD(minute,510,DATEADD(day,-475,@NgayMau))),
    (N'PKT036', N'PTN036', N'NV003', N'KH036', N'RLLV1AFA8SA310036', DATEADD(minute,510,DATEADD(day,-488,@NgayMau))),
    (N'PKT037', N'PTN037', N'NV003', N'KH037', N'MR0BT9F35MA310037', DATEADD(minute,510,DATEADD(day,-501,@NgayMau))),
    (N'PKT038', N'PTN038', N'NV003', N'KH038', N'MRHGM6625JA310038', DATEADD(minute,510,DATEADD(day,-514,@NgayMau))),
    (N'PKT039', N'PTN039', N'NV003', N'KH039', N'KMHCT41B9MA310039', DATEADD(minute,510,DATEADD(day,-527,@NgayMau))),
    (N'PKT040', N'PTN040', N'NV003', N'KH040', N'KNDEP81AXPA310040', DATEADD(minute,510,DATEADD(day,-540,@NgayMau))),
    (N'PKT041', N'PTN041', N'NV003', N'KH041', N'JM3KF2W7XLA310041', DATEADD(minute,510,DATEADD(day,-553,@NgayMau))),
    (N'PKT042', N'PTN042', N'NV003', N'KH042', N'MNCUMFF58JA310042', DATEADD(minute,510,DATEADD(day,-566,@NgayMau))),
    (N'PKT043', N'PTN043', N'NV003', N'KH043', N'MK2NC1W46NA310043', DATEADD(minute,510,DATEADD(day,-579,@NgayMau))),
    (N'PKT044', N'PTN044', N'NV003', N'KH044', N'MHYANC22XMA310044', DATEADD(minute,510,DATEADD(day,-592,@NgayMau))),
    (N'PKT045', N'PTN045', N'NV003', N'KH045', N'MNTCB4D23KA310045', DATEADD(minute,510,DATEADD(day,-605,@NgayMau))),
    (N'PKT046', N'PTN046', N'NV003', N'KH046', N'WDDWF4JB3JA310046', DATEADD(minute,510,DATEADD(day,-618,@NgayMau))),
    (N'PKT047', N'PTN047', N'NV003', N'KH047', N'WBA5R1109NA310047', DATEADD(minute,510,DATEADD(day,-631,@NgayMau))),
    (N'PKT048', N'PTN048', N'NV003', N'KH048', N'RLLV1AFA5PA310048', DATEADD(minute,510,DATEADD(day,-644,@NgayMau))),
    (N'PKT049', N'PTN049', N'NV003', N'KH001', N'MR0BT9F3XNA310049', DATEADD(minute,510,DATEADD(day,-657,@NgayMau))),
    (N'PKT050', N'PTN050', N'NV003', N'KH002', N'MRHGM6624KA310050', DATEADD(minute,510,DATEADD(day,-20,@NgayMau))),
    (N'PKT051', N'PTN051', N'NV003', N'KH003', N'KMHCT41B8NA310051', DATEADD(minute,510,DATEADD(day,-33,@NgayMau))),
    (N'PKT052', N'PTN052', N'NV003', N'KH004', N'KNDEP81A3LA310052', DATEADD(minute,510,DATEADD(day,-46,@NgayMau))),
    (N'PKT053', N'PTN053', N'NV003', N'KH005', N'JM3KF2W74MA310053', DATEADD(minute,510,DATEADD(day,-59,@NgayMau))),
    (N'PKT054', N'PTN054', N'NV003', N'KH006', N'MNCUMFF52KA310054', DATEADD(minute,510,DATEADD(day,-72,@NgayMau))),
    (N'PKT055', N'PTN055', N'NV003', N'KH007', N'MK2NC1W49PA310055', DATEADD(minute,510,DATEADD(day,-85,@NgayMau))),
    (N'PKT056', N'PTN056', N'NV003', N'KH008', N'MHYANC224NA310056', DATEADD(minute,510,DATEADD(day,-98,@NgayMau))),
    (N'PKT057', N'PTN057', N'NV003', N'KH009', N'MNTCB4D28LA310057', DATEADD(minute,510,DATEADD(day,-111,@NgayMau))),
    (N'PKT058', N'PTN058', N'NV003', N'KH010', N'WDDWF4JB0FA310058', DATEADD(minute,510,DATEADD(day,-124,@NgayMau))),
    (N'PKT059', N'PTN059', N'NV003', N'KH011', N'WBA5R1101PA310059', DATEADD(minute,510,DATEADD(day,-137,@NgayMau))),
    (N'PKT060', N'PTN060', N'NV003', N'KH012', N'RLLV1AFA2RA310060', DATEADD(minute,510,DATEADD(day,-150,@NgayMau))),
    (N'PKT061', N'PTN061', N'NV003', N'KH013', N'MR0BT9F37PA310061', DATEADD(minute,510,DATEADD(day,-163,@NgayMau))),
    (N'PKT062', N'PTN062', N'NV003', N'KH014', N'MRHGM6623FA310062', DATEADD(minute,510,DATEADD(day,-176,@NgayMau))),
    (N'PKT063', N'PTN063', N'NV003', N'KH015', N'KMHCT41B0PA310063', DATEADD(minute,510,DATEADD(day,-189,@NgayMau))),
    (N'PKT064', N'PTN064', N'NV003', N'KH016', N'KNDEP81A8MA310064', DATEADD(minute,510,DATEADD(day,-202,@NgayMau))),
    (N'PKT065', N'PTN065', N'NV003', N'KH017', N'JM3KF2W79NA310065', DATEADD(minute,510,DATEADD(day,-215,@NgayMau))),
    (N'PKT066', N'PTN066', N'NV003', N'KH018', N'MNCUMFF57LA310066', DATEADD(minute,510,DATEADD(day,-228,@NgayMau))),
    (N'PKT067', N'PTN067', N'NV003', N'KH019', N'MK2NC1W44KA310067', DATEADD(minute,510,DATEADD(day,-241,@NgayMau))),
    (N'PKT068', N'PTN068', N'NV003', N'KH020', N'MHYANC227PA310068', DATEADD(minute,510,DATEADD(day,-254,@NgayMau))),
    (N'PKT069', N'PTN069', N'NV003', N'KH021', N'MNTCB4D27GA310069', DATEADD(minute,510,DATEADD(day,-267,@NgayMau))),
    (N'PKT070', N'PTN070', N'NV003', N'KH022', N'WDDWF4JBXGA310070', DATEADD(minute,510,DATEADD(day,-280,@NgayMau))),
    (N'PKT071', N'PTN071', N'NV003', N'KH023', N'WBA5R1101KA310071', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT072', N'PTN072', N'NV003', N'KH024', N'RLLV1AFA1SA310072', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT073', N'PTN073', N'NV003', N'KH001', N'MR0BT9F31JA310001', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT074', N'PTN074', N'NV003', N'KH002', N'MRHGM6627FA310002', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT075', N'PTN075', N'NV003', N'KH003', N'KMHCT41B5JA310003', DATEADD(minute,510,DATEADD(day,-2,@NgayMau))),
    (N'PKT076', N'PTN076', N'NV003', N'KH004', N'KNDEP81A3LA310004', DATEADD(minute,510,DATEADD(day,-358,@NgayMau))),
    (N'PKT077', N'PTN077', N'NV003', N'KH005', N'JM3KF2W77HA310005', DATEADD(minute,510,DATEADD(day,-371,@NgayMau))),
    (N'PKT078', N'PTN078', N'NV003', N'KH006', N'MNCUMFF55FA310006', DATEADD(minute,510,DATEADD(day,-384,@NgayMau))),
    (N'PKT079', N'PTN079', N'NV003', N'KH007', N'MK2NC1W48KA310007', DATEADD(minute,510,DATEADD(day,-397,@NgayMau))),
    (N'PKT080', N'PTN080', N'NV003', N'KH008', N'MHYANC226MA310008', DATEADD(minute,510,DATEADD(day,-410,@NgayMau))),
    (N'PKT081', N'PTN081', N'NV003', N'KH009', N'MNTCB4D20GA310009', DATEADD(minute,510,DATEADD(day,-423,@NgayMau))),
    (N'PKT082', N'PTN082', N'NV003', N'KH010', N'WDDWF4JB5FA310010', DATEADD(minute,510,DATEADD(day,-436,@NgayMau))),
    (N'PKT083', N'PTN083', N'NV003', N'KH011', N'WBA5R1105KA310011', DATEADD(minute,510,DATEADD(day,-449,@NgayMau))),
    (N'PKT084', N'PTN084', N'NV003', N'KH012', N'RLLV1AFA6PA310012', DATEADD(minute,510,DATEADD(day,-462,@NgayMau))),
    (N'PKT085', N'PTN085', N'NV003', N'KH013', N'MR0BT9F36KA310013', DATEADD(minute,510,DATEADD(day,-475,@NgayMau))),
    (N'PKT086', N'PTN086', N'NV003', N'KH014', N'MRHGM6621GA310014', DATEADD(minute,510,DATEADD(day,-488,@NgayMau))),
    (N'PKT087', N'PTN087', N'NV003', N'KH015', N'KMHCT41BXKA310015', DATEADD(minute,510,DATEADD(day,-501,@NgayMau))),
    (N'PKT088', N'PTN088', N'NV003', N'KH016', N'KNDEP81A8MA310016', DATEADD(minute,510,DATEADD(day,-514,@NgayMau))),
    (N'PKT089', N'PTN089', N'NV003', N'KH017', N'JM3KF2W76JA310017', DATEADD(minute,510,DATEADD(day,-527,@NgayMau))),
    (N'PKT090', N'PTN090', N'NV003', N'KH018', N'MNCUMFF5XGA310018', DATEADD(minute,510,DATEADD(day,-540,@NgayMau)));

-- ChiTietPhieuKiemTra: 180 bản ghi
INSERT INTO dbo.[ChiTietPhieuKiemTra] ([maPKT], [maDichVu], [hangMucKiemTra], [ketQuaChuanDoan], [ketQuaKiemTra])
VALUES
    (N'PKT001', N'DV001', N'Thay dầu động cơ', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT001', N'DV002', N'Thay lọc dầu', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT002', N'DV002', N'Thay lọc dầu', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT002', N'DV003', N'Kiểm tra phanh', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT003', N'DV003', N'Kiểm tra phanh', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT003', N'DV004', N'Thay má phanh', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT004', N'DV004', N'Thay má phanh', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT004', N'DV005', N'Thay lọc gió điều hòa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT005', N'DV005', N'Thay lọc gió điều hòa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT005', N'DV006', N'Vệ sinh điều hòa', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT006', N'DV006', N'Vệ sinh điều hòa', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT006', N'DV007', N'Thay ắc quy', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT007', N'DV007', N'Thay ắc quy', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT007', N'DV008', N'Thay lốp xe', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT008', N'DV008', N'Thay lốp xe', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT008', N'DV009', N'Cân bằng động bánh xe', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT009', N'DV009', N'Cân bằng động bánh xe', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT009', N'DV010', N'Thay bugi', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT010', N'DV010', N'Thay bugi', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT010', N'DV011', N'Thay dây curoa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT011', N'DV011', N'Thay dây curoa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT011', N'DV012', N'Thay nước làm mát', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT012', N'DV012', N'Thay nước làm mát', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT012', N'DV013', N'Thay giảm xóc', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT013', N'DV013', N'Thay giảm xóc', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT013', N'DV014', N'Sửa hệ thống điện', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT014', N'DV014', N'Sửa hệ thống điện', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT014', N'DV015', N'Đại tu động cơ', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT015', N'DV015', N'Đại tu động cơ', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT015', N'DV016', N'Sửa hộp số', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT016', N'DV016', N'Sửa hộp số', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT016', N'DV017', N'Bảo dưỡng miễn phí', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT017', N'DV017', N'Bảo dưỡng miễn phí', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT017', N'DV001', N'Thay dầu động cơ', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT018', N'DV001', N'Thay dầu động cơ', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT018', N'DV002', N'Thay lọc dầu', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT019', N'DV002', N'Thay lọc dầu', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT019', N'DV003', N'Kiểm tra phanh', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT020', N'DV003', N'Kiểm tra phanh', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT020', N'DV004', N'Thay má phanh', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT021', N'DV004', N'Thay má phanh', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT021', N'DV005', N'Thay lọc gió điều hòa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT022', N'DV005', N'Thay lọc gió điều hòa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT022', N'DV006', N'Vệ sinh điều hòa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT023', N'DV006', N'Vệ sinh điều hòa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT023', N'DV007', N'Thay ắc quy', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT024', N'DV007', N'Thay ắc quy', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT024', N'DV008', N'Thay lốp xe', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT025', N'DV008', N'Thay lốp xe', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT025', N'DV009', N'Cân bằng động bánh xe', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT026', N'DV009', N'Cân bằng động bánh xe', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT026', N'DV010', N'Thay bugi', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT027', N'DV010', N'Thay bugi', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT027', N'DV011', N'Thay dây curoa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT028', N'DV011', N'Thay dây curoa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT028', N'DV012', N'Thay nước làm mát', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT029', N'DV012', N'Thay nước làm mát', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT029', N'DV013', N'Thay giảm xóc', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT030', N'DV013', N'Thay giảm xóc', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT030', N'DV014', N'Sửa hệ thống điện', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT031', N'DV014', N'Sửa hệ thống điện', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT031', N'DV015', N'Đại tu động cơ', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT032', N'DV015', N'Đại tu động cơ', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT032', N'DV016', N'Sửa hộp số', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT033', N'DV016', N'Sửa hộp số', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT033', N'DV017', N'Bảo dưỡng miễn phí', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT034', N'DV017', N'Bảo dưỡng miễn phí', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT034', N'DV001', N'Thay dầu động cơ', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT035', N'DV001', N'Thay dầu động cơ', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT035', N'DV002', N'Thay lọc dầu', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT036', N'DV002', N'Thay lọc dầu', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT036', N'DV003', N'Kiểm tra phanh', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT037', N'DV003', N'Kiểm tra phanh', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT037', N'DV004', N'Thay má phanh', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT038', N'DV004', N'Thay má phanh', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT038', N'DV005', N'Thay lọc gió điều hòa', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT039', N'DV005', N'Thay lọc gió điều hòa', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT039', N'DV006', N'Vệ sinh điều hòa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT040', N'DV006', N'Vệ sinh điều hòa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT040', N'DV007', N'Thay ắc quy', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT041', N'DV007', N'Thay ắc quy', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT041', N'DV008', N'Thay lốp xe', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT042', N'DV008', N'Thay lốp xe', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT042', N'DV009', N'Cân bằng động bánh xe', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT043', N'DV009', N'Cân bằng động bánh xe', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT043', N'DV010', N'Thay bugi', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT044', N'DV010', N'Thay bugi', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT044', N'DV011', N'Thay dây curoa', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT045', N'DV011', N'Thay dây curoa', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT045', N'DV012', N'Thay nước làm mát', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT046', N'DV012', N'Thay nước làm mát', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT046', N'DV013', N'Thay giảm xóc', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT047', N'DV013', N'Thay giảm xóc', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT047', N'DV014', N'Sửa hệ thống điện', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT048', N'DV014', N'Sửa hệ thống điện', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT048', N'DV015', N'Đại tu động cơ', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT049', N'DV015', N'Đại tu động cơ', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT049', N'DV016', N'Sửa hộp số', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT050', N'DV016', N'Sửa hộp số', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT050', N'DV017', N'Bảo dưỡng miễn phí', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT051', N'DV017', N'Bảo dưỡng miễn phí', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT051', N'DV018', N'Kiểm tra sau bảo hành', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT052', N'DV017', N'Bảo dưỡng miễn phí', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT052', N'DV018', N'Kiểm tra sau bảo hành', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT053', N'DV002', N'Thay lọc dầu', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT053', N'DV003', N'Kiểm tra phanh', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT054', N'DV003', N'Kiểm tra phanh', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT054', N'DV004', N'Thay má phanh', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT055', N'DV004', N'Thay má phanh', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT055', N'DV005', N'Thay lọc gió điều hòa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT056', N'DV005', N'Thay lọc gió điều hòa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT056', N'DV006', N'Vệ sinh điều hòa', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT057', N'DV006', N'Vệ sinh điều hòa', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT057', N'DV007', N'Thay ắc quy', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT058', N'DV007', N'Thay ắc quy', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT058', N'DV008', N'Thay lốp xe', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT059', N'DV008', N'Thay lốp xe', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT059', N'DV009', N'Cân bằng động bánh xe', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT060', N'DV009', N'Cân bằng động bánh xe', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT060', N'DV010', N'Thay bugi', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT061', N'DV010', N'Thay bugi', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT061', N'DV011', N'Thay dây curoa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT062', N'DV011', N'Thay dây curoa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT062', N'DV012', N'Thay nước làm mát', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT063', N'DV012', N'Thay nước làm mát', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT063', N'DV013', N'Thay giảm xóc', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT064', N'DV013', N'Thay giảm xóc', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT064', N'DV014', N'Sửa hệ thống điện', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT065', N'DV014', N'Sửa hệ thống điện', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT065', N'DV015', N'Đại tu động cơ', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT066', N'DV015', N'Đại tu động cơ', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT066', N'DV016', N'Sửa hộp số', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT067', N'DV016', N'Sửa hộp số', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT067', N'DV017', N'Bảo dưỡng miễn phí', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT068', N'DV017', N'Bảo dưỡng miễn phí', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT068', N'DV001', N'Thay dầu động cơ', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT069', N'DV001', N'Thay dầu động cơ', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT069', N'DV002', N'Thay lọc dầu', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT070', N'DV002', N'Thay lọc dầu', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT070', N'DV003', N'Kiểm tra phanh', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT071', N'DV003', N'Kiểm tra phanh', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT071', N'DV004', N'Thay má phanh', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT072', N'DV004', N'Thay má phanh', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT072', N'DV005', N'Thay lọc gió điều hòa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT073', N'DV005', N'Thay lọc gió điều hòa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT073', N'DV006', N'Vệ sinh điều hòa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT074', N'DV006', N'Vệ sinh điều hòa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT074', N'DV007', N'Thay ắc quy', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT075', N'DV007', N'Thay ắc quy', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT075', N'DV008', N'Thay lốp xe', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT076', N'DV008', N'Thay lốp xe', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT076', N'DV009', N'Cân bằng động bánh xe', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT077', N'DV009', N'Cân bằng động bánh xe', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT077', N'DV010', N'Thay bugi', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT078', N'DV010', N'Thay bugi', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT078', N'DV011', N'Thay dây curoa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT079', N'DV011', N'Thay dây curoa', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT079', N'DV012', N'Thay nước làm mát', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT080', N'DV012', N'Thay nước làm mát', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT080', N'DV013', N'Thay giảm xóc', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT081', N'DV013', N'Thay giảm xóc', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT081', N'DV014', N'Sửa hệ thống điện', N'Chẩn đoán sơ bộ đã ghi nhận; chờ hoàn tất phép kiểm tra.', NULL),
    (N'PKT082', N'DV014', N'Sửa hệ thống điện', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT082', N'DV015', N'Đại tu động cơ', N'Chẩn đoán sơ bộ đã ghi nhận; chờ hoàn tất phép kiểm tra.', NULL),
    (N'PKT083', N'DV015', N'Đại tu động cơ', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT083', N'DV016', N'Sửa hộp số', N'Chẩn đoán sơ bộ đã ghi nhận; chờ hoàn tất phép kiểm tra.', NULL),
    (N'PKT084', N'DV016', N'Sửa hộp số', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT084', N'DV017', N'Bảo dưỡng miễn phí', N'Chẩn đoán sơ bộ đã ghi nhận; chờ hoàn tất phép kiểm tra.', NULL),
    (N'PKT085', N'DV017', N'Bảo dưỡng miễn phí', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT085', N'DV001', N'Thay dầu động cơ', N'Chẩn đoán sơ bộ đã ghi nhận; chờ hoàn tất phép kiểm tra.', NULL),
    (N'PKT086', N'DV001', N'Thay dầu động cơ', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT086', N'DV002', N'Thay lọc dầu', N'Chẩn đoán sơ bộ đã ghi nhận; chờ hoàn tất phép kiểm tra.', NULL),
    (N'PKT087', N'DV002', N'Thay lọc dầu', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT087', N'DV003', N'Kiểm tra phanh', N'Chẩn đoán sơ bộ đã ghi nhận; chờ hoàn tất phép kiểm tra.', NULL),
    (N'PKT088', N'DV003', N'Kiểm tra phanh', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT088', N'DV004', N'Thay má phanh', N'Chẩn đoán sơ bộ đã ghi nhận; chờ hoàn tất phép kiểm tra.', NULL),
    (N'PKT089', N'DV004', N'Thay má phanh', N'Ghi nhận dấu hiệu hao mòn, cần xử lý theo hạng mục.', N'Không đạt'),
    (N'PKT089', N'DV005', N'Thay lọc gió điều hòa', N'Chẩn đoán sơ bộ đã ghi nhận; chờ hoàn tất phép kiểm tra.', NULL),
    (N'PKT090', N'DV005', N'Thay lọc gió điều hòa', N'Chưa phát hiện bất thường ở bộ phận đã kiểm tra.', N'Đạt'),
    (N'PKT090', N'DV006', N'Vệ sinh điều hòa', N'Chẩn đoán sơ bộ đã ghi nhận; chờ hoàn tất phép kiểm tra.', NULL);

-- PhieuBaoGia: 80 bản ghi
INSERT INTO dbo.[PhieuBaoGia] ([maBaoGia], [maPKT], [maKhachHang], [maNhanVien], [VIN], [ngayLap], [ngayHetHan], [thoiGianXacNhan], [trangThai])
SELECT v.[maBaoGia], v.[maPKT], kt.[maKhachHang], v.[maNhanVien], v.[VIN], v.[ngayLap], v.[ngayHetHan], v.[thoiGianXacNhan], v.[trangThai]
FROM (VALUES
    (N'PBG001', N'PKT001', N'NV001', N'MR0BT9F31JA310001', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG002', N'PKT002', N'NV001', N'MRHGM6627FA310002', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG003', N'PKT003', N'NV001', N'KMHCT41B5JA310003', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG004', N'PKT004', N'NV001', N'KNDEP81A3LA310004', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG005', N'PKT005', N'NV001', N'JM3KF2W77HA310005', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG006', N'PKT006', N'NV001', N'MNCUMFF55FA310006', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG007', N'PKT007', N'NV001', N'MK2NC1W48KA310007', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG008', N'PKT008', N'NV001', N'MHYANC226MA310008', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG009', N'PKT009', N'NV001', N'MNTCB4D20GA310009', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG010', N'PKT010', N'NV001', N'WDDWF4JB5FA310010', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG011', N'PKT011', N'NV001', N'WBA5R1105KA310011', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG012', N'PKT012', N'NV001', N'RLLV1AFA6PA310012', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG013', N'PKT013', N'NV001', N'MR0BT9F36KA310013', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG014', N'PKT014', N'NV001', N'MRHGM6621GA310014', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG015', N'PKT015', N'NV001', N'KMHCT41BXKA310015', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG016', N'PKT016', N'NV001', N'KNDEP81A8MA310016', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG017', N'PKT017', N'NV001', N'JM3KF2W76JA310017', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG018', N'PKT018', N'NV001', N'MNCUMFF5XGA310018', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG019', N'PKT019', N'NV001', N'MK2NC1W42LA310019', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG020', N'PKT020', N'NV001', N'MHYANC225NA310020', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), DATEADD(minute,555,DATEADD(day,-2,@NgayMau)), N'Đã xác nhận'),
    (N'PBG021', N'PKT021', N'NV001', N'MNTCB4D2XHA310021', DATEADD(minute,540,DATEADD(day,-293,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-289,@NgayMau)), DATEADD(minute,555,DATEADD(day,-293,@NgayMau)), N'Đã xác nhận'),
    (N'PBG022', N'PKT022', N'NV001', N'WDDWF4JBXGA310022', DATEADD(minute,540,DATEADD(day,-306,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-302,@NgayMau)), DATEADD(minute,555,DATEADD(day,-306,@NgayMau)), N'Đã xác nhận'),
    (N'PBG023', N'PKT023', N'NV001', N'WBA5R110XLA310023', DATEADD(minute,540,DATEADD(day,-319,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-315,@NgayMau)), DATEADD(minute,555,DATEADD(day,-319,@NgayMau)), N'Đã xác nhận'),
    (N'PBG024', N'PKT024', N'NV001', N'RLLV1AFA9RA310024', DATEADD(minute,540,DATEADD(day,-332,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-328,@NgayMau)), DATEADD(minute,555,DATEADD(day,-332,@NgayMau)), N'Đã xác nhận'),
    (N'PBG025', N'PKT025', N'NV001', N'MR0BT9F30LA310025', DATEADD(minute,540,DATEADD(day,-345,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-341,@NgayMau)), DATEADD(minute,555,DATEADD(day,-345,@NgayMau)), N'Đã xác nhận'),
    (N'PBG026', N'PKT026', N'NV001', N'MRHGM6626HA310026', DATEADD(minute,540,DATEADD(day,-358,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-354,@NgayMau)), DATEADD(minute,555,DATEADD(day,-358,@NgayMau)), N'Đã xác nhận'),
    (N'PBG027', N'PKT027', N'NV001', N'KMHCT41B4LA310027', DATEADD(minute,540,DATEADD(day,-371,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-367,@NgayMau)), DATEADD(minute,555,DATEADD(day,-371,@NgayMau)), N'Đã xác nhận'),
    (N'PBG028', N'PKT028', N'NV001', N'KNDEP81A2NA310028', DATEADD(minute,540,DATEADD(day,-384,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-380,@NgayMau)), DATEADD(minute,555,DATEADD(day,-384,@NgayMau)), N'Đã xác nhận'),
    (N'PBG029', N'PKT029', N'NV001', N'JM3KF2W70KA310029', DATEADD(minute,540,DATEADD(day,-397,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-393,@NgayMau)), DATEADD(minute,555,DATEADD(day,-397,@NgayMau)), N'Đã xác nhận'),
    (N'PBG030', N'PKT030', N'NV001', N'MNCUMFF59HA310030', DATEADD(minute,540,DATEADD(day,-410,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-406,@NgayMau)), DATEADD(minute,555,DATEADD(day,-410,@NgayMau)), N'Đã xác nhận'),
    (N'PBG031', N'PKT031', N'NV001', N'MK2NC1W41MA310031', DATEADD(minute,540,DATEADD(day,-423,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-419,@NgayMau)), DATEADD(minute,555,DATEADD(day,-423,@NgayMau)), N'Đã xác nhận'),
    (N'PBG032', N'PKT032', N'NV001', N'MHYANC228PA310032', DATEADD(minute,540,DATEADD(day,-436,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-432,@NgayMau)), DATEADD(minute,555,DATEADD(day,-436,@NgayMau)), N'Đã xác nhận'),
    (N'PBG033', N'PKT033', N'NV001', N'MNTCB4D29JA310033', DATEADD(minute,540,DATEADD(day,-449,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-445,@NgayMau)), DATEADD(minute,555,DATEADD(day,-449,@NgayMau)), N'Đã xác nhận'),
    (N'PBG034', N'PKT034', N'NV001', N'WDDWF4JB4HA310034', DATEADD(minute,540,DATEADD(day,-462,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-458,@NgayMau)), DATEADD(minute,555,DATEADD(day,-462,@NgayMau)), N'Đã xác nhận'),
    (N'PBG035', N'PKT035', N'NV001', N'WBA5R1104MA310035', DATEADD(minute,540,DATEADD(day,-475,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-471,@NgayMau)), DATEADD(minute,555,DATEADD(day,-475,@NgayMau)), N'Đã xác nhận'),
    (N'PBG036', N'PKT036', N'NV001', N'RLLV1AFA8SA310036', DATEADD(minute,540,DATEADD(day,-488,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-484,@NgayMau)), DATEADD(minute,555,DATEADD(day,-488,@NgayMau)), N'Đã xác nhận'),
    (N'PBG037', N'PKT037', N'NV001', N'MR0BT9F35MA310037', DATEADD(minute,540,DATEADD(day,-501,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-497,@NgayMau)), DATEADD(minute,555,DATEADD(day,-501,@NgayMau)), N'Đã xác nhận'),
    (N'PBG038', N'PKT038', N'NV001', N'MRHGM6625JA310038', DATEADD(minute,540,DATEADD(day,-514,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-510,@NgayMau)), DATEADD(minute,555,DATEADD(day,-514,@NgayMau)), N'Đã xác nhận'),
    (N'PBG039', N'PKT039', N'NV001', N'KMHCT41B9MA310039', DATEADD(minute,540,DATEADD(day,-527,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-523,@NgayMau)), DATEADD(minute,555,DATEADD(day,-527,@NgayMau)), N'Đã xác nhận'),
    (N'PBG040', N'PKT040', N'NV001', N'KNDEP81AXPA310040', DATEADD(minute,540,DATEADD(day,-540,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-536,@NgayMau)), DATEADD(minute,555,DATEADD(day,-540,@NgayMau)), N'Đã xác nhận'),
    (N'PBG041', N'PKT041', N'NV001', N'JM3KF2W7XLA310041', DATEADD(minute,540,DATEADD(day,-553,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-549,@NgayMau)), DATEADD(minute,555,DATEADD(day,-553,@NgayMau)), N'Đã xác nhận'),
    (N'PBG042', N'PKT042', N'NV001', N'MNCUMFF58JA310042', DATEADD(minute,540,DATEADD(day,-566,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-562,@NgayMau)), DATEADD(minute,555,DATEADD(day,-566,@NgayMau)), N'Đã xác nhận'),
    (N'PBG043', N'PKT043', N'NV001', N'MK2NC1W46NA310043', DATEADD(minute,540,DATEADD(day,-579,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-575,@NgayMau)), DATEADD(minute,555,DATEADD(day,-579,@NgayMau)), N'Đã xác nhận'),
    (N'PBG044', N'PKT044', N'NV001', N'MHYANC22XMA310044', DATEADD(minute,540,DATEADD(day,-592,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-588,@NgayMau)), DATEADD(minute,555,DATEADD(day,-592,@NgayMau)), N'Đã xác nhận'),
    (N'PBG045', N'PKT045', N'NV001', N'MNTCB4D23KA310045', DATEADD(minute,540,DATEADD(day,-605,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-601,@NgayMau)), DATEADD(minute,555,DATEADD(day,-605,@NgayMau)), N'Đã xác nhận'),
    (N'PBG046', N'PKT046', N'NV001', N'WDDWF4JB3JA310046', DATEADD(minute,540,DATEADD(day,-618,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-614,@NgayMau)), DATEADD(minute,555,DATEADD(day,-618,@NgayMau)), N'Đã xác nhận'),
    (N'PBG047', N'PKT047', N'NV001', N'WBA5R1109NA310047', DATEADD(minute,540,DATEADD(day,-631,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-627,@NgayMau)), DATEADD(minute,555,DATEADD(day,-631,@NgayMau)), N'Đã xác nhận'),
    (N'PBG048', N'PKT048', N'NV001', N'RLLV1AFA5PA310048', DATEADD(minute,540,DATEADD(day,-644,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-640,@NgayMau)), DATEADD(minute,555,DATEADD(day,-644,@NgayMau)), N'Đã xác nhận'),
    (N'PBG049', N'PKT049', N'NV001', N'MR0BT9F3XNA310049', DATEADD(minute,540,DATEADD(day,-657,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-653,@NgayMau)), DATEADD(minute,555,DATEADD(day,-657,@NgayMau)), N'Đã xác nhận'),
    (N'PBG050', N'PKT050', N'NV001', N'MRHGM6624KA310050', DATEADD(minute,540,DATEADD(day,-20,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-16,@NgayMau)), DATEADD(minute,555,DATEADD(day,-20,@NgayMau)), N'Đã xác nhận'),
    (N'PBG051', N'PKT051', N'NV001', N'KMHCT41B8NA310051', DATEADD(minute,540,DATEADD(day,-33,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-29,@NgayMau)), DATEADD(minute,555,DATEADD(day,-33,@NgayMau)), N'Đã xác nhận'),
    (N'PBG052', N'PKT052', N'NV001', N'KNDEP81A3LA310052', DATEADD(minute,540,DATEADD(day,-46,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-42,@NgayMau)), DATEADD(minute,555,DATEADD(day,-46,@NgayMau)), N'Đã xác nhận'),
    (N'PBG053', N'PKT053', N'NV001', N'JM3KF2W74MA310053', DATEADD(minute,540,DATEADD(day,-59,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-55,@NgayMau)), DATEADD(minute,555,DATEADD(day,-59,@NgayMau)), N'Đã xác nhận'),
    (N'PBG054', N'PKT054', N'NV001', N'MNCUMFF52KA310054', DATEADD(minute,540,DATEADD(day,-72,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-68,@NgayMau)), DATEADD(minute,555,DATEADD(day,-72,@NgayMau)), N'Đã xác nhận'),
    (N'PBG055', N'PKT055', N'NV001', N'MK2NC1W49PA310055', DATEADD(minute,540,DATEADD(day,-85,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-81,@NgayMau)), DATEADD(minute,555,DATEADD(day,-85,@NgayMau)), N'Đã xác nhận'),
    (N'PBG056', N'PKT056', N'NV001', N'MHYANC224NA310056', DATEADD(minute,540,DATEADD(day,-98,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-94,@NgayMau)), DATEADD(minute,555,DATEADD(day,-98,@NgayMau)), N'Đã xác nhận'),
    (N'PBG057', N'PKT057', N'NV001', N'MNTCB4D28LA310057', DATEADD(minute,540,DATEADD(day,-111,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-107,@NgayMau)), DATEADD(minute,555,DATEADD(day,-111,@NgayMau)), N'Đã xác nhận'),
    (N'PBG058', N'PKT058', N'NV001', N'WDDWF4JB0FA310058', DATEADD(minute,540,DATEADD(day,-124,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-120,@NgayMau)), DATEADD(minute,555,DATEADD(day,-124,@NgayMau)), N'Đã xác nhận'),
    (N'PBG059', N'PKT059', N'NV001', N'WBA5R1101PA310059', DATEADD(minute,540,DATEADD(day,-137,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-133,@NgayMau)), DATEADD(minute,555,DATEADD(day,-137,@NgayMau)), N'Đã xác nhận'),
    (N'PBG060', N'PKT060', N'NV001', N'RLLV1AFA2RA310060', DATEADD(minute,540,DATEADD(day,-150,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-146,@NgayMau)), DATEADD(minute,555,DATEADD(day,-150,@NgayMau)), N'Đã xác nhận'),
    (N'PBG061', N'PKT061', N'NV001', N'MR0BT9F37PA310061', DATEADD(minute,540,DATEADD(day,-163,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-159,@NgayMau)), DATEADD(minute,555,DATEADD(day,-163,@NgayMau)), N'Đã xác nhận'),
    (N'PBG062', N'PKT062', N'NV001', N'MRHGM6623FA310062', DATEADD(minute,540,DATEADD(day,-176,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-172,@NgayMau)), DATEADD(minute,555,DATEADD(day,-176,@NgayMau)), N'Đã xác nhận'),
    (N'PBG063', N'PKT063', N'NV001', N'KMHCT41B0PA310063', DATEADD(minute,540,DATEADD(day,-189,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-185,@NgayMau)), DATEADD(minute,555,DATEADD(day,-189,@NgayMau)), N'Đã xác nhận'),
    (N'PBG064', N'PKT064', N'NV001', N'KNDEP81A8MA310064', DATEADD(minute,540,DATEADD(day,-202,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-198,@NgayMau)), DATEADD(minute,555,DATEADD(day,-202,@NgayMau)), N'Đã xác nhận'),
    (N'PBG065', N'PKT065', N'NV001', N'JM3KF2W79NA310065', DATEADD(minute,540,DATEADD(day,-215,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-211,@NgayMau)), DATEADD(minute,555,DATEADD(day,-215,@NgayMau)), N'Đã xác nhận'),
    (N'PBG066', N'PKT066', N'NV001', N'MNCUMFF57LA310066', DATEADD(minute,540,DATEADD(day,-228,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-224,@NgayMau)), DATEADD(minute,555,DATEADD(day,-228,@NgayMau)), N'Đã xác nhận'),
    (N'PBG067', N'PKT067', N'NV001', N'MK2NC1W44KA310067', DATEADD(minute,540,DATEADD(day,-241,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-237,@NgayMau)), DATEADD(minute,555,DATEADD(day,-241,@NgayMau)), N'Đã xác nhận'),
    (N'PBG068', N'PKT068', N'NV001', N'MHYANC227PA310068', DATEADD(minute,540,DATEADD(day,-254,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-250,@NgayMau)), DATEADD(minute,555,DATEADD(day,-254,@NgayMau)), N'Đã xác nhận'),
    (N'PBG069', N'PKT069', N'NV001', N'MNTCB4D27GA310069', DATEADD(minute,540,DATEADD(day,-267,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-263,@NgayMau)), DATEADD(minute,555,DATEADD(day,-267,@NgayMau)), N'Đã xác nhận'),
    (N'PBG070', N'PKT070', N'NV001', N'WDDWF4JBXGA310070', DATEADD(minute,540,DATEADD(day,-280,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-276,@NgayMau)), DATEADD(minute,555,DATEADD(day,-280,@NgayMau)), N'Đã xác nhận'),
    (N'PBG071', N'PKT071', N'NV001', N'WBA5R1101KA310071', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), NULL, N'Còn hiệu lực'),
    (N'PBG072', N'PKT072', N'NV001', N'RLLV1AFA1SA310072', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), NULL, N'Còn hiệu lực'),
    (N'PBG073', N'PKT073', N'NV001', N'MR0BT9F31JA310001', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), NULL, N'Còn hiệu lực'),
    (N'PBG074', N'PKT074', N'NV001', N'MRHGM6627FA310002', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), NULL, N'Còn hiệu lực'),
    (N'PBG075', N'PKT075', N'NV001', N'KMHCT41B5JA310003', DATEADD(minute,540,DATEADD(day,-2,@NgayMau)), DATEADD(minute,1439,DATEADD(day,2,@NgayMau)), NULL, N'Còn hiệu lực'),
    (N'PBG076', N'PKT076', N'NV001', N'KNDEP81A3LA310004', DATEADD(minute,540,DATEADD(day,-358,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-354,@NgayMau)), NULL, N'Hết hiệu lực'),
    (N'PBG077', N'PKT077', N'NV001', N'JM3KF2W77HA310005', DATEADD(minute,540,DATEADD(day,-371,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-367,@NgayMau)), NULL, N'Hết hiệu lực'),
    (N'PBG078', N'PKT078', N'NV001', N'MNCUMFF55FA310006', DATEADD(minute,540,DATEADD(day,-384,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-380,@NgayMau)), NULL, N'Hết hiệu lực'),
    (N'PBG079', N'PKT079', N'NV001', N'MK2NC1W48KA310007', DATEADD(minute,540,DATEADD(day,-397,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-393,@NgayMau)), NULL, N'Hết hiệu lực'),
    (N'PBG080', N'PKT080', N'NV001', N'MHYANC226MA310008', DATEADD(minute,540,DATEADD(day,-410,@NgayMau)), DATEADD(minute,1439,DATEADD(day,-406,@NgayMau)), NULL, N'Hết hiệu lực')
) AS v([maBaoGia], [maPKT], [maNhanVien], [VIN], [ngayLap], [ngayHetHan], [thoiGianXacNhan], [trangThai])
JOIN dbo.[PhieuKiemTra] kt ON kt.[maPKT]=v.[maPKT] AND kt.[VIN]=v.[VIN];

-- ChiTietPhieuBaoGia: 168 bản ghi
INSERT INTO dbo.[ChiTietPhieuBaoGia] ([maBaoGia], [maDichVu], [maPhuTung], [soLuongPhuTung], [donGiaPhuTung], [chiPhiNhanCong], [trangThai])
VALUES
    (N'PBG001', N'DV001', N'PT001', 2, 850000.00, 150000.00, N'Chờ sửa chữa'),
    (N'PBG001', N'DV002', N'PT008', 3, 250000.00, 100000.00, N'Chờ sửa chữa'),
    (N'PBG002', N'DV002', N'PT002', 3, 180000.00, 100000.00, N'Chờ sửa chữa'),
    (N'PBG002', N'DV003', N'PT009', 1, 2300000.00, 250000.00, N'Chờ sửa chữa'),
    (N'PBG003', N'DV003', N'PT003', 1, 950000.00, 250000.00, N'Chờ sửa chữa'),
    (N'PBG003', N'DV004', N'PT010', 2, 3500000.00, 300000.00, N'Chờ sửa chữa'),
    (N'PBG004', N'DV004', N'PT004', 2, 220000.00, 300000.00, N'Chờ sửa chữa'),
    (N'PBG004', N'DV005', N'PT011', 3, 1250000.00, 120000.00, N'Chờ sửa chữa'),
    (N'PBG005', N'DV005', N'PT005', 3, 1850000.00, 120000.00, N'Chờ sửa chữa'),
    (N'PBG005', N'DV006', N'PT012', 1, 2800000.00, 650000.00, N'Chờ sửa chữa'),
    (N'PBG006', N'DV006', N'PT006', 1, 150000.00, 650000.00, N'Chờ sửa chữa'),
    (N'PBG006', N'DV007', N'PT013', 2, 1800000.00, 200000.00, N'Chờ sửa chữa'),
    (N'PBG007', N'DV007', N'PT007', 2, 650000.00, 200000.00, N'Chờ sửa chữa'),
    (N'PBG007', N'DV008', N'PT014', 3, 320000.00, 180000.00, N'Chờ sửa chữa'),
    (N'PBG008', N'DV008', N'PT008', 3, 250000.00, 180000.00, N'Chờ sửa chữa'),
    (N'PBG008', N'DV009', N'PT015', 1, 480000.00, 250000.00, N'Chờ sửa chữa'),
    (N'PBG009', N'DV009', N'PT009', 1, 2300000.00, 250000.00, N'Chờ sửa chữa'),
    (N'PBG009', N'DV010', N'PT016', 2, 350000.00, 180000.00, N'Chờ sửa chữa'),
    (N'PBG009', N'DV009', N'PT038', 1, 25000.00, 0.00, N'Chờ sửa chữa'),
    (N'PBG010', N'DV010', N'PT010', 2, 3500000.00, 180000.00, N'Chờ sửa chữa'),
    (N'PBG010', N'DV011', N'PT017', 3, 1450000.00, 450000.00, N'Chờ sửa chữa'),
    (N'PBG011', N'DV011', N'PT011', 3, 1250000.00, 450000.00, N'Chờ sửa chữa'),
    (N'PBG011', N'DV012', N'PT018', 1, 950000.00, 150000.00, N'Chờ sửa chữa'),
    (N'PBG012', N'DV012', N'PT012', 1, 2800000.00, 150000.00, N'Chờ sửa chữa'),
    (N'PBG012', N'DV013', N'PT019', 2, 6500000.00, 800000.00, N'Chờ sửa chữa'),
    (N'PBG013', N'DV013', N'PT013', 2, 1800000.00, 800000.00, N'Chờ sửa chữa'),
    (N'PBG013', N'DV014', N'PT020', 3, 850000.00, 1500000.00, N'Chờ sửa chữa'),
    (N'PBG014', N'DV014', N'PT014', 3, 320000.00, 1500000.00, N'Chờ sửa chữa'),
    (N'PBG014', N'DV015', N'PT021', 1, 40000000.00, 12000000.00, N'Chờ sửa chữa'),
    (N'PBG015', N'DV015', N'PT015', 1, 480000.00, 12000000.00, N'Chờ sửa chữa'),
    (N'PBG015', N'DV016', N'PT022', 2, 35000000.00, 8500000.00, N'Chờ sửa chữa'),
    (N'PBG016', N'DV016', N'PT016', 2, 350000.00, 8500000.00, N'Chờ sửa chữa'),
    (N'PBG016', N'DV017', N'PT023', 3, 3200000.00, 0.00, N'Chờ sửa chữa'),
    (N'PBG017', N'DV017', N'PT000', 0, 0.00, 0.00, N'Chờ sửa chữa'),
    (N'PBG017', N'DV001', N'PT024', 1, 4800000.00, 150000.00, N'Chờ sửa chữa'),
    (N'PBG018', N'DV001', N'PT018', 1, 950000.00, 150000.00, N'Chờ sửa chữa'),
    (N'PBG018', N'DV002', N'PT025', 2, 3800000.00, 100000.00, N'Chờ sửa chữa'),
    (N'PBG018', N'DV001', N'PT038', 1, 25000.00, 0.00, N'Chờ sửa chữa'),
    (N'PBG019', N'DV002', N'PT019', 2, 6500000.00, 100000.00, N'Chờ sửa chữa'),
    (N'PBG019', N'DV003', N'PT026', 3, 8500000.00, 250000.00, N'Chờ sửa chữa'),
    (N'PBG020', N'DV003', N'PT020', 3, 850000.00, 250000.00, N'Chờ sửa chữa'),
    (N'PBG020', N'DV004', N'PT027', 1, 750000.00, 300000.00, N'Chờ sửa chữa'),
    (N'PBG021', N'DV004', N'PT021', 1, 40000000.00, 300000.00, N'Chờ sửa chữa'),
    (N'PBG021', N'DV005', N'PT028', 2, 950000.00, 120000.00, N'Chờ sửa chữa'),
    (N'PBG022', N'DV005', N'PT022', 2, 35000000.00, 120000.00, N'Chờ sửa chữa'),
    (N'PBG022', N'DV006', N'PT029', 3, 1850000.00, 650000.00, N'Chờ sửa chữa'),
    (N'PBG023', N'DV006', N'PT023', 3, 3200000.00, 650000.00, N'Chờ sửa chữa'),
    (N'PBG023', N'DV007', N'PT030', 1, 2600000.00, 200000.00, N'Chờ sửa chữa'),
    (N'PBG024', N'DV007', N'PT024', 1, 4800000.00, 200000.00, N'Chờ sửa chữa'),
    (N'PBG024', N'DV008', N'PT031', 2, 680000.00, 180000.00, N'Chờ sửa chữa'),
    (N'PBG025', N'DV008', N'PT025', 2, 3800000.00, 180000.00, N'Chờ sửa chữa'),
    (N'PBG025', N'DV009', N'PT032', 3, 1650000.00, 250000.00, N'Chờ sửa chữa'),
    (N'PBG026', N'DV009', N'PT026', 3, 8500000.00, 250000.00, N'Chờ sửa chữa'),
    (N'PBG026', N'DV010', N'PT033', 1, 3200000.00, 180000.00, N'Chờ sửa chữa'),
    (N'PBG027', N'DV010', N'PT027', 1, 750000.00, 180000.00, N'Chờ sửa chữa'),
    (N'PBG027', N'DV011', N'PT034', 2, 450000.00, 450000.00, N'Chờ sửa chữa'),
    (N'PBG027', N'DV010', N'PT038', 1, 25000.00, 0.00, N'Chờ sửa chữa'),
    (N'PBG028', N'DV011', N'PT028', 2, 950000.00, 450000.00, N'Chờ sửa chữa'),
    (N'PBG028', N'DV012', N'PT035', 3, 180000.00, 150000.00, N'Chờ sửa chữa'),
    (N'PBG029', N'DV012', N'PT029', 3, 1850000.00, 150000.00, N'Chờ sửa chữa'),
    (N'PBG029', N'DV013', N'PT000', 0, 0.00, 800000.00, N'Chờ sửa chữa'),
    (N'PBG030', N'DV013', N'PT030', 1, 2600000.00, 800000.00, N'Chờ sửa chữa'),
    (N'PBG030', N'DV014', N'PT037', 2, 15000.00, 1500000.00, N'Chờ sửa chữa'),
    (N'PBG031', N'DV014', N'PT031', 2, 680000.00, 1500000.00, N'Chờ sửa chữa'),
    (N'PBG031', N'DV015', N'PT038', 3, 25000.00, 12000000.00, N'Chờ sửa chữa'),
    (N'PBG032', N'DV015', N'PT032', 3, 1650000.00, 12000000.00, N'Chờ sửa chữa'),
    (N'PBG032', N'DV016', N'PT001', 1, 850000.00, 8500000.00, N'Chờ sửa chữa'),
    (N'PBG033', N'DV016', N'PT033', 1, 3200000.00, 8500000.00, N'Chờ sửa chữa'),
    (N'PBG033', N'DV017', N'PT002', 2, 180000.00, 0.00, N'Chờ sửa chữa'),
    (N'PBG034', N'DV017', N'PT034', 2, 450000.00, 0.00, N'Chờ sửa chữa'),
    (N'PBG034', N'DV001', N'PT003', 3, 950000.00, 150000.00, N'Chờ sửa chữa'),
    (N'PBG035', N'DV001', N'PT035', 3, 180000.00, 150000.00, N'Chờ sửa chữa'),
    (N'PBG035', N'DV002', N'PT004', 1, 220000.00, 100000.00, N'Chờ sửa chữa'),
    (N'PBG036', N'DV002', N'PT036', 1, 95000.00, 100000.00, N'Chờ sửa chữa'),
    (N'PBG036', N'DV003', N'PT005', 2, 1850000.00, 250000.00, N'Chờ sửa chữa'),
    (N'PBG036', N'DV002', N'PT038', 1, 25000.00, 0.00, N'Chờ sửa chữa'),
    (N'PBG037', N'DV003', N'PT037', 2, 15000.00, 250000.00, N'Chờ sửa chữa'),
    (N'PBG037', N'DV004', N'PT006', 3, 150000.00, 300000.00, N'Chờ sửa chữa'),
    (N'PBG038', N'DV004', N'PT038', 3, 25000.00, 300000.00, N'Chờ sửa chữa'),
    (N'PBG038', N'DV005', N'PT007', 1, 650000.00, 120000.00, N'Chờ sửa chữa'),
    (N'PBG039', N'DV005', N'PT001', 1, 850000.00, 120000.00, N'Chờ sửa chữa'),
    (N'PBG039', N'DV006', N'PT008', 2, 250000.00, 650000.00, N'Chờ sửa chữa'),
    (N'PBG040', N'DV006', N'PT002', 2, 180000.00, 650000.00, N'Chờ sửa chữa'),
    (N'PBG040', N'DV007', N'PT009', 3, 2300000.00, 200000.00, N'Chờ sửa chữa'),
    (N'PBG041', N'DV007', N'PT003', 3, 950000.00, 200000.00, N'Chờ sửa chữa'),
    (N'PBG041', N'DV008', N'PT010', 1, 3500000.00, 180000.00, N'Chờ sửa chữa'),
    (N'PBG042', N'DV008', N'PT004', 1, 220000.00, 180000.00, N'Chờ sửa chữa'),
    (N'PBG042', N'DV009', N'PT011', 2, 1250000.00, 250000.00, N'Chờ sửa chữa'),
    (N'PBG043', N'DV009', N'PT005', 2, 1850000.00, 250000.00, N'Chờ sửa chữa'),
    (N'PBG043', N'DV010', N'PT012', 3, 2800000.00, 180000.00, N'Chờ sửa chữa'),
    (N'PBG044', N'DV010', N'PT006', 3, 150000.00, 180000.00, N'Chờ sửa chữa'),
    (N'PBG044', N'DV011', N'PT013', 1, 1800000.00, 450000.00, N'Chờ sửa chữa'),
    (N'PBG045', N'DV011', N'PT007', 1, 650000.00, 450000.00, N'Chờ sửa chữa'),
    (N'PBG045', N'DV012', N'PT014', 2, 320000.00, 150000.00, N'Chờ sửa chữa'),
    (N'PBG045', N'DV011', N'PT038', 1, 25000.00, 0.00, N'Chờ sửa chữa'),
    (N'PBG046', N'DV012', N'PT008', 2, 250000.00, 150000.00, N'Chờ sửa chữa'),
    (N'PBG046', N'DV013', N'PT015', 3, 480000.00, 800000.00, N'Chờ sửa chữa'),
    (N'PBG047', N'DV013', N'PT009', 3, 2300000.00, 800000.00, N'Chờ sửa chữa'),
    (N'PBG047', N'DV014', N'PT016', 1, 350000.00, 1500000.00, N'Chờ sửa chữa'),
    (N'PBG048', N'DV014', N'PT010', 1, 3500000.00, 1500000.00, N'Chờ sửa chữa'),
    (N'PBG048', N'DV015', N'PT017', 2, 1450000.00, 12000000.00, N'Chờ sửa chữa'),
    (N'PBG049', N'DV015', N'PT011', 2, 1250000.00, 12000000.00, N'Chờ sửa chữa'),
    (N'PBG049', N'DV016', N'PT018', 3, 950000.00, 8500000.00, N'Chờ sửa chữa'),
    (N'PBG050', N'DV016', N'PT012', 3, 2800000.00, 8500000.00, N'Chờ sửa chữa'),
    (N'PBG050', N'DV017', N'PT019', 1, 6500000.00, 0.00, N'Chờ sửa chữa'),
    (N'PBG051', N'DV017', N'PT039', 1, 0.00, 25000.00, N'Chờ sửa chữa'),
    (N'PBG051', N'DV018', N'PT039', 1, 0.00, 25000.00, N'Chờ sửa chữa'),
    (N'PBG052', N'DV017', N'PT040', 1, 0.00, 0.00, N'Chờ sửa chữa'),
    (N'PBG052', N'DV018', N'PT040', 1, 0.00, 0.00, N'Chờ sửa chữa'),
    (N'PBG053', N'DV002', N'PT015', 3, 480000.00, 100000.00, N'Chờ sửa chữa'),
    (N'PBG053', N'DV003', N'PT022', 1, 35000000.00, 250000.00, N'Chờ sửa chữa'),
    (N'PBG054', N'DV003', N'PT016', 1, 350000.00, 250000.00, N'Chờ sửa chữa'),
    (N'PBG054', N'DV004', N'PT023', 2, 3200000.00, 300000.00, N'Chờ sửa chữa'),
    (N'PBG054', N'DV003', N'PT038', 1, 25000.00, 0.00, N'Chờ sửa chữa'),
    (N'PBG055', N'DV004', N'PT017', 2, 1450000.00, 300000.00, N'Chờ sửa chữa'),
    (N'PBG055', N'DV005', N'PT024', 3, 4800000.00, 120000.00, N'Chờ sửa chữa'),
    (N'PBG056', N'DV005', N'PT018', 3, 950000.00, 120000.00, N'Chờ sửa chữa'),
    (N'PBG056', N'DV006', N'PT025', 1, 3800000.00, 650000.00, N'Chờ sửa chữa'),
    (N'PBG057', N'DV006', N'PT019', 1, 6500000.00, 650000.00, N'Chờ sửa chữa'),
    (N'PBG057', N'DV007', N'PT026', 2, 8500000.00, 200000.00, N'Chờ sửa chữa'),
    (N'PBG058', N'DV007', N'PT020', 2, 850000.00, 200000.00, N'Chờ sửa chữa'),
    (N'PBG058', N'DV008', N'PT027', 3, 750000.00, 180000.00, N'Chờ sửa chữa'),
    (N'PBG059', N'DV008', N'PT021', 3, 40000000.00, 180000.00, N'Chờ sửa chữa'),
    (N'PBG059', N'DV009', N'PT028', 1, 950000.00, 250000.00, N'Chờ sửa chữa'),
    (N'PBG060', N'DV009', N'PT022', 1, 35000000.00, 250000.00, N'Chờ sửa chữa'),
    (N'PBG060', N'DV010', N'PT029', 2, 1850000.00, 180000.00, N'Chờ sửa chữa'),
    (N'PBG061', N'DV010', N'PT023', 2, 3200000.00, 180000.00, N'Chờ sửa chữa'),
    (N'PBG061', N'DV011', N'PT030', 3, 2600000.00, 450000.00, N'Chờ sửa chữa'),
    (N'PBG062', N'DV011', N'PT024', 3, 4800000.00, 450000.00, N'Chờ sửa chữa'),
    (N'PBG062', N'DV012', N'PT031', 1, 680000.00, 150000.00, N'Chờ sửa chữa'),
    (N'PBG063', N'DV012', N'PT025', 1, 3800000.00, 150000.00, N'Chờ sửa chữa'),
    (N'PBG063', N'DV013', N'PT032', 2, 1650000.00, 800000.00, N'Chờ sửa chữa'),
    (N'PBG063', N'DV012', N'PT038', 1, 25000.00, 0.00, N'Chờ sửa chữa'),
    (N'PBG064', N'DV013', N'PT026', 2, 8500000.00, 800000.00, N'Chờ sửa chữa'),
    (N'PBG064', N'DV014', N'PT033', 3, 3200000.00, 1500000.00, N'Chờ sửa chữa'),
    (N'PBG065', N'DV014', N'PT027', 3, 750000.00, 1500000.00, N'Chờ sửa chữa'),
    (N'PBG065', N'DV015', N'PT034', 1, 450000.00, 12000000.00, N'Chờ sửa chữa'),
    (N'PBG066', N'DV015', N'PT028', 1, 950000.00, 12000000.00, N'Chờ sửa chữa'),
    (N'PBG066', N'DV016', N'PT035', 2, 180000.00, 8500000.00, N'Chờ sửa chữa'),
    (N'PBG067', N'DV016', N'PT029', 2, 1850000.00, 8500000.00, N'Chờ sửa chữa'),
    (N'PBG067', N'DV017', N'PT036', 3, 95000.00, 0.00, N'Chờ sửa chữa'),
    (N'PBG068', N'DV017', N'PT030', 3, 2600000.00, 0.00, N'Chờ sửa chữa'),
    (N'PBG068', N'DV001', N'PT037', 1, 15000.00, 150000.00, N'Chờ sửa chữa'),
    (N'PBG069', N'DV001', N'PT031', 1, 680000.00, 150000.00, N'Chờ sửa chữa'),
    (N'PBG069', N'DV002', N'PT038', 2, 25000.00, 100000.00, N'Chờ sửa chữa'),
    (N'PBG070', N'DV002', N'PT032', 2, 1650000.00, 100000.00, N'Chờ sửa chữa'),
    (N'PBG070', N'DV003', N'PT001', 3, 850000.00, 250000.00, N'Chờ sửa chữa'),
    (N'PBG071', N'DV003', N'PT033', 3, 3200000.00, 250000.00, N'Chờ xác nhận'),
    (N'PBG071', N'DV004', N'PT002', 1, 180000.00, 300000.00, N'Chờ xác nhận'),
    (N'PBG072', N'DV004', N'PT034', 1, 450000.00, 300000.00, N'Chờ xác nhận'),
    (N'PBG072', N'DV005', N'PT003', 2, 950000.00, 120000.00, N'Chờ xác nhận'),
    (N'PBG072', N'DV004', N'PT038', 1, 25000.00, 0.00, N'Chờ xác nhận'),
    (N'PBG073', N'DV005', N'PT035', 2, 180000.00, 120000.00, N'Chờ xác nhận'),
    (N'PBG073', N'DV006', N'PT004', 3, 220000.00, 650000.00, N'Chờ xác nhận'),
    (N'PBG074', N'DV006', N'PT036', 3, 95000.00, 650000.00, N'Chờ xác nhận'),
    (N'PBG074', N'DV007', N'PT005', 1, 1850000.00, 200000.00, N'Chờ xác nhận'),
    (N'PBG075', N'DV003', N'PT000', 0, 0.00, 250000.00, N'Chờ xác nhận'),
    (N'PBG075', N'DV008', N'PT006', 2, 150000.00, 180000.00, N'Chờ xác nhận'),
    (N'PBG076', N'DV006', N'PT000', 0, 0.00, 650000.00, N'Thiếu phụ tùng + Chờ xác nhận'),
    (N'PBG076', N'DV009', N'PT007', 3, 650000.00, 250000.00, N'Thiếu phụ tùng + Chờ xác nhận'),
    (N'PBG077', N'DV009', N'PT001', 3, 850000.00, 250000.00, N'Thiếu phụ tùng + Chờ xác nhận'),
    (N'PBG077', N'DV010', N'PT008', 1, 250000.00, 180000.00, N'Thiếu phụ tùng + Chờ xác nhận'),
    (N'PBG078', N'DV010', N'PT002', 1, 180000.00, 180000.00, N'Thiếu phụ tùng + Chờ xác nhận'),
    (N'PBG078', N'DV011', N'PT009', 2, 2300000.00, 450000.00, N'Thiếu phụ tùng + Chờ xác nhận'),
    (N'PBG079', N'DV011', N'PT003', 2, 950000.00, 450000.00, N'Thiếu phụ tùng + Chờ xác nhận'),
    (N'PBG079', N'DV012', N'PT010', 3, 3500000.00, 150000.00, N'Thiếu phụ tùng + Chờ xác nhận'),
    (N'PBG080', N'DV012', N'PT004', 3, 220000.00, 150000.00, N'Thiếu phụ tùng + Chờ xác nhận'),
    (N'PBG080', N'DV013', N'PT011', 1, 1250000.00, 800000.00, N'Thiếu phụ tùng + Chờ xác nhận');

-- PhieuSuaChua: 70 bản ghi
INSERT INTO dbo.[PhieuSuaChua] ([maPSC], [maBaoGia], [maKhachHang], [VIN], [maNhanVien], [ngayLap], [ngayHoanThanh], [trangThai])
VALUES
    (N'PSC001', N'PBG001', N'KH001', N'MR0BT9F31JA310001', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Chờ sửa chữa'),
    (N'PSC002', N'PBG002', N'KH002', N'MRHGM6627FA310002', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Chờ sửa chữa'),
    (N'PSC003', N'PBG003', N'KH003', N'KMHCT41B5JA310003', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Chờ sửa chữa'),
    (N'PSC004', N'PBG004', N'KH004', N'KNDEP81A3LA310004', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Chờ sửa chữa'),
    (N'PSC005', N'PBG005', N'KH005', N'JM3KF2W77HA310005', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Chờ sửa chữa'),
    (N'PSC006', N'PBG006', N'KH006', N'MNCUMFF55FA310006', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Chờ sửa chữa'),
    (N'PSC007', N'PBG007', N'KH007', N'MK2NC1W48KA310007', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Chờ sửa chữa'),
    (N'PSC008', N'PBG008', N'KH008', N'MHYANC226MA310008', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Chờ sửa chữa'),
    (N'PSC009', N'PBG009', N'KH009', N'MNTCB4D20GA310009', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Chờ sửa chữa'),
    (N'PSC010', N'PBG010', N'KH010', N'WDDWF4JB5FA310010', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Chờ sửa chữa'),
    (N'PSC011', N'PBG011', N'KH011', N'WBA5R1105KA310011', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Đang sửa chữa'),
    (N'PSC012', N'PBG012', N'KH012', N'RLLV1AFA6PA310012', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Đang sửa chữa'),
    (N'PSC013', N'PBG013', N'KH013', N'MR0BT9F36KA310013', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Đang sửa chữa'),
    (N'PSC014', N'PBG014', N'KH014', N'MRHGM6621GA310014', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Đang sửa chữa'),
    (N'PSC015', N'PBG015', N'KH015', N'KMHCT41BXKA310015', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Đang sửa chữa'),
    (N'PSC016', N'PBG016', N'KH016', N'KNDEP81A8MA310016', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Đang sửa chữa'),
    (N'PSC017', N'PBG017', N'KH017', N'JM3KF2W76JA310017', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Đang sửa chữa'),
    (N'PSC018', N'PBG018', N'KH018', N'MNCUMFF5XGA310018', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Đang sửa chữa'),
    (N'PSC019', N'PBG019', N'KH019', N'MK2NC1W42LA310019', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Đang sửa chữa'),
    (N'PSC020', N'PBG020', N'KH020', N'MHYANC225NA310020', N'NV003', DATEADD(minute,570,DATEADD(day,-2,@NgayMau)), NULL, N'Đang sửa chữa'),
    (N'PSC021', N'PBG021', N'KH021', N'MNTCB4D2XHA310021', N'NV003', DATEADD(minute,570,DATEADD(day,-293,@NgayMau)), DATEADD(minute,720,DATEADD(day,-293,@NgayMau)), N'Đã sửa xong'),
    (N'PSC022', N'PBG022', N'KH022', N'WDDWF4JBXGA310022', N'NV003', DATEADD(minute,570,DATEADD(day,-306,@NgayMau)), DATEADD(minute,720,DATEADD(day,-306,@NgayMau)), N'Đã sửa xong'),
    (N'PSC023', N'PBG023', N'KH023', N'WBA5R110XLA310023', N'NV003', DATEADD(minute,570,DATEADD(day,-319,@NgayMau)), DATEADD(minute,720,DATEADD(day,-319,@NgayMau)), N'Đã sửa xong'),
    (N'PSC024', N'PBG024', N'KH024', N'RLLV1AFA9RA310024', N'NV003', DATEADD(minute,570,DATEADD(day,-332,@NgayMau)), DATEADD(minute,720,DATEADD(day,-332,@NgayMau)), N'Đã sửa xong'),
    (N'PSC025', N'PBG025', N'KH025', N'MR0BT9F30LA310025', N'NV003', DATEADD(minute,570,DATEADD(day,-345,@NgayMau)), DATEADD(minute,720,DATEADD(day,-345,@NgayMau)), N'Đã sửa xong'),
    (N'PSC026', N'PBG026', N'KH026', N'MRHGM6626HA310026', N'NV003', DATEADD(minute,570,DATEADD(day,-358,@NgayMau)), DATEADD(minute,720,DATEADD(day,-358,@NgayMau)), N'Đã sửa xong'),
    (N'PSC027', N'PBG027', N'KH027', N'KMHCT41B4LA310027', N'NV003', DATEADD(minute,570,DATEADD(day,-371,@NgayMau)), DATEADD(minute,720,DATEADD(day,-371,@NgayMau)), N'Đã sửa xong'),
    (N'PSC028', N'PBG028', N'KH028', N'KNDEP81A2NA310028', N'NV003', DATEADD(minute,570,DATEADD(day,-384,@NgayMau)), DATEADD(minute,720,DATEADD(day,-384,@NgayMau)), N'Đã sửa xong'),
    (N'PSC029', N'PBG029', N'KH029', N'JM3KF2W70KA310029', N'NV003', DATEADD(minute,570,DATEADD(day,-397,@NgayMau)), DATEADD(minute,720,DATEADD(day,-397,@NgayMau)), N'Đã sửa xong'),
    (N'PSC030', N'PBG030', N'KH030', N'MNCUMFF59HA310030', N'NV003', DATEADD(minute,570,DATEADD(day,-410,@NgayMau)), DATEADD(minute,720,DATEADD(day,-410,@NgayMau)), N'Đã sửa xong'),
    (N'PSC031', N'PBG031', N'KH031', N'MK2NC1W41MA310031', N'NV003', DATEADD(minute,570,DATEADD(day,-423,@NgayMau)), DATEADD(minute,720,DATEADD(day,-423,@NgayMau)), N'Chờ thanh toán'),
    (N'PSC032', N'PBG032', N'KH032', N'MHYANC228PA310032', N'NV003', DATEADD(minute,570,DATEADD(day,-436,@NgayMau)), DATEADD(minute,720,DATEADD(day,-436,@NgayMau)), N'Chờ thanh toán'),
    (N'PSC033', N'PBG033', N'KH033', N'MNTCB4D29JA310033', N'NV003', DATEADD(minute,570,DATEADD(day,-449,@NgayMau)), DATEADD(minute,720,DATEADD(day,-449,@NgayMau)), N'Chờ thanh toán'),
    (N'PSC034', N'PBG034', N'KH034', N'WDDWF4JB4HA310034', N'NV003', DATEADD(minute,570,DATEADD(day,-462,@NgayMau)), DATEADD(minute,720,DATEADD(day,-462,@NgayMau)), N'Chờ thanh toán'),
    (N'PSC035', N'PBG035', N'KH035', N'WBA5R1104MA310035', N'NV003', DATEADD(minute,570,DATEADD(day,-475,@NgayMau)), DATEADD(minute,720,DATEADD(day,-475,@NgayMau)), N'Chờ thanh toán'),
    (N'PSC036', N'PBG036', N'KH036', N'RLLV1AFA8SA310036', N'NV003', DATEADD(minute,570,DATEADD(day,-488,@NgayMau)), DATEADD(minute,720,DATEADD(day,-488,@NgayMau)), N'Chờ thanh toán'),
    (N'PSC037', N'PBG037', N'KH037', N'MR0BT9F35MA310037', N'NV003', DATEADD(minute,570,DATEADD(day,-501,@NgayMau)), DATEADD(minute,720,DATEADD(day,-501,@NgayMau)), N'Chờ thanh toán'),
    (N'PSC038', N'PBG038', N'KH038', N'MRHGM6625JA310038', N'NV003', DATEADD(minute,570,DATEADD(day,-514,@NgayMau)), DATEADD(minute,720,DATEADD(day,-514,@NgayMau)), N'Chờ thanh toán'),
    (N'PSC039', N'PBG039', N'KH039', N'KMHCT41B9MA310039', N'NV003', DATEADD(minute,570,DATEADD(day,-527,@NgayMau)), DATEADD(minute,720,DATEADD(day,-527,@NgayMau)), N'Chờ thanh toán'),
    (N'PSC040', N'PBG040', N'KH040', N'KNDEP81AXPA310040', N'NV003', DATEADD(minute,570,DATEADD(day,-540,@NgayMau)), DATEADD(minute,720,DATEADD(day,-540,@NgayMau)), N'Chờ thanh toán'),
    (N'PSC041', N'PBG041', N'KH041', N'JM3KF2W7XLA310041', N'NV003', DATEADD(minute,570,DATEADD(day,-553,@NgayMau)), DATEADD(minute,720,DATEADD(day,-553,@NgayMau)), N'Đã thanh toán'),
    (N'PSC042', N'PBG042', N'KH042', N'MNCUMFF58JA310042', N'NV003', DATEADD(minute,570,DATEADD(day,-566,@NgayMau)), DATEADD(minute,720,DATEADD(day,-566,@NgayMau)), N'Đã thanh toán'),
    (N'PSC043', N'PBG043', N'KH043', N'MK2NC1W46NA310043', N'NV003', DATEADD(minute,570,DATEADD(day,-579,@NgayMau)), DATEADD(minute,720,DATEADD(day,-579,@NgayMau)), N'Đã thanh toán'),
    (N'PSC044', N'PBG044', N'KH044', N'MHYANC22XMA310044', N'NV003', DATEADD(minute,570,DATEADD(day,-592,@NgayMau)), DATEADD(minute,720,DATEADD(day,-592,@NgayMau)), N'Đã thanh toán'),
    (N'PSC045', N'PBG045', N'KH045', N'MNTCB4D23KA310045', N'NV003', DATEADD(minute,570,DATEADD(day,-605,@NgayMau)), DATEADD(minute,720,DATEADD(day,-605,@NgayMau)), N'Đã thanh toán'),
    (N'PSC046', N'PBG046', N'KH046', N'WDDWF4JB3JA310046', N'NV003', DATEADD(minute,570,DATEADD(day,-618,@NgayMau)), DATEADD(minute,720,DATEADD(day,-618,@NgayMau)), N'Đã thanh toán'),
    (N'PSC047', N'PBG047', N'KH047', N'WBA5R1109NA310047', N'NV003', DATEADD(minute,570,DATEADD(day,-631,@NgayMau)), DATEADD(minute,720,DATEADD(day,-631,@NgayMau)), N'Đã thanh toán'),
    (N'PSC048', N'PBG048', N'KH048', N'RLLV1AFA5PA310048', N'NV003', DATEADD(minute,570,DATEADD(day,-644,@NgayMau)), DATEADD(minute,720,DATEADD(day,-644,@NgayMau)), N'Đã thanh toán'),
    (N'PSC049', N'PBG049', N'KH001', N'MR0BT9F3XNA310049', N'NV003', DATEADD(minute,570,DATEADD(day,-657,@NgayMau)), DATEADD(minute,720,DATEADD(day,-657,@NgayMau)), N'Đã thanh toán'),
    (N'PSC050', N'PBG050', N'KH002', N'MRHGM6624KA310050', N'NV003', DATEADD(minute,570,DATEADD(day,-20,@NgayMau)), DATEADD(minute,720,DATEADD(day,-20,@NgayMau)), N'Đã thanh toán'),
    (N'PSC051', N'PBG051', N'KH003', N'KMHCT41B8NA310051', N'NV003', DATEADD(minute,570,DATEADD(day,-33,@NgayMau)), DATEADD(minute,720,DATEADD(day,-33,@NgayMau)), N'Hoàn tất'),
    (N'PSC052', N'PBG052', N'KH004', N'KNDEP81A3LA310052', N'NV003', DATEADD(minute,570,DATEADD(day,-46,@NgayMau)), DATEADD(minute,720,DATEADD(day,-46,@NgayMau)), N'Hoàn tất'),
    (N'PSC053', N'PBG053', N'KH005', N'JM3KF2W74MA310053', N'NV003', DATEADD(minute,570,DATEADD(day,-59,@NgayMau)), DATEADD(minute,720,DATEADD(day,-59,@NgayMau)), N'Hoàn tất'),
    (N'PSC054', N'PBG054', N'KH006', N'MNCUMFF52KA310054', N'NV003', DATEADD(minute,570,DATEADD(day,-72,@NgayMau)), DATEADD(minute,720,DATEADD(day,-72,@NgayMau)), N'Hoàn tất'),
    (N'PSC055', N'PBG055', N'KH007', N'MK2NC1W49PA310055', N'NV003', DATEADD(minute,570,DATEADD(day,-85,@NgayMau)), DATEADD(minute,720,DATEADD(day,-85,@NgayMau)), N'Hoàn tất'),
    (N'PSC056', N'PBG056', N'KH008', N'MHYANC224NA310056', N'NV003', DATEADD(minute,570,DATEADD(day,-98,@NgayMau)), DATEADD(minute,720,DATEADD(day,-98,@NgayMau)), N'Hoàn tất'),
    (N'PSC057', N'PBG057', N'KH009', N'MNTCB4D28LA310057', N'NV003', DATEADD(minute,570,DATEADD(day,-111,@NgayMau)), DATEADD(minute,720,DATEADD(day,-111,@NgayMau)), N'Hoàn tất'),
    (N'PSC058', N'PBG058', N'KH010', N'WDDWF4JB0FA310058', N'NV003', DATEADD(minute,570,DATEADD(day,-124,@NgayMau)), DATEADD(minute,720,DATEADD(day,-124,@NgayMau)), N'Hoàn tất'),
    (N'PSC059', N'PBG059', N'KH011', N'WBA5R1101PA310059', N'NV003', DATEADD(minute,570,DATEADD(day,-137,@NgayMau)), DATEADD(minute,720,DATEADD(day,-137,@NgayMau)), N'Hoàn tất'),
    (N'PSC060', N'PBG060', N'KH012', N'RLLV1AFA2RA310060', N'NV003', DATEADD(minute,570,DATEADD(day,-150,@NgayMau)), DATEADD(minute,720,DATEADD(day,-150,@NgayMau)), N'Hoàn tất'),
    (N'PSC061', N'PBG061', N'KH013', N'MR0BT9F37PA310061', N'NV003', DATEADD(minute,570,DATEADD(day,-163,@NgayMau)), NULL, N'Đã hủy'),
    (N'PSC062', N'PBG062', N'KH014', N'MRHGM6623FA310062', N'NV003', DATEADD(minute,570,DATEADD(day,-176,@NgayMau)), NULL, N'Đã hủy'),
    (N'PSC063', N'PBG063', N'KH015', N'KMHCT41B0PA310063', N'NV003', DATEADD(minute,570,DATEADD(day,-189,@NgayMau)), NULL, N'Đã hủy'),
    (N'PSC064', N'PBG064', N'KH016', N'KNDEP81A8MA310064', N'NV003', DATEADD(minute,570,DATEADD(day,-202,@NgayMau)), NULL, N'Đã hủy'),
    (N'PSC065', N'PBG065', N'KH017', N'JM3KF2W79NA310065', N'NV003', DATEADD(minute,570,DATEADD(day,-215,@NgayMau)), NULL, N'Đã hủy'),
    (N'PSC066', N'PBG066', N'KH018', N'MNCUMFF57LA310066', N'NV003', DATEADD(minute,570,DATEADD(day,-228,@NgayMau)), NULL, N'Đã hủy'),
    (N'PSC067', N'PBG067', N'KH019', N'MK2NC1W44KA310067', N'NV003', DATEADD(minute,570,DATEADD(day,-241,@NgayMau)), NULL, N'Đã hủy'),
    (N'PSC068', N'PBG068', N'KH020', N'MHYANC227PA310068', N'NV003', DATEADD(minute,570,DATEADD(day,-254,@NgayMau)), NULL, N'Đã hủy'),
    (N'PSC069', N'PBG069', N'KH021', N'MNTCB4D27GA310069', N'NV003', DATEADD(minute,570,DATEADD(day,-267,@NgayMau)), NULL, N'Đã hủy'),
    (N'PSC070', N'PBG070', N'KH022', N'WDDWF4JBXGA310070', N'NV003', DATEADD(minute,570,DATEADD(day,-280,@NgayMau)), NULL, N'Đã hủy');

-- ChiTietPhieuSuaChua: 105 bản ghi ban đầu
INSERT INTO dbo.[ChiTietPhieuSuaChua] ([maPSC], [maDichVu], [maPhuTung], [trangThaiHangMuc], [soLuongPhuTung], [ketQuaNghiemThu], [ngayBatDauBaoHanh], [thoiHanBaoHanh], [ngayHetHanBaoHanh])
VALUES
    (N'PSC011', N'DV011', N'PT011', N'Thiếu phụ tùng', 3, N'Không đạt', NULL, 0, NULL),
    (N'PSC011', N'DV012', N'PT018', N'Đang sửa chữa', 1, N'Không đạt', NULL, 0, NULL),
    (N'PSC012', N'DV012', N'PT012', N'Đang sửa chữa', 1, N'Không đạt', NULL, 0, NULL),
    (N'PSC012', N'DV013', N'PT019', N'Thiếu phụ tùng', 2, N'Không đạt', NULL, 0, NULL),
    (N'PSC013', N'DV013', N'PT013', N'Thiếu phụ tùng', 2, N'Không đạt', NULL, 0, NULL),
    (N'PSC013', N'DV014', N'PT020', N'Đang sửa chữa', 3, N'Không đạt', NULL, 0, NULL),
    (N'PSC014', N'DV014', N'PT014', N'Đang sửa chữa', 3, N'Không đạt', NULL, 0, NULL),
    (N'PSC014', N'DV015', N'PT021', N'Thiếu phụ tùng', 1, N'Không đạt', NULL, 0, NULL),
    (N'PSC015', N'DV015', N'PT015', N'Thiếu phụ tùng', 1, N'Không đạt', NULL, 0, NULL),
    (N'PSC015', N'DV016', N'PT022', N'Đang sửa chữa', 2, N'Không đạt', NULL, 0, NULL),
    (N'PSC016', N'DV016', N'PT016', N'Đang sửa chữa', 2, N'Không đạt', NULL, 0, NULL),
    (N'PSC016', N'DV017', N'PT023', N'Thiếu phụ tùng', 3, N'Không đạt', NULL, 0, NULL),
    (N'PSC017', N'DV017', N'PT000', N'Đang sửa chữa', 0, N'Không đạt', NULL, 0, NULL),
    (N'PSC017', N'DV001', N'PT024', N'Đang sửa chữa', 1, N'Không đạt', NULL, 0, NULL),
    (N'PSC018', N'DV001', N'PT018', N'Đang sửa chữa', 1, N'Không đạt', NULL, 0, NULL),
    (N'PSC018', N'DV002', N'PT025', N'Thiếu phụ tùng', 2, N'Không đạt', NULL, 0, NULL),
    (N'PSC018', N'DV001', N'PT038', N'Đang sửa chữa', 1, N'Không đạt', NULL, 0, NULL),
    (N'PSC019', N'DV002', N'PT019', N'Thiếu phụ tùng', 2, N'Không đạt', NULL, 0, NULL),
    (N'PSC019', N'DV003', N'PT026', N'Đang sửa chữa', 3, N'Không đạt', NULL, 0, NULL),
    (N'PSC020', N'DV003', N'PT020', N'Đang sửa chữa', 3, N'Không đạt', NULL, 0, NULL),
    (N'PSC020', N'DV004', N'PT027', N'Thiếu phụ tùng', 1, N'Không đạt', NULL, 0, NULL),
    (N'PSC021', N'DV004', N'PT021', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-293,@NgayMau)), 24, DATEADD(month,24,DATEADD(minute,720,DATEADD(day,-293,@NgayMau)))),
    (N'PSC021', N'DV005', N'PT028', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-293,@NgayMau)), 60, DATEADD(month,60,DATEADD(minute,720,DATEADD(day,-293,@NgayMau)))),
    (N'PSC022', N'DV005', N'PT022', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-306,@NgayMau)), 60, DATEADD(month,60,DATEADD(minute,720,DATEADD(day,-306,@NgayMau)))),
    (N'PSC022', N'DV006', N'PT029', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-306,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-306,@NgayMau)))),
    (N'PSC023', N'DV006', N'PT023', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-319,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-319,@NgayMau)))),
    (N'PSC023', N'DV007', N'PT030', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-319,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-319,@NgayMau)))),
    (N'PSC024', N'DV007', N'PT024', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-332,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-332,@NgayMau)))),
    (N'PSC024', N'DV008', N'PT031', N'Hoàn thành', 2, N'Đạt', NULL, 0, NULL),
    (N'PSC025', N'DV008', N'PT025', N'Hoàn thành', 2, N'Đạt', NULL, 0, NULL),
    (N'PSC025', N'DV009', N'PT032', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-345,@NgayMau)), 24, DATEADD(month,24,DATEADD(minute,720,DATEADD(day,-345,@NgayMau)))),
    (N'PSC026', N'DV009', N'PT026', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-358,@NgayMau)), 24, DATEADD(month,24,DATEADD(minute,720,DATEADD(day,-358,@NgayMau)))),
    (N'PSC026', N'DV010', N'PT033', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-358,@NgayMau)), 60, DATEADD(month,60,DATEADD(minute,720,DATEADD(day,-358,@NgayMau)))),
    (N'PSC027', N'DV010', N'PT027', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-371,@NgayMau)), 60, DATEADD(month,60,DATEADD(minute,720,DATEADD(day,-371,@NgayMau)))),
    (N'PSC027', N'DV011', N'PT034', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-371,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-371,@NgayMau)))),
    (N'PSC027', N'DV010', N'PT038', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-371,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-371,@NgayMau)))),
    (N'PSC028', N'DV011', N'PT028', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-384,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-384,@NgayMau)))),
    (N'PSC028', N'DV012', N'PT035', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-384,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-384,@NgayMau)))),
    (N'PSC029', N'DV012', N'PT029', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-397,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-397,@NgayMau)))),
    (N'PSC029', N'DV013', N'PT000', N'Hoàn thành', 0, N'Đạt', NULL, 0, NULL),
    (N'PSC030', N'DV013', N'PT030', N'Hoàn thành', 1, N'Đạt', NULL, 0, NULL),
    (N'PSC030', N'DV014', N'PT037', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-410,@NgayMau)), 24, DATEADD(month,24,DATEADD(minute,720,DATEADD(day,-410,@NgayMau)))),
    (N'PSC031', N'DV014', N'PT031', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-423,@NgayMau)), 24, DATEADD(month,24,DATEADD(minute,720,DATEADD(day,-423,@NgayMau)))),
    (N'PSC031', N'DV015', N'PT038', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-423,@NgayMau)), 60, DATEADD(month,60,DATEADD(minute,720,DATEADD(day,-423,@NgayMau)))),
    (N'PSC032', N'DV015', N'PT032', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-436,@NgayMau)), 60, DATEADD(month,60,DATEADD(minute,720,DATEADD(day,-436,@NgayMau)))),
    (N'PSC032', N'DV016', N'PT001', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-436,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-436,@NgayMau)))),
    (N'PSC033', N'DV016', N'PT033', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-449,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-449,@NgayMau)))),
    (N'PSC033', N'DV017', N'PT002', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-449,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-449,@NgayMau)))),
    (N'PSC034', N'DV017', N'PT034', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-462,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-462,@NgayMau)))),
    (N'PSC034', N'DV001', N'PT003', N'Hoàn thành', 3, N'Đạt', NULL, 0, NULL),
    (N'PSC035', N'DV001', N'PT035', N'Hoàn thành', 3, N'Đạt', NULL, 0, NULL),
    (N'PSC035', N'DV002', N'PT004', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-475,@NgayMau)), 24, DATEADD(month,24,DATEADD(minute,720,DATEADD(day,-475,@NgayMau)))),
    (N'PSC036', N'DV002', N'PT036', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-488,@NgayMau)), 24, DATEADD(month,24,DATEADD(minute,720,DATEADD(day,-488,@NgayMau)))),
    (N'PSC036', N'DV003', N'PT005', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-488,@NgayMau)), 60, DATEADD(month,60,DATEADD(minute,720,DATEADD(day,-488,@NgayMau)))),
    (N'PSC036', N'DV002', N'PT038', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-488,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-488,@NgayMau)))),
    (N'PSC037', N'DV003', N'PT037', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-501,@NgayMau)), 60, DATEADD(month,60,DATEADD(minute,720,DATEADD(day,-501,@NgayMau)))),
    (N'PSC037', N'DV004', N'PT006', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-501,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-501,@NgayMau)))),
    (N'PSC038', N'DV004', N'PT038', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-514,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-514,@NgayMau)))),
    (N'PSC038', N'DV005', N'PT007', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-514,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-514,@NgayMau)))),
    (N'PSC039', N'DV005', N'PT001', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-527,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-527,@NgayMau)))),
    (N'PSC039', N'DV006', N'PT008', N'Hoàn thành', 2, N'Đạt', NULL, 0, NULL),
    (N'PSC040', N'DV006', N'PT002', N'Hoàn thành', 2, N'Đạt', NULL, 0, NULL),
    (N'PSC040', N'DV007', N'PT009', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-540,@NgayMau)), 24, DATEADD(month,24,DATEADD(minute,720,DATEADD(day,-540,@NgayMau)))),
    (N'PSC041', N'DV007', N'PT003', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-553,@NgayMau)), 24, DATEADD(month,24,DATEADD(minute,720,DATEADD(day,-553,@NgayMau)))),
    (N'PSC041', N'DV008', N'PT010', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-553,@NgayMau)), 60, DATEADD(month,60,DATEADD(minute,720,DATEADD(day,-553,@NgayMau)))),
    (N'PSC042', N'DV008', N'PT004', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-566,@NgayMau)), 60, DATEADD(month,60,DATEADD(minute,720,DATEADD(day,-566,@NgayMau)))),
    (N'PSC042', N'DV009', N'PT011', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-566,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-566,@NgayMau)))),
    (N'PSC043', N'DV009', N'PT005', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-579,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-579,@NgayMau)))),
    (N'PSC043', N'DV010', N'PT012', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-579,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-579,@NgayMau)))),
    (N'PSC044', N'DV010', N'PT006', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-592,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-592,@NgayMau)))),
    (N'PSC044', N'DV011', N'PT013', N'Hoàn thành', 1, N'Đạt', NULL, 0, NULL),
    (N'PSC045', N'DV011', N'PT007', N'Hoàn thành', 1, N'Đạt', NULL, 0, NULL),
    (N'PSC045', N'DV012', N'PT014', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-605,@NgayMau)), 24, DATEADD(month,24,DATEADD(minute,720,DATEADD(day,-605,@NgayMau)))),
    (N'PSC045', N'DV011', N'PT038', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-605,@NgayMau)), 60, DATEADD(month,60,DATEADD(minute,720,DATEADD(day,-605,@NgayMau)))),
    (N'PSC046', N'DV012', N'PT008', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-618,@NgayMau)), 24, DATEADD(month,24,DATEADD(minute,720,DATEADD(day,-618,@NgayMau)))),
    (N'PSC046', N'DV013', N'PT015', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-618,@NgayMau)), 60, DATEADD(month,60,DATEADD(minute,720,DATEADD(day,-618,@NgayMau)))),
    (N'PSC047', N'DV013', N'PT009', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-631,@NgayMau)), 60, DATEADD(month,60,DATEADD(minute,720,DATEADD(day,-631,@NgayMau)))),
    (N'PSC047', N'DV014', N'PT016', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-631,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-631,@NgayMau)))),
    (N'PSC048', N'DV014', N'PT010', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-644,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-644,@NgayMau)))),
    (N'PSC048', N'DV015', N'PT017', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-644,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-644,@NgayMau)))),
    (N'PSC049', N'DV015', N'PT011', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-657,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-657,@NgayMau)))),
    (N'PSC049', N'DV016', N'PT018', N'Hoàn thành', 3, N'Đạt', NULL, 0, NULL),
    (N'PSC050', N'DV016', N'PT012', N'Hoàn thành', 3, N'Đạt', NULL, 0, NULL),
    (N'PSC050', N'DV017', N'PT019', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-20,@NgayMau)), 24, DATEADD(month,24,DATEADD(minute,720,DATEADD(day,-20,@NgayMau)))),
    (N'PSC051', N'DV017', N'PT039', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-33,@NgayMau)), 24, DATEADD(month,24,DATEADD(minute,720,DATEADD(day,-33,@NgayMau)))),
    (N'PSC051', N'DV018', N'PT039', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-33,@NgayMau)), 60, DATEADD(month,60,DATEADD(minute,720,DATEADD(day,-33,@NgayMau)))),
    (N'PSC052', N'DV017', N'PT040', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-46,@NgayMau)), 60, DATEADD(month,60,DATEADD(minute,720,DATEADD(day,-46,@NgayMau)))),
    (N'PSC052', N'DV018', N'PT040', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-46,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-46,@NgayMau)))),
    (N'PSC053', N'DV002', N'PT015', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-59,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-59,@NgayMau)))),
    (N'PSC053', N'DV003', N'PT022', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-59,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-59,@NgayMau)))),
    (N'PSC054', N'DV003', N'PT016', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-72,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-72,@NgayMau)))),
    (N'PSC054', N'DV004', N'PT023', N'Hoàn thành', 2, N'Đạt', NULL, 0, NULL),
    (N'PSC054', N'DV003', N'PT038', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-72,@NgayMau)), 24, DATEADD(month,24,DATEADD(minute,720,DATEADD(day,-72,@NgayMau)))),
    (N'PSC055', N'DV004', N'PT017', N'Hoàn thành', 2, N'Đạt', NULL, 0, NULL),
    (N'PSC055', N'DV005', N'PT024', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-85,@NgayMau)), 24, DATEADD(month,24,DATEADD(minute,720,DATEADD(day,-85,@NgayMau)))),
    (N'PSC056', N'DV005', N'PT018', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-98,@NgayMau)), 24, DATEADD(month,24,DATEADD(minute,720,DATEADD(day,-98,@NgayMau)))),
    (N'PSC056', N'DV006', N'PT025', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-98,@NgayMau)), 60, DATEADD(month,60,DATEADD(minute,720,DATEADD(day,-98,@NgayMau)))),
    (N'PSC057', N'DV006', N'PT019', N'Hoàn thành', 1, N'Đạt', DATEADD(minute,720,DATEADD(day,-111,@NgayMau)), 60, DATEADD(month,60,DATEADD(minute,720,DATEADD(day,-111,@NgayMau)))),
    (N'PSC057', N'DV007', N'PT026', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-111,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-111,@NgayMau)))),
    (N'PSC058', N'DV007', N'PT020', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-124,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-124,@NgayMau)))),
    (N'PSC058', N'DV008', N'PT027', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-124,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-124,@NgayMau)))),
    (N'PSC059', N'DV008', N'PT021', N'Hoàn thành', 3, N'Đạt', DATEADD(minute,720,DATEADD(day,-137,@NgayMau)), 120, DATEADD(month,120,DATEADD(minute,720,DATEADD(day,-137,@NgayMau)))),
    (N'PSC059', N'DV009', N'PT028', N'Hoàn thành', 1, N'Đạt', NULL, 0, NULL),
    (N'PSC060', N'DV009', N'PT022', N'Hoàn thành', 1, N'Đạt', NULL, 0, NULL),
    (N'PSC060', N'DV010', N'PT029', N'Hoàn thành', 2, N'Đạt', DATEADD(minute,720,DATEADD(day,-150,@NgayMau)), 24, DATEADD(month,24,DATEADD(minute,720,DATEADD(day,-150,@NgayMau))));




    -- 36 phiếu nhập kho lịch sử (PNK001 - PNK036) cho fresh install
INSERT INTO dbo.[PhieuNhapKho] ([maPNK], [maNCC], [maNhanVien], [ngayNhap], [trangThai])
VALUES
    (N'PNK001', N'NCC001', N'NV002', DATEADD(minute,600,DATEADD(day,-176,@NgayMau)), N'Chờ duyệt'),
    (N'PNK002', N'NCC002', N'NV002', DATEADD(minute,600,DATEADD(day,-172,@NgayMau)), N'Đã duyệt'),
    (N'PNK003', N'NCC003', N'NV002', DATEADD(minute,600,DATEADD(day,-168,@NgayMau)), N'Không duyệt'),
    (N'PNK004', N'NCC004', N'NV002', DATEADD(minute,600,DATEADD(day,-164,@NgayMau)), N'Chờ duyệt'),
    (N'PNK005', N'NCC005', N'NV002', DATEADD(minute,600,DATEADD(day,-160,@NgayMau)), N'Đã duyệt'),
    (N'PNK006', N'NCC006', N'NV002', DATEADD(minute,600,DATEADD(day,-156,@NgayMau)), N'Không duyệt'),
    (N'PNK007', N'NCC007', N'NV002', DATEADD(minute,600,DATEADD(day,-152,@NgayMau)), N'Chờ duyệt'),
    (N'PNK008', N'NCC008', N'NV002', DATEADD(minute,600,DATEADD(day,-148,@NgayMau)), N'Đã duyệt'),
    (N'PNK009', N'NCC009', N'NV002', DATEADD(minute,600,DATEADD(day,-144,@NgayMau)), N'Không duyệt'),
    (N'PNK010', N'NCC010', N'NV002', DATEADD(minute,600,DATEADD(day,-140,@NgayMau)), N'Chờ duyệt'),
    (N'PNK011', N'NCC011', N'NV002', DATEADD(minute,600,DATEADD(day,-136,@NgayMau)), N'Đã duyệt'),
    (N'PNK012', N'NCC012', N'NV002', DATEADD(minute,600,DATEADD(day,-132,@NgayMau)), N'Không duyệt'),
    (N'PNK013', N'NCC001', N'NV002', DATEADD(minute,600,DATEADD(day,-128,@NgayMau)), N'Chờ duyệt'),
    (N'PNK014', N'NCC002', N'NV002', DATEADD(minute,600,DATEADD(day,-124,@NgayMau)), N'Đã duyệt'),
    (N'PNK015', N'NCC003', N'NV002', DATEADD(minute,600,DATEADD(day,-120,@NgayMau)), N'Không duyệt'),
    (N'PNK016', N'NCC004', N'NV002', DATEADD(minute,600,DATEADD(day,-116,@NgayMau)), N'Chờ duyệt'),
    (N'PNK017', N'NCC005', N'NV002', DATEADD(minute,600,DATEADD(day,-112,@NgayMau)), N'Đã duyệt'),
    (N'PNK018', N'NCC006', N'NV002', DATEADD(minute,600,DATEADD(day,-108,@NgayMau)), N'Không duyệt'),
    (N'PNK019', N'NCC007', N'NV002', DATEADD(minute,600,DATEADD(day,-104,@NgayMau)), N'Chờ duyệt'),
    (N'PNK020', N'NCC008', N'NV002', DATEADD(minute,600,DATEADD(day,-100,@NgayMau)), N'Đã duyệt'),
    (N'PNK021', N'NCC009', N'NV002', DATEADD(minute,600,DATEADD(day,-96,@NgayMau)), N'Không duyệt'),
    (N'PNK022', N'NCC010', N'NV002', DATEADD(minute,600,DATEADD(day,-92,@NgayMau)), N'Chờ duyệt'),
    (N'PNK023', N'NCC011', N'NV002', DATEADD(minute,600,DATEADD(day,-88,@NgayMau)), N'Đã duyệt'),
    (N'PNK024', N'NCC012', N'NV002', DATEADD(minute,600,DATEADD(day,-84,@NgayMau)), N'Không duyệt'),
    (N'PNK025', N'NCC001', N'NV002', DATEADD(minute,600,DATEADD(day,-80,@NgayMau)), N'Chờ duyệt'),
    (N'PNK026', N'NCC002', N'NV002', DATEADD(minute,600,DATEADD(day,-76,@NgayMau)), N'Đã duyệt'),
    (N'PNK027', N'NCC003', N'NV002', DATEADD(minute,600,DATEADD(day,-72,@NgayMau)), N'Không duyệt'),
    (N'PNK028', N'NCC004', N'NV002', DATEADD(minute,600,DATEADD(day,-68,@NgayMau)), N'Chờ duyệt'),
    (N'PNK029', N'NCC005', N'NV002', DATEADD(minute,600,DATEADD(day,-64,@NgayMau)), N'Đã duyệt'),
    (N'PNK030', N'NCC006', N'NV002', DATEADD(minute,600,DATEADD(day,-60,@NgayMau)), N'Không duyệt'),
    (N'PNK031', N'NCC007', N'NV002', DATEADD(minute,600,DATEADD(day,-56,@NgayMau)), N'Chờ duyệt'),
    (N'PNK032', N'NCC008', N'NV002', DATEADD(minute,600,DATEADD(day,-52,@NgayMau)), N'Đã duyệt'),
    (N'PNK033', N'NCC009', N'NV002', DATEADD(minute,600,DATEADD(day,-48,@NgayMau)), N'Không duyệt'),
    (N'PNK034', N'NCC010', N'NV002', DATEADD(minute,600,DATEADD(day,-44,@NgayMau)), N'Chờ duyệt'),
    (N'PNK035', N'NCC011', N'NV002', DATEADD(minute,600,DATEADD(day,-40,@NgayMau)), N'Đã duyệt'),
    (N'PNK036', N'NCC012', N'NV002', DATEADD(minute,600,DATEADD(day,-36,@NgayMau)), N'Không duyệt');

-- ChiTietPhieuNhapKho: 108 bản ghi
INSERT INTO dbo.[ChiTietPhieuNhapKho] ([maPNK], [maPhuTung], [soLuongNhap], [donGiaNhap])
VALUES
    (N'PNK001', N'PT003', 8, 684000.00),
    (N'PNK001', N'PT004', 9, 158400.00),
    (N'PNK001', N'PT005', 10, 1332000.00),
    (N'PNK002', N'PT006', 8, 108000.00),
    (N'PNK002', N'PT007', 10, 468000.00),
    (N'PNK002', N'PT008', 12, 180000.00),
    (N'PNK003', N'PT009', 8, 1656000.00),
    (N'PNK003', N'PT010', 11, 2520000.00),
    (N'PNK003', N'PT011', 14, 900000.00),
    (N'PNK004', N'PT012', 8, 2016000.00),
    (N'PNK004', N'PT013', 12, 1296000.00),
    (N'PNK004', N'PT014', 16, 230400.00),
    (N'PNK005', N'PT015', 8, 345600.00),
    (N'PNK005', N'PT016', 13, 252000.00),
    (N'PNK005', N'PT017', 18, 1044000.00),
    (N'PNK006', N'PT018', 8, 684000.00),
    (N'PNK006', N'PT019', 14, 4680000.00),
    (N'PNK006', N'PT020', 20, 612000.00),
    (N'PNK007', N'PT021', 8, 28800000.00),
    (N'PNK007', N'PT022', 15, 25200000.00),
    (N'PNK007', N'PT023', 22, 2304000.00),
    (N'PNK008', N'PT024', 8, 3456000.00),
    (N'PNK008', N'PT025', 16, 2736000.00),
    (N'PNK008', N'PT026', 24, 6120000.00),
    (N'PNK009', N'PT027', 8, 540000.00),
    (N'PNK009', N'PT028', 17, 684000.00),
    (N'PNK009', N'PT029', 26, 1332000.00),
    (N'PNK010', N'PT030', 8, 1872000.00),
    (N'PNK010', N'PT031', 18, 489600.00),
    (N'PNK010', N'PT032', 28, 1188000.00),
    (N'PNK011', N'PT033', 8, 2304000.00),
    (N'PNK011', N'PT034', 19, 324000.00),
    (N'PNK011', N'PT035', 30, 129600.00),
    (N'PNK012', N'PT036', 8, 68400.00),
    (N'PNK012', N'PT037', 20, 10800.00),
    (N'PNK012', N'PT038', 32, 18000.00),
    (N'PNK013', N'PT039', 8, 0.00),
    (N'PNK013', N'PT040', 21, 0.00),
    (N'PNK013', N'PT001', 34, 612000.00),
    (N'PNK014', N'PT002', 8, 129600.00),
    (N'PNK014', N'PT003', 22, 684000.00),
    (N'PNK014', N'PT004', 36, 158400.00),
    (N'PNK015', N'PT005', 8, 1332000.00),
    (N'PNK015', N'PT006', 23, 108000.00),
    (N'PNK015', N'PT007', 38, 468000.00),
    (N'PNK016', N'PT008', 8, 180000.00),
    (N'PNK016', N'PT009', 24, 1656000.00),
    (N'PNK016', N'PT010', 40, 2520000.00),
    (N'PNK017', N'PT011', 8, 900000.00),
    (N'PNK017', N'PT012', 25, 2016000.00),
    (N'PNK017', N'PT013', 42, 1296000.00),
    (N'PNK018', N'PT014', 8, 230400.00),
    (N'PNK018', N'PT015', 26, 345600.00),
    (N'PNK018', N'PT016', 44, 252000.00),
    (N'PNK019', N'PT017', 8, 1044000.00),
    (N'PNK019', N'PT018', 27, 684000.00),
    (N'PNK019', N'PT019', 46, 4680000.00),
    (N'PNK020', N'PT020', 8, 612000.00),
    (N'PNK020', N'PT021', 28, 28800000.00),
    (N'PNK020', N'PT022', 48, 25200000.00),
    (N'PNK021', N'PT023', 8, 2304000.00),
    (N'PNK021', N'PT024', 29, 3456000.00),
    (N'PNK021', N'PT025', 50, 2736000.00),
    (N'PNK022', N'PT026', 8, 6120000.00),
    (N'PNK022', N'PT027', 30, 540000.00),
    (N'PNK022', N'PT028', 52, 684000.00),
    (N'PNK023', N'PT029', 8, 1332000.00),
    (N'PNK023', N'PT030', 31, 1872000.00),
    (N'PNK023', N'PT031', 54, 489600.00),
    (N'PNK024', N'PT032', 8, 1188000.00),
    (N'PNK024', N'PT033', 32, 2304000.00),
    (N'PNK024', N'PT034', 56, 324000.00),
    (N'PNK025', N'PT035', 8, 129600.00),
    (N'PNK025', N'PT036', 33, 68400.00),
    (N'PNK025', N'PT037', 58, 10800.00),
    (N'PNK026', N'PT038', 8, 18000.00),
    (N'PNK026', N'PT039', 34, 0.00),
    (N'PNK026', N'PT040', 60, 0.00),
    (N'PNK027', N'PT001', 8, 612000.00),
    (N'PNK027', N'PT002', 35, 129600.00),
    (N'PNK027', N'PT003', 62, 684000.00),
    (N'PNK028', N'PT004', 8, 158400.00),
    (N'PNK028', N'PT005', 36, 1332000.00),
    (N'PNK028', N'PT006', 64, 108000.00),
    (N'PNK029', N'PT007', 8, 468000.00),
    (N'PNK029', N'PT008', 37, 180000.00),
    (N'PNK029', N'PT009', 66, 1656000.00),
    (N'PNK030', N'PT010', 8, 2520000.00),
    (N'PNK030', N'PT011', 38, 900000.00),
    (N'PNK030', N'PT012', 68, 2016000.00),
    (N'PNK031', N'PT013', 8, 1296000.00),
    (N'PNK031', N'PT014', 39, 230400.00),
    (N'PNK031', N'PT015', 70, 345600.00),
    (N'PNK032', N'PT016', 8, 252000.00),
    (N'PNK032', N'PT017', 40, 1044000.00),
    (N'PNK032', N'PT018', 72, 684000.00),
    (N'PNK033', N'PT019', 8, 4680000.00),
    (N'PNK033', N'PT020', 41, 612000.00),
    (N'PNK033', N'PT021', 74, 28800000.00),
    (N'PNK034', N'PT022', 8, 25200000.00),
    (N'PNK034', N'PT023', 42, 2304000.00),
    (N'PNK034', N'PT024', 76, 3456000.00),
    (N'PNK035', N'PT025', 8, 2736000.00),
    (N'PNK035', N'PT026', 43, 6120000.00),
    (N'PNK035', N'PT027', 78, 540000.00),
    (N'PNK036', N'PT028', 8, 684000.00),
    (N'PNK036', N'PT029', 44, 1332000.00),
    (N'PNK036', N'PT030', 80, 1872000.00);

-- HoaDon dịch vụ: 30 bản ghi; thêm 12 hóa đơn bán hàng ở phần nâng cấp cuối file.



-- HoaDon dịch vụ: 30 bản ghi; thêm 12 hóa đơn bán hàng ở phần nâng cấp cuối file.
INSERT INTO dbo.[HoaDon] ([maHoaDon], [maGiamGia], [maKhachHang], [maNhanVien], [maPSC], [VIN], [ngayLap], [tienPhuTung], [tienCong], [dichVuKhac], [thueVAT], [giamGia], [trangThaiThanhToan], [maPhuongThuc])
VALUES
    (N'HD001', N'MGG013', N'KH031', N'NV001', N'PSC031', N'MK2NC1W41MA310031', DATEADD(minute,735,DATEADD(day,-423,@NgayMau)), 1435000.00, 13500000.00, 0.00, 1194800.00, 0.00, N'Chờ thanh toán', N'TT002'),
    (N'HD002', N'MGG014', N'KH032', N'NV001', N'PSC032', N'MHYANC228PA310032', DATEADD(minute,735,DATEADD(day,-436,@NgayMau)), 5800000.00, 20500000.00, 0.00, 2104000.00, 1025000.00, N'Chờ thanh toán', N'TT001'),
    (N'HD003', N'MGG015', N'KH033', N'NV001', N'PSC033', N'MNTCB4D29JA310033', DATEADD(minute,735,DATEADD(day,-449,@NgayMau)), 3560000.00, 8500000.00, 100000.00, 972800.00, 850000.00, N'Chờ thanh toán', N'TT002'),
    (N'HD004', N'MGG016', N'KH034', N'NV001', N'PSC034', N'WDDWF4JB4HA310034', DATEADD(minute,735,DATEADD(day,-462,@NgayMau)), 3750000.00, 150000.00, 0.00, 312000.00, 22500.00, N'Chờ thanh toán', N'TT001'),
    (N'HD005', N'MGG017', N'KH035', N'NV001', N'PSC035', N'WBA5R1104MA310035', DATEADD(minute,735,DATEADD(day,-475,@NgayMau)), 760000.00, 250000.00, 0.00, 80800.00, 50000.00, N'Chờ thanh toán', N'TT002'),
    (N'HD006', NULL, N'KH036', N'NV001', N'PSC036', N'RLLV1AFA8SA310036', DATEADD(minute,735,DATEADD(day,-488,@NgayMau)), 3820000.00, 350000.00, 100000.00, 341600.00, 0.00, N'Chờ thanh toán', N'TT001'),
    (N'HD007', NULL, N'KH037', N'NV001', N'PSC037', N'MR0BT9F35MA310037', DATEADD(minute,735,DATEADD(day,-501,@NgayMau)), 480000.00, 550000.00, 0.00, 82400.00, 0.00, N'Chờ thanh toán', N'TT002'),
    (N'HD008', NULL, N'KH038', N'NV001', N'PSC038', N'MRHGM6625JA310038', DATEADD(minute,735,DATEADD(day,-514,@NgayMau)), 725000.00, 420000.00, 0.00, 91600.00, 0.00, N'Chờ thanh toán', N'TT001'),
    (N'HD009', NULL, N'KH039', N'NV001', N'PSC039', N'KMHCT41B9MA310039', DATEADD(minute,735,DATEADD(day,-527,@NgayMau)), 1350000.00, 770000.00, 100000.00, 177600.00, 0.00, N'Chờ thanh toán', N'TT002'),
    (N'HD010', NULL, N'KH040', N'NV001', N'PSC040', N'KNDEP81AXPA310040', DATEADD(minute,735,DATEADD(day,-540,@NgayMau)), 7260000.00, 850000.00, 0.00, 648800.00, 0.00, N'Chờ thanh toán', N'TT001'),
    (N'HD011', N'MGG001', N'KH041', N'NV001', N'PSC041', N'JM3KF2W7XLA310041', DATEADD(minute,735,DATEADD(day,-553,@NgayMau)), 6350000.00, 380000.00, 0.00, 538400.00, 0.00, N'Đã thanh toán', N'TT002'),
    (N'HD012', N'MGG002', N'KH042', N'NV001', N'PSC042', N'MNCUMFF58JA310042', DATEADD(minute,735,DATEADD(day,-566,@NgayMau)), 2720000.00, 430000.00, 100000.00, 260000.00, 21500.00, N'Đã thanh toán', N'TT001'),
    (N'HD013', N'MGG003', N'KH043', N'NV001', N'PSC043', N'MK2NC1W46NA310043', DATEADD(minute,735,DATEADD(day,-579,@NgayMau)), 12100000.00, 430000.00, 0.00, 1002400.00, 43000.00, N'Đã thanh toán', N'TT002'),
    (N'HD014', N'MGG004', N'KH044', N'NV001', N'PSC044', N'MHYANC22XMA310044', DATEADD(minute,735,DATEADD(day,-592,@NgayMau)), 2250000.00, 630000.00, 0.00, 230400.00, 94500.00, N'Đã thanh toán', N'TT001'),
    (N'HD015', N'MGG005', N'KH045', N'NV001', N'PSC045', N'MNTCB4D23KA310045', DATEADD(minute,735,DATEADD(day,-605,@NgayMau)), 1315000.00, 600000.00, 100000.00, 161200.00, 120000.00, N'Đã thanh toán', N'TT002'),
    (N'HD016', N'MGG006', N'KH046', N'NV001', N'PSC046', N'WDDWF4JB3JA310046', DATEADD(minute,735,DATEADD(day,-618,@NgayMau)), 1940000.00, 950000.00, 0.00, 231200.00, 285000.00, N'Đã thanh toán', N'TT001'),
    (N'HD017', N'MGG007', N'KH047', N'NV001', N'PSC047', N'WBA5R1109NA310047', DATEADD(minute,735,DATEADD(day,-631,@NgayMau)), 7250000.00, 2300000.00, 0.00, 764000.00, 0.00, N'Đã thanh toán', N'TT002'),
    (N'HD018', N'MGG008', N'KH048', N'NV001', N'PSC048', N'RLLV1AFA5PA310048', DATEADD(minute,735,DATEADD(day,-644,@NgayMau)), 6400000.00, 13500000.00, 100000.00, 1600000.00, 675000.00, N'Đã thanh toán', N'TT001'),
    (N'HD019', N'MGG009', N'KH001', N'NV001', N'PSC049', N'MR0BT9F3XNA310049', DATEADD(minute,735,DATEADD(day,-657,@NgayMau)), 5350000.00, 20500000.00, 0.00, 2068000.00, 2050000.00, N'Đã thanh toán', N'TT002'),
    (N'HD020', N'MGG010', N'KH002', N'NV001', N'PSC050', N'MRHGM6624KA310050', DATEADD(minute,735,DATEADD(day,-20,@NgayMau)), 14900000.00, 8500000.00, 0.00, 1872000.00, 1275000.00, N'Đã thanh toán', N'TT001'),
    (N'HD021', N'MGG011', N'KH003', N'NV001', N'PSC051', N'KMHCT41B8NA310051', DATEADD(minute,735,DATEADD(day,-33,@NgayMau)), 0.00, 50000.00, 0.00, 4000.00, 10000.00, N'Đã thanh toán', N'TT002'),
    (N'HD022', N'MGG012', N'KH004', N'NV001', N'PSC052', N'KNDEP81A3LA310052', DATEADD(minute,735,DATEADD(day,-46,@NgayMau)), 0.00, 0.00, 0.00, 0.00, 0.00, N'Đã thanh toán', N'TT001'),
    (N'HD023', NULL, N'KH005', N'NV001', N'PSC053', N'JM3KF2W74MA310053', DATEADD(minute,735,DATEADD(day,-59,@NgayMau)), 36440000.00, 350000.00, 0.00, 2943200.00, 0.00, N'Đã thanh toán', N'TT002'),
    (N'HD024', NULL, N'KH006', N'NV001', N'PSC054', N'MNCUMFF52KA310054', DATEADD(minute,735,DATEADD(day,-72,@NgayMau)), 6775000.00, 550000.00, 100000.00, 594000.00, 0.00, N'Đã thanh toán', N'TT001'),
    (N'HD025', NULL, N'KH007', N'NV001', N'PSC055', N'MK2NC1W49PA310055', DATEADD(minute,735,DATEADD(day,-85,@NgayMau)), 17300000.00, 420000.00, 0.00, 1417600.00, 0.00, N'Đã thanh toán', N'TT002'),
    (N'HD026', NULL, N'KH008', N'NV001', N'PSC056', N'MHYANC224NA310056', DATEADD(minute,735,DATEADD(day,-98,@NgayMau)), 6650000.00, 770000.00, 0.00, 593600.00, 0.00, N'Đã thanh toán', N'TT001'),
    (N'HD027', NULL, N'KH009', N'NV001', N'PSC057', N'MNTCB4D28LA310057', DATEADD(minute,735,DATEADD(day,-111,@NgayMau)), 23500000.00, 850000.00, 100000.00, 1956000.00, 0.00, N'Đã thanh toán', N'TT002'),
    (N'HD028', NULL, N'KH010', N'NV001', N'PSC058', N'WDDWF4JB0FA310058', DATEADD(minute,735,DATEADD(day,-124,@NgayMau)), 3950000.00, 380000.00, 0.00, 346400.00, 0.00, N'Đã thanh toán', N'TT001'),
    (N'HD029', NULL, N'KH011', N'NV001', N'PSC059', N'WBA5R1101PA310059', DATEADD(minute,735,DATEADD(day,-137,@NgayMau)), 120950000.00, 430000.00, 0.00, 9710400.00, 0.00, N'Đã thanh toán', N'TT002'),
    (N'HD030', NULL, N'KH012', N'NV001', N'PSC060', N'RLLV1AFA2RA310060', DATEADD(minute,735,DATEADD(day,-150,@NgayMau)), 38700000.00, 430000.00, 100000.00, 3138400.00, 0.00, N'Đã thanh toán', N'TT001');

-- ChiTietHoaDon: 120 bản ghi
INSERT INTO dbo.[ChiTietHoaDon] ([maHoaDon], [maDichVu], [maPhuTung], [soLuong], [donGia])
VALUES
    (N'HD001', N'DV014', NULL, 1, 1500000.00),
    (N'HD001', NULL, N'PT031', 2, 680000.00),
    (N'HD001', N'DV015', NULL, 1, 12000000.00),
    (N'HD001', NULL, N'PT038', 3, 25000.00),
    (N'HD002', N'DV015', NULL, 1, 12000000.00),
    (N'HD002', N'DV015', N'PT032', 3, 1650000.00),
    (N'HD002', N'DV016', NULL, 1, 8500000.00),
    (N'HD002', N'DV016', N'PT001', 1, 850000.00),
    (N'HD003', N'DV016', NULL, 1, 8500000.00),
    (N'HD003', NULL, N'PT033', 1, 3200000.00),
    (N'HD003', NULL, N'PT002', 2, 180000.00),
    (N'HD004', N'DV017', N'PT034', 2, 450000.00),
    (N'HD004', N'DV001', NULL, 1, 150000.00),
    (N'HD004', N'DV001', N'PT003', 3, 950000.00),
    (N'HD005', N'DV001', NULL, 1, 150000.00),
    (N'HD005', NULL, N'PT035', 3, 180000.00),
    (N'HD005', N'DV002', NULL, 1, 100000.00),
    (N'HD005', NULL, N'PT004', 1, 220000.00),
    (N'HD006', N'DV002', NULL, 1, 100000.00),
    (N'HD006', N'DV002', N'PT036', 1, 95000.00),
    (N'HD006', N'DV003', NULL, 1, 250000.00),
    (N'HD006', N'DV003', N'PT005', 2, 1850000.00),
    (N'HD006', N'DV002', N'PT038', 1, 25000.00),
    (N'HD007', N'DV003', NULL, 1, 250000.00),
    (N'HD007', NULL, N'PT037', 2, 15000.00),
    (N'HD007', N'DV004', NULL, 1, 300000.00),
    (N'HD007', NULL, N'PT006', 3, 150000.00),
    (N'HD008', N'DV004', NULL, 1, 300000.00),
    (N'HD008', N'DV004', N'PT038', 3, 25000.00),
    (N'HD008', N'DV005', NULL, 1, 120000.00),
    (N'HD008', N'DV005', N'PT007', 1, 650000.00),
    (N'HD009', N'DV005', NULL, 1, 120000.00),
    (N'HD009', NULL, N'PT001', 1, 850000.00),
    (N'HD009', N'DV006', NULL, 1, 650000.00),
    (N'HD009', NULL, N'PT008', 2, 250000.00),
    (N'HD010', N'DV006', NULL, 1, 650000.00),
    (N'HD010', N'DV006', N'PT002', 2, 180000.00),
    (N'HD010', N'DV007', NULL, 1, 200000.00),
    (N'HD010', N'DV007', N'PT009', 3, 2300000.00),
    (N'HD011', N'DV007', NULL, 1, 200000.00),
    (N'HD011', NULL, N'PT003', 3, 950000.00),
    (N'HD011', N'DV008', NULL, 1, 180000.00),
    (N'HD011', NULL, N'PT010', 1, 3500000.00),
    (N'HD012', N'DV008', NULL, 1, 180000.00),
    (N'HD012', N'DV008', N'PT004', 1, 220000.00),
    (N'HD012', N'DV009', NULL, 1, 250000.00),
    (N'HD012', N'DV009', N'PT011', 2, 1250000.00),
    (N'HD013', N'DV009', NULL, 1, 250000.00),
    (N'HD013', NULL, N'PT005', 2, 1850000.00),
    (N'HD013', N'DV010', NULL, 1, 180000.00),
    (N'HD013', NULL, N'PT012', 3, 2800000.00),
    (N'HD014', N'DV010', NULL, 1, 180000.00),
    (N'HD014', N'DV010', N'PT006', 3, 150000.00),
    (N'HD014', N'DV011', NULL, 1, 450000.00),
    (N'HD014', N'DV011', N'PT013', 1, 1800000.00),
    (N'HD015', N'DV011', NULL, 1, 450000.00),
    (N'HD015', NULL, N'PT007', 1, 650000.00),
    (N'HD015', N'DV012', NULL, 1, 150000.00),
    (N'HD015', NULL, N'PT014', 2, 320000.00),
    (N'HD015', NULL, N'PT038', 1, 25000.00),
    (N'HD016', N'DV012', NULL, 1, 150000.00),
    (N'HD016', N'DV012', N'PT008', 2, 250000.00),
    (N'HD016', N'DV013', NULL, 1, 800000.00),
    (N'HD016', N'DV013', N'PT015', 3, 480000.00),
    (N'HD017', N'DV013', NULL, 1, 800000.00),
    (N'HD017', NULL, N'PT009', 3, 2300000.00),
    (N'HD017', N'DV014', NULL, 1, 1500000.00),
    (N'HD017', NULL, N'PT016', 1, 350000.00),
    (N'HD018', N'DV014', NULL, 1, 1500000.00),
    (N'HD018', N'DV014', N'PT010', 1, 3500000.00),
    (N'HD018', N'DV015', NULL, 1, 12000000.00),
    (N'HD018', N'DV015', N'PT017', 2, 1450000.00),
    (N'HD019', N'DV015', NULL, 1, 12000000.00),
    (N'HD019', NULL, N'PT011', 2, 1250000.00),
    (N'HD019', N'DV016', NULL, 1, 8500000.00),
    (N'HD019', NULL, N'PT018', 3, 950000.00),
    (N'HD020', N'DV016', NULL, 1, 8500000.00),
    (N'HD020', N'DV016', N'PT012', 3, 2800000.00),
    (N'HD020', N'DV017', N'PT019', 1, 6500000.00),
    (N'HD021', N'DV017', NULL, 1, 25000.00),
    (N'HD021', NULL, N'PT039', 2, 0.00),
    (N'HD021', N'DV018', NULL, 1, 25000.00),
    (N'HD022', N'DV017', NULL, 1, 0.00),
    (N'HD022', N'DV017', N'PT040', 1, 0.00),
    (N'HD022', N'DV018', NULL, 1, 0.00),
    (N'HD022', N'DV018', N'PT040', 1, 0.00),
    (N'HD022', N'DV020', NULL, 0, 0.00),
    (N'HD023', N'DV002', NULL, 1, 100000.00),
    (N'HD023', NULL, N'PT015', 3, 480000.00),
    (N'HD023', N'DV003', NULL, 1, 250000.00),
    (N'HD023', NULL, N'PT022', 1, 35000000.00),
    (N'HD024', N'DV003', NULL, 1, 250000.00),
    (N'HD024', N'DV003', N'PT016', 1, 350000.00),
    (N'HD024', N'DV004', NULL, 1, 300000.00),
    (N'HD024', N'DV004', N'PT023', 2, 3200000.00),
    (N'HD024', N'DV003', N'PT038', 1, 25000.00),
    (N'HD025', N'DV004', NULL, 1, 300000.00),
    (N'HD025', NULL, N'PT017', 2, 1450000.00),
    (N'HD025', N'DV005', NULL, 1, 120000.00),
    (N'HD025', NULL, N'PT024', 3, 4800000.00),
    (N'HD026', N'DV005', NULL, 1, 120000.00),
    (N'HD026', N'DV005', N'PT018', 3, 950000.00),
    (N'HD026', N'DV006', NULL, 1, 650000.00),
    (N'HD026', N'DV006', N'PT025', 1, 3800000.00),
    (N'HD027', N'DV006', NULL, 1, 650000.00),
    (N'HD027', NULL, N'PT019', 1, 6500000.00),
    (N'HD027', N'DV007', NULL, 1, 200000.00),
    (N'HD027', NULL, N'PT026', 2, 8500000.00),
    (N'HD028', N'DV007', NULL, 1, 200000.00),
    (N'HD028', N'DV007', N'PT020', 2, 850000.00),
    (N'HD028', N'DV008', NULL, 1, 180000.00),
    (N'HD028', N'DV008', N'PT027', 3, 750000.00),
    (N'HD029', N'DV008', NULL, 1, 180000.00),
    (N'HD029', NULL, N'PT021', 3, 40000000.00),
    (N'HD029', N'DV009', NULL, 1, 250000.00),
    (N'HD029', NULL, N'PT028', 1, 950000.00),
    (N'HD030', N'DV009', NULL, 1, 250000.00),
    (N'HD030', N'DV009', N'PT022', 1, 35000000.00),
    (N'HD030', N'DV010', NULL, 1, 180000.00),
    (N'HD030', N'DV010', N'PT029', 2, 1850000.00);

-- LichBaoHanh: 20 bản ghi
INSERT INTO dbo.[LichBaoHanh] ([maLBH], [maPSC], [maNhanVien], [maKhachHang], [VIN], [ngayXuLy], [ketQuaXuLy], [ghiChu])
VALUES
    (N'LBH001', N'PSC041', N'NV003', N'KH041', N'JM3KF2W7XLA310041', DATEADD(minute,600,DATEADD(day,-551,@NgayMau)), N'Đã điều chỉnh và chạy thử đạt yêu cầu.', NULL),
    (N'LBH002', N'PSC042', N'NV003', N'KH042', N'MNCUMFF58JA310042', DATEADD(minute,600,DATEADD(day,-564,@NgayMau)), N'Đã thay phụ tùng theo điều kiện bảo hành.', N'Khách đã nhận thông tin kết quả xử lý.'),
    (N'LBH003', N'PSC043', N'NV003', N'KH043', N'MK2NC1W46NA310043', DATEADD(minute,600,DATEADD(day,-577,@NgayMau)), N'Đã hướng dẫn khách sử dụng và theo dõi thêm.', NULL),
    (N'LBH004', N'PSC044', N'NV003', N'KH044', N'MHYANC22XMA310044', DATEADD(minute,600,DATEADD(day,-590,@NgayMau)), N'Kiểm tra lại, hệ thống hoạt động ổn định.', N'Khách đã nhận thông tin kết quả xử lý.'),
    (N'LBH005', N'PSC045', N'NV003', N'KH045', N'MNTCB4D23KA310045', DATEADD(minute,600,DATEADD(day,-603,@NgayMau)), N'Đã điều chỉnh và chạy thử đạt yêu cầu.', NULL),
    (N'LBH006', N'PSC046', N'NV003', N'KH046', N'WDDWF4JB3JA310046', DATEADD(minute,600,DATEADD(day,-616,@NgayMau)), N'Đã thay phụ tùng theo điều kiện bảo hành.', N'Khách đã nhận thông tin kết quả xử lý.'),
    (N'LBH007', N'PSC047', N'NV003', N'KH047', N'WBA5R1109NA310047', DATEADD(minute,600,DATEADD(day,-629,@NgayMau)), N'Đã hướng dẫn khách sử dụng và theo dõi thêm.', NULL),
    (N'LBH008', N'PSC048', N'NV003', N'KH048', N'RLLV1AFA5PA310048', DATEADD(minute,600,DATEADD(day,-642,@NgayMau)), N'Kiểm tra lại, hệ thống hoạt động ổn định.', N'Khách đã nhận thông tin kết quả xử lý.'),
    (N'LBH009', N'PSC049', N'NV003', N'KH001', N'MR0BT9F3XNA310049', DATEADD(minute,600,DATEADD(day,-655,@NgayMau)), N'Đã điều chỉnh và chạy thử đạt yêu cầu.', NULL),
    (N'LBH010', N'PSC050', N'NV003', N'KH002', N'MRHGM6624KA310050', DATEADD(minute,600,DATEADD(day,-18,@NgayMau)), N'Đã thay phụ tùng theo điều kiện bảo hành.', N'Khách đã nhận thông tin kết quả xử lý.'),
    (N'LBH011', N'PSC051', N'NV003', N'KH003', N'KMHCT41B8NA310051', DATEADD(minute,600,DATEADD(day,-31,@NgayMau)), N'Đã hướng dẫn khách sử dụng và theo dõi thêm.', NULL),
    (N'LBH012', N'PSC052', N'NV003', N'KH004', N'KNDEP81A3LA310052', DATEADD(minute,600,DATEADD(day,-44,@NgayMau)), N'Kiểm tra lại, hệ thống hoạt động ổn định.', N'Khách đã nhận thông tin kết quả xử lý.'),
    (N'LBH013', N'PSC053', N'NV003', N'KH005', N'JM3KF2W74MA310053', DATEADD(minute,600,DATEADD(day,-57,@NgayMau)), N'Đã điều chỉnh và chạy thử đạt yêu cầu.', NULL),
    (N'LBH014', N'PSC054', N'NV003', N'KH006', N'MNCUMFF52KA310054', DATEADD(minute,600,DATEADD(day,-70,@NgayMau)), N'Đã thay phụ tùng theo điều kiện bảo hành.', N'Khách đã nhận thông tin kết quả xử lý.'),
    (N'LBH015', N'PSC055', N'NV003', N'KH007', N'MK2NC1W49PA310055', DATEADD(minute,600,DATEADD(day,-83,@NgayMau)), N'Đã hướng dẫn khách sử dụng và theo dõi thêm.', NULL),
    (N'LBH016', N'PSC056', N'NV003', N'KH008', N'MHYANC224NA310056', DATEADD(minute,600,DATEADD(day,-96,@NgayMau)), N'Kiểm tra lại, hệ thống hoạt động ổn định.', N'Khách đã nhận thông tin kết quả xử lý.'),
    (N'LBH017', N'PSC057', N'NV003', N'KH009', N'MNTCB4D28LA310057', DATEADD(minute,600,DATEADD(day,-109,@NgayMau)), N'Đã điều chỉnh và chạy thử đạt yêu cầu.', NULL),
    (N'LBH018', N'PSC058', N'NV003', N'KH010', N'WDDWF4JB0FA310058', DATEADD(minute,600,DATEADD(day,-122,@NgayMau)), N'Đã thay phụ tùng theo điều kiện bảo hành.', N'Khách đã nhận thông tin kết quả xử lý.'),
    (N'LBH019', N'PSC059', N'NV003', N'KH011', N'WBA5R1101PA310059', DATEADD(minute,600,DATEADD(day,-135,@NgayMau)), N'Đã hướng dẫn khách sử dụng và theo dõi thêm.', NULL),
    (N'LBH020', N'PSC060', N'NV003', N'KH012', N'RLLV1AFA2RA310060', DATEADD(minute,600,DATEADD(day,-148,@NgayMau)), N'Kiểm tra lại, hệ thống hoạt động ổn định.', N'Khách đã nhận thông tin kết quả xử lý.');

-- Gắn dữ liệu bảo hành mẫu vào một hạng mục cụ thể còn hạn trên cùng phiếu.
;WITH HangMucHopLe AS (
    SELECT l.maLBH,c.maDichVu,c.maPhuTung,
           ROW_NUMBER() OVER(PARTITION BY l.maLBH ORDER BY c.maDichVu,c.maPhuTung) AS rn
    FROM dbo.LichBaoHanh l
    JOIN dbo.ChiTietPhieuSuaChua c ON c.maPSC=l.maPSC
    WHERE c.ketQuaNghiemThu=N'Đạt' AND c.ngayBatDauBaoHanh<=l.ngayXuLy AND c.ngayHetHanBaoHanh>=l.ngayXuLy
)
UPDATE l SET maDichVu=h.maDichVu,maPhuTung=h.maPhuTung
FROM dbo.LichBaoHanh l JOIN HangMucHopLe h ON h.maLBH=l.maLBH AND h.rn=1;

-- LichNhacBaoDuong: 40 bản ghi
INSERT INTO dbo.[LichNhacBaoDuong] ([maLichNhac], [maKhachHang], [maPSC], [VIN], [ngayNhac], [noiDung], [trangThai])
VALUES
    (N'LNBD001', N'KH041', N'PSC041', N'JM3KF2W7XLA310041', DATEADD(minute,540,DATEADD(day,-548,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Không liên hệ được'),
    (N'LNBD002', N'KH041', N'PSC041', N'JM3KF2W7XLA310041', DATEADD(minute,540,DATEADD(day,-541,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Khách đặt hẹn'),
    (N'LNBD003', N'KH042', N'PSC042', N'MNCUMFF58JA310042', DATEADD(minute,540,DATEADD(day,-561,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Khách chưa có nhu cầu'),
    (N'LNBD004', N'KH042', N'PSC042', N'MNCUMFF58JA310042', DATEADD(minute,540,DATEADD(day,-554,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Đã gửi'),
    (N'LNBD005', N'KH043', N'PSC043', N'MK2NC1W46NA310043', DATEADD(minute,540,DATEADD(day,35,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Chưa gửi'),
    (N'LNBD006', N'KH043', N'PSC043', N'MK2NC1W46NA310043', DATEADD(minute,540,DATEADD(day,-567,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Khách đặt hẹn'),
    (N'LNBD007', N'KH044', N'PSC044', N'MHYANC22XMA310044', DATEADD(minute,540,DATEADD(day,-587,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Khách chưa có nhu cầu'),
    (N'LNBD008', N'KH044', N'PSC044', N'MHYANC22XMA310044', DATEADD(minute,540,DATEADD(day,-580,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Đã gửi'),
    (N'LNBD009', N'KH045', N'PSC045', N'MNTCB4D23KA310045', DATEADD(minute,540,DATEADD(day,-600,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Không liên hệ được'),
    (N'LNBD010', N'KH045', N'PSC045', N'MNTCB4D23KA310045', DATEADD(minute,540,DATEADD(day,40,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Chưa gửi'),
    (N'LNBD011', N'KH046', N'PSC046', N'WDDWF4JB3JA310046', DATEADD(minute,540,DATEADD(day,-613,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Khách chưa có nhu cầu'),
    (N'LNBD012', N'KH046', N'PSC046', N'WDDWF4JB3JA310046', DATEADD(minute,540,DATEADD(day,-606,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Đã gửi'),
    (N'LNBD013', N'KH047', N'PSC047', N'WBA5R1109NA310047', DATEADD(minute,540,DATEADD(day,-626,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Không liên hệ được'),
    (N'LNBD014', N'KH047', N'PSC047', N'WBA5R1109NA310047', DATEADD(minute,540,DATEADD(day,-619,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Khách đặt hẹn'),
    (N'LNBD015', N'KH048', N'PSC048', N'RLLV1AFA5PA310048', DATEADD(minute,540,DATEADD(day,45,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Chưa gửi'),
    (N'LNBD016', N'KH048', N'PSC048', N'RLLV1AFA5PA310048', DATEADD(minute,540,DATEADD(day,-632,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Đã gửi'),
    (N'LNBD017', N'KH001', N'PSC049', N'MR0BT9F3XNA310049', DATEADD(minute,540,DATEADD(day,-652,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Không liên hệ được'),
    (N'LNBD018', N'KH001', N'PSC049', N'MR0BT9F3XNA310049', DATEADD(minute,540,DATEADD(day,-645,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Khách đặt hẹn'),
    (N'LNBD019', N'KH002', N'PSC050', N'MRHGM6624KA310050', DATEADD(minute,540,DATEADD(day,-15,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Khách chưa có nhu cầu'),
    (N'LNBD020', N'KH002', N'PSC050', N'MRHGM6624KA310050', DATEADD(minute,540,DATEADD(day,50,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Chưa gửi'),
    (N'LNBD021', N'KH003', N'PSC051', N'KMHCT41B8NA310051', DATEADD(minute,540,DATEADD(day,-28,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Không liên hệ được'),
    (N'LNBD022', N'KH003', N'PSC051', N'KMHCT41B8NA310051', DATEADD(minute,540,DATEADD(day,-21,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Khách đặt hẹn'),
    (N'LNBD023', N'KH004', N'PSC052', N'KNDEP81A3LA310052', DATEADD(minute,540,DATEADD(day,-41,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Khách chưa có nhu cầu'),
    (N'LNBD024', N'KH004', N'PSC052', N'KNDEP81A3LA310052', DATEADD(minute,540,DATEADD(day,-34,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Đã gửi'),
    (N'LNBD025', N'KH005', N'PSC053', N'JM3KF2W74MA310053', DATEADD(minute,540,DATEADD(day,55,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Chưa gửi'),
    (N'LNBD026', N'KH005', N'PSC053', N'JM3KF2W74MA310053', DATEADD(minute,540,DATEADD(day,-47,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Khách đặt hẹn'),
    (N'LNBD027', N'KH006', N'PSC054', N'MNCUMFF52KA310054', DATEADD(minute,540,DATEADD(day,-67,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Khách chưa có nhu cầu'),
    (N'LNBD028', N'KH006', N'PSC054', N'MNCUMFF52KA310054', DATEADD(minute,540,DATEADD(day,-60,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Đã gửi'),
    (N'LNBD029', N'KH007', N'PSC055', N'MK2NC1W49PA310055', DATEADD(minute,540,DATEADD(day,-80,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Không liên hệ được'),
    (N'LNBD030', N'KH007', N'PSC055', N'MK2NC1W49PA310055', DATEADD(minute,540,DATEADD(day,60,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Chưa gửi'),
    (N'LNBD031', N'KH008', N'PSC056', N'MHYANC224NA310056', DATEADD(minute,540,DATEADD(day,-93,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Khách chưa có nhu cầu'),
    (N'LNBD032', N'KH008', N'PSC056', N'MHYANC224NA310056', DATEADD(minute,540,DATEADD(day,-86,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Đã gửi'),
    (N'LNBD033', N'KH009', N'PSC057', N'MNTCB4D28LA310057', DATEADD(minute,540,DATEADD(day,-106,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Không liên hệ được'),
    (N'LNBD034', N'KH009', N'PSC057', N'MNTCB4D28LA310057', DATEADD(minute,540,DATEADD(day,-99,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Khách đặt hẹn'),
    (N'LNBD035', N'KH010', N'PSC058', N'WDDWF4JB0FA310058', DATEADD(minute,540,DATEADD(day,65,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Chưa gửi'),
    (N'LNBD036', N'KH010', N'PSC058', N'WDDWF4JB0FA310058', DATEADD(minute,540,DATEADD(day,-112,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Đã gửi'),
    (N'LNBD037', N'KH011', N'PSC059', N'WBA5R1101PA310059', DATEADD(minute,540,DATEADD(day,-132,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Không liên hệ được'),
    (N'LNBD038', N'KH011', N'PSC059', N'WBA5R1101PA310059', DATEADD(minute,540,DATEADD(day,-125,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Khách đặt hẹn'),
    (N'LNBD039', N'KH012', N'PSC060', N'RLLV1AFA2RA310060', DATEADD(minute,540,DATEADD(day,-145,@NgayMau)), N'Nhắc bảo dưỡng lần 1: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Khách chưa có nhu cầu'),
    (N'LNBD040', N'KH012', N'PSC060', N'RLLV1AFA2RA310060', DATEADD(minute,540,DATEADD(day,70,@NgayMau)), N'Nhắc bảo dưỡng lần 2: kiểm tra dầu, phanh, lốp và các hạng mục đã sửa.', N'Chưa gửi');

-- LichSuTichDiem: 20 bản ghi
INSERT INTO dbo.[LichSuTichDiem] ([maLichSuTD], [maKhachHang], [maHoaDon], [ngayTichDiem], [soDiem], [lyDo])
VALUES
    (N'LS001', N'KH041', N'HD011', DATEADD(minute,735,DATEADD(day,-553,@NgayMau)), 72, N'Tích điểm từ hóa đơn HD011; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS002', N'KH042', N'HD012', DATEADD(minute,735,DATEADD(day,-566,@NgayMau)), 35, N'Tích điểm từ hóa đơn HD012; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS003', N'KH043', N'HD013', DATEADD(minute,735,DATEADD(day,-579,@NgayMau)), 135, N'Tích điểm từ hóa đơn HD013; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS004', N'KH044', N'HD014', DATEADD(minute,735,DATEADD(day,-592,@NgayMau)), 31, N'Tích điểm từ hóa đơn HD014; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS005', N'KH045', N'HD015', DATEADD(minute,735,DATEADD(day,-605,@NgayMau)), 21, N'Tích điểm từ hóa đơn HD015; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS006', N'KH046', N'HD016', DATEADD(minute,735,DATEADD(day,-618,@NgayMau)), 31, N'Tích điểm từ hóa đơn HD016; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS007', N'KH047', N'HD017', DATEADD(minute,735,DATEADD(day,-631,@NgayMau)), 103, N'Tích điểm từ hóa đơn HD017; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS008', N'KH048', N'HD018', DATEADD(minute,735,DATEADD(day,-644,@NgayMau)), 216, N'Tích điểm từ hóa đơn HD018; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS009', N'KH001', N'HD019', DATEADD(minute,735,DATEADD(day,-657,@NgayMau)), 279, N'Tích điểm từ hóa đơn HD019; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS010', N'KH002', N'HD020', DATEADD(minute,735,DATEADD(day,-20,@NgayMau)), 252, N'Tích điểm từ hóa đơn HD020; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS011', N'KH003', N'HD021', DATEADD(minute,735,DATEADD(day,-33,@NgayMau)), 0, N'Tích điểm từ hóa đơn HD021; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS012', N'KH004', N'HD022', DATEADD(minute,735,DATEADD(day,-46,@NgayMau)), 0, N'Tích điểm từ hóa đơn HD022; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS013', N'KH005', N'HD023', DATEADD(minute,735,DATEADD(day,-59,@NgayMau)), 397, N'Tích điểm từ hóa đơn HD023; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS014', N'KH006', N'HD024', DATEADD(minute,735,DATEADD(day,-72,@NgayMau)), 80, N'Tích điểm từ hóa đơn HD024; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS015', N'KH007', N'HD025', DATEADD(minute,735,DATEADD(day,-85,@NgayMau)), 191, N'Tích điểm từ hóa đơn HD025; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS016', N'KH008', N'HD026', DATEADD(minute,735,DATEADD(day,-98,@NgayMau)), 80, N'Tích điểm từ hóa đơn HD026; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS017', N'KH009', N'HD027', DATEADD(minute,735,DATEADD(day,-111,@NgayMau)), 264, N'Tích điểm từ hóa đơn HD027; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS018', N'KH010', N'HD028', DATEADD(minute,735,DATEADD(day,-124,@NgayMau)), 46, N'Tích điểm từ hóa đơn HD028; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS019', N'KH011', N'HD029', DATEADD(minute,735,DATEADD(day,-137,@NgayMau)), 1310, N'Tích điểm từ hóa đơn HD029; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.'),
    (N'LS020', N'KH012', N'HD030', DATEADD(minute,735,DATEADD(day,-150,@NgayMau)), 423, N'Tích điểm từ hóa đơn HD030; giá trị mẫu trước giảm, làm tròn xuống. Đây là quy ước dữ liệu mẫu.');

UPDATE historyRow
SET ngayHetHan=DATEADD(year,1,cycleStart.firstEarn)
FROM dbo.LichSuTichDiem historyRow
CROSS APPLY(SELECT MIN(ngayTichDiem) AS firstEarn FROM dbo.LichSuTichDiem WHERE maKhachHang=historyRow.maKhachHang) cycleStart
WHERE historyRow.ngayHetHan IS NULL;

UPDATE q
SET maNhanVien=CASE (CONVERT(int,RIGHT(q.maBaoGia,3))-1)%4
    WHEN 1 THEN 'NV009' WHEN 2 THEN 'NV010' WHEN 3 THEN 'NV011' ELSE 'NV003' END
FROM dbo.PhieuBaoGia q;
UPDATE k
SET maNhanVien=CASE (CONVERT(int,RIGHT(q.maBaoGia,3))-1)%4
    WHEN 1 THEN 'NV009' WHEN 2 THEN 'NV010' WHEN 3 THEN 'NV011' ELSE 'NV003' END
FROM dbo.PhieuKiemTra k
JOIN dbo.PhieuBaoGia q ON q.maPKT=k.maPKT;
UPDATE p
SET maNhanVien=q.maNhanVien
FROM dbo.PhieuSuaChua p
JOIN dbo.PhieuBaoGia q ON q.maBaoGia=p.maBaoGia;
UPDATE l
SET maNhanVien=p.maNhanVien
FROM dbo.LichBaoHanh l
JOIN dbo.PhieuSuaChua p ON p.maPSC=l.maPSC;





        COMMIT TRANSACTION;
        PRINT N'[SEED] Nạp bộ dữ liệu mẫu cơ sở thành công.';
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END
ELSE
BEGIN
    PRINT N'[SEED] Dữ liệu mẫu cơ sở đã tồn tại, không nạp lại để bảo toàn dữ liệu hiện có.';
END

IF EXISTS (SELECT 1 FROM dbo.PhuongTien WHERE maKhachHang IS NULL)
    THROW 51301, N'Còn phương tiện chưa được gán chủ sở hữu. Hãy gán khách hàng cho xe trước khi tiếp tục.', 1;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID(N'dbo.PhuongTien') AND name=N'maKhachHang' AND is_nullable=1)
BEGIN
    IF EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.PhuongTien') AND name=N'IX_PhuongTien_KhachHang')
        DROP INDEX [IX_PhuongTien_KhachHang] ON dbo.PhuongTien;
    ALTER TABLE dbo.PhuongTien ALTER COLUMN maKhachHang varchar(5) NOT NULL;
END;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID(N'dbo.PhuongTien') AND name=N'IX_PhuongTien_KhachHang')
    CREATE INDEX [IX_PhuongTien_KhachHang] ON dbo.PhuongTien (maKhachHang);
GO


-- Mã giảm giá thưởng điểm; coupon thường MGG001..MGG036 không thuộc chu kỳ tích điểm.
;WITH ChuKyDiem AS (
    SELECT maKhachHang, MIN(ngayTichDiem) AS ngayBatDauChuKy
    FROM dbo.LichSuTichDiem
    WHERE ngayHetHan >= SYSDATETIME()
    GROUP BY maKhachHang
), TongDiemChuKy AS (
    SELECT c.maKhachHang, c.ngayBatDauChuKy, SUM(CONVERT(bigint, l.soDiem)) AS tongDiem
    FROM ChuKyDiem c
    JOIN dbo.LichSuTichDiem l ON l.maKhachHang=c.maKhachHang
        AND l.ngayTichDiem>=c.ngayBatDauChuKy
        AND l.ngayHetHan>=SYSDATETIME()
    GROUP BY c.maKhachHang, c.ngayBatDauChuKy
), MocThuong AS (
    SELECT mocDiem, mucGiam
    FROM (VALUES (50,5.00),(100,10.00),(200,15.00),(500,30.00)) v(mocDiem,mucGiam)
)
INSERT INTO dbo.[MaGiamGia]
    ([maKhachHang],[mucGiam],[ngayTao],[ngayHetHan],[trangThai],[mocDiem],[ngayBatDauChuKy])
SELECT t.maKhachHang, m.mucGiam, t.ngayBatDauChuKy,
       DATEADD(year,1,t.ngayBatDauChuKy), N'Đang hiệu lực', m.mocDiem, t.ngayBatDauChuKy
FROM TongDiemChuKy t
CROSS JOIN MocThuong m
WHERE t.tongDiem>=m.mocDiem
  AND NOT EXISTS (
      SELECT 1 FROM dbo.MaGiamGia g
      WHERE g.maKhachHang=t.maKhachHang
        AND g.ngayBatDauChuKy=t.ngayBatDauChuKy
        AND g.mocDiem=m.mocDiem
  );

-- ============================================================================
-- D.1.1. BỔ SUNG DỮ LIỆU HẠNG MỤC SỬA CHỮA VÀ CẤP PHÁT (Idempotent)
-- ============================================================================
INSERT INTO dbo.ChiTietPhieuSuaChua(maPSC,maDichVu,maPhuTung,trangThaiHangMuc,soLuongPhuTung,ketQuaNghiemThu,ngayBatDauBaoHanh,thoiHanBaoHanh,ngayHetHanBaoHanh)
SELECT p.maPSC,c.maDichVu,c.maPhuTung,
       CASE WHEN c.maPhuTung='PT000' THEN N'Đang sửa chữa' WHEN c.trangThai LIKE N'Thiếu phụ tùng%' THEN N'Thiếu phụ tùng' ELSE N'Chờ sửa chữa' END,
       c.soLuongPhuTung,N'Không đạt',NULL,0,NULL
FROM dbo.PhieuSuaChua p JOIN dbo.ChiTietPhieuBaoGia c ON c.maBaoGia=p.maBaoGia
WHERE p.trangThai IN (N'Chờ sửa chữa',N'Đã hủy')
  AND NOT EXISTS(SELECT 1 FROM dbo.ChiTietPhieuSuaChua d
                  WHERE d.maPSC=p.maPSC AND d.maDichVu=c.maDichVu AND d.maPhuTung=c.maPhuTung);

-- Đồng bộ dữ liệu hạng mục với tiến độ phiếu; nhân viên kho ghi nhận các lần cấp phát/hoàn trả.
UPDATE d
SET d.trangThaiHangMuc=N'Đang sửa chữa',
    d.soLuongCapPhat=d.soLuongPhuTung,
    d.soLuongSuDung=0,
    d.soLuongHoanTra=0,
    d.ngayCapPhat=CASE WHEN d.soLuongPhuTung>0 THEN DATEADD(minute,600,CONVERT(datetime2(0),CONVERT(date,p.ngayLap))) END,
    d.maNhanVienCapPhat=CASE WHEN d.soLuongPhuTung>0 AND CONVERT(int,RIGHT(d.maPSC,3))%2=0 THEN 'NV012'
                             WHEN d.soLuongPhuTung>0 THEN 'NV013' END,
    d.ngayHoanTra=NULL,
    d.maNhanVienXacNhanHoanTra=NULL
FROM dbo.ChiTietPhieuSuaChua d JOIN dbo.PhieuSuaChua p ON p.maPSC=d.maPSC
WHERE p.trangThai=N'Đang sửa chữa';

UPDATE d
SET d.trangThaiHangMuc=N'Hoàn thành',
    d.soLuongCapPhat=d.soLuongPhuTung,
    d.soLuongHoanTra=CASE WHEN d.maPhuTung<>'PT000' AND d.soLuongPhuTung>1
                               AND CONVERT(int,RIGHT(d.maPSC,3))%4=0 THEN 1 ELSE 0 END,
    d.soLuongSuDung=d.soLuongPhuTung-CASE WHEN d.maPhuTung<>'PT000' AND d.soLuongPhuTung>1
                                              AND CONVERT(int,RIGHT(d.maPSC,3))%4=0 THEN 1 ELSE 0 END,
    d.ngayCapPhat=CASE WHEN d.soLuongPhuTung>0 THEN DATEADD(minute,600,CONVERT(datetime2(0),CONVERT(date,p.ngayLap))) END,
    d.maNhanVienCapPhat=CASE WHEN d.soLuongPhuTung>0 AND CONVERT(int,RIGHT(d.maPSC,3))%2=0 THEN 'NV012'
                             WHEN d.soLuongPhuTung>0 THEN 'NV013' END,
    d.ngayHoanTra=CASE WHEN d.maPhuTung<>'PT000' AND d.soLuongPhuTung>1
                            AND CONVERT(int,RIGHT(d.maPSC,3))%4=0 THEN DATEADD(minute,690,CONVERT(datetime2(0),CONVERT(date,p.ngayHoanThanh))) END,
    d.maNhanVienXacNhanHoanTra=CASE WHEN d.maPhuTung<>'PT000' AND d.soLuongPhuTung>1
                                         AND CONVERT(int,RIGHT(d.maPSC,3))%4=0 THEN 'NV012' END
FROM dbo.ChiTietPhieuSuaChua d JOIN dbo.PhieuSuaChua p ON p.maPSC=d.maPSC
WHERE p.trangThai IN (N'Đã sửa xong',N'Chờ thanh toán',N'Đã thanh toán',N'Hoàn tất');

-- Phiếu hủy đã được quản lý duyệt; không cấp phát phụ tùng cho các phiếu này.
UPDATE dbo.PhieuSuaChua
SET maNguoiDuyetHuy='NV004',
    lyDoHuy=CASE (CONVERT(int,RIGHT(maPSC,3))-61)%5
        WHEN 0 THEN N'Khách hàng không còn nhu cầu sửa chữa.'
        WHEN 1 THEN N'Khách hàng yêu cầu hủy trước khi bắt đầu sửa chữa.'
        WHEN 2 THEN N'Khách hàng chưa xác nhận phương án sửa chữa.'
        WHEN 3 THEN N'Khách hàng đổi lịch và lập phiếu sửa chữa mới.'
        ELSE N'Không thống nhất được thời gian tiếp nhận xe.' END
WHERE trangThai=N'Đã hủy';

-- D.2. DỮ LIỆU KỊCH BẢN UC009 & ĐỒNG BỘ SEQUENCE (Idempotent)
-- ============================================================================
DECLARE @NgayMau datetime2(0) = CONVERT(date, SYSDATETIME());

-- 1. Nạp riêng từng yêu cầu và chi tiết còn thiếu để có thể chạy lại an toàn
INSERT INTO dbo.[YeuCauNhapKho] ([maYCNK], [maNCC], [maNhanVien], [ngayTao], [trangThai], [maNhanVienDuyet], [ngayDuyet], [ghiChu])
SELECT v.maYCNK, v.maNCC, v.maNhanVien, v.ngayTao, v.trangThai, v.maNhanVienDuyet, v.ngayDuyet, v.ghiChu
FROM (VALUES
    (N'YCN001', N'NCC001', N'NV002', DATEADD(minute,480,DATEADD(day,-1,@NgayMau)), N'Chờ duyệt', NULL, NULL, N'Yêu cầu nhập bổ sung dầu động cơ và lọc nhớt quý 4'),
    (N'YCN002', N'NCC002', N'NV002', DATEADD(minute,500,DATEADD(day,-2,@NgayMau)), N'Đã duyệt', N'NV004', DATEADD(minute,540,DATEADD(day,-1,@NgayMau)), N'Yêu cầu nhập má phanh phục vụ bảo dưỡng định kỳ'),
    (N'YCN003', N'NCC001', N'NV002', DATEADD(minute,520,DATEADD(day,-3,@NgayMau)), N'Đã duyệt', N'NV004', DATEADD(minute,560,DATEADD(day,-2,@NgayMau)), N'Yêu cầu nhập phụ tùng lọc gió điều hòa và bugi'),
    (N'YCN004', N'NCC003', N'NV002', DATEADD(minute,480,DATEADD(day,-5,@NgayMau)), N'Đã xử lý', N'NV004', DATEADD(minute,520,DATEADD(day,-4,@NgayMau)), N'Nhập phụ tùng dầu động cơ đầu tháng'),
    (N'YCN005', N'NCC002', N'NV002', DATEADD(minute,500,DATEADD(day,-6,@NgayMau)), N'Từ chối', N'NV004', DATEADD(minute,540,DATEADD(day,-5,@NgayMau)), N'Yêu cầu nhập má phanh ngoài danh mục tiêu chuẩn (bị từ chối)'),
    (N'YCN006', N'NCC004', N'NV012', DATEADD(minute,510,DATEADD(day,-1,@NgayMau)), N'Chờ duyệt', NULL, NULL, N'Bổ sung lọc gió động cơ và bugi cho đợt bảo dưỡng'),
    (N'YCN007', N'NCC005', N'NV013', DATEADD(minute,515,DATEADD(day,-3,@NgayMau)), N'Đã duyệt', N'NV004', DATEADD(minute,550,DATEADD(day,-2,@NgayMau)), N'Đã duyệt nhập má phanh và dầu phanh theo kế hoạch'),
    (N'YCN008', N'NCC006', N'NV012', DATEADD(minute,520,DATEADD(day,-4,@NgayMau)), N'Từ chối', N'NV004', DATEADD(minute,560,DATEADD(day,-3,@NgayMau)), N'Từ chối do nhà cung cấp báo giá vượt ngân sách'),
    (N'YCN009', N'NCC007', N'NV013', DATEADD(minute,525,DATEADD(day,-7,@NgayMau)), N'Đã xử lý', N'NV004', DATEADD(minute,565,DATEADD(day,-6,@NgayMau)), N'Đã nhận đủ phụ tùng theo yêu cầu'),
    (N'YCN010', N'NCC008', N'NV012', DATEADD(minute,530,DATEADD(day,-2,@NgayMau)), N'Đã duyệt', N'NV004', DATEADD(minute,570,DATEADD(day,-1,@NgayMau)), N'Đã duyệt nhập lọc dầu cho đợt bảo dưỡng định kỳ')
) AS v(maYCNK, maNCC, maNhanVien, ngayTao, trangThai, maNhanVienDuyet, ngayDuyet, ghiChu)
WHERE NOT EXISTS (SELECT 1 FROM dbo.YeuCauNhapKho r WHERE r.maYCNK=v.maYCNK);

INSERT INTO dbo.[ChiTietYeuCauNhapKho] ([maYCNK], [maPhuTung], [soLuongYeuCau], [donGiaDuKien])
SELECT v.maYCNK, v.maPhuTung, v.soLuongYeuCau, v.donGiaDuKien
FROM (VALUES
    (N'YCN001', N'PT001', 20, 120000.00),
    (N'YCN001', N'PT002', 15, 85000.00),
    (N'YCN002', N'PT003', 10, 350000.00),
    (N'YCN003', N'PT004', 25, 110000.00),
    (N'YCN003', N'PT005', 30, 95000.00),
    (N'YCN004', N'PT001', 10, 120000.00),
    (N'YCN005', N'PT003', 50, 350000.00),
    (N'YCN006', N'PT006', 12, 90000.00),
    (N'YCN006', N'PT007', 8, 390000.00),
    (N'YCN007', N'PT008', 16, 150000.00),
    (N'YCN008', N'PT009', 6, 1380000.00),
    (N'YCN009', N'PT010', 9, 2100000.00),
    (N'YCN010', N'PT011', 14, 750000.00)
) AS v(maYCNK, maPhuTung, soLuongYeuCau, donGiaDuKien)
WHERE NOT EXISTS (SELECT 1 FROM dbo.ChiTietYeuCauNhapKho d WHERE d.maYCNK=v.maYCNK AND d.maPhuTung=v.maPhuTung);
PRINT N'[SEED] Đồng bộ đủ 10 yêu cầu và 13 dòng chi tiết nhập kho mẫu.';

-- 2. Bổ sung độc lập các phiếu nhập kho liên kết để chạy lại vẫn đủ từng trạng thái UC009
DECLARE @MaxPNK int = 0, @PnkId varchar(6);
SELECT @MaxPNK = ISNULL(MAX(TRY_CONVERT(int, SUBSTRING(maPNK, 4, 3))), 0)
FROM dbo.PhieuNhapKho WHERE maPNK LIKE 'PNK[0-9][0-9][0-9]';

IF NOT EXISTS (SELECT 1 FROM dbo.PhieuNhapKho WHERE maYCNK = N'YCN003')
BEGIN
    SET @MaxPNK += 1;
    SET @PnkId = 'PNK' + RIGHT('000' + CONVERT(varchar(3), @MaxPNK), 3);
    INSERT INTO dbo.[PhieuNhapKho] ([maPNK], [maNCC], [maNhanVien], [ngayNhap], [trangThai], [maYCNK])
    VALUES (@PnkId, N'NCC001', N'NV012', DATEADD(minute,600,DATEADD(day,-1,@NgayMau)), N'Chờ xác nhận nhập kho', N'YCN003');
    INSERT INTO dbo.[ChiTietPhieuNhapKho] ([maPNK], [maPhuTung], [soLuongYeuCau], [soLuongThucNhan], [soLuongNhap], [donGiaNhap])
    VALUES (@PnkId, N'PT004', 25, 25, 23, 110000.00), (@PnkId, N'PT005', 30, 28, 28, 95000.00);
END;

IF NOT EXISTS (SELECT 1 FROM dbo.PhieuNhapKho WHERE maYCNK = N'YCN010')
BEGIN
    SET @MaxPNK += 1;
    SET @PnkId = 'PNK' + RIGHT('000' + CONVERT(varchar(3), @MaxPNK), 3);
    INSERT INTO dbo.[PhieuNhapKho] ([maPNK], [maNCC], [maNhanVien], [ngayNhap], [trangThai], [maYCNK])
    VALUES (@PnkId, N'NCC008', N'NV012', DATEADD(minute,600,DATEADD(day,-1,@NgayMau)), N'Chờ xác nhận nhập kho', N'YCN010');
    INSERT INTO dbo.[ChiTietPhieuNhapKho] ([maPNK], [maPhuTung], [soLuongYeuCau], [soLuongThucNhan], [soLuongNhap], [donGiaNhap])
    VALUES (@PnkId, N'PT011', 14, 14, 13, 750000.00);
END;

IF NOT EXISTS (SELECT 1 FROM dbo.PhieuNhapKho WHERE maYCNK = N'YCN004')
BEGIN
    SET @MaxPNK += 1;
    SET @PnkId = 'PNK' + RIGHT('000' + CONVERT(varchar(3), @MaxPNK), 3);
    INSERT INTO dbo.[PhieuNhapKho] ([maPNK], [maNCC], [maNhanVien], [ngayNhap], [trangThai], [maYCNK])
    VALUES (@PnkId, N'NCC003', N'NV013', DATEADD(minute,600,DATEADD(day,-4,@NgayMau)), N'Đã nhập kho', N'YCN004');
    INSERT INTO dbo.[ChiTietPhieuNhapKho] ([maPNK], [maPhuTung], [soLuongYeuCau], [soLuongThucNhan], [soLuongNhap], [donGiaNhap])
    VALUES (@PnkId, N'PT001', 10, 10, 10, 120000.00);
END;

IF NOT EXISTS (SELECT 1 FROM dbo.PhieuNhapKho WHERE maYCNK = N'YCN007')
BEGIN
    SET @MaxPNK += 1;
    SET @PnkId = 'PNK' + RIGHT('000' + CONVERT(varchar(3), @MaxPNK), 3);
    INSERT INTO dbo.[PhieuNhapKho] ([maPNK], [maNCC], [maNhanVien], [ngayNhap], [trangThai], [maYCNK])
    VALUES (@PnkId, N'NCC005', N'NV013', DATEADD(minute,600,DATEADD(day,-2,@NgayMau)), N'Đã hủy', N'YCN007');
END;

IF NOT EXISTS (SELECT 1 FROM dbo.PhieuNhapKho WHERE maYCNK = N'YCN009')
BEGIN
    SET @MaxPNK += 1;
    SET @PnkId = 'PNK' + RIGHT('000' + CONVERT(varchar(3), @MaxPNK), 3);
    INSERT INTO dbo.[PhieuNhapKho] ([maPNK], [maNCC], [maNhanVien], [ngayNhap], [trangThai], [maYCNK])
    VALUES (@PnkId, N'NCC007', N'NV013', DATEADD(minute,600,DATEADD(day,-6,@NgayMau)), N'Đã nhập kho', N'YCN009');
    INSERT INTO dbo.[ChiTietPhieuNhapKho] ([maPNK], [maPhuTung], [soLuongYeuCau], [soLuongThucNhan], [soLuongNhap], [donGiaNhap])
    VALUES (@PnkId, N'PT010', 9, 9, 9, 2100000.00);
END;
PRINT N'[SEED] Đảm bảo có phiếu nhập kho chờ xác nhận, đã nhập kho và đã hủy.';

-- 3. Đồng bộ lại Sequences theo MAX thực tế
DECLARE @CurMaxPNK int = 0;
SELECT @CurMaxPNK = ISNULL(MAX(CONVERT(int, SUBSTRING(maPNK, 4, 3))), 0)
FROM dbo.PhieuNhapKho WHERE maPNK LIKE 'PNK[0-9][0-9][0-9]';
DECLARE @NextValPNK int = CASE WHEN @CurMaxPNK > 0 THEN @CurMaxPNK + 1 ELSE 1 END;
DECLARE @SqlSeqPNK nvarchar(500) = N'ALTER SEQUENCE dbo.Seq_PhieuNhapKho RESTART WITH ' + CONVERT(nvarchar(10), @NextValPNK) + N';';
EXEC sp_executesql @SqlSeqPNK;

DECLARE @CurMaxYCN int = 0;
SELECT @CurMaxYCN = ISNULL(MAX(CONVERT(int, SUBSTRING(maYCNK, 4, 3))), 0)
FROM dbo.YeuCauNhapKho WHERE maYCNK LIKE 'YCN[0-9][0-9][0-9]';
DECLARE @NextValYCN int = CASE WHEN @CurMaxYCN > 0 THEN @CurMaxYCN + 1 ELSE 1 END;
DECLARE @SqlSeqYCN nvarchar(500) = N'ALTER SEQUENCE dbo.Seq_YeuCauNhapKho RESTART WITH ' + CONVERT(nvarchar(10), @NextValYCN) + N';';
EXEC sp_executesql @SqlSeqYCN;
GO


-- ============================================================================
-- D.3. PHÂN BỔ NHÂN SỰ TRÊN DỮ LIỆU MẪU
-- Chạy sau khi seed cơ sở và UC009 đã được nạp; chỉ chuẩn hóa các mã mẫu.
UPDATE dbo.PhieuTiepNhan
SET maNhanVien=CASE (CONVERT(int,RIGHT(maPTN,3))-1)%4
    WHEN 1 THEN 'NV006' WHEN 2 THEN 'NV007' WHEN 3 THEN 'NV008' ELSE 'NV001' END
WHERE maPTN BETWEEN 'PTN001' AND 'PTN100' AND maNhanVien='NV001';

-- Cân doanh thu hóa đơn dịch vụ theo giá trị, giữ nguyên số tiền và trạng thái hóa đơn.
UPDATE h SET h.maNhanVien=v.maNhanVien
FROM dbo.HoaDon h JOIN (VALUES
    ('HD001','NV006'),('HD002','NV007'),('HD003','NV008'),('HD004','NV001'),('HD005','NV006'),
    ('HD006','NV007'),('HD007','NV008'),('HD008','NV001'),('HD009','NV006'),('HD010','NV007'),
    ('HD011','NV007'),('HD012','NV008'),('HD013','NV007'),('HD014','NV007'),('HD015','NV008'),
    ('HD016','NV001'),('HD017','NV008'),('HD018','NV007'),('HD019','NV001'),('HD020','NV008'),
    ('HD021','NV001'),('HD022','NV001'),('HD023','NV008'),('HD024','NV001'),('HD025','NV001'),
    ('HD026','NV008'),('HD027','NV001'),('HD028','NV001'),('HD029','NV006'),('HD030','NV007')
) v(maHoaDon,maNhanVien) ON v.maHoaDon=h.maHoaDon
WHERE h.maNhanVien='NV001';

-- Hóa đơn bán lẻ được chia vòng đều; dữ liệu HD031..HD042 mới cũng dùng cùng phân bổ.
UPDATE h SET h.maNhanVien=v.maNhanVien
FROM dbo.HoaDon h JOIN (VALUES
    ('HD031','NV001'),('HD032','NV006'),('HD033','NV007'),('HD034','NV008'),
    ('HD035','NV001'),('HD036','NV006'),('HD037','NV007'),('HD038','NV008'),
    ('HD039','NV001'),('HD040','NV006'),('HD041','NV007'),('HD042','NV008')
) v(maHoaDon,maNhanVien) ON v.maHoaDon=h.maHoaDon
WHERE h.maNhanVien='NV001';

UPDATE dbo.YeuCauNhapKho
SET maNhanVien=CASE (CONVERT(int,RIGHT(maYCNK,3))-1)%3
    WHEN 1 THEN 'NV012' WHEN 2 THEN 'NV013' ELSE 'NV002' END
WHERE maYCNK BETWEEN 'YCN001' AND 'YCN005' AND maNhanVien='NV002';

UPDATE dbo.PhieuNhapKho
SET maNhanVien=CASE (CONVERT(int,RIGHT(maPNK,3))-1)%3
    WHEN 1 THEN 'NV012' WHEN 2 THEN 'NV013' ELSE 'NV002' END
WHERE maPNK BETWEEN 'PNK001' AND 'PNK036' AND maNhanVien='NV002';
GO
-- ============================================================================
-- D.4. BẢO ĐẢM SCHEMA VÀ NẠP HÓA ĐƠN BÁN LẺ MẪU
-- ============================================================================
-- Schema hóa đơn dịch vụ/bán lẻ và dữ liệu HD031..HD042
-- Nâng cấp tại chỗ, không xóa database và không thay đổi hóa đơn cũ.
-- Chạy trên database đã chọn trong SSMS; sao lưu trước khi nâng cấp.
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
GO
BEGIN TRY
    BEGIN TRANSACTION;
    IF OBJECT_ID(N'dbo.HoaDon',N'U') IS NULL THROW 51100,N'Chưa có bảng HoaDon trong database đang chọn.',1;
    IF EXISTS(SELECT 1 FROM dbo.HoaDon WHERE
       (maPSC IS NOT NULL AND VIN IS NULL) OR
       (maPSC IS NULL AND (VIN IS NOT NULL OR tienCong<>0 OR dichVuKhac<>0 OR COALESCE(giamGia,0)<>0 OR maGiamGia IS NOT NULL)))
        THROW 51101,N'Hóa đơn cũ vi phạm quy tắc hai loại; kiểm tra dữ liệu trước khi nâng cấp, script không tự sửa.',1;
    IF EXISTS(SELECT 1 FROM dbo.ChiTietHoaDon c JOIN dbo.HoaDon h ON h.maHoaDon=c.maHoaDon
       WHERE h.maPSC IS NULL AND (c.maDichVu IS NOT NULL OR c.maPhuTung IS NULL OR c.soLuong<=0))
        THROW 51102,N'Hóa đơn bán hàng cũ có chi tiết dịch vụ hoặc số lượng không hợp lệ.',1;
    IF EXISTS(SELECT 1 FROM dbo.LichSuTichDiem l JOIN dbo.HoaDon h ON h.maHoaDon=l.maHoaDon WHERE h.maPSC IS NULL)
        THROW 51103,N'Có lịch sử tích điểm cho hóa đơn bán hàng; cần rà soát trước khi nâng cấp.',1;
    ALTER TABLE dbo.HoaDon ALTER COLUMN VIN varchar(17) NULL;
    IF OBJECT_ID(N'dbo.CK_HoaDon_HaiLoai',N'C') IS NULL
        ALTER TABLE dbo.HoaDon WITH CHECK ADD CONSTRAINT CK_HoaDon_HaiLoai CHECK (
           (maPSC IS NOT NULL AND VIN IS NOT NULL) OR
           (maPSC IS NULL AND VIN IS NULL AND tienCong=0 AND dichVuKhac=0 AND COALESCE(giamGia,0)=0 AND maGiamGia IS NULL));
    -- FK ghép với maPSC không kiểm tra khách hàng khi maPSC=NULL.
    IF NOT EXISTS(SELECT 1 FROM sys.foreign_keys WHERE parent_object_id=OBJECT_ID(N'dbo.HoaDon') AND referenced_object_id=OBJECT_ID(N'dbo.KhachHang'))
        ALTER TABLE dbo.HoaDon WITH CHECK ADD CONSTRAINT FK_HoaDon_KhachHang_HaiLoai FOREIGN KEY(maKhachHang) REFERENCES dbo.KhachHang(maKhachHang);
    EXEC(N'CREATE OR ALTER TRIGGER dbo.TR_HoaDon_HaiLoai ON dbo.HoaDon AFTER INSERT,UPDATE AS
    BEGIN
      SET NOCOUNT ON;
      IF EXISTS(SELECT 1 FROM inserted i LEFT JOIN dbo.PhieuSuaChua p ON p.maPSC=i.maPSC
        WHERE i.maPSC IS NOT NULL AND (p.maPSC IS NULL OR p.maKhachHang<>i.maKhachHang OR p.VIN<>i.VIN))
        THROW 51104,N''Hóa đơn dịch vụ phải khớp khách hàng và VIN của phiếu sửa chữa.'',1;
      IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.ChiTietHoaDon c ON c.maHoaDon=i.maHoaDon
        WHERE i.maPSC IS NULL AND (c.maDichVu IS NOT NULL OR c.maPhuTung IS NULL OR c.soLuong<=0))
        THROW 51105,N''Hóa đơn bán hàng không được có dòng dịch vụ hoặc số lượng không dương.'',1;
      IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.LichSuTichDiem l ON l.maHoaDon=i.maHoaDon WHERE i.maPSC IS NULL)
        THROW 51106,N''Hóa đơn đã tích điểm dịch vụ không được đổi thành bán hàng.'',1;
    END;');
    EXEC(N'CREATE OR ALTER TRIGGER dbo.TR_ChiTietHoaDon_HaiLoai ON dbo.ChiTietHoaDon AFTER INSERT,UPDATE AS
    BEGIN
      SET NOCOUNT ON;
      IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.HoaDon h ON h.maHoaDon=i.maHoaDon
        WHERE h.maPSC IS NULL AND (i.maDichVu IS NOT NULL OR i.maPhuTung IS NULL OR i.soLuong<=0))
        THROW 51107,N''Hóa đơn bán hàng chỉ chứa phụ tùng với số lượng dương.'',1;
    END;');
    EXEC(N'CREATE OR ALTER TRIGGER dbo.TR_LichSuTichDiem_HaiLoai ON dbo.LichSuTichDiem AFTER INSERT,UPDATE AS
    BEGIN
      SET NOCOUNT ON;
      IF EXISTS(SELECT 1 FROM inserted i JOIN dbo.HoaDon h ON h.maHoaDon=i.maHoaDon WHERE h.maPSC IS NULL)
        THROW 51108,N''Chỉ tích điểm trên hóa đơn dịch vụ.'',1;
    END;');
    COMMIT;
END TRY
BEGIN CATCH
    IF XACT_STATE()<>0 ROLLBACK;
    THROW;
END CATCH;
GO

-- 12 hóa đơn bán hàng HD031..HD042, 24 chi tiết. Không tạo xe/PSC giả.
-- Giữ giá bán tại thời điểm hóa đơn; cập nhật tồn chỉ cho 4 hóa đơn đã thanh toán mới.
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
GO


IF NOT EXISTS (SELECT 1 FROM dbo.HoaDon WHERE maHoaDon = N'HD031')
BEGIN
    PRINT N'[SEED] Đang nạp hóa đơn bán hàng bán lẻ HD031..HD042...';
BEGIN TRY
    BEGIN TRANSACTION;
    DECLARE @NgayHoaDonMau datetime2(0)=CONVERT(date,SYSDATETIME());
    DECLARE @Mau TABLE(maHoaDon varchar(5) PRIMARY KEY,maKhachHang varchar(5),maNhanVien varchar(5),ngayLui int,trangThai nvarchar(50),maPhuongThuc varchar(5));
    INSERT @Mau VALUES
      ('HD031','KH001','NV001',12,N'Đã thanh toán','TT001'),('HD032','KH002','NV006',11,N'Đã thanh toán','TT002'),
      ('HD033','KH003','NV007',10,N'Đã thanh toán','TT001'),('HD034','KH004','NV008',9,N'Đã thanh toán','TT002'),
      ('HD035','KH005','NV001',8,N'Chờ thanh toán','TT001'),('HD036','KH001','NV006',7,N'Chờ thanh toán','TT002'),
      ('HD037','KH002','NV007',6,N'Chờ thanh toán','TT001'),('HD038','KH003','NV008',5,N'Chờ thanh toán','TT002'),
      ('HD039','KH004','NV001',4,N'Chưa thanh toán','TT001'),('HD040','KH005','NV006',3,N'Chưa thanh toán','TT002'),
      ('HD041','KH001','NV007',2,N'Chưa thanh toán','TT001'),('HD042','KH002','NV008',1,N'Chưa thanh toán','TT002');
    DECLARE @Dong TABLE(maHoaDon varchar(5),maPhuTung varchar(6),soLuong int,donGia decimal(18,2),PRIMARY KEY(maHoaDon,maPhuTung));
    INSERT @Dong VALUES
      ('HD031','PT001',1,850000),('HD031','PT002',1,180000),
      ('HD032','PT003',1,950000),('HD032','PT004',1,220000),
      ('HD033','PT001',1,850000),('HD033','PT004',1,220000),
      ('HD034','PT002',1,180000),('HD034','PT003',1,950000),
      ('HD035','PT001',1,850000),('HD035','PT003',1,950000),
      ('HD036','PT002',2,180000),('HD036','PT004',2,220000),
      ('HD037','PT001',2,850000),('HD037','PT002',1,180000),
      ('HD038','PT003',1,950000),('HD038','PT004',2,220000),
      ('HD039','PT001',1,850000),('HD039','PT004',1,220000),
      ('HD040','PT002',2,180000),('HD040','PT003',1,950000),
      ('HD041','PT003',2,950000),('HD041','PT004',1,220000),
      ('HD042','PT001',1,850000),('HD042','PT002',2,180000);
    -- Không chiếm mã của hóa đơn người dùng đã lập. Có thể chạy lại đúng bộ seed này.
    IF EXISTS(SELECT 1 FROM @Mau m JOIN dbo.HoaDon h WITH(UPDLOCK,HOLDLOCK) ON h.maHoaDon=m.maHoaDon
       WHERE h.maPSC IS NOT NULL OR h.VIN IS NOT NULL OR h.maKhachHang<>m.maKhachHang OR h.maNhanVien<>m.maNhanVien
       OR h.tienCong<>0 OR h.dichVuKhac<>0 OR h.maGiamGia IS NOT NULL OR COALESCE(h.giamGia,0)<>0
       OR h.tienPhuTung<>(SELECT SUM(d.soLuong*d.donGia) FROM @Dong d WHERE d.maHoaDon=m.maHoaDon)
       OR h.thueVAT<>ROUND(h.tienPhuTung*0.08,2)
       OR EXISTS(SELECT c.maPhuTung,c.soLuong,c.donGia FROM dbo.ChiTietHoaDon c WHERE c.maHoaDon=m.maHoaDon EXCEPT SELECT d.maPhuTung,d.soLuong,d.donGia FROM @Dong d WHERE d.maHoaDon=m.maHoaDon)
       OR EXISTS(SELECT d.maPhuTung,d.soLuong,d.donGia FROM @Dong d WHERE d.maHoaDon=m.maHoaDon EXCEPT SELECT c.maPhuTung,c.soLuong,c.donGia FROM dbo.ChiTietHoaDon c WHERE c.maHoaDon=m.maHoaDon))
        THROW 51109,N'Mã HD031..HD042 đã có dữ liệu khác. Không ghi đè; cần chọn bộ mã seed khác.',1;
    DECLARE @Moi TABLE(maHoaDon varchar(5) PRIMARY KEY);
    INSERT dbo.HoaDon(maHoaDon,maGiamGia,maKhachHang,maNhanVien,maPSC,VIN,ngayLap,tienPhuTung,tienCong,dichVuKhac,thueVAT,giamGia,trangThaiThanhToan,maPhuongThuc)
    OUTPUT inserted.maHoaDon INTO @Moi
    SELECT m.maHoaDon,NULL,m.maKhachHang,m.maNhanVien,NULL,NULL,DATEADD(hour,10,DATEADD(day,-m.ngayLui,@NgayHoaDonMau)),
       t.tien,0,0,ROUND(t.tien*0.08,2),0,m.trangThai,m.maPhuongThuc
    FROM @Mau m CROSS APPLY(SELECT SUM(d.soLuong*d.donGia) tien FROM @Dong d WHERE d.maHoaDon=m.maHoaDon)t
    WHERE NOT EXISTS(SELECT 1 FROM dbo.HoaDon h WITH(UPDLOCK,HOLDLOCK) WHERE h.maHoaDon=m.maHoaDon);
    INSERT dbo.ChiTietHoaDon(maHoaDon,maDichVu,maPhuTung,soLuong,donGia)
    SELECT d.maHoaDon,NULL,d.maPhuTung,d.soLuong,d.donGia FROM @Dong d JOIN @Moi n ON n.maHoaDon=d.maHoaDon;
    DECLARE @Xuat TABLE(maPhuTung varchar(6) PRIMARY KEY,soLuong int);
    INSERT @Xuat SELECT d.maPhuTung,SUM(d.soLuong) FROM @Dong d JOIN @Moi n ON n.maHoaDon=d.maHoaDon
      JOIN @Mau m ON m.maHoaDon=d.maHoaDon WHERE m.trangThai=N'Đã thanh toán' GROUP BY d.maPhuTung;
    IF EXISTS(SELECT 1 FROM @Xuat x LEFT JOIN dbo.TonKho k WITH(UPDLOCK,HOLDLOCK) ON k.maPhuTung=x.maPhuTung WHERE k.maPhuTung IS NULL OR k.soLuongHienTai<x.soLuong)
        THROW 51110,N'Không đủ tồn kho để tạo hóa đơn mẫu đã thanh toán. Toàn bộ seed được hoàn tác.',1;
    UPDATE k SET k.soLuongHienTai=k.soLuongHienTai-x.soLuong FROM dbo.TonKho k JOIN @Xuat x ON x.maPhuTung=k.maPhuTung;
    IF EXISTS(SELECT 1 FROM sys.sequences WHERE object_id=OBJECT_ID(N'dbo.Seq_HoaDon'))
    BEGIN
      DECLARE @NextHD int=(SELECT MAX(CONVERT(int,SUBSTRING(maHoaDon,3,3)))+1 FROM dbo.HoaDon);
      DECLARE @SeqHD bigint=(SELECT CONVERT(bigint,current_value) FROM sys.sequences WHERE object_id=OBJECT_ID(N'dbo.Seq_HoaDon'));
      IF @NextHD>@SeqHD
      BEGIN
        DECLARE @AlterHD nvarchar(200)=N'ALTER SEQUENCE dbo.Seq_HoaDon RESTART WITH '+CAST(@NextHD AS nvarchar(20));
        EXEC(@AlterHD);
      END;
    END;
    SELECT COUNT(*) HoaDonMoi FROM @Moi;
    COMMIT;
END TRY
BEGIN CATCH
    IF XACT_STATE()<>0 ROLLBACK;
    THROW;
END CATCH;

    PRINT N'[SEED] Nạp hóa đơn bán lẻ thành công.';
END
ELSE
BEGIN
    PRINT N'[SEED] Hóa đơn bán lẻ đã tồn tại, không nạp lại.';
END
GO


-- ============================================================================
-- E. KIỂM TRA TÍNH TOÀN VẸN RÀNG BUỘC (VERIFICATION)
-- ============================================================================
PRINT N'[VERIFY] Đang kiểm tra toàn vẹn ràng buộc với DBCC CHECKCONSTRAINTS...';
DBCC CHECKCONSTRAINTS WITH ALL_CONSTRAINTS;
GO
PRINT N'============================================================================';
PRINT N'CÀI ĐẶT / NÂNG CẤP DATABASE [GaraOto] HOÀN TẤT THÀNH CÔNG.';
PRINT N'============================================================================';
GO
