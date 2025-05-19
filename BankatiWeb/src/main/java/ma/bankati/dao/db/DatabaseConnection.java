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
        // Don't log password for security reasons
    }

    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            throw new SQLException("Database connection not initialized. Call initialize() first.");
        }

        // Add connection debugging
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Successfully established database connection");
            return conn;
        } catch (SQLException e) {
            System.err.println("Failed to establish database connection:");
            System.err.println("URL: " + url);
            System.err.println("Username: " + username);
            System.err.println("Error: " + e.getMessage());

            // Try connecting without specifying a database, just to check SQL Server connectivity
            try {
                String baseUrl = url.substring(0, url.indexOf(";databaseName="));
                baseUrl += ";encrypt=true;trustServerCertificate=true";
                System.out.println("Trying to connect to SQL Server without specific database: " + baseUrl);
                Connection conn = DriverManager.getConnection(baseUrl, username, password);
                System.out.println("Connected to SQL Server, but not to the specific database. The database might not exist.");

                // If we get here, SQL Server is running but the database doesn't exist
                // Try to create the database
                try {
                    System.out.println("Attempting to create database...");
                    var stmt = conn.createStatement();
                    stmt.executeUpdate("IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'BankatiDB') BEGIN CREATE DATABASE BankatiDB END");
                    System.out.println("Successfully created database BankatiDB or it already exists");
                    conn.close();

                    // Now try again with the original URL
                    return DriverManager.getConnection(url, username, password);
                } catch (SQLException createEx) {
                    System.err.println("Failed to create database: " + createEx.getMessage());
                    throw e; // Throw the original exception
                }
            } catch (SQLException baseEx) {
                System.err.println("Also failed to connect to SQL Server without database: " + baseEx.getMessage());
                System.err.println("SQL Server may not be running or accepting connections.");
                throw e; // Throw the original exception
            }
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }
}