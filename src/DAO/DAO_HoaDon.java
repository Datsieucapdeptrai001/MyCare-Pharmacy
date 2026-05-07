package DAO;

import ConnectDB.ConnectDB;
import Entity.ChiTietHoaDon;
import Entity.HoaDon;
import Entity.KhachHang;
import Entity.KhuyenMai;
import Entity.LoHang;
import Entity.NhanVien;
import Entity.PhanBoLoHang;
import Enumeration.LoaiHoaDon;
import Enumeration.PhuongThucThanhToan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DAO_HoaDon {

    public DAO_HoaDon() {}

    public List<Object[]> layDanhSachHoaDonChoBang() {
        List<Object[]> ds = new ArrayList<>();
        
        // ĐÃ SỬA: Đổi / 100 thành / 100.0 để tránh lỗi chia số nguyên (mất VAT) trong SQL
        String sql = "SELECT hd.id, hd.loaiHD, hd.ngayLapHD, kh.hoVaTen, kh.sdt, hd.phuongThucThanhToan, hd.ghiChu, " +
                "(SELECT SUM(ct.soLuong * dv.gia * (1 + (ISNULL(sp.thueVAT, 0) / 100.0))) " + 
                " FROM ChiTietHoaDon ct " +
                " JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id AND ct.sanPhamId = dv.sanPhamId " +
                " JOIN SanPham sp ON ct.sanPhamId = sp.id " + 
                " WHERE ct.hoaDonId = hd.id) as tongTienGoc " +
                "FROM HoaDon hd " +
                "LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id " +
                "ORDER BY hd.ngayLapHD DESC"; 
                     
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            
            DecimalFormat df = new DecimalFormat("#,###đ");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            while (rs.next()) {
                double totalAmount = rs.getDouble("tongTienGoc");
                double originalAmount = totalAmount; // Lưu lại tổng tiền gốc để tính % KM
                double tongTienGiam = 0;
                String ghiChu = rs.getString("ghiChu");
                String loaiHD = rs.getString("loaiHD"); 
                
                // ĐÃ SỬA: Xử lý khấu trừ tiền giảm giá đồng bộ với hàm layThongTinGiaTuHDGoc
                if (ghiChu != null && !ghiChu.isEmpty()) {
                    String[] parts = ghiChu.split("\\|");
                    for (String p : parts) {
                        p = p.trim();
                        if (p.startsWith("Dùng điểm: -") || p.contains("KM_GIAM:")) {
                            try {
                                long tienGiam = Long.parseLong(p.replaceAll("[^0-9]", ""));
                                tongTienGiam += tienGiam;
                            } catch (Exception ignored) {}
                        } else if (p.startsWith("KM:")) {
                            // Truy vấn để bóc tách và tính toán % giảm hoặc tiền mặt từ HinhThucKhuyenMai
                            String[] mks = p.substring(3).trim().split(",");
                            for (String mk : mks) {
                                String sqlKM = "SELECT loaiHinhThuc, giaTri FROM HinhThucKhuyenMai WHERE khuyenMaiId = ?";
                                try (PreparedStatement pstKM = con.prepareStatement(sqlKM)) {
                                    pstKM.setString(1, mk.trim());
                                    try (ResultSet rsKM = pstKM.executeQuery()) {
                                        if (rsKM.next()) {
                                            String loaiKM = rsKM.getString("loaiHinhThuc");
                                            double val = rsKM.getDouble("giaTri");
                                            if (loaiKM.contains("PHAN_TRAM") || loaiKM.contains("%")) {
                                                tongTienGiam += originalAmount * (val / 100.0);
                                            } else if (loaiKM.contains("TIEN_MAT")) {
                                                tongTienGiam += val;
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                }
                
                totalAmount -= tongTienGiam; // Trừ đi tổng tiền khuyến mãi
                if (totalAmount < 0) totalAmount = 0;

                String id = rs.getString("id");
                String ngay = rs.getTimestamp("ngayLapHD") != null 
                             ? rs.getTimestamp("ngayLapHD").toLocalDateTime().format(dtf) : "";
                String kh = rs.getString("hoVaTen") != null ? rs.getString("hoVaTen") : "Khách lẻ";
                String sdt = rs.getString("sdt") != null ? rs.getString("sdt") : "";
                
                String pt = rs.getString("phuongThucThanhToan");
                String hienThiPT = "CHUYEN_KHOAN_NGAN_HANG".equals(pt) ? "Chuyển khoản" : "Tiền mặt";

                String trangThai = "Hoàn thành";
                if (loaiHD != null && (loaiHD.equals("TRA_HANG") || loaiHD.equals("DOI_HANG"))) {
                    trangThai = "Đổi trả";
                } else if (ghiChu != null) {
                    if (ghiChu.contains("Lưu nháp") || ghiChu.contains("Đang xử lý")) trangThai = "Đang xử lý";
                    else if (ghiChu.contains("Đã hủy")) trangThai = "Đã hủy";
                }

                ds.add(new Object[]{
                    id, ngay, kh, sdt, hienThiPT, df.format(totalAmount), trangThai, ghiChu
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }
    
    public List<String> timGoiYHoaDonHoanThanh(String tuKhoa) {
        List<String> ds = new ArrayList<>();
        // ĐÃ FIX: Chuyển kiểm tra trạng thái sang lọc qua loaiHD và ghiChu
        String sql = "SELECT id FROM HoaDon WHERE id LIKE ? AND hoaDonGocId IS NULL " +
                     "AND loaiHD = 'BAN_HANG' " +
                     "AND (ghiChu IS NULL OR (ghiChu NOT LIKE N'%Lưu nháp%' AND ghiChu NOT LIKE N'%Đã hủy%'))";
        
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
             
            pst.setString(1, "%" + tuKhoa + "%");
            
            try (ResultSet rs = pst.executeQuery()) {
                int count = 0;
                while (rs.next() && count < 5) {
                    ds.add(rs.getString("id"));
                    count++;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn gợi ý hóa đơn: " + e.getMessage());
        }
        return ds;
    }
 // HÀM SINH MÃ TỰ ĐỘNG LIÊN TỤC KHÔNG BAO GIỜ TRÙNG
    public String phatSinhMaHoaDonTuDong() {
        String maMoi = "";
        int year = java.time.LocalDateTime.now().getYear();
        
        // Đã sửa lại thành "HD-" để khớp chính xác với hiển thị trên giao diện của bạn
        String prefix = "HD-" + year + "-"; 
        
        String sql = "SELECT id FROM HoaDon WHERE id LIKE ?";
        int maxStt = 0;
        
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, prefix + "%");
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    if (id != null && id.startsWith(prefix)) {
                        try {
                            // Lấy phần đuôi sau chữ "HD-2026-" để chuyển thành số
                            int stt = Integer.parseInt(id.substring(prefix.length()));
                            if (stt > maxStt) {
                                maxStt = stt;
                            }
                        } catch (NumberFormatException e) {
                            // Bỏ qua nếu lỗi
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Tạo mã mới (Ví dụ: HD-2026-1, HD-2026-2...)
        maMoi = prefix + (maxStt + 1);
        
        return maMoi;
    }
    public List<Object[]> layDanhSachHoaDonTheoNVHomNay(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) return null;

        List<Object[]> ds = new ArrayList<>();
        String sql =
            "SELECT hd.id, hd.loaiHD, hd.ngayLapHD, kh.hoVaTen, kh.sdt, hd.phuongThucThanhToan, hd.ghiChu, " +
            "(SELECT SUM(ct.soLuong * dv.gia * (1 + (ISNULL(sp.thueVAT, 0) / 100.0))) " +
            " FROM ChiTietHoaDon ct " +
            " JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id AND ct.sanPhamId = dv.sanPhamId " +
            " JOIN SanPham sp ON ct.sanPhamId = sp.id " +
            " WHERE ct.hoaDonId = hd.id) as tongTienGoc " +
            "FROM HoaDon hd " +
            "LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id " +
            "WHERE hd.nhanVienId = ? " +
            "AND CAST(hd.ngayLapHD AS DATE) = CAST(GETDATE() AS DATE) " +
            "ORDER BY hd.ngayLapHD DESC";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, maNV.trim());

            try (ResultSet rs = pst.executeQuery()) {
                DecimalFormat df = new DecimalFormat("#,###đ");
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                while (rs.next()) {
                    double totalAmount = rs.getDouble("tongTienGoc");
                    double originalAmount = totalAmount;
                    double tongTienGiam = 0;
                    String ghiChu = rs.getString("ghiChu");
                    String loaiHD = rs.getString("loaiHD");

                    if (ghiChu != null && !ghiChu.isEmpty()) {
                        String[] parts = ghiChu.split("\\|");
                        for (String p : parts) {
                            p = p.trim();
                            if (p.startsWith("Dùng điểm: -") || p.contains("KM_GIAM:")) {
                                try { tongTienGiam += Long.parseLong(p.replaceAll("[^0-9]", "")); } catch (Exception ignored) {}
                            } else if (p.startsWith("KM:")) {
                                String[] mks = p.substring(3).trim().split(",");
                                for (String mk : mks) {
                                    String sqlKM = "SELECT loaiHinhThuc, giaTri FROM HinhThucKhuyenMai WHERE khuyenMaiId = ?";
                                    try (PreparedStatement pstKM = con.prepareStatement(sqlKM)) {
                                        pstKM.setString(1, mk.trim());
                                        try (ResultSet rsKM = pstKM.executeQuery()) {
                                            if (rsKM.next()) {
                                                String loaiKM = rsKM.getString("loaiHinhThuc");
                                                double val = rsKM.getDouble("giaTri");
                                                if (loaiKM.contains("PHAN_TRAM") || loaiKM.contains("%"))
                                                    tongTienGiam += originalAmount * (val / 100.0);
                                                else if (loaiKM.contains("TIEN_MAT"))
                                                    tongTienGiam += val;
                                            }
                                        }
                                    } catch (Exception ignored) {}
                                }
                            }
                        }
                    }

                    totalAmount -= tongTienGiam;
                    if (totalAmount < 0) totalAmount = 0;

                    String id = rs.getString("id");
                    String ngay = rs.getTimestamp("ngayLapHD") != null
                                  ? rs.getTimestamp("ngayLapHD").toLocalDateTime().format(dtf) : "";
                    String kh = rs.getString("hoVaTen") != null ? rs.getString("hoVaTen") : "Khách lẻ";
                    String sdt = rs.getString("sdt") != null ? rs.getString("sdt") : "";
                    String pt = rs.getString("phuongThucThanhToan");
                    String hienThiPT = "CHUYEN_KHOAN_NGAN_HANG".equals(pt) ? "Chuyển khoản" : "Tiền mặt";

                    String trangThai = "Hoàn thành";
                    if (loaiHD != null && (loaiHD.equals("TRA_HANG") || loaiHD.equals("DOI_HANG"))) {
                        trangThai = "Đổi trả";
                    } else if (ghiChu != null) {
                        if (ghiChu.contains("Lưu nháp") || ghiChu.contains("Đang xử lý")) trangThai = "Đang xử lý";
                        else if (ghiChu.contains("Đã hủy")) trangThai = "Đã hủy";
                    }

                    ds.add(new Object[]{ id, ngay, kh, sdt, hienThiPT, df.format(totalAmount), trangThai, ghiChu });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Dành cho STAFF: chỉ lấy phiếu đổi/trả do nhân viên đó lập,
     * trong ngày hôm nay.
     */
    /**
     * Dành cho STAFF: chỉ lấy phiếu đổi/trả do nhân viên đó lập,
     * trong ngày hôm nay.
     */
    /**
     * Dành cho STAFF: Lấy danh sách phiếu đổi trả.
     * MỞ KHÓA: Fix lỗi múi giờ 2 giờ sáng và nới lỏng kiểm tra nhân viên.
     */
    public List<Object[]> layDanhSachPhieuDoiTraTheoNVHomNay(String maNV) {
        List<Object[]> list = new ArrayList<>();
        String cleanMaNV = (maNV != null) ? maNV.trim() : "";
        
        // 1. Quét theo mã DTH hoặc loại phiếu (chống lỗi form lưu sai loại)
        // 2. Cho phép hiển thị nếu phiếu chưa gắn nhân viên (IS NULL)
        // 3. BỎ chặn ngày GETDATE() để tránh lỗi múi giờ lúc nửa đêm
        String sql = "SELECT hd.id, hd.hoaDonGocId, kh.hoVaTen, hd.loaiHD, hd.ghiChu, hd.ngayLapHD " +
                     "FROM HoaDon hd " +
                     "LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id " +
                     "WHERE (hd.loaiHD IN ('DOI_HANG', 'TRA_HANG', N'Đổi hàng', N'Trả hàng') OR hd.id LIKE 'DTH-%') " +
                     "AND (hd.nhanVienId = ? OR hd.nhanVienId IS NULL OR hd.nhanVienId = '') " +
                     "ORDER BY hd.ngayLapHD DESC";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, cleanMaNV);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String loaiHD_DB = rs.getString(4);
                String loaiPhieuHienThi = "Đổi/Trả";
                
                // Dịch mã chuẩn xác bất chấp Database đang lưu tiếng Anh hay tiếng Việt
                if (loaiHD_DB != null) {
                    if (loaiHD_DB.contains("TRA") || loaiHD_DB.equalsIgnoreCase("Trả hàng")) {
                        loaiPhieuHienThi = "Trả hàng";
                    } else if (loaiHD_DB.contains("DOI") || loaiHD_DB.equalsIgnoreCase("Đổi hàng")) {
                        loaiPhieuHienThi = "Đổi hàng";
                    }
                }

                list.add(new Object[]{
                    rs.getString(1),    // Mã phiếu
                    rs.getString(2),    // Hóa đơn gốc
                    rs.getString(3) != null ? rs.getString(3) : "Khách lẻ", // Tên khách
                    loaiPhieuHienThi,   // Loại phiếu
                    rs.getString(5),    // Ghi chú / Trạng thái
                    rs.getTimestamp(6)  // Ngày tạo
                });
            }
        } catch (Exception e) {
            System.out.println("Lỗi DAO_HoaDon: " + e.getMessage());
        }
        return list;
    }


    public List<Object[]> layDanhSachHoaDonCuaNhanVien(String maNV) {
        List<Object[]> ds = new ArrayList<>();
        
        // SỬA: Thêm hd.loaiHD vào SELECT và GROUP BY. Xóa phần hd.loaiHD = 'BAN_HANG'
        String sql = "SELECT hd.id, hd.loaiHD, hd.ngayLapHD, kh.hoVaTen, kh.sdt, hd.phuongThucThanhToan, hd.ghiChu, " +
                     "SUM(ct.soLuong * dv.gia) as tongTien " +
                     "FROM HoaDon hd " +
                     "LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id " +
                     "LEFT JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId " +
                     "LEFT JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id AND ct.sanPhamId = dv.sanPhamId " +
                     "WHERE hd.nhanVienId = ? " + // Chỉ lọc theo nhân viên
                     "GROUP BY hd.id, hd.loaiHD, hd.ngayLapHD, kh.hoVaTen, kh.sdt, hd.phuongThucThanhToan, hd.ghiChu " +
                     "ORDER BY hd.ngayLapHD DESC";

        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maNV); 
            
            try (ResultSet rs = pst.executeQuery()) {
                DecimalFormat df = new DecimalFormat("#,###đ");
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                while (rs.next()) {
                    String maHD = rs.getString("id");
                    String loaiHD = rs.getString("loaiHD");
                    String ngay = rs.getTimestamp("ngayLapHD") != null ? rs.getTimestamp("ngayLapHD").toLocalDateTime().format(dtf) : "";
                    String tenKH = rs.getString("hoVaTen") != null ? rs.getString("hoVaTen") : "Khách lẻ";
                    String sdt = rs.getString("sdt") != null ? rs.getString("sdt") : "";
                    
                    String pttt = rs.getString("phuongThucThanhToan");
                    String pt = (pttt != null && pttt.equals("TIEN_MAT")) ? "Tiền mặt" : "Chuyển khoản";
                    
                    String tongTien = df.format(rs.getDouble("tongTien"));
                    String ghiChu = rs.getString("ghiChu");
                    
                    // LOGIC MỚI: ĐỒNG BỘ TRẠNG THÁI VỚI ADMIN
                    String trangThai = "Hoàn thành";
                    if (loaiHD != null && (loaiHD.equals("TRA_HANG") || loaiHD.equals("DOI_HANG"))) {
                        trangThai = "Đổi trả";
                    } else if (ghiChu != null) {
                        if (ghiChu.contains("Lưu nháp") || ghiChu.contains("Đang xử lý")) trangThai = "Đang xử lý";
                        else if (ghiChu.contains("Đã hủy")) trangThai = "Đã hủy";
                    }
                    
                    ds.add(new Object[]{maHD, ngay, tenKH, sdt, pt, tongTien, trangThai});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    public HoaDon timHoaDonTheoMa(String maHD) {
        HoaDon hd = null;
        // Query lấy thông tin hóa đơn và tính tổng tiền từ bảng ChiTiet
        String sql = "SELECT hd.*, kh.hoVaTen, " +
                     "(SELECT SUM(ct.soLuong * dv.gia) FROM ChiTietHoaDon ct " +
                     " JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id AND ct.sanPhamId = dv.sanPhamId " +
                     " WHERE ct.hoaDonId = hd.id) as tongTien " +
                     "FROM HoaDon hd " +
                     "LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id " +
                     "WHERE hd.id = ?";

        // Đưa Connection ra ngoài
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, maHD);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    hd = new HoaDon();
                    // FIX: Sử dụng đúng hàm setId() và setNgayLapHD() từ Entity
                    hd.setId(rs.getString("id"));
                    if (rs.getTimestamp("ngayLapHD") != null) {
                        hd.setNgayLapHD(rs.getTimestamp("ngayLapHD").toLocalDateTime());
                    }
                    
                    // Khởi tạo đối tượng khách hàng
                    KhachHang kh = new KhachHang();
                    kh.setHoVaTen(rs.getString("hoVaTen") != null ? rs.getString("hoVaTen") : "Khách lẻ");
                    hd.setKhachHangId(kh); // FIX: Đúng tên hàm setKhachHangId
                    
                    // Lưu tạm tổng tiền vào ghi chú hoặc xử lý riêng tùy logic của bạn
                    hd.setGhiChu(String.valueOf(rs.getDouble("tongTien"))); 
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hd;
    }

    public boolean themHoaDon(HoaDon hd) {
        String sql = "INSERT INTO HoaDon (id, loaiHD, ghiChu, ngayLapHD, nhanVienId, khachHangId, khuyenMaiId, phuongThucThanhToan, hoaDonGocId) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int n = 0;
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, hd.getId());
            
            // FIX LỖI: Bảo vệ an toàn chống dội Check Constraint của Database
            String loaiStr = hd.getLoaiHD().name();
            if (!loaiStr.equals("BAN_HANG") && !loaiStr.equals("TRA_HANG") && !loaiStr.equals("DOI_HANG")) {
                loaiStr = "TRA_HANG"; // Ép về chuẩn nếu code cũ truyền sai Enum
            }
            pst.setString(2, loaiStr);
            
            pst.setString(3, hd.getGhiChu());
            pst.setTimestamp(4, Timestamp.valueOf(hd.getNgayLapHD()));
            pst.setString(5, hd.getNhanVienId().getNhanVien());

            if (hd.getKhachHangId() != null) pst.setString(6, hd.getKhachHangId().getId());
            else pst.setNull(6, java.sql.Types.NVARCHAR);

            if (hd.getKhuyenMaiId() != null) pst.setString(7, hd.getKhuyenMaiId().getId());
            else pst.setNull(7, java.sql.Types.NVARCHAR);

            // FIX LỖI: Chặn phương thức thanh toán sai chuẩn
            String pt = (hd.getPhuongThucThanhToan() != null) ? hd.getPhuongThucThanhToan().name() : "TIEN_MAT";
            if (!pt.equals("TIEN_MAT") && !pt.equals("CHUYEN_KHOAN_NGAN_HANG")) pt = "TIEN_MAT";
            pst.setString(8, pt);

            if (hd.getHoaDonGocId() != null) pst.setString(9, hd.getHoaDonGocId().getId());
            else pst.setNull(9, java.sql.Types.NVARCHAR);

            n = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public HoaDon layHoaDonTheoMa(String maHD) {
        HoaDon hd = null;
        // JOIN NhanVien để lấy đúng tên NV đã tạo hóa đơn
        String sql = "SELECT hd.*, nv.hoVaTen AS tenNhanVien " +
                     "FROM HoaDon hd " +
                     "LEFT JOIN NhanVien nv ON hd.nhanVienId = nv.id " +
                     "WHERE hd.id = ?";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maHD);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    hd = new HoaDon();
                    hd.setId(rs.getString("id"));
                    hd.setGhiChu(rs.getString("ghiChu"));

                    if (rs.getString("loaiHD") != null) {
                        hd.setLoaiHD(LoaiHoaDon.valueOf(rs.getString("loaiHD")));
                    }

                    if (rs.getTimestamp("ngayLapHD") != null) {
                        hd.setNgayLapHD(rs.getTimestamp("ngayLapHD").toLocalDateTime());
                    }

                    if (rs.getString("nhanVienId") != null) {
                        NhanVien nv = new NhanVien();
                        nv.setNhanVien(rs.getString("nhanVienId"));
                        // Lấy đúng tên NV từ JOIN — không phụ thuộc UserSession
                        nv.setHoVaTen(rs.getString("tenNhanVien"));
                        hd.setNhanVienId(nv);
                    }

                    if (rs.getString("khachHangId") != null) {
                        KhachHang kh = new KhachHang();
                        kh.setId(rs.getString("khachHangId"));
                        hd.setKhachHangId(kh);
                    }

                    if (rs.getString("khuyenMaiId") != null) {
                        KhuyenMai km = new KhuyenMai();
                        km.setId(rs.getString("khuyenMaiId"));
                        hd.setKhuyenMaiId(km);
                    }

                    if (rs.getString("phuongThucThanhToan") != null) {
                        hd.setPhuongThucThanhToan(
                            PhuongThucThanhToan.valueOf(rs.getString("phuongThucThanhToan"))
                        );
                    }

                    if (rs.getString("hoaDonGocId") != null) {
                        HoaDon hdGoc = new HoaDon();
                        hdGoc.setId(rs.getString("hoaDonGocId"));
                        hd.setHoaDonGocId(hdGoc);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return hd;
    }

    public ArrayList<HoaDon> getDanhSachHoaDonLuuNhap() {
        ArrayList<HoaDon> dsHoaDonNhap = new ArrayList<>();
        
        String sql = "SELECT * FROM HoaDon WHERE ghiChu = N'Lưu nháp'"; 
        
        // Đưa Connection ra ngoài
        Connection con = ConnectDB.getInstance().getConnection(); 
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String maHD = rs.getString("id");
                String maNV = rs.getString("nhanVienId");
                String maKH = rs.getString("khachHangId");
                
                java.sql.Timestamp sqlTimestamp = rs.getTimestamp("ngayLapHD");
                LocalDateTime ngayLap = (sqlTimestamp != null) ? sqlTimestamp.toLocalDateTime() : null;
                
                NhanVien nv = new NhanVien();
                if(maNV != null) nv.setNhanVien(maNV); 
                
                KhachHang kh = new KhachHang();
                if(maKH != null) kh.setId(maKH);
                
                HoaDon hd = new HoaDon();
                hd.setId(maHD);
                hd.setNgayLapHD(ngayLap);
                hd.setNhanVienId(nv);
                
                if (maKH != null) {
                    hd.setKhachHangId(kh);
                }
                
                if (rs.getString("loaiHD") != null) {
                    hd.setLoaiHD(LoaiHoaDon.valueOf(rs.getString("loaiHD")));
                }
                hd.setGhiChu(rs.getString("ghiChu"));
                
                dsHoaDonNhap.add(hd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsHoaDonNhap;
    }

    public List<HoaDon> layTatCaHoaDon() {
        List<HoaDon> dsHD = new java.util.ArrayList<>();
        try {
            // Đưa Connection ra ngoài và xóa Statement/ResultSet khỏi try để không bị auto close nếu có lỗi
            java.sql.Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT id, ngayLapHD FROM HoaDon"; 
            
            try(java.sql.PreparedStatement pst = con.prepareStatement(sql);
                java.sql.ResultSet rs = pst.executeQuery()){
                
                while (rs.next()) {
                    HoaDon hd = new HoaDon();
                    hd.setId(rs.getString("id"));
                    if (rs.getTimestamp("ngayLapHD") != null) {
                        hd.setNgayLapHD(rs.getTimestamp("ngayLapHD").toLocalDateTime());
                    }
                    dsHD.add(hd);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsHD;
    }

    public boolean capNhatTrangThai(String idHD, LoaiHoaDon loaiMoi) {
        String sql = "UPDATE HoaDon SET loaiHD = ? WHERE id = ?";
        // Đã đúng, không có try(Connection) ở đây
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, loaiMoi.name());
            pst.setString(2, idHD);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    public boolean luuGiaoDichThanhToan(HoaDon hd, List<ChiTietHoaDon> dsCTHD,
                                        DAO_ChiTietHoaDon daoCTHD,
                                        DAO_LoHang daoLo,
                                        DAO_PhanBoLoHang daoPB) {
        Connection con = null;
        try {
            con = ConnectDB.getInstance().getConnection();
            con.setAutoCommit(false);

            // Xóa nháp cũ nếu tồn tại
            try (PreparedStatement pDel1 = con.prepareStatement("DELETE FROM ChiTietHoaDon WHERE hoaDonId = ?");
                 PreparedStatement pDel2 = con.prepareStatement("DELETE FROM HoaDon WHERE id = ?")) {
                pDel1.setString(1, hd.getId()); pDel1.executeUpdate();
                pDel2.setString(1, hd.getId()); pDel2.executeUpdate();
            } catch (Exception ignored) {}

            if (!themHoaDon(con, hd)) throw new Exception("Lỗi lưu hóa đơn");

            for (ChiTietHoaDon ct : dsCTHD) {
                if (!daoCTHD.themCTHD(con, ct)) throw new Exception("Lỗi lưu chi tiết");

                List<LoHang> dsLo = daoLo.layLoTheoSP(con, ct.getSanPhamId().getId());
                int canLay = ct.getSoLuong();

                for (LoHang lh : dsLo) {
                    if (canLay <= 0) break;
                    int layDuoc = Math.min(lh.getSoLuongLoHang(), canLay);

                    daoPB.themPhanBo(con, new PhanBoLoHang(hd, ct.getDonViDoLuongId(), ct.getSanPhamId(), lh, layDuoc));
                    daoLo.capNhatSoLuongVaTrangThaiLo(con, lh.getId(), lh.getSoLuongLoHang() - layDuoc);
                    canLay -= layDuoc;
                }
                if (canLay > 0) throw new Exception("Kho không đủ hàng: " + ct.getSanPhamId().getId());
            }

            con.commit();
            return true;
        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean themHoaDon(Connection con, HoaDon hd) throws SQLException {
        String sql = "INSERT INTO HoaDon (id, loaiHD, ghiChu, ngayLapHD, nhanVienId, khachHangId, khuyenMaiId, phuongThucThanhToan, hoaDonGocId) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, hd.getId());
            
            String loaiStr = hd.getLoaiHD().name();
            if (!loaiStr.equals("BAN_HANG") && !loaiStr.equals("TRA_HANG") && !loaiStr.equals("DOI_HANG")) {
                loaiStr = "TRA_HANG"; 
            }
            pst.setString(2, loaiStr);
            
            pst.setString(3, hd.getGhiChu());
            pst.setTimestamp(4, Timestamp.valueOf(hd.getNgayLapHD()));
            pst.setString(5, hd.getNhanVienId().getNhanVien());

            if (hd.getKhachHangId() != null) pst.setString(6, hd.getKhachHangId().getId());
            else pst.setNull(6, java.sql.Types.NVARCHAR);

            if (hd.getKhuyenMaiId() != null) pst.setString(7, hd.getKhuyenMaiId().getId());
            else pst.setNull(7, java.sql.Types.NVARCHAR);

            String pt = (hd.getPhuongThucThanhToan() != null) ? hd.getPhuongThucThanhToan().name() : "TIEN_MAT";
            if (!pt.equals("TIEN_MAT") && !pt.equals("CHUYEN_KHOAN_NGAN_HANG")) pt = "TIEN_MAT";
            pst.setString(8, pt);

            if (hd.getHoaDonGocId() != null) pst.setString(9, hd.getHoaDonGocId().getId());
            else pst.setNull(9, java.sql.Types.NVARCHAR);

            return pst.executeUpdate() > 0;
        }
    }
 // Thêm vào class DAO_HoaDon.java
    public boolean capNhatTrangThaiVaGhiChu(String maPhieu, String trangThaiMoi, String ghiChuMoi) {
        // Logic: Cập nhật cột ghiChu để các hàm layDanhSach có thể nhận diện trạng thái
        String sql = "UPDATE HoaDon SET ghiChu = ? WHERE id = ?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, trangThaiMoi + " | " + ghiChuMoi);
            pst.setString(2, maPhieu);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Object[]> layDanhSachPhieuDoiTra() {
        List<Object[]> list = new ArrayList<>();
        // CHỈ LẤY CÁC PHIẾU LÀ ĐỔI HOẶC TRẢ HÀNG
        String sql = "SELECT hd.id, hd.hoaDonGocId, kh.hoVaTen, hd.loaiHD, hd.ghiChu, hd.ngayLapHD " +
                     "FROM HoaDon hd " +
                     "LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id " +
                     "WHERE hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') " +
                     "ORDER BY hd.ngayLapHD DESC";
        
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
             
             // ĐÃ FIX: Giữ nguyên Giờ/Phút/Giây để giao diện có thể so sánh với Giờ bắt đầu ca
             DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
             
             while (rs.next()) {
                 String maPhieu = rs.getString("id");
                 String hdGoc = rs.getString("hoaDonGocId");
                 String khach = rs.getString("hoVaTen") != null ? rs.getString("hoVaTen") : "Khách lẻ";
                 String loaiHD = rs.getString("loaiHD").equals("TRA_HANG") ? "Trả hàng" : "Đổi hàng";
                 String ghiChuDB = rs.getString("ghiChu"); 
                 
                 // Lấy đầy đủ ngày giờ
                 String ngay = rs.getTimestamp("ngayLapHD").toLocalDateTime().format(dtf);
                 
                 list.add(new Object[]{maPhieu, hdGoc, khach, loaiHD, ghiChuDB, ngay});
             }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean capNhatTrangThaiPhieuDoiTra(String maPhieu, String trangThaiMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        String ghiChuHienTai = "";

        // BƯỚC 1: LẤY GHI CHÚ HIỆN TẠI TỪ DB LÊN
        String sqlSelect = "SELECT ghiChu FROM HoaDon WHERE id = ?";
        try (PreparedStatement pstSelect = con.prepareStatement(sqlSelect)) {
            pstSelect.setString(1, maPhieu);
            try (ResultSet rs = pstSelect.executeQuery()) {
                if (rs.next()) {
                    ghiChuHienTai = rs.getString("ghiChu");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // BƯỚC 2: DÙNG JAVA ĐỂ TÁCH VÀ GẮN LẠI TRẠNG THÁI MỚI (CHẮC CHẮN 100%)
        if (ghiChuHienTai == null || ghiChuHienTai.isEmpty()) return false;

        // Split chuỗi ra bằng dấu "|" 
        String[] parts = ghiChuHienTai.split("\\|");
        if (parts.length > 0) {
            // Thay thế phần tử đầu tiên (Trạng thái) bằng trạng thái mới
            parts[0] = trangThaiMoi + " ";
        }
        
        // Nối chuỗi lại
        String ghiChuMoi = String.join("|", parts);

        // BƯỚC 3: UPDATE GHI CHÚ MỚI VÀO LẠI DB
        String sqlUpdate = "UPDATE HoaDon SET ghiChu = ? WHERE id = ?";
        try (PreparedStatement pstUpdate = con.prepareStatement(sqlUpdate)) {
            pstUpdate.setString(1, ghiChuMoi);
            pstUpdate.setString(2, maPhieu);
            return pstUpdate.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public Object[] layThongTinGiaTuHDGoc(String maHDGoc, String tenSP) {
        String dvt = "Hộp";
        double giaGocHienTai = 0.0, thueVAT = 0.0;
        
        try (java.sql.Connection con = ConnectDB.getInstance().getConnection()) {
            // 1. Lấy giá niêm yết và VAT của SP
            String sql1 = "SELECT dv.ten, dv.gia, ISNULL(sp.thueVAT, 0) as vat FROM ChiTietHoaDon ct JOIN SanPham sp ON ct.sanPhamId = sp.id JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id AND ct.sanPhamId = dv.sanPhamId WHERE ct.hoaDonId = ? AND sp.ten = ?";
            try (java.sql.PreparedStatement pst1 = con.prepareStatement(sql1)) {
                pst1.setString(1, maHDGoc); pst1.setString(2, tenSP);
                try (java.sql.ResultSet rs1 = pst1.executeQuery()) {
                    if (rs1.next()) { dvt = rs1.getString("ten"); giaGocHienTai = rs1.getDouble("gia"); thueVAT = rs1.getDouble("vat"); }
                }
            }
            if (giaGocHienTai <= 0) return new Object[]{dvt, 0.0};
            
            // Tính giá đã gồm VAT (VD: 650k + 10% = 715k)
            double giaSPCoVAT = giaGocHienTai * (1 + (thueVAT / 100.0));
            
            // 2. Lấy TỔNG TIỀN của nguyên Hóa đơn & GHI CHÚ để bóc tách Voucher
            double tongTienGocCuaHD = 0;
            String ghiChuHD = "";
            String sql2 = "SELECT hd.ghiChu, SUM(ct.soLuong * dv.gia * (1 + ISNULL(sp.thueVAT, 0)/100.0)) as tong FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId JOIN SanPham sp ON ct.sanPhamId = sp.id JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id AND ct.sanPhamId = dv.sanPhamId WHERE hd.id = ? GROUP BY hd.ghiChu";
            try (java.sql.PreparedStatement pst2 = con.prepareStatement(sql2)) {
                pst2.setString(1, maHDGoc);
                try (java.sql.ResultSet rs2 = pst2.executeQuery()) {
                    if (rs2.next()) { ghiChuHD = rs2.getString("ghiChu"); tongTienGocCuaHD = rs2.getDouble("tong"); }
                }
            }
            
            // 3. Quét Ghi chú y hệt như form ChiTietHoaDon để tìm đúng số tiền 235.950đ
            double tongTienGiam = 0;
            if (ghiChuHD != null && !ghiChuHD.isEmpty()) {
                String[] parts = ghiChuHD.split("\\|");
                for (String p : parts) {
                    p = p.trim();
                    if (p.startsWith("Dùng điểm: -")) {
                        try { tongTienGiam += Double.parseDouble(p.substring(12).replaceAll("[^0-9]", "")); } catch(Exception e){}
                    } else if (p.startsWith("KM:")) {
                        String[] mks = p.substring(3).trim().split(",");
                        for (String mk : mks) {
                            String sql3 = "SELECT loaiHinhThuc, giaTri FROM HinhThucKhuyenMai WHERE khuyenMaiId = ?";
                            try (java.sql.PreparedStatement pst3 = con.prepareStatement(sql3)) {
                                pst3.setString(1, mk.trim());
                                try (java.sql.ResultSet rs3 = pst3.executeQuery()) {
                                    if (rs3.next()) {
                                        String loaiKM = rs3.getString("loaiHinhThuc");
                                        double val = rs3.getDouble("giaTri");
                                        if (loaiKM.contains("PHAN_TRAM") || loaiKM.contains("%")) {
                                            tongTienGiam += tongTienGocCuaHD * (val / 100.0);
                                        } else if (loaiKM.contains("TIEN_MAT")) {
                                            tongTienGiam += val;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // 4. Khấu trừ tiền giảm giá vào SP (Chia đều phần trăm)
            double giaThucTe = giaSPCoVAT;
            if (tongTienGocCuaHD > 0 && tongTienGiam > 0) {
                double tyLeDongGop = giaSPCoVAT / tongTienGocCuaHD;
                double giamChoSpNay = tongTienGiam * tyLeDongGop;
                giaThucTe = giaSPCoVAT - giamChoSpNay;
            }
            
            return new Object[]{dvt, giaThucTe};
            
        } catch (Exception e) { e.printStackTrace(); }
        return new Object[]{dvt, 0.0};
    } 
}