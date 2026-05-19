package DAO;

import ConnectDB.ConnectDB;
import Entity.ChiTietHoaDon;
import Entity.DonViDoLuong;
import Entity.HoaDon;
import Entity.SanPham;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DAO_ChiTietHoaDon {

    public DAO_ChiTietHoaDon() {}

    public boolean themChiTietDoiTra(String maHD, String tenSP, String dvt, int soLuong, double donGia, String ghiChu) {
        // Cú pháp 6 tham số để khớp với BUS và GUI của cậu
        String sql = "INSERT INTO ChiTietHoaDon (hoaDonId, sanPhamId, donViDoLuongId, soLuong, donGiaThucTe, thanhTien, ghiChu) " +
                     "SELECT TOP 1 ?, sp.id, dv.id, ?, ?, ?, ? " +
                     "FROM SanPham sp " +
                     "JOIN DonViDoLuong dv ON sp.id = dv.sanPhamId " +
                     "WHERE sp.ten LIKE ? AND dv.ten LIKE ?";
                     
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, maHD);
            pst.setInt(2, soLuong);
            pst.setDouble(3, donGia);             // Nhận giá tiền trực tiếp từ GUI
            pst.setDouble(4, soLuong * donGia);   // Tự nhân ra Thành tiền
            
            // Ép chết lỗi NULL: Nếu ghiChu rỗng thì để mặc định
            String note = (ghiChu == null || ghiChu.isEmpty()) ? "Hàng Đổi/Trả" : ghiChu;
            pst.setString(5, note);
            
            // Dùng LIKE để chốt hạ vụ sai font, dư dấu cách
            pst.setString(6, "%" + tenSP.trim() + "%");
            pst.setString(7, "%" + dvt.trim() + "%");
            
            return pst.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean xoaChiTietTheoMaHD(String maHD) {
        String sqlDel = "DELETE FROM ChiTietHoaDon WHERE hoaDonId = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pstDel = con.prepareStatement(sqlDel)) {
            
            pstDel.setString(1, maHD);
            return pstDel.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa ChiTietHoaDon: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public List<Object[]> layDanhSachSanPhamTheoMaHD(String maHD) {
        List<Object[]> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        
        // SQL MỚI: Tự động JOIN với PhanBoLoHang và LoHang để lấy Ngày Hết Hạn
        String sql = "SELECT sp.ten AS TenSP, dv.ten AS DVT, ct.soLuong AS SL, " +
                     "dv.gia AS DonGia, sp.thueVAT AS VAT, ct.ghiChu AS GhiChu, " +
                     "(SELECT TOP 1 lh.ngayHetHan FROM PhanBoLoHang pb " +
                     " JOIN LoHang lh ON pb.loHangId = lh.id " +
                     " WHERE pb.hoaDonId = ct.hoaDonId AND pb.sanPhamId = ct.sanPhamId AND pb.donViDoLuongId = ct.donViDoLuongId) AS HSD " +
                     "FROM ChiTietHoaDon ct " +
                     "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                     "LEFT JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id AND ct.sanPhamId = dv.sanPhamId " +
                     "WHERE ct.hoaDonId = ?";
                     
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setString(1, maHD);
            try (ResultSet rs = pstm.executeQuery()) {
                int stt = 1;
                while (rs.next()) {
                    String tenSP = rs.getString("TenSP");
                    String ghiChu = rs.getString("GhiChu");
                    java.sql.Date hsd = rs.getDate("HSD");

                    String extraInfo = "";

                    // 1. Xử lý ghi chú cắt liều
                    if (ghiChu != null && !ghiChu.trim().isEmpty() && !ghiChu.equals("Hàng Đổi/Trả") && !ghiChu.equals("TRA_LAI") && !ghiChu.equals("DOI_LAY")) {
                        extraInfo += ghiChu.replace("\n", "<br/>") + "<br/>";
                        if (!tenSP.toUpperCase().contains("CẮT LIỀU")) {
                            tenSP = "THUỐC CẮT LIỀU - " + tenSP;
                        }
                    }

                    // 2. Thêm Ngày Hết Hạn
                    if (hsd != null) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
                        extraInfo += "Ngày hết hạn: " + sdf.format(hsd);
                    }

                    // 3. Gộp thành chuỗi HTML để JTable tự xuống dòng
                    if (!extraInfo.isEmpty()) {
                        tenSP = "<html><b>" + tenSP + "</b><br/><span style='font-size:9.5px; color:#555555;'>" + extraInfo + "</span></html>";
                    }

                    String dvt = rs.getString("DVT") != null ? rs.getString("DVT") : "Hộp";
                    int sl = rs.getInt("SL");
                    double donGia = rs.getDouble("DonGia");
                    double vatPercent = rs.getDouble("VAT"); 
                    if (vatPercent > 0 && vatPercent < 1) vatPercent = vatPercent * 100;

                    double tienGoc = sl * donGia; 
                    String strDonGia = String.format("%,d", (long)donGia).replace(',', '.') + "đ";
                    String strThanhTienGoc = String.format("%,d", (long)tienGoc).replace(',', '.') + "đ";
                    String strVAT = (int)vatPercent + "%"; 

                    Object[] row = new Object[]{
                        String.valueOf(stt++), tenSP, dvt, String.valueOf(sl), strDonGia, strVAT, strThanhTienGoc  
                    };
                    list.add(row);
                }
            }
        } catch (Exception e) { e.printStackTrace(); } 
        return list;
    }

 // HÀM 1:
    public boolean themCTHD(ChiTietHoaDon cthd) {
        if (cthd == null || cthd.getHoaDonId() == null || cthd.getDonViDoLuongId() == null || cthd.getSanPhamId() == null) {
            return false;
        }
        // ĐÃ FIX: Thêm donGiaThucTe và thanhTien
        String sql = "INSERT INTO ChiTietHoaDon (hoaDonId, donViDoLuongId, sanPhamId, soLuong, donGiaThucTe, thanhTien) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, cthd.getHoaDonId().getId());
            pst.setString(2, cthd.getDonViDoLuongId().getId());
            pst.setString(3, cthd.getSanPhamId().getId());
            pst.setInt(4, cthd.getSoLuong());
            pst.setDouble(5, cthd.getDonGiaThucTe()); // Truyền giá gốc
            pst.setDouble(6, cthd.getThanhTien());    // Truyền giá thực tế đã trừ KM
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // HÀM 2: Dùng MERGE (upsert) để tránh lỗi Duplicate PK nếu bản ghi cũ còn sót lại
    public boolean themCTHD(Connection con, ChiTietHoaDon cthd) throws SQLException {
        if (cthd == null || cthd.getHoaDonId() == null || cthd.getDonViDoLuongId() == null || cthd.getSanPhamId() == null) {
            return false;
        }
        // Dùng MERGE thay cho INSERT để xử lý an toàn trường hợp bản ghi đã tồn tại
     // Trong file DAO_ChiTietHoaDon.java, hàm themCTHD
        String sql = "MERGE INTO ChiTietHoaDon AS Target " +
                     "USING (SELECT ? AS hoaDonId, ? AS donViDoLuongId, ? AS sanPhamId, ? AS soLuong, ? AS donGiaThucTe, ? AS thanhTien) AS Source " +
                     "ON Target.hoaDonId = Source.hoaDonId " +
                     "   AND Target.donViDoLuongId = Source.donViDoLuongId " +
                     "   AND Target.sanPhamId = Source.sanPhamId " +
                     "WHEN MATCHED THEN " +
                     "    UPDATE SET Target.soLuong = Target.soLuong + Source.soLuong, " + // FIX: Thêm Target. và Source.
                     "               Target.thanhTien = Target.thanhTien + Source.thanhTien " +
                     "WHEN NOT MATCHED THEN " +
                     "    INSERT (hoaDonId, donViDoLuongId, sanPhamId, soLuong, donGiaThucTe, thanhTien) " +
                     "    VALUES (Source.hoaDonId, Source.donViDoLuongId, Source.sanPhamId, Source.soLuong, Source.donGiaThucTe, Source.thanhTien);";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, cthd.getHoaDonId().getId());
            pst.setString(2, cthd.getDonViDoLuongId().getId());
            pst.setString(3, cthd.getSanPhamId().getId());
            pst.setInt(4, cthd.getSoLuong());
            pst.setDouble(5, cthd.getDonGiaThucTe());
            pst.setDouble(6, cthd.getThanhTien());
            return pst.executeUpdate() >= 0;
        }
    }


    public List<Object[]> layDuLieuChoTaoHoaDon(String maHD) {
        List<Object[]> list = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection(); 
        
        // ĐÃ FIX: JOIN trực tiếp từ ct.donViDoLuongId sang dv.id
        String sql = "SELECT sp.ten AS TenSP, dv.ten AS DVT, ct.soLuong AS SL, dv.gia AS DonGia " +
                     "FROM ChiTietHoaDon ct " +
                     "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                     "LEFT JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id " +
                     "WHERE ct.hoaDonId = ?";

        if (maHD != null && maHD.startsWith("DTH")) {
            sql += " AND ct.ghiChu = 'TRA_LAI'";
        }
                     
        try (PreparedStatement pstm = con.prepareStatement(sql)) {
            pstm.setString(1, maHD);
            
            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    String tenSP = rs.getString("TenSP");
                    String dvt = rs.getString("DVT") != null ? rs.getString("DVT") : "Hộp";
                    int sl = rs.getInt("SL");
                    double donGia = rs.getDouble("DonGia");
                    
                    double thanhTien = sl * donGia; 

                    String strDonGia = String.format("%,d", (long)donGia).replace(',', '.') + "đ";
                    String strThanhTien = String.format("%,d", (long)thanhTien).replace(',', '.') + "đ";

                    Object[] row = new Object[]{
                        tenSP, dvt, String.valueOf(sl), strDonGia, "0", strThanhTien, ""   
                    };
                    list.add(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ChiTietHoaDon> layDSChiTietHD(String maHoaDon) {
        List<ChiTietHoaDon> dsCTHD = new ArrayList<>();
        String sql = "SELECT * FROM ChiTietHoaDon WHERE hoaDonId = ?";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maHoaDon);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ChiTietHoaDon ct = new ChiTietHoaDon();

                    HoaDon hd = new HoaDon();
                    hd.setId(rs.getString("hoaDonId"));
                    ct.setHoaDonId(hd);

                    DonViDoLuong dvdl = new DonViDoLuong();
                    dvdl.setId(rs.getString("donViDoLuongId"));
                    ct.setDonViDoLuongId(dvdl);

                    SanPham sp = new SanPham();
                    sp.setId(rs.getString("sanPhamId"));
                    ct.setSanPhamId(sp);

                    ct.setSoLuong(rs.getInt("soLuong"));
                    dsCTHD.add(ct);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsCTHD;
    }
}