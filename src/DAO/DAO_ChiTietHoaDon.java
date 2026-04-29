package DAO;

import ConnectDB.ConnectDB;
import Entity.ChiTietHoaDon;
import Entity.DonViDoLuong;
import Entity.HoaDon;
import Entity.SanPham;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DAO_ChiTietHoaDon {

    public DAO_ChiTietHoaDon() {}

    public List<Object[]> layDanhSachSanPhamTheoMaHD(String maHD) {
        List<Object[]> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        
        String sql = "SELECT sp.ten AS TenSP, dv.ten AS DVT, ct.soLuong AS SL, " +
                     "dv.gia AS DonGia, sp.thueVAT AS VAT " +
                     "FROM ChiTietHoaDon ct " +
                     "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                     "JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id AND ct.sanPhamId = dv.sanPhamId " +
                     "WHERE ct.hoaDonId = ?";
                     
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setString(1, maHD);
            
            try (ResultSet rs = pstm.executeQuery()) {
                int stt = 1;
                while (rs.next()) {
                    String tenSP = rs.getString("TenSP");
                    String dvt = rs.getString("DVT");
                    int sl = rs.getInt("SL");
                    double donGia = rs.getDouble("DonGia");
                    double vatPercent = rs.getDouble("VAT"); // Lấy giá trị VAT từ Database
                    
                    // =======================================================
                    // FIX LỖI 0% VAT: Xử lý số thập phân (VD: 0.05 -> 5.0)
                    // =======================================================
                    if (vatPercent > 0 && vatPercent < 1) {
                        vatPercent = vatPercent * 100;
                    }

                    // Tính toán thành tiền: (Số lượng * Đơn giá) + Tiền VAT
                    double tienChuaVAT = sl * donGia;
                    double tienVAT = tienChuaVAT * (vatPercent / 100);
                    double thanhTien = tienChuaVAT + tienVAT;

                    // Format chuỗi tiền tệ (vd: 25000 -> 25.000đ)
                    String strDonGia = String.format("%,d", (long)donGia).replace(',', '.') + "đ";
                    String strThanhTien = String.format("%,d", (long)thanhTien).replace(',', '.') + "đ";
                    String strVAT = (int)vatPercent + "%"; // Bây giờ (int) 5.0 sẽ ra chuẩn 5%

                    // Tạo mảng Object đúng 7 cột mà UI ChiTietHoaDon đang yêu cầu:
                    Object[] row = new Object[]{
                        String.valueOf(stt++),
                        tenSP,
                        dvt,
                        String.valueOf(sl),
                        strDonGia,
                        strVAT,
                        strThanhTien
                    };
                    
                    list.add(row);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi SQL layDanhSachSanPhamTheoMaHD: " + e.getMessage());
            e.printStackTrace();
        } 
        
        return list;
    }

    public boolean themCTHD(ChiTietHoaDon cthd) {
        if (cthd == null
                || cthd.getHoaDonId() == null
                || cthd.getDonViDoLuongId() == null
                || cthd.getSanPhamId() == null) {
            return false;
        }

        String sql = "INSERT INTO ChiTietHoaDon (hoaDonId, donViDoLuongId, sanPhamId, soLuong) VALUES (?, ?, ?, ?)";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, cthd.getHoaDonId().getId());
            pst.setString(2, cthd.getDonViDoLuongId().getId());
            pst.setString(3, cthd.getSanPhamId().getId());
            pst.setInt(4, cthd.getSoLuong());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    
    public boolean themCTHD(Connection con, ChiTietHoaDon cthd) throws SQLException {
        if (cthd == null
                || cthd.getHoaDonId() == null
                || cthd.getDonViDoLuongId() == null
                || cthd.getSanPhamId() == null) {
            return false;
        }

        String sql = "INSERT INTO ChiTietHoaDon (hoaDonId, donViDoLuongId, sanPhamId, soLuong) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, cthd.getHoaDonId().getId());
            pst.setString(2, cthd.getDonViDoLuongId().getId());
            pst.setString(3, cthd.getSanPhamId().getId());
            pst.setInt(4, cthd.getSoLuong());

            return pst.executeUpdate() > 0;
        }
    }

    // Hàm nạp dữ liệu riêng cho bảng Tạo Hóa Đơn (Lưu nháp)
    public List<Object[]> layDuLieuChoTaoHoaDon(String maHD) {
        List<Object[]> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection(); 
        
        String sql = "SELECT sp.ten AS TenSP, dv.ten AS DVT, ct.soLuong AS SL, dv.gia AS DonGia " +
                     "FROM ChiTietHoaDon ct " +
                     "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                     "JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id AND ct.sanPhamId = dv.sanPhamId " +
                     "WHERE ct.hoaDonId = ?";
                     
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setString(1, maHD);
            
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    String tenSP = rs.getString("TenSP");
                    String dvt = rs.getString("DVT");
                    int sl = rs.getInt("SL");
                    double donGia = rs.getDouble("DonGia");
                    
                    double thanhTien = sl * donGia; // Tính tổng thành tiền chưa giảm giá

                    // Format chuỗi tiền tệ (vd: 50.000đ) để in lên bảng UI cho đẹp
                    String strDonGia = String.format("%,d", (long)donGia).replace(',', '.') + "đ";
                    String strThanhTien = String.format("%,d", (long)thanhTien).replace(',', '.') + "đ";

                    // Bảng Tạo Hóa Đơn của bạn có thứ tự cột: 
                    // Tên | ĐVT | SL | Đơn giá | Giảm giá | Thành tiền | Ghi chú
                    Object[] row = new Object[]{
                        tenSP,
                        dvt,
                        String.valueOf(sl),
                        strDonGia,
                        "0", // Tiền giảm mặc định là 0 (hoặc thay bằng logic KM nếu có)
                        strThanhTien,
                        ""   // Ghi chú
                    };
                    list.add(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ChiTietHoaDon> layDSChiTietHD(String maHoaDon) {
        List<ChiTietHoaDon> dsCTHD = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietHoaDon WHERE hoaDonId = ?";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maHoaDon);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ChiTietHoaDon ct = new ChiTietHoaDon();

                    HoaDon hd = new HoaDon();
                    hd.setId(rs.getString("hoaDonId"));
                    ct.setHoaDonId(hd);

                    DonViDoLuong dvdl = new DonViDoLuong();
                    dvdl.setId(rs.getString("donViDoLuongId"));
                    ct.setDonViDoLuongId(dvdl);

                    SanPham sp = new SanPham();
                    sp.setId(rs.getString("sanPhamId"));
                    ct.setSanPhamId(sp);

                    ct.setSoLuong(rs.getInt("soLuong"));
                    dsCTHD.add(ct);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dsCTHD;
    }
}