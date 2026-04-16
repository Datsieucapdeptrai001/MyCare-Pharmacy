package BUS;

import DAO.DAO_LoHang;
import Entity.LoHang;
import Enumeration.TrangThaiLoHang;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ConnectDB.ConnectDB;

public class BUS_Kho {
    private final DAO_LoHang daoLoHang;

    public BUS_Kho() {
        this.daoLoHang = new DAO_LoHang();
    }

    public boolean kiemTraTonKho(String maSP, int soLuongCanBan) {
        List<LoHang> dsLoHienCo = daoLoHang.layLoTheoSP(maSP);
        int tongTonKho = 0;

        for (LoHang lh : dsLoHienCo) {
            tongTonKho += lh.getSoLuongLoHang();
        }

        return tongTonKho >= soLuongCanBan;
    }

    public List<LoHang> canhBaoHangSapHetHan(int soNgayCanhBao) {
        List<LoHang> dsToanBoLo = daoLoHang.layDSLoHang();
        List<LoHang> dsCanhBao = new ArrayList<>();

        LocalDateTime hienTai = LocalDateTime.now();
        LocalDateTime mocCanhBao = hienTai.plusDays(soNgayCanhBao);

        for (LoHang lh : dsToanBoLo) {
            if (lh == null || lh.getNgayHetHan() == null) continue;

            if (lh.getNgayHetHan().isAfter(hienTai)
                    && lh.getNgayHetHan().isBefore(mocCanhBao)
                    && lh.getSoLuongLoHang() > 0
                    && lh.getTrangThai() != TrangThaiLoHang.HET_HAN) {
                dsCanhBao.add(lh);
            }
        }

        return dsCanhBao;
    }

    public int xuLyXuatKhoFEFO(String maSP, int soLuongCanXuat) {
        if (maSP == null || maSP.trim().isEmpty() || soLuongCanXuat <= 0) {
            throw new IllegalArgumentException("Mã sản phẩm hoặc số lượng cần xuất không hợp lệ.");
        }

        Connection con = null;
        int soLuongBanDau = soLuongCanXuat;

        try {
            con = ConnectDB.getInstance().getConnection();
            con.setAutoCommit(false);

            List<LoHang> dsLo = daoLoHang.layLoTheoSP(con, maSP);
            int soLuongConThieu = soLuongCanXuat;

            for (LoHang lh : dsLo) {
                if (soLuongConThieu == 0) break;
                if (lh == null || lh.getSoLuongLoHang() <= 0) continue;

                int soLuongDaXuat = xuatTuMotLo(con, lh, soLuongConThieu);
                soLuongConThieu -= soLuongDaXuat;
            }

            if (soLuongConThieu > 0) {
                throw new SQLException("Kho không đủ hàng. Còn thiếu " + soLuongConThieu + " đơn vị.");
            }

            con.commit();
            return 0;

        } catch (Exception e) {
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return soLuongBanDau;

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

    private int xuatTuMotLo(Connection con, LoHang lh, int soLuongCanXuat) throws SQLException {
        int soLuongTrongLo = lh.getSoLuongLoHang();
        int soLuongXuatThucTe = Math.min(soLuongTrongLo, soLuongCanXuat);
        int soLuongMoi = soLuongTrongLo - soLuongXuatThucTe;

        boolean ok = daoLoHang.capNhatSoLuongTon(con, lh.getId(), soLuongMoi);
        if (!ok) {
            throw new SQLException("Không cập nhật được số lượng tồn cho lô " + lh.getId());
        }

        if (soLuongMoi == 0) {
            ok = daoLoHang.capNhatTrangThaiLo(con, lh.getId(), TrangThaiLoHang.HET_HANG);
            if (!ok) {
                throw new SQLException("Không cập nhật được trạng thái lô " + lh.getId());
            }
        }

        return soLuongXuatThucTe;
    }

    public boolean kiemKeKho() {
        try {
            List<LoHang> dsToanBo = daoLoHang.layDSLoHang();
            LocalDateTime hienTai = LocalDateTime.now();

            for (LoHang lh : dsToanBo) {
                if (lh == null) continue;

                if (lh.getNgayHetHan() != null
                        && lh.getNgayHetHan().isBefore(hienTai)
                        && lh.getTrangThai() != TrangThaiLoHang.HET_HAN) {
                    daoLoHang.capNhatTrangThaiLo(lh.getId(), TrangThaiLoHang.HET_HAN);

                } else if (lh.getSoLuongLoHang() == 0
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

    // =========================
    // HAM TRUNG GIAN CHO GUI
    // =========================

    public List<LoHang> layDSLoHang() {
        return daoLoHang.layDSLoHang();
    }

    public boolean themLoHang(LoHang loHang) {
        if (loHang == null) return false;
        return daoLoHang.themLoHang(loHang);
    }

    public boolean capNhatSoLuongTon(String maLoHang, int soLuongMoi) {
        return daoLoHang.capNhatSoLuongTon(maLoHang, soLuongMoi);
    }

    public boolean capNhatTrangThaiLo(String maLoHang, TrangThaiLoHang trangThaiMoi) {
        return daoLoHang.capNhatTrangThaiLo(maLoHang, trangThaiMoi);
    }

    public boolean capNhatLoHetHang(String maLoHang) {
        if (maLoHang == null || maLoHang.trim().isEmpty()) {
            return false;
        }

        boolean ok1 = daoLoHang.capNhatSoLuongTon(maLoHang, 0);
        boolean ok2 = daoLoHang.capNhatTrangThaiLo(maLoHang, TrangThaiLoHang.HET_HANG);
        return ok1 && ok2;
    }
}