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
                kh.setDiemTichLuy(rs.getInt("diemTichLuy"));

                if (rs.getTimestamp("ngayTao") != null) {
                    kh.setNgayTao(rs.getTimestamp("ngayTao").toLocalDateTime());
                }

                dsKhachHang.add(kh);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsKhachHang;
    }

    // Thêm khách hàng mới
    public boolean themKhachHang(KhachHang kh) {
        String sql = "INSERT INTO KhachHang (id, sdt, hoVaTen, ngayTao, diemTichLuy) VALUES (?, ?, ?, ?, ?)";
        int n = 0;
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, kh.getId());
            pst.setString(2, kh.getSdt());
            pst.setString(3, kh.getHoVaTen());

            LocalDateTime ngayTao = kh.getNgayTao() != null ? kh.getNgayTao() : LocalDateTime.now();
            pst.setTimestamp(4, Timestamp.valueOf(ngayTao));

            pst.setInt(5, kh.getDiemTichLuy());

            n = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    // Cập nhật thông tin khách hàng
    public boolean capNhatKhachHang(KhachHang kh) {
        String sql = "UPDATE KhachHang SET sdt = ?, hoVaTen = ?, diemTichLuy = ? WHERE id = ?";
        int n = 0;
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, kh.getSdt());
            pst.setString(2, kh.getHoVaTen());
            pst.setInt(3, kh.getDiemTichLuy());
            pst.setString(4, kh.getId());

            n = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    // Lấy thông tin khách hàng bằng số điện thoại
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
                    kh.setDiemTichLuy(rs.getInt("diemTichLuy"));

                    if (rs.getTimestamp("ngayTao") != null) {
                        kh.setNgayTao(rs.getTimestamp("ngayTao").toLocalDateTime());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kh;
    }

    // Cập nhật điểm tích lũy
    public boolean capNhatDiemTichLuy(String id, int diemMoi) {
        String sql = "UPDATE KhachHang SET diemTichLuy = ? WHERE id = ?";
        int n = 0;
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, diemMoi);
            pst.setString(2, id);

            n = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return n > 0;
    }
}