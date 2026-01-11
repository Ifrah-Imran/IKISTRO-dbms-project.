package backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleDB {
    // Update these credentials as needed
private static final String URL = "jdbc:oracle:thin:@localhost:1521/orclpdb";
    private static final String DB_USER = "YOUR_USERNAME";
    private static final String DB_PASSWORD = "YOUR_PASSWORD";

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Oracle JDBC Driver not found. Please add ojdbc jar to classpath.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
    }

    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Connected to Oracle Database successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Failed to connect to Oracle Database.");
            e.printStackTrace();
        }
    }
}
