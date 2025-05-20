package ma.bankati.dao.compteDao.sqlDb;

import ma.bankati.dao.compteDao.ICompteDao;
import ma.bankati.dao.db.DatabaseConnection;
import ma.bankati.model.compte.Compte;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompteDao implements ICompteDao {

    @Override
    public Compte findByUserId(Long userId) {
        String sql = "SELECT * FROM Comptes WHERE userId = ?";
        System.out.println("RECHERCHE COMPTE - UserID: " + userId);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Compte compte = mapResultSetToCompte(rs);
                    System.out.println("COMPTE TROUVÉ - ID: " + compte.getId() +
                            ", UserID: " + compte.getUserId() +
                            ", Solde: " + compte.getSolde() +
                            ", Devise: " + compte.getDevise());
                    return compte;
                } else {
                    System.out.println("AUCUN COMPTE TROUVÉ pour UserID: " + userId);
                }
            }
        } catch (SQLException e) {
            System.err.println("ERREUR RECHERCHE COMPTE - UserID: " + userId + " - " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Compte> findAll() {
        List<Compte> comptes = new ArrayList<>();
        String sql = "SELECT * FROM Comptes";
        System.out.println("RECHERCHE TOUS LES COMPTES");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Compte compte = mapResultSetToCompte(rs);
                comptes.add(compte);
            }
            System.out.println("COMPTES TROUVÉS: " + comptes.size());
        } catch (SQLException e) {
            System.err.println("ERREUR RECHERCHE TOUS COMPTES - " + e.getMessage());
            e.printStackTrace();
        }

        return comptes;
    }

    @Override
    public Compte save(Compte compte) {
        String sql = "INSERT INTO Comptes (userId, solde, devise) VALUES (?, ?, ?)";
        System.out.println("CRÉATION COMPTE - UserID: " + compte.getUserId() +
                ", Solde: " + compte.getSolde() +
                ", Devise: " + compte.getDevise());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, compte.getUserId());
            stmt.setDouble(2, compte.getSolde());
            stmt.setString(3, compte.getDevise());

            int affectedRows = stmt.executeUpdate();
            System.out.println("CRÉATION COMPTE - Lignes affectées: " + affectedRows);

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Long id = generatedKeys.getLong(1);
                        compte.setId(id);
                        System.out.println("COMPTE CRÉÉ - ID généré: " + id);
                    }
                }
            } else {
                System.err.println("ÉCHEC CRÉATION COMPTE - Aucune ligne affectée");
            }
        } catch (SQLException e) {
            System.err.println("ERREUR CRÉATION COMPTE - " + e.getMessage());
            e.printStackTrace();
        }

        return compte;
    }

    @Override
    public void update(Compte compte) {
        String sql = "UPDATE Comptes SET solde = ?, devise = ? WHERE id = ?";
        System.out.println("MISE À JOUR COMPTE - ID: " + compte.getId() +
                ", UserID: " + compte.getUserId() +
                ", Nouveau solde: " + compte.getSolde() +
                ", Devise: " + compte.getDevise());

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            // Désactiver l'auto-commit pour cette transaction
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, compte.getSolde());
                stmt.setString(2, compte.getDevise());
                stmt.setLong(3, compte.getId());

                int rowsAffected = stmt.executeUpdate();
                System.out.println("MISE À JOUR COMPTE - Lignes affectées: " + rowsAffected);

                if (rowsAffected > 0) {
                    // La mise à jour a fonctionné, on valide
                    conn.commit();
                    System.out.println("MISE À JOUR COMPTE RÉUSSIE - ID: " + compte.getId());
                } else {
                    // Aucune ligne affectée, on annule
                    conn.rollback();
                    System.err.println("ÉCHEC MISE À JOUR COMPTE - ID: " + compte.getId() + " - Aucune ligne affectée");

                    // Vérifier si le compte existe
                    try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM Comptes WHERE id = ?")) {
                        checkStmt.setLong(1, compte.getId());
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next() && rs.getInt(1) == 0) {
                                System.err.println("COMPTE INEXISTANT - ID: " + compte.getId());
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                // En cas d'erreur, on annule
                conn.rollback();
                throw e;
            } finally {
                // Restaurer l'auto-commit
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            System.err.println("ERREUR MISE À JOUR COMPTE - ID: " + compte.getId() + " - " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void updateSolde(Long userId, Double nouveauSolde) {
        String sql = "UPDATE Comptes SET solde = ? WHERE userId = ?";
        System.out.println("MISE À JOUR SOLDE - UserID: " + userId + ", Nouveau solde: " + nouveauSolde);

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            // Désactiver l'auto-commit pour cette transaction
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, nouveauSolde);
                stmt.setLong(2, userId);

                int rowsAffected = stmt.executeUpdate();
                System.out.println("MISE À JOUR SOLDE - Lignes affectées: " + rowsAffected);

                if (rowsAffected > 0) {
                    // La mise à jour a fonctionné, on valide
                    conn.commit();
                    System.out.println("MISE À JOUR SOLDE RÉUSSIE - UserID: " + userId);

                    // Vérifier le nouveau solde pour confirmation
                    try (PreparedStatement checkStmt = conn.prepareStatement("SELECT solde FROM Comptes WHERE userId = ?")) {
                        checkStmt.setLong(1, userId);
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next()) {
                                double actualSolde = rs.getDouble("solde");
                                System.out.println("SOLDE ACTUEL APRÈS MISE À JOUR: " + actualSolde);
                            }
                        }
                    }
                } else {
                    // Aucune ligne affectée, on annule
                    conn.rollback();
                    System.err.println("ÉCHEC MISE À JOUR SOLDE - UserID: " + userId + " - Aucune ligne affectée");

                    // Vérifier si le compte existe
                    try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM Comptes WHERE userId = ?")) {
                        checkStmt.setLong(1, userId);
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next() && rs.getInt(1) == 0) {
                                System.err.println("AUCUN COMPTE TROUVÉ POUR L'UTILISATEUR - UserID: " + userId);
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                // En cas d'erreur, on annule
                conn.rollback();
                throw e;
            } finally {
                // Restaurer l'auto-commit
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            System.err.println("ERREUR MISE À JOUR SOLDE - UserID: " + userId + " - " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Méthode spécifique pour incrémenter directement le solde d'un compte
     * Cette méthode est utile pour les opérations comme l'approbation de crédit
     */
    public boolean incrementerSolde(Long userId, Double montant) {
        String sql = "UPDATE Comptes SET solde = solde + ? WHERE userId = ?";
        System.out.println("INCRÉMENTATION SOLDE - UserID: " + userId + ", Montant: " + montant);

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            // Désactiver l'auto-commit pour cette transaction
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                // D'abord, vérifier le solde actuel
                double soldeActuel = 0.0;
                try (PreparedStatement checkStmt = conn.prepareStatement("SELECT solde FROM Comptes WHERE userId = ?")) {
                    checkStmt.setLong(1, userId);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            soldeActuel = rs.getDouble("solde");
                            System.out.println("SOLDE ACTUEL AVANT INCRÉMENTATION: " + soldeActuel);
                        } else {
                            System.err.println("AUCUN COMPTE TROUVÉ POUR L'UTILISATEUR - UserID: " + userId);
                            conn.rollback();
                            return false;
                        }
                    }
                }

                // Ensuite, mettre à jour le solde
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setDouble(1, montant);
                    stmt.setLong(2, userId);

                    int rowsAffected = stmt.executeUpdate();
                    System.out.println("INCRÉMENTATION SOLDE - Lignes affectées: " + rowsAffected);

                    if (rowsAffected > 0) {
                        // Vérifier le nouveau solde
                        try (PreparedStatement verifyStmt = conn.prepareStatement("SELECT solde FROM Comptes WHERE userId = ?")) {
                            verifyStmt.setLong(1, userId);
                            try (ResultSet rs = verifyStmt.executeQuery()) {
                                if (rs.next()) {
                                    double nouveauSolde = rs.getDouble("solde");
                                    System.out.println("SOLDE APRÈS INCRÉMENTATION: " + nouveauSolde +
                                            " (Ancien: " + soldeActuel + " + " + montant + ")");

                                    // Vérifier que l'incrémentation a bien eu lieu
                                    if (Math.abs(nouveauSolde - (soldeActuel + montant)) < 0.001) {
                                        // Le solde a été correctement mis à jour
                                        conn.commit();
                                        System.out.println("INCRÉMENTATION SOLDE RÉUSSIE - UserID: " + userId);
                                        return true;
                                    } else {
                                        // Anomalie détectée
                                        System.err.println("ANOMALIE DÉTECTÉE - Le solde ne correspond pas à la valeur attendue");
                                        conn.rollback();
                                        return false;
                                    }
                                }
                            }
                        }

                        // Si on arrive ici, c'est qu'on n'a pas pu vérifier le nouveau solde
                        conn.commit();
                        return true;
                    } else {
                        // Aucune ligne affectée
                        conn.rollback();
                        System.err.println("ÉCHEC INCRÉMENTATION SOLDE - UserID: " + userId + " - Aucune ligne affectée");
                        return false;
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            System.err.println("ERREUR INCRÉMENTATION SOLDE - UserID: " + userId + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Compte mapResultSetToCompte(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long userId = rs.getLong("userId");
        Double solde = rs.getDouble("solde");
        String devise = rs.getString("devise");

        return Compte.builder()
                .id(id)
                .userId(userId)
                .solde(solde)
                .devise(devise)
                .build();
    }
}