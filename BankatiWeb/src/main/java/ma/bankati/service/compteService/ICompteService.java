package ma.bankati.service.compteService;

public interface ICompteService {

    double getSolde();
    void crediterCompte(double montant);
    void debiterCompte(double montant);
    boolean updateSolde(double nouveauSolde);
}