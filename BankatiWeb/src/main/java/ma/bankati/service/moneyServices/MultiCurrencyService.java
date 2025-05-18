package ma.bankati.service.moneyServices;

import ma.bankati.dao.dataDao.IDao;
import ma.bankati.model.data.Devise;
import ma.bankati.model.data.MoneyData;

public class MultiCurrencyService implements IMoneyService {

    private IDao dao;
    private Devise deviseSelectionnee = Devise.Dh; // Par défaut

    // Taux de conversion par rapport au Euro (base)
    private static final double TAUX_EUR = 1.0;
    private static final double TAUX_USD = 1.08;
    private static final double TAUX_GBP = 0.84;
    private static final double TAUX_DH = 10.0;

    public MultiCurrencyService() {
    }

    public MultiCurrencyService(IDao dao) {
        this.dao = dao;
    }

    public void setDevise(Devise devise) {
        this.deviseSelectionnee = devise;
    }

    public Devise getDevise() {
        return deviseSelectionnee;
    }

    @Override
    public MoneyData convertData() {
        double montantEuro = dao.fetchData(); // Montant en Euro
        double montantConverti = 0.0;

        switch (deviseSelectionnee) {
            case €:
                montantConverti = montantEuro * TAUX_EUR;
                break;
            case $:
                montantConverti = montantEuro * TAUX_USD;
                break;
            case £:
                montantConverti = montantEuro * TAUX_GBP;
                break;
            case Dh:
                montantConverti = montantEuro * TAUX_DH;
                break;
        }

        return new MoneyData(montantConverti, deviseSelectionnee);
    }

    public MoneyData convertData(Devise devise) {
        setDevise(devise);
        return convertData();
    }
}