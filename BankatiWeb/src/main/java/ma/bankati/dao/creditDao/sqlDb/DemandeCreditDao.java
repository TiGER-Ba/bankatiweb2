package ma.bankati.dao.creditDao.sqlDb;

import ma.bankati.dao.creditDao.IDemandeCreditDao;
import ma.bankati.dao.db.DatabaseConnection;
import ma.bankati.model.credit.DemandeCredit;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DemandeCreditDao implements IDemandeCreditDao {

    @Override
    public List<DemandeCredit> findByUserId(Long userId) {
        List<DemandeCredit> demandes = new ArrayList<>();
        String sql = "SELECT * FROM DemandesCredit WHERE userId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DemandeCredit demande = mapResultSetToDemandeCredit(rs);
                    demandes.add(demande);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return demandes;
    }

    @Override
    public List<DemandeCredit> findByStatut(String statut) {
        List<DemandeCredit> demandes = new ArrayList<>();
        String sql = "SELECT * FROM DemandesCredit WHERE statut = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, statut);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DemandeCredit demande = mapResultSetToDemandeCredit(rs);
                    demandes.add(demande);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return demandes;
    }

    @Override
    public List<DemandeCredit> findAll() {
        List<DemandeCredit> demandes = new ArrayList<>();
        String sql = "SELECT * FROM DemandesCredit";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                DemandeCredit demande = mapResultSetToDemandeCredit(rs);
                demandes.add(demande);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return demandes;
    }

    @Override
    public DemandeCredit findById(Long id) {
        String sql = "SELECT * FROM DemandesCredit WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDemandeCredit(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public DemandeCredit save(DemandeCredit demande) {
        String sql = "INSERT INTO DemandesCredit (userId, montant, motif, statut, dateCreation, dateTraitement, commentaire) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set default values if not provided
            if (demande.getDateCreation() == null) {
                demande.setDateCreation(LocalDate.now());
            }
            if (demande.getStatut() == null) {
                demande.setStatut("EN_ATTENTE");
            }

            stmt.setLong(1, demande.getUserId());
            stmt.setDouble(2, demande.getMontant());
            stmt.setString(3, demande.getMotif());
            stmt.setString(4, demande.getStatut());
            stmt.setDate(5, Date.valueOf(demande.getDateCreation()));

            if (demande.getDateTraitement() != null) {
                stmt.setDate(6, Date.valueOf(demande.getDateTraitement()));
            } else {
                stmt.setNull(6, Types.DATE);
            }

            stmt.setString(7, demande.getCommentaire());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        demande.setId(generatedKeys.getLong(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return demande;
    }

    @Override
    public void update(DemandeCredit demande) {
        String sql = "UPDATE DemandesCredit SET montant = ?, motif = ?, statut = ?, dateTraitement = ?, commentaire = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, demande.getMontant());
            stmt.setString(2, demande.getMotif());
            stmt.setString(3, demande.getStatut());

            if (demande.getDateTraitement() != null) {
                stmt.setDate(4, Date.valueOf(demande.getDateTraitement()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.setString(5, demande.getCommentaire());
            stmt.setLong(6, demande.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(DemandeCredit demande) {
        deleteById(demande.getId());
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM DemandesCredit WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateStatut(Long id, String statut, String commentaire) {
        String sql = "UPDATE DemandesCredit SET statut = ?, commentaire = ?, dateTraitement = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, statut);
            stmt.setString(2, commentaire);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            stmt.setLong(4, id);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private DemandeCredit mapResultSetToDemandeCredit(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long userId = rs.getLong("userId");
        Double montant = rs.getDouble("montant");
        String motif = rs.getString("motif");
        String statut = rs.getString("statut");
        LocalDate dateCreation = rs.getDate("dateCreation").toLocalDate();

        Date dateTraitementSql = rs.getDate("dateTraitement");
        LocalDate dateTraitement = dateTraitementSql != null ? dateTraitementSql.toLocalDate() : null;

        String commentaire = rs.getString("commentaire");

        return DemandeCredit.builder()
                .id(id)
                .userId(userId)
                .montant(montant)
                .motif(motif)
                .statut(statut)
                .dateCreation(dateCreation)
                .dateTraitement(dateTraitement)
                .commentaire(commentaire)
                .build();
    }
}