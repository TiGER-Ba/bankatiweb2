package ma.bankati.service.compteService;

import ma.bankati.dao.compteDao.ICompteDao;
import ma.bankati.model.compte.Compte;

public class CompteServiceImpl implements ICompteService {

    private ICompteDao compteDao;
    private Long currentUserId;

    public CompteServiceImpl() {
    }

    public CompteServiceImpl(ICompteDao compteDao) {
        this.compteDao = compteDao;
    }

    public void setCurrentUserId(Long userId) {
        this.currentUserId = userId;
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    @Override
    public double getSolde() {
        if (currentUserId == null) {
            return 0.0; // Default value if no user ID provided
        }

        Compte compte = compteDao.findByUserId(currentUserId);
        return compte != null ? compte.getSolde() : 0.0;
    }

    @Override
    public void crediterCompte(double montant) {
        if (currentUserId == null) {
            return;
        }

        Compte compte = compteDao.findByUserId(currentUserId);
        if (compte != null) {
            double nouveauSolde = compte.getSolde() + montant;
            compte.setSolde(nouveauSolde);
            compteDao.update(compte);
            System.out.println("Compte utilisateur " + currentUserId + " crédité de " + montant + " EUR. Nouveau solde: " + nouveauSolde + " EUR");
        } else {
            System.err.println("Erreur: Compte introuvable pour l'utilisateur ID " + currentUserId);
        }
    }

    @Override
    public void debiterCompte(double montant) {
        if (currentUserId == null) {
            return;
        }

        Compte compte = compteDao.findByUserId(currentUserId);
        if (compte != null && compte.getSolde() >= montant) {
            double nouveauSolde = compte.getSolde() - montant;
            compte.setSolde(nouveauSolde);
            compteDao.update(compte);
            System.out.println("Compte utilisateur " + currentUserId + " débité de " + montant + " EUR. Nouveau solde: " + nouveauSolde + " EUR");
        } else if (compte != null) {
            System.err.println("Erreur: Solde insuffisant pour débiter " + montant + " EUR du compte de l'utilisateur " + currentUserId);
        } else {
            System.err.println("Erreur: Compte introuvable pour l'utilisateur ID " + currentUserId);
        }
    }

    @Override
    public boolean updateSolde(double nouveauSolde) {
        if (currentUserId == null) {
            return false;
        }

        Compte compte = compteDao.findByUserId(currentUserId);
        if (compte != null) {
            compte.setSolde(nouveauSolde);
            compteDao.update(compte);
            System.out.println("Solde du compte utilisateur " + currentUserId + " mis à jour à " + nouveauSolde + " EUR");
            return true;
        }
        System.err.println("Erreur: Compte introuvable pour l'utilisateur ID " + currentUserId);
        return false;
    }

    public double getSoldeForUser(Long userId) {
        Compte compte = compteDao.findByUserId(userId);
        return compte != null ? compte.getSolde() : 0.0;
    }
}