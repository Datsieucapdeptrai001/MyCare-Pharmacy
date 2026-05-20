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
	[gioiTinh] [nvarchar](10) NULL,
	[ngaySinh] [date] NULL,
	[diaChi] [nvarchar](255) NULL,
	[email] [nvarchar](100) NULL
)
GO

CREATE TABLE [dbo].[KhoHang](
	[id] [nvarchar](50) NOT NULL PRIMARY KEY,
	[sucChua] [int] NOT NULL
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
-- PHIẾU KIỂM KÊ KHO / CHI TIẾT PHIẾU KIỂM KÊ KHO
-- Đồng bộ từ SQLQuery4, đặt trong SQL_FINAL theo đúng thứ tự tạo bảng.
-- FK sẽ được add ở cuối sau khi đủ bảng cha: KhoHang, NhanVien, LoHang, SanPham.
-- ==============================================================================
CREATE TABLE [dbo].[PhieuKiemKeKho] (
    [id] NVARCHAR(50) NOT NULL PRIMARY KEY,
    [ngayKiemKe] DATETIME2(7) NOT NULL DEFAULT SYSDATETIME(),
    [khoHangId] NVARCHAR(50) NOT NULL,
    [nhanVienId] NVARCHAR(50) NULL,
    [tongSoDong] INT NOT NULL DEFAULT 0,
    [tongChenhLech] INT NOT NULL DEFAULT 0,
    [ghiChu] NVARCHAR(255) NULL,
    [trangThai] NVARCHAR(30) NOT NULL DEFAULT N'HOAN_THANH'
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
    [lyDo] NVARCHAR(255) NULL
)
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
INSERT [dbo].[NhanVien] VALUES (N'QL-0001', N'Quản Lý', N'CCHN-QL-2020-001', N'0901234567', N'admin@mycarepharmacy.vn', N'NGUOI_QUAN_LY', N'DANG_LAM_VIEC', N'Nam', CAST(N'1985-12-12' AS Date), N'TP.HCM', N'079085000006')

INSERT [dbo].[KhachHang] VALUES (N'KH-0001', N'0311223344', N'Nguyễn Thị Lan', CAST(N'2024-01-10T08:00:00.0000000' AS DateTime2), 500, NULL, NULL, NULL, NULL)
INSERT [dbo].[KhachHang] VALUES (N'KH-0002', N'0322334455', N'Trần Văn Bình', CAST(N'2024-02-15T09:30:00.0000000' AS DateTime2), 1200, NULL, NULL, NULL, NULL)
INSERT [dbo].[KhachHang] VALUES (N'KH-0003', N'0333445566', N'Lê Thị Hoa', CAST(N'2024-03-20T10:00:00.0000000' AS DateTime2), 350, NULL, NULL, NULL, NULL)
INSERT [dbo].[KhachHang] VALUES (N'KH-0004', N'0344556677', N'Phạm Thanh Tùng', CAST(N'2024-04-05T11:00:00.0000000' AS DateTime2), 2000, NULL, NULL, NULL, NULL)
INSERT [dbo].[KhachHang] VALUES (N'KH-0005', N'0355667788', N'Võ Thị Mai', CAST(N'2024-05-12T13:00:00.0000000' AS DateTime2), 150, NULL, NULL, NULL, NULL)
INSERT [dbo].[KhachHang] VALUES (N'KH-0006', N'0366778899', N'Đặng Minh Khoa', CAST(N'2024-06-18T14:30:00.0000000' AS DateTime2), 800, NULL, NULL, NULL, NULL)
INSERT [dbo].[KhachHang] VALUES (N'KH-0007', N'0377889900', N'Hoàng Thị Thu', CAST(N'2024-07-22T08:45:00.0000000' AS DateTime2), 0, NULL, NULL, NULL, NULL)
INSERT [dbo].[KhachHang] VALUES (N'KH-0008', N'0388990011', N'Bùi Văn Long', CAST(N'2024-08-30T09:15:00.0000000' AS DateTime2), 450, NULL, NULL, NULL, NULL)
INSERT [dbo].[KhachHang] VALUES (N'KH-0009', N'0399001122', N'Ngô Thị Thanh', CAST(N'2024-09-14T10:30:00.0000000' AS DateTime2), 3000, NULL, NULL, NULL, NULL)
INSERT [dbo].[KhachHang] VALUES (N'KH-0010', N'0310112233', N'Dương Quốc Hùng', CAST(N'2024-10-25T15:00:00.0000000' AS DateTime2), 100, NULL, NULL, NULL, NULL)

INSERT [dbo].[KhoHang] VALUES (N'KHO-0001', 5000)
INSERT [dbo].[KhoHang] VALUES (N'KHO-0002', 3000)

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

INSERT [dbo].[SanPham] VALUES (N'SP2024-0001', N'THUOC_KE_DON', N'VIEN_NANG', N'Amoxicillin 500mg', N'Amox 500', N'Pymepharco', N'Amoxicillin trihydrate', CAST(5.00 AS Decimal(18, 2)), N'500mg', N'Kháng sinh nhóm penicillin', N'Viên', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(55000.00 AS Decimal(18, 2)), N'8934663100017', N'Kháng sinh – Kháng viêm')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0002', N'THUOC_KE_DON', N'VIEN_NEN', N'Cefuroxime 500mg', N'Cefu 500', N'Stada VN', N'Cefuroxime axetil', CAST(5.00 AS Decimal(18, 2)), N'500mg', N'Kháng sinh cephalosporin', N'Viên', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(65000.00 AS Decimal(18, 2)), N'8934663100024', N'Kháng sinh – Kháng viêm')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0003', N'THUOC_KE_DON', N'VIEN_NEN', N'Metformin 500mg', N'Metf 500', N'Traphaco', N'Metformin hydrochloride', CAST(5.00 AS Decimal(18, 2)), N'500mg', N'Điều trị đái tháo đường', N'Viên', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(75000.00 AS Decimal(18, 2)), N'8934663100031', N'Đái tháo đường')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0004', N'THUOC_KE_DON', N'VIEN_NEN', N'Losartan 50mg', N'Losar 50', N'Imexpharm', N'Losartan kali', CAST(5.00 AS Decimal(18, 2)), N'50mg', N'Thuốc hạ huyết áp', N'Viên', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(40000.00 AS Decimal(18, 2)), N'8934663100048', N'Tim mạch – Huyết áp')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0005', N'THUOC_KE_DON', N'HON_DICH', N'Augmentin 250mg/5ml Siro', N'Aug Siro', N'GSK', N'Amoxicillin + Acid clavulanic', CAST(5.00 AS Decimal(18, 2)), N'250mg/5ml', N'Kháng sinh cho trẻ em', N'Chai', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(90000.00 AS Decimal(18, 2)), N'8934663100086', N'Kháng sinh – Kháng viêm')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0006', N'THUOC_KE_DON', N'HON_DICH', N'Azithromycin 200mg/5ml', N'Azith Siro', N'Pymepharco', N'Azithromycin dihydrate', CAST(5.00 AS Decimal(18, 2)), N'200mg/5ml', N'Kháng sinh macrolide', N'Chai', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(75000.00 AS Decimal(18, 2)), N'8934663100093', N'Kháng sinh – Kháng viêm')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0007', N'THUOC_KE_DON', N'VIEN_NEN', N'Amlodipine 5mg', N'Amlo 5', N'Stada VN', N'Amlodipine besylate', CAST(5.00 AS Decimal(18, 2)), N'5mg', N'Điều trị tăng huyết áp', N'Viên', CAST(N'2024-01-02T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(70000.00 AS Decimal(18, 2)), N'8934663100109', N'Tim mạch – Huyết áp')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0008', N'THUOC_KE_DON', N'VIEN_NEN', N'Atorvastatin 20mg', N'Ator 20', N'Imexpharm', N'Atorvastatin calcium', CAST(5.00 AS Decimal(18, 2)), N'20mg', N'Thuốc hạ mỡ máu', N'Viên', CAST(N'2024-01-02T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(98000.00 AS Decimal(18, 2)), N'8934663100116', N'Tim mạch – Huyết áp')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0009', N'THUOC_KHONG_KE_DON', N'VIEN_NEN', N'Paracetamol 500mg', N'Para 500', N'Domesco', N'Paracetamol', CAST(5.00 AS Decimal(18, 2)), N'500mg', N'Giảm đau, hạ sốt', N'Viên', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(40000.00 AS Decimal(18, 2)), N'8934673100014', N'Thần kinh – Giảm đau')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0010', N'THUOC_KHONG_KE_DON', N'VIEN_NEN', N'Ibuprofen 400mg', N'Ibup 400', N'OPV', N'Ibuprofen', CAST(5.00 AS Decimal(18, 2)), N'400mg', N'Kháng viêm không steroid', N'Viên', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(18000.00 AS Decimal(18, 2)), N'8934673100021', N'Thần kinh – Giảm đau')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0011', N'THUOC_KHONG_KE_DON', N'VIEN_SUI', N'Vitamin C 1000mg Sủi', N'VitC 1000', N'DHG Pharma', N'Acid ascorbic', CAST(5.00 AS Decimal(18, 2)), N'1000mg', N'Tăng sức đề kháng', N'Viên', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(65000.00 AS Decimal(18, 2)), N'8934673100038', N'Vitamin – Bổ sung')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0012', N'THUOC_KHONG_KE_DON', N'VIEN_NEN', N'Cetirizine 10mg', N'Cetir 10', N'Stada VN', N'Cetirizine hydrochloride', CAST(5.00 AS Decimal(18, 2)), N'10mg', N'Chống dị ứng', N'Viên', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(18000.00 AS Decimal(18, 2)), N'8934673100045', N'Hô hấp – Dị ứng')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0013', N'THUOC_KHONG_KE_DON', N'THUOC_BOT', N'Oresol Cam', N'ORS Cam', N'Vinpharco', N'Glucose + Natri Clorid', CAST(5.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Bù nước và điện giải', N'Gói', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(90000.00 AS Decimal(18, 2)), N'8934673100069', N'Tiêu hóa – Dạ dày')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0014', N'THUOC_KHONG_KE_DON', N'DUNG_DICH', N'Xịt mũi Naphazoline 0.05%', N'Xịt Naphaz', N'Pharmedic', N'Naphazoline hydrochloride', CAST(5.00 AS Decimal(18, 2)), N'0.05%', N'Giảm ngạt mũi', N'Chai', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(35000.00 AS Decimal(18, 2)), N'8934673100052', N'Hô hấp – Dị ứng')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0015', N'THUOC_KHONG_KE_DON', N'VIEN_NEN', N'Domperidon 10mg', N'Domp 10', N'Pymepharco', N'Domperidone', CAST(5.00 AS Decimal(18, 2)), N'10mg', N'Chống nôn', N'Viên', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(40000.00 AS Decimal(18, 2)), N'8934673100076', N'Tiêu hóa – Dạ dày')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0016', N'THUOC_KHONG_KE_DON', N'VIEN_NANG', N'Omeprazole 20mg', N'Ome 20', N'Traphaco', N'Omeprazole', CAST(5.00 AS Decimal(18, 2)), N'20mg', N'Điều trị loét dạ dày', N'Viên', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(52000.00 AS Decimal(18, 2)), N'8934673100083', N'Tiêu hóa – Dạ dày')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0017', N'THUOC_KHONG_KE_DON', N'DUNG_DICH', N'Siro Ho Bổ Phế', N'Siro Ho BP', N'Traphaco', N'Thảo dược', CAST(5.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Giảm ho', N'Chai', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(55000.00 AS Decimal(18, 2)), N'8934673100090', N'Tiêu hóa – Dạ dày')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0018', N'THUOC_KHONG_KE_DON', N'THUOC_BOT', N'Smecta 3g Bột', N'Smecta', N'Ipsen', N'Diosmectite', CAST(5.00 AS Decimal(18, 2)), N'3g', N'Điều trị tiêu chảy', N'Gói', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(220000.00 AS Decimal(18, 2)), N'8934673100106', N'Tiêu hóa – Dạ dày')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0019', N'THUOC_KHONG_KE_DON', N'VIEN_NEN', N'Loratadine 10mg', N'Lorat 10', N'DHG Pharma', N'Loratadine', CAST(5.00 AS Decimal(18, 2)), N'10mg', N'Kháng histamine', N'Viên', CAST(N'2024-01-03T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(16000.00 AS Decimal(18, 2)), N'8934673100168', N'Hô hấp – Dị ứng')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0020', N'THUOC_KHONG_KE_DON', N'VIEN_NANG', N'Esomeprazole 20mg', N'Esome 20', N'AstraZeneca', N'Esomeprazole magnesium', CAST(5.00 AS Decimal(18, 2)), N'20mg', N'Điều trị trào ngược', N'Viên', CAST(N'2024-01-03T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(52000.00 AS Decimal(18, 2)), N'8934673100175', N'Tiêu hóa – Dạ dày')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0021', N'THUC_PHAM_CHUC_NANG', N'VIEN_NANG', N'Vitamin D3 K2 2000IU', N'VitD3K2', N'Nature Made', N'Cholecalciferol', CAST(10.00 AS Decimal(18, 2)), N'2000IU', N'Hỗ trợ xương khớp', N'Viên', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(220000.00 AS Decimal(18, 2)), N'8934673100113', N'Vitamin – Bổ sung')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0022', N'THUC_PHAM_CHUC_NANG', N'VIEN_NANG', N'Omega-3 Fish Oil 1000mg', N'Omega3 1000', N'Nature Made', N'EPA + DHA', CAST(10.00 AS Decimal(18, 2)), N'1000mg', N'Hỗ trợ tim mạch', N'Viên', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(280000.00 AS Decimal(18, 2)), N'8934673100120', N'Vitamin – Bổ sung')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0023', N'THUC_PHAM_CHUC_NANG', N'VIEN_NEN', N'Canxi Nano 500mg', N'Canxi Nano', N'DHG Pharma', N'Calcium carbonate', CAST(10.00 AS Decimal(18, 2)), N'500mg', N'Bổ sung canxi', N'Viên', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(85000.00 AS Decimal(18, 2)), N'8934673100137', N'Vitamin – Bổ sung')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0024', N'THUC_PHAM_CHUC_NANG', N'DUNG_DICH', N'Collagen Peptide 5000mg', N'Collagen 5K', N'Kinoko VN', N'Collagen hydrolyzed', CAST(10.00 AS Decimal(18, 2)), N'5000mg', N'Làm đẹp da', N'Gói', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(420000.00 AS Decimal(18, 2)), N'4005900117908', N'Da liễu – Mỹ phẩm')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0025', N'THUC_PHAM_CHUC_NANG', N'KEO_NGAM', N'Melatonin 5mg', N'Melat 5', N'Natrol USA', N'Melatonin', CAST(10.00 AS Decimal(18, 2)), N'5mg', N'Hỗ trợ giấc ngủ', N'Viên', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(280000.00 AS Decimal(18, 2)), N'8934673100144', N'Vitamin – Bổ sung')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0026', N'THUC_PHAM_CHUC_NANG', N'VIEN_NEN', N'Magie B6 Úc', N'Mg B6', N'Blackmores', N'Magnesium + Vitamin B6', CAST(10.00 AS Decimal(18, 2)), N'500mg', N'Giảm căng thẳng', N'Viên', CAST(N'2024-01-04T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(195000.00 AS Decimal(18, 2)), N'8934673100151', N'Vitamin – Bổ sung')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0027', N'THUC_PHAM_CHUC_NANG', N'THUOC_BOT', N'Probiotics 10 tỷ CFU', N'Probio 10B', N'Yakult VN', N'Lactobacillus', CAST(10.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Hỗ trợ tiêu hóa', N'Viên', CAST(N'2024-01-04T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(165000.00 AS Decimal(18, 2)), N'8934673100229', N'Vitamin – Bổ sung')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0028', N'MY_PHAM', N'HON_DICH', N'Kem dưỡng da Eucerin Q10', N'Eucerin Q10', N'Eucerin', N'Chưa cập nhật', CAST(10.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Chống lão hóa', N'Hộp', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(650000.00 AS Decimal(18, 2)), N'4005900272935', N'Da liễu – Mỹ phẩm')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0029', N'MY_PHAM', N'HON_DICH', N'Kem chống nắng La Roche', N'LRP SPF50', N'La Roche-Posay', N'Chưa cập nhật', CAST(10.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Bảo vệ da nhạy cảm', N'Tuýp', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(580000.00 AS Decimal(18, 2)), N'3337872413148', N'Da liễu – Mỹ phẩm')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0030', N'MY_PHAM', N'DUNG_DICH', N'Sữa rửa mặt CeraVe', N'CeraVe Foam', N'CeraVe', N'Chưa cập nhật', CAST(10.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Sữa rửa mặt tạo bọt', N'Chai', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(320000.00 AS Decimal(18, 2)), N'3606000594227', N'Da liễu – Mỹ phẩm')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0031', N'MY_PHAM', N'DUNG_DICH', N'Nước tẩy trang Bioderma', N'Bioderma', N'Bioderma', N'Chưa cập nhật', CAST(10.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Tẩy trang dịu nhẹ', N'Chai', CAST(N'2024-01-01T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(280000.00 AS Decimal(18, 2)), N'3701129801420', N'Da liễu – Mỹ phẩm')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0032', N'MY_PHAM', N'DUNG_DICH', N'Toner Paula Niacinamide', N'Paula Toner', N'Paula Choice', N'Niacinamide', CAST(10.00 AS Decimal(18, 2)), N'10%', N'Làm sáng da', N'Chai', CAST(N'2024-01-05T00:00:00.0000000' AS DateTime2), N'HOAT_DONG', CAST(850000.00 AS Decimal(18, 2)), N'0761591032015', N'Da liễu – Mỹ phẩm')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0033', N'THUOC_KHONG_KE_DON', N'THUOC_NHO_GIOT', N'V.Rohto Vitamin', N'V.Rohto', N'Rohto', N'Vitamin B5, B6', CAST(10.00 AS Decimal(18, 2)), N'13ml', N'Giảm mỏi mắt', N'Chai', GETDATE(), N'HOAT_DONG', CAST(52000.00 AS Decimal(18, 2)), N'4987241115532', N'Mắt – Tai – Mũi')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0034', N'THUOC_KHONG_KE_DON', N'THUOC_NHO_GIOT', N'Thuốc nhỏ mắt Osla', N'Osla', N'MerAP', N'Natri clorid', CAST(5.00 AS Decimal(18, 2)), N'15ml', N'Rửa mắt', N'Chai', GETDATE(), N'HOAT_DONG', CAST(22000.00 AS Decimal(18, 2)), N'8936077610014', N'Mắt – Tai – Mũi')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0035', N'MY_PHAM', N'SUC_MIENG', N'Listerine Cool Mint', N'Listerine', N'Johnson', N'Thymol', CAST(10.00 AS Decimal(18, 2)), N'750ml', N'Hơi thở thơm mát', N'Chai', GETDATE(), N'HOAT_DONG', CAST(115000.00 AS Decimal(18, 2)), N'0761591032022', N'Da liễu – Mỹ phẩm')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0036', N'MY_PHAM', N'SUC_MIENG', N'Betadine Gargle', N'Betadine', N'Mundipharma', N'Povidone-Iodine', CAST(10.00 AS Decimal(18, 2)), N'125ml', N'Sát khuẩn miệng', N'Chai', GETDATE(), N'HOAT_DONG', CAST(68000.00 AS Decimal(18, 2)), N'5038483381038', N'Da liễu – Mỹ phẩm')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0037', N'THUOC_KHONG_KE_DON', N'KEO_NGAM', N'Strepsils Cool', N'Strepsils', N'Reckitt', N'Dichlorobenzyl', CAST(10.00 AS Decimal(18, 2)), N'1.2mg', N'Giảm đau họng', N'Viên', GETDATE(), N'HOAT_DONG', CAST(80000.00 AS Decimal(18, 2)), N'8934673100205', N'Hô hấp – Dị ứng')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0038', N'THUOC_KHONG_KE_DON', N'VIEN_SUI', N'Efferalgan 500mg', N'Efferalgan', N'UPSA', N'Paracetamol', CAST(5.00 AS Decimal(18, 2)), N'500mg', N'Hạ sốt nhanh', N'Viên', GETDATE(), N'HOAT_DONG', CAST(62000.00 AS Decimal(18, 2)), N'8934673100182', N'Thần kinh – Giảm đau')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0039', N'THUOC_KHONG_KE_DON', N'THUOC_BOT', N'Hapacol 150', N'Hapacol 150', N'DHG Pharma', N'Paracetamol', CAST(5.00 AS Decimal(18, 2)), N'150mg', N'Hạ sốt cho trẻ', N'Gói', GETDATE(), N'HOAT_DONG', CAST(68000.00 AS Decimal(18, 2)), N'8934673100199', N'Vitamin – Bổ sung')
INSERT [dbo].[SanPham] VALUES (N'SP2024-0040', N'THUOC_KE_DON', N'HON_DICH', N'Phosphalugel', N'Chữ P', N'Astellas', N'Aluminum phosphate', CAST(5.00 AS Decimal(18, 2)), N'20%', N'Kháng axit dạ dày', N'Gói', GETDATE(), N'HOAT_DONG', CAST(110000.00 AS Decimal(18, 2)), N'8934673100212', N'Tiêu hóa – Dạ dày')

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


-- ==============================================================================
-- DỮ LIỆU LỊCH SỬ KIỂM KÊ KHO MẪU 2026
-- Dữ liệu dùng cho màn hình Kiểm kê kho và Nhật ký lịch sử kiểm kê.
-- Một số phiếu có chênh lệch nhỏ để kiểm thử màu chênh lệch, lý do và chi tiết phiếu.
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

-- Đồng bộ tồn hiện tại theo phiếu kiểm kê mới nhất có chênh lệch.
UPDATE [dbo].[LoHang] SET [soLuongLoHang] = 99 WHERE [id] = N'LH-0021';
UPDATE [dbo].[LoHang] SET [soLuongLoHang] = 62 WHERE [id] = N'LH-0024';
UPDATE [dbo].[LoHang] SET [soLuongLoHang] = 198 WHERE [id] = N'LH-0001';
GO

INSERT [dbo].[CaLamViec] VALUES (N'Ca-0001', N'DS-0001', CAST(N'2024-03-01T07:30:00.0000000' AS DateTime2), CAST(N'2024-03-01T17:30:00.0000000' AS DateTime2), CAST(2500000.00 AS Decimal(18,2)), CAST(500000.00 AS Decimal(18,2)), CAST(3000000.00 AS Decimal(18,2)), 1, N'Bình thường')

-- ==============================================================================
-- DỮ LIỆU LỊCH SỬ KIỂM KÊ KHO MẪU 2026
-- Dữ liệu dùng cho màn hình Kiểm kê kho và Nhật ký lịch sử kiểm kê.
-- Một số phiếu có chênh lệch nhỏ để kiểm thử màu chênh lệch, lý do và chi tiết phiếu.
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

-- Đồng bộ tồn hiện tại theo phiếu kiểm kê mới nhất có chênh lệch.
UPDATE [dbo].[LoHang] SET [soLuongLoHang] = 99 WHERE [id] = N'LH-0021';
UPDATE [dbo].[LoHang] SET [soLuongLoHang] = 62 WHERE [id] = N'LH-0024';
UPDATE [dbo].[LoHang] SET [soLuongLoHang] = 198 WHERE [id] = N'LH-0001';
GO

INSERT [dbo].[CaLamViec] VALUES (N'Ca-0002', N'DS-0002', CAST(N'2024-03-01T07:30:00.0000000' AS DateTime2), CAST(N'2024-03-01T17:30:00.0000000' AS DateTime2), CAST(1800000.00 AS Decimal(18,2)), CAST(500000.00 AS Decimal(18,2)), CAST(2300000.00 AS Decimal(18,2)), 1, N'Bình thường')

-- ==============================================================================
-- DỮ LIỆU LỊCH SỬ KIỂM KÊ KHO MẪU 2026
-- Dữ liệu dùng cho màn hình Kiểm kê kho và Nhật ký lịch sử kiểm kê.
-- Một số phiếu có chênh lệch nhỏ để kiểm thử màu chênh lệch, lý do và chi tiết phiếu.
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

-- Đồng bộ tồn hiện tại theo phiếu kiểm kê mới nhất có chênh lệch.
UPDATE [dbo].[LoHang] SET [soLuongLoHang] = 99 WHERE [id] = N'LH-0021';
UPDATE [dbo].[LoHang] SET [soLuongLoHang] = 62 WHERE [id] = N'LH-0024';
UPDATE [dbo].[LoHang] SET [soLuongLoHang] = 198 WHERE [id] = N'LH-0001';
GO

INSERT [dbo].[CaLamViec] VALUES (N'Ca-0003', N'DS-0003', CAST(N'2024-03-02T07:30:00.0000000' AS DateTime2), CAST(N'2024-03-02T17:30:00.0000000' AS DateTime2), CAST(3200000.00 AS Decimal(18,2)), CAST(500000.00 AS Decimal(18,2)), CAST(3700000.00 AS Decimal(18,2)), 1, N'Bình thường')

-- ==============================================================================
-- DỮ LIỆU LỊCH SỬ KIỂM KÊ KHO MẪU 2026
-- Dữ liệu dùng cho màn hình Kiểm kê kho và Nhật ký lịch sử kiểm kê.
-- Một số phiếu có chênh lệch nhỏ để kiểm thử màu chênh lệch, lý do và chi tiết phiếu.
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

-- Đồng bộ tồn hiện tại theo phiếu kiểm kê mới nhất có chênh lệch.
UPDATE [dbo].[LoHang] SET [soLuongLoHang] = 99 WHERE [id] = N'LH-0021';
UPDATE [dbo].[LoHang] SET [soLuongLoHang] = 62 WHERE [id] = N'LH-0024';
UPDATE [dbo].[LoHang] SET [soLuongLoHang] = 198 WHERE [id] = N'LH-0001';
GO

INSERT [dbo].[CaLamViec] VALUES (N'Ca-0004', N'DS-0004', CAST(N'2024-03-02T07:30:00.0000000' AS DateTime2), CAST(N'2024-03-02T17:30:00.0000000' AS DateTime2), CAST(2100000.00 AS Decimal(18,2)), CAST(500000.00 AS Decimal(18,2)), CAST(2600000.00 AS Decimal(18,2)), 1, N'Bình thường')

-- ==============================================================================
-- DỮ LIỆU LỊCH SỬ KIỂM KÊ KHO MẪU 2026
-- Dữ liệu dùng cho màn hình Kiểm kê kho và Nhật ký lịch sử kiểm kê.
-- Một số phiếu có chênh lệch nhỏ để kiểm thử màu chênh lệch, lý do và chi tiết phiếu.
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

-- Đồng bộ tồn hiện tại theo phiếu kiểm kê mới nhất có chênh lệch.
UPDATE [dbo].[LoHang] SET [soLuongLoHang] = 99 WHERE [id] = N'LH-0021';
UPDATE [dbo].[LoHang] SET [soLuongLoHang] = 62 WHERE [id] = N'LH-0024';
UPDATE [dbo].[LoHang] SET [soLuongLoHang] = 198 WHERE [id] = N'LH-0001';
GO

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
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0006', N'DVL-0033', N'SP2024-0019', 2, CAST(16000.00 AS Decimal(18,2)), CAST(32000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0007', N'DVL-0043', N'SP2024-0024', 2, CAST(420000.00 AS Decimal(18,2)), CAST(840000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0007', N'DVL-0047', N'SP2024-0026', 1, CAST(195000.00 AS Decimal(18,2)), CAST(195000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0008', N'DVL-0021', N'SP2024-0012', 1, CAST(18000.00 AS Decimal(18,2)), CAST(18000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0008', N'DVL-0044', N'SP2024-0025', 5, CAST(280000.00 AS Decimal(18,2)), CAST(1400000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0009', N'DVL-0030', N'SP2024-0017', 2, CAST(55000.00 AS Decimal(18,2)), CAST(110000.00 AS Decimal(18,2)), NULL)
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD2024-0009', N'DVL-0031', N'SP2024-0018', 1, CAST(220000.00 AS Decimal(18,2)), CAST(220000.00 AS Decimal(18,2)), NULL)
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
INSERT [dbo].[ChiTietHoaDon] VALUES (N'HD-2025-11', N'DVL-0023', N'SP2024-0013', 1, CAST(90000.00 AS Decimal(18,2)), CAST(90000.00 AS Decimal(18,2)), NULL)
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
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0004', N'DVL-0028', N'SP2024-0016', N'LH-0015', 2)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0005', N'DVL-0052', N'SP2024-0030', N'LH-0028', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0005', N'DVL-0053', N'SP2024-0031', N'LH-0029', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0006', N'DVL-0025', N'SP2024-0014', N'LH-0039', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0006', N'DVL-0033', N'SP2024-0019', N'LH-0018', 2)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0007', N'DVL-0043', N'SP2024-0024', N'LH-0023', 2)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0007', N'DVL-0047', N'SP2024-0026', N'LH-0025', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0008', N'DVL-0021', N'SP2024-0012', N'LH-0012', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0008', N'DVL-0044', N'SP2024-0025', N'LH-0024', 5)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0009', N'DVL-0030', N'SP2024-0017', N'LH-0016', 2)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0009', N'DVL-0031', N'SP2024-0018', N'LH-0017', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0010', N'DVL-0006', N'SP2024-0003', N'LH-0003', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0010', N'DVL-0008', N'SP2024-0004', N'LH-0004', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD2024-0010', N'DVL-0019', N'SP2024-0011', N'LH-0011', 1)

-- PHAN BO LO HANG 2025
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-1', N'DVL-0008', N'SP2024-0004', N'LH-0004', 2)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-2', N'DVL-0030', N'SP2024-0017', N'LH-0016', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-3', N'DVL-0016', N'SP2024-0009', N'LH-0009', 2)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-4', N'DVL-0002', N'SP2024-0001', N'LH-0001', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-5', N'DVL-0052', N'SP2024-0030', N'LH-0028', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-6', N'DVL-0025', N'SP2024-0014', N'LH-0039', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-7', N'DVL-0043', N'SP2024-0024', N'LH-0023', 2)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-8', N'DVL-0021', N'SP2024-0012', N'LH-0012', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-9', N'DVL-0030', N'SP2024-0017', N'LH-0016', 2)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-10', N'DVL-0006', N'SP2024-0003', N'LH-0003', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-11', N'DVL-0023', N'SP2024-0013', N'LH-0013', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2025-12', N'DVL-0025', N'SP2024-0014', N'LH-0039', 1)

-- PHAN BO LO HANG 2026
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2026-1', N'DVL-0026', N'SP2024-0015', N'LH-0014', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2026-2', N'DVL-0030', N'SP2024-0017', N'LH-0016', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2026-3', N'DVL-0039', N'SP2024-0022', N'LH-0021', 1)
INSERT [dbo].[PhanBoLoHang] VALUES (N'HD-2026-4', N'DVL-0052', N'SP2024-0030', N'LH-0028', 1)
GO

-- ==============================================================================
-- 5b. CẬP NHẬT maVach VÀ nhomBenhLy CHO 40 SẢN PHẨM
-- ==============================================================================

UPDATE [dbo].[SanPham] SET [maVach]=N'8934663100017', [nhomBenhLy]=N'Kháng sinh – Kháng viêm' WHERE [id]=N'SP2024-0001'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934663100024', [nhomBenhLy]=N'Kháng sinh – Kháng viêm' WHERE [id]=N'SP2024-0002'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934663100086', [nhomBenhLy]=N'Kháng sinh – Kháng viêm' WHERE [id]=N'SP2024-0005'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934663100093', [nhomBenhLy]=N'Kháng sinh – Kháng viêm' WHERE [id]=N'SP2024-0006'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934663100031', [nhomBenhLy]=N'Đái tháo đường' WHERE [id]=N'SP2024-0003'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934663100048', [nhomBenhLy]=N'Tim mạch – Huyết áp' WHERE [id]=N'SP2024-0004'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934663100109', [nhomBenhLy]=N'Tim mạch – Huyết áp' WHERE [id]=N'SP2024-0007'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934663100116', [nhomBenhLy]=N'Tim mạch – Huyết áp' WHERE [id]=N'SP2024-0008'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100014', [nhomBenhLy]=N'Thần kinh – Giảm đau' WHERE [id]=N'SP2024-0009'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100021', [nhomBenhLy]=N'Thần kinh – Giảm đau' WHERE [id]=N'SP2024-0010'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100182', [nhomBenhLy]=N'Thần kinh – Giảm đau' WHERE [id]=N'SP2024-0038'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100038', [nhomBenhLy]=N'Vitamin – Bổ sung' WHERE [id]=N'SP2024-0011'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100113', [nhomBenhLy]=N'Vitamin – Bổ sung' WHERE [id]=N'SP2024-0021'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100120', [nhomBenhLy]=N'Vitamin – Bổ sung' WHERE [id]=N'SP2024-0022'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100137', [nhomBenhLy]=N'Vitamin – Bổ sung' WHERE [id]=N'SP2024-0023'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100144', [nhomBenhLy]=N'Vitamin – Bổ sung' WHERE [id]=N'SP2024-0025'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100151', [nhomBenhLy]=N'Vitamin – Bổ sung' WHERE [id]=N'SP2024-0026'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100229', [nhomBenhLy]=N'Vitamin – Bổ sung' WHERE [id]=N'SP2024-0027'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100199', [nhomBenhLy]=N'Vitamin – Bổ sung' WHERE [id]=N'SP2024-0039'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100045', [nhomBenhLy]=N'Hô hấp – Dị ứng' WHERE [id]=N'SP2024-0012'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100052', [nhomBenhLy]=N'Hô hấp – Dị ứng' WHERE [id]=N'SP2024-0014'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100168', [nhomBenhLy]=N'Hô hấp – Dị ứng' WHERE [id]=N'SP2024-0019'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100205', [nhomBenhLy]=N'Hô hấp – Dị ứng' WHERE [id]=N'SP2024-0037'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100069', [nhomBenhLy]=N'Tiêu hóa – Dạ dày' WHERE [id]=N'SP2024-0013'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100076', [nhomBenhLy]=N'Tiêu hóa – Dạ dày' WHERE [id]=N'SP2024-0015'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100083', [nhomBenhLy]=N'Tiêu hóa – Dạ dày' WHERE [id]=N'SP2024-0016'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100090', [nhomBenhLy]=N'Tiêu hóa – Dạ dày' WHERE [id]=N'SP2024-0017'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100106', [nhomBenhLy]=N'Tiêu hóa – Dạ dày' WHERE [id]=N'SP2024-0018'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100175', [nhomBenhLy]=N'Tiêu hóa – Dạ dày' WHERE [id]=N'SP2024-0020'
UPDATE [dbo].[SanPham] SET [maVach]=N'8934673100212', [nhomBenhLy]=N'Tiêu hóa – Dạ dày' WHERE [id]=N'SP2024-0040'
UPDATE [dbo].[SanPham] SET [maVach]=N'4005900117908', [nhomBenhLy]=N'Da liễu – Mỹ phẩm' WHERE [id]=N'SP2024-0024'
UPDATE [dbo].[SanPham] SET [maVach]=N'4005900272935', [nhomBenhLy]=N'Da liễu – Mỹ phẩm' WHERE [id]=N'SP2024-0028'
UPDATE [dbo].[SanPham] SET [maVach]=N'3337872413148', [nhomBenhLy]=N'Da liễu – Mỹ phẩm' WHERE [id]=N'SP2024-0029'
UPDATE [dbo].[SanPham] SET [maVach]=N'3606000594227', [nhomBenhLy]=N'Da liễu – Mỹ phẩm' WHERE [id]=N'SP2024-0030'
UPDATE [dbo].[SanPham] SET [maVach]=N'3701129801420', [nhomBenhLy]=N'Da liễu – Mỹ phẩm' WHERE [id]=N'SP2024-0031'
UPDATE [dbo].[SanPham] SET [maVach]=N'0761591032015', [nhomBenhLy]=N'Da liễu – Mỹ phẩm' WHERE [id]=N'SP2024-0032'
UPDATE [dbo].[SanPham] SET [maVach]=N'0761591032022', [nhomBenhLy]=N'Da liễu – Mỹ phẩm' WHERE [id]=N'SP2024-0035'
UPDATE [dbo].[SanPham] SET [maVach]=N'5038483381038', [nhomBenhLy]=N'Da liễu – Mỹ phẩm' WHERE [id]=N'SP2024-0036'
UPDATE [dbo].[SanPham] SET [maVach]=N'4987241115532', [nhomBenhLy]=N'Mắt – Tai – Mũi' WHERE [id]=N'SP2024-0033'
UPDATE [dbo].[SanPham] SET [maVach]=N'8936077610014', [nhomBenhLy]=N'Mắt – Tai – Mũi' WHERE [id]=N'SP2024-0034'

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
ALTER TABLE [dbo].[PhieuKiemKeKho] ADD CONSTRAINT [FK_PKK_KhoHang]
FOREIGN KEY([khoHangId]) REFERENCES [dbo].[KhoHang]([id]) ON UPDATE CASCADE
ALTER TABLE [dbo].[PhieuKiemKeKho] ADD CONSTRAINT [FK_PKK_NhanVien]
FOREIGN KEY([nhanVienId]) REFERENCES [dbo].[NhanVien]([id]) ON UPDATE CASCADE ON DELETE SET NULL
ALTER TABLE [dbo].[PhieuKiemKeKho] ADD CONSTRAINT [CK_PKK_TongSoDong] CHECK ([tongSoDong] >= 0)
ALTER TABLE [dbo].[PhieuKiemKeKho] ADD CONSTRAINT [CK_PKK_TrangThai] CHECK ([trangThai] IN (N'HOAN_THANH', N'DA_HUY'))

ALTER TABLE [dbo].[ChiTietPhieuKiemKeKho] ADD CONSTRAINT [FK_CTPKK_PhieuKiemKe]
FOREIGN KEY([phieuKiemKeId]) REFERENCES [dbo].[PhieuKiemKeKho]([id]) ON UPDATE CASCADE ON DELETE CASCADE
ALTER TABLE [dbo].[ChiTietPhieuKiemKeKho] ADD CONSTRAINT [FK_CTPKK_LoHang]
FOREIGN KEY([loHangId]) REFERENCES [dbo].[LoHang]([id])
ALTER TABLE [dbo].[ChiTietPhieuKiemKeKho] ADD CONSTRAINT [FK_CTPKK_SanPham]
FOREIGN KEY([sanPhamId]) REFERENCES [dbo].[SanPham]([id])
ALTER TABLE [dbo].[ChiTietPhieuKiemKeKho] ADD CONSTRAINT [FK_CTPKK_KhoHang]
FOREIGN KEY([khoHangId]) REFERENCES [dbo].[KhoHang]([id])
ALTER TABLE [dbo].[ChiTietPhieuKiemKeKho] ADD CONSTRAINT [CK_CTPKK_TonKho] CHECK ([tonHeThong] >= 0 AND [tonThucTe] >= 0)
ALTER TABLE [dbo].[ChiTietPhieuKiemKeKho] ADD CONSTRAINT [CK_CTPKK_ChenhLech] CHECK ([chenhLech] = [tonThucTe] - [tonHeThong])
GO

CREATE NONCLUSTERED INDEX [IX_PKK_NgayKiemKe]
    ON [dbo].[PhieuKiemKeKho]([ngayKiemKe] DESC, [id] DESC);
GO

CREATE NONCLUSTERED INDEX [IX_CTPKK_PhieuKiemKe]
    ON [dbo].[ChiTietPhieuKiemKeKho]([phieuKiemKeId]);
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

-- ===========================================================================
-- STORED PROCEDURE: sp_BaoCaoTaiChinh
-- Database: MYCAREPHARMACY
-- Mục đích: Báo cáo tài chính chuẩn mực (Doanh thu Gộp → Lợi nhuận Gộp)
--           gộp theo NGÀY hoặc THÁNG, có hỗ trợ lọc nhân viên / khoảng thời gian
--
-- Công thức:
--   (1) Doanh Thu Gộp     = SUM(thanhTien) của BAN_HANG + DOI_HANG(soLuong > 0)
--   (2) Thuế VAT           = SUM( thanhTien × VAT/(100+VAT) ) — bóc VAT từ giá đã có VAT
--                            [Nếu donGiaThucTe là giá CHƯA VAT, dùng: donGiaThucTe×soLuong×VAT/100]
--   (3) Hàng Bán Bị Trả   = SUM(ABS(thanhTien)) của TRA_HANG + DOI_HANG(soLuong < 0)
--   (4) Doanh Thu Thuần   = (1) − (3) − (2)
--   (5) COGS              = Σ(pbl.soLuong × lh.gia) cho BAN_HANG/DOI_HANG(+)
--                           − Σ(pbl.soLuong × lh.gia) cho TRA_HANG/DOI_HANG(−)
--                           [Dùng subquery GROUP BY trước khi JOIN để tránh nhân bản dòng]
--   (6) Lợi Nhuận Gộp    = (4) − (5)
--
-- Parameters:
--   @groupBy   NVARCHAR(10): 'NGAY' hoặc 'THANG'
--   @tuNgay    DATE: Từ ngày (NULL = không giới hạn)
--   @denNgay   DATE: Đến ngày (NULL = không giới hạn)
--   @maNV      NVARCHAR(50): Lọc theo nhân viên (NULL = tất cả)
--
-- Trả về: thoiGian | doanhThuGop | thueVAT | hangBanBiTraLai |
--         doanhThuThuan | giaVonHangBan | loiNhuanGop
-- ===========================================================================
USE [MYCAREPHARMACY];
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
