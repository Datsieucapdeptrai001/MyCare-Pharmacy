package DAO;

import ConnectDB.ConnectDB;
import Entity.KhachHang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DAO_KhachHang {

    public DAO_KhachHang() {
    }

    // Lấy toàn bộ danh sách khách hàng
    public List<KhachHang> getDSKhachHang() {
        List<KhachHang> dsKhachHang = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang";
        Connection con = ConnectDB.getInstance().getConnection();

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                KhachHang kh = new KhachHang();
                kh.setId(rs.getString("id"));
                kh.setSdt(rs.getString("sdt"));
                kh.setHoVaTen(rs.getString("hoVaTen"));
                
                if (rs.getTimestamp("ngayTao") != null) {
                    kh.setNgayTao(rs.getTimestamp("ngayTao").toLocalDateTime());
                }
                
                // Nếu DB của bạn có thêm cột diemTichLuy, bạn gọi: 
                // kh.setDiemTichLuy(rs.getInt("diemTichLuy"));

                dsKhachHang.add(kh);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsKhachHang;
    }

    // Thêm khách hàng mới
    public boolean themKhachHang(KhachHang kh) {
        String sql = "INSERT INTO KhachHang (id, sdt, hoVaTen, ngayTao) VALUES (?, ?, ?, ?)";
        int n = 0;
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, kh.getId());
            pst.setString(2, kh.getSdt());
            pst.setString(3, kh.getHoVaTen());
            
            LocalDateTime ngayTao = kh.getNgayTao() != null ? kh.getNgayTao() : LocalDateTime.now();
            pst.setTimestamp(4, Timestamp.valueOf(ngayTao));

            n = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    // Cập nhật thông tin khách hàng (Thường chỉ cho phép cập nhật Tên hoặc SĐT)
    public boolean capNhatKhachHang(KhachHang kh) {
        String sql = "UPDATE KhachHang SET sdt = ?, hoVaTen = ? WHERE id = ?";
        int n = 0;
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, kh.getSdt());
            pst.setString(2, kh.getHoVaTen());
            pst.setString(3, kh.getId()); // ID là không đổi

            n = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    // Lấy thông tin khách hàng bằng Số điện thoại (Rất tiện khi khách đọc SĐT lúc thanh toán)
    public KhachHang getKhachHangTheoSDT(String sdt) {
        KhachHang kh = null;
        String sql = "SELECT * FROM KhachHang WHERE sdt = ?";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, sdt);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    kh = new KhachHang();
                    kh.setId(rs.getString("id"));
                    kh.setSdt(rs.getString("sdt"));
                    kh.setHoVaTen(rs.getString("hoVaTen"));
                    if (rs.getTimestamp("ngayTao") != null) kh.setNgayTao(rs.getTimestamp("ngayTao").toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kh;
    }
}