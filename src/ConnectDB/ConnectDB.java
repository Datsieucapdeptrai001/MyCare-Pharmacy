package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;

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

    public static Connection getConnection() {
        try {
            if (con == null || con.isClosed()) {
                instance.connect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return con;
    }
}