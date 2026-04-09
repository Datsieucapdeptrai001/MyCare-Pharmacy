package DAO;

import ConnectDB.ConnectDB;
import Entity.HinhThucKhuyenMai;
import Entity.KhuyenMai;
import Entity.SanPham;
import Entity.DonViDoLuong;
import Enum.DoiTuongApDung;
import Enum.LoaiHinhThuc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DAO_HinhThucKhuyenMai {

	public HinhThucKhuyenMai layTheoMaKM(String maKM) {
	    HinhThucKhuyenMai htkm = null;
	    String sql = "SELECT * FROM HinhThucKhuyenMai WHERE khuyenMaiId = ?";
	    Connection con = ConnectDB.getInstance().getConnection();

	    try (PreparedStatement pst = con.prepareStatement(sql)) {
	        pst.setString(1, maKM);

	        try (ResultSet rs = pst.executeQuery()) {
	            if (rs.next()) {
	                htkm = new HinhThucKhuyenMai();
	                htkm.setId(rs.getString("id"));

	                if (rs.getString("loaiHinhThuc") != null) {
	                    htkm.setLoaiHinhThuc(LoaiHinhThuc.valueOf(rs.getString("loaiHinhThuc")));
	                }

	                if (rs.getString("doiTuongApDung") != null) {
	                    htkm.setDoiTuongApDung(DoiTuongApDung.valueOf(rs.getString("doiTuongApDung")));
	                }

	                htkm.setMoTa(rs.getString("moTa"));
	                htkm.setGiaTri(rs.getDouble("giaTri"));
	                htkm.setGiamToiDa(rs.getDouble("giamToiDa"));

	                if (rs.getString("donViDoLuongId") != null) {
	                    DonViDoLuong dvdl = new DonViDoLuong();
	                    dvdl.setId(rs.getString("donViDoLuongId"));
	                    htkm.setDonViDoLuongId(dvdl);
	                }

	                if (rs.getString("sanPhamId") != null) {
	                    SanPham sp = new SanPham();
	                    sp.setId(rs.getString("sanPhamId"));
	                    htkm.setSanPhamId(sp);
	                }

	                if (rs.getString("khuyenMaiId") != null) {
	                    KhuyenMai km = new KhuyenMai();
	                    km.setId(rs.getString("khuyenMaiId"));
	                    htkm.setKhuyenMaiId(km);
	                }
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return htkm;
	}
}