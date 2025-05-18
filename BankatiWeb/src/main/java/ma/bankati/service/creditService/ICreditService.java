package ma.bankati.service.creditService;

import ma.bankati.model.credit.DemandeCredit;
import java.util.List;

public interface ICreditService {

    // Pour les clients
    List<DemandeCredit> getMesDemandesCredit(Long userId);
    DemandeCredit ajouterDemande(Long userId, Double montant, String motif);
    boolean supprimerDemande(Long creditId, Long userId);

    // Pour les admins
    List<DemandeCredit> getToutesLesDemandes();
    List<DemandeCredit> getDemandesEnAttente();
    void approuverDemande(Long creditId, String commentaire);
    void refuserDemande(Long creditId, String commentaire);
}