package ma.bankati.utils;

import ma.bankati.dao.compteDao.ICompteDao;
import ma.bankati.model.compte.Compte;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import ma.bankati.dao.db.DatabaseConnection;

public class CreditCompteUtil {

    /**
     * Met à jour directement le solde d'un compte utilisateur dans la base de données
     */
    public static boolean updateSoldeUserInDB(Long userId, double montantEuro) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction explicitly

            String sql = "UPDATE Comptes SET solde = solde + ? WHERE userId = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, montantEuro);
                stmt.setLong(2, userId);

                int rowsAffected = stmt.executeUpdate();
                System.out.println("MISE À JOUR DIRECTE DU SOLDE - Utilisateur ID: " + userId +
                        ", Montant crédité: " + montantEuro + " EUR, Lignes affectées: " + rowsAffected);

                conn.commit(); // Commit the transaction
                return rowsAffected > 0;
            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback(); // Rollback in case of error
                    } catch (SQLException se) {
                        se.printStackTrace();
                    }
                }
                throw e;
            }
        } catch (Exception e) {
            System.err.println("ERREUR lors de la mise à jour du solde pour l'utilisateur " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset to default
                    conn.close();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
            }
        }
    }

    /**
     * Affiche le solde actuel d'un compte utilisateur en base de données
     */
    public static double getSoldeUserFromDB(Long userId, ICompteDao compteDao) {
        try {
            Compte compte = compteDao.findByUserId(userId);
            if (compte != null) {
                System.out.println("SOLDE ACTUEL - Utilisateur ID: " + userId +
                        ", Solde: " + compte.getSolde() + " EUR, Devise: " + compte.getDevise());
                return compte.getSolde();
            } else {
                System.err.println("COMPTE NON TROUVÉ pour l'utilisateur ID: " + userId);
                return 0.0;
            }
        } catch (Exception e) {
            System.err.println("ERREUR lors de la récupération du solde pour l'utilisateur " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * Récupère directement le solde de l'utilisateur depuis la base de données
     */
    public static double getSoldeDirectFromDB(Long userId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT solde FROM Comptes WHERE userId = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, userId);
                try (var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        double solde = rs.getDouble("solde");
                        System.out.println("SOLDE DIRECT - Utilisateur ID: " + userId + ", Solde: " + solde + " EUR");
                        return solde;
                    } else {
                        System.err.println("AUCUN COMPTE TROUVÉ pour l'utilisateur ID: " + userId);
                        return 0.0;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("ERREUR lors de la récupération directe du solde pour l'utilisateur " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
            }
        }
    }
}