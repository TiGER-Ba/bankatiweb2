package ma.bankati;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestSqlConnection {
    public static void main(String[] args) {
        try {
            System.out.println("Testing SQL Server JDBC connection...");

            // Load the driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("JDBC driver loaded successfully");

            // Connection string
            String url = "jdbc:sqlserver://localhost;databaseName=master;encrypt=true;trustServerCertificate=true";
            String user = "sa";
            String password = "YourSaPassword"; // Replace with your actual password

            System.out.println("Attempting to connect to: " + url);

            // Connect
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                System.out.println("Connected to SQL Server successfully!");

                // Test query execution
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery("SELECT @@VERSION");

                if (rs.next()) {
                    System.out.println("SQL Server Version: " + rs.getString(1));
                }

                // Test database creation if it doesn't exist
                stmt.executeUpdate("IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'BankatiDB') BEGIN CREATE DATABASE BankatiDB END");
                System.out.println("BankatiDB database created or already exists");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("SQL Server JDBC driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("SQL Server connection error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}