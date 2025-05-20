package ma.bankati.web.controllers.mainController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.bankati.dao.compteDao.ICompteDao;
import ma.bankati.model.compte.Compte;
import ma.bankati.model.data.Devise;
import ma.bankati.model.data.MoneyData;
import ma.bankati.model.users.ERole;
import ma.bankati.model.users.User;
import ma.bankati.service.compteService.CompteServiceImpl;
import ma.bankati.service.compteService.ICompteService;
import ma.bankati.service.moneyServices.IMoneyService;
import ma.bankati.service.moneyServices.MultiCurrencyService;
import ma.bankati.service.statsService.StatsService;
import ma.bankati.utils.CreditCompteUtil;

import java.io.IOException;

@WebServlet(urlPatterns = "/home", loadOnStartup = 1)
public class HomeController extends HttpServlet
{
    private IMoneyService service;
    private StatsService statsService;
    private ICompteDao compteDao;

    @Override
    public void init() throws ServletException {
        System.out.println("HomeController créé et initialisé");
        service = (IMoneyService) getServletContext().getAttribute("moneyService");
        statsService = (StatsService) getServletContext().getAttribute("statsService");
        compteDao = (ICompteDao) getServletContext().getAttribute("compteDao");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("Call for HomeController doGet Method");

        // Récupérer l'utilisateur connecté
        User user = (User) request.getSession().getAttribute("connectedUser");

        // Récupérer la devise sélectionnée depuis la session ou le paramètre
        String deviseParam = request.getParameter("devise");
        Devise deviseSelectionnee = Devise.Dh; // Par défaut

        if (deviseParam != null) {
            try {
                deviseSelectionnee = Devise.valueOf(deviseParam);
                // Sauvegarder le choix dans la session
                request.getSession().setAttribute("deviseSelectionnee", deviseSelectionnee);
            } catch (IllegalArgumentException e) {
                // Devise invalide, garder la valeur par défaut
            }
        } else {
            // Récupérer de la session si pas de paramètre
            Devise deviseSession = (Devise) request.getSession().getAttribute("deviseSelectionnee");
            if (deviseSession != null) {
                deviseSelectionnee = deviseSession;
            }
        }

        MoneyData result;

        // Pour les administrateurs, nous affichons les statistiques, pas le solde
        if (user != null && user.getRole() == ERole.ADMIN) {
            // Ajouter les statistiques pour l'admin
            if (statsService != null) {
                request.setAttribute("nombreUtilisateurs", statsService.getNombreUtilisateurs());
                request.setAttribute("nombreDemandesEnAttente", statsService.getNombreDemandesEnAttente());
                request.setAttribute("montantTotalCreditApprouve", statsService.getMontantTotalCreditApprouve());
            }

            // Valeur par défaut pour l'affichage
            result = new MoneyData(0.0, deviseSelectionnee);
        }
        // Pour les clients, nous récupérons leur solde directement depuis la base de données
        else if (user != null && compteDao != null) {
            try {
                // Récupérer le compte de l'utilisateur directement de la base de données
                Compte compte = compteDao.findByUserId(user.getId());

                if (compte != null) {
                    double solde = compte.getSolde();
                    System.out.println("SOLDE RÉCUPÉRÉ - User ID: " + user.getId() + ", Solde: " + solde + " EUR");

                    // Convertir le solde selon la devise sélectionnée
                    if (service instanceof MultiCurrencyService) {
                        MultiCurrencyService multiService = (MultiCurrencyService) service;
                        result = multiService.convertAmount(solde, deviseSelectionnee);
                    } else {
                        result = new MoneyData(solde, deviseSelectionnee);
                    }
                } else {
                    System.err.println("COMPTE NON TROUVÉ - User ID: " + user.getId());
                    result = new MoneyData(0.0, deviseSelectionnee);
                }
            } catch (Exception e) {
                System.err.println("ERREUR lors de la récupération du solde: " + e.getMessage());
                e.printStackTrace();
                result = new MoneyData(0.0, deviseSelectionnee);
            }
        }
        // Cas par défaut (utiliser le service de données en mémoire)
        else {
            // Convertir via le service standard
            if (service instanceof MultiCurrencyService) {
                MultiCurrencyService multiService = (MultiCurrencyService) service;
                result = multiService.convertData(deviseSelectionnee);
            } else {
                result = service.convertData();
            }
        }

        request.setAttribute("result", result);
        request.setAttribute("deviseSelectionnee", deviseSelectionnee);

        // 🔁 Récupérer le chemin de la vue injecté par le filtre
        String viewPath = (String) request.getAttribute("viewPath");

        if (viewPath == null) {
            // Cas de sécurité si quelqu'un arrive ici sans rôle (non connecté ?)
            response.sendRedirect("login");
            return;
        }

        request.getRequestDispatcher(viewPath).forward(request, response);
    }

    @Override
    public void destroy() {
        System.out.println("HomeController détruit");
    }
}