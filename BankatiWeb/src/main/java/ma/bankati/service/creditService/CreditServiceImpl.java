package ma.bankati.service.creditService;

import ma.bankati.dao.creditDao.IDemandeCreditDao;
import ma.bankati.dao.compteDao.ICompteDao;
import ma.bankati.model.credit.DemandeCredit;
import ma.bankati.model.compte.Compte;
import ma.bankati.service.compteService.ICompteService;
import ma.bankati.service.compteService.CompteServiceImpl;
import ma.bankati.utils.CreditCompteUtil;
import java.util.List;
import java.time.LocalDate;

public class CreditServiceImpl implements ICreditService {

    private IDemandeCreditDao creditDao;
    private ICompteService compteService;
    private ICompteDao compteDao;

    public CreditServiceImpl() {
    }

    public CreditServiceImpl(IDemandeCreditDao creditDao, ICompteService compteService) {
        this.creditDao = creditDao;
        this.compteService = compteService;
    }

    public void setCompteDao(ICompteDao compteDao) {
        this.compteDao = compteDao;
        System.out.println("CompteDao injecté dans CreditService: " + (compteDao != null));
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
                .statut("EN_ATTENTE")
                .dateCreation(LocalDate.now())
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
            try {
                // 1. Mettre à jour le statut de la demande
                creditDao.updateStatut(creditId, "APPROUVEE", commentaire);
                System.out.println("CREDIT APPROUVÉ - ID: " + creditId + ", Utilisateur: " + demande.getUserId() +
                        ", Montant: " + demande.getMontant() + " EUR");

                // 2. Mise à jour DIRECTE du solde dans la base de données
                boolean updateSuccess = CreditCompteUtil.updateSoldeUserInDB(
                        demande.getUserId(), demande.getMontant());

                if (updateSuccess) {
                    System.out.println("CREDIT RÉUSSI - Solde mis à jour pour l'utilisateur " + demande.getUserId());
                } else {
                    System.err.println("ÉCHEC MÉTHODE DIRECTE - Tentative via CompteDao...");

                    // 2.1 Tentative via compteDao directement si la mise à jour directe a échoué
                    if (compteDao != null) {
                        Compte compte = compteDao.findByUserId(demande.getUserId());
                        if (compte != null) {
                            // Obtenir le solde actuel et ajouter le montant du crédit
                            double nouveauSolde = compte.getSolde() + demande.getMontant();
                            compte.setSolde(nouveauSolde);

                            // Mettre à jour le compte
                            compteDao.update(compte);
                            System.out.println("CREDIT VIA COMPTEDAO - Utilisateur " + demande.getUserId() +
                                    " crédité de " + demande.getMontant() + " EUR. Nouveau solde: " +
                                    nouveauSolde + " EUR");
                        } else {
                            System.err.println("ÉCHEC COMPTEDAO - Compte introuvable pour l'utilisateur " + demande.getUserId());
                        }
                    } else {
                        System.err.println("ÉCHEC COMPTEDAO - compteDao non disponible");
                    }
                }

                // 3. Tentative via le service de compte (si tout le reste a échoué)
                if (!updateSuccess && compteDao == null && compteService != null) {
                    try {
                        if (compteService instanceof CompteServiceImpl) {
                            CompteServiceImpl impl = (CompteServiceImpl) compteService;

                            // Sauvegarder l'ID utilisateur actuel
                            Long currentUserId = null;
                            try {
                                java.lang.reflect.Field field = impl.getClass().getDeclaredField("currentUserId");
                                field.setAccessible(true);
                                currentUserId = (Long) field.get(impl);
                            } catch (Exception e) {
                                // Ignorer l'erreur
                            }

                            // Définir l'ID utilisateur et créditer le compte
                            impl.setCurrentUserId(demande.getUserId());
                            compteService.crediterCompte(demande.getMontant());
                            System.out.println("CREDIT VIA SERVICE - Utilisateur " + demande.getUserId() +
                                    " crédité de " + demande.getMontant() + " EUR");

                            // Restaurer l'ID utilisateur
                            if (currentUserId != null) {
                                impl.setCurrentUserId(currentUserId);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("ERREUR lors du crédit via service: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                // 4. Vérifier le solde après toutes les tentatives
                if (compteDao != null) {
                    CreditCompteUtil.getSoldeUserFromDB(demande.getUserId(), compteDao);
                }

            } catch (Exception e) {
                System.err.println("ERREUR lors de l'approbation du crédit: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (demande != null) {
            System.err.println("ERREUR: La demande n'est pas en attente (statut actuel: " + demande.getStatut() + ")");
        } else {
            System.err.println("ERREUR: Demande introuvable (ID: " + creditId + ")");
        }
    }

    @Override
    public void refuserDemande(Long creditId, String commentaire) {
        creditDao.updateStatut(creditId, "REFUSEE", commentaire);
    }
}