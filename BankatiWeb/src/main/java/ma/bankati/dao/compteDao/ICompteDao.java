package ma.bankati.dao.compteDao;

import ma.bankati.model.compte.Compte;
import java.util.List;

public interface ICompteDao {
    Compte findByUserId(Long userId);
    List<Compte> findAll();
    Compte save(Compte compte);
    void update(Compte compte);
    void updateSolde(Long userId, Double nouveauSolde);
}