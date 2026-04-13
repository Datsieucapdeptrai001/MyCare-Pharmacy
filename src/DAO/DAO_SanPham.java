package DAO;

import ConnectDB.ConnectDB;
import Entity.LoHang;
import Entity.SanPham;
import Enum.DangBaoChe;
import Enum.DanhMucSanPham;
import Enum.TrangThaiLoHang;

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
 // Nhớ import các thư viện: java.util.List, java.util.ArrayList, java.sql.*
    public List<Object[]> timKiemSanPhamBan(String tuKhoa) {
        List<Object[]> list = new ArrayList<>();
        
        // Truy vấn: Tìm theo Tên hoặc Mã SP. Chỉ lấy SP còn tồn kho > 0
        String sql = "SELECT sp.id, sp.ten, dv.tenDonVi, dv.gia, ISNULL(SUM(lh.soLuongLoHang), 0) AS tonKho " +
                     "FROM SanPham sp " +
                     "JOIN DonViDoLuong dv ON sp.id = dv.sanPhamId " +
                     "LEFT JOIN LoHang lh ON sp.id = lh.sanPhamId AND lh.trangThai = 'CON_HANG' " +
                     "WHERE sp.ten LIKE ? OR sp.id LIKE ? " +
                     "GROUP BY sp.id, sp.ten, dv.tenDonVi, dv.gia " +
                     "HAVING ISNULL(SUM(lh.soLuongLoHang), 0) > 0"; 

        // [ĐÃ SỬA]: Kéo Connection ra ngoài khối try() để tránh bị tự động close()
        // Vì trong ConnectDB.java của bạn, getConnection() là hàm static nên gọi trực tiếp luôn
        Connection con = ConnectDB.getConnection();

        // Chỉ đưa PreparedStatement vào try-with-resources để tự động giải phóng bộ nhớ câu lệnh SQL
        try (PreparedStatement pst = con.prepareStatement(sql)) {

            // Thêm % vào 2 đầu để tìm kiếm gần đúng (chứa từ khóa)
            String searchPattern = "%" + tuKhoa + "%";
            pst.setString(1, searchPattern);
            pst.setString(2, searchPattern);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString("id");
                    String ten = rs.getString("ten");
                    String donVi = rs.getString("tenDonVi");
                    String gia = String.valueOf(Math.round(rs.getDouble("gia"))); // Bỏ phần thập phân .0
                    String tonKho = String.valueOf(rs.getInt("tonKho"));

                    // Đưa vào mảng Object để trả về
                    list.add(new Object[]{id, ten, donVi, gia, tonKho});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
 // =====================================================================
    // CÁC HÀM BỔ SUNG ĐỂ THÊM/SỬA/XÓA/TẠO MÃ TỰ ĐỘNG XUỐNG DATABASE
    // =====================================================================
    
    // 1. Tự động sinh mã SP mới nhất (VD: Từ SP2024-0032 -> SP2024-0033)
    public String layMaSanPhamMoiNhat() {
        String sql = "SELECT TOP 1 id FROM SanPham ORDER BY id DESC";
        Connection con = ConnectDB.getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                String lastId = rs.getString("id"); // VD: SP2024-0042
                int number = Integer.parseInt(lastId.split("-")[1]);
                return String.format("SP2024-%04d", number + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "SP2024-0001"; // Trả về mã đầu tiên nếu bảng rỗng
    }

    // 2. Thêm Sản Phẩm mới thẳng vào DB
    public boolean themSanPhamNhanh(String id, String danhMuc, String dang, String ten, String vietTat, String nsx, String hoatChat, double vat, String hamLuong, String moTa, String dvt) {
        String sql = "INSERT INTO SanPham (id, danhMuc, dang, ten, tenVietTat, nhaSanXuat, hoatChat, thueVAT, hamLuong, moTa, donViDoCoBan, ngayTao) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";
        Connection con = ConnectDB.getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, id); pst.setString(2, danhMuc); pst.setString(3, dang);
            pst.setString(4, ten); pst.setString(5, vietTat); pst.setString(6, nsx);
            pst.setString(7, hoatChat); pst.setDouble(8, vat); pst.setString(9, hamLuong);
            pst.setString(10, moTa); pst.setString(11, dvt);
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // 3. Cập nhật Sản Phẩm
    public boolean capNhatSanPhamNhanh(String id, String danhMuc, String dang, String ten, String vietTat, String nsx, String hoatChat, double vat, String hamLuong, String moTa, String dvt) {
        String sql = "UPDATE SanPham SET danhMuc=?, dang=?, ten=?, tenVietTat=?, nhaSanXuat=?, hoatChat=?, thueVAT=?, hamLuong=?, moTa=?, donViDoCoBan=? WHERE id=?";
        Connection con = ConnectDB.getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, danhMuc); pst.setString(2, dang); pst.setString(3, ten);
            pst.setString(4, vietTat); pst.setString(5, nsx); pst.setString(6, hoatChat);
            pst.setDouble(7, vat); pst.setString(8, hamLuong); pst.setString(9, moTa);
            pst.setString(10, dvt); pst.setString(11, id);
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // 4. Xóa Sản Phẩm
    public boolean xoaSanPham(String id) {
        String sql = "DELETE FROM SanPham WHERE id=?";
        Connection con = ConnectDB.getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, id);
            return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
}