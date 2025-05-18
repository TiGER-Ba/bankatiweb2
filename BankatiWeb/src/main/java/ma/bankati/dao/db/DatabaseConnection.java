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
        initialized = true;

        System.out.println("Database connection parameters initialized");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
    }

    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            throw new SQLException("Database connection not initialized. Call initialize() first.");
        }
        return DriverManager.getConnection(url, username, password);
    }

    public static boolean isInitialized() {
        return initialized;
    }
}