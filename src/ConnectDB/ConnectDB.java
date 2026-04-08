package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectDB {
    private static Connection con = null;
    private static ConnectDB instance = new ConnectDB();
    public static ConnectDB getInstance() {
        return instance;
    }
    public void connect() {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=MYCAREPHARMACY;encrypt=false";
        String user = "sa";
        String password = "123456789";

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); // thêm dòng này

            con = DriverManager.getConnection(url, user, password);
            System.out.println("Kết nối DB thành công!");
        } catch (Exception e) {
            System.out.println("Kết nối DB thất bại!");
            e.printStackTrace();
        }
    }
    public void disconnect() {
        if (con != null) {
            try {
                con.close();
                System.out.println("Đã ngắt kết nối!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public Connection getConnection() {
        return con;
    }
}