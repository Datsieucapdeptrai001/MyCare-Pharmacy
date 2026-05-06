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

    public boolean themChiTietDoiTra(String maHD, String tenSP, int soLuong, String ghiChu) {
        // SỬA LỖI 1: Tên cột là 'ten' chứ không phải 'tenSanPham'
        // SỬA LỖI 2: Lấy luôn donViDoLuongId để tránh bị null làm tàng hình sản phẩm
        String sql = "INSERT INTO ChiTietHoaDon (hoaDonId, sanPhamId, donViDoLuongId, soLuong, ghiChu) " +
                     "SELECT TOP 1 ?, sp.id, dv.id, ?, ? " +
                     "FROM SanPham sp " +
                     "LEFT JOIN DonViDoLuong dv ON sp.id = dv.sanPhamId " +
                     "WHERE sp.ten = ?";
                     
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maHD);
            pst.setInt(2, soLuong);
            pst.setString(3, ghiChu);
            pst.setString(4, tenSP); // Tìm theo cột 'ten'
            
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Object[]> layDanhSachSanPhamTheoMaHD(String maHD) {
        List<Object[]> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        
        // SỬA LỖI 3: Dùng LEFT JOIN để an toàn tuyệt đối, kể cả khi donViDoLuong bị Null
        String sql = "SELECT sp.ten AS TenSP, dv.ten AS DVT, ct.soLuong AS SL, " +
                     "dv.gia AS DonGia, sp.thueVAT AS VAT " +
                     "FROM ChiTietHoaDon ct " +
                     "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                     "LEFT JOIN DonViDoLuong dv ON sp.id = dv.sanPhamId " + 
                     "WHERE ct.hoaDonId = ?";
                     
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setString(1, maHD);
            
            try (ResultSet rs = pstm.executeQuery()) {
                int stt = 1;
                while (rs.next()) {
                    String tenSP = rs.getString("TenSP");
                    String dvt = rs.getString("DVT") != null ? rs.getString("DVT") : "Hộp";
                    int sl = rs.getInt("SL");
                    double donGia = rs.getDouble("DonGia");
                    double vatPercent = rs.getDouble("VAT"); 
                    
                    if (vatPercent > 0 && vatPercent < 1) {
                        vatPercent = vatPercent * 100;
                    }

                    double tienChuaVAT = sl * donGia;
                    double tienVAT = tienChuaVAT * (vatPercent / 100);
                    double thanhTien = tienChuaVAT + tienVAT;

                    String strDonGia = String.format("%,d", (long)donGia).replace(',', '.') + "đ";
                    String strThanhTien = String.format("%,d", (long)thanhTien).replace(',', '.') + "đ";
                    String strVAT = (int)vatPercent + "%"; 

                    Object[] row = new Object[]{
                        String.valueOf(stt++), tenSP, dvt, String.valueOf(sl),
                        strDonGia, strVAT, strThanhTien
                    };
                    
                    list.add(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
        
        return list;
    }

    public boolean themCTHD(ChiTietHoaDon cthd) {
        if (cthd == null || cthd.getHoaDonId() == null || cthd.getDonViDoLuongId() == null || cthd.getSanPhamId() == null) {
            return false;
        }
        String sql = "INSERT INTO ChiTietHoaDon (hoaDonId, donViDoLuongId, sanPhamId, soLuong) VALUES (?, ?, ?, ?)";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
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
        if (cthd == null || cthd.getHoaDonId() == null || cthd.getDonViDoLuongId() == null || cthd.getSanPhamId() == null) {
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

    public List<Object[]> layDuLieuChoTaoHoaDon(String maHD) {
        List<Object[]> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection(); 
        
        // SỬA LỖI 4: Dùng LEFT JOIN và Tách riêng Sản phẩm "TRA_LAI" không để lọt sản phẩm "DOI_LAY" vào bảng trả
        String sql = "SELECT sp.ten AS TenSP, dv.ten AS DVT, ct.soLuong AS SL, dv.gia AS DonGia " +
                     "FROM ChiTietHoaDon ct " +
                     "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                     "LEFT JOIN DonViDoLuong dv ON sp.id = dv.sanPhamId " +
                     "WHERE ct.hoaDonId = ?";

        // Nếu là phiếu DTH, chỉ bóc các SP khách trả lại (TRA_LAI) lên bảng chính
        if (maHD != null && maHD.startsWith("DTH")) {
            sql += " AND ct.ghiChu = 'TRA_LAI'";
        }
                     
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setString(1, maHD);
            
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    String tenSP = rs.getString("TenSP");
                    String dvt = rs.getString("DVT") != null ? rs.getString("DVT") : "Hộp";
                    int sl = rs.getInt("SL");
                    double donGia = rs.getDouble("DonGia");
                    
                    double thanhTien = sl * donGia; 

                    String strDonGia = String.format("%,d", (long)donGia).replace(',', '.') + "đ";
                    String strThanhTien = String.format("%,d", (long)thanhTien).replace(',', '.') + "đ";

                    Object[] row = new Object[]{
                        tenSP, dvt, String.valueOf(sl), strDonGia, "0", strThanhTien, ""   
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