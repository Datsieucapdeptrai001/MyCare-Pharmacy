package BUS;

import DAO.DAO_LoHang;
import Entity.LoHang;
import Enum.TrangThaiLoHang;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ConnectDB.ConnectDB;

public class BUS_Kho {
    private DAO_LoHang daoLoHang;

    public BUS_Kho() {
        this.daoLoHang = new DAO_LoHang();
    }

    // Nghiệp vụ: Kiểm tra xem tổng tồn kho của thuốc này có đủ để bán không
    public boolean kiemTraTonKho(String maSP, int soLuongCanBan) {
        List<LoHang> dsLoHienCo = daoLoHang.layLoTheoSP(maSP);
        int tongTonKho = 0;
        
        for (LoHang lh : dsLoHienCo) {
            tongTonKho += lh.getSoLuongLoHang();
        }
        
        return tongTonKho >= soLuongCanBan;
    }

    // Nghiệp vụ: Cảnh báo hàng cận Date (Sắp hết hạn)
    // Tham số soNgayCanhBao: Ví dụ truyền vào 90 (Cảnh báo trước 3 tháng)
    public List<LoHang> canhBaoHangSapHetHan(int soNgayCanhBao) {
        // Tái sử dụng hàm layDSLoHang (bạn đã có trong DAO_LoHang từ trước)
        List<LoHang> dsToanBoLo = new DAO_LoHang().layDSLoHang(); 
        List<LoHang> dsCanhBao = new ArrayList<>();
        
        LocalDateTime thoiDiemHienTai = LocalDateTime.now();
        LocalDateTime thoiDiemGioiHan = thoiDiemHienTai.plusDays(soNgayCanhBao);

        for (LoHang lh : dsToanBoLo) {
            // Lô chưa hết hạn, nhưng ngày hết hạn nằm trong khoảng cảnh báo
            if (lh.getNgayHetHan().isAfter(thoiDiemHienTai) && lh.getNgayHetHan().isBefore(thoiDiemGioiHan)) {
                if (lh.getSoLuongLoHang() > 0) {
                    dsCanhBao.add(lh);
                }
            }
        }
        return dsCanhBao;
    }

    // Nghiệp vụ quan trọng nhất: Xuất kho theo chuẩn FEFO (Hết hạn trước -> Xuất trước)
    // Trả về số lượng thuốc THỰC TẾ không thể đáp ứng (nếu kho không đủ hàng)
    // Nếu kho đủ hàng, nó sẽ trả về 0 và tự động trừ số lượng trong DB.
    public int xuLyXuatKhoFEFO(String maSP, int soLuongCanXuat) {
        Connection con = null;

        try {
            con = ConnectDB.getInstance().getConnection();
            con.setAutoCommit(false);

            List<LoHang> dsLo = daoLoHang.layLoTheoSP(con, maSP);
            int soLuongConThieu = soLuongCanXuat;

            for (LoHang lh : dsLo) {
                if (soLuongConThieu <= 0) {
                    break;
                }

                if (lh == null) {
                    continue;
                }

                int soLuongTrongLo = lh.getSoLuongLoHang();

                if (soLuongTrongLo >= soLuongConThieu) {
                    boolean ok = daoLoHang.capNhatSoLuongTon(con, lh.getId(), soLuongTrongLo - soLuongConThieu);
                    if (!ok) {
                        throw new SQLException("Không cập nhật được số lượng tồn cho lô " + lh.getId());
                    }

                    if (soLuongTrongLo - soLuongConThieu == 0) {
                        ok = daoLoHang.capNhatTrangThaiLo(con, lh.getId(), TrangThaiLoHang.HET_HANG);
                        if (!ok) {
                            throw new SQLException("Không cập nhật được trạng thái lô " + lh.getId());
                        }
                    }

                    soLuongConThieu = 0;
                } else {
                    boolean ok = daoLoHang.capNhatSoLuongTon(con, lh.getId(), 0);
                    if (!ok) {
                        throw new SQLException("Không cập nhật được số lượng tồn cho lô " + lh.getId());
                    }

                    ok = daoLoHang.capNhatTrangThaiLo(con, lh.getId(), TrangThaiLoHang.HET_HANG);
                    if (!ok) {
                        throw new SQLException("Không cập nhật được trạng thái lô " + lh.getId());
                    }

                    soLuongConThieu -= soLuongTrongLo;
                }
            }

            if (soLuongConThieu > 0) {
                throw new SQLException("Kho không đủ hàng. Còn thiếu " + soLuongConThieu + " đơn vị.");
            }

            con.commit();
            return 0;

        } catch (Exception e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            e.printStackTrace();
            return soLuongCanXuat;
        } finally {
            try {
                if (con != null) {
                    con.setAutoCommit(true);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    // Nghiệp vụ: Kiểm kê kho định kỳ (Quét và tự động chuyển trạng thái các lô hàng)
    public boolean kiemKeKho() {
        try {
            List<LoHang> dsToanBo = new DAO_LoHang().layDSLoHang();
            LocalDateTime hienTai = LocalDateTime.now();

            for (LoHang lh : dsToanBo) {
                if (lh == null) {
                    continue;
                }

                // Nếu qua ngày hết hạn -> Chuyển thành HẾT HẠN
                if (lh.getNgayHetHan() != null
                        && lh.getNgayHetHan().isBefore(hienTai)
                        && lh.getTrangThai() != TrangThaiLoHang.HET_HAN) {
                    daoLoHang.capNhatTrangThaiLo(lh.getId(), TrangThaiLoHang.HET_HAN);
                }
                // Nếu số lượng về 0 -> Chuyển thành HẾT HÀNG
                else if (lh.getSoLuongLoHang() == 0
                        && lh.getTrangThai() != TrangThaiLoHang.HET_HANG) {
                    daoLoHang.capNhatTrangThaiLo(lh.getId(), TrangThaiLoHang.HET_HANG);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}