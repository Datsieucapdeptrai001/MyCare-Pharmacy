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
        String sql = "SELECT id, ten, danhMuc, ISNULL(hoatChat, '') AS hoatChat, dang, " +
                "ISNULL(nhaSanXuat, 'Khác') AS nhaSanXuat, ISNULL(thueVAT, 0) AS thueVAT, ISNULL(giaBan, 0) AS giaBan " +
                "FROM SanPham " +
                "WHERE ISNULL(trangThai, 'HOAT_DONG') != 'AN'";
                
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                String ma       = rs.getString("id");
                String ten      = rs.getString("ten");
                String danhMucDB = rs.getString("danhMuc");
                String loai = "Sản phẩm chức năng";
                if ("THUOC_KE_DON".equals(danhMucDB))      loai = "Thuốc kê đơn";
                else if ("THUOC_KHONG_KE_DON".equals(danhMucDB)) loai = "Thuốc không kê đơn";
                else if ("MY_PHAM".equals(danhMucDB))      loai = "Mỹ phẩm";
                
                String hoatChat = rs.getString("hoatChat");
                String dangDB   = rs.getString("dang");
                String dang = "Viên nén";
                if (dangDB != null) {
                    switch (dangDB) {
                        case "VIEN_NANG":      dang = "Viên nang";       break;
                        case "VIEN_SUI":       dang = "Viên sủi";        break;
                        case "THUOC_BOT":      dang = "Thuốc bột";       break;
                        case "KEO_NGAM":       dang = "Kẹo ngậm";        break;
                        case "DUNG_DICH":      dang = "Dung dịch";       break;
                        case "HON_DICH":       dang = "Hỗn dịch";        break;
                        case "THUOC_NHO_GIOT": dang = "Thuốc nhỏ giọt"; break;
                        case "SUC_MIENG":      dang = "Súc miệng";       break;
                        default:               dang = "Viên nén";        break;
                    }
                }
                String nsx = rs.getString("nhaSanXuat");
                String vat = rs.getDouble("thueVAT") + "%";
                String giaBan = String.format("%,.0f", rs.getDouble("giaBan"));
                
                ds.add(new Object[]{ma, ten, loai, hoatChat, dang, nsx, vat, giaBan});
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ds;
    }

    public List<SanPham> getDsThuoc() {
        List<SanPham> dsSanPham = new ArrayList<>();
        String sql = "SELECT * FROM SanPham WHERE ISNULL(trangThai, 'HOAT_DONG') != 'AN'";
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) dsSanPham.add(mapSanPham(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return dsSanPham;
    }

    public SanPham getSanPhamTheoMa(String id) {
        SanPham sp = null;
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement("SELECT * FROM SanPham WHERE id = ?")) {
            pst.setString(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) sp = mapSanPham(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return sp;
    }

    public SanPham getSanPhamDayDu(String id) {
        return getSanPhamTheoMa(id);
    }

    public List<LoHang> layLoTheoSP(String maSP) {
        List<LoHang> dsLoHang = new ArrayList<>();
        String sql = "SELECT * FROM LoHang WHERE sanPhamId = ? AND soLuongLoHang > 0 " +
                     "AND trangThai NOT IN ('HET_HAN', 'AN') ORDER BY ngayHetHan ASC";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maSP);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    LoHang lh = new LoHang();
                    lh.setId(rs.getString("id"));
                    lh.setSoLoHang(rs.getString("soLoHang"));
                    lh.setSoLuongLoHang(rs.getInt("soLuongLoHang"));
                    lh.setGia(rs.getInt("gia"));
                    if (rs.getString("trangThai") != null)
                        lh.setTrangThai(TrangThaiLoHang.valueOf(rs.getString("trangThai")));
                    if (rs.getTimestamp("ngayHetHan") != null)
                        lh.setNgayHetHan(rs.getTimestamp("ngayHetHan").toLocalDateTime());
                    if (rs.getTimestamp("ngayNhap") != null)
                        lh.setNgayNhap(rs.getTimestamp("ngayNhap").toLocalDateTime());
                    SanPham sp = new SanPham(); sp.setId(rs.getString("sanPhamId"));
                    lh.setSanPhamId(sp);
                    dsLoHang.add(lh);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return dsLoHang;
    }

    /**
     * Lấy danh sách đơn vị quy đổi từ bảng DonViDoLuong.
     * Tên cột theo schema thực tế: ten, chuyenDoiDonViCoBan.
     * Trả về List<Object[]> = {tên đơn vị, tỉ lệ quy đổi (String)}.
     */
    public List<Object[]> layDonViDoLuongTheoSP(String maSP) {
        List<Object[]> ds = new ArrayList<>();
        // Dùng đúng tên cột từ schema: ten, chuyenDoiDonViCoBan
        String sql = "SELECT ten, chuyenDoiDonViCoBan FROM DonViDoLuong " +
                     "WHERE sanPhamId = ? ORDER BY chuyenDoiDonViCoBan ASC";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maSP);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ds.add(new Object[]{
                        rs.getString("ten"),
                        rs.getBigDecimal("chuyenDoiDonViCoBan").toPlainString()
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("layDonViDoLuongTheoSP lỗi: " + e.getMessage());
        }
        return ds;
    }

    public List<Object[]> timKiemSanPhamBan(String text) {
        List<Object[]> ds = new ArrayList<>();
        
        // ĐÃ FIX: Tách riêng từng lô hàng (Không dùng GROUP BY) và sắp xếp HSD tăng dần
        String sql = "SELECT sp.id, sp.ten, sp.donViDoCoBan, sp.giaBan, " +
                     "lh.soLuongLoHang AS soLuongTon, sp.danhMuc, ISNULL(sp.thueVAT, 0) AS thueVAT, " +
                     "lh.soLoHang, lh.ngayHetHan " +
                     "FROM SanPham sp JOIN LoHang lh ON sp.id = lh.sanPhamId " +
                     "WHERE (sp.ten LIKE ? OR sp.tenVietTat LIKE ? OR sp.hoatChat LIKE ? " +
                     "       OR sp.id LIKE ?) " +
                     "AND ISNULL(lh.trangThai, '') != 'HET_HAN' " +
                     "AND lh.soLuongLoHang > 0 " +
                     "ORDER BY lh.ngayHetHan ASC";
                     
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
             
            String p = "%" + text + "%";
            pst.setString(1, p); pst.setString(2, p);
            pst.setString(3, p); pst.setString(4, p); 
            
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String danhMucDB = rs.getString("danhMuc");
                    String loai = "Khác";
                    if (danhMucDB != null) {
                        if (danhMucDB.equals("THUOC_KE_DON"))           loai = "Thuốc kê đơn";
                        else if (danhMucDB.equals("THUOC_KHONG_KE_DON")) loai = "Thuốc không kê đơn";
                        else if (danhMucDB.equals("THUC_PHAM_CHUC_NANG")) loai = "Thực phẩm chức năng";
                        else if (danhMucDB.equals("MY_PHAM"))            loai = "Mỹ phẩm";
                    }
                    
                    // Lấy Lô và định dạng lại Ngày hết hạn
                    String loHang = rs.getString("soLoHang");
                    java.sql.Date dateHSD = rs.getDate("ngayHetHan");
                    String hsdStr = "";
                    if (dateHSD != null) {
                        hsdStr = new java.text.SimpleDateFormat("dd/MM/yyyy").format(dateHSD);
                    }
                    
                    ds.add(new Object[]{
                        rs.getString("id"), rs.getString("ten"),
                        rs.getString("donViDoCoBan"), rs.getDouble("giaBan"),
                        rs.getInt("soLuongTon"), loai, 
                        rs.getDouble("thueVAT"), loHang, hsdStr
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ds;
    }

    public boolean capNhatSoLuongTon(String maLoHang, int soLuongMoi) {
        if (soLuongMoi < 0) return false;
        String sql = "UPDATE LoHang SET soLuongLoHang = ? WHERE id = ? AND ISNULL(trangThai, 'CON_HANG') <> 'AN'";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, soLuongMoi); pst.setString(2, maLoHang);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public String layMaSanPhamMoiNhat() {
        // Dùng CAST để sắp xếp đúng theo số, tránh lỗi lexicographic khi vượt 9999 SP
        String sql = "SELECT TOP 1 id FROM SanPham " +
                     "ORDER BY CAST(SUBSTRING(id, CHARINDEX('-', id) + 1, LEN(id)) AS INT) DESC";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                String lastId = rs.getString("id");
                int number = Integer.parseInt(lastId.replaceAll("[^0-9]", ""));
                return String.format("SP2024-%04d", number + 1);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "SP2024-0001";
    }

    public boolean anSanPham(String id) {
        String sql = "UPDATE SanPham SET trangThai = 'AN' WHERE id = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, id); return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean themSanPhamNhanh(String id, String danhMuc, String dang, String ten,
						            String vietTat, String nsx, String hoatChat, double vat,
						            String hamLuong, String moTa, String dvt, double giaBan) {
	    	String sql = "INSERT INTO SanPham (id, danhMuc, dang, ten, tenVietTat, nhaSanXuat, hoatChat, " +
						"thueVAT, hamLuong, moTa, donViDoCoBan, giaBan, ngayTao) " +
						"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,GETDATE())";
		try (Connection con = ConnectDB.getInstance().getConnection();
		     PreparedStatement pst = con.prepareStatement(sql)) {
			pst.setString(1, id);     pst.setString(2, danhMuc);
			pst.setString(3, dang);   pst.setString(4, ten);
			pst.setString(5, vietTat); pst.setString(6, nsx);
			pst.setString(7, hoatChat); pst.setDouble(8, vat);
			pst.setString(9, hamLuong); pst.setString(10, moTa);
			pst.setString(11, dvt);   pst.setDouble(12, giaBan);
			return pst.executeUpdate() > 0;
		} catch (Exception e) { e.printStackTrace(); }
		return false;
	}

    public boolean capNhatSanPhamNhanh(String id, String danhMuc, String dang, String ten,
							            String vietTat, String nsx, String hoatChat, double vat,
							            String hamLuong, String moTa, String dvt, double giaBan) {
		String sql = "UPDATE SanPham SET danhMuc=?,dang=?,ten=?,tenVietTat=?,nhaSanXuat=?," +
					"hoatChat=?,thueVAT=?,hamLuong=?,moTa=?,donViDoCoBan=?,giaBan=? " +
					"WHERE id=?";
		try (Connection con = ConnectDB.getInstance().getConnection();
			     PreparedStatement pst = con.prepareStatement(sql)) {
			pst.setString(1, danhMuc);  pst.setString(2, dang);
			pst.setString(3, ten);      pst.setString(4, vietTat);
			pst.setString(5, nsx);      pst.setString(6, hoatChat);
			pst.setDouble(7, vat);      pst.setString(8, hamLuong);
			pst.setString(9, moTa);     pst.setString(10, dvt);
			pst.setDouble(11, giaBan);  pst.setString(12, id);
			return pst.executeUpdate() > 0;
		} catch (Exception e) { e.printStackTrace(); }
		return false;
    }

    public List<SanPham> timKiemSanPhamDoiTra(String tuKhoa) {
        List<SanPham> ds = new ArrayList<>();
        // Thêm điều kiện loại bỏ sản phẩm đã ẩn, tránh hiện trong phiếu đổi trả
        String sql = "SELECT * FROM SanPham WHERE (ten LIKE ? OR id LIKE ?) " +
                     "AND ISNULL(trangThai, 'HOAT_DONG') != 'AN'";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            String p = "%" + tuKhoa + "%";
            pst.setString(1, p); pst.setString(2, p);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    SanPham sp = new SanPham();
                    sp.setId(rs.getString("id")); sp.setTen(rs.getString("ten"));
                    String danhMucStr = rs.getString("danhMuc");
                    if (danhMucStr != null && !danhMucStr.isEmpty())
                        sp.setDanhMuc(DanhMucSanPham.valueOf(danhMucStr));
                    ds.add(sp);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ds;
    }

    public boolean xoaSanPham(String id) {
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement("DELETE FROM SanPham WHERE id=?")) {
            pst.setString(1, id); return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public int getSoLuongTon(String maSP) {
        String sql = "SELECT ISNULL(SUM(lh.soLuongLoHang),0) AS tonKho FROM LoHang lh " +
                     "WHERE lh.sanPhamId=? AND lh.soLuongLoHang>0 AND lh.ngayHetHan>=GETDATE()";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maSP);
            try (ResultSet rs = pst.executeQuery()) { if (rs.next()) return rs.getInt("tonKho"); }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public List<Object[]> layDanhSachSanPhamDaAn() {
        List<Object[]> ds = new ArrayList<>();
        String sql = "SELECT id,ten,danhMuc,hoatChat,dang,nhaSanXuat,thueVAT,ISNULL(giaBan,0) AS giaBan FROM SanPham WHERE trangThai='AN'";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                ds.add(new Object[]{ rs.getString("id"), rs.getString("ten"),
                    rs.getString("danhMuc"), rs.getString("hoatChat"),
                    rs.getString("dang"), rs.getString("nhaSanXuat"),
                    rs.getDouble("thueVAT") + "%",
                    String.format("%,.0f", rs.getDouble("giaBan")) });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ds;
    }

    public boolean khoiPhucSanPham(String id) {
        String sql = "UPDATE SanPham SET trangThai='HOAT_DONG' WHERE id=?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, id); return pst.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public List<String> layDanhSachTenSanPham() {
        List<String> ds = new ArrayList<>();
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ten FROM SanPham")) {
            while (rs.next()) ds.add(rs.getString("ten"));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    // ── Private helper ─────────────────────────────────────────────────────
    private SanPham mapSanPham(ResultSet rs) throws SQLException {
        SanPham sp = new SanPham();
        sp.setId(rs.getString("id"));
        if (rs.getString("danhMuc") != null) sp.setDanhMuc(DanhMucSanPham.valueOf(rs.getString("danhMuc")));
        try {
            if (rs.getString("dang") != null) sp.setDang(DangBaoChe.valueOf(rs.getString("dang")));
        } catch (IllegalArgumentException e) { System.out.println("DangBaoChe lạ: " + rs.getString("dang")); }
        sp.setTen(rs.getString("ten"));
        sp.setTenVietTat(rs.getString("tenVietTat"));
        sp.setNhaSanXuat(rs.getString("nhaSanXuat"));
        sp.setHoatChat(rs.getString("hoatChat"));
        sp.setThueVAT(rs.getDouble("thueVAT"));
        sp.setHamLuong(rs.getString("hamLuong"));
        sp.setMoTa(rs.getString("moTa"));
        sp.setDonViDoCoBan(rs.getString("donViDoCoBan"));
        // Dùng try/catch phòng trường hợp cột chưa tồn tại (trước khi chạy patch SQL)
        try { sp.setGiaBan(rs.getDouble("giaBan")); }  catch (Exception ignored) {}
        if (rs.getTimestamp("ngayTao") != null)
            sp.setNgayTao(rs.getTimestamp("ngayTao").toLocalDateTime());
        return sp;
    }
 // HÀM LƯU BẢNG ĐƠN VỊ QUY ĐỔI XUỐNG DB (XỬ LÝ ĐƯỢC CẢ VIÊN/HỘP/VỈ)
    public void luuDonViQuyDoi(String maSP, String donViCoBan, double giaBanCoBan, List<Object[]> dsDonVi) {
        if (dsDonVi == null || dsDonVi.isEmpty()) return;
        try (Connection con = ConnectDB.getInstance().getConnection()) {
            if (con == null) return;
            for (Object[] dv : dsDonVi) {
                String tenDVT = dv[0].toString();
                double tiLe = (Double) dv[1];
                double giaGiaoDien = (Double) dv[2];
                upsertDonVi(con, maSP, tenDVT, tiLe, giaGiaoDien);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void upsertDonVi(Connection con, String maSP, String tenDVT, double tiLe, double gia) throws SQLException {
        String donViId = null;
        String sqlCheck = "SELECT id FROM DonViDoLuong WHERE sanPhamId = ? AND ten = ?";
        try (PreparedStatement pst = con.prepareStatement(sqlCheck)) {
            pst.setString(1, maSP); pst.setString(2, tenDVT);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) donViId = rs.getString("id");
        }

        if (donViId != null) {
            String sqlUpd = "UPDATE DonViDoLuong SET chuyenDoiDonViCoBan = ?, gia = ? WHERE id = ?";
            try (PreparedStatement pst = con.prepareStatement(sqlUpd)) {
                pst.setDouble(1, tiLe); pst.setDouble(2, gia); pst.setString(3, donViId);
                pst.executeUpdate();
            }
        } else {
            String sqlIns = "INSERT INTO DonViDoLuong (id, sanPhamId, ten, chuyenDoiDonViCoBan, gia) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pst = con.prepareStatement(sqlIns)) {
                String newId = "DVL-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
                pst.setString(1, newId); pst.setString(2, maSP);
                pst.setString(3, tenDVT); pst.setDouble(4, tiLe); pst.setDouble(5, gia);
                pst.executeUpdate();
            }
        }
    }

 // HÀM LẤY ĐƠN VỊ QUY ĐỔI TỪ DB LÊN ĐỂ ĐỔ VÀO BẢNG GIAO DIỆN
    public List<Object[]> layDonViQuyDoiTheoSP(String maSP) { // Xóa tham số donViCoBan
        List<Object[]> ds = new ArrayList<>();
        String sql = "SELECT ten, chuyenDoiDonViCoBan, gia FROM DonViDoLuong WHERE sanPhamId = ? ORDER BY chuyenDoiDonViCoBan ASC";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maSP);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    String ten = rs.getString("ten");
                    double tiLe = rs.getDouble("chuyenDoiDonViCoBan");
                    String gia = String.format("%,.0f", rs.getDouble("gia"));
                    ds.add(new Object[]{ten, tiLe, gia});
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return ds;
    }
 // Lấy danh sách các Nhà sản xuất hiện có trong DB (không trùng lặp)
    public List<String> layDanhSachNhaSanXuat() {
        List<String> ds = new ArrayList<>();
        String sql = "SELECT DISTINCT nhaSanXuat FROM SanPham WHERE nhaSanXuat IS NOT NULL AND nhaSanXuat != ''";
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) ds.add(rs.getString(1));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    // Lấy danh sách các Đơn vị tính hiện có trong bảng DonViDoLuong (không trùng lặp)
    public List<String> layDanhSachDonViTinh() {
        List<String> ds = new ArrayList<>();
        String sql = "SELECT DISTINCT ten FROM DonViDoLuong WHERE ten IS NOT NULL AND ten != ''";
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) ds.add(rs.getString(1));
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public boolean laSanPhamDaAn(String maSP) {
        String sql = "SELECT COUNT(*) FROM SanPham WHERE id = ? AND trangThai = 'AN'";
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maSP);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}