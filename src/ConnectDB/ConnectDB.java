package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static Connection con = null;
    private static final ConnectDB instance = new ConnectDB();

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;databaseName=MYCAREPHARMACY;encrypt=false";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456789";

    public static ConnectDB getInstance() {
        return instance;
    }

    public void connect() {
        try {
            if (con == null || con.isClosed()) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                con = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Kết nối DB thành công!");
            }
        } catch (Exception e) {
            System.out.println("Kết nối DB thất bại!");
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                con = null;
                System.out.println("Đã ngắt kết nối!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            // Kiểm tra: Nếu biến connection chưa có, HOẶC đã bị đóng -> Kết nối lại
            if (con == null || con.isClosed()) {
                connect(); // Gọi lại hàm tạo kết nối (hàm chứa DriverManager.getConnection...) của bạn
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }
}