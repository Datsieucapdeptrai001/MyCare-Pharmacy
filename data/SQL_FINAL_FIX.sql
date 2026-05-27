/* =========================================================
   SQL FINAL BẢN CUỐI - MYCAREPHARMACY
   Đã gồm:
   - SQL_FINAL gốc
   - Query4 kiểm kê kho
   - Fix ViTriThuoc / MauLieu / ChiTietMauLieu theo DAO_SanPham.java hiện tại
   - Data mẫu hợp lý 2026
   ========================================================= */

USE [master];
GO

IF DB_ID('MYCAREPHARMACY') IS NOT NULL
BEGIN
    ALTER DATABASE [MYCAREPHARMACY] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE [MYCAREPHARMACY];
END
GO

-- 2. TẠO DATABASE MỚI
CREATE DATABASE [MYCAREPHARMACY];
GO

USE [MYCAREPHARMACY];
GO
CREATE TABLE [dbo].[LieuMau](
    [id] [nvarchar](50) NOT NULL PRIMARY KEY,
    [tenLieu] [nvarchar](150) NOT NULL,
    [nhomBenh] [nvarchar](100) NULL,
    [moTa] [nvarchar](255) NULL
);

CREATE TABLE [dbo].[ChiTietLieuMau](
    [lieuMauId] [nvarchar](50) NOT NULL,
    [sanPhamId] [nvarchar](50) NOT NULL,
    [soLuong] [int] NOT NULL,
    CONSTRAINT [PK_ChiTietLieuMau] PRIMARY KEY CLUSTERED ([lieuMauId] ASC, [sanPhamId] ASC)
);
GO
CREATE TABLE [dbo].[ApDungKhuyenMai](
	[id] [nvarchar](50) NOT NULL PRIMARY KEY,
	[khuyenMaiId] [nvarchar](50) NOT NULL,
	[sanPhamId] [nvarchar](50) NOT NULL
)
GO

CREATE TABLE [dbo].[CaLamViec](
	[id] [nvarchar](50) NOT NULL PRIMARY KEY,
	[nhanVienId] [nvarchar](50) NOT NULL,
	[thoiGianBatDau] [datetime2](7) NOT NULL,
	[thoiGianKetThuc] [datetime2](7) NULL,
	[tienHeThongGhiNhan] [decimal](18, 2) NOT NULL,
	[tienDauCa] [decimal](18, 2) NOT NULL,
	[tienKetCa] [decimal](18, 2) NOT NULL,
	[loaiCa] [int] DEFAULT ((0)) NULL,
	[ghiChuKetCa] [nvarchar](max) NULL
)
GO

CREATE TABLE [dbo].[CauHinhTichDiem](
	[tienMua] [int] NULL,
	[diemThuong] [int] NULL,
	[tienDoiMotDiem] [int] NULL,
	[diemToiThieu] [int] NULL
)
GO

CREATE TABLE [dbo].[ChiTietHoaDon](
	[hoaDonId] [nvarchar](50) NOT NULL,
	[donViDoLuongId] [nvarchar](50) NOT NULL,
	[sanPhamId] [nvarchar](50) NOT NULL,
	[soLuong] [int] NOT NULL,
	[donGiaThucTe] [decimal](18, 2) NOT NULL DEFAULT 0,
	[thanhTien] [decimal](18, 2) NOT NULL DEFAULT 0,
    [ghiChu] [nvarchar](255) NULL,
    CONSTRAINT [PK_ChiTietHoaDon] PRIMARY KEY CLUSTERED ([hoaDonId] ASC, [donViDoLuongId] ASC, [sanPhamId] ASC)
)
GO

CREATE TABLE [dbo].[DieuKienKhuyenMai](
	[id] [nvarchar](50) NOT NULL PRIMARY KEY,
	[loaiDieuKien] [nvarchar](20) NOT NULL,
	[doiTuongApDung] [nvarchar](20) NOT NULL,
	[giaTri] [decimal](18, 2) NOT NULL,
	[khuyenMaiId] [nvarchar](50) NOT NULL
)
GO

CREATE TABLE [dbo].[DonViDoLuong](
	[id] [nvarchar](50) NOT NULL,
	[sanPhamId] [nvarchar](50) NOT NULL,
	[ten] [nvarchar](100) NOT NULL,
	[chuyenDoiDonViCoBan] [decimal](18, 2) NOT NULL,
	[gia] [decimal](18, 2) NOT NULL,
	[maVach] [nvarchar](500) NULL, -- Luu nhieu ma cach nhau bang dau phay
    CONSTRAINT [PK_DonViDoLuong] PRIMARY KEY CLUSTERED ([id] ASC, [sanPhamId] ASC)
)
GO

CREATE TABLE [dbo].[HinhThucKhuyenMai](
	[id] [nvarchar](50) NOT NULL PRIMARY KEY,
	[loaiHinhThuc] [nvarchar](30) NOT NULL,
	[doiTuongApDung] [nvarchar](20) NOT NULL,
	[moTa] [nvarchar](max) NULL,
	[giaTri] [decimal](18, 2) NOT NULL,
	[donViDoLuongId] [nvarchar](50) NULL,
	[sanPhamId] [nvarchar](50) NULL,
	[khuyenMaiId] [nvarchar](50) NOT NULL,
	[giamToiDa] [decimal](18, 2) NULL,
	[spYeuCau] [nvarchar](255) NULL,
	[slYeuCau] [int] NULL,
	[spTang] [nvarchar](255) NULL,
	[slTang] [int] NULL,
	[dvdlYeuCau] [nvarchar](50) NULL,
	[dvdlTang] [nvarchar](50) NULL
)
GO

CREATE TABLE [dbo].[HoaDon](
	[id] [nvarchar](50) NOT NULL PRIMARY KEY,
	[loaiHD] [nvarchar](20) NOT NULL,
	[ghiChu] [nvarchar](max) NULL,
	[ngayLapHD] [datetime2](7) NOT NULL,
	[nhanVienId] [nvarchar](50) NOT NULL,
	[khachHangId] [nvarchar](50) NULL,
	[khuyenMaiId] [nvarchar](50) NULL,
	[phuongThucThanhToan] [nvarchar](40) NOT NULL,
	[hoaDonGocId] [nvarchar](50) NULL
)
GO

CREATE TABLE [dbo].[KhachHang](
	[id] [nvarchar](50) NOT NULL PRIMARY KEY,
	[sdt] [nvarchar](15) NULL,
	[hoVaTen] [nvarchar](100) NOT NULL,
	[ngayTao] [datetime2](7) NOT NULL,
	[diemTichLuy] [int] DEFAULT ((0)) NOT NULL,
)
GO

CREATE TABLE [dbo].[KhoHang](
	[id] [nvarchar](50) NOT NULL PRIMARY KEY,
	[sucChua] [int] NOT NULL
)
GO

-- ==============================================================================
-- VỊ TRÍ THUỐC / KỆ TỦ
-- Bổ sung để DAO_SanPham.layTatCaViTri() không lỗi Invalid object name 'ViTriThuoc'.
-- Bảng này dùng cho combo vị trí trên màn hình sản phẩm; chưa gắn FK vào SanPham
-- để không phá cấu trúc INSERT SanPham gốc.
-- ==============================================================================
CREATE TABLE [dbo].[ViTriThuoc](
    [id] [nvarchar](50) NOT NULL PRIMARY KEY,
    [viTriId] AS ([id]),
    [maViTri] [nvarchar](50) NULL,
    [tenViTri] [nvarchar](150) NOT NULL,
    [khuVuc] [nvarchar](100) NULL,
    [maKe] [nvarchar](50) NULL,
    [tenKe] [nvarchar](100) NULL,
    [tang] [int] NULL,
    [ngan] [int] NULL,
    [moTa] [nvarchar](255) NULL,
    [trangThai] [nvarchar](20) NOT NULL DEFAULT N'HOAT_DONG'
)
GO


CREATE TABLE [dbo].[KhuyenMai](
	[id] [nvarchar](50) NOT NULL PRIMARY KEY,
	[tenKhuyenMai] [nvarchar](150) NOT NULL,
	[moTa] [nvarchar](max) NULL,
	[ngayTao] [datetime2](7) NOT NULL,
	[ngayBatDau] [datetime2](7) NOT NULL,
	[ngayKetThuc] [datetime2](7) NULL,
	[trangThai] [nvarchar](20) DEFAULT ('HOAT_DONG') NULL
)
GO

CREATE TABLE [dbo].[LoHang](
	[id] [nvarchar](50) NOT NULL PRIMARY KEY,
	[soLoHang] [nvarchar](100) NOT NULL,
	[sanPhamId] [nvarchar](50) NOT NULL,
	[soLuongLoHang] [int] NOT NULL,
	[gia] [decimal](18, 2) NOT NULL,
	[ngayHetHan] [datetime2](7) NULL,
	[trangThai] [nvarchar](20) NOT NULL,
	[ngayNhap] [datetime2](7) NOT NULL,
	[khoHangId] [nvarchar](50) NOT NULL,
	[maVachNoiBo] [nvarchar](50) NULL
)
GO


-- ==============================================================================
-- PHIẾU NHẬP HÀNG / CHI TIẾT PHIẾU NHẬP HÀNG
-- Đặt sau LoHang để không lỗi FK, FK sẽ add ở cuối sau khi đủ bảng cha.
-- ==============================================================================
CREATE TABLE [dbo].[PhieuNhapHang] (
    [id] NVARCHAR(50) NOT NULL PRIMARY KEY,
    [ngayNhap] DATETIME2(7) NOT NULL DEFAULT SYSDATETIME(),
    [nhaCungCapId] NVARCHAR(50) NULL,
    [nhanVienId] NVARCHAR(50) NULL,
    [tongTien] DECIMAL(18, 2) NOT NULL DEFAULT 0,
    [ghiChu] NVARCHAR(255) NULL,
    [trangThai] NVARCHAR(30) NOT NULL DEFAULT N'HOAN_THANH'
)
GO

CREATE TABLE [dbo].[ChiTietPhieuNhapHang] (
    [id] NVARCHAR(50) NOT NULL PRIMARY KEY,
    [phieuNhapId] NVARCHAR(50) NOT NULL,
    [loHangId] NVARCHAR(50) NOT NULL,
    [sanPhamId] NVARCHAR(50) NOT NULL,
    [khoHangId] NVARCHAR(50) NOT NULL,
    [soLoHang] NVARCHAR(100) NOT NULL,
    [soLuongNhap] INT NOT NULL,
    [donGiaNhap] DECIMAL(18, 2) NOT NULL,
    [thanhTien] DECIMAL(18, 2) NOT NULL,
    [hanSuDung] DATETIME2(7) NULL
)
GO

CREATE TABLE [dbo].[PhieuXuatKho] (
    [Id] INT IDENTITY(1,1) PRIMARY KEY,
    [SoLoHang] NVARCHAR(50) NOT NULL,
    [SoLuongXuat] INT NOT NULL,
    [LyDoXuat] NVARCHAR(255) NOT NULL,
    [NguoiThucHien] NVARCHAR(100) NOT NULL,
    [NgayXuat] DATETIME DEFAULT CURRENT_TIMESTAMP
)
GO


CREATE TABLE [dbo].[NhanVien](
	[id] [nvarchar](50) NOT NULL PRIMARY KEY,
	[hoVaTen] [nvarchar](100) NOT NULL,
	[soChungChiHanhNghe] [nvarchar](50) NULL,
	[sdt] [nvarchar](15) NULL,
	[email] [nvarchar](100) NULL,
	[chucVu] [nvarchar](20) NOT NULL,
	[trangThaiLamViec] [nvarchar](20) NOT NULL,
	[gioiTinh] [nvarchar](10) NULL,
	[ngaySinh] [date] NULL,
	[diaChi] [nvarchar](255) NULL,
	[cccd] [nvarchar](20) NULL
)
GO

CREATE TABLE [dbo].[PhanBoLoHang](
	[hoaDonId] [nvarchar](50) NOT NULL,
	[donViDoLuongId] [nvarchar](50) NOT NULL,
	[sanPhamId] [nvarchar](50) NOT NULL,
	[loHangId] [nvarchar](50) NOT NULL,
	[soLuong] [int] NOT NULL,
    CONSTRAINT [PK_PhanBoLoHang] PRIMARY KEY CLUSTERED ([hoaDonId] ASC, [donViDoLuongId] ASC, [sanPhamId] ASC, [loHangId] ASC)
)
GO

CREATE TABLE [dbo].[SanPham](
	[id] [nvarchar](50) NOT NULL PRIMARY KEY,
	[danhMuc] [nvarchar](30) NOT NULL,
	[dang] [nvarchar](20) NOT NULL,
	[ten] [nvarchar](150) NOT NULL,
	[tenVietTat] [nvarchar](100) NULL,
	[nhaSanXuat] [nvarchar](150) NULL,
	[hoatChat] [nvarchar](150) NULL,
	[thueVAT] [decimal](18, 2) NOT NULL,
	[hamLuong] [nvarchar](100) NULL,
	[moTa] [nvarchar](max) NULL,
	[donViDoCoBan] [nvarchar](50) NOT NULL,
	[ngayTao] [datetime2](7) NULL,
	[trangThai] [nvarchar](20) DEFAULT ('HOAT_DONG') NULL,
	[giaBan] [decimal](18, 2) DEFAULT ((0)) NOT NULL,
	[maVach] [nvarchar](500) NULL,
	[nhomBenhLy] [nvarchar](100) NULL
)
GO



-- ==============================================================================
-- PHIẾU KIỂM KÊ KHO / CHI TIẾT PHIẾU KIỂM KÊ KHO
-- Đặt sau NhanVien + KhoHang + LoHang + SanPham để tạo FK trực tiếp không lỗi.
-- ==============================================================================
CREATE TABLE [dbo].[PhieuKiemKeKho] (
    [id] NVARCHAR(50) NOT NULL PRIMARY KEY,
    [ngayKiemKe] DATETIME2(7) NOT NULL DEFAULT SYSDATETIME(),
    [khoHangId] NVARCHAR(50) NOT NULL,
    [nhanVienId] NVARCHAR(50) NULL,
    [tongSoDong] INT NOT NULL DEFAULT 0,
    [tongChenhLech] INT NOT NULL DEFAULT 0,
    [ghiChu] NVARCHAR(255) NULL,
    [trangThai] NVARCHAR(30) NOT NULL DEFAULT N'HOAN_THANH',

    CONSTRAINT [FK_PKK_KhoHang] FOREIGN KEY ([khoHangId])
        REFERENCES [dbo].[KhoHang]([id]),

    CONSTRAINT [FK_PKK_NhanVien] FOREIGN KEY ([nhanVienId])
        REFERENCES [dbo].[NhanVien]([id])
)
GO

CREATE TABLE [dbo].[ChiTietPhieuKiemKeKho] (
    [id] NVARCHAR(50) NOT NULL PRIMARY KEY,
    [phieuKiemKeId] NVARCHAR(50) NOT NULL,
    [loHangId] NVARCHAR(50) NOT NULL,
    [sanPhamId] NVARCHAR(50) NOT NULL,
    [khoHangId] NVARCHAR(50) NOT NULL,
    [soLoHang] NVARCHAR(100) NOT NULL,
    [tonHeThong] INT NOT NULL,
    [tonThucTe] INT NOT NULL,
    [chenhLech] INT NOT NULL,
    [lyDo] NVARCHAR(255) NULL,

    CONSTRAINT [FK_CTPKK_PhieuKiemKe] FOREIGN KEY ([phieuKiemKeId])
        REFERENCES [dbo].[PhieuKiemKeKho]([id]) ON DELETE CASCADE,

    CONSTRAINT [FK_CTPKK_LoHang] FOREIGN KEY ([loHangId])
        REFERENCES [dbo].[LoHang]([id]),

    CONSTRAINT [FK_CTPKK_SanPham] FOREIGN KEY ([sanPhamId])
        REFERENCES [dbo].[SanPham]([id]),

    CONSTRAINT [FK_CTPKK_KhoHang] FOREIGN KEY ([khoHangId])
        REFERENCES [dbo].[KhoHang]([id])
)
GO

CREATE INDEX [IX_PhieuKiemKeKho_NgayKiemKe]
    ON [dbo].[PhieuKiemKeKho]([ngayKiemKe] DESC);
GO

CREATE INDEX [IX_CTPKK_PhieuKiemKe]
    ON [dbo].[ChiTietPhieuKiemKeKho]([phieuKiemKeId]);
GO
CREATE TABLE [dbo].[TaiKhoan](
	[id] [nvarchar](50) NOT NULL PRIMARY KEY,
	[nhanVienId] [nvarchar](50) NULL UNIQUE,
	[vaiTro] [nvarchar](20) NOT NULL,
	-- tenDangNhap = mã nhân viên (QL-0001, DS-0001, ...)
	[tenDangNhap] [nvarchar](50) NOT NULL UNIQUE,
	[matKhau] [nvarchar](255) NOT NULL
)
GO

-- UNIQUE CONSTRAINTS
ALTER TABLE [dbo].[ApDungKhuyenMai] ADD CONSTRAINT [UQ_ApDungKhuyenMai] UNIQUE NONCLUSTERED ([khuyenMaiId] ASC, [sanPhamId] ASC)
GO


-- ==============================================================================

-- UNIQUE INDEX tren SanPham.maVach (chi ap dung cho gia tri non-null)
-- Cho phep nhieu san pham khong co ma vach (NULL), nhung ma vach phai duy nhat
CREATE UNIQUE NONCLUSTERED INDEX [UQ_SanPham_maVach]
    ON [dbo].[SanPham]([maVach] ASC)
    WHERE ([maVach] IS NOT NULL AND [maVach] != '');
GO
-- UNIQUE INDEX tren DonViDoLuong.maVach
CREATE UNIQUE NONCLUSTERED INDEX [UQ_DonViDoLuong_maVach]
    ON [dbo].[DonViDoLuong]([maVach] ASC)
    WHERE ([maVach] IS NOT NULL AND [maVach] != '');
GO
-- 4. THÊM RÀNG BUỘC CHECK (CHECK CONSTRAINTS)
-- ==============================================================================

ALTER TABLE [dbo].[CaLamViec] ADD CONSTRAINT [CK_CaLamViec_ThoiGian] CHECK (([thoiGianKetThuc] IS NULL OR [thoiGianKetThuc]>=[thoiGianBatDau]))
ALTER TABLE [dbo].[DieuKienKhuyenMai] ADD CONSTRAINT [CK_DieuKienKhuyenMai_DoiTuong] CHECK (([doiTuongApDung]=N'HOA_DON' OR [doiTuongApDung]=N'SAN_PHAM'))
ALTER TABLE [dbo].[DieuKienKhuyenMai] ADD CONSTRAINT [CK_DieuKienKhuyenMai_Loai] CHECK (([loaiDieuKien]=N'GIA_TRI' OR [loaiDieuKien]=N'SO_LUONG'))
ALTER TABLE [dbo].[HinhThucKhuyenMai] ADD CONSTRAINT [CK_HinhThucKhuyenMai_DoiTuong] CHECK (([doiTuongApDung]=N'HOA_DON' OR [doiTuongApDung]=N'SAN_PHAM'))
ALTER TABLE [dbo].[HinhThucKhuyenMai] ADD CONSTRAINT [CK_HinhThucKhuyenMai_Loai] CHECK (([loaiHinhThuc]=N'SAN_PHAM_KEM_THEO' OR [loaiHinhThuc]=N'GIAM_THEO_PHAN_TRAM' OR [loaiHinhThuc]=N'GIAM_TIEN_MAT'))
ALTER TABLE [dbo].[HoaDon] ADD CONSTRAINT [CK_HoaDon_Loai] CHECK (([loaiHD]=N'BAN_HANG' OR [loaiHD]=N'TRA_HANG' OR [loaiHD]=N'DOI_HANG'))
ALTER TABLE [dbo].[HoaDon] ADD CONSTRAINT [CK_HoaDon_PhuongThucThanhToan] CHECK (([phuongThucThanhToan]=N'CHUYEN_KHOAN_NGAN_HANG' OR [phuongThucThanhToan]=N'TIEN_MAT'))
ALTER TABLE [dbo].[KhuyenMai] ADD CONSTRAINT [CK_KhuyenMai_ThoiGian] CHECK (([ngayKetThuc] IS NULL OR [ngayKetThuc]>=[ngayBatDau]))
ALTER TABLE [dbo].[KhuyenMai] ADD CONSTRAINT [CK_KhuyenMai_TrangThai] CHECK (([trangThai]='HOAT_DONG' OR [trangThai]='KHONG_HOAT_DONG' OR [trangThai]='HET_HAN'))
ALTER TABLE [dbo].[LoHang] ADD CONSTRAINT [CK_LoHang_TrangThai] CHECK (([trangThai]=N'HET_HAN' OR [trangThai]=N'HET_HANG' OR [trangThai]=N'CON_HANG' OR [trangThai]=N'AN'))
ALTER TABLE [dbo].[NhanVien] ADD CONSTRAINT [CK_NhanVien_ChucVu] CHECK (([chucVu]=N'NGUOI_QUAN_LY' OR [chucVu]=N'DUOC_SI'))
ALTER TABLE [dbo].[NhanVien] ADD CONSTRAINT [CK_NhanVien_TrangThaiLamViec] CHECK (([trangThaiLamViec]=N'DANG_LAM_VIEC' OR [trangThaiLamViec]=N'NGHI_PHEP' OR [trangThaiLamViec]=N'THOI_VIEC'))
ALTER TABLE [dbo].[SanPham] ADD CONSTRAINT [CK_SanPham_Dang] CHECK (([dang]=N'VIEN_NEN' OR [dang]=N'VIEN_NANG' OR [dang]=N'VIEN_SUI' OR [dang]=N'THUOC_BOT' OR [dang]=N'KEO_NGAM' OR [dang]=N'DUNG_DICH' OR [dang]=N'HON_DICH' OR [dang]=N'THUOC_NHO_GIOT' OR [dang]=N'SUC_MIENG'))
ALTER TABLE [dbo].[SanPham] ADD CONSTRAINT [CK_SanPham_DanhMuc] CHECK (([danhMuc]=N'MY_PHAM' OR [danhMuc]=N'THUOC_KE_DON' OR [danhMuc]=N'THUOC_KHONG_KE_DON' OR [danhMuc]=N'THUC_PHAM_CHUC_NANG'))
ALTER TABLE [dbo].[TaiKhoan] ADD CONSTRAINT [CK_TaiKhoan_VaiTro] CHECK (([vaiTro]=N'STAFF' OR [vaiTro]=N'ADMIN'))
ALTER TABLE [dbo].[ChiTietLieuMau] ADD CONSTRAINT [FK_ChiTietLieuMau_LieuMau] FOREIGN KEY([lieuMauId]) REFERENCES [dbo].[LieuMau] ([id]) ON DELETE CASCADE;
ALTER TABLE [dbo].[ChiTietLieuMau] ADD CONSTRAINT [FK_ChiTietLieuMau_SanPham] FOREIGN KEY([sanPhamId]) REFERENCES [dbo].[SanPham] ([id]) ON DELETE CASCADE ON UPDATE CASCADE;
GO
-- ==============================================================================
-- 5. CHÈN DỮ LIỆU ĐÚNG THỨ TỰ CHA - CON
-- ==============================================================================
-- A. BẢNG KHÔNG CHỨA KHÓA NGOẠI
INSERT [dbo].[NhanVien] VALUES (N'DS-0001', N'Nguyễn Tuấn Đạt', N'CCHN-DS-2021-001', N'0912345678', N'dat@mycarepharmacy.vn', N'DUOC_SI', N'DANG_LAM_VIEC', N'Nam', CAST(N'1990-01-01' AS Date), N'TP.HCM', N'079090000001')
INSERT [dbo].[NhanVien] VALUES (N'DS-0002', N'Mai Trung Kiên', N'CCHN-DS-2021-002', N'0923456789', N'kien@mycarepharmacy.vn', N'DUOC_SI', N'DANG_LAM_VIEC', N'Nam', CAST(N'1992-05-10' AS Date), N'TP.HCM', N'079092000002')
INSERT [dbo].[NhanVien] VALUES (N'DS-0003', N'Nguyễn Văn Phương Nam', N'CCHN-DS-2021-003', N'0934567890', N'nam@mycarepharmacy.vn', N'DUOC_SI', N'DANG_LAM_VIEC', N'Nam', CAST(N'1995-08-15' AS Date), N'TP.HCM', N'079095000003')
INSERT [dbo].[NhanVien] VALUES (N'DS-0004', N'Trần Long Thuận', N'CCHN-DS-2022-001', N'0945678901', N'thuan@mycarepharmacy.vn', N'DUOC_SI', N'DANG_LAM_VIEC', N'Nam', CAST(N'1993-11-20' AS Date), N'TP.HCM', N'079093000004')
INSERT [dbo].[NhanVien] VALUES (N'DS-0005', N'Võ Anh Kiệt', N'CCHN-DS-2022-002', N'0956789012', N'kiet@mycarepharmacy.vn', N'DUOC_SI', N'DANG_LAM_VIEC', N'Nam', CAST(N'1996-03-25' AS Date), N'TP.HCM', N'079096000005')
INSERT [dbo].[NhanVien] VALUES (N'QL-0001', N'Nguyễn Quản Lý', N'CCHN-QL-2020-001', N'0901234567', N'admin@mycarepharmacy.vn', N'NGUOI_QUAN_LY', N'DANG_LAM_VIEC', N'Nam', CAST(N'1985-12-12' AS Date), N'TP.HCM', N'079085000006')

-- CHÈN DỮ LIỆU BẢNG KHACH HANG (ĐÃ RÚT GỌN 5 CỘT)
INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) 
VALUES (N'KH-0001', N'0311223344', N'Nguyễn Thị Lan', CAST(N'2024-01-10T08:00:00.0000000' AS DateTime2), 500);

INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) 
VALUES (N'KH-0002', N'0322334455', N'Trần Văn Bình', CAST(N'2024-02-15T09:30:00.0000000' AS DateTime2), 1200);

INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) 
VALUES (N'KH-0003', N'0333445566', N'Lê Thị Hoa', CAST(N'2024-03-20T10:00:00.0000000' AS DateTime2), 350);

INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) 
VALUES (N'KH-0004', N'0344556677', N'Phạm Thanh Tùng', CAST(N'2024-04-05T11:00:00.0000000' AS DateTime2), 2000);

INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) 
VALUES (N'KH-0005', N'0355667788', N'Võ Thị Mai', CAST(N'2024-05-12T13:00:00.0000000' AS DateTime2), 150);

INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) 
VALUES (N'KH-0006', N'0366778899', N'Đặng Minh Khoa', CAST(N'2024-06-18T14:30:00.0000000' AS DateTime2), 800);

INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) 
VALUES (N'KH-0007', N'0377889900', N'Hoàng Thị Thu', CAST(N'2024-07-22T08:45:00.0000000' AS DateTime2), 0);

INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) 
VALUES (N'KH-0008', N'0388990011', N'Bùi Văn Long', CAST(N'2024-08-30T09:15:00.0000000' AS DateTime2), 450);

INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) 
VALUES (N'KH-0009', N'0399001122', N'Ngô Thị Thanh', CAST(N'2024-09-14T10:30:00.0000000' AS DateTime2), 3000);

INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) 
VALUES (N'KH-0010', N'0310112233', N'Dương Quốc Hùng', CAST(N'2024-10-25T15:00:00.0000000' AS DateTime2), 100);

INSERT [dbo].[KhoHang] VALUES (N'KHO-0001', 5000)
INSERT [dbo].[KhoHang] VALUES (N'KHO-0002', 3000)

-- VỊ TRÍ THUỐC MẪU 2026
INSERT [dbo].[ViTriThuoc] VALUES (N'VT-0001', N'VT-A1-01', N'Kệ A1 - Ngăn 01', N'Quầy bán thuốc', N'KE-A1', N'Kệ thuốc kê đơn', 1, 1, N'Kháng sinh, thuốc kê đơn bán chạy', N'HOAT_DONG')
INSERT [dbo].[ViTriThuoc] VALUES (N'VT-0002', N'VT-A1-02', N'Kệ A1 - Ngăn 02', N'Quầy bán thuốc', N'KE-A1', N'Kệ thuốc kê đơn', 1, 2, N'Tim mạch, huyết áp', N'HOAT_DONG')
INSERT [dbo].[ViTriThuoc] VALUES (N'VT-0003', N'VT-B1-01', N'Kệ B1 - Ngăn 01', N'Quầy OTC', N'KE-B1', N'Kệ thuốc không kê đơn', 1, 1, N'Giảm đau, hạ sốt, dị ứng', N'HOAT_DONG')
INSERT [dbo].[ViTriThuoc] VALUES (N'VT-0004', N'VT-B1-02', N'Kệ B1 - Ngăn 02', N'Quầy OTC', N'KE-B1', N'Kệ thuốc không kê đơn', 1, 2, N'Tiêu hóa, dạ dày', N'HOAT_DONG')
INSERT [dbo].[ViTriThuoc] VALUES (N'VT-0005', N'VT-C1-01', N'Kệ C1 - Ngăn 01', N'Khu vitamin', N'KE-C1', N'Kệ thực phẩm chức năng', 1, 1, N'Vitamin, khoáng chất, bổ sung sức khỏe', N'HOAT_DONG')
INSERT [dbo].[ViTriThuoc] VALUES (N'VT-0006', N'VT-D1-01', N'Kệ D1 - Ngăn 01', N'Mỹ phẩm - chăm sóc da', N'KE-D1', N'Kệ mỹ phẩm', 1, 1, N'Mỹ phẩm, chăm sóc da, nước súc miệng', N'HOAT_DONG')
INSERT [dbo].[ViTriThuoc] VALUES (N'VT-0007', N'VT-KHO-01', N'Kho dự trữ - Tủ 01', N'Kho dự trữ', N'TU-01', N'Tủ dự trữ khô mát', 1, 1, N'Lô hàng dự trữ, chưa trưng bày', N'HOAT_DONG')
INSERT [dbo].[ViTriThuoc] VALUES (N'VT-0008', N'VT-KHO-02', N'Kho dự trữ - Tủ 02', N'Kho dự trữ', N'TU-02', N'Tủ hàng cồng kềnh', 1, 2, N'Siro, chai, mỹ phẩm kích thước lớn', N'HOAT_DONG')
GO


INSERT [dbo].[CauHinhTichDiem] VALUES (10000, 1, 100, 100)

INSERT [dbo].[KhuyenMai] VALUES (N'KM-2024-0001', N'Khuyến mãi Mùa Hè', N'Giảm giá 10% cho toàn bộ hóa đơn từ 500k', CAST(N'2026-04-30T20:49:44.0400000' AS DateTime2), CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), CAST(N'2026-12-31T00:00:00.0000000' AS DateTime2), N'HOAT_DONG')
INSERT [dbo].[KhuyenMai] VALUES (N'KM-2024-0011', N'Khuyến mãi Giảm 5%', N'Giảm 5% cho hóa đơn từ 200.000đ', CAST(N'2026-04-30T20:56:30.6066667' AS DateTime2), CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), CAST(N'2026-12-31T00:00:00.0000000' AS DateTime2), N'HOAT_DONG')
INSERT [dbo].[KhuyenMai] VALUES (N'KM-2024-0012', N'Khuyến mãi Giảm 10%', N'Giảm 10% cho hóa đơn từ 500.000đ', CAST(N'2026-04-30T20:56:30.6100000' AS DateTime2), CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), CAST(N'2026-12-31T00:00:00.0000000' AS DateTime2), N'HOAT_DONG')
INSERT [dbo].[KhuyenMai] VALUES (N'KM-2024-0013', N'Siêu sale Giảm 15%', N'Giảm 15% cho hóa đơn từ 1.000.000đ', CAST(N'2026-04-30T20:56:30.6100000' AS DateTime2), CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), CAST(N'2026-12-31T00:00:00.0000000' AS DateTime2), N'HOAT_DONG')
INSERT [dbo].[KhuyenMai] VALUES (N'KM-2024-0014', N'Mua 3 tặng 1 Paracetamol', N'Mua 3 Paracetamol tặng 1 Paracetamol', CAST(N'2026-04-30T20:56:30.6100000' AS DateTime2), CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), CAST(N'2026-12-31T00:00:00.0000000' AS DateTime2), N'KHONG_HOAT_DONG')
INSERT [dbo].[KhuyenMai] VALUES (N'KM-2024-0015', N'Tri ân khách hàng VIP', N'Giảm 20% cho hóa đơn từ 2.000.000đ', CAST(N'2026-04-30T20:56:30.6100000' AS DateTime2), CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), CAST(N'2026-12-31T00:00:00.0000000' AS DateTime2), N'HOAT_DONG')
INSERT [dbo].[KhuyenMai] VALUES (N'KM-2024-0016', N'Combo dị ứng mùa lạnh', N'Mua 2 Cetirizine tặng 1 Xịt mũi', CAST(N'2026-04-30T20:56:30.6100000' AS DateTime2), CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), CAST(N'2026-12-31T00:00:00.0000000' AS DateTime2), N'HOAT_DONG')
INSERT [dbo].[KhuyenMai] VALUES (N'KM-2024-0017', N'Ngày hội sức khỏe', N'Giảm 8% cho hóa đơn từ 300.000đ', CAST(N'2026-04-30T20:56:30.6100000' AS DateTime2), CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), CAST(N'2026-12-31T00:00:00.0000000' AS DateTime2), N'HOAT_DONG')
INSERT [dbo].[KhuyenMai] VALUES (N'KM-2024-0018', N'Tuần lễ vàng', N'Giảm 12% cho hóa đơn từ 800.000đ', CAST(N'2026-04-30T20:56:30.6133333' AS DateTime2), CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), CAST(N'2026-12-31T00:00:00.0000000' AS DateTime2), N'HOAT_DONG')
INSERT [dbo].[KhuyenMai] VALUES (N'KM-2024-0019', N'Tăng đề kháng Mùa Dịch', N'Mua 5 tặng 1 Vitamin C Sủi', CAST(N'2026-04-30T20:56:30.6133333' AS DateTime2), CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), CAST(N'2026-12-31T00:00:00.0000000' AS DateTime2), N'HOAT_DONG')
INSERT [dbo].[KhuyenMai] VALUES (N'KM-2024-0020', N'Mua buôn giảm sốc', N'Giảm 25% cho hóa đơn từ 5.000.000đ', CAST(N'2026-04-30T20:56:30.6133333' AS DateTime2), CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), CAST(N'2026-12-31T00:00:00.0000000' AS DateTime2), N'HOAT_DONG')

INSERT [dbo].[SanPham] VALUES (N'SP2024-0001', N'THUOC_KE_DON', N'VIEN_NANG', N'Amoxicillin 500mg', N'Amox 500', N'Pymepharco', N'Amoxicillin trihydrate', CAST(5.00 AS Decimal(18, 2)), N'500mg', N'Kháng sinh nhóm penicillin', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(2000.00 AS Decimal(18, 2)), N'8934663100017', N'Ho – Đờm – Viêm họng')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0002', N'THUOC_KE_DON', N'VIEN_NEN', N'Cefuroxime 500mg', N'Cefu 500', N'Stada VN', N'Cefuroxime axetil', CAST(5.00 AS Decimal(18, 2)), N'500mg', N'Kháng sinh cephalosporin', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(5000.00 AS Decimal(18, 2)), N'8934663100024', N'Ho – Đờm – Viêm họng')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0003', N'THUOC_KE_DON', N'VIEN_NEN', N'Metformin 500mg', N'Metf 500', N'Traphaco', N'Metformin hydrochloride', CAST(5.00 AS Decimal(18, 2)), N'500mg', N'Điều trị đái tháo đường', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(800.00 AS Decimal(18, 2)), N'8934663100031', N'Đái tháo đường')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0004', N'THUOC_KE_DON', N'VIEN_NEN', N'Losartan 50mg', N'Losar 50', N'Imexpharm', N'Losartan kali', CAST(5.00 AS Decimal(18, 2)), N'50mg', N'Thuốc hạ huyết áp', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(1500.00 AS Decimal(18, 2)), N'8934663100048', N'Tim mạch – Huyết áp')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0005', N'THUOC_KE_DON', N'HON_DICH', N'Augmentin 250mg/5ml Siro', N'Aug Siro', N'GSK', N'Amoxicillin + Acid clavulanic', CAST(5.00 AS Decimal(18, 2)), N'250mg/5ml', N'Kháng sinh cho trẻ em', N'Chai', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(90000.00 AS Decimal(18, 2)), N'8934663100086', N'Ho – Đờm – Viêm họng')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0006', N'THUOC_KE_DON', N'HON_DICH', N'Azithromycin 200mg/5ml', N'Azith Siro', N'Pymepharco', N'Azithromycin dihydrate', CAST(5.00 AS Decimal(18, 2)), N'200mg/5ml', N'Kháng sinh macrolide', N'Chai', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(75000.00 AS Decimal(18, 2)), N'8934663100093', N'Ho – Đờm – Viêm họng')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0007', N'THUOC_KE_DON', N'VIEN_NEN', N'Amlodipine 5mg', N'Amlo 5', N'Stada VN', N'Amlodipine besylate', CAST(5.00 AS Decimal(18, 2)), N'5mg', N'Điều trị tăng huyết áp', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(2500.00 AS Decimal(18, 2)), N'8934663100109', N'Tim mạch – Huyết áp')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0008', N'THUOC_KE_DON', N'VIEN_NEN', N'Atorvastatin 20mg', N'Ator 20', N'Imexpharm', N'Atorvastatin calcium', CAST(5.00 AS Decimal(18, 2)), N'20mg', N'Thuốc hạ mỡ máu', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(3500.00 AS Decimal(18, 2)), N'8934663100116', N'Tim mạch – Huyết áp')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0009', N'THUOC_KHONG_KE_DON', N'VIEN_NEN', N'Paracetamol 500mg', N'Para 500', N'Domesco', N'Paracetamol', CAST(5.00 AS Decimal(18, 2)), N'500mg', N'Giảm đau, hạ sốt', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(400.00 AS Decimal(18, 2)), N'8934673100014', N'Đau đầu – Giảm đau – Hạ sốt')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0010', N'THUOC_KHONG_KE_DON', N'VIEN_NEN', N'Ibuprofen 400mg', N'Ibup 400', N'OPV', N'Ibuprofen', CAST(5.00 AS Decimal(18, 2)), N'400mg', N'Kháng viêm không steroid', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(600.00 AS Decimal(18, 2)), N'8934673100021', N'Đau đầu – Giảm đau – Hạ sốt')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0011', N'THUOC_KHONG_KE_DON', N'VIEN_SUI', N'Vitamin C 1000mg Sủi', N'VitC 1000', N'DHG Pharma', N'Acid ascorbic', CAST(5.00 AS Decimal(18, 2)), N'1000mg', N'Tăng sức đề kháng', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(2167.00 AS Decimal(18, 2)), N'8934673100038', N'Thuốc bổ – Vitamin – Khoáng chất')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0012', N'THUOC_KHONG_KE_DON', N'VIEN_NEN', N'Cetirizine 10mg', N'Cetir 10', N'Stada VN', N'Cetirizine hydrochloride', CAST(5.00 AS Decimal(18, 2)), N'10mg', N'Chống dị ứng', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(600.00 AS Decimal(18, 2)), N'8934673100045', N'Dị ứng – Mẩn ngứa – Mề đay')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0013', N'THUOC_KHONG_KE_DON', N'THUOC_BOT', N'Oresol Cam', N'ORS Cam', N'Vinpharco', N'Glucose + Natri Clorid', CAST(5.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Bù nước và điện giải', N'Gói', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(3000.00 AS Decimal(18, 2)), N'8934673100069', N'Dạ dày – Tiêu hóa – Đại tràng')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0014', N'THUOC_KHONG_KE_DON', N'DUNG_DICH', N'Xịt mũi Naphazoline 0.05%', N'Xịt Naphaz', N'Pharmedic', N'Naphazoline hydrochloride', CAST(5.00 AS Decimal(18, 2)), N'0.05%', N'Giảm ngạt mũi', N'Chai', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(35000.00 AS Decimal(18, 2)), N'8934673100052', N'Dị ứng – Mẩn ngứa – Mề đay')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0015', N'THUOC_KHONG_KE_DON', N'VIEN_NEN', N'Domperidon 10mg', N'Domp 10', N'Pymepharco', N'Domperidone', CAST(5.00 AS Decimal(18, 2)), N'10mg', N'Chống nôn', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(1333.00 AS Decimal(18, 2)), N'8934673100076', N'Dạ dày – Tiêu hóa – Đại tràng')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0016', N'THUOC_KHONG_KE_DON', N'VIEN_NANG', N'Omeprazole 20mg', N'Ome 20', N'Traphaco', N'Omeprazole', CAST(5.00 AS Decimal(18, 2)), N'20mg', N'Điều trị loét dạ dày', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(1733.00 AS Decimal(18, 2)), N'8934673100083', N'Dạ dày – Tiêu hóa – Đại tràng')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0017', N'THUOC_KHONG_KE_DON', N'DUNG_DICH', N'Siro Ho Bổ Phế', N'Siro Ho BP', N'Traphaco', N'Thảo dược', CAST(5.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Giảm ho', N'Chai', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(55000.00 AS Decimal(18, 2)), N'8934673100090', N'Ho – Đờm – Viêm họng')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0018', N'THUOC_KHONG_KE_DON', N'THUOC_BOT', N'Smecta 3g Bột', N'Smecta', N'Ipsen', N'Diosmectite', CAST(5.00 AS Decimal(18, 2)), N'3g', N'Điều trị tiêu chảy', N'Gói', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(11000.00 AS Decimal(18, 2)), N'8934673100106', N'Dạ dày – Tiêu hóa – Đại tràng')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0019', N'THUOC_KHONG_KE_DON', N'VIEN_NEN', N'Loratadine 10mg', N'Lorat 10', N'DHG Pharma', N'Loratadine', CAST(5.00 AS Decimal(18, 2)), N'10mg', N'Kháng histamine', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(533.00 AS Decimal(18, 2)), N'8934673100168', N'Dị ứng – Mẩn ngứa – Mề đay')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0020', N'THUOC_KHONG_KE_DON', N'VIEN_NANG', N'Esomeprazole 20mg', N'Esome 20', N'AstraZeneca', N'Esomeprazole magnesium', CAST(5.00 AS Decimal(18, 2)), N'20mg', N'Điều trị trào ngược', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(1733.00 AS Decimal(18, 2)), N'8934673100175', N'Dạ dày – Tiêu hóa – Đại tràng')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0021', N'THUC_PHAM_CHUC_NANG', N'VIEN_NANG', N'Vitamin D3 K2 2000IU', N'VitD3K2', N'Nature Made', N'Cholecalciferol', CAST(10.00 AS Decimal(18, 2)), N'2000IU', N'Hỗ trợ xương khớp', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(7333.00 AS Decimal(18, 2)), N'8934673100113', N'Thuốc bổ – Vitamin – Khoáng chất')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0022', N'THUC_PHAM_CHUC_NANG', N'VIEN_NANG', N'Omega-3 Fish Oil 1000mg', N'Omega3 1000', N'Nature Made', N'EPA + DHA', CAST(10.00 AS Decimal(18, 2)), N'1000mg', N'Hỗ trợ tim mạch', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(9333.00 AS Decimal(18, 2)), N'8934673100120', N'Thuốc bổ – Vitamin – Khoáng chất')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0023', N'THUC_PHAM_CHUC_NANG', N'VIEN_NEN', N'Canxi Nano 500mg', N'Canxi Nano', N'DHG Pharma', N'Calcium carbonate', CAST(10.00 AS Decimal(18, 2)), N'500mg', N'Bổ sung canxi', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(2833.00 AS Decimal(18, 2)), N'8934673100137', N'Thuốc bổ – Vitamin – Khoáng chất')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0024', N'THUC_PHAM_CHUC_NANG', N'DUNG_DICH', N'Collagen Peptide 5000mg', N'Collagen 5K', N'Kinoko VN', N'Collagen hydrolyzed', CAST(10.00 AS Decimal(18, 2)), N'5000mg', N'Làm đẹp da', N'Gói', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(42000.00 AS Decimal(18, 2)), N'4005900117908', N'Dược mỹ phẩm – Da liễu')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0025', N'THUC_PHAM_CHUC_NANG', N'KEO_NGAM', N'Melatonin 5mg', N'Melat 5', N'Natrol USA', N'Melatonin', CAST(10.00 AS Decimal(18, 2)), N'5mg', N'Hỗ trợ giấc ngủ', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(14000.00 AS Decimal(18, 2)), N'8934673100144', N'Thuốc bổ – Vitamin – Khoáng chất')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0026', N'THUC_PHAM_CHUC_NANG', N'VIEN_NEN', N'Magie B6 Úc', N'Mg B6', N'Blackmores', N'Magnesium + Vitamin B6', CAST(10.00 AS Decimal(18, 2)), N'500mg', N'Giảm căng thẳng', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(6500.00 AS Decimal(18, 2)), N'8934673100151', N'Thuốc bổ – Vitamin – Khoáng chất')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0027', N'THUC_PHAM_CHUC_NANG', N'THUOC_BOT', N'Probiotics 10 tỷ CFU', N'Probio 10B', N'Yakult VN', N'Lactobacillus', CAST(10.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Hỗ trợ tiêu hóa', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(5500.00 AS Decimal(18, 2)), N'8934673100229', N'Thuốc bổ – Vitamin – Khoáng chất')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0028', N'MY_PHAM', N'HON_DICH', N'Kem dưỡng da Eucerin Q10', N'Eucerin Q10', N'Eucerin', N'Chưa cập nhật', CAST(10.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Chống lão hóa', N'Hộp', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(650000.00 AS Decimal(18, 2)), N'4005900272935', N'Dược mỹ phẩm – Da liễu')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0029', N'MY_PHAM', N'HON_DICH', N'Kem chống nắng La Roche', N'LRP SPF50', N'La Roche-Posay', N'Chưa cập nhật', CAST(10.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Bảo vệ da nhạy cảm', N'Tuýp', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(580000.00 AS Decimal(18, 2)), N'3337872413148', N'Dược mỹ phẩm – Da liễu')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0030', N'MY_PHAM', N'DUNG_DICH', N'Sữa rửa mặt CeraVe', N'CeraVe Foam', N'CeraVe', N'Chưa cập nhật', CAST(10.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Sữa rửa mặt tạo bọt', N'Chai', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(320000.00 AS Decimal(18, 2)), N'3606000594227', N'Dược mỹ phẩm – Da liễu')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0031', N'MY_PHAM', N'DUNG_DICH', N'Nước tẩy trang Bioderma', N'Bioderma', N'Bioderma', N'Chưa cập nhật', CAST(10.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Tẩy trang dịu nhẹ', N'Chai', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(280000.00 AS Decimal(18, 2)), N'3701129801420', N'Dược mỹ phẩm – Da liễu')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0032', N'MY_PHAM', N'DUNG_DICH', N'Toner Paula Niacinamide', N'Paula Toner', N'Paula Choice', N'Niacinamide', CAST(10.00 AS Decimal(18, 2)), N'10%', N'Làm sáng da', N'Chai', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(850000.00 AS Decimal(18, 2)), N'0761591032015', N'Dược mỹ phẩm – Da liễu')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0033', N'THUOC_KHONG_KE_DON', N'THUOC_NHO_GIOT', N'V.Rohto Vitamin', N'V.Rohto', N'Rohto', N'Vitamin B5, B6', CAST(10.00 AS Decimal(18, 2)), N'13ml', N'Giảm mỏi mắt', N'Chai', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(52000.00 AS Decimal(18, 2)), N'4987241115532', N'Mắt – Tai – Mũi')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0034', N'THUOC_KHONG_KE_DON', N'THUOC_NHO_GIOT', N'Thuốc nhỏ mắt Osla', N'Osla', N'MerAP', N'Natri clorid', CAST(5.00 AS Decimal(18, 2)), N'15ml', N'Rửa mắt', N'Chai', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(22000.00 AS Decimal(18, 2)), N'8936077610014', N'Mắt – Tai – Mũi')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0035', N'MY_PHAM', N'SUC_MIENG', N'Listerine Cool Mint', N'Listerine', N'Johnson', N'Thymol', CAST(10.00 AS Decimal(18, 2)), N'750ml', N'Hơi thở thơm mát', N'Chai', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(115000.00 AS Decimal(18, 2)), N'0761591032022', N'Dược mỹ phẩm – Da liễu')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0036', N'MY_PHAM', N'SUC_MIENG', N'Betadine Gargle', N'Betadine', N'Mundipharma', N'Povidone-Iodine', CAST(10.00 AS Decimal(18, 2)), N'125ml', N'Sát khuẩn miệng', N'Chai', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(68000.00 AS Decimal(18, 2)), N'5038483381038', N'Dược mỹ phẩm – Da liễu')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0037', N'THUOC_KHONG_KE_DON', N'KEO_NGAM', N'Strepsils Cool', N'Strepsils', N'Reckitt', N'Dichlorobenzyl', CAST(10.00 AS Decimal(18, 2)), N'1.2mg', N'Giảm đau họng', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(8000.00 AS Decimal(18, 2)), N'8934673100205', N'Dị ứng – Mẩn ngứa – Mề đay')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0038', N'THUOC_KHONG_KE_DON', N'VIEN_SUI', N'Efferalgan 500mg', N'Efferalgan', N'UPSA', N'Paracetamol', CAST(5.00 AS Decimal(18, 2)), N'500mg', N'Hạ sốt nhanh', N'Viên', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(3100.00 AS Decimal(18, 2)), N'8934673100182', N'Đau đầu – Giảm đau – Hạ sốt')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0039', N'THUOC_KHONG_KE_DON', N'THUOC_BOT', N'Hapacol 150', N'Hapacol 150', N'DHG Pharma', N'Paracetamol', CAST(5.00 AS Decimal(18, 2)), N'150mg', N'Hạ sốt cho trẻ', N'Gói', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(3400.00 AS Decimal(18, 2)), N'8934673100199', N'Sốt – Cảm cúm – Sổ mũi')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0040', N'THUOC_KE_DON', N'HON_DICH', N'Phosphalugel', N'Chữ P', N'Astellas', N'Aluminum phosphate', CAST(5.00 AS Decimal(18, 2)), N'20%', N'Kháng axit dạ dày', N'Gói', CAST(N'2026-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(11000.00 AS Decimal(18, 2)), N'8934673100212', N'Dạ dày – Tiêu hóa – Đại tràng')
-- ... (Các dòng INSERT [dbo].[SanPham] ...)
GO

-- ==============================================================================
-- CHÈN LIỀU MẪU VÀ CHI TIẾT LIỀU MẪU (Phải đặt SAU KHI đã có SanPham)
-- ==============================================================================
INSERT INTO [dbo].[LieuMau] VALUES 
(N'LM-0001', N'Liều ho có đờm, sổ mũi', N'Hô hấp', N'Uống 3 ngày: Kháng sinh, Dị ứng, Siro'),
(N'LM-0002', N'Liều tiêu chảy, bù nước', N'Tiêu hóa', N'Cầm tiêu chảy, bù nước điện giải');
GO

-- Chi tiết Liều LM-0001
INSERT INTO [dbo].[ChiTietLieuMau] VALUES 
(N'LM-0001', N'SP2024-0001', 6), -- Amoxicillin 500mg
(N'LM-0001', N'SP2024-0012', 3), -- Cetirizine 10mg
(N'LM-0001', N'SP2024-0017', 1); -- Siro Ho BP

-- Chi tiết Liều LM-0002
INSERT INTO [dbo].[ChiTietLieuMau] VALUES 
(N'LM-0002', N'SP2024-0018', 6), -- Smecta 3g Bột
(N'LM-0002', N'SP2024-0013', 3); -- Oresol Cam
GO

-- ==============================================================================
-- TƯƠNG THÍCH TÊN BẢNG MẪU LIỀU
-- Code DAO hiện đang gọi MauLieu, trong khi database gốc đặt là LieuMau.
-- Tạo VIEW để cả 2 tên đều chạy được mà không phải sửa code Java.
-- ==============================================================================
GO

-- ==============================================================================
-- B. BẢNG CÓ KHÓA NGOẠI
-- ...
-- ==============================================================================
-- B. BẢNG CÓ KHÓA NGOẠI
-- tenDangNhap = mã nhân viên (QL-0001, DS-0001, ...)
-- Mật khẩu ở đây là dạng THÔ - sẽ tự động được băm khi đăng nhập lần đầu
-- ==============================================================================
INSERT [dbo].[TaiKhoan] VALUES (N'TK0001', N'QL-0001', N'ADMIN', N'QL-0001', N'12345678')
INSERT [dbo].[TaiKhoan] VALUES (N'TK0002', N'DS-0001', N'STAFF', N'DS-0001', N'23652461')
INSERT [dbo].[TaiKhoan] VALUES (N'TK0003', N'DS-0002', N'STAFF', N'DS-0002', N'23653611')
INSERT [dbo].[TaiKhoan] VALUES (N'TK0004', N'DS-0003', N'STAFF', N'DS-0003', N'23652641')
INSERT [dbo].[TaiKhoan] VALUES (N'TK0005', N'DS-0004', N'STAFF', N'DS-0004', N'23652091')
INSERT [dbo].[TaiKhoan] VALUES (N'TK0006', N'DS-0005', N'STAFF', N'DS-0005', N'23654991')

INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0001', N'SP2024-0001', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(2000.00 AS Decimal(18, 2)), N'DVDL00010001')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0002', N'SP2024-0001', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(55000.00 AS Decimal(18, 2)), N'DVDL00010002')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0003', N'SP2024-0002', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(5000.00 AS Decimal(18, 2)), N'DVDL00020003')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0004', N'SP2024-0002', N'Hộp', CAST(14.00 AS Decimal(18, 2)), CAST(65000.00 AS Decimal(18, 2)), N'DVDL00020004')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0005', N'SP2024-0003', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(800.00 AS Decimal(18, 2)), N'DVDL00030005')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0006', N'SP2024-0003', N'Hộp', CAST(100.00 AS Decimal(18, 2)), CAST(75000.00 AS Decimal(18, 2)), N'DVDL00030006')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0007', N'SP2024-0004', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(1500.00 AS Decimal(18, 2)), N'DVDL00040007')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0008', N'SP2024-0004', N'Hộp', CAST(28.00 AS Decimal(18, 2)), CAST(40000.00 AS Decimal(18, 2)), N'DVDL00040008')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0009', N'SP2024-0005', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(90000.00 AS Decimal(18, 2)), N'DVDL00050009')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0010', N'SP2024-0006', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(75000.00 AS Decimal(18, 2)), N'DVDL00060010')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0011', N'SP2024-0007', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(2500.00 AS Decimal(18, 2)), N'DVDL00070011')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0012', N'SP2024-0007', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(70000.00 AS Decimal(18, 2)), N'DVDL00070012')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0013', N'SP2024-0008', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(3500.00 AS Decimal(18, 2)), N'DVDL00080013')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0014', N'SP2024-0008', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(98000.00 AS Decimal(18, 2)), N'DVDL00080014')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0015', N'SP2024-0009', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(400.00 AS Decimal(18, 2)), N'DVDL00090015')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0016', N'SP2024-0009', N'Hộp', CAST(100.00 AS Decimal(18, 2)), CAST(40000.00 AS Decimal(18, 2)), N'DVDL00090016')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0017', N'SP2024-0010', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(600.00 AS Decimal(18, 2)), N'DVDL00100017')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0018', N'SP2024-0010', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(18000.00 AS Decimal(18, 2)), N'DVDL00100018')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0019', N'SP2024-0011', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(2167.00 AS Decimal(18, 2)), N'DVDL00110019')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0020', N'SP2024-0011', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(65000.00 AS Decimal(18, 2)), N'DVDL00110020')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0021', N'SP2024-0012', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(600.00 AS Decimal(18, 2)), N'DVDL00120021')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0022', N'SP2024-0012', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(18000.00 AS Decimal(18, 2)), N'DVDL00120022')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0023', N'SP2024-0013', N'Gói', CAST(1.00 AS Decimal(18, 2)), CAST(3000.00 AS Decimal(18, 2)), N'DVDL00130023')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0024', N'SP2024-0013', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(90000.00 AS Decimal(18, 2)), N'DVDL00130024')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0025', N'SP2024-0014', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(35000.00 AS Decimal(18, 2)), N'DVDL00140025')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0026', N'SP2024-0015', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(1333.00 AS Decimal(18, 2)), N'DVDL00150026')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0027', N'SP2024-0015', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(40000.00 AS Decimal(18, 2)), N'DVDL00150027')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0028', N'SP2024-0016', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(1733.00 AS Decimal(18, 2)), N'DVDL00160028')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0029', N'SP2024-0016', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(52000.00 AS Decimal(18, 2)), N'DVDL00160029')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0030', N'SP2024-0017', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(55000.00 AS Decimal(18, 2)), N'DVDL00170030')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0031', N'SP2024-0018', N'Gói', CAST(1.00 AS Decimal(18, 2)), CAST(11000.00 AS Decimal(18, 2)), N'DVDL00180031')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0032', N'SP2024-0018', N'Hộp', CAST(20.00 AS Decimal(18, 2)), CAST(220000.00 AS Decimal(18, 2)), N'DVDL00180032')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0033', N'SP2024-0019', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(533.00 AS Decimal(18, 2)), N'DVDL00190033')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0034', N'SP2024-0019', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(16000.00 AS Decimal(18, 2)), N'DVDL00190034')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0035', N'SP2024-0020', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(1733.00 AS Decimal(18, 2)), N'DVDL00200035')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0036', N'SP2024-0020', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(52000.00 AS Decimal(18, 2)), N'DVDL00200036')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0037', N'SP2024-0021', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(7333.00 AS Decimal(18, 2)), N'DVDL00210037')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0038', N'SP2024-0021', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(220000.00 AS Decimal(18, 2)), N'DVDL00210038')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0039', N'SP2024-0022', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(9333.00 AS Decimal(18, 2)), N'DVDL00220039')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0040', N'SP2024-0022', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(280000.00 AS Decimal(18, 2)), N'DVDL00220040')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0041', N'SP2024-0023', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(2833.00 AS Decimal(18, 2)), N'DVDL00230041')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0042', N'SP2024-0023', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(85000.00 AS Decimal(18, 2)), N'DVDL00230042')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0043', N'SP2024-0024', N'Gói', CAST(1.00 AS Decimal(18, 2)), CAST(42000.00 AS Decimal(18, 2)), N'DVDL00240043')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0044', N'SP2024-0025', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(14000.00 AS Decimal(18, 2)), N'DVDL00250044')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0045', N'SP2024-0025', N'Hộp', CAST(20.00 AS Decimal(18, 2)), CAST(280000.00 AS Decimal(18, 2)), N'DVDL00250045')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0046', N'SP2024-0026', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(6500.00 AS Decimal(18, 2)), N'DVDL00260046')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0047', N'SP2024-0026', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(195000.00 AS Decimal(18, 2)), N'DVDL00260047')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0048', N'SP2024-0027', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(5500.00 AS Decimal(18, 2)), N'DVDL00270048')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0049', N'SP2024-0027', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(165000.00 AS Decimal(18, 2)), N'DVDL00270049')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0050', N'SP2024-0028', N'Hộp', CAST(1.00 AS Decimal(18, 2)), CAST(650000.00 AS Decimal(18, 2)), N'DVDL00280050')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0051', N'SP2024-0029', N'Tuýp', CAST(1.00 AS Decimal(18, 2)), CAST(580000.00 AS Decimal(18, 2)), N'DVDL00290051')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0052', N'SP2024-0030', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(320000.00 AS Decimal(18, 2)), N'DVDL00300052')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0053', N'SP2024-0031', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(280000.00 AS Decimal(18, 2)), N'DVDL00310053')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0054', N'SP2024-0032', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(850000.00 AS Decimal(18, 2)), N'DVDL00320054')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0055', N'SP2024-0033', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(52000.00 AS Decimal(18, 2)), N'DVDL00330055')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0056', N'SP2024-0034', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(22000.00 AS Decimal(18, 2)), N'DVDL00340056')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0057', N'SP2024-0035', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(115000.00 AS Decimal(18, 2)), N'DVDL00350057')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0058', N'SP2024-0036', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(68000.00 AS Decimal(18, 2)), N'DVDL00360058')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0059', N'SP2024-0037', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(8000.00 AS Decimal(18, 2)), N'DVDL00370059')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0060', N'SP2024-0037', N'Hộp', CAST(10.00 AS Decimal(18, 2)), CAST(80000.00 AS Decimal(18, 2)), N'DVDL00370060')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0061', N'SP2024-0038', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(3100.00 AS Decimal(18, 2)), N'DVDL00380061')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0062', N'SP2024-0038', N'Hộp', CAST(20.00 AS Decimal(18, 2)), CAST(62000.00 AS Decimal(18, 2)), N'DVDL00380062')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0063', N'SP2024-0039', N'Gói', CAST(1.00 AS Decimal(18, 2)), CAST(3400.00 AS Decimal(18, 2)), N'DVDL00390063')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0064', N'SP2024-0039', N'Hộp', CAST(20.00 AS Decimal(18, 2)), CAST(68000.00 AS Decimal(18, 2)), N'DVDL00390064')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0065', N'SP2024-0040', N'Gói', CAST(1.00 AS Decimal(18, 2)), CAST(11000.00 AS Decimal(18, 2)), N'DVDL00400065')
INSERT [dbo].[DonViDoLuong] VALUES (N'DVL-0066', N'SP2024-0040', N'Hộp', CAST(10.00 AS Decimal(18, 2)), CAST(110000.00 AS Decimal(18, 2)), N'DVDL00400066')

INSERT [dbo].[DieuKienKhuyenMai] VALUES (N'DK-0001', N'GIA_TRI', N'HOA_DON', CAST(500000.00 AS Decimal(18, 2)), N'KM-2024-0001')
INSERT [dbo].[DieuKienKhuyenMai] VALUES (N'DK-0011', N'GIA_TRI', N'HOA_DON', CAST(200000.00 AS Decimal(18, 2)), N'KM-2024-0011')
INSERT [dbo].[DieuKienKhuyenMai] VALUES (N'DK-0012', N'GIA_TRI', N'HOA_DON', CAST(500000.00 AS Decimal(18, 2)), N'KM-2024-0012')
INSERT [dbo].[DieuKienKhuyenMai] VALUES (N'DK-0013', N'GIA_TRI', N'HOA_DON', CAST(1000000.00 AS Decimal(18, 2)), N'KM-2024-0013')
INSERT [dbo].[DieuKienKhuyenMai] VALUES (N'DK-0014', N'SO_LUONG', N'SAN_PHAM', CAST(3.00 AS Decimal(18, 2)), N'KM-2024-0014')
INSERT [dbo].[DieuKienKhuyenMai] VALUES (N'DK-0015', N'GIA_TRI', N'HOA_DON', CAST(2000000.00 AS Decimal(18, 2)), N'KM-2024-0015')
INSERT [dbo].[DieuKienKhuyenMai] VALUES (N'DK-0016', N'SO_LUONG', N'SAN_PHAM', CAST(2.00 AS Decimal(18, 2)), N'KM-2024-0016')
INSERT [dbo].[DieuKienKhuyenMai] VALUES (N'DK-0017', N'GIA_TRI', N'HOA_DON', CAST(300000.00 AS Decimal(18, 2)), N'KM-2024-0017')
INSERT [dbo].[DieuKienKhuyenMai] VALUES (N'DK-0018', N'GIA_TRI', N'HOA_DON', CAST(800000.00 AS Decimal(18, 2)), N'KM-2024-0018')
INSERT [dbo].[DieuKienKhuyenMai] VALUES (N'DK-0019', N'SO_LUONG', N'SAN_PHAM', CAST(5.00 AS Decimal(18, 2)), N'KM-2024-0019')
INSERT [dbo].[DieuKienKhuyenMai] VALUES (N'DK-0020', N'GIA_TRI', N'HOA_DON', CAST(5000000.00 AS Decimal(18, 2)), N'KM-2024-0020')

INSERT [dbo].[HinhThucKhuyenMai] VALUES (N'HT-0001', N'GIAM_THEO_PHAN_TRAM', N'HOA_DON', N'Giảm 10%', CAST(10.00 AS Decimal(18, 2)), NULL, NULL, N'KM-2024-0001', NULL, NULL, NULL, NULL, NULL, N'', N'')
INSERT [dbo].[HinhThucKhuyenMai] VALUES (N'HT-0011', N'GIAM_THEO_PHAN_TRAM', N'HOA_DON', N'Giảm 5%', CAST(5.00 AS Decimal(18, 2)), NULL, NULL, N'KM-2024-0011', NULL, NULL, NULL, NULL, NULL, NULL, NULL)
INSERT [dbo].[HinhThucKhuyenMai] VALUES (N'HT-0012', N'GIAM_THEO_PHAN_TRAM', N'HOA_DON', N'Giảm 10%', CAST(10.00 AS Decimal(18, 2)), NULL, NULL, N'KM-2024-0012', NULL, NULL, NULL, NULL, NULL, NULL, NULL)
INSERT [dbo].[HinhThucKhuyenMai] VALUES (N'HT-0013', N'GIAM_THEO_PHAN_TRAM', N'HOA_DON', N'Giảm 15%', CAST(15.00 AS Decimal(18, 2)), NULL, NULL, N'KM-2024-0013', NULL, NULL, NULL, NULL, NULL, NULL, NULL)
INSERT [dbo].[HinhThucKhuyenMai] VALUES (N'HT-0014', N'SAN_PHAM_KEM_THEO', N'SAN_PHAM', N'Tặng 1 Paracetamol', CAST(0.00 AS Decimal(18, 2)), NULL, NULL, N'KM-2024-0014', NULL, N'SP2024-0009', 3, N'SP2024-0009', 1, NULL, NULL)
INSERT [dbo].[HinhThucKhuyenMai] VALUES (N'HT-0015', N'GIAM_THEO_PHAN_TRAM', N'HOA_DON', N'Giảm 20%', CAST(20.00 AS Decimal(18, 2)), NULL, NULL, N'KM-2024-0015', NULL, NULL, NULL, NULL, NULL, NULL, NULL)
INSERT [dbo].[HinhThucKhuyenMai] VALUES (N'HT-0016', N'SAN_PHAM_KEM_THEO', N'SAN_PHAM', N'Tặng 1 Xịt mũi', CAST(0.00 AS Decimal(18, 2)), NULL, NULL, N'KM-2024-0016', NULL, N'SP2024-0012', 2, N'SP2024-0014', 1, NULL, NULL)
INSERT [dbo].[HinhThucKhuyenMai] VALUES (N'HT-0017', N'GIAM_THEO_PHAN_TRAM', N'HOA_DON', N'Giảm 8%', CAST(8.00 AS Decimal(18, 2)), NULL, NULL, N'KM-2024-0017', NULL, NULL, NULL, NULL, NULL, NULL, NULL)
INSERT [dbo].[HinhThucKhuyenMai] VALUES (N'HT-0018', N'GIAM_THEO_PHAN_TRAM', N'HOA_DON', N'Giảm 12%', CAST(12.00 AS Decimal(18, 2)), NULL, NULL, N'KM-2024-0018', NULL, NULL, NULL, NULL, NULL, NULL, NULL)
INSERT [dbo].[HinhThucKhuyenMai] VALUES (N'HT-0019', N'SAN_PHAM_KEM_THEO', N'SAN_PHAM', N'Tặng 1 Vitamin C', CAST(0.00 AS Decimal(18, 2)), NULL, NULL, N'KM-2024-0019', NULL, N'SP2024-0011', 5, N'SP2024-0011', 1, NULL, NULL)
INSERT [dbo].[HinhThucKhuyenMai] VALUES (N'HT-0020', N'GIAM_THEO_PHAN_TRAM', N'HOA_DON', N'Giảm 25%', CAST(25.00 AS Decimal(18, 2)), NULL, NULL, N'KM-2024-0020', NULL, NULL, NULL, NULL, NULL, NULL, NULL)

INSERT [dbo].[LoHang] VALUES (N'LH-0001', N'LOT-AMX-260101', N'SP2024-0001', 200, CAST(1400.00 AS Decimal(18,2)), CAST(N'2026-08-31' AS DateTime2), N'CON_HANG', CAST(N'2026-01-05T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0001')
INSERT [dbo].[LoHang] VALUES (N'LH-0002', N'LOT-CEF-260102', N'SP2024-0002', 150, CAST(3500.00 AS Decimal(18,2)), CAST(N'2026-09-30' AS DateTime2), N'CON_HANG', CAST(N'2026-01-08T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0002')
INSERT [dbo].[LoHang] VALUES (N'LH-0003', N'LOT-MET-260103', N'SP2024-0003', 300, CAST(560.00 AS Decimal(18,2)), CAST(N'2026-10-31' AS DateTime2), N'CON_HANG', CAST(N'2026-01-11T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0003')
INSERT [dbo].[LoHang] VALUES (N'LH-0004', N'LOT-LOS-260104', N'SP2024-0004', 250, CAST(1050.00 AS Decimal(18,2)), CAST(N'2026-11-30' AS DateTime2), N'CON_HANG', CAST(N'2026-01-14T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0004')
INSERT [dbo].[LoHang] VALUES (N'LH-0005', N'LOT-AUG-260105', N'SP2024-0005', 100, CAST(63000.00 AS Decimal(18,2)), CAST(N'2026-12-31' AS DateTime2), N'CON_HANG', CAST(N'2026-01-17T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0005')
INSERT [dbo].[LoHang] VALUES (N'LH-0006', N'LOT-AZI-260106', N'SP2024-0006', 120, CAST(52500.00 AS Decimal(18,2)), CAST(N'2027-01-31' AS DateTime2), N'CON_HANG', CAST(N'2026-01-20T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0006')
INSERT [dbo].[LoHang] VALUES (N'LH-0007', N'LOT-AML-260107', N'SP2024-0007', 200, CAST(1750.00 AS Decimal(18,2)), CAST(N'2027-02-28' AS DateTime2), N'CON_HANG', CAST(N'2026-01-23T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0007')
INSERT [dbo].[LoHang] VALUES (N'LH-0008', N'LOT-ATO-260108', N'SP2024-0008', 180, CAST(2450.00 AS Decimal(18,2)), CAST(N'2027-03-31' AS DateTime2), N'CON_HANG', CAST(N'2026-01-26T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0008')
INSERT [dbo].[LoHang] VALUES (N'LH-0009', N'LOT-PAR-260109', N'SP2024-0009', 500, CAST(280.00 AS Decimal(18,2)), CAST(N'2027-04-30' AS DateTime2), N'CON_HANG', CAST(N'2026-01-29T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0009')
INSERT [dbo].[LoHang] VALUES (N'LH-0010', N'LOT-IBU-260110', N'SP2024-0010', 400, CAST(420.00 AS Decimal(18,2)), CAST(N'2027-05-31' AS DateTime2), N'CON_HANG', CAST(N'2026-02-01T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0010')
INSERT [dbo].[LoHang] VALUES (N'LH-0011', N'LOT-VTC-260201', N'SP2024-0011', 300, CAST(1516.90 AS Decimal(18,2)), CAST(N'2027-06-30' AS DateTime2), N'CON_HANG', CAST(N'2026-02-04T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0011')
INSERT [dbo].[LoHang] VALUES (N'LH-0012', N'LOT-CET-260202', N'SP2024-0012', 350, CAST(420.00 AS Decimal(18,2)), CAST(N'2027-07-31' AS DateTime2), N'CON_HANG', CAST(N'2026-02-07T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0012')
INSERT [dbo].[LoHang] VALUES (N'LH-0013', N'LOT-ORS-260203', N'SP2024-0013', 400, CAST(2100.00 AS Decimal(18,2)), CAST(N'2027-08-31' AS DateTime2), N'CON_HANG', CAST(N'2026-02-10T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0013')
INSERT [dbo].[LoHang] VALUES (N'LH-0014', N'LOT-NAP-260204', N'SP2024-0014', 200, CAST(24500.00 AS Decimal(18,2)), CAST(N'2027-09-30' AS DateTime2), N'CON_HANG', CAST(N'2026-02-13T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0014')
INSERT [dbo].[LoHang] VALUES (N'LH-0015', N'LOT-DOM-260205', N'SP2024-0015', 150, CAST(933.10 AS Decimal(18,2)), CAST(N'2027-10-31' AS DateTime2), N'CON_HANG', CAST(N'2026-02-16T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0015')
INSERT [dbo].[LoHang] VALUES (N'LH-0016', N'LOT-OME-260206', N'SP2024-0016', 100, CAST(1213.10 AS Decimal(18,2)), CAST(N'2027-11-30' AS DateTime2), N'CON_HANG', CAST(N'2026-02-19T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0016')
INSERT [dbo].[LoHang] VALUES (N'LH-0017', N'LOT-SIR-260207', N'SP2024-0017', 80, CAST(38500.00 AS Decimal(18,2)), CAST(N'2027-12-31' AS DateTime2), N'CON_HANG', CAST(N'2026-02-22T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0017')
INSERT [dbo].[LoHang] VALUES (N'LH-0018', N'LOT-SME-260208', N'SP2024-0018', 200, CAST(7700.00 AS Decimal(18,2)), CAST(N'2028-01-31' AS DateTime2), N'CON_HANG', CAST(N'2026-02-25T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0018')
INSERT [dbo].[LoHang] VALUES (N'LH-0019', N'LOT-LOR-260209', N'SP2024-0019', 250, CAST(373.10 AS Decimal(18,2)), CAST(N'2028-02-29' AS DateTime2), N'CON_HANG', CAST(N'2026-02-28T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0019')
INSERT [dbo].[LoHang] VALUES (N'LH-0020', N'LOT-ESO-260210', N'SP2024-0020', 120, CAST(1213.10 AS Decimal(18,2)), CAST(N'2028-03-31' AS DateTime2), N'CON_HANG', CAST(N'2026-03-03T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0020')
INSERT [dbo].[LoHang] VALUES (N'LH-0021', N'LOT-VTD-260301', N'SP2024-0021', 100, CAST(5279.76 AS Decimal(18,2)), CAST(N'2028-04-30' AS DateTime2), N'CON_HANG', CAST(N'2026-03-06T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0021')
INSERT [dbo].[LoHang] VALUES (N'LH-0022', N'LOT-OMG-260302', N'SP2024-0022', 80, CAST(6719.76 AS Decimal(18,2)), CAST(N'2028-05-31' AS DateTime2), N'CON_HANG', CAST(N'2026-03-09T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0022')
INSERT [dbo].[LoHang] VALUES (N'LH-0023', N'LOT-CAL-260303', N'SP2024-0023', 120, CAST(2039.76 AS Decimal(18,2)), CAST(N'2028-06-30' AS DateTime2), N'CON_HANG', CAST(N'2026-03-12T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0023')
INSERT [dbo].[LoHang] VALUES (N'LH-0024', N'LOT-COL-260304', N'SP2024-0024', 60, CAST(30240.00 AS Decimal(18,2)), CAST(N'2028-07-31' AS DateTime2), N'CON_HANG', CAST(N'2026-03-15T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0024')
INSERT [dbo].[LoHang] VALUES (N'LH-0025', N'LOT-MEL-260305', N'SP2024-0025', 80, CAST(10080.00 AS Decimal(18,2)), CAST(N'2028-08-31' AS DateTime2), N'CON_HANG', CAST(N'2026-03-18T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0025')
INSERT [dbo].[LoHang] VALUES (N'LH-0026', N'LOT-MAG-260306', N'SP2024-0026', 100, CAST(4680.00 AS Decimal(18,2)), CAST(N'2028-09-30' AS DateTime2), N'CON_HANG', CAST(N'2026-03-21T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0026')
INSERT [dbo].[LoHang] VALUES (N'LH-0027', N'LOT-PRO-260307', N'SP2024-0027', 50, CAST(3960.00 AS Decimal(18,2)), CAST(N'2028-10-31' AS DateTime2), N'CON_HANG', CAST(N'2026-03-24T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0027')
INSERT [dbo].[LoHang] VALUES (N'LH-0028', N'LOT-EUC-260308', N'SP2024-0028', 40, CAST(494000.00 AS Decimal(18,2)), CAST(N'2028-11-30' AS DateTime2), N'CON_HANG', CAST(N'2026-03-27T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0028')
INSERT [dbo].[LoHang] VALUES (N'LH-0029', N'LOT-LRC-260309', N'SP2024-0029', 35, CAST(440800.00 AS Decimal(18,2)), CAST(N'2028-12-31' AS DateTime2), N'CON_HANG', CAST(N'2026-03-30T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0029')
INSERT [dbo].[LoHang] VALUES (N'LH-0030', N'LOT-CRV-260310', N'SP2024-0030', 40, CAST(243200.00 AS Decimal(18,2)), CAST(N'2029-01-31' AS DateTime2), N'CON_HANG', CAST(N'2026-04-02T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0030')
INSERT [dbo].[LoHang] VALUES (N'LH-0031', N'LOT-BIO-260401', N'SP2024-0031', 30, CAST(212800.00 AS Decimal(18,2)), CAST(N'2029-02-28' AS DateTime2), N'CON_HANG', CAST(N'2026-04-05T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0031')
INSERT [dbo].[LoHang] VALUES (N'LH-0032', N'LOT-PAU-260402', N'SP2024-0032', 20, CAST(646000.00 AS Decimal(18,2)), CAST(N'2029-03-31' AS DateTime2), N'CON_HANG', CAST(N'2026-04-08T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0032')
INSERT [dbo].[LoHang] VALUES (N'LH-0033', N'LOT-ROH-260403', N'SP2024-0033', 50, CAST(39520.00 AS Decimal(18,2)), CAST(N'2029-04-30' AS DateTime2), N'CON_HANG', CAST(N'2026-04-11T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0033')
INSERT [dbo].[LoHang] VALUES (N'LH-0034', N'LOT-OSL-260404', N'SP2024-0034', 60, CAST(16720.00 AS Decimal(18,2)), CAST(N'2029-05-31' AS DateTime2), N'CON_HANG', CAST(N'2026-04-14T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0034')
INSERT [dbo].[LoHang] VALUES (N'LH-0035', N'LOT-LIS-260405', N'SP2024-0035', 40, CAST(87400.00 AS Decimal(18,2)), CAST(N'2029-06-30' AS DateTime2), N'CON_HANG', CAST(N'2026-04-17T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0035')
INSERT [dbo].[LoHang] VALUES (N'LH-0036', N'LOT-BET-260406', N'SP2024-0036', 30, CAST(51680.00 AS Decimal(18,2)), CAST(N'2029-07-31' AS DateTime2), N'CON_HANG', CAST(N'2026-04-20T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0036')
INSERT [dbo].[LoHang] VALUES (N'LH-0037', N'LOT-STR-260407', N'SP2024-0037', 50, CAST(5600.00 AS Decimal(18,2)), CAST(N'2029-08-31' AS DateTime2), N'CON_HANG', CAST(N'2026-04-23T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0037')
INSERT [dbo].[LoHang] VALUES (N'LH-0038', N'LOT-EFF-260408', N'SP2024-0038', 60, CAST(2170.00 AS Decimal(18,2)), CAST(N'2029-09-30' AS DateTime2), N'CON_HANG', CAST(N'2026-04-26T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0038')
INSERT [dbo].[LoHang] VALUES (N'LH-0039', N'LOT-HAP-260409', N'SP2024-0039', 80, CAST(2380.00 AS Decimal(18,2)), CAST(N'2029-10-31' AS DateTime2), N'CON_HANG', CAST(N'2026-04-29T09:00:00.0000000' AS DateTime2), N'KHO-0001', N'MVLH0039')
INSERT [dbo].[LoHang] VALUES (N'LH-0040', N'LOT-PHO-260410', N'SP2024-0040', 25, CAST(7700.00 AS Decimal(18,2)), CAST(N'2029-11-30' AS DateTime2), N'CON_HANG', CAST(N'2026-05-02T09:00:00.0000000' AS DateTime2), N'KHO-0002', N'MVLH0040')

-- ==============================================================================
-- DỮ LIỆU MẪU LỊCH SỬ KIỂM KÊ KHO 2026
-- Dữ liệu hợp lý: 3 phiếu kiểm kê, có phiếu khớp 100%, có phiếu lệch thừa/thiếu nhỏ.
-- Sau kiểm kê, tồn hiện tại của các lô lệch được cập nhật theo tồn thực tế.
-- ==============================================================================
INSERT [dbo].[PhieuKiemKeKho] VALUES (N'PKK-0001', CAST(N'2026-05-05T08:30:00.0000000' AS DateTime2), N'KHO-0001', N'QL-0001', 5, 0, N'Kiểm kê định kỳ đầu tháng 05/2026 - kho quầy chính', N'HOAN_THANH')
INSERT [dbo].[PhieuKiemKeKho] VALUES (N'PKK-0002', CAST(N'2026-05-12T16:10:00.0000000' AS DateTime2), N'KHO-0002', N'QL-0001', 5, 1, N'Kiểm kê kho dự trữ, có chênh lệch nhỏ đã ghi nhận lý do', N'HOAN_THANH')
INSERT [dbo].[PhieuKiemKeKho] VALUES (N'PKK-0003', CAST(N'2026-05-19T09:15:00.0000000' AS DateTime2), N'KHO-0001', N'DS-0001', 6, -2, N'Kiểm kê nhanh nhóm thuốc bán chạy tại quầy', N'HOAN_THANH')
GO

INSERT [dbo].[ChiTietPhieuKiemKeKho] VALUES (N'CTPKK-0001', N'PKK-0001', N'LH-0001', N'SP2024-0001', N'KHO-0001', N'LOT-AMX-260101', 200, 200, 0, NULL)
INSERT [dbo].[ChiTietPhieuKiemKeKho] VALUES (N'CTPKK-0002', N'PKK-0001', N'LH-0002', N'SP2024-0002', N'KHO-0001', N'LOT-CEF-260102', 150, 150, 0, NULL)
INSERT [dbo].[ChiTietPhieuKiemKeKho] VALUES (N'CTPKK-0003', N'PKK-0001', N'LH-0005', N'SP2024-0005', N'KHO-0001', N'LOT-AUG-260105', 100, 100, 0, NULL)
INSERT [dbo].[ChiTietPhieuKiemKeKho] VALUES (N'CTPKK-0004', N'PKK-0001', N'LH-0006', N'SP2024-0006', N'KHO-0001', N'LOT-AZI-260106', 120, 120, 0, NULL)
INSERT [dbo].[ChiTietPhieuKiemKeKho] VALUES (N'CTPKK-0005', N'PKK-0001', N'LH-0012', N'SP2024-0012', N'KHO-0001', N'LOT-CET-260202', 350, 350, 0, NULL)

INSERT [dbo].[ChiTietPhieuKiemKeKho] VALUES (N'CTPKK-0006', N'PKK-0002', N'LH-0021', N'SP2024-0021', N'KHO-0002', N'LOT-VTD-260301', 100, 99, -1, N'Lẻ 1 viên do bể vỉ khi sắp xếp kho')
INSERT [dbo].[ChiTietPhieuKiemKeKho] VALUES (N'CTPKK-0007', N'PKK-0002', N'LH-0022', N'SP2024-0022', N'KHO-0002', N'LOT-OMG-260302', 80, 80, 0, NULL)
INSERT [dbo].[ChiTietPhieuKiemKeKho] VALUES (N'CTPKK-0008', N'PKK-0002', N'LH-0024', N'SP2024-0024', N'KHO-0002', N'LOT-COL-260304', 60, 62, 2, N'Tìm thấy 2 gói để sai vị trí trong kho dự trữ')
INSERT [dbo].[ChiTietPhieuKiemKeKho] VALUES (N'CTPKK-0009', N'PKK-0002', N'LH-0028', N'SP2024-0028', N'KHO-0002', N'LOT-EUC-260308', 40, 40, 0, NULL)
INSERT [dbo].[ChiTietPhieuKiemKeKho] VALUES (N'CTPKK-0010', N'PKK-0002', N'LH-0030', N'SP2024-0030', N'KHO-0002', N'LOT-CRV-260310', 40, 40, 0, NULL)

INSERT [dbo].[ChiTietPhieuKiemKeKho] VALUES (N'CTPKK-0011', N'PKK-0003', N'LH-0001', N'SP2024-0001', N'KHO-0001', N'LOT-AMX-260101', 200, 198, -2, N'Rách vỉ trong quá trình trưng bày, đã loại khỏi tồn bán')
INSERT [dbo].[ChiTietPhieuKiemKeKho] VALUES (N'CTPKK-0012', N'PKK-0003', N'LH-0004', N'SP2024-0004', N'KHO-0001', N'LOT-LOS-260104', 250, 250, 0, NULL)
INSERT [dbo].[ChiTietPhieuKiemKeKho] VALUES (N'CTPKK-0013', N'PKK-0003', N'LH-0007', N'SP2024-0007', N'KHO-0001', N'LOT-AML-260107', 200, 200, 0, NULL)
INSERT [dbo].[ChiTietPhieuKiemKeKho] VALUES (N'CTPKK-0014', N'PKK-0003', N'LH-0009', N'SP2024-0009', N'KHO-0001', N'LOT-PAR-260109', 500, 500, 0, NULL)
INSERT [dbo].[ChiTietPhieuKiemKeKho] VALUES (N'CTPKK-0015', N'PKK-0003', N'LH-0012', N'SP2024-0012', N'KHO-0001', N'LOT-CET-260202', 350, 350, 0, NULL)
INSERT [dbo].[ChiTietPhieuKiemKeKho] VALUES (N'CTPKK-0016', N'PKK-0003', N'LH-0039', N'SP2024-0039', N'KHO-0001', N'LOT-HAP-260409', 80, 80, 0, NULL)
GO

-- Cập nhật tồn hiện tại theo phiếu kiểm kê cuối cùng có lệch.
UPDATE [dbo].[LoHang] SET [soLuongLoHang] = 198 WHERE [id] = N'LH-0001';
UPDATE [dbo].[LoHang] SET [soLuongLoHang] = 99  WHERE [id] = N'LH-0021';
UPDATE [dbo].[LoHang] SET [soLuongLoHang] = 62  WHERE [id] = N'LH-0024';
GO


-- ==============================================================================
-- DỮ LIỆU PHIẾU NHẬP ĐỒNG BỘ VỚI LÔ HÀNG MẪU
-- Mỗi lô hàng mẫu có 1 phiếu nhập, đủ giá nhập, thành tiền, HSD, kho, sản phẩm.
-- ==============================================================================
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0001', CAST(N'2026-01-05T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(280000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-AMX-260101 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0002', CAST(N'2026-01-08T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(525000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-CEF-260102 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0003', CAST(N'2026-01-11T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(168000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-MET-260103 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0004', CAST(N'2026-01-14T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(262500.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-LOS-260104 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0005', CAST(N'2026-01-17T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(6300000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-AUG-260105 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0006', CAST(N'2026-01-20T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(6300000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-AZI-260106 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0007', CAST(N'2026-01-23T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(350000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-AML-260107 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0008', CAST(N'2026-01-26T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(441000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-ATO-260108 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0009', CAST(N'2026-01-29T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(140000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-PAR-260109 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0010', CAST(N'2026-02-01T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(168000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-IBU-260110 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0011', CAST(N'2026-02-04T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(455070.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-VTC-260201 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0012', CAST(N'2026-02-07T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(147000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-CET-260202 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0013', CAST(N'2026-02-10T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(840000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-ORS-260203 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0014', CAST(N'2026-02-13T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(4900000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-NAP-260204 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0015', CAST(N'2026-02-16T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(139965.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-DOM-260205 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0016', CAST(N'2026-02-19T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(121310.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-OME-260206 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0017', CAST(N'2026-02-22T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(3080000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-SIR-260207 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0018', CAST(N'2026-02-25T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(1540000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-SME-260208 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0019', CAST(N'2026-02-28T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(93275.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-LOR-260209 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0020', CAST(N'2026-03-03T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(145572.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-ESO-260210 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0021', CAST(N'2026-03-06T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(527976.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-VTD-260301 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0022', CAST(N'2026-03-09T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(537580.80 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-OMG-260302 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0023', CAST(N'2026-03-12T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(244771.20 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-CAL-260303 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0024', CAST(N'2026-03-15T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(1814400.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-COL-260304 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0025', CAST(N'2026-03-18T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(806400.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-MEL-260305 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0026', CAST(N'2026-03-21T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(468000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-MAG-260306 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0027', CAST(N'2026-03-24T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(198000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-PRO-260307 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0028', CAST(N'2026-03-27T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(19760000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-EUC-260308 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0029', CAST(N'2026-03-30T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(15428000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-LRC-260309 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0030', CAST(N'2026-04-02T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(9728000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-CRV-260310 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0031', CAST(N'2026-04-05T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(6384000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-BIO-260401 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0032', CAST(N'2026-04-08T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(12920000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-PAU-260402 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0033', CAST(N'2026-04-11T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(1976000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-ROH-260403 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0034', CAST(N'2026-04-14T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(1003200.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-OSL-260404 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0035', CAST(N'2026-04-17T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(3496000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-LIS-260405 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0036', CAST(N'2026-04-20T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(1550400.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-BET-260406 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0037', CAST(N'2026-04-23T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(280000.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-STR-260407 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0038', CAST(N'2026-04-26T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(130200.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-EFF-260408 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0039', CAST(N'2026-04-29T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(190400.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-HAP-260409 - KHO-0001', N'HOAN_THANH')
INSERT [dbo].[PhieuNhapHang] VALUES (N'PN-0040', CAST(N'2026-05-02T09:00:00.0000000' AS DateTime2), NULL, N'QL-0001', CAST(192500.00 AS Decimal(18,2)), N'Phiếu nhập mẫu 2026 - LOT-PHO-260410 - KHO-0002', N'HOAN_THANH')
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0001', N'PN-0001', N'LH-0001', N'SP2024-0001', N'KHO-0001', N'LOT-AMX-260101', 200, CAST(1400.00 AS Decimal(18,2)), CAST(280000.00 AS Decimal(18,2)), CAST(N'2026-08-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0002', N'PN-0002', N'LH-0002', N'SP2024-0002', N'KHO-0001', N'LOT-CEF-260102', 150, CAST(3500.00 AS Decimal(18,2)), CAST(525000.00 AS Decimal(18,2)), CAST(N'2026-09-30' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0003', N'PN-0003', N'LH-0003', N'SP2024-0003', N'KHO-0001', N'LOT-MET-260103', 300, CAST(560.00 AS Decimal(18,2)), CAST(168000.00 AS Decimal(18,2)), CAST(N'2026-10-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0004', N'PN-0004', N'LH-0004', N'SP2024-0004', N'KHO-0001', N'LOT-LOS-260104', 250, CAST(1050.00 AS Decimal(18,2)), CAST(262500.00 AS Decimal(18,2)), CAST(N'2026-11-30' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0005', N'PN-0005', N'LH-0005', N'SP2024-0005', N'KHO-0001', N'LOT-AUG-260105', 100, CAST(63000.00 AS Decimal(18,2)), CAST(6300000.00 AS Decimal(18,2)), CAST(N'2026-12-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0006', N'PN-0006', N'LH-0006', N'SP2024-0006', N'KHO-0001', N'LOT-AZI-260106', 120, CAST(52500.00 AS Decimal(18,2)), CAST(6300000.00 AS Decimal(18,2)), CAST(N'2027-01-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0007', N'PN-0007', N'LH-0007', N'SP2024-0007', N'KHO-0001', N'LOT-AML-260107', 200, CAST(1750.00 AS Decimal(18,2)), CAST(350000.00 AS Decimal(18,2)), CAST(N'2027-02-28' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0008', N'PN-0008', N'LH-0008', N'SP2024-0008', N'KHO-0001', N'LOT-ATO-260108', 180, CAST(2450.00 AS Decimal(18,2)), CAST(441000.00 AS Decimal(18,2)), CAST(N'2027-03-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0009', N'PN-0009', N'LH-0009', N'SP2024-0009', N'KHO-0001', N'LOT-PAR-260109', 500, CAST(280.00 AS Decimal(18,2)), CAST(140000.00 AS Decimal(18,2)), CAST(N'2027-04-30' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0010', N'PN-0010', N'LH-0010', N'SP2024-0010', N'KHO-0001', N'LOT-IBU-260110', 400, CAST(420.00 AS Decimal(18,2)), CAST(168000.00 AS Decimal(18,2)), CAST(N'2027-05-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0011', N'PN-0011', N'LH-0011', N'SP2024-0011', N'KHO-0001', N'LOT-VTC-260201', 300, CAST(1516.90 AS Decimal(18,2)), CAST(455070.00 AS Decimal(18,2)), CAST(N'2027-06-30' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0012', N'PN-0012', N'LH-0012', N'SP2024-0012', N'KHO-0001', N'LOT-CET-260202', 350, CAST(420.00 AS Decimal(18,2)), CAST(147000.00 AS Decimal(18,2)), CAST(N'2027-07-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0013', N'PN-0013', N'LH-0013', N'SP2024-0013', N'KHO-0001', N'LOT-ORS-260203', 400, CAST(2100.00 AS Decimal(18,2)), CAST(840000.00 AS Decimal(18,2)), CAST(N'2027-08-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0014', N'PN-0014', N'LH-0014', N'SP2024-0014', N'KHO-0001', N'LOT-NAP-260204', 200, CAST(24500.00 AS Decimal(18,2)), CAST(4900000.00 AS Decimal(18,2)), CAST(N'2027-09-30' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0015', N'PN-0015', N'LH-0015', N'SP2024-0015', N'KHO-0001', N'LOT-DOM-260205', 150, CAST(933.10 AS Decimal(18,2)), CAST(139965.00 AS Decimal(18,2)), CAST(N'2027-10-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0016', N'PN-0016', N'LH-0016', N'SP2024-0016', N'KHO-0001', N'LOT-OME-260206', 100, CAST(1213.10 AS Decimal(18,2)), CAST(121310.00 AS Decimal(18,2)), CAST(N'2027-11-30' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0017', N'PN-0017', N'LH-0017', N'SP2024-0017', N'KHO-0001', N'LOT-SIR-260207', 80, CAST(38500.00 AS Decimal(18,2)), CAST(3080000.00 AS Decimal(18,2)), CAST(N'2027-12-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0018', N'PN-0018', N'LH-0018', N'SP2024-0018', N'KHO-0001', N'LOT-SME-260208', 200, CAST(7700.00 AS Decimal(18,2)), CAST(1540000.00 AS Decimal(18,2)), CAST(N'2028-01-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0019', N'PN-0019', N'LH-0019', N'SP2024-0019', N'KHO-0001', N'LOT-LOR-260209', 250, CAST(373.10 AS Decimal(18,2)), CAST(93275.00 AS Decimal(18,2)), CAST(N'2028-02-29' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0020', N'PN-0020', N'LH-0020', N'SP2024-0020', N'KHO-0001', N'LOT-ESO-260210', 120, CAST(1213.10 AS Decimal(18,2)), CAST(145572.00 AS Decimal(18,2)), CAST(N'2028-03-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0021', N'PN-0021', N'LH-0021', N'SP2024-0021', N'KHO-0002', N'LOT-VTD-260301', 100, CAST(5279.76 AS Decimal(18,2)), CAST(527976.00 AS Decimal(18,2)), CAST(N'2028-04-30' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0022', N'PN-0022', N'LH-0022', N'SP2024-0022', N'KHO-0002', N'LOT-OMG-260302', 80, CAST(6719.76 AS Decimal(18,2)), CAST(537580.80 AS Decimal(18,2)), CAST(N'2028-05-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0023', N'PN-0023', N'LH-0023', N'SP2024-0023', N'KHO-0002', N'LOT-CAL-260303', 120, CAST(2039.76 AS Decimal(18,2)), CAST(244771.20 AS Decimal(18,2)), CAST(N'2028-06-30' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0024', N'PN-0024', N'LH-0024', N'SP2024-0024', N'KHO-0002', N'LOT-COL-260304', 60, CAST(30240.00 AS Decimal(18,2)), CAST(1814400.00 AS Decimal(18,2)), CAST(N'2028-07-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0025', N'PN-0025', N'LH-0025', N'SP2024-0025', N'KHO-0002', N'LOT-MEL-260305', 80, CAST(10080.00 AS Decimal(18,2)), CAST(806400.00 AS Decimal(18,2)), CAST(N'2028-08-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0026', N'PN-0026', N'LH-0026', N'SP2024-0026', N'KHO-0002', N'LOT-MAG-260306', 100, CAST(4680.00 AS Decimal(18,2)), CAST(468000.00 AS Decimal(18,2)), CAST(N'2028-09-30' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0027', N'PN-0027', N'LH-0027', N'SP2024-0027', N'KHO-0002', N'LOT-PRO-260307', 50, CAST(3960.00 AS Decimal(18,2)), CAST(198000.00 AS Decimal(18,2)), CAST(N'2028-10-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0028', N'PN-0028', N'LH-0028', N'SP2024-0028', N'KHO-0002', N'LOT-EUC-260308', 40, CAST(494000.00 AS Decimal(18,2)), CAST(19760000.00 AS Decimal(18,2)), CAST(N'2028-11-30' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0029', N'PN-0029', N'LH-0029', N'SP2024-0029', N'KHO-0002', N'LOT-LRC-260309', 35, CAST(440800.00 AS Decimal(18,2)), CAST(15428000.00 AS Decimal(18,2)), CAST(N'2028-12-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0030', N'PN-0030', N'LH-0030', N'SP2024-0030', N'KHO-0002', N'LOT-CRV-260310', 40, CAST(243200.00 AS Decimal(18,2)), CAST(9728000.00 AS Decimal(18,2)), CAST(N'2029-01-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0031', N'PN-0031', N'LH-0031', N'SP2024-0031', N'KHO-0002', N'LOT-BIO-260401', 30, CAST(212800.00 AS Decimal(18,2)), CAST(6384000.00 AS Decimal(18,2)), CAST(N'2029-02-28' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0032', N'PN-0032', N'LH-0032', N'SP2024-0032', N'KHO-0002', N'LOT-PAU-260402', 20, CAST(646000.00 AS Decimal(18,2)), CAST(12920000.00 AS Decimal(18,2)), CAST(N'2029-03-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0033', N'PN-0033', N'LH-0033', N'SP2024-0033', N'KHO-0002', N'LOT-ROH-260403', 50, CAST(39520.00 AS Decimal(18,2)), CAST(1976000.00 AS Decimal(18,2)), CAST(N'2029-04-30' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0034', N'PN-0034', N'LH-0034', N'SP2024-0034', N'KHO-0002', N'LOT-OSL-260404', 60, CAST(16720.00 AS Decimal(18,2)), CAST(1003200.00 AS Decimal(18,2)), CAST(N'2029-05-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0035', N'PN-0035', N'LH-0035', N'SP2024-0035', N'KHO-0002', N'LOT-LIS-260405', 40, CAST(87400.00 AS Decimal(18,2)), CAST(3496000.00 AS Decimal(18,2)), CAST(N'2029-06-30' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0036', N'PN-0036', N'LH-0036', N'SP2024-0036', N'KHO-0002', N'LOT-BET-260406', 30, CAST(51680.00 AS Decimal(18,2)), CAST(1550400.00 AS Decimal(18,2)), CAST(N'2029-07-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0037', N'PN-0037', N'LH-0037', N'SP2024-0037', N'KHO-0002', N'LOT-STR-260407', 50, CAST(5600.00 AS Decimal(18,2)), CAST(280000.00 AS Decimal(18,2)), CAST(N'2029-08-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0038', N'PN-0038', N'LH-0038', N'SP2024-0038', N'KHO-0002', N'LOT-EFF-260408', 60, CAST(2170.00 AS Decimal(18,2)), CAST(130200.00 AS Decimal(18,2)), CAST(N'2029-09-30' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0039', N'PN-0039', N'LH-0039', N'SP2024-0039', N'KHO-0001', N'LOT-HAP-260409', 80, CAST(2380.00 AS Decimal(18,2)), CAST(190400.00 AS Decimal(18,2)), CAST(N'2029-10-31' AS DateTime2))
INSERT [dbo].[ChiTietPhieuNhapHang] VALUES (N'CTPN-0040', N'PN-0040', N'LH-0040', N'SP2024-0040', N'KHO-0002', N'LOT-PHO-260410', 25, CAST(7700.00 AS Decimal(18,2)), CAST(192500.00 AS Decimal(18,2)), CAST(N'2029-11-30' AS DateTime2))
GO

INSERT [dbo].[CaLamViec] VALUES (N'Ca-0001', N'DS-0001', CAST(N'2024-03-01T07:30:00.0000000' AS DateTime2), CAST(N'2024-03-01T17:30:00.0000000' AS DateTime2), CAST(2500000.00 AS Decimal(18,2)), CAST(500000.00 AS Decimal(18,2)), CAST(3000000.00 AS Decimal(18,2)), 1, N'Bình thường')
INSERT [dbo].[CaLamViec] VALUES (N'Ca-0002', N'DS-0002', CAST(N'2024-03-01T07:30:00.0000000' AS DateTime2), CAST(N'2024-03-01T17:30:00.0000000' AS DateTime2), CAST(1800000.00 AS Decimal(18,2)), CAST(500000.00 AS Decimal(18,2)), CAST(2300000.00 AS Decimal(18,2)), 1, N'Bình thường')
INSERT [dbo].[CaLamViec] VALUES (N'Ca-0003', N'DS-0003', CAST(N'2024-03-02T07:30:00.0000000' AS DateTime2), CAST(N'2024-03-02T17:30:00.0000000' AS DateTime2), CAST(3200000.00 AS Decimal(18,2)), CAST(500000.00 AS Decimal(18,2)), CAST(3700000.00 AS Decimal(18,2)), 1, N'Bình thường')
INSERT [dbo].[CaLamViec] VALUES (N'Ca-0004', N'DS-0004', CAST(N'2024-03-02T07:30:00.0000000' AS DateTime2), CAST(N'2024-03-02T17:30:00.0000000' AS DateTime2), CAST(2100000.00 AS Decimal(18,2)), CAST(500000.00 AS Decimal(18,2)), CAST(2600000.00 AS Decimal(18,2)), 1, N'Bình thường')
INSERT [dbo].[CaLamViec] VALUES (N'Ca-0005', N'DS-0005', CAST(N'2024-03-03T07:30:00.0000000' AS DateTime2), CAST(N'2024-03-03T17:30:00.0000000' AS DateTime2), CAST(1500000.00 AS Decimal(18,2)), CAST(500000.00 AS Decimal(18,2)), CAST(2000000.00 AS Decimal(18,2)), 1, N'Bình thường')

-- HÓA ĐƠN NĂM 2024
INSERT [dbo].[HoaDon] VALUES (N'HD2024-0001', N'BAN_HANG', NULL, CAST(N'2024-01-05T08:30:00.0000000' AS DateTime2), N'DS-0001', N'KH-0001', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD2024-0002', N'BAN_HANG', NULL, CAST(N'2024-01-10T09:00:00.0000000' AS DateTime2), N'DS-0002', N'KH-0002', NULL, N'CHUYEN_KHOAN_NGAN_HANG', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD2024-0003', N'BAN_HANG', NULL, CAST(N'2024-02-14T10:15:00.0000000' AS DateTime2), N'DS-0003', N'KH-0003', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD2024-0004', N'BAN_HANG', NULL, CAST(N'2024-02-20T11:00:00.0000000' AS DateTime2), N'DS-0004', N'KH-0004', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD2024-0005', N'BAN_HANG', NULL, CAST(N'2024-03-05T08:45:00.0000000' AS DateTime2), N'DS-0005', N'KH-0005', NULL, N'CHUYEN_KHOAN_NGAN_HANG', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD2024-0006', N'BAN_HANG', NULL, CAST(N'2024-03-15T13:30:00.0000000' AS DateTime2), N'DS-0001', N'KH-0006', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD2024-0007', N'BAN_HANG', NULL, CAST(N'2024-04-10T09:00:00.0000000' AS DateTime2), N'DS-0002', N'KH-0007', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD2024-0008', N'BAN_HANG', NULL, CAST(N'2024-04-20T14:00:00.0000000' AS DateTime2), N'DS-0003', N'KH-0008', NULL, N'CHUYEN_KHOAN_NGAN_HANG', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD2024-0009', N'BAN_HANG', NULL, CAST(N'2024-05-05T10:30:00.0000000' AS DateTime2), N'DS-0004', N'KH-0009', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD2024-0010', N'BAN_HANG', NULL, CAST(N'2024-05-18T11:15:00.0000000' AS DateTime2), N'DS-0005', N'KH-0010', NULL, N'TIEN_MAT', NULL)

-- HÓA ĐƠN NĂM 2025
INSERT [dbo].[HoaDon] VALUES (N'HD-2025-1', N'BAN_HANG', NULL, CAST(N'2025-01-05T08:30:00' AS DateTime2), N'DS-0001', N'KH-0001', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD-2025-2', N'BAN_HANG', NULL, CAST(N'2025-02-14T09:00:00' AS DateTime2), N'DS-0002', N'KH-0002', NULL, N'CHUYEN_KHOAN_NGAN_HANG', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD-2025-3', N'BAN_HANG', NULL, CAST(N'2025-03-20T10:15:00' AS DateTime2), N'DS-0003', N'KH-0003', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD-2025-4', N'BAN_HANG', NULL, CAST(N'2025-04-10T11:00:00' AS DateTime2), N'DS-0004', N'KH-0004', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD-2025-5', N'BAN_HANG', NULL, CAST(N'2025-05-05T08:45:00' AS DateTime2), N'DS-0005', N'KH-0005', NULL, N'CHUYEN_KHOAN_NGAN_HANG', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD-2025-6', N'BAN_HANG', NULL, CAST(N'2025-06-15T13:30:00' AS DateTime2), N'DS-0001', N'KH-0006', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD-2025-7', N'BAN_HANG', NULL, CAST(N'2025-07-20T09:00:00' AS DateTime2), N'DS-0002', N'KH-0007', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD-2025-8', N'BAN_HANG', NULL, CAST(N'2025-08-10T14:00:00' AS DateTime2), N'DS-0003', N'KH-0008', NULL, N'CHUYEN_KHOAN_NGAN_HANG', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD-2025-9', N'BAN_HANG', NULL, CAST(N'2025-09-25T10:30:00' AS DateTime2), N'DS-0004', N'KH-0009', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD-2025-10', N'BAN_HANG', NULL, CAST(N'2025-10-18T11:15:00' AS DateTime2), N'DS-0005', N'KH-0010', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD-2025-11', N'BAN_HANG', NULL, CAST(N'2025-11-05T08:30:00' AS DateTime2), N'DS-0001', N'KH-0001', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD-2025-12', N'BAN_HANG', NULL, CAST(N'2025-12-20T09:00:00' AS DateTime2), N'DS-0002', N'KH-0002', NULL, N'CHUYEN_KHOAN_NGAN_HANG', NULL)

-- HÓA ĐƠN NĂM 2026
INSERT [dbo].[HoaDon] VALUES (N'HD-2026-1', N'BAN_HANG', NULL, CAST(N'2026-01-10T08:30:00' AS DateTime2), N'DS-0001', N'KH-0003', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD-2026-2', N'BAN_HANG', NULL, CAST(N'2026-02-14T09:00:00' AS DateTime2), N'DS-0002', N'KH-0004', NULL, N'CHUYEN_KHOAN_NGAN_HANG', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD-2026-3', N'BAN_HANG', NULL, CAST(N'2026-03-05T10:15:00' AS DateTime2), N'DS-0003', N'KH-0005', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] VALUES (N'HD-2026-4', N'BAN_HANG', NULL, CAST(N'2026-04-20T11:00:00' AS DateTime2), N'DS-0004', N'KH-0006', NULL, N'TIEN_MAT', NULL)

INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0001', N'DVL-0016', N'SP2024-0009', 2, CAST(40000.00 AS Decimal(18,2)), CAST(80000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0002', N'DVL-0003', N'SP2024-0002', 1, CAST(65000.00 AS Decimal(18,2)), CAST(65000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0003', N'DVL-0018', N'SP2024-0010', 3, CAST(18000.00 AS Decimal(18,2)), CAST(54000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0004', N'DVL-0002', N'SP2024-0001', 1, CAST(55000.00 AS Decimal(18,2)), CAST(55000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0004', N'DVL-0028', N'SP2024-0016', 2, CAST(52000.00 AS Decimal(18,2)), CAST(104000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0005', N'DVL-0052', N'SP2024-0030', 1, CAST(320000.00 AS Decimal(18,2)), CAST(320000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0005', N'DVL-0053', N'SP2024-0031', 1, CAST(280000.00 AS Decimal(18,2)), CAST(280000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0006', N'DVL-0025', N'SP2024-0014', 1, CAST(35000.00 AS Decimal(18,2)), CAST(35000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0006', N'DVL-0033', N'SP2024-0019', 2, CAST(533.00 AS Decimal(18,2)), CAST(1066.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0007', N'DVL-0043', N'SP2024-0024', 2, CAST(420000.00 AS Decimal(18,2)), CAST(840000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0007', N'DVL-0047', N'SP2024-0026', 1, CAST(195000.00 AS Decimal(18,2)), CAST(195000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0008', N'DVL-0021', N'SP2024-0012', 1, CAST(18000.00 AS Decimal(18,2)), CAST(18000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0008', N'DVL-0044', N'SP2024-0025', 5, CAST(280000.00 AS Decimal(18,2)), CAST(1400000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0009', N'DVL-0030', N'SP2024-0017', 2, CAST(55000.00 AS Decimal(18,2)), CAST(110000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0009', N'DVL-0031', N'SP2024-0018', 1, CAST(11000.00 AS Decimal(18,2)), CAST(11000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0010', N'DVL-0006', N'SP2024-0003', 1, CAST(75000.00 AS Decimal(18,2)), CAST(75000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0010', N'DVL-0008', N'SP2024-0004', 1, CAST(40000.00 AS Decimal(18,2)), CAST(40000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0010', N'DVL-0019', N'SP2024-0011', 1, CAST(65000.00 AS Decimal(18,2)), CAST(65000.00 AS Decimal(18,2)), NULL)

-- CHI TIET HOA DON 2025
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD-2025-1', N'DVL-0008', N'SP2024-0004', 2, CAST(40000.00 AS Decimal(18,2)), CAST(80000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD-2025-2', N'DVL-0030', N'SP2024-0017', 1, CAST(55000.00 AS Decimal(18,2)), CAST(55000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD-2025-3', N'DVL-0016', N'SP2024-0009', 2, CAST(40000.00 AS Decimal(18,2)), CAST(80000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD-2025-4', N'DVL-0002', N'SP2024-0001', 1, CAST(55000.00 AS Decimal(18,2)), CAST(55000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD-2025-5', N'DVL-0052', N'SP2024-0030', 1, CAST(320000.00 AS Decimal(18,2)), CAST(320000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD-2025-6', N'DVL-0025', N'SP2024-0014', 1, CAST(35000.00 AS Decimal(18,2)), CAST(35000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD-2025-7', N'DVL-0043', N'SP2024-0024', 2, CAST(420000.00 AS Decimal(18,2)), CAST(840000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD-2025-8', N'DVL-0021', N'SP2024-0012', 1, CAST(18000.00 AS Decimal(18,2)), CAST(18000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD-2025-9', N'DVL-0030', N'SP2024-0017', 2, CAST(55000.00 AS Decimal(18,2)), CAST(110000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD-2025-10', N'DVL-0006', N'SP2024-0003', 1, CAST(75000.00 AS Decimal(18,2)), CAST(75000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD-2025-11', N'DVL-0023', N'SP2024-0013', 1, CAST(3000.00 AS Decimal(18,2)), CAST(3000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD-2025-12', N'DVL-0025', N'SP2024-0014', 1, CAST(35000.00 AS Decimal(18,2)), CAST(35000.00 AS Decimal(18,2)), NULL)

-- CHI TIET HOA DON 2026
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD-2026-1', N'DVL-0026', N'SP2024-0015', 1, CAST(40000.00 AS Decimal(18,2)), CAST(40000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD-2026-2', N'DVL-0030', N'SP2024-0017', 1, CAST(55000.00 AS Decimal(18,2)), CAST(55000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD-2026-3', N'DVL-0039', N'SP2024-0022', 1, CAST(280000.00 AS Decimal(18,2)), CAST(280000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD-2026-4', N'DVL-0052', N'SP2024-0030', 1, CAST(320000.00 AS Decimal(18,2)), CAST(320000.00 AS Decimal(18,2)), NULL)
-- PHAN BO LO HANG 2024
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0001', N'DVL-0016', N'SP2024-0009', N'LH-0009', 2)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0002', N'DVL-0003', N'SP2024-0002', N'LH-0002', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0003', N'DVL-0018', N'SP2024-0010', N'LH-0010', 3)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0004', N'DVL-0002', N'SP2024-0001', N'LH-0001', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0004', N'DVL-0028', N'SP2024-0016', N'LH-0016', 2)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0005', N'DVL-0052', N'SP2024-0030', N'LH-0030', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0005', N'DVL-0053', N'SP2024-0031', N'LH-0031', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0006', N'DVL-0025', N'SP2024-0014', N'LH-0014', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0006', N'DVL-0033', N'SP2024-0019', N'LH-0019', 2)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0007', N'DVL-0043', N'SP2024-0024', N'LH-0024', 2)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0007', N'DVL-0047', N'SP2024-0026', N'LH-0026', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0008', N'DVL-0021', N'SP2024-0012', N'LH-0012', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0008', N'DVL-0044', N'SP2024-0025', N'LH-0025', 5)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0009', N'DVL-0030', N'SP2024-0017', N'LH-0017', 2)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0009', N'DVL-0031', N'SP2024-0018', N'LH-0018', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0010', N'DVL-0006', N'SP2024-0003', N'LH-0003', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0010', N'DVL-0008', N'SP2024-0004', N'LH-0004', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0010', N'DVL-0019', N'SP2024-0011', N'LH-0011', 1)

-- PHAN BO LO HANG 2025
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-1', N'DVL-0008', N'SP2024-0004', N'LH-0004', 2)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-2', N'DVL-0030', N'SP2024-0017', N'LH-0017', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-3', N'DVL-0016', N'SP2024-0009', N'LH-0009', 2)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-4', N'DVL-0002', N'SP2024-0001', N'LH-0001', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-5', N'DVL-0052', N'SP2024-0030', N'LH-0030', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-6', N'DVL-0025', N'SP2024-0014', N'LH-0014', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-7', N'DVL-0043', N'SP2024-0024', N'LH-0024', 2)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-8', N'DVL-0021', N'SP2024-0012', N'LH-0012', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-9', N'DVL-0030', N'SP2024-0017', N'LH-0017', 2)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-10', N'DVL-0006', N'SP2024-0003', N'LH-0003', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-11', N'DVL-0023', N'SP2024-0013', N'LH-0013', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-12', N'DVL-0025', N'SP2024-0014', N'LH-0014', 1)

-- PHAN BO LO HANG 2026
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2026-1', N'DVL-0026', N'SP2024-0015', N'LH-0015', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2026-2', N'DVL-0030', N'SP2024-0017', N'LH-0017', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2026-3', N'DVL-0039', N'SP2024-0022', N'LH-0022', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2026-4', N'DVL-0052', N'SP2024-0030', N'LH-0030', 1)
GO

-- ==============================================================================
-- 5b. CẬP NHẬT maVach VÀ nhomBenhLy CHO 40 SẢN PHẨM
-- ==============================================================================

UPDATE [dbo].[SanPham] SET [maVach]=N'8934663100017' WHERE [id]=N'SP2024-0001'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934663100024' WHERE [id]=N'SP2024-0002'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934663100086' WHERE [id]=N'SP2024-0005'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934663100093' WHERE [id]=N'SP2024-0006'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934663100031' WHERE [id]=N'SP2024-0003'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934663100048' WHERE [id]=N'SP2024-0004'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934663100109' WHERE [id]=N'SP2024-0007'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934663100116' WHERE [id]=N'SP2024-0008'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100014' WHERE [id]=N'SP2024-0009'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100021' WHERE [id]=N'SP2024-0010'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100182' WHERE [id]=N'SP2024-0038'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100038' WHERE [id]=N'SP2024-0011'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100113' WHERE [id]=N'SP2024-0021'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100120' WHERE [id]=N'SP2024-0022'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100137' WHERE [id]=N'SP2024-0023'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100144' WHERE [id]=N'SP2024-0025'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100151' WHERE [id]=N'SP2024-0026'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100229' WHERE [id]=N'SP2024-0027'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100199' WHERE [id]=N'SP2024-0039'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100045' WHERE [id]=N'SP2024-0012'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100052' WHERE [id]=N'SP2024-0014'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100168' WHERE [id]=N'SP2024-0019'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100205' WHERE [id]=N'SP2024-0037'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100069' WHERE [id]=N'SP2024-0013'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100076' WHERE [id]=N'SP2024-0015'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100083' WHERE [id]=N'SP2024-0016'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100090' WHERE [id]=N'SP2024-0017'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100106' WHERE [id]=N'SP2024-0018'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100175' WHERE [id]=N'SP2024-0020'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100212' WHERE [id]=N'SP2024-0040'
UPDATE [dbo].[SanPham] SET [maVach]=N'4005900117908' WHERE [id]=N'SP2024-0024'
UPDATE [dbo].[SanPham] SET [maVach]=N'4005900272935' WHERE [id]=N'SP2024-0028'
UPDATE [dbo].[SanPham] SET [maVach]=N'3337872413148' WHERE [id]=N'SP2024-0029'
UPDATE [dbo].[SanPham] SET [maVach]=N'3606000594227' WHERE [id]=N'SP2024-0030'
UPDATE [dbo].[SanPham] SET [maVach]=N'3701129801420' WHERE [id]=N'SP2024-0031'
UPDATE [dbo].[SanPham] SET [maVach]=N'0761591032015' WHERE [id]=N'SP2024-0032'
UPDATE [dbo].[SanPham] SET [maVach]=N'0761591032022' WHERE [id]=N'SP2024-0035'
UPDATE [dbo].[SanPham] SET [maVach]=N'5038483381038' WHERE [id]=N'SP2024-0036'
UPDATE [dbo].[SanPham] SET [maVach]=N'4987241115532' WHERE [id]=N'SP2024-0033'
UPDATE [dbo].[SanPham] SET [maVach]=N'8936077610014' WHERE [id]=N'SP2024-0034'

GO


/* =========================================================
   GẮN VỊ TRÍ KỆ CHO LÔ HÀNG
   LoHang.viTriId -> ViTriThuoc.id
   ========================================================= */
IF COL_LENGTH('dbo.LoHang', 'viTriId') IS NULL
BEGIN
    ALTER TABLE dbo.LoHang ADD viTriId NVARCHAR(50) NULL;
END
GO

UPDATE dbo.LoHang
SET viTriId = CASE
    WHEN khoHangId = 'KHO-0001' THEN 'VT-0001'
    WHEN khoHangId = 'KHO-0002' THEN 'VT-0007'
    ELSE 'VT-0001'
END
WHERE viTriId IS NULL OR LTRIM(RTRIM(viTriId)) = '';
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys WHERE name = 'FK_LoHang_ViTriThuoc'
)
BEGIN
    ALTER TABLE dbo.LoHang
    ADD CONSTRAINT FK_LoHang_ViTriThuoc
    FOREIGN KEY (viTriId) REFERENCES dbo.ViTriThuoc(id);
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'IX_LoHang_viTriId'
      AND object_id = OBJECT_ID('dbo.LoHang')
)
BEGIN
    CREATE INDEX IX_LoHang_viTriId ON dbo.LoHang(viTriId);
END
GO

-- ==============================================================================
-- 6. TẠO LẠI CÁC KHÓA NGOẠI (FOREIGN KEYS)
-- ==============================================================================
ALTER TABLE [dbo].[ApDungKhuyenMai] ADD CONSTRAINT [FK_ApDungKhuyenMai_KhuyenMai] FOREIGN KEY([khuyenMaiId]) REFERENCES [dbo].[KhuyenMai] ([id]) ON UPDATE CASCADE ON DELETE CASCADE
ALTER TABLE [dbo].[ApDungKhuyenMai] ADD CONSTRAINT [FK_ApDungKhuyenMai_SanPham] FOREIGN KEY([sanPhamId]) REFERENCES [dbo].[SanPham] ([id]) ON UPDATE CASCADE ON DELETE CASCADE
ALTER TABLE [dbo].[CaLamViec] ADD CONSTRAINT [FK_CaLamViec_NhanVien] FOREIGN KEY([nhanVienId]) REFERENCES [dbo].[NhanVien] ([id]) ON UPDATE CASCADE
ALTER TABLE [dbo].[ChiTietHoaDon] ADD CONSTRAINT [FK_ChiTietHoaDon_DonViDoLuong] FOREIGN KEY([donViDoLuongId], [sanPhamId]) REFERENCES [dbo].[DonViDoLuong] ([id], [sanPhamId])
ALTER TABLE [dbo].[ChiTietHoaDon] ADD CONSTRAINT [FK_ChiTietHoaDon_HoaDon] FOREIGN KEY([hoaDonId]) REFERENCES [dbo].[HoaDon] ([id]) ON UPDATE CASCADE ON DELETE CASCADE
ALTER TABLE [dbo].[ChiTietHoaDon] ADD CONSTRAINT [FK_ChiTietHoaDon_SanPham] FOREIGN KEY([sanPhamId]) REFERENCES [dbo].[SanPham] ([id]) ON UPDATE CASCADE
ALTER TABLE [dbo].[DieuKienKhuyenMai] ADD CONSTRAINT [FK_DieuKienKhuyenMai_KhuyenMai] FOREIGN KEY([khuyenMaiId]) REFERENCES [dbo].[KhuyenMai] ([id]) ON UPDATE CASCADE ON DELETE CASCADE
ALTER TABLE [dbo].[DonViDoLuong] ADD CONSTRAINT [FK_DonViDoLuong_SanPham] FOREIGN KEY([sanPhamId]) REFERENCES [dbo].[SanPham] ([id]) ON UPDATE CASCADE
ALTER TABLE [dbo].[HinhThucKhuyenMai] ADD CONSTRAINT [FK_HinhThucKhuyenMai_DonViDoLuong] FOREIGN KEY([donViDoLuongId], [sanPhamId]) REFERENCES [dbo].[DonViDoLuong] ([id], [sanPhamId]) ON UPDATE CASCADE ON DELETE SET NULL
ALTER TABLE [dbo].[HinhThucKhuyenMai] ADD CONSTRAINT [FK_HinhThucKhuyenMai_KhuyenMai] FOREIGN KEY([khuyenMaiId]) REFERENCES [dbo].[KhuyenMai] ([id]) ON UPDATE CASCADE ON DELETE CASCADE
ALTER TABLE [dbo].[HoaDon] ADD CONSTRAINT [FK_HoaDon_HoaDonGoc] FOREIGN KEY([hoaDonGocId]) REFERENCES [dbo].[HoaDon] ([id])
ALTER TABLE [dbo].[HoaDon] ADD CONSTRAINT [FK_HoaDon_KhachHang] FOREIGN KEY([khachHangId]) REFERENCES [dbo].[KhachHang] ([id]) ON UPDATE CASCADE ON DELETE SET NULL
ALTER TABLE [dbo].[HoaDon] ADD CONSTRAINT [FK_HoaDon_KhuyenMai] FOREIGN KEY([khuyenMaiId]) REFERENCES [dbo].[KhuyenMai] ([id]) ON UPDATE CASCADE ON DELETE SET NULL
ALTER TABLE [dbo].[HoaDon] ADD CONSTRAINT [FK_HoaDon_NhanVien] FOREIGN KEY([nhanVienId]) REFERENCES [dbo].[NhanVien] ([id]) ON UPDATE CASCADE
ALTER TABLE [dbo].[LoHang] ADD CONSTRAINT [FK_LoHang_KhoHang] FOREIGN KEY([khoHangId]) REFERENCES [dbo].[KhoHang] ([id]) ON UPDATE CASCADE
ALTER TABLE [dbo].[LoHang] ADD CONSTRAINT [FK_LoHang_SanPham] FOREIGN KEY([sanPhamId]) REFERENCES [dbo].[SanPham] ([id]) ON UPDATE CASCADE
ALTER TABLE [dbo].[PhanBoLoHang] ADD CONSTRAINT [FK_PhanBoLoHang_ChiTietHoaDon] FOREIGN KEY([hoaDonId], [donViDoLuongId], [sanPhamId]) REFERENCES [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId]) ON DELETE CASCADE
ALTER TABLE [dbo].[PhanBoLoHang] ADD CONSTRAINT [FK_PhanBoLoHang_LoHang] FOREIGN KEY([loHangId]) REFERENCES [dbo].[LoHang] ([id])
ALTER TABLE [dbo].[TaiKhoan] ADD CONSTRAINT [FK_TaiKhoan_NhanVien] FOREIGN KEY([nhanVienId]) REFERENCES [dbo].[NhanVien] ([id]) ON UPDATE CASCADE ON DELETE SET NULL
GO
USE [MYCAREPHARMACY];
GO


-- ==============================================================================
-- RÀNG BUỘC BỔ SUNG CHO NHẬP LÔ / MÃ VẠCH / PHIẾU NHẬP
-- Logic:
-- 1) Cùng sản phẩm + cùng kho + cùng mã lô đang hoạt động chỉ có 1 dòng.
-- 2) Nếu nhập trùng đủ sản phẩm + kho + mã lô + HSD + giá nhập thì BUS/DAO cộng dồn.
-- 3) Nếu cùng sản phẩm + kho + mã lô nhưng khác HSD hoặc giá nhập thì DAO báo lỗi, DB cũng chặn tạo dòng mới.
-- 4) Mỗi lô có mã vạch nội bộ riêng để xem/in lại tem.
-- ==============================================================================
-- Logic lô hàng 2026: cùng sản phẩm + cùng kho + cùng mã lô thì chỉ có một dòng active; nếu nhập lại đúng HSD và giá nhập thì BUS/DAO cộng dồn.
CREATE UNIQUE NONCLUSTERED INDEX [UQ_LoHang_SP_Kho_SoLo_Active]
    ON [dbo].[LoHang]([sanPhamId], [khoHangId], [soLoHang])
    WHERE ([trangThai] <> N'AN');
GO

CREATE UNIQUE NONCLUSTERED INDEX [UQ_LoHang_MaVachNoiBo]
    ON [dbo].[LoHang]([maVachNoiBo])
    WHERE ([maVachNoiBo] IS NOT NULL AND [maVachNoiBo] <> '');
GO

ALTER TABLE [dbo].[PhieuNhapHang]
ADD CONSTRAINT [FK_PhieuNhapHang_NhanVien]
FOREIGN KEY([nhanVienId]) REFERENCES [dbo].[NhanVien]([id])
ON UPDATE CASCADE ON DELETE SET NULL;
GO

ALTER TABLE [dbo].[ChiTietPhieuNhapHang]
ADD CONSTRAINT [FK_CTPN_PhieuNhap]
FOREIGN KEY([phieuNhapId]) REFERENCES [dbo].[PhieuNhapHang]([id])
ON UPDATE CASCADE ON DELETE CASCADE;
GO

ALTER TABLE [dbo].[ChiTietPhieuNhapHang]
ADD CONSTRAINT [FK_CTPN_LoHang]
FOREIGN KEY([loHangId]) REFERENCES [dbo].[LoHang]([id]);
GO

ALTER TABLE [dbo].[ChiTietPhieuNhapHang]
ADD CONSTRAINT [FK_CTPN_SanPham]
FOREIGN KEY([sanPhamId]) REFERENCES [dbo].[SanPham]([id]);
GO

ALTER TABLE [dbo].[ChiTietPhieuNhapHang]
ADD CONSTRAINT [FK_CTPN_KhoHang]
FOREIGN KEY([khoHangId]) REFERENCES [dbo].[KhoHang]([id]);
GO

ALTER TABLE [dbo].[ChiTietPhieuNhapHang]
ADD CONSTRAINT [CK_CTPN_SoLuongNhap] CHECK ([soLuongNhap] > 0);
GO

ALTER TABLE [dbo].[ChiTietPhieuNhapHang]
ADD CONSTRAINT [CK_CTPN_GiaNhap] CHECK ([donGiaNhap] > 0 AND [thanhTien] >= 0);
GO

ALTER TABLE TaiKhoan ALTER COLUMN matKhau VARCHAR(255);
GO

/* =========================================================
   CHUẨN HÓA GIÁ BÁN 2026
   - SanPham.giaBan lưu theo đơn vị cơ bản để code so sánh với giá vốn lô.
   - DonViDoLuong.gia lưu giá bán theo từng đơn vị bán.
   ========================================================= */
UPDATE sp
SET sp.giaBan = dvl.gia
FROM dbo.SanPham sp
INNER JOIN dbo.DonViDoLuong dvl
    ON dvl.sanPhamId = sp.id
   AND dvl.ten = sp.donViDoCoBan
WHERE sp.giaBan <> dvl.gia;
GO


/* =========================================================
   BỔ SUNG MÃ VẠCH ĐƠN VỊ ĐO LƯỜNG 2026
   - SanPham.maVach: mã vạch sản phẩm.
   - DonViDoLuong.maVach: mã vạch theo đơn vị bán, dùng cho quét bán hàng.
   - LoHang.maVachNoiBo: mã vạch nội bộ theo từng lô, dùng cho nhập/xuất/in tem lô.
   ========================================================= */
;WITH dv AS (
    SELECT id, sanPhamId,
           ROW_NUMBER() OVER (ORDER BY sanPhamId, id) AS rn
    FROM dbo.DonViDoLuong
)
UPDATE d
SET maVach = N'893990' + RIGHT('000000' + CAST(dv.rn AS VARCHAR(6)), 6)
FROM dbo.DonViDoLuong d
INNER JOIN dv ON dv.id = d.id AND dv.sanPhamId = d.sanPhamId
WHERE d.maVach IS NULL OR LTRIM(RTRIM(d.maVach)) = '';
GO

/* Đảm bảo toàn bộ lô có mã vạch nội bộ để xem/in lại tem. */
UPDATE dbo.LoHang
SET maVachNoiBo = N'MVLH' + RIGHT('0000' + REPLACE(id, N'LH-', N''), 4)
WHERE maVachNoiBo IS NULL OR LTRIM(RTRIM(maVachNoiBo)) = '';
GO

USE [master]
GO
ALTER DATABASE [MYCAREPHARMACY] SET READ_WRITE
GO

/* =========================================================
   KIỂM TRA NHANH DATA GIÁ 2026
   - Giá vốn lô hàng phải nhỏ hơn giá bán đơn vị cơ bản.
   - Mã vạch nội bộ lô hàng không được NULL.
   ========================================================= */
-- SELECT lh.id, lh.soLoHang, lh.sanPhamId, lh.gia AS giaNhap_DVCB, sp.giaBan AS giaBan_DVCB, lh.maVachNoiBo
-- FROM dbo.LoHang lh
-- JOIN dbo.SanPham sp ON sp.id = lh.sanPhamId
-- WHERE lh.gia >= sp.giaBan OR lh.maVachNoiBo IS NULL OR LTRIM(RTRIM(lh.maVachNoiBo)) = '';
--
-- SELECT * FROM dbo.DonViDoLuong WHERE maVach IS NULL OR LTRIM(RTRIM(maVach)) = '';
-- SELECT maVach, COUNT(*) FROM dbo.DonViDoLuong WHERE maVach IS NOT NULL GROUP BY maVach HAVING COUNT(*) > 1;


/* =========================================================
   PATCH FINAL THEO DAO_SanPham.java HIỆN TẠI
   ========================================================= */

USE [MYCAREPHARMACY];
GO

/* =========================================================
   PATCH FINAL THEO DAO_SanPham.java HIỆN TẠI
   Fix:
   - ViTriThuoc phải có: id, viTriId, khu, ke, tang, moTa
   - SanPham phải có: viTriId
   - MauLieu phải có: comboId, tenCombo, nhomBenh, giaBanCombo, ghiChu, ngayTao, ngaySua
   - ChiTietMauLieu phải có: id, comboId, sanPhamId, dvt, sang, trua, chieu, toi, cachDung, soNgay, tongSoLuong, giaDonVi
   ========================================================= */

PRINT N'===== CHECK DATABASE ĐANG CHẠY PATCH =====';
SELECT DB_NAME() AS CurrentDatabase;
GO

/* =========================================================
   1) FIX ViTriThuoc
   ========================================================= */

IF OBJECT_ID(N'dbo.ViTriThuoc', N'V') IS NOT NULL
BEGIN
    DROP VIEW dbo.ViTriThuoc;
END
GO

IF OBJECT_ID(N'dbo.ViTriThuoc', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ViTriThuoc (
        id NVARCHAR(50) NOT NULL PRIMARY KEY,
        viTriId NVARCHAR(50) NULL,
        khu NVARCHAR(100) NOT NULL,
        ke NVARCHAR(100) NOT NULL,
        tang NVARCHAR(100) NULL,
        moTa NVARCHAR(255) NULL
    );
END
GO

IF COL_LENGTH('dbo.ViTriThuoc', 'id') IS NULL
BEGIN
    ALTER TABLE dbo.ViTriThuoc ADD id NVARCHAR(50) NULL;
END
GO

IF COL_LENGTH('dbo.ViTriThuoc', 'viTriId') IS NULL
BEGIN
    ALTER TABLE dbo.ViTriThuoc ADD viTriId NVARCHAR(50) NULL;
END
GO

IF COL_LENGTH('dbo.ViTriThuoc', 'khu') IS NULL
BEGIN
    ALTER TABLE dbo.ViTriThuoc ADD khu NVARCHAR(100) NULL;
END
GO

IF COL_LENGTH('dbo.ViTriThuoc', 'ke') IS NULL
BEGIN
    ALTER TABLE dbo.ViTriThuoc ADD ke NVARCHAR(100) NULL;
END
GO

IF COL_LENGTH('dbo.ViTriThuoc', 'tang') IS NULL
BEGIN
    ALTER TABLE dbo.ViTriThuoc ADD tang NVARCHAR(100) NULL;
END
GO

IF COL_LENGTH('dbo.ViTriThuoc', 'moTa') IS NULL
BEGIN
    ALTER TABLE dbo.ViTriThuoc ADD moTa NVARCHAR(255) NULL;
END
GO

/* =========================================================
   FIX ViTriThuoc AN TOÀN
   Lưu ý: viTriId trong file này là computed column: viTriId AS (id).
   Vì vậy KHÔNG UPDATE / INSERT trực tiếp vào viTriId.
   tang là INT nên KHÔNG gán chuỗi N'Tầng 1'.
   ========================================================= */

UPDATE dbo.ViTriThuoc
SET khu = ISNULL(NULLIF(khu, ''), ISNULL(NULLIF(khuVuc, ''), N'Khu A')),
    ke = ISNULL(NULLIF(ke, ''), ISNULL(NULLIF(maKe, ''), N'Kệ 01')),
    tang = ISNULL(tang, 1),
    moTa = ISNULL(moTa, N'Vị trí lưu trữ thuốc')
WHERE khu IS NULL OR khu = '' OR ke IS NULL OR ke = '' OR tang IS NULL OR moTa IS NULL;
GO

/* Thêm dữ liệu mẫu phụ nếu chưa có; không insert vào cột computed viTriId */
IF NOT EXISTS (SELECT 1 FROM dbo.ViTriThuoc WHERE id = 'VT-001')
BEGIN
    INSERT INTO dbo.ViTriThuoc(id, maViTri, tenViTri, khuVuc, maKe, tenKe, tang, ngan, moTa, trangThai, khu, ke)
    VALUES
    ('VT-001', 'VT-A-01', N'Khu A - Kệ 01', N'Khu A', N'KE-01', N'Kệ 01', 1, 1, N'Thuốc giảm đau, hạ sốt', N'HOAT_DONG', N'Khu A', N'Kệ 01'),
    ('VT-002', 'VT-A-02', N'Khu A - Kệ 02', N'Khu A', N'KE-02', N'Kệ 02', 1, 1, N'Thuốc kháng sinh', N'HOAT_DONG', N'Khu A', N'Kệ 02'),
    ('VT-003', 'VT-B-01', N'Khu B - Kệ 01', N'Khu B', N'KE-01', N'Kệ 01', 2, 1, N'Thuốc tiêu hóa', N'HOAT_DONG', N'Khu B', N'Kệ 01'),
    ('VT-004', 'VT-B-02', N'Khu B - Kệ 02', N'Khu B', N'KE-02', N'Kệ 02', 2, 1, N'Thuốc hô hấp', N'HOAT_DONG', N'Khu B', N'Kệ 02'),
    ('VT-005', 'VT-C-01', N'Khu C - Kệ 01', N'Khu C', N'KE-01', N'Kệ 01', 1, 1, N'Vitamin và thực phẩm chức năng', N'HOAT_DONG', N'Khu C', N'Kệ 01'),
    ('VT-006', 'VT-TL-01', N'Tủ lạnh - Ngăn 01', N'Tủ lạnh', N'TU-LANH', N'Ngăn 01', 1, 1, N'Hàng cần bảo quản lạnh', N'HOAT_DONG', N'Tủ lạnh', N'Ngăn 01');
END
GO

/* =========================================================
   2) FIX SanPham.viTriId
   ========================================================= */

IF COL_LENGTH('dbo.SanPham', 'viTriId') IS NULL
BEGIN
    ALTER TABLE dbo.SanPham ADD viTriId NVARCHAR(50) NULL;
END
GO

/* Gán vị trí mẫu cho sản phẩm chưa có vị trí */
UPDATE dbo.SanPham
SET viTriId =
    CASE
        WHEN danhMuc LIKE '%KHANG%' OR hoatChat LIKE N'%Amoxicillin%' THEN 'VT-002'
        WHEN hoatChat LIKE N'%Paracetamol%' OR ten LIKE N'%Para%' THEN 'VT-001'
        WHEN ten LIKE N'%Vitamin%' OR danhMuc LIKE '%TPCN%' THEN 'VT-005'
        ELSE 'VT-003'
    END
WHERE viTriId IS NULL OR viTriId = '';
GO

/* =========================================================
   3) FIX MauLieu
   ========================================================= */

IF OBJECT_ID(N'dbo.MauLieu', N'V') IS NOT NULL
BEGIN
    DROP VIEW dbo.MauLieu;
END
GO

IF OBJECT_ID(N'dbo.MauLieu', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.MauLieu (
        comboId NVARCHAR(50) NOT NULL PRIMARY KEY,
        tenCombo NVARCHAR(255) NOT NULL,
        nhomBenh NVARCHAR(100) NULL,
        giaBanCombo FLOAT NOT NULL DEFAULT 0,
        ghiChu NVARCHAR(255) NULL,
        ngayTao DATETIME NOT NULL DEFAULT GETDATE(),
        ngaySua DATETIME NULL
    );
END
GO

IF COL_LENGTH('dbo.MauLieu', 'comboId') IS NULL
BEGIN
    ALTER TABLE dbo.MauLieu ADD comboId NVARCHAR(50) NULL;
END
GO

IF COL_LENGTH('dbo.MauLieu', 'mauLieuId') IS NULL
BEGIN
    ALTER TABLE dbo.MauLieu ADD mauLieuId NVARCHAR(50) NULL;
END
GO

IF COL_LENGTH('dbo.MauLieu', 'tenCombo') IS NULL
BEGIN
    ALTER TABLE dbo.MauLieu ADD tenCombo NVARCHAR(255) NULL;
END
GO

IF COL_LENGTH('dbo.MauLieu', 'tenMauLieu') IS NULL
BEGIN
    ALTER TABLE dbo.MauLieu ADD tenMauLieu NVARCHAR(255) NULL;
END
GO

IF COL_LENGTH('dbo.MauLieu', 'nhomBenh') IS NULL
BEGIN
    ALTER TABLE dbo.MauLieu ADD nhomBenh NVARCHAR(100) NULL;
END
GO

IF COL_LENGTH('dbo.MauLieu', 'giaBanCombo') IS NULL
BEGIN
    ALTER TABLE dbo.MauLieu ADD giaBanCombo FLOAT NOT NULL CONSTRAINT DF_MauLieu_giaBanCombo DEFAULT 0;
END
GO

IF COL_LENGTH('dbo.MauLieu', 'giaBan') IS NULL
BEGIN
    ALTER TABLE dbo.MauLieu ADD giaBan FLOAT NOT NULL CONSTRAINT DF_MauLieu_giaBan DEFAULT 0;
END
GO

IF COL_LENGTH('dbo.MauLieu', 'ghiChu') IS NULL
BEGIN
    ALTER TABLE dbo.MauLieu ADD ghiChu NVARCHAR(255) NULL;
END
GO

IF COL_LENGTH('dbo.MauLieu', 'ngayTao') IS NULL
BEGIN
    ALTER TABLE dbo.MauLieu ADD ngayTao DATETIME NOT NULL CONSTRAINT DF_MauLieu_ngayTao DEFAULT GETDATE();
END
GO

IF COL_LENGTH('dbo.MauLieu', 'ngaySua') IS NULL
BEGIN
    ALTER TABLE dbo.MauLieu ADD ngaySua DATETIME NULL;
END
GO

/* Đồng bộ tên cột cũ/mới */
UPDATE dbo.MauLieu
SET comboId = ISNULL(NULLIF(comboId, ''), mauLieuId)
WHERE (comboId IS NULL OR comboId = '') AND mauLieuId IS NOT NULL;
GO

UPDATE dbo.MauLieu
SET mauLieuId = ISNULL(NULLIF(mauLieuId, ''), comboId)
WHERE (mauLieuId IS NULL OR mauLieuId = '') AND comboId IS NOT NULL;
GO

;WITH x AS (
    SELECT *, ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS rn
    FROM dbo.MauLieu
    WHERE comboId IS NULL OR comboId = ''
)
UPDATE x
SET comboId = 'CB-20260101-' + RIGHT('0000' + CAST(rn AS VARCHAR(10)), 4);
GO

UPDATE dbo.MauLieu
SET mauLieuId = comboId
WHERE mauLieuId IS NULL OR mauLieuId = '';
GO

UPDATE dbo.MauLieu
SET tenCombo = ISNULL(NULLIF(tenCombo, ''), tenMauLieu)
WHERE (tenCombo IS NULL OR tenCombo = '') AND tenMauLieu IS NOT NULL;
GO

UPDATE dbo.MauLieu
SET tenMauLieu = ISNULL(NULLIF(tenMauLieu, ''), tenCombo)
WHERE (tenMauLieu IS NULL OR tenMauLieu = '') AND tenCombo IS NOT NULL;
GO

UPDATE dbo.MauLieu
SET tenCombo = ISNULL(NULLIF(tenCombo, ''), N'Mẫu liều chưa đặt tên'),
    tenMauLieu = ISNULL(NULLIF(tenMauLieu, ''), ISNULL(NULLIF(tenCombo, ''), N'Mẫu liều chưa đặt tên')),
    nhomBenh = ISNULL(NULLIF(nhomBenh, ''), N'Khác'),
    giaBanCombo = CASE WHEN ISNULL(giaBanCombo, 0) <= 0 THEN ISNULL(giaBan, 0) ELSE giaBanCombo END,
    giaBan = CASE WHEN ISNULL(giaBan, 0) <= 0 THEN ISNULL(giaBanCombo, 0) ELSE giaBan END
WHERE tenCombo IS NULL OR tenCombo = '' OR tenMauLieu IS NULL OR tenMauLieu = '';
GO

/* =========================================================
   4) FIX ChiTietMauLieu
   ========================================================= */

IF OBJECT_ID(N'dbo.ChiTietMauLieu', N'V') IS NOT NULL
BEGIN
    DROP VIEW dbo.ChiTietMauLieu;
END
GO

IF OBJECT_ID(N'dbo.ChiTietMauLieu', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ChiTietMauLieu (
        id INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        comboId NVARCHAR(50) NOT NULL,
        sanPhamId NVARCHAR(50) NOT NULL,
        dvt NVARCHAR(50) NULL,
        sang INT NOT NULL DEFAULT 0,
        trua INT NOT NULL DEFAULT 0,
        chieu INT NOT NULL DEFAULT 0,
        toi INT NOT NULL DEFAULT 0,
        cachDung NVARCHAR(255) NULL,
        soNgay INT NOT NULL DEFAULT 1,
        tongSoLuong FLOAT NOT NULL DEFAULT 0,
        giaDonVi FLOAT NOT NULL DEFAULT 0
    );
END
GO

IF COL_LENGTH('dbo.ChiTietMauLieu', 'id') IS NULL
BEGIN
    ALTER TABLE dbo.ChiTietMauLieu ADD id INT IDENTITY(1,1) NOT NULL;
END
GO

IF COL_LENGTH('dbo.ChiTietMauLieu', 'comboId') IS NULL
BEGIN
    ALTER TABLE dbo.ChiTietMauLieu ADD comboId NVARCHAR(50) NULL;
END
GO

IF COL_LENGTH('dbo.ChiTietMauLieu', 'mauLieuId') IS NULL
BEGIN
    ALTER TABLE dbo.ChiTietMauLieu ADD mauLieuId NVARCHAR(50) NULL;
END
GO

IF COL_LENGTH('dbo.ChiTietMauLieu', 'sanPhamId') IS NULL
BEGIN
    ALTER TABLE dbo.ChiTietMauLieu ADD sanPhamId NVARCHAR(50) NULL;
END
GO

IF COL_LENGTH('dbo.ChiTietMauLieu', 'dvt') IS NULL
BEGIN
    ALTER TABLE dbo.ChiTietMauLieu ADD dvt NVARCHAR(50) NULL;
END
GO

IF COL_LENGTH('dbo.ChiTietMauLieu', 'sang') IS NULL
BEGIN
    ALTER TABLE dbo.ChiTietMauLieu ADD sang INT NOT NULL CONSTRAINT DF_CTML_sang DEFAULT 0;
END
GO

IF COL_LENGTH('dbo.ChiTietMauLieu', 'trua') IS NULL
BEGIN
    ALTER TABLE dbo.ChiTietMauLieu ADD trua INT NOT NULL CONSTRAINT DF_CTML_trua DEFAULT 0;
END
GO

IF COL_LENGTH('dbo.ChiTietMauLieu', 'chieu') IS NULL
BEGIN
    ALTER TABLE dbo.ChiTietMauLieu ADD chieu INT NOT NULL CONSTRAINT DF_CTML_chieu DEFAULT 0;
END
GO

IF COL_LENGTH('dbo.ChiTietMauLieu', 'toi') IS NULL
BEGIN
    ALTER TABLE dbo.ChiTietMauLieu ADD toi INT NOT NULL CONSTRAINT DF_CTML_toi DEFAULT 0;
END
GO

IF COL_LENGTH('dbo.ChiTietMauLieu', 'cachDung') IS NULL
BEGIN
    ALTER TABLE dbo.ChiTietMauLieu ADD cachDung NVARCHAR(255) NULL;
END
GO

IF COL_LENGTH('dbo.ChiTietMauLieu', 'soNgay') IS NULL
BEGIN
    ALTER TABLE dbo.ChiTietMauLieu ADD soNgay INT NOT NULL CONSTRAINT DF_CTML_soNgay DEFAULT 1;
END
GO

IF COL_LENGTH('dbo.ChiTietMauLieu', 'tongSoLuong') IS NULL
BEGIN
    ALTER TABLE dbo.ChiTietMauLieu ADD tongSoLuong FLOAT NOT NULL CONSTRAINT DF_CTML_tongSoLuong DEFAULT 0;
END
GO

IF COL_LENGTH('dbo.ChiTietMauLieu', 'giaDonVi') IS NULL
BEGIN
    ALTER TABLE dbo.ChiTietMauLieu ADD giaDonVi FLOAT NOT NULL CONSTRAINT DF_CTML_giaDonVi DEFAULT 0;
END
GO

UPDATE dbo.ChiTietMauLieu
SET comboId = ISNULL(NULLIF(comboId, ''), mauLieuId)
WHERE (comboId IS NULL OR comboId = '') AND mauLieuId IS NOT NULL;
GO

UPDATE dbo.ChiTietMauLieu
SET mauLieuId = ISNULL(NULLIF(mauLieuId, ''), comboId)
WHERE (mauLieuId IS NULL OR mauLieuId = '') AND comboId IS NOT NULL;
GO

/* =========================================================
   5) Dữ liệu mẫu MauLieu + ChiTietMauLieu
   ========================================================= */

IF NOT EXISTS (SELECT 1 FROM dbo.MauLieu WHERE comboId = 'CB-20260101-0001')
BEGIN
    INSERT INTO dbo.MauLieu(comboId, mauLieuId, tenCombo, tenMauLieu, nhomBenh, giaBanCombo, giaBan, ghiChu, ngayTao)
    VALUES
    ('CB-20260101-0001', 'CB-20260101-0001', N'Combo cảm cúm thông thường', N'Combo cảm cúm thông thường', N'Hô hấp', 85000, 85000, N'Dùng cho triệu chứng cảm cúm nhẹ', GETDATE()),
    ('CB-20260101-0002', 'CB-20260101-0002', N'Combo đau dạ dày nhẹ', N'Combo đau dạ dày nhẹ', N'Tiêu hóa', 95000, 95000, N'Dùng theo hướng dẫn dược sĩ', GETDATE()),
    ('CB-20260101-0003', 'CB-20260101-0003', N'Combo đau nhức cơ xương', N'Combo đau nhức cơ xương', N'Xương khớp – Gút', 120000, 120000, N'Dùng ngắn ngày', GETDATE());
END
GO

/* Chỉ insert chi tiết nếu có sản phẩm tương ứng trong DB */
IF OBJECT_ID(N'dbo.SanPham', N'U') IS NOT NULL
BEGIN
    IF NOT EXISTS (SELECT 1 FROM dbo.ChiTietMauLieu WHERE comboId = 'CB-20260101-0001')
    BEGIN
        INSERT INTO dbo.ChiTietMauLieu(comboId, mauLieuId, sanPhamId, dvt, sang, trua, chieu, toi, cachDung, soNgay, tongSoLuong, giaDonVi)
        SELECT TOP 1 'CB-20260101-0001', 'CB-20260101-0001', id, ISNULL(donViDoCoBan, N'Viên'), 1, 0, 1, 0, N'Uống sau ăn', 3, 6, ISNULL(giaBan, 0)
        FROM dbo.SanPham
        WHERE ISNULL(trangThai,'HOAT_DONG') <> 'AN'
        ORDER BY id;

        INSERT INTO dbo.ChiTietMauLieu(comboId, mauLieuId, sanPhamId, dvt, sang, trua, chieu, toi, cachDung, soNgay, tongSoLuong, giaDonVi)
        SELECT TOP 1 'CB-20260101-0002', 'CB-20260101-0002', id, ISNULL(donViDoCoBan, N'Viên'), 1, 1, 1, 0, N'Uống trước ăn', 3, 9, ISNULL(giaBan, 0)
        FROM dbo.SanPham
        WHERE ISNULL(trangThai,'HOAT_DONG') <> 'AN'
        ORDER BY id DESC;

        INSERT INTO dbo.ChiTietMauLieu(comboId, mauLieuId, sanPhamId, dvt, sang, trua, chieu, toi, cachDung, soNgay, tongSoLuong, giaDonVi)
        SELECT TOP 1 'CB-20260101-0003', 'CB-20260101-0003', id, ISNULL(donViDoCoBan, N'Viên'), 1, 0, 1, 0, N'Uống sau ăn', 5, 10, ISNULL(giaBan, 0)
        FROM dbo.SanPham
        WHERE ISNULL(trangThai,'HOAT_DONG') <> 'AN'
        ORDER BY ten;
    END
END
GO

/* =========================================================
   6) TEST CUỐI FILE
   ========================================================= */

PRINT N'===== TEST CỘT SAU KHI PATCH =====';

SELECT TOP 10
    id,
    viTriId,
    khu,
    ke,
    tang,
    moTa
FROM dbo.ViTriThuoc
ORDER BY id;

SELECT TOP 10
    comboId,
    tenCombo,
    nhomBenh,
    giaBanCombo,
    ghiChu
FROM dbo.MauLieu
ORDER BY tenCombo;

SELECT TOP 10
    id,
    comboId,
    sanPhamId,
    dvt,
    sang,
    trua,
    chieu,
    toi,
    soNgay,
    tongSoLuong,
    giaDonVi
FROM dbo.ChiTietMauLieu
ORDER BY id;

PRINT N'===== PATCH FINAL THEO DAO_SanPham.java ĐÃ CHẠY XONG =====';
GO
-- Xóa trigger cũ
DROP TRIGGER IF EXISTS [dbo].[TRG_DongBoGiaBan_LoHang];
GO
CREATE TRIGGER TRG_DongBoGiaBan_LoHang
ON LoHang
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE SP
    SET SP.giaBan = I.gia * 1.3
    FROM SanPham SP
    JOIN inserted I ON SP.id = I.sanPhamId
    WHERE SP.giaBan = 0;
END;
GO
/* =============================================================
   MYCAREPHARMACY – PATCH BỔ SUNG 2026
   Nội dung:
   1. ViTriThuoc bổ sung (VT-0009 → VT-0016)
   2. SanPham mới (SP2024-0041 → SP2024-0060)  – 20 sản phẩm
   3. DonViDoLuong cho sản phẩm mới  (DVL-0067 → DVL-0104)
   4. LoHang tương ứng               (LH-0041  → LH-0060)
   5. PhieuNhapHang + ChiTietPhieu   (PN-0041  → PN-0060)
   6. MauLieu – 42 combo theo nhóm bệnh × lứa tuổi
   7. ChiTietMauLieu cho từng combo
   ============================================================= */

USE [MYCAREPHARMACY];
GO

/* ============================================================
   1. VỊ TRÍ KỆ BỔ SUNG
   ============================================================ */
INSERT INTO dbo.ViTriThuoc(id,maViTri,tenViTri,khuVuc,maKe,tenKe,tang,ngan,moTa,trangThai,khu,ke)
VALUES
(N'VT-0009',N'VT-A2-01',N'Kệ A2 - Ngăn 01',N'Quầy bán thuốc',N'KE-A2',N'Kệ kháng sinh bổ sung',1,1,N'Kháng sinh bổ sung, beta-lactam, macrolide',N'HOAT_DONG',N'Khu A',N'Kệ A2'),
(N'VT-0010',N'VT-A2-02',N'Kệ A2 - Ngăn 02',N'Quầy bán thuốc',N'KE-A2',N'Kệ thuốc tim mạch bổ sung',1,2,N'Lợi tiểu, corticoid, giãn phế quản',N'HOAT_DONG',N'Khu A',N'Kệ A2'),
(N'VT-0011',N'VT-B2-01',N'Kệ B2 - Ngăn 01',N'Quầy OTC',N'KE-B2',N'Kệ OTC bổ sung',1,1,N'Ho, dị ứng thế hệ mới, siro trẻ em',N'HOAT_DONG',N'Khu B',N'Kệ B2'),
(N'VT-0012',N'VT-B2-02',N'Kệ B2 - Ngăn 02',N'Quầy OTC',N'KE-B2',N'Kệ nhuận tràng – bù nước',1,2,N'Nhuận tràng, men tiêu hóa, bù nước',N'HOAT_DONG',N'Khu B',N'Kệ B2'),
(N'VT-0013',N'VT-C2-01',N'Kệ C2 - Ngăn 01',N'Khu vitamin',N'KE-C2',N'Kệ khoáng chất bổ sung',1,1,N'Kẽm, sắt, biotin, vitamin nhóm B',N'HOAT_DONG',N'Khu C',N'Kệ C2'),
(N'VT-0014',N'VT-C2-02',N'Kệ C2 - Ngăn 02',N'Khu vitamin',N'KE-C2',N'Kệ men vi sinh',1,2,N'Men vi sinh, Enterogermina, Probiotics',N'HOAT_DONG',N'Khu C',N'Kệ C2'),
(N'VT-0015',N'VT-D2-01',N'Kệ D2 - Ngăn 01',N'Quầy dung dịch',N'KE-D2',N'Kệ dung dịch – muối',1,1,N'NaCl 0.9%, dung dịch bù nước',N'HOAT_DONG',N'Khu D',N'Kệ D2'),
(N'VT-0016',N'VT-KHO-03',N'Kho dự trữ - Tủ 03',N'Kho dự trữ',N'TU-03',N'Tủ thuốc kê đơn bổ sung',1,3,N'Thuốc kê đơn dự trữ lâu dài',N'HOAT_DONG',N'Kho dự trữ',N'Tủ 03');
GO

/* ============================================================
   2. SẢN PHẨM MỚI (SP2024-0041 → SP2024-0060)
   ============================================================ */
INSERT INTO [dbo].[SanPham]
    (id, danhMuc, dang, ten, tenVietTat, nhaSanXuat, hoatChat, thueVAT, hamLuong, moTa, donViDoCoBan, ngayTao, trangThai, giaBan, maVach, nhomBenhLy, viTriId)
VALUES
(N'SP2024-0041',N'THUOC_KHONG_KE_DON',N'VIEN_NEN',N'Dextromethorphan 15mg',N'Dextro 15',N'Pymepharco',N'Dextromethorphan HBr',CAST(5.00 AS DECIMAL(18,2)),N'15mg',N'Ức chế ho trung ương, không gây buồn ngủ',N'Viên',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(1200.00 AS DECIMAL(18,2)),N'8934683100011',N'Dị ứng – Mẩn ngứa – Mề đay',NULL),
(N'SP2024-0042',N'THUOC_KHONG_KE_DON',N'VIEN_NEN',N'Diphenhydramine 25mg',N'Diphen 25',N'Domesco',N'Diphenhydramine HCl',CAST(5.00 AS DECIMAL(18,2)),N'25mg',N'Kháng histamine thế hệ 1, an thần nhẹ',N'Viên',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(1500.00 AS DECIMAL(18,2)),N'8934683100028',N'Dị ứng – Mẩn ngứa – Mề đay',NULL),
(N'SP2024-0043',N'THUC_PHAM_CHUC_NANG',N'VIEN_NEN',N'Zinc Gluconate 10mg',N'Zinc 10',N'DHG Pharma',N'Zinc gluconate',CAST(10.00 AS DECIMAL(18,2)),N'10mg',N'Bổ sung kẽm, tăng đề kháng',N'Viên',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(2500.00 AS DECIMAL(18,2)),N'8934683100035',N'Thuốc bổ – Vitamin – Khoáng chất',NULL),
(N'SP2024-0044',N'THUC_PHAM_CHUC_NANG',N'VIEN_NEN',N'Sắt (II) Sulfat 300mg',N'Fe Sulfat',N'Traphaco',N'Ferrous sulfate',CAST(10.00 AS DECIMAL(18,2)),N'300mg',N'Bổ sung sắt, hỗ trợ thiếu máu thiếu sắt',N'Viên',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(1800.00 AS DECIMAL(18,2)),N'8934683100042',N'Thuốc bổ – Vitamin – Khoáng chất',NULL),
(N'SP2024-0045',N'THUC_PHAM_CHUC_NANG',N'VIEN_NEN',N'Vitamin B Complex',N'B Complex',N'Imexpharm',N'B1+B2+B3+B5+B6+B12',CAST(10.00 AS DECIMAL(18,2)),N'Chưa cập nhật',N'Tổng hợp vitamin nhóm B, bổ sung năng lượng',N'Viên',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(2000.00 AS DECIMAL(18,2)),N'8934683100059',N'Thuốc bổ – Vitamin – Khoáng chất',NULL),
(N'SP2024-0046',N'THUOC_KHONG_KE_DON',N'HON_DICH',N'Ibuprofen 100mg/5ml Siro Trẻ em',N'Ibu Siro TE',N'Stada VN',N'Ibuprofen',CAST(5.00 AS DECIMAL(18,2)),N'100mg/5ml',N'Giảm đau hạ sốt cho trẻ từ 3 tháng',N'Chai',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(65000.00 AS DECIMAL(18,2)),N'8934683100066',N'Đau đầu – Giảm đau – Hạ sốt',NULL),
(N'SP2024-0047',N'THUC_PHAM_CHUC_NANG',N'DUNG_DICH',N'Enterogermina 5ml',N'Entero 5ml',N'Sanofi',N'Bacillus clausii',CAST(10.00 AS DECIMAL(18,2)),N'5ml',N'Men vi sinh, phục hồi hệ vi khuẩn ruột',N'Lọ',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(18000.00 AS DECIMAL(18,2)),N'8934683100073',N'Dạ dày – Tiêu hóa – Đại tràng',NULL),
(N'SP2024-0048',N'THUOC_KHONG_KE_DON',N'THUOC_BOT',N'Sorbitol 5g Bột',N'Sorbitol 5g',N'Vinpharco',N'Sorbitol',CAST(5.00 AS DECIMAL(18,2)),N'5g',N'Nhuận tràng thẩm thấu, điều trị táo bón',N'Gói',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(5000.00 AS DECIMAL(18,2)),N'8934683100080',N'Dạ dày – Tiêu hóa – Đại tràng',NULL),
(N'SP2024-0049',N'THUOC_KE_DON',N'VIEN_NEN',N'Metronidazole 250mg',N'Metro 250',N'Pymepharco',N'Metronidazole',CAST(5.00 AS DECIMAL(18,2)),N'250mg',N'Kháng ký sinh trùng, điều trị nhiễm trùng ruột',N'Viên',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(1500.00 AS DECIMAL(18,2)),N'8934683100097',N'Ho – Đờm – Viêm họng',NULL),
(N'SP2024-0050',N'THUOC_KE_DON',N'VIEN_NEN',N'Clarithromycin 250mg',N'Clari 250',N'Stada VN',N'Clarithromycin',CAST(5.00 AS DECIMAL(18,2)),N'250mg',N'Kháng sinh macrolide thế hệ mới',N'Viên',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(5500.00 AS DECIMAL(18,2)),N'8934683100103',N'Ho – Đờm – Viêm họng',NULL),
(N'SP2024-0051',N'THUOC_KE_DON',N'VIEN_NEN',N'Furosemide 40mg',N'Furo 40',N'Traphaco',N'Furosemide',CAST(5.00 AS DECIMAL(18,2)),N'40mg',N'Lợi tiểu quai, điều trị phù tim mạch',N'Viên',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(1000.00 AS DECIMAL(18,2)),N'8934683100110',N'Tim mạch – Huyết áp',NULL),
(N'SP2024-0052',N'THUOC_KE_DON',N'VIEN_NEN',N'Prednisolone 5mg',N'Predni 5',N'Imexpharm',N'Prednisolone',CAST(5.00 AS DECIMAL(18,2)),N'5mg',N'Corticosteroid, kháng viêm dị ứng mạnh',N'Viên',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(1200.00 AS DECIMAL(18,2)),N'8934683100127',N'Ho – Đờm – Viêm họng',NULL),
(N'SP2024-0053',N'THUOC_KE_DON',N'VIEN_NEN',N'Salbutamol 4mg',N'Salbu 4',N'OPV',N'Salbutamol sulfate',CAST(5.00 AS DECIMAL(18,2)),N'4mg',N'Giãn phế quản, điều trị hen phế quản',N'Viên',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(2000.00 AS DECIMAL(18,2)),N'8934683100134',N'Dị ứng – Mẩn ngứa – Mề đay',NULL),
(N'SP2024-0054',N'THUOC_KE_DON',N'VIEN_NANG',N'Pantoprazole 40mg',N'Panto 40',N'Stada VN',N'Pantoprazole sodium',CAST(5.00 AS DECIMAL(18,2)),N'40mg',N'Ức chế bơm proton mạnh, loét dạ dày tá tràng',N'Viên',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(4500.00 AS DECIMAL(18,2)),N'8934683100141',N'Dạ dày – Tiêu hóa – Đại tràng',NULL),
(N'SP2024-0055',N'THUOC_KHONG_KE_DON',N'DUNG_DICH',N'Nước muối NaCl 0.9%',N'NaCl 0.9%',N'Mekophar',N'Sodium chloride',CAST(5.00 AS DECIMAL(18,2)),N'0.9% 500ml',N'Rửa vết thương, vệ sinh mũi, pha dịch',N'Chai',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(25000.00 AS DECIMAL(18,2)),N'8934683100158',N'Dị ứng – Mẩn ngứa – Mề đay',NULL),
(N'SP2024-0056',N'THUOC_KE_DON',N'VIEN_NEN',N'Glibenclamide 5mg',N'Glib 5',N'Traphaco',N'Glibenclamide',CAST(5.00 AS DECIMAL(18,2)),N'5mg',N'Sulfonylurea, điều trị đái tháo đường type 2',N'Viên',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(1500.00 AS DECIMAL(18,2)),N'8934683100165',N'Đái tháo đường',NULL),
(N'SP2024-0057',N'THUOC_KE_DON',N'VIEN_NEN',N'Simvastatin 20mg',N'Simva 20',N'Domesco',N'Simvastatin',CAST(5.00 AS DECIMAL(18,2)),N'20mg',N'Statin, giảm cholesterol toàn phần và LDL',N'Viên',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(2500.00 AS DECIMAL(18,2)),N'8934683100172',N'Tim mạch – Huyết áp',NULL),
(N'SP2024-0058',N'THUC_PHAM_CHUC_NANG',N'VIEN_NEN',N'Vitamin B1 250mg',N'VitB1 250',N'DHG Pharma',N'Thiamine hydrochloride',CAST(10.00 AS DECIMAL(18,2)),N'250mg',N'Bổ sung B1, hỗ trợ thần kinh',N'Viên',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(1500.00 AS DECIMAL(18,2)),N'8934683100189',N'Thuốc bổ – Vitamin – Khoáng chất',NULL),
(N'SP2024-0059',N'THUC_PHAM_CHUC_NANG',N'VIEN_NEN',N'Biotin 5000mcg',N'Biotin 5K',N'Nature Made',N'D-Biotin',CAST(10.00 AS DECIMAL(18,2)),N'5000mcg',N'Hỗ trợ tóc móng da, chống rụng tóc',N'Viên',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(8000.00 AS DECIMAL(18,2)),N'8934683100196',N'Dược mỹ phẩm – Da liễu',NULL),
(N'SP2024-0060',N'THUOC_KHONG_KE_DON',N'VIEN_NEN',N'Clorpheniramine 4mg',N'Clorphen 4',N'Pymepharco',N'Chlorphenamine maleate',CAST(5.00 AS DECIMAL(18,2)),N'4mg',N'Kháng histamine thế hệ 1, giá rẻ phổ biến',N'Viên',CAST(N'2026-05-23' AS DATETIME2),N'HOAT_DONG',CAST(800.00 AS DECIMAL(18,2)),N'8934683100202',N'Dị ứng – Mẩn ngứa – Mề đay',NULL);
GO

/* ============================================================
   3. ĐƠN VỊ ĐO LƯỜNG
   ============================================================ */
INSERT [dbo].[DonViDoLuong] VALUES
(N'DVL-0067',N'SP2024-0041',N'Viên',CAST(1.00 AS DECIMAL(18,2)),CAST(1200.00 AS DECIMAL(18,2)),N'DVDL00410067'),
(N'DVL-0068',N'SP2024-0041',N'Hộp', CAST(10.00 AS DECIMAL(18,2)),CAST(12000.00 AS DECIMAL(18,2)),N'DVDL00410068'),
(N'DVL-0069',N'SP2024-0042',N'Viên',CAST(1.00 AS DECIMAL(18,2)),CAST(1500.00 AS DECIMAL(18,2)),N'DVDL00420069'),
(N'DVL-0070',N'SP2024-0042',N'Hộp', CAST(20.00 AS DECIMAL(18,2)),CAST(30000.00 AS DECIMAL(18,2)),N'DVDL00420070'),
(N'DVL-0071',N'SP2024-0043',N'Viên',CAST(1.00 AS DECIMAL(18,2)),CAST(2500.00 AS DECIMAL(18,2)),N'DVDL00430071'),
(N'DVL-0072',N'SP2024-0043',N'Hộp', CAST(30.00 AS DECIMAL(18,2)),CAST(75000.00 AS DECIMAL(18,2)),N'DVDL00430072'),
(N'DVL-0073',N'SP2024-0044',N'Viên',CAST(1.00 AS DECIMAL(18,2)),CAST(1800.00 AS DECIMAL(18,2)),N'DVDL00440073'),
(N'DVL-0074',N'SP2024-0044',N'Hộp', CAST(30.00 AS DECIMAL(18,2)),CAST(54000.00 AS DECIMAL(18,2)),N'DVDL00440074'),
(N'DVL-0075',N'SP2024-0045',N'Viên',CAST(1.00 AS DECIMAL(18,2)),CAST(2000.00 AS DECIMAL(18,2)),N'DVDL00450075'),
(N'DVL-0076',N'SP2024-0045',N'Hộp', CAST(30.00 AS DECIMAL(18,2)),CAST(60000.00 AS DECIMAL(18,2)),N'DVDL00450076'),
(N'DVL-0077',N'SP2024-0046',N'Chai',CAST(1.00 AS DECIMAL(18,2)),CAST(65000.00 AS DECIMAL(18,2)),N'DVDL00460077'),
(N'DVL-0078',N'SP2024-0047',N'Lọ',  CAST(1.00 AS DECIMAL(18,2)),CAST(18000.00 AS DECIMAL(18,2)),N'DVDL00470078'),
(N'DVL-0079',N'SP2024-0047',N'Hộp', CAST(10.00 AS DECIMAL(18,2)),CAST(180000.00 AS DECIMAL(18,2)),N'DVDL00470079'),
(N'DVL-0080',N'SP2024-0048',N'Gói', CAST(1.00 AS DECIMAL(18,2)),CAST(5000.00 AS DECIMAL(18,2)),N'DVDL00480080'),
(N'DVL-0081',N'SP2024-0048',N'Hộp', CAST(20.00 AS DECIMAL(18,2)),CAST(100000.00 AS DECIMAL(18,2)),N'DVDL00480081'),
(N'DVL-0082',N'SP2024-0049',N'Viên',CAST(1.00 AS DECIMAL(18,2)),CAST(1500.00 AS DECIMAL(18,2)),N'DVDL00490082'),
(N'DVL-0083',N'SP2024-0049',N'Hộp', CAST(30.00 AS DECIMAL(18,2)),CAST(45000.00 AS DECIMAL(18,2)),N'DVDL00490083'),
(N'DVL-0084',N'SP2024-0050',N'Viên',CAST(1.00 AS DECIMAL(18,2)),CAST(5500.00 AS DECIMAL(18,2)),N'DVDL00500084'),
(N'DVL-0085',N'SP2024-0050',N'Hộp', CAST(14.00 AS DECIMAL(18,2)),CAST(77000.00 AS DECIMAL(18,2)),N'DVDL00500085'),
(N'DVL-0086',N'SP2024-0051',N'Viên',CAST(1.00 AS DECIMAL(18,2)),CAST(1000.00 AS DECIMAL(18,2)),N'DVDL00510086'),
(N'DVL-0087',N'SP2024-0051',N'Hộp', CAST(30.00 AS DECIMAL(18,2)),CAST(30000.00 AS DECIMAL(18,2)),N'DVDL00510087'),
(N'DVL-0088',N'SP2024-0052',N'Viên',CAST(1.00 AS DECIMAL(18,2)),CAST(1200.00 AS DECIMAL(18,2)),N'DVDL00520088'),
(N'DVL-0089',N'SP2024-0052',N'Hộp', CAST(30.00 AS DECIMAL(18,2)),CAST(36000.00 AS DECIMAL(18,2)),N'DVDL00520089'),
(N'DVL-0090',N'SP2024-0053',N'Viên',CAST(1.00 AS DECIMAL(18,2)),CAST(2000.00 AS DECIMAL(18,2)),N'DVDL00530090'),
(N'DVL-0091',N'SP2024-0053',N'Hộp', CAST(30.00 AS DECIMAL(18,2)),CAST(60000.00 AS DECIMAL(18,2)),N'DVDL00530091'),
(N'DVL-0092',N'SP2024-0054',N'Viên',CAST(1.00 AS DECIMAL(18,2)),CAST(4500.00 AS DECIMAL(18,2)),N'DVDL00540092'),
(N'DVL-0093',N'SP2024-0054',N'Hộp', CAST(14.00 AS DECIMAL(18,2)),CAST(63000.00 AS DECIMAL(18,2)),N'DVDL00540093'),
(N'DVL-0094',N'SP2024-0055',N'Chai',CAST(1.00 AS DECIMAL(18,2)),CAST(25000.00 AS DECIMAL(18,2)),N'DVDL00550094'),
(N'DVL-0095',N'SP2024-0056',N'Viên',CAST(1.00 AS DECIMAL(18,2)),CAST(1500.00 AS DECIMAL(18,2)),N'DVDL00560095'),
(N'DVL-0096',N'SP2024-0056',N'Hộp', CAST(30.00 AS DECIMAL(18,2)),CAST(45000.00 AS DECIMAL(18,2)),N'DVDL00560096'),
(N'DVL-0097',N'SP2024-0057',N'Viên',CAST(1.00 AS DECIMAL(18,2)),CAST(2500.00 AS DECIMAL(18,2)),N'DVDL00570097'),
(N'DVL-0098',N'SP2024-0057',N'Hộp', CAST(30.00 AS DECIMAL(18,2)),CAST(75000.00 AS DECIMAL(18,2)),N'DVDL00570098'),
(N'DVL-0099',N'SP2024-0058',N'Viên',CAST(1.00 AS DECIMAL(18,2)),CAST(1500.00 AS DECIMAL(18,2)),N'DVDL00580099'),
(N'DVL-0100',N'SP2024-0058',N'Hộp', CAST(30.00 AS DECIMAL(18,2)),CAST(45000.00 AS DECIMAL(18,2)),N'DVDL00580100'),
(N'DVL-0101',N'SP2024-0059',N'Viên',CAST(1.00 AS DECIMAL(18,2)),CAST(8000.00 AS DECIMAL(18,2)),N'DVDL00590101'),
(N'DVL-0102',N'SP2024-0059',N'Hộp', CAST(30.00 AS DECIMAL(18,2)),CAST(240000.00 AS DECIMAL(18,2)),N'DVDL00590102'),
(N'DVL-0103',N'SP2024-0060',N'Viên',CAST(1.00 AS DECIMAL(18,2)),CAST(800.00 AS DECIMAL(18,2)),N'DVDL00600103'),
(N'DVL-0104',N'SP2024-0060',N'Hộp', CAST(20.00 AS DECIMAL(18,2)),CAST(16000.00 AS DECIMAL(18,2)),N'DVDL00600104');
GO

/* ============================================================
   4. LÔ HÀNG (giaNhap ~70% giaBan)
   ============================================================ */
INSERT [dbo].[LoHang] VALUES
(N'LH-0041',N'LOT-DEX-260501',N'SP2024-0041',200,CAST(840.00 AS DECIMAL(18,2)),CAST(N'2028-06-30' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-05T09:00:00' AS DATETIME2),N'KHO-0001',N'MVLH0041',N'VT-0011'),
(N'LH-0042',N'LOT-DPH-260502',N'SP2024-0042',150,CAST(1050.00 AS DECIMAL(18,2)),CAST(N'2028-07-31' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-05T09:00:00' AS DATETIME2),N'KHO-0001',N'MVLH0042',N'VT-0011'),
(N'LH-0043',N'LOT-ZNC-260503',N'SP2024-0043',120,CAST(1750.00 AS DECIMAL(18,2)),CAST(N'2028-08-31' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-08T09:00:00' AS DATETIME2),N'KHO-0002',N'MVLH0043',N'VT-0013'),
(N'LH-0044',N'LOT-FES-260504',N'SP2024-0044',100,CAST(1260.00 AS DECIMAL(18,2)),CAST(N'2028-09-30' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-08T09:00:00' AS DATETIME2),N'KHO-0002',N'MVLH0044',N'VT-0013'),
(N'LH-0045',N'LOT-BCP-260505',N'SP2024-0045',150,CAST(1400.00 AS DECIMAL(18,2)),CAST(N'2028-10-31' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-08T09:00:00' AS DATETIME2),N'KHO-0002',N'MVLH0045',N'VT-0013'),
(N'LH-0046',N'LOT-IBS-260506',N'SP2024-0046', 80,CAST(45500.00 AS DECIMAL(18,2)),CAST(N'2028-11-30' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-10T09:00:00' AS DATETIME2),N'KHO-0001',N'MVLH0046',N'VT-0011'),
(N'LH-0047',N'LOT-ENT-260507',N'SP2024-0047',200,CAST(12600.00 AS DECIMAL(18,2)),CAST(N'2028-12-31' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-10T09:00:00' AS DATETIME2),N'KHO-0002',N'MVLH0047',N'VT-0014'),
(N'LH-0048',N'LOT-SOR-260508',N'SP2024-0048',200,CAST(3500.00 AS DECIMAL(18,2)),CAST(N'2029-01-31' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-10T09:00:00' AS DATETIME2),N'KHO-0001',N'MVLH0048',N'VT-0012'),
(N'LH-0049',N'LOT-MET-260509',N'SP2024-0049',300,CAST(1050.00 AS DECIMAL(18,2)),CAST(N'2029-02-28' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-13T09:00:00' AS DATETIME2),N'KHO-0001',N'MVLH0049',N'VT-0009'),
(N'LH-0050',N'LOT-CLA-260510',N'SP2024-0050',150,CAST(3850.00 AS DECIMAL(18,2)),CAST(N'2029-03-31' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-13T09:00:00' AS DATETIME2),N'KHO-0001',N'MVLH0050',N'VT-0009'),
(N'LH-0051',N'LOT-FUR-260511',N'SP2024-0051',250,CAST(700.00 AS DECIMAL(18,2)),CAST(N'2029-04-30' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-15T09:00:00' AS DATETIME2),N'KHO-0001',N'MVLH0051',N'VT-0010'),
(N'LH-0052',N'LOT-PRE-260512',N'SP2024-0052',200,CAST(840.00 AS DECIMAL(18,2)),CAST(N'2029-05-31' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-15T09:00:00' AS DATETIME2),N'KHO-0001',N'MVLH0052',N'VT-0010'),
(N'LH-0053',N'LOT-SAL-260513',N'SP2024-0053',200,CAST(1400.00 AS DECIMAL(18,2)),CAST(N'2029-06-30' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-15T09:00:00' AS DATETIME2),N'KHO-0001',N'MVLH0053',N'VT-0010'),
(N'LH-0054',N'LOT-PAN-260514',N'SP2024-0054',120,CAST(3150.00 AS DECIMAL(18,2)),CAST(N'2029-07-31' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-17T09:00:00' AS DATETIME2),N'KHO-0001',N'MVLH0054',N'VT-0009'),
(N'LH-0055',N'LOT-NCL-260515',N'SP2024-0055',100,CAST(17500.00 AS DECIMAL(18,2)),CAST(N'2029-08-31' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-17T09:00:00' AS DATETIME2),N'KHO-0001',N'MVLH0055',N'VT-0015'),
(N'LH-0056',N'LOT-GLI-260516',N'SP2024-0056',200,CAST(1050.00 AS DECIMAL(18,2)),CAST(N'2029-09-30' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-19T09:00:00' AS DATETIME2),N'KHO-0001',N'MVLH0056',N'VT-0010'),
(N'LH-0057',N'LOT-SIM-260517',N'SP2024-0057',180,CAST(1750.00 AS DECIMAL(18,2)),CAST(N'2029-10-31' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-19T09:00:00' AS DATETIME2),N'KHO-0001',N'MVLH0057',N'VT-0010'),
(N'LH-0058',N'LOT-VB1-260518',N'SP2024-0058',150,CAST(1050.00 AS DECIMAL(18,2)),CAST(N'2029-11-30' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-21T09:00:00' AS DATETIME2),N'KHO-0002',N'MVLH0058',N'VT-0013'),
(N'LH-0059',N'LOT-BIO-260519',N'SP2024-0059', 60,CAST(5600.00 AS DECIMAL(18,2)),CAST(N'2029-12-31' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-21T09:00:00' AS DATETIME2),N'KHO-0002',N'MVLH0059',N'VT-0013'),
(N'LH-0060',N'LOT-CPH-260520',N'SP2024-0060',300,CAST(560.00 AS DECIMAL(18,2)),CAST(N'2030-01-31' AS DATETIME2),N'CON_HANG',CAST(N'2026-05-23T09:00:00' AS DATETIME2),N'KHO-0001',N'MVLH0060',N'VT-0011');
GO

/* ============================================================
   5. PHIẾU NHẬP
   ============================================================ */
INSERT [dbo].[PhieuNhapHang] VALUES
(N'PN-0041',CAST(N'2026-05-05T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(168000.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-DEX-260501',N'HOAN_THANH'),
(N'PN-0042',CAST(N'2026-05-05T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(157500.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-DPH-260502',N'HOAN_THANH'),
(N'PN-0043',CAST(N'2026-05-08T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(210000.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-ZNC-260503',N'HOAN_THANH'),
(N'PN-0044',CAST(N'2026-05-08T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(126000.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-FES-260504',N'HOAN_THANH'),
(N'PN-0045',CAST(N'2026-05-08T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(210000.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-BCP-260505',N'HOAN_THANH'),
(N'PN-0046',CAST(N'2026-05-10T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(3640000.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-IBS-260506',N'HOAN_THANH'),
(N'PN-0047',CAST(N'2026-05-10T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(2520000.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-ENT-260507',N'HOAN_THANH'),
(N'PN-0048',CAST(N'2026-05-10T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(700000.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-SOR-260508',N'HOAN_THANH'),
(N'PN-0049',CAST(N'2026-05-13T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(315000.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-MET-260509',N'HOAN_THANH'),
(N'PN-0050',CAST(N'2026-05-13T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(577500.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-CLA-260510',N'HOAN_THANH'),
(N'PN-0051',CAST(N'2026-05-15T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(175000.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-FUR-260511',N'HOAN_THANH'),
(N'PN-0052',CAST(N'2026-05-15T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(168000.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-PRE-260512',N'HOAN_THANH'),
(N'PN-0053',CAST(N'2026-05-15T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(280000.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-SAL-260513',N'HOAN_THANH'),
(N'PN-0054',CAST(N'2026-05-17T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(378000.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-PAN-260514',N'HOAN_THANH'),
(N'PN-0055',CAST(N'2026-05-17T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(1750000.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-NCL-260515',N'HOAN_THANH'),
(N'PN-0056',CAST(N'2026-05-19T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(210000.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-GLI-260516',N'HOAN_THANH'),
(N'PN-0057',CAST(N'2026-05-19T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(315000.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-SIM-260517',N'HOAN_THANH'),
(N'PN-0058',CAST(N'2026-05-21T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(157500.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-VB1-260518',N'HOAN_THANH'),
(N'PN-0059',CAST(N'2026-05-21T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(336000.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-BIO-260519',N'HOAN_THANH'),
(N'PN-0060',CAST(N'2026-05-23T09:00:00' AS DATETIME2),NULL,N'QL-0001',CAST(168000.00 AS DECIMAL(18,2)),N'Nhập mới - LOT-CPH-260520',N'HOAN_THANH');
GO

INSERT [dbo].[ChiTietPhieuNhapHang] VALUES
(N'CTPN-0041',N'PN-0041',N'LH-0041',N'SP2024-0041',N'KHO-0001',N'LOT-DEX-260501',200,CAST(840.00 AS DECIMAL(18,2)),CAST(168000.00 AS DECIMAL(18,2)),CAST(N'2028-06-30' AS DATETIME2)),
(N'CTPN-0042',N'PN-0042',N'LH-0042',N'SP2024-0042',N'KHO-0001',N'LOT-DPH-260502',150,CAST(1050.00 AS DECIMAL(18,2)),CAST(157500.00 AS DECIMAL(18,2)),CAST(N'2028-07-31' AS DATETIME2)),
(N'CTPN-0043',N'PN-0043',N'LH-0043',N'SP2024-0043',N'KHO-0002',N'LOT-ZNC-260503',120,CAST(1750.00 AS DECIMAL(18,2)),CAST(210000.00 AS DECIMAL(18,2)),CAST(N'2028-08-31' AS DATETIME2)),
(N'CTPN-0044',N'PN-0044',N'LH-0044',N'SP2024-0044',N'KHO-0002',N'LOT-FES-260504',100,CAST(1260.00 AS DECIMAL(18,2)),CAST(126000.00 AS DECIMAL(18,2)),CAST(N'2028-09-30' AS DATETIME2)),
(N'CTPN-0045',N'PN-0045',N'LH-0045',N'SP2024-0045',N'KHO-0002',N'LOT-BCP-260505',150,CAST(1400.00 AS DECIMAL(18,2)),CAST(210000.00 AS DECIMAL(18,2)),CAST(N'2028-10-31' AS DATETIME2)),
(N'CTPN-0046',N'PN-0046',N'LH-0046',N'SP2024-0046',N'KHO-0001',N'LOT-IBS-260506', 80,CAST(45500.00 AS DECIMAL(18,2)),CAST(3640000.00 AS DECIMAL(18,2)),CAST(N'2028-11-30' AS DATETIME2)),
(N'CTPN-0047',N'PN-0047',N'LH-0047',N'SP2024-0047',N'KHO-0002',N'LOT-ENT-260507',200,CAST(12600.00 AS DECIMAL(18,2)),CAST(2520000.00 AS DECIMAL(18,2)),CAST(N'2028-12-31' AS DATETIME2)),
(N'CTPN-0048',N'PN-0048',N'LH-0048',N'SP2024-0048',N'KHO-0001',N'LOT-SOR-260508',200,CAST(3500.00 AS DECIMAL(18,2)),CAST(700000.00 AS DECIMAL(18,2)),CAST(N'2029-01-31' AS DATETIME2)),
(N'CTPN-0049',N'PN-0049',N'LH-0049',N'SP2024-0049',N'KHO-0001',N'LOT-MET-260509',300,CAST(1050.00 AS DECIMAL(18,2)),CAST(315000.00 AS DECIMAL(18,2)),CAST(N'2029-02-28' AS DATETIME2)),
(N'CTPN-0050',N'PN-0050',N'LH-0050',N'SP2024-0050',N'KHO-0001',N'LOT-CLA-260510',150,CAST(3850.00 AS DECIMAL(18,2)),CAST(577500.00 AS DECIMAL(18,2)),CAST(N'2029-03-31' AS DATETIME2)),
(N'CTPN-0051',N'PN-0051',N'LH-0051',N'SP2024-0051',N'KHO-0001',N'LOT-FUR-260511',250,CAST(700.00 AS DECIMAL(18,2)),CAST(175000.00 AS DECIMAL(18,2)),CAST(N'2029-04-30' AS DATETIME2)),
(N'CTPN-0052',N'PN-0052',N'LH-0052',N'SP2024-0052',N'KHO-0001',N'LOT-PRE-260512',200,CAST(840.00 AS DECIMAL(18,2)),CAST(168000.00 AS DECIMAL(18,2)),CAST(N'2029-05-31' AS DATETIME2)),
(N'CTPN-0053',N'PN-0053',N'LH-0053',N'SP2024-0053',N'KHO-0001',N'LOT-SAL-260513',200,CAST(1400.00 AS DECIMAL(18,2)),CAST(280000.00 AS DECIMAL(18,2)),CAST(N'2029-06-30' AS DATETIME2)),
(N'CTPN-0054',N'PN-0054',N'LH-0054',N'SP2024-0054',N'KHO-0001',N'LOT-PAN-260514',120,CAST(3150.00 AS DECIMAL(18,2)),CAST(378000.00 AS DECIMAL(18,2)),CAST(N'2029-07-31' AS DATETIME2)),
(N'CTPN-0055',N'PN-0055',N'LH-0055',N'SP2024-0055',N'KHO-0001',N'LOT-NCL-260515',100,CAST(17500.00 AS DECIMAL(18,2)),CAST(1750000.00 AS DECIMAL(18,2)),CAST(N'2029-08-31' AS DATETIME2)),
(N'CTPN-0056',N'PN-0056',N'LH-0056',N'SP2024-0056',N'KHO-0001',N'LOT-GLI-260516',200,CAST(1050.00 AS DECIMAL(18,2)),CAST(210000.00 AS DECIMAL(18,2)),CAST(N'2029-09-30' AS DATETIME2)),
(N'CTPN-0057',N'PN-0057',N'LH-0057',N'SP2024-0057',N'KHO-0001',N'LOT-SIM-260517',180,CAST(1750.00 AS DECIMAL(18,2)),CAST(315000.00 AS DECIMAL(18,2)),CAST(N'2029-10-31' AS DATETIME2)),
(N'CTPN-0058',N'PN-0058',N'LH-0058',N'SP2024-0058',N'KHO-0002',N'LOT-VB1-260518',150,CAST(1050.00 AS DECIMAL(18,2)),CAST(157500.00 AS DECIMAL(18,2)),CAST(N'2029-11-30' AS DATETIME2)),
(N'CTPN-0059',N'PN-0059',N'LH-0059',N'SP2024-0059',N'KHO-0002',N'LOT-BIO-260519', 60,CAST(5600.00 AS DECIMAL(18,2)),CAST(336000.00 AS DECIMAL(18,2)),CAST(N'2029-12-31' AS DATETIME2)),
(N'CTPN-0060',N'PN-0060',N'LH-0060',N'SP2024-0060',N'KHO-0001',N'LOT-CPH-260520',300,CAST(560.00 AS DECIMAL(18,2)),CAST(168000.00 AS DECIMAL(18,2)),CAST(N'2030-01-31' AS DATETIME2));
GO

/* ============================================================
   6. MẪU LIỀU – 42 COMBO (14 nhóm bệnh × 3 lứa tuổi)
   ============================================================ */

-- CẢM CÚM
INSERT INTO dbo.MauLieu(comboId,mauLieuId,tenCombo,tenMauLieu,nhomBenh,giaBanCombo,giaBan,ghiChu,ngayTao) VALUES
(N'CB-2026-CF-01',N'CB-2026-CF-01',N'Cảm cúm – Trẻ em',N'Cảm cúm – Trẻ em',N'Ho – Đờm – Viêm họng',32200,32200,N'Trẻ < 12 tuổi; liều theo cân nặng; cần kê đơn trước khi dùng kháng sinh',GETDATE()),
(N'CB-2026-CF-02',N'CB-2026-CF-02',N'Cảm cúm – Người lớn',N'Cảm cúm – Người lớn',N'Ho – Đờm – Viêm họng',51000,51000,N'Người 18–59 tuổi; kháng sinh kèm giảm đau hạ sốt và kháng histamine',GETDATE()),
(N'CB-2026-CF-03',N'CB-2026-CF-03',N'Cảm cúm – Người già',N'Cảm cúm – Người già',N'Ho – Đờm – Viêm họng',33200,33200,N'Người ≥ 60 tuổi; giảm liều para; ưu tiên kháng histamine ít buồn ngủ',GETDATE());

-- HO
INSERT INTO dbo.MauLieu(comboId,mauLieuId,tenCombo,tenMauLieu,nhomBenh,giaBanCombo,giaBan,ghiChu,ngayTao) VALUES
(N'CB-2026-HO-01',N'CB-2026-HO-01',N'Ho – Trẻ em',N'Ho – Trẻ em',N'Ho – Đờm – Viêm họng',85600,85600,N'Siro ho thảo dược + hạ sốt; không dùng Dextromethorphan < 6 tuổi',GETDATE()),
(N'CB-2026-HO-02',N'CB-2026-HO-02',N'Ho – Người lớn',N'Ho – Người lớn',N'Ho – Đờm – Viêm họng',110200,110200,N'Siro ho + giảm đau + Strepsils ngậm làm dịu họng',GETDATE()),
(N'CB-2026-HO-03',N'CB-2026-HO-03',N'Ho – Người già',N'Ho – Người già',N'Ho – Đờm – Viêm họng',82600,82600,N'Giảm liều para; Strepsils ngậm tại chỗ; tránh kháng histamine gây buồn ngủ',GETDATE());

-- SỐT
INSERT INTO dbo.MauLieu(comboId,mauLieuId,tenCombo,tenMauLieu,nhomBenh,giaBanCombo,giaBan,ghiChu,ngayTao) VALUES
(N'CB-2026-ST-01',N'CB-2026-ST-01',N'Sốt – Trẻ em',N'Sốt – Trẻ em',N'Sốt – Cảm cúm – Sổ mũi',58800,58800,N'Hapacol liều trẻ em + bù nước điện giải sau sốt',GETDATE()),
(N'CB-2026-ST-02',N'CB-2026-ST-02',N'Sốt – Người lớn',N'Sốt – Người lớn',N'Sốt – Cảm cúm – Sổ mũi',40000,40000,N'Para + Efferalgan sủi nhanh + Oresol bù nước',GETDATE()),
(N'CB-2026-ST-03',N'CB-2026-ST-03',N'Sốt – Người già',N'Sốt – Người già',N'Sốt – Cảm cúm – Sổ mũi',21600,21600,N'Liều nhẹ; không dùng Ibuprofen; bổ sung nước liên tục',GETDATE());

-- TIÊU CHẢY
INSERT INTO dbo.MauLieu(comboId,mauLieuId,tenCombo,tenMauLieu,nhomBenh,giaBanCombo,giaBan,ghiChu,ngayTao) VALUES
(N'CB-2026-TC-01',N'CB-2026-TC-01',N'Tiêu chảy – Trẻ em',N'Tiêu chảy – Trẻ em',N'Dạ dày – Tiêu hóa – Đại tràng',126000,126000,N'Smecta + Oresol; không dùng Loperamide trẻ < 2 tuổi',GETDATE()),
(N'CB-2026-TC-02',N'CB-2026-TC-02',N'Tiêu chảy – Người lớn',N'Tiêu chảy – Người lớn',N'Dạ dày – Tiêu hóa – Đại tràng',181000,181000,N'Smecta + Oresol + Probiotics phục hồi hệ vi khuẩn',GETDATE()),
(N'CB-2026-TC-03',N'CB-2026-TC-03',N'Tiêu chảy – Người già',N'Tiêu chảy – Người già',N'Dạ dày – Tiêu hóa – Đại tràng',175500,175500,N'Theo dõi mất nước; Probiotics dài ngày; cẩn thận suy thận',GETDATE());

-- DỊ ỨNG
INSERT INTO dbo.MauLieu(comboId,mauLieuId,tenCombo,tenMauLieu,nhomBenh,giaBanCombo,giaBan,ghiChu,ngayTao) VALUES
(N'CB-2026-DU-01',N'CB-2026-DU-01',N'Dị ứng – Trẻ em',N'Dị ứng – Trẻ em',N'Dị ứng – Mẩn ngứa – Mề đay',38700,38700,N'Loratadine ít gây buồn ngủ + xịt mũi hỗ trợ',GETDATE()),
(N'CB-2026-DU-02',N'CB-2026-DU-02',N'Dị ứng – Người lớn',N'Dị ứng – Người lớn',N'Dị ứng – Mẩn ngứa – Mề đay',39200,39200,N'Cetirizine tối + xịt mũi khi ngạt',GETDATE()),
(N'CB-2026-DU-03',N'CB-2026-DU-03',N'Dị ứng – Người già',N'Dị ứng – Người già',N'Dị ứng – Mẩn ngứa – Mề đay',38700,38700,N'Loratadine an toàn hơn; chú ý tương tác thuốc tim mạch',GETDATE());

-- ĐAU DẠ DÀY
INSERT INTO dbo.MauLieu(comboId,mauLieuId,tenCombo,tenMauLieu,nhomBenh,giaBanCombo,giaBan,ghiChu,ngayTao) VALUES
(N'CB-2026-DD-01',N'CB-2026-DD-01',N'Đau dạ dày – Trẻ em',N'Đau dạ dày – Trẻ em',N'Dạ dày – Tiêu hóa – Đại tràng',119000,119000,N'Domperidon + Phosphalugel; hạn chế PPI cho trẻ nhỏ',GETDATE()),
(N'CB-2026-DD-02',N'CB-2026-DD-02',N'Đau dạ dày – Người lớn',N'Đau dạ dày – Người lớn',N'Dạ dày – Tiêu hóa – Đại tràng',151300,151300,N'PPI + chống nôn + kháng acid; liệu trình 14 ngày',GETDATE()),
(N'CB-2026-DD-03',N'CB-2026-DD-03',N'Đau dạ dày – Người già',N'Đau dạ dày – Người già',N'Dạ dày – Tiêu hóa – Đại tràng',178300,178300,N'Pantoprazole mạnh hơn + Phosphalugel; kiểm tra H.pylori',GETDATE());

-- HUYẾT ÁP
INSERT INTO dbo.MauLieu(comboId,mauLieuId,tenCombo,tenMauLieu,nhomBenh,giaBanCombo,giaBan,ghiChu,ngayTao) VALUES
(N'CB-2026-HA-01',N'CB-2026-HA-01',N'Huyết áp – Người lớn',N'Huyết áp – Người lớn',N'Tim mạch – Huyết áp',120000,120000,N'Losartan + Amlodipine; đo HA hàng ngày; không ngừng đột ngột',GETDATE()),
(N'CB-2026-HA-02',N'CB-2026-HA-02',N'Huyết áp – Người già',N'Huyết áp – Người già',N'Tim mạch – Huyết áp',225000,225000,N'Thêm Atorvastatin hạ mỡ máu; kê đơn bắt buộc; theo dõi điện giải',GETDATE()),
(N'CB-2026-HA-03',N'CB-2026-HA-03',N'Huyết áp kết hợp mỡ máu',N'Huyết áp kết hợp mỡ máu',N'Tim mạch – Huyết áp',150000,150000,N'Amlodipine + Simvastatin; uống tối trước ngủ để tăng hiệu quả',GETDATE());

-- ĐÁI THÁO ĐƯỜNG
INSERT INTO dbo.MauLieu(comboId,mauLieuId,tenCombo,tenMauLieu,nhomBenh,giaBanCombo,giaBan,ghiChu,ngayTao) VALUES
(N'CB-2026-DT-01',N'CB-2026-DT-01',N'Đái tháo đường – Người lớn',N'Đái tháo đường – Người lớn',N'Đái tháo đường',48000,48000,N'Metformin sau ăn; theo dõi đường huyết 2 lần/ngày',GETDATE()),
(N'CB-2026-DT-02',N'CB-2026-DT-02',N'Đái tháo đường – Người già',N'Đái tháo đường – Người già',N'Đái tháo đường',69000,69000,N'Metformin liều thấp + Losartan bảo vệ thận; kiểm GFR định kỳ',GETDATE()),
(N'CB-2026-DT-03',N'CB-2026-DT-03',N'ĐTĐ kết hợp biến chứng tim',N'ĐTĐ kết hợp biến chứng tim',N'Đái tháo đường',123000,123000,N'Kiểm soát đường huyết + huyết áp đồng thời; theo dõi tim mạch',GETDATE());

-- KHÁNG SINH HÔ HẤP
INSERT INTO dbo.MauLieu(comboId,mauLieuId,tenCombo,tenMauLieu,nhomBenh,giaBanCombo,giaBan,ghiChu,ngayTao) VALUES
(N'CB-2026-KS-01',N'CB-2026-KS-01',N'Viêm họng – Trẻ em',N'Viêm họng – Trẻ em',N'Ho – Đờm – Viêm họng',141000,141000,N'Augmentin siro 7 ngày; liều theo cân nặng; kê đơn bắt buộc',GETDATE()),
(N'CB-2026-KS-02',N'CB-2026-KS-02',N'Viêm họng – Người lớn',N'Viêm họng – Người lớn',N'Ho – Đờm – Viêm họng',128000,128000,N'Amoxicillin 7 ngày + hạ sốt + Strepsils ngậm',GETDATE()),
(N'CB-2026-KS-03',N'CB-2026-KS-03',N'Viêm họng – Người già',N'Viêm họng – Người già',N'Ho – Đờm – Viêm họng',60000,60000,N'Cefuroxime phổ rộng hơn Amoxicillin; uống sau ăn',GETDATE());

-- ĐAU NHỨC CƠ XƯƠNG
INSERT INTO dbo.MauLieu(comboId,mauLieuId,tenCombo,tenMauLieu,nhomBenh,giaBanCombo,giaBan,ghiChu,ngayTao) VALUES
(N'CB-2026-CN-01',N'CB-2026-CN-01',N'Đau nhức cơ xương – Trẻ em',N'Đau nhức cơ xương – Trẻ em',N'Xương khớp – Gút',200600,200600,N'Hạ sốt giảm đau + Canxi + D3 phát triển xương',GETDATE()),
(N'CB-2026-CN-02',N'CB-2026-CN-02',N'Đau nhức cơ xương – Người lớn',N'Đau nhức cơ xương – Người lớn',N'Xương khớp – Gút',204000,204000,N'Ibuprofen ngắn ngày + Magie B6 lâu dài giảm co cơ',GETDATE()),
(N'CB-2026-CN-03',N'CB-2026-CN-03',N'Đau nhức cơ xương – Người già',N'Đau nhức cơ xương – Người già',N'Xương khớp – Gút',447600,447600,N'Ibuprofen thận trọng + Canxi + D3; phòng loãng xương',GETDATE());

-- MẤT NGỦ / CĂNG THẲNG
INSERT INTO dbo.MauLieu(comboId,mauLieuId,tenCombo,tenMauLieu,nhomBenh,giaBanCombo,giaBan,ghiChu,ngayTao) VALUES
(N'CB-2026-MN-01',N'CB-2026-MN-01',N'Mất ngủ – Người lớn',N'Mất ngủ – Người lớn',N'Đau đầu – Giảm đau – Hạ sốt',420000,420000,N'Melatonin 30 ngày; trước ngủ 30 phút; phòng tối; tắt màn hình',GETDATE()),
(N'CB-2026-MN-02',N'CB-2026-MN-02',N'Mất ngủ – Người già',N'Mất ngủ – Người già',N'Đau đầu – Giảm đau – Hạ sốt',615000,615000,N'Melatonin + Magie B6 giảm căng thẳng; liều thấp hơn người trẻ',GETDATE()),
(N'CB-2026-MN-03',N'CB-2026-MN-03',N'Căng thẳng – Người lớn/già',N'Căng thẳng – Người lớn/già',N'Đau đầu – Giảm đau – Hạ sốt',390000,390000,N'Magie B6 sáng chiều; an thần tự nhiên; không gây nghiện',GETDATE());

-- VITAMIN BỔ SUNG
INSERT INTO dbo.MauLieu(comboId,mauLieuId,tenCombo,tenMauLieu,nhomBenh,giaBanCombo,giaBan,ghiChu,ngayTao) VALUES
(N'CB-2026-VT-01',N'CB-2026-VT-01',N'Vitamin bổ sung – Trẻ em',N'Vitamin bổ sung – Trẻ em',N'Thuốc bổ – Vitamin – Khoáng chất',390000,390000,N'Canxi + D3 hỗ trợ phát triển chiều cao và xương',GETDATE()),
(N'CB-2026-VT-02',N'CB-2026-VT-02',N'Vitamin bổ sung – Người lớn',N'Vitamin bổ sung – Người lớn',N'Thuốc bổ – Vitamin – Khoáng chất',565000,565000,N'Vitamin C + D3 + Omega-3; sức khỏe tổng thể',GETDATE()),
(N'CB-2026-VT-03',N'CB-2026-VT-03',N'Vitamin bổ sung – Người già',N'Vitamin bổ sung – Người già',N'Thuốc bổ – Vitamin – Khoáng chất',670000,670000,N'Canxi + D3 + Omega-3; phòng loãng xương và tim mạch',GETDATE());

-- TÁO BÓN
INSERT INTO dbo.MauLieu(comboId,mauLieuId,tenCombo,tenMauLieu,nhomBenh,giaBanCombo,giaBan,ghiChu,ngayTao) VALUES
(N'CB-2026-TB-01',N'CB-2026-TB-01',N'Táo bón – Trẻ em',N'Táo bón – Trẻ em',N'Dạ dày – Tiêu hóa – Đại tràng',86000,86000,N'Probiotics cải thiện hệ vi sinh + bổ sung nước; không Sorbitol < 3 tuổi',GETDATE()),
(N'CB-2026-TB-02',N'CB-2026-TB-02',N'Táo bón – Người lớn',N'Táo bón – Người lớn',N'Dạ dày – Tiêu hóa – Đại tràng',154000,154000,N'Probiotics dài hạn + Sorbitol cấp tốc khi cần',GETDATE()),
(N'CB-2026-TB-03',N'CB-2026-TB-03',N'Táo bón – Người già',N'Táo bón – Người già',N'Dạ dày – Tiêu hóa – Đại tràng',193000,193000,N'Probiotics dài hạn + Domperidon tăng nhu động ruột',GETDATE());

-- MẮT MŨI HỌNG
INSERT INTO dbo.MauLieu(comboId,mauLieuId,tenCombo,tenMauLieu,nhomBenh,giaBanCombo,giaBan,ghiChu,ngayTao) VALUES
(N'CB-2026-MT-01',N'CB-2026-MT-01',N'Mắt mũi họng – Trẻ em',N'Mắt mũi họng – Trẻ em',N'Mắt – Tai – Mũi',57000,57000,N'Osla rửa mắt + xịt mũi; an toàn cho trẻ',GETDATE()),
(N'CB-2026-MT-02',N'CB-2026-MT-02',N'Mắt mũi họng – Người lớn',N'Mắt mũi họng – Người lớn',N'Mắt – Tai – Mũi',167000,167000,N'V.Rohto mắt + xịt mũi + Strepsils họng; bộ 3 triệu chứng',GETDATE()),
(N'CB-2026-MT-03',N'CB-2026-MT-03',N'Mắt mũi họng – Người già',N'Mắt mũi họng – Người già',N'Mắt – Tai – Mũi',134000,134000,N'Osla nhẹ nhàng + Strepsils; tránh thuốc co mạch nếu có tăng HA',GETDATE());
GO

/* ============================================================
   7. CHI TIẾT MẪU LIỀU
   id = IDENTITY – không cần khai báo
   tongSoLuong = (sang+trua+chieu+toi) × soNgay
   ============================================================ */

-- CB-2026-CF-01 Cảm cúm – Trẻ em
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-CF-01',N'CB-2026-CF-01',N'SP2024-0039',N'Gói', 1,1,1,0,N'Pha 100ml nước ấm; liều trẻ em theo cân nặng; cách 4–6h/lần',3,9,3400),
(N'CB-2026-CF-01',N'CB-2026-CF-01',N'SP2024-0019',N'Viên',1,0,0,0,N'1 viên buổi sáng; Loratadine ít buồn ngủ phù hợp trẻ đi học',3,3,533);

-- CB-2026-CF-02 Cảm cúm – Người lớn
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-CF-02',N'CB-2026-CF-02',N'SP2024-0009',N'Viên',2,2,2,0,N'2 viên sau ăn mỗi 6h khi sốt/đau; không quá 8 viên/ngày',3,18,400),
(N'CB-2026-CF-02',N'CB-2026-CF-02',N'SP2024-0012',N'Viên',0,0,0,1,N'1 viên Cetirizine buổi tối; giảm chảy nước mũi dị ứng',3,3,600),
(N'CB-2026-CF-02',N'CB-2026-CF-02',N'SP2024-0001',N'Viên',1,1,1,0,N'Amoxicillin 500mg 3 lần/ngày; uống đủ 7 ngày không bỏ giữa chừng',7,21,2000);

-- CB-2026-CF-03 Cảm cúm – Người già
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-CF-03',N'CB-2026-CF-03',N'SP2024-0009',N'Viên',1,1,1,0,N'1 viên sau ăn; giảm liều nếu có bệnh gan/thận; không quá 3g/ngày',3,9,400),
(N'CB-2026-CF-03',N'CB-2026-CF-03',N'SP2024-0019',N'Viên',0,0,0,1,N'Loratadine tối; an toàn hơn Diphenhydramine với người cao tuổi',3,3,533),
(N'CB-2026-CF-03',N'CB-2026-CF-03',N'SP2024-0001',N'Viên',1,1,0,0,N'2 lần/ngày; uống đủ 7 ngày; theo dõi tương tác thuốc khác',7,14,2000);

-- CB-2026-HO-01 Ho – Trẻ em
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-HO-01',N'CB-2026-HO-01',N'SP2024-0017',N'Chai',1,0,0,0,N'1 chai Siro Ho Bổ Phế; đo thìa đúng liều theo nhãn; dùng 5–7 ngày',1,1,55000),
(N'CB-2026-HO-01',N'CB-2026-HO-01',N'SP2024-0039',N'Gói', 1,1,1,0,N'Hạ sốt khi trẻ sốt trên 38.5°C; pha 1 gói với 100ml nước ấm',3,9,3400);

-- CB-2026-HO-02 Ho – Người lớn
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-HO-02',N'CB-2026-HO-02',N'SP2024-0017',N'Chai',1,0,0,0,N'1 chai siro ho; dùng 7–10 ngày theo hướng dẫn',1,1,55000),
(N'CB-2026-HO-02',N'CB-2026-HO-02',N'SP2024-0009',N'Viên',2,2,2,0,N'2 viên sau ăn khi sốt hoặc đau họng',3,18,400),
(N'CB-2026-HO-02',N'CB-2026-HO-02',N'SP2024-0037',N'Viên',1,0,1,0,N'Ngậm Strepsils tan chậm; không nhai; sáng tối',3,6,8000);

-- CB-2026-HO-03 Ho – Người già
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-HO-03',N'CB-2026-HO-03',N'SP2024-0017',N'Chai',1,0,0,0,N'1 chai; uống liều người lớn; theo dõi đờm nếu nhiều',1,1,55000),
(N'CB-2026-HO-03',N'CB-2026-HO-03',N'SP2024-0009',N'Viên',1,1,1,0,N'1 viên sau ăn; giảm liều nếu có bệnh gan thận',3,9,400),
(N'CB-2026-HO-03',N'CB-2026-HO-03',N'SP2024-0037',N'Viên',1,0,0,0,N'1 viên ngậm buổi sáng; làm dịu họng rát',3,3,8000);

-- CB-2026-ST-01 Sốt – Trẻ em
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-ST-01',N'CB-2026-ST-01',N'SP2024-0039',N'Gói', 1,1,1,1,N'Hapacol 150mg/gói cho trẻ; cách tối thiểu 4–6h; không quá 5 lần/ngày',3,12,3400),
(N'CB-2026-ST-01',N'CB-2026-ST-01',N'SP2024-0013',N'Gói', 1,0,1,0,N'Bù nước điện giải sau sốt; pha đúng tỷ lệ theo nhãn',3,6,3000);

-- CB-2026-ST-02 Sốt – Người lớn
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-ST-02',N'CB-2026-ST-02',N'SP2024-0009',N'Viên',2,2,2,2,N'2 viên mỗi 4–6h khi sốt; không quá 8 viên/ngày; sau ăn',3,24,400),
(N'CB-2026-ST-02',N'CB-2026-ST-02',N'SP2024-0038',N'Viên',1,0,1,0,N'Efferalgan sủi trong 200ml nước; hạ sốt nhanh hơn viên thường',2,4,3100),
(N'CB-2026-ST-02',N'CB-2026-ST-02',N'SP2024-0013',N'Gói', 1,1,0,0,N'Bù điện giải sau mồ hôi khi sốt cao',3,6,3000);

-- CB-2026-ST-03 Sốt – Người già
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-ST-03',N'CB-2026-ST-03',N'SP2024-0009',N'Viên',1,1,1,0,N'1 viên sau ăn; không tự tăng liều; không dùng Ibuprofen',3,9,400),
(N'CB-2026-ST-03',N'CB-2026-ST-03',N'SP2024-0013',N'Gói', 1,0,1,0,N'Bù nước thường xuyên; uống từng ngụm nhỏ liên tục',3,6,3000);

-- CB-2026-TC-01 Tiêu chảy – Trẻ em
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-TC-01',N'CB-2026-TC-01',N'SP2024-0018',N'Gói', 1,1,1,0,N'Pha trong 50ml nước; uống sau mỗi lần đi ngoài; 3 gói/ngày',3,9,11000),
(N'CB-2026-TC-01',N'CB-2026-TC-01',N'SP2024-0013',N'Gói', 1,1,1,0,N'Bù nước điện giải; pha đúng tỷ lệ; không thêm đường',3,9,3000);

-- CB-2026-TC-02 Tiêu chảy – Người lớn
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-TC-02',N'CB-2026-TC-02',N'SP2024-0018',N'Gói', 1,1,1,0,N'3 gói Smecta/ngày sau ăn; dùng 3–5 ngày',3,9,11000),
(N'CB-2026-TC-02',N'CB-2026-TC-02',N'SP2024-0013',N'Gói', 1,1,1,0,N'Bù nước sau mỗi lần đi ngoài; tối đa 3 gói/ngày',3,9,3000),
(N'CB-2026-TC-02',N'CB-2026-TC-02',N'SP2024-0027',N'Viên',1,0,1,0,N'Probiotics xa kháng sinh ≥ 2h; sáng chiều trước ăn',5,10,5500);

-- CB-2026-TC-03 Tiêu chảy – Người già
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-TC-03',N'CB-2026-TC-03',N'SP2024-0018',N'Gói', 1,1,0,0,N'2 gói/ngày; dùng 5 ngày; theo dõi chức năng thận',5,10,11000),
(N'CB-2026-TC-03',N'CB-2026-TC-03',N'SP2024-0013',N'Gói', 1,1,1,0,N'Bù nước liên tục; người già dễ mất nước hơn',3,9,3000),
(N'CB-2026-TC-03',N'CB-2026-TC-03',N'SP2024-0027',N'Viên',1,0,0,0,N'1 viên/ngày; phục hồi hệ vi sinh dài hạn',7,7,5500);

-- CB-2026-DU-01 Dị ứng – Trẻ em
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-DU-01',N'CB-2026-DU-01',N'SP2024-0019',N'Viên',1,0,0,0,N'1 viên buổi sáng; Loratadine ít gây buồn ngủ; phù hợp ngày học',7,7,533),
(N'CB-2026-DU-01',N'CB-2026-DU-01',N'SP2024-0014',N'Chai',1,0,0,0,N'Xịt 1–2 lần mỗi bên mũi; dùng 7–14 ngày',1,1,35000);

-- CB-2026-DU-02 Dị ứng – Người lớn
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-DU-02',N'CB-2026-DU-02',N'SP2024-0012',N'Viên',0,0,0,1,N'1 viên Cetirizine buổi tối; tác dụng kéo dài 24h',7,7,600),
(N'CB-2026-DU-02',N'CB-2026-DU-02',N'SP2024-0014',N'Chai',1,0,0,0,N'Xịt mũi 1–2 lần/bên; không dùng quá 5 ngày liên tiếp',1,1,35000);

-- CB-2026-DU-03 Dị ứng – Người già
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-DU-03',N'CB-2026-DU-03',N'SP2024-0019',N'Viên',0,0,0,1,N'Loratadine tối; ít tương tác với thuốc tim mạch hơn Cetirizine',7,7,533),
(N'CB-2026-DU-03',N'CB-2026-DU-03',N'SP2024-0014',N'Chai',1,0,0,0,N'Xịt nhẹ; tránh Naphazoline nếu có tăng huyết áp',1,1,35000);

-- CB-2026-DD-01 Đau dạ dày – Trẻ em
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-DD-01',N'CB-2026-DD-01',N'SP2024-0015',N'Viên',1,1,1,0,N'Domperidon uống 30 phút trước ăn; giảm nôn nao',5,15,1333),
(N'CB-2026-DD-01',N'CB-2026-DD-01',N'SP2024-0040',N'Gói', 1,1,1,0,N'Phosphalugel sau ăn 1–2h; kháng acid tại chỗ',3,9,11000);

-- CB-2026-DD-02 Đau dạ dày – Người lớn
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-DD-02',N'CB-2026-DD-02',N'SP2024-0016',N'Viên',1,0,0,0,N'PPI Omeprazole trước bữa sáng 30 phút; liệu trình 14 ngày',14,14,1733),
(N'CB-2026-DD-02',N'CB-2026-DD-02',N'SP2024-0015',N'Viên',1,1,1,0,N'Domperidon chống nôn 30 phút trước 3 bữa ăn',7,21,1333),
(N'CB-2026-DD-02',N'CB-2026-DD-02',N'SP2024-0040',N'Gói', 1,1,1,0,N'Kháng acid sau ăn 1–2h; không pha thuốc khác vào',3,9,11000);

-- CB-2026-DD-03 Đau dạ dày – Người già
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-DD-03',N'CB-2026-DD-03',N'SP2024-0054',N'Viên',1,0,0,0,N'Pantoprazole 40mg mạnh hơn; trước bữa sáng 30 phút; 14 ngày',14,14,4500),
(N'CB-2026-DD-03',N'CB-2026-DD-03',N'SP2024-0040',N'Gói', 1,0,1,0,N'Kháng acid sáng tối; uống sau ăn 1h',7,14,11000);

-- CB-2026-HA-01 Huyết áp – Người lớn
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-HA-01',N'CB-2026-HA-01',N'SP2024-0004',N'Viên',1,0,0,0,N'Losartan 50mg sáng; không ngừng đột ngột; đo HA hàng ngày',30,30,1500),
(N'CB-2026-HA-01',N'CB-2026-HA-01',N'SP2024-0007',N'Viên',1,0,0,0,N'Amlodipine 5mg sáng; có thể phù chân nhẹ tuần đầu',30,30,2500);

-- CB-2026-HA-02 Huyết áp – Người già
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-HA-02',N'CB-2026-HA-02',N'SP2024-0007',N'Viên',1,0,0,0,N'Amlodipine sáng; nhẹ nhàng không hạ HA đột ngột',30,30,2500),
(N'CB-2026-HA-02',N'CB-2026-HA-02',N'SP2024-0004',N'Viên',0,0,0,1,N'Losartan tối; bảo vệ thận; không dùng NSAIDs đồng thời',30,30,1500),
(N'CB-2026-HA-02',N'CB-2026-HA-02',N'SP2024-0008',N'Viên',0,0,0,1,N'Atorvastatin tối trước ngủ; không ăn bưởi',30,30,3500);

-- CB-2026-HA-03 Huyết áp kết hợp mỡ máu
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-HA-03',N'CB-2026-HA-03',N'SP2024-0007',N'Viên',1,0,0,0,N'Amlodipine sáng; giảm sức cản ngoại vi hiệu quả',30,30,2500),
(N'CB-2026-HA-03',N'CB-2026-HA-03',N'SP2024-0057',N'Viên',0,0,0,1,N'Simvastatin tối trước ngủ; kiểm men gan 3 tháng/lần',30,30,2500);

-- CB-2026-DT-01 ĐTĐ – Người lớn
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-DT-01',N'CB-2026-DT-01',N'SP2024-0003',N'Viên',1,1,0,0,N'Metformin sau ăn sáng và trưa; giảm tác dụng phụ dạ dày',30,60,800);

-- CB-2026-DT-02 ĐTĐ – Người già
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-DT-02',N'CB-2026-DT-02',N'SP2024-0003',N'Viên',1,0,0,0,N'Metformin liều thấp; chú ý GFR; ngừng khi GFR < 30',30,30,800),
(N'CB-2026-DT-02',N'CB-2026-DT-02',N'SP2024-0004',N'Viên',1,0,0,0,N'Losartan bảo vệ thận và tim ở người ĐTĐ có tăng HA',30,30,1500);

-- CB-2026-DT-03 ĐTĐ biến chứng tim
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-DT-03',N'CB-2026-DT-03',N'SP2024-0003',N'Viên',1,1,0,0,N'Metformin kiểm soát đường huyết; uống sau ăn',30,60,800),
(N'CB-2026-DT-03',N'CB-2026-DT-03',N'SP2024-0007',N'Viên',1,0,0,0,N'Amlodipine kiểm soát HA; phòng biến chứng tim mạch',30,30,2500);

-- CB-2026-KS-01 Viêm họng – Trẻ em
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-KS-01',N'CB-2026-KS-01',N'SP2024-0005',N'Chai',1,0,0,0,N'Augmentin siro 7 ngày; liều theo cân nặng kg/ml; kê đơn',1,1,90000),
(N'CB-2026-KS-01',N'CB-2026-KS-01',N'SP2024-0039',N'Gói', 1,1,1,0,N'Hạ sốt khi trẻ sốt trên 38.5°C; cách tối thiểu 4h',5,15,3400);

-- CB-2026-KS-02 Viêm họng – Người lớn
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-KS-02',N'CB-2026-KS-02',N'SP2024-0001',N'Viên',1,1,1,0,N'Amoxicillin 500mg 3 lần/ngày; uống đủ 7 ngày không bỏ',7,21,2000),
(N'CB-2026-KS-02',N'CB-2026-KS-02',N'SP2024-0009',N'Viên',1,1,1,0,N'Giảm đau hạ sốt sau ăn; hỗ trợ triệu chứng',5,15,400),
(N'CB-2026-KS-02',N'CB-2026-KS-02',N'SP2024-0037',N'Viên',1,0,1,0,N'Ngậm Strepsils làm dịu họng; sáng tối',5,10,8000);

-- CB-2026-KS-03 Viêm họng – Người già
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-KS-03',N'CB-2026-KS-03',N'SP2024-0002',N'Viên',1,0,1,0,N'Cefuroxime 500mg sau ăn; phổ rộng hơn Amoxicillin; 7 ngày',7,14,5000),
(N'CB-2026-KS-03',N'CB-2026-KS-03',N'SP2024-0009',N'Viên',1,0,1,0,N'1 viên cách 6h khi cần; không tự tăng liều',5,10,400);

-- CB-2026-CN-01 Đau nhức cơ xương – Trẻ em
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-CN-01',N'CB-2026-CN-01',N'SP2024-0039',N'Gói', 1,1,1,0,N'Hạ sốt giảm đau liều trẻ em; theo cân nặng',3,9,3400),
(N'CB-2026-CN-01',N'CB-2026-CN-01',N'SP2024-0023',N'Viên',1,0,1,0,N'Canxi sáng chiều sau ăn; tránh dùng cùng sắt',30,60,2833);

-- CB-2026-CN-02 Đau nhức cơ xương – Người lớn
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-CN-02',N'CB-2026-CN-02',N'SP2024-0010',N'Viên',1,1,1,0,N'Ibuprofen 400mg sau ăn; không quá 5 ngày liên tiếp',5,15,600),
(N'CB-2026-CN-02',N'CB-2026-CN-02',N'SP2024-0026',N'Viên',1,0,0,0,N'Magie B6 sáng; giảm co cơ và căng thẳng dài hạn',30,30,6500);

-- CB-2026-CN-03 Đau nhức cơ xương – Người già
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-CN-03',N'CB-2026-CN-03',N'SP2024-0010',N'Viên',1,1,0,0,N'Ibuprofen thận trọng với bệnh thận/dạ dày; sau ăn',5,10,600),
(N'CB-2026-CN-03',N'CB-2026-CN-03',N'SP2024-0023',N'Viên',1,0,1,0,N'Canxi sáng chiều; không dùng ban đêm tránh sỏi thận',30,60,2833),
(N'CB-2026-CN-03',N'CB-2026-CN-03',N'SP2024-0021',N'Viên',1,0,0,0,N'Vitamin D3 sáng khi no; tăng hấp thu Canxi tối đa',30,30,7333);

-- CB-2026-MN-01 Mất ngủ – Người lớn
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-MN-01',N'CB-2026-MN-01',N'SP2024-0025',N'Viên',0,0,0,1,N'Melatonin 5mg trước ngủ 30 phút; phòng tối; tắt màn hình',30,30,14000);

-- CB-2026-MN-02 Mất ngủ – Người già
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-MN-02',N'CB-2026-MN-02',N'SP2024-0025',N'Viên',0,0,0,1,N'Melatonin 2.5–5mg trước ngủ; liều thấp hơn người trẻ',30,30,14000),
(N'CB-2026-MN-02',N'CB-2026-MN-02',N'SP2024-0026',N'Viên',1,0,0,0,N'Magie B6 buổi sáng; giảm căng thẳng thần kinh ban ngày',30,30,6500);

-- CB-2026-MN-03 Căng thẳng – Người lớn/già
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-MN-03',N'CB-2026-MN-03',N'SP2024-0026',N'Viên',1,0,1,0,N'Magie B6 sáng chiều; sau ăn; an thần tự nhiên; không gây nghiện',30,60,6500);

-- CB-2026-VT-01 Vitamin – Trẻ em
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-VT-01',N'CB-2026-VT-01',N'SP2024-0023',N'Viên',1,0,1,0,N'Canxi sáng chiều; sau ăn; hỗ trợ chiều cao',30,60,2833),
(N'CB-2026-VT-01',N'CB-2026-VT-01',N'SP2024-0021',N'Viên',1,0,0,0,N'Vitamin D3 sáng khi no; tăng hấp thu canxi vào xương',30,30,7333);

-- CB-2026-VT-02 Vitamin – Người lớn
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-VT-02',N'CB-2026-VT-02',N'SP2024-0011',N'Viên',1,0,0,0,N'Vitamin C 1000mg sáng sau ăn; tránh kích ứng dạ dày',30,30,2167),
(N'CB-2026-VT-02',N'CB-2026-VT-02',N'SP2024-0021',N'Viên',1,0,0,0,N'Vitamin D3 sáng khi no',30,30,7333),
(N'CB-2026-VT-02',N'CB-2026-VT-02',N'SP2024-0022',N'Viên',1,0,0,0,N'Omega-3 sau bữa ăn có dầu; hỗ trợ tim mạch não bộ',30,30,9333);

-- CB-2026-VT-03 Vitamin – Người già
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-VT-03',N'CB-2026-VT-03',N'SP2024-0023',N'Viên',1,0,1,0,N'Canxi sáng chiều; không dùng đêm tránh sỏi thận',30,60,2833),
(N'CB-2026-VT-03',N'CB-2026-VT-03',N'SP2024-0021',N'Viên',1,0,0,0,N'Vitamin D3; đặc biệt quan trọng người ít ra nắng',30,30,7333),
(N'CB-2026-VT-03',N'CB-2026-VT-03',N'SP2024-0022',N'Viên',1,0,0,0,N'Omega-3 giảm viêm bảo vệ tim mạch cho người cao tuổi',30,30,9333);

-- CB-2026-TB-01 Táo bón – Trẻ em
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-TB-01',N'CB-2026-TB-01',N'SP2024-0027',N'Viên',1,0,0,0,N'Probiotics sáng trước ăn; uống nhiều nước; cải thiện dần',14,14,5500),
(N'CB-2026-TB-01',N'CB-2026-TB-01',N'SP2024-0013',N'Gói', 0,0,1,0,N'1 gói Oresol chiều; tăng lượng nước bổ sung',3,3,3000);

-- CB-2026-TB-02 Táo bón – Người lớn
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-TB-02',N'CB-2026-TB-02',N'SP2024-0027',N'Viên',1,0,1,0,N'Probiotics 2 lần/ngày trước ăn; duy trì 14 ngày',14,28,5500),
(N'CB-2026-TB-02',N'CB-2026-TB-02',N'SP2024-0048',N'Gói', 0,0,0,1,N'Sorbitol 1 gói tối khi táo bón; không dùng quá 3 ngày liên tiếp',3,3,5000);

-- CB-2026-TB-03 Táo bón – Người già
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-TB-03',N'CB-2026-TB-03',N'SP2024-0027',N'Viên',1,0,0,0,N'Probiotics dài hạn; cải thiện nhu động ruột chậm',30,30,5500),
(N'CB-2026-TB-03',N'CB-2026-TB-03',N'SP2024-0015',N'Viên',1,1,1,0,N'Domperidon tăng nhu động dạ dày-ruột; uống 30 phút trước ăn',7,21,1333),
(N'CB-2026-TB-03',N'CB-2026-TB-03',N'SP2024-0048',N'Gói', 0,0,0,1,N'Sorbitol 1 gói tối khi cần; theo dõi điện giải',3,3,5000);

-- CB-2026-MT-01 Mắt mũi họng – Trẻ em
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-MT-01',N'CB-2026-MT-01',N'SP2024-0034',N'Chai',1,0,0,0,N'Nhỏ mắt Osla 1–2 giọt; rửa sạch mắt mỗi sáng',1,1,22000),
(N'CB-2026-MT-01',N'CB-2026-MT-01',N'SP2024-0014',N'Chai',1,0,0,0,N'Xịt mũi 1 lần mỗi bên khi ngạt; dùng 5–7 ngày',1,1,35000);

-- CB-2026-MT-02 Mắt mũi họng – Người lớn
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-MT-02',N'CB-2026-MT-02',N'SP2024-0033',N'Chai',1,0,0,0,N'V.Rohto 1–2 giọt; giảm mỏi mắt do màn hình; 2–3 lần/ngày',1,1,52000),
(N'CB-2026-MT-02',N'CB-2026-MT-02',N'SP2024-0014',N'Chai',1,0,0,0,N'Xịt mũi khi ngạt; không quá 3 lần/ngày',1,1,35000),
(N'CB-2026-MT-02',N'CB-2026-MT-02',N'SP2024-0037',N'Viên',1,0,1,0,N'Strepsils ngậm giảm đau rát họng; sáng tối',5,10,8000);

-- CB-2026-MT-03 Mắt mũi họng – Người già
INSERT INTO dbo.ChiTietMauLieu(comboId,mauLieuId,sanPhamId,dvt,sang,trua,chieu,toi,cachDung,soNgay,tongSoLuong,giaDonVi) VALUES
(N'CB-2026-MT-03',N'CB-2026-MT-03',N'SP2024-0034',N'Chai',1,0,0,0,N'Osla nhẹ nhàng; tránh thuốc co mạch nếu có tăng HA',1,1,22000),
(N'CB-2026-MT-03',N'CB-2026-MT-03',N'SP2024-0037',N'Viên',1,0,1,0,N'Strepsils sáng tối; ngậm tan chậm không nhai; làm dịu họng',7,14,8000);
GO

/* ============================================================
   8. CẬP NHẬT viTriId, đồng bộ giaBan
   ============================================================ */
UPDATE dbo.SanPham SET viTriId=N'VT-0009' WHERE id IN (N'SP2024-0049',N'SP2024-0050',N'SP2024-0054');
UPDATE dbo.SanPham SET viTriId=N'VT-0010' WHERE id IN (N'SP2024-0051',N'SP2024-0052',N'SP2024-0053',N'SP2024-0056',N'SP2024-0057');
UPDATE dbo.SanPham SET viTriId=N'VT-0011' WHERE id IN (N'SP2024-0041',N'SP2024-0042',N'SP2024-0046',N'SP2024-0060');
UPDATE dbo.SanPham SET viTriId=N'VT-0012' WHERE id = N'SP2024-0048';
UPDATE dbo.SanPham SET viTriId=N'VT-0013' WHERE id IN (N'SP2024-0043',N'SP2024-0044',N'SP2024-0045',N'SP2024-0058',N'SP2024-0059');
UPDATE dbo.SanPham SET viTriId=N'VT-0014' WHERE id = N'SP2024-0047';
UPDATE dbo.SanPham SET viTriId=N'VT-0015' WHERE id = N'SP2024-0055';
GO

-- Đồng bộ giaBan từ DonViDoLuong theo đơn vị cơ bản
UPDATE sp
SET sp.giaBan = dvl.gia
FROM dbo.SanPham sp
JOIN dbo.DonViDoLuong dvl ON dvl.sanPhamId = sp.id AND dvl.ten = sp.donViDoCoBan
WHERE sp.id LIKE N'SP2024-00[456]%' AND sp.giaBan <> dvl.gia;
GO

/* ============================================================
   9. KIỂM TRA NHANH
   ============================================================ */
PRINT N'=== SẢN PHẨM MỚI (SP2024-0041 → 0060) ===';
SELECT id, ten, danhMuc, giaBan, nhomBenhLy, viTriId
FROM dbo.SanPham WHERE id LIKE N'SP2024-004%' OR id LIKE N'SP2024-005%' OR id LIKE N'SP2024-006%'
ORDER BY id;

PRINT N'=== VỊ TRÍ KỆ MỚI ===';
SELECT id, maViTri, tenViTri, khuVuc FROM dbo.ViTriThuoc WHERE id LIKE N'VT-001%' ORDER BY id;

PRINT N'=== TỔNG HỢP COMBO THEO NHÓM BỆNH ===';
SELECT nhomBenh, COUNT(*) AS soCombo FROM dbo.MauLieu WHERE comboId LIKE N'CB-2026-%' GROUP BY nhomBenh ORDER BY nhomBenh;

PRINT N'=== 42 COMBO MẪU LIỀU 2026 ===';
SELECT comboId, tenCombo, nhomBenh, giaBanCombo FROM dbo.MauLieu WHERE comboId LIKE N'CB-2026-%' ORDER BY nhomBenh, comboId;

PRINT N'=== PATCH HOÀN THÀNH ===';
GO
-- Đảm bảo tất cả viTriId trong SanPham đều trỏ đến ViTriThuoc hợp lệ
-- (gán mặc định cho những SP chưa có)
UPDATE [dbo].[SanPham]
SET viTriId = N'VT-0003'
WHERE viTriId IS NULL OR viTriId NOT IN (SELECT id FROM [dbo].[ViTriThuoc]);
GO

ALTER TABLE [dbo].[SanPham]
ADD CONSTRAINT [FK_SanPham_ViTriThuoc]
FOREIGN KEY ([viTriId]) REFERENCES [dbo].[ViTriThuoc]([id]);
GO
-- Đảm bảo không có comboId mồ côi trước khi thêm FK
DELETE FROM [dbo].[ChiTietMauLieu]
WHERE comboId NOT IN (SELECT comboId FROM [dbo].[MauLieu]);
GO

ALTER TABLE [dbo].[ChiTietMauLieu]
ADD CONSTRAINT [FK_ChiTietMauLieu_MauLieu]
FOREIGN KEY ([comboId]) REFERENCES [dbo].[MauLieu]([comboId]) ON DELETE CASCADE;
GO
CREATE TABLE [dbo].[NhaCungCap] (
    [id]        NVARCHAR(50)  NOT NULL PRIMARY KEY,
    [ten]       NVARCHAR(150) NOT NULL,
    [diaChi]    NVARCHAR(255) NULL,
    [sdt]       NVARCHAR(15)  NULL,
    [email]     NVARCHAR(100) NULL,
    [maSoThue]  NVARCHAR(20)  NULL,
    [trangThai] NVARCHAR(20)  NOT NULL DEFAULT N'HOAT_DONG',
    CONSTRAINT [CK_NhaCungCap_TrangThai]
        CHECK ([trangThai] IN (N'HOAT_DONG', N'NGUNG_HOP_TAC'))
);
GO

-- Dữ liệu mẫu khớp với nhà sản xuất trong SanPham
INSERT INTO [dbo].[NhaCungCap] VALUES
(N'NCC-0001', N'Pymepharco',          N'TP. Tuy Hòa, Phú Yên',    N'02573822896', N'info@pymepharco.com',   N'4100101123', N'HOAT_DONG'),
(N'NCC-0002', N'Stada VN',            N'Bình Dương',               N'02743781516', N'info@stada.com.vn',     N'3700400235', N'HOAT_DONG'),
(N'NCC-0003', N'Traphaco',            N'Hà Nội',                   N'02438584905', N'info@traphaco.com.vn',  N'0100108367', N'HOAT_DONG'),
(N'NCC-0004', N'Imexpharm',           N'Đồng Tháp',                N'02773877214', N'info@imexpharm.com',    N'1400101095', N'HOAT_DONG'),
(N'NCC-0005', N'DHG Pharma',          N'Cần Thơ',                  N'02923891433', N'info@dhgpharma.com.vn', N'1800100436', N'HOAT_DONG'),
(N'NCC-0006', N'Domesco',             N'Đồng Tháp',                N'02773877862', N'info@domesco.com.vn',   N'1400101062', N'HOAT_DONG'),
(N'NCC-0007', N'GSK Việt Nam',        N'Hà Nội',                   N'02439411100', N'info@gsk.com',          N'0101244889', N'HOAT_DONG'),
(N'NCC-0008', N'Sanofi Việt Nam',     N'TP.HCM',                   N'02839106200', N'info@sanofi.com',       N'0301452946', N'HOAT_DONG'),
(N'NCC-0009', N'Công ty Dược phẩm OPV', N'TP.HCM',               N'02838971818', N'info@opv.com.vn',       N'0302435789', N'HOAT_DONG'),
(N'NCC-0010', N'Nature Made (Nhập khẩu)', N'TP.HCM',             N'02838001122', N'import@naturemade.vn',  N'0302876543', N'HOAT_DONG');
GO

-- Gắn FK vào PhieuNhapHang
ALTER TABLE [dbo].[PhieuNhapHang]
ADD CONSTRAINT [FK_PhieuNhapHang_NhaCungCap]
FOREIGN KEY ([nhaCungCapId]) REFERENCES [dbo].[NhaCungCap]([id])
ON UPDATE CASCADE ON DELETE SET NULL;
GO

-- Cập nhật mẫu: gán nhà cung cấp cho phiếu nhập theo nhà sản xuất
UPDATE pn
SET pn.nhaCungCapId = CASE
    WHEN ct.sanPhamId LIKE 'SP2024-000[12]%' OR ct.sanPhamId IN ('SP2024-0001','SP2024-0006','SP2024-0041') THEN 'NCC-0001' -- Pymepharco
    WHEN ct.sanPhamId IN ('SP2024-0002','SP2024-0007','SP2024-0050','SP2024-0054') THEN 'NCC-0002' -- Stada
    WHEN ct.sanPhamId IN ('SP2024-0003','SP2024-0016','SP2024-0040','SP2024-0044','SP2024-0051','SP2024-0056') THEN 'NCC-0003' -- Traphaco
    WHEN ct.sanPhamId IN ('SP2024-0004','SP2024-0008','SP2024-0052') THEN 'NCC-0004' -- Imexpharm
    WHEN ct.sanPhamId IN ('SP2024-0011','SP2024-0019','SP2024-0023','SP2024-0039','SP2024-0058') THEN 'NCC-0005' -- DHG
    WHEN ct.sanPhamId IN ('SP2024-0009','SP2024-0046') THEN 'NCC-0006' -- Domesco
    WHEN ct.sanPhamId = 'SP2024-0005' THEN 'NCC-0007' -- GSK
    WHEN ct.sanPhamId = 'SP2024-0047' THEN 'NCC-0008' -- Sanofi
    WHEN ct.sanPhamId = 'SP2024-0010' THEN 'NCC-0009' -- OPV
    ELSE 'NCC-0001'
END
FROM [dbo].[PhieuNhapHang] pn
JOIN [dbo].[ChiTietPhieuNhapHang] ct ON ct.phieuNhapId = pn.id
WHERE pn.nhaCungCapId IS NULL;
GO
USE MYCAREPHARMACY;
GO

-- Bước 1: Xóa cái luật cũ đang chặn chữ 'AN'
ALTER TABLE KhuyenMai DROP CONSTRAINT CK_KhuyenMai_TrangThai;
GO

-- Bước 2: Tạo lại luật mới, cho phép thêm trạng thái 'AN'
ALTER TABLE KhuyenMai ADD CONSTRAINT CK_KhuyenMai_TrangThai 
CHECK (trangThai IN ('HOAT_DONG', 'KHONG_HOAT_DONG', 'AN'));
GO
CREATE TABLE [dbo].[LichSuDiem] (
    [id]          NVARCHAR(50)  NOT NULL PRIMARY KEY,
    [khachHangId] NVARCHAR(50)  NOT NULL,
    [hoaDonId]    NVARCHAR(50)  NULL,
    [loai]        NVARCHAR(10)  NOT NULL,
    [soDiem]      INT           NOT NULL,
    [ghiChu]      NVARCHAR(255) NULL,
    [thoiGian]    DATETIME2(7)  NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT [FK_LichSuDiem_KhachHang]
        FOREIGN KEY ([khachHangId]) REFERENCES [dbo].[KhachHang]([id]) ON DELETE CASCADE,
    CONSTRAINT [FK_LichSuDiem_HoaDon]
        FOREIGN KEY ([hoaDonId]) REFERENCES [dbo].[HoaDon]([id]) ON DELETE SET NULL
)INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00001', N'KH-0001', N'HD2024-0001', N'TICH', 8, N'Tích điểm từ hóa đơn HD2024-0001', CAST(N'2024-01-05 08:30:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00002', N'KH-0002', N'HD2024-0002', N'TICH', 6, N'Tích điểm từ hóa đơn HD2024-0002', CAST(N'2024-01-10 09:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00003', N'KH-0003', N'HD2024-0003', N'TICH', 5, N'Tích điểm từ hóa đơn HD2024-0003', CAST(N'2024-02-14 10:15:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00004', N'KH-0004', N'HD2024-0004', N'TICH', 15, N'Tích điểm từ hóa đơn HD2024-0004', CAST(N'2024-02-20 11:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00005', N'KH-0005', N'HD2024-0005', N'TICH', 60, N'Tích điểm từ hóa đơn HD2024-0005', CAST(N'2024-03-05 08:45:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00006', N'KH-0006', N'HD2024-0006', N'TICH', 3, N'Tích điểm từ hóa đơn HD2024-0006', CAST(N'2024-03-15 13:30:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00007', N'KH-0007', N'HD2024-0007', N'TICH', 103, N'Tích điểm từ hóa đơn HD2024-0007', CAST(N'2024-04-10 09:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00008', N'KH-0008', N'HD2024-0008', N'TICH', 141, N'Tích điểm từ hóa đơn HD2024-0008', CAST(N'2024-04-20 14:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00009', N'KH-0009', N'HD2024-0009', N'TICH', 12, N'Tích điểm từ hóa đơn HD2024-0009', CAST(N'2024-05-05 10:30:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00010', N'KH-0010', N'HD2024-0010', N'TICH', 18, N'Tích điểm từ hóa đơn HD2024-0010', CAST(N'2024-05-18 11:15:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00011', N'KH-0001', N'HD-2025-1', N'TICH', 8, N'Tích điểm từ hóa đơn HD-2025-1', CAST(N'2025-01-05 08:30:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00012', N'KH-0002', N'HD-2025-2', N'TICH', 5, N'Tích điểm từ hóa đơn HD-2025-2', CAST(N'2025-02-14 09:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00013', N'KH-0003', N'HD-2025-3', N'TICH', 8, N'Tích điểm từ hóa đơn HD-2025-3', CAST(N'2025-03-20 10:15:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00014', N'KH-0004', N'HD-2025-4', N'TICH', 5, N'Tích điểm từ hóa đơn HD-2025-4', CAST(N'2025-04-10 11:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00015', N'KH-0005', N'HD-2025-5', N'TICH', 32, N'Tích điểm từ hóa đơn HD-2025-5', CAST(N'2025-05-05 08:45:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00016', N'KH-0006', N'HD-2025-6', N'TICH', 3, N'Tích điểm từ hóa đơn HD-2025-6', CAST(N'2025-06-15 13:30:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00017', N'KH-0007', N'HD-2025-7', N'TICH', 84, N'Tích điểm từ hóa đơn HD-2025-7', CAST(N'2025-07-20 09:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00018', N'KH-0008', N'HD-2025-8', N'TICH', 1, N'Tích điểm từ hóa đơn HD-2025-8', CAST(N'2025-08-10 14:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00019', N'KH-0009', N'HD-2025-9', N'TICH', 11, N'Tích điểm từ hóa đơn HD-2025-9', CAST(N'2025-09-25 10:30:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00020', N'KH-0010', N'HD-2025-10', N'TICH', 7, N'Tích điểm từ hóa đơn HD-2025-10', CAST(N'2025-10-18 11:15:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00021', N'KH-0002', N'HD-2025-12', N'TICH', 3, N'Tích điểm từ hóa đơn HD-2025-12', CAST(N'2025-12-20 09:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00022', N'KH-0003', N'HD-2026-1', N'TICH', 4, N'Tích điểm từ hóa đơn HD-2026-1', CAST(N'2026-01-10 08:30:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00023', N'KH-0004', N'HD-2026-2', N'TICH', 5, N'Tích điểm từ hóa đơn HD-2026-2', CAST(N'2026-02-14 09:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00024', N'KH-0005', N'HD-2026-3', N'TICH', 28, N'Tích điểm từ hóa đơn HD-2026-3', CAST(N'2026-03-05 10:15:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00025', N'KH-0006', N'HD-2026-4', N'TICH', 32, N'Tích điểm từ hóa đơn HD-2026-4', CAST(N'2026-04-20 11:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00026', N'KH-0001', N'HD2024-0004', N'TIEU', 50, N'Dùng điểm giảm giá', CAST(N'2024-03-01 10:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00027', N'KH-0002', N'HD2024-0007', N'TIEU', 100, N'Dùng điểm thanh toán', CAST(N'2024-06-10 14:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00028', N'KH-0004', N'HD-2025-4', N'TIEU', 200, N'Dùng điểm đổi thưởng', CAST(N'2025-05-20 09:30:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00029', N'KH-0006', NULL, N'TIEU', 80, N'Điều chỉnh thủ công', CAST(N'2025-08-01 15:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00030', N'KH-0007', N'HD-2025-7', N'TIEU', 187, N'Dùng điểm thanh toán', CAST(N'2025-08-15 11:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00031', N'KH-0009', N'HD-2025-9', N'TIEU', 500, N'Dùng điểm đổi thưởng', CAST(N'2025-10-10 16:00:00.0000000' AS DateTime2));

GO
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00032', N'KH-0001', NULL, N'TICH', 534, N'Số dư tích lũy trước hệ thống', CAST(N'2024-01-10 08:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00033', N'KH-0002', NULL, N'TICH', 1286, N'Số dư tích lũy trước hệ thống', CAST(N'2024-02-15 09:30:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00034', N'KH-0003', NULL, N'TICH', 333, N'Số dư tích lũy trước hệ thống', CAST(N'2024-03-20 10:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00035', N'KH-0004', NULL, N'TICH', 2175, N'Số dư tích lũy trước hệ thống', CAST(N'2024-04-05 11:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00036', N'KH-0005', NULL, N'TICH', 30, N'Số dư tích lũy trước hệ thống', CAST(N'2024-05-12 13:00:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00037', N'KH-0006', NULL, N'TICH', 842, N'Số dư tích lũy trước hệ thống', CAST(N'2024-06-18 14:30:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00038', N'KH-0008', NULL, N'TICH', 308, N'Số dư tích lũy trước hệ thống', CAST(N'2024-08-30 09:15:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00039', N'KH-0009', NULL, N'TICH', 3477, N'Số dư tích lũy trước hệ thống', CAST(N'2024-09-14 10:30:00.0000000' AS DateTime2));
INSERT INTO [dbo].[LichSuDiem] VALUES (N'LS-00040', N'KH-0010', NULL, N'TICH', 75, N'Số dư tích lũy trước hệ thống', CAST(N'2024-10-25 15:00:00.0000000' AS DateTime2));

/* =========================================================
   PATCH: CHUẨN HÓA LỢI NHUẬN "NHẬP 5 BÁN 10" VÀ CẬP NHẬT TRIGGER TỰ ĐỘNG
   (Chỉ dán vào cuối file, chạy một lần là tự động xử lý toàn bộ)
   ========================================================= */

USE [MYCAREPHARMACY];
GO

PRINT N'===== BẮT ĐẦU CHUẨN HÓA DỮ LIỆU GIÁ BÁN =====';

-- 1. CẬP NHẬT GIÁ BÁN SẢN PHẨM = GIÁ NHẬP x 2 
-- Lấy giá nhập của lô hàng mới nhất làm mốc để nhân đôi giá bán.
WITH LatestLoHang AS (
    SELECT sanPhamId, gia,
           ROW_NUMBER() OVER(PARTITION BY sanPhamId ORDER BY ngayNhap DESC) as rn
    FROM dbo.LoHang
)
UPDATE sp
SET sp.giaBan = lh.gia * 2
FROM dbo.SanPham sp
JOIN LatestLoHang lh ON sp.id = lh.sanPhamId AND lh.rn = 1;
GO

-- 2. ĐỒNG BỘ GIÁ BÁN CHO ĐƠN VỊ ĐO LƯỜNG CƠ BẢN (Ví dụ: Viên, Gói, Chai)
UPDATE dvl
SET dvl.gia = sp.giaBan
FROM dbo.DonViDoLuong dvl
JOIN dbo.SanPham sp ON sp.id = dvl.sanPhamId AND dvl.ten = sp.donViDoCoBan;
GO

-- 3. TÍNH LẠI GIÁ BÁN CHO ĐƠN VỊ LỚN (Ví dụ: Hộp = Giá Viên x Số lượng viên/hộp)
UPDATE dvl
SET dvl.gia = dvl.chuyenDoiDonViCoBan * sp.giaBan
FROM dbo.DonViDoLuong dvl
JOIN dbo.SanPham sp ON sp.id = dvl.sanPhamId AND dvl.ten <> sp.donViDoCoBan;
GO

PRINT N'===== CẬP NHẬT TRIGGER TỰ ĐỘNG CHO HỆ THỐNG =====';

-- 4. TẠO LẠI TRIGGER (Bạn có thể Ctrl+F xóa cái trigger cũ ở dòng 1221 đi, hoặc để đoạn này chạy đè lên đều được)
DROP TRIGGER IF EXISTS [dbo].[TRG_DongBoGiaBan_LoHang];
GO

CREATE TRIGGER [dbo].[TRG_DongBoGiaBan_LoHang]
ON [dbo].[LoHang]
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    -- LOGIC CHUẨN: Chỉ tự động thiết lập giá bán (Giá nhập x 2) cho các sản phẩm 
    -- MỚI TINH chưa có giá (giaBan = 0). Sản phẩm cũ (giaBan > 0) giữ nguyên 
    -- để không làm loạn giá niêm yết trên kệ khi nhập lô mới.
    UPDATE SP
    SET SP.giaBan = I.gia * 2
    FROM SanPham SP
    JOIN inserted I ON SP.id = I.sanPhamId
    WHERE SP.giaBan = 0;
END;
GO

PRINT N'===== PATCH HOÀN TẤT =====';
GO