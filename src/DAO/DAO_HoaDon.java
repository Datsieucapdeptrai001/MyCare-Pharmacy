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

        // Dùng try-with-resources để tự động đóng Connection (chống đơ máy)
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
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

    public boolean themHoaDon(HoaDon hd) {
        String sql = "INSERT INTO HoaDon (id, loaiHD, ghiChu, ngayLapHD, nhanVienId, khachHangId, khuyenMaiId, phuongThucThanhToan, hoaDonGocId) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int n = 0;
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
        
        // CÁCH FIX: Chỉnh lại câu SQL. Vì bảng không có cột TrangThai, 
        // giả định bạn đang dùng cột ghiChu hoặc cột loaiHD để đánh dấu hóa đơn nháp.
        // Hãy đổi 'ghiChu' thành 'loaiHD = ...' nếu hệ thống của bạn dùng Enum cho nháp.
        String sql = "SELECT * FROM HoaDon WHERE ghiChu = N'Lưu nháp'"; 
        
        // 1. Dùng getInstance().getConnection() để tránh lỗi văng app
        try (Connection con = ConnectDB.getInstance().getConnection(); 
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                // 2. Map đúng tên cột trong Database theo các hàm ở trên
                String maHD = rs.getString("id");
                String maNV = rs.getString("nhanVienId");
                String maKH = rs.getString("khachHangId");
                
                java.sql.Timestamp sqlTimestamp = rs.getTimestamp("ngayLapHD");
                LocalDateTime ngayLap = (sqlTimestamp != null) ? sqlTimestamp.toLocalDateTime() : null;
                
                // 3. Khởi tạo bằng constructor rỗng và set thuộc tính
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

    public boolean capNhatTrangThai(String idHD, LoaiHoaDon loaiMoi) {
        String sql = "UPDATE HoaDon SET loaiHD = ? WHERE id = ?";
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