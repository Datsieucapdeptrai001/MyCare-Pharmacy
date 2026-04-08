package GUI;

import ConnectDB.ConnectDB;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        ConnectDB.getInstance().connect();

        Connection con = ConnectDB.getInstance().getConnection();

        if (con != null) {
            System.out.println("OK rồi!");
        }
    }
}