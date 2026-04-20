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
     * Lấy danh sách các đơn vị tính và giá tiền của một sản phẩm
     * @param maSP ID của sản phẩm cần lấy giá
     * @return Danh sách DonViDoLuong (Hộp, Vỉ, Viên...) kèm giá tương ứng
     */
    public List<DonViDoLuong> getDSTheoMaSP(String maSP) {
        List<DonViDoLuong> ds = new ArrayList<>();
        // Câu lệnh SQL lấy đơn vị tính và giá từ bảng DonViDoLuong
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
                 // DÒNG ĐÃ SỬA LỖI (Gọi đúng tên cột heSoQuyDoi):
                    double heSo = rs.getDouble("chuyenDoiDonViCoBan");
                    
                    // Gắn ngược lại mã SP để đúng cấu trúc Entity
                    SanPham sp = new SanPham();
                    sp.setId(maSP);
                    dv.setSanPhamId(sp);
                    
                    ds.add(dv);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi SQL DAO_DonViDoLuong: " + e.getMessage());
            e.printStackTrace();
        }
        return ds;
    }
}