package DAO;

import ConnectDB.ConnectDB;
import Entity.KhuyenMai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DAO_KhuyenMai {

    public DAO_KhuyenMai() {
        try { 
            ConnectDB.getInstance().connect(); 
        } catch (Exception e) { 
            System.err.println("Lỗi khởi tạo kết nối DB tại DAO_KhuyenMai: " + e.getMessage()); 
        }
    }

    public List<String> layDanhSachMaKMCoHieuLuc() {
        List<String> dsMa = new ArrayList<>();
        String sql = "SELECT id FROM KhuyenMai WHERE ngayBatDau <= ? AND ngayKetThuc >= ? AND trangThai = 1";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            Timestamp bayGio = Timestamp.valueOf(LocalDateTime.now());
            pst.setTimestamp(1, bayGio);
            pst.setTimestamp(2, bayGio);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    dsMa.add(rs.getString("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsMa;
    }

    public boolean themKhuyenMai(KhuyenMai km) {
        String sql = "INSERT INTO KhuyenMai (id, tenKhuyenMai, moTa, ngayTao, ngayBatDau, ngayKetThuc) VALUES (?, ?, ?, ?, ?, ?)";
        int n = 0; 
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, km.getId()); 
            pst.setString(2, km.getTenKhuyenMai()); 
            pst.setString(3, km.getMoTa());
            LocalDateTime ngayTao = km.getNgayTao() != null ? km.getNgayTao() : LocalDateTime.now();
            pst.setTimestamp(4, Timestamp.valueOf(ngayTao)); 
            pst.setTimestamp(5, Timestamp.valueOf(km.getNgayBatDau()));
            pst.setTimestamp(6, Timestamp.valueOf(km.getNgayKetThuc()));
            n = pst.executeUpdate();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return n > 0;
    }

    public boolean capNhatKhuyenMai(KhuyenMai km) {
        String sql = "UPDATE KhuyenMai SET tenKhuyenMai = ?, moTa = ?, ngayBatDau = ?, ngayKetThuc = ? WHERE id = ?";
        int n = 0; 
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, km.getTenKhuyenMai()); 
            pst.setString(2, km.getMoTa());
            pst.setTimestamp(3, Timestamp.valueOf(km.getNgayBatDau())); 
            pst.setTimestamp(4, Timestamp.valueOf(km.getNgayKetThuc()));
            pst.setString(5, km.getId());
            n = pst.executeUpdate();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return n > 0;
    }

    public boolean xoaKhuyenMai(String maKM) {
        String sql = "DELETE FROM KhuyenMai WHERE id = ?";
        int n = 0; 
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maKM); 
            n = pst.executeUpdate();
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return n > 0;
    }

    public List<KhuyenMai> layDsKhuyenMai() {
        List<KhuyenMai> dsKhuyenMai = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMai"; 
        Connection con = ConnectDB.getInstance().getConnection();
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                KhuyenMai km = new KhuyenMai();
                km.setId(rs.getString("id")); 
                km.setTenKhuyenMai(rs.getString("tenKhuyenMai")); 
                km.setMoTa(rs.getString("moTa"));
                if (rs.getTimestamp("ngayTao") != null) km.setNgayTao(rs.getTimestamp("ngayTao").toLocalDateTime());
                if (rs.getTimestamp("ngayBatDau") != null) km.setNgayBatDau(rs.getTimestamp("ngayBatDau").toLocalDateTime());
                if (rs.getTimestamp("ngayKetThuc") != null) km.setNgayKetThuc(rs.getTimestamp("ngayKetThuc").toLocalDateTime());
                dsKhuyenMai.add(km);
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return dsKhuyenMai;
    }

    public KhuyenMai layMaKM(String maKM) {
        KhuyenMai km = null; 
        String sql = "SELECT * FROM KhuyenMai WHERE id = ?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maKM);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    km = new KhuyenMai(); 
                    km.setId(rs.getString("id")); 
                    km.setTenKhuyenMai(rs.getString("tenKhuyenMai")); 
                    km.setMoTa(rs.getString("moTa"));
                    if (rs.getTimestamp("ngayTao") != null) km.setNgayTao(rs.getTimestamp("ngayTao").toLocalDateTime());
                    if (rs.getTimestamp("ngayBatDau") != null) km.setNgayBatDau(rs.getTimestamp("ngayBatDau").toLocalDateTime());
                    if (rs.getTimestamp("ngayKetThuc") != null) km.setNgayKetThuc(rs.getTimestamp("ngayKetThuc").toLocalDateTime());
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return km;
    }

    public List<Object[]> layDanhSachKhuyenMaiChoTable() {
        List<Object[]> listData = new ArrayList<>();
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return listData;
        
        String sql = "SELECT k.id, k.tenKhuyenMai, k.ngayBatDau, k.ngayKetThuc, k.trangThai, " +
                     "h.loaiHinhThuc, h.giaTri as mucGiam, h.giamToiDa, h.spTang, h.slTang, h.dvdlTang, h.spYeuCau, h.slYeuCau, h.dvdlYeuCau, " + 
                     "d.giaTri as donToiThieu, h.doiTuongApDung " +
                     "FROM KhuyenMai k " +
                     "LEFT JOIN HinhThucKhuyenMai h ON k.id = h.khuyenMaiId " +
                     "LEFT JOIN DieuKienKhuyenMai d ON k.id = d.khuyenMaiId";
        
        try (PreparedStatement stmt = con.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            java.util.Date currentDate = new java.util.Date();

            while (rs.next()) {
                String id = rs.getString("id"); 
                String ten = rs.getString("tenKhuyenMai");
                Timestamp startDB = rs.getTimestamp("ngayBatDau"); 
                Timestamp endDB = rs.getTimestamp("ngayKetThuc");
                String hinhThucDB = rs.getString("loaiHinhThuc");
                
                boolean trangThaiDB = rs.getBoolean("trangThai");

                double mucGiamDB = rs.getDouble("mucGiam");
                double giamToiDaDB = rs.getDouble("giamToiDa"); 
                double donToiThieuDB = rs.getObject("donToiThieu") != null ? rs.getDouble("donToiThieu") : 0;

                // ĐÃ FIX: Lấy tên Sản phẩm để hiển thị thay vì chữ "Sản phẩm" vô hồn
                String spYeuCau = rs.getString("spYeuCau");
                String spHienThi = (spYeuCau != null && !spYeuCau.trim().isEmpty()) ? spYeuCau : "Tất cả SP";

                String hinhThucUI = "Giảm phần trăm (%)";
                String mucGiamUI = "";
                String donToiThieuUI = "";
                String doiTuongUI = "";
                
                if ("SAN_PHAM_KEM_THEO".equals(hinhThucDB)) {
                    hinhThucUI = "Sản phẩm kèm theo";
                    String spTang = rs.getString("spTang"); 
                    int slTang = rs.getInt("slTang");
                    String dvdlTang = rs.getString("dvdlTang");
                    String unitStr = (dvdlTang != null && !dvdlTang.isEmpty()) ? dvdlTang : "SP";
                    mucGiamUI = "Tặng " + slTang + " " + unitStr + " " + (spTang != null ? spTang : "");
                    
                    String dvM = rs.getString("dvdlYeuCau");
                    donToiThieuUI = "Mua " + rs.getInt("slYeuCau") + " " + (dvM != null && !dvM.isEmpty() ? dvM : "SP");
                    doiTuongUI = spHienThi; // Hiển thị Đích danh tên Sản phẩm
                } else if ("GIAM_TIEN_MAT".equals(hinhThucDB)) {
                    hinhThucUI = "Giảm tiền mặt";
                    mucGiamUI = String.format("-%,.0f đ", mucGiamDB);
                    
                    String dvM = rs.getString("dvdlYeuCau");
                    donToiThieuUI = "Mua " + rs.getInt("slYeuCau") + " " + (dvM != null && !dvM.isEmpty() ? dvM : "SP");
                    doiTuongUI = spHienThi; // Hiển thị Đích danh tên Sản phẩm
                } else {
                    mucGiamUI = mucGiamDB + "%";
                    if (giamToiDaDB > 0) {
                        mucGiamUI += " (Tối đa " + String.format("%,.0f đ", giamToiDaDB) + ")";
                    }
                    donToiThieuUI = donToiThieuDB > 0 ? String.format("%,.0f đ", donToiThieuDB) : "Không yêu cầu";
                    doiTuongUI = rs.getString("doiTuongApDung") != null && rs.getString("doiTuongApDung").equals("HOA_DON") ? "Hóa đơn" : spHienThi;
                }
                
                String thoiGianUI = "N/A"; 
                if (startDB != null && endDB != null) {
                    thoiGianUI = new java.text.SimpleDateFormat("dd/MM/yyyy").format(startDB) + " - " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(endDB);
                }

                String trangThaiUI = "Tạm dừng"; 
                boolean isToggleOn = false;

                if (!trangThaiDB) { 
                    trangThaiUI = "Tạm dừng"; 
                    isToggleOn = false; 
                } else if (startDB != null && endDB != null) {
                    if (currentDate.before(startDB)) { trangThaiUI = "Sắp diễn ra"; isToggleOn = true; } 
                    else if (currentDate.after(endDB)) { trangThaiUI = "Đã kết thúc"; isToggleOn = false; } 
                    else { trangThaiUI = "Đang hoạt động"; isToggleOn = true; }
                }
                
                listData.add(new Object[]{ 
                    id, ten, hinhThucUI, mucGiamUI, donToiThieuUI, doiTuongUI, thoiGianUI, trangThaiUI, "Xem", isToggleOn, 
                    mucGiamDB, donToiThieuDB, rs.getString("spYeuCau"), rs.getInt("slYeuCau"), rs.getString("dvdlYeuCau"),
                    rs.getString("spTang"), rs.getInt("slTang"), rs.getString("dvdlTang"), giamToiDaDB
                });
            }
        } catch (Exception ex) { 
            ex.printStackTrace(); 
        }
        return listData;
    }

    public boolean capNhatTrangThai(String maKM, boolean trangThai) {
        String sql = "UPDATE KhuyenMai SET trangThai = ? WHERE id = ?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setBoolean(1, trangThai); 
            pst.setString(2, maKM); 
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { 
            return false; 
        }
    }

    public void khoiTaoBangTichDiem() {
        Connection con = ConnectDB.getInstance().getConnection(); 
        if(con == null) return;
        String createTableSQL = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='CauHinhTichDiem' and xtype='U') " +
                                "CREATE TABLE CauHinhTichDiem (tienMua INT, diemThuong INT, tienDoiMotDiem INT, diemToiThieu INT)";
        try { 
            con.createStatement().execute(createTableSQL); 
        } catch (SQLException ignored) {}
    }

    public int[] layCauHinhTichDiem() {
        khoiTaoBangTichDiem(); 
        Connection con = ConnectDB.getInstance().getConnection(); 
        if(con == null) return null;
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM CauHinhTichDiem")) {
            if (rs.next()) return new int[] { rs.getInt("tienMua"), rs.getInt("diemThuong"), rs.getInt("tienDoiMotDiem"), rs.getInt("diemToiThieu") };
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return null;
    }

    public boolean luuCauHinhTichDiem(int tienMua, int diemThuong, int tienDoi, int diemToiThieu) {
        khoiTaoBangTichDiem(); 
        Connection con = ConnectDB.getInstance().getConnection(); 
        if(con == null) return false;
        String sql = "IF EXISTS (SELECT 1 FROM CauHinhTichDiem) " +
                     "UPDATE CauHinhTichDiem SET tienMua=?, diemThuong=?, tienDoiMotDiem=?, diemToiThieu=? " +
                     "ELSE INSERT INTO CauHinhTichDiem (tienMua, diemThuong, tienDoiMotDiem, diemToiThieu) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, tienMua); 
            pst.setInt(2, diemThuong); 
            pst.setInt(3, tienDoi); 
            pst.setInt(4, diemToiThieu);
            pst.setInt(5, tienMua); 
            pst.setInt(6, diemThuong); 
            pst.setInt(7, tienDoi); 
            pst.setInt(8, diemToiThieu);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return false;
    }
    public List<Object[]> layDanhSachKhuyenMaiFull() {
        List<Object[]> ds = new ArrayList<>();
        String sql = "SELECT k.id, k.tenKhuyenMai, h.moTa, h.loaiHinhThuc, h.giaTri, " +
                     "ISNULL(spY.ten, '') as tenSPY, ISNULL(spT.ten, '') as tenSPT " +
                     "FROM KhuyenMai k JOIN HinhThucKhuyenMai h ON k.id = h.khuyenMaiId " +
                     "LEFT JOIN SanPham spY ON h.spYeuCau = spY.id " +
                     "LEFT JOIN SanPham spT ON h.spTang = spT.id WHERE k.trangThai = 1";
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                ds.add(new Object[]{
                    rs.getString("id"), 
                    rs.getString("tenKhuyenMai"), 
                    rs.getString("moTa"),
                    rs.getString("loaiHinhThuc"), 
                    rs.getDouble("giaTri"),
                    rs.getString("tenSPY"), 
                    rs.getString("tenSPT")
                });
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return ds;
    }
    public double[] layThongKeHieuSuatKM(String maKM) {
        double[] stats = new double[]{0, 0, 0}; 
        Connection con = ConnectDB.getInstance().getConnection();
        if (con == null) return stats;

        try {
            String loaiHinhThuc = "";
            double giaTriKM = 0;
            double giamToiDa = 0;
            String sqlKM = "SELECT loaiHinhThuc, giaTri, giamToiDa FROM HinhThucKhuyenMai WHERE khuyenMaiId = ?";
            try (PreparedStatement pstKM = con.prepareStatement(sqlKM)) {
                pstKM.setString(1, maKM);
                try (ResultSet rsKM = pstKM.executeQuery()) {
                    if (rsKM.next()) {
                        loaiHinhThuc = rsKM.getString("loaiHinhThuc");
                        giaTriKM = rsKM.getDouble("giaTri");
                        giamToiDa = rsKM.getDouble("giamToiDa");
                    }
                }
            }

            String sqlHD = "SELECT hd.id, " +
                           "(SELECT ISNULL(SUM(ct.soLuong * dv.gia * (1 + (ISNULL(sp.thueVAT, 0) / 100.0))), 0) " +
                           " FROM ChiTietHoaDon ct " +
                           " JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id AND ct.sanPhamId = dv.sanPhamId " +
                           " JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                           " WHERE ct.hoaDonId = hd.id) as tongTienGoc " +
                           "FROM HoaDon hd WHERE hd.khuyenMaiId = ?";

            try (PreparedStatement pst = con.prepareStatement(sqlHD)) {
                pst.setString(1, maKM);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        double tongTienGoc = rs.getDouble("tongTienGoc");
                        double tienGiamHD = 0;

                        if (loaiHinhThuc.contains("PHAN_TRAM") || loaiHinhThuc.contains("%")) {
                            tienGiamHD = tongTienGoc * (giaTriKM / 100.0);
                            if (giamToiDa > 0 && tienGiamHD > giamToiDa) {
                                tienGiamHD = giamToiDa;
                            }
                        } else if (loaiHinhThuc.contains("TIEN_MAT")) {
                            tienGiamHD = giaTriKM;
                            if (tienGiamHD > tongTienGoc) tienGiamHD = tongTienGoc;
                        }

                        double doanhThuThucTe = tongTienGoc - tienGiamHD;
                        if (doanhThuThucTe < 0) doanhThuThucTe = 0;

                        stats[0]++; 
                        stats[1] += tienGiamHD; 
                        stats[2] += doanhThuThucTe; 
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stats;
    }
 // Thêm vào file DAO_KhuyenMai.java
    public Object[] getChiTietKhuyenMai(String maKM) {
        String sql = "SELECT h.loaiHinhThuc, ISNULL(h.giaTri, 0) AS mucGiam, " +
                      "ISNULL(d.giaTri, 0) AS dkGiaTri, d.loaiDieuKien, " +
                      "ISNULL(h.slYeuCau, 0) AS h_slYeuCau, " +
                      "ISNULL(sp.ten, ISNULL(h.spYeuCau, '')) AS tenSpYeuCau, " +
                      "ISNULL(spTang.ten, ISNULL(h.spTang, '')) AS tenSpTang, " +
                      "ISNULL(dv.ten, ISNULL(h.dvdlYeuCau, '')) AS tenDvdlYeuCau " +
                      "FROM KhuyenMai k " +
                      "JOIN HinhThucKhuyenMai h ON k.id = h.khuyenMaiId " +
                      "LEFT JOIN DieuKienKhuyenMai d ON k.id = d.khuyenMaiId " +
                      "LEFT JOIN SanPham sp ON h.spYeuCau = sp.id " +
                      "LEFT JOIN SanPham spTang ON h.spTang = spTang.id " +
                      "LEFT JOIN DonViDoLuong dv ON h.dvdlYeuCau = dv.id " +
                      "WHERE k.id = ? AND k.trangThai = 1 " +
                      "AND CAST(k.ngayBatDau AS DATE) <= CAST(GETDATE() AS DATE) " +
                      "AND (k.ngayKetThuc IS NULL OR CAST(k.ngayKetThuc AS DATE) >= CAST(GETDATE() AS DATE))";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maKM);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Object[] {
                        rs.getString("loaiHinhThuc"), rs.getDouble("mucGiam"),
                        rs.getDouble("dkGiaTri"), rs.getString("loaiDieuKien"),
                        rs.getInt("h_slYeuCau"), rs.getString("tenSpYeuCau"),
                        rs.getString("tenSpTang"), rs.getString("tenDvdlYeuCau")
                    };
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}