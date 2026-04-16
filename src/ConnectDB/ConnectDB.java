package ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static final ConnectDB instance = new ConnectDB();

    // Thông tin kết nối của bạn giữ nguyên
    private static final String URL =
            "jdbc:sqlserver://localhost:1433;databaseName=MYCAREPHARMACY;encrypt=false";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456789";

    private ConnectDB() {
        // Constructor mặc định
    }

    public static ConnectDB getInstance() {
        return instance;
    }

    // HÀM QUAN TRỌNG NHẤT: Luôn tạo và trả về một kết nối mới tinh
    public Connection getConnection() {
        Connection con = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            // Bỏ dòng in "Kết nối thành công" ở đây để Console của bạn đỡ bị spam hàng loạt tin nhắn
        } catch (Exception e) {
            System.out.println("Kết nối DB thất bại!");
            e.printStackTrace();
        }
        return con;
    }

    // Hàm test thử kết nối lúc mới chạy app (dùng ở file Main nếu muốn)
    public void connect() {
        try {
            Connection testCon = getConnection();
            if (testCon != null && !testCon.isClosed()) {
                System.out.println("Khởi tạo kết nối DB thành công!");
                testCon.close(); // Test xong thì đóng
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Hàm disconnect() không còn cần thiết nữa vì các DAO sẽ tự quản lý đóng mở riêng
    public void disconnect() {
        System.out.println("Đã chuyển sang chế độ tự động quản lý kết nối!");
    }
}