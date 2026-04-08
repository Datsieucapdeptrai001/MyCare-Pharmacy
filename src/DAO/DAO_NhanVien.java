package DAO;

import ConnectDB.ConnectDB;
import Entity.NhanVien;
import Enum.ChucVu;
import Enum.TrangThaiLamViec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DAO_NhanVien {

    public DAO_NhanVien() {
    }

    // Lấy toàn bộ danh sách nhân viên
    public List<NhanVien> layDSNhanVien() {
        List<NhanVien> dsNhanVien = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien";
        
        // Lấy kết nối từ lớp ConnectDB của bạn
        Connection con = ConnectDB.getInstance().getConnection();

        if (con == null) {
            System.out.println("Lỗi: Chưa kết nối CSDL. Vui lòng gọi ConnectDB.getInstance().connect() trước.");
            return dsNhanVien;
        }

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setNhanVien(rs.getString("nhanVien")); // Mã nhân viên
                nv.setHoVaTen(rs.getString("hoVaTen"));
                nv.setSoChungChiHanhNghe(rs.getString("soChungChiHanhNghe"));
                nv.setSdt(rs.getString("sdt"));
                nv.setEmail(rs.getString("email"));
                
                // Ép kiểu chuỗi từ SQL Server sang Enum của Java
                nv.setChucVu(ChucVu.valueOf(rs.getString("chucVu")));
                nv.setTrangThaiLamViec(TrangThaiLamViec.valueOf(rs.getString("trangThaiLamViec")));

                dsNhanVien.add(nv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsNhanVien;
    }

    // Thêm nhân viên mới
    public boolean themNhanVien(NhanVien nv) {
        String sql = "INSERT INTO NhanVien (nhanVien, hoVaTen, soChungChiHanhNghe, sdt, email, chucVu, trangThaiLamViec) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int n = 0;
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, nv.getNhanVien());
            pst.setString(2, nv.getHoVaTen());
            pst.setString(3, nv.getSoChungChiHanhNghe());
            pst.setString(4, nv.getSdt());
            pst.setString(5, nv.getEmail());
            pst.setString(6, nv.getChucVu().name()); // Lưu dưới dạng chuỗi
            pst.setString(7, nv.getTrangThaiLamViec().name()); // Lưu dưới dạng chuỗi
            
            n = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    // Cập nhật thông tin nhân viên
    public boolean capNhatNhanVien(NhanVien nv) {
        String sql = "UPDATE NhanVien SET hoVaTen=?, soChungChiHanhNghe=?, sdt=?, email=?, chucVu=?, trangThaiLamViec=? WHERE nhanVien=?";
        int n = 0;
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, nv.getHoVaTen());
            pst.setString(2, nv.getSoChungChiHanhNghe());
            pst.setString(3, nv.getSdt());
            pst.setString(4, nv.getEmail());
            pst.setString(5, nv.getChucVu().name());
            pst.setString(6, nv.getTrangThaiLamViec().name());
            pst.setString(7, nv.getNhanVien()); // Điều kiện WHERE để tìm đúng nhân viên
            
            n = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    // Tìm kiếm chính xác một nhân viên theo mã
    public NhanVien layNhanVienTheoMa(String maNV) {
        NhanVien nv = null;
        String sql = "SELECT * FROM NhanVien WHERE nhanVien=?";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maNV);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    nv = new NhanVien();
                    nv.setNhanVien(rs.getString("nhanVien"));
                    nv.setHoVaTen(rs.getString("hoVaTen"));
                    nv.setSoChungChiHanhNghe(rs.getString("soChungChiHanhNghe"));
                    nv.setSdt(rs.getString("sdt"));
                    nv.setEmail(rs.getString("email"));
                    nv.setChucVu(ChucVu.valueOf(rs.getString("chucVu")));
                    nv.setTrangThaiLamViec(TrangThaiLamViec.valueOf(rs.getString("trangThaiLamViec")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nv;
    }
}