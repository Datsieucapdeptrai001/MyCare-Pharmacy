package DAO;

import ConnectDB.ConnectDB;
import Entity.ChiTietHoaDon;
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

    public boolean themCTHD(ChiTietHoaDon cthd) {
        String sql = "INSERT INTO ChiTietHoaDon (hoaDonId, donViDoLuongId, sanPhamId, soLuong) VALUES (?, ?, ?, ?)";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, cthd.getHoaDonId().getId());
            
            // Giả định DonViDoLuong có hàm getId() hoặc bạn lưu tên đơn vị
            // pst.setString(2, cthd.getDonViDoLuongId().getId()); 
            pst.setString(2, null); // Tạm set null nếu chưa xử lý DonViDoLuong
            
            pst.setString(3, cthd.getSanPhamId().getId());
            pst.setInt(4, cthd.getSoLuong());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Cần truyền mã Hóa Đơn vào để lấy đúng danh sách chi tiết của Hóa Đơn đó
    public List<ChiTietHoaDon> layDSChiTietHD(String maHoaDon) {
        List<ChiTietHoaDon> dsCTHD = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietHoaDon WHERE hoaDonId = ?";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maHoaDon);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ChiTietHoaDon ct = new ChiTietHoaDon();
                    HoaDon hd = new HoaDon(); hd.setId(rs.getString("hoaDonId"));
                    ct.setHoaDonId(hd);
                    
                    SanPham sp = new SanPham(); sp.setId(rs.getString("sanPhamId"));
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