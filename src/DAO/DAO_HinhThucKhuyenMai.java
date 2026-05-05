package DAO;

import ConnectDB.ConnectDB;
import Entity.HinhThucKhuyenMai;
import Entity.KhuyenMai;
import Entity.SanPham;
import Enumeration.DoiTuongApDung;
import Enumeration.LoaiHinhThuc;
import Entity.DonViDoLuong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DAO_HinhThucKhuyenMai {

    public boolean themHinhThuc(HinhThucKhuyenMai htkm) {
        String sql = "INSERT INTO HinhThucKhuyenMai (id, loaiHinhThuc, doiTuongApDung, moTa, giaTri, giamToiDa, khuyenMaiId, spYeuCau, slYeuCau, dvdlYeuCau, spTang, slTang, dvdlTang) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, htkm.getId());
            pst.setString(2, htkm.getLoaiHinhThuc() != null ? htkm.getLoaiHinhThuc().name() : null);
            pst.setString(3, htkm.getDoiTuongApDung() != null ? htkm.getDoiTuongApDung().name() : null);
            pst.setString(4, htkm.getMoTa());
            pst.setDouble(5, htkm.getGiaTri());
            pst.setDouble(6, htkm.getGiamToiDa());
            pst.setString(7, htkm.getKhuyenMaiId() != null ? htkm.getKhuyenMaiId().getId() : null);
            pst.setString(8, htkm.getSpYeuCau());
            pst.setInt(9, htkm.getSlYeuCau());
            pst.setString(10, htkm.getDvdlYeuCau());
            pst.setString(11, htkm.getSpTang());
            pst.setInt(12, htkm.getSlTang());
            pst.setString(13, htkm.getDvdlTang());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean capNhatHinhThuc(HinhThucKhuyenMai htkm) {
        String sql = "UPDATE HinhThucKhuyenMai SET loaiHinhThuc=?, doiTuongApDung=?, moTa=?, giaTri=?, giamToiDa=?, spYeuCau=?, slYeuCau=?, dvdlYeuCau=?, spTang=?, slTang=?, dvdlTang=? WHERE khuyenMaiId=?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, htkm.getLoaiHinhThuc() != null ? htkm.getLoaiHinhThuc().name() : null);
            pst.setString(2, htkm.getDoiTuongApDung() != null ? htkm.getDoiTuongApDung().name() : null);
            pst.setString(3, htkm.getMoTa());
            pst.setDouble(4, htkm.getGiaTri());
            pst.setDouble(5, htkm.getGiamToiDa());
            pst.setString(6, htkm.getSpYeuCau());
            pst.setInt(7, htkm.getSlYeuCau());
            pst.setString(8, htkm.getDvdlYeuCau());
            pst.setString(9, htkm.getSpTang());
            pst.setInt(10, htkm.getSlTang());
            pst.setString(11, htkm.getDvdlTang());
            pst.setString(12, htkm.getKhuyenMaiId().getId());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean xoaTheoMaKM(String maKM) {
        String sql = "DELETE FROM HinhThucKhuyenMai WHERE khuyenMaiId=?";
        Connection con = ConnectDB.getInstance().getConnection();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maKM);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

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
                    if (rs.getString("loaiHinhThuc") != null) htkm.setLoaiHinhThuc(LoaiHinhThuc.valueOf(rs.getString("loaiHinhThuc")));
                    if (rs.getString("doiTuongApDung") != null) htkm.setDoiTuongApDung(DoiTuongApDung.valueOf(rs.getString("doiTuongApDung")));
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
                    KhuyenMai km = new KhuyenMai();
                    km.setId(rs.getString("khuyenMaiId"));
                    htkm.setKhuyenMaiId(km);

                    try {
                        htkm.setSpYeuCau(rs.getString("spYeuCau"));
                        htkm.setSlYeuCau(rs.getInt("slYeuCau"));
                        htkm.setDvdlYeuCau(rs.getString("dvdlYeuCau"));
                        htkm.setSpTang(rs.getString("spTang"));
                        htkm.setSlTang(rs.getInt("slTang"));
                        htkm.setDvdlTang(rs.getString("dvdlTang"));
                    } catch(Exception ignored){}
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return htkm;
    }
}