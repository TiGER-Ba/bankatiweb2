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

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCompte(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Compte> findAll() {
        List<Compte> comptes = new ArrayList<>();
        String sql = "SELECT * FROM Comptes";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Compte compte = mapResultSetToCompte(rs);
                comptes.add(compte);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return comptes;
    }

    @Override
    public Compte save(Compte compte) {
        String sql = "INSERT INTO Comptes (userId, solde, devise) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, compte.getUserId());
            stmt.setDouble(2, compte.getSolde());
            stmt.setString(3, compte.getDevise());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        compte.setId(generatedKeys.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return compte;
    }

    @Override
    public void update(Compte compte) {
        String sql = "UPDATE Comptes SET solde = ?, devise = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, compte.getSolde());
            stmt.setString(2, compte.getDevise());
            stmt.setLong(3, compte.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateSolde(Long userId, Double nouveauSolde) {
        String sql = "UPDATE Comptes SET solde = ? WHERE userId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, nouveauSolde);
            stmt.setLong(2, userId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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