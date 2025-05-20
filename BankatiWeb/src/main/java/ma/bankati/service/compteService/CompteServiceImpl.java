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
            compte.setSolde(compte.getSolde() + montant);
            compteDao.update(compte);
        }
    }

    @Override
    public void debiterCompte(double montant) {
        if (currentUserId == null) {
            return;
        }

        Compte compte = compteDao.findByUserId(currentUserId);
        if (compte != null && compte.getSolde() >= montant) {
            compte.setSolde(compte.getSolde() - montant);
            compteDao.update(compte);
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
            return true;
        }
        return false;
    }

    public double getSoldeForUser(Long userId) {
        Compte compte = compteDao.findByUserId(userId);
        return compte != null ? compte.getSolde() : 0.0;
    }
}