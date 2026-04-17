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
        // Đảm bảo kết nối DB luôn sẵn sàng ở tầng Data
        try {
            ConnectDB.getInstance().connect();
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo kết nối DB tại DAO_KhuyenMai: " + e.getMessage());
        }
    }

    // ===========================================
    // CÁC HÀM QUẢN LÝ KHUYẾN MÃI CHÍNH
    // ===========================================
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

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
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
        
        if (con == null) {
            System.err.println("Lỗi: Không có kết nối Database!");
            return listData;
        }
        
        try {
            // Kiểm tra xem cột trangThai đã tồn tại chưa (để tránh lỗi SQL nếu chưa chạy ALTER TABLE)
            boolean hasTrangThaiCol = true;
            try (Statement st = con.createStatement()) {
                st.executeQuery("SELECT trangThai FROM KhuyenMai WHERE 1=0");
            } catch (SQLException e) { 
                hasTrangThaiCol = false; 
            }

            String sql = "SELECT k.id, k.tenKhuyenMai, k.ngayBatDau, k.ngayKetThuc, " +
                         (hasTrangThaiCol ? "k.trangThai, " : "") +
                         "h.loaiHinhThuc, h.giaTri as mucGiam, h.spTang, h.slTang, h.spYeuCau, h.slYeuCau, " + 
                         "d.giaTri as donToiThieu, h.doiTuongApDung " +
                         "FROM KhuyenMai k " +
                         "LEFT JOIN HinhThucKhuyenMai h ON k.id = h.khuyenMaiId " +
                         "LEFT JOIN DieuKienKhuyenMai d ON k.id = d.khuyenMaiId";
            
            try (PreparedStatement stmt = con.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                java.util.Date currentDate = new java.util.Date();

                while (rs.next()) {
                    String id = rs.getString("id");
                    String ten = rs.getString("tenKhuyenMai");
                    Timestamp startDB = rs.getTimestamp("ngayBatDau");
                    Timestamp endDB = rs.getTimestamp("ngayKetThuc");
                    String hinhThucDB = rs.getString("loaiHinhThuc");
                    
                    // ĐỌC CỘT TRẠNG THÁI TỪ DB
                    boolean trangThaiDB = true;
                    if (hasTrangThaiCol) {
                        trangThaiDB = rs.getBoolean("trangThai");
                    }

                    // --- CÁC BIẾN KIỂU DOUBLE GỐC DÙNG ĐỂ TÍNH TOÁN ---
                    double mucGiamDB = rs.getDouble("mucGiam");
                    double donToiThieuDB = 0;
                    if(rs.getObject("donToiThieu") != null) {
                        donToiThieuDB = rs.getDouble("donToiThieu");
                    }

                    // --- CÁC BIẾN KIỂU STRING DÙNG ĐỂ HIỂN THỊ LÊN BẢNG (GUI) ---
                    String hinhThucUI = "Giảm phần trăm (%)";
                    String mucGiamUI = "";
                    String donToiThieuUI = "";
                    String doiTuongUI = "";
                    
                    if ("SAN_PHAM_KEM_THEO".equals(hinhThucDB)) {
                        hinhThucUI = "Sản phẩm kèm theo";
                        String spTang = rs.getString("spTang");
                        int slTang = rs.getInt("slTang");
                        
                        mucGiamUI = "Tặng " + slTang + " " + (spTang != null ? spTang : "SP");
                        donToiThieuUI = "Mọi đơn hàng";
                        doiTuongUI = "Tất cả";
                    } else {
                        mucGiamUI = mucGiamDB + "%";
                        donToiThieuUI = donToiThieuDB > 0 ? String.format("%,.0f đ", donToiThieuDB) : "Không yêu cầu";
                        doiTuongUI = rs.getString("doiTuongApDung") != null && rs.getString("doiTuongApDung").equals("HOA_DON") ? "Hóa đơn" : "Sản phẩm";
                    }
                    
                    String thoiGianUI = "N/A";
                    String trangThaiUI = "Tạm dừng";
                    boolean isToggleOn = false;

                    // XỬ LÝ LỌC TRẠNG THÁI BẬT/TẮT CỦA NGƯỜI DÙNG
                    if (!trangThaiDB) { 
                        trangThaiUI = "Tạm dừng";
                        isToggleOn = false;
                    } else if (startDB != null && endDB != null) {
                        thoiGianUI = new java.text.SimpleDateFormat("dd/MM/yyyy").format(startDB) + " - " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(endDB);
                        
                        if (currentDate.before(startDB)) {
                            trangThaiUI = "Sắp diễn ra";
                            isToggleOn = true;
                        } else if (currentDate.after(endDB)) {
                            trangThaiUI = "Đã kết thúc";
                            isToggleOn = false;
                        } else {
                            trangThaiUI = "Đang hoạt động";
                            isToggleOn = true;
                        }
                    }
                    
                    // QUAN TRỌNG NHẤT LÀ Ở ĐÂY: Thêm mucGiamDB và donToiThieuDB vào vị trí số [10] và [11]
                    listData.add(new Object[]{ 
                        id,             // [0]
                        ten,            // [1]
                        hinhThucUI,     // [2]
                        mucGiamUI,      // [3] (Dạng chữ, VD: "10%" - Để show lên bảng)
                        donToiThieuUI,  // [4] (Dạng chữ, VD: "200.000 đ" - Để show lên bảng)
                        doiTuongUI,     // [5]
                        thoiGianUI,     // [6]
                        trangThaiUI,    // [7]
                        "Xem",          // [8]
                        isToggleOn,     // [9]
                        mucGiamDB,      // [10] (Dạng số thực Double - ĐỂ TÍNH TIỀN TRONG GUI)
                        donToiThieuDB   // [11] (Dạng số thực Double - ĐỂ TÍNH TIỀN TRONG GUI)
                    });
                }
            }
        } catch (Exception ex) { 
            ex.printStackTrace();
        }
        return listData;
    }
    public boolean capNhatTrangThai(String maKM, boolean trangThai) {
        String sql = "UPDATE KhuyenMai SET trangThai = ? WHERE id = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setBoolean(1, trangThai);
            pst.setString(2, maKM);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
    // ===========================================
    // CÁC HÀM QUẢN LÝ CẤU HÌNH TÍCH ĐIỂM
    // ===========================================
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
        try (ResultSet rs = con.createStatement().executeQuery("SELECT * FROM CauHinhTichDiem")) {
            if (rs.next()) {
                return new int[] {
                    rs.getInt("tienMua"), rs.getInt("diemThuong"), 
                    rs.getInt("tienDoiMotDiem"), rs.getInt("diemToiThieu")
                };
            }
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
            pst.setInt(1, tienMua); pst.setInt(2, diemThuong); pst.setInt(3, tienDoi); pst.setInt(4, diemToiThieu);
            pst.setInt(5, tienMua); pst.setInt(6, diemThuong); pst.setInt(7, tienDoi); pst.setInt(8, diemToiThieu);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}