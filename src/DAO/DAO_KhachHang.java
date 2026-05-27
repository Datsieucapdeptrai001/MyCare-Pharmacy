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
 // Thay thế toàn bộ nội dung của hàm phatSinhMaKHTiepTheo bằng code này
    public String phatSinhMaKHTiepTheo() {
        String maMoi = "KH-0001"; 
        String sql = "SELECT id FROM KhachHang WHERE id LIKE 'KH%'";
        
        Connection con = ConnectDB.getInstance().getConnection();
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            int maxSo = 0;
            while (rs.next()) {
                String id = rs.getString("id"); 
                try {
                    // Dùng Regex xóa tất cả ký tự không phải là số (xóa cả chữ KH và dấu -)
                    // VD: "KH-0010" -> "0010", "KH5" -> "5"
                    String numberOnly = id.replaceAll("[^0-9]", "");
                    
                    if (!numberOnly.isEmpty()) {
                        int so = Integer.parseInt(numberOnly); // "0010" sẽ thành 10
                        if (so > maxSo) {
                            maxSo = so; // Tìm được số lớn nhất thực sự
                        }
                    }
                } catch (Exception ex) {
                    // Bỏ qua nếu có mã rác không thể ép kiểu
                }
            }
            
            // Cộng thêm 1 vào số lớn nhất để ra mã mới, format chuẩn 4 số
            maMoi = String.format("KH-%04d", maxSo + 1);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maMoi;
    }

    // Thay thế hàm getMaKHTuDong để trỏ về chung 1 logic, tránh xung đột
    public String getMaKHTuDong() {
        return phatSinhMaKHTiepTheo();
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
        
        // [FIX]: Thay SUM(ct.soLuong * dv.gia) thành SUM(ct.thanhTien). 
        // Vì ct.thanhTien trong CSDL hiện tại đã được tính gộp cả Thuế VAT và trừ Khuyến mãi.
        // Đồng thời có thể bỏ LEFT JOIN với DonViDoLuong cho nhẹ truy vấn.
        String sql = "SELECT kh.id, kh.hoVaTen, kh.sdt, kh.diemTichLuy, kh.ngayTao, " +
                     "COUNT(DISTINCT hd.id) AS soDonHang, " +
                     "ISNULL(SUM(ct.thanhTien), 0) AS tongChiTieu " +
                     "FROM KhachHang kh " +
                     "LEFT JOIN HoaDon hd ON kh.id = hd.khachHangId AND hd.loaiHD = 'BAN_HANG' " +
                     "LEFT JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId " +
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
                
                // Tiền chi tiêu bây giờ đã tính cả VAT 100% chính xác
                String tongChiTieu = df.format(rs.getDouble("tongChiTieu")); 
                String diem = String.valueOf(rs.getInt("diemTichLuy"));
                
                String ngayTao = "";
                if (rs.getTimestamp("ngayTao") != null) {
                    ngayTao = rs.getTimestamp("ngayTao").toLocalDateTime().format(dtf);
                }

                // Đưa vào mảng khớp với thứ tự cột trên giao diện
                ds.add(new Object[]{id, ten, sdt, soDon, tongChiTieu, diem, ngayTao, ""});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }
    public List<String[]> getLichSuDiem(String khachHangId) {
        List<String[]> dsLichSu = new ArrayList<>();
        // Truy vấn sắp xếp từ mới nhất đến cũ nhất
        String sql = "SELECT hoaDonId, loai, soDiem, ghiChu, thoiGian FROM LichSuDiem WHERE khachHangId = ? ORDER BY thoiGian DESC";
        
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, khachHangId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                String hoaDonId = rs.getString("hoaDonId");
                if (hoaDonId == null || hoaDonId.trim().isEmpty()) hoaDonId = "Hệ thống"; 
                
                String loai = rs.getString("loai");
                String soDiem = String.valueOf(rs.getInt("soDiem"));
                
                String ghiChu = rs.getString("ghiChu");
                if (ghiChu == null) ghiChu = "";
                
                java.sql.Timestamp ts = rs.getTimestamp("thoiGian");
                String thoiGian = "";
                if (ts != null) {
                    thoiGian = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(ts);
                }
                
                // Mảng 5 phần tử khớp chính xác với những gì ManHinhKhachHang đang đợi
                dsLichSu.add(new String[]{hoaDonId, loai, soDiem, ghiChu, thoiGian});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsLichSu;
    }

 // Ghi một dòng lịch sử điểm
 public boolean ghiLichSuDiem(String khachHangId, String hoaDonId, String loai, int soDiem, String ghiChu) {
     String sql = "INSERT INTO LichSuDiem (id, khachHangId, hoaDonId, loai, soDiem, ghiChu, thoiGian) VALUES (?,?,?,?,?,?,?)";
     try {
         Connection con = ConnectDB.getInstance().getConnection();
         PreparedStatement pst = con.prepareStatement(sql);
         pst.setString(1, "LS-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
         pst.setString(2, khachHangId);
         pst.setString(3, hoaDonId);
         pst.setString(4, loai);
         pst.setInt(5, soDiem);
         pst.setString(6, ghiChu);
         pst.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
         return pst.executeUpdate() > 0;
     } catch (SQLException e) { e.printStackTrace(); }
     return false;
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