package ma.bankati.web.controllers.creditControllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.bankati.model.credit.DemandeCredit;
import ma.bankati.model.users.User;
import ma.bankati.service.creditService.ICreditService;

import java.io.IOException;
import java.util.List;

@WebServlet("/client/credits/*")
public class ClientCreditController extends HttpServlet {

    private ICreditService creditService;

    @Override
    public void init() throws ServletException {
        creditService = (ICreditService) getServletContext().getAttribute("creditService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            User user = (User) req.getSession().getAttribute("connectedUser");
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // Afficher la liste des demandes
                List<DemandeCredit> demandes = creditService.getMesDemandesCredit(user.getId());
                req.setAttribute("demandes", demandes);
                req.getRequestDispatcher("/public/credits.jsp").forward(req, resp);

            } else if (pathInfo.equals("/new")) {
                // Afficher le formulaire d'ajout
                req.getRequestDispatcher("/public/credit-form.jsp").forward(req, resp);

            } else if (pathInfo.equals("/delete")) {
                // Supprimer une demande
                Long creditId = Long.parseLong(req.getParameter("id"));
                creditService.supprimerDemande(creditId, user.getId());
                resp.sendRedirect(req.getContextPath() + "/client/credits");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur lors du traitement de la demande");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = (User) req.getSession().getAttribute("connectedUser");
        String pathInfo = req.getPathInfo();

        if (pathInfo.equals("/save")) {
            Double montant = Double.parseDouble(req.getParameter("montant"));
            String motif = req.getParameter("motif");

            creditService.ajouterDemande(user.getId(), montant, motif);
            resp.sendRedirect(req.getContextPath() + "/client/credits");
        }
    }
}