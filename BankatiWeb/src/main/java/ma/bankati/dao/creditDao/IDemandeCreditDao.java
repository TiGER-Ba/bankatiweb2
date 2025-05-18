package ma.bankati.dao.creditDao;

import ma.bankati.dao.userDao.CrudDao;
import ma.bankati.model.credit.DemandeCredit;
import java.util.List;

public interface IDemandeCreditDao extends CrudDao<DemandeCredit, Long> {

    List<DemandeCredit> findByUserId(Long userId);

    List<DemandeCredit> findByStatut(String statut);

    List<DemandeCredit> findAll();

    void updateStatut(Long id, String statut, String commentaire);
}