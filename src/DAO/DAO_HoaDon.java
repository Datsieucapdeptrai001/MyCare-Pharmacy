package DAO;

import ConnectDB.ConnectDB;
import Entity.HoaDon;
import Entity.KhachHang;
import Entity.KhuyenMai;
import Entity.NhanVien;
import Enumeration.LoaiHoaDon;
import Enumeration.PhuongThucThanhToan;

import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DAO_HoaDon {

    public DAO_HoaDon() {}
    public List<Object[]> layDanhSachHoaDonChoBang() {
        List<Object[]> ds = new ArrayList<>();
        // Câu lệnh SQL lấy thông tin hóa đơn, tên khách hàng và tính tổng tiền
        String sql = "SELECT hd.id, hd.ngayLapHD, kh.hoVaTen, kh.sdt, hd.phuongThucThanhToan, " +
                     "SUM(ct.soLuong * dv.gia) as tongTien " +
                     "FROM HoaDon hd " +
                     "LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id " +
                     "JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId " +
                     "JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id AND ct.sanPhamId = dv.sanPhamId " +
                     "WHERE hd.loaiHD = 'BAN_HANG' " +
                     "GROUP BY hd.id, hd.ngayLapHD, kh.hoVaTen, kh.sdt, hd.phuongThucThanhToan " +
                     "ORDER BY hd.ngayLapHD DESC";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            DecimalFormat df = new DecimalFormat("#,###đ");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            while (rs.next()) {
                String maHD = rs.getString("id");
                // Xử lý ngày tháng
                String ngay = rs.getTimestamp("ngayLapHD").toLocalDateTime().format(dtf);
                // Xử lý khách hàng (nếu null thì là Khách lẻ)
                String tenKH = rs.getString("hoVaTen") != null ? rs.getString("hoVaTen") : "Khách lẻ";
                String sdt = rs.getString("sdt") != null ? rs.getString("sdt") : "";
                // Xử lý phương thức thanh toán
                String pt = rs.getString("phuongThucThanhToan").equals("TIEN_MAT") ? "Tiền mặt" : "Chuyển khoản";
                // Định dạng tiền
                String tongTien = df.format(rs.getDouble("tongTien"));
                
                // Các cột bổ trợ cho JTable trong ManHinhBanHang
                String trangThai = "Hoàn thành";
                String xem = ""; // Cột icon xem
                String hiddenCat = "Tất cả"; // Cột ẩn để lọc danh mục

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