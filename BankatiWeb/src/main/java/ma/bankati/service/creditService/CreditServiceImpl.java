package ma.bankati.service.creditService;

import ma.bankati.dao.creditDao.IDemandeCreditDao;
import ma.bankati.model.credit.DemandeCredit;
import ma.bankati.service.compteService.ICompteService;
import java.util.List;

public class CreditServiceImpl implements ICreditService {

    private IDemandeCreditDao creditDao;
    private ICompteService compteService;

    public CreditServiceImpl() {
    }

    public CreditServiceImpl(IDemandeCreditDao creditDao, ICompteService compteService) {
        this.creditDao = creditDao;
        this.compteService = compteService;
    }

    @Override
    public List<DemandeCredit> getMesDemandesCredit(Long userId) {
        return creditDao.findByUserId(userId);
    }

    @Override
    public DemandeCredit ajouterDemande(Long userId, Double montant, String motif) {
        // Convertir le montant de DH vers Euro avant de sauvegarder
        // Assumant que les montants sont toujours saisis en DH
        Double montantEnEuro = montant / 10.0; // 1 Euro = 10 DH

        DemandeCredit demande = DemandeCredit.builder()
                .userId(userId)
                .montant(montantEnEuro) // Sauvegarder en Euro
                .motif(motif)
                .build();
        return creditDao.save(demande);
    }

    @Override
    public boolean supprimerDemande(Long creditId, Long userId) {
        DemandeCredit demande = creditDao.findById(creditId);
        if (demande != null &&
                demande.getUserId().equals(userId) &&
                demande.isModifiable()) {
            creditDao.deleteById(creditId);
            return true;
        }
        return false;
    }

    @Override
    public List<DemandeCredit> getToutesLesDemandes() {
        return creditDao.findAll();
    }

    @Override
    public List<DemandeCredit> getDemandesEnAttente() {
        return creditDao.findByStatut("EN_ATTENTE");
    }

    @Override
    public void approuverDemande(Long creditId, String commentaire) {
        DemandeCredit demande = creditDao.findById(creditId);
        if (demande != null && "EN_ATTENTE".equals(demande.getStatut())) {
            // Mettre à jour le statut
            creditDao.updateStatut(creditId, "APPROUVEE", commentaire);

            // Créditer le compte avec le montant du crédit
            if (compteService != null) {
                // Le montant dans la demande est déjà en Euro
                // donc on le crédite directement
                compteService.crediterCompte(demande.getMontant());
                System.out.println("Crédit approuvé: " + demande.getMontant() + " EUR ajouté au compte");
            }
        }
    }

    @Override
    public void refuserDemande(Long creditId, String commentaire) {
        creditDao.updateStatut(creditId, "REFUSEE", commentaire);
    }
}