USE master;
GO

-- ==============================================================================
-- PHẦN 1: TẠO MỚI DATABASE VÀ CÁC BẢNG (SCRIPT GỐC)
-- ==============================================================================

-- 1. Ép ngắt kết nối và Xóa DB cũ nếu tồn tại
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'MYCAREPHARMACY')
BEGIN
    ALTER DATABASE MYCAREPHARMACY SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE MYCAREPHARMACY;
END
GO
-- 2. Tạo DB mới
CREATE DATABASE MYCAREPHARMACY;
GO
ALTER DATABASE [MYCAREPHARMACY] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [MYCAREPHARMACY] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [MYCAREPHARMACY] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [MYCAREPHARMACY] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [MYCAREPHARMACY] SET ARITHABORT OFF 
GO
ALTER DATABASE [MYCAREPHARMACY] SET AUTO_CLOSE ON 
GO
ALTER DATABASE [MYCAREPHARMACY] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [MYCAREPHARMACY] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [MYCAREPHARMACY] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [MYCAREPHARMACY] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [MYCAREPHARMACY] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [MYCAREPHARMACY] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [MYCAREPHARMACY] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [MYCAREPHARMACY] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [MYCAREPHARMACY] SET  ENABLE_BROKER 
GO
ALTER DATABASE [MYCAREPHARMACY] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [MYCAREPHARMACY] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [MYCAREPHARMACY] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [MYCAREPHARMACY] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [MYCAREPHARMACY] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [MYCAREPHARMACY] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [MYCAREPHARMACY] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [MYCAREPHARMACY] SET RECOVERY SIMPLE 
GO
ALTER DATABASE [MYCAREPHARMACY] SET  MULTI_USER 
GO
ALTER DATABASE [MYCAREPHARMACY] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [MYCAREPHARMACY] SET DB_CHAINING OFF 
GO
ALTER DATABASE [MYCAREPHARMACY] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [MYCAREPHARMACY] SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE [MYCAREPHARMACY] SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE [MYCAREPHARMACY] SET ACCELERATED_DATABASE_RECOVERY = OFF  
GO
ALTER DATABASE [MYCAREPHARMACY] SET QUERY_STORE = ON
GO
ALTER DATABASE [MYCAREPHARMACY] SET QUERY_STORE (OPERATION_MODE = READ_WRITE, CLEANUP_POLICY = (STALE_QUERY_THRESHOLD_DAYS = 30), DATA_FLUSH_INTERVAL_SECONDS = 900, INTERVAL_LENGTH_MINUTES = 60, MAX_STORAGE_SIZE_MB = 1000, QUERY_CAPTURE_MODE = AUTO, SIZE_BASED_CLEANUP_MODE = AUTO, MAX_PLANS_PER_QUERY = 200, WAIT_STATS_CAPTURE_MODE = ON)
GO
USE [MYCAREPHARMACY]
GO
/****** Object:  Table [dbo].[ApDungKhuyenMai]    Script Date: 12/04/2026 4:24:56 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ApDungKhuyenMai](
	[id] [nvarchar](50) NOT NULL,
	[khuyenMaiId] [nvarchar](50) NOT NULL,
	[sanPhamId] [nvarchar](50) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[CaLamViec]    Script Date: 12/04/2026 4:24:56 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[CaLamViec](
	[id] [nvarchar](50) NOT NULL,
	[nhanVienId] [nvarchar](50) NOT NULL,
	[thoiGianBatDau] [datetime2](7) NOT NULL,
	[thoiGianKetThuc] [datetime2](7) NULL,
	[tienHeThongGhiNhan] [decimal](18, 2) NOT NULL,
	[tienDauCa] [decimal](18, 2) NOT NULL,
	[tienKetCa] [decimal](18, 2) NOT NULL,
    -- Thêm các cột mới tại đây:
    [loaiCa] [int] DEFAULT 0,
    [ghiChuKetCa] [nvarchar](max) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ChiTietHoaDon]    Script Date: 12/04/2026 4:24:56 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ChiTietHoaDon](
	[hoaDonId] [nvarchar](50) NOT NULL,
	[donViDoLuongId] [nvarchar](50) NOT NULL,
	[sanPhamId] [nvarchar](50) NOT NULL,
	[soLuong] [int] NOT NULL,
 CONSTRAINT [PK_ChiTietHoaDon] PRIMARY KEY CLUSTERED 
(
	[hoaDonId] ASC,
	[donViDoLuongId] ASC,
	[sanPhamId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[DieuKienKhuyenMai]    Script Date: 12/04/2026 4:24:56 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[DieuKienKhuyenMai](
	[id] [nvarchar](50) NOT NULL,
	[loaiDieuKien] [nvarchar](20) NOT NULL,
	[doiTuongApDung] [nvarchar](20) NOT NULL,
	[giaTri] [decimal](18, 2) NOT NULL,
	[khuyenMaiId] [nvarchar](50) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[DonViDoLuong]    Script Date: 12/04/2026 4:24:56 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[DonViDoLuong](
	[id] [nvarchar](50) NOT NULL,
	[sanPhamId] [nvarchar](50) NOT NULL,
	[ten] [nvarchar](100) NOT NULL,
	[chuyenDoiDonViCoBan] [decimal](18, 2) NOT NULL,
	[gia] [decimal](18, 2) NOT NULL,
 CONSTRAINT [PK_DonViDoLuong] PRIMARY KEY CLUSTERED 
(
	[id] ASC,
	[sanPhamId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[HinhThucKhuyenMai]    Script Date: 12/04/2026 4:24:56 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[HinhThucKhuyenMai](
	[id] [nvarchar](50) NOT NULL,
	[loaiHinhThuc] [nvarchar](30) NOT NULL,
	[doiTuongApDung] [nvarchar](20) NOT NULL,
	[moTa] [nvarchar](max) NULL,
	[giaTri] [decimal](18, 2) NOT NULL,
	[donViDoLuongId] [nvarchar](50) NULL,
	[sanPhamId] [nvarchar](50) NULL,
	[khuyenMaiId] [nvarchar](50) NOT NULL,
	[giamToiDa] [decimal](18, 2) NULL,
    -- Thêm các cột mới tại đây:
    [spYeuCau] [nvarchar](255) NULL,
    [slYeuCau] [int] NULL,
    [spTang] [nvarchar](255) NULL,
    [slTang] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[HoaDon]    Script Date: 12/04/2026 4:24:56 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[HoaDon](
	[id] [nvarchar](50) NOT NULL,
	[loaiHD] [nvarchar](20) NOT NULL,
	[ghiChu] [nvarchar](max) NULL,
	[ngayLapHD] [datetime2](7) NOT NULL,
	[nhanVienId] [nvarchar](50) NOT NULL,
	[khachHangId] [nvarchar](50) NULL,
	[khuyenMaiId] [nvarchar](50) NULL,
	[phuongThucThanhToan] [nvarchar](40) NOT NULL,
	[hoaDonGocId] [nvarchar](50) NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[KhachHang]    Script Date: 12/04/2026 4:24:56 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[KhachHang](
	[id] [nvarchar](50) NOT NULL,
	[sdt] [nvarchar](15) NULL,
	[hoVaTen] [nvarchar](100) NOT NULL,
	[ngayTao] [datetime2](7) NOT NULL,
	[diemTichLuy] [int] NOT NULL DEFAULT 0,
    -- BỔ SUNG CÁC CỘT THƯỜNG THIẾU DƯỚI ĐÂY:
	[gioiTinh] [nvarchar](10) NULL,
	[ngaySinh] [date] NULL,
	[diaChi] [nvarchar](255) NULL,
	[email] [nvarchar](100) NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[KhoHang]    Script Date: 12/04/2026 4:24:56 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[KhoHang](
	[id] [nvarchar](50) NOT NULL,
	[sucChua] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[KhuyenMai]    Script Date: 12/04/2026 4:24:56 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[KhuyenMai](
	[id] [nvarchar](50) NOT NULL,
	[tenKhuyenMai] [nvarchar](150) NOT NULL,
	[moTa] [nvarchar](max) NULL,
	[ngayTao] [datetime2](7) NOT NULL,
	[ngayBatDau] [datetime2](7) NOT NULL,
	[ngayKetThuc] [datetime2](7) NULL,
    -- Thêm cột mới tại đây:
    [trangThai] [bit] DEFAULT 1,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[LoHang]    Script Date: 12/04/2026 4:24:56 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[LoHang](
	[id] [nvarchar](50) NOT NULL,
	[soLoHang] [nvarchar](100) NOT NULL,
	[sanPhamId] [nvarchar](50) NOT NULL,
	[soLuongLoHang] [int] NOT NULL,
	[gia] [decimal](18, 2) NOT NULL,
	[ngayHetHan] [datetime2](7) NULL,
	[trangThai] [nvarchar](20) NOT NULL,
	[ngayNhap] [datetime2](7) NOT NULL,
	[khoHangId] [nvarchar](50) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[NhanVien]    Script Date: 12/04/2026 4:24:56 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[NhanVien](
	[id] [nvarchar](50) NOT NULL,
	[hoVaTen] [nvarchar](100) NOT NULL,
	[soChungChiHanhNghe] [nvarchar](50) NULL,
	[sdt] [nvarchar](15) NULL,
	[email] [nvarchar](100) NULL,
	[chucVu] [nvarchar](20) NOT NULL,
	[trangThaiLamViec] [nvarchar](20) NOT NULL,
    -- BỔ SUNG CÁC CỘT THƯỜNG THIẾU DƯỚI ĐÂY:
	[gioiTinh] [nvarchar](10) NULL,
	[ngaySinh] [date] NULL,
	[diaChi] [nvarchar](255) NULL,
	[cccd] [nvarchar](20) NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[PhanBoLoHang]    Script Date: 12/04/2026 4:24:56 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PhanBoLoHang](
	[hoaDonId] [nvarchar](50) NOT NULL,
	[donViDoLuongId] [nvarchar](50) NOT NULL,
	[sanPhamId] [nvarchar](50) NOT NULL,
	[loHangId] [nvarchar](50) NOT NULL,
	[soLuong] [int] NOT NULL,
 CONSTRAINT [PK_PhanBoLoHang] PRIMARY KEY CLUSTERED 
(
	[hoaDonId] ASC,
	[donViDoLuongId] ASC,
	[sanPhamId] ASC,
	[loHangId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[SanPham]    Script Date: 12/04/2026 4:24:56 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[SanPham](
	[id] [nvarchar](50) NOT NULL,
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
	[trangThai] [nvarchar](20) NULL DEFAULT 'HOAT_DONG', -- <--- BẠN THÊM DÒNG NÀY VÀO ĐÂY
	[giaBan]    [decimal](18, 2) NOT NULL DEFAULT 0,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[TaiKhoan]    Script Date: 12/04/2026 4:24:56 CH ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TaiKhoan](
	[id] [nvarchar](50) NOT NULL,
	[nhanVienId] [nvarchar](50) NULL,
	[vaiTro] [nvarchar](20) NOT NULL,
	[tenDangNhap] [nvarchar](50) NOT NULL,
	[matKhau] [nvarchar](255) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

-- ==============================================================================
-- PHẦN 2: CHÈN DỮ LIỆU MẪU
-- ==============================================================================

INSERT [dbo].[CaLamViec] ([id], [nhanVienId], [thoiGianBatDau], [thoiGianKetThuc], [tienHeThongGhiNhan], [tienDauCa], [tienKetCa], [loaiCa], [ghiChuKetCa]) VALUES (N'Ca-0001', N'DS-0001', CAST(N'2024-03-01T07:30:00.0000000' AS DateTime2), CAST(N'2024-03-01T17:30:00.0000000' AS DateTime2), CAST(2500000.00 AS Decimal(18, 2)), CAST(500000.00 AS Decimal(18, 2)), CAST(3000000.00 AS Decimal(18, 2)), 1, N'Bình thường')
INSERT [dbo].[CaLamViec] ([id], [nhanVienId], [thoiGianBatDau], [thoiGianKetThuc], [tienHeThongGhiNhan], [tienDauCa], [tienKetCa], [loaiCa], [ghiChuKetCa]) VALUES (N'Ca-0002', N'DS-0002', CAST(N'2024-03-01T07:30:00.0000000' AS DateTime2), CAST(N'2024-03-01T17:30:00.0000000' AS DateTime2), CAST(1800000.00 AS Decimal(18, 2)), CAST(500000.00 AS Decimal(18, 2)), CAST(2300000.00 AS Decimal(18, 2)), 1, N'Bình thường')
INSERT [dbo].[CaLamViec] ([id], [nhanVienId], [thoiGianBatDau], [thoiGianKetThuc], [tienHeThongGhiNhan], [tienDauCa], [tienKetCa], [loaiCa], [ghiChuKetCa]) VALUES (N'Ca-0003', N'DS-0003', CAST(N'2024-03-02T07:30:00.0000000' AS DateTime2), CAST(N'2024-03-02T17:30:00.0000000' AS DateTime2), CAST(3200000.00 AS Decimal(18, 2)), CAST(500000.00 AS Decimal(18, 2)), CAST(3700000.00 AS Decimal(18, 2)), 1, N'Bình thường')
INSERT [dbo].[CaLamViec] ([id], [nhanVienId], [thoiGianBatDau], [thoiGianKetThuc], [tienHeThongGhiNhan], [tienDauCa], [tienKetCa], [loaiCa], [ghiChuKetCa]) VALUES (N'Ca-0004', N'DS-0004', CAST(N'2024-03-02T07:30:00.0000000' AS DateTime2), CAST(N'2024-03-02T17:30:00.0000000' AS DateTime2), CAST(2100000.00 AS Decimal(18, 2)), CAST(500000.00 AS Decimal(18, 2)), CAST(2600000.00 AS Decimal(18, 2)), 1, N'Bình thường')
INSERT [dbo].[CaLamViec] ([id], [nhanVienId], [thoiGianBatDau], [thoiGianKetThuc], [tienHeThongGhiNhan], [tienDauCa], [tienKetCa], [loaiCa], [ghiChuKetCa]) VALUES (N'Ca-0005', N'DS-0005', CAST(N'2024-03-03T07:30:00.0000000' AS DateTime2), CAST(N'2024-03-03T17:30:00.0000000' AS DateTime2), CAST(1500000.00 AS Decimal(18, 2)), CAST(500000.00 AS Decimal(18, 2)), CAST(2000000.00 AS Decimal(18, 2)), 1, N'Bình thường')
GO
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0001', N'DVL-0008', N'SP2024-0004', 2)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0001', N'DVL-0030', N'SP2024-0016', 1)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0002', N'DVL-0039', N'SP2024-0021', 1)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0002', N'DVL-0041', N'SP2024-0022', 1)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0003', N'DVL-0016', N'SP2024-0009', 2)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0003', N'DVL-0023', N'SP2024-0012', 1)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0004', N'DVL-0002', N'SP2024-0001', 1)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0004', N'DVL-0028', N'SP2024-0015', 1)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0005', N'DVL-0052', N'SP2024-0028', 1)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0005', N'DVL-0053', N'SP2024-0029', 1)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0006', N'DVL-0025', N'SP2024-0013', 1)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0006', N'DVL-0033', N'SP2024-0018', 1)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0007', N'DVL-0043', N'SP2024-0023', 2)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0007', N'DVL-0047', N'SP2024-0025', 1)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0008', N'DVL-0021', N'SP2024-0011', 1)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0008', N'DVL-0044', N'SP2024-0024', 5)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0009', N'DVL-0030', N'SP2024-0016', 2)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0009', N'DVL-0031', N'SP2024-0017', 1)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0010', N'DVL-0006', N'SP2024-0003', 1)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0010', N'DVL-0008', N'SP2024-0004', 1)
INSERT [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId], [soLuong]) VALUES (N'HD2024-0010', N'DVL-0019', N'SP2024-0010', 1)
GO
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0001', N'SP2024-0001', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(2000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0002', N'SP2024-0001', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(55000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0003', N'SP2024-0002', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(5000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0004', N'SP2024-0002', N'Hộp', CAST(14.00 AS Decimal(18, 2)), CAST(65000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0005', N'SP2024-0003', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(800.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0006', N'SP2024-0003', N'Hộp', CAST(100.00 AS Decimal(18, 2)), CAST(75000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0007', N'SP2024-0004', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(1500.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0008', N'SP2024-0004', N'Hộp', CAST(28.00 AS Decimal(18, 2)), CAST(40000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0009', N'SP2024-0005', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(90000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0010', N'SP2024-0006', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(75000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0011', N'SP2024-0007', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(2500.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0012', N'SP2024-0007', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(70000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0013', N'SP2024-0008', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(3500.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0014', N'SP2024-0008', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(98000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0015', N'SP2024-0009', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(500.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0016', N'SP2024-0009', N'Vỉ', CAST(10.00 AS Decimal(18, 2)), CAST(4500.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0017', N'SP2024-0009', N'Hộp', CAST(100.00 AS Decimal(18, 2)), CAST(40000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0018', N'SP2024-0010', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(1000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0019', N'SP2024-0010', N'Hộp', CAST(20.00 AS Decimal(18, 2)), CAST(18000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0020', N'SP2024-0011', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(3500.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0021', N'SP2024-0011', N'Hộp', CAST(20.00 AS Decimal(18, 2)), CAST(65000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0022', N'SP2024-0012', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(2000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0023', N'SP2024-0012', N'Hộp', CAST(10.00 AS Decimal(18, 2)), CAST(18000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0024', N'SP2024-0013', N'Gói', CAST(1.00 AS Decimal(18, 2)), CAST(5000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0025', N'SP2024-0013', N'Hộp', CAST(20.00 AS Decimal(18, 2)), CAST(90000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0026', N'SP2024-0014', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(35000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0027', N'SP2024-0015', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(1500.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0028', N'SP2024-0015', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(40000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0029', N'SP2024-0016', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(2000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0030', N'SP2024-0016', N'Hộp', CAST(28.00 AS Decimal(18, 2)), CAST(52000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0031', N'SP2024-0017', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(55000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0032', N'SP2024-0018', N'Gói', CAST(1.00 AS Decimal(18, 2)), CAST(8000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0033', N'SP2024-0018', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(220000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0034', N'SP2024-0019', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(1800.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0035', N'SP2024-0019', N'Hộp', CAST(10.00 AS Decimal(18, 2)), CAST(16000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0036', N'SP2024-0020', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(4000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0037', N'SP2024-0020', N'Hộp', CAST(14.00 AS Decimal(18, 2)), CAST(52000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0038', N'SP2024-0021', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(4000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0039', N'SP2024-0021', N'Hộp', CAST(60.00 AS Decimal(18, 2)), CAST(220000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0040', N'SP2024-0022', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(3000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0041', N'SP2024-0022', N'Hộp', CAST(100.00 AS Decimal(18, 2)), CAST(280000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0042', N'SP2024-0023', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(1500.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0043', N'SP2024-0023', N'Hộp', CAST(60.00 AS Decimal(18, 2)), CAST(85000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0044', N'SP2024-0024', N'Gói', CAST(1.00 AS Decimal(18, 2)), CAST(15000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0045', N'SP2024-0024', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(420000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0046', N'SP2024-0025', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(5000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0047', N'SP2024-0025', N'Hộp', CAST(60.00 AS Decimal(18, 2)), CAST(280000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0048', N'SP2024-0026', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(3500.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0049', N'SP2024-0026', N'Hộp', CAST(60.00 AS Decimal(18, 2)), CAST(195000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0050', N'SP2024-0027', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(6000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0051', N'SP2024-0027', N'Hộp', CAST(30.00 AS Decimal(18, 2)), CAST(165000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0052', N'SP2024-0028', N'Hộp', CAST(1.00 AS Decimal(18, 2)), CAST(650000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0053', N'SP2024-0029', N'Tuýp', CAST(1.00 AS Decimal(18, 2)), CAST(580000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0054', N'SP2024-0030', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(320000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0055', N'SP2024-0031', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(280000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0056', N'SP2024-0032', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(850000.00 AS Decimal(18, 2)))
-- ================== BỔ SUNG ĐƠN VỊ ĐO LƯỜNG CHO 8 SẢN PHẨM MỚI ==================
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0057', N'SP2024-0033', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(52000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0058', N'SP2024-0034', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(22000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0059', N'SP2024-0035', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(115000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0060', N'SP2024-0036', N'Chai', CAST(1.00 AS Decimal(18, 2)), CAST(68000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0061', N'SP2024-0037', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(3500.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0062', N'SP2024-0037', N'Hộp', CAST(24.00 AS Decimal(18, 2)), CAST(80000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0063', N'SP2024-0038', N'Viên', CAST(1.00 AS Decimal(18, 2)), CAST(4000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0064', N'SP2024-0038', N'Hộp', CAST(16.00 AS Decimal(18, 2)), CAST(62000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0065', N'SP2024-0039', N'Gói', CAST(1.00 AS Decimal(18, 2)), CAST(3000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0066', N'SP2024-0039', N'Hộp', CAST(24.00 AS Decimal(18, 2)), CAST(68000.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0067', N'SP2024-0040', N'Gói', CAST(1.00 AS Decimal(18, 2)), CAST(4500.00 AS Decimal(18, 2)))
INSERT [dbo].[DonViDoLuong] ([id], [sanPhamId], [ten], [chuyenDoiDonViCoBan], [gia]) VALUES (N'DVL-0068', N'SP2024-0040', N'Hộp', CAST(26.00 AS Decimal(18, 2)), CAST(110000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[HoaDon] ([id], [loaiHD], [ghiChu], [ngayLapHD], [nhanVienId], [khachHangId], [khuyenMaiId], [phuongThucThanhToan], [hoaDonGocId]) VALUES (N'HD2024-0001', N'BAN_HANG', N'Khách quen, mua thuốc huyết áp định kỳ', CAST(N'2024-03-01T09:15:00.0000000' AS DateTime2), N'DS-0001', N'KH-0001', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] ([id], [loaiHD], [ghiChu], [ngayLapHD], [nhanVienId], [khachHangId], [khuyenMaiId], [phuongThucThanhToan], [hoaDonGocId]) VALUES (N'HD2024-0002', N'BAN_HANG', N'Mua vitamin tổng hợp', CAST(N'2024-03-01T10:30:00.0000000' AS DateTime2), N'DS-0001', N'KH-0002', NULL, N'CHUYEN_KHOAN_NGAN_HANG', NULL)
INSERT [dbo].[HoaDon] ([id], [loaiHD], [ghiChu], [ngayLapHD], [nhanVienId], [khachHangId], [khuyenMaiId], [phuongThucThanhToan], [hoaDonGocId]) VALUES (N'HD2024-0003', N'BAN_HANG', NULL, CAST(N'2024-03-01T11:00:00.0000000' AS DateTime2), N'DS-0002', N'KH-0003', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] ([id], [loaiHD], [ghiChu], [ngayLapHD], [nhanVienId], [khachHangId], [khuyenMaiId], [phuongThucThanhToan], [hoaDonGocId]) VALUES (N'HD2024-0004', N'BAN_HANG', N'Mua thuốc kháng sinh theo toa bác sĩ', CAST(N'2024-03-02T08:45:00.0000000' AS DateTime2), N'DS-0003', N'KH-0004', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] ([id], [loaiHD], [ghiChu], [ngayLapHD], [nhanVienId], [khachHangId], [khuyenMaiId], [phuongThucThanhToan], [hoaDonGocId]) VALUES (N'HD2024-0005', N'BAN_HANG', N'Mua sản phẩm chăm sóc da', CAST(N'2024-03-02T14:00:00.0000000' AS DateTime2), N'DS-0003', N'KH-0005', NULL, N'CHUYEN_KHOAN_NGAN_HANG', NULL)
INSERT [dbo].[HoaDon] ([id], [loaiHD], [ghiChu], [ngayLapHD], [nhanVienId], [khachHangId], [khuyenMaiId], [phuongThucThanhToan], [hoaDonGocId]) VALUES (N'HD2024-0006', N'BAN_HANG', NULL, CAST(N'2024-03-02T15:30:00.0000000' AS DateTime2), N'DS-0003', NULL, NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] ([id], [loaiHD], [ghiChu], [ngayLapHD], [nhanVienId], [khachHangId], [khuyenMaiId], [phuongThucThanhToan], [hoaDonGocId]) VALUES (N'HD2024-0007', N'BAN_HANG', N'Mua bổ sung dinh dưỡng', CAST(N'2024-03-03T09:00:00.0000000' AS DateTime2), N'DS-0004', N'KH-0006', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] ([id], [loaiHD], [ghiChu], [ngayLapHD], [nhanVienId], [khachHangId], [khuyenMaiId], [phuongThucThanhToan], [hoaDonGocId]) VALUES (N'HD2024-0008', N'BAN_HANG', N'Khách mua lẻ', CAST(N'2024-03-03T10:15:00.0000000' AS DateTime2), N'DS-0005', N'KH-0007', NULL, N'TIEN_MAT', NULL)
INSERT [dbo].[HoaDon] ([id], [loaiHD], [ghiChu], [ngayLapHD], [nhanVienId], [khachHangId], [khuyenMaiId], [phuongThucThanhToan], [hoaDonGocId]) VALUES (N'HD2024-0009', N'BAN_HANG', N'Mua thuốc điều trị dạ dày', CAST(N'2024-03-03T11:30:00.0000000' AS DateTime2), N'DS-0005', N'KH-0008', NULL, N'CHUYEN_KHOAN_NGAN_HANG', NULL)
INSERT [dbo].[HoaDon] ([id], [loaiHD], [ghiChu], [ngayLapHD], [nhanVienId], [khachHangId], [khuyenMaiId], [phuongThucThanhToan], [hoaDonGocId]) VALUES (N'HD2024-0010', N'BAN_HANG', N'Mua đầy đủ thuốc tháng này', CAST(N'2024-03-03T13:45:00.0000000' AS DateTime2), N'DS-0004', N'KH-0009', NULL, N'TIEN_MAT', NULL)
GO
INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) VALUES (N'KH-0001', N'0311223344', N'Nguyễn Thị Lan', CAST(N'2024-01-10T08:00:00.0000000' AS DateTime2), 0)
INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) VALUES (N'KH-0002', N'0322334455', N'Trần Văn Bình', CAST(N'2024-02-15T09:30:00.0000000' AS DateTime2), 0)
INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) VALUES (N'KH-0003', N'0333445566', N'Lê Thị Hoa', CAST(N'2024-03-20T10:00:00.0000000' AS DateTime2), 0)
INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) VALUES (N'KH-0004', N'0344556677', N'Phạm Thanh Tùng', CAST(N'2024-04-05T11:00:00.0000000' AS DateTime2), 0)
INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) VALUES (N'KH-0005', N'0355667788', N'Võ Thị Mai', CAST(N'2024-05-12T13:00:00.0000000' AS DateTime2), 0)
INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) VALUES (N'KH-0006', N'0366778899', N'Đặng Minh Khoa', CAST(N'2024-06-18T14:30:00.0000000' AS DateTime2), 0)
INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) VALUES (N'KH-0007', N'0377889900', N'Hoàng Thị Thu', CAST(N'2024-07-22T08:45:00.0000000' AS DateTime2), 0)
INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) VALUES (N'KH-0008', N'0388990011', N'Bùi Văn Long', CAST(N'2024-08-30T09:15:00.0000000' AS DateTime2), 0)
INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) VALUES (N'KH-0009', N'0399001122', N'Ngô Thị Thanh', CAST(N'2024-09-14T10:30:00.0000000' AS DateTime2), 0)
INSERT [dbo].[KhachHang] ([id], [sdt], [hoVaTen], [ngayTao], [diemTichLuy]) VALUES (N'KH-0010', N'0310112233', N'Dương Quốc Hùng', CAST(N'2024-10-25T15:00:00.0000000' AS DateTime2), 0)
GO
INSERT [dbo].[KhoHang] ([id], [sucChua]) VALUES (N'KHO-0001', 5000)
INSERT [dbo].[KhoHang] ([id], [sucChua]) VALUES (N'KHO-0002', 3000)
GO
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0001', N'LOT-2024-0001', N'SP2024-0001', 500, CAST(1800.00 AS Decimal(18, 2)), CAST(N'2026-06-30T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-01-05T08:00:00.0000000' AS DateTime2), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0002', N'LOT-2024-0002', N'SP2024-0002', 200, CAST(4500.00 AS Decimal(18, 2)), CAST(N'2026-08-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-01-05T08:00:00.0000000' AS DateTime2), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0003', N'LOT-2024-0003', N'SP2024-0003', 1000, CAST(700.00 AS Decimal(18, 2)), CAST(N'2026-12-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-01-08T09:00:00.0000000' AS DateTime2), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0004', N'LOT-2024-0004', N'SP2024-0004', 800, CAST(1300.00 AS Decimal(18, 2)), CAST(N'2026-10-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-01-08T09:00:00.0000000' AS DateTime2), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0005', N'LOT-2024-0005', N'SP2024-0005', 150, CAST(80000.00 AS Decimal(18, 2)), CAST(N'2025-12-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-01-10T10:00:00.0000000' AS DateTime2), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0006', N'LOT-2024-0006', N'SP2024-0006', 100, CAST(65000.00 AS Decimal(18, 2)), CAST(N'2025-11-30T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-01-10T10:00:00.0000000' AS DateTime2), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0007', N'LOT-2024-0007', N'SP2024-0007', 400, CAST(2200.00 AS Decimal(18, 2)), CAST(N'2027-01-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-01-12T08:00:00.0000000' AS DateTime2), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0008', N'LOT-2024-0008', N'SP2024-0008', 300, CAST(3000.00 AS Decimal(18, 2)), CAST(N'2027-02-28T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-01-12T08:00:00.0000000' AS DateTime2), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0009', N'LOT-2024-0009', N'SP2024-0009', 2000, CAST(400.00 AS Decimal(18, 2)), CAST(N'2027-06-30T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-01-12T08:00:00.0000000' AS DateTime2), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0010', N'LOT-2024-0010', N'SP2024-0010', 1000, CAST(850.00 AS Decimal(18, 2)), CAST(N'2027-03-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-01-12T08:00:00.0000000' AS DateTime2), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0011', N'LOT-2024-0011', N'SP2024-0011', 500, CAST(3000.00 AS Decimal(18, 2)), CAST(N'2026-09-30T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-01-15T09:00:00.0000000' AS DateTime2), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0012', N'LOT-2024-0012', N'SP2024-0012', 600, CAST(1700.00 AS Decimal(18, 2)), CAST(N'2026-07-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-01-15T09:00:00.0000000' AS DateTime2), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0013', N'LOT-2024-0013', N'SP2024-0013', 400, CAST(4000.00 AS Decimal(18, 2)), CAST(N'2026-05-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-01-18T10:00:00.0000000' AS DateTime2), N'KHO-0002')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0014', N'LOT-2024-0014', N'SP2024-0014', 200, CAST(28000.00 AS Decimal(18, 2)), CAST(N'2026-04-30T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-01-18T10:00:00.0000000' AS DateTime2), N'KHO-0002')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0015', N'LOT-2024-0015', N'SP2024-0015', 500, CAST(1200.00 AS Decimal(18, 2)), CAST(N'2026-11-30T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-01-20T08:00:00.0000000' AS DateTime2), N'KHO-0002')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0016', N'LOT-2024-0016', N'SP2024-0016', 700, CAST(1700.00 AS Decimal(18, 2)), CAST(N'2026-08-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-01-20T08:00:00.0000000' AS DateTime2), N'KHO-0002')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0017', N'LOT-2024-0017', N'SP2024-0017', 200, CAST(48000.00 AS Decimal(18, 2)), CAST(N'2025-10-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-01-22T09:00:00.0000000' AS DateTime2), N'KHO-0002')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0018', N'LOT-2024-0018', N'SP2024-0018', 800, CAST(6500.00 AS Decimal(18, 2)), CAST(N'2026-09-30T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-02-01T08:00:00.0000000' AS DateTime2), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0019', N'LOT-2024-0019', N'SP2024-0019', 500, CAST(1500.00 AS Decimal(18, 2)), CAST(N'2026-10-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-02-01T08:00:00.0000000' AS DateTime2), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0020', N'LOT-2024-0020', N'SP2024-0020', 400, CAST(3500.00 AS Decimal(18, 2)), CAST(N'2026-11-30T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-02-01T08:00:00.0000000' AS DateTime2), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0021', N'LOT-2024-0021', N'SP2024-0021', 300, CAST(3500.00 AS Decimal(18, 2)), CAST(N'2027-01-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-02-05T09:00:00.0000000' AS DateTime2), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0022', N'LOT-2024-0022', N'SP2024-0022', 400, CAST(2500.00 AS Decimal(18, 2)), CAST(N'2027-02-28T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-02-05T09:00:00.0000000' AS DateTime2), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0023', N'LOT-2024-0023', N'SP2024-0023', 600, CAST(1200.00 AS Decimal(18, 2)), CAST(N'2026-12-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-02-08T10:00:00.0000000' AS DateTime2), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0024', N'LOT-2024-0024', N'SP2024-0024', 200, CAST(13000.00 AS Decimal(18, 2)), CAST(N'2026-06-30T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-02-08T10:00:00.0000000' AS DateTime2), N'KHO-0002')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0025', N'LOT-2024-0025', N'SP2024-0025', 300, CAST(4500.00 AS Decimal(18, 2)), CAST(N'2026-10-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-02-10T08:00:00.0000000' AS DateTime2), N'KHO-0002')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0026', N'LOT-2024-0026', N'SP2024-0026', 400, CAST(3000.00 AS Decimal(18, 2)), CAST(N'2027-03-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-02-10T08:00:00.0000000' AS DateTime2), N'KHO-0002')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0027', N'LOT-2024-0027', N'SP2024-0027', 300, CAST(5000.00 AS Decimal(18, 2)), CAST(N'2026-12-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-02-12T09:00:00.0000000' AS DateTime2), N'KHO-0002')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0028', N'LOT-2024-0028', N'SP2024-0028', 50, CAST(580000.00 AS Decimal(18, 2)), CAST(N'2027-06-30T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-02-12T09:00:00.0000000' AS DateTime2), N'KHO-0002')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0029', N'LOT-2024-0029', N'SP2024-0029', 80, CAST(510000.00 AS Decimal(18, 2)), CAST(N'2027-04-30T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-02-15T08:00:00.0000000' AS DateTime2), N'KHO-0002')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0030', N'LOT-2024-0030', N'SP2024-0030', 100, CAST(270000.00 AS Decimal(18, 2)), CAST(N'2026-12-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-02-15T08:00:00.0000000' AS DateTime2), N'KHO-0002')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0031', N'LOT-2024-0031', N'SP2024-0031', 120, CAST(230000.00 AS Decimal(18, 2)), CAST(N'2026-11-30T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-02-18T09:00:00.0000000' AS DateTime2), N'KHO-0002')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0032', N'LOT-2024-0032', N'SP2024-0032', 60, CAST(720000.00 AS Decimal(18, 2)), CAST(N'2027-05-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', CAST(N'2024-02-18T09:00:00.0000000' AS DateTime2), N'KHO-0002')
-- ================== BỔ SUNG LÔ HÀNG CHO 8 SẢN PHẨM MỚI ==================
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0033', N'LOT-2024-0033', N'SP2024-0033', 100, CAST(35000.00 AS Decimal(18, 2)), CAST(N'2026-12-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', GETDATE(), N'KHO-0002')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0034', N'LOT-2024-0034', N'SP2024-0034', 150, CAST(15000.00 AS Decimal(18, 2)), CAST(N'2027-01-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', GETDATE(), N'KHO-0002')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0035', N'LOT-2024-0035', N'SP2024-0035', 80, CAST(85000.00 AS Decimal(18, 2)), CAST(N'2027-05-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', GETDATE(), N'KHO-0002')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0036', N'LOT-2024-0036', N'SP2024-0036', 50, CAST(45000.00 AS Decimal(18, 2)), CAST(N'2026-11-30T00:00:00.0000000' AS DateTime2), N'CON_HANG', GETDATE(), N'KHO-0002')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0037', N'LOT-2024-0037', N'SP2024-0037', 500, CAST(2000.00 AS Decimal(18, 2)), CAST(N'2026-10-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', GETDATE(), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0038', N'LOT-2024-0038', N'SP2024-0038', 300, CAST(2500.00 AS Decimal(18, 2)), CAST(N'2027-08-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', GETDATE(), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0039', N'LOT-2024-0039', N'SP2024-0039', 600, CAST(1500.00 AS Decimal(18, 2)), CAST(N'2026-09-30T00:00:00.0000000' AS DateTime2), N'CON_HANG', GETDATE(), N'KHO-0001')
INSERT [dbo].[LoHang] ([id], [soLoHang], [sanPhamId], [soLuongLoHang], [gia], [ngayHetHan], [trangThai], [ngayNhap], [khoHangId]) VALUES (N'LH-0040', N'LOT-2024-0040', N'SP2024-0040', 250, CAST(3000.00 AS Decimal(18, 2)), CAST(N'2026-12-31T00:00:00.0000000' AS DateTime2), N'CON_HANG', GETDATE(), N'KHO-0001')
GO
INSERT [dbo].[NhanVien] ([id], [hoVaTen], [soChungChiHanhNghe], [sdt], [email], [chucVu], [trangThaiLamViec], [gioiTinh], [ngaySinh], [diaChi], [cccd]) VALUES (N'DS-0001', N'Nguyễn Tuấn Đạt', N'CCHN-DS-2021-001', N'0912345678', N'dat@mycarepharmacy.vn', N'DUOC_SI', N'DANG_LAM_VIEC', N'Nam', CAST(N'1990-01-01' AS Date), N'TP.HCM', N'079090000001')
INSERT [dbo].[NhanVien] ([id], [hoVaTen], [soChungChiHanhNghe], [sdt], [email], [chucVu], [trangThaiLamViec], [gioiTinh], [ngaySinh], [diaChi], [cccd]) VALUES (N'DS-0002', N'Mai Trung Kiên', N'CCHN-DS-2021-002', N'0923456789', N'kien@mycarepharmacy.vn', N'DUOC_SI', N'DANG_LAM_VIEC', N'Nam', CAST(N'1992-05-10' AS Date), N'TP.HCM', N'079092000002')
INSERT [dbo].[NhanVien] ([id], [hoVaTen], [soChungChiHanhNghe], [sdt], [email], [chucVu], [trangThaiLamViec], [gioiTinh], [ngaySinh], [diaChi], [cccd]) VALUES (N'DS-0003', N'Nguyễn Văn Phương Nam', N'CCHN-DS-2021-003', N'0934567890', N'nam@mycarepharmacy.vn', N'DUOC_SI', N'DANG_LAM_VIEC', N'Nam', CAST(N'1995-08-15' AS Date), N'TP.HCM', N'079095000003')
INSERT [dbo].[NhanVien] ([id], [hoVaTen], [soChungChiHanhNghe], [sdt], [email], [chucVu], [trangThaiLamViec], [gioiTinh], [ngaySinh], [diaChi], [cccd]) VALUES (N'DS-0004', N'Trần Long Thuận', N'CCHN-DS-2022-001', N'0945678901', N'thuan@mycarepharmacy.vn', N'DUOC_SI', N'DANG_LAM_VIEC', N'Nam', CAST(N'1993-11-20' AS Date), N'TP.HCM', N'079093000004')
INSERT [dbo].[NhanVien] ([id], [hoVaTen], [soChungChiHanhNghe], [sdt], [email], [chucVu], [trangThaiLamViec], [gioiTinh], [ngaySinh], [diaChi], [cccd]) VALUES (N'DS-0005', N'Võ Anh Kiệt', N'CCHN-DS-2022-002', N'0956789012', N'kiet@mycarepharmacy.vn', N'DUOC_SI', N'DANG_LAM_VIEC', N'Nam', CAST(N'1996-03-25' AS Date), N'TP.HCM', N'079096000005')
INSERT [dbo].[NhanVien] ([id], [hoVaTen], [soChungChiHanhNghe], [sdt], [email], [chucVu], [trangThaiLamViec], [gioiTinh], [ngaySinh], [diaChi], [cccd]) VALUES (N'QL-0001', N'Nguyễn Quản Lý', N'CCHN-QL-2020-001', N'0901234567', N'admin@mycarepharmacy.vn', N'NGUOI_QUAN_LY', N'DANG_LAM_VIEC', N'Nam', CAST(N'1985-12-12' AS Date), N'TP.HCM', N'079085000006')
GO
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0001', N'DVL-0008', N'SP2024-0004', N'LH-0004', 2)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0001', N'DVL-0030', N'SP2024-0016', N'LH-0016', 1)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0002', N'DVL-0039', N'SP2024-0021', N'LH-0021', 1)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0002', N'DVL-0041', N'SP2024-0022', N'LH-0022', 1)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0003', N'DVL-0016', N'SP2024-0009', N'LH-0009', 2)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0003', N'DVL-0023', N'SP2024-0012', N'LH-0012', 1)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0004', N'DVL-0002', N'SP2024-0001', N'LH-0001', 1)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0004', N'DVL-0028', N'SP2024-0015', N'LH-0015', 1)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0005', N'DVL-0052', N'SP2024-0028', N'LH-0028', 1)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0005', N'DVL-0053', N'SP2024-0029', N'LH-0029', 1)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0006', N'DVL-0025', N'SP2024-0013', N'LH-0013', 1)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0006', N'DVL-0033', N'SP2024-0018', N'LH-0018', 1)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0007', N'DVL-0043', N'SP2024-0023', N'LH-0023', 2)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0007', N'DVL-0047', N'SP2024-0025', N'LH-0025', 1)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0008', N'DVL-0021', N'SP2024-0011', N'LH-0011', 1)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0008', N'DVL-0044', N'SP2024-0024', N'LH-0024', 5)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0009', N'DVL-0030', N'SP2024-0016', N'LH-0016', 2)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0009', N'DVL-0031', N'SP2024-0017', N'LH-0017', 1)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0010', N'DVL-0006', N'SP2024-0003', N'LH-0003', 1)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0010', N'DVL-0008', N'SP2024-0004', N'LH-0004', 1)
INSERT [dbo].[PhanBoLoHang] ([hoaDonId], [donViDoLuongId], [sanPhamId], [loHangId], [soLuong]) VALUES (N'HD2024-0010', N'DVL-0019', N'SP2024-0010', N'LH-0010', 1)
GO

INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0001', N'THUOC_KE_DON', N'VIEN_NANG', N'Amoxicillin 500mg', N'Amox 500', N'Pymepharco', N'Amoxicillin trihydrate', CAST(5.00 AS Decimal(18, 2)), N'500mg', N'Kháng sinh nhóm penicillin', N'Viên', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(55000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0002', N'THUOC_KE_DON', N'VIEN_NEN', N'Cefuroxime 500mg', N'Cefu 500', N'Stada VN', N'Cefuroxime axetil', CAST(5.00 AS Decimal(18, 2)), N'500mg', N'Kháng sinh cephalosporin', N'Viên', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(65000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0003', N'THUOC_KE_DON', N'VIEN_NEN', N'Metformin 500mg', N'Metf 500', N'Traphaco', N'Metformin hydrochloride', CAST(5.00 AS Decimal(18, 2)), N'500mg', N'Điều trị đái tháo đường', N'Viên', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(75000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0004', N'THUOC_KE_DON', N'VIEN_NEN', N'Losartan 50mg', N'Losar 50', N'Imexpharm', N'Losartan kali', CAST(5.00 AS Decimal(18, 2)), N'50mg', N'Thuốc hạ huyết áp', N'Viên', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(40000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0005', N'THUOC_KE_DON', N'HON_DICH', N'Augmentin 250mg/5ml Siro', N'Aug Siro', N'GSK', N'Amoxicillin + Acid clavulanic', CAST(5.00 AS Decimal(18, 2)), N'250mg/5ml', N'Kháng sinh cho trẻ em', N'Chai', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(90000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0006', N'THUOC_KE_DON', N'HON_DICH', N'Azithromycin 200mg/5ml', N'Azith Siro', N'Pymepharco', N'Azithromycin dihydrate', CAST(5.00 AS Decimal(18, 2)), N'200mg/5ml', N'Kháng sinh macrolide', N'Chai', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(75000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0007', N'THUOC_KE_DON', N'VIEN_NEN', N'Amlodipine 5mg', N'Amlo 5', N'Stada VN', N'Amlodipine besylate', CAST(5.00 AS Decimal(18, 2)), N'5mg', N'Điều trị tăng huyết áp', N'Viên', CAST(N'2024-01-02' AS DateTime2), N'HOAT_DONG', CAST(70000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0008', N'THUOC_KE_DON', N'VIEN_NEN', N'Atorvastatin 20mg', N'Ator 20', N'Imexpharm', N'Atorvastatin calcium', CAST(5.00 AS Decimal(18, 2)), N'20mg', N'Thuốc hạ mỡ máu', N'Viên', CAST(N'2024-01-02' AS DateTime2), N'HOAT_DONG', CAST(98000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0009', N'THUOC_KHONG_KE_DON', N'VIEN_NEN', N'Paracetamol 500mg', N'Para 500', N'Domesco', N'Paracetamol', CAST(5.00 AS Decimal(18, 2)), N'500mg', N'Giảm đau, hạ sốt', N'Viên', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(40000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0010', N'THUOC_KHONG_KE_DON', N'VIEN_NEN', N'Ibuprofen 400mg', N'Ibup 400', N'OPV', N'Ibuprofen', CAST(5.00 AS Decimal(18, 2)), N'400mg', N'Kháng viêm không steroid', N'Viên', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(18000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0011', N'THUOC_KHONG_KE_DON', N'VIEN_SUI', N'Vitamin C 1000mg Sủi', N'VitC 1000', N'DHG Pharma', N'Acid ascorbic', CAST(5.00 AS Decimal(18, 2)), N'1000mg', N'Tăng sức đề kháng', N'Viên', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(65000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0012', N'THUOC_KHONG_KE_DON', N'VIEN_NEN', N'Cetirizine 10mg', N'Cetir 10', N'Stada VN', N'Cetirizine hydrochloride', CAST(5.00 AS Decimal(18, 2)), N'10mg', N'Chống dị ứng', N'Viên', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(18000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0013', N'THUOC_KHONG_KE_DON', N'THUOC_BOT', N'Oresol Cam', N'ORS Cam', N'Vinpharco', N'Glucose + Natri Clorid', CAST(5.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Bù nước và điện giải', N'Gói', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(90000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0014', N'THUOC_KHONG_KE_DON', N'DUNG_DICH', N'Xịt mũi Naphazoline 0.05%', N'Xịt Naphaz', N'Pharmedic', N'Naphazoline hydrochloride', CAST(5.00 AS Decimal(18, 2)), N'0.05%', N'Giảm ngạt mũi', N'Chai', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(35000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0015', N'THUOC_KHONG_KE_DON', N'VIEN_NEN', N'Domperidon 10mg', N'Domp 10', N'Pymepharco', N'Domperidone', CAST(5.00 AS Decimal(18, 2)), N'10mg', N'Chống nôn', N'Viên', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(40000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0016', N'THUOC_KHONG_KE_DON', N'VIEN_NANG', N'Omeprazole 20mg', N'Ome 20', N'Traphaco', N'Omeprazole', CAST(5.00 AS Decimal(18, 2)), N'20mg', N'Điều trị loét dạ dày', N'Viên', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(52000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0017', N'THUOC_KHONG_KE_DON', N'DUNG_DICH', N'Siro Ho Bổ Phế', N'Siro Ho BP', N'Traphaco', N'Thảo dược', CAST(5.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Giảm ho', N'Chai', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(55000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0018', N'THUOC_KHONG_KE_DON', N'THUOC_BOT', N'Smecta 3g Bột', N'Smecta', N'Ipsen', N'Diosmectite', CAST(5.00 AS Decimal(18, 2)), N'3g', N'Điều trị tiêu chảy', N'Gói', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(220000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0019', N'THUOC_KHONG_KE_DON', N'VIEN_NEN', N'Loratadine 10mg', N'Lorat 10', N'DHG Pharma', N'Loratadine', CAST(5.00 AS Decimal(18, 2)), N'10mg', N'Kháng histamine', N'Viên', CAST(N'2024-01-03' AS DateTime2), N'HOAT_DONG', CAST(16000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0020', N'THUOC_KHONG_KE_DON', N'VIEN_NANG', N'Esomeprazole 20mg', N'Esome 20', N'AstraZeneca', N'Esomeprazole magnesium', CAST(5.00 AS Decimal(18, 2)), N'20mg', N'Điều trị trào ngược', N'Viên', CAST(N'2024-01-03' AS DateTime2), N'HOAT_DONG', CAST(52000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0021', N'THUC_PHAM_CHUC_NANG', N'VIEN_NANG', N'Vitamin D3 K2 2000IU', N'VitD3K2', N'Nature Made', N'Cholecalciferol', CAST(10.00 AS Decimal(18, 2)), N'2000IU', N'Hỗ trợ xương khớp', N'Viên', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(220000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0022', N'THUC_PHAM_CHUC_NANG', N'VIEN_NANG', N'Omega-3 Fish Oil 1000mg', N'Omega3 1000', N'Nature Made', N'EPA + DHA', CAST(10.00 AS Decimal(18, 2)), N'1000mg', N'Hỗ trợ tim mạch', N'Viên', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(280000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0023', N'THUC_PHAM_CHUC_NANG', N'VIEN_NEN', N'Canxi Nano 500mg', N'Canxi Nano', N'DHG Pharma', N'Calcium carbonate', CAST(10.00 AS Decimal(18, 2)), N'500mg', N'Bổ sung canxi', N'Viên', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(85000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0024', N'THUC_PHAM_CHUC_NANG', N'DUNG_DICH', N'Collagen Peptide 5000mg', N'Collagen 5K', N'Kinoko VN', N'Collagen hydrolyzed', CAST(10.00 AS Decimal(18, 2)), N'5000mg', N'Làm đẹp da', N'Gói', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(420000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0025', N'THUC_PHAM_CHUC_NANG', N'KEO_NGAM', N'Melatonin 5mg', N'Melat 5', N'Natrol USA', N'Melatonin', CAST(10.00 AS Decimal(18, 2)), N'5mg', N'Hỗ trợ giấc ngủ', N'Viên', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(280000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0026', N'THUC_PHAM_CHUC_NANG', N'VIEN_NEN', N'Magie B6 Úc', N'Mg B6', N'Blackmores', N'Magnesium + Vitamin B6', CAST(10.00 AS Decimal(18, 2)), N'500mg', N'Giảm căng thẳng', N'Viên', CAST(N'2024-01-04' AS DateTime2), N'HOAT_DONG', CAST(195000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0027', N'THUC_PHAM_CHUC_NANG', N'THUOC_BOT', N'Probiotics 10 tỷ CFU', N'Probio 10B', N'Yakult VN', N'Lactobacillus', CAST(10.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Hỗ trợ tiêu hóa', N'Viên', CAST(N'2024-01-04' AS DateTime2), N'HOAT_DONG', CAST(165000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0028', N'MY_PHAM', N'HON_DICH', N'Kem dưỡng da Eucerin Q10', N'Eucerin Q10', N'Eucerin', N'Chưa cập nhật', CAST(10.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Chống lão hóa', N'Hộp', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(650000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0029', N'MY_PHAM', N'HON_DICH', N'Kem chống nắng La Roche', N'LRP SPF50', N'La Roche-Posay', N'Chưa cập nhật', CAST(10.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Bảo vệ da nhạy cảm', N'Tuýp', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(580000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0030', N'MY_PHAM', N'DUNG_DICH', N'Sữa rửa mặt CeraVe', N'CeraVe Foam', N'CeraVe', N'Chưa cập nhật', CAST(10.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Sữa rửa mặt tạo bọt', N'Chai', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(320000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0031', N'MY_PHAM', N'DUNG_DICH', N'Nước tẩy trang Bioderma', N'Bioderma', N'Bioderma', N'Chưa cập nhật', CAST(10.00 AS Decimal(18, 2)), N'Chưa cập nhật', N'Tẩy trang dịu nhẹ', N'Chai', CAST(N'2024-01-01' AS DateTime2), N'HOAT_DONG', CAST(280000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0032', N'MY_PHAM', N'DUNG_DICH', N'Toner Paula Niacinamide', N'Paula Toner', N'Paula Choice', N'Niacinamide', CAST(10.00 AS Decimal(18, 2)), N'10%', N'Làm sáng da', N'Chai', CAST(N'2024-01-05' AS DateTime2), N'HOAT_DONG', CAST(850000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0033', N'THUOC_KHONG_KE_DON', N'THUOC_NHO_GIOT', N'V.Rohto Vitamin', N'V.Rohto', N'Rohto', N'Vitamin B5, B6', CAST(10.00 AS Decimal(18, 2)), N'13ml', N'Giảm mỏi mắt', N'Chai', GETDATE(), N'HOAT_DONG', CAST(52000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0034', N'THUOC_KHONG_KE_DON', N'THUOC_NHO_GIOT', N'Thuốc nhỏ mắt Osla', N'Osla', N'MerAP', N'Natri clorid', CAST(5.00 AS Decimal(18, 2)), N'15ml', N'Rửa mắt', N'Chai', GETDATE(), N'HOAT_DONG', CAST(22000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0035', N'MY_PHAM', N'SUC_MIENG', N'Listerine Cool Mint', N'Listerine', N'Johnson', N'Thymol', CAST(10.00 AS Decimal(18, 2)), N'750ml', N'Hơi thở thơm mát', N'Chai', GETDATE(), N'HOAT_DONG', CAST(115000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0036', N'MY_PHAM', N'SUC_MIENG', N'Betadine Gargle', N'Betadine', N'Mundipharma', N'Povidone-Iodine', CAST(10.00 AS Decimal(18, 2)), N'125ml', N'Sát khuẩn miệng', N'Chai', GETDATE(), N'HOAT_DONG', CAST(68000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0037', N'THUOC_KHONG_KE_DON', N'KEO_NGAM', N'Strepsils Cool', N'Strepsils', N'Reckitt', N'Dichlorobenzyl', CAST(10.00 AS Decimal(18, 2)), N'1.2mg', N'Giảm đau họng', N'Viên', GETDATE(), N'HOAT_DONG', CAST(80000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0038', N'THUOC_KHONG_KE_DON', N'VIEN_SUI', N'Efferalgan 500mg', N'Efferalgan', N'UPSA', N'Paracetamol', CAST(5.00 AS Decimal(18, 2)), N'500mg', N'Hạ sốt nhanh', N'Viên', GETDATE(), N'HOAT_DONG', CAST(62000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0039', N'THUOC_KHONG_KE_DON', N'THUOC_BOT', N'Hapacol 150', N'Hapacol 150', N'DHG Pharma', N'Paracetamol', CAST(5.00 AS Decimal(18, 2)), N'150mg', N'Hạ sốt cho trẻ', N'Gói', GETDATE(), N'HOAT_DONG', CAST(68000.00 AS Decimal(18, 2)))
INSERT [dbo].[SanPham] ([id], [danhMuc], [dang], [ten], [tenVietTat], [nhaSanXuat], [hoatChat], [thueVAT], [hamLuong], [moTa], [donViDoCoBan], [ngayTao], [trangThai], [giaBan]) VALUES (N'SP2024-0040', N'THUOC_KE_DON', N'HON_DICH', N'Phosphalugel', N'Chữ P', N'Astellas', N'Aluminum phosphate', CAST(5.00 AS Decimal(18, 2)), N'20%', N'Kháng axit dạ dày', N'Gói', GETDATE(), N'HOAT_DONG', CAST(110000.00 AS Decimal(18, 2)))
GO
INSERT [dbo].[TaiKhoan] ([id], [nhanVienId], [vaiTro], [tenDangNhap], [matKhau]) VALUES (N'TK0001', N'QL-0001', N'ADMIN', N'admin', N'12345678')
INSERT [dbo].[TaiKhoan] ([id], [nhanVienId], [vaiTro], [tenDangNhap], [matKhau]) VALUES (N'TK0002', N'DS-0001', N'STAFF', N'dat', N'23652461')
INSERT [dbo].[TaiKhoan] ([id], [nhanVienId], [vaiTro], [tenDangNhap], [matKhau]) VALUES (N'TK0003', N'DS-0002', N'STAFF', N'kien', N'23653611')
INSERT [dbo].[TaiKhoan] ([id], [nhanVienId], [vaiTro], [tenDangNhap], [matKhau]) VALUES (N'TK0004', N'DS-0003', N'STAFF', N'nam', N'23652641')
INSERT [dbo].[TaiKhoan] ([id], [nhanVienId], [vaiTro], [tenDangNhap], [matKhau]) VALUES (N'TK0005', N'DS-0004', N'STAFF', N'thuan', N'23652091')
INSERT [dbo].[TaiKhoan] ([id], [nhanVienId], [vaiTro], [tenDangNhap], [matKhau]) VALUES (N'TK0006', N'DS-0005', N'STAFF', N'kiet', N'23654991')
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_ApDungKhuyenMai]    Script Date: 12/04/2026 4:25:10 CH ******/
ALTER TABLE [dbo].[ApDungKhuyenMai] ADD  CONSTRAINT [UQ_ApDungKhuyenMai] UNIQUE NONCLUSTERED 
(
	[khuyenMaiId] ASC,
	[sanPhamId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__TaiKhoan__59267D4A013C99A0]    Script Date: 12/04/2026 4:25:10 CH ******/
ALTER TABLE [dbo].[TaiKhoan] ADD UNIQUE NONCLUSTERED 
(
	[tenDangNhap] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__TaiKhoan__D86F2027CF35E3EB]    Script Date: 12/04/2026 4:25:10 CH ******/
ALTER TABLE [dbo].[TaiKhoan] ADD UNIQUE NONCLUSTERED 
(
	[nhanVienId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO

ALTER TABLE [dbo].[ApDungKhuyenMai]  WITH CHECK ADD  CONSTRAINT [FK_ApDungKhuyenMai_KhuyenMai] FOREIGN KEY([khuyenMaiId])
REFERENCES [dbo].[KhuyenMai] ([id])
ON UPDATE CASCADE
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[ApDungKhuyenMai] CHECK CONSTRAINT [FK_ApDungKhuyenMai_KhuyenMai]
GO
ALTER TABLE [dbo].[ApDungKhuyenMai]  WITH CHECK ADD  CONSTRAINT [FK_ApDungKhuyenMai_SanPham] FOREIGN KEY([sanPhamId])
REFERENCES [dbo].[SanPham] ([id])
ON UPDATE CASCADE
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[ApDungKhuyenMai] CHECK CONSTRAINT [FK_ApDungKhuyenMai_SanPham]
GO
ALTER TABLE [dbo].[CaLamViec]  WITH CHECK ADD  CONSTRAINT [FK_CaLamViec_NhanVien] FOREIGN KEY([nhanVienId])
REFERENCES [dbo].[NhanVien] ([id])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[CaLamViec] CHECK CONSTRAINT [FK_CaLamViec_NhanVien]
GO
ALTER TABLE [dbo].[ChiTietHoaDon]  WITH CHECK ADD  CONSTRAINT [FK_ChiTietHoaDon_DonViDoLuong] FOREIGN KEY([donViDoLuongId], [sanPhamId])
REFERENCES [dbo].[DonViDoLuong] ([id], [sanPhamId])
GO
ALTER TABLE [dbo].[ChiTietHoaDon] CHECK CONSTRAINT [FK_ChiTietHoaDon_DonViDoLuong]
GO
ALTER TABLE [dbo].[ChiTietHoaDon]  WITH CHECK ADD  CONSTRAINT [FK_ChiTietHoaDon_HoaDon] FOREIGN KEY([hoaDonId])
REFERENCES [dbo].[HoaDon] ([id])
ON UPDATE CASCADE
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[ChiTietHoaDon] CHECK CONSTRAINT [FK_ChiTietHoaDon_HoaDon]
GO
ALTER TABLE [dbo].[ChiTietHoaDon]  WITH CHECK ADD  CONSTRAINT [FK_ChiTietHoaDon_SanPham] FOREIGN KEY([sanPhamId])
REFERENCES [dbo].[SanPham] ([id])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[ChiTietHoaDon] CHECK CONSTRAINT [FK_ChiTietHoaDon_SanPham]
GO
ALTER TABLE [dbo].[DieuKienKhuyenMai]  WITH CHECK ADD  CONSTRAINT [FK_DieuKienKhuyenMai_KhuyenMai] FOREIGN KEY([khuyenMaiId])
REFERENCES [dbo].[KhuyenMai] ([id])
ON UPDATE CASCADE
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[DieuKienKhuyenMai] CHECK CONSTRAINT [FK_DieuKienKhuyenMai_KhuyenMai]
GO
ALTER TABLE [dbo].[DonViDoLuong]  WITH CHECK ADD  CONSTRAINT [FK_DonViDoLuong_SanPham] FOREIGN KEY([sanPhamId])
REFERENCES [dbo].[SanPham] ([id])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[DonViDoLuong] CHECK CONSTRAINT [FK_DonViDoLuong_SanPham]
GO
ALTER TABLE [dbo].[HinhThucKhuyenMai]  WITH CHECK ADD  CONSTRAINT [FK_HinhThucKhuyenMai_DonViDoLuong] FOREIGN KEY([donViDoLuongId], [sanPhamId])
REFERENCES [dbo].[DonViDoLuong] ([id], [sanPhamId])
ON UPDATE CASCADE
ON DELETE SET NULL
GO
ALTER TABLE [dbo].[HinhThucKhuyenMai] CHECK CONSTRAINT [FK_HinhThucKhuyenMai_DonViDoLuong]
GO
ALTER TABLE [dbo].[HinhThucKhuyenMai]  WITH CHECK ADD  CONSTRAINT [FK_HinhThucKhuyenMai_KhuyenMai] FOREIGN KEY([khuyenMaiId])
REFERENCES [dbo].[KhuyenMai] ([id])
ON UPDATE CASCADE
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[HinhThucKhuyenMai] CHECK CONSTRAINT [FK_HinhThucKhuyenMai_KhuyenMai]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [FK_HoaDon_HoaDonGoc] FOREIGN KEY([hoaDonGocId])
REFERENCES [dbo].[HoaDon] ([id])
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [FK_HoaDon_HoaDonGoc]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [FK_HoaDon_KhachHang] FOREIGN KEY([khachHangId])
REFERENCES [dbo].[KhachHang] ([id])
ON UPDATE CASCADE
ON DELETE SET NULL
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [FK_HoaDon_KhachHang]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [FK_HoaDon_KhuyenMai] FOREIGN KEY([khuyenMaiId])
REFERENCES [dbo].[KhuyenMai] ([id])
ON UPDATE CASCADE
ON DELETE SET NULL
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [FK_HoaDon_KhuyenMai]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [FK_HoaDon_NhanVien] FOREIGN KEY([nhanVienId])
REFERENCES [dbo].[NhanVien] ([id])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [FK_HoaDon_NhanVien]
GO
ALTER TABLE [dbo].[LoHang]  WITH CHECK ADD  CONSTRAINT [FK_LoHang_KhoHang] FOREIGN KEY([khoHangId])
REFERENCES [dbo].[KhoHang] ([id])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[LoHang] CHECK CONSTRAINT [FK_LoHang_KhoHang]
GO
ALTER TABLE [dbo].[LoHang]  WITH CHECK ADD  CONSTRAINT [FK_LoHang_SanPham] FOREIGN KEY([sanPhamId])
REFERENCES [dbo].[SanPham] ([id])
ON UPDATE CASCADE
GO
ALTER TABLE [dbo].[LoHang] CHECK CONSTRAINT [FK_LoHang_SanPham]
GO
ALTER TABLE [dbo].[PhanBoLoHang]  WITH CHECK ADD  CONSTRAINT [FK_PhanBoLoHang_ChiTietHoaDon] FOREIGN KEY([hoaDonId], [donViDoLuongId], [sanPhamId])
REFERENCES [dbo].[ChiTietHoaDon] ([hoaDonId], [donViDoLuongId], [sanPhamId])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[PhanBoLoHang] CHECK CONSTRAINT [FK_PhanBoLoHang_ChiTietHoaDon]
GO
ALTER TABLE [dbo].[PhanBoLoHang]  WITH CHECK ADD  CONSTRAINT [FK_PhanBoLoHang_LoHang] FOREIGN KEY([loHangId])
REFERENCES [dbo].[LoHang] ([id])
GO
ALTER TABLE [dbo].[PhanBoLoHang] CHECK CONSTRAINT [FK_PhanBoLoHang_LoHang]
GO
ALTER TABLE [dbo].[TaiKhoan]  WITH CHECK ADD  CONSTRAINT [FK_TaiKhoan_NhanVien] FOREIGN KEY([nhanVienId])
REFERENCES [dbo].[NhanVien] ([id])
ON UPDATE CASCADE
ON DELETE SET NULL
GO
ALTER TABLE [dbo].[TaiKhoan] CHECK CONSTRAINT [FK_TaiKhoan_NhanVien]
GO
ALTER TABLE [dbo].[CaLamViec]  WITH CHECK ADD  CONSTRAINT [CK_CaLamViec_ThoiGian] CHECK  (([thoiGianKetThuc] IS NULL OR [thoiGianKetThuc]>=[thoiGianBatDau]))
GO
ALTER TABLE [dbo].[CaLamViec] CHECK CONSTRAINT [CK_CaLamViec_ThoiGian]
GO
ALTER TABLE [dbo].[DieuKienKhuyenMai]  WITH CHECK ADD  CONSTRAINT [CK_DieuKienKhuyenMai_DoiTuong] CHECK  (([doiTuongApDung]=N'HOA_DON' OR [doiTuongApDung]=N'SAN_PHAM'))
GO
ALTER TABLE [dbo].[DieuKienKhuyenMai] CHECK CONSTRAINT [CK_DieuKienKhuyenMai_DoiTuong]
GO
ALTER TABLE [dbo].[DieuKienKhuyenMai]  WITH CHECK ADD  CONSTRAINT [CK_DieuKienKhuyenMai_Loai] CHECK  (([loaiDieuKien]=N'GIA_TRI' OR [loaiDieuKien]=N'SO_LUONG'))
GO
ALTER TABLE [dbo].[DieuKienKhuyenMai] CHECK CONSTRAINT [CK_DieuKienKhuyenMai_Loai]
GO
ALTER TABLE [dbo].[HinhThucKhuyenMai]  WITH CHECK ADD  CONSTRAINT [CK_HinhThucKhuyenMai_DoiTuong] CHECK  (([doiTuongApDung]=N'HOA_DON' OR [doiTuongApDung]=N'SAN_PHAM'))
GO
ALTER TABLE [dbo].[HinhThucKhuyenMai] CHECK CONSTRAINT [CK_HinhThucKhuyenMai_DoiTuong]
GO
ALTER TABLE [dbo].[HinhThucKhuyenMai]  WITH CHECK ADD  CONSTRAINT [CK_HinhThucKhuyenMai_Loai] CHECK  (([loaiHinhThuc]=N'SAN_PHAM_KEM_THEO' OR [loaiHinhThuc]=N'GIAM_THEO_PHAN_TRAM'))
GO
ALTER TABLE [dbo].[HinhThucKhuyenMai] CHECK CONSTRAINT [CK_HinhThucKhuyenMai_Loai]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [CK_HoaDon_Loai] CHECK  (([loaiHD]=N'BAN_HANG' OR [loaiHD]=N'TRA_HANG' OR [loaiHD]=N'DOI_HANG'))
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [CK_HoaDon_Loai]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [CK_HoaDon_PhuongThucThanhToan] CHECK  (([phuongThucThanhToan]=N'CHUYEN_KHOAN_NGAN_HANG' OR [phuongThucThanhToan]=N'TIEN_MAT'))
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [CK_HoaDon_PhuongThucThanhToan]
GO
ALTER TABLE [dbo].[KhuyenMai]  WITH CHECK ADD  CONSTRAINT [CK_KhuyenMai_ThoiGian] CHECK  (([ngayKetThuc] IS NULL OR [ngayKetThuc]>=[ngayBatDau]))
GO
ALTER TABLE [dbo].[KhuyenMai] CHECK CONSTRAINT [CK_KhuyenMai_ThoiGian]
GO
ALTER TABLE [dbo].[LoHang]  WITH CHECK ADD  CONSTRAINT [CK_LoHang_TrangThai] CHECK  (([trangThai]=N'HET_HAN' OR [trangThai]=N'HET_HANG' OR [trangThai]=N'CON_HANG'))
GO
ALTER TABLE [dbo].[LoHang] CHECK CONSTRAINT [CK_LoHang_TrangThai]
GO
ALTER TABLE [dbo].[NhanVien]  WITH CHECK ADD  CONSTRAINT [CK_NhanVien_ChucVu] CHECK  (([chucVu]=N'NGUOI_QUAN_LY' OR [chucVu]=N'DUOC_SI'))
GO
ALTER TABLE [dbo].[NhanVien] CHECK CONSTRAINT [CK_NhanVien_ChucVu]
GO
ALTER TABLE [dbo].[NhanVien]  WITH CHECK ADD  CONSTRAINT [CK_NhanVien_TrangThaiLamViec] CHECK  (([trangThaiLamViec]=N'DANG_LAM_VIEC' OR [trangThaiLamViec]=N'NGHI_PHEP' OR [trangThaiLamViec]=N'THOI_VIEC'))
GO
ALTER TABLE [dbo].[NhanVien] CHECK CONSTRAINT [CK_NhanVien_TrangThaiLamViec]
GO
ALTER TABLE [dbo].[SanPham]  WITH CHECK ADD  CONSTRAINT [CK_SanPham_Dang] CHECK  (([dang]=N'VIEN_NEN' OR [dang]=N'VIEN_NANG' OR [dang]=N'VIEN_SUI' OR [dang]=N'THUOC_BOT' OR [dang]=N'KEO_NGAM' OR [dang]=N'DUNG_DICH' OR [dang]=N'HON_DICH' OR [dang]=N'THUOC_NHO_GIOT' OR [dang]=N'SUC_MIENG'))
GO
ALTER TABLE [dbo].[SanPham] CHECK CONSTRAINT [CK_SanPham_Dang]
GO
ALTER TABLE [dbo].[SanPham]  WITH CHECK ADD  CONSTRAINT [CK_SanPham_DanhMuc] CHECK  (([danhMuc]=N'MY_PHAM' OR [danhMuc]=N'THUOC_KE_DON' OR [danhMuc]=N'THUOC_KHONG_KE_DON' OR [danhMuc]=N'THUC_PHAM_CHUC_NANG'))
GO
ALTER TABLE [dbo].[SanPham] CHECK CONSTRAINT [CK_SanPham_DanhMuc]
GO
ALTER TABLE [dbo].[TaiKhoan]  WITH CHECK ADD  CONSTRAINT [CK_TaiKhoan_VaiTro] CHECK  (([vaiTro]=N'STAFF' OR [vaiTro]=N'ADMIN'))
GO
ALTER TABLE [dbo].[TaiKhoan] CHECK CONSTRAINT [CK_TaiKhoan_VaiTro]
GO
USE [master]
GO
ALTER DATABASE [MYCAREPHARMACY] SET  READ_WRITE 
GO
USE [MYCAREPHARMACY]
GO
ALTER TABLE [dbo].[TaiKhoan] DROP CONSTRAINT [CK_TaiKhoan_VaiTro]
GO
ALTER TABLE [dbo].[LoHang] DROP CONSTRAINT [CK_LoHang_TrangThai]
GO
ALTER TABLE [dbo].[LoHang] WITH CHECK ADD CONSTRAINT [CK_LoHang_TrangThai]
    CHECK (([trangThai] = N'HET_HAN' OR [trangThai] = N'HET_HANG'
         OR [trangThai] = N'CON_HANG' OR [trangThai] = N'AN'))
GO
UPDATE sp SET sp.giaBan = dvl.gia
FROM SanPham sp
INNER JOIN DonViDoLuong dvl ON dvl.sanPhamId = sp.id AND dvl.ten = sp.donViDoCoBan
WHERE sp.giaBan != dvl.gia;