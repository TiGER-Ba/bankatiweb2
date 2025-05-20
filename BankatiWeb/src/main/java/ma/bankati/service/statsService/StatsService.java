package ma.bankati.service.statsService;

import ma.bankati.dao.userDao.IUserDao;
import ma.bankati.dao.creditDao.IDemandeCreditDao;
import ma.bankati.model.credit.DemandeCredit;
import ma.bankati.model.users.ERole;
import ma.bankati.model.users.User;

import java.util.List;

public class StatsService {
    private final IUserDao userDao;
    private final IDemandeCreditDao creditDao;

    public StatsService(IUserDao userDao, IDemandeCreditDao creditDao) {
        this.userDao = userDao;
        this.creditDao = creditDao;
    }

    public int getNombreUtilisateurs() {
        List<User> users = userDao.findAll();
        // Ne compter que les utilisateurs avec le rôle USER
        return (int) users.stream()
                .filter(user -> user.getRole() == ERole.USER)
                .count();
    }

    public int getNombreDemandesEnAttente() {
        List<DemandeCredit> demandesEnAttente = creditDao.findByStatut("EN_ATTENTE");
        return demandesEnAttente.size();
    }

    public double getMontantTotalCreditApprouve() {
        List<DemandeCredit> creditsApprouves = creditDao.findByStatut("APPROUVEE");
        double sommeEuros = creditsApprouves.stream()
                .mapToDouble(DemandeCredit::getMontant)
                .sum();

        // Convertir en DH (1 Euro = 10 DH)
        return Math.round(sommeEuros * 10 * 100) / 100.0;  // Arrondir à 2 décimales
    }
}