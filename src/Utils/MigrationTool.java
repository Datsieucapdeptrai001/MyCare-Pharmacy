package Utils;

import ConnectDB.ConnectDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MigrationTool {

    public static void main(String[] args) {
        System.out.println("🚀 BẮT ĐẦU QUÁ TRÌNH NÂNG CẤP BẢO MẬT DỮ LIỆU (DATA MIGRATION)...");
        
        try {
            // 1. Mở kết nối đến Database (Gọi hàm connect của bạn nếu cần)
            ConnectDB.getInstance().connect(); 
            Connection con = ConnectDB.getInstance().getConnection();
            
            if (con == null) {
                System.out.println("❌ Lỗi: Không thể kết nối đến Database!");
                return;
            }

            // 2. Lấy toàn bộ tài khoản lên
            String sqlSelect = "SELECT id, tenDangNhap, matKhau FROM TaiKhoan";
            PreparedStatement pstSelect = con.prepareStatement(sqlSelect);
            ResultSet rs = pstSelect.executeQuery();

            // 3. Chuẩn bị lệnh Update
            String sqlUpdate = "UPDATE TaiKhoan SET matKhau = ? WHERE id = ?";
            PreparedStatement pstUpdate = con.prepareStatement(sqlUpdate);

            int count = 0;

            // 4. Duyệt qua từng tài khoản và xử lý
            while (rs.next()) {
                String id = rs.getString("id");
                String tenDangNhap = rs.getString("tenDangNhap");
                String matKhauCu = rs.getString("matKhau");

                // Nếu mật khẩu CHƯA CÓ dấu ":" tức là mật khẩu thô -> Phải băm ngay!
                if (!matKhauCu.contains(":")) {
                    System.out.print("Đang mã hóa tài khoản [" + tenDangNhap + "]... ");
                    
                    // Gọi hàm băm của hệ thống
                    String matKhauDaBam = PasswordUtils.hashPassword(matKhauCu);
                    
                    // Cập nhật lại xuống DB
                    pstUpdate.setString(1, matKhauDaBam);
                    pstUpdate.setString(2, id);
                    pstUpdate.executeUpdate();
                    
                    System.out.println("✅ THÀNH CÔNG!");
                    count++;
                }
            }
            
            System.out.println("==================================================");
            System.out.println("🎉 HOÀN TẤT! Đã bọc thép an toàn cho " + count + " tài khoản.");
            
        } catch (Exception e) {
            System.out.println("❌ Có lỗi xảy ra trong quá trình Migration:");
            e.printStackTrace();
        }
    }
}