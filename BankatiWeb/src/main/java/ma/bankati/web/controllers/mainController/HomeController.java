package ma.bankati.web.controllers.mainController;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.bankati.model.data.Devise;
import ma.bankati.model.data.MoneyData;
import ma.bankati.model.users.User;
import ma.bankati.service.compteService.CompteServiceImpl;
import ma.bankati.service.compteService.ICompteService;
import ma.bankati.service.moneyServices.IMoneyService;
import ma.bankati.service.moneyServices.MultiCurrencyService;

import java.io.IOException;

@WebServlet(urlPatterns = "/home", loadOnStartup = 1)
public class HomeController extends HttpServlet
{
    private IMoneyService service;

    @Override
    public void init() throws ServletException {
        System.out.println("HomeController créé et initialisé");
        service = (IMoneyService) getServletContext().getAttribute("moneyService");
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

        // Si le service est MultiCurrencyService, utiliser la devise sélectionnée
        if (service instanceof MultiCurrencyService) {
            MultiCurrencyService multiService = (MultiCurrencyService) service;
            // Si CompteService est disponible, définir l'utilisateur actuel
            ICompteService compteService = (ICompteService) getServletContext().getAttribute("compteService");
            if (compteService instanceof CompteServiceImpl && user != null) {
                ((CompteServiceImpl) compteService).setCurrentUserId(user.getId());
            }
            result = multiService.convertData(deviseSelectionnee);
        } else {
            result = service.convertData();
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