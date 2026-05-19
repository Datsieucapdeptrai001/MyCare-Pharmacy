package BUS;

import DAO.DAO_ChiTietLieuMau;
import Entity.ChiTietLieuMau;
import java.util.List;

public class BUS_ChiTietLieuMau {
    private DAO_ChiTietLieuMau daoChiTiet = new DAO_ChiTietLieuMau();

    public List<ChiTietLieuMau> getChiTietTheoLieuMau(String lieuMauId) {
        return daoChiTiet.getChiTietTheoLieuMau(lieuMauId);
    }

    public boolean themChiTiet(ChiTietLieuMau ct) {
        // Có thể thêm code kiểm tra nghiệp vụ ở đây (ví dụ: Số lượng phải > 0)
        if (ct.getSoLuong() <= 0) {
            return false;
        }
        return daoChiTiet.themChiTiet(ct);
    }

    public boolean xoaChiTietCuaLieu(String lieuMauId) {
        return daoChiTiet.xoaChiTietCuaLieu(lieuMauId);
    }
}