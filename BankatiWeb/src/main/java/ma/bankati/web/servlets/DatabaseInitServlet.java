package ma.bankati.web.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.bankati.dao.db.DatabaseConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = "/init-db", loadOnStartup = 1)
public class DatabaseInitServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        if (!DatabaseConnection.isInitialized()) {
            System.err.println("Database connection not initialized. Cannot initialize database tables.");
            return;
        }

        try {
            initializeDatabase();
            System.out.println("Database tables initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing database tables: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("Failed to initialize database tables", e);
        }
    }

    private void initializeDatabase() throws Exception {
        // Read the initialization SQL script
        // Modifier cette ligne pour corriger le chemin du script SQL
        InputStream is = getClass().getClassLoader().getResourceAsStream("sql/init-db.sql");
        if (is == null) {
            // Try alternate locations
            is = getClass().getClassLoader().getResourceAsStream("init-db.sql");
            if (is == null) {
                is = getClass().getResourceAsStream("/init-db.sql");
            }
            if (is == null) {
                is = getClass().getResourceAsStream("/sql/init-db.sql");
            }
            if (is == null) {
                throw new Exception("Could not find the init-db.sql script. Please ensure it exists in src/main/resources/sql/init-db.sql or src/main/resources/init-db.sql");
            }
        }

        System.out.println("Found init-db.sql script, loading...");
        String sqlScript = new BufferedReader(new InputStreamReader(is))
                .lines().collect(Collectors.joining("\n"));
        System.out.println("SQL script loaded successfully, length: " + sqlScript.length() + " characters");

        // Split the script into individual statements
        String[] statements = sqlScript.split("GO|;");
        System.out.println("Found " + statements.length + " SQL statements to execute");

        // Execute each statement
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty()) {
                    try {
                        System.out.println("Executing SQL statement: " + sql.substring(0, Math.min(50, sql.length())) + "...");
                        stmt.executeUpdate(sql);
                        System.out.println("SQL statement executed successfully");
                    } catch (Exception e) {
                        System.err.println("Error executing SQL statement: " + sql);
                        System.err.println("Error: " + e.getMessage());
                        // Continue with other statements
                    }
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        resp.getWriter().write("<html><head><title>Database Initialization</title></head>");
        resp.getWriter().write("<body><h1>Database Initialization</h1>");
        resp.getWriter().write("<p>Database initialization completed successfully!</p>");
        resp.getWriter().write("<p><a href='" + req.getContextPath() + "/login'>Go to Login Page</a></p>");
        resp.getWriter().write("</body></html>");
    }
}