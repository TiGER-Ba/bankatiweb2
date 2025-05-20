package ma.bankati.utils;

import ma.bankati.dao.compteDao.ICompteDao;
import ma.bankati.model.compte.Compte;
import java.sql.Connection;
import java.sql.PreparedStatement;
import ma.bankati.dao.db.DatabaseConnection;

public class CreditCompteUtil {

    /**
     * Met à jour directement le solde d'un compte utilisateur dans la base de données
     */
    public static boolean updateSoldeUserInDB(Long userId, double montantEuro) {
        try {
            String sql = "UPDATE Comptes SET solde = solde + ? WHERE userId = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setDouble(1, montantEuro);
                stmt.setLong(2, userId);

                int rowsAffected = stmt.executeUpdate();
                System.out.println("MISE À JOUR DIRECTE DU SOLDE - Utilisateur ID: " + userId +
                        ", Montant crédité: " + montantEuro + " EUR, Lignes affectées: " + rowsAffected);

                return rowsAffected > 0;
            }
        } catch (Exception e) {
            System.err.println("ERREUR lors de la mise à jour du solde pour l'utilisateur " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
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
}