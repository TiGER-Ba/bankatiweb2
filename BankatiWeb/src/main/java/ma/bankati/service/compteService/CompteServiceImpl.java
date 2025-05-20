package ma.bankati.service.compteService;

import ma.bankati.dao.compteDao.ICompteDao;
import ma.bankati.dao.compteDao.sqlDb.CompteDao;
import ma.bankati.model.compte.Compte;

public class CompteServiceImpl implements ICompteService {

    private ICompteDao compteDao;
    private Long currentUserId;

    public CompteServiceImpl() {
    }

    public CompteServiceImpl(ICompteDao compteDao) {
        this.compteDao = compteDao;
        System.out.println("CompteServiceImpl initialisé avec compteDao: " + (compteDao != null ? "OK" : "NULL!"));
    }

    public void setCurrentUserId(Long userId) {
        this.currentUserId = userId;
        System.out.println("CompteServiceImpl: currentUserId défini à " + userId);
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    @Override
    public double getSolde() {
        if (currentUserId == null) {
            System.err.println("CompteService.getSolde: Aucun userId défini!");
            return 0.0; // Default value if no user ID provided
        }

        Compte compte = compteDao.findByUserId(currentUserId);
        if (compte != null) {
            System.out.println("CompteService.getSolde: UserID=" + currentUserId + ", Solde=" + compte.getSolde() + " EUR");
            return compte.getSolde();
        } else {
            System.err.println("CompteService.getSolde: Compte introuvable pour UserID=" + currentUserId);
            return 0.0;
        }
    }

    @Override
    public void crediterCompte(double montant) {
        if (currentUserId == null) {
            System.err.println("CompteService.crediterCompte: Aucun userId défini!");
            return;
        }

        System.out.println("CompteService.crediterCompte: Tentative de crédit de " + montant + " EUR pour UserID=" + currentUserId);

        // Essayer d'abord la méthode directe si disponible
        if (compteDao instanceof CompteDao) {
            try {
                CompteDao sqlCompteDao = (CompteDao) compteDao;
                boolean success = sqlCompteDao.incrementerSolde(currentUserId, montant);
                if (success) {
                    System.out.println("CompteService.crediterCompte: Crédit direct réussi pour UserID=" + currentUserId +
                            ", Montant=" + montant + " EUR");
                    return;
                } else {
                    System.err.println("CompteService.crediterCompte: Échec du crédit direct, tentative avec méthode standard...");
                }
            } catch (Exception e) {
                System.err.println("CompteService.crediterCompte: Erreur lors du crédit direct: " + e.getMessage());
            }
        }

        // Méthode standard si la méthode directe n'est pas disponible ou a échoué
        Compte compte = compteDao.findByUserId(currentUserId);
        if (compte != null) {
            try {
                double soldeActuel = compte.getSolde();
                double nouveauSolde = soldeActuel + montant;

                System.out.println("CompteService.crediterCompte: Solde actuel=" + soldeActuel +
                        " EUR, Ajout=" + montant + " EUR, Nouveau solde=" + nouveauSolde + " EUR");

                compte.setSolde(nouveauSolde);
                compteDao.update(compte);

                // Vérifier que la mise à jour a fonctionné
                Compte compteApres = compteDao.findByUserId(currentUserId);
                if (compteApres != null) {
                    System.out.println("CompteService.crediterCompte: Solde après mise à jour=" +
                            compteApres.getSolde() + " EUR");

                    if (Math.abs(compteApres.getSolde() - nouveauSolde) < 0.001) {
                        System.out.println("CompteService.crediterCompte: Crédit confirmé pour UserID=" + currentUserId);
                    } else {
                        System.err.println("CompteService.crediterCompte: Anomalie détectée! Solde attendu=" +
                                nouveauSolde + " EUR, Solde réel=" + compteApres.getSolde() + " EUR");

                        // Tentative de correction
                        compteDao.updateSolde(currentUserId, nouveauSolde);
                        System.out.println("CompteService.crediterCompte: Tentative de correction avec updateSolde");
                    }
                }
            } catch (Exception e) {
                System.err.println("CompteService.crediterCompte: Erreur pendant la mise à jour: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("CompteService.crediterCompte: Compte introuvable pour UserID=" + currentUserId);
        }
    }

    @Override
    public void debiterCompte(double montant) {
        if (currentUserId == null) {
            System.err.println("CompteService.debiterCompte: Aucun userId défini!");
            return;
        }

        System.out.println("CompteService.debiterCompte: Tentative de débit de " + montant + " EUR pour UserID=" + currentUserId);

        Compte compte = compteDao.findByUserId(currentUserId);
        if (compte != null) {
            double soldeActuel = compte.getSolde();

            if (soldeActuel >= montant) {
                try {
                    double nouveauSolde = soldeActuel - montant;
                    System.out.println("CompteService.debiterCompte: Solde actuel=" + soldeActuel +
                            " EUR, Débit=" + montant + " EUR, Nouveau solde=" + nouveauSolde + " EUR");

                    compte.setSolde(nouveauSolde);
                    compteDao.update(compte);

                    // Vérifier que la mise à jour a fonctionné
                    Compte compteApres = compteDao.findByUserId(currentUserId);
                    if (compteApres != null) {
                        System.out.println("CompteService.debiterCompte: Solde après mise à jour=" +
                                compteApres.getSolde() + " EUR");

                        if (Math.abs(compteApres.getSolde() - nouveauSolde) < 0.001) {
                            System.out.println("CompteService.debiterCompte: Débit confirmé pour UserID=" + currentUserId);
                        } else {
                            System.err.println("CompteService.debiterCompte: Anomalie détectée! Solde attendu=" +
                                    nouveauSolde + " EUR, Solde réel=" + compteApres.getSolde() + " EUR");

                            // Tentative de correction
                            compteDao.updateSolde(currentUserId, nouveauSolde);
                            System.out.println("CompteService.debiterCompte: Tentative de correction avec updateSolde");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("CompteService.debiterCompte: Erreur pendant la mise à jour: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("CompteService.debiterCompte: Solde insuffisant! Solde actuel=" +
                        soldeActuel + " EUR, Montant demandé=" + montant + " EUR");
            }
        } else {
            System.err.println("CompteService.debiterCompte: Compte introuvable pour UserID=" + currentUserId);
        }
    }

    @Override
    public boolean updateSolde(double nouveauSolde) {
        if (currentUserId == null) {
            System.err.println("CompteService.updateSolde: Aucun userId défini!");
            return false;
        }

        System.out.println("CompteService.updateSolde: Tentative de mise à jour du solde à " +
                nouveauSolde + " EUR pour UserID=" + currentUserId);

        try {
            // Tenter d'abord la mise à jour directe avec updateSolde
            compteDao.updateSolde(currentUserId, nouveauSolde);

            // Vérifier que la mise à jour a fonctionné
            Compte compteApres = compteDao.findByUserId(currentUserId);
            if (compteApres != null) {
                if (Math.abs(compteApres.getSolde() - nouveauSolde) < 0.001) {
                    System.out.println("CompteService.updateSolde: Mise à jour réussie, solde=" +
                            compteApres.getSolde() + " EUR");
                    return true;
                } else {
                    System.err.println("CompteService.updateSolde: Anomalie détectée! Solde attendu=" +
                            nouveauSolde + " EUR, Solde réel=" + compteApres.getSolde() + " EUR");

                    // Tentative de correction avec update complet
                    Compte compte = compteApres;
                    compte.setSolde(nouveauSolde);
                    compteDao.update(compte);

                    // Vérifier à nouveau
                    Compte compteFinal = compteDao.findByUserId(currentUserId);
                    if (compteFinal != null && Math.abs(compteFinal.getSolde() - nouveauSolde) < 0.001) {
                        System.out.println("CompteService.updateSolde: Correction réussie, solde final=" +
                                compteFinal.getSolde() + " EUR");
                        return true;
                    } else {
                        System.err.println("CompteService.updateSolde: Échec de la correction!");
                        return false;
                    }
                }
            } else {
                System.err.println("CompteService.updateSolde: Impossible de vérifier la mise à jour, compte introuvable");
                return false;
            }
        } catch (Exception e) {
            System.err.println("CompteService.updateSolde: Erreur pendant la mise à jour: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public double getSoldeForUser(Long userId) {
        if (userId == null) {
            System.err.println("CompteService.getSoldeForUser: userId est null!");
            return 0.0;
        }

        System.out.println("CompteService.getSoldeForUser: Recherche du solde pour UserID=" + userId);

        Compte compte = compteDao.findByUserId(userId);
        if (compte != null) {
            System.out.println("CompteService.getSoldeForUser: Solde trouvé pour UserID=" +
                    userId + ", Solde=" + compte.getSolde() + " EUR");
            return compte.getSolde();
        } else {
            System.err.println("CompteService.getSoldeForUser: Compte introuvable pour UserID=" + userId);
            return 0.0;
        }
    }

    /**
     * Méthode directe pour incrémenter le solde d'un utilisateur spécifique
     * sans modifier currentUserId
     */
    public boolean crediterCompteUtilisateur(Long userId, double montant) {
        if (userId == null) {
            System.err.println("CompteService.crediterCompteUtilisateur: userId est null!");
            return false;
        }

        System.out.println("CompteService.crediterCompteUtilisateur: Tentative de crédit de " +
                montant + " EUR pour UserID=" + userId);

        // Sauvegarder l'ID courant
        Long saveCurrentUserId = this.currentUserId;

        try {
            // Temporairement définir l'ID courant
            this.currentUserId = userId;

            // Utiliser la méthode standard
            this.crediterCompte(montant);

            // Vérifier le résultat
            Compte compte = compteDao.findByUserId(userId);
            return (compte != null);

        } catch (Exception e) {
            System.err.println("CompteService.crediterCompteUtilisateur: Erreur: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Restaurer l'ID courant
            this.currentUserId = saveCurrentUserId;
        }
    }
}