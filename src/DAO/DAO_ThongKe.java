package DAO;

import ConnectDB.ConnectDB;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class DAO_ThongKe {

    public DAO_ThongKe() {}

    private Connection getConn() {
        return ConnectDB.getInstance().getConnection();
    }

    // LEGACY METHODS

    public int demSoLuongHoaDon(LocalDateTime tuNgay, LocalDateTime denNgay) {
        int soLuong = 0;
        String sql = "SELECT COUNT(*) as TongSo FROM HoaDon WHERE ngayLapHD BETWEEN ? AND ? AND loaiHD = 'BAN_HANG'";
        try (PreparedStatement pst = getConn().prepareStatement(sql)) {
            pst.setTimestamp(1, Timestamp.valueOf(tuNgay));
            pst.setTimestamp(2, Timestamp.valueOf(denNgay));
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) soLuong = rs.getInt("TongSo");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return soLuong;
    }

    public double tinhDoanhThu(LocalDateTime tuNgay, LocalDateTime denNgay) {
        double doanhThu = 0;
        String sql = "SELECT SUM(CASE WHEN ct.donGiaThucTe > 0 THEN ct.thanhTien ELSE (ct.soLuong*dvl.gia) END) AS DoanhThu " +
                     "FROM HoaDon hd " +
                     "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId " +
                     "JOIN DonViDoLuong dvl ON ct.donViDoLuongId = dvl.id AND ct.sanPhamId = dvl.sanPhamId " +
                     "WHERE hd.ngayLapHD BETWEEN ? AND ? AND hd.loaiHD = 'BAN_HANG'";
        try (PreparedStatement pst = getConn().prepareStatement(sql)) {
            pst.setTimestamp(1, Timestamp.valueOf(tuNgay));
            pst.setTimestamp(2, Timestamp.valueOf(denNgay));
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) doanhThu = rs.getDouble("DoanhThu");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return doanhThu;
    }

    public double tinhLoiNhuan(LocalDateTime tuNgay, LocalDateTime denNgay) {
        double dt = tinhDoanhThu(tuNgay, denNgay);
        double cp = 0;
        String sql = "SELECT ISNULL(SUM(lh.soLuongLoHang * lh.gia), 0) AS ChiPhi " +
                     "FROM LoHang lh WHERE lh.ngayNhap BETWEEN ? AND ?";
        try (PreparedStatement pst = getConn().prepareStatement(sql)) {
            pst.setTimestamp(1, Timestamp.valueOf(tuNgay));
            pst.setTimestamp(2, Timestamp.valueOf(denNgay));
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) cp = rs.getDouble("ChiPhi");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return dt - cp;
    }

    // NHÂN VIÊN DƯỢC SĨ

    /** Trả về List<String[3]>: {id, hoVaTen, chucVu} của tất cả dược sĩ đang làm việc */
    public List<String[]> getDuocSiList() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT id, hoVaTen, chucVu FROM NhanVien " +
                     "WHERE chucVu='DUOC_SI' AND trangThaiLamViec='DANG_LAM_VIEC' ORDER BY hoVaTen";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(new String[]{rs.getString("id"), rs.getString("hoVaTen"), rs.getString("chucVu")});
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // DOANH THU & CHI PHÍ 12 THÁNG
    public double[] getDoanhThu12Thang(int year, String filter) {
        double[] data = new double[12];
        String condHD = "";
        // Tự động nhận diện đây là mã NV hay là chuỗi lọc Tháng/Quý
        if (filter != null && !filter.isEmpty()) {
            if (filter.trim().startsWith("AND")) condHD = " " + filter;
            else condHD = " AND hd.nhanVienId='" + filter + "'";
        }
        
        String sql = "SELECT MONTH(hd.ngayLapHD) m, ISNULL(SUM(CASE WHEN ct.donGiaThucTe > 0 THEN ct.thanhTien ELSE (ct.soLuong*dvl.gia) END),0)/1000000.0 dt " +
                     "FROM HoaDon hd " +
                     "JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId " +
                     "JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId " +
                     "WHERE YEAR(hd.ngayLapHD)=? AND hd.loaiHD='BAN_HANG'" + condHD +
                     " GROUP BY MONTH(hd.ngayLapHD)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { int m = rs.getInt("m"); if (m>=1&&m<=12) data[m-1]=rs.getDouble("dt"); }
        } catch (Exception e) { e.printStackTrace(); }
        return data;
    }

    // DONUT - PHÂN LOẠI SẢN PHẨM
    public int[] getSoLuongTheoLoaiSP(int year, String filter) {
        String[] catDB = {"THUOC_KE_DON","THUOC_KHONG_KE_DON","THUC_PHAM_CHUC_NANG","MY_PHAM"};
        int[] catVals = new int[4];
        int total = 0;
        
        String condHD = "";
        if (filter != null && !filter.isEmpty()) {
            if (filter.trim().startsWith("AND")) condHD = " " + filter;
            else condHD = " AND hd.nhanVienId='" + filter + "'";
        }

        for (int i = 0; i < 4; i++) {
            String sql = "SELECT ISNULL(SUM(ct.soLuong),0) FROM ChiTietHoaDon ct " +
                         "JOIN HoaDon hd ON hd.id=ct.hoaDonId " +
                         "JOIN SanPham sp ON sp.id=ct.sanPhamId " +
                         "WHERE sp.danhMuc=? AND YEAR(hd.ngayLapHD)=? AND hd.loaiHD='BAN_HANG'" + condHD;
            try (PreparedStatement ps = getConn().prepareStatement(sql)) {
                ps.setString(1, catDB[i]); ps.setInt(2, year);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) catVals[i] = Math.max(1, rs.getInt(1));
            } catch (Exception e) { catVals[i] = 1; }
            total += catVals[i];
        }
        int[] result = new int[4];
        for (int i = 0; i < 4; i++)
            result[i] = Math.max(1, (int) Math.round(catVals[i] * 100.0 / total));
        return result;
    }
    
    /** Chi phí nhập hàng 12 tháng (triệu đồng). condPN: điều kiện SQL thêm vào query LoHang */
    public double[] getChiPhi12Thang(int year, String condPN) {
        double[] data = new double[12];
        String sql = "SELECT MONTH(ngayNhap) m, ISNULL(SUM(soLuongLoHang*gia),0)/1000000.0 cp " +
                     "FROM LoHang WHERE YEAR(ngayNhap)=?" + condPN + " GROUP BY MONTH(ngayNhap)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { int m = rs.getInt("m"); if (m>=1&&m<=12) data[m-1]=rs.getDouble("cp"); }
        } catch (Exception e) { /* Không có dữ liệu chi phí là hợp lệ */ }
        return data;
    }

    // THỐNG KÊ THEO NGÀY

    /** 10 ngày gần nhất có HĐ, sắp xếp tăng dần, định dạng dd/MM/yyyy */
    public List<String> get10NgayGanNhat(int year, String condHD) {
        List<String> dates = new ArrayList<>();
        String sql = "SELECT TOP 10 CONVERT(NVARCHAR, CAST(hd.ngayLapHD AS DATE), 103) AS d " +
                     "FROM HoaDon hd WHERE hd.loaiHD='BAN_HANG' AND YEAR(hd.ngayLapHD)=" + year + condHD + " " +
                     "GROUP BY CAST(hd.ngayLapHD AS DATE) ORDER BY CAST(hd.ngayLapHD AS DATE) DESC";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) dates.add(0, rs.getString("d")); // đảo để tăng dần
        } catch (Exception e) { e.printStackTrace(); }
        return dates;
    }

    /** Đếm số HĐ của 1 NV trong 1 ngày cụ thể (định dạng dd/MM/yyyy) */
    public int getDailyHDCuaNV(String nvId, String date, String condHD) {
        String sql = "SELECT COUNT(*) FROM HoaDon hd WHERE hd.nhanVienId=? " +
                     "AND CONVERT(NVARCHAR,CONVERT(DATE,hd.ngayLapHD),103)=? AND hd.loaiHD='BAN_HANG'" + condHD;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, nvId); ps.setString(2, date);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { /* ignored */ }
        return 0;
    }

    /**
     * Dữ liệu 30 ngày gần nhất.
     * Mỗi Object[3]: {date(String dd/MM/yyyy), hdCount(int), dt(double triệu đồng)}
     */
    public List<Object[]> getThongKe30NgayGanNhat(String condHD) {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT CONVERT(NVARCHAR,CAST(hd.ngayLapHD AS DATE),103) d, COUNT(DISTINCT hd.id) cnt, " +
                     "ISNULL(SUM(CASE WHEN ct.donGiaThucTe > 0 THEN ct.thanhTien ELSE (ct.soLuong*dvl.gia) END),0)/1000000.0 dt FROM HoaDon hd " +
                     "JOIN ChiTietHoaDon ct ON ct.hoaDonId=hd.id " +
                     "JOIN DonViDoLuong dvl ON dvl.id=ct.donViDoLuongId AND dvl.sanPhamId=ct.sanPhamId " +
                     "WHERE hd.loaiHD='BAN_HANG' " +
                     "AND CAST(hd.ngayLapHD AS DATE)>=DATEADD(DAY,-29,CAST(GETDATE() AS DATE))" + condHD + " " +
                     "GROUP BY CAST(hd.ngayLapHD AS DATE) ORDER BY CAST(hd.ngayLapHD AS DATE) ASC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.add(new Object[]{rs.getString("d"), rs.getInt("cnt"), rs.getDouble("dt")});
        } catch (Exception e) { /* ignored */ }
        return result;
    }

    // NHÂN VIÊN - CA SÁNG / CHIỀU

    /** Ca SÁNG (6h–14h): double[2] = {hdCount, dt_triệu_đồng} */
    public double[] getKetQuaCaSang(String nvId, int year, String condHD) {
        String sql = "SELECT COUNT(DISTINCT hd.id) hd, ISNULL(SUM(CASE WHEN ct.donGiaThucTe > 0 THEN ct.thanhTien ELSE (ct.soLuong*dvl.gia) END),0)/1000000.0 dt " +
                     "FROM HoaDon hd JOIN ChiTietHoaDon ct ON ct.hoaDonId=hd.id " +
                     "JOIN DonViDoLuong dvl ON dvl.id=ct.donViDoLuongId AND dvl.sanPhamId=ct.sanPhamId " +
                     "WHERE hd.nhanVienId=? AND YEAR(hd.ngayLapHD)=? AND hd.loaiHD='BAN_HANG' " +
                     "AND DATEPART(HOUR,hd.ngayLapHD) >= 6 AND DATEPART(HOUR,hd.ngayLapHD) < 14" + condHD;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, nvId); ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new double[]{rs.getInt("hd"), rs.getDouble("dt")};
        } catch (Exception e) { /* ignored */ }
        return new double[]{0, 0};
    }

    /** Ca CHIỀU (14h–22h): double[2] = {hdCount, dt_triệu_đồng} */
    public double[] getKetQuaCaChieu(String nvId, int year, String condHD) {
        String sql = "SELECT COUNT(DISTINCT hd.id) hd, ISNULL(SUM(CASE WHEN ct.donGiaThucTe > 0 THEN ct.thanhTien ELSE (ct.soLuong*dvl.gia) END),0)/1000000.0 dt " +
                     "FROM HoaDon hd JOIN ChiTietHoaDon ct ON ct.hoaDonId=hd.id " +
                     "JOIN DonViDoLuong dvl ON dvl.id=ct.donViDoLuongId AND dvl.sanPhamId=ct.sanPhamId " +
                     "WHERE hd.nhanVienId=? AND YEAR(hd.ngayLapHD)=? AND hd.loaiHD='BAN_HANG' " +
                     "AND DATEPART(HOUR,hd.ngayLapHD) >= 14 AND DATEPART(HOUR,hd.ngayLapHD) < 22" + condHD;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, nvId); ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new double[]{rs.getInt("hd"), rs.getDouble("dt")};
        } catch (Exception e) { /* ignored */ }
        return new double[]{0, 0};
    }

    /** Ca TỐI (22h–6h sáng hôm sau): double[2] = {hdCount, dt_triệu_đồng} */
    public double[] getKetQuaCaToi(String nvId, int year, String condHD) {
        String sql = "SELECT COUNT(DISTINCT hd.id) hd, ISNULL(SUM(CASE WHEN ct.donGiaThucTe > 0 THEN ct.thanhTien ELSE (ct.soLuong*dvl.gia) END),0)/1000000.0 dt " +
                     "FROM HoaDon hd JOIN ChiTietHoaDon ct ON ct.hoaDonId=hd.id " +
                     "JOIN DonViDoLuong dvl ON dvl.id=ct.donViDoLuongId AND dvl.sanPhamId=ct.sanPhamId " +
                     "WHERE hd.nhanVienId=? AND YEAR(hd.ngayLapHD)=? AND hd.loaiHD='BAN_HANG' " +
                     "AND (DATEPART(HOUR,hd.ngayLapHD) >= 22 OR DATEPART(HOUR,hd.ngayLapHD) < 6)" + condHD;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, nvId); ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new double[]{rs.getInt("hd"), rs.getDouble("dt")};
        } catch (Exception e) { /* ignored */ }
        return new double[]{0, 0};
    }

    // TỔNG SỐ HÓA ĐƠN

    /** Tổng số HĐ bán hàng theo năm + điều kiện lọc */
    public long getTongHoaDon(int year, String condHD) {
        String sql = "SELECT COUNT(*) FROM HoaDon hd WHERE YEAR(hd.ngayLapHD)=? AND hd.loaiHD='BAN_HANG'" + condHD;
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // TOP SẢN PHẨM BÁN CHẠY

    /**
     * Top 10 sản phẩm bán chạy nhất.
     * Mỗi Object[4]: {tenSP(String), danhMuc(String), soLuong(int), doanhThu_triệu(double)}
     */
    public List<Object[]> getTopSanPham(int year, String condHD) {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT TOP 10 sp.ten, sp.danhMuc, SUM(ct.soLuong) sl, " +
                     "ISNULL(SUM(CASE WHEN ct.donGiaThucTe > 0 THEN ct.thanhTien ELSE (ct.soLuong*dvl.gia) END),0) dt " +
                     "FROM ChiTietHoaDon ct " +
                     "JOIN HoaDon hd ON hd.id=ct.hoaDonId " +
                     "JOIN SanPham sp ON sp.id=ct.sanPhamId " +
                     "JOIN DonViDoLuong dvl ON dvl.sanPhamId=ct.sanPhamId AND dvl.id=ct.donViDoLuongId " +
                     "WHERE YEAR(hd.ngayLapHD)=? AND hd.loaiHD='BAN_HANG'" + condHD +
                     " GROUP BY sp.ten, sp.danhMuc ORDER BY dt DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                result.add(new Object[]{
                    rs.getString("ten"), rs.getString("danhMuc"),
                    rs.getInt("sl"),     rs.getDouble("dt") / 1_000_000.0
                });
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // BÁO CÁO VAT

    /**
     * Top 20 SP cho báo cáo VAT.
     * Mỗi Object[5]: {maSP, tenSP, danhMuc, vatPct(int), tienThue_triệu(double)}
     */
    public List<Object[]> getVATReport(int year, String condHD) {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT TOP 20 sp.id, sp.ten, sp.danhMuc, " +
                     "CASE WHEN sp.danhMuc='THUOC_KE_DON' THEN 5 ELSE 10 END vatPct, " +
                     "ISNULL(SUM(CASE WHEN ct.donGiaThucTe > 0 THEN ct.thanhTien ELSE (ct.soLuong*dvl.gia) END),0) dt " +
                     "FROM ChiTietHoaDon ct " +
                     "JOIN HoaDon hd ON hd.id=ct.hoaDonId " +
                     "JOIN SanPham sp ON sp.id=ct.sanPhamId " +
                     "JOIN DonViDoLuong dvl ON dvl.sanPhamId=ct.sanPhamId AND dvl.id=ct.donViDoLuongId " +
                     "WHERE YEAR(hd.ngayLapHD)=? AND hd.loaiHD='BAN_HANG'" + condHD +
                     " GROUP BY sp.id,sp.ten,sp.danhMuc ORDER BY dt DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double dt = rs.getDouble("dt");
                int vat  = rs.getInt("vatPct");
                result.add(new Object[]{
                    rs.getString("id"), rs.getString("ten"),
                    rs.getString("danhMuc"), vat,
                    (dt * vat / 100.0) / 1_000_000.0
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }

    // SẢN PHẨM SẮP HẾT HẠN

    /**
     * Lô hàng còn hàng, hết hạn trong 6 tháng tới.
     * Mỗi Object[5]: {soLoHang, tenSP, maKho, soLuong(int), ngayHetHan(String dd/MM/yyyy)}
     */
    public List<Object[]> getSpSapHetHan() {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT lh.soLoHang, sp.ten, kh.id AS Kho, lh.soLuongLoHang, lh.ngayHetHan " +
                     "FROM LoHang lh " +
                     "JOIN SanPham sp ON lh.sanPhamId = sp.id " +
                     "JOIN KhoHang kh ON lh.khoHangId = kh.id " +
                     "WHERE lh.trangThai = 'CON_HANG' " +
                     "AND lh.ngayHetHan <= DATEADD(MONTH, 6, GETDATE()) " +
                     "ORDER BY lh.ngayHetHan ASC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            while (rs.next())
                result.add(new Object[]{
                    rs.getString("soLoHang"), rs.getString("ten"),
                    rs.getString("Kho"),      rs.getInt("soLuongLoHang"),
                    sdf.format(rs.getDate("ngayHetHan"))
                });
        } catch (Exception e) { e.printStackTrace(); }
        return result;
    }
    
    // CÁC HÀM BỔ SUNG CHO MÀN HÌNH CHÍNH (DASHBOARD)
    
    public int getTongSanPham() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM SanPham";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return count;
    }

    public int getTongKhachHang() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM KhachHang";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return count;
    }

    public int getHoaDonHomNay(String maNV) {
        int count = 0;
        String cond = (maNV != null && !maNV.isEmpty()) ? " AND nhanVienId='" + maNV + "'" : "";
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE CONVERT(DATE,ngayLapHD)=CONVERT(DATE,GETDATE()) AND loaiHD='BAN_HANG'" + cond;
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return count;
    }

    public double getDoanhThuHomNay(String maNV) {
        double dt = 0;
        String cond = (maNV != null && !maNV.isEmpty()) ? " AND hd.nhanVienId='" + maNV + "'" : "";
        String sql = "SELECT hd.ghiChu, SUM(ct.soLuong * dvl.gia * (1 + ISNULL(sp.thueVAT, 0)/100.0)) as tongGoc " +
                     "FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId " +
                     "JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId " +
                     "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                     "WHERE CONVERT(DATE,hd.ngayLapHD)=CONVERT(DATE,GETDATE()) AND hd.loaiHD='BAN_HANG'" + cond +
                     " GROUP BY hd.id, hd.ghiChu";
        try (Connection con = getConn(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) dt += tinhTienThucTeCuaHoaDon(con, rs.getDouble("tongGoc"), rs.getString("ghiChu"));
        } catch (Exception e) {} return dt;
    }

    public double getDoanhThuTienMatHomNay(String maNV) {
        double dt = 0;
        String cond = (maNV != null && !maNV.isEmpty()) ? " AND hd.nhanVienId='" + maNV + "'" : "";
        String sql = "SELECT hd.ghiChu, SUM(ct.soLuong * dvl.gia * (1 + ISNULL(sp.thueVAT, 0)/100.0)) as tongGoc " +
                     "FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId " +
                     "JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId " +
                     "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                     "WHERE hd.phuongThucThanhToan='TIEN_MAT' AND CONVERT(DATE,hd.ngayLapHD)=CONVERT(DATE,GETDATE()) AND hd.loaiHD='BAN_HANG'" + cond +
                     " GROUP BY hd.id, hd.ghiChu";
        try (Connection con = getConn(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) dt += tinhTienThucTeCuaHoaDon(con, rs.getDouble("tongGoc"), rs.getString("ghiChu"));
        } catch (Exception e) {} return dt;
    }

    public List<Object[]> getHoaDonGanDayTrongCa(String maNV) {
        List<Object[]> list = new ArrayList<>();
        String cond = (maNV != null && !maNV.isEmpty()) ? " AND hd.nhanVienId='" + maNV + "'" : "";
        
        String sql = "SELECT hd.id, ISNULL(kh.hoVaTen, N'Khách lẻ') AS kh, ISNULL(SUM(ct.soLuong * dvl.gia), 0) AS tong, hd.phuongThucThanhToan AS pttt " +
                     "FROM HoaDon hd " +
                     "LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id " +
                     "LEFT JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId " +
                     "LEFT JOIN DonViDoLuong dvl ON ct.donViDoLuongId = dvl.id AND ct.sanPhamId = dvl.sanPhamId " +
                     "WHERE hd.loaiHD = 'BAN_HANG' AND CONVERT(DATE, hd.ngayLapHD) = CONVERT(DATE, GETDATE()) " + cond +
                     " GROUP BY hd.id, kh.hoVaTen, hd.phuongThucThanhToan, hd.ngayLapHD ORDER BY hd.ngayLapHD DESC";
                     
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(new Object[]{rs.getString("id"), rs.getString("kh"), rs.getDouble("tong"), rs.getString("pttt")});
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
    
    public List<Object[]> getTop4SanPhamSapHetHang() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT TOP 4 sp.ten, ISNULL(SUM(lh.soLuongLoHang),0) ton FROM SanPham sp LEFT JOIN LoHang lh ON sp.id=lh.sanPhamId GROUP BY sp.ten ORDER BY ton ASC";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(new Object[]{rs.getString("ten"), rs.getInt("ton"), 50});
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int getSoSanPhamDuTon() {
        int count = 0;
        String sql = "SELECT COUNT(DISTINCT sp.id) FROM SanPham sp JOIN LoHang lh ON sp.id=lh.sanPhamId";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return count;
    }

    public int getSoLoHangSapHetHanKhoang(int days) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM LoHang WHERE ngayHetHan IS NOT NULL AND DATEDIFF(DAY,GETDATE(),ngayHetHan)<=" + days;
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return count;
    }

    public List<Object[]> getLoHangSapHetHanNhanh(int days) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT TOP 8 sp.ten, lh.soLoHang, lh.soLuongLoHang, lh.ngayHetHan, DATEDIFF(DAY,GETDATE(),lh.ngayHetHan) cl FROM LoHang lh JOIN SanPham sp ON lh.sanPhamId=sp.id WHERE lh.ngayHetHan IS NOT NULL AND DATEDIFF(DAY,GETDATE(),lh.ngayHetHan)<=" + days + " ORDER BY lh.ngayHetHan ASC";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(new Object[]{rs.getString("ten"), rs.getString("soLoHang"), rs.getInt("soLuongLoHang"), rs.getTimestamp("ngayHetHan"), rs.getInt("cl")});
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public double getDoanhThu7NgayQua(String maNV) {
        double dt = 0;
        String sql = "SELECT ISNULL(SUM(CASE WHEN ct.donGiaThucTe > 0 THEN ct.thanhTien ELSE (ct.soLuong*dvl.gia) END),0) FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId WHERE hd.ngayLapHD>=DATEADD(DAY,-7,GETDATE()) AND hd.loaiHD='BAN_HANG'"
            + (maNV != null && !maNV.isEmpty() ? " AND hd.nhanVienId=?" : "");
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            if (maNV != null && !maNV.isEmpty()) ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) dt = rs.getDouble(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return dt;
    }

    public int getSoHoaDon7NgayQua(String maNV) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE ngayLapHD>=DATEADD(DAY,-7,GETDATE()) AND loaiHD='BAN_HANG'"
            + (maNV != null && !maNV.isEmpty() ? " AND nhanVienId=?" : "");
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            if (maNV != null && !maNV.isEmpty()) ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) count = rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return count;
    }

    public int getTongPhieuDoiTra() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE loaiHD IN ('TRA_HANG', 'DOI_HANG')";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return count;
    }

    public int getPhieuDoiTraChoXuLy() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE loaiHD IN ('TRA_HANG', 'DOI_HANG') " +
                     "AND (ghiChu IS NULL OR (ghiChu NOT LIKE N'%Hoàn thành%' AND ghiChu NOT LIKE N'%Từ chối%'))";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return count;
    }

    public int getSoHoaDonTheoCa(String maNV, LocalDateTime start) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE nhanVienId=? AND ngayLapHD>=? AND loaiHD='BAN_HANG'";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, maNV); ps.setTimestamp(2, Timestamp.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) count = rs.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return count;
    }

    public double getDoanhThuTheoCa(String maNV, LocalDateTime start) {
        double dt = 0;
        String sql = "SELECT hd.ghiChu, SUM(ct.soLuong * dvl.gia * (1 + ISNULL(sp.thueVAT, 0)/100.0)) as tongGoc " +
                     "FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId " +
                     "JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId " +
                     "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                     "WHERE hd.nhanVienId=? AND hd.ngayLapHD>=? AND hd.loaiHD='BAN_HANG' " +
                     "GROUP BY hd.id, hd.ghiChu";
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) { 
            ps.setString(1, maNV); 
            ps.setTimestamp(2, Timestamp.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) { 
                while (rs.next()) dt += tinhTienThucTeCuaHoaDon(con, rs.getDouble("tongGoc"), rs.getString("ghiChu"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return dt;
    }
    private double tinhTienThucTe(Connection con, double tongGocCoVat, String ghiChu) {
        double tongTienGiam = 0;
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
                                    if (loaiKM.contains("PHAN_TRAM") || loaiKM.contains("%")) {
                                        tongTienGiam += tongGocCoVat * (val / 100.0);
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
        return Math.max(0, tongGocCoVat - tongTienGiam);
    }
    public double getDoanhThuTienMatTheoCa(String maNV, LocalDateTime start) {
        double dt = 0;
        String sql = "SELECT hd.ghiChu, SUM(ct.soLuong * dvl.gia * (1 + ISNULL(sp.thueVAT, 0)/100.0)) as tongGoc " +
                     "FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId " +
                     "JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId " +
                     "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                     "WHERE hd.nhanVienId=? AND hd.phuongThucThanhToan='TIEN_MAT' AND hd.ngayLapHD>=? AND hd.loaiHD='BAN_HANG' " +
                     "GROUP BY hd.id, hd.ghiChu";
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV); ps.setTimestamp(2, Timestamp.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) { 
                while (rs.next()) dt += tinhTienThucTeCuaHoaDon(con, rs.getDouble("tongGoc"), rs.getString("ghiChu"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return dt;
    }

    // Lấy danh sách nhân viên thật từ DB cho Combobox
    public List<String[]> getDanhSachNhanVien() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT id, hoVaTen FROM NhanVien WHERE trangThaiLamViec='DANG_LAM_VIEC'";
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(new String[]{rs.getString("id"), rs.getString("hoVaTen")});
        } catch (Exception e) {}
        return list;
    }

    private String getCaCondition(int ca) {
        String cond = "";
        if (ca == 1) cond = " AND DATEPART(HOUR, hd.ngayLapHD) BETWEEN 6 AND 13";
        else if (ca == 2) cond = " AND DATEPART(HOUR, hd.ngayLapHD) BETWEEN 14 AND 21";
        else if (ca == 3) cond = " AND ((DATEPART(HOUR, hd.ngayLapHD) >= 22) OR (DATEPART(HOUR, hd.ngayLapHD) < 6))";

        try {
            Utils.UserSession session = Utils.UserSession.getInstance();
            if (session != null && !session.isAdmin() && session.getCaHienTai() != null) {
                java.time.LocalDateTime start = session.getCaHienTai().getThoiGianBatDau();
                // FIX LỖI: Chuyển sang định dạng ISO 8601 (có chữ 'T') để SQL Server phân tích chính xác tuyệt đối
                java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
                cond += " AND hd.ngayLapHD >= '" + start.format(fmt) + "'";
            }
        } catch (Exception e) {}

        return cond;
    }

    public int getHoaDonHomNay(String maNV, int ca) {
        int count = 0;
        String cond = (maNV != null && !maNV.isEmpty()) ? " AND hd.nhanVienId='" + maNV + "'" : "";
        cond += getCaCondition(ca);
        String sql = "SELECT COUNT(*) FROM HoaDon hd WHERE CONVERT(DATE, hd.ngayLapHD)=CONVERT(DATE, GETDATE()) AND hd.loaiHD='BAN_HANG'" + cond;
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) count = rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }

    public List<Object[]> getHoaDonGanDayTrongCa(String maNV, int ca) {
        List<Object[]> list = new ArrayList<>();
        String cond = (maNV != null && !maNV.isEmpty()) ? " AND hd.nhanVienId='" + maNV + "'" : "";
        cond += getCaCondition(ca);
        
        String sql = "SELECT hd.id, ISNULL(kh.hoVaTen, N'Khách lẻ') AS kh, " +
                     "SUM(CASE WHEN ct.donGiaThucTe > 0 THEN ct.thanhTien ELSE (ct.soLuong * dvl.gia) END) AS tong, hd.phuongThucThanhToan AS pttt " +
                     "FROM HoaDon hd " +
                     "LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id " +
                     "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId " +
                     "JOIN DonViDoLuong dvl ON ct.donViDoLuongId = dvl.id AND ct.sanPhamId = dvl.sanPhamId " +
                     "WHERE hd.loaiHD = 'BAN_HANG' AND CONVERT(DATE, hd.ngayLapHD) = CONVERT(DATE, GETDATE()) " + cond +
                     " GROUP BY hd.id, kh.hoVaTen, hd.phuongThucThanhToan, hd.ngayLapHD " +
                     "ORDER BY hd.ngayLapHD DESC";
                     
        try (Statement st = getConn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{rs.getString("id"), rs.getString("kh"), rs.getDouble("tong"), rs.getString("pttt")});
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 1. Thêm hàm lấy TỔNG TIỀN HÀNG (Giá gốc - Hàm cũ của bạn mình đổi tên lại cho rõ nghĩa)
 // 1. Thêm hàm lấy TỔNG TIỀN HÀNG (Giá gốc)
    public double getTongTienHangHomNay(String maNV, int ca) {
        double tong = 0;
        String cond = (maNV != null && !maNV.isEmpty()) ? " AND hd.nhanVienId='" + maNV + "'" : "";
        cond += getCaCondition(ca); // Kế thừa bộ lọc chặn giờ mở ca ở trên

        String sql =
            "SELECT ISNULL(SUM(ct.soLuong * dvl.gia),0) " +
            "FROM HoaDon hd " +
            "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId " +
            "JOIN DonViDoLuong dvl ON ct.donViDoLuongId = dvl.id " +
            "AND ct.sanPhamId = dvl.sanPhamId " +
            "WHERE CONVERT(DATE, hd.ngayLapHD) = CONVERT(DATE, GETDATE()) " +
            "AND hd.loaiHD = 'BAN_HANG' " + cond;

        try (Connection con = getConn(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                tong = rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tong;
    }

    // 2. Hàm lấy TỔNG KHUYẾN MÃI (Đã Fix: Tính qua chênh lệch giá gốc và thanh toán thực tế)
    public double getTongKhuyenMaiHomNay(String maNV, int ca) {
        // Khuyến mãi = Giá gốc (chưa giảm) - Doanh thu thực tế (đã giảm)
        return getTongTienHangHomNay(maNV, ca) - getDoanhThuHomNay(maNV, ca);
    }

    public double getDoanhThuHomNay(String maNV, int ca) {
        double dt = 0;
        String cond = (maNV != null && !maNV.isEmpty()) ? " AND hd.nhanVienId='" + maNV + "'" : "";
        cond += getCaCondition(ca);
        String sql = "SELECT hd.ghiChu, SUM(ct.soLuong * dvl.gia * (1 + ISNULL(sp.thueVAT, 0)/100.0)) as tongGoc " +
                     "FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId " +
                     "JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId " +
                     "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                     "WHERE CONVERT(DATE, hd.ngayLapHD) = CONVERT(DATE, GETDATE()) AND hd.loaiHD = 'BAN_HANG'" + cond +
                     " GROUP BY hd.id, hd.ghiChu";
        try (Connection con = getConn(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) dt += tinhTienThucTeCuaHoaDon(con, rs.getDouble("tongGoc"), rs.getString("ghiChu"));
        } catch (Exception e) {} return dt;
    }
    public double getDoanhThuTienMatHomNay(String maNV, int ca) {
        double dt = 0;
        String cond = (maNV != null && !maNV.isEmpty()) ? " AND hd.nhanVienId='" + maNV + "'" : "";
        cond += getCaCondition(ca);
        String sql = "SELECT hd.ghiChu, SUM(ct.soLuong * dvl.gia * (1 + ISNULL(sp.thueVAT, 0)/100.0)) as tongGoc " +
                     "FROM HoaDon hd JOIN ChiTietHoaDon ct ON hd.id=ct.hoaDonId " +
                     "JOIN DonViDoLuong dvl ON ct.donViDoLuongId=dvl.id AND ct.sanPhamId=dvl.sanPhamId " +
                     "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                     "WHERE hd.phuongThucThanhToan = 'TIEN_MAT' AND CONVERT(DATE, hd.ngayLapHD) = CONVERT(DATE, GETDATE()) AND hd.loaiHD = 'BAN_HANG'" + cond +
                     " GROUP BY hd.id, hd.ghiChu";
        try (Connection con = getConn(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) dt += tinhTienThucTeCuaHoaDon(con, rs.getDouble("tongGoc"), rs.getString("ghiChu"));
        } catch (Exception e) {} return dt;
    }

    public double getTienHoanTraTheoCa(String maNV, LocalDateTime start) {
        double tongHoanTra = 0;
        // ĐÃ FIX: Chỉ tính tiền chi ra đối với những phiếu đã "Hoàn thành" (Đã tiếp nhận)
        String sql = "SELECT id, loaiHD, ghiChu FROM HoaDon WHERE nhanVienId = ? AND ngayLapHD >= ? AND loaiHD IN ('TRA_HANG', 'DOI_HANG') AND ghiChu LIKE N'%Hoàn thành%'";
        
        try (Connection con = getConn(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String loai = rs.getString("loaiHD");
                    String ghiChu = rs.getString("ghiChu");
                    
                    // Bóc tách số tiền trực tiếp từ chuỗi Ghi chú giống như trên giao diện ManHinhDoiTra
                    if (ghiChu != null && ghiChu.contains("|")) {
                        String[] parts = ghiChu.split("\\|");
                        
                        if (loai.equals("TRA_HANG")) {
                            // Trả hàng -> Lấy tiền ở vị trí số 3 (parts[2])
                            if (parts.length >= 3) {
                                String tienHoanStr = parts[2].trim().replaceAll("[^0-9]", "");
                                if (!tienHoanStr.isEmpty()) {
                                    tongHoanTra += Double.parseDouble(tienHoanStr);
                                }
                            }
                        } else if (loai.equals("DOI_HANG")) {
                            // Đổi hàng -> Lấy tiền ở vị trí số 4 (parts[3])
                            if (parts.length >= 4) {
                                String chenhLechStr = parts[3].trim(); 
                                // Đổi hàng chỉ tính là "Chi ra" khi Cửa hàng phải thối lại tiền cho khách (Chênh lệch âm / Hoàn tiền)
                                if (chenhLechStr.contains("Hoàn")) {
                                    String tienThoiStr = chenhLechStr.replaceAll("[^0-9]", "");
                                    if (!tienThoiStr.isEmpty()) {
                                        tongHoanTra += Double.parseDouble(tienThoiStr);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return tongHoanTra;
    }

    // Thêm hàm bổ trợ này nếu file của bạn chưa có
    private double getTongTienChiTietDoiTra(Connection con, String maHD, String loaiGhiChu) {
        double tong = 0;
        String sql = "SELECT ISNULL(SUM(ct.soLuong * dvl.gia), 0) FROM ChiTietHoaDon ct " +
                     "JOIN DonViDoLuong dvl ON ct.donViDoLuongId = dvl.id AND ct.sanPhamId = dvl.sanPhamId " +
                     "WHERE ct.hoaDonId = ?";
        if (loaiGhiChu != null) {
            sql += " AND ct.ghiChu = ?";
        }
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            if (loaiGhiChu != null) ps.setString(2, loaiGhiChu);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tong = rs.getDouble(1);
            }
        } catch (Exception e) {}
        return tong;
    }
    private double tinhTienThucTeCuaHoaDon(Connection con, double tongGocCoVat, String ghiChu) {
        double tongTienGiam = 0;
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
                                    if (loaiKM.contains("PHAN_TRAM") || loaiKM.contains("%")) {
                                        tongTienGiam += tongGocCoVat * (val / 100.0);
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
        return Math.max(0, tongGocCoVat - tongTienGiam);
    }
    
}