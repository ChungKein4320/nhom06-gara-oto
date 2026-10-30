-- ============================================================================
-- Migration V2: UC009 — Warehouse Replenishment Request, Approval and Actual Receiving
-- Target Database: Dedicated Test Database [GaraOto_UC009_TEST]
-- Engine: Microsoft SQL Server 2016+
-- Transactional & Fail-Safe Architecture:
-- 1. Preflight Verification (Database, Required Prerequisite Tables, Sequence Range, Data Validity)
-- 2. Atomic Transaction (DDL, Backfill, Constraint Adjustments, Filtered Unique Index)
-- 3. Automatic Rollback on Any DDL/DML Failure with Diagnostics
-- ============================================================================

-- ----------------------------------------------------------------------------
-- GIAI ĐOẠN 1: PREFLIGHT CHECKS (Kiểm tra điều kiện tiên quyết trước khi đổi schema)
-- ----------------------------------------------------------------------------

-- 1.1 Kiểm tra database hiện hành (CHỈ cho phép database test riêng [GaraOto_UC009_TEST])
IF DB_NAME() <> N'GaraOto_UC009_TEST'
BEGIN
    RAISERROR(N'Preflight Check Thất Bại: Database hiện hành là [%s]. Migration V2 chỉ được phép thực thi trên database test riêng [GaraOto_UC009_TEST] để bảo vệ tuyệt đối database mặc định [GaraOto]. Vui lòng chuyển kết nối sang [GaraOto_UC009_TEST] trước khi chạy migration.', 16, 1, DB_NAME());
    RETURN;
END

-- 1.2 Kiểm tra sự tồn tại của các bảng và cột tiền đề
IF OBJECT_ID(N'dbo.NhaCungCap', N'U') IS NULL
   OR OBJECT_ID(N'dbo.NhanVien', N'U') IS NULL
   OR OBJECT_ID(N'dbo.PhuTung', N'U') IS NULL
   OR OBJECT_ID(N'dbo.PhieuNhapKho', N'U') IS NULL
   OR OBJECT_ID(N'dbo.ChiTietPhieuNhapKho', N'U') IS NULL
BEGIN
    RAISERROR(N'Preflight Check Thất Bại: Không tìm thấy đầy đủ các bảng tiền đề trong schema hiện tại (cần có: NhaCungCap, NhanVien, PhuTung, PhieuNhapKho, ChiTietPhieuNhapKho).', 16, 1);
    RETURN;
END

-- 1.3 Kiểm tra dải mã YeuCauNhapKho hiện có (nếu bảng đã tồn tại)
DECLARE @PreflightMaxYCN int = 0;
IF OBJECT_ID(N'dbo.YeuCauNhapKho', N'U') IS NOT NULL
BEGIN
    SELECT @PreflightMaxYCN = ISNULL(MAX(CONVERT(int, SUBSTRING(maYCNK, 4, 3))), 0)
    FROM dbo.[YeuCauNhapKho]
    WHERE maYCNK LIKE 'YCN[0-9][0-9][0-9]';

    IF @PreflightMaxYCN >= 999
    BEGIN
        RAISERROR(N'Preflight Check Thất Bại: Dải mã YeuCauNhapKho đã đạt giới hạn tối đa (YCN999). Không còn dải mã 3 chữ số để khởi tạo thêm sequence.', 16, 1);
        RETURN;
    END
END

-- 1.4 Kiểm tra tính hợp lệ của dữ liệu hiện tại trước khi đặt CHECK constraint mới
IF EXISTS (SELECT 1 FROM dbo.[ChiTietPhieuNhapKho] WHERE [soLuongNhap] < 0)
BEGIN
    RAISERROR(N'Preflight Check Thất Bại: Bảng ChiTietPhieuNhapKho chứa số lượng nhập âm ([soLuongNhap] < 0). Không thể áp dụng CHECK constraint.', 16, 1);
    RETURN;
END

IF EXISTS (SELECT 1 FROM dbo.[PhieuNhapKho] WHERE [trangThai] NOT IN (N'Chờ duyệt', N'Đã duyệt', N'Đã nhập kho', N'Không duyệt'))
BEGIN
    RAISERROR(N'Preflight Check Thất Bại: Bảng PhieuNhapKho chứa trạng thái không hợp lệ ngoài miền cho phép.', 16, 1);
    RETURN;
END

PRINT N'Preflight checks thành công: Database, bảng tiền đề, dữ liệu hiện tại và dải mã đều hợp lệ.';

-- ----------------------------------------------------------------------------
-- GIAI ĐOẠN 2: THỰC THI SCHEMA VÀ BACKFILL TRONG GIAO DỊCH NGUYÊN TỬ
-- ----------------------------------------------------------------------------

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRANSACTION;

BEGIN TRY
    -- 2.1 Sequence cho YeuCauNhapKho
    IF NOT EXISTS (SELECT 1 FROM sys.sequences WHERE name = 'Seq_YeuCauNhapKho' AND schema_id = SCHEMA_ID('dbo'))
    BEGIN
        DECLARE @StartVal int = CASE WHEN @PreflightMaxYCN > 0 THEN @PreflightMaxYCN + 1 ELSE 1 END;
        DECLARE @SqlCreateSeq nvarchar(500) = N'CREATE SEQUENCE dbo.[Seq_YeuCauNhapKho] AS int START WITH ' 
            + CONVERT(nvarchar(10), @StartVal) + N' INCREMENT BY 1 MINVALUE 1 MAXVALUE 999 CACHE 10;';
        EXEC sp_executesql @SqlCreateSeq;
        PRINT N'[DDL] Đã tạo sequence dbo.Seq_YeuCauNhapKho với giá trị khởi đầu: ' + CONVERT(nvarchar(10), @StartVal);
    END
    ELSE IF @PreflightMaxYCN > 0
    BEGIN
        DECLARE @SqlRestartSeq nvarchar(500) = N'ALTER SEQUENCE dbo.[Seq_YeuCauNhapKho] RESTART WITH ' 
            + CONVERT(nvarchar(10), @PreflightMaxYCN + 1) + N';';
        EXEC sp_executesql @SqlRestartSeq;
        PRINT N'[DDL] Đã đồng bộ sequence dbo.Seq_YeuCauNhapKho về giá trị: ' + CONVERT(nvarchar(10), @PreflightMaxYCN + 1);
    END

    -- 2.2 Bảng YeuCauNhapKho
    IF OBJECT_ID(N'dbo.YeuCauNhapKho', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.[YeuCauNhapKho] (
            [maYCNK] varchar(6) NOT NULL CONSTRAINT [DF_YeuCauNhapKho_Ma] DEFAULT ('YCN' + RIGHT('000' + CONVERT(varchar(3), NEXT VALUE FOR dbo.[Seq_YeuCauNhapKho]), 3)),
            [maNCC] varchar(6) NOT NULL,
            [maNhanVien] varchar(5) NOT NULL,
            [ngayTao] datetime2(0) NOT NULL CONSTRAINT [DF_YeuCauNhapKho_NgayTao] DEFAULT (SYSDATETIME()),
            [trangThai] nvarchar(30) NOT NULL CONSTRAINT [DF_YeuCauNhapKho_TrangThai] DEFAULT (N'Chờ duyệt'),
            [maNhanVienDuyet] varchar(5) NULL,
            [ngayDuyet] datetime2(0) NULL,
            [ghiChu] nvarchar(255) NULL,
            CONSTRAINT [PK_YeuCauNhapKho] PRIMARY KEY ([maYCNK]),
            CONSTRAINT [CK_YeuCauNhapKho_Ma] CHECK (DATALENGTH([maYCNK]) = 6 AND [maYCNK] COLLATE Latin1_General_100_BIN2 LIKE 'YCN[0-9][0-9][0-9]'),
            CONSTRAINT [CK_YeuCauNhapKho_NgayTao] CHECK ([ngayTao] <= SYSDATETIME()),
            CONSTRAINT [CK_YeuCauNhapKho_TrangThai] CHECK ([trangThai] IN (N'Chờ duyệt', N'Đã duyệt', N'Từ chối', N'Đã xử lý')),
            CONSTRAINT [CK_YeuCauNhapKho_NgayDuyet] CHECK ([ngayDuyet] IS NULL OR [ngayDuyet] >= [ngayTao])
        );
        PRINT N'[DDL] Đã tạo bảng dbo.YeuCauNhapKho.';
    END
    ELSE
    BEGIN
        IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_YeuCauNhapKho_TrangThai')
            ALTER TABLE dbo.[YeuCauNhapKho] DROP CONSTRAINT [CK_YeuCauNhapKho_TrangThai];
        ALTER TABLE dbo.[YeuCauNhapKho] WITH CHECK ADD CONSTRAINT [CK_YeuCauNhapKho_TrangThai] 
            CHECK ([trangThai] IN (N'Chờ duyệt', N'Đã duyệt', N'Từ chối', N'Đã xử lý'));
    END

    -- 2.3 Bảng ChiTietYeuCauNhapKho
    IF OBJECT_ID(N'dbo.ChiTietYeuCauNhapKho', N'U') IS NULL
    BEGIN
        CREATE TABLE dbo.[ChiTietYeuCauNhapKho] (
            [maYCNK] varchar(6) NOT NULL,
            [maPhuTung] varchar(5) NOT NULL,
            [soLuongYeuCau] int NOT NULL,
            [donGiaDuKien] decimal(18,2) NOT NULL,
            CONSTRAINT [PK_ChiTietYeuCauNhapKho] PRIMARY KEY ([maYCNK], [maPhuTung]),
            CONSTRAINT [CK_ChiTietYeuCauNhapKho_SoLuong] CHECK ([soLuongYeuCau] > 0),
            CONSTRAINT [CK_ChiTietYeuCauNhapKho_DonGia] CHECK ([donGiaDuKien] >= 0),
            CONSTRAINT [CK_ChiTietYeuCauNhapKho_KhongPT000] CHECK ([maPhuTung] <> 'PT000')
        );
        PRINT N'[DDL] Đã tạo bảng dbo.ChiTietYeuCauNhapKho.';
    END

    -- 2.4 Khóa ngoại cho YeuCauNhapKho và ChiTietYeuCauNhapKho
    IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_YeuCauNhapKho_NhaCungCap')
        ALTER TABLE dbo.[YeuCauNhapKho] WITH CHECK ADD CONSTRAINT [FK_YeuCauNhapKho_NhaCungCap] FOREIGN KEY ([maNCC]) REFERENCES dbo.[NhaCungCap] ([maNCC]);

    IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_YeuCauNhapKho_NhanVien')
        ALTER TABLE dbo.[YeuCauNhapKho] WITH CHECK ADD CONSTRAINT [FK_YeuCauNhapKho_NhanVien] FOREIGN KEY ([maNhanVien]) REFERENCES dbo.[NhanVien] ([maNhanVien]);

    IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_YeuCauNhapKho_NhanVienDuyet')
        ALTER TABLE dbo.[YeuCauNhapKho] WITH CHECK ADD CONSTRAINT [FK_YeuCauNhapKho_NhanVienDuyet] FOREIGN KEY ([maNhanVienDuyet]) REFERENCES dbo.[NhanVien] ([maNhanVien]);

    IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_ChiTietYeuCauNhapKho_YeuCauNhapKho')
        ALTER TABLE dbo.[ChiTietYeuCauNhapKho] WITH CHECK ADD CONSTRAINT [FK_ChiTietYeuCauNhapKho_YeuCauNhapKho] FOREIGN KEY ([maYCNK]) REFERENCES dbo.[YeuCauNhapKho] ([maYCNK]);

    IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_ChiTietYeuCauNhapKho_PhuTung')
        ALTER TABLE dbo.[ChiTietYeuCauNhapKho] WITH CHECK ADD CONSTRAINT [FK_ChiTietYeuCauNhapKho_PhuTung] FOREIGN KEY ([maPhuTung]) REFERENCES dbo.[PhuTung] ([maPhuTung]);

    -- 2.5 Cập nhật PhieuNhapKho: thêm cột maYCNK và khóa ngoại
    IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.PhieuNhapKho') AND name = 'maYCNK')
    BEGIN
        ALTER TABLE dbo.[PhieuNhapKho] ADD [maYCNK] varchar(6) NULL;
        PRINT N'[DDL] Đã thêm cột maYCNK vào dbo.PhieuNhapKho.';
    END

    IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_PhieuNhapKho_YeuCauNhapKho')
    BEGIN
        ALTER TABLE dbo.[PhieuNhapKho] WITH CHECK ADD CONSTRAINT [FK_PhieuNhapKho_YeuCauNhapKho] FOREIGN KEY ([maYCNK]) REFERENCES dbo.[YeuCauNhapKho] ([maYCNK]);
        PRINT N'[DDL] Đã tạo khóa ngoại FK_PhieuNhapKho_YeuCauNhapKho.';
    END

    -- 2.6 Cập nhật CHECK constraint trên PhieuNhapKho.trangThai: cho phép 'Đã nhập kho' và giữ 'Đã duyệt' lịch sử
    IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_PhieuNhapKho_TrangThai')
        ALTER TABLE dbo.[PhieuNhapKho] DROP CONSTRAINT [CK_PhieuNhapKho_TrangThai];
    ALTER TABLE dbo.[PhieuNhapKho] WITH CHECK ADD CONSTRAINT [CK_PhieuNhapKho_TrangThai] 
        CHECK ([trangThai] IN (N'Chờ xác nhận nhập kho', N'Đã nhập kho', N'Đã hủy'));
    PRINT N'[DDL] Đã cập nhật ràng buộc CK_PhieuNhapKho_TrangThai (cho phép Đã nhập kho).';

    -- 2.7 Filtered unique index: 1-1 giữa PhieuNhapKho và YeuCauNhapKho (cho phép các bản ghi lịch sử có maYCNK IS NULL)
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UQ_PhieuNhapKho_maYCNK' AND object_id = OBJECT_ID('dbo.PhieuNhapKho'))
    BEGIN
        CREATE UNIQUE NONCLUSTERED INDEX [UQ_PhieuNhapKho_maYCNK]
        ON dbo.[PhieuNhapKho] ([maYCNK])
        WHERE [maYCNK] IS NOT NULL;
        PRINT N'[DDL] Đã tạo unique filtered index UQ_PhieuNhapKho_maYCNK.';
    END

    -- 2.8 Cập nhật ChiTietPhieuNhapKho: thêm soLuongYeuCau và soLuongThucNhan
    IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.ChiTietPhieuNhapKho') AND name = 'soLuongYeuCau')
    BEGIN
        ALTER TABLE dbo.[ChiTietPhieuNhapKho] ADD [soLuongYeuCau] int NULL;
        PRINT N'[DDL] Đã thêm cột soLuongYeuCau vào dbo.ChiTietPhieuNhapKho.';
    END

    IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.ChiTietPhieuNhapKho') AND name = 'soLuongThucNhan')
    BEGIN
        ALTER TABLE dbo.[ChiTietPhieuNhapKho] ADD [soLuongThucNhan] int NULL;
        PRINT N'[DDL] Đã thêm cột soLuongThucNhan vào dbo.ChiTietPhieuNhapKho.';
    END

    -- 2.9 Backfill dữ liệu lịch sử bằng COALESCE độc lập từng cột (bảo toàn giá trị không NULL)
    -- Dùng sp_executesql để batch biên dịch an toàn mà không cần lệnh GO gây ngắt giao dịch
    DECLARE @SqlBackfill nvarchar(max) = N'
        UPDATE dbo.[ChiTietPhieuNhapKho]
        SET [soLuongYeuCau] = COALESCE([soLuongYeuCau], [soLuongNhap]),
            [soLuongThucNhan] = COALESCE([soLuongThucNhan], [soLuongNhap])
        WHERE [soLuongYeuCau] IS NULL OR [soLuongThucNhan] IS NULL;';
    EXEC sp_executesql @SqlBackfill;
    PRINT N'[DML] Đã backfill độc lập cho các cột NULL trong dbo.ChiTietPhieuNhapKho (bảo toàn dữ liệu lịch sử).';

    -- 2.10 Điều chỉnh CHECK constraints cho ChiTietPhieuNhapKho
    IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_ChiTietPhieuNhapKho_SoLuong')
        ALTER TABLE dbo.[ChiTietPhieuNhapKho] DROP CONSTRAINT [CK_ChiTietPhieuNhapKho_SoLuong];
    ALTER TABLE dbo.[ChiTietPhieuNhapKho] WITH CHECK ADD CONSTRAINT [CK_ChiTietPhieuNhapKho_SoLuong] CHECK ([soLuongNhap] >= 0);

    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_ChiTietPhieuNhapKho_SoLuongYeuCau')
        ALTER TABLE dbo.[ChiTietPhieuNhapKho] WITH CHECK ADD CONSTRAINT [CK_ChiTietPhieuNhapKho_SoLuongYeuCau] CHECK ([soLuongYeuCau] IS NULL OR [soLuongYeuCau] > 0);

    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_ChiTietPhieuNhapKho_SoLuongThucNhan')
        ALTER TABLE dbo.[ChiTietPhieuNhapKho] WITH CHECK ADD CONSTRAINT [CK_ChiTietPhieuNhapKho_SoLuongThucNhan] CHECK ([soLuongThucNhan] IS NULL OR [soLuongThucNhan] >= 0);

    IF NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CK_ChiTietPhieuNhapKho_HopLeThucNhan')
    BEGIN
        DECLARE @SqlConstraint nvarchar(max) = N'
            ALTER TABLE dbo.[ChiTietPhieuNhapKho] WITH CHECK ADD CONSTRAINT [CK_ChiTietPhieuNhapKho_HopLeThucNhan]
            CHECK ([soLuongThucNhan] IS NULL OR ([soLuongNhap] <= [soLuongThucNhan] AND ([soLuongYeuCau] IS NULL OR [soLuongThucNhan] <= [soLuongYeuCau])));';
        EXEC sp_executesql @SqlConstraint;
    END
    PRINT N'[DDL] Đã cập nhật các ràng buộc CHECK cho dbo.ChiTietPhieuNhapKho.';

    -- Commit transaction khi mọi bước đều thành công
    COMMIT TRANSACTION;
    PRINT N'============================================================================';
    PRINT N'Migration V2__UC009_Warehouse_Workflow hoàn tất thành công và đã được commit.';
    PRINT N'============================================================================';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    DECLARE @ErrMsg nvarchar(4000) = ERROR_MESSAGE();
    DECLARE @ErrSev int = ERROR_SEVERITY();
    DECLARE @ErrState int = ERROR_STATE();
    DECLARE @ErrLine int = ERROR_LINE();

    RAISERROR(N'Migration V2 thất bại tại dòng %d. Giao dịch đã được rollback an toàn. Chi tiết lỗi: %s', 
              @ErrSev, @ErrState, @ErrLine, @ErrMsg);
END CATCH;
