package DAO;

import ConnectDB.ConnectDB;
import Entity.ChiTietHoaDon;
import Entity.HoaDon;
import Entity.KhachHang;
import Entity.KhuyenMai;
import Entity.LoHang;
import Entity.NhanVien;
import Entity.PhanBoLoHang;
import Enumeration.LoaiHoaDon;
import Enumeration.PhuongThucThanhToan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DAO_HoaDon {

    public DAO_HoaDon() {}

    public List<Object[]> layDanhSachHoaDonChoBang() {
        List<Object[]> ds = new ArrayList<>();
        
        // SỬA: Thêm hd.loaiHD và BỎ điều kiện "WHERE hd.loaiHD = 'BAN_HANG'"
        String sql = "SELECT hd.id, hd.loaiHD, hd.ngayLapHD, kh.hoVaTen, kh.sdt, hd.phuongThucThanhToan, hd.ghiChu, " +
                "(SELECT SUM(ct.soLuong * dv.gia * (1 + (ISNULL(sp.thueVAT, 0) / 100))) " + 
                " FROM ChiTietHoaDon ct " +
                " JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id AND ct.sanPhamId = dv.sanPhamId " +
                " JOIN SanPham sp ON ct.sanPhamId = sp.id " + 
                " WHERE ct.hoaDonId = hd.id) as tongTienGoc " +
                "FROM HoaDon hd " +
                "LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id " +
                "ORDER BY hd.ngayLapHD DESC"; // Bỏ WHERE đi để lấy mọi loại hóa đơn
                     
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            
            DecimalFormat df = new DecimalFormat("#,###đ");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            while (rs.next()) {
                double totalAmount = rs.getDouble("tongTienGoc");
                String ghiChu = rs.getString("ghiChu");
                String loaiHD = rs.getString("loaiHD"); // Đọc loại HD từ SQL lên
                
                // XỬ LÝ KHẤU TRỪ TIỀN GIẢM GIÁ
                if (ghiChu != null && !ghiChu.isEmpty()) {
                    String[] parts = ghiChu.split("\\|");
                    for (String p : parts) {
                        p = p.trim();
                        if (p.startsWith("Dùng điểm: -") || p.contains("KM_GIAM:")) {
                            try {
                                long tienGiam = Long.parseLong(p.replaceAll("[^0-9]", ""));
                                totalAmount -= tienGiam;
                            } catch (Exception ignored) {}
                        }
                    }
                }
                
                if (totalAmount < 0) totalAmount = 0;

                String id = rs.getString("id");
                String ngay = rs.getTimestamp("ngayLapHD") != null 
                             ? rs.getTimestamp("ngayLapHD").toLocalDateTime().format(dtf) : "";
                String kh = rs.getString("hoVaTen") != null ? rs.getString("hoVaTen") : "Khách lẻ";
                String sdt = rs.getString("sdt") != null ? rs.getString("sdt") : "";
                
                String pt = rs.getString("phuongThucThanhToan");
                String hienThiPT = "CHUYEN_KHOAN_NGAN_HANG".equals(pt) ? "Chuyển khoản" : "Tiền mặt";

                // LOGIC MỚI: XÁC ĐỊNH ĐÚNG TRẠNG THÁI HIỂN THỊ
                String trangThai = "Hoàn thành";
                if (loaiHD != null && (loaiHD.equals("TRA_HANG") || loaiHD.equals("DOI_HANG"))) {
                    trangThai = "Đổi trả";
                } else if (ghiChu != null) {
                    if (ghiChu.contains("Lưu nháp") || ghiChu.contains("Đang xử lý")) trangThai = "Đang xử lý";
                    else if (ghiChu.contains("Đã hủy")) trangThai = "Đã hủy";
                }

                ds.add(new Object[]{
                    id, ngay, kh, sdt, hienThiPT, df.format(totalAmount), trangThai, ghiChu
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }
    
    public List<Object[]> layDanhSachHoaDonCuaNhanVien(String maNV) {
        List<Object[]> ds = new ArrayList<>();
        
        // SỬA: Thêm hd.loaiHD vào SELECT và GROUP BY. Xóa phần hd.loaiHD = 'BAN_HANG'
        String sql = "SELECT hd.id, hd.loaiHD, hd.ngayLapHD, kh.hoVaTen, kh.sdt, hd.phuongThucThanhToan, hd.ghiChu, " +
                     "SUM(ct.soLuong * dv.gia) as tongTien " +
                     "FROM HoaDon hd " +
                     "LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id " +
                     "LEFT JOIN ChiTietHoaDon ct ON hd.id = ct.hoaDonId " +
                     "LEFT JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id AND ct.sanPhamId = dv.sanPhamId " +
                     "WHERE hd.nhanVienId = ? " + // Chỉ lọc theo nhân viên
                     "GROUP BY hd.id, hd.loaiHD, hd.ngayLapHD, kh.hoVaTen, kh.sdt, hd.phuongThucThanhToan, hd.ghiChu " +
                     "ORDER BY hd.ngayLapHD DESC";

        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maNV); 
            
            try (ResultSet rs = pst.executeQuery()) {
                DecimalFormat df = new DecimalFormat("#,###đ");
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

                while (rs.next()) {
                    String maHD = rs.getString("id");
                    String loaiHD = rs.getString("loaiHD");
                    String ngay = rs.getTimestamp("ngayLapHD") != null ? rs.getTimestamp("ngayLapHD").toLocalDateTime().format(dtf) : "";
                    String tenKH = rs.getString("hoVaTen") != null ? rs.getString("hoVaTen") : "Khách lẻ";
                    String sdt = rs.getString("sdt") != null ? rs.getString("sdt") : "";
                    
                    String pttt = rs.getString("phuongThucThanhToan");
                    String pt = (pttt != null && pttt.equals("TIEN_MAT")) ? "Tiền mặt" : "Chuyển khoản";
                    
                    String tongTien = df.format(rs.getDouble("tongTien"));
                    String ghiChu = rs.getString("ghiChu");
                    
                    // LOGIC MỚI: ĐỒNG BỘ TRẠNG THÁI VỚI ADMIN
                    String trangThai = "Hoàn thành";
                    if (loaiHD != null && (loaiHD.equals("TRA_HANG") || loaiHD.equals("DOI_HANG"))) {
                        trangThai = "Đổi trả";
                    } else if (ghiChu != null) {
                        if (ghiChu.contains("Lưu nháp") || ghiChu.contains("Đang xử lý")) trangThai = "Đang xử lý";
                        else if (ghiChu.contains("Đã hủy")) trangThai = "Đã hủy";
                    }
                    
                    ds.add(new Object[]{maHD, ngay, tenKH, sdt, pt, tongTien, trangThai});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    public HoaDon timHoaDonTheoMa(String maHD) {
        HoaDon hd = null;
        // Query lấy thông tin hóa đơn và tính tổng tiền từ bảng ChiTiet
        String sql = "SELECT hd.*, kh.hoVaTen, " +
                     "(SELECT SUM(ct.soLuong * dv.gia) FROM ChiTietHoaDon ct " +
                     " JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id AND ct.sanPhamId = dv.sanPhamId " +
                     " WHERE ct.hoaDonId = hd.id) as tongTien " +
                     "FROM HoaDon hd " +
                     "LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id " +
                     "WHERE hd.id = ?";

        // Đưa Connection ra ngoài
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setString(1, maHD);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    hd = new HoaDon();
                    // FIX: Sử dụng đúng hàm setId() và setNgayLapHD() từ Entity
                    hd.setId(rs.getString("id"));
                    if (rs.getTimestamp("ngayLapHD") != null) {
                        hd.setNgayLapHD(rs.getTimestamp("ngayLapHD").toLocalDateTime());
                    }
                    
                    // Khởi tạo đối tượng khách hàng
                    KhachHang kh = new KhachHang();
                    kh.setHoVaTen(rs.getString("hoVaTen") != null ? rs.getString("hoVaTen") : "Khách lẻ");
                    hd.setKhachHangId(kh); // FIX: Đúng tên hàm setKhachHangId
                    
                    // Lưu tạm tổng tiền vào ghi chú hoặc xử lý riêng tùy logic của bạn
                    hd.setGhiChu(String.valueOf(rs.getDouble("tongTien"))); 
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hd;
    }

    public boolean themHoaDon(HoaDon hd) {
        String sql = "INSERT INTO HoaDon (id, loaiHD, ghiChu, ngayLapHD, nhanVienId, khachHangId, khuyenMaiId, phuongThucThanhToan, hoaDonGocId) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int n = 0;
        // Đã đúng, không có try(Connection) ở đây
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, hd.getId());
            pst.setString(2, hd.getLoaiHD().name());
            pst.setString(3, hd.getGhiChu());
            pst.setTimestamp(4, Timestamp.valueOf(hd.getNgayLapHD()));

            pst.setString(5, hd.getNhanVienId().getNhanVien());

            if (hd.getKhachHangId() != null) {
                pst.setString(6, hd.getKhachHangId().getId());
            } else {
                pst.setNull(6, java.sql.Types.NVARCHAR);
            }

            if (hd.getKhuyenMaiId() != null) {
                pst.setString(7, hd.getKhuyenMaiId().getId());
            } else {
                pst.setNull(7, java.sql.Types.NVARCHAR);
            }

            pst.setString(8, hd.getPhuongThucThanhToan().name());

            if (hd.getHoaDonGocId() != null) {
                pst.setString(9, hd.getHoaDonGocId().getId());
            } else {
                pst.setNull(9, java.sql.Types.NVARCHAR);
            }

            n = pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return n > 0;
    }

    public HoaDon layHoaDonTheoMa(String maHD) {
        HoaDon hd = null;
        String sql = "SELECT * FROM HoaDon WHERE id = ?";
        // Đã đúng, không có try(Connection) ở đây
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maHD);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    hd = new HoaDon();
                    hd.setId(rs.getString("id"));
                    hd.setGhiChu(rs.getString("ghiChu"));

                    if (rs.getString("loaiHD") != null) {
                        hd.setLoaiHD(LoaiHoaDon.valueOf(rs.getString("loaiHD")));
                    }

                    if (rs.getTimestamp("ngayLapHD") != null) {
                        hd.setNgayLapHD(rs.getTimestamp("ngayLapHD").toLocalDateTime());
                    }

                    if (rs.getString("nhanVienId") != null) {
                        NhanVien nv = new NhanVien();
                        nv.setNhanVien(rs.getString("nhanVienId"));
                        hd.setNhanVienId(nv);
                    }

                    if (rs.getString("khachHangId") != null) {
                        KhachHang kh = new KhachHang();
                        kh.setId(rs.getString("khachHangId"));
                        hd.setKhachHangId(kh);
                    }

                    if (rs.getString("khuyenMaiId") != null) {
                        KhuyenMai km = new KhuyenMai();
                        km.setId(rs.getString("khuyenMaiId"));
                        hd.setKhuyenMaiId(km);
                    }

                    if (rs.getString("phuongThucThanhToan") != null) {
                        hd.setPhuongThucThanhToan(
                            PhuongThucThanhToan.valueOf(rs.getString("phuongThucThanhToan"))
                        );
                    }

                    if (rs.getString("hoaDonGocId") != null) {
                        HoaDon hdGoc = new HoaDon();
                        hdGoc.setId(rs.getString("hoaDonGocId"));
                        hd.setHoaDonGocId(hdGoc);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return hd;
    }

    public ArrayList<HoaDon> getDanhSachHoaDonLuuNhap() {
        ArrayList<HoaDon> dsHoaDonNhap = new ArrayList<>();
        
        String sql = "SELECT * FROM HoaDon WHERE ghiChu = N'Lưu nháp'"; 
        
        // Đưa Connection ra ngoài
        Connection con = ConnectDB.getInstance().getConnection(); 
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String maHD = rs.getString("id");
                String maNV = rs.getString("nhanVienId");
                String maKH = rs.getString("khachHangId");
                
                java.sql.Timestamp sqlTimestamp = rs.getTimestamp("ngayLapHD");
                LocalDateTime ngayLap = (sqlTimestamp != null) ? sqlTimestamp.toLocalDateTime() : null;
                
                NhanVien nv = new NhanVien();
                if(maNV != null) nv.setNhanVien(maNV); 
                
                KhachHang kh = new KhachHang();
                if(maKH != null) kh.setId(maKH);
                
                HoaDon hd = new HoaDon();
                hd.setId(maHD);
                hd.setNgayLapHD(ngayLap);
                hd.setNhanVienId(nv);
                
                if (maKH != null) {
                    hd.setKhachHangId(kh);
                }
                
                if (rs.getString("loaiHD") != null) {
                    hd.setLoaiHD(LoaiHoaDon.valueOf(rs.getString("loaiHD")));
                }
                hd.setGhiChu(rs.getString("ghiChu"));
                
                dsHoaDonNhap.add(hd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsHoaDonNhap;
    }

    public List<HoaDon> layTatCaHoaDon() {
        List<HoaDon> dsHD = new java.util.ArrayList<>();
        try {
            // Đưa Connection ra ngoài và xóa Statement/ResultSet khỏi try để không bị auto close nếu có lỗi
            java.sql.Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT id, ngayLapHD FROM HoaDon"; 
            
            try(java.sql.PreparedStatement pst = con.prepareStatement(sql);
                java.sql.ResultSet rs = pst.executeQuery()){
                
                while (rs.next()) {
                    HoaDon hd = new HoaDon();
                    hd.setId(rs.getString("id"));
                    if (rs.getTimestamp("ngayLapHD") != null) {
                        hd.setNgayLapHD(rs.getTimestamp("ngayLapHD").toLocalDateTime());
                    }
                    dsHD.add(hd);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsHD;
    }

    public boolean capNhatTrangThai(String idHD, LoaiHoaDon loaiMoi) {
        String sql = "UPDATE HoaDon SET loaiHD = ? WHERE id = ?";
        // Đã đúng, không có try(Connection) ở đây
        Connection con = ConnectDB.getInstance().getConnection();

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, loaiMoi.name());
            pst.setString(2, idHD);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
    public boolean luuGiaoDichThanhToan(HoaDon hd, List<ChiTietHoaDon> dsCTHD,
                                        DAO_ChiTietHoaDon daoCTHD,
                                        DAO_LoHang daoLo,
                                        DAO_PhanBoLoHang daoPB) {
        Connection con = null;
        try {
            con = ConnectDB.getInstance().getConnection();
            con.setAutoCommit(false);

            // Xóa nháp cũ nếu tồn tại
            try (PreparedStatement pDel1 = con.prepareStatement("DELETE FROM ChiTietHoaDon WHERE hoaDonId = ?");
                 PreparedStatement pDel2 = con.prepareStatement("DELETE FROM HoaDon WHERE id = ?")) {
                pDel1.setString(1, hd.getId()); pDel1.executeUpdate();
                pDel2.setString(1, hd.getId()); pDel2.executeUpdate();
            } catch (Exception ignored) {}

            if (!themHoaDon(con, hd)) throw new Exception("Lỗi lưu hóa đơn");

            for (ChiTietHoaDon ct : dsCTHD) {
                if (!daoCTHD.themCTHD(con, ct)) throw new Exception("Lỗi lưu chi tiết");

                List<LoHang> dsLo = daoLo.layLoTheoSP(con, ct.getSanPhamId().getId());
                int canLay = ct.getSoLuong();

                for (LoHang lh : dsLo) {
                    if (canLay <= 0) break;
                    int layDuoc = Math.min(lh.getSoLuongLoHang(), canLay);

                    daoPB.themPhanBo(con, new PhanBoLoHang(hd, ct.getDonViDoLuongId(), ct.getSanPhamId(), lh, layDuoc));
                    daoLo.capNhatSoLuongVaTrangThaiLo(con, lh.getId(), lh.getSoLuongLoHang() - layDuoc);
                    canLay -= layDuoc;
                }
                if (canLay > 0) throw new Exception("Kho không đủ hàng: " + ct.getSanPhamId().getId());
            }

            con.commit();
            return true;
        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Hàm mới này nhận Connection từ bên ngoài truyền vào, không tự tạo Connection mới
    public boolean themHoaDon(Connection con, HoaDon hd) throws SQLException {
        String sql = "INSERT INTO HoaDon (id, loaiHD, ghiChu, ngayLapHD, nhanVienId, khachHangId, khuyenMaiId, phuongThucThanhToan, hoaDonGocId) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        // Lưu ý: Không dùng try-with-resources cho Connection ở đây, vì mình cần giữ nó mở cho các thao tác khác
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, hd.getId());
            pst.setString(2, hd.getLoaiHD().name());
            pst.setString(3, hd.getGhiChu());
            pst.setTimestamp(4, Timestamp.valueOf(hd.getNgayLapHD()));
            pst.setString(5, hd.getNhanVienId().getNhanVien());

            if (hd.getKhachHangId() != null) {
                pst.setString(6, hd.getKhachHangId().getId());
            } else {
                pst.setNull(6, java.sql.Types.NVARCHAR);
            }

            if (hd.getKhuyenMaiId() != null) {
                pst.setString(7, hd.getKhuyenMaiId().getId());
            } else {
                pst.setNull(7, java.sql.Types.NVARCHAR);
            }

            pst.setString(8, hd.getPhuongThucThanhToan().name());

            if (hd.getHoaDonGocId() != null) {
                pst.setString(9, hd.getHoaDonGocId().getId());
            } else {
                pst.setNull(9, java.sql.Types.NVARCHAR);
            }

            return pst.executeUpdate() > 0;
        }
    }
 // Thêm vào class DAO_HoaDon.java
    public boolean capNhatTrangThaiVaGhiChu(String maPhieu, String trangThaiMoi, String ghiChuMoi) {
        // Logic: Cập nhật cột ghiChu để các hàm layDanhSach có thể nhận diện trạng thái
        String sql = "UPDATE HoaDon SET ghiChu = ? WHERE id = ?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, trangThaiMoi + " | " + ghiChuMoi);
            pst.setString(2, maPhieu);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
 // 1. Lấy danh sách Phiếu đổi trả từ SQL
 // =========================================================================
    // CÁC HÀM XỬ LÝ RIÊNG CHO MÀN HÌNH ĐỔI / TRẢ HÀNG
    // =========================================================================

    public List<Object[]> layDanhSachPhieuDoiTra() {
        List<Object[]> list = new ArrayList<>();
        // CHỈ LẤY CÁC PHIẾU LÀ ĐỔI HOẶC TRẢ HÀNG
        String sql = "SELECT hd.id, hd.hoaDonGocId, kh.hoVaTen, hd.loaiHD, hd.ghiChu, hd.ngayLapHD " +
                     "FROM HoaDon hd " +
                     "LEFT JOIN KhachHang kh ON hd.khachHangId = kh.id " +
                     "WHERE hd.loaiHD IN ('TRA_HANG', 'DOI_HANG') " +
                     "ORDER BY hd.ngayLapHD DESC";
        
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
             
             DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
             
             while (rs.next()) {
                 String maPhieu = rs.getString("id");
                 String hdGoc = rs.getString("hoaDonGocId");
                 String khach = rs.getString("hoVaTen") != null ? rs.getString("hoVaTen") : "Khách lẻ";
                 String loaiHD = rs.getString("loaiHD").equals("TRA_HANG") ? "Trả hàng" : "Đổi hàng";
                 String ghiChuDB = rs.getString("ghiChu"); 
                 String ngay = rs.getTimestamp("ngayLapHD").toLocalDateTime().format(dtf);
                 
                 list.add(new Object[]{maPhieu, hdGoc, khach, loaiHD, ghiChuDB, ngay});
             }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean capNhatTrangThaiPhieuDoiTra(String maPhieu, String trangThaiMoi) {
        Connection con = ConnectDB.getInstance().getConnection();
        String ghiChuHienTai = "";

        // BƯỚC 1: LẤY GHI CHÚ HIỆN TẠI TỪ DB LÊN
        String sqlSelect = "SELECT ghiChu FROM HoaDon WHERE id = ?";
        try (PreparedStatement pstSelect = con.prepareStatement(sqlSelect)) {
            pstSelect.setString(1, maPhieu);
            try (ResultSet rs = pstSelect.executeQuery()) {
                if (rs.next()) {
                    ghiChuHienTai = rs.getString("ghiChu");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // BƯỚC 2: DÙNG JAVA ĐỂ TÁCH VÀ GẮN LẠI TRẠNG THÁI MỚI (CHẮC CHẮN 100%)
        if (ghiChuHienTai == null || ghiChuHienTai.isEmpty()) return false;

        // Split chuỗi ra bằng dấu "|" 
        String[] parts = ghiChuHienTai.split("\\|");
        if (parts.length > 0) {
            // Thay thế phần tử đầu tiên (Trạng thái) bằng trạng thái mới
            parts[0] = trangThaiMoi + " ";
        }
        
        // Nối chuỗi lại
        String ghiChuMoi = String.join("|", parts);

        // BƯỚC 3: UPDATE GHI CHÚ MỚI VÀO LẠI DB
        String sqlUpdate = "UPDATE HoaDon SET ghiChu = ? WHERE id = ?";
        try (PreparedStatement pstUpdate = con.prepareStatement(sqlUpdate)) {
            pstUpdate.setString(1, ghiChuMoi);
            pstUpdate.setString(2, maPhieu);
            return pstUpdate.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public Object[] layThongTinGiaTuHDGoc(String maHDGoc, String tenSP) {
        // 1. Thử lấy giá từ bảng ChiTietHoaDon (để đảm bảo lấy đúng giá gốc lúc giao dịch)
        String sql = "SELECT dv.ten, dv.gia FROM ChiTietHoaDon ct " +
                     "JOIN SanPham sp ON ct.sanPhamId = sp.id " +
                     "JOIN DonViDoLuong dv ON ct.donViDoLuongId = dv.id " +
                     "WHERE ct.hoaDonId = ? AND sp.ten = ?";
        
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maHDGoc);
            pst.setString(2, tenSP);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{ rs.getString(1), rs.getDouble(2) };
                }
            }
            
            // 2. FALLBACK: NẾU KHÔNG TÌM THẤY (Do SP mới đổi lấy chưa lưu kịp vào ChiTietHoaDon)
            // -> Truy vấn trực tiếp giá bán hiện hành của sản phẩm đó từ danh mục
            String sqlFallback = "SELECT TOP 1 dv.ten, dv.gia FROM SanPham sp " +
                                 "JOIN DonViDoLuong dv ON sp.id = dv.sanPhamId " +
                                 "WHERE sp.ten = ?";
            try (PreparedStatement pst2 = con.prepareStatement(sqlFallback)) {
                pst2.setString(1, tenSP);
                try (ResultSet rs2 = pst2.executeQuery()) {
                    if (rs2.next()) {
                        return new Object[]{ rs2.getString(1), rs2.getDouble(2) };
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        
        return new Object[]{ "Hộp", 0.0 }; 
    }
}