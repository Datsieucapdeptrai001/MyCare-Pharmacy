package DAO;

import ConnectDB.ConnectDB;
import Entity.DonViDoLuong;
import Entity.SanPham;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DAO_DonViDoLuong {

    public DAO_DonViDoLuong() {}

    /**
     * Lấy danh sách các đơn vị tính và giá tiền của một sản phẩm theo MÃ SẢN PHẨM
     */
    public List<DonViDoLuong> getDSTheoMaSP(String maSP) {
        List<DonViDoLuong> ds = new ArrayList<>();
        String sql = "SELECT * FROM DonViDoLuong WHERE sanPhamId = ?";
        
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, maSP);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    DonViDoLuong dv = new DonViDoLuong();
                    dv.setId(rs.getString("id"));
                    dv.setTen(rs.getString("ten"));
                    dv.setGia(rs.getDouble("gia"));
                    dv.setChuyenDoiSangDonViCoBan(rs.getDouble("chuyenDoiDonViCoBan")); 
                    
                    SanPham sp = new SanPham();
                    sp.setId(maSP);
                    dv.setSanPhamId(sp);
                    
                    ds.add(dv);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi SQL DAO_DonViDoLuong (getDSTheoMaSP): " + e.getMessage());
            e.printStackTrace();
        }
        return ds;
    }

    /**
     * Lấy danh sách các đơn vị tính của một sản phẩm theo TÊN SẢN PHẨM
     * (Hàm này được BUS gọi để phục vụ tự động load Combobox trên GUI)
     */
    public List<DonViDoLuong> getDSTheoTenSP(String tenSP) {
        List<DonViDoLuong> ds = new ArrayList<>();
        // Truy vấn JOIN 2 bảng để tìm Đơn vị đo lường thông qua tên Sản Phẩm.
        // Dùng LIKE thay vì = để tìm kiếm an toàn và linh hoạt hơn.
        String sql = "SELECT d.* FROM DonViDoLuong d INNER JOIN SanPham s ON d.sanPhamId = s.id WHERE s.ten LIKE ?";
        
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            // Gắn thêm % để so khớp gần đúng nếu chuỗi có khoảng trắng dư thừa
            pst.setString(1, "%" + tenSP.trim() + "%");
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    DonViDoLuong dv = new DonViDoLuong();
                    dv.setId(rs.getString("id"));
                    dv.setTen(rs.getString("ten"));
                    dv.setGia(rs.getDouble("gia"));
                    dv.setChuyenDoiSangDonViCoBan(rs.getDouble("chuyenDoiDonViCoBan"));
                    
                    // Gắn ID sản phẩm vào Entity cho đúng cấu trúc
                    SanPham sp = new SanPham();
                    sp.setId(rs.getString("sanPhamId"));
                    dv.setSanPhamId(sp);
                    
                    ds.add(dv);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi SQL DAO_DonViDoLuong (getDSTheoTenSP): " + e.getMessage());
            e.printStackTrace();
        }
        return ds;
    }
}