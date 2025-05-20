package ma.bankati.dao.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static String url;
    private static String username;
    private static String password;
    private static boolean initialized = false;

    public static void initialize(Properties properties) {
        url = properties.getProperty("datasource.url");
        username = properties.getProperty("datasource.username");
        password = properties.getProperty("datasource.password");

        try {
            // Load the MySQL driver
            Class.forName(properties.getProperty("datasource.driver"));
            System.out.println("MySQL JDBC driver loaded successfully: " + properties.getProperty("datasource.driver"));
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load MySQL JDBC driver: " + e.getMessage());
            e.printStackTrace();
        }

        initialized = true;

        System.out.println("Database connection parameters initialized");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
        // Don't log password for security reasons
    }

    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            throw new SQLException("Database connection not initialized. Call initialize() first.");
        }

        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Successfully established MySQL database connection");
            return conn;
        } catch (SQLException e) {
            System.err.println("Failed to establish database connection:");
            System.err.println("URL: " + url);
            System.err.println("Username: " + username);
            System.err.println("Error: " + e.getMessage());

            throw e;
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }
}