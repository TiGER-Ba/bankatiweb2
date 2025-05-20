package ma.bankati.web.controllers.userController;

import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import ma.bankati.dao.compteDao.ICompteDao;
import ma.bankati.dao.db.DatabaseConnection;
import ma.bankati.dao.userDao.IUserDao;
import ma.bankati.dao.userDao.sqlDb.UserDao;
import ma.bankati.model.compte.Compte;
import ma.bankati.model.users.ERole;
import ma.bankati.model.users.User;

public class UserController {

    private final IUserDao userDao;
    private ICompteDao compteDao;

    public UserController() {
        this.userDao = new UserDao();
    }

    public void init(ServletContext context) {
        this.compteDao = (ICompteDao) context.getAttribute("compteDao");
        System.out.println("UserController initialisé avec compteDao: " + (compteDao != null ? "OK" : "NULL!"));
    }

    public void showAll(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<User> users = userDao.findAll();
        req.setAttribute("users", users);
        req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
    }

    public void editForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = Long.parseLong(req.getParameter("id"));
        User user = userDao.findById(id);
        req.setAttribute("user", user);
        showAll(req, resp);
    }

    public void delete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = Long.parseLong(req.getParameter("id"));
        deleteUserWithRelatedData(id);
        resp.sendRedirect(req.getContextPath() + "/users");
    }

    private void deleteUserWithRelatedData(Long userId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try {
                // 1. D'abord vérifier si l'utilisateur existe
                User user = userDao.findById(userId);
                if (user == null) {
                    System.err.println("Utilisateur non trouvé: ID=" + userId);
                    return;
                }

                // 2. Supprimer les demandes de crédit de l'utilisateur
                PreparedStatement stmtCredits = conn.prepareStatement("DELETE FROM DemandesCredit WHERE userId = ?");
                stmtCredits.setLong(1, userId);
                int creditsDeleted = stmtCredits.executeUpdate();
                stmtCredits.close();
                System.out.println("Demandes de crédit supprimées pour l'utilisateur " + userId + ": " + creditsDeleted);

                // 3. Supprimer le compte bancaire de l'utilisateur
                PreparedStatement stmtComptes = conn.prepareStatement("DELETE FROM Comptes WHERE userId = ?");
                stmtComptes.setLong(1, userId);
                int comptesDeleted = stmtComptes.executeUpdate();
                stmtComptes.close();
                System.out.println("Comptes supprimés pour l'utilisateur " + userId + ": " + comptesDeleted);

                // 4. Enfin, supprimer l'utilisateur lui-même
                PreparedStatement stmtUser = conn.prepareStatement("DELETE FROM Users WHERE id = ?");
                stmtUser.setLong(1, userId);
                int usersDeleted = stmtUser.executeUpdate();
                stmtUser.close();
                System.out.println("Utilisateur supprimé: " + userId + ", résultat: " + usersDeleted);

                // Valider la transaction si tout s'est bien passé
                conn.commit();
                System.out.println("Suppression complète de l'utilisateur " + userId + " et de ses données associées");

            } catch (SQLException e) {
                // En cas d'erreur, annuler la transaction
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException se) {
                        se.printStackTrace();
                    }
                }
                System.err.println("ERREUR lors de la suppression de l'utilisateur " + userId + ": " + e.getMessage());
                e.printStackTrace();
                throw e;
            }

        } catch (Exception e) {
            System.err.println("ERREUR FATALE lors de la suppression de l'utilisateur " + userId + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException se) {
                    se.printStackTrace();
                }
            }
        }
    }

    public void saveOrUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idStr = req.getParameter("id");
        Long id = (idStr == null || idStr.isEmpty()) ? null : Long.parseLong(idStr);
        boolean isNewUser = (id == null);

        User user = User.builder()
                .id(id)
                .firstName(req.getParameter("firstName"))
                .lastName(req.getParameter("lastName"))
                .username(req.getParameter("username"))
                .password(req.getParameter("password"))
                .role(ERole.valueOf(req.getParameter("role")))
                .build();

        // Enregistrer ou mettre à jour l'utilisateur
        if (isNewUser) {
            user = userDao.save(user);
            System.out.println("Nouvel utilisateur créé - ID: " + user.getId() +
                    ", Username: " + user.getUsername() +
                    ", Rôle: " + user.getRole());

            // Créer automatiquement un compte bancaire pour le nouvel utilisateur
            createBankAccount(user.getId());
        } else {
            userDao.update(user);
            System.out.println("Utilisateur mis à jour - ID: " + user.getId() +
                    ", Username: " + user.getUsername() +
                    ", Rôle: " + user.getRole());
        }

        resp.sendRedirect(req.getContextPath() + "/users");
    }

    /**
     * Crée un compte bancaire pour un utilisateur
     */
    private void createBankAccount(Long userId) {
        System.out.println("Création d'un compte bancaire pour l'utilisateur ID: " + userId);

        // Méthode 1: Utiliser compteDao si disponible
        if (compteDao != null) {
            try {
                // Vérifier d'abord si un compte existe déjà
                Compte existingCompte = compteDao.findByUserId(userId);
                if (existingCompte != null) {
                    System.out.println("Un compte existe déjà pour l'utilisateur ID: " + userId);
                    return;
                }

                // Créer un nouveau compte avec un solde initial
                Compte newCompte = Compte.builder()
                        .userId(userId)
                        .solde(0.0)  // Solde initial à 0
                        .devise("EUR") // Devise par défaut
                        .build();

                Compte savedCompte = compteDao.save(newCompte);
                System.out.println("Compte bancaire créé via compteDao - ID: " + savedCompte.getId() +
                        ", Utilisateur ID: " + userId +
                        ", Solde initial: " + savedCompte.getSolde() +
                        ", Devise: " + savedCompte.getDevise());
                return;
            } catch (Exception e) {
                System.err.println("Erreur lors de la création du compte via compteDao: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("compteDao non disponible, utilisation de la méthode directe SQL");
        }

        // Méthode 2: Accès direct à la base de données (fallback)
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try {
                // Vérifier si un compte existe déjà
                try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM Comptes WHERE userId = ?")) {
                    checkStmt.setLong(1, userId);
                    try (var rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            System.out.println("Un compte existe déjà pour l'utilisateur ID: " + userId);
                            conn.commit();
                            return;
                        }
                    }
                }

                // Créer un nouveau compte
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO Comptes (userId, solde, devise) VALUES (?, ?, ?)",
                        PreparedStatement.RETURN_GENERATED_KEYS)) {

                    insertStmt.setLong(1, userId);
                    insertStmt.setDouble(2, 0.0); // Solde initial à 0
                    insertStmt.setString(3, "EUR"); // Devise par défaut

                    int rowsAffected = insertStmt.executeUpdate();

                    if (rowsAffected > 0) {
                        try (var generatedKeys = insertStmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                long compteId = generatedKeys.getLong(1);
                                System.out.println("Compte bancaire créé via SQL direct - ID: " + compteId +
                                        ", Utilisateur ID: " + userId +
                                        ", Solde initial: 0.0 EUR");
                            }
                        }
                        conn.commit();
                    } else {
                        System.err.println("Échec de la création du compte - Aucune ligne affectée");
                        conn.rollback();
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erreur SQL lors de la création du compte: " + e.getMessage());
                e.printStackTrace();
                conn.rollback();
            }
        } catch (Exception e) {
            System.err.println("Erreur fatale lors de la création du compte: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}