package DAO;

import ConnectDB.ConnectDB;
import Entity.CaLamViec;
import Entity.NhanVien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DAO_CaLamViec {

    public DAO_CaLamViec() {
    }

    // Thêm ca làm việc mới (Mở ca)
    public boolean themCa(CaLamViec ca) {
        String sql = "INSERT INTO CaLamViec (id, nhanVienId, thoiGianBatDau, thoiGianKetThuc, tienHeThongGhiNhan, tienDauCa, tienKetCa) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int n = 0;
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, ca.getId());
            pst.setString(2, ca.getNhanVienId().getNhanVien()); // Lấy mã nhân viên
            
            // Thời gian bắt đầu bắt buộc phải có
            pst.setTimestamp(3, Timestamp.valueOf(ca.getThoiGianBatDau()));
            
            // Thời gian kết thúc ban đầu có thể null
            if (ca.getThoiGianKetThuc() != null) {
                pst.setTimestamp(4, Timestamp.valueOf(ca.getThoiGianKetThuc()));
            } else {
                pst.setNull(4, java.sql.Types.TIMESTAMP);
            }
            
            pst.setDouble(5, ca.getTienHeThongGhiNhan());
            pst.setDouble(6, ca.getTienDauCa());
            pst.setDouble(7, ca.getTienKetCa());

            n = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    // Cập nhật ca (Dùng khi Chốt ca/Kết thúc ca)
    public boolean capNhatCa(CaLamViec ca) {
        String sql = "UPDATE CaLamViec SET thoiGianKetThuc=?, tienHeThongGhiNhan=?, tienKetCa=? WHERE id=?";
        int n = 0;
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            if (ca.getThoiGianKetThuc() != null) {
                pst.setTimestamp(1, Timestamp.valueOf(ca.getThoiGianKetThuc()));
            } else {
                pst.setNull(1, java.sql.Types.TIMESTAMP);
            }
            
            pst.setDouble(2, ca.getTienHeThongGhiNhan());
            pst.setDouble(3, ca.getTienKetCa());
            pst.setString(4, ca.getId()); // Điều kiện cập nhật theo ID ca

            n = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    // Lấy ca đang diễn ra của một nhân viên (Ca chưa có thời gian kết thúc)
    public CaLamViec getCaHienTai(String maNhanVien) {
        CaLamViec ca = null;
        String sql = "SELECT * FROM CaLamViec WHERE nhanVienId=? AND thoiGianKetThuc IS NULL";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maNhanVien);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    ca = new CaLamViec();
                    ca.setId(rs.getString("id"));
                    
                    NhanVien nv = new NhanVien();
                    nv.setNhanVien(rs.getString("nhanVienId"));
                    ca.setNhanVienId(nv);
                    
                    ca.setThoiGianBatDau(rs.getTimestamp("thoiGianBatDau").toLocalDateTime());
                    
                    // Xử lý an toàn nếu thoiGianKetThuc null
                    Timestamp thoiGianKetThuc = rs.getTimestamp("thoiGianKetThuc");
                    if (thoiGianKetThuc != null) {
                        ca.setThoiGianKetThuc(thoiGianKetThuc.toLocalDateTime());
                    }
                    
                    ca.setTienHeThongGhiNhan(rs.getDouble("tienHeThongGhiNhan"));
                    ca.setTienDauCa(rs.getDouble("tienDauCa"));
                    ca.setTienKetCa(rs.getDouble("tienKetCa"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ca;
    }

    // Lấy danh sách toàn bộ lịch sử ca làm việc (Sắp xếp mới nhất lên đầu)
    public List<CaLamViec> getLichSuCa() {
        List<CaLamViec> dsCa = new ArrayList<>();
        String sql = "SELECT * FROM CaLamViec ORDER BY thoiGianBatDau DESC";
        Connection con = ConnectDB.getInstance().getConnection();

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                CaLamViec ca = new CaLamViec();
                ca.setId(rs.getString("id"));
                
                NhanVien nv = new NhanVien();
                nv.setNhanVien(rs.getString("nhanVienId"));
                ca.setNhanVienId(nv);
                
                ca.setThoiGianBatDau(rs.getTimestamp("thoiGianBatDau").toLocalDateTime());
                
                Timestamp thoiGianKetThuc = rs.getTimestamp("thoiGianKetThuc");
                if (thoiGianKetThuc != null) {
                    ca.setThoiGianKetThuc(thoiGianKetThuc.toLocalDateTime());
                }
                
                ca.setTienHeThongGhiNhan(rs.getDouble("tienHeThongGhiNhan"));
                ca.setTienDauCa(rs.getDouble("tienDauCa"));
                ca.setTienKetCa(rs.getDouble("tienKetCa"));
                
                dsCa.add(ca);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsCa;
    }
}