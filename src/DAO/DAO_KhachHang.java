package DAO;

import ConnectDB.ConnectDB;
import Entity.KhachHang;
import java.time.LocalDateTime;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
public class DAO_KhachHang {

    public DAO_KhachHang() {
    }
    public KhachHang timKhachHangTheoSDT(String sdt) {
        KhachHang kh = null;
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // LƯU Ý: Đổi dòng dưới đây theo đúng class kết nối Database của bạn (ví dụ: ConnectDB.getInstance().getConnection())
            con = ConnectDB.getInstance().getConnection(); 
            
            String sql = "SELECT * FROM KhachHang WHERE sdt = ?";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, sdt);
            
            rs = stmt.executeQuery();

            if (rs.next()) {
                kh = new KhachHang();
                kh.setId(rs.getString("id"));
                kh.setSdt(rs.getString("sdt"));
                kh.setHoVaTen(rs.getString("hoVaTen"));
                
                // Ép kiểu từ SQL Timestamp sang java.time.LocalDateTime của Java
                Timestamp timestamp = rs.getTimestamp("ngayTao");
                if (timestamp != null) {
                    kh.setNgayTao(timestamp.toLocalDateTime());
                }
                
                kh.setDiemTichLuy(rs.getInt("diemTichLuy"));
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi tìm khách hàng theo SĐT: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Đóng kết nối để giải phóng bộ nhớ
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                // Không đóng connection nếu bạn dùng chung 1 connection xuyên suốt app
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return kh;
    }
    public KhachHang timKhachHangTheoMa(String id) {
        KhachHang kh = null;
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT * FROM KhachHang WHERE id = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                kh = new KhachHang();
                kh.setId(rs.getString("id"));
                kh.setHoVaTen(rs.getString("hoVaTen"));
                kh.setSdt(rs.getString("sdt"));
                kh.setDiemTichLuy(rs.getInt("diemTichLuy"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return kh;
    }
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
 // Thêm hàm này vào DAO_KhachHang.java
    public String phatSinhMaKHTiepTheo() {
        String maMoi = "KH1"; // Mặc định nếu CSDL chưa có khách hàng nào
        String sql = "SELECT id FROM KhachHang WHERE id LIKE 'KH%'";
        
        Connection con = ConnectDB.getInstance().getConnection();
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            int maxSo = 0;
            while (rs.next()) {
                String id = rs.getString("id"); // Lấy mã, ví dụ: "KH7"
                try {
                    // Cắt bỏ 2 ký tự đầu ("KH") và ép phần còn lại sang số nguyên
                    int so = Integer.parseInt(id.substring(2));
                    if (so > maxSo) {
                        maxSo = so; // Tìm số lớn nhất
                    }
                } catch (Exception ex) {
                    // Bỏ qua những mã cũ sai định dạng (VD: KH2024-0001)
                }
            }
            // Cộng thêm 1 vào số lớn nhất để ra mã mới
            maMoi = "KH" + (maxSo + 1);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maMoi;
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
    public List<Object[]> layDanhSachKhachHangChoBang() {
        List<Object[]> ds = new ArrayList<>();
        
        // Lệnh SQL: Lấy thông tin KH, đồng thời đếm số hóa đơn và tính tổng tiền đã mua
        String sql = "SELECT kh.id, kh.hoVaTen, kh.sdt, kh.diemTichLuy, kh.ngayTao, " +
                     "COUNT(DISTINCT hd.id) AS soDonHang, " +
                     "ISNULL(SUM(ct.soLuong * dv.gia), 0) AS tongChiTieu " +
                     "FROM KhachHang kh " +
                     "LEFT JOIN HoaDon hd ON kh.id = hd.khachHangId AND hd.loaiHD = 'BAN_HANG' " +
                     "LEFT JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId " +
                     "LEFT JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id " +
                     "GROUP BY kh.id, kh.hoVaTen, kh.sdt, kh.diemTichLuy, kh.ngayTao " +
                     "ORDER BY kh.ngayTao DESC";

        Connection con = ConnectDB.getInstance().getConnection();
        
        try (PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            DecimalFormat df = new DecimalFormat("#,###đ");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            while (rs.next()) {
                String id = rs.getString("id");
                String ten = rs.getString("hoVaTen") != null ? rs.getString("hoVaTen") : "Khách chưa có tên";
                String sdt = rs.getString("sdt") != null ? rs.getString("sdt") : "";
                
                String soDon = String.valueOf(rs.getInt("soDonHang"));
                String tongChiTieu = df.format(rs.getDouble("tongChiTieu"));
                String diem = String.valueOf(rs.getInt("diemTichLuy"));
                
                String ngayTao = "";
                if (rs.getTimestamp("ngayTao") != null) {
                    ngayTao = rs.getTimestamp("ngayTao").toLocalDateTime().format(dtf);
                }

                // Đưa vào mảng khớp với thứ tự 8 cột trên giao diện của bạn
                ds.add(new Object[]{id, ten, sdt, soDon, tongChiTieu, diem, ngayTao, ""});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
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