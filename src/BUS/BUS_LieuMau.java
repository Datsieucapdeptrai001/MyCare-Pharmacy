package BUS;

import DAO.DAO_LieuMau;
import Entity.LieuMau;
import java.util.List;

public class BUS_LieuMau {
    private DAO_LieuMau daoLieuMau = new DAO_LieuMau();

    // Lấy danh sách tất cả các Liều Mẫu để đưa lên ComboBox
    public List<LieuMau> getTatCaLieuMau() {
        return daoLieuMau.getTatCaLieuMau();
    }

    // Lấy chi tiết các thuốc bên trong 1 liều mẫu để đổ vào Bảng
    public List<Object[]> getChiTietThuocCuaLieu(String idLieuMau) {
        return daoLieuMau.getChiTietThuocCuaLieu(idLieuMau);
    }
}