package ma.bankati.model.credit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DemandeCredit {

    private Long id;
    private Long userId;
    private Double montant;
    private String motif;
    private String statut; // EN_ATTENTE, APPROUVEE, REFUSEE
    private LocalDate dateCreation;
    private LocalDate dateTraitement;
    private String commentaire; // Commentaire de l'admin

    public boolean isModifiable() {
        return "EN_ATTENTE".equals(statut);
    }

    // Méthodes utilitaires pour la conversion vers java.util.Date pour JSTL
    public Date getDateCreationAsDate() {
        return dateCreation != null ?
                Date.from(dateCreation.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
    }

    public Date getDateTraitementAsDate() {
        return dateTraitement != null ?
                Date.from(dateTraitement.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
    }
}