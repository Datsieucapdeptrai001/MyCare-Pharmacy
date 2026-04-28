package DAO;

import ConnectDB.ConnectDB;
import Entity.HoaDon;
import Entity.KhachHang;
import Entity.KhuyenMai;
import Entity.NhanVien;
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
        
        // 1. Đã đổi thành LEFT JOIN để hóa đơn nháp chưa có sản phẩm vẫn hiện lên bảng
        String sql = "SELECT hd.id, hd.ngayLapHD, kh.hoVaTen, kh.sdt, hd.phuongThucThanhToan, hd.ghiChu, " +
                     "SUM(ct.soLuong * dv.gia) as tongTien " +
                     "FROM HoaDon hd " +
                     "LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id " +
                     "LEFT JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId " +
                     "LEFT JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id AND ct.sanPhamId = dv.sanPhamId " +
                     "WHERE hd.loaiHD = 'BAN_HANG' " +
                     "GROUP BY hd.id, hd.ngayLapHD, kh.hoVaTen, kh.sdt, hd.phuongThucThanhToan, hd.ghiChu " +
                     "ORDER BY hd.ngayLapHD DESC";

        // Đưa Connection ra ngoài để tránh bị đóng
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            DecimalFormat df = new DecimalFormat("#,###đ");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            while (rs.next()) {
                String maHD = rs.getString("id");
                
                // Tránh lỗi nếu ngày lập bị null
                String ngay = "";
                if (rs.getTimestamp("ngayLapHD") != null) {
                    ngay = rs.getTimestamp("ngayLapHD").toLocalDateTime().format(dtf);
                }
                
                String tenKH = rs.getString("hoVaTen") != null ? rs.getString("hoVaTen") : "Khách lẻ";
                String sdt = rs.getString("sdt") != null ? rs.getString("sdt") : "";
                
                // 2. BẮT LỖI NULL KHI CHƯA THANH TOÁN (HÓA ĐƠN NHÁP)
                String pttt = rs.getString("phuongThucThanhToan");
                String pt = "Chưa TT"; // Mặc định cho hóa đơn nháp
                if (pttt != null) {
                    pt = pttt.equals("TIEN_MAT") ? "Tiền mặt" : "Chuyển khoản";
                }
                
                // Lấy tổng tiền (Nếu chưa có thuốc sẽ trả về 0)
                String tongTien = df.format(rs.getDouble("tongTien"));
                
                // 3. LOGIC PHÂN BIỆT TRẠNG THÁI DỰA VÀO GHI CHÚ
                String ghiChu = rs.getString("ghiChu");
                String trangThai = "Hoàn thành"; // Mặc định

                if (ghiChu != null) {
                    if (ghiChu.equals("Lưu nháp")) {
                        trangThai = "Đang xử lý";
                    } else if (ghiChu.contains("Đã hủy")) {
                        trangThai = "Đã hủy";
                    }
                }
                
                String xem = ""; 
                String hiddenCat = "Tất cả"; 

                ds.add(new Object[]{maHD, ngay, tenKH, sdt, pt, tongTien, trangThai, xem, hiddenCat});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }
    
    public List<Object[]> layDanhSachHoaDonCuaNhanVien(String maNV) {
        List<Object[]> ds = new ArrayList<>();
        
        // CÂU SQL: Copy y hệt của Admin nhưng thêm điều kiện lọc theo Mã NV
        String sql = "SELECT hd.id, hd.ngayLapHD, kh.hoVaTen, kh.sdt, hd.phuongThucThanhToan, hd.ghiChu, " +
                     "SUM(ct.soLuong * dv.gia) as tongTien " +
                     "FROM HoaDon hd " +
                     "LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id " +
                     "LEFT JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId " +
                     "LEFT JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id AND ct.sanPhamId = dv.sanPhamId " +
                     "WHERE hd.loaiHD = 'BAN_HANG' AND hd.nhanVienId = ? " + // <--- LỌC CHUẨN Ở ĐÂY
                     "GROUP BY hd.id, hd.ngayLapHD, kh.hoVaTen, kh.sdt, hd.phuongThucThanhToan, hd.ghiChu " +
                     "ORDER BY hd.ngayLapHD DESC";

        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maNV); // Truyền mã nhân viên vào
            
            try (ResultSet rs = pst.executeQuery()) {
                DecimalFormat df = new DecimalFormat("#,###đ");
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                while (rs.next()) {
                    String maHD = rs.getString("id");
                    String ngay = rs.getTimestamp("ngayLapHD") != null ? rs.getTimestamp("ngayLapHD").toLocalDateTime().format(dtf) : "";
                    String tenKH = rs.getString("hoVaTen") != null ? rs.getString("hoVaTen") : "Khách lẻ";
                    String sdt = rs.getString("sdt") != null ? rs.getString("sdt") : "";
                    
                    String pttt = rs.getString("phuongThucThanhToan");
                    String pt = (pttt != null && pttt.equals("TIEN_MAT")) ? "Tiền mặt" : "Chuyển khoản";
                    
                    String tongTien = df.format(rs.getDouble("tongTien"));
                    
                    // ĐỒNG BỘ LOGIC TRẠNG THÁI VỚI ADMIN
                    String ghiChu = rs.getString("ghiChu");
                    String trangThai = "Hoàn thành";
                    if (ghiChu != null) {
                        if (ghiChu.equals("Lưu nháp")) trangThai = "Đang xử lý";
                        else if (ghiChu.contains("Đã hủy")) trangThai = "Đã hủy";
                    }
                    
                    // Trả về đúng 7 cột mà ManHinhDanhSachHoaDon đang chờ đợi
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
        // Đã đúng, không có try(Connection) ở đây
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, hd.getId());
            pst.setString(2, hd.getLoaiHD().name());
            pst.setString(3, hd.getGhiChu());
            pst.setTimestamp(4, Timestamp.valueOf(hd.getNgayLapHD()));

            pst.setString(5, hd.getNhanVienId().getNhanVien());

            if (hd.getKhachHangId() != null) {
                pst.setString(6, hd.getKhachHangId().getId());
            } else {
                pst.setNull(6, java.sql.Types.NVARCHAR);
            }

            if (hd.getKhuyenMaiId() != null) {
                pst.setString(7, hd.getKhuyenMaiId().getId());
            } else {
                pst.setNull(7, java.sql.Types.NVARCHAR);
            }

            pst.setString(8, hd.getPhuongThucThanhToan().name());

            if (hd.getHoaDonGocId() != null) {
                pst.setString(9, hd.getHoaDonGocId().getId());
            } else {
                pst.setNull(9, java.sql.Types.NVARCHAR);
            }

            n = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return n > 0;
    }

    public HoaDon layHoaDonTheoMa(String maHD) {
        HoaDon hd = null;
        String sql = "SELECT * FROM HoaDon WHERE id = ?";
        // Đã đúng, không có try(Connection) ở đây
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
}