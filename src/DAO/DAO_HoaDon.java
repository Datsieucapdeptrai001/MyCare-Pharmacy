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

 // File: DAO_HoaDon.java
    public List<Object[]> layDanhSachHoaDonRaw() {
        List<Object[]> ds = new ArrayList<>();
        // SQL lấy Tổng tiền chưa thuế và Tổng tiền thuế riêng biệt
        String sql = "SELECT hd.id, hd.loaiHD, hd.ngayLapHD, kh.hoVaTen, kh.sdt, hd.phuongThucThanhToan, hd.ghiChu, " +
                "(SELECT SUM(ct.soLuong * dv.gia) FROM ChiTietHoaDon ct " + 
                " JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id AND ct.sanPhamId = dv.sanPhamId " +
                " WHERE ct.hoaDonId = hd.id) as tongTienChuaThue, " +
                "(SELECT SUM(ct.soLuong * dv.gia * (ISNULL(sp.thueVAT, 0) / 100.0)) FROM ChiTietHoaDon ct " + 
                " JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id AND ct.sanPhamId = dv.sanPhamId " +
                " JOIN SanPham sp ON ct.sanPhamId = sp.id WHERE ct.hoaDonId = hd.id) as tongTienThue " +
                "FROM HoaDon hd LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id ORDER BY hd.ngayLapHD DESC"; 
                     
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                ds.add(new Object[]{
                    rs.getString("id"), rs.getString("loaiHD"), rs.getTimestamp("ngayLapHD"),
                    rs.getString("hoVaTen"), rs.getString("sdt"), rs.getString("phuongThucThanhToan"),
                    rs.getString("ghiChu"), rs.getDouble("tongTienChuaThue"), rs.getDouble("tongTienThue")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    // 2. Hàm phụ trợ để BUS gọi lấy thông tin Khuyến Mãi (Không viết SQL dính vào BUS)
    public double[] layThongTinKhuyenMai(String maKM) {
        String sqlKM = "SELECT loaiHinhThuc, giaTri FROM HinhThucKhuyenMai WHERE khuyenMaiId = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pstKM = con.prepareStatement(sqlKM)) {
            pstKM.setString(1, maKM.trim());
            try (ResultSet rsKM = pstKM.executeQuery()) {
                if (rsKM.next()) {
                    String loaiKM = rsKM.getString("loaiHinhThuc");
                    double val = rsKM.getDouble("giaTri");
                    // Trả về mảng: Index 0 là loại (1 = %, 2 = Tiền mặt), Index 1 là Giá trị
                    if (loaiKM.contains("PHAN_TRAM") || loaiKM.contains("%")) {
                        return new double[]{1.0, val}; 
                    } else if (loaiKM.contains("TIEN_MAT")) {
                        return new double[]{2.0, val}; 
                    }
                }
            }
        } catch (Exception ignored) {}
        return new double[]{0.0, 0.0};
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
    public String phatSinhMaHoaDonTuDong() {
        int year = java.time.Year.now().getValue();
        String prefix = "HD-" + year + "-"; 
        String maMoi = prefix + "1"; 
        
        String sql = "SELECT id FROM HoaDon WHERE id LIKE ?";
        
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
             
            pst.setString(1, prefix + "%");
            
            try (ResultSet rs = pst.executeQuery()) {
                int maxStt = 0;
                while (rs.next()) {
                    String id = rs.getString("id"); // VD: HD-2026-1 hoặc HD-2026-1-LuuNhap
                    if (id != null) {
                        String[] parts = id.split("-");
                        // Lấy phần tử số 3 (index 2) chính là số thứ tự
                        if (parts.length >= 3) {
                            try {
                                int stt = Integer.parseInt(parts[2]);
                                if (stt > maxStt) {
                                    maxStt = stt;
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
                
                // Nếu đã có hóa đơn trong năm, cộng thêm 1 từ số đếm lớn nhất
                if (maxStt > 0) {
                    maMoi = String.format("HD-%d-%d", year, maxStt + 1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maMoi;
    }
    public long tinhDoanhThuTienMatCaHienTai(String maNV, java.time.LocalDateTime thoiGianBatDau) {
        long doanhThuTienMat = 0;
        
        // Hỗ trợ quét cả Enum kiểu số (0, 1) và kiểu chuỗi ('BAN_HANG', 'TRA_HANG')
        String sql = "SELECT " +
                     "ISNULL(SUM(CASE WHEN hd.loaiHD = 'BAN_HANG' OR hd.loaiHD = '0' THEN ct.soLuong * dv.gia ELSE 0 END), 0) - " + 
                     "ISNULL(SUM(CASE WHEN hd.loaiHD = 'TRA_HANG' OR hd.loaiHD = '1' THEN ct.soLuong * dv.gia ELSE 0 END), 0) " +   
                     "FROM ChiTietHoaDon ct " +
                     "JOIN HoaDon hd ON ct.hoaDonId = hd.id " +
                     "JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id " +
                     "WHERE hd.nhanVienId = ? " +
                     "AND (hd.phuongThucThanhToan = 'TIEN_MAT' OR hd.phuongThucThanhToan = '0') " + 
                     "AND hd.ngayLapHD >= ? " +
                     // [FIX] Loại trừ hóa đơn nháp và đã hủy khỏi phép tính doanh thu
                     "AND (hd.ghiChu IS NULL OR (" +
                     "    hd.ghiChu NOT LIKE N'%Lưu nháp%' AND " +
                     "    hd.ghiChu NOT LIKE N'%Đã hủy%'))";
        
        try (java.sql.Connection con = ConnectDB.getInstance().getConnection();
             java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, maNV);
            pst.setTimestamp(2, java.sql.Timestamp.valueOf(thoiGianBatDau));
            
            try (java.sql.ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    doanhThuTienMat = (long) rs.getDouble(1);
                }
            }
        } catch (Exception ex) {
            System.err.println("Lỗi tính tiền trong két DAO: " + ex.getMessage());
        }
        
        return doanhThuTienMat;
    }
    public List<Object[]> layDanhSachHoaDonTheoNVHomNay(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) return null;

        List<Object[]> ds = new ArrayList<>();

        // [FIX] Thay subquery tính lại giá từ đầu (soLuong * gia * VAT)
        // bằng subquery đọc trực tiếp ct.thanhTien — giá trị này đã được
        // lưu đúng sau khi áp dụng KM + VAT tại thời điểm bán hàng.
        // Không cần JOIN DonViDoLuong hay SanPham trong subquery nữa.
        //
        // LƯU Ý: Nếu bảng ChiTietHoaDon CÓ cột loaiChiTiet, hãy bỏ comment
        // dòng điều kiện AND bên dưới để chỉ tính dòng BAN_HANG, tránh
        // cộng nhầm dòng quà tặng/đổi trả.
        String sql =
            "SELECT hd.id, hd.loaiHD, hd.ngayLapHD, kh.hoVaTen, kh.sdt, hd.phuongThucThanhToan, hd.ghiChu, " +
            "(SELECT ISNULL(SUM(ct.thanhTien), 0) " +
            " FROM ChiTietHoaDon ct " +
            " WHERE ct.hoaDonId = hd.id " +
            // " AND ct.loaiChiTiet = 'BAN_HANG' " + // Bỏ comment nếu cột loaiChiTiet tồn tại
            ") as tongThucTe " +
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
                    // [FIX] Đọc tongThucTe thay vì tongTienGoc.
                    // Giá trị này đã bao gồm VAT và đã trừ KM từng dòng.
                    double totalAmount = rs.getDouble("tongThucTe");
                    String ghiChu = rs.getString("ghiChu");
                    String loaiHD = rs.getString("loaiHD");

                    // [FIX] Bỏ toàn bộ logic đọc KM từ ghiChu (nguồn gốc
                    // gây lỗi tính sai) vì KM đã nằm trong ct.thanhTien rồi.
                    // Chỉ giữ lại phần trừ "Dùng điểm" — đây là khoản giảm
                    // bổ sung bên ngoài thanhTien, áp trực tiếp lên tổng HĐ.
                    if (ghiChu != null && !ghiChu.isEmpty()) {
                        for (String p : ghiChu.split("\\|")) {
                            p = p.trim();
                            if (p.startsWith("Dùng điểm: -")) {
                                try {
                                    totalAmount -= Long.parseLong(p.replaceAll("[^0-9]", ""));
                                } catch (Exception ignored) {}
                            }
                        }
                    }
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
        
        // [FIX] Thay SUM(soLuong * gia) + GROUP BY + JOIN DonViDoLuong
        // bằng subquery đọc ct.thanhTien trực tiếp — nhất quán với
        // layDanhSachHoaDonTheoNVHomNay. Bỏ JOIN DonViDoLuong và GROUP BY
        // vì không còn cần tính tay nữa. Kết quả đã bao gồm VAT + KM đúng.
        //
        // LƯU Ý: Nếu bảng ChiTietHoaDon CÓ cột loaiChiTiet, bỏ comment
        // dòng điều kiện AND bên dưới.
        String sql = "SELECT hd.id, hd.loaiHD, hd.ngayLapHD, kh.hoVaTen, kh.sdt, hd.phuongThucThanhToan, hd.ghiChu, " +
                     "(SELECT ISNULL(SUM(ct.thanhTien), 0) " +
                     " FROM ChiTietHoaDon ct " +
                     " WHERE ct.hoaDonId = hd.id " +
                     // " AND ct.loaiChiTiet = 'BAN_HANG' " + // Bỏ comment nếu cột loaiChiTiet tồn tại
                     ") as tongThucTe " +
                     "FROM HoaDon hd " +
                     "LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id " +
                     "WHERE hd.nhanVienId = ? " +
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
                    
                    // [FIX] Đọc tongThucTe thay vì tongTien (alias cũ đã bị xóa)
                    String tongTien = df.format(rs.getDouble("tongThucTe"));
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
            
            if (hd.getNhanVienId() != null) {
                // Đảm bảo entity NhanVien có hàm getId() hoặc dùng trường chứa mã (VD: NV2024001)
                pst.setString(5, hd.getNhanVienId().getNhanVien()); 
            } else {
                pst.setNull(5, java.sql.Types.VARCHAR);
            }
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
    public boolean luuGiaoDichThanhToan(HoaDon hd, List<ChiTietHoaDon> dsCTHD, List<ChiTietHoaDon> dsQuaTang,
            DAO_ChiTietHoaDon daoCTHD,
            DAO_LoHang daoLo,
            DAO_PhanBoLoHang daoPB) {
Connection con = null;
try {
con = ConnectDB.getInstance().getConnection();
con.setAutoCommit(false);

// Xóa nháp cũ theo đúng thứ tự FK: PhanBoLoHang → ChiTietHoaDon → HoaDon
// (Nếu xóa ChiTietHoaDon trước khi xóa PhanBoLoHang sẽ bị lỗi FK bị bắt im lặng, khiến bản ghi cũ còn lại)
try (PreparedStatement pDelPB  = con.prepareStatement("DELETE FROM PhanBoLoHang WHERE hoaDonId = ?");
     PreparedStatement pDelCT  = con.prepareStatement("DELETE FROM ChiTietHoaDon WHERE hoaDonId = ?");
     PreparedStatement pDelHD  = con.prepareStatement("DELETE FROM HoaDon WHERE id = ?")) {
    pDelPB.setString(1, hd.getId());  pDelPB.executeUpdate();
    pDelCT.setString(1, hd.getId());  pDelCT.executeUpdate();
    pDelHD.setString(1, hd.getId());  pDelHD.executeUpdate();
} catch (Exception eDel) {
    // Ghi log để dễ debug nếu có vấn đề với việc xóa nháp
    System.err.println("[luuGiaoDich] Xóa nháp cũ: " + eDel.getMessage());
}

if (!themHoaDon(con, hd)) throw new Exception("Lỗi lưu hóa đơn");

// ==========================================
// BƯỚC GỘP: GỘP QUÀ TẶNG VÀO SẢN PHẨM MUA ĐỂ CHỐNG LỖI TRÙNG KHÓA CHÍNH (PK)
// ==========================================
List<ChiTietHoaDon> dsTongGop = new ArrayList<>();

java.util.function.Consumer<ChiTietHoaDon> addOrMerge = (newItem) -> {
    for (ChiTietHoaDon existing : dsTongGop) {
        if (existing.getSanPhamId().getId().trim().equals(newItem.getSanPhamId().getId().trim()) &&
            existing.getDonViDoLuongId().getId().trim().equals(newItem.getDonViDoLuongId().getId().trim())) {
            existing.setSoLuong(existing.getSoLuong() + newItem.getSoLuong());
            return;
        }
    }
    dsTongGop.add(newItem);
};

if (dsCTHD != null) {
    for (ChiTietHoaDon ct : dsCTHD) {
        addOrMerge.accept(ct);
    }
}
if (dsQuaTang != null) {
    for (ChiTietHoaDon qt : dsQuaTang) {
        addOrMerge.accept(qt);
    }
}

// 1. LƯU TẤT CẢ CHI TIẾT HÓA ĐƠN (ĐÃ GỘP) VÀO DATABASE
for (ChiTietHoaDon ct : dsTongGop) {
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
    public boolean capNhatTrangThaiVaGhiChu(String maPhieu, String trangThaiMoi, String ghiChuMoi) {
        String sql = "UPDATE HoaDon SET ghiChu = ? WHERE id = ?";
        // Đưa Connection vào khối try()
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
             
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
    
    /**
     * Khi duyệt "Hoàn thành" phiếu ĐỔI HÀNG:
     * - Xuất kho hàng mới (soLuong > 0) → tạo PhanBoLoHang, trừ LoHang
     * - Nhập lại kho hàng trả (soLuong < 0) → cộng lại LoHang gốc
     * Không làm gì với TRA_HANG (không có hàng xuất mới).
     */
    public boolean xuatKhoKhiDuyetDoiHang(String maPhieu) {
        Connection con = null;
        try {
            con = ConnectDB.getInstance().getConnection();
            con.setAutoCommit(false);

            // 1. Kiểm tra phiếu có phải DOI_HANG không
            String loaiHD = "";
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT loaiHD, hoaDonGocId FROM HoaDon WHERE id = ?")) {
                ps.setString(1, maPhieu);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) loaiHD = rs.getString("loaiHD");
                }
            }
            // DOI_HANG: soLuong > 0 → xuất mới, soLuong < 0 → cộng lại kho
            // TRA_HANG: chỉ cộng lại kho (soLuong < 0), không xuất mới
            boolean isTRAHang = "TRA_HANG".equals(loaiHD);
            if (!isTRAHang && !"DOI_HANG".equals(loaiHD)) {
                con.setAutoCommit(true);
                return true;
            }

            // 2. Lấy tất cả chi tiết phiếu đổi
            List<Object[]> dsCT = new ArrayList<>(); // [sanPhamId, donViDoLuongId, soLuong]
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT sanPhamId, donViDoLuongId, soLuong FROM ChiTietHoaDon WHERE hoaDonId = ?")) {
                ps.setString(1, maPhieu);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        dsCT.add(new Object[]{
                            rs.getString("sanPhamId"),
                            rs.getString("donViDoLuongId"),
                            rs.getInt("soLuong")
                        });
                    }
                }
            }

            DAO_LoHang daoLo = new DAO_LoHang();
            DAO_PhanBoLoHang daoPB = new DAO_PhanBoLoHang();

            for (Object[] ct : dsCT) {
                String spId   = (String) ct[0];
                String dvlId  = (String) ct[1];
                int    soLuong = (int)    ct[2];

                // Lấy hệ số quy đổi về ĐVCB (LoHang.soLuongLoHang lưu theo ĐVCB)
                int heSo = 1;
                try (PreparedStatement psHeSo = con.prepareStatement(
                        "SELECT chuyenDoiDonViCoBan FROM DonViDoLuong WHERE id = ? AND sanPhamId = ?")) {
                    psHeSo.setString(1, dvlId);
                    psHeSo.setString(2, spId);
                    try (ResultSet rsHeSo = psHeSo.executeQuery()) {
                        if (rsHeSo.next()) heSo = (int) rsHeSo.getDouble(1);
                    }
                }

                if (soLuong > 0 && !isTRAHang) {
                    // XUẤT KHO hàng mới: chỉ DOI_HANG mới xuất hàng mới cho khách
                    // canLayCoBan tính theo ĐVCB để so sánh/trừ đúng với LoHang.soLuongLoHang
                    List<LoHang> dsLo = daoLo.layLoTheoSP(con, spId);
                    int canLayCoBan = soLuong * heSo;
                    for (LoHang lh : dsLo) {
                        if (canLayCoBan <= 0) break;
                        int layDuocCoBan = Math.min(lh.getSoLuongLoHang(), canLayCoBan);
                        HoaDon hdRef = new HoaDon(); hdRef.setId(maPhieu);
                        Entity.DonViDoLuong dvl = new Entity.DonViDoLuong(); dvl.setId(dvlId);
                        Entity.SanPham sp = new Entity.SanPham(); sp.setId(spId);
                        // PhanBoLoHang.soLuong lưu theo đơn vị bán (để tính COGS: pbl.soLuong * dvl.chuyenDoi * lh.gia)
                        int pblSoLuong = (heSo > 0) ? layDuocCoBan / heSo : layDuocCoBan;
                        daoPB.themPhanBo(con, new PhanBoLoHang(hdRef, dvl, sp, lh, pblSoLuong));
                        daoLo.capNhatSoLuongVaTrangThaiLo(con, lh.getId(), lh.getSoLuongLoHang() - layDuocCoBan);
                        canLayCoBan -= layDuocCoBan;
                    }
                    if (canLayCoBan > 0) throw new Exception("Kho không đủ hàng để xuất đổi: " + spId);

                } else if (soLuong < 0) {
                    // NHẬP LẠI KHO hàng trả: cộng lại theo ĐVCB
                    int soLuongTraCoBan = Math.abs(soLuong) * heSo;
                    List<LoHang> dsLo = daoLo.layLoTheoSP(con, spId);
                    if (!dsLo.isEmpty()) {
                        LoHang loGoc = dsLo.get(0);
                        daoLo.capNhatSoLuongVaTrangThaiLo(con, loGoc.getId(),
                                loGoc.getSoLuongLoHang() + soLuongTraCoBan);
                    }
                }
            }

            con.commit();
            return true;
        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (Exception e) { e.printStackTrace(); }
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
    public boolean thanhToanToanDien(HoaDon hd, List<ChiTietHoaDon> dsCTHD, List<ChiTietHoaDon> dsQuaTang, 
            String maHDDangSua, KhachHang kh, int diemChenhLech) {
    		Connection con = ConnectDB.getInstance().getConnection();
    		if (con == null) return false;

    		try {
    			con.setAutoCommit(false); // BẮT ĐẦU TRANSACTION - Sống cùng sống, chết cùng chết

    			// 1. Dọn dẹp hóa đơn nháp (nếu đang sửa)
    			if (maHDDangSua != null && !maHDDangSua.isEmpty()) {
    				try (PreparedStatement pstDelCT = con.prepareStatement("DELETE FROM ChiTietHoaDon WHERE hoaDonId = ?")) {
    					pstDelCT.setString(1, maHDDangSua);
    					pstDelCT.executeUpdate();
    				}
    				try (PreparedStatement pstDelHD = con.prepareStatement("DELETE FROM HoaDon WHERE id = ?")) {
    					pstDelHD.setString(1, maHDDangSua);
    					pstDelHD.executeUpdate();
    				}
    			}

// 2. Insert Hóa Đơn (Tự viết câu lệnh Insert của ông vào đây)
    			String sqlInsertHD = "INSERT INTO HoaDon (id, loaiHD, ngayLapHD, nhanVienId, khachHangId, phuongThucThanhToan, khuyenMaiId, ghiChu) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
				try (PreparedStatement pstHD = con.prepareStatement(sqlInsertHD)) {
					pstHD.setString(1, hd.getId());
					pstHD.setString(2, hd.getLoaiHD().toString());
					pstHD.setTimestamp(3, Timestamp.valueOf(hd.getNgayLapHD()));
					pstHD.setString(4, hd.getNhanVienId().getNhanVien());
					pstHD.setString(5, hd.getKhachHangId() != null ? hd.getKhachHangId().getId() : null);
					pstHD.setString(6, hd.getPhuongThucThanhToan().toString());
					pstHD.setString(7, hd.getKhuyenMaiId() != null ? hd.getKhuyenMaiId().getId() : null);
					pstHD.setString(8, hd.getGhiChu());
					pstHD.executeUpdate();
				}

// 3. Insert Chi Tiết Hóa Đơn và Quà Tặng (Đã gộp trùng lặp)
				String sqlInsertCT = "INSERT INTO ChiTietHoaDon (hoaDonId, sanPhamId, donViDoLuongId, soLuong, donGiaThucTe, thanhTien) VALUES (?, ?, ?, ?, ?, ?)";
				try (PreparedStatement pstCT = con.prepareStatement(sqlInsertCT)) {
                    List<ChiTietHoaDon> dsTongGop = new ArrayList<>();
                    java.util.function.Consumer<ChiTietHoaDon> addOrMerge = (newItem) -> {
                        for (ChiTietHoaDon existing : dsTongGop) {
                            if (existing.getSanPhamId().getId().trim().equals(newItem.getSanPhamId().getId().trim()) &&
                                existing.getDonViDoLuongId().getId().trim().equals(newItem.getDonViDoLuongId().getId().trim())) {
                                existing.setSoLuong(existing.getSoLuong() + newItem.getSoLuong());
                                return;
                            }
                        }
                        dsTongGop.add(newItem);
                    };

                    if (dsCTHD != null) {
                        for (ChiTietHoaDon ct : dsCTHD) {
                            addOrMerge.accept(ct);
                        }
                    }
                    if (dsQuaTang != null) {
                        for (ChiTietHoaDon qt : dsQuaTang) {
                            addOrMerge.accept(qt);
                        }
                    }
					for (ChiTietHoaDon ct : dsTongGop) {
						double donGiaThucTe = ct.getDonGiaThucTe();
						double thanhTien = donGiaThucTe * ct.getSoLuong();
						pstCT.setString(1, hd.getId());
						pstCT.setString(2, ct.getSanPhamId().getId());
						pstCT.setString(3, ct.getDonViDoLuongId().getId());
						pstCT.setInt(4, ct.getSoLuong());
						pstCT.setDouble(5, donGiaThucTe);
						pstCT.setDouble(6, thanhTien);
						pstCT.addBatch();
					}
					pstCT.executeBatch();
				}

				// 4. Cập nhật điểm Khách Hàng (nếu có)
				if (kh != null && kh.getSdt() != null && diemChenhLech != 0) {
					String sqlUpdateDiem = "UPDATE KhachHang SET diemTichLuy = ISNULL(diemTichLuy, 0) + ? WHERE sdt = ?";
					try (PreparedStatement pstDiem = con.prepareStatement(sqlUpdateDiem)) {
						pstDiem.setInt(1, diemChenhLech);
						pstDiem.setString(2, kh.getSdt());
						pstDiem.executeUpdate();
					}
				}

				con.commit(); // NẾU MỌI THỨ OK -> LƯU THẬT VÀO DB
				return true;
    		} catch (Exception e) {
    			try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } // LỖI LÀ HỦY TOÀN BỘ
    			e.printStackTrace();
    			return false;
    		} finally {
    			try { con.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
    		}
    }
    public String[] layMaSPVaMaDVT(String tenSP, String tenDVT) {
        String sql = "SELECT sp.id AS MaSP, dv.id AS MaDVT FROM SanPham sp JOIN DonViDoLuong dv ON sp.id = dv.sanPhamId WHERE sp.ten = ? AND dv.ten = ?";
        try (java.sql.Connection con = ConnectDB.getInstance().getConnection();
             java.sql.PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, tenSP);
            pst.setString(2, tenDVT);
            try (java.sql.ResultSet rs = pst.executeQuery()) {
                if(rs.next()) return new String[]{rs.getString("MaSP"), rs.getString("MaDVT")};
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null; 
    }
    public java.util.List<Object[]> layDanhSachKhuyenMaiFull() {
        java.util.List<Object[]> result = new java.util.ArrayList<>();
        String sql = "SELECT k.id, k.tenKhuyenMai, h.moTa, ISNULL(spYeuCau.ten, '') AS tenSanPhamYeuCau " +
                     "FROM KhuyenMai k " +
                     "JOIN HinhThucKhuyenMai h ON k.id = h.khuyenMaiId " +
                     "LEFT JOIN SanPham spYeuCau ON h.spYeuCau = spYeuCau.id " +
                     "WHERE k.trangThai = 1 " + 
                     "AND CAST(k.ngayBatDau AS DATE) <= CAST(GETDATE() AS DATE) " +
                     "AND (k.ngayKetThuc IS NULL OR CAST(k.ngayKetThuc AS DATE) >= CAST(GETDATE() AS DATE))";

        try (java.sql.Connection con = ConnectDB.getInstance().getConnection();
             java.sql.Statement st = con.createStatement();
             java.sql.ResultSet rs = st.executeQuery(sql)) {
             
            while(rs.next()) {
                Object[] row = new Object[6]; 
                row[0] = rs.getString("id");               
                row[1] = rs.getString("tenKhuyenMai");     
                row[2] = rs.getString("moTa");             
                row[3] = ""; 
                row[4] = ""; 
                row[5] = rs.getString("tenSanPhamYeuCau"); 
                
                result.add(row);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public boolean capNhatHoaDon(HoaDon hd) {
        String sql = "UPDATE HoaDon SET khachHangId = ?, ghiChu = ?, ngayLapHD = ?, khuyenMaiId = ? WHERE id = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
             
            // Xử lý Khách Hàng
            if (hd.getKhachHangId() != null && hd.getKhachHangId().getId() != null) {
                pst.setString(1, hd.getKhachHangId().getId());
            } else {
                pst.setNull(1, java.sql.Types.NVARCHAR);
            }
            
            pst.setString(2, hd.getGhiChu());
            pst.setTimestamp(3, java.sql.Timestamp.valueOf(hd.getNgayLapHD()));
            
            // Xử lý Khuyến Mãi
            if (hd.getKhuyenMaiId() != null && hd.getKhuyenMaiId().getId() != null) {
                pst.setString(4, hd.getKhuyenMaiId().getId());
            } else {
                pst.setNull(4, java.sql.Types.NVARCHAR);
            }
            
            pst.setString(5, hd.getId());
            
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật HoaDon: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public boolean huyHoaDon(String maHD) {
        String sql = "UPDATE HoaDon SET ghiChu = N'Đã hủy' WHERE id = ?"; 
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, maHD);
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void xoaHoaDonNhap(String maHD) {
        try (java.sql.Connection con = ConnectDB.getInstance().getConnection()) {
            try (java.sql.PreparedStatement pst1 = con.prepareStatement("DELETE FROM ChiTietHoaDon WHERE hoaDonId = ?")) {
                pst1.setString(1, maHD); pst1.executeUpdate();
            }
            try (java.sql.PreparedStatement pst2 = con.prepareStatement("DELETE FROM HoaDon WHERE id = ?")) {
                pst2.setString(1, maHD); pst2.executeUpdate();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    public long layDoanhThuTienMatTuDauCa(String maNhanVien, java.time.LocalDateTime thoiGianBatDau) {
        String sql = "SELECT " +
                     "ISNULL(SUM(CASE WHEN hd.loaiHD = 0 THEN ct.soLuong * dv.gia ELSE 0 END), 0) - " +
                     "ISNULL(SUM(CASE WHEN hd.loaiHD = 1 THEN ct.soLuong * dv.gia ELSE 0 END), 0) " +
                     "FROM ChiTietHoaDon ct " +
                     "JOIN HoaDon hd ON ct.hoaDonId = hd.id " +
                     "JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id " +
                     "WHERE hd.nhanVienId = ? " +
                     "AND hd.phuongThucThanhToan = 0 " +
                     "AND hd.ngayLapHD >= ? " +
                     // [FIX] Loại trừ hóa đơn nháp và đã hủy khỏi phép tính tiền két
                     "AND (hd.ghiChu IS NULL OR (" +
                     "    hd.ghiChu NOT LIKE N'%Lưu nháp%' AND " +
                     "    hd.ghiChu NOT LIKE N'%Đã hủy%'))";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maNhanVien);
            pst.setTimestamp(2, java.sql.Timestamp.valueOf(thoiGianBatDau));
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return (long) rs.getDouble(1);
            }
        } catch (Exception e) {
            System.err.println("Lỗi DAO_HoaDon (layDoanhThuTienMatTuDauCa): " + e.getMessage());
        }
        return 0;
    }

    public String layGhiChuHoaDon(String maHD) {
        String sql = "SELECT ghiChu FROM HoaDon WHERE id = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maHD);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getString("ghiChu");
            }
        } catch (Exception e) {
            System.err.println("Lỗi DAO_HoaDon (layGhiChuHoaDon): " + e.getMessage());
        }
        return "";
    }

    public String[] layPhieuDoiTraTheoHDGoc(String maHDGoc) {
        // [FIX] Bỏ qua các phiếu đổi/trả đã bị Từ chối hoặc Đã hủy để cho phép nhân viên tạo lại phiếu mới
        String sql = "SELECT id, loaiHD FROM HoaDon WHERE hoaDonGocId = ? " +
                     "AND (ghiChu IS NULL OR (ghiChu NOT LIKE N'%Từ chối%' AND ghiChu NOT LIKE N'%Đã hủy%'))";
        
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
             
            pst.setString(1, maHDGoc);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new String[]{rs.getString("id"), rs.getString("loaiHD")};
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi DAO_HoaDon (layPhieuDoiTraTheoHDGoc): " + e.getMessage());
        }
        return null;
    }

    public String layTenKhachHangTheoHD(String maHD) {
        String sql = "SELECT kh.hoVaTen FROM HoaDon hd JOIN KhachHang kh ON hd.khachHangId = kh.id WHERE hd.id = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maHD);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String ten = rs.getString("hoVaTen");
                    if (ten != null && !ten.trim().isEmpty()) return ten;
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi DAO_HoaDon (layTenKhachHangTheoHD): " + e.getMessage());
        }
        return "Khách lẻ";
    }

    public java.util.List<String> timKiemMaHoaDonGoiY(String tuKhoa) {
        java.util.List<String> ds = new java.util.ArrayList<>();
        String sql = "SELECT TOP 5 id, ghiChu FROM HoaDon WHERE id LIKE ? AND hoaDonGocId IS NULL";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, "%" + tuKhoa + "%");
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String gc = rs.getString("ghiChu");
                    if (gc == null) gc = "";
                    if (!gc.contains("Lưu nháp") && !gc.contains("Đang xử lý")
                            && !gc.contains("Đã hủy") && !gc.contains("Từ chối")) {
                        ds.add(rs.getString("id"));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi DAO_HoaDon (timKiemMaHoaDonGoiY): " + e.getMessage());
        }
        return ds;
    }
}