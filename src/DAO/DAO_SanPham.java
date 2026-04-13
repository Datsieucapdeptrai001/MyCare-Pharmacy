package DAO;

import ConnectDB.ConnectDB;
import Entity.LoHang;
import Entity.SanPham;
import Enumeration.DangBaoChe;
import Enumeration.DanhMucSanPham;
import Enumeration.TrangThaiLoHang;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DAO_SanPham {

    public DAO_SanPham() {
    }
    public List<Object[]> layDanhSachSanPhamChoBang() {
        List<Object[]> ds = new ArrayList<>();
        // Câu lệnh lấy danh sách sản phẩm. Tùy vào CSDL của bạn có cột nhaSanXuat, thueVAT không, 
        // ở đây dùng ISNULL để đảm bảo không bị lỗi nếu cột bị trống
        String sql = "SELECT id, ten, danhMuc, ISNULL(hoatChat, '') AS hoatChat, dang, " +
                     "ISNULL(nhaSanXuat, 'Khác') AS nhaSanXuat, ISNULL(thueVAT, 0) AS thueVAT " +
                     "FROM SanPham";
        
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
             
            while (rs.next()) {
                String ma = rs.getString("id");
                String ten = rs.getString("ten");
                
                // 1. Chuyển đổi Danh Mục (Enum) sang Tiếng Việt
                String danhMucDB = rs.getString("danhMuc");
                String loai = "Sản phẩm chức năng"; // Mặc định
                if ("THUOC_KE_DON".equals(danhMucDB)) loai = "Thuốc kê đơn";
                else if ("THUOC_KHONG_KE_DON".equals(danhMucDB)) loai = "Thuốc không kê đơn";
                
                String hoatChat = rs.getString("hoatChat");
                
                // 2. Chuyển đổi Dạng Bào Chế (Enum)
                String dangDB = rs.getString("dang");
                String dang = "Viên nén"; 
                if ("DANG_LONG".equals(dangDB)) dang = "Dung dịch";
                
                String nsx = rs.getString("nhaSanXuat");
                String vat = rs.getDouble("thueVAT") + "%";
                
                // 3. Gom vào mảng Object theo đúng thứ tự 7 cột của Bảng
                ds.add(new Object[]{ma, ten, loai, hoatChat, dang, nsx, vat});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }
    // 1. Lấy danh sách toàn bộ Thuốc/Sản phẩm theo đúng tên hàm trong sơ đồ
    public List<SanPham> getDsThuoc() {
        List<SanPham> dsSanPham = new ArrayList<>();
        String sql = "SELECT * FROM SanPham";
        Connection con = ConnectDB.getInstance().getConnection();

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                SanPham sp = new SanPham();
                sp.setId(rs.getString("id"));
                
                if (rs.getString("danhMuc") != null) sp.setDanhMuc(DanhMucSanPham.valueOf(rs.getString("danhMuc")));
                if (rs.getString("dang") != null) sp.setDang(DangBaoChe.valueOf(rs.getString("dang")));
                
                sp.setTen(rs.getString("ten"));
                sp.setTenVietTat(rs.getString("tenVietTat"));
                sp.setNhaSanXuat(rs.getString("nhaSanXuat"));
                sp.setHoatChat(rs.getString("hoatChat"));
                sp.setThueVAT(rs.getDouble("thueVAT"));
                sp.setHamLuong(rs.getString("hamLuong"));
                sp.setMoTa(rs.getString("moTa"));
                sp.setDonViDoCoBan(rs.getString("donViDoCoBan"));
                
                if (rs.getTimestamp("ngayTao") != null) {
                    sp.setNgayTao(rs.getTimestamp("ngayTao").toLocalDateTime());
                }

                dsSanPham.add(sp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsSanPham;
    }

    // [HÀM BỔ SUNG CHO TRA CỨU]: Lấy chi tiết 1 sản phẩm theo ID
    public SanPham getSanPhamTheoMa(String id) {
        SanPham sp = null;
        String sql = "SELECT * FROM SanPham WHERE id = ?";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    sp = new SanPham();
                    sp.setId(rs.getString("id"));
                    
                    if (rs.getString("danhMuc") != null) sp.setDanhMuc(DanhMucSanPham.valueOf(rs.getString("danhMuc")));
                    if (rs.getString("dang") != null) sp.setDang(DangBaoChe.valueOf(rs.getString("dang")));
                    
                    sp.setTen(rs.getString("ten"));
                    sp.setTenVietTat(rs.getString("tenVietTat"));
                    sp.setNhaSanXuat(rs.getString("nhaSanXuat"));
                    sp.setHoatChat(rs.getString("hoatChat"));
                    sp.setThueVAT(rs.getDouble("thueVAT"));
                    sp.setHamLuong(rs.getString("hamLuong"));
                    sp.setMoTa(rs.getString("moTa"));
                    sp.setDonViDoCoBan(rs.getString("donViDoCoBan"));
                    
                    if (rs.getTimestamp("ngayTao") != null) {
                        sp.setNgayTao(rs.getTimestamp("ngayTao").toLocalDateTime());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sp;
    }

    // 2. Lấy danh sách Lô hàng theo Sản phẩm (Đã FIX: Bổ sung lấy ngày hết hạn)
    public List<LoHang> layLoTheoSP(String maSP) {
        List<LoHang> dsLoHang = new ArrayList<>();
        String sql = "SELECT * FROM LoHang WHERE sanPhamId = ? AND soLuongLoHang > 0 AND trangThai != 'HET_HAN' ORDER BY ngayHetHan ASC";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maSP);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    LoHang lh = new LoHang();
                    lh.setId(rs.getString("id"));
                    lh.setSoLoHang(rs.getString("soLoHang"));
                    lh.setSoLuongLoHang(rs.getInt("soLuongLoHang"));
                    lh.setGia(rs.getInt("gia"));
                    if (rs.getString("trangThai") != null) lh.setTrangThai(TrangThaiLoHang.valueOf(rs.getString("trangThai")));
                    
                    // [BỔ SUNG QUAN TRỌNG]: Lấy ngày hết hạn để phục vụ thuật toán FEFO trừ kho
                    if (rs.getTimestamp("ngayHetHan") != null) lh.setNgayHetHan(rs.getTimestamp("ngayHetHan").toLocalDateTime());
                    if (rs.getTimestamp("ngayNhap") != null) lh.setNgayNhap(rs.getTimestamp("ngayNhap").toLocalDateTime());
                    
                    // Gắn ID sản phẩm vào lô
                    SanPham sp = new SanPham(); sp.setId(rs.getString("sanPhamId"));
                    lh.setSanPhamId(sp);
                    
                    dsLoHang.add(lh);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsLoHang;
    }
    public java.util.List<Object[]> timKiemSanPhamBan(String keyword) {
        java.util.List<Object[]> list = new java.util.ArrayList<>();
        java.sql.Connection con = null;
        java.sql.PreparedStatement pstm = null;
        java.sql.ResultSet rs = null;

        try {
            // Thay bằng class Connection thật của bạn (VD: Database.getInstance().getConnection())
            con = ConnectDB.getConnection(); 
            
            // CÂU SQL: Nối 3 bảng SanPham, DonViDoLuong và LoHang (để lấy tồn kho)
            String sql = "SELECT " +
                         "    sp.id, " +
                         "    sp.ten, " +
                         "    dv.ten AS donVi, " +
                         "    dv.gia AS giaBan, " +
                         "    ISNULL(SUM(lh.soLuongLoHang), 0) AS tonKho " +
                         "FROM SanPham sp " +
                         "JOIN DonViDoLuong dv ON sp.id = dv.sanPhamId " +
                         "LEFT JOIN LoHang lh ON sp.id = lh.sanPhamId AND lh.trangThai = N'CON_HANG' AND lh.ngayHetHan > GETDATE() " +
                         "WHERE sp.ten LIKE ? OR sp.tenVietTat LIKE ? OR sp.hoatChat LIKE ? OR sp.id LIKE ? " +
                         "GROUP BY sp.id, sp.ten, dv.ten, dv.gia";

            pstm = con.prepareStatement(sql);
            String searchPattern = "%" + keyword + "%";
            pstm.setString(1, searchPattern);
            pstm.setString(2, searchPattern);
            pstm.setString(3, searchPattern);
            pstm.setString(4, searchPattern);

            rs = pstm.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[5];
                row[0] = rs.getString("id");        // Mã SP
                row[1] = rs.getString("ten");       // Tên SP
                row[2] = rs.getString("donVi");     // Đơn vị tính
                row[3] = rs.getDouble("giaBan");    // Giá bán
                row[4] = rs.getInt("tonKho");       // Tồn kho

                list.add(row);
            }
        } catch (Exception e) {
            System.err.println("Lỗi SQL tại timKiemSanPhamBan: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstm != null) pstm.close();
            } catch (Exception ex) {}
        }
        return list;
    }
    // 3. Cập nhật số lượng tồn kho
    public boolean capNhatSoLuongTon(String maLoHang, int soLuongMoi) {
        String sql = "UPDATE LoHang SET soLuongLoHang = ? WHERE id = ?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, soLuongMoi);
            pst.setString(2, maLoHang);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}