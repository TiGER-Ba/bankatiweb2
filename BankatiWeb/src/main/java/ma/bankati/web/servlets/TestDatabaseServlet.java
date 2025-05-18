package ma.bankati.web.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.bankati.dao.db.DatabaseConnection;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

@WebServlet("/test-db")
public class TestDatabaseServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.println("<html><head><title>Database Test</title></head><body>");
        out.println("<h1>Database Connection Test</h1>");

        try {
            Connection conn = DatabaseConnection.getConnection();
            out.println("<p style='color:green'>Database connection successful!</p>");

            // Test a simple query
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery("SELECT @@VERSION");

            if (rs.next()) {
                out.println("<p>SQL Server Version: " + rs.getString(1) + "</p>");
            }

            // Test User table
            rs = stmt.executeQuery("SELECT COUNT(*) FROM Users");
            if (rs.next()) {
                out.println("<p>Number of users in database: " + rs.getInt(1) + "</p>");
            }

            // Test Compte table
            rs = stmt.executeQuery("SELECT COUNT(*) FROM Comptes");
            if (rs.next()) {
                out.println("<p>Number of accounts in database: " + rs.getInt(1) + "</p>");
            }

            // Test DemandeCredit table
            rs = stmt.executeQuery("SELECT COUNT(*) FROM DemandesCredit");
            if (rs.next()) {
                out.println("<p>Number of credit requests in database: " + rs.getInt(1) + "</p>");
            }

            conn.close();

            out.println("<p><a href='" + req.getContextPath() + "/login'>Go to Login Page</a></p>");
        } catch (Exception e) {
            out.println("<p style='color:red'>Database connection failed: " + e.getMessage() + "</p>");
            e.printStackTrace(out);
        }

        out.println("</body></html>");
    }
}