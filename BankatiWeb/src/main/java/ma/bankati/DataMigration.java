package ma.bankati;

import ma.bankati.dao.db.DatabaseConnection;
import ma.bankati.model.users.ERole;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

public class DataMigration {

    public static void main(String[] args) {
        try {
            // Initialize database connection
            Properties properties = new Properties();
            properties.setProperty("datasource.url", "jdbc:mysql://localhost:3306/bankatidb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
            properties.setProperty("datasource.username", "root");
            properties.setProperty("datasource.password", "tigerbaba2012"); // Change to your password
            properties.setProperty("datasource.driver", "com.mysql.cj.jdbc.Driver");

            DatabaseConnection.initialize(properties);

            // Migrate data from text files
            migrateUsers();
            migrateComptes();
            migrateDemandesCredit();

            System.out.println("Data migration completed successfully!");
        } catch (Exception e) {
            System.err.println("Error during data migration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void migrateUsers() throws IOException, SQLException {
        System.out.println("Migrating users...");
        Path usersFile = Paths.get(DataMigration.class.getClassLoader().getResource("FileBase/users.txt").getPath());
        List<String> lines = Files.readAllLines(usersFile);

        try (Connection conn = DatabaseConnection.getConnection()) {
            // First clear existing users (optional - remove if you want to keep existing MySQL data)
            conn.createStatement().executeUpdate("DELETE FROM Comptes");
            conn.createStatement().executeUpdate("DELETE FROM DemandesCredit");
            conn.createStatement().executeUpdate("DELETE FROM Users");

            String sql = "INSERT INTO Users (id, firstName, lastName, username, password, role, creationDate) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Skip header
                for (int i = 1; i < lines.size(); i++) {
                    String[] fields = lines.get(i).split("-");
                    Long id = Long.parseLong(fields[0]);
                    String firstName = fields[1].equals("null") ? null : fields[1];
                    String lastName = fields[2].equals("null") ? null : fields[2];
                    String username = fields[3].equals("null") ? null : fields[3];
                    String password = fields[4].equals("null") ? null : fields[4];
                    ERole role = fields[5].equals("null") ? null : (fields[5].equals("ADMIN") ? ERole.ADMIN : ERole.USER);
                    LocalDate dateCreation = null;
                    if (!fields[6].equals("null")) {
                        dateCreation = LocalDate.parse(fields[6], DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                    }

                    stmt.setLong(1, id);
                    stmt.setString(2, firstName);
                    stmt.setString(3, lastName);
                    stmt.setString(4, username);
                    stmt.setString(5, password);
                    stmt.setString(6, role.toString());
                    stmt.setDate(7, dateCreation != null ? java.sql.Date.valueOf(dateCreation) : java.sql.Date.valueOf(LocalDate.now()));

                    stmt.executeUpdate();
                    System.out.println("Migrated user: " + username);
                }
            }
        }
    }

    private static void migrateComptes() throws IOException, SQLException {
        System.out.println("Migrating comptes...");
        Path comptesFile = Paths.get(DataMigration.class.getClassLoader().getResource("FileBase/comptes.txt").getPath());
        List<String> lines = Files.readAllLines(comptesFile);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Comptes (id, userId, solde, devise) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Skip header
                for (int i = 1; i < lines.size(); i++) {
                    String[] fields = lines.get(i).split("-");
                    Long id = Long.parseLong(fields[0]);
                    Long userId = Long.parseLong(fields[1]);
                    Double solde = Double.parseDouble(fields[2]);
                    String devise = fields[3];

                    stmt.setLong(1, id);
                    stmt.setLong(2, userId);
                    stmt.setDouble(3, solde);
                    stmt.setString(4, devise);

                    stmt.executeUpdate();
                    System.out.println("Migrated compte for user ID: " + userId);
                }
            }
        }
    }

    private static void migrateDemandesCredit() throws IOException, SQLException {
        System.out.println("Migrating demandes de crédit...");
        Path creditsFile = Paths.get(DataMigration.class.getClassLoader().getResource("FileBase/credits.txt").getPath());
        List<String> lines = Files.readAllLines(creditsFile);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO DemandesCredit (id, userId, montant, motif, statut, dateCreation, dateTraitement, commentaire) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Skip header
                for (int i = 1; i < lines.size(); i++) {
                    String[] fields = lines.get(i).split("-");
                    if (fields.length < 8) {
                        System.err.println("Invalid line format: " + lines.get(i));
                        continue;
                    }

                    Long id = Long.parseLong(fields[0]);
                    Long userId = Long.parseLong(fields[1]);
                    Double montant = Double.parseDouble(fields[2]);
                    String motif = fields[3].equals("null") ? null : fields[3];
                    String statut = fields[4];
                    LocalDate dateCreation = LocalDate.parse(fields[5], DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                    LocalDate dateTraitement = fields[6].equals("null") ? null : LocalDate.parse(fields[6], DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                    String commentaire = fields[7].equals("null") ? null : fields[7];

                    stmt.setLong(1, id);
                    stmt.setLong(2, userId);
                    stmt.setDouble(3, montant);
                    stmt.setString(4, motif);
                    stmt.setString(5, statut);
                    stmt.setDate(6, java.sql.Date.valueOf(dateCreation));
                    stmt.setDate(7, dateTraitement != null ? java.sql.Date.valueOf(dateTraitement) : null);
                    stmt.setString(8, commentaire);

                    stmt.executeUpdate();
                    System.out.println("Migrated credit demand ID: " + id);
                }
            }
        }
    }
}