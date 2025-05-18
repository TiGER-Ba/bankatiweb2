package ma.bankati.service.compteService;

import ma.bankati.dao.compteDao.ICompteDao;
import ma.bankati.model.compte.Compte;

public class CompteServiceImpl implements ICompteService {

    private ICompteDao compteDao;
    private Long currentUserId; // Pour identifier l'utilisateur actuel

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
            return getSoldeFromFile(); // Fallback vers l'ancien système
        }

        Compte compte = compteDao.findByUserId(currentUserId);
        return compte != null ? compte.getSolde() : 0.0;
    }

    // Méthode pour l'ancien système (fichier compte.txt)
    private double getSoldeFromFile() {
        try {
            var path = getClass().getClassLoader().getResource("FileBase/compte.txt");
            if (path != null) {
                var lines = java.nio.file.Files.readAllLines(java.nio.file.Paths.get(path.toURI()));
                if (lines.size() > 1) {
                    return Double.parseDouble(lines.get(1).trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public void crediterCompte(double montant) {
        if (currentUserId == null) {
            // Utiliser l'ancien système
            crediterCompteFile(montant);
            return;
        }

        Compte compte = compteDao.findByUserId(currentUserId);
        if (compte != null) {
            compte.setSolde(compte.getSolde() + montant);
            compteDao.update(compte);
        }
    }

    private void crediterCompteFile(double montant) {
        double soldeActuel = getSoldeFromFile();
        double nouveauSolde = soldeActuel + montant;
        updateSolde(nouveauSolde);
    }

    @Override
    public void debiterCompte(double montant) {
        if (currentUserId == null) {
            double soldeActuel = getSoldeFromFile();
            double nouveauSolde = soldeActuel - montant;
            if (nouveauSolde >= 0) {
                updateSolde(nouveauSolde);
            }
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
        try {
            var path = getClass().getClassLoader().getResource("FileBase/compte.txt");
            if (path != null) {
                java.nio.file.Files.write(
                        java.nio.file.Paths.get(path.toURI()),
                        java.util.Arrays.asList("Solde du compte", String.valueOf(nouveauSolde))
                );
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public double getSoldeForUser(Long userId) {
        Compte compte = compteDao.findByUserId(userId);
        return compte != null ? compte.getSolde() : 0.0;
    }
}